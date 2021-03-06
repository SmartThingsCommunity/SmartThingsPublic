/**
 *  Copyright 2020 SmartThings
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
 *  Generic Z-Wave Water/Temp/Humidity Sensor
 *
 *  Author: SmartThings
 *  Date: 2020-07-06
 */

metadata {
	definition(name: "Z-Wave Water/Temp/Humidity Sensor", namespace: "smartthings", author: "SmartThings", mnmn: "SmartThings", vid: "generic-leak-5", ocfDeviceType: "x.com.st.d.sensor.moisture") {
		capability "Water Sensor"
		capability "Temperature Measurement"
		capability "Relative Humidity Measurement"
		capability "Tamper Alert"
		capability "Battery"
		capability "Sensor"
		capability "Health Check"
		capability "Configuration"

		fingerprint mfr:"0371", prod:"0002", model:"0013", deviceJoinName: "Aeotec Water Leak Sensor", mnmn: "SmartThings", vid: "aeotec-water-sensor-7-pro" //EU //Aeotec Water Sensor 7 Pro
		fingerprint mfr:"0371", prod:"0102", model:"0013", deviceJoinName: "Aeotec Water Leak Sensor", mnmn: "SmartThings", vid: "aeotec-water-sensor-7-pro" //US //Aeotec Water Sensor 7 Pro
		fingerprint mfr:"0371", prod:"0202", model:"0013", deviceJoinName: "Aeotec Water Leak Sensor", mnmn: "SmartThings", vid: "aeotec-water-sensor-7-pro" //AU //Aeotec Water Sensor 7 Pro
	}

	tiles(scale: 2) {
		multiAttributeTile(name: "water", type: "generic", width: 6, height: 4) {
			tileAttribute("device.water", key: "PRIMARY_CONTROL") {
				attributeState("dry", label:'${name}', icon: "st.alarm.water.dry", backgroundColor: "#ffffff")
				attributeState("wet", label:'${name}', icon: "st.alarm.water.wet", backgroundColor: "#00A0DC")
			}
		}
		valueTile("battery", "device.battery", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "battery", label: '${currentValue}% battery'
		}
		valueTile("humidity", "device.humidity", inactiveLabel: false, width: 2, height: 2) {
			state "humidity", label: '${currentValue}% humidity', unit: ""
		}
		valueTile("temperature", "device.temperature", width: 2, height: 2) {
			state("temperature", label: '${currentValue}°',
					backgroundColors: [
							// Celsius
							[value: 0, color: "#153591"],
							[value: 7, color: "#1e9cbb"],
							[value: 15, color: "#90d2a7"],
							[value: 23, color: "#44b621"],
							[value: 28, color: "#f1d801"],
							[value: 35, color: "#d04e00"],
							[value: 37, color: "#bc2323"],
							// Fahrenheit
							[value: 40, color: "#153591"],
							[value: 44, color: "#1e9cbb"],
							[value: 59, color: "#90d2a7"],
							[value: 74, color: "#44b621"],
							[value: 84, color: "#f1d801"],
							[value: 95, color: "#d04e00"],
							[value: 96, color: "#bc2323"]
					])
		}
		valueTile("tamper", "device.tamper", height: 2, width: 2, decoration: "flat") {
			state "clear", label: 'tamper clear', backgroundColor: "#ffffff"
			state "detected", label: 'tampered', backgroundColor: "#ff0000"
		}

		main "water"
		details(["water", "humidity", "temperature", "battery", "tamper"])
	}
}

def installed() {
	clearTamper()
	response([
		secure(zwave.batteryV1.batteryGet()),
		"delay 500",
		secure(zwave.notificationV3.notificationGet(notificationType: 0x05)), // water
		"delay 500",
		secure(zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType: 0x01)), // temperature
		"delay 500",
		secure(zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType: 0x05)), // humidity
		"delay 10000",
		secure(zwave.wakeUpV2.wakeUpNoMoreInformation())
	])
}

def updated() {
	configure()
}

def configure() {
	sendEvent(name: "checkInterval", value: 8 * 60 * 60 + 10 * 60, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])
}

def parse(String description) {
	def results = []

	if (description.startsWith("Err")) {
		results += createEvent(descriptionText: description, displayed: true)
	} else {
		def cmd = zwave.parse(description)
		if (cmd) {
			results += zwaveEvent(cmd)
		}
	}

	log.debug "parse() result ${results.inspect()}"

	return results
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

def zwaveEvent(physicalgraph.zwave.commands.notificationv3.NotificationReport cmd) {
	if (cmd.notificationType == 0x05) {
		if (cmd.event == 0x01 || cmd.event == 0x02) {
			sensorWaterEvent(1)
		} else if (cmd.event == 0x00) {
			sensorWaterEvent(0)
		}
	} else if (cmd.notificationType == 0x07) {
		if (cmd.event == 0x03) {
			runIn(10, clearTamper, [overwrite: true, forceForLocallyExecuting: true])
			createEvent(name: "tamper", value: "detected", descriptionText: "$device.displayName was tampered")
		} else if (cmd.event == 0x00) {
			createEvent(name: "tamper", value: "clear")
		}
	}
}

def zwaveEvent(physicalgraph.zwave.commands.sensorbinaryv2.SensorBinaryReport cmd) {
	return sensorWaterEvent(cmd.sensorValue)
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd) {
	return sensorWaterEvent(cmd.value)
}

def zwaveEvent(physicalgraph.zwave.commands.batteryv1.BatteryReport cmd) {
	def map = [name: "battery", unit: "%", isStateChange: true]
	state.lastbatt = now()

	if (cmd.batteryLevel == 0xFF) {
		map.value = 1
		map.descriptionText = "$device.displayName battery is low!"
	} else {
		map.value = cmd.batteryLevel
	}

	createEvent(map)
}

def zwaveEvent(physicalgraph.zwave.commands.sensormultilevelv5.SensorMultilevelReport cmd) {
	def map = [:]

	switch (cmd.sensorType) {
		case 0x01:
			map.name = "temperature"
			map.unit = temperatureScale
			map.value = convertTemperatureIfNeeded(cmd.scaledSensorValue, cmd.scale == 1 ? "F" : "C", cmd.precision)
			break
		case 0x05:
			map.name = "humidity"
			map.value = cmd.scaledSensorValue.toInteger()
			map.unit = "%"
			break
		default:
			map.descriptionText = cmd.toString()
	}

	createEvent(map)
}

def zwaveEvent(physicalgraph.zwave.commands.wakeupv2.WakeUpNotification cmd) {
	def cmds = []
	def result = createEvent(descriptionText: "$device.displayName woke up", isStateChange: false)
	cmds += secure(zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType: 0x05))
	cmds += secure(zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType: 0x01))

	if (!state.lastbatt || (now() - state.lastbatt) >= 10 * 60 * 60 * 1000) {
		cmds += ["delay 1000",
				 secure(zwave.batteryV1.batteryGet()),
				 "delay 2000"
		]
	}

	cmds += secure(zwave.wakeUpV2.wakeUpNoMoreInformation())

	[result, response(cmds)]
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
	log.warn "Unhandled command: ${cmd}"
}

def sensorWaterEvent(value) {
	if (value) {
		createEvent(name: "water", value: "wet", descriptionText: "$device.displayName detected water leakage")
	} else {
		createEvent(name: "water", value: "dry", descriptionText: "$device.displayName detected that leakage is no longer present")
	}
}

def clearTamper() {
	sendEvent(name: "tamper", value: "clear")
}

private secure(cmd) {
	if (zwaveInfo.zw.contains("s")) {
		zwave.securityV1.securityMessageEncapsulation().encapsulate(cmd).format()
	} else {
		cmd.format()
	}
}
