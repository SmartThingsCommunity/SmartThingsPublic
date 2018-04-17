/**
 *  Copyright 2018 SmartThings
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
	definition(name: "Z-Wave Dual Switch", namespace: "smartthings", author: "SmartThings") {
		capability "Actuator"
		capability "Health Check"
		capability "Light"
		capability "Refresh"
		capability "Sensor"
		capability "Switch"

		// This DTH uses 2 switch endpoints. Parent DTH controlls endpoint 1 so please use '1' at the end of deviceJoinName
		// Child device (isComponent : false) representing endpoint 2 will substitude 1 with 2 for easier identification.
		fingerprint mfr: "0258", prod: "0003", model: "008B", deviceJoinName: "NEO Coolcam Light Switch 1"
	}

	// tile definitions
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
}

def installed() {
	def componentLabel
	if (device.displayName.endsWith('1')) {
		componentLabel = "${device.displayName[0..-2]}2"
	} else {
		// no '1' at the end of deviceJoinName - use 2 to indicate second switch anyway
		componentLabel = "$device.displayName 2"
	}
	try {
		String dni = "${device.deviceNetworkId}-ep2"
		addChildDevice("Binary Switch Endpoint", dni, device.hub.id,
			[completedSetup: true, label: "${componentLabel}",
			 isComponent   : false, componentName: "ch2", componentLabel: "${componentLabel}"])
		log.debug "Endpoint 2 (Binary Switch Endpoint) added as $componentLabel"
	} catch (e) {
		log.warn "Failed to add endpoint 2 ($desc) as Binary Switch Endpoint - $e"
	}
	configure()
}

def updated() {
	configure()
}

def configure() {
	// Device-Watch simply pings if no device events received for checkInterval duration of 32min = 2 * 15min + 2min lag time
	sendEvent(name: "checkInterval", value: 30 * 60 + 2 * 60, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID, offlinePingable: "1"])
	def commands = []
	if (zwaveInfo.mfr.equals("0258")) {
		commands << zwave.configurationV1.configurationSet(parameterNumber: 4, size: 1, configurationValue: [0]).format()
		commands << "delay 100"
	}
	commands << zwave.basicV1.basicGet().format()
	response(commands)
}

def parse(String description) {
	def result = null
	def cmd = zwave.parse(description, [0x20: 1, 0x84: 1, 0x98: 1, 0x56: 1, 0x60: 3])
	if (cmd) {
		result = zwaveEvent(cmd)
	}
	log.debug("'$description' parsed to $result")
	return createEvent(result)
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

def zwaveEvent(physicalgraph.zwave.commands.multichannelv3.MultiChannelCmdEncap cmd) {
	def encapsulatedCommand = cmd.encapsulatedCommand([0x32: 3, 0x25: 1, 0x20: 1])
	if (cmd.sourceEndPoint == 1) {
		zwaveEvent(encapsulatedCommand)
	} else { // sourceEndPoint == 2
		childDevices[0]?.handleZWave(encapsulatedCommand)
		[:]
	}
}

def zwaveEvent(physicalgraph.zwave.commands.crc16encapv1.Crc16Encap cmd) {
	def versions = [0x31: 2, 0x30: 1, 0x84: 1, 0x9C: 1, 0x70: 2]
	def version = versions[cmd.commandClass as Integer]
	def ccObj = version ? zwave.commandClass(cmd.commandClass, version) : zwave.commandClass(cmd.commandClass)
	def encapsulatedCommand = ccObj?.command(cmd.command)?.parse(cmd.data)
	if (encapsulatedCommand) {
		zwaveEvent(encapsulatedCommand)
	}
	[:]
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
	[descriptionText: "$device.displayName: $cmd", isStateChange: true]
}

def on() {
	// parent DTH conrols endpoint 1
	def endpointNumber = 1
	delayBetween([
		encap(endpointNumber, zwave.switchBinaryV1.switchBinarySet(switchValue: 0xFF)),
		encap(endpointNumber, zwave.switchBinaryV1.switchBinaryGet())
	])
}

def off() {
	// parent DTH conrols endpoint 1
	def endpointNumber = 1
	delayBetween([
		encap(endpointNumber, zwave.switchBinaryV1.switchBinarySet(switchValue: 0x00)),
		encap(endpointNumber, zwave.switchBinaryV1.switchBinaryGet())
	])
}

/**
 * PING is used by Device-Watch in attempt to reach the Device
 * */
def ping() {
	refresh()
}

def refresh() {
	// parent DTH conrols endpoint 1
	def endpointNumber = 1
	encap(endpointNumber, zwave.switchBinaryV1.switchBinaryGet())
}

// sendCommand is called by endpoint 2 child device handler
def sendCommand(endpointDevice, commands) {
	//There is only 1 child device - endpoint 2
	def endpointNumber = 2
	def result
	if (commands instanceof String) {
		commands = commands.split(',') as List
	}
	result = commands.collect { encap(endpointNumber, it) }
	sendHubCommand(result, 100)
}

def encap(endpointNumber, cmd) {
	if (cmd instanceof physicalgraph.zwave.Command) {
		zwave.multiChannelV3.multiChannelCmdEncap(destinationEndPoint: endpointNumber).encapsulate(cmd).format()
	} else if (cmd.startsWith("delay")) {
		cmd
	} else {
		def header = "600D00"
		String.format("%s%02X%s", header, endpointNumber, cmd)
	}
}

