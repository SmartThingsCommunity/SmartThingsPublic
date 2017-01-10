//==================================================================================
/**
 *  Motion-Switch-Sched SmartApp
 *
 *  Copyright 2017 PASCI Ciro Ippolito 2017
 ***********************************************************************************
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
 //==================================================================================
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
//==================================================================================
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
//==================================================================================
def installed() {
	log.debug "Installed with settings: ${settings}"
	initialize()
}
//==================================================================================
def updated() {
	log.debug "Updated with settings: ${settings}"
	unsubscribe()
	initialize()
}
//==================================================================================
def initialize() {
    log.debug "Installata at: " + now()
    schedule(FROMTime, handlerFROMTIME)
    schedule(TOTime,   handlerTOTime)
    subscribe(themotion, "motion.active",   handler_Motion___Active)
    subscribe(themotion, "motion.inactive", handler_motion_inactive)
}
// HANDLERS
//==================================================================================
def handlerFROMTIME() {
    log.debug "FROM TIME at ${new Date()}"
    OUTLE.on()
}
//==================================================================================
def handlerTOTime() {
	log.debug "TO TIME at ${new Date()}"
	checkMotion();
    /*
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
    */
}
//==================================================================================
def handler_Motion___Active(evt) {
	log.debug("MOT Active")
	OUTLE.on()
}
//==================================================================================
def handler_motion_inactive(evt) {
	log.debug("MOT Inactive")
	def between = timeOfDayIsBetween(FROMTime, TOTime, new Date(), location.timeZone)  
	if (between) {
    	log.debug("It dasan't mattar Christopha")
		OUTLE.on()
    } else {
		runIn(60 * minutes, checkMotion)
    }
}
//==================================================================================
def checkMotion() {
    log.debug "In checkMotion scheduled method"
    // get the current state object for the motion sensor
    def motionState = themotion.currentState("motion")
    if (motionState.value == "inactive") {
        def elapsed = now() - motionState.date.time // time elapsed between now and when the motion reported inactive
        def threshold = 1000 * 60 * minutes  // elapsed time is in milliseconds
            if (elapsed >= threshold) {
            	log.debug "Motion has stayed inactive long enough since last check ($elapsed ms):  turning switch off"
            	OUTLE.off()
            } else {
            	log.debug "Motion has not stayed inactive long enough since last check ($elapsed ms):  doing nothing"
        }
    } else {
		// Motion active; just log it and do nothing
		log.debug "Motion is active, do nothing and wait for inactive"
    }
}
//==================================================================================
//==================================================================================