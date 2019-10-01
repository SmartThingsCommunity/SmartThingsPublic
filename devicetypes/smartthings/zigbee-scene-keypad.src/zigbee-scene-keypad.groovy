/**
 *  Copyright 2019 SmartThings
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
 *	Author: f.mei@samsung.com
 *	Date: 2019-02-18
 */

import groovy.json.JsonOutput
import physicalgraph.zigbee.zcl.DataType

metadata {
	definition (name: "Zigbee Scene Keypad", namespace: "smartthings", author: "SmartThings", mcdSync: true) {
		capability "Actuator"
		capability "Button"
		capability "Configuration"
		capability "Refresh"
		capability "Sensor"
		capability "Health Check"

                fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005", outClusters: "0003, 0004, 0005", manufacturer: "REXENSE", model: "HY0048", deviceJoinName: "情景开关 1", vid: "generic-4-button-alt"
                fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005", outClusters: "0003, 0004, 0005", manufacturer: "REXENSE", model: "0106-G", deviceJoinName: "情景开关 1", vid: "generic-6-button-alt"
                fingerprint profileId: "0104", inClusters: "0000, 0005", outClusters: "0000, 0005, 0017", manufacturer: "ORVIBO", model: "cef8701bb8664a67a83033c071ef05f2", deviceJoinName: "情景开关 1", vid: "generic-3-button-alt"

    }

	tiles {
		standardTile("button", "device.button", width: 2, height: 2) {
			state "default", label: "", icon: "st.unknown.zwave.remote-controller", backgroundColor: "#ffffff"
			state "button 1 pushed", label: "pushed #1", icon: "st.unknown.zwave.remote-controller", backgroundColor: "#00A0DC"
		}
        
		standardTile("refresh", "device.refresh", inactiveLabel: false, decoration: "flat") {
			state "default", action:"refresh.refresh", icon:"st.secondary.refresh"
		}
		main (["button"])
		details(["button", "refresh"])
	}
}

def parse(String description) {
	def map = zigbee.getEvent(description)
	def result = map ? map : parseAttrMessage(description)
	if (result?.name == "switch") {
		result = createEvent(descriptionText: "Wake up event came in", isStateChange: true)
	}
	log.debug "Description ${description} parsed to ${result}"
	return result
}

def parseAttrMessage(description) {
	def descMap = zigbee.parseDescriptionAsMap(description)    
	if (descMap?.clusterInt == 0x0017 || descMap?.clusterInt == 0xFE05) {
	        def event = [:]
                def buttonNumber
                if (descMap?.clusterInt == 0x0017) {
       			buttonNumber = Integer.valueOf(descMap.data[0]) 
                } else if (descMap?.clusterInt == 0xFE05) {
       			buttonNumber = Integer.valueOf(descMap?.value)
                }
       		log.debug "Number is ${buttonNumber}"
                event = createEvent(name: "button", value: "pushed", data: [buttonNumber: buttonNumber], descriptionText: "pushed", isStateChange: true)
                if (buttonNumber != 1) {
       			sendEventToChild(buttonNumber, event)
                } else {
       			sendEvent(event)
       	        }
    	}
}

def sendEventToChild(buttonNumber, event) {
	String childDni = "${device.deviceNetworkId}:$buttonNumber"
	def child = childDevices.find { it.deviceNetworkId == childDni }
	child?.sendEvent(event)
}

def refresh() {
	return zigbee.enrollResponse()
}

def ping() {
	refresh()
}

def configure() {
	return zigbee.enrollResponse()
}

def installed() {
    	def numberOfButtons = getChildCount()
	sendEvent(name: "numberOfButtons", value: numberOfButtons, displayed: false)
	sendEvent(name: "checkInterval", value: 32 * 60, displayed: false, data: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID])
	if (!childDevices) {
		addChildButtons(numberOfButtons)
	}
	sendEvent(name: "supportedButtonValues", value: ["pushed"])
}

def updated() {
	runIn(2, "initialize", [overwrite: true])
}

def initialize() {
	
}

private addChildButtons(numberOfButtons) {
	for (def i : 2..numberOfButtons) {
		try {
			String childDni = "${device.deviceNetworkId}:$i"
			def componentLabel = (device.displayName.endsWith(' 1') ? device.displayName[0..-2] : device.displayName) + "${i}"
			addChildDevice("Child Button", "${device.deviceNetworkId}:${i}", device.hubId,
                	[completedSetup: true, label: "${device.displayName} button ${i}",
                 	isComponent: true, componentName: "button$i", componentLabel: "Button $i"])
		} catch (Exception e) {
			log.debug "Exception: ${e}"
		}
	}
}

private getChildCount() {
	if (device.getDataValue("model") == "0106-G") {
		return 6
	} else if (device.getDataValue("model") == "HY0048") {
		return 4
	} else if (device.getDataValue("model") == "cef8701bb8664a67a83033c071ef05f2") {
		return 3
	}
}

