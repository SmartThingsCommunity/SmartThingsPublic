/**
 *
 *  Inovelli 2-Channel Smart Plug
 *
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
	definition(name: "Inovelli 2-Channel Smart Plug", namespace: "erocm123", author: "Eric Maycock", ocfDeviceType: "oic.d.smartplug", mnmn: "SmartThings", vid: "generic-switch") {
		capability "Actuator"
		capability "Sensor"
		capability "Switch"
		capability "Polling"
		capability "Refresh"
		capability "Health Check"

		// Fingerprints moved to "Inovelli 2-Channel Smart Plug MCD" for modern MCD experience.
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
		logging("Parsed ${cmd} to ${result.inspect()}", 1)
	} else {
		logging("Non-parsed event: ${description}", 2)
	}
	return result
}
def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd, ep = null) {
	logging("BasicReport ${cmd} - ep ${ep}", 2)
	if (ep) {
		def event
		childDevices.each {
			childDevice ->
				if (childDevice.deviceNetworkId == "$device.deviceNetworkId-ep$ep") {
					childDevice.sendEvent(name: "switch", value: cmd.value ? "on" : "off")
				}
		}
		if (cmd.value) {
			event = [createEvent([name: "switch", value: "on"])]
		} else {
			def allOff = true
			childDevices.each {
				n ->
					if (n.currentState("switch")?.value != "off") allOff = false
			}
			if (allOff) {
				event = [createEvent([name: "switch", value: "off"])]
			} else {
				event = [createEvent([name: "switch", value: "on"])]
			}
		}
		return event
	}
}
def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicSet cmd) {
	logging("BasicSet ${cmd}", 2)
	def result = createEvent(name: "switch", value: cmd.value ? "on" : "off", type: "digital")
	def cmds = []
	cmds << encap(zwave.switchBinaryV1.switchBinaryGet(), 1)
	cmds << encap(zwave.switchBinaryV1.switchBinaryGet(), 2)
	return [result, response(commands(cmds))] // returns the result of reponse()
}
def zwaveEvent(physicalgraph.zwave.commands.switchbinaryv1.SwitchBinaryReport cmd, ep = null) {
	logging("SwitchBinaryReport ${cmd} - ep ${ep}", 2)
	if (ep) {
		def event
		def childDevice = childDevices.find {
			it.deviceNetworkId == "$device.deviceNetworkId-ep$ep"
		}
		if (childDevice) childDevice.sendEvent(name: "switch", value: cmd.value ? "on" : "off")
		if (cmd.value) {
			event = [createEvent([name: "switch", value: "on"])]
		} else {
			def allOff = true
			childDevices.each {
				n->
					if (n.currentState("switch")?.value != "off") allOff = false
			}
			if (allOff) {
				event = [createEvent([name: "switch", value: "off"])]
			} else {
				event = [createEvent([name: "switch", value: "on"])]
			}
		}
		return event
	} else {
		def result = createEvent(name: "switch", value: cmd.value ? "on" : "off", type: "digital")
		def cmds = []
		cmds << encap(zwave.switchBinaryV1.switchBinaryGet(), 1)
		cmds << encap(zwave.switchBinaryV1.switchBinaryGet(), 2)
		return [result, response(commands(cmds))] // returns the result of reponse()
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
	logging("MultiChannelCmdEncap ${cmd}", 2)
	def encapsulatedCommand = cmd.encapsulatedCommand([0x32: 3, 0x25: 1, 0x20: 1])
	if (encapsulatedCommand) {
		zwaveEvent(encapsulatedCommand, cmd.sourceEndPoint as Integer)
	}
}
def zwaveEvent(physicalgraph.zwave.commands.manufacturerspecificv2.ManufacturerSpecificReport cmd) {
	logging("ManufacturerSpecificReport ${cmd}", 2)
	def msr = String.format("%04X-%04X-%04X", cmd.manufacturerId, cmd.productTypeId, cmd.productId)
	logging("msr: $msr", 2)
	updateDataValue("MSR", msr)
}
def zwaveEvent(physicalgraph.zwave.Command cmd) {
	// This will capture any commands not handled by other instances of zwaveEvent
	// and is recommended for development so you can see every command the device sends
	logging("Unhandled Event: ${cmd}", 2)
}
def on() {
	logging("on()", 1)
	commands([
			zwave.switchAllV1.switchAllOn(),
			encap(zwave.switchBinaryV1.switchBinaryGet(), 1),
			encap(zwave.switchBinaryV1.switchBinaryGet(), 2)
	])
}
def off() {
	logging("off()", 1)
	commands([
			zwave.switchAllV1.switchAllOff(),
			encap(zwave.switchBinaryV1.switchBinaryGet(), 1),
			encap(zwave.switchBinaryV1.switchBinaryGet(), 2)
	])
}
void childOn(String dni) {
	logging("childOn($dni)", 1)
	def cmds = []
	cmds << new physicalgraph.device.HubAction(command(encap(zwave.basicV1.basicSet(value: 0xFF), channelNumber(dni))))
	cmds << new physicalgraph.device.HubAction(command(encap(zwave.switchBinaryV1.switchBinaryGet(), channelNumber(dni))))
	sendHubCommand(cmds, 1000)
}
void childOff(String dni) {
	logging("childOff($dni)", 1)
	def cmds = []
	cmds << new physicalgraph.device.HubAction(command(encap(zwave.basicV1.basicSet(value: 0x00), channelNumber(dni))))
	cmds << new physicalgraph.device.HubAction(command(encap(zwave.switchBinaryV1.switchBinaryGet(), channelNumber(dni))))
	sendHubCommand(cmds, 1000)
}
void childRefresh(String dni) {
	logging("childRefresh($dni)", 1)
	def cmds = []
	cmds << new physicalgraph.device.HubAction(command(encap(zwave.switchBinaryV1.switchBinaryGet(), channelNumber(dni))))
	sendHubCommand(cmds, 1000)
}
def poll() {
	logging("poll()", 1)
	commands([
			encap(zwave.switchBinaryV1.switchBinaryGet(), 1),
			encap(zwave.switchBinaryV1.switchBinaryGet(), 2),
	])
}
def refresh() {
	logging("refresh()", 1)
	commands([
			encap(zwave.switchBinaryV1.switchBinaryGet(), 1),
			encap(zwave.switchBinaryV1.switchBinaryGet(), 2),
	])
}
def ping() {
	logging("ping()", 1)
	refresh()
}
def installed() {
	logging("installed()", 1)
	command(zwave.manufacturerSpecificV1.manufacturerSpecificGet())
	createChildDevices()
}
def updated() {
	logging("updated()", 1)
	if (!childDevices) {
		createChildDevices()
	} else if (device.label != state.oldLabel) {
		childDevices.each {
			if (it.label == "${state.oldLabel} (CH${channelNumber(it.deviceNetworkId)})") {
				def newLabel = "${device.displayName} (CH${channelNumber(it.deviceNetworkId)})"
				it.setLabel(newLabel)
			}
		}
		state.oldLabel = device.label
	}
	sendEvent(name: "checkInterval", value: 2 * 15 * 60 + 2 * 60, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID, offlinePingable: "1"])
	sendEvent(name: "needUpdate", value: device.currentValue("needUpdate"), displayed: false, isStateChange: true)
}
def zwaveEvent(physicalgraph.zwave.commands.configurationv2.ConfigurationReport cmd) {
	logging("${device.displayName} parameter '${cmd.parameterNumber}' with a byte size of '${cmd.size}' is set to '${cmd2Integer(cmd.configurationValue)}'", 2)
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
	dni.split("-ep")[-1] as Integer
}
private void createChildDevices() {
	state.oldLabel = device.label
	for (i in 1..2) {
		addChildDevice("Switch Child Device",
				"${device.deviceNetworkId}-ep${i}",
				device.hubId,
				[completedSetup: true,
				 label: "${device.displayName} (CH${i})",
				 isComponent: true,
				 componentName: "ep$i",
				 componentLabel: "Channel $i"
		])
	}
}

private def logging(message, level) {
	if (logLevel != "0") {
		switch (logLevel) {
			case "1":
				if (level > 1) log.debug "$message"
				break
			case "99":
				log.debug "$message"
				break
		}
	}
}
