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
        name: "Start Bringing lights up nn minutes before sunset",
        namespace: "examples",
        author: "SmartThings",
        description: "Turn on lights based on ZIP code",
        category: "My Apps",
        iconUrl: "https://s3.amazonaws.com/smartapp-icons/ModeMagic/rise-and-shine.png",
        iconX2Url: "https://s3.amazonaws.com/smartapp-icons/ModeMagic/rise-and-shine@2x.png",
        iconX3Url: "https://s3.amazonaws.com/smartapp-icons/ModeMagic/rise-and-shine@2x.png")

preferences {
    section("Lights") {
        input "Lights", "capability.dimmers", title: "Which lights to turn on?"
    }
    section("Sunset offset (optional)...") {
        input "sunsetOffsetValue", "text", title: "HH:MM", required: false
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
    // for each dimmer
    //      get diff from current level and 99
    //      divide time left by diff
    //      set level to current + diff

    /*
    schedule to run at the next Sunset.
    At next sunset
        Get light level; set to start level.(end level is 99)
        set current level to light level
        schedule to run every minute
        while current level < end level
            set the light level = current + increment
            set current = light level
        unschedule
        reschedule tp run at next sunset.
    */
    astroCheck()
}

def sunriseSunsetTimeHandler(evt) {
    state.lastSunriseSunsetEvent = now()
    log.debug "SmartNightlight.sunriseSunsetTimeHandler($app.id)"
    astroCheck()
}

/*
def sunsetHandler() {
    dimmers.each { dimmer ->

        log.debug "turning on lights"
        switches.on()

        // schedule for tomorrow
        scheduleNextSunset(new Date() + 1)
    }
}
*/
def astroCheck() {
    def s = getSunriseAndSunset(zipCode: zipCode, sunriseOffset: sunriseOffset, sunsetOffset: sunsetOffset)
    state.riseTime = s.sunrise.time
    state.setTime = s.sunset.time
    log.debug "rise: ${new Date(state.riseTime)}($state.riseTime), set: ${new Date(state.setTime)}($state.setTime)"
}


/*
preferences {
    section("Lights") {
        input "switches", "capability.switch", title: "Which lights to turn on?"
        input "offset", "number", title: "Turn on this many minutes before sunset"
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
    subscribe(location, "sunsetTime", sunsetTimeHandler)

    //schedule it to run today too
    scheduleTurnOn(location.currentValue("sunsetTime"))
}

def sunsetTimeHandler(evt) {
    //when I find out the sunset time, schedule the lights to turn on with an offset
    scheduleTurnOn(evt.value)
}

def scheduleTurnOn(sunsetString) {
    //get the Date value for the string
    def sunsetTime = Date.parse("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", sunsetString)

    //calculate the offset
    def timeBeforeSunset = new Date(sunsetTime.time - (offset * 60 * 1000))

    log.debug "Scheduling for: $timeBeforeSunset (sunset is $sunsetTime)"

    //schedule this to run one time
    runOnce(timeBeforeSunset, turnOn)


}

def turnOn() {


    Lights.each { light ->
        def curDimmerLevel = light.currentLevel
        def nextDimmerLevel = curDimmerLevel + 3
        light.setLevel(nextDimmerLevel)
        }
    }


    log.debug "turning on lights"
    check if ligh is on or not
    get the level of the lights....
    set the level to the current plus the increment
    log message
    runIn(60, turnOn)
    when light is fully on
    unsubscribe
    subscribe(location, "sunsetTime", sunsetTimeHandler)




}

 */
