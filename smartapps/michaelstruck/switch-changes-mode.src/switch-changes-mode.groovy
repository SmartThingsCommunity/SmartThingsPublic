/**
 *  Switch Changes Mode
 *
 *  Copyright 2015 Michael Struck
 *  Version 1.01 3/8/15
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
 *  Ties a mode to a switch's (virtual or real) on/off state. Perfect for use with IFTTT.
 *  Simple define a switch to be used, then tie the on/off state of the switch to a specific mode.
 *  Connect the switch to an IFTTT action, and the mode will fire with the switch state change.
 *
 *
 */
definition(
    name: "Switch Changes Mode",
    namespace: "MichaelStruck",
    author: "Michael Struck",
    description: "Ties a mode to a switch's state. Perfect for use with IFTTT.",
    category: "Convenience",
    iconUrl: "https://raw.githubusercontent.com/MichaelStruck/SmartThings/master/IFTTT-SmartApps/App1.png",
    iconX2Url: "https://raw.githubusercontent.com/MichaelStruck/SmartThings/master/IFTTT-SmartApps/App1@2x.png",
    iconX3Url: "https://raw.githubusercontent.com/MichaelStruck/SmartThings/master/IFTTT-SmartApps/App1@2x.png")

preferences {
	page(name: "getPref", title: "Choose Switch and Modes", install:true, uninstall: true) {
    	section("Choose a switch to use...") {
			input "controlSwitch", "capability.switch", title: "Switch", multiple: false, required: true
   		}
		section("Change to a new mode when...") {
			input "onMode", "mode", title: "Switch is on", required: false
			input "offMode", "mode", title: "Switch is off", required: false 
		}
	}
}

def installed() {
	log.debug "Installed with settings: ${settings}"

	subscribe(controlSwitch, "switch", "switchHandler")
}

def updated() {
	log.debug "Updated with settings: ${settings}"

	unsubscribe()
	subscribe(controlSwitch, "switch", "switchHandler")
}

def switchHandler(evt) {
	if (evt.value == "on") {
    	changeMode(onMode)
    } else {
    	changeMode(offMode)
    }
}

def changeMode(newMode) {

	if (newMode && location.mode != newMode) {
		if (location.modes?.find{it.name == newMode}) {
			setLocationMode(newMode)
		}
		else {
		log.debug "Unable to change to undefined mode '${newMode}'"
		}
	}
}