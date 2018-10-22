/**
 *  Spruce Controller Composite *
 *  Copyright 2017 Plaid Systems
 *
 *	Author: NC
 *	Date: 2017-6
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
 -------------6-2017 update---------------
 * change to composite device
 * add child devices
 * add device health
 * seperate switch and program
 * match Spruce Scheduler to use new commands seperate from switch
 */

metadata {
	definition (name: 'Spruce Controller', namespace: 'plaidsystems', author: 'Plaid Systems') {
		capability 'Switch'
        capability 'Configuration'
        capability 'Refresh'
        capability 'Actuator'
        capability 'Valve'
        capability 'Health Check'
		
        attribute 'checkInterval', 'string'
        attribute 'program', 'string'
        attribute 'switch', 'string'
        attribute 'switch1', 'string'
		attribute 'switch2', 'string'
        attribute 'switch3', 'string'
        attribute 'switch4', 'string'
		attribute 'switch5', 'string'
        attribute 'switch6', 'string'
		attribute 'switch8', 'string'
		attribute 'switch5', 'string'		
		attribute 'switch4', 'string'
		attribute 'switch6', 'string'
		attribute 'switch7', 'string'
        attribute 'switch9', 'string'
		attribute 'switch10', 'string'
		attribute 'switch11', 'string'
		attribute 'switch12', 'string'
		attribute 'switch13', 'string'
		attribute 'switch14', 'string'
		attribute 'switch15', 'string'
        attribute 'switch16', 'string'
        attribute 'pause', 'string'
		attribute 'rainsensor', 'string'
        attribute 'status', 'string'
        attribute 'tileMessage', 'string'
        attribute 'minutes', 'string'
        attribute 'VALUE_UP', 'string'
        attribute 'VALUE_DOWN', 'string'        
        
        command 'levelUp'
        command 'levelDown'
        command 'programOn'
        command 'programOff'
        command 'programWait'
        command 'programEnd'        
        command 'pause'
        command 'endpause'
        command 'start'
        
        command 'on'
        command 'off'        
        command 'zoneon'
        command 'zoneoff'
        
        command 'config'
        command 'refresh'        
        command 'rain'
        command 'manual'
        command 'manualTime'
        command 'settingsMap'
        command 'writeTime'
        command 'writeType'        
        command 'notify'
        command 'updated'      
        
		//new release
        fingerprint endpointId: "1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18", profileId: "0104", deviceId: "0002", deviceVersion: "00", inClusters: "0000,0003,0004,0005,0006,000F", outClusters: "0003, 0019", manufacturer: "PLAID SYSTEMS", model: "PS-SPRZ16-01", deviceJoinName: "Spruce Controller"
        fingerprint endpointId: "1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18", profileId: "0104", deviceId: "0002", deviceVersion: "00", inClusters: "0000,0003,0004,0005,0006,0009,000A,000F", outClusters: "0003, 0019", manufacturer: "PLAID SYSTEMS", model: "PS-SPRZ16-01", deviceJoinName: "Spruce Controller"
		fingerprint endpointId: "1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18", profileId: "0104", deviceId: "0002", deviceVersion: "00", inClusters: "0000,0003,0004,0005,0006,0009,000A,000F", outClusters: "0003, 0006, 0019", manufacturer: "PLAID SYSTEMS", model: "PS-SPRWIFI16-01", deviceJoinName: "Spruce Controller WiFi"																																																																		   
		
	}

	// simulator metadata
	simulator {
		// status messages
		
		// reply messages		
	}
    
    preferences {
        input description: 'If you have a rain sensor wired to the rain sensor input on the Spruce controller, turn it on here.', displayDuringSetup: true, type: 'paragraph', element: 'paragraph', title: 'Rain Sensor'
        input 'RainEnable', 'bool', title: 'Rain Sensor Attached?', required: false, displayDuringSetup: true
        input description: 'Enable your zones below, turning on Display zones will show each zone independently in the main screen.', displayDuringSetup: true, type: 'paragraph', element: 'paragraph', title: 'Enabled Zones'
        input name: 'zonedisplay', type: 'bool', title: 'Display zones independently', displayDuringSetup: true
        input name: 'z1', type: 'bool', title: 'Enable Zone 1', displayDuringSetup: true
        input name: 'z2', type: 'bool', title: 'Enable Zone 2', displayDuringSetup: true
        input name: 'z3', type: 'bool', title: 'Enable Zone 3', displayDuringSetup: true
        input name: 'z4', type: 'bool', title: 'Enable Zone 4', displayDuringSetup: true
        input name: 'z5', type: 'bool', title: 'Enable Zone 5', displayDuringSetup: true
        input name: 'z6', type: 'bool', title: 'Enable Zone 6', displayDuringSetup: true
        input name: 'z7', type: 'bool', title: 'Enable Zone 7', displayDuringSetup: true
        input name: 'z8', type: 'bool', title: 'Enable Zone 8', displayDuringSetup: true
        input name: 'z9', type: 'bool', title: 'Enable Zone 9', displayDuringSetup: true
        input name: 'z10', type: 'bool', title: 'Enable Zone 10', displayDuringSetup: true
        input name: 'z11', type: 'bool', title: 'Enable Zone 11', displayDuringSetup: true
        input name: 'z12', type: 'bool', title: 'Enable Zone 12', displayDuringSetup: true
        input name: 'z13', type: 'bool', title: 'Enable Zone 13', displayDuringSetup: true
        input name: 'z14', type: 'bool', title: 'Enable Zone 14', displayDuringSetup: true
        input name: 'z15', type: 'bool', title: 'Enable Zone 15', displayDuringSetup: true
        input name: 'z16', type: 'bool', title: 'Enable Zone 16', displayDuringSetup: true    
    }

	// UI tile definitions
	tiles {
    
    	multiAttributeTile(name:"switchall", type:"generic", width:6, height:4) {        
            tileAttribute('device.status', key: 'PRIMARY_CONTROL') {
            attributeState 'schedule', label: 'Ready', icon: 'http://www.plaidsystems.com/smartthings/st_spruce_leaf_225_top.png'
            attributeState 'finished', label: 'Finished', icon: 'st.Outdoor.outdoor5', backgroundColor: '#46c2e8'
            attributeState 'raintoday', label: 'Rain Today', icon: 'http://www.plaidsystems.com/smartthings/st_rain.png', backgroundColor: '#d65fe3'
            attributeState 'rainy', label: 'Rain', icon: 'http://www.plaidsystems.com/smartthings/st_rain.png', backgroundColor: '#d65fe3'
            attributeState 'raintom', label: 'Rain Tomorrow', icon: 'http://www.plaidsystems.com/smartthings/st_rain.png', backgroundColor: '#d65fe3'
            attributeState 'donewweek', label: 'Finished', icon: 'st.Outdoor.outdoor5', backgroundColor: '#00A0DC'
            attributeState 'skipping', label: 'Skip', icon: 'st.Outdoor.outdoor20', backgroundColor: '#46c2e8'
            attributeState 'moisture', label: 'Ready', icon: 'st.Weather.weather2', backgroundColor: '#46c2e8'
            attributeState 'pause', label: 'PAUSE', icon: 'st.contact.contact.open', backgroundColor: '#e86d13'
			attributeState 'delayed', label: 'Delayed', icon: 'st.contact.contact.open', backgroundColor: '#e86d13'
            attributeState 'active', label: 'Active', icon: 'st.Outdoor.outdoor12', backgroundColor: '#3DC72E'
            attributeState 'season', label: 'Adjust', icon: 'st.Outdoor.outdoor17', backgroundColor: '#ffb900'
            attributeState 'disable', label: 'Off', icon: 'st.secondary.off', backgroundColor: '#cccccc'
            attributeState 'warning', label: 'Warning', icon: 'http://www.plaidsystems.com/smartthings/st_spruce_leaf_225_top_yellow.png'
            attributeState 'alarm', label: 'Alarm', icon: 'http://www.plaidsystems.com/smartthings/st_spruce_leaf_225_s_red.png', backgroundColor: '#e66565'
            }
            
            tileAttribute("device.minutes", key: "VALUE_CONTROL") {
                attributeState "VALUE_UP", action: "levelUp"
                attributeState "VALUE_DOWN", action: "levelDown"
            }
            
            tileAttribute("device.tileMessage", key: "SECONDARY_CONTROL") {
                attributeState "tileMessage", label: '${currentValue}'     
            }            
            
        }
        valueTile('minutes', 'device.minutes'){
        	state 'minutes', label: '${currentValue} min'
        }
        valueTile('dummy', 'device.minutes'){
        	state 'minutes', label: ''
        }/*
		standardTile('switch', 'device.switch', width:2, height:1) {
            state 'off', label: 'Start', action: 'programOn', icon: 'st.Outdoor.outdoor12', backgroundColor: '#a9a9a9'
            state 'programOn', label: 'Wait', action: 'programOff', icon: 'st.contact.contact.open', backgroundColor: '#f6e10e'
            state 'programWait', label: 'Wait', action: 'programEnd', icon: 'st.contact.contact.open', backgroundColor: '#f6e10e'
            state 'on', label: 'Running', action: 'programEnd', icon: 'st.Outdoor.outdoor12', backgroundColor: '#3DC72E'
		}
        */
        standardTile('program', 'program', width:1, height:1) {
            state 'off', label: 'Start', action: 'programOn', icon: 'st.Outdoor.outdoor12', backgroundColor: '#a9a9a9'
            state 'programOn', label: 'Wait', action: 'programOff', icon: 'st.contact.contact.open', backgroundColor: '#f6e10e'
            state 'programWait', label: 'Wait', action: 'programOff', icon: 'st.contact.contact.open', backgroundColor: '#f6e10e'
            state 'on', label: 'Running', action: 'programOff', icon: 'st.Outdoor.outdoor12', backgroundColor: '#3DC72E'
		}
        valueTile('pause', 'pause', width:1, height:1) {            
            state 'closed', label: '', icon: 'st.contact.contact.closed', decoration: 'flat'
            state 'open', label: 'Pause', icon: 'st.contact.contact.open', decoration: 'flat'            
		}
        standardTile("rainsensor", "device.rainsensor", decoration: 'flat') {			
			state "rainSensoroff", label: 'sensor', icon: 'http://www.plaidsystems.com/smartthings/st_drop_on.png'
            state "rainSensoron", label: 'sensor', icon: 'http://www.plaidsystems.com/smartthings/st_drop_on_blue_small.png'
            state "disable", label: 'sensor', icon: 'http://www.plaidsystems.com/smartthings/st_drop_slash.png'
            state "enable", label: 'sensor', icon: 'http://www.plaidsystems.com/smartthings/st_drop_on.png'
		}
        childDeviceTile('switch1', 'switch1', childTileName: "switch")//, width:1, height:1)
        childDeviceTile('switch2', 'switch2', childTileName: "switch")//, width:1, height:1)
        childDeviceTile('switch3', 'switch3', childTileName: "switch")//, width:1, height:1)
        childDeviceTile('switch4', 'switch4', childTileName: "switch")//, width:1, height:1)
        childDeviceTile('switch5', 'switch5', childTileName: "switch")//, width:1, height:1)
        childDeviceTile('switch6', 'switch6', childTileName: "switch")//, width:1, height:1)
        childDeviceTile('switch7', 'switch7', childTileName: "switch")//, width:1, height:1)
        childDeviceTile('switch8', 'switch8', childTileName: "switch")//, width:1, height:1)
        childDeviceTile('switch9', 'switch9', childTileName: "switch")//, width:1, height:1)
        childDeviceTile('switch10', 'switch10', childTileName: "switch")//, width:1, height:1)
        childDeviceTile('switch11', 'switch11', childTileName: "switch")//, width:1, height:1)
        childDeviceTile('switch12', 'switch12', childTileName: "switch")//, width:1, height:1)
        childDeviceTile('switch13', 'switch13', childTileName: "switch")//, width:1, height:1)
        childDeviceTile('switch14', 'switch14', childTileName: "switch")//, width:1, height:1)
        childDeviceTile('switch15', 'switch15', childTileName: "switch")//, width:1, height:1)
        childDeviceTile('switch16', 'switch16', childTileName: "switch")//, width:1, height:1)
      
        standardTile('refresh', 'device.switch', inactiveLabel: false, decoration: 'flat') {
			state 'default', action: 'refresh', icon:'st.secondary.refresh'
		}
        standardTile('configure', 'device.configure', inactiveLabel: false, decoration: 'flat') {
			state 'configure', label:'', action:'configuration.configure', icon:'http://www.plaidsystems.com/smartthings/st_syncsettings.png'
		}        
		
        main (['switchall'])        
        details(['switchall','program','pause','minutes','rainsensor','refresh','configure','switch1','switch2','switch3','switch4','switch5','switch6','switch7','switch8','switch9','switch10','switch11','switch12','switch13','switch14','switch15','switch16'])		
    }       
}

def installed(){
	state.counter = state.counter ? state.counter + 1 : 1
    
    if (state.counter == 1) {
    	//check every 48 hours - moved to configure
    	//sendEvent(name: "checkInterval", value: 2 * 10 * 60 + 2 * 60, displayed: false, data: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID])
        
        removeChildDevices()
    	createChildDevices()
    	response(refresh() + configure())
	}
    
}

def updated(){
	log.debug "updated"
    //check every 48 hours
    sendEvent(name: "checkInterval", value: 2 * 10 * 60 + 2 * 60, displayed: false, data: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID])
	
    createChildDevices()
    response(rain())
    	
}

private void createChildDevices(){
	//state.oldLabel = device.label   	
    def isComp = true
    if (zonedisplay) isComp = false
    
    removeChildDevices()
    //add children
    for (i in 1..16){
        if(settings."${"z${i}"}")addChildDevice("Spruce zone", "${device.deviceNetworkId}.${i}", null, [completedSetup: true, label: "${device.label} ${i}", isComponent: "${isComp}", componentName: "switch${i}", componentLabel: "${device.label} ${i}"])        
        }    
}



private removeChildDevices() {
	log.debug "remove children"
	
    //get and delete children avoids duplicate children
    def children = getChildDevices()    
    if(children != null){
        children.each{
            deleteChildDevice(it.deviceNetworkId)
            }
        }
       
}


//set minutes
def levelUp(){
	def newvalue = 1
    if (device.latestValue('minutes') != null) newvalue = device.latestValue('minutes').toInteger()+1
    if (newvalue >= 60) newvalue = 60
    def value = newvalue.toString()    
    log.debug value
	sendEvent(name: 'minutes', value: "${value}", descriptionText: "Manual Time set to ${value}", displayed: false)    
}

def levelDown(){
	def newvalue = device.latestValue('minutes').toInteger()-1
    if (newvalue <= 0) newvalue = 1
    def value = newvalue.toString()    
    log.debug value
	sendEvent(name: 'minutes', value: "${value}", descriptionText: "Manual Time set to ${value}", displayed: false)
}

// Parse incoming device messages to generate events
def parse(String description) {	
	log.debug "Parse description ${description}"
    def result = null
    def map = [:]
    if (description?.startsWith('read attr -')) {
		def descMap = parseDescriptionAsMap(description)
		//log.debug "Desc Map: $descMap"
        //using 000F cluster instead of 0006 (switch) because ST does not differentiate between EPs and processes all as switch
		if (descMap.cluster == '000F' && descMap.attrId == '0055') {
			log.debug 'Zone'
            map = getZone(descMap)            
		}
        else if (descMap.cluster == '0009' && descMap.attrId == '0000') {
			log.debug 'Alarm'
            map = getAlarm(descMap)
            }
	}
    else if (description?.startsWith('catchall: 0104 0006 01 01 0040 00 C7BB 00 00 0000 0B 01 0100')){    	
		//log.debug 'switch on'
        map.name = 'switch'
        map.value = 'on'
        map.descriptionText = "${device.displayName} turned sprinkler program on"
    }
    else if (description?.startsWith('catchall: 0104 0006 01 01 0040 00 C7BB 00 00 0000 0B 01 0000')){    	
		//log.debug 'switch off'
        map.name = 'switch'
        map.value = 'off'
        map.descriptionText = "${device.displayName} turned sprinkler program off"
    }
    else if (description?.startsWith('catchall: 0104 0009')){
    	log.debug 'Sync settings to controller complete'
        if (device.latestValue('status') != 'alarm'){
        	def configEvt = createEvent(name: 'status', value: 'schedule', descriptionText: "Sync settings to controller complete")
            def configMsg = createEvent(name: 'tileMessage', value: 'Sync settings to controller complete', descriptionText: "Sync settings to controller complete", displayed: false)
        	result = [configEvt, configMsg]
            }
        return result
    }
  
    if (map) {
    	result = createEvent(map)
    	//configure after reboot
        if (map.value == 'warning' || map.value == 'alarm'){
            def cmds = config()       
            def alarmEvt = createEvent(name: 'tileMessage', value: map.descriptionText, descriptionText: "${map.descriptionText}", displayed: false)
            result = cmds?.collect { new physicalgraph.device.HubAction(it) } + createEvent(map) + alarmEvt           
			return result
		}
        else if (map.name == 'rainsensor'){
        	def rainEvt = createEvent(name: 'tileMessage', value: map.descriptionText, descriptionText: "${map.descriptionText}", displayed: false)
        	result = [createEvent(map), rainEvt]
            return result
        }
        else if (map.name == 'switch'){
        	def programEvt
            if (map.value == 'on') programEvt = createEvent(name: 'program', value: 'on', descriptionText: 'Program turned on')
            else if (map.value == 'off') programEvt = createEvent(name: 'program', value: 'off', descriptionText: 'Program turned off')
            result = [createEvent(map), programEvt]
            return result
        }
	}
	if (map) log.debug "Parse returned ${map} ${result}"
	return result
}

def parseDescriptionAsMap(description) {
	(description - 'read attr - ').split(',').inject([:]) { map, param ->
		def nameAndValue = param.split(':')
		map += [(nameAndValue[0].trim()):nameAndValue[1].trim()]
	}
}

def getZone(descMap){
	def map = [:]
    
    def EP = Integer.parseInt(descMap.endpoint.trim(), 16)
    
    String onoff
    if(descMap.value == '00'){
    	onoff = 'off'
    }    
    else onoff = 'on'
        
    if (EP == 1){
    	//map.name = 'program'
        map.name = 'switch'
        map.value = onoff
        map.descriptionText = "${device.displayName} turned sprinkler program ${onoff}"        
        }
        
    else if (EP == 18) {
        map.name = 'rainsensor'
    	log.debug "Rain enable: ${RainEnable}, sensor: ${onoff}"
        map.value = 'rainSensor' + onoff
        map.descriptionText = "${device.displayName} rain sensor is ${onoff}"        
        }
   	else {
        EP -= 1
       	map.name = 'switch' + EP
    	map.value = 'zone' + onoff	//'z' + EP + onoff
    	map.descriptionText = "${device.displayName} turned Zone $EP ${onoff}"
        def childDevice = childDevices.find{it.deviceNetworkId == "${device.deviceNetworkId}.${EP}"}        
        def result = createEvent(name: 'switch', value: onoff, descriptionText: "${childDevice} is ${onoff}", isStateChange: true, displayed: true)        
        if(childDevice) {
        log.debug result
        //return 
        childDevice.sendEvent(result)
        }
    	}
   	
    map.isStateChange = true 
    map.displayed = true
    return map    
}

def getAlarm(descMap){
	def map = [:]
    map.name = 'status'
    def alarmID = Integer.parseInt(descMap.value.trim(), 16)
    log.debug "${alarmID}"
    map.value = 'alarm'
    map.displayed = true
    map.isStateChange = true
    if(alarmID <= 0){
    	map.descriptionText = "${device.displayName} reboot, no other alarms"
        map.value = 'warning'
        //map.isStateChange = false
        }
    else map.descriptionText = "${device.displayName} reboot, reported zone ${alarmID - 1} error, please check zone is working correctly, press SYNC SETTINGS button to clear"
       
    return map        
}

//status notify and change status
def notify(String val, String txt){
	sendEvent(name: 'status', value: val, descriptionText: txt, isStateChange: true, displayed: true)
    
    //String txtShort = txt.take(100)
    sendEvent(name: 'tileMessage', value: txt, descriptionText: "", isStateChange: true, displayed: false)
}

//prefrences - rain sensor, manual time
def rain() {
    log.debug "Rain sensor: ${RainEnable}"
    if (RainEnable) sendEvent(name: 'rainsensor', value: 'enable', descriptionText: "${device.displayName} rain sensor is enabled", isStateChange: true)
    else sendEvent(name: 'rainsensor', value: 'disable', descriptionText: "${device.displayName} rain sensor is disabled", isStateChange: true)
    
    if (RainEnable) "st wattr 0x${device.deviceNetworkId} 18 0x0F 0x51 0x10 {01}"
    else "st wattr 0x${device.deviceNetworkId} 18 0x0F 0x51 0x10 {00}"
}

def manualTime(value){	
	sendEvent(name: 'minutes', value: "${value}", descriptionText: "Manual Time set to ${value}", displayed: false)
}

def manual(){    
    def newManaul = 10    
    if (device.latestValue('minutes')) newManaul = device.latestValue('minutes').toInteger()    
    log.debug "Manual Zone runtime ${newManaul} mins"    
    def manualTime = hex(newManaul)  
    
    def sendCmds = []
    sendCmds.push("st wattr 0x${device.deviceNetworkId} 1 6 0x4002 0x21 {00${manualTime}}")
    return sendCmds
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
	// Device-Watch allows 2 check-in misses from device (plus 2 mins lag time)
    sendEvent(name: "checkInterval", value: 2 * 10 * 60 + 2 * 60, displayed: false, data: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID])
	
    sendEvent(name: 'status', value: 'schedule', descriptionText: "Syncing settings to controller")
    sendEvent(name: 'minutes', value: "10", descriptionText: "Manual Time set to 10 mins", displayed: false)
    sendEvent(name: 'tileMessage', value: 'Syncing settings to controller', descriptionText: 'Syncing settings to controller')
    config()    
}

def config(){

	String zigbeeId = swapEndianHex(device.hub.zigbeeId)
	log.debug "Configuring Reporting and Bindings ${device.deviceNetworkId} ${device.zigbeeId}"
    
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
    return configCmds + rain()
}

//PING is used by Device-Watch in attempt to reach the Device
def ping() {
    refresh()
}

def refresh() {

	log.debug "refresh pressed"
    //sendEvent(name: 'tileMessage', value: 'Refresh', descriptionText: 'Refresh')
        
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
    
    return refreshCmds
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

//used for schedule 
def programOn(){
    //sendEvent(name: 'switch', value: 'programOn', descriptionText: 'Program turned on')
    if (device.latestValue('pause') != 'closed') endpause()
    sendEvent(name: 'program', value: 'programOn', descriptionText: 'Program turned on', displayed: false) 
}

def programWait(){    
    sendEvent(name: 'program', value: 'programWait', descriptionText: "Initializing Schedule")
}

def programEnd(){
	//sets switch to off and tells schedule switch is off/schedule complete with manaual
    sendEvent(name: 'program', value: 'off', descriptionText: 'Program manually turned off')
    //off() 
}
    
def programOff(){    
    sendEvent(name: 'program', value: 'off', descriptionText: 'Program turned off', displayed: false)
    off()
}

def start(){
	if (device.latestValue('pause') != 'closed') endpause()
    on()
}

//new pause function
def pause(){
	log.debug "pause"
    sendEvent(name: 'pause', value: 'open', descriptionText: "Paused", displayed: true)
	def pauseCmds = []
    pauseCmds.push("st wattr 0x${device.deviceNetworkId} 1 6 0x4002 0x21 {0000}")
    //send 0 time and off-> signal pause event
	return pauseCmds + "st cmd 0x${device.deviceNetworkId} 1 6 0 {}"
}

def endpause(){	
	log.debug "endpause"
    sendEvent(name: 'pause', value: 'closed', descriptionText: "Pause end", displayed: true)	
    //on()
}

//on & off redefined for Alexa to start manual schedule
def on() {
	log.debug 'on'
	"st cmd 0x${device.deviceNetworkId} 1 6 1 {}"
}

def off() {
	log.debug 'off'
	//sendEvent(name: 'program', value: 'off', descriptionText: 'Schedule off')
	"st cmd 0x${device.deviceNetworkId} 1 6 0 {}" 
}


// Commands to children
def zoneon(dni) {	
    def DNI = dni.replaceFirst("${device.deviceNetworkId}.","")
    DNI++
    log.debug DNI
    return manual() + "st cmd 0x${device.deviceNetworkId} ${DNI} 6 1 {}"    
}

def zoneoff(dni) {	
    def DNI = dni.replaceFirst("${device.deviceNetworkId}.","")
    DNI++
    log.debug DNI
    "st cmd 0x${device.deviceNetworkId} ${DNI} 6 0 {}"
}