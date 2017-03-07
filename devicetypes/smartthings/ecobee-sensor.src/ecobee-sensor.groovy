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
 *	Ecobee Sensor
 *
 *	Author: SmartThings
 */
metadata {
	definition (name: "Ecobee Sensor", namespace: "smartthings", author: "SmartThings") {
		capability "Sensor"
		capability "Temperature Measurement"
		capability "Motion Sensor"
		capability "Refresh"
	}

	tiles {
		valueTile("temperature", "device.temperature", width: 2, height: 2) {
			state("temperature", label:'${currentValue}°', unit:"F",
					backgroundColors:[
							// Celsius
							[value: 0, color: "#153591"],
							[value: 7, color: "#1e9cbb"],
							[value: 15, color: "#90d2a7"],
							[value: 23, color: "#44b621"],
							[value: 28, color: "#f1d801"],
							[value: 35, color: "#d04e00"],
							[value: 37, color: "#bc2323"],
							// Fahrenheit
							[value: 40, color: "#153591"],
							[value: 44, color: "#1e9cbb"],
							[value: 59, color: "#90d2a7"],
							[value: 74, color: "#44b621"],
							[value: 84, color: "#f1d801"],
							[value: 95, color: "#d04e00"],
							[value: 96, color: "#bc2323"]
					]
			)
		}

		standardTile("motion", "device.motion") {
			state("inactive", label:'no motion', icon:"st.motion.motion.inactive", backgroundColor:"#cccccc")
			state("active", label:'motion', icon:"st.motion.motion.active", backgroundColor:"#00A0DC")
		}

		standardTile("refresh", "device.refresh", inactiveLabel: false, decoration: "flat") {
			state "default", action:"refresh.refresh", icon:"st.secondary.refresh"
		}

		main (["temperature","motion"])
		details(["temperature","motion","refresh"])
	}
}

def refresh() {
	log.debug "refresh called"
	poll()
}

void poll() {
	log.debug "Executing 'poll' using parent SmartApp"
	parent.pollChild()

}
