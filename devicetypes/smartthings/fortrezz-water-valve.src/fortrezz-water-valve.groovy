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
	definition (name: "Fortrezz Water Valve", namespace: "smartthings", author: "SmartThings") {
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
	tiles {
		standardTile("contact", "device.contact", width: 2, height: 2, canChangeIcon: true) {
			state "open", label: '${name}', action: "valve.close", icon: "st.valves.water.open", backgroundColor: "#00A0DC", nextState:"closing"
			state "closed", label: '${name}', action: "valve.open", icon: "st.valves.water.closed", backgroundColor: "#ffffff", nextState:"opening"
			state "opening", label: '${name}', action: "valve.close", icon: "st.valves.water.open", backgroundColor: "#00A0DC"
			state "closing", label: '${name}', action: "valve.open", icon: "st.valves.water.closed", backgroundColor: "#ffffff"
		}
		standardTile("refresh", "device.switch", inactiveLabel: false, decoration: "flat") {
			state "default", label:'', action:"refresh.refresh", icon:"st.secondary.refresh"
		}

		main "contact"
		details(["contact","refresh"])
	}
}

def updated(){
// Device-Watch simply pings if no device events received for 32min(checkInterval)
	sendEvent(name: "checkInterval", value: 2 * 15 * 60 + 2 * 60, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])
}

def parse(String description) {
	log.trace description
	def result = null
	def cmd = zwave.parse(description)
	if (cmd) {
		result = createEvent(zwaveEvent(cmd))
	}
	log.debug "Parse returned ${result?.descriptionText}"
	return result
}

def zwaveEvent(physicalgraph.zwave.commands.switchbinaryv1.SwitchBinaryReport cmd) {
	def value = cmd.value ? "closed" : "open"
	[name: "contact", value: value, descriptionText: "$device.displayName valve is $value"]
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
	[:] // Handles all Z-Wave commands we aren't interested in
}

def open() {
	zwave.switchBinaryV1.switchBinarySet(switchValue: 0x00).format()
}

def close() {
	zwave.switchBinaryV1.switchBinarySet(switchValue: 0xFF).format()
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
