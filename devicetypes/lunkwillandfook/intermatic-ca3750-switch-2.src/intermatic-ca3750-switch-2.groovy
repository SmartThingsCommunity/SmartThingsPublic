/**
 *  Intermatic CA3750 Switch 2
 *
 *  Copyright 2015 Jeremy Huckeba
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
	// Automatically generated. Make future change here.
	definition (name: "Intermatic CA3750 Switch 2", namespace: "LunkwillAndFook", author: "Jeremy Huckeba") {
		capability "Switch"
		capability "Polling"
		capability "Refresh"
        
		attribute "switch", "string"

		command "on"
		command "off"
		command "turnedOn"
		command "turnedOff"
	}
    
	simulator {
		// TODO: define status and reply messages here
	}
    
	// tile definitions
    tiles {
        standardTile("switch", "device.switch", width: 2, height: 2, canChangeIcon: true) {
            state "off", label: '${name}', action: "on", icon: "st.switches.switch.off", backgroundColor: "#ffffff"
            state "on", label: '${name}', action: "off", icon: "st.switches.switch.on", backgroundColor: "#79b821"
        }
        standardTile("refresh", "device.switch", inactiveLabel: false, decoration: "flat") {
            state "default", label:"", action:"refresh", icon:"st.secondary.refresh"
        }
        
        main "switch"

        //details(["switch","switch1","switch2","refresh"])
        details(["switch","refresh"])
    }
}

def poll() {
	log.debug "Polling Switch - $device.label"
	parent.poll()
}

def refresh() {
	log.debug "Refresh Switch - $device.label"
	parent.refresh()
}

def on() {
	parent.on()
    log.debug "Parent on called"
}

def off() {
	parent.off()
    log.debug "Parent off called"
}

def turnedOn() {
	sendEvent(name: "switch", value: "on")
}

def turnedOff() {
	sendEvent(name: "switch", value: "off")
}