/**
 *  Spruce Controller V2_4 Big Tiles *
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
	definition (name: 'Spruce Controller', namespace: 'plaidsystems', author: 'Plaid Systems') {
		capability 'Switch'
        capability 'Configuration'
        capability 'Refresh'
        capability 'Actuator'
        capability 'Valve'        
		
        attribute 'switch', 'string'
        attribute 'switch1', 'string'
		attribute 'switch2', 'string'
		attribute 'switch8', 'string'
		attribute 'switch5', 'string'
		attribute 'switch3', 'string'
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
        
        command 'on'
        command 'off'
        command 'zon'
        command 'zoff'
        command 'z1on'
		command 'z1off'
		command 'z2on'
		command 'z2off'        
		command 'z3on'
		command 'z3off'
		command 'z4on'
		command 'z4off'
		command 'z5on'
		command 'z5off'
		command 'z6on'
		command 'z6off'
		command 'z7on'
		command 'z7off'
		command 'z8on'
		command 'z8off'
        command 'z9on'
        command 'z9off'
		command 'z10on'
		command 'z10off'
		command 'z11on'
		command 'z11off'
		command 'z12on'
		command 'z12off'
		command 'z13on'
		command 'z13off'
		command 'z14on'
		command 'z14off'
		command 'z15on'
		command 'z15off'
		command 'z16on'
		command 'z16off'
        
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
        
		//ST release
		//fingerprint endpointId: '1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18', profileId: '0104', deviceId: '0002', deviceVersion: '00', inClusters: '0000,0003,0004,0005,0006,000F', outClusters: '0003, 0019', manufacturer: 'PLAID SYSTEMS', model: 'PS-SPRZ16-01'
		//new release
        fingerprint endpointId: "1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18", profileId: "0104", deviceId: "0002", deviceVersion: "00", inClusters: "0000,0003,0004,0005,0006,0009,000A,000F", outClusters: "0003, 0019", manufacturer: "PLAID SYSTEMS", model: "PS-SPRZ16-01"
		
	}

	// simulator metadata
	simulator {
		// status messages
		
		// reply messages		
	}
    
    preferences {
    input description: 'If you have a rain sensor wired to the rain sensor input on the Spruce controller, turn it on here.', displayDuringSetup: true, type: 'paragraph', element: 'paragraph', title: 'Rain Sensor'
    input description: 'The SYNC SETTINGS button must be pressed after making a change to the Rain sensor:', displayDuringSetup: false, type: 'paragraph', element: 'paragraph', title: ''     
    input 'RainEnable', 'bool', title: 'Rain Sensor Attached?', required: false, displayDuringSetup: true
    input description: 'Adjust manual water time with arrows on main tile.  The time indicated in the first small tile indicates the time the zone will water when manually switched on.', displayDuringSetup: false, type: 'paragraph', element: 'paragraph', title: ''
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
        }
		standardTile('switch', 'device.switch', width:2, height:2) {
            state 'off', label: 'Start', action: 'programOn', icon: 'st.Outdoor.outdoor12', backgroundColor: '#a9a9a9'
            state 'programOn', label: 'Wait', action: 'programOff', icon: 'st.contact.contact.open', backgroundColor: '#f6e10e'
            state 'programWait', label: 'Wait', action: 'programEnd', icon: 'st.contact.contact.open', backgroundColor: '#f6e10e'
            state 'on', label: 'Running', action: 'programEnd', icon: 'st.Outdoor.outdoor12', backgroundColor: '#3DC72E'
		}        
        standardTile("rainsensor", "device.rainsensor", decoration: 'flat') {			
			state "rainSensoroff", label: 'sensor', icon: 'http://www.plaidsystems.com/smartthings/st_drop_on.png'
            state "rainSensoron", label: 'sensor', icon: 'http://www.plaidsystems.com/smartthings/st_drop_on_blue_small.png'
            state "disable", label: 'sensor', icon: 'http://www.plaidsystems.com/smartthings/st_drop_x_small.png'
            state "enable", label: 'sensor', icon: 'http://www.plaidsystems.com/smartthings/st_drop_on.png'
		}
        standardTile('switch1', 'device.switch1', inactiveLabel: false) {			
			state 'z1off', label: '1', action: 'z1on', icon: 'st.valves.water.closed', backgroundColor: '#ffffff'
            state 'z1on', label: '1', action: 'z1off', icon: 'st.valves.water.open', backgroundColor: '#00A0DC'
		}
        standardTile('switch2', 'device.switch2', inactiveLabel: false) {            
            state 'z2off', label: '2', action: 'z2on', icon: 'st.valves.water.closed', backgroundColor: '#ffffff'
            state 'z2on', label: '2', action: 'z2off', icon: 'st.valves.water.open', backgroundColor: '#00A0DC'
		}        
        standardTile('switch3', 'device.switch3', inactiveLabel: false) {			
			state 'z3off', label: '3', action: 'z3on', icon: 'st.valves.water.closed', backgroundColor: '#ffffff'
            state 'z3on', label: '3', action: 'z3off', icon: 'st.valves.water.open', backgroundColor: '#00A0DC'
		}
        standardTile('switch4', 'device.switch4', inactiveLabel: false) {            
            state 'z4off', label: '4', action: 'z4on', icon: 'st.valves.water.closed', backgroundColor: '#ffffff'
            state 'z4on', label: '4', action: 'z4off', icon: 'st.valves.water.open', backgroundColor: '#00A0DC'
		}
        standardTile('switch5', 'device.switch5', inactiveLabel: false) {            
            state 'z5off', label: '5', action: 'z5on', icon: 'st.valves.water.closed', backgroundColor: '#ffffff'
            state 'z5on', label: '5', action: 'z5off', icon: 'st.valves.water.open', backgroundColor: '#00A0DC'
		}
		standardTile('switch6', 'device.switch6', inactiveLabel: false) {            
            state 'z6off', label: '6', action: 'z6on', icon: 'st.valves.water.closed', backgroundColor: '#ffffff'
            state 'z6on', label: '6', action: 'z6off', icon: 'st.valves.water.open', backgroundColor: '#00A0DC'
		}
        standardTile('switch7', 'device.switch7', inactiveLabel: false) {            
            state 'z7off', label: '7', action: 'z7on', icon: 'st.valves.water.closed', backgroundColor: '#ffffff'
            state 'z7on', label: '7', action: 'z7off', icon: 'st.valves.water.open', backgroundColor: '#00A0DC'
		}
		standardTile('switch8', 'device.switch8', inactiveLabel: false) {            
            state 'z8off', label: '8', action: 'z8on', icon: 'st.valves.water.closed', backgroundColor: '#ffffff'
            state 'z8on', label: '8', action: 'z8off', icon: 'st.valves.water.open', backgroundColor: '#00A0DC'
		}
        standardTile('switch9', 'device.switch9', inactiveLabel: false) {			
			state 'z9off', label: '9', action: 'z9on', icon: 'st.valves.water.closed', backgroundColor: '#ffffff'
            state 'z9on', label: '9', action: 'z9off', icon: 'st.valves.water.open', backgroundColor: '#00A0DC'
		}
        standardTile('switch10', 'device.switch10', inactiveLabel: false) {            
            state 'z10off', label: '10', action: 'z10on', icon: 'st.valves.water.closed', backgroundColor: '#ffffff'
            state 'z10on', label: '10', action: 'z10off', icon: 'st.valves.water.open', backgroundColor: '#00A0DC'
		}        
        standardTile('switch11', 'device.switch11', inactiveLabel: false) {			
			state 'z11off', label: '11', action: 'z11on', icon: 'st.valves.water.closed', backgroundColor: '#ffffff'
            state 'z11on', label: '11', action: 'z11off', icon: 'st.valves.water.open', backgroundColor: '#00A0DC'
		}
        standardTile('switch12', 'device.switch12', inactiveLabel: false) {            
            state 'z12off', label: '12', action: 'z12on', icon: 'st.valves.water.closed', backgroundColor: '#ffffff'
            state 'z12on', label: '12', action: 'z12off', icon: 'st.valves.water.open', backgroundColor: '#00A0DC'
		}
        standardTile('switch13', 'device.switch13', inactiveLabel: false) {            
            state 'z13off', label: '13', action: 'z13on', icon: 'st.valves.water.closed', backgroundColor: '#ffffff'
            state 'z13on', label: '13', action: 'z13off', icon: 'st.valves.water.open', backgroundColor: '#00A0DC'
		}
		standardTile('switch14', 'device.switch14', inactiveLabel: false) {            
            state 'z14off', label: '14', action: 'z14on', icon: 'st.valves.water.closed', backgroundColor: '#ffffff'
            state 'z14on', label: '14', action: 'z14off', icon: 'st.valves.water.open', backgroundColor: '#00A0DC'
		}
        standardTile('switch15', 'device.switch15', inactiveLabel: false) {            
            state 'z15off', label: '15', action: 'z15on', icon: 'st.valves.water.closed', backgroundColor: '#ffffff'
            state 'z15on', label: '15', action: 'z15off', icon: 'st.valves.water.open', backgroundColor: '#00A0DC'
		}		
        standardTile('switch16', 'device.switch16', inactiveLabel: false) {            
            state 'z16off',  label: '16', action: 'z16on', icon: 'st.valves.water.closed', backgroundColor: '#ffffff'
            state 'z16on',  label: '16', action: 'z16off', icon: 'st.valves.water.open', backgroundColor: '#00A0DC'
		}        
        standardTile('refresh', 'device.switch', inactiveLabel: false, decoration: 'flat') {
			state 'default', action: 'refresh', icon:'st.secondary.refresh'//-icon'            
		}
        standardTile('configure', 'device.configure', inactiveLabel: false, decoration: 'flat') {
			state 'configure', label:'', action:'configuration.configure', icon:'http://www.plaidsystems.com/smartthings/st_syncsettings.png'//sync_icon_small.png'
		}        
		
        main (['switchall'])        
        details(['switchall','minutes','rainsensor','switch1','switch2','switch3','switch4','switch','switch5','switch6','switch7','switch8','switch9','switch10','switch11','switch12','refresh','configure','switch13','switch14','switch15','switch16'])		
    }       
}

//used for schedule 
def programOn(){
    sendEvent(name: 'switch', value: 'programOn', descriptionText: 'Program turned on')    
    }

def programWait(){    
    sendEvent(name: 'switch', value: 'programWait', descriptionText: "Initializing Schedule")
    }

def programEnd(){
	//sets switch to off and tells schedule switch is off/schedule complete with manaual
    sendEvent(name: 'switch', value: 'off', descriptionText: 'Program manually turned off')
    zoff() 
    }
    
def programOff(){    
    sendEvent(name: 'switch', value: 'off', descriptionText: 'Program turned off')
    off()
    }

//set minutes
def levelUp(){
	def newvalue = 1
    if (device.latestValue('minutes') != null) newvalue = device.latestValue('minutes').toInteger()+1
    if (newvalue >= 60) newvalue = 60
    def value = newvalue.toString()    
    log.debug value
	sendEvent(name: 'minutes', value: "${value}", descriptionText: "Manual Time set to ${value}", display: false)    
}

def levelDown(){
	def newvalue = device.latestValue('minutes').toInteger()-1
    if (newvalue <= 0) newvalue = 1
    def value = newvalue.toString()    
    log.debug value
	sendEvent(name: 'minutes', value: "${value}", descriptionText: "Manual Time set to ${value}", display: false)
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
    	map.value = 'z' + EP + onoff
    	map.descriptionText = "${device.displayName} turned Zone $EP ${onoff}"
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
	sendEvent(name: 'status', value: val, descriptionText: txt, isStateChange: true, display: false)
    
    //String txtShort = txt.take(100)
    sendEvent(name: 'tileMessage', value: txt, descriptionText: "", isStateChange: true, display: false)
}

def updated(){
	log.debug "updated"
    
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
	sendEvent(name: 'minutes', value: "${value}", descriptionText: "Manual Time set to ${value}", display: false)
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
	
    sendEvent(name: 'status', value: 'schedule', descriptionText: "Syncing settings to controller")
    sendEvent(name: 'minutes', value: "10", descriptionText: "Manual Time set to 10 mins", display: false)
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

def refresh() {

	log.debug "refresh pressed"
    sendEvent(name: 'tileMessage', value: 'Refresh', descriptionText: 'Refresh')
        
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

//on & off redefined for Alexa to start manual schedule
def on() {    
    log.debug 'Alexa on'
    //schedule subscribes to programOn
    sendEvent(name: 'switch', value: 'programOn', descriptionText: 'Alexa turned program on')           
}
def off() {
	log.debug 'Alexa off'
    sendEvent(name: 'switch', value: 'off', descriptionText: 'Alexa turned program off')
    zoff()        
}

// Commands to device
//zones on - 8
def zon() {
	"st cmd 0x${device.deviceNetworkId} 1 6 1 {}"
}
def zoff() {
	"st cmd 0x${device.deviceNetworkId} 1 6 0 {}" 
}
def z1on() {	
    return manual() + "st cmd 0x${device.deviceNetworkId} 2 6 1 {}"    
}
def z1off() {    
    "st cmd 0x${device.deviceNetworkId} 2 6 0 {}"
}
def z2on() {	
    return manual() + "st cmd 0x${device.deviceNetworkId} 3 6 1 {}"    
}
def z2off() {    
    "st cmd 0x${device.deviceNetworkId} 3 6 0 {}"    
}
def z3on() {        
    return manual() + "st cmd 0x${device.deviceNetworkId} 4 6 1 {}"    
}
def z3off() {	
    "st cmd 0x${device.deviceNetworkId} 4 6 0 {}"    
}
def z4on() {	
    return manual() + "st cmd 0x${device.deviceNetworkId} 5 6 1 {}"    
}
def z4off() {    
    "st cmd 0x${device.deviceNetworkId} 5 6 0 {}"    
}
def z5on() {	
	return manual() + "st cmd 0x${device.deviceNetworkId} 6 6 1 {}"   
}
def z5off() {
	"st cmd 0x${device.deviceNetworkId} 6 6 0 {}"    
}
def z6on() {
	return manual() + "st cmd 0x${device.deviceNetworkId} 7 6 1 {}"
}
def z6off() {
    "st cmd 0x${device.deviceNetworkId} 7 6 0 {}"
}
def z7on() {
	return manual() + "st cmd 0x${device.deviceNetworkId} 8 6 1 {}"  
}
def z7off() {
	"st cmd 0x${device.deviceNetworkId} 8 6 0 {}"    
}
def z8on() {
	return manual() + "st cmd 0x${device.deviceNetworkId} 9 6 1 {}"    
}
def z8off() {
	"st cmd 0x${device.deviceNetworkId} 9 6 0 {}"    
}

//zones 9 - 16
def z9on() {	
    return manual() + "st cmd 0x${device.deviceNetworkId} 10 6 1 {}"    
}
def z9off() {	    
    "st cmd 0x${device.deviceNetworkId} 10 6 0 {}"   
}
def z10on() {	
    return manual() + "st cmd 0x${device.deviceNetworkId} 11 6 1 {}"    
}
def z10off() {    
    "st cmd 0x${device.deviceNetworkId} 11 6 0 {}"    
}
def z11on() {        
    return manual() + "st cmd 0x${device.deviceNetworkId} 12 6 1 {}"    
}
def z11off() {	
    "st cmd 0x${device.deviceNetworkId} 12 6 0 {}"    
}
def z12on() {	
    return manual() + "st cmd 0x${device.deviceNetworkId} 13 6 1 {}"    
}
def z12off() {    
    "st cmd 0x${device.deviceNetworkId} 13 6 0 {}"    
}
def z13on() {	
	return manual() + "st cmd 0x${device.deviceNetworkId} 14 6 1 {}"   
}
def z13off() {
	"st cmd 0x${device.deviceNetworkId} 14 6 0 {}"    
}
def z14on() {
	return manual() + "st cmd 0x${device.deviceNetworkId} 15 6 1 {}"
}
def z14off() {
    "st cmd 0x${device.deviceNetworkId} 15 6 0 {}"
}
def z15on() {
	return manual() + "st cmd 0x${device.deviceNetworkId} 16 6 1 {}"  
}
def z15off() {
	"st cmd 0x${device.deviceNetworkId} 16 6 0 {}"    
}
def z16on() {
	return manual() + "st cmd 0x${device.deviceNetworkId} 17 6 1 {}"    
}
def z16off() {
	"st cmd 0x${device.deviceNetworkId} 17 6 0 {}"    
}

