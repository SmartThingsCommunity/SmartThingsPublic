/**
 *	Ecolink Siren
 *
 *	Copyright 2018
 *
 *	Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *	in compliance with the License. You may obtain a copy of the License at:
 *
 *		http://www.apache.org/licenses/LICENSE-2.0
 *
 *	Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *	on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *	for the specific language governing permissions and limitations under the License.
 *
 */
metadata {
	definition (name: "Ecolink Wireless Siren", namespace: "SmartThings", author: "SmartThings", mnmn: "SmartThings", vid: "generic-switch") {
		capability "Actuator"
		capability "Health Check"
		capability "Switch"
		capability "Refresh"
		capability "Sensor"
		capability "Alarm"

		//zw:L type:1005 mfr:014A prod:0005 model:000A ver:1.10 zwv:4.05 lib:03 cc:5E,86,72,5A,85,59,73,25,60,8E,20,7A role:05 ff:8F00 ui:8F00 epc:4 ep:['1005 20,25,5E,59,85']
		fingerprint mfr: "014A", prod: "0005", model: "000A", deviceJoinName: "Ecolink Wireless Siren"	 
	}

	tiles(scale: 2) {
		multiAttributeTile(name: "switch", type: "lighting", width: 6, height: 4, canChangeIcon: true) {
			tileAttribute("device.switch", key: "PRIMARY_CONTROL") {
				attributeState "on", label: '${name}', action: "switch.off", icon: "st.switches.switch.on", backgroundColor: "#00A0DC"
				attributeState "off", label: '${name}', action: "switch.on", icon: "st.switches.switch.off", backgroundColor: "#ffffff"
			}
		}

		childDeviceTiles("endpoints")

		standardTile("refresh", "device.switch", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
			state "default", label: '', action: "refresh.refresh", icon: "st.secondary.refresh"
		}
	}
}

def installed() {
	addChildSwitches()
	initialize()
}

def updated() {
	initialize()
}

def initialize() {
	if (!childDevices) {
		addChildSwitches()
	}
	sendEvent(name: "checkInterval", value: 2 * 60 * 60 + 2 * 60, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID, offlinePingable: "1"])
	off()
	setChildSirensOff()
}

def parse(String description) {

	def result = null
	def cmd = zwave.parse(description)
	if (cmd) {
		result = zwaveEvent(cmd)
		
		if(result!=null) {
			createEvent(result)
		}
	}
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd) {
	[name: "switch", value: cmd.value ? "on" : "off"]
}

def zwaveEvent(physicalgraph.zwave.commands.switchbinaryv1.SwitchBinaryReport cmd) {
	[name: "switch", value: cmd.value ? "on" : "off"]
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
	[:]
}

def zwaveEvent(physicalgraph.zwave.commands.multichannelv3.MultiChannelCmdEncap cmd) {
	def encapsulatedCommand = cmd.encapsulatedCommand()
	def srcEndpoint = cmd.sourceEndPoint
	def destEnd = cmd.destinationEndPoint
	
	if(srcEndpoint == 1) {
		zwaveEvent(encapsulatedCommand)
	} else {
		String childDni = "${device.deviceNetworkId}-ep$srcEndpoint"
		def child = childDevices.find { it.deviceNetworkId == childDni }

		child?.handleZWave(encapsulatedCommand)
	}
}

def on() {
	sendEvent(name: "switch", value: "on")
	def cmds = []
	cmds << basicSetCmd(0xFF, 1)
	cmds << basicGetCmd(1)
	delayBetween(cmds, 100)
}

def off() {
	sendEvent(name: "switch", value: "off")
	def cmds = []
	cmds << basicSetCmd(0x00, 1)
	cmds << basicGetCmd(1)
	delayBetween(cmds, 100)
}

def setChildSirensOff() {
	def cmds = []
	
	endPoints.each {
		cmds << basicSetCmd(0x00, it)
		cmds << basicGetCmd(it)
	}
	delayBetween(cmds, 50)
}

def addChildSwitches() {
	for (i in 2..4) {
		String childDni = "${device.deviceNetworkId}-ep$i"
		String componentLabel =	 "$device.displayName $i"
		String ch = "ch$i"
		
		addChildDevice("Z-Wave Binary Switch Endpoint Siren", childDni, device.hub.id,[completedSetup: true, label: componentLabel, isComponent: true, componentName: ch, componentLabel: componentLabel])
	}
}

def getEndPoints() { [2, 3, 4] }

def basicSetCmd(value, endPoint) {
	multiChannelCmdEncapCmd(zwave.basicV1.basicSet(value: value), endPoint)
}

def basicGetCmd(endPoint) {
	multiChannelCmdEncapCmd(zwave.basicV1.basicGet(), endPoint)
}

def multiChannelCmdEncapCmd(cmd, endPoint) {	
	def cmds = []
	cmds << zwave.multiChannelV3.multiChannelCmdEncap(destinationEndPoint: endPoint).encapsulate(cmd).format()
}

def sendCommand(device, commands) {	 
	def result = commands.collect { 
		if (it instanceof String) {
			it
		} else {
			encap(channelNumber(device), it) 
		}
	}
	sendHubCommand(result, 100)
}

def channelNumber(String dni) {
	dni.split("-ep")[-1] as Integer
}

def encap(endpointNumber, cmd) {
	zwave.multiChannelV3.multiChannelCmdEncap(destinationEndPoint: endpointNumber).encapsulate(cmd).format()
}

def ping() {
	refresh()
}

def refresh() {
	def cmds = []	
	endPoints.each {
		cmds << basicGetCmd(it)
	}
	cmds << basicGetCmd(1)
	return delayBetween(cmds, 100)
}