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
	definition (name: "Eaton Anyplace Switch", namespace: "smartthings", author: "SmartThings") {
		capability "Actuator"
		capability "Sensor"
		capability "Switch"
		capability "Switch Level"

		//zw:S type:0100 mfr:001A prod:4243 model:0000 ver:3.01 zwv:3.67 lib:01 cc:72,77,86,85 ccOut:26
		fingerprint mfr: "001A ", prod: "4243", model: "0000", deviceJoinName: "Eaton Anyplace Switch"
	}

	tiles {
		multiAttributeTile(name: "switch", type: "generic", width: 6, height: 4, canChangeIcon: true) {
			tileAttribute("device.switch", key: "PRIMARY_CONTROL") {
				attributeState "on", label: '${name}', icon: "st.unknown.zwave.device", backgroundColor: "#00a0dc"
				attributeState "off", label: '${name}', icon: "st.unknown.zwave.device", backgroundColor: "#ffffff"
			}
			tileAttribute("device.level", key: "SECONDARY_CONTROL") {
				attributeState "level", label: '${currentValue} %'
			}
		}

		main "switch"
		details (["switch"])
	}

	preferences {
		input( name: "defaultLevel", type: "number", required: false,
				title: "Dimming level after switch on. Minimal and maximal level settings may override this value. Value range: 1 - 100",
				description: "100", range: "1..100"
		)
		input( name: "step", type: "number", required: false,
				title: "Device's dimming up/down buttons step size. Value range: 1 - 100.",
				description: "10", range: "1..100"
		)
	}
}

def installed() {
	sendHubCommand zwave.associationV2.associationSet(groupingIdentifier:1, nodeId:[zwaveHubNodeId]).format()
	// initialize state
	sendEvent(name: "switch", value: "off")
	sendEvent(name: "level", value: 0, unit: "%")
}

// We can not change device's state remotely, so we do not define on/off/setLevel methods

def parse(String description) {
	def result = []
	def cmd = zwave.parse(description)
	if (cmd) {
		result += zwaveEvent(cmd)
	}
	return result
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicGet cmd) {
	def currentValue = device.currentState("switch").value.equals("on") ? 255 : 0
	response zwave.basicV1.basicReport(value: currentValue).format()
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicSet cmd) {
	def events = []
	//change state internally - device always thinks it is switched on to handle dimmer buttons
	if (cmd.value) {
		def switchOnLevel = settings.defaultLevel ? settings.defaultLevel : 100
		events << createEvent(name: "switch", value: "on")
		events << createEvent(name: "level", value: switchOnLevel, unit: "%")
	} else {
		events << createEvent(name: "switch", value: "off")
		events << createEvent(name: "level", value: 0, unit: "%")
	}
	events
}

def zwaveEvent(physicalgraph.zwave.commands.switchmultilevelv3.SwitchMultilevelStartLevelChange  cmd) {
	//get initial value
	def currentLevel = Integer.parseInt(device.currentState("level").value)
	//apply default if not set
	def currentStep = (settings.step) ? settings.step : 10
	//increase/decrease value
	def newLevel
	if (cmd.upDown) {
		newLevel = Math.max(currentLevel - currentStep, 1)
	} else {
		newLevel = Math.min(currentLevel + currentStep, 100)
	}
	def events = []
	events << createEvent(name: "switch", value: "on")
	events << createEvent(name: "level", value: newLevel, unit: "%")
	events
}


def zwaveEvent(physicalgraph.zwave.commands.switchmultilevelv3.SwitchMultilevelStopLevelChange  cmd) {
	//dimmer action already handled in SwitchMultilevelStartLevelChange
	return null
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
	createEvent(descriptionText: "$device.displayName: $cmd", isStateChange: true)
}
