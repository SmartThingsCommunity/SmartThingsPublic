import physicalgraph.zigbee.zcl.DataType
import groovy.json.JsonOutput


metadata {
    definition(name: "ITM Fan Child", namespace: "SAMSUNG LED", author: "SAMSUNG LED", ocfDeviceType: "oic.d.fan", genericHandler: "Zigbee"){//, mnmn: "SmartThings"){//, vid: "SmartThings-smartthings-Z-Wave_Fan_Controller_4_Speed"){  
        capability "Actuator"
        capability "Configuration"
        capability "Refresh"
        capability "Switch"
        //capability "Switch Level"
        capability "Fan Speed"
        
        command "low"
		command "medium"
		command "high"
        command "max"
		command "raiseFanSpeed"
		command "lowerFanSpeed"

        //fingerprint profileId: "0104", inClusters: "0000,0003,0004,0005,0006,0202", outClusters : "0019", manufacturer: "chw", model: "chw", deviceJoinName: "ITM Fan", mnmn: "SmartThings", vid: "SmartThings-smartthings-Z-Wave_Fan_Controller_4_Speed"
    }
/*
    tiles(scale: 2) {
        multiAttributeTile(name: "fanSpeed", type: "generic", width: 6, height: 4, canChangeIcon: true) {
			tileAttribute("device.fanSpeed", key: "PRIMARY_CONTROL") {
				attributeState "0", label: "off", action: "switch.on", icon: "st.thermostat.fan-off", backgroundColor: "#ffffff"
				attributeState "1", label: "low", action: "switch.off", icon: "st.thermostat.fan-on", backgroundColor: "#00a0dc"
				attributeState "2", label: "medium", action: "switch.off", icon: "st.thermostat.fan-on", backgroundColor: "#00a0dc"
				attributeState "3", label: "high", action: "switch.off", icon: "st.thermostat.fan-on", backgroundColor: "#00a0dc"                
			}
			tileAttribute("device.fanSpeed", key: "VALUE_CONTROL") {
				attributeState "VALUE_UP", action: "raiseFanSpeed"
				attributeState "VALUE_DOWN", action: "lowerFanSpeed"
			}
		}
        
        multiAttributeTile(name: "switch", type: "generic", width: 6, height: 4, canChangeIcon: true) {
			tileAttribute("device.switch", key: "PRIMARY_CONTROL") {
				attributeState "on", label: '${name}', action: "switch.off", icon: "st.switches.fan-on", backgroundColor: "#00A0DC", nextState: "turningOff"
				attributeState "off", label: '${name}', action: "switch.on", icon: "st.switches.fan-off", backgroundColor: "#ffffff", nextState: "turningOn"
				attributeState "turningOn", label: '${name}', action: "switch.off", icon: "st.switches.fan-on", backgroundColor: "#00A0DC", nextState: "turningOff"
				attributeState "turningOff", label: '${name}', action: "switch.on", icon: "st.switches.fan-off", backgroundColor: "#ffffff", nextState: "turningOn"
			}
			tileAttribute("device.level", key: "SLIDER_CONTROL") {
				attributeState "level", action: "switch level.setLevel"
			}
		}
		standardTile("refresh", "device.switch", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
			state "default", label: '', action: "refresh.refresh", icon: "st.secondary.refresh"
		}
		main "fanSpeed"
		details(["fanSpeed", "switch", "level", "refresh"])
	}*/
    
}

def setRapidCooling(value){
	log.debug "setRapidCooling $value"
    if(value == "on")
    	sendEvent(name: "rapidCooling", value: "on")
    else
    	sendEvent(name: "rapidCooling", value: "off")
}

def raiseFanSpeed() {
log.debug "raiseFanSpeed"
	setFanSpeed(Math.min((device.currentValue("fanSpeed") as Integer) + 1, 4))
}

def lowerFanSpeed() {
log.debug "lowerFanSpeed"
	setFanSpeed(Math.max((device.currentValue("fanSpeed") as Integer) - 1, 0))
}

def off() {
   setFanSpeed(0)
}
def on() {
   setFanSpeed(1)
}

/*
def setLevel(value) {
    parent.setLevel(value, null, device)
}
*/

def setFanSpeed(speed) {
	log.debug "child setFanSpeed $speed"
	
    if (speed as Integer == 0) 
    {
        sendEvent(name: "switch", value: "off", displayed: true, isStateChange: true)
        sendEvent(name: "device.switch", value: "off", displayed: true, isStateChange: true)
    }
    else{
        sendEvent(name: "switch", value: "on", displayed: true, isStateChange: true)
        sendEvent(name: "device.switch", value: "on", displayed: true, isStateChange: true)
    }   
    
    parent_fan_Speed(speed)
}

def parent_fan_Speed(speed){
    parent.setFanSpeed(speed, device)
}

def fan_off() {
	log.debug "fan_off"
	send_fanSpeed(0x00)
}
def low() {
	log.debug "low"
	send_fanSpeed(0x01)
}

def medium() {
	log.debug "medium"
	//fan_speed = 2
    send_fanSpeed(0x02)
    //sendZigbeeCommands(cmds)
}

def high() {
	log.debug "high"
	//fan_speed = 3
	send_fanSpeed(0x03)
}

def max() {
	log.debug "max"
	send_fanSpeed(0x04)
}

def isStateChange(event) {
	log.trace "[isStateChange(${event})]"
    
	def eventChange = true;
	switch(event['name']) {
    	case "switch": 
        	 eventChange = (event['value'] != null && event['value'] != device.currentValue("switch"))
        	break
        case "fanSpeed":
        	eventChange = (event['value'] != null && event['value'] != device.currentValue("fanSpeed"))
        	break
        default:
        	log.debug "[isStateChange] Unknown event: ${event}"
            break;
    }
    
    log.trace "[isStateChange] ${eventChange}"
    return eventChange
}

def parse(String description) {
    log.debug "[Child] - PARSE IN Child: $description"
}
/*
// Parse incoming device messages to generate events
def parse(String description) {
   log.debug "parse: description is $description"
   def event = zigbee.getEvent(description)
   def zigbeeMap = zigbee.parseDescriptionAsMap(description)
   def cluster = zigbee.parse(description)
   if (event) {
       //if (event.name == "colorTemperature") {
       //    event.unit = "K"
       //}
       
       log.debug "parse: Sending event $event"
       sendEvent(event)
   }
    else if (description?.startsWith('read attr -')) {
		log.debug "read attribute event"
		if (zigbeeMap.cluster == "0202" && zigbeeMap.attrId == "0000"){
        	log.debug "read attr cluster 0202"
            zigbeeMap.name = "fanSpeed"
            sendEvent(zigbeeMap)
            if(zigbeeMap.value == "00"){
            	log.debug "fan_off => switch off"
                sendEvent(name: "switch", value: "off")
            }
            else{
            	log.debug "fan_on => switch on"
            	sendEvent(name: "switch", value: "on")
            }
        }
	}
   else if(cluster.clusterId == 0x0202){
   		log.debug "confirm write attrib fanspeed"
        //zigbee.readAttribute(0x0202, 0x0000)
        //sendEvent(name: "fanSpeed", value: "02")
   }
   else {
       log.warn "parse: DID NOT PARSE MESSAGE for description : $description"
       log.debug zigbee.parseDescriptionAsMap(description)
   }
}

private send_fanSpeed(val){
	delayBetween([
     	//zigbee.writeAttribute(0x0202, 0x0001, DataType.ENUM8, 0x00),
        zigbee.writeAttribute(0x0202, 0x0000, DataType.ENUM8, val),
		zigbee.readAttribute(0x0202, 0x0000)
	], 100)
}
*/
def read_fan_status(){
	zigbee.readAttribute(0x0202, 0x0000)
}

void refresh() {
	log.debug "[Child] - refresh()"
    parent.refresh(device)
	parent.childRefresh(device.deviceNetworkId)
}
/*
def refresh() {
   def cmds =  zigbee.onOffRefresh() + read_fan_status()
   cmds
}*/

def ping() {
    log.debug "[Child] - ping()"
    parent.ping(device)
}

def configure() {
   log.debug "[Child] - configure()"
   //sendEvent(name: "checkInterval", value: 60, displayed: false, data: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID])
   //zigbee.onOffConfig() + zigbee.levelConfig() + zigbee.onOffRefresh() + zigbee.levelRefresh() + zigbee.colorTemperatureRefresh()
   //zigbee.onOffConfig() + zigbee.onOffRefresh() + zigbee.readAttribute(0x0202, 0x0000)
   /*
   delayBetween([
     	zigbee.onOffConfig(),
        zigbee.onOffRefresh(),
		zigbee.readAttribute(0x0202, 0x0000),
	], 100)
    
    delayBetween([
    	zigbee.configureReporting(0x0006, 0x0000, DataType.BOOL, 1, 3600, 1),
    	zigbee.configureReporting(0x0202, 0x0000, DataType.ENUM8, 1, 3600, 1),
    	zigbee.configureReporting(0x0202, 0xfd01, DataType.ENUM8, 1, 3600, 1)
    ], 100)*/
}
def updated() {
   log.debug "[Child] - updated()"
}
/*
def setColorTemperature(value) {
    log.debug "color control"
    value = value as Integer
       def tempInMired = Math.round(1000000 / value)
       def finalHex = zigbee.swapEndianHex(zigbee.convertToHexString(tempInMired, 4))
       zigbee.command(COLOR_CONTROL_CLUSTER, MOVE_TO_COLOR_TEMPERATURE_COMMAND, "$finalHex", "0000") +    
    ["delay 700"] +
       zigbee.readAttribute(COLOR_CONTROL_CLUSTER, ATTRIBUTE_COLOR_TEMPERATURE)
}*/
def installed() {
   log.debug "[Child] - installed"
   configure()
}

def uninstalled() {
	log.debug "[Child] - uninstalled()"
	//parent.delete()
}
