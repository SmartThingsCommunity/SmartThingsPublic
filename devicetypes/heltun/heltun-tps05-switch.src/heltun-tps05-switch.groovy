/**
 *  HELTUN TPS05 Switch
 *
 *  Copyright 2022 Sarkis Kabrailian
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 */
 
import groovy.transform.Field

@Field static int roomTemperature = 1
@Field static int humidity = 5
@Field static int illuminance = 3

metadata {
	definition (name: "HELTUN TPS05 Switch", namespace: "HELTUN", author: "Sarkis Kabrailian", cstHandler: true, mcdSync: true ) {
		capability "Temperature Measurement"
		capability "Illuminance Measurement"
		capability "Relative Humidity Measurement"
		capability "Energy Meter"
		capability "Power Meter"
		capability "Configuration"
		capability "Health Check"
		capability "Refresh"

		fingerprint mfr: "0344", prod: "0004", model: "0003", deviceJoinName: "HELTUN Panel"
	}
	preferences {
		input (
			title: "HE-TPS05 | HELTUN Touch Panel Switch",
			description: "The user manual document with all technical information is available in support.heltun.com page. In case of technical questions please contact HELTUN Support Team at support@heltun.com",
			type: "paragraph",
			element: "paragraph"
		)
			parameterMap().each {
			if (it.title != null) {
				input (
					title: "${it.title}",
					description: it.description,
					type: "paragraph",
					element: "paragraph"
				)
			}
			def unit = it.unit ? it.unit : ""
			def defV = it.default as Integer
			def defVDescr = it.options ? it.options.get(defV) : "${defV}${unit} - Default Value"
			input (
				name: it.name,
				title: null,
				description: "$defVDescr",
				type: it.type,
				options: it.options,
				range: (it.min != null && it.max != null) ? "${it.min}..${it.max}" : null,
				defaultValue: it.default,
				required: false
			)
		}
	}
}

def checkParam() {
	boolean needConfig = false
	parameterMap().each {
		if (state."$it.name" == null || state."$it.name".state == "defNotConfigured") {
			state."$it.name" = [value: it.default as Integer, state: "defNotConfigured"]
			needConfig = true
		}
		if (settings."$it.name" != null && (state."$it.name".value != settings."$it.name" as Integer || state."$it.name".state == "notConfigured")) {
			state."$it.name".value = settings."$it.name" as Integer
			state."$it.name".state = "notConfigured"
			needConfig = true
		}
	}
	if (needConfig) {
		configParam()
	}
}

private configParam() {
	def cmds = []
	for (parameter in parameterMap()) {
		if (state."$parameter.name"?.value != null && state."$parameter.name"?.state in ["notConfigured", "defNotConfigured"] ) {
			cmds << zwave.configurationV2.configurationSet(scaledConfigurationValue: state."$parameter.name".value, parameterNumber: parameter.paramNum, size: parameter.size).format()
			cmds << zwave.configurationV2.configurationGet(parameterNumber: parameter.paramNum).format()
			break
		}
	}
	if (cmds) {
		runIn(5, "checkParam")
		sendHubCommand(cmds,500)
	}
}

def zwaveEvent(physicalgraph.zwave.commands.sensormultilevelv5.SensorMultilevelReport cmd) {
	def map = [:]
	def localScale = getTemperatureScale() //HubScale
	def deviceScale = (cmd.scale == 1) ? "F" : "C" //DeviceScale
	def child = childDevices?.find {channelNumber(it.deviceNetworkId) == 1 }
	if (roomTemperature == cmd.sensorType) {
		def deviceTemp = cmd.scaledSensorValue
		def scaledTemp = (deviceScale == localScale) ? deviceTemp : (deviceScale == "F" ? roundC(fahrenheitToCelsius(deviceTemp)) : celsiusToFahrenheit(deviceTemp).toDouble().round(0).toInteger())
		map.name = "temperature"
		map.value = scaledTemp
		map.unit = localScale
		sendEvent(map)
	} else if (humidity == cmd.sensorType) {
		map.name = "humidity"
		map.value = cmd.scaledSensorValue.toInteger()
		map.unit = "%"
		sendEvent(map)
	} else if (illuminance == cmd.sensorType) {
		map.name = "illuminance"
		map.value = cmd.scaledSensorValue
		sendEvent(map)
	}
}

def zwaveEvent(physicalgraph.zwave.commands.configurationv2.ConfigurationReport cmd) {
	def parameter = parameterMap().find( {it.paramNum == cmd.parameterNumber } ).name
	if (state."$parameter".value == cmd.scaledConfigurationValue){
		state."$parameter".state = "configured"
	} else {
		state."$parameter".state = "error"
	}
	configParam()
}

def updated() {
	if (childDevices && device.label != state.oldLabel) {
		childDevices.each {
			def newLabel = getChildName(channelNumber(it.deviceNetworkId))
			it.setLabel(newLabel)
		}
		state.oldLabel = device.label
	}
	initialize()
}

def initialize() {
	runIn(3, "checkParam")
}

def installed() {
	def numberOfButtons = 5
	state.numberOfButtons = numberOfButtons
	def existingChildren = getChildDevices()
	for (i in 1..numberOfButtons) {
		def buttonNetworkId = "${device.deviceNetworkId}:${i+2*numberOfButtons}"
		def relayNetworkId = "${device.deviceNetworkId}:${i+numberOfButtons}"
		def backlightNetworkId = "${device.deviceNetworkId}:${i}"
		def childRelayExists = (existingChildren.find {child -> child.getDeviceNetworkId() == relayNetworkId} != NULL)
		def childButtonExists = (existingChildren.find {child -> child.getDeviceNetworkId() == buttonNetworkId} != NULL)
		def childBacklightExists = (existingChildren.find {child -> child.getDeviceNetworkId() == backlightNetworkId} != NULL)
		if (!childBacklightExists ) {
			addChildDevice("smartthings","Child Switch", backlightNetworkId, device.hubId, [completedSetup: true, label: getChildName(i), isComponent: false])
		}
		if (!childRelayExists) {
			addChildDevice("HELTUN", "Heltun Child Relay", relayNetworkId, device.hubId,[completedSetup: true, label: getChildName(i+numberOfButtons), isComponent: false])
		}
		if (!childButtonExists ) {
			addChildDevice("smartthings", "Child Button", buttonNetworkId, device.hubId, [completedSetup: true, label: getChildName(i+2*numberOfButtons), isComponent: true, componentName: "button$i", componentLabel: "Button ${i}"])
		}
	}
	initialize()
}

private getChildName(channelNumber) {
	def prefix = device.displayName
	if (prefix == "HELTUN Panel") {
		prefix = "HELTUN"
	}
	def numberOfButtons = state.numberOfButtons
	if (channelNumber in 1..numberOfButtons) {
		return "${prefix} " + "${"Backlight"} " + "${channelNumber}"
	}
	else if (channelNumber in (numberOfButtons+1)..(2*numberOfButtons)){
		return "${prefix} " + "${"Switch"} " + "${channelNumber-numberOfButtons}"
	}
	else if (channelNumber in (2*numberOfButtons+1)..(3*numberOfButtons)){
		return "${prefix} " + "${"Button"} " + "${channelNumber-numberOfButtons*2}"
	}
}

private channelNumber(String deviceNetworkId) {
	deviceNetworkId.split(":")[-1] as Integer
}

def parse(String description) {
	def cmd = zwave.parse(description)
	if (cmd) {
		return zwaveEvent(cmd)
	}
}

private void setState(value, endpoint = null) {
	def cmds = [
		encap(zwave.basicV1.basicSet(value: value), endpoint),
		encap(zwave.switchBinaryV1.switchBinaryGet(), endpoint),
	]
	sendHubCommand(cmds, 500)
}

private encap(cmd, endpoint) {
	if (endpoint) {
		zwave.multiChannelV3.multiChannelCmdEncap(destinationEndPoint:endpoint).encapsulate(cmd).format()
	} else {
		cmd.format()
	}
}

def childOn(childId) {
	setState(0xFF, channelNumber(childId))
}

def childOff(childId) {
	setState(0x00, channelNumber(childId))
}

def zwaveEvent(physicalgraph.zwave.commands.meterv3.MeterReport cmd) {
	def map = [:]
	if (cmd.meterType == 1) {
		if (cmd.scale == 0) {
			map.name = "energy"
			map.value = cmd.scaledMeterValue
			map.unit = "kWh"
			sendEvent(map)
		} else if (cmd.scale == 2) {
			map.name = "power"
			map.value = Math.round(cmd.scaledMeterValue)
			map.unit = "W"
			sendEvent(map)
		}
	}
}

def zwaveEvent(physicalgraph.zwave.commands.centralscenev1.CentralSceneNotification cmd) {
	log.info cmd
	def numberOfButtons = state.numberOfButtons
	def state
	def buttonN = cmd.sceneNumber
	switch (cmd.keyAttributes as Integer) {
		case 0:
			state = "pushed"
			break
		case 1: 
			state = "up"
			break
		case 2: 
			state = "held"
			break
	}
	if (buttonN) {
		def buttonId = buttonN + numberOfButtons * 2
		def child = childDevices?.find {channelNumber(it.deviceNetworkId) == buttonId }
		child?.sendEvent([name: "button", value: state, data: [buttonNumber: 1], isStateChange: true])
	}
}

def zwaveEvent(physicalgraph.zwave.commands.multichannelv3.MultiChannelCmdEncap cmd, ep = null) {
	def encapsulatedCommand = cmd.encapsulatedCommand()
	zwaveEvent(encapsulatedCommand, cmd.sourceEndPoint as Integer)
}

def zwaveEvent(physicalgraph.zwave.commands.switchbinaryv1.SwitchBinaryReport cmd, ep = null) {
	def numberOfButtons = state.numberOfButtons
	def value = cmd.value
	def childDevice = childDevices?.find {channelNumber(it.deviceNetworkId) == ep }
	def corrRelCons = 0
	def corRelParam = 11 + ep - numberOfButtons
	if (ep in numberOfButtons..(2*numberOfButtons)) {
		def param = parameterMap().find( {it.paramNum == corRelParam } ).name
		def paramState = state."$param"
		if (paramState) {
			corrRelCons = paramState.value
		}
	}
	if (childDevice) {
		childDevice.sendEvent(name: "switch", value: value ? "on" : "off")
		if (value) {
			sendEvent(name: "switch", value: "on")
			childDevice.sendEvent(name: "power", value: corrRelCons, unit: "W")
		} else {
			childDevice.sendEvent(name: "power", value: 0, unit: "W")
			if (!childDevices.any { it.currentValue("switch") == "on" }) {
				sendEvent(name: "switch", value: "off")
			}
		}
	}
}

def zwaveEvent(physicalgraph.zwave.commands.clockv1.ClockReport cmd) {
	def currDate = Calendar.getInstance(location.timeZone)
	def time = [hour: currDate.get(Calendar.HOUR_OF_DAY), minute: currDate.get(Calendar.MINUTE), weekday: currDate.get(Calendar.DAY_OF_WEEK)]
	if ((time.hour != cmd.hour) || (time.minute != cmd.minute) || (time.weekday != cmd.weekday)) {
		sendHubCommand(zwave.clockV1.clockSet(time).format())
	}
}

def zwaveEvent(physicalgraph.zwave.commands.multichannelassociationv2.MultiChannelAssociationReport cmd) {
	def cmds = []
	if (cmd.groupingIdentifier == 1) {
		if (cmd.nodeId != [0, zwaveHubNodeId, 0]) {
			cmds << zwave.multiChannelAssociationV2.multiChannelAssociationRemove(groupingIdentifier: 1).format()
			cmds << zwave.multiChannelAssociationV2.multiChannelAssociationSet(groupingIdentifier: 1, nodeId: [0,zwaveHubNodeId,0]).format()
		}
	}
	if (cmds) {
		sendHubCommand(cmds, 1200)
	}
}

def configure() {
	refresh()
}

def getRefreshCommands() {
	def numberOfButtons = state.numberOfButtons
	def cmds = []
	cmds << zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType:roomTemperature).format()
	cmds << zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType:humidity).format()
	cmds << zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType:illuminance).format()
	for (i in 1..(2 * numberOfButtons)) {
		cmds << encap(zwave.switchBinaryV1.switchBinaryGet(), i)
	}
	return cmds
}

def refresh() {
	def cmds = getRefreshCommands()
	cmds << zwave.clockV1.clockGet().format()
	cmds << zwave.multiChannelAssociationV2.multiChannelAssociationGet(groupingIdentifier: 1).format()
	sendHubCommand(cmds, 1200)
	runIn(15, "checkParam")
}

def ping() {
	def cmds = getRefreshCommands()
	sendHubCommand(cmds, 1200)
}

def resetEnergyMeter() {
	sendHubCommand(zwave.meterV3.meterReset().format())
}

private parameterMap() {[
	[title: "Relays Output Mode", description: "These Parameters determine the type of loads connected to the device relay outputs. " + 
	"The output type can be NO – normal open (no contact/voltage switch the load OFF) or NC - normal close (output is contacted / there is a voltage to switch the load OFF)",
	 name: "Selected Relay 1 Mode", options: [
				0: "NO - Normal Open", 
				1: "NC - Normal Close"
		], paramNum: 7, size: 1, default: "0", type: "enum"],

	[name: "Selected Relay 2 Mode", options: [
				0: "NO - Normal Open", 
				1: "NC - Normal Close"
		], paramNum: 8, size: 1, default: "0", type: "enum"],

	[name: "Selected Relay 3 Mode", options: [
				0: "NO - Normal Open", 
				1: "NC - Normal Close"
		], paramNum: 9, size: 1, default: "0", type: "enum"],

	[name: "Selected Relay 4 Mode", options: [
				0: "NO - Normal Open", 
				1: "NC - Normal Close"
		], paramNum: 10, size: 1, default: "0", type: "enum"],

	[name: "Selected Relay 5 Mode", options: [
				0: "NO - Normal Open", 
				1: "NC - Normal Close"
		], paramNum: 11, size: 1, default: "0", type: "enum"],

	[title: "Relays Load Power", description: "These parameters are used to specify the loads power that are connected to the device outputs (Relays). " +
	"Using your connected device’s power consumption specification (see associated owner’s manual), set the load in Watts for the outputs bellow:",
	 name: "Selected Relay 1 Load Power in Watts", paramNum: 12, size: 2, default: 0, type: "number", min: 0, max: 1100, unit: "W"],

	[name: "Selected Relay 2 Load Power in Watts", paramNum: 13, size: 2, default: 0, type: "number", min: 0, max: 1100, unit: "W"],

	[name: "Selected Relay 3 Load Power in Watts", paramNum: 14, size: 2, default: 0, type: "number", min: 0, max: 1100, unit: "W"],

	[name: "Selected Relay 4 Load Power in Watts", paramNum: 15, size: 2, default: 0, type: "number", min: 0, max: 1100, unit: "W"],

	[name: "Selected Relay 5 Load Power in Watts", paramNum: 16, size: 2, default: 0, type: "number", min: 0, max: 1100, unit: "W"],

	[title: "Air Temperature Calibration", description: "This Parameter defines the offset value for room air temperature. " +
	"This value will be added or subtracted from the air temperature sensor reading.Through the Z-Wave network the value of this Parameter should be x10, e.g. for 1.5°C set the value 15.",
	 name: "Selected Temperature Offset in °Cx10", paramNum: 17, size: 1, default: 0, type: "number", min: -100, max: 100, unit: " °Cx10"],

	[title: "Touch Sensor Sensitivity Threshold", description: "This Parameter allows to adjust the Touch Buttons Sensitivity. " +
	"Note: Setting the sensitivity too high can lead to false touch detection. We recommend not changing this Parameter unless there is a special need to do so.",
	 name: "Selected Touch Sensitivity", options: [
				1: "Level 1 (Low sensitivity)",
				2: "Level 2",
				3: "Level 3",
				4: "Level 4",
				5: "Level 5",
				6: "Level 6",
				7: "Level 7",
				8: "Level 8",
				9: "Level 9",
				10: "Level 10 (High sensitivity)"
		], paramNum: 6, size: 1, default: "6", type: "enum"],

	[title: "Brightness Control", description: "The HE-TPS05 can adjust its display brightness automatically depending on the illumination of the ambient environment and also allows to control it manually.",
	 name: "Selected Brightness Level", options: [
				0: "Auto",
				1: "Level 1 (Lowest)",
				2: "Level 2",
				3: "Level 3",
				4: "Level 4",
				5: "Level 5",
				6: "Level 6",
				7: "Level 7",
				8: "Level 8",
				9: "Level 9",
				10: "Level 10 (Highest)"
		], paramNum: 5, size: 1, default: "0", type: "enum"], 

	[title: "Buttons Backlight Color", description: "This parameter defines backlights active state color",
	 name: "Selected Active State Color", options: [
				0: "Red",
				1: "Blue"
		], paramNum: 30, size: 1, default: "1", type: "enum"],

	[title: "Buttons Backlight Control Source", description: "This parameter defines the buttons backlight control source",
	 name: "Backlight 1", options: [
				0: "Disabled",
				1: "Controlled by Touch Button",
				2: "Controlled by Gateway"
		], paramNum: 31, size: 1, default: "1", type: "enum"],

	[name: "Backlight 2", options: [
				0: "Disabled",
				1: "Controlled by Touch Button",
				2: "Controlled by Gateway"
		], paramNum: 32, size: 1, default: "1", type: "enum"],

	[name: "Backlight 3", options: [
				0: "Disabled",
				1: "Controlled by Touch Button",
				2: "Controlled by Gateway"
		], paramNum: 33, size: 1, default: "1", type: "enum"],

	[name: "Backlight 4", options: [
				0: "Disabled",
				1: "Controlled by Touch Button",
				2: "Controlled by Gateway"
		], paramNum: 34, size: 1, default: "1", type: "enum"],

	[name: "Backlight 5", options: [
				0: "Disabled",
				1: "Controlled by Touch Button",
				2: "Controlled by Gateway"
		], paramNum: 35, size: 1, default: "1", type: "enum"],

	[title: "Buttons Hold Control Mode", description: "This Parameter defines how the relay should react while holding the corresponding button. The options are: " +
	"Hold is disabled, Operate like click, " +
	"Momentary Switch: When the button is held, the relay output state is ON, as soon as the button is released the relay output state changes to OFF, " +
	"Reversed Momentary: When the button is held, the relay output state is OFF, as soon as the button is released the relay output state changes to ON, " +
	"Toggle: When the button is held or released the relay output state will toggle its state (ON to OFF or OFF to ON).",
	 name: "Selected Hold Control Mode for Button 1", options: [
				0: "Hold is disabled",
				1: "Operate like click",
				2: "Momentary Switch",
				3: "Reversed Momentary",
				4: "Toggle"
		], paramNum: 41, size: 1, default: "2", type: "enum"],

	[name: "Selected Hold Control Mode for Button 2", options: [
				0: "Hold is disabled",
				1: "Operate like click",
				2: "Momentary Switch",
				3: "Reversed Momentary",
				4: "Toggle"
		], paramNum: 42, size: 1, default: "2", type: "enum"],

	[name: "Selected Hold Control Mode for Button 3", options: [
				0: "Hold is disabled",
				1: "Operate like click",
				2: "Momentary Switch",
				3: "Reversed Momentary",
				4: "Toggle"
		], paramNum: 43, size: 1, default: "2", type: "enum"],

	[name: "Selected Hold Control Mode for Button 4", options: [
				0: "Hold is disabled",
				1: "Operate like click",
				2: "Momentary Switch",
				3: "Reversed Momentary",
				4: "Toggle"
		], paramNum: 44, size: 1, default: "2", type: "enum"],

	[name: "Selected Hold Control Mode for Button 5", options: [
				0: "Hold is disabled",
				1: "Operate like click",
				2: "Momentary Switch",
				3: "Reversed Momentary",
				4: "Toggle"
		], paramNum: 45, size: 1, default: "2", type: "enum"],

	[title: "Buttons Click Control Mode", description: "These Parameters defines how the relay should react when clicking the corresponding button. The options are: " +
	"Click is disabled, Toggle Switch (Relay): relay inverts state (ON to OFF, OFF to ON) according to the relay state, " +
	"Toggle Switch (Backlight): relay inverts state (ON to OFF, OFF to ON) according to the button backlight state, " +
	"Only On: Relay switches to ON state only, " +
	"Only Off: Relay switches to OFF state only, " +
	"Timer: On > Off: Relay output switches to ON state (contacts are closed) then after a specified time switches back to OFF state (contacts are open). The time is specified in 'Relay Timer Mode Duration' below, " +
	"Timer: Off > On: Relay output switches to OFF state (contacts are open) then after a specified time switches back to On state (contacts are closed). The time is specified in 'Relay Timer Mode Duration' below ",
	 name: "Selected Click Control Mode for Button 1", options: [
				0: "Click is disabled",
				1: "Toggle Switch (Relay)",
				2: "Toggle Switch (Backlight)",
				3: "Only On",
				4: "Only Off",
				5: "Timer: On > Off",
				6: "Timer: Off > On"
		], paramNum: 51, size: 1, default: "1", type: "enum"],

	[name: "Selected Click Control Mode for Button 2", options: [
				0: "Click is disabled",
				1: "Toggle Switch (Relay)",
				2: "Toggle Switch (Backlight)",
				3: "Only On",
				4: "Only Off",
				5: "Timer: On > Off",
				6: "Timer: Off > On"
		], paramNum: 52, size: 1, default: "1", type: "enum"],

	[name: "Selected Click Control Mode for Button 3", options: [
				0: "Click is disabled",
				1: "Toggle Switch (Relay)",
				2: "Toggle Switch (Backlight)",
				3: "Only On",
				4: "Only Off",
				5: "Timer: On > Off",
				6: "Timer: Off > On"
		], paramNum: 53, size: 1, default: "1", type: "enum"],

	[name: "Selected Click Control Mode for Button 4", options: [
				0: "Click is disabled",
				1: "Toggle Switch (Relay)",
				2: "Toggle Switch (Backlight)",
				3: "Only On",
				4: "Only Off",
				5: "Timer: On > Off",
				6: "Timer: Off > On"
		], paramNum: 54, size: 1, default: "1", type: "enum"],

	[name: "Selected Click Control Mode for Button 5", options: [
				0: "Click is disabled",
				1: "Toggle Switch (Relay)",
				2: "Toggle Switch (Backlight)",
				3: "Only On",
				4: "Only Off",
				5: "Timer: On > Off",
				6: "Timer: Off > On"
		], paramNum: 55, size: 1, default: "1", type: "enum"],

	[title: "Button Number for Relays Output Control", description: "This parameter defines the relays control source",
	 name: "Selected Relay 1 Control Source", options: [
				0: "Controlled by Gateway",
				1: "Touch Button 1 (Top Left)",
				2: "Touch Button 2 (Top Right)",
				3: "Touch Button 3 (Bottom Left)",
				4: "Touch Button 4 (Bottom Right)",
				5: "Touch Button 5 (Center)"
		], paramNum: 61, size: 1, default: "1", type: "enum"],

	[name: "Selected Relay 1 Control Source", options: [
				0: "Controlled by Gateway",
				1: "Touch Button 1 (Top Left)",
				2: "Touch Button 2 (Top Right)",
				3: "Touch Button 3 (Bottom Left)",
				4: "Touch Button 4 (Bottom Right)",
				5: "Touch Button 5 (Center)"
		], paramNum: 61, size: 1, default: "1", type: "enum"],

	[name: "Selected Relay 2 Control Source", options: [
				0: "Controlled by Gateway",
				1: "Touch Button 1 (Top Left)",
				2: "Touch Button 2 (Top Right)",
				3: "Touch Button 3 (Bottom Left)",
				4: "Touch Button 4 (Bottom Right)",
				5: "Touch Button 5 (Center)"
		], paramNum: 62, size: 1, default: "2", type: "enum"],

	[name: "Selected Relay 3 Control Source", options: [
				0: "Controlled by Gateway",
				1: "Touch Button 1 (Top Left)",
				2: "Touch Button 2 (Top Right)",
				3: "Touch Button 3 (Bottom Left)",
				4: "Touch Button 4 (Bottom Right)",
				5: "Touch Button 5 (Center)"
		], paramNum: 63, size: 1, default: "3", type: "enum"],

	[name: "Selected Relay 4 Control Source", options: [
				0: "Controlled by Gateway",
				1: "Touch Button 1 (Top Left)",
				2: "Touch Button 2 (Top Right)",
				3: "Touch Button 3 (Bottom Left)",
				4: "Touch Button 4 (Bottom Right)",
				5: "Touch Button 5 (Center)"
		], paramNum: 64, size: 1, default: "4", type: "enum"],

	[name: "Selected Relay 5 Control Source", options: [
				0: "Controlled by Gateway",
				1: "Touch Button 1 (Top Left)",
				2: "Touch Button 2 (Top Right)",
				3: "Touch Button 3 (Bottom Left)",
				4: "Touch Button 4 (Bottom Right)",
				5: "Touch Button 5 (Center)"
		], paramNum: 65, size: 1, default: "5", type: "enum"],

	[title: "Relays Timer Mode Duration", description: "These parameters specify the duration in seconds for the Timer modes for Click Control Mode above. " +
	"Press the button and the relay output goes to ON/OFF for the specified time then changes back to OFF/ON. " +
	"If the value is set to “0” the relay output will operate as a short contact (duration is about 0.5 sec)",
	 name: "Selected Relay 1 Timer Mode Duration in seconds", paramNum: 71, size: 2, default: 0, type: "number", min: 0 , max: 43200, unit: "s"],

	[name: "Selected Relay 2 Timer Mode Duration in seconds", paramNum: 72, size: 2, default: 0, type: "number", min: 0 , max: 43200, unit: "s"],

	[name: "Selected Relay 3 Timer Mode Duration in seconds", paramNum: 73, size: 2, default: 0, type: "number", min: 0 , max: 43200, unit: "s"],

	[name: "Selected Relay 4 Timer Mode Duration in seconds", paramNum: 74, size: 2, default: 0, type: "number", min: 0 , max: 43200, unit: "s"],

	[name: "Selected Relay 5 Timer Mode Duration in seconds", paramNum: 75, size: 2, default: 0, type: "number", min: 0 , max: 43200, unit: "s"],

	[title: "Retore Relays State", description: "This parameter determines if the last relay state should be restored after power failure or not. " +
	"These parameters are available on firmware V2.4 or higher",
	 name: "Selected Mode for Relay 1", options: [
				0: "Relay Off After Power Failure",
				1: "Restore Last State"
		], paramNum: 66, size: 1, default: "0", type: "enum"],

	[name: "Selected Mode for Relay 2", options: [
				0: "Relay Off After Power Failure",
				1: "Restore Last State"
		], paramNum: 67, size: 1, default: "0", type: "enum"],

	[name: "Selected Mode for Relay 3", options: [
				0: "Relay Off After Power Failure",
				1: "Restore Last State"
		], paramNum: 68, size: 1, default: "0", type: "enum"],

	[name: "Selected Mode for Relay 4", options: [
				0: "Relay Off After Power Failure",
				1: "Restore Last State"
		], paramNum: 69, size: 1, default: "0", type: "enum"],

	[name: "Selected Mode for Relay 5", options: [
				0: "Relay Off After Power Failure",
				1: "Restore Last State"
		], paramNum: 70, size: 1, default: "0", type: "enum"],

	[title: "Relay Inverse Mode", description: "The values in this Parameter specify the relays that will operate in inverse mode. Relays can operate in an inverse mode in two different ways: " +
	"1. When the first and the second relays are connected to two different external switches. In this case, after pressing a button, the corresponding relay connected to that button will toggle its state (ON to OFF or OFF to ON), and the other relay will be switched OFF. " +
	"2.	When two relays are connected to the same external switch. In this case, the relays will operate in roller shutter mode and their behavior will follow these four cycles: " +
	"a - 1st press of button: the first relay will be switched ON, the second relay will be switched OFF, " +
	"b - 2nd press of button: both relays will be switched OFF, " +
	"c - 3rd press of button: the second relay will be switched ON, the first relay will be switched OFF, " +
	"d - 4th press of button: both relays will be switched OFF. " +
	"≡ Note: In this mode, both relays cannot be switched ON at the same time (i.e. simultaneously). " +
	"≡ Note: Switching OFF one relay will always operate before switching ON another relay to prevent both relays from being ON at the same time.",
	 name: "Group 1", options: [
				0: "Disabled",
				12: "1st & 2nd Relay",
				13: "1st & 3rd Relay",
				14: "1st & 4th Relay",
				15: "1st & 5th Relay",
				23: "2nd & 3rd Relay",
				24: "2nd & 4th Relay",
				25: "2nd & 5th Relay",
				34: "3rd & 4th Relay",
				35: "3rd & 5th Relay",
				45: "4th & 5th Relay"
		], paramNum: 101, size: 1, default: "0", type: "enum"],

	[name: "Group 2", options: [
				0: "Disabled",
				12: "1st & 2nd Relay",
				13: "1st & 3rd Relay",
				14: "1st & 4th Relay",
				15: "1st & 5th Relay",
				23: "2nd & 3rd Relay",
				24: "2nd & 4th Relay",
				25: "2nd & 5th Relay",
				34: "3rd & 4th Relay",
				35: "3rd & 5th Relay",
				45: "4th & 5th Relay"
		], paramNum: 102, size: 1, default: "0", type: "enum"], 

	[title: "Energy Consumption Meter Consecutive Report Interval", description: "When the device is connected to the gateway, it periodically sends reports from its energy consumption sensor even if there is no change in the value. " +
	"This parameter defines the interval between consecutive reports of real time and cumulative energy consumption data to the gateway",
	 name: "Selected Energy Report Interval in minutes", paramNum: 141, size: 1, default: 10, type: "number", min: 1 , max: 120, unit: "min"],

	[title: "Control Energy Meter Report", description: "This Parameter determines if the change in the energy meter will result in a report being sent to the gateway. " +
	"Note: When the device is turning ON, the consumption data will be sent to the gateway once, even if the report is disabled.",
	 name: "Sending Energy Meter Reports", options: [
				0: "Disabled",
				1: "Enabled"
		], paramNum: 142, size: 1, default: "1", type: "enum"],

	[title: "Sensors Consecutive Report Interval", description: "When the device is connected to the gateway, it periodically sends to the gateway reports from its external " +
	"NTC temperature sensor even if there are not changes in the values. This Parameter defines the interval between consecutive reports",
	 name: "Selected Energy Report Interval in minutes", paramNum: 143, size: 1, default: 10, type: "number", min: 1 , max: 120, unit: "min"],

	[title: "Air & Floor Temperature Sensors Report Threshold", description: "This Parameter determines the change in temperature level (in °C) resulting in temperature sensors " +
	"report being sent to the gateway. The value of this Parameter should be x10 for °C, e.g. for 0.4°C use value 4. Use the value 0 if there is a need to stop sending the reports.",
	 name: "Selected Temperature Threshold in °Cx10", paramNum: 144, size: 1, default: 2, type: "number", min: 0 , max: 100, unit: " °Cx10"],

	[title: "Humidity Sensor Report Threshold", description: "This Parameter determines the change in humidity level in % resulting in humidity sensors " +
	"report being sent to the gateway. Use the value 0 if there is a need to stop sending the reports.",
	 name: "Selected Humidity Threshold in %", paramNum: 145, size: 1, default: 2, type: "number", min: 0 , max: 25, unit: "%"],

	[title: "Light Sensor Report Threshold", description: "This Parameter determines the change in the ambient environment illuminance level resulting in a light sensors report " +
	"being sent to the gateway. From 10% to 99% can be selected. Use the value 0 if there is a need to stop sending the reports.",
	 name: "Selected Light Sensor Threshold in %", paramNum: 146, size: 1, default: 50, type: "number", min: 0 , max: 99, unit: "%"]

]}
