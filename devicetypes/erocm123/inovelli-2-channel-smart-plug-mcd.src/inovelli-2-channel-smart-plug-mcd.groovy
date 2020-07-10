/**
 *
 *  Inovelli 2-Channel Smart Plug MCD
 *
 *  Copyright 2020 SmartThings
 *
 *  Original integration:
 *  github: Eric Maycock (erocm123)
 *  Date: 2017-04-27
 *  Copyright Eric Maycock
 *
 *  Includes all configuration parameters and ease of advanced configuration.
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
	definition(name: "Inovelli 2-Channel Smart Plug MCD", namespace: "erocm123", author: "Eric Maycock", ocfDeviceType: "oic.d.smartplug", mcdSync: true) {
		capability "Actuator"
		capability "Sensor"
		capability "Switch"
		capability "Polling"
		capability "Refresh"
		capability "Health Check"

		fingerprint manufacturer: "015D", prod: "0221", model: "251C", deviceJoinName: "Show Home Outlet" // Show Home 2-Channel Smart Plug
		fingerprint manufacturer: "0312", prod: "0221", model: "251C", deviceJoinName: "Inovelli Outlet" // Inovelli 2-Channel Smart Plug
		fingerprint manufacturer: "0312", prod: "B221", model: "251C", deviceJoinName: "Inovelli Outlet" // Inovelli 2-Channel Smart Plug
		fingerprint manufacturer: "0312", prod: "0221", model: "611C", deviceJoinName: "Inovelli Outlet" // Inovelli 2-Channel Outdoor Smart Plug
		fingerprint manufacturer: "015D", prod: "0221", model: "611C", deviceJoinName: "Inovelli Outlet" // Inovelli 2-Channel Outdoor Smart Plug
		fingerprint manufacturer: "015D", prod: "6100", model: "6100", deviceJoinName: "Inovelli Outlet" // Inovelli 2-Channel Outdoor Smart Plug
		fingerprint manufacturer: "0312", prod: "6100", model: "6100", deviceJoinName: "Inovelli Outlet" // Inovelli 2-Channel Outdoor Smart Plug
		fingerprint manufacturer: "015D", prod: "2500", model: "2500", deviceJoinName: "Inovelli Outlet" // Inovelli 2-Channel Smart Plug w/Scene
	}
	simulator {}
	preferences {}
	tiles {
		multiAttributeTile(name: "switch", type: "lighting", width: 6, height: 4, canChangeIcon: true) {
			tileAttribute("device.switch", key: "PRIMARY_CONTROL") {
				attributeState "off", label: '${name}', action: "switch.on", icon: "st.switches.switch.off", backgroundColor: "#ffffff", nextState: "turningOn"
				attributeState "on", label: '${name}', action: "switch.off", icon: "st.switches.switch.on", backgroundColor: "#00a0dc", nextState: "turningOff"
				attributeState "turningOff", label: '${name}', action: "switch.on", icon: "st.switches.switch.off", backgroundColor: "#ffffff", nextState: "turningOn"
				attributeState "turningOn", label: '${name}', action: "switch.off", icon: "st.switches.switch.on", backgroundColor: "#00a0dc", nextState: "turningOff"
			}
		}
		standardTile("refresh", "device.switch", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "default", label: "", action: "refresh.refresh", icon: "st.secondary.refresh"
		}
		main(["switch"])
		details(["switch",
				 childDeviceTiles("all"), "refresh"
		])
	}
}

def parse(String description) {
	def result = []
	def cmd = zwave.parse(description)

	if (cmd) {
		result += zwaveEvent(cmd)
		log.debug "Parsed ${cmd} to ${result.inspect()}"
	} else {
		log.debug "Non-parsed event: ${description}"
	}

	return result
}

def handleSwitchEndpointEvent(cmd, ep) {
	def event
	def childDevice = childDevices.find { it.deviceNetworkId == "$device.deviceNetworkId:$ep" }

	childDevice?.sendEvent(name: "switch", value: cmd.value ? "on" : "off")

	if (cmd.value) {
		event = [createEvent([name: "switch", value: "on"])]
	} else {
		def allOff = true

		childDevices.each { n ->
			if (n.currentState("switch")?.value != "off")
				allOff = false
		}

		if (allOff) {
			event = [createEvent([name: "switch", value: "off"])]
		} else {
			event = [createEvent([name: "switch", value: "on"])]
		}
	}

	return event
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd, ep = null) {
	log.debug "BasicReport ${cmd} - outlet ${ep}"

	if (ep) {
		return handleSwitchEndpointEvent(cmd, ep)
	}
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicSet cmd) {
	log.debug "BasicSet ${cmd}"
	def result = createEvent(name: "switch", value: cmd.value ? "on" : "off")
	def cmds = []

	cmds << encap(zwave.switchBinaryV1.switchBinaryGet(), 1)
	cmds << encap(zwave.switchBinaryV1.switchBinaryGet(), 2)

	return [result, response(commands(cmds))]
}

def zwaveEvent(physicalgraph.zwave.commands.switchbinaryv1.SwitchBinaryReport cmd, ep = null) {
	log.debug "SwitchBinaryReport ${cmd} - outlet ${ep}"

	if (ep) {
		return handleSwitchEndpointEvent(cmd, ep)
	} else {
		def result = createEvent(name: "switch", value: cmd.value ? "on" : "off")
		def cmds = []

		cmds << encap(zwave.switchBinaryV1.switchBinaryGet(), 1)
		cmds << encap(zwave.switchBinaryV1.switchBinaryGet(), 2)

		return [result, response(commands(cmds))]
	}
}

def zwaveEvent(physicalgraph.zwave.commands.multichannelv3.MultiChannelCmdEncap cmd) {
	if (cmd.commandClass == 0x6C && cmd.parameter.size >= 4) { // Supervision encapsulated Message
		// Supervision header is 4 bytes long, two bytes dropped here are the latter two bytes of the supervision header
		cmd.parameter = cmd.parameter.drop(2)
		// Updated Command Class/Command now with the remaining bytes
		cmd.commandClass = cmd.parameter[0]
		cmd.command = cmd.parameter[1]
		cmd.parameter = cmd.parameter.drop(2)
	}
	log.debug "MultiChannelCmdEncap ${cmd}"
	def encapsulatedCommand = cmd.encapsulatedCommand([0x32: 3, 0x25: 1, 0x20: 1])

	if (encapsulatedCommand) {
		zwaveEvent(encapsulatedCommand, cmd.sourceEndPoint as Integer)
	}
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
	// This will capture any commands not handled by other instances of zwaveEvent
	// and is recommended for development so you can see every command the device sends
	log.debug "Unhandled Event: ${cmd}"
}

def on() {
	log.debug "on()"

	commands([
		zwave.switchAllV1.switchAllOn(),
		encap(zwave.switchBinaryV1.switchBinaryGet(), 1),
		encap(zwave.switchBinaryV1.switchBinaryGet(), 2)
	])
}

def off() {
	log.debug "off()"

	commands([
		zwave.switchAllV1.switchAllOff(),
		encap(zwave.switchBinaryV1.switchBinaryGet(), 1),
		encap(zwave.switchBinaryV1.switchBinaryGet(), 2)
	])
}

void childOn(String dni) {
	log.debug "childOn($dni)"
	def cmds = []

	cmds << new physicalgraph.device.HubAction(command(encap(zwave.basicV1.basicSet(value: 0xFF), channelNumber(dni))))
	cmds << new physicalgraph.device.HubAction(command(encap(zwave.switchBinaryV1.switchBinaryGet(), channelNumber(dni))))

	sendHubCommand(cmds, 1000)
}

void childOff(String dni) {
	log.debug "childOff($dni)"
	def cmds = []

	cmds << new physicalgraph.device.HubAction(command(encap(zwave.basicV1.basicSet(value: 0x00), channelNumber(dni))))
	cmds << new physicalgraph.device.HubAction(command(encap(zwave.switchBinaryV1.switchBinaryGet(), channelNumber(dni))))

	sendHubCommand(cmds, 1000)
}

void childRefresh(String dni) {
	log.debug "childRefresh($dni)"
	def cmds = []

	cmds << new physicalgraph.device.HubAction(command(encap(zwave.switchBinaryV1.switchBinaryGet(), channelNumber(dni))))

	sendHubCommand(cmds, 1000)
}

def poll() {
	log.debug "poll()"

	refresh()
}

def refresh() {
	log.debug "refresh()"

	commands([
		encap(zwave.switchBinaryV1.switchBinaryGet(), 1),
		encap(zwave.switchBinaryV1.switchBinaryGet(), 2),
	])
}

def ping() {
	log.debug "ping()"

	refresh()
}

def getCheckInterval() {
	2 * 15 * 60 + 2 * 60
}

def installed() {
	log.debug "installed()"

	sendEvent(name: "checkInterval", value: checkInterval, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])

	createChildDevices()
	response(refresh())
}

def updated() {
	log.debug "updated()"

	if (!childDevices) {
		createChildDevices()
	} else if (device.label != state.oldLabel) {
		childDevices.each {
			it.setLabel("${device.displayName} Outlet ${channelNumber(it.deviceNetworkId)}")
		}
		state.oldLabel = device.label
	}
}

def zwaveEvent(physicalgraph.zwave.commands.configurationv2.ConfigurationReport cmd) {
	log.debug "${device.displayName} parameter '${cmd.parameterNumber}' with a byte size of '${cmd.size}' is set to '${cmd2Integer(cmd.configurationValue)}'"
}

private encap(cmd, endpoint) {
	if (endpoint) {
		zwave.multiChannelV3.multiChannelCmdEncap(destinationEndPoint: endpoint).encapsulate(cmd)
	} else {
		cmd
	}
}

private command(physicalgraph.zwave.Command cmd) {
	if (state.sec) {
		zwave.securityV1.securityMessageEncapsulation().encapsulate(cmd).format()
	} else {
		cmd.format()
	}
}

private commands(commands, delay = 1000) {
	delayBetween(commands.collect {
		command(it)
	}, delay)
}

private channelNumber(String dni) {
	dni.split(":")[-1] as Integer
}

private void createChildDevices() {
	state.oldLabel = device.label

	for (i in 1..2) {
		def newDevice = addChildDevice("smartthings", "Child Switch Health",
				"${device.deviceNetworkId}:${i}",
				device.hubId,
				[completedSetup: true,
				 label: "${device.displayName} Outlet ${i}",
				 isComponent: true,
				 componentName: "outlet$i",
				 componentLabel: "Outlet $i"
		])

		newDevice.sendEvent(name: "checkInterval", value: checkInterval, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])
	}
}
