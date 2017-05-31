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
	definition (name: "Motion Detector", namespace: "smartthings", author: "SmartThings") {
		capability "Motion Sensor"
		capability "Sensor"

		fingerprint profileId: "0104", deviceId: "0402", inClusters: "0000,0001,0003,0009,0500"
	}

	// simulator metadata
	simulator {
		status "active": "zone report :: type: 19 value: 0031"
		status "inactive": "zone report :: type: 19 value: 0030"
	}

	// UI tile definitions
	tiles {
		standardTile("motion", "device.motion", width: 2, height: 2) {
			state("active", label:'motion', icon:"st.motion.motion.active", backgroundColor:"#00A0DC")
			state("inactive", label:'no motion', icon:"st.motion.motion.inactive", backgroundColor:"#cccccc")
		}

		main "motion"
		details "motion"
	}
}

// Parse incoming device messages to generate events
def parse(String description) {
	def name = null
	def value = description
	def descriptionText = null
	if (zigbee.isZoneType19(description)) {
		name = "motion"
		def isActive = zigbee.translateStatusZoneType19(description)
		value = isActive ? "active" : "inactive"
		descriptionText = isActive ? "${device.displayName} detected motion" : "${device.displayName} motion has stopped"
	}

	def result = createEvent(
		name: name,
		value: value,
		descriptionText: descriptionText
	)

	log.debug "Parse returned ${result?.descriptionText}"
	return result
}
