metadata {
    definition (name: "AXIS Gear", namespace: "axis", author: "AXIS Labs") {  
        capability "Actuator"
        capability "Configuration"
        capability "Switch"
        capability "Switch Level"
        capability "Refresh"        
        capability "Battery"
        capability "HealthCheck"
        capability "Window Shade"
		
        //Custom Commandes to achieve 25% increment control
        command "ShadesUp"
        command "ShadesDown"
        
                
        fingerprint profileId: "0200", inClusters: "0000, 0001, 0004, 0005, 0006, 0008, 0100, 0102", manufacturer: "AXIS", model: "GR-ZB01-W", deviceJoinName: "AXIS Gear"
        //ClusterIDs: 0000 - Basic; 0004 - Groups; 0005 - Scenes; 0006 - On/Off; 0008 - Level Control; 0100 - Shade Configuration; 0102 - Window Covering;
        //Updated 2017-06-21
        //Updated 2017-08-24 - added power cluster 0001 - added battery, level, reporting, & health check 
    }
   
	tiles(scale: 2) {
        multiAttributeTile(name:"shade", type: "lighting", width: 6, height: 6) {
            tileAttribute("device.windowShade", key: "PRIMARY_CONTROL") {
                attributeState("open",  icon:"http://i.imgur.com/4TbsR54.png", backgroundColor:"#ffcc33", nextState: "closed")
                attributeState("partial",  icon:"http://i.imgur.com/vBA17WL.png", backgroundColor:"#ffcc33", nextState: "closed")
                attributeState("closed",  icon:"http://i.imgur.com/mtHdMse.png", backgroundColor:"#bbbbdd", nextState: "open")
             //label:'Open', label: 'Partial', label:'Closed'
             }
                tileAttribute ("device.level", key: "VALUE_CONTROL") {
              		attributeState("VALUE_UP", action: "ShadesUp")
        			attributeState("VALUE_DOWN", action: "ShadesDown")
             }
   		}
        //Added a "doubled" state to toggle states between positions
        standardTile("main", "device.windowShade"){
        	state("open", label:'Open', icon:"http://i.imgur.com/St7oRQl.png", backgroundColor:"#ffcc33", nextState: "closed")
            state("partial", label:'Partial',  icon:"http://i.imgur.com/y0ZpmZp.png", backgroundColor:"#ffcc33", nextState: "closed")
            state("closed", label:'Closed', icon:"http://i.imgur.com/SAiEADI.png", backgroundColor:"#bbbbdd", nextState: "open")
            //action:"close",action:"open"
        }
	 	controlTile("levelSliderControl", "device.level", "slider", height: 2, width: 6, inactiveLabel: true) {
            state("level", action:"setLevel")
        }
        
        valueTile("battery", "device.battery", inactiveLabel:false, decoration:"flat", width:4, height:2) {
			state "battery", label:'${currentValue}% battery', unit:""
		}
        
		standardTile("refresh", "device.refresh", inactiveLabel:false, decoration:"flat", width:2, height:2) {
			state "default", label:'', action:"refresh.refresh", icon:"st.secondary.refresh"
		}
        
        main(["main"])
        details(["shade", "levelSliderControl","battery", "refresh"])
        //details('shade', 'levelSliderControl')
        //details('levelSliderControl', 'otherTile', 'anotherTile') //adjustment and order of tiles
	}

}

private getCLUSTER_POWER() {0x0001}
private getCLUSTER_LEVEL() {0x0008}
private getLEVEL_ATTR_LEVEL() {0x0000}
private getPOWER_ATTR_BATTERY() {0x0021}


def ShadesUp(){
	def shadeValue = device.latestValue("level") as Integer ?: 0 
    
    if (shadeValue > 0){
      	shadeValue = Math.max(25 * (Math.round(shadeValue / 25) - 1), 0) as Integer
    }else { 
    	shadeValue = 0
	}
    //sendEvent(name:"level", value:shadeValue, displayed:true)
    setLevel(shadeValue)
    
}

def ShadesDown(){
	def shadeValue = device.latestValue("level") as Integer ?: 0 
    
    if (shadeValue < 100){
      	shadeValue = Math.min(25 * (Math.round(shadeValue / 25) + 1), 100) as Integer
    }else { 
    	shadeValue = 100
	}
    //sendEvent(name:"level", value:shadeValue, displayed:true)
    setLevel(shadeValue)
    
}

def on() {
	//sendEvent(name:"level", value:0, displayed:true)
    setLevel(0)
    
    //zigbee.on()
}

def off() {
	setLevel(100)
    //zigbee.off()
}

def setLevel(value) {
	//sendEvent(name: "integerFloat", value: 47.0)
	sendEvent(name:"level", value: value, displayed:true)
    setWindowShade(value)
	zigbee.setLevel(value)
    //refresh()
}

def open() {
    on()  
}

def close() {
	off()
}

def ping(){
	refresh
    log.debug "Ping() "
    
}

def setWindowShade(value){
 if ((value>0)&&(value<100)){
    	sendEvent(name:"windowShade", value: "partial", displayed:true)
    } else if (value == 100){
    	sendEvent(name:"windowShade", value: "closed", displayed:true)
    }else{
    	sendEvent(name:"windowShade", value: "open", displayed:true)
    }
}

def refresh() {
    def cmds = 
    	zigbee.readAttribute(CLUSTER_POWER, POWER_ATTR_BATTERY) +
    	zigbee.readAttribute(CLUSTER_LEVEL, LEVEL_ATTR_LEVEL) 	
    return cmds 
}

def configure() {
    log.debug "Configuring Reporting and Bindings."
    sendEvent(name: "checkInterval", value: 30, displayed: true, data: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID])
    def cmds = 
    	zigbee.configureReporting(CLUSTER_POWER, POWER_ATTR_BATTERY, 0x20, 0, 60, 0x01) +
        zigbee.configureReporting(CLUSTER_LEVEL, LEVEL_ATTR_LEVEL, 0x20, 0, 60, 0x01)
        log.info "configure() --- cmds: $cmds"
    return refresh + cmds
}

def parse(String description) {
    log.trace "parse() --- description: $description"

    Map map = [:]
    if (description?.startsWith('read attr -')) {
        map = parseReportAttributeMessage(description)
    }

    def result = map ? createEvent(map) : null
    log.debug "parse() --- returned: $result"
    return result
}

private Map parseReportAttributeMessage(String description) {
    Map descMap = zigbee.parseDescriptionAsMap(description)
    Map resultMap = [:]
    if (descMap.clusterInt == CLUSTER_POWER && descMap.attrInt == POWER_ATTR_BATTERY) {
        resultMap.name = "battery"
        def batteryValue = Math.round(Integer.parseInt(descMap.value, 16))
        log.debug "parseDescriptionAsMap() --- Battery: $batteryValue"
        if ((batteryValue >= 0)&&(batteryValue <= 100)){
        	resultMap.value = batteryValue
        }
        
    }
    else if (descMap.clusterInt == CLUSTER_LEVEL && descMap.attrInt == LEVEL_ATTR_LEVEL) {
        resultMap.name = "level"
        def levelValue = Math.round(Integer.parseInt(descMap.value, 16))
        //Set icon based on device feedback for the  open, closed, & partial configuration
        setWindowShade(levelValue)
        resultMap.value = levelValue 
    }
    else {
        log.debug "parseReportAttributeMessage() --- ignoring attribute"
    }
    return resultMap
}
