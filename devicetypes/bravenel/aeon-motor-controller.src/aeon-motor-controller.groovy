/**
 *  Aeon Motor Controller
 *
 *  Copyright 2015 Bruce Ravenel
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
	definition (name: "Aeon Motor Controller", namespace: "bravenel", author: "Bruce Ravenel") {
		capability "Refresh"
		capability "Actuator"
        capability "doorControl"
        capability "Switch"

        command "up"
        command "down"
        command "stop"

//		fingerprint inClusters: "0x26,0x32"
	}

	simulator {
        status "up":   "command: 2604, payload: FF"
        status "down": "command: 2604, payload: 00"
        status "stop": "command: 2605, payload: FE"

		["FF", "FE", "00"].each { val ->
			reply "2001$val,delay 100,2602": "command: 2603, payload: $val"
		}
	}

	tiles {
		standardTile("motor", "device.motor", width: 2, height: 2) {
			state("stopUp", label:'stop', icon:"st.doors.garage.garage-open", action: 'down', backgroundColor:"#79b821")
			state("stopDn", label:'stop', icon:"st.doors.garage.garage-closed", action: 'up', backgroundColor:"#79b821")
			state("up", label:'up', icon:"st.doors.garage.garage-opening", action:'stop', backgroundColor:"#ffe71e")
			state("down", label:'down', icon:"st.doors.garage.garage-closing", action:'stop', backgroundColor:"#ffe71e")
		}
		valueTile("energy", "device.energy", decoration: "flat") {
			state "default", label:' '
		}
		standardTile("refresh", "device.switch", inactiveLabel: false, decoration: "flat") {
			state "default", label:"", action:"refresh.refresh", icon:"st.secondary.refresh"
		}
        standardTile("stop", "device.switch") {
        	state "default", label:"", action: "stop", icon:"http://cdn.device-icons.smartthings.com/sonos/stop-btn@2x.png"
        }
        standardTile("up", "device.switch") {
        	state "default", label: "Up", action: "up", icon:"http://cdn.device-icons.smartthings.com/thermostat/thermostat-up@2x.png"
        }
        standardTile("down", "device.switch") {
        	state "default", label: "Down", action: "down", icon:"http://cdn.device-icons.smartthings.com/thermostat/thermostat-down@2x.png"
        }
	}
	main(["motor"])
	details(["motor",  "refresh", "energy", "up", "down", "stop",])
}

// parse events into attributes
def parse(String description) {
	def result = []
	def cmd = zwave.parse(description, [0x20: 1, 0x26: 3])
	if (cmd) {
		result = zwaveEvent(cmd)
		log.debug("'$description' parsed to $result")
	} else {
		log.debug("Couldn't zwave.parse '$description'")
	}
	result
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd) {
	motorEvents(cmd)
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicSet cmd) {
	motorEvents(cmd)
}

def zwaveEvent(physicalgraph.zwave.commands.switchmultilevelv3.SwitchMultilevelReport cmd) {
	motorEvents(cmd)
}

def motorEvents(physicalgraph.zwave.Command cmd) {
	def result = []
    def switchEvent = []
    if(cmd.value == 0) {switchEvent = createEvent(name: "motor", value: "down", descriptionText: text)}
    else if(cmd.value == 254) {
    	def stopVal = state.up ? "stopUp" : "stopDn"
    	switchEvent = createEvent(name: "motor", value: stopVal, descriptionText: text)
    }
    else if(cmd.value == 255) {switchEvent = createEvent(name: "motor", value: "up", descriptionText: text)}
	result << switchEvent
}

def refresh() {
	zwave.switchMultilevelV1.switchMultilevelGet().format()
}

def up() {
	state.up = true
	delayBetween([
    	zwave.basicV1.basicSet(value: 0xFF).format(),
		zwave.switchMultilevelV1.switchMultilevelGet().format()
    ], 1000)
}

def down() {
	state.up = false
	delayBetween([
    	zwave.basicV1.basicSet(value: 0x00).format(),
		zwave.switchMultilevelV1.switchMultilevelGet().format()
    ], 1000)
}

def stop() {
	delayBetween([
    	zwave.switchMultilevelV1.switchMultilevelStopLevelChange().format(),
		zwave.switchMultilevelV1.switchMultilevelGet().format()
    ], 1000)
}

def on() {
	state.up = true
	delayBetween([
    	zwave.basicV1.basicSet(value: 0xFF).format(),
		zwave.switchMultilevelV1.switchMultilevelGet().format()
    ], 1000)
}

def off() {
	state.up = false
	delayBetween([
    	zwave.basicV1.basicSet(value: 0x00).format(),
		zwave.switchMultilevelV1.switchMultilevelGet().format()
    ], 1000)
}