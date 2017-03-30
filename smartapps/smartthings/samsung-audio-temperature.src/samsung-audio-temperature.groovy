/**
 *  Samsung Cutom Message
 *
 *  Copyright 2014 yijoo
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
    name: "Samsung Audio Temperature",
    namespace: "smartthings",
    author: "yijoo",
    description: "Play a custom message through your Samsung Audio when the temperature is changed.",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
	page(name: "mainPage", title: "Play a message on your Samsung Audio when something happens", install: true, uninstall: true)
}

def mainPage() {
	dynamicPage(name: "mainPage") {
		section("Play message when"){
			input "temperatureSensor", "capability.temperatureMeasurement", title: "Temperature Sensor", required: false, multiple: true
		}
		section{
			input "conditionType", "enum", title: "Temperature Condition?", required: true, defaultValue: "Raise above...", options: [
				"Raise above...",
				"Drop below..."]
		}
		section("When temperature rises above... or drops below...") {
			input "temperatureValue", "number", title: "Temperature?"
		}
		section{
			input "message","text",title:"Play this message", required:false, multiple: false
		}
        section {
			input "SamsungAudio", "capability.musicPlayer", title: "On this Samsung player", required: true
		}
	}
}

def installed() {
	log.debug "Installed with settings: ${settings}"
	subscribeToEvents()
}

def updated() {
	log.debug "Updated with settings: ${settings}"
	unsubscribe()
	subscribeToEvents()
}

def subscribeToEvents() {
	subscribe(temperatureSensor, "temperature", temperatureHandler)
	subscribe(app, appTouchHandler)
    
	loadText()
}

def temperatureHandler(evt) {
	log.trace "temperature: $evt.value, $evt"
//	log.trace "eventHandler($evt?.name: $evt?.value)"

	def temp =  temperatureValue
    log.trace "teperature value : $temp"
    
	switch (conditionType) {
    	case "Raise above..." :
				// TODO: Replace event checks with internal state
log.trace "Raise above..."
				if (evt.doubleValue > temperatureValue) {
					log.debug "Checking how long the temperature sensor has been reporting > $temp"

					// Don't send a continuous stream of text messages
					def deltaMinutes = 10 // TODO: Ask for "retry interval" in prefs?
					def timeAgo = new Date(now() - (1000 * 60 * deltaMinutes).toLong())
					def recentEvents = temperatureSensor.eventsSince(timeAgo)?.findAll { it.name == "temperature" }
					log.trace "Found ${recentEvents?.size() ?: 0} events in the last $deltaMinutes minutes"
					def alreadySentSms = recentEvents.count { it.doubleValue > temperatureValue } > 1

					if (alreadySentSms) {
						log.debug "SMS already sent to $phone1 within the last $deltaMinutes minutes"
						// TODO: Send "Temperature back to normal" SMS, turn switch off
					} else {
						takeAction(evt)
					}
				}
        		break;

		case "Drop below..." :
				// TODO: Replace event checks with internal state
log.trace "Drop below..."
				if (evt.doubleValue < temperatureValue) {
					log.debug "Checking how long the temperature sensor has been reporting <  $temp"

					// Don't send a continuous stream of text messages
					def deltaMinutes = 10 // TODO: Ask for "retry interval" in prefs?
					def timeAgo = new Date(now() - (1000 * 60 * deltaMinutes).toLong())
					def recentEvents = temperatureSensor.eventsSince(timeAgo)?.findAll { it.name == "temperature" }
					log.trace "Found ${recentEvents?.size() ?: 0} events in the last $deltaMinutes minutes"
					def alreadySentSms = recentEvents.count { it.doubleValue < temperatureValue } > 1

					if (alreadySentSms) {
						log.debug "SMS already sent to $phone1 within the last $deltaMinutes minutes"
						// TODO: Send "Temperature back to normal" SMS, turn switch off
					} else {
						takeAction(evt)
					}
				}
        		break;
		}

/*
	// TODO: Replace event checks with internal state

	if (evt.doubleValue >= tooHot) {
		log.debug "Checking how long the temperature sensor has been reporting <= $tooHot"

		// Don't send a continuous stream of text messages
		def deltaMinutes = 10 // TODO: Ask for "retry interval" in prefs?
		def timeAgo = new Date(now() - (1000 * 60 * deltaMinutes).toLong())
		def recentEvents = temperatureSensor1.eventsSince(timeAgo)?.findAll { it.name == "temperature" }
		log.trace "Found ${recentEvents?.size() ?: 0} events in the last $deltaMinutes minutes"
		def alreadySentSms = recentEvents.count { it.doubleValue >= tooHot } > 1

		if (alreadySentSms) {
			log.debug "SMS already sent to $phone1 within the last $deltaMinutes minutes"
			// TODO: Send "Temperature back to normal" SMS, turn switch off
		} else {
			takeAction(evt)
		}
	}
*/
}

private takeAction(evt) {
	log.trace "takeAction()"

	SamsungAudio.playTrackAndRestore(state.sound.uri, state.sound.duration, volume)

	if (frequency || oncePerDay) {
		state[frequencyKey(evt)] = now()
	}
	log.trace "Exiting takeAction()"
}

private frequencyKey(evt) {
	"lastActionTimeStamp"
}

private dayString(Date date) {
	def df = new java.text.SimpleDateFormat("yyyy-MM-dd")
	if (location.timeZone) {
		df.setTimeZone(location.timeZone)
	}
	else {
		df.setTimeZone(TimeZone.getTimeZone("America/New_York"))
	}
	df.format(date)
}

private oncePerDayOk(Long lastTime) {
	def result = true
	if (oncePerDay) {
		result = lastTime ? dayString(new Date()) != dayString(new Date(lastTime)) : true
		log.trace "oncePerDayOk = $result"
	}
	result
}

// TODO - centralize somehow
private oncePerDayOk() {
	modeOk && daysOk && timeOk
}
// TODO - End Centralize

def appTouchHandler(evt) {
	takeAction(evt)
}

private loadText() {

	if (message) {
		state.sound = textToSpeech(message instanceof List ? message[0] : message) // not sure why this is (sometimes) needed)
	}
	else {
		state.sound = textToSpeech("You selected the custom message option but did not enter a message in the $app.label Smart App")
	}
}