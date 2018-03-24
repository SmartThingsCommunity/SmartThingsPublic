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
 *  Cycle Switch every "x" minutes
 *  Choose how long the switch stays on for and how long it stays off for.
 *
 *  Author: Lenny Cunningham
 */
definition(
    name: "Cycle Switch every 'x' minutes.",
    namespace: "lennyz71",
    author: "Lenny Cunningham",
    description: "Cycle a switch on and off every 'x' minutes. Choose how long the switch stays on for and how long it stays off for.",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Meta/light_contact-outlet.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Meta/light_contact-outlet@2x.png"
)

preferences {
	section("Switch to cycle every x minutes..."){
		input "switch1", "capability.switch"
	}
    section("Automatically turn switch on for this long...") {
        input "minutesOn", "number", title: "On time (in minutes):", required: true
    }
    section("Automatically turn switch off for this long...") {
        input "minutesOff", "number", title: "Off time (in minutes):", required: true
    }
}

def installed() {
	log.debug "Installed with settings: ${settings}"
    initialize()
}

def updated() {
	log.debug "Updated with settings: ${settings}"
    initialize()
}

def initialize()
{
    turnOnSwitch()
}

def turnOnSwitch() {
	switch1.on()
	def delay = (minutesOn * 60)
	runIn(delay, turnOffSwitch)
}

def turnOffSwitch() {
	switch1.off()
    def delay = (minutesOff * 60) 
    runIn(delay, turnOnSwitch)
}