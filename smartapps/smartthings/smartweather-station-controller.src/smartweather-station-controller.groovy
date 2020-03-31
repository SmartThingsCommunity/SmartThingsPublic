/**
 *  Weather Station Controller
 *
 *  Copyright 2014 SmartThings
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
    name: "SmartWeather Station Controller",
    namespace: "smartthings",
    author: "SmartThings",
    description: "Updates SmartWeather Station Tile devices every hour.",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/SafetyAndSecurity/App-MindYourHome.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/SafetyAndSecurity/App-MindYourHome@2x.png"
)

preferences {
	section {
		input "weatherDevices", "device.smartweatherStationTile"
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
    scheduledEvent()
}

def scheduledEvent() {
	log.info "SmartWeather Station Controller / scheduledEvent terminated due to deprecation" // device handles this itself now -- Bob
/*
	log.trace "scheduledEvent()"

	def delayTimeSecs = 60 * 60 // reschedule every 60 minutes
	def runAgainWindowMS = 58 * 60 * 1000 // can run at most every 58 minutes
	def timeSinceLastRunMS = state.lastRunTime ? now() - state.lastRunTime : null //how long since it last ran?

	if(!timeSinceLastRunMS || timeSinceLastRunMS > runAgainWindowMS){
		runIn(delayTimeSecs, scheduledEvent, [overwrite: false])
		state.lastRunTime = now()
		weatherDevices.refresh()
	} else {
		log.trace "Trying to run smartweather-station-controller too soon. Has only been ${timeSinceLastRunMS} ms but needs to be at least ${runAgainWindowMS} ms"
	}
    */
}
