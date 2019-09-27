/**
 *  Copyright 2018 SmartThings
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
metadata {
	definition (name: "Simulated Window Shade", namespace: "smartthings", author: "SmartThings", runLocally: false, mnmn: "SmartThings", vid: "generic-window-shade") {
		capability "Actuator"
		capability "Window Shade"
		capability "Window Shade Preset"

		// Commands to use in the simulator
		command "openPartially"
		command "closePartially"
		command "partiallyOpen"
		command "opening"
		command "closing"
		command "opened"
		command "closed"
		command "unknown"
	}

	preferences {}

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
			tileAttribute ("device.level", key: "SLIDER_CONTROL") {
				attributeState "level", action:"setLevel"
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

def parse(String description) {
}

def open() {
	log.debug "open()"
	opening()
	runIn(5, "opened")
}

def close() {
	log.debug "close()"
	closing()
	runIn(5, "closed")
}

def pause() {
	log.debug "pause()"
	partiallyOpen()
}

def openPartially() {
	log.debug "openPartially()"
	opening()
	runIn(5, "partiallyOpen")
}

def closePartially() {
	log.debug "closePartially()"
	closing()
	runIn(5, "partiallyOpen")
}

def partiallyOpen() {
	log.debug "- partially open"
	sendEvent(name: "windowShade", value: "partially open", isStateChange: true)
}

def opening() {
	log.debug "- opening"
	sendEvent(name: "windowShade", value: "opening", isStateChange: true)
}

def closing() {
	log.debug "- closing"
	sendEvent(name: "windowShade", value: "closing", isStateChange: true)
}

def opened() {
	log.debug "- open"
	sendEvent(name: "windowShade", value: "open", isStateChange: true)
}

def closed() {
	log.debug "- closed"
	sendEvent(name: "windowShade", value: "closed", isStateChange: true)
}

def unknown() {
	// TODO: Add some "fuzzing" logic so that this gets hit every now and then?
	log.debug "- unknown"
	sendEvent(name: "windowShade", value: "unknown", isStateChange: true)
}

def presetPosition() {
	log.debug "presetPosition()"
	if (device.currentValue("windowShade") == "open") {
		closePartially()
	} else if (device.currentValue("windowShade") == "closed") {
		openPartially()
	} else {
		partiallyOpen()
	}
}

def installed() {
	opened()
}

def updated() {
	installed()
}
