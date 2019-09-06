/**
 *  Tesla Powerwall Manager 
 * 
 *  Copyright 2019 DarwinsDen.com
 *  
 *  ****** WARNING ****** USE AT YOUR OWN RISK!
 *  This software was developed in the hopes that it will be useful to others, however, 
 *  it is beta software and may have unforeseen side effects to your equipment and related accounts.
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
 *  Access Token initialization code is derived from Trent Foley's excellent Tesla Connect SmartThings Smart App
 *     https://github.com/trentfoley/SmartThingsPublic/blob/master/smartapps/trentfoley/tesla-connect.src/tesla-connect.groovy 
 *
 */

include 'asynchttp_v1'

def version() {
    return "v0.1.5e.20190906"
}

/* 
 *	06-Sep-2019 >>> v0.1.5e.20190906 - Updated watchdog to only notify once when issue first occurs and when resolved 
 *	13-Aug-2019 >>> v0.1.4e.20190813 - Added grid/outage status display, notifications, and device on/off controls 
 *	09-Aug-2019 >>> v0.1.3e.20190809 - Added reserve% scheduling & polling interval preferences
 *	29-Jul-2019 >>> v0.1.2e.20190729 - Set reserve percent to 100% in backup-only mode. Added mode scheduling.
 *	23-Jul-2019 >>> v0.1.1e.20190723 - Initial beta release
 */
 
definition(
    name: "Tesla Powerwall Manager", namespace: "darwinsden", author: "Darwin", description: "Monitor and control your Tesla Powerwall",
    category: "My Apps",
    iconUrl: "https://rawgit.com/DarwinsDen/SmartThingsPublic/master/resources/icons/pwLogoAlphaCentered.png",
    iconX2Url: "https://rawgit.com/DarwinsDen/SmartThingsPublic/master/resources/icons/pwLogoAlphaCentered.png"
)

preferences {
    page(name: "pageMain")
    page(name: "verifyPowerwalls")
    page(name: "accountInfo")
    page(name: "pageNotifications")
    page(name: "pageSchedules")
    page(name: "pageReserveSchedule")
    page(name: "pageRemove")
    page(name: "schedule1Options")
    page(name: "schedule2Options")
    page(name: "schedule3Options")
    page(name: "schedule4Options")
    page(name: "schedule5Options")
    page(name: "pagePwPreferences")
    page(name: "pageDevicesToControl")
}

private pageMain() {
    return dynamicPage(name: "pageMain", title: "", nextPage: "verifyPowerwalls") {
        section() {
            paragraph app.version(),
                title: "PowerWall Manager", required: false, image: "https://rawgit.com/DarwinsDen/SmartThingsPublic/master/resources/icons/pwLogoAlphaCentered.png"
        }
        section("Tesla Account Information") {
           href "accountInfo", title: "Account Information..", description: "", required: (!userEmail || !userPw), image: "https://rawgit.com/DarwinsDen/SmartThingsPublic/master/resources/icons/Tesla-Icon.png"
        }
        section("Preferences") {
            href "pageNotifications", title: "Notification Preferences..", description: "", required: false,
                image: "https://rawgit.com/DarwinsDen/SmartThingsPublic/master/resources/icons/notification.png"
            href "pageSchedules", title: "Schedules..", description: "", required: false,
                image: "https://rawgit.com/DarwinsDen/SmartThingsPublic/master/resources/icons/calendar.png"
            href "pageDevicesToControl", title: "Turn off devices during a grid outage..", description: "", required: false,
                image: "http://cdn.device-icons.smartthings.com/Home/home30-icn@2x.png"
            href "pagePwPreferences", title: "Powerwall Manager Preferences..", description: "", required: false,
                image: "https://rawgit.com/DarwinsDen/SmartThingsPublic/master/resources/icons/cog.png"
        }    
        section("For more information") {
          href(name: "Site", title: "For more information, questions, or to provide feedback, please visit: DarwinsDen.com/powerwall",
               description: "Tap to open the Powerwall Manager web page on DarwinsDen.com",
             required: false,
              image: "https://darwinsden.com/download/ddlogo-for-pwmanager-0-png",
             url: "https://darwinsden.com/powerwall/")
      
        }
        section("Remove Powerwall Manager") {
            href "pageRemove", title: "", description: "Remove Powerwall Manager", required: false
        }
        
    }
}

def pageRemove() {
    dynamicPage(name: "pageRemove", title: "", install: false, uninstall: true) {
        section() {
            paragraph parent ? "CAUTION: You are about to remove the '${app.label}'." : "If an error occurs during removal, " +
            "you may need to first manually remove references to the Powerwall Manager child device from other smart apps if you have manually added them.",
                required: true, state: null
        }
    }
}

private accountInfo () {
 
    return dynamicPage(name: "accountInfo", title: "", install:false) {

        resetAccountAccess ()
        section("Tesla Account Information: ") {
            input "userEmail", "text", title: "Email", autoCorrect: false, required: true
            input "userPw", "password", title: "Password", autoCorrect: false, required: true
        }
    }
}

def pageNotifications() {
    dynamicPage(name: "pageNotifications", title: "Notification Preferences", install: false, uninstall: false) {

        section("Powerwall Notification Triggers:") {
            input "notifyWhenVersionChanges", "boolean", required: false, defaultValue: false, title: "Notify when Powerwall software version changes"
            input "notifyWhenModesChange", "boolean", required: false, defaultValue: false, title: "Notify when Powerwall configuration (modes/schedules) change"
            input "notifyWhenGridStatusChanges", "boolean", required: false, defaultValue: false, title: "Notify of grid status changes/power failures"
            input "notifyWhenReserveApproached", "boolean", required: false, defaultValue: false, title: "Notify when Powerwall energy left percentage approaches reserve percentage"
            input "notifyWhenLowerLimitReached", "boolean", required: false, defaultValue: false, title: "Notify when Powerwall energy left percentage reaches a lower limit"
            input "lowerLimitNotificationValue", "number", required: false, title: "Percentage value to use for Lower Limit Notification"
            input "notifyOfSchedules", "boolean", required: false, defaultValue: true, title: "Notify when schedules are being executed by the Powerwall Manager"
            input "notifyWhenAnomalies", "boolean", required: false, defaultValue: true, title: "Notify when anomalies are encountered in the Powerwall Manager SmartApp"
        }
        
        section("Notification method (push notifications are via ST app) and phone number if text/SMS messages are selected") {
            input "notificationMethod", "enum", required: false, defaultValue: "push", title: "Notification Method", options: ["none", "text", "push", "text and push"]
            input "phoneNumber", "phone", title: "Phone number for text messages", description: "Phone Number", required: false
        }
    }
}

def schedule1Options() {
    dynamicPage(name: "schedule1Options", title: "Schedule 1", install: false, uninstall: false) {
        section("Reserve setting only applies to Self-Powered and Backup-Only modes") {
           input "schedule1Mode", "enum", required: false, title: "Mode to set", options: ["No Action", "Backup-Only","Self-Powered", "Time-Based Control"]
           input "schedule1Reserve", "enum", required: false, title: "Reserve % to set",
                options: ["No Action":"No Action", "0":"0%","5":"5%","10":"10%","25":"25%","50":"50%","75":"75%","90":"90%","100":"100%"]
           input "schedule1Time", "time", required: false, title: "At what time?"
           input "schedule1Days", "enum", required: false, title: "On which days...", multiple: true,
                options: ["Monday", "Tuesday", "Wednesday", "Thursday","Friday","Saturday","Sunday"]
        }
    }
}

def schedule2Options() {
    dynamicPage(name: "schedule2Options", title: "Schedule 2", install: false, uninstall: false) {
        section("Reserve setting only applies to Self-Powered and Backup-Only modes") {
           input "schedule2Mode", "enum", required: false, title: "Mode to set", options: ["No Action", "Backup-Only","Self-Powered", "Time-Based Control"]
           input "schedule2Reserve", "enum", required: false, title: "Reserve % to set",
                options: ["No Action":"No Action", "0":"0%","5":"5%","10":"10%","25":"25%","50":"50%","75":"75%","90":"90%","100":"100%"]
           input "schedule2Time", "time", required: false, title: "At what time?"
           input "schedule2Days", "enum", required: false, title: "On which days...", multiple: true,
                options: ["Monday", "Tuesday", "Wednesday", "Thursday","Friday","Saturday","Sunday"]
        }
    }
}

def schedule3Options() {
    dynamicPage(name: "schedule3Options", title: "Schedule 3", install: false, uninstall: false) {
        section("Reserve setting only applies to Self-Powered and Backup-Only modes") {
           input "schedule3Mode", "enum", required: false, title: "Mode to set", options: ["No Action", "Backup-Only","Self-Powered", "Time-Based Control"]
           input "schedule3Reserve", "enum", required: false, title: "Reserve % to set",
                options: ["No Action":"No Action", "0":"0%","5":"5%","10":"10%","25":"25%","50":"50%","75":"75%","90":"90%","100":"100%"]
           input "schedule3Time", "time", required: false, title: "At what time?"
           input "schedule3Days", "enum", required: false, title: "On which days...", multiple: true,
                options: ["Monday", "Tuesday", "Wednesday", "Thursday","Friday","Saturday","Sunday"]
        }
    }
}

def schedule4Options() {
    dynamicPage(name: "schedule4Options", title: "Schedule 4", install: false, uninstall: false) {
        section("Reserve setting only applies to Self-Powered and Backup-Only modes") {
           input "schedule4Mode", "enum", required: false, title: "Mode to set", options: ["No Action", "Backup-Only","Self-Powered", "Time-Based Control"]
           input "schedule4Reserve", "enum", required: false, title: "Reserve % to set",
                options: ["No Action":"No Action", "0":"0%","5":"5%","10":"10%","25":"25%","50":"50%","75":"75%","90":"90%","100":"100%"]
           input "schedule4Time", "time", required: false, title: "At what time?"
           input "schedule4Days", "enum", required: false, title: "On which days...", multiple: true,
                options: ["Monday", "Tuesday", "Wednesday", "Thursday","Friday","Saturday","Sunday"]
        }
    }
}

def schedule5Options() {
    dynamicPage(name: "schedule5Options", title: "Schedule 5", install: false, uninstall: false) {
        section("Reserve setting only applies to Self-Powered and Backup-Only modes") {
           input "schedule5Mode", "enum", required: false, title: "Mode to set", options: ["No Action", "Backup-Only","Self-Powered", "Time-Based Control"]
           input "schedule5Reserve", "enum", required: false, title: "Reserve % to set",
                options: ["No Action":"No Action", "0":"0%","5":"5%","10":"10%","25":"25%","50":"50%","75":"75%","90":"90%","100":"100%"]
           input "schedule5Time", "time", required: false, title: "At what time?"
           input "schedule5Days", "enum", required: false, title: "On which days...", multiple: true,
                options: ["Monday", "Tuesday", "Wednesday", "Thursday","Friday","Saturday","Sunday"]
        }
    }
}

def pagePwPreferences() {
    dynamicPage(name: "pagePwPreferences", title: "Powerwall Manager Preferences", install: false, uninstall: false) {
        section("") {
           input "pollingPeriod", "enum", required: false, title: "Powerwall polling interval", defaultValue: "10 minutes",
                options: ["Do not poll","5 minutes","10 minutes","30 minutes","1 hour"]
        }
    }
}

def pageDevicesToControl() {
    dynamicPage(name: "pageDevicesToControl", title: "Turn off devices when a grid outage is detected", install: false, uninstall: false) {
        section("") {
          input "devicesToOffDuringOutage", "capability.switch", title: "Devices that should be turned off during a grid outage", required: false, multiple: true
          input "turnDevicesBackOnAfterOutage", "boolean", required: false, defaultValue: false, 
                title: "Turn the above selected devices back on after grid outage is over?"
        }
    }
}


def actionsValid (modeSetting, reserveSetting) {
     return (modeSetting && modeSetting.toString() != "No Action") || (reserveSetting && reserveSetting.toString() != "No Action")
}

def scheduleValid (timeSetting,daysSetting) {
     return timeSetting != null && daysSetting != null && daysSetting.size() > 0
}

def getOptionsString (modeSetting,reserveSetting,timeSetting,daysSetting)
{
        def optionsString = ''
        if (actionsValid (modeSetting, reserveSetting)) {
           if (scheduleValid (timeSetting, daysSetting)) {
              if (modeSetting && modeSetting.toString() != "No Action") {
                 optionsString = "Mode: " + modeSetting.toString()
              }
              if (reserveSetting && reserveSetting.toString() != "No Action") {
                 if (optionsString != '') {
                     optionsString = optionsString + ',\n'
                 }
                 optionsString = optionsString + "Reserve: " + reserveSetting.toString() + '%'
              }
              def timeFormat = new java.text.SimpleDateFormat("hh:mm a")
              def isoDatePattern = "yyyy-MM-dd'T'HH:mm:ss.SSS"
              def isoTime = new java.text.SimpleDateFormat(isoDatePattern).parse(timeSetting.toString())
              def time = timeFormat.format(isoTime)
              optionsString = optionsString + '\n' +  time + ' ' + daysSetting
           } else {
               optionsString = "No time or days scheduled"
           }
        }
        else {
           optionsString = "No actions scheduled"
        } 

        
    return optionsString
}
        
def pageSchedules() {
    dynamicPage(name: "pageSchedules", title: "Powerwall setting changes are subject to Powerwall processing rules and may not immediately take effect at the time they are commanded.", install: false, uninstall: false) {
        def optionsString
        optionsString = getOptionsString(schedule1Mode, schedule1Reserve, schedule1Time, schedule1Days)
        section("Schedule 1") {
            href "schedule1Options", title: "${optionsString}", description: ""
        }
        optionsString = getOptionsString(schedule2Mode, schedule2Reserve, schedule2Time, schedule2Days)
        section("Schedule 2") {
            href "schedule2Options", title: "${optionsString}", description: ""
        }
        optionsString = getOptionsString(schedule3Mode, schedule3Reserve, schedule3Time, schedule3Days)
        section("Schedule 3") {
            href "schedule3Options", title: "${optionsString}", description: ""
        }
        optionsString = getOptionsString(schedule4Mode, schedule4Reserve, schedule4Time, schedule4Days)
        section("Schedule 4") {
            href "schedule4Options", title: "${optionsString}", description: ""
        }
        optionsString = getOptionsString(schedule5Mode, schedule5Reserve, schedule5Time, schedule5Days)
        section("Schedule 5") {
            href "schedule5Options", title: "${optionsString}", description: ""
        }
    }
}

def setSchedules() {
    if (actionsValid (schedule1Mode, schedule1Reserve)) {
        if (scheduleValid (schedule1Time, schedule1Days)) {
            log.debug "scheduling mode 1"
            schedule(schedule1Time.toString(), processSchedule1)
        } else {
             def message = "Schedule 1 actions are enabled in preferences, but schedule time and/or days were not specified. Schedule could not be set."
            sendNotificationMessage(message, "anomaly")
        }
    }
    
    if (actionsValid (schedule2Mode, schedule2Reserve)) {
        if (scheduleValid (schedule2Time, schedule2Days)) {
            log.debug "scheduling mode 2"
            schedule(schedule2Time.toString(), processSchedule2)
        } else {
            def message = "Schedule 2 actions are enabled in preferences, but schedule time and/or days were not specified. Schedule could not be set."
            sendNotificationMessage(message, "anomaly")
        }
    }
    
    if (actionsValid (schedule3Mode, schedule3Reserve)) {
        if (scheduleValid (schedule3Time, schedule3Days)) {
            log.debug "scheduling mode 3"
            schedule(schedule3Time.toString(), processSchedule3)
        } else {
            def message = "Schedule 3 actions are enabled in preferences, but schedule time and/or days were not specified. Schedule could not be set."
            sendNotificationMessage(message, "anomaly")
        }
    }
    
    if (actionsValid (schedule4Mode, schedule4Reserve)) {
        if (scheduleValid (schedule4Time, schedule4Days)) {
            log.debug "scheduling mode 4"
            schedule(schedule4Time.toString(), processSchedule4)
        } else {
            def message = "Schedule 4 actions are enabled in preferences, but schedule time, and/or days were not specified. Schedule could not be set."
            sendNotificationMessage(message, "anomaly")
        }
    }
    if (actionsValid (schedule5Mode, schedule5Reserve)) {
        if (scheduleValid (schedule5Time, schedule5Days)) {
            log.debug "scheduling mode 5"
            schedule(schedule5Time.toString(), processSchedule5)
        } else {
            def message = "Schedule 5 actions are enabled in preferences, but schedule time, and/or days were not specified. Schedule could not be set."
            sendNotificationMessage(message, "anomaly")
        }
    }
}

def getTheDay() {
    def df = new java.text.SimpleDateFormat("EEEE")
    // Ensure the new date object is set to local time zone
    df.setTimeZone(location.timeZone)
    def day = df.format(new Date())
    //log.debug "Today is: ${day}"
    return day
}

def commandPwFromSchedule (mode, reserve, scheduledDays) {
    def day = getTheDay()
    if (scheduledDays?.contains(day)) { 
        def pwDevice = getChildDevice("powerwallDashboard")
        def message = "Performing scheduled Powerwall actions." 
        if (mode && mode.toString() != "No Action") {
           message = message + " Setting mode to ${mode.toString()}."
           if (mode.toString()=="Backup-Only") {
              setBackupOnlyMode(pwDevice) 
           } else if (mode.toString()=="Self-Powered") {
              setSelfPoweredMode(pwDevice)
           } else if (mode.toString()=="Time-Based Control") {
              setTimeBasedControlMode(pwDevice)
           } else {
              def errMessage = "Unexpected condition processing scheduled mode change: ${mode.toString()}"
              sendNotificationMessage(errMessage, "anomaly")  
           }
        }
        if (reserve && reserve.toString() != "No Action") {
           message = message + " Setting reserve to ${reserve}%."
           if (reserve.toInteger() >= 0 && reserve.toInteger() <= 100) {
               runIn(10,commandBackupReservePercent,[data: [reservePercent:reserve.toInteger()]])
              //setBackupReservePercent(pwDevice,reserve.toInteger()) 
           } else {
              def errMessage = "Unexpected condition processing scheduled reserve % change: ${reserve}}"
              sendNotificationMessage(errMessage, "anomaly")  
           }
        }
        if (notifyOfSchedules?.toBoolean()) {
               sendNotificationMessage(message)     
        }   
    }
}
  
def processSchedule1 () {
    log.debug "processing Mode 1 schedule"
    commandPwFromSchedule (schedule1Mode, schedule1Reserve, schedule1Days)
}

def processSchedule2 () {
    log.debug "processing Mode 2 schedule"
    commandPwFromSchedule (schedule2Mode, schedule2Reserve, schedule2Days)
}
def processSchedule3 () {
    log.debug "processing Mode 3 schedule"
    commandPwFromSchedule (schedule3Mode, schedule3Reserve, schedule3Days)
}
def processSchedule4 () {
    log.debug "processing Mode 4 schedule"
    commandPwFromSchedule (schedule4Mode, schedule4Reserve, schedule4Days)
}

def processSchedule5 () {
    log.debug "processing Mode 5 schedule"
    commandPwFromSchedule (schedule5Mode, schedule5Reserve, schedule5Days)
}

def verifyPowerwalls() {
    try {
        getPowerwalls()
        if (!state.accessTokenValid) {
            return dynamicPage(name: "verifyPowerwalls", title: "Tesla account issue", install: false, uninstall: false, nextPage: "") {
               section("Error verifying Tesla/Powerwall account") {
                 paragraph "Please go back and check your username and password"
             }
        }
       } else if (state.foundPowerwalls) {
            return dynamicPage(name: "verifyPowerwalls", title: "Tap 'Save' to complete installation/update.", install: true, uninstall: false) {
                section("Found Powerwall(s): ") {
                    paragraph "Name: ${state.siteName}\n" +
                         "Id: ${state.pwId}\n" + 
                         "Site Id: ${state.energySiteId}"   
                }
                   
 
            } 
         } else {
            return dynamicPage(name: "verifyPowerwalls", title: "Tesla", install: false, uninstall: true, nextPage: "") {
                section("Error: No Powerwalls found") {
                    paragraph "Please go back and check your username and password"
                }
            }
 
        }
        
    } catch (Exception e) {
        log.error e
        return dynamicPage(name: "verifyPowerwalls", title: "Tesla", install: false, uninstall: true, nextPage: "") {
            section("Error accessing Powerwall account") {
                paragraph "Please check your username and password"
            }
        }
    }
}

private getUrl() { "https://owner-api.teslamotors.com"}
private getId() {"81527cff06843c8634fdc09e8ac0abefb46ac849f38fe1e431c2ef2106796384"}
private getSecret() {"c7257eb71a564034f9419ee651c7d0e5f7aa6bfbd18bafb5c5c033b093bb2fa3"}
private getAgent() {"darwinsden"}

private getToken() {
    if (!state.access_token) {
      try {
        if (state.refresh_token) {
            try {
                httpPostJson([
                    uri: url,
                    path: "/oauth/token",
                    headers: ['User-Agent': agent],
                    body: [
                        grant_type: "refresh_token",
                        client_id: id,
                        client_secret: secret,
                        refresh_token: state.refresh_token
                    ]
                ]) {
                    resp ->
                        state.access_token = resp.data.access_token
                        state.refresh_token = resp.data.refresh_token
                }
            } catch (groovyx.net.http.HttpResponseException e) {
                log.warn e
                state.access_token = null
                if (e.response?.data?.status?.code == 14) {
                    state.refresh_token = null
                }
            }
        }

        if (!state.access_token) {
            httpPostJson([
                uri: url,
                path: "/oauth/token",
                headers: ['User-Agent': agent],
                body: [
                    grant_type: "password",
                    client_id: id,
                    client_secret: secret,
                    email: userEmail,
                    password: userPw
                ]
            ]) {
                resp ->
                state.accessTokenValid = true
                state.access_token = resp.data.access_token
                state.refresh_token = resp.data.refresh_token
            }
        }
    } catch (Exception e) {
        state.accessTokenValid = false
        log.error "Unhandled exception getting token: $e"
     }
    }
    return state.access_token
}

private resetAccountAccess () {
   state.refresh_token = null
   state.access_token = null
   state.accessTokenValid = false
}
 
private httpAuthAsyncGet (handlerMethod, String path) {
    try {
         log.debug "Async requesting: ${path}"
         def requestParameters = [
            uri: url,
            path: path,
            headers: ['User-Agent': agent, Authorization: "Bearer ${token}"]]
         asynchttp_v1.get(handlerMethod, requestParameters)
    } 
    catch (e) {
       log.error "Http Get failed: ${e}"
    }
}

private httpAuthGet(String path, Closure closure) {
    log.debug "requesting: ${path}"
    try {
        def requestParameters = [
            uri: url,
            path: path,
            headers: [
                'User-Agent': agent,
                Authorization: "Bearer ${token}"
            ]
        ]
           httpGet(requestParameters) {
                resp -> closure(resp)
           }
    } 
    catch (e) {
       log.error "Http Get failed: ${e}"
    }
}

private httpAuthPost (Map params = [:], String path, Closure closure) {
    log.debug "Command: ${params}"
    try {
        def requestParameters = [
            uri: url,
            path: path,
            headers: [
                'User-Agent': agent,
                Authorization: "Bearer ${token}"
            ]
        ]

        if (params.body) {
                requestParameters["body"] = params.body
                httpPostJson(requestParameters) {
                    resp -> closure(resp)
                }
            } else {
                httpPost(requestParameters) {
                    resp -> closure(resp)
                }
            }

    } catch (groovyx.net.http.HttpResponseException e) {
        log.error "Request failed for path: ${path}.  ${e.response?.data}"
    }
}

private sendNotificationMessage(message, msgType=null) {
    def sendPushMessage = (!notificationMethod || (notificationMethod.toString() == "push" || notificationMethod.toString() == "text and push"))
    def sendTextMessage = (notificationMethod && (notificationMethod.toString() == "text" || notificationMethod.toString() == "text and push"))
    log.debug "notification message: ${message}"
    if (msgType == null || msgType != "anomaly" || notifyWhenAnomalies?.toBoolean()) {
       if (sendTextMessage == true) {
           if (phoneNumber) {
            sendSmsMessage(phoneNumber.toString(), message)
           }
       }
       if (sendPushMessage == true) {
           sendPush(message)
       }
    }
}

private getPowerwalls() {
       state.foundPowerwalls = false
       httpAuthGet("/api/1/products", {
        resp ->
        //log.debug "response data for products was ${resp.data} "
        resp.data.response.each {
            product ->
                if (product.resource_type == "battery") {
                    state.foundPowerwalls = true
                    log.debug "battery found: ${product.id} site_name: ${product.site_name} energy_site_id: ${product.energy_site_id}"
                    state.energySiteId = product.energy_site_id
                    state.pwId = product.id
                    state.siteName = product.site_name
                }
        }
    })
}

def installed() {
    log.debug "Installed"
    initialize()
}

def updated() {
    log.debug "Updated"
    initialize()
}

def uninstalled() {
    removeChildDevices(getChildDevices())
}

private removeChildDevices(delete) {
    delete.each {
        deleteChildDevice(it.deviceNetworkId)
    }
}

def initialize() {

    createDeviceForPowerwall()

    unsubscribe()
    unschedule()
    setSchedules()

    if (pollingPeriod == "5 minutes") {
       runEvery5Minutes(processMain) 
    } else if (pollingPeriod == "30 minutes") {
       runEvery30Minutes(processMain)   
    } else if (pollingPeriod == "1 hour") {
       runEvery1Hour(processMain)   
    } else if (pollingPeriod != "Do not poll") {
       runEvery10Minutes(processMain) //default
    } else {
       log.debug "not polling Powerwall"
    }
       
    runEvery1Hour(processWatchdog)
    runEvery3Hours(processWatchdog)
    runIn (5, processMain)
}

private createDeviceForPowerwall() {
      def pwDevice = getChildDevice("powerwallDashboard")
      if (!pwDevice) {
             def device = addChildDevice("darwinsden", "Tesla Powerwall", "powerwallDashboard", null, [name: "Tesla Powerwall", label: "Tesla Powerwall", completedSetup: true])
             log.debug "created powerwall device"
      } else {
             log.debug "device for Powerwall already exists"
             pwDevice.initialize()
      }
}

def updateIfChanged(device, attr, value, delta=null) {
    def currentValue=null
    if (state.currentAttrValue==null){
       state.currentAttrValue = [:]
    }
    if (state.currentAttrValue[attr] != null) {
       currentValue=state.currentAttrValue[attr].toString()
    }
    //log.debug "new value: ${value} old value: ${currentValue} attribute: ${attr} delta: ${delta} "
    def deltaMet = (currentValue == null || value != null && delta != null && Math.abs ((value.toInteger() - currentValue.toInteger()).toInteger()) > delta.toInteger())
    def changed = value != null && value != '' && currentValue != null && currentValue != '' && value != currentValue && (!delta || deltaMet)
    state.currentAttrValue[attr] = value.toString()
    def heartBeatUpdateDue = false

    if (state.lastHeartbeatUpdateTime != null) {
        if (state.lastHeartbeatUpdateTime[attr] != null) {
            if ((now() - state.lastHeartbeatUpdateTime[attr])> 60000) {
                heartBeatUpdateDue = true
            }
        }
    } else {
        state.lastHeartbeatUpdateTime = [:]
    }

    if (changed || heartBeatUpdateDue || (currentValue == null && (value != null && value!=''))) {
        device.sendEvent(name: attr, value: value)
        state.lastHeartbeatUpdateTime[attr] = now()
    }
    return changed
}

def checkBatteryNotifications (data) {
     if (notifyWhenReserveApproached?.toBoolean()) {
       if (data.batteryPercent - data.reservePercent < 5) {
          def status
          if (data.batteryPercent <= data.reservePercent) {
              status = "has reached"
          } else {
              status = "is approaching"
          }
          if (state.timeOfLastReserveNotification == null) {
            state.timeOfLastReserveNotification = now()
            sendNotificationMessage("Powerwall battery level of ${data.batteryPercent.round(1)}% ${status} ${data.reservePercent}% reserve level.")   
           } 
        } else if (state.timeOfLastReserveNotification != null && now() - state.timeOfLastReserveNotification >= 30 * 60 * 1000) {
             //reset for new notification if alert condition no longer exists and it's been at least 30 minutes since last notification
             state.timeOfLastReserveNotification = null
        }
     }
     if (notifyWhenLowerLimitReached?.toBoolean() && lowerLimitNotificationValue != null) {
       if (data.batteryPercent <= lowerLimitNotificationValue.toFloat()) {
          if (state.timeOfLastLimitNotification == null) {
            state.timeOfLastLimitNotification = now()
            sendNotificationMessage("Powerwall battery level of ${data.batteryPercent.round(1)}% dropped below notification limit of ${lowerLimitNotificationValue}%.")  
          } 
       } else if (state.timeOfLastLimitNotification != null && now() - state.timeOfLastLimitNotification >= 30 * 60 * 1000) {
            //reset for new notification if alert condition no longer exists and it's been at least 30 minutes since last notification
            state.timeOfLastLimitNotification = null
      }
     }
}
          
def processSiteResponse(response, callData) {
    log.debug "processing site data response"
    def data = response.json.response
    //log.debug "${data}"
    def pwDevice = getChildDevice("powerwallDashboard")
    if (pwDevice) {
        def strategy = data.tou_settings.optimization_strategy
        def strategyUi
        if (strategy == "economics") {
                strategyUi = "Cost-Saving"
        } else if (strategy=="balanced") {
                strategyUi = "Balanced"
        } else {
             strategyUi = strategy
        }
        state.strategy=strategyUi.toString()
        def changed = updateIfChanged(pwDevice, "currentStrategy", strategyUi)
        if (changed && notifyWhenModesChange?.toBoolean()) {             
              sendNotificationMessage("Powerwall ATC optimization strategy changed to ${strategyUi}")              
        }
        
        if (notifyWhenModesChange?.toBoolean() && state?.lastSchedule && data.tou_settings.schedule != state.lastSchedule) {
            sendNotificationMessage("Powerwall Advanced Time Controls schedule has changed") 
        }
        state.lastSchedule=data.tou_settings.schedule  
        //log.debug "sched: ${data.tou_settings.schedule}"
    } else {
        log.debug("No Powerwall device to update")
    }
}

def processPowerwallResponse(response, callData) {
    log.debug "processing powerwall response"
    def data=response.json.response
    //log.debug "${data}"   
    def child = getChildDevice("powerwallDashboard")
    
    if (child) {
        def reservePercent
        if (data.operation == "backup") {
            reservePercent = 100
        } else {
            reservePercent = data.backup.backup_reserve_percent.toInteger()
        }
      
        updateIfChanged(child, "reservePercent", reservePercent.toInteger())
        updateIfChanged(child, "reserve_pending", reservePercent.toInteger())
        
        if (data.total_pack_energy > 1) //sometimes data appears invalid
        {
            def batteryPercent = data.energy_left.toFloat() / data.total_pack_energy.toFloat() * 100.0
            updateIfChanged(child, "battery", batteryPercent.toInteger())
            updateIfChanged(child, "batteryPercent", batteryPercent.round(1))
            runIn(1, checkBatteryNotifications, [data: [batteryPercent: batteryPercent, reservePercent: data.backup.backup_reserve_percent.toInteger()]])
        }
   
        updateIfChanged(child, "loadPower", data.power_reading.load_power[0].toInteger(),100)
        updateIfChanged(child, "gridPower", data.power_reading.grid_power[0].toInteger(),100)
        updateIfChanged(child, "power", data.power_reading.grid_power[0].toInteger(),100)
        updateIfChanged(child, "solarPower", data.power_reading.solar_power[0].toInteger(),100)
        updateIfChanged(child, "powerwallPower", data.power_reading.battery_power[0].toInteger(),100)
        def versionString=''
        if (data.version != null) {
           versionString='V'+data.version.toString()
        }
        //Grid Status
        def gridStatusString
        def gridStatusEnum
        if (data.grid_status == "Inactive") {
           gridStatusString = "Status: Off-Grid" 
           gridStatusEnum = "offGrid"
        } else {
           gridStatusString = "Status: On-Grid"
           gridStatusEnum = "onGrid"
        }
        def changed = updateIfChanged(child, "gridStatus", gridStatusEnum)
        if (changed) {
           if (gridStatusEnum == "offGrid") {
              runIn (1, processOffGridActions)
           } else {
              runIn (1, processOnGridActions)
           } 
         }
        
        updateIfChanged(child, "sitenameAndVers", data.site_name.toString()+' ' + versionString + '\n'+gridStatusString)
        updateIfChanged(child, "siteName", data.site_name.toString())
 
        changed = updateIfChanged(child, "pwVersion", versionString)
        if (changed && notifyWhenVersionChanges?.toBoolean()) {
            sendNotificationMessage("Powerwall software version changed to ${versionString}")
         }
         
        if (data.user_settings.storm_mode_enabled.toBoolean()) {
             updateIfChanged(child, "stormwatch", "Stormwatch: Enabled") 
        } else {
             updateIfChanged(child, "stormwatch", "Stormwatch: Disabled")  
        }
       
        def opMode = "Unknown"
        if (data.operation == "autonomous") {
            opMode = "Time-Based Control"
        } else if (data.operation == "self_consumption") {
            opMode = "Self-Powered"
        } else if (data.operation == "backup") {
            opMode = "Backup-Only"
        }
        changed = updateIfChanged(child, "currentOpState", opMode)
        if (changed && notifyWhenModesChange?.toBoolean()) {
            sendNotificationMessage("Powerwall op mode changed to ${opMode}")     
        }
        //log.debug "grid status is: ${data.grid_status}"         
    }
    state.lastCompletedTime = now()
}
          
def processOffGridActions () 
{
    log.debug "processing off grid actions"
    def child = getChildDevice("powerwallDashboard")
    updateIfChanged(child, "switch", "off")        
    if (notifyWhenGridStatusChanges?.toBoolean()) {
        sendNotificationMessage("Powerwall status changed to: Off Grid")
    }
    if (devicesToOffDuringOutage?.size()) {
        devicesToOffDuringOutage.off()
    }
}

def processOnGridActions () 
{
    log.debug "processing on grid actions"
    def child = getChildDevice("powerwallDashboard")
    updateIfChanged(child, "switch", "on")         
    if (notifyWhenGridStatusChanges?.toBoolean()) {
        sendNotificationMessage("Powerwall status changed to: On Grid")
    }
    if (devicesToOffDuringOutage?.size() && turnDevicesBackOnAfterOutage?.toBoolean()) {
       devicesToOffDuringOutage.on()
    }
}

def requestSiteData() {
    if (!state?.lastSiteRequestTime || now()-state.lastSiteRequestTime > 1000) {
       //log.debug "requesting site info"
       httpAuthAsyncGet('processSiteResponse',"/api/1/energy_sites/${state.energySiteId}/site_info")
       state.lastSiteRequestTime = now()
    }
}

def requestPwData() {
     if (!state?.lastPwRequestTime || now()-state.lastPwRequestTime > 1000) {
       //log.debug "requesting powerwall data"
       httpAuthAsyncGet('processPowerwallResponse',"/api/1/powerwalls/${state.pwId}")
       state.lastPwRequestTime = now()
     }
}

def commandOpMode(data) {
    log.debug "commanding opMode to ${data.mode}"
    httpAuthPost(body:[default_real_mode:data.mode],"/api/1/energy_sites/${state.energySiteId}/operation",{ resp ->
        //log.debug "${resp.data}"
    }
    )
    runIn(2, requestPwData)
    runIn (30, processWatchdog)
}

def setSelfPoweredMode(child) {
	child.sendEvent(name: "currentOpState", value: "Pending Self-Powered", displayed: false)
    runIn(2,commandOpMode,[data: [mode: "self_consumption"]])
}

def setTimeBasedControlMode(child) {
	child.sendEvent(name: "currentOpState", value: "Pending Time-Based", displayed: false)
    runIn(2,commandOpMode,[data: [mode: "autonomous"]])
}

def setBackupOnlyMode(child) {
	child.sendEvent(name: "currentOpState", value: "Pending Backup-Only", displayed: false)
    runIn(2,commandOpMode,[data: [mode: "backup"]])
}

def commandTouStrategy(data)
{
    log.debug "commanding TOU strategy to ${data.strategy}"
    //request Site Data to get a current tbc schedule. Schedule needs to be sent on tou strategy command schedule will be re-set to default
    def latestSchedule
    try {
       httpAuthGet("/api/1/energy_sites/${state.energySiteId}/site_info",
         {resp ->   
           //log.debug "${resp.data}"
           if (resp?.data?.response?.tou_settings?.schedule) {
              latestSchedule = resp.data.response.tou_settings.schedule
              //log.debug "got schedule ${latestSchedule}"
           }
         })
     } catch (Exception e) {
        log.debug "Exception ${e} getting latest schedule"
     }
     if (latestSchedule == null) {
       //log.debug "setting latest schedule to last known state"
       latestSchedule=state.lastSchedule
    }
   
    def commands = [tou_settings:[optimization_strategy:data.strategy,schedule:latestSchedule]]
    httpAuthPost(body:commands,"/api/1/energy_sites/${state.energySiteId}/time_of_use_settings", 
      { resp -> //log.debug "${resp.data}"
         //log.debug "TOU strategy command sent"
      })
    runIn(2, requestSiteData)
    runIn (30, processWatchdog)
}

def setTbcBalanced(child) {
    //log.debug "commanding TBC Balanced"
    child.sendEvent(name: "currentStrategy", value: "Pending Balanced", displayed: false)
    runIn(2,commandTouStrategy,[data: [strategy: "balanced"]])
}

def setTbcCostSaving(child) {
    //log.debug "commanding TBC CostSaving"
    child.sendEvent(name: "currentStrategy", value: "Pending Cost-Saving", displayed: false)
    runIn(2,commandTouStrategy,[data: [strategy: "economics"]])
}

def commandBackupReservePercent(data) {
   log.debug "commanding reserve to ${data.reservePercent}%"
   httpAuthPost(body:[backup_reserve_percent:data.reservePercent],"/api/1/energy_sites/${state.energySiteId}/backup", { resp ->
      }
    )
   runIn(2, requestPwData)
   runIn (30, processWatchdog)
}

def setBackupReservePercent(child, value) {
    //log.debug "commanding reserve to ${value}%"
    if (value >= 0 && value <= 100) {
       runIn(2,commandBackupReservePercent,[data: [reservePercent:value]])
    } else {
       log.debug "Backup reserve percent of: ${value} not sent. Must be between 0 and 100"
    }
}
        
def refresh(child) {
    log.debug "refresh requested"
    runIn(1, processMain)  
    runIn (30, processWatchdog)
}

def processWatchdog() {
    def lastTimeProcessed
    def lastTimeCompleted
    if (!state.lastProcessedTime | !state.lastCompletedTime) {
        lastTimeProcessed = now()
        lastTimeCompleted = now()
    } else {
        lastTimeProcessed = state.lastProcessedTime
        lastTimeCompleted = state.lastCompletedTime
    }

    def secondsSinceLastProcessed = (now() - lastTimeProcessed) / 1000
    def secondsSinceLastProcessCompleted = (now() - lastTimeCompleted) / 1000

    if (secondsSinceLastProcessed > 1800) {
        if (!state?.processedWarningSent) {
           sendNotificationMessage("Warning: Powerwall Manager has not executed in ${(secondsSinceLastProcessed/60).toInteger()} minutes. Reinitializing","anomaly")
           state.processedWarningSent = true
        }
        runIn(30, initialize)
    } else {
       if (secondsSinceLastProcessCompleted > 1800) {
          if (!state?.completedWarningSent) {
              sendNotificationMessage("Warning: Powerwall Manager has not successfully received and processed data in ${(secondsSinceLastProcessCompleted/60).toInteger()} minutes. Reinitializing","anomaly")
              state.completedWarningSent = true
          }
          runIn(30, initialize)
       } else {
          if (state?.completedWarningSent || state?.processedWarningSent) {
             sendNotificationMessage("Info: Powerwall Manager has successfully resumed operation","anomaly")
             state.completedWarningSent = false 
             state.processedWarningSent = false
          }      
       }
    }

}

def processMain () {
    state.lastProcessedTime = now()
    def lastStateProcessTime
    if (state.lastStateRunTime == null) {
        lastStateProcessTime = 0
    } else {
       lastStateProcessTime = state.lastStateRunTime
    }
    def secondsSinceLastRun = (now() - lastStateProcessTime) / 1000
    if (secondsSinceLastRun > 60) {
        state.lastStateRunTime = now()
        runIn (1, requestPwData)
        runIn (10, requestSiteData)
    }
}