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
	definition (name: "Simulated Water Sensor", namespace: "smartthings/testing", author: "SmartThings") {
		capability "Water Sensor"
		capability "Sensor"
		capability "Health Check"
        
        command "wet"
        command "dry"
	}

	simulator {
		status "wet": "water:wet"
		status "dry": "water:dry"
	}

	tiles {
		standardTile("water", "device.water", width: 2, height: 2) {
			state "dry", icon:"st.alarm.water.dry", backgroundColor:"#ffffff", action: "wet"
			state "wet", icon:"st.alarm.water.wet", backgroundColor:"#00A0DC", action: "dry"
		}
		standardTile("wet", "device.water", inactiveLabel: false, decoration: "flat") {
			state "default", label:'Wet', action:"wet", icon: "st.alarm.water.wet"
		}         
		standardTile("dry", "device.water", inactiveLabel: false, decoration: "flat") {
			state "default", label:'Dry', action:"dry", icon: "st.alarm.water.dry"
		}  
		main "water"
		details(["water","wet","dry"])
	}
}

def installed() {
	log.trace "Executing 'installed'"
	initialize()
}

def updated() {
	log.trace "Executing 'updated'"
	initialize()
}

private initialize() {
	log.trace "Executing 'initialize'"

	sendEvent(name: "DeviceWatch-DeviceStatus", value: "online")
	sendEvent(name: "healthStatus", value: "online")
	sendEvent(name: "DeviceWatch-Enroll", value: [protocol: "cloud", scheme:"untracked"].encodeAsJson(), displayed: false)
}

def parse(String description) {
	def pair = description.split(":")
	createEvent(name: pair[0].trim(), value: pair[1].trim())
}

def wet() {
	log.trace "wet()"
	sendEvent(name: "water", value: "wet")
}

def dry() {
	log.trace "dry()"
	sendEvent(name: "water", value: "dry")
}
