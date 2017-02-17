/**
*  Copyright 2017 ELFEGE
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
*  Virtual Thermostat
*
*  Author: SmartThings
*/
definition(
    name: "Virtual Thermostat ++",
    namespace: "ELFEGE",
    author: "ELFEGE",
    description: "Control a space heater or window air conditioner in conjunction with any temperature sensor, like a SmartSense Multi.",
    category: "Green Living",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Meta/temp_thermo-switch.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Meta/temp_thermo-switch@2x.png"
)

preferences {
    page name: "pageSetup"
    page name: "settings"
    page name: "TemperaturesByMode"
}

def pageSetup() {

    def pageProperties = [
        name:       "pageSetup",
        title:      "Status",
        nextPage:   null,
        install:    true,
        uninstall:  true
    ]

    return dynamicPage(pageProperties) {
        section("About this App:"){
            paragraph "Manage a virtual Thermostat"
        }
        section("Setup Menu") {
            href "settings", title: "Choose settings", description: ""
            href "TemperaturesByMode", title: "Set different temperatures depending on location mode", description: ""
        }

        section([title:"Options", mobileOnly:true]) {
            label title:"Assign a name", required:false
        }
    }
}
def settings(){

    def pageProperties = [
        name:       "settings",
        title:      "Settings",
        nextPage:   "pageSetup"
    ]

    dynamicPage(pageProperties) {

        section("Choose a temperature sensor... "){
            input "sensor", "capability.temperatureMeasurement", title: "Sensor"
        }
        section("Select the heater or air conditioner outlet(s)... "){
            input "outlets", "capability.switch", title: "Outlets", multiple: true
        }
        section("Select 'heat' for a heater and 'cool' for an air conditioner..."){
            input "mode", "enum", title: "Heating or cooling?", options: ["heat","cool"]
        }
        section("Run only when there's been movement from (optional, leave blank to not require motion)..."){
            input "motion", "capability.motionSensor", title: "Motion", required: false
        }
        section("Within this number of minutes..."){
            input "minutes", "number", title: "Minutes", required: false
        }
        section(""){
            input "Modes", "mode", title: "Run this app only in these modes", required: true, multiple: true, submitOnChange: true
        }
        section("Allow Manual Override"){
            input "override", "bool", title: "Override when turned off by user", default: false, required: false
        }
    }
}
def TemperaturesByMode(){

    def pageProperties = [
        name:       "TemperaturesByMode",
        title:      "Set Temperatures by Mode",
        nextPage:   "pageSetup"
    ]

    dynamicPage(pageProperties) {

        log.debug "Selected Modes are $Modes"


        section("Set the desired temperatures..."){
            input "desireTempMode0", "decimal", title: "Desired Temp in $Mode0 mode", required: true
            if(ModesSize > 1){
                input "desireTempMode1", "decimal", title: "Desired Temp in $Mode1 mode"
            }
            if(ModesSize > 2){
                input "desireTempMode2", "decimal", title: "Desired Temp in $Mode2 mode"
            }
            if(ModesSize > 3){
                input "desireTempMode3", "decimal", title: "Desired Temp in $Mode3 mode"
            }
            if(ModesSize > 4){
                input "desireTempMode4", "decimal", title: "Desired Temp in $Mode4 mode"
            }
            if(ModesSize > 5){
                input "desireTempMode5", "decimal", title: "Desired Temp in $Mode5 mode"
            }
        }
    }
}

def installed(){
    log.debug "installed with settings: $settings"
    init()
}
def updated(){
    log.debug "updated with settings: $settings"
    unschedule()
    unsubscribe()

    init()

}
def init(){
    subscribe(sensor, "temperature", temperatureHandler)
    subscribe(outlets, "switch", switchHandler)
    subscribe(location, "mode", ChangedModeHandler)	
    if (motion) {
        subscribe(motion, "motion", motionHandler)
        log.debug "subscribed to motion events"
    }
    state.HandlerIsScheduled = 0
    state.override = 0
    atomicState.OffbyApp = 1

    def scheduledTime = 1
    state.HandlerIsScheduled = 1
    schedule("0 0/$scheduledTime * * * ?", temperatureHandler)
    log.debug "temperatureHandler scheduled to run every $scheduledTime minutes"


    def SelectedModes = Modes.findAll { AllModes ->
        AllModes in Modes ? true : false
    }
    log.debug "${SelectedModes.size()} out of ${Modes.size()} Modes are selected"

    def ModesSize = SelectedModes.size()

    log.debug "Mode Size is $ModesSize"

    def Mode0 = Modes[0]
    def Mode1 = Modes[1]
    def Mode2 = Modes[2]
    def Mode3 = Modes[3]
    def Mode4 = Modes[4]
    def Mode5 = Modes[5]

    log.trace "Mode0: $Mode0, Mode1: $Mode1, Mode2: $Mode2, Mode3: $Mode3, Mode4: $Mode4, Mode5: $Mode5, "

    temperatureHandler()

}

def ChangedModeHandler(evt){
    log.debug "Current Mode is $evt.value" 
    def CurrMode = location.currentMode

    if(CurrMode in Modes){
        def scheduledTime = 1
        state.HandlerIsScheduled = 1
        schedule("0 0/$scheduledTime * * * ?", temperatureHandler)
        log.debug "temperatureHandler scheduled to run every $scheduledTime minutes"
        temperatureHandler()
    }
    else {
        unschedule(temperatureHandler)
    }

    log.debug "Selected Modes are $Modes"

    def Mode0 = Modes[0]
    def Mode1 = Modes[1]
    def Mode2 = Modes[2]
    def Mode3 = Modes[3]
    def Mode4 = Modes[4]
    def Mode5 = Modes[5]

    log.trace "Mode0: $Mode0, Mode1: $Mode1, Mode2: $Mode2, Mode3: $Mode3, Mode4: $Mode4, Mode5: $Mode5, "
    desiredTemp()
}
def desiredTemp(){

    def CurrMode = location.currentMode

    def desiredTemp = 65 

    if(CurrMode in Modes[0]){
        desiredTemp = desireTempMode0
        log.debug "Desired temp is now : $desiredTemp ----a-----"
    } 
    else if(CurrMode in Modes[1]){
        desiredTemp = desireTempMode1
        log.debug "Desired temp is now : $desiredTemp ----b-----"
    } 
    else if(CurrMode in  Modes[2]){
        desiredTemp = desireTempMode2
        log.debug "Desired temp is now : $desiredTemp -----c----"
    } 
    else if(CurrMode in  Modes[3]){
        desiredTemp = desireTempMode3
        log.debug "Desired temp is now : $desiredTemp ----d-----"
    } 
    else if(CurrMode in  Modes[4]){
        desiredTemp = desireTempMode4
        log.debug "Desired temp is now : $desiredTemp ---e------"
    } 
    else if(CurrMode in  Modes[5]){
        desiredTemp = desireTempMode5
        log.debug "Desired temp is now : $desiredTemp ---f------"
    }
    else { 
        log.debug "Home is not in any selected modes"
    }
    return desiredTemp
}
def switchHandler(evt){

    log.debug "$evt.device is $evt.value"
    log.debug "state.override = $state.override"
    log.debug "atomicState.OffbyApp = $atomicState.OffbyApp"

    def CurrMode = location.currentMode
    def ModeOk = CurrMode in Modes
    if(override){

        if(evt.value == "off" && state.override == 0){
            atomicState.OffbyApp = 1
            log.debug "atomicState.OffbyApp value back to 1"
        }

        if(evt.value == "on" && !ModeOk){
            // this is an on override. Keep on but turn on in an hour"
            state.override = 1
            outlets?.off([delay:360000])
            log.debug "OVERRIDE outside of $Modes modes... turning off $outlets in 1 hour"
        }
        else if(evt.value == "off" && !ModeOk){
            state.override = 0
            log.debug "OVERRIDE outside of $Modes modes CANCELED"
        }
        else if(evt.value == "on" && state.override == 1){
            log.debug "OVERRIDE MODE CANCELED, resuming normal operation"
            state.override = 0
        }
        else if (evt.value == "off" && state.override == 0 && atomicState.OffbyApp == 0){
            log.debug "OVERRIDE MODE"
            state.override = 1
        }
        else if(evt.value == "on" && atomicState.OffbyApp == 1 && state.override == 0){
            atomicState.OffbyApp = 0
        }
    } 
    else { 
        log.debug "NO OVERRIDE OPTION"
        state.override = 0
        atomicState.OffbyApp = 1
    }

    def currSwitches = outlets.currentSwitch

    def onSwitches = currSwitches.findAll { switchVal ->
        switchVal == "on" ? true : false
    }
    log.debug "${onSwitches.size()} out of ${outlets.size()} switches are on"
    atomicState.totalOutlets = outlets.size()
    atomicState.switchVal = onSwitches.size()

    log.debug "atomicState.switchVal = $atomicState.switchVal"
}
def temperatureHandler(evt){
    def CurrMode = location.currentMode

    state.currentTemp = sensor.currentTemperature as double
        //log.debug "$sensor event is $evt.value" // this handler run according to a schedule so evt.value would return an error
        log.debug "Current Temperature is ${state.currentTemp}°F"

    Switches()
}
def motionHandler(evt){

    log.debug "Motion is $evt.value"
    if(evt.value == "active"){
        state.motion = 1
        Switches()
    }
    else if (evt.value == "inactive") {

        def isActive = hasBeenRecentMotion()
        log.debug "Has been active within the last $minutes minute($isActive)"

        if (isActive) {

            state.motion = 1
            // switches will be triggered by temperature handler 

        }
        else {
            state.motion = 0
        }
    }
}
private Switches(){
	
    def CurrMode = location.currentMode
    
    def desiredTemp = desiredTemp()
    
    def switchVal = atomicState.switchVal
    def totalOutlets = atomicState.totalOutlets

    if(!motion){
        state.motion = 1
    }
    log.debug "Switches() loop"

    if(CurrMode in Modes){
        if(state.motion == 1){
            if (mode == "cool") {
                // air conditioner
                log.debug "evaluating cool. Desired temperature is $desiredTemp"    
                if (state.currentTemp > desiredTemp) {
                    if(state.override == 0){
                        // if out of override the app needs to know that not all outlets were turned back on, so it turns back on all the others
                        if(switchVal != totalOutlets){

                            log.debug "atomicState.OffbyApp = $atomicState.OffbyApp"
                            outlets?.on()
                            log.debug "$outlets turned ON"
                            state.outlet = "on"
                        }
                        else {
                            log.debug "outlets already on, so doing nothing" 
                        }
                    }
                    else {log.debug "App in override mode, doing nothing"}
                }
                else {
                    if(state.override == 0){
                        atomicState.OffbyApp = 1
                        log.debug "atomicState.OffbyApp = $atomicState.OffbyApp"
                        outlets?.off()                   
                        log.debug "$outlets turned OFF"
                        state.outlet = "off"
                    }
                    else {
                        log.debug "App in override mode, doing nothing"
                    }
                }
            }
            else {
                // heater
                log.debug "evaluating heat. Desired temperature is $desiredTemp"
                if (state.currentTemp < desiredTemp) {
                    if(state.override == 0){
                        log.debug "switchVal = $switchVal"
                        // if out of override the app needs to know that not all outlets were turned back on, so it turns back on all the others
                       if(switchVal != totalOutlets){

                            log.debug "atomicState.OffbyApp = $atomicState.OffbyApp"
                            outlets?.on()
                            log.debug "$outlets turned ON"
                            state.outlet = "on"
                        }
                        else {
                            log.debug "outlets already on, so doing nothing"
                        }
                    }
                    else {log.debug "App in override mode, doing nothing"}
                }
                else {
                    if(state.override == 0){
                        if(switchVal != 0){
                            atomicState.OffbyApp = 1
                            log.debug "atomicState.OffbyApp = $atomicState.OffbyApp"

                            outlets?.off()
                            log.debug "$outlets turned OFF"
                            state.outlet = "off"
                        }
                        else {
                            log.debug "$outlets ALREADY OFF"
                        }
                    }
                    else {
                        log.debug "App in override mode, doing nothing"
                    }
                }
            }
        }
        else {
            if(motion){
                log.debug "no motion within time frame, turning off $outlets"
                if(state.override == 0){       
                    if(state.override == 0){
                        if(atomicState.switchVal != 0){
                            atomicState.OffbyApp = 1
                            outlets?.off()
                        }
                        else {
                            log.debug "$outlets ALREADY OFF"
                        }
                    }
                } 
                else {
                    log.debug "App in override mode, doing nothing"
                }
            }
            else {
                log.debug "Motion detection not selected by user, doing nothing"
            }
        }   
    }
    else { 
        log.debug "Not in $Modes mode, making sure all outlets are off"
        if(state.override == 0){
            if(atomicState.switchVal != 0){             
                atomicState.OffbyApp = 1
                outlets?.off()
            }
            else { 
                if(state.HandlerIsScheduled != 0){
                    log.debug "unschedule()"
                    unschedule()
                    state.HandlerIsScheduled = 0
                }
                log.debug "Waiting for events"
            }
        }
        else {
            log.debug "App in override mode, doing nothing"
        }

    }
}
private hasBeenRecentMotion(){
    def isActive = false
    if (motion && minutes) {
        def deltaMinutes = minutes as Long
        if (deltaMinutes) {
            def motionEvents = motion.eventsSince(new Date(now() - (60000 * deltaMinutes)))
            log.trace "Found ${motionEvents?.size() ?: 0} events in the last $deltaMinutes minutes"
            if (motionEvents.find { it.value == "active" }) {
                isActive = true
            }
        }
    }
    else {
        isActive = true
    }
    isActive
}


