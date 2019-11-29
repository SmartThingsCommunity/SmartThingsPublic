/*
Blue Iris Fusion - Trigger 
(Child app for camera triggering.  Parent app is: "Blue Iris Fusion") 

Created by FLYJMZ (flyjmz230@gmail.com)

Based on work by:
Tony Gutierrez in "Blue Iris Profile Integration"
jpark40 at https://community.smartthings.com/t/blue-iris-profile-trigger/17522/76
luma at https://community.smartthings.com/t/blue-iris-camera-trigger-from-smart-things/25147/9

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
in compliance with the License. You may obtain a copy of the License at:

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
for the specific language governing permissions and limitations under the License.
*/


//////////////////////////////////////////////////////////////////////////////////////////////
///                                     App Info                                            //
//////////////////////////////////////////////////////////////////////////////////////////////

/*
Smartthings Community Thread: https://community.smartthings.com/t/release-bi-fusion-v3-0-adds-blue-iris-device-type-handler-blue-iris-camera-dth-motion-sensing/103032

Github: https://github.com/flyjmz/jmzSmartThings/tree/master/smartapps/flyjmz/blue-iris-fusion-trigger.src

PARENT APP CAN BE FOUND ON GITHUB: https://github.com/flyjmz/jmzSmartThings/tree/master/smartapps/flyjmz/blue-iris-fusion.src

Version History:
Version 1.0 - 30July2016    Initial release
Version 1.1 - 3August2016   Cleaned up code
Version 1.2 - 4August2016   Added Alarm trigger capability from rayzurbock
Version 2.0 - 14Dec2016     Added ability to restrict triggering to defined time periods
Version 2.1 - 17Jan2017     Added preference to turn debug logging on or off
Version 2.2 - 22Jan2017     Added trigger notifications
Version 2.3 - 23Jan2017     Slight tweak to notifications, now receving notifications in the app is user defined instead of always on.
Version 2.4 - 30May2017     Added button push to trigger options
Version 2.5 - 5Oct2017      Added Contact Closing and Switch turning off to trigger options
Version 3.0 - 26Oct2017     Added Blue Iris Server and Camera Device Type Integration, and App Update Notifications
Version 3.0.1 - 28Oct2017   Enabled full Camera DTH support regardless of command method to Blue Iris (BI Server DTH/Local/External)
Version 3.0.2 - 31Oct2017   Added triggers for: acceleration, presence, shock, smoke, carbonMonoxide, and water sensors.
"                           Added ability to send preset commands to cameras when triggering.
"                           Remind Users to click on each Trigger app instance to confirm settings & tap 'done' to ensure initialize() runs.
Version 3.0.3 - 26Nov2017   Changed variable "actionName" to "processName" to fixed java error everyone had (it's a class name, can't be a variable).
"                           Cleaned up log.trace/debug/info to prevent passwords from posting all the time.
Version 3.0.4 - 29Nov2017   Fixed typos, Fixed issue where it required a preset number during initialization.  Confirmed the external mode works as well!!
Version 3.0.5 - 8Dec2017    Removed extra log.trace calls.
Version 3.1 - 5Mar2018      Updates to support camera DTH changes
"                           Added ability to move the camera back to it’s original preset after the triggered event. (per @jrfarrar's request)
Version 3.2 - 17Apr2018     Added option to use mode change as a trigger option per @prjct92eh2 request 
"                           Added knock sensing as a trigger option
Version 3.2.1 - 22Apr2018   Fixed extraneous error messages when mode changes used for trigger (added 3sec delay to let mode change finish first) 
Version 3.2.2 - 6Jul2018	processEvents(data map) data map corrected from knocker and evthandler
"							Mode based trigger delay time now user configurable
"							Fixed "return to preset" actions, it was wasn't overwriting runin calls, but should be (if the same thing keeps triggering the camera and the camera moved to the preset, it needs to just stay there).  
"                               >>>>6Jul18 update-actually it can't overwrite, because if it does then only one camera will return (if you are using more than 1 camera in the same child app)
"                                   >>>>The fix: All the cameras in this child app move to presets or not together, only the presets they move to and from are individual.  So instead of calling returnPresetLocalAction() individually for each camera, just run it once for all cameras, with overwrite on!
"							Fixed Trigger creation when using presets and list of cameras.  The ".toInteger()" attached to the preset inputs to ensure the input was a number just doesn't work for some reason. Removed.
"							Continued to fix presets, the runIn() methods improperly passed data.  Corrected minor typos, cleaned up logs.
"                           6Jul18, resumed work.  Fixed return presets! (I hope. Commented fix in code.) Updated logs.

To Do:
-see todos
*/

def appVersion() {"3.2.2"}

definition(
    name: "Blue Iris Fusion - Trigger",
    namespace: "flyjmz",
    author: "flyjmz230@gmail.com",
    parent: "flyjmz:Blue Iris Fusion",
    description: "Child app to 'Blue Iris Fusion.' Install that app, it will call this during setup.",
    category: "Safety & Security",
    iconUrl: "https://raw.githubusercontent.com/flyjmz/jmzSmartThings/master/resources/BlueIris_logo.png",
    iconX2Url: "https://raw.githubusercontent.com/flyjmz/jmzSmartThings/master/resources/BlueIris_logo%402x.png")

preferences {
    page(name: "mainPage", title: "BI Fusion Custom Camera Triggers", install: true, uninstall: true)
    page(name: "certainTime")
}

def mainPage() {
    return dynamicPage(name: "mainPage", title: "BI Fusion Custom Camera Triggers", submitOnChange: true) {
        section(""){
            paragraph "Select the Camera(s) to control by either selecting from your BI Camera Devices list or type in a camera's short name"
            input "usingCameraDTH", "bool", title: "Select from list?", submitOnChange: true
            paragraph "NOTE: To select cameras, first complete the initial BI Fusion setup. (The camera devices aren't created until you click 'Done' the first time)."
        }
        if (usingCameraDTH) {
            section("Select Blue Iris Camera(s) to Trigger") {   
                input "biCamerasSelected", "capability.videoCamera", title: "Blue Iris Cameras", required: false, multiple: true, submitOnChange: true 
                paragraph "NOTE: Be sure to only select Blue Iris cameras."
            }
        } else {
            section("Blue Iris Camera Name") {  
                paragraph "Enter the Blue Iris short name for the Camera (case-sensitive, no spaces or special characters)."
                input "biCamera", "text", title: "Camera Name", required: false
            }
        }
        section("Select Trigger Events"){   
            input "myMotion", "capability.motionSensor", title: "Motion Sensors Active", required: false, multiple: true
            input "myContactOpen", "capability.contactSensor", title: "Contact Sensors Opening", required: false, multiple: true
            input "myContactClosed", "capability.contactSensor", title: "Contact Sensors Closing", required: false, multiple: true
            input "mySwitchOn", "capability.switch", title: "Switches Turning On", required: false, multiple: true
            input "mySwitchOff", "capability.switch", title: "Switches Turning Off", required: false, multiple: true
            input "myAlarm", "capability.alarm", title: "Alarm Activated", required: false, multiple: true
            input "myButton", "capability.button", title: "Button Pushed", required: false, multiple: true
            input "myAccel", "capability.accelerationSensor", title: "Acceleration Active", required: false, multiple: true
            input "myPresence", "capability.presenceSensor", title: "Presence Arrived", required: false, multiple: true
            input "myShock", "capability.shockSensor", title: "Shock Detected", required: false, multiple: true
            input "mySmoke", "capability.smokeDetector", title: "Smoke/CO Detected", required: false, multiple: true
            input "myWater", "capability.waterSensor", title: "Water Detected", required: false, multiple: true
            input "myMode", "mode", title: "When mode changes to", required: false, multiple: true, submitOnChange: true
            if (myMode) {input name: "modeDelay", type: "number", title: "Delay after mode change (>=3sec)?", required: false}
            input "doorKnocker", "bool", title: "When someone knocks", required: false, multiple: false, submitOnChange: true
            if (doorKnocker) {
                input name: "knockSensor", type: "capability.accelerationSensor", title: "When Someone Knocks Where?"
                input name: "openSensor", type: "capability.contactSensor", title: "But not when they open this door?"
                input name: "knockDelay", type: "number", title: "Knock Delay (defaults to 5s)?", required: false
            }
        }
        section("Camera Trigger and/or PTZ") {
            paragraph "This will trigger camera(s) to record by default, you can also choose to move the camera(s) to a preset:"
            input "usePreset", "bool", title: "Move to Preset?", required: true, submitOnChange: true
            def disableRecording = false
            if (usePreset) {
                input "disableRecording", "bool", title: "Disable Recording (and only move to preset)?", required: false
                input "returnPreset", "bool", title: "Have camera return to original preset after the trigger event?", required: false, submitOnChange: true
                if (returnPreset) input "returnPresetWaitTime", "number", title: "Wait period (seconds) before returning to original preset", required: true
                if (usingCameraDTH) {
                    biCamerasSelected.each { camera ->
                        def cameraName = camera.displayName  
                        input "preset-${cameraName}", "number", title: "Preset # for Camera: ${cameraName}", required: true
                        if (returnPreset) input "switchBackPreset-${cameraName}", "number", title: "Preset # to Return to for '${cameraName}'", required: true
                    }
                } else {
                    input "biPreset", "number", title: "Preset # for Camera: ${biCamera}", required: true
                    if (returnPreset) input "returnBiPreset", "number", title: "Preset # to Return to", required: true
                }
            }
        }
        section(title: "More options", hidden: hideOptionsSection(), hideable: true) {
            def timeLabel = timeIntervalLabel()
            href "certainTime", title: "Only during a certain time", description: timeLabel ?: "Tap to set", state: timeLabel ? "complete" : null
            input "days", "enum", title: "Only on certain days of the week", multiple: true, required: false, options: ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"]
            input "modes", "mode", title: "Only when mode is", multiple: true, required: false
        }
        section("Notifications") {
            def receiveAlerts = false
            input "receiveAlerts", "bool", title: "Receive Push/SMS Alerts When Triggered?"
            if (!parent.localOnly && !parent.usingBIServer) {
                paragraph "You can also receive error SMS/PUSH notifications for this trigger since you are using 'WAN/External' connections.  Message delivery matches your settings in the main BI Fusion app."
                def receiveNotifications = false
                input "receiveNotifications", "bool", title: "Receive Error Push/SMS Notifications?"  //todo -- this would also work for usingBIServer, just need to code it out.
            }
        }
        section("") {
            input "customTitle", "text", title: "Assign a Name", required: true
        }
    }
}

def certainTime() {
    dynamicPage(name:"certainTime",title: "Only during a certain time", uninstall: false) {
        section() {
            input "startingX", "enum", title: "Starting at", options: ["A specific time", "Sunrise", "Sunset"], defaultValue: "A specific time", submitOnChange: true
            if(startingX in [null, "A specific time"]) input "starting", "time", title: "Start time", required: false
            else {
                if(startingX == "Sunrise") input "startSunriseOffset", "number", range: "*..*", title: "Offset in minutes (+/-)", required: false
                else if(startingX == "Sunset") input "startSunsetOffset", "number", range: "*..*", title: "Offset in minutes (+/-)", required: false
                    }
        }

        section() {
            input "endingX", "enum", title: "Ending at", options: ["A specific time", "Sunrise", "Sunset"], defaultValue: "A specific time", submitOnChange: true
            if(endingX in [null, "A specific time"]) input "ending", "time", title: "End time", required: false
            else {
                if(endingX == "Sunrise") input "endSunriseOffset", "number", range: "*..*", title: "Offset in minutes (+/-)", required: false
                else if(endingX == "Sunset") input "endSunsetOffset", "number", range: "*..*", title: "Offset in minutes (+/-)", required: false
                    }
        }
    }
}

def installed() {
    if (parent.loggingOn) log.debug "Installed with settings: ${settings}"
    initialize()
    app.updateLabel("${customTitle}")
}

def updated() {
    if (parent.loggingOn) log.debug "Updated with settings: ${settings}"
    unsubscribe()
    initialize()
    app.updateLabel("${customTitle}")
}

def initialize() {
    subscribe(myMotion, "motion.active", eventHandlerBinary)
    subscribe(myContactOpen, "contact.open", eventHandlerBinary)
    subscribe(myContactClosed, "contact.closed", eventHandlerBinary)
    subscribe(mySwitchOn, "switch.on", eventHandlerBinary)
    subscribe(mySwitchOff, "switch.off", eventHandlerBinary)
    subscribe(myAlarm, "alarm.strobe", eventHandlerBinary)
    subscribe(myAlarm, "alarm.siren", eventHandlerBinary)
    subscribe(myAlarm, "alarm.both", eventHandlerBinary)
    subscribe(myButton, "button.pushed", eventHandlerBinary)
    subscribe(myAccel, "acceleration.active", eventHandlerBinary)
    subscribe(myPresence, "presence.active", eventHandlerBinary)
    subscribe(myShock, "shock.detected", eventHandlerBinary)
    subscribe(mySmoke, "smoke.detected", eventHandlerBinary)
    subscribe(mySmoke, "carbonMonoxide.detected", eventHandlerBinary)
    subscribe(myWater, "water.detected", eventHandlerBinary)
    subscribe(location, "mode", modeChecker)
    subscribe(knockSensor, "acceleration.active", knockAcceleration)
    subscribe(openSensor, "contact.closed", doorClosed)
    state.lastClosed = 0
    def names = []
    def presets = [] 
    def returnPresets = []
    if (usingCameraDTH) { 
        biCamerasSelected.each { camera ->
            names += camera.name
            if (usePreset) {
                def presetInput = "preset-${camera.displayName}"
                presets += settings[presetInput]   
                if (returnPreset) {
                    def switchBackPreset = "switchBackPreset-${camera.displayName}"
                    returnPresets += settings[switchBackPreset]
                }
            }
        }
    } else {
        names += biCamera
        if (usePreset) {
            presets += biPreset
            if (returnPreset) {
                returnPresets += returnBiPreset
            }
        }
    }
    state.shortNameList = names
    state.presetList = presets
    state.returnPresetList = returnPresets
    state.listSize =  state.shortNameList.size()
    log.info "initialized, listSize is $state.listSize, cameras are $state.shortNameList, presets are $state.presetList, and returnPresets are $state.returnPresetList"
}

def eventHandlerBinary(evt) {
    if (parent.loggingOn) log.debug "processed event ${evt.name} from device ${evt.displayName} with value ${evt.value}"
    if (allOk) {
        log.info "Event occured within the desired timing conditions, sending commands"
        processEvents([name: "$evt.displayName", value: "$evt.value"])
    } else if (parent.loggingOn) log.debug "event did not occur within the desired timing conditions, not triggering"
}   

def processEvents(data) {
    def processName = ""
    if (usePreset && !disableRecording) {
        processName = " Moving and Triggering "
        //todo: add a if (parent.usingBIServer) {command here}
    } else if (usePreset && disableRecording) {
        processName = " Moving "
        //todo: add a if (parent.usingBIServer) {command here}
    } else if (!usePreset && disableRecording) {
        processName = " Doing Nothing to "
        //todo: add a if (parent.usingBIServer) {command here}
    } else if (!usePreset && !disableRecording) {
        processName = " Triggering "
        //todo: add a if (parent.usingBIServer) {command here}
    }
    if (parent.loggingOn) log.debug "processName is $processName"  //todo- probably worth splitting out using server as above.
    if (parent.localOnly || parent.usingBIServer) {  //The trigger runs it's own local/external code, so we need to know which BI Fusion is use (and localOnly is the same as using the server in this case)
        if (usingCameraDTH) {       //todo - once callback works, these can be deleted (because the callback will say the camera IS triggered, etc.
            if (!receiveAlerts) sendNotificationEvent("${data.name} is ${data.value}, BI Fusion is" + processName + "Cameras: ${biCamerasSelected}")
            if (receiveAlerts) parent.send("${data.name} is ${data.value}, BI Fusion is" + processName + "Cameras: ${biCamerasSelected}")
        } else {
            if (!receiveAlerts) sendNotificationEvent("${data.name} is ${data.value}, BI Fusion is" + processName + "Camera: ${biCamera}")
            if (receiveAlerts) parent.send("${data.name} is ${data.value}, BI Fusion is" + processName + "Camera: ${biCamera}")
        }
        localAction()
    } else externalAction()
}

def localAction() {
    def triggerCommand = ""
    def presetCommand = ""
    for (int i = 0; i < state.listSize; i++) {
        if (usePreset) {
            log.info "Moving ${state.shortNameList[i]} to preset ${state.presetList[i]}"
            def presetString = 7 + state.presetList[i]  //1-7 are other actions, presets start at number 8.
            presetCommand = "/cam/${state.shortNameList[i]}/pos=${presetString}&user=${parent.username}&pw=${parent.password}"
            talkToHub(presetCommand)
            if (returnPreset) runIn(returnPresetWaitTime, returnPresetLocalAction)
        }
        if (!disableRecording) {
            log.info "Triggering: ${state.shortNameList[i]}"
            triggerCommand = "/admin?camera=${state.shortNameList[i]}&trigger&user=${parent.username}&pw=${parent.password}"
            talkToHub(triggerCommand)
        }
    }
}

def returnPresetLocalAction() {
    def presetCommand = ""
    for (int i = 0; i < state.listSize; i++) {
        log.info "Moving ${state.shortNameList[i]} back to preset ${state.returnPresetList[i]}"
        def presetString = 7 + state.returnPresetList[i]  //1-7 are other actions, presets start at number 8.
        presetCommand = "/cam/${state.shortNameList[i]}/pos=${presetString}&user=${parent.username}&pw=${parent.password}"
        talkToHub(presetCommand)
    }
}

def talkToHub(commandPath) {  //todo can I use a 'callback' function to parse results?  Otherwise the trigger app really isn't confirming it worked...
    def biHost = "${parent.host}:${parent.port}"
    def httpMethod = "GET"
    def httpRequest = [
        method:     httpMethod,
        path:       commandPath,
        headers:    [
            HOST:       biHost,
            Accept:     "*/*",
        ]
    ]
    def hubAction = new physicalgraph.device.HubAction(httpRequest)
    sendHubCommand(hubAction)
    if (parent.loggingOn) log.debug hubAction
}

def externalAction() {
    log.info "Running externalAction"
    try {
        httpPostJson(uri: parent.host + ':' + parent.port, path: '/json',  body: ["cmd":"login"]) { response ->
            if (parent.loggingOn) log.debug response.data
            if (parent.loggingOn) log.debug "logging in"
            if (response.data.result == "fail") {
                if (parent.loggingOn) log.debug "BI_Inside initial call fail, proceeding to login"
                def session = response.data.session
                def hash = parent.username + ":" + response.data.session + ":" + parent.password
                hash = hash.encodeAsMD5()
                httpPostJson(uri: parent.host + ':' + parent.port, path: '/json',  body: ["cmd":"login","session":session,"response":hash]) { response2 ->
                    if (response2.data.result == "success") {
                        if (parent.loggingOn) log.debug ("BI_Logged In")
                        if (parent.loggingOn) log.debug response2.data
                        httpPostJson(uri: parent.host + ':' + parent.port, path: '/json',  body: ["cmd":"status","session":session]) { response3 ->
                            if (response3.data.result == "success"){
                                if (parent.loggingOn) log.debug ("BI_Retrieved Status")
                                if (parent.loggingOn) log.debug response3.data

                                ////////////////////Trigger to Record////////////////////////////////////////////////////
                                if (!disableRecording) {
                                     if (parent.loggingOn) log.debug "Triggering: ${state.shortNameList}"
                                    for (int i = 0; i < state.listSize; i++) {
                                        def shortName = state.shortNameList[i]
                                        httpPostJson(uri: parent.host + ':' + parent.port, path: '/json',  body: ["cmd":"trigger","camera":shortName,"session":session]) { response4 ->
                                            if (parent.loggingOn) log.debug response4.data
                                            if (response4.data.result == "success") {
                                                log.info "BI Fusion triggered: ${shortName}"
                                            } else {
                                                log.error "BI Fusion Failure: ${shortName} not triggered"
                                                if (parent.loggingOn) log.debug(response4.data.data.reason)
                                                if (!receiveNotifications) sendNotificationEvent("BI Fusion Failure: ${shortName} not triggered")
                                                if (receiveNotifications) parent.send("BI Fusion Failure: ${shortName} not triggered")
                                            }
                                        }
                                    }
                                }

                                ////////////////////Move to Preset Position//////////////////////////////////////////////
                                if (usePreset) {
                                    if (parent.loggingOn) log.debug "Moving ${state.shortNameList} to preset ${state.presetList}"
                                    for (int i = 0; i < state.listSize; i++) {
                                        def shortName = state.shortNameList[i]
                                        def presetNumber = state.presetList[i] + 100  //Blue Iris JSON command for preset is 101...120 for preset 1...20
                                        httpPostJson(uri: parent.host + ':' + parent.port, path: '/json',  body: ["cmd":"ptz","camera":shortName,"button":presetNumber, "session":session]) { response4 -> 
                                            if (parent.loggingOn) log.debug response4.data
                                            if (response4.data.result == "success") {
                                                log.info "BI Fusion moved $shortName to preset '${state.presetList[i]}'"
                                                if (returnPreset) runIn(returnPresetWaitTime, returnPresetExternalAction, [data: [listNumber: "$i"]])
                                            } else {
                                                log.error "BI Fusion Failure: preset '${state.presetList[i]}' not sent to $shortName"
                                                if (parent.loggingOn) log.debug(response4.data.data.reason)
                                                if (!receiveNotifications) sendNotificationEvent("BI Fusion Failure: preset '${state.presetList[i]}' not sent to $shortName")
                                                if (receiveNotifications) parent.send("BI Fusion Failure: preset '${state.presetList[i]}' not sent to $shortName")
                                            }
                                        }
                                    }
                                }

                                ////////////////////Logout///////////////////////////////////////////////////////////////
                                httpPostJson(uri: parent.host + ':' + parent.port, path: '/json',  body: ["cmd":"logout","session":session]) { response5 ->
                                    if (parent.loggingOn) log.debug response5.data
                                    if (parent.loggingOn) log.debug "Logged out"
                                }
                                
                            } else {
                                log.error "BI Fusion Failure: didn't receive status"
                                if (parent.loggingOn) log.debug(response3.data.data.reason)
                                if (!receiveNotifications) sendNotificationEvent("BI Fusion Failure: Couldn't Login to Blue Iris")
                                if (receiveNotifications) parent.send("BI Fusion Failure: Couldn't Login to Blue Iris")
                            }
                        }
                    } else {
                        log.error "BI Fusion Failure: Couldn't Login to Blue Iris"
                        if (parent.loggingOn) log.debug(response2.data.data.reason)
                        if (!receiveNotifications) sendNotificationEvent("BI Fusion Failure: Couldn't Login to Blue Iris")
                        if (receiveNotifications) parent.send("BI Fusion Failure: Couldn't Login to Blue Iris")
                    }
                }
            } else {
                log.error "BI Fusion Failure: Couldn't Login to Blue Iris"
                if (parent.loggingOn) log.debug(response.data.data.reason)
                if (!receiveNotifications) sendNotificationEvent("BI Fusion Failure: Couldn't Login to Blue Iris")
                if (receiveNotifications) parent.send("BI Fusion Failure: Couldn't Login to Blue Iris")
            }
        }
    } catch(Exception e) {
        log.error "BI Fusion Failure: $e"
        if (!receiveNotifications) sendNotificationEvent("BI Fusion Failure, turn on debugging and check logs")
        if (receiveNotifications) parent.send("BI Fusion Failure, turn on debugging and check logs")
    }
}

def returnPresetExternalAction(data) {
	def i = data.listNumber
    log.info "Running externalAction"
    try {
        httpPostJson(uri: parent.host + ':' + parent.port, path: '/json',  body: ["cmd":"login"]) { response ->
            if (parent.loggingOn) log.debug response.data
            if (parent.loggingOn) log.debug "logging in"
            if (response.data.result == "fail") {
                if (parent.loggingOn) log.debug "BI_Inside initial call fail, proceeding to login"
                def session = response.data.session
                def hash = parent.username + ":" + response.data.session + ":" + parent.password
                hash = hash.encodeAsMD5()
                httpPostJson(uri: parent.host + ':' + parent.port, path: '/json',  body: ["cmd":"login","session":session,"response":hash]) { response2 ->
                    if (response2.data.result == "success") {
                        if (parent.loggingOn) log.debug ("BI_Logged In")
                        if (parent.loggingOn) log.debug response2.data
                        httpPostJson(uri: parent.host + ':' + parent.port, path: '/json',  body: ["cmd":"status","session":session]) { response3 ->
                            if (response3.data.result == "success"){
                                if (parent.loggingOn) log.debug ("BI_Retrieved Status")
                                if (parent.loggingOn) log.debug response3.data

                                ////////////////////Move Back to Return Preset Position//////////////////////////////////////////////
                                def shortName = state.shortNameList[i]
                                def presetNumber = state.returnPresetList[i] + 100  //Blue Iris JSON command for preset is 101...120 for preset 1...20
                                if (parent.loggingOn) log.debug "Moving ${shortName} back to preset ${presetNumber}"
                                httpPostJson(uri: parent.host + ':' + parent.port, path: '/json',  body: ["cmd":"ptz","camera":shortName,"button":presetNumber, "session":session]) { response4 -> 
                                    if (parent.loggingOn) log.debug response4.data
                                    if (response4.data.result == "success") {
                                        log.info "BI Fusion moved $shortName to preset '${state.presetList[i]}'"
                                        if (returnPreset) runIn(returnPresetWaitTime, returnPresetExternalAction, [data: [listNumber: "$i"]])
                                    } else {
                                        log.error "BI Fusion Failure: preset '${state.presetList[i]}' not sent to $shortName"
                                        if (parent.loggingOn) log.debug(response4.data.data.reason)
                                        if (!receiveNotifications) sendNotificationEvent("BI Fusion Failure: preset '${state.presetList[i]}' not sent to $shortName")
                                        if (receiveNotifications) parent.send("BI Fusion Failure: preset '${state.presetList[i]}' not sent to $shortName")
                                    }
                                }
                                ////////////////////Logout///////////////////////////////////////////////////////////////
                                httpPostJson(uri: parent.host + ':' + parent.port, path: '/json',  body: ["cmd":"logout","session":session]) { response5 ->
                                    if (parent.loggingOn) log.debug response5.data
                                    if (parent.loggingOn) log.debug "Logged out"
                                }
                                
                            } else {
                                log.error "BI Fusion Failure: didn't receive status"
                                if (parent.loggingOn) log.debug(response3.data.data.reason)
                                if (!receiveNotifications) sendNotificationEvent("BI Fusion Failure: Couldn't Login to Blue Iris")
                                if (receiveNotifications) parent.send("BI Fusion Failure: Couldn't Login to Blue Iris")
                            }
                        }
                    } else {
                        log.error "BI Fusion Failure: Couldn't Login to Blue Iris"
                        if (parent.loggingOn) log.debug(response2.data.data.reason)
                        if (!receiveNotifications) sendNotificationEvent("BI Fusion Failure: Couldn't Login to Blue Iris")
                        if (receiveNotifications) parent.send("BI Fusion Failure: Couldn't Login to Blue Iris")
                    }
                }
            } else {
                log.error "BI Fusion Failure: Couldn't Login to Blue Iris"
                if (parent.loggingOn) log.debug(response.data.data.reason)
                if (!receiveNotifications) sendNotificationEvent("BI Fusion Failure: Couldn't Login to Blue Iris")
                if (receiveNotifications) parent.send("BI Fusion Failure: Couldn't Login to Blue Iris")
            }
        }
    } catch(Exception e) {
        log.error "BI Fusion Failure: $e"
        if (!receiveNotifications) sendNotificationEvent("BI Fusion Failure, turn on debugging and check logs")
        if (receiveNotifications) parent.send("BI Fusion Failure, turn on debugging and check logs")
    }
}

def modeChecker(evt) {
    if (evt.name != "mode") {return;}
    def checkMode = evt.value
    def triggerFromModeChange = false
    if (myMode != null) log.info "mode change detected, mode now: " + checkMode
    if (allOk) {
        myMode.each { eachOfMyMode ->
            if (checkMode == eachOfMyMode) {
                triggerFromModeChange = true
                if (parent.loggingOn) log.debug "checkMode '$checkMode' is in eachOfMyMode: '$eachOfMyMode', triggering cameras after short delay"
            }  
        }
    }
    if (triggerFromModeChange) {
    	def modeDelayTime = modeDelay ?: 5
        runIn(modeDelay, processEvents, [data: [name: "Mode", value: "$checkMode"]])
        	//added delay because triggers were happening while the mode change was happening,
            //causing error messages because it had sent the mode change, but the trigger's responses 
            //were coming back before the error validation for profile change completed,
            //so it thought it didn't change profiles.
    }
}

def knockAcceleration(evt) {
    def delay = knockDelay ?: 5
    runIn(delay, "doorKnock")
}

def doorClosed(evt) {
    state.lastClosed = now()
}

def doorKnock() {
    if ( (openSensor.latestValue("contact") == "closed") && (now() - (60 * 1000) > state.lastClosed) && allOk) {
        def knockSensorName = "${knockSensor.label ?: knockSensor.name}"
        log.info "$knockSensorName detected a knock."
        processEvents([name: "$knockSensorName", value: "knocking"])
    }
    else {
        if (parent.loggingOn) log.debug("${knockSensor.label ?: knockSensor.name} knocked, but looks like it was just someone opening the door.")
    }
}

private getAllOk() {
    modeOk && daysOk && timeOk
}

private getDaysOk() {
    def result = true
    if (days) {
        def df = new java.text.SimpleDateFormat("EEEE")
        if (location.timeZone) {
            df.setTimeZone(location.timeZone)
        }
        else {
            df.setTimeZone(TimeZone.getTimeZone("America/New_York"))
        }
        def day = df.format(new Date())
        result = days.contains(day)
    }
    return result
}

private getTimeOk() {
    def result = true
    if ((starting && ending) ||
        (starting && endingX in ["Sunrise", "Sunset"]) ||
        (startingX in ["Sunrise", "Sunset"] && ending) ||
        (startingX in ["Sunrise", "Sunset"] && endingX in ["Sunrise", "Sunset"])) {
        def currTime = now()
        def start = null
        def stop = null
        def s = getSunriseAndSunset(zipCode: zipCode, sunriseOffset: startSunriseOffset, sunsetOffset: startSunsetOffset)
        if(startingX == "Sunrise") start = s.sunrise.time
        else if(startingX == "Sunset") start = s.sunset.time
            else if(starting) start = timeToday(starting,location.timeZone).time
                s = getSunriseAndSunset(zipCode: zipCode, sunriseOffset: endSunriseOffset, sunsetOffset: endSunsetOffset)
            if(endingX == "Sunrise") stop = s.sunrise.time
            else if(endingX == "Sunset") stop = s.sunset.time
                else if(ending) stop = timeToday(ending,location.timeZone).time
                    result = start < stop ? currTime >= start && currTime <= stop : currTime <= stop || currTime >= start
            }
    return result
}

private getModeOk() {
    def result = !modes || modes.contains(location.mode)
    return result
}

private hhmm(time, fmt = "h:mm a") {
    def t = timeToday(time, location.timeZone)
    def f = new java.text.SimpleDateFormat(fmt)
    f.setTimeZone(location.timeZone ?: timeZone(time))
    f.format(t)
}

private hideOptionsSection() {
    (starting || ending || days || modes || startingX || endingX) ? false : true
}

private offset(value) {
    def result = value ? ((value > 0 ? "+" : "") + value + " min") : ""
}

private timeIntervalLabel() {
    def result = ""
    if (startingX == "Sunrise" && endingX == "Sunrise") {result = "Sunrise" + offset(startSunriseOffset) + " to " + "Sunrise" + offset(endSunriseOffset)}
    else if (startingX == "Sunrise" && endingX == "Sunset") {result = "Sunrise" + offset(startSunriseOffset) + " to " + "Sunset" + offset(endSunsetOffset)}
    else if (startingX == "Sunset" && endingX == "Sunrise") {result = "Sunset" + offset(startSunsetOffset) + " to " + "Sunrise" + offset(endSunriseOffset)}
    else if (startingX == "Sunset" && endingX == "Sunset") {result = "Sunset" + offset(startSunsetOffset) + " to " + "Sunset" + offset(endSunsetOffset)}
    else if (startingX == "Sunrise" && ending) {result = "Sunrise" + offset(startSunriseOffset) + " to " + hhmm(ending, "h:mm a z")}
    else if (startingX == "Sunset" && ending) {result = "Sunset" + offset(startSunsetOffset) + " to " + hhmm(ending, "h:mm a z")}
    else if (starting && endingX == "Sunrise") {result = hhmm(starting) + " to " + "Sunrise" + offset(endSunriseOffset)}
    else if (starting && endingX == "Sunset") {result = hhmm(starting) + " to " + "Sunset" + offset(endSunsetOffset)}
    else if (starting && ending) {result = hhmm(starting) + " to " + hhmm(ending, "h:mm a z")}
}