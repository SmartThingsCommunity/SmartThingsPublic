/**
 *  Copyright 2017 SmartThings
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
 *  Everspring ST815 Illuminance Sensor
 *
 *  Author: SmartThings
 *  Date: 2017-3-4
 */

metadata {
	definition (name: "Everspring Illuminance Sensor", namespace: "smartthings", author: "SmartThings") {
		capability "Illuminance Measurement"
		capability "Battery"
		capability "Configuration"
		capability "Sensor"
		capability "Health Check"

		fingerprint mfr:"0060", prod:"0007", model:"0001"
	}

	simulator {
		for( int i = 0; i <= 100; i += 20 ) {
			status "illuminace ${i} lux": new physicalgraph.zwave.Zwave().sensorMultilevelV2.sensorMultilevelReport(
				scaledSensorValue: i, precision: 0, sensorType: 3, scale: 1).incomingMessage()
		}

		for( int i = 0; i <= 100; i += 20 ) {
			status "battery ${i}%": new physicalgraph.zwave.Zwave().batteryV1.batteryReport(
				batteryLevel: i).incomingMessage()
		}

		status "wakeup":  "command: 8407, payload: "
	}

	tiles(scale: 2) {
		valueTile("temperature", "device.temperature", inactiveLabel: false, width: 2, height: 2) {
			state "temperature", label:'${currentValue}°',
			backgroundColors:[
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
			state "humidity", label:'${currentValue}% humidity', unit:""
		}
		valueTile("battery", "device.battery", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "battery", label:'${currentValue}% battery', unit:""
		}

		main( ["temperature", "humidity"] )
		details( ["temperature", "humidity", "battery"] )
	}
}

def updated() {
	state.configured = false
}

def parse(String description) {
	def result = []

	def cmd = zwave.parse(description, [0x20: 1, 0x31: 2, 0x70: 1, 0x71: 1, 0x80: 1, 0x84: 2, 0x85: 2])

	if (cmd) {
		result = zwaveEvent(cmd)
	}

	if (result instanceof List) {
		log.debug "Parsed '$description' to ${result.collect { it.respondsTo("toHubAction") ? it.toHubAction() : it }}"
	} else {
		log.debug "Parsed '$description' to ${result}"
	}
	return result
}

def zwaveEvent(physicalgraph.zwave.commands.wakeupv2.WakeUpNotification cmd) {
	def result = [
		createEvent(descriptionText: "${device.displayName} woke up", isStateChange: false)
	]
	if (state.configured) {
		result << response(zwave.batteryV1.batteryGet())
	} else {
		result << response(configure())
	}
	return result
}

def zwaveEvent(physicalgraph.zwave.commands.alarmv1.AlarmReport cmd) {
	if (cmd.alarmType == 1 && cmd.alarmType == 0xFF) {
		return createEvent(descriptionText: "${device.displayName} battery is low", isStateChange: true)
	} else if (cmd.alarmType == 2 && cmd.alarmLevel == 1) {
		return createEvent(descriptionText: "${device.displayName} powered up", isStateChange: false)
	}
}

def zwaveEvent(physicalgraph.zwave.commands.sensormultilevelv2.SensorMultilevelReport cmd) {

	def map = [:]
	switch( cmd.sensorType ) {
		case 3:
			// luminance
			map.value = cmd.scaledSensorValue.toInteger().toString()
			map.unit = "lux"
			map.name = "illuminance"
			break;
	}
	
	return createEvent(map)
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

	def response_cmds  = []
	if (!currentTemperature) {
		response_cmds << zwave.sensorMultilevelV2.sensorMultilevelGet().format()
		response_cmds << "delay 1000"
	}
	response_cmds << zwave.wakeUpV1.wakeUpNoMoreInformation().format()

	return [createEvent(map), response(response_cmds)]
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
	log.debug "Unhandled: ${cmd.toString()}"
	return [:]
}

def configure() {
	state.configured = true
	sendEvent(name: "checkInterval", value: 8 * 60 * 60 + 2 * 60, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])
	delayBetween([
		// Auto report time interval in minutes
		zwave.configurationV1.configurationSet(parameterNumber: 5, size: 2, scaledConfigurationValue: 20).format(),

		// Auto report lux change threshold
		zwave.configurationV1.configurationSet(parameterNumber: 6, size: 2, scaledConfigurationValue: 30).format(),

		// Get battery – report triggers WakeUpNMI
		zwave.batteryV1.batteryGet().format()
	]) 
}