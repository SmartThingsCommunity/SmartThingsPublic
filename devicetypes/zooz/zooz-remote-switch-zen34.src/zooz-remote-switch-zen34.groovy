/*
 *  Zooz Remote Switch ZEN34
 *
 *  Changelog:
 *
 *    2021-09-15
 *      - requested change
 *    2021-08-31
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
	0x20: 1,	// Basic
	0x26: 3,	// Switch Multilevel (4)
	0x55: 1,	// Transport Service
	0x59: 1,	// AssociationGrpInfo
	0x5A: 1,	// DeviceResetLocally
	0x5B: 1,	// CentralScene (3)
	0x5E: 2,	// ZwaveplusInfo
	0x6C: 1,	// Supervision
	0x70: 1,	// Configuration
	0x72: 2,	// ManufacturerSpecific
	0x73: 1,	// Powerlevel
	0x7A: 2,	// Firmware Update Md (3)
	0x80: 1,	// Battery
	0x84: 2,	// WakeUp
	0x85: 2,	// Association
	0x86: 1,	// Version (2)
	0x87: 1,	// Indicator
	0x8E: 2,	// MultiChannelAssociation (3)
	0x9F: 1		// Security 2
]

@Field static List<String> supportedButtonValues = ["down","down_hold","down_2x","down_3x","down_4x","down_5x","up","up_hold","up_2x","up_3x","up_4x","up_5x","down_released","up_released"]

@Field static Map configParams = [
	ledMode: [num:1, title:"LED Indicator Mode", size:1, defaultVal:1, options:[0:"Always off", 1:"On when pressed [DEFAULT]", 2:"Always on (upper paddle color)", 3:"Always on (lower paddle color)"]],
	upperPaddleLedColor: [num:2, title:"Upper Paddled LED Indicator Color", size:1, defaultVal:1, options:[0:"White", 1:"Blue [DEFAULT]", 2:"Green", 3:"Red", 4:"Magenta", 5:"Yellow", 6:"Cyan"]],
	lowerPaddleLedColor: [num:3, title:"Lower Paddle LED Indicator Color", size:1, defaultVal:0, options:[0:"White [DEFAULT]", 1:"Blue", 2:"Green", 3:"Red", 4:"Magenta", 5:"Yellow", 6:"Cyan"]]
]

@Field static int wakeUpInterval = 43200
@Field static int btnPushed = 0
@Field static int btnReleased = 1
@Field static int btnHeld = 2
@Field static int btnPushed2x = 3
@Field static int btnPushed6x = 7

metadata {
	definition (
		name:"Zooz Remote Switch ZEN34", 
		namespace:"Zooz", 
		author: "Kevin LaFramboise (krlaframboise)", 
		ocfDeviceType: "x.com.st.d.remotecontroller",
		mnmn: "SmartThingsCommunity",
		vid: "540fce12-499a-3b90-b276-f4159eb55f42"
	) {
		capability "Sensor"
		capability "Battery"
		capability "Button"
		capability "Refresh"
		capability "Configuration"
		capability "Health Check"
		capability "platemusic11009.firmware"
		capability "platemusic11009.syncStatus"

		fingerprint mfr: "027A", prod: "7000", model: "F001", deviceJoinName: "Zooz Remote" //Zooz Remote Switch ZEN34, raw description: zw:Ss2a type:1800 mfr:027A prod:7000 model:F001 ver:1.01 zwv:7.13 lib:03 cc:5E,55,9F,6C sec:86,85,8E,59,72,5A,73,80,5B,70,84,7A
	}

	preferences {
		configParams.each { name, param ->
			input name, "enum",
				title: param.title,
				required: false,
				displayDuringSetup: false,
				defaultValue: param.defaultVal,
				options: param.options
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
	state.refreshAll = true
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
	state.debugLoggingEnabled = (safeToInt(settings?.debugOutput, 1) != 0)

	refreshSyncStatus()

	if (!device.currentValue("checkInterval")) {
		sendEvent([name: "checkInterval", value: ((wakeUpInterval * 2) + (5 * 60)), displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID]])
	}

	if (!device.currentValue("supportedButtonValues")) {
		sendEvent(name:"supportedButtonValues", value: supportedButtonValues.encodeAsJSON(), displayed:false)
	}

	if (!device.currentValue("numberOfButtons")) {
		sendEvent(name:"numberOfButtons", value:1, displayed:false)
	}

	if (!device.currentValue("button")) {
		sendButtonEvent("up")
	}
}

def configure() {
	logDebug "configure()..."
	List<String> cmds = []

	if (state.refreshAll || !device.currentValue("firmwareVersion")) {
		cmds << secureCmd(zwave.versionV1.versionGet())
	}

	if (state.refreshAll || !device.currentValue("battery")) {
		cmds << secureCmd(zwave.batteryV1.batteryGet())
	}

	state.refreshAll = false

	if (state.wakeUpInterval != wakeUpInterval) {
		cmds << secureCmd(zwave.wakeUpV2.wakeUpIntervalSet(seconds: wakeUpInterval, nodeid: zwaveHubNodeId))
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
	if (cmds) {
		sendHubCommand(cmds, 500)
	}
}

def ping() {
	logDebug "ping()..."
}

def refresh() {
	logDebug "refresh()..."
	state.refreshAll = true

	refreshSyncStatus()

	logForceWakeupMessage("The next time the device wakes up, the sensor data will be requested.")
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

void zwaveEvent(physicalgraph.zwave.commands.wakeupv2.WakeUpIntervalReport cmd) {
	logDebug "$cmd"
	runIn(4, refreshSyncStatus)
	state.wakeUpInterval = cmd.seconds
}

void zwaveEvent(physicalgraph.zwave.commands.wakeupv2.WakeUpNotification cmd) {
	logDebug "Device Woke Up..."
	runIn(4, refreshSyncStatus)
	configure()
	sendHubCommand([secureCmd(zwave.wakeUpV2.wakeUpNoMoreInformation())])
}

void zwaveEvent(physicalgraph.zwave.commands.configurationv1.ConfigurationReport cmd) {
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

void zwaveEvent(physicalgraph.zwave.commands.versionv1.VersionReport cmd) {
	logDebug "${cmd}"
	sendEvent(name: "firmwareVersion", value: (cmd.applicationVersion + (cmd.applicationSubVersion / 100)))
}

void zwaveEvent(physicalgraph.zwave.commands.batteryv1.BatteryReport cmd) {
	def val = (cmd.batteryLevel == 0xFF ? 1 : cmd.batteryLevel)
	if (val > 100) {
		val = 100
	}
	logDebug "Battery is ${val}%"
	sendEvent(name:"battery", value:val, unit:"%", isStateChange: true)
}

void zwaveEvent(physicalgraph.zwave.commands.centralscenev1.CentralSceneNotification cmd){
	if (state.lastSequenceNumber != cmd.sequenceNumber) {
		state.lastSequenceNumber = cmd.sequenceNumber

		String paddle = (cmd.sceneNumber == 1) ? "up" : "down"
		String btnVal
		switch (cmd.keyAttributes){
			case btnPushed:
				btnVal = paddle
				break
			case btnReleased:
				logDebug "${paddle}_released is not supported by SmartThings"
				btnVal = paddle + "_released"
				break
			case btnHeld:
				btnVal = paddle + "_hold"
				break
			case { it >= btnPushed2x && it <= btnPushed6x}:
				btnVal = paddle + "_${cmd.keyAttributes - 1}x"
				break
			default:
				logDebug "keyAttributes ${cmd.keyAttributes} not supported"
		}

		if (btnVal) {
			sendButtonEvent(btnVal)
		}
	}
}

void sendButtonEvent(String value) {
	String desc = "${device.displayName} ${value}"
	logDebug(desc)
	sendEvent(name: "button", value: value, data:[buttonNumber: 1], isStateChange: true, descriptionText: desc)
}

void zwaveEvent(physicalgraph.zwave.Command cmd) {
	logDebug "Unhandled zwaveEvent: $cmd"
}

void refreshSyncStatus() {
	int changes = pendingChanges
	sendEvent(name: "syncStatus", value: (changes ?  "${changes} Pending Changes" : "Synced"), displayed: false)
}

void logForceWakeupMessage(String msg) {
	log.warn "${msg}  You can force the device to wake up immediately by tapping the upper paddle 7x."
}

Integer getPendingChanges() {
	int configChanges = safeToInt(configParams.count { name, param ->
		(getSettingVal(name) != getStoredVal(name))
	}, 0)
	int pendingWakeUpInterval = (state.wakeUpInterval != wakeUpInterval ? 1 : 0)
	return (configChanges + pendingWakeUpInterval)
}

Integer getSettingVal(String name) {
	return (settings ? safeToInt(settings[name], null) : null)
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