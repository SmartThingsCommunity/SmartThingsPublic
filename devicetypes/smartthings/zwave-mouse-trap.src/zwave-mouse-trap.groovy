/**
 *  Copyright 2018 SmartThings
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
	definition(name: "Z-Wave Mouse Trap", namespace: "smartthings", author: "SmartThings", ocfDeviceType: "x.com.st.d.sensor.contact", runLocally: false, executeCommandsLocally: false) {
		capability "Sensor"
		capability "Battery"
		capability "Configuration"
		capability "Health Check"

		attribute "pestControl", "enum", ["cleared", "exterminated", "armed", "disarmed"]

		fingerprint mfr: "021F", prod: "0003", model: "0104", deviceJoinName: "Dome Mouser"

	}

	tiles(scale: 2) {
		multiAttributeTile(name: "pestControl", type: "generic", width: 6, height: 4) {
			tileAttribute("device.pestControl", key: "PRIMARY_CONTROL") {
				attributeState("cleared", label: '${name}', icon: "st.contact.contact.open", backgroundColor: "#e86d13")
				attributeState("exterminated", label: '${name}', icon: "st.contact.contact.open", backgroundColor: "#e86d13")
				attributeState("armed", label: '${name}', icon: "st.contact.contact.open", backgroundColor: "#e86d13")
				attributeState("disarmed", label: '${name}', icon: "st.contact.contact.closed", backgroundColor: "#00A0DC")
			}
		}
		valueTile("battery", "device.battery", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "battery", label: '${currentValue}% battery', unit: ""
		}

		main "pestControl"
		details(["pestControl", "battery"])
	}
}

private getCommandClassVersions() {
	[0x20: 1, 0x25: 1, 0x30: 1, 0x31: 5, 0x80: 1, 0x84: 1, 0x71: 3, 0x9C: 1]
}

def parse(String description) {
	def result = []
	def cmd = zwave.parse(description, commandClassVersions)
	if (cmd) {
		result = zwaveEvent(cmd)
	}
	log.debug "parsed '$description' to $result"
	return result
}

def installed() {
	// Device-Watch simply pings if no device events received for 8h 6min(checkInterval)
	sendEvent(name: "checkInterval", value: 8 * 60 * 60 + 6 * 60, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])
}

def updated() {
	// Device-Watch simply pings if no device events received for 8h 6min(checkInterval)
	sendEvent(name: "checkInterval", value: 8 * 60 * 60 + 6 * 60, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])
}

def configure() {
	response([
		zwave.notificationV3.notificationGet(notificationType: 0x13).format(),
		zwave.manufacturerSpecificV2.manufacturerSpecificGet().format()
	], 1000)
}

def zwaveEvent(physicalgraph.zwave.commands.switchbinaryv1.SwitchBinaryReport cmd) {
	//ignore, to prevent override of NotificationReport
	[]
}

def zwaveEvent(physicalgraph.zwave.commands.sensorbinaryv1.SensorBinaryReport cmd) {
	//ignore, to prevent override of NotificationReport
	[]
}

def zwaveEvent(physicalgraph.zwave.commands.notificationv3.NotificationReport cmd) {
	def result = []
	def value
	def description
	if (cmd.notificationType == 0x07) {
		switch (cmd.event) {
			case 0x00:
				value = "clear"
				description = "Trap cleared"
				break
			case 0x07:
				value = "exterminated"
				description = "Pest exterminated"
				break
			default:
				break
		}
		result = createEvent(name: "pestControl", value: value, descriptionText: description)
	} else if (cmd.notificationType == 0x13) {
		switch (cmd.event) {
			case 0x00:
				value = "clear"
				description = "Trap cleared"
				break
			case 0x02:
				value = "armed"
				description = "Trap armed"
				break
			case 0x04:
				value = "disarmed"
				description = "Trap disarmed"
				break
			case 0x08:
				value = "exterminated"
				description = "Pest exterminated"
				break
			default:
				break
		}
		result = createEvent(name: "pestControl", value: value, descriptionText: description)
	}
	result
}

def zwaveEvent(physicalgraph.zwave.commands.wakeupv1.WakeUpNotification cmd) {
	def event = createEvent(descriptionText: "${device.displayName} woke up", isStateChange: false)
	def cmds = []
	if (!state.MSR) {
		cmds << zwave.manufacturerSpecificV2.manufacturerSpecificGet()
		cmds << "delay 1200"
	}

	if (device.currentValue("pestControl") == null) { // Incase our initial request didn't make it
		zwave.notificationV3.notificationGet(notificationType: 0x13).format().format()
	}

	if (!state.lastbat || now() - state.lastbat > 53 * 60 * 60 * 1000) {
		cmds << zwave.batteryV1.batteryGet().format()
	} else {
		// If we check the battery state we will send NoMoreInfo in the handler for BatteryReport so that we definitely get the report
		cmds << zwave.wakeUpV1.wakeUpNoMoreInformation().format()
	}

	[event, response(cmds)]
}

def zwaveEvent(physicalgraph.zwave.commands.batteryv1.BatteryReport cmd) {
	def map = [name: "battery", unit: "%"]
	if (cmd.batteryLevel == 0xFF) {
		map.value = 1
		map.descriptionText = "${device.displayName} has a low battery"
		map.isStateChange = true
	} else {
		map.value = cmd.batteryLevel
	}
	state.lastbat = now()
	[createEvent(map), response(zwave.wakeUpV1.wakeUpNoMoreInformation())]
}

def zwaveEvent(physicalgraph.zwave.commands.manufacturerspecificv2.ManufacturerSpecificReport cmd) {
	def result = []

	def msr = String.format("%04X-%04X-%04X", cmd.manufacturerId, cmd.productTypeId, cmd.productId)
	log.debug "msr: $msr"
	updateDataValue("MSR", msr)

	result << createEvent(descriptionText: "$device.displayName MSR: $msr", isStateChange: false)

	if (!device.currentState("battery")) {
		result << response(zwave.batteryV1.batteryGet())
	}
	result
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
	createEvent(descriptionText: "$device.displayName: $cmd", displayed: false)
}
