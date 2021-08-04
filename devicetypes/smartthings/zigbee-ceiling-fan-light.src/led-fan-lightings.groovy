/*
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
 *  ZigBee Ceiling Fan Light
 *
 *  Author: SAMSUNG LED
 *  Date: 2021-06-30
 */

import physicalgraph.zigbee.zcl.DataType
import groovy.json.JsonOutput

metadata {
	definition (name: "LED FAN lightings", namespace: "SAMSUNG LED", author: "SAMSUNG LED") {
		capability "Actuator"		
		capability "Configuration"
		capability "Health Check"
		capability "Refresh"
		capability "Switch"
		capability "Switch Level"

		// Samsung LED
		fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0008, 0300", outClusters: "0019", manufacturer: "Samsung Electronics", model: "SAMSUNG-ITM-Z-003", deviceJoinName: "Samsung Light", mnmn: "Samsung Electronics", vid: "SAMSUNG-ITM-Z-003"
	}

	// UI tile definitions
	tiles(scale: 2) {
		multiAttributeTile(name: "switch", type: "lighting", width: 6, height: 4, canChangeIcon: true) {
			tileAttribute("device.switch", key: "PRIMARY_CONTROL") {
				attributeState "on", label: '${name}', action: "off", icon: "st.switches.light.on", backgroundColor: "#00A0DC", nextState: "turningOff"
				attributeState "off", label: '${name}', action: "on", icon: "st.switches.light.off", backgroundColor: "#ffffff", nextState: "turningOn"
				attributeState "turningOn", label: '${name}', action: "off", icon: "st.switches.light.on", backgroundColor: "#00A0DC", nextState: "turningOff"
				attributeState "turningOff", label: '${name}', action: "on", icon: "st.switches.light.off", backgroundColor: "#ffffff", nextState: "turningOn"
			}
			tileAttribute("device.level", key: "SLIDER_CONTROL") {
				attributeState "level", action: "switch level.setLevel"
			}
		}

		standardTile("refresh", "device.refresh", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "default", label: "", action: "refresh.refresh", icon: "st.secondary.refresh"
		}
		
		main(["switch"])
		details(["switch", "refresh", "switchLevel"])
	}
}

private getFAN_CLUSTER_VALUE() { 0x0202 }
private getFAN_STATUS_VALUE() { 0x0000 }
private getON_OFF_CLUSTER_VALUE() { 0x0006 }

def parse(String description) {
	// Parse incoming device messages to generate events
	log.debug "description is $description"
	def event = zigbee.getEvent(description)
	def zigbeeMap = zigbee.parseDescriptionAsMap(description)
	if (event) {
		sendEvent(event)
	} else if (description?.startsWith('read attr -')) {
		if (zigbeeMap.clusterInt == FAN_CLUSTER_VALUE &&
		    zigbeeMap.attrInt == FAN_STATUS_VALUE) {		
			log.debug "read attribute event for fan cluster attrib FAN_STATUS"
			def childDevice = getChildDevices()?.find {
				//find light child device
				log.debug "parse() child device found"
				it.device.deviceNetworkId == "${device.deviceNetworkId}:1" 
			}          
			event.displayed = true
			zigbeeMap.name = "fanSpeed"
			childDevice.sendEvent(zigbeeMap)
			if (zigbeeMap.value == "00") {
				log.debug "fan_off => switch off"
				childDevice.sendEvent(name: "switch", value: "off")
				childDevice.sendEvent(name: "level", value: 0)	// For cloud to cloud device UI update
			} else {
				log.debug "fan_on => switch on"
				childDevice.sendEvent(name: "switch", value: "on")
				def int_v  = zigbeeMap.value as Integer
				int_v = int_v * 25
				int_v = int_v > 100 ? 100 : int_v
				log.debug "child device Level set $int_v"
				childDevice.sendEvent(name: "level", value: int_v)	// For cloud to cloud device UI update
			}
		}		
	} else {
		def cluster = zigbee.parse(description)
		if (cluster && 
		    cluster.clusterId == ON_OFF_CLUSTER_VALUE &&
		    cluster.command == 0x07) {
			if (cluster.data[0] == 0x00) {
				log.debug "ON/OFF REPORTING CONFIG RESPONSE: " + cluster
				sendEvent(name: "checkInterval", value: 60 * 12, displayed: false, data: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID])
			} else {
				log.warn "ON/OFF REPORTING CONFIG FAILED- error code:${cluster.data[0]}"
			}
		}
	}
}

def off() {
	zigbee.off()
}

def on() {
	zigbee.on()
}

def setLevel(value) {
	zigbee.setLevel(value)
}

def setFanSpeed(speed, device=null) {
	if (device == null) {
		return
	}
    	log.debug "parent setFanSpeed"
    	if (speed as Integer == 0) {		
        	log.debug "fan_off"
	    	sendFanSpeed(0x00)
	} else if (speed as Integer == 1) {		
        	log.debug "low"
	    	sendFanSpeed(0x01)
	} else if (speed as Integer == 2) {		
        	log.debug "medium"	
        	sendFanSpeed(0x02)    
	} else if (speed as Integer == 3) {		
        	log.debug "high"	
	    	sendFanSpeed(0x03)
    	} else if (speed as Integer == 4) {		
        	log.debug "max"
	    	sendFanSpeed(0x04)
    	} else {		
        	log.debug "max"
	    	sendFanSpeed(0x04)
    	}    
}

private sendFanSpeed(val) {
	delayBetween([zigbee.writeAttribute(FAN_CLUSTER_VALUE, FAN_STATUS_VALUE, DataType.ENUM8, val), zigbee.readAttribute(FAN_CLUSTER_VALUE, FAN_STATUS_VALUE)], 100)
}

def ping() {
	// PING is used by Device-Watch in attempt to reach the Device
	return zigbee.onOffRefresh()
}

def refresh() {
	zigbee.onOffRefresh() +
	zigbee.levelRefresh() +
	zigbee.onOffConfig(0, 300) +
	zigbee.levelConfig()+
	zigbee.readAttribute(FAN_CLUSTER_VALUE, FAN_STATUS_VALUE)
}

def configure() {
	log.debug "Configuring Reporting and Bindings."
	// Device-Watch allows 2 check-in misses from device + ping (plus 1 min lag time)
	// enrolls with default periodic reporting until newer 5 min interval is confirmed
	sendEvent(name: "checkInterval", value: 2 * 10 * 60 + 1 * 60, displayed: false, data: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID])
	// OnOff minReportTime 0 seconds, maxReportTime 5 min. Reporting interval if no activity
	refresh()
}

def installed() {
	log.debug "Samsung ITM test prod installed"
	addChildFan()    
	configure()
}

def addChildFan() {
	log.debug "add child fan"
	def componentLabel
	if (device.displayName.endsWith(' Light') ||
	    device.displayName.endsWith(' light')) {
		componentLabel = "${device.displayName[0..-6]} Fan"
	} else {
		// no '1' at the end of deviceJoinName - use 2 to indicate second switch anyway
		componentLabel = "$device.displayName Fan"
	}	
	try {
		String dni = "${device.deviceNetworkId}:1"
		addChildDevice("ITM Fan Child", dni, device.hub.id, [completedSetup: true, label: "${componentLabel}", isComponent: false])
		log.debug "Child Fan device (ITM Fan Controller) added as $componentLabel"
	} catch(e) {
		log.warn "Failed to add ITM Fan Controller - $e"
	}
    	def childDevice = getChildDevices()?.find {
		//find light child device
        	log.debug "parse() child device found"
        	it.device.deviceNetworkId == "${device.deviceNetworkId}:1" 
    	}
	if (childDevice != null) {
		childDevice.sendEvent(name: "switch", value: "off")
	}
}

def deleteChildren() {	
	def children = getChildDevices()        	
	children.each { 
		child -> deleteChildDevice(child.deviceNetworkId)
    	}	
	log.info "Deleting children"                  
}

def delete() {
	log.debug "[Parent] - delete()"
}

def uninstalled() {
	log.debug "[Parent] - uninstalled"
	try{
		def childDevice = getChildDevices()?.find {		
			//find light child device
			log.debug "parse() child device found"
			it.device.deviceNetworkId == "${device.deviceNetworkId}:1" 
        	}
		if (childDevice != null) {
			deleteChildren()
		}
    	} catch(e) {}
}
