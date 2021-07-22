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

import groovy.transform.Field

@Field final TemperatureMeasurementCluster = 0x0402
@Field final MeasuredValue = 0x0000

metadata {
	definition (name: "Smartenit Motion Sensor", namespace: "Smartenit", author: "SmartThings", mnmn: "SmartThings") {
		capability "Actuator"
		capability "Motion Sensor"
		capability "Sensor"
		capability "Temperature Measurement"
		capability "Refresh"

		fingerprint manufacturer: "Compacta", model: "ZBMS3-1", deviceJoinName: "Smartenit Motion Sensor" 
	}
}

def refresh() {
	log.debug "refreshing"
	return zigbee.readAttribute(TemperatureMeasurementCluster, MeasuredValue, [destEndpoint: 0x0003])
}

// Parse incoming device messages to generate events
def parse(String description) {
	log.debug "$description"

	def descriptionText = null
	Map eventDescMap = zigbee.parseDescriptionAsMap(description)
	def event = zigbee.getEvent(description)
	log.debug "EventDescMap: ${eventDescMap}"
	log.debug "event: ${event}"

	if (zigbee.isZoneType19(description)) {
		def isActive = zigbee.translateStatusZoneType19(description)
		def value = isActive ? "active" : "inactive"
		descriptionText = isActive ? "${device.displayName} detected motion" : "${device.displayName} motion has stopped"
		return createEvent(name: "motion", value: value, descriptionText: descriptionText)
	} else if (event && (event.name == "temperature")) {
		descriptionText = "${device.displayName} is reporting a temperature of ${event.value} ${event.unit}"
		return createEvent(name: event.name, value: event.value, unit: event.unit, descriptionText: descriptionText)
	} else {
		return [:]
	}
}