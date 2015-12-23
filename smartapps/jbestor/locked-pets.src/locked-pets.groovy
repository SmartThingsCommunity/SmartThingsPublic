/**
 *  Locked Pets
 *
 *  Copyright 2015 Jason Bestor
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
    name: "Locked Pets",
    namespace: "Jbestor",
    author: "Jason Bestor",
    description: "Use a motion sensor and a door sensor to make sure your pets aren't locked up.",
    category: "Pets",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
	section("Check when motion detected:") {
        input "themotion", "capability.motionSensor", required: true, title: "Where?"
    }
    section("Notify me if the door is closed") {
        input "contact", "capability.contactSensor", required: true
    }
    section("Send notification and/or text message") {
        input("recipients", "contact", title: "Send notifications to") {
            input "sendPush", "enum", title: "Push Notification", required: false, options: ["Yes", "No"]
            input "phone1", "phone", title: "Phone Number", required: false
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
	subscribe(themotion, "motion.active", motionDetectedHandler)
}

def motionDetectedHandler(evt) {
    log.debug "motionDetectedHandler called: $evt"
    checkDoor()
}

def checkDoor()
{
	def contactState = contact.currentState("contact")
    if (contactState.value == "closed") {	
		log.debug "Contact is closed: calling sendMessage()"
		sendMessage()
	} else {
		log.warn "Contact is open:  doing nothing"
	}
}

void sendMessage()
{
	def msg = "${themotion.displayName} has detected motion and ${contact.displayName} is closed."
	log.info msg
    if (location.contactBookEnabled) {
        sendNotificationToContacts(msg, recipients)
    }
    else {
        if (phone) {
            sendSms phone, msg
        } else {
            sendPush msg
        }
    }
}