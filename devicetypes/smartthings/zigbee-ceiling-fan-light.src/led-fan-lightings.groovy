/**
 *  Copyright 2017 SmartThings
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
 *  ZigBee White Color Temperature Bulb
 *
 *  Author: SmartThings
 *  Date: 2015-09-22
 */

import physicalgraph.zigbee.zcl.DataType
import groovy.json.JsonOutput

metadata {
	definition(name: "LED FAN lightings", namespace: "SAMSUNG LED", author: "SAMSUNG LED", runLocally: true, minHubCoreVersion: '000.019.00012', executeCommandsLocally: true, genericHandler: "Zigbee") {

		capability "Actuator"
		//capability "Color Temperature"
		capability "Configuration"
		capability "Health Check"
		capability "Refresh"
		capability "Switch"
		capability "Switch Level"
		capability "Light"

		attribute "colorName", "string"		

		// Samsung LED
		fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0008, 0300", outClusters: "0019", manufacturer: "Samsung Electronics", model: "SAMSUNG-ITM-Z-003", deviceJoinName: "Samsung Light", mnmn: "Samsung Electronics", vid: "SAMSUNG-ITM-Z-003" //ITM CCT
		
	}

	// UI tile definitions
	tiles(scale: 2) {
		multiAttributeTile(name: "switch", type: "lighting", width: 6, height: 4, canChangeIcon: true) {
			tileAttribute("device.switch", key: "PRIMARY_CONTROL") {
				attributeState "on", label: '${name}', action: "switch.off", icon: "st.switches.light.on", backgroundColor: "#00A0DC", nextState: "turningOff"
				attributeState "off", label: '${name}', action: "switch.on", icon: "st.switches.light.off", backgroundColor: "#ffffff", nextState: "turningOn"
				attributeState "turningOn", label: '${name}', action: "switch.off", icon: "st.switches.light.on", backgroundColor: "#00A0DC", nextState: "turningOff"
				attributeState "turningOff", label: '${name}', action: "switch.on", icon: "st.switches.light.off", backgroundColor: "#ffffff", nextState: "turningOn"
			}
			tileAttribute("device.level", key: "SLIDER_CONTROL") {
				attributeState "level", action: "switch level.setLevel"
			}
		}

		standardTile("refresh", "device.refresh", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "default", label: "", action: "refresh.refresh", icon: "st.secondary.refresh"
		}

		controlTile("colorTempSliderControl", "device.colorTemperature", "slider", width: 4, height: 2, inactiveLabel: false, range: "(2700..6500)") {
			state "colorTemperature", action: "color temperature.setColorTemperature"
		}
		valueTile("colorName", "device.colorName", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "colorName", label: '${currentValue}'
		}
		main(["switch"])
		details(["switch", "colorTempSliderControl", "colorName", "refresh", "switchLevel", "fanSpeed"])
	}
}

// Globals
private getMOVE_TO_COLOR_TEMPERATURE_COMMAND() { 0x0A }

private getCOLOR_CONTROL_CLUSTER() { 0x0300 }

private getATTRIBUTE_COLOR_TEMPERATURE() { 0x0007 }

// Parse incoming device messages to generate events
def parse(String description) {
	log.debug "description is $description"
	def event = zigbee.getEvent(description)
    def zigbeeMap = zigbee.parseDescriptionAsMap(description)
	if (event) {
		if (event.name == "level" && event.value == 0) {
		} else {
			if (event.name == "colorTemperature") {
				setGenericName(event.value)
			}
			sendEvent(event)
		}
	} else if (description?.startsWith('read attr -') && zigbeeMap.cluster == "0202" && zigbeeMap.attrId == "0000") {
		//map = parseReadAttributeMessage(description)
		log.debug "read attribute event for fan cluster attrib 0x0000"
        def childDevice = getChildDevices()?.find {		//find light child device
        	log.debug "parse() child device found"
            it.device.deviceNetworkId == "${device.deviceNetworkId}-Fan" 
        }          
        event.displayed = true
        def isStateChange = childDevice.isStateChange(event)
        log.trace "isStateChange: ${isStateChange}"
        event.isStateChange = isStateChange
        
        
        zigbeeMap.name = "fanSpeed"
        childDevice.sendEvent(zigbeeMap)
        if(zigbeeMap.value == "00"){
            log.debug "fan_off => switch off"
            childDevice.sendEvent(name: "switch", value: "off")
            childDevice.sendEvent(name: "level", value: 0)
        }
        else{
            log.debug "fan_on => switch on"
            childDevice.sendEvent(name: "switch", value: "on")
            def int_v  = zigbeeMap.value as Integer
            int_v = int_v * 25
            int_v = int_v>100?100:int_v
            log.debug "child device Level set $int_v"
            childDevice.sendEvent(name: "level", value: int_v)
        }
	}
    else {
		def cluster = zigbee.parse(description)

		if (cluster && cluster.clusterId == 0x0006 && cluster.command == 0x07) {
			if (cluster.data[0] == 0x00) {
				log.debug "ON/OFF REPORTING CONFIG RESPONSE: " + cluster
				sendEvent(name: "checkInterval", value: 60 * 12, displayed: false, data: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID])
			} else {
				log.warn "ON/OFF REPORTING CONFIG FAILED- error code:${cluster.data[0]}"
			}
		} else {
			//log.warn "DID NOT PARSE MESSAGE for description : $description"
			//log.debug "${cluster}"
		}
	}
}

def off() {
	zigbee.off()
}

def on() {
	zigbee.on()
}

def setLevel(value, rate = null, device = null) {
	if(device == null)	zigbee.setLevel(value)
    else{
    	if(value <= 1){
            setFanSpeed(0, device)
        }
        else if(value <= 25){
            setFanSpeed(1, device)
        }
        else if(value <= 50){
            setFanSpeed(2, device)
        }
        else if(value <= 75){
            setFanSpeed(3, device)
        }        
        else if(value <= 100){
            setFanSpeed(4, device)
        }        
    }
}

def setFanSpeed(speed, device=null){
	if(device == null) return
    log.debug "parent setFanSpeed"
    if (speed as Integer == 0) {
		fan_off()// + createEvent(name: "fanSpeed", value: 0)
	} else if (speed as Integer == 1) {
		low()// + createEvent(name: "fanSpeed", value: 1)
	} else if (speed as Integer == 2) {
		medium()// + createEvent(name: "fanSpeed", value: 2)
	} else if (speed as Integer == 3) {
		high()// + createEvent(name: "fanSpeed", value: 3)    
    } else if (speed as Integer == 4) {
		max()// + createEvent(name: "fanSpeed", value: 3)
    } else {
		max()// + createEvent(name: "fanSpeed", value: 3)
    }    
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

private send_fanSpeed(val){
	delayBetween([
     	//zigbee.writeAttribute(0x0202, 0x0001, DataType.ENUM8, 0x00),
        zigbee.writeAttribute(0x0202, 0x0000, DataType.ENUM8, val),
		zigbee.readAttribute(0x0202, 0x0000)
	], 100)
}

/**
 * PING is used by Device-Watch in attempt to reach the Device
 * */
def ping() {
	return zigbee.onOffRefresh()
}

def refresh() {
	zigbee.onOffRefresh() +
			zigbee.levelRefresh() +
			zigbee.colorTemperatureRefresh() +
			zigbee.onOffConfig(0, 300) +
			zigbee.levelConfig()
}

def configure() {
	log.debug "Configuring Reporting and Bindings."
	// Device-Watch allows 2 check-in misses from device + ping (plus 1 min lag time)
	// enrolls with default periodic reporting until newer 5 min interval is confirmed
	sendEvent(name: "checkInterval", value: 2 * 10 * 60 + 1 * 60, displayed: false, data: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID])

	// OnOff minReportTime 0 seconds, maxReportTime 5 min. Reporting interval if no activity
	refresh()
}

/*
def setColorTemperature(value) {
	value = value as Integer
	def tempInMired = Math.round(1000000 / value)
	def finalHex = zigbee.swapEndianHex(zigbee.convertToHexString(tempInMired, 4))

	List cmds = []
	if (device.getDataValue("manufacturer") == "sengled" && device.getDataValue("model") == "Z01-A19NAE26") {
		// Sengled Element Plus will ignore the command if the transition time is 0x0000
		cmds << zigbee.command(COLOR_CONTROL_CLUSTER, MOVE_TO_COLOR_TEMPERATURE_COMMAND, "$finalHex 0100")
	} else {
		cmds << zigbee.command(COLOR_CONTROL_CLUSTER, MOVE_TO_COLOR_TEMPERATURE_COMMAND, "$finalHex 0000")
	}
	cmds << zigbee.readAttribute(COLOR_CONTROL_CLUSTER, ATTRIBUTE_COLOR_TEMPERATURE)
	cmds
}
*/

//Naming based on the wiki article here: http://en.wikipedia.org/wiki/Color_temperature
def setGenericName(value) {
	if (value != null) {
		def genericName = "White"
		if (value < 3300) {
			genericName = "Soft White"
		} else if (value < 4150) {
			genericName = "Moonlight"
		} else if (value <= 5000) {
			genericName = "Cool White"
		} else if (value >= 5000) {
			genericName = "Daylight"
		}
		sendEvent(name: "colorName", value: genericName)
	}
}

def installed() {
	log.debug "Samsung ITM test prod installed"
    addChildFan()
    configure() 
    return
    /*If((device.getDataValue("manufacturer") == "Samsung Electronics") && ((device.getDataValue("model") == "SAMSUNG-ITM-Z-001"))){
    	log.debug "Samsung ITM test prod installed"
    	addChildFan()
        configure() 
    }
    
    if (((device.getDataValue("manufacturer") == "MRVL") && (device.getDataValue("model") == "MZ100"))
			|| (device.getDataValue("manufacturer") == "OSRAM SYLVANIA")
			|| (device.getDataValue("manufacturer") == "OSRAM")
			|| (device.getDataValue("manufacturer") == "sengled")
			|| (device.getDataValue("manufacturer") == "Third Reality, Inc")) {
		if ((device.currentState("level")?.value == null) || (device.currentState("level")?.value == 0)) {
			sendEvent(name: "level", value: 100)
		}
	}
    */
}

def addChildFan(){
        log.debug "add child fan"
	def componentLabel
	if (device.displayName.endsWith(' Light') || device.displayName.endsWith(' light')) {
		componentLabel = "${device.displayName[0..-6]} Fan"
	} else {
		// no '1' at the end of deviceJoinName - use 2 to indicate second switch anyway
		componentLabel = "$device.displayName Fan"
	}
	try {
		String dni = "${device.deviceNetworkId}-Fan"
		addChildDevice("ITM Fan Child", dni, device.hub.id,
			[completedSetup: true, label: "${componentLabel}", isComponent: false])
		log.debug "Child Fan device (ITM Fan Controller) added as $componentLabel"
	} catch (e) {
		log.warn "Failed to add ITM Fan Controller - $e"
	}
    def childDevice = getChildDevices()?.find {		//find light child device
        log.debug "parse() child device found"
        it.device.deviceNetworkId == "${device.deviceNetworkId}-Fan" 
    }
    if(childDevice != null)    childDevice.sendEvent(name: "switch", value: "off")
}

def deleteChildren() {	
	def children = getChildDevices()        	
    children.each {child->
  		deleteChildDevice(child.deviceNetworkId)
    }	
    log.info "Deleting children"                  
}

def delete(){
	log.debug "[Parent] - delete()"
}

def uninstalled(){
	log.debug "[Parent] - uninstalled"
    try{
        def childDevice = getChildDevices()?.find {		//find light child device
            log.debug "parse() child device found"
            it.device.deviceNetworkId == "${device.deviceNetworkId}-Fan" 
        }
        if(childDevice != null)    deleteChildren()
    }
    catch(e){}
    /*
    if(device.deviceNetworkId == null){
    	log.debug "[Parent] - aleady parent uninstalled"
    }
    else{
    	log.debug "[Parent] - alive parent"
        deleteChildDevice(device.deviceNetworkId)
    }*/
}
