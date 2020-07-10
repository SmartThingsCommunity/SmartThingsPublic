/**
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
 *  Sunrise, Sunset
 *
 *  Author: SmartThings
 *
 *  Date: 2013-04-30
 */
definition(
        name: "Sunrise/Sunset",
        namespace: "smartthings",
        author: "SmartThings",
        description: "Changes mode and controls lights based on local sunrise and sunset times.",
        category: "Mode Magic",
        iconUrl: "https://s3.amazonaws.com/smartapp-icons/ModeMagic/rise-and-shine.png",
        iconX2Url: "https://s3.amazonaws.com/smartapp-icons/ModeMagic/rise-and-shine@2x.png"
)

preferences {
    section ("At sunrise...") {
        input "sunriseMode", "mode", title: "Change mode to?", required: false
        input "sunriseOn", "capability.switch", title: "Turn on?", required: false, multiple: true
        input "sunriseOff", "capability.switch", title: "Turn off?", required: false, multiple: true
    }
    section ("At sunset...") {
        input "sunsetMode", "mode", title: "Change mode to?", required: false
        input "sunsetOn", "capability.switch", title: "Turn on?", required: false, multiple: true
        input "sunsetOff", "capability.switch", title: "Turn off?", required: false, multiple: true
    }
    section ("Sunrise offset (optional)...") {
        input "sunriseOffsetValue", "text", title: "HH:MM", required: false
        input "sunriseOffsetDir", "enum", title: "Before or After", required: false, options: ["Before","After"]
    }
    section ("Sunset offset (optional)...") {
        input "sunsetOffsetValue", "text", title: "HH:MM", required: false
        input "sunsetOffsetDir", "enum", title: "Before or After", required: false, options: ["Before","After"]
    }
    section ("Zip code (optional, defaults to location coordinates)...") {
        input "zipCode", "text", required: false
    }
    section( "Notifications" ) {
        input("recipients", "contact", title: "Send notifications to") {
            input "sendPushMessage", "enum", title: "Send a push notification?", options: ["Yes", "No"], required: false
            input "phoneNumber", "phone", title: "Send a text message?", required: false
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
    subscribe(location, "position", locationPositionChange)
    subscribe(location, "sunriseTime", sunriseTimeHandler)
    subscribe(location, "sunsetTime", sunsetTimeHandler)

    //Run today too
    scheduleWithOffset(location.currentValue("sunsetTime"), sunsetOffsetValue, sunsetOffsetDir, "sunsetHandler")
    scheduleWithOffset(location.currentValue("sunriseTime"), sunriseOffsetValue, sunriseOffsetDir, "sunriseHandler")
}

def locationPositionChange(evt) {
    log.trace "locationChange()"
    updated()
}

def sunsetTimeHandler(evt) {
    log.trace "sunsetTimeHandler()"
    scheduleWithOffset(evt.value, sunsetOffsetValue, sunsetOffsetDir, "sunsetHandler")
}

def sunriseTimeHandler(evt) {
    log.trace "sunriseTimeHandler()"
    scheduleWithOffset(evt.value, sunriseOffsetValue, sunriseOffsetDir, "sunriseHandler")
}

def scheduleWithOffset(nextSunriseSunsetTime, offset, offsetDir, handlerName) {
    def nextSunriseSunsetTimeDate = Date.parse("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", nextSunriseSunsetTime)
    def offsetTime = new Date(nextSunriseSunsetTimeDate.time + getOffset(offset, offsetDir))

    log.debug "scheduling Sunrise/Sunset for $offsetTime"
    runOnce(offsetTime, handlerName, [overwrite: false])
}

def sunriseHandler() {
    log.info "Executing sunrise handler"
    if (sunriseOn) {
        sunriseOn.on()
    }
    if (sunriseOff) {
        sunriseOff.off()
    }
    changeMode(sunriseMode)
}

def sunsetHandler() {
    log.info "Executing sunset handler"
    if (sunsetOn) {
        sunsetOn.on()
    }
    if (sunsetOff) {
        sunsetOff.off()
    }
    changeMode(sunsetMode)
}

def changeMode(newMode) {
    if (newMode && location.mode != newMode) {
        if (location.modes?.find{it.name == newMode}) {
            setLocationMode(newMode)
            send "${label} has changed the mode to '${newMode}'"
        }
        else {
            send "${label} tried to change to undefined mode '${newMode}'"
        }
    }
}

private send(msg) {
    if (location.contactBookEnabled) {
        log.debug("sending notifications to: ${recipients?.size()}")
        sendNotificationToContacts(msg, recipients)
    }
    else {
        if (sendPushMessage != "No") {
            log.debug("sending push message")
            sendPush(msg)
        }

        if (phoneNumber) {
            log.debug("sending text message")
            sendSms(phoneNumber, msg)
        }
    }

    log.debug msg
}

private getLabel() {
    app.label ?: "SmartThings"
}

private getOffset(String offsetValue, String offsetDir) {
    def timeOffsetMillis = calculateTimeOffsetMillis(offsetValue)
    if (offsetDir == "Before") {
        return -timeOffsetMillis
    }
    return timeOffsetMillis
}

private calculateTimeOffsetMillis(String offset) {
    def result = 0
    if (!offset) {
        return result
    }

    def before = offset.startsWith('-')
    if (before || offset.startsWith('+')) {
        offset = offset[1..-1]
    }

    if (offset.isNumber()) {
        result = Math.round((offset as Double) * 60000L)
    } else if (offset.contains(":")) {
        def segs = offset.split(":")
        result = (segs[0].toLong() * 3600000L) + (segs[1].toLong() * 60000L)
    }

    if (before) {
        result = -result
    }

    result
}