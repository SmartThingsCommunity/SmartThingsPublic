/**
 *  Copyright 2020 SmartThings
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */
metadata {
	definition(name: "Qubino Flush 1D Relay", namespace: "smartthings", author: "SmartThings", ocfDeviceType: "oic.d.switch") {
		capability "Actuator"
		capability "Configuration"
		capability "Health Check"
		capability "Switch"
		capability "Refresh"
		capability "Sensor"
		
		fingerprint mfr: "0159 ", prod: "0002", model: "0053", deviceJoinName: "Qubino Flush 1D Relay"
	}

	tiles(scale: 2) {
		multiAttributeTile(name: "switch", type: "lighting", width: 6, height: 4, canChangeIcon: true) {
			tileAttribute("device.switch", key: "PRIMARY_CONTROL") {
				attributeState "on", label: '${name}', action: "switch.off", icon: "st.switches.switch.on", backgroundColor: "#00A0DC"
				attributeState "off", label: '${name}', action: "switch.on", icon: "st.switches.switch.off", backgroundColor: "#ffffff"
			}
		}

		standardTile("refresh", "device.switch", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
			state "default", label: '', action: "refresh.refresh", icon: "st.secondary.refresh"
		}

		main "switch"
		details(["switch", "refresh"])
	}

	preferences {
	parameterMap.each {
		input (
			title: it.name,
			description: it.description,
			type: "paragraph",
			element: "paragraph"
		)

		switch(it.type) {
			case "boolRange":
				input(
					name: it.key + "Boolean", 
					type: "bool",
					title: "Enable",
					description: "If you disable this option, it will overwrite setting below.",
					defaultValue: it.defaultValue != it.disableValue,
					required: false
				)
				input(
					name: it.key, 
					type: "number",
					title: "Set value (range ${it.range})",
					defaultValue: it.defaultValue,
					range: it.range,
					required: false
				)
				break
			case "boolean":
				input(
					type: "paragraph",
					element: "paragraph",
					description: "Option enabled: ${it.activeDescription}\n" + 
						"Option disabled: ${it.inactiveDescription}" 
				)
				input(
					name: it.key, 
					type: "boolean",
					title: "Enable",
					defaultValue: it.defaultValue == it.activeOption,
					required: false
				)
				break
			case "enum":
				input(
					name: it.key, 
					title: "Select",
					type: "enum",
					options: it.values,
					defaultValue: it.defaultValue,
					required: false
				)
				break
			case "range":
				input(
					name: it.key, 
					type: "number",
					title: "Set value (range ${it.range})",
					defaultValue: it.defaultValue,
					range: it.range,
					required: false
				)
				break
		}
	}
}
}

def installed() {
	sendEvent(name: "checkInterval", value: 2 * 15 * 60 + 2 * 60, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID, offlinePingable: "1"])
	response(refresh())
	state.currentPreferencesState = [:]
	parameterMap.each {
		state.currentPreferencesState."$it.key" = [:]
		state.currentPreferencesState."$it.key".value = getPreferenceValue(it)
		if (it.type == "boolRange" && getPreferenceValue(it) == it.disableValue) {
			state.currentPreferencesState."$it.key".status = "disablePending"
		} else {
			state.currentPreferencesState."$it.key".status = "synced"
		}
	}
}

def updated() {
	initialize()
	parameterMap.each {
		if (isPreferenceChanged(it)) {
			log.debug "Preference ${it.key} has been updated from value: ${state.currentPreferencesState."$it.key".value} to ${settings."$it.key"}"
			state.currentPreferencesState."$it.key".status = "syncPending"
			if (it.type == "boolRange") {
				def preferenceName = it.key + "Boolean"
				if (notNullCheck(settings."$preferenceName")) {
					if (!settings."$preferenceName") {
						state.currentPreferencesState."$it.key".status = "disablePending"
					} else if (state.currentPreferencesState."$it.key".status == "disabled") {
						state.currentPreferencesState."$it.key".status = "syncPending"
					}
				} else {
					state.currentPreferencesState."$it.key".status = "syncPending"
				}
			}
		} else if (!state.currentPreferencesState."$it.key".value) {
			log.warn "Preference ${it.key} no. ${it.parameterNumber} has no value. Please check preference declaration for errors."
		}
	}
	syncConfiguration()
}

def configure() {
	def commands = []
	commands << zwave.manufacturerSpecificV2.manufacturerSpecificGet().format()
	commands << zwave.associationV1.associationSet(groupingIdentifier:1, nodeId:[zwaveHubNodeId]).format()
	commands << zwave.associationV1.associationSet(groupingIdentifier:2, nodeId:[zwaveHubNodeId]).format()
	commands << zwave.associationV1.associationSet(groupingIdentifier:3, nodeId:[zwaveHubNodeId]).format()
	commands << zwave.associationV1.associationSet(groupingIdentifier:4, nodeId:[zwaveHubNodeId]).format()
	commands << zwave.associationV1.associationSet(groupingIdentifier:5, nodeId:[zwaveHubNodeId]).format()
	commands << zwave.associationV1.associationSet(groupingIdentifier:6, nodeId:[zwaveHubNodeId]).format()
	delayBetween(commands, 500)
}


def initialize() {
	sendEvent(name: "checkInterval", value: 2 * 60 * 60 + 2 * 60, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])
}

def getCommandClassVersions() {
	[
		0x20: 1, //Basic
		0x5A: 1, //Device Reset Locally
		0x73: 1, //Powerlevel
		0x86: 2, //Version
		0x72: 2, //Manufacturer Specific
		0x27: 1, //Switch All
		0x25: 1, //Switch Binary
		0x32: 4, //Meter
		0x31: 5, //Sensor Multi Level 
		0x85: 2, //Association
		0x8E: 3, //Multi Channel Association
		0x59: 2, //Association Grp Info
		0x70: 2, //Configuration
	]
}

def parse(String description) {
	log.debug "parse() description: ${description}"
	def result = null
	def cmd = zwave.parse(description, commandClassVersions)
	if (cmd) {
		result = zwaveEvent(cmd)
		log.debug("'$description' parsed to $result")
	} else {
		log.debug("Couldn't zwave.parse '$description'")
	}
	return result
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd) {
	[name: "switch", value: cmd.value ? "on" : "off"]
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicSet cmd) {
	[name: "switch", value: cmd.value ? "on" : "off"]
}

def zwaveEvent(physicalgraph.zwave.commands.switchbinaryv1.SwitchBinaryReport cmd) {
	[name: "switch", value: cmd.value ? "on" : "off"]
}

def zwaveEvent(physicalgraph.zwave.commands.manufacturerspecificv2.ManufacturerSpecificReport cmd) {
	log.debug "manufacturerId:   $cmd.manufacturerId, manufacturerName: $cmd.manufacturerName, productId:        $cmd.productId, productTypeId:    $cmd.productTypeId"
	def msr = String.format("%04X-%04X-%04X", cmd.manufacturerId, cmd.productTypeId, cmd.productId)
	updateDataValue("MSR", msr)
	updateDataValue("manufacturer", cmd.manufacturerName)
	createEvent([descriptionText: "$device.displayName MSR: $msr", isStateChange: false])
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
	log.debug("Commands: ${cmd}")
	[:]
}

def on() {
    log.debug "on"
	delayBetween([
			zwave.basicV1.basicSet(value: 0xFF).format(),
			zwave.basicV1.basicGet().format()
	])
}

def off() {
    log.debug "off"
	delayBetween([
			zwave.basicV1.basicSet(value: 0x00).format(),
			zwave.basicV1.basicGet().format()
	])
}

def ping() {
	refresh()
}

def refresh() {
	def commands = []
	commands << zwave.basicV1.basicGet().format()
	if (getDataValue("MSR") == null) {
		commands << zwave.manufacturerSpecificV1.manufacturerSpecificGet().format()
	}
	delayBetween(commands)
}
//Preferences part

def zwaveEvent(physicalgraph.zwave.commands.configurationv2.ConfigurationReport cmd) {
	log.debug "Configuration report: ${cmd}"
	def preference = parameterMap.find( {it.parameterNumber == cmd.parameterNumber} )
	def key = preference.key
	def preferenceValue = getPreferenceValue(preference, cmd.scaledConfigurationValue)
	if (settings."$key" == preferenceValue) {
		state.currentPreferencesState."$key".value = settings."$key"
		state.currentPreferencesState."$key".status = "synced"
	} else if (preference.type == "boolRange") {
		if (state.currentPreferencesState."$key".status == "disablePending" && preferenceValue == preference.disableValue) {
			state.currentPreferencesState."$key".status = "disabled"
		} else {
			runIn(5, "syncConfiguration", [overwrite: true])
		}
	} else {
		state.currentPreferencesState."$key"?.status = "syncPending"
		runIn(5, "syncConfiguration", [overwrite: true])
	}
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
		case "boolRange":
			def parameterKeyBoolean = parameterKey + "Boolean"
			return !notNullCheck(settings."$parameterKeyBoolean") || settings."$parameterKeyBoolean" ? settings."$parameterKey" : preference.disableValue
		case "range":
			return settings."$parameterKey"
		default:
			return Integer.parseInt(settings."$parameterKey")
	}
}

private isPreferenceChanged(preference) {
	if (notNullCheck(settings."$preference.key")) {
		if (preference.type == "boolRange") {
			def boolName = preference.key + "Boolean"
			if (state.currentPreferencesState."$preference.key".status == "disabled") {
				return settings."$boolName"
			} else {
				return state.currentPreferencesState."$preference.key".value != settings."$preference.key" || !settings."$boolName"
			}
		} else {
			return state.currentPreferencesState."$preference.key".value != settings."$preference.key"
		}
	} else {
		return false
	}
}

private notNullCheck(value) {
	return value != null
}

private encap(cmd, endpoint = null) {
	if (cmd) {
		if (endpoint) {
			cmd = zwave.multiChannelV3.multiChannelCmdEncap(destinationEndPoint: endpoint).encapsulate(cmd)
		}

		if (zwaveInfo.zw.endsWith("s")) {
			zwave.securityV1.securityMessageEncapsulation().encapsulate(cmd).format()
		} else {
			cmd.format()
		}
	}
}

private syncConfiguration() {
	def commands = []
	parameterMap.each {
		if (state.currentPreferencesState."$it.key".status == "syncPending") {
            commands += encap(zwave.configurationV2.configurationSet(scaledConfigurationValue: getCommandValue(it), parameterNumber: it.parameterNumber, size: it.size))
		    commands += encap(zwave.configurationV2.configurationGet(parameterNumber: it.parameterNumber))
		} else if (state.currentPreferencesState."$it.key".status == "disablePending") {
			commands += encap(zwave.configurationV2.configurationSet(scaledConfigurationValue: it.disableValue, parameterNumber: it.parameterNumber, size: it.size))
			commands += encap(zwave.configurationV2.configurationGet(parameterNumber: it.parameterNumber))
		}
	}
	sendHubCommand(commands)
}

private getParameterMap() {[
	[
		name: "Input 1 switch type", key: "input1SwitchType", type: "boolean",
		parameterNumber: 1, size: 1, defaultValue: 1,
		optionInactive: 0, inactiveDescription: "mono-stable switch type (push button)",
		optionActive: 1, activeDescription: " bi-stable switch type ",
		description: "Defines the type of switch connected to Input 1 "
	],
	[
		name: "Input 2 contact type", key: "input2ContactType", type: "boolean",
		parameterNumber: 2, size: 1, defaultValue: 0,
		optionInactive: 0, inactiveDescription: "NO (normally open) input type",
		optionActive: 1, activeDescription: "1 -  NC (normally close) input type",
		description: "Defines the type of switch connected to Input 2 "
	],
	[
		name: "Saving the state of the relay after a power failure", key: "savingTheStateOfTheRelayAfterAPowerFailure", type: "boolean",
		parameterNumber: 30, size: 1, defaultValue: 0,
		optionInactive: 0, inactiveDescription: "Flush 1D relay module saves its state before power failure (it returns to the last position saved before a power failure)",
		optionActive: 1, activeDescription: "Flush 1D relay module does not save  the state after a power failure, it returns to   &amp;amp;amp;amp;quot;off&amp;amp;amp;amp;quot; position.",
		description: "Support saving the state of the relay after a power failure "
	],
	[
		name: "Output Switch selection", key: "outputSwitchSelection", type: "boolean",
		parameterNumber: 63, size: 1, defaultValue: 0,
		optionInactive: 0, inactiveDescription: "When system is turned off the output is 0V (NC).",
		optionActive: 1, activeDescription: "When system is turned off the output is 230V or 24V (NO).",
		description: "Support output Switch selection Set value means the type of the device that is connected to the output. The device type can be normally open (NO) or normally close (NC).  "
	]	
]}