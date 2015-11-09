/**
 *  Change Mode Based on Illuminance And/Or Sunset/Sunrise And/Or Time of Day
 *
 *  Copyright 2015 Anders Heie
 *
 *  Based on Smartthings "Sunrise, Sunset" application
 *  Based on Arno Arnaud "Bright When Dark And/Or Bright After Sunset" application
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
    name: "Change Mode Based on Illuminance And/Or Sunset/Sunrise And/Or Time of Day",
    namespace: "pope",
    author: "Anders Heie",
    description: "Change the mode based on the illuminanceof or more illuminance detectors, or sunset/sunrise for your location, or time of day.",
    category: "Mode Magic",
    iconUrl: "http://cdn.device-icons.smartthings.com/Weather/weather11-icn.png",
    iconX2Url: "http://cdn.device-icons.smartthings.com/Weather/weather11-icn@2x.png",
    iconX3Url: "http://cdn.device-icons.smartthings.com/Weather/weather11-icn@2x.png")

preferences {
 	section("Change mode to")
    {
  		input "newMode", "mode", title: "Mode", required: true
    }
    section("Using any of these light sensors...")
    {
        input "lightSensors", "capability.illuminanceMeasurement",title: "Light Sensors", multiple: true, required: false
        input "luxLevelUp", "number", title: "Illuminance Rising Above (0-999)", required: false
        input "luxLevelDown", "number", title: "Illuminance falling below (1-1000)", required: false
    }
    section("Force change at this time every day") {
   		input "startTime", "time", title: "Time of Day", required: false
  	}
	section ("Sunset/Sunrise") {
    	input "sunriseOn", "bool", title: "Change mode at Sunrise?", required: true, default: false
        input "sunsetOn", "bool", title: "Change mode at Sunset?", required: true, default: false
	}
	section ("Sunrise offset (optional)...") {
		input "sunriseOffsetValue", "text", title: "HH:MM", required: false
		input "sunriseOffsetDir", "enum", title: "Before or After", required: false, options: ["Before","After"]
	}
	section ("Sunset offset (optional)...") {
		input "sunsetOffsetValue", "text", title: "HH:MM", required: false
		input "sunsetOffsetDir", "enum", title: "Before or After", required: false, options: ["Before","After"]
	}
	section ("Zip code (optional, defaults to location coordinates)...") {
		input "zipCode", "text", required: false
	}
	section ("Turn on and off these switches when mode changes") {
	    input "switchesOn", "capability.switch", title: "Turn on?", required: false, multiple: true
		input "switchesOff", "capability.switch", title: "Turn off?", required: false, multiple: true
    }
    
	section( "Notifications" ) {
        input("recipients", "contact", title: "Send notifications to") {
            input "sendPushMessage", "enum", title: "Send a push notification?", options: ["Yes", "No"], required: false
            input "phoneNumber", "phone", title: "Send a text message?", required: false
        }
	}
}

def installed() {
	initialize()
}

def updated() {
	unsubscribe()
	//unschedule handled in astroCheck method
	initialize()
}

def initialize() {
	subscribe(location, "position", locationPositionChange)
	subscribe(location, "sunriseTime", sunriseSunsetTimeHandler)
	subscribe(location, "sunsetTime", sunriseSunsetTimeHandler)
    subscribe(lightSensors, "illuminance", illuminanceHandler, [filterEvents: false])
	if (startTime != null && startTime != "") {
		log.debug "Scheduling mode change to run at $startTime"
        // Schedules recurring event at this time (Ignores the Date)
		schedule(startTime, "scheduledStart")
	}
	astroCheck()
}

def scheduledStart() {
	log.trace "We reached startTime time-of-day, triggering mode change"
    changeMode(newMode)
}


def locationPositionChange(evt) {
	log.trace "locationPositionChange()"
	astroCheck()
}

def sunriseSunsetTimeHandler(evt) {
	log.trace "sunriseSunsetTimeHandler()"
	astroCheck()
}

def astroCheck() {
	def s = getSunriseAndSunset(zipCode: zipCode, sunriseOffset: sunriseOffset, sunsetOffset: sunsetOffset)

	def now = new Date()
	def riseTime = s.sunrise
	def setTime = s.sunset
	log.debug "riseTime: $riseTime"
	log.debug "setTime: $setTime"

	if (state.riseTime != riseTime.time) {
		unschedule("sunriseHandler")

		if(riseTime.before(now)) {
			riseTime = riseTime.next()
		}

		state.riseTime = riseTime.time

		log.info "scheduling sunrise handler for $riseTime"
		schedule(riseTime, sunriseHandler)
	}

	if (state.setTime != setTime.time) {
		unschedule("sunsetHandler")

	    if(setTime.before(now)) {
		    setTime = setTime.next()
	    }

		state.setTime = setTime.time

		log.info "scheduling sunset handler for $setTime"
	    schedule(setTime, sunsetHandler)
	}
}

def sunriseHandler() {
	log.info "Executing sunrise handler"
	if (sunriseOn) {
		changeMode(newMode, "Sunrise")
	}
}

def sunsetHandler() {
	log.info "Executing sunset handler"
	if (sunsetOn) {
		changeMode(newMode, "Sunset")
	}
}

def changeMode(newMode, message) {
	log.debug "changeMode($newMode, $message)"
	if (newMode && location.mode != newMode) {
		if (location.modes?.find{it.name == newMode}) {
			setLocationMode(newMode)
            if(switchesOn) {
            	switchesOn.on()
                log.debug "switchesOn.on()"
            }
            if(switchesOff) {
            	switchesOff.off()
                log.debug "switchesOff.off()"
            }
			send "${label} has changed the mode to '${newMode}' because of " + message
		}
		else {
			send "${label} tried to change to undefined mode '${newMode}' because of " + message
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

        if (phoneNumber) {
            log.debug("sending text message")
            sendSms(phoneNumber, msg)
        }
    }

	log.debug msg
}

private getLabel() {
	app.label ?: "SmartThings"
}

private getSunriseOffset() {
	sunriseOffsetValue ? (sunriseOffsetDir == "Before" ? "-$sunriseOffsetValue" : sunriseOffsetValue) : null
}

private getSunsetOffset() {
	sunsetOffsetValue ? (sunsetOffsetDir == "Before" ? "-$sunsetOffsetValue" : sunsetOffsetValue) : null
}

def illuminanceHandler(evt)
{
	log.debug "$evt.name: $evt.value, "
    if (evt.integerValue >  ((luxLevelUp != null && luxLevelUp != "") ? luxLevelUp : 1000))
    	{
        log.debug "Switch to $newMode because illuminance is superior to luxLevelUp lux..."
        changeMode(newMode, "Illuminance of $evt.integerValue > $luxLevelUp")
		}
	else if (evt.integerValue < ((luxLevelDown != null && luxLevelDown != "") ? luxLevelDown : 0))
		{
		log.debug "Switch to $newMode because illuminance is less than luxLevelDown lux..."
        changeMode(newMode, "Illuminance of $evt.integerValue < $luxLevelDown")
        }
}