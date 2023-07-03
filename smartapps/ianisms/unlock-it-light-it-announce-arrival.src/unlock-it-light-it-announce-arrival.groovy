/**
 *  Unlock it, Light it, Announce Arrival SmartApp
 *
 *  Copyright 2015 Ian N. Bennett
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
 *  Date: 2015-02-23
 */

definition(
    name: "Unlock it, Light it, Announce Arrival",
    namespace: "ianisms",
    author: "Ian N. Bennett",
    description: "On arrival: unlocks the door and after door opens, turns on lights and announces arrival",
    category: "Safety & Security",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Allstate/lock_it_when_i_leave.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Allstate/lock_it_when_i_leave%402x.png",
    oauth: true
)

preferences {
    section(hideable: true, "Basics") {
        input "enableApp", "boolean", title: "Enable App?", required: true, defaultValue: true
        input "presenceSensors", "capability.presenceSensor", title: "Which presence sensor(s)?", multiple: true
        input "presenceSensorNamePattern", "text", title: "Presnse sensor name pattern", description: "Ex.: 's for Joe's iPhone will resolve to Joe", multiple: false, required: false, capitalization: "none"
        input "locks", "capability.lock", title: "Which Lock(s)?", multiple: true
        input "doorContacts", "capability.contactSensor", title: "Which Door Contact(s)?", multiple: true
    }
        
    section(hideable: true, "Light Control") {
        input "enableLightControl", "boolean", title: "Turn on light after arrival?", defaultValue: true
        input "switches", "capability.switch", title: "Which switch(es)?", multiple: true, required: false
        input "hues", "capability.colorControl", title: "Which Hue Bulb(s)?", required:false, multiple:true
        input "color", "enum", title: "Hue Color:", required: false, multiple:false, options: [
            ["Daylight":"Daylight - Energize, Default"],
            ["Soft White":"Soft White"],
            ["White":"Concentrate"],
            ["Warm White":"Relax"],
            "Red","Green","Blue","Yellow","Orange","Purple","Pink"]
        input "brightnessLevel", "number", title: "Set light brightness to:", description: "0-100% in increments of 10", required: false, defaultValue: 80
    }

    section(hideable: true, "Greetings and Notfications") {
        input "enableGreetings", "boolean", title: "Greet on Arrival?", defaultValue: true
        input "speechDevices", "capability.musicPlayer", title: "Speech Device(s):", multiple: true, required: false
        input "volume", "number", title: "Temporarily change volume to:", description: "0-100%", required: false, defaultValue: 80
        input "enableNotifications", "boolean", title: "Send push notifications?", defaultValue: true
    }

    section(hideable: true, "Debugging") {
        input "logLevel", "enum", title: "Log Level:",  required: false, defaultValue: "Info", options: ["Info","Debug","Trace","Events"]
    }
}

def installed()
{
    init()
}

def updated()
{
    unsubscribe()
    init()
}

def init() {    
    if(enableApp == "true") {
        log("init: App is enabled...")

        state.presence = null
        state.newArrival = false
        state.isDark = false

        subscribe(app, appTouch)    
        subscribe(presenceSensors, "presence.present", presence)
        subscribe(doorContacts, "contact.open", contactOpen)

        if(enableLightControl == "true") {
            log("init: light control enabled...")
            subscribe(location, "sunrise", sunriseHandler)
            subscribe(location, "sunset", sunsetHandler)
        }
    
        if (enableGreetings == "true" && doorContacts != null) {
            log("init: greetings after arrival enabled...")
            subscribe(doorContacts, "contact.open", contactOpen)
        }
    } else {        
        log("init: App is disabled...")
    }
}

def appTouch(evt) {
    speak("state.presence is ${state.presence}, state.newArrival is ${state.newArrival}, and state.isDark is ${state.isDark}")
}

def presence(evt)
{ 
    state.presence = evt.displayName.toLowerCase();
    state.newArrival = true
    def endIndex = state.presence.length();
    if(presenceSensorNamePattern) {
        endIndex = state.presence.indexOf(presenceSensorNamePattern);
        state.presence = state.presence.substring(0, (endIndex > 0 ? endIndex : state.presence.length()));
    }
        
    log("presence: arrival of ${state.presence}")

    def anyLocked = locks.count{it.currentLock == "unlocked"} != locks.size()

    if (anyLocked) {
        locks.unlock()
        if (enableNotifications == "true") {            
            sendPush("Unlocked locks on arrival of ${state.presence}")
        }

        sendNotificationEvent("Unlocked locks on arrival of ${state.presence}") 
    }
        
    if (enableLightControl == "true" && state.isDark == true) {
        log("presence: turning on lights when dark")
        lightsOn()
    }

}

def sunriseHandler(evt) {
    log("sunriseHandler")
    state.isDark = false
}

def sunsetHandler(evt) {
    log("sunsetHandler")
    state.isDark = true
}

def contactOpen(evt) {	    
    if(state.presence != null && state.newArrival == true) { 
        state.newArrival = false
        
        log("contactOpen: ${evt.displayName} open after new arrival of ${state.presence}")
        
        if (enableGreetings == "true" && speechDevices != null) {   
            speak("Welcome home, ${state.presence}")
        }

        state.presence = null
    }
}

private lightsOn() {
    if(switches != null) {
        switches.on()
        log("lightsOn: turned on ${switches}")
    }
    
    def hueColor = 53
    def saturation = 91

    switch(color) {
        case "White":
            hueColor = 52
            saturation = 19
            break;
        case "Daylight":
            hueColor = 53
            saturation = 91
            break;
        case "Soft White":
            hueColor = 23
            saturation = 56
            break;
        case "Warm White":
            hueColor = 20
            saturation = 80 //83
            break;
        case "Blue":
            hueColor = 70
            break;
        case "Green":
            hueColor = 39
            break;
        case "Yellow":
            hueColor = 25
            break;
        case "Orange":
            hueColor = 10
            break;
        case "Purple":
            hueColor = 75
            break;
        case "Pink":
            hueColor = 83
            break;
        case "Red":
            hueColor = 100
            break;
    }

    hues*.setColor([hue: hueColor, saturation: saturation, level: brightnessLevel as Integer ?: 100])
}

private speak(msg) {

    if(speechDevices != null) {
        log("speak: speaking ${msg} on ${speechDevices}")      
        speechDevices.playTextAndRestore(msg, 85)
    }
}

private log(msg, level = logLevel) {
    
    switch(level) {
        case "Info":
            log.info(msg)  
            break;
        case "Debug":
            log.debug(msg)  
            break;
        case "Trace":
            log.trace(msg)  
            break;
        case "Events":
            log.trace(msg)  
            sendNotificationEvent(msg) 
            break;
    }
}