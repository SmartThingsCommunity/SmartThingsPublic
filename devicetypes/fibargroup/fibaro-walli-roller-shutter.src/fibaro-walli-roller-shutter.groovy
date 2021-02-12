/**
 *	Copyright 2020 SmartThings
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

import java.lang.Math

metadata {
	definition (name: "Fibaro Walli Roller Shutter", namespace: "fibargroup", author: "SmartThings", ocfDeviceType: "oic.d.blind", mnmn: "SmartThings", vid: "SmartThings-smartthings-Fibaro_Roller_Shutter") {
		capability "Window Shade"
		capability "Window Shade Level"
		capability "Power Meter"
		capability "Energy Meter"
		capability "Refresh"
		capability "Health Check"
		capability "Configuration"

		capability "Switch Level"

		fingerprint mfr: "010F", prod: "1D01", model: "1000", deviceJoinName: "Fibaro Window Treatment"
	}

	tiles(scale: 2) {
		multiAttributeTile(name:"windowShade", type: "generic", width: 6, height: 4) {
			tileAttribute("device.windowShade", key: "PRIMARY_CONTROL") {
				attributeState "open", label: 'Open', action: "close", icon: "http://www.ezex.co.kr/img/st/window_open.png", backgroundColor: "#00A0DC", nextState: "closing"
				attributeState "closed", label: 'Closed', action: "open", icon: "http://www.ezex.co.kr/img/st/window_close.png", backgroundColor: "#ffffff", nextState: "opening"
				attributeState "partially open", label: 'Partially open', action: "close", icon: "http://www.ezex.co.kr/img/st/window_open.png", backgroundColor: "#d45614", nextState: "closing"
				attributeState "opening", label: 'Opening', action: "pause", icon: "http://www.ezex.co.kr/img/st/window_open.png", backgroundColor: "#00A0DC", nextState: "partially open"
				attributeState "closing", label: 'Closing', action: "pause", icon: "http://www.ezex.co.kr/img/st/window_close.png", backgroundColor: "#ffffff", nextState: "partially open"
			}
		}
		standardTile("contPause", "device.switch", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "pause", label:"", icon:'st.sonos.pause-btn', action:'pause', backgroundColor:"#cccccc"
		}
		standardTile("refresh", "device.refresh", inactiveLabel: false, decoration: "flat", width: 2, height: 1) {
			state "default", label:"", action:"refresh.refresh", icon:"st.secondary.refresh"
		}
		valueTile("shadeLevel", "device.level", width: 4, height: 1) {
			state "level", label: 'Shade is ${currentValue}% up', defaultState: true
		}
		controlTile("levelSliderControl", "device.level", "slider", width:2, height: 1, inactiveLabel: false) {
			state "level", action:"switch level.setLevel"
		}

		main "windowShade"
		details(["windowShade", "contPause", "shadeLevel", "levelSliderControl", "refresh"])
	}

	preferences {
		// Preferences template begin
		parameterMap.each {
			input (title: it.name, description: it.description, type: "paragraph", element: "paragraph")

			switch(it.type) {
				case "boolRange":
					input(
							name: it.key + "Boolean", type: "bool", title: "Enable",
							description: "If you disable this option, it will overwrite setting below.",
							defaultValue: it.defaultValue != it.disableValue, required: false
					)
					input(
							name: it.key, type: "number", title: "Set value (range ${it.range})",
							defaultValue: it.defaultValue, range: it.range, required: false
					)
					break
				case "boolean":
					input(
							type: "paragraph", element: "paragraph",
							description: "Option enabled: ${it.activeDescription}\n Option disabled: ${it.inactiveDescription}"
					)
					input(
							name: it.key, type: "boolean",
							title: "Enable", defaultValue: it.defaultValue == it.activeOption, required: false
					)
					break
				case "enum":
					input(
							name: it.key, title: "Select", type: "enum",
							options: it.values, defaultValue: it.defaultValue, required: false
					)
					break
				case "range":
					input(
							name: it.key, type: "number", title: "Set value (range ${it.range})",
							defaultValue: it.defaultValue, range: it.range, required: false
					)
					break
			}
		}
		// Preferences template end
	}
}

def installed() {
	state.calibrationStatus = "notStarted"
	sendEvent(name: "checkInterval", value: 2 * 60 * 60, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])
	// Preferences template begin
	state.currentPreferencesState = [:]
	parameterMap.each {
		state.currentPreferencesState."$it.key" = [:]
		state.currentPreferencesState."$it.key".value = getPreferenceValue(it)
		if (it.type == "boolRange" && getPreferenceValue(it) == it.disableValue) {
			state.currentPreferencesState."$it.key".status = "disablePending"
		} else {
			def preferenceName = it.key + "Boolean"
			settings."$preferenceName" = true
			state.currentPreferencesState."$it.key".status = "synced"
		}
	}
	// Preferences template end
	sendEvent(name: "supportedWindowShadeCommands", value: ["open", "close", "pause"])
}

def updated() {
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
				statusOverrideIfNeeded(it.key)
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

def zwaveEvent(physicalgraph.zwave.commands.configurationv2.ConfigurationReport cmd) {
	// Preferences template begin
	log.debug "Configuration report: ${cmd}"
	def preference = parameterMap.find( {it.parameterNumber == cmd.parameterNumber} )
	def key = preference.key
	def preferenceValue = getPreferenceValue(preference, cmd.scaledConfigurationValue)
	if (settings."$key" == preferenceValue) {
		state.currentPreferencesState."$key".value = settings."$key"
		state.currentPreferencesState."$key".status = "synced"
		handleConfigurationChange(cmd)
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
    handleConfigurationChange(cmd)
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
		def value = state.currentPreferencesState."$preference.key"
		switch (preference.type) {
			case "boolRange":
				def boolName = preference.key + "Boolean"
				if (state.currentPreferencesState."$preference.key".status == "disabled") {
					return settings."$boolName"
				} else {
					return state.currentPreferencesState."$preference.key".value != settings."$preference.key" || !settings."$boolName"
				}
			default:
				return state.currentPreferencesState."$preference.key".value != settings."$preference.key"
		}
	} else {
		return false
	}
}

private notNullCheck(value) {
	return value != null
}

private statusOverrideIfNeeded(preferenceKey) {
	switch (preferenceKey) {
		case "forceCalibration":
			if (state.calibrationStatus == "done") {
				state.currentPreferencesState."$preferenceKey".status = "synced"
			}
		break
	}
}

def handleConfigurationChange(confgurationReport) {
	switch (confgurationReport.parameterNumber) {
		case 150: // Calibrating
			switch(confgurationReport.scaledConfigurationValue) {
				case 0: // "Device is not calibrated"
					state.calibrationStatus = "notStarted"
					break
				case 1: // "Device is calibrated"
                    state.calibrationStatus = "done"
                    state.currentPreferencesState.forceCalibration.status = "synced"
                    break
                case 2: // "Force Calibration"
                    state.calibrationStatus = state.calibrationStatus == "notStarted" ? "pending" : state.calibrationStatus
                    break
			}
			log.info "Calibration ${state.calibrationStatus}"
			break
		case 151: //Operating mode
			switch(confgurationReport.scaledConfigurationValue) {
				case 1: // "Roller blind (with positioning)"
					log.info "Device is already configured as Roller Blind"
					break
				case 2: // "Venetian blind (with positioning)"
					log.info "Changing device type to Fibaro Walli Roller Shutter Venetian"
					setDeviceType("Fibaro Walli Roller Shutter Venetian")
					break
				case 5: // "Roller blind with built-in driver"
				case 6: // "Roller blind with built-in driver (impulse)"
					log.info "Changing device type to Fibaro Walli Roller Shutter Driver"
					setDeviceType("Fibaro Walli Roller Shutter Driver")
					break
			}
			break
		default:
			log.info "Parameter no. ${confgurationReport.parameterNumber} has no specific handler"
			break
	}
}

def parse(String description) {
	def result = null
	def cmd = zwave.parse(description)
	if (cmd) {
		result = zwaveEvent(cmd)
	} else {
		log.warn "${device.displayName} - no-parsed event: ${description}"
	}
	log.debug "Parse returned: ${result}"
	return result
}

def close() {
	setShadeLevel(0x64)
}

def open() {
	setShadeLevel(0x00)
}

def pause() {
	encap(zwave.switchMultilevelV3.switchMultilevelStopLevelChange())
}

def setLevel(level) {
	setShadeLevel(level)
}

def setShadeLevel(level) {
	log.debug "Setting shade level: ${level}"
	state.isManualCommand = false
	def currentLevel = Integer.parseInt(device.currentState("shadeLevel").value)
	state.blindsLastCommand = currentLevel > level ? "opening" : "closing"
	state.shadeTarget = level
	encap(zwave.switchMultilevelV3.switchMultilevelSet(value: Math.min(0x63, level)), 1)
}

def refresh() {
	sendHubCommand([
			encap(zwave.switchMultilevelV3.switchMultilevelGet())
	])
}

def ping() {
	refresh()
}

def configure() {
	def configurationCommands = []
	configurationCommands += encap(zwave.associationV1.associationSet(groupingIdentifier: 2, nodeId: [zwaveHubNodeId]))
	configurationCommands += encap(zwave.associationV1.associationSet(groupingIdentifier: 3, nodeId: [zwaveHubNodeId]))

	delayBetween(configurationCommands)
}

def zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityMessageEncapsulation cmd) {
	def encapsulatedCommand = cmd.encapsulatedCommand()
	if (encapsulatedCommand) {
		zwaveEvent(encapsulatedCommand)
	} else {
		log.warn "unable to extract secure command from $cmd"
		createEvent(descriptionText: cmd.toString())
	}
}

def zwaveEvent(physicalgraph.zwave.commands.multichannelv3.MultiChannelCmdEncap cmd, ep = null) {
	def encapsulatedCommand = cmd.encapsulatedCommand()
	zwaveEvent(encapsulatedCommand, cmd.sourceEndPoint as Integer)
}

def zwaveEvent(physicalgraph.zwave.commands.switchmultilevelv3.SwitchMultilevelReport  cmd, ep = null) {
	if (cmd.value != 0xFE && ep != 2) {
		shadeEvent(cmd.value)
	} else {
		log.warn "Something went wrong with calibration, position of blind is unknown"
	}
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd, ep = null) {
	if (ep != 2) {
		shadeEvent(cmd.value)
	}
}

private shadeEvent(value) {
	def shadeValue
	if (!value) {
		shadeValue = "open"
	} else if (value == 0x63) {
		shadeValue = "closed"
	} else {
		shadeValue = "partially open"
	} 
	[
		createEvent(name: "windowShade", value: shadeValue, isStateChange: true, descriptionText: "Window blinds is ${shadeValue}"),
		createEvent(name: "level", value: value != 0x63 ? value : 100),
		createEvent(name: "shadeLevel", value: value != 0x63 ? value : 100)
	]
}

def zwaveEvent(physicalgraph.zwave.commands.meterv3.MeterReport cmd, ep = null) {
	def toReturn = []
	def eventMap = [:]
	def additionalShadeEvent = [:]
	if (cmd.meterType == 0x01) {
		if (cmd.scale == 0x00) {
			eventMap.name = "energy"
			eventMap.value = cmd.scaledMeterValue
			eventMap.unit = "kWh"
			toReturn += createEvent(eventMap)
		} else if (cmd.scale == 0x02) {
			eventMap.name = "power"
			eventMap.value = Math.round(cmd.scaledMeterValue)
			eventMap.unit = "W"
			toReturn += createEvent(eventMap)
			if (cmd.scaledMeterValue) {
				additionalShadeEvent.name = "windowShade"
				additionalShadeEvent.value = state.blindsLastCommand
				toReturn += createEvent(additionalShadeEvent)
				if (!state.isManualCommand) {
					sendEvent(name: "level", value: state.shadeTarget)
					sendEvent(name: "shadeLevel", value: state.shadeTarget)
				}
			} else {
				toReturn += response(encap(zwave.switchMultilevelV3.switchMultilevelGet(), 1))
			}
		}
	}
	toReturn
}

def zwaveEvent(physicalgraph.zwave.commands.switchmultilevelv3.SwitchMultilevelStartLevelChange cmd) {
	state.isManualCommand = true
	state.blindsLastCommand = cmd.upDown ? "opening" : "closing"
}

def zwaveEvent(physicalgraph.zwave.Command cmd, ep = null) {
	log.warn "Unhandled ${cmd}" + (ep ? " from endpoint $ep" : "")
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

private getParameterMap() {[
		[
				name: "LED frame - colour when moving", key: "ledFrame-ColourWhenMoving", type: "enum",
				parameterNumber: 11, size: 1, defaultValue: 1,
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
				description: "This setting defines the LED colour when the motor is running."
		],
		[
				name: "LED frame - colour when not moving", key: "ledFrame-ColourWhenNotMoving", type: "enum",
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
				description: "This setting defines the LED colour when the motor isn't running."
		],
		[
				name: "LED frame - brightness", key: "ledFrame-Brightness", type: "boolRange",
				parameterNumber: 13, size: 1, defaultValue: 100,
				range: "1..100", disableValue: 0,
				description: "This setting allows to adjust the LED frame brightness."
		],
		[
				name: "Force calibration", key: "forceCalibration", type: "boolean",
				parameterNumber: 150, size: 1, defaultValue: 0,
				optionInactive: 0, inactiveDescription: "Blinds are not calibrated.",
				optionActive: 2, activeDescription: "Blinds calibration process starts.",
				description: "This setting allows triggering blinds calibration process."
		],
		[
				name: "Operating mode", key: "operatingMode", type: "enum",
				parameterNumber: 151, size: 1, defaultValue: 1,
				values: [
						1: "Roller blind (with positioning)",
						2: "Venetian blind (with positioning)",
						5: "Roller blind with built-in driver",
						6: "Roller blind with built-in driver (impulse)"
				],
				description: "This setting allows adjusting operation according to the connected device."
		],
		[
				name: "Delay motor stop after reaching end switch", key: "delayMotorStopAfterReachingEndSwitch", type: "range",
				parameterNumber: 154, size: 2, defaultValue: 10,
				range: "1..255",
				description: "The setting determines the time after which the motor will be stopped after end switch contacts are closed."
		],
		[
				name: "Motor operation detection", key: "motorOperationDetection", type: "range",
				parameterNumber: 155, size: 2, defaultValue: 10,
				range: "1..255",
				description: "Power threshold interpreted as reaching a limit switch."
		],
		[
				name: "Buttons orientation", key: "buttonsOrientation", type: "boolean",
				parameterNumber: 24, size: 1, defaultValue: 0,
				optionInactive: 0, inactiveDescription: "default (1st button UP, 2nd button DOWN)",
				optionActive: 1, activeDescription: "reversed (1st button DOWN, 2nd button UP)",
				description: "This setting allows reversing the operation of the buttons."
		],
		[
				name: "Outputs orientation", key: "outputsOrientation", type: "boolean",
				parameterNumber: 25, size: 1, defaultValue: 0,
				optionInactive: 0, inactiveDescription: "(Q1 - UP, Q2 - DOWN)LED disabled",
				optionActive: 1, activeDescription: "reversed (Q1 - DOWN, Q2 - UP)",
				description: "This setting allows reversing the operation of Q1 and Q2 without changing the wiring (e.g. in case of invalid motor connection)."
		],
		[
				name: "Power reports - include self-consumption", key: "powerReports-IncludeSelf-Consumption", type: "boolean",
				parameterNumber: 60, size: 1, defaultValue: 0,
				optionInactive: 0, inactiveDescription: "Self-consumption not included",
				optionActive: 1, activeDescription: "Self-consumption included",
				description: "This setting determines whether the power measurements should include power consumed by the device itself."
		],
		[
				name: "Power reports - on change", key: "powerReports-OnChange", type: "boolRange",
				parameterNumber: 61, size: 2, defaultValue: 15,
				range: "1..500", disableValue: 0,
				description: "This setting defines minimal change (from the last reported) in measured power that results in sending new report. For loads under 50W the setting is irrelevant, report are sent every 5W change."
		],
		[
				name: "Power reports - periodic", key: "powerReports-Periodic", type: "boolRange",
				parameterNumber: 62, size: 2, defaultValue: 3600,
				range: "30..32400", disableValue: 0,
				description: "This setting defines reporting interval for measured power."
		],
		[
				name: "Energy reports - on change", key: "energyReports-OnChange", type: "boolRange",
				parameterNumber: 65, size: 2, defaultValue: 10,
				range: "1..500", disableValue: 0,
				description: "This setting defines minimal change (from the last reported) in measured energy that results in sending new report."
		],
		[
				name: "Energy reports - periodic", key: "energyReports-Periodic", type: "boolRange",
				parameterNumber: 66, size: 2, defaultValue: 3600,
				range: "30..32400", disableValue: 0,
				description: "This setting defines reporting interval for measured energy."
		]
]}