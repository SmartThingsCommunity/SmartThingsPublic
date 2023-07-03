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
    iconUrl: "http://static1.squarespace.com/static/5751f711d51cd45f35ec6b77/t/59c561cb268b9638e8ba6c23/1512332763339/?format=1500w",
    iconX2Url: "http://static1.squarespace.com/static/5751f711d51cd45f35ec6b77/t/59c561cb268b9638e8ba6c23/1512332763339/?format=1500w",
    iconX3Url: "http://static1.squarespace.com/static/5751f711d51cd45f35ec6b77/t/59c561cb268b9638e8ba6c23/1512332763339/?format=1500w",
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
            input "LSen", "capability.illuminanceMeasurement", title: "pick a sensor", required:true, multiple: false, submitOnChange: true

            if(LSen){

                input "maxValue", "number", title: "Please, select max lux value for this sensor", default: false

            }
            input "pause", "bool", title: "pause if all selected dimmers are off", default: false
        }


        section("Differentiate Values With Location Mode") {
            input "modes", "mode", title:"select modes", required: false, multiple: true, submitOnChange: true

            if(modes){
                def i = 0
                state.dimValMode = []
                def dimValMode = []
                for(modes.size() != 0; i < modes.size(); i++){
                    input "dimValMode${i}", "number", required:true, title: "select a maximum value for ${modes[i]}"

                }
            }
        }
        section("motion"){
            input "motionSensors", "capability.motionSensor", title: "Turn this lights on with motion", despcription: "pick a motion sensor", required:false, multiple:true


        }
        section("override"){
            input "OVRD", "bool", default: false, title: "Allow manual input to override this application"
            paragraph "NB: this will apply until you turn the lights off and on."
            if(motionsensor){
                paragraph "You have picked motion sensor and overrides"
            }
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

    subscribe(motionSensors, "motion", motionHandler)
    subscribe(dimmers, "level", dimmersHandler)
    subscribe(dimmers, "switch", switchHandler)
    subscribe(LSen, "illuminance", illuminanceHandler)
    //subscribe(location, "mode", ChangedModeHandler)	
    atomicState.override = false
    log.debug "initialization ok"
    evaluate()
}

def switchHandler(evt){
    log.debug "$evt.device is now set to $evt.value"

    if(OVRD && atomicState.override){
        log.debug "END OF OVERRIDE"
        atomicState.override = false
    }

    if(evt.value == "off"){  // prevent currently running loop from turning it back on
        def device = evt.device
        device.setLevel(0)
    }


}

def dimmersHandler(evt){
    log.debug "$evt.device is now set to $evt.value"

    def autoDim = atomicState.dimVal
    def val = evt.value.toInteger()
    log.debug "autoDim = $autoDim // $val"


    if(OVRD && autoDim != val && evt.value != "off" && evt.value != "on"){
        log.debug "OVERRIDE TRIGGERED"
        atomicState.override = true
    }
    else {
        log.debug "NO OVERRIDE.."
        atomicState.override = false
    }
}

def illuminanceHandler(evt){
    log.debug "$evt.device is $evt.value"
    evaluate()
}

def motionHandler(){
    log.debug "motion $evt.value at $evt.device "


}

def evaluate(){

    /**********************************************************************/
    runIn(10, evaluate) // TESTS ONLY COMMENT OUT AFTER !!!! 
    /**********************************************************************/

    def illum = LSen.currentValue("illuminance")
    def FindUnit = LSen.currentState("illuminance")

    log.debug """dimmers levels are : ${dimmers.currentValue("level")} 
dimmers states are ${dimmers.currentValue("switch")}
illuminance is: $illum"""
    def states = dimmers.currentValue("switch")
    def atLeastOneIsOn = "on" in states
    log.debug "atLeastOneIsOn = $atLeastOneIsOn"


    /// ALGEBRA

    //  log.debug "unit: ${FindUnit.unit}"
    //  if(FindUnit.unit == "lux"){
    log.debug "$LSen returns values in lux"
    /// ALGEBRA corrected with lux frame of reference
    def xa = 0 		// min illuminance
    def ya = 100 	// corresponding dimmer level
    def xb = maxValue 	// max illuminance
    def yb = 0 		// corresponding dimmer level

    def coef = (yb-ya)/(xb-xa)	// slope
    def b = ya - coef * xa // solution to ya = coef*xa + b //

    def dimVal = coef*illum + b
    dimVal = dimVal.toInteger()

    log.debug """
    ALGEBRA RESULTS: 
    current illuminance : $illum
    xa = $xa,
    ya = $ya, 
    xb = $xb, 
    yb = $yb, 
    slope = $coef, 
    b = $b, 
    result = $dimVal
    """

    if(dimVal <= 0){
        dimVal = 1
    }

    if(atLeastOneIsOn){
        if(dimVal == 0){
            dimVal = 1
        }
        dimVal = dimVal
        log.debug "setting dimmers to $dimVal"
        setDimmers(dimVal)
    }
    else {
        log.debug "doing nothing"
    }



}

def setDimmers(val){

    def isNotOff = true

    def i = 0
    def s = dimmers.size()

    if(modes)
    {
        if(location.currentMode in modes){

            while(location.currentMode != modes[i]){
                i++
                    log.debug "; $i ;"
            }
            def valMode = "dimValMode${i}"
            val = settings.find{it.key == valMode}.value

            log.debug "ADJUSTED WITH CURRENT MODE == > valMode = $valMode "
        }
    }
    if(atomicState.dimVal > valMode){
        atomicState.dimVal = valMode // for override and pause purposes only
    }
    else 
    {
        atomicState.dimVal = val // for override and pause purposes only
    }

    if(!atomicState.override || !OVRD){
        i = 0
        for(s != 0; i < s; i++){
            isNotOff = "on" in dimmers[i].currentValue("switch")
            if(isNotOff){
                dimmers[i].setLevel(val)
                log.debug "${dimmers[i]} set to $val"
            }
        }

    }
    else {
        log.debug "OVERRIDE MODE, DOING NOTHING"
    }

log.trace """
atomicState.dimVal = $atomicState.dimVal
val = $val
"""
}

