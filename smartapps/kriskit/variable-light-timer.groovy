/**
 *  Variable Light Timer
 *
 *  Copyright 2015 Chris Kitch
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
    name: "Variable Light Timer",
    namespace: "kriskit",
    author: "Chris Kitch",
    description: "Different timers for different device activations",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
	section("Door") {
        input "contact", "capability.contactSensor", required: true
        input "contactDelay", type: "number", title: "Light Off Delay (seconds)", defaultValue: 45
	}
    
    section("Motion") {
		input "motion", "capability.motionSensor", required: true
        input "motionDelay", type: "number", title: "Light Off Delay (seconds)", defaultValue: 15 * 60
    }
    
    section("Lights") {
    	input "lights", "capability.switch", multiple: true
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
	subscribe(contact, "contact", onContactChange)
    subscribe(motion, "motion", onMotionChange)
}

def onContactChange(evt) {
	if (evt.value == "open") {
        log.debug "Door contact open"
        lights?.on()
        startTurnOffDelay(contactDelay)
    }
}

def onMotionChange(evt) {
	if (evt.value == "active") {
    	log.debug "Motion sensor active"
        lights?.on()
    	startTurnOffDelay(motionDelay)
	}
}

def startTurnOffDelay(delay) {
	log.debug "Setting lights off delay to $delay seconds"
	runIn(delay.toInteger(), "turnOff")
}

def turnOff() {
	log.debug "Turning off lights"
	lights?.off()
}

// TODO: implement event handlers
