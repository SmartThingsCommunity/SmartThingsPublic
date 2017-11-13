/**
 *  WeatherMood
 *
 *  Copyright 2017 David Becher
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
import java.util.regex.* 
 
definition(
    name: "Weather Mood Lighting",
    namespace: "thinkdavid",
    author: "David Becher",
    description: "Changes the color of a light depending on the weather forecast.",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png") {
    appSetting "WU"
}


preferences {
	section("Which light would you like to respond to the weather?") {
    	input "thelight", "capability.colorControl", required: true
    }
    section("Send Push Notification?") {
        input "sendPush", "bool", required: false,
              title: "Send Push Notification with the weather if turned on in the morning?"
    }
}

def installed() {
	log.debug "Installed with settings: ${settings}"

	initialize()
}

def updated() {
	log.debug "Updated with settings: ${settings}"

	unsubscribe()
	initialize()
}

def initialize() {
	// the selected light's switch will be the trigger for the smartApp.
	subscribe(thelight, "switch.on", lightTurnedOnHandler)
}

def lightTurnedOnHandler(evt) {
	log.debug "lightTurnedOn: $evt"

// Parameters for the Weather Underground API Call
    def params = [
    uri: "http://api.wunderground.com",
    path: "/api/18c51436d4714689/conditions/q/VA/Richmond.json"
]

def weather
def temp

// Weather Underground API Call to get the weather
try {
    httpGet(params) { resp ->
        log.debug "${resp.data.current_observation.weather}"
        log.debug "${resp.data.current_observation.temp_f}"
        temp = "${resp.data.current_observation.temp_f}"
        weather = "${resp.data.current_observation.weather}"
    }
} catch (e) {
    log.error "something went wrong: $e"
}

// Only the morning light
def start = timeToday("06:00", location.getTimeZone())
def end = timeToday("10:00", location.getTimeZone())
def now = new Date()

log.debug "start: ${start}"
log.debug "end: ${end}"
log.debug "now: ${now}"

// Send push notification with the weather
if(timeOfDayIsBetween(start, end, now, location.timeZone))
sendPush("Hello! You've turned on your weather light! It's ${temp} degrees farenheit today and the conditions are ${weather}")

//Depending on the return of weather, set the light's colour hue.
// Hue is set on a percentage - so to set the hue = (hue degree/360)
if (weather =~ ".*Rain.*") { // if raining, take precedence
    thelight.setHue(70)
    thelight.setSaturation(100)
    log.debug "Color Changed: Rain"
} else if (temp < 50) { // if cold
	thelight.setHue(58)
    thelight.setSaturation(100)
    log.debug "Color Changed: Cold"
} else if (temp > 80) { // if hot
	thelight.setHue(10)
    thelight.setSaturation(100)
    log.debug "Color Changed: Hot"
} else if (weather =~ ".*Cloudy" || weather == "Overcast") { // if cloudy 
    thelight.setHue(23)
    thelight.setSaturation(56)
    log.debug "Color Changed: Cloudy"
} else { // anything else, we're good. essentially, not cloudy, not raining, not cold, not hot = green
	thelight.setHue(42)
    thelight.setSaturation(100)
    log.debug "No match for weather: ${weather}"
}

log.debug "after: ${thelight.currentValue('hue')}"
log.debug "after: ${thelight.currentValue('saturation')}"

}