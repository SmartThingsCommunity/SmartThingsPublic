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
    description: "Control thermostat when you sleep, wake up, in-bed, out-bed.",
    category: "Health & Wellness",
    iconUrl: "http://cdn.device-icons.smartthings.com/Health%20&%20Wellness/health9-icn@2x.png",
    iconX2Url: "http://cdn.device-icons.smartthings.com/Health%20&%20Wellness/health9-icn@2x.png",
    iconX3Url: "http://cdn.device-icons.smartthings.com/Health%20&%20Wellness/health9-icn@2x.png")


preferences {
	page(name:"Mainpage", install:true, uninstall:true) {
        section("Select sleep sense.") {
            input "sleepsensor", "capability.sleepSensor", title: "Select SLEEPsense", multiple: false, required: true
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

    subscribe(sleepsensor, "sleeping", sleepsensorHandler)
    subscribe(sleepsensor, "bedstate", sleepsensorHandler)
}

def updated()
{
	log.trace "updated()"
	unsubscribe()
    subscribe(sleepsensor, "sleeping", sleepsensorHandler)
    subscribe(sleepsensor, "bedstate", sleepsensorHandler)
}

def initialize() {
	// TODO: subscribe to attributes, devices, locations, etc.
}


def sleepsensorHandler(evt) {
    log.debug "sleepsensorHandler: $evt.value"
    
    if(evt.value == "in bed")
    {
    	log.debug "In Bed temperature control"

        thermostatcontrol.each
        {
            log.trace inbed_temperature
            if(inbed_temperature == null)
            {
                log.trace "inbed_temperature data null"
            }
            else
            {
                def value = inbed_temperature as Double
                thermostatcontrol.setCoolingSetpoint(value)
            }

        }            
    }
    else if(evt.value == "out of bed")
    {
    	log.debug "Out Bed temperature control"
        thermostatcontrol.each
        {
            log.trace outofbed_temperature
            if(outofbed_temperature == null)
            {
                log.trace "outofbed_temperature data null"
            }
            else
            {
                def value = outofbed_temperature as Double
                thermostatcontrol.setCoolingSetpoint(value)
            }
        }
    }
    else if(evt.value == "sleeping")
    {
    	log.debug "Sleeping temperature control"
        thermostatcontrol.each
        {
            log.trace sleep_temperature
            if(sleep_temperature == null)
            {
                log.trace "sleep_temperature data null"
            }
            else
			{
            	def value = sleep_temperature as Double
                thermostatcontrol.setCoolingSetpoint(value)
            }
        }
    }
    else if(evt.value == "not sleeping")
    {
        log.debug "Awake temperature control"
        thermostatcontrol.each
        {
            log.trace wake_temperature
            if(wake_temperature == null)
            {
                log.trace "wake_temperature data null"
            }
            else
            {
                def value = wake_temperature as Double
                thermostatcontrol.setCoolingSetpoint(value)
            }
        }
    }
    
    if(isOK == true)
    {    	
       if(state.sleepAction == "in bed")
        {
        }
        else if(state.sleepAction == "Out Bed")
        {
        }
        else if(state.sleepAction == "Sleeping")
        {
        }
        else if(state.sleepAction == "Awake")
        {
        }
    }
}



