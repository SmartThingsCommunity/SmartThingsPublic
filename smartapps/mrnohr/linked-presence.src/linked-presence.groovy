/**
 *  Double Presence
 *
 *  Copyright 2014 Matt Nohr
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
		name: "Linked Presence",
		namespace: "mrnohr",
		author: "Matt Nohr",
		description: "Send an alert of one presence sensor leaves without the other",
		category: "My Apps",
		iconUrl: "https://s3.amazonaws.com/smartapp-icons/Meta/text_presence.png",
		iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Meta/text_presence@2x.png")

/**
 * Based on the discussion here: http://community.smartthings.com/t/linking-presence-sensors-in-pairs-for-seniors/4915
 *
 * Get an alert if one presence sensor leaves without the other one. The alert could be turning on a light, or sending
 * a push/text message. The original idea was to monitor aging parents where one parent shouldn't be leaving the house
 * without the other one. Could also work with parents and children, making sure a child doesn't leave without a parent.
 *
 * It works by subscribing to "not present" events for the "dependant" presence sensor. If the "main" sensor hasn't also
 * recently left (in the past minute), then send the alert.
 */
preferences {
	section("Presence Sensors") {
		input "sensorDependant", "capability.presenceSensor", title: "When this person leaves alone"
		input "sensorMain", "capability.presenceSensor", title: "Without this person"
		input "timeDelay", "number", title: "With this delay (minutes)", default: 1
	}
	section("Alerts") {
		input "switches", "capability.switch", title: "Turn on some lights?", required: false, multiple: true
		input "sendPushMessage", "enum", title: "Send a push notification?", options: ["Yes", "No"], required: false
		input "phoneNumber", "phone", title: "Send a text message?", required: false
	}
}

def installed() {
	log.debug "Installed with settings: ${settings}"

	initialize()
}

def updated() {
	log.debug "Updated with settings: ${settings}"

	unsubscribe()
	unschedule()
	initialize()
}

def initialize() {
	subscribe(sensorDependant, "presence.not present", "leavingHandler")
}

//this will just handle scheduling/calling the checkOtherPresence method to handle the delay
def leavingHandler(evt) {
	if(timeDelay > 0) {
		log.debug "scheduling presence check for $timeDelay minute(s)"
		runIn(timeDelay * 60, "checkOtherPresence")
	} else {
		checkOtherPresence()
	}
}

def checkOtherPresence() {
	log.debug "checking other presence sensor"
	def dependantEvent = sensorDependant.latestState("presence")

	//first make sure the dependant is still away
	if(dependantEvent.value == "not present") {
		//make sure there is an event first, just in the rare case there are no events
		def mainEvent = sensorMain.latestState("presence")
		if(mainEvent) {
			//compare latest events from both devices
			def eventTimeDifference = Math.abs(dependantEvent.date.time - mainEvent.date.time) / (1000 * 60) //minutes
			def allowedTimeDifference = 1 //maybe use same timeDelay?

			//make sure dependant left with main
			if(mainEvent != "not present" || eventTimeDifference > allowedTimeDifference) {
				def message = "$sensorDependant left without $sensorMain"
				log.warn message
				send(message) // send push/text
				switches.on() // turn on lights
			}
		} else {
			//this should only happen if sensorMain is brand new and has never had any events
			log.warn "$sensorDependant left but no events for $sensorMain"
		}
	}
}

private send(msg) {
	if(sendPushMessage != "No") {
		log.debug "Sending push notification: $msg"
		sendPush(msg)
	}

	if(phoneNumber) {
		log.debug "Sending text notification: $msg"
		sendSms(phoneNumber, msg)
	}
}