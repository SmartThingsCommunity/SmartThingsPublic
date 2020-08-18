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
	definition (name: "Z-Wave Switch Battery", namespace: "smartthings", author: "SmartThings", ocfDeviceType: "oic.d.switch", runLocally: true, minHubCoreVersion: '000.017.0012', executeCommandsLocally: true) {
		capability "Actuator"
		capability "Battery"
		capability "Health Check"
		capability "Refresh"
		capability "Sensor"
		capability "Switch"
		
		//zw:F type:1001 mfr:014A prod:0006 model:0002 ver:10.01 zwv:4.38 lib:03 cc:5E,86,72,73,80,25,85,59,7A role:07 ff:9D00 ui:9D00
		fingerprint mfr:"014A", prod:"0006", model:"0002", deviceJoinName: "Ecolink Switch" //Ecolink Z-Wave Plus Toggle Light Switch
		//zw:F type:1001 mfr:014A prod:0006 model:0003 ver:10.01 zwv:4.38 lib:03 cc:5E,86,72,73,80,25,85,59,7A role:07 ff:9D00 ui:9D00
		fingerprint mfr:"014A", prod:"0006", model:"0003", deviceJoinName: "Ecolink Switch" //Ecolink Z-Wave Plus Smart Switch - Dual Rocker
		//zw:F type:1001 mfr:014A prod:0006 model:0004 ver:10.01 zwv:4.38 lib:03 cc:5E,86,72,73,80,25,85,59,7A role:07 ff:9D00 ui:9D00
		fingerprint mfr:"014A", prod:"0006", model:"0004", deviceJoinName: "Ecolink Switch" //Ecolink Z-Wave Plus Smart Switch - Dual Toggle
		//zw:F type:1001 mfr:014A prod:0006 model:0005 ver:10.01 zwv:4.38 lib:03 cc:5E,86,72,73,80,25,85,59,7A role:07 ff:9D00 ui:9D00
		fingerprint mfr:"014A", prod:"0006", model:"0005", deviceJoinName: "Ecolink Switch" //Ecolink Z-Wave Plus Smart Switch - Single Rocker
		//zw:F type:1001 mfr:014A prod:0006 model:0006 ver:10.01 zwv:4.38 lib:03 cc:5E,86,72,73,80,25,85,59,7A role:07 ff:9D00 ui:9D00
		fingerprint mfr:"014A", prod:"0006", model:"0006", deviceJoinName: "Ecolink Switch" //Ecolink Z-Wave Plus Smart Switch - Single Toggle
	}
	
	tiles(scale: 2) {
		multiAttributeTile(name:"switch", type: "lighting", width: 6, height: 4, canChangeIcon: true) {
			tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
				attributeState "on", label: '${name}', action: "switch.off", icon: "st.switches.switch.on", backgroundColor: "#00A0DC"
				attributeState "off", label: '${name}', action: "switch.on", icon: "st.switches.switch.off", backgroundColor: "#ffffff"
			}
		}

		standardTile("refresh", "device.switch", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
			state "default", label:'', action:"refresh.refresh", icon:"st.secondary.refresh"
		}
		
		valueTile("battery", "device.battery", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
			state "battery", label: '${currentValue}% battery', unit: ""
		}

		main "switch"
		details(["switch","refresh","battery"])
	}
}

def installed() {
	initialize()
}

def initialize() {
	sendEvent(name: "checkInterval", value: 2 * 60 * 60 + 2 * 60, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID, offlinePingable: "1"])
	response(refresh())
}

def updated() {
	initialize()
}

def parse(String description) {
	def result
	def cmd = zwave.parse(description)
	if (cmd) {
		result = createEvent(zwaveEvent(cmd))
	}
	
	result
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

def zwaveEvent(physicalgraph.zwave.commands.batteryv1.BatteryReport cmd) {
	def map = [name: "battery", unit: "%"]
	if (cmd.batteryLevel == 0xFF) {
		map.value = 1
		map.descriptionText = "$device.displayName has a low battery"
	} else {
		map.value = cmd.batteryLevel
	}
	map
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
	// Handles all Z-Wave commands we aren't interested in
	[:]
}

def on() {
	delayBetween([
		zwave.basicV1.basicSet(value: 0xFF).format(),
		zwave.switchBinaryV1.switchBinaryGet().format()
	], 500)
}

def off() {
	delayBetween([
		zwave.basicV1.basicSet(value: 0x00).format(),
		zwave.switchBinaryV1.switchBinaryGet().format()
	], 500)
}

def ping() {
	refresh()
}

def refresh() {
	delayBetween([
		zwave.switchBinaryV1.switchBinaryGet().format(),
		zwave.batteryV1.batteryGet().format()
	])
}
