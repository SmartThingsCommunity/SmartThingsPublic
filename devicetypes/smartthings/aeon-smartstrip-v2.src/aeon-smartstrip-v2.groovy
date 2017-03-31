/**
 *  Copyright 2017 SmartThings
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
	definition (name: "Aeon SmartStrip V2", namespace: "smartthings", author: "SmartThings") {
		capability "Switch"
		capability "Energy Meter"
		capability "Power Meter"
		capability "Refresh"
		capability "Configuration"
		capability "Actuator"
		capability "Sensor"

		command "reset"

		fingerprint manufacturer: "0086", prod: "0003", model: "000B", deviceJoinName: "Aeon SmartStrip"
	}

	// simulator metadata
	simulator {
		status "on":  "command: 2003, payload: FF"
		status "off":  "command: 2003, payload: 00"
		status "switch1 on": "command: 600D, payload: 01 00 25 03 FF"
		status "switch1 off": "command: 600D, payload: 01 00 25 03 00"
		status "switch4 on": "command: 600D, payload: 04 00 25 03 FF"
		status "switch4 off": "command: 600D, payload: 04 00 25 03 00"
		status "power": new physicalgraph.zwave.Zwave().meterV1.meterReport(
				scaledMeterValue: 30, precision: 3, meterType: 4, scale: 2, size: 4).incomingMessage()
		status "energy": new physicalgraph.zwave.Zwave().meterV1.meterReport(
				scaledMeterValue: 200, precision: 3, meterType: 0, scale: 0, size: 4).incomingMessage()
		status "power1": "command: 600D, payload: 0100" + new physicalgraph.zwave.Zwave().meterV1.meterReport(
				scaledMeterValue: 30, precision: 3, meterType: 4, scale: 2, size: 4).format()
		status "energy2": "command: 600D, payload: 0200" + new physicalgraph.zwave.Zwave().meterV1.meterReport(
				scaledMeterValue: 200, precision: 3, meterType: 0, scale: 0, size: 4).format()

		// reply messages
		reply "2001FF,delay 100,2502": "command: 2503, payload: FF"
		reply "200100,delay 100,2502": "command: 2503, payload: 00"
	}

	// tile definitions
	tiles {
		multiAttributeTile(name:"switch", type: "lighting", width: 6, height: 4, canChangeIcon: true){
			tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
				attributeState "off", label: '${name}', action: "switch.on", icon: "st.switches.switch.off", backgroundColor: "#ffffff", nextState:"turningOn"
				attributeState "on", label: '${name}', action: "switch.off", icon: "st.switches.switch.on", backgroundColor: "#00A0DC", nextState:"turningOff"
				attributeState "turningOn", label:'${name}', action:"switch.off", icon:"st.switches.switch.on", backgroundColor:"#00A0DC", nextState:"turningOff"
				attributeState "turningOff", label:'${name}', action:"switch.on", icon:"st.switches.switch.off", backgroundColor:"#ffffff", nextState:"turningOn"
			}
		}
		childDeviceTiles("outlets")

		valueTile("power", "device.power", decoration: "flat", width: 2, height: 1) {
			state "default", label:'${currentValue} W'
		}

		valueTile("energy", "device.energy", decoration: "flat", width: 2, height: 1) {
			state "default", label:'${currentValue} kWh'
		}

		standardTile("reset", "device.energy", decoration: "flat") {
			state "default", label:'reset kWh', action:"reset"
		}

		standardTile("refresh", "device.power", decoration: "flat") {
			state "default", label:'', action:"refresh.refresh", icon:"st.secondary.refresh"
		}
	}
}

void installed() {
	createChildDevices()
}

def updated() {
	if (!childDevices) {
		// If here then either the type of an existing device was changed or a device was deleted (only possible in the IDE)
		createChildDevices()
	}
	else if (device.label != state.oldLabel) {
		childDevices.each {
			def newLabel = "${device.displayName} (${channelNumber(it.deviceNetworkId)})"
			it.setLabel(newLabel)
		}
		state.oldLabel = device.label
	}
}

def parse(String description) {
	trace "parse($description)"
	def result = null
	if (description.startsWith("Err")) {
		result = createEvent(descriptionText:description, isStateChange:true)
	} else if (description != "updated") {
		def cmd = zwave.parse(description, [0x60: 3, 0x32: 3, 0x25: 1, 0x20: 1])
		if (cmd) {
			result = zwaveEvent(cmd, null)
		}
	}
	result
}

def zwaveEvent(physicalgraph.zwave.commands.multichannelv3.MultiChannelCmdEncap cmd, ep) {
	trace "zwaveEvent(physicalgraph.zwave.commands.multichannelv3.MultiChannelCmdEncap $cmd, $ep)"
	def encapsulatedCommand = cmd.encapsulatedCommand([0x32: 3, 0x25: 1, 0x20: 1])
	if (encapsulatedCommand) {
		if (encapsulatedCommand.commandClassId == 0x32) {
			// Metered outlets are numbered differently than switches
			Integer endpoint = cmd.sourceEndPoint
			if (endpoint > 2) {
				zwaveEvent(encapsulatedCommand, endpoint - 2)
			} else if (endpoint == 0) {
				zwaveEvent(encapsulatedCommand, 0)
			} else {
				trace("Ignoring metered outlet $endpoint msg: $encapsulatedCommand")
			}
		} else {
			zwaveEvent(encapsulatedCommand, cmd.sourceEndPoint as Integer)
		}
	}
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd, endpoint) {
	trace "zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport $cmd, $endpoint)"
	zwaveBinaryEvent(cmd, endpoint)
}

def zwaveEvent(physicalgraph.zwave.commands.switchbinaryv1.SwitchBinaryReport cmd, endpoint) {
	trace "zwaveEvent(physicalgraph.zwave.commands.switchbinaryv1.SwitchBinaryReport $cmd, $endpoint)"
	zwaveBinaryEvent(cmd, endpoint)
}

void zwaveBinaryEvent(cmd, endpoint) {
	debug "BINARY..zwaveBinaryEvent(value: $cmd.value, $endpoint)"
	def actions = []
	def children = childDevices
	if (endpoint) {
		// Endpoint message for a child device. Send event and issue read for the endpoint and overall power values
		def childDevice = children.find { it.deviceNetworkId.endsWith("$endpoint") }
		childDevice.sendEvent(name: "switch", value: cmd.value ? "on" : "off")
		actions << encap(zwave.meterV3.meterGet(scale: 2), endpoint)
		actions << zwave.meterV3.meterGet(scale: 2).format()

		// Not strictly necessary but done to speed up the UX
		if (cmd.value) {
			// One on and the strip is on
			sendEvent(name: "switch", value: "on")
		} else {
			// All off and the strip is off
			if (!children.any { it.currentValue("switch") == "on" }) {
				sendEvent(name: "switch", value: "off")
			}
		}
	}
	else {
		// Overall event. Send overall event, process children, and issue read for power value
		def value = cmd.value ? "on" : "off"

		// We seem to get main off events when one outlet goes off and others are still on, so need this check
		if (cmd.value) {
			sendEvent(name: "switch", value: value)
		}
		children.each {
			actions << encap(zwave.switchBinaryV1.switchBinaryGet(), channelNumber(it.deviceNetworkId))
		}
		actions << zwave.meterV3.meterGet(scale: 2).format()
	}
	if (actions) {
		// Sending read requests
		log.trace "........actions=$actions"
		sendHubCommand(delayBetween(actions).collect {response(it)})
	}
}

void zwaveEvent(physicalgraph.zwave.commands.meterv3.MeterReport cmd, ep) {
	debug "METER...zwaveEvent(scale: $cmd.scale, value: $cmd.scaledMeterValue, $ep)"
	def actions = []
	def events = []
	if (cmd.scale < 2) {
		def val = Math.round(cmd.scaledMeterValue*100)/100.0
		def event = sendEndpointEvent(ep, [name: "energy", value: val, unit: ["kWh", "kVAh"][cmd.scale]])
		if (!ep && event.isStateChange) {
			// Total strip energy consumption changed, check individual outlets
			(1..4).each { endpoint ->
				actions << encap(zwave.meterV2.meterGet(scale: 0), endpoint)
			}
		}
	} else {
		sendEndpointEvent(ep, [name: "power", value: Math.round(cmd.scaledMeterValue), unit: "W"])
	}

	if (actions) {
		// Sending read requests
		sendHubCommand(delayBetween(actions).collect {response(it)})
	}
}

private Map sendEndpointEvent(endpoint, map) {
	Map data = createEvent(map)
	if (endpoint) {
		def childDevice = childDevices.find{it.deviceNetworkId.endsWith("ep$endpoint")}
		childDevice.sendEvent(data)
		trace "sendEndpointEvent(endpoint:$endpoint, data:$map) for $childDevice"
	}
	else {
		sendEvent(data)
		trace "sendEndpointEvent(endpoint:$endpoint, data:$map) for $device"
	}
	data
}

def zwaveEvent(physicalgraph.zwave.commands.manufacturerspecificv2.ManufacturerSpecificReport cmd, ep) {
	trace "ManufacturerSpecificReport"
	updateDataValue("MSR", String.format("%04X-%04X-%04X", cmd.manufacturerId, cmd.productTypeId, cmd.productId))
	null
}

def zwaveEvent(physicalgraph.zwave.Command cmd, ep) {
	log.warn "${device.displayName}: Unhandled ${cmd}" + (ep ? " from endpoint $ep" : "")
}

def onOffCmd(value, endpoint = null) {
	def actions = [
			encap(zwave.basicV1.basicSet(value: value), endpoint),
			encap(zwave.switchBinaryV1.switchBinaryGet(), endpoint)
	]
	sendHubCommand(delayBetween(actions, 500).collect{response(it)})
}

void childOn(String dni) {
	onOffCmd(0xFF, channelNumber(dni))
}

void childOff(String dni) {
	onOffCmd(0, channelNumber(dni))
}
def on() {
	onOffCmd(0xFF)
}

def off() {
	onOffCmd(0x0)
}

def refresh() {
	def actions = [
			zwave.switchBinaryV1.switchBinaryGet().format(),
	]
	//for (ep in 1..4) {
	//	actions << encap(zwave.switchBinaryV1.switchBinaryGet(), ep)
	//}
	actions << zwave.meterV3.meterGet(scale: 0).format()
	actions << zwave.meterV3.meterGet(scale: 2).format()
	delayBetween(actions, 500)
}

def resetCmd(endpoint = null) {
	def actions = delayBetween([
			encap(zwave.meterV2.meterReset(), endpoint),
			encap(zwave.meterV2.meterGet(scale: 0), endpoint)
	])
}

void childReset(String dni) {
	sendHubCommand(resetCmd(channelNumber(dni)).collect{response(it)})
}

def refreshCmd(endpoint = null) {
	def actions = delayBetween([
			encap(zwave.switchBinaryV1.switchBinaryGet(), endpoint),
			encap(zwave.meterV2.meterGet(scale: 0), endpoint),
			encap(zwave.meterV3.meterGet(scale: 2), endpoint),
	])
	actions
}

void childRefresh(String dni) {
	trace "childRefresh($dni)"
	sendHubCommand(refreshCmd(channelNumber(dni)).collect{response(it)})
}

void reset() {
	def actions = delayBetween((0..4).collect{encap(zwave.meterV2.meterReset(), it)})
	actions << "delay 500"
	actions += delayBetween((0..4).collect{encap(zwave.meterV2.meterGet(scale: 0), it)})
	sendHubCommand(actions.collect{response(it)})
}

def configure() {
	def cmds = [
			zwave.configurationV1.configurationSet(parameterNumber: 101, size: 4, configurationValue: [0, 0, 0, 1]).format(),
			zwave.configurationV1.configurationSet(parameterNumber: 102, size: 4, configurationValue: [0, 0, 0x79, 0]).format(),
			zwave.configurationV1.configurationSet(parameterNumber: 112, size: 4, scaledConfigurationValue: 90).format(),
	]
	[5, 8, 9, 10, 11].each { p ->
		cmds << zwave.configurationV1.configurationSet(parameterNumber: p, size: 2, scaledConfigurationValue: 5).format()
	}
	[12, 15, 16, 17, 18].each { p ->
		cmds << zwave.configurationV1.configurationSet(parameterNumber: p, size: 1, scaledConfigurationValue: 50).format()
	}
	cmds += [
			zwave.configurationV1.configurationSet(parameterNumber: 111, size: 4, scaledConfigurationValue: 15*60).format(),
			zwave.configurationV1.configurationSet(parameterNumber: 4, size: 1, configurationValue: [1]).format(),
	]
	delayBetween(cmds) + "delay 5000" + refresh()
}

Map oldDeviceStates(String dni) {
	def n = channelNumber(dni)
	Map result = [:]
	def switchN = currentState("switch$n")
	if (switchN) {
		result.switch = switchN.value
		result.power = currentState("power$n").value
		result.energy = currentState("energy$n").value
	}
	result
}

private encap(cmd, endpoint = null) {
	if (endpoint) {
		if (cmd.commandClassId == 0x32) {
			// Metered outlets are numbered differently than switches
			if (endpoint < 0x80) {
				endpoint += 2
			} else {
				endpoint = ((endpoint & 0x7F) << 2) | 0x80
			}
		}
		zwave.multiChannelV3.multiChannelCmdEncap(destinationEndPoint:endpoint).encapsulate(cmd).format()
	} else {
		cmd.format()
	}
}

private void createChildDevices() {
	state.oldLabel = device.label
	for (i in 1..4) {
		addChildDevice("Aeon SmartStrip V2 Outlet", "${device.deviceNetworkId}-ep${i}", null,
				[completedSetup: true, label: "${device.displayName} (${i})",
				 isComponent: true, componentName: "ch$i", componentLabel: "Switchable $i"])
	}
}

private channelNumber(String dni) {
	def result = dni.split("-ep")[-1] as Integer
	result
}

private info(msg) {
	//log.info(msg)
}

private debug(msg) {
	log.debug(msg)
}

private trace(msg) {
	//log.trace(msg)
}
