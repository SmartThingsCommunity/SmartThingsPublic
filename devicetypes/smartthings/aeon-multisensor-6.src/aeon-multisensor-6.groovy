/**
 *  Copyright 2015 SmartThings
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
	definition(name: "Aeon Multisensor 6", namespace: "smartthings", author: "SmartThings", runLocally: true, minHubCoreVersion: '000.020.00008', executeCommandsLocally: true, ocfDeviceType: "x.com.st.d.sensor.motion") {
		capability "Motion Sensor"
		capability "Temperature Measurement"
		capability "Relative Humidity Measurement"
		capability "Illuminance Measurement"
		capability "Ultraviolet Index"
		capability "Configuration"
		capability "Sensor"
		capability "Battery"
		capability "Health Check"
		capability "Power Source"
		capability "Tamper Alert"

		attribute "batteryStatus", "string"

		fingerprint deviceId: "0x2101", inClusters: "0x5E,0x86,0x72,0x59,0x85,0x73,0x71,0x84,0x80,0x30,0x31,0x70,0x7A", outClusters: "0x5A", deviceJoinName: "Aeon Multipurpose Sensor"
		fingerprint deviceId: "0x2101", inClusters: "0x5E,0x86,0x72,0x59,0x85,0x73,0x71,0x84,0x80,0x30,0x31,0x70,0x7A,0x5A", deviceJoinName: "Aeon Multipurpose Sensor"
		fingerprint mfr: "0086", prod: "0002", model: "0064", deviceJoinName: "Aeotec Multipurpose Sensor" //EU //Aeotec MultiSensor 6
		fingerprint mfr: "0086", prod: "0102", model: "0064", deviceJoinName: "Aeotec Multipurpose Sensor" //US //Aeotec MultiSensor 6
		fingerprint mfr: "0086", prod: "0202", model: "0064", deviceJoinName: "Aeotec Multipurpose Sensor" //AU //Aeotec MultiSensor 6
		fingerprint mfr: "0371", prod: "0002", model: "0018", deviceJoinName: "Aeotec Multipurpose Sensor" //Aeotec MultiSensor 7 (EU)
		fingerprint mfr: "0371", prod: "0102", model: "0018", deviceJoinName: "Aeotec Multipurpose Sensor" //Aeotec MultiSensor 7 (US)
		fingerprint mfr: "0371", prod: "0202", model: "0018", deviceJoinName: "Aeotec Multipurpose Sensor" //Aeotec MultiSensor 7 (AU)
	}

	simulator {
		status "no motion": "command: 9881, payload: 00300300"
		status "motion": "command: 9881, payload: 003003FF"

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

		for (int i in [0, 20, 89, 100, 200, 500, 1000]) {
			status "illuminance ${i} lux": new physicalgraph.zwave.Zwave().securityV1.securityMessageEncapsulation().encapsulate(
				new physicalgraph.zwave.Zwave().sensorMultilevelV2.sensorMultilevelReport(scaledSensorValue: i, sensorType: 3)
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

	preferences {
		input "motionDelayTime", "enum", title: "Motion Sensor Delay Time",
			options: ["20 seconds", "30 seconds", "40 seconds", "1 minute", "2 minutes", "3 minutes", "4 minutes"]

		input "motionSensitivity", "enum", title: "Motion Sensor Sensitivity", options: ["maximum", "normal", "minimum", "disabled"]

		input "reportInterval", "enum", title: "Report Interval", description: "How often the device should report in minutes",
			options: ["1 minute", "2 minutes", "3 minutes", "4 minutes", "8 minutes", "15 minutes", "30 minutes", "1 hour", "6 hours", "12 hours", "18 hours", "24 hours"]
	}

	tiles(scale: 2) {
		multiAttributeTile(name: "motion", type: "generic", width: 6, height: 4) {
			tileAttribute("device.motion", key: "PRIMARY_CONTROL") {
				attributeState "active", label: 'motion', icon: "st.motion.motion.active", backgroundColor: "#00A0DC"
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
			state "illuminance", label: '${currentValue} lux', unit: ""
		}

		valueTile("ultravioletIndex", "device.ultravioletIndex", inactiveLabel: false, width: 2, height: 2) {
			state "ultravioletIndex", label: '${currentValue} UV index', unit: ""
		}

		valueTile("battery", "device.battery", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "battery", label: '${currentValue}% battery', unit: ""
		}

		valueTile("batteryStatus", "device.batteryStatus", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "batteryStatus", label: '${currentValue}', unit: ""
		}

		valueTile("powerSource", "device.powerSource", height: 2, width: 2, decoration: "flat") {
			state "powerSource", label: '${currentValue} powered', backgroundColor: "#ffffff"
		}
		valueTile("tamper", "device.tamper", height: 2, width: 2, decoration: "flat") {
			state "clear", label: 'tamper clear', backgroundColor: "#ffffff"
			state "detected", label: 'tampered', backgroundColor: "#ff0000"
		}

		main(["motion", "temperature", "humidity", "illuminance", "ultravioletIndex"])
		details(["motion", "temperature", "humidity", "illuminance", "ultravioletIndex", "batteryStatus", "tamper"])
	}
}

def installed() {
// Device-Watch simply pings if no device events received for 122min(checkInterval)
	sendEvent(name: "checkInterval", value: 2 * 60 * 60 + 2 * 60, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])

	sendEvent(name: "tamper", value: "clear", displayed: false)
}

def updated() {

	/* Since battery is handled locally but we use this batteryStatus tile, we need a good periodic way to keep the
		value updated when on battery power. Ideally this would be done in the local handler, but this is a decent
		stopgap.
	 */
	if (device.latestValue("powerSource") == "battery") {
		sendEvent(name: "batteryStatus", value: "${device.latestValue("battery")}% battery", displayed: false)
	}

	log.debug "Updated with settings: ${settings}"

	if (!getDataValue("configured")) { // this is the update call made after install, device is still awake
		response(configure())
	} else if (device.latestValue("powerSource") == "battery") {
		setConfigured("false")
		//wait until the next time device wakeup to send configure command after user change preference
	} else { // We haven't identified the power supply, or the power supply is USB, so configure
		setConfigured("false")
		response(configure())
	}
}

def parse(String description) {
	def result = null
	if (description.startsWith("Err 106")) {
		log.debug "parse() >> Err 106"
		result = createEvent(name: "secureInclusion", value: "failed", isStateChange: true,
			descriptionText: "This sensor failed to complete the network security key exchange. If you are unable to control it via SmartThings, you must remove it from your network and add it again.")
	} else if (description != "updated") {
		log.debug "parse() >> zwave.parse(description)"

		def cmd = zwave.parse(description, [0x31: 5, 0x30: 2, 0x84: 1])
		if (cmd) {
			result = zwaveEvent(cmd)
		}
	}
	log.debug "After zwaveEvent(cmd) >> Parsed '${description}' to ${result.inspect()}"
	return result
}

//this notification will be sent only when device is battery powered
def zwaveEvent(physicalgraph.zwave.commands.wakeupv1.WakeUpNotification cmd) {
	def result = [createEvent(descriptionText: "${device.displayName} woke up", isStateChange: false)]
	def cmds = []
	if (!isConfigured()) {
		log.debug("late configure")
		result << response(configure())
	} else {
		log.debug("Device has been configured sending >> wakeUpNoMoreInformation()")
		if (isAeotecMultisensor7()) cmds << zwave.configurationV1.configurationGet(parameterNumber: 10).format()
		cmds << zwave.wakeUpV1.wakeUpNoMoreInformation().format()
		result << response(cmds)
	}
	result
}

def zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityMessageEncapsulation cmd) {
	state.sec = 1
	def result = []
	//we need to catch payload so short that it does not contain configuration parameter size (NullPointerException)
	//and actual size smaller than indicated by configuration parameter size (IndexOutOfBoundsException)
	if (cmd.payload[1] == 0x70 && cmd.payload[2] == 0x06 && (cmd.payload.size() < 5 || cmd.payload.size < 5 + cmd.payload[4])) {
		log.debug "Configuration Report command for parameter ${cmd.payload[3]} returned by the device is too short. Retry."
		sendHubCommand(command(zwave.configurationV1.configurationGet(parameterNumber: cmd.payload[3])))
	} else {
		def encapsulatedCommand = cmd.encapsulatedCommand([0x31: 5, 0x30: 2, 0x84: 1])
		log.debug "encapsulated: ${encapsulatedCommand}"
		if (encapsulatedCommand) {
			result = zwaveEvent(encapsulatedCommand)
		} else {
			log.warn "Unable to extract encapsulated cmd from $cmd"
			result = createEvent(descriptionText: cmd.toString())
		}
	}
	result
}

def zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityCommandsSupportedReport cmd) {
	log.info "Executing zwaveEvent 98 (SecurityV1): 03 (SecurityCommandsSupportedReport) with cmd: $cmd"
	state.sec = 1
}

def zwaveEvent(physicalgraph.zwave.commands.securityv1.NetworkKeyVerify cmd) {
	state.sec = 1
	log.info "Executing zwaveEvent 98 (SecurityV1): 07 (NetworkKeyVerify) with cmd: $cmd (node is securely included)"
	def result = [createEvent(name: "secureInclusion", value: "success", descriptionText: "Secure inclusion was successful", isStateChange: true)]
	result
}

def zwaveEvent(physicalgraph.zwave.commands.manufacturerspecificv2.ManufacturerSpecificReport cmd) {
	log.info "Executing zwaveEvent 72 (ManufacturerSpecificV2) : 05 (ManufacturerSpecificReport) with cmd: $cmd"
	log.debug "manufacturerId:   ${cmd.manufacturerId}"
	log.debug "manufacturerName: ${cmd.manufacturerName}"
	log.debug "productId:        ${cmd.productId}"
	log.debug "productTypeId:    ${cmd.productTypeId}"
	def msr = String.format("%04X-%04X-%04X", cmd.manufacturerId, cmd.productTypeId, cmd.productId)
	updateDataValue("MSR", msr)
}

def zwaveEvent(physicalgraph.zwave.commands.batteryv1.BatteryReport cmd) {
	def result = []
	def map = [name: "battery", unit: "%"]
	if (cmd.batteryLevel == 0xFF) {
		map.value = 1
		map.descriptionText = "${device.displayName} battery is low"
		map.isStateChange = true
	} else {
		map.value = cmd.batteryLevel
	}
	state.lastbatt = now()
	result << createEvent(map)
	if (device.latestValue("powerSource") != "dc") {
		result << createEvent(name: "batteryStatus", value: "${map.value}% battery", displayed: false)
	}
	result
}

def zwaveEvent(physicalgraph.zwave.commands.sensormultilevelv5.SensorMultilevelReport cmd) {
	def map = [:]
	switch (cmd.sensorType) {
		case 1:
			map.name = "temperature"
			def cmdScale = cmd.scale == 1 ? "F" : "C"
			map.value = convertTemperatureIfNeeded(cmd.scaledSensorValue, cmdScale, cmd.precision)
			map.unit = getTemperatureScale()
			break
		case 3:
			map.name = "illuminance"
			map.value = cmd.scaledSensorValue.toInteger()
			map.unit = "lux"
			break
		case 5:
			map.name = "humidity"
			map.value = cmd.scaledSensorValue.toInteger()
			map.unit = "%"
			break
		case 0x1B:
			map.name = "ultravioletIndex"
			map.value = cmd.scaledSensorValue.toInteger()
			break
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

def zwaveEvent(physicalgraph.zwave.commands.sensorbinaryv2.SensorBinaryReport cmd) {
	motionEvent(cmd.sensorValue)
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicSet cmd) {
	motionEvent(cmd.value)
}

def clearTamper() {
	sendEvent(name: "tamper", value: "clear")
}

def zwaveEvent(physicalgraph.zwave.commands.notificationv3.NotificationReport cmd) {
	def result = []
	if (cmd.notificationType == 7) {
		switch (cmd.event) {
			case 0:
				result << motionEvent(0)
				result << createEvent(name: "tamper", value: "clear")
				break
			case 3:
			case 9:
				result << createEvent(name: "tamper", value: "detected", descriptionText: "$device.displayName was tampered")
				// Clear the tamper alert after 10s. This is a temporary fix for the tamper attribute until local execution handles it
				unschedule(clearTamper, [forceForLocallyExecuting: true])
				runIn(10, clearTamper, [forceForLocallyExecuting: true])
				break
			case 7:
			case 8:
				result << motionEvent(1)
				break
		}
	} else if (cmd.notificationType == 8) {
		switch (cmd.event) {
			case 2:
				result << createEvent(name: "powerSource", value: "battery", displayed: false)
				break
			case 3:
				result << createEvent(name: "powerSource", value: "dc", displayed: false)
				break
		}
	} else {
		log.warn "Need to handle this cmd.notificationType: ${cmd.notificationType}"
		result << createEvent(descriptionText: cmd.toString(), isStateChange: false)
	}
	result
}

def zwaveEvent(physicalgraph.zwave.commands.configurationv2.ConfigurationReport cmd) {
	log.debug "ConfigurationReport: $cmd"
	def result = []
	def value
	if (isAeotecMultisensor7() && cmd.parameterNumber == 10) {
		value = cmd.scaledConfigurationValue ? "dc" : "battery"
		result << createEvent(name: "powerSource", value: value, displayed: false)
	} else if (cmd.parameterNumber == 9) {
		if (cmd.configurationValue[0] == 0) {
			value = "dc"
			if (!isConfigured()) {
				log.debug("ConfigurationReport: configuring device")
				result << response(configure())
			}
			result << createEvent(name: "batteryStatus", value: "USB Cable", displayed: false)
			result << createEvent(name: "powerSource", value: value, displayed: false)
		} else if (cmd.configurationValue[0] == 1) {
			result << createEvent(name: "powerSource", value: "battery", displayed: false)
			result << createEvent(name: "batteryStatus", value: "${device.latestValue("battery")}% battery", displayed: false)
		}
	} else {
		if (cmd.parameterNumber == 4) {
			//received response to last command in configure() - configuration is complete
			setConfigured("true")
		}
		updateDataValuesForDebugging(cmd.parameterNumber, cmd.scaledConfigurationValue)
	}
	result
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
	log.debug "General zwaveEvent cmd: ${cmd}"
	createEvent(descriptionText: cmd.toString(), isStateChange: false)
}

/**
 * PING is used by Device-Watch in attempt to reach the Device
 * */
def ping() {
	if (device.latestValue("powerSource") == "battery") {
		log.debug "Can't ping a wakeup device on battery"
	} else {
		//dc or unknown - get sensor report
		command(zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType: 0x01)) //poll the temperature to ping
	}
}

def configure() {
	// This sensor joins as a secure device if you double-click the button to include it
	log.debug "${device.displayName} is configuring its settings"

	def request = []
	
	//0. added as MSR wasn't getting detected upon pair. 
	request << zwave.manufacturerSpecificV2.manufacturerSpecificGet()
	//1. set association groups for hub - 2 groups are used to set battery refresh interval different than sensor report interval
	request << zwave.associationV1.associationSet(groupingIdentifier: 1, nodeId: zwaveHubNodeId)

	//2. automatic report flags
	// param 101 -103 [4 bytes] 128: light sensor, 64 humidity, 32 temperature sensor, 15 ultraviolet sensor, 1 battery sensor
	// set value  241 (default for 101) to get all reports. Set value 0 for no reports (default for 102-103)
	//association group 1
	request << zwave.configurationV1.configurationSet(parameterNumber: 101, size: 1, scaledConfigurationValue: 240)

	//association group 2
	request << zwave.configurationV1.configurationSet(parameterNumber: 102, size: 1, scaledConfigurationValue: 1)

	// Expedite this if we know this info so that we can execute the code below
	if (!state.MSR && zwaveInfo?.mfr && zwaveInfo.prod && zwaveInfo.model) {
		state.MSR = "${zwaveInfo.mfr}-${zwaveInfo.prod}-${zwaveInfo.model}"
	}

	switch (state.MSR) {
		case "0086-0002-0064":  // MultiSensor 6 EU
		case "0086-0102-0064":  // MultiSensor 6 US
		case "0086-0202-0064":  // MultiSensor 6 AU
					//3. no-motion report x seconds after motion stops (default 20 secs)
					request << zwave.configurationV1.configurationSet(parameterNumber: 3, size: 2, scaledConfigurationValue: timeOptionValueMap[motionDelayTime] ?: 20)

					//4. motionSensitivity 3 levels: 3-normal, 5-maximum (default), 1-minimum, 0 - disabled
					request << zwave.configurationV1.configurationSet(parameterNumber: 4, size: 1,
						configurationValue:
								motionSensitivity == "normal" ? [3] :
									motionSensitivity == "minimum" ? [1] :
											motionSensitivity == "disabled" ? [0] : [5])

					//5. Parameters 111-113: report interval for association group 1-3
					//association group 1 - set in preferences, default 8 mins
					request << zwave.configurationV1.configurationSet(parameterNumber: 111, size: 4, scaledConfigurationValue: timeOptionValueMap[reportInterval] ?: (8 * 60))

					//association group 2 - report battery every 6 hours
					request << zwave.configurationV1.configurationSet(parameterNumber: 112, size: 4, scaledConfigurationValue: 6 * 60 * 60)
			break
		case "0371-0002-0018":  // MultiSensor 7 EU
		case "0371-0102-0018":  // MultiSensor 7 US
		case "0371-0202-0018":  // MultiSensor 7 AU
					//3. no-motion report x seconds after motion stops (default 30 secs)
					request << zwave.configurationV1.configurationSet(parameterNumber: 3, size: 2, scaledConfigurationValue: timeOptionValueMap[motionDelayTime] ?: 30)

					//4. motionSensitivity 3 levels: 6-normal, 11-maximum (default), 1-minimum, 0 - disabled
					request << zwave.configurationV1.configurationSet(parameterNumber: 4, size: 1,
						configurationValue:
								motionSensitivity == "normal" ? [6] :
									motionSensitivity == "minimum" ? [1] :
											motionSensitivity == "disabled" ? [0] : [11])

					//5. Parameters 111-113: report interval for association group 1-3
					//association group 1 - set in preferences, default 8 mins
					request << zwave.configurationV1.configurationSet(parameterNumber: 111, size: 2, scaledConfigurationValue: timeOptionValueMap[reportInterval] ?: (8 * 60))

					//association group 2 - report battery every 6 hours
					request << zwave.configurationV1.configurationSet(parameterNumber: 112, size: 2, scaledConfigurationValue: 6 * 60 * 60)
			break
	}
	
	//6. report automatically ONLY on threshold change
	//From manual:
	//Enable/disable the selective reporting only when measurements reach a certain threshold or percentage set in 41-44.
	//This is used to reduce network traffic.  (0 = disable, 1 = enable)
	//Note: If USB power, the Sensor will check the threshold every 10 seconds. If battery power, the Sensor will check the threshold
	//when it is waken up.
	request << zwave.configurationV1.configurationSet(parameterNumber: 40, size: 1, scaledConfigurationValue: 1)
	
	//7. query sensor data
	request << zwave.batteryV1.batteryGet()
	request << zwave.sensorBinaryV2.sensorBinaryGet(sensorType: 0x0C) //motion
	request << zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType: 0x01) //temperature
	request << zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType: 0x03) //illuminance
	request << zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType: 0x05) //humidity
	request << zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType: 0x1B) //ultravioletIndex

	//8. query configuration
	request << zwave.configurationV1.configurationGet(parameterNumber: 9)
	request << zwave.configurationV1.configurationGet(parameterNumber: 10)
	request << zwave.configurationV1.configurationGet(parameterNumber: 101)
	request << zwave.configurationV1.configurationGet(parameterNumber: 102)
	request << zwave.configurationV1.configurationGet(parameterNumber: 111)
	request << zwave.configurationV1.configurationGet(parameterNumber: 112)
	request << zwave.configurationV1.configurationGet(parameterNumber: 40)
	//Last parameter number is important, as we set configuration completion flag when we receive response to this get command
	request << zwave.configurationV1.configurationGet(parameterNumber: 4)

	// set the check interval based on the report interval preference. (default 122 minutes)
	// we do this here in case the device is in wakeup mode
	def checkInterval = 2 * (timeOptionValueMap[reportInterval] ?: 60 * 60) + 2 * 60
	sendEvent(name: "checkInterval", value: checkInterval, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])

	commands(request, (state.sec || zwaveInfo?.zw?.contains("s")) ? 2000 : 500) + ["delay 20000", zwave.wakeUpV1.wakeUpNoMoreInformation().format()]
}


private def getTimeOptionValueMap() {
	[
		"20 seconds": 20,
		"30 seconds": 30,
		"40 seconds": 40,
		"1 minute"  : 60,
		"2 minutes" : 2 * 60,
		"3 minutes" : 3 * 60,
		"4 minutes" : 4 * 60,
		"5 minutes" : 5 * 60,
		"8 minutes" : 8 * 60,
		"15 minutes": 15 * 60,
		"30 minutes": 30 * 60,
		"1 hours"   : 1 * 60 * 60,
		"6 hours"   : 6 * 60 * 60,
		"12 hours"  : 12 * 60 * 60,
		"18 hours"  : 18 * 60 * 60,
		"24 hours"  : 24 * 60 * 60
	]
}

private setConfigured(configure) {
	updateDataValue("configured", configure)
}

private isConfigured() {
	getDataValue("configured") == "true"
}

private command(physicalgraph.zwave.Command cmd) {
	if (state.sec || zwaveInfo?.zw?.contains("s")) {
		zwave.securityV1.securityMessageEncapsulation().encapsulate(cmd).format()
	} else {
		cmd.format()
	}
}

private commands(commands, delay = 200) {
	log.debug "sending commands: ${commands}"
	delayBetween(commands.collect { command(it) }, delay)
}

def updateDataValuesForDebugging(parameterNumber, scaledConfigurationValue) {
	switch (parameterNumber) {
		case 101:
			updateDataValue("Group 1 reports enabled", getReportTypesFromValue(scaledConfigurationValue))
			break
		case 102:
			updateDataValue("Group 2 reports enabled", getReportTypesFromValue(scaledConfigurationValue))
			break
		case 111:
			updateDataValue("Group 1 reports interval", getIntervalString(scaledConfigurationValue))
			break
		case 112:
			updateDataValue("Group 2 reports interval", getIntervalString(scaledConfigurationValue))
			break
		case 40:
			updateDataValue("Automatic reports only when change is over threshold", scaledConfigurationValue ? "enabled" : "disabled")
			break
		case 4:
			updateDataValue("Motion Sensitivity (0-5)", "$scaledConfigurationValue")
			break
		case 9:
			//handled already as a state variable - do nothing
			break
		default:
			updateDataValue("Parameter $parameterNumber", "$scaledConfigurationValue")
			break
	}
}

def getIntervalString(interval) {
	interval % 3600 == 0 ? "${interval / 3600} hours" : (
		interval % 60 == 0 ? "${interval / 60} minutes" : "$scaledConfigurationValue seconds"
	)
}

def getReportTypesFromValue(value) {
	// param 101 -103 [4 bytes] 128: light sensor, 64 humidity, 32 temperature sensor, 16 ultraviolet sensor, 1 battery sensor
	def reportList = ""
	if (value > 0) {
		reportList = ""
		if (value & 128) reportList += "Luminance, "
		if (value & 64) reportList += "Humidity, "
		if (value & 32) reportList += "Temperature, "
		if (value & 16) reportList += "Ultraviolet, "
		if (value & 1) {
			reportList += "Battery"
		} else {
			reportList = reportList[0..-3]
		}
	} else {
		reportList = "none"
	}
	reportList
}

private isAeotecMultisensor7() {
	zwaveInfo.model.equals("0018") 
}
