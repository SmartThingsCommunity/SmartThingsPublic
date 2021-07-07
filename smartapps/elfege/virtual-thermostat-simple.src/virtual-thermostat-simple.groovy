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
    name: "Virtual Thermostat SIMPLE",
    namespace: "ELFEGE",
    author: "ELFEGE",
    description: "Control a space heater or window air conditioner in conjunction with any temperature sensor, like a SmartSense Multi.",
    category: "Green Living",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Meta/temp_thermo-switch.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Meta/temp_thermo-switch@2x.png"
)

preferences { 
    page name:"pageSetup"
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

        section("Select a switch") {
            input "SWT", "capability.switch", title: "pick a switch", required:true, multiple: false
        }
        section("Select a temperature measurment device"){
            input "temperature", "capability.temperatureMeasurement", title: "Thermostat", required: true, multiple: false, submitOnChange: true
        }
        section("Select cooling or heating") {
            input "desiredOperation", "enum", title: "select an option", required:true, options: ["cooling", "heating"]
        }
        section("Desired temperature") {
            input "desired", "number", title: "type in the desired temperature", required:true
        }
        section("Location Modes") {
            input "modes", "mode", title: "Set for specific mode(s)", multiple: true
        }

        section([mobileOnly:true]) {
            label title: "Assign a name", required: false
            //mode title: "Set for specific mode(s)", required: false // very bad for it might leave a heater on once mode changed

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

    subscribe(temperature, "temperature", temperatureHandler)
    subscribe(SWT, "switch", switchHandler)
    log.debug "initialization ok"
    schedule("23 0/1 * * * ?", evaluate)
    evaluate()
}


def temperatureHandler(evt){
    log.debug "$evt.device returns ${evt.value}F"
    evaluate()
}

def switchHandler(evt){

    log.debug "$evt.device is $evt.value"
    evaluate()
}

def evaluate(){



    def currTemp = temperature.currentValue("temperature") as double
        def SWTstate = SWT.currentValue("switch")

        if(location.currentMode in modes){

            log.debug "currTemp = $currTemp, desired = $desired, SWTstate = $SWTstate"
            if(desiredOperation == "cooling"){
                log.debug "cooling"
                if(currTemp > desired){          
                    if(SWTstate == "off"){
                        SWT.on()               
                    }
                }
                else if(currTemp < desired){
                    if( SWTstate != "off"){
                        SWT.off()
                    }
                    else {
                        log.debug "$SWT already off (1)"
                    }
                }
            }
            // heating
            else {
                log.debug "heating"
                if(currTemp < desired){ 
                    if( SWTstate == "off"){
                        SWT.on()             
                    }
                }       
                else if(currTemp > desired){
                    if( SWTstate != "off"){
                        SWT.off()
                    }
                    else {
                        log.debug "$SWT already off (2)"
                    }
                }
            }
        }
    else {
        if( SWTstate != "off"){
            log.debug "outside of location modes, turning off all devices"
            SWT.off()
        }
        else {
            log.debug "outside of location modes"
        }
    }
}

