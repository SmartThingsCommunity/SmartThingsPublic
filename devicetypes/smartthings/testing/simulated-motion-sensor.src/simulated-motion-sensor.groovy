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
	definition (name: "Simulated Motion Sensor", namespace: "smartthings/testing", author: "bob") {
		capability "Motion Sensor"
		capability "Sensor"

		command "active"
		command "inactive"
	}

	simulator {
		status "active": "motion:active"
		status "inactive": "motion:inactive"
	}

	tiles {
		standardTile("motion", "device.motion", width: 2, height: 2) {
			state("inactive", label:'no motion', icon:"st.motion.motion.inactive", backgroundColor:"#cccccc", action: "active")
			state("active", label:'motion', icon:"st.motion.motion.active", backgroundColor:"#00A0DC", action: "inactive")
		}
		main "motion"
		details "motion"
	}
}

def parse(String description) {
	def pair = description.split(":")
	createEvent(name: pair[0].trim(), value: pair[1].trim())
}

def active() {
	log.trace "active()"
	sendEvent(name: "motion", value: "active")
}

def inactive() {
	log.trace "inactive()"
    sendEvent(name: "motion", value: "inactive")
}
