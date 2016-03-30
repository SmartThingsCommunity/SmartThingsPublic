/**
 *  Don't Overcharge Apple Watch
 *
 *  Copyright 2015 Alex Guyot
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
    name: "Don't Overcharge Apple Watch",
    namespace: "guyot",
    author: "Alex Guyot",
    description: "The Apple Watch charges in less than three hours. To prevent overcharging, turn off the power to the charger after three hours of charging.",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
	section("Which charger is for your Apple Watch?") {
		input "watchCharger", "capability.switch"
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
	subscribe(watchCharger, "switch", switchHandler)
}

def switchHandler(evt) {
    log.debug "$evt.value"

    if (evt.value == "on") { // when the switch is turned on
        runIn(60*60*3, timeout) // trigger the timeout in three hours
    }
}

def timeout() {
	log.debug watchCharger.currentValue("switch")
	watchCharger.off() // turn off the Watch charger
    log.debug watchCharger.currentValue("switch")
}
