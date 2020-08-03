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
	definition (name: "Fibaro Walli Double Switch", namespace: "fibargroup", author: "SmartThings", mnmn: "SmartThings", vid: "generic-switch-power-energy", genericHandler: "Z-Wave") {
		capability "Actuator"
		capability "Configuration"
		capability "Health Check"
		capability "Energy Meter"
		capability "Power Meter"
		capability "Refresh"
		capability "Sensor"
		capability "Switch"

		command "reset"

		// Fibaro Walli Double Switch FGWDSEU-221
		// Raw Description zw:Ls type:1001 mfr:010F prod:1B01 model:1000 ver:5.01 zwv:6.02 lib:03 cc:5E,55,98,9F,56,6C,22 sec:25,85,8E,59,86,72,5A,73,32,70,71,75,5B,7A,60 role:05 ff:9D00 ui:9D00 epc:2
		fingerprint mfr: "010F", prod: "1B01", model: "1000", deviceJoinName: "Fibaro Switch"
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
		state.currentPreferencesState."$it.key".status = "synced"
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
		// boolean values are returned as strings from the UI preferences
			return settings."$parameterKey" == 'true' ? preference.optionActive : preference.optionInactive
		case "range":
			return settings."$parameterKey"
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
			encap(zwave.multiChannelV3.multiChannelEndPointGet())
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
	} else {
		state.currentPreferencesState."$key"?.status = "syncPending"
		runIn(5, "syncConfiguration", [overwrite: true])
	}
	// Preferences template end
}

def zwaveEvent(physicalgraph.zwave.commands.multichannelv3.MultiChannelEndPointReport cmd, ep = null) {
	if(!childDevices) {
		addChildSwitches(cmd.endPoints)
	}
	response([
		refreshAll()
	])
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
		String childDni = "${device.deviceNetworkId}-$endpoint"
		def child = childDevices.find { it.deviceNetworkId == childDni }
		child?.sendEvent(name: "switch", value: value, isStateChange: true, descriptionText: "Switch ${endpoint} is ${value}")
	}
}

def zwaveEvent(physicalgraph.zwave.commands.meterv3.MeterReport cmd, ep = null) {
	log.debug "Meter ${cmd}" + (ep ? " from endpoint $ep" : "")
	if (ep == 1) {
		createEvent(createMeterEventMap(cmd))
	} else if(ep) {
		String childDni = "${device.deviceNetworkId}-$ep"
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

def childOn(deviceNetworkId = null) {
	childOnOff(deviceNetworkId, 0xFF)
}

def childOff(deviceNetworkId = null) {
	childOnOff(deviceNetworkId, 0x00)
}

def childOnOff(deviceNetworkId, value) {
	def switchId = deviceNetworkId ? getSwitchId(deviceNetworkId) : 2
	if (switchId != null) sendHubCommand onOffCmd(value, switchId)
}

private onOffCmd(value, endpoint = 1) {
	delayBetween([
		encap(zwave.basicV1.basicSet(value: value), endpoint),
		encap(zwave.basicV1.basicGet(), endpoint)
	], 1000)
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

def childRefresh(deviceNetworkId = null, includeMeterGet = true) {
	def switchId = deviceNetworkId ? getSwitchId(deviceNetworkId) : 2
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
	childDevices.each { childReset(it.deviceNetworkId) }
	sendHubCommand reset()
}

def childReset(deviceNetworkId = null) {
	def switchId = deviceNetworkId ? getSwitchId(deviceNetworkId) : 2
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
	def split = deviceNetworkId?.split("-")
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
			String childDni = "${device.deviceNetworkId}-$endpoint"
			def componentLabel = device.displayName + " ${endpoint}"
			addChildDevice("Fibaro Double Switch 2 - USB", childDni, device.getHub().getId(), [
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
		name: "LED frame - colour when ON", key: "ledFrame-ColourWhenOn", type: "enum",
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
			8: "colour changes smoothly depending on the measured power",
			9: "colour changes in steps depending on the measured power"
		],
		description: "LED colour when the device is ON. When set to 8 or 9, LED frame colour will change depending on the measured power and parameter 10. Other colours are set permanently and do not depend on the power consumption."
	],
	[
		name: "LED frame - colour when OFF", key: "ledFrame-ColourWhenOff", type: "enum",
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
		description: "LED colour when the device is OFF."
	],
	[
		name: "LED frame - brightness", key: "ledFrame-Brightness", type: "range",
		parameterNumber: 13, size: 1, defaultValue: 100,
		range: "1..102",
		description: "Adjust the LED frame brightness."
	],
	[
		name: "Buttons operation", key: "buttonsOperation", type: "enum",
		parameterNumber: 20, size: 1, defaultValue: 1,
		values: [
			1: "1st and 2nd button toggle the load",
			2: "1st button turns the load ON, 2nd button turns the load OFF",
			3: "device works in 2-way/3-way switch configuration"
		],
		description: "How device buttons should control the channels."
	],
	[
		name: "Buttons orientation", key: "buttonsOrientation", type: "boolean",
		parameterNumber: 24, size: 1, defaultValue: 0,
		optionInactive: 0, inactiveDescription: "default (1st button controls 1st channel, 2nd button controls 2nd channel)",
		optionActive: 1, activeDescription: "reversed (1st button controls 2nd channel, 2nd button controls 1st channel)",
		description: "Reverse the operation of the buttons."
	],
	[
		name: "Outputs orientation", key: "outputsOrientation", type: "boolean",
		parameterNumber: 25, size: 1, defaultValue: 0,
		optionInactive: 0, inactiveDescription: "default (Q1 - 1st channel, Q2 - 2nd channel)",
		optionActive: 1, activeDescription: "reversed (Q1 - 2nd channel, Q2 - 1st channel)",
		description: "Reverse the operation of Q1 and Q2 without changing the wiring (e.g. in case of invalid connection). Changing orientation turns both outputs off."
	]
]}