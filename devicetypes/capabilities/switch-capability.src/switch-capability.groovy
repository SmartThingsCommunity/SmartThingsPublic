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
	definition (name: "Switch Capability", namespace: "capabilities", author: "SmartThings") {
		capability "Switch"
	}

	// simulator metadata
	simulator {
		// status messages
		status "on": "switch:on"
		status "off": "switch:off"

		// reply messages
		reply "on": "switch:on"
		reply "off": "switch:off"
	}

	// UI tile definitions
	tiles {
		standardTile("switch", "device.switch", width: 2, height: 2, canChangeIcon: true) {
			state "off", label: '${name}', action: "switch.on", icon: "st.switches.switch.off", backgroundColor: "#ffffff"
			state "on", label: '${name}', action: "switch.off", icon: "st.switches.switch.on", backgroundColor: "#00A0DC"
		}
		main "switch"
		details "switch"
	}
}

def parse(String description) {
	def pair = description.split(":")
	createEvent(name: pair[0].trim(), value: pair[1].trim())
}

def on() {
	'on'
}

def off() {
	'off'
}
