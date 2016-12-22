/**
 *  Did I Leave It Open?
 *
 *  Copyright 2016 Gary Johnson
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
    name: "Did I Leave It Open?",
    namespace: "garyjohnson",
    author: "Gary Johnson",
    description: "Sends a notification if something is still open when a mode changes.",
    category: "Mode Magic",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
	section() {
        input "modes", "mode", title: "When I enter these modes", multiple: true
    }
        
   	section("And I left this open") {
        input "contactSensor", "capability.contactSensor"
	}
    
    section("Notify these people") {
        input("recipients", "contact", title: "Send notifications to") {
          input "phone", "phone", title: "Send notifications to",
                description: "Phone Number", required: false
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
    log.debug "init with mode ${location.mode}"
    subscribe(location, "mode", modeChangeHandler)
}

def modeChangeHandler(evt) {
	log.debug "contactState: $contactSensor.contactState.stringValue"
    log.debug "modes: $modes"
    log.debug "value: $evt.value"
	if(modes.contains(evt.value) && contactSensor.contactState.stringValue == "open") {
    	log.debug "You left it open!"
        def message = "You left the ${contactSensor.displayName} open!"
        if (location.contactBookEnabled && recipients) {
        	log.debug "contact book enabled!"
        	sendNotificationToContacts(message, recipients)
    	} else {
        	log.debug "contact book not enabled"
        	if (phone) {
            	sendSms(phone, message)
        	}
    	}
    }
}

// TODO: implement event handlers