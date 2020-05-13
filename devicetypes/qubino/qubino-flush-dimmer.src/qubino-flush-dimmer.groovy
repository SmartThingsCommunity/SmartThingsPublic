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
	definition (name: "Qubino Flush Dimmer", namespace: "qubino", author: "SmartThings", mnmn: "SmartThings", ocfDeviceType: "oic.d.switch", runLocally: false, executeCommandsLocally: false) {
		capability "Actuator"
		capability "Configuration"
		capability "Health Check"
		capability "Refresh"
		capability "Sensor"
		capability "Switch"
		capability "Switch Level"
		capability "Power Meter"
		capability "Energy Meter"

		// Qubino Flush Dimmer - ZMNHDD
		fingerprint mfr: "0159", prod: "0001", model: "0051", deviceJoinName: "Qubino Dimmer Switch", ocfDeviceType: "oic.d.smartplug"
	}

	tiles(scale: 2) {
		multiAttributeTile(name:"switch", type: "lighting", width: 6, height: 4, canChangeIcon: true) {
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

		standardTile("refresh", "device.switch", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "default", label:"", action:"refresh.refresh", icon:"st.secondary.refresh"
		}

		main(["switch"])
		details(["switch", "level", "power", "energy", "refresh"])

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
			commands += secure(zwave.configurationV2.configurationSet(scaledConfigurationValue: getCommandValue(it), parameterNumber: it.parameterNumber, size: it.size))
			commands += secure(zwave.configurationV2.configurationGet(parameterNumber: it.parameterNumber))
		} else if (state.currentPreferencesState."$it.key".status == "disablePending") {
			commands += secure(zwave.configurationV2.configurationSet(scaledConfigurationValue: it.disableValue, parameterNumber: it.parameterNumber, size: it.size))
			commands += secure(zwave.configurationV2.configurationGet(parameterNumber: it.parameterNumber))
		}
	}
	sendHubCommand(commands)
}

def getCommandClassVersions() {
	[
			0x20: 1, // Basic Set
			0x5A: 1, // Device Reset Locally
			0x73: 1, // Power Level
			0x98: 1, // Security
			0x86: 2, // Version
			0x72: 2, // Manufacturer Specific
			0x27: 1, // Switch All
			0x25: 1, // Switch Binary
			0x26: 3, // Switch Multilevel
			0x32: 3, // Meter
			0x71: 1, // Alarm
			0x85: 2, // Association
			0x8E: 2, // Multi Channel Association
			0x59: 2, // Association Group
			0x70: 1, // Configuration
			0x60: 3, // Multi Instance
			0x31: 5, // Sensor MultiLevel
			0x30: 2, // Sensor Binary
			0x71: 3, // Notification
			0xEF: 1, // Mark
	]
}

def configure() {
	def commands = []
	commands << zwave.associationV1.associationSet(groupingIdentifier:1, nodeId:[zwaveHubNodeId]).format()
	commands << zwave.associationV1.associationSet(groupingIdentifier:2, nodeId:[zwaveHubNodeId]).format()
	commands << zwave.associationV1.associationSet(groupingIdentifier:3, nodeId:[zwaveHubNodeId]).format()
	commands << zwave.associationV1.associationSet(groupingIdentifier:4, nodeId:[zwaveHubNodeId]).format()
	delayBetween(commands, 500)
}

def parse(String description) {
	log.debug "parse() description: ${description}"
	def result = null
	def cmd = zwave.parse(description, commandClassVersions)
	if (cmd) {
		result = zwaveEvent(cmd)
		log.debug("'$description' parsed to $result")
	} else {
		log.debug("Couldn't zwave.parse '$description'")
	}
	return result
}

def zwaveEvent(physicalgraph.zwave.commands.configurationv1.ConfigurationReport cmd) {
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

def zwaveEvent(physicalgraph.zwave.Command cmd, ep = null) {
	// Handles all Z-Wave commands we aren't interested in
	log.debug("zwaveEvent other commands: ${cmd}")
	[:]
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
	/*
	This cmd value reports inaccurate values which often differ from the values reported by SwitchMultilevelSet by 1-3%.
	Probbably we should only depend on SwitchMultilevelSet report.
	*/
	log.debug "SwitchMultilevelReport: ${cmd}"
	//dimmerEvents(cmd)
}

def zwaveEvent(physicalgraph.zwave.commands.switchmultilevelv3.SwitchMultilevelSet cmd, ep = null) {
	log.debug "SwitchMultilevelSet: ${cmd}"
	dimmerEvents(cmd)
}

def zwaveEvent(physicalgraph.zwave.commands.switchmultilevelv3.SwitchMultilevelStartLevelChange cmd) {
	log.debug "SwitchMultilevelStartLevelChange: ${cmd}"
	def result = []
	result << createEvent(name: "switch", value: "on")
	result << createEvent(name: "level", value: cmd.startLevel)
	return result
}

def zwaveEvent(physicalgraph.zwave.commands.switchmultilevelv3.SwitchMultilevelStopLevelChange cmd) {
	log.debug "SwitchMultilevelStopLevelChange: ${cmd}"
	refresh()
}

private dimmerEvents(physicalgraph.zwave.Command cmd, ep = null) {
	def value = (cmd.value ? "on" : "off")
	def result = [createEvent(name: "switch", value: value)]
	if (cmd.value <= 100) {
		result << createEvent(name: "level", value: cmd.value == 99 ? 100 : cmd.value)
		if (cmd.value != 0) {
			state.lastDimmingLevel = cmd.value
			log.debug "Dimming level state set to ${cmd.value}"
		}
	}
	log.debug "dimmerEvents: ${result}"
	return result
}
def zwaveEvent(physicalgraph.zwave.commands.meterv3.MeterReport cmd, ep = null) {
	log.debug "MeterReport: ${cmd}"
	handleMeterReport(cmd)
}

def zwaveEvent(physicalgraph.zwave.commands.sensormultilevelv5.SensorMultilevelReport cmd, ep = null) {
	log.debug "SensorMultilevelReport: ${cmd}, endpoint: ${ep}"
	createChildEvent(cmd)
}

def zwaveEvent(physicalgraph.zwave.commands.notificationv3.NotificationReport cmd, ep = null) {
	log.debug "NotificationReport: ${cmd}, endpoint: ${ep}"
	createChildEvent(cmd)
}

def zwaveEvent(physicalgraph.zwave.commands.sensorbinaryv2.SensorBinaryReport cmd, ep = null) {
	log.debug "SensorBinaryReport: ${cmd}, endpoint: ${ep}"
	def result
	if (cmd.sensorValue == 0) {
		result = createEvent([ name: "sensor", value: "inactive", descriptionText: "endpoint: ${ep}" ])
	} else if (cmd.sensorValue == 255) {
		result = createEvent([ name: "sensor", value: "active", descriptionText: "endpoint: ${ep}" ])
	}
	return result
}

def zwaveEvent(physicalgraph.zwave.commands.multichannelv3.MultiChannelCmdEncap cmd, ep = null) {
	def encapsulatedCommand = cmd.encapsulatedCommand()
	log.debug "MultiChannelCmdEncap: ${encapsulatedCommand}" + (ep ? " from endpoint $ep" : "")
	if (encapsulatedCommand) {
		zwaveEvent(encapsulatedCommand, cmd.sourceEndPoint as Integer)
	}
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
			createEvent(name: "power", value: Math.round(cmd.scaledMeterValue), unit: "W")
		}
	}
}

def getNotificationComponentLabel() {
	[0x01 : [componentLabel: "Qubino Notification from Smoke Sensor", dthName: "Child Smoke Alarm"],
	 0x02 : [componentLabel: "Qubino Notification from CO Sensor", dthName: "Child CO Alarm"],
	 0x03 : [componentLabel: "Qubino Notification from CO2 Sensor", dthName: "Child CO2 Alarm"],
	 0x04 : [componentLabel: "Qubino Overheat Notification", dthName: "Child Heat Alarm"],
	 0x05 : [componentLabel: "Qubino Notification from Water Sensor", dthName: "Child Water Sensor"],
	 0x07 : [componentLabel: "Qubino Notification from Motion Sensor", dthName: "Child Motion Sensor"]]
}

def handleChildEvent(cmd) {
	log.debug ("Creating child event: ${cmd}")
	def map = [:]
	def componentLabel
	def childDthName
	def childDni
	def childNamespace

	if (cmd.hasProperty("notificationType")) {
		componentLabel = notificationComponentLabel[cmd.notificationType as int].find{ it.key == "componentLabel" }?.value
		childDthName = notificationComponentLabel[cmd.notificationType as int].find{ it.key == "dthName" }?.value
		childNamespace = "smartthings"

		if (cmd.notificationType == 0x01) { // Smoke Alarm
			map.name = "smoke"
			switch (cmd.event) {
				case 0x02:
					map.value = "detected"
					map.descriptionText = "$device.displayName detected smoke"
					break
				case 0x00:
					map.value = "clear"
					map.descriptionText = "$device.displayName detected no smoke"
					break
			}
		} else if (cmd.notificationType == 0x02) { // CO Alarm
			map.name = "carbonMonoxide"
			switch (cmd.event) {
				case 0x02:
					map.value = "detected"
					map.descriptionText = "$device.displayName detected CO"
					break
				case 0x00:
					map.value = "clear"
					map.descriptionText = "$device.displayName detected no CO"
					break
			}
		} else if (cmd.notificationType == 0x03) { // C02 Alarm
			map.name = "carbonDioxide"
			switch (cmd.event) {
				case 0x02:
					map.value = "detected"
					map.descriptionText = "$device.displayName detected CO2"
					break
				case 0x00:
					map.value = "clear"
					map.descriptionText = "$device.displayName detected no CO2"
					break
			}
		} else if (cmd.notificationType == 0x04) { // Overheat Alarm
			map.name = "temperatureAlarm"
			switch (cmd.event) {
				case 0x00:
					map.value = "cleared"
					map.descriptionText = "$device.displayName heat is clear"
					break
				case 0x02:
					map.value = "heat"
					map.descriptionText = "$device.displayName heat was detected!"
					break
			}
		} else if (cmd.notificationType == 0x05) { // Water Alarm
			map.name = "water"
			switch (cmd.event) {
				case 0x02:
					map.value = "wet"
					map.descriptionText = "$device.displayName detected water"
					break
				case 0x00:
					map.value = "dry"
					map.descriptionText = "$device.displayName detected no water"
					break
			}
		} else if (cmd.notificationType == 0x07) { // Home Security
			map.name = "motion"
			if (cmd.event == 0x00) {
				map.value = "inactive"
				map.descriptionText = "$device.displayName detected no motion"
			} else if (cmd.event == 0x07 || cmd.event == 0x08) {
				map.value = "active"
				map.descriptionText = "$device.displayName detected motion"
			}
		}
	} else if (cmd.hasProperty("sensorType")) {
		componentLabel = "Qubino Temperature Sensor"
		childDthName = "Child Temperature Sensor"
		childNamespace = "qubino"
		switch (cmd.sensorType) {
			case 0x01:
				log.debug ("cmd: ${cmd}")
				map.name = "temperature"
				def cmdScale = cmd.scale == 1 ? "F" : "C"
				map.value = convertTemperatureIfNeeded(cmd.scaledSensorValue, cmdScale, cmd.precision)
				map.unit = getTemperatureScale()
				break
		}
	}
	childDni = "${device.deviceNetworkId}:${childUniqueDni[componentLabel]}"
	if (doesChildDeviceExist(componentLabel)) {
		log.debug("Found device: ${componentLabel}")
	} else {
		log.debug ("Device ${componentLabel} not found. Creating new child device.")
		createChildDevice(componentLabel, childNamespace, childDthName, childDni)
	}
	sendEventToChild(map, childDni)
}

def sendEventToChild(event, childDni) {
	def child = childDevices.find { it.deviceNetworkId == childDni }
	log.debug "Sending event: ${event} to child: ${child}"
	child?.sendEvent(event)
}

private refreshChild() {
	sendHubCommand(zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType: 1, scale: 0).format())
}

def doesChildDeviceExist(componentLabel) {
	def exists = (childDevices.find{it.toString() == componentLabel.toString()} == null) ? false : true
}

def getAppSwitchOnOffTime() {
	/*
	User can set on/off time between 50 - 255 ms only when the dimmer is activated from the physical wall-switch.
	This function adjusts that time to be used also when activating from the Mobile App (1-3s).
	Map value from the range 50..255 into range 1..3 >> Math.round(1 + ((dimmingTimeBtn - 50) * (3-1))/ (255-50)) = Math.round(1 + ((dimmingTimeBtn - 50) * mappingConst
	*/

	def dimmingTimePref = parameterMap.find({ it.key == 'dimmingTimeSoftOnOff' })
	def dimmingTimeBtn = getCommandValue(dimmingTimePref) ?: dimmingTimePref.defaultValue
	def mappingConst = 0.00976
	def appOnOffTime = Math.round(1 + ((dimmingTimeBtn - 50) * mappingConst))
	log.debug "appOnOffTime: $appOnOffTime"
	return appOnOffTime
}

def on() {
	def value = state.lastDimmingLevel ?: getCommandValue(maxDimmingLvlPref) ?: maxDimmingLvlPref.defaultValue
	def dimmingOn = appSwitchOnOffTime

	log.debug "dimming on value: ${value}"
	log.debug "dimming on time: ${dimmingOn}"
	encapSequence([
		zwave.switchMultilevelV3.switchMultilevelSet(dimmingDuration: dimmingOn, value: value),
		//zwave.switchMultilevelV3.switchMultilevelGet(),
		meterGet(scale: 0),
		meterGet(scale: 2)
	], statusDelay)
}

def off() {
	def dimmingOff = appSwitchOnOffTime
	log.debug ("dimming off time: ${dimmingOff}")
	encapSequence([
		zwave.switchMultilevelV3.switchMultilevelSet(dimmingDuration: dimmingOff, value: 0x00),
		//zwave.switchMultilevelV3.switchMultilevelGet(),
		meterGet(scale: 0),
		meterGet(scale: 2)
	], statusDelay)
}

def ping() {
	refresh()
}

def refresh() {
	log.debug "refresh()"
	encapSequence([
		zwave.switchMultilevelV1.switchMultilevelGet(),
		meterGet(scale: 0),
		meterGet(scale: 2),
		zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType: 1, scale: 0)
	], 100)
}

def checkIfLevelCorrect(level) {
	def max = getCommandValue(maxDimmingLvlPref) ?: maxDimmingLvlPref.defaultValue
	def min = getCommandValue(minDimmingLvlPref) ?: minDimmingLvlPref.defaultValue
	def value = state.lastDimmingLevel ?: getCommandValue(maxDimmingLvlPref) ?: maxDimmingLvlPref.defaultValue

	if (level > max && level <= 100) {
		level = max
		log.warn "setLvl is greater than ${max}. New lvl: ${level}"
	} else if (level < min) {
		level = min
		log.warn "setLvl is lower than ${min}. New lvl: ${level}"
	}
	return level as int
}

def setLevel(value) {
	// this method is called without duration argument so dimmingDuration is set according to the current on/off transition time (1-3s).
	log.debug "setLvl: $value"
	def result = []
	def level = checkIfLevelCorrect(value)
	encapSequence([
		zwave.switchMultilevelV3.switchMultilevelSet(dimmingDuration: appSwitchOnOffTime, value: level),
		//zwave.switchMultilevelV3.switchMultilevelGet(),
		meterGet(scale: 0),
		meterGet(scale: 2)
	], statusDelay)
}

def setLevel(value, duration) {
	// this method is called with duration argument which should be passed as dimmingDuration parameter (unless the preference for dimming duration is specified).
	def level = checkIfLevelCorrect(value)
	def dimmingDuration = (dimmingDurationPref != 0) ? dimmingDurationPref : duration
	log.debug "setLevel: $value, duration: $dimmingDuration"
	def durationDelay = dimmingDuration < 128 ? (dimmingDuration * 1000) + 2000 : (Math.round(dimmingDuration / 60) * 60 * 1000) + 2000 as Integer
	encapSequence([
		zwave.switchMultilevelV3.switchMultilevelSet(dimmingDuration: dimmingDuration, value: level),
		zwave.switchMultilevelV3.switchMultilevelGet(),
		meterGet(scale: 0),
		meterGet(scale: 2)
	], durationDelay)
}

def getDimmingDurationPref() {
	// Check if the 'dimming duration' preference is specified and return it's value (1..127s).
	def dimmingDurationPref = parameterMap.find({it.key == 'dimmingDuration'})
	def dimmingDuration = getCommandValue(dimmingDurationPref) ?: dimmingDurationPref.defaultValue
	return dimmingDuration
}

def getStatusDelay() {
	return 1000 * (appSwitchOnOffTime + 1) as Integer
}

def getMaxDimmingLvlPref() {
	return parameterMap.find({it.key == 'maximumDimmingValue'})
}

def getMinDimmingLvlPref() {
	return parameterMap.find({it.key == 'minimumDimmingValue'})
}

def createChildDevice(componentLabel, childNamespace, childDthName, childDni) {
	try {
		addChildDevice(childNamespace, childDthName, childDni, device.hub.id,[
				completedSetup: true,
				label: componentLabel,
				isComponent: false
		])
	} catch(Exception e) {
		log.debug "Exception: ${e}"
	}
}

def getChildUniqueDni() {
	["Qubino Notification from Motion Sensor":"1",
	 "Qubino Notification from CO Sensor":"2",
	 "Qubino Notification from Smoke Sensor":"3",
	 "Qubino Notification from CO2 Sensor":"4",
	 "Qubino Notification from Water Sensor":"5",
	 "Qubino Overheat Notification":"6",
	 "Qubino Temperature Sensor":"7"]
}

def meterGet(scale) {
	zwave.meterV3.meterGet(scale)
}

private encapSequence(cmds, Integer delay=250) {
	delayBetween(cmds.collect{ secure(it) }, delay)
}

private secure(physicalgraph.zwave.Command cmd) {
	if (zwaveInfo?.zw?.contains("s")) {
		zwave.securityV1.securityMessageEncapsulation().encapsulate(cmd).format()
	} else {
		log.debug "no encapsulation supported for command: $cmd"
		cmd.format()
	}
}

private getParameterMap() {[
		[
			name: "Input 1 switch type", key: "input1SwitchType", type: "enum",
			parameterNumber: 1, size: 1, defaultValue: 0,
			values: [
				0: "Default value - push button (momentary)",
				1: "on/off toggle switch"
			],
			description: "Select between push-button (momentary) and on/off toggle switch types."
		],
		[
			name: "Input 2 switch type", key: "inputsSwitchTypes", type: "enum",
			parameterNumber: 2, size: 1, defaultValue: 0,
			values: [
				0: "Default value - push button (momentary)",
				1: "on/off toggle switch"
			],
			description: "Select between push-button (momentary) and on/off toggle switch types. Both inputs must work the same way."
		],
		[
			name: "Input 2 contact type", key: "input2ContactType", type: "enum",
			parameterNumber: 3, size: 1, defaultValue: 0,
			values: [
				0: "Default value - NO (normally open) input type",
				1: "NC (normally close) input type"
			],
			description: "Select between normally open (NO) and normally close (NC) contact types."
		],
		[
			name: "Input 3 contact type", key: "input3ContactType", type: "enum",
			parameterNumber: 4, size: 1, defaultValue: 0,
			values: [
				0: "Default value - NO (normally open) input type",
				1: "NC (normally close) input type"
			],
			description: "Select between normally open (NO) and normally close (NC) contact types."
		],
		[
			name: "Activate/deactivate functions ALL ON/ALL OFF", key: "activate/DeactivateFunctionsAllOn/AllOff", type: "enum",
			parameterNumber: 10, size: 2, defaultValue: 255,
			values: [
				0: "ALL ON not active, ALL OFF not active",
				1: "ALL ON not active, ALL OFF active",
				2: "ALL ON active, ALL OFF not active",
				255: "Default value - ALL ON active, ALL OFF active",
			],
			description: "Responds to commands ALL ON/ALL OFF that may be sent by the main controller or by other controller belonging to the system."
		],
		[
			name: "Automatic turning off output after set time", key: "automaticTurningOffOutputAfterSetTime", type: "boolRange",
			parameterNumber: 11, size: 2, defaultValue: 0,
			range: "1..32535", disableValue: 0,
			description: "Turns off the output after set time. " +
				"0 Default value - Auto OFF disabled, " +
				"1 second - 32535 seconds Auto OFF enabled with define time, step is 1 second"
		],
		[
			name: "Automatic turning on output after set time", key: "automaticTurningOnOutputAfterSetTime", type: "boolRange",
			parameterNumber: 12, size: 2, defaultValue: 0,
			range: "1..32535", disableValue: 0,
			description: "Turns on the output after set time." +
				"0 (Default value) - Auto ON disabled, " +
				"1 second - 32535 seconds Auto ON enabled with define time, step is 1 second"
		],
		[
			name: "Enable/Disable the 3-way switch/additional switch", key: "enable/DisableAdditionalSwitch", type: "enum",
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
			description: "If Double click function is enabled, a fast double click on the push button will set dimming power at maximum dimming value. Valid only if input is set as push button."
		],
		[
			name: "Saving the state of the device after a power failure", key: "savingTheStateOfTheDeviceAfterAPowerFailure", type: "boolean",
			parameterNumber: 30, size: 1, defaultValue: 0,
			optionInactive: 0, inactiveDescription: "Default value - Flush Dimmer module saves its state before power failure (it returns to the last position saved before a power failure)",
			optionActive: 1,
			description: "Based on the parameter settings the stores/does not store the last value of the output after power failure. "
		],
		[
			name: "Watt Power Consumption Reporting Threshold for Load", key: "wattPowerConsumptionThreshold", type: "range",
			parameterNumber: 40, size: 1, defaultValue: 10,
			range: "1..100",
			description: "0 = Power consumption reporting disabled, " +
				"default value = 10% " +
				"1%-100% Power consumption reporting enabled. " +
				"New value is reported only when Wattage in real time changes by more than the percentage value set in this parameter compared to the previous Wattage reading, starting at 1% " +
				"NOTE: Power consumption needs to increase or decrease by at least 1 Watt to be reported, REGARDLESS of percentage set in this parameter."
		],
		[
			name: "Watt Power Consumption Reporting Time Threshold for Load", key: "wattPowerConsumptionTimeThreshold", type: "range",
			parameterNumber: 42, size: 2, defaultValue: 10,
			range: "1..32767",
			description: "default value 0" +
				"0 to 29 - Power consumption reporting disabled. " +
				"30-32757 Power consumption reporting enabled. " +
				"Report is sent according to ime interval (value) set here"
		],
		[
			name: "Minimum dimming value", key: "minimumDimmingValue", type: "range",
			parameterNumber: 60, size: 1, defaultValue: 1,
			range: "1..98",
			description: "1 (Default value) = 1% (minimum dimming value), " +
				"1 - 98 = 1% - 98%, step is 1%. Minimum dimming values is set by entered value. " +
				"If Switch_multilevel_set is set to the value '0', the output is turned OFF. " +
				"If Switch_multilevel_set is set to the value '1', the output is set to the minimum diming value. " +
				"NOTE: The minimum level may not be higher than the maximum level."
		],
		[
			name: "Maximum dimming value", key: "maximumDimmingValue", type: "range",
			parameterNumber: 61, size: 1, defaultValue: 99,
			range: "2..99",
			description: "99 (Default value) = 99% (Maximum dimming value)" +
				"2 - 99 = 2% - 99%, step is 1%. Maximum dimming values is set by entered value. " +
				"When the switch type is selected as Bi-stable, it is not possible to dim the value between min and max. " +
				"NOTE: The maximum level may not be lower than the minimum level."
		],
		[
			name: "Dimming time (soft on/off)", key: "dimmingTimeSoftOnOff", type: "range",
			parameterNumber: 65, size: 2, defaultValue: 100,
			range: "50..255",
			description: "Set value means time of moving the Flush Dimmer between min. and max. dimming values by short press of push button I1 or controlled through UI (BasicSet). " +
				"100 (Default value) = 1s, " +
				"50 - 255 = 500 - 2550 milliseconds (2,55s), step is 10 milliseconds"
		],
		[
			name: "Dimming time when key pressed", key: "dimmingTimeWhenKeyPressed", type: "range",
			parameterNumber: 66, size: 2, defaultValue: 3,
			range: "1..255",
			description: "Time of moving the Flush Dimmer between min. and max dimming values by continues hold of push button I1 or associated device. " +
				"3 seconds (Default value), " +
				"1 - 255 seconds"
		],
		[
			name: "Ignore start level", key: "ignoreStartLevel", type: "boolean",
			parameterNumber: 67, size: 1, defaultValue: 0,
			optionInactive: 0, inactiveDescription: "Default value - Respect start level",
			optionActive: 1, activeDescription: "Ignore start level",
			description: "This parameter is used with association group 3. A receiving device SHOULD respect the start level if the Ignore Start Level bit is 0. " +
				"A receiving device MUST ignore the start level if the Ignore Start Level bit is 1."
		],
		[
			name: "Dimming duration", key: "dimmingDuration", type: "range",
			parameterNumber: 68, size: 1, defaultValue: 0,
			range: "0..127",
			description: "This parameter is used with association group 3. The Duration field MUST specify the time that the transition should take from the current value to the new target value. " +
				"A supporting device SHOULD respect the specified Duration value. " +
				"0 (Default value) - dimming duration according to parameter: 'Dimming time when key pressed'," +
				"1 to 127 seconds"
		],
		[
			name: "Enable/Disable Endpoint l2 or select the Notification Type and the Notification Event", key: "enable/DisableNotificationl2", type: "enum",
			parameterNumber: 100, size: 1, defaultValue: 0,
			values: [
				0: "Default value - Endpoint l2 disabled",
				1: "Home Security; Motion Detection, unknown location",
				2: "CO; Carbon Monoxide detected, unknown location",
				3: "CO2; CarbonDioxide detected, unknown location",
				4: "Water Alarm; Water Leak detected, unknown location",
				5: "Heat Alarm; Overheat detected, unknown location",
				6: "Smoke Alarm; Smoke detected, unknown location",
				9: "Sensor Binary" //9
			],
			description: "NOTE1: After changing the values of the parameter, first exclude the device (without setting the parameters to their default values), then wait at least 30 seconds to re-include the device!. " +
				"NOTE2: When the parameter is set to value 9 the notifications are sent for the Home Security notification type."
		],
		[
			name: "Enable/Disable Endpoint l3 or select the Notification Type and the Notification Event", key: "enable/DisableNotificationl3", type: "enum",
			parameterNumber: 101, size: 1, defaultValue: 0,
			values: [
				0: "Default value - Endpoint l3 disabled",
				1: "Home Security; Motion Detection, unknown location",
				2: "CO; Carbon Monoxide detected, unknown location",
				3: "CO2; CarbonDioxide detected, unknown location",
				4: "Water Alarm; Water Leak detected, unknown location",
				5: "Heat Alarm; Overheat detected, unknown location",
				6: "Smoke Alarm; Smoke detected, unknown location",
				9: "Sensor Binary" //9
			],
			description: "NOTE1: After changing the values of the parameter, first exclude the device (without setting the parameters to their default values), then wait at least 30 seconds to re-include the device!. " +
				"NOTE2: When the parameter is set to value 9 the notifications are sent for the Home Security notification type."
		],
		[
			name: "Temperature sensor offset settings", key: "temperatureSensorOffsetSettings", type: "range",
			parameterNumber: 110, size: 2, defaultValue: 32536,
			range: "1..32536",
			description: "Set value is added or subtracted to actual measured value by sensor. " +
				"32536 (Default value), " +
				"1 to 100 Value from 0.1 degrees celsius to 10.0 degrees celsius is added to actual measured temperature " +
				"1001 to 1100 Value from -0.1 degrees celsius to -10.0 degrees celsius is subtracted to actual measured temperature"
		],
		[
			name: "Digital temperature sensor reporting", key: "digitalTemperatureSensorReporting", type: "range",
			parameterNumber: 120, size: 1, defaultValue: 5,
			range: "1..127",
			description: "Digital temperature sensor reporting If digital temperature sensor is connected, module reports measured temperature on temperature change defined by this parameter. " +
				"Available configuration parameters (data type is 1 Byte DEC): " +
				"0 - Reporting disabled, " +
				"5 (Default value) = 0,5°C change, " +
				"1 - 127 = 0,1°C - 12,7°C, step is 0,1°C"
		],
		[
			name: "Enable/Disable Reporting on set command", key: "enable/DisableReporting", type: "boolean",
			parameterNumber: 249, size: 1, defaultValue: 0,
			optionInactive: 0, inactiveDescription: "Default value - Double click disabled",
			optionActive: 1, activeDescription: "Reporting on set command enabled",
			description: "Choose whether reports (containing the new output state) are sent to the gateway (hub) after a set command is received by the device"
		]

]}