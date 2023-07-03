/**
 *  Turn Off
 *
 *  Copyright 2017 Trevor
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
    name: "Turn Off",
    namespace: "TEAPPS",
    author: "Trevor",
    description: "Turn off a light after its been turned on.",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
	    
    section("Lights") {
    	input "lights", "capability.switch", required: true, title: "Which lights to turn off after being turned on?", multiple: true
        input "offsetHours", "number", title: "How long until the light is turned off (hours)?"
        input "offsetMinutes", "number", title: "How long until the light is turned off (minutes)?"
        input "offsetSeconds", "number", title: "How long until the light is turned off (seconds)?"
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
	// TODO: subscribe to attributes, devices, locations, etc.
    
    //Subscribe to the light device being turned on
    subscribe(lights, "switch.on", lightHandler)
    

}

// TODO: implement event handlers
def lightHandler(evt) {

  if("on" == evt.value)
    //Light was turned on
    log.debug "Light is in ${evt.value} state"
    
    //Get the date and time
    def currentTime = new Date()

    //Calculate the offset
    def timeToTurnOff = new Date(currentTime.time + (offsetHours * 60 * 60 * 1000) + (offsetMinutes * 60 * 1000) + (offsetSeconds * 1000))

    log.debug "Scheduling for: $timeToTurnOff"

    //Schedule this to run one time
    runOnce(timeToTurnOff, turnOff)
}

def turnOff() {
    log.debug "turning off lights"
    lights.off()
}