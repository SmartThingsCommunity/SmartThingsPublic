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
 *  Left It On
 *
 *  Author: SmartThings
 *  Date: 2013-05-09
 */
definition(
    name: "Left It On",
    namespace: "smartthings",
    author: "SmartThings",
    description: "Notifies you when you have left a switch on longer than a specified amount of time.",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/ModeMagic/bon-voyage.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/ModeMagic/bon-voyage%402x.png"
)

preferences {

	section("Monitor this switch") {
		input "contact", "capability.switch", title: "switch", required: true, multiple: false
	}
	section("And notify me if it's on for more than this many minutes (default 10)") {
		input "openThreshold", "number", description: "Number of minutes", required: false
	}
    section("Delay between notifications (default 10 minutes") {
        input "frequency", "number", title: "Number of minutes", description: "", required: false
    }
	section("Via text message at this number (or via push notification if not specified") {
        input("recipients", "contact", title: "Send notifications to") {
            input "phone", "phone", title: "Phone number (optional)", required: false
        }
	}
}

def installed() {
	log.trace "installed()"
	subscribe()
}

def updated() {
	log.trace "updated()"
	unsubscribe()
	subscribe()
}

def subscribe() {
	subscribe(contact, "switch.on", switchOpen)
	subscribe(contact, "switch.off", switchClosed)
}

def doorOpen(evt)
{
	log.trace "switchOpen($evt.name: $evt.value)"
	def t0 = now()
	def delay = (openThreshold != null && openThreshold != "") ? openThreshold * 60 : 600
	runIn(delay, switchOpenTooLong, [overwrite: false])
	log.debug "scheduled switchOpenTooLong in ${now() - t0} msec"
}

def doorClosed(evt)
{
	log.trace "switchClosed($evt.name: $evt.value)"
}

def switchOpenTooLong() {
	def contactState = contact.currentState("contact")
    def freq = (frequency != null && frequency != "") ? frequency * 60 : 600

	if (contactState.value == "on") {
		def elapsed = now() - contactState.rawDateCreated.time
		def threshold = ((openThreshold != null && openThreshold != "") ? openThreshold * 60000 : 60000) - 1000
		if (elapsed >= threshold) {
			log.debug "Contact has stayed on long enough since last check ($elapsed ms):  calling sendMessage()"
			sendMessage()
            runIn(freq, switchOpenTooLong, [overwrite: false])
		} else {
			log.debug "Contact has not stayed on long enough since last check ($elapsed ms):  doing nothing"
		}
	} else {
		log.warn "switchOpenTooLong() called but switch is off:  doing nothing"
	}
}

void sendMessage()
{
	def minutes = (openThreshold != null && openThreshold != "") ? openThreshold : 10
	def msg = "${contact.displayName} has been left on for ${minutes} minutes."
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