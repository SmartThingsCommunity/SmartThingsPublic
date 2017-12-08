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
	definition(name: "Eaton Master Switch", namespace: "smartthings", author: "SmartThings") {
		capability "Actuator"
		capability "Refresh"
		capability "Sensor"
		capability "Relay Switch"
		capability "Switch"

		attribute "protectionMode", "enum", ["unprotected", "sequence", "noControl"]

		command "toggleProtectionMode"

		//zw:L type:1003 mfr:001A prod:534C model:0000 ver:3.21 zwv:3.67 lib:03 cc:25,27,75,86,70,71,85,77,2B,2C,72,73,82,87
		//25 - Switch Binary
		//27 - Switch All
		//70 - Configuration
		//71 - Alarm
		//72 - Manufacturer Specific
		//73 - Powerlevel
		//75 - Protection
		//77 - Node Naming
		//82 - Hail
		//85 - Association
		//86 - Version
		//87 - Indicator

		fingerprint mfr: "001A ", prod: "534C ", model: "0000", deviceJoinName: "Eaton RF9501 Switch "
	}

	tiles(scale: 2) {
		multiAttributeTile(name: "switch", type: "generic", width: 6, height: 4, canChangeIcon: true) {
			tileAttribute("device.switch", key: "PRIMARY_CONTROL") {
				attributeState "on", label: '${name}', action: "switch.off", icon: "st.Lighting.light11", backgroundColor: "#00a0dc", nextState: "off"
				attributeState "off", label: '${name}', action: "switch.on", icon: "st.Lighting.light13", backgroundColor: "#ffffff", nextState: "on"
			}
		}
		standardTile("refresh", "device.switch", inactiveLabel: false, decoration: "flat", width: 4, height: 2, backgroundColor: "#00a0dc") {
			state "default", label: '', action: "refresh.refresh", icon: "st.secondary.refresh"
		}
		standardTile("protectionMode", "device.protectionMode", decoration: "flat", width: 2, height: 2) {
			state "unprotected", label: 'Unprotected', action: "toggleProtectionMode", icon: "st.security.alarm.off"
			state "sequence", label: 'Sequence', action: "toggleProtectionMode", icon: "st.security.alarm.partial"
			state "noControl", label: 'Protected', action: "toggleProtectionMode", icon: "st.security.alarm.on"
		}


		main "switch"
		details(["switch", "refresh", "protectionMode"])
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
		runIn(2, "initialize", [overwrite: true])
		// Allow configure command to be sent and acknowledged before proceeding
	} else {
		//re-initialize with updated parameters
		initialize()
	}
}

def initialize() {
	def delayedOff = settings.delayedOffTime ?: 10
	def protectionValue = getProtectionValue()
	//this way we do not reset protection on initialized
	sendEvent(name: "protectionMode", value: protectionValue)
	def cmds = []
	//manufacturer information needs to be checked only once
	if (!getDataValue("manufacturer")) {
		cmds << zwave.manufacturerSpecificV2.manufacturerSpecificGet()
	}
	//Check initial switch state
	cmds << zwave.basicV1.basicGet()
	//set dalayed off parameter to value set in preferences (default value is 10s)
	cmds << zwave.configurationV1.configurationSet(configurationValue: [delayedOff], parameterNumber: 1, size: 1)
	cmds << zwave.protectionV1.protectionSet(protectionState: protectionValue)
	sendHubCommand cmds*.format()
}

def on() {
	//Earlier versions of device will not allow setting basic set value for "on" (configuration parameter 4)
	def cmds = []
	cmds << zwave.basicV1.basicSet(value: 0xFF)
	//Get current switch value to confirm change
	cmds << zwave.basicV1.basicGet()
	delayBetween cmds*.format(), 500
}

def off() {
	def cmds = []
	cmds << zwave.basicV1.basicSet(value: 0x00)
	//Get current switch value to confirm change
	cmds << zwave.basicV1.basicGet()
	delayBetween cmds*.format(), 500
}

def refresh() {
	def cmds = []
	//Get current switch value
	cmds << zwave.basicV1.basicGet()
	//Get current off delay setting
	cmds << zwave.configurationV1.configurationGet(parameterNumber: 1)
	//Get protection settings
	cmds << zwave.protectionV1.protectionGet()
	delayBetween cmds*.format(), 500
}

def parse(String description) {
	def result = []
	//parse z-wave command
	def cmd = zwave.parse(description, [0x75: 1])
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
	def delayedOff = settings.delayedOffTime ?: 10
	//settings are read-only, so send event if settings are out of sync
	if (cmd.parameterNumber == 1 && cmd.configurationValue[0] != delayedOff) {
		createEvent([descriptionText: "$device.displayName delayed off time settings are out of sync. Please save device preferences.",
					 isStateChange  : true])
	} else {
		return null
	}
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
	//received unhandled command
	createEvent(descriptionText: "$device.displayName: $cmd", isStateChange: true)
}

def zwaveEvent(physicalgraph.zwave.commands.protectionv1.ProtectionReport cmd) {
	def protectionModeStates = ["unprotected", "sequence", "noControl"]
	def protectionModeValue = protectionModeStates[cmd.protectionState]
	createEvent(name: "protectionMode", value: protectionModeValue)
}


def toggleProtectionMode() {
	def newProtection = (getProtectionValue() + 1) % 3 //current -> new : 0 -> 1; 1 -> 2; 2 -> 0
	def cmds = []
	cmds << zwave.protectionV1.protectionSet(protectionState: newProtection)
	cmds << zwave.protectionV1.protectionGet()
	delayBetween cmds*.format()
}

private getProtectionValue() {
	def value
	//avoid MissingPropertyException
	switch (device.currentValue("protectionMode")) {
		case "sequence":
			value = 1
			break
		case "noControl":
			value = 2
			break
		case "unprotected":
		default:
			value = 0
			break
	}
	value
}