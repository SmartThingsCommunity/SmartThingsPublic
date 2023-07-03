/**
 *  Battery Monitor
 *
 *  Copyright 2014 Anders Heie
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
    name: "Battery Monitor",
    namespace: "Pope",
    author: "Anders Heie",
    description: "Monitors all Battery levels and sends a notification if lower than a set threshold.  A Button allows an immediate check. ",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
	section("Battery Settings") {
		input "threshold", "number", title: "Alert when below...", required: true
        input "batteries", "capability.battery", title: "Which devices?", multiple: true
        input "theTime", "time", title: "Time to battery check every day"
	}
}



def installed() {
	log.debug "Installed with settings: ${settings}"
	initialize()
}

def updated() {
	log.debug "Updated with settings: ${settings}"
	unschedule()
	initialize()
}

def initialize() {

    //schedule the job
    // http://docs.smartthings.com/en/latest/smartapp-developers-guide/scheduling.html
    schedule(theTime, doBatteryCheck)

    //run at install too
    doBatteryCheck()

}

def doBatteryCheck() {
    log.debug "doBatteryCheck called at ${new Date()}"
	
    def belowLevelCntr = 0
	def pushMsg = ""

	for (batteryDevice in batteries) {
    	def batteryLevel = batteryDevice.currentValue("battery")
		log.debug "${batteryDevice.name} '${batteryDevice.label}' battery: ${batteryLevel}% versus alarm at ${settings.threshold}"
        if ( batteryLevel <= settings.threshold ) {
            pushMsg += "${batteryDevice.name} '${batteryDevice.label}' battery: ${batteryLevel}% \n"
            belowLevelCntr++
        }
    }

    if (belowLevelCntr ){
    	pushMsg = "You have ${belowLevelCntr} devices below the set alarm level ${settings.threshold}% \n" + pushMsg

    } else {
        pushMsg = "Battery Check App executed with no devices below ${settings.threshold}%"
    }

    log.debug(pushMsg)

    sendPush(pushMsg)
}