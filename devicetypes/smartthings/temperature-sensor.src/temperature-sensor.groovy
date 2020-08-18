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
	definition (name: "Temperature Sensor", namespace: "smartthings", author: "SmartThings") {
		capability "Temperature Measurement"
		capability "Relative Humidity Measurement"
		capability "Sensor"

		fingerprint profileId: "0104", deviceId: "0302", inClusters: "0000,0001,0003,0009,0402,0405", deviceJoinName: "Temperature Sensor"
	}

	// simulator metadata
	simulator {
		for (int i = 0; i <= 100; i += 10) {
			status "${i}F": "temperature: $i F"
		}

		for (int i = 0; i <= 100; i += 10) {
			status "${i}%": "humidity: ${i}%"
		}
	}

	// UI tile definitions
	tiles {
		valueTile("temperature", "device.temperature", width: 2, height: 2) {
			state("temperature", label:'${currentValue}°',
				backgroundColors:[
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
		valueTile("humidity", "device.humidity") {
			state "humidity", label:'${currentValue}%', unit:""
		}

		main(["temperature", "humidity"])
		details(["temperature", "humidity"])
	}
}

// Parse incoming device messages to generate events
def parse(String description) {
	def name = parseName(description)
	def value = parseValue(description)
	def unit = name == "temperature" ? getTemperatureScale() : (name == "humidity" ? "%" : null)
	def result = createEvent(name: name, value: value, unit: unit)
	log.debug "Parse returned ${result?.descriptionText}"
	return result
}

private String parseName(String description) {
	if (description?.startsWith("temperature: ")) {
		return "temperature"
	} else if (description?.startsWith("humidity: ")) {
		return "humidity"
	}
	null
}

private String parseValue(String description) {
	if (description?.startsWith("temperature: ")) {
		return zigbee.parseHATemperatureValue(description, "temperature: ", getTemperatureScale())
	} else if (description?.startsWith("humidity: ")) {
		def pct = (description - "humidity: " - "%").trim()
		if (pct.isNumber()) {
			return Math.round(new BigDecimal(pct)).toString()
		}
	}
	null
}
