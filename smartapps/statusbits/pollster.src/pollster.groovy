/**
 *  Pollster - The SmartThings Polling Daemon.
 *
 *  Pollster works behind the scenes and periodically calls 'poll' or
 *  'refresh' commands for selected devices. Devices can be arranged into
 *  three polling groups with configurable polling intervals down to 1 minute.
 *
 *  Please visit [https://github.com/statusbits/smartthings] for more
 *  information. 
 *
 *  --------------------------------------------------------------------------
 *  Copyright © 2014 Statusbits.com
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may
 *  not use this file except in compliance with the License. You may obtain a
 *  copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *  WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 *  License  for the specific language governing permissions and limitations
 *  under the License.
 *  --------------------------------------------------------------------------
 *
 *  Version 1.5.0 (02/08/2016)
 */

definition(
    name: "Pollster",
    namespace: "statusbits",
    author: "geko@statusbits.com",
    description: "Poll or refresh device status periodically.",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")

preferences {
    section("About") {
        def hrefAbout = [
            url:        "http://statusbits.github.io/smartthings/",
            style:      "embedded",
            title:      "Tap for more information...",
            description:"http://statusbits.github.io/smartthings/",
            required:   false
        ]

        paragraph about()
        //href hrefAbout
    }

    (1..4).each() { n ->
        section("Scheduled Polling Group ${n}") {
            input "group_${n}", "capability.polling", title:"Select devices to be polled", multiple:true, required:false
            input "refresh_${n}", "capability.refresh", title:"Select devices to be refreshed", multiple:true, required:false
            input "interval_${n}", "number", title:"Set polling interval (in minutes)", defaultValue:5
        }
    }

    section("REST API Polling Group") {
        paragraph "Poll these devices via REST API endpoint."
        input "enableRestApi", "bool", title:"Enable REST endpoint", defaultValue:false
        input "restPoll", "capability.polling", title:"Select devices to be polled", multiple:true, required:false
        input "restRefresh", "capability.refresh", title:"Select devices to be refreshed", multiple:true, required:false
    }
}

mappings {
    path("/poll") {
        action: [ GET: "apiPoll" ]
    }
}

def installed() {
    initialize()
}

def updated() {
    initialize()
}

def onAppTouch(event) {
    LOG("onAppTouch(${event.value})")

    watchdog()
    pollingTask1()
    pollingTask2()
    pollingTask3()
    pollingTask4()

    if (settings.restPoll) {
        settings.restPoll*.poll()
    }

    if (settings.restRefresh) {
        settings.restRefresh*.refresh()
    }
}

def onLocation(event) {
    LOG("onLocation(${event.value})")

    watchdog()
}

def pollingTask1() {
    LOG("pollingTask1()")

    state.trun1 = now()

    if (settings.group_1) {
        settings.group_1*.poll()
    }

    if (settings.refresh_1) {
        settings.refresh_1*.refresh()
    }
}

def pollingTask2() {
    LOG("pollingTask2()")

    state.trun2 = now()

    if (settings.group_2) {
        settings.group_2*.poll()
    }

    if (settings.refresh_2) {
        settings.refresh_2*.refresh()
    }
}

def pollingTask3() {
    LOG("pollingTask3()")

    state.trun3 = now()

    if (settings.group_3) {
        settings.group_3*.poll()
    }

    if (settings.refresh_3) {
        settings.refresh_3*.refresh()
    }
}

def pollingTask4() {
    LOG("pollingTask4()")

    state.trun4 = now()

    if (settings.group_4) {
        settings.group_4*.poll()
    }

    if (settings.refresh_4) {
        settings.refresh_4*.refresh()
    }
}

// Handle '.../poll' REST endpoint
def apiPoll() {
    LOG("apiPoll()")

    if (settings.restPoll) {
        settings.restPoll*.poll()
    }

    if (settings.restRefresh) {
        settings.restRefresh*.refresh()
    }

    watchdog()

    return [status:"ok"]
}

def watchdog() {
    LOG("watchdog()")

    (1..4).each() { n ->
        def interval = settings."interval_${n}".toInteger()
        def trun = state."trun${n}"

        if (interval && trun && ((now() - trun) > ((interval + 10) * 60000))) {
            log.warn "Polling task #${n} stalled. Restarting..."
            restart()
            return
        }
    }
}

private def initialize() {
    log.info "Pollster. Version ${version()}. ${copyright()}"
    LOG("initialize() with settings: ${settings}")

    if (settings.enableRestApi && state.accessToken == null) {
        initAccessToken()
    }

    state.trun1 = 0
    state.trun2 = 0
    state.trun3 = 0
    state.trun4 = 0

    Random rand = new Random(now())
    def numTasks = 0
    (1..4).each() { n ->
        def minutes = settings."interval_${n}".toInteger()
        def seconds = rand.nextInt(60)
        def size1 = settings["group_${n}"]?.size() ?: 0
        def size2 = settings["refresh_${n}"]?.size() ?: 0

        safeUnschedule("pollingTask${n}")

        if (minutes > 0 && (size1 + size2) > 0) {
            LOG("Scheduling polling task ${n} to run every ${minutes} minutes.")
            def sched = "${seconds} 0/${minutes} * * * ?"
            schedule(sched, "pollingTask${n}")
            numTasks++
        }
    }

    if (numTasks) {
        subscribe(app, onAppTouch)
        subscribe(location, onLocation)
        subscribe(location, "position", onLocation)
        subscribe(location, "sunrise", onLocation)
        subscribe(location, "sunset", onLocation)
    }

    LOG("state: ${state}")
}

private def initAccessToken() {
    LOG("initAccessToken()")

    try {
        def token = createAccessToken()
        log.info "Created access token: ${token})"
        state.url = "https://graph.api.smartthings.com/api/smartapps/installations/${app.id}/poll?access_token=${token}"
    } catch (e) {
        log.error "Cannot create access token. ${e}"
        state.url = null
        return false
    }

    return true
}

private def safeUnschedule() {
    try {
        unschedule()
    }

    catch(e) {
        log.error ${e}
    }
}

private def safeUnschedule(handler) {
    try {
        unschedule(handler)
    }

    catch(e) {
        log.error ${e}
    }
}

private def restart() {
    //sendNotification("Pollster stalled. Restarting...")
    updated()
}

private def about() {
    def text =
        "Version ${version()}\n${copyright()}\n\n" +
        "You can contribute to the development of this app by making a " +
        "PayPal donation to geko@statusbits.com. We appreciate your support."
}

private def version() {
    return "Version 1.5.0"
}

private def copyright() {
    return "Copyright © 2014 Statusbits.com"
}

private def LOG(message) {
    log.trace message
}