/*
 *  Zooz ZSE44 Temperature | Humidity XS Sensor
 *
 *  Changelog:
 *
 *    2022-02-01
 *      - Requested changes
 *
 *    2022-01-27
 *      - Replaced temperatureAlarm custom capability with built-in capability.
 *
 *    2022-01-26.2
 *      - Requested Changes
 *
 *    2022-01-26
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
	0x31: 5,	// SensorMultilevel
	0x55: 1,	// Transport Service v2
	0x59: 1,	// AssociationGrpInfo v3
	0x5A: 1,	// DeviceResetLocally
	0x5E: 2,	// ZwaveplusInfo v2
	0x6C: 1,	// Supervision
	0x70: 2,	// Configuration v4
	0x71: 3,	// Notification v4
	0x72: 2,	// ManufacturerSpecific
	0x73: 1,	// Powerlevel
	0x7A: 2,	// FirmwareUpdateMd v5
	0x80: 1,	// Battery
	0x84: 2,	// WakeUp
	0x85: 2,	// Association v3
	0x86: 1,	// Version v2
	0x87: 1,	// Indicator v3
	0x8E: 2,	// Multi Channel Association v4
	0x9F: 1 	// Security 2
]

@Field static Map configParams = [
	lowBatteryReports: [num:2, title:"Low Battery Reports", size:1, defaultVal:10, options:[10:"10% [DEFAULT]", 20:"20%", 30:"30%", 40:"40%", 50:"50%"]],
	tempReportingThreshold: [num:3, title:"Temperature Reporting Threshold", size:1, defaultVal:10, range:"10..100", desc:"10..100 (10 = 1°)"],
	tempReportingInterval: [num:16, title:"Temperature Reporting Interval", size:2, defaultVal:240, range:"0..480", desc:"0(disabled), 1..480(minutes)"],
	tempUnit: [num:13, title:"Temperature Unit", size:1, defaultVal:1, options:[0:"Celsius", 1:"Fahrenheit [DEFAULT]"]],
	tempOffset: [num:14, title:"Temperature Offset", size:1, defaultVal:100, range:"0..200", desc:"0..200 (0: -10°, 100: 0°, 200: +10°)"],
	highTempThreshold: [num:5, title:"Heat Alert Temperature", size:1, defaultVal:120, range:"50..120", desc:"50..120(°)"],
	lowTempThreshold: [num:7, title:"Freeze Alert Temperature", size:1, defaultVal:10, range:"10..100", desc:"10..100(°)"],
	humidityReportingThreshold: [num:4, title:"Humidity Reporting Threshold", size:1, defaultVal:5, range:"1..50", desc:"1..50(%)"],
	humidityReportingInterval: [num:17, title:"Humidity Reporting Interval", size:2, defaultVal:240, range:"0..480", desc:"0(disabled), 1..480(minutes)"],
	humidityOffset: [num:15, title:"Humidity Offset", size:1, defaultVal:100, range:"0..200", desc:"0..200 (0: -10%, 100: 0%, 200: +10%)"],
	highHumidityThreshold: [num:9, title:"High Humidity Alert Level", size:1, defaultVal:0, range:"0..100", desc:"0(disabled), 1..100(%)"],
	lowHumidityThreshold: [num:11, title:"Low Humidity Alert Level", size:1, defaultVal:0, range:"0..100", desc:"0(disabled), 1..100(%)"]
]

@Field static Map temperatureSensor = [sensorType:1, scale:1]
@Field static Map humiditySensor = [sensorType: 5, scale:0]
@Field static Map temperatureAlarm = [name:"temperatureAlarm", notificationType:4, eventValues:[0:"cleared", 2:"heat", 6:"freeze"]]
@Field static Map humidityAlarm = [name:"humidityAlarm", notificationType:16, eventValues:[0:"normal", 2:"high", 6:"low"]]
@Field static int wakeUpInterval = 43200

metadata {
	definition (
		name: "Zooz ZSE44 Temperature | Humidity XS Sensor",
		namespace: "Zooz",
		author: "Kevin LaFramboise (@krlaframboise)",
		ocfDeviceType:"oic.d.thermostat",
		vid: "b68c78d7-bd01-3717-a2ac-d1d55ce5ef73",
		mnmn: "SmartThingsCommunity"
	) {
		capability "Sensor"
		capability "Temperature Measurement"
		capability "Relative Humidity Measurement"
		capability "Battery"
		capability "Refresh"
		capability "Health Check"
		capability "Configuration"
		capability "platemusic11009.temperatureHumiditySensor"
		capability "temperatureAlarm"
		capability "platemusic11009.humidityAlarm"
		capability "platemusic11009.firmware"
		capability "platemusic11009.syncStatus"

		// zw:Ss2a type:0701 mfr:027A prod:7000 model:E004 ver:1.10 zwv:7.13 lib:03 cc:5E,55,9F,6C sec:86,85,8E,59,31,72,5A,87,73,80,71,70,84,7A
		fingerprint mfr:"027A", prod:"7000", model:"E004", deviceJoinName: "Zooz Multipurpose Sensor" // Zooz ZSE44 Temperature | Humidity XS Sensor
	}

	preferences {
		configParams.each { name, param ->
			if (param.options) {
				input name, "enum",
					title: param.title,
					description: "Default: ${param.options[param.defaultVal]}",
					required: false,
					displayDuringSetup: false,
					defaultValue: param.defaultVal,
					options: param.options
			} else if (param.range) {
				input name, "number",
					title: param.title,
					description: "${param.desc} - Default: ${param.defaultVal}",
					required: false,
					displayDuringSetup: false,
					defaultValue: param.defaultVal,
					range: param.range
			}
		}

		input "debugLogging", "enum",
			title: "Logging:",
			description: "Default: Enabled",
			required: false,
			defaultValue: "1",
			options: ["0":"Disabled", "1":"Enabled"]
	}
}

def installed() {
	logDebug "installed()..."
	state.pendingRefresh = true
	initialize()
}

def updated() {
	logDebug "updated()..."
	initialize()

	if (pendingChanges) {
		logForceWakeupMessage("The setting changes will be sent to the device the next time it wakes up.")
	}
}

void initialize() {
	state.debugLoggingEnabled = (safeToInt(settings?.debugLogging, 1) != 0)

	refreshSyncStatus()

	if (device.currentValue("temperatureHumidity") == null) {
		state.displayHumidity = " "
		state.displayTemperature = " "
		sendEvent(name:"temperatureHumidity", value:" ")
	}

	if (!device.currentValue("temperatureAlarm")) {
		sendEvent(name:"temperatureAlarm", value:"cleared")
	}

	if (!device.currentValue("humidityAlarm")) {
		sendEvent(name:"humidityAlarm", value:"normal")
	}

	if (!device.currentValue("checkInterval")) {
		sendEvent([name: "checkInterval", value: ((wakeUpInterval * 2) + (5 * 60)), displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID]])
	}
}

def refresh() {
	logDebug "refresh()..."
	refreshSyncStatus()
	state.pendingRefresh = true
	logForceWakeupMessage("The device will be refreshed the next time it wakes up.")
}

void logForceWakeupMessage(String msg) {
	log.warn "${msg}  To force the device to wake up immediately press the action button 4x quickly."
}

def configure() {
	logDebug "configure()..."
	sendHubCommand(getRefreshCmds(), 250)
}

List<String> getRefreshCmds() {
	List<String> cmds = []

	if (state.wakeUpInterval == null) {
		cmds << secureCmd(zwave.wakeUpV2.wakeUpIntervalGet())
	}

	if (state.pendingRefresh || !device.currentValue("battery")) {
		cmds << secureCmd(zwave.batteryV1.batteryGet())
	}

	if (state.pendingRefresh || (device.currentValue("temperature") == null)) {
		cmds << secureCmd(zwave.sensorMultilevelV5.sensorMultilevelGet(scale: temperatureSensor.scale, sensorType: temperatureSensor.sensorType))
	}

	if (state.pendingRefresh || (device.currentValue("humidity") == null)) {
		cmds << secureCmd(zwave.sensorMultilevelV5.sensorMultilevelGet(scale: humiditySensor.scale, sensorType: humiditySensor.sensorType))
	}

	if (state.pendingRefresh || !device.currentValue("firmwareVersion")) {
		cmds << secureCmd(zwave.versionV1.versionGet())
	}

	state.pendingRefresh = false
	return cmds
}

List<String> getConfigureCmds() {
	List<String> cmds = []

	int changes = pendingChanges
	if (changes) {
		log.warn "Syncing ${changes} Change(s)"
	}

	if (state.wakeUpInterval != wakeUpInterval) {
		cmds << secureCmd(zwave.wakeUpV2.wakeUpIntervalSet(seconds: wakeUpInterval, nodeid:zwaveHubNodeId))
		cmds << secureCmd(zwave.wakeUpV2.wakeUpIntervalGet())
	}

	configParams.each { name, param ->
		Integer storedVal = getStoredVal(name)
		Integer settingVal = getSettingVal(name)
		if (storedVal != settingVal) {
			logDebug "Changing ${param.title}(#${param.num}) from ${storedVal} to ${settingVal}"
			cmds << secureCmd(zwave.configurationV1.configurationSet(parameterNumber: param.num, size: param.size, scaledConfigurationValue: settingVal))
			cmds << secureCmd(zwave.configurationV1.configurationGet(parameterNumber: param.num))
		}
	}
	return cmds
}

def ping() {
	logDebug "ping()"
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
	logDebug "Device Woke Up..."
	List<String> cmds = []

	cmds += getRefreshCmds()
	cmds += getConfigureCmds()

	if (!cmds) {
		cmds << secureCmd(zwave.batteryV1.batteryGet())
	}

	cmds << secureCmd(zwave.wakeUpV2.wakeUpNoMoreInformation())
	sendHubCommand(cmds, 250)
}

void zwaveEvent(physicalgraph.zwave.commands.wakeupv2.WakeUpIntervalReport cmd) {
	logDebug "Wake Up Interval = ${cmd.seconds} seconds"
	state.wakeUpInterval = cmd.seconds
	refreshSyncStatus()
}

void zwaveEvent(physicalgraph.zwave.commands.batteryv1.BatteryReport cmd) {
	Integer val = (cmd.batteryLevel == 0xFF ? 1 : cmd.batteryLevel)
	if (val > 100) {
		val = 100
	}
	logDebug "Battery is ${val}%"
	sendEvent(name:"battery", value:val, unit:"%", isStateChange: true)
}

void zwaveEvent(physicalgraph.zwave.commands.notificationv3.NotificationReport cmd) {
	logDebug "${cmd}"
	switch (cmd.notificationType) {
		case temperatureAlarm.notificationType:
			sendAlarmEvent(temperatureAlarm, cmd.event)
			break
		case humidityAlarm.notificationType:
			sendAlarmEvent(humidityAlarm, cmd.event)
			break
		default:
			logDebug "${cmd}"
	}
}

void sendAlarmEvent(Map alarm, int notificationEvent) {
	String value = alarm.eventValues[notificationEvent]
	if (value) {
		logDebug "${alarm.name} is ${value}"
		sendEvent(name: alarm.name, value: value)
	}
}

void zwaveEvent(physicalgraph.zwave.commands.sensormultilevelv5.SensorMultilevelReport cmd) {
	switch (cmd.sensorType) {
		case temperatureSensor.sensorType:
			def temperature = convertTemperatureIfNeeded(cmd.scaledSensorValue, (cmd.scale ? "F" : "C"), cmd.precision)
			sendTemperatureEvent(temperature)
			break
		case humiditySensor.sensorType:
			sendHumidityEvent(cmd.scaledSensorValue)
			break
		default:
			logDebug "Unhandled: ${cmd}"
	}
}

void sendTemperatureEvent(value) {
	state.displayTemperature = "${value}°${temperatureScale}"
	logDebug "temperature is ${value}°${temperatureScale}"
	sendEvent(name: "temperature", value: value, unit: temperatureScale)
	sendTemperatureHumidityEvent()
}

void sendHumidityEvent(value) {
	state.displayHumidity = "${safeToInt(value)}%"
	logDebug "humidity is ${value}%"
	sendEvent(name: "humidity", value: value, unit: "%")
	sendTemperatureHumidityEvent()
}

void sendTemperatureHumidityEvent() {
	sendEvent(name: "temperatureHumidity", value: "${state.displayTemperature} | ${state.displayHumidity}", displayed: false)
}

void zwaveEvent(physicalgraph.zwave.commands.versionv1.VersionReport cmd) {
	logDebug "${cmd}"
	sendEvent(name: "firmwareVersion", value: (cmd.applicationVersion + (cmd.applicationSubVersion / 100)))
}

void zwaveEvent(physicalgraph.zwave.commands.configurationv2.ConfigurationReport cmd) {
	runIn(4, refreshSyncStatus)
	String name = configParams.find { name, param -> param.num == cmd.parameterNumber }?.key
	if (name) {
		int val = cmd.scaledConfigurationValue

		if ((val < 0) && ((name == "humidityOffset") || (name == "tempOffset"))) {
			val = (val + 256)
		}

		state[name] = val
		logDebug "${configParams[name]?.title}(#${configParams[name]?.num}) = ${val}"
	} else {
		logDebug "Parameter #${cmd.parameterNumber} = ${cmd.scaledConfigurationValue}"
	}
}

void zwaveEvent(physicalgraph.zwave.Command cmd) {
	logDebug "Unhandled zwaveEvent: ${cmd}"
}

void refreshSyncStatus() {
	int changes = pendingChanges
	sendEvent(name: "syncStatus", value: (changes ?  "${changes} Pending Changes" : "Synced"), displayed: false)
}

Integer getPendingChanges() {
	int configChanges = configParams.count { name, param ->
		(getSettingVal(name) != getStoredVal(name))
	}
	int pendingWakeUpInterval = (state.wakeUpInterval != wakeUpInterval ? 1 : 0)
	return (configChanges + pendingWakeUpInterval)
}

Integer getSettingVal(String name) {
	Integer value = safeToInt(settings[name], null)
	if ((value == null) && (getStoredVal(name) != null)) {
		return configParams[name].defaultVal
	} else {
		return value
	}
}

Integer getStoredVal(String name) {
	return safeToInt(state[name], null)
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

void logDebug(String msg) {
	if (state.debugLoggingEnabled != false) {
		log.debug "$msg"
	}
}