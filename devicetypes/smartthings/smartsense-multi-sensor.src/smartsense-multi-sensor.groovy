/**
 *  SmartSense Multi
 *
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
 	definition (name: "SmartSense Multi Sensor", namespace: "smartthings", author: "SmartThings") {
 		
        capability "Three Axis"
		capability "Battery"
 		capability "Configuration"
        capability "Sensor"
 		capability "Contact Sensor"
 		capability "Acceleration Sensor"
 		capability "Refresh"
 		capability "Temperature Measurement"
        
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
 			input title: "Temperature Offset", description: "This feature allows you to correct any temperature variations by selecting an offset. Ex: If your sensor consistently reports a temp that's 5 degrees too warm, you'd enter \"-5\". If 3 degrees too cold, enter \"+3\".", displayDuringSetup: false, type: "paragraph", element: "paragraph"
 			input "tempOffset", "number", title: "Degrees", description: "Adjust temperature by this many degrees", range: "*..*", displayDuringSetup: false
 		}
		section {
 			input("garageSensor", "enum", title: "Do you want to use this sensor on a garage door?", options: ["Yes", "No"], defaultValue: "No", required: false, displayDuringSetup: false)
		}
 	}

	tiles(scale: 2) {
		multiAttributeTile(name:"status", type: "generic", width: 6, height: 4){
			tileAttribute ("device.status", key: "PRIMARY_CONTROL") {
				attributeState "open", label:'${name}', icon:"st.contact.contact.open", backgroundColor:"#ffa81e"
				attributeState "closed", label:'${name}', icon:"st.contact.contact.closed", backgroundColor:"#79b821"
				attributeState "garage-open", label:'Open', icon:"st.doors.garage.garage-open", backgroundColor:"#ffa81e"
				attributeState "garage-closed", label:'Closed', icon:"st.doors.garage.garage-closed", backgroundColor:"#79b821"
			}
		}
		standardTile("contact", "device.contact", width: 2, height: 2) {
			state("open", label:'${name}', icon:"st.contact.contact.open", backgroundColor:"#ffa81e")
			state("closed", label:'${name}', icon:"st.contact.contact.closed", backgroundColor:"#79b821")
		}
		standardTile("acceleration", "device.acceleration", width: 2, height: 2) {
			state("active", label:'${name}', icon:"st.motion.acceleration.active", backgroundColor:"#53a7c0")
			state("inactive", label:'${name}', icon:"st.motion.acceleration.inactive", backgroundColor:"#ffffff")
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
		valueTile("3axis", "device.threeAxis", decoration: "flat", wordWrap: false, width: 2, height: 2) {
			state("threeAxis", label:'${currentValue}', unit:"", backgroundColor:"#ffffff")
		}
		valueTile("battery", "device.battery", decoration: "flat", inactiveLabel: false, width: 2, height: 2) {
			state "battery", label:'${currentValue}% battery', unit:""
		}
 		standardTile("refresh", "device.refresh", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
 			state "default", action:"refresh.refresh", icon:"st.secondary.refresh"
 		}


		main(["status", "acceleration", "temperature"])
		details(["status", "acceleration", "temperature", "3axis", "battery", "refresh"])
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

 	def result = map ? createEvent(map) : null

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
 			resultMap = getBatteryResult(cluster.data.last())
 			break

 			case 0xFC02:
            log.debug 'ACCELERATION'
 			break

 			case 0x0402:
 			log.debug 'TEMP'
                // temp is last 2 data values. reverse to swap endian
                String temp = cluster.data[-2..-1].reverse().collect { cluster.hex1(it) }.join()
                def value = getTemperature(temp)
                resultMap = getTemperatureResult(value)
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
	else if (descMap.cluster == "FC02" && descMap.attrId == "0012") {
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
	List parsedMsg = description.split(' ')
	String msgCode = parsedMsg[2]

	Map resultMap = [:]
	switch(msgCode) {
        case '0x0020': // Closed/No Motion/Dry
			if (garageSensor != "Yes"){
				resultMap = getContactResult('closed')
			}
        break

        case '0x0021': // Open/Motion/Wet
			if (garageSensor != "Yes"){
				resultMap = getContactResult('open')
			}
        break

        case '0x0022': // Tamper Alarm
        break

        case '0x0023': // Battery Alarm
        break

        case '0x0024': // Supervision Report
			if (garageSensor != "Yes"){
				resultMap = getContactResult('closed')
			}
        break

        case '0x0025': // Restore Report
			if (garageSensor != "Yes"){
				resultMap = getContactResult('open')
			}
        break

        case '0x0026': // Trouble/Failure
        break

        case '0x0028': // Test Mode
        break
    }
    return resultMap
}

def updated() {
	log.debug "updated called"
	log.info "garage value : $garageSensor"
	if (garageSensor == "Yes") {
		def descriptionText = "Updating device to garage sensor"
		if (device.latestValue("status") == "open") {
			sendEvent(name: 'status', value: 'garage-open', descriptionText: descriptionText)
		}
		else if (device.latestValue("status") == "closed") {
			sendEvent(name: 'status', value: 'garage-closed', descriptionText: descriptionText)
		}
	}
	else {
		def descriptionText = "Updating device to open/close sensor"
		if (device.latestValue("status") == "garage-open") {
			sendEvent(name: 'status', value: 'open', descriptionText: descriptionText)
		}
		else if (device.latestValue("status") == "garage-closed") {
			sendEvent(name: 'status', value: 'closed', descriptionText: descriptionText)
		}
	}
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
		log.debug "Battery"
        log.debug rawValue
		def linkText = getLinkText(device)

		def result = [
		name: 'battery',
        value: '--'
		]

		def volts = rawValue / 10
		def descriptionText
        
        if (rawValue == 255) {}
        else {
		
        if (volts > 3.5) {
			result.descriptionText = "${linkText} battery has too much power (${volts} volts)."
		}
		else {
			def minVolts = 2.1
			def maxVolts = 3.0
			def pct = (volts - minVolts) / (maxVolts - minVolts)
			result.value = Math.min(100, (int) pct * 100)
			result.descriptionText = "${linkText} battery was ${result.value}%"
		}}

		return result
	}

	private Map getTemperatureResult(value) {
		log.debug "Temperature"
		def linkText = getLinkText(device)
		if (tempOffset) {
			def offset = tempOffset as int
			def v = value as int
			value = v + offset
		}
		def descriptionText = "${linkText} was ${value}°${temperatureScale}"
		return [
		name: 'temperature',
		value: value,
		descriptionText: descriptionText
		]
	}

	private Map getContactResult(value) {
		log.debug "Contact"
		def linkText = getLinkText(device)
		def descriptionText = "${linkText} was ${value == 'open' ? 'opened' : 'closed'}"
		sendEvent(name: 'contact', value: value, descriptionText: descriptionText, displayed:false)
		sendEvent(name: 'status', value: value, descriptionText: descriptionText)
	}

	private getAccelerationResult(numValue) {
		log.debug "Acceleration"
        def name = "acceleration"
		def value = numValue.endsWith("1") ? "active" : "inactive"
		def linkText = getLinkText(device)
		def descriptionText = "$linkText was $value"
		def isStateChange = isStateChange(device, name, value)
		[
			name: name,
			value: value,
			descriptionText: descriptionText,
			isStateChange: isStateChange
		]
	}

	def refresh() {
		log.debug "Refreshing Values "
        
        def refreshCmds = []
        
		if (device.getDataValue("manufacturer") == "SmartThings") {
        	
			log.debug "Refreshing Values for manufacturer: SmartThings "
         	refreshCmds = refreshCmds + [

	            /* These values of Motion Threshold Multiplier(01) and Motion Threshold (D200) 
	               seem to be giving pretty accurate results for the XYZ co-ordinates for this manufacturer. 
	               Separating these out in a separate if-else because I do not want to touch Centralite part 
	               as of now. 
	            */

	            "zcl mfg-code ${manufacturerCode}", "delay 200",
	            "zcl global write 0xFC02 0 0x20 {01}", "delay 200",
	            "send 0x${device.deviceNetworkId} 1 1", "delay 400",
	            
	            "zcl mfg-code ${manufacturerCode}", "delay 200",
	            "zcl global write 0xFC02 2 0x21 {D200}", "delay 200",
            	"send 0x${device.deviceNetworkId} 1 1", "delay 400",
                
            ]
            
        
        } else {
             refreshCmds = refreshCmds + [

                /* sensitivity - default value (8) */
	            "zcl mfg-code ${manufacturerCode}", "delay 200",
                "zcl global write 0xFC02 0 0x20 {02}", "delay 200",
            	"send 0x${device.deviceNetworkId} 1 1", "delay 400",
            ]
        }
        
        //Common refresh commands
        refreshCmds = refreshCmds + [
            "st rattr 0x${device.deviceNetworkId} 1 0x402 0", "delay 200",
            "st rattr 0x${device.deviceNetworkId} 1 1 0x20", "delay 200",

            "zcl mfg-code ${manufacturerCode}", "delay 200",
            "zcl global read 0xFC02 0x0010",
            "send 0x${device.deviceNetworkId} 1 1","delay 400"
        ]

		return refreshCmds + enrollResponse()
	}

	def configure() {

		String zigbeeEui = swapEndianHex(device.hub.zigbeeEui)
		log.debug "Configuring Reporting"
		
		def configCmds = [

		"zcl global write 0x500 0x10 0xf0 {${zigbeeEui}}", "delay 200",
		"send 0x${device.deviceNetworkId} 1 ${endpointId}", "delay 500",

		"zdo bind 0x${device.deviceNetworkId} ${endpointId} 1 1 {${device.zigbeeId}} {}", "delay 200",
		"zcl global send-me-a-report 1 0x20 0x20 30 21600 {01}",		//checkin time 6 hrs
		"send 0x${device.deviceNetworkId} 1 ${endpointId}", "delay 500",

		"zdo bind 0x${device.deviceNetworkId} ${endpointId} 1 0x402 {${device.zigbeeId}} {}", "delay 200",
		"zcl global send-me-a-report 0x402 0 0x29 30 3600 {6400}",
		"send 0x${device.deviceNetworkId} 1 ${endpointId}", "delay 500",

		"zdo bind 0x${device.deviceNetworkId} ${endpointId} 1 0xFC02 {${device.zigbeeId}} {}", "delay 200",
		"zcl mfg-code ${manufacturerCode}",
		"zcl global send-me-a-report 0xFC02 0x0010 0x18 10 3600 {01}",
		"send 0x${device.deviceNetworkId} 1 ${endpointId}", "delay 500",

		"zcl mfg-code ${manufacturerCode}",
		"zcl global send-me-a-report 0xFC02 0x0012 0x29 1 3600 {01}",
		"send 0x${device.deviceNetworkId} 1 ${endpointId}", "delay 500",

		"zcl mfg-code ${manufacturerCode}",
		"zcl global send-me-a-report 0xFC02 0x0013 0x29 1 3600 {01}",
		"send 0x${device.deviceNetworkId} 1 ${endpointId}", "delay 500",

		"zcl mfg-code ${manufacturerCode}",
		"zcl global send-me-a-report 0xFC02 0x0014 0x29 1 3600 {01}",
		"send 0x${device.deviceNetworkId} 1 ${endpointId}", "delay 500"

		]
   
   return configCmds + refresh()
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
		"send 0x${device.deviceNetworkId} 1 ${endpointId}", "delay 500",
		//Enroll Response
		"raw 0x500 {01 23 00 00 00}",
		"send 0x${device.deviceNetworkId} 1 1", "delay 200"
	]
}


private Map parseAxis(String description) {
	log.debug "parseAxis"
	def xyzResults = [x: 0, y: 0, z: 0]
    def parts = description.split("2900")
    parts[0] = "12" + parts[0]
    parts.each { part ->
    	part = part.trim()
        if (part.startsWith("12")) {
    		def unsignedX = hexToInt(part.split("12")[1].trim())
			def signedX = unsignedX > 32767 ? unsignedX - 65536 : unsignedX
			xyzResults.x = signedX
            log.debug "X Part: ${signedX}"
        }
        // Y and the Z axes are interchanged between SmartThings's implementation and Centralite's implementation
        else if (part.startsWith("13")) {
			def unsignedY = hexToInt(part.split("13")[1].trim())
			def signedY = unsignedY > 32767 ? unsignedY - 65536 : unsignedY
			if (device.getDataValue("manufacturer") == "SmartThings") {
                xyzResults.z = -signedY
                log.debug "Z Part: ${xyzResults.z}"
                if (garageSensor == "Yes")
                    garageEvent(xyzResults.z)
            } 
            else {
                xyzResults.y = signedY
                log.debug "Y Part: ${signedY}"
            }
        }
        else if (part.startsWith("14")) {
			def unsignedZ = hexToInt(part.split("14")[1].trim())
			def signedZ = unsignedZ > 32767 ? unsignedZ - 65536 : unsignedZ
			if (device.getDataValue("manufacturer") == "SmartThings") {
                xyzResults.y = signedZ
                log.debug "Y Part: ${signedZ}"
            } else {
                xyzResults.z = signedZ
                log.debug "Z Part: ${signedZ}"
                if (garageSensor == "Yes")
                    garageEvent(signedZ)
            
            }
        }
    }

	getXyzResult(xyzResults, description)
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
		def linkText = getLinkText(device)
		def descriptionText = "${linkText} was ${contactValue == 'open' ? 'opened' : 'closed'}"
		sendEvent(name: 'contact', value: contactValue, descriptionText: descriptionText, displayed:false)
		sendEvent(name: 'status', value: garageValue, descriptionText: descriptionText)
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


