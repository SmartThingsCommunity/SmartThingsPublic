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
	definition (name: "Zigbee Scene Keypad", namespace: "smartthings", author: "SmartThings", mcdSync: true, ocfDeviceType: "x.com.st.d.remotecontroller") {
		capability "Actuator"
		capability "Button"
		capability "Configuration"
		capability "Refresh"
		capability "Sensor"
		capability "Health Check"

                fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005", outClusters: "0003, 0004, 0005", manufacturer: "REXENSE", model: "HY0048", deviceJoinName: "GDKES Remote Control", vid: "generic-4-button-alt" //GDKES Scene Keypad
                fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005", outClusters: "0003, 0004, 0005", manufacturer: "REXENSE", model: "0106-G", deviceJoinName: "GDKES Remote Control", vid: "generic-6-button-alt" //GDKES Scene Keypad
                fingerprint profileId: "0104", inClusters: "0000, 0005", outClusters: "0000, 0005, 0017", manufacturer: "ORVIBO", model: "cef8701bb8664a67a83033c071ef05f2", deviceJoinName: "ORVIBO Remote Control", vid: "generic-3-button-alt" //ORVIBO Scene Keypad
                fingerprint profileId: "0104", inClusters: "0004", outClusters: "0000, 0001, 0003, 0004, 0005, 0B05", manufacturer: "HEIMAN", model: "E-SceneSwitch-EM-3.0", deviceJoinName: "HEIMAN Remote Control", vid: "generic-4-button-alt" //HEIMAN Scene Keypad

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
	if (descMap?.clusterInt == 0x0017 || descMap?.clusterInt == 0xFE05 || descMap?.clusterInt == 0x0005) {
	        def event = [:]
                def buttonNumber
                if (descMap?.clusterInt == 0x0017) {
				buttonNumber = Integer.valueOf(descMap.data[0])
                } else if (descMap?.clusterInt == 0xFE05) {
				buttonNumber = Integer.valueOf(descMap?.value)
                } else if(descMap?.clusterInt == 0x0005) {
				buttonNumber = buttonNum[device.getDataValue("model")][descMap.data[2]]
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
	def cmds = zigbee.enrollResponse()
	if (isHeimanButton())
		cmds += zigbee.writeAttribute(0x0000, 0x0012, DataType.BOOLEAN, 0x01) +
		addHubToGroup(0x000F) + addHubToGroup(0x0010) + addHubToGroup(0x0011) + addHubToGroup(0x0013)
	return cmds
}

def installed() {
    	def numberOfButtons = getChildCount()
	sendEvent(name: "numberOfButtons", value: numberOfButtons, displayed: false)
	sendEvent(name: "checkInterval", value: 32 * 60, displayed: false, data: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID])
	if (!childDevices) {
		addChildButtons(numberOfButtons)
	}
	if (childDevices) {
		def event
		for (def endpoint : 1..device.currentValue("numberOfButtons")) {
			event = createEvent(name: "button", value: "pushed", isStateChange: true, displayed: false)
			sendEventToChild(endpoint, event)
		}
	}

	sendEvent(name: "button", value: "pushed", isStateChange: true, displayed: false)
	sendEvent(name: "supportedButtonValues", value: supportedButtonValues.encodeAsJSON(), displayed: false)
}

def updated() {
	runIn(2, "initialize", [overwrite: true])
}

def initialize() {
	
}

private addChildButtons(numberOfButtons) {
	for (def endpoint : 2..numberOfButtons) {
		try {
			String childDni = "${device.deviceNetworkId}:$endpoint"
			def childLabel = (device.displayName.endsWith(' 1') ? device.displayName[0..-2] : device.displayName) + "${endpoint}"
			def child = addChildDevice("Child Button", childDni, device.getHub().getId(), [
					completedSetup: true,
					label         : childLabel,
					isComponent   : true,
					componentName : "button$endpoint",
					componentLabel: "Button $endpoint"
			])
			child.sendEvent(name: "supportedButtonValues", value: supportedButtonValues.encodeAsJSON(), displayed: false)
		} catch(Exception e) {
			log.debug "Exception: ${e}"
		}
	}
}

private getSupportedButtonValues() {
	def values = ["pushed"]
	return values
}

private getChildCount() {
	if (device.getDataValue("model") == "0106-G") {
		return 6
	} else if (device.getDataValue("model") == "HY0048" || device.getDataValue("model") == "E-SceneSwitch-EM-3.0") {
		return 4
	} else if (device.getDataValue("model") == "cef8701bb8664a67a83033c071ef05f2") {
		return 3
	}
}

private getCLUSTER_GROUPS() { 0x0004 }

private boolean isHeimanButton() {
	device.getDataValue("model") == "E-SceneSwitch-EM-3.0"
}

private List addHubToGroup(Integer groupAddr) {
	["st cmd 0x0000 0x01 ${CLUSTER_GROUPS} 0x00 {${zigbee.swapEndianHex(zigbee.convertToHexString(groupAddr,4))} 00}",
	 "delay 200"]
}

private getButtonNum() {[
		"E-SceneSwitch-EM-3.0" : [
				"01" : 2,
				"02" : 1,
				"03" : 3,
				"05" : 4
		]
]}