/**
 *  Copyright 2020 SRPOL
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *		http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */
metadata {
	definition (name: "Qubino Flush 2 Relay", namespace: "qubino", author: "SmartThings", mnmn: "SmartThings", vid: "generic-switch-power-energy") {
		capability "Switch"
		capability "Power Meter"
		capability "Energy Meter"
		capability "Refresh"
		capability "Actuator"
		capability "Sensor"
		capability "Health Check"
		capability "Configuration"

		command "reset"

		fingerprint mfr: "0159", prod: "0002", model: "0051", deviceJoinName: "Qubino Switch 1" //Qubino Flush 2 Relay
		fingerprint mfr: "0159", prod: "0002", model: "0052", deviceJoinName: "Qubino Switch" //Qubino Flush 1 Relay 
		fingerprint mfr: "0159", prod: "0002", model: "0053", deviceJoinName: "Qubino Switch", mnmn: "SmartThings", vid: "generic-switch" //Qubino Flush 1D Relay
	}

	tiles(scale: 2) {
		multiAttributeTile(name:"switch", type: "generic", width: 6, height: 4, canChangeIcon: true){
			tileAttribute("device.switch", key: "PRIMARY_CONTROL") {
				attributeState("on", label: '${name}', action: "switch.off", icon: "st.switches.switch.on", backgroundColor: "#00a0dc")
				attributeState("off", label: '${name}', action: "switch.on", icon: "st.switches.switch.off", backgroundColor: "#ffffff")
			}
		}
		valueTile("power", "device.power", decoration: "flat", width: 2, height: 2) {
			state "default", label:'${currentValue} W'
		}
		valueTile("energy", "device.energy", decoration: "flat", width: 2, height: 2) {
			state "default", label:'${currentValue} kWh'
		}
		standardTile("refresh", "device.power", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "default", label:'', action:"refresh.refresh", icon:"st.secondary.refresh"
		}
		standardTile("reset", "device.energy", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "default", label:'reset kWh', action:"reset"
		}

		main(["switch"])
		details(["switch","power","energy","refresh","reset"])
	}

	preferences {
		parameterMap.each {
			input (title: it.name, description: it.description, type: "paragraph", element: "paragraph")

			switch(it.type) {
				case "boolean":
					input(type: "paragraph", element: "paragraph", description: "Option enabled: ${it.activeDescription}\n" +
							"Option disabled: ${it.inactiveDescription}"
					)
					input(name: it.key, type: "boolean", title: "Enable", defaultValue: it.defaultValue == it.activeOption, required: false)
					break
				case "enum":
					input(name: it.key, title: "Select", type: "enum", options: it.values, defaultValue: it.defaultValue, required: false)
					break
			}
		}
	}
}

def installed() {
	if (zwaveInfo?.model.equals("0051")) {
		state.numberOfSwitches = 2
	} else {
		state.numberOfSwitches = 1
	}
	
 	if (!childDevices && state.numberOfSwitches > 1) {
		addChildSwitches(state.numberOfSwitches)
	}
	sendEvent(name: "checkInterval", value: 2 * 15 * 60 + 2 * 60, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])
	// Preferences template begin
	state.currentPreferencesState = [:]
	parameterMap.each {
		state.currentPreferencesState."$it.key" = [:]
		state.currentPreferencesState."$it.key".value = getPreferenceValue(it)
		def preferenceName = it.key + "Boolean"
		settings."$preferenceName" = true
		state.currentPreferencesState."$it.key".status = "synced"
	}
	// Preferences template end
	response([
			refresh((1..state.numberOfSwitches).toList()),
			addToAssociationGroupIfNeeded()
	].flatten())
}

def updated() {
	if (!childDevices && state.numberOfSwitches > 1) {
		addChildSwitches(state.numberOfSwitches)
	}
	// Preferences template begin
	parameterMap.each {
		if (isPreferenceChanged(it) && !excludeParameterFromSync(it)) {
			log.debug "Preference ${it.key} has been updated from value: ${state.currentPreferencesState."$it.key".value} to ${settings."$it.key"}"
			state.currentPreferencesState."$it.key".status = "syncPending"
		} else if (!state.currentPreferencesState."$it.key".value) {
			log.warn "Preference ${it.key} no. ${it.parameterNumber} has no value. Please check preference declaration for errors."
		}
	}
	syncConfiguration()
	// Preferences template end
}

def excludeParameterFromSync(preference){
	def exclude = false
	if (preference.key == "outputQ2SwitchSelection") {
		if (zwaveInfo?.model?.equals("0052") || zwaveInfo?.model?.equals("0053")) {
			exclude = true
		}
	}

	if (exclude) {
		log.warn "Preference no ${preference.parameterNumber} - ${preference.key} is not supported by this device"
	}
	return exclude
}

def configure() {
	def cmds = []

	if (zwaveInfo?.model?.equals("0051")) {
		// parameters 40 and 41 - power consumption reporting threshold for Q1 and Q2 loads (respectively) - 5 %
		cmds += encap(zwave.configurationV1.configurationSet(parameterNumber: 40, size: 1, scaledConfigurationValue: 5))
		cmds += encap(zwave.configurationV1.configurationSet(parameterNumber: 41, size: 1, scaledConfigurationValue: 5))
		// parameters 42 and 43 - power consumption reporting time threshold for Q1 and Q2 (respectively) - 5 minutes
		// additionally, manual states that default value for below parameters is 0, which disables power reporting
		cmds += encap(zwave.configurationV1.configurationSet(parameterNumber: 42, size: 2, scaledConfigurationValue: 300))
		cmds += encap(zwave.configurationV1.configurationSet(parameterNumber: 43, size: 2, scaledConfigurationValue: 300))
	} else if (zwaveInfo?.model?.equals("0052")) {
		//parameter 40 - power reporting threshold for Q1 load - 75%
		cmds += encap(zwave.configurationV1.configurationSet(parameterNumber: 40, size: 1, scaledConfigurationValue: 75))
	}

	delayBetween(cmds, 500)
}

def addToAssociationGroupIfNeeded() {
	def cmds = []
	if (zwaveInfo?.model?.equals("0052")) {
		//Hub automatically adds device to multiChannelAssosciationGroup and this needs to be removed
		cmds += encap(zwave.multiChannelAssociationV2.multiChannelAssociationRemove(groupingIdentifier: 1, nodeId:[])) 
		cmds += encap(zwave.associationV2.associationSet(groupingIdentifier: 1, nodeId: [zwaveHubNodeId]))
	}
	cmds
}

private syncConfiguration() {
	def commands = []
	parameterMap.each {
		try {
			if (state.currentPreferencesState."$it.key".status == "syncPending") {
				commands += encap(zwave.configurationV2.configurationSet(scaledConfigurationValue: getCommandValue(it), parameterNumber: it.parameterNumber, size: it.size))
				commands += encap(zwave.configurationV2.configurationGet(parameterNumber: it.parameterNumber))
			} else if (state.currentPreferencesState."$it.key".status == "disablePending") {
				commands += encap(zwave.configurationV2.configurationSet(scaledConfigurationValue: it.disableValue, parameterNumber: it.parameterNumber, size: it.size))
				commands += encap(zwave.configurationV2.configurationGet(parameterNumber: it.parameterNumber))
			}
		} catch (e) {
			log.warn "There's been an issue with preference: ${it.key}"
		}
	}
	sendHubCommand(commands)
}

def zwaveEvent(physicalgraph.zwave.commands.configurationv2.ConfigurationReport cmd, ep = null) {
	// Preferences template begin
	log.debug "Configuration report: ${cmd}"
	def preference = parameterMap.find( {it.parameterNumber == cmd.parameterNumber} )
	def key = preference.key
	def preferenceValue = getPreferenceValue(preference, cmd.scaledConfigurationValue)
	if (settings."$key" == preferenceValue) {
		state.currentPreferencesState."$key".value = settings."$key"
		state.currentPreferencesState."$key".status = "synced"
	} else {
		state.currentPreferencesState."$key"?.status = "syncPending"
		runIn(5, "syncConfiguration", [overwrite: true])
	}
	// Preferences template end
}

private getPreferenceValue(preference, value = "default") {
	def integerValue = value == "default" ? preference.defaultValue : value.intValue()
	switch (preference.type) {
		case "enum":
			return String.valueOf(integerValue)
		case "boolean":
			return String.valueOf(preference.optionActive == integerValue)
		default:
			return integerValue
	}
}

private getCommandValue(preference) {
	def parameterKey = preference.key
	switch (preference.type) {
		case "boolean":
			return settings."$parameterKey" ? preference.optionActive : preference.optionInactive
		default:
			return Integer.parseInt(settings."$parameterKey")
	}
}

private isPreferenceChanged(preference) {
	if (settings."$preference.key" != null) {
		return state.currentPreferencesState."$preference.key".value != settings."$preference.key"
	} else {
		return false
	}
}

def parse(String description) {
	def result = null
	if (description.startsWith("Err")) {
		result = createEvent(descriptionText:description, isStateChange:true)
	} else if (description != "updated") {
		def cmd = zwave.parse(description)
		if (cmd) {
			result = zwaveEvent(cmd)
		}
	}
	log.debug "parsed '${description}' to ${result.inspect()}"
	result
}

def zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityMessageEncapsulation cmd, ep = null) {
	log.debug "Security Message Encap ${cmd}"
	def encapsulatedCommand = cmd.encapsulatedCommand()
	if (encapsulatedCommand) {
		zwaveEvent(encapsulatedCommand, null)
	} else {
		log.warn "Unable to extract encapsulated cmd from $cmd"
		createEvent(descriptionText: cmd.toString())
	}
}

def zwaveEvent(physicalgraph.zwave.commands.multichannelv3.MultiChannelCmdEncap cmd, ep = null) {
	log.debug "Multichannel command ${cmd}" + (ep ? " from endpoint $ep" : "")
	if (cmd.commandClass == 0x6C && cmd.parameter.size >= 4) { // Supervision encapsulated Message
		// Supervision header is 4 bytes long, two bytes dropped here are the latter two bytes of the supervision header
		cmd.parameter = cmd.parameter.drop(2)
		// Updated Command Class/Command now with the remaining bytes
		cmd.commandClass = cmd.parameter[0]
		cmd.command = cmd.parameter[1]
		cmd.parameter = cmd.parameter.drop(2)
	}
	def encapsulatedCommand = cmd.encapsulatedCommand()
	zwaveEvent(encapsulatedCommand, cmd.sourceEndPoint as Integer)
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd, ep = null) {
	log.debug "Basic ${cmd}" + (ep ? " from endpoint $ep" : "")
	changeSwitch(ep, cmd)
}

def zwaveEvent(physicalgraph.zwave.commands.switchbinaryv1.SwitchBinaryReport cmd, ep = null) {
	log.debug "Binary ${cmd}" + (ep ? " from endpoint $ep" : "")
	changeSwitch(ep, cmd)
}

def defaultEndpoint() {
	if (zwaveInfo?.model?.equals("0052")) {
		return null
	} else {
		return 1
	}
}

private changeSwitch(endpoint, cmd) {
	def value = cmd.value ? "on" : "off"
	if (endpoint == defaultEndpoint()) {
		createEvent(name: "switch", value: value, isStateChange: true, descriptionText: "Switch ${endpoint} is ${value}")
	} else if (endpoint) {
		String childDni = "${device.deviceNetworkId}:$endpoint"
		def child = childDevices.find { it.deviceNetworkId == childDni }
		child?.sendEvent(name: "switch", value: value, isStateChange: true, descriptionText: "Switch ${endpoint} is ${value}")
	}
}

def zwaveEvent(physicalgraph.zwave.commands.meterv3.MeterReport cmd, ep = null) {
	def result = []

	log.debug "Meter ${cmd}" + (ep ? " from endpoint $ep" : "")

	if (ep == defaultEndpoint()) {
		result << createEvent(createMeterEventMap(cmd))
	} else if (ep) {
		String childDni = "${device.deviceNetworkId}:$ep"
		def child = childDevices.find { it.deviceNetworkId == childDni }

		child?.sendEvent(createMeterEventMap(cmd))
	}
	// Query energy when we receive power reports
	if (cmd.scale == 2) {
		result << response(encap(zwave.meterV3.meterGet(scale: 0x00), ep))
	}

	result
}

private createMeterEventMap(cmd) {
	def eventMap = [:]
	if (cmd.meterType == 1) {
		if (cmd.scale == 0) {
			eventMap.name = "energy"
			eventMap.value = cmd.scaledMeterValue
			eventMap.unit = "kWh"
		} else if (cmd.scale == 2) {
			eventMap.name = "power"
			eventMap.value = Math.round(cmd.scaledMeterValue)
			eventMap.unit = "W"
		}
	}
	eventMap
}

def zwaveEvent(physicalgraph.zwave.commands.sensormultilevelv5.SensorMultilevelReport cmd, ep = null) {
	log.debug "SensorMultilevelReport ${cmd}" + (ep ? " from endpoint $ep" : "")
	def result = []

	def map = [:]
	switch (cmd.sensorType) {
		case 1:
			map.name = "temperature"
			def cmdScale = cmd.scale == 1 ? "F" : "C"
			map.value = convertTemperatureIfNeeded(cmd.scaledSensorValue, cmdScale, cmd.precision)
			map.unit = getTemperatureScale()
			break
		default:
			map.descriptionText = cmd.toString()
	}
	def child = childDevices.find { it.deviceNetworkId == state.temperatureSensorDni }
	if (!child) {
		child = addChildTemperatureSensor()
	}
	child?.sendEvent(map)
	createEvent(map)
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicSet cmd, ep = null) { 
	log.debug "Basic ${cmd}" + (ep ? " from endpoint $ep" : "")
	changeSwitch(ep, cmd)
}

def zwaveEvent(physicalgraph.zwave.Command cmd, ep) {
	log.warn "Unhandled ${cmd}" + (ep ? " from endpoint $ep" : "")
}

def on() {
	onOffCmd(0xFF)
}

def off() {
	onOffCmd(0x00)
}

def ping() {
	refresh()
}

def childOnOff(deviceNetworkId, value) {
	def switchId = getSwitchId(deviceNetworkId)
	if (switchId != null) sendHubCommand onOffCmd(value, switchId)
}

private onOffCmd(value, endpoint = defaultEndpoint()) {
	delayBetween([
			encap(zwave.basicV1.basicSet(value: value), endpoint),
			encap(zwave.basicV1.basicGet(), endpoint)
	])
}

def childRefresh(deviceNetworkId, includeMeterGet = true) {
	def switchId = getSwitchId(deviceNetworkId)
	if (switchId != null) {
		sendHubCommand refresh([switchId],includeMeterGet)
	}
}

def refresh(endpoints = [1], includeMeterGet = true) {

	def cmds = []

	endpoints.each {
		cmds << [encap(zwave.basicV1.basicGet(), it)]
		if (includeMeterGet) {
			cmds << encap(zwave.meterV3.meterGet(scale: 0), it)
			cmds << encap(zwave.meterV3.meterGet(scale: 2), it)
		}
	}

	delayBetween(cmds, 200)
}

private resetAll() {
	childDevices.each {
		if (it.deviceNetworkId != state.temperatureSensorDni) {
			childReset(it.deviceNetworkId)
		}
	}
	sendHubCommand reset()
}

def childReset(deviceNetworkId) {
	def switchId = getSwitchId(deviceNetworkId)
	if (switchId != null) {
		log.debug "Child reset switchId: ${switchId}"
		sendHubCommand reset(switchId)
	}
}

def reset(endpoint = 1) {
	log.debug "Resetting endpoint: ${endpoint}"
	delayBetween([
			encap(zwave.meterV3.meterReset(), endpoint),
			encap(zwave.meterV3.meterGet(scale: 0), endpoint),
			"delay 500"
	], 500)
}

def getSwitchId(deviceNetworkId) {
	def split = deviceNetworkId?.split(":")
	return (split.length > 1) ? split[1] as Integer : null
}

private encap(cmd, endpoint = null) {
	if (cmd) {
		if (endpoint) {
			cmd = zwave.multiChannelV3.multiChannelCmdEncap(destinationEndPoint: endpoint).encapsulate(cmd)
		}

		if (zwaveInfo.zw.contains("s")) {
			zwave.securityV1.securityMessageEncapsulation().encapsulate(cmd).format()
		} else {
			cmd.format()
		}
	}
}

private addChildSwitches(numberOfSwitches) {
	for (def endpoint : 2..numberOfSwitches) {
		try {
			String childDni = "${device.deviceNetworkId}:$endpoint"
			def componentLabel = device.displayName[0..-2] + "${endpoint}"
			addChildDevice("smartthings", "Child Metering Switch", childDni, device.getHub().getId(), [
					completedSetup	: true,
					label			: componentLabel,
					isComponent		: false
			])
		} catch(Exception e) {
			log.warn "Exception: ${e}"
		}
	}
}

private addChildTemperatureSensor() {
	try {
		String childDni = "${device.deviceNetworkId}:${state.numberOfSwitches + 1}"
		state.temperatureSensorDni = childDni
		def childDevice = addChildDevice("qubino", "Qubino Temperature Sensor", childDni, device.getHub().getId(), [
				completedSetup	: true,
				label			: "Qubino Temperature Sensor",
				isComponent		: false
		])
		childDevice
	} catch(Exception e) {
		log.warn "Exception: ${e}"
	}
}

private getParameterMap() {[
		[
				name: "Input 1 switch type", key: "input1SwitchType", type: "enum",
				parameterNumber: 1, size: 1, defaultValue: 1,
				values: [
						0: "Mono-stable switch type (push button)",
						1: "Bi-stable switch type",
				],
				description: "Input 1 switch type"
		],
		[
				name: "Input 2 switch type", key: "input2SwitchType", type: "enum",
				parameterNumber: 2, size: 1, defaultValue: 1,
				values: [
						0: "Mono-stable switch type (push button)",
						1: "Bi-stable switch type",
				],
				description: "Input 2 switch type"
		],
		[
				name: "Saving the state of the relays Q1 and Q2 after a power failure", key: "savingTheStateOfTheRelaysQ1AndQ2AfterAPowerFailure", type: "boolean",
				parameterNumber: 30, size: 1, defaultValue: 0,
				optionInactive: 0, inactiveDescription: "State is saved and brought back after a power failure",
				optionActive: 1, activeDescription: "State is not saved, outputs will be off after a power failure",
				description: "Saving the state of the relays Q1 and Q2 after a power failure"
		],
		[
				name: "Output Q1 Switch selection", key: "outputQ1SwitchSelection", type: "enum",
				parameterNumber: 63, size: 1, defaultValue: 0,
				values: [
						0: "When system is turned off the output is 0V (NC).",
						1: "When system is turned off the output is 230V (NO).",
				],
				description: "Set value means the type of the device that is connected to the Q1 output. The device type can be normally open (NO) or normally close (NC). "
		],
		[
				name: "Output Q2 Switch selection", key: "outputQ2SwitchSelection", type: "enum",
				parameterNumber: 64, size: 1, defaultValue: 0,
				values: [
						0: "When system is turned off the output is 0V (NC).",
						1: "When system is turned off the output is 230V (NO).",
				],
				description: "(Only for Qubino Flush 2 Relay) Set value means the type of the device that is connected to the Q2 output. The device type can be normally open (NO) or normally close (NC).  "
		]
]}