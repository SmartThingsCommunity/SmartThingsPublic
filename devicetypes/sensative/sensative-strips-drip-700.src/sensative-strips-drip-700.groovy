/*
 *  Sensative Strips Drip 700 v1.3
 *
 *
 *  Changelog:
 *
 *    1.3 (26/07/2021)
 *      - Remove updateLastCheckIn() and String convertToLocalTimeString(dt)functions based on review and Kevin's input
 *
 *    1.2 (28/06/2021)
 *      - Requested Changes
 *
 *    1.1 (06/06/2021)
 *      - Requested Changes
 *
 *    1.0 (05/12/2021)
 *      - Initial Release
 *
 *
 *  Copyright 2021 Sensative
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
	0x22: 1,	// ApplicationStatus
	0x31: 5,	// Sensor Multilevel (v7)
	0x55: 1,	// Transport Service
	0x59: 1,	// AssociationGrpInfo
	0x5A: 1,	// DeviceResetLocally
	0x5E: 2,	// ZwaveplusInfo
	0x6C: 1,	// Supervision
	0x70: 2,	// Configuration
	0x71: 3,	// Alarm v1 or Notification v4
	0x72: 2,	// ManufacturerSpecific
	0x73: 1,	// Powerlevel
	0x7A: 2,	// FirmwareUpdateMd
	0x80: 1,	// Battery
	0x84: 2,	// WakeUp
	0x85: 2,	// Association
	0x86: 1,	// Version (2)
	0x87: 1,	// Indicator
	0x8E: 2,	// Multi Channel Association
	0x9F: 1 	// Security 2
]

@Field static int wakeUpIntervalSeconds = 43200
@Field static int tempSensorType = 1
@Field static int leakageCalibrationParamNum = 23
@Field static int heatAlarm = 4
@Field static int heatAlarmHigh = 2
@Field static int heatAlarmLow = 6
@Field static int waterAlarm = 5
@Field static int waterAlarmWet = 2
@Field static int homeSecurity = 7
@Field static int homeSecurityTamper = 11

metadata {
	definition (
		name: "Sensative Strips Drip 700",
		namespace: "Sensative",
		author: "Kevin LaFramboise",
		ocfDeviceType:"x.com.st.d.sensor.moisture",
		vid: "480ed59e-91d4-3cfc-a077-b06151590ef0",
		mnmn: "SmartThingsCommunity"
	) {
		capability "Sensor"
		capability "Water Sensor"
		capability "Temperature Measurement"
		capability "Tamper Alert"
		capability "Battery"
		capability "Configuration"
		capability "Refresh"
		capability "Health Check"
		capability "platemusic11009.firmware"
		capability "platemusic11009.temperatureAlarm"		

		fingerprint mfr:"019A", prod:"0004", model:"000B", deviceJoinName: "Strips Drip 700" //Raw Description: zw:Ss2a type:2101 mfr:019A prod:0004 model:000B ver:8.1A zwv:7.13 lib:07 cc:5E,22,55,9F,6C sec:86,85,8E,59,72,31,5A,87,73,80,70,71,84,7A
	}

	preferences {
		configParams.each { param ->
			if (param.options) {
				input "configParam${param.num}", "enum",
					title: "${param.name}:",
					required: false,
					displayDuringSetup: false,
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

		input "debugLogging", "enum",
			title: "Logging:",
			required: false,
			defaultValue: 1,
			options: [0:"Disabled", 1:"Enabled [DEFAULT]"]
	}
}

def installed() {
	logDebug "installed()..."
	state.pendingRefresh = true
	initialize()
}

def updated() {
	if (!isDuplicateCommand(state.lastUpdated, 1000)) {
		state.lastUpdated = new Date().time

		logDebug "updated()..."
		initialize()

		if (!getSettingValue(leakageCalibrationParamNum) && (state.leakageCalibrated != null)) {
			// reset flag so that it performs calibration the next time it's set to true.
			logDebug "Resetting leakage/moisture sensor calibration setting..."
			state.leakageCalibrated = null
		}

		if (pendingChanges) {
			logForceWakeupMessage("The configuration changes will be sent to the device the next time it wakes up.")
		}
	}
}

void initialize() {
	state.debugLoggingEnabled = (safeToInt(settings?.debugOutput, 1) != 0)

	if (!device.currentValue("tamper")) {
		sendEventIfNew("tamper", "clear")
	}

	if (!device.currentValue("water")) {
		sendEventIfNew("water", "dry")
	}

	if (!device.currentValue("temperatureAlarm")) {
		sendEventIfNew("temperatureAlarm", "normal")
	}

	if (!device.currentValue("checkInterval")) {
		sendEvent(name: "checkInterval", value: ((wakeUpIntervalSeconds * 2) + 300), displayed: falsle, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])
	}
}

def configure() {
	logDebug "configure()..."
	state.pendingRefresh = true
	sendCommands(getConfigureCmds())
}

List<String> getConfigureCmds() {
	runIn(6, refreshSyncStatus)

	int changes = pendingChanges
	if (changes) {
		log.warn "Syncing ${changes} Change(s)"
	}

	List<String> cmds = [ ]

	if (state.pendingRefresh) {
		cmds << batteryGetCmd()
		cmds << secureCmd(zwave.versionV1.versionGet())
		cmds << secureCmd(zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType: tempSensorType))
	}

	if (state.pendingRefresh || (state.wakeUpInterval != wakeUpIntervalSeconds)) {
		logDebug "Changing wake up interval to ${wakeUpIntervalSeconds} seconds"
		cmds << secureCmd(zwave.wakeUpV2.wakeUpIntervalSet(seconds:wakeUpIntervalSeconds, nodeid:zwaveHubNodeId))
		cmds << secureCmd(zwave.wakeUpV2.wakeUpIntervalGet())
	}

	configParams.each {
		Integer storedVal = getParamStoredValue(it.num)
		Integer settingVal = getSettingValue(it.num)

		if (it.num != leakageCalibrationParamNum) {
			if ((settingVal != null) && (settingVal != storedVal)) {
				logDebug "CHANGING ${it.name}(#${it.num}) from ${storedVal} to ${settingVal}"
				cmds << configSetCmd(it, settingVal)
				cmds << configGetCmd(it)
			} else if (state.pendingRefresh) {
				cmds << configGetCmd(it)
			}
		} else {
			if (settingVal && !state.leakageCalibrated) {
				logDebug "Performing leakage/moisture sensor calibration..."
				state.leakageCalibrated = false // Indicate that calibration has been started
				cmds << configSetCmd(it, settingVal)
				cmds << configGetCmd(it)
			}
		}
	}

	state.pendingRefresh = false
	return cmds
}

// Required for HealthCheck Capability, but doesn't actually do anything because this device sleeps.
def ping() {
	logDebug "ping()"
}

def refresh() {
	logDebug "refresh()..."
	state.pendingRefresh = true
	logForceWakeupMessage("The device will be refreshed the next time it wakes up.")
}

void logForceWakeupMessage(String msg) {
	log.warn "${msg}  To force the device to wake up immediately, move the magnet towards the round end 3 times."
}

String batteryGetCmd() {
	return secureCmd(zwave.batteryV1.batteryGet())
}

String configSetCmd(Map param, int value) {
	return secureCmd(zwave.configurationV2.configurationSet(parameterNumber: param.num, size: param.size, scaledConfigurationValue: value))
}

String configGetCmd(Map param) {
	return secureCmd(zwave.configurationV2.configurationGet(parameterNumber: param.num))
}

String secureCmd(cmd) {
	try {
		if (zwaveInfo?.zw?.contains("s")) {
			return zwave.securityV1.securityMessageEncapsulation().encapsulate(cmd).format()
		} else {
			return cmd.format()
		}
	} catch (ex) {
		return cmd.format()
	}
}

void sendCommands(List<String> cmds, Integer delay=250) {
	if (cmds) {
		def actions = []
		cmds.each {
			actions << new physicalgraph.device.HubAction(it)
		}
		sendHubCommand(actions, delay)
	}
}

def parse(String description) {
	def cmd = zwave.parse(description, commandClassVersions)
	if (cmd) {
		zwaveEvent(cmd)
	} else {
		log.warn "Unable to parse: $description"
	}    
	return []
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

	cmds += getConfigureCmds()

	if (cmds) {
		cmds << "delay 1000"
	} else {
		cmds << batteryGetCmd()
	}

	cmds << secureCmd(zwave.wakeUpV2.wakeUpNoMoreInformation())
	sendCommands(cmds)
}

void zwaveEvent(physicalgraph.zwave.commands.wakeupv2.WakeUpIntervalReport cmd) {
	logDebug "Wake Up Interval = ${cmd.seconds} seconds"
	state.wakeUpInterval = cmd.seconds
}

void zwaveEvent(physicalgraph.zwave.commands.versionv1.VersionReport cmd) {
	logDebug "${cmd}"
	sendEventIfNew("firmwareVersion", (cmd.applicationVersion + (cmd.applicationSubVersion / 100)))
}

void zwaveEvent(physicalgraph.zwave.commands.batteryv1.BatteryReport cmd) {
	int val = (cmd.batteryLevel == 0xFF ? 1 : safeToInt(cmd.batteryLevel))
	if (val > 100) val = 100
	if (val < 1) val = 1

	String desc = "${device.displayName}: battery is ${val}%"
	logDebug(desc)

	sendEvent(name: "battery", value: val, unit: "%", isStateChange: true, descriptionText: desc)
}

void zwaveEvent(physicalgraph.zwave.commands.configurationv2.ConfigurationReport cmd) {
	runIn(4, refreshSyncStatus)

	Map param = configParams.find { it.num == cmd.parameterNumber }
	if (param) {
		logDebug "${param.name}(#${param.num}) = ${cmd.scaledConfigurationValue}"
		setParamStoredValue(param.num, cmd.scaledConfigurationValue)

		if ((param.num == leakageCalibrationParamNum) && (state.leakageCalibrated == false) && !cmd.scaledConfigurationValue) {
			state.leakageCalibrated = true // calibration was started so indicate it completed to prevent it from being run again.
			logDebug "Leakage/moisture sensor calibration finished..."
		}
	} else {
		logDebug "Unknown Parameter #${cmd.parameterNumber} = ${cmd.scaledConfigurationValue}"
	}
}

void zwaveEvent(physicalgraph.zwave.commands.sensormultilevelv5.SensorMultilevelReport cmd) {
	logDebug "${cmd}"
	if (cmd.sensorType == tempSensorType) {
		def unit = cmd.scale == 1 ? "F" : "C"
		def temp = convertTemperatureIfNeeded(cmd.scaledSensorValue, unit, cmd.precision)
		sendEventIfNew("temperature", temp, true, temperatureScale)
	}
}

void zwaveEvent(physicalgraph.zwave.commands.notificationv3.NotificationReport cmd) {
	logDebug "${cmd}"
	switch (cmd.notificationType) {
		case heatAlarm:
			if ((cmd.event == heatAlarmHigh) || (cmd.eventParameter[0] == heatAlarmHigh)) {
				sendEventIfNew("temperatureAlarm", ((cmd.event == heatAlarmHigh) ? "high" : "normal"))
			} else if ((cmd.event == heatAlarmLow) || (cmd.eventParameter[0] == heatAlarmLow)) {
				sendEventIfNew("temperatureAlarm", ((cmd.event == heatAlarmLow) ? "low" : "normal"))
			}
			break
		case waterAlarm:
			sendEventIfNew("water", ((cmd.event == waterAlarmWet) ? "wet" : "dry"))
			break
		case homeSecurity:
			if ((cmd.event == homeSecurityTamper) || (cmd.eventParameter[0] == homeSecurityTamper)) {
				sendEventIfNew("tamper", ((cmd.event == homeSecurityTamper) ? "detected" : "clear"))
			}
			break
	}
}

void zwaveEvent(physicalgraph.zwave.Command cmd) {
	logDebug "${cmd}"
}

void refreshSyncStatus() {
	int changes = pendingChanges
	sendEventIfNew("syncStatus", (changes ?  "${changes} Pending Changes" : "Synced"), false)
}

int getPendingChanges() {
	int configChanges = safeToInt(configParams.count {
		((it.num != leakageCalibrationParam.num) && (getSettingValue(it.num) != null) && (getSettingValue(it.num) != getParamStoredValue(it.num)))
	}, 0)
	return (configChanges + ((state.wakeUpInterval != wakeUpIntervalSeconds) ? 1 : 0))
}

Integer getSettingValue(int paramNum) {
	return safeToInt((settings ? settings["configParam${paramNum}"] : null), null)
}

Integer getParamStoredValue(int paramNum) {
	return safeToInt(state["configVal${paramNum}"], null)
}

void setParamStoredValue(int paramNum, int value) {
	state["configVal${paramNum}"] = value
}

void sendEventIfNew(String name, value, boolean displayed=true, String unit="") {
	String desc = "${device.displayName}: ${name} is ${value}${unit}"
	if (device.currentValue(name) != value) {
		if (name != "syncStatus") {
			logDebug(desc)
		}

		Map evt = [
			name: name,
			value: value,
			descriptionText: desc,
			displayed: displayed
		]

		if (unit) {
			evt.unit = unit
		}
		sendEvent(evt)
	}
}

List<Map> getConfigParams() {
	return [
		ledAlarmParam,
		tempReportingTypeParam,
		tempAlarmsParam,
		highTempAlarmLevelParam,
		lowTempAlarmLevelParam,
		leakageAlarmParam,
		leakageAlarmLevelParam,
		leakageAlarmIntervalParam,
		activateSupervisionParam,
		leakageCalibrationParam,
		tempOffsetParam,
		tempReportingIntervalParam,
		tempDeltaParam,
		tempHysteresisParam
	]
}

Map getLedAlarmParam() {
	return [num:2, name:"LED alarm event reporting", size:1, options:[0:"Off", 1:"On [DEFAULT]"]]
}

Map getTempReportingTypeParam() {
	return [num:4, name:"Temperature reporting type", size:1, options:[
		0:"Off [DEFAULT]",
		1:"Actual value on Temperature Delta change",
		2:"Actual value at Temperature Reporting Interval",
		3:"Average value every 12 hours"
	]]
}

Map getTempAlarmsParam() {
	return [num:6, name:"Temperature alarms", size:1, options:[0:"Off [DEFAULT]", 1:"On"]]
}

Map getHighTempAlarmLevelParam() {
	return [num:7, name:"High temperature alarm level (°C)", size:1, defaultVal: 40, range:"-20..80"]
}

Map getLowTempAlarmLevelParam() {
	return [num:8, name:"Low temperature alarm level (°C)", size:1, defaultVal: 5, range:"-20..60"]
}

Map getLeakageAlarmParam() {
	return [num:12, name:"Leakage/moisture alarm", size:1, options:[0:"Off", 1:"On [DEFAULT]"]]
}

Map getLeakageAlarmLevelParam() {
	return [num:13, name:"Leakage/moisture alarm level (1:almost dry ~ 100:wet)", size:1, defaultVal: 10, range:"1..100"]
}

Map getLeakageAlarmIntervalParam() {
	return [num:14, name:"Leakage/moisture reporting period (hours)", size:1, defaultVal:0, range:"0..120"]
}

Map getActivateSupervisionParam() {
	return [num:15, name:"Activate Supervision", size:1, options:[0:"Off", 1:"Alarm Report [DEFAULT]", 2:"All Reports"]]
}

Map getLeakageCalibrationParam() {
	return [num:23, name:"Leakage/moisture sensor calibration", size:1, options:[0:"Off [DEFAULT]", 1:"Perform calibration"]]
}

Map getTempOffsetParam() {
	return [num:24, name:"Temperature offset (-10.0°C ~ +10.0°C)", size:1, defaultVal:0, range:"-100..100"]
}

Map getTempReportingIntervalParam() {
	return [num:25, name:"Temperature reporting period (minutes)", size:2, range:"15..1440", defaultVal:1440]
}

Map getTempDeltaParam() {
	return [num:26, name:"Temperature delta (0.5°C ~ 10°C)", size:1, defaultVal: 20, range:"5..100"]
}

Map getTempHysteresisParam() {
	return [num:27, name:"Temperature hysteresis for temperature alarms (0.5°C ~ 10°C)", size:1, defaultVal: 20, range:"5..100"]
}

Integer safeToInt(val, Integer defaultVal=0) {
	if ("${val}"?.isInteger()) {
		return "${val}".toInteger()
	} else if ("${val}".isDouble()) {
		return "${val}".toDouble()?.round()
	} else {
		return  defaultVal
	}
}

boolean isDuplicateCommand(lastExecuted, allowedMil) {
	!lastExecuted ? false : (lastExecuted + allowedMil > new Date().time)
}

void logDebug(String msg) {
	if (state.debugLoggingEnabled != false) {
		log.debug "$msg"
	}
}