/**
 *  Set Color Temperature From Virtual Dimmer
 *
 *  Copyright 2016 Ilan Goodman
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
definition(
    name: "Set Color Temperature From Virtual Dimmer",
    namespace: "ilangoodman",
    author: "Ilan Goodman",
    description: "Listen for events from a virtual dimmer and use it to set color temperature on tunable white bulbs.",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
	section("When this changes...") { 
		input "dimmer", "capability.switchLevel", 
			multiple: false, 
			title: "Virtual dimmer switch", 
			required: true
	}
    
    section("Set the color temperature on these...") { 
		input "lights", "capability.colorTemperature", 
			multiple: true, 
			title: "Tunable white lights", 
			required: true
	}
}

def installed() {
	log.debug "Installed with settings: ${settings}"

	initialize()
}

def updated() {
	log.debug "Updated with settings: ${settings}"

	unsubscribe()
	initialize()
}

def initialize() {
	subscribe(dimmer, "level", setLevelHandler)
}

def setLevelHandler(event) {
	def level = event.value.toFloat()
	def kelvin = Math.round((level * 38) + 2700)
    
    log.debug "Dimmer level: $level"
    log.debug "Setting color temperature to $kelvin"
    lights?.setColorTemperature(kelvin)
}