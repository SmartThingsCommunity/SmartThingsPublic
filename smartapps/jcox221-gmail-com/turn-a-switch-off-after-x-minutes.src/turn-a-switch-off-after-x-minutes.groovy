/**
 *  Turn a switch off after X minutes
 *
 *  Copyright 2020 John Cox
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
    name: "Turn a switch off after X minutes",
    namespace: "jcox221@gmail.com",
    author: "John Cox",
    description: "Turn a switch off after a user-specified amount of minutes.",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")

preferences {
	section("When it turns on..."){
		input "switch1", "capability.switch", required: true, title: "Switch?"
	}
    section("Turn off after..."){
    input "minutes", "number", required: true, title: "Minutes?"
    }
}

def installed() {
	log.debug "Installed with settings: ${settings}"
	subscribe(switch1, "switch.on", switchOnHandler)
}

def updated(settings) {
	unsubscribe()
	subscribe(switch1, "switch.on", switchOnHandler)
}

def switchOnHandler(evt) {
	def minuteDelay = 60 * minutes
	runIn(minuteDelay, turnOffSwitch)
}

def turnOffSwitch() {
	switch1.off()
}