/**
 *  ps_Dust to Dawn Motion Lights
 *
 *  Copyright 2014 patrick@patrickstuart.com
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
    name: "ps_Dust to Dawn Motion Lights",
    namespace: "ps",
    author: "patrick@patrickstuart.com",
    description: "Turn lights on at Dust, motion bright, fade out no motion, turn off lights at Dawn...",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
	section("Select Dimmers you want to Use") {
        input "motions", "capability.motionSensor", title: "Motions", required: false, multiple: true
        input "switches", "capability.switchLevel", title: "Switches", required: false, multiple: true
	}
    section ("Zip code (optional, defaults to location coordinates)...") {
		input "zipCode", "text", required: false
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
    subscribe(motions, "motion", handleMotionEvent)
    checkSun()   
}

def checkSun() {
    def zip = settings.zip as String
    def sunInfo = getSunriseAndSunset(zipCode: zip)
    def current = now()
    if(sunInfo.sunrise.time > current || sunInfo.sunset.time < current) {
    	state.sunMode = "SunsetMode"
    }
    else {
    	state.sunMode = "SunriseMode"
    }
    log.info("Sunset: ${sunInfo.sunset.time}")
    log.info("Sunrise: ${sunInfo.sunrise.time}")
    log.info("Current: ${current}")
    log.info("sunMode: ${state.sunMode}")
    
    if(current < sunInfo.sunrise.time) {
    	runIn(((sunInfo.sunrise.time - current) / 1000).toInteger(), setSunrise)
    }
    
    if(current < sunInfo.sunset.time) {
    	runIn(((sunInfo.sunset.time - current) / 1000).toInteger(), setSunset)
    }
    schedule(timeTodayAfter(new Date(), "01:00", location.timeZone), checkSun)
    if (state.sunMode == "SunriseMode")
    {
    	setSunrise()
    }
    else
    {
    	setSunset()
    }
}

def handleMotionEvent(evt) {
	log.debug "Motion event Detected do something $evt"
    log.debug state
    state.motion = evt.value
    
    //testing force state.sunMode to SunsetMode
    //state.sunMode = "SunsetMode"
    
    if (state.sunMode == "SunsetMode" && state.motion == "active") {
    	switches?.setLevel(100)
        state.Level = "99"
    	log.debug "Set the switches to level 100"
        log.debug state
        
    	//Change this to # of seconds to leave light on
        runIn(300, setSunset)
    }
    else 
    {
    	log.debug "it is after sunrise but before sunset, do nothing"
    //runIn(120, sunriseHandler)
    }
}

def setSunset() {
	log.debug "Sunset handler"
    switches?.setLevel(20)
    state.Level = "20"
    // if state.motion is false
}

def setSunrise() {
	log.info "Executing sunrise handler"
    switches?.setLevel(0)
    state.Level = "off"
}