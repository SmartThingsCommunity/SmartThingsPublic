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
	definition (name: "Cooper Anyplace Switch", namespace: "smartthings", author: "SmartThings") {
		capability "Actuator"
		capability "Sensor"
		capability "Switch"
		capability "Switch Level"

		//zw:S type:0100 mfr:001A prod:4243 model:0000 ver:3.01 zwv:3.67 lib:01 cc:72,77,86,85 ccOut:26
		fingerprint mfr: "001A ", prod: "4243", model: "0000", deviceJoinName: "Cooper Anyplace Switch"
	}

	tiles {
		multiAttributeTile(name: "switch",type: "lighting", width: 6, height:4, canChangeIcon: true) {
			tileAttribute("device.switch", key: "PRIMARY_CONTROL") {
				attributeState "on", label: '${name}', action: "switch.off", icon: "st.unknown.zwave.device", backgroundColor: "#00a0dc", nextState: "off"
				attributeState "off", label: '${name}', action: "switch.on", icon: "st.unknown.zwave.device", backgroundColor: "#ffffff", nextState: "on"
			}
			tileAttribute("device.level", key: "SLIDER_CONTROL") {
				attributeState "level", action: "switch level.setLevel", range: "(0..100)",  unit:"%"
			}
		}

		main "switch"
		details (["switch"])
	}

	preferences {
		input( name: "defaultLevel", type: "number", required: false,
				title: "Dimming level after switch on (leave empty to maintain last setting). Minimal and maximal level settings may override this value.",
				description: "[1..100]", range: "1..100"
		)
		input( name: "step", type: "number", required: false,
				title: "Device's dimming up/down buttons step size. If value is not set, step will be 10.",
				description: "[1..100]", range: "1..100"
		)
		//min level in name only - we check which is lower anyway
		input( name: "minLevel", type: "number", required: false,
				title: "Minimal dimming level. If value is not set, minimal level will be 0.",
				description: "[0..100]", range: "0..100"
		)
		//max level in name only - we check which is higher anyway
		input( name: "maxLevel", type: "number", required: false,
				title: "Maximal dimming level. If value is not set, maximal level  will be 100.",
				description: "[1..100]", range: "1..100"
		)
	}
}

def installed() {
	//set initial state to avoid null pointers
	sendEvent(name:"switch", value: "off")
	sendEvent(name:"level", value: "100")
	sendHubCommand(new physicalgraph.device.HubAction(zwave.associationV2.associationSet(groupingIdentifier:1, nodeId:[zwaveHubNodeId]).format()))
}

def on() {
	sendEvent(name: "switch", value: "on")
	if (settings.defaultLevel != null) {
		setLevel(settings.defaultLevel)
	}
}

def off() {
	sendEvent(name: "switch", value: "off")
}

def setLevel(value) {
	sendEvent(name: "level", value: applyConstraints(value), unit: "%")
}

def parse(String description) {
	if (description.startsWith("Err")) {
		sendEvent(descriptionText:description, isStateChange:true)
	} else {
		def cmd = zwave.parse(description)
		if (cmd) {
			zwaveEvent(cmd)
		}
	}
	return []
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicGet cmd) {
	//switch needs to always think it is switched on, as only then it will send dimmer button events
	sendHubCommand(new physicalgraph.device.HubAction(zwave.basicV1.basicReport(value: 0).format()))
	//switch will reply with Basic Set command with value 255
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicSet cmd) {
	//change state internally - device always thinks it is switched on to handle dimmer buttons
	if (device.currentState("switch").value.equals("on")) {
		off()
	} else {
		on()
	}
}

def zwaveEvent(physicalgraph.zwave.commands.switchmultilevelv3.SwitchMultilevelStartLevelChange  cmd) {
	//get initial value
	def value = Integer.parseInt(device.currentState("level").value)
	//apply default if not set
	def currentStep = (settings.step) ? settings.step : 10
	//increase/decrease value
	setLevel((cmd.upDown) ? value - currentStep : value + currentStep)
}


def zwaveEvent(physicalgraph.zwave.commands.switchmultilevelv3.SwitchMultilevelStopLevelChange  cmd) {
	//dimmer action already handled in SwitchMultilevelStartLevelChange
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
	createEvent(descriptionText: "$device.displayName: $cmd", isStateChange: true)
}

private applyConstraints(value) {
	//check user settings for constrains
	def constraint1 = (settings.minLevel == null) ? 0 : settings.minLevel
	def constraint2 = (settings.maxLevel == null) ? 100 : settings.maxLevel

	//check which constrain is lower, and which is upper
	def lowerConstraint = Math.min(constraint1, constraint2)
	def upperConstraint = Math.max(constraint1, constraint2)

	//apply
	def newLevel = Math.max(lowerConstraint, value)
	Math.min(upperConstraint, newLevel)
}
