/*
 *  Zooz ZSE11 Q Sensor
 *
 *  Changelog:
 *
 *    2022-03-09
 *      - Requested changes
 *    2022-03-02
 *      - Requested changes
 *    2022-03-01
 *      - Publication Release
 *
 *  Copyright 2022 Zooz
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
*/

import groovy.transform.Field

@Field static Map commandClassVersions = [
	0x30: 2, // SensorBinary
	0x31: 5, // SensorMultilevel
	0x55: 1, // Transport Service
	0x59: 1, // AssociationGrpInfo
	0x5A: 1, // DeviceResetLocally
	0x5E: 2, // ZwaveplusInfo
	0x6C: 1, // Supervision
	0x70: 1, // Configuration
	0x71: 3, // Notification
	0x72: 2, // ManufacturerSpecific
	0x73: 1, // Powerlevel
	0x7A: 2, // FirmwareUpdateMd
	0x80: 1, // Battery
	0x84: 2, // WakeUp
	0x85: 2, // Association
	0x86: 1, // Version
	0x98: 1, // Security S0
	0x9F: 1	 // Security S2
]

@Field static String batteryCC = "80"
@Field static int homeSecurity = 7
@Field static int homeSecurityTamper = 3
@Field static int tempSensorType = 1
@Field static int lightSensorType = 3
@Field static int humiditySensorType = 5
@Field static int motionSensorType = 12

metadata {
	definition (
		name: "Zooz ZSE11 Q Sensor",
		namespace: "Zooz",
		author: "Kevin LaFramboise (@krlaframboise)",
		ocfDeviceType: "x.com.st.d.sensor.motion",
		mnmn: "SmartThingsCommunity",
		vid: "42067896-6424-3a34-b753-b87d8c92262f"
	) {
		capability "Sensor"
		capability "Motion Sensor"
		capability "Tamper Alert"
		capability "Temperature Measurement"
		capability "Illuminance Measurement"
		capability "Relative Humidity Measurement"
		capability "Battery"
		capability "Refresh"
		capability "Health Check"
		capability "Power Source"

		// zw:Ss2 type:0701 mfr:027A prod:0200 model:0006 ver:1.09 zwv:6.04 lib:03 cc:5E,6C,55,98,9F sec:86,72,71,59,85,80,84,73,30,31,70,5A,7A
		fingerprint mfr:"027A", prod:"0200", model:"0006", deviceJoinName: "Zooz Multipurpose Sensor" // Zooz ZSE11 Q Sensor (EU)
		// zw:Ss2 type:0701 mfr:027A prod:0201 model:0006 ver:1.09 zwv:6.04 lib:03 cc:5E,6C,55,98,9F sec:86,72,71,59,85,80,84,73,30,31,70,5A,7A
		fingerprint mfr:"027A", prod:"0201", model:"0006", deviceJoinName: "Zooz Multipurpose Sensor" // Zooz ZSE11 Q Sensor (US)
		// zw:Ss2 type:0701 mfr:027A prod:0202 model:0006 ver:1.09 zwv:6.04 lib:03 cc:5E,6C,55,98,9F sec:86,72,71,59,85,80,84,73,30,31,70,5A,7A
		fingerprint mfr:"027A", prod:"0202", model:"0006", deviceJoinName: "Zooz Multipurpose Sensor" // Zooz ZSE11 Q Sensor (AU)
	}

	preferences {
		configParams.each { param ->
			if (param.options) {
				input "configParam${param.num}", "enum",
					title: "${param.name}:",
					required: false,
					displayDuringSetup: false,
					defaultValue: param.defaultVal,
					options: param.options
			} else if (param.range) {
				input "configParam${param.num}", "number",
					title: "${param.name}:",
					required: false,
					displayDuringSetup: false,
					defaultValue: param.defaultVal,
					range: param.range
			}
		}

		input "tempOffset", "decimal",
			title: "Temperature Offset:",
			required: false,
			defaultValue: 0,
			range: "-50..50"

		input "humidityOffset", "number",
			title: "Humidity Offset:",
			required: false,
			defaultValue: 0,
			range: "-50..50"

		input "lightOffset", "number",
			title: "Light Offset:",
			required: false,
			defaultValue: 0,
			range: "-20000..20000"
	}
}

def installed() {
	log.debug "installed()..."
	state.firstConfig = true
	initialize()
}

def updated() {
	log.debug "updated()..."

	initialize()

	if (!state.firstConfig) {
		if (device.currentValue("powerSource") == "battery") {
			logForceWakeupMessage("Configuration changes will be sent to the device the next time it wakes up.")
		} else {
			sendHubCommand(getConfigCmds())
		}
	} else {
		sendHubCommand(getRefreshCmds())
		state.firstConfig = false
	}
}

void initialize() {
	if (!device.currentValue("checkInterval")) {
		sendEvent(name: "checkInterval", value: ((60 * 60 * 24) + (60 * 5)), displayed: false, data:[protocol: "zwave", hubHardwareId: device.hub.hardwareID])
	}

	if (!device.currentValue("tamper")) {
		sendEvent(name: "tamper", value: "clear")
	}

	if (device.currentValue("powerSource") == null) {
		boolean hasBatteryCC = ((zwaveInfo?.cc?.find { it.toString() == batteryCC }) || (zwaveInfo?.sec?.find { it.toString() == batteryCC })) 

		String powerSource = (hasBatteryCC ? "battery" : "dc")
		sendEvent(name: "powerSource", value: powerSource)

		if (powerSource == "dc") {
			sendEvent(name: "battery", value: 100, unit: "%")
		}
	}

	sendTempEvent(state.reportedTemp)
	sendLightEvent(state.reportedLight)
	sendHumidityEvent(state.reportedHumidity)
}

def configure() {
	log.debug "configure()..."
	sendHubCommand(getConfigCmds(), 200)
}

List<String> getConfigCmds() {
	List<String> cmds = []

	configParams.each { param ->
		def storedVal = safeToInt(state["configVal${param.num}"] , null)
		if ("${storedVal}" != "${param.value}") {
			log.debug "Changing ${param.name}(#${param.num}) from ${storedVal} to ${param.value}"
			cmds << secureCmd(zwave.configurationV1.configurationSet(parameterNumber: param.num, size: param.size, scaledConfigurationValue: param.value))
			cmds << secureCmd(zwave.configurationV1.configurationGet(parameterNumber: param.num))
		}
	}
	return cmds
}

def refresh() {
	log.debug "refresh()..."

	if (device.currentValue("tamper") != "clear") {
		sendEvent(name:"tamper", value:"clear")
	}

	if (device.currentValue("powerSource") == "battery") {
		state.pendingRefresh = true
		logForceWakeupMessage("The sensor values will be requested the next time the device wakes up.")
	} else {
		sendHubCommand(getRefreshCmds())
	}
}

void logForceWakeupMessage(msg) {
	log.debug "${msg}  You can force the device to wake up immediately by holding the z-button for 3 seconds."
}

List<String> getRefreshCmds() {
	return [
		secureCmd(zwave.sensorBinaryV2.sensorBinaryGet(sensorType: motionSensorType)),		
		sensorMultilevelGetCmd(tempSensorType),
		sensorMultilevelGetCmd(lightSensorType),
		sensorMultilevelGetCmd(humiditySensorType),
		batteryGetCmd()
	]
}

String sensorMultilevelGetCmd(sensorType) {
	def scale = (sensorType == tempSensorType ? 0 : 1)
	return secureCmd(zwave.sensorMultilevelV5.sensorMultilevelGet(scale: scale, sensorType: sensorType))
}

String batteryGetCmd() {
	return secureCmd(zwave.batteryV1.batteryGet())
}

String secureCmd(cmd) {
	if (zwaveInfo?.zw?.contains("s")) {
		return zwave.securityV1.securityMessageEncapsulation().encapsulate(cmd).format()
	} else {
		return cmd.format()
	}
}

def parse(String description) {
	def cmd = zwave.parse(description, commandClassVersions)
	if (cmd) {
		zwaveEvent(cmd)
	} else {
		log.warn "Unable to parse: $description"
	}
}

void zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityMessageEncapsulation cmd) {
	def encapsulatedCmd = cmd.encapsulatedCommand(commandClassVersions)
	if (encapsulatedCmd) {
		zwaveEvent(encapsulatedCmd)
	} else {
		log.warn "Unable to extract encapsulated cmd from $cmd"
	}
}

void zwaveEvent(physicalgraph.zwave.commands.wakeupv2.WakeUpNotification cmd) {
	log.debug "Device Woke Up"
	List<String> cmds = []

	if (state.pendingRefresh) {
		state.pendingRefresh = false
		cmds += getRefreshCmds()
	}

	cmds += getConfigCmds()
	
	if (!cmds) {
		cmds << batteryGetCmd()
	}

	cmds << secureCmd(zwave.wakeUpV1.wakeUpNoMoreInformation())
	sendHubCommand(cmds)
}

void zwaveEvent(physicalgraph.zwave.commands.batteryv1.BatteryReport cmd) {
	int val = (cmd.batteryLevel == 0xFF ? 1 : cmd.batteryLevel)
	val = Math.min(Math.max(1, val), 100)

	if (device.currentValue("powerSource") != "battery") {
		log.debug "powerSource is battery"
		sendEvent(name:"powerSource", value:"battery")
	}

	log.debug "battery is ${val}%"
	sendEvent(name:"battery", value:val, unit:"%", isStateChange:true)
}

void zwaveEvent(physicalgraph.zwave.commands.sensormultilevelv5.SensorMultilevelReport cmd) {
	switch (cmd.sensorType) {
		case tempSensorType:
			def temp = convertTemperatureIfNeeded(cmd.scaledSensorValue, (cmd.scale ? "F" : "C"), cmd.precision)
			sendTempEvent(temp)
			break
		case lightSensorType:
			sendLightEvent(cmd.scaledSensorValue)
			break
		case humiditySensorType:
			sendHumidityEvent(cmd.scaledSensorValue)
			break
		default:
			log.debug "Unhandled: ${cmd}"
	}
}

void sendTempEvent(reportedVal) {
	reportedVal = safeToDec(reportedVal)
	state.reportedTemp = reportedVal

	def adjVal = (safeToDec(settings?.tempOffset) + reportedVal)
	log.debug "temperature is ${adjVal}°${temperatureScale}"
	sendEvent(name:"temperature", value:adjVal, unit:temperatureScale)
}

void sendLightEvent(reportedVal) {
	reportedVal = safeToInt(reportedVal)
	if (reportedVal < 0) {
		// workaround for bug in original firmware
		reportedVal = (reportedVal + 65536)
	}
	state.reportedLight = reportedVal

	def adjVal = (safeToInt(settings?.lightOffset) + reportedVal)
	if (adjVal < 0) adjVal = 0
	log.debug "illuminance is ${adjVal}lux"
	sendEvent(name:"illuminance", value:adjVal, unit:"lux")
}

void sendHumidityEvent(reportedVal) {
	reportedVal = safeToInt(reportedVal)
	state.reportedHumidity = reportedVal

	def adjVal = (safeToInt(settings?.humidityOffset) + reportedVal)
	adjVal = Math.min(Math.max(0, adjVal), 100)
	log.debug "humidity is ${adjVal}%"
	sendEvent(name:"humidity", value:adjVal, unit:"%")
}

void zwaveEvent(physicalgraph.zwave.commands.notificationv3.NotificationReport cmd) {
	if (cmd.notificationType == homeSecurity) {
		if ((cmd.event == homeSecurityTamper) || (cmd.eventParameter[0] == homeSecurityTamper)) {
			String value = (cmd.event ? "detected" : "clear")
			log.debug "tamper is ${value}"
			sendEvent(name:"tamper", value:value)
		}
	}
}

void zwaveEvent(physicalgraph.zwave.commands.sensorbinaryv2.SensorBinaryReport cmd) {
	if (cmd.sensorType == motionSensorType) {
		String value = (cmd.sensorValue ? "active" : "inactive")
		log.debug "motion is ${value}"
		sendEvent(name:"motion", value:value)
	}
}

void zwaveEvent(physicalgraph.zwave.commands.configurationv1.ConfigurationReport cmd) {
	def param = configParams.find { it.num == cmd.parameterNumber }
	if (param) {
		def val = cmd.scaledConfigurationValue
		log.debug "${param.name}(#${param.num}) = ${val}"
		state["configVal${param.num}"] = val
	}
}

void zwaveEvent(physicalgraph.zwave.Command cmd) {
	log.debug "Ignored Command: $cmd"
}

List<Map> getConfigParams() {
	[
		motionSensitivityParam,
		motionResetParam,
		motionLedParam,
		reportingFrequencyParam,
		temperatureThresholdParam,
		humidityThresholdParam,
		lightThresholdParam
	]
}

Map getMotionSensitivityParam() {
	return getParam(12, "Motion Sensitivity", 1, 6, [0:"Motion Disabled", 1:"1 - Least Sensitive", 2:"2", 3:"3", 4:"4", 5:"5", 6:"6 [DEFAULT]", 7:"7", 8:"8 - Most Sensitive"])
}

Map getMotionResetParam() {
	return getParam(13, "Motion Clear Time (10-3600 Seconds)", 2, 30, null, "10..3600")
}

Map getMotionLedParam() {
	return getParam(19, "Motion LED", 1, 1, [0:"Disabled", 1:"Enabled [DEFAULT]"])
}

Map getReportingFrequencyParam() {
	return getParam(172, "Minimum Reporting Frequency (1-774 Hours)", 2, 4, null, "1..744")
}

Map getTemperatureThresholdParam() {
	return getParam(183, "Temperature Reporting Threshold (1-144°F)", 2, 1, null, "1..144")
}

Map getHumidityThresholdParam() {
	return getParam(184, "Humidity Reporting Threshold (0:No Reports, 1-80%)", 1, 5, null, "0..80")
}

Map getLightThresholdParam() {
	return getParam(185, "Light Reporting Threshold (0:No Reports, 1-30000 lux)", 2, 50, null, "0..30000")
}

Map getParam(Integer num, String name, Integer size, Integer defaultVal, Map options, range=null) {
	Integer val = safeToInt((settings ? settings["configParam${num}"] : null), defaultVal)

	return [num: num, name: name, size: size, defaultVal: defaultVal, value: val, options: options, range: range]
}

Integer safeToInt(val, Integer defaultVal=0) {
	if ("${val}"?.isInteger()) {
		return "${val}".toInteger()
	} else if ("${val}".isDouble()) {
		return "${val}".toDouble()?.round()
	} else {
		return defaultVal
	}
}

BigDecimal safeToDec(val, BigDecimal defaultVal=0) {
	return "${val}"?.isBigDecimal() ? "${val}".toBigDecimal() : defaultVal
}