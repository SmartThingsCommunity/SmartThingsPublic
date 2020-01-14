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

		fingerprint inClusters: "0000, 0001, 0003, 0007, 0020, 0B05", outClusters: "0003, 0006, 0019", manufacturer: "CentraLite", model:"3450-L", deviceJoinName: "Iris KeyFob", mnmn: "SmartThings", vid: "generic-4-button"
		fingerprint inClusters: "0000, 0001, 0003, 0007, 0020, 0B05", outClusters: "0003, 0006, 0019", manufacturer: "CentraLite", model:"3450-L2", deviceJoinName: "Iris KeyFob", mnmn: "SmartThings", vid: "generic-4-button"
		fingerprint profileId: "0104", inClusters: "0004", outClusters: "0000, 0001, 0003, 0004, 0005, 0B05", manufacturer: "HEIMAN", model: "SceneSwitch-EM-3.0", deviceJoinName: "HEIMAN Scene Keypad", vid: "generic-4-button"
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
	} else if (descMap?.clusterInt == zigbee.ONOFF_CLUSTER && descMap.isClusterSpecific) {
		map = getButtonEvent(descMap)
	} else if(descMap?.clusterInt == 0xFC80) {
		def buttonNumber
		buttonNumber = Integer.valueOf(descMap?.command[1].toInteger()) + 1
       
		log.debug "Number is ${buttonNumber}"
		def event = createEvent(name: "button", value: "pushed", data: [buttonNumber: buttonNumber], descriptionText: "pushed", isStateChange: true)
		if (buttonNumber != 1) {
			sendEventToChild(buttonNumber, event)
		} else {
			sendEvent(event)
		}
   	}
	map
}

def getButtonEvent(descMap) {
	if (descMap.commandInt == 1) {
		getButtonResult("press")
	}
	else if (descMap.commandInt == 0) {
		def button = buttonMap[device.getDataValue("model")][descMap.sourceEndpoint]
		getButtonResult("release", button)
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
			def descriptionText = (device.displayName.endsWith(' 1') ? "${device.displayName[0..-2]} button" : "${device.displayName}") + " ${buttonNumber} was ${buttonState}"
			event = createEvent(name: "button", value: buttonState, data: [buttonNumber: buttonNumber], descriptionText: descriptionText, isStateChange: true)
			sendEventToChild(buttonNumber, event)
			return createEvent(descriptionText: descriptionText)
		}
	} else if (buttonState == 'press') {
		state.pressTime = now()
		return event
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
	return zigbee.onOffConfig() +
			zigbee.configureReporting(zigbee.POWER_CONFIGURATION_CLUSTER, batteryVoltage, DataType.UINT8, 30, 21600, 0x01) +
			zigbee.enrollResponse() +
			zigbee.readAttribute(zigbee.POWER_CONFIGURATION_CLUSTER, batteryVoltage) + bindings
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
			def componentLabel = (device.displayName.endsWith(' 1') ? device.displayName[0..-2] : device.displayName) + "${endpoint}"
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
		]
]}

private getSupportedButtonValues() {
	def values
	if (device.getDataValue("model") == "SceneSwitch-EM-3.0") {
		values = ["pushed"]
	} else {
		values = ["pushed", "held"]
	}
	return values
}

private getModelNumberOfButtons() {[
		"3450-L" : 4,
		"3450-L2" : 4,
		"SceneSwitch-EM-3.0" : 4
]}

private getModelBindings(model) {
	def bindings = []
	for(def endpoint : 1..modelNumberOfButtons[model]) {
		bindings += zigbee.addBinding(zigbee.ONOFF_CLUSTER, ["destEndpoint" : endpoint])
	}
	bindings
}
