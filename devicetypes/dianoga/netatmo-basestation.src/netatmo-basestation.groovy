/**
 *  netatmo-basestation
 *
 *  Copyright 2014 Brian Steere
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
	definition (name: "Netatmo Basestation", namespace: "dianoga", author: "Brian Steere") {
		capability "Sensor"
		capability "Relative Humidity Measurement"
		capability "Temperature Measurement"

		attribute "carbonDioxide", "string"
		attribute "noise", "string"
		attribute "pressure", "string"
	}

	simulator {
		// TODO: define status and reply messages here
	}

	tiles {
		valueTile("temperature", "device.temperature", width: 2, height: 2) {
 			state("temperature", label: '${currentValue}°', backgroundColors: [
 				[value: 31, color: "#153591"],
 				[value: 44, color: "#1e9cbb"],
 				[value: 59, color: "#90d2a7"],
 				[value: 74, color: "#44b621"],
 				[value: 84, color: "#f1d801"],
 				[value: 95, color: "#d04e00"],
 				[value: 96, color: "#bc2323"]
 				]
 				)
 		}
 		valueTile("humidity", "device.humidity", inactiveLabel: false) {
 			state "humidity", label:'${currentValue}%', unit:"Humidity"
 		}
 		valueTile("carbonDioxide", "device.carbonDioxide", inactiveLabel: false) {
 			state "carbonDioxide", label:'${currentValue}ppm', unit:"CO2", backgroundColors: [
 				[value: 600, color: "#44B621"],
                [value: 999, color: "#ffcc00"],
                [value: 1000, color: "#e86d13"]
 				]
 		}
 		valueTile("noise", "device.noise", inactiveLabel: false) {
 			state "noise", label:'${currentValue}db', unit:"Noise"
 		}
 		valueTile("pressure", "device.pressure", inactiveLabel: false) {
 			state "pressure", label:'${currentValue}mbar', unit:"Pressure"
 		}
 		standardTile("refresh", "device.pressure", inactiveLabel: false, decoration: "flat") {
 			state "default", action:"device.poll", icon:"st.secondary.refresh"
 		}
 		main(["temperature", "humidity", "carbonDioxide", "noise", "pressure"])
 		details(["temperature", "humidity", "carbonDioxide", "noise", "pressure", "refresh"])
	}
}

// parse events into attributes
def parse(String description) {
	log.debug "Parsing '${description}'"
	// TODO: handle 'humidity' attribute
	// TODO: handle 'temperature' attribute
	// TODO: handle 'carbonDioxide' attribute
	// TODO: handle 'noise' attribute
	// TODO: handle 'pressure' attribute

}

def poll() {
	parent.poll()
}