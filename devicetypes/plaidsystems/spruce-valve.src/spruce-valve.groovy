/**
 *  Copyright 2020 Plaid Systems
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
 */
metadata {
	definition (name: "Spruce Valve", namespace: "plaidsystems", author: "Plaid Systems", mnmn: "SmartThingsCommunity", vid: "43db5ded-7e37-3d39-9793-5c88203c81ce"){
    	capability "Actuator"
		capability "Valve"
		capability "Sensor"
		capability "Health Check"
        capability "heartreturn55003.valveDuration"
        
        command "open"
        command "close"
        command "setValveDuration"
        
        attribute "valveDuration", "NUMBER"
	}

	// tile definitions
	tiles {
		standardTile("valve", "device.valve", width: 2, height: 2) {
			state "closed", label: "closed", action: "open"
			state "open", label: "open", action: "close"
		}        
		main "valve"
		details(["valve"])
	}
}

def installed() {	
	initialize()	
}

def updated() {
	initialize()
}

private initialize() {
	//log.trace "Executing 'initialize'"
	
    sendEvent(name: "valve", value: "closed")
    sendEvent(name: "valveDuration", value: 5, unit: "mins")
    
    sendEvent(name: "DeviceWatch-DeviceStatus", value: "online")
	sendEvent(name: "healthStatus", value: "online")
	sendEvent(name: "DeviceWatch-Enroll", value: [protocol: "cloud", scheme:"untracked"].encodeAsJson(), displayed: false)    
}

def parse(String onoff) {
	log.debug "Child Desc: ${onoff}"
    sendEvent(name: "valve", value: onoff)    
}

def open() {	
    def eventMap = createEvent(dni: device.deviceNetworkId, value: 'open', duration: device.latestValue("valveDuration").toInteger())
    parent.valveOn(eventMap)
}

def close() {
	def eventMap = createEvent(dni: device.deviceNetworkId, value: 'closed', duration: 0)
    parent.valveOff(eventMap)
}

def setValveDuration(valveDuration){
	log.debug valveDuration
    sendEvent(name: "valveDuration", value: valveDuration, unit: "mins")
}

def ping() {
	// Intentionally left blank as parent should handle this
}