/**
 *  Timer
 *
 *  Copyright 2018 Heath Stewart
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
    name: "Timer",
    namespace: "heaths",
    author: "Heath Stewart",
    description: "Turns a switch off after a configurable period of time when the switch was turned on.",
    category: "Green Living",
    iconUrl: "https://heaths.github.io/img/timer.png",
    iconX2Url: "https://heaths.github.io/img/timer2x.png",
    iconX3Url: "https://heaths.github.io/img/timer2x.png")

preferences {
	section("When this switch is turned on...") {
		input "theSwitch", "capability.switch", title: "Switch", required: true
	}
	section("Turn after after...") {
		input "minutes", "number", title: "Minutes", required: true
	}
}

def installed() {
	log.debug "Installed with settings: ${settings}"

	initialize()
}

def updated() {
	log.debug "Updated with settings: ${settings}"

	unsubscribe()
	unschedule()
	initialize()
}

def initialize() {
	subscribe(theSwitch, "switch.on", onSwitch)
}

def onSwitch(evt) {
	log.debug "${theSwitch} turned on; shutting off after ${minutes} minutes"

	runIn(minutes * 60, turnOff)
}

def turnOff() {
	log.debug "Turning off ${theSwitch}"

	theSwitch.off()
}
