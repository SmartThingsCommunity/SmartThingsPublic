/**
 *  Copyright 2015 SmartThings
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
	definition (name: "Aeon SmartStrip", namespace: "smartthings", author: "SmartThings") {
		capability "Switch"
		capability "Energy Meter"
		capability "Power Meter"
		capability "Refresh"
		capability "Configuration"
		capability "Actuator"
		capability "Sensor"

		command "reset"

		(1..4).each { n ->
			attribute "switch$n", "enum", ["on", "off"]
			attribute "power$n", "number"
			attribute "energy$n", "number"
			command "on$n"
			command "off$n"
			command "reset$n"
		}

		fingerprint deviceId: "0x1001", inClusters: "0x25,0x32,0x27,0x70,0x85,0x72,0x86,0x60", outClusters: "0x82"
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
	tiles(scale: 2){
		multiAttributeTile(name:"switch", type: "generic", width: 6, height: 4, canChangeIcon: true){
			tileAttribute("device.switch", key: "PRIMARY_CONTROL") {
				attributeState("on", label: '${name}', action: "switch.off", icon: "st.switches.switch.on", backgroundColor: "#00a0dc")
				attributeState("off", label: '${name}', action: "switch.on", icon: "st.switches.switch.off", backgroundColor: "#ffffff")
			}
 		}
		valueTile("power", "device.power", decoration: "flat", width: 2, height: 2) {
			state "default", label:'${currentValue} W'
		}
		valueTile("energy", "device.energy", decoration: "flat", width: 2, height: 2) {
			state "default", label:'${currentValue} kWh'
		}
		standardTile("reset", "device.energy", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "default", label:'reset kWh', action:"reset"
		}
		standardTile("refresh", "device.power", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "default", label:'', action:"refresh.refresh", icon:"st.secondary.refresh"
		}

		(1..4).each { n ->
			standardTile("switch$n", "switch$n", canChangeIcon: true, width: 2, height: 2) {
				state "on", label: '${name}', action: "off$n", icon: "st.switches.switch.on", backgroundColor: "#00a0dc"
				state "off", label: '${name}', action: "on$n", icon: "st.switches.switch.off", backgroundColor: "#ffffff"
			}
			valueTile("power$n", "power$n", decoration: "flat", width: 2, height: 2) {
				state "default", label:'${currentValue} W'
			}
			valueTile("energy$n", "energy$n", decoration: "flat", width: 2, height: 2) {
				state "default", label:'${currentValue} kWh'
			}
		}

		main(["switch", "power", "energy", "switch1", "switch2", "switch3", "switch4"])
		details(["switch","power","energy",
				 "switch1","power1","energy1",
				 "switch2","power2","energy2",
				 "switch3","power3","energy3",
				 "switch4","power4","energy4",
				 "refresh","reset"])
	}
}


def parse(String description) {
	def result = null
	if (description.startsWith("Err")) {
		result = createEvent(descriptionText:description, isStateChange:true)
	} else if (description != "updated") {
		def cmd = zwave.parse(description, [0x60: 3, 0x32: 3, 0x25: 1, 0x20: 1])
		if (cmd) {
			result = zwaveEvent(cmd, null)
		}
	}
	log.debug "parsed '${description}' to ${result.inspect()}"
	result
}

def endpointEvent(endpoint, map) {
	if (endpoint) {
		map.name = map.name + endpoint.toString()
	}
	createEvent(map)
}

def zwaveEvent(physicalgraph.zwave.commands.multichannelv3.MultiChannelCmdEncap cmd, ep) {
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
				log.debug("Ignoring metered outlet $endpoint msg: $encapsulatedCommand")
				[]
			}
		} else {
			zwaveEvent(encapsulatedCommand, cmd.sourceEndPoint as Integer)
		}
	}
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd, endpoint) {
	def map = [name: "switch", type: "physical", value: (cmd.value ? "on" : "off")]
	def events = [endpointEvent(endpoint, map)]
	def cmds = []
	if (endpoint) {
		cmds += delayBetween([2,0].collect { s -> encap(zwave.meterV3.meterGet(scale: s), endpoint) }, 1000)
		if(endpoint < 4) cmds += ["delay 1500", encap(zwave.basicV1.basicGet(), endpoint + 1)]
	} else if (events[0].isStateChange) {
		events += (1..4).collect { ep -> endpointEvent(ep, map.clone()) }
		cmds << "delay 3000"
		cmds += delayBetween((0..4).collect { ep -> encap(zwave.meterV3.meterGet(scale: 2), ep) }, 800)
	}
	if(cmds) events << response(cmds)
	events
}

def zwaveEvent(physicalgraph.zwave.commands.switchbinaryv1.SwitchBinaryReport cmd, endpoint) {
	def map = [name: "switch", value: (cmd.value ? "on" : "off")]
	def events = [endpointEvent(endpoint, map)]
	def cmds = []
	if (!endpoint && events[0].isStateChange) {
		events += (1..4).collect { ep -> endpointEvent(ep, map.clone()) }
		cmds << "delay 3000"
		cmds += delayBetween((1..4).collect { ep -> encap(zwave.meterV3.meterGet(scale: 2), ep) })
	}
	if(cmds) events << response(cmds)
	events
}

def zwaveEvent(physicalgraph.zwave.commands.meterv3.MeterReport cmd, ep) {
	def event = [:]
	def cmds = []
	if (cmd.scale < 2) {
		def val = Math.round(cmd.scaledMeterValue*100)/100.0
		event = endpointEvent(ep, [name: "energy", value: val, unit: ["kWh", "kVAh"][cmd.scale]])
	} else {
		event = endpointEvent(ep, [name: "power", value: Math.round(cmd.scaledMeterValue), unit: "W"])
	}
	if (!ep && event.isStateChange && event.name == "energy") {
		// Total strip energy consumption changed, check individual outlets
		(1..4).each { endpoint ->
			cmds << encap(zwave.meterV2.meterGet(scale: 0), endpoint)
			cmds << "delay 400"
		}
	}
	cmds ? [event, response(cmds)] : event
}

def zwaveEvent(physicalgraph.zwave.commands.manufacturerspecificv2.ManufacturerSpecificReport cmd, ep) {
	updateDataValue("MSR", String.format("%04X-%04X-%04X", cmd.manufacturerId, cmd.productTypeId, cmd.productId))
	null
}

def zwaveEvent(physicalgraph.zwave.Command cmd, ep) {
	log.debug "${device.displayName}: Unhandled ${cmd}" + (ep ? " from endpoint $ep" : "")
}

def onOffCmd(value, endpoint = null) {
	[
		encap(zwave.basicV1.basicSet(value: value), endpoint),
		"delay 500",
		encap(zwave.switchBinaryV1.switchBinaryGet(), endpoint),
		"delay 3000",
		encap(zwave.meterV3.meterGet(scale: 2), endpoint)
	]
}

def on() { onOffCmd(0xFF) }
def off() { onOffCmd(0x0) }

def on1() { onOffCmd(0xFF, 1) }
def on2() { onOffCmd(0xFF, 2) }
def on3() { onOffCmd(0xFF, 3) }
def on4() { onOffCmd(0xFF, 4) }

def off1() { onOffCmd(0, 1) }
def off2() { onOffCmd(0, 2) }
def off3() { onOffCmd(0, 3) }
def off4() { onOffCmd(0, 4) }

def refresh() {
	delayBetween([
		zwave.basicV1.basicGet().format(),
		zwave.meterV3.meterGet(scale: 0).format(),
		zwave.meterV3.meterGet(scale: 2).format(),
		encap(zwave.basicV1.basicGet(), 1)  // further gets are sent from the basic report handler
	])
}

def resetCmd(endpoint = null) {
	delayBetween([
		encap(zwave.meterV2.meterReset(), endpoint),
		encap(zwave.meterV2.meterGet(scale: 0), endpoint)
	])
}

def reset() {
	delayBetween([resetCmd(null), reset1(), reset2(), reset3(), reset4()])
}

def reset1() { resetCmd(1) }
def reset2() { resetCmd(2) }
def reset3() { resetCmd(3) }
def reset4() { resetCmd(4) }

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

private encap(cmd, endpoint) {
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
