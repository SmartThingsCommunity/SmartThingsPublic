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
	definition (name: "Aeon Multisensor 6", namespace: "smartthings", author: "SmartThings", runLocally: true, minHubCoreVersion: '000.020.00008', executeCommandsLocally: true) {
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

		fingerprint deviceId: "0x2101", inClusters: "0x5E,0x86,0x72,0x59,0x85,0x73,0x71,0x84,0x80,0x30,0x31,0x70,0x7A", outClusters: "0x5A"
		fingerprint deviceId: "0x2101", inClusters: "0x5E,0x86,0x72,0x59,0x85,0x73,0x71,0x84,0x80,0x30,0x31,0x70,0x7A,0x5A"
		fingerprint mfr:"0086", prod:"0102", model:"0064", deviceJoinName: "Aeotec MultiSensor 6"
	}

	simulator {
		status "no motion" : "command: 9881, payload: 00300300"
		status "motion"    : "command: 9881, payload: 003003FF"

		for (int i = 0; i <= 100; i += 20) {
			status "temperature ${i}F": new physicalgraph.zwave.Zwave().securityV1.securityMessageEncapsulation().encapsulate(
				new physicalgraph.zwave.Zwave().sensorMultilevelV2.sensorMultilevelReport(
					scaledSensorValue: i, precision: 1, sensorType: 1, scale: 1)
				).incomingMessage()
		}

		for (int i = 0; i <= 100; i += 20) {
			status "humidity ${i}%":  new physicalgraph.zwave.Zwave().securityV1.securityMessageEncapsulation().encapsulate(
				new physicalgraph.zwave.Zwave().sensorMultilevelV2.sensorMultilevelReport(scaledSensorValue: i, sensorType: 5)
			).incomingMessage()
		}

		for (int i in [0, 20, 89, 100, 200, 500, 1000]) {
			status "illuminance ${i} lux":  new physicalgraph.zwave.Zwave().securityV1.securityMessageEncapsulation().encapsulate(
				new physicalgraph.zwave.Zwave().sensorMultilevelV2.sensorMultilevelReport(scaledSensorValue: i, sensorType: 3)
			).incomingMessage()
		}

		for (int i in [0, 5, 10, 15, 50, 99, 100]) {
			status "battery ${i}%":  new physicalgraph.zwave.Zwave().securityV1.securityMessageEncapsulation().encapsulate(
				new physicalgraph.zwave.Zwave().batteryV1.batteryReport(batteryLevel: i)
			).incomingMessage()
		}
		status "low battery alert":  new physicalgraph.zwave.Zwave().securityV1.securityMessageEncapsulation().encapsulate(
			new physicalgraph.zwave.Zwave().batteryV1.batteryReport(batteryLevel: 255)
		).incomingMessage()

		status "wake up" : "command: 8407, payload: "
	}

	preferences {
		input description: "Please consult AEOTEC MULTISENSOR 6 operating manual for advanced setting options. You can skip this configuration to use default settings",
				title: "Advanced Configuration", displayDuringSetup: true, type: "paragraph", element: "paragraph"

		input "motionDelayTime", "enum", title: "Motion Sensor Delay Time",
				options: ["20 seconds", "40 seconds", "1 minute", "2 minutes", "3 minutes", "4 minutes"], defaultValue: "${motionDelayTime}", displayDuringSetup: true

		input "motionSensitivity", "enum", title: "Motion Sensor Sensitivity", options: ["normal","maximum","minimum"], defaultValue: "${motionSensitivity}", displayDuringSetup: true

		input "reportInterval", "enum", title: "Sensors Report Interval",
				options: ["8 minutes", "15 minutes", "30 minutes", "1 hour", "6 hours", "12 hours", "18 hours", "24 hours"], defaultValue: "${reportInterval}", displayDuringSetup: true
	}

	tiles(scale: 2) {
		multiAttributeTile(name:"motion", type: "generic", width: 6, height: 4){
			tileAttribute ("device.motion", key: "PRIMARY_CONTROL") {
				attributeState "active", label:'motion', icon:"st.motion.motion.active", backgroundColor:"#00A0DC"
				attributeState "inactive", label:'no motion', icon:"st.motion.motion.inactive", backgroundColor:"#cccccc"
			}
		}
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

		valueTile("illuminance", "device.illuminance", inactiveLabel: false, width: 2, height: 2) {
			state "illuminance", label:'${currentValue} lux', unit:""
		}

		valueTile("ultravioletIndex", "device.ultravioletIndex", inactiveLabel: false, width: 2, height: 2) {
			state "ultravioletIndex", label:'${currentValue} UV index', unit:""
		}

		valueTile("battery", "device.battery", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "battery", label:'${currentValue}% battery', unit:""
		}

		valueTile("batteryStatus", "device.batteryStatus", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "batteryStatus", label:'${currentValue}', unit:""
		}

		valueTile("powerSource", "device.powerSource", height: 2, width: 2, decoration: "flat") {
			state "powerSource", label:'${currentValue} powered', backgroundColor:"#ffffff"
		}
		valueTile("tamper", "device.tamper", height: 2, width: 2, decoration: "flat") {
			state "clear", label:'tamper clear', backgroundColor:"#ffffff"
			state "detected", label:'tampered', backgroundColor:"#ff0000"
		}

		main(["motion", "temperature", "humidity", "illuminance", "ultravioletIndex"])
		details(["motion", "temperature", "humidity", "illuminance", "ultravioletIndex", "batteryStatus", "tamper"])
	}
}

def installed(){
// Device-Watch simply pings if no device events received for 122min(checkInterval)
	sendEvent(name: "checkInterval", value: 2 * 60 * 60 + 2 * 60, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])

	sendEvent(name: "tamper", value: "clear", displayed: false)
}

def updated() {
	log.debug "Updated with settings: ${settings}"
	log.debug "${device.displayName} is now ${device.latestValue("powerSource")}"


	def powerSource = device.latestValue("powerSource")

	if (!powerSource) { // Check to see if we have updated to new powerSource attr
		def powerSupply = device.latestValue("powerSupply")

		if (powerSupply) {
			powerSource = (powerSupply == "Battery") ? "battery" : "dc"

			sendEvent(name: "powerSource", value: powerSource, displayed: false)
		}
	}

	if (powerSource == "battery") {
		setConfigured("false") //wait until the next time device wakeup to send configure command after user change preference
	} else { // We haven't identified the power supply, or the power supply is USB, so configure
		response(configure())
	}
}

def parse(String description) {
	log.debug "parse() >> description: $description"
	def result = null
	if (description.startsWith("Err 106")) {
		log.debug "parse() >> Err 106"
		result = createEvent( name: "secureInclusion", value: "failed", isStateChange: true,
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
		cmds << zwave.wakeUpV1.wakeUpNoMoreInformation().format()
		result << response(cmds)
	}
	result
}

def zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityMessageEncapsulation cmd) {
	def encapsulatedCommand = cmd.encapsulatedCommand([0x31: 5, 0x30: 2, 0x84: 1])
	state.sec = 1
	log.debug "encapsulated: ${encapsulatedCommand}"
	if (encapsulatedCommand) {
		zwaveEvent(encapsulatedCommand)
	} else {
		log.warn "Unable to extract encapsulated cmd from $cmd"
		createEvent(descriptionText: cmd.toString())
	}
}

def zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityCommandsSupportedReport cmd) {
	log.info "Executing zwaveEvent 98 (SecurityV1): 03 (SecurityCommandsSupportedReport) with cmd: $cmd"
	state.sec = 1
}

def zwaveEvent(physicalgraph.zwave.commands.securityv1.NetworkKeyVerify cmd) {
	state.sec = 1
	log.info "Executing zwaveEvent 98 (SecurityV1): 07 (NetworkKeyVerify) with cmd: $cmd (node is securely included)"
	def result = [createEvent(name:"secureInclusion", value:"success", descriptionText:"Secure inclusion was successful", isStateChange: true)]
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
	def map = [ name: "battery", unit: "%" ]
	if (cmd.batteryLevel == 0xFF) {
		map.value = 1
		map.descriptionText = "${device.displayName} battery is low"
		map.isStateChange = true
	} else {
		map.value = cmd.batteryLevel
	}
	state.lastbatt = now()
	result << createEvent(map)
	if (device.latestValue("powerSource") != "dc"){
		result << createEvent(name: "batteryStatus", value: "${map.value}% battery", displayed: false)
	}
	result
}

def zwaveEvent(physicalgraph.zwave.commands.sensormultilevelv5.SensorMultilevelReport cmd){
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
				result << createEvent(name: "tamper", value: "detected", descriptionText: "$device.displayName was tampered")
				// Clear the tamper alert after 10s. This is a temporary fix for the tamper attribute until local execution handles it
				unschedule(clearTamper)
				runIn(10, clearTamper)
				break
			case 7:
				result << motionEvent(1)
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
	if (cmd.parameterNumber == 9 && cmd.configurationValue[0] == 0) {
		value = "dc"
		if (!isConfigured()) {
			log.debug("ConfigurationReport: configuring device")
			result << response(configure())
		}
		result << createEvent(name: "batteryStatus", value: "USB Cable", displayed: false)
		result << createEvent(name: "powerSource", value: value, displayed: false)
	}else if (cmd.parameterNumber == 9 && cmd.configurationValue[0] == 1) {
		value = "battery"
		result << createEvent(name: "powerSource", value: value, displayed: false)
	} else if (cmd.parameterNumber == 101){
		result << response(configure())
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
	if (device.latestValue("powerSource") == "dc") {
		command(zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType: 0x01)) //poll the temperature to ping
	} else {
		log.debug "Can't ping a wakeup device on battery"
	}
}

def configure() {
	// This sensor joins as a secure device if you double-click the button to include it
	log.debug "${device.displayName} is configuring its settings"
	def request = []

	//1. set association groups for hub
	request << zwave.associationV1.associationSet(groupingIdentifier:1, nodeId:zwaveHubNodeId)

	request << zwave.associationV1.associationSet(groupingIdentifier:2, nodeId:zwaveHubNodeId)

	//2. automatic report flags
	// param 101 -103 [4 bytes] 128: light sensor, 64 humidity, 32 temperature sensor, 2 ultraviolet sensor, 1 battery sensor -> send command 227 to get all reports
	request << zwave.configurationV1.configurationSet(parameterNumber: 101, size: 4, scaledConfigurationValue: 226) //association group 1

	request << zwave.configurationV1.configurationSet(parameterNumber: 102, size: 4, scaledConfigurationValue: 1) //association group 2

	//3. no-motion report x seconds after motion stops (default 20 secs)
	request << zwave.configurationV1.configurationSet(parameterNumber: 3, size: 2, scaledConfigurationValue: timeOptionValueMap[motionDelayTime] ?: 20)

	//4. motionSensitivity 3 levels: 64-normal (default), 127-maximum, 0-minimum
	request << zwave.configurationV1.configurationSet(parameterNumber: 6, size: 1,
			scaledConfigurationValue:
					motionSensitivity == "normal" ? 64 :
							motionSensitivity == "maximum" ? 127 :
									motionSensitivity == "minimum" ? 0 : 64)

	//5. report every x minutes (threshold reports don't work on battery power, default 8 mins)
	request << zwave.configurationV1.configurationSet(parameterNumber: 111, size: 4, scaledConfigurationValue: timeOptionValueMap[reportInterval] ?: (8*60)) //association group 1

	request << zwave.configurationV1.configurationSet(parameterNumber: 112, size: 4, scaledConfigurationValue: 6*60*60)  //association group 2

	//6. report automatically on threshold change
	request << zwave.configurationV1.configurationSet(parameterNumber: 40, size: 1, scaledConfigurationValue: 1)

	//7. query sensor data
	request << zwave.batteryV1.batteryGet()
	request << zwave.sensorBinaryV2.sensorBinaryGet(sensorType: 0x0C) //motion
	request << zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType: 0x01) //temperature
	request << zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType: 0x03) //illuminance
	request << zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType: 0x05) //humidity
	request << zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType: 0x1B) //ultravioletIndex
	request << zwave.configurationV1.configurationGet(parameterNumber: 9)

	setConfigured("true")

	// set the check interval based on the report interval preference. (default 122 minutes)
	// we do this here in case the device is in wakeup mode
	def checkInterval = 2 * 60 * 60 + 2 * 60
	if (reportInterval) {
		checkInterval = 2*timeOptionValueMap[reportInterval] + (2 * 60)
	}
	sendEvent(name: "checkInterval", value: checkInterval, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])

	commands(request) + ["delay 20000", zwave.wakeUpV1.wakeUpNoMoreInformation().format()]
}

private def getTimeOptionValueMap() { [
		"20 seconds" : 20,
		"40 seconds" : 40,
		"1 minute"   : 60,
		"2 minutes"  : 2*60,
		"3 minutes"  : 3*60,
		"4 minutes"  : 4*60,
		"5 minutes"  : 5*60,
		"8 minutes"  : 8*60,
		"15 minutes" : 15*60,
		"30 minutes" : 30*60,
		"1 hours"    : 1*60*60,
		"6 hours"    : 6*60*60,
		"12 hours"   : 12*60*60,
		"18 hours"   : 18*60*60,
		"24 hours"   : 24*60*60,
]}

private setConfigured(configure) {
	updateDataValue("configured", configure)
}

private isConfigured() {
	getDataValue("configured") == "true"
}

private command(physicalgraph.zwave.Command cmd) {
	if (state.sec) {
		zwave.securityV1.securityMessageEncapsulation().encapsulate(cmd).format()
	} else {
		cmd.format()
	}
}

private commands(commands, delay=200) {
	log.info "sending commands: ${commands}"
	delayBetween(commands.collect{ command(it) }, delay)
}
