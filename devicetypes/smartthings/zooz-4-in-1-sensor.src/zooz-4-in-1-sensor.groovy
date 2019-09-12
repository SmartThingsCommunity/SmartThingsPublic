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
	definition(name: "Zooz 4-in-1 sensor", namespace: "smartthings", author: "SmartThings", mnmn: "SmartThings", vid: "generic-motion-8", ocfDeviceType: "x.com.st.d.sensor.motion") {
		capability "Motion Sensor"
		capability "Temperature Measurement"
		capability "Relative Humidity Measurement"
		capability "Illuminance Measurement"
		capability "Configuration"
		capability "Sensor"
		capability "Battery"
		capability "Health Check"
		capability "Tamper Alert"

		fingerprint mfr: "027A", prod: "2021", model: "2101", deviceJoinName: "Zooz 4-in-1 sensor"
	}

	tiles(scale: 2) {
		multiAttributeTile(name: "motion", type: "generic", width: 6, height: 4) {
			tileAttribute("device.motion", key: "PRIMARY_CONTROL") {
				attributeState "active", label: 'motion', icon: "st.motion.motion.active", backgroundColor: "#00a0dc"
				attributeState "inactive", label: 'no motion', icon: "st.motion.motion.inactive", backgroundColor: "#cccccc"
			}
		}
		valueTile("temperature", "device.temperature", inactiveLabel: false, width: 2, height: 2) {
			state "temperature", label: '${currentValue}°',
					backgroundColors: [
							[value: 32, color: "#153591"],
							[value: 44, color: "#1e9cbb"],
							[value: 59, color: "#90d2a7"],
							[value: 74, color: "#44b621"],
							[value: 84, color: "#f1d801"],
							[value: 92, color: "#d04e00"],
							[value: 98, color: "#bc2323"]
					]
		}
		valueTile("humidity", "device.humidity", inactiveLabel: false, width: 2, height: 2) {
			state "humidity", label: '${currentValue}% humidity', unit: ""
		}
		valueTile("illuminance", "device.illuminance", inactiveLabel: false, width: 2, height: 2) {
			state "luminosity", label: '${currentValue} lux', unit: ""
		}
		valueTile("battery", "device.battery", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "battery", label: '${currentValue}% battery', unit: ""
		}
		valueTile("tamper", "device.tamper", height: 2, width: 2, decoration: "flat") {
			state "clear", label: 'tamper clear', backgroundColor: "#ffffff"
			state "detected", label: 'tampered', backgroundColor: "#ff0000"
		}

		main(["motion", "temperature", "humidity", "illuminance"])
		details(["motion", "temperature", "humidity", "illuminance", "battery", "tamper"])
	}
}

def initialize() {
	sendEvent(name: "checkInterval", value: 2 * 60 * 60 + 2 * 60, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])
	clearTamper()
}

def installed() {
	initialize()
}

def updated() {
	initialize()
}

def parse(String description) {
	def result = []
	if (description.startsWith("Err")) {
		result = createEvent(descriptionText:description, isStateChange:true)
	} else {
		def cmd = zwave.parse(description)
		if (cmd) {
			result += zwaveEvent(cmd)
		}
	}
	log.debug "Parse returned: ${result}"
	result
}

def zwaveEvent(physicalgraph.zwave.commands.wakeupv2.WakeUpNotification cmd) {
	def results = []
	results += createEvent(descriptionText: "$device.displayName woke up", isStateChange: false)
	results += response([
			secure(zwave.batteryV1.batteryGet()),
			"delay 2000",
			secure(zwave.wakeUpV2.wakeUpNoMoreInformation())
	])
	results
}

def zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityMessageEncapsulation cmd) {
	def encapsulatedCommand = cmd.encapsulatedCommand()
	if (encapsulatedCommand) {
		zwaveEvent(encapsulatedCommand)
	} else {
		log.warn "Unable to extract encapsulated cmd from $cmd"
		createEvent(descriptionText: cmd.toString())
	}
}

def zwaveEvent(physicalgraph.zwave.commands.batteryv1.BatteryReport cmd) {
	def map = [ name: "battery", unit: "%" ]
	if (cmd.batteryLevel == 0xFF) {
		map.value = 1
		map.descriptionText = "${device.displayName} battery is low"
		map.isStateChange = true
	} else {
		map.value = cmd.batteryLevel
	}
	createEvent(map)
}

def zwaveEvent(physicalgraph.zwave.commands.sensormultilevelv5.SensorMultilevelReport cmd) {
	def map = [:]
	switch (cmd.sensorType) {
		case 1:
			map.name = "temperature"
			def cmdScale = cmd.scale == 1 ? "F" : "C"
			map.value = convertTemperatureIfNeeded(cmd.scaledSensorValue, cmdScale, cmd.precision)
			map.unit = getTemperatureScale()
			break;
		case 3:
			map.name = "illuminance"
			map.value = getLuxFromPercentage(cmd.scaledSensorValue.toInteger())
			map.unit = "lux"
			break;
		case 5:
			map.name = "humidity"
			map.value = cmd.scaledSensorValue.toInteger()
			map.unit = "%"
			break;
		default:
			map.descriptionText = cmd.toString()
	}
	createEvent(map)
}

def motionEvent(value) {
	def map = [name: "motion"]
	if (value) {
		map.value = "active"
		map.descriptionText = "$device.displayName detected motion"
	} else {
		map.value = "inactive"
		map.descriptionText = "$device.displayName motion has stopped"
	}
	createEvent(map)
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicSet cmd) {
	motionEvent(cmd.value)
}

def zwaveEvent(physicalgraph.zwave.commands.notificationv3.NotificationReport cmd) {
	def result
	if (cmd.notificationType == 7 && cmd.event == 8) {
		result = motionEvent(cmd.notificationStatus)
	} else if (cmd.notificationType == 7 && cmd.event == 3) {
		result = createEvent(name: "tamper", value: "detected", descriptionText: "$device.displayName was tampered")
		runIn(10, clearTamper, [overwrite: true, forceForLocallyExecuting: true])
	} else if (cmd.notificationType == 7 && cmd.event == 0) {
		if (cmd.eventParameter[0] == 8) {
			result = motionEvent(0)
		} else {
			result = createEvent(name: "tamper", value: "clear", descriptionText: "$device.displayName tamper was cleared")
		}
	} else {
		result = createEvent(descriptionText: cmd.toString(), isStateChange: false)
	}
	return result
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
	createEvent(descriptionText: cmd.toString(), isStateChange: false)
}

def ping() {
	secure(zwave.batteryV1.batteryGet())
}

def configure() {
	def request = []
	request << zwave.batteryV1.batteryGet()
	request << zwave.notificationV3.notificationGet(notificationType: 0x07, event: 0x08)  //motion
	request << zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType: 0x01) //temperature
	request << zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType: 0x03) //illuminance
	request << zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType: 0x05) //humidity

	secureSequence(request) + ["delay 20000", zwave.wakeUpV2.wakeUpNoMoreInformation().format()]
}

def clearTamper() {
	sendEvent(name: "tamper", value: "clear")
}

private getLuxFromPercentage(percentageValue) {
	def multiplier = luxConversionData.find {
		percentageValue >= it.min && percentageValue <= it.max
	}?.multiplier ?: 5.312
	def luxValue = percentageValue * multiplier
	Math.round(luxValue)
}

private getLuxConversionData() {[
		[min: 0, max: 9.99, multiplier: 3.843],
		[min: 10, max: 19.99, multiplier: 5.231],
		[min: 20, max: 29.99, multiplier: 4.999],
		[min: 30, max: 39.99, multiplier: 4.981],
		[min: 40, max: 49.99, multiplier: 5.194],
		[min: 50, max: 59.99, multiplier: 6.016],
		[min: 60, max: 69.99, multiplier: 4.852],
		[min: 70, max: 79.99, multiplier: 4.836],
		[min: 80, max: 89.99, multiplier: 4.613],
		[min: 90, max: 100, multiplier: 4.5]
]}

private secure(physicalgraph.zwave.Command cmd) {
	zwave.securityV1.securityMessageEncapsulation().encapsulate(cmd).format()
}

private secureSequence(commands, delay = 200) {
	delayBetween(commands.collect{ secure(it) }, delay)
}
