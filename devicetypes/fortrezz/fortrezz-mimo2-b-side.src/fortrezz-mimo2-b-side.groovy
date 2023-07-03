/**
 *  FortrezZ MIMO2+ B-Side
 *
 *  Copyright 2016 FortrezZ, LLC
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
	definition (name: "FortrezZ MIMO2+ B-Side", namespace: "fortrezz", author: "FortrezZ, LLC") {
		capability "Contact Sensor"
		capability "Relay Switch"
		capability "Switch"
		capability "Voltage Measurement"
        capability "Refresh"
	}
    
	tiles {
         standardTile("switch", "device.switch", width: 2, height: 2) {
            state "on", label: "Relay 2 On", action: "off", icon: "http://swiftlet.technology/wp-content/uploads/2016/06/Switch-On-104-edit.png", backgroundColor: "#53a7c0"            
			state "off", label: "Relay 2 Off", action: "on", icon: "http://swiftlet.technology/wp-content/uploads/2016/06/Switch-Off-104-edit.png", backgroundColor: "#ffffff"
        }
        standardTile("anaDig1", "device.anaDig1", inactiveLabel: false) {
			state "open", label: '${name}', icon: "st.contact.contact.open", backgroundColor: "#ffa81e"
			state "closed", label: '${name}', icon: "st.contact.contact.closed", backgroundColor: "#79b821"
            state "val", label:'${currentValue}v', unit:"", defaultState: true
		}
        standardTile("refresh", "device.switch", inactiveLabel: false, decoration: "flat") {
			state "default", label:'', action:"refresh.refresh", icon:"st.secondary.refresh"
		}
        standardTile("powered", "device.powered", inactiveLabel: false) {
			state "powerOn", label: "Power On", icon: "st.switches.switch.on", backgroundColor: "#79b821"
			state "powerOff", label: "Power Off", icon: "st.switches.switch.off", backgroundColor: "#ffa81e"
		}
		standardTile("configure", "device.configure", inactiveLabel: false, decoration: "flat") {
			state "configure", label:'', action:"configuration.configure", icon:"st.secondary.configure"
		}
        standardTile("blank", "device.blank", inactiveLabel: true, decoration: "flat") {
        	state("blank", label: '')
        }
		main (["switch"])
		details(["switch", "anaDig1", "blank", "blank", "refresh", "powered"])
	}
}

// parse events into attributes
def parse(String description) {
	log.debug "Parsing '${description}'"
	// TODO: handle 'contact' attribute
	// TODO: handle 'switch' attribute
	// TODO: handle 'switch' attribute
	// TODO: handle 'voltage' attribute

}

def eventParse(evt) {
	log.debug("Event: ${evt.name}=${evt.value}")
    switch(evt.name) {
    	case "powered":
        	sendEvent(name: evt.name, value: evt.value)
        	break
    	case "switch2":
        	sendEvent(name: "switch", value: evt.value)
        	break
    	case "contact2":
        	sendEvent(name: "contact", value: evt.value)
        	break
    	case "voltage2":
        	sendEvent(name: "voltage", value: evt.value)
        	break
    	case "relay2":
        	sendEvent(name: evt.name, value: evt.value)
        	break
    	case "anaDig2":
        	sendEvent(name: "anaDig1", value: evt.value)
        	break
    }
}

// handle commands
def on() {
    parent.on2(device.id)
	log.debug("Executing 'on'")
	// TODO: Send Event to parent device for "on2"
}

def off() {
    parent.off2(device.id)
	log.debug("Executing 'off'")
	// TODO: Send Event to parent device for "off2"
}
def refresh() {
	parent.refresh2(device.id)
    log.debug("Executing 'refresh'")
}