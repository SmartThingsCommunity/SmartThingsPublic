/**
 * 	Copyright 2020 SmartThings
 *
 *  Fibaro Walli Dimmer Switch
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
metadata {
	definition (name: "Fibaro Walli Dimmer Switch", namespace: "fibargroup", author: "SmartThings", ocfDeviceType: "oic.d.switch", runLocally: false, minHubCoreVersion: '000.017.0012', executeCommandsLocally: false, genericHandler: "Z-Wave") {

		capability "Actuator"
		capability "Configuration"
		capability "Energy Meter"
		capability "Health Check"
		capability "Power Meter"
		capability "Refresh"
		capability "Sensor"
		capability "Switch"
		capability "Switch Level"

		command "reset"

		fingerprint mfr: "010F", prod: "1C01", model: "1000", deviceJoinName: "Dimmer" // raw description: zw:Ls type:1101 mfr:010F prod:1C01 model:1000 ver:5.01 zwv:6.02 lib:03 cc:5E,55,98,56,6C,22 sec:26,85,8E,59,86,72,5A,73,32,70,71,75,5B,7A role:05 ff:9C00 ui:9C00
	}

	tiles(scale: 2) {
		multiAttributeTile(name:"switch", type: "lighting", width: 6, height: 4, canChangeIcon: true){
			tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
				attributeState "on", label:'${name}', action:"switch.off", icon:"st.switches.switch.on", backgroundColor:"#00a0dc", nextState:"turningOff"
				attributeState "off", label:'${name}', action:"switch.on", icon:"st.switches.switch.off", backgroundColor:"#ffffff", nextState:"turningOn"
				attributeState "turningOn", label:'${name}', action:"switch.off", icon:"st.switches.switch.on", backgroundColor:"#00a0dc", nextState:"turningOff"
				attributeState "turningOff", label:'${name}', action:"switch.on", icon:"st.switches.switch.off", backgroundColor:"#ffffff", nextState:"turningOn"
			}
			tileAttribute ("device.level", key: "SLIDER_CONTROL") {
				attributeState "level", action:"switch level.setLevel"
			}
		}
		valueTile("power", "device.power", width: 2, height: 2) {
			state "default", label:'${currentValue} W'
		}
		valueTile("energy", "device.energy", width: 2, height: 2) {
			state "default", label:'${currentValue} kWh'
		}
		standardTile("reset", "device.energy", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "default", label:'reset kWh', action:"reset"
		}
		standardTile("refresh", "device.switch", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "default", label:"", action:"refresh.refresh", icon:"st.secondary.refresh"
		}
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

	main(["switch","power","energy"])
	details(["switch", "power", "energy", "refresh", "reset"])
}

def getCommandClassVersions() {
	[
			// cc:
			0x22: 1, // Application Status
			0x55: 1, // Transport Service
			0x56: 1, // Crc16 Encap
			0x5E: 1, //
			0x6C: 1, //
			0x98: 1, // Security
			// sec:
			0x26: 3, // Switch Multilevel
			0x32: 3, // Meter
			0x59: 1, // Association Grp Info
			0x5A: 1, // Device Reset Locally
			0x5B: 2, // Central Scene
			0x70: 2, // Configuration
			0x71: 2, // Notification
			0x72: 2, // Manufacturer Specific
			0x73: 1, // Power Level
			0x75: 2, // Protection
			0x7A: 2, // Firmware Update Md
			0x85: 2, // Association
			0x86: 1, // Version
			0x8E: 2  // Multi Channel Association
	]
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

	response(refresh())
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
	def integerValue = (value == "default" ? preference.defaultValue : value.intValue())
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

// parse events into attributes
def parse(String description) {
	//log.debug "description: ${description}"
	def result = null
	if (description != "updated") {
		def cmd = zwave.parse(description, commandClassVersions)
		//log.debug "cmd: ${cmd}"
		if (cmd) {
			result = zwaveEvent(cmd)
			//log.debug("'$description' parsed to $result")
		} else {
			log.debug("Couldn't zwave.parse '$description'")
		}
	}
	result
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

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd) {
	dimmerEvents(cmd)
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicSet cmd) {
	dimmerEvents(cmd)
}

def zwaveEvent(physicalgraph.zwave.commands.switchmultilevelv3.SwitchMultilevelReport cmd) {
	dimmerEvents(cmd)
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
	log.debug "Unhandled command: ${cmd}"
	[:]
}

def dimmerEvents(physicalgraph.zwave.Command cmd) {
	def result = []
	def value = (cmd.value ? "on" : "off")
	def switchEvent = createEvent(name: "switch", value: value, descriptionText: "$device.displayName was turned $value")
	result << switchEvent
	if (cmd.value) {
		result << createEvent(name: "level", value: cmd.value == 99 ? 100 : cmd.value , unit: "%")
	}
	if (switchEvent.isStateChange) {
		result << response(["delay 3000", meterGet(scale: 2).format()])
	}
	return result
}

def zwaveEvent(physicalgraph.zwave.commands.meterv3.MeterReport cmd) {
	log.debug "v3 Meter report: "+cmd
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

def configure() {
	log.debug "configure()"
	def result = []

	result << response(encap(meterGet(scale: 0)))
	result << response(encap(meterGet(scale: 2)))
}

def on() {
	encapSequence([
			zwave.basicV1.basicSet(value: 0xFF),
			zwave.switchMultilevelV1.switchMultilevelGet(),
	], 1000)
}

def off() {
	encapSequence([
			zwave.basicV1.basicSet(value: 0x00),
			zwave.switchMultilevelV1.switchMultilevelGet(),
	], 1000)
}

def setLevel(level, rate = null) {
	if(level > 99) level = 99
	encapSequence([
			zwave.basicV1.basicSet(value: level),
			zwave.switchMultilevelV1.switchMultilevelGet()
	], 1000)
}

/**
 * PING is used by Device-Watch in attempt to reach the Device
 * */
def ping() {
	log.debug "ping() called"
	refresh()
}

def refresh() {
	log.debug "refresh()"

	encapSequence([
			zwave.switchMultilevelV1.switchMultilevelGet(),
			meterGet(scale: 0),
			meterGet(scale: 2),
	], 1000)
}

def reset() {
	encapSequence([
			meterReset(),
			meterGet(scale: 0)
	])
}

def meterGet(scale) {
	zwave.meterV2.meterGet(scale)
}

def meterReset() {
	zwave.meterV2.meterReset()
}

private encapSequence(cmds, Integer delay=250) {
	delayBetween(cmds.collect{ encap(it) }, delay)
}

private encap(physicalgraph.zwave.Command cmd) {
	if (zwaveInfo?.zw?.contains("s")) {
		secEncap(cmd)
	} else {
		log.debug "no encapsulation supported for command: $cmd"
		cmd.format()
	}
}

private secEncap(physicalgraph.zwave.Command cmd) {
	log.debug "encapsulating command using Secure Encapsulation, command: $cmd"
	zwave.securityV1.securityMessageEncapsulation().encapsulate(cmd).format()
}

private getParameterMap() {[
	[
		name: "Remember device state", key: "rememberDeviceState", type: "boolean",
		parameterNumber: 1, size: 1, defaultValue: 1,
		optionInactive: 0, inactiveDescription: "remains switched off after restoring power",
		optionActive: 1, activeDescription: "restores remembered state after restoring power",
		description: "This parameter determines how the device will react in the event of power supply failure (e.g. power outage). "
	],
	[
		name: "LED frame – power limit", key: "ledFrame–PowerLimit", type: "range",
		parameterNumber: 10, size: 4, defaultValue: 3500,
		range: "100..5000",
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
		range: "0..102",
		description: "This parameter allows to adjust the LED frame brightness. " +
				"101 - brightness directly proportional to set level, " +
				"102 - brightness inversely proportional to set level"
	],
	[
		name: "Minimum brightness level", key: "minimumBrightnessLevel", type: "range",
		parameterNumber: 150, size: 1, defaultValue: 1,
		range: "1..98",
		description: "This parameter is set automatically during the calibration process, but can be changed manually after the calibration."
	],
	[
		name: "Maximum brightness level", key: "maximumBrightnessLevel", type: "range",
		parameterNumber: 151, size: 1, defaultValue: 99,
		range: "2..99",
		description: "This parameter is set automatically during the calibration process, but can be changed manually after the calibration."
	],
	[
		name: "Incandescence level of dimmable compact fluorescent lamps", key: "incandescenceLevelOfDimmableCompactFluorescentLamps", type: "range",
		parameterNumber: 152, size: 1, defaultValue: 1,
		range: "1..99",
		description: "The virtual value set as a percentage level between parameters MIN (1%) and MAX. (99%). The device will set to this value after the first switch on. It is required for warming up and switching dimmable compact fluorescent lamps and certain types of light sources."
	],
	[
		name: "Incandescence time of dimmable compact fluorescent lamps", key: "incandescenceTimeOfDimmableCompactFluorescentLamps", type: "range",
		parameterNumber: 153, size: 2, defaultValue: 0,
		range: "0..255",
		description: "This parameter determines the time required for switching compact fluorescent lamps and certain types of light sources. Setting this parameter to 0 will disable the incandescence functionality."
	],
	[
		name: "Automatic control – dimming step size", key: "automaticControl–DimmingStepSize", type: "range",
		parameterNumber: 154, size: 1, defaultValue: 1,
		range: "1..99",
		description: "This parameter defines the percentage value of dimming step during the automatic control."
	],
	[
		name: "Automatic control – time of dimming step", key: "automaticControl–TimeOfDimmingStep", type: "range",
		parameterNumber: 155, size: 2, defaultValue: 1,
		range: "0..255",
		description: "This parameter defines the time of performing a single dimming step set in parameter 154 during the automatic control."
	],
	[
		name: "Manual control – dimming step size", key: "manualControl–DimmingStepSize", type: "range",
		parameterNumber: 156, size: 1, defaultValue: 1,
		range: "1..99",
		description: "This parameter defines the percentage value of the dimming step during the manual control. " +
				"1 - [MSB] – Notification Type (Default)\n" +
				"2 - Notification Value\n" +
				"3 - Event/State Parameters\n" +
				"4 - [LSB] – action\n" +
				"1 to 99 - (1-99%, 1% step) – dimming step"
	],
	[
		name: "Manual control – time of dimming step", key: "manualControl–TimeOfDimmingStep", type: "range",
		parameterNumber: 157, size: 2, defaultValue: 5,
		range: "0..255",
		description: "This parameter defines the time of performing a single dimming step set in parameter 156 during the manual control."
	],
	[
		name: "Auto-off functionality", key: "auto-OffFunctionality", type: "boolRange",
		parameterNumber: 158, size: 2, defaultValue: 0,
		range: "1..32767", disableValue: 0,
		description: "This parameter allows to automatically switch off the device after a specified time from switching the light source on. It may be useful when the device is installed in the stairway."
	],
	[
		name: "Force auto-calibration", key: "forceAuto-Calibration", type: "enum",
		parameterNumber: 159, size: 1, defaultValue: 0,
		values: [
			0: "readout",
			1: "force auto-calibration without FIBARO Bypass 2",
			2: "force auto-calibration with FIBARO Bypass 2"
		],
		description: "Changing value of this parameter will force the calibration process. During the calibration parameter is set to 1 or 2 and switched to 0 upon completion."
	],
	[
		name: "Auto-calibration status (read-only parameter)", key: "auto-CalibrationStatus(Read-OnlyParameter)", type: "boolean",
		parameterNumber: 160, size: 1, defaultValue: 0,
		optionInactive: 0, inactiveDescription: "calibration procedure not performed or the device operates on manual settings",
		optionActive: 1, activeDescription: "the device operates on auto-calibration settings",
		description: "This parameter determines operating mode of the device (automatic/manual settings)."
	],
	[
		name: "Burnt out bulb detection", key: "burntOutBulbDetection", type: "boolRange",
		parameterNumber: 161, size: 1, defaultValue: 0,
		range: "1..99", disableValue: 0,
		description: "This parameter defines percentage power variation (compared to power consumption measured during the calibration) to be interpreted as load error/burnt out bulb."
	],
	[
		name: "Time delay of a burnt out bulb and overload detection", key: "timeDelayOfABurntOutBulbAndOverloadDetection", type: "boolRange",
		parameterNumber: 162, size: 2, defaultValue: 5,
		range: "1..255", disableValue: 0,
		description: "This parameter defines detection delay for the burnt out bulb (parameter 161) and overload (parameter 2)."
	],
	/*[
		name: "First button – Switch ON value sent to 2nd and 3rd association groups", key: "firstButton–SwitchOnValueSentTo2NdAnd3RdAssociationGroups", type: "range",
		parameterNumber: 163, size: 2, defaultValue: 255,
		range: "0..255",
		description: "This parameter defines value sent with Switch OFF command to devices associated in 2nd and 3rd association group. " +
				"0 to 99 - value sent\n" +
				"254 - send value equal to the current level\n" +
				"255 - value sent (Default)\t"
	],
	[
		name: "Second button – Switch OFF value sent to 2nd and 3rd association groups", key: "secondButton–SwitchOffValueSentTo2NdAnd3RdAssociationGroups", type: "range",
		parameterNumber: 164, size: 2, defaultValue: 0,
		range: "0..254",
		description: "This parameter defines value sent with Switch OFF command to devices associated in 2nd and 3rd association group." +
                "0 to 99 (Default) - value sent\n" +
                "254 - send value equal to the current level"
	],*/
	[
		name: "Double click – set level", key: "doubleClick–SetLevel", type: "range",
		parameterNumber: 165, size: 1, defaultValue: 99,
		range: "0..99",
		description: "This parameter defines brightness level set after double-clicking any of the buttons. The same value is also sent to devices associated with 2nd and 3rd association group."
	],
	[
		name: "Load control mode", key: "loadControlMode", type: "enum",
		parameterNumber: 170, size: 1, defaultValue: 2,
		values: [
			0: "forced leading edge",
			1: "forced trailing edge",
			2: "control mode selected automatically (based on auto-calibration)"
		],
		description: "This parameter allows to set the desired load control mode. Auto-calibration sets value of this parameter to 2 (control mode recognized during auto-calibration), but the installer may force control mode using this parameter. After changing parameter value, turn the load OFF and ON to change control mode."
	],
	[
		name: "Load control mode recognized during auto-calibration (read only)", key: "loadControlModeRecognizedDuringAuto-Calibration(ReadOnly)", type: "boolean",
		parameterNumber: 171, size: 1, defaultValue: 0,
		optionInactive: 0, inactiveDescription: "leading edge",
		optionActive: 1, activeDescription: "trailing edge",
		description: "This parameter allows to read load control mode that was set during auto-calibration."
	],
	[
		name: "ON/OFF mode", key: "on/OffMode", type: "enum",
		parameterNumber: 172, size: 1, defaultValue: 2,
		values: [
			0: "ON/OFF mode disabled (dimming is possible)",
			1: "ON/OFF mode enabled (dimming is not possible)",
			2: "mode selected automatically"
		],
		description: "This mode is necessary while connecting non-dimmable light sources. Setting this parameter to 1 automatically ignores brightening/dimming time settings. Forced auto-calibration will set this parameter’s value to 2."
	],
	[
		name: "Dimmability of the load (read only)", key: "dimmabilityOfTheLoad(ReadOnly)", type: "boolean",
		parameterNumber: 173, size: 1, defaultValue: 0,
		optionInactive: 0, inactiveDescription: "load recognized as dimmable",
		optionActive: 1, activeDescription: "load recognized as non-dimmable",
		description: "This parameter allows to read if the load detected during calibration procedure is dimmable."
	],
	[
		name: "Soft-start functionality", key: "soft-StartFunctionality", type: "enum",
		parameterNumber: 174, size: 1, defaultValue: 1,
		values: [
			0: "no soft-start",
			1: "short soft-start (0.1s)",
			2: "long soft-start (0.5s)"
		],
		description: "This parameter allows to set time required to warm up the filament of halogen bulb."
	],
	[
		name: "Auto-calibration after power on", key: "auto-CalibrationAfterPowerOn", type: "enum",
		parameterNumber: 175, size: 1, defaultValue: 1,
		values: [
			0: "no auto-calibration after power on",
			2: "Auto-calibration after each power on",
			3: "Auto-calibration after each LOAD ERROR (no load, load failure, burnt out bulb), if parameter 176 is set to 1 also after SURGE (output overvoltage) and OVERCURRENT (output overcurrent)",
			4: "Auto-calibration after each power on or after each LOAD ERROR (no load, load failure, burnt out bulb), if parameter 176 is set to 1 also after SURGE (output overvoltage) and OVERCURRENT (output overcurrent)"
		],
		description: "This parameter determines the trigger of auto-calibration procedure, e.g. power on, load error, etc."
	],
	[
		name: "Behaviour after OVERCURRENT or SURGE", key: "behaviourAfterOvercurrentOrSurge", type: "boolean",
		parameterNumber: 176, size: 1, defaultValue: 1,
		optionInactive: 0, inactiveDescription: "device permanently disabled until re-enabling by command or external switch",
		optionActive: 1, activeDescription: "three attempts to turn on the load",
		description: "Error occurrences related to surge or overcurrent results in turning off the output to prevent possible malfunction. By default the device performs three attempts to turn on the load (useful in case of temporary, short failures of the power supply)."
	],
	[
		name: "Brightness level correction for flickering loads", key: "brightnessLevelCorrectionForFlickeringLoads", type: "range",
		parameterNumber: 177, size: 2, defaultValue: 255,
		range: "0..255",
		description: "Correction reduces spontaneous flickering of some capacitive loads (e.g. dimmable LEDs) at certain brightness levels in 2-wire installation. In countries using ripple-control, correction may cause changes in brightness. In this case it is necessary to disable correction or adjust the time of correction for flickering loads." +
				"0 - automatic correction disabled\n" +
				"1 to 254 - (1-254s, 1s step) – duration of correction\n" +
				"255 - automatic correction always enabled (Default)"
	],
	[
		name: "Method of calculating the active power", key: "methodOfCalculatingTheActivePower", type: "enum",
		parameterNumber: 178, size: 1, defaultValue: 0,
		values: [
			0: "measurement based on the standard algorithm",
			1: "approximation based on the calibration data",
			2: "approximation based on the control angle"
		],
		description: "This parameter defines how to calculate active power. It is useful in a case of 2-wire connection with light sources other than resistive."
	],
	[
		name: "Approximated power at the maximum brightness level", key: "approximatedPowerAtTheMaximumBrightnessLevel", type: "range",
		parameterNumber: 179, size: 2, defaultValue: 0,
		range: "0..500",
		description: "This parameter determines the approximate value of the power that will be reported by the device at its maximum brightness level."
	],
	[
		name: "Overload safety switch", key: "overloadSafetySwitch", type: "boolRange",
		parameterNumber: 2, size: 4, defaultValue: 3500,
		range: "10..5000", disableValue: 0,
		description: "This function allows to turn off the controlled device in case of exceeding the defined power. Controlled device can be turned back on via the button or sending a control frame."
	],
	[
		name: "Buttons orientation", key: "buttonsOrientation", type: "boolean",
		parameterNumber: 24, size: 1, defaultValue: 0,
		optionInactive: 0, inactiveDescription: "default (1st button brightens, 2nd button dims)",
		optionActive: 1, activeDescription: "reversed (1st button dims, 2nd button brightens)",
		description: "This parameter allows reversing the operation of the buttons."
	],
	[
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
			2: "Notification Status",
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
	/*[
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
		name: "Power reports – on change", key: "powerReports–OnChange", type: "boolRange",
		parameterNumber: 61, size: 2, defaultValue: 15,
		range: "1..500", disableValue: 0,
		description: "This parameter defines minimal change (from the last reported) in measured power that results in sending new report. For loads under 50W the parameter is irrelevant, report are sent every 5W change."
	],
	[
		name: "Power reports – periodic", key: "powerReports–Periodic", type: "boolRange",
		parameterNumber: 62, size: 2, defaultValue: 3600,
		range: "30..32400", disableValue: 0,
		description: "This parameter defines reporting interval for measured power. Periodic reports are independent from changes in value (parameter 61)."
	],
	[
		name: "Energy reports – on change", key: "energyReports–OnChange", type: "boolRange",
		parameterNumber: 65, size: 2, defaultValue: 10,
		range: "1..500", disableValue: 0,
		description: "This parameter defines minimal change (from the last reported) in measured energy that results in sending new report."
	],
	[
		name: "Energy reports – periodic", key: "energyReports–Periodic", type: "boolRange",
		parameterNumber: 66, size: 2, defaultValue: 3600,
		range: "30..32400", disableValue: 0,
		description: "This parameter defines reporting interval for measured energy. Periodic reports are independent from changes in value (parameter 65)."
	]
]}