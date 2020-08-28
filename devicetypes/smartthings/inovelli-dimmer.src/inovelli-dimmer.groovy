/*	Copyright 2020 SmartThings
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
*	Inovelli Dimmer
*
*	Copyright 2020 SmartThings
*
*/
metadata {
	definition(name: "Inovelli Dimmer", namespace: "smartthings", author: "SmartThings", ocfDeviceType: "oic.d.switch", mcdSync: true) {
		capability "Actuator"
		capability "Configuration"
		capability "Energy Meter"
		capability "Health Check"
		capability "Refresh"
		capability "Sensor"
		capability "Switch"
		capability "Switch Level"
		capability "Power Meter"

		fingerprint mfr: "031E", prod: "0001", model: "0001", deviceJoinName: "Inovelli Dimmer Switch", mnmn: "SmartThings", vid: "SmartThings-smartthings-Inovelli_Dimmer" //Inovelli Dimmer LZW31-SN
		fingerprint mfr: "031E", prod: "0003", model: "0001", deviceJoinName: "Inovelli Dimmer Switch", mnmn: "SmartThings", vid: "SmartThings-smartthings-Inovelli_Dimmer_LZW31" //Inovelli Dimmer LZW31
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
		valueTile("power", "device.power", width: 2, height: 2) {
			state "default", label: '${currentValue} W'
		}
		valueTile("energy", "device.energy", width: 2, height: 2) {
			state "default", label: '${currentValue} kWh'
		}
		standardTile("refresh", "device.switch", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "default", label: "", action: "refresh.refresh", icon: "st.secondary.refresh"
		}
	}

	main(["switch", "power", "energy"])
	details(["switch", "power", "energy", "refresh"])

	preferences {
		// Preferences template begin
		parameterMap.each {
			input(title: it.name, description: it.description, type: "paragraph", element: "paragraph")

			switch (it.type) {
				case "boolRange":
					input(
						name: it.key + "Boolean", type: "bool", title: "Enable", description: "If you disable this option, it will overwrite setting below.",
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
						description: "Option enabled: ${it.activeDescription}\n" + "Option disabled: ${it.inactiveDescription}"
					)
					input(
						name: it.key, type: "bool", title: "Enable",
						defaultValue: it.defaultValue == it.activeOption, required: false
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

private getUP_BUTTON(){ 1 }
private getDOWN_BUTTON(){ 2 }
private getCONFIGURATION_BUTTON(){ 3 }

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
	if(isInovelliDimmerLZW31SN()) {
		createChildButtonDevices()
		def value = ['pushed', 'pushed_2x', 'pushed_3x', 'pushed_4x', 'pushed_5x'].encodeAsJson()
		sendEvent(name: "supportedButtonValues", value: value)
		sendEvent(name: "numberOfButtons", value: 3, displayed: true)
	}
	createChildDevice("smartthings", "Child Color Control", "${device.deviceNetworkId}:4", "LED Bar", "LEDColorConfiguration")
}

def configure() {
	sendHubCommand(getReadConfigurationFromTheDeviceCommands())
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

				if (isNotNull(settings."$preferenceName")) {
					if (!settings."$preferenceName") {
						state.currentPreferencesState."$it.key".status = "disablePending"
					} else if (state.currentPreferencesState."$it.key".status == "disabled") {
						state.currentPreferencesState."$it.key".status = "syncPending"
					}
				} else {
					state.currentPreferencesState."$it.key".status = "syncPending"
				}
			}
		} else if (state.currentPreferencesState."$it.key".value == null) {
			log.warn "Preference ${it.key} no. ${it.parameterNumber} has no value. Please check preference declaration for errors."
		}
	}
	syncConfiguration()
	// Preferences template end

	response(refresh())
}

private getReadConfigurationFromTheDeviceCommands() {
	def commands = []
	parameterMap.each {
		state.currentPreferencesState."$it.key".status = "reverseSyncPending"
		commands += encap(zwave.configurationV2.configurationGet(parameterNumber: it.parameterNumber))
	}
	commands
}

private syncConfiguration() {
	def commands = []
	log.debug "syncConfiguration ${settings}"
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
	if (cmd.parameterNumber == 13) {
		handleLEDPreferenceEvent(cmd)
	} else {
		// Preferences template begin
		log.debug "Configuration report: ${cmd}"
		def preference = parameterMap.find({ it.parameterNumber == cmd.parameterNumber })
		def key = preference.key
		def preferenceValue = getPreferenceValue(preference, cmd.scaledConfigurationValue)
		log.debug "settings.key ${settings."$key"} preferenceValue ${preferenceValue}"

		if (state.currentPreferencesState."$key".status == "reverseSyncPending") {
			log.debug "reverseSyncPending"
			state.currentPreferencesState."$key".value = preferenceValue
			state.currentPreferencesState."$key".status = "synced"
		} else {
			if (preferenceValue instanceof String && settings."$key" == preferenceValue.toBoolean()) {
				state.currentPreferencesState."$key".value = settings."$key"
				state.currentPreferencesState."$key".status = "synced"
			} else if (preferenceValue instanceof Integer && settings."$key" == preferenceValue) {
				state.currentPreferencesState."$key".value = settings."$key"
				state.currentPreferencesState."$key".status = "synced"
			} else if (preference.type == "boolRange") {
				log.debug "${state.currentPreferencesState."$key".status}"
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
	log.debug "settings parameter key ${settings."$parameterKey"} ${preference} "
	switch (preference.type) {
		case "boolean":
			return settings."$parameterKey" ? preference.optionActive : preference.optionInactive
		case "boolRange":
			def parameterKeyBoolean = parameterKey + "Boolean"
			return !isNotNull(settings."$parameterKeyBoolean") || settings."$parameterKeyBoolean" ? settings."$parameterKey" : preference.disableValue
		case "range":
			return settings."$parameterKey"
		default:
			return Integer.parseInt(settings."$parameterKey")
	}
}

private isNotNull(value) {
	return value != null
}

private isPreferenceChanged(preference) {
	if (isNotNull(settings."$preference.key")) {
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

def parse(String description) {
	def result = null
	if (description != "updated") {
		def cmd = zwave.parse(description)
		if (cmd) {
			result = zwaveEvent(cmd)
			log.debug("'$description' parsed to $result")
		} else {
			log.debug("Couldn't zwave.parse '$description'")
		}
	}
	log.debug "parsed '${description}' to ${result.inspect()}"
	result
}

def handleLEDPreferenceEvent(cmd) {
	def hueState = [name: "hue", value: "${Math.round(zwaveValueToHuePercent(cmd.scaledConfigurationValue))}"]
	def childDni = "${device.deviceNetworkId}:4"
	def childDevice = childDevices.find { it.deviceNetworkId == childDni }
	childDevice?.sendEvent(hueState)
	childDevice?.sendEvent(name: "saturation", value: "100")
}

def createChildDevice(childDthNamespace, childDthName, childDni, childComponentLabel, childComponentName) {
	try {
		log.debug "Creating a child device: ${childDthNamespace}, ${childDthName}, ${childDni}, ${childComponentLabel}, ${childComponentName}"
		addChildDevice(childDthNamespace, childDthName, childDni, device.hub.id,
			[
				completedSetup: true,
				label         : childComponentLabel,
				isComponent   : true,
				componentName : childComponentName,
				componentLabel: childComponentLabel
			])
	} catch (Exception e) {
		log.debug "Exception: ${e}"
	}
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd) {
	dimmerEvents(cmd)
}

def zwaveEvent(physicalgraph.zwave.commands.switchmultilevelv3.SwitchMultilevelReport cmd) {
	dimmerEvents(cmd)
}

def dimmerEvents(physicalgraph.zwave.Command cmd) {
	def switchEvent = createEvent([name: "switch", value: cmd.value ? "on" : "off", descriptionText: "$device.displayName was turned ${cmd.value ? "on" : "off"}"])
	def dimmerEvent = createEvent([name: "level", value: cmd.value == 99 ? 100 : cmd.value, unit: "%"])
	def result = [switchEvent, dimmerEvent]
	if (switchEvent.isStateChange) {
		result << response(["delay 1000", zwave.meterV3.meterGet(scale: 2).format()])
	}
	return result
}

def on() {
	encapSequence([
		zwave.basicV1.basicSet(value: 0xFF),
		zwave.basicV1.basicGet()
	], 1000)
}

def off() {
	encapSequence([
		zwave.basicV1.basicSet(value: 0x00),
		zwave.basicV1.basicGet()
	], 1000)
}

def setLevel(level) {
	if (level > 99) level = 99
	encapSequence([
		zwave.basicV1.basicSet(value: level),
		zwave.switchMultilevelV1.switchMultilevelGet()
	], 1000)
}

def zwaveEvent(physicalgraph.zwave.commands.meterv3.MeterReport cmd) {
	def map = [:]
	if (cmd.meterType == 1 && cmd.scale == 0) {
		map = [name: "energy", value: cmd.scaledMeterValue.toDouble().round(1), unit: "kWh"]
	} else if (cmd.meterType == 1 && cmd.scale == 2) {
		map = [name: "power", value: Math.round(cmd.scaledMeterValue), unit: "W"]
	}
	createEvent(map)
}

private getButtonLabel() {
	[
		"Up button",
		"Down button",
		"Configuration button"
	]
}

private void createChildButtonDevices() {
	for (buttonNumber in 1..3) {
		def child = addChildDevice("smartthings", "Child Button", "${device.deviceNetworkId}:${buttonNumber}", device.hub.id,
			[
				completedSetup: true,
				label         : buttonLabel[buttonNumber - 1],
				isComponent   : true,
				componentName : "button$buttonNumber",
				componentLabel: buttonLabel[buttonNumber - 1]
			])

		def value = buttonNumber == 3 ? ['pushed'] : ['pushed', 'pushed_2x', 'pushed_3x', 'pushed_4x', 'pushed_5x']
		child.sendEvent(name: "supportedButtonValues", value: value.encodeAsJSON(), displayed: false)
		child.sendEvent(name: "numberOfButtons", value: 1, displayed: false)
		child.sendEvent(name: "button", value: "pushed", data: [buttonNumber: 1], displayed: false)
	}
}

def sendButtonEvent(gesture, buttonNumber) {
	def event = createEvent([name: "button", value: gesture, data: [buttonNumber: buttonNumber], isStateChange: true])
	String childDni = "${device.deviceNetworkId}:$buttonNumber"
	def child = childDevices.find { it.deviceNetworkId == childDni }
	child?.sendEvent(event)
	return createEvent([name: "button", value: gesture, data: [buttonNumber: buttonNumber], isStateChange: true, displayed: false])
}

def labelForGesture( attribute) {
	def	gesture = "pushed"
	if (attribute == 0) {
		gesture;
	} else {
		def number = attribute - 1;
		"${gesture}_${number}x";
	}
}

def zwaveEvent(physicalgraph.zwave.commands.centralscenev1.CentralSceneNotification cmd) {
	log.info("CentralSceneNotification, keyAttributes=${cmd.keyAttributes}, sceneNumber=${cmd.sceneNumber}")
	def singleClick = 0;
	def multipleClicks = [3, 4, 5, 6]
	def supportedAttributes = [singleClick] + multipleClicks
	int attribute = cmd.keyAttributes
	int scene = cmd.sceneNumber
	if (scene == 1 && attribute in supportedAttributes) {
		sendButtonEvent(labelForGesture(attribute), DOWN_BUTTON);
	} else if (scene == 2 && attribute in supportedAttributes) {
		sendButtonEvent(labelForGesture(attribute), UP_BUTTON);
	} else if (scene == 3 && attribute == singleClick) {
		sendButtonEvent("pushed", CONFIGURATION_BUTTON)
	} else {
		log.warn("Unhandled scene notification, keyAttributes=${attribute}, sceneNumber=${scene}")
	}
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
	// Handles all Z-Wave commands we aren't interested in
	log.debug "${cmd}"
	[:]
}

def childSetColor(value) {
	sendHubCommand setColorCmd(value)
}

def setColorCmd(value) {
	if (value.hue == null || value.saturation == null) return
	def ledColor = Math.round(huePercentToZwaveValue(value.hue))
	encapSequence([
		zwave.configurationV2.configurationSet(scaledConfigurationValue: ledColor, parameterNumber: 13, size: 2),
		zwave.configurationV2.configurationGet(parameterNumber: 13)
	], 1000)
}

private huePercentToZwaveValue(value) {
	return value <= 2 ? 0 : (value >= 98 ? 255 : value / 100 * 255)
}

private zwaveValueToHuePercent(value) {
	return value <= 2 ? 0 : (value >= 254 ? 100 : value / 255 * 100)
}

def refresh() {
	encapSequence([
		zwave.basicV1.basicGet(),
		zwave.meterV3.meterGet(scale: 0)
	], 1000)
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

private encapSequence(cmds, Integer delay = 250) {
	delayBetween(cmds.collect { encap(it) }, delay)
}

private isInovelliDimmerLZW31SN(){
	zwaveInfo.mfr.equals("031E") && zwaveInfo.prod.equals("0001") && zwaveInfo.model.equals("0001")
}

private getParameterMap() {
	[
		[
			name           : "Dimming Speed", key: "dimmingSpeed", type: "range",
			parameterNumber: 1, size: 1, defaultValue: 3,
			range          : "1..100",
			description    : "How fast or slow the light turns on when you hold the switch in seconds (ie: dimming from 10-20%, 80-60%, etc). Value 0 - Instant On. This parameter can be set without a HUB from the Configuration Button. Finally, if you are using a,dumb switch in a 3-Way setting, this parameter will not work if you manually press the dumb switch (it will only work if you press the smart switch)."
		],
		[
			name           : "Power On State", key: "powerOnState", type: "range",
			parameterNumber: 11, size: 1, defaultValue: 0,
			range          : "0..101",
			description    : "When power is restored, the switch reverts to either On, Off, or Last Level. Example of how the values work: 0 = Off, 1-100 = Specific % On, 101 = Returns to Level before Power Outage. This parameter can be set without a HUB from the Configuration Button."
		],
		[
			name           : "LED Indicator Intensity", key: "ledIndicatorIntensity", type: "range",
			parameterNumber: 14, size: 1, defaultValue: 5,
			range          : "0..10",
			description    : "This will set the intensity of the LED bar (ie: how bright it is). Example of how the values work: 0 = Off, 1 = Low, 5 = Medium, 10 = High. This parameter can be set without a HUB from the Configuration Button."
		],
		[
			name           : "LED Indicator Intensity (When Off)", key: "ledIndicatorIntensity(WhenOff)", type: "range",
			parameterNumber: 15, size: 1, defaultValue: 1,
			range          : "0..10",
			description    : "This is the intensity of the LED bar when the switch is off. Example of how the values work: 0 = Off, 1 = Low, 5 = Medium, 10 = High. This parameter can be set without a HUB from the Configuration Button."
		],
		[
			name           : "LED Indicator Timeout", key: "ledIndicatorTimeout", type: "range",
			parameterNumber: 17, size: 1, defaultValue: 3,
			range          : "0..10",
			description    : "Changes the amount of time the RGB Bar shows the Dim level if the LED Bar has been disabled. Example of how the values work: 0 = Always off, 1 = 1 second after level is adjusted."
		],
		[
			name           : "Dimming Speed (Z-Wave)", key: "dimmingSpeed(Z-Wave)", type: "range",
			parameterNumber: 2, size: 1, defaultValue: 101,
			range          : "0..101",
			description    : "How fast or slow the light turns dim when you adjust the switch remotely (ie: dimming from 10-20%, 80-60%, etc). Entering the value of 101 = Keeps the switch in sync with Parameter 1."
		],
		[
			name           : "Ramp Rate", key: "rampRate", type: "range",
			parameterNumber: 3, size: 1, defaultValue: 101,
			range          : "0..101",
			description    : "How fast or slow the light turns on when you press the switch 1x to bring from On to Off or Off to On. Entering the value of 101 = Keeps the switch in sync with Parameter 1."
		],
		[
			name           : "Ramp Rate (Z-Wave)", key: "rampRate(Z-Wave)", type: "range",
			parameterNumber: 4, size: 1, defaultValue: 101,
			range          : "0..101",
			description    : "How fast or slow the light turns on when you bring your switch from On to Off or Off to On remotely. Entering the value of 101 = Keeps the switch in sync with Parameter 1."
		],
		[
			name           : "Invert Switch", key: "invertSwitch", type: "boolean",
			parameterNumber: 7, size: 1, defaultValue: 0,
			optionInactive : 0, inactiveDescription: "Disabled",
			optionActive   : 1, activeDescription: "Enabled",
			description    : "Inverts the switch"
		],
		[
			name           : "Auto Off Timer", key: "autoOffTimer", type: "boolRange",
			parameterNumber: 8, size: 2, defaultValue: 0,
			range          : "1..32767", disableValue: 0,
			description    : "Automatically turns the switch off after x amount of seconds (value 0 = Disabled)"
		]
	]
}
