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
	definition (name: "Acceleration Sensor Capability", namespace: "capabilities", author: "SmartThings") {
		capability "Acceleration Sensor"
	}

	simulator {
		status "active": "acceleration:active"
		status "inactive": "acceleration:inactive"
	}

	tiles {
		standardTile("acceleration", "device.acceleration", width: 2, height: 2) {
			state("inactive", label:'${name}', icon:"st.motion.acceleration.inactive", backgroundColor:"#cccccc")
			state("active", label:'${name}', icon:"st.motion.acceleration.active", backgroundColor:"#00A0DC")
		}

		main "acceleration"
		details "acceleration"
	}
}

def parse(String description) {
	def pair = description.split(":")
	createEvent(name: pair[0].trim(), value: pair[1].trim())
}
