/**
 *  Iris Smart Button
 *
 *  Copyright 2015 Mitch Pond
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
	definition (name: "Iris Smart Button", namespace: "mitchpond", author: "Mitch Pond") {
		capability "Battery"
		capability "Button"
        capability "Configuration"
        capability "Refresh"
		capability "Sensor"
        capability "Temperature Measurement"

		command "test"
        
        attribute "lastPress", "string"

		fingerprint endpointId: "01", profileId: "0104", inClusters: "0000,0001,0003,0007,0020,0402,0B05", outClusters: "0003,0006,0019", model:"3460-L", manufacturer: "CentraLite"
	}
    
    simulator {
   		status "button 2 pressed": "catchall: 0104 0006 02 01 0140 00 6F37 01 00 0000 01 00"
        status "button 2 released": "catchall: 0104 0006 02 01 0140 00 6F37 01 00 0000 00 00"
    }
    
    preferences{
    	input ("holdTime", "number", title: "Minimum time in seconds for a press to count as \"held\"",
        		defaultValue: 3, displayDuringSetup: false)
        input ("tempOffset", "number", title: "Enter an offset to adjust the reported temperature",
        		defaultValue: 0, displayDuringSetup: false)
    }

	tiles(scale: 2) {
    	standardTile("button", "device.button", decoration: "flat", width: 2, height: 2) {
        	state "default", icon: "st.unknown.zwave.remote-controller", backgroundColor: "#ffffff"//, action: "test()"
        }
		valueTile("battery", "device.battery", decoration: "flat", width: 2, height: 2) {
			state "battery", label:'${currentValue}% battery', unit:""
		}
        valueTile("temperature", "device.temperature", width: 2, height: 2) {
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
        standardTile("refresh", "device.refresh", decoration: "flat", width: 2, height: 2) {
			state "default", action:"refresh.refresh", icon:"st.secondary.refresh"
		}

		main (["temperature"])
		details(["button","temperature","battery","refresh"])
	}
}

def parse(String description) {
	log.debug "Parsing '${description}'"
    def descMap = zigbee.parseDescriptionAsMap(description)
    //log.debug descMap
    
	def results = []
    if (description?.startsWith('catchall:'))
		results = parseCatchAllMessage(descMap)
	else if (description?.startsWith('read attr -'))
		results = parseReportAttributeMessage(descMap)
    else if (description?.startsWith('temperature: '))
		results = parseCustomMessage(description)  
	return results;
}

def configure(){
	[
	"zdo bind 0x${device.deviceNetworkId} 1 1 6 {${device.zigbeeId}} {}", "delay 200",
    "zdo bind 0x${device.deviceNetworkId} 1 1 0x402 {${device.zigbeeId}} {}", "delay 200",
    
    "zcl global send-me-a-report 0x402 0 0x29 30 3600 {6400}", "delay 100",
	"send 0x${device.deviceNetworkId} 1 1", "delay 200",
    
    "zcl global send-me-a-report 1 0x20 0x20 3600 86400 {01}", "delay 100", //battery report request
	"send 0x${device.deviceNetworkId} 1 1", "delay 200"
    ] + refresh()
}

def refresh(){
	[
    "st rattr 0x${device.deviceNetworkId} 1 1 0x20",
    "st rattr 0x${device.deviceNetworkId} 1 0x402 0"
    ]
}

private Map parseCustomMessage(String description) {
	if (description?.startsWith('temperature: ')) {
		def value = zigbee.parseHATemperatureValue(description, "temperature: ", getTemperatureScale())
		createTempEvent(value)
	}
}

def parseCatchAllMessage(descMap) {
	//log.debug (descMap)
    if (descMap?.clusterId == "0006" && descMap?.command == "01") 		//button pressed
    	createPressEvent(descMap.sourceEndpoint as int)
    else if (descMap?.clusterId == "0006" && descMap?.command == "00") 	//button released
    	createButtonEvent(descMap.sourceEndpoint as int)
    else if (descMap?.clusterId == "0402" && descMap?.command == "01") 	//temperature response
    	parseTempAttributeMsg(descMap)
}

def parseReportAttributeMessage(descMap) {
	if (descMap?.cluster == "0001" && descMap?.attrId == "0020") createBatteryEvent(getBatteryLevel(descMap.value))
    else if (descMap?.cluster == "0402" && descMap?.attrId == "0000") createTempEvent(getTemperature(descMap.value))
}

private parseTempAttributeMsg(descMap) {
	String temp = descMap.data[-2..-1].reverse().join()
    createTempEvent(getTemperature(temp))
}

private createTempEvent(value) {
	return createEvent(getTemperatureResult(value))
}

private createBatteryEvent(percent) {
	log.debug "Battery level at " + percent
	return createEvent([name: "battery", value: percent])
}

//this method determines if a press should count as a push or a hold and returns the relevant event type
private createButtonEvent(button) {
	def currentTime = now()
    def startOfPress = device.latestState('lastPress').date.getTime()
    def timeDif = currentTime - startOfPress
    def holdTimeMillisec = (settings.holdTime?:3).toInteger() * 1000
    
    if (timeDif < 0) 
    	return []	//likely a message sequence issue. Drop this press and wait for another. Probably won't happen...
    else if (timeDif < holdTimeMillisec) 
    	return createButtonPushedEvent(button)
    else 
    	return createButtonHeldEvent(button)
}

private createPressEvent(button) {
	return createEvent([name: 'lastPress', value: now(), data:[buttonNumber: button], displayed: false])
}

private createButtonPushedEvent(button) {
	log.debug "Button ${button} pushed"
	return createEvent([
    	name: "button",
        value: "pushed", 
        data:[buttonNumber: button], 
        descriptionText: "${device.displayName} button ${button} was pushed",
        isStateChange: true, 
        displayed: true])
}

private createButtonHeldEvent(button) {
	log.debug "Button ${button} held"
	return createEvent([
    	name: "button",
        value: "held", 
        data:[buttonNumber: button], 
        descriptionText: "${device.displayName} button ${button} was held",
        isStateChange: true])
}

private getBatteryLevel(rawValue) {
	def intValue = Integer.parseInt(rawValue,16)
	def min = 2.1
    def max = 3.0
    def vBatt = intValue / 10
    return ((vBatt - min) / (max - min) * 100) as int
}

def getTemperature(value) {
	def celsius = Integer.parseInt(value, 16).shortValue() / 100
	if(getTemperatureScale() == "C"){
		return celsius
	} else {
		return celsiusToFahrenheit(celsius) as Integer
	}
}

private Map getTemperatureResult(value) {
	log.debug 'TEMP'
	def linkText = getLinkText(device)
	if (tempOffset) {
		def offset = tempOffset as int
		def v = value as int
		value = v + offset
	}
	def descriptionText = "${linkText} was ${value}°${temperatureScale}"
    log.debug "Temp value: "+value
	return [
		name: 'temperature',
		value: value,
		descriptionText: descriptionText
	]
}

// handle commands
def test() {
    log.debug "Test"
	//zigbee.refreshData("0","4") + zigbee.refreshData("0","5") + zigbee.refreshData("1","0x0020")
    zigbee.refreshData("0x402","0")
}