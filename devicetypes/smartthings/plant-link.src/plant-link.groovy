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
 *	PlantLink
 *
 *	Author: SmartThings
 *	Date: 2013-12-17
 */
metadata {

	definition (name: "Plant Link", namespace: "smartthings", author: "SmartThings") {
		capability "Relative Humidity Measurement"
		capability "Battery"
		capability "Sensor"
		capability "Health Check"

		fingerprint profileId: "0104", inClusters: "0000,0003,0405,FC08", outClusters: "0003"
		fingerprint endpoint: "1", profileId: "0104", inClusters: "0000,0001,0003,0B04", outClusters: "0003", manufacturer: "", model: "", deviceJoinName: "OSO Technologies PlantLink Soil Moisture Sensor"
	}

	tiles {
		valueTile("humidity", "device.humidity", width: 2, height: 2) {
			state("humidity", label:'${currentValue}%', unit:"",
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
		valueTile("battery", "device.battery") {
			state "battery", label:'${currentValue}%', unit:""
		}

		main(["humidity", "battery"])
		details(["humidity", "battery"])
	}
}

def updated() {
	// Device-Watch allows 2 check-in misses from device
	sendEvent(name: "checkInterval", value: 2 * 15 * 60 + 2 * 60, displayed: false, data: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID])
}

// Parse incoming device messages to generate events
def parse(String description) {
	log.debug "Parse description $description"
	def map = [:]
	if (description?.startsWith("read attr -")) {
		def descMap = parseDescriptionAsMap(description)
		log.debug "Desc Map: $descMap"
		if (descMap.cluster == "0405" && descMap.attrId == "0000") {
			log.debug "Humidity"
			map.name = "humidity"
			map.value = calculateHumidity(descMap.value)
		} else if (descMap.cluster == "0001" && descMap.attrId == "0000") {
			log.debug "Battery"
			map.name = "battery"
			map.value = calculateBattery(descMap.value)
		}
	}

	def result = null
	if (map) {
		result = createEvent(map)
	}
	log.debug "Parse returned $map"
	return result
}

def parseDescriptionAsMap(description) {
	(description - "read attr - ").split(",").inject([:]) { map, param ->
		def nameAndValue = param.split(":")
		map += [(nameAndValue[0].trim()):nameAndValue[1].trim()]
	}
}

private calculateHumidity(value) {
	//adc reading of 0x1ec0 produces a plant fuel level near 0
	//adc reading of 0x2100 produces a plant fuel level near 100%
	def range = 576 //8448 - 7872
	def percent = (Integer.parseInt(value, 16) / range) * 100
	percent = Math.max(0.0, Math.min(percent, 100.0))
	percent
}

private calculateBattery(value) {
	def min = 2300
	def percent = (Integer.parseInt(value, 16) - min) / 10
	// Make sure our percentage is between 0 - 100
	percent = Math.max(0.0, Math.min(percent, 100.0))
	percent
}

private hex(value) {
	new BigInteger(Math.round(value).toString()).toString(16)
}
