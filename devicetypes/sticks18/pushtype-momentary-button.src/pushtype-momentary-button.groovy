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
 *  Momentary Button Tile
 *
 *  Author: Scott G
 *
 *  Date: 2016-18-01
 *
 *  Version 1.0 - Initial beta release of new tile. Triggers different "pushtype" events for on() vs off() commands.
 *
 */
metadata {
	definition (name: "Pushtype Momentary Button", namespace: "sticks18", author: "Scott G") {
		capability "Actuator"
		capability "Switch"
		capability "Momentary"
		capability "Sensor"

attribute "pushtype", "string"
	}

	// simulator metadata
	simulator {
	}

	// UI tile definitions
	tiles {
		standardTile("switch", "device.switch", width: 2, height: 2, canChangeIcon: true) {
			state "off", label: 'Push', action: "momentary.push", backgroundColor: "#ffffff", nextState: "on"
			state "on", label: 'Push', action: "momentary.push", backgroundColor: "#53a7c0"
		}
		main "switch"
		details "switch"
	}
}

def parse(String description) {
}

def push(type) {
	sendEvent(name: "switch", value: "on", isStateChange: true)
	sendEvent(name: "switch", value: "off", isStateChange: true)
	sendEvent(name: "pushtype", value: type, isStateChange: true)
}

def on() {
	push("on")
}

def off() {
	push("off")
}