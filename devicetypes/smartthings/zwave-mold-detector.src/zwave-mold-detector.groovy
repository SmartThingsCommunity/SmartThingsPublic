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
 *  Date: 2020-07-22
 */

metadata {
	definition(name: "Z-Wave Mold Detector", namespace: "smartthings", author: "SmartThings", mnmn: "SmartThings", vid: "generic-mold", ocfDeviceType: "oic.d.thermostat") {
		capability "Temperature Measurement"
		capability "Relative Humidity Measurement"
		capability "Dew Point"
		capability "Mold Health Concern"
		capability "Battery"
		capability "Sensor"
		capability "Health Check"

		// Aeotec Aerq Temperature and Humidity Sensor
		fingerprint mfr:"0371", prod:"0002", model:"0009", deviceJoinName: "Aeotec Multipurpose Sensor", mnmn: "SmartThings", vid: "aeotec-temp-humidity" //EU 
		fingerprint mfr:"0371", prod:"0102", model:"0009", deviceJoinName: "Aeotec Multipurpose Sensor", mnmn: "SmartThings", vid: "aeotec-temp-humidity" //US
		fingerprint mfr:"0371", prod:"0202", model:"0009", deviceJoinName: "Aeotec Multipurpose Sensor", mnmn: "SmartThings", vid: "aeotec-temp-humidity" //AU
		// POPP Mold Detector
		fingerprint mfr:"0154", prod:"0004", model:"0014", deviceJoinName: "POPP Multipurpose Sensor"	//EU 
	}

	tiles(scale: 2) {
		multiAttributeTile(name: "temperature", type: "generic", width: 6, height: 4, canChangeIcon: true) {
			tileAttribute("device.temperature", key: "PRIMARY_CONTROL") {
				attributeState "temperature", label: '${currentValue}°',
						backgroundColors: [
								[value: 31, color: "#153591"],
								[value: 44, color: "#1e9cbb"],
								[value: 59, color: "#90d2a7"],
								[value: 74, color: "#44b621"],
								[value: 84, color: "#f1d801"],
								[value: 95, color: "#d04e00"],
								[value: 96, color: "#bc2323"]
						]
			}
		}
		valueTile("humidity", "device.humidity", inactiveLabel: false, width: 2, height: 2) {
			state "humidity", label: '${currentValue}% humidity', unit: ""
		}
		valueTile("dewPoint", "device.dewPoint", inactiveLabel: false, width: 2, height: 2) {
			state "dewPoint", label: '${currentValue}° dewPoint', unit: ""
		}
		valueTile("battery", "device.battery", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "battery", label: '${currentValue}% battery', unit: ""
		}
		standardTile("refresh", "device.refresh", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "default", label: "", action: "refresh.refresh", icon: "st.secondary.refresh"
		}

		main "temperature", "humidity", "dewPoint"
		details(["temperature", "humidity", "dewPoint", "battery"])
	}
}

def installed() {
	sendEvent(name: "checkInterval", value: 8 * 60 * 60 + 10 * 60, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])
	// device doesn't send it on inclusion by itslef, so event is needed to populate plugin
	sendEvent(name: "moldHealthConcern", value: "good", displayed: false)

	def cmds = [
		secure(zwave.batteryV1.batteryGet()),
		secure(zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType: 0x05)), // humidity
		secure(zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType: 0x01)), // temperature
		secure(zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType: 0x0B)), // dew point
		secure(zwave.wakeUpV2.wakeUpNoMoreInformation())
	]

	response(cmds)
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

def zwaveEvent(physicalgraph.zwave.commands.wakeupv2.WakeUpIntervalReport cmd) {
	log.debug "Wake Up Interval Report: ${cmd}"
}

def zwaveEvent(physicalgraph.zwave.commands.notificationv3.NotificationReport cmd) {
	log.debug "Event: ${cmd.event}, Notification type: ${cmd.notificationType}"

	def value
	def description

	if (cmd.notificationType == 0x10) {  // Mold Environment Detection
		switch (cmd.event) {
			case 0x00:
				value = "good"
				description = "Mold environment not detected"
				break
			case 0x02:
				value = "unhealthy"
				description = "Mold environment detected"
				break
			default:
				log.warn "Not handled event type for Mold Environment Detection: ${cmd.event}"
				return
		}

		createEvent(name: "moldHealthConcern", value: value, descriptionText: description, isStateChange: true, displayed: true)
	}
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
			map.displayed = true
			map.isStateChange = true
			break
		case 0x05:
			map.name = "humidity"
			map.value = cmd.scaledSensorValue.toInteger()
			map.unit = "%"
			map.displayed = true
			map.isStateChange = true
			break
		case 0x0B:
			map.name = "dewpoint"
			map.unit = temperatureScale
			map.value = convertTemperatureIfNeeded(cmd.scaledSensorValue, cmd.scale == 1 ? "F" : "C", cmd.precision)
			map.displayed = true
			map.isStateChange = true
			break
		default:
			map.descriptionText = cmd.toString()
	}

	createEvent(map)
}

def zwaveEvent(physicalgraph.zwave.commands.wakeupv2.WakeUpNotification cmd) {
	def cmds = []
	def result = createEvent(descriptionText: "$device.displayName woke up", isStateChange: false)

	if (!state.lastbatt || (now() - state.lastbatt) >= 10 * 60 * 60 * 1000) {
		cmds += [
			"delay 1000",
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

private secure(cmd) {
	if (zwaveInfo.zw.contains("s")) {
		zwave.securityV1.securityMessageEncapsulation().encapsulate(cmd).format()
	} else {
		cmd.format()
	}
}
