/**
 *  HELTUN RS01 Switch
 *
 *  Copyright 2021 Sarkis Kabrailian
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
metadata {
	definition (name: "HELTUN RS01 Switch", namespace: "HELTUN", author: "Sarkis Kabrailian", cstHandler: true, mcdSync: true, ocfDeviceType: "oic.d.switch") {
		capability "Switch"
		capability "Energy Meter"
		capability "Power Meter"
		capability "Configuration"
		capability "Health Check"
		capability "Refresh"

		fingerprint mfr: "0344", prod: "0004", model: "0009", deviceJoinName: "HELTUN"
	}
	preferences {
		input (
			title: "HE-RS01 | HELTUN Relay Switch",
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
	if ( needConfig ) {
		configParam()
	}
}

private configParam() {
	def cmds = []
	for (parameter in parameterMap()) {
		if ( state."$parameter.name"?.value != null && state."$parameter.name"?.state in ["notConfigured", "defNotConfigured"] ) {
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

def zwaveEvent(physicalgraph.zwave.commands.configurationv2.ConfigurationReport cmd) {
	def parameter = parameterMap().find( {it.paramNum == cmd.parameterNumber } ).name
	if (state."$parameter".value == cmd.scaledConfigurationValue) {
		state."$parameter".state = "configured"
	}
	else {
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
	state.oldLabel = device.label
	def existingChildren = getChildDevices()
	for (i in 1..numberOfButtons) {
		def buttonNetworkId = "${device.deviceNetworkId}:${i+10}"
		def relayNetworkId = "${device.deviceNetworkId}:${i}"
		def childRelayExists = (existingChildren.find {child -> child.getDeviceNetworkId() == relayNetworkId} != NULL)
		def childButtonExists = (existingChildren.find {child -> child.getDeviceNetworkId() == buttonNetworkId} != NULL)
		if (!childRelayExists) {
			addChildDevice("HELTUN", "Heltun Child Relay", relayNetworkId, device.hubId,[completedSetup: true, label: getChildName(i), isComponent: false])
		}
		if (!childButtonExists ) {
			def child = addChildDevice("smartthings", "Child Button", buttonNetworkId, device.hubId, [completedSetup: true, label: getChildName(i+10), isComponent: true, componentName: "button$i", componentLabel: "Button ${i}"])
		}
	}
	initialize()
}

private getChildName(channelNumber) {
	if (channelNumber in 1..5) {
		return "${device.displayName} " + "${"Switch"} " + "${channelNumber}"
	}
	else if (channelNumber in 11..16) {
		return "${device.displayName} " + "${"Button"} " + "${channelNumber-10}"
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
	def map = [
		encap(zwave.basicV1.basicSet(value: value), endpoint),
		encap(zwave.switchBinaryV1.switchBinaryGet(), endpoint),
	]
	sendHubCommand(map, 500)
}

private encap(cmd, endpoint) {
	if (endpoint) {
		zwave.multiChannelV3.multiChannelCmdEncap(destinationEndPoint:endpoint).encapsulate(cmd).format()
	} else {
		cmd.format()
	}
}

def on() {
	def map = [
		encap(zwave.basicV1.basicSet(value: 0xFF), 0xFF),
		encap(zwave.switchBinaryV1.switchBinaryGet(), 1),
		encap(zwave.switchBinaryV1.switchBinaryGet(), 2),
		encap(zwave.switchBinaryV1.switchBinaryGet(), 3),
		encap(zwave.switchBinaryV1.switchBinaryGet(), 4),
		encap(zwave.switchBinaryV1.switchBinaryGet(), 5)
	]
	sendHubCommand(map, 100)
}

def off() {
	def map = [
		encap(zwave.basicV1.basicSet(value: 0), 0xFF),
		encap(zwave.switchBinaryV1.switchBinaryGet(), 1),
		encap(zwave.switchBinaryV1.switchBinaryGet(), 2),
		encap(zwave.switchBinaryV1.switchBinaryGet(), 3),
		encap(zwave.switchBinaryV1.switchBinaryGet(), 4),
		encap(zwave.switchBinaryV1.switchBinaryGet(), 5)
	]
	sendHubCommand(map, 100)
}

def childOn(childId) {
	setState(0xFF, channelNumber(childId))
}

def childOff(childId) {
	setState(0, channelNumber(childId))
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
	def state
	def buttonN
	switch (cmd.keyAttributes as Integer) {
		case 0:
			state = "pushed"
			buttonN = cmd.sceneNumber
			break
		case 1: 
			state = "up"
			buttonN = cmd.sceneNumber
			break
		case 2: 
			state = "held"
			buttonN = cmd.sceneNumber
			break
	}
	if (buttonN) {
		def buttonId = buttonN + 10
		def child = childDevices?.find {channelNumber(it.deviceNetworkId) == buttonId }
		child?.sendEvent([name: "button", value: state, data: [buttonNumber: 1], isStateChange: true])
	}
}

def zwaveEvent(physicalgraph.zwave.commands.multichannelv3.MultiChannelCmdEncap cmd) {
	def endPoint = cmd.sourceEndPoint
	def encapsulatedCommand = cmd.encapsulatedCommand([0x32: 3, 0x25: 1, 0x20: 1])
	def value = encapsulatedCommand.value
	def childDevice = childDevices?.find {channelNumber(it.deviceNetworkId) == endPoint }
	def corrRelCons = 0
	def corRelParam = 11 + endPoint
	def param = parameterMap().find( {it.paramNum == corRelParam } ).name
	def paramState = state."$param"
	if (paramState){
		corrRelCons = paramState.value
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
	if ((time.hour != cmd.hour) || (time.minute != cmd.minute) || (time.weekday != cmd.weekday)){
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

def refresh() {
	def cmds = []
	for (i in 1..5){
		cmds << encap(zwave.switchBinaryV1.switchBinaryGet(), i)
	}
	cmds << zwave.clockV1.clockGet().format()
	cmds << zwave.multiChannelAssociationV2.multiChannelAssociationGet(groupingIdentifier: 1).format()
	sendHubCommand(cmds, 1200)
	runIn(15, "checkParam")
}

def ping() {
	refresh()
}

def resetEnergyMeter() {
	sendHubCommand(zwave.meterV3.meterReset().format())
}

private parameterMap() {[  
	[
		title: "Relays Output Mode", description: "These Parameters determine the type of loads connected to the device relay outputs. The output type can be NO – normal open (no contact/voltage switch the load OFF) or NC - normal close (output is contacted / there is a voltage to switch the load OFF)", name: "Selected Relay 1 Mode", 
		options: [
			0: "NO - Normal Open",
			1: "NC - Normal Close"
		], paramNum: 7, size: 1, default: "0", type: "enum"
	],
	[
		name: "Selected Relay 2 Mode", 
		options: [
			0: "NO - Normal Open",
			1: "NC - Normal Close"
		], paramNum: 8, size: 1, default: "0", type: "enum"
	],
	[
		name: "Selected Relay 3 Mode", 
		options: [
			0: "NO - Normal Open",
			1: "NC - Normal Close"
		], paramNum: 9, size: 1, default: "0", type: "enum"
	],
	[
		name: "Selected Relay 4 Mode", 
		options: [
			0: "NO - Normal Open",
			1: "NC - Normal Close"
		], paramNum: 10, size: 1, default: "0", type: "enum"
	],
	[
		name: "Selected Relay 5 Mode", 
		options: [
			0: "NO - Normal Open",
			1: "NC - Normal Close"
		], paramNum: 11, size: 1, default: "0", type: "enum"
	],
	[
		title: "Relays Load Power", description: "These parameters are used to specify the loads power that are connected to the device outputs (Relays). Using your connected device’s power consumption specification (see associated owner’s manual), set the load in Watts for the outputs bellow:",
		name: "Selected Relay 1 Load Power in Watts", paramNum: 12, size: 2, default: 0, type: "number", min: 0, max: 1100, unit: "W"
	],
	[
		name: "Selected Relay 2 Load Power in Watts", paramNum: 13, size: 2, default: 0, type: "number", min: 0, max: 1100, unit: "W"
	],
	[
		name: "Selected Relay 3 Load Power in Watts", paramNum: 14, size: 2, default: 0, type: "number", min: 0, max: 1100, unit: "W"
	],
	[
		name: "Selected Relay 4 Load Power in Watts", paramNum: 15, size: 2, default: 0, type: "number", min: 0, max: 1100, unit: "W"
	],
	[
		name: "Selected Relay 5 Load Power in Watts", paramNum: 16, size: 2, default: 0, type: "number", min: 0, max: 1100, unit: "W"
	],
	[
		title: "Hold Control Mode for external inputs S1-S5", description: "This Parameter defines how the relay should react while holding the button connected to the corresponding external input. The options are: Hold is disabled, Operate like click, Momentary Switch: When the button is held, the relay output state is ON, as soon as the button is released the relay output state changes to OFF, Reversed Momentary: When the button is held, the relay output state is OFF, as soon as the button is released the relay output state changes to ON, Toggle: When the button is held or released the relay output state will toggle its state (ON to OFF or OFF to ON).", name: "Selected Hold Control Mode for S1",
		options: [
			0: "Hold is disabled",
			1: "Operate like click",
			2: "Momentary Switch",
			3: "Reversed Momentary",
			4: "Toggle"
		], paramNum: 41, size: 1, default: "2", type: "enum"
	],
	[
		name: "Selected Hold Control Mode for S2",
		options: [
			0: "Hold is disabled",
			1: "Operate like click",
			2: "Momentary Switch",
			3: "Reversed Momentary",
			4: "Toggle"
		], paramNum: 42, size: 1, default: "2", type: "enum"
	],
	[
		name: "Selected Hold Control Mode for S3", 
		options: [
			0: "Hold is disabled",
			1: "Operate like click",
			2: "Momentary Switch",
			3: "Reversed Momentary",
			4: "Toggle"
		], paramNum: 43, size: 1, default: "2", type: "enum"
	],
	[
		name: "Selected Hold Control Mode for S4", 
		options: [
			0: "Hold is disabled",
			1: "Operate like click",
			2: "Momentary Switch",
			3: "Reversed Momentary",
			4: "Toggle"
		], paramNum: 44, size: 1, default: "2", type: "enum"
	],
	[
		name: "Selected Hold Control Mode for S5",
		options: [
			0: "Hold is disabled",
			1: "Operate like click",
			2: "Momentary Switch",
			3: "Reversed Momentary",
			4: "Toggle"
		], paramNum: 45, size: 1, default: "2", type: "enum"
	],
	[
		title: "Hold Mode Duration for External Inputs S1-S5", description: "These Parameters specify the time the device needs to recognize a hold mode when the button connected to an external input is held (key closed). These parameters are available on firmware V1.4 or higher",
		name: "Selected Duration for S1 in milliseconds", paramNum: 46, size: 2, default: 500, type: "number", min: 200 , max: 5000, unit: "ms"
	],
	[
		name: "Selected Duration for S2 in milliseconds", paramNum: 47, size: 2, default: 500, type: "number", min: 200 , max: 5000, unit: "ms"
	],
	[
		name: "Selected Duration for S3 in milliseconds", paramNum: 48, size: 2, default: 500, type: "number", min: 200 , max: 5000, unit: "ms"
	],
	[
		name: "Selected Duration for S4 in milliseconds", paramNum: 49, size: 2, default: 500, type: "number", min: 200 , max: 5000, unit: "ms"
	],
	[
		name: "Selected Duration for S5 in milliseconds", paramNum: 50, size: 2, default: 500, type: "number", min: 200 , max: 5000, unit: "ms"
	],
	[
		title: "Click control mode for external inputs S1-S5", description: "These Parameters defines how the relay should react when clicking the button connected to the corresponding external input. The options are: Click is disabled, Toggle switch: relay inverts state (ON to OFF, OFF to ON), Only On: Relay switches to ON state only, Only Off: Relay switches to OFF state only, Timer: On > Off: Relay output switches to ON state (contacts are closed) then after a specified time switches back to OFF state (contacts are open). The time is specified in 'Relay Timer Mode Duration' below, Timer: Off > On: Relay output switches to OFF state (contacts are open) then after a specified time switches back to On state (contacts are closed). The time is specified in 'Relay Timer Mode Duration' below ", name: "Selected Click Control Mode for S1",
		options: [
			0: "Click is disabled",
			1: "Toggle Switch",
			2: "Only On",
			3: "Only Off",
			4: "Timer: On > Off",
			5: "Timer: Off > On"
		], paramNum: 51, size: 1, default: "1", type: "enum"
	],
	[
		name: "Selected Click Control Mode for S2", 
		options: [
			0: "Click is disabled",
			1: "Toggle Switch",
			2: "Only On",
			3: "Only Off",
			4: "Timer: On > Off",
			5: "Timer: Off > On"
		], paramNum: 52, size: 1, default: "1", type: "enum"
	],
	[
		name: "Selected Click Control Mode for S3", 
		options: [
			0: "Click is disabled",
			1: "Toggle Switch",
			2: "Only On",
			3: "Only Off",
			4: "Timer: On > Off",
			5: "Timer: Off > On"
		], paramNum: 53, size: 1, default: "1", type: "enum"
	],
	[
		name: "Selected Click Control Mode for S4", options: [
			0: "Click is disabled",
			1: "Toggle Switch",
			2: "Only On",
			3: "Only Off",
			4: "Timer: On > Off",
			5: "Timer: Off > On"
		], paramNum: 54, size: 1, default: "1", type: "enum"
	],
	[
		name: "Selected Click Control Mode for S5", 
		options: [
			0: "Click is disabled",
			1: "Toggle Switch",
			2: "Only On",
			3: "Only Off",
			4: "Timer: On > Off",
			5: "Timer: Off > On"
		], paramNum: 55, size: 1, default: "1", type: "enum"
	],
	[
		title: "Relays Timer Mode Duration", description: "These parameters specify the duration in seconds for the Timer modes for Click Control Mode above. Press the button and the relay output goes to ON/OFF for the specified time then changes back to OFF/ON. If the value is set to “0” the relay output will operate as a short contact (duration is about 0.5 sec)",
		name: "Selected Relay 1 Timer Mode Duration in seconds", paramNum: 71, size: 2, default: 0, type: "number", min: 0 , max: 43200, unit: "s"
	],
	[
		name: "Selected Relay 2 Timer Mode Duration in seconds", paramNum: 72, size: 2, default: 0, type: "number", min: 0 , max: 43200, unit: "s"
	],
	[
		name: "Selected Relay 3 Timer Mode Duration in seconds", paramNum: 73, size: 2, default: 0, type: "number", min: 0 , max: 43200, unit: "s"
	],
	[
		name: "Selected Relay 4 Timer Mode Duration in seconds", paramNum: 74, size: 2, default: 0, type: "number", min: 0 , max: 43200, unit: "s"
	],
	[
		name: "Selected Relay 5 Timer Mode Duration in seconds", paramNum: 75, size: 2, default: 0, type: "number", min: 0 , max: 43200, unit: "s"
	],
	[
		title: "External Input Number for Relays Output Control", description: "These Parameters defines the relays control source.", name: "Selected Relay 1 Control Source", 
		options: [
			0: "Controlled by gateway",
			1: "Controlled by S1",
			2: "Controlled by S2",
			3: "Controlled by S3",
			4: "Controlled by S4",
			5: "Controlled by S5"
		], paramNum: 61, size: 1, default: "1", type: "enum"
	],
	[
		name: "Selected Relay 2 Control Source", 
		options: [
			0: "Controlled by gateway",
			1: "Controlled by S1",
			2: "Controlled by S2",
			3: "Controlled by S3",
			4: "Controlled by S4",
			5: "Controlled by S5"
		], paramNum: 62, size: 1, default: "2", type: "enum"
	],
	[
		name: "Selected Relay 3 Control Source", 
		options: [
			0: "Controlled by gateway",
			1: "Controlled by S1",
			2: "Controlled by S2",
			3: "Controlled by S3",
			4: "Controlled by S4",
			5: "Controlled by S5"
		], paramNum: 63, size: 1, default: "3", type: "enum"
	],
	[
		name: "Selected Relay 4 Control Source", 
		options: [
			0: "Controlled by gateway",
			1: "Controlled by S1",
			2: "Controlled by S2",
			3: "Controlled by S3",
			4: "Controlled by S4",
			5: "Controlled by S5"
		], paramNum: 64, size: 1, default: "4", type: "enum"
	],
	[
		name: "Selected Relay 5 Control Source", 
		options: [
			0: "Controlled by gateway",
			1: "Controlled by S1",
			2: "Controlled by S2",
			3: "Controlled by S3",
			4: "Controlled by S4",
			5: "Controlled by S5"
		], paramNum: 65, size: 1, default: "5", type: "enum"
	],
	[
		title: "Retore Relays State", description: "This parameter determines if the last relay state should be restored after power failure or not. These parameters are available on firmware V1.4 or higher", name: "Selected Mode for Relay 1", 
		options: [
			0: "Relay Off After Power Failure",
			1: "Restore Last State"
		], paramNum: 66, size: 1, default: "0", type: "enum"
	],
	[
		name: "Selected Mode for Relay 2", 
		options: [
			0: "Relay Off After Power Failure",
			1: "Restore Last State"
		], paramNum: 67, size: 1, default: "0", type: "enum"
	],
	[
		name: "Selected Mode for Relay 3", 
		options: [
			0: "Relay Off After Power Failure",
			1: "Restore Last State"
		], paramNum: 68, size: 1, default: "0", type: "enum"
	],
	[
		name: "Selected Mode for Relay 4", 
		options: [
			0: "Relay Off After Power Failure",
			1: "Restore Last State"
		], paramNum: 69, size: 1, default: "0", type: "enum"
	],
	[
		name: "Selected Mode for Relay 5", 
		options: [
			0: "Relay Off After Power Failure",
			1: "Restore Last State"
		], paramNum: 70, size: 1, default: "0", type: "enum"
	],
	[
		title: "Relay Inverse Mode", description: "The values in this Parameter specify the relays that will operate in inverse mode. Relays can operate in an inverse mode in two different ways: 1. When the first and the second relays are connected to two different external switches. In this case, after pressing a button, the corresponding relay connected to that button will toggle its state (ON to OFF or OFF to ON), and the other relay will be switched OFF. 2.	When two relays are connected to the same external switch. In this case, the relays will operate in roller shutter mode and their behavior will follow these four cycles: a - 1st press of button: the first relay will be switched ON, the second relay will be switched OFF, b - 2nd press of button: both relays will be switched OFF, c - 3rd press of button: the second relay will be switched ON, the first relay will be switched OFF, d - 4th press of button: both relays will be switched OFF. ≡ Note: In this mode, both relays cannot be switched ON at the same time (i.e. simultaneously). ≡ Note: Switching OFF one relay will always operate before switching ON another relay to prevent both relays from being ON at the same time.", name: "Group 1", 
		options: [
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
		], paramNum: 101, size: 1, default: "0", type: "enum"
	],
	[
		name: "Group 2", 
		options: [
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
		], paramNum: 102, size: 1, default: "0", type: "enum"
	],
	[
		title: "Energy Consumption Meter Consecutive Report Interval", description: "When the device is connected to the gateway, it periodically sends reports from its energy consumption sensor even if there is no change in the value. This parameter defines the interval between consecutive reports of real time and cumulative energy consumption data to the gateway",
		name: "Selected Energy Report Interval in minutes", paramNum: 141, size: 1, default: 10, type: "number", min: 1 , max: 120, unit: "min"
	],
	[
		title: "Control Energy Meter Report", description: "This Parameter determines if the change in the energy meter will result in a report being sent to the gateway. Note: When the device is turning ON, the consumption data will be sent to the gateway once, even if the report is disabled.", name: "Sending Energy Meter Reports", 
		options: [
			0: "Disabled",
			1: "Enabled"
		], paramNum: 142, size: 1, default: "1", type: "enum"
	]
]}