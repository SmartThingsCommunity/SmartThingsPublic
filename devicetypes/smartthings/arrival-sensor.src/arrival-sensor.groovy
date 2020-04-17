import groovy.json.JsonOutput

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
	definition (name: "Arrival Sensor", namespace: "smartthings", author: "SmartThings", runLocally: true, minHubCoreVersion: '000.017.0012', executeCommandsLocally: false) {
		capability "Tone"
		capability "Actuator"
		capability "Signal Strength"
		capability "Presence Sensor"
		capability "Sensor"
		capability "Battery"
		capability "Health Check"

		fingerprint profileId: "FC01", deviceId: "019A", deviceJoinName: "SmartThings Presence Sensor"
		fingerprint profileId: "FC01", deviceId: "0131", inClusters: "0000,0003", outClusters: "0003", deviceJoinName: "SmartThings Presence Sensor"
		fingerprint profileId: "FC01", deviceId: "0131", inClusters: "0000", outClusters: "0006", deviceJoinName: "SmartThings Presence Sensor"
	}

	simulator {
		status "present": "presence: 1"
		status "not present": "presence: 0"
		status "battery": "battery: 27, batteryDivisor: 0A, rssi: 100, lqi: 64"
	}

	preferences {
		section {
			image(name: 'educationalcontent', multiple: true, images: [
				"http://cdn.device-gse.smartthings.com/Arrival/Arrival1.jpg",
				"http://cdn.device-gse.smartthings.com/Arrival/Arrival2.jpg"
				])
		}
	}

	tiles {
		standardTile("presence", "device.presence", width: 2, height: 2, canChangeBackground: true) {
			state "present", labelIcon:"st.presence.tile.present", backgroundColor:"#00a0dc"
			state "not present", labelIcon:"st.presence.tile.not-present", backgroundColor:"#ffffff"
		}
		standardTile("beep", "device.beep", decoration: "flat") {
			state "beep", label:'', action:"tone.beep", icon:"st.secondary.beep", backgroundColor:"#ffffff"
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
		
		main "presence"
		details(["presence", "beep", "battery"/*, "lqi"*/])
	}
}

def beep() {
	/*
	You can make the speaker turn on for 0.5-second beeps by sending some CLI commands:

		Command: send raw, wait 7, send raw, wait 7, send raw
		Future: new packet type "st.beep"

		raw 0xFC05 {15 0A 11 00 00 15 01}
		send 0x2F7F 2 2

	where "0xABCD" is the node ID of the Smart Tag, everything else above is a constant. Except
	the "15 01" at the end of the first raw command, that sets the speaker's period (reciprocal
	of frequency). You can play with this value up or down to experiment with loudness as the
	loudness will be strongly dependent upon frequency and the enclosure that it's in. Note that
	"15 01" represents the hex number 0x0115 so a lower frequency is "16 01" (longer period) and
	a higher frequency is "14 01" (shorter period). Note that since the tag only checks its parent
	for messages every 5 seconds (while at rest) or every 3 seconds (while in motion) it will take
	up to this long from the time you send the message to the time you hear a sound.
	*/

	// Used source endpoint of 0x02 because we are using smartthings manufacturer specific cluster.
	[
		"raw 0xFC05 {15 0A 11 00 00 15 01}",
		"delay 200",
		"send 0x$zigbee.deviceNetworkId 0x02 0x$zigbee.endpointId",
		"delay 7000",
		"raw 0xFC05 {15 0A 11 00 00 15 01}",
		"delay 200",
		"send 0x$zigbee.deviceNetworkId 0x02 0x$zigbee.endpointId",
		"delay 7000",
		"raw 0xFC05 {15 0A 11 00 00 15 01}",
		"delay 200",
		"send 0x$zigbee.deviceNetworkId 0x02 0x$zigbee.endpointId",
		"delay 7000",
		"raw 0xFC05 {15 0A 11 00 00 15 01}",
		"delay 200",
		"send 0x$zigbee.deviceNetworkId 0x02 0x$zigbee.endpointId",
		"delay 7000",
		"raw 0xFC05 {15 0A 11 00 00 15 01}",
		"delay 200",
		"send 0x$zigbee.deviceNetworkId 0x02 0x$zigbee.endpointId",
	]
}

def installed() {
	// Arrival sensors only goes OFFLINE when Hub is off
	sendEvent(name: "DeviceWatch-Enroll", value: JsonOutput.toJson([protocol: "zigbee", scheme:"untracked"]), displayed: false)
}

def parse(String description) {
	def results
	if (isBatteryMessage(description)) {
		results = parseBatteryMessage(description)
	}
	else {
		results = parsePresenceMessage(description)
	}

	log.debug "Parse returned $results.descriptionText"
	results
}

private Map parsePresenceMessage(String description) {
	def name = parseName(description)
	def value = parseValue(description)
	def linkText = getLinkText(device)
	def descriptionText = parseDescriptionText(linkText, value, description)
	def handlerName = getState(value)
	def isStateChange = isStateChange(device, name, value)

	def results = [
		name: name,
		value: value,
		unit: null,
		linkText: linkText,
		descriptionText: descriptionText,
		handlerName: handlerName,
		isStateChange: isStateChange,
		displayed: displayed(description, isStateChange)
	]

	results
}

private String parseName(String description) {
	if (description?.startsWith("presence: ")) {
		return "presence"
	}
	null
}

private String parseValue(String description) {
	if (description?.startsWith("presence: "))
	{
		if (description?.endsWith("1"))
		{
			return "present"
		}
		else if (description?.endsWith("0"))
		{
			return "not present"
		}
	}

	description
}

private parseDescriptionText(String linkText, String value, String description) {
	switch(value) {
		case "present": return "$linkText has arrived"
		case "not present": return "$linkText has left"
		default: return value
	}
}

private getState(String value) {
	def state = value
	if (value == "present") {
		state = "arrived"
	}
	else if (value == "not present") {
		state = "left"
	}

	state
}

private Boolean isBatteryMessage(String description) {
	// "raw:36EF1C, dni:36EF, battery:1B, rssi:, lqi:"
	description ==~ /.*battery:.*rssi:.*lqi:.*/
}

private List parseBatteryMessage(String description) {
	def results = []
	def parts = description.split(',')
	parts.each { part ->
		part = part.trim()
		if (part.startsWith('battery:')) {
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

private getBatteryResult(part, description) {
	def batteryDivisor = description.split(",").find {it.split(":")[0].trim() == "batteryDivisor"} ? description.split(",").find {it.split(":")[0].trim() == "batteryDivisor"}.split(":")[1].trim() : null
	def name = "battery"
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
		//displayed: displayed(description, isStateChange)
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
		//displayed: displayed(description, isStateChange)
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
		//displayed: displayed(description, isStateChange)
		displayed: false
	]
}
