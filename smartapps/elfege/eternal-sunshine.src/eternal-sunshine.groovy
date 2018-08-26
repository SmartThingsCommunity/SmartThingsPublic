/**
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
*  Eternal Sunshine
*
*  Author: Elfege
*/

definition(
    name: "Eternal Sunshine",
    namespace: "elfege",
    author: "elfege",
    description: "Adjust dimmers with illuminance",
    category: "Convenience",
    iconUrl: "http://elfege.com/assets/penrose.png",
    iconX2Url: "http://elfege.com/assets/penrose.png",
    //pausable: false
)

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

        section("Select the dimmers you wish to control") {
            input "dimmers", "capability.switchLevel", title: "pick a dimmer", required:true, multiple: true
        }

        section("Select Illuminance Sensor") {
            input "LSen", "capability.illuminanceMeasurement", title: "pick a sensor", required:true, multiple: false
            input "pause", "bool", title: "pause if all selected dimmers are off", default: false
        }
        section([mobileOnly:true]) {
            label title: "Assign a name", required: false
            mode title: "Set for specific mode(s)", required: false, uninstall: true
        }

    }
}

def installed() {
    log.debug "Installed with settings: ${settings}"
    initialize()
}

def updated() {
    log.debug "updated with settings: ${settings}"
    unsubscribe()
    unschedule()
    initialize()
}

def initialize() {

    subscribe(dimmers, "level", dimmersHandler)
    subscribe(LSen, "illuminance", illuminanceHandler)
    //subscribe(location, "mode", ChangedModeHandler)	
    log.debug "initialization ok"
}

def dimmersHandler(evt){
    log.debug "$evt.device is now set to $evt.value"
    evaluate()
}

def illuminanceHandler(evt){
    log.debug "$evt.device is $evt.value"
    evaluate()
}

def evaluate(){

    def illum = LSen.currentValue("illuminance")
    def FindUnit = LSen.currentState("illuminance")

    log.debug """dimmers levels are : ${dimmers.currentValue("level")} 
dimmers states are ${dimmers.currentValue("switch")}
illuminance is: $illum"""
    def states = dimmers.currentValue("switch")
    def atLeastOneIsOn = "on" in states
    log.debug "atLeastOneIsOn = $atLeastOneIsOn"


    /// ALGEBRA

    def xa = 0 		// min illuminance
    def ya = 100 	// corresponding dimmer level
    def xb = 100 	// max illuminance
    def yb = 0 		// corresponding dimmer level


    log.debug "unit: ${FindUnit.unit}"
    if(FindUnit.unit == "lux"){
        log.debug "this sensor sends values in lux"
        /// ALGEBRA corrected with lux frame of reference
        xa = 0 		// min illuminance
        ya = 100 	// corresponding dimmer level
        xb = 10000 	// max illuminance
        yb = 0 		// corresponding dimmer level

    }

    def coef = (yb-ya)/(xb-xa)	// slope
    def b = ya - coef * xa // solution to ya = coef*xa + b //

    def dimVal = coef*illum + b


    if(dimVal <= 0){
        dimVal = 1
    }

    

    if(atLeastOneIsOn){
        if(dimVal == 0){
            dimVal = 1
        }
        dimVal = dimVal.toInteger()
        setDimmers(dimVal)
    }
    else {
        log.debug "doing nothing"
    }

log.debug "dimVal = $dimVal"
}

def setDimmers(val){

    def isNotOff = true

    def i = 0
    def s = dimmers.size()

    for(s != 0; i < s; i++){
        isNotOff = "on" in dimmers[i].currentValue("switch")
        if(isNotOff){
            dimmers[i].setLevel(val)
        }
    }

}

