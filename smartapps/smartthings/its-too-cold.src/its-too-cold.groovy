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
 *  It's Too Cold
 *
 *  Author: SmartThings
 *  Modified by: Corey Seliger
 * 
 */
definition(
    name: "It's Too Cold",
    namespace: "smartthings",
    author: "SmartThings",
    description: "Monitor the temperature and when it drops below your setting get a text and/or turn on a heater or additional appliance.",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Meta/temp_thermo-switch.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Meta/temp_thermo-switch@2x.png",
    pausable: true
)

preferences {
	section("Monitor the temperature...") {
		input "temperatureSensor1", "capability.temperatureMeasurement"
	}
	section("When the temperature drops below...") {
		input "thresholdTemperature", "number", title: "Temperature?"
	}
    section( "Notifications" ) {
        input("recipients", "contact", title: "Send notifications to") {
            input "sendPushMessage", "enum", title: "Send a push notification?", options: ["Yes", "No"], required: false
            input "phone1", "phone", title: "Send a Text Message?", required: false
            input "notificationInterval", "number", title: "Notification Interval", description: "Notification interval in minutes (0 for none)", defaultValue: "10", required: false
        }
    }
	section("Turn on a heater...") {
		input "switch1", "capability.switch", required: false
	}
}

def installed() {
	initialize()
	subscribe(temperatureSensor1, "temperature", temperatureHandler)
}

def updated() {
	unsubscribe()
	initialize()
	subscribe(temperatureSensor1, "temperature", temperatureHandler)
}

def initialize () {
	// Define some variables that need to persist between events
    state.isTooCold = false // Assume false initially, let handler decide later
    state.lastSentNotice = 0 // The last time a notice (push/text) was sent.
    state.eventStartTime = 0 // Useful for tracking last update, etc.
}

def getElapsedTime(startTime, currentTime) {
	// Returns elapsed time in minutes
	return ((currentTime - startTime) / (1000 * 60).toLong()).toLong()
}

def temperatureHandler(evt) {
	log.trace "temperature: $evt.value, $evt"

	if (evt.doubleValue <= thresholdTemperature) {
		// We are entering or continuing to handle an active event.
		if (!state.isTooCold) {
			// Handle a new event.

			// Set the state for the event
			state.isTooCold = true
			state.eventStartTime = now()
			state.lastSentNotice = state.eventStartTime

			// Notify the user
			log.debug "Temperature dropped below $thresholdTemperature:  sending SMS and activating $switch1"
			send("${temperatureSensor1.displayName} is too cold, reporting a temperature of ${evt.value}${evt.unit?:tempScale}")

			// Attempt to do something about it.
			switch1?.on()

		} else {
			// Handle a continuing event
            def elapsedMinutes = getElapsedTime(state.eventStartTime, now())
			if (notificationInterval > 0 && getElapsedTime(state.lastSentNotice, now()) >= notificationInterval) {
				// Update our last update time and send a new update.
				state.lastSentNotice = now()
				send("${temperatureSensor1.displayName} continues to be too cold for $elapsedMinutes minutes, reporting a temperature of ${evt.value}${evt.unit?:tempScale}")
			} else {
				log.debug "Temperature continues below $thresholdTemperature ${evt.unit?:tempScale} for $elapsedMinutes minutes"
            }
		}
	} else { 
		if (state.isTooCold) {
			// We are resolving an active event

			// Shut off the switch
			switch1?.off()

			// Calculate some metrics and report back to the user
			def elapsedMinutes = getElapsedTime(state.eventStartTime, now()) 
			def minuteWord = elapsedMinutes != 1 ? "minutes" : "minute"
			send("${temperatureSensor1.displayName} returned to normal after $elapsedMinutes $minuteWord, reporting a temperature of ${evt.value}${evt.unit?:tempScale}")

			// Reset our metrics
			initialize()
		}  
	}
}

private send(msg) {
    if (location.contactBookEnabled) {
        log.debug("sending notifications to: ${recipients?.size()}")
        sendNotificationToContacts(msg, recipients)
    }
    else {
        if (sendPushMessage != "No") {
            log.debug("sending push message")
            sendPush(msg)
        }

        if (phone1) {
            log.debug("sending text message")
            sendSms(phone1, msg)
        }
    }

    log.debug msg
}
