/**
 *	Copyright 2021 TechniSat
 *
 *	Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *	in compliance with the License. You may obtain a copy of the License at:
 *
 *		http://www.apache.org/licenses/LICENSE-2.0
 *
 *	Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *	on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *	for the specific language governing permissions and limitations under the License.
 *
 */
import groovy.json.JsonOutput

metadata {
	definition (name: "TechniSat On/Off switch", namespace: "TechniSat", author: "TechniSat", vid:"generic-switch-power-energy",
				mnmn: "SmartThings", runLocally: true, minHubCoreVersion: '000.017.0012',
				executeCommandsLocally: false) {
		capability "Energy Meter"
		capability "Switch"
		capability "Power Meter"
		capability "Refresh"
		capability "Configuration"
		capability "Health Check"

		fingerprint mfr: "0299", prod: "0002", model: "1A90", deviceJoinName: "TechniSat Switch"
	}

	preferences {
		parameterMap.each {
			input(title: "Parameter ${it.paramZwaveNum}: ${it.title}",
				description: it.descr,
				type: "paragraph",
				element: "paragraph")
			if (it.enableSwitch) {
				input(name: it.enableKey,
					title: "Enable",
					type: "bool",
					required: false)
			}
			input(name: it.key,
				title: it.paramName,
				type: it.type,
				options: it.values,
				range: it.range,
				required: false)
		}
	}
}
def installed() {
	log.debug "installed()"
	initStateConfig()
	initialize()
}

def updated() {
	log.debug "updated()"
	initialize()
	syncConfig()
}

def initialize() {
	sendEvent(name: "checkInterval", value: 2 * 15 * 60 + 2 * 60, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])
}

def getCommandClassVersions() {
	[
		0x20: 1,  // Basic
		0x25: 1,  // Switch Binary
		0x32: 3,  // Meter
		0x56: 1,  // Crc16Encap
		0x70: 2,  // Configuration
		0x98: 1,  // Security
	]
}

def parse(String description) {
	log.debug "parse() - description: "+description
	def result = null
	if (description != "updated") {
		def cmd = zwave.parse(description, commandClassVersions)
		if (cmd) {
			result = zwaveEvent(cmd)
			log.debug("'$description' parsed to $result")
		} else {
			log.debug("Couldn't zwave.parse '$description'")
		}
	}
	result
}

def handleMeterReport(cmd) {
	if (cmd.meterType == 1) {
		if (cmd.scale == 0) {
			createEvent(name: "energy", value: cmd.scaledMeterValue, unit: "kWh")
		} else if (cmd.scale == 1) {
			createEvent(name: "energy", value: cmd.scaledMeterValue, unit: "kVAh")
		} else if (cmd.scale == 2) {
			createEvent(name: "power", value: Math.round(cmd.scaledMeterValue), unit: "W")
		}
	}
}

def zwaveEvent(physicalgraph.zwave.commands.meterv3.MeterReport cmd) {
	log.debug "v3 Meter report: "+cmd
	handleMeterReport(cmd)
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd) {
	log.debug "Basic report: "+cmd
	def value = (cmd.value ? "on" : "off")
	def evt = createEvent(name: "switch", value: value, type: "physical", descriptionText: "$device.displayName was turned $value")
	if (evt.isStateChange) {
		[evt, response(["delay 3000", meterGet(scale: 2).format()])]
	} else {
		evt
	}
}

def zwaveEvent(physicalgraph.zwave.commands.switchbinaryv1.SwitchBinaryReport cmd) {
	log.debug "Switch binary report: "+cmd
	def value = (cmd.value ? "on" : "off")
	createEvent(name: "switch", value: value, type: "digital", descriptionText: "$device.displayName was turned $value")
}

def zwaveEvent(physicalgraph.zwave.commands.configurationv2.ConfigurationReport cmd) {
	def param = parameterMap.find( {it.paramZwaveNum == cmd.parameterNumber } )
		
	if (state.currentConfig."$param.key".status != "sync") {
		if (state.currentConfig."$param.key"?.newValue == cmd.scaledConfigurationValue ||
			 state.currentConfig."$param.key".status == "init") {
			log.debug "Parameter ${param.key} set to value:${cmd.scaledConfigurationValue}"
			state.currentConfig."$param.key".status = "sync"
			state.currentConfig."$param.key".value = cmd.scaledConfigurationValue
		} else {
			log.debug "Parameter ${param.key} set to value failed: is:${cmd.scaledConfigurationValue} <> ${state.currentConfig."$param.key".newValue}"
			state.currentConfig."$param.key".status = "failed"
			syncConfig()
		}
	} else {
		log.debug "Parameter ${param.key} update received. value:${cmd.scaledConfigurationValue}"
		state.currentConfig."$param.key".value  = cmd.scaledConfigurationValue
	}
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
	log.debug "${device.displayName}: Unhandled: $cmd"
	[:]
}

def on() {
	encapSequence([
		zwave.switchBinaryV1.switchBinarySet(switchValue: 0xFF),
		zwave.switchBinaryV1.switchBinaryGet(),
		meterGet(scale: 2)
	], 3000)
}

def off() {
	encapSequence([
		zwave.switchBinaryV1.switchBinarySet(switchValue: 0x00),
		zwave.switchBinaryV1.switchBinaryGet(),
		meterGet(scale: 2)
	], 3000)
}

def ping() {
	log.debug "ping()"
	refresh()
}

def poll() {
	sendHubCommand(refresh())
}

def refresh() {
	log.debug "refresh()"
	encapSequence([
		zwave.switchBinaryV1.switchBinaryGet(),
		meterGet(scale: 0),
		meterGet(scale: 2)
	])
}

def resetEnergyMeter() {
	log.debug "resetEnergyMeter: not implemented"
}

def configure() {
	log.debug "configure()"
	def result = []

	log.debug "Configure zwaveInfo: "+zwaveInfo

	initStateConfigFromDevice()
	logStateConfig()
	result << response(encap(meterGet(scale: 0)))
	result << response(encap(meterGet(scale: 2)))
	result << response(encap(zwave.switchBinaryV1.switchBinaryGet()))
	result
}

def meterGet(map) {
	return zwave.meterV2.meterGet(map)
}

def zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityMessageEncapsulation cmd) {
	def encapsulatedCommand = cmd.encapsulatedCommand(commandClassVersions)
	if (encapsulatedCommand) {
		log.debug "Parsed SecurityMessageEncapsulation into: ${encapsulatedCommand}"
		zwaveEvent(encapsulatedCommand)
	} else {
		log.warn "Unable to extract Secure command from $cmd"
	}
}

def zwaveEvent(physicalgraph.zwave.commands.crc16encapv1.Crc16Encap cmd) {
	def version = commandClassVersions[cmd.commandClass as Integer]
	def ccObj = version ? zwave.commandClass(cmd.commandClass, version) : zwave.commandClass(cmd.commandClass)
	def encapsulatedCommand = ccObj?.command(cmd.command)?.parse(cmd.data)
	if (encapsulatedCommand) {
		log.debug "Parsed Crc16Encap into: ${encapsulatedCommand}"
		zwaveEvent(encapsulatedCommand)
	} else {
		log.warn "Unable to extract CRC16 command from $cmd"
	}
}

private secEncap(physicalgraph.zwave.Command cmd) {
	log.debug "encapsulating command using Secure Encapsulation, command: $cmd"
	zwave.securityV1.securityMessageEncapsulation().encapsulate(cmd).format()
}

private crcEncap(physicalgraph.zwave.Command cmd) {
	log.debug "encapsulating command using CRC16 Encapsulation, command: $cmd"
	zwave.crc16EncapV1.crc16Encap().encapsulate(cmd).format()
}

private encap(physicalgraph.zwave.Command cmd) {
	if (zwaveInfo?.zw?.contains("s")) {
		secEncap(cmd)
	} else if (zwaveInfo?.cc?.contains("56")) {
		crcEncap(cmd)
	} else {
		log.debug "no encapsulation supported for command: $cmd"
		cmd.format()
	}
}

private encapSequence(cmds, Integer delay=250) {
	delayBetween(cmds.collect{ encap(it) }, delay)
}

private isConfigChanged(parameter) {
	def settingsValue = settings."$parameter.key"
	log.debug "isConfigChanged parameter:${parameter.key}: ${settingsValue}"
	if (parameter.enableSwitch) {
		if (settings."$parameter.enableKey" != null) {
			if (settings."$parameter.enableKey" == false) {
				settingsValue = 0;
			}
		}
	}
	if (settingsValue != null) {
		Integer value = 0
		if (parameter.type == "number") {
			value = settingsValue
		} else {
			value = Integer.parseInt(settingsValue)
		}
		if (state.currentConfig."$parameter.key".value != value) {
			state.currentConfig."$parameter.key".newValue = value
			log.debug "${parameter.key} set:${value} value:${state.currentConfig."$parameter.key".value} newValue:${state.currentConfig."$parameter.key".newValue}"
			return true
		} else if (state.currentConfig."$parameter.key".status != "sync") {
			log.debug "${parameter.key} retry to set; is:${state.currentConfig."$parameter.key".value} should:${state.currentConfig."$parameter.key".newValue}"
			return true
		}
		return false
	} else {
		log.debug "pref value not set yet"
		return false
	}
}

private syncConfig() {
	def commands = []
	parameterMap.each {
		if (isConfigChanged(it)) {
			log.debug "Parameter ${it.key} has been updated from value: ${state.currentConfig."$it.key".value} to ${state.currentConfig."$it.key".newValue}"
			state.currentConfig."$it.key".status = "syncPending"
			commands << response(encap(zwave.configurationV2.configurationSet(configurationValue: intToParam(state.currentConfig."$it.key".newValue, it.paramZwaveSize),
																				parameterNumber: it.paramZwaveNum, size: it.paramZwaveSize)))                                                                
			commands << response(encap(zwave.configurationV2.configurationGet(parameterNumber: it.paramZwaveNum)))
		} else if (state.currentConfig."$it.key".value == null) {
			log.warn "Parameter ${it.key} no. ${it.paramZwaveNum} has no value. Please check preference declaration for errors."
		}
	}
	if(commands) {
		sendHubCommand(commands,1000)
	}
}

private initStateConfig() {
	log.debug "initStateConfig()"
	state.currentConfig = [:]
	parameterMap.each {
		log.debug "set $it.key"
		state.currentConfig."$it.key" = [:]
		state.currentConfig."$it.key".value = new Integer('0')
		state.currentConfig."$it.key".newValue = new Integer('0')
		state.currentConfig."$it.key".status = "init"
	}
}

private initStateConfigFromDevice() {
	log.debug "initStateConfigFromDevice()"
	def commands = []
	parameterMap.each {
		commands << response(encap(zwave.configurationV2.configurationGet(parameterNumber: it.paramZwaveNum)))
	}
	if(commands) {
		sendHubCommand(commands,1000)
	}
}

private logStateConfig() {
	parameterMap.each {
		log.debug "key:$it.key value: ${state.currentConfig."$it.key".value} newValue: ${state.currentConfig."$it.key".newValue} status: ${state.currentConfig."$it.key".status}"
	}
}

private List intToParam(Long value, Integer size = 1) {
	def result = []
	size.times {
		result = result.plus(0, (value & 0xFF) as Short)
		value = (value >> 8)
	}
	return result
}

private getParameterMap() {
	[
		[
			title: "Wattage meter report interval",
			descr: "Interval of current wattage meter reports in 10 seconds. 3 ... 8640 (30 seconds - 1 day)",
			key: "wattageMeterReportInterval",
			paramName: "Set Value (3..8640)",
			type: "number",
			range: "3..8640",
			enableSwitch: true,
			enableKey: "wattageMeterReportDisable",
			paramZwaveNum: 2, 
			paramZwaveSize: 1
		],
		[
			title: "Energy meter report interval",
			descr: "Interval of active energy meter reports in minutes. 10 ... 30240 (10 minutes - 3 weeks)",
			key: "energyMeterReportInterval",
			enableSwitch: true,
			enableKey: "energyMeterReportDisable",
			paramName: "Set Value (10..30240)",
			type: "number",
			range: "10..30240",
			paramZwaveNum: 3,
			paramZwaveSize: 2
		],
		[
			title: "Operation mode of button T",
			descr: "Operation mode of button T",
			key: "buttonModeSetting",
			paramName: "Select",
			type: "enum",
			values: [
				0: "0 - T1 turns L1 on, T2 turn L1 off",
				1: "1 - T1 & T2 toggle output L1"
			],
			paramZwaveNum: 4, 
			paramZwaveSize: 1
		],
		[
			title: "External Connector",
			descr: "Configuration of switch type connected to extension connector S",
			key: "externalSwitchSetting",
			paramName: "Select",
			type: "enum",
			values: [
				0: "0 - toggle switch",
				1: "1 - push button switch"
			],
			paramZwaveNum: 5,
			paramZwaveSize: 1
		],
	]
}
