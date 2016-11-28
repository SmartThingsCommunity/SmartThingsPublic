/**
 *  Set Lights Color Temp On A Schedule
 *
 *  Copyright 2016 Ilan Goodman
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
    name: "Set Lights Color Temp On A Schedule",
    namespace: "ilangoodman",
    author: "Ilan Goodman",
    description: "Change the color temperature and brightness of tunable white bulbs at the given time.",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
	section("Adjust these lights...") {
		input "lights", "capability.colorTemperature",
        	multiple: true, 
			title: "Tunable White Bulbs", 
			required: true
	}
    
    section("Set color temperature to...") {
    	input "colorTemp", "number",
        title: "Color Temperature",
        required: true
    }
    
    section("At this time every day...") {
    	input "theTime", "time", 
        	title: "Time to execute every day",
        	required: true
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
	schedule(theTime, setColorTempHandler)
}

def setColorTempHandler()
{	
	log.debug "setColorTempHandler: called at ${new Date()}"
    log.debug "setColorTempHandler: setting color temp to $colorTemp"
    lights?.setColorTemperature(colorTemp)
    
}