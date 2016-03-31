/**
 *  Bathroom Light Controller
 *
 *  Copyright 2015 Alex Guyot
 *
 * TESTING
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
 *
 * TO SMARTTHINGS SUPPORT AGENTS:
 *
 * Notes on the basic functionality and layout of the code in this SmartApp can be found below
 * in case any fellow SmartThings Support agents feel like taking a look at my code as an example
 * to get a better sense of what the SmartApps that they might have to deal with are doing.
 * 
 * Any notes on the basic details will be in the /* comment form, while standard documentation will be
 * in the single line // comment format.
*/

/*
 * The definition section defines the settings that can be found by clicking "App Settings" on a SmartApp in the IDE
 * the reason the values in App Settings are sometimes unchangeable is beacuse they are explicitly defined in the code
 * and thus can only be manipulated by chagning their values in the definition section of the SmartApp
*/
definition(
    name: "Bathroom Light Controller",
    namespace: "guyot",
    author: "Alex Guyot",
    description: "Bathroom light turns on when both doors are closed, off ten minutes later or when one or both doors are opened.",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")

/*
 * the preferences section defines the pages that users see when initially setting the
 * SmartApp up in the SmartThings mobile app
*/
preferences {
    /* 
     * this first section prompts the user to choose which contact sensors they want to monitor
     * for this app
    */
    section ("Choose Contact Sensors") {
    	paragraph "When these doors are all closed, turn on the light. When at least one of them is open, turn off the light."
        input "contacts", "capability.contactSensor", multiple: true, required: true // required: true means they must pick at least one
    }
    /*
     * next they choose which interior motion sensors (if any) they want to use as a failsafe
     * to determine that someone is inside
    */
    section ("Choose Motion Sensors") {
    	paragraph "When all of the above doors are closed, these optional interior motion sensors can keep the light on only if someone is inside."
        paragraph "If no sensors are chosen, the light will go off after 7 minutes of no motion when all doors are closed."
        input "motions", "capability.motionSensor", multiple: true, required: false // required: false means they don't have to pick any
    	paragraph "Are you controlling lights on the interior or exterior of your bathroom?"
        input "lightLocation", "enum", required: true, options: ["interior", "exterior"]
    }
    /* 
     * Finally, in this section the user chooses which lights the app should turn on based on the
     * contact and motion sensors
    */
    section ("Choose the Lights to be Controlled") {
    	paragraph "Choose one or more lights to turn on/off based on the contact and motion sensors chosen above"
        input "lights", "capability.switch", multiple: true, required: true
    }
}

/*
 * The installed() method is called when the SmartApp is first installed
 * (which is when the "Done" button is tapped after choosing values for the Preferences above)
*/
def installed() {
	/*
	 * log.debug lines are for debugging only and don't affect the app's behavior
	*/
    log.debug "Installed with settings: ${settings}"
	initialize() /* this is calling the initialize() method, which you can find below */
}

/*
 * The initialize() method subscribes to the events of all of the devices chosen in the Preferences
 * subscribing to events means that every time one of the subscribed-to devices registers an event,
 * this SmartApp gets notified and can decide if it needs to do something based on that event
*/
def initialize() {
	subscribe(contacts, "contact", contactHandler) // subscribe to all contact events from the chosen contact sensors
    subscribe(motions, "motion", motionHandler) // subscribe to all motion events from the chosen motion sensors
	state.timeClosed = now()
}

/*
 * The updated() method gets called when changes are made to the device's Preferences
 * bascially it just unsubscribes from the old set of devices and subscribes to the new set of devices
*/
def updated() {
	log.debug "Updated with settings: ${settings}"
	unsubscribe() // built-in method, unsubscribes the SmartApp from all currently subscribed-to devices
	initialize() // calls the initialize() method again to resubscribe to the new set of devices
}

// Event Handlers

/*
 * Event handlers are passed the event itself, these is where all the action occurs
 * something in the real world happens to trigger a device, the device sends an event, its event handler
 * is notified of that event and can run code to act on it
*/

// handler to deal with contact sensor events
def contactHandler(evt) {
	/*
     * the event from the contact handler is passed into this method as the variable "evt"
     * calling "evt.value" will return the value stored in the event,
     * which will either be "open" or "closed"
    */
    log.debug "$evt.value"
    if (evt.value == "open") { // if at least one door is open
        if (lightLocation == "interior") {
            runIn(5000, lights.off)
        } else {
            lights.off() // turn off the lights
        }
    } else {
    	log.debug "unscheduling"
        // unschedule any events (in this SmartApp those would only be timers to turn off the lights)
    	unschedule()
    	// if the event value was closed, check whether all of the doors are closed
	    def openContacts = contacts.findAll {
    	    it.currentValue("contact") == "open"
            /* 
             * This is Groovy closure. It's weird, but what it's doing is looking through every
             * contact sensor and finding all sensors with a current value of "open" and saving
             * them into the openContacts list.
            */
    	}
        if (openContacts.size() == 0) {
            if (lightLocation == "interior") {
                state.timeClosed = now()
            	// lights were already turned on by the motion sensor, no need to duplicate effort
            	//checkForPresence()
            } else {
                state.timeClosed = now()
	        	lights.on()
                //checkForPresence()
            }
        }
    }
}

// handler to deal with motion sensor events
def motionHandler(evt) {
    log.debug "$evt.value"
    /*
     * This handler will factor in motion sensors as a failsafe for when all doors are closed despite
     * no one being in the bathroom. A moment after all doors have closed, we will query the motion
     * sensors. If there is no motion, it is highly likely that no one is in the bathroom, so we can
     * turn the lights off. If there is motion, then someone is in the bathroom. Since there's no way
     * for that person to leave without opening the doors, we will keep the lights on indefinitely until
     * a door opens.
    */
    if (evt.value == "active") { // if the motion sensor becomes activated
        def openContacts = contacts.findAll {
    	    it.currentValue("contact") == "open"
	    }
        if (lightLocation == "interior") {
        	lights.on() // turn on the lights
            if (openContacts.size == 0) {
                log.debug "unscheduling"
                // unschedule any events (in this SmartApp those would only be timers to turn off the lights)
                unschedule()
            }
            /*if (openContacts.size() > 0) {
            	runIn(60*3, lightsOut)
            } else {
            	log.debug "unscheduling"
        		// unschedule any events (in this SmartApp those would only be timers to turn off the lights)
    			unschedule()
        	}*/
        } else {
            if (openContacts.size() == 0) {
            	lights.on() // turn on the lights
                log.debug "unscheduling"
        		// unschedule any events (in this SmartApp those would only be timers to turn off the lights)
    			unschedule()
            }
        }
    } else if (evt.value == "inactive") {
        log.debug "$state.timeClosed"
    }
}

// handler to deal with light events
def timeoutHandler(evt) {
    // execute handler in ten minutes from now
    runIn(60*10, switchHandler)
}

def switchHandler() {
    switch1.off()
}

// Misc Methods

/*
 * Other methods which are not handlers go below here. This is just my personal coding style.
*/

// method to query the motion sensor to make sure someone is inside when doors are closed
/*def checkForPresence() {
	log.debug "checking for presence"
    runIn(14, offIfEmpty)
}

def offIfEmpty() {
	log.debug("offIfEmpty triggered")
    def activeMotions = motions.findAll {
    	it.currentValue("motion") == "active"
    }
    if (activeMotions.size() > 0) {
    	log.debug "more than 0 active motion sensors"
        return
    } else {
    	log.debug "0 active motion sensors"
        runIn(15, lightsOut)
    	return
    }
}

def lightsOut() {
	log.debug "lights out"
	lights.off()
}*/