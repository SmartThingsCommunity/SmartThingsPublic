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
 *  Greetings Earthling
 *
 *  Author: SmartThings
 *  Date: 2013-03-07
 */
definition(
    name: "Greetings Earthling",
    namespace: "smartthings",
    author: "SmartThings",
    description: "Monitors a set of presence detectors and triggers a mode change when someone arrives at home.",
    category: "Mode Magic",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/App-LightUpMyWorld.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/App-LightUpMyWorld@2x.png"
)

preferences {

	section("When one of these people arrive at home") {
		input "people", "capability.presenceSensor", multiple: true
	}
	section("Change to this mode") {
		input "newMode", "mode", title: "Mode?"
	}
	section("False alarm threshold (defaults to 10 min)") {
		input "falseAlarmThreshold", "decimal", title: "Number of minutes", required: false
	}
	section( "Notifications" ) {
        input("recipients", "contact", title: "Send notifications to") {
            input "sendPushMessage", "enum", title: "Send a push notification?", options: ["Yes", "No"], required: false
            input "phone", "phone", title: "Send a Text Message?", required: false
        }
	}

}

def installed() {
	log.debug "Installed with settings: ${settings}"
	// log.debug "Current mode = ${location.mode}, people = ${people.collect{it.label + ': ' + it.currentPresence}}"
	subscribe(people, "presence", presence)
}

def updated() {
	log.debug "Updated with settings: ${settings}"
	// log.debug "Current mode = ${location.mode}, people = ${people.collect{it.label + ': ' + it.currentPresence}}"
	unsubscribe()
	subscribe(people, "presence", presence)
}

def presence(evt)
{
	log.debug "evt.name: $evt.value"
	def threshold = (falseAlarmThreshold != null && falseAlarmThreshold != "") ? (falseAlarmThreshold * 60 * 1000) as Long : 10 * 60 * 1000L

	if (location.mode != newMode) {

		def t0 = new Date(now() - threshold)
		if (evt.value == "present") {

			def person = getPerson(evt)
			def recentNotPresent = person.statesSince("presence", t0).find{it.value == "not present"}
			if (recentNotPresent) {
				log.debug "skipping notification of arrival of Person because last departure was only ${now() - recentNotPresent.date.time} msec ago"
			}
			else {
				def message = "${person.displayName} arrived at home, changing mode to '${newMode}'"
				send(message)
				setLocationMode(newMode)
			}
		}
	}
	else {
		log.debug "mode is the same, not evaluating"
	}
}

private getPerson(evt)
{
	people.find{evt.deviceId == it.id}
}

private send(msg) {
    if (location.contactBookEnabled) {
        sendNotificationToContacts(msg, recipients)
    }
    else {
        if (sendPushMessage != "No") {
            log.debug("sending push message")
            sendPush(msg)
        }

        if (phone) {
            log.debug("sending text message")
            sendSms(phone, msg)
        }
    }
}
