/**
 *  Copyright 2016 Jeremy Lucier
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
 *  Idle Garage Door
 *
 *  Author: Jeremy Lucier
 */
definition(
    name: "Idle Garage Door",
    namespace: "jrlucier",
    author: "Jeremy Lucier",
    description: "Monitor your garage for inactivity.  If no motion has been detected for X minutes, then close the specified garage door(s).",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Meta/garage_contact.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Meta/garage_contact@2x.png"
)


preferences {
    section("Motion sensor(s) to monitor for inactivity") {
        input "motionSensors", "capability.motionSensor", multiple: true, required: true
    }
    section("Garage door(s) to monitor and close") {
        input "garageDoors", "capability.garageDoorControl", multiple: true, required: true
    }
    section("Close after how many minutes of inactivity") {
	input "minutes", "number", title: "Minutes", required: true
    }
    section("Send push notification?") {
        input "sendPush", "bool", required: false,
              title: "Send Push Notification On Close"
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

    // Fire off the activityHandler when motion stops
    subscribe(motionSensors, "motion.inactive", activityHandler)
    
    // Subscribe to the garage door opening, so that if somehow
    // someone doesn't trigger the motion detector it'll still
    // close after the specified amount of time
    subscribe(garageDoors, "door.open", activityHandler)
}


def activityHandler(evt) {
    log.debug "activityHandler called: $evt"
    
    // This will overwrite any prior timer
    runIn(60 * minutes, checkMotion)
}

def checkMotion() {
    log.debug "In checkMotion scheduled method"
    def motionStatesInactive = allMotionStatesInactive()
    def garageDoorsClosed = allGarageDoorsClosed()
    
	// Check nothing's happening on the motion sensors, and a door is at least up
    if (motionStatesInactive == true && garageDoorsClosed == false) {
    
    	// Ensures we don't trigger the close too early (like if the motion sensor is active during this check)
        if (allMotionSensorsWithinThreshold() == true) {
            log.debug "Motion has stayed inactive long enough since last check ($elapsed ms):  Closing garage door(s)"
            if (sendPush) {
                sendPush("Closing inactive garage door(s)")
            }
            
            // Close the doors!
            closeOpenGarageDoors()
        } else {
            log.debug "Motion has not stayed inactive long enough since last check ($elapsed ms):  Doing nothing"
        }
    } else {
        // Motion active; just log it and do nothing
        log.debug "Motion is active or the garage door are down/moving, do nothing and wait for inactive again. All Garage Doors Closed: $garageDoorsClosed; All Motion States Inactive: $motionStatesInactive"
    }
}

/**
	Check if all of the motion sensors are not in a threshold of reporting active.  If not, we don't want to close a garage door on an active garage!
**/
def allMotionSensorsWithinThreshold() {
    def threshold = 1000 * 60 * minutes
    def outOfThreshold = motionSensors.find{m -> m.currentState("motion").date.time < threshold}
    return (outOfThreshold == null)
}

/**
	Are all motion sensors inactive?
**/
def allMotionStatesInactive() {
    def activeSensor = motionSensors.find{m -> m.currentState("motion").value != "inactive"}
    return (activeSensor == null)
}

/**
	Are all garage doors closed?
**/
def allGarageDoorsClosed() {
     def activeDoor = garageDoors.find{g -> g.currentState("door").value != "closed"}
     return (activeDoor == null)
}

/**
	Close all the open garage doors
**/
def closeOpenGarageDoors() {
     garageDoors.each {g -> if (g.currentState("door").value != "closed") g.close() }
}
