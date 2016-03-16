/**
 *  SLEEPsense-thermostat control
 *
 *  Copyright 2015 Mobileapp
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
    name: "SLEEPsense-thermostat control",
    namespace: "samsung",
    author: "Mobileapp",
    description: "Control thermostat when you sleep & wake up.",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
	page(name:"Mainpage", install:true, uninstall:true) {
        section("Select sleep sense.") {
            input "sleepsensor", "capability.sleepSensor", title: "Sleep Sensor", multiple: false, required: true
        }
        section("Set temperature") {
            input "thermostatcontrol", "capability.thermostat", title: "Thermostat control", multiple: false, required: true
            input "inbed_temperature", number, title :"In bed"
            input "outofbed_temperature", number, title :"Out of bed"
            input "sleep_temperature", number, title :"Sleeping"
            input "wake_temperature", number, title :"Awake"
        }
    }
}


def installed()
{
	log.trace "installed()"
   	state.sleepAction = "Out Bed"

    subscribe(sleepsensor, "sleeping", sleepsensorHandler)    
}

def updated()
{
	log.trace "updated()"
	state.sleepAction = "Out Bed"
    
	unsubscribe()
    subscribe(sleepsensor, "sleeping", sleepsensorHandler)
    subscribe(sleepsensor, "bedstate", sleepsensorHandler)
  
}

def initialize() {
	// TODO: subscribe to attributes, devices, locations, etc.
}


def sleepsensorHandler(evt) {
	//log.debug "sleepsensorHandler: $evt.name"	
    log.debug "sleepsensorHandler: $evt.value"
    state.isOK = false;
    
    if(evt.value == "in bed")
    {
    	log.debug "In Bed actions will be excuted"
        state.sleepAction = "in bed"
    	state.isOK = true;
    }
    else if(evt.value == "out of bed")
    {
    	log.debug "Out Bed actions will be excuted"
        state.sleepAction = "Out Bed"
    	state.isOK = true;
    }
    else if(evt.value == "sleeping")
    {
    	log.debug "Sleeping actions will be excuted"
        state.sleepAction = "Sleeping"
    	state.isOK = true;
    }
    else if(evt.value == "not sleeping")
    {
    	log.debug "Awake actions will be excuted"
      	state.sleepAction = "Awake"
    	state.isOK = true;
    }
    else
    {
    	return
    }
    
    thermostatcontrol()
   
}



def thermostatcontrol() {
    if(state.isOK == true)
    {    	
       if(state.sleepAction == "in bed")
        {
            log.debug "In Bed temperature control"
            thermostatcontrol.each
            {
            	log.trace inbed_temperature
                log.trace thermostatcontrol                
	            def value = inbed_temperature as Double
                thermostatcontrol.setCoolingSetpoint(value)
                
            }            
        }
        else if(state.sleepAction == "Out Bed")
        {
            log.debug "Out Bed temperature control"
            thermostatcontrol.each
            {
	            log.trace inbed_temperature
    	        log.trace thermostatcontrol               
                if(outofbed_temperature == null)
                {
	    	        log.trace "input data null"
                }
                else
                {
                    def value = outofbed_temperature as Double
                    thermostatcontrol.setCoolingSetpoint(value)
                }
            }
        }
        else if(state.sleepAction == "Sleeping")
        {
            log.debug "Sleeping temperature control"
            thermostatcontrol.each
            {
	            log.trace inbed_temperature
    	        log.trace thermostatcontrol                
        	    def value = sleep_temperature as Double
            	thermostatcontrol.setCoolingSetpoint(value)
            }
        }
        else if(state.sleepAction == "Awake")
        {
            log.debug "Awake temperature control"
            thermostatcontrol.each
            {
	            log.trace inbed_temperature
    	        log.trace thermostatcontrol                
        	    def value = wake_temperature as Double
            	thermostatcontrol.setCoolingSetpoint(value)
            }
        }
    }
}

def changedLocationMode(evt) {
	log.debug "changedLocationMode: $evt"
	switches?.on()
}

def appTouch(evt) {
	log.debug "appTouch: $evt"
	switches?.on()
}
