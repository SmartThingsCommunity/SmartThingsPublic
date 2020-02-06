/**
 *  Copyright 2018, 2019 SmartThings
 *
 *  Provides a simulated window shade.
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
	definition (name: "Simulated Window Shade", namespace: "smartthings/testing", author: "SmartThings", runLocally: false) {
		capability "Actuator"
		capability "Window Shade"
		capability "Window Shade Preset"
		capability "Switch Level"
		capability "Window Shade Level"
		capability "Battery"

		// Commands to use in the simulator
		command "openPartially"
		command "closePartially"
		command "partiallyOpen"
		command "opening"
		command "closing"
		command "opened"
		command "closed"
		command "unknown"

		command "shadeLevel0"
		command "shadeLevel1"
		command "shadeLevel10"
		command "shadeLevel50"
		command "shadeLevel100"

		command "batteryLevel0"
		command "batteryLevel1"
		command "batteryLevel10"
		command "batteryLevel50"
		command "batteryLevel100"
	}

	preferences {
		section {
			input("actionDelay", "number",
				title: "Action Delay\n\nAn emulation for how long it takes the window shade to perform the requested action.",
				description: "In seconds (1-120; default if empty: 5 sec)",
				range: "1..120", displayDuringSetup: false)
		}
		section {
			input "preset", "number",
				title: "Preset position",
				description: "Set the window shade preset position",
				defaultValue: 50, range: "1..100", required: false, displayDuringSetup: false
		}
		section {
			input("supportedCommands", "enum",
				title: "Supported Commands\n\nThis controls the value for supportedWindowShadeCommands.",
				description: "open, close, pause", defaultValue: "2", multiple: false,
				options: [
					"1": "open, close",
					"2": "open, close, pause",
					"3": "open",
					"4": "close",
					"5": "pause",
					"6": "open, pause",
					"7": "close, pause",
					"8": "<empty list>",
					// For testing OCF/mobile client bugs
					"9": "open, closed, pause",
					"10": "open, closed, close, pause",
					"11": "plain text - not list"
				]
			)
		}
	}

	tiles(scale: 2) {
		multiAttributeTile(name:"windowShade", type: "generic", width: 6, height: 4){
			tileAttribute ("device.windowShade", key: "PRIMARY_CONTROL") {
				attributeState "open", label:'${name}', action:"close", icon:"st.shades.shade-open", backgroundColor:"#79b821", nextState:"closing"
				attributeState "closed", label:'${name}', action:"open", icon:"st.shades.shade-closed", backgroundColor:"#ffffff", nextState:"opening"
				attributeState "partially open", label:'Open', action:"close", icon:"st.shades.shade-open", backgroundColor:"#79b821", nextState:"closing"
				attributeState "opening", label:'${name}', action:"pause", icon:"st.shades.shade-opening", backgroundColor:"#79b821", nextState:"partially open"
				attributeState "closing", label:'${name}', action:"pause", icon:"st.shades.shade-closing", backgroundColor:"#ffffff", nextState:"partially open"
				attributeState "unknown", label:'${name}', action:"open", icon:"st.shades.shade-closing", backgroundColor:"#ffffff", nextState:"opening"
			}
			tileAttribute ("device.windowShadeLevel", key: "SLIDER_CONTROL") {
				attributeState "shadeLevel", action:"setShadeLevel"
			}
		}

		valueTile("blank", "device.blank", width: 2, height: 2, decoration: "flat") {
			state "default", label: ""	
		}
		valueTile("commandsLabel", "device.commands", width: 6, height: 1, decoration: "flat") {
			state "default", label: "Commands:"	
		}

		standardTile("windowShadeOpen", "device.windowShade", width: 2, height: 2, decoration: "flat") {
			state "default", label: "open", action:"open", icon:"st.Home.home2"
		}
		standardTile("windowShadeClose", "device.windowShade", width: 2, height: 2, decoration: "flat") {
			state "default", label: "close", action:"close", icon:"st.Home.home2"
		}
		standardTile("windowShadePause", "device.windowShade", width: 2, height: 2, decoration: "flat") {
			state "default", label: "pause", action:"pause", icon:"st.Home.home2"
		}
		standardTile("windowShadePreset", "device.windowShadePreset", width: 2, height: 2, decoration: "flat") {
			state "default", label: "preset", action:"presetPosition", icon:"st.Home.home2"
		}

		valueTile("statesLabel", "device.states", width: 6, height: 1, decoration: "flat") {
			state "default", label: "State Events:"	
		}

		standardTile("windowShadePartiallyOpen", "device.windowShade", width: 2, height: 2, decoration: "flat") {
			state "default", label: "partially open", action:"partiallyOpen", icon:"st.Home.home2"
		}
		standardTile("windowShadeOpening", "device.windowShade", width: 2, height: 2, decoration: "flat") {
			state "default", label: "opening", action:"opening", icon:"st.Home.home2"
		}
		standardTile("windowShadeClosing", "device.windowShade", width: 2, height: 2, decoration: "flat") {
			state "default", label: "closing", action:"closing", icon:"st.Home.home2"
		}
		standardTile("windowShadeOpened", "device.windowShade", width: 2, height: 2, decoration: "flat") {
			state "default", label: "opened", action:"opened", icon:"st.Home.home2"
		}
		standardTile("windowShadeClosed", "device.windowShade", width: 2, height: 2, decoration: "flat") {
			state "default", label: "closed", action:"closed", icon:"st.Home.home2"
		}
		standardTile("windowShadeUnknown", "device.windowShade", width: 2, height: 2, decoration: "flat") {
			state "default", label: "unknown", action:"unknown", icon:"st.Home.home2"
		}

		main(["windowShade"])
		details(["windowShade",
				 "commandsLabel",
				 "windowShadeOpen", "windowShadeClose", "windowShadePause", "windowShadePreset", "blank", "blank",
				 "statesLabel",
				 "windowShadePartiallyOpen", "windowShadeOpening", "windowShadeClosing", "windowShadeOpened", "windowShadeClosed", "windowShadeUnknown"])

	}
}

private getSupportedCommandsMap() {
	[
		"1": ["open", "close"],
		"2": ["open", "close", "pause"],
		"3": ["open"],
		"4": ["close"],
		"5": ["pause"],
		"6": ["open", "pause"],
		"7": ["close", "pause"],
		"8": [],
		// For testing OCF/mobile client bugs
		"9": ["open", "closed", "pause"],
		"10": ["open", "closed", "close", "pause"],
		"11": "open"
	]
}

private getShadeActionDelay() {
	(settings.actionDelay != null) ? settings.actionDelay : 5
}
private getShadePresetPos() {
	(settings.presetPos != null) ? settings.presetPos : 50
}

def installed() {
	log.debug "installed()"

	updated()
	opened()

	shadeLevel100()
	batteryLevel100()
}

def updated() {
	log.debug "updated()"

	def commands = (settings.supportedCommands != null) ? settings.supportedCommands : "2"

	sendEvent(name: "supportedWindowShadeCommands", value: JsonOutput.toJson(supportedCommandsMap[commands]), isStateChange: true)
}

def parse(String description) {
	log.debug "parse(): $description"
}

// Just a utility function to send level events; emulates a parse event that would contain the shade level
private updateShadeLevel(level) {
	log.debug "shadeLevel: ${level}"
	sendEvent(name: "shadeLevel", value: level, unit: "%", isStateChange: true)
	sendEvent(name: "level", value: level, unit: "%", isStateChange: true)	
}

// Capability commands

// TODO: Implement a state machine to fine tune the behavior here.
// Right now, tapping "open" and then "pause" leads to "opening",
// "partially open", then "open" as the open() command completes.
// The `runIn()`s below should all call a marshaller to handle the
// movement to a new state. This will allow for shade level sim, too.

def open() {
	log.debug "open()"
	opening()
	runIn(shadeActionDelay, "opened")
}

def close() {
	log.debug "close()"
	closing()
	runIn(shadeActionDelay, "closed")
}

def pause() {
	log.debug "pause()"
	partiallyOpen()
}

def presetPosition() {
	log.debug "presetPosition()"

	setShadeLevel(preset ?: 50)
}

def setShadeLevel(level) {
	log.debug "setShadeLevel(${level})"
	def normalizedLevel = Math.min(100, Math.max(0, level))
	def lastLevel = device.currentValue("shadeLevel") ?: 100

	// TODO: Update shade states; simulate opening or closing
	updateShadeLevel(normalizedLevel)

	if (normalizedLevel == 100) {
		opened(false)
	} else if (normalizedLevel > 0) {
		partiallyOpen(false)
	} else {
		closed(false)
	}
}

def setLevel(level, rate = 0) {
	log.debug "setLevel(${level})"
	setShadeLevel(level)
}

// Custom test commands

def openPartially() {
	log.debug "openPartially()"
	opening()
	runIn(shadeActionDelay, "partiallyOpen")
}

def closePartially() {
	log.debug "closePartially()"
	closing()
	runIn(shadeActionDelay, "partiallyOpen")
}

def partiallyOpen(updateLevel = true) {
	log.debug "windowShade: partially open"
	sendEvent(name: "windowShade", value: "partially open", isStateChange: true)
	/* // Will check ramifications before uncommenting
	if (updateLevel) {
		updateShadeLevel(random() % 99)
	}
	*/
}

def opening() {
	log.debug "windowShade: opening"
	sendEvent(name: "windowShade", value: "opening", isStateChange: true)
}

def closing() {
	log.debug "windowShade: closing"
	sendEvent(name: "windowShade", value: "closing", isStateChange: true)
}

def opened(updateLevel = true) {
	log.debug "windowShade: open"
	sendEvent(name: "windowShade", value: "open", isStateChange: true)

	if (updateLevel) {
		updateShadeLevel(100)
	}
}

def closed(updateLevel = true) {
	log.debug "windowShade: closed"
	sendEvent(name: "windowShade", value: "closed", isStateChange: true)

	if (updateLevel) {
		updateShadeLevel(0)
	}
}

def unknown() {
	// TODO: Add some "fuzzing" logic so that this gets hit every now and then?
	log.debug "windowShade: unknown"
	sendEvent(name: "windowShade", value: "unknown", isStateChange: true)
}

def shadeLevel0() {
	setShadeLevel(0)
}

def shadeLevel1() {
	setShadeLevel(1)
}

def shadeLevel10() {
	setShadeLevel(10)
}

def shadeLevel50() {
	setShadeLevel(50)
}

def shadeLevel100() {
	setShadeLevel(100)
}

def setBatteryLevel(level) {
	log.debug "battery: ${level}"
	sendEvent(name: "battery", value: level, unit: "%", isStateChange: true)
}

def batteryLevel0() {
	setBatteryLevel(0)
}

def batteryLevel1() {
	setBatteryLevel(1)
}

def batteryLevel10() {
	setBatteryLevel(10)
}

def batteryLevel50() {
	setBatteryLevel(50)
}

def batteryLevel100() {
	setBatteryLevel(100)
}
