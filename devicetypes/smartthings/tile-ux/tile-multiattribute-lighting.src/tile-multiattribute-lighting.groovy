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
	definition (
		name: "lightingDeviceTile",
		namespace: "smartthings/tile-ux",
		author: "SmartThings") {

		capability "Switch Level"
		capability "Actuator"
		capability "Color Control"
		capability "Power Meter"
		capability "Switch"
		capability "Refresh"
		capability "Sensor"

		command "setAdjustedColor"
		command "reset"
		command "refresh"
	}

	tiles(scale: 2) {
		multiAttributeTile(name:"switch", type: "lighting", width: 6, height: 4, canChangeIcon: true) {
			tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
				attributeState "on", label:'${name}', action:"switch.off", icon:"st.lights.philips.hue-single", backgroundColor:"#00A0DC", nextState:"turningOff"
				attributeState "off", label:'${name}', action:"switch.on", icon:"st.lights.philips.hue-single", backgroundColor:"#ffffff", nextState:"turningOn"
				attributeState "turningOn", label:'${name}', action:"switch.off", icon:"st.lights.philips.hue-single", backgroundColor:"#00A0DC", nextState:"turningOff"
				attributeState "turningOff", label:'${name}', action:"switch.on", icon:"st.lights.philips.hue-single", backgroundColor:"#ffffff", nextState:"turningOn"
			}
			tileAttribute ("device.power", key: "SECONDARY_CONTROL") {
				attributeState "power", label:'Power level: ${currentValue}W', icon: "st.Appliances.appliances17"
			}
			tileAttribute ("device.level", key: "SLIDER_CONTROL") {
				attributeState "level", action:"switch level.setLevel"
			}
			tileAttribute ("device.color", key: "COLOR_CONTROL") {
				attributeState "color", action:"setAdjustedColor"
			}
		}

		multiAttributeTile(name:"switchNoPower", type: "lighting", width: 6, height: 4, canChangeIcon: true) {
			tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
				attributeState "on", label:'${name}', action:"switch.off", icon:"st.lights.philips.hue-single", backgroundColor:"#00A0DC", nextState:"turningOff"
				attributeState "off", label:'${name}', action:"switch.on", icon:"st.lights.philips.hue-single", backgroundColor:"#ffffff", nextState:"turningOn"
				attributeState "turningOn", label:'${name}', action:"switch.off", icon:"st.lights.philips.hue-single", backgroundColor:"#00A0DC", nextState:"turningOff"
				attributeState "turningOff", label:'${name}', action:"switch.on", icon:"st.lights.philips.hue-single", backgroundColor:"#ffffff", nextState:"turningOn"
			}
			tileAttribute ("device.level", key: "SLIDER_CONTROL") {
				attributeState "level", action:"switch level.setLevel"
			}
			tileAttribute ("device.color", key: "COLOR_CONTROL") {
				attributeState "color", action:"setAdjustedColor"
			}
		}

		multiAttributeTile(name:"switchNoSlider", type: "lighting", width: 6, height: 4, canChangeIcon: true) {
			tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
				attributeState "on", label:'${name}', action:"switch.off", icon:"st.lights.philips.hue-single", backgroundColor:"#00A0DC", nextState:"turningOff"
				attributeState "off", label:'${name}', action:"switch.on", icon:"st.lights.philips.hue-single", backgroundColor:"#ffffff", nextState:"turningOn"
				attributeState "turningOn", label:'${name}', action:"switch.off", icon:"st.lights.philips.hue-single", backgroundColor:"#00A0DC", nextState:"turningOff"
				attributeState "turningOff", label:'${name}', action:"switch.on", icon:"st.lights.philips.hue-single", backgroundColor:"#ffffff", nextState:"turningOn"
			}
			tileAttribute ("device.power", key: "SECONDARY_CONTROL") {
				attributeState "power", label:'The power level is currently: ${currentValue}W', icon: "st.Appliances.appliances17"
			}
			tileAttribute ("device.color", key: "COLOR_CONTROL") {
				attributeState "color", action:"setAdjustedColor"
			}
		}

		multiAttributeTile(name:"switchNoSliderOrColor", type: "lighting", width: 6, height: 4, canChangeIcon: true) {
			tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
				attributeState "on", label:'${name}', action:"switch.off", icon:"st.lights.philips.hue-single", backgroundColor:"#00A0DC", nextState:"turningOff"
				attributeState "off", label:'${name}', action:"switch.on", icon:"st.lights.philips.hue-single", backgroundColor:"#ffffff", nextState:"turningOn"
				attributeState "turningOn", label:'${name}', action:"switch.off", icon:"st.lights.philips.hue-single", backgroundColor:"#00A0DC", nextState:"turningOff"
				attributeState "turningOff", label:'${name}', action:"switch.on", icon:"st.lights.philips.hue-single", backgroundColor:"#ffffff", nextState:"turningOn"
			}
			tileAttribute ("device.power", key: "SECONDARY_CONTROL") {
				attributeState "power", label:'The light is currently consuming this amount of power: ${currentValue}W', icon: "st.Appliances.appliances17"
			}
		}

		valueTile("color", "device.color", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "color", label: '${currentValue}'
		}

		standardTile("reset", "device.reset", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "reset", label:"Reset Color", action:"reset", icon:"st.lights.philips.hue-single", defaultState: true
		}
		standardTile("refresh", "device.switch", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "refresh", label:"", action:"refresh.refresh", icon:"st.secondary.refresh", defaultState: true
		}

		main(["switch"])
		details(["switch", "switchNoPower", "switchNoSlider", "switchNoSliderOrColor", "color", "refresh", "reset"])
	}
}

// parse events into attributes
def parse(description) {
	log.debug "parse() - $description"
	def results = []
	def map = description
	if (description instanceof String)  {
		log.debug "Hue Bulb stringToMap - ${map}"
		map = stringToMap(description)
	}
	if (map?.name && map?.value) {
		results << createEvent(name: "${map?.name}", value: "${map?.value}")
	}
	results
}

// handle commands
def on() {
	//log.trace parent.on(this)
	sendEvent(name: "switch", value: "on")
}

def off() {
	//log.trace parent.off(this)
	sendEvent(name: "switch", value: "off")
}

def nextLevel() {
	def level = device.latestValue("level") as Integer ?: 0
	if (level <= 100) {
		level = Math.min(25 * (Math.round(level / 25) + 1), 100) as Integer
	}
	else {
		level = 25
	}
	setLevel(level)
}

def setLevel(percent) {
	log.debug "setLevel: ${percent}, this"
	sendEvent(name: "level", value: percent)
	def power = Math.round(percent / 1.175) * 0.1
	sendEvent(name: "power", value: power)
}

def setSaturation(percent) {
	log.debug "setSaturation: ${percent}, $this"
	sendEvent(name: "saturation", value: percent)
}

def setHue(percent) {
	log.debug "setHue: ${percent}, $this"
	sendEvent(name: "hue", value: percent)
}

def setColor(value) {
	log.debug "setColor: ${value}, $this"
	if (value.hue) { sendEvent(name: "hue", value: value.hue)}
	if (value.saturation) { sendEvent(name: "saturation", value: value.saturation)}
	if (value.hex) { sendEvent(name: "color", value: value.hex)}
	if (value.level) { sendEvent(name: "level", value: value.level)}
	if (value.switch) { sendEvent(name: "switch", value: value.switch)}
}

def reset() {
	log.debug "Executing 'reset'"
	setAdjustedColor([level:100, hex:"#90C638", saturation:56, hue:23])
}

def setAdjustedColor(value) {
	if (value) {
		log.trace "setAdjustedColor: ${value}"
		def adjusted = value + [:]
		adjusted.hue = adjustOutgoingHue(value.hue)
		// Needed because color picker always sends 100
		adjusted.level = null
		setColor(adjusted)
	}
}

def refresh() {
	log.debug "Executing 'refresh'"
}

def adjustOutgoingHue(percent) {
	def adjusted = percent
	if (percent > 31) {
		if (percent < 63.0) {
			adjusted = percent + (7 * (percent -30 ) / 32)
		}
		else if (percent < 73.0) {
			adjusted = 69 + (5 * (percent - 62) / 10)
		}
		else {
			adjusted = percent + (2 * (100 - percent) / 28)
		}
	}
	log.info "percent: $percent, adjusted: $adjusted"
	adjusted
}
