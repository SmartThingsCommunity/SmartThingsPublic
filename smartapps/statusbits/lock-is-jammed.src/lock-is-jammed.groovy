/**
 *  Remind to Lock.
 *
 *  This smart app monitors a door lock and sends push notifications, text
 *  messages and/or voice notifications if the door is left unlocked for
 *  longer than a specified period of time.
 *
 *  Version 1.0.2 (2014-12-06)
 *
 *  The latest version of this file can be found at:
 *  https://github.com/statusbits/smartthings/tree/master/RemindToLock
 *
 *  Credits:
 *  This program is partially based on the "Left It Unlocked" app written by
 *  Matt (@mattjfrank).
 *
 *  --------------------------------------------------------------------------
 *
 *  Copyright (c) 2014 Statusbits.com
 *
 *  This program is free software: you can redistribute it and/or modify it
 *  under the terms of the GNU General Public License as published by the Free
 *  Software Foundation, either version 3 of the License, or (at your option)
 *  any later version.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *  or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 *  for more details.
 *
 *  You should have received a copy of the GNU General Public License along
 *  with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

definition(
    name: "Lock is jammed",
    namespace: "statusbits",
    author: "geko@statusbits.com",
    description: "Notify me when a door is jammed.",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")

preferences {
    section {
        paragraph textAbout()
        paragraph "${textVersion()}\n${textCopyright()}"
    }

    section("Remind me when a lock is jammed") {
        input "lock", "capability.lock", title:"Which lock?"
        input "notifyAfter", "number", title:"If jammed for (minutes)", defaultValue:5
        input "repeatEvery", "number", title:"Repeat every (minutes)", defaultValue:5, required:false
        input "repeatLimit", "number", title:"But no more than (times)", defaultValue:3, required:false
    }

    section("Notification Options") {
        input "pushMessage", "bool", title:"Send push message", defaultValue:true
        //input "phoneNumber1", "phone", title:"Send text message to this number", required:false
        //input "phoneNumber2", "phone", title:"Send text message to this number", required:false
        //input "phoneNumber3", "phone", title:"Send text message to this number", required:false
        //input "speechDevice", "capability.speechSynthesis", title:"Speak using these devices", multiple:true, required:false
        input "customPhrase", "text", title:"Utter this phrase", description:"Leave this blank for a default phrase", required:false
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

private def initialize() {
    TRACE("initialize()")

    state.notifyMs = (settings.notifyAfter?.toInteger() ?: 0) * 60000
    state.repeatMs = (settings.repeatEvery?.toInteger() ?: 0) * 60000
    state.repeatLimit = settings.repeatLimit?.toInteger() ?: 0
    state.repeatCount = 0
    state.timeUnknown = 0

    if (lock && state.notifyMs > 0) {
        subscribe(lock, "lock.locked", onLocked)
        subscribe(lock, "lock.unknown", onUnknown)

        def lockState = lock.currentState("lock")
        if (lockState.value == "unknown") {
            scheduleTask()
        }
    }

    STATE()
}

private def scheduleTask() {
    TRACE("scheduleTask()")

    def currentTime = now()
    state.timeUnknown = currentTime
    state.repeatCount = 0
    runNotifyAt(currentTime + state.notifyMs)
}

def onLocked(evt) {
    TRACE("onLocked(${evt.displayName})")
    unschedule()
}

def onUnknown(evt) {
    TRACE("onUnknown(${evt.displayName})")

    if (state.notifyMs) {
        scheduleTask()
    }
}

def taskNotify() {
    TRACE("taskNotify()")

    def lockState = lock.currentState("lock")
    if (lockState.value == "locked") {
        log.trace "Hmm... The lock is already locked."
        return
    }

    def currentTime = now()
    def unknownFor = Math.round((currentTime - state.timeUnknown) / 60000)
    def message = "The ${lock.displayName} has been jammed for ${unknownFor} "
    if (unknown > 1) {
        message += "minutes."
    } else {
        message += "minute."
    }

    log.trace message

    if (settings.pushMessage) {
        sendPush(message)

    } else {
        sendNotificationEvent(message)
    }
	/*
    if (settings.phoneNumber1) {
        sendSms(settings.phoneNumber1, message)
    }

    if (settings.phoneNumber2) {
        sendSms(settings.phoneNumber2, message)
    }

    if (settings.phoneNumber3) {
        sendSms(settings.phoneNumber3, message)
    }

    if (settings.speechSynth) {
        if (settings.customPhrase) {
            message = settings.customPhrase
        }

        settings.speechSynth*.speak(message)
    }
	*/
    if (state.repeatMs) {
        // Re-schedule notification task
        if (state.repeatLimit) {
            def count = state.repeatCount + 1
            if (count >= state.repeatLimit) {
                return
            }

            state.repeatCount = count
        }

        runNotifyAt(currentTime + state.repeatMs)
    }
}

private def runNotifyAt(timeMs) {
    TRACE("runNotifyAt(${timeMs})")

    def date = new Date(timeMs)
    runOnce(date, taskNotify)
    log.trace "'taskNotify' scheduled to run at ${date}"
}

private def textAbout() {
    return "This smart app monitors a door lock and sends push " +
        "notifications, text messages and/or voice notifications if the " +
        "door is left jammed for longer than a specified period of time."
}

private def textCopyright() {
    return "Copyright (c) 2014 Statusbits.com"
}

private def textVersion() {
    return "Version 1.0.2"
}

private def TRACE(message) {
    //log.debug message
}

private def STATE() {
    //log.debug "settings: ${settings}"
    //log.debug "state: ${state}"
}