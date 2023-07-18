/*
 *  Copyright 2021 SmartThings
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
	def event = zigbee.getEvent(description)	
	if (event) {
		sendEvent(event)
	} else if (description?.startsWith('read attr -')) {
		def zigbeeMap = zigbee.parseDescriptionAsMap(description)
		if (zigbeeMap.clusterInt == FAN_CLUSTER_VALUE &&
		    zigbeeMap.attrInt == FAN_STATUS_VALUE) {
			def childDevice = childDevices.find {
				//find light child device
				it.device.deviceNetworkId == "${device.deviceNetworkId}:1" 
			}
			def fanSpeedEvent = createEvent(name: "fanSpeed", value: zigbeeMap.value as Integer)
			childDevice.sendEvent(fanSpeedEvent)
			if (fanSpeedEvent.value == 0) {
				childDevice.sendEvent(name: "switch", value: "off")
				childDevice.sendEvent(name: "level", value: 0)	// For cloud to cloud device UI update
			} else {
				childDevice.sendEvent(name: "switch", value: "on")
				def int_v = fanSpeedEvent.value
				int_v = int_v * 25
				int_v = int_v > 100 ? 100 : int_v
				childDevice.sendEvent(name: "level", value: int_v)	// For cloud to cloud device UI update
			}
		}		
	} else {
		def cluster = zigbee.parse(description)
		if (cluster && 
		    cluster.clusterInt == ON_OFF_CLUSTER_VALUE &&
		    cluster.command == 0x07) {
			if (cluster.data[0] == 0x00) {
				sendEvent(name: "checkInterval", value: 60 * 12, displayed: false, data: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID])
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

def setLevel(value, duration) {
	zigbee.setLevel(value)
}

def sendFanSpeed(val) {
	delayBetween([zigbee.writeAttribute(FAN_CLUSTER_VALUE, FAN_STATUS_VALUE, DataType.ENUM8, val), zigbee.readAttribute(FAN_CLUSTER_VALUE, FAN_STATUS_VALUE)], 100)
}

def ping() {
	// PING is used by Device-Watch in attempt to reach the Device	
	return zigbee.onOffRefresh() +
		zigbee.readAttribute(FAN_CLUSTER_VALUE, FAN_STATUS_VALUE)
}

def refresh() {
	zigbee.onOffRefresh() +
	zigbee.levelRefresh() +	
	zigbee.readAttribute(FAN_CLUSTER_VALUE, FAN_STATUS_VALUE)
}

def configure() {
	// Device-Watch allows 2 check-in misses from device + ping (plus 1 min lag time)
	// enrolls with default periodic reporting until newer 5 min interval is confirmed
	sendEvent(name: "checkInterval", value: 2 * 10 * 60 + 1 * 60, displayed: false, data: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID])
	// OnOff minReportTime 0 seconds, maxReportTime 5 min. Reporting interval if no activity
	return zigbee.onOffConfig(0, 300) +
		zigbee.levelConfig() +
		refresh()
}

def installed() {
	addChildFan()
}

def addChildFan() {
	def componentLabel
	def childDevice
	
	if (device.displayName.endsWith(' Light') ||
	    device.displayName.endsWith(' light')) {
		componentLabel = "${device.displayName[0..-6]} Fan"
	} else {
		// no '1' at the end of deviceJoinName - use 2 to indicate second switch anyway
		componentLabel = "$device.displayName Fan"
	}	
	try {
		String dni = "${device.deviceNetworkId}:1"
		childDevice = addChildDevice("ITM Fan Child", dni, device.hub.id, [completedSetup: true, label: "${componentLabel}", isComponent: false])
	} catch(e) {
		log.warn "Failed to add ITM Fan Controller - $e"
	}
    	
	if (childDevice != null) {
		childDevice.sendEvent(name: "switch", value: "off")
	}
}
