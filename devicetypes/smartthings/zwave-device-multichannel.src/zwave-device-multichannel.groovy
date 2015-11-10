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
	definition (name: "Z-Wave Device Multichannel", namespace: "smartthings", author: "SmartThings") {
		capability "Actuator"
		capability "Switch"
		capability "Switch Level"
		capability "Refresh"
		capability "Configuration"
		capability "Sensor"
		capability "Zw Multichannel"

		fingerprint inClusters: "0x60"
		fingerprint inClusters: "0x60, 0x25"
		fingerprint inClusters: "0x60, 0x26"
		fingerprint inClusters: "0x5E, 0x59, 0x60, 0x8E"
	}

	simulator {
		status "on":  "command: 2003, payload: FF"
		status "off": "command: 2003, payload: 00"
		reply "8E010101,delay 800,6007": "command: 6008, payload: 4004"
		reply "8505": "command: 8506, payload: 02"
		reply "59034002": "command: 5904, payload: 8102003101000000"
		reply "6007":  "command: 6008, payload: 0002"
		reply "600901": "command: 600A, payload: 10002532"
		reply "600902": "command: 600A, payload: 210031"
	}

	tiles {
		standardTile("switch", "device.switch", width: 2, height: 2, canChangeIcon: true) {
			state "on", label: '${name}', action: "switch.off", icon: "st.unknown.zwave.device", backgroundColor: "#79b821"
			state "off", label: '${name}', action: "switch.on", icon: "st.unknown.zwave.device", backgroundColor: "#ffffff"
		}
		standardTile("switchOn", "device.switch", inactiveLabel: false, decoration: "flat") {
			state "on", label:'on', action:"switch.on", icon:"st.switches.switch.on"
		}
		standardTile("switchOff", "device.switch", inactiveLabel: false, decoration: "flat") {
			state "off", label:'off', action:"switch.off", icon:"st.switches.switch.off"
		}
		standardTile("refresh", "device.switch", inactiveLabel: false, decoration: "flat") {
			state "default", label:'', action:"refresh.refresh", icon:"st.secondary.refresh"
		}
		controlTile("levelSliderControl", "device.level", "slider", height: 1, width: 3, inactiveLabel: false) {
			state "level", action:"switch level.setLevel"
		}

		main "switch"
		details (["switch", "switchOn", "switchOff", "levelSliderControl", "refresh"])
	}
}

def parse(String description) {
	def result = null
	if (description.startsWith("Err")) {
	    result = createEvent(descriptionText:description, isStateChange:true)
	} else if (description != "updated") {
		def cmd = zwave.parse(description, [0x20: 1, 0x84: 1, 0x98: 1, 0x56: 1, 0x60: 3])
		if (cmd) {
			result = zwaveEvent(cmd)
		}
	}
	log.debug("'$description' parsed to $result")
	return result
}

def zwaveEvent(physicalgraph.zwave.commands.wakeupv1.WakeUpNotification cmd) {
	[ createEvent(descriptionText: "${device.displayName} woke up", isStateChange:true),
	  response(["delay 2000", zwave.wakeUpV1.wakeUpNoMoreInformation().format()]) ]
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd) {
	if (cmd.value == 0) {
		createEvent(name: "switch", value: "off")
	} else if (cmd.value == 255) {
		createEvent(name: "switch", value: "on")
	} else {
		[ createEvent(name: "switch", value: "on"), createEvent(name: "switchLevel", value: cmd.value) ]
	}
}

private List loadEndpointInfo() {
	if (state.endpointInfo) {
		state.endpointInfo
	} else if (device.currentValue("epInfo")) {
		fromJson(device.currentValue("epInfo"))
	} else {
		[]
	}
}

def zwaveEvent(physicalgraph.zwave.commands.multichannelv3.MultiChannelEndPointReport cmd) {
	updateDataValue("endpoints", cmd.endPoints.toString())
	if (!state.endpointInfo) {
		state.endpointInfo = loadEndpointInfo()
	}
	if (state.endpointInfo.size() > cmd.endPoints) {
		cmd.endpointInfo
	}
	state.endpointInfo = [null] * cmd.endPoints
	//response(zwave.associationV2.associationGroupingsGet())
	[ createEvent(name: "epInfo", value: util.toJson(state.endpointInfo), displayed: false, descriptionText:""),
	  response(zwave.multiChannelV3.multiChannelCapabilityGet(endPoint: 1)) ]
}

def zwaveEvent(physicalgraph.zwave.commands.multichannelv3.MultiChannelCapabilityReport cmd) {
	def result = []
	def cmds = []
	if(!state.endpointInfo) state.endpointInfo = []
	state.endpointInfo[cmd.endPoint - 1] = cmd.format()[6..-1]
	if (cmd.endPoint < getDataValue("endpoints").toInteger()) {
		cmds = zwave.multiChannelV3.multiChannelCapabilityGet(endPoint: cmd.endPoint + 1).format()
	} else {
		log.debug "endpointInfo: ${state.endpointInfo.inspect()}"
	}
	result << createEvent(name: "epInfo", value: util.toJson(state.endpointInfo), displayed: false, descriptionText:"")
	if(cmds) result << response(cmds)
	result
}

def zwaveEvent(physicalgraph.zwave.commands.associationv2.AssociationGroupingsReport cmd) {
	state.groups = cmd.supportedGroupings
	if (cmd.supportedGroupings > 1) {
		[response(zwave.associationGrpInfoV1.associationGroupInfoGet(groupingIdentifier:2, listMode:1))]
	}
}

def zwaveEvent(physicalgraph.zwave.commands.associationgrpinfov1.AssociationGroupInfoReport cmd) {
	def cmds = []
	/*for (def i = 0; i < cmd.groupCount; i++) {
		def prof = cmd.payload[5 + (i * 7)]
		def num = cmd.payload[3 + (i * 7)]
		if (prof == 0x20 || prof == 0x31 || prof == 0x71) {
			updateDataValue("agi$num", String.format("%02X%02X", *(cmd.payload[(7*i+5)..(7*i+6)])))
			cmds << response(zwave.multiChannelAssociationV2.multiChannelAssociationSet(groupingIdentifier:num, nodeId:zwaveHubNodeId))
		}
	}*/
	for (def i = 2; i <= state.groups; i++) {
		cmds << response(zwave.multiChannelAssociationV2.multiChannelAssociationSet(groupingIdentifier:i, nodeId:zwaveHubNodeId))
	}
	cmds
}

def zwaveEvent(physicalgraph.zwave.commands.multichannelv3.MultiChannelCmdEncap cmd) {
	def encapsulatedCommand = cmd.encapsulatedCommand([0x32: 3, 0x25: 1, 0x20: 1])
	if (encapsulatedCommand) {
		if (state.enabledEndpoints.find { it == cmd.sourceEndPoint }) {
			def formatCmd = ([cmd.commandClass, cmd.command] + cmd.parameter).collect{ String.format("%02X", it) }.join()
			createEvent(name: "epEvent", value: "$cmd.sourceEndPoint:$formatCmd", isStateChange: true, displayed: false, descriptionText: "(fwd to ep $cmd.sourceEndPoint)")
		} else {
			zwaveEvent(encapsulatedCommand, cmd.sourceEndPoint as Integer)
		}
	}
}

def zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityMessageEncapsulation cmd) {
	def encapsulatedCommand = cmd.encapsulatedCommand([0x20: 1, 0x84: 1])
	if (encapsulatedCommand) {
		state.sec = 1
		def result = zwaveEvent(encapsulatedCommand)
		result = result.collect {
			if (it instanceof physicalgraph.device.HubAction && !it.toString().startsWith("9881")) {
				response(cmd.CMD + "00" + it.toString())
			} else {
				it
			}
		}
		result
	}
}

def zwaveEvent(physicalgraph.zwave.commands.crc16encapv1.Crc16Encap cmd) {
	def versions = [0x31: 2, 0x30: 1, 0x84: 1, 0x9C: 1, 0x70: 2]
	// def encapsulatedCommand = cmd.encapsulatedCommand(versions)
	def version = versions[cmd.commandClass as Integer]
	def ccObj = version ? zwave.commandClass(cmd.commandClass, version) : zwave.commandClass(cmd.commandClass)
	def encapsulatedCommand = ccObj?.command(cmd.command)?.parse(cmd.data)
	if (encapsulatedCommand) {
		zwaveEvent(encapsulatedCommand)
	}
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
	createEvent(descriptionText: "$device.displayName: $cmd", isStateChange: true)
}

def on() {
	commands([zwave.basicV1.basicSet(value: 0xFF), zwave.basicV1.basicGet()])
}

def off() {
	commands([zwave.basicV1.basicSet(value: 0x00), zwave.basicV1.basicGet()])
}

def refresh() {
	command(zwave.basicV1.basicGet())
}

def setLevel(value) {
	commands([zwave.basicV1.basicSet(value: value as Integer), zwave.basicV1.basicGet()], 4000)
}

def configure() {
	commands([
		zwave.multiChannelV3.multiChannelEndPointGet()
	], 800)
}

def epCmd(Integer ep, String cmds) {
	def result
	if (cmds) {
		def header = state.sec ? "988100600D00" : "600D00"
		result = cmds.split(",").collect { cmd -> (cmd.startsWith("delay")) ? cmd : String.format("%s%02X%s", header, ep, cmd) }
	}
	result
}

def enableEpEvents(enabledEndpoints) {
	state.enabledEndpoints = enabledEndpoints.split(",").findAll()*.toInteger()
	null
}

private command(physicalgraph.zwave.Command cmd) {
	if (state.sec) {
		zwave.securityV1.securityMessageEncapsulation().encapsulate(cmd).format()
	} else {
		cmd.format()
	}
}

private commands(commands, delay=200) {
	delayBetween(commands.collect{ command(it) }, delay)
}

private encap(cmd, endpoint) {
	if (endpoint) {
		command(zwave.multiChannelV3.multiChannelCmdEncap(destinationEndPoint:endpoint).encapsulate(cmd))
	} else {
		command(cmd)
	}
}

private encapWithDelay(commands, endpoint, delay=200) {
	delayBetween(commands.collect{ encap(it, endpoint) }, delay)
}
