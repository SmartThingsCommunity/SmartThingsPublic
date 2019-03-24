/**
 *  Econet EV100 Vent
 *
 *  Copyright 2014 SmartThings
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
	// Automatically generated. Make future change here.
	definition (name: "EcoNet Vent", namespace: "smartthings", author: "SmartThings") {
		capability "Switch Level"
		capability "Actuator"
		capability "Switch"
		capability "Battery"
		capability "Refresh"
		capability "Sensor"
        capability "Polling"
        capability "Configuration"
		capability "Health Check"

		command "open"
		command "close"

		fingerprint deviceId: "0x1100", inClusters: "0x26,0x72,0x86,0x77,0x80,0x20"
		fingerprint mfr:"0157", prod:"0100", model:"0100", deviceJoinName: "EcoNet Controls Z-Wave Vent"
	}

	simulator {
		status "on":  "command: 2603, payload: FF"
		status "off": "command: 2603, payload: 00"
		status "09%": "command: 2603, payload: 09"
		status "10%": "command: 2603, payload: 0A"
		status "33%": "command: 2603, payload: 21"
		status "66%": "command: 2603, payload: 42"
		status "99%": "command: 2603, payload: 63"

		// reply messages
		reply "2001FF,delay 100,2602": "command: 2603, payload: FF"
		reply "200100,delay 100,2602": "command: 2603, payload: 00"
		reply "200119,delay 100,2602": "command: 2603, payload: 19"
		reply "200132,delay 100,2602": "command: 2603, payload: 32"
		reply "20014B,delay 100,2602": "command: 2603, payload: 4B"
		reply "200163,delay 100,2602": "command: 2603, payload: 63"
	}

	tiles {
		standardTile("switch", "device.switch", width: 2, height: 2, canChangeIcon: true) {
			state "on", action:"switch.off", icon:"st.vents.vent-open-text", backgroundColor:"#00a0dc"
			state "off", action:"switch.on", icon:"st.vents.vent-closed", backgroundColor:"#ffffff"
		}
        valueTile("battery", "device.battery", inactiveLabel: false, decoration: "flat") {
			state "battery", label:'${currentValue}% battery', unit:""
		}
		controlTile("levelSliderControl", "device.level", "slider", height: 1, width: 3, inactiveLabel: false, range:"(0..100)") {
			state "level", action:"switch level.setLevel"
		}
		standardTile("refresh", "device.switch", inactiveLabel: false, decoration: "flat") {
			state "default", label:"", action:"refresh.refresh", icon:"st.secondary.refresh"
		}

		main(["switch"])
		details(["switch","battery","refresh","levelSliderControl"])
	}
}

def parse(String description) {
	def result = []
	def cmd = zwave.parse(description, [0x20: 1, 0x26: 3, 0x70: 1, 0x32:3])
	if (cmd) {
		result = zwaveEvent(cmd)
        log.debug("'$description' parsed to $result")
	} else {
		log.debug("Couldn't zwave.parse '$description'")
	}
    result
}

def installed() {
	// Device-Watch simply pings if no device events received for 32min(checkInterval)
	sendEvent(name: "checkInterval", value: 2 * 15 * 60 + 2 * 60, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])
}

//send the command to stop polling
def updated() {
	// Device-Watch simply pings if no device events received for 32min(checkInterval)
	sendEvent(name: "checkInterval", value: 2 * 15 * 60 + 2 * 60, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])
	response("poll stop")
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd) {
	dimmerEvents(cmd) + [ response("poll stop") ]  // we get a BasicReport when the hub starts polling
}

//parse manufacture name and store it
def zwaveEvent(physicalgraph.zwave.commands.manufacturerspecificv2.ManufacturerSpecificReport cmd) {
	if (state.manufacturer != cmd.manufacturerName) {
		updateDataValue("manufacturer", cmd.manufacturerName)
	}
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicSet cmd) {
	dimmerEvents(cmd)
}

def zwaveEvent(physicalgraph.zwave.commands.switchmultilevelv1.SwitchMultilevelReport cmd) {
	dimmerEvents(cmd)
}

def zwaveEvent(physicalgraph.zwave.commands.switchmultilevelv3.SwitchMultilevelReport cmd) {
	dimmerEvents(cmd)
}

def dimmerEvents(physicalgraph.zwave.Command cmd) {
	def text = "$device.displayName is ${cmd.value ? "open" : "closed"}"
	def switchEvent = createEvent(name: "switch", value: (cmd.value ? "on" : "off"), descriptionText: text)
	def levelEvent = createEvent(name:"level", value: cmd.value == 99 ? 100 : cmd.value , unit:"%")
	[switchEvent, levelEvent]
}

def zwaveEvent(physicalgraph.zwave.commands.batteryv1.BatteryReport cmd) {
	def map = [ name: "battery", unit: "%" ]
	if (cmd.batteryLevel == 0xFF) {
		map.value = 1
		map.descriptionText = "${device.displayName} has a low battery"
		map.isStateChange = true
	} else {
		map.value = cmd.batteryLevel
	}
	state.lastbat = new Date().time
	createEvent(map)
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
	def linkText = device.label ?: device.name
	[linkText: linkText, descriptionText: "$linkText: $cmd", displayed: false]
}

def on() {
	delayBetween([
		zwave.basicV1.basicSet(value: 0xFF).format(),
		zwave.switchMultilevelV1.switchMultilevelGet().format()
	])
}

def open() {
	on()
}

def off() {
	delayBetween([
		zwave.basicV1.basicSet(value: 0x00).format(),
		zwave.switchMultilevelV1.switchMultilevelGet().format()
	])
}

def close() {
	off()
}

def setLevel(value) {
	delayBetween([
		zwave.basicV1.basicSet(value: value).format(),
		zwave.switchMultilevelV1.switchMultilevelGet().format()
	])
}

def setLevel(value, duration) {
	setLevel(value)
}

/**
 * PING is used by Device-Watch in attempt to reach the Device
 * */
def ping() {
	refresh()
}

def refresh() {
	delayBetween([
		zwave.switchMultilevelV1.switchMultilevelGet().format(),
		zwave.batteryV1.batteryGet().format()
	], 2200)
}

//poll for battery once a day
def poll() {
    
	if (secondsPast(state.lastbatt, 36*60*60)) {
		return zwave.batteryV1.batteryGet().format()
	} else {
    
		return zwave.switchMultilevelV1.switchMultilevelGet().format()
	}
}
def configure() {
	[
    zwave.manufacturerSpecificV1.manufacturerSpecificGet().format()
    ]
    refresh()
}

//check last message so battery poll doesn't happen all the time
private Boolean secondsPast(timestamp, seconds) {
	if (!(timestamp instanceof Number)) {
		if (timestamp instanceof Date) {
			timestamp = timestamp.time
		} else if ((timestamp instanceof String) && timestamp.isNumber()) {
			timestamp = timestamp.toLong()
		} else {
			return true
		}
	}
	return (new Date().time - timestamp) > (seconds * 1000)
}

//Warn message for unkown events
def createEvents(physicalgraph.zwave.Command cmd) {
	log.warn "UNEXPECTED COMMAND: $cmd"
}
