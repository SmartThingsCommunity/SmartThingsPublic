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
import physicalgraph.zigbee.clusters.iaszone.ZoneStatus
import physicalgraph.zigbee.zcl.DataType

metadata {
	definition(name: "SmartSense Multi Sensor", namespace: "smartthings", author: "SmartThings") {

		capability "Three Axis"
		capability "Battery"
		capability "Configuration"
		capability "Sensor"
		capability "Contact Sensor"
		capability "Acceleration Sensor"
		capability "Refresh"
		capability "Temperature Measurement"
		capability "Health Check"

		command "enrollResponse"
		fingerprint inClusters: "0000,0001,0003,0402,0500,0020,0B05,FC02", outClusters: "0019", manufacturer: "CentraLite", model: "3320"
		fingerprint inClusters: "0000,0001,0003,0402,0500,0020,0B05,FC02", outClusters: "0019", manufacturer: "CentraLite", model: "3321"
		fingerprint inClusters: "0000,0001,0003,0402,0500,0020,0B05,FC02", outClusters: "0019", manufacturer: "CentraLite", model: "3321-S", deviceJoinName: "Multipurpose Sensor"
		fingerprint inClusters: "0000,0001,0003,000F,0020,0402,0500,FC02", outClusters: "0019", manufacturer: "SmartThings", model: "multiv4", deviceJoinName: "Multipurpose Sensor"

		attribute "status", "string"
	}

	simulator {
		status "open": "zone report :: type: 19 value: 0031"
		status "closed": "zone report :: type: 19 value: 0030"

		status "acceleration": "acceleration: 1"
		status "no acceleration": "acceleration: 0"

		for (int i = 10; i <= 50; i += 10) {
			status "temp ${i}C": "contactState: 0, accelerationState: 0, temp: $i C, battery: 100"
		}

		// kinda hacky because it depends on how it is installed
		status "x,y,z: 0,0,0": "x: 0, y: 0, z: 0"
		status "x,y,z: 1000,0,0": "x: 1000, y: 0, z: 0"
		status "x,y,z: 0,1000,0": "x: 0, y: 1000, z: 0"
		status "x,y,z: 0,0,1000": "x: 0, y: 0, z: 1000"
	}
	preferences {
		section {
			image(name: 'educationalcontent', multiple: true, images: [
					"http://cdn.device-gse.smartthings.com/Multi/Multi1.jpg",
					"http://cdn.device-gse.smartthings.com/Multi/Multi2.jpg",
					"http://cdn.device-gse.smartthings.com/Multi/Multi3.jpg",
					"http://cdn.device-gse.smartthings.com/Multi/Multi4.jpg"
			])
		}
		section {
			input title: "Temperature Offset", description: "This feature allows you to correct any temperature variations by selecting an offset. Ex: If your sensor consistently reports a temp that's 5 degrees too warm, you'd enter '-5'. If 3 degrees too cold, enter '+3'.", displayDuringSetup: false, type: "paragraph", element: "paragraph"
			input "tempOffset", "number", title: "Degrees", description: "Adjust temperature by this many degrees", range: "*..*", displayDuringSetup: false
		}
		section {
			input("garageSensor", "enum", title: "Do you want to use this sensor on a garage door?", description: "Tap to set", options: ["Yes", "No"], defaultValue: "No", required: false, displayDuringSetup: false)
		}
	}

	tiles(scale: 2) {
		multiAttributeTile(name: "status", type: "generic", width: 6, height: 4) {
			tileAttribute("device.status", key: "PRIMARY_CONTROL") {
				attributeState "open", label: 'Open', icon: "st.contact.contact.open", backgroundColor: "#e86d13"
				attributeState "closed", label: 'Closed', icon: "st.contact.contact.closed", backgroundColor: "#00a0dc"
				attributeState "garage-open", label: 'Open', icon: "st.doors.garage.garage-open", backgroundColor: "#e86d13"
				attributeState "garage-closed", label: 'Closed', icon: "st.doors.garage.garage-closed", backgroundColor: "#00a0dc"
			}
		}
		standardTile("contact", "device.contact", width: 2, height: 2) {
			state("open", label: 'Open', icon: "st.contact.contact.open", backgroundColor: "#e86d13")
			state("closed", label: 'Closed', icon: "st.contact.contact.closed", backgroundColor: "#00a0dc")
		}
		standardTile("acceleration", "device.acceleration", width: 2, height: 2) {
			state("active", label: 'Active', icon: "st.motion.acceleration.active", backgroundColor: "#00a0dc")
			state("inactive", label: 'Inactive', icon: "st.motion.acceleration.inactive", backgroundColor: "#cccccc")
		}
		valueTile("temperature", "device.temperature", width: 2, height: 2) {
			state("temperature", label: '${currentValue}°',
					backgroundColors: [
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
		valueTile("battery", "device.battery", decoration: "flat", inactiveLabel: false, width: 2, height: 2) {
			state "battery", label: '${currentValue}% battery', unit: ""
		}
		standardTile("refresh", "device.refresh", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "default", action: "refresh.refresh", icon: "st.secondary.refresh"
		}


		main(["status", "acceleration", "temperature"])
		details(["status", "acceleration", "temperature", "battery", "refresh"])
	}
}

def parse(String description) {
	def maps = []
	maps << zigbee.getEvent(description)
	if (!maps[0]) {
		maps = []
		if (description?.startsWith('zone status')) {
			maps += parseIasMessage(description)
		} else {
			Map descMap = zigbee.parseDescriptionAsMap(description)
			if (descMap?.clusterInt == 0x0001 && descMap.commandInt != 0x07 && descMap?.value) {
				maps << getBatteryResult(Integer.parseInt(descMap.value, 16))
			} else if (descMap?.clusterInt == zigbee.TEMPERATURE_MEASUREMENT_CLUSTER && descMap.commandInt == 0x07) {
				if (descMap.data[0] == "00") {
					log.debug "TEMP REPORTING CONFIG RESPONSE: $descMap"
					sendEvent(name: "checkInterval", value: 60 * 12, displayed: false, data: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID])
				} else {
					log.warn "TEMP REPORTING CONFIG FAILED- error code: ${descMap.data[0]}"
				}
			} else if (descMap?.clusterInt == zigbee.IAS_ZONE_CLUSTER && descMap.attrInt == zigbee.ATTRIBUTE_IAS_ZONE_STATUS && descMap?.value) {
				maps += translateZoneStatus(new ZoneStatus(zigbee.convertToInt(descMap?.value)))
			} else {
				maps += handleAcceleration(descMap)
			}
		}
	} else if (maps[0].name == "temperature") {
		def map = maps[0]
		if (tempOffset) {
			map.value = (int) map.value + (int) tempOffset
		}
		map.descriptionText = temperatureScale == 'C' ? '{{ device.displayName }} was {{ value }}°C' : '{{ device.displayName }} was {{ value }}°F'
		map.translatable = true
	}

	def result = maps.inject([]) {acc, it ->
		if (it) {
			acc << createEvent(it)
		}
	}
	if (description?.startsWith('enroll request')) {
		List cmds = zigbee.enrollResponse()
		log.debug "enroll response: ${cmds}"
		result = cmds?.collect { new physicalgraph.device.HubAction(it) }
	}
	return result
}

private List<Map> handleAcceleration(descMap) {
	def result = []
	if (descMap.clusterInt == 0xFC02 && descMap.attrInt == 0x0010) {
		def value = descMap.value == "01" ? "active" : "inactive"
		log.debug "Acceleration $value"
		result << [
				name           : "acceleration",
				value          : value,
				descriptionText: "{{ device.displayName }} was $value",
				isStateChange  : isStateChange(device, "acceleration", value),
				translatable   : true
		]

		if (descMap.additionalAttrs) {
			result += parseAxis(descMap.additionalAttrs)
		}
	} else if (descMap.clusterInt == 0xFC02 && descMap.attrInt == 0x0012) {
		def addAttrs = descMap.additionalAttrs ?: []
		addAttrs << ["attrInt": descMap.attrInt, "value": descMap.value]
		result += parseAxis(addAttrs)
	}
	return result
}

private List<Map> parseAxis(List<Map> attrData) {
	def results = []
	def x = hexToSignedInt(attrData.find { it.attrInt == 0x0012 }?.value)
	def y = hexToSignedInt(attrData.find { it.attrInt == 0x0013 }?.value)
	def z = hexToSignedInt(attrData.find { it.attrInt == 0x0014 }?.value)

	if ([x, y ,z].any { it == null }) {
		return []
	}

	def xyzResults = [:]
	if (device.getDataValue("manufacturer") == "SmartThings") {
		// This mapping matches the current behavior of the Device Handler for the Centralite sensors
		xyzResults.x = z
		xyzResults.y = y
		xyzResults.z = -x
	} else {
		// The axises reported by the Device Handler differ from the axises reported by the sensor
		// This may change in the future
		xyzResults.x = z
		xyzResults.y = x
		xyzResults.z = y
	}

	log.debug "parseAxis -- ${xyzResults}"

	if (garageSensor == "Yes")
		results += garageEvent(xyzResults.z)

	def value = "${xyzResults.x},${xyzResults.y},${xyzResults.z}"
	results << [
			name           : "threeAxis",
			value          : value,
			linkText       : getLinkText(device),
			descriptionText: "${getLinkText(device)} was ${value}",
			handlerName    : name,
			isStateChange  : isStateChange(device, "threeAxis", value),
			displayed      : false
	]
	results
}

private List<Map> parseIasMessage(String description) {
	ZoneStatus zs = zigbee.parseZoneStatus(description)

	translateZoneStatus(zs)
}

private List<Map> translateZoneStatus(ZoneStatus zs) {
	List<Map> results = []

	if (garageSensor != "Yes") {
		def value = zs.isAlarm1Set() ? 'open' : 'closed'
		log.debug "Contact: ${device.displayName} value = ${value}"
		def descriptionText = value == 'open' ? '{{ device.displayName }} was opened' : '{{ device.displayName }} was closed'
		results << [name: 'contact', value: value, descriptionText: descriptionText, displayed: false, translatable: true]
		results << [name: 'status', value: value, descriptionText: descriptionText, translatable: true]
	}

	return results
}

private Map getBatteryResult(rawValue) {
	log.debug "Battery rawValue = ${rawValue}"

	def result = [:]

	def volts = rawValue / 10

	if (!(rawValue == 0 || rawValue == 255)) {
		result.name = 'battery'
		result.translatable = true
		result.descriptionText = "{{ device.displayName }} battery was {{ value }}%"
		if (device.getDataValue("manufacturer") == "SmartThings") {
			volts = rawValue // For the batteryMap to work the key needs to be an int
			def batteryMap = [28: 100, 27: 100, 26: 100, 25: 90, 24: 90, 23: 70,
							  22: 70, 21: 50, 20: 50, 19: 30, 18: 30, 17: 15, 16: 1, 15: 0]
			def minVolts = 15
			def maxVolts = 28

			if (volts < minVolts)
				volts = minVolts
			else if (volts > maxVolts)
				volts = maxVolts
			def pct = batteryMap[volts]
			result.value = pct
		} else {
			def minVolts = 2.1
			def maxVolts = 2.7
			// Get the current battery percentage as a multiplier 0 - 1
			def curValVolts = Integer.parseInt(device.currentState("battery")?.value ?: "100") / 100.0
			// Find the corresponding voltage from our range
			curValVolts = curValVolts * (maxVolts - minVolts) + minVolts
			// Round to the nearest 10th of a volt
			curValVolts = Math.round(10 * curValVolts) / 10.0
			// Only update the battery reading if we don't have a last reading,
			// OR we have received the same reading twice in a row
			// OR we don't currently have a battery reading
			// OR the value we just received is at least 2 steps off from the last reported value
			if(state?.lastVolts == null || state?.lastVolts == volts || device.currentState("battery")?.value == null || Math.abs(curValVolts - volts) > 0.1) {
				def pct = (volts - minVolts) / (maxVolts - minVolts)
				def roundedPct = Math.round(pct * 100)
				if (roundedPct <= 0)
					roundedPct = 1
				result.value = Math.min(100, roundedPct)
			} else {
				// Don't update as we want to smooth the battery values
				result = null
			}
			state.lastVolts = volts
		}
	}

	return result
}

List<Map> garageEvent(zValue) {
	List<Map> results = []
	def absValue = zValue.abs()
	def contactValue = null
	def garageValue = null
	if (absValue > 900) {
		contactValue = 'closed'
		garageValue = 'garage-closed'
	} else if (absValue < 100) {
		contactValue = 'open'
		garageValue = 'garage-open'
	}
	if (contactValue != null) {
		def descriptionText = contactValue == 'open' ? '{{ device.displayName }} was opened' : '{{ device.displayName }} was closed'
		results << [name: 'contact', value: contactValue, descriptionText: descriptionText, displayed: false, translatable: true]
		results << [name: 'status', value: garageValue, descriptionText: descriptionText, translatable: true]
	}
	results
}

/**
 * PING is used by Device-Watch in attempt to reach the Device
 * */
def ping() {
	return zigbee.readAttribute(0x001, 0x0020) // Read the Battery Level
}

def refresh() {
	log.debug "Refreshing Values "

	def refreshCmds = zigbee.readAttribute(zigbee.TEMPERATURE_MEASUREMENT_CLUSTER, 0x0000) +
			zigbee.readAttribute(zigbee.POWER_CONFIGURATION_CLUSTER, 0x0020) +
			zigbee.readAttribute(0xFC02, 0x0010, [mfgCode: manufacturerCode]) +
			zigbee.readAttribute(zigbee.IAS_ZONE_CLUSTER, zigbee.ATTRIBUTE_IAS_ZONE_STATUS) + zigbee.enrollResponse()

	return refreshCmds
}

def configure() {
	// Device-Watch allows 2 check-in misses from device + ping (plus 1 min lag time)
	// enrolls with default periodic reporting until newer 5 min interval is confirmed
	sendEvent(name: "checkInterval", value: 2 * 60 * 60 + 1 * 60, displayed: false, data: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID])

	log.debug "Configuring Reporting"
	def configCmds = []

	if (device.getDataValue("manufacturer") == "SmartThings") {
		log.debug "Refreshing Values for manufacturer: SmartThings "
		/* These values of Motion Threshold Multiplier(0x01) and Motion Threshold (0x0276)
            seem to be giving pretty accurate results for the XYZ co-ordinates for this manufacturer.
            Separating these out in a separate if-else because I do not want to touch Centralite part
            as of now.
        */
		configCmds += zigbee.writeAttribute(0xFC02, 0x0000, 0x20, 0x01, [mfgCode: manufacturerCode])
		configCmds += zigbee.writeAttribute(0xFC02, 0x0002, 0x21, 0x0276, [mfgCode: manufacturerCode])
	} else {
		// Write a motion threshold of 2 * .063g = .126g
		// Currently due to a Centralite firmware issue, this will cause a read attribute response that
		// indicates acceleration even when there isn't.
		configCmds += zigbee.writeAttribute(0xFC02, 0x0000, 0x20, 0x02, [mfgCode: manufacturerCode])
	}

	// temperature minReportTime 30 seconds, maxReportTime 5 min. Reporting interval if no activity
	// battery minReport 30 seconds, maxReportTime 6 hrs by default
	configCmds += zigbee.batteryConfig() +
			zigbee.temperatureConfig(30, 300) +
			zigbee.configureReporting(0xFC02, 0x0010, DataType.BITMAP8, 10, 3600, 0x01, [mfgCode: manufacturerCode]) +
			zigbee.configureReporting(0xFC02, 0x0012, DataType.INT16, 1, 3600, 0x0001, [mfgCode: manufacturerCode]) +
			zigbee.configureReporting(0xFC02, 0x0013, DataType.INT16, 1, 3600, 0x0001, [mfgCode: manufacturerCode]) +
			zigbee.configureReporting(0xFC02, 0x0014, DataType.INT16, 1, 3600, 0x0001, [mfgCode: manufacturerCode])

	return refresh() + configCmds
}

def updated() {
	log.debug "updated called"
	log.info "garage value : $garageSensor"
	if (garageSensor == "Yes") {
		def descriptionText = "Updating device to garage sensor"
		if (device.latestValue("status") == "open") {
			sendEvent(name: 'status', value: 'garage-open', descriptionText: descriptionText, translatable: true)
		} else if (device.latestValue("status") == "closed") {
			sendEvent(name: 'status', value: 'garage-closed', descriptionText: descriptionText, translatable: true)
		}
	} else {
		def descriptionText = "Updating device to open/close sensor"
		if (device.latestValue("status") == "garage-open") {
			sendEvent(name: 'status', value: 'open', descriptionText: descriptionText, translatable: true)
		} else if (device.latestValue("status") == "garage-closed") {
			sendEvent(name: 'status', value: 'closed', descriptionText: descriptionText, translatable: true)
		}
	}
}

private hexToSignedInt(hexVal) {
	if (!hexVal) {
		return null
	}

	def unsignedVal = hexToInt(hexVal)
	unsignedVal > 32767 ? unsignedVal - 65536 : unsignedVal
}

private getManufacturerCode() {
	if (device.getDataValue("manufacturer") == "SmartThings") {
		return "0x110A"
	} else {
		return "0x104E"
	}
}

private hexToInt(value) {
	new BigInteger(value, 16)
}
