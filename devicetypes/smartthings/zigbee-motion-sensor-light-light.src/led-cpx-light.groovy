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
 *  LED CPX light
 *
 *  Author: SAMSUMG LED
 *  Date: 2021-12-08
 */

import physicalgraph.zigbee.zcl.DataType
import groovy.json.JsonOutput

metadata {
	definition(name: "LED CPX light", namespace: "SAMSUNG LED", author: "SAMSUNG LED", runLocally: true, minHubCoreVersion: '000.019.00012', executeCommandsLocally: true, genericHandler: "Zigbee") {
		capability "Actuator"
		capability "Color Temperature"
		capability "Configuration"
		capability "Health Check"
		capability "Refresh"
		capability "Switch"
		capability "Switch Level"
		capability "Light"
		
		attribute "colorName", "string"
		
		// Samsung LED
		fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0008, 0300, 0406", outClusters: "0019", manufacturer: "Juno", model: "ABL-LIGHTSENSOR-Z-001", deviceJoinName: "ABL CPX Light"
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
		details(["switch", "refresh", "switchLevel", "colorTempSliderControl", "colorName"]) //
	}
}

// Parse incoming device messages to generate events
def parse(String description) {
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
	} else if (zigbeeMap.cluster == "0406" && zigbeeMap.attrId == "0000") {
		def childDevice = getChildDevices()?.find {
			it.device.deviceNetworkId == "${device.deviceNetworkId}:1"
		}
		zigbeeMap.value = zigbeeMap.value.endsWith("01") ? "active" : "inactive"
		zigbeeMap.name = "motion"
		childDevice.sendEvent(zigbeeMap)
	} else {
		def cluster = zigbee.parse(description)
		if (cluster && cluster.clusterId == 0x0006 && cluster.command == 0x07) {
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

def ping() {
	return zigbee.onOffRefresh()
}

def refresh() {
	zigbee.configureReporting(0x406, 0x0000, 0x18, 30, 600, null) +
		zigbee.onOffRefresh() +
		zigbee.levelRefresh() +
		zigbee.colorTemperatureRefresh() +
		zigbee.onOffConfig(0, 300) +
		zigbee.levelConfig()
}

def configure() {
	def cmds = delayBetween([
		sendEvent(name: "checkInterval", value: 2 * 10 * 60 + 1 * 60, displayed: false, data: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID]),
		zigbee.configureReporting(0x406, 0x0000, 0x18, 30, 600, null),
		zigbee.onOffRefresh(),
		zigbee.levelRefresh(),
		zigbee.colorTemperatureRefresh(),
		zigbee.onOffConfig(0, 300),
		zigbee.levelConfig()
	])
	return cmds
}

def setColorTemperature(value) {
	value = value as Integer
	def tempInMired = Math.round(1000000 / value)
	def finalHex = zigbee.swapEndianHex(zigbee.convertToHexString(tempInMired, 4))
	
	List cmds = []  
	cmds << zigbee.command(COLOR_CONTROL_CLUSTER, MOVE_TO_COLOR_TEMPERATURE_COMMAND, "$finalHex 0000")
	cmds << zigbee.readAttribute(COLOR_CONTROL_CLUSTER, ATTRIBUTE_COLOR_TEMPERATURE)
	cmds
}

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
	addChildSensor()
	return
}

def addChildSensor(){
	def componentLabel
	if (device.displayName.endsWith(' Light') || device.displayName.endsWith(' light')) {
		componentLabel = "${device.displayName[0..-6]} Motion sensor"
	} else {
		componentLabel = "$device.displayName Motion sensor"
	} try {
		String dni = "${device.deviceNetworkId}:1"
		addChildDevice("ITM CPX Motion sensor child", dni, device.hub.id, [completedSetup: true, label: "${componentLabel}", isComponent: false])
	} catch (e) {
		log.warn "Failed to add ITM Motion sensor Controller - $e"
	}
	
	def childDevice = getChildDevices()?.find {
		it.device.deviceNetworkId == "${device.deviceNetworkId}:1"
	}
	
	if(childDevice != null) {
		childDevice.sendEvent(name: "motion", value: "inactive")
	}
}
