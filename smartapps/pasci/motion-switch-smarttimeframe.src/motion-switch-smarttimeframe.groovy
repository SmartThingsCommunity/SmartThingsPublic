/**
 *  Motion-Switch-Sched SmartApp
 *
 *  Copyright 2017 PASCI Ciro Ippolito 2017
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
    name: "Motion Switch SmartTimeframe",
    namespace: "PASCI",
    author: "PASCI",
    description: "Motion Activated switch + Override timeframe.",
    category: "Convenience",
    iconUrl: "http://cdn.device-icons.smartthings.com/Home/home30-icn@2x.png",
    iconX2Url: "http://cdn.device-icons.smartthings.com/Home/home30-icn@2x.png",
    iconX3Url: "http://cdn.device-icons.smartthings.com/Home/home30-icn@2x.png"
    )
    //iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    //iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    //iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")
preferences {
    section("When MOTION is detected") {
        input "themotion", "capability.motionSensor", required: true, title: "WHERE"
    }
    section("Turn ON this SWITCH") {
        input "OUTLE", "capability.switch", required: true, title: "WHICH"
    }
    section("ON for how long") {
         input "minutes", "number", required: true, title: "MINUTES)"
    }
    section("ON OVERRIDE time frame (if there is no motion)") {
        input "FROMTime", "time", title: "From", required: true
        input "TOTime", "time", title: "To", required: true
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
    log.debug "Installata at: " + now()
    schedule(FROMTime, handlerFROMTIME)
    schedule(TOTime,   handlerTOTime)
    subscribe(themotion, "motion", motionDetectedHandler)
}
// HANDLERS
def handlerFROMTIME() {
    log.debug "TURN ON at ${new Date()}"
    OUTLE.on()
}
def handlerTOTime() {
	def motionState = themotion.currentState("motion")
	log.debug "state of motion sensor is: " + motionState.value
    log.debug "TURN OFF at ${new Date()}"
    log.debug "state of motion sensor is: " + themotion.motion
    if (motionState.value ==  "active"){
    	log.debug "state of motion sensor is ACTIVE"
    }else{
    	OUTLE.off()
    	log.debug "state of motion sensor is INACTIVE"
    }   
}
def motionDetectedHandler(evt) {
    log.debug "motionDetectedHandler called: $evt"
	log.debug "INACTIVE";
   	def between = timeOfDayIsBetween(FROMTime, TOTime, new Date(), location.timeZone)  
	if (between) {
		OUTLE.on()
    } else {
		if  (evt.value == "active"){
          log.debug "ACTIVE";
          OUTLE.on()
        }else{
            OUTLE.off()
        }
    }
}
