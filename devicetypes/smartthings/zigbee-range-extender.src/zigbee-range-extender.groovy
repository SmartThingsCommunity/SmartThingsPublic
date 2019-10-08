/**
 *	Copyright 2019 SmartThings
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
	definition (name: "Zigbee Range Extender", namespace: "smartthings", author: "SmartThings", ocfDeviceType: "oic.d.networking", mnmn: "SmartThings", vid: "SmartThings-smartthings-Z-Wave_Range_Extender") {
		capability "Health Check"

		fingerprint profileId: "0104", inClusters: "0000, 0003, 0009, 0B05, 1000, FC7C", outClusters: "0019, 0020, 1000",  manufacturer: "IKEA of Sweden",  model: "TRADFRI signal repeater", deviceJoinName: "TRÅDFRI Signal Repeater"
		fingerprint profileId: "0104", inClusters: "0000, 0003", outClusters: "0019",  manufacturer: "Smartenit, Inc",  model: "ZB3RE", deviceJoinName: "Smartenit Range Extender"
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
	sendEvent(name: "checkInterval", value: 1930, displayed: false, data: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID])
}

def parse(String description) {
	def map = zigbee.getEvent(description)
	def result
	if(!map) {
		result = parseAttrMessage(description)
	} else {
		log.warn "Unexpected event: ${map}"
	}
	log.debug "Description ${description} parsed to ${result}"
	return result
}

def parseAttrMessage(description) {
	def descMap = zigbee.parseDescriptionAsMap(description)
	log.debug "Desc Map: $descMap"
	createEvent(name: "status", displayed: true, value: 'online', descriptionText: "$device.displayName is online")
}

def ping() {
	sendHubCommand(zigbee.readAttribute(zigbee.BASIC_CLUSTER, ZCL_VERSION_ATTRIBUTE))
}

private getZCL_VERSION_ATTRIBUTE() { 0x0000 }