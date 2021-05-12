/**
 *	Copyright 2021 TechniSat
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
	definition (name: "TechniSat Series switch child", namespace: "TechniSat", author: "TechniSat", vid: "generic-switch-power-energy",
				mnmn: "SmartThings") {
		capability "Energy Meter"
		capability "Switch"
		capability "Power Meter"
		capability "Refresh"
		capability "Health Check"
	}
}

def installed() {
	log.debug "installed()"
	initialize()
}

def updated() {
	log.debug "updated()"  
	initialize()
}

def initialize() {
	sendEvent(name: "checkInterval", value: 2 * 15 * 60 + 2 * 60, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID, offlinePingable: "1"])
	refresh()
}

def handleMeterReport(cmd) {
	if (cmd.meterType == 1) {
		if (cmd.scale == 0) {
			sendEvent(name: "energy", value: cmd.scaledMeterValue, unit: "kWh")
		} else if (cmd.scale == 1) {
			sendEvent(name: "energy", value: cmd.scaledMeterValue, unit: "kVAh")
		} else if (cmd.scale == 2) {
			sendEvent(name: "power", value: Math.round(cmd.scaledMeterValue), unit: "W")
		}
	}
}

def handleZWave(physicalgraph.zwave.commands.meterv3.MeterReport cmd) {
	log.debug "v3 Meter report: "+cmd
	handleMeterReport(cmd)
}

def handleZWave(physicalgraph.zwave.commands.basicv1.BasicReport cmd) {
	log.debug "Basic report: "+cmd
	def value = (cmd.value ? "on" : "off")
	def evt = sendEvent(name: "switch", value: value, type: "physical", descriptionText: "$device.displayName was turned $value")
	if (evt.isStateChange) {
		parent.sendCommand(device, ["delay 3000", meterGet(scale: 2).format()])
	}
}

def handleZWave(physicalgraph.zwave.commands.basicv1.BasicSet cmd) {
	log.debug "Basic set: "+cmd
	def value = (cmd.value ? "on" : "off")
	sendEvent(name: "switch", value: value, descriptionText: "$device.displayName was turned $value")
}

def handleZWave(physicalgraph.zwave.commands.switchbinaryv1.SwitchBinaryReport cmd) {
	log.debug "Switch binary report: "+cmd
	def value = (cmd.value ? "on" : "off")
	sendEvent(name: "switch", value: value, type: "digital", descriptionText: "$device.displayName was turned $value")
}

def handleZWave(physicalgraph.zwave.Command cmd) {
	log.debug "${device.displayName}: Unhandled: $cmd"
	[:]
}

def on() {
	parent.sendCommand(device, [zwave.switchBinaryV1.switchBinarySet(switchValue: 0xFF),
								zwave.switchBinaryV1.switchBinaryGet(),
								zwave.meterV2.meterGet(scale: 2)])
}

def off() {
	parent.sendCommand(device, [zwave.switchBinaryV1.switchBinarySet(switchValue: 0x00),
								zwave.switchBinaryV1.switchBinaryGet(),
								zwave.meterV2.meterGet(scale: 2)])
}

def ping() {
	refresh()
}

def refresh() {
	log.debug "refresh()"
	parent.sendCommand(device, [zwave.switchBinaryV1.switchBinaryGet(),
								zwave.meterV2.meterGet(scale: 0),
								zwave.meterV2.meterGet(scale: 2)])
}

