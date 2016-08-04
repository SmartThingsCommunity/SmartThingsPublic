/**
 *  List My Devices
 *
 *  Copyright 2016 Ravi Dubey
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
    name: "List My Devices",
    namespace: "Ravi-em",
    author: "Ravi Dubey",
    description: "This all will show current status of devices.",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
	section("Turn on when motion detected") {
    	input "themotion", "capability.motionSensor", required:true, title:"Where?"
    }
    section("Turn on this light") {
    	input "theswitch", "capability.switch", required:true, title:"Which light?",multiple:true
    }
    section("Lock this door") {
    	input "thelock", "capability.lock", required:true, title:"Which lock?",multiple:true
    }    
    section("Send Notifications?") {
        input("recipients", "contact", title: "Send notifications to") {
        input "phone", "phone", title: "Warn with text message (optional)",description: "Phone Number", required: false
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
	// TODO: subscribe to attributes, devices, locations, etc.
    subscribe(themotion, "motion", motionDetectedHandler)
    subscribe(theswitch, "switch", someEventHandler)
    
}

def motionDetectedHandler(evt){
	log.debug "motionDetectedHandler called: $evt"
	log.debug evt.value
    
    if (evt.value == "active") {
    // motion detected
    theswitch.on()
    thelock.lock()
    sendNotificationToContacts("your message here", recipients)
    } else if (evt.value == "inactive") {   
    // motion stopped
    theswitch.off()
    thelock.unlock()
    }
    
    
  
}

def someEventHandler(evt) {


/*

// returns a list of the values for all switches
def currSwitches = theswitch.currentSwitch
def onSwitches = currSwitches.findAll { switchVal ->
	switchVal == "on" ? true : false
	}
log.debug "${onSwitches.size()} out of ${theswitch.size()} switches are on"
*/

  for (swt in theswitch) {
        //log.debug "in for loop:"
        def nm = swt.displayName;
        def st= swt.currentState("switch")
        //log.debug "$nm.value"
        
        if('on'==st.value)
        {
               log.debug "$nm.value is on"
        }
        if('off'==st.value)
        {
               log.debug "$nm.value is off"
        }
        
	}
}

// TODO: implement event handlers