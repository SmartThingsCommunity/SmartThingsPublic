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
 *  Check garage/door if I left it open
 *
 */

definition(
    name: "Check garage/door if I left it open.",
    namespace: "jorgeci",
    author: "jorgeci@gmail.com",
    description: "Check if garabe/door is left open when a SmartSense Presence tag or smartphone leaves a location.",
    category: "Safety & Security",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience%402x.png",
)

preferences {
	section("When I leave...") {
		input "presence1", "capability.presenceSensor", title: "Who?", multiple: true
	}
    section("Monitor this door or window") {
		input "contact", "capability.contactSensor"
	}
    section("Via text message at this number (or via push notification if not specified") {
	      input("recipients", "contact", title: "Send notifications to") {
            input "phone", "phone", title: "Phone number (optional)", required: false
        }
	}
}

def installed()
{
	log.trace "installed()"
	subscribe(presence1, "presence", presence)
}

def updated()
{
	unsubscribe()
	subscribe(presence1, "presence", presence)
}

def presence(evt)
{
	if (evt.value == "present") {
		log.debug "somebody arrive ignore it"
	}
	else {
		def contactState = contact.currentState("contact")
		if (contactState.value == "open") {	
        	log.debug "Contact leave and the door is open"
			sendMessage(evt.displayName)
        }
	}
}

void sendMessage(person)
{
	def msg = "${person} left the house and ${contact.displayName} is open."
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