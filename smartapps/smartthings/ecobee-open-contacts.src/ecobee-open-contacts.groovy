/**
 *  ecobee Open Contacts
 *
 *  Copyright 2016 Sean Kendall Schneyer
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
 
 /**
  * TODO: Add support for more than on/off such as programs
  */
def getVersionNum() { return "0.1.0" }
private def getVersionLabel() { return "ecobee Routines Version ${getVersionNum()}" }



definition(
	name: "ecobee Open Contacts",
	namespace: "smartthings",
	author: "Sean Kendall Schneyer (smartthings at linuxbox dot org)",
	description: "Support for changing ecobee runtime based on status of contact sensors",
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
	
		if (myThermostats?.size() > 0) {

			section(title: "Select Contact Sensors") {
				input(name: "contactSensors", title: "Contact Sensors: ", type: "capability.contactSensor", required: true, multiple: true, description: "")
			}
        
			section(title: "Timers") {
				input(name: "offDelay", title: "Delay time (in minutes) before turning off HVAC [Default=5]", type: "enum", required: true, metadata: [values: [2, 3, 4, 5, 10, 15, 30]], defaultValue: 5)
				input(name: "onDelay", title: "Delay time (in minutes) before turning HVAC back on [Default=0]", type: "enum", required: true, metadata: [values: [0, 1, 2, 3, 4, 5, 10, 15, 30]], defaultValue: 0)        	
	        }
		} // End if (myThermostats?.size() > 0)
	
// Saved from Routines SmartApp for now
/*        
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
            
*/

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
	unschedule()
	initialize()
}

def initialize() {
	LOG("initialize() entered")
	if(tempDisable == true) {
		LOG("Teporarily Disapabled as per request.", 2, null, "warn")
		return true
	}

	// TODO: Create an array of sensors for states?

	subscribe(contactSensors, "contact.open", sensorOpened)
	subscribe(contactSensors, "contact.closed", sensorClosed)
	
	// Normalize settings data
	// normalizeSettings()
	buildSensorStateArray()
	LOG("initialize() exiting")
}

private buildSensorStateArray() {
	// Create an array for all of the sensors and their current states
	def tempSensorStateArray = [:]
	
}

def sensorOpened(evt) {
	// A sensor (door/window) was opened
	LOG("sensorOpened() entered with evt: ${evt}", 5)
	
	def gotEvent = evt.value?.toLowerCase()
	LOG("Event name received (in lowercase): ${gotEvent}  and current expected: ${state.expectedEvent}", 5)
}

def sensorClosed(evt) {
	// A sensor (door/window) was closed
	LOG("sensorClosed() entered with evt: ${evt}", 5)
	
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
