/**
*  Eternal Sunshine of the Spotless Flat
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
    name: "Eternal Sunshine of the Spotless Flat",
    namespace: "elfege",
    author: "Elfege",
    description: "Adjusts dimmer lever with light sensor (requires modified Lux Sensor Device Handler, contact me for details elfege@elfege.com)",
    category: "Convenience",
    iconUrl: "http://elfege.com/penrose.jpg",
    iconX2Url: "http://elfege.com/penrose.jpg",
    iconX3Url: "http://elfege.com/penrose.jpg")


preferences {
    section("select at least one dimmer") {
        input "dimmer", "capability.switchLevel", title: "pick a dimmer", required:true, multiple: true
    }
    section("select a ligt sensor") {
        input "lightSensor", "capability.illuminanceMeasurement", title: "pick a sensor", required:true, multiple: true
    }
    section("set a scale of dimming increment values ") {
        input "DimIncrVal", "decimal", title: "pick an increment value", range: "5..20", required:true, multiple: false
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

}

def illuminanceHandler(evt){



    log.debug "illuminance is $evt.integerValue"
	int dim = 0 

    def maxlux = 1000
    state.previousEvtValue = 0
    
    //int DimIncrVal = DimIncrVal
    //int i = maxlux / DimIncrVal
    //log.debug "value for i is: $i" 

	def ProportionLux = 0
    
	if(evt.integerValue != 0){
    ProportionLux = (maxlux / evt.integerValue) 
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
    dimmer.setLevel(dim) 
    log.debug "light set to $dim %"
}



