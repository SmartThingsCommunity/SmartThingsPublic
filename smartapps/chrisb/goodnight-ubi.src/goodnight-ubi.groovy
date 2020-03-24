/**
 *  Goodnight Ubi
 *
 *  Copyright 2014 Christopher Boerma
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
    name: "Goodnight Ubi",
    namespace: "chrisb",
    author: "chrisb",
    description: "An app to coordinate bedtime activities between Ubi and SmartThings.  This app will activate when a Virtual Tile is triggers (Setup custom behavior in Ubi to turn on this tile when you say goodnight to ubi).  This app will then turn off selected lights after a specified number of minutes.  It will also check if any doors or windows are open.  If they are, Ubi will tell you which ones are open.  Finally, the app will say goodnight to hello home if requested.",
    category: "Safety & Security",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
	    section("Enter Ubi information:") {
			input "behaviorToken", "text", title: "What is the Ubi Token?", required: true, autoCorrect:false
            	// Get token from the Ubi Portal.  Select HTTP request as trigger and token will be displayed.
            input "trigger", "capability.switch", title: "Which virtual tile is the trigger?", required: true
            	// Create a Virtual on/off button tile for this.
		}

		section("Which doors and windows should I check?"){
			input "doors", "capability.contactSensor", multiple: true
        }
        
		section("Which light switches will I be turning off?") {
			input "theSwitches", "capability.switch", Title: "Which?", multiple: true, required: false
            input "minutes", "number", Title: "After how many minutes?", required: true
		}
    	section("Should I say 'Goodnight' to Hello Home?") {
        	input "sayPhrase", "enum", metadata:[values:["Yes","No"]]
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
	subscribe(trigger, "switch.on", switchOnHandler)				// User should set up on Ubi that when the choosen
}    																// trigger is said, Ubi turns on this virtual switch.

def switchOnHandler(evt) {
    log.debug "trigger turned on!"
    
    def timeDelay = minutes * 60									// convert minutes to seconds.
    runIn (timeDelay, lightsOut)									// schedule the lights out procedure

	def phrase = ""													// Make sure Phrase is empty at the start of each run.

    doors.each { doorOpen ->										// cycles through all contact sensor devices selected
    	if (doorOpen.currentContact == "open") {					// if the current selected device is open, then:
            log.debug "$doorOpen.displayName"						// echo to the simulator the device's name
    		def toReplace = doorOpen.displayName					// make variable 'toReplace' = the devices name.
			def replaced = toReplace.replaceAll(' ', '%20')			// make variable 'replaced' = 'toReplace' with all the space changed to %20
			log.debug replaced										// echo to the simulator the new name.
            
            phrase = phrase.replaceAll('%20And%20', '%20')			// Remove any previously added "and's" to make it sound natural.

			if (phrase == "") {										// If Phrase is empty (ie, this is the first name to be added)...
            	phrase = "The%20" + replaced 						// ...then add "The%20" plus the device name.
			} else {												// If Phrase isn't empty...
            	phrase = phrase + ",%20And%20The%20" + replaced		// ...then add ",%20And%20The%20".
			}
            
            log.debug phrase  										// Echo the current version of 'Phrase'            
        }															// Closes the IF statement.
    }    															// Closes the doors.each cycle
    	
    if (phrase == "") {
    	phrase = "The%20house%20is%20ready%20for%20night."
    	}
    else {
    	phrase = "You%20have%20left%20" + phrase + "open"
    }
    
    httpGet("https://portal.theubi.com/webapi/behaviour?access_token=${behaviorToken}&variable=${phrase}")
          				// send the http request and push the device name (replaced) as the variable.
						// On the Ubi side you need to setup a custom behavior (which you've already done to get the token)
                        // and have say something like: "Hold on!  The ${variable} is open!"  Ubi will then take 'replaced'
                        // from this http request and insert it into the phrase that it says.
    	    
	if (sayPhrase == "Yes") {										// If the user selected to say Goodnight...
        location.helloHome.execute("Good Night!")					// ...say goodnight to Hello Home.
    }
}																	// Close the switchOnHandler Process

def lightsOut() {
	log.debug "Turning off trigger"
	trigger.off()													// Turn off the trigger tile button for next run
	if (theSwitches == "") {} else {								// If the user didn't enter any light to turn off, do nothing...
	    log.debug "Turning off switches"							// ...but if the user did enter lights, then turn them
		theSwitches.off()											// off here.
    }
}
