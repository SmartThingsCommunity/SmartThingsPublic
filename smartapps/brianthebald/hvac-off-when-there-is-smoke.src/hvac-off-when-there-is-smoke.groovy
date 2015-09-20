/**
 *  HVAC Off When There Is Smoke
 *
 *  Copyright 2015 Brian Stearns
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
    name: "HVAC Off When There Is Smoke",
    namespace: "brianthebald",
    author: "Brian Stearns",
    description: "Turn off HVAC (thermostats) when smoke is detected",
    category: "Safety & Security",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png"
)

preferences {
	section("Select the smoke detectors to monitor...") {
		input "smokeDetectors", "capability.smokeDetector", title: "Smoke Detectors", multiple: true, required: true
	}
    
    section("Select the thermostats to shut off...") {
		input "thermostats", "capability.thermostat", title: "Thermostats", multiple: true, required: true
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
  subscribe(smokeDetectors, "smoke", smokeHandler)
}

def smokeHandler(evt) {
  if("detected" == evt.value) {
	log.debug "Smoke detected! Turning off thermostats"
    thermostats.off()
  }
}