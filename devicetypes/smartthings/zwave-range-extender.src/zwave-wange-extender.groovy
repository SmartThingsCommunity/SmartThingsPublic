/**
 *	Copyright 2018 SmartThings
 *
 *	Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *	in compliance with the License. You may obtain a copy of the License at:
 *
 *		http://www.apache.org/licenses/LICENSE-2.0
 *
 *	Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *	on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *	for the specific language governing permissions and limitations under the License.
 *
 */
metadata {
	definition (name: "Z-Wave Range Extender", namespace: "smartthings", author: "SmartThings", ocfDeviceType: "oic.d.networking") {
		capability "Health Check"

		fingerprint mfr: "0086", prod: "0104", model: "0075", deviceJoinName: "Aeotec Range Extender 6" //US
		fingerprint mfr: "0086", prod: "0204", model: "0075", deviceJoinName: "Aeotec Range Extender 6" //UK
		fingerprint mfr: "0086", prod: "0004", model: "0075", deviceJoinName: "Aeotec Range Extender 6" //EU
	}

	tiles(scale: 2) {
		multiAttributeTile(name: "status", type: "generic", width: 6, height: 4) {
			tileAttribute("device.status", key: "PRIMARY_CONTROL") {
				attributeState "online", label: 'online', icon: "st.motion.motion.active", backgroundColor: "#00A0DC"
			}
		}
		main "status"
		details(["status"])
	}
}

def installed() {
	runEvery5Minutes(ping)
	sendEvent(name: "checkInterval", value: 1930, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])
}

def parse(String description) {
	def cmd = zwave.parse(description)
	log.debug "Parse returned ${cmd}"
	cmd ? zwaveEvent(cmd) : null
}

def zwaveEvent(physicalgraph.zwave.commands.manufacturerspecificv2.ManufacturerSpecificReport cmd) {
	createEvent(name: "status", displayed: true, value: 'online', descriptionText: "$device.displayName is online")
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
	createEvent(descriptionText: "$device.displayName: $cmd", displayed: false)
}

def ping() {
	sendHubCommand(new physicalgraph.device.HubAction(zwave.manufacturerSpecificV2.manufacturerSpecificGet().format()))
}
