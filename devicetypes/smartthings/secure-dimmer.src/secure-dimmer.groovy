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
	definition (name: "Secure Dimmer", namespace: "smartthings", author: "SmartThings") {
		capability "Switch Level"
		capability "Actuator"
		capability "Switch"
		capability "Refresh"
		capability "Sensor"

		fingerprint deviceId: "0x11", inClusters: "0x98"
	}

	simulator {
		status "on":  "command: 9881, payload: 002603FF"
		status "off": "command: 9881, payload: 00260300"
		status "09%": "command: 9881, payload: 00260309"
		status "10%": "command: 9881, payload: 0026030A"
		status "33%": "command: 9881, payload: 00260321"
		status "66%": "command: 9881, payload: 00260342"
		status "99%": "command: 9881, payload: 00260363"

		// reply messages
		reply "9881002001FF,delay 100,9881002602": "command: 9881, payload: 002603FF"
		reply "988100200100,delay 100,9881002602": "command: 9881, payload: 00260300"
		reply "988100200119,delay 100,9881002602": "command: 9881, payload: 00260319"
		reply "988100200132,delay 100,9881002602": "command: 9881, payload: 00260332"
		reply "98810020014B,delay 100,9881002602": "command: 9881, payload: 0026034B"
		reply "988100200163,delay 100,9881002602": "command: 9881, payload: 00260363"
	}

	tiles {
		standardTile("switch", "device.switch", width: 2, height: 2, canChangeIcon: true) {
			state "on", label:'${name}', action:"switch.off", icon:"st.switches.switch.on", backgroundColor:"#00A0DC", nextState:"turningOff"
			state "off", label:'${name}', action:"switch.on", icon:"st.switches.switch.off", backgroundColor:"#ffffff", nextState:"turningOn"
			state "turningOn", label:'${name}', action:"switch.off", icon:"st.switches.switch.on", backgroundColor:"#00A0DC", nextState:"turningOff"
			state "turningOff", label:'${name}', action:"switch.on", icon:"st.switches.switch.off", backgroundColor:"#ffffff", nextState:"turningOn"
		}
		controlTile("levelSliderControl", "device.level", "slider", height: 1, width: 3, inactiveLabel: false) {
			state "level", action:"switch level.setLevel"
		}
		standardTile("refresh", "device.switch", inactiveLabel: false, decoration: "flat") {
			state "default", label:"", action:"refresh.refresh", icon:"st.secondary.refresh"
		}

		main(["switch"])
		details(["switch", "levelSliderControl", "refresh"])
	}
}

def parse(String description) {
	if (description.startsWith("Err 106")) {
		state.sec = 0
		createEvent(descriptionText: description, isStateChange: true)
	} else {
		def cmd = zwave.parse(description, [0x20: 1, 0x26: 3, 0x70: 1, 0x32:3, 0x98: 1])
		if (cmd) {
			zwaveEvent(cmd)
		} else {
			log.debug("Couldn't zwave.parse '$description'")
			null
		}
	}
}

def zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityMessageEncapsulation cmd) {
	def encapsulatedCommand = cmd.encapsulatedCommand([0x20: 1, 0x26: 3, 0x32: 3])
	state.sec = 1
	if (encapsulatedCommand) {
		zwaveEvent(encapsulatedCommand)
	}
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd) {
	dimmerEvents(cmd)
}

def zwaveEvent(physicalgraph.zwave.commands.switchmultilevelv1.SwitchMultilevelReport cmd) {
	dimmerEvents(cmd)
}

def zwaveEvent(physicalgraph.zwave.commands.switchmultilevelv3.SwitchMultilevelReport cmd) {
	dimmerEvents(cmd)
}

private dimmerEvents(physicalgraph.zwave.Command cmd) {
	def value = (cmd.value ? "on" : "off")
	def result = [createEvent(name: "switch", value: value, descriptionText: "$device.displayName was turned $value")]
	if (cmd.value) {
		result << createEvent(name: "level", value: cmd.value, unit: "%")
	}
	return result
}

def zwaveEvent(physicalgraph.zwave.commands.meterv3.MeterReport cmd) {
	def map = [:]
	if (cmd.meterType == 1) {
		if (cmd.scale == 0) {
			map = [name: "energy", value: cmd.scaledMeterValue, unit: "kWh"]
		} else if (cmd.scale == 1) {
			map = [name: "energy", value: cmd.scaledMeterValue, unit: "kVAh"]
		} else if (cmd.scale == 2) {
			map = [name: "power", value: cmd.scaledMeterValue, unit: "W"]
		} else {
			map = [name: "electric", value: cmd.scaledMeterValue, unit: ["pulses", "V", "A", "R/Z", ""][cmd.scale - 3]]
		}
	} else if (cmd.meterType == 2) {
		map = [name: "gas", value: cmd.scaledMeterValue, unit: ["m^3", "ft^3", "", "pulses", ""][cmd.scale]]
	} else if (cmd.meterType == 3) {
		map = [name: "water", value: cmd.scaledMeterValue, unit: ["m^3", "ft^3", "gal"][cmd.scale]]
	}
	map.isStateChange = true  // just show in activity
	createEvent(map)
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
	def linkText = device.label ?: device.name
	[linkText: linkText, descriptionText: "$linkText: $cmd", displayed: false]
}

def on() {
	secureSequence([
		zwave.basicV1.basicSet(value: 0xFF),
		zwave.switchMultilevelV1.switchMultilevelGet()
	], 3500)
}

def off() {
	secureSequence([
		zwave.basicV1.basicSet(value: 0x00),
		zwave.switchMultilevelV1.switchMultilevelGet()
	], 3500)
}

def setLevel(value) {
	secureSequence([
		zwave.basicV1.basicSet(value: value),
		zwave.switchMultilevelV1.switchMultilevelGet()
	])
}

def setLevel(value, duration) {
	def dimmingDuration = duration < 128 ? duration : 128 + Math.round(duration / 60)
	secure(zwave.switchMultilevelV2.switchMultilevelSet(value: value, dimmingDuration: dimmingDuration))
}

def refresh() {
	secure(zwave.switchMultilevelV1.switchMultilevelGet())
}

private secure(physicalgraph.zwave.Command cmd) {
	if (state.sec) {
		zwave.securityV1.securityMessageEncapsulation().encapsulate(cmd).format()
	} else {
		cmd.format()
	}
}

private secureSequence(Collection commands, ...delayBetweenArgs) {
	delayBetween(commands.collect{ secure(it) }, *delayBetweenArgs)
}
