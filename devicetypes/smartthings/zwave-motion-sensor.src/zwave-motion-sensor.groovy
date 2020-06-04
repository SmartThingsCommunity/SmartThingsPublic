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
 *  Generic Z-Wave Motion Sensor
 *
 *  Author: SmartThings
 *  Date: 2013-11-25
 */

metadata {
	definition (name: "Z-Wave Motion Sensor", namespace: "smartthings", author: "SmartThings", ocfDeviceType: "x.com.st.d.sensor.motion", runLocally: true, minHubCoreVersion: '000.017.0012', executeCommandsLocally: false, genericHandler: "Z-Wave") {
		capability "Motion Sensor"
		capability "Sensor"
		capability "Battery"
		capability "Health Check"
		capability "Tamper Alert"
		capability "Configuration"

		fingerprint mfr: "011F", prod: "0001", model: "0001", deviceJoinName: "Schlage Motion Sensor"  // Schlage motion //Schlage Motion Sensor
		fingerprint mfr: "014A", prod: "0001", model: "0001", deviceJoinName: "Ecolink Motion Sensor"  // Ecolink motion //Ecolink Motion Sensor
		fingerprint mfr: "014A", prod: "0004", model: "0001", deviceJoinName: "Ecolink Motion Sensor"  // Ecolink motion + //Ecolink Motion Sensor
		fingerprint mfr: "0060", prod: "0001", model: "0002", deviceJoinName: "Everspring Motion Sensor"  // Everspring SP814 //Everspring Motion Sensor
		fingerprint mfr: "0060", prod: "0001", model: "0003", deviceJoinName: "Everspring Motion Sensor"  // Everspring HSP02 //Everspring Motion Sensor
		fingerprint mfr: "0060", prod: "0001", model: "0004", deviceJoinName: "Everspring Motion Sensor"  //Everspring SP815 //Everspring Motion Detector
		fingerprint mfr: "0060", prod: "0001", model: "0005", deviceJoinName: "Everspring Motion Sensor"  //Everspring Motion Detector
		fingerprint mfr: "0060", prod: "0001", model: "0006", deviceJoinName: "Everspring Motion Sensor" //Everspring SP817 //Everspring Motion Detector
		fingerprint mfr: "011A", prod: "0601", model: "0901", deviceJoinName: "Enerwave Motion Sensor"  // Enerwave ZWN-BPC //Enerwave Motion Sensor
		fingerprint mfr: "0063", prod: "4953", model: "3133", deviceJoinName: "GE Motion Sensor" //GE Portable Smart Motion Sensor
		fingerprint mfr: "0214", prod: "0003", model: "0002", deviceJoinName: "BeSense Motion Sensor" //BeSense Motion Detector
		fingerprint mfr: "027A", prod: "0001", model: "0005", deviceJoinName: "Zooz Motion Sensor" //Zooz Outdoor Motion Sensor
		fingerprint mfr: "027A", prod: "0301", model: "0012", deviceJoinName: "Zooz Motion Sensor", mnmn: "SmartThings", vid: "generic-motion-2" //Zooz Motion Sensor
	}

	simulator {
		status "inactive": "command: 3003, payload: 00"
		status "active": "command: 3003, payload: FF"
	}

	tiles(scale: 2) {
		multiAttributeTile(name:"motion", type: "generic", width: 6, height: 4){
			tileAttribute("device.motion", key: "PRIMARY_CONTROL") {
				attributeState("active", label:'motion', icon:"st.motion.motion.active", backgroundColor:"#00A0DC")
				attributeState("inactive", label:'no motion', icon:"st.motion.motion.inactive", backgroundColor:"#CCCCCC")
			}
		}
		valueTile("battery", "device.battery", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state("battery", label:'${currentValue}% battery', unit:"")
		}
		valueTile("tamper", "device.tamper", height: 2, width: 2, decoration: "flat") {
			state "clear", label: 'tamper clear', backgroundColor: "#ffffff"
			state "detected", label: 'tampered', backgroundColor: "#ff0000"
		}

		main "motion"
		details(["motion", "battery", "tamper"])
	}

	// Preferences for Everspring SP815 and Everspring SP817
	// preferences {
		section {
			input(
					title: "Settings Available For Everspring SP815 and SP817 only",
					description: "",
					type: "paragraph",
					element: "paragraph"
			)
			input (
					title: "Temperature and Humidity Auto Report (Everspring SP815 only):",
					description: "This setting allows to adjusts report time (in seconds) of temperature and humidity report.",
					name: "temperatureAndHumidityReport",
					type: "number",
					range: "600..1440",
					defaultValue: 600
			) // defaultValue: 600
			input (
					title: "Re-trigger Interval Setting (Everspring SP815 and Everspring SP817 only):",
					description: "The setting adjusts the sleep period (in seconds) after the detector has been triggered. No response will be made during this interval if a movement is presented. Longer re-trigger interval will result in longer battery life.",
					name: "retriggerIntervalSettings",
					type: "number",
					range: "10..3600",
					defaultValue: 180
			) // defaultValue: 180
		}
	}

def installed() {
// Device wakes up every 4 hours, this interval allows us to miss one wakeup notification before marking offline
	sendEvent(name: "checkInterval", value: 8 * 60 * 60 + 2 * 60, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])
	sendEvent(name: "tamper", value: "clear", displayed: false)
}

def updated() {
	log.debug "updated"
// Device wakes up every 4 hours, this interval allows us to miss one wakeup notification before marking offline
	sendEvent(name: "checkInterval", value: 8 * 60 * 60 + 2 * 60, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])
	response(getConfigurationCommands())
}

def configure() {
	if (isEverspringSP815orSP817()) {
		state.configured = false
		state.intervalConfigured = false
		if(isEverspringSP815()){
			state.temperatureConfigured = false
		}
	}
	response(initialPoll())
}

private getCommandClassVersions() {
	[0x20: 1, 0x30: 1, 0x31: 5, 0x80: 1, 0x84: 1, 0x71: 3, 0x9C: 1]
}

def parse(String description) {
	def result = null
	if (description.startsWith("Err")) {
	    result = createEvent(descriptionText:description)
	} else {
		def cmd = zwave.parse(description, commandClassVersions)
		if (cmd) {
			result = zwaveEvent(cmd)
		} else {
			result = createEvent(value: description, descriptionText: description, isStateChange: false)
		}
	}
	return result
}

def sensorValueEvent(value) {
	if (value) {
		createEvent(name: "motion", value: "active", descriptionText: "$device.displayName detected motion")
	} else {
		createEvent(name: "tamper", value: "clear")
		createEvent(name: "motion", value: "inactive", descriptionText: "$device.displayName motion has stopped")
	}
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd)
{
	sensorValueEvent(cmd.value)
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicSet cmd)
{
	sensorValueEvent(cmd.value)
}

def zwaveEvent(physicalgraph.zwave.commands.switchbinaryv1.SwitchBinaryReport cmd)
{
	sensorValueEvent(cmd.value)
}

def zwaveEvent(physicalgraph.zwave.commands.sensorbinaryv1.SensorBinaryReport cmd)
{
	sensorValueEvent(cmd.sensorValue)
}

def zwaveEvent(physicalgraph.zwave.commands.sensoralarmv1.SensorAlarmReport cmd)
{
	sensorValueEvent(cmd.sensorState)
}

def zwaveEvent(physicalgraph.zwave.commands.notificationv3.NotificationReport cmd)
{
	def result = []
	if (cmd.notificationType == 0x07) {
		if (cmd.v1AlarmType == 0x07) {  // special case for nonstandard messages from Monoprice ensors
			result << sensorValueEvent(cmd.v1AlarmLevel)
		} else if (cmd.event == 0x01 || cmd.event == 0x02 || cmd.event == 0x07 || cmd.event == 0x08) {
			result << sensorValueEvent(1)
		} else if (cmd.event == 0x00) {
			result << sensorValueEvent(0)
		} else if (cmd.event == 0x03) {
			result << createEvent(name: "tamper", value: "detected", descriptionText: "$device.displayName covering was removed", isStateChange: true)
			result << response(zwave.batteryV1.batteryGet())
			unschedule(clearTamper, [forceForLocallyExecuting: true])
			runIn(10, clearTamper, [forceForLocallyExecuting: true])
		} else if (cmd.event == 0x05 || cmd.event == 0x06) {
			result << createEvent(descriptionText: "$device.displayName detected glass breakage", isStateChange: true)
		}
	} else if (cmd.notificationType) {
		def text = "Notification $cmd.notificationType: event ${([cmd.event] + cmd.eventParameter).join(", ")}"
		result << createEvent(name: "notification$cmd.notificationType", value: "$cmd.event", descriptionText: text, isStateChange: true, displayed: false)
	} else {
		def value = cmd.v1AlarmLevel == 255 ? "active" : cmd.v1AlarmLevel ?: "inactive"
		result << createEvent(name: "alarm $cmd.v1AlarmType", value: value, isStateChange: true, displayed: false)
	}

	log.debug "isConfigured: ${isConfigured()}"
	if (isEverspringSP815orSP817() && !isConfigured()) {
		result += lateConfigure()
	}
	result
}

def clearTamper() {
	sendEvent(name: "tamper", value: "clear")
}

def zwaveEvent(physicalgraph.zwave.commands.wakeupv1.WakeUpNotification cmd)
{
	def result = [createEvent(descriptionText: "${device.displayName} woke up", isStateChange: false)]

	if (isEverspringSP815orSP817() && !isConfigured()) {
		result += lateConfigure(true)
	}

	if (isEnerwave() && device.currentState('motion') == null) {  // Enerwave motion doesn't always get the associationSet that the hub sends on join
		result << response(zwave.associationV1.associationSet(groupingIdentifier:1, nodeId:zwaveHubNodeId))
	}
	if (!state.lastbat || (new Date().time) - state.lastbat > 53*60*60*1000) {
		result << response(zwave.batteryV1.batteryGet())
	} else {
		result << response(zwave.wakeUpV1.wakeUpNoMoreInformation())
	}
	result
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
	state.lastbat = new Date().time
	[createEvent(map), response(zwave.wakeUpV1.wakeUpNoMoreInformation())]
}

def zwaveEvent(physicalgraph.zwave.commands.sensormultilevelv5.SensorMultilevelReport cmd)
{
	def map = [ displayed: true, value: cmd.scaledSensorValue.toString() ]
	switch (cmd.sensorType) {
		case 1:
			map.name = "temperature"
			map.unit = cmd.scale == 1 ? "F" : "C"
			break;
		case 3:
			map.name = "illuminance"
			map.value = cmd.scaledSensorValue.toInteger().toString()
			map.unit = "lux"
			break;
		case 5:
			map.name = "humidity"
			map.value = cmd.scaledSensorValue.toInteger().toString()
			map.unit = cmd.scale == 0 ? "%" : ""
			break;
		case 0x1E:
			map.name = "loudness"
			map.unit = cmd.scale == 1 ? "dBA" : "dB"
			break;
	}
	createEvent(map)
}

def zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityMessageEncapsulation cmd) {
	def encapsulatedCommand = cmd.encapsulatedCommand(commandClassVersions)
	// log.debug "encapsulated: $encapsulatedCommand"
	if (encapsulatedCommand) {
		state.sec = 1
		zwaveEvent(encapsulatedCommand)
	}
}

def zwaveEvent(physicalgraph.zwave.commands.crc16encapv1.Crc16Encap cmd)
{
	// def encapsulatedCommand = cmd.encapsulatedCommand(commandClassVersions)
	def version = commandClassVersions[cmd.commandClass as Integer]
	def ccObj = version ? zwave.commandClass(cmd.commandClass, version) : zwave.commandClass(cmd.commandClass)
	def encapsulatedCommand = ccObj?.command(cmd.command)?.parse(cmd.data)
	if (encapsulatedCommand) {
		return zwaveEvent(encapsulatedCommand)
	}
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
	def encapsulatedCommand = cmd.encapsulatedCommand(commandClassVersions)
	log.debug "Command from endpoint ${cmd.sourceEndPoint}: ${encapsulatedCommand}"
	if (encapsulatedCommand) {
		result = zwaveEvent(encapsulatedCommand)
	}
	result
}

def zwaveEvent(physicalgraph.zwave.commands.multicmdv1.MultiCmdEncap cmd) {
	log.debug "MultiCmd with $numberOfCommands inner commands"
	cmd.encapsulatedCommands(commandClassVersions).collect { encapsulatedCommand ->
		zwaveEvent(encapsulatedCommand)
	}.flatten()
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
	createEvent(descriptionText: "$device.displayName: $cmd", displayed: false)
}

def zwaveEvent(physicalgraph.zwave.commands.manufacturerspecificv2.ManufacturerSpecificReport cmd) {
	def result = []

	def msr = String.format("%04X-%04X-%04X", cmd.manufacturerId, cmd.productTypeId, cmd.productId)
	log.debug "msr: $msr"
	updateDataValue("MSR", msr)

	result << createEvent(descriptionText: "$device.displayName MSR: $msr", isStateChange: false)
	result
}

def initialPoll() {
	def request = []
	if (isEnerwave()) { // Enerwave motion doesn't always get the associationSet that the hub sends on join
		request << zwave.associationV1.associationSet(groupingIdentifier:1, nodeId:zwaveHubNodeId)
	}
	if (isEverspringSP815orSP817()) {
		request += getConfigurationCommands()
	}
	request << zwave.batteryV1.batteryGet()
	request << zwave.sensorBinaryV2.sensorBinaryGet(sensorType: 0x0C) //motion
	request << zwave.notificationV3.notificationGet(notificationType: 0x07, event: 0x08) //motion for Everspiring
	log.debug "Request is: ${request}"
	commands(request) + ["delay 20000", zwave.wakeUpV1.wakeUpNoMoreInformation().format()]
}

private commands(commands, delay=200) {
	log.info "sending commands: ${commands}"
	delayBetween(commands.collect{ command(it) }, delay)
}

private command(physicalgraph.zwave.Command cmd) {
	if (zwaveInfo && zwaveInfo.zw?.contains("s")) {
		zwave.securityV1.securityMessageEncapsulation().encapsulate(cmd).format()
	} else if (zwaveInfo && zwaveInfo.cc?.contains("56")){
		zwave.crc16EncapV1.crc16Encap().encapsulate(cmd).format()
	} else {
		cmd.format()
	}
}

def getConfigurationCommands() {
	log.debug "getConfigurationCommands"
	def result = []
	if (isEverspringSP815()) {
		if (!state.temperatureAndHumidityReport) state.temperatureAndHumidityReport = getEverspringDefaults[1]
		if (!state.retriggerIntervalSettings) state.retriggerIntervalSettings = getEverspringDefaults[2]

		Integer temperatureAndHumidityReport = (settings.temperatureAndHumidityReport as Integer) ?: everspringDefaults[1]
		Integer retriggerIntervalSettings = (settings.retriggerIntervalSettings as Integer) ?: everspringDefaults[2]

		if (!isConfigured() || (temperatureAndHumidityReport != state.temperatureAndHumidityReport || retriggerIntervalSettings != state.retriggerIntervalSettings)) {
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
	} else if (isEverspringSP817()) {
		if (!state.retriggerIntervalSettings) state.retriggerIntervalSettings = everspringDefaults[2]
		Integer retriggerIntervalSettings = (settings.retriggerIntervalSettings as Integer) ?: everspringDefaults[2]
		if (!isConfigured() || (retriggerIntervalSettings != state.retriggerIntervalSettings)) {
			state.configured = false
			if (!state.intervalConfigured || retriggerIntervalSettings != state.retriggerIntervalSettings) {
				state.intervalConfigured = false
				result << zwave.configurationV2.configurationSet(parameterNumber: 4, size: 2, scaledConfigurationValue: retriggerIntervalSettings)
				result << zwave.configurationV2.configurationGet(parameterNumber: 4)
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
	sendHubCommand(commands(getConfigurationCommands(),200))
}

private isConfigured() {
	return state.configured == true
}

def zwaveEvent(physicalgraph.zwave.commands.configurationv2.ConfigurationReport cmd) {
	if (isEverspringSP815orSP817()) {
		if (cmd.parameterNumber == 1) {
			state.temperatureAndHumidityReport = scaledConfigurationValue
			state.temperatureConfigured = true
		} else if ((isEverspringSP815() && cmd.parameterNumber == 2) || (isEverspringSP817() && cmd.parameterNumber == 4)) {
			state.retriggerIntervalSettings = scaledConfigurationValue
			state.intervalConfigured = true
		}
		if (state.intervalConfigured == true && (isEverspringSP817() || (isEverspringSP815() && state.temperatureConfigured== true))) {
			state.configured = true
		}
		log.debug "Everspring Configuration Report: ${cmd}"
		return [:]
	}
}

private isEnerwave() {
	zwaveInfo?.mfr?.equals("011A") && zwaveInfo?.prod?.equals("0601") && zwaveInfo?.model?.equals("0901")
}

private isEverspringSP815() {
	zwaveInfo?.mfr?.equals("0060") && zwaveInfo?.model?.equals("0004")
}

private isEverspringSP817() {
	zwaveInfo?.mfr?.equals("0060") && zwaveInfo?.model?.equals("0006")
}

private isEverspringSP815orSP817() {
	zwaveInfo?.mfr?.equals("0060") && (zwaveInfo?.model?.equals("0004") || zwaveInfo?.model?.equals("0006"))
}
