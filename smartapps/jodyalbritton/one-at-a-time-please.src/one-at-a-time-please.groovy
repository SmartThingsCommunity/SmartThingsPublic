/**
 *  One At A Time Please
 *
 *  Copyright 2015 jody albritton
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
    name: "One At A Time Please",
    namespace: "jodyalbritton",
    author: "Jody Albritton",
    description: "When one switch is turned on, turn the rest in the group off. ",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
	section("Define Switch Group") {
		input "switches", "capability.switch", title: "Select Switches...", required: true, multiple: true
	}
}

def installed() {
	log.debug "Installed with settings: ${settings}"
    switches.each { device ->
    	device.off()
    }

	initialize()
}

def updated() {
	log.debug "Updated with settings: ${settings}"

	unsubscribe()
	initialize()
}

def initialize() {
	subscribeToEvents()
}

def subscribeToEvents() {
	subscribe(switches, "switch.on", eventHandler)
}


def eventHandler(evt) {
	
    // Loop through the switches 
    switches.each { device ->
    	
        def target = device.toString()
        def current = evt.device.toString()
        log.debug "$target:$current"
    	
        // Turn all switches off that are not the one that was pressed
        if (target != current) {
        	log.debug "${target} off because ${current} is on"
        	device.off()
        }
    }
    	
}