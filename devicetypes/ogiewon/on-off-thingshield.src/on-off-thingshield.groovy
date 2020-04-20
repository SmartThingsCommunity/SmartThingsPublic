/**
 *  Copyright 2015 SmartThings (original)
 *  
 *  2017-02-11 Dan Ogorchock - Since SmartThings has abandoned the ThingShield, I have created a new
 *                             Arduino "SmartThings" library with support for the ThingShield, 
 *                             W5100 Ethernet Shield, and the NodeMCU ESP8266 Board for connectivity to 
 *                             the SmartThings cloud.  This file is to be used with the On/Off ThingShield
 *                             example found in this new library which can be found at
 *
 *                             https://github.com/DanielOgorchock/ST_Anything/tree/master/Arduino/libraries/SmartThings
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
	definition (name: "On/Off ThingShield", namespace: "ogiewon", author: "Dan Ogorchock") {
		capability "Actuator"
		capability "Switch"
		capability "Sensor"
	}

	// Simulator metadata
	simulator {
		status "on":  "catchall: 0104 0000 01 01 0040 00 0A21 00 00 0000 0A 00 0A6F6E"
		status "off": "catchall: 0104 0000 01 01 0040 00 0A21 00 00 0000 0A 00 0A6F6666"

		// reply messages
		reply "raw 0x0 { 00 00 0a 0a 6f 6e }": "catchall: 0104 0000 01 01 0040 00 0A21 00 00 0000 0A 00 0A6F6E"
		reply "raw 0x0 { 00 00 0a 0a 6f 66 66 }": "catchall: 0104 0000 01 01 0040 00 0A21 00 00 0000 0A 00 0A6F6666"
	}

	// UI tile definitions
	tiles {
		standardTile("switch", "device.switch", width: 2, height: 2, canChangeIcon: true, canChangeBackground: true) {
			state "on", label: '${name}', action: "switch.off", icon: "st.switches.switch.on", backgroundColor: "#79b821"
			state "off", label: '${name}', action: "switch.on", icon: "st.switches.switch.off", backgroundColor: "#ffffff"
		}

		main "switch"
		details "switch"
	}
}

// Parse incoming device messages to generate events
def parse(String description) {
	def value = zigbee.parse(description)?.text
	def name = value in ["on","off"] ? "switch" : null
	def result = createEvent(name: name, value: value)
	log.debug "Parse returned ${result?.descriptionText}"
	return result
}

// Commands sent to the device
def on() {
	zigbee.smartShield(text: "on").format()
}

def off() {
	zigbee.smartShield(text: "off").format()
}