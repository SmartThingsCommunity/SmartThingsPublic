/**
 *  Smart Alarm is a versatile and highly configurable home security
 *  for the SmartThings.
 *
 *  Please visit <http://statusbits.github.io/smartalarm/> for more
 *  information.
 *
 *  Version 2.2.5 (01/03/2015)
 *
 *  The latest version of this file can be found on GitHub at:
 *  <https://github.com/statusbits/smartalarm>
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

import groovy.json.JsonSlurper

definition(
    name: "Smart Alarm V2.2.5",
    namespace: "statusbits",
    author: "geko@statusbits.com",
    description: "The ultimate home security application for SmartThings.",
    category: "Safety & Security",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/SafetyAndSecurity/App-IsItSafe.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/SafetyAndSecurity/App-IsItSafe@2x.png",
    oauth: [displayName:"Smart Alarm", displayLink:"https://github.com/statusbits/smartalarm/"]
)

mappings {
    path("/armaway") {
        action: [ GET: "apiArmAway" ]
    }

    path("/armaway/:pincode") {
        action: [ GET: "apiArmAway" ]
    }

    path("/armstay") {
        action: [ GET: "apiArmStay" ]
    }

    path("/armstay/:pincode") {
        action: [ GET: "apiArmStay" ]
    }

    path("/disarm") {
        action: [ GET: "apiDisarm" ]
    }

    path("/disarm/:pincode") {
        action: [ GET: "apiDisarm" ]
    }

    path("/panic") {
        action: [ GET: "apiPanic" ]
    }

    path("/status") {
        action: [ GET: "apiGetStatus" ]
    }

    path("/cp") {
        action: [ GET: "controlPanel" ]
    }
}

preferences {
    page name:"pageSetup"
    page name:"pageAbout"
    page name:"pageSelectZones"
    page name:"pageZoneSettings"
    page name:"pageAlarmSettings"
    page name:"pageNotifications"
    page name:"pageVoiceOptions"
    page name:"pageZoneStatus"
    page name:"pageRemoteControl"
    page name:"pageRestApiOptions"
}

// Show setup page
def pageSetup() {
    TRACE("pageSetup()")

    if (state.version != buildNumber()) {
        setupInit()
        return pageAbout()
    }

    def alarmStatus
    if (state.armed) {
        alarmStatus = "armed "
        alarmStatus += state.stay ? "Stay" : "Away"
    } else {
        alarmStatus = "disarmed"
    }

    def hrefControlPanel = [
        url:        getControlPanelUrl(),
        style:      "embedded",
        title:      "Control Panel",
        description:"Tap to launch control panel...",
        required:   false
    ]

    def pageProperties = [
        name:       "pageSetup",
        title:      "Status",
        nextPage:   null,
        install:    true,
        uninstall:  state.installed
    ]

    return dynamicPage(pageProperties) {
        section {
            paragraph "Smart Alarm is ${alarmStatus}"
            if (state.zones.size()) {
                href "pageZoneStatus", title:"Zone Status", description:"Tap to open"
            }
            //if (isRestApiEnabled()) {
            //    href hrefControlPanel
            //}
        }
        section("Setup Menu") {
            href "pageAlarmSettings", title:"Smart Alarm Settings", description:"Tap to open"
            href "pageSelectZones", title:"Add/Remove Zones", description:"Tap to open"
            href "pageZoneSettings", title:"Zone Settings", description:"Tap to open"
            href "pageNotifications", title:"Notification Options", description:"Tap to open"
            href "pageVoiceOptions", title:"Voice Notification Options", description:"Tap to open"
            href "pageRemoteControl", title:"Remote Control Settings", description:"Tap to open"
            href "pageRestApiOptions", title:"REST API Options", description:"Tap to open"
            href "pageAbout", title:"About Smart Alarm", description:"Tap to open"
        }
        section([title:"Options", mobileOnly:true]) {
            label title:"Assign a name", required:false
        }
    }
}

// Show "About" page
def pageAbout() {
    TRACE("pageAbout()")

    def textAbout =
        "${textVersion()}\n${textCopyright()}\n\n" +
        "You can contribute to the development of this app by making " +
        "donation to geko@statusbits.com via PayPal."

    def hrefInfo = [
        url:        "http://statusbits.github.io/smartalarm/",
        style:      "embedded",
        title:      "Tap here for more information...",
        description:"http://statusbits.github.io/smartalarm/",
        required:   false,
    ]

    def pageProperties = [
        name:       "pageAbout",
        title:      "About",
        nextPage:   "pageSetup",
        install:    false,
        uninstall:  false
    ]

    return dynamicPage(pageProperties) {
        section {
            paragraph textAbout
            href hrefInfo
        }
        section("License") {
            paragraph textLicense()
        }
    }
}

// Show "Zone Status" page
def pageZoneStatus() {
    TRACE("pageZoneStatus()")

    def pageProperties = [
        name:       "pageZoneStatus",
        title:      "Zone Status",
        nextPage:   "pageSetup",
        uninstall:  false
    ]

    return dynamicPage(pageProperties) {
        state.zones.each() {
            def device = getDeviceById(it.deviceId)
            def zoneStatus = "${it.sensorType}, "

            if (it.alert) {
                zoneStatus += "alert"
            } else if (it.interior) {
                zoneStatus += "interior"
            } else if (it.entrance) {
                zoneStatus += "entrance"
            } else {
                zoneStatus += "exterior"
            }

            if (it.bypass) {
                zoneStatus += ", bypassed"
            } else if (it.armed) {
                zoneStatus += ", armed"
            } else {
                zoneStatus += ", disarmed"
            }

            section(device.displayName) {
                paragraph zoneStatus
            }
        }
    }
}

// Show "Add/Remove Zones" page
def pageSelectZones() {
    TRACE("pageZoneSettings()")

    def helpPage =
        "A security zone is an area or your property protected by one of " +
        "the available sensors (contact, motion, moisture or smoke). When " +
        "the zone is armed, activating the sensor will set off an alarm."

    def inputContact = [
        name:       "z_contact",
        type:       "capability.contactSensor",
        title:      "Which contact sensors?",
        multiple:   true,
        required:   false
    ]

    def inputMotion = [
        name:       "z_motion",
        type:       "capability.motionSensor",
        title:      "Which motion sensors?",
        multiple:   true,
        required:   false
    ]

    def inputSmoke = [
        name:       "z_smoke",
        type:       "capability.smokeDetector",
        title:      "Which smoke sensors?",
        multiple:   true,
        required:   false
    ]

    def inputMoisture = [
        name:       "z_water",
        type:       "capability.waterSensor",
        title:      "Which moisture sensors?",
        multiple:   true,
        required:   false
    ]

    def pageProperties = [
        name:       "pageSelectZones",
        title:      "Add/Remove Zones",
        nextPage:   "pageSetup",
        uninstall:  state.installed
    ]

    return dynamicPage(pageProperties) {
        section {
            paragraph helpPage
            input inputContact
            input inputMotion
            input inputSmoke
            input inputMoisture
        }
    }
}

// Show "Zone Settings" page
def pageZoneSettings() {
    TRACE("pageZoneSettings()")

    def helpPage =
        "Each zone can be designated as Exterior (default), Interior, " +
        "Entrance or Alert zone."

    def helpInteriorZones =
        "Zones designated as Interior will not be armed in Stay mode, " +
        "allowing you to freely move inside the premises while the alarm " +
        "is armed."

    def helpEntranceZones =
        "If a zone is designated as Entrance, then alarm will not sound " +
        "for a specified number of seconds, allowing you to disarm the " +
        "alarm after entering the premises."

    def helpAlertZones =
        "Zones designated as Alert are always armed and are typically used " +
        "for smoke and flood alarms."

    def helpBypassZones =
        "You can prevent a zone from setting off an alarm by enabling zone " +
        "'bypass'. Bypassed zones will not be armed."

    def inputEntranceZones = [
        name:           "entranceZones",
        type:           "enum",
        title:          "Select entrance zones",
        metadata:       [values: getZoneNames()],
        multiple:       true,
        required:       false
    ]

    def inputInteriorZones = [
        name:           "interiorZones",
        type:           "enum",
        title:          "Select interior zones",
        metadata:       [values: getZoneNames()],
        multiple:       true,
        required:       false
    ]

    def inputAlertZones = [
        name:           "alertZones",
        type:           "enum",
        title:          "Select alert zones",
        metadata:       [values: getZoneNames()],
        multiple:       true,
        required:       false
    ]

    def inputBypassZones = [
        name:           "bypassZones",
        type:           "enum",
        title:          "Bypass selected zones",
        metadata:       [values: getZoneNames()],
        multiple:       true,
        required:       false
    ]

    def pageProperties = [
        name:       "pageZoneSettings",
        title:      "Zone Settings",
        nextPage:   "pageSetup",
        uninstall:  state.installed
    ]

    return dynamicPage(pageProperties) {
        section {
            paragraph helpPage
        }
        section("Interior Zones") {
            paragraph helpInteriorZones
            input inputInteriorZones
        }
        section("Entrance Zones") {
            paragraph helpEntranceZones
            input inputEntranceZones
        }
        section("Alert Zones") {
            paragraph helpAlertZones
            input inputAlertZones
        }
        section("Bypass Zones") {
            paragraph helpBypassZones
            input inputBypassZones
        }
    }
}

// Show "Smart Alarm Settings" page
def pageAlarmSettings() {
    TRACE("pageAlarmSettings()")

    def helpArming =
        "Smart Alarm can be armed and disarmed by simply setting the home " +
        "'Mode'. There are two arming options - Stay and Away. Interior " +
        "zones are not armed in Stay mode, allowing you to freely move " +
        "inside your home."

    def helpExitDelay =
        "Exit delay allows you to arm the alarm and exit the premises " +
        "through one of the Entrance zones without setting off an alarm. " +
        "Exit delay is not used when arming in Stay mode."

    def helpEntryDelay =
        "Entry delay allows you to enter the premises when Smart Alarm is " +
        "armed and disarm it within specified time without setting off an " +
        "alarm. Entry delay can be optionally disabled in Stay mode."

    def helpAlarm =
        "When an alarm is set off, Smart Alarm can turn on sirens and light" +
        "switches, take camera snapshots and execute a 'Hello, Home' action."

    def helpSilent =
        "Enable Silent mode if you wish to temporarily disable sirens and " +
        "switches. You will still receive push notifications and/or text " +
        "messages, if configured."

    def inputAwayModes = [
        name:           "awayModes",
        type:           "mode",
        title:          "Arm Away in these Modes",
        multiple:       true,
        required:       false
    ]

    def inputStayModes = [
        name:           "stayModes",
        type:           "mode",
        title:          "Arm Stay in these Modes",
        multiple:       true,
        required:       false
    ]

    def inputDisarmModes = [
        name:           "disarmModes",
        type:           "mode",
        title:          "Disarm in these Modes",
        multiple:       true,
        required:       false
    ]

    def inputExitDelay = [
        name:           "exitDelay",
        type:           "enum",
        metadata:       [values:["0","15","30","45","60"]],
        title:          "Exit delay (in seconds)",
        defaultValue:   "30",
        required:       true
    ]

    def inputEntryDelay = [
        name:           "entryDelay",
        type:           "enum",
        metadata:       [values:["0","15","30","45","60"]],
        title:          "Entry delay (in seconds)",
        defaultValue:   "30",
        required:       true
    ]

    def inputEntryDelayDisable = [
        name:           "entryDelayDisable",
        type:           "bool",
        title:          "Disable in Stay mode",
        defaultValue:   false
    ]

    def hhActions = getHelloHomeActions()
    def inputHelloHome = [
        name:           "helloHomeAction",
        type:           "enum",
        title:          "Execute this Hello Home action",
        metadata:       [values: hhActions],
        required:       false
    ]

    def inputAlarms = [
        name:           "alarms",
        type:           "capability.alarm",
        title:          "Activate these alarms",
        multiple:       true,
        required:       false
    ]

    def inputSwitches = [
        name:           "switches",
        type:           "capability.switch",
        title:          "Turn on these switches",
        multiple:       true,
        required:       false
    ]

    def inputCameras = [
        name:           "cameras",
        type:           "capability.imageCapture",
        title:          "Take camera snapshots",
        multiple:       true,
        required:       false
    ]

    def inputSilent = [
        name:           "silent",
        type:           "bool",
        title:          "Enable silent mode",
        defaultValue:   false
    ]

    def pageProperties = [
        name:       "pageAlarmSettings",
        title:      "Smart Alarm Settings",
        nextPage:   "pageSetup",
        uninstall:  state.installed
    ]

    return dynamicPage(pageProperties) {
        section {
            paragraph helpArming
            input inputAwayModes
            input inputStayModes
            input inputDisarmModes
        }
        section("Exit Delay") {
            paragraph helpExitDelay
            input inputExitDelay
        }
        section("Entry Delay") {
            paragraph helpEntryDelay
            input inputEntryDelay
            input inputEntryDelayDisable
        }
        section("Alarm Options") {
            paragraph helpAlarm
            input inputAlarms
            input inputSwitches
            input inputCameras
            input inputHelloHome
            paragraph helpSilent
            input inputSilent
        }
    }
}

// Show "Notification Options" page
def pageNotifications() {
    TRACE("pageNotifications()")

    def helpAbout =
        "Smart Alarm has multiple ways of notifying you when its armed, " +
        "disarmed or when an alarm is set off, including Push " +
        "notifications, SMS (text) messages and Pushbullet notification " +
        "service."

    def inputPushAlarm = [
        name:           "pushMessage",
        type:           "bool",
        title:          "Notify on Alarm",
        defaultValue:   true
    ]

    def inputPushStatus = [
        name:           "pushStatusMessage",
        type:           "bool",
        title:          "Notify on Status Change",
        defaultValue:   true
    ]

    def inputPhone1 = [
        name:           "phone1",
        type:           "phone",
        title:          "Send to this number",
        required:       false
    ]

    def inputPhone1Alarm = [
        name:           "smsAlarmPhone1",
        type:           "bool",
        title:          "Notify on Alarm",
        defaultValue:   false
    ]

    def inputPhone1Status = [
        name:           "smsStatusPhone1",
        type:           "bool",
        title:          "Notify on Status Change",
        defaultValue:   false
    ]

    def inputPhone2 = [
        name:           "phone2",
        type:           "phone",
        title:          "Send to this number",
        required:       false
    ]

    def inputPhone2Alarm = [
        name:           "smsAlarmPhone2",
        type:           "bool",
        title:          "Notify on Alarm",
        defaultValue:   false
    ]

    def inputPhone2Status = [
        name:           "smsStatusPhone2",
        type:           "bool",
        title:          "Notify on Status Change",
        defaultValue:   false
    ]

    def inputPhone3 = [
        name:           "phone3",
        type:           "phone",
        title:          "Send to this number",
        required:       false
    ]

    def inputPhone3Alarm = [
        name:           "smsAlarmPhone3",
        type:           "bool",
        title:          "Notify on Alarm",
        defaultValue:   false
    ]

    def inputPhone3Status = [
        name:           "smsStatusPhone3",
        type:           "bool",
        title:          "Notify on Status Change",
        defaultValue:   false
    ]

    def inputPhone4 = [
        name:           "phone4",
        type:           "phone",
        title:          "Send to this number",
        required:       false
    ]

    def inputPhone4Alarm = [
        name:           "smsAlarmPhone4",
        type:           "bool",
        title:          "Notify on Alarm",
        defaultValue:   false
    ]

    def inputPhone4Status = [
        name:           "smsStatusPhone4",
        type:           "bool",
        title:          "Notify on Status Change",
        defaultValue:   false
    ]

    def inputPushbulletDevice = [
        name:           "pushbullet",
        type:           "device.pushbullet",
        title:          "Use these Pushbullet devices",
        multiple:       true,
        required:       false
    ]

    def inputPushbulletAlarm = [
        name:           "pushbulletAlarm",
        type:           "bool",
        title:          "Notify on Alarm",
        defaultValue:   true
    ]

    def inputPushbulletStatus = [
        name:           "pushbulletStatus",
        type:           "bool",
        title:          "Notify on Status Change",
        defaultValue:   true
    ]

    def pageProperties = [
        name:       "pageNotifications",
        title:      "Notification Options",
        nextPage:   "pageSetup",
        uninstall:  state.installed
    ]

    return dynamicPage(pageProperties) {
        section {
            paragraph helpAbout
        }
        section("Push Notifications") {
            input inputPushAlarm
            input inputPushStatus
        }
        section("Text Message (SMS) #1") {
            input inputPhone1
            input inputPhone1Alarm
            input inputPhone1Status
        }
        section("Text Message (SMS) #2") {
            input inputPhone2
            input inputPhone2Alarm
            input inputPhone2Status
        }
        section("Text Message (SMS) #3") {
            input inputPhone3
            input inputPhone3Alarm
            input inputPhone3Status
        }
        section("Text Message (SMS) #4") {
            input inputPhone4
            input inputPhone4Alarm
            input inputPhone4Status
        }
        section("Pushbullet Notifications") {
            input inputPushbulletDevice
            input inputPushbulletAlarm
            input inputPushbulletStatus
        }
    }
}

// Show "Voice Notification Options" page
def pageVoiceOptions() {
    TRACE("pageVoiceOptions()")

    def helpAbout =
        "Smart Alarm can utilize available speech synthesis devices (e.g. " +
        "VLC Thing) to provide voice notifications."

    def inputSpeechDevice = [
        name:           "speechSynth",
        type:           "capability.speechSynthesis",
        title:          "Use these text-to-speech devices",
        multiple:       true,
        required:       false
    ]

    def inputSpeechOnAlarm = [
        name:           "speechOnAlarm",
        type:           "bool",
        title:          "Notify on Alarm",
        defaultValue:   true
    ]

    def inputSpeechOnStatus = [
        name:           "speechOnStatus",
        type:           "bool",
        title:          "Notify on Status Change",
        defaultValue:   true
    ]

    def inputSpeechTextAlarm = [
        name:           "speechText",
        type:           "text",
        title:          "Alarm Phrase",
        required:       false
    ]

    def inputSpeechTextArmedAway = [
        name:           "speechTextArmedAway",
        type:           "text",
        title:          "Armed Away Phrase",
        required:       false
    ]

    def inputSpeechTextArmedStay = [
        name:           "speechTextArmedStay",
        type:           "text",
        title:          "Armed Stay Phrase",
        required:       false
    ]

    def inputSpeechTextDisarmed = [
        name:           "speechTextDisarmed",
        type:           "text",
        title:          "Disarmed Phrase",
        required:       false
    ]

    def pageProperties = [
        name:       "pageNotifications",
        title:      "Notification Options",
        nextPage:   "pageSetup",
        uninstall:  state.installed
    ]

    return dynamicPage(pageProperties) {
        section {
            paragraph helpAbout
            input inputSpeechDevice
            input inputSpeechOnAlarm
            input inputSpeechOnStatus
            input inputSpeechTextAlarm
            input inputSpeechTextArmedAway
            input inputSpeechTextArmedStay
            input inputSpeechTextDisarmed
        }
    }
}

// Show "Remote Control Options" page
def pageRemoteControl() {
    TRACE("pageRemoteControl()")

    def textHelp =
        "You can use remote controls such as Aeon Labs Minimote to arm " +
        "and disarm Smart Alarm."

    def inputButtons = [
        name:       "buttons",
        type:       "capability.button",
        title:      "Which remote controls?",
        multiple:   true,
        required:   false
    ]

    def inputArmAway = [
        name:           "buttonArmAway",
        type:           "enum",
        title:          "Which button to Arm Away?",
        metadata:       [values:["1","2","3","4"]],
        defaultValue:   "1",
        required:       false
    ]

    def inputArmStay = [
        name:           "buttonArmStay",
        type:           "enum",
        title:          "Which button to Arm Stay?",
        metadata:       [values:["1","2","3","4"]],
        defaultValue:   "2",
        required:       false
    ]

    def inputDisarm = [
        name:           "buttonDisarm",
        type:           "enum",
        title:          "Which button to Disarm?",
        metadata:       [values:["1","2","3","4"]],
        defaultValue:   "3",
        required:       false
    ]

    def inputPanic = [
        name:           "buttonPanic",
        type:           "enum",
        title:          "Which button to Panic?",
        metadata:       [values:["1","2","3","4"]],
        defaultValue:   "4",
        required:       false
    ]

    def pageProperties = [
        name:       "pageRemoteControl",
        title:      "Remote Control Settings",
        nextPage:   "pageSetup",
        install:    false,
        uninstall:  false
    ]

    return dynamicPage(pageProperties) {
        section {
            paragraph textHelp
            input inputButtons
            input inputArmAway
            input inputArmStay
            input inputDisarm
            input inputPanic
        }
    }
}

// Show "Control Panel Options" page
def pageRestApiOptions() {
    TRACE("pageRestApiOptions()")

    def textHelp =
        "Smart Alarm can be controlled remotely by any Web client using " +
        "REST API. Please refer to Smart Alarm documentation for more " +
        "information.\n\n" +
        "WARNING: Make sure OAuth is enabled in the smart app settings " +
        "(in SmartThings IDE) before enabling REST API."

    def textPincode =
        "You can specify optional PIN code to protect arming and disarming " +
        "Smart Alarm via REST API from unauthorized access. If set, the " +
        "PIN code is always required for disarming Smart Alarm, however " +
        "you can optionally turn it off for arming Smart Alarm."

    def inputRestApi = [
        name:           "restApiEnabled",
        type:           "bool",
        title:          "Enable REST API",
        defaultValue:   false
    ]

    def inputPincode = [
        name:           "pincode",
        type:           "number",
        title:          "PIN Code",
        required:       false
    ]

    def inputArmWithPin = [
        name:           "armWithPin",
        type:           "bool",
        title:          "Require PIN code to arm",
        defaultValue:   true
    ]

    def pageProperties = [
        name:       "pageRestApiOptions",
        title:      "REST API Options",
        nextPage:   "pageSetup",
        install:    false,
        uninstall:  false
    ]

    return dynamicPage(pageProperties) {
        section {
            paragraph textHelp
            input inputRestApi
            paragraph textPincode
            input inputPincode
            input inputArmWithPin
        }

        if (isRestApiEnabled()) {
            section("REST API Info") {
                paragraph "Base URL:\n" + state.restEndpoint
                paragraph "Access Token:\n" + state.accessToken
            }
        }
    }
}

def installed() {
    TRACE("installed()")

    initialize()
    state.installed = true
}

def updated() {
    TRACE("updated()")

    unschedule()
    unsubscribe()
    initialize()
}

private def setupInit() {
    TRACE("setupInit()")

    state.version = buildNumber()
    if (state.installed == null) {
        state.installed = false
        state.armed = false
        state.alarm = false
        state.zones = []
    }
}

private def initialize() {
    log.trace "${app.name}. ${textVersion()}. ${textCopyright()}"

    state._init_ = true
    state.exitDelay = settings.exitDelay?.toInteger() ?: 0
    state.entryDelay = settings.entryDelay?.toInteger() ?: 0
    state.offSwitches = []

    if (settings.awayModes?.contains(location.mode)) {
        state.armed = true
        state.stay = false
    } else if (settings.stayModes?.contains(location.mode)) {
        state.armed = true
        state.stay = true
    } else {
        state.armed = false
        state.stay = false
    }

    initZones()
    initButtons()
    initRestApi()
    resetPanel()
    subscribe(location, onLocation)

    STATE()
    state._init_ = false
}

private def initRestApi() {
    if (settings.restApiEnabled) {
        if (!state.accessToken) {
            def token = createAccessToken()
            TRACE("Created new access token: ${token})")
        }
        state.restEndpoint = "https://graph.api.smartthings.com/api/smartapps/installations/${app.id}"
        log.info "REST Endpoint: ${state.restEndpoint}"
        log.info "Access token: ${state.accessToken}"
    } else {
        state.restEndpoint = ""
        log.info "REST API disabled"
    }
}

private def isRestApiEnabled() {
    return settings.restApiEnabled && state.accessToken
}

private def initZones() {
    TRACE("initZones()")

    state.zones = []

    if (settings.z_contact) {
        settings.z_contact.each() {
            String zoneName = "contact: ${it.displayName}"
            def zone = [
                deviceId:   it.id,
                sensorType: "contact",
                alert:      settings.alertZones?.contains(zoneName) ?: false,
                entrance:   settings.entranceZones?.contains(zoneName) ?: false,
                interior:   settings.interiorZones?.contains(zoneName) ?: false,
                bypass:     settings.bypassZones?.contains(zoneName) ?: false,
                armed:      false,
                alarm:      null
            ]

            state.zones << zone
        }
        subscribe(settings.z_contact, "contact.open", onContact)
    }

    if (settings.z_motion) {
        settings.z_motion.each() {
            String zoneName = "motion: ${it.displayName}"
            def zone = [
                deviceId:   it.id,
                sensorType: "motion",
                alert:      settings.alertZones?.contains(zoneName) ?: false,
                entrance:   settings.entranceZones?.contains(zoneName) ?: false,
                interior:   settings.interiorZones?.contains(zoneName) ?: false,
                bypass:     settings.bypassZones?.contains(zoneName) ?: false,
                armed:      false,
                alarm:      null
            ]

            state.zones << zone
        }
        subscribe(settings.z_motion, "motion.active", onMotion)
    }

    if (settings.z_smoke) {
        settings.z_smoke.each() {
            String zoneName = "smoke: ${it.displayName}"
            TRACE("zoneName: ${zoneName}")
            def zone = [
                deviceId:   it.id,
                sensorType: "smoke",
                alert:      settings.alertZones?.contains(zoneName) ?: false,
                entrance:   settings.entranceZones?.contains(zoneName) ?: false,
                interior:   settings.interiorZones?.contains(zoneName) ?: false,
                bypass:     settings.bypassZones?.contains(zoneName) ?: false,
                armed:      false,
                alarm:      null
            ]

            state.zones << zone
        }
        subscribe(settings.z_smoke, "smoke.detected", onSmoke)
        subscribe(settings.z_smoke, "smoke.tested", onSmoke)
        subscribe(settings.z_smoke, "carbonMonoxide.detected", onSmoke)
        subscribe(settings.z_smoke, "carbonMonoxide.tested", onSmoke)
    }

    if (settings.z_water) {
        settings.z_water.each() {
            String zoneName = "water: ${it.displayName}"
            TRACE("zoneName: ${zoneName}")
            def zone = [
                deviceId:   it.id,
                sensorType: "water",
                alert:      settings.alertZones?.contains(zoneName) ?: false,
                entrance:   settings.entranceZones?.contains(zoneName) ?: false,
                interior:   settings.interiorZones?.contains(zoneName) ?: false,
                bypass:     settings.bypassZones?.contains(zoneName) ?: false,
                armed:      false,
                alarm:      null
            ]

            state.zones << zone
        }
        subscribe(settings.z_water, "water.wet", onWater)
    }
}

private def initButtons() {
    TRACE("initButtons()")

    state.buttonActions = [:]
    if (settings.buttons) {
        if (settings.buttonArmAway) {
            state.buttonActions[settings.buttonArmAway] = "armAway"
        }
        if (settings.buttonArmStay) {
            state.buttonActions[settings.buttonArmStay] = "armStay"
        }
        if (settings.buttonDisarm) {
            state.buttonActions[settings.buttonDisarm] = "disarm"
        }
        if (settings.buttonPanic) {
            state.buttonActions[settings.buttonPanic] = "panic"
        }

        subscribe(settings.buttons, "button.pushed", onButtonPushed)
    }
}

def resetPanel() {
    TRACE("resetPanel()")

    unschedule()
    settings.alarms*.off()

    // only turn back off those switches that we turned on
    def switchesOff = state.offSwitches
    if (switchesOff) {
        TRACE("switchesOff: ${switchesOff}")
        settings.switches.each() {
            if (switchesOff.contains(it.id)) {
                it.off()
            }
        }
        state.offSwitches = []
    }

    state.alarm = false

    // Reset zones
    state.zones.each() {
        it.alarm = null

        if (it.bypass) {
            it.armed = false
        } else if (it.alert) {
            it.armed = true
        } else if (it.interior) {
            it.armed = state.armed && !state.stay
        } else if (it.entrance) {
            it.armed = state.armed && (state.stay || state.exitDelay == 0)
        } else {
            it.armed = state.armed
        }
    }

    // Schedule delayed arming of Entrance zones
    if (state.armed && !state.stay && state.exitDelay) {
        myRunIn(state.exitDelay, armEntranceZones)
    }

    // Send notification
    def msg = "Alarm "
    if (state.armed) {
        def mode = state.stay ? "Stay" : "Away"
        msg += "Armed"
    } else {
        msg += "Disarmed"
    }

    log.trace msg
    notify(msg)
    notifyVoice()
}

private def onZoneEvent(evt, sensorType) {
    TRACE("onZoneEvent(${evt.displayName}, ${sensorType})")

    def zone = getZoneForDevice(evt.deviceId, sensorType)
    if (!zone) {
        log.warn "Cannot find zone for device ${evt.deviceId}"
        return
    }

    if (!zone.armed) {
        return
    }

    zone.alarm = evt.displayName

    if (state.alarm) {
        // already in alarm state
        return
    }

    // Activate alarm
    state.alarm = true
    if (zone.entrance && state.entryDelay && !(state.stay && settings.entryDelayDisable)) {
        myRunIn(state.entryDelay, activateAlarm)
    } else {
        activateAlarm()
    }
}

def onContact(evt)  { onZoneEvent(evt, "contact") }
def onMotion(evt)   { onZoneEvent(evt, "motion") }
def onSmoke(evt)    { onZoneEvent(evt, "smoke") }
def onWater(evt)    { onZoneEvent(evt, "water") }

def onLocation(evt) {
    TRACE("onLocation(${evt.value})")

    String mode = evt.value
    if (settings.awayModes?.contains(mode)) {
        armAway()
    } else if (settings.stayModes?.contains(mode)) {
        armStay()
    } else if (settings.disarmModes?.contains(mode)) {
        disarm()
    }
}

def onButtonPushed(evt) {
    TRACE("onButtonPushed(${evt.displayName})")

    if (!evt.data) {
        return
    }

    def slurper = new JsonSlurper()
    def data = slurper.parseText(evt.data)
    def button = data.buttonNumber
    if (button) {
        TRACE("Button '${button}' was pushed.")
        def action = state.buttonActions["${button}"]
        if (action) {
            log.trace "Executing button action ${action}()"
            "${action}"()
        }
    }
}

def armAway() {
    TRACE("armAway()")

    if (state.armed && !state.stay) {
        return
    }

    state.armed = true
    state.stay = false
    resetPanel()
}

def armStay() {
    TRACE("armStay()")

    if (state.armed && state.stay) {
        return
    }

    state.armed = true
    state.stay = true
    resetPanel()
}

def disarm() {
    TRACE("disarm()")

    if (state.armed) {
        state.armed = false
        resetPanel()
    }
}

def armEntranceZones() {
    TRACE("armEntranceZones()")

    if (state.armed) {
        state.zones.each() {
            if (it.entrance && !it.bypass) {
                it.armed = true
            }
        }
        def msg = "Entrance zones are armed"
        log.trace msg
    }
}

def panic() {
    TRACE("panic()")

    state.alarm = true;
    activateAlarm()
}

// .../armaway REST API endpoint
def apiArmAway() {
    TRACE("apiArmAway()")

    if (!isRestApiEnabled()) {
        log.error "REST API disabled"
        return httpError(403, "Access denied")
    }

    if (settings.pincode && settings.armWithPin) {
        if (params.pincode != settings.pincode.toString()) {
            log.error "Invalid PIN code '${params.pincode}'"
            return httpError(403, "Access denied")
        }
    }

    armAway()
    return apiGetStatus()
}

// .../armstay REST API endpoint
def apiArmStay() {
    TRACE("apiArmStay()")

    if (!isRestApiEnabled()) {
        log.error "REST API disabled"
        return httpError(403, "Access denied")
    }

    if (settings.pincode && settings.armWithPin) {
        if (params.pincode != settings.pincode.toString()) {
            log.error "Invalid PIN code '${params.pincode}'"
            return httpError(403, "Access denied")
        }
    }

    armStay()
    return apiGetStatus()
}

// .../disarm REST API endpoint
def apiDisarm() {
    TRACE("apiDisarm()")

    if (!isRestApiEnabled()) {
        log.error "REST API disabled"
        return httpError(403, "Access denied")
    }

    if (settings.pincode) {
        if (params.pincode != settings.pincode.toString()) {
            log.error "Invalid PIN code '${params.pincode}'"
            return httpError(403, "Access denied")
        }
    }

    disarm()
    return apiGetStatus()
}

// .../panic REST API endpoint
def apiPanic() {
    TRACE("apiPanic()")

    if (!isRestApiEnabled()) {
        log.error "REST API disabled"
        return httpError(403, "Access denied")
    }

    panic()
    return apiGetStatus()
}

// .../status REST API endpoint
def apiGetStatus() {
    TRACE("apiGetStatus()")

    if (!isRestApiEnabled()) {
        log.error "REST API disabled"
        return httpError(403, "Access denied")
    }

    def status = [:]
    status.status = state.armed ? (state.stay ? "armed stay" : "armed away") : "disarmed"
    status.alarm = state.alarm

    return status
}

// .../cp REST API endpoint
def controlPanel() {
    TRACE("controlPanel()")

    if (!isRestApiEnabled()) {
        log.error "REST API disabled"
        return httpError(403, "Access denied")
    }

    def html = '''\
<!DOCTYPE HTML>
<html lang="en-US">
<head><title>Smart Alarm</title></head>
<body><h2>Coming soon...</h2></body>
</html>
'''

    render contentType:"text/html", data: html
}

def activateAlarm() {
    TRACE("activateAlarm()")

    if (!state.alarm) {
        log.warn "activateAlarm: false alarm"
        return
    }

    // Activate alarms and switches
    if (!settings.silent) {
        settings.alarms*.both()

        // Only turn on those switches that are currently off
        def switchesOn = settings.switches?.findAll { it?.currentSwitch == "off" }
        TRACE("switchesOn: ${switchesOn}")
        if (switchesOn) {
            switchesOn*.on()
            state.offSwitches = switchesOn.collect { it.id }
        }
    }

    // Take camera snapshots
    settings.cameras*.take()

    // Execute Hello Home action
    if (settings.helloHomeAction) {
        log.trace "Executing HelloHome action \'${settings.helloHomeAction}\'"
        location.helloHome.execute(settings.helloHomeAction)
    }

    // Send notifications
    def msg = "Alarm at ${location.name}!"
    state.zones.each() {
        if (it.alarm) {
            msg += "\n${it.alarm}"
        }
    }
    log.trace msg
    notify(msg)
    notifyVoice()

    // Schedule panel reset in 3 minutes
    myRunIn(180, resetPanel)
}

private def notify(msg) {
    TRACE("notify(${msg})")
    if (state.alarm) {
        // Alarm notification
        if (settings.pushMessage) {
            mySendPush(msg)
        } else {
            sendNotificationEvent(msg)
        }

        if (settings.smsAlarmPhone1 && settings.phone1) {
            sendSms(phone1, msg)
        }

        if (settings.smsAlarmPhone2 && settings.phone2) {
            sendSms(phone2, msg)
        }

        if (settings.smsAlarmPhone3 && settings.phone3) {
            sendSms(phone3, msg)
        }

        if (settings.smsAlarmPhone4 && settings.phone4) {
            sendSms(phone4, msg)
        }

        if (settings.pushbulletAlarm && settings.pushbullet) {
        	settings.pushbullet*.push(msg, msg)
        }    
    } else {
        // Status change notification
        if (settings.pushStatusMessage) {
            mySendPush(msg)
        } else {
            sendNotificationEvent(msg)
        }

        if (settings.smsStatusPhone1 && settings.phone1) {
            sendSms(phone1, msg)
        }

        if (settings.smsStatusPhone2 && settings.phone2) {
            sendSms(phone2, msg)
        }

        if (settings.smsStatusPhone3 && settings.phone3) {
            sendSms(phone3, msg)
        }

        if (settings.smsStatusPhone4 && settings.phone4) {
            sendSms(phone4, msg)
        }

        if (settings.pushbulletStatus && settings.pushbullet) {
        	settings.pushbullet*.push(msg, msg)
        }
    }
}

private def notifyVoice() {
    TRACE("notifyVoice()")

    if (!settings.speechSynth || state._init_) {
        return
    }

    def phrase = null
    if (state.alarm) {
        // Alarm notification
        if (settings.speechOnAlarm) {
            phrase = settings.speechText ?: getStatusPhrase()
        }
    } else {
        // Status change notification
        if (settings.speechOnStatus) {
            if (state.armed) {
                if (state.stay) {
                    phrase = settings.speechTextArmedStay ?: getStatusPhrase()
                } else {
                    phrase = settings.speechTextArmedAway ?: getStatusPhrase()
                }
            } else {
                phrase = settings.speechTextDisarmed ?: getStatusPhrase()
            }
        }
    }

    if (phrase) {
        settings.speechSynth*.speak(phrase)
    }
}

private def getStatusPhrase() {
    TRACE("getStatusPhrase()")

    def phrase = ""
    if (state.alarm) {
        phrase = "Alarm at ${location.name}!"
        state.zones.each() {
            if (it.alarm) {
                phrase += " In zone ${it.alarm}."
            }
        }
    } else {
        phrase = "${location.name} alarm is "
        if (state.armed) {
            def mode = state.stay ? "stay" : "away"
            phrase += "armed in ${mode} mode."
        } else {
            phrase += "disarmed."
        }
    }

    return phrase
}

private def getHelloHomeActions() {
    def actions = []
    location.helloHome?.getPhrases().each {
        actions << "${it.label}"
    }

    return actions.sort()
}

private def getZoneNames() {
    def zoneNames = []
    for (dev in settings.z_contact) {
        zoneNames << "contact: ${dev.displayName}"
    }
    for (dev in settings.z_motion) {
        zoneNames << "motion: ${dev.displayName}"
    }
    for (dev in settings.z_smoke) {
        zoneNames << "smoke: ${dev.displayName}"
    }
    for (dev in settings.z_water) {
        zoneNames << "water: ${dev.displayName}"
    }

    return zoneNames.sort()
}

private def getZoneForDevice(id, sensorType) {
    return state.zones.find() { it.deviceId == id && it.sensorType == sensorType }
}

private def getDeviceById(id) {
    def device = settings.z_contact?.find() { it.id == id }

    if (!device) {
        device = settings.z_motion?.find() { it.id == id }
    }

    if (!device) {
        device = settings.z_smoke?.find() { it.id == id }
    }

    if (!device) {
        device = settings.z_water?.find() { it.id == id }
    }

    return device
}

private def getControlPanelUrl(id) {
    def url = ""
    if (isRestApiEnabled()) {
         url = state.restEndpoint + "/cp?access_token=" + state.accessToken
    }

    return url
}

private def myRunIn(delay_s, func) {
    TRACE("myRunIn(${delay_s})")

    if (delay_s > 0) {
        def tms = now() + (delay_s * 1000)
        def date = new Date(tms)
        runOnce(date, func)
        TRACE("'${func}' scheduled to run at ${date}")
    }
}

private def mySendPush(msg) {
    // cannot call sendPush() from installed() or updated()
    if (!state._init_) {
        // sendPush can throw an exception
        try {
            sendPush(msg)
        } catch (e) {
            log.error e
        }
    }
}

private def buildNumber() {
    return 150103
}

private def textVersion() {
    def text = "Version 2.2.5 (01/03/2015)"
}

private def textCopyright() {
    def text = "Copyright © 2014 Statusbits.com"
}

private def textLicense() {
    def text =
        "This program is free software: you can redistribute it and/or " +
        "modify it under the terms of the GNU General Public License as " +
        "published by the Free Software Foundation, either version 3 of " +
        "the License, or (at your option) any later version.\n\n" +
        "This program is distributed in the hope that it will be useful, " +
        "but WITHOUT ANY WARRANTY; without even the implied warranty of " +
        "MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU " +
        "General Public License for more details.\n\n" +
        "You should have received a copy of the GNU General Public License " +
        "along with this program. If not, see <http://www.gnu.org/licenses/>."
}

private def TRACE(message) {
    //log.debug message
}

private def STATE() {
    //log.trace "settings: ${settings}"
    //log.trace "state: ${state}"
}