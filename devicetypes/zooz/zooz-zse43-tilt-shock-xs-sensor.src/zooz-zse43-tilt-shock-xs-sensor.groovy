/*
 *  Zooz ZSE43 Tilt | Shock XS Sensor
 *
 *  Changelog:
 *
 *    2021-11-25
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
	0x30: 2,	// SensorBinary
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
	ledIndicator: [num:1, title:"LED Indicator", size:1, defaultVal:3, options:[0:"LED off", 1:"Blinks on vibration only", 2:"Blinks for open/close only", 3:"Blinks for any status change [DEFAULT]"]],
	lowBatteryReports: [num:3, title:"Low Battery Reports", size:1, defaultVal:20, options:[10:"10%", 20:"20% [DEFAULT]", 30:"30%", 40:"40%", 50:"50%"]],
	vibrationSensitivity: [num:4, title:"Vibration Sensitivity", size:1, defaultVal:0, options:[0:"High [DEFAULT]", 1:"Medium", 2:"Low"]],
	disableEnableSensors: [num:7, title:"Disable / Enable Sensors", size:1, defaultVal:2, options:[0:"Only tilt sensor enabled", 1:"Only vibration sensor enabled", 2:"Both sensors enabled [DEFAULT]"]]
]

@Field static int contactOnly = 0
@Field static int vibrationOnly = 1
@Field static int accessControl = 6
@Field static int accessControlOpen = 22
@Field static int accessControlClosed = 23
@Field static int homeSecurity = 7
@Field static int homeSecurityVibration = 3
@Field static int sensorTypeContact = 10
@Field static int wakeUpInterval = 43200

metadata {
	definition (
		name: "Zooz ZSE43 Tilt | Shock XS Sensor",
		namespace: "Zooz",
		author: "Kevin LaFramboise (@krlaframboise)",
		ocfDeviceType:"oic.d.sensor",
		vid: "11ae8701-e665-34ea-8b46-3ce2ce15d0f3",
		mnmn: "SmartThingsCommunity"
	) {
		capability "Sensor"
		capability "Acceleration Sensor"
		capability "Contact Sensor"
		capability "Battery"
		capability "Refresh"
		capability "Health Check"
		capability "Configuration"
		capability "platemusic11009.contactVibrationSensor"
		capability "platemusic11009.firmware"
		capability "platemusic11009.syncStatus"

		// zw:Ss2a type:0701 mfr:027A prod:7000 model:E003 ver:1.10 zwv:7.13 lib:03 cc:5E,55,9F,6C sec:86,85,8E,59,72,5A,87,73,80,71,30,70,84,7A
		fingerprint mfr:"027A", prod:"7000", model:"E003", deviceJoinName: "Zooz Tilt | Shock Sensor" // Zooz ZSE43 Tilt | Shock XS Sensor		
	}

	preferences {
		configParams.each { name, param ->
			if (param.options) {
				input name, "enum",
					title: param.title,
					required: false,
					displayDuringSetup: false,
					defaultValue: param.defaultVal,
					options: param.options
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
	logDebug "updated()..."
	initialize()

	if (pendingChanges) {
		logForceWakeupMessage("The setting changes will be sent to the device the next time it wakes up.")
	}
}

void initialize() {
	state.debugLoggingEnabled = (safeToInt(settings?.debugLogging, 1) != 0)
	refreshSyncStatus()

	if (!device.currentValue("checkInterval")) {
		sendEvent([name: "checkInterval", value: ((wakeUpInterval * 2) + (5 * 60)), displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID]])
	}

	if (!device.currentValue("acceleration") || ((device.currentValue("acceleration") == "active") && (getSettingVal("disableEnableSensors") == contactOnly))) {
		sendEvent(name: "acceleration", value: "inactive", displayed:false)
	}

	if (!device.currentValue("contactVibration")) {
		sendEvent(name: "contactVibration", value: "inactive", displayed:false)
	} else {
		sendContactVibrationEvent(device.currentValue("contact"), device.currentValue("acceleration"))
	}
}

def refresh() {
	logDebug "refresh()..."

	if (state.pendingRefresh) {
		sendAccelerationEvent("inactive")
	}

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

	if (state.pendingRefresh || !device.currentValue("battery")) {
		cmds << secureCmd(zwave.batteryV1.batteryGet())
	}

	if (state.pendingRefresh || !device.currentValue("firmwareVersion")) {
		cmds << secureCmd(zwave.versionV1.versionGet())
	}

	if (state.pendingRefresh || !device.currentValue("contact")) {
		cmds << secureCmd(zwave.sensorBinaryV2.sensorBinaryGet(sensorType: sensorTypeContact))
	}

	if (state.wakeUpInterval == null) {
		cmds << secureCmd(zwave.wakeUpV2.wakeUpIntervalGet())
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
	sendHubCommand(cmds, 150)
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

void zwaveEvent(physicalgraph.zwave.commands.sensorbinaryv2.SensorBinaryReport cmd) {
	logDebug "${cmd}"
	if (cmd.sensorType == sensorTypeContact) {
		sendContactEvent(cmd.sensorValue ? "open" : "closed")
	}
}

void zwaveEvent(physicalgraph.zwave.commands.notificationv3.NotificationReport cmd) {
	if (cmd.notificationType == accessControl) {
		if (cmd.event == accessControlOpen) {
			sendContactEvent("open")
		} else if (cmd.event == accessControlClosed) {
			sendContactEvent("closed")
		} else {
			logDebug "${cmd}"
		}
	} else if (cmd.notificationType == homeSecurity) {
		sendAccelerationEvent((cmd.event == homeSecurityVibration) ? "active" : "inactive")
	} else {
		logDebug "${cmd}"
	}
}

void sendContactEvent(String value) {
	logDebug "Contact is ${value}"
	sendEvent(name: "contact", value: value)
	sendContactVibrationEvent(value, device.currentValue("acceleration"))
}

void sendAccelerationEvent(String value) {
	logDebug "Acceleration is ${value}"
	sendEvent(name: "acceleration", value: value)
	sendContactVibrationEvent(device.currentValue("contact"), value)
}

void sendContactVibrationEvent(String contactValue, String vibrationValue) {
	String value
	switch (getSettingVal("disableEnableSensors")) {
		case contactOnly:
			value = contactValue
			break
		case vibrationOnly:
			value = vibrationValue
			break
		default:
			value = "${contactValue}${vibrationValue.capitalize()}"
	}

	if (device.currentValue("contactVibration") != value) {
		sendEvent(name: "contactVibration", value: value, displayed: false)
	}
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
	int configChanges = safeToInt(configParams.count { name, param ->
		(getSettingVal(name) != getStoredVal(name))
	}, 0)
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