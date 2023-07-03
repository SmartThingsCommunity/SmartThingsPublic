/**
 *  Door Ajar
 *
 *  Copyright 2018 Robin Adams
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
    name: "Door Ajar",
    namespace: "robinaa2",
    author: "Robin Adams",
    description: "Notifies when a door has been open over a specified amount of time.",
    category: "",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
    section("Door sensor:") {
        input "sensor", "capability.contactSensor", required: true, title: "Where?"
    }
    section("Timeout:") {
        input "minutes", "number", required: true, title: "Minutes?"
    }
    section("Send Notifications?") {
        input("recipients", "contact", title: "Send notifications to") {
            input "phone", "phone", title: "Warn with text message (optional)",
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
    subscribe(sensor, "contact.open", doorOpenHandler)
    subscribe(sensor, "contact.closed", doorClosedHandler)
}

def doorOpenHandler(evt) {
    log.debug "doorOpenHandler called: $evt"
    runIn(60 * minutes, check)
}

def doorClosedHandler(evt) {
    log.debug "doorClosedHandler called: $evt"
}

def check() {
	log.debug "In check scheduled method"
    
    def state = sensor.currentState("contact")

    if (state.value == "open") {
        // get the time elapsed between now and when the motion reported inactive
        def elapsed = now() - state.date.time

        // elapsed time is in milliseconds, so the threshold must be converted to milliseconds too
        def threshold = 1000 * 60 * minutes

        if (elapsed >= threshold) {
            log.debug "Door has been ajar long enough ($elapsed ms)"
            
            def message = "The ${sensor.displayName} is open!"
            if (location.contactBookEnabled && recipients) {
                log.debug "Contact Book enabled!"
                sendNotificationToContacts(message, recipients)
            } else {
                log.debug "Contact Book not enabled"
                if (phone) {
                    sendSms(phone, message)
                }
            }
        } else {
            log.debug "Door has been re-opened since timer started ($elapsed ms):  doing nothing"
        }
    } else {
        // Motion active; just log it and do nothing
        log.debug "Door was closed, do nothing"
    }
}