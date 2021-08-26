/*
 *  Zooz ZSE41 XS Open | Close Sensor
 *
 *  Changelog:
 *
 *    2021-08-25
 *      - Publication Release
 *
 *  Copyright 2021 Zooz
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
	0x30: 1,	// SensorBinary
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

@Field static int accessControl = 6
@Field static int accessControlOpen = 22
@Field static int wakeUpIntervalSeconds = 43200

metadata {
	definition (
		name: "Zooz ZSE41 XS Open Close Sensor",
		namespace: "Zooz",
		author: "Kevin LaFramboise (@krlaframboise)",
		ocfDeviceType:"x.com.st.d.sensor.contact",
		vid: "d94b52ad-a557-373f-8194-b629159dfccd",
		mnmn: "SmartThingsCommunity"
	) {
		capability "Sensor"
		capability "Contact Sensor"
		capability "Battery"
		capability "Refresh"
		capability "Health Check"
		capability "Configuration"
		capability "platemusic11009.firmware"
		capability "platemusic11009.syncStatus"

		fingerprint mfr:"027A", prod:"7000", model:"E001", deviceJoinName: "Zooz ZSE41 XS Open Close Sensor" // zw:Ss2a type:0701 mfr:027A prod:7000 model:E001 ver:1.05 zwv:7.13 lib:03 cc:5E,55,9F,6C sec:86,85,8E,59,72,5A,87,73,80,71,30,70,84,7A
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

		input "debugLogging", "enum",
			title: "Logging:",
			required: false,
			defaultValue: "1",
			options: ["0":"Disabled", "1":"Enabled [DEFAULT]"]
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
		runIn(2, refreshSyncStatus)

		logDebug "updated()..."
		initialize()

		if (pendingChanges) {
			logForceWakeupMessage("The configuration changes will be sent to the device the next time it wakes up.")
		}
	}
}

void initialize() {
	state.debugLoggingEnabled = (safeToInt(settings?.debugLogging, 1) != 0)

	if (!device.currentValue("contact")) {
		sendEvent(name: "contact", value: "open")
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
	List<String> cmds = []

	int changes = pendingChanges
	if (changes) {
		log.warn "Syncing ${changes} Change(s)"
	}

	if (state.pendingRefresh || !device.currentValue("firmwareVersion")) {
		cmds << secureCmd(zwave.batteryV1.batteryGet())
		cmds << secureCmd(zwave.sensorBinaryV1.sensorBinaryGet())
	}

	if (state.pendingRefresh || !device.currentValue("firmwareVersion")) {
		cmds << secureCmd(zwave.versionV1.versionGet())
	}

	if (state.pendingRefresh || (state.wakeUpInterval != wakeUpIntervalSeconds)) {
		logDebug "Changing wake up interval to ${wakeUpIntervalSeconds} seconds"
		cmds << secureCmd(zwave.wakeUpV2.wakeUpIntervalSet(seconds: wakeUpIntervalSeconds, nodeid:zwaveHubNodeId))
		cmds << secureCmd(zwave.wakeUpV2.wakeUpIntervalGet())
	}

	configParams.each { param ->
		Integer storedVal = getParamStoredValue(param.num)
		Integer settingVal = getSettingValue(param.num)
		if ((settingVal != null) && (settingVal != storedVal)) {
			logDebug "CHANGING ${param.name}(#${param.num}) from ${storedVal} to ${settingVal}"
			cmds << secureCmd(zwave.configurationV2.configurationSet(parameterNumber: param.num, size: param.size, scaledConfigurationValue: settingVal))
			cmds << secureCmd(zwave.configurationV2.configurationGet(parameterNumber: param.num))
		}
	}
	state.pendingRefresh = false
	return cmds
}

def ping() {
	logDebug "ping()"
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

String secureCmd(cmd) {
	if (zwaveInfo?.zw?.contains("s")) {
		return zwave.securityV1.securityMessageEncapsulation().encapsulate(cmd).format()
	} else {
		return cmd.format()
	}
}

void sendCommands(List<String> cmds, Integer delay=100) {
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
		cmds << "delay 500"
	} else {
		cmds << secureCmd(zwave.batteryV1.batteryGet())
	}

	cmds << secureCmd(zwave.wakeUpV2.wakeUpNoMoreInformation())
	sendCommands(cmds)
}

void zwaveEvent(physicalgraph.zwave.commands.wakeupv2.WakeUpIntervalReport cmd) {
	logDebug "Wake Up Interval = ${cmd.seconds} seconds"
	state.wakeUpInterval = cmd.seconds
}

void zwaveEvent(physicalgraph.zwave.commands.batteryv1.BatteryReport cmd) {
	int val = (cmd.batteryLevel == 0xFF ? 1 : safeToInt(cmd.batteryLevel))
	if (val > 100) val = 100
	if (val < 1) val = 1
	logDebug "${device.displayName}: battery is ${val}%"
	sendEvent(name: "battery", value: val, unit: "%", isStateChange: true, displayed:true)
}

void zwaveEvent(physicalgraph.zwave.commands.sensorbinaryv1.SensorBinaryReport cmd) {
	logDebug "${cmd}"
	sendContactEvent(cmd.sensorValue)
}

void zwaveEvent(physicalgraph.zwave.commands.notificationv3.NotificationReport cmd) {
	logDebug "${cmd}"
	if (cmd.notificationType == accessControl) {
		sendContactEvent(cmd.event == accessControlOpen)
	}
}

void sendContactEvent(rawVal) {
	sendEventIfNew("contact", (rawVal ? "open" : "closed"))
}

void zwaveEvent(physicalgraph.zwave.commands.versionv1.VersionReport cmd) {
	logDebug "${cmd}"
	sendEvent(name: "firmwareVersion", value: (cmd.applicationVersion + (cmd.applicationSubVersion / 100)))
}

void zwaveEvent(physicalgraph.zwave.commands.configurationv2.ConfigurationReport cmd) {
	runIn(4, refreshSyncStatus)
	Map param = configParams.find { it.num == cmd.parameterNumber }
	if (param) {
		logDebug "${param.name}(#${param.num}) = ${cmd.scaledConfigurationValue}"
		setParamStoredValue(param.num, cmd.scaledConfigurationValue)
	} else {
		logDebug "Unknown Parameter #${cmd.parameterNumber} = ${cmd.scaledConfigurationValue}"
	}
}

void zwaveEvent(physicalgraph.zwave.Command cmd) {
	logDebug "Unhandled zwaveEvent: ${cmd}"
}

void refreshSyncStatus() {
	int changes = pendingChanges
	sendEventIfNew("syncStatus", (changes ?  "${changes} Pending Changes" : "Synced"), false)
}

int getPendingChanges() {
	return safeToInt(configParams.count { 
	((getSettingValue(it.num) != null) && (getSettingValue(it.num) != getParamStoredValue(it.num))) })
}

Integer getSettingValue(int paramNum) {
	return safeToInt(settings ? settings["configParam${paramNum}"] : null, null)
}

Integer getParamStoredValue(int paramNum) {
	return safeToInt(state["configVal${paramNum}"], null)
}

void setParamStoredValue(int paramNum, int value) {
	state["configVal${paramNum}"] = value
}

void sendEventIfNew(String name, value, boolean displayed=true) {
	String desc = "${device.displayName}: ${name} is ${value}"
	if (device.currentValue(name) != value) {
		if (name != "syncStatus") {
			logDebug(desc)
		}
		sendEvent(name: name, value: value, descriptionText: desc, displayed: displayed)
	}
}

List<Map> getConfigParams() {
	return [
		[num: 1, name: "LED Indicator", size: 1, defaultVal: 1, options: [1:"Enabled [DEFAULT]", 0:"Disabled"]],
		[num: 4, name: "Low Battery Alert", size: 1, defaultVal: 20, options: [10:"10%", 20:"20% [DEFAULT]", 30:"30%", 40:"40%", 50:"50%"]],
		[num: 5, name: "Reported State with Magnet Closed", size: 1, defaultVal: 0, options: [0:"Closed [DEFAULT]", 1:"Open"]],
		[num: 6, name: "Contact Open Delay", size: 4, defaultVal: 0, range:"0..3600"],
		[num: 7, name: "Contact Closed Delay", size: 4, defaultVal: 0, range:"0..3600"]
	]
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

boolean isDuplicateCommand(lastExecuted, allowedMil) {
	!lastExecuted ? false : (lastExecuted + allowedMil > new Date().time)
}

void logDebug(String msg) {
	if (state.debugLoggingEnabled != false) {
		log.debug "$msg"
	}
}