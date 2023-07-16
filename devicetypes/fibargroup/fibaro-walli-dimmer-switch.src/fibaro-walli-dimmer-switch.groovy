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
	definition (name: "Fibaro Walli Dimmer Switch", namespace: "fibargroup", author: "SmartThings", mnmn: "SmartThings", vid: "generic-dimmer-power-energy", ocfDeviceType: "oic.d.switch", runLocally: false, executeCommandsLocally: false) {

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

		// Fibaro Walli Dimmer FGWDEU-111,
		// Raw Description: zw:Ls type:1101 mfr:010F prod:1C01 model:1000 ver:5.01 zwv:6.02 lib:03 cc:5E,55,98,56,6C,22 sec:26,85,8E,59,86,72,5A,73,32,70,71,75,5B,7A role:05 ff:9C00 ui:9C00
		fingerprint mfr: "010F", prod: "1C01", model: "1000", deviceJoinName: "Fibaro Dimmer Switch"
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
		state.currentPreferencesState."$it.key".status = "synced"
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
			// boolean values are returned as strings from the UI preferences
			return settings."$parameterKey" == 'true' ? preference.optionActive : preference.optionInactive
		case "range":
			return settings."$parameterKey"
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
	resetEnergyMeter()
}

def resetEnergyMeter() {
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
			8: "colour changes smoothly depending on the measured power",
			9: "colour changes in steps depending on the measured power"
		],
		description: "LED colour when the device is ON. When set to 8 or 9, LED frame colour will change depending on the measured power and parameter 10. Other colours are set permanently and do not depend on the power consumption."
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
		description: "LED colour when the device is OFF."
	],
	[
		name: "LED frame – brightness", key: "ledFrame–Brightness", type: "range",
		parameterNumber: 13, size: 1, defaultValue: 100,
		range: "0..102",
		description: "Adjust the LED frame brightness. " +
				"101 - brightness directly proportional to set level, " +
				"102 - brightness inversely proportional to set level"
	],
	[
		name: "Manual control – dimming step size", key: "manualControl–DimmingStepSize", type: "range",
		parameterNumber: 156, size: 1, defaultValue: 1,
		range: "1..99",
		description: "Percentage value of the dimming step during the manual control (1 to 99 %)"
	],
	[
		name: "Manual control – time of dimming step", key: "manualControl–TimeOfDimmingStep", type: "range",
		parameterNumber: 157, size: 2, defaultValue: 5,
		range: "0..255",
		description: "Time to perform a single dimming step set in the parameter: 'Manual control – dimming step size' during the manual control."
	],
	[
		name: "Double click – set level", key: "doubleClick–SetLevel", type: "range",
		parameterNumber: 165, size: 1, defaultValue: 99,
		range: "0..99",
		description: "Brightness level set after double-clicking any of the buttons."
	],
	[
		name: "Buttons orientation", key: "buttonsOrientation", type: "boolean",
		parameterNumber: 24, size: 1, defaultValue: 0,
		optionInactive: 0, inactiveDescription: "default (1st button brightens, 2nd button dims)",
		optionActive: 1, activeDescription: "reversed (1st button dims, 2nd button brightens)",
		description: "Reverse the operation of the buttons."
	]
]}