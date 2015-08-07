/**
 *  Recirculator Schedule
 *
 *  Copyright 2015 Justin Wood
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
    name: "Recirculator Schedule",
    namespace: "terafin",
    author: "terafin",
    description: "Recirculator Scheduling",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
	section("Select Recirculator"){
		input "switch1", "capability.switch", multiple: true
	}
	section("Turn the Recirculator On in the morning at:"){
		input "startTimeMorning", "time", title: "Start Time - Morning", required:false
	}
	section("Turn the Recirculator Off in the morning at:"){
		input "endTimeMorning", "time", title: "End Time - Morning", required:false
	}    

	section("Turn the Recirculator On in the evening at:"){
		input "startTimeEvening", "time", title: "Start Time - Evening", required:false
	}
	section("Turn the Recirculator Off in the morning at:"){
		input "endTimeEvening", "time", title: "End Time - Evening", required:false
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
	unschedule()
	def currTime = now()
	def endTimeEveningTime = timeToday(endTimeEvening).time
	def startTimeEveningTime = timeToday(startTimeEvening).time
	def endTimeMorningTime = timeToday(endTimeMorning).time
	def startTimeMorningTime = timeToday(startTimeMorning).time

    log.debug "Current Time: $currTime"
    log.debug "endTimeEveningTime: $endTimeEveningTime"
    log.debug "startTimeEveningTime: $startTimeEveningTime"
    log.debug "endTimeMorningTime: $endTimeMorningTime"
    log.debug "startTimeMorningTime: $startTimeMorningTime"

     if(startTimeMorning && (currTime < startTimeMorningTime) ) {
      	schedule(startTimeMorning, turnOnRecirculator)
        log.debug "Scheduling to turn on recirculator in the morning at $startTimeMorning"
    } else if(endTimeMorning && (currTime < endTimeMorningTime) ) {
        log.debug "Scheduling to turn OFF recirculator in the morning at $endTimeMorning"
        schedule(endTimeMorning,turnOffRecirculator)
	} else if(startTimeEvening && (currTime < startTimeEveningTime) ) {
        log.debug "Scheduling to turn on recirculator in the evening at $startTimeEvening"
		schedule(startTimeEvening, turnOnRecirculator)
  	} else if(endTimeEvening && (currTime < endTimeEveningTime) ) {
        log.debug "Scheduling to turn OFF recirculator in the evening at $endTimeEvening"
		schedule(endTimeEvening,turnOffRecirculator)
    } else if(startTimeMorning) {
      	schedule(startTimeMorning, turnOnRecirculator)
        log.debug "Scheduling to turn on recirculator tomorrow, in the morning at $startTimeMorning"
    }
}

def turnOnRecirculator() {
    log.info "turned on recirculator"
    switch1.on()

    updated()
 }

def turnOffRecirculator() {
    log.info "turned off recirculator"
    switch1.off()

    updated()
}
