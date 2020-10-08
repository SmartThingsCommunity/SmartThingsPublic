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
	definition (name: "SiHAS USM-300-Z", namespace: "shinasys", author: "Shina System Co., Ltd", cstHandler: true, vid: "generic-motion-6") {
		capability "Battery"
		capability "Motion Sensor"
		capability "Temperature Measurement"
		capability "Illuminance Measurement"
		capability "Relative Humidity Measurement"
        capability "Health Check"
        capability "Sensor"
        capability "Refresh"
        
        fingerprint profileId: "0104", deviceId: "0001", inClusters: "0000,0001,0003,0004,0020,0400,0402,0406,0500,0B05", outClusters: "0003,0004,0019", manufacturer: "ShinaSystem", model: "USM-300Z", deviceJoinName: "SiHAS USM-300Z"
  

	}
	

	simulator {
		// TODO: define status and reply messages here
	}

	tiles {
    
        
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
    	def catchall = zigbee.parse(description)
        if (catchall.clusterId == 0x0000 ) {
            def MsgLength = catchall.data.size()

            //usm parsing 
            if (catchall.data.get(0) == 0x84/*attribute id 0xff84*/ && catchall.data.get(1) == 0xff/*attribute id 0xff84*/ && (catchall.data.get(2) == 0x41 || catchall.data.get(2) == 0x42)/*data type*/ && catchall.data.get(3) == 0x0C/*LENGHT*/ && catchall.data.get(4) == 0x07/*USM*/) {
                def event = (catchall.data.get(4+7)>>4)&0x0f;
                event = event>2 ? event-2:event
                //log.debug "event= $event"
                def evt1 = getMotionResult(event==2?"active":"inactive")
                log.info evt1
                if(evt1) createEvent(evt1) //sendEvent(name: evt1?.name, value: evt1?.value) //createEvent(map)

                def evt2 = getBatteryResult(catchall.data.get(4+6))
                log.info evt2
                if(evt2) createEvent(evt2) //sendEvent(name: evt2?.name, value: evt2?.value) //createEvent(map)
                
                def evt3 = getTempResult((catchall.data.get(4+9) <<1) | (catchall.data.get(4+10)>>7 & 0x01) )
                log.info evt3
                if(evt3) createEvent(evt3) //sendEvent(name: evt3?.name, value: evt3?.value) //createEvent(map)
                
                def evt4 = getHumiResult((catchall.data.get(4+10) & 0x7f)  )
                log.info evt4
                if(evt4) createEvent(evt4) //sendEvent(name: evt4?.name, value: evt4?.value) //createEvent(map)
                
                def evt5 = getIllumiResult(((catchall.data.get(4+7) & 0x0f )<<8) | (catchall.data.get(4+8)) )
                log.info evt5
                if(evt5) createEvent(evt5) //sendEvent(name: evt5?.name, value: evt5?.value) //createEvent(map)
				 return [evt1, evt2, evt3, evt4, evt5]

            }
		}
	}

	if (map) {
		log.debug "${device.displayName}: Parse returned ${map}"
		return createEvent(map)
	} else
		return [:]

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
        name: 'humidity',
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