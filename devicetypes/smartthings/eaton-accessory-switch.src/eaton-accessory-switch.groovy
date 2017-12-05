/**
 *  Copyright 2017 SmartThings
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */

metadata {
	definition(name: "Eaton Accessory Switch", namespace: "smartthings", author: "SmartThings") {
		capability "Actuator"
		capability "Refresh"
		capability "Sensor"
		capability "Switch"

		attribute "delayedOffValue", "number"

		//zw:L type:1201 mfr:001A prod:5352 model:0000 ver:3.13 zwv:3.52 lib:03 cc:27,75,86,70,85,77,2B,2C,72,73,87
		fingerprint mfr: "001A ", prod: "5352", model: "0000", deviceJoinName: "Eaton Accessory Switch"
	}

	tiles(scale: 2) {
		multiAttributeTile(name: "switch", width: 6, height: 4, canChangeIcon: true) {
			tileAttribute("device.switch", key: "PRIMARY_CONTROL") {
				attributeState "on", label: '${name}', action: "switch.off", icon: "st.unknown.zwave.device", backgroundColor: "#00a0dc", nextState: "off"
				attributeState "off", label: '${name}', action: "switch.on", icon: "st.unknown.zwave.device", backgroundColor: "#ffffff", nextState: "on"
			}
		}

		standardTile("refresh", "device.switch", inactiveLabel: false, decoration: "flat", width: 4, height: 2, backgroundColor: "#00a0dc") {
			state "default", label: '', action: "refresh.refresh", icon: "st.secondary.refresh"
		}

		standardTile("delayedOff", "device.delayedOffValue", inactiveLabel: false, decoration: "flat",
				width: 2, height: 2, backgroundColor: "#00a0dc") {
			state "default", label: 'Delayed off time:\n${currentValue}s'
		}

		main "switch"
		details(["switch", "refresh", "delayedOff"])
	}

	preferences {
		input "delayedOffTime", "number",
				title: "Time after which switch will turn off when using delayed off feature (1 - 255 [s])",
				description: "10", range: "1..255"
	}
}

def installed() {
	//use runIn to schedule the initialize method in case "updated()" method below is also sending commands to the device
	runIn(3, "initialize", [overwrite: true])  // Allow configure command to be sent and acknowledged before proceeding
}

def updated() {
	//avoid calling initialize() twice when installing the device
	if (!getDataValue("manufacturer")) {
		runIn(2, "initialize", [overwrite: true])  // Allow configure command to be sent and acknowledged before proceeding
	} else {
		//re-initialize with updated parameters
		initialize()
	}
}

def initialize() {
	def delayedOff = settings.delayedOffTime ? settings.delayedOffTime : 10
	sendEvent(name: "delayedOffValue", value: delayedOff)

	def cmds = []
	//manufacturer information needs to be checked only once
	if (!getDataValue("manufacturer")) {
		cmds << zwave.manufacturerSpecificV2.manufacturerSpecificGet()
	}
	//Check initial switch state
	cmds << zwave.basicV1.basicGet()
	//set dalayed off parameter to value set in preferences (default value is 10s)
	cmds << zwave.configurationV1.configurationSet(configurationValue: [delayedOff], parameterNumber: 1, size: 1)
	sendHubCommand cmds*.format()
}

def on() {
	//Earlier versions of device will not allow setting basic set value for "on" (configuration parameter 4)
	def cmds = []
	cmds << zwave.basicV1.basicSet(value: 0xFF)
	//Get current switch value to confirm change
	cmds << zwave.basicV1.basicGet()
	delayBetween cmds*.format(), 200
}

def off() {
	def cmds = []
	cmds << zwave.basicV1.basicSet(value: 0x00)
	//Get current switch value to confirm change
	cmds << zwave.basicV1.basicGet()
	delayBetween cmds*.format(), 200
}

def refresh() {
	def cmds = []
	//Get current switch value
	cmds << zwave.basicV1.basicGet()
	//Get current off delay setting
	cmds << zwave.configurationV1.configurationGet(parameterNumber: 1)
	delayBetween cmds*.format(), 200
}

def parse(String description) {
	def result = []
	//parse z-wave command
	def cmd = zwave.parse(description)
	if (cmd) {
		//handle z-wave commands
		result += zwaveEvent(cmd)
	}
	return result
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicSet cmd) {
	createEvent(name: "switch", value: cmd.value == 0 ? "off" : "on")
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd) {
	createEvent(name: "switch", value: cmd.value == 0 ? "off" : "on")
}

def zwaveEvent(physicalgraph.zwave.commands.manufacturerspecificv2.ManufacturerSpecificReport cmd) {
	//update manufacturer info
	if (cmd.manufacturerName) {
		updateDataValue("manufacturer", cmd.manufacturerName)
	}
	if (cmd.productTypeId) {
		updateDataValue("productTypeId", cmd.productTypeId.toString())
	}
	if (cmd.productId != null) {
		updateDataValue("productId", cmd.productId.toString())
	}
	return null
}

def zwaveEvent(physicalgraph.zwave.commands.configurationv2.ConfigurationReport cmd) {
	//Update dalayedOffTime preference value
	if (cmd.parameterNumber == 1) {
		// Settings are read-only
		// We use custom attribute do display current "Delayed Off" time set on the device.
		createEvent(name: "delayedOffValue", value: cmd.configurationValue[0])
	} else {
		return null
	}
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
	//received unhandled command
	createEvent(descriptionText: "$device.displayName: $cmd")
}
