/**
 *  Multiple Preferences Pages
 *
 *  Copyright 2015 james anderson
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
    name: "Multiple Preferences Pages",
    namespace: "jimmyjames",
    author: "james anderson",
    description: "Multiple Preferences Pages",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
    page(name: "pageOne", title: "When there's activity on any of these sensors", nextPage: "pageTwo", uninstall: true) {
        section("Choose sensors to trigger the action") {

            input "contactSensors", "capability.contactSensor",
                title: "Open/close sensors", multiple: true, required: false

            input "motionSensors", "capability.motionSensor",
                title: "Motion sensors?", multiple: true, required: false
        }
    }
    page(name: "pageTwo", title: "Turn on these lights", nextPage: "pageThree") {
        section {
            input "switches", "capability.switch", multiple: true, required: false
        }
    }
    page(name: "pageThree", title: "Name app and configure modes", install: true, uninstall: true) {
        section([mobileOnly:true]) {
            label title: "Assign a name", required: false
            mode title: "Set for specific mode(s)", required: false
        }
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
	// TODO: subscribe to attributes, devices, locations, etc.
}

// TODO: implement event handlers