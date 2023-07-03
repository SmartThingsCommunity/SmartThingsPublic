metadata {
	definition (name: "Television Remote", namespace: "smartthings", author: "SmartThings") {
    		capability "switch" 
            
			command "mute" 
			command "menu"    
            command "Up"
            command "Down"
            command "Left"
            command "Right" 
			command "chup" 
 			command "chdown"               
			command "volup"    
            command "voldown"           
            command "Enter"
            command "Return"
            command "Exit"
            command "Info"            
            command "Input"
            command "Pip"
            command "lastch"
            command "one"
            command "two"
            command "three"
            command "four"
            command "five"
            command "six"
            command "seven"
            command "eight"
            command "nine"
            command "minues"
            command "zero"
	}

    standardTile("switch", "device.switch", width: 1, height: 1, canChangeIcon: true) {
        state "default", label:'Power', action:"switch.on", icon:"st.Electronics.electronics15", backgroundColor:"#ffffff"
    }
    standardTile("mute", "device.switch", decoration: "flat", canChangeIcon: false) {
        state "default", label:'Mute', action:"mute", decoration: "flat", icon:"st.custom.sonos.muted", backgroundColor:"#ffffff"
    }  
    standardTile("power", "device.switch",decoration: "flat", canChangeIcon: false) {
        state "default", label:'Power', action:"switch.off", decoration: "flat", icon:"st.samsung.da.RC_ic_power", backgroundColor:"#ffffff"
    }  
    standardTile("menu", "device.switch", decoration: "flat", canChangeIcon: false) {
        state "default", label:'Menu', action:"menu", decoration: "flat", icon:"st.vents.vent", backgroundColor:"#ffffff"
    }  
   standardTile("chup", "device.switch", decoration: "flat", canChangeIcon: false) {
        state "default", label:'CH Up', action:"chup", icon:"st.thermostat.thermostat-up"
    }
	standardTile("chdown", "device.switch", decoration: "flat", canChangeIcon: false) {
        state "default", label:'CH Down', action:"chdown", icon:"st.thermostat.thermostat-down"
    }
        standardTile("volup", "device.switch", decoration: "flat", canChangeIcon: false) {
        state "default", label:'Vol Up', action:"volup", icon:"st.thermostat.thermostat-up"
    }
    standardTile("voldown", "device.switch", decoration: "flat", canChangeIcon: false) {
        state "default", label:'Vol Down', action:"voldown", icon:"st.thermostat.thermostat-down"
    }
    standardTile("info", "device.switch", decoration: "flat", canChangeIcon: false) {
        state "default", label:'Info', action:"Info", decoration: "flat"
    } 
     standardTile("inputc", "device.switch", decoration: "flat", canChangeIcon: false) {
        state "default", label:'input', action:"Input", decoration: "flat"
    } 
    standardTile("Up", "device.switch", decoration: "flat", canChangeIcon: false) {
        state "default", label:'Up', action:"Up", icon:"st.thermostat.thermostat-up"
    }
    standardTile("Down", "device.switch", decoration: "flat", canChangeIcon: false) {
        state "default", label:'Down', action:"Down", icon:"st.thermostat.thermostat-down"
    }
    standardTile("Left", "device.switch", decoration: "flat", canChangeIcon: false) {
        state "default", label:'Left', action:"Left", icon:"st.thermostat.thermostat-left"
    }
    standardTile("Right", "device.switch", decoration: "flat", canChangeIcon: false) {
        state "default", label:'Right', action:"Right", icon:"st.thermostat.thermostat-right"
    }
    standardTile("Enter", "device.switch", decoration: "flat", canChangeIcon: false) {
        state "default", label:'Enter', action:"Enter", icon:"st.illuminance.illuminance.dark"
    }
    standardTile("Return", "device.switch", decoration: "flat", canChangeIcon: false) {
        state "default", label:'Return', action:"Return", icon:"st.secondary.refresh-icon"
    }
    standardTile("Exit", "device.switch", decoration: "flat", canChangeIcon: false) {
        state "default", label:'Exit', action:"Exit", icon:"st.locks.lock.unlocked"
    } 
     standardTile("pip", "device.switch", decoration: "flat", canChangeIcon: false) {
        state "default", label:'PIP', action:"Pip", decoration: "flat"
    } 
     standardTile("lastch", "device.switch", decoration: "flat", canChangeIcon: false) {
        state "default", label:'LAST CH', action:"lastch", decoration: "flat"
    } 
    standardTile("one", "device.switch", decoration: "flat", canChangeIcon: false) {
        state "default", label:'1', action:"one", decoration: "flat"
    } 
    standardTile("two", "device.switch", decoration: "flat", canChangeIcon: false) {
        state "default", label:'2', action:"two", decoration: "flat"
    } 
    standardTile("three", "device.switch", decoration: "flat", canChangeIcon: false) {
        state "default", label:'3', action:"three", decoration: "flat"
    } 
    standardTile("four", "device.switch", decoration: "flat", canChangeIcon: false) {
        state "default", label:'4', action:"four", decoration: "flat"
    } 
    standardTile("five", "device.switch", decoration: "flat", canChangeIcon: false) {
        state "default", label:'5', action:"five", decoration: "flat"
    } 
    standardTile("six", "device.switch", decoration: "flat", canChangeIcon: false) {
        state "default", label:'6', action:"six", decoration: "flat"
    } 
    standardTile("seven", "device.switch", decoration: "flat", canChangeIcon: false) {
        state "default", label:'7', action:"seven", decoration: "flat"
    } 
    standardTile("eight", "device.switch", decoration: "flat", canChangeIcon: false) {
        state "default", label:'8', action:"eight", decoration: "flat"
    } 
    standardTile("nine", "device.switch",width: 1, height: 1, decoration: "flat", canChangeIcon: false) {
        state "default", label:'9', action:"nine", decoration: "flat"
    } 
    standardTile("minues", "device.switch",width: 1, height: 1, decoration: "flat", canChangeIcon: false) {
        state "default", label:'-', action:"minues", decoration: "flat"
    } 
    standardTile("zero", "device.switch", width: 1, height: 1 ,decoration: "flat", canChangeIcon: false) {
        state "default", label:'0', action:"zero", decoration: "flat"
    } 
    standardTile("space", "device.switch",width: 1, height: 1, decoration: "flat", canChangeIcon: false) {
        state "default", label:'', action:"space", decoration: "flat"
    } 
    main "switch"
    details (["mute","power","menu","chup","info","volup","chdown","inputc","voldown","Return","Up","Exit","Left","Enter","Right","pip","Down","lastch","one","two","three","four","five","six","seven","eight","nine","minues","zero","space"])	
}

def parse(String description) {
	return null
}

void installed() {
	initialize()
}
def initialize() {
	updateDataValue("EnrolledUTDH", "true")
}

def updated() {
	log.debug "updated()"
	//parent.setName(device.label, device.deviceNetworkId)
	initialize()
}

// Called when the DTH is uninstalled, is this true for cirrus/gadfly integrations?
// Informs parent to purge its associated data
def uninstalled() {
    log.debug "uninstalled() parent.purgeChildDevice($device.deviceNetworkId)"
    // purge DTH from parent
    //parent?.purgeChildDevice(this)
}


def on() {
def key="1"
	log.debug "Turning TV OFF"
    def deviceId =  device.currentValue("device_b_one_id")
def keyCheck= device.currentValue("R1")
log.debug "device id:${deviceId} keyvalue ${keyCheck}"
if(keyCheck=="1")
{
parent.sendIrData(key,deviceId)
 sendEvent(name:"Power", value: "Power", descriptionText: "$device.displayName is set to Power",displayed: true) 
}
  
}
def off() {
def key="1"
	log.debug "Turning TV OFF"
    def deviceId =  device.currentValue("device_b_one_id")
def keyCheck= device.currentValue("R1")
log.debug "device id:${deviceId} keyvalue ${keyCheck}"
if(keyCheck=="1")
{
parent.sendIrData(key,deviceId)
 sendEvent(name:"Power", value: "Power", descriptionText: "$device.displayName is set to Power",displayed: true) 
}
  
}

def mute(){
def key="6"
def deviceId =  device.currentValue("device_b_one_id")
def keyCheck= device.currentValue("R6")
if(keyCheck=="6")
{
parent.sendIrData(key,deviceId)
sendEvent(name:"Mute", value: "Mute", descriptionText: "$device.displayName is set to Mute", displayed: true) 
}

  
}
def menu(){
def key="23"
def deviceId =  device.currentValue("device_b_one_id")
def keyCheck= device.currentValue("R23")
if(keyCheck=="23")
{
parent.sendIrData(key,deviceId)
sendEvent(name:"Menu", value: "Menu", descriptionText: "$device.displayName is set to Menu", displayed: true) 
}
  
}
def Up(){
def key="43"
def deviceId =  device.currentValue("device_b_one_id")
def keyCheck= device.currentValue("R43")
if(keyCheck=="43")
{
parent.sendIrData(key,deviceId)
sendEvent(name:"Up", value: "Up", descriptionText: "$device.displayName is set to Up", displayed: true) 
}
  
}
def Left(){
def key="42"
def deviceId =  device.currentValue("device_b_one_id")
def keyCheck= device.currentValue("R42")
if(keyCheck=="42")
{
parent.sendIrData(key,deviceId)
sendEvent(name:"Left", value: "Left", descriptionText: "$device.displayName is set to Left", displayed: true) 
}
  
}
def Right(){
def key="44"
def deviceId =  device.currentValue("device_b_one_id")
def keyCheck= device.currentValue("R44")
if(keyCheck=="44")
{
parent.sendIrData(key,deviceId)
sendEvent(name:"Right", value: "Right", descriptionText: "$device.displayName is set to Right", displayed: true) 
}
  
}
def Down(){
def key="46"
def deviceId =  device.currentValue("device_b_one_id")
def keyCheck= device.currentValue("R46")
if(keyCheck=="46")
{
parent.sendIrData(key,deviceId)
sendEvent(name:"Down", value: "Down", descriptionText: "$device.displayName is set to Down", displayed: true) 
}
  
}
def chup(){
def key="3"
def deviceId =  device.currentValue("device_b_one_id")
def keyCheck= device.currentValue("R3")
if(keyCheck=="3")
{
parent.sendIrData(key,deviceId)
sendEvent(name:"Channel Up", value: "Channel Up", descriptionText: "$device.displayName is set to Channel Up", displayed: true) 
}
  
}
def chdown(){
def key="2"
def deviceId =  device.currentValue("device_b_one_id")
def keyCheck= device.currentValue("R2")
if(keyCheck=="2")
{
parent.sendIrData(key,deviceId)
sendEvent(name:"Channel Down", value: "Channel Down", descriptionText: "$device.displayName is set to Channel Down", displayed: true) 
}

}
def volup(){
def key="5"
def deviceId =  device.currentValue("device_b_one_id")
def keyCheck= device.currentValue("R5")
if(keyCheck=="5")
{
parent.sendIrData(key,deviceId)
sendEvent(name:"Volume Up", value: "Volume Up", descriptionText: "$device.displayName is set to Volume Up", displayed: true) 
}

}
def voldown(){
def key="4"
def deviceId =  device.currentValue("device_b_one_id")
def keyCheck= device.currentValue("R4")
if(keyCheck=="4")
{
parent.sendIrData(key,deviceId)
sendEvent(name:"Volume Down", value: "Volume Down", descriptionText: "$device.displayName is set to Volume Down", displayed: true) 
}

}
def Enter(){
def key="45"
def deviceId =  device.currentValue("device_b_one_id")
def keyCheck= device.currentValue("R45")
if(keyCheck=="45")
{
parent.sendIrData(key,deviceId)
sendEvent(name:"Enter", value: "Enter", descriptionText: "$device.displayName is set to Enter", displayed: true) 
}

}
def Return(){
def key="55"
def deviceId =  device.currentValue("device_b_one_id")
def keyCheck= device.currentValue("R55")
if(keyCheck=="55")
{
parent.sendIrData(key,deviceId)
sendEvent(name:"Return", value: "Return", descriptionText: "$device.displayName is set to Return", displayed: true)
}
 
}
def Exit(){
def key="41"
def deviceId =  device.currentValue("device_b_one_id")
def keyCheck= device.currentValue("R41")
if(keyCheck=="41")
{
parent.sendIrData(key,deviceId)
sendEvent(name:"Exit", value: "Exit", descriptionText: "$device.displayName is set to Exit", displayed: true) 
}

}
def Info(){
def key="33"
def deviceId =  device.currentValue("device_b_one_id")
def keyCheck= device.currentValue("R33")
if(keyCheck=="33")
{
parent.sendIrData(key,deviceId)
sendEvent(name:"Info", value: "Info", descriptionText: "$device.displayName is set to Info", displayed: true) 
}

}
def Input(){
def key="25"
def deviceId =  device.currentValue("device_b_one_id")
def keyCheck= device.currentValue("R25")
if(keyCheck=="25")
{
parent.sendIrData(key,deviceId)
sendEvent(name:"Input", value: "Input", descriptionText: "$device.displayName is set to Input", displayed: true) 
}

}
def Pip(){
def key="31"
def deviceId =  device.currentValue("device_b_one_id")
def keyCheck= device.currentValue("R31")
if(keyCheck=="31")
{
parent.sendIrData(key,deviceId)
sendEvent(name:"Pip", value: "Pip", descriptionText: "$device.displayName is set to Pip", displayed: true) 
}

}
def lastch(){
def key="27"
def deviceId =  device.currentValue("device_b_one_id")
def keyCheck= device.currentValue("R27")
if(keyCheck=="27")
{
parent.sendIrData(key,deviceId)
sendEvent(name:"Last Channel", value: "Last Channel", descriptionText: "$device.displayName is set to Last Channel", displayed: true) 
}

}
def one(){
def key="14"
def deviceId =  device.currentValue("device_b_one_id")
def keyCheck= device.currentValue("R14")
if(keyCheck=="14")
{
parent.sendIrData(key,deviceId)
sendEvent(name:"1", value: "1", descriptionText: "$device.displayName is set to 1", displayed: true) 
}

}
def two(){
def key="15"
def deviceId =  device.currentValue("device_b_one_id")
def keyCheck= device.currentValue("R15")
if(keyCheck=="15")
{
parent.sendIrData(key,deviceId)
sendEvent(name:"2", value: "2", descriptionText: "$device.displayName is set to 2", displayed: true)
}
 
}
def three(){
def key="16"
def deviceId =  device.currentValue("device_b_one_id")
def keyCheck= device.currentValue("R16")
if(keyCheck=="16")
{
parent.sendIrData(key,deviceId)
sendEvent(name:"3", value: "3", descriptionText: "$device.displayName is set to 3", displayed: true) 
}

}
def four(){
def key="17"
def deviceId =  device.currentValue("device_b_one_id")
def keyCheck= device.currentValue("R17")
if(keyCheck=="17")
{
parent.sendIrData(key,deviceId)
sendEvent(name:"4", value: "4", descriptionText: "$device.displayName is set to 4", displayed: true) 
}

}
def five(){
def key="18"
def deviceId =  device.currentValue("device_b_one_id")
def keyCheck= device.currentValue("R18")
if(keyCheck=="18")
{
parent.sendIrData(key,deviceId)
sendEvent(name:"5", value: "5", descriptionText: "$device.displayName is set to 5", displayed: true) 
}

}
def six(){
def key="19"
def deviceId =  device.currentValue("device_b_one_id")
def keyCheck= device.currentValue("R19")
if(keyCheck=="19")
{
parent.sendIrData(key,deviceId)
sendEvent(name:"6", value: "6", descriptionText: "$device.displayName is set to 6", displayed: true) 
}

}
def seven(){
def key="20"
def deviceId =  device.currentValue("device_b_one_id")
def keyCheck= device.currentValue("R20")
if(keyCheck=="20")
{
parent.sendIrData(key,deviceId)
sendEvent(name:"7", value: "7", descriptionText: "$device.displayName is set to 7", displayed: true) 
}

}
def eight(){
def key="21"
def deviceId =  device.currentValue("device_b_one_id")
def keyCheck= device.currentValue("R21")
if(keyCheck=="21")
{
parent.sendIrData(key,deviceId)
sendEvent(name:"8", value: "8", descriptionText: "$device.displayName is set to 8", displayed: true) 
}

}
def nine(){
def key="22"
def deviceId =  device.currentValue("device_b_one_id")
def keyCheck= device.currentValue("R22")
if(keyCheck=="22")
{
parent.sendIrData(key,deviceId)
sendEvent(name:"9", value: "9", descriptionText: "$device.displayName is set to 9", displayed: true) 
}

}
def zero(){
def key="13"
def deviceId =  device.currentValue("device_b_one_id")
def keyCheck= device.currentValue("R13")
if(keyCheck=="13")
{
parent.sendIrData(key,deviceId)
sendEvent(name:"0", value: "0", descriptionText: "$device.displayName is set to 0", displayed: true) 
}

}
def minues(){
def key="35"
def deviceId =  device.currentValue("device_b_one_id")
def keyCheck= device.currentValue("R35")
if(keyCheck=="35")
{
parent.sendIrData(key,deviceId)
}

}

def generateEvent(Map results) {
log.debug("data tvremote for result:${results}")
	if(results) {
		def linkText = getLinkText(device)
		results.each { name, value ->
			def event = [name: name, linkText: linkText, handlerName: name]
			def sendValue = value
            if (name=="status") {
				sendValue =  value  // API return temperature values in F
				event << [value: sendValue]
			}else {
				event << [value: value.toString()]
			}

				event << [displayed: false]
				sendEvent(event)
		}
		
	}
}