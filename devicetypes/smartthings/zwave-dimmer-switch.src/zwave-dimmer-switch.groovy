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
	definition (name: "Z-wave Dimmer Switch Generic", namespace: "smartthings", author: "SmartThings") {
		capability "Switch Level"
		capability "Actuator"
		capability "Switch"
		capability "Polling"
		capability "Refresh"
		capability "Sensor"

		fingerprint inClusters: "0x26", deviceJoinName: "Z-Wave Dimmer"
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

		standardTile("refresh", "device.switch", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
			state "default", label:'', action:"refresh.refresh", icon:"st.secondary.refresh"
		}

		valueTile("level", "device.level", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "level", label:'${currentValue} %', unit:"%", backgroundColor:"#ffffff"
		}

		main(["switch"])
		details(["switch", "level", "refresh"])

	}
}

def parse(String description) {
	def result = null
	if (description != "updated") {
		log.debug "parse() >> zwave.parse($description)"
		def cmd = zwave.parse(description, [0x20: 1, 0x26: 1, 0x70: 1])
		if (cmd) {
			result = zwaveEvent(cmd)
		}
	}
	if (result?.name == 'hail' && hubFirmwareLessThan("000.011.00602")) {
		result = [result, response(zwave.basicV1.basicGet())]
		log.debug "Was hailed: requesting state update"
	} else {
		log.debug "Parse returned ${result?.descriptionText}"
	}
	return result
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd) {
	dimmerEvents(cmd)
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicSet cmd) {
	dimmerEvents(cmd)
}

def zwaveEvent(physicalgraph.zwave.commands.switchmultilevelv1.SwitchMultilevelReport cmd) {
	dimmerEvents(cmd)
}

def zwaveEvent(physicalgraph.zwave.commands.switchmultilevelv1.SwitchMultilevelSet cmd) {
	dimmerEvents(cmd)
}

private dimmerEvents(physicalgraph.zwave.Command cmd) {
	def value = (cmd.value ? "on" : "off")
	def result = [createEvent(name: "switch", value: value)]
	if (cmd.value && cmd.value <= 100) {
		result << createEvent(name: "level", value: cmd.value, unit: "%")
	}
	return result
}


def zwaveEvent(physicalgraph.zwave.commands.configurationv1.ConfigurationReport cmd) {
	log.debug "ConfigurationReport $cmd"
	def value = "when off"
	if (cmd.configurationValue[0] == 1) {value = "when on"}
	if (cmd.configurationValue[0] == 2) {value = "never"}
	createEvent([name: "indicatorStatus", value: value])
}

def zwaveEvent(physicalgraph.zwave.commands.hailv1.Hail cmd) {
	createEvent([name: "hail", value: "hail", descriptionText: "Switch button was pressed", displayed: false])
}

def zwaveEvent(physicalgraph.zwave.commands.manufacturerspecificv2.ManufacturerSpecificReport cmd) {
	log.debug "manufacturerId:   ${cmd.manufacturerId}"
	log.debug "manufacturerName: ${cmd.manufacturerName}"
	log.debug "productId:        ${cmd.productId}"
	log.debug "productTypeId:    ${cmd.productTypeId}"
	def msr = String.format("%04X-%04X-%04X", cmd.manufacturerId, cmd.productTypeId, cmd.productId)
	updateDataValue("MSR", msr)
	updateDataValue("manufacturer", cmd.manufacturerName)
	createEvent([descriptionText: "$device.displayName MSR: $msr", isStateChange: false])
}

def zwaveEvent(physicalgraph.zwave.commands.switchmultilevelv1.SwitchMultilevelStopLevelChange cmd) {
	[createEvent(name:"switch", value:"on"), response(zwave.switchMultilevelV1.switchMultilevelGet().format())]
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
	// Handles all Z-Wave commands we aren't interested in
	[:]
}

def on() {
	delayBetween([
			zwave.basicV1.basicSet(value: 0xFF).format(),
			zwave.switchMultilevelV1.switchMultilevelGet().format()
	],5000)
}

def off() {
	delayBetween([
			zwave.basicV1.basicSet(value: 0x00).format(),
			zwave.switchMultilevelV1.switchMultilevelGet().format()
	],5000)
}

def setLevel(value) {
	log.debug "setLevel >> value: $value"
	def valueaux = value as Integer
	def level = Math.max(Math.min(valueaux, 99), 0)
	if (level > 0) {
		sendEvent(name: "switch", value: "on")
	} else {
		sendEvent(name: "switch", value: "off")
	}
	sendEvent(name: "level", value: level, unit: "%")
	delayBetween ([zwave.basicV1.basicSet(value: level).format(), zwave.switchMultilevelV1.switchMultilevelGet().format()], 5000)
}

def setLevel(value, duration) {
	log.debug "setLevel >> value: $value, duration: $duration"
	def valueaux = value as Integer
	def level = Math.max(Math.min(valueaux, 99), 0)
	def dimmingDuration = duration < 128 ? duration : 128 + Math.round(duration / 60)
	def getStatusDelay = duration < 128 ? (duration*1000)+2000 : (Math.round(duration / 60)*60*1000)+2000
	delayBetween ([zwave.switchMultilevelV2.switchMultilevelSet(value: level, dimmingDuration: dimmingDuration).format(),
				   zwave.switchMultilevelV1.switchMultilevelGet().format()], getStatusDelay)
}

def poll() {
	zwave.switchMultilevelV1.switchMultilevelGet().format()
}

def refresh() {
	log.debug "refresh() is called"
	def commands = []
	commands << zwave.switchMultilevelV1.switchMultilevelGet().format()
	if (getDataValue("MSR") == null) {
		commands << zwave.manufacturerSpecificV1.manufacturerSpecificGet().format()
	}
	delayBetween(commands,100)
}

def invertSwitch(invert=true) {
	if (invert) {
		zwave.configurationV1.configurationSet(configurationValue: [1], parameterNumber: 4, size: 1).format()
	}
	else {
		zwave.configurationV1.configurationSet(configurationValue: [0], parameterNumber: 4, size: 1).format()
	}
}
