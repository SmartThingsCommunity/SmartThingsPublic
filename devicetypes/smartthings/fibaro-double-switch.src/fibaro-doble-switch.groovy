/**
 *  Copyright 2020 SmartThings
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
	definition (name: "Fibaro Wall Double Switch", namespace: "smartthings", author: "SmartThings", mnmn: "SmartThings", vid: "generic-switch-power-energy", genericHandler: "Z-Wave") {
		capability "Actuator"
		capability "Configuration"
		capability "Health Check"
		capability "Energy Meter"
		capability "Power Meter"
		capability "Refresh"
		capability "Sensor"
		capability "Switch"

		command "reset"

		fingerprint mfr: "010F", prod: "1B01", model: "1000", deviceJoinName: "Fibaro Switch" //Raw Description zw:Ls type:1001 mfr:010F prod:1B01 model:1000 ver:5.01 zwv:6.02 lib:03 cc:5E,55,98,9F,56,6C,22 sec:25,85,8E,59,86,72,5A,73,32,70,71,75,5B,7A,60 role:05 ff:9D00 ui:9D00 epc:2
	}

	tiles(scale: 2){
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
		// Preferences template begin
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
		// Preferences template end
	}
}

def installed() {
	log.debug "Installed ${device.displayName}"
	// Device-Watch simply pings if no device events received for checkInterval duration of 32min = 30min + 2min lag time
	sendEvent(name: "checkInterval", value: 30 * 60 + 2 * 60, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID, offlinePingable: "1"])

	// Preferences template begin
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
	// Preferences template end
}

def updated() {
	sendHubCommand encap(zwave.multiChannelV3.multiChannelEndPointGet())

	// Preferences template begin
	parameterMap.each {
		if (state.currentPreferencesState."$it.key".value != settings."$it.key" && settings."$it.key") {
			log.debug "Preference ${it.key} has been updated from value: ${state.currentPreferencesState."$it.key".value} to ${settings."$it.key"}"
			state.currentPreferencesState."$it.key".status = "syncPending"
		} else if (!state.currentPreferencesState."$it.key".value) {
			log.warn "Preference ${it.key} no. ${it.parameterNumber} has no value. Please check preference declaration for errors."
		}
		if (it.type == "boolRange") {
			def preferenceName = it.key + "Boolean"
			if (!settings."$preferenceName") {
				state.currentPreferencesState."$it.key".status = "disablePending"
			} else if (state.currentPreferencesState."$it.key".status == "disabled") {
				state.currentPreferencesState."$it.key".status = "syncPending"
			}
		}
	}
	syncConfiguration()
	// Preferences template end
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
			return settings."$parameterKeyBoolean" ? settings."$parameterKey" : preference.disableValue
		default:
			return Integer.parseInt(settings."$parameterKey")
	}
}

/**
 * Mapping of command classes and associated versions used for this DTH
 */
private getCommandClassVersions() {
	[
			0x20: 1,  // Basic
			0x25: 1,  // Switch Binary
			0x30: 1,  // Sensor Binary
			0x31: 2,  // Sensor MultiLevel
			0x32: 3,  // Meter
			0x56: 1,  // Crc16Encap
			0x60: 3,  // Multi-Channel
			0x70: 2,  // Configuration
			0x72: 2,  // Manufacturer Specific
			0x73: 1,  // Powerlevel
			0x84: 1,  // WakeUp
			0x86: 2,  // Version
			0x98: 2   // Security
	]
}

def configure() {
	log.debug "Configure..."
	response([
			encap(zwave.multiChannelV3.multiChannelEndPointGet()),
			encap(zwave.manufacturerSpecificV2.manufacturerSpecificGet())
	])
}

def parse(String description) {
	def result = null
	if (description.startsWith("Err")) {
		result = createEvent(descriptionText:description, isStateChange:true)
	} else if (description != "updated") {
		def cmd = zwave.parse(description)
		if (cmd) {
			result = zwaveEvent(cmd, null)
		}
	}
	log.debug "parsed '${description}' to ${result.inspect()}"
	result
}

def zwaveEvent(physicalgraph.zwave.commands.configurationv2.ConfigurationReport cmd, enpoint = null) {
	// Preferences template begin
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
	// Preferences template end
}

// cmd.endPoints includes the USB ports but we don't want to expose them as child devices since they cannot be controlled so hardcode to just include the outlets
def zwaveEvent(physicalgraph.zwave.commands.multichannelv3.MultiChannelEndPointReport cmd, ep = null) {
	if(!childDevices) {
		addChildSwitches(cmd.endPoints)
	}
	response([
			refreshAll()
	])
}

def zwaveEvent(physicalgraph.zwave.commands.manufacturerspecificv2.ManufacturerSpecificReport cmd, ep = null) {
	def mfr = Integer.toHexString(cmd.manufacturerId)
	def model = Integer.toHexString(cmd.productId)
	updateDataValue("mfr", mfr)
	updateDataValue("model", model)
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
	def encapsulatedCommand = cmd.encapsulatedCommand()
	zwaveEvent(encapsulatedCommand, cmd.sourceEndPoint as Integer)
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd, ep = null) {
	log.debug "Basic ${cmd}" + (ep ? " from endpoint $ep" : "")
	handleSwitchReport(ep, cmd)
}

def zwaveEvent(physicalgraph.zwave.commands.switchbinaryv1.SwitchBinaryReport cmd, ep = null) {
	log.debug "Binary ${cmd}" + (ep ? " from endpoint $ep" : "")
	handleSwitchReport(ep, cmd)
}

private handleSwitchReport(endpoint, cmd) {
	def value = cmd.value ? "on" : "off"
	endpoint ? changeSwitch(endpoint, value) : []
}

private changeSwitch(endpoint, value) {
	if (endpoint == 1) {
		createEvent(name: "switch", value: value, isStateChange: true, descriptionText: "Switch ${endpoint} is ${value}")
	} else {
		String childDni = "${device.deviceNetworkId}:$endpoint"
		def child = childDevices.find { it.deviceNetworkId == childDni }
		child?.sendEvent(name: "switch", value: value, isStateChange: true, descriptionText: "Switch ${endpoint} is ${value}")
	}
}

def zwaveEvent(physicalgraph.zwave.commands.meterv3.MeterReport cmd, ep = null) {
	log.debug "Meter ${cmd}" + (ep ? " from endpoint $ep" : "")
	if (ep == 1) {
		createEvent(createMeterEventMap(cmd))
	} else if(ep) {
		String childDni = "${device.deviceNetworkId}:$ep"
		def child = childDevices.find { it.deviceNetworkId == childDni }
		child?.sendEvent(createMeterEventMap(cmd))
	} else {
		createEvent([isStateChange:  false, descriptionText: "Wattage change has been detected. Refreshing each endpoint"])
	}
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

private onOffCmd(value, endpoint = 1) {
	delayBetween([
			encap(zwave.basicV1.basicSet(value: value), endpoint),
			encap(zwave.basicV1.basicGet(), endpoint),
			"delay 3000",
			encap(zwave.meterV3.meterGet(scale: 0), endpoint),
			encap(zwave.meterV3.meterGet(scale: 2), endpoint)
	])
}

private refreshAll(includeMeterGet = true) {

	def endpoints = [1]

	childDevices.each {
		def switchId = getSwitchId(it.deviceNetworkId)
		if (switchId != null) {
			endpoints << switchId
		}
	}
	sendHubCommand refresh(endpoints,includeMeterGet)
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
		//cmds << encap(zwave.configurationV2.configurationSet(scaledConfigurationValue: 2, parameterNumber: 11, size: 1))
		//cmds << encap(zwave.configurationV2.configurationGet(parameterNumber: 11))

		//state.currentPreferencesState."ledFrame–ColourWhenOn".value = 2

		if (includeMeterGet) {
			//cmds << encap(zwave.meterV3.meterGet(scale: 0), it)
			//cmds << encap(zwave.meterV3.meterGet(scale: 2), it)
		}
	}
	delayBetween(cmds, 200)
}

private resetAll() {
	childDevices.each { childReset(it.deviceNetworkId) }
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
		log.debug "encap command: ${cmd} "
		if (zwaveInfo.zw.endsWith("s")) {
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
			addChildDevice("Child Metering Switch", childDni, device.getHub().getId(), [
					completedSetup	: true,
					label			: componentLabel,
					isComponent		: false
			])
		} catch(Exception e) {
			log.debug "Exception: ${e}"
		}
	}
}

private getParameterMap() {[
	[
		name: "Remember device state", key: "rememberDeviceState", type: "boolean",
		parameterNumber: 1, size: 1, defaultValue: 1,
		optionInactive: 0, inactiveDescription: "remains switched off after restoring power",
		optionActive: 1, activeDescription: "restores remembered state after restoring power",
		description: "This parameter determines how the device will react in the event of power supply failure (e.g. power outage). The parameter is not relevant for outputs set to pulse mode (parameter 150/151 set to 2)."
	],
	[
		name: "LED frame – power limit", key: "ledFrame–PowerLimit", type: "range",
		parameterNumber: 10, size: 4, defaultValue: 36800,
		range: "500..36800",
		 description: "This parameter determines maximum active power. Exceeding it results in the LED frame flashing violet. Function is active only when parameter 11 is set to 8 or 9."
	],
	[
			name: "LED frame – colour when ON", key: "ledFrame–ColourWhenOn", type: "enum",
			parameterNumber: 11, size: 1, defaultValue: 1,
			values: [
					0: "LED disabled",
					1: "White",
					2: "Red",
					3: "Green",
					4: "Blue",
					5: "Yellow",
					6: "Cyan",
					7: "Magenta",
					8: "colour changes smoothly depending on measured power",
					9: "colour changes in steps depending on measured power"
			],
			description: "This parameter defines the LED colour when thedevice is ON. When set to 8 or 9, LED frame colour will change depending on he measured power and parameter 10. Other colours are set permanently and do not depend on power consumption."
	],
	[
		name: "LED frame – colour when OFF", key: "ledFrame–ColourWhenOff", type: "enum",
		parameterNumber: 12, size: 1, defaultValue: 0,
		values: [
			0: "LED disabled",
			1: "White",
			2: "Red",
			3: "Green",
			4: "Blue",
			5: "Yellow",
			6: "Cyan",
			7: "Magenta"
		],
		description: "This parameter defines the LED colour when the device is OFF."
	],
	[
		name: "LED frame – brightness", key: "ledFrame–Brightness", type: "range",
		parameterNumber: 13, size: 1, defaultValue: 100,
		range: "1..102",
		description: "This parameter allows to adjust the LED frame brightness."
	],
	[
		name: "First channel – operating mode", key: "firstChannel–OperatingMode", type: "enum",
		parameterNumber: 150, size: 1, defaultValue: 0,
		values: [
			0: "standard operation",
			1: "delayed OFF",
			2: "single pulse"
		],
		description: "This parameter allows to choose operating for the 1st channel ."
	],
	[
		name: "Second channel – operating mode", key: "secondChannel–OperatingMode", type: "enum",
		parameterNumber: 151, size: 1, defaultValue: 0,
		values: [
			0: "standard operation",
			1: "delayed OFF",
			2: "single pulse"
		],
		description: "This parameter allows to choose operating for the 2nd channel."
	],
	[
		name: "First channel - reaction to switch for delayed OFF / pulse modes", key: "firstChannel-ReactionToSwitchForDelayedOff/PulseModes", type: "enum",
		parameterNumber: 152, size: 1, defaultValue: 0,
		values: [
			0: "cancel mode and set default state",
			1: "no reaction - mode runs until it ends",
			2: "reset timer - start counting from the beginning"
		],
		description: "This parameter determines how the device in timed mode reacts to pushing the button for 1st channel. The parameter is relevant only for button toggles modes (parameter 20 set to 1 or 3)."
	],
	[
		name: "Second channel - reaction to switch for delayed OFF / pulse modes", key: "secondChannel-ReactionToSwitchForDelayedOff/PulseModes", type: "enum",
		parameterNumber: 153, size: 1, defaultValue: 0,
		values: [
			0: "cancel mode and set default state",
			1: "no reaction - mode runs until it ends",
			2: "reset timer - start counting from the beginning"
		],
		description: "This parameter determines how the device in timed mode reacts to pushing the button for 2nd channel. The parameter is relevant only for button toggles modes (parameter 20 set to 1 or 3)."
	],
	[
		name: "First channel - time parameter for delayed OFF / pulse modes", key: "firstChannel-TimeParameterForDelayedOff/PulseModes", type: "range",
		parameterNumber: 154, size: 2, defaultValue: 50,
		range: "1..32000",
		description: "This parameter allows to set time parameter used in timed modes for 1st channel (parameter 150). Delay time for switching off or duration of the pulse."
	],
	[
		name: "Second channel - time parameter for delayed OFF / pulse modes", key: "secondChannel-TimeParameterForDelayedOff/PulseModes", type: "range",
		parameterNumber: 155, size: 2, defaultValue: 50,
		range: "1..32000",
		description: "This parameter allows to set time parameter used in timed modes for 2nd channel (parameter 151). Delay time for switching off or duration of the pulse."
	],
	/*[
		name: "First channel – Switch ON value sent to 2nd and 3rd association groups", key: "firstChannel–SwitchOnValueSentTo2NdAnd3RdAssociationGroups", type: "range",
		parameterNumber: 156, size: 2, defaultValue: 255,
		range: "255",
		description: "This parameter defines value sent with Switch ON command to devices associated in 2nd and 3rd association group for manual changes of Endpoint 1 state."
	],
	[
		name: "First channel – Switch OFF value sent to 2nd and 3rd association groups", key: "firstChannel–SwitchOffValueSentTo2NdAnd3RdAssociationGroups", type: "range",
		parameterNumber: 157, size: 2, defaultValue: 0,
		range: "255",
		description: "This parameter defines value sent with Switch OFF command to devices associated in 2nd and 3rd association group for manual changes of Endpoint 1 state."
	],
	[
		name: "First channel – Double Click value sent to 2nd and 3rd association groups", key: "firstChannel–DoubleClickValueSentTo2NdAnd3RdAssociationGroups", type: "range",
		parameterNumber: 158, size: 2, defaultValue: 99,
		range: "255",
		description: "This parameter defines value sent with Double Click command to devices associated in 2nd and 3rd association group for manual changes of Endpoint 1 state."
	],
	[
		name: "Second channel – Switch ON value sent to 4th and 5th association groups", key: "secondChannel–SwitchOnValueSentTo4ThAnd5ThAssociationGroups", type: "range",
		parameterNumber: 159, size: 2, defaultValue: 255,
		range: "255",
		description: "This parameter defines value sent with Switch ON command to devices associated in 4th and  5th association group for manual changes of Endpoint 2 state."
	],
	[
		name: "Second channel – Switch OFF value sent to 4th and 5th association groups", key: "secondChannel–SwitchOffValueSentTo4ThAnd5ThAssociationGroups", type: "range",
		parameterNumber: 160, size: 2, defaultValue: 0,
		range: "255",
		description: "This parameter defines value sent with Switch OFF command to devices associated in 4th and 5th association group for manual changes of Endpoint 2 state."
	],
	[
		name: "Second channel – Double Click value sent to 4th and 5th association groups", key: "secondChannel–DoubleClickValueSentTo4ThAnd5ThAssociationGroups", type: "range",
		parameterNumber: 161, size: 2, defaultValue: 99,
		range: "255",
		description: "This parameter defines value sent with Double Click command to devices associated in 4th and 5th association group for manual changes of Endpoint 2 state."
	],*/
	[
		name: "First channel – overload safety switch", key: "firstChannel–OverloadSafetySwitch", type: "boolRange",
		parameterNumber: 2, size: 4, defaultValue: 0,
		range: "10..45000", disableValue: 0,
		description: "This function allows to turn off the controlled device in case of exceeding the defined power. Controlled device can be turned back on via button or sending a control frame."
	],
	[
		name: "Buttons operation", key: "buttonsOperation", type: "enum",
		parameterNumber: 20, size: 1, defaultValue: 1,
		values: [
			1: "1st and 2nd button toggle the load",
			2: "1st button turns the load ON, 2nd button turns the load OFF",
			3: "device works in 2-way/3-way switch configuration"
		],
		description: "This parameter defines how device buttons should control the channels."
	],
	[
		name: "Buttons orientation", key: "buttonsOrientation", type: "boolean",
		parameterNumber: 24, size: 1, defaultValue: 0,
		optionInactive: 0, inactiveDescription: "default (1st button controls 1st channel, 2nd button controls 2nd channel)",
		optionActive: 1, activeDescription: "reversed (1st button controls 2nd channel, 2nd button controls 1st channel)",
		description: "This parameter allows reversing the operation of the buttons."
	],
	[
		name: "Outputs orientation", key: "outputsOrientation", type: "boolean",
		parameterNumber: 25, size: 1, defaultValue: 0,
		optionInactive: 0, inactiveDescription: "default (Q1 - 1st channel, Q2 - 2nd channel)",
		optionActive: 1, activeDescription: "reversed (Q1 - 2nd channel, Q2 - 1st channel)",
		description: "This parameter allows reversing the operation of Q1 and Q2 without changing the wiring (e.g. in case of invalid connection). Changing orientation turns both outputs off."
	],
	[
		name: "Second channel – overload safety switch", key: "secondChannel–OverloadSafetySwitch", type: "boolRange",
		parameterNumber: 3, size: 4, defaultValue: 0,
		range: "10..36200", disableValue: 0,
		description: "This function allows to turn off the controlled device in case of exceeding the defined power. Controlled device can be turned back on via button or sending a control frame."
	],
	/*[
		name: "Alarm configuration - 1st slot", key: "alarmConfiguration-1StSlot", type: "enum",
		parameterNumber: 30, size: 4, defaultValue: 0,
		values: [
			1: "[MSB] – Notification Type",
			2: "Notification Value",
			3: "Event/State Parameters",
			4: "[LSB] – action"
		],
		description: "This parameter determines to which alarm frames and how the device should react. The parameters consist of 4 bytes, three most significant bytes are set according to the official Z-Wave protocol specification."
	],
	[
		name: "Alarm configuration - 2nd slot", key: "alarmConfiguration-2NdSlot", type: "enum",
		parameterNumber: 31, size: 4, defaultValue: 0,
		values: [
			1: "[MSB] – Notification Type",
			2: "Notification Value",
			3: "Event/State Parameters",
			4: "[LSB] – action"
		],
		description: "This parameter determines to which alarm frames and how the device should react. The parameters consist of 4 bytes, three most significant bytes are set according to the official Z-Wave protocol specification."
	],
	[
		name: "Alarm configuration - 3rd slot", key: "alarmConfiguration-3RdSlot", type: "enum",
		parameterNumber: 32, size: 4, defaultValue: 0,
		values: [
			1: "[MSB] – Notification Type",
			2: "Notification Value",
			3: "Event/State Parameters",
			4: "[LSB] – action"
		],
		description: "This parameter determines to which alarm frames and how the device should react. The parameters consist of 4 bytes, three most significant bytes are set according to the official Z-Wave protocol specification."
	],
	[
		name: "Alarm configuration - 4th slot", key: "alarmConfiguration-4ThSlot", type: "enum",
		parameterNumber: 33, size: 4, defaultValue: 0,
		values: [
			0: "Notification Type",
			1: "[MSB] – Notification Type",
			2: "Notification Status",
			3: "Event/State Parameters",
			4: "[LSB] – action"
		],
		description: "This parameter determines to which alarm frames and how the device should react. The parameters consist of 4 bytes, three most significant bytes are set according to the official Z-Wave protocol specification."
	],
	[
		name: "Alarm configuration - 5th slot", key: "alarmConfiguration-5ThSlot", type: "enum",
		parameterNumber: 34, size: 4, defaultValue: 0,
		values: [
			1: "[MSB] – Notification Type",
			2: "Notification Status",
			3: "Event/State Parameters",
			4: "[LSB] - action"
		],
		description: "This parameter determines to which alarm frames and how the device should react. The parameters consist of 4 bytes, three most significant bytes are set according to the official Z-Wave protocol specification."
	],
	[
		name: "Alarm configuration – duration", key: "alarmConfiguration–Duration", type: "range",
		parameterNumber: 35, size: 2, defaultValue: 600,
		range: "1..32400",
		description: "This parameter defines duration of alarm sequence.  When time set in this parameter elapses, alarm is cancelled, LED frame and relay restore normal operation, but do not recover state from before the alarm."
	],
	[
		name: "First button – scenes sent", key: "firstButton–ScenesSent", type: "enum",
		parameterNumber: 40, size: 1, defaultValue: 0,
		values: [
			1: "Key pressed 1 time",
			2: "Key pressed 2 time",
			4: "Key pressed 3 time",
			8: "Key hold down and key released"
		],
		description: "This parameter determines which actions result in sending scene IDs assigned to them. Values can be combined (e.g. 1+2=3 means that scenes for single and double click are sent). Enabling scenes for triple click disables entering the device in learn mode by triple clicking."
	],
	[
		name: "Second button – scenes sent", key: "secondButton–ScenesSent", type: "enum",
		parameterNumber: 41, size: 1, defaultValue: 0,
		values: [
			1: "Key pressed 1 time",
			2: "Key pressed 2 time",
			4: "Key pressed 3 time",
			8: "Key hold down and key released"
		],
		description: "This parameter determines which actions result in sending scene IDs assigned to them. Values can be combined (e.g. 1+2=3 means that scenes for single and double click are sent). Enabling scenes for triple click disables entering the device in learn mode by triple clicking."
	],*/
	[
		name: "Power reports – include self-consumption", key: "powerReports–IncludeSelf-Consumption", type: "boolean",
		parameterNumber: 60, size: 1, defaultValue: 0,
		optionInactive: 0, inactiveDescription: "Self-consumption not included",
		optionActive: 1, activeDescription: "Self-consumption included",
		description: "This parameter determines whether the power measurements should include power consumed by the device itself."
	],
	[
		name: "Power reports for first channel – on change", key: "powerReportsForFirstChannel–OnChange", type: "boolRange",
		parameterNumber: 61, size: 2, defaultValue: 15,
		range: "1..500", disableValue: 0,
		description: "This parameter defines minimal change (from the last reported) in measured power that results in sending new report. For loads under 50W the parameter is irrelevant, report are sent every 5W change."
	],
	[
		name: "Power reports for first channel – periodic", key: "powerReportsForFirstChannel–Periodic", type: "boolRange",
		parameterNumber: 62, size: 2, defaultValue: 3600,
		range: "30..32400", disableValue: 0,
		description: "This parameter defines reporting interval for measured power. Periodic reports are independent from changes in value (parameter 61)."
	],
	[
		name: "Power reports for second channel – on change", key: "powerReportsForSecondChannel–OnChange", type: "boolRange",
		parameterNumber: 63, size: 2, defaultValue: 15,
		range: "1..500", disableValue: 0,
		description: "This parameter defines minimal change (from the last reported) in measured power that results in sending new report. For loads under 50W the parameter is irrelevant, report are sent every 5W change."
	],
	[
		name: "Power reports for second channel – periodic", key: "powerReportsForSecondChannel–Periodic", type: "boolRange",
		parameterNumber: 64, size: 2, defaultValue: 3600,
		range: "30..32400", disableValue: 0,
		description: "This parameter defines reporting interval for measured power. Periodic reports are independent from changes in value (parameter 63)."
	],
	[
		name: "Energy reports for first channel – on change", key: "energyReportsForFirstChannel–OnChange", type: "boolRange",
		parameterNumber: 65, size: 2, defaultValue: 10,
		range: "1..500", disableValue: 0,
		description: "This parameter defines minimal change (from the last reported) in measured energy that results in sending new report."
	],
	[
		name: "Energy reports for first channel – periodic", key: "energyReportsForFirstChannel–Periodic", type: "boolRange",
		parameterNumber: 66, size: 2, defaultValue: 3600,
		range: "30..32400", disableValue: 0,
		description: "This parameter defines reporting interval for measured energy. Periodic reports are independent from changes in value (parameter 66)."
	],
	[
		name: "Energy reports for second channel – on change", key: "energyReportsForSecondChannel–OnChange", type: "boolRange",
		parameterNumber: 67, size: 2, defaultValue: 10,
		range: "1..500", disableValue: 0,
		description: "This parameter defines minimal change (from the last reported) in measured energy that results in sending new report."
	],
	[
		name: "Energy reports for second channel – periodic", key: "energyReportsForSecondChannel–Periodic", type: "boolRange",
		parameterNumber: 68, size: 2, defaultValue: 3600,
		range: "30..32400", disableValue: 0,
		description: "This parameter defines reporting interval for measured energy. Periodic reports are independent from changes in value (parameter 67)."
	]
]}