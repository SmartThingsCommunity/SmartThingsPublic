/**
 *  Turn Off Pool Water
 *
 *  Copyright 2018 Adam Outler
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
    name: "Fill My Pool And Stop",
    namespace: "adamoutler",
    author: "Adam Outler",
    description: "Turn off a valve after a designated time. ",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png") {
    appSetting "valve"
    appSetting "time"
    }
    
preferences {
	section("Pool Setup") {
        input "valve", "capability.valve",
        	title: "Which valve?", multiple: false
        input "minutes","number",
        	title: "How many minutes do you want to fill the pool before automatic shutoff? Use whole numbers. 1 hour=60, 4 hours=240", multiple: false
	}
}

def installed() {
	log.debug "Installed with settings: ${settings}"
	initialize()
}

def closedValveHandler(evt){
 log.debug("Valve closed. unscheduling all scheduled closures of ${valve}")
 unschedule()
}


def openValveHandler(evt){
 log.debug "${valve} detected opened"
 runIn(60*(int)minutes, closeValve)
 log.debug "${valve} scheduled to close in ${minutes} minutes"
}

def updated() {
	log.debug "Updated with settings: ${settings}"
	unsubscribe()
	initialize()
}

def initialize() {
       valve.close()
       subscribe(valve, "valve.open", openValveHandler)
       subscribe(valve, "valve.closed", closedValveHandler)
}

def closeValve(){
  valve.close()
   Log.debug "${valve} was closed after ${minutes} minutes"
}