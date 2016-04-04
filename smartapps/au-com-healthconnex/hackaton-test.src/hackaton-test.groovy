/**
 *  Hackaton Test
 *
 *  Copyright 2016 Fredy Rincon
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
    name: "Hackaton Test",
    namespace: "au.com.healthconnex",
    author: "Fredy Rincon",
    description: "Test for Hackaton",
    category: "Health & Wellness",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    oauth: true)


preferences {
    section {
        input "switches", "capability.switch", multiple: true
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
    subscribe(switches, "switch", someEventHandler)
}

def someEventHandler(evt) {
    // returns a list of the values for all switches
    def currSwitches = switches.currentSwitch

    def onSwitches = currSwitches.findAll { switchVal ->
        switchVal == "on" ? true : false
    }

    log.error "${onSwitches.size()} out of ${switches.size()} switches are on"
	log.warn "${onSwitches.size()} out of ${switches.size()} switches are on"
	log.info "${onSwitches.size()} out of ${switches.size()} switches are on"
	log.debug "${onSwitches.size()} out of ${switches.size()} switches are on"
	log.trace "${onSwitches.size()} out of ${switches.size()} switches are on"
    
    try {
    def x = "some string"
    	x.somethingThatDoesNotExist
	} catch (all) {
    	log.error "Something went horribly wrong!\n${all}"
	}
}