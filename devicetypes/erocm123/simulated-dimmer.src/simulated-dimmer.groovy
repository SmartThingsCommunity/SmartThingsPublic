/**
 *  Simulated Dimmer
 *
 *  Copyright 2016 Eric Maycock (erocm123)
 * 
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
	
    definition (name: "Simulated Dimmer", namespace: "erocm123", author: "Eric Maycock") {
		capability "Switch"
        capability "Relay Switch"
        capability "Switch Level"

		command "onPhysical"
		command "offPhysical"
	}

	tiles (scale:2) {
		standardTile("switch", "device.switch", width: 3, height: 3, canChangeIcon: true) {
			state "off", label: '${currentValue}', action: "switch.on", icon: "st.switches.switch.off", backgroundColor: "#ffffff"
			state "on", label: '${currentValue}', action: "switch.off", icon: "st.switches.switch.on", backgroundColor: "#79b821"
		}
		standardTile("on", "device.switch", decoration: "flat") {
			state "default", label: 'On', action: "onPhysical", backgroundColor: "#ffffff"
		}
		standardTile("off", "device.switch", decoration: "flat") {
			state "default", label: 'Off', action: "offPhysical", backgroundColor: "#ffffff"
		}
        valueTile("level", "device.level", inactiveLabel: false, decoration: "flat", width: 3, height: 3) {
			state "level", label:'${currentValue} %', unit:"%", backgroundColor:"#ffffff"
		}
        controlTile("levelSliderControl", "device.level", "slider", height: 2, width: 6, inactiveLabel: false) {
			state "level", action:"switch level.setLevel"
		}
        main "switch"
		details(["switch", "level", "levelSliderControl"])
	}
}

def parse(String description) {

}

def parse(Map description) {
    def eventMap
    if (description.type == null) eventMap = [name:"$description.name", value:"$description.value"]
    else eventMap = [name:"$description.name", value:"$description.value", type:"$description.type"]
    createEvent(eventMap)
}

def on() {
	log.debug "$version on()"
	sendEvent(name: "switch", value: "on")
}

def off() {
	log.debug "$version off()"
	sendEvent(name: "switch", value: "off")
}

def onPhysical() {
	log.debug "$version onPhysical()"
	sendEvent(name: "switch", value: "on", type: "physical")
}

def offPhysical() {
	log.debug "$version offPhysical()"
	sendEvent(name: "switch", value: "off", type: "physical")
}

def setLevel(value) {
	log.debug "setLevel >> value: $value"
	def level = Math.max(Math.min(value as Integer, 99), 0)
	if (level > 0) {
		sendEvent(name: "switch", value: "on")
	} else {
		sendEvent(name: "switch", value: "off")
	}
	sendEvent(name: "level", value: level, unit: "%")
}

def setLevel(value, duration) {
	log.debug "setLevel >> value: $value, duration: $duration"
	def level = Math.max(Math.min(value as Integer, 99), 0)
	setLevel(level)
}

private getVersion() {
	"PUBLISHED"
}