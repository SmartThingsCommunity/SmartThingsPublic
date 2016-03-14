/*
===============================================================================
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
===============================================================================
 *  Purpose: SmartSense Motion Sensor DTH File
 *
 *  Filename: SmartSense-Motion-Sensor.src/SmartSense-Motion-Sensor.groovy
 *
 *  Change History:
 *  1. 20160116 TW - Update/Edit to support i18n translations
 *  2. 20160125 TW = Incorporated new battery mapping from TM
===============================================================================
 */

metadata {
	definition (name: "SmartSense Motion Sensor", namespace: "smartthings", author: "SmartThings") {
		capability "Motion Sensor"
		capability "Configuration"
		capability "Battery"
        capability "Temperature Measurement"
		capability "Refresh"
        
        command "enrollResponse"

		fingerprint inClusters: "0000,0001,0003,0402,0500,0020,0B05", outClusters: "0019", manufacturer: "CentraLite", model: "3305-S"
        fingerprint inClusters: "0000,0001,0003,0402,0500,0020,0B05", outClusters: "0019", manufacturer: "CentraLite", model: "3325-S", deviceJoinName: "Motion Sensor"
        fingerprint inClusters: "0000,0001,0003,0402,0500,0020,0B05", outClusters: "0019", manufacturer: "CentraLite", model: "3305"
        fingerprint inClusters: "0000,0001,0003,0402,0500,0020,0B05", outClusters: "0019", manufacturer: "CentraLite", model: "3325"
        fingerprint inClusters: "0000,0001,0003,0402,0500,0020,0B05", outClusters: "0019", manufacturer: "CentraLite", model: "3326"
        fingerprint inClusters: "0000,0001,0003,000F,0020,0402,0500", outClusters: "0019", manufacturer: "SmartThings", model: "motionv4", deviceJoinName: "Motion Sensor"
	}

	simulator {
		status "active": "zone report :: type: 19 value: 0031"
		status "inactive": "zone report :: type: 19 value: 0030"
	}

	preferences {
		section {
			image(name: 'educationalcontent', multiple: true, images: [
				"http://cdn.device-gse.smartthings.com/Motion/Motion1.jpg",
				"http://cdn.device-gse.smartthings.com/Motion/Motion2.jpg",
				"http://cdn.device-gse.smartthings.com/Motion/Motion3.jpg"
				])
		}
		section {
			input title: "Temperature Offset", description: "This feature allows you to correct any temperature variations by selecting an offset. Ex: If your sensor consistently reports a temp that's 5 degrees too warm, you'd enter '-5'. If 3 degrees too cold, enter '+3'.", displayDuringSetup: false, type: "paragraph", element: "paragraph"
			input "tempOffset", "number", title: "Degrees", description: "Adjust temperature by this many degrees", range: "*..*", displayDuringSetup: false
		}
	}

	tiles(scale: 2) {
		multiAttributeTile(name:"motion", type: "generic", width: 6, height: 4){
			tileAttribute ("device.motion", key: "PRIMARY_CONTROL") {
				attributeState "active", label:'motion', icon:"st.motion.motion.active", backgroundColor:"#53a7c0"
				attributeState "inactive", label:'no motion', icon:"st.motion.motion.inactive", backgroundColor:"#ffffff"
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

		main(["motion", "temperature"])
		details(["motion", "temperature", "battery", "refresh"])
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

			case 0x0406:
            	log.debug 'motion'
                resultMap.name = 'motion'
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
    else if (descMap.cluster == "0406" && descMap.attrId == "0000") {
    	def value = descMap.value.endsWith("01") ? "active" : "inactive"
    	resultMap = getMotionResult(value)
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
        case '0x0020': // Closed/No Motion/Dry
        	resultMap = getMotionResult('inactive')
            break

        case '0x0021': // Open/Motion/Wet
        	resultMap = getMotionResult('active')
            break

        case '0x0022': // Tamper Alarm
        	log.debug 'motion with tamper alarm'
        	resultMap = getMotionResult('active')
            break

        case '0x0023': // Battery Alarm
            break

        case '0x0024': // Supervision Report
        	log.debug 'no motion with tamper alarm'
        	resultMap = getMotionResult('inactive')
            break

        case '0x0025': // Restore Report
            break

        case '0x0026': // Trouble/Failure
        	log.debug 'motion with failure alarm'
        	resultMap = getMotionResult('active')
            break

        case '0x0028': // Test Mode
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
	log.debug "Battery rawValue = ${rawValue}"
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
			result.descriptionText = "{{ device.displayName }} battery has too much power: (> 3.5) volts."
		}
		else {
			if (device.getDataValue("manufacturer") == "SmartThings") {         
				volts = rawValue // For the batteryMap to work the key needs to be an int
				def batteryMap = [28:100, 27:100, 26:100, 25:90, 24:90, 23:70,
								  22:70, 21:50, 20:50, 19:30, 18:30, 17:15, 16:1, 15:0]
				def minVolts = 15
				def maxVolts = 28

				if (volts < minVolts)
					volts = minVolts
				else if (volts > maxVolts)
					volts = maxVolts
				def pct = batteryMap[volts]
				if (pct != null) {
					result.value = pct
                    def value = pct
					result.descriptionText = "{{ device.displayName }} battery was {{ value }}"
				}
			}
			else {
				def minVolts = 2.1
				def maxVolts = 3.0
				def pct = (volts - minVolts) / (maxVolts - minVolts)
				result.value = Math.min(100, (int) pct * 100)
				result.descriptionText = "{{ device.displayName }} battery was {{ value }}"
			}
		}
	}

	return result
}

private Map getTemperatureResult(value) {
	log.debug 'TEMP'
	if (tempOffset) {
		def offset = tempOffset as int
		def v = value as int
		value = v + offset
	}
    def descriptionText
    if ( temperatureScale == 'C' )
    	descriptionText = '{{ device.displayName }} was {{ value }}°C'
    else
    	descriptionText = '{{ device.displayName }} was {{ value }}°F'

	return [
		name: 'temperature',
		value: value,
		descriptionText: descriptionText,
        translatable: true
	]
}

private Map getMotionResult(value) {
	log.debug 'motion'
	String descriptionText = value == 'active' ? "{{ device.displayName }} detected motion" : "{{ device.displayName }} motion has stopped"
    //String descriptionText = '{{ device.displayName }} is {{ value | translate }}'
	return [
		name: 'motion',
		value: value,
		descriptionText: descriptionText,
        translatable: true
	]
}

def refresh() {
	log.debug "refresh executed"
	def refreshCmds = [
		"st rattr 0x${device.deviceNetworkId} 1 0x402 0", "delay 200",
		"st rattr 0x${device.deviceNetworkId} 1 1 0x20", "delay 200"
	]

	return refreshCmds + enrollResponse()
}

def configure() {
	String zigbeeEui = swapEndianHex(device.hub.zigbeeEui)
	log.debug "Configuring Reporting, IAS CIE, and Bindings."

	def configCmds = [
		"zcl global write 0x500 0x10 0xf0 {${zigbeeEui}}", "delay 200",
		"send 0x${device.deviceNetworkId} 1 ${endpointId}", "delay 500",

		"zdo bind 0x${device.deviceNetworkId} ${endpointId} 1 1 {${device.zigbeeId}} {}", "delay 200",
		"zcl global send-me-a-report 1 0x20 0x20 30 21600 {01}",		//checkin time 6 hrs
		"send 0x${device.deviceNetworkId} 1 ${endpointId}", "delay 500",

		"zdo bind 0x${device.deviceNetworkId} ${endpointId} 1 0x402 {${device.zigbeeId}} {}", "delay 200",
		"zcl global send-me-a-report 0x402 0 0x29 300 3600 {6400}",
		"send 0x${device.deviceNetworkId} 1 ${endpointId}", "delay 500"
	]
	return configCmds + refresh() // send refresh cmds as part of config
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