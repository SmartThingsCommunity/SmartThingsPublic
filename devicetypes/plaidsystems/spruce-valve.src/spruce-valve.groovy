/**
 *  Copyright Plaid Systems 2020
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

/*
remove duration slider from presentation until SmartThings fixes Slider setter command
vid: "558e73d3-c800-3669-b276-1d4352eda12b"
*/
 
metadata {
	definition (name: "Spruce Valve", namespace: "plaidsystems", author: "Plaid Systems", mnmn: "SmartThingsCommunity", vid: "ed1871a2-7ee6-31a4-bdb1-d638121226c0"){
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

def parse(String onOff) {
	log.debug "Child Desc: ${onOff}"
	sendEvent(name: "valve", value: onOff)
}

def open() {
	def eventMap = createEvent(dni: device.deviceNetworkId, value: 'open', duration: device.latestValue("valveDuration").toInteger(), label: device.label)
	parent.valveOn(eventMap)
}

def close() {
	def eventMap = createEvent(dni: device.deviceNetworkId, value: 'closed', duration: 0, label: device.label)
	parent.valveOff(eventMap)
}

def setValveDuration(duration) {
	log.debug duration
	sendEvent(name: "valveDuration", value: duration, unit: "mins")
}

def ping() {
	// Intentionally left blank as parent should handle this
}