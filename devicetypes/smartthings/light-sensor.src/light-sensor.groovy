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
	definition (name: "Light Sensor", namespace: "smartthings", author: "SmartThings") {
		capability "Illuminance Measurement"
		capability "Sensor"

		fingerprint profileId: "0104", deviceId: "0106", inClusters: "0000,0001,0003,0009,0400"
	}

	// simulator metadata
	simulator {
		status "dark": "illuminance: 8"
		status "light": "illuminance: 300"
		status "bright": "illuminance: 1000"
	}

	// UI tile definitions
	tiles(scale: 2) {
		multiAttributeTile(name:"illuminance", type: "generic", width: 6, height: 4){
			tileAttribute("device.illuminance", key: "PRIMARY_CONTROL") {
				attributeState("illuminance", label:'${currentValue}', unit:"lux",
					backgroundColors:[
						[value: 9, color: "#767676"],
						[value: 315, color: "#ffa81e"],
						[value: 1000, color: "#fbd41b"]
					]
				)
			}
		}

		main "illuminance"
		details "illuminance"
	}
}

// Parse incoming device messages to generate events
def parse(String description) {
	def result
	if (description?.startsWith("illuminance: ")) {
		def raw = description - "illuminance: "
		if (raw.isNumber()) {
			result = createEvent(
				name:  "illuminance",
				value: Math.round(zigbee.lux(raw as Integer)).toString(),
				unit:  "lux"
			)
		}
	}
	log.debug "Parse returned ${result?.descriptionText}"
	return result
}
