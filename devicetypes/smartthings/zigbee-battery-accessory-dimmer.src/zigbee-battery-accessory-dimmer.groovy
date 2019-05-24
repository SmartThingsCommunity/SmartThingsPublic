/**
 *	Copyright 2019 SmartThings
 *
 *	Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *	in compliance with the License. You may obtain a copy of the License at:
 *
 *		http://www.apache.org/licenses/LICENSE-2.0
 *
 *	Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *	on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *	for the specific language governing permissions and limitations under the License.
 *
 */

import physicalgraph.zigbee.zcl.DataType

metadata {
	definition (name: "ZigBee Battery Accessory Dimmer", namespace: "smartthings", author: "SmartThings", ocfDeviceType: "x.com.st.d.remotecontroller") {
		capability "Actuator"
		capability "Battery"
		capability "Configuration"
		capability "Health Check"
		capability "Switch"
		capability "Switch Level"
		
		fingerprint profileId: "0104", inClusters: "0000,0001,0003,0020,FC11", outClusters: "0003,0004,0006,0008,FC10", manufacturer: "sengled", model: "E1E-G7F", deviceJoinName: "Sengled Smart Switch"
	}

	tiles(scale: 2) {
		multiAttributeTile(name:"switch", type: "lighting", width: 6, height: 4, canChangeIcon: true){
			tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
				attributeState "on", label:'${name}', action:"switch.off", icon:"st.switches.light.on", backgroundColor:"#00A0DC", nextState:"turningOff"
				attributeState "off", label:'${name}', action:"switch.on", icon:"st.switches.light.off", backgroundColor:"#ffffff", nextState:"turningOn"
				attributeState "turningOn", label:'${name}', action:"switch.off", icon:"st.switches.light.on", backgroundColor:"#00A0DC", nextState:"turningOff"
				attributeState "turningOff", label:'${name}', action:"switch.on", icon:"st.switches.light.off", backgroundColor:"#ffffff", nextState:"turningOn"
			}
			tileAttribute ("device.level", key: "SLIDER_CONTROL") {
				attributeState "level", action:"switch level.setLevel"
			}
			tileAttribute ("device.battery", key: "SECONDARY_CONTROL") {
				attributeState "battery", label: 'battery ${currentValue}%', unit: "%"
			}
		}
		main "switch"
		details(["switch"])
	}
}

def getDOUBLE_STEP() {10}
def getSTEP() {5}

def parse(String description) {

	def descMap = zigbee.parseDescriptionAsMap(description)
	if (isSengledSwitch()) {
		handleSengledSwitchEvents(descMap)
	}
	handleBatteryEvents(descMap)
}

private boolean isSengledSwitch() {
	device.getDataValue("model") == "E1E-G7F"
}

def handleSengledSwitchEvents(descMap) {
	if (descMap?.clusterInt == 0xFC10) {
		def currentLevel = device.currentValue("level") as Integer ?: 0
		def value = 0

		switch(descMap.data[0]) {
			case '01':
				//short press of 'ON' button
				sendEvent(name: "switch", value: "on")
				break
			case '02':
				// move up
				if (descMap.data[2] == '02') {
					//long press of 'BRIGHTEN' button
					value = Math.min(currentLevel + DOUBLE_STEP, 100)
				} else if (descMap.data[2] == '01') {
					//short press of 'BRIGHTEN' button
					value = Math.min(currentLevel + STEP, 100)
				}
				sendEvent(name: "switch", value: "on")
				sendEvent(name: "level", value: value)
				break
			case '03':
				//move down
				if (descMap.data[2] == '02') {
					//long press of 'DIM' button
					value = Math.max(currentLevel - DOUBLE_STEP, 0)
				} else if (descMap.data[2] == '01') {
					//short press of 'DIM' button
					value = Math.max(currentLevel - STEP, 0)
				}

				if (value == 0) {
					sendEvent(name: "switch", value: "off")
				} else {
					sendEvent(name: "level", value: value)
				}
				break
			case '04':
				//short press of 'OFF' button
				sendEvent(name: "switch", value: "off")
				break
			case '06':
				//long press of 'ON' button
				sendEvent(name: "switch", value: "on")
				break
			case '08':
				//long press of 'OFF' button
				sendEvent(name: "switch", value: "off")
				break
			default:
				break
		}
	}
}

def handleBatteryEvents(descMap) {
	if (descMap?.clusterInt == zigbee.POWER_CONFIGURATION_CLUSTER && descMap?.value) {
		def batteryValue = zigbee.convertHexToInt(descMap.value) / 2
		sendEvent(name: "battery", value: batteryValue)
	}
}

def off() {
	sendEvent(name: "switch", value: "off", isStateChange: true)
}

def on() {
	sendEvent(name: "switch", value: "on", isStateChange: true)
}

def setLevel(value, rate = null) {
	if (value == 0) {
		sendEvent(name: "switch", value: "off")
	} else {
		sendEvent(name: "switch", value: "on")
		sendEvent(name: "level", value: value)
	}
}

def ping() {
	zigbee.readAttribute(zigbee.POWER_CONFIGURATION_CLUSTER, 0x0021)
}

def installed() {
	sendEvent(name: "switch", value: "on", displayed: false)
	sendEvent(name: "level", value: 100, displayed: false)
}

def configure() {
	sendEvent(name: "checkInterval", value: 2 * 60 * 60 + 10 * 60, displayed: false, data: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID, offlinePingable: "1"])
	zigbee.readAttribute(zigbee.POWER_CONFIGURATION_CLUSTER, 0x0021) + zigbee.configureReporting(zigbee.POWER_CONFIGURATION_CLUSTER, 0x0021, DataType.UINT8, 30, 10 * 60, null)
}

