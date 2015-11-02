/**
 *  Arrival lights
 *
 *  Copyright 2015 Jon Paterson
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
    name: "Arrival lights",
    namespace: "jonpaterson",
    author: "Jon Paterson",
    description: "Control outdoor lights for arrival",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
	section("When one of these persons arrives") {
		input "people", "capability.presenceSensor", multiple: true
	}
    section("And it's dark...") {
		input "luminance", "capability.illuminanceMeasurement", title: "Where?"
	}
    section("Turn on these lights...") {
		input "switch1", "capability.switch", multiple: true, title: "Where?"
	}
    section("Delay before turning off") {                    
		input "delayMins", "number", title: "How long after arrival to leave lights on"
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
	subscribe(people, "presence", presenseHandler)
}

def presenseHandler(evt) {
	if (evt.value == "present") {
        def lightSensorState = luminance.currentIlluminance
        log.debug "SENSOR = $lightSensorState"
        if (lightSensorState && lightSensorState < 20) {
            log.trace "light.on() ... [luminance: ${lightSensorState}]"
            switch1.on()
        }
    }
}
