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
	definition (name: "Eaton Receptacle", namespace: "smartthings", author: "SmartThings") {
		capability "Actuator"
		capability "Switch"
		capability "Refresh"
		capability "Sensor"

		attribute "powerUpStateValue", "enum", ["On", "Off", "Last State"]
		attribute "protectionValue", "enum", ["Disabled", "By sequence", "No local operation"]

		//zw:L type:1003 mfr:001A prod:5244 model:0000 ver:3.05 zwv:3.67 lib:03 cc:25,27,75,86,70,71,85,77,2B,2C,72,73,82,87
		fingerprint mfr:"001A", prod:"5244", deviceJoinName: "Eaton Receptacle"
	}

	preferences {
		input ("powerUpState", "enum", title: "Power Up State", description: "Last State",
				required: false, options:["on": "On", "off": "Off", "lastState": "Last State"])

		input ("protection", "enum", title: "Child Lockout", description: "Disabled",
				required: false, options:["disabled": "Disabled", "sequence": "Protection by Sequence", "noOperation": "No local operation"])
	}

	tiles(scale: 2) {
		multiAttributeTile(name:"switch", type: "lighting", width: 6, height: 4, canChangeIcon: false){
			tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
				attributeState ("on", label: '${name}', action: "switch.off",
						icon: "st.switches.switch.on", backgroundColor: "#00A0DC")
				attributeState ("off", label: '${name}', action: "switch.on",
						icon: "st.switches.switch.off", backgroundColor: "#ffffff")
			}
		}
		standardTile("refresh", "device.switch", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
			state "default", label:'', action:"refresh.refresh", icon:"st.secondary.refresh"
		}

		standardTile("powerUp", "device.powerUpStateValue", inactiveLabel: false, decoration: "flat",
				width: 2, height: 2, backgroundColor: "#00a0dc") {
			state "default", label: 'Power up state:\n${currentValue}'
		}

		standardTile("protection", "device.protectionValue", inactiveLabel: false, decoration: "flat",
				width: 2, height: 2, backgroundColor: "#00a0dc") {
			state "default", label: 'Protection:\n${currentValue}'
		}

		main "switch"
		details(["switch", "refresh", "powerUp", "protection"])
	}
}

def updated(){
	def powerUpValue = getPowerUpStateValue()
	def protectionValue = getProtectionValue()
	// Update or initialize displayed attributes
	sendEvent(name: "protectionValue", value: getProtectionName(protectionValue))
	sendEvent(name: "powerUpStateValue", value: getPowerUpStateName(powerUpValue))
	def cmds = []
	cmds << zwave.configurationV1.configurationSet(configurationValue: [getPowerUpStateValue()],
			parameterNumber: 5, size :1)
	cmds << zwave.protectionV1.protectionSet(protectionState: protectionValue)
	cmds << zwave.manufacturerSpecificV2.manufacturerSpecificGet()
	sendHubCommand cmds*.format(), 300
}

def on() {
	def cmds = []
	cmds << zwave.basicV1.basicSet(value: 0xFF)
	cmds << zwave.basicV1.basicGet()
	delayBetween cmds*.format(), 300
}

def off() {
	def cmds = []
	cmds << zwave.basicV1.basicSet(value: 0x00)
	cmds << zwave.basicV1.basicGet()
	delayBetween cmds*.format(), 300
}

def refresh() {
	def cmds = []
	cmds << zwave.basicV1.basicGet()
	cmds << zwave.configurationV1.configurationGet(parameterNumber: 5)
	cmds << zwave.protectionV1.protectionGet()
	delayBetween cmds*.format(), 300
}

def parse(String description) {
	def result = []
	def cmd = zwave.parse(description, [0x75:1])
	if (cmd) {
		result += zwaveEvent(cmd)
	}
	return result
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd) {
	createEvent(name: "switch", value: cmd.value ? "on" : "off")
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicSet cmd) {
	createEvent(name: "switch", value: cmd.value ? "on" : "off")
}

def zwaveEvent(physicalgraph.zwave.commands.manufacturerspecificv2.ManufacturerSpecificReport cmd) {
	if (cmd.manufacturerName) {
		updateDataValue("manufacturer", cmd.manufacturerName)
	}
	if (cmd.productTypeId) {
		updateDataValue("productTypeId", cmd.productTypeId.toString())
	}
	// productId 0 is a valid value
	if (cmd.productId != null) {
		updateDataValue("productId", cmd.productId.toString())
	}
	return null
}

def zwaveEvent(physicalgraph.zwave.commands.configurationv2.ConfigurationReport cmd) {
	//settings are read-only
	if(cmd.parameterNumber == 5) {
		createEvent(name: "powerUpStateValue", value: getPowerUpStateName(cmd.configurationValue[0]))
	} else {
		return null
	}
}

def zwaveEvent(physicalgraph.zwave.commands.protectionv1.ProtectionReport cmd) {
	//settings are read-only
	createEvent(name: "protectionValue", value: getProtectionName(cmd.protectionState))
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
	// Handles all Z-Wave commands we aren't interested in
	log.debug "Unexpected zwave command $cmd"
	return null
}

private getPowerUpStateValue() {
	def value
	switch (settings.powerUpState) {
		case "off":
			value = 1
			break
		case "on":
			value = 2
			break
		case "lastState":
		default:
			value = 3
			break
	}
	value
}

private getPowerUpStateName(arg) {
	def value
	switch (arg) {
		case 1:
			value = "Off"
			break
		case 2:
			value = "On"
			break
		case 3:
		default:
			value = "Last State"
			break
	}
	value
}

private getProtectionValue() {
	def value
	switch (settings.protection) {
		case "sequence":
			value = 1
			break
		case "noOperation":
			value = 2
			break
		case "disabled":
		default:
			value = 0
			break
	}
	value
}

private getProtectionName(arg) {
	def value
	switch (arg) {
		case 1:
			value = "By sequence"
			break
		case 2:
			value = "No local operation"
			break
		case 0:
		default:
			value = "Disabled"
			break
	}
	value
}
