/**
 *  Close and Trigger Routine
 *
 *  Copyright 2015 Chris LeBlanc
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
    name: "Close and Trigger Routine",
    namespace: "LeBlaaanc",
    author: "Chris LeBlanc",
    description: "Activates a switch then waits for a contact sensor and then runs a routine.",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
  page(name: "pageVirtualSwitch")
  page(name: "pageDoor")
  page(name: "pageContactSensor")
  page(name: "pageRoutine")
}

def pageVirtualSwitch() {    
    dynamicPage(name: "pageVirtualSwitch", title: "Virtual Switch", nextPage: "pageDoor", unintall:true) {        
		section("When this switch is activated..."){
			input "sourceSwitch", "capability.switch", title: "Which?"
		}
	}
}

def pageDoor() {    
    dynamicPage(name: "pageDoor", title: "Door", nextPage: "pageContactSensor") {        
		section("Close this door..."){
			input "targetDoor", "capability.actuator", title: "Which?"
		}
	}
}

def pageContactSensor() {    
    dynamicPage(name: "pageContactSensor", title: "Contact Sensor", nextPage: "pageRoutine") {        
		section("Contact sensor..."){
			input "targetContactSensor", "capability.contactSensor", title: "Which?"
		}
	}
}

def pageRoutine() {    
    dynamicPage(name: "pageRoutine", title: "Routine", install: true) {        
        def phrases = location.helloHome?.getPhrases()*.label
    	section("Routine.") {
        	input "targetRoutine", "enum", title: "Phrase", required: false, options: phrases
		}
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
	subscribe(sourceSwitch, "switch", sourceSwitchHandler)
	
}

def sourceSwitchHandler(evt) {
	log.debug "{$evt.value}";
	if (evt.value == "on") {
    	targetDoor.close()
        subscribe(targetContactSensor, "contact", contactHandler)
    } else {
    	unsubscribe(targetContactSensor)
    }
}

def contactHandler(evt) {
	log.debug "{$evt.value}"
	if (evt.value == "closed") {
    	log.debug "Trigger routine."
    }
}