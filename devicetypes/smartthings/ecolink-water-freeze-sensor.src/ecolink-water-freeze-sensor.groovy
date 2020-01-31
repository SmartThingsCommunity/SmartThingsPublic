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
 *  Ecolink Water/Freeze Sensor
 *
 */

metadata {
	definition(name: "Ecolink Water/Freeze Sensor", namespace: "smartthings", author: "SmartThings", ocfDeviceType: "x.com.st.d.sensor.moisture", runLocally: false, minHubCoreVersion: '000.017.0012', executeCommandsLocally: false) {
		capability "Water Sensor"
		capability "Temperature Alarm"
		capability "Sensor"
		capability "Battery"
		capability "Health Check"

		fingerprint mfr: "014A", prod: "0005", model: "0010", deviceJoinName: "Ecolink Water/Freeze Sensor"
	}

	simulator {
	}

	tiles(scale: 2) {
		multiAttributeTile(name: "water", type: "generic", width: 6, height: 4) {
			tileAttribute("device.water", key: "PRIMARY_CONTROL") {
				attributeState("dry", icon: "st.alarm.water.dry", backgroundColor: "#ffffff")
				attributeState("wet", icon: "st.alarm.water.wet", backgroundColor: "#00A0DC")
			}
		}
		valueTile("battery", "device.battery", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "battery", label: '${currentValue}% battery', unit: ""
		}
		valueTile("temperatureAlarm", "device.temperatureAlarm", inactiveLabel: false, decoration: "flat", width: 4, height: 2) {
			state "cleared", icon: "st.Weather.weather14", label: '${currentValue}', unit: ""
			state "freeze", icon: "st.Weather.weather7", label: '${currentValue}', unit: ""
		}

		main "water"
		details(["water", "battery", "temperatureAlarm"])
	}
}

def installed() {
	sendEvent(name: "checkInterval", value: (2 * 4 * 60 + 2) * 60, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])
	//initial states will be updated in when cover is closed, as device sends wakeup notification when this happens
}

def updated() {
	sendEvent(name: "checkInterval", value: (2 * 4 * 60 + 2) * 60, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])
}

def parse(String description) {
	def result
	if (description.startsWith("Err")) {
		result = createEvent(descriptionText: description)
	} else {
		def cmd = zwave.parse(description)
		if (cmd) {
			result = zwaveEvent(cmd)
		} else {
			result = createEvent(value: description, descriptionText: description, isStateChange: false)
		}
	}
	log.debug "Parsed '$description' to $result"
	result
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd) {
	waterSensorValueEvent(cmd.value)
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicSet cmd) {
	waterSensorValueEvent(cmd.value)
}

def zwaveEvent(physicalgraph.zwave.commands.sensorbinaryv2.SensorBinaryReport cmd) {
	if (cmd.sensorType == 0x06) {
		waterSensorValueEvent(cmd.sensorValue)
	} else if (cmd.sensorType == 0x07) {
		freezeSensorValueEvent(cmd.sensorValue)
	} else {
		createEvent(descriptionText: "Unknown sensor report: Sensor type: $cmd.sensorType, Sensor value: $cmd.sensorValue", displayed: true)
	}
}

def zwaveEvent(physicalgraph.zwave.commands.notificationv3.NotificationReport cmd) {
	def result
	if (cmd.notificationType == 0x05) {
		result = handleWaterNotification(cmd.event)
	} else if (cmd.notificationType == 0x07) {
		result = handleHomeSecurityNotification(cmd.event)
	} else if (cmd.notificationType == 0x08) {
		result = handlePowerManagementNotification(cmd.event)
	} else if (cmd.notificationType) {
		def text = "Notification $cmd.notificationType: event ${([cmd.event] + cmd.eventParameter).join(", ")}"
		result = createEvent(name: "notification$cmd.notificationType", value: "$cmd.event", descriptionText: text, displayed: false)
	} else {
		def value = cmd.v1AlarmLevel == 255 ? "active" : cmd.v1AlarmLevel ?: "inactive"
		result = createEvent(name: "alarm $cmd.v1AlarmType", value: value, displayed: false)
	}
	result
}

def zwaveEvent(physicalgraph.zwave.commands.wakeupv2.WakeUpNotification cmd) {
	def result = []
	if (device.currentValue("temperatureAlarm") == null) {
		result << response(zwave.sensorBinaryV2.sensorBinaryGet(sensorType: 0x07))
	}
	if (device.currentValue("water") == null) {
		result << response(zwave.sensorBinaryV2.sensorBinaryGet(sensorType: 0x06))
	}
	if (!state.lastbat || (new Date().time) - state.lastbat > 53 * 60 * 60 * 1000) {
		result << response(zwave.batteryV1.batteryGet())
	} else {
		result << response(zwave.wakeUpV1.wakeUpNoMoreInformation())
	}
	result << createEvent(descriptionText: "${device.displayName} woke up", isStateChange: false)
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


def zwaveEvent(physicalgraph.zwave.Command cmd) {
	createEvent(descriptionText: "$device.displayName: $cmd", displayed: false)
}

def handleWaterNotification(event) {
	def map
	if (event == 0x02) {
		map = [name: "water", value: "wet"]
	} else if (event == 0x04) {
		map = [name: "water", value: "dry"]
	} else {
		map = [displayed: false, descriptionText: "Water event $event"]
	}
	createEvent(map)
}

def handleHomeSecurityNotification(event) {
	def map
	if (event == 0x00) {
		map = [descriptionText: "$device.displayName covering was closed", isStateChange: true]
	} else if (event == 0x03) {
		map = [descriptionText: "$device.displayName covering was removed", isStateChange: true]
	} else {
		map = [displayed: false, descriptionText: "Home Security Notification event $event"]
	}
	createEvent(map)
}

def handlePowerManagementNotification(event) {
	def map
	if (event == 0x0A) {
		map = [name: "battery", value: 1, descriptionText: "Battery is getting low", displayed: true]
	} else if (event == 0x0b) {
		map = [name: "battery", value: 0, descriptionText: "Battery needs replacing", isStateChange: true]
	} else {
		map = [displayed: false, descriptionText: "Power Management Notification event $event"]
	}
	createEvent(map)
}

def waterSensorValueEvent(value) {
	def eventValue = value ? "wet" : "dry"
	createEvent(name: "water", value: eventValue, descriptionText: "$device.displayName is $eventValue")
}

def freezeSensorValueEvent(value) {
	def eventValue = value ? "freeze" : "cleared"
	createEvent(name: "temperatureAlarm", value: eventValue, descriptionText: "$device.displayName is $eventValue")
}

