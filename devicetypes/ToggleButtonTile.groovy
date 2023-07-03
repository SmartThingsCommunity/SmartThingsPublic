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
 *  Toggle Button Tile
 *
 *  Author: SBDOBRESCU
 *
 *  Date: 2015-11-13
 */
metadata {
	definition (name: "Toggle Button Tile", namespace: "smartthings", author: "Bobby Dobrescu") {
		capability "Actuator"
		capability "Switch"
		capability "Sensor"
	}

	// simulator metadata
	simulator {
	}

	// UI tile definitions
	tiles {
		standardTile("button", "device.switch", width: 2, height: 2, canChangeIcon: true) {
			state "off", label: 'Off', action: "switch.on", icon: "st.switches.switch.off", backgroundColor: "#ffffff", nextState: "on"
			state "on", label: 'On', action: "switch.off", icon: "st.switches.switch.on", backgroundColor: "#79b821", nextState: "off"
		}
		main "button"
		details "button"
	}
}

def parse(String description) {
	log.trace "parse($description)"
}

def on() { 
	push() 

}
def off() { 
	push() 
}

def push() { 
	def toggleState = device.currentState("switch")?.value
	log.debug "Current State ${device.currentState("switch")?.value}"
    if (toggleState == "on") {
		sendEvent(name: "switch", value: "off", isStateChange: true, display: false)
	} else if (toggleState == "off") {
		sendEvent(name: "switch", value: "on", isStateChange: true, display: false)
	} 
	sendEvent(name: "momentary", value: "pushed", display: false, displayed: false)
}
