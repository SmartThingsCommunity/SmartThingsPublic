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

		fingerprint mfr: "027A", prod: "2021", model: "2101", deviceJoinName: "Zooz Multipurpose Sensor" // Zooz 4-in-1 sensor
		fingerprint mfr: "0109", prod: "2021", model: "2101", deviceJoinName: "Vision Multipurpose Sensor" // ZP3111US 4-in-1 Motion
		fingerprint mfr: "0060", prod: "0001", model: "0004", deviceJoinName: "Everspring Motion Sensor", mnmn: "SmartThings", vid: "SmartThings-smartthings-Everspring_Multisensor" // Everspring Immune Pet PIR Sensor SP815
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

	preferences {
		section {
			input(
					title: "Settings Available For Everspring SP815 only",
					description: "To apply updated device settings to the device press the learn key on the device three times or check the device manual.",
					type: "paragraph",
					element: "paragraph"
			)
			input(
					title: "Temperature and Humidity Auto Report (Everspring SP815 only):",
					description: "This setting allows to adjusts report time (in seconds) of temperature and humidity report.",
					name: "temperatureAndHumidityReport",
					type: "number",
					range: "600..1440",
					defaultValue: 600
			)
			input(
					title: "Re-trigger Interval Setting (Everspring SP815 only):",
					description: "The setting adjusts the sleep period (in seconds) after the detector has been triggered. No response will be made during this interval if a movement is presented. Longer re-trigger interval will result in longer battery life.",
					name: "retriggerIntervalSettings",
					type: "number",
					range: "10..3600",
					defaultValue: 180
			)
		}
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
	getConfigurationCommands()
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

	log.debug "isConfigured: $state.configured"
	if (isEverspringSP815() && !state.configured) {
		results += lateConfigure()
	}

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
	if (isEverspringSP815()) {
		state.configured = false
		state.intervalConfigured = false
		state.temperatureConfigured = false
	}
	def request = []
	request << zwave.batteryV1.batteryGet()
	request << zwave.notificationV3.notificationGet(notificationType: 0x07, event: 0x08)  //motion
	request << zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType: 0x01) //temperature
	request << zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType: 0x05) //humidity
	if (isEverspringSP815()) {
		request += getConfigurationCommands()
	} else {
		request << zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType: 0x03) //illuminance
	}

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

def getConfigurationCommands() {
	log.debug "getConfigurationCommands"
	def result = []

	if (isEverspringSP815()) {
		Integer temperatureAndHumidityReport = (settings.temperatureAndHumidityReport as Integer) ?: everspringDefaults[1]
		Integer retriggerIntervalSettings = (settings.retriggerIntervalSettings as Integer) ?: everspringDefaults[2]

		if (!state.temperatureAndHumidityReport) {
			state.temperatureAndHumidityReport = getEverspringDefaults[1]
		}
		if (!state.retriggerIntervalSettings) {
			state.retriggerIntervalSettings = getEverspringDefaults[2]
		}

		if (!state.configured || (temperatureAndHumidityReport != state.temperatureAndHumidityReport || retriggerIntervalSettings != state.retriggerIntervalSettings)) {
			state.configured = false // this flag needs to be set to false when settings are changed (and the device was initially configured before)

			if (!state.temperatureConfigured || temperatureAndHumidityReport != state.temperatureAndHumidityReport) {
				state.temperatureConfigured = false
				result << zwave.configurationV2.configurationSet(parameterNumber: 1, size: 2, scaledConfigurationValue: temperatureAndHumidityReport)
				result << zwave.configurationV2.configurationGet(parameterNumber: 1)
			}
			if (!state.intervalConfigured || retriggerIntervalSettings != state.retriggerIntervalSettings) {
				state.intervalConfigured = false
				result << zwave.configurationV2.configurationSet(parameterNumber: 2, size: 2, scaledConfigurationValue: retriggerIntervalSettings)
				result << zwave.configurationV2.configurationGet(parameterNumber: 2)
			}
		}
	}

	return result
}

def getEverspringDefaults() {
	[1: 600,
	 2: 180]
}

def lateConfigure() {
	log.debug "lateConfigure"
	sendHubCommand(getConfigurationCommands(), 200)
}

def zwaveEvent(physicalgraph.zwave.commands.configurationv2.ConfigurationReport cmd) {
	if (isEverspringSP815()) {
		if (cmd.parameterNumber == 1) {
			state.temperatureAndHumidityReport = scaledConfigurationValue
			state.temperatureConfigured = true
		} else if (cmd.parameterNumber == 2) {
			state.retriggerIntervalSettings = scaledConfigurationValue
			state.intervalConfigured = true
		}

		if (state.intervalConfigured && state.temperatureConfigured) {
			state.configured = true
		}
		log.debug "Everspring Configuration Report: ${cmd}"
	}

	return [:]
}

private isEverspringSP815() {
	zwaveInfo?.mfr?.equals("0060") && zwaveInfo?.model?.equals("0004")
}
