/*
 *  Copyright 2016 SmartThings
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
	definition (name: "Mobile Presence", namespace: "smartthings", author: "SmartThings", ocfDeviceType: "x.com.st.d.mobile.presence") {
		capability "Presence Sensor"
		capability "Occupancy Sensor"
		capability "Sensor"
	}

	simulator {
		status "present": "presence: 1"
		status "not present": "presence: 0"
		status "occupied": "occupancy: 1"
		status "unoccupied": "occupancy: 0"
	}

	tiles {
		standardTile("presence", "device.presence", width: 2, height: 2, canChangeBackground: true) {
			state("present", labelIcon:"st.presence.tile.mobile-present", backgroundColor:"#00A0DC")
			state("not present", labelIcon:"st.presence.tile.mobile-not-present", backgroundColor:"#ffffff")
		}
		main "presence"
		details "presence"
	}
}

def parse(String description) {
	def value = parseValue(description)

	/*
	 * When 'not present' event received (left case)
	 *     -> If occupancy value is not 'unoccupied', occupancy value should be 'unoccupied' before posting 'not present'
	 * When 'occupied' event received (inside case)
	 *     -> If presence value is not 'present', presence value should be 'present' before posting 'occupied'
	 */
	switch(value) {
		case "not present":
        	if (device.currentState("occupancy") != "unoccupied") sendEvent(generateEvent("occupancy: 0"))
            break
        case "occupied":
        	if (device.currentState("presence") != "present") sendEvent(generateEvent("presence: 1"))
            break
	}

    sendEvent(generateEvent(description))
}

private generateEvent(String description) {
	log.debug "description: $description"
	def value = parseValue(description)
	def name = parseName(description)
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
	log.debug "GenerateEvent returned $results.descriptionText"

	return results
}

private String parseName(String description) {
	if (description?.startsWith("presence: ")) {
		return "presence"
	} else if (description?.startsWith("occupancy: ")) {
		return "occupancy"
	}
	null
}

private String parseValue(String description) {
	switch(description) {
		case "presence: 1": return "present"
		case "presence: 0": return "not present"
		case "occupancy: 1": return "occupied"
		case "occupancy: 0": return "unoccupied"
		default: return description
	}
}

private parseDescriptionText(String linkText, String value, String description) {
	switch(value) {
		case "present": return "{{ linkText }} has arrived"
		case "not present": return "{{ linkText }} has left"
		case "occupied": return "{{ linkText }} is inside"
		case "unoccupied": return "{{ linkText }} is away"
		default: return value
	}
}

private getState(String value) {
	switch(value) {
		case "present": return "arrived"
		case "not present": return "left"
		case "occupied": return "inside"
		case "unoccupied": return "away"
		default: return value
	}
}
