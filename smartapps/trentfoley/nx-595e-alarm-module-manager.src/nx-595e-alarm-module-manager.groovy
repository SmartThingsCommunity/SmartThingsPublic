/**
 *  Copyright 2016 Trent Foley
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 *	NX-595e Alarm Module Service Manager
 *
 *	Author: Trent Foley
 *	Date: 2016-03-27
 */
definition(
    name: "NX-595e Alarm Module Manager",
    namespace: "trentfoley",
    author: "Trent Foley",
    description: "Connect your NX-595e Alarm Module to SmartThings.",
    category: "My Apps",
    iconUrl: "https://i.ytimg.com/i/m_u-jwM0DlXns-U2tzp1Hw/1.jpg",
    iconX2Url: "https://i.ytimg.com/i/m_u-jwM0DlXns-U2tzp1Hw/1.jpg",
    iconX3Url: "https://i.ytimg.com/i/m_u-jwM0DlXns-U2tzp1Hw/1.jpg",
    singleInstance: true
) { }

preferences {
	section("Select the contact sensors to trigger alarm state refresh...") {
		input "contactSensors", "capability.contactSensor", required: true, title: "Contact Sensors", multiple: true
    }
}

def getProxyHost() { "192.168.1.96:595" }
def getChildNamespace() { "trentfoley" }
def getZoneChildDeviceName() { "NX-595e Zone" }
def getAlarmChildDeviceName() { "NX-595e Alarm" }

private debugEvent(message, displayEvent = false) {
	def results = [
		name: "debug",
		descriptionText: message,
		displayed: displayEvent
	]
	log.debug "${message}"
	sendEvent(results)
}

private errorEvent(message, displayEvent = true) {
	def results = [
		name: "error",
		descriptionText: message,
		displayed: displayEvent
	]
	log.error "${message}"
	sendEvent(results)
}

def installed() {
    debugEvent("installed()")
    initialize()
}

def updated() {
    debugEvent("updated()")
    unsubscribe()
    state.subscribed = false
    initialize()
}

def initialize() {
    debugEvent("initialize() subscribe state is ${state.subscribe}")
    
    // Ensure alarm device exists
    def dni = getAlarmDeviceNetworkId()
    def alarmDevice = getChildDevice(dni)
    if(!alarmDevice) {
        alarmDevice = addChildDevice(childNamespace, alarmChildDeviceName, dni, null, [ label: "Alarm System" ])
        debugEvent("Created ${alarmDevice.displayName} with device network id: ${dni}")
    } else {
        debugEvent("Found already existing ${alarmDevice.displayName} with device network id: ${dni}")
    }
    
    if(!state.subscribed) {
        log.debug "subscribe to location"
        subscribe(location, null, locationHandler, [filterEvents:false])
        subscribe(location, "alarmSystemStatus", alarmHandler)
        subscribe(contactSensors, "contact", contactHandler)
        state.subscribed = true
    }
    
    requestStatus()
}

def contactHandler(evt) {
    debugEvent("contactHandler()")
	requestStatus()
}

def requestStatus() {
	debugEvent("requestStatus()")
	def hubAction = new physicalgraph.device.HubAction(
        method: "GET",
        path: "/alarm/status",
        headers: [
            HOST: getProxyHost()
        ]
    )
    debugEvent(hubAction)
    sendHubCommand(hubAction)
}

/**
 * Called when location has changed, contains information from
 * network transactions.
 *
 * @param evt Holds event information
 */
def locationHandler(evt) {
	debugEvent("locationHandler()")
    
	if ("${evt.source}" == "HUB") {
        def parsedMessage = parseLanMessage(evt.description)
        def alarmStatus = parsedMessage?.json
        
        debugEvent(alarmStatus)
        def armType = alarmStatus?.ArmType
        if (armType) {
        	def systemStatus = alarmStatus.SystemStatus
        	debugEvent("Alarm System Status: ${armType}: ${systemStatus}")
            
            // Update system status
        	def alarmDevice = getChildDevice(getAlarmDeviceNetworkId())
            alarmDevice.sendEvent(name: "armType", value: armType, descriptionText: armType == "away" ? "Armed Away" : armType == "stay" ? "Armed Stay" : "Off");
            alarmDevice.sendEvent(name: "status", value: systemStatus, descriptionText: systemStatus);
        	alarmDevice.sendEvent(
            	name: "switch",
                value: alarmStatus.IsChimeEnabled ? "on" : "off",
                descriptionText: alarmStatus.IsChimeEnabled ? "Chime Enabled" : "Chime Disabled"
            )
            
            // Update zones
            alarmStatus?.Zones?.each { zone ->
                def zoneIndex = zone.Index
                def zoneName = zone.Name
                def zoneStatus = zone.Status
                if (zoneName && zoneStatus) {
                    def dni = getDeviceNetworkId(zoneIndex)
                    def device = getChildDevice(dni)
                    if(!device) {
                        device = addChildDevice(childNamespace, zoneChildDeviceName, dni, null, [ label: "${zoneName}" ])
                        debugEvent("Created ${device.displayName} (${zoneStatus}, ${zone.IsBypassed ? "Bypassed" : "Not Bypassed"}) with device network id: ${dni}")
                    } else {
                        debugEvent("Found already existing ${device.displayName} (${zoneStatus}, ${zone.IsBypassed ? "Bypassed" : "Not Bypassed"}) with device network id: ${dni}")
                    }

                    // Update zone attributes
                    device.sendEvent(name: "zoneIndex", value: zoneIndex, displayed: false)

                    device.sendEvent(
                    	name: "switch",
                        value: zone.IsBypassed ? "on" : "off",
                        descriptionText: zone.IsBypassed ? "Zone Bypassed" : "Zone Not Bypassed",
                    )
                    
                    device.sendEvent(name: "status", value: zone.IsBypassed ? "Bypass" : zoneStatus)

                    def contactValue = zoneStatus == "Ready" ? "closed" : "open"
                    device.sendEvent(name: "contact", value: contactValue, displayed: false)
                }
            }
        }
    }
}

private getAlarmDeviceNetworkId() {
	return getDeviceNetworkId(99)
}

private String getDeviceNetworkId(int zoneIndex) {
	return [ app.id, zoneIndex ].join('.')
}

// Called from Alarm System child device
def refresh() {
	debugEvent("refresh()")
	requestStatus()
}

def bypass(child) {
	debugEvent("bypass(${child.device.currentValue("zoneIndex")})")
    
	def hubAction = new physicalgraph.device.HubAction(
        method: "POST",
        path: "/zone/bypass/${child.device.currentValue("zoneIndex")}",
        headers: [
            HOST: getProxyHost()
        ]
    )
	debugEvent(hubAction)
    sendHubCommand(hubAction)
}

def chime() {
	debugEvent("chime()")
	
    def hubAction = new physicalgraph.device.HubAction(
        method: "POST",
        path: "/alarm/chime",
        headers: [
            HOST: getProxyHost()
        ]
    )
	debugEvent(hubAction)
    sendHubCommand(hubAction)
}

def alarmHandler(evt) {
	debugEvent("alarmHandler(${evt.value})")
    
    // "off", "away", "stay"
    def hubAction = new physicalgraph.device.HubAction(
        method: "POST",
        path: "/alarm/arm/${evt.value}",
        headers: [
            HOST: getProxyHost()
        ]
    )
	debugEvent(hubAction)
    sendHubCommand(hubAction)
}
