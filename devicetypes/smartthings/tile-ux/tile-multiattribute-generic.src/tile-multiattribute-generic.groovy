/**
 *  Copyright 2016 SmartThings, Inc.
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
	definition (name: "genericDeviceTile", namespace: "smartthings/tile-ux", author: "SmartThings") {
		capability "Actuator"
		capability "Switch"
		capability "Switch Level"

		command "levelUp"
		command "levelDown"
		command "randomizeLevel"
	}

	tiles(scale: 2) {
		multiAttributeTile(name:"basicTile", type:"generic", width:6, height:4) {
			tileAttribute("device.switch", key: "PRIMARY_CONTROL") {
				attributeState "on", label:'${name}', action:"switch.off", icon:"st.switches.switch.on", backgroundColor:"#79b821", nextState:"turningOff"
				attributeState "off", label:'${name}', action:"switch.on", icon:"st.switches.switch.off", backgroundColor:"#ffffff", nextState:"turningOn"
				attributeState "turningOn", label:'${name}', action:"switch.off", icon:"st.switches.switch.on", backgroundColor:"#79b821", nextState:"turningOff"
				attributeState "turningOff", label:'${name}', action:"switch.on", icon:"st.switches.switch.off", backgroundColor:"#ffffff", nextState:"turningOn"
			}
		}
		multiAttributeTile(name:"sliderTile", type:"generic", width:6, height:4) {
			tileAttribute("device.switch", key: "PRIMARY_CONTROL") {
				attributeState "on", label:'${name}', backgroundColor:"#79b821", nextState:"turningOff"
				attributeState "off", label:'${name}', backgroundColor:"#ffffff", nextState:"turningOn"
				attributeState "turningOn", label:'${name}', backgroundColor:"#79b821", nextState:"turningOff"
				attributeState "turningOff", label:'${name}', backgroundColor:"#ffffff", nextState:"turningOn"
			}
			tileAttribute("device.level", key: "SECONDARY_CONTROL") {
				attributeState "level", icon: 'st.Weather.weather1', action:"randomizeLevel", defaultState: true
			}
			tileAttribute("device.level", key: "SLIDER_CONTROL") {
				attributeState "level", action:"switch level.setLevel", defaultState: true
			}
		}
		multiAttributeTile(name:"valueTile", type:"generic", width:6, height:4) {
			tileAttribute("device.level", key: "PRIMARY_CONTROL") {
				attributeState "level", label:'${currentValue}', defaultState: true, backgroundColors:[
					[value: 0, color: "#ff0000"],
					[value: 20, color: "#ffff00"],
					[value: 40, color: "#00ff00"],
					[value: 60, color: "#00ffff"],
					[value: 80, color: "#0000ff"],
					[value: 100, color: "#ff00ff"]
				]
			}
			tileAttribute("device.switch", key: "SECONDARY_CONTROL") {
				attributeState "on", label:'${name}', action:"switch.off", icon:"st.switches.switch.on", backgroundColor:"#79b821", nextState:"turningOff"
				attributeState "off", label:'${name}', action:"switch.on", backgroundColor:"#ffffff", nextState:"turningOn"
				attributeState "turningOn", label:'…', action:"switch.off", icon:"st.switches.switch.on", backgroundColor:"#79b821", nextState:"turningOff"
				attributeState "turningOff", label:'…', action:"switch.on", backgroundColor:"#ffffff", nextState:"turningOn"
			}
			tileAttribute("device.level", key: "VALUE_CONTROL") {
				attributeState "VALUE_UP", action: "levelUp"
				attributeState "VALUE_DOWN", action: "levelDown"
			}
		}
		multiAttributeTile(name:"lengthyTile", type:"generic", width:6, height:4) {
			tileAttribute("device.lengthyText", key: "PRIMARY_CONTROL") {
				attributeState "lengthyText", label:'The value of this tile is long and should wrap to two lines', backgroundColor:"#79b821", defaultState: true
			}
			tileAttribute("device.lengthyText", key: "SECONDARY_CONTROL") {
				attributeState "lengthyText", label:'The value of this tile is long and should wrap to two lines', backgroundColor:"#79b821", defaultState: true
			}
		}
		multiAttributeTile(name:"multilineTile", type:"generic", width:6, height:4) {
			tileAttribute("device.multilineText", key: "PRIMARY_CONTROL") {
				attributeState "multiLineText", label:'Line 1 YES\nLine 2 YES\nLine 3 NO', backgroundColor:"#79b821", defaultState: true
			}
			tileAttribute("device.multilineText", key: "SECONDARY_CONTROL") {
				attributeState "multiLineText", label:'Line 1 YES\nLine 2 YES\nLine 3 NO', backgroundColor:"#79b821", defaultState: true
			}
		}
		multiAttributeTile(name:"lengthyTileWithIcon", type:"generic", width:6, height:4) {
			tileAttribute("device.lengthyText", key: "PRIMARY_CONTROL") {
				attributeState "lengthyText", label:'The value of this tile is long and should wrap to two lines', backgroundColor:"#79b821", icon: "st.switches.switch.on", defaultState: true
			}
			tileAttribute("device.lengthyText", key: "SECONDARY_CONTROL") {
				attributeState "lengthyText", label:'The value of this tile is long and should wrap to two lines', backgroundColor:"#79b821", icon: "st.switches.switch.on", defaultState: true
			}
		}
		multiAttributeTile(name:"multilineTileWithIcon", type:"generic", width:6, height:4) {
			tileAttribute("device.multilineText", key: "PRIMARY_CONTROL") {
				attributeState "multilineText", label:'Line 1 YES\nLine 2 YES\nLine 3 NO', backgroundColor:"#79b821", icon: "st.switches.switch.on", defaultState: true
			}
			tileAttribute("device.multilineText", key: "SECONDARY_CONTROL") {
				attributeState "multilineText", label:'Line 1 YES\nLine 2 YES\nLine 3 NO', backgroundColor:"#79b821", icon: "st.switches.switch.on", defaultState: true
			}
		}

		main(["basicTile"])
		details(["basicTile", "sliderTile", "valueTile", "lengthyTile", "multilineTile", "lengthyTileWithIcon", "multilineTileWithIcon"])
	}
}

def installed() {
	sendEvent(name: "lengthyText", value: "The value of this tile is long and should wrap to two lines")
	sendEvent(name: "multilineText", value: "Line 1 YES\nLine 2 YES\nLine 3 NO")
}

def parse(String description) {
	// This is a simulated device. No incoming data to parse.
}

def on() {
	log.debug "turningOn"
	sendEvent(name: "switch", value: "on")
}

def off() {
	log.debug "turningOff"
	sendEvent(name: "switch", value: "off")
}

def setLevel(percent) {
	log.debug "setLevel: ${percent}, this"
	sendEvent(name: "level", value: percent)
}

def randomizeLevel() {
	def level = Math.round(Math.random() * 100)
	setLevel(level)
}

def levelUp() {
	def level = device.latestValue("level") as Integer ?: 0
	if (level < 100) {
		level = level + 1
	}
	setLevel(level)
}

def levelDown() {
	def level = device.latestValue("level") as Integer ?: 0
	if (level > 0) {
		level = level - 1
	}
	setLevel(level)
}
