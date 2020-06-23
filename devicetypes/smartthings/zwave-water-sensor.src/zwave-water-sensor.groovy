/**
 *  Copyright 2018 SmartThings
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
 *	Generic Z-Wave Water Sensor
 *
 *	Author: SmartThings
 *	Date: 2013-03-05
 */

metadata {
	definition(name: "Z-Wave Water Sensor", namespace: "smartthings", author: "SmartThings", ocfDeviceType: "x.com.st.d.sensor.moisture", runLocally: true, minHubCoreVersion: '000.017.0012', executeCommandsLocally: false) {
		capability "Water Sensor"
		capability "Sensor"
		capability "Battery"
		capability "Health Check"
		capability "Configuration"

		fingerprint deviceId: '0xA102', inClusters: '0x30,0x9C,0x60,0x85,0x8E,0x72,0x70,0x86,0x80,0x84,0x7A', deviceJoinName: "Water Leak Sensor"
		fingerprint mfr: "021F", prod: "0003", model: "0085", deviceJoinName: "Dome Water Leak Sensor" //Dome Leak Sensor
		fingerprint mfr: "0258", prod: "0003", model: "1085", deviceJoinName: "NEO Coolcam Water Leak Sensor" //NAS-WS03ZE //NEO Coolcam Water Sensor
		fingerprint mfr: "0086", prod: "0102", model: "007A", deviceJoinName: "Aeotec Water Leak Sensor" //US //Aeotec Water Sensor 6
		fingerprint mfr: "0086", prod: "0002", model: "007A", deviceJoinName: "Aeotec Water Leak Sensor" //EU //Aeotec Water Sensor 6
		fingerprint mfr: "0086", prod: "0202", model: "007A", deviceJoinName: "Aeotec Water Leak Sensor" //AU //Aeotec Water Sensor 6
		fingerprint mfr: "000C", prod: "0201", model: "000A", deviceJoinName: "HomeSeer Water Leak Sensor" //HomeSeer LS100+ Water Sensor
	}

	simulator {
		status "dry": "command: 3003, payload: 00"
		status "wet": "command: 3003, payload: FF"
		status "dry notification": "command: 7105, payload: 00 00 00 FF 05 FE 00 00"
		status "wet notification": "command: 7105, payload: 00 FF 00 FF 05 02 00 00"
		status "wake up": "command: 8407, payload: "
	}

	tiles(scale: 2) {
		multiAttributeTile(name: "water", type: "generic", width: 6, height: 4) {
			tileAttribute("device.water", key: "PRIMARY_CONTROL") {
				attributeState("dry", label:'${name}', icon: "st.alarm.water.dry", backgroundColor: "#ffffff")
				attributeState("wet", label:'${name}', icon: "st.alarm.water.wet", backgroundColor: "#00A0DC")
			}
		}
		valueTile("battery", "device.battery", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "battery", label: '${currentValue}% battery', unit: ""
		}

		main "water"
		details(["water", "battery"])
	}
}

def initialize() {
	if (isAeotec() || isNeoCoolcam() || isDome()) {
		// 8 hour (+ 2 minutes) ping for Aeotec, NEO Coolcam, Dome
		sendEvent(name: "checkInterval", value: 8 * 60 * 60 + 2 * 60, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])
	} else {
		// 12 hours (+ 2 minutes) for other devices
		sendEvent(name: "checkInterval", value: 12 * 60 * 60 + 2 * 60, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])
	}
}

def installed() {
	initialize()
	//water alarm
	def cmds = [ encap(zwave.notificationV3.notificationGet(notificationType: 0x05)),
				 encap(zwave.batteryV1.batteryGet())]
	response(cmds)
}

def updated() {
	initialize()
}

def configure() {
	if (isAeotec()) {
		def commands = []
		commands << encap(zwave.associationV2.associationSet(groupingIdentifier:3, nodeId: [zwaveHubNodeId]))
		commands << encap(zwave.associationV2.associationSet(groupingIdentifier:4, nodeId: [zwaveHubNodeId]))
		// send basic sets to devices in groups 3 and 4 when water is detected
		commands << encap(zwave.configurationV1.configurationSet(parameterNumber: 0x58, scaledConfigurationValue: 1, size: 1))
		commands << encap(zwave.configurationV1.configurationSet(parameterNumber: 0x59, scaledConfigurationValue: 1, size: 1))
		// Tell sensor to send us battery information instead of USB power information
		commands << encap(zwave.configurationV1.configurationSet(parameterNumber: 0x5E, scaledConfigurationValue: 1, size: 1))
		response(delayBetween(commands, 1000) + ["delay 20000", encap(zwave.wakeUpV1.wakeUpNoMoreInformation())])
	} else if (isNeoCoolcam() || isDome()) {
		// wakeUpInterval set to 4 h for NEO Coolcam, Dome
		zwave.wakeUpV1.wakeUpIntervalSet(seconds: 4 * 3600, nodeid: zwaveHubNodeId).format()
	}
}

private getCommandClassVersions() {
	[0x20: 1, 0x30: 1, 0x31: 5, 0x80: 1, 0x84: 1, 0x71: 3, 0x9C: 1]
}

def parse(String description) {
	def result = null
	if (description.startsWith("Err")) {
		result = createEvent(descriptionText: description)
	} else {
		def cmd = zwave.parse(description, commandClassVersions)
		if (cmd) {
			result = zwaveEvent(cmd)
		} else {
			result = createEvent(value: description, descriptionText: description, isStateChange: false)
		}
	}
	log.debug "Parsed '$description' to $result"
	return result
}

def sensorValueEvent(value) {
	def eventValue = value ? "wet" : "dry"
	createEvent(name: "water", value: eventValue, descriptionText: "$device.displayName is $eventValue")
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd) {
	sensorValueEvent(cmd.value)
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicSet cmd) {
	sensorValueEvent(cmd.value)
}

def zwaveEvent(physicalgraph.zwave.commands.switchbinaryv1.SwitchBinaryReport cmd) {
	sensorValueEvent(cmd.value)
}

def zwaveEvent(physicalgraph.zwave.commands.sensorbinaryv1.SensorBinaryReport cmd) {
	sensorValueEvent(cmd.sensorValue)
}

def zwaveEvent(physicalgraph.zwave.commands.sensoralarmv1.SensorAlarmReport cmd) {
	sensorValueEvent(cmd.sensorState)
}

def zwaveEvent(physicalgraph.zwave.commands.notificationv3.NotificationReport cmd) {
	def result = []
	if (cmd.notificationType == 0x05) {
		switch (cmd.event) {
			case 0x00:
				if (cmd.eventParametersLength && cmd.eventParameter.size() && eventParameter[0] > 0x02) {
					result << createEvent(descriptionText: "Water alarm cleared", isStateChange: true)
				} else {
					result << createEvent(name: "water", value: "dry")
				}
				break
			case 0xFE:
				result << createEvent(name: "water", value: "dry")
				break
			case 0x01:
			case 0x02:
				result << createEvent(name: "water", value: "wet")
				break
			case 0x03:
			case 0x04:
				result << createEvent(descriptionText: "Water level dropped", isStateChange: true)
				break
			case 0x05:
				result << createEvent(descriptionText: "Replace water filter", isStateChange: true)
				break
			case 0x06:
				def level = ["alarm", "alarm", "below low threshold", "above high threshold", "max"][cmd.eventParameter[0]]
				result << createEvent(descriptionText: "Water flow $level", isStateChange: true)
				break
			case 0x07:
				def level = ["alarm", "alarm", "below low threshold", "above high threshold", "max"][cmd.eventParameter[0]]
				result << createEvent(descriptionText: "Water pressure $level", isStateChange: true)
				break
		}
	} else if (cmd.notificationType == 0x04) {
		if (cmd.event <= 0x02) {
			result << createEvent(descriptionText: "$device.displayName detected overheat", isStateChange: true)
		} else if (cmd.event <= 0x04) {
			result << createEvent(descriptionText: "$device.displayName detected rapid temperature rise", isStateChange: true)
		} else {
			result << createEvent(descriptionText: "$device.displayName detected low temperature", isStateChange: true)
		}
	} else if (cmd.notificationType == 0x07) {
		if (cmd.event == 0x03) {
			result << createEvent(descriptionText: "$device.displayName covering was removed", isStateChange: true)
			result << response([
				encap(zwave.wakeUpV1.wakeUpIntervalSet(seconds: 4 * 3600, nodeid: zwaveHubNodeId)),
				encap(zwave.batteryV1.batteryGet())])
		}
	} else if (cmd.notificationType) {
		def text = "Notification $cmd.notificationType: event ${([cmd.event] + cmd.eventParameter).join(", ")}"
		result << createEvent(name: "notification$cmd.notificationType", value: "$cmd.event", descriptionText: text, displayed: false)
	} else {
		def value = cmd.v1AlarmLevel == 255 ? "active" : cmd.v1AlarmLevel ?: "inactive"
		result << createEvent(name: "alarm $cmd.v1AlarmType", value: value, displayed: false)
	}
	result
}

def zwaveEvent(physicalgraph.zwave.commands.wakeupv1.WakeUpNotification cmd) {
	def result = [createEvent(descriptionText: "${device.displayName} woke up", isStateChange: false)]
	if (!state.lastbat || (new Date().time) - state.lastbat > 53 * 60 * 60 * 1000) {
		result << response(encap(zwave.batteryV1.batteryGet()))
	} else {
		result << response(encap(zwave.wakeUpV1.wakeUpNoMoreInformation()))
	}
	result
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
	state.lastbat = new Date().time
	[createEvent(map), response(encap(zwave.wakeUpV1.wakeUpNoMoreInformation()))]
}

def zwaveEvent(physicalgraph.zwave.commands.sensormultilevelv5.SensorMultilevelReport cmd) {
	def map = [displayed: true, value: cmd.scaledSensorValue.toString()]
	switch (cmd.sensorType) {
		case 1:
			map.name = "temperature"
			map.unit = cmd.scale == 1 ? "F" : "C"
			break;
		case 5:
			map.name = "humidity"
			map.value = cmd.scaledSensorValue.toInteger().toString()
			map.unit = cmd.scale == 0 ? "%" : ""
			break;
	}
	createEvent(map)
}

def zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityMessageEncapsulation cmd) {
	def encapsulatedCommand = cmd.encapsulatedCommand(commandClassVersions)
	// log.debug "encapsulated: $encapsulatedCommand"
	if (encapsulatedCommand) {
		state.sec = 1
		zwaveEvent(encapsulatedCommand)
	}
}

def zwaveEvent(physicalgraph.zwave.commands.crc16encapv1.Crc16Encap cmd) {
	// def encapsulatedCommand = cmd.encapsulatedCommand(commandClassVersions)
	def version = commandClassVersions[cmd.commandClass as Integer]
	def ccObj = version ? zwave.commandClass(cmd.commandClass, version) : zwave.commandClass(cmd.commandClass)
	def encapsulatedCommand = ccObj?.command(cmd.command)?.parse(cmd.data)
	if (encapsulatedCommand) {
		return zwaveEvent(encapsulatedCommand)
	}
}

def zwaveEvent(physicalgraph.zwave.commands.multichannelv3.MultiChannelCmdEncap cmd) {
	def result = null
	if (cmd.commandClass == 0x6C && cmd.parameter.size >= 4) { // Supervision encapsulated Message
		// Supervision header is 4 bytes long, two bytes dropped here are the latter two bytes of the supervision header
		cmd.parameter = cmd.parameter.drop(2)
		// Updated Command Class/Command now with the remaining bytes
		cmd.commandClass = cmd.parameter[0]
		cmd.command = cmd.parameter[1]
		cmd.parameter = cmd.parameter.drop(2)
	}
	def encapsulatedCommand = cmd.encapsulatedCommand(commandClassVersions)
	log.debug "Command from endpoint ${cmd.sourceEndPoint}: ${encapsulatedCommand}"
	if (encapsulatedCommand) {
		result = zwaveEvent(encapsulatedCommand)
	}
	result
}

def zwaveEvent(physicalgraph.zwave.commands.multicmdv1.MultiCmdEncap cmd) {
	log.debug "MultiCmd with $numberOfCommands inner commands"
	cmd.encapsulatedCommands(commandClassVersions).collect { encapsulatedCommand ->
		zwaveEvent(encapsulatedCommand)
	}.flatten()
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
	createEvent(descriptionText: "$device.displayName: $cmd", displayed: false)
}

def zwaveEvent(physicalgraph.zwave.commands.manufacturerspecificv2.ManufacturerSpecificReport cmd) {
	def result = []

	def msr = String.format("%04X-%04X-%04X", cmd.manufacturerId, cmd.productTypeId, cmd.productId)
	log.debug "msr: $msr"
	updateDataValue("MSR", msr)

	result << createEvent(descriptionText: "$device.displayName MSR: $msr", isStateChange: false)
	result
}

private encap(physicalgraph.zwave.Command cmd) {
	if (zwaveInfo.zw.contains("s") || state.sec == 1) {
		secEncap(cmd)
	} else if (zwaveInfo?.cc?.contains("56")){
		crcEncap(cmd)
	} else {
		cmd.format()
	}
}

private secEncap(physicalgraph.zwave.Command cmd) {
	zwave.securityV1.securityMessageEncapsulation().encapsulate(cmd).format()
}

private crcEncap(physicalgraph.zwave.Command cmd) {
	zwave.crc16EncapV1.crc16Encap().encapsulate(cmd).format()
}

private isDome() {
	zwaveInfo.mfr == "021F" && zwaveInfo.model == "0085"
}

private isNeoCoolcam() {
	zwaveInfo.mfr == "0258" && zwaveInfo.model == "1085"
}

private isAeotec() {
	zwaveInfo.mfr == "0086" && zwaveInfo.model == "007A"
}