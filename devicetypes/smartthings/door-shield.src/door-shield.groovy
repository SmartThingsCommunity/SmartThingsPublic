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
	definition (name: "Door Shield", namespace: "smartthings", author: "SmartThings") {

		command "open"
	}

	// simulator metadata
	simulator {
		// status messages
		status "ping": "catchall: 0104 0000 01 01 0040 00 6A67 00 00 0000 0A 00 0A70696E67"
		status "response": "catchall: 0104 0000 01 01 0040 00 0A21 00 00 0000 0A 00 0A4F4D4E4F4D4E4F4D4E4F4D"
	}

	// UI tile definitions
	tiles {
		standardTile("shield", "device.shield", width: 2, height: 2, canChangeBackground: true) {
			state(name:"default", action:"open", icon:"st.shields.shields.door-shield", backgroundColor:"#ffffff")
		}

		main "shield"
		details "shield"
	}
}

// Parse incoming device messages to generate events
def parse(String description) {
	def value = zigbee.parse(description)?.text
	def name = value && value != "ping" ? "response" : null
	def result = createEvent(name: name, value: value)
	log.debug "Parse returned ${result?.descriptionText}"
	return result
}

// Commands sent to the device
def open() {
	zigbee.smartShield(text: "open sesame").format()
}