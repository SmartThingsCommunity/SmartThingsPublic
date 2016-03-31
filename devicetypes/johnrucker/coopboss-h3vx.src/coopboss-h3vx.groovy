/**
 *  CoopBoss H3Vx
 *
 *  Copyright 2015 John Rucker
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *  Icon location = http://scripts.3dgo.net/smartthings/icons/
 */
 
metadata {
	definition (name: "CoopBoss H3Vx", namespace: "JohnRucker", author: "John.Rucker@Solar-Current.com") {
        capability "Refresh"
        capability "Polling"
        capability "Sensor"
        capability "Actuator"
        capability "Configuration"
		capability "Temperature Measurement"   
        capability "Door Control"
        
        command "setOpenLightLevel"
        command "closeDoor"
        command "closeDoorHiI"
        command "openDoor"
        command "autoCloseOn"
        command "autoCloseOff"
        command "autoOpenOn"
        command "autoOpenOff"
        command "setOpenLevel"
        command "setCloseLevelTo"
        command "setOpenLevelTo" 
        command "setSensitivityLevel"

        attribute "doorState","string"
        attribute "currentLightLevel","number"
        attribute "closeLightLevel","number"
        attribute "openLightLevel","number"
        attribute "autoCloseEnable","string" 
        attribute "autoOpenEnable","string"
        attribute "TempProb1","number"
        attribute "TempProb2","number"   
        attribute "dayOrNight","string"
        attribute "doorSensitivity","number"
        attribute "doorCurrent","number"        

    	fingerprint profileId: "0104", inClusters: "0000,0101,0402"
    
	}

	// simulator metadata
	simulator {
    }
    
    
	preferences {
		input description: "This feature allows you to correct any temperature variations by selecting an offset. Ex: If your sensor consistently reports a temp that's 5 degrees too warm, you'd enter \"-5\". If 3 degrees too cold, enter \"+3\".", displayDuringSetup: false, type: "paragraph", element: "paragraph"
		input "tempOffsetCoop", "number", title: "Coop Temperature Offset", description: "Adjust temperature by this many degrees", range: "*..*", displayDuringSetup: false
		input "tempOffsetOutside", "number", title: "Outside Temperature Offset", description: "Adjust temperature by this many degrees", range: "*..*", displayDuringSetup: false        
	}    
       

	// UI tile definitions
	tiles {       
		standardTile("doorCtrl", "device.doorState", width: 1, height: 1, canChangeIcon: true){
			state "unknown", label: '${name}', action:"openDoor", icon: "st.Outdoor.outdoor20", nextState:"Sent"          
			state "open", label: '${name}', action:"closeDoor", icon: "st.Outdoor.outdoor20", backgroundColor: "#0000ff" , nextState:"Sent" 
			state "opening", label: '${name}', action:"closeDoor", icon: "st.Outdoor.outdoor20", backgroundColor: "#ffa81e"            
			state "closed", label: '${name}', action:"openDoor", icon: "st.Outdoor.outdoor20", backgroundColor: "#79b821", nextState:"Sent" 
			state "closing", label: '${name}', action:"openDoor", icon: "st.Outdoor.outdoor20", backgroundColor: "#ffa81e" 
			state "jammed", label: '${name}', action:"closeDoorHiI", icon: "st.Outdoor.outdoor20", backgroundColor: "#ff0000", nextState:"Sent" 
			state "forced close", label: 'forced\rclose', action:"openDoor", icon: "st.Outdoor.outdoor20", backgroundColor: "#ff8000", nextState:"Sent"     
			state "Sent", label: 'wait', icon: "st.motion.motion.active", backgroundColor: "#ffa81e"              
		}    
        
		standardTile("dtlsDoorCtrl", "device.doorState", width: 2, height: 2){
			state "unknown", label: '${name}', action:"openDoor", icon: "st.secondary.tools", nextState:"Sent"         
			state "open", label: '${name}', action:"closeDoor", icon: "st.doors.garage.garage-open", backgroundColor: "#0000ff", nextState:"Sent"  
			state "opening", label: '${name}', action:"closeDoor", icon: "st.doors.garage.garage-opening", backgroundColor: "#ffa81e"            
			state "closed", label: '${name}', action:"openDoor", icon: "st.doors.garage.garage-closed", backgroundColor: "#79b821", nextState:"Sent" 
			state "closing", label: '${name}', action:"openDoor", icon: "st.doors.garage.garage-closing", backgroundColor: "#ffa81e"  
			state "jammed", label: '${name}', action:"closeDoorHiI", icon: "st.doors.garage.garage-open", backgroundColor: "#ff0000", nextState:"Sent"  
			state "forced close", label: "forced", action:"openDoor", icon: "st.doors.garage.garage-closed", backgroundColor: "#ff8000", nextState:"Sent" 
			state "Sent", label: 'wait', icon: "st.motion.motion.active", backgroundColor: "#ffa81e"             
		}            
        
		standardTile("autoClose", "device.autoCloseEnable", width: 1, height: 1){
			state "on", label: 'Auto', action:"autoCloseOff", icon: "st.doors.garage.garage-closing", backgroundColor: "#79b821"
			state "off", label: 'Auto', action:"autoCloseOn", icon: "st.doors.garage.garage-closing"
		}    
        
		standardTile("autoOpen", "device.autoOpenEnable", width: 1, height: 1){
			state "on", label: 'Auto', action:"autoOpenOff", icon: "st.doors.garage.garage-opening", backgroundColor: "#79b821"
			state "off", label: 'Auto', action:"autoOpenOn", icon: "st.doors.garage.garage-opening"
		}            
        
		valueTile("TempProb1", "device.TempProb1", width: 1, height: 1, decoration: "flat"){
        state defaultState: true, label:'Outside\r${currentValue}°', unit:"F"}              
        
		valueTile("TempProb2", "device.TempProb2", width: 1, height: 1, decoration: "flat"){
        state defaultState: true, label:'Coop\r${currentValue}°', unit:"F"}        

		valueTile("currentLevel", "device.currentLightLevel", width: 1, height: 1, decoration: "flat") {
        state defaultState: true, label:'Sun\r${currentValue}'}    
                
        valueTile("dayOrNight", "device.dayOrNight", decoration: "flat",  inactiveLabel: false, width: 3, height: 1) {
        state defaultState: true, label:'${currentValue}.'
        }             
                
        controlTile("SetClSlider", "device.closeLightLevel", "slider", height: 1, width: 2, inactiveLabel: false, range:"(0..100)") {
        state "closeLightLevel", action:"setCloseLevelTo", backgroundColor:"#d04e00"
        }
        
        valueTile("SetClValue", "device.closeLightLevel", decoration: "flat",  inactiveLabel: false, width: 1, height: 1) {
        state defaultState: true, label:'Close\nSunlight\n${currentValue}'
        }        
        
        controlTile("SetOpSlider", "device.openLightLevel", "slider", height: 1, width: 2, inactiveLabel: false, range:"(0..100)") {
        state "openLightLevel", action:"setOpenLevelTo", backgroundColor:"#d04e00"
        }
        
        valueTile("SetOpValue", "device.openLightLevel", decoration: "flat",  inactiveLabel: false, width: 1, height: 1) {
        state defaultState: true, label:'Open\nSunlight\n${currentValue}'
        } 
        
        controlTile("SetSensitivitySlider", "device.doorSensitivity", "slider", height: 1, width: 2, inactiveLabel: false, range:"(0..100)") {
        state "openLightLevel", action:"setSensitivityLevel", backgroundColor:"#d04e00"
        }        
        
        valueTile("SetSensitivityValue", "device.doorSensitivity", decoration: "flat",  inactiveLabel: false, width: 1, height: 1) {
        state defaultState: true, label:'Door\nSensitivity\n${currentValue}'
        }          
        
        standardTile("refresh", "device.refresh", decoration: "flat", inactiveLabel: false) {
		state defaultState: true, label:'All', action:"refresh.refresh", icon:"st.secondary.refresh-icon"
		}         
        
		main "doorCtrl"
		details (["dtlsDoorCtrl", "autoClose", "autoOpen",  "TempProb1", "TempProb2", "currentLevel", "dayOrNight",
        "SetClSlider", "SetClValue", "SetOpSlider", "SetOpValue", "SetSensitivitySlider", "SetSensitivityValue", "refresh"])
	}
}

// Parse incoming device messages to generate events  def parse(String description) {
def parse(String description) {
	//log.debug "description: $description"
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
    log.debug map
	return map ? createEvent(map) : null
}

private Map parseCatchAllMessage(String description) {
    Map resultMap = [:]    
    def cluster = zigbee.parse(description)
    if (cluster.clusterId == 0x0402) {
        switch(cluster.sourceEndpoint) {

            case 0x39:		// Endpoint 0x39 is the temperature of probe 1
            String temp = cluster.data[-2..-1].reverse().collect { cluster.hex1(it) }.join()
            resultMap.name = "TempProb1"
            def celsius = Integer.valueOf(temp,16).shortValue()    
            if (celsius ==  -32768){									// This number is used to indicate an error in the temperature reading
                resultMap.value = "ERR"
            }else{
                celsius = celsius / 100									// Temperature value is sent X 100.
                resultMap.value = celsiusToFahrenheit(celsius)		
                if (tempOffsetOutside) {
                    def offset = tempOffsetOutside as int
					resultMap.value = resultMap.value + offset
                }                      
            }
            break

            case 0x40:													// Endpoint 0x40 is the temperature of probe 2
            String temp = cluster.data[-2..-1].reverse().collect { cluster.hex1(it) }.join()
            resultMap.name = "TempProb2"
            def celsius = Integer.valueOf(temp,16).shortValue() 									
            //resultMap.descriptionText = "Prob2 celsius value = ${celsius}"
            if (celsius ==  -32768){									// This number is used to indicate an error in the temperature reading
                resultMap.value = "ERR"
            }else{
            	celsius = celsius / 100									// Temperature value is sent X 100.
                resultMap.value = celsiusToFahrenheit(celsius)			
                if (tempOffsetCoop) {
                    def offset = tempOffsetCoop as int
					resultMap.value = resultMap.value + offset
                }                                                
            }                
            sendEvent(name: "temperature", value: resultMap.value)		// set the temperatureMeasurment capability to temperature             
            break
    	}                        
	}  
    return resultMap
}

private Map parseReportAttributeMessage(String description) {
    Map resultMap = [:]
    def descMap = parseDescriptionAsMap(description)
    //log.debug "read attr descMap --> $descMap"
    if (descMap.cluster == "0101" && descMap.attrId == "0003") {
        resultMap.name = "doorState"
        if (descMap.value == "00"){
            resultMap.value = "unknown"
        }else if(descMap.value == "01"){
            resultMap.value = "closed"
        }else if(descMap.value == "02"){
            resultMap.value = "open"                   
        }else if(descMap.value == "03"){
            resultMap.value = "jammed"     
        }else if(descMap.value == "04"){
            resultMap.value = "forced close"   
        }else if(descMap.value == "05"){
            resultMap.value = "forced close"   
        }else if(descMap.value == "06"){
            resultMap.value = "closing"   
        }else if(descMap.value == "07"){
            resultMap.value = "opening"                   
        }else {            
            resultMap.value = "unknown"
        }   
        resultMap.descriptionText = "Door State Changed to ${resultMap.value}"
        
    } else if (descMap.cluster == "0101" && descMap.attrId == "0400") { 
        resultMap.name = "currentLightLevel"
        resultMap.value = (Integer.parseInt(descMap.value, 16))      
        resultMap.displayed = false
        def cLL = device.currentState("closeLightLevel")?.value as int
        def oLL = device.currentState("openLightLevel")?.value as int 
        def nLL = resultMap.value as int
        if (nLL < cLL){  
           	sendEvent(name: "dayOrNight", value: "The current sunlight level is ${nLL}, it must be > ${oLL} for an auto open", displayed: false)
        }else { 
            sendEvent(name: "dayOrNight", value: "The current sunlight level is ${nLL}, it must be < ${cLL} for an auto close", displayed: false)
        }
        
    } else if (descMap.cluster == "0101" && descMap.attrId == "0401") { 
        resultMap.name = "closeLightLevel"
        resultMap.value = (Integer.parseInt(descMap.value, 16))

    } else if (descMap.cluster == "0101" && descMap.attrId == "0402") { 
        resultMap.name = "openLightLevel"
        resultMap.value = (Integer.parseInt(descMap.value, 16))

    } else if (descMap.cluster == "0101" && descMap.attrId == "0403") { 
        resultMap.name = "autoCloseEnable"
        if (descMap.value == "01"){resultMap.value = "on"}
        else{resultMap.value = "off"}

    } else if (descMap.cluster == "0101" && descMap.attrId == "0404") { 
        resultMap.name = "autoOpenEnable"
        if (descMap.value == "01"){resultMap.value = "on"}
        else{resultMap.value = "off"}  

    } else if (descMap.cluster == "0101" && descMap.attrId == "0405") { 
        resultMap.name = "doorCurrent"
        resultMap.value = (Integer.parseInt(descMap.value, 16)) 

     } else if (descMap.cluster == "0101" && descMap.attrId == "0408") { 
        resultMap.name = "doorSensitivity"
        resultMap.value = (100 - Integer.parseInt(descMap.value, 16))
    
    } 
    
    return resultMap
}

private Map parseCustomMessage(String description) {
	Map resultMap = [:]
    if (description?.startsWith('temperature: ')) {
        resultMap.name = "temperature"
    	def rawT = (description - "temperature: ").trim()
        resultMap.descriptionText = "Temperature celsius value = ${rawT}"
        if (rawT ==  -32768){											// This number is used to indicate an error in the temperature reading
            resultMap.value = "ERR"
        }else{
            resultMap.value = celsiusToFahrenheit(rawT.toFloat()) as Float
            sendEvent(name: "TempProb2", value: resultMap.value, displayed: false)		// Workaround for lack of access to endpoint information for Temperature report  
        }        
	}
    resultMap.displayed = false
    log.info "Temperature reported = ${resultMap.value}"
    return resultMap    
}

def parseDescriptionAsMap(description) {
    (description - "read attr - ").split(",").inject([:]) { map, param ->
        def nameAndValue = param.split(":")
        map += [(nameAndValue[0].trim()):nameAndValue[1].trim()]
    }
}

// Added for Temeperature parse
def getFahrenheit(value) {
	def celsius = Integer.parseInt(value, 16)
	return celsiusToFahrenheit(celsius) as Integer
}

// Commands to device

def close() {
	log.debug "close calling closeDoor"
	closeDoor()
}

def open() {
	log.debug "open calling openDoor"
	openDoor()
}

def openDoor() {
	sendEvent(name: "doorState", value: "opening")
	log.debug "Sending Open command"
	"st cmd 0x${device.deviceNetworkId} 0x38 0x0101 0x1 {}"
}

def closeDoor() {
	sendEvent(name: "doorState", value: "closing")
	log.debug "Sending Close command"
	"st cmd 0x${device.deviceNetworkId} 0x38 0x0101 0x0 {}"
}

def closeDoorHiI() {
	sendEvent(name: "doorState", value: "closing")
	log.debug "Sending High Current Close command"
	"st cmd 0x${device.deviceNetworkId} 0x38 0x0101 0x4 {}"
}

def autoOpenOn() {
	log.debug "Setting Auto Open On"
	sendEvent(name: "autoOpenEnable", value: "on")
	"st cmd 0x${device.deviceNetworkId} 0x38 0x0101 0x0C {}"
}

def autoOpenOff() {
	log.debug "Setting Auto Open Off"
	sendEvent(name: "autoOpenEnable", value: "off")
	"st cmd 0x${device.deviceNetworkId} 0x38 0x0101 0x0D {}"
}

def autoCloseOn() {
	log.debug "Setting Auto Close On"
	sendEvent(name: "autoCloseEnable", value: "on")
	"st cmd 0x${device.deviceNetworkId} 0x38 0x0101 0x0A {}"
}

def autoCloseOff() {
	log.debug "Setting Auto Close Off"
	sendEvent(name: "autoCloseEnable", value: "off")
	"st cmd 0x${device.deviceNetworkId} 0x38 0x0101 0x0B {}"
}

def setOpenLevelTo(cValue) {
	def cX = cValue
	log.debug "Setting Open Light Level to ${cX} Hex = 0x${Integer.toHexString(cX)}"
    
    def cmd = []
    cmd << "st wattr 0x${device.deviceNetworkId} 0x38 0x0101 0x402 0x23 {${Integer.toHexString(cX)}}"	
    cmd << "delay 150"
    cmd << "st rattr 0x${device.deviceNetworkId} 0x38 0x0101 0x402"											// Read light value   
    cmd 
    
}

def setCloseLevelTo(cValue) {
	def cX = cValue
	log.debug "Setting Close Light Level to ${cX} Hex = 0x${Integer.toHexString(cX)}"
    
    def cmd = []
    cmd << "st wattr 0x${device.deviceNetworkId} 0x38 0x0101 0x401 0x23 {${Integer.toHexString(cX)}}"	
    cmd << "delay 150"
    cmd << "st rattr 0x${device.deviceNetworkId} 0x38 0x0101 0x401"											// Read light value   
    cmd 
    
}

def setSensitivityLevel(cValue) {
	def cX = 100 - cValue
	log.debug "Setting Door sensitivity level to ${cX} Hex = 0x${Integer.toHexString(cX)}"
    
    def cmd = []
    cmd << "st wattr 0x${device.deviceNetworkId} 0x38 0x0101 0x408 0x23 {${Integer.toHexString(cX)}}"		// Write attribute.  0x23 is a 32 bit integer value. SmartThings does not send a 32 bit integer value but the receiving code was written to accept this.  They should fix this command!!
    cmd << "delay 150"
    cmd << "st rattr 0x${device.deviceNetworkId} 0x38 0x0101 0x408"											// Read attribute 
    cmd 
    
}

def setNormalCloseCurrent() {
	log.debug "Sending set normal current to last door close current"
	"st cmd 0x${device.deviceNetworkId} 0x38 0x0101 0x13 {}"
}

def poll(){
	log.debug "Polling Device"
    def cmd = []
	cmd << "st rattr 0x${device.deviceNetworkId} 0x38 0x0101 0x0003"   // Read Door State 
    cmd << "delay 150"
    
    cmd << "st rattr 0x${device.deviceNetworkId} 0x38 0x0101 0x0400"	// Read Current Light Level
    cmd << "delay 150"        
    
    cmd << "st rattr 0x${device.deviceNetworkId} 0x39 0x0402 0x0000"    // Read probe 1 Temperature 
    cmd << "delay 150"       
    
    cmd << "st rattr 0x${device.deviceNetworkId} 0x40 0x0402 0x0000"    // Read probe 2 Temperature        
    
    cmd
}

def refresh() {
	log.debug "sending refresh command"   
    def cmd = []
	cmd << "st rattr 0x${device.deviceNetworkId} 0x38 0x0101 0x0003"   // Read Door State 
    cmd << "delay 150"
    
    cmd << "st rattr 0x${device.deviceNetworkId} 0x38 0x0101 0x0400"	// Read Current Light Level
    cmd << "delay 150"        
    
    cmd << "st rattr 0x${device.deviceNetworkId} 0x38 0x0101 0x0401"	// Read Door Close Light Level
    cmd << "delay 150"    
    
    cmd << "st rattr 0x${device.deviceNetworkId} 0x38 0x0101 0x0402"	// Read Door Open Light Level
    cmd << "delay 150"
    
    cmd << "st rattr 0x${device.deviceNetworkId} 0x38 0x0101 0x0403"	// Read Auto Door Close Settings 
    cmd << "delay 150"    
    
    cmd << "st rattr 0x${device.deviceNetworkId} 0x38 0x0101 0x0404"	// Read Auto Door Open Settings
    cmd << "delay 150"  
    
    cmd << "st rattr 0x${device.deviceNetworkId} 0x39 0x0402 0x0000"    // Read Current Temperature from Coop Probe 1
    cmd << "delay 150" 
    
    cmd << "st rattr 0x${device.deviceNetworkId} 0x40 0x0402 0x0000"    // Read Current Temperature from Coop Probe 2    
    cmd << "delay 150"   
    
    cmd << "st rattr 0x${device.deviceNetworkId} 0x38 0x0101 0x0405"    // Current required to close door      
    cmd << "delay 150"     
    
    cmd << "st rattr 0x${device.deviceNetworkId} 0x38 0x0101 0x0408"    // Object detection sensitivity      
    cmd
}

def setOpenLightLevel(value){
	log.debug "Setting Open Light Level to ${value} MPH."
    float xFloat = value																					// Convert value to Single Float
    int xBits = Float.floatToIntBits(xFloat)																// Convert single to bits
      
    def cmd = []
    cmd << "st wattr 0x${device.deviceNetworkId} 0x38 0x000C 0x0401 0x39 {${Integer.toHexString(xBits)}}"	
    cmd << "delay 150"
    cmd << "st rattr 0x${device.deviceNetworkId} 0x38 0x000C 0x0401"										 
    cmd    
}


def configure() {
    log.debug "Binding SEP 0x38 DEP 0x01 Cluster 0x0101 Lock cluster to hub"  
    log.debug "Binding SEP 0x39 DEP 0x01 Cluster 0x0402 Temperature cluster to hub"      
    log.debug "Binding SEP 0x40 DEP 0x01 Cluster 0x0402 Temperature cluster to hub"      
    
    def cmd = []
    cmd << "zdo bind 0x${device.deviceNetworkId} 0x38 0x01 0x0101 {${device.zigbeeId}} {}"			// Bind to end point 0x38 and the lock cluster
    cmd << "delay 150"
    cmd << "zdo bind 0x${device.deviceNetworkId} 0x39 0x01 0x0402 {${device.zigbeeId}} {}"    		// Bind to end point 0x39 and the temperature cluster
    cmd << "delay 150"
    cmd << "zdo bind 0x${device.deviceNetworkId} 0x40 0x01 0x0402 {${device.zigbeeId}} {}"    		// Bind to end point 0x40 and the temperature cluster
    cmd << "delay 1500"    
    
    log.info "Sending ZigBee Configuration Commands to Coop Control"
    return cmd + refresh()
	}