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
	definition (name: "Spark", namespace: "smartthings", author: "SmartThings") {
		capability "Switch"
	}


	// tile definitions
	tiles {
		standardTile("switch", "device.switch", width: 2, height: 2, canChangeIcon: true) {
			state "on", label: '${name}', action: "switch.off", icon: "st.switches.switch.on", backgroundColor: "#00A0DC"
			state "off", label: '${name}', action: "switch.on", icon: "st.switches.switch.off", backgroundColor: "#ffffff"
		}

		main "switch"
		details "switch"
	}
}

def parse(String description) {
	log.error "This device does not support incoming events"
	return null
}

def on() {
	put 'turnOn'
}

def off() {
	put 'turnOff'
}

private put(action) {
	// TODO: will this be configurable by user?
	def apiKey = "fb91rfPFS84wmzH3"

	rest(
			method: 'PUT',
			endpoint: "http://sprk.io",
			path: "/device/${device.deviceNetworkId}/${action}",
			query: [api_key: apiKey]
	)
}
