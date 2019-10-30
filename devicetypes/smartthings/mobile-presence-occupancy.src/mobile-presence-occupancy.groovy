/*
 *  Copyright 2018 SmartThings
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not
 *  use this file except in compliance with the License. You may obtain a copy
 *  of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *  WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 *  License for the specific language governing permissions and limitations
 *  under the License.
 */

metadata {
	definition (name: "Mobile Presence Occupancy", namespace: "smartthings", author: "SmartThings", ocfDeviceType: "x.com.st.d.mobile.presence") {
		capability "Presence Sensor"
		capability "Occupancy Sensor"
		capability "Sensor"
	}

	simulator {
		status "not present": "presence: 0"
		status "present": "presence: 1"
		status "unoccupied": "occupancy: 0"
		status "occupied": "occupancy: 1"
	}

	tiles {
		standardTile("presence", "device.presence", width: 2, height: 2, canChangeBackground: true) {
			state("present", labelIcon:"st.presence.tile.mobile-present", backgroundColor:"#00A0DC")
			state("not present", labelIcon:"st.presence.tile.mobile-not-present", backgroundColor:"#ffffff")
		}
		standardTile("occupancy", "device.occupancy", width: 2, height: 2, canChangeBackground: true) {
			state ("occupied", labelIcon: "st.presence.tile.mobile-present", backgroundColor: "#00A0DC")
			state ("unoccupied", labelIcon: "st.presence.tile.mobile-not-present", backgroundColor:"#ffffff")
		}
		main "presence"
		details(["presence", "occupancy"])
	}
}

def parse(String description) {
	def name = parseName(description)
	def value = parseValue(description)
	def linkText = getLinkText(device)
	def descriptionText = parseDescriptionText(linkText, value, description)
	def handlerName = getState(value)
	def isStateChange = isStateChange(device, name, value)

	def results = [
    	translatable: true,
		name: name,
		value: value,
		unit: null,
		linkText: linkText,
		descriptionText: descriptionText,
		handlerName: handlerName,
		isStateChange: isStateChange,
		displayed: displayed(description, isStateChange)
	]
	log.debug "Parse returned $results.descriptionText"
	return results
}

private String parseName(String description) {
	log.debug "parseName $description"
	switch(description) {
		case "presence: 0": 
		case "presence: 1": 
 			return "presence"
		case "occupancy: 0": 
		case "occupancy: 1": 
 			return "occupancy"
	}
}

private String parseValue(String description) {
	log.debug "parseValue $description"
	switch(description) {
		case "presence: 0": return "not present"
		case "presence: 1": return "present"
		case "occupancy: 0": return "unoccupied"
		case "occupancy: 1": return "occupied"
		default: return description
	}
}

private parseDescriptionText(String linkText, String value, String description) {
	log.debug "parseDescriptionText $description"
	switch(value) {
		case "not present": return "{{ linkText }} has left"
		case "present": return "{{ linkText }} has arrived"
		case "unoccupied": return "{{ linkText }} is away"
		case "occupied": return "{{ linkText }} is inside"
		default: return value
	}
}

private getState(String value) {
	log.debug "getState $value"
	switch(value) {
		case "not present": return "left"
		case "present": return "arrived"
		case "unoccupied": return "away"
		case "occupied": return "inside"
		default: return value
	}
}
