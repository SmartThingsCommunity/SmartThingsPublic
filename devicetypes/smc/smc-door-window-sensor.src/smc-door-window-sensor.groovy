/**
 *  SMCDW30-Z
 *
 *  Copyright 2016 Roy Chuang
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
	definition (name: "SMC Door/Window Sensor", namespace: "SMC", author: "Roy Chuang") {
		capability "Contact Sensor"
		capability "Configuration"
		capability "Battery"
		capability "Temperature Measurement"
		capability "Refresh"
		capability "Tamper Alert"
		capability "Sensor"		

		command "enrollResponse"

		fingerprint profileId: "0104", deviceId: "0402", deviceVersion: "00", inClusters: "0000,0001,0003,0402,0500,0020,0B05", outClusters: "0019", manufacturer: "ACCTON", model: "SMCDW30-Z"
	}

	simulator {
		status "closed": "zone status 0x0000 -- extended status 0x00"
		status "open": "zone status 0x0001 -- extended status 0x00"
		status "tamper alert with closed": "zone status 0x0004 -- extended status 0x00"
		status "tamper alert with open": "zone status 0x0005 -- extended status 0x00"
	}

	preferences {
		section {
			image(name: 'educationalcontent', multiple: true, images: [
				"http://na.smc.com/site/wp-content/uploads/2016/06/SMCDW30-Z-Sensor-_Magnet-1-170x170.png"
				])
		}
		section {
			input title: "Temperature Offset", description: "This feature allows you to correct any temperature variations by selecting an offset. Ex: If your sensor consistently reports a temp that's 5 degrees too warm, you'd enter '-5'. If 3 degrees too cold, enter '+3'.", displayDuringSetup: false, type: "paragraph", element: "paragraph"
			input "tempOffset", "number", title: "Degrees", description: "Adjust temperature by this many degrees", range: "*..*", displayDuringSetup: false
		}
	}

	tiles(scale: 2) {
		multiAttributeTile(name:"contact", type: "generic", width: 6, height: 4){
			tileAttribute ("device.contact", key: "PRIMARY_CONTROL") {
				attributeState "open", label:'${name}', icon:"st.contact.contact.open", backgroundColor:"#53a7c0"
				attributeState "closed", label:'${name}', icon:"st.contact.contact.closed", backgroundColor:"#ffffff"
				attributeState "tamper alert with open", label:'tamper', icon:"st.contact.contact.open", backgroundColor:"#cc0000"
				attributeState "tamper alert with closed", label:'tamper', icon:"st.contact.contact.closed", backgroundColor:"#cc0000"
			}
		}
		valueTile("temperature", "device.temperature", width: 2, height: 2) {
			state("temperature", label:'${currentValue}°', unit:"F",
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
		standardTile("rssi", "device.rssi", decoration: "flat", inactiveLabel: false, width: 2, height: 2) {
			state "rssi", label:'${currentValue}% rssi', unit:""
		}

		main(["contact"])
		details(["contact", "temperature", "battery", "refresh", "rssi"])
	}
}

def parse(String description) {
	log.debug "description: $description"

	Map map = [:]
	if (description?.startsWith('catchall:')) {
		map = parseCatchAllMessage(description)
	}
	else if (description?.startsWith('read attr -')) {
		map = parseReportAttributeMessage(description)
	}
	else if (description?.startsWith('temperature: ')) {
		map = parseCustomMessage(description)
	}
	else if (description?.startsWith('zone status')) {
		map = parseIasMessage(description)
	}

	log.debug "Parse returned $map"
	def result = map ? createEvent(map) : null

	if (description?.startsWith('enroll request')) {
		List cmds = enrollResponse()
		log.debug "enroll response: ${cmds}"
		result = cmds?.collect { new physicalgraph.device.HubAction(it) }
	}
	return result
}

private Map parseCatchAllMessage(String description) {
	Map resultMap = [:]
	def cluster = zigbee.parse(description)
	if (shouldProcessMessage(cluster)) {
		switch(cluster.clusterId) {
			case 0x0001:
				resultMap = getBatteryResult(cluster.data.last())
				break

			case 0x0402:
				// temp is last 2 data values. reverse to swap endian
				String temp = cluster.data[-2..-1].reverse().collect { cluster.hex1(it) }.join()
				def value = getTemperature(temp)
				resultMap = getTemperatureResult(value)
				break

			case 0x0b05:
				log.debug 'Diag'
				break
		}
	}

	return resultMap
}

private boolean shouldProcessMessage(cluster) {
	// 0x0B is default response indicating message got through
	// 0x07 is bind message
	boolean ignoredMessage = cluster.profileId != 0x0104 ||
		cluster.command == 0x0B ||
		cluster.command == 0x07 ||
		(cluster.data.size() > 0 && cluster.data.first() == 0x3e)
	return !ignoredMessage
}

private Map parseReportAttributeMessage(String description) {
	Map descMap = (description - "read attr - ").split(",").inject([:]) { map, param ->
		def nameAndValue = param.split(":")
		map += [(nameAndValue[0].trim()):nameAndValue[1].trim()]
	}
	log.debug "Desc Map: $descMap"

	Map resultMap = [:]
	if (descMap.cluster == "0402" && descMap.attrId == "0000") {
		def value = getTemperature(descMap.value)
		resultMap = getTemperatureResult(value)
	}
	else if (descMap.cluster == "0001" && descMap.attrId == "0020") {
		resultMap = getBatteryResult(Integer.parseInt(descMap.value, 16))
	}
	else if (descMap.cluster == "0B05" && descMap.attrId == "011d") {
		resultMap = getRssiResult(Integer.parseInt(descMap.value, 16))
    }

	return resultMap
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
	List parsedMsg = description.split(' ')
	String msgCode = parsedMsg[2]

	Map resultMap = [:]
	switch(msgCode) {
		case '0x0000': // Closed
            resultMap = getContactResult('closed')
			break

		case '0x0001': // Open/Alarm1
			resultMap = getContactResult('open')
			break

		case '0x0004': // Closed & Tamper
			resultMap = getContactResult('tamper alert with closed')
			break

		case '0x0005': // Open & Tamper
            resultMap = getContactResult('tamper alert with open')
			break

		default :
			log.warn "ZoneStatus not support!"
			break
	}
	return resultMap
}

def getTemperature(value) {
	def celsius = Integer.parseInt(value, 16).shortValue() / 100
	if(getTemperatureScale() == "C"){
		return celsius
	} else {
		return celsiusToFahrenheit(celsius) as Integer
	}
}

private Map getBatteryResult(rawValue) {
	log.info "Battery rawValue = ${rawValue}"
	def linkText = getLinkText(device)

	def result = [
		name: 'battery',
		value: '--',
		translatable: true
	]

	def volts = rawValue / 10

	if (rawValue == 0 || rawValue == 255) {}
	else {
		if (volts > 3.5) {
			result.descriptionText = "${device.displayName} battery has too much power: (> 3.5) volts."
		}
		else {
				def minVolts = 2.1
				def maxVolts = 3.2
				def pct = (volts - minVolts) / (maxVolts - minVolts)
				def roundedPct = Math.round(pct * 100)
				result.value = Math.min(100, roundedPct)
				result.descriptionText = "${device.displayName} battery was ${result.value}%"
		}
	}

	return result
}

private Map getRssiResult(rawValue) {
	log.info "RSSI rawValue = ${rawValue}"
	def linkText = getLinkText(device)

	def result = [
		name: 'rssi',
		value: '--',
		translatable: true
	]
    
	def pct = rawValue / 255
	def roundedPct = Math.round(pct * 100)
	result.value = Math.min(100, roundedPct)
	result.descriptionText = "${device.displayName} RSSI was ${result.value}%"

	return result
}

private Map getTemperatureResult(value) {
	if (tempOffset) {
		def offset = tempOffset as int
		def v = value as int
		value = v + offset
	}
    
    log.info "Temperature is ${value}"
    def descriptionText
    if ( temperatureScale == 'C' )
    	descriptionText = "${device.displayName} was ${value}°C"
    else
    	descriptionText = "${device.displayName} was ${value}°F"

	return [
		name: 'temperature',
		value: value,
		descriptionText: descriptionText,
        translatable: true
	]
}

private Map getContactResult(value) {
	String descriptionText = "${device.displayName}"
	return [
		name: 'contact',
		value: value,
		descriptionText: descriptionText,
        translatable: true
	]
}

def refresh() {
	log.debug "refresh called"
	def refreshCmds = [
		"st rattr 0x${device.deviceNetworkId} 1 0x402 0", "delay 200",
		"st rattr 0x${device.deviceNetworkId} 1 0x001 0x20", "delay 200",
		"st rattr 0x${device.deviceNetworkId} 1 0xb05 0x11d", "delay 200"
	]

	return refreshCmds
}

def configure() {
	sendEvent(name: "checkInterval", value: 7200, displayed: false)

	String zigbeeEui = swapEndianHex(device.hub.zigbeeEui)
	log.debug "Configuring Reporting, IAS CIE, and Bindings."
	log.debug "${device.hub.zigbeeEui}. ${zigbeeEui}"

	def configCmds = [
		/* Write CIE */
		"zcl global write 0x500 0x10 0xf0 {${zigbeeEui}}", "delay 200",
		"send 0x${device.deviceNetworkId} 1 ${endpointId}", "delay 2000",

		"zdo bind 0x${device.deviceNetworkId} ${endpointId} 1 1 {${device.zigbeeId}} {}", "delay 500",
		"zcl global send-me-a-report 0x0001 0x20 0x20 30 21600 {02}",		//Battery Voltage 6 hrs
		"send 0x${device.deviceNetworkId} 1 ${endpointId}", "delay 1000",

		"zdo bind 0x${device.deviceNetworkId} ${endpointId} 1 0x402 {${device.zigbeeId}} {}", "delay 500",
		"zcl global send-me-a-report 0x0402 0 0x29 30 3600 {C800}",		//Temperature 1 hr
		"send 0x${device.deviceNetworkId} 1 ${endpointId}", "delay 1000",

		//Set long poll interval to 5 min
		"raw 0x0020 {11 00 02 b0 04 00 00}", 
		"send 0x${device.deviceNetworkId} 1 1", "delay 500"
	]
	return configCmds + refresh() // send refresh cmds as part of config
}

def enrollResponse() {
	log.debug "Sending enroll response"
	String zigbeeEui = swapEndianHex(device.hub.zigbeeEui)
	[
		//Enroll Response
		"raw 0x500 {01 23 00 00 00}",
		"send 0x${device.deviceNetworkId} 1 1", "delay 200"
	]
}

private getEndpointId() {
	new BigInteger(device.endpointId, 16).toString()
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