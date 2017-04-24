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
 *  SmartSense Virtual Momentary Contact Switch
 *
 *  Author: SmartThings
 *  Date: 2013-03-07
 */
metadata {
	definition (name: "Z-Wave Virtual Momentary Contact Switch", namespace: "smartthings", author: "SmartThings", ocfDeviceType: "x.com.st.d.sensor.contact") {
		capability "Actuator"
		capability "Switch"
		capability "Refresh"
		capability "Momentary"
		capability "Sensor"
		capability "Relay Switch"
	}

	// simulator metadata
	simulator {
		status "on":  "command: 2003, payload: FF"
		status "off": "command: 2003, payload: 00"

		// reply messages
		reply "2001FF,2502,delay 2000,200100,2502": "command: 2503, payload: FF"
		reply "200100,2502": "command: 2503, payload: 00"
	}

	// tile definitions
	tiles {
		standardTile("switch", "device.switch", width: 2, height: 2, canChangeIcon: true) {
			state "off", label: '${name}', action: "momentary.push", icon: "st.switches.switch.off", backgroundColor: "#ffffff"
			state "on", label: '${name}', action: "switch.off", icon: "st.switches.switch.on", backgroundColor: "#00a0dc"
		}
		standardTile("refresh", "device.switch", inactiveLabel: false, decoration: "flat") {
			state "default", label:'', action:"refresh.refresh", icon:"st.secondary.refresh"
		}

		main "switch"
		details(["switch","refresh"])
	}
}

def parse(String description) {
	def result = null
	def cmd = zwave.parse(description, [0x20: 1])
	if (cmd) {
		result = createEvent(zwaveEvent(cmd))
	}
	log.debug "Parse returned ${result?.descriptionText}"
	return result
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd) {
	[name: "switch", value: cmd.value ? "on" : "off", type: "physical"]
}

def zwaveEvent(physicalgraph.zwave.commands.switchbinaryv1.SwitchBinaryReport cmd) {
	[name: "switch", value: cmd.value ? "on" : "off", type: "digital"]
}

def zwaveEvent(physicalgraph.zwave.commands.manufacturerspecificv2.ManufacturerSpecificReport cmd) {
	if (state.manufacturer != cmd.manufacturerName) {
		updateDataValue("manufacturer", cmd.manufacturerName)
	}

	final relays = [
	    [manufacturerId:0x0113, productTypeId: 0x5246, productId: 0x3133, productName: "Evolve LFM-20"],
		[manufacturerId:0x5254, productTypeId: 0x8000, productId: 0x0002, productName: "Remotec ZFM-80"]
	]

	def productName  = null
	for (it in relays) {
		if (it.manufacturerId == cmd.manufacturerId && it.productTypeId == cmd.productTypeId && it.productId == cmd.productId) {
			productName = it.productName
			break
		}
	}

	if (productName) {
		log.debug "Relay found: $productName"
		updateDataValue("productName", productName)
	}
	[name: "manufacturer", value: cmd.manufacturerName]
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
	// Handles all Z-Wave commands we aren't interested in
	[:]
}

def push() {
	def cmds = [
		zwave.basicV1.basicSet(value: 0xFF).format(),
		zwave.switchBinaryV1.switchBinaryGet().format(),
		"delay 3000",
		zwave.basicV1.basicSet(value: 0x00).format(),
		zwave.switchBinaryV1.switchBinaryGet().format()
	]
}

def on() {
	push()
}

def off() {
	[
		zwave.basicV1.basicSet(value: 0x00).format(),
		zwave.switchBinaryV1.switchBinaryGet().format()
	]
}

def refresh() {
	delayBetween([
		zwave.switchBinaryV1.switchBinaryGet().format(),
		zwave.manufacturerSpecificV1.manufacturerSpecificGet().format()
	])
}
