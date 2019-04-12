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
 *  Generic Z-Wave Motion/Temp/Light Sensor
 *
 *  Author: SmartThings
 *  Date: 2018-11-05
 */

metadata {
	definition(name: "Z-Wave Motion/Temp/Light Sensor", namespace: "smartthings", author: "SmartThings", mnmn: "Samsung", vid: "generic-trisensor-1", ocfDeviceType: "x.com.st.d.sensor.motion") {
		capability "Motion Sensor"
		capability "Illuminance Measurement"
		capability "Battery"
		capability "Sensor"
		capability "Health Check"
		capability "Temperature Measurement"
		capability "Configuration"

		fingerprint mfr:"0371", prod:"0002", model:"0005", deviceJoinName: "Aeotec TriSensor"	//ZW005-C EU
		fingerprint mfr:"0371", prod:"0102", model:"0005", deviceJoinName: "Aeotec TriSensor" 	//ZW005-A US
	}

	tiles(scale: 2) {
		multiAttributeTile(name: "motion", type: "generic", width: 6, height: 4) {
			tileAttribute("device.motion", key: "PRIMARY_CONTROL") {
				attributeState("active", label: 'motion', icon: "st.motion.motion.active", backgroundColor: "#00A0DC")
				attributeState("inactive", label: 'no motion', icon: "st.motion.motion.inactive", backgroundColor: "#CCCCCC")
			}
		}
		valueTile("battery", "device.battery", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "battery", label: '${currentValue}% battery'
		}
		valueTile("illuminance", "device.illuminance", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state("luminosity", label:'${currentValue} ${unit}', unit:"lux")
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

		main "motion"
		details(["motion", "illuminance", "temperature", "battery"])
	}
}

def installed() {
	response([secure(zwave.batteryV1.batteryGet()),
			  "delay 500",
			  secure(zwave.notificationV3.notificationGet(notificationType: 7)), // motion
			  "delay 500",
			  secure(zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType: 0x01)), // temperature
			  "delay 500",
			  secure(zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType: 0x03, scale: 1)), // illuminance
			  "delay 10000",
			  secure(zwave.wakeUpV2.wakeUpNoMoreInformation())])
}

def updated() {
	configure()
}

def configure() {
	sendEvent(name: "checkInterval", value: 8 * 60 * 60 + 2 * 60, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])
	response(secure(zwave.configurationV1.configurationSet(parameterNumber: 2, size: 2, scaledConfigurationValue: 30)))
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
	if (cmd.notificationType == 0x07) {
		if (cmd.event == 0x08) {
			sensorMotionEvent(1)
		} else if (cmd.event == 0x00) {
			sensorMotionEvent(0)
		}
	}
}

def zwaveEvent(physicalgraph.zwave.commands.sensorbinaryv2.SensorBinaryReport cmd) {
	return sensorMotionEvent(cmd.sensorValue)
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd) {
	return sensorMotionEvent(cmd.value)
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
		case 1:
			map.name = "temperature"
			map.unit = temperatureScale
			map.value = convertTemperatureIfNeeded(cmd.scaledSensorValue, cmd.scale == 1 ? "F" : "C", cmd.precision)
			break
		case 3:
			map.name = "illuminance"
			map.value = cmd.scaledSensorValue.toInteger().toString()
			map.unit = "lux"
			break
		default:
			map.descriptionText = cmd.toString()
	}
	createEvent(map)
}

def zwaveEvent(physicalgraph.zwave.commands.wakeupv2.WakeUpNotification cmd) {
	def cmds = []
	def result = createEvent(descriptionText: "$device.displayName woke up", isStateChange: false)
	cmds += secure(zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType: 3, scale: 1))
	cmds += secure(zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType: 1))
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

def sensorMotionEvent(value) {
	if (value) {
		createEvent(name: "motion", value: "active", descriptionText: "$device.displayName detected motion")
	} else {
		createEvent(name: "motion", value: "inactive", descriptionText: "$device.displayName motion has stopped")
	}
}

private secure(cmd) {
	if(zwaveInfo.zw.endsWith("s")) {
		zwave.securityV1.securityMessageEncapsulation().encapsulate(cmd).format()
	} else {
		cmd.format()
	}
}