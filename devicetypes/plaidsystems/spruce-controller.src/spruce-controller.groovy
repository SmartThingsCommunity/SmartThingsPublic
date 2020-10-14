/**
 *  Copyright 2020 PlaidSystems
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
 Version v3.0
 * Update for new Samsung SmartThings app
 * update vid with status, message, rainsensor
 * maintain compatibility with Spruce Scheduler
 * Requires Spruce Valve as child device 
 
 Version v2.7
 * added Rain Sensor = Water Sensor Capability
 * added Pump/Master
 * add "Dimmer" to Spruce zone child for manual duration
 
**/
 
private def getVersion() { return "v3.0 10-2020" }
 
metadata {
	definition (name: "Spruce Controller", namespace: "plaidsystems", author: "plaidsystems", mnmn: "SmartThingsCommunity", vid: "412ceebe-09e5-3b06-8ed6-ab12ec248cc7"){
		capability "Actuator"
        capability "Switch"        
        capability "Water Sensor"
        capability "Sensor"
        capability "Health Check"
        capability "heartreturn55003.status"
        capability "heartreturn55003.controllerMessage"
        capability "heartreturn55003.rainSensor"
        
        capability "Configuration"
        capability "Refresh"        
        
        attribute "status", "string"
        attribute "controllerMessage", "string"
        attribute "rainsensor", "string"
        
        command "on"
        command "off"        
        command "valveOn"
        command "valveOff"
        command "zoneDuration"
        
        command "wet"
        command "dry"
        
        command "setStatus"
        command "setRainsensor"
        command "setControllerMessage"
        
        command "zon"
        command "zoff"
        command 'programOn'
        command 'programOff'
        command 'programWait'
        command 'programEnd'
        
        command "config"
        command "refresh"        
        command "rain"
        command "settingsMap"
        command "writeTime"
        command "writeType"        
        command "notify"
        command "updated"

		//new release
        fingerprint endpointId: "1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18", profileId: "0104", deviceId: "0002", deviceVersion: "00", inClusters: "0000,0003,0004,0005,0006,000F", outClusters: "0003, 0019", manufacturer: "PLAID SYSTEMS", model: "PS-SPRZ16-01", deviceJoinName: "Spruce Controller"
        fingerprint endpointId: "1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18", profileId: "0104", deviceId: "0002", deviceVersion: "00", inClusters: "0000,0003,0004,0005,0006,0009,000A,000F", outClusters: "0003, 0019", manufacturer: "PLAID SYSTEMS", model: "PS-SPRZ16-01", deviceJoinName: "Spruce Controller"
		
	}

	preferences {
        input title: "Version", description: getVersion(), displayDuringSetup: true, type: "paragraph", element: "paragraph"
        
        input title: "Rain Sensor", description: "If you have a rain sensor wired to the rain sensor input on the Spruce controller, turn it on here.", displayDuringSetup: true, type: "paragraph", element: "paragraph"
        input "RainEnable", "bool", title: "Rain Sensor Attached?", required: false, displayDuringSetup: true              
                
        input title: "Zone devices", displayDuringSetup: true, type: "paragraph", element: "paragraph",
        	description: "Enable Zones for manual control and automations. Spruce Scheduler will work without zones or pump/master enabled here."
        
        input name: "pumpMasterZone", type: "enum", title: "Pump or Master zone", description: "Optional. Spruce Scheduler will also set this up.", required: false,
        	options: ["Zone 1", "Zone 2", "Zone 3", "Zone 4", "Zone 5", "Zone 6", "Zone 7", "Zone 8", "Zone 9", "Zone 10", "Zone 11", "Zone 12", "Zone 13", "Zone 14", "Zone 15", "Zone 16"]
            
        input name: "z1", type: "bool", title: "Enable Zone 1", displayDuringSetup: true
        input name: "z2", type: "bool", title: "Enable Zone 2", displayDuringSetup: true
        input name: "z3", type: "bool", title: "Enable Zone 3", displayDuringSetup: true
        input name: "z4", type: "bool", title: "Enable Zone 4", displayDuringSetup: true
        input name: "z5", type: "bool", title: "Enable Zone 5", displayDuringSetup: true
        input name: "z6", type: "bool", title: "Enable Zone 6", displayDuringSetup: true
        input name: "z7", type: "bool", title: "Enable Zone 7", displayDuringSetup: true
        input name: "z8", type: "bool", title: "Enable Zone 8", displayDuringSetup: true
        input name: "z9", type: "bool", title: "Enable Zone 9", displayDuringSetup: true
        input name: "z10", type: "bool", title: "Enable Zone 10", displayDuringSetup: true
        input name: "z11", type: "bool", title: "Enable Zone 11", displayDuringSetup: true
        input name: "z12", type: "bool", title: "Enable Zone 12", displayDuringSetup: true
        input name: "z13", type: "bool", title: "Enable Zone 13", displayDuringSetup: true
        input name: "z14", type: "bool", title: "Enable Zone 14", displayDuringSetup: true
        input name: "z15", type: "bool", title: "Enable Zone 15", displayDuringSetup: true
        input name: "z16", type: "bool", title: "Enable Zone 16", displayDuringSetup: true
    }
	tiles(scale: 2) {        
		standardTile("switch", "device.switch", width: 2, height: 2) {		
            state "off", label: "off", action: "on"
            state "on", label: "on", action: "off"
        }
        standardTile("water", "device.water", width: 2, height: 2) {
			state "dry", icon:"st.alarm.water.dry", backgroundColor:"#ffffff", action: "wet"
			state "wet", icon:"st.alarm.water.wet", backgroundColor:"#00A0DC", action: "dry"
		}
        standardTile("wet", "device.water", inactiveLabel: false, decoration: "flat") {
			state "default", label:'Wet', action:"wet", icon: "st.alarm.water.wet"
		}         
		standardTile("dry", "device.water", inactiveLabel: false, decoration: "flat") {
			state "default", label:'Dry', action:"dry", icon: "st.alarm.water.dry"
		}        
        standardTile("refresh", "device.switch", width: 2, height: 2) {		
            state "off", label: "off", action: "on"
            state "on", label: "on", action: "off"
        }
        childDeviceTiles("outlets")
        main "switch"
        details(["switch", "water","refresh"])
	}
}

//----------------------zigbee parse-------------------------------//

// Parse incoming device messages to generate events
def parse(String description) {	
	//log.debug "Parse description ${description}"
    def result = []
    def endpoint, value, command
    def map = zigbee.parseDescriptionAsMap(description)
    log.debug "map ${map}"
    
    if (description.contains("on/off")){    	
        command = 1
        value = description[-1]
    }
    else {
    	endpoint = ( map.sourceEndpoint == null ? hextoint(map.endpoint) : hextoint(map.sourceEndpoint) )
    	value = ( map.sourceEndpoint == null ? hextoint(map.value) : null )    
    	command = (value != null ? commandType(endpoint, map.clusterInt) : null)
    }
    
    if (command != null) log.debug "command ${command} endpoint ${endpoint} value ${value} cluster ${map.clusterInt}"
    switch(command) {
      case "alarm":
        log.debug "alarm"
        result.push(createEvent(name: "status", value: "alarm"))
        result.push(createEvent(name: "controllerMessage", value: "System reboot.  Possible issue with valve or wiring."))
        break
      case "program":
      	log.debug "program"        
        if (value == 1) result.push(createEvent(name: "switch", value: "on"))
        else if (value == 0) result.push(createEvent(name: "switch", value: "off"))
        break
      case "zone":      
      	def onoff = (value == 1 ? "open" : "closed")
        def child = childDevices.find{it.deviceNetworkId == "${device.deviceNetworkId}:${endpoint}"}
        if(child) child.sendEvent(name: "valve", value: onoff)
        break
      case "rainSensor":
      	log.debug "rainSensor"
        def rainsensor = (value == 1 ? "rainsensoron" : "rainsensoroff")
        if (!RainEnable) rainsensor = "disabled"
        result.push(createEvent(name: "rainsensor", value: rainsensor))
        break
      case "refresh":
        log.debug "refresh"
        
        break
      default:
      	//log.debug "null"
        break
    }
    
	//log.debug "result: ${result}"
	return result
}

def commandType(endpoint, cluster){
	if (cluster == 9) return "alarm"
    //else if (cluster == 15 && DNI == 18) return "refresh"
	else if (endpoint == 1) return "program"
    else if (endpoint in 2..17) return "zone"
    else if (endpoint == 18) return "rainSensor"
    else if (endpoint == 19) return "refresh"
}

//--------------------end zigbee parse-------------------------------//

def installed() {	
	if (!childDevices) {
    	removeChildDevices()
		createChildDevices()
        response(refresh() + configure())
	}
    initialize()
}

def updated() {
	log.debug "updated"
    
    createChildDevices()
    initialize()
}

def initialize(){
    sendEvent(name: "switch", value: "off")
    sendEvent(name: "status", value: "Idle")
    sendEvent(name: "controllerMessage", value: "INITIAL SETUP: Enable zones in settings")
    response(pumpMaster() + rain())
}


private void createChildDevices() {	
    log.debug "create children"
    
    //create, rename, or remove child    
    for (i in 1..16){
    	def dni = i + 1
        if(settings."${"z${i}"}"){
        	def child = childDevices.find{it.deviceNetworkId == "${device.deviceNetworkId}:${dni}"}
            //create child
            if (!child){
            	def childLabel = (state.oldLabel != null ? "${state.oldLabel} Zone${i}" : "Spruce Zone${i}")
                if (settings.pumpMasterZone == i) childLabel = "Spruce PM Zone${i}"
                child = addChildDevice("Spruce Valve", "${device.deviceNetworkId}:${dni}", device.hubId,
                        [completedSetup: true, label: "${childLabel}",
                         isComponent: false])
                         log.debug "${child}"
                    child.sendEvent(name: "switch", value: "off", displayed: false)
            }
            //or rename child
            else if (device.label != state.oldLabel){
            	def childLabel = (state.oldLabel != null ? "${state.oldLabel} Zone${i}" : "Spruce Zone${i}")
                if (settings.pumpMasterZone == i) childLabel = "Spruce PM Zone${i}"
            	child.setLabel("${childLabel}")
            }
        }
        //remove child
        else if (childDevices.find{it.deviceNetworkId == "${device.deviceNetworkId}:${dni}"}){
        	deleteChildDevice("${device.deviceNetworkId}:${dni}")
        }
        
    }
    
    state.oldLabel = device.label
}

private removeChildDevices() {
	log.debug "remove all children"
	
    //get and delete children avoids duplicate children
    def children = getChildDevices()    
    if(children != null){
        children.each{
            deleteChildDevice(it.deviceNetworkId)
        }
    }       
}


//----------------------------------commands--------------------------------------//
//used for schedule
def notify(String val, String txt){	
    sendEvent(name: "status", value: "${val}", descriptionText: "${val}")
    sendEvent(name: "controllerMessage", value: "${txt}", descriptionText: "${txt}")
}

def setStatus(status){
	log.debug "status ${status}"
    sendEvent(name: "status", value: status, descriptionText: "Initialized")
}

def setRainsensor(rainsensor){
	log.debug "status ${rainsensor}"
    sendEvent(name: "rainsensor", value: "${rainsensor}", descriptionText: "Initialized")
}

def setControllerMessage(controllerMessage){
	log.debug "status ${controllerMessage}"
    sendEvent(name: "controllerMessage", value: controllerMessage, descriptionText: "Initialized")
}

def programOn(){
	log.debug "programOn"
    sendEvent(name: "switch", value: "programOn", descriptionText: "Program turned on")    
}

def programWait(){
	log.debug "programWait"
    sendEvent(name: 'switch', value: 'programWait', descriptionText: "Initializing Schedule")    
}

def programEnd(){
	log.debug "programEnd"    
    zoff()
}
    
def programOff(){
	log.debug "programEnd"    
    off()
}

//on & off redefined for Alexa to start manual schedule
def on() {
    log.debug "on"
    //Spruce Scheduler subscribes to programOn    
    //sendEvent(name: "switch", value: "programOn", descriptionText: "Schedule on")
    refresh()
}

def off() {
	log.debug "off"
    sendEvent(name: "switch", value: "off", descriptionText: "Schedule off")
    zoff()        
}

// Commands to device
//program on/off
def zon() {
	"st cmd 0x${device.deviceNetworkId} 1 6 1 {}"
}
def zoff() {
	"st cmd 0x${device.deviceNetworkId} 1 6 0 {}" 
}

// Commands to zones/valves
def valveOn(valueMap) {
	log.debug valueMap
    def endpoint = valueMap.dni.replaceFirst("${device.deviceNetworkId}:","").toInteger()
    def duration = (valueMap.duration != null ? valueMap.duration.toInteger() : 0)
    
    sendEvent(name: "status", value: "Zone ${endpoint} on for ${duration}min(s)", descriptionText: "Zone ${endpoint} on for ${duration}min(s)")
    
    zoneOn(endpoint, duration)
}

def valveOff(valueMap) {
	def endpoint = valueMap.dni.replaceFirst("${device.deviceNetworkId}:","").toInteger()    
        
    sendEvent(name: "status", value: "Zone ${endpoint} turned off", descriptionText: "Zone ${endpoint} turned off")

	zoneOff(endpoint)    
}
def zoneOn(endpoint, duration) {
	return zoneDuration(duration) + zigbee.command(6, 1, "", [destEndpoint: endpoint])
}

def zoneOff(endpoint) {    
    zigbee.command(6, 0, "", [destEndpoint: endpoint])
}

def zoneDuration(int duration){
    def hexDuration = hex(duration)  
    
    def sendCmds = []
    sendCmds.push("st wattr 0x${device.deviceNetworkId} 1 6 0x4002 0x21 {00${hexDuration}}")
    return sendCmds
}

//------------------end commands----------------------------------//

//write switch time settings map
def settingsMap(WriteTimes, attrType){
	log.debug WriteTimes    
	
    def i = 1
    def runTime
    def sendCmds = []
    while(i <= 17){
    	  
    	if (WriteTimes."${i}"){        	
        	runTime = hex(Integer.parseInt(WriteTimes."${i}"))
        	//log.debug "${i} : $runTime"
		
        	if (attrType == 4001) sendCmds.push("st wattr 0x${device.deviceNetworkId} ${i} 0x06 0x4001 0x21 {00${runTime}}")
        	else sendCmds.push("st wattr 0x${device.deviceNetworkId} ${i} 0x06 0x4002 0x21 {00${runTime}}")
            sendCmds.push("delay 600")
        }
        i++
    }    
    return sendCmds
}

//send switch time
def writeType(wEP, cycle){
	//log.debug "wt ${wEP} ${cycle}"
    "st wattr 0x${device.deviceNetworkId} ${wEP} 0x06 0x4001 0x21 {00" + hex(cycle) + "}"
}

//send switch off time
def writeTime(wEP, runTime){    
    "st wattr 0x${device.deviceNetworkId} ${wEP} 0x06 0x4002 0x21 {00" + hex(runTime) + "}"
}

//set reporting and binding
def configure() {
	// Device-Watch allows 2 check-in misses from device, checks every 2 hours
    sendEvent(name: "DeviceWatch-DeviceStatus", value: "online")
	sendEvent(name: "healthStatus", value: "online")
	sendEvent(name: "DeviceWatch-Enroll", value: 2* 60 * 60, displayed: false, data: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID])
	    
    config()    
}

def config(){
	configureHealthCheck()
    
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
	log.debug "device health ping"    
    return zigbee.onOffRefresh()
}

def rain() {
    log.debug "Rain sensor: ${RainEnable}"
        
    if (RainEnable) return "st wattr 0x${device.deviceNetworkId} 18 0x0F 0x51 0x10 {01}"
    else return "st wattr 0x${device.deviceNetworkId} 18 0x0F 0x51 0x10 {00}"  
}

def pumpMaster() {
    def pumpMasterEndpoint = (settings.pumpMasterZone != null ? settings.pumpMasterZone.replaceFirst("Zone ","").toInteger() : null)
    log.debug "Pump/Master zone: ${pumpMasterEndpoint}"
    
    def endpointMap = [:]    
    int zone = 1
    while(zone <= 17)
    {
        def zoneCycle = (zone == pumpMasterEndpoint ? 4 : 2)
        //endpoint = zone + 1
        endpointMap."${zone+1}" = "${zoneCycle}"
        zone++
    }
    
    return settingsMap(endpointMap, 4001)
}

def refresh() {
	log.debug "refresh pressed"
    
    def refreshCmds = [
        
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
    
    //will trigger off command if checked during schedule initialization
    if (device.latestValue("switch") != "programWait") refreshCmds + ["st rattr 0x${device.deviceNetworkId} 1 0x0F 0x55", "delay 500"]
    
    return refreshCmds
}

def healthPoll() {
	log.debug "healthPoll()"
	def cmds = refresh()
	cmds.each { sendHubCommand(new physicalgraph.device.HubAction(it)) }
}

def configureHealthCheck() {
	Integer hcIntervalMinutes = 12
	if (!state.hasConfiguredHealthCheck) {
		log.debug "Configuring Health Check, Reporting"
		unschedule("healthPoll")
		runEvery5Minutes("healthPoll")
		def healthEvent = [name: "checkInterval", value: hcIntervalMinutes * 60, displayed: false, data: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID]]
		// Device-Watch allows 2 check-in misses from device
		sendEvent(healthEvent)
		childDevices.each {
			it.sendEvent(healthEvent)
		}
		state.hasConfiguredHealthCheck = true
	}
}

//parse hex string and make integer
private hextoint(String hex) {
	Long.parseLong(hex, 16).toInteger()
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