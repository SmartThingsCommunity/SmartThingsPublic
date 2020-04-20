/**
 *  Copyright 2015 SmartThings
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
 *  Light Follows Me
 *
 *  Author: SmartThings
 */

definition(
    name: "Smarter lights",
    namespace: "smartthings",
    author: "SmartThings",
    description: "Turn your lights on when motion is detected and then off again once the motion stops for a set period of time, unless you have adjusted the lights.",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Meta/temp_thermo-switch.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Meta/temp_thermo-switch@2x.png"
)

preferences {
	section("Turn on when there's movement..."){
		input("motion1", "capability.motionSensor", title: "Where?")
	}
	section("And off when there's been no movement for..."){
		input("minutes1", "number", title: "Minutes?")
	}
	section("Turn on/off light(s)..."){
		input("switches", "capability.switchLevel", multiple: true)
	}
	section("Set to this level")
	{
		input("level", "number", required: true)
	}
}

def installed() {
	subscribe(motion1, "motion", motionHandler)
}

def updated() {
	unsubscribe()
	subscribe(motion1, "motion", motionHandler)
}

def motionHandler(evt) {
	log.debug "$evt.name: $evt.value"
	if (evt.value == "active") {
		def shouldTurnOn = true
	
		for (aSwitch in switches) 
		{
			log.debug "aSwitch.currentLevel = $aSwitch.currentLevel"
            log.debug "switches.currentSwitch = $aSwitch.currentSwitch"
           	if(aSwitch.currentLevel > level && aSwitch.currentSwitch != "off")
            {
				aSwitch.refresh()
           		shouldTurnOn = false
            }
        }

		if (shouldTurnOn)
		{
			log.debug "turning on lights"
			switches.setLevel(level)
		}
		else
		{
            log.debug "Light levels have changed."
		}
	} 
	else if (evt.value == "inactive") 
	{
		runIn(minutes1 * 60, scheduleCheck, [overwrite: false])
	}
}

def scheduleCheck() {
	log.debug "schedule check"
	def motionState = motion1.currentState("motion")
    if (motionState.value == "inactive") {
        def elapsed = now() - motionState.rawDateCreated.time
    	def threshold = 1000 * 60 * minutes1 - 1000
    	if (elapsed >= threshold) {
            log.debug "Motion has stayed inactive long enough since last check ($elapsed ms):  checking light levels"
			def shouldTurnOff = true
			for (aSwitch in switches) 
			{
				aSwitch.refresh()
            	if(aSwitch.currentLevel > level)
            	{
            		shouldTurnOff = false
            	}
            }
            if (shouldTurnOff)
            {
            	switches.setLevel(0);
            }
            else
            {
            	log.debug "Light levels have changed."
            }
    	} else {
        	log.debug "Motion has not stayed inactive long enough since last check ($elapsed ms):  doing nothing"
        }
    } else {
    	log.debug "Motion is active, do nothing and wait for inactive"
    }
}