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
	definition (name: "Simulated Water Valve", namespace: "smartthings/testing", author: "SmartThings") {
		capability "Actuator"
		capability "Valve"
		capability "Sensor"
	}

	// tile definitions
	tiles {
		standardTile("contact", "device.contact", width: 2, height: 2, canChangeIcon: true) {
			state "closed", label: '${name}', action: "valve.open", icon: "st.valves.water.closed", backgroundColor: "#ffffff"
			state "open", label: '${name}', action: "valve.close", icon: "st.valves.water.open", backgroundColor: "#00A0DC"
		}
		standardTile("refresh", "device.switch", inactiveLabel: false, decoration: "flat") {
			state "default", label:'', action:"refresh.refresh", icon:"st.secondary.refresh"
		}

		main "contact"
		details(["contact","refresh"])
	}
}

def installed() {
	sendEvent(name: "contact", value: "closed")
}

def open() {
	sendEvent(name: "contact", value: "open")
}

def close() {
	sendEvent(name: "contact", value: "closed")
}
