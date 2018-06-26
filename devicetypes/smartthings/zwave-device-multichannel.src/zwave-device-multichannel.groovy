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
		capability "Zw Multichannel" // deprecated

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

	tiles(scale: 2) {
		multiAttributeTile(name:"switch", type: "lighting", width: 6, height: 4, canChangeIcon: true){
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
		childDeviceTiles("endpoints")
		/*standardTile("refresh", "device.switch", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
			state "default", label:'', action:"refresh.refresh", icon:"st.secondary.refresh"
		}*/
	}
}

def installed() {
	def queryCmds = []
	def delay = 200
	def zwInfo = getZwaveInfo()
	def endpointCount = zwInfo.epc as Integer
	def endpointDescList = zwInfo.ep ?: []

	// This is needed until getZwaveInfo() parses the 'ep' field
	if (endpointCount && !zwInfo.ep && device.hasProperty("rawDescription")) {
		try {
			def matcher = (device.rawDescription =~ /ep:(\[.*?\])/)  // extract 'ep' field
			endpointDescList = util.parseJson(matcher[0][1].replaceAll("'", '"'))
		} catch (Exception e) {
			log.warn "couldn't extract ep from rawDescription"
		}
	}

	if (zwInfo.zw.contains("s")) {
		// device was included securely
		state.sec = true
	}

	if (endpointCount > 1 && endpointDescList.size() == 1) {
		// This means all endpoints are identical
		endpointDescList *= endpointCount
	}

	endpointDescList.eachWithIndex { desc, i ->
		def num = i + 1
		if (desc instanceof String && desc.size() >= 4) {
			// desc is in format "1001 AA,BB,CC" where 1001 is the device class and AA etc are the command classes
			// supported by this endpoint
			def parts = desc.split(' ')
			def deviceClass = parts[0]
			def cmdClasses = parts.size() > 1 ? parts[1].split(',') : []
			def typeName = typeNameForDeviceClass(deviceClass)
			def componentLabel = "${typeName} ${num}"
			log.debug "EP #$num d:$deviceClass, cc:$cmdClasses, t:$typeName"
			if (typeName) {
				try {
					String dni = "${device.deviceNetworkId}-ep${num}"
					addChildDevice(typeName, dni, device.hub.id,
							[completedSetup: true, label: "${device.displayName} ${componentLabel}",
							 isComponent: true, componentName: "ch${num}", componentLabel: "${componentLabel}"])
					// enabledEndpoints << num.toString()
					log.debug "Endpoint $num ($desc) added as $componentLabel"
				} catch (e) {
					log.warn "Failed to add endpoint $num ($desc) as $typeName - $e"
				}
			} else {
				log.debug "Endpoint $num ($desc) ignored"
			}
			def cmds = cmdClasses.collect { cc -> queryCommandForCC(cc) }.findAll()
			if (cmds) {
				queryCmds += encapWithDelay(cmds, num) + ["delay 200"]
			}
		}
	}

	response(queryCmds)
}

private typeNameForDeviceClass(String deviceClass) {
	def typeName = null

	switch (deviceClass[0..1]) {
		case "10":
		case "31":
			typeName = "Switch Endpoint"
			break
		case "11":
			typeName = "Dimmer Endpoint"
			break
		case "08":
			//typeName = "Thermostat Endpoint"
			//break
		case "21":
			typeName = "Multi Sensor Endpoint"
			break
		case "20":
		case "A1":
			typeName = "Sensor Endpoint"
			break
	}
	return typeName
}

private queryCommandForCC(cc) {
	switch (cc) {
		case "30":
			return zwave.sensorBinaryV2.sensorBinaryGet(sensorType: 0xFF).format()
		case "71":
			return zwave.notificationV3.notificationSupportedGet().format()
		case "31":
			return zwave.sensorMultilevelV4.sensorMultilevelGet().format()
		case "32":
			return zwave.meterV1.meterGet().format()
		case "8E":
			return zwave.multiChannelAssociationV2.multiChannelAssociationGroupingsGet().format()
		case "85":
			return zwave.associationV2.associationGroupingsGet().format()
		default:
			return null
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

def uninstalled() {
	sendEvent(name: "epEvent", value: "delete all", isStateChange: true, displayed: false, descriptionText: "Delete endpoint devices")
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
		util.parseJson((device.currentValue("epInfo")))
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
		def formatCmd = ([cmd.commandClass, cmd.command] + cmd.parameter).collect{ String.format("%02X", it) }.join()
		if (state.enabledEndpoints.find { it == cmd.sourceEndPoint }) {
			createEvent(name: "epEvent", value: "$cmd.sourceEndPoint:$formatCmd", isStateChange: true, displayed: false, descriptionText: "(fwd to ep $cmd.sourceEndPoint)")
		}
		def childDevice = getChildDeviceForEndpoint(cmd.sourceEndPoint)
		if (childDevice) {
			log.debug "Got $formatCmd for ${childDevice.name}"
			childDevice.handleEvent(formatCmd)
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

// epCmd is part of the deprecated Zw Multichannel capability
def epCmd(Integer ep, String cmds) {
	def result
	if (cmds) {
		def header = state.sec ? "988100600D00" : "600D00"
		result = cmds.split(",").collect { cmd -> (cmd.startsWith("delay")) ? cmd : String.format("%s%02X%s", header, ep, cmd) }
	}
	result
}

// enableEpEvents is part of the deprecated Zw Multichannel capability
def enableEpEvents(enabledEndpoints) {
	state.enabledEndpoints = enabledEndpoints.split(",").findAll()*.toInteger()
	null
}

// sendCommand is called by endpoint child device handlers
def sendCommand(endpointDevice, commands) {
	def result
	if (commands instanceof String) {
		commands = commands.split(',') as List
	}
	def endpoint = deviceEndpointNumber(endpointDevice)
	if (endpoint) {
		log.debug "${endpointDevice.deviceNetworkId} cmd: ${commands}"
		result = commands.collect { cmd ->
			if (cmd.startsWith("delay")) {
				new physicalgraph.device.HubAction(cmd)
			} else {
				new physicalgraph.device.HubAction(encap(cmd, endpoint))
			}
		}
		sendHubCommand(result, 0)
	}
}

private deviceEndpointNumber(device) {
	String dni = device.deviceNetworkId
	if (dni.size() >= 5 && dni[2..3] == "ep") {
		// Old format: 01ep2
		return device.deviceNetworkId[4..-1].toInteger()
	} else if (dni.size() >= 6 && dni[2..4] == "-ep") {
		// New format: 01-ep2
		return device.deviceNetworkId[5..-1].toInteger()
	} else {
		log.warn "deviceEndpointNumber() expected 'XX-epN' format for dni of $device"
	}
}

private getChildDeviceForEndpoint(Integer endpoint) {
	def children = childDevices
	if (children && endpoint) {
		return children.find{ it.deviceNetworkId.endsWith("ep$endpoint") }
	}
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
		if (cmd instanceof physicalgraph.zwave.Command) {
			command(zwave.multiChannelV3.multiChannelCmdEncap(destinationEndPoint:endpoint).encapsulate(cmd))
		} else {
			// If command is already formatted, we can't use the multiChannelCmdEncap class
			def header = state.sec ? "988100600D00" : "600D00"
			String.format("%s%02X%s", header, endpoint, cmd)
		}
	} else {
		command(cmd)
	}
}

private encapWithDelay(commands, endpoint, delay=200) {
	delayBetween(commands.collect{ encap(it, endpoint) }, delay)
}
