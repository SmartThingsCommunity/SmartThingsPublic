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
 *  Author: Chris
 *  Date: 2021-01-08
 */

metadata {
	definition(name: "Aeotec aerQ Sensor", namespace: "smartthings", author: "SmartThings", ocfDeviceType: "oic.d.thermostat", minHubCoreVersion: '000.017.0012', executeCommandsLocally: false) {
		capability "Sensor"
		capability "Battery"
		capability "Health Check"
		capability "Temperature Measurement"
		capability "Relative Humidity Measurement"
		capability "Configuration"
        
		attribute "dewpoint", "number"
		attribute "updateNeeded", "string"
		attribute "parameter1", "number"
		attribute "parameter2", "number"
		attribute "parameter4", "number"
        
		fingerprint mfr:"0371", prod:"0002", model:"0009", deviceJoinName: "Temperature Humidity Sensor", mnmn: "SmartThings", vid: "aeotec-water-sensor-7-pro" //EU //aerQ Sensor
		fingerprint mfr:"0371", prod:"0102", model:"0009", deviceJoinName: "Temperature Humidity Sensor", mnmn: "SmartThings", vid: "aeotec-water-sensor-7-pro" //US //aerQ Sensor

	}

	simulator {
    		//These aren't the droids you're looking for.
	}

	tiles(scale: 2) {
		multiAttributeTile(name:"temperature", type:"generic", width:3, height:2, canChangeIcon: true) {
			tileAttribute("device.temperature", key: "PRIMARY_CONTROL") {
				attributeState("temperature", label: '${currentValue}°', icon: "st.alarm.temperature.normal",
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
		
			tileAttribute("device.humidity", key: "SECONDARY_CONTROL") {
					attributeState("humidity", label: '${currentValue}', unit: "%")
			}
        
       		}
	}
        
	tiles(scale: 1) {
		multiAttributeTile(name:"dewpoint", type:"generic", width:3, height:2, canChangeIcon: true) {
			tileAttribute("device.dewpoint", key: "SECONDARY_CONTROL") {
				attributeState("dewpoint", label: '${currentValue}°', icon: "st.alarm.temperature.normal",
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
	}
        
	valueTile("battery", "device.battery", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
		state "battery", label: '${currentValue}% battery', unit: ""
	}

	main "temperature"
	details(["temperature", "humidity", "dewpoint", "battery"])
        
	preferences {
		section {
			input(
				title: "Threshold settings - These settings are checked once per hour, if enough change to temperature or humidity to send a report. Operates at the same time as Periodic reports.",
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
				title: "All configurations will take place after aerQ Sensor has been woken up. You can wait up to an hour or immediately wakeup aerQ by tapping its button.",
				type: "paragraph",
				element: "paragraph"
			)
		}
	}
}


def installed() {
	setCheckInterval()
	def cmds = [
		secure(zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType: 0x01)),
		secure(zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType: 0x05)),
        	secure(zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType: 0x0B)),
		secure(zwave.batteryV1.batteryGet())
	]
	response(cmds)
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
			map.name = "temperature"
			map.value = convertTemperatureIfNeeded(cmd.scaledSensorValue, cmd.scale == 1 ? "F" : "C", cmd.precision)
			map.unit = cmd.scale == 1 ? "F" : "C"
			break;
		case 0x05:
			map.name = "humidity"
			map.value = cmd.scaledSensorValue.toInteger()
			map.unit = "%"
			break
        	case 0x0B:
			map.name = "dewpoint"
			map.value = convertTemperatureIfNeeded(cmd.scaledSensorValue, cmd.scale == 1 ? "F" : "C", cmd.precision)
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
            sendEvent(name: "parameter4", value: cmd.scaledConfigurationValue, displayed: false) 
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
    if (state.parameter1 == thresholdTemperatureValue && state.parameter2 == thresholdHumidityValue && state.parameter4 == periodicReportValue) {
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
