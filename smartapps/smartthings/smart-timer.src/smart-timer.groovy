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
 *  CoolDeep
 *
 *  Author: CoolDeep
 */

definition(
    name: "Smart Timer",
    namespace: "smartthings",
    author: "CoolDeep",
    description: "If a light/device is already on, leave it on. Else, if a light/device switches on on a trigger, switch off in a given time",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience%402x.png"
)

preferences {
	section("When a sesonsor triggers(turns on)...") {
		input "mySwitch", "capability.switch", title: "Switches", multiple: true, required: true
        input "mySensor", "capability.contactSensor", title: "Sensors", multiple: true, required: true
	}
    section("Turn it off after how many minutes?") {
		input "minutesLater", "decimal", title: "When?"
	}
}

def installed() {
	log.debug "Installed with settings: ${settings}"
	subscribe(mySensor, "contact.opened", myHandler)
}

def updated() {
	log.debug "Updated with settings: ${settings}"
	unsubscribe()
	subscribe(mySensor, "contact.open", myHandler)
}
    
def myHandler(evt) {
	def lightVal = mySwitch.currentSwitch
    log.debug ">> TheSwitch current value turned ${lightVal}"
   
  	if (lightVal.contains("off")) {
    	def delay = minutesLater * 60
    	log.debug ">>> Turning off in ${minutesLater} minutes (${delay}seconds)"
    	runIn(delay, turnOffSwitch)
    }
}

def turnOffSwitch() {
	mySwitch.off()
}