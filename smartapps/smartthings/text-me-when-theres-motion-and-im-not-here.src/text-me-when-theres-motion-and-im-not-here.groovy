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
 *  Text Me When There's Motion and I'm Not Here
 *
 *  Author: SmartThings
 */

definition(
    name: "Text Me When There's Motion and I'm Not Here",
    namespace: "smartthings",
    author: "SmartThings",
    description: "Send a text message when there is motion while you are away.",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Meta/intruder_motion-presence.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Meta/intruder_motion-presence@2x.png"
)

preferences {
	section("When there's movement...") {
		input "motion1", "capability.motionSensor", title: "Where?"
	}
	section("While I'm out...") {
		input "presence1", "capability.presenceSensor", title: "Who?"
	}
	section("Text me at...") {
        input("recipients", "contact", title: "Send notifications to") {
            input "phone1", "phone", title: "Phone number?"
        }
	}
}

def installed() {
	subscribe(motion1, "motion.active", motionActiveHandler)
}

def updated() {
	unsubscribe()
	subscribe(motion1, "motion.active", motionActiveHandler)
}

def motionActiveHandler(evt) {
	log.trace "$evt.value: $evt, $settings"

	if (presence1.latestValue("presence") == "not present") {
		// Don't send a continuous stream of text messages
		def deltaSeconds = 10
		def timeAgo = new Date(now() - (1000 * deltaSeconds))
		def recentEvents = motion1.eventsSince(timeAgo)
		log.debug "Found ${recentEvents?.size() ?: 0} events in the last $deltaSeconds seconds"
		def alreadySentSms = recentEvents.count { it.value && it.value == "active" } > 1

		if (alreadySentSms) {
			log.debug "SMS already sent within the last $deltaSeconds seconds"
		} else {
            if (location.contactBookEnabled) {
                log.debug "$motion1 has moved while you were out, sending notifications to: ${recipients?.size()}"
                sendNotificationToContacts("${motion1.label} ${motion1.name} moved while you were out", recipients)
            }
            else {
                log.debug "$motion1 has moved while you were out, sending text"
                sendSms(phone1, "${motion1.label} ${motion1.name} moved while you were out")
            }
		}
	} else {
		log.debug "Motion detected, but presence sensor indicates you are present"
	}
}
