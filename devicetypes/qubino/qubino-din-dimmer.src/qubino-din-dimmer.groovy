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
	definition(name: "Qubino DIN Dimmer", namespace: "qubino", author: "SmartThings", mnmn: "SmartThings", vid:"generic-dimmer-power-energy", ocfDeviceType: "oic.d.switch", runLocally: false, executeCommandsLocally: false) {
		capability "Actuator"
		capability "Configuration"
		capability "Energy Meter"
		capability "Health Check"
		capability "Power Meter"
		capability "Refresh"
		capability "Sensor"
		capability "Switch"
		capability "Switch Level"

		// Raw Description: zw:Ls type:1101 mfr:0159 prod:0001 model:0052 ver:3.01 zwv:4.24 lib:03 cc:5E,5A,73,98 sec:86,72,27,25,26,32,71,85,8E,59,70 secOut:26 role:05 ff:9C00 ui:9C00
		fingerprint mfr: "0159", prod: "0001", model: "0052", deviceJoinName: "Qubino Dimmer"
	}

	tiles(scale: 2) {
		multiAttributeTile(name: "switch", type: "lighting", width: 6, height: 4, canChangeIcon: true) {
			tileAttribute("device.switch", key: "PRIMARY_CONTROL") {
				attributeState "on", label: '${name}', action: "switch.off", icon: "st.switches.light.on", backgroundColor: "#00a0dc", nextState: "turningOff"
				attributeState "off", label: '${name}', action: "switch.on", icon: "st.switches.light.off", backgroundColor: "#ffffff", nextState: "turningOn"
				attributeState "turningOn", label: '${name}', action: "switch.off", icon: "st.switches.light.on", backgroundColor: "#79b821", nextState: "turningOff"
				attributeState "turningOff", label: '${name}', action: "switch.on", icon: "st.switches.light.off", backgroundColor: "#ffffff", nextState: "turningOn"
			}
			tileAttribute("device.level", key: "SLIDER_CONTROL") {
				attributeState "level", action: "switch level.setLevel"
			}
		}
		valueTile("power", "device.power", width: 2, height: 2) {
			state "default", label: '${currentValue} W'
		}
		valueTile("energy", "device.energy", width: 2, height: 2) {
			state "default", label: '${currentValue} kWh'
		}
		standardTile("refresh", "device.power", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "default", label: '', action: "refresh.refresh", icon: "st.secondary.refresh"
		}

		main(["switch"])
		details(["switch", "power", "energy", "refresh"])
	}

	preferences {
		// Preferences template begin
		parameterMap.each {
			input(
				title: it.name,
				description: it.description,
				type: "paragraph",
				element: "paragraph"
			)

			switch (it.type) {
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
	sendEvent(name: "checkInterval", value: 2 * 15 * 60 + 2 * 60, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])
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
	// Device-Watch simply pings if no device events received for 32min(checkInterval)
	sendEvent(name: "checkInterval", value: 2 * 15 * 60 + 2 * 60, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])
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
		0x5E: 1,
		0x5A: 1, //Device Reset Locally
		0x73: 1, //Powerlevel
		0x98: 1, //Security
		0x86: 1, //Version
		0x72: 2, //Manufacturer Specific
		0x27: 1, //Switch All
		0x25: 1, //Switch Binary
		0x26: 3, //Switch Multilevel
		0x32: 3, //Meter
		0x71: 3, //Notification
		0x85: 2, //Association
		0x8E: 2, //Multi Channel Association
		0x59: 1, //Association Grp Info
		0x70: 2, //Configuration
	]
}

def configure() {
	def configurationCommands = [
		encap(zwave.associationV1.associationSet(groupingIdentifier: 1, nodeId: [zwaveHubNodeId])),
		encap(zwave.associationV1.associationSet(groupingIdentifier: 2, nodeId: [zwaveHubNodeId])),
		encap(zwave.associationV1.associationSet(groupingIdentifier: 3, nodeId: [zwaveHubNodeId])),
		encap(zwave.associationV1.associationSet(groupingIdentifier: 4, nodeId: [zwaveHubNodeId])),
		encap(zwave.associationV1.associationSet(groupingIdentifier: 5, nodeId: [zwaveHubNodeId])),
		encap(zwave.multiChannelV3.multiChannelEndPointGet()),
		encap(zwave.meterV2.meterReset())
	]
	delayBetween(configurationCommands)
}

def parse(String description) {
	log.debug "Parsing '${description}'"
	def result = null
	def cmd = zwave.parse(description)
	if (cmd) {
		result = zwaveEvent(cmd)
		log.debug "Parsed ${cmd} to ${result.inspect()}, ${result}"
	} else {
		log.debug "Non-parsed event: ${description}"
	}
	return result
}

def zwaveEvent(physicalgraph.zwave.commands.configurationv2.ConfigurationReport cmd) {
	// Preferences template begin
	log.debug "Configuration report: ${cmd}"
	def preference = parameterMap.find({ it.parameterNumber == cmd.parameterNumber })
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
			return settings."$parameterKey" == "true" ? preference.optionActive : preference.optionInactive
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
			return state.currentPreferencesState?."$preference.key".value != settings."$preference.key"
		}
	} else {
		return false
	}
}

def zwaveEvent(physicalgraph.zwave.commands.multichannelv3.MultiChannelEndPointReport cmd, ep = null) {
	log.debug "MultiChannelEndPointReport: ${cmd}"
	if (!childDevices) {
		createChildDevice()
	}
	response([refresh()])
}

def createChildDevice() {
	try {
		String childDni = "${device.deviceNetworkId}:2"
		String componentLabel = "Qubino Temperature Sensor"
		def properties = [
			completedSetup: true,
			label         : componentLabel,
			isComponent   : false
		]
		addChildDevice(
			"Child Temperature Sensor",
			childDni,
			device.hub.id,
			properties
		)
	} catch (Exception e) {
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
	def value = (cmd.value ? "on" : "off")
	createEvent(name: "switch", value: value)
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicSet cmd) {
	log.debug "BasicSet: ${cmd}"
	[]
}

def zwaveEvent(physicalgraph.zwave.commands.switchmultilevelv3.SwitchMultilevelReport cmd, ep = null) {
	log.debug "SwitchMultilevelReport: ${cmd}"
	dimmerEvents(cmd)
}

def zwaveEvent(physicalgraph.zwave.commands.switchmultilevelv3.SwitchMultilevelSet cmd) {
	log.debug "SwitchMultilevelSet: ${cmd}"
	[]
}

def dimmerEvents(physicalgraph.zwave.Command cmd, ep = null) {
	log.debug "dimmerEvents() value: ${cmd.value}"
	def value = (cmd.value ? "on" : "off")
	return [
		createEvent(name: "switch", value: value),
		createEvent(name: "level", value: cmd.value == 99 ? 100 : cmd.value)
	]
}

def zwaveEvent(physicalgraph.zwave.commands.switchbinaryv1.SwitchBinaryReport cmd, ep = null) {
	log.debug "SwitchBinaryReport: ${cmd}"
	def value = (cmd.value ? "on" : "off")
	createEvent(name: "switch", value: value)
}

def zwaveEvent(physicalgraph.zwave.commands.meterv3.MeterReport cmd, ep = null) {
	log.debug "MeterReport: ${cmd}"
	handleMeterReport(cmd)
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

def zwaveEvent(physicalgraph.zwave.commands.sensormultilevelv5.SensorMultilevelReport cmd, ep = null) {
	log.debug "SensorMultilevelReport: ${cmd}, endpoint: ${ep}"
	def params = createSensorMultilevelReportParameters(cmd)
	log.debug "SensorMultilevelReport, ${params}, ${params.name}, ${params.value}, ${params.unit}"
	if (childDevices) {
		sendEventToChild(params)
	}
	createEvent(params)
}

def createSensorMultilevelReportParameters(physicalgraph.zwave.commands.sensormultilevelv5.SensorMultilevelReport cmd) {
	if (cmd.sensorType == 1) {
		def cmdScale = cmd.scale == 1 ? "F" : "C"
		[name : "temperature",
		 value: convertTemperatureIfNeeded(cmd.scaledSensorValue, cmdScale, cmd.precision),
		 unit : getTemperatureScale()]
	} else {
		[descriptionText: cmd.toString()]
	}
}

def sendEventToChild(event) {
	String childDni = "${device.deviceNetworkId}:2"
	def child = childDevices.find { it.deviceNetworkId == childDni }
	log.debug "Sending event: ${event} to child: ${child}"
	child?.sendEvent(event)
}

def zwaveEvent(physicalgraph.zwave.commands.switchmultilevelv3.SwitchMultilevelStartLevelChange cmd, ep = null) {
	log.debug "SwitchMultilevelStartLevelChange: ${cmd}"
	[]
}

def zwaveEvent(physicalgraph.zwave.commands.switchmultilevelv3.SwitchMultilevelStopLevelChange cmd, ep = null) {
	log.debug "SwitchMultilevelStopLevelChange: ${cmd}"
	[]
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
	log.debug "Unhandled: $cmd"
	[]
}

def on() {
	encapSequence([
		zwave.switchMultilevelV3.switchMultilevelSet(value: 0xFF, dimmingDuration: 0x00),
		zwave.switchMultilevelV1.switchMultilevelGet(),
		zwave.meterV2.meterGet(scale: 2),
		zwave.meterV2.meterGet(scale: 0)
	], 3000)
}

def off() {
	encapSequence([
		zwave.switchMultilevelV3.switchMultilevelSet(value: 0x00, dimmingDuration: 0x00),
		zwave.switchMultilevelV1.switchMultilevelGet(),
		zwave.meterV2.meterGet(scale: 2),
		zwave.meterV2.meterGet(scale: 0)
	], 3000)
}

def setLevel(value) {
	log.debug "setLevel() + $value"
	def valueaux = value as Integer
	def level = Math.max(Math.min(valueaux, 99), 0)
	encapSequence([
		zwave.switchMultilevelV3.switchMultilevelSet(value: level, dimmingDuration: 0x00),
		zwave.switchMultilevelV1.switchMultilevelGet()
	])
}

/**
 * PING is used by Device-Watch in attempt to reach the Device
 * */
def ping() {
	log.debug "ping()"
	refresh()
}

def refresh() {
	log.debug "refresh()"
	encapSequence([
		zwave.switchMultilevelV1.switchMultilevelGet(),
		zwave.meterV2.meterGet(scale: 2),
		zwave.meterV2.meterGet(scale: 0)
	])
}

private refreshChild() {
	sendHubCommand(zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType: 1, scale: 0).format())
}

/*
 * Security encapsulation support:
 */

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

private encapSequence(cmds, Integer delay = 250) {
	delayBetween(cmds.collect { encap(it) }, delay)
}

private getParameterMap() {
	[
		[
			name               : "Input switch type",
			key                : "inputSwitchType",
			type               : "boolean",
			parameterNumber    : 1,
			size               : 1,
			defaultValue       : 0,
			optionInactive     : 0,
			inactiveDescription: "Mono-stable switch type (push button)",
			optionActive       : 1,
			activeDescription  : "Bi-stable switch type (toggle switch)",
			description        : "Type of switch type: mono-stable switch type (push button) or bi-stable switch type (toggle switch)"
		],
		[
			name           : "Activate / deactivate functions ALL ON / ALL OFF ",
			key            : "activate/DeactivateFunctionsAllOn/AllOff",
			type           : "enum",
			parameterNumber: 10, size: 2, defaultValue: 255,
			values         : [
				255: "ALL ON active, ALL OFF active",
				0  : "ALL ON is not active, ALL OFF is not active",
				1  : "ALL ON is not active, ALL OFF active",
				2  : "ALL ON active, ALL OFF is not active"
			],
			description    : "DIN Dimmer module responds to commands ALL ON / ALL OFF that may be sent by the main controller or by other controller belonging to the system. "
		],
		[
			name           : "Automatic turning off output after set time ",
			key            : "automaticTurningOffOutputAfterSetTime", type: "boolRange",
			parameterNumber: 11,
			size           : 2,
			defaultValue   : 0,
			range          : "1..32536",
			disableValue   : 0,
			description    : "Turns off the output after set time."
		],
		[
			name           : "Temperature sensor offset settings",
			key            : "temperatureSensorOffsetSettings",
			type           : "boolRange",
			parameterNumber: 110,
			size           : 2,
			defaultValue   : 32536,
			range          : "1..1100",
			disableValue   : 32536,
			description    : "Set value is added or subtracted to actual measured value by sensor. Available configuration parameters: 32536 = offset is 0.0°C,	1 - 100 = value from 0.1°C to 10.0°C is added to actual measured temperature. 1001 - 1100 = value from -0.1°C to -10.0°C is subtracted to actual measured temperature."
		],
		[
			name           : "Automatic turning on output after set time ",
			key            : "automaticTurningOnOutputAfterSetTime",
			type           : "boolRange",
			parameterNumber: 12,
			size           : 2,
			defaultValue   : 0,
			range          : "1..32535",
			disableValue   : 0,
			description    : "Turns on the output after set time."
		],
		[
			name           : "Digital temperature sensor reporting",
			key            : "digitalTemperatureSensorReporting",
			type           : "range",
			parameterNumber: 120,
			size           : 1,
			defaultValue   : 5,
			range          : "0..127",
			description    : "Digital temperature sensor reporting If digital temperature sensor is connected, module reports measured temperature on temperature change defined by this parameter. Available configuration parameters: default value 5 = 0,5°C. 1- 127 = 0,1°C - 12,7°C, step is 0,1°C change.	0 = reporting disabled"
		],
		[
			name               : "Enable/Disable Double click function",
			key                : "enable/DisableDoubleClickFunction",
			type               : "boolean",
			parameterNumber    : 21,
			size               : 1,
			defaultValue       : 0,
			optionInactive     : 0,
			inactiveDescription: "Double click disabled",
			optionActive       : 1,
			activeDescription  : "Double click enabled",
			description        : "If Double click function is enabled, a fast double click on the push button will set dimming power at maximum dimming value."
		],
		[
			name               : "Saving the state of the device after a power failure",
			key                : "savingTheStateOfTheDeviceAfterAPowerFailure",
			type               : "boolean",
			parameterNumber    : 30,
			size               : 1,
			defaultValue       : 0,
			optionInactive     : 0,
			inactiveDescription: "DIN Dimmer module saves its state before power failure (it returns to the last position saved before a power failure)",
			optionActive       : 1,
			activeDescription  : "DIN Dimmer module does not save the state after a power failure, it returns to off position",
			description        : "Based on the parameter settings the stores/does not store the last value of the output after power failure."
		],
		[
			name           : "Power reporting in Watts on power change",
			key            : "powerReportingInWattsOnPowerChange",
			type           : "boolRange",
			parameterNumber: 40,
			size           : 1,
			defaultValue   : 5,
			range          : "1..100",
			disableValue   : 0,
			description    : "Set value means percentage, set value from 0 - 100=0% - 100%.  Power report is send (push) only when actual power in Watts in real time changes for more than set percentage comparing to previous actual power in Watts, step is 1%. NOTE: if power changed is less than 1W, the report is not send (pushed), independent of percentage set."
		],
		[
			name           : " Power reporting in Watts by time interval",
			key            : "powerReportingInWattsByTimeInterval",
			type           : "boolRange",
			parameterNumber: 42,
			size           : 2,
			defaultValue   : 0,
			range          : "1..32767",
			disableValue   : 0,
			description    : "Time interval (0 - 32767) in seconds, when power report is send. Reporting enabled. Power report is send with time interval set by entered value. Please note, that too fast reporting can cause too much Z-Wave traffic resulting in Z-Wave poor response."
		],
		[
			name           : "Minimum dimming value",
			key            : "minimumDimmingValue",
			type           : "range",
			parameterNumber: 60,
			size           : 1,
			defaultValue   : 1,
			range          : "1..98",
			description    : "The minimum level may not be higher than the maximum level! 1% min. When the switch type is selected as Bi-stable, it is not possible to dim the value between min and max."
		],
		[
			name           : "Maximum dimming value",
			key            : "maximumDimmingValue",
			type           : "range",
			parameterNumber: 61,
			size           : 1,
			defaultValue   : 99,
			range          : "2..99",
			description    : "The maximum level may not be lower than the minimum level! When the switch type is selected as Bi-stable, it is not possible to dim the value between min and max."
		],
		[
			name           : "Dimming time (soft on/off)",
			key            : "dimmingTime(SoftOn/Off)",
			type           : "range",
			parameterNumber: 65,
			size           : 2,
			defaultValue   : 100,
			range          : "50..255",
			description    : "Set value means time of moving the DIN Dimmer between min. and max. dimming values by short press of push button."
		],
		[
			name           : "Dimming time when key pressed",
			key            : "dimmingTimeWhenKeyPressed",
			type           : "range",
			parameterNumber: 66,
			size           : 2,
			defaultValue   : 3,
			range          : "1..255",
			description    : "Time of moving the DIN Dimmer between min. and max dimming values by continues hold of push button or associated device."
		],
		[
			name               : "Ignore start level",
			key                : "ignoreStartLevel",
			type               : "boolean",
			parameterNumber    : 67,
			size               : 1,
			defaultValue       : 0,
			optionInactive     : 0,
			inactiveDescription: "Respect start level",
			optionActive       : 1,
			activeDescription  : "Ignore start level",
			description        : "Choose whether the device should use (or disregard) the start dimming level value. If the device is configured to use the start level, it should start the dimming process from the currently set dimming level. This parameter is used with association group 3."
		],
		[
			name           : "Dimming duration",
			key            : "dimmingDuration",
			type           : "range",
			parameterNumber: 68,
			size           : 1,
			defaultValue   : 0,
			range          : "1..127",
			description    : "Choose the time during which the device will transition from the current value to the new target value. This parameter applies to the association group 3."
		]
	]
}

