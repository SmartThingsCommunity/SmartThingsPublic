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
 *  Once a Day
 *
 *  Author: SmartThings
 *
 *  Turn on one or more switches at a specified time and turn them off at a later time.
 */

definition(
    name: "Once a Day",
    namespace: "smartthings",
    author: "SmartThings",
    description: "Turn on one or more switches at a specified time and turn them off at a later time.",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Meta/light_outlet.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Meta/light_outlet@2x.png"
)

preferences {
	section("Select switches to control...") {
		input name: "switches", type: "capability.switch", multiple: true
	}
	section("Turn them all on at...") {
		input name: "startTime", title: "Turn On Time?", type: "time"
	}
	section("And turn them off at...") {
		input name: "stopTime", title: "Turn Off Time?", type: "time"
	}
}

def installed() {
	log.debug "Installed with settings: ${settings}"
	schedule(startTime, "startTimerCallback")
	schedule(stopTime, "stopTimerCallback")

}

def updated(settings) {
	unschedule()
	schedule(startTime, "startTimerCallback")
	schedule(stopTime, "stopTimerCallback")
}

def startTimerCallback() {
	log.debug "Turning on switches"
	switches.on()

}

def stopTimerCallback() {
	log.debug "Turning off switches"
	switches.off()
}
