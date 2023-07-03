/**
 *  Copyright 2015 SmartThings
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
metadata {
	definition (name: "Virtual Contact Sensor Switch", namespace: "ericyew", author: "Eric Yew") {
		capability "Contact Sensor"
		capability "Sensor"
    capability "Actuator"
    capability "Switch"    
    command "timing"
	}

	// simulator metadata
	simulator {
		}

	// UI tile definitions
	tiles {
		standardTile("contact", "device.contact", width: 2, height: 2) {
			state "open", label: '${name}', icon: "st.contact.contact.open", backgroundColor: "#e86d13"
			state "closed", label: '${name}', icon: "st.contact.contact.closed", backgroundColor: "#00A0DC"
            state "timing", label: '${name}', icon: "st.Health & Wellness.health7", backgroundColor: "#00A0DC"
		}
        standardTile("close", "device.switch", inactiveLabel: false, decoration: "flat") {
			state "default", label:'close', action:"switch.off"
		}

		main "contact"
		details "contact","close"
	}
    preferences {
    	input name: "autoReset", type: "bool", title: "Auto Reset?", description: "Force Sensor to Auto Reset to Closed", required: false, defaultValue: false	
        input name: "resetDelay", type: "number", title: "Reset Delay", description: "Number of Seconds to Hold Open", required: false, defaultValue: 1
        input name: "delayOpen", type: "bool", title: "Delay Open?", description: "Delays Opening of Contact", required: false, defaultValue: false	
        input name: "openDelay", type: "number", title: "Open Delay", description: "Number of Seconds to Delay Open", required: false, defaultValue: 120 
        
	}
}
def on() {
    
    log.debug "ON was sent"
    if (delayOpen == true) {
    	log.debug "Timing"
        sendEvent(name: "contact", value: "timing")
        runIn(openDelay, openContact)
    }
    else {
    	log.debug "Open Contact Without Delay"
    	openContact()
    }
}

def off() {
	log.debug "OFF was sent"
    sendEvent(name: "contact", value: "closed")
    log.debug "Contact is closed"
}

def timing() {
    log.debug "Timing was sent"
    sendEvent(name: "contact", value: "timing")
}
def openContact() {
	
    sendEvent(name: "contact", value: "open")
    log.debug "Contact is open"
    if (autoReset == true) {
    	log.debug "Performing Auto Reset"
    	runIn(resetDelay + 1, off)
    }
}
