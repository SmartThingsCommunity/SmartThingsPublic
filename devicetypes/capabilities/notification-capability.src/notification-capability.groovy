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
	definition (name: "Notification Capability", namespace: "capabilities", author: "SmartThings") {
		capability "Notification"
	}

	// simulator metadata
	simulator {
		// status messages
		// none

		// reply messages
        //reply "polling":"poll:polling"
	}

	// UI tile definitions
	/**tiles {
		standardTile("poll", "device.poll", width: 2, height: 2, canChangeIcon: true) {
			state "polling", label: '${name}', action: "tone.silent", icon: "st.switches.switch.on", backgroundColor: "#79b821"
		}
		main "poll"
		details "poll"
	}**/
}

def parse(String description) {
	def pair = description.split(":")
	createEvent(name: pair[0].trim(), value: pair[1].trim())
}

def deviceNotification(note) {
    note
}