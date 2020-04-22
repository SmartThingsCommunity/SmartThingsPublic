/**
 *  Use Switch to Control Outlets
 *
 *  Copyright 2016 Mark Vickstrom
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
    name: "Use Switch to Control Outlets",
    namespace: "mdvickst",
    author: "Mark Vickstrom",
    description: "Use a Switch to control multiple outlets",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
	section("Use this Switch") {
		input "triggerSwitch", "capability.switch", title: "Which Switch?", required: true
	}
    section("To Control these Devices") {
    	input "switchesToControl", "capability.switch", title: "Which Outlets/Switches?", multiple: true, required: true
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
	subscribe(triggerSwitch, "switch", triggerSwitchPressed)
}

def triggerSwitchPressed(evt){
    if (evt.value == "on") {
        log.debug "swtich pressed On"
        switchesToControl?.on()
    } else if (evt.value == "off") {
        log.debug "swtich pressed Off"
        switchesToControl?.off()
    }
}
