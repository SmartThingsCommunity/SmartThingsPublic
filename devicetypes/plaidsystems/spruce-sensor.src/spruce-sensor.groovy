/**
 *  Spruce Sensor -updated with SLP3 model number 3/2019
 *
 *  Copyright 2014 Plaid Systems
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
 -------10/20/2015 Updates--------
 -Fix/add battery reporting interval to update
 -remove polling and/or refresh
 
 -------5/2017 Updates--------
 -Add fingerprints for SLP
 -add device health, check every 60mins + 2mins
 
 -------3/2019 Updates--------
 -Add fingerprints for SLP3
 -change device health from 62mins to 3 hours
 */
 
metadata {
	definition (name: "Spruce Sensor", namespace: "plaidsystems", author: "Plaid Systems") {
		
		capability "Configuration"
		capability "Battery"
        capability "Relative Humidity Measurement"
        capability "Temperature Measurement"
        capability "Sensor"
        capability "Health Check"
        //capability "Polling"
		
        attribute "maxHum", "string"
        attribute "minHum", "string"        
        
        
        command "resetHumidity"
        command "refresh"
        
        fingerprint profileId: "0104", inClusters: "0000,0001,0003,0402,0405", outClusters: "0003, 0019", manufacturer: "PLAID SYSTEMS", model: "PS-SPRZMS-01", deviceJoinName: "Spruce Sensor"
        fingerprint profileId: "0104", inClusters: "0000,0001,0003,0402,0405", outClusters: "0003, 0019", manufacturer: "PLAID SYSTEMS", model: "PS-SPRZMS-SLP1", deviceJoinName: "Spruce Sensor"
        fingerprint profileId: "0104", inClusters: "0000,0001,0003,0402,0405", outClusters: "0003, 0019", manufacturer: "PLAID SYSTEMS", model: "PS-SPRZMS-SLP3", deviceJoinName: "Spruce Sensor"
	}

	preferences {
		input "tempOffset", "number", title: "Temperature Offset", description: "Adjust temperature by this many degrees", range: "*..*", displayDuringSetup: false
        input "interval", "number", title: "Report Interval", description: "How often the device should report in minutes", range: "1..120", defaultValue: 10, displayDuringSetup: false
        input "resetMinMax", "bool", title: "Reset Humidity min and max", required: false, displayDuringSetup: false
      }

	tiles {
		valueTile("temperature", "device.temperature", canChangeIcon: false, canChangeBackground: false) {
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
		valueTile("humidity", "device.humidity", width: 2, height: 2, canChangeIcon: false, canChangeBackground: true) {
			state "humidity", label:'${currentValue}%', unit:"",
            	backgroundColors:[
					[value: 0, color: "#635C0C"],
					[value: 16, color: "#EBEB21"],
					[value: 22, color: "#C7DE6A"],
					[value: 42, color: "#9AD290"],
					[value: 64, color: "#44B621"],
					[value: 80, color: "#3D79D9"],
					[value: 96, color: "#0A50C2"]
				], icon:"st.Weather.weather12"
		}
        
        valueTile("maxHum", "device.maxHum", canChangeIcon: false, canChangeBackground: false) {
			state "maxHum", label:'High ${currentValue}%', unit:"",
            	backgroundColors:[
					[value: 0, color: "#635C0C"],
					[value: 16, color: "#EBEB21"],
					[value: 22, color: "#C7DE6A"],
					[value: 42, color: "#9AD290"],
					[value: 64, color: "#44B621"],
					[value: 80, color: "#3D79D9"],
					[value: 96, color: "#0A50C2"]
				]
		}
		valueTile("minHum", "device.minHum", canChangeIcon: false, canChangeBackground: false) {
			state "minHum", label:'Low ${currentValue}%', unit:"",
            	backgroundColors:[
					[value: 0, color: "#635C0C"],
					[value: 16, color: "#EBEB21"],
					[value: 22, color: "#C7DE6A"],
					[value: 42, color: "#9AD290"],
					[value: 64, color: "#44B621"],
					[value: 80, color: "#3D79D9"],
					[value: 96, color: "#0A50C2"]
				]
		} 
 
		valueTile("battery", "device.battery", decoration: "flat", canChangeIcon: false, canChangeBackground: false) {
			state "battery", label:'${currentValue}% battery'
		}
        
		main  (["humidity"])
		details(["humidity","maxHum","minHum","temperature","battery"])
	}
}

def parse(String description) {
	log.debug "Parse description $description config: ${device.latestValue('configuration')} interval: $interval"      
    
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
    def result = map ? createEvent(map) : null
 	
    //check in configuration change
    if (!device.latestValue('configuration')) result = poll()
    if (device.latestValue('configuration') as float != interval && interval != null) {
        result = poll()            
    }
 	log.debug "result: $result"
    return result
    
}



private Map parseCatchAllMessage(String description) {
    Map resultMap = [:]
    def linkText = getLinkText(device)
	//log.debug "Catchall"
    def descMap = zigbee.parse(description)
    
    //check humidity configuration is complete
    if (descMap.command == 0x07 && descMap.clusterId == 0x0405){    	
        def configInterval = 10
        if (interval != null) configInterval = interval        
        sendEvent(name: 'configuration',value: configInterval, descriptionText: "Configuration Successful")        
        //setConfig()
        log.debug "config complete"        
        //return resultMap = [name: 'configuration', value: configInterval, descriptionText: "Settings configured successfully"]                
    }
    else if (descMap.command == 0x0001){    
    	def hexString = "${hex(descMap.data[5])}" + "${hex(descMap.data[4])}"
    	def intString = Integer.parseInt(hexString, 16)    
    	//log.debug "command: $descMap.command clusterid: $descMap.clusterId $hexString $intString"
    
    	if (descMap.clusterId == 0x0402){    	
            def value = getTemperature(hexString)
            resultMap = getTemperatureResult(value)    
        }
        else if (descMap.clusterId == 0x0405){
            def value = Math.round(new BigDecimal(intString / 100)).toString()
            resultMap = getHumidityResult(value)

        }
        else return null
    }
    else return null 
    
    return resultMap
}    
    
private Map parseReportAttributeMessage(String description) {	
    def descMap = parseDescriptionAsMap(description)
	log.debug "Desc Map: $descMap"
    log.debug "Report Attributes"
 
	Map resultMap = [:]
	if (descMap.cluster == "0001" && descMap.attrId == "0000") {		
        resultMap = getBatteryResult(descMap.value)
	}    
    return resultMap
}

def parseDescriptionAsMap(description) {
	(description - "read attr - ").split(",").inject([:]) { map, param ->
		def nameAndValue = param.split(":")
		map += [(nameAndValue[0].trim()):nameAndValue[1].trim()]
	}
}

private Map parseCustomMessage(String description) {
	Map resultMap = [:]  
        
	log.debug "parseCustom"
	if (description?.startsWith('temperature: ')) {
		def value = zigbee.parseHATemperatureValue(description, "temperature: ", getTemperatureScale())
		resultMap = getTemperatureResult(value)
	}
	else if (description?.startsWith('humidity: ')) {
		def pct = (description - "humidity: " - "%").trim()
		if (pct.isNumber()) {        	
            def value = Math.round(new BigDecimal(pct)).toString()
			resultMap = getHumidityResult(value)
		} else {
			log.error "invalid humidity: ${pct}"
		}    
	}
	return resultMap
}
 
private Map getHumidityResult(value) {
    def linkText = getLinkText(device)
    def maxHumValue = 0
    def minHumValue = 0
    if (device.currentValue("maxHum") != null) maxHumValue = device.currentValue("maxHum").toInteger()
    if (device.currentValue("minHum") != null) minHumValue = device.currentValue("minHum").toInteger()
    log.debug "Humidity max: ${maxHumValue} min: ${minHumValue}"
    def compare = value.toInteger()
    
    if (compare > maxHumValue) {
        sendEvent(name: 'maxHum', value: value, unit: '%', descriptionText: "${linkText} soil moisture high is ${value}%")
        }
    else if (((compare < minHumValue) || (minHumValue <= 2)) && (compare != 0)) {
        sendEvent(name: 'minHum', value: value, unit: '%', descriptionText: "${linkText} soil moisture low is ${value}%")
        }    
    
    return [
    	name: 'humidity',
    	value: value,
    	unit: '%',
        descriptionText: "${linkText} soil moisture is ${value}%"
    ]
}



def getTemperature(value) {
	def celsius = (Integer.parseInt(value, 16).shortValue()/100)
    //log.debug "Report Temp $value : $celsius C"
	if(getTemperatureScale() == "C"){
		return celsius
	} else {
		return celsiusToFahrenheit(celsius) as Integer
	}
}

private Map getTemperatureResult(value) {
	log.debug "Temperature: $value"
	def linkText = getLinkText(device)
        
	if (tempOffset) {
		def offset = tempOffset as int
		def v = value as int
		value = v + offset        
	}
	def descriptionText = "${linkText} is ${value}°${temperatureScale}"
	return [
		name: 'temperature',
		value: value,
		descriptionText: descriptionText,
		unit: temperatureScale
	]
}

private Map getBatteryResult(value) {
	log.debug 'Battery'
	def linkText = getLinkText(device)
        
    def result = [
    	name: 'battery'
    ]
    	
	def min = 2500   
	def percent = ((Integer.parseInt(value, 16) - min) / 5)
	percent = Math.max(0, Math.min(percent, 100.0))
    result.value = Math.round(percent)
    
    def descriptionText
    if (percent < 10) result.descriptionText = "${linkText} battery is getting low $percent %."
	else result.descriptionText = "${linkText} battery is ${result.value}%"
	
	return result
}

def resetHumidity(){
	def linkText = getLinkText(device)
    def minHumValue = 0
    def maxHumValue = 0
    sendEvent(name: 'minHum', value: minHumValue, unit: '%', descriptionText: "${linkText} min soil moisture reset to ${minHumValue}%")
    sendEvent(name: 'maxHum', value: maxHumValue, unit: '%', descriptionText: "${linkText} max soil moisture reset to ${maxHumValue}%")
}	

def setConfig(){
	def configInterval = 100
    if (interval != null) configInterval = interval        
    sendEvent(name: 'configuration',value: configInterval, descriptionText: "Configuration initialized")
}

def installed(){
	//check every 62 minutes
    sendEvent(name: "checkInterval", value: 60 * 60 + 2 * 60, displayed: false, data: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID])
}

//when device preferences are changed
def updated(){	
    log.debug "device updated"
    if (!device.latestValue('configuration')) configure()
    else{
    	if (resetMinMax == true) resetHumidity()
        if (device.latestValue('configuration') as float != interval && interval != null){
            sendEvent(name: 'configuration',value: 0, descriptionText: "Settings changed and will update at next report. Measure interval set to ${interval} mins")
    	}
    }
    //check every 62mins or interval + 120s
    def reportingInterval  = interval * 60 + 2 * 60
    if (reportingInterval < 3720) reportingInterval = 3720
    sendEvent(name: "checkInterval", value: reportingInterval, displayed: false, data: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID])
}

//poll
def poll() {
	log.debug "poll called"
    List cmds = []
    if (!device.latestValue('configuration')) cmds += configure()
    else if (device.latestValue('configuration').toInteger() != interval && interval != null) { 
    	cmds += intervalUpdate()
    }
    //cmds += refresh()
    log.debug "commands $cmds"
    return cmds?.collect { new physicalgraph.device.HubAction(it) }    
}

//update intervals
def intervalUpdate(){
	log.debug "intervalUpdate"
    def minReport = 10
    def maxReport = 610
    if (interval != null) {
    	minReport = interval
        maxReport = interval * 61
    }
    [    
    	"zcl global send-me-a-report 0x405 0x0000 0x21 $minReport $maxReport {6400}", "delay 500",
    	"send 0x${device.deviceNetworkId} 1 1", "delay 500",
        "zcl global send-me-a-report 1 0x0000 0x21 0x0C 0 {0500}", "delay 500",
        "send 0x${device.deviceNetworkId} 1 1", "delay 500",
    ]    
}

def refresh() {
	log.debug "refresh"
    [
        "st rattr 0x${device.deviceNetworkId} 1 0x402 0", "delay 500",
        "st rattr 0x${device.deviceNetworkId} 1 0x405 0", "delay 500",    
        "st rattr 0x${device.deviceNetworkId} 1 1 0"
    ]    
}

//configure
def configure() {
	//set minReport = measurement in minutes
    def minReport = 10
    def maxReport = 610
	    
    //String zigbeeId = swapEndianHex(device.hub.zigbeeId)
	//log.debug "zigbeeid ${device.zigbeeId} deviceId ${device.deviceNetworkId}"
    if (!device.zigbeeId) sendEvent(name: 'configuration',value: 0, descriptionText: "Device Zigbee Id not found, remove and attempt to rejoin device")
    else sendEvent(name: 'configuration',value: 100, descriptionText: "Configuration initialized")
    //log.debug "Configuring Reporting and Bindings. min: $minReport max: $maxReport "
	
    [
        "zdo bind 0x${device.deviceNetworkId} 1 1 0x402 {${device.zigbeeId}} {}", "delay 500",
        "zdo bind 0x${device.deviceNetworkId} 1 1 0x405 {${device.zigbeeId}} {}", "delay 500",                
		"zdo bind 0x${device.deviceNetworkId} 1 1 1 {${device.zigbeeId}} {}", "delay 1000",
        
        //temperature
        "zcl global send-me-a-report 0x402 0x0000 0x29 1 0 {3200}",
        "send 0x${device.deviceNetworkId} 1 1", "delay 500",
        
        //min = soil measure interval
        "zcl global send-me-a-report 0x405 0x0000 0x21 $minReport $maxReport {6400}",        
        "send 0x${device.deviceNetworkId} 1 1", "delay 500",       
     
        //min = battery measure interval  1 = 1 hour     
        "zcl global send-me-a-report 1 0x0000 0x21 0x0C 0 {0500}",
        "send 0x${device.deviceNetworkId} 1 1", "delay 500"
	] + refresh()
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
