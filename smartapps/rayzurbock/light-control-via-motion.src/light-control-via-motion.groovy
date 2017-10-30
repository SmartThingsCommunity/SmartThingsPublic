/**
 *  Copyright 2015 Rayzurbock
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
 *  Light Control by Motion
 *  Author: Rayzurbock  rayzur [at] rayzurbock.com
 *  Source: https://github.com/rayzurbock/SmartThings-LightControlViaMotion
 *  Purpose:  
 *     1) Reliably turn light(s) on when any motion sensor in a specific area senses motion.  
 *     2) Reliably turn light(s) off when motion sensor has not seen motion for x minutes.
 */

definition(
    name: "Light Control via Motion",
    namespace: "rayzurbock",
    author: "Rayzurbock",
    description: "Reliably turn your lights on when motion is detected on a single or series of sensors and then off again once the motion stops for a set period of time.",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Meta/temp_thermo-switch.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Meta/temp_thermo-switch@2x.png"
)

preferences {
	section("Turn on when there's movement..."){
		input "motion", "capability.motionSensor", title: "Where?", multiple: true
	}
	section("And off when there's been no movement for..."){
		input "minutes", "number", title: "Minutes?"
	}
	section("Turn on/off light(s)..."){
		input "switches", "capability.switch", multiple: true
	}
}

def installed() {
	log.debug "LCM: Installed..."
    subscribe(motion, "motion", motionHandler)
}

def updated() {
	"LCM: Settings Updated..."
    unsubscribe()
    unschedule()
	subscribe(motion, "motion", motionHandler)
}

def motionHandler(evt) {
	//state.debugEnabled = true
    state.debugEnabled = false
    DebugLog("LCM: $evt.displayName: $evt.value")
	if (evt.value == "active") {
        state.inactivecount = 0
    	switches.each() {
        	//DebugLog("LCM: DEBUG: ${it.displayName}=${it.currentState("switch").value}")
			if (it.currentState("switch").value == "off") {
            	DebugLog("LCM: ${it.displayName}, turning ON due to motion at $evt.displayName")
				switches.on()
            } else {
            	DebugLog("LCM: ${it.displayName}, light is already on.")
            }
        }
	} else if (evt.value == "inactive") {
            state.active = false
            motion.each{
              if (it.currentState("motion").value == "active") { state.active = true }
            }
            if (state.active == false) {
                DebugLog("All motions inactive in target area, rechecking in 1 minute")
                runIn(60, scheduleCheck, [overwrite: true])
            } else {
               DebugLog("Other motions active in target area, ignoring inactive motion event")
            }
            //runIn((minutes * 60), scheduleCheck, [overwrite: true])
            //DebugLog("LCM: $evt.displayName, Schedule check in ${(minutes * 60)} seconds")
	}
}

def scheduleCheck() {
    state.active = false
	motion.each{
        if (it.currentState("motion").value == "active"){
          state.active = true
          state.inactivecount = 0
          DebugLog("LCM: ${it.displayName} is active; not turning switch off")
        }
    }
    if (state.active == false) {
        state.inactivecount = state.inactivecount + 1
        DebugLog("LCM: Inactive Counter Increased to ${state.inactivecount} of ${minutes}")
        if (state.inactivecount >= minutes) {
            DebugLog("LCM: Motions in target area have not detected motion for ${minutes} minutes.  Turning off lights")
            state.inactivecount = 0
            switches.each(){
               	    if (it.currentState("switch").value == "on") {
                	    DebugLog("LCM: Turning off: ${it.displayName}")
            		    it.off()
                    } else {
                	    DebugLog("LCM: No affected lights were on")
                    }
            }
       } else {
           runIn(60, scheduleCheck, [overwrite: true]) //reschedule check every minute while light is on and motion is inactive.
       }
    }
}

def DebugLog(txt){
   if (state.debugEnabled == true) {log.debug txt}
}