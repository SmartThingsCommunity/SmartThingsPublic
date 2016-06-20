//#1.65   Mar 25, 2016

/**
 *  Blue Iris Integration
 *
 *  Copyright 2014 Tony Gutierrez
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
definition(
    name: "Blue Iris Profile Integration",
    namespace: "pursual",
    author: "Tony Gutierrez",
    description: "Integration with Blue Iris JSON Interface for purposes of toggling recording/alert profiles.",
    category: "Safety & Security",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")

/**
 *  
 *
 *  Author: pursual
 *  Date: 2016-03-04
 *
 *  Sets Blue Iris to a profile that corresponds to the Smartthings "mode". 
 */

preferences {
    page(name:"selectModes")
    page(name:"BISettings")
}

def selectModes() {  
  dynamicPage(name: "selectModes", title: "Mode and Profile Matching", nextPage:"BISettings", uninstall:true) {    
    section("") {
        paragraph "Numbers 1-7 correspond to Blue Iris profile numbers. To ignore a mode leave it blank. A profile of 0 sets Blue Iris to 'incative'."
        location.modes.each { mode ->
            def modeId = mode.id.toString()  
            input "mode-${modeId}", "number", title: "Mode ${mode}", required: false
        }
    }
  }
}

def BISettings() {
    dynamicPage(name:"BISettings", "title":"Blue Iris Login Info", uninstall:true, install:true) {
        section( "" ) {
            input "host", "string", title: "BI WAN Server Host(include http://)", required:true
            input "port", "number", title: "BI WAN Server Port (81?)", required:true, default:81
            input "username", "string", title: "BI Username", required: true
            input "password", "string", title: "BI Password", required: true
            paragraph "Currently, BI only allows Admin Users to toggle profiles. ST only allows WAN traffic, so internal IPs will not work. Use a DDNS service to get a WAN address."
        }
    }
}

def installed() {
    log.debug "Installed with settings: ${settings}"
    unsubscribe()
    subscribe(location, modeChange)
}

def updated() {
    log.debug "Updated with settings: ${settings}"
    unsubscribe()
    subscribe(location, modeChange)    
}

def modeChange(evt)
{
    if (evt.name != "mode") {return;}
    log.debug "BI_modeChange detected. " + evt.value
    def checkMode = ""
    
    //easiest way to get mode by id. Didnt want to use names.
    location.modes.each { mode ->
        if (mode.name == evt.value){
            checkMode = "mode-" + mode.id
            log.debug "BI_modeChange matched to " + mode.name
        }
    }
    
    if (checkMode != "" && settings[checkMode]){
        log.debug "BI_Found profile " + settings[checkMode]
        takeAction(settings[checkMode].toInteger());
    }
}

def takeAction(profile)
{
    def errorMsg = "Could not adjust Blue Iris :("
    
    try {
        httpPostJson(uri: host + ':' + port, path: '/json',  body: ["cmd":"login"]) { response ->
            //log.debug response.data

            if (response.data.result == "fail")
            {
               log.debug "BI_Inside initial call fail, proceeding to login"
               def session = response.data.session
               def hash = username + ":" + response.data.session + ":" + password
               hash = hash.encodeAsMD5()

               httpPostJson(uri: host + ':' + port, path: '/json',  body: ["cmd":"login","session":session,"response":hash]) { response2 ->
                    if (response2.data.result == "success") {
                        def BIprofileNames = response2.data.data.profiles;
                        log.debug ("BI_Logged In")
                        //log.debug response2.data
                        httpPostJson(uri: host + ':' + port, path: '/json',  body: ["cmd":"status","session":session]) { response3 ->
                            log.debug ("BI_Retrieved Status");
                            //log.debug response3.data
                            if (response3.data.result == "success"){
                                if (response3.data.data.profile != profile){
                                    httpPostJson(uri: host + ':' + port, path: '/json',  body: ["cmd":"status","profile":profile,"session":session]) { response4 ->
                                        //log.debug response4.data
                                        if (response4.data.result == "success") {
                                            if (response4.data.data.profile.toInteger() == profile.toInteger()) {
                                                log.debug ("I set Blue Iris to profile ${profileName(BIprofileNames,profile)}!")
                                                sendNotificationEvent("I set Blue Iris to profile ${profileName(BIprofileNames,profile)}!")
                                            } else {
                                                log.debug ("Hmmm...Blue Iris ended up on profile ${profileName(BIprofileNames,response4.data.data.profile)}? I tried ${profileName(BIprofileNames,profile)}. Check your user permissions.")
                                                sendNotificationEvent("Hmmm...Blue Iris ended up on profile ${profileName(BIprofileNames,response4.data.data.profile)}? I tried ${profileName(BIprofileNames,profile)}. Check your user permissions.");
                                            }
                                            httpPostJson(uri: host + ':' + port, path: '/json',  body: ["cmd":"logout","session":session]) { response5 ->
                                                //log.debug response5.data
                                                //log.debug "Logged out?"
                                            }
                                        } else {
                                            log.debug "BI_FAILURE"
                                            log.debug(response4.data.data.reason)
                                            sendNotificationEvent(errorMsg)
                                        }
                                    }
                                } else {
                                    log.debug ("Blue Iris is already at profile ${profileName(BIprofileNames,profile)}.")
                                    sendNotificationEvent("Blue Iris is already at profile ${profileName(BIprofileNames,profile)}.")
                                    }
                            } else {
                                log.debug "BI_FAILURE"
                                log.debug(response3.data.data.reason)
                                sendNotificationEvent(errorMsg)
                            }
                        }
                    } else {
                        log.debug "BI_FAILURE"
                        log.debug(response2.data.data.reason)
                        sendNotificationEvent(errorMsg)
                    }
                }
            } else {
                log.debug "FAILURE"
                log.debug(response.data.data.reason)
                sendNotificationEvent(errorMsg)
            }
        }
    } catch(Exception e) {
        log.debug(e)
        sendNotificationEvent(errorMsg);
    }
}

def profileName(names, num) {
    if (names[num.toInteger()]) {
        names[num.toInteger()] + " (#${num})"
    } else {
        '#' + num
    }
}
