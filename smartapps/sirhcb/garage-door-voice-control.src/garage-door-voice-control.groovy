/**
 *  Garage Door Voice Control
 *
 *  Copyright 2015 chrisb
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
    name: "Garage Door Voice Control",
    namespace: "sirhcb",
    author: "chrisb",
    description: "An App designed to use with Echo IFTTT triggers to give more natural voice controls.",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png"
)

preferences {
	page(name: "pageMain")
	page(name: "pageMisc")
    page(name: "pageInstruct")
    page(name: "pageGarage")
    page(name: "pageNotify")
}

// Note that above my preferences are just a bunch of pages.  We'll uses the pages (see below) to get
// the information that we need to run the app.  The first page listed above is my main page and 
// automatically comes up when I start to install the app.

def pageMain() {
	dynamicPage(name: "pageMain", uninstall: true, install: true) {
		// ****NOTE: You **HAVE** to have install: true on the last page you are using.  If you
        // don't have this, the program will never install.  While this is my first page, it will
        // call each addtional page and then bounce back here, so this is also my last page.
		section() {
        	paragraph "Garage Door Voice Control is designed to work with Alexa and IFTTT trigger controls."
            href "pageInstruct", title:"Instructions", description:"Tap here to see full instructions."  
        }    
      	section("Setup Menu") {
        	paragraph "Select each line below to setup individual settings in each area."
            
            href "pageMisc", title:"Create the Virtual Button.", description:"Tap here to open page."
            href "pageGarage", title:"Enter Garage information.", description:"Tap here to open page."
            href "pageNotify", title:"Select how to receive notifications.", description:"Tap here to open page."
        }

	    section("SmartApp Name") {
            label title:"Assign a name", required:false
        }
	}
}

def pageMisc() {
    dynamicPage(name: "pageMisc", title: "Name your Buttons.", uninstall: true) {
    	// You can put uninstall: false here as well to prevent users from uninstalling from this
        // (and other 'subpages') but I'm leaving it as true.  See also that I do NOT have 
        // "install: true" here.  That way when the user hits DONE after editing this page, it 
        // will bounce back to the mainpage.  This is true for the other 'subpages' as well.
		section("Button Name") {
        	paragraph "Enter a name for the button that we'll turn on when we want to OPEN the garage door."
			input "buttonOpen", "text", title: "Enter a name for the open button.", required: true
            paragraph "Enter a name for the button that we'll turn on when we want to CLOSE the garage door."
			input "buttonClose", "text", title: "Enter a name for the close button.", required: true
		}
	}
}

def pageGarage() {
    dynamicPage(name: "pageGarage", title: "Enter Garage Information:", uninstall: true) {
    	section() {
        	paragraph "What Sensor monitors if your garage is open or closed?"
            input "door", "capability.contactSensor", title: "Sensor?", required: true
			paragraph "And what's the relay that you use to open/close the garage door?"
			input "outlet", "capability.switch", title: "Relay?"
		}
	}
}

def pageNotify() {
    dynamicPage(name: "pageNotify", title: "Notifications:", uninstall: true) {
    	section(){
        	paragraph "How would you like to receive notification about garage door status?"
        }
        section("Push Notification") {
        	input "pushYN", "bool", title: "Send a push notification?", required: false, defaultValue: false	
        }    
        section("Spoken notification") {
        	input "talkYN", "bool", title: "Speak after completing task?", required: false, defaultValue: false	
			input "TTspeaker", "capability.speechSynthesis", title: "Select your TTS Speaker", required: false
	    }
	}
}

def pageInstruct() {
	dynamicPage(name: "pageInstruct", uninstall: false) {
		section("Instructions:"){
        	paragraph "This app will create two virtual on/off button tiles.  You will give these names a little bit later."
            paragraph "After installing this app, open your IFTTT smart app and give IFTTT permission to access these new virtual devices."
            paragraph "Next, in IFTTT, setup a recipe that will use an Alexa voice command to turn the 'open' tile.  For example: 'the garage door open' could be your voice prompt.  You would then say to your echo: 'Alexa, trigger the garage door open.'"
            paragraph "Now create a similar recipe to turn on the 'close' tile."
			paragraph "That should do it!  Now when you cue Echo to open your garage door it will only do the action if the door is closed.  Likewise, if you tell Alexa to close the garage door it will only do it if the garage is open."
		}
		section("Other uses:") {
		    paragraph "As stated above, this idea is to use with this Amazon Echo, but because it could be setup with any IFTTT channel as input."
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

	def buttonOpenLabel = settings.find {it.key == "buttonOpen"}
    log.debug "${buttonOpenLabel}"   
    // Find, from settings areay, the record that has buttonName in it.  Use the key from that record
    // and create a new variable 'buttonLabel' equal to that key.  The key in this case is the name
    // the user entered for the trigger button.
    
    def DeviceID = app.id+"/open"
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
                def trigger = addChildDevice("sirhcb", "On/Off Button Tile", app.id+"/open", null, [name:buttonOpenLabel.value, label:buttonOpenLabel.value])
	// create the child device.  The child device needs some info here.
    //		First: Namespace... generally make this the same as the name space for your app.
    //		Second: The device type.  You NEED to have this device type in your namespace.  If you're
    //				using a SmartThigns default device type, make a copy of it in your device type area.
    //		Third: The unique Device ID.  Same as what we did above.
    //		Fourth: The Hub... this apparently can be left null and it'll use whatever Hub the user is
    //				working on/with.
    //		Finally we need details like name, label.  Name probably isn't requred. 
    } 
	// Okay, we created the childDevice, now what?
	// For reasons unknown to me, you can't apparently subscribe directly to the child device, so we
    // cheat and make a virtual device of our virtual device.
	def openDevice = getChildDevice(DeviceID)
    openDevice.name = buttonOpenLabel
    log.debug "MyO = ${openDevice}"
    log.debug "MyO.name = ${openDevice.name}"
	// So we created myDevice to essentially be the in app equivalent of the childDevice.  Now we
    // subscribe to this device.
	subscribe(openDevice, "switch.on", garageOpen) 

	def buttonCloseLabel = settings.find {it.key == "buttonClose"}
    log.debug "${buttonCloseLabel}"   
    // Find, from settings areay, the record that has buttonName in it.  Use the key from that record
    // and create a new variable 'buttonLabel' equal to that key.  The key in this case is the name
    // the user entered for the trigger button.
    
    // Now we're going to do the whole process again but for the close button.
    def DeviceIDC = app.id+"/close"
    log.debug "did = ${DeviceIDC}"
	def existingDeviceC = getChildDevice(DeviceIDC)
    log.debug "EXD = ${existingDeviceC}"
    if(!existingDeviceC) {
                def trigger = addChildDevice("sirhcb", "On/Off Button Tile", app.id+"/close", null, [name:buttonCloseLabel.value, label:buttonCloseLabel.value])
    } 
	def closeDevice = getChildDevice(DeviceIDC)
    closeDevice.name = buttonCloseLabel
    log.debug "MyC = ${closeDevice}"
    log.debug "MyC.name = ${closeDevice.name}"
	subscribe(closeDevice, "switch.on", garageClose) 
}

def switchesOff() {

	// This process is to turn off the trigger buttons.  We have to re-create the virtual devices
    // of the child devices like we did in the install process above.  We're not re-creating the
    // child devices so we don't have to do everything here.

	def buttonCloseLabel = settings.find {it.key == "buttonClose"}
    def DeviceIDC = app.id+"/close"
	def closeDevice = getChildDevice(DeviceIDC)
    closeDevice.name = buttonCloseLabel
    
 	def buttonOpenLabel = settings.find {it.key == "buttonOpen"}
    def DeviceID = app.id+"/open"
	def openDevice = getChildDevice(DeviceID)
    openDevice.name = buttonOpenLabel

	closeDevice.off()
    openDevice.off()
}


def garageClose(evt) {											// When close device is turned on...
	
    def phrase = ""												// Clear out the phrase
	if (door.currentContact == "closed") {						// If the garaged is closed, then...
        phrase = door.displayName + " is already closed."		// Set the phrase
        }														// but don't do anything else.
    else {														// If it is open, then...
		phrase = "Closing " + door.displayName					// Set the phrase
		outlet.on()												// And turn on the outlet.
    }
    log.debug "${phrase}"										// Echo once more to the logs before sending to device.
	if (talkYN) {												// If the user wanted TTS notification, then...
		TTspeaker.speak(phrase)									// Send the phrase to the TTS device.
	}
	if (pushYN) {												// If the user also requsted a push notification
    	sendPush("${phrase}")									// then send it here.
	}    
    runIn (10, switchesOff)										// In 10 seconds, turn off the child devices.
}

def garageOpen(evt) {											// Same process as above, but for the
																// open device...
    def phrase = ""
	if (door.currentContact == "open") {
        phrase = door.displayName + " is already open."
        }
    else {
        phrase = "Opening " + door.displayName
    	outlet.on()
    }
    log.debug "${phrase}"	
	if (talkYN) {
		TTspeaker.speak(phrase)
	}
	if (pushYN) {				
    	sendPush("${phrase}")	
	}

    runIn (10, switchesOff)
}