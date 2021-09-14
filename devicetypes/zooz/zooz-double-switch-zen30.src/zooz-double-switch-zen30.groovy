/*
 *  Zooz Double Switch ZEN30
 *
 *  Changelog:
 *
 *    2021-08-30
 *      - Requested changes
 *    2021-08-28
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
	0x25: 1,	// SwitchBinary
	0x26: 3,	// SwitchMultilevel
	0x55: 1,	// TransportService
	0x59: 1,	// AssociationGrpInfo
	0x5A: 1,	// DeviceResetLocally
	0x5B: 1,	// CentralScene
	0x5E: 2,	// ZwaveplusInfo
	0x60: 3,	// MultiChannel
	0x6C: 1,	// Supervision
	0x70: 1,	// Configuration
	0x7A: 2,	// FirmwareUpdateMd
	0x72: 2,	// ManufacturerSpecific
	0x73: 1,	// Powerlevel
	0x85: 2,	// Association
	0x86: 1,	// Version
	0x8E: 2,	// MultiChannelAssociation
	0x98: 1,	// Security S0
	0x9F: 1		// Security S2
]

@Field static int supervisionCC = 108
@Field static int upperPaddle = 1
@Field static int lowerPaddle = 2
@Field static int relayButton = 3
@Field static int btnPushed = 0
@Field static int btnReleased = 1
@Field static int btnHeld = 2
@Field static Map endpoints = [dimmer: 0, relay: 1]

@Field static List<String> supportedButtonValues = ["pushed","held","pushed_2x","pushed_3x","pushed_4x","pushed_5x","down","down_hold","down_2x","down_3x","down_4x","down_5x","up","up_hold","up_2x","up_3x","up_4x","up_5x"]

@Field static Map configParams = [
	powerFailureParam: [num:12, title:"On Oﬀ Status After Power Failure", size:1, defaultVal:3, options:[0:"Dimmer Off / Relay Off", 1:"Dimmer Off / Relay On", 2:"Dimmer On / Relay Off", 3:"Dimmer Remember / Relay Remember [DEFAULT]", 4:"Dimmer Remember / Relay On", 5:"Dimmer Remember / Relay Off", 6:"Dimmer On / Relay Remember", 7:"Dimmer Off / Relay Remember", 8:"Dimmer On / Relay On"]],
	ledSceneControlParam: [num:7, title:"LED Indicator Mode for Scene Control", size:1, defaultVal:1, options:[0:"LED Enabled", 1:"LED Disabled [DEFAULT]"]],
	relayLedModeParam: [num:2, title:"Relay LED Indicator Mode", size:1, defaultVal:0, options:[0:"On When Off [DEFAULT]", 1:"On When On", 2:"Always Off", 3:"Always On"]],
	relayLedColorParam: [num:4, title:"Relay LED Indicator Color", size:1, defaultVal:0, options:[0:"White [DEFAULT]", 1:"Blue", 2:"Green", 3:"Red"]],
	relayLedBrightnessParam: [num:6, title:"Relay LED Indicator Brightness", size:1, defaultVal:1, options:[0:"100%", 1:"60% [DEFAULT]", 2:"30%"]],
	relayAutoOffParam: [num:10, title:"Relay Auto Turn-Off Timer (Minutes)", size:4, defaultVal:0, range:"0..65535"],
	relayAutoOnParam: [num:11, title:"Relay Auto Turn-On Timer (Minutes)", size:4, defaultVal:0, range:"0..65535"],
	relayLoadControlParam: [num:20, title:"Relay Load Control", size:1, defaultVal:1, options:[0:"Physical Disabled", 1:"Physical / Digital Enabled [DEFAULT]", 2:"Physical / Digital Disabled"]],
	relayPhysicalDisabledBehaviorParam: [num:25, title:"Relay Physical Disabled Behavior [FIRMWARE >= 1.05]", size:1, defaultVal:0, options:[0:"Change Status/LED [DEFAULT]", 1:"Don't Change Status/LED"], minFirmware: 1.05],
	dimmerLedModeParam: [num:1, title:"Dimmer LED Indicator Mode", size:1, defaultVal:0, options:[0:"On When Off [DEFAULT]", 1:"On When On", 2:"Always Off", 3:"Always On"]],
	dimmerLedColorParam: [num:3, title:"Dimmer LED Indicator Color", size:1, defaultVal:0, options:[0:"White [DEFAULT]", 1:"Blue", 2:"Green", 3:"Red"]],
	dimmerLedBrightnessParam: [num:5, title:"Dimmer LED Indicator Brightness", size:1, defaultVal:1, options:[0:"100%", 1:"60% [DEFAULT]", 2:"30%"]],
	dimmerAutoOffParam: [num:8, title:"Dimmer Auto Turn-Off Timer (Minutes)", size:4, defaultVal:0, range:"0..65535"],
	dimmerAutoOnParam: [num:9, title:"Dimmer Auto Turn-On Timer (Minutes)", size:4, defaultVal:0, range:"0..65535"],
	dimmerRampRateParam: [num:13, title:"Dimmer Physical Ramp Rate (Seconds)", size:1, defaultVal:1, range:"0..99"],
	dimmerPaddleHeldRampRateParam: [num:21, title:"Dimming Speed when Paddle is Held (Seconds)", size:1, defaultVal:4, range:"1..99"],
	dimmerMinimumBrightnessParam: [num:14, title:"Dimmer Minimum Brightness (%)", size:1, defaultVal:1, range:"1..99"],
	dimmerMaximumBrightnessParam: [num:15, title:"Dimmer Maximum Brightness (%)", size:1, defaultVal:99, range:"1..99"],
	dimmerCustomBrightnessParam: [num:23, title:"Custom Brightness (%)", size:1, defaultVal:0, range:"0..99"],
	dimmerBrightnessControlParam: [num:18, title:"Dimmer Brightness Control", size:1, defaultVal:0, options:[0:"Double Tap Maximum [DEFAULT]", 1:"Single Tap Custom", 2:"Single Tap Maximum"]],
	dimmerDoubleTapFunctionParam: [num:17, title:"Dimmer Double Tap Function", size:1, defaultVal:0, options:[0:"Turn on Full Brightness [DEFAULT]", 1:"Turn on Maximum Brightness"]],
	dimmerLoadControlParam: [num:19, title:"Dimmer Load Control", size:1, defaultVal:1, options:[0:"Physical Disabled", 1:"Physical / Digital Enabled [DEFAULT]", 2:"Physical / Digital Disabled"]],
	dimmerPhysicalDisabledBehaviorParam: [num:24, title:"Dimmer Physical Disabled Behavior [FIRMWARE >= 1.05]", size:1, defaultVal:0, options:[0:"Change Status/LED [DEFAULT]", 1:"Don't Change Status/LED"], minFirmware:1.05],
	dimmerNightModeBrightnessParam: [num:26, title:"Night Mode Brightness (%) [FIRMWARE >= 1.05]", size:1, defaultVal:20, range:"0..99", minFirmware:1.05],
	dimmerPaddleControlParam: [num:27, title:"Paddle Orientation for Dimmer [FIRMWARE >= 1.05]", size:1, defaultVal:0, options:[0:"Normal [DEFAULT]", 1:"Reverse", 2:"Toggle"], minFirmware:1.05]
]

metadata {
	definition (
		name: "Zooz Double Switch ZEN30",
		namespace: "Zooz",
		author: "Kevin LaFramboise (@krlaframboise)",
		ocfDeviceType: "oic.d.light",
		mnmn: "SmartThingsCommunity",
		vid: "8e189c52-eb8b-36e4-b9e2-2ba459caa6af"
	) {
		capability "Actuator"
		capability "Sensor"
		capability "Switch"
		capability "Switch Level"
		capability "Configuration"
		capability "Refresh"
		capability "Health Check"
		capability "Button"
		capability "platemusic11009.firmware"
		capability "platemusic11009.syncStatus"
		
		//zw:Ls2 type:1101 mfr:027A prod:A000 model:A008 ver:2.00 zwv:5.03 lib:03 cc:5E,6C,55,9F sec:86,26,25,85,8E,59,72,5A,73,5B,60,70,7A epc:1
		fingerprint mfr: "027A", prod: "A000", model: "A008", deviceJoinName: "Zooz Switch" //Zooz Double Switch ZEN30
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
			} else if (param.range) {
				input name, "number",
					title: param.title,
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
	initialize()
}

def updated() {
	logDebug "updated()..."
	initialize()
	configure()
}

void initialize() {
	state.debugLoggingEnabled = (safeToInt(settings?.debugLogging, 1) != 0)

	refreshSyncStatus()

	if (!device.currentValue("checkInterval")) {
		sendEvent([name: "checkInterval", value: ((60 * 60 * 3) + (5 * 60)), displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID]])
	}

	if (!device.currentValue("supportedButtonValues")) {
		sendEvent(name:"supportedButtonValues", value:supportedButtonValues.encodeAsJSON(), displayed:false)
	}

	if (!device.currentValue("numberOfButtons")) {
		sendEvent(name:"numberOfButtons", value:1, displayed:false)
	}

	if (!device.currentValue("button")) {
		sendButtonEvent("pushed")
	}

	if (!childDevices) {
		addChildDevice(
			"smartthings",
			"Child Switch",
			"${device.deviceNetworkId}:${endpoints.relay}",
			null,
			[
				completedSetup: true,
				label: "${device.displayName} Relay",
				isComponent: false
			]
		)
		refresh()
	}
}

def configure() {
	logDebug "configure()..."
	List<String> cmds = []
	BigDecimal firmware = safeToDec(device.currentValue("firmwareVersion"), 0.0)

	if (device.currentValue("firmwareVersion") == null) {
		cmds << secureCmd(zwave.versionV1.versionGet())
	}

	configParams.each { name, param ->
		if (firmwareSupportsParam(firmware, param)) {
			Integer storedVal = getStoredVal(name)
			Integer settingVal = getSettingVal(name)
			if (storedVal != settingVal) {
				logDebug "Changing ${param.title}(#${param.num}) from ${storedVal} to ${settingVal}"
				cmds << secureCmd(zwave.configurationV1.configurationSet(parameterNumber: param.num, size: param.size, scaledConfigurationValue: settingVal))
				cmds << secureCmd(zwave.configurationV1.configurationGet(parameterNumber: param.num))
			}
		}
	}
	if (cmds) {
		sendHubCommand(cmds, 500)
	}
}

def ping() {
	logDebug "ping()..."
	return [ multiChannelCmdEncapCmd(zwave.switchMultilevelV3.switchMultilevelGet(), endpoints.dimmer) ]
}

def on() {
	logDebug "on()..."
	return getSetLevelCmds(state.lastLevel)
}

def off() {
	logDebug "off()..."
	return getSetLevelCmds(0x00)
}

def setLevel(level, duration=null) {
	logDebug "setLevel($level, $duration)..."
	return getSetLevelCmds(level, duration)
}

List<String> getSetLevelCmds(level, duration=null) {
	state.expectedLevel = level
	def levelVal = validateRange(level, 99, 0, 99)
	def durationVal = validateRange(duration, 1, 0, 30)
	return [
		multiChannelCmdEncapCmd(zwave.switchMultilevelV3.switchMultilevelSet(dimmingDuration: durationVal, value: levelVal), endpoints.dimmer)
	]
}

def refresh() {
	logDebug "refresh()..."
	refreshSyncStatus()

	if (device.currentValue("syncStatus") != "Synced") {
		configure()
	}

	return sendHubCommand([
		multiChannelCmdEncapCmd(zwave.switchMultilevelV3.switchMultilevelGet(), endpoints.dimmer),
		multiChannelCmdEncapCmd(zwave.switchBinaryV1.switchBinaryGet(), endpoints.relay),
		secureCmd(zwave.versionV1.versionGet())
	], 500)
}

def childOn(dni) {
	logDebug "childOn(${dni})..."
	sendHubCommand([
		multiChannelCmdEncapCmd(zwave.switchBinaryV1.switchBinarySet(switchValue: 0xFF), endpoints.relay)
	])
}

def childOff(dni) {
	logDebug "childOff(${dni})..."
	sendHubCommand([
		multiChannelCmdEncapCmd(zwave.switchBinaryV1.switchBinarySet(switchValue: 0x00), endpoints.relay)
	])
}

String multiChannelCmdEncapCmd(cmd, endpoint) {
	if (endpoint) {
		return secureCmd(zwave.multiChannelV3.multiChannelCmdEncap(destinationEndPoint:safeToInt(endpoint)).encapsulate(cmd))
	} else {
		return secureCmd(cmd)
	}
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
	return []
}

void zwaveEvent(physicalgraph.zwave.commands.multichannelv3.MultiChannelCmdEncap cmd) {
	// Workaround that was added to all SmartThings Multichannel DTHs.
	if ((cmd.commandClass == supervisionCC) && (cmd.parameter.size >= 4)) { // Supervision encapsulated Message
		// Supervision header is 4 bytes long, two bytes dropped here are the latter two bytes of the supervision header
		cmd.parameter = cmd.parameter.drop(2)
		// Updated Command Class/Command now with the remaining bytes
		cmd.commandClass = cmd.parameter[0]
		cmd.command = cmd.parameter[1]
		cmd.parameter = cmd.parameter.drop(2)
	}

	def encapsulatedCommand = cmd.encapsulatedCommand(commandClassVersions)
	if (encapsulatedCommand) {
		zwaveEvent(encapsulatedCommand, cmd.sourceEndPoint)
	} else {
		logDebug "Unable to get encapsulated command: $cmd"
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

void zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd, endpoint=0) {
	logDebug "${cmd} (${endpoint})"
	sendSwitchEvents(cmd.value, endpoint)
}

void zwaveEvent(physicalgraph.zwave.commands.switchbinaryv1.SwitchBinaryReport cmd, endpoint=0) {
	logDebug "${cmd} (${endpoint})"
	sendSwitchEvents(cmd.value, endpoint)
}

void zwaveEvent(physicalgraph.zwave.commands.switchmultilevelv3.SwitchMultilevelReport cmd, endpoint=0) {
	logDebug "${cmd} (${endpoint})"
	sendSwitchEvents(cmd.value, endpoint)
}

void sendSwitchEvents(rawVal, Integer endpoint) {
	String switchVal = rawVal ? "on" : "off"
	if (endpoint == endpoints.dimmer) {
		logDebug "switch is ${switchVal}"
		sendEvent(name: "switch", value: switchVal)

		int level = (state.expectedLevel == 100 ? 100 : rawVal)
		sendEvent(name: "level", value: level, unit: "%")
		if (level > 0) {
			state.lastLevel = level
		}
		state.expectedLevel = null
	} else {
		def child = childDevices[0]
		if ((child != null) && (child.currentValue("switch") != switchVal)) {
			logDebug "${child.displayName} switch is ${switchVal}"
			child.sendEvent(name: "switch", value: switchVal)
		}
	}
}

void zwaveEvent(physicalgraph.zwave.commands.centralscenev1.CentralSceneNotification cmd) {
	if (state.lastSequenceNumber != cmd.sequenceNumber) {
		state.lastSequenceNumber = cmd.sequenceNumber

		String actionType
		String btnVal
		String displayName = ""

		switch (cmd.sceneNumber) {
			case upperPaddle:
				actionType = "up"
				break
			case lowerPaddle:
				actionType = "down"
				break
			case relayButton:
				actionType = "pushed"
				displayName = "${childDevices[0]?.displayName} "
		}

		switch (cmd.keyAttributes){
			case btnPushed:
				btnVal = actionType
				break
			case btnReleased:
				// btnVal = (cmd.sceneNumber == relayButton) ? "released" : "${actionType}_released"
				logDebug "Button Value 'released' is not supported by SmartThings"
				break
			case btnHeld:
				btnVal = (actionType == "pushed") ? "held" : "${actionType}_hold"
				break
			default:
				btnVal = "${actionType}_${cmd.keyAttributes - 1}x"
		}

		if (btnVal) {
			logDebug "${displayName} Button ${btnVal}"
			sendButtonEvent(btnVal)
		}
	}
}

void sendButtonEvent(String value) {
	sendEvent(name: "button", value: value, data:[buttonNumber: 1], isStateChange: true)
}

void zwaveEvent(physicalgraph.zwave.Command cmd) {
	logDebug "Unhandled zwaveEvent: $cmd"
}

void refreshSyncStatus() {
	int changes = pendingChanges
	sendEvent(name: "syncStatus", value: (changes ?  "${changes} Pending Changes" : "Synced"), displayed: false)
}

Integer getPendingChanges() {
	BigDecimal firmware = safeToDec(device.currentValue("firmwareVersion"), 0.0)
	return configParams.count { name, param ->
		((firmwareSupportsParam(firmware, param)) && (getSettingVal(name) != getStoredVal(name)))
	}
}

Integer getSettingVal(String name) {
	return (settings ? safeToInt(settings[name], null) : null)
}

Integer getStoredVal(String name) {
	return safeToInt(state[name], null)
}

boolean firmwareSupportsParam(BigDecimal firmware, Map param) {
	return (firmware >= safeToDec(param.minFirmware, 0.0))
}

Integer validateRange(val, Integer defaultVal, Integer lowVal, Integer highVal) {
	Integer intVal = safeToInt(val, defaultVal)
	if (intVal > highVal) {
		return highVal
	} else if (intVal < lowVal) {
		return lowVal
	} else {
		return intVal
	}
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

void logDebug(String msg) {
	if (state.debugLoggingEnabled != false) {
		log.debug "$msg"
	}
}