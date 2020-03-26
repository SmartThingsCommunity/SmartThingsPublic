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
 *  Good Night
 *
 *  Author: SmartThings
 *  Date: 2013-03-07
 */
definition(
    name: "Good Night",
    namespace: "smartthings",
    author: "SmartThings",
    description: "Changes mode when motion ceases after a specific time of night.",
    category: "Mode Magic",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/ModeMagic/good-night.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/ModeMagic/good-night@2x.png"
)

preferences {
	section("When there is no motion on any of these sensors") {
		input "motionSensors", "capability.motionSensor", title: "Where?", multiple: true
	}
	section("For this amount of time") {
		input "minutes", "number", title: "Minutes?"
	}
	section("After this time of day") {
		input "timeOfDay", "time", title: "Time?"
	}
	section("And (optionally) these switches are all off") {
		input "switches", "capability.switch", multiple: true, required: false
	}
	section("Change to this mode") {
		input "newMode", "mode", title: "Mode?"
	}
	section( "Notifications" ) {
        input("recipients", "contact", title: "Send notifications to") {
            input "sendPushMessage", "enum", title: "Send a push notification?", options: ["Yes", "No"], required: false
            input "phoneNumber", "phone", title: "Send a Text Message?", required: false
        }
	}

}

def installed() {
	log.debug "Current mode = ${location.mode}"
	createSubscriptions()
}

def updated() {
	log.debug "Current mode = ${location.mode}"
	unsubscribe()
	createSubscriptions()
}

def createSubscriptions()
{
	subscribe(motionSensors, "motion.active", motionActiveHandler)
	subscribe(motionSensors, "motion.inactive", motionInactiveHandler)
	subscribe(switches, "switch.off", switchOffHandler)
	subscribe(location, modeChangeHandler)

	if (state.modeStartTime == null) {
		state.modeStartTime = 0
	}
}

def modeChangeHandler(evt) {
	state.modeStartTime = now()
}

def switchOffHandler(evt) {
	if (correctMode() && correctTime()) {
		if (allQuiet() && switchesOk()) {
			takeActions()
		}
	}
}

def motionActiveHandler(evt)
{
	log.debug "Motion active"
}

def motionInactiveHandler(evt)
{
	// for backward compatibility
	if (state.modeStartTime == null) {
		subscribe(location, modeChangeHandler)
		state.modeStartTime = 0
	}

	if (correctMode() && correctTime()) {
		runIn(minutes * 60, scheduleCheck, [overwrite: false])
	}
}

def scheduleCheck()
{
	log.debug "scheduleCheck, currentMode = ${location.mode}, newMode = $newMode"
	
	if (correctMode() && correctTime()) {
		if (allQuiet() && switchesOk()) {
			takeActions()
		}
	}
}

private takeActions() {
	def message = "Goodnight! SmartThings changed the mode to '$newMode'"
	send(message)
	setLocationMode(newMode)
	log.debug message
}

private correctMode() {
	if (location.mode != newMode) {
		true
	} else {
		log.debug "Location is already in the desired mode:  doing nothing"
		false
	}
}

private correctTime() {
	def t0 = now()
	def modeStartTime = new Date(state.modeStartTime)
	def startTime = timeTodayAfter(modeStartTime, timeOfDay, location.timeZone)
	if (t0 >= startTime.time) {
		true
	} else {
		log.debug "The current time of day (${new Date(t0)}), is not in the correct time window ($startTime):  doing nothing"
		false
	}
}

private switchesOk() {
	def result = true
	for (it in (switches ?: [])) {
		if (it.currentSwitch == "on") {
			result = false
			break
		}
	}
	log.debug "Switches are all off: $result"
	result
}

private allQuiet() {
	def threshold = 1000 * 60 * minutes - 1000
	def states = motionSensors.collect { it.currentState("motion") ?: [:] }.sort { a, b -> b.dateCreated <=> a.dateCreated }
	if (states) {
		if (states.find { it.value == "active" }) {
			log.debug "Found active state"
			false
		} else {
			def sensor = states.first()
		    def elapsed = now() - sensor.rawDateCreated.time
			if (elapsed >= threshold) {
				log.debug "No active states, and enough time has passed"
				true
			} else {
				log.debug "No active states, but not enough time has passed"
				false
			}
		}
	} else {
		log.debug "No states to check for activity"
		true
	}
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

        if (phoneNumber) {
            log.debug("sending text message")
            sendSms(phoneNumber, msg)
        }
    }

	log.debug msg
}
