/**
 *  Turn on by ZIP code
 *
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
 */
definition(
        name: "Turn on by ZIP code",
        namespace: "examples",
        author: "SmartThings",
        description: "Turn on lights based on ZIP code",
        category: "My Apps",
        iconUrl: "https://s3.amazonaws.com/smartapp-icons/ModeMagic/rise-and-shine.png",
        iconX2Url: "https://s3.amazonaws.com/smartapp-icons/ModeMagic/rise-and-shine@2x.png",
        iconX3Url: "https://s3.amazonaws.com/smartapp-icons/ModeMagic/rise-and-shine@2x.png")

preferences {
    section("Lights") {
        input "switches", "capability.switch", title: "Which lights to turn on?"
    }
    section("Sunset offset (optional)...") {
        input "sunsetOffsetValue", "text", title: "HH:MM", required: false
    }
    section("Zip code") {
        input "zipCode", "text", required: false
    }
}

def installed() {
    initialize()
}

def updated() {
    unsubscribe()
    initialize()
}

def initialize() {
    scheduleNextSunset()
}

def scheduleNextSunset(date = null) {
    def s = getSunriseAndSunset(zipCode: zipCode, sunsetOffset: getSunsetOffset(), date: date)
    def now = new Date()
    def setTime = s.sunset
    log.debug "setTime: $setTime"

    // use state to keep track of sunset times between executions
    // if sunset time has changed, unschedule and reschedule handler with updated time
    if(state.setTime != setTime.time) {
        unschedule("sunsetHandler")

        if(setTime.before(now)) {
            setTime = setTime.next()
        }

        state.setTime = setTime.time

        log.info "scheduling sunset handler for $setTime"
        schedule(setTime, sunsetHandler)
    }
}

def sunsetHandler() {
    log.debug "turning on lights"
    switches.on()

    // schedule for tomorrow
    scheduleNextSunset(new Date() + 1)
}

private getSunsetOffset() {
    //if there is an offset, make negative since we only care about before
    sunsetOffsetValue ? "-$sunsetOffsetValue" : null
}