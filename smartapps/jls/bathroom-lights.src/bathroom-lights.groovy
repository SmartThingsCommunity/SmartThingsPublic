/**
 *  Bathroom Lights
 *
 *  Copyright 2015 Jesse Silverberg
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
    name: "Bathroom Lights",
    namespace: "JLS",
    author: "Jesse Silverberg",
    description: "A specialized bathroom lighting automation.  Turns lights on with motion, dims after a custom period of time to give the occupant a warning that lights will turn off soon, and then turns off the lights unless motion is detected again.  Motion brightens the lights back up.  Perfect for showering so you don't get left in the dark, and solves the problem of motion detection during lights-off dimming transitions.",
    category: "Convenience",
    iconUrl: "https://www.dropbox.com/s/6kxtd2v5reggonq/lightswitch.gif?raw=1",
    iconX2Url: "https://www.dropbox.com/s/6kxtd2v5reggonq/lightswitch.gif?raw=1",
    iconX3Url: "https://www.dropbox.com/s/6kxtd2v5reggonq/lightswitch.gif?raw=1")


preferences {

	section("Select devices") {
		input "lights", "capability.switchLevel", title:"Bathroom lights", multiple: true  , required: true      
		input "motionSensor", "capability.motionSensor", title: "Bathroom motion sensor(s)", multiple: true, required: true
    }
    section("Brightness and timing options") {
        input "dimLevelHi", "number", title: "Brighten lights to this level (0-100)...", required: true
        input "brightenTimeHi", "number", title: "Hold lights here for this long (minutes)...", required: true
        input "dimLevelLow", "number", title: "Then dim lights to this level (0-100)...", required: true
    	input "brightenTimeLow", "number", title: "And turn off after this long (minutes)...", required: true
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
    subscribe(motionSensor, "motion.active", motionDetectedHandler)
    subscribe(motionSensor, "motion.inactive", motionStoppedHandler)
}

def motionDetectedHandler(evt) {
	log.debug "motionDetectedHandler called: $evt"
    unschedule()
    lights.setLevel(dimLevelHi)
}

def motionStoppedHandler(evt) {
	log.debug "motionStoppedHandler called: $evt"
    
    def noMotion = motionSensor.find{it.currentMotion == "active"} == null
    if (noMotion) {
    	log.debug "motionStoppedHandler found no motion on all sensors"
    	log.debug "Scheduling lights to dim and turn off"
    	runIn(60 * brightenTimeHi, dimLights)
    	runIn(60 * (brightenTimeHi + brightenTimeLow), offLights)
    }
}

def dimLights() {
    log.debug "Dimming lights $lights to $dimLeveLow"
	lights.setLevel(dimLevelLow)
}

def offLights() {
	log.debug "Turning off lights $lights"
	lights.off()
}

