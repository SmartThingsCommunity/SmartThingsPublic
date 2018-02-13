/**
 *  Copyright 2018 SmartThings
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
	definition(name: "Eaton Anyplace Switch", namespace: "smartthings", author: "SmartThings") {
		capability "Actuator"
		capability "Sensor"
		capability "Button"

		attribute "switch", "enum", ["on", "off"]

		//zw:S type:0100 mfr:001A prod:4243 model:0000 ver:3.01 zwv:3.67 lib:01 cc:72,77,86,85 ccOut:26
		fingerprint mfr: "001A", prod: "4243", model: "0000", deviceJoinName: "Eaton Anyplace Switch"
	}


	tiles(scale: 2) {
		multiAttributeTile(name: "rich-control", type: "generic", width: 6, height: 4, canChangeIcon: true) {
			tileAttribute("device.button", key: "PRIMARY_CONTROL") {
				attributeState "default", label: ' ', action: "", icon: "st.unknown.zwave.remote-controller", backgroundColor: "#ffffff"
			}
		}

		main "rich-control"
		details(["rich-control"])
	}
}

def installed() {
	// initialize state
	sendEvent(name: "numberOfButtons", value: 1, isStateChange: true, displayed: false)
	sendEvent(name: "switch", value: "off", isStateChange: true, displayed: false)
}

def parse(String description) {
	def result = []
	def cmd = zwave.parse(description)
	if (cmd) {
		result += zwaveEvent(cmd)
	}
	return result
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicGet cmd) {
	def currentValue = device.currentValue("switch").equals("on") ? 255 : 0
	response(zwave.basicV1.basicReport(value: currentValue).format())
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicSet cmd) {
	def events = []
	events << createEvent(name: "switch", value: cmd.value ? "on" : "off", isStateChange: true, displayed: false)
	events << createEvent(name: "button", value: "pushed", data: [buttonNumber: 1], descriptionText: "$device.displayName button was pushed", isStateChange: true)
	events
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
	[:]
}
