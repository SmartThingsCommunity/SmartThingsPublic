/**
 *  Nobody Home
 *
 *  Author: brian@bevey.org, raychi@gmail.com
 *  Date: 12/02/2015
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an "AS
 *  IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *  express or implied. See the License for the specific language
 *  governing permissions and limitations under the License.
 */

/**
 *  Monitors a set of presence sensors and trigger appropriate mode
 *  based on configured modes and sunrise/sunset time.
 *
 *  - When everyone is away [Away]
 *  - When someone is home during the day [Home]
 *  - When someone is home at the night [Night]
 */

// ********** App related functions **********

// The definition provides metadata about the App to SmartThings.
definition (
    name:        "Nobody Home",
    namespace:   "imbrianj",
    author:      "brian@bevey.org",
    description: "Automatically set Away/Home/Night mode based on a set of presence sensors and sunrise/sunset time.",
    category:    "Mode Magic",
    iconUrl:     "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url:   "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url:   "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png"
)

// The preferences defines information the App needs from the user.
preferences {
    section("Presence sensors to monitor") {
        input "people", "capability.presenceSensor", multiple: true
    }

    section("Mode setting") {
        input "newAwayMode",    "mode", title: "Everyone is away"
        input "newSunriseMode", "mode", title: "Someone is home during the day"
        input "newSunsetMode",  "mode", title: "Someone is home at night"
    }

    section("Mode change delay (minutes)") {
        input "awayThreshold", "decimal", title: "Away delay [5m]", required: false
        input "arrivalThreshold", "decimal", title: "Arrival delay [2m]", required: false
    }

    section("Notifications") {
        input "sendPushMessage", "bool", title: "Push notification", required:false
    }
}

// called when the user installs the App
def installed()
{
    log.debug("installed() @${location.name}: ${settings}")
    initialize(true)
}

// called when the user installs the app, or changes the App
// preference
def updated()
{
    log.debug("updated() @${location.name}: ${settings}")
    unsubscribe()
    initialize(false)
}

def initialize(isInstall)
{
    // subscribe to all the events we care about
    log.debug("Subscribing to events ...")

    // thing to subscribe, attribute/state we care about, and callback fn
    subscribe(people,   "presence", presenceHandler)
    subscribe(location, "sunrise",  sunriseHandler)
    subscribe(location, "sunset",   sunsetHandler)

    // set the optional parameter values. these are not available
    // directly until the app has initialized (that is,
    // installed/updated has returned). so here we access them through
    // the settings object, as otherwise will get an exception.

    // store information we need in state object so we can get access
    // to it later in our event handlers.

    // calculate the away threshold in seconds. can't use the simpler
    // default falsy value, as value of 0 (no delay) is evaluated to
    // false (not specified), but we want 0 to represent no delay. so
    // we compare against null explicitly to see if the user has set a
    // value or not.
    if (settings.awayThreshold == null) {
        settings.awayThreshold = 5  // default away 5 minute
    }
    state.awayDelay = (int) settings.awayThreshold * 60
    log.debug("awayThreshold set to " + state.awayDelay + " second(s)")

    if (settings.arrivalThreshold == null) {
        settings.arrivalThreshold = 2  // default arrival 2 minute
    }
    state.arrivalDelay = (int) settings.arrivalThreshold * 60
    log.debug("arrivalThreshold set to " + state.arrivalDelay + " second(s)")

    // get push notification setting
    state.isPush = settings.sendPushMessage ? true : false
    log.debug("sendPushMessage set to " + state.isPush)

    // on install (not update), figure out what mode we should be in
    // IF someone's home. This value is needed so that when a presence
    // sensor is triggered, we know what mode to set the system to, as
    // the sunrise/sunset event handler may not be triggered yet after
    // a fresh install.
    if (isInstall) {
        // TODO: for now, we simply assume daytime. a better approach
        //       would be to figure out whether current time is day or
        //       night, and set it appropriately. However there
        //       doesn't seem to be a way to query this directly
        //       without a zip code. This will become the correct
        //       value at the next sunrise/sunset event.
        log.debug("No sun info yet, assuming daytime")
        state.modeIfHome = newSunriseMode

        // set keep a separate sun mode state so we can show a more
        // helpful message when sun events are triggered.
        state.currentSunMode = "sunUnknown"

        state.eventDevice = ""  // last event device

        // device that triggered timer. This is not necessarily the
        // eventDevice. For example, if A arrives, kick off timer,
        // then b arrives before timer elapsed, we want the
        // notification message to reference A, not B.
        state.timerDevice = null

        // anything in flight? We use this to avoid scheduling
        // duplicate timers (so we don't extend the timer).
        state.pendingOp = "init"

        // now set the correct mode for the location. This way, we
        // don't need to wait for the next sun/presence event.

        // we schedule this action to run after the app has fully
        // initialized. This way, the app install is faster and the
        // user customized app name is used in the notification.
        runIn(7, "setInitialMode")
    }
    // On update, we don't change state.modeIfHome. This is so that we
    // preserve the current sun rise/set state we obtained in earlier
    // sunset/sunrise handler. This way the app remains in the correct
    // sun state when the user reconfigures it.
}

def setInitialMode()
{
    changeSunMode(state.modeIfHome)
    state.pendingOp = null
}

// ********** sunrise/sunset handling **********

// event handler when the sunrise time is reached
def sunriseHandler(evt)
{
    // we store the mode we should be in, IF someone's home
    state.modeIfHome = newSunriseMode
    state.currentSunMode = "sunRise"

    // change mode if someone's home, otherwise set to away
    changeSunMode(newSunriseMode)
}

// event handler when the sunset time is reached
def sunsetHandler(evt)
{
    // we store the mode we should be in, IF someone's home
    state.modeIfHome = newSunsetMode
    state.currentSunMode = "sunSet"

    // change mode if someone's home, otherwise set to away
    changeSunMode(newSunsetMode)
}

def changeSunMode(newMode)
{
    // if everyone is away, we need to check and ensure the system is
    // in away mode.
    if (isEveryoneAway()) {
        // this shouldn not happen normally as the mode should already
        // be changed during presenceHandler, but in case this is not
        // done, such as when app is initially installed while away,
        // and system is not in away mode, then we toggle it to away
        // at the sun rise/set event.
        changeMode(newAwayMode, " because no one is present")
    } else {
        // someone is home, we update the mode depending on
        // sunrise/sunset.
        if (state.currentSunMode == "sunRise") {
            changeMode(newMode, " because it's sunrise")
        } else if (state.currentSunMode == "sunSet") {
            changeMode(newMode, " because it's sunset")
        } else {
            changeMode(newMode)
        }
    }
}

// ********** presence handling **********

// event handler when presence sensor changes state
def presenceHandler(evt)
{
    // get the device name that resulted in the change
    state.eventDevice= evt.device?.displayName

    // is setInitialMode() still pending?
    if (state.pendingOp == "init") {
        log.debug("Pending ${state.pendingOp} op still in progress, ignoring presence event")
        return
    }

    if (evt.value == "not present") {
        handleDeparture()
    } else {
        handleArrival()
    }
}

def handleDeparture()
{
    log.info("${state.eventDevice} left ${location.name}")

    // do nothing if someone's still home
    if (!isEveryoneAway()) {
        log.info("Someone is still present, no actions needed")
        return
    }

    // Now we set away mode. We perform the following actions even if
    // home is already in away mode because an arrival timer may be
    // pending, and scheduling delaySetMode() has the nice effect of
    // canceling any previous pending timer, which is what we want to
    // do. So we do this even if delay is 0.
    log.info("Scheduling ${newAwayMode} mode in " + state.awayDelay + "s")
    state.pendingOp = "away"
    state.timerDevice = state.eventDevice
    // we always use runIn(). This has the benefit of automatically
    // replacing any pending arrival/away timer. if any arrival timer
    // is active, it will be clobbered with this away timer. If any
    // away timer is active, it will be extended with this new timeout
    // (though normally it should not happen)
    runIn(state.awayDelay, "delaySetMode")
}

def handleArrival()
{
    // someone returned home, set home/night mode after delay
    log.info("${state.eventDevice} arrived at ${location.name}")

    def numHome = isAnyoneHome()
    if (!numHome) {
        // no one home, do nothing for now (should NOT happen)
        log.warn("${deviceName} arrived, but isAnyoneHome() returned false!")
        return
    }

    if (numHome > 1) {
        // not the first one home, do nothing, as any action that
        // should happen would've happened when the first sensor
        // arrived. this is the opposite of isEveryoneAway() where we
        // don't do anything if someone's still home.
        log.debug("Someone is already present, no actions needed")
        return
    }

    // check if any pending arrival timer is already active. we want
    // the timer to trigger when the 1st person arrives, but not
    // extended when the 2nd person arrives later. this should not
    // happen because of the >1 check above, but just in case.
    if (state.pendingOp == "arrive") {
        log.debug("Pending ${state.pendingOp} op already in progress, do nothing")
        return
    }

    // now we set home/night mode
    log.info("Scheduling ${state.modeIfHome} mode in " + state.arrivalDelay + "s")
    state.pendingOp = "arrive"
    state.timerDevice = state.eventDevice
    // if any away timer is active, it will be clobbered with
    // this arrival timer
    runIn(state.arrivalDelay, "delaySetMode")
}


// ********** helper functions **********

// change the system to the new mode, unless its already in that mode.
def changeMode(newMode, reason="")
{
    if (location.mode != newMode) {
        // notification message
        def message = "${location.name} changed mode from '${location.mode}' to '${newMode}'" + reason
        setLocationMode(newMode)
        send(message)  // send message after changing mode
    } else {
        log.debug("${location.name} is already in ${newMode} mode, no actions needed")
    }
}

// create a useful departure/arrival reason string
def reasonStr(isAway, delaySec, delayMin)
{
    def reason

    // if we are invoked by timer, use the stored timer trigger
    // device, otherwise use the last event device
    if (state.timerDevice) {
        reason = " because ${state.timerDevice} "
    } else {
        reason = " because ${state.eventDevice} "
    }

    if (isAway) {
        reason += "left"
    } else {
        reason += "arrived"
    }

    if (delaySec) {
        if (delaySec > 60) {
            if (delayMin == null) {
                delayMin = (int) delaySec / 60
            }
            reason += " ${delayMin} minutes ago"
        } else {
            reason += " ${delaySec}s ago"
        }
    }

    return reason
}

// http://docs.smartthings.com/en/latest/smartapp-developers-guide/scheduling.html#schedule-from-now
//
// By default, if a method is scheduled to run in the future, and then
// another call to runIn with the same method is made, the last one
// overwrites the previously scheduled method.
//
// We use the above property to schedule our arrval/departure delay
// using the same function so we don't have to worry about
// arrival/departure timer firing independently and complicating code.
def delaySetMode()
{
    def newMode = null
    def reason = ""

    // timer has elapsed, check presence status to figure out what we
    // need to do
    if (isEveryoneAway()) {
        reason = reasonStr(true, state.awayDelay, awayThreshold)
        newMode = newAwayMode
        if (state.pendingOp) {
            log.debug("${state.pendingOp} timer elapsed: everyone is away")
        }
    } else {
        reason = reasonStr(false, state.arrivalDelay, arrivalThreshold)
        newMode = state.modeIfHome
        if (state.pendingOp) {
            log.debug("${state.pendingOp} timer elapsed: someone is home")
        }
    }

    // now change the mode
    changeMode(newMode, reason);

    state.pendingOp = null
    state.timerDevice = null
}

private isEveryoneAway()
{
    def result = true

    if (people.findAll { it?.currentPresence == "present" }) {
        result = false
    }
    // log.debug("isEveryoneAway: ${result}")

    return result
}

// return the number of people that are home
private isAnyoneHome()
{
    def result = 0
    // iterate over our people variable that we defined
    // in the preferences method
    for (person in people) {
        if (person.currentPresence == "present") {
            result++
        }
    }
    return result
}

private send(msg)
{
    if (state.isPush) {
        log.debug("Sending push notification")
        sendPush(msg)
    } else {
        log.debug("Sending notification")
        sendNotificationEvent(msg)
    }
    log.info(msg)
}