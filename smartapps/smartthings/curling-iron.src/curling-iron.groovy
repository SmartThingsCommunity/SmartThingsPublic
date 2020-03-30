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
 *  Curling Iron
 *
 *  Author: SmartThings
 *  Date: 2013-03-20
 */
definition(
    name: "Curling Iron",
    namespace: "smartthings",
    author: "SmartThings",
    description: "Turns on an outlet when the user is present and off after a period of time",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Meta/temp_thermo-switch.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Meta/temp_thermo-switch@2x.png"
)

preferences {
	section("When someone's around because of...") {
		input name: "motionSensors", title: "Motion here", type: "capability.motionSensor", multiple: true, required: false
		input name: "presenceSensors", title: "And (optionally) these sensors being present", type: "capability.presenceSensor", multiple: true, required: false
	}
	section("Turn on these outlet(s)") {
		input name: "outlets", title: "Which?", type: "capability.switch", multiple: true
	}
	section("For this amount of time") {
		input name: "minutes", title: "Minutes?", type: "number", multiple: false
	}
}

def installed() {
	subscribeToEvents()
}

def updated() {
	unsubscribe()
	subscribeToEvents()
}

def subscribeToEvents() {
	subscribe(motionSensors, "motion.active", motionActive)
	subscribe(motionSensors, "motion.inactive", motionInactive)
	subscribe(presenceSensors, "presence.not present", notPresent)
}

def motionActive(evt) {
	log.debug "$evt.name: $evt.value"
	if (anyHere()) {
		outletsOn()
	}
}

def motionInactive(evt) {
	log.debug "$evt.name: $evt.value"
	if (allQuiet()) {
		outletsOff()
	}
}

def notPresent(evt) {
	log.debug "$evt.name: $evt.value"
	if (!anyHere()) {
		outletsOff()
	}
}

def allQuiet() {
	def result = true
	for (it in motionSensors) {
		if (it.currentMotion == "active") {
			result = false
			break
		}
	}
	return result
}

def anyHere() {
	def result = true
	for (it in presenceSensors) {
		if (it.currentPresence == "not present") {
			result = false
			break
		}
	}
	return result
}

def outletsOn() {
	outlets.on()
	unschedule("scheduledTurnOff")
}

def outletsOff() {
	def delay = minutes * 60
	runIn(delay, "scheduledTurnOff")
}

def scheduledTurnOff() {
	outlets.off()
	unschedule("scheduledTurnOff") // Temporary work-around to scheduling bug
}


