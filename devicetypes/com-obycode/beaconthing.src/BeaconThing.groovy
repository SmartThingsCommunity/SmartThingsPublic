/**
*  BeaconThing
*
*  Copyright 2015 obycode
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
	definition (name: "BeaconThing", namespace: "com.obycode", author: "obycode") {
		capability "Beacon"
		capability "Presence Sensor"
		capability "Sensor"

		command "setPresence"
	}

	simulator {
		status "present": "presence: 1"
		status "not present": "presence: 0"
	}

	tiles {
		standardTile("presence", "device.presence", width: 2, height: 2, canChangeBackground: true) {
			state("present", labelIcon:"st.presence.tile.present", backgroundColor:"#53a7c0")
			state("not present", labelIcon:"st.presence.tile.not-present", backgroundColor:"#ffffff")
		}
		main "presence"
		details "presence"
	}
}

// parse events into attributes
def parse(String description) {
	log.debug "Parsing '${description}'"
	// initialize to closed state
	if (description == "updated") {
		sendEvent(name: "presence", value: "not present")
	}
}

def setPresence(status) {
	log.debug "called setPresence!"
	sendEvent(name:"presence", value:status)
}
