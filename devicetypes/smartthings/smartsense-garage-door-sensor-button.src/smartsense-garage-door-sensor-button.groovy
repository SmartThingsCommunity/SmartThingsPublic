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
 *  SmartSense Garage Door Sensor Button
 *
 *  Author: SmartThings
 *  Date: 2013-03-09
 */
metadata {
	definition (name: "SmartSense Garage Door Sensor Button", namespace: "smartthings", author: "SmartThings") {
		capability "Three Axis"
		capability "Garage Door Control"
		capability "Contact Sensor"
		capability "Actuator"
		capability "Acceleration Sensor"
		capability "Signal Strength"
		capability "Temperature Measurement"
		capability "Sensor"
		capability "Battery"

		attribute "status", "string"
		attribute "buttonPress", "string"

		command "actuate"
	}

	simulator {
		status "acceleration": "acceleration: 1, rssi: 0, lqi: 0"
		status "no acceleration": "acceleration: 0, rssi: 0, lqi: 0"

		for (int i = 20; i <= 100; i += 10) {
			status "${i}F": "contactState: 0, accelerationState: 0, temp: $i F, battery: 100, rssi: 100, lqi: 255"
		}

		// kinda hacky because it depends on how it is installed
		status "x,y,z: 0,0,0": "x: 0, y: 0, z: 0, rssi: 100, lqi: 255"
		status "x,y,z: 1000,0,0": "x: 1000, y: 0, z: 0, rssi: 100, lqi: 255"
		status "x,y,z: 0,1000,0": "x: 0, y: 1000, z: 0, rssi: 100, lqi: 255"
		status "x,y,z: 0,0,1000": "x: 0, y: 0, z: 1000, rssi: 100, lqi: 255"
	}

	tiles {
		standardTile("status", "device.status", width: 2, height: 2) {
			state("closed", label:'${name}', icon:"st.doors.garage.garage-closed", action: "actuate", backgroundColor:"#00A0DC", nextState:"opening")
			state("open", label:'${name}', icon:"st.doors.garage.garage-open", action: "actuate", backgroundColor:"#e86d13", nextState:"closing")
			state("opening", label:'${name}', icon:"st.doors.garage.garage-opening", backgroundColor:"#e86d13")
			state("closing", label:'${name}', icon:"st.doors.garage.garage-closing", backgroundColor:"#00A0DC")
		}
		standardTile("contact", "device.contact") {
			state("open", label:'${name}', icon:"st.contact.contact.open", backgroundColor:"#e86d13")
			state("closed", label:'${name}', icon:"st.contact.contact.closed", backgroundColor:"#00A0DC")
		}
		standardTile("acceleration", "device.acceleration", decoration: "flat") {
			state("active", label:'${name}', icon:"st.motion.acceleration.active", backgroundColor:"#00A0DC")
			state("inactive", label:'${name}', icon:"st.motion.acceleration.inactive", backgroundColor:"#ffffff")
		}
		valueTile("temperature", "device.temperature", decoration: "flat") {
			state("temperature", label:'${currentValue}°')
		}
		valueTile("3axis", "device.threeAxis", decoration: "flat", wordWrap: false) {
			state("threeAxis", label:'${currentValue}', unit:"")
		}
		valueTile("battery", "device.battery", decoration: "flat", inactiveLabel: false) {
			state "battery", label:'${currentValue}% battery', unit:""/*, backgroundColors:[
				[value: 5, color: "#BC2323"],
				[value: 10, color: "#D04E00"],
				[value: 15, color: "#F1D801"],
				[value: 16, color: "#FFFFFF"]
			]*/
		}
		/*
		valueTile("lqi", "device.lqi", decoration: "flat", inactiveLabel: false) {
			state "lqi", label:'${currentValue}% signal', unit:""
		}
		*/

		main(["status","contact", "acceleration"])
		details(["status","contact", "acceleration", "temperature", "3axis", "battery"/*, "lqi"*/])
	}

	preferences {
		input "tempOffset", "number", title: "Temperature offset", description: "Select how many degrees to adjust the temperature.", range: "*..*", displayDuringSetup: false
	}
}

def open() {
	if (device.currentValue("status") != "open") {
		log.debug "Sending button press event to open door"
		sendEvent(name: "buttonPress", value: "true", isStateChange: true, unit: "")
	}
	else {
		log.debug "Not opening door since it is already open"
	}
}

def close() {
	if (device.currentValue("status") != "closed") {
		log.debug "Sending button press event to close door"
		sendEvent(name: "buttonPress", value: "true", isStateChange: true, unit: "")
	}
	else {
		log.debug "Not closing door since it is already closed"
	}
}

def parse(String description) {
	log.debug "parse($description)"
	def results = null

	if (!isSupportedDescription(description) || description.startsWith("zone")) {
		// Ignore this in favor of orientation-based state
		// results = parseSingleMessage(description)
	}
	else {
		results = parseMultiSensorMessage(description)
	}
	log.debug "Parse returned ${results?.descriptionText}"
	return results

}

def actuate() {
	log.debug "Sending button press event"
	sendEvent(name: "buttonPress", value: "true", isStateChange: true, unit: "")
}

private List parseMultiSensorMessage(description) {
	def results = []
	if (isAccelerationMessage(description)) {
		results = parseAccelerationMessage(description)
	}
	else if (isContactMessage(description)) {
		results = parseContactMessage(description)
	}
	else if (isRssiLqiMessage(description)) {
		results = parseRssiLqiMessage(description)
	}
	else if (isOrientationMessage(description)) {
		results = parseOrientationMessage(description)
	}

	results
}

private List parseAccelerationMessage(String description) {
	def results = []
	def parts = description.split(',')
	parts.each { part ->
		part = part.trim()
		if (part.startsWith('acceleration:')) {
			def event = getAccelerationResult(part, description)
			results << event
		}
		else if (part.startsWith('rssi:')) {
			results << getRssiResult(part, description)
		}
		else if (part.startsWith('lqi:')) {
			results << getLqiResult(part, description)
		}
	}

	results
}

private List parseContactMessage(String description) {
	def results = []
	def parts = description.split(',')
	parts.each { part ->
		part = part.trim()
		if (part.startsWith('accelerationState:')) {
			results << getAccelerationResult(part, description)
		}
		else if (part.startsWith('temp:')) {
			results << getTempResult(part, description)
		}
		else if (part.startsWith('battery:')) {
			results << getBatteryResult(part, description)
		}
		else if (part.startsWith('rssi:')) {
			results << getRssiResult(part, description)
		}
		else if (part.startsWith('lqi:')) {
			results << getLqiResult(part, description)
		}
	}

	results
}

private List parseOrientationMessage(String description) {
	def results = []
	def xyzResults = [x: 0, y: 0, z: 0]
	def parts = description.split(',')
	parts.each { part ->
		part = part.trim()
		if (part.startsWith('x:')) {
			def unsignedX = part.split(":")[1].trim().toInteger()
			def signedX = unsignedX > 32767 ? unsignedX - 65536 : unsignedX
			xyzResults.x = signedX
		}
		else if (part.startsWith('y:')) {
			def unsignedY = part.split(":")[1].trim().toInteger()
			def signedY = unsignedY > 32767 ? unsignedY - 65536 : unsignedY
			xyzResults.y = signedY
		}
		else if (part.startsWith('z:')) {
			def unsignedZ = part.split(":")[1].trim().toInteger()
			def signedZ = unsignedZ > 32767 ? unsignedZ - 65536 : unsignedZ
			xyzResults.z = signedZ
		}
		else if (part.startsWith('rssi:')) {
			results << getRssiResult(part, description)
		}
		else if (part.startsWith('lqi:')) {
			results << getLqiResult(part, description)
		}
	}

	def xyz = getXyzResult(xyzResults, description)
	results << xyz

	// Looks for Z-axis orientation as virtual contact state
	def a = xyz.value.split(',').collect{it.toInteger()}
	def absValueXY = Math.max(Math.abs(a[0]), Math.abs(a[1]))
	def absValueZ = Math.abs(a[2])
	log.debug "absValueXY: $absValueXY, absValueZ: $absValueZ"


	if (absValueZ > 825 && absValueXY < 175) {
		results << createEvent(name: "contact", value: "open", unit: "")
		results << createEvent(name: "status", value: "open", unit: "")
		results << createEvent(name: "door", value: "open", unit: "")
		log.debug "STATUS: open"
	}
	else if (absValueZ < 75 && absValueXY > 825) {
		results << createEvent(name: "contact", value: "closed", unit: "")
		results << createEvent(name: "status", value: "closed", unit: "")
		results << createEvent(name: "door", value: "closed", unit: "")
		log.debug "STATUS: closed"
	}

	results
}

private List parseRssiLqiMessage(String description) {
	def results = []
	// "lastHopRssi: 91, lastHopLqi: 255, rssi: 91, lqi: 255"
	def parts = description.split(',')
	parts.each { part ->
		part = part.trim()
		if (part.startsWith('lastHopRssi:')) {
			results << getRssiResult(part, description, true)
		}
		else if (part.startsWith('lastHopLqi:')) {
			results << getLqiResult(part, description, true)
		}
		else if (part.startsWith('rssi:')) {
			results << getRssiResult(part, description)
		}
		else if (part.startsWith('lqi:')) {
			results << getLqiResult(part, description)
		}
	}

	results
}

private getAccelerationResult(part, description) {
	def name = "acceleration"
	def value = part.endsWith("1") ? "active" : "inactive"
	def linkText = getLinkText(device)
	def descriptionText = "$linkText ${name} was $value"
	def isStateChange = isStateChange(device, name, value)

	[
		name: name,
		value: value,
		unit: null,
		linkText: linkText,
		descriptionText: descriptionText,
		handlerName: value,
		isStateChange: isStateChange,
		displayed: displayed(description, isStateChange)
	]
}

private getTempResult(part, description) {
	def name = "temperature"
	def temperatureScale = getTemperatureScale()
	def value = zigbee.parseSmartThingsTemperatureValue(part, "temp: ", temperatureScale)
	if (tempOffset) {
		value = new BigDecimal((value as float) + (tempOffset as float)).setScale(1, BigDecimal.ROUND_HALF_UP)
	}
	def linkText = getLinkText(device)
	def descriptionText = "$linkText was $value°$temperatureScale"
	def isStateChange = isTemperatureStateChange(device, name, value.toString())

	[
		name: name,
		value: value,
		unit: temperatureScale,
		linkText: linkText,
		descriptionText: descriptionText,
		handlerName: name,
		isStateChange: isStateChange,
		displayed: displayed(description, isStateChange)
	]
}

private getXyzResult(results, description) {
	def name = "threeAxis"
	def value = "${results.x},${results.y},${results.z}"
	def linkText = getLinkText(device)
	def descriptionText = "$linkText ${name} was $value"
	def isStateChange = isStateChange(device, name, value)

	[
		name: name,
		value: value,
		unit: null,
		linkText: linkText,
		descriptionText: descriptionText,
		handlerName: name,
		isStateChange: isStateChange,
		displayed: false
	]
}

private getBatteryResult(part, description) {
	def batteryDivisor = description.split(",").find {it.split(":")[0].trim() == "batteryDivisor"} ? description.split(",").find {it.split(":")[0].trim() == "batteryDivisor"}.split(":")[1].trim() : null
	def name = "battery"
	def value = zigbee.parseSmartThingsBatteryValue(part, batteryDivisor)
	def unit = "%"
	def linkText = getLinkText(device)
	def descriptionText = "$linkText ${name} was ${value}${unit}"
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

private getRssiResult(part, description, lastHop=false) {
	def name = lastHop ? "lastHopRssi" : "rssi"
	def valueString = part.split(":")[1].trim()
	def value = (Integer.parseInt(valueString) - 128).toString()
	def linkText = getLinkText(device)
	def descriptionText = "$linkText ${name} was $value dBm"

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
private getLqiResult(part, description, lastHop=false) {
	def name = lastHop ? "lastHopLqi" : "lqi"
	def valueString = part.split(":")[1].trim()
	def percentageOf = 255
	def value = Math.round((Integer.parseInt(valueString) / percentageOf * 100)).toString()
	def unit = "%"
	def linkText = getLinkText(device)
	def descriptionText = "$linkText ${name} was: ${value}${unit}"

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

private Boolean isAccelerationMessage(String description) {
	// "acceleration: 1, rssi: 91, lqi: 255"
	description ==~ /acceleration:.*rssi:.*lqi:.*/
}

private Boolean isContactMessage(String description) {
	// "contactState: 1, accelerationState: 0, temp: 14.4 C, battery: 28, rssi: 59, lqi: 255"
	description ==~ /contactState:.*accelerationState:.*temp:.*battery:.*rssi:.*lqi:.*/
}

private Boolean isRssiLqiMessage(String description) {
	// "lastHopRssi: 91, lastHopLqi: 255, rssi: 91, lqi: 255"
	description ==~ /lastHopRssi:.*lastHopLqi:.*rssi:.*lqi:.*/
}

private Boolean isOrientationMessage(String description) {
	// "x: 0, y: 33, z: 1017, rssi: 102, lqi: 255"
	description ==~ /x:.*y:.*z:.*rssi:.*lqi:.*/
}
