/**
 *  Copyright 2017 SmartThings
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
import groovy.json.JsonOutput

metadata {
	definition (name: "iblinds Z-Wave", namespace: "iblinds", author: "HABHomeIntel", ocfDeviceType: "oic.d.blind",  mnmn: "SmartThings", vid: "generic-shade-3") {
		capability "Switch Level"
		capability "Switch"
		capability "Battery"
		capability "Refresh"
		capability "Actuator"
		capability "Health Check"
		capability "Window Shade"
		capability "Window Shade Preset"
		capability "Window Shade Level"

		fingerprint mfr:"0287", prod:"0003", model:"000D", deviceJoinName: "iBlinds Motor"
		fingerprint mfr:"0287", prod:"0004", model:"0071", deviceJoinName: "iBlinds Motor"
	}

	simulator {
		status "open":  "command: 2603, payload: FF"
		status "closed": "command: 2603, payload: 00"
		status "10%": "command: 2603, payload: 0A"
		status "66%": "command: 2603, payload: 42"
		status "99%": "command: 2603, payload: 63"
		status "battery 100%": "command: 8003, payload: 64"
		status "battery low": "command: 8003, payload: FF"

		// reply messages
		reply "2001FF,delay 1000,2602": "command: 2603, payload: 10 FF FE"
		reply "200100,delay 1000,2602": "command: 2603, payload: 60 00 FE"
		reply "200142,delay 1000,2602": "command: 2603, payload: 10 42 FE"
		reply "200163,delay 1000,2602": "command: 2603, payload: 10 63 FE"
	}

	tiles(scale: 2) {
		multiAttributeTile(name:"windowShade", type: "lighting", width: 6, height: 4){
			tileAttribute("device.windowShade", key: "PRIMARY_CONTROL") {
				attributeState "open", label: '${name}', action: "close", icon: "http://i.imgur.com/4TbsR54.png", backgroundColor: "#79b821", nextState: "closing"
				attributeState "closed", label: '${name}', action: "open", icon: "st.shades.shade-closed", backgroundColor: "#ffffff", nextState: "opening"
				attributeState "partially open", label: 'Open', action: "close", icon: "st.shades.shade-open", backgroundColor: "#79b821", nextState: "closing"
				attributeState "opening", label: '${name}', action: "pause", icon: "st.shades.shade-opening", backgroundColor: "#79b821", nextState: "partially open"
				attributeState "closing", label: '${name}', action: "pause", icon: "st.shades.shade-closing", backgroundColor: "#ffffff", nextState: "partially open"
			}
			tileAttribute("device.level", key: "SLIDER_CONTROL") {
				attributeState "level", action: "setShadeLevel"
			}
		}

		standardTile("home", "device.level", width: 2, height: 2, decoration: "flat") {
			state "default", label: "home", action: "presetPosition", icon: "st.Home.home2"
		}

		standardTile("refresh", "device.refresh", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
			state "default", label: '', action: "refresh.refresh", icon: "st.secondary.refresh", nextState: "disabled"
			state "disabled", label: '', action: "", icon: "st.secondary.refresh"
		}

		valueTile("battery", "device.battery", decoration: "flat", inactiveLabel: false, width: 2, height: 2) {
			state "battery", label: '${currentValue}% battery', unit: ""
		}

		preferences {
			input "preset", "number", title: "Preset position", description: "Set the window shade preset position", defaultValue: 50, range: "1..100", required: false, displayDuringSetup: false

			// User has the option of opening/closing with slats down or up -- this controls the behavior when using Window Shade Level
			input "reverse", "bool", title: "Reverse", description: "Reverse blind direction", defaultValue: false, required: false, displayDuringSetup: false

			// Right now the device does not auto-report SwitchMultilevelReport so we will use this to determine, based on the user's setup, how long to delay before SwitchMultilevelGet
		}

		main(["windowShade"])
		details(["windowShade", "home", "refresh", "battery"])
	}
}

def parse(String description) {
	def result = null
	//if (description =~ /command: 2603, payload: ([0-9A-Fa-f]{6})/)
	// TODO: Workaround manual parsing of v4 multilevel report
	def cmd = zwave.parse(description, [0x20: 1, 0x26: 3])  // TODO: switch to SwitchMultilevel v4 and use target value

	if (cmd) {
		result = zwaveEvent(cmd)
	}

	log.debug "Parsed '$description' to ${result.inspect()}"
	return result
}

def getCheckInterval() {
	// iblinds is a battery-powered device, and it's not very critical
	// to know whether they're online or not – 12 hrs
	12 * 60 * 60 //12 hours
}

def installed() {
	sendEvent(name: "checkInterval", value: checkInterval, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])
	sendEvent(name: "supportedWindowShadeCommands", value: JsonOutput.toJson(["open", "close"]), displayed: false)
	response(refresh())
}

def updated() {
	if (device.latestValue("checkInterval") != checkInterval) {
		sendEvent(name: "checkInterval", value: checkInterval, displayed: false)
	}

	if (!device.latestState("battery")) {
		response(zwave.batteryV1.batteryGet())
	}
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd) {
	handleLevelReport(cmd)
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicSet cmd) {
	handleLevelReport(cmd)
}

def zwaveEvent(physicalgraph.zwave.commands.switchmultilevelv3.SwitchMultilevelReport cmd) {
	handleLevelReport(cmd)
}

def zwaveEvent(physicalgraph.zwave.commands.switchmultilevelv3.SwitchMultilevelStopLevelChange cmd) {
	[ createEvent(name: "windowShade", value: "partially open", displayed: false, descriptionText: "$device.displayName shade stopped"),
	  response(zwave.switchMultilevelV1.switchMultilevelGet().format()) ]
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

	state.lastbatt = now()

	createEvent(map)
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
	log.debug "unhandled $cmd"

	return []
}

/**
 * Helper functions for converting between parabolic slat tilt values and
 * SmartThings capability percentages, taking in account user preferences.
 *
 * Device behavior:
 *      0 is 0% slats down (closed)
 *   1-49 is 1-99% slats down (partially open)
 *     50 is 100% open slats horizontal (open)
 *  51-98 is 99-1% slats up (51 is 99% open) (partially open)
 *     99 is 0% slats up (closed)
 *
 * Slat orientation:
 *     Up: user view ->		\
 *   Down: user view ->		/
 *   Open: user view ->		--
 */

private getTILTLEVEL_FULLY_OPEN() { 50 }
private getTILTLEVEL_FULLY_CLOSED_UP() { 99 }
private getTILTLEVEL_FULLY_CLOSED_DOWN() { 0 }

private getSlatDirectionMap() {[
 	DOWN: 0,
 	UP: 1
]}

private deviceEventToCapability(deviceLevel) {
	def capabilityLevel = deviceLevel
	def direction = slatDirectionMap.DOWN

	// TODO: convert percentages based on above to capability level and direction

	return [level: capabilityLevel, direction: direction]
}

private capabilityEventToDevice(level, direction) {
	def deviceLevel = level

	// TODO: convert capability level and direction to device percent taking in account user preferences

	return deviceLevel
}

/**
 * Device handler helper function
 */

private buildWindowShadeEvents(level) {
	def result = []
	def descriptionText = null
	def shadeValue = null

	if (level >= 99) {
		level = 100
		shadeValue = "open"
	} else if (level <= 0) {
		level = 0
		shadeValue = "closed"
	} else {
		shadeValue = "partially open"
		descriptionText = "${device.displayName} tilt is ${shadeLevel.level}% open"
	}

	result << createEvent(name: "level", value: level, unit: "%")
	result << createEvent(name: "shadeLevel", value: level, unit: "%")
	result << createEvent(name: "windowShade", value: shadeValue, descriptionText: descriptionText)

	return result
}

private handleLevelReport(physicalgraph.zwave.Command cmd) {
	def shadeLevel = deviceEventToCapability(cmd.value as Integer)
	def result = buildWindowShadeEvents(shadeLevel.level)

	if (!state.lastbatt || now() - state.lastbatt > 24 * 60 * 60 * 1000) {
		log.debug "requesting battery"
		state.lastbatt = (now() - 23 * 60 * 60 * 1000) // don't queue up multiple battery reqs in a row
		result << response(["delay 15000", zwave.batteryV1.batteryGet().format()])
	}

	return result
}

/**
 * Capability commands
 */

def open() {
	log.debug "open()"

	setShadeLevel(100)
}

def close() {
	log.debug "close()"

	setShadeLevel(0)
}

def pause() {
	log.debug "pause()"

	zwave.switchMultilevelV3.switchMultilevelStopLevelChange().format()
}

def setLevel(value, duration = null) {
	log.debug "setLevel($value)"

	setShadeLevel(value)
}

def setShadeLevel(value) {
	def descriptionText = null

	log.debug "setShadeLevel($value)"
	Integer level = Math.max(Math.min((value as Integer), 99), 0)
	Integer tiltLevel = level / 2 // we will use this value to decide what level is sent to device (reverse or not reversed)

	if (reverse) {  // Check to see if user wants blinds to operate in reverse direction
		tiltLevel = 99 - level
	}

	if (level <= 0) {
		//level = 0
		sendEvent(name: "windowShade", value: "closed")
	} else if (level >= 99) {
		level = 99
		sendEvent(name: "windowShade", value: "closed")
	} else if (level == 50) {
		sendEvent(name: "windowShade", value: "open")
	} else {
		descriptionText = "${device.displayName} tilt level is ${level}% open"
		sendEvent(name: "windowShade", value: "partially open" , descriptionText: descriptionText)
	}

	//log.debug "Level - ${level}%  & Tilt Level - ${tiltLevel}%"
	sendEvent(name: "level", value: level, unit: "%", descriptionText: descriptionText)
	sendEvent(name: "shadeLevel", value: level, unit: "%", descriptionText: descriptionText)

	zwave.switchMultilevelV3.switchMultilevelSet(value: tiltLevel).format()
}

def presetPosition() {
	setShadeLevel(preset != null ? preset : 50)
}

def ping() {
	refresh()
}

def refresh() {
	log.debug "refresh()"

	delayBetween([
		zwave.switchMultilevelV1.switchMultilevelGet().format(),
		zwave.batteryV1.batteryGet().format()
	], 1500)
}
