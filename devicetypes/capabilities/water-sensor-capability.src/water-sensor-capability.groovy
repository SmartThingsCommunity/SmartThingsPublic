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
	definition (name: "Water Sensor Capability", namespace: "capabilities", author: "SmartThings") {
		capability "Water Sensor"
	}

	simulator {
		status "wet": "water:wet"
		status "dry": "water:dry"
	}

	tiles {
		standardTile("water", "device.water", width: 2, height: 2) {
			state "dry", icon:"st.alarm.water.dry", backgroundColor:"#ffffff"
			state "wet", icon:"st.alarm.water.wet", backgroundColor:"#00A0DC"
		}

		main "water"
		details "water"
	}
}

def parse(String description) {
	def pair = description.split(":")
	createEvent(name: pair[0].trim(), value: pair[1].trim())
}
