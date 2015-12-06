/**
 *  Miner Watcher
 *  Based on: Energy Saver
 *
 *  Copyright 2014 SmartThings
 *  Copyright 2015 trentfoley64
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
    name: "Miner Watcher",
    namespace: "trentfoley64",
    author: "A. Trent Foley",
		description: "Turn things off for a while when energy usage falls outside of thresholds.",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Meta/light_outlet.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Meta/light_outlet@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Meta/light_outlet@2x.png"
)

preferences {
	section {
		input(name: "minerMeter", type: "capability.powerMeter", title: "When This Power Meter...", required: true, multiple: false, description: null)
        input(name: "thresholdLow", type: "number", title: "Reports Below...", required: true, description: "in Watts.")
        input(name: "thresholdHigh", type: "number", title: "Reports Above...", required: true, description: "in Watts.")
	}
    section {
    	input(name: "minerSwitches", type: "capability.switch", title: "Turn Off These Switches Powering Mining Equipment", required: true, multiple: true, description: null)
        input(name: "coolOff", type: "number", title: "For how many minutes?", required: true, description: null)
        input(name: "waitForIt", type: "number", title: "Allow how many minutes for startup?", required: true, description: null)
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
    state.waitingForStartup = false
	subscribe(minerMeter, "power", minerMeterHandler)
}

def minerMeterHandler(evt) {
    def minerMeterValue = evt.value as double
    def thresholdLowValue = thresholdLow as int
    def thresholdHighValue = thresholdHigh as int
    
    if (!state.waitingForStartup) {
    	if (minerMeterValue < thresholdLowValue || minerMeterValue > thresholdHighValue) {
	    	log.debug "${minerMeter} reported energy consumption of ${minerMeterValue} which is not between ${thresholdLow} and ${thresholdHigh}. Turning off ${minerSwitches}."
    		minerSwitches.off()
        
        	log.debug "waiting for ${minutes} before restoring power."
    		state.waitingForStartup = true
        	def minutes = 60 * coolOff
			runIn(minutes, restorePower)
        }
	}
}

def restorePower() {
	log.debug "Turning on ${minerSwitches}."
	minerSwitches.on()
    def minutes = 60 * waitForIt
    runIn(minutes, resumeMonitoring)
}

def resumeMonitoring() {
	log.debug "Resume monitoring"
    state.waitingForStartup = false
}
