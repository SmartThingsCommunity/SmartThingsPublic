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
*	Z-Wave Plus Motion Sensor with Temperature Measurement, ZP3102*-5
*
*/

metadata {
	definition (name: "Z-Wave Plus Motion/Temp Sensor", namespace: "smartthings", author: "SmartThings") {
		capability "Motion Sensor"
		capability "Temperature Measurement"
		capability "Configuration"
		capability "Battery"
		capability "Sensor"

		// for Astralink
		attribute "ManufacturerCode", "string"
		attribute "ProduceTypeCode", "string"
		attribute "ProductCode", "string"
		attribute "WakeUp", "string"
		attribute "WirelessConfig", "string"
				
		fingerprint deviceId: "0x0701", inClusters: "0x5E, 0x98, 0x86, 0x72, 0x5A, 0x85, 0x59, 0x73, 0x80, 0x71, 0x31, 0x70, 0x84, 0x7A", deviceJoinName: "Motion Sensor"
		fingerprint type:"8C07", inClusters: "5E,98,86,72,5A,31,71", deviceJoinName: "Motion Sensor"
		fingerprint mfr:"0109", prod:"2002", model:"0205", deviceJoinName: "Motion Sensor"// not using deviceJoinName because it's sold under different brand names
	}

	tiles(scale: 2) {
		multiAttributeTile(name:"motion", type: "generic", width: 6, height: 4){
			tileAttribute("device.motion", key: "PRIMARY_CONTROL") {
				attributeState("active", label:'motion', icon:"st.motion.motion.active", backgroundColor:"#00A0DC")
				attributeState("inactive", label:'no motion', icon:"st.motion.motion.inactive", backgroundColor:"#CCCCCC")
			}
		}

		valueTile("temperature", "device.temperature", inactiveLabel: false, width: 2, height: 2) {
			state "temperature", label:'${currentValue}°',
				backgroundColors:[
					[value: 31, color: "#153591"],
					[value: 44, color: "#1e9cbb"],
					[value: 59, color: "#90d2a7"],
					[value: 74, color: "#44b621"],
					[value: 84, color: "#f1d801"],
					[value: 95, color: "#d04e00"],
					[value: 96, color: "#bc2323"]
				]
		}
		valueTile("battery", "device.battery", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "battery", label:'${currentValue}% battery', unit:"%"
		}

		main(["motion", "temperature"])
		details(["motion", "temperature", "battery"])
	}
}

def updated() {
	if (!device.currentState("ManufacturerCode")) {
		response(secure(zwave.manufacturerSpecificV2.manufacturerSpecificGet()))
	}
}

def configure() {
	log.debug "configure()"
	def cmds = []

	if (!isSecured()) {
		// secure inclusion may not be complete yet
		cmds << "delay 1000"
	}

	cmds += secureSequence([
		zwave.manufacturerSpecificV2.manufacturerSpecificGet(),
		zwave.batteryV1.batteryGet(),
		zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType:1, scale:1)
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
		0x31: 5,  // SensorMultilevel
		0x84: 2	  // WakeUp
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
	def result = []
	if (value) {
		log.debug "sensorValueEvent($value) : active"
		result << createEvent(name: "motion", value: "active", descriptionText: "$device.displayName detected motion")
	} else {
		log.debug "sensorValueEvent($value) : inactive"
		result << createEvent(name: "motion", value: "inactive", descriptionText: "$device.displayName motion has stopped")
	}
	return result
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd) {
	return sensorValueEvent(cmd.value)
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicSet cmd) {
	return sensorValueEvent(cmd.value)
}

def zwaveEvent(physicalgraph.zwave.commands.switchbinaryv1.SwitchBinaryReport cmd) {
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
	if (cmd.notificationType == 0x07) {
		if (cmd.event == 0x01 || cmd.event == 0x02) {
			result << sensorValueEvent(1)
		} else if (cmd.event == 0x03) {
			result << createEvent(descriptionText: "$device.displayName covering was removed", isStateChange: true)
			result << response(secure(zwave.manufacturerSpecificV2.manufacturerSpecificGet()))
		} else if (cmd.event == 0x05 || cmd.event == 0x06) {
			result << createEvent(descriptionText: "$device.displayName detected glass breakage", isStateChange: true)
		} else if (cmd.event == 0x07) {
			result << sensorValueEvent(1)
		} else if (cmd.event == 0x08) {
			result << sensorValueEvent(1)
		} else if (cmd.event == 0x00) {
			if (cmd.eventParametersLength && cmd.eventParameter[0] == 3) {
				result << createEvent(descriptionText: "$device.displayName covering replaced", isStateChange: true, displayed: false)
			} else {
				result << sensorValueEvent(0)
			}
		} else if (cmd.event == 0xFF) {
			result << sensorValueEvent(1)
		} else {
			result << createEvent(descriptionText: "$device.displayName sent event $cmd.event")
		}
	} else if (cmd.notificationType) {
		def text = "Notification $cmd.notificationType: event ${([cmd.event] + cmd.eventParameter).join(", ")}"
		result << createEvent(name: "notification$cmd.notificationType", value: "$cmd.event", descriptionText: text, displayed: false)
	} else {
		def value = cmd.v1AlarmLevel == 255 ? "active" : cmd.v1AlarmLevel ?: "inactive"
		result << createEvent(name: "alarm $cmd.v1AlarmType", value: value, displayed: false)
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
	def result = []
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

def zwaveEvent(physicalgraph.zwave.commands.sensormultilevelv5.SensorMultilevelReport cmd) {
	def result = []
	def map = [:]
	switch (cmd.sensorType) {
		case 1:
			def cmdScale = cmd.scale == 1 ? "F" : "C"
			map.name = "temperature"
			map.value = convertTemperatureIfNeeded(cmd.scaledSensorValue, cmdScale, cmd.precision)
			map.unit = getTemperatureScale()
			break;
		case 3:
			map.name = "illuminance"
			map.value = cmd.scaledSensorValue.toInteger().toString()
			map.unit = "lux"
			break;
		case 5:
			map.name = "humidity"
			map.value = cmd.scaledSensorValue.toInteger().toString()
			map.unit = cmd.scale == 0 ? "%" : ""
			break;
		case 0x1E:
			map.name = "loudness"
			map.unit = cmd.scale == 1 ? "dBA" : "dB"
			map.value = cmd.scaledSensorValue.toString()
			break;
		default:
			map.descriptionText = cmd.toString()
	}
	result << createEvent(map)
	return result
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

	if (manufacturerCode == "0109" && productTypeCode == "2002") {
		result << response(secureSequence([
			// Change re-trigger duration to 1 minute
			zwave.configurationV1.configurationSet(parameterNumber: 1, configurationValue: [1], size: 1),
			zwave.batteryV1.batteryGet(),
			zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType:1, scale:1)
		], 400))
	}

	return result
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
	return [createEvent(descriptionText: "$device.displayName: $cmd", displayed: false)]
}

private secure(physicalgraph.zwave.Command cmd) {
	if (!isSecured()) {  // default to secure
		cmd.format()
	} else {
		zwave.securityV1.securityMessageEncapsulation().encapsulate(cmd).format()
	}
}

private secureSequence(commands, delay=200) {
	delayBetween(commands.collect{ secure(it) }, delay)
}

private isSecured() {
	if (zwaveInfo && zwaveInfo.zw) {
		return zwaveInfo.zw.contains("s")
	} else {
		return state.sec == 1
	}
}
