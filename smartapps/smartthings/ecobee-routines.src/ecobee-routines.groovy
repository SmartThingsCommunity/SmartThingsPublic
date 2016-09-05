/**
 *  ecobee Routines
 *
 *  Copyright 2015 Sean Kendall Schneyer
 *
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
def getVersionNum() { return "0.1.4" }
private def getVersionLabel() { return "ecobee Routines Version ${getVersionNum()}" }

/*
 *
 * 0.1.4 - Fix Custom Mode Handling
 *
 */


definition(
	name: "ecobee Routines",
	namespace: "smartthings",
	author: "Sean Kendall Schneyer (smartthings at linuxbox dot org)",
	description: "Support for changing ecobee Programs based on SmartThings Routine execution or Mode changes",
	category: "Convenience",
	parent: "smartthings:Ecobee (Connect)",
	iconUrl: "https://s3.amazonaws.com/smartapp-icons/Partner/ecobee.png",
	iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Partner/ecobee@2x.png",
	singleInstance: false
)

preferences {
	page(name: "mainPage")
}

// Preferences Pages
def mainPage() {
	dynamicPage(name: "mainPage", title: "Setup Routines", uninstall: true, install: true) {
    	section(title: "Name for Routine Handler") {
        	label title: "Name this Routine Handler", required: true
        
        }
        
        section(title: "Select Thermostats") {
        	if(settings.tempDisable == true) paragraph "WARNING: Temporarily Disabled as requested. Turn back on to activate handler."
        	input ("myThermostats", "capability.Thermostat", title: "Pick Ecobee Thermostat(s)", required: true, multiple: true, submitOnChange: true)            
		}
        
        section(title: "Select Mode or Routine") {
        	// Settings option for using Mode or Routine
            input(name: "modeOrRoutine", title: "Use Mode Change, Routine Execution: ", type: "enum", required: true, multiple: false, description: "Tap to choose...", metadata:[values:["Mode", "Routine"]], submitOnChange: true)
		}
        
	        if (myThermostats?.size() > 0) {
            	if(settings.modeOrRoutine == "Mode") {
	    	    	// Start defining which Modes(s) to allow the SmartApp to execute in
                    // TODO: Need to run in all modes now and then filter on which modes were selected!!!
    	            //mode(title: "When Hello Mode(s) changes to: ", required: true)
                    section(title: "Modes") {
                    	input(name: "modes", type: "mode", title: "When Hello Mode(s) change to: ", required: true, multiple: true)
					}
                } else if(settings.modeOrRoutine == "Routine") {
                	// Routine based inputs
                     def actions = location.helloHome?.getPhrases()*.label
					if (actions) {
            			// sort them alphabetically
            			actions.sort()
						LOG("Actions found: ${actions}", 4)
						// use the actions as the options for an enum input
                        section(title: "Routines") {
							input(name:"action", type:"enum", title: "When these Routines execute: ", options: actions, required: true, multiple: true)
                        }
					} // End if (actions)
                } // Mode or Routine If/Then/Else

				section(title: "Actions") {
                	def programs = getEcobeePrograms()
                    programs = programs + ["Resume Program"]
                	LOG("Found the following programs: ${programs}", 4)
                    
	               	input(name: "whichProgram", title: "Switch to this Ecobee Program: ", type: "enum", required: true, multiple:false, description: "Tap to choose...", options: programs, submitOnChange: true)
    	       	    input(name: "fanMode", title: "Select a Fan Mode to use\n(Optional) ", type: "enum", required: false, multiple: false, description: "Tap to choose...", metadata:[values:["On", "Auto", "default"]], submitOnChange: true)
        	       	if(settings.whichProgram != "Resume Program") input(name: "holdType", title: "Select the Hold Type to use\n(Optional) ", type: "enum", required: false, multiple: false, description: "Tap to choose...", metadata:[values:["Until I Change", "Until Next Program", "default"]], submitOnChange: true)
            	   	input(name: "useSunriseSunset", title: "Also at Sunrise or Sunset?\n(Optional) ", type: "enum", required: false, multiple: true, description: "Tap to choose...", metadata:[values:["Sunrise", "Sunset"]], submitOnChange: true)                
                }
            } // End if myThermostats size
            section(title: "Temporarily Disable?") {
            	input(name: "tempDisable", title: "Temporarily Disable Handler? ", type: "bool", required: false, description: "", submitOnChange: true)                
        	}
        
        section (getVersionLabel())
    }
}


// Main functions
def installed() {
	LOG("installed() entered", 5)
	initialize()  
}

def updated() {
	LOG("updated() entered", 5)
	unsubscribe()
    initialize()
	
}

def initialize() {
	LOG("initialize() entered")
    if(tempDisable == true) {
    	LOG("Temporarily Disabled as per request.", 2, null, "warn")
    	return true
    }
	
	if (settings.modeOrRoutine == "Routine") {
    	subscribe(location, "routineExecuted", changeProgramHandler)
    } else {
    	subscribe(location, "mode", changeProgramHandler)
    }	   
   
    if(useSunriseSunset?.size() > 0) {
		// Setup subscriptions for sunrise and/or sunset as well
        if( useSunriseSunset.contains("Sunrise") ) subscribe(location, "sunrise", changeProgramHandler)
        if( useSunriseSunset.contains("Sunset") ) subscribe(location, "sunset", changeProgramHandler)
    }
    
	// Normalize settings data
    normalizeSettings()
    LOG("initialize() exiting")
}

// get the combined set of Ecobee Programs applicable for these thermostats
private def getEcobeePrograms() {
	def programs

	if (myThermostats?.size() > 0) {
		myThermostats.each { stat ->
        	def DNI = stat.device.deviceNetworkId
            LOG("Getting list of programs for stat (${stat}) with DNI (${DNI})", 4)
        	if (!programs) {
            	LOG("No programs yet, adding to the list", 5)
                programs = parent.getAvailablePrograms(stat)
            } else {
            	LOG("Already have some programs, need to create the set of overlapping", 5)
                programs = programs.intersect(parent.getAvailablePrograms(stat))
            }
        }
	} 
    LOG("getEcobeePrograms: returning ${programs}", 4)
    return programs
}

private def normalizeSettings() {
	// whichProgram
	state.programParam = ""
	if (whichProgram != null && whichProgram != "") {
    	if (whichProgram == "Resume Program") {
        	state.doResumeProgram = true
        } else {        	
    		state.programParam = whichProgram
    	}
	}
    
    // fanMode
    state.fanCommand = ""
    if (fanMode != null && fanMode != "") {
    	if (fanMode == "On") {
        	state.fanCommand = "fanOn"
        } else if (fanMode == "Auto") {
        	state.fanCommand = "fanAuto"
        } else {
        	state.fanCommand = ""
        }
    }
    
    // holdType
    state.holdTypeParam = null
    if (holdType != null && holdType != "") {
    	if (holdType == "Until I Change") {
        	state.holdTypeParam = "indefinite"
        } else if (holdType == "Until Next Program") {
        	state.holdTypeParam = "nextTransition"
        } else {
        	state.holdTypeParam = null
        }
    }
    
	if (settings.modeOrRoutine == "Routine") {
    	state.expectedEvent = settings.action
    } else {
    	state.expectedEvent = settings.modes
    }
	LOG("state.expectedEvent set to ${state.expectedEvent}", 4)

}

def changeProgramHandler(evt) {
	LOG("changeProgramHander() entered with evt: ${evt}", 5)
	
    def gotEvent 
    if (settings.modeOrRoutine == "Routine") {
    	gotEvent = evt.displayName?.toLowerCase()
    } else {
    	gotEvent = evt.value?.toLowerCase()
    }
    LOG("Event name received (in lowercase): ${gotEvent}  and current expected: ${state.expectedEvent}", 5)

    if ( !state.expectedEvent*.toLowerCase().contains(gotEvent) ) {
    	LOG("Received an mode/routine that we aren't watching. Nothing to do.", 4)
        return true
    }
    
    settings.myThermostats.each { stat ->
    	LOG("In each loop: Working on stat: ${stat}", 4, null, "trace")
    	// First let's change the Thermostat Program
        if(state.doResumeProgram == true) {
        	LOG("Resuming Program for ${stat}", 4, null, "trace")
        	stat.resumeProgram()
        } else {
        	LOG("Setting Thermostat Program to programParam: ${state.programParam} and holdType: ${state.holdTypeParam}", 4, null, "trace")
        	stat.setThermostatProgram(state.programParam, state.holdTypeParam)
		}
        if (state.fanCommand != "" && state.fanCommand != null) stat."${state.fanCommand}"()
    }
    return true
}



// Helper Functions
private def LOG(message, level=3, child=null, logType="debug", event=true, displayEvent=true) {
	message = "${app.label} ${message}"
	parent.LOG(message, level, child, logType, event, displayEvent)
}
