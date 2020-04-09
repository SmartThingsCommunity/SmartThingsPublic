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
 *  Presence Change Text
 *
 *  Author: SmartThings
 */
definition(
    name: "Presence Change Text",
    namespace: "smartthings",
    author: "SmartThings",
    description: "Send me a text message when my presence status changes.",
    category: "Safety & Security",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Meta/text_presence.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Meta/text_presence@2x.png"
)

preferences {
	section("When a presence sensor arrives or departs this location..") {
		input "presence", "capability.presenceSensor", title: "Which sensor?"
	}
	section("Send a text message to...") {
        input("recipients", "contact", title: "Send notifications to") {
            input "phone1", "phone", title: "Phone number?"
        }
	}
}


def installed() {
	subscribe(presence, "presence", presenceHandler)
}

def updated() {
	unsubscribe()
	subscribe(presence, "presence", presenceHandler)
}

def presenceHandler(evt) {
	if (evt.value == "present") {
		// log.debug "${presence.label ?: presence.name} has arrived at the ${location}"

        if (location.contactBookEnabled) {
            sendNotificationToContacts("${presence.label ?: presence.name} has arrived at the ${location}", recipients)
        }
        else {
            sendSms(phone1, "${presence.label ?: presence.name} has arrived at the ${location}")
        }
	} else if (evt.value == "not present") {
		// log.debug "${presence.label ?: presence.name} has left the ${location}"

        if (location.contactBookEnabled) {
            sendNotificationToContacts("${presence.label ?: presence.name} has left the ${location}", recipients)
        }
        else {
            sendSms(phone1, "${presence.label ?: presence.name} has left the ${location}")
        }
	}
}
