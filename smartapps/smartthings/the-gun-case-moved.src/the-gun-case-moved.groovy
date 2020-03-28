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
 *  The Gun Case Moved
 *
 *  Author: SmartThings
 */
definition(
    name: "The Gun Case Moved",
    namespace: "smartthings",
    author: "SmartThings",
    description: "Send a text when your gun case moves",
    category: "Safety & Security",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Meta/text_accelerometer.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Meta/text_accelerometer@2x.png"
)

preferences {
	section("When the gun case moves..."){
		input "accelerationSensor", "capability.accelerationSensor", title: "Where?"
	}
	section("Text me at..."){
        input("recipients", "contact", title: "Send notifications to") {
            input "phone1", "phone", title: "Phone number?"
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
	// Don't send a continuous stream of text messages
	def deltaSeconds = 5
	def timeAgo = new Date(now() - (1000 * deltaSeconds))
	def recentEvents = accelerationSensor.eventsSince(timeAgo)
	log.trace "Found ${recentEvents?.size() ?: 0} events in the last $deltaSeconds seconds"
	def alreadySentSms = recentEvents.count { it.value && it.value == "active" } > 1

	if (alreadySentSms) {
		log.debug "SMS already sent to phone within the last $deltaSeconds seconds"
	} else {
        if (location.contactBookEnabled) {
            sendNotificationToContacts("Gun case has moved!", recipients)
        }
        else {
            log.debug "$accelerationSensor has moved, texting phone"
            sendSms(phone1, "Gun case has moved!")
        }
	}
}
