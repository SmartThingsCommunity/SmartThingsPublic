/**
*  Eternal Sunshine
*
*  Copyright 2016 Elfege
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
    name: "Eternal Sunshine",
    namespace: "elfege",
    author: "Elfege",
    description: "Adjusts dimmer lever with light sensor. Optionally, this app can stop running whenever lights are manually turned off and resumes when they're turned back on",
    category: "Convenience",
    iconUrl: "http://elfege.com/penrose.jpg",
    iconX2Url: "http://elfege.com/penrose.jpg",
    iconX3Url: "http://elfege.com/penrose.jpg")


preferences { 
    page name:"pageSetup"
    page name:"settings"
    page name:"Options"
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

        section("Setup Menu") {
            href "settings", title: "Main Settings", description: ""
            href "Options", title: "More Options", description: ""

        }
    }
}

def settings() {

    def pageProperties = [
        name:       "settings",
        title:      "Settings",
        nextPage:   "pageSetup"
    ]

    return dynamicPage(pageProperties) {

        section("select at least one dimmer") {
            input "dimmer", "capability.switchLevel", title: "pick a dimmer", required:true, multiple: true
        }
        section("select a ligt sensor") {
            input "lightSensor", "capability.illuminanceMeasurement", title: "pick a sensor", required:true, multiple: false
        }

        section("set a max illuminance value") {
            input "maxluxOFF", "number", title: "Turn off the light when illuminance is above this value", range: "1..100", required: false, default: "100", description: "value in %"
        }
    }
}

def Options() {
    def pageProperties = [
        name:       "Options",
        title:      "Options",
        nextPage:   "pageSetup"
    ]

    return dynamicPage(pageProperties){
        section("Select Optional Rules") {    
            input "OnlyIfNotOff", "bool", title: "Run this app only if ${dimmer} isn't turned off", default: false, uninstall: true, install: true

            paragraph "Below you can set a maximum value above which your lights won't be set. Useful after watching a movie or when you want your night mode to be more intimate..."        
            input "Partvalue", "bool", title: "optional: set a maximum light level?", required:false, default: false, submitOnChange: true, uninstall: true, install: true
            if(Partvalue){
                input "SetPartvalue", "decimal", title: "Set a maximul value", range: "1..100", required: true, uninstall: true, install: true
                input "exceptionMode", "bool", title: "Apply this threshold only when on specific Modes?", required: false, default: false, submitOnChange: true, uninstall: true, install: true     

                if(exceptionMode){
                    input "ExceptionMode", "mode", title: "select which mode", required: true, multiple: false, submitOnChange: true, uninstall: true, install: true
                    if(ExceptionMode){
                        input "ExceptionMode2", "mode", title: "Optional: select another mode", required: false, multiple: false, submitOnChange: true, uninstall: true, install: true
                        if(ExceptionMode2){
                            input "SetPartvalue2", "decimal", title: "What maximum value in this mode?", range: "1..100", required: true, uninstall: true, install: true
                        }
                    }
                }
            }
        }
        section([mobileOnly:true]) {
            label title: "Assign a name", required: false
            mode title: "Set for specific mode(s)", required: false

        }
    }
}

def installed() {
    log.debug "Installed with settings: ${settings}"
    initialize()
}
def updated() {

    log.debug "Updated with settings: ${settings}"
    unsubscribe()
    unschedule()
    initialize()

}
def initialize() {

    state.messageSent = false

    state.StopTheApp = 0

    subscribe(lightSensor, "illuminance", illuminanceHandler)
    subscribe(dimmer, "switch.on", SwitchHandler)
    subscribe(dimmer, "switch.off", SwitchHandler)
    subscribe(dimmer, "level", switchSetLevelHandler)

    /*def runTime = 5
schedule("0 0/$runTime * * * ?", doubleCheck)
log.debug "doubleCheck Scheduled to run every $runTime minutes"
*/
    state.luxvalue = 0 
}
def switchSetLevelHandler(evt) {
    log.debug "dimmer set to $evt.integerValue   ----------------------------"
    log.debug "state.StopTheApp value is currently $state.StopTheApp" 
}
def SwitchHandler(evt){ 
    log.debug "$dimmer currentSwitch evt value is $evt.value "
    log.debug "$dimmer status value is $dimmer.currentSwitch "

    log.debug "so far state.StopTheApp value was $state.StopTheApp" 

    if(evt.value == "on"){
        state.StopTheApp = 0 
        state.dimmerSW = 1
        log.debug "Now state.StopTheApp value set to $state.StopTheApp" 
    }
    else if(evt.value == "off"){
        if(state.dim != 0) { 
            state.StopTheApp = 1
            state.dimmerSW = 0
            log.debug "Now state.StopTheApp value set to $state.StopTheApp" 
            log.debug "SWITCH TURNED OFF BY USER"
            log.debug "Double check in 5 seconds"
            runIn(5, doubleCheck) // sometimes state.dim value didn't refresh on time so double check whether and make sure it was not a manual shut off

        }
        else if(state.dim == 0){ 
            log.debug "SWITCH TURNED OFF WITHOUT USER INTERVENTITON"

        }
    }
}
def illuminanceHandler(evt){
    log.debug "illuminance is $evt.integerValue"

    log.debug "state.dimmerSW value is $state.dimmerSW" 

    state.luxvalue = evt.integerValue
    
     if(OnlyIfNotOff){
        if(state.StopTheApp == 0) {
            DIM()
            log.debug "DIM because state.StopTheApp = $state.StopTheApp"

        }
        else { 

            log.debug "doing nothing because $dimmer was manually turned off "
        }
    }
    else {
        DIM()
        log.debug "DIM because no OnlyIfNotOff"
    }

}
def doubleCheck() { 

    if(state.dim == 0) {
        state.StopTheApp = 0
        log.debug "state.StopTheApp RESET because state.dim = $state.dim (verif: should be 0)" 

        Evaluate()
    } 
    else if( state.dimmerSW != 1) { 
        log.debug "Manual Intervention CONFIRMED"
    }
    log.debug "DOUBLE CHECK OK"
    log.debug "state.luxvalue is $state.luxvalue"

}
def DIM(){
    log.debug "state.luxvalue is $state.luxvalue"

    def latest = lightSensor.currentState("illuminance")

    log.debug "unit: ${latest.unit}"

    def maxlux = 100

    def ProportionLux = 0 as int
        def CurrMode = location.currentMode

        log.debug "CurrMode is $CurrMode"
    log.debug "ExceptionMode is $ExceptionMode"


    if(latest.unit == "lux"){
        def DimIncrVal = 5
        def maxluxOFF = maxluxOFF*10
        maxlux = 1000
        log.debug "lux is the measurment unit"
        log.debug "maxlux set as ${maxlux}$latest.unit"

        if(state.luxvalue != 0){
            ProportionLux = maxlux / state.luxvalue
            log.debug "calculating proportionLux multiplier as: $ProportionLux"
        }
        else{
            ProportionLux = 100
            log.debug "ProportionLux set to 100 because lux = $state.luxvalue"
        }

        log.debug "ProportionLux value returns $ProportionLux"

        if ( ProportionLux == 1) {
            // this is the case when lux is maxed 

            state.dim = 0
            log.debug "ProportionLux is 1 so dim set to 0"
        }
        else {
            state.dim = (ProportionLux * DimIncrVal)
            log.debug "calculating state.dim"
            // example 1000 / 500 = 2 so dim = 2 * 5 light will be dimmed down or up? by 10%
            // example 1000 / 58 = 17.xxx so dim = 17 * 5 so if lux is 58 then light will be set to 85%.
        }
        if(state.dim > 100){
            state.dim = 100
        } 
    }
    else { 
        // unit is % so just match absolute values
        maxlux = 100
        log.debug "percentage is the measurment unit"    
        log.debug "maxlux set as ${maxlux}$latest.unit"
        state.dim = 100 - state.luxvalue       
    }

    // max values
    if(Partvalue){
        log.debug "PartValue eval"
        // max values by mode
        if(exceptionMode){
            log.debug "exceptionMode eval"
            if(CurrMode == ExceptionMode){
                log.debug "currentMode eval"
                if(state.dim >= SetPartvalue){
                    state.dim = SetPartvalue
                    log.debug "state.dim value is: $state.dim (SetPartvalue)"
                }
            } 
            else if(CurrMode == ExceptionMode2){
                if(state.dim >= SetPartvalue2){
                    state.dim = SetPartvalue2
                    log.debug "state.dim value is: $state.dim (SetPartvalue2)"
                }
            }
        }
        else {
            log.debug "Just PartValue eval (not depending on location mode)"
            if(Partvalue){
                if(state.dim >= SetPartvalue){
                    state.dim = SetPartvalue
                    log.debug "state.dim value is: $state.dim (SetPartvalue alone)"
                }
            }
        }
    }
    if(state.luxvalue < maxluxOFF){     
        state.StopTheApp = 0
        SetDimmers()
    }
    else {
        state.dim = 0          
        log.debug "light set to 0 % because illuminance is high"
        state.StopTheApp = 0
        SetDimmers()
    }
}
def SetDimmers(){
    dimmer.setLevel(state.dim) 
}
private send(msg) {
    if (location.contactBookEnabled) {
        sendNotificationToContacts(msg, recipients)
    }
    else {
        if (sendPushMessage != "No") {
            log.debug("sending push message")
            sendPush(msg)
        }

        if (phone) {
            log.debug("sending text message")
            sendSms(phone, msg)
        }
    }

    log.debug msg
}


