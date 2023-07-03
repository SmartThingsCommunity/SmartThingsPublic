/**
 *  Tibber Thermostat
 *
 *  Copyright 2017 J&oslash;ran Sandvoll
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
    name: "Tibber Thermostat",
    namespace: "Tibber",
    author: "Jøran Sandvoll",
    description: "Enable smart heating through Tibber. Make use of the Tibber and your SmartThings thermostats (you select which ones) are automatically adjusted based on your daily schedules, energy prices, your home\u2019s thermal capacity and weather information.",
    category: "Convenience",
    iconUrl: "https://store.tibber.com/no/wp-content/uploads/sites/8/2017/12/tibber_app_logo.png",
    iconX2Url: "https://store.tibber.com/no/wp-content/uploads/sites/8/2017/12/tibber_app_logo.png",
    iconX3Url: "https://store.tibber.com/no/wp-content/uploads/sites/8/2017/12/tibber_app_logo.png")

mappings {
  path("/thermostats") {
    action: [
      GET: "listThermostats"
    ]
  }
  path("/thermostats/:id/:setpoint") {
    action: [
      PUT: "updateThermostats"
    ]
  }
}

def listThermostats() {
    def resp = []
    thermostats.each {
        def temperatureState = it.temperatureState
        def heatingSetpointState = it.heatingSetpointState
        def range = it.currentValue("heatingSetpointRange")
        def rangeLow = 0
        def rangeHigh = 100
        if(range!=null){
        	rangeLow = range[0]
			rangeHigh = range[1]
        }
      	resp << [
        id: it.id, 
        name: it.displayName, 
        heatingSetpoint: it.currentValue("heatingSetpoint"),
        rangeLow: rangeLow,
        rangeHigh: rangeHigh,
        temperature: it.currentValue("temperature"), 
        mode: it.currentValue("thermostatMode"),
        heatingSetpointState: heatingSetpointState, 
        temperatureState: temperatureState]
    }
    
    return resp
}
void updateThermostats() {
    // use the built-in request object to get the command parameter
    def id = params.id
    def setpoint = Float.parseFloat(params.setpoint)
    log.debug(id)
    log.debug(setpoint)
    thermostats.each{
    	if(it.id==id){
        	it.setHeatingSetpoint(setpoint);
        }
    }
}


preferences {
  section ("Allow external service to control these things...") {
    input "thermostats", "capability.thermostat", multiple: true, required: false
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
}