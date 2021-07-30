/*
 *  Sensative Strips Guard 700 v1.2
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
@Field static int accessControlClosed = 23
@Field static int homeSecurity = 7
@Field static int homeSecurityOpen = 2
@Field static int homeSecurityTamper = 11
@Field static int wakeUpIntervalSeconds = 43200

metadata {
	definition (
		name: "Sensative Strips Guard 700",
		namespace: "Sensative",
		author: "Kevin LaFramboise",
		ocfDeviceType:"x.com.st.d.sensor.contact",
		mnmn: "SmartThingsCommunity",
		vid: "6d19b679-a36a-327f-809d-163f8b8d54d9"
	) {
		capability "Sensor"
		capability "Contact Sensor"
		capability "Tamper Alert"
		capability "Battery"
		capability "Configuration"
		capability "Refresh"
		capability "Health Check"
		capability "platemusic11009.firmware"		

		fingerprint mfr:"019A", prod:"0004", model:"0004", deviceJoinName: "Strips Guard 700" //Raw Description: zw:Ss2a type:0701 mfr:019A prod:0004 model:0004 ver:8.1A zwv:7.13 lib:07 cc:5E,22,55,9F,6C sec:86,85,8E,59,72,30,5A,87,73,80,70,71,84,7A
	}

	preferences {
		configParams.each { param ->
			input "configParam${param.num}", "enum",
				title: "${param.name}:",
				required: false,
				displayDuringSetup: false,
				options: param.options
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

	if (!device.currentValue("contact")) {
		sendEventIfNew("contact", "open")
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
		cmds << secureCmd(zwave.sensorBinaryV1.sensorBinaryGet())
	}

	if (state.pendingRefresh || (state.wakeUpInterval != wakeUpIntervalSeconds)) {
		logDebug "Changing wake up interval to ${wakeUpIntervalSeconds} seconds"
		cmds << secureCmd(zwave.wakeUpV2.wakeUpIntervalSet(seconds:wakeUpIntervalSeconds, nodeid:zwaveHubNodeId))
		cmds << secureCmd(zwave.wakeUpV2.wakeUpIntervalGet())
	}

	configParams.each {
		Integer storedVal = getParamStoredValue(it.num)
		Integer settingVal = getSettingValue(it.num)

		if ((settingVal != null) && (settingVal != storedVal)) {
			logDebug "CHANGING ${it.name}(#${it.num}) from ${storedVal} to ${settingVal}"
			cmds << secureCmd(zwave.configurationV2.configurationSet(parameterNumber: it.num, size: it.size, scaledConfigurationValue: settingVal))
			cmds << configGetCmd(it)
		} else if (state.pendingRefresh) {
			cmds << configGetCmd(it)
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
	} else {
		logDebug "Unknown Parameter #${cmd.parameterNumber} = ${cmd.scaledConfigurationValue}"
	}
}

void zwaveEvent(physicalgraph.zwave.commands.sensorbinaryv1.SensorBinaryReport cmd) {
	logDebug "${cmd}"
	sendContactEvent(cmd.sensorValue)
}

void zwaveEvent(physicalgraph.zwave.commands.notificationv3.NotificationReport cmd) {
	logDebug "${cmd}"
	switch (cmd.notificationType) {
		case accessControl:
			if ((cmd.event == accessControlOpen) || (cmd.event == accessControlClosed)) {
				sendContactEvent(cmd.event == accessControlOpen)
			}
			break
		case homeSecurity:
			if ((cmd.event == homeSecurityTamper) || (cmd.eventParameter[0] == homeSecurityTamper)) {
				sendTamperEvent(cmd.event == homeSecurityTamper)
			} else if ((cmd.event == homeSecurityOpen) || (cmd.eventParameter[0] == homeSecurityOpen)) {
				sendContactEvent(cmd.event == homeSecurityOpen)
			}
			break
	}
}

void sendContactEvent(rawVal) {
	sendEventIfNew("contact", (rawVal ? "open" : "closed"))
}

void sendTamperEvent(rawVal) {
	sendEventIfNew("tamper", (rawVal ? "detected" : "clear"))
}

void zwaveEvent(physicalgraph.zwave.Command cmd) {
	logDebug "${cmd}"
}

void refreshSyncStatus() {
	int changes = pendingChanges
	sendEventIfNew("syncStatus", (changes ?  "${changes} Pending Changes" : "Synced"), false)
}

int getPendingChanges() {
	return safeToInt(configParams.count { ((getSettingValue(it.num) != null) && (getSettingValue(it.num) != getParamStoredValue(it.num))) }) + ((state.wakeUpInterval != wakeUpIntervalSeconds) ? 1 : 0)
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
		ledAlarmParam,
		activateSupervisionParam
	]
}

Map getLedAlarmParam() {
	return [num: 2, name: "LED alarm event reporting", size: 1, options: [0: "Turns off LED for door open events", 1:"On [DEFAULT]"]]
}

Map getActivateSupervisionParam() {
	return [num:15, name:"Activate Supervision", size:1, options:[0:"Off", 1:"Alarm Report [DEFAULT]", 2:"All Reports"]]
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