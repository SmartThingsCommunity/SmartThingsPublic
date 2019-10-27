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
	definition (name: "SmartSense Motion", namespace: "smartthings", author: "SmartThings") {
		capability "Signal Strength"
		capability "Motion Sensor"
		capability "Sensor"
		capability "Battery"

		fingerprint profileId: "0104", deviceId: "013A", inClusters: "0000", outClusters: "0006"
		fingerprint profileId: "FC01", deviceId: "013A"
	}

	simulator {
		status "active": "zone report :: type: 19 value: 0031"
		status "inactive": "zone report :: type: 19 value: 0030"
	}

	tiles(scale: 2) {
		multiAttributeTile(name:"motion", type: "generic", width: 6, height: 4){
			tileAttribute ("device.motion", key: "PRIMARY_CONTROL") {
				attributeState "active", label:'motion', icon:"st.motion.motion.active", backgroundColor:"#53a7c0"
				attributeState "inactive", label:'no motion', icon:"st.motion.motion.inactive", backgroundColor:"#ffffff"
			}
		}
		valueTile("battery", "device.battery", decoration: "flat", inactiveLabel: false, width: 2, height: 2) {
			state "battery", label:'${currentValue}% battery', unit:""
		}

		main "motion"
		details(["motion", "battery"])
	}
}

def parse(String description) {
	def results = [:]
	if (isZoneType19(description) || !isSupportedDescription(description)) {
		results = parseBasicMessage(description)
	}
	else if (isMotionStatusMessage(description)){
		results = parseMotionStatusMessage(description)
	}

	results
}

private Map parseBasicMessage(description) {
	def name = parseName(description)
	def results = [:]
	if (name != null) {
		def value = parseValue(description)
		def linkText = getLinkText(device)
		def descriptionText = parseDescriptionText(linkText, value, description)
		def handlerName = value
		def isStateChange = isStateChange(device, name, value)

		results = [
				name           : name,
				value          : value,
				linkText       : linkText,
				descriptionText: descriptionText,
				handlerName    : handlerName,
				isStateChange  : isStateChange,
				displayed      : displayed(description, isStateChange)
		]
	}
	log.debug "Parse returned $results.descriptionText"
	return results
}

private String parseName(String description) {
	if (isSupportedDescription(description)) {
		return "motion"
	}
	null
}

private String parseValue(String description) {
	if (isZoneType19(description)) {
		if (translateStatusZoneType19(description)) {
			return "active"
		}
		else {
			return "inactive"
		}
	}

	description
}

private parseDescriptionText(String linkText, String value, String description) {
	switch(value) {
		case "active": return "$linkText detected motion"
		case "inactive": return "$linkText motion has stopped"
		default: return value
	}
}

private Boolean isMotionStatusMessage(String description) {
	// "raw:7D360000001D55FF, dni:7D36, motion:00, battery:00, powerSource:00, rssi:1D, lqi:55, other:FF" - old (incorrect dev-conn parse)
	// "raw:7D360000001D55FF, dni:7D36, motion:00, powerSource:0000, battery:00, rssi:1D, lqi:55, other:FF" - old (correct dev-conn parse)
	// "raw:7D360000001D55FF, dni:7D36, motion:00, powerSource:00, battery:00, batteryDivisor:00, rssi:1D, lqi:55, other:FF" - new
		description ==~ /raw:.*dni:.*motion:.*battery:.*powerSource:.*rssi:.*lqi:.*/ || description ==~ /raw:.*dni:.*motion:.*powerSource:.*battery:.*rssi:.*lqi:.*/
}

private List parseMotionStatusMessage(String description) {
	def results = []
	def parts = description.split(',')
	parts.each { part ->
		part = part.trim()
		if (part.startsWith('motion:')) {
			def motionResult = getMotionResult(part, description)
			if (motionResult) {
				results << motionResult
			}
		}
		else if (part.startsWith('powerSource:')) {
			def powerSourceResult = getPowerSourceResult(part, description)
			if (powerSourceResult) {
				results << powerSourceResult
			}
		}
		else if (part.startsWith('battery:')) {
			def batteryResult = getBatteryResult(part, description)
			if (batteryResult) {
				results << batteryResult
			}
		}
		else if (part.startsWith('rssi:')) {
			def rssiResult = getRssiResult(part, description)
			if (rssiResult) {
				results << rssiResult
			}
		}
		else if (part.startsWith('lqi:')) {
			def lqiResult = getLqiResult(part, description)
			if (lqiResult) {
				results << lqiResult
			}
		}
	}

	results
}

private getMotionResult(part, description) {
	def name = "motion"
	def valueString = part.split(":")[1].trim()
	def valueInt = Integer.parseInt(valueString, 16)
	def value = valueInt == 0 ? "inactive" : "active"
	def linkText = getLinkText(device)
	def descriptionText = parseDescriptionText(linkText, value, description)
	def isStateChange = isStateChange(device, name, value)

	[
		name: name,
		value: value,
		linkText: linkText,
		descriptionText: descriptionText,
		handlerName: value,
		isStateChange: isStateChange,
		displayed: displayed(description, isStateChange)
	]
}

private getPowerSourceResult(part, description) {
	def name = "powerSource"
	def valueString = part.split(":")[1].trim()
	def valueInt = Integer.parseInt(valueString, 16)
	def value = valueInt == 0 ? "battery" : "powered"
	def linkText = getLinkText(device)
	def descriptionText
	if (value == "battery") {
		descriptionText = "$linkText is ${value} powered"
	}
	else {
		descriptionText = "$linkText is plugged in"
	}
	def isStateChange = isStateChange(device, name, value)

	[
		name: name,
		value: value,
		linkText: linkText,
		descriptionText: descriptionText,
		handlerName: name,
		isStateChange: isStateChange,
		displayed: false
	]
}

private getBatteryResult(part, description) {
	def name = "battery"
	def valueString = part.split(":")[1].trim()
	def valueInt = Integer.parseInt(valueString, 16)

	// Temporarily disallow zero as a valid result b/c the current firmware has a bug where zero is only value being sent
	// This effectively disables battery reporting for this device, so needs to be removed once FW is updated
	if (valueInt == 0) return null


	def batteryDivisor = description.split(",").find {it.split(":")[0].trim() == "batteryDivisor"} ? description.split(",").find {it.split(":")[0].trim() == "batteryDivisor"}.split(":")[1].trim() : null
	def value = zigbee.parseSmartThingsBatteryValue(part, batteryDivisor)
	def unit = "%"
	def linkText = getLinkText(device)
	def descriptionText = "$linkText battery was ${value}${unit}"
	def isStateChange = isStateChange(device, name, value)

	[
		name: name,
		value: value,
		unit: unit,
		linkText: linkText,
		descriptionText: descriptionText,
		handlerName: name,
		isStateChange: isStateChange,
		displayed: false
	]
}

private getRssiResult(part, description) {
	def name = "rssi"
	def parts = part.split(":")
	if (parts.size() != 2) return null

	def valueString = parts[1].trim()
	def valueInt = Integer.parseInt(valueString, 16)
	def value = (valueInt - 128).toString()
	def linkText = getLinkText(device)
	def descriptionText = "$linkText was $value dBm"
	def isStateChange = isStateChange(device, name, value)

	[
		name: name,
		value: value,
		unit: "dBm",
		linkText: linkText,
		descriptionText: descriptionText,
		handlerName: null,
		isStateChange: isStateChange,
		displayed: false
	]
}

/**
 * Use LQI (Link Quality Indicator) as a measure of signal strength. The values
 * are 0 to 255 (0x00 to 0xFF) and higher values represent higher signal
 * strength. Return as a percentage of 255.
 *
 * Note: To make the signal strength indicator more accurate, we could combine
 * LQI with RSSI.
 */
private getLqiResult(part, description) {
	def name = "lqi"
	def parts = part.split(":")
	if (parts.size() != 2) return null

	def valueString = parts[1].trim()
	def valueInt = Integer.parseInt(valueString, 16)
	def percentageOf = 255
	def value = Math.round((valueInt / percentageOf * 100)).toString()
	def unit = "%"
	def linkText = getLinkText(device)
	def descriptionText = "$linkText Signal (LQI) was ${value}${unit}"
	def isStateChange = isStateChange(device, name, value)

	[
		name: name,
		value: value,
		unit: unit,
		linkText: linkText,
		descriptionText: descriptionText,
		handlerName: null,
		isStateChange: isStateChange,
		displayed: false
	]
}