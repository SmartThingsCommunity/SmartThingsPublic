/**
 *  Verisure integration for Smartthings platform
 *
 *  Copyright 2017 Anders Sveen & Martin Carlsson
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
 * --------------------------------------------------------------------------------------------------------------------
 *
 *  Huge props to Per Sandström who's made the python library for Verisure integration which this is based heavily on.
 *  This would not be possible without it. See: https://github.com/persandstrom/python-verisure
 *
 *  CHANGE LOG
 *  - 0.1   - Added option for Splunk logging
 *  - 0.1.1 - Removed option to set update frequency. Need to use cron to have realiable updates, and it won't poll faster than each minute.
 *  - 0.2   - Added support to grab climate data.
 *  - 0.2.1 - Fixed issue with parsing numbers for climate data.
 *  - 0.2.2 - Cleaned up some code
 *  - 0.2.3 - Added enable/disable option
 *  - 0.5   - Changed to using API servers and async http requests. Great improvements for stability.
 *  - 0.5.1 - Added throttle detection and postponing of updates
 *  - 0.5.2 - Option to disable remote logging, small cleanups and renamed unarmed to match real status of disarmed
 *  - 0.5.3 - Fixed error with ARMED_AWAY state
 *  - 0.5.4 - Added automatic switching of API url when Verisure switches servers
 *
 * NOTES (please read these):
 *
 * - WARNING: Your credentials are no longer checked on first login. WATCH THE LOGS AFTER INSTALLATION for errors.
 *
 * - Some errors still occur. There are intermittent:
 *      - groovyx.net.http.HttpResponseException: Unauthorized
 *      - "Request limit has been reached" - In this event the app will wait a couple of minutes before trying again.
 *
 * - If you have several locations in Verisure this app will currently not work. Sorry, let me know if you need it. :)
 *
 * - For some reason not all log entries show up in my console. It does get logged because I get them on the remote Splunk server.
 *
 *
 * Version: 0.5.2
 *
 */
definition(
        name: "Verisure",
        namespace: "smartthings.f12.no",
        author: "Anders Sveen & Martin Carlsson",
        description: "Lets you trigger automations whenever your Verisure alarm changes state or temperature changes.",
        category: "Safety & Security",
        iconUrl: "https://pbs.twimg.com/profile_images/448742746266677248/8RSgcRVz.jpeg",
        iconX2Url: "https://pbs.twimg.com/profile_images/448742746266677248/8RSgcRVz.jpeg",
        iconX3Url: "https://pbs.twimg.com/profile_images/448742746266677248/8RSgcRVz.jpeg") {
}


preferences {
    page(name: "setupPage")
}

include 'asynchttp_v1'

def setupPage() {
    dynamicPage(name: "setupPage", title: "Configure Verisure", install: true, uninstall: true) {

        section("Disable updating here") {
            input "enabled", "bool", defaultValue: "true", title: "Enabled?"
        }

        section("Authentication") {
            input "username", "text", title: "Username"
            input "password", "password", title: "Password"
        }

        def actions = location.helloHome?.getPhrases()*.label
        actions.sort()

        section("Action when disarmed") {
            input "disarmedAction", "enum", title: "Action for unarmed", options: actions, required: false
            input "armedAction", "enum", title: "Action for armed", options: actions, required: false
            input "armedHomeAction", "enum", title: "Action for armed home", options: actions, required: false
        }

        section("Remote logging?") {
            input "remoteLogEnabled", "bool", defaultValue: "true", title: "Enabled?", required: false
            input "logUrl", "text", title: "Splunk URL to log to", required: false
            input "logToken", "text", title: "Splunk Authorization Token", required: false
        }
    }
}

// --- Smartthings lifecycle

def installed() {
    initialize()
}

def updated() {
    debug("updated", "Settings updated")
    unsubscribe()
    unschedule()
    initialize()
}

def uninstalled() {
    debug("uninstalled", "Uninstalling Verisure and removing child devices")
    removeChildDevices(getChildDevices())
}

def initialize() {
    try {
        debug("initialize", "Scheduling polling")
        schedule("* 0/1 * * * ?", checkPeriodically)
    } catch (e) {
        error("initialize", "Could not initialize app. Not scheduling updates.", e)
    }
}

// --- Getters

def getBaseUrl() {
	if (state.serverUrl == null) {
    	switchBaseUrl()
    }
    return state.serverUrl
}

def switchBaseUrl() {
	if (state.serverUrl == "https://e-api01.verisure.com/xbn/2") {
    	state.serverUrl = "https://e-api02.verisure.com/xbn/2"
    } else {
    	state.serverUrl = "https://e-api01.verisure.com/xbn/2"
    }
    debug("switchBaseUrl", "Base url switched to ${state.serverUrl} . ")
}

private hasChildDevice(id) {
    return getChildDevice(id) != null
}

def getAlarmState() {
    debug("getAlarmState", "Retrieving cached alarm state")
    return state.previousAlarmState
}

// -- Application logic

def checkPeriodically() {
    debug("transaction", " ===== START_UPDATE")

    // Backwards compatibility because of rename. Can be deleted after a little while.
    state.disarmedAction = disarmedAction ? disarmedAction : unarmedAction

    // Handling some parameter setup, copying from settings to enable programmatically changing them
    state.app_version = "0.5.4"
    state.remoteLogEnabled = remoteLogEnabled
    state.logUrl = logUrl
    state.logToken = logToken

    debug("checkPeriodically", "Periodic check from timer")
    if (enabled != null && !enabled) {
        debug("checkPeriodically", "Not updating status as alarm is disabled in settings.")
        return
    } else if (state.throttleCounter && state.throttleCounter > 0) {
        debug("checkPeriodically", "Got throttling errors, postponing poll another " + state.throttleCounter + " minutes.")
        state.throttleCounter = state.throttleCounter - 1
        return
    }
    try {
        loginAndUpdateStates()
    } catch (Exception e) {
        error("checkPeriodically", "Error updating alarm state", e)
    }
}

def loginAndUpdateStates() {
    debug("loginAndUpdateStates", "Doing login")

    def loginUrl = getBaseUrl() + "/cookie"

    def authString = "Basic " + ("CPE/" + username + ":" + password).bytes.encodeBase64()

    def params = [
            uri        : loginUrl,
            headers    : [
                    Authorization: authString
            ],
            contentType: "application/json"
    ]

    asynchttp_v1.post('handleLoginResponse', params)
}


def fetchStatusFromServer(sessionCookie, installationId) {
    debug("fetchStatusFromServer", "Fetching overview")

    fetchResourceFromServer("overview", sessionCookie, installationId, "handleOverviewResponse")
}

def fetchInstallationId(sessionCookie) {
    debug("fetchInstallationId", "Finding installation")
    def params = [
            uri        : getBaseUrl() + "/installation/search?email=" + URLEncoder.encode(username),
            contentType: "application/json",
            headers    : [
                    Cookie: "vid=" + sessionCookie
            ]
    ]

    httpGet(params) { response ->
        def installationId = response.data[0]["giid"]
        debug("fetchInstallationId", "Found installation id.")
        return installationId
    }
}

// --- Response handlers for async http

def handleLoginResponse(response, data) {
    debug("handleLoginResponse", "Response for login received")

    if (!checkResponse("handleLoginResponse", response)) return

    def sessionCookie = response.json["cookie"]
    debug("handleLoginResponse", "Session cookie received.")

    fetchStatusFromServer(sessionCookie, fetchInstallationId(sessionCookie))
}

def checkResponse(context, response) {
    if (response.hasError() || response.status != 200) {
        error(context, "Did not get correct response. Got response ${response} .", null)
        if (response.hasError()) {
            if (response.errorData.contains("Request limit has been reached")) {
                state.throttleCounter = 2
            } else if (response.errorData.contains("XBN Database is not activated")) {
            	switchBaseUrl()
            }
            debug(context, "Response has error: " + response.errorData)
        } else {
            debug(context, "Did not get 200. Response code was: " + Integer.toString(response.status))
        }
        return false
    }
    return true
}


def handleOverviewResponse(response, data) {
    debug("handleOverviewResponse", "Overview response received. ")

    if (!checkResponse("handleOverviewResponse", response)) return

    parseAlarmState(response.json["armState"])
    parseSensorResponse(response.json["climateValues"])

    debug("handleOverviewResponse", "Overview response handled")
    debug("transaction", " ===== END_UPDATE")
}

// --- Parse responses to Smartthings objects

def parseSensorResponse(climateState) {
    debug("parseSensorResponse", "Parsing climate sensors")
    //Add or Update Sensors
    climateState.each { updatedJsonDevice ->
        Double tempNumber = updatedJsonDevice.temperature
        Double humidityNumber = updatedJsonDevice.humidity

        if (!hasChildDevice(updatedJsonDevice.deviceLabel)) {
            addChildDevice(app.namespace, "Verisure Sensor", updatedJsonDevice.deviceLabel, null, [
                    label      : updatedJsonDevice.deviceArea,
                    timestamp  : updatedJsonDevice.time,
                    humidity   : humidityNumber,
                    type       : updatedJsonDevice.deviceType,
                    temperature: tempNumber
            ])
            debug("climateDevice.created", updatedJsonDevice.toString())
        } else {
            def existingDevice = getChildDevice(updatedJsonDevice.deviceLabel)

            debug("climateDevice.updated", updatedJsonDevice.deviceArea + " | Humidity: " + humidityNumber + " | Temperature: " + tempNumber, false)
            existingDevice.sendEvent(name: "humidity", value: humidityNumber)
            existingDevice.sendEvent(name: "timestamp", value: updatedJsonDevice.times)
            existingDevice.sendEvent(name: "type", value: updatedJsonDevice.deviceType)
            existingDevice.sendEvent(name: "temperature", value: tempNumber)
        }
    }
}

def parseAlarmState(alarmState) {
    if (state.previousAlarmState == null) {
        state.previousAlarmState = alarmState.statusType
    }

    debug("handleAlarmResponse", "Updating alarm device")
    //Add & update main alarm
    if (!hasChildDevice('verisure-alarm')) {
        debug("alarmDevice.created", alarmDevice)
        addChildDevice(app.namespace, "Verisure Alarm", "verisure-alarm", null, [status: alarmState.statusType, loggedBy: alarmState.name, loggedWhen: alarmState.date])
    } else {
        def alarmDevice = getChildDevice('verisure-alarm')

        debug("alarmDevice.updated", alarmDevice.getDisplayName() + " | Status: " + alarmState.statusType + " | LoggedBy: " + alarmState.name + " | LoggedWhen: " + alarmState.date, false)
        alarmDevice.sendEvent(name: "status", value: alarmState.statusType)
        alarmDevice.sendEvent(name: "loggedBy", value: alarmState.name)
        alarmDevice.sendEvent(name: "loggedWhen", value: alarmState.date)
        alarmDevice.sendEvent(name: "lastUpdate", value: new Date())
    }

    if (alarmState.statusType != state.previousAlarmState) {
        debug("updateAlarmState", "State changed, execution actions")
        state.previousAlarmState = alarmState.statusType
        triggerActions(alarmState.statusType)
    } else {
        debug("updateAlarmState", "State not change. Not triggering routines.")
    }
}

// -- Helper methods

def triggerActions(alarmState) {
    if (alarmState == "ARMED_AWAY" && armedAction) {
        executeAction(armedAction)
    } else if (alarmState == "DISARMED" && state.disarmedAction) {
        executeAction(state.disarmedAction)
    } else if (alarmState == "ARMED_HOME" && armedHomeAction) {
        executeAction(armedHomeAction)
    } else {
        debug("triggerActions", "No actions found for state: " + alarmState)
    }
}

def executeAction(action) {
    debug("executeAction", "Executing action ${action}")
    location.helloHome?.execute(action)
}

private removeChildDevices(delete) {
    delete.each {
        deleteChildDevice(it.deviceNetworkId)
    }
}

/**
 * Generic method for fetching resource on server
 *
 */
def fetchResourceFromServer(resource, sessionCookie, installationId, handler) {
    debug("fetchResourceFromServer", "Fetching resource " + resource)

    def params = [
            uri        : getBaseUrl() + "/installation/" + installationId + "/" + resource,
            contentType: "application/json",
            headers    : [
                    Cookie: "vid=" + sessionCookie
            ]
    ]

    asynchttp_v1.get(handler, params)
}

// --- The following methods has been added to ease remote debugging as well as enforce a certain way of logging.
private createLogString(String context, String message) {
    return "[verisure." + context + "] " + message
}

private error(String context, String text, Exception e) {
    error(context, text, e, true)
}

private error(String context, String text, Exception e, Boolean remote) {
    log.error(createLogString(context, text), e)
    if (remote && state.remoteLogEnabled) {
        httpLog("error", text, e)
    }
}

private debug(String context, String text) {
    debug(context, text, true)
}

private debug(String context, String text, Boolean remote) {
    log.debug(createLogString(context, text))
    if (remote && state.remoteLogEnabled) {
        httpLog("debug", text, null)
    }
}

private httpLog(level, text, e) {
    def message = text

    if (e) {
        message = message + "\n" + e
    }

    def time = new Date()

    def json_body = [
            time : time.getTime(),
            host : location.id + ".smartthings.com",
            event: [
                    time       : time.format("E MMM dd HH:mm:ss.SSS z yyyy"),
                    smartapp_id: app.id,
                    location_id: location.id,
                    namespace  : app.namespace,
                    app_name   : app.name,
                    app_version: state.app_version,
                    level      : level,
                    message    : message
            ]
    ]

    def json_params = [
            uri    : state.logUrl,
            headers: [
                    'Authorization': "Splunk ${state.logToken}"
            ],
            body   : json_body
    ]

    asynchttp_v1.post(json_params)
}