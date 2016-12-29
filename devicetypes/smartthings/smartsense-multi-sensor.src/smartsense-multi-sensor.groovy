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

metadata {
	definition (name: "SmartSense Multi Sensor", namespace: "smartthings", author: "SmartThings") {

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
		multiAttributeTile(name:"status", type: "generic", width: 6, height: 4){
			tileAttribute ("device.status", key: "PRIMARY_CONTROL") {
				attributeState "open", label:'Open', icon:"st.contact.contact.open", backgroundColor:"#ffa81e"
				attributeState "closed", label:'Closed', icon:"st.contact.contact.closed", backgroundColor:"#79b821"
				attributeState "garage-open", label:'Open', icon:"st.doors.garage.garage-open", backgroundColor:"#ffa81e"
				attributeState "garage-closed", label:'Closed', icon:"st.doors.garage.garage-closed", backgroundColor:"#79b821"
			}
		}
		standardTile("contact", "device.contact", width: 2, height: 2) {
			state("open", label:'Open', icon:"st.contact.contact.open", backgroundColor:"#ffa81e")
			state("closed", label:'Closed', icon:"st.contact.contact.closed", backgroundColor:"#79b821")
		}
		standardTile("acceleration", "device.acceleration", width: 2, height: 2) {
			state("active", label:'Active', icon:"st.motion.acceleration.active", backgroundColor:"#53a7c0")
			state("inactive", label:'Inactive', icon:"st.motion.acceleration.inactive", backgroundColor:"#ffffff")
		}
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
		valueTile("battery", "device.battery", decoration: "flat", inactiveLabel: false, width: 2, height: 2) {
			state "battery", label:'${currentValue}% battery', unit:""
		}
		standardTile("refresh", "device.refresh", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "default", action:"refresh.refresh", icon:"st.secondary.refresh"
		}


		main(["status", "acceleration", "temperature"])
		details(["status", "acceleration", "temperature", "battery", "refresh"])
	}
 }

def parse(String description) {
	Map map = [:]
	if (description?.startsWith('catchall:')) {
		map = parseCatchAllMessage(description)
	}
	else if (description?.startsWith('temperature: ')) {
		map = parseCustomMessage(description)
	}
	else if (description?.startsWith('zone status')) {
		map = parseIasMessage(description)
	}

	def result = map ? createEvent(map) : [:]

	if (description?.startsWith('enroll request')) {
		List cmds = enrollResponse()
		log.debug "enroll response: ${cmds}"
		result = cmds?.collect { new physicalgraph.device.HubAction(it) }
	}
	else if (description?.startsWith('read attr -')) {
		result = parseReportAttributeMessage(description).each { createEvent(it) }
	}
	return result
}

private Map parseCatchAllMessage(String description) {
	Map resultMap = [:]
	def cluster = zigbee.parse(description)
	log.debug cluster
	if (shouldProcessMessage(cluster)) {
		switch(cluster.clusterId) {
			case 0x0001:
				// 0x07 - configure reporting
				if (cluster.command != 0x07) {
					resultMap = getBatteryResult(cluster.data.last())
				}
			break

			case 0xFC02:
				log.debug 'ACCELERATION'
			break

			case 0x0402:
				if (cluster.command == 0x07) {
					if(cluster.data[0] == 0x00) {
						log.debug "TEMP REPORTING CONFIG RESPONSE" + cluster
						resultMap = [name: "checkInterval", value: 60 * 12, displayed: false, data: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID]]
					}
					else {
						log.warn "TEMP REPORTING CONFIG FAILED- error code:${cluster.data[0]}"
					}
				}
				else {
					// temp is last 2 data values. reverse to swap endian
					String temp = cluster.data[-2..-1].reverse().collect { cluster.hex1(it) }.join()
					def value = getTemperature(temp)
					resultMap = getTemperatureResult(value)
				}
			break
		}
	}

	return resultMap
}

private boolean shouldProcessMessage(cluster) {
	// 0x0B is default response indicating message got through
	boolean ignoredMessage = cluster.profileId != 0x0104 ||
	cluster.command == 0x0B ||
	(cluster.data.size() > 0 && cluster.data.first() == 0x3e)
	return !ignoredMessage
}

private List parseReportAttributeMessage(String description) {
	Map descMap = (description - "read attr - ").split(",").inject([:]) { map, param ->
		def nameAndValue = param.split(":")
		map += [(nameAndValue[0].trim()):nameAndValue[1].trim()]
	}

	List result = []
	if (descMap.cluster == "0402" && descMap.attrId == "0000") {
		def value = getTemperature(descMap.value)
		result << getTemperatureResult(value)
	}
	else if (descMap.cluster == "FC02" && descMap.attrId == "0010") {
		if (descMap.value.size() == 32) {
			// value will look like 00ae29001403e2290013001629001201
			// breaking this apart and swapping byte order where appropriate, this breaks down to:
			//   X (0x0012) = 0x0016
			//   Y (0x0013) = 0x03E2
			//   Z (0x0014) = 0x00AE
			// note that there is a known bug in that the x,y,z attributes are interpreted in the wrong order
			// this will be fixed in a future update
			def threeAxisAttributes = descMap.value[0..-9]
			result << parseAxis(threeAxisAttributes)
			descMap.value = descMap.value[-2..-1]
		}
		result << getAccelerationResult(descMap.value)
	}
	else if (descMap.cluster == "FC02" && descMap.attrId == "0012" && descMap.value.size() == 24) {
		// The size is checked to ensure the attribute report contains X, Y and Z values
		// If all three axis are not included then the attribute report is ignored
		result << parseAxis(descMap.value)
	}
	else if (descMap.cluster == "0001" && descMap.attrId == "0020") {
		result << getBatteryResult(Integer.parseInt(descMap.value, 16))
	}

	return result
}

private Map parseCustomMessage(String description) {
	Map resultMap = [:]
	if (description?.startsWith('temperature: ')) {
		def value = zigbee.parseHATemperatureValue(description, "temperature: ", getTemperatureScale())
		resultMap = getTemperatureResult(value)
	}
	return resultMap
}

private Map parseIasMessage(String description) {
	ZoneStatus zs = zigbee.parseZoneStatus(description)
	Map resultMap = [:]

			if (garageSensor != "Yes"){
		resultMap = zs.isAlarm1Set() ? getContactResult('open') : getContactResult('closed')
			}

	return resultMap
}

def updated() {
	log.debug "updated called"
	log.info "garage value : $garageSensor"
	if (garageSensor == "Yes") {
		def descriptionText = "Updating device to garage sensor"
		if (device.latestValue("status") == "open") {
			sendEvent(name: 'status', value: 'garage-open', descriptionText: descriptionText, translatable: true)
		}
		else if (device.latestValue("status") == "closed") {
			sendEvent(name: 'status', value: 'garage-closed', descriptionText: descriptionText, translatable: true)
		}
	}
	else {
		def descriptionText = "Updating device to open/close sensor"
		if (device.latestValue("status") == "garage-open") {
			sendEvent(name: 'status', value: 'open', descriptionText: descriptionText, translatable: true)
		}
		else if (device.latestValue("status") == "garage-closed") {
			sendEvent(name: 'status', value: 'closed', descriptionText: descriptionText, translatable: true)
		}
	}
}

def getTemperature(value) {
	def celsius = Integer.parseInt(value, 16).shortValue() / 100
	if(getTemperatureScale() == "C"){
		return Math.round(celsius)
		} else {
			return Math.round(celsiusToFahrenheit(celsius))
		}
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
			def maxVolts = 3.0
			def pct = (volts - minVolts) / (maxVolts - minVolts)
			def roundedPct = Math.round(pct * 100)
			if (roundedPct <= 0)
				roundedPct = 1
			result.value = Math.min(100, roundedPct)
		}
	}

	return result
}

private Map getTemperatureResult(value) {
	log.debug "Temperature"
	if (tempOffset) {
		def offset = tempOffset as int
		def v = value as int
		value = v + offset
	}
	def descriptionText = temperatureScale == 'C' ? '{{ device.displayName }} was {{ value }}°C':
			'{{ device.displayName }} was {{ value }}°F'

	return [
		name: 'temperature',
		value: value,
		descriptionText: descriptionText,
		translatable: true,
		unit: temperatureScale
	]
}

private Map getContactResult(value) {
	log.debug "Contact: ${device.displayName} value = ${value}"
	def descriptionText = value == 'open' ? '{{ device.displayName }} was opened' : '{{ device.displayName }} was closed'
	sendEvent(name: 'contact', value: value, descriptionText: descriptionText, displayed: false, translatable: true)
	return [name: 'status', value: value, descriptionText: descriptionText, translatable: true]
}

private getAccelerationResult(numValue) {
	log.debug "Acceleration"
	def name = "acceleration"
    def value
    def descriptionText

	if ( numValue.endsWith("1") ) {
    	value = "active"
        descriptionText = '{{ device.displayName }} was active'
    } else {
    	value = "inactive"
        descriptionText = '{{ device.displayName }} was inactive'
    }

	def isStateChange = isStateChange(device, name, value)
	return [
		name: name,
		value: value,
		descriptionText: descriptionText,
		isStateChange: isStateChange,
		translatable: true
	]
}

/**
 * PING is used by Device-Watch in attempt to reach the Device
 * */
def ping() {
	return zigbee.readAttribute(0x001, 0x0020) // Read the Battery Level
}

def refresh() {
	log.debug "Refreshing Values "

	def refreshCmds = []

	if (device.getDataValue("manufacturer") == "SmartThings") {
		log.debug "Refreshing Values for manufacturer: SmartThings "
		/* These values of Motion Threshold Multiplier(0x01) and Motion Threshold (0x0276)
            seem to be giving pretty accurate results for the XYZ co-ordinates for this manufacturer.
            Separating these out in a separate if-else because I do not want to touch Centralite part
            as of now.
        */
		refreshCmds += zigbee.writeAttribute(0xFC02, 0x0000, 0x20, 0x01, [mfgCode: manufacturerCode])
		refreshCmds += zigbee.writeAttribute(0xFC02, 0x0002, 0x21, 0x0276, [mfgCode: manufacturerCode])
	} else {
		refreshCmds += zigbee.writeAttribute(0xFC02, 0x0000, 0x20, 0x02, [mfgCode: manufacturerCode])
	}

	//Common refresh commands
	refreshCmds += zigbee.readAttribute(0x0402, 0x0000) +
			zigbee.readAttribute(0x0001, 0x0020) +
			zigbee.readAttribute(0xFC02, 0x0010, [mfgCode: manufacturerCode])

	return refreshCmds + enrollResponse()
}

def configure() {
	// Device-Watch allows 2 check-in misses from device + ping (plus 1 min lag time)
	// enrolls with default periodic reporting until newer 5 min interval is confirmed
	sendEvent(name: "checkInterval", value: 2 * 60 * 60 + 1 * 60, displayed: false, data: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID])

	log.debug "Configuring Reporting"

	// temperature minReportTime 30 seconds, maxReportTime 5 min. Reporting interval if no activity
	// battery minReport 30 seconds, maxReportTime 6 hrs by default
	def configCmds = zigbee.batteryConfig() +
			zigbee.temperatureConfig(30, 300) +
			zigbee.configureReporting(0xFC02, 0x0010, 0x18, 10, 3600, 0x01, [mfgCode: manufacturerCode]) +
			zigbee.configureReporting(0xFC02, 0x0012, 0x29, 1, 3600, 0x0001, [mfgCode: manufacturerCode]) +
			zigbee.configureReporting(0xFC02, 0x0013, 0x29, 1, 3600, 0x0001, [mfgCode: manufacturerCode]) +
			zigbee.configureReporting(0xFC02, 0x0014, 0x29, 1, 3600, 0x0001, [mfgCode: manufacturerCode])

	return refresh() + configCmds
}

private getEndpointId() {
	new BigInteger(device.endpointId, 16).toString()
}

def enrollResponse() {
	log.debug "Sending enroll response"
	String zigbeeEui = swapEndianHex(device.hub.zigbeeEui)
	[
		//Resending the CIE in case the enroll request is sent before CIE is written
		"zcl global write 0x500 0x10 0xf0 {${zigbeeEui}}", "delay 200",
		"send 0x${device.deviceNetworkId} 1 ${endpointId}", "delay 2000",
		//Enroll Response
		"raw 0x500 {01 23 00 00 00}", "delay 200",
		"send 0x${device.deviceNetworkId} 1 1", "delay 2000"
	]
}

private Map parseAxis(String description) {
	def z = hexToSignedInt(description[0..3])
	def y = hexToSignedInt(description[10..13])
	def x = hexToSignedInt(description[20..23])
	def xyzResults = [x: x, y: y, z: z]

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
		garageEvent(xyzResults.z)

	getXyzResult(xyzResults, description)
}

private hexToSignedInt(hexVal) {
	def unsignedVal = hexToInt(hexVal)
	unsignedVal > 32767 ? unsignedVal - 65536 : unsignedVal
}

def garageEvent(zValue) {
	def absValue = zValue.abs()
	def contactValue = null
	def garageValue = null
	if (absValue>900) {
		contactValue = 'closed'
		garageValue = 'garage-closed'
	}
	else if (absValue < 100) {
		contactValue = 'open'
		garageValue = 'garage-open'
	}
	if (contactValue != null){
		def descriptionText = contactValue == 'open' ? '{{ device.displayName }} was opened' :'{{ device.displayName }} was closed'
		sendEvent(name: 'contact', value: contactValue, descriptionText: descriptionText, displayed:false, translatable: true)
		sendEvent(name: 'status', value: garageValue, descriptionText: descriptionText, translatable: true)
	}
}

private Map getXyzResult(results, description) {
	def name = "threeAxis"
	def value = "${results.x},${results.y},${results.z}"
	def linkText = getLinkText(device)
	def descriptionText = "$linkText was $value"
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

private hex(value) {
	new BigInteger(Math.round(value).toString()).toString(16)
}

private String swapEndianHex(String hex) {
	reverseArray(hex.decodeHex()).encodeHex()
}

private byte[] reverseArray(byte[] array) {
	int i = 0;
	int j = array.length - 1;
	byte tmp;
	while (j > i) {
		tmp = array[j];
		array[j] = array[i];
		array[i] = tmp;
		j--;
		i++;
	}
	return array
}
