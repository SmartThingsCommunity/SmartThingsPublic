/*
 *  Zooz ZEN51 Dry Contact Relay
 *
 *  Changelog:
 *
 *    2022-03-09
 *      - requested change.
 *    2022-03-02
 *      - Removed central scene setting
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
	0x20: 1,	// Basic
	0x22: 1,	// ApplicationStatus
	0x25: 1,	// SwitchBinary
	0x55: 1,	// TransportService
	0x59: 1,	// AssociationGrpInfo
	0x5A: 1,	// DeviceResetLocally
	0x5B: 1,	// CentralScene
	0x5E: 2,	// ZwaveplusInfo
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

@Field static int btnPushed = 0
@Field static int btnReleased = 1
@Field static int btnHeld = 2
@Field static List<String> supportedButtonValues = ["pushed","held","pushed_2x","pushed_3x","pushed_4x","pushed_5x"]

@Field static Map configParams = [
	ledIndicator: [num:1, title:"Led indicator", size:1, defaultVal:1, options:[0:"Disabled", 1:"Enabled"]],
	autoOff: [num:2, title:"Auto Off Timer", size:2, defaultVal:0, range:"0..65535", desc:"0(disabled), 1..65535(timer unit)"],
	autoOn: [num:3, title:"Auto On Timer", size:2, defaultVal:0, range:"0..65535", desc:"0(disabled), 1..65535(timer unit)"],
	timerUnit: [num:10, title:"Timer Unit", size:1, defaultVal:1, options:[1:"Minutes", 2:"Seconds"]],
	statusAfterPowerFailure: [num:4, title:"On/Off Status After Power Failure", size:1, defaultVal:2, options:[0:"Forced off", 1:"Forced on", 2:"Restore previous state"]],
	loadControl: [num:6, title:"Load Control", size:1, defaultVal:1, options:[0:"Disable Switch/ Enable Z-Wave", 1:"Enable Switch and Z-Wave", 2:"Disable Switch and Z-Wave"]],
	switchType: [num:7, title:"Switch Type", size:1, defaultVal:2, options:[0:"Toggle Switch", 1:"Momentary Light Switch", 2:"Toggle Up On/Down Off", 3:"3-way Impulse Control", 4:"Garage Door Mode"]],
	relayBehavior: [num:9, title:"Relay Type Behavior", size:1, defaultVal:0, options:[0:"Normally Open (NO)", 1:"Normally Closed (NC)"]],
	impulseDuration: [num:11, title:"Impulse Duration for 3-way", size:1, defaultVal:10, range:"2..200", desc:"2..200 (seconds)"]
]

metadata {
	definition (
		name: "Zooz ZEN51 Dry Contact Relay",
		namespace: "Zooz",
		author: "Kevin LaFramboise (@krlaframboise)",
		ocfDeviceType: "oic.d.light",
		mnmn: "SmartThingsCommunity",
		vid: "d4bdecb2-4374-3c96-aceb-24223399fe5f"
	) {
		capability "Actuator"
		capability "Sensor"
		capability "Switch"
		capability "Button"
		capability "Refresh"
		capability "Health Check"
		
		// zw:Ls2a type:1000 mfr:027A prod:0104 model:0201 ver:1.24 zwv:7.15 lib:03 cc:5E,55,9F,6C,22 sec:25,70,85,59,8E,86,72,5A,73,7A,5B,87
		fingerprint mfr: "027A", prod: "0104", model: "0201", deviceJoinName: "Zooz Switch" // Zooz ZEN51 Dry Contact Relay
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
		
	if (!device.currentValue("numberOfButtons")) {
		sendEvent(name:"numberOfButtons", value:1, displayed:false)	
		sendEvent(name: "supportedButtonValues", value: supportedButtonValues.encodeAsJSON(), displayed: false)	
		sendButtonEvent("pushed")
	}
}

def configure() {
	log.debug "configure()..."
	List<String> cmds = []

	if (device.currentValue("switch") == null) {
		cmds << secureCmd(zwave.switchBinaryV1.switchBinaryGet())
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
	return [ secureCmd(zwave.switchBinaryV1.switchBinarySet(switchValue: 0xFF)) ]
}

def off() {
	log.debug "off()..."	
	return [ secureCmd(zwave.switchBinaryV1.switchBinarySet(switchValue: 0x00)) ]
}

def ping() {
	log.debug "ping()..."
	return [ secureCmd(zwave.switchBinaryV1.switchBinaryGet()) ]
}

def refresh() {
	log.debug "refresh()..."	
	sendHubCommand([ secureCmd(zwave.switchBinaryV1.switchBinaryGet()) ])
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

void zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd) {
	log.debug "${cmd}"
	sendSwitchEvent(cmd.value)
}

void zwaveEvent(physicalgraph.zwave.commands.switchbinaryv1.SwitchBinaryReport cmd) {
	log.debug "${cmd}"
	sendSwitchEvent(cmd.value)
}

void sendSwitchEvent(int rawValue) {
	String value = (rawValue ? "on" : "off")
	log.debug("Switch is ${value}")
	sendEvent(name: "switch", value: value)
}

void zwaveEvent(physicalgraph.zwave.commands.centralscenev1.CentralSceneNotification cmd) {
	if (state.lastSequenceNumber != cmd.sequenceNumber) {
		state.lastSequenceNumber = cmd.sequenceNumber

		String value
		switch (cmd.keyAttributes){
			case btnPushed:
				value = "pushed"
				break
			case btnReleased:
				// value = released"
				log.debug "Button Value 'released' is not supported by SmartThings"
				break
			case btnHeld:
				value = "held"
				break
			default:
				value = "pushed_${cmd.keyAttributes - 1}x"
		}

		if (value) {
			sendButtonEvent(value)
		}
	}
}

void sendButtonEvent(String value) {	
	log.debug "button ${value}"
	sendEvent(name: "button", value: value, data:[buttonNumber: 1], isStateChange: true)
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