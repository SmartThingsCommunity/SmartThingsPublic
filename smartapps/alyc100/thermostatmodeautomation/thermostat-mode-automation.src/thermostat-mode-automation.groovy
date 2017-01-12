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
 *	v1.2.1 - Bug fixes.
 *	v1.3 -	 Option added to set mode of thermostat after boost action if reset mode is set to 'Boost'.
 * 	v1.3.1 - Bug fixes.
 *  v1.3.2 - Stop possible infinite loop when handlers create events themselves.
 *	v1.3.3 - Label change for Boost
 *  v1.4 - 	 Option added to restrict Auto Mode rules to specific days and time of day.
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
            options: ["Set To Schedule", "Boost", "Turn Off", "Set to Manual"], required: true, defaultValue: 'Turn Off')
  		}
        
        section {
        	input ("resetThermostats", "enum", title: "Reset thermostats after trigger turns off?",
            options: ["true": "Yes","false": "No"], required: true, submitOnChange: true)
  		}
        
        if (resetThermostats == "true") {
            section {
    			input ("resumedThermostatMode", "enum", multiple: false, title: "Reset thermostats back to this mode", submitOnChange: true,
            	options: ["Set To Schedule", "Boost", "Turn Off", "Set to Manual"], required: true, defaultValue: 'Set To Schedule')
  			}
            
            if (resumedThermostatMode == "Boost") {
            	section {
    				input ("thermostatModeAfterBoost", "enum", multiple: false, title: "What to do when Boost has finished",
            		options: ["Set To Schedule", "Turn Off", "Set to Manual"], required: true, defaultValue: 'Set To Schedule')
  				}
            }
        }
  
  		section( "Additional configuration" ) {
            input "days", "enum", title: "Only on certain days of the week", multiple: true, required: false,
				options: ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"]
            href "timeIntervalInput", title: "Only during a certain time", description: getTimeLabel(starting, ending), state: greyedOutTime(starting, ending), refreshAfterSelection:true
  			input ("temp", "decimal", title: "If setting to Manual, set the temperature to this", required: false, defaultValue: 21)
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

page(name: "timeIntervalInput", title: "Only during a certain time", refreshAfterSelection:true) {
	section {
			input "starting", "time", title: "Starting", required: false 
			input "ending", "time", title: "Ending", required: false 
	}
}

def installed() {
    log.debug "Installed with settings: ${settings}"
    //set up initial thermostat state and force thermostat into correct mode
    state.thermostatAltered = false
    state.boostingReset = false
    
    //Flags to stop possible infinite loop scenarios when handlers create events
    state.internalThermostatEvent = false
    state.internalSwitchEvent = false
    
    subscribe(thermostats, "thermostatMode", thermostateventHandler)
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
    }
}

def updated() {
    log.debug "Updated with settings: ${settings}"
    unsubscribe()
    //set up initial thermostat state and force thermostat into correct mode
    state.thermostatAltered = false
    state.boostingReset = false
    state.internalThermostatEvent = false
    state.internalSwitchEvent = false
    subscribe(thermostats, "thermostatMode", thermostateventHandler)
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
    }   
}

//Handler and action for switch detection
def switchHandler(evt) {
	if(allOk) {
		log.debug "evt.value: $evt.value"
    	log.debug "state.internalSwitchEvent: $state.internalSwitchEvent"
    	if (state.internalSwitchEvent == false) {
    		takeActionForSwitch(evt.value)   
    	}
    	state.internalSwitchEvent = false
    }
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
            state.internalThermostatEvent = true
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
            	log.debug "Thermostats have been altered, turning back to $resumedThermostatMode"     		
                //Turn selected thermostats into selected mode
                state.internalThermostatEvent = true
            	changeAllThermostatsModes(thermostats, resumedThermostatMode, "$theSwitch.label has turned off")
            	
                //Set flag if boost mode is selected as reset state so it can be set back to desired mode in 'thermostatModeAfterBoost'
                if (resumedThermostatMode == "Boost") {
                	state.boostingReset = true
                }
                
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
	if(allOk) {
    	log.debug "evt.value: $evt.value"
    	takeActionForMode(evt.value)   
    }
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
            state.internalThermostatEvent = true
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
     		       
            	//Turn each thermostat to selected mode
                state.internalThermostatEvent = true
            	changeAllThermostatsModes(thermostats, resumedThermostatMode, "mode has changed to $mode")   

 				//Set flag if boost mode is selected as reset state so it can be set back to desired mode in 'thermostatModeAfterBoost'
                if (resumedThermostatMode == "Boost") {
                	state.boostingReset = true
                }  
                              
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
    log.debug "state.thermostatAltered: $state.thermostatAltered"
    log.debug "alteredThermostatMode: $alteredThermostatMode"
    log.debug "state.boostingReset: $state.boostingReset"
    //If boost mode is selected as the trigger, turn switch off if boost mode finishes...
 	if (state.internalThermostatEvent == false) {
    	if (modeTrigger == "false") {    
    		//if the switch is currently on, check the new mode of the thermostat and set switch to off if necessary
        	if (alteredThermostatMode == "Boost") {
            	state.internalSwitchEvent = true
        		if (evt.value != "emergency heat") {
                	//Switching the switch to off should trigger an event that resets app state
        			theSwitch.off()
        		} 
            	else {
            		//Switching the switch to on so it can't be boost again
            		theSwitch.on()
                }
            }
       	 } 
    
    	//If boost mode is selected as resumed state, need to set thermostat mode as per preference
    	if (state.boostingReset) {
    		if (evt.value != "emergency heat") {
            	state.internalThermostatEvent = true
        		changeAllThermostatsModes(thermostats, thermostatModeAfterBoost, "Boost has now finished")
            	//Reset boosting reset flag
            	state.boostingReset = false           
        	}
    	}
    }
    state.internalThermostatEvent = false
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
    	else if (newThermostatMode == "Boost") {
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

private getAllOk() {
	daysOk && timeOk
}

private getDaysOk() {
	def result = true
	if (days) {
		def df = new java.text.SimpleDateFormat("EEEE")
		if (location.timeZone) {
			df.setTimeZone(location.timeZone)
		}
		else {
			df.setTimeZone(TimeZone.getTimeZone("Europe/London"))
		}
		def day = df.format(new Date())
		result = days.contains(day)
	}
	log.trace "daysOk = $result"
	result
}

private getTimeOk() {
	def result = true
	if (starting && ending) {
		def currTime = now()
		def start = timeToday(starting).time
		def stop = timeToday(ending).time
		result = start < stop ? currTime >= start && currTime <= stop : currTime <= stop || currTime >= start
	}
	log.trace "timeOk = $result"
	result
}

private hhmm(time, fmt = "h:mm a")
{
	def t = timeToday(time, location.timeZone)
	def f = new java.text.SimpleDateFormat(fmt)
	f.setTimeZone(location.timeZone ?: timeZone(time))
	f.format(t)
}

def getTimeLabel(starting, ending){

	def timeLabel = "Tap to set"
	
    if(starting && ending){
    	timeLabel = "Between" + " " + hhmm(starting) + " "  + "and" + " " +  hhmm(ending)
    }
    else if (starting) {
		timeLabel = "Start at" + " " + hhmm(starting)
    }
    else if(ending){
    timeLabel = "End at" + hhmm(ending)
    }
	timeLabel
}

private hideOptionsSection() {
	(starting || ending || days || modes) ? false : true
}


def greyedOutSettings(){
	def result = ""
    if (starting || ending || days || falseAlarmThreshold) {
    	result = "complete"	
    }
    result
}

def greyedOutTime(starting, ending){
	def result = ""
    if (starting || ending) {
    	result = "complete"	
    }
    result
}
