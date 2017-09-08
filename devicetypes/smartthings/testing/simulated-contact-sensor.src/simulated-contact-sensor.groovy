/**
 *  Copyright 2014 SmartThings
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
	definition (name: "Simulated Contact Sensor", namespace: "smartthings/testing", author: "bob") {
		capability "Contact Sensor"
		capability "Sensor"

		command "open"
		command "close"
	}

	simulator {
		status "open": "contact:open"
		status "closed": "contact:closed"
	}

	tiles {
		standardTile("contact", "device.contact", width: 2, height: 2) {
			state("closed", label:'${name}', icon:"st.contact.contact.closed", backgroundColor:"#00A0DC", action: "open")
			state("open", label:'${name}', icon:"st.contact.contact.open", backgroundColor:"#e86d13", action: "close")
		}
		main "contact"
		details "contact"
	}
}

def parse(String description) {
	def pair = description.split(":")
	createEvent(name: pair[0].trim(), value: pair[1].trim())
}

def open() {
	log.trace "open()"
	sendEvent(name: "contact", value: "open")
}

def close() {
	log.trace "close()"
    sendEvent(name: "contact", value: "closed")
}