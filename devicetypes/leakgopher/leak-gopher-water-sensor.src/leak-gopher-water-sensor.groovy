/**
 *  Copyright 2020 Leak Intelligence LLC
 *
 *	Leak Gopher Water Sensor
 *
 *	Author: Leak Intelligence LLC
 *	Date: 2020-03-24
 */

metadata {
	definition(name: "Leak Gopher Water Sensor", namespace: "LeakGopher", author: "Leak Intelligence LLC", ocfDeviceType: "x.com.st.d.sensor.moisture", runLocally: true, minHubCoreVersion: '000.017.0012', executeCommandsLocally: false) {
		capability "Water Sensor"
		capability "Sensor"
		capability "Battery"
		capability "Health Check"
		capability "Configuration"

		fingerprint deviceId: '0xA102', inClusters: '0x30,0x9C,0x60,0x85,0x8E,0x72,0x70,0x86,0x80,0x84,0x7A'
		fingerprint mfr: "0173", prod: "4C47", model: "4C44", deviceJoinName: "Leak Gopher Leak Sensor"
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
		// 12 hours 
		sendEvent(name: "checkInterval", value: (2 * 12 + 2) * 60 * 60, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])
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
	} else if (zwaveInfo.cc.contains("56")){
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