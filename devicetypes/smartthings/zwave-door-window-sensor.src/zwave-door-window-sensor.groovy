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
 *  Z-Wave Door/Window Sensor
 *
 *  Author: SmartThings
 *  Date: 2013-11-3
 */

metadata {
	definition(name: "Z-Wave Door/Window Sensor", namespace: "smartthings", author: "SmartThings", ocfDeviceType: "x.com.st.d.sensor.contact", runLocally: true, minHubCoreVersion: '000.017.0012', executeCommandsLocally: false, genericHandler: "Z-Wave") {
		capability "Contact Sensor"
		capability "Sensor"
		capability "Battery"
		capability "Configuration"
		capability "Health Check"

		fingerprint deviceId: "0x2001", inClusters: "0x30,0x80,0x84,0x85,0x86,0x72"
		fingerprint deviceId: "0x07", inClusters: "0x30"
		fingerprint deviceId: "0x0701", inClusters: "0x5E,0x98"
		fingerprint deviceId: "0x0701", inClusters: "0x5E,0x86,0x72,0x98", outClusters: "0x5A,0x82"
		fingerprint deviceId: "0x0701", inClusters: "0x5E,0x80,0x71,0x85,0x70,0x72,0x86,0x30,0x31,0x84,0x59,0x73,0x5A,0x8F,0x98,0x7A", outClusters: "0x20"
		// Philio multi+
		fingerprint mfr: "0086", prod: "0002", model: "001D", deviceJoinName: "Aeotec Door/Window Sensor (Gen 5)"
		fingerprint mfr: "0086", prod: "0102", model: "0070", deviceJoinName: "Aeotec Door/Window Sensor 6" //US
		fingerprint mfr: "0086", prod: "0002", model: "0070", deviceJoinName: "Aeotec Door/Window Sensor 6" //EU
		fingerprint mfr: "0086", prod: "0102", model: "0059", deviceJoinName: "Aeotec Recessed Door Sensor"
		fingerprint mfr: "014A", prod: "0001", model: "0002", deviceJoinName: "Ecolink Door/Window Sensor"
		fingerprint mfr: "014A", prod: "0001", model: "0003", deviceJoinName: "Ecolink Tilt Sensor"
		fingerprint mfr: "014A", prod: "0004", model: "0003", deviceJoinName: "Ecolink Tilt Sensor"
		fingerprint mfr: "011A", prod: "0601", model: "0903", deviceJoinName: "Enerwave Magnetic Door/Window Sensor"
		fingerprint mfr: "014F", prod: "2001", model: "0102", deviceJoinName: "Nortek GoControl Door/Window Sensor"
		fingerprint mfr: "0063", prod: "4953", model: "3031", deviceJoinName: "Jasco Hinge Pin Door Sensor"
		fingerprint mfr: "019A", prod: "0003", model: "0003", deviceJoinName: "Sensative Strips"
		fingerprint mfr: "0258", prod: "0003", model: "0082", deviceJoinName: "NEO Coolcam Door/Window Sensor"
		fingerprint mfr: "0258", prod: "0003", model: "1082", deviceJoinName: "NEO Coolcam Door/Window Sensor" // NAS-DS01ZE
		fingerprint mfr: "021F", prod: "0003", model: "0101", deviceJoinName: "Dome Door/Window Sensor"
		//zw:S type:0701 mfr:014A prod:0004 model:0002 ver:10.01 zwv:4.05 lib:06 cc:5E,86,72,73,80,71,85,59,84,30,70 ccOut:20 role:06 ff:8C07 ui:8C00
		fingerprint mfr: "014A", prod: "0004", model: "0002", deviceJoinName: "Ecolink Door/Window Sensor"
		//zw:Ss type:0701 mfr:0086 prod:0002 model:0059 ver:1.14 zwv:3.92 lib:03 cc:5E,86,72,98,5A ccOut:82 sec:30,80,84,70,85,59,71,7A,73 role:06 ff:8C00 ui:8C00
		fingerprint mfr: "0086", prod: "0002", model: "0059", deviceJoinName: "Aeon Recessed Door Sensor"
		//zw:S type:0701 mfr:0214 prod:0002 model:0001 ver:6.38 zwv:4.38 lib:06 cc:5E,30,84,80,86,72,71,70,85,59,73,5A role:06 ff:8C06 ui:8C06
		fingerprint mfr: "0214", prod: "0002", model: "0001", deviceJoinName: "BeSense Door/Window Detector"
		fingerprint mfr: "0086", prod: "0002", model: "0078", deviceJoinName: "Aeotec Door/Window Sensor Gen5" //EU
	}

	// simulator metadata
	simulator {
		// status messages
		status "open": "command: 2001, payload: FF"
		status "closed": "command: 2001, payload: 00"
		status "wake up": "command: 8407, payload: "
	}

	// UI tile definitions
	tiles(scale: 2) {
		multiAttributeTile(name: "contact", type: "generic", width: 6, height: 4) {
			tileAttribute("device.contact", key: "PRIMARY_CONTROL") {
				attributeState("open", label: '${name}', icon: "st.contact.contact.open", backgroundColor: "#e86d13")
				attributeState("closed", label: '${name}', icon: "st.contact.contact.closed", backgroundColor: "#00A0DC")
			}
		}
		valueTile("battery", "device.battery", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "battery", label: '${currentValue}% battery', unit: ""
		}

		main "contact"
		details(["contact", "battery"])
	}
}

private getCommandClassVersions() {
	[0x20: 1, 0x25: 1, 0x30: 1, 0x31: 5, 0x80: 1, 0x84: 1, 0x71: 3, 0x9C: 1]
}

def parse(String description) {
	def result = null
	if (description.startsWith("Err 106")) {
		if ((zwaveInfo.zw == null && state.sec != 0) || zwaveInfo?.zw?.endsWith("s")) {
			log.debug description
		} else {
			result = createEvent(
				descriptionText: "This sensor failed to complete the network security key exchange. If you are unable to control it via SmartThings, you must remove it from your network and add it again.",
				eventType: "ALERT",
				name: "secureInclusion",
				value: "failed",
				isStateChange: true,
			)
		}
	} else if (description != "updated") {
		def cmd = zwave.parse(description, commandClassVersions)
		if (cmd) {
			result = zwaveEvent(cmd)
		}
	}
	log.debug "parsed '$description' to $result"
	return result
}

def installed() {
	// Device-Watch simply pings if no device events received for 482min(checkInterval)
	sendEvent(name: "checkInterval", value: 2 * 4 * 60 * 60 + 2 * 60, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])
	// this is the nuclear option because the device often goes to sleep before we can poll it
	sendEvent(name: "contact", value: "open", descriptionText: "$device.displayName is open")
	sendEvent(name: "battery", unit: "%", value: 100)
	response(initialPoll())
}

def updated() {
	// Device-Watch simply pings if no device events received for 482min(checkInterval)
	sendEvent(name: "checkInterval", value: 2 * 4 * 60 * 60 + 2 * 60, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])
}

def configure() {
	// currently supported devices do not require initial configuration
}

def sensorValueEvent(value) {
	if (value) {
		createEvent(name: "contact", value: "open", descriptionText: "$device.displayName is open")
	} else {
		createEvent(name: "contact", value: "closed", descriptionText: "$device.displayName is closed")
	}
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
	if (cmd.notificationType == 0x06 && cmd.event == 0x16) {
		result << sensorValueEvent(1)
	} else if (cmd.notificationType == 0x06 && cmd.event == 0x17) {
		result << sensorValueEvent(0)
	} else if (cmd.notificationType == 0x07) {
		if (cmd.v1AlarmType == 0x07) {  // special case for nonstandard messages from Monoprice door/window sensors
			result << sensorValueEvent(cmd.v1AlarmLevel)
		} else if (cmd.event == 0x01 || cmd.event == 0x02) {
			result << sensorValueEvent(1)
		} else if (cmd.event == 0x03) {
			result << createEvent(descriptionText: "$device.displayName covering was removed", isStateChange: true)
			if (!state.MSR) result << response(command(zwave.manufacturerSpecificV2.manufacturerSpecificGet()))
		} else if (cmd.event == 0x05 || cmd.event == 0x06) {
			result << createEvent(descriptionText: "$device.displayName detected glass breakage", isStateChange: true)
		} else if (cmd.event == 0x07) {
			if (!state.MSR) result << response(command(zwave.manufacturerSpecificV2.manufacturerSpecificGet()))
			result << createEvent(name: "motion", value: "active", descriptionText: "$device.displayName detected motion")
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
	def event = createEvent(descriptionText: "${device.displayName} woke up", isStateChange: false)
	def cmds = []
	if (!state.MSR) {
		cmds << zwave.manufacturerSpecificV2.manufacturerSpecificGet()
	}

	if (device.currentValue("contact") == null) {
		// In case our initial request didn't make it, initial state check no. 3
		cmds << zwave.sensorBinaryV2.sensorBinaryGet(sensorType: zwave.sensorBinaryV2.SENSOR_TYPE_DOOR_WINDOW)
	}

	if (!state.lastbat || now() - state.lastbat > 53 * 60 * 60 * 1000) {
		cmds << zwave.batteryV1.batteryGet()
	}

	def request = []
	if (cmds.size() > 0) {
		request = commands(cmds, 1000)
		request << "delay 20000"
	}
	request << zwave.wakeUpV1.wakeUpNoMoreInformation().format()

	[event, response(request)]
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
	[createEvent(map)]
}

def zwaveEvent(physicalgraph.zwave.commands.manufacturerspecificv2.ManufacturerSpecificReport cmd) {
	def result = []

	def msr = String.format("%04X-%04X-%04X", cmd.manufacturerId, cmd.productTypeId, cmd.productId)
	log.debug "msr: $msr"
	updateDataValue("MSR", msr)

	result << createEvent(descriptionText: "$device.displayName MSR: $msr", isStateChange: false)

	// change DTH if required based on MSR
	if (!retypeBasedOnMSR()) {
		if (msr == "011A-0601-0901") {
			// Enerwave motion doesn't always get the associationSet that the hub sends on join
			result << response(zwave.associationV1.associationSet(groupingIdentifier: 1, nodeId: zwaveHubNodeId))
		}
	} else {
		// if this is door/window sensor check initial contact state no.2
		if (!device.currentState("contact")) {
			result << response(command(zwave.sensorBinaryV2.sensorBinaryGet(sensorType: zwave.sensorBinaryV2.SENSOR_TYPE_DOOR_WINDOW)))
		}
	}

	// every battery device can miss initial battery check. check initial battery state no.2
	if (!device.currentState("battery")) {
		result << response(command(zwave.batteryV1.batteryGet()))
	}

	result
}

def zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityMessageEncapsulation cmd) {
	def encapsulatedCommand = cmd.encapsulatedCommand(commandClassVersions)
	if (encapsulatedCommand) {
		zwaveEvent(encapsulatedCommand)
	}
}

def zwaveEvent(physicalgraph.zwave.commands.crc16encapv1.Crc16Encap cmd) {
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

def initialPoll() {
	def request = []
	if (isEnerwave()) { // Enerwave motion doesn't always get the associationSet that the hub sends on join
		request << zwave.associationV1.associationSet(groupingIdentifier: 1, nodeId: zwaveHubNodeId)
	}

	// check initial battery and contact state no.1
	request << zwave.batteryV1.batteryGet()
	request << zwave.sensorBinaryV2.sensorBinaryGet(sensorType: zwave.sensorBinaryV2.SENSOR_TYPE_DOOR_WINDOW)
	request << zwave.manufacturerSpecificV2.manufacturerSpecificGet()
	commands(request, 500) + ["delay 6000", command(zwave.wakeUpV1.wakeUpNoMoreInformation())]
}

private command(physicalgraph.zwave.Command cmd) {
	if ((zwaveInfo.zw == null && state.sec != 0) || zwaveInfo?.zw?.endsWith("s")) {
		zwave.securityV1.securityMessageEncapsulation().encapsulate(cmd).format()
	} else {
		cmd.format()
	}
}

private commands(commands, delay = 200) {
	delayBetween(commands.collect { command(it) }, delay)
}

def retypeBasedOnMSR() {
	def dthChanged = true
	switch (state.MSR) {
		case "0086-0002-002D":
			log.debug "Changing device type to Z-Wave Water Sensor"
			setDeviceType("Z-Wave Water Sensor")
			break
		case "011F-0001-0001":  // Schlage motion
		case "014A-0001-0001":  // Ecolink motion
		case "014A-0004-0001":  // Ecolink motion +
		case "0060-0001-0002":  // Everspring SP814
		case "0060-0001-0003":  // Everspring HSP02
		case "011A-0601-0901":  // Enerwave ZWN-BPC
			log.debug "Changing device type to Z-Wave Motion Sensor"
			setDeviceType("Z-Wave Motion Sensor")
			break
		case "013C-0002-000D":  // Philio multi +
			log.debug "Changing device type to 3-in-1 Multisensor Plus (SG)"
			setDeviceType("3-in-1 Multisensor Plus (SG)")
			break
		case "0109-2001-0106":  // Vision door/window
			log.debug "Changing device type to Z-Wave Plus Door/Window Sensor"
			setDeviceType("Z-Wave Plus Door/Window Sensor")
			break
		case "0109-2002-0205": // Vision Motion
			log.debug "Changing device type to Z-Wave Plus Motion/Temp Sensor"
			setDeviceType("Z-Wave Plus Motion/Temp Sensor")
			break
		default:
			dthChanged = false
			break
	}
	dthChanged
}

// this is present in zwave-motion-sensor.groovy DTH too
private isEnerwave() {
	zwaveInfo?.mfr?.equals("011A") && zwaveInfo?.prod?.equals("0601") && zwaveInfo?.model?.equals("0901")
}
