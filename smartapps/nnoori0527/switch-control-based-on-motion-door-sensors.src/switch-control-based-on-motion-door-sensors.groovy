/**
 *  Switch control based on motion/door sensors
 *
 *  Copyright 2017 narges noori
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
    name: "Switch control based on motion/door sensors",
    namespace: "nnoori0527",
    author: "narges noori",
    description: "This application turns on the outlet if the entrace door opens (multipurpose sensor) and there is motion in front of the motion sensor for a user defined amount of time (say 5 minutes). If there is no activity in front of the motion sensor for a user defined amount of time (say 10 minutes) the outlet is turned off.",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    oauth: [displayName: "Switch/outlet control", displayLink: ""])




preferences {

	section ("Allow external service to control these things...") {
    	input "switches", "capability.switch", multiple: true, required: true
    }
    
    section("Turn on when motion detected after someone opens the door:") {
        input "motionSensor", "capability.motionSensor", required: true, title: "Where?"
    }
    
    section("Turn on when there's movement for:") {
        input "minutes", "number", required: true, title: "Minutes to turn on TV?"
    }
    section("Turn on when there's movement for:") {
        input "minutesInactive", "number", required: true, title: "Minutes to wait before turning off TV?"
    }
   
    
    section("Turn on when door open:") {
        input "doorOpenCloseDetector", "capability.contactSensor", required: true, title: "Where?"
    }

    section("Turn on this outlet") {
        input "tvOutlet", "capability.switch", required: true
    }
    
}

def installed() {
    initialize()
}

def updated() {
    unsubscribe()
    initialize()
}

def initialize() {
    subscribe(doorOpenCloseDetector, "contact.open", doorOpenedHandler)
    subscribe(motionSensor, "motion.inactive", motionStoppedHandler)
}

def doorOpenedHandler(evt){
	log.debug "doorOpenedHandler called: $evt"
    runIn(60 * minutes, checkMotion)
    //tvOutlet.on()
}

def checkMotion() {
    log.debug "In checkMotion scheduled method"

    def motionState = motionSensor.currentState("motion")

    if (motionState.value == "active") {
        // get the time elapsed between now and when the motion reported inactive
        def elapsed = now() - motionState.date.time

        // elapsed time is in milliseconds, so the threshold must be converted to milliseconds too
        def threshold = 1000 * 60 * minutes

        if (elapsed <= threshold) {
            log.debug "Motion has stayed active long enough since last check ($elapsed ms):  turning outlet on"
            //myswitch.on()
            tvOutlet.on()
        } else {
            log.debug "Motion has not stayed active long enough since last check ($elapsed ms):  doing nothing"
        }
    } else {
        // Motion active; just log it and do nothing
        log.debug "Motion is inactive, do nothing and wait for active"
    }
}

def motionStoppedHandler(evt) {
    log.debug "motionStoppedHandler called: $evt"
    runIn(60 * (minutesInactive+1), checkMotionTurnOff)
}

def checkMotionTurnOff() {
    log.debug "In checkMotionTurnOff scheduled method"

    def motionState = motionSensor.currentState("motion")

    if (motionState.value == "inactive") {
        // get the time elapsed between now and when the motion reported inactive
        def elapsed = now() - motionState.date.time

        // elapsed time is in milliseconds, so the threshold must be converted to milliseconds too
        def threshold = 1000 * 60 * minutesInactive

        if (elapsed >= threshold) {
            log.debug "Motion has stayed inactive long enough since last check ($elapsed ms):  turning switch off"
            tvOutlet.off()
        } else {
            log.debug "Motion has not stayed inactive long enough since last check ($elapsed ms):  doing nothing"
        }
    } else {
        // Motion active; just log it and do nothing
        log.debug "Motion is active, do nothing and wait for inactive"
    }
} 



mappings {
  path("/switches") {
    action: [
      GET: "listSwitches"
    ]
  }
  path("/switches/:command") {
    action: [
      PUT: "updateSwitches"
    ]
  }
}

// returns a list like
// [[name: "kitchen lamp", value: "off"], [name: "bathroom", value: "on"]]
def listSwitches() {

    def resp = []
    switches.each {
        resp << [name: it.displayName, value: it.currentValue("switch")]
    }
    return resp
}

void updateSwitches() {
    // use the built-in request object to get the command parameter
    def command = params.command

    // all switches have the comand
    // execute the command on all switches
    // (note we can do this on the array - the command will be invoked on every element
    switch(command) {
        case "on":
            switches.on()
            break
        case "off":
            switches.off()
            break
        default:
            httpError(400, "$command is not a valid command for all switches specified")
    }

}



