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
def getChildName() { "NX-595e Zone" }
def getChimeSwitchDeviceName() { "NX-595e Chime" }

private debugEvent(message, displayEvent = false) {
	def results = [
		name: "debug",
		descriptionText: message,
		displayed: displayEvent
	]
	log.debug "${results}"
	sendEvent(results)
}

private errorEvent(message, displayEvent = true) {
	def results = [
		name: "error",
		descriptionText: message,
		displayed: displayEvent
	]
	log.error "${results}"
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
    state.zonesSubscribed = false
    initialize()
}

def initialize() {
    debugEvent("initialize() subscribe state is ${state.subscribe}")
    
    // Ensure chime device exists
    def dni = getDeviceNetworkId(99)
    def chimeDevice = getChildDevice(dni)
    if(!chimeDevice) {
        chimeDevice = addChildDevice(childNamespace, chimeSwitchDeviceName, dni, null, [ label: "Alarm Chime" ])
        debugEvent("Created ${chimeDevice.displayName} with device network id: ${dni}")
    } else {
        debugEvent("Found already existing ${chimeDevice.displayName} with device network id: ${dni}")
    }
    
    if(!state.subscribed) {
        subscribeToCommand(chimeDevice, "on", chimeCommand)
        subscribeToCommand(chimeDevice, "off", chimeCommand)

        log.debug "subscribe to location"
        subscribe(location, null, locationHandler, [filterEvents:false])
        subscribe(location, "alarmSystemStatus", alarmHandler)
        subscribe(contactSensors, "contact", contactHandler)
        state.subscribed = true
    }
    
    requestZones()
}

def contactHandler(evt) {
    debugEvent("contactHandler()")
	requestZones()
}

def requestZones() {
	def hubAction = new physicalgraph.device.HubAction(
        method: "GET",
        path: "/zone",
        headers: [
            HOST: getProxyHost()
        ]
    )

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
        // Convert the event into something we can use
        def parsedMessage = parseLanMessage(evt.description)
        // log.debug "parsedMessage: ${parsedMessage}"
        
        def zones = parsedMessage?.json    
        if (zones) {
            zones.each { zone ->
                def zoneIndex = zone.Index
            	def zoneName = zone.Name
                def zoneStatus = zone.Status
                if (zoneName && zoneStatus) {
                    def dni = getDeviceNetworkId(zoneIndex)
                    def device = getChildDevice(dni)
                    if(!device) {
                        device = addChildDevice(childNamespace, childName, dni, null, [ label: "${zoneName}" ])
                        debugEvent("Created ${device.displayName} with device network id: ${dni}")
                        subscribeToZone(device)
                    } else {
                        debugEvent("Found already existing ${device.displayName} with device network id: ${dni}")
                        if(!state.zonesSubscribed) {
                            subscribeToZone(device)
                        }
                    }
                    
                    device.setZoneIndex(zoneIndex)
                    device.setZoneStatus(zoneStatus)
                }
            }
            
            state.zonesSubscribed = true
        }
    }
}

private String getDeviceNetworkId(int zoneIndex) {
	return [ app.id, zoneIndex ].join('.')
}

private subscribeToZone(device) {
	debugEvent("subscribeToZone(${device.displayName})")
	subscribeToCommand(device, "on", bypassCommand)
    subscribeToCommand(device, "off", bypassCommand)
}

def chimeCommand(evt) {
	log.debug "chimeCommand(${evt.value})"

}

def bypassCommand(evt) {
    log.debug "bypassCommand(${evt.value}) ${evt.device.displayName} (${evt.device.currentZoneIndex})"
    
    
}

def alarmHandler(evt) {
	debugEvent("alarmHandler()")
    log.debug "the source of this event: ${evt.source}"
    log.debug "Alarm Handler value: ${evt.value}"
    log.debug "alarmSystemStatus: ${location.currentState("alarmSystemStatus")?.value}"
    
    // "off"
    // "away"
    // "stay"
}
