/**
 *  Thermostat Mode Automation (CHILD APP to Auto Mode for Thermostats)
 *
 *  Copyright 2015 Alex Lee Yuk Cheung
 *
 *	1. 	Save and Self-publish 'Auto Mode for Thermostats' SmartApp (https://github.com/alyc100/SmartThingsPublic/blob/master/smartapps/alyc100/parent/auto-mode-for-thermostats.src/auto-mode-for-thermostats.groovy)
 *		by creating a new SmartApp in the SmartThings IDE and pasting the source code in the "From Code" tab. 
 *
 *	2. 	Save (do not publish) 'Thermostat Mode Automation' SmartApp (https://github.com/alyc100/SmartThingsPublic/blob/master/smartapps/alyc100/thermostatmodeautomation/thermostat-mode-automation.src/thermostat-mode-automation.groovy)
 *		by creating a new SmartApp in the SmartThings IDE and pasting the source code in the "From Code" tab. 
 *
 *	3. 	Open SmartThings mobile app and locate "Auto Mode for Thermostats" SmartApp in the "My Apps" section of the Marketplace.
 *
 * 	Changes operating mode (e.g off) of selected thermostats when Smartthings hub changes into selected modes (e.g away). 
 *	Turns thermostats back into another desired operating mode (e.g Emergency Heat) when mode changes back (e.g home).
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 *	VERSION HISTORY
 *  22.11.2015
 *	v1.0 - Initial Release
 */

definition(
    name:        "Thermostat Mode Automation",
    namespace:   "alyc100/thermostatmodeautomation",
    author:      "Alex Lee Yuk Cheung",
    description: "Turns off selected thermostats when Smartthings hub changes into selected modes (e.g away). Turns thermostats back into desired operating state when mode changes back (e.g home).",
    category:    "My Apps",
    iconUrl:     "https://s3.amazonaws.com/smartapp-icons/Meta/temp_thermo.png",
    iconX2Url:   "https://s3.amazonaws.com/smartapp-icons/Meta/temp_thermo@2x.png"
)

preferences {
  section("When SmartThings enters these modes") {
        input "modes", "mode", multiple: true, required: true
  }

  section("Using these thermostats") {
    	input "thermostats", "capability.thermostat", multiple: true, required: true
  }
  
  section("Set thermostats to this mode") {
    	input "alteredThermostatMode", "enum", multiple: false,
              options: ["Set To Schedule", "Boost for 60 minutes", "Turn Off", "Set to Manual"], required: true, defaultValue: 'Keep Off'
  }
  
  section("And then change thermostats back to this mode when SmartThings mode changes back") {
    	input "thermostatMode", "enum", multiple: false,
              options: ["Set To Schedule", "Boost for 60 minutes", "Keep Off", "Set to Manual"], required: true, defaultValue: 'Set To Schedule'
  }
  
  section("If setting to Manual, set the temperature to this") {
  		input "temp", "number", required: false, defaultValue: 21
  }
  
  section( "Notifications" ) {
        input "sendPushMessage", "enum", title: "Send a push notification?",
            options: ["Yes", "No"], required: false
        input "phone", "phone", title: "Send a Text Message?", required: false
  }
}

def installed() {
    log.debug "Installed with settings: ${settings}"
    def currentMode = location.mode
    log.debug "currentMode = $currentMode"
    //set up initial thermostat state and force thermostat into correct mode
    state.thermostatAltered = false
    if (currentMode in modes) {
        takeAction(currentMode)
    }
    subscribe(location, "mode", modeevent)
}

def updated() {
    log.debug "Updated with settings: ${settings}"
    def currentMode = location.mode
    log.debug "currentMode = $currentMode"
    unsubscribe()
    //set up initial thermostat state and force thermostat into correct mode
    state.thermostatAltered = false
    if (currentMode in modes) {
        takeAction(currentMode)
    }
    subscribe(location, "mode", modeevent)
}

def modeevent(evt) {
    log.debug "evt.name: $evt.value"
    takeAction(evt.value)   
}

def takeAction(mode) {
	// Is incoming mode in the event input enumeration
    if (mode in modes)
    {
    	//Check thermostat is not already altered
    	if (!state.thermostatAltered)
        {
        	//Turn selected thermostats into selected mode
        	           
            //Add detail to push message if set to Manual is specified
        	log.debug "$mode in selected modes, turning thermostats to $alteredThermostatMode"
            def thermostatModeDetail = alteredThermostatMode
            if (alteredThermostatMode == "Set to Manual") {
            	thermostatModeDetail = thermostatModeDetail + " at $temp°C"
            }
            
            //Turn each thermostat to desired mode
            def message = ''
            for (thermostat in thermostats) {
            	message = "SmartThings has turned $thermostat.label to $alteredThermostatMode because mode has changed to $mode"
        		log.info message
            	send(message)
           		log.debug "Setting $thermostat.label to $thermostatModeDetail"
            	if (alteredThermostatMode == "Set to Manual") {
            		thermostat.heat()
             	   	thermostat.setHeatingSetpoint(temp)
            	}
            	else if (alteredThermostatMode == "Turn Off") {
            		thermostat.off()
            	}
            	else if (alteredThermostatMode == "Boost for 60 minutes") {
                	thermostat.auto()
            		thermostat.emergencyHeat()
            	}
            	else {
            		thermostat.auto()
				}
            }
        	state.thermostatAltered = true
        }
    }
    else {
        log.debug "$mode is not in select modes"
        //Check if thermostats have previously been altered
        if (state.thermostatAltered)
        {
        	//Add detail to push message if set to Manual is specified
        	log.debug "Thermostats have been altered, turning back to $thermostatMode"
            def thermostatModeDetail = thermostatMode
            if (thermostatMode == "Set to Manual") {
            	thermostatModeDetail = thermostatModeDetail + " at $temp°C"
            }
                        
            //Turn each thermostat to desired mode
            def message = ''
            for (thermostat in thermostats) {
            	message = "SmartThings has turned $thermostat.label to $thermostatModeDetail because mode has changed to $mode."
            	log.info message
            	send(message)
           		log.debug "Setting $thermostat.label to $thermostatModeDetail"
            	if (thermostatMode == "Set to Manual") {
            		thermostat.heat()
             	   	thermostat.setHeatingSetpoint(temp)
            	}
            	else if (thermostatMode == "Keep Off") {
            		thermostat.off()
            	}
            	else if (thermostatMode == "Boost for 60 minutes") {
                	thermostat.auto()
            		thermostat.emergencyHeat()
            	}
            	else {
            		thermostat.auto()
				}
  			}
            state.thermostatAltered = false
        }
        else
     	{
        	log.debug "Thermostats were not altered. No action taken."
        }
    }
}

private send(msg) {
    if ( sendPushMessage != "No" ) {
        log.debug( "sending push message" )
        sendPush( msg )
    }

    if ( phone ) {
        log.debug( "sending text message" )
        sendSms( phone, msg )
    }

    log.debug msg
}