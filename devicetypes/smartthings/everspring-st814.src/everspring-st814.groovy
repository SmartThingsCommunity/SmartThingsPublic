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
 *  Everspring ST814 Temperature/Humidity Sensor
 *
 *  Author: SmartThings
 *  Date: 2017-3-4
 */

metadata {
	definition (name: "Everspring ST814", namespace: "smartthings", author: "SmartThings") {
		capability "Temperature Measurement"
		capability "Relative Humidity Measurement"
		capability "Battery"
		capability "Configuration"
		capability "Sensor"
		capability "Health Check"

		fingerprint mfr:"0060", prod:"0006", model:"0001", deviceJoinName: "Everspring Multipurpose Sensor"
	}

	simulator {
		for( int i = 0; i <= 100; i += 20 ) {
			status "temperature ${i}F": new physicalgraph.zwave.Zwave().sensorMultilevelV2.sensorMultilevelReport(
				scaledSensorValue: i, precision: 1, sensorType: 1, scale: 1).incomingMessage()
		}

		for( int i = 0; i <= 100; i += 20 ) {
			status "humidity ${i}%": new physicalgraph.zwave.Zwave().sensorMultilevelV2.sensorMultilevelReport(
				scaledSensorValue: i, precision: 0, sensorType: 5).incomingMessage()
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
		case 1:
			/* temperature */
			def cmdScale = cmd.scale == 1 ? "F" : "C"
			map.value = convertTemperatureIfNeeded(cmd.scaledSensorValue, cmdScale, cmd.precision)
			map.unit = getTemperatureScale()
			map.name = "temperature"
			break
		case 5:
			/* humidity */
			map.value = cmd.scaledSensorValue.toInteger().toString()
			map.unit = "%"
			map.name = "humidity"
			break
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

def zwaveEvent(physicalgraph.zwave.commands.multichannelv3.MultiChannelCmdEncap cmd) {
	def result = null
	if (cmd.commandClass == 0x6C && cmd.parameter.size >= 4) { // Supervision encapsulated Message
		// Supervision header is 4 bytes long, two bytes dropped here are the latter two bytes of the supervision header
		cmd.parameter = cmd.parameter.drop(2)
		// Updated Command Class/Command now with the remaining bytes
		cmd.commandClass = cmd.parameter[0]
		cmd.command = cmd.parameter[1]
		cmd.parameter = cmd.parameter.drop(2)
	}
	def encapsulatedCommand = cmd.encapsulatedCommand([0x20: 1, 0x31: 2, 0x70: 1, 0x71: 1, 0x80: 1, 0x84: 2, 0x85: 2])
	log.debug ("Command from endpoint ${cmd.sourceEndPoint}: ${encapsulatedCommand}")
	if (encapsulatedCommand) {
		result = zwaveEvent(encapsulatedCommand)
	}
	result
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
		zwave.configurationV1.configurationSet(parameterNumber: 6, size: 2, scaledConfigurationValue: 20).format(),

		// Auto report temperature change threshold
		zwave.configurationV1.configurationSet(parameterNumber: 7, size: 1, scaledConfigurationValue: 2).format(),

		// Auto report humidity change threshold
		zwave.configurationV1.configurationSet(parameterNumber: 8, size: 1, scaledConfigurationValue: 5).format(),

		// Get battery – report triggers WakeUpNMI
		zwave.batteryV1.batteryGet().format()
	]) 
}