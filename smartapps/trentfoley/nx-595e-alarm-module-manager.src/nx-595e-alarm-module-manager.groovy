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
def getChildName() { "NX-595e Alarm Module Zone" }

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
    state.subscribe = false
    initialize()
}

def initialize() {
    debugEvent("initialize()")
    
    if(!state.subscribe) {
        log.debug "subscribe to location"
        subscribe(location, null, locationHandler, [filterEvents:false])
	    subscribe(contactSensors, "contact", contactHandler)
        state.subscribe = true
    }
    
    def hubAction = new physicalgraph.device.HubAction(
        method: "GET",
        path: "/zone",
        headers: [
            HOST: getProxyHost()
        ]
    )

    sendHubCommand(hubAction)
}

def contactHandler(evt) {
    log.debug "one of the configured contact sensors changed states"
}

/**
 * Called when location has changed, contains information from
 * network transactions.
 *
 * @param evt Holds event information
 */
def locationHandler(evt) {
	debugEvent("locationHandler()")

    // Convert the event into something we can use
    def zoneListResponse = parseLanMessage(evt.description)
    def zones = zoneListResponse.json

    state.zoneDevices = zones.collect { zone ->
    	def dni = getDeviceNetworkId(zone.Index)
        def device = getChildDevice(dni)
        if(!device) {
            device = addChildDevice(childNamespace, childName, dni, null, [ label: "Alarm Zone: ${zone.Name}" ])
            debugEvent("Created ${device.displayName} with device network id: ${dni}")
        } else {
        	debugEvent("Found already existing ${device.displayName} with device network id: ${dni}")
        }
       
        device.setZoneStatus(zone.Status)
        
        return device
    }
    
	
}

private String getDeviceNetworkId(int zoneIndex) {
	return [ app.id, zoneIndex ].join('.')
}

// Poll Child is invoked from the Child Device itself as part of the Poll Capability
def pollChild(child) {
    def deviceNetworkId = child.device.deviceNetworkId
    debugEvent("pollChild(${deviceNetworkId})")

}
