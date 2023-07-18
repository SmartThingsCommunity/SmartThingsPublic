/**
*	Copyright 2016 SmartThings
*	Copyright 2015 AstraLink
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
*	Z-Wave Plus Door/Window Sensor, ZD2102*-5
*
*/

metadata {
	definition (name: "Z-Wave Plus Door/Window Sensor", namespace: "smartthings", author: "SmartThings", ocfDeviceType: "x.com.st.d.sensor.contact", runLocally: true, minHubCoreVersion: '000.020.00008', executeCommandsLocally: true) {
		capability "Contact Sensor"
		capability "Configuration"
		capability "Battery"
		capability "Sensor"

		// for Astralink
		attribute "ManufacturerCode", "string"
		attribute "ProduceTypeCode", "string"
		attribute "ProductCode", "string"
		attribute "WakeUp", "string"
		attribute "WirelessConfig", "string"

		fingerprint deviceId: "0x0701", inClusters: "0x5E, 0x98, 0x86, 0x72, 0x5A, 0x85, 0x59, 0x73, 0x80, 0x71, 0x70, 0x84, 0x7A", deviceJoinName: "Open/Closed Sensor"
		fingerprint type:"8C07", inClusters: "5E,98,86,72,5A,71", deviceJoinName: "Open/Closed Sensor"
		fingerprint mfr:"0109", prod:"2001", model:"0106", deviceJoinName: "Open/Closed Sensor"// not using deviceJoinName because it's sold under different brand names
	}

	tiles(scale: 2) {
		multiAttributeTile(name:"contact", type: "generic", width: 6, height: 4){
			tileAttribute ("device.contact", key: "PRIMARY_CONTROL") {
				attributeState "open", label:'${name}', icon:"st.contact.contact.open", backgroundColor:"#e86d13"
				attributeState "closed", label:'${name}', icon:"st.contact.contact.closed", backgroundColor:"#00a0dc"
			}
		}
		valueTile("battery", "device.battery", decoration: "flat", inactiveLabel: false, width: 2, height: 2) {
			state "battery", label:'${currentValue}% battery', unit:""
		}

		main (["contact"])
		details(["contact","battery"])
	}

	simulator {
		// messages the device returns in response to commands it receives
		status "open (basic)" : "command: 9881, payload: 00 20 01 FF"
		status "closed (basic)" : "command: 9881 payload: 00 20 01 00"
		status "open (notification)" : "command: 9881, payload: 00 71 05 06 FF 00 FF 06 16 00 00"
		status "closed (notification)" : "command: 9881, payload: 00 71 05 06 00 00 FF 06 17 00 00"
		status "tamper: enclosure opened" : "command: 9881, payload: 00 71 05 07 FF 00 FF 07 03 00 00"
		status "tamper: enclosure replaced" : "command: 9881, payload: 00 71 05 07 00 00 FF 07 00 00 00"
		status "wake up" : "command: 9881, payload: 00 84 07"
		status "battery (100%)" : "command: 9881, payload: 00 80 03 64"
		status "battery low" : "command: 9881, payload: 00 80 03 FF"
	}
}

def configure() {
	log.debug "configure()"
	def cmds = []

	if (state.sec != 1) {
		// secure inclusion may not be complete yet
		cmds << "delay 1000"
	}

	cmds += secureSequence([
		zwave.manufacturerSpecificV2.manufacturerSpecificGet(),
		zwave.batteryV1.batteryGet(),
	], 500)

	cmds << "delay 8000"
	cmds << secure(zwave.wakeUpV1.wakeUpNoMoreInformation())
	return cmds
}

private getCommandClassVersions() {
	[
		0x71: 3,  // Notification
		0x5E: 2,  // ZwaveplusInfo
		0x59: 1,  // AssociationGrpInfo
		0x85: 2,  // Association
		0x20: 1,  // Basic
		0x80: 1,  // Battery
		0x70: 1,  // Configuration
		0x5A: 1,  // DeviceResetLocally
		0x7A: 2,  // FirmwareUpdateMd
		0x72: 2,  // ManufacturerSpecific
		0x73: 1,  // Powerlevel
		0x98: 1,  // Security
		0x84: 2,  // WakeUp
		0x86: 1,  // Version
	]
}

// Parse incoming device messages to generate events
def parse(String description) {
	def result = []
	def cmd
	if (description.startsWith("Err 106")) {
		state.sec = 0
		result = createEvent( name: "secureInclusion", value: "failed", eventType: "ALERT",
				descriptionText: "This sensor failed to complete the network security key exchange. If you are unable to control it via SmartThings, you must remove it from your network and add it again.")
	} else if (description.startsWith("Err")) {
		result = createEvent(descriptionText: "$device.displayName $description", isStateChange: true)
	} else {
		cmd = zwave.parse(description, commandClassVersions)
		if (cmd) {
			result = zwaveEvent(cmd)
		}
	}

	if (result instanceof List) {
		result = result.flatten()
	}

	log.debug "Parsed '$description' to $result"
	return result
}

def zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityMessageEncapsulation cmd) {
	def encapsulatedCommand = cmd.encapsulatedCommand(commandClassVersions)
	log.debug "encapsulated: $encapsulatedCommand"
	if (encapsulatedCommand) {
		state.sec = 1
		return zwaveEvent(encapsulatedCommand)
	} else {
		log.warn "Unable to extract encapsulated cmd from $cmd"
		return [createEvent(descriptionText: cmd.toString())]
	}
}

def sensorValueEvent(value) {
	if (value) {
		createEvent(name: "contact", value: "open", descriptionText: "$device.displayName is open")
	} else {
		createEvent(name: "contact", value: "closed", descriptionText: "$device.displayName is closed")
	}
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd) {
	return sensorValueEvent(cmd.value)
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicSet cmd) {
	return sensorValueEvent(cmd.value)
}

def zwaveEvent(physicalgraph.zwave.commands.sensorbinaryv2.SensorBinaryReport cmd) {
	return sensorValueEvent(cmd.sensorValue)
}

def zwaveEvent(physicalgraph.zwave.commands.sensoralarmv1.SensorAlarmReport cmd) {
	return sensorValueEvent(cmd.sensorState)
}

def zwaveEvent(physicalgraph.zwave.commands.notificationv3.NotificationReport cmd) {
	def result = []
	if (cmd.notificationType == 0x06 && cmd.event == 0x16) {
		result << sensorValueEvent(1)
	} else if (cmd.notificationType == 0x06 && cmd.event == 0x17) {
		result << sensorValueEvent(0)
	} else if (cmd.notificationType == 0x07) {
		if (cmd.event == 0x00) {
			if (cmd.eventParametersLength == 0 || cmd.eventParameter[0] != 3) {
				result << createEvent(descriptionText: "$device.displayName covering replaced", isStateChange: true, displayed: false)
			} else {
				result << sensorValueEvent(0)
			}
		} else if (cmd.event == 0x01 || cmd.event == 0x02) {
			result << sensorValueEvent(1)
		} else if (cmd.event == 0x03) {
			result << createEvent(descriptionText: "$device.displayName covering was removed", isStateChange: true)
			if (!device.currentState("ManufacturerCode")) {
				result << response(secure(zwave.manufacturerSpecificV2.manufacturerSpecificGet()))
			}
		} else if (cmd.event == 0x05 || cmd.event == 0x06) {
			result << createEvent(descriptionText: "$device.displayName detected glass breakage", isStateChange: true)
		} else {
			result << createEvent(descriptionText: "$device.displayName event $cmd.event ${cmd.eventParameter.inspect()}", isStateChange: true, displayed: false)
		}
	} else if (cmd.notificationType) {
		result << createEvent(descriptionText: "$device.displayName notification $cmd.notificationType event $cmd.event ${cmd.eventParameter.inspect()}", isStateChange: true, displayed: false)
	} else {
		def value = cmd.v1AlarmLevel == 255 ? "active" : cmd.v1AlarmLevel ?: "inactive"
		result << createEvent(name: "alarm $cmd.v1AlarmType", value: value, isStateChange: true, displayed: false)
	}
	return result
}

def zwaveEvent(physicalgraph.zwave.commands.wakeupv2.WakeUpNotification cmd) {
	def event = createEvent(name: "WakeUp", value: "wakeup", descriptionText: "${device.displayName} woke up", isStateChange: true, displayed: false)  // for Astralink
	def cmds = []

	if (!device.currentState("ManufacturerCode")) {
		cmds << secure(zwave.manufacturerSpecificV2.manufacturerSpecificGet())
		cmds << "delay 2000"
	}
	if (!state.lastbat || now() - state.lastbat > 10*60*60*1000) {
		event.descriptionText += ", requesting battery"
		cmds << secure(zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType:1, scale:1))
		cmds << "delay 800"
		cmds << secure(zwave.batteryV1.batteryGet())
		cmds << "delay 2000"
	} else {
		log.debug "not checking battery, was updated ${(now() - state.lastbat)/60000 as int} min ago"
	}
	cmds << secure(zwave.wakeUpV1.wakeUpNoMoreInformation())

	return [event, response(cmds)]
}

def zwaveEvent(physicalgraph.zwave.commands.batteryv1.BatteryReport cmd) {
	def map = [ name: "battery", unit: "%" ]
	if (cmd.batteryLevel == 0xFF) {
		map.value = 1
		map.descriptionText = "${device.displayName} has a low battery"
		map.isStateChange = true
	} else {
		map.value = cmd.batteryLevel
	}
	def event = createEvent(map)

	// Save at least one battery report in events list every few days
	if (!event.isStateChange && (now() - 3*24*60*60*1000) > device.latestState("battery")?.date?.time) {
		map.isStateChange = true
	}
	state.lastbat = now()
	return [event]
}

def zwaveEvent(physicalgraph.zwave.commands.manufacturerspecificv2.ManufacturerSpecificReport cmd) {
	def result = []
	def manufacturerCode = String.format("%04X", cmd.manufacturerId)
	def productTypeCode = String.format("%04X", cmd.productTypeId)
	def productCode = String.format("%04X", cmd.productId)
	def wirelessConfig = "ZWP"
	log.debug "MSR ${manufacturerCode} ${productTypeCode} ${productCode}"

	result << createEvent(name: "ManufacturerCode", value: manufacturerCode)
	result << createEvent(name: "ProduceTypeCode", value: productTypeCode)
	result << createEvent(name: "ProductCode", value: productCode)
	result << createEvent(name: "WirelessConfig", value: wirelessConfig)

	return result
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
	return [createEvent(descriptionText: "$device.displayName: $cmd", displayed: false)]
}

private secure(physicalgraph.zwave.Command cmd) {
	if (state.sec == 0) {  // default to secure
		cmd.format()
	} else {
		zwave.securityV1.securityMessageEncapsulation().encapsulate(cmd).format()
	}
}

private secureSequence(commands, delay=200) {
	delayBetween(commands.collect{ secure(it) }, delay)
}
