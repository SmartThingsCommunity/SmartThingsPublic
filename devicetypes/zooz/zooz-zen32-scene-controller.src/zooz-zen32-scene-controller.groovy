/*
*  Zooz ZEN32 Scene Controller
*
*  Changelog:
*
*    2022-03-17
*      - Requested changes
*    2022-02-27
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
	0x20: 1,	// Basic
	0x25: 1,	// Switch Binary
	0x55: 1,	// Transport Service
	0x59: 1,	// AssociationGrpInfo
	0x5A: 1,	// DeviceResetLocally
	0x5B: 1,	// CentralScene (3)
	0x5E: 2,	// ZwaveplusInfo
	0x6C: 1,	// Supervision
	0x70: 1,	// Configuration
	0x7A: 2,	// FirmwareUpdateMd
	0x72: 2,	// ManufacturerSpecific
	0x73: 1,	// Powerlevel
	0x85: 2,	// Association
	0x86: 1,	// Version (2)
	0x87: 1,	// Indicator
	0x8E: 2,	// Multi Channel Association
	0x98: 1,	// Security S0
	0x9F: 1		// Security S2
]

@Field static Map configParams = [
	autoOffTimer: [num:16, title:"Auto Turn-Off Timer (Minutes)", size:4, defaultVal:0, range:"0..65535", desc:"0(disabled), 1..65535(minutes)"],
	autoOnTimer: [num:17, title:"Auto Turn-On Timer (Minutes)", size:4, defaultVal:0, range:"0..65535", desc:"0(disabled), 1..65535(minutes)"],
	statusAfterPowerFailure: [num:18, title:"On Off Status After Power Failure", defaultVal:0, options:[0:"Restore previous state", 1:"Forced off", 2:"Forced on"]],
	relayLoadControl: [num:19, title:"Relay Load Control", defaultVal:1, options:[1:"Enable Switch and Z-Wave", 0:"Disable Switch/ Enable Z-Wave",  2:"Disable Switch and Z-Wave"]],
	disabledRelayBehavior: [num:20, title:"Disabled Relay Load Control Behavior", defaultVal:0, options:[0:"Reports Status / Changes LED", 1:"Doesn't Report Status / Change LED"]],
	threeWaySwitchType: [num:21, title:"3-Way Switch Type", defaultVal:0, options:[0:"Toggle On/Off Switch", 1:"Momentary Switch (ZAC99)"]]
]

@Field static List<Map> buttons = [
	[btnNum: 1, params:[ledMode:[num:2], ledColor:[num:7], ledBrightness:[num:12]]],
	[btnNum: 2, params:[ledMode:[num:3], ledColor:[num:8], ledBrightness:[num:13]]],
	[btnNum: 3, params:[ledMode:[num:4], ledColor:[num:9], ledBrightness:[num:14]]],
	[btnNum: 4, params:[ledMode:[num:5], ledColor:[num:10], ledBrightness:[num:15]]],
	[btnNum: 5, params:[ledMode:[num:1], ledColor:[num:6], ledBrightness:[num:11]]]
]

@Field static Map ledParamOptions = [
	ledMode:[0:"onWhenOff", 1:"onWhenOn", 2:"alwaysOff", 3:"alwaysOn"],
	ledColor:[0:"white", 1:"blue", 2:"green", 3:"red"],
	ledBrightness:[0:"bright", 1:"medium", 2:"low"]
]

@Field static int btnPushed = 0
@Field static int btnReleased = 1
@Field static int btnHeld = 2

metadata {
	definition (
		name: "Zooz ZEN32 Scene Controller",
		namespace: "Zooz",
		author: "Kevin LaFramboise (@krlaframboise)",
		ocfDeviceType: "oic.d.switch",
		mnmn: "SmartThingsCommunity",
		vid: "a0e5a3b8-4dc2-3616-87d1-58a520a2dc52"
	) {
		capability "Actuator"
		capability "Sensor"
		capability "Switch"
		capability "Light"
		capability "Configuration"
		capability "Refresh"
		capability "Health Check"
		capability "Button"
		capability "platemusic11009.firmware"

		// zw:Ls2a type:1000 mfr:027A prod:7000 model:A008 ver:1.01 zwv:7.13 lib:03 cc:5E,55,9F,6C sec:86,25,70,20,5B,85,8E,59,72,5A,73,87,7A
		fingerprint mfr:"027A", prod:"7000", model: "A008", deviceJoinName:"Zooz Switch" // Zooz ZEN32 Scene Controller
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
	}
}

def installed() {
	log.debug "installed()..."
	initialize()
	state.firstRun = true
}

def updated() {
	log.debug "updated()..."
	initialize()

	if (!state.firstRun) {
		executeConfigure()
	} else {
		state.firstRun = false
	}
}

void initialize() {
	if (!device.currentValue("checkInterval")) {
		sendEvent([name: "checkInterval", value: ((60 * 60 * 3) + (5 * 60)), displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID]])
	}

	buttons.each { btn ->
		if (!findChildByButton(btn)) {
			addChildButton(btn)
		}
	}
}

void addChildButton(Map btn) {
	log.debug "Creating Button ${btn.btnNum}"
	try {
		addChildDevice(
			"Zooz",
			"Zooz ZEN32 Scene Controller Button",
			"${device.deviceNetworkId}:${btn.btnNum}",
			device.getHub().getId(),
			[
				completedSetup: true,
				label: "Zooz Button ${btn.btnNum}",
				isComponent: false
			]
		)
	} catch(Exception e) {
		log.warn "${e}"
	}
}

void executeConfigure() {
	List<String> cmds = []

	if (!device.currentValue("switch")) {
		cmds << switchBinaryGetCmd()
	}

	if (!device.currentValue("firmwareVersion")) {
		cmds << versionGetCmd()
	}

	configParams.each { name, param ->
		Integer storedVal = getStoredVal(name)
		Integer settingVal = getSettingVal(name)
		if ((storedVal == null) || (storedVal != settingVal)) {
			if (settingVal != null) {
				log.debug "Changing ${param.title}(#${param.num}) from ${storedVal} to ${settingVal}"
				cmds << configSetCmd(param, settingVal)
			}
			cmds << configGetCmd(param)
		}
	}

	if (cmds) {
		sendHubCommand(cmds, 100)
	}
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

def ping() {
	log.debug "ping()..."
	return [ switchBinaryGetCmd() ]
}

def on() {
	log.debug "on()..."
	return [ switchBinarySetCmd(0xFF) ]
}

def off() {
	log.debug "off()..."
	return [ switchBinarySetCmd(0x00) ]
}

def refresh() {
	log.debug "refresh()..."
	List<String> cmds = [
		switchBinaryGetCmd(),
		versionGetCmd()
	]

	buttons.each { btn ->
		btn.params.each { name, param ->
			cmds << configGetCmd(param)
		}
	}
	sendHubCommand(cmds)
}

void childRefresh(String dni) {
	log.debug "childRefresh(${dni})..."
	Map btn = findButtonByDNI(dni)
	if (btn) {
		List<String> cmds = []
		btn.params.each { name, param ->
			cmds << configGetCmd(param)
		}
		sendHubCommand(cmds)
	}
}

void childSetLedMode(String dni, String mode) {
	log.debug "childSetLedMode(${dni}, ${mode})..."
	Map btn = findButtonByDNI(dni)
	if (btn) {
		mode = mode?.toLowerCase()?.trim()
		Integer value = ledParamOptions.ledMode.find { it.value.toLowerCase() == mode }?.key

		if (value != null) {
			sendConfigCmds(btn.params.ledMode, value)
		} else {
			log.warn "${mode} is not a valid LED Mode"
		}
	}
}

void childSetLedColor(String dni, String color) {
	log.debug "childSetLedColor(${dni}, ${color})..."
	Map btn = findButtonByDNI(dni)
	if (btn) {
		color = color?.toLowerCase()?.trim()
		Integer value = ledParamOptions.ledColor.find { it.value.toLowerCase() == color }?.key

		if (value != null) {
			sendConfigCmds(btn.params.ledColor, value)
		} else {
			log.warn "${color} is not a valid LED Color"
		}
	}
}

void childSetLedBrightness(String dni, String brightness) {
	log.debug "childSetLedBrightness(${dni}, ${brightness})..."
	Map btn = findButtonByDNI(dni)
	if (btn) {
		brightness = brightness?.toLowerCase()?.trim()
		Integer value = ledParamOptions.ledBrightness.find { it.value == brightness }?.key

		if (value != null) {
			sendConfigCmds(btn.params.ledBrightness, value)
		} else {
			log.warn "${brightness} is not a valid LED Brightness"
		}
	}
}

void sendConfigCmds(Map param, int value) {
	sendHubCommand([
		configSetCmd(param, value),
		configGetCmd(param)
	])
}

String versionGetCmd() {
	return secureCmd(zwave.versionV1.versionGet())
}

String switchBinaryGetCmd() {
	return secureCmd(zwave.switchBinaryV1.switchBinaryGet())
}

String switchBinarySetCmd(val) {
	return secureCmd(zwave.switchBinaryV1.switchBinarySet(switchValue: val))
}

String configSetCmd(Map param, int value) {
	int size = (param.size ?: 1)
	return secureCmd(zwave.configurationV1.configurationSet(parameterNumber: param.num, size: size, scaledConfigurationValue: value))
}

String configGetCmd(Map param) {
	return secureCmd(zwave.configurationV1.configurationGet(parameterNumber: param.num))
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

void zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityMessageEncapsulation cmd) {
	def encapsulatedCmd = cmd.encapsulatedCommand(commandClassVersions)
	if (encapsulatedCmd) {
		zwaveEvent(encapsulatedCmd)
	} else {
		log.warn "Unable to extract encapsulated cmd from $cmd"
	}
}

void zwaveEvent(physicalgraph.zwave.commands.configurationv1.ConfigurationReport cmd) {
	int value = cmd.scaledConfigurationValue
	String name = configParams.find { name, param -> param.num == cmd.parameterNumber }?.key
	if (name) {
		state[name] = value
		log.debug "${configParams[name]?.title}(#${configParams[name]?.num}) = ${value}"
	} else {
		handleLedEvent(cmd.parameterNumber, value)
	}
}

void handleLedEvent(int paramNum, int configVal) {
	buttons.each { btn ->
		String name = btn.params.find { it.value.num == paramNum}?.key
		if (name) {
			String value = ledParamOptions[name].get(configVal)
			if (value) {
				log.debug "Button ${btn.btnNum} ${name} is ${value}"
				findChildByButton(btn)?.sendEvent(name: name, value: value)
			}
		}
	}
}

void zwaveEvent(physicalgraph.zwave.commands.versionv1.VersionReport cmd) {
	log.debug "${cmd}"
	sendEvent(name: "firmwareVersion", value: (cmd.applicationVersion + (cmd.applicationSubVersion / 100)))
}

void zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd) {
	sendSwitchEvent(cmd.value)
}

void zwaveEvent(physicalgraph.zwave.commands.switchbinaryv1.SwitchBinaryReport cmd) {
	sendSwitchEvent(cmd.value)
}

void sendSwitchEvent(rawVal) {
	String value = (rawVal ? "on" : "off")
	log.debug "switch is ${value}"
	sendEvent(name: "switch", value: value)
}

void zwaveEvent(physicalgraph.zwave.commands.centralscenev1.CentralSceneNotification cmd){
	if (state.lastSequenceNumber != cmd.sequenceNumber) {
		state.lastSequenceNumber = cmd.sequenceNumber

		Map btn = findButtonByNum(cmd.sceneNumber)
		if (btn) {
			String value
			switch (cmd.keyAttributes){
				case btnPushed:
					value = "pushed"
					break
				case btnReleased:
					log.debug "Button Value 'released' is not supported by SmartThings"
					break
				case btnHeld:
					value = "held"
					break
				default:
					value = "pushed_${cmd.keyAttributes - 1}x"
			}

			if (value) {
				log.debug "button ${btn.btnNum} ${value}"
				findChildByButton(btn)?.sendEvent(name: "button", value: value, data:[buttonNumber: 1], isStateChange: true)
			}
		} else {
			log.debug "Scene ${cmd.sceneNumber} is not a valid Button Number"
		}
	}
}

void zwaveEvent(physicalgraph.zwave.Command cmd) {
	log.debug "Unhandled zwaveEvent: $cmd"
}

def findChildByButton(Map btn) {
	return childDevices?.find { btn == findButtonByDNI(it.deviceNetworkId) }
}

Map findButtonByDNI(String dni) {
	Integer btnNum = safeToInt("${dni}".reverse().take(1), null)
	if (btnNum) {
		return findButtonByNum(btnNum)
	} else {
		log.warn "${dni} is not a valid Button DNI"
	}
}

Map findButtonByNum(Integer btnNum) {
	return buttons.find { it.btnNum == btnNum }
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