/**
 *  Copyright 2016 M. Fox
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
 *  Based on:
 *  Keep Me Cozy
 *  Author: SmartThings
 */
definition(
    name: "Thermosmart",
    namespace: "foxtechnology",
    author: "M. Fox",
    description: "V1.1n: Changes your thermostat settings automatically in response to a mode change. You can either SET the thermostate to your desired temperature or RESUME the regular schedule. ",
    category: "Green Living",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Meta/temp_thermo.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Meta/temp_thermo@2x.png"
)

preferences {
	 page(name: "setThermostat")
}

def setThermostat() {
	dynamicPage(name: "setThermostat", title: null, install: true, uninstall: true) {
 
		section{
			input(name: "thermostats", type: "capability.thermostat", title: "Thermostats",
            	description: null, multiple: true, required: true)
        }
        
	    section{
    	    input(name: "state", type: "enum", title: "State", 
            	description: null, multiple: false, required: true, options: ["Set","Resume"], submitOnChange: true)
	    }
        
        section {
            label title: "Assign a name", required: false
            mode title: "Set for specific mode(s)", required: true
        }        
        
		if (settings.state == "Set") {
			section {
				input(name: "heatingSetpoint", type: "number", title: "Heating Setpoint Degrees?", required: true)
            }
            section {
				input(name: "coolingSetpoint", type: "number", title: "Cooling Setpoint Degrees?", required: true) 
			}
		}
	}
}



def installed()
{
	subscribe(thermostats, "heatingSetpoint", heatingSetpointHandler)
	subscribe(thermostats, "coolingSetpoint", coolingSetpointHandler)
	subscribe(thermostats, "temperature", temperatureHandler)
	subscribe(location, changedLocationMode)
	subscribe(app, appTouch)
}

def updated()
{
	unsubscribe()
	subscribe(thermostats, "heatingSetpoint", heatingSetpointHandler)
	subscribe(thermostats, "coolingSetpoint", coolingSetpointHandler)
	subscribe(thermostats, "temperature", temperatureHandler)
	subscribe(location, changedLocationMode)
	subscribe(app, appTouch)
}

def heatingSetpointHandler(evt)
{
	log.debug "heatingSetpointHandler: $evt, $settings"
}

def coolingSetpointHandler(evt)
{
	log.debug "coolingSetpointHandler: $evt, $settings"
}

def temperatureHandler(evt)
{
	log.debug "TemperatureHandler: $evt, $settings"
}

def changedLocationMode(evt)
{
    if(settings.state == "Set")
    {
	  thermostats.setHeatingSetpoint(heatingSetpoint)
	  thermostats.setCoolingSetpoint(coolingSetpoint)
    }
    else if (settings.state == "Resume") 
    {
      thermostats.resumeProgram()
    }
    
}

def appTouch(evt)
{
	log.debug "appTouch: $evt, $settings"
    log.debug "Current state: $settings.state"
    if(settings.state == "Set"){
       settings.state = "Resume"
       log.debug "setting state to Reset: $settings.state"
    }
    else {
       settings.state = "Set"
       log.debug "setting state to Set: $settings.state"
    }
  

}

// catchall
def event(evt)
{
	log.debug "value: $evt.value, event: $evt, settings: $settings, handlerName: ${evt.handlerName}"
}