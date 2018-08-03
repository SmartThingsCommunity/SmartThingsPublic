/**
 *	Copyright 2018 SmartThings
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
	definition(name: "Z-Wave Binary Switch Endpoint Siren", namespace: "smartthings", author: "SmartThings", mnmn: "SmartThings", vid: "generic-switch") {
		capability "Actuator"
		capability "Health Check"
		capability "Refresh"
		capability "Sensor"
		capability "Switch"
	}

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
	configure()
}

def updated() {
	configure()
}

def configure() {
	sendEvent(name: "checkInterval", value: 2 * 60 * 60 + 2 * 60, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID, offlinePingable: "1"])
	sendEvent(name: "switch", value: "off")
	refresh()
}

def handleZWave(physicalgraph.zwave.commands.basicv1.BasicReport cmd) {
	switchEvents(cmd)
}

def handleZWave(physicalgraph.zwave.commands.basicv1.BasicSet cmd) {
	switchEvents(cmd)
}

def handleZWave(physicalgraph.zwave.commands.switchbinaryv1.SwitchBinaryReport cmd) {
	switchEvents(cmd)
}

def switchEvents(physicalgraph.zwave.Command cmd) {
	def value = (cmd.value ? "on" : "off")
	sendEvent(name: "switch", value: value)
}

def handleZWave(physicalgraph.zwave.Command cmd) {
	[:]
}

def on() {

	//Endpoint no. 2 is double short beep. Second report is needed to change button display to current state "OFF" 
	if (parent.channelNumber(device.deviceNetworkId) == 2) {
		parent.sendCommand(device.deviceNetworkId, [zwave.basicV1.basicSet(value: 0xFF), zwave.switchBinaryV1.switchBinaryGet(),"delay 2000", zwave.switchBinaryV1.switchBinaryGet()])
	} else {
		parent.sendCommand(device.deviceNetworkId, [zwave.basicV1.basicSet(value: 0xFF), zwave.switchBinaryV1.switchBinaryGet()])
	}
}

def off() {
	parent.sendCommand(device.deviceNetworkId, [zwave.basicV1.basicSet(value: 0), zwave.switchBinaryV1.switchBinaryGet()])
}

def ping() {
	refresh()
}

def refresh() {
	parent.sendCommand(device.deviceNetworkId, zwave.switchBinaryV1.switchBinaryGet())
}