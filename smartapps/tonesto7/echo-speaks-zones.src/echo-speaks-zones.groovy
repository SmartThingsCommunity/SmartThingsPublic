/**
 *  Echo Speaks - Zones
 *
 *  Copyright 2018, 2019, 2020 Anthony Santilli
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

String appVersion()  { return "3.6.2.0" }
String appModified() { return "2020-04-22" }
String appAuthor()	 { return "Anthony S." }
Boolean isBeta()     { return false }
Boolean isST()       { return (getPlatform() == "SmartThings") }

definition(
    name: "Echo Speaks - Zones",
    namespace: "tonesto7",
    author: "Anthony Santilli",
    description: "DO NOT INSTALL FROM MARKETPLACE\n\nAllows you to create virtual broadcast zones based on your echo devices using device/location events to active the zone.",
    category: "My Apps",
    parent: "tonesto7:Echo Speaks",
    iconUrl: "https://raw.githubusercontent.com/tonesto7/echo-speaks/master/resources/icons/es_groups.png",
    iconX2Url: "https://raw.githubusercontent.com/tonesto7/echo-speaks/master/resources/icons/es_groups.png",
    iconX3Url: "https://raw.githubusercontent.com/tonesto7/echo-speaks/master/resources/icons/es_groups.png",
    importUrl  : "https://raw.githubusercontent.com/tonesto7/echo-speaks/master/smartapps/tonesto7/echo-speaks-zones.src/echo-speaks-zones.groovy",
    pausable: true)

preferences {
    page(name: "startPage")
    page(name: "uhOhPage")
    page(name: "codeUpdatePage")
    page(name: "mainPage", install: false, uninstall: false)
    page(name: "prefsPage")
    page(name: "conditionsPage")
    page(name: "condTimePage")
    page(name: "zoneNotifPage")
    page(name: "zoneNotifTimePage")
    page(name: "zoneHistoryPage")
    page(name: "uninstallPage")
    page(name: "namePage")
}

def startPage() {
    if(parent != null) {
        if(!state?.isInstalled && parent?.childInstallOk() != true) { return uhOhPage() }
        else {
            state?.isParent = false; return (checkMinVersion()) ? codeUpdatePage() : mainPage();
        }
    } else { return uhOhPage() }
}

def appInfoSect(sect=true)	{
    def instDt = state?.dateInstalled ? fmtTime(state?.dateInstalled, "MMM dd '@'h:mm a", true) : null
    section() { href "empty", title: pTS("${app?.name}", getAppImg("es_groups", true)), description: "${instDt ? "Installed: ${instDt}\n" : ""}Version: ${appVersion()}", image: getAppImg("es_groups") }
}

def codeUpdatePage () {
    return dynamicPage(name: "codeUpdatePage", title: "Update is Required", install: false, uninstall: false) {
        section() { paragraph "Looks like your Zone App needs an update\n\nPlease make sure all app and device code is updated to the most current version\n\nOnce updated your zones will resume normal operation.", required: true, state: null, image: getAppImg("exclude") }
    }
}

def uhOhPage () {
    return dynamicPage(name: "uhOhPage", title: "This install Method is Not Allowed", install: false, uninstall: true) {
        section() {
            def str = "HOUSTON WE HAVE A PROBLEM!\n\nEcho Speaks - Zones can't be directly installed from the Marketplace.\n\nPlease use the Echo Speaks SmartApp to configure them."
            paragraph str, required: true, state: null, image: getAppImg("exclude")
        }
        if(isST()) { remove("Remove this bad Zone", "WARNING!!!", "BAD Zone SHOULD be removed") }
    }
}

def mainPage() {
    Boolean newInstall = (state?.isInstalled != true)
    return dynamicPage(name: "mainPage", nextPage: (!newInstall ? "" : "namePage"), uninstall: (newInstall == true), install: !newInstall) {
        appInfoSect()
        Boolean paused = isPaused()
        Boolean dup = (settings?.duplicateFlag == true || state?.dupPendingSetup == true)
        if(dup) {
            state?.dupOpenedByUser = true
            section() { paragraph pTS("This Zone was just created from an existing zone.\n\nPlease review the settings and save to activate...", getAppImg("pause_orange", true), false, "red"), required: true, state: null, image: getAppImg("pause_orange") }
        }
        if(paused) {
            section() {
                paragraph pTS("This Zone is currently disabled...\nTo edit the please re-enable it.", getAppImg("pause_orange", true), false, "red"), required: true, state: null, image: getAppImg("pause_orange")
            }
        } else {
            if(settings?.cond_mode && !settings?.cond_mode_cmd) { settingUpdate("cond_mode_cmd", "are", "enum") }
            Boolean condConf = conditionsConfigured()
            section(sTS("Zone Configuration:")) {
                href "conditionsPage", title: inTS("Zone Activation Conditions", getAppImg("conditions", true)), description: getConditionsDesc(), required: true, state: (condConf ? "complete": null), image: getAppImg("conditions")
                if(condConf) { echoDevicesInputByPerm("announce") }
            }

            if(settings?.zone_EchoDevices) {
                section(sTS("Condition Delays:")) {
                    input "zone_active_delay", "number", title: inTS("Delay Activation in Seconds\n(Optional)", getAppImg("delay_time", true)), required: false, submitOnChange: true, image: getAppImg("delay_time")
                    input "zone_inactive_delay", "number", title: inTS("Delay Deactivation in Seconds\n(Optional)", getAppImg("delay_time", true)), required: false, submitOnChange: true, image: getAppImg("delay_time")
                }
                section(sTS("Zone Active Tasks (Optional):")) {
                    input "zone_active_switches_on", "capability.switch", title: inTS("Turn on Switches when Zone Active\n(Optional)", getAppImg("switch", true)), multiple: true, required: false, submitOnChange: true, image: getAppImg("switch")
                    input "zone_active_switches_off", "capability.switch", title: inTS("Turn off Switches when Zone Active\n(Optional)", getAppImg("switch", true)), multiple: true, required: false, submitOnChange: true, image: getAppImg("switch")
                }
                section(sTS("Zone Inactive Tasks (Optional):")) {
                    input "zone_inactive_switches_on", "capability.switch", title: inTS("Turn on Switches when Zone Inactive\n(Optional)", getAppImg("switch", true)), multiple: true, required: false, submitOnChange: true, image: getAppImg("switch")
                    input "zone_inactive_switches_off", "capability.switch", title: inTS("Turn off Switches when Zone Inactive\n(Optional)", getAppImg("switch", true)), multiple: true, required: false, submitOnChange: true, image: getAppImg("switch")
                }
                section(sTS("Notifications:")) {
                    def t0 = getAppNotifDesc()
                    href "zoneNotifPage", title: inTS("Send Notifications", getAppImg("notification2", true)), description: (t0 ? "${t0}\nTap to modify" : "Tap to configure"), state: (t0 ? "complete" : null), image: getAppImg("notification2")
                }
            }
        }

        section(sTS("Zone History")) {
            href "zoneHistoryPage", title: inTS("View Zone History", getAppImg("tasks", true)), description: "", image: getAppImg("tasks")
        }

        section(sTS("Preferences")) {
            href "prefsPage", title: inTS("Logging Preferences", getAppImg("settings", true)), description: "", image: getAppImg("settings")
            if(state?.isInstalled) {
                input "zonePause", "bool", title: inTS("Disable Zone?", getAppImg("pause_orange", true)), defaultValue: false, submitOnChange: true, image: getAppImg("pause_orange")
                if(zonePause) { unsubscribe() }
            }
        }
        if(state?.isInstalled) {
            section(sTS("Name this Zone:")) {
                input "appLbl", "text", title: inTS("Zone Name", getAppImg("name_tag", true)), description: "", required:true, submitOnChange: true, image: getAppImg("name_tag")
            }
            section(sTS("Remove Zone:")) {
                href "uninstallPage", title: inTS("Remove this Zone", getAppImg("uninstall", true)), description: "Tap to Remove...", image: getAppImg("uninstall")
            }
        }
    }
}

private echoDevicesInputByPerm(type) {
    List echoDevs = parent?.getChildDevicesByCap(type as String)
    if(echoDevs?.size()) {
        def eDevsMap = echoDevs?.collectEntries { [(it?.getId()): [label: it?.getLabel(), lsd: (it?.currentWasLastSpokenToDevice?.toString() == "true")]] }?.sort { a,b -> b?.value?.lsd <=> a?.value?.lsd ?: a?.value?.label <=> b?.value?.label }
        input "zone_EchoDevices", "enum", title: inTS("Echo Devices in Zone", getAppImg("echo_gen1", true)), description: "Select the devices", options: eDevsMap?.collectEntries { [(it?.key): "${it?.value?.label}${(it?.value?.lsd == true) ? "\n(Last Spoken To)" : ""}"] }, multiple: true, required: true, submitOnChange: true, image: getAppImg("echo_gen1")
    } else { paragraph "No devices were found with support for ($type)"}
}

def zoneHistoryPage() {
    return dynamicPage(name: "zoneHistoryPage", install: false, uninstall: false) {
        section() {
            getZoneHistory()
        }
        if(atomicState?.zoneHistory?.size()) {
            section("") {
                input "clearZoneHistory", "bool", title: inTS("Clear Zone History?", getAppImg("reset", true)), description: "Clears Stored Zone History.", defaultValue: false, submitOnChange: true, image: getAppImg("reset")
                if(settings?.clearZoneHistory) { settingUpdate("clearZoneHistory", "false", "bool"); atomicState?.zoneHistory = []; }
            }
        }
    }
}

def prefsPage() {
    return dynamicPage(name: "prefsPage", install: false, uninstall: false) {
        section(sTS("Logging:")) {
            input "logInfo", "bool", title: inTS("Show Info Logs?", getAppImg("debug", true)), required: false, defaultValue: true, submitOnChange: true, image: getAppImg("debug")
            input "logWarn", "bool", title: inTS("Show Warning Logs?", getAppImg("debug", true)), required: false, defaultValue: true, submitOnChange: true, image: getAppImg("debug")
            input "logError", "bool", title: inTS("Show Error Logs?", getAppImg("debug", true)), required: false, defaultValue: true, submitOnChange: true, image: getAppImg("debug")
            input "logDebug", "bool", title: inTS("Show Debug Logs?", getAppImg("debug", true)), description: "Auto disables after 6 hours", required: false, defaultValue: false, submitOnChange: true, image: getAppImg("debug")
            input "logTrace", "bool", title: inTS("Show Detailed Logs?", getAppImg("debug", true)), description: "Only enable when asked to.\n(Auto disables after 6 hours)", required: false, defaultValue: false, submitOnChange: true, image: getAppImg("debug")
        }
        if(advLogsActive()) { logsEnabled() }
    }
}

def namePage() {
    return dynamicPage(name: "namePage", install: true, uninstall: false) {
        section(sTS("Zone Description:")) {
            input "appLbl", "text", title: inTS("Name this Zone", getAppImg("name_tag", true)), description: "", required:true, submitOnChange: true, image: getAppImg("name_tag")
        }
    }
}

def uninstallPage() {
    return dynamicPage(name: "uninstallPage", title: "Uninstall", install: false , uninstall: true) {
        section("") { paragraph "This will delete this Echo Speaks Zone." }
        if(isST()) { remove("Remove ${app?.label} Zone", "WARNING!!!", "Last Chance to Stop!\nThis action is not reversible\n\nThis Zone will be removed") }
    }
}

/******************************************************************************
    CONDITIONS SELECTION PAGE
******************************************************************************/

def conditionsPage() {
    return dynamicPage(name: "conditionsPage", title: "", nextPage: "mainPage", install: false, uninstall: false) {
        Boolean multiConds = multipleConditions()
        if(multiConds) {
            section() {
                input "cond_require_all", "bool", title: inTS("Require All Selected Conditions to Pass Before Activating Zone?", getAppImg("checkbox", true)), required: false, defaultValue: false, submitOnChange: true, image: getAppImg("checkbox")
                paragraph pTS("Notice:\n${cond_require_all == true ? "All selected conditions required to make zone active." : "Any condition will make this zone active."}", null, false, "#2784D9"), state: "complete"
            }
        }

        section(sTS("Time/Date")) {
            href "condTimePage", title: inTS("Time Schedule", getAppImg("clock", true)), description: getTimeCondDesc(false), state: (timeCondConfigured() ? "complete" : null), image: getAppImg("clock")
            input "cond_days", "enum", title: inTS("Days of the week", getAppImg("day_calendar", true)), multiple: true, required: false, submitOnChange: true, options: weekDaysEnum(), image: getAppImg("day_calendar")
            input "cond_months", "enum", title: inTS("Months of the year", getAppImg("day_calendar", true)), multiple: true, required: false, submitOnChange: true, options: monthEnum(), image: getAppImg("day_calendar")
        }

        section (sTS("Mode Conditions")) {
            input "cond_mode", "mode", title: inTS("Location Modes...", getAppImg("mode", true)), multiple: true, required: false, submitOnChange: true, image: getAppImg("mode")
            if(settings?.cond_mode) {
                input "cond_mode_cmd", "enum", title: inTS("are...", getAppImg("command", true)), options: ["not":"not in these modes", "are":"In these Modes"], required: true, multiple: false, submitOnChange: true, image: getAppImg("command")
                if(cond_mode && cond_mode_cmd) {
                    input "cond_mode_db", "bool", title: inTS("Deactivate Zone immediately when Mode condition no longer passes?", getAppImg("checkbox", true)), required: false, defaultValue: false, submitOnChange: true, image: getAppImg("checkbox")
                }
            }
        }

        section (sTS("Alarm Conditions")) {
            input "cond_alarm", "enum", title: inTS("${getAlarmSystemName()} is...", getAppImg("alarm_home", true)), options: getAlarmTrigOpts(), multiple: false, required: false, submitOnChange: true, image: getAppImg("alarm_home")
            if(settings?.cond_alarm) {
                input "cond_alarm_db", "bool", title: inTS("Deactivate Zone immediately when Alarm condition no longer passes?", getAppImg("checkbox", true)), required: false, defaultValue: false, submitOnChange: true, image: getAppImg("checkbox")
            }
        }

        condNonNumSect("switch", "switch", "Switches/Outlets Conditions", "Switches/Outlets", ["on","off"], "are", "switch")

        condNonNumSect("motion", "motionSensor", "Motion Conditions", "Motion Sensors", ["active", "inactive"], "are", "motion")

        condNonNumSect("presence", "presenceSensor", "Presence Conditions", "Presence Sensors", ["present", "not present"], "are", "presence")

        condNonNumSect("contact", "contactSensor", "Door, Window, Contact Sensors Conditions", "Contact Sensors",  ["open","closed"], "are", "contact")

        condNonNumSect("acceleration", "accelerationSensor", "Accelorometer Conditions", "Accelorometer Sensors", ["active","inactive"], "are", "acceleration")

        condNonNumSect("lock", "lock", "Lock Conditions", "Smart Locks", ["locked", "unlocked"], "are", "lock")

        condNonNumSect("door", "garageDoorControl", "Garage Door Conditions", "Garage Doors", ["open", "closed"], "are", "garage_door")

        condNumValSect("temperature", "temperatureMeasurement", "Temperature Conditions", "Temperature Sensors", "Temperature", "temperature")

        condNumValSect("humidity", "relativeHumidityMeasurement", "Humidity Conditions", "Relative Humidity Sensors", "Relative Humidity (%)", "humidity")

        condNumValSect("illuminance", "illuminanceMeasurement", "Illuminance Conditions", "Illuminance Sensors", "Lux Level (%)", "illuminance")

        condNumValSect("level", "switchLevel", "Dimmers/Levels", "Dimmers/Levels", "Level (%)", "speed_knob")

        condNonNumSect("water", "waterSensor", "Water Sensors", "Water Sensors", ["wet", "dry"], "are", "water")

        condNumValSect("power", "powerMeter", "Power Events", "Power Meters", "Power Level (W)", "power")

        condNonNumSect("shade", "windowShades", "Window Shades", "Window Shades", ["open", "closed"], "are", "shade")

        condNonNumSect("valve", "valve", "Valves", "Valves", ["open", "closed"], "are", "valve")

        condNumValSect("battery", "battery", "Battery Level Conditions", "Batteries", "Level (%)", "battery")
    }
}

def condNonNumSect(String inType, String capType, String sectStr, String devTitle, cmdOpts, String cmdTitle, String image) {
    section (sTS(sectStr), hideWhenEmpty: true) {
        input "cond_${inType}", "capability.${capType}", title: inTS(devTitle, getAppImg(image, true)), multiple: true, submitOnChange: true, required:false, image: getAppImg(image), hideWhenEmpty: true
        if (settings?."cond_${inType}") {
            input "cond_${inType}_cmd", "enum", title: inTS("${cmdTitle}...", getAppImg("command", true)), options: cmdOpts, multiple: false, required: true, submitOnChange: true, image: getAppImg("command")
            if (settings?."cond_${inType}_cmd" && settings?."cond_${inType}"?.size() > 1) {
                input "cond_${inType}_all", "bool", title: inTS("ALL ${devTitle} must be (${settings?."cond_${inType}_cmd"})?", getAppImg("checkbox", true)), required: false, defaultValue: false, submitOnChange: true, image: getAppImg("checkbox")
            }
            input "cond_${inType}_db", "bool", title: inTS("Deactivate Zone immediately when ${cmdTitle} condition no longer passes?", getAppImg("checkbox", true)), required: false, defaultValue: false, submitOnChange: true, image: getAppImg("checkbox")
        }
    }
}

def condNumValSect(String inType, String capType, String sectStr, String devTitle, String cmdTitle, String image, hideable= false) {
    section (sTS(sectStr), hideWhenEmpty: true) {
        input "cond_${inType}", "capability.${capType}", title: inTS(devTitle, getAppImg(image, true)), multiple: true, submitOnChange: true, required: false, image: getAppImg(image), hideWhenEmpty: true
        if(settings?."cond_${inType}") {
            input "cond_${inType}_cmd", "enum", title: inTS("${cmdTitle} is...", getAppImg("command", true)), options: ["between", "below", "above", "equals"], required: true, multiple: false, submitOnChange: true, image: getAppImg("command")
            if (settings?."cond_${inType}_cmd") {
                if (settings?."cond_${inType}_cmd" in ["between", "below"]) {
                    input "cond_${inType}_low", "number", title: inTS("a ${settings?."cond_${inType}_cmd" == "between" ? "Low " : ""}${cmdTitle} of...", getAppImg("low", true)), required: true, submitOnChange: true, image: getAppImg("low")
                }
                if (settings?."cond_${inType}_cmd" in ["between", "above"]) {
                    input "cond_${inType}_high", "number", title: inTS("${settings?."cond_${inType}_cmd" == "between" ? "and a high " : "a "}${cmdTitle} of...", getAppImg("high", true)), required: true, submitOnChange: true, image: getAppImg("high")
                }
                if (settings?."cond_${inType}_cmd" == "equals") {
                    input "cond_${inType}_equal", "number", title: inTS("a ${cmdTitle} of...", getAppImg("equal", true)), required: true, submitOnChange: true, image: getAppImg("equal")
                }
                if (settings?."cond_${inType}"?.size() > 1) {
                    input "cond_${inType}_all", "bool", title: inTS("Require ALL devices to be (${settings?."cond_${inType}_cmd"}) values?", getAppImg("checkbox", true)), required: false, defaultValue: false, submitOnChange: true, image: getAppImg("checkbox")
                    if(!settings?."cond_${inType}_all") {
                        input "cond_${inType}_avg", "bool", title: inTS("Use the average of all selected device values?", getAppImg("checkbox", true)), required: false, defaultValue: false, submitOnChange: true, image: getAppImg("checkbox")
                    }
                }
                input "cond_${inType}_db", "bool", title: inTS("Deactivate Zone immediately when ${cmdTitle} condition no longer passes?", getAppImg("checkbox", true)), required: false, defaultValue: false, submitOnChange: true, image: getAppImg("checkbox")
            }
        }
    }
}

def condTimePage() {
    return dynamicPage(name:"condTimePage", title: "", install: false, uninstall: false) {
        Boolean timeReq = (settings["cond_time_start"] || settings["cond_time_stop"])
        section(sTS("Start Time:")) {
            input "cond_time_start_type", "enum", title: inTS("Starting at...", getAppImg("start_time", true)), options: ["time":"Time of Day", "sunrise":"Sunrise", "sunset":"Sunset"], required: false , submitOnChange: true, image: getAppImg("start_time")
            if(cond_time_start_type  == "time") {
                input "cond_time_start", "time", title: inTS("Start time", getAppImg("start_time", true)), required: timeReq, submitOnChange: true, image: getAppImg("start_time")
            } else if(cond_time_start_type in ["sunrise", "sunrise"]) {
                input "cond_time_start_offset", "number", range: "*..*", title: inTS("Offset in minutes (+/-)", getAppImg("start_time", true)), required: false, submitOnChange: true, image: getAppImg("threshold")
            }
        }
        section(sTS("Stop Time:")) {
            input "cond_time_stop_type", "enum", title: inTS("Stopping at...", getAppImg("start_time", true)), options: ["time":"Time of Day", "sunrise":"Sunrise", "sunset":"Sunset"], required: false , submitOnChange: true, image: getAppImg("stop_time")
            if(cond_time_stop_type == "time") {
                input "cond_time_stop", "time", title: inTS("Stop time", getAppImg("start_time", true)), required: timeReq, submitOnChange: true, image: getAppImg("stop_time")
            } else if(cond_time_stop_type in ["sunrise", "sunrise"]) {
                input "cond_time_stop_offset", "number", range: "*..*", title: inTS("Offset in minutes (+/-)", getAppImg("start_time", true)), required: false, submitOnChange: true, image: getAppImg("threshold")
            }
        }
    }
}

def zoneNotifPage() {
    return dynamicPage(name: "zoneNotifPage", title: "Zone Notifications", install: false, uninstall: false) {
        section (sTS("Message Customization:")) {
            input "notif_active_message", "text", title: inTS("Active Message?", getAppImg("text", true)), required: false, submitOnChange: true, image: getAppImg("text")
            input "notif_inactive_message", "text", title: inTS("Inactive Message?", getAppImg("text", true)), required: false, submitOnChange: true, image: getAppImg("text")
        }
        if(settings?.notif_active_message || settings?.notif_inactive_message) {
            section (sTS("Push Messages:")) {
                input "notif_send_push", "bool", title: inTS("Send Push Notifications...", getAppImg("question", true)), required: false, defaultValue: false, submitOnChange: true, image: getAppImg("question")
            }
            section (sTS("Text Messages:"), hideWhenEmpty: true) {
                paragraph pTS("To send to multiple numbers separate the number by a comma\n\nE.g. 8045551122,8046663344", getAppImg("info", true), false, "gray")
                paragraph pTS("SMS Support will soon be removed from Hubitat and SmartThings (UK)", getAppImg("info", true), false, "gray")
                input "notif_sms_numbers", "text", title: inTS("Send SMS Text to...", getAppImg("sms_phone", true)), required: false, submitOnChange: true, image: getAppImg("sms_phone")
            }
            section (sTS("Notification Devices:")) {
                input "notif_devs", "capability.notification", title: inTS("Send to Notification devices?", getAppImg("notification", true)), required: false, multiple: true, submitOnChange: true, image: getAppImg("notification")
            }
            section (sTS("Alexa Mobile Notification:")) {
                paragraph pTS("This will send a push notification the Alexa Mobile app.", null, false, "gray")
                input "notif_alexa_mobile", "bool", title: inTS("Send message to Alexa App?", getAppImg("notification", true)), required: false, defaultValue: false, submitOnChange: true, image: getAppImg("notification")
            }
            if(isST()) {
                section(sTS("Pushover Support:")) {
                    input "notif_pushover", "bool", title: inTS("Use Pushover Integration", getAppImg("pushover_icon", true)), required: false, submitOnChange: true, image: getAppImg("pushover")
                    if(settings?.notif_pushover == true) {
                        def poDevices = parent?.getPushoverDevices()
                        if(!poDevices) {
                            parent?.pushover_init()
                            paragraph pTS("If this is the first time enabling Pushover than leave this page and come back if the devices list is empty", null, false, "#2784D9"), state: "complete"
                        } else {
                            input "notif_pushover_devices", "enum", title: inTS("Select Pushover Devices", getAppImg("select_icon", true)), description: "Tap to select", groupedOptions: poDevices, multiple: true, required: false, submitOnChange: true, image: getAppImg("select_icon")
                            if(settings?.notif_pushover_devices) {
                                def t0 = [(-2):"Lowest", (-1):"Low", 0:"Normal", 1:"High", 2:"Emergency"]
                                input "notif_pushover_priority", "enum", title: inTS("Notification Priority (Optional)", getAppImg("priority", true)), description: "Tap to select", defaultValue: 0, required: false, multiple: false, submitOnChange: true, options: t0, image: getAppImg("priority")
                                input "notif_pushover_sound", "enum", title: inTS("Notification Sound (Optional)", getAppImg("sound", true)), description: "Tap to select", defaultValue: "pushover", required: false, multiple: false, submitOnChange: true, options: parent?.getPushoverSounds(), image: getAppImg("sound")
                            }
                        }
                    }
                }
            }
            if(isZoneNotifConfigured()) {
                section(sTS("Notification Restrictions:")) {
                    def nsd = getNotifSchedDesc()
                    href "zoneNotifTimePage", title: inTS("Quiet Restrictions", getAppImg("restriction", true)), description: (nsd ? "${nsd}\nTap to modify..." : "Tap to configure"), state: (nsd ? "complete" : null), image: getAppImg("restriction")
                }
                if(!state?.notif_message_tested) {
                    def actDevices = settings?.notif_alexa_mobile ? parent?.getDevicesFromList(settings?.zone_EchoDevices) : []
                    def aMsgDev = actDevices?.size() && settings?.notif_alexa_mobile ? actDevices[0] : null
                    if(sendNotifMsg("Info", "Zone Notification Test Successful. Notifications Enabled for ${app?.getLabel()}", aMsgDev, true)) { state?.notif_message_tested = true }
                }
            } else { state?.notif_message_tested = false }
        } else {
            section() { paragraph pTS("Configure either an Active or Inactive message to configure remaining notification options.", null, false, "#2784D9"), state: "complete" }
        }
    }
}

def zoneNotifTimePage() {
    return dynamicPage(name:"zoneNotifTimePage", title: "", install: false, uninstall: false) {
        def pre = "notif"
        Boolean timeReq = (settings["${pre}_time_start"] || settings["${pre}_time_stop"])
        section(sTS("Start Time:")) {
            input "${pre}_time_start_type", "enum", title: inTS("Starting at...", getAppImg("start_time", true)), options: ["time":"Time of Day", "sunrise":"Sunrise", "sunset":"Sunset"], required: false , submitOnChange: true, image: getAppImg("start_time")
            if(settings?."${pre}_time_start_type" == "time") {
                input "${pre}_time_start", "time", title: inTS("Start time", getAppImg("start_time", true)), required: timeReq, submitOnChange: true, image: getAppImg("start_time")
            } else if(settings?."${pre}_time_start_type" in ["sunrise", "sunrise"]) {
                input "${pre}_time_start_offset", "number", range: "*..*", title: inTS("Offset in minutes (+/-)", getAppImg("start_time", true)), required: false, submitOnChange: true, image: getAppImg("threshold")
            }
        }
        section(sTS("Stop Time:")) {
            input "${pre}_time_stop_type", "enum", title: inTS("Stopping at...", getAppImg("start_time", true)), options: ["time":"Time of Day", "sunrise":"Sunrise", "sunset":"Sunset"], required: false , submitOnChange: true, image: getAppImg("stop_time")
            if(settings?."${pre}_time_stop_type" == "time") {
                input "${pre}_time_stop", "time", title: inTS("Stop time", getAppImg("start_time", true)), required: timeReq, submitOnChange: true, image: getAppImg("stop_time")
            } else if(settings?."${pre}_time_stop_type" in ["sunrise", "sunrise"]) {
                input "${pre}_time_stop_offset", "number", range: "*..*", title: inTS("Offset in minutes (+/-)", getAppImg("start_time", true)), required: false, submitOnChange: true, image: getAppImg("threshold")
            }
        }
        section(sTS("Quiet Days:")) {
            input "${pre}_days", "enum", title: inTS("Only on these week days", getAppImg("day_calendar", true)), multiple: true, required: false, image: getAppImg("day_calendar"), options: weekDaysEnum()
        }
        section(sTS("Quiet Modes:")) {
            input "${pre}_modes", "mode", title: inTS("When these Modes are Active", getAppImg("mode", true)), multiple: true, submitOnChange: true, required: false, image: getAppImg("mode")
        }
    }
}


//********************************************
//              CORE APP METHODS
//********************************************

def installed() {
    logInfo("Installed Event Received...")
    state?.dateInstalled = getDtNow()
    initialize()
}

def updated() {
    logInfo("Updated Event Received...")
    if(state?.dupOpenedByUser == true) { state?.dupPendingSetup = false }
    initialize()
}

def initialize() {
    // logInfo("Initialize Event Received...")
    unsubscribe()
    unschedule()
    if(settings?.duplicateFlag == true && state?.dupPendingSetup == false) {
        settingUpdate("duplicateFlag", "false", "bool")
        state?.remove("dupOpenedByUser")
    } else if(settings?.duplicateFlag == true && state?.dupPendingSetup != false) {
        String newLbl = app?.getLabel() + app?.getLabel()?.toString()?.contains("(Dup)") ? "" : " (Dup)"
        app?.updateLabel(newLbl)
        state?.dupPendingSetup = true
        def dupState = parent?.getDupZoneStateData()
        if(dupState?.size()) {
            dupState?.each {k,v-> state[k] = v }
            parent?.clearDuplicationItems()
        }
        logInfo("Duplicated Zone has been created... Please open Zone and configure to complete setup...")
        return
    }
    state?.isInstalled = true
    updAppLabel()
    runIn(3, "zoneCleanup")
    runIn(7, "subscribeToEvts")
    runEvery1Hour("healthCheck")
    updConfigStatusMap()
    sendZoneStatus()
}

def uninstalled() {
    sendZoneRemoved()
}

String getZoneName() { return settings?.appLbl as String }

private updAppLabel() {
    String newLbl = "${settings?.appLbl} (Z${isPaused() ? " \u275A\u275A" : ""})"?.replaceAll(/(Dup)/, "").replaceAll("\\s"," ")
    if(settings?.appLbl && app?.getLabel() != newLbl) { app?.updateLabel(newLbl) }
}

public guardEventHandler(guardState) {
    if(!state?.alexaGuardState || state?.alexaGuardState != guardState) {
        state?.alexaGuardState = guardState
        def evt = [name: "guard", displayName: "Alexa Guard", value: state?.alexaGuardState, date: new Date(), deviceId: null]
        logTrace( "${evt?.name} Event | Device: ${evt?.displayName} | Value: (${strCapitalize(evt?.value)})")
        if(state?.handleGuardEvents) {
            executeAction(evt, false, "guardEventHandler", false, false)
        }
    }
}

private updConfigStatusMap() {
    Map sMap = atomicState?.configStatusMap ?: [:]
    sMap?.conditions = conditionsConfigured()
    sMap?.devices = devicesConfigured()
    atomicState?.configStatusMap = sMap
}

Boolean devicesConfigured() { return (settings?.zone_EchoDevices) }
private getConfStatusItem(item) { return (atomicState?.configStatusMap?.containsKey(item) && atomicState?.configStatusMap[item] == true) }

private zoneCleanup() {
    // State Cleanup
    List items = []
    items?.each { si-> if(state?.containsKey(si as String)) { state?.remove(si)} }
    //Cleans up unused Zone setting items
    List setItems = ["zone_delay"]
    List setIgn = ["zone_EchoDevices"]
    setItems?.each { sI-> if(settings?.containsKey(sI as String)) { settingRemove(sI as String) } }
}

public triggerInitialize() { runIn(3, "initialize") }

public updatePauseState(Boolean pause) {
    if(settings?.zonePause != pause) {
        logDebug("Received Request to Update Pause State to (${pause})")
        settingUpdate("zonePause", "${pause}", "bool")
        runIn(4, "updated")
    }
}

private healthCheck() {
    // logTrace("healthCheck", true)
    sendZoneStatus()
    if(advLogsActive()) { logsDisable() }
}

private condItemSet(String key) { return (settings?.containsKey("cond_${key}") && settings["cond_${key}"]) }

private subscribeToEvts() {
    if(checkMinVersion()) { logError("CODE UPDATE required to RESUME operation.  No events will be monitored.", true); return; }
    if(isPaused()) { logWarn("Zone is PAUSED... No Events will be subscribed to or scheduled....", true); return; }
    state?.handleGuardEvents = false
    List subItems = ["mode", "alarm", "presence", "motion", "water", "humidity", "temperature", "illuminance", "power", "lock", "shade", "valve", "door", "contact", "acceleration", "switch", "battery", "level"]

    //SCHEDULING
    if (settings?.cond_time_start_type) {
        if(settings?.cond_time_start_type in ["sunrise", "sunset"]) {
            if (settings?.cond_time_start_type == "sunset") { subscribe(location, "sunsetTime", zoneEvtHandler) }
            if (settings?.cond_time_start_type == "sunrise") { subscribe(location, "sunriseTime", zoneEvtHandler) }
        }
        if(settings?.cond_time_start_type == "time") {
            if(settings?.cond_time_start) { schedule(settings?.cond_time_start, zoneTimeStartCondHandler) }
            if(settings?.cond_time_stop) { schedule(settings?.cond_time_stop, zoneTimeStopCondHandler) }
        }
    }

    subItems?.each { si->
        if(settings?."cond_${si}") {
            switch(si as String) {
                case "alarm":
                    subscribe(location, (!isST() ? "hsmStatus" : "alarmSystemStatus"), zoneEvtHandler)
                    break
                case "mode":
                    if(settings?.cond_mode && !settings?.cond_mode_cmd) { settingUpdate("cond_mode_cmd", "are", "enum") }
                    subscribe(location, si as String, zoneEvtHandler)
                    break
                default:
                    subscribe(settings?."cond_${si}", attributeConvert(si as String), zoneEvtHandler)
                    break
            }
        }
    }
    // Subscribes to Zone Location Command from Other Echo Speaks apps.
    subscribe(location, "es3ZoneCmd", zoneCmdHandler)
    subscribe(location, "es3ZoneRefresh", zoneRefreshHandler)
}

String attributeConvert(String attr) {
    Map atts = ["door":"garageDoorControl", "shade":"windowShade"]
    return (atts?.containsKey(attr as String)) ? atts[attr as String] : attr as String
}

/***********************************************************************************************************
   CONDITIONS HANDLER
************************************************************************************************************/
Boolean reqAllCond() { return (multipleConditions() && settings?.cond_require_all == true)}

Boolean timeCondOk() {
    def startTime = null
    def stopTime = null
    def now = new Date()
    def sun = getSunriseAndSunset() // current based on geofence, previously was: def sun = getSunriseAndSunset(zipCode: zipCode)
    if(settings?.cond_time_start_type && settings?.cond_time_stop_type) {
        if(settings?.cond_time_start_type == "sunset") { startTime = sun?.sunset }
        else if(settings?.cond_time_start_type == "sunrise") { startTime = sun?.sunrise }
        else if(settings?.cond_time_start_type == "time" && settings?.cond_time_start) { startTime = settings?.cond_time_start }

        if(settings?.cond_time_stop_type == "sunset") { stopTime = sun?.sunset }
        else if(settings?.cond_time_stop_type == "sunrise") { stopTime = sun?.sunrise }
        else if(settings?.cond_time_stop_type == "time" && settings?.cond_time_stop) { stopTime = settings?.cond_time_stop }
    } else { return null }
    if(startTime && stopTime) {
        if(!isST()) {
            startTime = toDateTime(startTime)
            stopTime = toDateTime(stopTime)
        }
        return timeOfDayIsBetween(startTime, stopTime, new Date(), location?.timeZone)
    } else { return null }
}

Boolean dateCondOk() {
    if(settings?.cond_days == null && settings?.cond_months == null) return null
    Boolean dOk = settings?.cond_days ? (isDayOfWeek(settings?.cond_days)) : true
    Boolean mOk = settings?.cond_months ? (isMonthOfYear(settings?.cond_months)) : true
    logDebug("dateConditions | monthOk: $mOk | daysOk: $dOk")
    return reqAllCond() ? (mOk && dOk) : (mOk || dOk)
}

Boolean locationCondOk() {
    if(settings?.cond_mode == null && settings?.cond_mode_cmd == null && settings?.cond_alarm == null) return null
    Boolean mOk = (settings?.cond_mode && settings?.cond_mode_cmd) ? (isInMode(settings?.cond_mode, (settings?.cond_mode_cmd == "not"))) : true
    Boolean aOk = settings?.cond_alarm ? isInAlarmMode(settings?.cond_alarm) : true
    logDebug("locationConditions | modeOk: $mOk | alarmOk: $aOk")
    return reqAllCond() ? (mOk && aOk) : (mOk || aOk)
}

Boolean checkDeviceCondOk(type) {
    List devs = settings?."cond_${type}" ?: null
    String cmdVal = settings?."cond_${type}_cmd" ?: null
    Boolean all = (settings?."cond_${type}_all" == true)
    if( !(type && devs && cmdVal) ) { return true }
    return all ? allDevCapValsEqual(devs, type, cmdVal) : anyDevCapValsEqual(devs, type, cmdVal)
}

Boolean checkDeviceNumCondOk(type) {
    List devs = settings?."cond_${type}" ?: null
    String cmd = settings?."cond_${type}_cmd" ?: null
    Double dcl = settings?."cond_${type}_low" ?: null
    Double dch = settings?."cond_${type}_high" ?: null
    Double dce = settings?."cond_${type}_equal" ?: null
    Boolean dca = (settings?."cond_${type}_all" == true) ?: false
    if( !(type && devs && cmd) ) { return true }

    switch(cmd) {
        case "equals":
            if(dce) {
                if(dca) { return allDevCapNumValsEqual(devs, type, dce) }
                else { return anyDevCapNumValEqual(devs, type, dce) }
            }
            return true
            break
        case "between":
            if(dcl && dch) {
                if(dca) { return allDevCapNumValsBetween(devs, type, dcl, dch) }
                else { return anyDevCapNumValBetween(devs, type, dcl, dch) }
            }
            return true
            break
        case "above":
            if(dch) {
                if(dca) { return allDevCapNumValsAbove(devs, type, dch) }
                else { return anyDevCapNumValAbove(devs, type, dch) }
            }
            return true
            break
        case "below":
            if(dcl) {
                if(dca) { return allDevCapNumValsBelow(devs, type, dcl) }
                else { return anyDevCapNumValBelow(devs, type, dcl) }
            }
            return true
            break
    }
}

private isConditionOk(evt) {
    if(["switch", "motion", "presence", "contact", "acceleration", "lock", "door", "shade", "valve"]?.contains(evt)) {
        if(!settings?."cond_${evt}") { true }
        return checkDeviceCondOk(evt)
    } else if(["temperature", "humidity", "illuminance", "level", "power", "battery"]?.contains(evt)) {
        if(!settings?."cond_${evt}") { true }
        return checkDeviceNumCondOk(evt)
    } else if (evt == "mode") {
        return (settings?.cond_mode && settings?.cond_mode_cmd) ? (isInMode(settings?.cond_mode, (settings?.cond_mode_cmd == "not"))) : true
    } else if (["hsmStatus", "alarmSystemStatus"]?.contains(evt)) {
        return settings?.cond_alarm ? isInAlarmMode(settings?.cond_alarm) : true
    } else {
        return true
    }
}

Boolean deviceCondOk() {
    List skipped = []
    List passed = []
    List failed = []
    ["switch", "motion", "presence", "contact", "acceleration", "lock", "door", "shade", "valve"]?.each { i->
        if(!settings?."cond_${i}") { skipped?.push(i); return; }
        checkDeviceCondOk(i) ? passed?.push(i) : failed?.push(i);
    }
    ["temperature", "humidity", "illuminance", "level", "power", "battery"]?.each { i->
        if(!settings?."cond_${i}") { skipped?.push(i); return; }
        checkDeviceNumCondOk(i) ? passed?.push(i) : failed?.push(i);
    }
    logDebug("DeviceCondOk | Found: (${(passed?.size() + failed?.size())}) | Skipped: $skipped | Passed: $passed | Failed: $failed")
    Integer cndSize = (passed?.size() + failed?.size())
    if(cndSize == 0) return null
    return reqAllCond() ? (cndSize == passed?.size()) : (cndSize > 0 && passed?.size() >= 1)
}

def conditionStatus() {
    Boolean reqAll = reqAllCond()
    List failed = []
    List passed = []
    List skipped = []
    ["time", "date", "location", "device"]?.each { i->
        def s = "${i}CondOk"()
        if(s == null) { skipped?.push(i); return; }
        s ? passed?.push(i) : failed?.push(i);
    }
    Integer cndSize = (passed?.size() + failed?.size())
    logDebug("ConditionsStatus | RequireAll: ${reqAll} | Found: (${cndSize}) | Skipped: $skipped | Passed: $passed | Failed: $failed")
    Boolean ok = reqAll ? (cndSize == passed?.size()) : (cndSize > 0 && passed?.size() >= 1)
    if(cndSize == 0) ok = true;
    return [ok: ok, passed: passed, blocks: failed]
}

Boolean devCondConfigured(type) {
    return (settings?."cond_${type}" && settings?."cond_${type}_cmd")
}

Boolean devNumCondConfigured(type) {
    return (settings?."cond_${type}_cmd" && (settings?."cond_${type}_low" || settings?."cond_${type}_low" || settings?."trig_${type}_equal"))
}

Boolean timeCondConfigured() {
    Boolean startTime = (settings?.cond_time_start_type in ["sunrise", "sunset"] || (settings?.cond_time_start_type == "time" && settings?.cond_time_start))
    Boolean stopTime = (settings?.cond_time_stop_type in ["sunrise", "sunset"] || (settings?.cond_time_stop_type == "time" && settings?.cond_time_stop))
    return (startTime && stopTime)
}

Boolean dateCondConfigured() {
    Boolean days = (settings?.cond_days)
    Boolean months = (settings?.cond_months)
    return (days || months)
}

Boolean locationCondConfigured() {
    if(settings?.cond_mode && !settings?.cond_mode_cmd) { settingUpdate("cond_mode_cmd", "are", "enum") }
    Boolean mode = (settings?.cond_mode && settings?.cond_mode_cmd)
    Boolean alarm = (settings?.cond_alarm)
    return (mode || alarm)
}

Boolean deviceCondConfigured() {
    List devConds = ["switch", "motion", "presence", "contact", "acceleration", "lock", "door", "shade", "valve", "temperature", "humidity", "illuminance", "level", "power", "battery"]
    List items = []
    devConds?.each { dc-> if(devCondConfigured(dc)) { items?.push(dc) } }
    return (items?.size() > 0)
}

Integer deviceCondCount() {
    List devConds = ["switch", "motion", "presence", "contact", "acceleration", "lock", "door", "shade", "valve", "temperature", "humidity", "illuminance", "level", "power", "battery"]
    List items = []
    devConds?.each { dc-> if(devCondConfigured(dc)) { items?.push(dc) } }
    return items?.size() ?: 0
}

Boolean conditionsConfigured() {
    return (timeCondConfigured() || dateCondConfigured() || locationCondConfigured() || deviceCondConfigured())
}

Boolean multipleConditions() {
    Integer cnt = 0
    if(timeCondConfigured()) cnt++
    if(dateCondConfigured()) cnt++
    if(locationCondConfigured()) cnt++
    cnt = cnt + deviceCondCount()
    return (cnt>1)
}

/***********************************************************************************************************
    EVENT HANDLER FUNCTIONS
************************************************************************************************************/

def zoneEvtHandler(evt) {
    logTrace( "${evt?.name} Event | Device: ${evt?.displayName} | Value: (${strCapitalize(evt?.value)}) with a delay of ${now() - evt?.date?.getTime()}ms")
    checkZoneStatus(evt)
}

def zoneTimeStartCondHandler() {
    checkZoneStatus([name: "Time", displayName: "Condition Start Time"])
}

def zoneTimeStopCondHandler() {
    checkZoneStatus([name: "Time", displayName: "Condition Stop Time"])
}

private addToZoneHistory(evt, condStatus, Integer max=10) {
    Boolean ssOk = (stateSizePerc() <= 70)
    List eData = atomicState?.zoneHistory ?: []
    eData?.push([dt: getDtNow(), active: (condStatus?.ok == true), evtName: evt?.name, evtDevice: evt?.displayName, blocks: condStatus?.blocks, passed: condStatus?.passed])
    if(!ssOk || eData?.size() > max) { eData = eData?.drop( (eData?.size()-max)+1 ) }
    atomicState?.zoneHistory = eData
}

def checkZoneStatus(evt) {
    Map condStatus = conditionStatus()
    Boolean active = (condStatus?.ok == true)
    Boolean bypassDelay = false
    String delayType = active ? "active" : "inactive"
    Map data = [active: active, recheck: false, evtData: [name: evt?.name, displayName: evt?.displayName], condStatus: condStatus]
    Integer delay = settings?."zone_${delayType}_delay" ?: null
    if(!active && settings?."cond_${evt?.name}_db" == true) { bypassDelay = isConditionOk(evt?.name) != true }
    if(!bypassDelay && delay) {
        log.debug "updateZoneStatus to [${delayType}] in (${delay} sec)"
        runIn(delay, "updateZoneStatus", [data: data])
    } else {
        // log.debug "updateZoneStatus to [${delayType}]"
        if(bypassDelay) {
            log.debug "Bypassing Inactive Delay for (${evt?.name}) Event..."
        }
        updateZoneStatus(data)
    }
}

def sendZoneStatus() {
    Boolean active = (conditionStatus()?.ok == true)
    // state?.zoneConditionsOk = active
    sendLocationEvent(name: "es3ZoneState", value: app?.getId(), data:[name: getZoneName(), active: active], isStateChange: true, display: false, displayed: false)
}

def sendZoneRemoved() {
    sendLocationEvent(name: "es3ZoneRemoved", value: app?.getId(), data:[name: getZoneName()], isStateChange: true, display: false, displayed: false)
}

def updateZoneStatus(data) {
    Boolean active = (data?.active == true)
    Map condStatus = data?.condStatus ?: null
    if(data?.recheck == true) {
        condStatus = conditionStatus()
        active = (condStatus?.ok == true)
    }
    if(state?.zoneConditionsOk != active) {
        log.debug("Setting Zone (${getZoneName()}) Status to (${active ? "Active" : "Inactive"})")
        state?.zoneConditionsOk = active
        addToZoneHistory(data?.evtData, condStatus)
        sendLocationEvent(name: "es3ZoneState", value: app?.getId(), data: [ name: getZoneName(), active: active ], isStateChange: true, display: false, displayed: false)
        if(isZoneNotifConfigured()) {
            Boolean ok2Send = true
            String msgTxt = null
            if(active) {
                msgTxt = settings?.notif_active_message ?: null
            } else { msgTxt = settings?.notif_inactive_message ?: null }
            if(ok2Send && msgTxt) {
                def zoneDevices = getZoneDevices()
                def alexaMsgDev = zoneDevices?.size() && settings?.notif_alexa_mobile ? zoneDevices[0] : null
                if(sendNotifMsg(getZoneName() as String, msgTxt as String, alexaMsgDev, false)) { logDebug("Sent Zone Notification...") }
            }
        }
        if(active) {
            if(settings?.zone_active_switches_off) settings?.zone_active_switches_off?.off()
            if(settings?.zone_active_switches_on) settings?.zone_active_switches_on?.on()
        } else {
            if(settings?.zone_inactive_switches_off) settings?.zone_inactive_switches_off?.off()
            if(settings?.zone_inactive_switches_on) settings?.zone_inactive_switches_on?.on()
        }
    }
}

public getZoneHistory(asObj=false) {
    List zHist = atomicState?.zoneHistory ?: []
    Boolean isST = isST()
    List output = []
    if(zHist?.size()) {
        zHist?.each { h->
            String str = ""
            str += "Trigger: [${h?.evtName}]"
            str += "\nDevice: [${h?.evtDevice}]"
            str += "\nZone Status: ${h?.active ? "Activate" : "Deactivate"}"
            str += "\nConditions Passed: ${h?.passed}"
            str += "\nConditions Blocks: ${h?.blocks}"
            str += "\nDateTime: ${h?.dt}"
            output?.push(str)
        }
    } else { output?.push("No History Items Found...") }
    if(!asObj) {
        output?.each { i-> paragraph pTS(i) }
    } else { return output }
}

Map getZoneDevices() {
    List devObj = []
    List devices = parent?.getDevicesFromList(settings?.zone_EchoDevices)
    devices?.each { devObj?.push([deviceTypeId: it?.getEchoDeviceType() as String, deviceSerialNumber: it?.getEchoSerial() as String]) }
    return [devices: devices, devObj: devObj]//, jsonStr: new groovy.json.JsonOutput().toJson(devObj)]
}

public zoneRefreshHandler(evt) {
    String cmd = evt?.value;
    Map data = evt?.jsonData;
    switch(cmd) {
        case "checkStatus":
            checkZoneStatus()
            break
        case "sendStatus":
            sendZoneStatus()
            break
    }
}

public zoneCmdHandler(evt) {
    String cmd = evt?.value;
    Map data = evt?.jsonData;
    String appId = app?.getId() as String
    if(cmd && data && appId && data?.zones && data?.zones?.contains(appId) && data?.cmd) {
        // log.trace "zoneCmdHandler | Cmd: $cmd | Data: $data"
        def zoneDevs = getZoneDevices()
        Integer delay = data?.delay ?: null
        if(cmd == "speak" && zoneDevs?.devices?.size() >= 2) { cmd = "announcement" }
        switch(cmd) {
            case "speak":
                if(!data?.message) { logWarning("Zone Command Message is missing", true); return; }
                logDebug("Sending Speak Command: (${data?.message}) to Zone (${getZoneName()})${data?.changeVol ? " | Volume: ${data?.changeVol}" : ""}${data?.restoreVol ? " | Restore Volume: ${data?.restoreVol}" : ""}${delay ? " | Delay: (${delay})" : ""}")
                if(data?.changeVol || data?.restoreVol) {
                    zoneDevs?.devices?.each { dev->
                        if(isST() && delay) {
                            dev?.setVolumeSpeakAndRestore(data?.changeVol, data?.message, data?.restoreVol, [delay: delay])
                        } else { dev?.setVolumeSpeakAndRestore(data?.changeVol, data?.message, data?.restoreVol) }
                    }
                } else {
                    zoneDevs?.devices?.each { dev->
                        if(isST() && delay) {
                            dev?.speak(data?.message, [delay: delay])
                        } else { dev?.speak(data?.message) }
                    }
                }
                break
            case "announcement":
                if(zoneDevs?.devices?.size() > 0 && zoneDevs?.devObj) {
                    logDebug("Sending Announcement Command: (${data?.message}) to Zone (${getZoneName()})${data?.changeVol ? " | Volume: ${data?.changeVol}" : ""}${data?.restoreVol ? " | Restore Volume: ${data?.restoreVol}" : ""}${delay ? " | Delay: (${delay})" : ""}")
                    //NOTE: Only sends command to first device in the list | We send the list of devices to announce one and then Amazon does all the processing
                    if(isST() && delay) {
                        zoneDevs?.devices[0]?.sendAnnouncementToDevices(data?.message, (data?.title ?: getZoneName()), zoneDevs?.devObj, data?.changeVol, data?.restoreVol, [delay: delay])
                    } else { zoneDevs?.devices[0]?.sendAnnouncementToDevices(data?.message, (data?.title ?: getZoneName()), zoneDevs?.devObj, data?.changeVol, data?.restoreVol) }
                }
                break
            case "sequence":
                logDebug("Sending Sequence Command: (${data?.message}) to Zone (${getZoneName()})${delay ? " | Delay: (${delay})" : ""}")
                zoneDevs?.devices?.each { dev->
                    if(isST() && delay) {
                        dev?.executeSequenceCommand(data?.message, [delay: delay])
                    } else { dev?.executeSequenceCommand(data?.message) }
                }
                break
            case "builtin":
            case "calendar":
            case "weather":
            case "playback":
                log.debug("Sending ${data?.cmd?.toString()?.capitalize()} Command to Zone (${getZoneName()})${data?.changeVol ? " | Volume: ${data?.changeVol}" : ""}${data?.restoreVol ? " | Restore Volume: ${data?.restoreVol}" : ""}${delay ? " | Delay: (${delay})" : ""}")
                zoneDevs?.devices?.each { dev->
                    if(isST() && delay) {
                        if(data?.cmd != "volume") { dev?."${data?.cmd}"(data?.changeVol ?: null, data?.restoreVol ?: null, [delay: delay]) }
                        if(data?.cmd == "volume" && data?.changeVol) { dev?.setVolume(data?.changeVol, [delay: delay]) }
                    } else {
                        if(data?.cmd != "volume") { dev?."${data?.cmd}"(data?.changeVol ?: null, data?.restoreVol ?: null) }
                        if(data?.cmd == "volume" && data?.changeVol) { dev?.setVolume(data?.changeVol) }
                    }
                }
                break
            case "sounds":
                log.debug("Sending ${data?.cmd?.toString()?.capitalize()} | Name: ${data?.message} Command to Zone (${getZoneName()})${data?.changeVol ? " | Volume: ${data?.changeVol}" : ""}${data?.restoreVol ? " | Restore Volume: ${data?.restoreVol}" : ""}${delay ? " | Delay: (${delay})" : ""}")
                zoneDevs?.devices?.each { dev->
                    if(isST() && delay) {
                        dev?."${data?.cmd}"(data?.message, data?.changeVol ?: null, data?.restoreVol ?: null, [delay: delay])
                    } else {
                        dev?."${data?.cmd}"(data?.message, data?.changeVol ?: null, data?.restoreVol ?: null)
                    }
                }
                break
            case "music":
                logDebug("Sending ${data?.cmd?.toString()?.capitalize()} Command to Zone (${getZoneName()}) | Provider: ${data?.provider} | Search: ${data?.search}${delay ? " | Delay: (${delay})" : ""}${data?.changeVol ? " | Volume: ${data?.changeVol}" : ""}${data?.restoreVol ? " | Restore Volume: ${data?.restoreVol}" : ""}")
                if(isST() && delay) {
                    dev?."${data?.cmd}"(data?.search, data?.provider, data?.changeVol, data?.restoreVol, [delay: delay])
                } else {
                    dev?."${data?.cmd}"(data?.search, data?.provider, data?.changeVol, data?.restoreVol)
                }
                break
        }
    }
}
/******************************************
|   Restriction validators
*******************************************/

String attUnit(attr) {
    switch(attr) {
        case "humidity":
        case "level":
        case "battery":
            return " percent"
        case "temperature":
            return " degrees"
        case "illuminance":
            return " lux"
        case "power":
            return " watts"
        default:
            return ""
    }
}

Double getDevValueAvg(devs, attr) {
    List vals = devs?.findAll { it?."current${attr?.capitalize()}"?.isNumber() }?.collect { it?."current${attr?.capitalize()}" as Double }
    return vals?.size() ? (vals?.sum()/vals?.size())?.round(1) as Double : null
}

String getCurrentMode() {
    return location?.mode
}

List getLocationModes(Boolean sorted=false) {
    List modes = location?.modes*.name
    // log.debug "modes: ${modes}"
    return (sorted) ? modes?.sort() : modes
}

List getLocationRoutines() {
    return (isST()) ? location.helloHome?.getPhrases()*.label?.sort() : []
}

Boolean isInMode(modes, not=false) {
    return (modes) ? (not ? (!(getCurrentMode() in modes)) : (getCurrentMode() in modes)) : false
}

Boolean isInAlarmMode(modes) {
    return (modes) ? (parent?.getAlarmSystemStatus() in modes) : false
}

Boolean areAllDevsSame(List devs, String attr, val) {
    if(devs && attr && val) { return (devs?.findAll { it?.currentValue(attr) == val as String }?.size() == devs?.size()) }
    return false
}

Boolean allDevCapValsEqual(List devs, String cap, val) {
    if(devs) { return (devs?.findAll { it?."current${cap?.capitalize()}" == val }?.size() == devs?.size()) }
    return false
}

Boolean anyDevCapValsEqual(List devs, String cap, val) {
    return (devs && cap && val) ? (devs?.findAll { it?."current${cap?.capitalize()}" == val }?.size() >= 1) : false
}

Boolean anyDevCapNumValAbove(List devs, String cap, val) {
    return (devs && cap && val) ? (devs?.findAll { it?."current${cap?.capitalize()}"?.toDouble() > val?.toDouble() }?.size() >= 1) : false
}
Boolean anyDevCapNumValBelow(List devs, String cap, val) {
    return (devs && cap && val) ? (devs?.findAll { it?."current${cap?.capitalize()}"?.toDouble() < val?.toDouble() }?.size() >= 1) : false
}
Boolean anyDevCapNumValBetween(List devs, String cap, low, high) {
    return (devs && cap && low && high) ? (devs?.findAll { ( (it?."current${cap?.capitalize()}"?.toDouble() > low?.toDouble()) && (it?."current${cap?.capitalize()}"?.toDouble() < high?.toDouble()) ) }?.size() >= 1) : false
}
Boolean anyDevCapNumValEqual(List devs, String cap, val) {
    return (devs && cap && val) ? (devs?.findAll { it?."current${cap?.capitalize()}"?.toDouble() == val?.toDouble() }?.size() >= 1) : false
}

Boolean allDevCapNumValsAbove(List devs, String cap, val) {
    return (devs && cap && val) ? (devs?.findAll { it?."current${cap?.capitalize()}"?.toDouble() > val?.toDouble() }?.size() == devs?.size()) : false
}
Boolean allDevCapNumValsBelow(List devs, String cap, val) {
    return (devs && cap && val) ? (devs?.findAll { it?."current${cap?.capitalize()}"?.toDouble() < val?.toDouble() }?.size() == devs?.size()) : false
}
Boolean allDevCapNumValsBetween(List devs, String cap, low, high) {
    return (devs && cap && low && high) ? (devs?.findAll { ( (it?."current${cap?.capitalize()}"?.toDouble() > low?.toDouble()) && (it?."current${cap?.capitalize()}"?.toDouble() < high?.toDouble()) ) }?.size() == devs?.size()) : false
}
Boolean allDevCapNumValsEqual(List devs, String cap, val) {
    return (devs && cap && val) ? (devs?.findAll { it?."current${cap?.capitalize()}"?.toDouble() == val?.toDouble() }?.size() == devs?.size()) : false
}

Boolean devCapValEqual(List devs, String devId, String cap, val) {
    if(devs) { return (devs?.find { it?."current${cap?.capitalize()}" == val }) }
    return false
}

String getAlarmSystemName(abbr=false) {
    return isST() ? (abbr ? "SHM" : "Smart Home Monitor") : (abbr ? "HSM" : "Hubitat Safety Monitor")
}

public Map getZoneMetrics() {
    Map out = [:]
    out?.version = appVersion()
    out?.activeDelay = settings?.zone_active_delay ?: 0
    out?.inactiveDelay = settings?.zone_inactive_delay ?: 0
    out?.zoneDevices = settings?.zone_EchoDevices ?: []
    out?.activeSwitchesOnCnt = settings?.zone_active_switches_on ?: []
    out?.activeSwitchesOffCnt = settings?.zone_active_switches_off ?: []
    out?.inactiveSwitchesOnCnt = settings?.zone_inactive_switches_on ?: []
    out?.inactiveSwitchesOffCnt = settings?.zone_inactive_switches_off ?: []
    return out
}
/******************************************
|    Time and Date Conversion Functions
*******************************************/
def formatDt(dt, tzChg=true) {
    def tf = new java.text.SimpleDateFormat("E MMM dd HH:mm:ss z yyyy")
    if(tzChg) { if(location.timeZone) { tf.setTimeZone(location?.timeZone) } }
    return tf?.format(dt)
}

def dateTimeFmt(dt, fmt) {
    if(!(dt instanceof Date)) { try { dt = Date.parse("yyyy-MM-dd'T'HH:mm:ss.SSSZ", dt?.toString()) } catch(e) { dt = Date.parse("E MMM dd HH:mm:ss z yyyy", dt?.toString()) } }
    def tf = new java.text.SimpleDateFormat(fmt)
    if(location?.timeZone) { tf.setTimeZone(location?.timeZone) }
    return tf?.format(dt)
}

def convToTime(dt) {
    def newDt = dateTimeFmt(dt, "h:mm a")
    if(newDt?.toString()?.contains(":00 ")) { newDt?.toString()?.replaceAll(":00 ", " ") }
    return newDt
}

def convToDate(dt) {
    def newDt = dateTimeFmt(dt, "EEE, MMM d")
    return newDt
}

def convToDateTime(dt) {
    def t = dateTimeFmt(dt, "h:mm a")
    def d = dateTimeFmt(dt, "EEE, MMM d")
    return "$d, $t"
}

Date parseDate(dt) { return Date.parse("E MMM dd HH:mm:ss z yyyy", dt?.toString()) }
Boolean isDateToday(Date dt) { return (dt && dt?.clearTime().compareTo(new Date()?.clearTime()) >= 0) }
String strCapitalize(str) { return str ? str?.toString().capitalize() : null }
String pluralizeStr(obj, para=true) { return (obj?.size() > 1) ? "${para ? "(s)": "s"}" : "" }

def parseDt(pFormat, dt, tzFmt=true) {
    def result
    def newDt = Date.parse("$pFormat", dt)
    result = formatDt(newDt, tzFmt)
    //log.debug "parseDt Result: $result"
    return result
}

def getDtNow() {
    def now = new Date()
    return formatDt(now)
}

def epochToTime(tm) {
    def tf = new java.text.SimpleDateFormat("h:mm a")
    if(location?.timeZone) { tf?.setTimeZone(location?.timeZone) }
    return tf.format(tm)
}

def time2Str(time) {
    if(time) {
        def t = timeToday(time as String, location?.timeZone)
        def f = new java.text.SimpleDateFormat("h:mm a")
        if(location?.timeZone) { tf?.setTimeZone(location?.timeZone) }
        return f?.format(t)
    }
}

def fmtTime(t, fmt="h:mm a", altFmt=false) {
    if(!t) return null
    def dt = new Date().parse(altFmt ? "E MMM dd HH:mm:ss z yyyy" : "yyyy-MM-dd'T'HH:mm:ss.SSSZ", t?.toString())
    def tf = new java.text.SimpleDateFormat(fmt as String)
    if(location?.timeZone) { tf?.setTimeZone(location?.timeZone) }
    return tf?.format(dt)
}

def GetTimeDiffSeconds(lastDate, sender=null) {
    try {
        if(lastDate?.contains("dtNow")) { return 10000 }
        def now = new Date()
        def lastDt = Date.parse("E MMM dd HH:mm:ss z yyyy", lastDate)
        def start = Date.parse("E MMM dd HH:mm:ss z yyyy", formatDt(lastDt)).getTime()
        def stop = Date.parse("E MMM dd HH:mm:ss z yyyy", formatDt(now)).getTime()
        def diff = (int) (long) (stop - start) / 1000
        return diff
    }
    catch (ex) {
        logError("GetTimeDiffSeconds Exception: (${sender ? "$sender | " : ""}lastDate: $lastDate): ${ex?.message}")
        return 10000
    }
}

def getWeekDay() {
    def df = new java.text.SimpleDateFormat("EEEE")
    df.setTimeZone(location?.timeZone)
    return df.format(new Date())
}

def getWeekMonth() {
    def df = new java.text.SimpleDateFormat("W")
    df.setTimeZone(location?.timeZone)
    return df.format(new Date())
}

def getDay() {
    def df = new java.text.SimpleDateFormat("D")
    df.setTimeZone(location?.timeZone)
    return df.format(new Date())
}

def getYear() {
    def df = new java.text.SimpleDateFormat("yyyy")
    df.setTimeZone(location?.timeZone)
    return df.format(new Date())
}

def getMonth() {
    def df = new java.text.SimpleDateFormat("MMMMM")
    df.setTimeZone(location?.timeZone)
    return df.format(new Date())
}

def getWeekYear() {
    def df = new java.text.SimpleDateFormat("w")
    df.setTimeZone(location?.timeZone)
    return df.format(new Date())
}

Map getDateMap() {
    return [d: getWeekDay(), dm: getDay(), wm: getWeekMonth(), wy: getWeekYear(), m: getMonth(), y: getYear() ]
}

Boolean isDayOfWeek(opts) {
    def df = new java.text.SimpleDateFormat("EEEE")
    df.setTimeZone(location?.timeZone)
    def day = df.format(new Date())
    return ( opts?.contains(day) )
}

Boolean isTimeOfDay(startTime, stopTime) {
    if(!startTime && !stopTime) { return true }
    if(!isST()) { startTime = toDateTime(startTime); stopTime = toDateTime(stopTime); }
    return timeOfDayIsBetween(startTime, stopTime, new Date(), location.timeZone)
}

Boolean advLogsActive() { return (settings?.logDebug || settings?.logTrace) }
public logsEnabled() { if(advLogsActive() && getTsVal("logsEnabled")) { updTsVal("logsEnabled") } }
public logsDisable() { Integer dtSec = getLastTsValSecs("logsEnabled", null); if(dtSec && (dtSec > 3600*6) && advLogsActive()) { settingUpdate("logDebug", "false", "bool"); settingUpdate("logTrace", "false", "bool"); remTsVal("logsEnabled"); } }

private updTsVal(key, dt=null) {
	def data = atomicState?.tsDtMap ?: [:]
	if(key) { data[key] = dt ?: getDtNow() }
	atomicState?.tsDtMap = data
}

private remTsVal(key) {
	def data = atomicState?.tsDtMap ?: [:]
    if(key) {
        if(key instanceof List) {
            key?.each { k-> if(data?.containsKey(k)) { data?.remove(k) } }
        } else { if(data?.containsKey(key)) { data?.remove(key) } }
        atomicState?.tsDtMap = data
    }
}

def getTsVal(val) {
	def tsMap = atomicState?.tsDtMap
	if(val && tsMap && tsMap[val]) { return tsMap[val] }
	return null
}

private updAppFlag(key, val) {
	def data = atomicState?.appFlagsMap ?: [:]
	if(key) { data[key] = val }
	atomicState?.appFlagsMap = data
}

private remAppFlag(key) {
	def data = atomicState?.appFlagsMap ?: [:]
    if(key) {
        if(key instanceof List) {
            key?.each { k-> if(data?.containsKey(k)) { data?.remove(k) } }
        } else { if(data?.containsKey(key)) { data?.remove(key) } }
        atomicState?.appFlagsMap = data
    }
}

Boolean getAppFlag(val) {
    def aMap = atomicState?.appFlagsMap
    if(val && aMap && aMap[val]) { return aMap[val] }
    return false
}

private stateMapMigration() {
    //Timestamp State Migrations
    Map tsItems = [:]
    tsItems?.each { k, v-> if(state?.containsKey(k)) { updTsVal(v as String, state[k as String]); state?.remove(k as String); } }

    //App Flag Migrations
    Map flagItems = [:]
    flagItems?.each { k, v-> if(state?.containsKey(k)) { updAppFlag(v as String, state[k as String]); state?.remove(k as String); } }
    updAppFlag("stateMapConverted", true)
}

Integer getLastTsValSecs(val, nullVal=1000000) {
	def tsMap = atomicState?.tsDtMap
	return (val && tsMap && tsMap[val]) ? GetTimeDiffSeconds(tsMap[val]).toInteger() : nullVal
}

/******************************************
|   App Input Description Functions
*******************************************/

String unitStr(type) {
    switch(type) {
        case "temp":
            return "\u00b0${getTemperatureScale() ?: "F"}"
        case "humidity":
        case "battery":
            return "%"
        default:
            return ""
    }
}

String getAppNotifDesc(hide=false) {
    String str = ""
    if(isZoneNotifConfigured()) {
        str += hide ? "" : "Send To:\n"
        str += settings?.notif_sms_numbers ? " \u2022 (${settings?.notif_sms_numbers?.tokenize(",")?.size()} SMS Numbers)\n" : ""
        str += settings?.notif_send_push ? " \u2022 (Push Message)\n" : ""
        str += (settings?.notif_pushover && settings?.notif_pushover_devices?.size()) ? " \u2022 Pushover Device${pluralizeStr(settings?.notif_pushover_devices)} (${settings?.notif_pushover_devices?.size()})\n" : ""
        str += settings?.notif_alexa_mobile ? " \u2022 Alexa Mobile App\n" : ""
        str += getNotifSchedDesc(true) ? " \u2022 Restrictions: (${getOk2Notify() ? "${okSym()}" : "${notOkSym()}"})\n" : ""
    }
    return str != "" ? str : null
}

List getQuietDays() {
	List allDays = weekDaysEnum()
	List curDays = settings?.notif_days ?: []
	return allDays?.findAll { (!curDays?.contains(it as String)) }
}

String getNotifSchedDesc(min=false) {
    def sun = getSunriseAndSunset()
    def startInput = settings?.notif_time_start_type
    def startTime = settings?.notif_time_start
    def stopInput = settings?.notif_time_stop_type
    def stopTime = settings?.notif_time_stop
    def dayInput = settings?.notif_days
    def modeInput = settings?.notif_modes
    def str = ""
    def startLbl = ( (startInput == "Sunrise" || startInput == "Sunset") ? ( (startInput == "Sunset") ? epochToTime(sun?.sunset?.time) : epochToTime(sun?.sunrise?.time) ) : (startTime ? time2Str(startTime) : "") )
    def stopLbl = ( (stopInput == "Sunrise" || stopInput == "Sunset") ? ( (stopInput == "Sunset") ? epochToTime(sun?.sunset?.time) : epochToTime(sun?.sunrise?.time) ) : (stopTime ? time2Str(stopTime) : "") )
    str += (startLbl && stopLbl) ? " \u2022 Time: ${startLbl} - ${stopLbl}" : ""
    def qDays = getQuietDays()
    str += dayInput ? "${(startLbl || stopLbl) ? "\n" : ""} \u2022 Day${pluralizeStr(dayInput, false)}:${min ? " (${qDays?.size()} selected)" : "\n    - ${qDays?.join("\n    - ")}"}" : ""
    str += modeInput ? "${(startLbl || stopLbl || qDays) ? "\n" : ""} \u2022 Mode${pluralizeStr(modeInput, false)}:${min ? " (${modeInput?.size()} selected)" : "\n    - ${modeInput?.join("\n    - ")}"}" : ""
    return (str != "") ? "${str}" : null
}

String getConditionsDesc() {
    Boolean confd = conditionsConfigured()
    def time = null
    String sPre = "cond_"
    if(confd) {
        String str = "Conditions: (${(conditionStatus()?.ok == true) ? "${okSym()}" : "${notOkSym()}"})\n"
        str += (settings?.cond_require_all != true) ? " \u2022 Any Condition Allowed\n" : " \u2022 All Conditions Required\n"
        if(timeCondConfigured()) {
            str += " \u2022 Time Between: (${timeCondOk() ? "${okSym()}" : "${notOkSym()}"})\n"
            str += "    - ${getTimeCondDesc(false)}\n"
        }
        if(dateCondConfigured()) {
            str += " \u2022 Date:\n"
            str += settings?.cond_days      ? "    - Days: (${(isDayOfWeek(settings?.cond_days)) ? "${okSym()}" : "${notOkSym()}"})\n" : ""
            str += settings?.cond_months    ? "    - Months: (${(isMonthOfYear(settings?.cond_months)) ? "${okSym()}" : "${notOkSym()}"})\n"  : ""
        }
        if(settings?.cond_alarm || settings?.cond_mode) {
            str += " \u2022 Location: (${locationCondOk() ? "${okSym()}" : "${notOkSym()}"})\n"
            str += settings?.cond_alarm ? "    - Alarm Modes: (${(isInAlarmMode(settings?.cond_alarm)) ? "${okSym()}" : "${notOkSym()}"})\n" : ""
            str += settings?.cond_mode ? "    - Location Modes: (${(isInMode(settings?.cond_mode, (settings?.cond_mode_cmd == "not"))) ? "${okSym()}" : "${notOkSym()}"})\n" : ""
        }
        if(deviceCondConfigured()) {
            ["switch", "motion", "presence", "contact", "acceleration", "lock", "battery", "temperature", "illuminance", "shade", "door", "level", "valve", "water", "power"]?.each { evt->
                if(devCondConfigured(evt)) {
                    def condOk = false
                    if(evt in ["switch", "motion", "presence", "contact", "acceleration", "lock", "shade", "door", "valve", "water"]) { condOk = checkDeviceCondOk(evt) }
                    else if(evt in ["battery", "temperature", "illuminance", "level", "power"]) { condOk = checkDeviceNumCondOk(evt) }
                    // str += settings?."${}"
                    str += settings?."${sPre}${evt}"     ? " \u2022 ${evt?.capitalize()} (${settings?."${sPre}${evt}"?.size()}) (${condOk ? "${okSym()}" : "${notOkSym()}"})\n" : ""
                    def cmd = settings?."${sPre}${evt}_cmd" ?: null
                    if(cmd in ["between", "below", "above", "equals"]) {
                        def cmdLow = settings?."${sPre}${evt}_low" ?: null
                        def cmdHigh = settings?."${sPre}${evt}_high" ?: null
                        def cmdEq = settings?."${sPre}${evt}_equal" ?: null
                        str += (cmd == "equals" && cmdEq) ? "    - Value: ( =${cmdEq}${attUnit(evt)})${settings?."cond_${inType}_avg" ? "(Avg)" : ""}\n" : ""
                        str += (cmd == "between" && cmdLow && cmdHigh) ? "    - Value: (${cmdLow-cmdHigh}${attUnit(evt)})${settings?."cond_${inType}_avg" ? "(Avg)" : ""}\n" : ""
                        str += (cmd == "above" && cmdHigh) ? "    - Value: ( >${cmdHigh}${attUnit(evt)})${settings?."cond_${inType}_avg" ? "(Avg)" : ""}\n" : ""
                        str += (cmd == "below" && cmdLow) ? "    - Value: ( <${cmdLow}${attUnit(evt)})${settings?."cond_${inType}_avg" ? "(Avg)" : ""}\n" : ""
                    } else {
                        str += cmd ? "    - Value: (${cmd})${settings?."cond_${inType}_avg" ? "(Avg)" : ""}\n" : ""
                    }
                    str += (settings?."${sPre}${evt}_all" == true) ? "    - Require All: (${settings?."${sPre}${evt}_all"})\n" : ""
                }
            }
        }
        str += "\ntap to modify..."
        return str
    } else {
        return "tap to configure..."
    }
}

String getZoneDesc() {
    if(devicesConfigured() && conditionsConfigured()) {
        List eDevs = parent?.getDevicesFromList(settings?.zone_EchoDevices)?.collect { it?.displayName as String }
        String str = eDevs?.size() ? "Echo Devices in Zone:\n${eDevs?.join("\n")}\n" : ""
        str += settings?.zone_active_delay ? " \u2022 Activate Delay: (${settings?.zone_active_delay})\n" : ""
        str += settings?.zone_inactive_delay ? " \u2022 Deactivate Delay: (${settings?.zone_inactive_delay})\n" : ""
        str += "\nTap to modify..."
        return str
    } else {
        return "tap to configure..."
    }
}

String getTimeCondDesc(addPre=true) {
    def sun = getSunriseAndSunset()
    def sunsetTime = epochToTime(sun?.sunset?.time)
    def sunriseTime = epochToTime(sun?.sunrise?.time)
    def startType = settings?.cond_time_start_type
    def startTime = settings?.cond_time_start ? fmtTime(settings?.cond_time_start) : null
    def stopType = settings?.cond_time_stop_type
    def stopTime = settings?.cond_time_stop ? fmtTime(settings?.cond_time_stop) : null
    String startLbl = (
        (startType in ["Sunset", "Sunrise"]) ?
        ((startType == "Sunset") ? sunsetTime : sunriseTime) :
        startTime
    )
    def stopLbl = (
        (stopType in ["Sunrise", "Sunset"]) ?
        ((stopType == "Sunset") ? sunsetTime : sunriseTime) :
        stopTime
    )
    return ((startLbl && startLbl != "") && (stopLbl && stopLbl != "")) ? "${addPre ? "Time Condition:\n" : ""}(${startLbl} - ${stopLbl})" : "tap to configure..."
}

String getInputToStringDesc(inpt, addSpace = null) {
    Integer cnt = 0
    String str = ""
    if(inpt) {
        inpt.sort().each { item ->
            cnt = cnt+1
            str += item ? (((cnt < 1) || (inpt?.size() > 1)) ? "\n      ${item}" : "${addSpace ? "      " : ""}${item}") : ""
        }
    }
    //log.debug "str: $str"
    return (str != "") ? "${str}" : null
}

String randomString(Integer len) {
    def pool = ["a".."z",0..9].flatten()
    Random rand = new Random(new Date().getTime())
    def randChars = (0..len).collect { pool[rand.nextInt(pool.size())] }
    // logDebug("randomString: ${randChars?.join()}")
    return randChars.join()
}

def getRandomItem(items) {
    def list = new ArrayList<String>();
    items?.each { list?.add(it) }
    return list?.get(new Random().nextInt(list?.size()));
}

/***********************************************************************************************************
    HELPER FUNCTIONS
************************************************************************************************************/
void settingUpdate(name, value, type=null) {
    if(name && type) { app?.updateSetting("$name", [type: "$type", value: value]) }
    else if (name && type == null) { app?.updateSetting(name.toString(), value) }
}

void settingRemove(String name) {
    logTrace("settingRemove($name)...")
    if(name && settings?.containsKey(name as String)) { isST() ? app?.deleteSetting(name as String) : app?.removeSetting(name as String) }
}

List weekDaysEnum() {
    return ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"]
}

List monthEnum() {
    return ["January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"]
}

Map getAlarmTrigOpts() {
    return isST() ? ["away":"Armed Away","stay":"Armed Home","off":"Disarmed"] : ["armAway":"Armed Away","armHome":"Armed Home","disarm":"Disarmed", "alerts":"Alerts"]
}

def getShmIncidents() {
    def incidentThreshold = now() - 604800000
    return location.activeIncidents.collect{[date: it?.date?.time, title: it?.getTitle(), message: it?.getMessage(), args: it?.getMessageArgs(), sourceType: it?.getSourceType()]}.findAll{ it?.date >= incidentThreshold } ?: null
}

Boolean pushStatus() { return (settings?.notif_sms_numbers?.toString()?.length()>=10 || settings?.notif_send_push || settings?.notif_pushover) ? ((settings?.notif_send_push || (settings?.notif_pushover && settings?.notif_pushover_devices)) ? "Push Enabled" : "Enabled") : null }
Integer getLastNotifMsgSec() { return !state?.lastNotifMsgDt ? 100000 : GetTimeDiffSeconds(state?.lastNotifMsgDt, "getLastMsgSec").toInteger() }
Integer getLastChildInitRefreshSec() { return !state?.lastChildInitRefreshDt ? 3600 : GetTimeDiffSeconds(state?.lastChildInitRefreshDt, "getLastChildInitRefreshSec").toInteger() }
Boolean getOk2Notify() {
    Boolean smsOk = (settings?.notif_sms_numbers?.toString()?.length()>=10)
    Boolean pushOk = settings?.notif_send_push
    Boolean alexaMsg = (settings?.notif_alexa_mobile)
    Boolean pushOver = (settings?.notif_pushover && settings?.notif_pushover_devices)
    Boolean notifDevsOk = (settings?.notif_devs?.size())
    Boolean daysOk = settings?.notif_days ? (isDayOfWeek(settings?.notif_days)) : true
    Boolean timeOk = notifTimeOk()
    Boolean modesOk = settings?.notif_mode ? (isInMode(settings?.notif_mode)) : true
    logDebug("getOk2Notify() | smsOk: $smsOk | pushOk: $pushOk | pushOver: $pushOver || alexaMsg: $alexaMsg || daysOk: $daysOk | timeOk: $timeOk | modesOk: $modesOk")
    if(!(smsOk || pushOk || alexaMsg || notifDevsOk || pushOver)) { return false }
    if(!(daysOk && modesOk && timeOk)) { return false }
    return true
}

Boolean notifTimeOk() {
    def startTime = null
    def stopTime = null
    def now = new Date()
    def sun = getSunriseAndSunset() // current based on geofence, previously was: def sun = getSunriseAndSunset(zipCode: zipCode)
    if(settings?.notif_time_start_type && settings?.notif_time_stop_type) {
        if(settings?.notif_time_start_type == "sunset") { startTime = sun?.sunset }
        else if(settings?.notif_time_start_type == "sunrise") { startTime = sun?.sunrise }
        else if(settings?.notif_time_start_type == "time" && settings?.notif_time_start) { startTime = settings?.notif_time_start }

        if(settings?.notif_time_stop_type == "sunset") { stopTime = sun?.sunset }
        else if(settings?.notif_time_stop_type == "sunrise") { stopTime = sun?.sunrise }
        else if(settings?.notif_time_stop_type == "time" && settings?.notif_time_stop) { stopTime = settings?.notif_time_stop }
    } else { return true }
    if(startTime && stopTime) {
        if(!isST()) {
            startTime = toDateTime(startTime)
            stopTime = toDateTime(stopTime)
        }
        return timeOfDayIsBetween(startTime, stopTime, new Date(), location?.timeZone)
    } else { return true }
}

// Sends the notifications based on app settings
public sendNotifMsg(String msgTitle, String msg, alexaDev=null, Boolean showEvt=true) {
    logTrace("sendNotifMsg() | msgTitle: ${msgTitle}, msg: ${msg}, $alexaDev, showEvt: ${showEvt}")
    List sentSrc = ["Push"]
    Boolean sent = false
    try {
        String newMsg = "${msgTitle}: ${msg}"
        String flatMsg = newMsg.toString().replaceAll("\n", " ")
        if(!getOk2Notify()) {
            logInfo( "sendNotifMsg: Message Skipped During Quiet Time ($flatMsg)")
            if(showEvt) { sendNotificationEvent(newMsg) }
        } else {
            if(settings?.notif_send_push) {
                sendSrc?.push("Push Message")
                if(showEvt) {
                    sendPush(newMsg)	// sends push and notification feed
                } else { sendPushMessage(newMsg) } // sends push
                sent = true
            }
            if(settings?.notif_pushover && settings?.notif_pushover_devices) {
                sentSrc?.push("Pushover Message")
                Map msgObj = [title: msgTitle, message: msg, priority: (settings?.notif_pushover_priority?:0)]
                if(settings?.notif_pushover_sound) { msgObj?.sound = settings?.notif_pushover_sound }
                buildPushMessage(settings?.notif_pushover_devices, msgObj, true)
                sent = true
            }
            String smsPhones = settings?.notif_sms_numbers?.toString() ?: null
            if(smsPhones) {
                List phones = smsPhones?.toString()?.tokenize(",")
                for (phone in phones) {
                    String t0 = newMsg.take(140)
                    if(showEvt) {
                        sendSms(phone?.trim(), t0)	// send SMS and notification feed
                    } else { sendSmsMessage(phone?.trim(), t0) } // send SMS
                }
                sentSrc?.push("SMS Message to [${phones}]")
                sent = true
            }
            if(settings?.notif_devs) {
                sentSrc?.push("Notification Devices")
                settings?.notif_devs?.each { it?.deviceNotification(msg as String) }
                sent = true
            }
            if(settings?.notif_alexa_mobile && alexaDev) {
                alexaDev?.sendAlexaAppNotification(msg)
                sentSrc?.push("Alexa Mobile App")
                sent = true
            }
            if(sent) {
                state?.lastNotificationMsg = flatMsg
                state?.lastNotifMsgDt = getDtNow()
                logDebug("sendNotifMsg: Sent ${sendSrc} (${flatMsg})")
            }
        }
    } catch (ex) {
        logError("sendNotifMsg $sentstr Exception: ${ex}")
    }
    return sent
}

Boolean isZoneNotifConfigured() {
    return (
        (settings?.notif_active_message || settings?.notif_inactive_message) &&
        (settings?.notif_sms_numbers?.toString()?.length()>=10 || settings?.notif_send_push || settings?.notif_devs || settings?.notif_alexa_mobile || (isST() && settings?.notif_pushover && settings?.notif_pushover_devices))
    )
}

//PushOver-Manager Input Generation Functions
private getPushoverSounds(){return (Map) state?.pushoverManager?.sounds?:[:]}
private getPushoverDevices(){List opts=[];Map pmd=state?.pushoverManager?:[:];pmd?.apps?.each{k,v->if(v&&v?.devices&&v?.appId){Map dm=[:];v?.devices?.sort{}?.each{i->dm["${i}_${v?.appId}"]=i};addInputGrp(opts,v?.appName,dm);}};return opts;}
private inputOptGrp(List groups,String title){def group=[values:[],order:groups?.size()];group?.title=title?:"";groups<<group;return groups;}
private addInputValues(List groups,String key,String value){def lg=groups[-1];lg["values"]<<[key:key,value:value,order:lg["values"]?.size()];return groups;}
private listToMap(List original){original.inject([:]){r,v->r[v]=v;return r;}}
private addInputGrp(List groups,String title,values){if(values instanceof List){values=listToMap(values)};values.inject(inputOptGrp(groups,title)){r,k,v->return addInputValues(r,k,v)};return groups;}
private addInputGrp(values){addInputGrp([],null,values)}
//PushOver-Manager Location Event Subscription Events, Polling, and Handlers
public pushover_init(){subscribe(location,"pushoverManager",pushover_handler);pushover_poll()}
public pushover_cleanup(){state?.remove("pushoverManager");unsubscribe("pushoverManager");}
public pushover_poll(){sendLocationEvent(name:"pushoverManagerCmd",value:"poll",data:[empty:true],isStateChange:true,descriptionText:"Sending Poll Event to Pushover-Manager")}
public pushover_msg(List devs,Map data){if(devs&&data){sendLocationEvent(name:"pushoverManagerMsg",value:"sendMsg",data:data,isStateChange:true,descriptionText:"Sending Message to Pushover Devices: ${devs}");}}
public pushover_handler(evt){Map pmd=state?.pushoverManager?:[:];switch(evt?.value){case"refresh":def ed = evt?.jsonData;String id = ed?.appId;Map pA = pmd?.apps?.size() ? pmd?.apps : [:];if(id){pA[id]=pA?."${id}"instanceof Map?pA[id]:[:];pA[id]?.devices=ed?.devices?:[];pA[id]?.appName=ed?.appName;pA[id]?.appId=id;pmd?.apps = pA;};pmd?.sounds=ed?.sounds;break;case "reset":pmd=[:];break;};state?.pushoverManager=pmd;}
//Builds Map Message object to send to Pushover Manager
private buildPushMessage(List devices,Map msgData,timeStamp=false){if(!devices||!msgData){return};Map data=[:];data?.appId=app?.getId();data.devices=devices;data?.msgData=msgData;if(timeStamp){data?.msgData?.timeStamp=new Date().getTime()};pushover_msg(devices,data);}
Integer versionStr2Int(str) { return str ? str.toString()?.replaceAll("\\.", "")?.toInteger() : null }
Boolean checkMinVersion() { return (versionStr2Int(appVersion()) < parent?.minVersions()["zoneApp"]) }
Boolean isPaused() { return (settings?.zonePause == true) }

String okSym() { return "\u2713" }
String notOkSym() { return "\u2715" }
String getAppImg(String imgName, frc=false) { return (frc || isST()) ? "https://raw.githubusercontent.com/tonesto7/echo-speaks/${isBeta() ? "beta" : "master"}/resources/icons/${imgName}.png" : "" }
String getPublicImg(String imgName) { return isST() ? "https://raw.githubusercontent.com/tonesto7/SmartThings-tonesto7-public/master/resources/icons/${imgName}.png" : "" }
String sTS(String t, String i = null) { return isST() ? t : """<h3>${i ? """<img src="${i}" width="42"> """ : ""} ${t?.replaceAll("\\n", "<br>")}</h3>""" }
String pTS(String t, String i = null, bold=true, color=null) { return isST() ? t : "${color ? """<div style="color: $color;">""" : ""}${bold ? "<b>" : ""}${i ? """<img src="${i}" width="42"> """ : ""}${t?.replaceAll("\\n", "<br>")}${bold ? "</b>" : ""}${color ? "</div>" : ""}" }
String inTS(String t, String i = null, color=null) { return isST() ? t : """${color ? """<div style="color: $color;">""" : ""}${i ? """<img src="${i}" width="42"> """ : ""} <u>${t?.replaceAll("\\n", " ")}</u>${color ? "</div>" : ""}""" }

String bulletItem(String inStr, String strVal) { return "${inStr == "" ? "" : "\n"} \u2022 ${strVal}" }
String dashItem(String inStr, String strVal, newLine=false) { return "${(inStr == "" && !newLine) ? "" : "\n"} - ${strVal}" }

Integer stateSize() {
    def j = new groovy.json.JsonOutput().toJson(state)
    return j?.toString().length()
}
Integer stateSizePerc() { return (int) ((stateSize() / 100000)*100).toDouble().round(0) }

private addToLogHistory(String logKey, msg, Integer max=10) {
    Boolean ssOk = (stateSizePerc() <= 70)
    List eData = atomicState[logKey as String] ?: []
    eData.push([dt: getDtNow(), message: msg])
    if(!ssOk || eData?.size() > max) { eData = eData?.drop( (eData?.size()-max)+1 ) }
    atomicState[logKey as String] = eData
}
private logDebug(msg) { if(settings?.logDebug == true) { log.debug "Zone (v${appVersion()}) | ${msg}" } }
private logInfo(msg) { if(settings?.logInfo != false) { log.info " Zone (v${appVersion()}) | ${msg}" } }
private logTrace(msg) { if(settings?.logTrace == true) { log.trace "Zone (v${appVersion()}) | ${msg}" } }
private logWarn(msg, noHist=false) { if(settings?.logWarn != false) { log.warn " Zone (v${appVersion()}) | ${msg}"; }; if(!noHist) { addToLogHistory("warnHistory", msg, 15); } }
private logError(msg, noHist=false) { if(settings?.logError != false) { log.error " Zone (v${appVersion()}) | ${msg}"; }; if(!noHist) { addToLogHistory("errorHistory", msg, 15); } }

Map getLogHistory() {
    return [ warnings: atomicState?.warnHistory ?: [], errors: atomicState?.errorHistory ?: [] ]
}

private getPlatform() {
    def p = "SmartThings"
    if(state?.hubPlatform == null) {
        try { [dummy: "dummyVal"]?.encodeAsJson(); } catch (e) { p = "Hubitat" }
        // p = (location?.hubs[0]?.id?.toString()?.length() > 5) ? "SmartThings" : "Hubitat"
        state?.hubPlatform = p
        log.debug "hubPlatform: (${state?.hubPlatform})"
    }
    return state?.hubPlatform
}

//*******************************************************************
//    CLONE CHILD LOGIC
//*******************************************************************
public getDuplSettingData() {
    Map typeObj = [
        stat: [
            bool: ["notif_pushover", "notif_alexa_mobile", "logInfo", "logWarn", "logError", "logDebug", "logTrace", "enableWebCoRE"],
            enum: ["triggerEvents", "act_EchoDevices", "act_EchoZones", "zone_EchoDevices", "actionType", "cond_alarm", "cond_months", "cond_days", "notif_pushover_devices", "notif_pushover_priority", "notif_pushover_sound", "trig_alarm", "trig_guard", "webCorePistons"],
            mode: ["cond_mode", "trig_mode"],
            number: [],
            text: ["appLbl"]
        ],
        ends: [
            bool: ["_all", "_avg", "_once", "_send_push", "_use_custom", "_stop_on_clear", "_db"],
            enum: ["_cmd", "_type", "_time_start_type", "cond_time_stop_type", "_routineExecuted", "_scheduled_sunState", "_scheduled_recurrence", "_scheduled_days", "_scheduled_weeks", "_scheduled_months", "_scheduled_daynums", "_scheduled_type", "_routine_run", "_mode_run", "_webCorePistons", "_rt", "_rt_wd"],
            number: ["_wait", "_low", "_high", "_equal", "_delay", "_cnt", "_volume", "_scheduled_sunState_offset", "_after", "_after_repeat", "_rt_ed", "_volume_change", "_volume_restore"],
            text: ["_txt", "_sms_numbers"],
            time: ["_time_start", "_time_stop", "_scheduled_time"]
        ],
        caps: [
            _acceleration: "accelerationSensor",
            _battery: "battery",
            _contact: "contactSensor",
            _door: "garageDoorControl",
            _temperature: "temperatureMeasurement",
            _illuminance: "illuminanceMeasurement",
            _humidity: "relativeHumidityMeasurement",
            _motion: "motionSensor",
            _level: "switchLevel",
            _button: "button",
            _presence: "presenceSensor",
            _switch: "switch",
            _power: "powerMeter",
            _shade: "windowShades",
            _water: "waterSensor",
            _valve: "valve",
            _thermostat: "thermostat",
            _carbonMonoxide: "carbonMonoxideDetector",
            _smoke: "smokeDetector",
            _lock: "lock",
            _lock_code: "lock",
            _switches_off: "switch",
            _switches_on: "switch",
            _lights: "level",
            _color: "colorControl"
        ],
        dev: [
            _scene: "sceneActivator"
        ]
    ]
    Map setObjs = [:]
    typeObj?.stat?.each { sk,sv->
        sv?.each { svi-> if(settings?.containsKey(svi)) { setObjs[svi] = [type: sk as String, value: settings[svi] ] } }
    }
    typeObj?.ends?.each { ek,ev->
        ev?.each { evi-> settings?.findAll { it?.key?.endsWith(evi) }?.each { fk, fv-> setObjs[fk] = [type: ek as String, value: fv] } }
    }
    typeObj?.caps?.each { ck,cv->
        settings?.findAll { it?.key?.endsWith(ck) }?.each { fk, fv-> setObjs[fk] = [type: "capability.${cv}" as String, value: fv?.collect { it?.id as String }] }
    }
    typeObj?.dev?.each { dk,dv->
        settings?.findAll { it?.key?.endsWith(dk) }?.each { fk, fv-> setObjs[fk] = [type: "device.${dv}" as String, value: fv] }
    }
    Map data = [:]
    data?.label = app?.getLabel()?.toString()?.replace(" (Z \u275A\u275A)", "")
    data?.settings = setObjs
    return data
}

public getDuplStateData() {
    List stskip = ["isInstalled", "isParent", "lastNotifMsgDt", "lastNotificationMsg", "setupComplete", "valEvtHistory", "warnHistory", "errorHistory"]
    return state?.findAll { !(it?.key in stskip) }
}
