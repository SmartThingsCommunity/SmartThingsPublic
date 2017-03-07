/**
 *  Spruce Controller - Pre Release V2 10/11/2015
 *
 *  Copyright 2015 Plaid Systems
 *
 *	Author: NC
 *	Date: 2015-11
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
 -----------V3 updates-11-2015------------
 -Start program button updated to signal schedule check in Scheduler
 11/17 alarm "0" -> 0	(ln 305)
 */

metadata {
	definition (name: "Spruce Controller", namespace: "plaidsystems", author: "NCauffman") {
		capability "Switch"
        capability "Configuration"
        capability "Refresh"
        capability "Actuator"
        capability "Valve"        
		
        attribute "switch", "string"
        attribute "switch1", "string"
		attribute "switch2", "string"
		attribute "switch8", "string"
		attribute "switch5", "string"
		attribute "switch3", "string"
		attribute "switch4", "string"
		attribute "switch6", "string"
		attribute "switch7", "string"
        attribute "switch9", "string"
		attribute "switch10", "string"
		attribute "switch11", "string"
		attribute "switch12", "string"
		attribute "switch13", "string"
		attribute "switch14", "string"
		attribute "switch15", "string"
        attribute "switch16", "string"
		attribute "status", "string"        
        
        command "programOn"
        command "programOff"
        command "on"
        command "off"
        command "z1on"
		command "z1off"
		command "z2on"
		command "z2off"        
		command "z3on"
		command "z3off"
		command "z4on"
		command "z4off"
		command "z5on"
		command "z5off"
		command "z6on"
		command "z6off"
		command "z7on"
		command "z7off"
		command "z8on"
		command "z8off"
        command "z9on"
        command "z9off"
		command "z10on"
		command "z10off"
		command "z11on"
		command "z11off"
		command "z12on"
		command "z12off"
		command "z13on"
		command "z13off"
		command "z14on"
		command "z14off"
		command "z15on"
		command "z15off"
		command "z16on"
		command "z16off"
        command "offtime"
        
        command "refresh"        
        command "rain"
        command "manual"
        command "setDisplay"
        
        command "settingsMap"
        command "writeTime"
        command "writeType"        
        command "notify"
        command "updated"

		fingerprint endpointId: "1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18", profileId: "0104", deviceId: "0002", deviceVersion: "00", inClusters: "0000,0003,0004,0005,0006,000F", outClusters: "0003, 0019", manufacturer: "PLAID SYSTEMS", model: "PS-SPRZ16-01"
		
	}

	// simulator metadata
	simulator {
		// status messages
		
		// reply messages
		
	}
    preferences {
    input description: "Press Configure button after making changes to these preferences", displayDuringSetup: true, type: "paragraph", element: "paragraph", title: ""
    input "RainEnable", "bool", title: "Rain Sensor Attached?", required: false, displayDuringSetup: true
    input "ManualTime", "number", title: "Automatic shutoff time when a zone is turned on manually?", required: false, displayDuringSetup: true
	}

	// UI tile definitions
	tiles {
    
    	standardTile("status", "device.status") {            
            state "schedule", label: 'Schedule Set', icon: "http://www.plaidsystems.com/smartthings/st_spruce_leaf_225_t.png"
            state "finished", label: 'Spruce Finished', icon: "st.Outdoor.outdoor5", backgroundColor: "#46c2e8"
            state "raintoday", label: 'Rain Today', icon: "st.custom.wuk.nt_chancerain"
            state "rainy", label: 'Previous Rain', icon: "st.custom.wuk.nt_chancerain"
            state "raintom", label: 'Rain Tomorrow', icon: "st.custom.wuk.nt_chancerain"           
            state "donewweek", label: 'Spruce Finished', icon: "st.Outdoor.outdoor5", backgroundColor: "#00A0DC"
            state "skipping", label: 'Skip Today', icon: "st.Outdoor.outdoor20", backgroundColor: "#36cfe3"
            state "moisture", label: '', icon: "st.Weather.weather2", backgroundColor: "#36cfe3"
            state "pause", label: 'PAUSE', icon: "st.contact.contact.open", backgroundColor: "#e86d13"
            state "active", label: 'Active', icon: "st.Outdoor.outdoor12", backgroundColor: "#3DC72E"
            state "season", label: 'Seasonal Adjustment', icon: "st.Outdoor.outdoor17", backgroundColor: "#ffb900"
            state "disable", label: 'Disabled', icon: "st.secondary.off", backgroundColor: "#cccccc"
            state "warning", label: '', icon: "st.categories.damageAndDanger", backgroundColor: "#ffff7f"
            state "alarm", label: 'Alarm', icon: "st.categories.damageAndDanger", backgroundColor: "#f9240c"
        }
		standardTile("switch", "device.switch") {			
			//state "programOff", label: 'Start Program', action: "programOn", icon: "st.sonos.play-icon", backgroundColor: "#a9a9a9"
            state "off", label: 'Start Program', action: "programOn", icon: "st.sonos.play-icon", backgroundColor: "#a9a9a9"
            state "programOn", label: 'Initialize Program', action: "programOff", icon: "st.contact.contact.open", backgroundColor: "#f6e10e"            
            state "on", label: 'Program Running', action: "off", icon: "st.Outdoor.outdoor12", backgroundColor: "#3DC72E"
		}
        standardTile("rainsensor", "device.rainsensor") {			
			state "rainSensrooff", label: 'Rain Sensor Clear', icon: "st.Weather.weather14", backgroundColor: "#a9a9a9"
            state "rainSensoron", label: 'Rain Detected', icon: "st.Weather.weather10", backgroundColor: "#f6e10e"
		}    	
        standardTile("switch1", "device.switch1") {			
			state "z1off", label: '1', action: "z1on", icon: "st.valves.water.closed", backgroundColor: "#ffffff"
            state "z1on", label: '1', action: "z1off", icon: "st.valves.water.open", backgroundColor: "#00A0DC"
		}
        standardTile("switch2", "device.switch2") {            
            state "z2off", label: '2', action: "z2on", icon: "st.valves.water.closed", backgroundColor: "#ffffff"
            state "z2on", label: '2', action: "z2off", icon: "st.valves.water.open", backgroundColor: "#00A0DC"
		}        
        standardTile("switch3", "device.switch3", inactiveLabel: false) {			
			state "z3off", label: '3', action: "z3on", icon: "st.valves.water.closed", backgroundColor: "#ffffff"
            state "z3on", label: '3', action: "z3off", icon: "st.valves.water.open", backgroundColor: "#00A0DC"
		}
        standardTile("switch4", "device.switch4", inactiveLabel: false) {            
            state "z4off", label: '4', action: "z4on", icon: "st.valves.water.closed", backgroundColor: "#ffffff"
            state "z4on", label: '4', action: "z4off", icon: "st.valves.water.open", backgroundColor: "#00A0DC"
		}
        standardTile("switch5", "device.switch5", inactiveLabel: false) {            
            state "z5off", label: '5', action: "z5on", icon: "st.valves.water.closed", backgroundColor: "#ffffff"
            state "z5on", label: '5', action: "z5off", icon: "st.valves.water.open", backgroundColor: "#00A0DC"
		}
		standardTile("switch6", "device.switch6", inactiveLabel: false) {            
            state "z6off", label: '6', action: "z6on", icon: "st.valves.water.closed", backgroundColor: "#ffffff"
            state "z6on", label: '6', action: "z6off", icon: "st.valves.water.open", backgroundColor: "#00A0DC"
		}
        standardTile("switch7", "device.switch7", inactiveLabel: false) {            
            state "z7off", label: '7', action: "z7on", icon: "st.valves.water.closed", backgroundColor: "#ffffff"
            state "z7on", label: '7', action: "z7off", icon: "st.valves.water.open", backgroundColor: "#00A0DC"
		}
		standardTile("switch8", "device.switch8", inactiveLabel: false) {            
            state "z8off", label: '8', action: "z8on", icon: "st.valves.water.closed", backgroundColor: "#ffffff"
            state "z8on", label: '8', action: "z8off", icon: "st.valves.water.open", backgroundColor: "#00A0DC"
		}
        standardTile("switch9", "device.switch9", inactiveLabel: false) {			
			state "z9off", label: '9', action: "z9on", icon: "st.valves.water.closed", backgroundColor: "#ffffff"
            state "z9on", label: '9', action: "z9off", icon: "st.valves.water.open", backgroundColor: "#00A0DC"
		}
        standardTile("switch10", "device.switch10", inactiveLabel: false) {            
            state "z10off", label: '10', action: "z10on", icon: "st.valves.water.closed", backgroundColor: "#ffffff"
            state "z10on", label: '10', action: "z10off", icon: "st.valves.water.open", backgroundColor: "#00A0DC"
		}        
        standardTile("switch11", "device.switch11", inactiveLabel: false) {			
			state "z11off", label: '11', action: "z11on", icon: "st.valves.water.closed", backgroundColor: "#ffffff"
            state "z11on", label: '11', action: "z11off", icon: "st.valves.water.open", backgroundColor: "#00A0DC"
		}
        standardTile("switch12", "device.switch12", inactiveLabel: false) {            
            state "z12off", label: '12', action: "z12on", icon: "st.valves.water.closed", backgroundColor: "#ffffff"
            state "z12on", label: '12', action: "z12off", icon: "st.valves.water.open", backgroundColor: "#00A0DC"
		}
        standardTile("switch13", "device.switch13", inactiveLabel: false) {            
            state "z13off", label: '13', action: "z13on", icon: "st.valves.water.closed", backgroundColor: "#ffffff"
            state "z13on", label: '13', action: "z13off", icon: "st.valves.water.open", backgroundColor: "#00A0DC"
		}
		standardTile("switch14", "device.switch14", inactiveLabel: false) {            
            state "z14off", label: '14', action: "z14on", icon: "st.valves.water.closed", backgroundColor: "#ffffff"
            state "z14on", label: '14', action: "z14off", icon: "st.valves.water.open", backgroundColor: "#00A0DC"
		}
        standardTile("switch15", "device.switch15", inactiveLabel: false) {            
            state "z15off", label: '15', action: "z15on", icon: "st.valves.water.closed", backgroundColor: "#ffffff"
            state "z15on", label: '15', action: "z15off", icon: "st.valves.water.open", backgroundColor: "#00A0DC"
		}		
        standardTile("switch16", "device.switch16", inactiveLabel: false) {            
            state "z16off", label: '16', action: "z16on", icon: "st.valves.water.closed", backgroundColor: "#ffffff"
            state "z16on", label: '16', action: "z16off", icon: "st.valves.water.open", backgroundColor: "#00A0DC"
		}        
        standardTile("refresh", "device.switch", inactiveLabel: false, decoration: "flat") {
			state "default", action: "refresh", icon:"st.secondary.refresh"            
		}
        standardTile("configure", "device.configure", inactiveLabel: false, decoration: "flat") {
			state "configure", label:'', action:"configuration.configure", icon:"st.secondary.configure"
		}		
		
        main (["status"])        
        details(["status","rainsensor","switch","switch1","switch2","switch3","switch4","switch5","switch6","switch7","switch8","switch9","switch10","switch11","switch12","switch13","switch14","switch15","switch16","refresh","configure"])		
    }    
}

def programOn(){
    sendEvent(name: "switch", value: "programOn", descriptionText: "Program turned on")    
    }
    
def programOff(){    
    sendEvent(name: "switch", value: "off", descriptionText: "Program turned off")
    off()
    }
    
def updated(){
	log.debug "updated"    
}

// Parse incoming device messages to generate events
def parse(String description) {	
	//log.debug "Parse description $description"
    def result = null
    def map = [:]
	if (description?.startsWith("read attr -")) {
		def descMap = parseDescriptionAsMap(description)
		//log.debug "Desc Map: $descMap"
        //using 000F cluster instead of 0006 (switch) because ST does not differentiate between EPs and processes all as switch
		if (descMap.cluster == "000F" && descMap.attrId == "0055") {
			log.debug "Zone"
            map = getZone(descMap)            
		}
        else if (descMap.cluster == "0009" && descMap.attrId == "0000") {
			log.debug "Alarm"            
            map = getAlarm(descMap)
            }
	}     
	
	if (map) {
		result = createEvent(map)
	}
	log.debug "Parse returned $map $result"
	return result
}

def parseDescriptionAsMap(description) {
	(description - "read attr - ").split(",").inject([:]) { map, param ->
		def nameAndValue = param.split(":")
		map += [(nameAndValue[0].trim()):nameAndValue[1].trim()]
	}
}

def getZone(descMap){
	def map = [:]
    
    def EP = Integer.parseInt(descMap.endpoint.trim(), 16)
    
    String onoff
    if(descMap.value == "00"){
    	onoff = "off"        
    }    
    else onoff = "on"
            
    if (EP == 1){
    	map.name = "switch"
        map.value = onoff
        map.descriptionText = "${device.displayName} turned sprinkler program $onoff"
        }
        
    else if (EP == 18) {
        map.name = "rainsensor"
    	map.value = "rainSensor" + onoff
        map.descriptionText = "${device.displayName} rain sensor is $onoff"
        }
   	else {
        EP -= 1
        map.name = "switch" + EP
    	map.value = "z" + EP + onoff
    	map.descriptionText = "${device.displayName} turned Zone $EP $onoff"
    	}
        
    map.isStateChange = true 
    map.displayed = true    
    return map
}

def getAlarm(descMap){
	def map = [:]
    map.name = "status"
    def alarmID = Integer.parseInt(descMap.value.trim(), 16)
    log.debug "${alarmID}"
    if(alarmID <= 0) map.descriptionText = "${device.displayName} has rebooted, no other alarms"
    else map.descriptionText = "${device.displayName} rebooted, reported error on zone ${alarmID - 1}, please check zone is working correctly"    
	map.value = "alarm"
    map.isStateChange = true
    map.displayed = true
    return map        
}

//status notify and change status
def notify(value, text){	
	sendEvent(name:"status", value:"$value", descriptionText:"$text", isStateChange: true, display: false)

}

//prefrences - rain sensor, manual time
def rain() {
    log.debug "Rain $RainEnable"
    if (RainEnable) "st wattr 0x${device.deviceNetworkId} 18 0x0F 0x51 0x10 {01}"
    else "st wattr 0x${device.deviceNetworkId} 18 0x0F 0x51 0x10 {00}"
}
def manual(){
	log.debug "Time $ManualTime"
    def mTime = 10
    if (ManualTime) mTime = ManualTime
    def manualTime = hex(mTime)
    "st wattr 0x${device.deviceNetworkId} 1 6 0x4002 0x21 {00${manualTime}}"
    
    }

//write switch time settings map
def settingsMap(WriteTimes, attrType){
	log.debug WriteTimes    
	
    def i = 1
    def runTime
    def sendCmds = []
    while(i <= 17){
    	  
    	if (WriteTimes."${i}"){        	
        	runTime = hex(Integer.parseInt(WriteTimes."${i}"))
        	log.debug "${i} : $runTime"
		
        	if (attrType == 4001) sendCmds.push("st wattr 0x${device.deviceNetworkId} ${i} 0x06 0x4001 0x21 {00${runTime}}")
        	else sendCmds.push("st wattr 0x${device.deviceNetworkId} ${i} 0x06 0x4002 0x21 {00${runTime}}")
            sendCmds.push("delay 500")
        }
        i++
    }    
    return sendCmds
}

//send switch time
def writeType(wEP, cycle){
	log.debug "wt ${wEP} ${cycle}"
    "st wattr 0x${device.deviceNetworkId} ${wEP} 0x06 0x4001 0x21 {00" + hex(cycle) + "}"
    }
//send switch off time
def writeTime(wEP, runTime){    
    "st wattr 0x${device.deviceNetworkId} ${wEP} 0x06 0x4002 0x21 {00" + hex(runTime) + "}"
    }

//set reporting and binding
def configure() {

	String zigbeeId = swapEndianHex(device.hub.zigbeeId)
	log.debug "Confuguring Reporting and Bindings ${device.deviceNetworkId} ${device.zigbeeId}"
	sendEvent(name: 'configuration',value: 100, descriptionText: "Configuration initialized")
    
    def configCmds = [	
        //program on/off
        "zdo bind 0x${device.deviceNetworkId} 1 1 6 {${device.zigbeeId}} {}", "delay 1000",
        "zdo bind 0x${device.deviceNetworkId} 1 1 0x09 {${device.zigbeeId}} {}", "delay 1000",        
        "zdo bind 0x${device.deviceNetworkId} 1 1 0x0F {${device.zigbeeId}} {}", "delay 1000",
        //zones 1-8
        "zdo bind 0x${device.deviceNetworkId} 2 1 0x0F {${device.zigbeeId}} {}", "delay 1000",
        "zdo bind 0x${device.deviceNetworkId} 3 1 0x0F {${device.zigbeeId}} {}", "delay 1000",
		"zdo bind 0x${device.deviceNetworkId} 4 1 0x0F {${device.zigbeeId}} {}", "delay 1000",
        "zdo bind 0x${device.deviceNetworkId} 5 1 0x0F {${device.zigbeeId}} {}", "delay 1000",
        "zdo bind 0x${device.deviceNetworkId} 6 1 0x0F {${device.zigbeeId}} {}", "delay 1000",
        "zdo bind 0x${device.deviceNetworkId} 7 1 0x0F {${device.zigbeeId}} {}", "delay 1000",
        "zdo bind 0x${device.deviceNetworkId} 8 1 0x0F {${device.zigbeeId}} {}", "delay 1000",        
        "zdo bind 0x${device.deviceNetworkId} 9 1 0x0F {${device.zigbeeId}} {}", "delay 1000",
        //zones 9-16
        "zdo bind 0x${device.deviceNetworkId} 10 1 0x0F {${device.zigbeeId}} {}", "delay 1000",
        "zdo bind 0x${device.deviceNetworkId} 11 1 0x0F {${device.zigbeeId}} {}", "delay 1000",
		"zdo bind 0x${device.deviceNetworkId} 12 1 0x0F {${device.zigbeeId}} {}", "delay 1000",
        "zdo bind 0x${device.deviceNetworkId} 13 1 0x0F {${device.zigbeeId}} {}", "delay 1000",
        "zdo bind 0x${device.deviceNetworkId} 14 1 0x0F {${device.zigbeeId}} {}", "delay 1000",
        "zdo bind 0x${device.deviceNetworkId} 15 1 0x0F {${device.zigbeeId}} {}", "delay 1000",
        "zdo bind 0x${device.deviceNetworkId} 16 1 0x0F {${device.zigbeeId}} {}", "delay 1000",        
        "zdo bind 0x${device.deviceNetworkId} 17 1 0x0F {${device.zigbeeId}} {}", "delay 1000",
        //rain sensor
        "zdo bind 0x${device.deviceNetworkId} 18 1 0x0F {${device.zigbeeId}} {}",
        
        "zcl global send-me-a-report 6 0 0x10 1 0 {01}", "delay 500",
        "send 0x${device.deviceNetworkId} 1 1", "delay 500",
        
        "zcl global send-me-a-report 0x0F 0x55 0x10 1 0 {01}", "delay 500",
        "send 0x${device.deviceNetworkId} 1 1", "delay 500",
       
        "zcl global send-me-a-report 0x0F 0x55 0x10 1 0 {01}", "delay 500",
        "send 0x${device.deviceNetworkId} 1 2", "delay 500",
        
        "zcl global send-me-a-report 0x0F 0x55 0x10 1 0 {01}", "delay 500",
        "send 0x${device.deviceNetworkId} 1 3", "delay 500",
        
        "zcl global send-me-a-report 0x0F 0x55 0x10 1 0 {01}", "delay 500",
        "send 0x${device.deviceNetworkId} 1 4", "delay 500",
        
        "zcl global send-me-a-report 0x0F 0x55 0x10 1 0 {01}", "delay 500",
        "send 0x${device.deviceNetworkId} 1 5", "delay 500",
        
        "zcl global send-me-a-report 0x0F 0x55 0x10 1 0 {01}", "delay 500",
        "send 0x${device.deviceNetworkId} 1 6", "delay 500",
        
        "zcl global send-me-a-report 0x0F 0x55 0x10 1 0 {01}", "delay 500",
        "send 0x${device.deviceNetworkId} 1 7", "delay 500",
        
        "zcl global send-me-a-report 0x0F 0x55 0x10 1 0 {01}", "delay 500",
        "send 0x${device.deviceNetworkId} 1 8", "delay 500",      
        
        
        "zcl global send-me-a-report 0x0F 0x55 0x10 1 0 {01}", "delay 500",
        "send 0x${device.deviceNetworkId} 1 9", "delay 500",
       
        "zcl global send-me-a-report 0x0F 0x55 0x10 1 0 {01}", "delay 500",
        "send 0x${device.deviceNetworkId} 1 10", "delay 500",
        
        "zcl global send-me-a-report 0x0F 0x55 0x10 1 0 {01}", "delay 500",
        "send 0x${device.deviceNetworkId} 1 11", "delay 500",
        
        "zcl global send-me-a-report 0x0F 0x55 0x10 1 0 {01}", "delay 500",
        "send 0x${device.deviceNetworkId} 1 12", "delay 500",
        
        "zcl global send-me-a-report 0x0F 0x55 0x10 1 0 {01}", "delay 500",
        "send 0x${device.deviceNetworkId} 1 13", "delay 500",
        
        "zcl global send-me-a-report 0x0F 0x55 0x10 1 0 {01}", "delay 500",
        "send 0x${device.deviceNetworkId} 1 14", "delay 500",
        
        "zcl global send-me-a-report 0x0F 0x55 0x10 1 0 {01}", "delay 500",
        "send 0x${device.deviceNetworkId} 1 15", "delay 500",
        
        "zcl global send-me-a-report 0x0F 0x55 0x10 1 0 {01}", "delay 500",
        "send 0x${device.deviceNetworkId} 1 16", "delay 500",
        
        "zcl global send-me-a-report 0x0F 0x55 0x10 1 0 {01}", "delay 500",
        "send 0x${device.deviceNetworkId} 1 17", "delay 500",
        
        "zcl global send-me-a-report 0x0F 0x55 0x10 1 0 {01}", "delay 500",
        "send 0x${device.deviceNetworkId} 1 18", "delay 500",
        
        "zcl global send-me-a-report 0x09 0x00 0x21 1 0 {00}", "delay 500",
        "send 0x${device.deviceNetworkId} 1 1", "delay 500"
	]
    return configCmds + rain() + manual()
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

def refresh() {

	log.debug "refresh"
    def refreshCmds = [
	        
        "st rattr 0x${device.deviceNetworkId} 1 0x0F 0x55", "delay 500",
        
        "st rattr 0x${device.deviceNetworkId} 2 0x0F 0x55", "delay 500",
        "st rattr 0x${device.deviceNetworkId} 3 0x0F 0x55", "delay 500",
        "st rattr 0x${device.deviceNetworkId} 4 0x0F 0x55", "delay 500",
        "st rattr 0x${device.deviceNetworkId} 5 0x0F 0x55", "delay 500",
        "st rattr 0x${device.deviceNetworkId} 6 0x0F 0x55", "delay 500",        
        "st rattr 0x${device.deviceNetworkId} 7 0x0F 0x55", "delay 500",
        "st rattr 0x${device.deviceNetworkId} 8 0x0F 0x55", "delay 500",        
        "st rattr 0x${device.deviceNetworkId} 9 0x0F 0x55", "delay 500",
        
        "st rattr 0x${device.deviceNetworkId} 10 0x0F 0x55", "delay 500",
        "st rattr 0x${device.deviceNetworkId} 11 0x0F 0x55", "delay 500",
        "st rattr 0x${device.deviceNetworkId} 12 0x0F 0x55", "delay 500",
        "st rattr 0x${device.deviceNetworkId} 13 0x0F 0x55", "delay 500",
        "st rattr 0x${device.deviceNetworkId} 14 0x0F 0x55", "delay 500",        
        "st rattr 0x${device.deviceNetworkId} 15 0x0F 0x55", "delay 500",
        "st rattr 0x${device.deviceNetworkId} 16 0x0F 0x55", "delay 500",
        "st rattr 0x${device.deviceNetworkId} 17 0x0F 0x55", "delay 500",
        
        "st rattr 0x${device.deviceNetworkId} 18 0x0F 0x51","delay 500",
 	
    ]
    return refreshCmds + rain() + manual()
}

// Commands to device
//zones on - 8
def on() {
    //sendEvent(name:"status", value:"active", descriptionText:"Program Running", isStateChange: true, display: false)
    log.debug "on"
    "st cmd 0x${device.deviceNetworkId} 1 6 1 {}"    
}
def off() {
    log.debug "off"
    "st cmd 0x${device.deviceNetworkId} 1 6 0 {}"    
}
def z1on() {	
    "st cmd 0x${device.deviceNetworkId} 2 6 1 {}"
}
def z1off() {    
    "st cmd 0x${device.deviceNetworkId} 2 6 0 {}"
}
def z2on() {	
    "st cmd 0x${device.deviceNetworkId} 3 6 1 {}"    
}
def z2off() {    
    "st cmd 0x${device.deviceNetworkId} 3 6 0 {}"    
}
def z3on() {        
    "st cmd 0x${device.deviceNetworkId} 4 6 1 {}"    
}
def z3off() {	
    "st cmd 0x${device.deviceNetworkId} 4 6 0 {}"    
}
def z4on() {	
    "st cmd 0x${device.deviceNetworkId} 5 6 1 {}"    
}
def z4off() {    
    "st cmd 0x${device.deviceNetworkId} 5 6 0 {}"    
}
def z5on() {	
	"st cmd 0x${device.deviceNetworkId} 6 6 1 {}"   
}
def z5off() {
	"st cmd 0x${device.deviceNetworkId} 6 6 0 {}"    
}
def z6on() {
	"st cmd 0x${device.deviceNetworkId} 7 6 1 {}"
}
def z6off() {
    "st cmd 0x${device.deviceNetworkId} 7 6 0 {}"
}
def z7on() {
	"st cmd 0x${device.deviceNetworkId} 8 6 1 {}"  
}
def z7off() {
	"st cmd 0x${device.deviceNetworkId} 8 6 0 {}"    
}
def z8on() {
	"st cmd 0x${device.deviceNetworkId} 9 6 1 {}"    
}
def z8off() {
	"st cmd 0x${device.deviceNetworkId} 9 6 0 {}"    
}

//zones 9 - 16
def z9on() {	
    "st cmd 0x${device.deviceNetworkId} 10 6 1 {}"    
}
def z9off() {	    
    "st cmd 0x${device.deviceNetworkId} 10 6 0 {}"   
}
def z10on() {	
    "st cmd 0x${device.deviceNetworkId} 11 6 1 {}"    
}
def z10off() {    
    "st cmd 0x${device.deviceNetworkId} 11 6 0 {}"    
}
def z11on() {        
    "st cmd 0x${device.deviceNetworkId} 12 6 1 {}"    
}
def z11off() {	
    "st cmd 0x${device.deviceNetworkId} 12 6 0 {}"    
}
def z12on() {	
    "st cmd 0x${device.deviceNetworkId} 13 6 1 {}"    
}
def z12off() {    
    "st cmd 0x${device.deviceNetworkId} 13 6 0 {}"    
}
def z13on() {	
	"st cmd 0x${device.deviceNetworkId} 14 6 1 {}"   
}
def z13off() {
	"st cmd 0x${device.deviceNetworkId} 14 6 0 {}"    
}
def z14on() {
	"st cmd 0x${device.deviceNetworkId} 15 6 1 {}"
}
def z14off() {
    "st cmd 0x${device.deviceNetworkId} 15 6 0 {}"
}
def z15on() {
	"st cmd 0x${device.deviceNetworkId} 16 6 1 {}"  
}
def z15off() {
	"st cmd 0x${device.deviceNetworkId} 16 6 0 {}"    
}
def z16on() {
	"st cmd 0x${device.deviceNetworkId} 17 6 1 {}"    
}
def z16off() {
	"st cmd 0x${device.deviceNetworkId} 17 6 0 {}"    
}