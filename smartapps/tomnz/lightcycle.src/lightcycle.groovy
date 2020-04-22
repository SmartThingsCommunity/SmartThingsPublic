/**
 *  LightCycle
 *
 *  Copyright 2016 Tom Mitchell
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
    name: "LightCycle",
    namespace: "TomNZ",
    author: "Tom Mitchell",
    description: "Set up a daily cycle for a given set of color temperature lights. Specify brightness and temperature for various times throughout the day, and LightCycle will smoothly transition between those parameters at a regular interval (or when the lights are turned on). If you override one of the parameters, then LightCycle will stop updating it until the next time the light is turned on.",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Categories/lightsAndSwitches.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Categories/lightsAndSwitches@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Categories/lightsAndSwitches@2x.png")


preferences {
    page(name: "controls", title: "Controls", nextPage: "waypoints", uninstall: true) {
        section {
            input "bulbs", "capability.colorTemperature", title: "Select Lights", multiple:true, required: true
            input "period", "number", title: "Update interval (mins)", description: "Once the light has been turned on, how often should LightCycle update it?", required: true, defaultValue: 10, range: "1..30"
        }
    }
    page(name: "waypoints", title: "Waypoints", install: true, uninstall: true) {
        section {
            paragraph "A waypoint defines what brightness and temperature the lights should be at a given time. In practice, both values are interpolated between the waypoints before/after the current time. You must set at least one waypoint."
        }
        section("Waypoint A") {
            input "aTime", "time", title: "Time", required: true
            input "aLevel", "number", title: "Brightness (%)", description: "1-100 - whole numbers only", range: "1..100", required: true
            input "aTemp", "number", title: "Temp (K)", description: "2500-9000K - check what temperatures your model of light supports", range: "2500..9000", required: true
        }
        section("Waypoint B") {
            input "bTime", "time", title: "Time", required: false
            input "bLevel", "number", title: "Brightness (%)", range: "1..100", required: false
            input "bTemp", "number", title: "Temp (K)", range: "2500..9000", required: false
        }
        section("Waypoint C") {
            input "cTime", "time", title: "Time", required: false
            input "cLevel", "number", title: "Brightness (%)", range: "1..100", required: false
            input "cTemp", "number", title: "Temp (K)", range: "2500..9000", required: false
        }
        section("Waypoint D") {
            input "dTime", "time", title: "Time", required: false
            input "dLevel", "number", title: "Brightness (%)", range: "1..100", required: false
            input "dTemp", "number", title: "Temp (K)", range: "2500..9000", required: false
        }
        section("Waypoint E") {
            input "eTime", "time", title: "Time", required: false
            input "eLevel", "number", title: "Brightness (%)", range: "1..100", required: false
            input "eTemp", "number", title: "Temp (K)", range: "2500..9000", required: false
        }
        section("Waypoint F") {
            input "fTime", "time", title: "Time", required: false
            input "fLevel", "number", title: "Brightness (%)", range: "1..100", required: false
            input "fTemp", "number", title: "Temp (K)", range: "2500..9000", required: false
        }
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
    // Subscribe to all of our bulbs being turned on
    subscribe(bulbs, "switch.on", startCycle)
    // Also subscribe to them being switched off - once all
    // are switched off, then we can set a status flag as such
    subscribe(bulbs, "switch.off", stopCycle)

    // Set some state so we don't get any errors
    state.running = false
    state.level = 0
    state.temp = 0
}

// Helper methods
def isOn(bulb) {
    return bulb.currentValue("switch") == "on"
}

def getLevel(bulb) {
    // Level (0-100) is actually a decimal, but we just go to the
    // nearest integer
    return (int)(bulb.currentValue("level"))
}

def getTemp(bulb) {
    return (int)(bulb.currentValue("colorTemperature"))
}

def getTimeCal(time, parse) {
    // Given an input time, get a Calendar object for that time,
    // with the date set to an arbitrary fixed value. This allows
    // us to compare and consider time values without caring about
    // the date.
    def cal = Calendar.getInstance()
    if (parse) {
        // Parse flag means time is a string value
        time = timeToday(time)
        cal.setTime(time)
    } else {
        // No parse flag means epoch time in ms
        cal.setTimeInMillis(time)
    }
    // Arbitrary date
    cal.set(Calendar.DAY_OF_MONTH, 1)
    cal.set(Calendar.MONTH, 1)
    cal.set(Calendar.YEAR, 2000)
    return cal
}

def getNewValues(time) {
    // Return the level and temp for a given time
    // Kind of a pain with the way parameters work - this is gonna be
    // long :(

    // Get the Calendar object for current time - will be useful later
    def timeCal = getTimeCal(time, false)

    // Gather all the valid waypoint times/values - Waypoint A will
    // always be set
    def aCal = getTimeCal(aTime, true)
    def sortedTimes = [aCal]
    def vals = [dummy: 'val']
    vals[aCal.getTimeInMillis()] = [aLevel, aTemp]
    if (bTime != null) {
        def bCal = getTimeCal(bTime, true)
        sortedTimes << bCal
        vals[bCal.getTimeInMillis()] = [bLevel, bTemp]
    }
    if (cTime != null) {
        def cCal = getTimeCal(cTime, true)
        sortedTimes << cCal
        vals[cCal.getTimeInMillis()] = [cLevel, cTemp]
    }
    if (dTime != null) {
        def dCal = getTimeCal(dTime, true)
        sortedTimes << dCal
        vals[dCal.getTimeInMillis()] = [dLevel, dTemp]
    }
    if (eTime != null) {
        def eCal = getTimeCal(eTime, true)
        sortedTimes << eCal
        vals[eCal.getTimeInMillis()] = [eLevel, eTemp]
    }
    if (fTime != null) {
        def fCal = getTimeCal(fTime, true)
        sortedTimes << fCal
        vals[fCal.getTimeInMillis()] = [fLevel, fTemp]
    }

    // Determine the waypoint chronologically before the current time. e.g.
    // Waypoints:
    //   - 10am
    //   - 6pm
    // If current time is 11am. First set prevTime to 6:00pm (latest timestamp),
    // then work through from earliest to latest time. If the time is earlier
    // than the current time, then take that instead. (i.e. take 10am), but as
    // soon as we go past the current time (e.g. 6pm), then we exit out. If
    // instead it was currently 9am, then none of the times would be earlier
    // than the current time, so we'd instead want the LATEST time from the
    // day before - which is set in the beginning.
    //
    // This is somewhat confusing, but it works. We need to take note of if the
    // time was rolled over from the previous day, so we can account for that
    // later in the value computation.
    Collections.sort(sortedTimes)
    def prevTime = sortedTimes[-1]
    def prevRollover = true
    for (candidateTime in sortedTimes) {
        if (candidateTime.before(timeCal)) {
            prevTime = candidateTime
            prevRollover = false
        } else {
            break
        }
    }

    // Basically perform the reverse of above, to find the time immediately
    // following the current time chronologically.
    Collections.reverse(sortedTimes)
    def nextTime = sortedTimes[-1]
    def nextRollover = true
    for (candidateTime in sortedTimes) {
        if (candidateTime.after(timeCal)) {
            nextTime = candidateTime
            nextRollover = false
        } else {
            break
        }
    }

    log.debug "LightCycle - prev time: ${prevTime.getTime()}, rollover: ${prevRollover}"
    log.debug "LightCycle - curr time: ${timeCal.getTime()}"
    log.debug "LightCycle - next time: ${nextTime.getTime()}, rollover: ${nextRollover}"

    // Use the dictionary lookups we built before to extract the
    // level/temp values for the given waypoints
    def prevVals = vals[prevTime.getTimeInMillis()]
    def nextVals = vals[nextTime.getTimeInMillis()]
    def prevLevel = prevVals[0]
    def nextLevel = nextVals[0]
    def prevTemp = prevVals[1]
    def nextTemp = nextVals[1]

    def newLevel = 0
    def newTemp = 0

    if (prevTime.equals(nextTime)) {
        // Only one time point
        newLevel = prevLevel
        newTemp = prevTemp
        log.debug "LightCycle - using a single time point"

    } else {
        // Determine how far we've progressed between prev and next
        // We make some adjustments for rollover times - if it's rolled over from the
        // previous day, we need to subtract a day from it, and vice versa
        if (prevRollover) {
            prevTime.add(Calendar.DAY_OF_MONTH, -1)
        }
        if (nextRollover) {
            nextTime.add(Calendar.DAY_OF_MONTH, 1)
        }

        def prevFrac = (float)(timeCal.getTimeInMillis()/1000 - prevTime.getTimeInMillis()/1000) / (float)(nextTime.getTimeInMillis()/1000 - prevTime.getTimeInMillis()/1000)

        log.debug "LightCycle - frac prev -> next ${prevFrac}"
        log.debug "LightCycle - level prev: ${prevLevel}, next: ${nextLevel}"
        log.debug "LightCycle - temp prev: ${prevTemp}, next: ${nextTemp}"

        // Simple linear interpolation
        // Round to integers for output
        newLevel = (int)(prevLevel * (1 - prevFrac) + nextLevel * prevFrac)
        newTemp = (int)(prevTemp * (1 - prevFrac) + nextTemp * prevFrac)
    }

    // Round to integers for output
    def result = [level: newLevel, temp: newTemp]
    log.debug "LightCycle - new level: ${newLevel}, temp: ${newTemp}"

    return result
}

def startCycle(event) {
    // Called when any light we are interested is turned on. If we're
    // not running yet, then this is the signal to start up. If we're
    // already running, then set that light to whatever the other lights
    // are already on.

    // TODO: Having multiple lights turning on simultaneously can result in
    // each one doing the setup routine. Might be worth using atomicState
    // to perform rudimentary locking if this becomes an issue in future.

    if (state.running) {
        // Already running - just set the values to the ones stored in state
        event.device.setLevel(state.level)
        event.device.setColorTemperature(state.temp)
    } else {
        // Not running yet! We are lighting the torch so to speak...
        // Figure out what the level/temp should be
        def newValues = getNewValues(now())

        // Set them on the current light
        event.device.setLevel(newValues["level"])
        event.device.setColorTemperature(newValues["temp"])

        // Store it
        state.running = true
        state.level = newValues["level"]
        state.temp = newValues["temp"]

        log.debug "LightCycle - running!"

        // Kick off the timer to get this baby purring
        runIn(60 * period, tickCycle)
    }
}

def stopCycle(event) {
    // Called when any light we are interested in is turned off
    // This has no effect, unless ALL of the lights are now turned
    // off, then we set a status flag to indicate LightCycle is no
    // longer running.

    if (!state.running) {
        return
    }

    for (bulb in bulbs) {
        if (isOn(bulb)) {
            // At least one bulb is on, so this is a no-op
            return
        }
    }

    // If we reached this point, then everything is off
    state.running = false
    log.debug "LightCycle - going to sleep"

    // Unschedule is currently expensive according to the docs, and not
    // necessary based on our use of state... We may want to uncomment this
    // in future though:
    // unschedule()
}

def tickCycle(event) {
    // Called after each time duration has elapsed
    // We should find out what the next level/temp needs to be,
    // then update any settings that haven't been overriden.

    // It's possible that all of the lights were switched off
    // since we last ran - if so, just exit out without scheduling
    // another tick
    if (!state.running) {
        return
    }

    log.debug "LightCycle - ticking cycle"

    // Get next level/temp
    def newValues = getNewValues(now())

    def settingChanged = false

    // Update levels
    for (bulb in bulbs) {
        if (isOn(bulb) && getLevel(bulb).equals(state.level)) {
            bulb.setLevel(newValues["level"])
            settingChanged = true
        }
    }

    // Update temps
    for (bulb in bulbs) {
        if (isOn(bulb) && getTemp(bulb).equals(state.temp)) {
            bulb.setColorTemperature(newValues["temp"])
            settingChanged = true
        }
    }

    // If nothing was updated, then we've been overruled - go dormant til
    // something switches back on
    if (!settingChanged) {
        state.running = false
        log.debug "LightCycle - going to sleep"
        return
    }

    // Store state for next time
    state.level = newValues["level"]
    state.temp = newValues["temp"]

    // Schedule the next tick
    runIn(60 * period, tickCycle)
}
