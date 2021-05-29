/**
 *  SmartSense Temp/Humidity Sensor
 *
 *  Copyright 2014 SmartThings
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
	definition (name: "SmartSense Temp/Humidity",namespace: "smartthings", author: "SmartThings") {
		capability "Configuration"
		capability "Battery"
		capability "Refresh"
		capability "Temperature Measurement"
		capability "Relative Humidity Measurement"
 
 
		// fingerprint endpointId: "01", inClusters: "0001,0003,0020,0402,0B05,FC45", outClusters: "0019,0003"
	}
 
	simulator {
 
	}
 
	tiles {
		valueTile("temperature", "device.temperature", inactiveLabel: false, width: 2, height: 2) {
			state "temperature", label:'${currentValue}°',
				backgroundColors:[
					[value: 31, color: "#153591"],
					[value: 44, color: "#1e9cbb"],
					[value: 59, color: "#90d2a7"],
					[value: 74, color: "#44b621"],
					[value: 84, color: "#f1d801"],
					[value: 95, color: "#d04e00"],
					[value: 96, color: "#bc2323"]
				]
		}
		valueTile("humidity", "device.humidity", inactiveLabel: false) {
			state "humidity", label:'${currentValue}% humidity', unit:""
		}
 
		valueTile("battery", "device.battery", decoration: "flat", inactiveLabel: false) {
			state "battery", label:'${currentValue}% battery', unit:""/*, backgroundColors:[
				[value: 5, color: "#BC2323"],
				[value: 10, color: "#D04E00"],
				[value: 15, color: "#F1D801"],
				[value: 16, color: "#FFFFFF"]
			]*/
		}
        
        standardTile("refresh", "device.refresh", inactiveLabel: false, decoration: "flat") {
			state "default", action:"refresh.refresh", icon:"st.secondary.refresh"
		}
 
		main "temperature", "humidity"
		details(["temperature","humidity","refresh"])
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
	else if (description?.startsWith('temperature: ') || description?.startsWith('humidity: ')) {
		map = parseCustomMessage(description)
	}
 
	log.debug "Parse returned $map"
	return map ? createEvent(map) : null
}
 
private Map parseCatchAllMessage(String description) {
    Map resultMap = [:]
    def cluster = zigbee.parse(description)
    if (shouldProcessMessage(cluster)) {
        switch(cluster.clusterId) {
            case 0x0001:
                log.debug 'Battery'
                resultMap.name = 'battery'
                resultMap.value = getCatchallBatteryPercentage(cluster.data.last())
                break

            case 0x0402:
                log.debug 'TEMP'
                // temp is last 2 data values. reverse to swap endian
                String temp = cluster.data[-2..-1].reverse().collect { cluster.hex1(it) }.join()
                resultMap.name = 'temperature'
                resultMap.value = getTemperature(temp)
                break

			case 0xFC45:
            	log.debug 'Humidity'
                resultMap.name = 'humidity'
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

private int getCatchallBatteryPercentage(int value) {
    def minVolts = 2.1
    def maxVolts = 3.0
    def volts = value / 10
    def pct = (volts - minVolts) / (maxVolts - minVolts)
    return (int) pct * 100
}
 
private Map parseReportAttributeMessage(String description) {
	Map descMap = (description - "read attr - ").split(",").inject([:]) { map, param ->
		def nameAndValue = param.split(":")
		map += [(nameAndValue[0].trim()):nameAndValue[1].trim()]
	}
	log.debug "Desc Map: $descMap"
 
	Map resultMap = [:]
	if (descMap.cluster == "0402" && descMap.attrId == "0000") {
		log.debug "TEMP"
		resultMap.name = "temperature"
		resultMap.value = getTemperature(descMap.value)
	}
	else if (descMap.cluster == "0001" && descMap.attrId == "0020") {
		log.debug "Battery"
		resultMap.name = "battery"
		resultMap.value = calculateBattery(descMap.value)
	}
	else if (descMap.cluster == "FC45" && descMap.attrId == "0000") {
		log.debug "Humidity"
		resultMap.name = "humidity"
		resultMap.value = getReportAttributeHumidity(descMap.value)
	}
 
	return resultMap
}
 
def getReportAttributeHumidity(String value) {
    def humidity = null
    if (value?.trim()) {
        try {
        	// value is hex with no decimal
            def pct = Integer.parseInt(value.trim(), 16) / 100
            humidity = String.format('%3.0f', pct)
        } catch(NumberFormatException nfe) {
            log.debug "Error converting $value to humidity"
        }
    }
    return humidity
}
 
private Map parseCustomMessage(String description) {
	def name = null
	def value = null
	if (description?.startsWith('temperature: ')) {
		log.debug "TEMP"
		name = 'temperature'
		value = zigbee.parseHATemperatureValue(description, "temperature: ", getTemperatureScale())
	}
	else if (description?.startsWith('humidity: ')) {
		log.debug "Humidity"
		name = 'humidity'
		def pct = (description - "humidity: " - "%").trim()
		if (pct.isNumber()) {
			value = Math.round(new BigDecimal(pct)).toString()
		}
	}
	def unit = name == "temperature" ? getTemperatureScale() : (name == "humidity" ? "%" : null)
	return [name: name, value: value, unit: unit]
}
 
def getTemperature(value) {
	def celsius = Integer.parseInt(value, 16) / 100
	if(getTemperatureScale() == "C"){
		return celsius
	} else {
		return celsiusToFahrenheit(celsius) as Integer
	}
}

def refresh()
{
	log.debug "refresh temperature, humidity, and battery"
	[
		
		"zcl mfg-code 0xC2DF", "delay 1000",
		"zcl global read 0xFC45 0", "delay 1000",
		"send 0x${device.deviceNetworkId} 1 1", "delay 1000",
        "st rattr 0x${device.deviceNetworkId} 1 0x402 0", "delay 200",

	]
}

def updated() {
	log.debug "sending humidity reporting values"
	[
     "raw 0xFC45 {04 DF C2 08 06 00 00 00 21 64 00 2C 01 64}", "delay 200",
     "send 0x${device.deviceNetworkId} 1 1", "delay 1500",
    ]
}
        

def configure() {

	String zigbeeId = swapEndianHex(device.hub.zigbeeId)
	log.debug "Confuguring Reporting and Bindings."
	def configCmds = [	
  
        
        "zcl global send-me-a-report 1 0x20 0x20 300 3600 {0100}", "delay 500",
        "send 0x${device.deviceNetworkId} 1 1", "delay 1000",
        
        "zcl global send-me-a-report 0x402 0 0x29 300 3600 {6400}", "delay 200",
        "send 0x${device.deviceNetworkId} 1 1", "delay 1500",
        
        "raw 0xFC45 {04 DF C2 08 06 00 00 00 21 2C 01 10 0E 64}", "delay 200",
        "send 0x${device.deviceNetworkId} 1 1", "delay 1500",
        
        "zdo bind 0x${device.deviceNetworkId} 1 1 0xFC45 {${device.zigbeeId}} {}", "delay 1000",
		"zdo bind 0x${device.deviceNetworkId} 1 1 0x402 {${device.zigbeeId}} {}", "delay 500",
		"zdo bind 0x${device.deviceNetworkId} 1 1 1 {${device.zigbeeId}} {}"
	]
    return configCmds // + refresh() // send refresh cmds as part of config
}

private hex(value) {
	new BigInteger(Math.round(value).toString()).toString(16)
}

private calculateBattery(value) {
	def min = 2300
	def percent = (Integer.parseInt(value, 16) - min) / 10
	// Make sure our percentage is between 0 - 100
	percent = Math.max(0.0, Math.min(percent, 100.0))
	percent
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