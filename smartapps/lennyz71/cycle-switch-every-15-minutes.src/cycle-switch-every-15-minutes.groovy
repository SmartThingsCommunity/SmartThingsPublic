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
 *  Cycle Switch every 15 minutes
 *
 *  Author: Lenny Cunningham
 */
definition(
    name: "Cycle Switch every 15 minutes.",
    namespace: "lennyz71",
    author: "Lenny Cunningham",
    description: "Cycle a switch on and off every 15 minutes.",
    category: "",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Meta/light_contact-outlet.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Meta/light_contact-outlet@2x.png"
)

preferences {
	section("Switch to cycle every 15 minutes..."){
		input "switch1", "capability.switch"
	}
}

def installed() {
	log.debug "Installed with settings: ${settings}"
    turnOnSwitch()
}

def updated() {
	log.debug "Updated with settings: ${settings}"
    turnOnSwitch()
}

def turnOnSwitch() {
	switch1.on()
	def fifteenMinuteDelay = 60 * 15
	runIn(fifteenMinuteDelay, turnOffSwitch)
}

def turnOffSwitch() {
	switch1.off()
    def fifteenMinuteDelay = 60 * 15
    runIn(fifteenMinuteDelay, turnOnSwitch)
}