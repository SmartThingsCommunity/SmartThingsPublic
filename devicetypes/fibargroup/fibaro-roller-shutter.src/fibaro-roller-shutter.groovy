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
	definition (name: "Fibaro Roller Shutter", namespace: "fibargroup", author: "SmartThings", ocfDeviceType: "oic.d.blind", mcdSync: true) {
		capability "Power Meter"
		capability "Energy Meter"
		capability "Refresh"
		capability "Health Check"
		capability "Configuration"

		fingerprint mfr: "010F", prod: "0303", model: "1000", deviceJoinName: "Fibaro Window Treatment"
	}

	tiles(scale: 2) {
		valueTile("power", "device.power", decoration: "flat", width: 2, height: 2) {
			state "default", label:'${currentValue} W'
		}
		valueTile("energy", "device.energy", decoration: "flat", width: 2, height: 2) {
			state "default", label:'${currentValue} kWh'
		}
		standardTile("refresh", "device.power", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "default", label:'', action:"refresh.refresh", icon:"st.secondary.refresh"
		}
		
		details(["power", "energy", "refresh"])
	}

	preferences {
		parameterMap.each {
			input (title: it.name, description: it.description, type: "paragraph", element: "paragraph")

			switch(it.type) {
				case "boolRange":
					input(name: it.key + "Boolean", type: "bool", title: "Enable",
							description: "If you disable this option, it will overwrite setting below.",
							defaultValue: it.defaultValue != it.disableValue, required: false
					)
					input(name: it.key, type: "number", title: "Set value (range ${it.range})", defaultValue: it.defaultValue,
							range: it.range, required: false
					)
				break
				case "boolean":
					input(type: "paragraph", element: "paragraph", description: "Option enabled: ${it.activeDescription}\n" +
							"Option disablePending: ${it.inactiveDescription}"
					)
					input(name: it.key, type: "boolean", title: "Enable", defaultValue: it.defaultValue == it.activeOption, required: false)
				break
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
	initializeOnce()
}

def updated() {
	// Preferences template begin
	parameterMap.each {
		if (isPreferenceChanged(it)) {
			log.debug "Preference ${it.key} has been updated from value: ${getPreferenceValue(it.key)} to ${settings."$it.key"}"
			setPreferenceStatus(it.key, "syncPending")
			if (it.type == "boolRange") {
				def preferenceName = it.key + "Boolean"
				if (settings."$preferenceName" != null) {
					if (!settings."$preferenceName") {
						setPreferenceStatus(it.key, "disablePending")
					} else if (state.currentPreferencesState."$it.key".status == "disabled") {
						setPreferenceStatus(it.key, "syncPending")
					}
				} else {
					setPreferenceStatus(it.key, "syncPending")
				}
			}
		} else if (!getPreferenceValue(it.key)) {
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
			if (getPreferenceStatus(it.key) == "syncPending") {
				commands += encap(zwave.configurationV2.configurationSet(scaledConfigurationValue: getCommandValue(it), parameterNumber: it.parameterNumber, size: it.size))
				commands += encap(zwave.configurationV2.configurationGet(parameterNumber: it.parameterNumber))
			} else if (getPreferenceStatus(it.key) == "disablePending") {
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
	def preferenceValue = calculatePreferenceValue(preference, cmd.scaledConfigurationValue)
	if (settings."$key" == preferenceValue) {
		setPreferenceValue(key, settings."$key")
		setPreferenceStatus(key, "synced")
		handleConfigurationChange(cmd)
	} else if (preference.type == "boolRange") {
		if (getPreferenceStatus(key) == "disablePending" && preferenceValue == preference.disableValue) {
			setPreferenceStatus(key, "disabled")
		} else {
			runIn(5, "syncConfiguration", [overwrite: true])
		}
	} else {
		setPreferenceStatus(key, "syncPending")
		runIn(5, "syncConfiguration", [overwrite: true])
	}
	// Preferences template end
	handleConfigurationChange(cmd)
}

private calculatePreferenceValue(preference, value = "default") {
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
			return settings."$parameterKeyBoolean" == null || settings."$parameterKeyBoolean" ? settings."$parameterKey" : preference.disableValue
		case "range":
			return settings."$parameterKey"
		default:
			return Integer.parseInt(settings."$parameterKey")
	}
}

private isPreferenceChanged(preference) {
	if (settings."$preference.key" != null) {
		switch (preference.type) {
			case "boolRange":
				def boolName = preference.key + "Boolean"
				if (getPreferenceStatus(preference.key) == "disabled") {
					return settings."$boolName"
				} else {
					return getPreferenceValue(preference.key) != settings."$preference.key" || !settings."$boolName"
				}
			default:
				return getPreferenceValue(preference.key) != settings."$preference.key"
		}
	} else {
		return false
	}
}

def handleConfigurationChange(confgurationReport) {
	switch (confgurationReport.parameterNumber) {
		case 150: // Calibrating
			switch(confgurationReport.scaledConfigurationValue) {
				case 0: // "Device is not calibrated"
					setState("calibrationStatus", "notStarted")
				break
				case 1: // "Device is calibrated"
					setState("calibrationStatus", "done")
				break
				case 2: // "Force Calibration"
					setState("calibrationStatus", "pending")
				break
			}
			log.info "Calibration ${getState("calibrationStatus")}"
		break
		case 151: //Operating mode
			switch(confgurationReport.scaledConfigurationValue) {
				case 1: // "Roller blind (with positioning)"
				case 5: // "Roller blind with built-in driver"
				case 6: // "Roller blind with built-in driver (impulse)"
					checkAndTriggerModeChange("windowShade")
				break
				case 2: // "Venetian blind (with positioning)"
					checkAndTriggerModeChange("windowShadeVenetian")
				break
				case 3: // "Gate (without positioning)"
					checkAndTriggerModeChange("garageDoor")
				break
				case 4: // "Gate (with positioning)"
					checkAndTriggerModeChange("garageDoorPositioning")
				break
			}
			log.info "Current device's mode is: ${getState("currentMode")}"
		break
		case 152: // Venetian Blinds - time of full turn of the slats
			state.timeOfVenetianMovement = confgurationReport.scaledConfigurationValue
		break
		default:
			log.info "Parameter no. ${confgurationReport.parameterNumber} has no specific handler"
		break
	}
}

private checkAndTriggerModeChange(reportedMode) {
	if (getState("currentMode") != reportedMode) {
		deleteChildDevicesIfNeeded(reportedMode)
		setState("currentMode", reportedMode)
		createMainChildDeviceIfNeeded()
		createAssistantChildDeviceIfNeeded()
		setDeviceType("Type Switch")
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

def windowShadeInstalled(childDni) {
	def event = [name: "supportedWindowShadeCommands", value: ["open", "close", "pause"]]
	sendEventsToMainChildDevice([event])
	sendHubCommand([
		encap(zwave.switchMultilevelV3.switchMultilevelGet())
	])
}

def garageDoorInstalled(childDni) {
	def event = [name: "door", value: "unknown"]
	sendEventsToMainChildDevice([event])
}

def multilevelChildInstalled(childDni) {
	if (getState("currentMode").contains("Venetian")) {
		state.timeOfVenetianMovement = 150
		sendHubCommand(encap(zwave.switchMultilevelV3.switchMultilevelGet(), 2))
	} else {
		sendHubCommand(encap(zwave.switchMultilevelV3.switchMultilevelGet()))
	}
}

def closeChild(childDni) {
	setShadeLevel(0x64)
}

def openChild(childDni) {
	setShadeLevel(0x00)
}

def pauseChild(childDni) {
	sendHubCommand(encap(zwave.switchMultilevelV3.switchMultilevelStopLevelChange()))
}

def setLevelChild(level, childDni, currentLevel = null) {
	if (getState("currentMode").contains("window") && childDni == getState("childDeviceAssistant")) {
		setSlats(level)
	} else {
		state.blindsLastCommand = currentLevel > level ? "opening" : "closing"
		setShadeLevel(level)
	}
}

def setShadeLevel(level) {
	log.debug "Setting shade level: ${level}"
	state.isManualCommand = false
	state.shadeTarget = level
	sendHubCommand(encap(zwave.switchMultilevelV3.switchMultilevelSet(value: Math.min(0x63, level)), 1))
}

def setSlats(level) {
	state.isManualCommand = false
	def time = (int) (state.timeOfVenetianMovement  * 1.1)
	sendHubCommand([
		encap(zwave.switchMultilevelV3.switchMultilevelSet(value: Math.min(0x63, level)), 2),
		"delay ${time}",
		encap(zwave.switchMultilevelV3.switchMultilevelGet(), 2)
	])
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
	configurationCommands += encap(zwave.meterV3.meterGet(scale: 0x00))
	configurationCommands += encap(zwave.meterV3.meterGet(scale: 0x01))

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
	if (cmd.value != 0xFE) {
		if (ep != 2) {
			shadeEvent(cmd.value)
		} else {
			def event = [name: "level", value: cmd.value != 0x63 ? cmd.value : 100]
            sendEventsToAssistantChildDevice([event])
		}
	} else {
		log.warn "Something went wrong with calibration, position of blind is unknown"
		if (ep == 2) {
			sendEventsToAssistantChildDevice([[name: "level", value: 0]])
		} else {
			sendEventsToMainChildDevice([
				[name: mainEventName, value: "unknown"],
				[name: "level", value: 0],
				[name: "shadeLevel", value: 0]
			])
		}
	}
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd, ep = null) {
	if (cmd.value != 0xFE && ep != 2) {
		shadeEvent(cmd.value)
	} else {
		log.warn "Something went wrong with calibration, position of blind is unknown"
	}
}

private shadeEvent(value) {
	def shadeValue
	def toSend = []
	if (!value) {
		shadeValue = "open"
	} else if (value == 0x63) {
		shadeValue = "closed"
	} else {
		shadeValue = isModeWithPositioning() ? "partially open" : "open"
	}
	toSend += [name: mainEventName, value: shadeValue]
	if (isModeWithPositioning) {
		def levelEvent = [name: "level", value: value != 0x63 ? value : 100]
		if (getState("currentMode").contains("garage")) {
			sendEventsToAssistanChildDevice([levelEvent])
		} else {
			toSend += levelEvent
			toSend += [name: "shadeLevel", value: value != 0x63 ? value : 100]
		}
	}
	sendEventsToMainChildDevice(toSend)
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
				additionalShadeEvent.name = mainEventName
				additionalShadeEvent.value = state.blindsLastCommand
				sendEventsToMainChildDevice([additionalShadeEvent])
				if (!state.isManualCommand)
					def levelEvent = [name: "level", value: state.shadeTarget]
					def shadeLevelEvents = [[name: "level", value: state.shadeTarget], [name: "shadeLevel", value: state.shadeTarget]]
					getState("currentMode").contains("windowShade") ? sendEventsToMainChildDevice(shadeLevelEvents) : sendEventsToAssistantChildDevice([levelEvent])
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

private sendEventsToMainChildDevice(events) {
	if (getState("childDeviceMain")) {
		def child = childDevices.find { it.deviceNetworkId == getState("childDeviceMain") }
		events.each {
			child.sendEvent(it)
		}
	} else {
		log.warn "There's no main child device to send events to"
	}
}

private sendEventsToAssistantChildDevice(events) {
	if (getState("childDeviceAssistant")) {
		def child = childDevices.find { it.deviceNetworkId == getState("childDeviceAssistant") }
		events.each {
			child.sendEvent(it)
		}
	} else {
		log.warn "There's no assistant child device to send events to"
	}
}

private getMainEventName() {
	if (getState("currentMode").contains("windowShade")) {
		return "windowShade"
	} else if (getState("currentMode").contains("garageDoor")) {
		return "door"
	}
}

private isModeWithPositioning() {
	return getState("currentMode") != "garageDoor"
}

private createChildDevice(componentName, componentLabel, dthName, childIt) {
	try {
		def childDni = "${device.deviceNetworkId}:$childIt"
		def child = addChildDevice("smartthings", dthName, childDni, device.getHub().getId(), [
			completedSetup: true,
			label         : componentLabel,
			isComponent   : true,
			componentName : componentName,
			componentLabel: componentLabel
		])
	} catch(Exception e) {
		log.debug "Exception: ${e}"
	}
}

private createMainChildDeviceIfNeeded() {
	if (getState("currentMode").contains("windowShade") && !getState("childDeviceMain")) {
		setState("childDeviceMain", "${device.deviceNetworkId}:3")
		createChildDevice("windowShade", "Window Shade", "Child Window Shade", 3)
	} else if (getState("currentMode").contains("garageDoor") && !getState("childDeviceMain")) {
		setState("childDeviceMain", "${device.deviceNetworkId}:3")
		createChildDevice("garageDoor", "Garage Door", "Child Garage Door", 3)
	}
}

private createAssistantChildDeviceIfNeeded() {
	if (getState("currentMode").contains("Venetian")) {
		setState("childDeviceAssistant", "${device.deviceNetworkId}:4")
		createChildDevice("venetianBlind", "Venetian Blind", "Child Switch Multilevel", 4)
	} else if (getState("currentMode").contains("Positioning")) {
		setState("childDeviceAssistant", "${device.deviceNetworkId}:4")
		createChildDevice("garageDoorPosition", "Garage Door Position", "Child Switch Multilevel", 4)
	}
}

private deleteChildDevicesIfNeeded(reportedMode) {
	if (getState("currentMode").contains("window") && reportedMode.contains("window")) {
		if (getState("currentMode").contains("Venetian")) {
			getState("childDeviceAssistant")
			deleteChildDevice(getState("childDeviceAssistant"))
			setState("childDeviceAssistant", null)
		}
	} else if (getState("currentMode").contains("garage") && reportedMode.contains("garage")) {
		if (getState("currentMode").contains("Positioning")) {
			deleteChildDevice(getState("childDeviceAssistant"))
			setState("childDeviceAssistant", null)
		}
	} else {
		childDevices.each {
			if (it.deviceNetworkId == "${device.deviceNetworkId}:1" || it.deviceNetworkId == "${device.deviceNetworkId}:2")
				return

			deleteChildDevice(it.deviceNetworkId)
			setState("childDeviceMain", null)
			setState("childDeviceAssistant", null)
		}
	}
}

private initializeOnce() {
	if (!childDevices) {
		createChildDevice("states", "States Collection", "States Collection", 1)
		createChildDevice("preferences", "Preferences Collection", "Preferences Collection", 2)
		setState("deviceTypeName", "Fibaro Roller Shutter")
		setState("calibrationStatus", "notStarted")
		setState("childDeviceMain", null)
		setState("childDeviceAssistant", null)
		setState("currentMode", "windowShade")
		createMainChildDeviceIfNeeded()
		createAssistantChildDeviceIfNeeded()
		sendEvent(name: "checkInterval", value: 2 * 60 * 60, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])
		// Preferences template begin
		state.currentPreferencesState = [:]
		parameterMap.each {
			allocatePreference(it.key)
			setPreferenceValue(it.key, calculatePreferenceValue(it))
			if (it.type == "boolRange" && calculatePreferenceValue(it) == it.disableValue) {
				setPreferenceStatus(it.key, "disablePending")
			} else {
				def preferenceName = it.key + "Boolean"
				settings."$preferenceName" = true
				setPreferenceStatus(it.key, "synced")
			}
		}
		// Preferences template end
	}
}

private getStatesChild() {
	String childDni = "${device.deviceNetworkId}:1"
	return childDevices.find { it.deviceNetworkId == childDni }
}

private getPreferencesChild() {
	String childDni = "${device.deviceNetworkId}:2"
	return childDevices.find { it.deviceNetworkId == childDni }
}

private void setState(key, value) {
	def child = getStatesChild()
	child?.setState(key, value)
}

private getState(key) {
	def child = getStatesChild()
	child?.getState(key)
}

private void allocatePreference(key) {
	def child = getPreferencesChild()
	child?.allocatePreference(key)
}

private void setPreferenceValue(key, value) {
	def child = getPreferencesChild()
	child?.setPreferenceValue(key, value)
}

private getPreferenceValue(key) {
	def child = getPreferencesChild()
	child?.getPreferenceValue(key)
}

private void setPreferenceStatus(key, value) {
	def child = getPreferencesChild()
	child?.setPreferenceStatus(key, value)
}

private getPreferenceStatus(key) {
	def child = getPreferencesChild()
	child?.getPreferenceStatus(key)
}

private getParameterMap() {[
	[
		name: "Force calibration", key: "forceCalibration", type: "enum",
		parameterNumber: 150, size: 1, defaultValue: 0,
		values: [
			0: "device is not calibrated",
			1: "device is calibrated",
			2: "force device calibration"			
		],
		description: "This setting allows triggering calibration process. After calibration process is done, please manually select \"device is calibrated\" option. Relevant only for modes with automatic positioning."
	],
	[
		name: "Operating mode", key: "operatingMode", type: "enum",
		parameterNumber: 151, size: 1, defaultValue: 1,
		values: [
			1: "Roller blind (with positioning)",
			2: "Venetian blind (with positioning)",
			3: "Gate (without positioning)",
			4: "Gate (with positioning)",
			5: "Roller blind with built-in driver",
			6: "Roller blind with built-in driver (impulse)"		
		],
		description: "This setting allows adjusting operation according to the connected device."
	],
	[
		name: "Venetian blind - time of full turn of the slats", key: "venetianBlind-TimeOfFullTurnOfTheSlats", type: "range",
		parameterNumber: 152, size: 4, defaultValue: 150,
		range: "0..90000", 
		description: "This setting determines time of full turn cycle of the slats. Relevant only for Venetian Blinds mode. [100 = 1s]"
	],
	[
		name: "Set slats back to previous position", key: "setSlatsBackToPreviousPosition", type: "enum",
		parameterNumber: 153, size: 1, defaultValue: 1,
		values: [
			0: "slats return to previously set position only in case of the main controller operation.",
			1: "slats return to previously set position in case of the main controller operation, momentary switch operation, or when the limit switch is reached.",
			2: "slats return to previously set position in case of the main controller operation, momentary switch operation, when the limit switch is reached or after receiving the Switch Multilevel Stop control frame"			
		],
		description: "For Venetian blinds the setting determines slats positioning in various situations. The setting is irrelevant for other modes.  NOTE: If parameter 20 is set to 1 (toggle switch), change value of parameter 153 to 0 for slats to work properly."
	],
	[
		name: "Delay motor stop after reaching end switch", key: "delayMotorStopAfterReachingEndSwitch", type: "range",
		parameterNumber: 154, size: 2, defaultValue: 10,
		range: "0..600", 
		description: "For blinds the setting determines the time after which the motor will be stopped after end switch contacts are closed [100 = 1s]. For gates the setting determines time after which the gate will start closing automatically if S2 contacts are opened (if set to 0, gate will not close)."
	],
	[
		name: "Motor operation detection", key: "motorOperationDetection", type: "boolRange",
		parameterNumber: 155, size: 2, defaultValue: 10,
		range: "1..255", disableValue: 0,
		description: "Power threshold to be interpreted as reaching a limit switch."
	],
	[
		name: "Time of up movement", key: "timeOfUpMovement", type: "range",
		parameterNumber: 156, size: 4, defaultValue: 6000,
		range: "1..90000", 
		description: "This setting determines the time needed for roller blinds to reach the top [100 = 1s]. Relevant for modes without automatic calibration."
	],
	[
		name: "Time of down movement", key: "timeOfDownMovement", type: "range",
		parameterNumber: 157, size: 4, defaultValue: 6000,
		range: "1..90000", 
		description: "This setting determines time needed for roller blinds to reach the bottom [100 = 1s]. Relevant for modes without automatic calibration."
	],
	[
		name: "Switch type", key: "switchType", type: "enum",
		parameterNumber: 20, size: 1, defaultValue: 2,
		values: [
			0: "momentary switches",
			1: "toggle switches",
			2: "single, momentary switch (the switch should be connected to S1 terminal)"			
		],
		description: "This parameter defines as what type the device should treat the switch connected to the S1 and S2 terminals. This parameter is not relevant in gate operating modes (parameter 151 set to 3 or 4). In this case switch always works as a momentary and has to be connected to S1 terminal."
	],
	[
		name: "Inputs orientation", key: "inputsOrientation", type: "boolean",
		parameterNumber: 24, size: 1, defaultValue: 0,
		optionInactive: 0, inactiveDescription: "default (S1 - 1st channel, S2 - 2nd channel)",
		optionActive: 1, activeDescription: "reversed (S1 - 2nd channel, S2 - 1st channel)",
		description: "This parameter allows reversing the operation of switches connected to S1 and S2 without changing the wiring."
	],
	[
		name: "Outputs orientation", key: "outputsOrientation", type: "boolean",
		parameterNumber: 25, size: 1, defaultValue: 0,
		optionInactive: 0, inactiveDescription: "default (Q1 - 1st channel, Q2 - 2nd channel)",
		optionActive: 1, activeDescription: "reversed (Q1 - 2nd channel, Q2 - 1st channel)",
		description: "This parameter allows reversing the operation of Q1 and Q2 without changing the wiring (in case of invalid motor connection) to ensure proper operation."
	],
	[
		name: "Measuring power consumed by the device itself", key: "measuringPowerConsumedByTheDeviceItself", type: "boolean",
		parameterNumber: 60, size: 1, defaultValue: 0,
		optionInactive: 0, inactiveDescription: "function inactive",
		optionActive: 1, activeDescription: "function active",
		description: "This setting determines whether the power metering should include the amount of active power consumed by the device itself."
	],
	[
		name: "Power reports - on change", key: "powerReports-OnChange", type: "boolRange",
		parameterNumber: 61, size: 2, defaultValue: 15,
		range: "1..500", disableValue: 0, 
		description: "This setting determines the minimum change in consumed power that will result in sending new power report to the main controller. For loads under 50W, the parameter is not relevant and reports are sent every 5W change. Power report are sent no often then every 30 seconds."
	],
	[
		name: "Power reports - periodic", key: "powerReports-Periodic", type: "boolRange",
		parameterNumber: 62, size: 2, defaultValue: 3600,
		range: "30..32400", disableValue: 0, 
		description: "This setting determines in what time intervals the periodic power reports are sent to the main controller. Periodic reports do not depend on power change."
	],
	[
		name: "Energy reports - on change", key: "energyReports-OnChange", type: "boolRange",
		parameterNumber: 65, size: 2, defaultValue: 10,
		range: "1..500", disableValue: 0, 
		description: "This setting determines the minimum change in consumed energy that will result in sending new energy report to the main controller."
	],
	[
		name: "Energy reports - periodic", key: "energyReports-Periodic", type: "boolRange",
		parameterNumber: 66, size: 2, defaultValue: 3600,
		range: "30..32400", disableValue: 0, 
		description: "This setting determines in what time intervals the periodic energy reports are sent to the main controller. Periodic reports do not depend on energy change."
	]
]}