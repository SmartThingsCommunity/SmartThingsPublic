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
 *
 *	23.11.2015
 *	v1.1 - Now with support for Switch detection. 
 *		   Dynamic preference screen. 
 *	 	   Introduced option to disable thermostat reset.
 *
 *	24.11.2015
 *	v1.2 - 	 Extra Boost handling capabilities. 
 *		   	 Fixed bug where no reset was specified and app doesn't reset variable 'state.thermostatAltered'.
 *	v1.2.1 - Bug fixes
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
	page(name: "configurePage")
  
}

def configurePage() {
	dynamicPage(name: "configurePage", title: "", install: true, uninstall: true) {
		section {
    		input ("thermostats", "capability.thermostat", title: "For these thermostats",  multiple: true, required: true)
  		}
        
        section {
            input(name: "modeTrigger", title: "Set the trigger to",
                  description: null, multiple: false, required: true, submitOnChange: true, type: "enum", 
                  options: ["true": "Mode Change", "false": "Switches"])
        }

        
        if (modeTrigger == "true") {
            // Do something here like update a message on the screen,
            // or introduce more inputs. submitOnChange will refresh
            // the page and allow the user to see the changes immediately.
            // For example, you could prompt for the level of the dimmers
            // if dimmers have been selected:

            section {
       			input ("modes", "mode", title:"When SmartThings enters these modes", multiple: true, required: true)
  			}
        }
        else if (modeTrigger == "false"){
        	section {
        		input ("theSwitch", "capability.switch", title:"When this switch is activated", multiple: false, required: true) 		
          	}      
        }
 
  		section {
    		input ("alteredThermostatMode", "enum", multiple: false, title: "Set thermostats to this mode",
            options: ["Set To Schedule", "Boost for 60 minutes", "Turn Off", "Set to Manual"], required: true, defaultValue: 'Turn Off')
  		}
        
        section {
        	input ("resetThermostats", "enum", title: "Reset thermostats after trigger turns off?",
            options: ["true": "Yes","false": "No"], required: true, submitOnChange: true)
  		}
        
        if (resetThermostats == "true") {
            section {
    			input ("resumedThermostatMode", "enum", multiple: false, title: "Reset thermostats back to this mode",
            	options: ["Set To Schedule", "Boost for 60 minutes", "Turn Off", "Set to Manual"], required: true, defaultValue: 'Set To Schedule')
  			}
        }
  
  		section( "Additional configuration" ) {
  			input ("temp", "number", title: "If setting to Manual, set the temperature to this", required: false, defaultValue: 21)
  		}
  
  		section( "Notifications" ) {
        	input ("sendPushMessage", "enum", title: "Send a push notification?",
            options: ["Yes", "No"], required: true)
        	input ("phone", "phone", title: "Send a Text Message?", required: false)
  		}
        
        section {
        	label title: "Assign a name", required: false
        }
  	}
}

def installed() {
    log.debug "Installed with settings: ${settings}"
    //set up initial thermostat state and force thermostat into correct mode
    state.thermostatAltered = false
    
    //Check if mode or switch is the trigger and run initialisation
    if (modeTrigger == "true") {
    	def currentMode = location.mode
    	log.debug "currentMode = $currentMode"
    	if (currentMode in modes) {
        	takeActionForMode(currentMode)
    	}
    	subscribe(location, "mode", modeeventHandler)
    }
    else {
    	if (theSwitch.currentSwitch == "on") {
        	takeActionForSwitch(theSwitch.currentSwitch)
        }
    	subscribe(theSwitch, "switch", switchHandler)
        subscribe(thermostats, "thermostatMode", thermostateventHandler)
    }
}

def updated() {
    log.debug "Updated with settings: ${settings}"
    unsubscribe()
    //set up initial thermostat state and force thermostat into correct mode
    state.thermostatAltered = false
    
    //Check if mode or switch is the trigger and run initialisation
    if (modeTrigger == "true") {
    	def currentMode = location.mode
    	log.debug "currentMode = $currentMode"
    	if (currentMode in modes) {
        	takeActionForMode(currentMode)
    	}
    	subscribe(location, "mode", modeeventHandler)
    }
    else {
    	if (theSwitch.currentSwitch == "on") {
        	takeActionForSwitch(theSwitch.currentSwitch)
        }
    	subscribe(theSwitch, "switch", switchHandler)
        subscribe(thermostats, "thermostatMode", thermostateventHandler)
    }
}

//Handler and action for switch detection
def switchHandler(evt) {
	log.debug "evt.name: $evt.value"
    takeActionForSwitch(evt.value)   
}

def takeActionForSwitch(switchState) {
	// Is incoming switch is on
    if (switchState == "on")
    {
    	//Check thermostat is not already altered
    	if (!state.thermostatAltered)
        {
        	//Turn selected thermostats into selected mode
        	           
            //Add detail to push message if set to Manual is specified
        	log.debug "$theSwitch.label is on, turning thermostats to $alteredThermostatMode"
            changeAllThermostatsModes(thermostats, alteredThermostatMode, "$theSwitch.label has turned on")
            //Only if reset action is specified, set the thermostatAltered state.
            if (resetThermostats == "true")
            {
        		state.thermostatAltered = true
            }
        }
    }
    else {
        log.debug "$theSwitch.label is off"
        //Check if thermostats have previously been altered
        if (state.thermostatAltered)
        {
        	//Check if user wants to reset thermostats
        	if (resetThermostats == "true")
 			{
        		//Add detail to push message if set to Manual is specified
        		log.debug "Thermostats have been altered, turning back to $resumedThermostatMode"
            	changeAllThermostatsModes(thermostats, resumedThermostatMode, "$theSwitch.label has turned off")
            }
            //Reset app state
            state.thermostatAltered = false
        }
        else
     	{
        	log.debug "Thermostats were not altered. No action taken."
        }
    }
}

//Handler and action for mode detection
def modeeventHandler(evt) {
    log.debug "evt.name: $evt.value"
    takeActionForMode(evt.value)   
}

def takeActionForMode(mode) {
	// Is incoming mode in the event input enumeration
    if (mode in modes)
    {
    	//Check thermostat is not already altered
    	if (!state.thermostatAltered)
        {
        	//Turn selected thermostats into selected mode
        	           
            //Add detail to push message if set to Manual is specified
        	log.debug "$mode in selected modes, turning thermostats to $alteredThermostatMode"
            changeAllThermostatsModes(thermostats, alteredThermostatMode, "mode has changed to $mode")
            
        	//Only if reset action is specified, set the thermostatAltered state.
            if (resetThermostats == "true")
            {
        		state.thermostatAltered = true
            }
        }
    }
    else {
        log.debug "$mode is not in select modes"
        //Check if thermostats have previously been altered
        if (state.thermostatAltered)
        {
        	//Check if user wants to reset thermostats
        	if (resetThermostats == "true")
 			{
            	log.debug "Thermostats have been altered, turning back to $resumedThermostatMode"
        		       
            	//Turn each thermostat to desired mode
            	changeAllThermostatsModes(thermostats, resumedThermostatMode, "mode has changed to $mode")   

            }
            //Reset app state
            state.thermostatAltered = false
        }
        else
     	{
        	log.debug "Thermostats were not altered. No action taken."
        }
    }
}

//Handler for thermostat mode change
def thermostateventHandler(evt) {
	log.debug "evt.name: $evt.value"
    //If boost mode is selected as the trigger, turn switch off if boost mode finishes...
    if (alteredThermostatMode == "Boost for 60 minutes") {
    	//if the switch is currently on, check the new mode of the thermostat and set switch to off if necessary
        if (evt.value != "emergency heat") {
    		if (theSwitch.currentSwitch == "on") {
                def message = ''
        		message = "Boost has now finished. Turning $theSwitch.label off."
        		log.info message
        		send(message)
                //Switching the switch to off should trigger an event that resets app state
        		theSwitch.off()
                
        	}
        }
   	}
}

//Helper method for thermostat mode change
private changeAllThermostatsModes(thermostats, newThermostatMode, reason) {
	//Add detail to push message if set to Manual is specified
    def thermostatModeDetail = newThermostatMode
    if (newThermostatMode == "Set to Manual") {
    	thermostatModeDetail = thermostatModeDetail + " at $temp°C"
    }
	for (thermostat in thermostats) {
    	def message = ''
        message = "SmartThings has reset $thermostat.label to $thermostatModeDetail because $reason."
        log.info message
        send(message)
        log.debug "Setting $thermostat.label to $thermostatModeDetail" 
		if (newThermostatMode == "Set to Manual") {
    		thermostat.heat()
        	thermostat.setHeatingSetpoint(temp)
    	}
    	else if (newThermostatMode == "Turn Off") {
    		thermostat.off()
    	}
    	else if (newThermostatMode == "Boost for 60 minutes") {
    		thermostat.auto()
        	thermostat.emergencyHeat()
    	}
    	else {
    		thermostat.auto()
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