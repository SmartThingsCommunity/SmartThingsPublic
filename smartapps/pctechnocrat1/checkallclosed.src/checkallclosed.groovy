/**
 *  CheckAllClosed
 *
 *  Copyright 2018 Jared Bevis
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
    name: "CheckAllClosed",
    namespace: "pctechnocrat1",
    author: "Jared Bevis",
    description: "Checks door and window sensors for OPEN status at a preset time.  It sends a notification if any sensors report OPEN status.",
    category: "Safety & Security",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
	section("Sensors to check:") {
		// TODO: put inputs here
        input "Sensors", "capability.contactSensor", required: true, multiple: true
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
	log.debug "Checking all sensors closed... ${new Date()}"
    
    for (int i=0; i < Sensors.size(); i++){
    	def sensor_state = Sensors[i].currentValue("contact")
    	//log.debug "Contact Sensor ${Sensors[i].label}: ${sensor_state}"
        if(sensor_state == "open"){
        	sendPush("${Sensors[i].label} is open.")
            //log.debug "${Sensors[i].label} is open."
        }
    }
    
}

def initialize() {
	// TODO: subscribe to attributes, devices, locations, etc.
    schedule(CheckTime, runChecks)
    //runChecks()
}

// TODO: implement event handlers