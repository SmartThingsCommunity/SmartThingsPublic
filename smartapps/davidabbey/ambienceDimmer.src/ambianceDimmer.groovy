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
        name: "ambianceDimmer",
        namespace: "davidabbey",
        author: "davidabbey",
        description: "Bring the lights down according to a schedule.",
        category: "My Apps",
        iconUrl: "https://s3.amazonaws.com/smartapp-icons/ModeMagic/rise-and-shine.png",
        iconX2Url: "https://s3.amazonaws.com/smartapp-icons/ModeMagic/rise-and-shine@2x.png",
        iconX3Url: "https://s3.amazonaws.com/smartapp-icons/ModeMagic/rise-and-shine@2x.png")

preferences {
    section("Lights") {
        input "Dimmers", "capability.dimmer", title: "Which lights to turn on?"
    }
    section("Sunset offset (optional)...") {
        input "sunsetOffsetValue", "text", title: "HH:MM", required: false
    }
}

def defaultStart() {
    if (usesOldSettings() && direction && direction == "Down") {
        return 99
    }
    return 0
}

def defaultEnd() {
    if (usesOldSettings() && direction && direction == "Down") {
        return 0
    }
    return 99
}


def installed() {
    initialize()
}

def updated() {
    unsubscribe()
    initialize()
}

def initialize() {
    dimmers.each { dimmer ->
        def curDimmerLevel = dimmer.currentLevel
        def nextDimmerLevel = curDimmerLevel + 3
        log.debug(currDimmerLevel+"::"+nextDimmerLevel)
        dimmer.setLevel(nextDimmerLevel)
        }
    }
/*
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


def setLevelsInState() {
    def startLevels = [:]
    dimmers.each { dimmer ->
        if (usesOldSettings()) {
            startLevels[dimmer.id] = defaultStart()
        } else if (hasStartLevel()) {
            startLevels[dimmer.id] = startLevel
        } else {
            def dimmerIsOff = dimmer.currentValue("switch") == "off"
            startLevels[dimmer.id] = dimmerIsOff ? 0 : dimmer.currentValue("level")
        }
    }

    atomicState.startLevels = startLevels
}


private getSunsetOffset() {
    //if there is an offset, make negative since we only care about before
    sunsetOffsetValue ? "-$sunsetOffsetValue" : null
}


private increment() {

    if (!atomicState.running) {
        return
    }

    def percentComplete = completionPercentage()

    if (percentComplete > 99) {
        percentComplete = 99
    }

    updateDimmers(percentComplete)

    if (percentComplete < 99) {

        def runAgain = stepDuration()
        log.debug "Rescheduling to run again in ${runAgain} seconds"

        runIn(runAgain, 'increment', [overwrite: true])

    } else {

        int completionDelay = completionDelaySeconds()
        if (completionDelay) {
            log.debug "Finished with steps. Scheduling completion for ${completionDelay} second(s) from now"
            runIn(completionDelay, 'completion', [overwrite: true])
            unschedule("healthCheck")
            // don't let the health check start incrementing again while we wait for the delayed execution of completion
        } else {
            log.debug "Finished with steps. Execution completion"
            completion()
        }

    }
}


def updateDimmers(percentComplete) {
    dimmers.each { dimmer ->

        def nextLevel = dynamicLevel(dimmer, percentComplete)

        if (nextLevel == 0) {

            dimmer.off()

        } else {

            def shouldChangeColors = (colorize && colorize != "false")
            def canChangeColors = hasSetColorCommand(dimmer)

            log.debug "Setting ${deviceLabel(dimmer)} to ${nextLevel}"

            if (shouldChangeColors && canChangeColors) {
                dimmer.setColor([hue: getHue(dimmer, nextLevel), saturation: 100, level: nextLevel])
            } else {
                dimmer.setLevel(nextLevel)
            }

        }
    }

    sendTimeRemainingEvent(percentComplete)
}
*/
