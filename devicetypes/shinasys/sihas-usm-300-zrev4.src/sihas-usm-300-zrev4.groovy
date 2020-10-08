/**
 *  SiHAS USM-300-Z
 *
 *  Copyright 2020 Shina System Co., Ltd
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 */


metadata {
	definition (name: "SiHAS USM-300-Zrev4", namespace: "shinasys", author: "Shina System Co., Ltd", cstHandler: true, vid: "generic-motion-6") {
		capability "Battery"
		capability "Motion Sensor"
		capability "Temperature Measurement"
		capability "Illuminance Measurement"
		capability "Relative Humidity Measurement"
        capability "Health Check"
        capability "Sensor"
        capability "Refresh"
        
        fingerprint profileId: "0104", deviceId: "0001", inClusters: "0000,0001,0003,0004,0020,0400,0402,0406,0500,0B05", outClusters: "0003,0004,0019", manufacturer: "ShinaSystem", model: "USM-300Z", deviceJoinName: "SiHAS USM-300Zrev4"
  
  		//command "up"
        //command "down"
        //command "setTemperature", ["number"]
	}
	

	simulator {
		// TODO: define status and reply messages here
	}

	tiles {
     /*valueTile("temperature", "device.temperature", width: 2, height: 2) {
            state("temperature", label:'${currentValue}', unit:"F",
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
        
    	 standardTile("up", "device.temperature", inactiveLabel: false, decoration: "flat") {
            state "default", label:'up', action:"up"
        }
        standardTile("down", "device.temperature", inactiveLabel: false, decoration: "flat") {
            state "default", label:'down', action:"down"
        }
        */
		// TODO: define your main and details tiles here
      // main "temperature"
     //   details("temperature","up","down")
        
        
	}
}

// parse events into attributes
def parse(String description) {
	
	// TODO: handle 'battery' attribute
	// TODO: handle 'motion' attribute
	// TODO: handle 'temperature' attribute
	// TODO: handle 'illuminance' attribute
	// TODO: handle 'humidity' attribute
        log.debug "${device.displayName}: Parsing description: ${description}"

	// Determine current time and date in the user-selected date format and clock style
    

	// getEvent automatically retrieves temp and humidity in correct unit as integer
	Map map = zigbee.getEvent(description)

	
	if (description?.startsWith('catchall:')) {
		//map = parseCatchAllMessage(description)
        parseCatchAllMessage(description)
        map = null	
	}

	if (map) {
		log.debug "${device.displayName}: Parse returned ${map}"
		return createEvent(map)
	} else
		return [:]

}

// Check catchall for battery voltage data to pass to getBatteryResult for conversion to percentage report
private Map parseCatchAllMessage(String description) {
	Map resultMap = [:]
	def catchall = zigbee.parse(description)
	log.debug catchall

	if (catchall.clusterId == 0x0000 ) {
		def MsgLength = catchall.data.size()
		
        //usm parsing 
        if (catchall.data.get(0) == 0x84 && catchall.data.get(1) == 0xff && (catchall.data.get(2) == 0x41 || catchall.data.get(2) == 0x42) && catchall.data.get(3) == 0x0C/*LENGHT*/ && catchall.data.get(4) == 0x07/*USM*/) {
        	def event = (catchall.data.get(4+7)>>4)&0x0f;
            event = event>2 ? event-2:event
            //log.debug "event= $event"
            resultMap = getMotionResult(event==2?"active":"inactive")
            log.info resultMap
            if(resultMap) sendEvent(name: resultMap?.name, value: resultMap?.value) //createEvent(map)
			
            resultMap = getBatteryResult(catchall.data.get(4+6))
			log.info resultMap
            if(resultMap) sendEvent(name: resultMap?.name, value: resultMap?.value) //createEvent(map)
            resultMap = getTempResult((catchall.data.get(4+9) <<1) | (catchall.data.get(4+10)>>7 & 0x01) )
			log.info resultMap
            if(resultMap) sendEvent(name: resultMap?.name, value: resultMap?.value) //createEvent(map)
            resultMap = getHumiResult((catchall.data.get(4+10) & 0x7f)  )
			log.info resultMap
            if(resultMap) sendEvent(name: resultMap?.name, value: resultMap?.value) //createEvent(map)
            resultMap = getIllumiResult(((catchall.data.get(4+7) & 0x0f )<<8) | (catchall.data.get(4+8)) )
			log.info resultMap
            if(resultMap) sendEvent(name: resultMap?.name, value: resultMap?.value) //createEvent(map)
            
            
        }
	}
	return resultMap
}

private Map getMotionResult(mValue) {
	
	String descriptionTextx = (mValue == 'active') ? "${device.displayName} detected motion" : "${device.displayName} motion has stopped"
    //log.debug "motionx= ${mValue}"
	//log.debug descriptionTextx
    def result = [
        name: 'motion',
        value: mValue,
        translatable   : true,
        descriptionText : "${device.displayName} ${mValue}"
    ]

    return result
    
}

private Map getBatteryResult(rawValue) {
    // raw voltage is normally supplied as a 4 digit integer that needs to be divided by 1000
    // but in the case the final zero is dropped then divide by 100 to get actual voltage value
    //def rawVolts = rawValue / 1000
    def rawVolts = (rawValue + 100)/ 100
    def minVolts
    def maxVolts
	//log.debug "batt = $rawValue"
    if(voltsmin == null || voltsmin == "")
    	minVolts = 2.5
    else
   		minVolts = voltsmin

    if(voltsmax == null || voltsmax == "")
    	maxVolts = 3.1
    else
		maxVolts = voltsmax

    def pct = (rawVolts - minVolts) / (maxVolts - minVolts)
    def roundedPct = Math.min(100, Math.round(pct * 100))

    def result = [
        name: 'battery',
        value: roundedPct,
        unit: "%",
        isStateChange:true,
        descriptionText : "${device.displayName} Battery at ${roundedPct}% (${rawVolts} Volts)"
    ]

    return result
}


private Map getTempResult(rawValue) {
   
    def tempval = rawValue/10;
    def offset = tempOffset ? tempOffset : 0
    //tempval = (temperatureScale == "F") ? ((tempval * 1.8) + 32) + offset : tempval + offset
    tempval = tempval + offset
    //tempval = tempval.round(1)
    //log.debug "temp = $rawValue"
    
    def result = [
        name: 'temperature',
        value: displayTempInteger ? (int) tempval : tempval,
        isStateChange:true,
        descriptionText : "${device.displayName} temperature is ${tempval}°C"
    ]

	
  
    return result
}

private Map getHumiResult(rawValue) {
   
    def humival = rawValue;
    //log.debug "humival = $rawValue"
    def result = [
        name: 'relativeHumidityMeasurement.humidity',
        value: humival,
        isStateChange:true,
        descriptionText : "${device.displayName} humidity is ${humival}%"
    ]

		       
        
    return result
}

private Map getIllumiResult(rawValue) {
   
    def illumival = rawValue;
    //log.debug "illumival = $rawValue"
    def result = [
        name: 'illuminance',
        value: illumival,
        isStateChange:true,
        descriptionText : "${device.displayName} illuminance is ${illumival}lux"
    ]

		
        
        
    return result
}

def installed() {
    initialize()
}

def updated() {
    initialize()
}

def initialize() {
    sendEvent(name: "DeviceWatch-DeviceStatus", value: "online")
    sendEvent(name: "healthStatus", value: "online")
    sendEvent(name: "DeviceWatch-Enroll", value: [protocol: "cloud", scheme:"untracked"].encodeAsJson(), displayed: false)
    zigbee.readAttribute(0x0000, 0Xff84) 
    
}
/**
 * PING is used by Device-Watch in attempt to reach the Device
 * */
def ping() {
	//zigbee.readAttribute(0x0000, 0X0001)  //Application Version 
    zigbee.readAttribute(0x0000, 0Xff84) 
    log.debug "pings"
}

def refresh() {
//	log.debug "Refreshing Values"
	def refreshCmds = []

	//refreshCmds += zigbee.readAttribute(0x0000, 0X0001)  //Application Version 
	refreshCmds += zigbee.readAttribute(0x0000, 0Xff84)  //Application Version 		
	log.debug "refrsh cmd = $refreshCmds "
	return refreshCmds
}
def setLevel(value, rate = null) {
    setTemperature(value)
}

def up() {
    setTemperature(getTemperature() + 1)
}

def down() {
    setTemperature(getTemperature() - 1)
}

def setTemperature(value) {
    sendEvent(name:"temperature", value: value)
}