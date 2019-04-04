/**
 *  Copyright 2019 SmartThings
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
	definition (name: "Glentronics Connection Module", namespace: "smartthings", author: "SmartThings") {
		capability "Sensor"
		capability "Water Sensor"
		capability "Battery"
		capability "Power Source"
		capability "Health Check"

		fingerprint mfr:"0084", prod:"0093", model:"0114", deviceJoinName: "Glentronics Connection Module"
	}

	tiles (scale: 2){
		multiAttributeTile(name: "water", type: "generic", width: 6, height: 4) {
			tileAttribute("device.water", key: "PRIMARY_CONTROL") {
				attributeState("dry", icon: "st.alarm.water.dry", backgroundColor: "#ffffff")
				attributeState("wet", icon: "st.alarm.water.wet", backgroundColor: "#00A0DC")
			}
		}
		valueTile("battery", "device.battery", inactiveLabel: true, decoration: "flat", width: 2, height: 2) {
			state "battery", label: 'Backup battery: ${currentValue}%', unit: ""
		}
		valueTile("powerSource", "device.powerSource", width: 2, height: 1, inactiveLabel: true, decoration: "flat") {
			state "powerSource", label: 'Power Source: ${currentValue}', backgroundColor: "#ffffff"
		}
		main "water"
		details(["water", "battery", "powerSource"])
	}
}

def parse(String description) {
	def result
	if (description.startsWith("Err")) {
		result = createEvent(descriptionText:description, displayed:true)
	} else {
		def cmd = zwave.parse(description)
		if (cmd) {
			result = zwaveEvent(cmd)
		}
	}
	log.debug "Parse returned: ${result.inspect()}"
	return result
}

def installed() {
	//There's no possibility for initial poll, so to avoid empty fields, assuming everything is functioning correctly
	sendEvent(name: "battery", value: 100, unit: "%")
	sendEvent(name: "water", value: "dry")
	sendEvent(name: "powerSource", value: "mains")
	sendEvent(name: "checkInterval", value: 2 * 60 * 60 + 2 * 60, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID, offlinePingable: "1"])
}

def ping() {
	response(zwave.versionV1.versionGet().format())
}

def getPowerEvent(event) {
	if (event == 0x02) {
		createEvent(name: "powerSource", value: "battery", descriptionText: "Pump is powered with backup battery")
	} else if (event == 0x03) {
		createEvent(name: "powerSource", value: "mains", descriptionText: "Pump is powered with AC mains")
	} else if (event == 0x0B) {
		createEvent(name: "battery", value: 1, unit: "%", descriptionText: "Backup battery critically low")
	} else if (event == 0x0D) {
		createEvent(name: "battery", value: 100, unit: "%", descriptionText: "Backup battery is fully charged")
	}
}

def getManufacturerSpecificEvent(cmd) {
	if (cmd.event == 3) {
		if (cmd.eventParameter[0] == 0) {
			createEvent(name: "water", value: "dry", descriptionText: "Water alarm has been cleared")
		} else if (cmd.eventParameter[0] == 2) {
			createEvent(name: "water", value: "wet", descriptionText: "High water alarm")
		}
	}
}

def zwaveEvent(physicalgraph.zwave.commands.notificationv3.NotificationReport cmd) {
	log.debug "NotificationReport: ${cmd}"
	if (cmd.notificationType == 8) {
		getPowerEvent(cmd.event)
	} else if (cmd.notificationType == 9) {
		getManufacturerSpecificEvent(cmd)
	}
}

def zwaveEvent(physicalgraph.zwave.commands.versionv1.VersionReport cmd) {
	createEvent(descriptionText: "Device has responded to ping()")
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
	log.warn "Unhandled command: ${cmd}"
	createEvent(descriptionText: "Unhandled event came in")
}