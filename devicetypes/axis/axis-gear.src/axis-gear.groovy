metadata {
    definition (name: "AXIS Gear", namespace: "axis", author: "AXIS Labs") {  
        capability "Actuator"
        capability "Switch"
        capability "Switch Level"
        //capability "Sensor"        
        //capability "Battery"
        //capability "Temperature Measurement"
        capability "Window Shade"
		
        //Custom Commandes to achieve 25% increment control
        command "ShadesUp"
        command "ShadesDown"
        
                
        fingerprint profileId: "0200", inClusters: "0000, 0004, 0005, 0006, 0008, 0100, 0102", manufacturer: "AXIS", model: "GR-ZB01-W", deviceJoinName: "AXIS Gear"
        //ClusterIDs: 0000 - Basic; 0004 - Groups; 0005 - Scenes; 0006 - On/Off; 0008 - Level Control; 0100 - Shade Configuration; 0102 - Window Covering;
        //Updated 2017-06-21
    }
   
	tiles(scale: 2) {
        multiAttributeTile(name:"shade", type: "lighting", width: 6, height: 6) {
            tileAttribute("device.windowShade", key: "PRIMARY_CONTROL") {
                attributeState("open", label:'Open', action:"close", icon:"http://i.imgur.com/VQrJVYH.png", backgroundColor:"#ffcc33", nextState: "closed")
                attributeState("partial", label: 'Partial', icon:"http://i.imgur.com/MkY1eRD.png", backgroundColor:"#ffcc33", nextState: "closed")
                attributeState("closed", label:'Closed', action:"open", icon:"http://i.imgur.com/WwptfpQ.png", backgroundColor:"#bbbbdd", nextState: "open")
             }
                tileAttribute ("device.level", key: "VALUE_CONTROL") {
              		attributeState("VALUE_UP", action: "ShadesUp")
        			attributeState("VALUE_DOWN", action: "ShadesDown")
             }
   		}
        //Added a "doubled" state to toggle states between positions
        standardTile("main", "device.windowShade"){
        	state("open", label:'Open', action:"close", icon:"http://i.imgur.com/VQrJVYH.png", backgroundColor:"#ffcc33", nextState: "closed")
            state("partial", label:'Partial',  icon:"http://i.imgur.com/MkY1eRD.png", backgroundColor:"#ffcc33", nextState: "closed")
            state("closed", label:'Closed', action:"open", icon:"http://i.imgur.com/WwptfpQ.png", backgroundColor:"#bbbbdd", nextState: "open")
        }
	 	controlTile("levelSliderControl", "device.level", "slider", height: 2, width: 6, inactiveLabel: true) {
            state("level", action:"setLevel")
        }
        //Version 1: Placeholder for reporting battery level
        valueTile("integerFloat", "device.integerFloat", width:6, height: 2) {
			state "val", label:'${currentValue}% Battery'
		}
        
        main(["main"])
        details(["shade", "levelSliderControl","integerFloat"])
        //details('shade', 'levelSliderControl')
        //details('levelSliderControl', 'otherTile', 'anotherTile') //adjustment and order of tiles
	}

}

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
	//sendEvent(name:"level", value: 100, displayed:true)
    setLevel(100)
    //zigbee.off()
}

def setLevel(value) {
	sendEvent(name: "integerFloat", value: 47.0)
	sendEvent(name:"level", value: value, displayed:true)
    
    if ((value>0)&&(value<100)){
    	sendEvent(name:"windowShade", value: "partial", displayed:true)
    } else if (value == 100){
    	sendEvent(name:"windowShade", value: "closed", displayed:true)
    }else{
    	sendEvent(name:"windowShade", value: "open", displayed:true)
    }
	zigbee.setLevel(value)
}

def open() {
    on()  
}

def close() {
	off()
}
/*
def refresh() {
    return zigbee.readAttribute(0x0006, 0x0000) +
        zigbee.readAttribute(0x0008, 0x0000) +
        zigbee.configureReporting(0x0006, 0x0000, 0x10, 0, 600, null) +
        zigbee.configureReporting(0x0008, 0x0000, 0x20, 1, 3600, 0x01)
}
def configure() {
    log.debug "Configuring Reporting and Bindings."
    return zigbee.configureReporting(0x0006, 0x0000, 0x10, 0, 600, null) +
        zigbee.configureReporting(0x0008, 0x0000, 0x20, 1, 3600, 0x01) +
        zigbee.readAttribute(0x0006, 0x0000) +
        zigbee.readAttribute(0x0008, 0x0000)
}
*/