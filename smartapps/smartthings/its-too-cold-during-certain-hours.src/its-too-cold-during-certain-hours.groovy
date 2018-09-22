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
 *
 * Updated: c.mccurry - 2017-01-14 - added time range
 *
 */
definition(
    name: "It's Too Cold during certain hours",
    namespace: "SmartThings",
    author: "SmartThings",
    description: "Monitor the temperature and when it drops below your setting get a text and/or turn on a heater or additional appliance.  Only during certain times",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Meta/temp_thermo-switch.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Meta/temp_thermo-switch@2x.png"
)

preferences {
	section("Monitor the temperature...") {
		input "temperatureSensor1", "capability.temperatureMeasurement"
	}
	section("Turn on When the temperature drops below...") {
		input "temperature1", "number", title: "Temperature?"
	}
/* added 2017-01-13 */
	section("Turn off When the temperature rises above...") {
		input "temperature2", "number", title: "Temperature?"
	}
     section("Between these times only"){
		input "timeBegin", "time", title: "Starting at"
		input "timeEnd", "time", title: "Ending at"
    }
/* end */

    section( "Notifications" ) {
        input("recipients", "contact", title: "Send notifications to") {
            input "sendPushMessage", "enum", title: "Send a push notification?", options: ["Yes", "No"], required: false
            input "phone1", "phone", title: "Send a Text Message?", required: false
        }
    }
	section("Turn on a heater...") {
		input "switch1", "capability.switch", required: false
	}
}

def installed() {
	subscribe(temperatureSensor1, "temperature", temperatureHandler)
}

def updated() {
	unsubscribe()
	subscribe(temperatureSensor1, "temperature", temperatureHandler)
}

def temperatureHandler(evt) {
	log.trace "temperature: $evt.value, $evt"


/* Added 2017-01-14 */    

    def startCheck = timeToday(timeBegin)
    def stopCheck = timeToday(timeEnd)
    
    log.debug "startCheck: ${startCheck}"
    log.debug "stopCheck: ${stopCheck}"
    
    def between = timeOfDayIsBetween(startCheck, stopCheck, (new Date()), location.timeZone)
    /* For testing, hard-set between to something */
    /* between = 1 */
    log.debug "between: ${between}"    
    
    if ( !between ) {
        log.debug "Exiting Too Cold during certain hours - Not currently between begin / end times"
        return }
    else {
        log.debug "Continuing Too Cold during certain hours:  It is currently between begin / end times"
    } 

    def tooHot = temperature2
    
    if (evt.doubleValue >= tooHot) {
		log.debug "Now it's too hot! - turning switch off!"
        log.debug "Turning Switch Off"
		switch1?.off()
        return
    }
        
/* End of new adds */

	def tooCold = temperature1
	def mySwitch = settings.switch1

	// TODO: Replace event checks with internal state (the most reliable way to know if an SMS has been sent recently or not).
	if (evt.doubleValue <= tooCold) {
		log.debug "Checking how long the temperature sensor has been reporting <= $tooCold"

		// Don't send a continuous stream of text messages
		def deltaMinutes = 10 // TODO: Ask for "retry interval" in prefs?
		def timeAgo = new Date(now() - (1000 * 60 * deltaMinutes).toLong())
		def recentEvents = temperatureSensor1.eventsSince(timeAgo)?.findAll { it.name == "temperature" }
		log.trace "Found ${recentEvents?.size() ?: 0} events in the last $deltaMinutes minutes"
		def alreadySentSms = recentEvents.count { it.doubleValue <= tooCold } > 1

		if (alreadySentSms) {
			log.debug "SMS already sent within the last $deltaMinutes minutes"
			// TODO: Send "Temperature back to normal" SMS, turn switch off
		} else {
			log.debug "Temperature dropped below $tooCold:  sending SMS and activating $mySwitch"
			def tempScale = location.temperatureScale ?: "F"
			send("${temperatureSensor1.displayName} is too cold, reporting a temperature of ${evt.value}${evt.unit?:tempScale}")
            /* Added 2017-01-14 */
            log.debug "Turning Switch On"
            /* End of Adds */
			switch1?.on()
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