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
		section("When there's movement turn on...") {
			input "motion1", "capability.motionSensor", title: "Where?", multiple: true, required: true
		}
    	section("At what dimmer level..."){
			input "dimmerLevel", "number", title: "Dim Level?"
		}
    	section("And off when no motion for..."){
			input "minutes1", "number", title: "Minutes?"
		}
		section("Turn on/off lights..."){
			input "switch1", "capability.switch", multiple: true, required: true
		}
   }
   page(name: "pageTwo", title: "Some more timing options") {
   		section("Modes...") {
			input "modes", "mode", title: "Only when mode is", multiple: true, required: false
        }
        section ("Only trigger between Sunset and Sunrise...") {
        	input "sunEnabled", "bool", title: "On/Off", required:true
        }
        section ("Sunset offset (optional)...") {
			input "sunsetOffsetValue", "text", title: "HH:MM", required: false
			input "sunsetOffsetDir", "enum", title: "Before or After", required: false, options: ["Before","After"]
        }
		section ("Sunrise offset (optional)...") {
			input "sunriseOffsetValue", "text", title: "HH:MM", required: false
			input "sunriseOffsetDir", "enum", title: "Before or After", required: false, options: ["Before","After"]
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
    	log.debug "event detected"
    	if (sunRiseSetEnabled() && modeEnabled()) {
			log.debug "turning on lights"
			switch1.on()
        	switch1.setLevel(dimmerLevel)
        }
	} else if (evt.value == "inactive") {
		runIn(60 * minutes1, scheduleCheck, [overwrite: false])
	}
}

def scheduleCheck() {
	log.debug "schedule check"
	def motionState = motion1.currentState("motion")
    log.debug "$motionState.value"
    if (!motionState.contains("active")) {
        def elapsed = now() - motionState.rawDateCreated.time
    	def threshold = 1000 * 60 * minutes1 - 1000
    	if (elapsed >= threshold) {
            log.debug "Motion has stayed inactive long enough since last check ($elapsed ms):  turning lights off"
            switch1.off()
    	} else {
        	log.debug "Motion has not stayed inactive long enough since last check ($elapsed ms):  doing nothing"
        }
    } else {
    	log.debug "Motion is active, do nothing and wait for inactive"
    }
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