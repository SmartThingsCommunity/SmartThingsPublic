/**
 *  Momentary Button Tile
 *
 *  Copyright 2016 Michael Struck
 *  Version 1.0.1 2/27/16
 *
 *  Version 1.0.0 Initial release
 *  Version 1.0.1 Reverted back to original icons for better GUI experience
 *
 *  Uses code from SmartThings
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
	definition (name: "Momentary Button Tile", namespace: "MichaelStruck", author: "SmartThings") {
		capability "Actuator"
		capability "Switch"
		capability "Momentary"
		capability "Sensor"
        
		attribute "About", "string"
	}

	// simulator metadata
	simulator {
	}
	// UI tile definitions
	tiles(scale: 2) {
		multiAttributeTile(name: "switch", type: "generic", width: 6, height: 4, canChangeIcon: true, canChangeBackground: true) {
			tileAttribute("device.switch", key: "PRIMARY_CONTROL") {
				attributeState "off", label: 'push', action: "momentary.push", backgroundColor: "#ffffff",icon: "st.contact.contact.closed", nextState: "on"
				attributeState "on", label: 'push', action: "momentary.push", backgroundColor: "#79b821",icon: "st.contact.contact.closed"
			}
        }
        valueTile("about", "device.About", inactiveLabel: false, decoration: "flat", width: 6, height:2) {
            state "default", label:"Momentary Button Tile\nSwitch created by Alexa Helper\nSwitch code version 1.0.1 (03/01/16)"
		}
        main "switch"
		details (["switch","about"])
	}
}

def parse(String description) {
}

def push() {
	sendEvent(name: "switch", value: "on", isStateChange: true, display: false)
	sendEvent(name: "switch", value: "off", isStateChange: true, display: false)
	sendEvent(name: "momentary", value: "pushed", isStateChange: true)
}

def on() {
	push()
}

def off() {
	push()
}