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
 *  Mail Arrived
 *
 *  Author: SmartThings
 */
definition(
    name: "Mail Arrived",
    namespace: "smartthings",
    author: "SmartThings",
    description: "Send a text when mail arrives in your mailbox using a SmartSense Multi on your mailbox door. Note: battery life may be impacted in cold climates.",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Meta/mail_contact.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Meta/mail_contact@2x.png"
)

preferences {
	section("When mail arrives...") {
		input "accelerationSensor", "capability.accelerationSensor", title: "Where?"
	}
	section("Notify me...") {
        input("recipients", "contact", title: "Send notifications to") {
            input "pushNotification", "bool", title: "Push notification", required: false, defaultValue: "true"
            input "phone1", "phone", title: "Phone number", required: false
        }
	}
}

def installed() {
	subscribe(accelerationSensor, "acceleration.active", accelerationActiveHandler)
}

def updated() {
	unsubscribe()
	subscribe(accelerationSensor, "acceleration.active", accelerationActiveHandler)
}

def accelerationActiveHandler(evt) {
	log.trace "$evt.value: $evt, $settings"

	// Don't send a continuous stream of notifications
	def deltaSeconds = 5
	def timeAgo = new Date(now() - (1000 * deltaSeconds))
	def recentEvents = accelerationSensor.eventsSince(timeAgo)
	log.trace "Found ${recentEvents?.size() ?: 0} events in the last $deltaSeconds seconds"
	def alreadySentNotifications = recentEvents.count { it.value && it.value == "active" } > 1

	if (alreadySentNotifications) {
		log.debug "Notifications already sent within the last $deltaSeconds seconds (phone1: $phone1, pushNotification: $pushNotification)"
	}
	else {
        if (location.contactBookEnabled) {
            log.debug "$accelerationSensor has moved, notifying ${recipients?.size()}"
            sendNotificationToContacts("Mail has arrived!", recipients)
        }
        else {
        if (phone1 != null && phone1 != "") {
            log.debug "$accelerationSensor has moved, texting $phone1"
            sendSms(phone1, "Mail has arrived!")
        }
        if (pushNotification) {
            log.debug "$accelerationSensor has moved, sending push"
            sendPush("Mail has arrived!")
        }
    }
	}
}
