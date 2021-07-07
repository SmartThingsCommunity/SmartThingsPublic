/**
 *  Smart Basement
 *
 *  Copyright 2019 Paul Smith
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
    name: "Smart Basement",
    namespace: "paulsmithkc",
    author: "Paul Smith",
    description: "Tired of setting up multiple routines, scenes, and apps to configure your smart home? Use this app to easily make your basement smart.\r\n\r\nIntegrated Automations\r\n* Turn on/off all basement lights from a single switch.\r\n* Turn on the basement lights when the basement door opens.\r\n* Turn on the basement lights when motion is detected on the stairs.\r\n* Turn the basement lights off after X amount of time.",
    category: "Mode Magic",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/smartlights.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/smartlights@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/smartlights@2x.png")


preferences {
    page(name: "page1", install: true, uninstall: true)
}

def page1() {
	dynamicPage(name: "page1") {
        def installed = (app.installationState == "COMPLETE");

        section("Setup") {
            input "lightSwitch", "capability.switch", multiple: true, required: false, title: "What switch(s) controls the lights?"
            input "lights", "capability.switch", multiple: true, required: false, title: "Which lights do you want to control with this app?"
            input "doorOpen", "capability.contactSensor", multiple: true, required: false, title: "Whould you like the lights to turn on when the door is opened?"
            input "motion", "capability.motionSensor", multiple: true, required: false, title: "Whould you like the lights to turn on when motion is detected?"
            input "timeout", "number", required: false, title: "How many minutes do you want to wait to turn the lights off, when nothing is happening?"
        }
        section(mobileOnly: true, "Name") {
			label title: "Do you want to name this app?", required: false
		}
    }
}

def installed() {
	log.debug("Installed with settings: ${settings}");
	initialize();
}

def updated() {
	log.debug("Updated with settings: ${settings}");
	unsubscribe();
	initialize();
}

def initialize() {
	setLastActivated("on")

    // Lights and Switches
    if (lightSwitch) {
        subscribe(lightSwitch, "switch", switchHandler);
        subscribe(lightSwitch, "level", dimHandler);
    }
    
    // Door sensors
    if (doorOpen) {
		subscribe(doorOpen, "contact.open", doorOpenHandler);
    }
    
    // Motion Sensors
    if (motion) {
    	subscribe(motion, "motion", motionHandler);
    }
}

def setLastActivated(value) {
	state.lastActivated = now();
    if (timeout && value == "on") {
        runIn(60 * timeout, timeoutHandler)
    }
}

def timeoutHandler() {
    if (motion) {
        // Don't timeout if there is still motion detected
    	def motionState = motion.currentState("motion");
        if (motionState.value == "active") { return; }
    }
    
    def elapsed = now() - state.lastActivatedIndoor;
    def timeout = (indoorTimeout - 1) * 60 * 1000;
    if (elapsed >= timeout) {
        log.debug("timeout");
        lights?.off()
        state.lastActivated = now();
    }
}

def switchHandler(evt) {
    if (evt.value == "on") {
        log.debug("light switch turned on")
    	lights?.on()
    } else if (evt.value == "off") {
    	log.debug("light switch turned off")
    	lights?.off()
    }
    setLastActivatedIndoor(evt.value)
}

def dimHandler(evt) {
    log.debug("light dimmer: $evt.value")
    lights?.setLevel(evt.value)
    setLastActivated("on")
}

def doorOpenHandler(evt) {
    log.debug("door open detected")
    lights?.on()
    setLastActivated("on")
}

def motionHandler(evt) {
    if (evt.value == "active") {
        log.debug("indoor motion detected")
        lights?.on()
        setLastActivated("on")
    }
}
