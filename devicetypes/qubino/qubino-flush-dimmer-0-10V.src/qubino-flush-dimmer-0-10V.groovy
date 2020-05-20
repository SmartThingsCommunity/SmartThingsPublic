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
	definition(name: "Qubino Flush Dimmer 0-10V", namespace: "qubino", author: "SmartThings", mnmn: "SmartThings", ocfDeviceType: "oic.d.switch", runLocally: false, executeCommandsLocally: false) {
		capability "Actuator"
		capability "Configuration"
		capability "Health Check"
		capability "Refresh"
		capability "Sensor"
		capability "Switch"
		capability "Switch Level"

		// Qubino Flush Dimmer 0-10V - ZMNHVD
		// Raw Description: zw:L type:1100 mfr:0159 prod:0001 model:0053 ver:2.04 zwv:4.34 lib:03 cc:5E,86,5A,72,73,27,25,26,85,8E,59,70 ccOut:20,26 role:05 ff:9C00 ui:9C00
		fingerprint mfr: "0159", prod: "0001", model: "0053", deviceJoinName: "Qubino Dimmer"
	}

	tiles(scale: 2) {
		multiAttributeTile(name: "switch", type: "lighting", width: 6, height: 4, canChangeIcon: true) {
			tileAttribute("device.switch", key: "PRIMARY_CONTROL") {
				attributeState "on", label: '${name}', action: "switch.off", icon: "st.switches.switch.on", backgroundColor: "#00a0dc", nextState: "turningOff"
				attributeState "off", label: '${name}', action: "switch.on", icon: "st.switches.switch.off", backgroundColor: "#ffffff", nextState: "turningOn"
				attributeState "turningOn", label: '${name}', action: "switch.off", icon: "st.switches.switch.on", backgroundColor: "#00a0dc", nextState: "turningOff"
				attributeState "turningOff", label: '${name}', action: "switch.on", icon: "st.switches.switch.off", backgroundColor: "#ffffff", nextState: "turningOn"
			}
			tileAttribute("device.level", key: "SLIDER_CONTROL") {
				attributeState "level", action: "switch level.setLevel"
			}
		}

		standardTile("refresh", "device.switch", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
			state "default", label: '', action: "refresh.refresh", icon: "st.secondary.refresh"
		}

		valueTile("level", "device.level", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "level", label: '${currentValue} %', unit: "%", backgroundColor: "#ffffff"
		}

		main(["switch"])
		details(["switch", "level", "refresh"])

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
	// Device-Watch simply pings if no device events received for 32min(checkInterval)
	sendEvent(name: "checkInterval", value: 2 * 15 * 60 + 2 * 60, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID, offlinePingable: "1"])

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
	readConfigurationFromTheDevice()
	// Preferences template end
}

def updated() {
// Device-Watch simply pings if no device events received for 32min(checkInterval)
	sendEvent(name: "checkInterval", value: 2 * 15 * 60 + 2 * 60, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID, offlinePingable: "1"])

	// Preferences template begin
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
	// Preferences template end
}

private readConfigurationFromTheDevice() {
	def commands = []
	parameterMap.each {
		state.currentPreferencesState."$it.key".status = "reverseSyncPending"
		commands += encap(zwave.configurationV2.configurationGet(parameterNumber: it.parameterNumber))
	}
	sendHubCommand(commands)
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

def getCommandClassVersions() {
	[
		0x20:1, // Basic
		0x25:1, // Switch Binary
		0x26:3, // Switch Multilevel
		0x27:1, // Switch All
		0x59:1, // Association Grp Info
		0x5A:1, // Device Reset Locally
		0x70:2, // Configuration
		0x72:2, // Manufacturer Specific
		0x73:1, // Powerlevel
		0x85:2, // Association
		0x86:1, // Version
		0x8E:2  // Multi Channel Association
	]
}

def configure() {
	def commands = []

	/*
		Association Groups:
		Group 1: Lifeline group (reserved for communication with the primary gateway (hub))
		Group 2: Basic on/off (status change report for I1 input)
		Group 3: Start level change/stop (status change report for I1 input). Working only when the Parameter no. 1 is set to mono stable switch type.
		Group 4: Multilevel set (status change report of the Flush Dimmer 0-10V). Working only when the Parameter no. 1 is set to mono stable switch type.
		Group 5: Multilevel sensor report (status change report of the analogue sensor)
		Group 6: Multilevel sensor report (status change report of the temperature sensor)
	*/
	commands << zwave.manufacturerSpecificV2.manufacturerSpecificGet().format()
	commands << zwave.associationV1.associationSet(groupingIdentifier:1, nodeId:[zwaveHubNodeId]).format()
	commands << zwave.associationV1.associationSet(groupingIdentifier:2, nodeId:[zwaveHubNodeId]).format()
	commands << zwave.associationV1.associationSet(groupingIdentifier:3, nodeId:[zwaveHubNodeId]).format()
	commands << zwave.associationV1.associationSet(groupingIdentifier:4, nodeId:[zwaveHubNodeId]).format()
	commands << zwave.associationV1.associationSet(groupingIdentifier:5, nodeId:[zwaveHubNodeId]).format()
	commands << zwave.associationV1.associationSet(groupingIdentifier:6, nodeId:[zwaveHubNodeId]).format()
	commands << zwave.multiChannelV3.multiChannelEndPointGet().format()
	commands + refresh()

	response(delayBetween(commands, 100))
}

def parse(String description) {
	log.debug "parse() / description: ${description}"

	def result = null
	def cmd = zwave.parse(description)
	//def cmd = zwave.parse(description, commandClassVersions)
	if (cmd) {
		result = zwaveEvent(cmd)
	}

	log.debug "Parse returned ${result}"
	return result
}

def zwaveEvent(physicalgraph.zwave.commands.configurationv2.ConfigurationReport cmd) {
	// Preferences template begin
	log.debug "Configuration report: ${cmd}"
	def preference = parameterMap.find( {it.parameterNumber == cmd.parameterNumber} )
	def key = preference.key
	def preferenceValue = getPreferenceValue(preference, cmd.scaledConfigurationValue)

	if(state.currentPreferencesState."$key".status == "reverseSyncPending"){
		log.debug "reverseSyncPending"
		state.currentPreferencesState."$key".value = preferenceValue
		state.currentPreferencesState."$key".status = "synced"
	} else {
		def preferenceKey = preference.key
		def settingsKey = settings."$key"
		log.debug "preference.key: ${preferenceKey}"
		log.debug "settings.key: ${settingsKey}"
		log.debug "preferenceValue: ${preferenceValue}"

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
		case "boolRange":
			def parameterKeyBoolean = parameterKey + "Boolean"
			return !notNullCheck(settings."$parameterKeyBoolean") || settings."$parameterKeyBoolean" ? settings."$parameterKey" : preference.disableValue
		case "range":
			return settings."$parameterKey"
		default:
			return Integer.parseInt(settings."$parameterKey")
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

def zwaveEvent(physicalgraph.zwave.commands.multichannelv3.MultiChannelEndPointReport cmd, ep = null) {
	log.info "MultiChannelEndPointReport: ${cmd}"
	if (!childDevices) {
		createChildDevice()
	}
	response([
			refresh()
	])
}

def createChildDevice() {
	try {
		String childDni = "${device.deviceNetworkId}-2"
		String componentLabel =	 "Qubino Temperature Sensor"

		addChildDevice("Child Temperature Sensor", childDni, device.hub.id,[
				completedSetup: true,
				label: componentLabel,
				isComponent: false
		])
	} catch(Exception e) {
		log.debug "Exception: ${e}"
	}
}

def getChildId(deviceNetworkId) {
	def split = deviceNetworkId?.split("-")
	return (split.length > 1) ? split[1] as Integer : null
}

def zwaveEvent(physicalgraph.zwave.commands.multichannelv3.MultiChannelCmdEncap cmd, ep = null) {
	log.debug "Multichannel command ${cmd}" + (ep ? " from endpoint $ep" : "")
	def encapsulatedCommand = cmd.encapsulatedCommand()
	zwaveEvent(encapsulatedCommand, cmd.sourceEndPoint as Integer)
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd, ep = null) {
	log.debug "BasicReport: ${cmd}"
	dimmerEvents(cmd)
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicSet cmd, ep = null) {
	log.debug "BasicSet: ${cmd}"
	dimmerEvents(cmd)
}

def zwaveEvent(physicalgraph.zwave.commands.switchmultilevelv3.SwitchMultilevelReport cmd, ep = null) {
	log.debug "SwitchMultilevelReport: ${cmd}"
	dimmerEvents(cmd)
}

def zwaveEvent(physicalgraph.zwave.commands.switchmultilevelv3.SwitchMultilevelSet cmd, ep = null) {
	log.debug "SwitchMultilevelSet: ${cmd}"
	dimmerEvents(cmd)
}

private dimmerEvents(physicalgraph.zwave.Command cmd, ep = null) {
	def value = (cmd.value ? "on" : "off")
	def result = [createEvent(name: "switch", value: value)]
	if (cmd.value && cmd.value <= 100) {
		result << createEvent(name: "level", value: cmd.value == 99 ? 100 : cmd.value)
	}
	return result
}

def zwaveEvent(physicalgraph.zwave.commands.sensormultilevelv5.SensorMultilevelReport cmd, ep = null) {
	log.info "SensorMultilevelReport: ${cmd}, endpoint: ${ep}"
	def map = [:]
	def result = []
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
	log.debug "SensorMultilevelReport, ${map}, ${map.name}, ${map.value}, ${map.unit}"
	if (childDevices) {
		sendEventToChild(map)
	}
	result << createEvent(map)
}

def sendEventToChild(event) {
	String childDni = "${device.deviceNetworkId}-2"
	def child = childDevices.find { it.deviceNetworkId == childDni }
	log.debug "Sending event: ${event} to child: ${child}"
	child?.sendEvent(event)
}

def zwaveEvent(physicalgraph.zwave.commands.switchmultilevelv3.SwitchMultilevelStartLevelChange cmd) {
	log.debug "SwitchMultilevelStartLevelChange: ${cmd}"
	//[createEvent(name: "switch", value: "on")]
	[:]
}

def zwaveEvent(physicalgraph.zwave.commands.switchmultilevelv3.SwitchMultilevelStopLevelChange cmd) {
	log.debug "SwitchMultilevelStopLevelChange: ${cmd}"
	//[response(zwave.switchMultilevelV3.switchMultilevelGet().format())]
	[:]
}

def zwaveEvent(physicalgraph.zwave.commands.manufacturerspecificv2.ManufacturerSpecificReport cmd) {
	log.debug "manufacturerId: $cmd.manufacturerId"
	log.debug "manufacturerName: $cmd.manufacturerName"
	log.debug "productId: $cmd.productId"
	log.debug "productTypeId: $cmd.productTypeId"
	def msr = String.format("%04X-%04X-%04X", cmd.manufacturerId, cmd.productTypeId, cmd.productId)
	updateDataValue("MSR", msr)
	updateDataValue("manufacturer", cmd.manufacturerName)
	createEvent([descriptionText: "$device.displayName MSR: $msr", isStateChange: false])
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
	// Handles all Z-Wave commands we aren't interested in
	log.debug "Command: ${cmd}"
	[:]
}

def on() {
	delayBetween([
			zwave.basicV1.basicSet(value: 0xFF).format(),
			zwave.switchMultilevelV3.switchMultilevelGet().format()
	], 5000)
}

def off() {
	delayBetween([
			zwave.basicV1.basicSet(value: 0x00).format(),
			zwave.switchMultilevelV3.switchMultilevelGet().format()
	], 5000)
}

def setLevel(value) {
	log.debug "setLevel >> value: $value"
	def valueaux = value as Integer
	def level = Math.max(Math.min(valueaux, 99), 0)
	delayBetween([zwave.basicV1.basicSet(value: level).format(), zwave.switchMultilevelV3.switchMultilevelGet().format()], 5000)
}

def setLevel(value, duration) {
	log.debug "setLevel >> value: $value, duration: $duration"
	def valueaux = value as Integer
	def level = Math.max(Math.min(valueaux, 99), 0)
	def dimmingDuration = duration < 128 ? duration : 128 + Math.round(duration / 60)
	def getStatusDelay = duration < 128 ? (duration * 1000) + 2000 : (Math.round(duration / 60) * 60 * 1000) + 2000
	delayBetween([zwave.switchMultilevelV3.switchMultilevelSet(value: level, dimmingDuration: dimmingDuration).format(),
				  zwave.switchMultilevelV3.switchMultilevelGet().format()], getStatusDelay)
}

/**
 * PING is used by Device-Watch in attempt to reach the Device
 * */
def ping() {
	refresh()
}

def refresh() {
	log.debug "refresh"
	def commands = []

	commands << zwave.basicV1.basicGet().format()
	commands << zwave.switchMultilevelV3.switchMultilevelGet().format()
	commands << zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType: 1, scale: 0).format()

	delayBetween(commands, 100)
}

private refreshChild() {
	sendHubCommand(zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType: 1, scale: 0).format())
}

private getParameterMap() {[
	[
		name: "Input 1 switch type", key: "input1SwitchType", type: "enum",
		parameterNumber: 1, size: 1, defaultValue: 0,
		values: [
			0: "Default value - Mono-stable switch type (push button) – button quick press turns between previous set dimmer value and zero)",
			1: "Bi-stable switch type ",
			2: "Potentiometer (Flush Dimmer 0-10V  is using set value the last received from potentiometer or from z-wave controller)",
			3: "0-10V Temperature sensor (regulated output)",
			4: "0-10V Illumination sensor (regulated output)",
			5: "0-10V General purpose sensor (regulated output)"
		],
		description: "Set input based on device type (switch, potentiometer, 0-10V sensor,..)." +
			"After parameter change to value 3, 4 or 5 first exclude module (without setting parameters to default value) then wait at least 30s and then re include the module! "
	],
	[
		name: "Auto or manual selection", key: "autoOrManualSelection", type: "boolean",
		parameterNumber: 52, size: 1, defaultValue: 0,
		optionInactive: 0, inactiveDescription: "Default value - Manual",
		optionActive: 1, activeDescription: "Auto",
		description: "This parameter is influencing on the software only when the value of parameter number 'Input 1 switch type' is set to value 3, 4 or 5. " +
			"In manual mode regulation (how the input influence on output) is disabled. "
	],
	[
		name: "Input I1 Sensor reporting", key: "inputI1SensorReporting", type: "range",
		parameterNumber: 140, size: 2, defaultValue: 5,
		range: "1..10000",
		description: "Input I1 Sensor reporting If analogue sensor is connected, module reports measured value on change defined by this parameter. " +
			"0 Reporting disabled, " +
			"5 (Default value) = 0,5 change, " +
			"1 - 10000 = 0,1 - 1000 step is 0,1"
	],
	[
		name: "Enable/Disable Double click function", key: "enable/DisableDoubleClickFunction", type: "boolean",
		parameterNumber: 21, size: 1, defaultValue: 0,
		optionInactive: 0, inactiveDescription: "Default value - Double click disabled",
		optionActive: 1, activeDescription: "Double click enabled",
		description: "If Double click function is enabled, a fast double click on the push button will set dimming power at maximum dimming value. " +
			"Valid only if input is set as mono-stable (push button)."
	],
	[
		name: "Unsecure / Secure Inclusion", key: "unsecure/SecureInclusion", type: "boolean",
		parameterNumber: 250, size: 1, defaultValue: 0,
		optionInactive: 0, inactiveDescription: "Default value - Unsecure Inclusion",
		optionActive: 1, activeDescription: "Secure Inclusion",
		description: "A Flush dimmer supports secure and unsecure inclusion." +
			"Even if the controller does not support security command classes, a dimmer could be included as unsecure and keep all the functionality."
	],
	[
		name: "Saving the state of the device after a power failure", key: "savingTheStateOfTheDeviceAfterAPowerFailure", type: "boolean",
		parameterNumber: 30, size: 1, defaultValue: 0,
		optionInactive: 0, inactiveDescription: "Default value - Flush Dimmer 0-10V module saves its state before power failure (it returns to the last position saved before a power failure)",
		optionActive: 1, activeDescription: " Flush Dimmer 0-10V module does not save the state after a power failure, it returns to &amp;amp;amp;amp;amp;amp;amp;quot;off&amp;amp;amp;amp;amp;amp;amp;quot; position",
		description: "Based on the parameter settings the stores/does not store the last value of the output after power failure. "
	],
	[
		name: "Minimum dimming value", key: "minimumDimmingValue", type: "range",
		parameterNumber: 60, size: 1, defaultValue: 1,
		range: "1..98",
		description: "1 (Default value) = 1% (minimum dimming value), " +
			"1 - 98 = 1% - 98%, step is 1%. Minimum dimming values is set by entered value. " +
			"When the switch type is selected as Bi-stable, it is not possible to dim the value between min and max. " +
			"If Switch_multilevel_set is set to the value '0', the output is turned OFF. " +
			"If Switch_multilevel_set is set to the value '1', the output is set to the minimum diming value. " +
			"NOTE: The minimum level may not be higher than the maximum level."
	],
	[
		name: "Maximum dimming value", key: "maximumDimmingValue", type: "range",
		parameterNumber: 61, size: 1, defaultValue: 99,
		range: "99",
		description: "99 (Default value) = 99% (Maximum dimming value)" +
			"2 - 99 = 2% - 99%, step is 1%. Maximum dimming values is set by entered value. " +
			"When the switch type is selected as Bi-stable, it is not possible to dim the value between min and max. " +
			"NOTE: The maximum level may not be lower than the minimum level."
	],
	[
		name: "Dimming time (soft on/off)", key: "dimmingTime(SoftOn/Off)", type: "range",
		parameterNumber: 65, size: 2, defaultValue: 100,
		range: "50..255",
		description: "Set value means time of moving the Flush Dimmer 0-10V between min. and max. dimming values by short press of push button I1 or controlled through UI (BasicSet). " +
			"100 (Default value) = 1s, " +
			"50 - 255 = 500 - 2550 milliseconds (2,55s), step is 10 milliseconds"
	],
	[
		name: "Dimming time when key pressed", key: "dimmingTimeWhenKeyPressed", type: "range",
		parameterNumber: 66, size: 2, defaultValue: 3,
		range: "1..255",
		description: "Time of moving the Flush Dimmer 0-10V between min. and max dimming values by continues hold of push button I1 or associated device. " +
			"3 seconds (Default value), " +
			"1 - 255 seconds"
	],
	[
		name: "Dimming duration", key: "dimmingDuration", type: "range",
		parameterNumber: 68, size: 1, defaultValue: 0,
		range: "0..127",
		description: "This parameter is used with association group 3. The Duration field MUST specify the time that the transition should take from the current value to the new target value. " +
			"A supporting device SHOULD respect the specified Duration value. " +
			"0 (Default value) - dimming duration according to parameter: 'Dimming time when key pressed'," +
			"1 to 127 seconds"
	]
]}