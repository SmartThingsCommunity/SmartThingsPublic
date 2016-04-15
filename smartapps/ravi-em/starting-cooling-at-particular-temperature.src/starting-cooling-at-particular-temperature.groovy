/**
 *  Starting cooling at particular temperature
 *
 *  Copyright 2016 Ravi Dubey
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
    name: "Starting cooling at particular temperature",
    namespace: "Ravi-em",
    author: "Ravi Dubey",
    description: "Start air conditioner when room temperature reaches to particular state. i.e. start cooling when room temperature reaches 70 F.",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {	
	section("When the temperature of this room...") {
		input "sensor", "capability.temperatureMeasurement", title: "Which temperature measurement", required:true
	}
    section("Is higher than ...") {
        input "temperaturelimit", "number", title: "How much F ?", required:true,defaultValue: 70
    }
    section("Which thermostat ...") {
        input "thethermostat", "capability.thermostat", required:true, title: "Which thermostat ?"
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
    subscribe(sensor, "temperature", "manageCooling")
    subscribe(thethermostat, "thermostatMode", "manageThermostat")
}

def manageCooling(evt){
	def currtemperature = evt.doubleValue
    
    log.debug "current temp: $currtemperature"
    
    if(currtemperature >= temperaturelimit){
    	thethermostat.cool();
        thethermostat.setCoolingSetpoint('50')
    }
    else
    {
    	def thermostatValue = thethermostat.currentState("thermostatMode").value;
        log.debug "The thermostat is currently ${thermostatValue}";        
        log.debug "No need to start cooling.."
        if(thermostatValue=='cool'){
        	thethermostat.off();
        }        
    }
}

def manageThermostat(evt){
	log.debug evt.value;
}
// TODO: implement event handlers