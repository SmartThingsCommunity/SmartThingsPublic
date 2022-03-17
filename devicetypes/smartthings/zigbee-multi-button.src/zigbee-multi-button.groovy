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
 *	Author: SRPOL
 *	Date: 2019-02-18
 */

import groovy.json.JsonOutput
import physicalgraph.zigbee.zcl.DataType

metadata {
	definition (name: "Zigbee Multi Button", namespace: "smartthings", author: "SmartThings", mcdSync: true, ocfDeviceType: "x.com.st.d.remotecontroller") {
		capability "Actuator"
		capability "Battery"
		capability "Button"
		capability "Holdable Button"
		capability "Configuration"
		capability "Refresh"
		capability "Sensor"
		capability "Health Check"

		fingerprint inClusters: "0000, 0001, 0003, 0007, 0020, 0B05", outClusters: "0003, 0006, 0019", manufacturer: "CentraLite", model:"3450-L", deviceJoinName: "Iris Remote Control", mnmn: "SmartThings", vid: "generic-4-button" //Iris KeyFob
		fingerprint inClusters: "0000, 0001, 0003, 0007, 0020, 0B05", outClusters: "0003, 0006, 0019", manufacturer: "CentraLite", model:"3450-L2", deviceJoinName: "Iris Remote Control", mnmn: "SmartThings", vid: "generic-4-button" //Iris KeyFob
		fingerprint profileId: "0104", inClusters: "0004", outClusters: "0000, 0001, 0003, 0004, 0005, 0B05", manufacturer: "HEIMAN", model: "SceneSwitch-EM-3.0", deviceJoinName: "HEIMAN Remote Control", vid: "generic-4-button" //HEIMAN Scene Keypad

		//AduroSmart
		fingerprint inClusters: "0000, 0003, 0004, 0005, 0006, 0008, 0300, FCCC, 1000", outClusters: "0000, 0003, 0004, 0005, 0006, 0008, 0300, FCCC, 1000", manufacturer: "AduroSmart Eria", model: "ADUROLIGHT_CSC", deviceJoinName: "Eria Remote Control", mnmn: "SmartThings", vid: "generic-4-button" //Eria scene button switch V2.1
		fingerprint inClusters: "0000, 0003, 0004, 0005, 0006, 0008, 0300, FCCC, 1000", outClusters: "0000, 0003, 0004, 0005, 0006, 0008, 0300, FCCC, 1000", manufacturer: "ADUROLIGHT", model: "ADUROLIGHT_CSC", deviceJoinName: "Eria Remote Control", mnmn: "SmartThings", vid: "generic-4-button" //Eria scene button switch V2.0
		fingerprint inClusters: "0000, 0003, 0008, FCCC, 1000", outClusters: "0003, 0004, 0006, 0008, FCCC, 1000", manufacturer: "AduroSmart Eria", model: "Adurolight_NCC", deviceJoinName: "Eria Remote Control", mnmn: "SmartThings", vid: "generic-4-button" //Eria dimming button switch V2.1
		fingerprint inClusters: "0000, 0003, 0008, FCCC, 1000", outClusters: "0003, 0004, 0006, 0008, FCCC, 1000", manufacturer: "ADUROLIGHT", model: "Adurolight_NCC", deviceJoinName: "Eria Remote Control", mnmn: "SmartThings", vid: "generic-4-button" //Eria dimming button switch V2.0
		fingerprint inClusters: "0000,0001,0003,0020", outClusters: "0003,0004,0006,0019", manufacturer: "ShinaSystem", model: "MSM-300Z", deviceJoinName: "SiHAS Remote Control", mnmn: "SmartThingsCommunity", vid: "b18d7e4e-3775-3606-85a6-14b63cd8a0e3"
		fingerprint inClusters: "0000,0001,0003,0020", outClusters: "0003,0004,0006,0019", manufacturer: "ShinaSystem", model: "BSM-300Z", deviceJoinName: "SiHAS Remote Control", mnmn: "SmartThingsCommunity", vid: "0b6ace5f-e2d8-3e34-9b2a-5662bc9e20e1"
		fingerprint inClusters: "0000,0001,0003,0020", outClusters: "0003,0004,0006,0019", manufacturer: "ShinaSystem", model: "SBM300ZB1", deviceJoinName: "SiHAS Remote Control", mnmn: "SmartThingsCommunity", vid: "0b6ace5f-e2d8-3e34-9b2a-5662bc9e20e1"
		fingerprint inClusters: "0000,0001,0003,0020", outClusters: "0003,0004,0006,0019", manufacturer: "ShinaSystem", model: "SBM300ZB2", deviceJoinName: "SiHAS Remote Control", mnmn: "SmartThingsCommunity", vid: "57bb4dc5-40ef-335f-8e60-cc63190cc73b"
		fingerprint inClusters: "0000,0001,0003,0020", outClusters: "0003,0004,0006,0019", manufacturer: "ShinaSystem", model: "SBM300ZB3", deviceJoinName: "SiHAS Remote Control", mnmn: "SmartThingsCommunity", vid: "f3f3ab0e-82f5-36dd-839f-a048e1a3f8f9"
	}

	tiles {
		standardTile("button", "device.button", width: 2, height: 2) {
			state "default", label: "", icon: "st.unknown.zwave.remote-controller", backgroundColor: "#ffffff"
			state "button 1 pushed", label: "pushed #1", icon: "st.unknown.zwave.remote-controller", backgroundColor: "#00A0DC"
		}

		valueTile("battery", "device.battery", decoration: "flat", inactiveLabel: false) {
			state "battery", label:'${currentValue}% battery', unit:""
		}

		standardTile("refresh", "device.refresh", inactiveLabel: false, decoration: "flat") {
			state "default", action:"refresh.refresh", icon:"st.secondary.refresh"
		}
		main (["button"])
		details(["button", "battery", "refresh"])
	}
}

def parse(String description) {
	def map = zigbee.getEvent(description)
	def result = map ? map : parseAttrMessage(description)
	if (result.name == "switch") {
		result = createEvent(descriptionText: "Wake up event came in", isStateChange: true)
	}
	log.debug "Description ${description} parsed to ${result}"
	return result
}

def parseAttrMessage(description) {
	def descMap = zigbee.parseDescriptionAsMap(description)
	def map = [:]
	if (descMap?.clusterInt == zigbee.POWER_CONFIGURATION_CLUSTER && descMap.commandInt != 0x07 && descMap?.value) {
		map = getBatteryPercentageResult(Integer.parseInt(descMap.value, 16))
	} else if (isAduroSmartRemote()) {
		map = parseAduroSmartButtonMessage(descMap)
    	} else if (descMap?.clusterInt == zigbee.ONOFF_CLUSTER && descMap.isClusterSpecific) {
		map = getButtonEvent(descMap)
	} else if (descMap?.clusterInt == 0x0005) {
		def buttonNumber
		buttonNumber = buttonMap[device.getDataValue("model")][descMap.data[2]]
       
		log.debug "Number is ${buttonNumber}"
		def descriptionText = getButtonName() + " ${buttonNumber} was pushed"
		sendEventToChild(buttonNumber, createEvent(name: "button", value: "pushed", data: [buttonNumber: buttonNumber], descriptionText: descriptionText, isStateChange: true))
		map = createEvent(name: "button", value: "pushed", data: [buttonNumber: buttonNumber], descriptionText: descriptionText, isStateChange: true)
   	}
	map
}

def getButtonEvent(descMap) {
	if (descMap.commandInt == 1) {
		if (isShinaButton()) {
			def button = descMap.sourceEndpoint.toInteger()
			getButtonResult("double", button)
		} else {
			getButtonResult("press")
		}	
	} else if (descMap.commandInt == 0) {
		if (isShinaButton()) {
			def button = descMap.sourceEndpoint.toInteger()
			getButtonResult("pushed", button)
		} else {
			def button = buttonMap[device.getDataValue("model")][descMap.sourceEndpoint]
			getButtonResult("release", button)
		}
	} else if (descMap.commandInt == 2) {
		def button = descMap.sourceEndpoint.toInteger()
		getButtonResult("held", button)
	}
}

def getButtonResult(buttonState, buttonNumber = 1) {
	def event = [:]
	if (buttonState == 'release') {
		def timeDiff = now() - state.pressTime
		if (timeDiff > 10000) {
			return event
		} else {
			buttonState = timeDiff < holdTime ? "pushed" : "held"
			def descriptionText = getButtonName() + " ${buttonNumber} was ${buttonState}"
			event = createEvent(name: "button", value: buttonState, data: [buttonNumber: buttonNumber], descriptionText: descriptionText, isStateChange: true)
			sendEventToChild(buttonNumber, event)
			return createEvent(descriptionText: descriptionText)
		}
	} else if (buttonState == 'press') {
		state.pressTime = now()
		return event
	} else if ((buttonState == 'double') || (buttonState == 'pushed') || (buttonState == 'held')) {
		def descriptionText = getButtonName() + " ${buttonNumber} was ${buttonState}"
		event = createEvent(name: "button", value: buttonState, data: [buttonNumber: buttonNumber], descriptionText: descriptionText, isStateChange: true)
		sendEventToChild(buttonNumber, event)
		return createEvent(descriptionText: descriptionText)
	} 
}

def sendEventToChild(buttonNumber, event) {
	String childDni = "${device.deviceNetworkId}:$buttonNumber"
	def child = childDevices.find { it.deviceNetworkId == childDni }
	child?.sendEvent(event)	
}

def getBatteryPercentageResult(rawValue) {
	log.debug 'Battery'
	def volts = rawValue / 10
	if (volts > 3.0 || volts == 0 || rawValue == 0xFF) {
		[:]
	} else {
		def result = [
				name: 'battery'
		]
		def minVolts = 2.1
		def maxVolts = 3.0
		def pct = (volts - minVolts) / (maxVolts - minVolts)
		result.value = Math.min(100, (int)(pct * 100))
		def linkText = getLinkText(device)
		result.descriptionText = "${linkText} battery was ${result.value}%"
		createEvent(result)
	}
}

def refresh() {
	return zigbee.readAttribute(zigbee.POWER_CONFIGURATION_CLUSTER, batteryVoltage) +
			zigbee.readAttribute(zigbee.ONOFF_CLUSTER, switchType)
			zigbee.enrollResponse()
}

def ping() {
	refresh()
}

def configure() {
	def bindings = getModelBindings(device.getDataValue("model"))
	def cmds = zigbee.onOffConfig() +
			zigbee.configureReporting(zigbee.POWER_CONFIGURATION_CLUSTER, batteryVoltage, DataType.UINT8, 30, 21600, 0x01) +
			zigbee.enrollResponse() +
			zigbee.readAttribute(zigbee.POWER_CONFIGURATION_CLUSTER, batteryVoltage) + bindings
	if (isHeimanButton())
		cmds += zigbee.writeAttribute(0x0000, 0x0012, DataType.BOOLEAN, 0x01) +
		addHubToGroup(0x000F) + addHubToGroup(0x0010) + addHubToGroup(0x0011) + addHubToGroup(0x0012)
	return cmds
}

def installed() {
	sendEvent(name: "button", value: "pushed", isStateChange: true, displayed: false)
	sendEvent(name: "supportedButtonValues", value: supportedButtonValues.encodeAsJSON(), displayed: false)
	
	initialize()
}

def updated() {
	runIn(2, "initialize", [overwrite: true])
}

def initialize() {
	def numberOfButtons = modelNumberOfButtons[device.getDataValue("model")]
	sendEvent(name: "numberOfButtons", value: numberOfButtons, displayed: false)
	sendEvent(name: "checkInterval", value: 2 * 60 * 60 + 2 * 60, displayed: false, data: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID])
    
	if(!childDevices) {
		addChildButtons(numberOfButtons)
	}
	if(childDevices) {
		def event
		for(def endpoint : 1..device.currentValue("numberOfButtons")) {
			event = createEvent(name: "button", value: "pushed", isStateChange: true)
			sendEventToChild(endpoint, event)
		}
	}
}

private addChildButtons(numberOfButtons) {
	for(def endpoint : 1..numberOfButtons) {
		try {
			String childDni = "${device.deviceNetworkId}:$endpoint"
			def componentLabel = getButtonName() + "${endpoint}"

			if (isAduroSmartRemote()) {
				componentLabel = device.displayName + " - ${endpoint}"
			}
			def child = addChildDevice("Child Button", childDni, device.getHub().getId(), [
					completedSetup: true,
					label         : componentLabel,
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

private getBatteryVoltage() { 0x0020 }
private getSwitchType() { 0x0000 }
private getHoldTime() { 1000 }
private getButtonMap() {[
		"3450-L" : [
				"01" : 4,
				"02" : 3,
				"03" : 1,
				"04" : 2
		],
		"3450-L2" : [
				"01" : 4,
				"02" : 3,
				"03" : 1,
				"04" : 2
		],
		"SceneSwitch-EM-3.0" : [
				"01" : 1,
				"02" : 2,
				"03" : 3,
				"04" : 4
		]
]}

private getSupportedButtonValues() {
	def values
	if (device.getDataValue("model") == "SceneSwitch-EM-3.0") {
		values = ["pushed"]
	} else if (isAduroSmartRemote()) {
		values = ["pushed"]
	} else if (isShinaButton()) {
		values = ["pushed","held","double"]
	} else {
		values = ["pushed", "held"]
	}
	return values
}

private getModelNumberOfButtons() {[
		"3450-L" : 4,
		"3450-L2" : 4,
		"SceneSwitch-EM-3.0" : 4, 
		"ADUROLIGHT_CSC" : 4,
		"Adurolight_NCC" : 4,
		"BSM-300Z" : 1,
		"MSM-300Z" : 4,
		"SBM300ZB1" : 1,
		"SBM300ZB2" : 2,
		"SBM300ZB3" : 3
]}

private getModelBindings(model) {
	def bindings = []
	for(def endpoint : 1..modelNumberOfButtons[model]) {
		bindings += zigbee.addBinding(zigbee.ONOFF_CLUSTER, ["destEndpoint" : endpoint])
	}
	if (isAduroSmartRemote()) {
		bindings += zigbee.addBinding(zigbee.LEVEL_CONTROL_CLUSTER, ["destEndpoint" : 2]) + 
			zigbee.addBinding(zigbee.LEVEL_CONTROL_CLUSTER, ["destEndpoint" : 3])
	}
	bindings
}

private getButtonName() {
	def values = device.displayName.endsWith(' 1') ? "${device.displayName[0..-2]}" : "${device.displayName}"
	return values
}

private Map parseAduroSmartButtonMessage(Map descMap){
	def buttonState = "pushed"
	def buttonNumber = 0
	if (descMap.clusterInt == zigbee.ONOFF_CLUSTER) {
		if (descMap.command == "01") {
		    buttonNumber = 1
		} else if (descMap.command == "00") {
		    buttonNumber = 4
		}
	} else if (descMap.clusterInt == ADUROSMART_SPECIFIC_CLUSTER) {
		def list2 = descMap.data
		buttonNumber = (list2[1] as int) + 1
	}
	if (buttonNumber != 0) {
		def childevent = createEvent(name: "button", value: "pushed", data: [buttonNumber: 1], isStateChange: true)
		sendEventToChild(buttonNumber, childevent)
		def descriptionText = "$device.displayName button $buttonNumber was $buttonState"
		return createEvent(name: "button", value: buttonState, data: [buttonNumber: buttonNumber], descriptionText: descriptionText, isStateChange: true)
        } else {
		return [:]
	}
}

def isAduroSmartRemote(){
	((device.getDataValue("model") == "Adurolight_NCC") || (device.getDataValue("model") == "ADUROLIGHT_CSC"))
}

def getADUROSMART_SPECIFIC_CLUSTER() {0xFCCC}

private getCLUSTER_GROUPS() { 0x0004 }

private List addHubToGroup(Integer groupAddr) {
	["st cmd 0x0000 0x01 ${CLUSTER_GROUPS} 0x00 {${zigbee.swapEndianHex(zigbee.convertToHexString(groupAddr,4))} 00}",
	 "delay 200"]
}

def isHeimanButton(){
	device.getDataValue("model") == "SceneSwitch-EM-3.0"
}

private Boolean isShinaButton() {
	((device.getDataValue("model") == "BSM-300Z") || (device.getDataValue("model") == "MSM-300Z") || (device.getDataValue("model") == "SBM300ZB1") || (device.getDataValue("model") == "SBM300ZB2") || (device.getDataValue("model") == "SBM300ZB3"))	
}