/**
 *  Tesla Powerwall Manager 
 * 
 *  Copyright 2019 DarwinsDen.com
 *  
 *  ****** WARNING ****** USE AT YOUR OWN RISK!
 *  This software was developed in the hopes that it will be useful to others, however, 
 *  it is beta software and may have unforesoon side effects to your equipment and related accounts.
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
    return "v0.1.1e.20190723"
}

/*   
 *	23-Jun-2019 >>> v0.1.1e.20190723 - Initial beta release
 *
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
    page(name: "pageRemove")
}

private pageMain() {
    return dynamicPage(name: "pageMain", title: "", nextPage: "verifyPowerwalls") {
        section() {
            paragraph app.version(),
                title: "PowerWall Manager", required: false, image: "https://rawgit.com/DarwinsDen/SmartThingsPublic/master/resources/icons/pwLogoAlphaCentered.png"
        }
        section("Tesla Account Information: ") {
           href "accountInfo", title: "Tesla account Information..", description: "", required: (!userEmail || !userPw), image: "https://rawgit.com/DarwinsDen/SmartThingsPublic/master/resources/icons/Tesla-Icon.png"
        }
        section("Choose how you get notified") {
            href "pageNotifications", title: "Notification preferences..", description: "", required: false,
                image: "https://rawgit.com/DarwinsDen/SmartThingsPublic/master/resources/icons/notification.png"
        }

        section() {
            paragraph "For more information, questions, or to provide feedback, please visit: DarwinsDen.com/powerwall", title: "", required: false,
                image: "https://darwinsden.com/download/ddlogo-for-pwmanager-0-png"
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
            input "notifyWhenVersionChanges", "boolean", required: false, defaultValue: false, title: "Notify when powerwall version changes"
            input "notifyWhenOpModeChanges", "boolean", required: false, defaultValue: false, title: "Notify of operational mode changes (Backup-Only/Self-Powered/Advanced Time-Based Controls)"
            input "notifyWhenStrategyChanges", "boolean", required: false, defaultValue: false, title: "Notify of Advanced Time Controls Strategy Changes (Balanced/Cost-Saving)"
            input "notifyWhenReserveApproached", "boolean", required: false, defaultValue: false, title: "Notify when energy left percentage approaches reserve percentage"
            input "notifyWhenLowerLimitReached", "boolean", required: false, defaultValue: false, title: "Notify when energy left percentage reaches a lower limit"
            input "lowerLimitNotificationValue", "number", required: false, title: "Percentage value to use for Lower Limit Notification"
            input "notifyWhenTouScheduleChanges", "boolean", required: false, defaultValue: false, title: "Notify of Advanced Time Controls Schedule Changes (Peak/Off-Peak hours)"
            input "notifyWhenAnomalies", "boolean", required: false, defaultValue: true, title: "Notify when anomalies are encountered in the Powerwall Manager SmartApp"
        }
        
        section("Notification method (push notifications are via ST app) and phone number if text/SMS messages are selected") {
            input "notificationMethod", "enum", required: false, defaultValue: "push", title: "Notification Method", options: ["none", "text", "push", "text and push"]
            input "phoneNumber", "phone", title: "Phone number for text messages", description: "Phone Number", required: false
        }
    }
}

def verifyPowerwalls() {
    try {
        getPowerwalls()
        if (!state.accessTokenValid) {
            return dynamicPage(name: "verifyPowerwalls", title: "Tesla", install: false, uninstall: true, nextPage: "") {
               section("Error verifying Powerwall account") {
                 paragraph "Please go back and check your username and password"
             }
        }
       } else if (state.foundPowerwalls) {
            return dynamicPage(name: "verifyPowerwalls", title: "Tesla", install: true, uninstall: true) {
                section("Found Powerwall(s):") {
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
    if (sendTextMessage == true) {
        if (phoneNumber) {
            sendSmsMessage(phoneNumber.toString(), message)
        }
    }
    if (sendPushMessage == true) {
        sendPush(message)
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
        if (notifyWhenAnomalies?.toBoolean()) {
           sendNotificationMessage("Warning: Powerwall Manager has not processed in ${(secondsSinceLastProcessed/60).toInteger()} minutes. Reinitializing")
        }
        runIn(30, initialize)
    } else if (secondsSinceLastProcessCompleted > 1800) {
        if (notifyWhenAnomalies?.toBoolean()) {
           sendNotificationMessage("Warning: Powerwall Manager has not successfully run in ${(secondsSinceLastProcessCompleted/60).toInteger()} minutes. Reinitializing")
        }
        runIn(30, initialize)
    }
}

def processMain () {
    state.lastProcessedTime = now()
    runIn (1, requestPwData)
    def lastStateProcessTime
    if (state.lastStateRunTime == null) {
        lastStateProcessTime = 0
    } else {
       lastStateProcessTime = state.lastStateRunTime
    }
    def secondsSinceLastRun = (now() - lastStateProcessTime) / 1000
    if (secondsSinceLastRun > 500) {
        state.lastStateRunTime = now()
        //runIn (30, processStateEvents)
        runIn (10, requestSiteData)
    }
}

def initialize() {

    ensureDevicesForPowerwalls()

    unsubscribe()
    unschedule()
    
    runEvery10Minutes(processMain)
    runEvery1Hour(processWatchdog)
    runEvery3Hours(processWatchdog)
    runIn (5, processMain)
}

private ensureDevicesForPowerwalls() {
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
          if (state.timeOfLastReserveNotification == null) {
            state.timeOfLastReserveNotification = now()
            sendNotificationMessage("Powerwall battery level of ${data.batteryPercent.round(1)}% is approaching or has reached ${data.reservePercent}% reserve level.")   
           } 
        } else if (now() - state.timeOfLastReserveNotification >= 30 * 60 * 1000) {
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
       } else if (now() - state.timeOfLastLimitNotification >= 30 * 60 * 1000) {
            //reset for new notification if alert condition no longer exists and it's been at least 30 minutes since last notification
            state.timeOfLastLimitNotification = null
      }
     }
}
          
def processSiteResponse(response, callData) {
    log.debug "processing site data response"
    def data = response.json.response
    // log.debug "${data}"
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
        if (changed && notifyWhenStrategyChanges?.toBoolean()) {             
              sendNotificationMessage("Powerwall ATC optimization strategy changed to ${strategyUi}")              
        }
        
        if (notifyWhenTouScheduleChanges?.toBoolean() && state?.lastSchedule && data.tou_settings.schedule != state.lastSchedule) {
            sendNotificationMessage("Powerwall Advanced Time Controls schedule has changed") 
        }
        state.lastSchedule=data.tou_settings.schedule          
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
        updateIfChanged(child, "reservePercent", data.backup.backup_reserve_percent.toInteger())
        updateIfChanged(child, "reserve_pending", data.backup.backup_reserve_percent.toInteger())
        
        if (data.total_pack_energy > 1) //sometimes data appears invalid
        {
            def batteryPercent = data.energy_left.toFloat() / data.total_pack_energy.toFloat() * 100.0
            updateIfChanged(child, "battery", batteryPercent.round(1))
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
        updateIfChanged(child, "sitenameAndVers", data.site_name.toString()+' ' + versionString)
        updateIfChanged(child, "siteName", data.site_name.toString())
        def changed = updateIfChanged(child, "pwVersion", versionString)
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
        if (changed && notifyWhenOpModeChanges?.toBoolean()) {
            sendNotificationMessage("Powerwall op mode changed to ${opMode}")     
        }
    }
    state.lastCompletedTime = now()
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
    //log.debug "commanding TOU strategy"
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
   //log.debug "commanding reserve to ${data.reservePercent}%"
   httpAuthPost(body:[backup_reserve_percent:data.reservePercent],"/api/1/energy_sites/${state.energySiteId}/backup", { resp ->
      }
    )
   runIn(2, requestPwData)
}

def setBackupReservePercent(child, value) {
    //log.debug "commanding reserve to ${value}%"
    runIn(2,commandBackupReservePercent,[data: [reservePercent:value]])
}
        
def refresh(child) {
    log.debug "refresh requested"
    runIn(1, requestPwData)     
    runIn(5, requestSiteData)
}