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
 *  Bon Voyage
 *
 *  Author: SmartThings
 *  Date: 2013-03-07
 *
 *  Monitors a set of presence detectors and triggers a mode change when everyone has left.
 */

definition(
    name: "Depricated: Everyone is GONE (Using Routines)",
    namespace: "Mode Based",
    author: "SmartThings",
    description: "Monitors a set of SmartSense Presence tags or smartphones and triggers a mode change when everyone has left. Use Modes to turn on/off appliances, adjust the thermostat, turn on security apps, etc",
    category: "Mode Magic",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/App-LightUpMyWorld.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/App-LightUpMyWorld@2x.png"
)

preferences {
	section("When all of these people leave home") {
		input "people", "capability.presenceSensor", multiple: true
	}
	section("Change to this mode") {
		input "newMode", "mode", title: "Mode?"
	}
	section("False alarm threshold (defaults to 10 min)") {
		input "falseAlarmThreshold", "decimal", title: "Number of minutes", required: false
	}
	section( "Notifications" ) {
		input("recipients", "contact", title: "Send notifications to", required: false) {
			input "sendPushMessage", "enum", title: "Send a push notification?", options: ["Yes", "No"], required: false
			input "phone", "phone", title: "Send a Text Message?", required: false
		}
        input "helloHome", "enum", title: "Hello Home Notification?", options: ["Yes", "No"], required: false, defaultValue: "Yes"
	}

}

def installed() {
	log.debug "Installed with settings: ${settings}"
	log.debug "Current mode = ${location.mode}, people = ${people.collect{it.label + ': ' + it.currentPresence}}"
	subscribe(people, "presence", presence)
}

def updated() {
	log.debug "Updated with settings: ${settings}"
	log.debug "Current mode = ${location.mode}, people = ${people.collect{it.label + ': ' + it.currentPresence}}"
	unsubscribe()
	subscribe(people, "presence", presence)
}

def presence(evt)
{
	log.debug "evt.name: $evt.value"
	if (evt.value == "not present") {
		if (location.mode != newMode) {
			log.debug "checking if everyone is away"
			if (everyoneIsAway()) {
				log.debug "starting sequence"
				runIn(findFalseAlarmThreshold() * 60, "takeAction", [overwrite: false])
			}
		}
		else {
			log.debug "mode is the same, not evaluating"
		}
	}
	else {
		log.debug "present; doing nothing"
	}
}

def takeAction()
{
	if (everyoneIsAway()) {
		def threshold = 1000 * 60 * findFalseAlarmThreshold() - 1000
		def awayLongEnough = people.findAll { person ->
			def presenceState = person.currentState("presence")
			def elapsed = now() - presenceState.rawDateCreated.time
			elapsed >= threshold
		}
		log.debug "Found ${awayLongEnough.size()} out of ${people.size()} person(s) who were away long enough"
		if (awayLongEnough.size() == people.size()) {
			// TODO -- uncomment when app label is available
			//def message = "${app.label} changed your mode to '${newMode}' because everyone left home"
			def message = "SmartThings changed your mode to '${newMode}' because everyone left home"
			log.debug message
			send(message)
			setLocationMode(newMode)
		} else {
			log.debug "not everyone has been away long enough; doing nothing"
		}
	} else {
    	log.debug "not everyone is away; doing nothing"
    }
}

private everyoneIsAway()
{
	def result = true
	for (person in people) {
		if (person.currentPresence == "present") {
			result = false
			break
		}
	}
	log.debug "everyoneIsAway: $result"
	return result
}

private send(msg) {
	if (location.contactBookEnabled) {
        log.debug("sending notifications to: ${recipients?.size()}")
		sendNotificationToContacts(msg, recipients)
	}
	else  {
		if (sendPushMessage != "No") {
			log.debug("sending push message")
			sendPush(msg)	//first try sending a push notification and hello home notification (included in sendPush command)
		}
        else if(helloHome != "No"){		//if no push notification, check if the user wants a hello home notification
        	log.debug("sending Hello Home notification")
            sendNotificationEvent(msg)
        }

		if (phone) {
			log.debug("sending text message")
			sendSms(phone, msg)
		}
	}
	log.debug msg
}

private findFalseAlarmThreshold() {
	(falseAlarmThreshold != null && falseAlarmThreshold != "") ? falseAlarmThreshold : 10
}