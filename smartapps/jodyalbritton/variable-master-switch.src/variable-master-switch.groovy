/**
 *  Variable Master Switch
 *
 *  Copyright 2015 jody albritton
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
 *
 *
 *  Version .01
 *  08/13/2015
 *  Created for Keo on Live Code Friday with Patrick Stuart 
 *  Than you Tim Slagle and Patrick Stuart
 */
definition(
    name: "Variable Master Switch",
    namespace: "jodyalbritton",
    author: "jody albritton",
    description: "When any switch in the group is turned on or off the rest of the selected devices will follow",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
	section("Title") {
		input "switches", "capability.switch", title: "Select Switches...", required: true, multiple:true
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
	 switches.each { device ->
    	device.off()
    }
    
    state.status = false
    
    subscribeToEvents()
}

def subscribeToEvents(){
	subscribe(switches, "switch.on", switchOn)
    subscribe(switches, "switch.off", switchOff)
}



def switchOn(evt){
    if (state.status != "allOn"){
    // Turn on all of the switches
	switches.each { device ->
    	log.debug device
        	device.on()
            
     }
    
    state.status = true
  }
}

def switchOff(evt){

if (state.status != "allOff"){
    // Turn on all of the switches
	switches.each { device ->
    	log.debug device
        	device.off()
            
     }
    
    state.status = false
  }

}


