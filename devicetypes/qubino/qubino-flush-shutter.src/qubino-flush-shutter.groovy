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

metadata {
	definition (name: "Qubino Flush Shutter", namespace: "qubino", author: "SmartThings", ocfDeviceType: "oic.d.blind", mcdSync: true) {
		capability "Window Shade"
		capability "Window Shade Level"
		capability "Power Meter"
		capability "Energy Meter"
		capability "Refresh"
		capability "Health Check"
		capability "Configuration"

		//zw:L type:1107 mfr:0159 prod:0003 model:0052 ver:1.01 zwv:4.05 lib:03 cc:5E,86,72,5A,73,20,27,25,26,32,60,85,8E,59,70 ccOut:20,26 epc:2
		fingerprint mfr: "0159", prod: "0003", model: "0052", deviceJoinName: "Qubino Window Treatment" // Qubino Flush Shutter (110-230 VAC)
		//zw:L type:1107 mfr:0159 prod:0003 model:0053 ver:1.01 zwv:4.05 lib:03 cc:5E,86,72,5A,73,20,27,25,26,32,85,8E,59,70 ccOut:20,26
		fingerprint mfr: "0159", prod: "0003", model: "0053", deviceJoinName: "Qubino Window Treatment" // Qubino Flush Shutter DC
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
		valueTile("shadeLevel", "device.level", width: 4, height: 1) {
			state "shadeLevel", label: 'Shade is ${currentValue}% up', defaultState: true
		}
		controlTile("levelSliderControl", "device.level", "slider", width:2, height: 1, inactiveLabel: false) {
			state "shadeLevel", action:"switch level.setLevel"
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

		main "windowShade"
		details(["windowShade", "shadeLevel", "levelSliderControl", "power", "energy", "refresh"])
	}

	preferences {
		parameterMap.each {
			input (title: it.name, description: it.description, type: "paragraph", element: "paragraph")

			switch (it.type) {
				case "enum":
					input(name: it.key, title: "Select", type: "enum", options: it.values, defaultValue: it.defaultValue, required: false)
					break
				case "range":
					input(name: it.key, type: "number", title: "Set value (range ${it.range})", defaultValue: it.defaultValue, range: it.range, required: false)
					break
			}
		}
	}
}

def installed() {
	state.currentMode = null
	state.childDevices = [:]
	state.venetianBlindDni = null
	state.temperatureSensorDni = null
	sendHubCommand(encap(zwave.configurationV2.configurationGet(parameterNumber: 71)))
	sendEvent(name: "checkInterval", value: 2 * 60 * 60, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])
	// Preferences template begin
	state.currentPreferencesState = [:]
	parameterMap.each {
		state.currentPreferencesState."$it.key" = [:]
		state.currentPreferencesState."$it.key".value = getPreferenceValue(it)
		def preferenceName = it.key + "Boolean"
		settings."$preferenceName" = true
		state.currentPreferencesState."$it.key".status = "synced"
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
		try {
			if (state.currentPreferencesState."$it.key".status == "syncPending") {
				commands += encap(zwave.configurationV2.configurationSet(scaledConfigurationValue: getCommandValue(it), parameterNumber: it.parameterNumber, size: it.size))
				commands += encap(zwave.configurationV2.configurationGet(parameterNumber: it.parameterNumber))
			} else if (state.currentPreferencesState."$it.key".status == "disablePending") {
				commands += encap(zwave.configurationV2.configurationSet(scaledConfigurationValue: it.disableValue, parameterNumber: it.parameterNumber, size: it.size))
				commands += encap(zwave.configurationV2.configurationGet(parameterNumber: it.parameterNumber))
			}
		} catch (e) {
			log.warn "There's been an issue with preference: ${it.key}"
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
		default:
			return integerValue
	}
}

private getCommandValue(preference) {
	def parameterKey = preference.key
	switch (preference.type) {
		case "range":
			return settings."$parameterKey"
		default:
			return Integer.parseInt(settings."$parameterKey")
	}
}

private isPreferenceChanged(preference) {
	if (settings."$preference.key" != null) {
		def value = state.currentPreferencesState."$preference.key"
		return state.currentPreferencesState."$preference.key".value != settings."$preference.key"
	} else {
		return false
	}
}

def handleConfigurationChange(confgurationReport) {
	switch (confgurationReport.parameterNumber) {
		case 71: //Operating mode
			switch (confgurationReport.scaledConfigurationValue) {
				case 0: // Shutter
					checkAndTriggerModeChange("windowShade")
					break
				case 1: // Venetian
					checkAndTriggerModeChange("windowShadeVenetian")
					break
			}
			log.info "Current device's mode is: ${state.currentMode}"
			break
		case 72:
			state.timeOfVenetianMovement = confgurationReport.scaledConfigurationValue
			break
		default:
			log.info "Parameter no. ${confgurationReport.parameterNumber} has no specific handler"
			break
	}
}

private checkAndTriggerModeChange(reportedMode) {
	if (state.currentMode != reportedMode) {
		state.currentMode = reportedMode
		createVenetianBlindsChildDeviceIfNeeded()
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

def multilevelChildInstalled(childDni) {
	state.timeOfVenetianMovement = 150
	sendHubCommand(encap(zwave.switchMultilevelV3.switchMultilevelGet(), 2))
}

def close() {
	setShadeLevel(0x64)
}

def open() {
	setShadeLevel(0x00)
}

def pause() {
	def currentShadeState = device.currentState("windowShade").value
	if (currentShadeState == "opening" || currentShadeState == "closing") {
		encap(zwave.switchMultilevelV3.switchMultilevelStopLevelChange())
	} else {
		encap(zwave.switchMultilevelV3.switchMultilevelGet())
	}
}

def setLevelChild(level, childDni) {
	setSlats(level)
}

def setLevel(level) {
	setShadeLevel(level)
}

def setShadeLevel(level) {
	log.debug "Setting shade level: ${level}"
	encap(zwave.switchMultilevelV3.switchMultilevelSet(value: Math.min(0x63, level)))
}

def setSlats(level) {
	def time = (int) (state.timeOfVenetianMovement  * 1.1)
	sendHubCommand([
			encap(zwave.switchMultilevelV3.switchMultilevelSet(value: Math.min(0x63, level)), 2),
			"delay ${time}",
			encap(zwave.switchMultilevelV3.switchMultilevelGet(), 2)
	])
}

def refresh() {
	[
			encap(zwave.switchMultilevelV3.switchMultilevelGet()),
			encap(zwave.meterV3.meterGet(scale: 0x00)),
	]
}

def ping() {
	response(refresh())
}

def configure() {
	def configurationCommands = []
	configurationCommands += encap(zwave.associationV1.associationSet(groupingIdentifier: 7, nodeId: [zwaveHubNodeId]))
	configurationCommands += encap(zwave.meterV3.meterGet(scale: 0x00))
	configurationCommands += encap(zwave.meterV3.meterGet(scale: 0x02))
	configurationCommands += encap(zwave.switchMultilevelV3.switchMultilevelGet())
	configurationCommands += encap(zwave.configurationV2.configurationSet(scaledConfigurationValue: 1, parameterNumber: 40, size: 1))

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

def zwaveEvent(physicalgraph.zwave.commands.switchmultilevelv3.SwitchMultilevelReport cmd, ep = null) {
	log.debug "SwitchMultilevelReport ${cmd} from endpoint: ${ep}"
	if (cmd.value != 0xFE) {
		if (ep != 2) {
			shadeEvent(cmd.value)
		} else {
			def event = [name: "level", value: cmd.value != 0x63 ? cmd.value : 100]
			sendEventsToVenetianBlind([event])
		}
	} else {
		log.warn "Something went wrong with calibration, position of blind is unknown"
		if (ep == 2) {
			sendEventsToVenetianBlind([[name: "level", value: 0]])
		} else {
			[
					createEvent([name: "windowShade", value: "unknown"]),
					createEvent([name: "shadeLevel", value: 0])
			]
		}
	}
}

def zwaveEvent(physicalgraph.zwave.commands.switchmultilevelv3.SwitchMultilevelSet cmd, ep = null) {
	def currentLevel = Integer.parseInt(device.currentState("shadeLevel").value)
	state.blindsLastCommand = currentLevel > cmd.value ? "opening" : "closing"
	state.shadeTarget = cmd.value
	sendHubCommand(encap(zwave.meterV3.meterGet(scale: 0x02)))
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd, ep = null) {
	log.debug "BasicReport ${cmd}"
	if (cmd.value != 0xFE && ep != 2) {
		shadeEvent(cmd.value)
	} else {
		log.warn "Something went wrong with calibration, position of blind is unknown"
	}
}

private shadeEvent(value) {
	def shadeValue
	def events = []
	if (!value) {
		shadeValue = "open"
	} else if (value == 0x63) {
		shadeValue = "closed"
	} else {
		shadeValue = "partially open"
	}
	events += createEvent([name: "windowShade", value: shadeValue])
	events += createEvent([name: "shadeLevel", value: value != 0x63 ? value : 100])

	events
}

def zwaveEvent(physicalgraph.zwave.commands.meterv3.MeterReport cmd, ep = null) {
	def events = []
	if (cmd.meterType == 0x01) {
		def eventMap = [:]
		if (cmd.scale == 0x00) {
			eventMap.name = "energy"
			eventMap.value = cmd.scaledMeterValue
			eventMap.unit = "kWh"
			events += createEvent(eventMap)
		} else if (cmd.scale == 0x02) {
			eventMap.name = "power"
			eventMap.value = Math.round(cmd.scaledMeterValue)
			eventMap.unit = "W"
			events += createEvent(eventMap)
			if (Math.round(cmd.scaledMeterValue)) {
				events += createEvent([name: "windowShade", value: state.blindsLastCommand])
				events += createEvent([name: "shadeLevel", value: state.shadeTarget, displayed: false])
			} else {
				events += response(encap(zwave.switchMultilevelV3.switchMultilevelGet()))
			}
		}
	}
	events
}

def zwaveEvent(physicalgraph.zwave.commands.sensormultilevelv5.SensorMultilevelReport cmd, ep = null) {
	log.debug "SensorMultilevelReport ${cmd}" + (ep ? " from endpoint $ep" : "")
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
	def child = childDevices.find { it.deviceNetworkId == state.temperatureSensorDni }
	if (!child) {
		child = createChildDevice("qubinoTemperatureSensor", "Qubino Temperature Sensor", "Qubino Temperature Sensor", 3, "qubino", false)
		state.temperatureSensorDni = child.deviceNetworkId
	}
	child?.sendEvent(map)
	createEvent(map)
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

private sendEventsToVenetianBlind(events) {
	if (state.venetianBlindDni) {
		def child = childDevices.find { it.deviceNetworkId == state.venetianBlindDni }
		events.each {
			child.sendEvent(it)
		}
		createEvent(descriptionText: "Venetian Blinds level has been updated")
	} else {
		log.warn "There's no venetian child device to send events to"
	}
}

private createChildDevice(componentName, componentLabel, dthName, childIt, namespace = "smartthings", isComponent = true) {
	try {
		def childDni = "${device.deviceNetworkId}:$childIt"
		def child = addChildDevice(namespace, dthName, childDni, device.getHub().getId(), [
				completedSetup: true,
				label         : componentLabel,
				isComponent   : isComponent,
				componentName : componentName,
				componentLabel: componentLabel
		])
		return child
	} catch(Exception e) {
		log.debug "Exception: ${e}"
	}
}

private createVenetianBlindsChildDeviceIfNeeded() {
	if (state.currentMode.contains("Venetian")) {
		state.venetianBlindDni = createChildDevice("venetianBlind", "Venetian Blind", "Child Switch Multilevel", 2).deviceNetworkId
	}
}

private getParameterMap() {[
		[
				name: "Operating modes", key: "operatingModes", type: "enum",
				parameterNumber: 71, size: 1, defaultValue: 0,
				values: [
						0: "Shutter mode",
						1: "Venetian mode (up/down and slate rotation)"
				],
				description: "Set the device's operating mode."
		],
		[
				name: "Slats tilting full turn time", key: "slatsTiltingFullTurnTime", type: "range",
				parameterNumber: 72, size: 2, defaultValue: 150,
				range: "0..32767",
				description: "Specify the time required to rotate the slats 180 degrees. (100 = 1 second)"
		],
		[
				name: "Slats position", key: "slatsPosition", type: "enum",
				parameterNumber: 73, size: 1, defaultValue: 1,
				values: [
						0: "Slats return to previously set position only in case of Z-wave control (not valid for limit switch positions)",
						1: "Slats return to previously set position in case of Z-wave control, push-button operation or when the lower limit switch is reached"
				],
				description: "This parameter defines slats position after up/down movement through Z-wave or push-buttons."
		],
		[
				name: "Motor moving up/down time", key: "motorMovingUp/DownTime", type: "range",
				parameterNumber: 74, size: 2, defaultValue: 0,
				range: "0..32767",
				description: "Set the amount of time it takes to completely open or close shutter. Check manual for more detailed guidance."
		],
		[
				name: "Motor operation detection", key: "motorOperationDetection", type: "range",
				parameterNumber: 76, size: 1, defaultValue: 30,
				range: "0..127",
				description: "Power usage threshold which will be interpreted as motor reaching the limit switch."
		],
		[
				name: "Forced Shutter calibration", key: "forcedShutterCalibration", type: "enum",
				parameterNumber: 78, size: 1, defaultValue: 0,
				values: [
						0: "0: Calibration finished or not started",
						1: "1: Start calibration process"
				],
				description: "By modifying the parameters setting from 0 to 1 a Shutter enters the calibration mode. When calibration process is finished, completing full cycle - up, down and up, set this parameter value back to 0."
		]
]}