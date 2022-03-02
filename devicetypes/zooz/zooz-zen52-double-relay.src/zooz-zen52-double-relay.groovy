/*
 *  Zooz ZEN52 Double Relay
 *
 *  Changelog:
 *
 *    2022-03-02.2
 *      - Removed central scene setting
 *    2022-03-02
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
	0x22: 1,	// ApplicationStatus
	0x25: 1,	// SwitchBinary
	0x55: 1,	// TransportService
	0x59: 1,	// AssociationGrpInfo
	0x5A: 1,	// DeviceResetLocally
	0x5B: 1,	// CentralScene
	0x5E: 2,	// ZwaveplusInfo
	0x60: 3,	// MultiChannel
	0x6C: 1,	// Supervision
	0x70: 1,	// Configuration
	0x72: 2,	// ManufacturerSpecific
	0x73: 1,	// Powerlevel
	0x7A: 2,	// FirmwareUpdateMd
	0x85: 2,	// Association
	0x86: 1,	// Version
	0x87: 2,	// Indicator (3)
	0x8E: 2,	// MultiChannelAssociation
	0x98: 1,	// Security S0
	0x9F: 1		// Security S2
]

@Field static int supervisionCC = 108
@Field static int btnPushed = 0
@Field static int btnReleased = 1
@Field static int btnHeld = 2
@Field static int mainEndpoint = 0
@Field static List<Integer> relayEndpoints = [1, 2]
@Field static List<String> supportedButtonValues = ["pushed","held","pushed_2x","pushed_3x","pushed_4x","pushed_5x"]

@Field static Map configParams = [
	ledIndicator: [num:2, title:"Led indicator", size:1, defaultVal:1, options:[0:"Disabled", 1:"Enabled"]],
	relay1AutoOff: [num:3, title:"Relay 1 Auto Off Timer", size:2, defaultVal:0, range:"0..65535", desc:"0(disabled), 1..65535(timer unit)"],
	relay1AutoOn: [num:4, title:"Relay 1 Auto On Timer", size:2, defaultVal:0, range:"0..65535", desc:"0(disabled), 1..65535(timer unit)"],
	relay1TimerUnit: [num:7, title:"Relay 1 Timer Unit", size:1, defaultVal:1, options:[1:"Minutes", 2:"Seconds"]],
	relay1StatusAfterPowerFailure: [num:14, title:"Relay 1 Status After Power Failure", size:1, defaultVal:2, options:[0:"Forced off", 1:"Forced on", 2:"Restore previous state"]],
	relay1LoadControl: [num:17, title:"Relay 1 Load Control", size:1, defaultVal:1, options:[0:"Disable Switch/ Enable Z-Wave", 1:"Enable Switch and Z-Wave", 2:"Disable Switch and Z-Wave"]],
	relay1SwitchType: [num:20, title:"Relay 1 Switch Type", size:1, defaultVal:2, options:[0:"Toggle Switch", 1:"Momentary Light Switch", 2:"Toggle Up On/Down Off", 3:"3-way Impulse Control", 4:"Garage Door Mode"]],
	relay1ImpulseDuration: [num:22, title:"Relay 1 Impulse Duration for 3-way", size:1, defaultVal:10, range:"2..200", desc:"2..200"],
	relay2AutoOff: [num:5, title:"Relay 2 Auto Off Timer", size:2, defaultVal:0, range:"0..65535", desc:"0(disabled), 1..65535(timer unit)"],
	relay2AutoOn: [num:6, title:"Relay 2 Auto On Timer", size:2, defaultVal:0, range:"0..65535", desc:"0(disabled), 1..65535(timer unit)"],
	relay2TimerUnit: [num:8, title:"Relay 2 Timer Unit", size:1, defaultVal:1, options:[1:"Minutes", 2:"Seconds"]],
	relay2StatusAfterPowerFailure: [num:15, title:"Relay 2 Status After Power Failure", size:1, defaultVal:2, options:[0:"Forced off", 1:"Forced on", 2:"Restore previous state"]],
	relay2LoadControl: [num:18, title:"Relay 2 Load Control", size:1, defaultVal:1, options:[0:"Disable Switch/ Enable Z-Wave", 1:"Enable Switch and Z-Wave", 2:"Disable Switch and Z-Wave"]],
	relay2SwitchType: [num:21, title:"Relay 2 Switch Type", size:1, defaultVal:2, options:[0:"Toggle Switch", 1:"Momentary Light Switch", 2:"Toggle Up On/Down Off", 3:"3-way Impulse Control", 4:"Garage Door Mode"]],
	relay2ImpulseDuration: [num:23, title:"Relay 2 Impulse Duration for 3-way", size:1, defaultVal:10, range:"2..200", desc:"2..200"]
]

metadata {
	definition (
		name: "Zooz ZEN52 Double Relay",
		namespace: "Zooz",
		author: "Kevin LaFramboise (@krlaframboise)",
		ocfDeviceType: "oic.d.light", 
		mnmn: "SmartThings", 
		vid: "generic-switch"
	) {
		capability "Actuator"
		capability "Switch"
		capability "Refresh"
		capability "Health Check"

		// zw:Ls2a type:1000 mfr:027A prod:0104 model:0202 ver:1.11 zwv:7.15 lib:03 cc:5E,55,9F,6C,22 sec:25,70,85,59,8E,86,72,5A,73,7A,60,5B,87 epc:2
		fingerprint mfr: "027A", prod: "0104", model: "0202", deviceJoinName: "Zooz Switch" // Zooz ZEN52 Double Relay
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
	state.firstConfig = true
}

def updated() {
	log.debug "updated()..."
	initialize()

	if (!state.firstConfig) {
		configure()
	} else {
		state.firstConfig = false
	}
}

void initialize() {
	if (!device.currentValue("checkInterval")) {
		sendEvent([name: "checkInterval", value: ((60 * 60 * 3) + (5 * 60)), displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID]])
	}

	relayEndpoints.each { endpoint ->
		if (!findChildByEndpoint(endpoint)) {
			String dni = buildChildDNI(endpoint)
			def child
			try {
				child = createChildDevice(endpoint, dni, "Zooz", "Zooz Child Switch Button")
				child.sendEvent(name: "supportedButtonValues", value: supportedButtonValues.encodeAsJSON(), displayed: false)
				child.sendEvent(name:"numberOfButtons", value:1, displayed:false)
				sendButtonEvent(child, "pushed")
			} catch(e) {
				log.warn "${e}"
			}

			if (child) {
				childRefresh(child.deviceNetworkId)
			}
		}
	}
}

def createChildDevice(int endpoint, String dni, String dthNamespace, String dthName) {
	return addChildDevice(
		dthNamespace,
		dthName,
		dni,
		device.getHub().getId(),
		[
			completedSetup: true,
			label: "Zooz Switch ${endpoint}",
			isComponent: false
		]
	)
}

def configure() {
	log.debug "configure()..."
	List<String> cmds = []

	if (device.currentValue("switch") == null) {
		cmds << switchBinaryGetCmd(mainEndpoint)
	}

	configParams.each { name, param ->
		Integer storedVal = getStoredVal(name)
		Integer settingVal = getSettingVal(name)
		if (storedVal != settingVal) {
			log.debug "Changing ${param.title}(#${param.num}) from ${storedVal} to ${settingVal}"
			cmds << secureCmd(zwave.configurationV1.configurationSet(parameterNumber: param.num, size: param.size, scaledConfigurationValue: settingVal))
			cmds << secureCmd(zwave.configurationV1.configurationGet(parameterNumber: param.num))
		}
	}
	if (cmds) {
		sendHubCommand(cmds, 500)
	}
}

def on() {
	log.debug "on()..."
	executeOnOffCmds(0xFF, mainEndpoint)
}

def off() {
	log.debug "off()..."
	executeOnOffCmds(0x00, mainEndpoint)
}

void childOn(String dni) {
	executeOnOffCmds(0xFF, getEndpointFromDNI(dni))
}

void childOff(String dni) {
	executeOnOffCmds(0x00, getEndpointFromDNI(dni))
}

void executeOnOffCmds(int value, endpoint) {
	List<String> cmds = [
		multiChannelCmdEncapCmd(zwave.switchBinaryV1.switchBinarySet(switchValue: value), endpoint)
	]

	// Workaround for unreliable automatic reports.
	if (endpoint == mainEndpoint) {
		cmds += getRefreshRelaysCmds()
	} else {
		cmds << switchBinaryGetCmd(endpoint)
	}

	sendHubCommand(cmds)
}

List<String> getRefreshRelaysCmds() {
	List<String> cmds = []
	relayEndpoints.each { endpoint ->
		cmds << switchBinaryGetCmd(endpoint)
	}
	return cmds
}

def ping() {
	log.debug "ping()..."
	return [ switchBinaryGetCmd(mainEndpoint) ]
}

def refresh() {
	log.debug "refresh()..."
	sendHubCommand(getRefreshRelaysCmds(), 500)
}

void childRefresh(String dni) {
	sendHubCommand([
		switchBinaryGetCmd(getEndpointFromDNI(dni))
	])
}

String switchBinaryGetCmd(int endpoint) {
	return multiChannelCmdEncapCmd(zwave.switchBinaryV1.switchBinaryGet(), endpoint)
}

String multiChannelCmdEncapCmd(cmd, int endpoint=0) {
	if (endpoint) {
		return secureCmd(zwave.multiChannelV3.multiChannelCmdEncap(destinationEndPoint:endpoint).encapsulate(cmd))
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
		log.debug "Unable to get encapsulated command: $cmd"
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
	String name = configParams.find { name, param -> param.num == cmd.parameterNumber }?.key
	if (name) {
		int val = cmd.scaledConfigurationValue

		if (val < 0) {
			// device uses signed values
			val = (val + Math.pow(256, cmd.size))
		}

		state[name] = val
		log.debug "${configParams[name]?.title}(#${configParams[name]?.num}) = ${val}"
	} else {
		log.debug "Parameter #${cmd.parameterNumber} = ${cmd.scaledConfigurationValue}"
	}
}

void zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd, endpoint=0) {
	log.debug "${cmd} (${endpoint})"
	sendSwitchEvent(cmd.value, endpoint)
}

void zwaveEvent(physicalgraph.zwave.commands.switchbinaryv1.SwitchBinaryReport cmd, endpoint=0) {
	log.debug "${cmd} (${endpoint})"
	sendSwitchEvent(cmd.value, endpoint)
}

void sendSwitchEvent(int rawValue, int endpoint) {
	String value = (rawValue ? "on" : "off")
	if (endpoint == mainEndpoint) {
		log.debug("Switch is ${value}")
		sendEvent(name: "switch", value: value)
	} else {
		def child = findChildByEndpoint(endpoint)
		if (child) {
			log.debug("${child.displayName} switch is ${value}")
			child.sendEvent(name: "switch", value: value)
		} else {
			log.warn "Child device for endpoint ${endpoint} does not exist"
		}

		// Workaround for device not sending reports for main endpoint for physical or z-wave control.
		if (device.currentValue("switch") != value) {
			sendHubCommand([switchBinaryGetCmd(mainEndpoint)])
		}
	}
}

void zwaveEvent(physicalgraph.zwave.commands.centralscenev1.CentralSceneNotification cmd) {
	if (state.lastSequenceNumber != cmd.sequenceNumber) {
		state.lastSequenceNumber = cmd.sequenceNumber

		int endpoint = cmd.sceneNumber
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
			sendButtonEvent(findChildByEndpoint(endpoint), value)
		}
	}
}

void sendButtonEvent(child, String value) {
	if (child) {
		log.debug "${child.displayName} button ${value}"
		child.sendEvent(name: "button", value: value, data:[buttonNumber: 1], isStateChange: true)
	}
}

void zwaveEvent(physicalgraph.zwave.Command cmd) {
	log.debug "Unhandled zwaveEvent: $cmd"
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

def findChildByEndpoint(int endpoint) {
	String dni = buildChildDNI(endpoint)
	return childDevices?.find { it.deviceNetworkId == dni }
}

String buildChildDNI(int endpoint) {
	return "${device.deviceNetworkId}:${endpoint}"
}

int getEndpointFromDNI(String dni) {
	if (dni?.contains(":")) {
		String lastChar = dni.reverse().take(1)
		return safeToInt(lastChar, mainEndpoint)
	} else {
		return mainEndpoint
	}
}