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
	definition (name: "Motion Sensor Capability", namespace: "capabilities", author: "SmartThings") {
		capability "Motion Sensor"
	}

	simulator {
		status "active": "motion:active"
		status "inactive": "motion:inactive"
	}

	tiles {
		standardTile("motion", "device.motion", width: 2, height: 2) {
			state("inactive", label:'no motion', icon:"st.motion.motion.inactive", backgroundColor:"#ffffff")
			state("active", label:'motion', icon:"st.motion.motion.active", backgroundColor:"#53a7c0")
		}
		main "motion"
		details "motion"
	}
}

def parse(String description) {
	def pair = description.split(":")
	createEvent(name: pair[0].trim(), value: pair[1].trim())
}
