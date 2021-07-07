/**
*  Switch Changes Mode
*
*  Copyright 2016 Elfege
*  Version 2.0 3/8/15
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
*  Ties a mode to a switch's (virtual or real) on/off state. Perfect for use with IFTTT.
*  Simple define a switch to be used, then tie the on/off state of the switch to a specific mode.
*  Connect the switch to an IFTTT action, and the mode will fire with the switch state change.
*
*
*/
definition(
    name: "Routines and Switches Advanced",
    namespace: "ELFEGE",
    author: "ELFEGE",
    description: "Ties several routines to several switches and a power meter and toggles them back, depending on time and/or current mode (ideal for when you start watching TV).",
    category: "Convenience",
    iconUrl: "http://elfege.com/penrose.jpg")

preferences {
    page(name: "settings", title: "Select your preferences", install: true, uninstall: true) 
}
def settings() {
    dynamicPage(name: "settings", title: "Select a Switch and a routine", install:true, uninstall: true) {

        // 1st SWITCH
        section("This section defines a switch which can work with several optional conditions and routines based on power time, current mode and power consumption and other switches"){
        }
        section("Select a switch") {
            input "controlSwitch1", "capability.switch", title: "Switch", multiple: false, required: true 

        }
        section("When this switch is ON, run this routine")
        // get the available Routines
        def Routine = location.helloHome?.getPhrases()*.label
        if(Routine) {
            // sort them alphabetically
            Routine.sort()
            section("Pick a routine") {
                log.trace Routine
                // use the actions as the options for an enum input
                input "RunRoutine1", "enum", title: "Select a routine to execute", options: Routine, required: true 
            }
        }
        section("Do you want to dimm some lights?"){
            input "dimmer", "bool", title: "select preference", submitOnChange: true, default: false


            if(dimmer){
                input "controlSwitch1Level", "capability.switchLevel", title: "LevelSwitch", multiple: true
                input "levelTVon", "decimal", title:"when $controlSwitch1 is on, set to this level"
                input "levelTVoff", "decimal", title: "when $controlSwitch1 is off, set to this level"
            }
        }

        section ("OPTIONAL FEATURES available only for the first switch") {
        }
        section {
            input(name: "Back", type: "bool", title: "Optional : Do you wish to run another routine once this same switch is off?", options: ["true","false"], required: false, default: false)             
            input(name: "ExceptionMode", type: "mode", title: "But do not run this other routine if I'm already in the following mode:", required: false) 
        }
        section("Select the optional routine which will run once the switch is off:")
        // get the available Routines
        if(Routine) {
            // sort them alphabetically
            Routine.sort()
            section("Routine:") {
                log.trace Routine
                // use the actions as the options for an enum input
                input "RunRoutine1B", "enum", title: "Select a routine to execute", options: Routine, required: false 
            }
        }
        section("But if home is between these hours") {
            input "timeBegin", "time", title: "Time of Day to start"
            input "timeEnd", "time", title: "Time of Day to stop"
        }
        section("Then Run this routine instead")
        if(Routine) {
            // sort them alphabetically
            Routine.sort()
            section("Routine:") {
                log.trace Routine
                // use the actions as the options for an enum input
                input "RunRoutine1C", "enum", title: "Run this routine instead", options: Routine, required: false 
            }
        }
        section("And if home is between these hours") {
            input "timeBegin2", "time", title: "Time of Day to start"
            input "timeEnd2", "time", title: "Time of Day to stop"
        }
        section("Then Run this routine instead")
        if(Routine) {
            // sort them alphabetically
            Routine.sort()
            section("Routine:") {
                log.trace Routine
                // use the actions as the options for an enum input
                input "RunRoutine1D", "enum", title: "Run this routine instead", options: Routine, required: false 
            }
        }
        // POWER METER
        section {
            input(name: "meter", type: "capability.powerMeter", title: "When This Power Meter is below threshodl", required: false, multiple: false, description: null)
            input "SwitchesToPwOff", "capability.switch", title: "Turn off these swithces ", multiple: true, required: false
            input(name: "threshold", type: "number", title: "power threshold", required: false, description: "in either watts or kw.")
            input(name: "delay", type: "number", title: "After how long?", required: false, description: "time in minutes.")
        }

        section("SWITCHES AND ROUTINES with no extra features"){
        }

        section("Now, do you want all the following switches to automatically be turned off after having been turned on? This May allow more reliability by avoiding concommitent states") {
            input(name:"Toggle", type: "enum", title: "yes? No?", options:["yes", "no"], default:"yes")
        }
        // 2ND SWITCH
        section("Select a switch to use...") {
            input "controlSwitch2", "capability.switch", title: "Switch", multiple: false, required: false

        }
        section("When this switch is ON, run this routine")
        // get the available Routines
        if(Routine) {
            // sort them alphabetically
            Routine.sort()
            section("Select a routine") {
                log.trace Routine
                // use the actions as the options for an enum input
                input "RunRoutine2", "enum", title: "Select a routine to execute", options: Routine, required: false 
            }
        }

        // 3RD SWITCH
        section("Select a switch to use...") {
            input "controlSwitch3", "capability.switch", title: "Switch", multiple: false, required: false
        }
        section("When this switch is ON, run this routine")
        // get the available Routines
        if(Routine) {
            // sort them alphabetically
            Routine.sort()
            section("Pick a routine") {
                log.trace Routine
                // use the actions as the options for an enum input
                input "RunRoutine3", "enum", title: "Select a routine to execute", options: Routine, required: false 
            }
        }
        // 4TH SWITCH
        section("Select a switch") {
            input "controlSwitch4", "capability.switch", title: "Switch", multiple: false, required: false
        }
        section("When this switch is ON, run this routine")
        // get the available Routines
        if(Routine) {
            // sort them alphabetically
            Routine.sort()
            section("Select a routine") {
                log.trace Routine
                // use the actions as the options for an enum input
                input "RunRoutine4", "enum", title: "Select a routine to execute", options: Routine, required: false 
            }
        }
        // 5TH SWITCH
        section("Select a switch") {
            input "controlSwitch5", "capability.switch", title: "Switch", multiple: false, required: false
        }
        section("When this switch is ON, run this routine")
        // get the available Routines
        if(Routine) {
            // sort them alphabetically
            Routine.sort()
            section("Select a routine") {
                log.trace Routine
                // use the actions as the options for an enum input
                input "RunRoutine5", "enum", title: "Select a routine to execute", options: Routine, required: false 
            }
        }
    }
}

def installed() {
    log.debug "Installed with settings: ${settings}"

    subscribe(controlSwitch1, "switch", "switchHandler1")  
    subscribe(controlSwitch2, "switch", "switchHandler2")
    subscribe(controlSwitch3, "switch", "switchHandler3")
    subscribe(controlSwitch4, "switch", "switchHandler4")
    subscribe(controlSwitch5, "switch", "switchHandler5")
    subscribe(meter, "power", "MeterCountHandler")

    if (state.modeStartTime == null) {
        state.modeStartTime = 0
    }

}

def updated() {
    log.debug "Updated with settings: ${settings}"

    unsubscribe()
    subscribe(controlSwitch1, "switch", "switchHandler1")  
    subscribe(controlSwitch2, "switch", "switchHandler2")
    subscribe(controlSwitch3, "switch", "switchHandler3")
    subscribe(controlSwitch4, "switch", "switchHandler4")
    subscribe(controlSwitch5, "switch", "switchHandler5")
    subscribe(meter, "power", "MeterCountHandler")

    if (state.modeStartTime == null) {
        state.modeStartTime = 0
    }

}

def MeterCountHandler(evt) { 
    log.debug "MeterCountHandler was called"
    log.debug "Power value for $SwitchesToPwOff is : $evt.value"
    state.meterValue = evt.value as double
        state.thresholdValue = threshold as int

        if(delay) {
            def delayValue = delay * 60 as int
                runIn(delayValue, powerOff)
            log.debug "checking $SwitchesToPwOff's power consumption in $delay minutes"
        } 
    else { 
        log.debug "no delay for power event was selected, so running powerOff() immediately" 
        powerOff()
    }
}

def powerOff() { 
    if(state.meterValue <= state.thresholdValue) {
        log.debug "turning off $SwitchesToPwOff because power is below $state.thresholdValue watts)"
        SwitchesToPwOff.off()
    } 
    else { 
        log.debug "power value is still above $state.thresholdValue watts, so doing nothing"
    } 
}

def switchHandler1(evt) {
    log.debug "evt.device is: $evt.device"
    log.debug "evt.value is: $evt.value"

    def CurrMode = location.currentMode

    if (evt.value == "off"){

        if(dimmer){
            controlSwitch1Level.setLevel(levelTVoff)
        }

        if (Back == true){
            def now = new Date()
            def startCheck = timeToday(timeBegin, location.timeZone)
            def stopCheck = timeToday(timeEnd, location.timeZone)
            def startCheck2 = timeToday(timeBegin2, location.timeZone)
            def stopCheck2 = timeToday(timeEnd2, location.timeZone)

            log.debug "now: ${now}"
            log.debug "local time is $location.timeZone"
            log.debug "The zip code for this location: ${location.zipCode}"
            log.debug "startCheck: ${startCheck}"
            log.debug "stopCheck: ${stopCheck}"

            def between = timeOfDayIsBetween(startCheck, stopCheck, now, location.timeZone)

            def between2 = timeOfDayIsBetween(startCheck2, stopCheck2, now, location.timeZone)
            log.debug "between: ${between}"
            log.debug "between2: ${between2}"
            //log.debug "after: ${after}"

            if (CurrMode.name != ExceptionMode && between) {
                log.debug "$controlSwitch1 is $evt.value"
                log.debug "Now running $RunRoutine1C routine because time is between $startCheck and $stopCheck"
                location.helloHome?.execute(settings.RunRoutine1C)

            }
            else if (between2) {
                log.debug "$controlSwitch1 is $evt.value"
                log.debug "Now running $RunRoutine1D routine because time is between $startCheck2 and $stopCheck2"
                location.helloHome?.execute(settings.RunRoutine1D)
            }         
            else if (!ExceptionMode){
                log.debug "$controlSwitch1 is $evt.value"
                log.debug "Now running $RunRoutine1B routine"
                location.helloHome?.execute(settings.RunRoutine1B)
            } 
            else if (CurrMode.name != ExceptionMode) {
                log.debug "$controlSwitch1 is $evt.value"
                log.debug "Now running $RunRoutine1B routine"
                location.helloHome?.execute(settings.RunRoutine1B)
            }       
        } 
        else {
            log.debug "Toggle back option not selected"
        }
    }
    else if (evt.value == "on") {
        if(dimmer){
            controlSwitch1Level.setLevel(levelTVon)
        }
        location.helloHome?.execute(settings.RunRoutine1)
        log.debug "Now running $RunRoutine1 routine"
        log.debug "$controlSwitch1 is $evt.value"
    }
}

def switchHandler2(evt) {


    location.helloHome?.execute(settings.RunRoutine2)
    log.debug "Now running $RunRoutine2 routine"
    log.debug "$controlSwitch2 is $evt.value"
    if(Toggle == "yes") {
        switchesoff()
    }
}

def switchHandler3(evt) {

    location.helloHome?.execute(settings.RunRoutine3)

    log.debug "Now running $RunRoutine3 routine"
    log.debug "$controlSwitch3 is $evt.value"
    if(Toggle == "yes") {
        switchesoff()
    }
}

def switchHandler4(evt) {

    location.helloHome?.execute(settings.RunRoutine4)

    log.debug "Now running $RunRoutine4 routine"
    log.debug "$controlSwitch4 is $evt.value"
    if(Toggle == "yes") {
        switchesoff()
    }
}

def switchHandler5(evt) {

    location.helloHome?.execute(settings.RunRoutine5)

    log.debug "Now running $RunRoutine5 routine"
    log.debug "$controlSwitch5 is $evt.value"
    if(Toggle == "yes") {
        switchesoff()
    }
}

private switchesoff() { 
    if (controlSwitch2) {
        controlSwitch2.off()
    }
    if (controlSwitch3) {
        controlSwitch3.off()
    }
    if (controlSwitch4) {
        controlSwitch4.off()
    }
    if (controlSwitch5) {
        controlSwitch5.off()
    }
}

private correctTime() {
    def t0 = now()
    def modeStartTime = new Date(state.modeStartTime)
    def startTime = timeTodayAfter(modeStartTime, timeOfDay, location.timeZone)
    if (t0 >= startTime.time) {
        log.debug "The current time of day (${new Date(t0)}), startTime = ($startTime)"
        true
    } else {
        log.debug "The current time of day (${new Date(t0)}), is not in the correct time window ($startTime):  doing nothing"
        false
    }
}
