/**
 *  meriapp
 *
 *  Copyright 2015 nanoheal
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
    name: "meriapp",
    namespace: "meriapp",
    author: "nanoheal",
    description: "First try",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    oauth: true)


preferences {
		// TODO: put inputs here
        section ("When the door opens/closes...") {
        input "contact1", "capability.contactSensor",
              title: "Where?"
    }
    // What light should this app be configured for?
    section ("Turn on/off a light...") {
        input "switch1", "capability.switch"
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
def contactHandler(evt) {
    log.debug "$evt.value"

    // The contactSensor capability can be either "open" or "closed"
    // If it's "open", turn on the light!
    // If it's "closed" turn the light off.
    if (evt.value == "open") {
        switch1.on();
    } else if (evt.value == "closed") {
        switch1.off();
    }
}
def initialize() {
	// TODO: subscribe to attributes, devices, locations, etc.
    subscribe(contact1, "contact", contactHandler)
}

// TODO: implement event handlers