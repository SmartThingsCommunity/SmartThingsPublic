/**
 *  Copyright 2015 SmartThings
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
	definition (name: "Fortrezz Water Valve", namespace: "smartthings", author: "SmartThings", ocfDeviceType: "oic.d.watervalve") {
		capability "Actuator"
		capability "Health Check"
		capability "Valve"
		capability "Refresh"
		capability "Sensor"

		fingerprint deviceId: "0x1000", inClusters: "0x25,0x72,0x86,0x71,0x22,0x70"
		fingerprint mfr:"0084", prod:"0213", model:"0215", deviceJoinName: "FortrezZ Water Valve"
	}

	// simulator metadata
	simulator {
		status "close":  "command: 2503, payload: FF"
		status "open": "command: 2503, payload: 00"

		// reply messages
		reply "2001FF": "command: 2503, payload: FF"
		reply "200100": "command: 2503, payload: 00"
	}

	// tile definitions
	tiles(scale: 2) {
		multiAttributeTile(name:"contact", type: "generic", width: 6, height: 4, canChangeIcon: true){
			tileAttribute ("device.contact", key: "PRIMARY_CONTROL") {
				attributeState "open", label: '${name}', action: "valve.close", icon: "st.valves.water.open", backgroundColor: "#00A0DC", nextState:"closing"
				attributeState "closed", label: '${name}', action: "valve.open", icon: "st.valves.water.closed", backgroundColor: "#ffffff", nextState:"opening"
				attributeState "opening", label: '${name}', action: "valve.close", icon: "st.valves.water.open", backgroundColor: "#00A0DC"
				attributeState "closing", label: '${name}', action: "valve.open", icon: "st.valves.water.closed", backgroundColor: "#ffffff"
			}
		}

		standardTile("refresh", "device.valve", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
			state "default", label:'', action:"refresh.refresh", icon:"st.secondary.refresh"
		}

		main "contact"
		details(["contact","refresh"])
	}
}

def installed(){
// Device-Watch simply pings if no device events received for 32min(checkInterval)
	sendEvent(name: "checkInterval", value: 2 * 15 * 60 + 2 * 60, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID, offlinePingable: "1"])

	response(refresh())
}

def updated(){
// Device-Watch simply pings if no device events received for 32min(checkInterval)
	sendEvent(name: "checkInterval", value: 2 * 15 * 60 + 2 * 60, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID, offlinePingable: "1"])
}

def parse(String description) {
	log.trace description
	def cmd = zwave.parse(description)
	if (cmd) {
		return zwaveEvent(cmd)
	}
	log.debug "Could not parse message"
	return null
}

def zwaveEvent(physicalgraph.zwave.commands.switchbinaryv1.SwitchBinaryReport cmd) {
	def value = cmd.value ? "closed" : "open"

	return [createEventWithDebug([name: "contact", value: value, descriptionText: "$device.displayName valve is $value"]),
			createEventWithDebug([name: "valve", value: value, descriptionText: "$device.displayName valve is $value"])]
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
	return createEvent([:]) // Handles all Z-Wave commands we aren't interested in
}

def open() {
	delayBetween([
		zwave.switchBinaryV1.switchBinarySet(switchValue: 0x00).format(),
		zwave.switchBinaryV1.switchBinaryGet().format()
	], 500)
}

def close() {
	delayBetween([
		zwave.switchBinaryV1.switchBinarySet(switchValue: 0xFF).format(),
		zwave.switchBinaryV1.switchBinaryGet().format()
	], 500)
}

/**
 * PING is used by Device-Watch in attempt to reach the Device
 * */
def ping() {
	refresh()
}

def refresh() {
	zwave.switchBinaryV1.switchBinaryGet().format()
}

def createEventWithDebug(eventMap) {
	def event = createEvent(eventMap)
	log.debug "Event created with ${event?.name}:${event?.value} - ${event?.descriptionText}"
	return event
}
