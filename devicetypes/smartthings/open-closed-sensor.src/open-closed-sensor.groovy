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
	definition (name: "Open/Closed Sensor", namespace: "smartthings", author: "SmartThings") {
		capability "Contact Sensor"
		capability "Sensor"

		fingerprint profileId: "0104", deviceId: "0402", inClusters: "0000,0001,0003,0009,0500", outClusters: "0000"
	}

	// simulator metadata
	simulator {
		// status messages
		status "open":   "zone report :: type: 19 value: 0031"
		status "closed": "zone report :: type: 19 value: 0030"
	}

	// UI tile definitions
	tiles {
		standardTile("contact", "device.contact", width: 2, height: 2) {
			state "open", label: '${name}', icon: "st.contact.contact.open", backgroundColor: "#e86d13"
			state "closed", label: '${name}', icon: "st.contact.contact.closed", backgroundColor: "#00A0DC"
		}

		main "contact"
		details "contact"
	}
}

// Parse incoming device messages to generate events
def parse(String description) {
	def resMap
	if (description.startsWith("zone")) {
		resMap = createEvent(name: "contact", value: zigbee.parseZoneStatus(description).isAlarm1Set() ? "open" : "closed")
	}
	
	log.debug "Parse returned $resMap"
	return resMap
}
