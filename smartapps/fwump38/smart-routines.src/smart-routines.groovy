/**
 *  Smart Routines
 *
 *  Copyright 2015 David Mirch
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
    name: "Smart Routines",
    namespace: "fwump38",
    author: "David Mirch",
    description: "An app to assign routines to modes and automatically switch between modes based on time and presence.",
    category: "Mode Magic",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")

preferences {
    page(name: "selectRoutines")
    page(name: "selectTimes")
    page(name: "selectPeople")
    page(name: "modeTime")
}

def selectRoutines() {
    dynamicPage(name: "selectRoutines", title: "Routine Selection", nextPage: "selectTimes", uninstall: true) {

        section("Set a routine for each mode selected") {
            def m = location.mode
            def myModes = []
            location.modes.each {myModes << "$it"}
            input "modesX", "enum", multiple: true, title: "Select mode(s)", submitOnChange: true, options: myModes.sort(), defaultValue: [m]
            def sortModes = modesX
            if(!sortModes) setModeRoutine(m, "routine$m")
            if(!sortModes) setModeStatus(m, "routine$m")
            else sortModes = sortModes.sort()
            sortModes.each {setModeRoutine(it, "routine$it")}
            sortModes.each {setModeStatus(it, "routine$it")}
        }
    }
}

def setModeRoutine(thisMode, modeRoutine) {
     // get the available actions
    def actions = location.helloHome?.getPhrases()*.label
    if (actions) {
        // sort them alphabetically
        actions.sort()
    }
    def result = input modeRoutine, "enum", multiple: false, title: "Routine for $thisMode", required: true options: actions
}

def setModeStatus(thisMode, modeStatus) {
    def result = input modeStatus, type: "bool", title: "Status for $thisMode (ON=Home OFF=Away)", required: true, defaultValue: true 
}

def selectTimes() {
    dynamicPage(name:"selectTimes",title: "Time Range Selection", nextPage: "selectPeople", uninstall: true) {

        section("Set a time range for each mode selected") {
            def m = location.mode
            def myModes = []
            location.modes.each {myModes << "$it"}
            input "modesX", "enum", multiple: true, title: "Select mode(s)", submitOnChange: true, options: myModes.sort(), defaultValue: [m]
            def sortModes = modesX
            if(!sortModes) setModeStart(m, "modeStart$m")
            if(!sortModes) setModeEnd(m, "modeEnd$m")
            else sortModes = sortModes.sort()
            sortModes.each {setModeStart(it, "modeStart$it")}
            sortModes.each {setModeEnd(it, "modeEnd$it")}
        }

    }
}

def setModeStart(thisMode, startingX) {
    def result = input startingX, "enum", title: "Starting at", options: ["A specific time", "Sunrise", "Sunset"], defaultValue: "A specific time", submitOnChange: true
            if(startingX in [null, "A specific time"]) input "starting", "time", title: "Start time", required: false
            else {
                if(startingX == "Sunrise") input "startSunriseOffset", "number", range: "*..*", title: "Offset in minutes (+/-)", required: false
                else if(startingX == "Sunset") input "startSunsetOffset", "number", range: "*..*", title: "Offset in minutes (+/-)", required: false
            }
}

def setModeEnd(thisMode, endingX) {
    def result = input "endingX", "enum", title: "Ending at", options: ["A specific time", "Sunrise", "Sunset"], defaultValue: "A specific time", submitOnChange: true
            if(endingX in [null, "A specific time"]) input "ending", "time", title: "End time", required: false
            else {
                if(endingX == "Sunrise") input "endSunriseOffset", "number", range: "*..*", title: "Offset in minutes (+/-)", required: false
                else if(endingX == "Sunset") input "endSunsetOffset", "number", range: "*..*", title: "Offset in minutes (+/-)", required: false
            }
}

def selectPeople() {
    dynamicPage(name: "selectPeople", title: "People Selection", uninstall: true, install: true) {

        section("Change Home Modes According To Schedule") {
            input "peopleHome", "capability.presenceSensor", multiple: true, title: "If any of these people are home..."
            input "falseAlarmThresholdHome", "decimal", title: "Number of minutes", required: false, defaultValue: 10
            paragraph "If any of these people are home, the mode will change according to the home schedule."
        }
        section("Change Away Modes According To Schedule") {
            input "peopleAway", "capability.presenceSensor", multiple: true, title: "If all of these people are away..."
            input "falseAlarmThresholdAway", "decimal", title: "Number of minutes", required: false, defaultValue: 10
            paragraph "If all of these people leave, the mode will change according to the away schedule."
        }
    }
}

def modeTime() {
    dynamicPage(name:"modeTime",title: "Set a time range for $thisMode", uninstall: false) {
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




private offset(value) {
    def result = value ? ((value > 0 ? "+" : "") + value + " min") : ""
}

private timeIntervalLabel() {
    def result = ""
    if (startingX == "Sunrise" && endingX == "Sunrise") result = "Sunrise" + offset(startSunriseOffset) + " to Sunrise" + offset(endSunriseOffset)
    else if (startingX == "Sunrise" && endingX == "Sunset") result = "Sunrise" + offset(startSunriseOffset) + " to Sunset" + offset(endSunsetOffset)
    else if (startingX == "Sunset" && endingX == "Sunrise") result = "Sunset" + offset(startSunsetOffset) + " to Sunrise" + offset(endSunriseOffset)
    else if (startingX == "Sunset" && endingX == "Sunset") result = "Sunset" + offset(startSunsetOffset) + " to Sunset" + offset(endSunsetOffset)
    else if (startingX == "Sunrise" && ending) result = "Sunrise" + offset(startSunriseOffset) + " to " + hhmm(ending, "h:mm a z")
    else if (startingX == "Sunset" && ending) result = "Sunset" + offset(startSunsetOffset) + " to " + hhmm(ending, "h:mm a z")
    else if (starting && endingX == "Sunrise") result = hhmm(starting) + " to Sunrise" + offset(endSunriseOffset)
    else if (starting && endingX == "Sunset") result = hhmm(starting) + " to Sunset" + offset(endSunsetOffset)
    else if (starting && ending) result = hhmm(starting) + " to " + hhmm(ending, "h:mm a z")