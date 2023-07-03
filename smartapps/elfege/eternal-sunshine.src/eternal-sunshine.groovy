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
        section([mobileOnly:true]) {
            label title: "Assign a name", required: false
            mode title: "Set for specific mode(s)", required: false

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
            paragraph "This option also works with level setting: if you manually change the level, the app stops running until you turn the light off and on (give it about 5 secs delay)"

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

    atomicState.messageSent = false

    atomicState.StopTheApp = 0

    subscribe(lightSensor, "illuminance", illuminanceHandler)
    subscribe(dimmer, "switch.on", SwitchHandler)
    subscribe(dimmer, "switch.off", SwitchHandler)
    subscribe(dimmer, "level", switchSetLevelHandler)

    /*def runTime = 5
schedule("0 0/$runTime * * * ?", doubleCheck)
log.debug "doubleCheck Scheduled to run every $runTime minutes"
*/
    atomicState.luxvalue = 0 
    atomicState.donotOverride = true
    //atomicState.LevelShouldBe = dimmer.currentValue("switchLevel")
    //log.info "Current Dimmer's Value is: $atomicState.LevelShouldBe"
}
def switchSetLevelHandler(evt) {
log.debug "The source of this event is: ${evt.source}"
    log.debug "dimmer set to $evt.integerValue   ----------------------------"
    log.debug "atomicState.StopTheApp value is currently $atomicState.StopTheApp" 

    def shouldBe = atomicState.LevelShouldBe
    shouldBe = shouldBe.toInteger()
    def DonotOverride = atomicState.donotOverride

    if(!DonotOverride && atomicState.LevelShouldBe == evt.integerValue){
        atomicState.donotOverride = false // this is temporary until I get how to read current level dimmer and set it as shouldBe value at initialization. 
        log.debug "NO OVERRIDE"
        atomicState.LevelSetByApp = true 
    }
    else {
        log.debug "MANUAL OVERRIDE. RESET VALUE IS : $shouldBe"
        // need to send reset value as a push message. 
        atomicState.LevelSetByApp = true 
    }
}
def SwitchHandler(evt){ 
    log.debug "$dimmer currentSwitch evt value is $evt.value "
    log.debug "$dimmer status value is $dimmer.currentSwitch "

    log.debug "so far atomicState.StopTheApp value was $atomicState.StopTheApp" 

    if(evt.value == "on"){
        atomicState.StopTheApp = 0 
        log.debug "Now atomicState.StopTheApp value set to $atomicState.StopTheApp" 
        atomicState.dimmerSW = 1
        if(atomicState.LevelSetByApp == false){ 
            atomicState.LevelSetByApp = true
            log.debug "LEVEL OVERRIDE CANCELED"
        }

    }
    else if(evt.value == "off"){
        if(atomicState.dim != 0) { 
            atomicState.StopTheApp = 1
            atomicState.dimmerSW = 0
            log.debug "Now atomicState.StopTheApp value set to $atomicState.StopTheApp" 
            log.debug "SWITCH TURNED OFF BY USER"
            log.debug "Double check in 5 seconds"
            runIn(5, doubleCheck) // sometimes atomicState.dim value didn't refresh on time so double check whether and make sure it was not a manual shut off

        }
        else if(atomicState.dim == 0){ 
            log.debug "SWITCH TURNED OFF WITHOUT USER INTERVENTITON"

        }
    }
}
def illuminanceHandler(evt){
    log.debug "illuminance is $evt.integerValue"

    log.debug "atomicState.dimmerSW value is $atomicState.dimmerSW" 

    atomicState.luxvalue = evt.integerValue

    if(OnlyIfNotOff){
        if(atomicState.StopTheApp == 0 && atomicState.LevelSetByApp == true ) {
            DIM()
            log.debug """Dimming. 
atomicState.StopTheApp = $atomicState.StopTheApp
atomicState.LevelSetByApp = $atomicState.LevelSetByApp"""

        }
        else { 

            log.debug "doing nothing because $dimmer was manually et or turned off"
        }
    }
    else {
        DIM()
        log.debug "DIM because no OnlyIfNotOff"
    }   

}
def doubleCheck() { 

    if(atomicState.dim == 0) {
        atomicState.StopTheApp = 0
        log.debug "atomicState.StopTheApp RESET because atomicState.dim = $atomicState.dim (verif: should be 0)" 

        Evaluate()
    } 
    else if( atomicState.dimmerSW != 1) { 
        log.debug "Manual Intervention CONFIRMED"
    }
    log.debug "DOUBLE CHECK OK"
    log.debug "atomicState.luxvalue is $atomicState.luxvalue"

}
def DIM(){
    log.debug "atomicState.luxvalue is $atomicState.luxvalue"

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

        if(atomicState.luxvalue != 0){
            ProportionLux = maxlux / atomicState.luxvalue
            log.debug "calculating proportionLux multiplier as: $ProportionLux"
        }
        else{
            ProportionLux = 100
            log.debug "ProportionLux set to 100 because lux = $atomicState.luxvalue"
        }

        log.debug "ProportionLux value returns $ProportionLux"

        if ( ProportionLux == 1) {
            // this is the case when lux is maxed 

            atomicState.dim = 0
            log.debug "ProportionLux is 1 so dim set to 0"
        }
        else {
            atomicState.dim = (ProportionLux * DimIncrVal)
            log.debug "calculating atomicState.dim"
            // example 1000 / 500 = 2 so dim = 2 * 5 light will be dimmed down or up? by 10%
            // example 1000 / 58 = 17.xxx so dim = 17 * 5 so if lux is 58 then light will be set to 85%.
        }
        if(atomicState.dim > 100){
            atomicState.dim = 100
        } 
    }
    else { 
        // unit is % so just match absolute values
        maxlux = 100
        log.debug "percentage is the measurment unit"    
        log.debug "maxlux set as ${maxlux}$latest.unit"
        atomicState.dim = 100 - atomicState.luxvalue       
    }

    // max values
    if(Partvalue){
        log.debug "PartValue eval"
        // max values by mode
        if(exceptionMode){
            log.debug "exceptionMode eval"
            if(CurrMode == ExceptionMode){
                log.debug "currentMode eval"
                if(atomicState.dim >= SetPartvalue){
                    atomicState.dim = SetPartvalue
                    log.debug "atomicState.dim value is: $atomicState.dim (SetPartvalue)"
                }
            } 
            else if(CurrMode == ExceptionMode2){
                if(atomicState.dim >= SetPartvalue2){
                    atomicState.dim = SetPartvalue2
                    log.debug "atomicState.dim value is: $atomicState.dim (SetPartvalue2)"
                }
            }
        }
        else {
            log.debug "Just PartValue eval (not depending on location mode)"
            if(Partvalue){
                if(atomicState.dim >= SetPartvalue){
                    atomicState.dim = SetPartvalue
                    log.debug "atomicState.dim value is: $atomicState.dim (SetPartvalue alone)"
                }
            }
        }
    }
    if(atomicState.luxvalue < maxluxOFF){     
        atomicState.StopTheApp = 0
        SetDimmers()
    }
    else {
        atomicState.dim = 0          
        log.debug "light set to 0 % because illuminance is high"
        atomicState.StopTheApp = 0
        SetDimmers()
    }
}
def SetDimmers(){

    dimmer.setLevel(atomicState.dim) 
    atomicState.LevelShouldBe = atomicState.dim 
    atomicState.LevelSetByApp = true 
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


