/**
 *  Copyright 2018 SmartThings
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
 */

import physicalgraph.zigbee.zcl.DataType

metadata {
	definition (name: "ZigBee Accessory Dimmer", namespace: "smartthings", author: "SmartThings", ocfDeviceType: "x.com.st.d.remotecontroller") {
		capability "Actuator"
		capability "Switch"
		capability "Button"
		capability "Switch Level"
		capability "Configuration"

		fingerprint profileId: "0104", inClusters: "0000,1000,0003", outClusters: "0003,0004,0005,0006,0008,1000,0019", manufacturer: "Aurora", model: "Remote50AU", deviceJoinName: "Aurora Wireless Wall Remote"
		fingerprint endpointId: "01", profileId: "0104", deviceId: "0810", inClusters: "0000, 0001, 0003, 0009, 0B05, 1000", outClusters: "0003, 0004, 0006, 0008, 0019, 1000", manufacturer: "IKEA of Sweden", model: "TRADFRI wireless dimmer", deviceJoinName: "IKEA TRÅDFRI Wireless dimmer"
		fingerprint endpointId: "01", profileId: "C05E", deviceId: "0810", inClusters: "0000, 0001, 0003, 0009, 0B05, 1000", outClusters: "0003, 0004, 0006, 0008, 0019, 1000", manufacturer: "IKEA of Sweden", model: "TRADFRI wireless dimmer", deviceJoinName: "IKEA TRÅDFRI Wireless dimmer"
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
		}
		main "switch"
		details(["switch"])
	}
}

def getSTEP() {10}

def getONOFF_ON_COMMAND() { 0x01 }
def getONOFF_OFF_COMMAND() { 0x00 }
def getLEVEL_MOVE_LEVEL_COMMAND() { 0x00 }
def getLEVEL_MOVE_COMMAND() { 0x01 }
def getLEVEL_STEP_COMMAND() { 0x02 }
def getLEVEL_STOP_COMMAND() { 0x03 }
def getLEVEL_MOVE_LEVEL_ONOFF_COMMAND() { 0x04 }
def getLEVEL_MOVE_ONOFF_COMMAND() { 0x05 }
def getLEVEL_STEP_ONOFF_COMMAND() { 0x06 }

def isIkeaDimmer() {
	device.getDataValue("model") == "TRADFRI wireless dimmer"
}

// Parse incoming device messages to generate events
def parse(String description) {
	log.debug "description is $description"

	def event = zigbee.getEvent(description)
	if (event) {
		if (event.name=="level" && event.value==0) {}
		else {
			sendEvent(event)
		}
	} else {
		def descMap = zigbee.parseDescriptionAsMap(description)
		if (descMap && descMap.clusterInt == zigbee.ONOFF_CLUSTER) {
			if (descMap.commandInt == ONOFF_ON_COMMAND || descMap.commandInt == ONOFF_OFF_COMMAND) {
				if (device.currentValue("level") == 0) {
					sendEvent(name: "level", value: STEP)
				}

				if (isIkeaDimmer()) {
					sendEvent(name: "switch", value: descMap.commandInt == ONOFF_OFF_COMMAND ? "off" : "on")
				} else {
					sendEvent(name: "switch", value: device.currentValue("switch") == "on" ? "off" : "on")
				}
			}
		} else if (descMap && descMap.clusterInt == zigbee.LEVEL_CONTROL_CLUSTER) {
			def currentLevel = device.currentValue("level") as Integer ?: 0

			if (descMap.commandInt == LEVEL_STEP_COMMAND) {
				def value = Math.min(currentLevel + STEP, 100)

				log.debug "move to ${descMap.data}"

				if (descMap.data[0] == "00") {
					log.debug "move up"

					sendEvent(name: "switch", value: "on")
					sendEvent(name: "level", value: value)
				} else if (descMap.data[0] == "01") {
					log.debug "move down"

					value = Math.max(currentLevel - STEP, 0)

					// don't change level if switch will be turning off
					if (value == 0) {
						sendEvent(name: "switch", value: "off")
					} else {
						sendEvent(name: "level", value: value)
					}
				}
			} else if (descMap.commandInt == LEVEL_MOVE_COMMAND) {
				sendEvent(name: "level", value: descMap.data[0] == "00" ? 100 : STEP)
				sendEvent(name: "switch", value: "on" )

				log.debug "step to ${descMap.data}"
			} else if (descMap.commandInt == LEVEL_STOP_COMMAND) {
				log.debug "stop move"
			} else if (descMap.commandInt == LEVEL_MOVE_LEVEL_ONOFF_COMMAND) {
				if (descMap.data[0] == "00") {
					sendEvent(name: "switch", value: "off", isStateChange: true)
				} else if (descMap.data[0] == "FF") {
					sendEvent(name: "switch", value: "on", isStateChange: true)
				}
            }
		} else if (descMap && descMap.clusterInt == 0x0005) {
			if (descMap.commandInt == 0x05) {
				sendEvent(name: "button", value: "pushed", data: [buttonNumber: 1], isStateChange: true)
			} else if (descMap.commandInt == 0x04) {
				sendEvent(name: "button", value: "held", data: [buttonNumber: 1], isStateChange: true)
			}
		} else {
			log.warn "DID NOT PARSE MESSAGE for description : $description"
			log.debug "${descMap}"
		}
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

def installed() {
	sendEvent(name: "switch", value: "on", displayed: false)
	sendEvent(name: "level", value: 100, displayed: false)
	sendEvent(name: "button", value: "pushed", data: [buttonNumber: 1], displayed: false)
	sendEvent(name: "numberOfButtons", value: 1, displayed: false)
}

def configure() {
	//these are necessary to have the device report when its buttons are pushed
	zigbee.addBinding(zigbee.ONOFF_CLUSTER) + zigbee.addBinding(zigbee.LEVEL_CONTROL_CLUSTER) + zigbee.addBinding(0x0005)
}
