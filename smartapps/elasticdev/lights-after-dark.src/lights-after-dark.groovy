/**
 *  Lights After dark
 *
 *  Copyright 2015 Elastic Development
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
 *  The latest version of this file can be found at:
 *  https://github.com/jpansarasa/SmartThings/blob/master/SmartApps/YetAnotherPowerMonitor.groovy
 *
 *  Revision History
 *  ----------------
 *
 *  2015-01-04: Version: 1.0.0
 *  Initial Revision
 *
 */
import groovy.time.*

definition(
    name: "Lights After Dark",
    namespace: "elasticdev",
    author: "James P",
    description: "Turns on the lights when people arrive after sunset but before sunrise",
    category: "Safety & Security",
    iconUrl: "http://cdn.device-icons.smartthings.com/Lighting/light9-icn.png",
    iconX2Url: "http://cdn.device-icons.smartthings.com/Lighting/light9-icn@2x.png",
    iconX3Url: "http://cdn.device-icons.smartthings.com/Lighting/light9-icn@2x.png")

preferences {
    section("About") {
        paragraph "Turns on the lights when people arrive after sunset but before sunrise."
    }

    section("When one of these people arrive at home"){
        input "presence", "capability.presenceSensor", title: "Who?", required: true, multiple: true
    }

    section("Turn on these lights") {
        input "lights", "capability.switch", multiple: true
        input "lightsDuration", "number", title: "Stay on for how many minutes?", description: "5", required: false
    }

	section ("Additionally", hidden: hideOptionsSection(), hideable: true) {
		input "falseAlarmThreshold", "decimal", title: "False alarm threshold (minutes)", required: false
    }
}

def installed()
{
    log.trace "Installed with settings: ${settings}"
    initialize()
}

def updated()
{
    log.trace "Updated with settings: ${settings}"
    unsubscribe()
    initialize()
}

/**
 *	Initialize the script
 *
 *	Set the inital state and subscribe to the events
 */
def initialize() {
	//Set initial state
    state.afterDark = false
    state.threshold = (falseAlarmThreshold) ? (falseAlarmThreshold * 60 * 1000) as Long : 5 * 60 * 1000L
    state.lightsDuration = (lightsDuration) ? (lightsDuration * 60) as Long : 0
    state.userOverride = lights.currentSwitch=="on"

    //Subscribe to sunrise and sunset events
    subscribe(location, "sunrise", sunriseHandler)
    subscribe(location, "sunset", sunsetHandler)
	subscribe(presence, "presence", presenceHandler)
    subscribe(lights, "switch", switchHandler)

    //Check if current time is after sunset
    def sunset = getSunriseAndSunset().sunset
    def now = new Date()
    if (sunset.before(now)) {
        state.afterDark = true
    }
    log.trace "state.afterDark: $state.afterDark"
    log.trace "state.threshold: $state.threshold"
    log.trace "state.lightsDuration: $state.lightsDuration"
    log.trace "state.userOverride: $state.userOverride"
}

/**
 *	Sunrise Handler
 *
 *	Sets the afterDark flag in the state to false
 *
 *	evt		The sunrise event
 */
def sunriseHandler(evt) {
	//log.debug "Sunrise evt: ${evt}, ${evt.value}"
    def sunriseTime = new Date()
    log.info "Sunrise at ${sunriseTime}"
    state.afterDark = false
}

/**
 *	Sunset Handler
 *
 *	Sets the afterDark flag in the state to true
 *
 *	evt		The sunset event
 */
def sunsetHandler(evt) {
	//log.debug "Sunset evt: ${evt}"
    def sunsetTime = new Date()
    log.info "Sunset at ${sunsetTime}"
    state.afterDark = true
}

/**
 *	Switch Handler
 *
 *	Sets the userOverride flag in the state to the switche's state
 *
 *	evt		The switch event
 */
def switchHandler(evt) {
	//log.debug "Lights evt: ${evt}"  
    state.userOverride = evt.value=="on"
    log.info "state.userOverride ${state.userOverride}"
}



/**
 *	Presence Handler
 *
 *	Called when a presence sensor changes state to present 
 *
 *	evt		The presence event
 */
def presenceHandler(evt)
{
	def person = presence.find{it.id == evt.deviceId}
    log.debug "$person is $evt.value!"

	if(!state.userOverride) {
        if(state.afterDark) {
            if("present" == evt.value) {
                def thresholdWindow = new Date(now() - state.threshold)

                def recentNotPresent = person.statesSince("presence", thresholdWindow).find{it.value == "not present"}
                if (recentNotPresent) {
                    log.debug "skipping notification of arrival of ${person.displayName} because last departure was only ${now() - recentNotPresent.date.time} msec ago"
                }
                else {
                    lights.on()
                    //If we turn off the lights after some period, set timer
                    if (state.lightsDuration>0) {
                        def delayInSeconds = lightsDuration * 60
                        //No need to guard against multiple arrivals, 
                        //since calling 'runIn' again cancels any existing scheduled event
                        log.trace "Turning off lights in $delayInSeconds secs"
                        runIn(delayInSeconds, lightsOffHandler)
                    }
                }
            }
        }
        else {
            log.debug "Not after dark - do nothing"
        }
    }
    else {
        log.debug "user Override - do nothing"
    }
}

/**
 *	Lights Off Handler
 *
 *	Called to turn the lights off
 *
 *	evt		The runIn event
 */
def lightsOffHandler(evt)
{
    log.debug "Automatic lights off"
    lights.off()
}

/**
 * Enables/Disables the optional section
 */
private hideOptionsSection() {
    (falseAlarmThreshold ) ? false : true
}
//EOF