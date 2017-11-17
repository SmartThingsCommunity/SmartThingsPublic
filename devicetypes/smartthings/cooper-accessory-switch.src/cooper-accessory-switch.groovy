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
	definition(name: "Cooper Accessory Switch", namespace: "smartthings", author: "SmartThings") {
		capability "Actuator"
		capability "Refresh"
		capability "Sensor"
		capability "Switch"

		//zw:L type:1201 mfr:001A prod:5352 model:0000 ver:3.13 zwv:3.52 lib:03 cc:27,75,86,70,85,77,2B,2C,72,73,87
		fingerprint mfr: "001A ", prod: "5352", model: "0000", deviceJoinName: "Cooper Accessory Switch"
	}

	tiles(scale: 2) {
		multiAttributeTile(name: "switch", width: 6, height: 4, canChangeIcon: true) {
			tileAttribute("device.switch", key: "PRIMARY_CONTROL") {
				attributeState "on", label: '${name}', action: "switch.off", icon: "st.unknown.zwave.device", backgroundColor: "#00a0dc", nextState: "off"
				attributeState "off", label: '${name}', action: "switch.on", icon: "st.unknown.zwave.device", backgroundColor: "#ffffff", nextState: "on"
			}
		}

		standardTile("refresh", "device.switch", inactiveLabel: false, decoration: "flat", width: 6, height: 2, backgroundColor: "#00a0dc") {
			state "default", label: '', action: "refresh.refresh", icon: "st.secondary.refresh"
		}

		main "switch"
		details(["switch", "refresh"])
	}

	preferences {
		section {
			input( "delayedOffTime", "number",
					title: "Time after which switch will turn off when using delayed off feature",
					description: "[1..255]", defaultValue: 10, range: "1..255", required: false, displayDuringSetup: true
			)
		}
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
	def cmds = []
	//manufacturer information needs to be checked only once
	if (!getDataValue("manufacturer")) {
		cmds << new physicalgraph.device.HubAction(zwave.manufacturerSpecificV2.manufacturerSpecificGet().format())
	}
	//Check initial switch state
	cmds << new physicalgraph.device.HubAction(zwave.basicV1.basicGet().format())
	//set dalayed off parameter to value set in preferences (default value is 10s)
	cmds << new physicalgraph.device.HubAction(zwave.configurationV1
			.configurationSet(configurationValue: [settings.delayedOffTime ? settings.delayedOffTime : 10], parameterNumber: 1, size: 1).format())
	sendHubCommand(cmds)
}

def on() {
	//Earlier versions of device will not allow setting basic set value for "on" (configuration parameter 4)
	def cmds = []
	cmds << new physicalgraph.device.HubAction(zwave.basicV1.basicSet(value: 0xFF).format())
	//Get current switch value to confirm change
	cmds << new physicalgraph.device.HubAction(zwave.basicV1.basicGet().format())
	sendHubCommand(cmds, 200)
}

def off() {
	def cmds = []
	cmds << new physicalgraph.device.HubAction(zwave.basicV1.basicSet(value: 0x00).format())
	//Get current switch value to confirm change
	cmds << new physicalgraph.device.HubAction(zwave.basicV1.basicGet().format())
	sendHubCommand(cmds, 200)
}

def refresh() {
	def cmds = []
	//Get current switch value
	cmds << new physicalgraph.device.HubAction(zwave.basicV1.basicGet().format())
	//Get current off delay setting
	cmds << new physicalgraph.device.HubAction(zwave.configurationV1.configurationGet(parameterNumber: 1).format())
	sendHubCommand(cmds, 200)
}

def parse(String description) {
	//error handling
	if (description.startsWith("Err")) {
		sendEvent(descriptionText: description, isStateChange: true)
	} else {
		//parse z-wave command
		def cmd = zwave.parse(description)
		if (cmd) {
			//handle z-wave commands
			zwaveEvent(cmd)
		}
	}
	return []
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicSet cmd) {
	sendEvent(name: "switch", value: cmd.value == 0 ? "off" : "on")
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd) {
	sendEvent(name: "switch", value: cmd.value == 0 ? "off" : "on")
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
}

def zwaveEvent(physicalgraph.zwave.commands.configurationv2.ConfigurationReport cmd) {
	//Update dalayedOffTime preference value
	if (cmd.parameterNumber == 1) {
		settings.delayedOffTime = cmd.configurationValue
	}
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
	//received unhandled command
	sendEvent(descriptionText: "$device.displayName: $cmd")
}
