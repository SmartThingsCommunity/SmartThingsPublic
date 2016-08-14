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
    description: "Adjusts dimmer lever with light sensor (requires modified Lux Sensor Device Handler, contact me for details elfege@elfege.com)",
    category: "Convenience",
    iconUrl: "http://elfege.com/penrose.jpg",
    iconX2Url: "http://elfege.com/penrose.jpg",
    iconX3Url: "http://elfege.com/penrose.jpg")


preferences { 
    page(name: "settings", title: "Select your preferences", install: true, uninstall: true) 
}


def settings() {

    dynamicPage(name: "settings", title: "Select your preferences", install:true, uninstall: true) {

        section("select at least one dimmer") {
            input "dimmer", "capability.switchLevel", title: "pick a dimmer", required:true, multiple: true
        }
        section("select a ligt sensor") {
            input "lightSensor", "capability.illuminanceMeasurement", title: "pick a sensor", required:true, multiple: false
        }
        section("set a scale of dimming increment values ") {
            input "DimIncrVal", "decimal", title: "pick an increment value", range: "5..20", required:true, multiple: false
        }
        section("Select Optional Rules") {    
            input "OnlyIfNotOff", "bool", title: "Run this app only if ${dimmer} isn't turned off", default: false
            paragraph "Below you can set a maximum value above which your lights won't be set. Useful after watching a movie or when you want your night mode to be more intimate..."        
            input "Partvalue", "bool", title: "optional: set a maximum light level?", required:false, default: false, submitOnChange: true
            if(Partvalue){
                input "SetPartvalue", "decimal", title: "Set a maximul value", range: "1..100", required: true
                input "exceptionMode", "bool", title: "Apply this threshold only when on specific Modes?", required: false, default: false, submitOnChange: true     

                if(exceptionMode){
                    input "ExceptionMode", "mode", title: "select which mode", required: true, multiple: false, submitOnChange: true
                    if(ExceptionMode){
                        input "ExceptionMode2", "mode", title: "Optional: select another mode", required: false, multiple: false, submitOnChange: true
                        if(ExceptionMode2){
                            input "SetPartvalue2", "decimal", title: "What maximum value in this mode?", range: "1..100", required: true
                        }
                    }
                     paragraph "MAKE SURE TO SELECT below the modes that you just selected in the options above"
                }
            }
            mode(title: "Run this app only under these modes")
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
    initialize()
}
def initialize() {
    subscribe(lightSensor, "illuminance", illuminanceHandler)
    subscribe(dimmer, "switch", SwitchHandler)
}

def SwitchHandler(evt){ 
    log.debug "dimmer.currentSwitch is $dimmer.currentSwitch"
    if(evt.value == "on"){
        state.dimmer = "on"
        log.debug "Now Eternal Sunshine resumes its automation"
    } 
    else { 
        state.dimmer = "off"
    }
}

def illuminanceHandler(evt){
    log.debug "illuminance is $evt.integerValue"
    state.luxvalue = evt.integerValue
    if(OnlyIfNotOff){
        if(state.dimmer == "off"){

            log.debug "doing nothing because switch is off"
        }
        else {
            DIM()
        }
    }
    else {
        DIM()
    }
}

private DIM(){
    int dim = 0 
    def maxlux = 1000
    def ProportionLux = 0
    def CurrMode = location.currentMode

    log.debug "CurrMode is $CurrMode"
    log.debug "ExceptionMode is $ExceptionMode"

    if(state.luxvalue != 0){
        ProportionLux = (maxlux / state.luxvalue) 
    }
    else{
        ProportionLux = 100
    }

    log.debug "ProportionLux value returns $ProportionLux"

    if ( ProportionLux == 1) {
        // this is the case when lux is max at 1000
        if(Partvalue){
            dim = 1 // don't set to 0 to avoid creating an event that will turn off the entire app
        }
        else {
            dim = 0
            log.debug "ProportionLux is 1 so dim set to 0"
        }
    }
    else {
        dim = (ProportionLux * DimIncrVal) 
        // example 1000 / 500 = 2 so dim = 2 * 5 light will be dimmed down or up? by 10%
        // example 1000 / 58 = 17.xxx so dim = 17 * 5 so if lux is 58 then light will be set to 85%.
    }
    if(dim > 100){
        dim = 100
    } 

    if(Partvalue){
        log.debug "PartValue eval"
        if(exceptionMode){
            log.debug "exceptionMode eval"
            if(CurrMode == ExceptionMode){
                log.debug "currentMode eval"
                if(dim >= SetPartvalue){
                    dim = SetPartvalue
                }
            } 
            else if(CurrMode == ExceptionMode2){
                if(dim >= SetPartvalue2){
                    dim = SetPartvalue2
                }
            }
        }
        else {
            log.debug "Just PartValue eval"
            if(dim >= SetPartvalue){
                dim = SetPartvalue
            }
        }

    }




    dimmer.setLevel(dim) 
    log.debug "light set to $dim %"
}
