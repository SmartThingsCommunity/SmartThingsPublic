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
	definition(name: "Qubino Flush 1D Relay", namespace: "smartthings", author: "SmartThings", ocfDeviceType: "oic.d.switch", minHubCoreVersion: '000.019.00012') {
		capability "Actuator"
		capability "Configuration"
		capability "Health Check"
		capability "Switch"
		capability "Refresh"
		capability "Sensor"
		
		fingerprint mfr: "0159 ", prod: "0002", model: "0053", deviceJoinName: "Qubino Flush 1D Relay"
	}

	tiles(scale: 2) {
		multiAttributeTile(name: "switch", type: "lighting", width: 6, height: 4, canChangeIcon: true) {
			tileAttribute("device.switch", key: "PRIMARY_CONTROL") {
				attributeState "on", label: '${name}', action: "switch.off", icon: "st.switches.switch.on", backgroundColor: "#00A0DC"
				attributeState "off", label: '${name}', action: "switch.on", icon: "st.switches.switch.off", backgroundColor: "#ffffff"
			}
		}

		standardTile("refresh", "device.switch", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
			state "default", label: '', action: "refresh.refresh", icon: "st.secondary.refresh"
		}

		main "switch"
		details(["switch", "refresh"])
	}

    preferences {
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
}
}

def installed() {
	sendEvent(name: "checkInterval", value: 2 * 15 * 60 + 2 * 60, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID, offlinePingable: "1"])
	response(refresh())
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
}

def updated() {
	initialize()
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
}

def configure() {
	def commands = []
	commands << zwave.manufacturerSpecificV2.manufacturerSpecificGet().format()
	commands << zwave.associationV1.associationSet(groupingIdentifier:1, nodeId:[zwaveHubNodeId]).format()
	commands << zwave.associationV1.associationSet(groupingIdentifier:2, nodeId:[zwaveHubNodeId]).format()
	commands << zwave.associationV1.associationSet(groupingIdentifier:3, nodeId:[zwaveHubNodeId]).format()
	commands << zwave.associationV1.associationSet(groupingIdentifier:4, nodeId:[zwaveHubNodeId]).format()
	commands << zwave.associationV1.associationSet(groupingIdentifier:5, nodeId:[zwaveHubNodeId]).format()
	commands << zwave.associationV1.associationSet(groupingIdentifier:6, nodeId:[zwaveHubNodeId]).format()
	delayBetween(commands, 500)
}


def initialize() {
	if (!childDevices) {
		addChild()
	}
	sendEvent(name: "checkInterval", value: 2 * 60 * 60 + 2 * 60, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])
}

def getCommandClassVersions() {
	[
		0x20: 1, //Basic
		0x5A: 1, //Device Reset Locally
		0x73: 1, //Powerlevel
		0x86: 2, //Version
		0x72: 2, //Manufacturer Specific
		0x27: 1, //Switch All
		0x25: 1, //Switch Binary
		0x32: 4, //Meter
		0x31: 5, //Sensor Multi Level 
		0x85: 2, //Association
		0x8E: 3, //Multi Channel Association
		0x59: 2, //Association Grp Info
		0x70: 2, //Configuration
	]
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

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd) {
	[name: "switch", value: cmd.value ? "on" : "off"]
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicSet cmd) {
	[name: "switch", value: cmd.value ? "on" : "off"]
}

def zwaveEvent(physicalgraph.zwave.commands.switchbinaryv1.SwitchBinaryReport cmd) {
	[name: "switch", value: cmd.value ? "on" : "off"]
}

def zwaveEvent(physicalgraph.zwave.commands.manufacturerspecificv2.ManufacturerSpecificReport cmd) {
	log.debug "manufacturerId:   $cmd.manufacturerId, manufacturerName: $cmd.manufacturerName, productId:        $cmd.productId, productTypeId:    $cmd.productTypeId"
	def msr = String.format("%04X-%04X-%04X", cmd.manufacturerId, cmd.productTypeId, cmd.productId)
	updateDataValue("MSR", msr)
	updateDataValue("manufacturer", cmd.manufacturerName)
	createEvent([descriptionText: "$device.displayName MSR: $msr", isStateChange: false])
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
	log.debug("Commands: ${cmd}")
	[:]
}

def on() {
    log.debug "on"
	delayBetween([
			zwave.basicV1.basicSet(value: 0xFF).format(),
			zwave.basicV1.basicGet().format()
	])
}

def off() {
    log.debug "off"
	delayBetween([
			zwave.basicV1.basicSet(value: 0x00).format(),
			zwave.basicV1.basicGet().format()
	])
}

def ping() {
	refresh()
}

def refresh() {
	def commands = []
	commands << zwave.basicV1.basicGet().format()
	if (getDataValue("MSR") == null) {
		commands << zwave.manufacturerSpecificV1.manufacturerSpecificGet().format()
	}
	delayBetween(commands)
}

private addChild() {
		try {
			String childDni = "${device.deviceNetworkId}:2"

			def value = settings."enable/DisableEndpointI2OrSelectNotificationTypeAndEvent" as String
			String name = dthName(value)

			addChildDevice(name, childDni, device.getHub().getId(), [
					completedSetup: true,
					label: componentLabel,
					isComponent: false
			])
		} catch (Exception e) {
			log.debug "Excep: ${e} "
		}
}

def dthName(def number) {
	dthNames.find { it['number'] == number }?.get('dthName')
}

def getDthNames() {
	def map =
	//Number 0 - endpoit disabled. Default value
		[
			[number: "6", dthName: "Child Smoke Alarm"],
			[number: "2", dthName: "Child CO Alarm"],
			[number: "3", dthName: "Child CO2 Alarm"],
			[number: "5", dthName: "Child Heat Alarm"],
			[number: "4", dthName: "Child Water Sensor"],
			[number: "1", dthName: "Child Motion Sensor"]
		]
	map
}

def createChildEvent(cmd) {
	log.debug ("Creating child event: ${cmd}")
	def map = [:]

	if (cmd.hasProperty("notificationType")) {
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

	if (doesChildDeviceExist(componentLabel)) {
		log.debug("Found device: ${componentLabel}")
	} else {
		log.debug ("Adding child")
		addChild()
	}
	sendEventToChild(map)
}

def sendEventToChild(event) {
	def childDni = "${device.deviceNetworkId}:2"
	def child = childDevices.find { it.deviceNetworkId == childDni }
	log.debug "Sending event: ${event} to child: ${child}"
	child?.sendEvent(event)
}

private refreshChild() {
	sendHubCommand(zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType: 1, scale: 0).format())
}
//Preferences part

def zwaveEvent(physicalgraph.zwave.commands.configurationv2.ConfigurationReport cmd) {
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

private getParameterMap() {[
	[
		name: "Input 1 switch type", key: "input1SwitchType", type: "boolean",
		parameterNumber: 1, size: 1, defaultValue: 1,
		optionInactive: 0, inactiveDescription: "mono-stable switch type (push button)",
		optionActive: 1, activeDescription: " bi-stable switch type ",
		description: "Defines the type of switch connected to Input 1 "
	],
	[
		name: " Activate / deactivate functions ALL ON/ALL OFF", key: "activate/DeactivateFunctionsAllOn/AllOff", type: "enum",
		parameterNumber: 10, size: 2, defaultValue: 255,
		values: [
			255: "ALL ON active, ALL OFF active",
			0: "ALL ON is not active ALL OFF is not active",
			1: "ALL ON is not active ALL OFF active",
			2: "ALL ON active ALL OFF is not active"
		],
		description: "Support to activate / deactivate functions ALL ON/ALL OFF Flush 1D relay module responds to commands ALL ON / ALL OFF that may be sent by the main controller or by other controller belonging to the system.  "
	],
	[
		name: "Enable / Disable Endpoint I2 or select the Notification Type and the Notification Event", key: "enable/DisableEndpointI2OrSelectNotificationTypeAndEvent", type: "enum",
		parameterNumber: 100, size: 1, defaultValue: 0,
		values: [
			1: "Motion Detection",
			2: "Carbon Monoxide",
			3: "Carbon Dioxide",
			4: "Water Alarm",
			5: "Heat Alarm",
			6: "Smoke Alarm",
			0: "2nd endpoint disabled"
		],
		description: "Support Enable / Disable Endpoint I2 or select Notification Type and Event Enabling I2, means that Endpoint (I2) will be present on UI. Disabling it will result in hiding the endpoint according to the parameter set value. Additionally, a Notification Type and Event can be selected for the endpoint.   NOTE: After parameter change module has to be reincluded into the network for the setting to take effect! "
	],
	[
		name: "Automatic turning off output after set time ", key: "automaticTurningOffOutputAfterSetTime", type: "boolRange",
		parameterNumber: 11, size: 2, defaultValue: 0,
		range: "1..32535", disableValue: 0, 
		description: "Turns off the output after set time. " +
						"0 Default value - Auto OFF disabled, " +
						"1 second - 32535 seconds Auto OFF enabled with define time, step is 1 second"
	],
	[
		name: "Temperature sensor offset settings", key: "temperatureSensorOffsetSettings", type: "unknown",
		parameterNumber: 110, size: 2, defaultValue: 32536,
		description: "Defines temperature sensor offset settings Set value is added or subtracted to actual measured value by sensor. "
	],
	[
		name: "Automatic turning on output after set time ", key: "automaticTurningOnOutputAfterSetTime", type: "boolRange",
		parameterNumber: 12, size: 2, defaultValue: 0,
		range: "1..32535", disableValue: 0, 
		description: "Turns on the output after set time." +
						"0 (Default value) - Auto ON disabled, " +
						"1 second - 32535 seconds Auto ON enabled with define time, step is 1 second"
	],
	[
		name: "Digital temperature sensor reporting", key: "digitalTemperatureSensorReporting", type: "boolRange",
		parameterNumber: 120, size: 1, defaultValue: 5,
		range: "1..127", disableValue: 0, 
		description: "Defines the digital temperature sensor reporting If digital temperature sensor is connected, module reports measured temperature on temperature change defined by this parameter. "
	],
	[
		name: "Automatic turning off / on seconds or milliseconds selection", key: "automaticTurningOff/OnSecondsOrMillisecondsSelection", type: "boolean",
		parameterNumber: 15, size: 1, defaultValue: 0,
		optionInactive: 0, inactiveDescription: " seconds selected",
		optionActive: 1, activeDescription: "milliseconds selected",
		description: "Support automatic turning off / on seconds or milliseconds selection NOTE: This parameter is valid for both, turning on and turning off parameters. "
	],
	[
		name: "Input 2 contact type", key: "input2ContactType", type: "boolean",
		parameterNumber: 2, size: 1, defaultValue: 0,
		optionInactive: 0, inactiveDescription: "NO (normally open) input type",
		optionActive: 1, activeDescription: "1 -  NC (normally close) input type",
		description: "Defines the type of switch connected to Input 2 "
	],
	[
		name: "Saving the state of the relay after a power failure", key: "savingTheStateOfTheRelayAfterAPowerFailure", type: "boolean",
		parameterNumber: 30, size: 1, defaultValue: 0,
		optionInactive: 0, inactiveDescription: "Flush 1D relay module saves its state before power failure (it returns to the last position saved before a power failure)",
		optionActive: 1, activeDescription: "Flush 1D relay module does not save  the state after a power failure, it returns to   &amp;amp;amp;amp;quot;off&amp;amp;amp;amp;quot; position.",
		description: "Support saving the state of the relay after a power failure "
	],
	[
		name: "Output Switch selection", key: "outputSwitchSelection", type: "boolean",
		parameterNumber: 63, size: 1, defaultValue: 0,
		optionInactive: 0, inactiveDescription: "When system is turned off the output is 0V (NC).",
		optionActive: 1, activeDescription: "When system is turned off the output is 230V or 24V (NO).",
		description: "Support output Switch selection Set value means the type of the device that is connected to the output. The device type can be normally open (NO) or normally close (NC).  "
	]	
]}