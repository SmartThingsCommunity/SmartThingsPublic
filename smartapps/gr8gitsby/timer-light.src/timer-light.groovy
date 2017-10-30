/**
 *  Timer Light
 *
 *  Copyright 2016 Kevin Hill
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
 
import groovy.time.*

definition(
    name: "Timer Light",
    namespace: "gr8gitsby",
    author: "Kevin Hill",
    description: "This puts outside lights on a timer",
    category: "Convenience",
    iconUrl: "https://source.unsplash.com/category/nature/60x60",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
	// Use a light swtich
	section("Light Switch") {
		// Capabilities link: http://docs.smartthings.com/en/latest/capabilities-reference.html
        // Input data types: http://docs.smartthings.com/en/latest/device-type-developers-guide/device-preferences.html?highlight=input#supported-input-types
        input "theSwitch", "capability.switch", title: "Pick your light switch"
        input "confirmationSwitch", "capability.switch", title: "Pick the notification switch"
        input "minutes", "number", title: "Enter the number of minutes you'd like the lights on"
        input "person", "capability.presenceSensor", title: "When who arrives?"
        
        // When arrival is detected, if it is dark, turn on the outside lights
	}
}

def installed() {
	log.debug "Installed with settings: ${settings}"
    subscribe(theSwitch, "switch", switchHandler)
    subscribe(confirmationSwitch, "switch", confirmationSwitchHandler)
	initialize()
}

def updated() {
	log.debug "Updated with settings: ${settings}"
	unsubscribe()
	initialize()
}

def initialize() {
	// This method sets up the app
    subscribe(person, "presence", presenceChange)
    state.Flashes = 4
    initialSunPosition()
}

def presenceChange(evt){
	log.debug("presenceChange")
    // if someone arrives at night the lights will turn on
    if("present" == evt.value){
    	timerLight()
    }
}

// Function to set lights on timer for person arriving
def timerLight() {
	log.debug(state.sunPosition)
	if(state.sunPosition == "down"){
    	light.on()
    	runIn(60 * minutes, turnOff)
    }
}

def turnOff(){
	// This method turns off the ligt
	theSwitch.off()
    confirmationSwitch.off()
}

def initialSunPosition() {  
	// This method determines if sun is down at time of initializtion and run sunsetHandler() if so

	def s = getSunriseAndSunset(zipCode: "98052")
	def now = new Date()
    def riseTime = s.sunrise
	def setTime = s.sunset

    if(setTime.before(now)) {
        sunsetHandler()
        log.info "Sun is already down, run sunsetHandler"
    }
    else 
    {	if (riseTime.after(now)) {
        sunriseHandler()
        log.info "Sun is up, run sunriseHandler"
        }
    } 
}

def sunriseHandler() {
	// method to set the sun position when the sun rises
    state.sunPosition = "up"
}

def sunsetHandler() {
	// method to set the sun position when the sunsets
    state.sunPosition = "down"
}

/*
def switchHandler(evt) {
	log.debug("switch Handler Fired")
}
*/
def confirmationSwitchHandler(evt) {
	state.Flashes = state.Flashes - 1
    log.debug("Flashes: ${state.Flashes}")
    log.debug("Event State: ${evt.value}")
	if(evt.value == "on" && state.Flashes > 0) {
    	runIn(2, turnOffConfirmationSwitch)
    } else if(evt.value == "off" && state.Flashes > 0) {
    	runIn(2, turnOnConfirmationSwitch)
    }
    
    if(state.Flashes == 0){
    	state.Flashes = 4
    }
}

def turnOnConfirmationSwitch(){
	confirmationSwitch.on()
}

def turnOffConfirmationSwitch(){
	confirmationSwitch.off()
}

def switchHandler(evt){
	log.debug("light Event handler fired")
	// use Event rather than DeviceState because we may be changing DeviceState to only store changed values
    def recentStates = theSwitch.eventsSince(new Date(now() - 4000), [all:true, max: 10]).findAll{it.name == "switch"}
    log.debug "${recentStates?.size()} STATES FOUND, LAST AT ${recentStates ? recentStates[0].dateCreated : ''}"
	//sendPush("Attempting to check light presses")

    if (evt.value == "on") {
        log.debug("==== ON ====")
    }

    log.debug(recentStates.size())
    //if (evt.isPhysical()) {
    
		if (recentStates.size() >= 2) {
        	 
            log.debug "Outside light timer mode engaged"
            def switchattr = confirmationSwitch.currentValue("switch")
            log.debug "confirmationSwitch: $switchattr"
            
            confirmationSwitch.on()
            
            //alternativeSwitch.off()
            //def message = "${location.name} executed ${settings.onPhrase} because ${evt.title} was tapped twice."
            runIn(60 * minutes, turnOff)
        } else {
        	log.trace "Skipping digital on/off event"
        }
    
	//log.debug("switch is on, turn off in $minutes")
    //runIn(60*minutes, turnOff)
    
	//}
}