/**
 *  Copyright 2017 A4refillpad
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
 *  2017-03 First release of the Xiaomi Temp/Humidity Device Handler
 *  2017-03 Includes battery level (hope it works, I've only had access to a device for a limited period, time will tell!)
 *  2017-03 Last checkin activity to help monitor health of device and multiattribute tile
 *  2017-03 Changed temperature to update on .1° changes - much more useful
 *  2017-03-08 Changed the way the battery level is being measured. Very different to other Xiaomi sensors.
 *
 *  known issue: these devices do not seem to respond to refresh requests left in place in case things change
 *	known issue: tile formatting on ios and android devices vary a little due to smartthings app - again, nothing I can do about this
 *  known issue: there's nothing I can do about the pairing process with smartthings. it is indeed non standard, please refer to community forum for details
 *
 */
metadata {
	definition (name: "Xiaomi Humidity Temperature Sensor", namespace: "ericyew", author: "ericyew") {
		capability "Relative Humidity Measurement"
		capability "Temperature Measurement"
		capability "Sensor"
        capability "Battery"
        capability "Refresh"
        
        attribute "lastCheckin", "String"
        
		fingerprint profileId: "0104", deviceId: "0302", inClusters: "0000,0001,0003,0009,0402,0405"
	}

	// simulator metadata
	simulator {
    		for (int i = 0; i <= 100; i += 10) {
			status "${i}%": "humidity: ${i}%"
		}
        
		for (int i = 0; i <= 100; i += 10) {
			status "${i}F": "temperature: $i F"
		}
	}

	// UI tile definitions
	tiles(scale: 2) {
		multiAttributeTile(name:"humidity", type:"generic", width:6, height:4) {
			tileAttribute("device.humidity", key:"PRIMARY_CONTROL"){
			    attributeState("default", label:'${currentValue}%',
                backgroundColors:[
					[value: 0, color: "#bc2323"],
					[value: 20, color: "#d04e00"],
					[value: 40, color: "#f1d801"],
					[value: 50, color: "#44b621"],
					[value: 60, color: "#90d2a7"],
					[value: 70, color: "#1e9cbb"],
					[value: 80, color: "#153591"]
				]
			)
            }
            tileAttribute("device.lastCheckin", key: "SECONDARY_CONTROL") {
    			attributeState("default", label:'Last Update: ${currentValue}', icon: "st.Health & Wellness.health9")
			}
		}
		standardTile("temperature", "device.temperature", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "default", label:'${currentValue}°', icon:"st.Weather.weather2"
		}
        
        valueTile("battery", "device.battery", decoration: "flat", inactiveLabel: false, width: 2, height: 2) {
			state "default", label:'${currentValue}% battery', unit:""
		}
        
		valueTile("humidity2", "device.humidity", decoration: "flat", inactiveLabel: false) {
			state "default", label:'${currentValue}%', icon: "st.Weather.weather12"
        }

		standardTile("refresh", "device.refresh", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "default", action:"refresh.refresh", icon:"st.secondary.refresh"
        }
            
		main(["humidity2"])
		details(["humidity", "temperature", "battery","refresh"])
	}
}

// Parse incoming device messages to generate events
def parse(String description) {
	log.debug "RAW: $description"
	def name = parseName(description)
    log.debug "Parsename: $name"
	def value = parseValue(description)
    log.debug "Parsevalue: $value"
	def unit = name == "temperature" ? getTemperatureScale() : (name == "humidity" ? "%" : null)
	def result = createEvent(name: name, value: value, unit: unit)
    log.debug "Evencreated: $name, $value, $unit"
	log.debug "Parse returned ${result?.descriptionText}"
    def now = new Date().format("yyyy MMM dd EEE h:mm:ss a", location.timeZone)
    sendEvent(name: "lastCheckin", value: now)
	return result
}

private String parseName(String description) {

	if (description?.startsWith("temperature: ")) {
		return "temperature"
        
	} else if (description?.startsWith("humidity: ")) {
		return "humidity"
        
	} else if (description?.startsWith("catchall: ")) {
		return "battery"
	}
	null
}

private String parseValue(String description) {

	if (description?.startsWith("temperature: ")) {
		def value = ((description - "temperature: ").trim()) as Float 
        return (Math.round(value * 10))/ 10 as Float
        
	} else if (description?.startsWith("humidity: ")) {
		def pct = (description - "humidity: " - "%").trim()
        
		if (pct.isNumber()) {
			return Math.round(new BigDecimal(pct)).toString()
		}
	} else if (description?.startsWith("catchall: ")) {
		return parseCatchAllMessage(description)
	} else {
    log.debug "unknown: $description"
    sendEvent(name: "unknown", value: description)
    }
	null
}

private String parseCatchAllMessage(String description) {
	def result = "?"
	def cluster = zigbee.parse(description)
	log.debug cluster
	if (cluster) {
		switch(cluster.clusterId) {
			case 0x0000:
//			result = getBatteryResult(cluster.data.get(23))
			result = getBatteryResult(cluster.data.get(6)) 
 			break
		}
	}

	return result
}


private String getBatteryResult(rawValue) {
	log.debug 'Battery'
	def linkText = getLinkText(device)
	log.debug rawValue

	def result =  '--'
    def maxBatt = 100
    def battLevel = Math.round(rawValue * 100 / 255)
	
	if (battLevel > maxBatt) {
				battLevel = maxBatt
    }

	return battLevel
}

def refresh() {
	log.debug "refresh called"
	def refreshCmds = [
		"st rattr 0x${device.deviceNetworkId} 1 1 0x00", "delay 2000",
		"st rattr 0x${device.deviceNetworkId} 1 1 0x20", "delay 2000"
	]

	return refreshCmds + enrollResponse()
}

def configure() {
	// Device-Watch allows 2 check-in misses from device + ping (plus 1 min lag time)
	// enrolls with default periodic reporting until newer 5 min interval is confirmed
	sendEvent(name: "checkInterval", value: 2 * 60 * 60 + 1 * 60, displayed: false, data: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID])

	// temperature minReportTime 30 seconds, maxReportTime 5 min. Reporting interval if no activity
	// battery minReport 30 seconds, maxReportTime 6 hrs by default
	return refresh() + zigbee.batteryConfig() + zigbee.temperatureConfig(30, 900) // send refresh cmds as part of config
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
