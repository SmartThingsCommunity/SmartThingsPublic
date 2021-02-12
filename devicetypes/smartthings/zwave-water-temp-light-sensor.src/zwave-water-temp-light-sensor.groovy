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
 *  Z-Wave Water/Temp/Light Sensor
 *
 *  Author: SmartThings
 *  Date: 2018-08-09
 */

metadata {
	definition(name: "Z-Wave Water/Temp/Light Sensor", namespace: "smartthings", author: "SmartThings", ocfDeviceType: "x.com.st.d.sensor.moisture", runLocally: true, minHubCoreVersion: '000.017.0012', executeCommandsLocally: false) {
		capability "Water Sensor"
		capability "Sensor"
		capability "Battery"
		capability "Health Check"
		capability "Temperature Measurement"
		capability "Illuminance Measurement"

		fingerprint mfr: "019A", prod: "0003", model: "000A", deviceJoinName: "Sensative Water Leak Sensor" //Sensative Strips Comfort/Drip
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
		valueTile("temperature", "device.temperature", inactiveLabel: false, width: 2, height: 2) {
			state "temperature", label:'${currentValue}°'
		}
		valueTile("illuminance", "device.illuminance", inactiveLabel: false, width: 2, height: 2) {
			state "luminosity", label:'${currentValue} ${unit}', unit:"lux"
		}
		valueTile("battery", "device.battery", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "battery", label: '${currentValue}% battery', unit: ""
		}

		main "water"
		details(["water", "temperature", "illuminance", "battery"])
	}
}

def installed() {
	setCheckInterval()
	def cmds = [ zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType: 0x01).format(),
				zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType: 0x03).format(),
				zwave.notificationV3.notificationGet(notificationType: 0x05).format(), //water alarm
				zwave.configurationV2.configurationGet(parameterNumber:12).format(),
				zwave.batteryV1.batteryGet().format()]
	response(cmds)
}

def updated() {
	setCheckInterval()
}

private setCheckInterval() {
	sendEvent(name: "checkInterval", value: (2 * 12 + 2) * 60 * 60, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])
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
				if (cmd.eventParametersLength && cmd.eventParameter.size() && cmd.eventParameter[0] > 0x02) {
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
				zwave.wakeUpV1.wakeUpIntervalSet(seconds: 4 * 3600, nodeid: zwaveHubNodeId).format(),
				zwave.batteryV1.batteryGet().format()])
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
		result << response(zwave.batteryV1.batteryGet())
	} else {
		result << response(zwave.wakeUpV1.wakeUpNoMoreInformation())
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
	[createEvent(map), response(zwave.wakeUpV1.wakeUpNoMoreInformation())]
}

def zwaveEvent(physicalgraph.zwave.commands.sensormultilevelv5.SensorMultilevelReport cmd) {
	def map = [displayed: true, value: cmd.scaledSensorValue.toString()]
	switch (cmd.sensorType) {
		case 1:
			map.name = "temperature"
			map.unit = cmd.scale == 1 ? "F" : "C"
			break;
		case 3:
			map.name = "illuminance"
			map.unit = "lux"
			break
		// This is commented out as the device's notification reports for water tend to be a better baseline
		/*case 0x1F:
			map.name = "water"
			map.value = cmd.scaledSensorValue.toInteger() > 25 ? "wet" : "dry" //25 is the default value for the device sending a wet alarm
			break;*/
	}
	createEvent(map)
}

def zwaveEvent(physicalgraph.zwave.commands.configurationv2.ConfigurationReport cmd) {
	log.debug "Report. Param: $cmd.parameterNumber scaledValue: $cmd.scaledConfigurationValue"
	if (cmd.parameterNumber == 12 && cmd.scaledConfigurationValue == 0) {
		log.debug "Sensative Comfort detected. Changing device type."
		setDeviceType("Z-Wave Temp/Light Sensor")
	}
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
	createEvent(descriptionText: "$device.displayName: $cmd", displayed: false)
}
