/**
 *  Goodnight SmartHome
 *
 *  Copyright 2015 ChrisB
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
    name: "Goodnight SmartHome",
    namespace: "sirhcb",
    author: "chrisb",
    description: "An app to prepare to night time.  This app, triggered by a virtual switch, will do a safety check of your home, verifying by voice announcement if any selected doors or windows have been left open.  The app will optionally turn on select lights (such as hallway lights leading to bedrooms) and optionally turn off select lights after a given time delay.  The app will also optionally run a routine (such as goodnight) after a given delay.",
    category: "Safety & Security",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
	page(name: "pageMain")
	page(name: "pageMisc")
    page(name: "pageDoorWin")
    page(name: "pageLightsOn")
    page(name: "pageLightsOff")
    page(name: "pageRoutine")
}

// Note that above my preferences are just a bunch of pages.  We'll uses the pages (see below) to get
// the information that we need to run the app.  The first page listed above is my main page and 
// automatically comes up when I start to install the app.

def pageMain() {
	dynamicPage(name: "pageMain", uninstall: true, install: true) {
		// ****NOTE: You **HAVE** top have install: true on the last page you are using.  If you
        // don't have this, the program will never install.  While this is my first page, it will
        // call each addtional page and then bounce back here, so this is also my last page.
		section() {
        	paragraph "Goodnight SmartHome creates a virtual on/off button tile that it uses as a trigger."
            paragraph "To run the app, turn that device on either by manually tapping it in the mobile app, or tying it to a remote such as the AEON Minimote, or trigger it via voice command with Amazon Echo or similar voice interfaces."
        }    
      	section("Setup Menu") {
        	paragraph "Select each line below to setup individual settings in each area."
            href "pageMisc", title:"Setup Button and Speaker...", description:"Tap here to open page"
            href "pageDoorWin", title:"Select Which Doors and Windows to check...", description:"Tap here to open page"
            href "pageLightsOn", title:"Optional: Select which Lights to turn on...", description:"Tap here to open page"
            href "pageLightsOff", title:"Optional: Select which Light to turn off...", description:"Tap here to open page"
            href "pageRoutine", title:"Optional: Choose a Routine to Run...", description:"Tap here to open page"
        }

	    section("SmartApp Name") {
            label title:"Assign a name", required:false
        }
	}
}

def pageMisc() {
    dynamicPage(name: "pageMisc", title: "Setup button name and TTS device.", uninstall: true) {
    	// You can put uninstall: false here as well to prevent users from uninstalling from this
        // (and other 'subpages' but I'm leaving it as true.  See also that I do NOT have 
        // "install: true" here.  That way when the user hit DONE after editing this page, it 
        // will bounce back to the mainpage.  This is true for the other 'subpages' as well.
		section("Button Name") {
        	paragraph "Enter a name for the button that will act as the trigger for this app. Choose something friendly and short so it's easy to remember or say."
			input "buttonName", "text", title: "Enter a friendly name.", required: true
		}
        section("Text-to-Speach Device") {
        	paragraph "Next we'll need to know what device you're going to be using as your speaker."      
			input "TTspeaker", "capability.speechSynthesis", title: "Select your TTS Speaker", required: true
	    }
	}
}

def pageLightsOn() {
    dynamicPage(name: "pageLightsOn", title: "Pick your switches...", uninstall: true) {
    	section("Lights to turn on.") {
        	paragraph "Select any lights you want to turn on when this app runs. For example maybe a hallway light leading to a bedroom."
            input "onSwitches", "capability.switch", title: "Turn on which lights?", multiple: true, required: false
			paragraph "Note: If you want any of these lights to turn off automatically later, be sure to include them in the next section that turns lights off."
		}
	}
}

def pageLightsOff() {
    dynamicPage(name: "pageLightsOff", title: "Pick your switches...", uninstall: true) {
    	section("Lights to turn off.") {
        	paragraph "Select any lights you want to turn off when this app runs."
			input "theSwitches", "capability.switch", title: "Turn off which lights?", multiple: true, required: false
			paragraph "You will also need to indicate how many minutes after the app starts that these lights should turn off."
			input "minutes", "number", title: "After how many minutes?", required: false
            paragraph "If you don't enter a number the app will assume you want to turn them off immediately."
		}
	}
}

def pageDoorWin() {
	dynamicPage(name: "pageDoorWin", uninstall: true) {
		section("Doors and Windows."){
        	paragraph "Select all the windows and doors that you want to monitor. SmartThings will read off the names of any selected sensors that are left open when this app runs."
			input "doors", "capability.contactSensor", title: "Which doors/windows?", multiple: true
        }
	}
}

def pageRoutine() {    
    dynamicPage(name: "pageRoutine", title: "Pick a Routine to run.", uninstall: true) {        
        def phrases = location.helloHome?.getPhrases()*.label
    	section("Routine.") {
        	paragraph	"If you'd like to select a Routine to run, enter it here."
        	input "phrase2", "enum", title: "Phrase", required: false, options: phrases
		}
        section("When to schedule to Routine.") {
        	input "minutes2", "number", title: "After how many minutes?", required: false
		paragraph "If you don't enter a number the app will assume you want to run the Routine immediately."
		}
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
	// I need to get the name that the user wanted to call the trigger button, but because I'm just
    // loading the program it isn't really a thing I can work with here.  However, the names does
    // exist in the array of settings that is created when a program first tries to install, so:
	def buttonLabel = settings.find {it.key == "buttonName"}
    log.debug "${buttonLabel}"   
    // Find, from settings areay, the record that has buttonName in it.  Use the key from that record
    // and create a new variable 'buttonLabel' equal to that key.  The key in this case is the name
    // the user entered for the trigger button.
    
    def DeviceID = app.id+"/gnsh"
    log.debug "did = ${DeviceID}"
    // DeviceID is a variable that I'm creating here to give an ID to my childDevice that I'm going
    // to create in a moment. app.id is just the ID for each instance of this app.  I'm using this
    // to make sure it's different than any other Device ID, even if this app is installed multiple
    // times by a user.  the "/gnsh" just stands for Good Night Smart Home.  This extra add on isn't
    // strickly necessary here as this app only generates one childDevice, but if you had more you
    // would add on different endings to make sure each device ID was still unique.
	
	def existingDevice = getChildDevice(DeviceID)
    log.debug "EXD = ${existingDevice}"
    if(!existingDevice) {
    // Because we won't want multiple childDevices, I'm testing here to see if a childDevice with
    // this DeviceID already exists.  The If statement there basically asks: If there is no device
    // with that device ID, then do the next step:  
                def trigger = addChildDevice("sirhcb", "On/Off Button Tile", app.id+"/gnsh", null, [name:buttonLabel.value, label:buttonLabel.value])
	// create the child device.  The child device needs some info here.
    //		First: Namespace... generally make this the same as the name space for your app.
    //		Second: The device type.  You NEED to have this device type in your namespace.  If you're
    //				using a SmartThigns default device type, make a copy of it in your device type area.
    //		Third: The unique Device ID.  Same as what we did above.
    //		Fourth: The Hub... this apparently can be left null and it'll use whatever Hub the user is
    //				working on/with.
    //		Finally we need detials like name, label.  Name probably isn't requred. 
    } 
	// Okay, we created the childDevice, now what?
	// For reasons unknown to me, you can't apparently subscribe directly to the child device, so we
    // cheat and make a virtual device of our virtual device.
	def myDevice = getChildDevice(DeviceID)
    myDevice.name = buttonLabel
    log.debug "MyD = ${myDevice}"
    log.debug "MyD.name = ${myDevice.name}"
	// So we created myDevice to essentially be the in app equivalent of the childDevice.  Now we
    // subscribe to this device.
	subscribe(myDevice, "switch.on", switchOnHandler) 
}

def switchOnHandler(evt) {
    log.debug "trigger turned on!"

	//When SmartThings sees the switch turned on, then we need to do five things:
    //   1. Turn on the lights that the user wanted on, if any.
    //   2. Turn off the lights that the user wanted off, if any, at the given time.
    //   3. Run the routine that the user selected, if any, at the given time.
    //   4. Turn off our trigger device.
    //   5. Build the phrase of the open sensors and send it to the TTS speaker.
    
    // Part 1. Turn on light switches
    if (onSwitches) {												// If the user entered switch to be turned on then...
    	onSwitches.on()	    										// turn them on!
	}																// This happens immediately.

    // Part 2. Setup to turn lights off.
    if (theSwitches) {												// If there are switch to turn, then...
    	if ((minutes) == null) {									// If the user never entered a number for minute then..
        	def minutes = 0											// set minutes to 0.
        }    
	    def timeDelay = minutes * 60								// convert minutes to seconds.
    	runIn (timeDelay, lightsOut)								// schedule the lights out procedure
    }
    
    // Part 3. Run the routine.
    if (phrase2) {													// If the user selected to say a phrase
        if ((minutes2) == null) {									// If the user never entered a number for minute then..
        	def minutes2 = 0											// set minutes to 0.
        }    
	    def timeDelay2 = minutes * 60								// convert minutes to seconds.
    	runIn (timeDelay2, runRoutine)								// schedule the lights out procedure         
    }

	// Part 4. Turn off the trigger.
    //		I figured this would be as simple as saying myDevice.off(), but apparently when we
    //		created myDevice in the initialize() area above, that didn't carry through to this
    //		procedure, so we're redoing a number of steps here to rebuild myDevice.
	def buttonLabel = settings.find {it.key == "buttonName"}
    log.debug "${buttonLabel}"
    def DeviceID = app.id+"/gnsh"
	log.debug "did = ${DeviceID}"
//	def existingDevice = getChildDevice(DeviceID)
//  log.debug "EXD = ${existingDevice}"
	def myDevice = getChildDevice(DeviceID)
    myDevice.name = buttonLabel
    log.debug "MyD = ${myDevice}"
    log.debug "MyD.name = ${myDevice.name}"
    myDevice.off()												// And now we can turn off the trigger.

    // Part 5. Build and send the phrase.
	def phrase = ""													// Make sure Phrase is empty at the start of each run.

    doors.each { doorOpen ->										// cycles through all contact sensor devices selected
    	if (doorOpen.currentContact == "open") {					// if the current selected device is open, then:
            log.debug "$doorOpen.displayName"						// echo to the log the device's name           
            phrase = phrase.replaceAll(' and ', ' ')				// Remove any previously added "and's" to make it sound natural.

			if (phrase == "") {										// If Phrase is empty (ie, this is the first name to be added)...
            	phrase = "The " + doorOpen.displayName 				// ...then add "The " plus the device name.
			} else {												// If Phrase isn't empty...
            	phrase = phrase + ", and the " + doorOpen.displayName		// ...then add ", and the ".
			}														// and finally cycle to the next device.

            log.debug "${phrase}"									// Echo the current version of 'Phrase'            
        }															// Closes the IF statement.  ie: We're done with this device that was open.
    }    															// Closes the doors.each cycle.  ie: We're done looking at all devices now.

	// Now let's tidy up the phrase we're going to have our speaker say:   
    if (phrase == "") {												// If the phrase is empty (no open doors/windows were found),
    	phrase = "The house is ready for night."					// then insert a phrase saying the house is ready.
    	}
    else {															// but if we did find at least one thing open, then...
    	phrase = "You have left " + phrase + "open"					// Add some language to make it sound like a natural sentence.
    }
    log.debug "${phrase}"											// Echo once more to the logs before sending to device.
    TTspeaker.speak(phrase)											// Send the phrase to the TTS device.
}																	// Close the switchOnHandler Process

// This is just a simple procedure that turns off the selected switches.  This procedure gets
// scheduled in Step 2 above if the user wanted to turn any switches off.
def lightsOut() {
	theSwitches.off()
}

// This is just a simple procedure that runs the Routine.  This procedure gets
// scheduled in Step 3 above if the user wanted to run a Routine.
def runRoutine() {
	location.helloHome?.execute(phrase2)							// ...execute the routine.
}

