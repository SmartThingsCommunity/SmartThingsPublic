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
            paragraph: "Below you can set a maximul value above which your lights won't be set. Useful after watching a movie or when you want your night mode to be more intimate..."        
            input "halfvalue", "bool", title: "optional: set a max dim up value?", required:false, default: false, submitOnChange: true
            input "exceptionMode", "bool", title: "Apply this threshold only when on a specific Mode?", required: false, default: false, submitOnChange: true     
            if(halfvalue){
                input "SetHalfValue", "decimal", title: "set a max percentage", range: "1..100", required: true
            }
            if(exceptionMode){
                input "ExceptionMode", "mode", title: "select which mode", required: true
            }

            mode(title: "Run this app only under these modes")

            if(exceptionMode){
                paragraph: "BE SURE TO SELECT the mode that you set in the options, if any"
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


    state.value = evt.integerValue

    if(OnlyIfNotOff){
        if(state.dimmer == "off"){

            log.debug "doing nothing because switch was previously turned off"
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

    if(state.value != 0){
        ProportionLux = (maxlux / state.value) 
    }
    else{
        ProportionLux = 100
    }
    log.debug "ProportionLux value returns $ProportionLux"


    if ( ProportionLux == 1) {
        dim = 0    
    }
    else {

        dim = (ProportionLux * DimIncrVal) 
        // example 1000 / 500 = 2 so dim = 2 * 5 light will be dimmed down or up? by 10%
        // example 1000 / 58 = 17.xxx so dim = 17 * 5 so if lux is 58 then light will be set to 85%.
    }
    if(dim > 100){
        dim = 100
    } 

    if(halfvalue){
        if(exceptionMode){
            if(location.currentMode == ExceptionMode){
                if(dim >= SetHalfValue){
                    dim = SetHalfValue
                }
            }
        } 
        else { 
            if(dim >= SetHalfValue){
                dim = SetHalfValue
            }
        }

    }

    dimmer.setLevel(dim) 
    log.debug "light set to $dim %"
}
