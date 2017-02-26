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
 */
metadata {
	definition (name: "Dimmer Switch", namespace: "smartthings", author: "SmartThings") {
		capability "Switch Level"
		capability "Actuator"
		capability "Indicator"
		capability "Switch"
		capability "Polling"
		capability "Refresh"
		capability "Sensor"

		fingerprint inClusters: "0x26"
	}

	simulator {
		status "on":  "command: 2003, payload: FF"
		status "off": "command: 2003, payload: 00"
		status "09%": "command: 2003, payload: 09"
		status "10%": "command: 2003, payload: 0A"
		status "33%": "command: 2003, payload: 21"
		status "66%": "command: 2003, payload: 42"
		status "99%": "command: 2003, payload: 63"

		// reply messages
		reply "2001FF,delay 5000,2602": "command: 2603, payload: FF"
		reply "200100,delay 5000,2602": "command: 2603, payload: 00"
		reply "200119,delay 5000,2602": "command: 2603, payload: 19"
		reply "200132,delay 5000,2602": "command: 2603, payload: 32"
		reply "20014B,delay 5000,2602": "command: 2603, payload: 4B"
		reply "200163,delay 5000,2602": "command: 2603, payload: 63"
	}

	tiles(scale: 2) {
		multiAttributeTile(name:"switch", type: "lighting", width: 6, height: 4, canChangeIcon: true){
			tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
				attributeState "on", label:'${name}', action:"switch.off", icon:"st.switches.switch.on", backgroundColor:"#79b821", nextState:"turningOff"
				attributeState "off", label:'${name}', action:"switch.on", icon:"st.switches.switch.off", backgroundColor:"#ffffff", nextState:"turningOn"
				attributeState "turningOn", label:'${name}', action:"switch.off", icon:"st.switches.switch.on", backgroundColor:"#79b821", nextState:"turningOff"
				attributeState "turningOff", label:'${name}', action:"switch.on", icon:"st.switches.switch.off", backgroundColor:"#ffffff", nextState:"turningOn"
			}
			tileAttribute ("device.level", key: "SLIDER_CONTROL") {
				attributeState "level", action:"switch level.setLevel"
			}
		}

		standardTile("indicator", "device.indicatorStatus", height: 2, width: 2, inactiveLabel: false, decoration: "flat") {
			state "when off", action:"indicator.indicatorWhenOn", icon:"st.indicators.lit-when-off"
			state "when on", action:"indicator.indicatorNever", icon:"st.indicators.lit-when-on"
			state "never", action:"indicator.indicatorWhenOff", icon:"st.indicators.never-lit"
		}
		standardTile("refresh", "device.switch", height: 2, width: 2, inactiveLabel: false, decoration: "flat") {
			state "default", label:"", action:"refresh.refresh", icon:"st.secondary.refresh"
		}

		main(["switch"])
		details(["switch", "refresh", "indicator"])
	}
}

def parse(String description) {
	def item1 = [
		canBeCurrentState: false,
		linkText: getLinkText(device),
		isStateChange: false,
		displayed: false,
		descriptionText: description,
		value:  description
	]
	def result
	def cmd = zwave.parse(description, [0x20: 1, 0x26: 1, 0x70: 1])
	if (cmd) {
		result = createEvent(cmd, item1)
	}
	else {
		item1.displayed = displayed(description, item1.isStateChange)
		result = [item1]
	}
	log.debug "Parse returned ${result?.descriptionText}"
	result
}

def createEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd, Map item1) {
	def result = doCreateEvent(cmd, item1)
	for (int i = 0; i < result.size(); i++) {
		result[i].type = "physical"
	}
	result
}

def createEvent(physicalgraph.zwave.commands.basicv1.BasicSet cmd, Map item1) {
	def result = doCreateEvent(cmd, item1)
	for (int i = 0; i < result.size(); i++) {
		result[i].type = "physical"
	}
	result
}

def createEvent(physicalgraph.zwave.commands.switchmultilevelv1.SwitchMultilevelStartLevelChange cmd, Map item1) {
	[]
}

def createEvent(physicalgraph.zwave.commands.switchmultilevelv1.SwitchMultilevelStopLevelChange cmd, Map item1) {
	[response(zwave.basicV1.basicGet())]
}

def createEvent(physicalgraph.zwave.commands.switchmultilevelv1.SwitchMultilevelSet cmd, Map item1) {
	def result = doCreateEvent(cmd, item1)
	for (int i = 0; i < result.size(); i++) {
		result[i].type = "physical"
	}
	result
}

def createEvent(physicalgraph.zwave.commands.switchmultilevelv1.SwitchMultilevelReport cmd, Map item1) {
	def result = doCreateEvent(cmd, item1)
	result[0].descriptionText = "${item1.linkText} is ${item1.value}"
	result[0].handlerName = cmd.value ? "statusOn" : "statusOff"
	for (int i = 0; i < result.size(); i++) {
		result[i].type = "digital"
	}
	result
}

def doCreateEvent(physicalgraph.zwave.Command cmd, Map item1) {
	def result = [item1]

	item1.name = "switch"
	item1.value = cmd.value ? "on" : "off"
	item1.handlerName = item1.value
	item1.descriptionText = "${item1.linkText} was turned ${item1.value}"
	item1.canBeCurrentState = true
	item1.isStateChange = isStateChange(device, item1.name, item1.value)
	item1.displayed = item1.isStateChange

	if (cmd.value >= 5) {
		def item2 = new LinkedHashMap(item1)
		item2.name = "level"
		item2.value = cmd.value as String
		item2.unit = "%"
		item2.descriptionText = "${item1.linkText} dimmed ${item2.value} %"
		item2.canBeCurrentState = true
		item2.isStateChange = isStateChange(device, item2.name, item2.value)
		item2.displayed = false
		result << item2
	}
	result
}

def zwaveEvent(physicalgraph.zwave.commands.configurationv1.ConfigurationReport cmd) {
	def value = "when off"
	if (cmd.configurationValue[0] == 1) {value = "when on"}
	if (cmd.configurationValue[0] == 2) {value = "never"}
	[name: "indicatorStatus", value: value, display: false]
}

def createEvent(physicalgraph.zwave.Command cmd,  Map map) {
	// Handles any Z-Wave commands we aren't interested in
	log.debug "UNHANDLED COMMAND $cmd"
}

def on() {
	log.info "on"
	delayBetween([zwave.basicV1.basicSet(value: 0xFF).format(), zwave.switchMultilevelV1.switchMultilevelGet().format()], 5000)
}

def off() {
	delayBetween ([zwave.basicV1.basicSet(value: 0x00).format(), zwave.switchMultilevelV1.switchMultilevelGet().format()], 5000)
}

def setLevel(value) {
	def valueaux = value as Integer
	def level = Math.min(valueaux, 99)
	delayBetween ([zwave.basicV1.basicSet(value: level).format(), zwave.switchMultilevelV1.switchMultilevelGet().format()], 5000)
}

def setLevel(value, duration) {
	def valueaux = value as Integer
	def level = Math.min(valueaux, 99)
	def dimmingDuration = duration < 128 ? duration : 128 + Math.round(duration / 60)
	zwave.switchMultilevelV2.switchMultilevelSet(value: level, dimmingDuration: dimmingDuration).format()
}

def poll() {
	zwave.switchMultilevelV1.switchMultilevelGet().format()
}

def refresh() {
	zwave.switchMultilevelV1.switchMultilevelGet().format()
}

def indicatorWhenOn() {
	sendEvent(name: "indicatorStatus", value: "when on", display: false)
	zwave.configurationV1.configurationSet(configurationValue: [1], parameterNumber: 3, size: 1).format()
}

def indicatorWhenOff() {
	sendEvent(name: "indicatorStatus", value: "when off", display: false)
	zwave.configurationV1.configurationSet(configurationValue: [0], parameterNumber: 3, size: 1).format()
}

def indicatorNever() {
	sendEvent(name: "indicatorStatus", value: "never", display: false)
	zwave.configurationV1.configurationSet(configurationValue: [2], parameterNumber: 3, size: 1).format()
}

def invertSwitch(invert=true) {
	if (invert) {
		zwave.configurationV1.configurationSet(configurationValue: [1], parameterNumber: 4, size: 1).format()
	}
	else {
		zwave.configurationV1.configurationSet(configurationValue: [0], parameterNumber: 4, size: 1).format()
	}
}