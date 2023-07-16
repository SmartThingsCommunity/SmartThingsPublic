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

metadata  {
	definition (name: "TechniSat Series switch", namespace: "TechniSat", author: "TechniSat", vid:"generic-switch-power-energy",
				mnmn: "SmartThings") {
		capability "Energy Meter"
		capability "Switch"
		capability "Power Meter"
		capability "Refresh"
		capability "Configuration"
		capability "Health Check"

		fingerprint mfr: "0299", prod: "0003", model: "1A91", deviceJoinName: "TechniSat Switch 1"
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

private createChild() {
	
	log.debug "createChild componentLabel: ${componentLabel}"
	try {
		String dni = "${device.deviceNetworkId}:2"
		def componentLabel = "${device.displayName[0..-2]}2"
		addChildDevice("smartthings","Child Metering Switch", dni, device.getHub().getId(),
						[completedSetup: true, label: "${componentLabel}", isComponent: false])
		log.debug "Endpoint 2 (TechniSat Series switch child) added as $componentLabel"
  	} catch (e) {
		log.warn "Failed to add endpoint 2 ($desc) as TechniSat Series switch child - $e"
	}
}

def installed() {
	log.debug "installed()"
	createChild()
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
		0x60: 3,  // Multi-Channel
		0x70: 2,  // Configuration
		0x98: 1,  // Security
	]
}

def parse(String description) {
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

def createMeterEvent(cmd) {
	def eventMap = [:]
	if (cmd.meterType == 1) {
		if (cmd.scale == 0) {
			eventMap.name = "energy"
			eventMap.value = cmd.scaledMeterValue
			eventMap.unit = "kWh"
		} else if (cmd.scale == 1) {
			eventMap.name = "energy"
			eventMap.value = cmd.scaledMeterValue
			eventMap.unit = "kVAh"
		} else if (cmd.scale == 2) {
			eventMap.name = "power"
			eventMap.value = Math.round(cmd.scaledMeterValue)
			eventMap.unit = "W"
		}
	}
	eventMap
}

def zwaveEvent(physicalgraph.zwave.commands.meterv3.MeterReport cmd, endpoint=null) {
	log.debug "v3 Meter report endpoint $endpoint: "+cmd
	if (endpoint == 1) {
		createEvent(createMeterEvent(cmd))
	} else if (endpoint == 2) {
		childDevices[0]?.sendEvent(createMeterEvent(cmd))
	}

}

def handlOnOffReport(cmd, endpoint) {
	def value = (cmd.value ? "on" : "off")
	if (endpoint == 1) {
		def evt = createEvent(name: "switch", value: value, type: "physical", descriptionText: "$device.displayName was turned $value")
		if (evt.isStateChange) {
			[evt, response(["delay 3000",encapEp(endpoint, meterGet(scale: 2))])]
		} else {
			evt
		}
	} else if (endpoint == 2) {
		childDevices[0]?.sendEvent(name: "switch", value: value, type: "physical", descriptionText: "$device.displayName was turned $value")
		sendHubCommand(encapEp(endpoint, meterGet(scale: 2)))
	}
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd, endpoint=null) {
	log.debug "Basic report endpoint $endpoint: "+cmd
	handlOnOffReport(cmd,endpoint)
}

def zwaveEvent(physicalgraph.zwave.commands.switchbinaryv1.SwitchBinaryReport cmd, endpoint=null) {
	log.debug "Switch binary report endpoint: $endpoint: "+cmd
	handlOnOffReport(cmd,endpoint)
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

def zwaveEvent(physicalgraph.zwave.commands.multichannelv3.MultiChannelCmdEncap cmd) {
	if (cmd.commandClass == 0x6C && cmd.parameter.size >= 4) { 
		cmd.parameter = cmd.parameter.drop(2)
		cmd.commandClass = cmd.parameter[0]
		cmd.command = cmd.parameter[1]
		cmd.parameter = cmd.parameter.drop(2)
	}
	def encapsulatedCommand = cmd.encapsulatedCommand([0x20: 1, 0x25: 1, 0x32: 3])
	log.debug "handle cmd on endpoint ${cmd.sourceEndPoint}"
	zwaveEvent(encapsulatedCommand, cmd.sourceEndPoint as Integer)
}

def zwaveEvent(physicalgraph.zwave.Command cmd, endpoint=null) {
	if (endpoint == null) {
		log.debug "${device.displayName}: Unhandled: $cmd"
	} else {
		log.debug("$device.displayName: $cmd endpoint: $endpoint")
	}
	[:]
}

def getEndpoint(deviceNetworkId) {
	def split = deviceNetworkId?.split(":")
	return (split.length > 1) ? split[1] as Integer : null
}

def createOnOffCmd(value, endpoint = 1) {
	log.debug "createOnOffCmd value $value endpoint $endpoint"
	delayBetween([
		encapEp(endpoint, zwave.switchBinaryV1.switchBinarySet(switchValue: value)),
		encapEp(endpoint, zwave.switchBinaryV1.switchBinaryGet()),
		encapEp(endpoint, meterGet(scale: 2))
	])
}

def on() {
	createOnOffCmd(0xFF)
}

def off() {
	createOnOffCmd(0x00)
}

def childOnOff(deviceNetworkId, value) {
	def endpoint = getEndpoint(deviceNetworkId)
	log.debug("childOnOff from endpoint ${endpoint}")
	if (endpoint != null) {
		sendHubCommand(createOnOffCmd(value, endpoint))
	}
}

def ping() {
	log.debug "ping()"
	refresh()
}

def poll() {
	sendHubCommand(refresh())
}

def refreshAll() {
	sendHubCommand(refresh(1))
	sendHubCommand(refresh(2))
}

def refresh(endpoint = 1) {
	log.debug "refresh()"
	delayBetween([
		encapEp(endpoint, zwave.switchBinaryV1.switchBinaryGet()),
		encapEp(endpoint, meterGet(scale: 0)),
		encapEp(endpoint, meterGet(scale: 2))
	])
}

def childRefresh(deviceNetworkId) {
	def endpoint = getEndpoint(deviceNetworkId)
	log.debug("childRefresh from endpoint ${endpoint}")
	if (endpoint != null) {
		sendHubCommand(refresh(endpoint))
	}
}


def childReset(deviceNetworkId) {
	def endpoint = getEndpoint(deviceNetworkId)
	log.debug("childReset from endpoint ${endpoint}")
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
	refreshAll()
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

def encapEp(endpointNumber, cmd) {
	if (cmd instanceof physicalgraph.zwave.Command) {
		encap(zwave.multiChannelV3.multiChannelCmdEncap(destinationEndPoint: endpointNumber).encapsulate(cmd))
	} else if (cmd.startsWith("delay")) {
		cmd
	} else {
		def header = "600D00"
		String.format("%s%02X%s", header, endpointNumber, cmd)
	}
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
			commands << response(encap(zwave.configurationV2.configurationSet(scaledConfigurationValue: state.currentConfig."$it.key".newValue,
				parameterNumber: it.paramZwaveNum, size: it.paramZwaveSize)))                                                                
			commands << response(encap(zwave.configurationV2.configurationGet(parameterNumber: it.paramZwaveNum)))
		} else if (state.currentConfig."$it.key".value == null) {
			log.warn "Parameter ${it.key} no. ${it.paramZwaveNum} has no value. Please check preference declaration for errors."
		}
	}
	if (commands) {
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
	if (commands) {
		sendHubCommand(commands,1000)
	}
}

private logStateConfig() {
	parameterMap.each {
		log.debug "key:$it.key value: ${state.currentConfig."$it.key".value} newValue: ${state.currentConfig."$it.key".newValue} status: ${state.currentConfig."$it.key".status}"
	}
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
			enableSwitchDefaultValue: true,
			enableKey: "wattageMeterReportDisable",
			paramZwaveNum: 2, 
			paramZwaveSize: 1
		],
		[
			title: "Energy meter report interval",
			descr: "Interval of active energy meter reports in minutes. 10 ... 30240 (10 minutes - 3 weeks)",
			key: "energyMeterReportInterval",
			enableSwitch: true,
			enableSwitchDefaultValue: true,
			enableKey: "energyMeterReportDisable",
			paramName: "Set Value (10..30240)",
			type: "number", 
			range: "10..30240",
			paramZwaveNum: 3, 
			paramZwaveSize: 2
		],
		[
			title: "Operation mode of buttons T1 - T4",
			descr: "Operation mode of buttons T1 - T4",
			key: "buttonModeSetting",
			paramName: "Select",
			type: "enum",
			values: [
				0: "0 - top buttons turn outputs on, bottom buttons turn outputs off",
				1: "1 - buttons toggle the outputs on/off"
			],
			paramZwaveNum: 4, 
			paramZwaveSize: 1
		]
	]
}