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
 *  Z-Wave Temp/Humidity Sensor
 *
 *  Author: Chris
 *  Date: 2021-01-29
 */

metadata {
	definition(name: "Aeotec aerQ Sensor", namespace: "aeotec", author: "Aeotec", ocfDeviceType: "oic.d.thermostat", executeCommandsLocally: false, mnmn: "0ALy", vid: "f680ed17-d31d-3aef-a888-1a7ffbceb1cc") {
		capability "Sensor"
		capability "Battery"
		capability "Health Check"
		capability "Temperature Measurement"
		capability "Relative Humidity Measurement"
		capability "Configuration"
        capability "forgottenreturn26666.dewpointMeasurement"
        
		attribute "dewpoint", "number"
		attribute "updateNeeded", "string"
		attribute "parameter1", "number"
		attribute "parameter2", "number"
		attribute "parameter4", "number"
        attribute "parameter64", "number"
        
		fingerprint mfr:"0371", prod:"0002", model:"0009", deviceJoinName: "Temperature Humidity Sensor", mnmn: "0ALy", vid: "f680ed17-d31d-3aef-a888-1a7ffbceb1cc" //EU //aerQ Sensor
		fingerprint mfr:"0371", prod:"0102", model:"0009", deviceJoinName: "Temperature Humidity Sensor", mnmn: "0ALy", vid: "f680ed17-d31d-3aef-a888-1a7ffbceb1cc" //US //aerQ Sensor

	}

	simulator {
		for (int i = 0; i <= 100; i += 20) {
			status "temperature ${i}F": new physicalgraph.zwave.Zwave().securityV1.securityMessageEncapsulation().encapsulate(
				new physicalgraph.zwave.Zwave().sensorMultilevelV2.sensorMultilevelReport(
					scaledSensorValue: i, precision: 1, sensorType: 1, scale: 1)
			).incomingMessage()
		}

		for (int i = 0; i <= 100; i += 20) {
			status "humidity ${i}%": new physicalgraph.zwave.Zwave().securityV1.securityMessageEncapsulation().encapsulate(
				new physicalgraph.zwave.Zwave().sensorMultilevelV2.sensorMultilevelReport(scaledSensorValue: i, sensorType: 5)
			).incomingMessage()
		}

		for (int i in [0, 5, 10, 15, 50, 99, 100]) {
			status "battery ${i}%": new physicalgraph.zwave.Zwave().securityV1.securityMessageEncapsulation().encapsulate(
				new physicalgraph.zwave.Zwave().batteryV1.batteryReport(batteryLevel: i)
			).incomingMessage()
		}

		status "low battery alert": new physicalgraph.zwave.Zwave().securityV1.securityMessageEncapsulation().encapsulate(
			new physicalgraph.zwave.Zwave().batteryV1.batteryReport(batteryLevel: 255)
		).incomingMessage()

		status "wake up": "command: 8407, payload: "
	}

	tiles(scale: 2) {
		multiAttributeTile(name:"temperature", type:"generic", width:6, height:4, canChangeIcon: true) {
			tileAttribute("device.temperature", key: "PRIMARY_CONTROL") {
				attributeState("temperature", label: '${currentValue}°', 
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
                    			]
				)
			}
        	}
        
        	valueTile("humidity", "device.humidity", inactiveLabel: false, width: 2, height: 2) {
				state "humidity", label: '${currentValue}% humidity', unit: "%"
		}
        
        	valueTile("dewpoint", "device.dewpoint", inactiveLabel: false, width: 2, height: 2) {
			state "dewpoint", label: '${currentValue}°',
				backgroundColors: [
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
				]
		}
        
        	valueTile("battery", "device.battery", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "battery", label: '${currentValue}% battery', unit: ""
		}

		main "temperature"
		details(["temperature", "humidity", "dewpoint", "battery"])
	}
        
	preferences {
		section {
			input(
				title: "Threshold settings - These settings are checked once every 15 minutes, if enough change to temperature or humidity to send a report. Operates at the same time as Periodic reports.",
				type: "paragraph",
				element: "paragraph"
			)
			input(
				title: "1. Min Temperature Report Value:",
				description: "Minimum required temperature change to induce report (value is 1/10 scale).",
				name: "thresholdTemperatureValue",
				type: "number",
				range: "1..100",
				defaultValue: 20
			)
            		input(
				title: "2. Min Humidity Report Value:",
				description: "Minimum required temperature change to induce report.",
				name: "thresholdHumidityValue",
				type: "number",
				range: "1..20",
				defaultValue: 5
			)  
			input(
				title: "Periodic setting - Determines how often both temperature and humidity are reported. This setting operates at the same time as threshold reports.",
				type: "paragraph",
				element: "paragraph"
			)
			input(
				title: "4. Periodic Report:",
				description: "Determines how often temperature and humidity are reported without check requirement.",
				name: "periodicReportValue",
				type: "number",
				range: "900..65535",
				defaultValue: 43200
			)
            		input(
				title: "Temperature Scale setting - This setting will take 1 wake up to set in properly, then the following temperature sensor report after that wakeup will change the temperature unit and value appropriately. If you want to see immediate changes, wake up aerQ Sensor a few times.",
				type: "paragraph",
				element: "paragraph"
			)
            		input(
				title: "64. Temperature Scale:",
				description: "Set the temperature scale unit report in C or F (US defaults to F, EU defaults to C)",
				name: "temperatureScaleSetting",
				type: "number",
				range: "1..2",
			)
			input(
				title: "PARAMETERS END - All configurations will take place after aerQ Sensor has been woken up. You can wait up to 12 hours or immediately wakeup aerQ by tapping its button.",
				type: "paragraph",
				element: "paragraph"
			)
            		input(
				title: "OFFSET Sensors - Use these settings below to offset humidity or temperature. The values will change upon the next temperature or humidity report.",
				type: "paragraph",
				element: "paragraph"
			)
            		input(
				title: "a. Temperature offset:",
				description: "Offset the aerQ reported value for temperature.",
				name: "offsetTemperature",
				type: "decimal",
				defaultValue: 0
			)
            		input(
				title: "b. Humidity offset:",
				description: "Offset the aerQ reported value for humidity.",
				name: "offsetHumidity",
				type: "number",
				defaultValue: 0
			)
		}
	}
}


def installed() {
	setCheckInterval()
	def cmds = [
		secure(zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType: 0x0B)),
		secure(zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType: 0x01)),
		secure(zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType: 0x05)),
		secure(zwave.batteryV1.batteryGet())
	]
	response(delayBetween(cmds, 1500))
}

def updated() {
	def cmds = []
	cmds << sendEvent(name: "updateNeeded", value: "true", displayed: false) 
	response(cmds)
}

private setCheckInterval() {
	sendEvent(name: "checkInterval", value: (2 * 12 + 2) * 60 * 60, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])
}

private getCommandClassVersions() {
	[0x20: 1, 0x30: 1, 0x31: 5, 0x80: 1, 0x84: 1, 0x70: 2, 0x71: 3, 0x9C: 1]
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

def zwaveEvent(physicalgraph.zwave.commands.wakeupv1.WakeUpNotification cmd) {
	def result = [createEvent(descriptionText: "${device.displayName} woke up", isStateChange: false)]
    
	if (device.currentValue("updateNeeded") == "true") {
		if (thresholdTemperatureValue != state.parameter1 && thresholdTemperatureValue) {
			result << response(secure(zwave.configurationV1.configurationSet(parameterNumber: 1, size: 1, scaledConfigurationValue: thresholdTemperatureValue)))
			result << response(secure(zwave.configurationV1.configurationGet(parameterNumber: 1)))
		}
		if (thresholdHumidityValue != state.parameter2 && thresholdHumidityValue) {
			result << response(secure(zwave.configurationV1.configurationSet(parameterNumber: 2, size: 1, scaledConfigurationValue: thresholdHumidityValue)))
			result << response(secure(zwave.configurationV1.configurationGet(parameterNumber: 2)))
		}
		if (periodicReportValue != state.parameter4 && periodicReportValue) {
			result << response(secure(zwave.configurationV1.configurationSet(parameterNumber: 4, size: 2, scaledConfigurationValue: periodicReportValue)))
			result << response(secure(zwave.configurationV1.configurationGet(parameterNumber: 4)))
		}  
        	if (temperatureScaleSetting != state.parameter64 && temperatureScaleSetting) {
        		result << response(secure(zwave.configurationV1.configurationSet(parameterNumber: 64, size: 1, scaledConfigurationValue: temperatureScaleSetting)))
			result << response(secure(zwave.configurationV1.configurationGet(parameterNumber: 64)))
        	}
	}

	if (!state.lastbat || (new Date().time) - state.lastbat > 53 * 60 * 60 * 1000) {
		result << response(secure(zwave.batteryV1.batteryGet()))
	}
	result << response(secure(zwave.wakeUpV1.wakeUpNoMoreInformation()))
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
	[createEvent(map), response(secure(zwave.wakeUpV1.wakeUpNoMoreInformation()))]
}

def zwaveEvent(physicalgraph.zwave.commands.sensormultilevelv5.SensorMultilevelReport cmd) {
	def map = [:]
	switch (cmd.sensorType) {
		case 0x01:
			def finalTempValue 
			if (offsetTemperature){
				finalTempValue = cmd.scaledSensorValue
				finalTempValue = finalTempValue + offsetTemperature
			} else {
				finalTempValue = cmd.scaledSensorValue
			}
			map.name = "temperature"
			map.value = finalTempValue
			map.unit = cmd.scale == 1 ? "F" : "C"
		break;
		case 0x05:
			def finalHumValue
			if (offsetHumidity) {
				finalHumValue = cmd.scaledSensorValue.toInteger() + offsetHumidity.toInteger()
			} else {
				finalHumValue = cmd.scaledSensorValue.toInteger()
			}
			map.name = "humidity"
			map.value = finalHumValue
			map.unit = "%"
			break
		case 0x0B:
			map.name = "dewpoint"
			map.value = cmd.scaledSensorValue
			map.unit = cmd.scale == 1 ? "F" : "C"
			break
		default:
			map.descriptionText = cmd.toString()
			break
	}
	createEvent(map)
}

def zwaveEvent(physicalgraph.zwave.commands.configurationv2.ConfigurationReport cmd) {
	switch (cmd.parameterNumber) {
		case 0x01:
			state.parameter1 = cmd.scaledConfigurationValue
			sendEvent(name: "parameter1", value: cmd.scaledConfigurationValue, displayed: false) 
			break
		case 0x02:
			state.parameter2 = cmd.scaledConfigurationValue
			sendEvent(name: "parameter2", value: cmd.scaledConfigurationValue, displayed: false) 
			break
		case 0x04:
			state.parameter4 = cmd.scaledConfigurationValue
			if(state.parameter4 < 0) { 
				state.parameter4 = state.parameter4 + 65536 
			}
			sendEvent(name: "parameter4", value: state.parameter4, displayed: false) 
			break
		case 0x40:
			state.parameter64 = cmd.scaledConfigurationValue
			sendEvent(name: "parameter64", value: state.parameter64, displayed: false) 
			break
		default:
			log.debug "Setting unknown parameter"
			break
	}
    
	checkParameterValues()
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
	log.warn "Unhandled command: ${cmd}"
	createEvent(descriptionText: "$device.displayName: $cmd", displayed: false)
}

def checkParameterValues() {
	//if parameter settings fail somehow, wakeup can cause parameter settings to update again the next time. When all settings are true, then stop parameter updates the next time. 
	if (state.parameter1 == thresholdTemperatureValue && state.parameter2 == thresholdHumidityValue && state.parameter4 == periodicReportValue && state.parameter64 == temperatureScaleSetting) {
		sendEvent(name: "updateNeeded", value: "false", displayed: false) 
	} 
}

private secure(cmd) {
	if (zwaveInfo.zw.contains("s")) {
		zwave.securityV1.securityMessageEncapsulation().encapsulate(cmd).format()
	} else {
		cmd.format()
	}
}