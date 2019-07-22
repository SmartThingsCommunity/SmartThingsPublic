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
	definition (name: "Z-Wave Sensor", namespace: "smartthings", author: "SmartThings", runLocally: true, minHubCoreVersion: '000.017.0012', executeCommandsLocally: false) {
		capability "Sensor"
		capability "Battery"
		capability "Configuration"

		attribute "sensor", "string"

		fingerprint deviceId: "0xA1"
		fingerprint deviceId: "0x21"
		fingerprint deviceId: "0x20"
		fingerprint deviceId: "0x07"
	}

	simulator {
		status "active": "command: 3003, payload: FF"
		status "inactive": "command: 3003, payload: 00"
		status "motion":      "command: 7105, payload: 00 00 00 FF 07 08 00 00"
		status "no motion":   "command: 7105, payload: 00 00 00 FF 07 00 01 08 00"
		status "smoke":       "command: 7105, payload: 00 00 00 FF 01 02 00 00"
		status "smoke clear": "command: 7105, payload: 00 00 00 FF 01 00 01 01 00"
		status "dry notification": "command: 7105, payload: 00 00 00 FF 05 FE 00 00"
		status "wet notification": "command: 7105, payload: 00 FF 00 FF 05 02 00 00"
		status "wake up": "command: 8407, payload: "
	}

	tiles {
		standardTile("sensor", "device.sensor", width: 2, height: 2) {
			state("inactive", label:'inactive', icon:"st.unknown.zwave.sensor", backgroundColor:"#cccccc")
			state("active", label:'active', icon:"st.unknown.zwave.sensor", backgroundColor:"#00A0DC")
		}
		valueTile("battery", "device.battery", inactiveLabel: false, decoration: "flat") {
			state "battery", label:'${currentValue}% battery', unit:""
		}

		main "sensor"
		details(["sensor", "battery"])
	}
}

private getCommandClassVersions() {
	[0x20: 1, 0x30: 1, 0x31: 5, 0x32: 3, 0x80: 1, 0x84: 1, 0x71: 3, 0x9C: 1]
}

def parse(String description) {
	def result = []
	if (description.startsWith("Err")) {
	    result = createEvent(descriptionText:description, displayed:true)
	} else {
		def cmd = zwave.parse(description, commandClassVersions)
		if (cmd) {
			result = zwaveEvent(cmd)
		}
	}
	log.debug "Parsed '$description' to $result"
	return result
}

def sensorValueEvent(value) {
	if (value == 0) {
		createEvent([ name: "sensor", value: "inactive" ])
	} else if (value == 255) {
		createEvent([ name: "sensor", value: "active" ])
	} else {
		[ createEvent([ name: "sensor", value: "active" ]),
			createEvent([ name: "level", value: value ]) ]
	}
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd)
{
	sensorValueEvent(cmd.value)
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicSet cmd)
{
	sensorValueEvent(cmd.value)
}

def zwaveEvent(physicalgraph.zwave.commands.sensorbinaryv1.SensorBinaryReport cmd)
{
	sensorValueEvent(cmd.sensorValue)
}

def zwaveEvent(physicalgraph.zwave.commands.alarmv1.AlarmReport cmd)
{
	sensorValueEvent(cmd.alarmLevel)
}

def zwaveEvent(physicalgraph.zwave.commands.sensoralarmv1.SensorAlarmReport cmd)
{
	sensorValueEvent(cmd.sensorState)
}

def zwaveEvent(physicalgraph.zwave.commands.sensormultilevelv5.SensorMultilevelReport cmd)
{
	def map = [ displayed: true, value: cmd.scaledSensorValue.toString() ]
	switch (cmd.sensorType) {
		case 1:
			map.name = "temperature"
			map.unit = cmd.scale == 1 ? "F" : "C"
			break;
		case 2:
			map.name = "value"
			map.unit = cmd.scale == 1 ? "%" : ""
			break;
		case 3:
			map.name = "illuminance"
			map.value = cmd.scaledSensorValue.toInteger().toString()
			map.unit = "lux"
			break;
		case 4:
			map.name = "power"
			map.unit = cmd.scale == 1 ? "Btu/h" : "W"
			break;
		case 5:
			map.name = "humidity"
			map.value = cmd.scaledSensorValue.toInteger().toString()
			map.unit = cmd.scale == 0 ? "%" : ""
			break;
		case 6:
			map.name = "velocity"
			map.unit = cmd.scale == 1 ? "mph" : "m/s"
			break;
		case 8:
		case 9:
			map.name = "pressure"
			map.unit = cmd.scale == 1 ? "inHg" : "kPa"
			break;
		case 0xE:
			map.name = "weight"
			map.unit = cmd.scale == 1 ? "lbs" : "kg"
			break;
		case 0xF:
			map.name = "voltage"
			map.unit = cmd.scale == 1 ? "mV" : "V"
			break;
		case 0x10:
			map.name = "current"
			map.unit = cmd.scale == 1 ? "mA" : "A"
			break;
		case 0x12:
			map.name = "air flow"
			map.unit = cmd.scale == 1 ? "cfm" : "m^3/h"
			break;
		case 0x1E:
			map.name = "loudness"
			map.unit = cmd.scale == 1 ? "dBA" : "dB"
			break;
	}
	createEvent(map)
}

def zwaveEvent(physicalgraph.zwave.commands.meterv3.MeterReport cmd) {
	def map = [ displayed: true, value: cmd.scaledMeterValue ]
	if (cmd.meterType == 1) {
		map << ([
			[ name: "energy", unit: "kWh" ],
			[ name: "energy", unit: "kVAh" ],
			[ name: "power", unit: "W" ],
			[ name: "pulse count", unit: "pulses" ],
			[ name: "voltage", unit: "V" ],
			[ name: "current", unit: "A"],
			[ name: "power factor", unit: "R/Z"],
		][cmd.scale] ?: [ name: "electric" ])
	} else if (cmd.meterType == 2) {
		map << [ name: "gas", unit: ["m^3", "ft^3", "", "pulses", ""][cmd.scale] ]
	} else if (cmd.meterType == 3) {
		map << [ name: "water", unit: ["m^3", "ft^3", "gal"][cmd.scale] ]
	} else {
		map << [ name: "meter", descriptionText: cmd.toString() ]
	}
	createEvent(map)
}

def notificationEvent(String description, String value = "active") {
	createEvent([ name: "sensor", value: value, descriptionText: description, isStateChange: true ])
}

def zwaveEvent(physicalgraph.zwave.commands.notificationv3.NotificationReport cmd)
{
	def result = []
	if (cmd.notificationType == 0x01) {  // Smoke Alarm
		log.debug "Changing device type to Z-Wave Smoke Alarm"
		setDeviceType("Z-Wave Smoke Alarm")
		switch (cmd.event) {
			case 0x00:
			case 0xFE:
				result << notificationEvent("Smoke is clear", "inactive")
				result << createEvent(name: "smoke", value: "clear")
				break
			case 0x01:
			case 0x02:
				result << notificationEvent("Smoke detected")
				result << createEvent(name: "smoke", value: "detected")
				break
			case 0x03:
				result << notificationEvent("Smoke alarm tested")
				result << createEvent(name: "smoke", value: "tested")
				break
		}
	} else if (cmd.notificationType == 0x05) {  // Water Alarm
		switch (cmd.event) {
		case 0x00:
		case 0xFE:
			result << notificationEvent("Water alarm cleared", "inactive")
			result << createEvent(name: "water", value: "dry")
			break
		case 0x01:
		case 0x02:
			log.debug "Changing device type to Z-Wave Water Sensor"
			setDeviceType("Z-Wave Water Sensor")
			result << notificationEvent("Water leak detected")
			result << createEvent(name: "water", value: "wet")
			break
		case 0x03:
		case 0x04:
			result << notificationEvent("Water level dropped")
			break
		case 0x05:
			result << notificationEvent("Replace water filter")
			break
		case 0x06:
			def level = ["alarm", "alarm", "below low threshold", "above high threshold", "max"][cmd.eventParameter[0]]
			result << notificationEvent("Water flow $level")
			break
		case 0x07:
			def level = ["alarm", "alarm", "below low threshold", "above high threshold", "max"][cmd.eventParameter[0]]
			result << notificationEvent("Water pressure $level")
			break
		}
	} else if (cmd.notificationType == 0x06) {  // Access Control
		switch (cmd.event) {
			case 0x00:
				if (cmd.eventParametersLength && cmd.eventParameter.size() && eventParameter[0] != 0x16) {
					result << notificationEvent("Access control event cleared", "inactive")
				} else {
					result << notificationEvent("$device.displayName is closed", "inactive")
				}
			case 0x16:
				setDeviceType("Z-Wave Door/Window Sensor")
				result << notificationEvent("$device.displayName is open")
				result << createEvent(name: "contact", value: "open")
				break
			case 0x17:
				setDeviceType("Z-Wave Door/Window Sensor")
				result << notificationEvent("$device.displayName is closed")
				result << createEvent(name: "contact", value: "closed")
				break
		}
	} else if (cmd.notificationType == 0x07) {  // Home Security
		if (cmd.event == 0x00) {
			result << sensorValueEvent(0)
		} else if (cmd.event == 0x01 || cmd.event == 0x02) {
			result << sensorValueEvent(1)
		} else if (cmd.event == 0x03) {
			result << notificationEvent("$device.displayName covering was removed")
		} else if (cmd.event == 0x05 || cmd.event == 0x06) {
			result << notificationEvent("$device.displayName detected glass breakage")
		} else if (cmd.event == 0x07 || cmd.event == 0x08) {
			setDeviceType("Z-Wave Motion Sensor")
			result << notificationEvent("Motion detected")
			result << createEvent(name: "motion", value: "active", descriptionText:"$device.displayName detected motion")
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

def zwaveEvent(physicalgraph.zwave.commands.wakeupv1.WakeUpNotification cmd)
{
	def result = []
	result << new physicalgraph.device.HubAction(zwave.wakeUpV1.wakeUpNoMoreInformation().format())
	result << createEvent(descriptionText: "${device.displayName} woke up", isStateChange: false)
	result
}

def zwaveEvent(physicalgraph.zwave.commands.batteryv1.BatteryReport cmd) {
	def map = [ name: "battery", unit: "%" ]
	if (cmd.batteryLevel == 0xFF) {
		map.value = 1
		map.descriptionText = "${device.displayName} has a low battery"
	} else {
		map.value = cmd.batteryLevel
	}
	createEvent(map)
}

def zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityMessageEncapsulation cmd) {
	def encapsulatedCommand = cmd.encapsulatedCommand(commandClassVersions)
	if (encapsulatedCommand) {
		state.sec = 1
		zwaveEvent(encapsulatedCommand)
	}
}

def zwaveEvent(physicalgraph.zwave.commands.crc16encapv1.Crc16Encap cmd)
{
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


def configure() {
	if (zwaveInfo.cc?.contains("84")) {
		zwave.wakeUpV1.wakeUpNoMoreInformation().format()
	}
}
