/**
 *  Turn On Dimmed Bulb With Motion
 *
 *  Copyright 2015 Allen Brown
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
 *	I shameless borrowed from other apps to achieve the sunset/sunrise options. 
 */
definition(
    name: "Turn On Dimmed Bulb With Motion",
    namespace: "piguy54",
    author: "Allen Brown",
    description: "Do you have bulbs that Smart Lighting can't dim (looking at you GE Link)? In that case this does it. Simple implementation of turn on, on motion, at a set dimmer level and off again after inactive time.",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
	 page(name: "pageOne", title: "Basic Options", nextPage: "pageTwo", uninstall: true) {
		section("When there's movement turn on (required)") {
			input "motion1", "capability.motionSensor", title: "Where?", multiple: true, required: true
		}
    	section("At what dimmer level [1-100] (required)"){
			input "dimmerLevel", "number", title: "Dim Level?", required: true
		}
    	section("And off when no motion for (optional)"){
			input "delayMinutes", "number", title: "Minutes?"
		}
		section("Turn on/off lights (required)"){
			input "switch1", "capability.switch", multiple: true, required: true
		}
   }
   page(name: "pageTwo", title: "Some more timing options", nextPage: "pageThree") {
   		section("Which modes to trigger this automation? (optional)") {
			input "modes", "mode", title: "Only when mode is", multiple: true, required: false
        }
        section ("Only trigger between Sunset and Sunrise? (required)") {
        	input "sunEnabled", "bool", title: "On/Off", required:true
        }
        section ("Sunset offset (optional)") {
			input "sunsetOffsetValue", "text", title: "HH:MM", required: false
			input "sunsetOffsetDir", "enum", title: "Before or After", required: false, options: ["Before","After"]
        }
		section ("Sunrise offset (optional)") {
			input "sunriseOffsetValue", "text", title: "HH:MM", required: false
			input "sunriseOffsetDir", "enum", title: "Before or After", required: false, options: ["Before","After"]
		}
   }
   page(name: "pageThree", title: "Name app and configure modes", install: true, uninstall: true) {
   	    section([mobileOnly:true]) {
	        label title: "Assign a name", required: false
       }
   }
}

def installed() {
	initialize()
}

def updated() {
	unsubscribe()
	unschedule()
	initialize()
}

def initialize() {
	subscribe(motion1, "motion", motionHandler)
    subscribe(location, "position", locationPositionChange, [filterEvents: false])
	subscribe(location, "sunriseTime", sunriseSunsetTimeHandler, [filterEvents: false])
	subscribe(location, "sunsetTime", sunriseSunsetTimeHandler, [filterEvents: false])
	astroCheck()
}

def sunriseSunsetTimeHandler(evt) {
	state.lastSunriseSunsetEvent = now()
	log.debug "DimmedBulb.sunriseSunsetTimeHandler($app.id)"
	astroCheck()
}

def motionHandler(evt) {
	log.debug "$evt.name: $evt.value"
	if (evt.value == "active") {
		if (sunRiseSetEnabled() && modeEnabled()) {
			log.debug "turning on lights due to motion"
            switch1.setLevel(dimmerLevel)
			switch1.on()
			state.lastStatus = "on"
		}
		state.motionStopTime = null
	}
	else {
		state.motionStopTime = now()
		if(delayMinutes) {
			runIn(delayMinutes*60, turnOffMotionAfterDelay, [overwrite: false])
		} else {
			turnOffMotionAfterDelay()
		}
	}
}

def turnOffMotionAfterDelay() {
	log.trace "In turnOffMotionAfterDelay, state.motionStopTime = $state.motionStopTime, state.lastStatus = $state.lastStatus"
	if (state.motionStopTime && state.lastStatus != "off") {
		def elapsed = now() - state.motionStopTime
        log.trace "elapsed = $elapsed"
		if (elapsed >= ((delayMinutes ?: 0) * 60000L) - 2000) {
        	log.debug "Turning off lights"
			switch1.off()
			state.lastStatus = "off"
		}
	}
}

def scheduleCheck() {
	log.debug "In scheduleCheck - skipping"
}

def astroCheck() {
	def s = getSunriseAndSunset(zipCode: zipCode, sunriseOffset: sunriseOffset, sunsetOffset: sunsetOffset)
	state.riseTime = s.sunrise.time
	state.setTime = s.sunset.time
	log.debug "rise: ${new Date(state.riseTime)}($state.riseTime), set: ${new Date(state.setTime)}($state.setTime)"
}

private sunRiseSetEnabled() {
	def result
	def t = now()
	if (sunEnabled) {
    	result = t < state.riseTime || t > state.setTime
	}
    else {
    	result = true
    }
	log.debug "time based : $result"
	result
}

private modeEnabled() {
	def result
    def currMode = location.currentMode
    boolean isCollection = modes instanceof Collection
	log.debug "$location.currentMode $modes"
	if (modes == null) {
    	result = true
    }
	if (isCollection) {
		result = modes.contains(currMode)
    }
    else {
		result = currMode == modes
	}
    log.debug "mode based : $result"
    result
}

private getSunriseOffset() {
	sunriseOffsetValue ? (sunriseOffsetDir == "Before" ? "-$sunriseOffsetValue" : sunriseOffsetValue) : null
}

private getSunsetOffset() {
	sunsetOffsetValue ? (sunsetOffsetDir == "Before" ? "-$sunsetOffsetValue" : sunsetOffsetValue) : null
}