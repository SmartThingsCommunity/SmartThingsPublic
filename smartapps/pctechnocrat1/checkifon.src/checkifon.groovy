/**
 *  CheckIfOn
 *
 *  Copyright 2019 Jared Bevis
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
    name: "CheckIfOn",
    namespace: "pctechnocrat1",
    author: "Jared Bevis",
    description: "Routine to send notification if a switch is left on at a certain time.",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")

preferences {
	section("Switches to check:") {
		// TODO: put inputs here
        input "Switches", "capability.switch", required: true, multiple: true
	}
    
    section("What Time?"){
    	input "CheckTime", "time"
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

def runChecks(){
	log.debug "Checking all switches off... ${new Date()}"
    
    for (int i=0; i < Switches.size(); i++){
    	def switch_state = Switches[i].currentSwitch
    	//log.debug "Switch ${Switches[i].label}: ${switch_state}"
        if(switch_state == "on"){
        	sendPush("${Switches[i].label} is on.")
            //log.debug "${Switches[i].label} is on."
        }
    }
    
}

def initialize() {
	// TODO: subscribe to attributes, devices, locations, etc.
    schedule(CheckTime, runChecks)
    //runChecks()
}

// TODO: implement event handlers