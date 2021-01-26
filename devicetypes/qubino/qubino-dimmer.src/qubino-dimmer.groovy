/**
 *  Copyright 2020 SmartThings
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *	  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */
metadata {
	definition(name: "Qubino Dimmer", namespace: "qubino", author: "SmartThings", mnmn: "SmartThings", vid:"qubino-dimmer-power-energy", ocfDeviceType: "oic.d.switch", runLocally: false, executeCommandsLocally: false) {
		capability "Actuator"
		capability "Configuration"
		capability "Energy Meter"
		capability "Health Check"
		capability "Power Meter"
		capability "Refresh"
		capability "Sensor"
		capability "Switch"
		capability "Switch Level"

		// Qubino Flush Dimmer - ZMNHDD
		// Raw Description zw:Ls type:1101 mfr:0159 prod:0001 model:0051 ver:3.08 zwv:4.38 lib:03 cc:5E,5A,73,98 sec:86,72,27,25,26,32,31,71,60,85,8E,59,70 secOut:26 role:05 ff:9C00 ui:9C00 epc:2
		fingerprint mfr: "0159", prod: "0001", model: "0051", deviceJoinName: "Qubino Dimmer"

		// Qubino DIN Dimmer
		// Raw Description: zw:Ls type:1101 mfr:0159 prod:0001 model:0052 ver:3.01 zwv:4.24 lib:03 cc:5E,5A,73,98 sec:86,72,27,25,26,32,71,85,8E,59,70 secOut:26 role:05 ff:9C00 ui:9C00
		fingerprint mfr: "0159", prod: "0001", model: "0052", deviceJoinName: "Qubino Dimmer"

		// Qubino Flush Dimmer 0-10V - ZMNHVD
		// Raw Description: zw:L type:1100 mfr:0159 prod:0001 model:0053 ver:2.04 zwv:4.34 lib:03 cc:5E,86,5A,72,73,27,25,26,85,8E,59,70 ccOut:20,26 role:05 ff:9C00 ui:9C00
		fingerprint mfr: "0159", prod: "0001", model: "0053", deviceJoinName: "Qubino Dimmer", mnmn: "SmartThings", vid:"qubino-dimmer"

		//Qubino Mini Dimmer
		// Raw Description: zw:Ls type:1101 mfr:0159 prod:0001 model:0055 ver:20.02 zwv:5.03 lib:03 cc:5E,6C,55,98,9F sec:86,25,26,85,59,72,5A,70,32,71,73
		fingerprint mfr:"0159", prod:"0001", model:"0055", deviceJoinName: "Qubino Dimmer"
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

		valueTile("power", "device.power", width: 2, height: 2) {
			state "default", label: '${currentValue} W'
		}
		valueTile("energy", "device.energy", width: 2, height: 2) {
			state "default", label: '${currentValue} kWh'
		}

		main(["switch"])
		details(["switch", "level", "power", "energy", "refresh"])
	}

	preferences {
		// Preferences template begin
		parameterMap.each {
			input (
				title: it.name, description: it.description, type: "paragraph",	element: "paragraph"
			)

			switch(it.type) {
				case "boolean":
					input(
						type: "paragraph", element: "paragraph",
						description: "Option enabled: ${it.activeDescription}\n" +
									"Option disabled: ${it.inactiveDescription}"
					)
					input(
						name: it.key, type: "bool",	title: "Enable", required: false,
						defaultValue: it.defaultValue == it.activeOption
					)
					break
				case "enum":
					input(
						name: it.key, title: "Select", type: "enum", required: false, options: it.values,
						defaultValue: it.defaultValue
					)
					break
				case "range":
					input(
						name: it.key, type: "number", title: "Set value (range ${it.range})", range: it.range, required: false,
						defaultValue: it.defaultValue
					)
					break
			}
		}
		// Preferences template end
	}
}

//Globals, input types used in sevice settings (parameter #1: Input 1 switch type)
private getINPUT_TYPE_MONO_STABLE_SWITCH() {0}
private getINPUT_TYPE_BI_STABLE_SWITCH() {1}
private getINPUT_TYPE_POTENTIOMETER() {2}
private getINPUT_TYPE_TEMPERATURE_SENSOR() {3}

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
		if (isPreferenceChanged(it) && !excludeParameterFromSync(it)) {
			log.debug "Preference ${it.key} has been updated from value: ${state.currentPreferencesState."$it.key".value} to ${settings."$it.key"}"
			state.currentPreferencesState."$it.key".status = "syncPending"
		} else if (state.currentPreferencesState."$it.key".value == null) {
			log.warn "Preference ${it.key} no. ${it.parameterNumber} has no value. Please check preference declaration for errors."
		}
	}
	syncConfiguration()
	// Preferences template end
}

def excludeParameterFromSync(preference){
	def exclude = false
	if (preference.key == "input1SwitchType") {
		// Only Flush Dimmer 0-10V supports all input types:
		// 0 - MONO_STABLE_SWITCH,
		// 1 - BI_STABLE_SWITCH,
		// 2 - TYPE_POTENTIOMETER,
		// 3 - TEMPERATURE_SENSO.
		if (supportsMonoAndBiStableSwitchOnly() && (preference.value == INPUT_TYPE_POTENTIOMETER || preference.value == INPUT_TYPE_TEMPERATURE_SENSOR)){
			exclude = true
		}
	} else if (preference.key == "inputsSwitchTypes" || preference.key == "enable/DisableAdditionalSwitch") {
		// Only Flush Dimmer supports this parameter
		if (isDINDimmer() || isFlushDimmer010V()) {
			exclude = true
		}
	} else if (preference.key == "minimumDimmingValue"){
		exclude = true
	}

	if (exclude) {
		log.warn "Preference no ${preference.parameterNumber} - ${preference.key} is not supported by this device"
	}
	return exclude
}

private getReadConfigurationFromTheDeviceCommands() {
	def commands = []
	parameterMap.each {
		state.currentPreferencesState."$it.key".status = "reverseSyncPending"
		commands += zwave.configurationV2.configurationGet(parameterNumber: it.parameterNumber)
	}
	commands
}

private syncConfiguration() {
	def commands = []
	parameterMap.each {
		if (state.currentPreferencesState."$it.key".status == "syncPending") {
			commands += zwave.configurationV2.configurationSet(scaledConfigurationValue: getCommandValue(it), parameterNumber: it.parameterNumber, size: it.size)
			commands += zwave.configurationV2.configurationGet(parameterNumber: it.parameterNumber)
		} else if (state.currentPreferencesState."$it.key".status == "disablePending") {
			commands += zwave.configurationV2.configurationSet(scaledConfigurationValue: it.disableValue, parameterNumber: it.parameterNumber, size: it.size)
			commands += zwave.configurationV2.configurationGet(parameterNumber: it.parameterNumber)
		}
	}
	sendHubCommand(encapCommands(commands))
}

def configure() {
	def commands = []
	log.debug "configure"
	/*
		Association Groups:

		Flush Dimmer:

		Group 1: Lifeline group (reserved for communication with the hub).
		Group 2: BasicSetKey1 (status change report for I1 input), up to 16 nodes.
		Group 3: DimmerStartStopKey1 (status change report for I1 input), up to 16 nodes.
		Group 4: DimmerSetKey1 (status change report of the Flush Dimmer) up to 16 nodes
		Group 5: BasicSetKey2 (status change report for I2 input) up to 16 nodes.
		Group 6: NotificationKey2 (status change report for I2 input) up to 16 nodes.

		Flush Dimmer 0-10V:

		Group 1: Lifeline group (reserved for communication with the hub)
		Group 2: Basic on/off (status change report for the input)
		Group 3: Start level change/stop (status change report for the input).
				 Working only when the Parameter no. 1 is set to mono stable switch type.
		Group 4: Multilevel set (status change report of dimmer). Working only when the Parameter no. 1 is set to mono stable switch type.
		Group 5: Multilevel sensor report (status change report of the analogue sensor).
		Group 6: Multilevel sensor report (status change report of the temperature sensor)


		Qubino DIN Dimmer:

		Group 1: Lifeline group (reserved for communication with the hub).
		Group 2: Basic on/off (status change report for output), up to 16 nodes.
		Group 3: Start level change/stop (status change report for I1 input).
		Group 4: Multilevel set (status change report of the output).
		Group 5: Multilevel sensor report (external temperature sensor report).

	*/
	commands << zwave.multiChannelAssociationV2.multiChannelAssociationRemove(groupingIdentifier:1, nodeId:[])
	commands << zwave.multiChannelAssociationV2.multiChannelAssociationSet(groupingIdentifier:1, nodeId:[zwaveHubNodeId])
	commands << zwave.multiChannelAssociationV2.multiChannelAssociationGet(groupingIdentifier: 1)
	if (isDINDimmer()) {
		//parameter 42 - power reporting time threshold
		commands << zwave.configurationV1.configurationSet(parameterNumber: 42, size: 2, scaledConfigurationValue: 2 * 15 * 60 + 2 * 60)
	}
	commands += getRefreshCommands()
	commands += getReadConfigurationFromTheDeviceCommands()

	encapCommands(commands)
}

def parse(String description) {
	log.debug "parse() / description: ${description}"

	def result = null
	def cmd = zwave.parse(description)
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

def zwaveEvent(physicalgraph.zwave.commands.multichannelv3.MultiChannelCmdEncap cmd, ep = null) {
	log.debug "Multichannel command ${cmd}" + (ep ? " from endpoint $ep" : "")
	def encapsulatedCommand = cmd.encapsulatedCommand()
	zwaveEvent(encapsulatedCommand, cmd.sourceEndPoint as Integer)
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
	// Handles other Z-Wave commands that are not supported here
	log.debug "Command: ${cmd}"
	[:]
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd, ep = null) {
	log.debug "BasicReport: ${cmd}"
	dimmerEvents(cmd)
}

def zwaveEvent(physicalgraph.zwave.commands.switchmultilevelv3.SwitchMultilevelReport cmd, ep = null) {
	log.debug "SwitchMultilevelReport: ${cmd}"
	dimmerEvents(cmd)
}

def zwaveEvent(physicalgraph.zwave.commands.meterv3.MeterReport cmd, ep = null) {
	log.debug "MeterReport: ${cmd}"
	handleMeterReport(cmd)
}

def handleMeterReport(cmd) {
	if (cmd.meterType == 1) {
		if (cmd.scale == 0) {
			log.debug("createEvent energy")
			createEvent(name: "energy", value: cmd.scaledMeterValue, unit: "kWh")
		} else if (cmd.scale == 1) {
			log.debug("createEvent energy kVAh")
			createEvent(name: "energy", value: cmd.scaledMeterValue, unit: "kVAh")
		} else if (cmd.scale == 2) {
			log.debug("createEvent power")
			if (isDINDimmer()) {
				sendHubCommand(encap(zwave.meterV3.meterGet(scale: 0x00)))
			}
			createEvent(name: "power", value: Math.round(cmd.scaledMeterValue), unit: "W")
		}
	}
}

private dimmerEvents(physicalgraph.zwave.Command cmd, ep = null) {
	def cmdValue = cmd.value
	def value = (cmdValue ? "on" : "off")
	def result = [createEvent(name: "switch", value: value)]
	if (cmdValue && cmdValue <= 100) {
		result << createEvent(name: "level", value: cmdValue == 99 ? 100 : cmdValue)
	}

	return result
}

Integer adjustValueToRange(value){
	if(value == 0){
		return 0
	}
	def minDimmingLvlPref = settings.minimumDimmingValue ?: parameterMap.find({it.key == 'minimumDimmingValue'}).defaultValue
	return Math.max(value, minDimmingLvlPref)
}

def zwaveEvent(physicalgraph.zwave.commands.sensormultilevelv5.SensorMultilevelReport cmd, ep = null) {
	log.info "SensorMultilevelReport: ${cmd}, endpoint: ${ep}"
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
	log.debug "SensorMultilevelReport, ${map}, ${map.name}, ${map.value}, ${map.unit}"
	handleChildEvent(map)
	result << createEvent(map)
}

def handleChildEvent(map) {
	def childDni = "${device.deviceNetworkId}:" + 2
	log.debug "handleChildEvent / find child device: ${childDni}"
	def childDevice = childDevices.find { it.deviceNetworkId == childDni }

	if(!childDevice) {
		log.debug "handleChildEvent / creating a child device"
		childDevice = createChildDevice(
				"qubino",
				"Qubino Temperature Sensor",
				childDni,
				"Qubino Temperature Sensor"
		)
	}
	log.debug "handleChildEvent / sending event: ${map} to child: ${childDevice}"
	childDevice?.sendEvent(map)
}

def createChildDevice(childDthNamespace, childDthName, childDni, childComponentLabel) {
	try {
		log.debug "Creating a child device: ${childDthNamespace}, ${childDthName}, ${childDni}, ${childComponentLabel}"
		def childDevice = addChildDevice(childDthNamespace, childDthName, childDni, device.hub.id,
				[
					completedSetup: true,
					label: childComponentLabel,
					isComponent: false
				])
		log.debug "createChildDevice: ${childDevice}"
		childDevice
	} catch(Exception e) {
		log.debug "Exception: ${e}"
	}
}

def on() {
	def commands = [
		zwave.switchMultilevelV3.switchMultilevelSet(value: 0xFF, dimmingDuration: 0x00),
	]

	encapCommands(commands, 3000)
}

def off() {
	def commands = [
		zwave.switchMultilevelV3.switchMultilevelSet(value: 0x00, dimmingDuration: 0x00),
	]

	encapCommands(commands, 3000)
}

def setLevel(value, duration = null) {
	log.debug "setLevel >> value: $value, duration: $duration"
	def valueaux = value as Integer
	def level = Math.max(Math.min(valueaux, 99), 0)
	def getStatusDelay = 3000
	def dimmingDuration

	def commands = []

	if(duration == null) {
		dimmingDuration = 0
	} else {
		dimmingDuration = duration < 128 ? duration : 128 + Math.round(duration / 60)
		getStatusDelay = duration < 128 ? (duration * 1000) + 2000 : (Math.round(duration / 60) * 60 * 1000) + 2000
	}

	def adjustedLevel = adjustValueToRange(level)
	commands << zwave.switchMultilevelV3.switchMultilevelSet(value: adjustedLevel, dimmingDuration: dimmingDuration)

	encapCommands(commands, getStatusDelay)
}

/**
 * PING is used by Device-Watch in attempt to reach the Device
 * */
def ping() {
	refresh()
}

def refresh() {
	log.debug "refresh"
	refreshChild()
	encapCommands(getRefreshCommands())
}

def getRefreshCommands() {
	def commands = []

	commands << zwave.basicV1.basicGet()
	commands += getPowerMeterCommands()

	commands
}

def getPowerMeterCommands() {
	def commands = []

	if(supportsPowerMeter()) {
		commands << zwave.meterV2.meterGet(scale: 0)
		commands << zwave.meterV2.meterGet(scale: 2)
	}
	commands
}

private refreshChild() {
	// refresh a child temperature sensor (if available)
	if(childDevices){
		def childDni = "${device.deviceNetworkId}:2"
		def childDevice = childDevices.find { it.deviceNetworkId == childDni }

		if (childDevice != null) {
			sendHubCommand(encap(zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType: 1, scale: 0)))
		}
	}
}

private encapCommands(commands, delay=200) {
	if (commands.size() > 0) {
		delayBetween(commands.collect{ encap(it) }, delay)
	} else {
		[]
	}
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

private supportsMonoAndBiStableSwitchOnly() {
	return isDINDimmer() || isFlushDimmer()
}

private supportsPowerMeter() {
	return isDINDimmer() || isFlushDimmer()
}

private isFlushDimmer(){
	zwaveInfo.mfr.equals("0159") && zwaveInfo.model.equals("0051")
}

private isDINDimmer(){
	zwaveInfo.mfr.equals("0159") && zwaveInfo.model.equals("0052")
}

private isFlushDimmer010V(){
	zwaveInfo.mfr.equals("0159") && zwaveInfo.model.equals("0053")
}

private getParameterMap() {[
	[
		name: "Input 1 switch type", key: "input1SwitchType", type: "enum",
		parameterNumber: 1, size: 1, defaultValue: 0,
		values: [
			0: "Default value - Mono-stable switch type (push button) – button quick press turns between previous set dimmer value and zero)",
			1: "Bi-stable switch type (on/off toggle switch)",
			2: "Potentiometer (applies to Flush Dimmer 0-10V only, dimmer is using set value the last received from potentiometer or from z-wave controller)"
		],
		description: "Set input based on device type (mono-stable switch, bi-stable switch, potentiometer)."
	],
	[
			name: "Input 2 switch type (applies to Qubino Flush Dimmer only)", key: "inputsSwitchTypes", type: "enum",
			parameterNumber: 2, size: 1, defaultValue: 0,
			values: [
					0: "Default value - Mono-stable switch type (push button) – button quick press turns between previous set dimmer value and zero)",
					1: "Bi-stable switch type (on/off toggle switch)"
			],
			description: "Select between push-button (momentary) and on/off toggle switch types. Both inputs must work the same way."
	],
	[
			name: "Enable/Disable the 3-way switch/additional switch (applies to Qubino Flush Dimmer only)", key: "enable/DisableAdditionalSwitch", type: "enum",
			parameterNumber: 20, size: 1, defaultValue: 0,
			values: [
					0: "Default value - single push-button (connected to l1)",
					1: "3-way switch (connected to l1 and l2)",
					2: "additional switch (connected to l2)",
			],
			description: "Dimming is done by using a push-button or a switch, connected to l1 (by default). If the 3-way switch option is set, dimming can be controlled by a push-button or a switch connected to l1 and l2."
	],
	[
		name: "Enable/Disable Double click function", key: "enable/DisableDoubleClickFunction", type: "boolean",
		parameterNumber: 21, size: 1, defaultValue: 0,
		optionInactive: 0, inactiveDescription: "Default value - Double click disabled",
		optionActive: 1, activeDescription: "Double click enabled",
		description: "If enabled, a fast double-click on the push button will set the dimming level to its max. " +
			"Valid only if input is set as mono-stable (push button)."
	],
	[
		name: "Saving the state of the device after a power failure", key: "savingTheStateOfTheDeviceAfterAPowerFailure", type: "boolean",
		parameterNumber: 30, size: 1, defaultValue: 0,
		optionInactive: 0, inactiveDescription: "Default value - dimmer module saves its state before power failure (it returns to the last position saved before a power failure)",
		optionActive: 1, activeDescription: " Flush Dimmer 0-10V module does not save the state after a power failure, it returns to off position",
		description: "Set whether the device stores or does not store the last output level in the event of a power outage."
	],
	[
		name           : "Minimum dimming value",
		key            : "minimumDimmingValue",
		type           : "range",
		parameterNumber: 60,
		size           : 1,
		defaultValue   : 1,
		range          : "1..98",
		description    : "Select minimum dimming value for this device. When the switch type is selected as Bi-stable, it is not possible to dim the value between min and max."
	],
	[
		name: "Dimming time (soft on/off)", key: "dimmingTime(SoftOn/Off)", type: "range",
		parameterNumber: 65, size: 2, defaultValue: 100,
		range: "50..255",
		description: "The time it takes for the dimmer to transition between min and max brightness after a short press of the button or when controlled through the UI" +
			"100 (Default value) = 1s, " +
			"50 - 255 = 500 - 2550 milliseconds (2,55s), step is 10 milliseconds"
	],
	[
		name: "Dimming time when key pressed", key: "dimmingTimeWhenKeyPressed", type: "range",
		parameterNumber: 66, size: 2, defaultValue: 3,
			range: "1..255",
			description: "The time it takes for the dimmer to transition between min and max brightness when push button I1 or other associated device is held continuously" +
				"3 seconds (Default value), " +
				"1 - 255 seconds"
	],
	[
		name: "Dimming duration", key: "dimmingDuration", type: "range",
		parameterNumber: 68, size: 1, defaultValue: 0,
		range: "0..127",
		description: "The Duration field MUST specify the time that the transition should take from the current value to the new target value. " +
			"A supporting device SHOULD respect the specified Duration value. " +
			"0 (Default value) - dimming duration according to parameter: 'Dimming time when key pressed'," +
			"1 to 127 seconds"
	]
]}