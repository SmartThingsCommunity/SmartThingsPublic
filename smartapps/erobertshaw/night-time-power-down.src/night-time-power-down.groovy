/**
 *  Night time power down
 *
 *  Copyright 2015 Aperations.com llc
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
    name: "Night time power down",
    namespace: "erobertshaw",
    author: "Aperations.com llc",
    description: "Turn switches off late at night to save power.",
    category: "Green Living",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")
    

preferences {
	
  	section {
    	input(name: "switches", type: "capability.switch", title: "Turn Off These Switches", required: true, multiple: true, description: null)
  	}
    section {
    	input "turnOffTime", "time", required: true , title: "Turn Off Time"
        input "turnOnTime", "time", required: true , title: "Turn On Time"
    }
    
    section {
    	input name: "meter", type: "capability.powerMeter", title: "Power meter", required: false
    	input(name: "threshold", type: "number", title: "Turn off ONLY if power less than", required: false, description: "in watts")
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
	schedule(turnOffTime, tryShutdown)
    schedule(turnOnTime, turnBackOn)
}

def tryShutdown(){
	//log.debug meter.currentValue("power")
    if(meter != null && threshold != null && threshold.value < meter.currentValue("power")) return
    switches.each { 
    	if( it.currentValue("switch") == "on"){
        	it.off()
        }
    }
}

def turnBackOn(){
	switches.each { 
    	if( it.currentValue("switch") != "on"){
        	it.on()
        }
    }
}

