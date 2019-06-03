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
		capability "Health Check"

		fingerprint profileId: "0104", inClusters: "0000,1000,0003", outClusters: "0003,0004,0005,0006,0008,1000,0019", manufacturer: "Aurora", model: "Remote50AU", deviceJoinName: "Aurora Wireless Wall Remote"
		fingerprint manufacturer: "IKEA of Sweden", model: "TRADFRI wireless dimmer", deviceJoinName: "IKEA TRÅDFRI Wireless dimmer" // 01 [0104 or C05E] 0810 02 06 0000 0001 0003 0009 0B05 1000 06 0003 0004 0006 0008 0019 1000
		fingerprint profileId: "0104", inClusters: "0000,1000,0003", outClusters: "0003,0004,0005,0006,0008,1000,0019", manufacturer: "LDS", model: "ZBT-DIMController-D0800", deviceJoinName: "Müller Licht Tint Mobile Switch"
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

def getSTEP() { 10 }

def getSCENES_CLUSTER() { 0x0005 }
def getSCENES_STORE_SCENE_COMMAND() { 0x04 }
def getSCENES_RECALL_SCENE_COMMAND() { 0x05 }

def getONOFF_ON_COMMAND() { 0x01 }
def getONOFF_OFF_COMMAND() { 0x00 }
def getLEVEL_MOVE_LEVEL_COMMAND() { 0x00 }
def getLEVEL_MOVE_COMMAND() { 0x01 }
def getLEVEL_STEP_COMMAND() { 0x02 }
def getLEVEL_STOP_COMMAND() { 0x03 }
def getLEVEL_MOVE_LEVEL_ONOFF_COMMAND() { 0x04 }
def getLEVEL_MOVE_ONOFF_COMMAND() { 0x05 }
def getLEVEL_STEP_ONOFF_COMMAND() { 0x06 }
def getLEVEL_STOP_ONOFF_COMMAND() { 0x07 }
def getLEVEL_DIRECTION_UP() { "00" }
def getLEVEL_DIRECTION_DOWN() { "01" }

def getUINT8_STR() { "20" }


def isIkeaDimmer() {
	device.getDataValue("model") == "TRADFRI wireless dimmer"
}
def isAuroraRemote() {
	device.getDataValue("model") == "Remote50AU"
}
def isMullerLichtTint() {
	device.getDataValue("model") == "ZBT-DIMController-D0800"
}

// Parse incoming device messages to generate events
def parse(String description) {
	log.debug "description is $description"
	def results = []

	def event = zigbee.getEvent(description)
	if (event) {
		if (event.name=="level" && event.value==0) {}
		else {
			results << createEvent(event)
		}
	} else {
		def descMap = zigbee.parseDescriptionAsMap(description)

		if (descMap.clusterInt == zigbee.ONOFF_CLUSTER) {
			results += handleSwitchEvent(descMap)
		} else if (descMap.clusterInt == zigbee.LEVEL_CONTROL_CLUSTER) {
			results += handleLevelEvent(descMap)
		} else if (descMap.clusterInt == SCENES_CLUSTER) {
			if (descMap.commandInt == SCENES_RECALL_SCENE_COMMAND) {
				results << createEvent(name: "button", value: "pushed", data: [buttonNumber: 1], isStateChange: true)
			} else if (descMap.commandInt == SCENES_STORE_SCENE_COMMAND) {
				results << createEvent(name: "button", value: "held", data: [buttonNumber: 1], isStateChange: true)
			}
		} else {
			log.warn "DID NOT PARSE MESSAGE for description : $description"
			log.debug "${descMap}"
		}
	}

	log.debug "parse returned $results"
	return results
}

def handleSwitchEvent(descMap) {
	def results = []

	if (descMap.commandInt == ONOFF_ON_COMMAND || descMap.commandInt == ONOFF_OFF_COMMAND) {
		if (device.currentValue("level") == 0) {
			results << createEvent(name: "level", value: STEP)
		}

		if (isAuroraRemote() || isMullerLichtTint()) { // Devices that have one power button
			results << createEvent(name: "switch", value: device.currentValue("switch") == "on" ? "off" : "on")
		} else {
			results << createEvent(name: "switch", value: descMap.commandInt == ONOFF_OFF_COMMAND ? "off" : "on")
		}
	}

	return results
}

def handleLevelEvent(descMap) {
	def results = []

	if (descMap.commandInt == LEVEL_STEP_COMMAND) {
		results += handleStepEvent(descMap)
	} else if (descMap.commandInt == LEVEL_MOVE_COMMAND || descMap.commandInt == LEVEL_MOVE_ONOFF_COMMAND) {
		// For the IKEA Dimmer we are going to treat Level Move and Level Move with On/Off as Level Step
		if (isIkeaDimmer()) {
			results += handleStepEvent(descMap)
		} else {
			def newLevel = descMap.data[0] == LEVEL_DIRECTION_UP ? 100 : STEP
			results << createEvent(name: "level", value: newLevel)
			results << createEvent(name: "switch", value: "on")

			log.debug "step $STEP to ${newLevel}"
		}
	} else if (descMap.commandInt == LEVEL_STOP_COMMAND || descMap.commandInt == LEVEL_STOP_ONOFF_COMMAND) {
		// We are not going to handle this event because we are not implementing this the way that the Zigbee spec indicates
		log.debug "Received stop move - not handling"
	} else if (descMap.commandInt == LEVEL_MOVE_LEVEL_ONOFF_COMMAND) {
		// The spec defines this as "Move to level with on/off". The IKEA Dimmer sends us 0x00 or 0xFF only, so we will treat this more as a
		// on/off command for the dimmer. Otherwise, we will treat this as off or on and setLevel.
		if (descMap.data[0] == "00") {
			results << createEvent(name: "switch", value: "off", isStateChange: true)
		} else if (descMap.data[0] == "FF" && isIkeaDimmer()) {
			// The IKEA Dimmer sends 0xFF -- this is technically not to spec, but we will treat this as an "on"
			if (device.currentValue("level") == 0) {
				results << createEvent(name: "level", value: STEP)
			}

			results << createEvent(name: "switch", value: "on", isStateChange: true)
		} else {
			results << createEvent(name: "switch", value: "on", isStateChange: true)
			// Handle the Zigbee level the same way as we would normally with the same code path -- commandInt doesn't matter right now
			// The first byte is the level, the second two bytes are the rate -- we only care about the level right now.
			results << createEvent(zigbee.getEventFromAttrData(descMap.clusterInt, descMap.commandInt, UINT8_STR, descMap.data[0]))
		}
	}

	return results
}

def handleStepEvent(descMap) {
	def results = []
	def currentLevel = device.currentValue("level") as Integer ?: 0
	def value = null

	log.debug "handleStepEvent - ${descMap.data}"

	if (descMap.data[0] == LEVEL_DIRECTION_UP) {
		value = Math.min(currentLevel + STEP, 100)
	} else if (descMap.data[0] == LEVEL_DIRECTION_DOWN) {
		value = Math.max(currentLevel - STEP, 0)
	}

	if (value != null) {
		log.debug "Step ${descMap.data[0] == LEVEL_DIRECTION_UP ? "up" : "down"} by $STEP to $value"

		// don't change level if switch will be turning off
		if (value == 0) {
			results << createEvent(name: "switch", value: "off")
		} else {
			results << createEvent(name: "switch", value: "on")
			results << createEvent(name: "level", value: value)
		}
	} else {
		log.debug "Received invalid direction ${descMap.data[0]} - descMap.data = ${descMap.data}"
	}

	return results
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
	sendEvent(name: "DeviceWatch-Enroll", value: [protocol: "zigbee", scheme:"untracked"].encodeAsJson(), displayed: false)
	//these are necessary to have the device report when its buttons are pushed
	zigbee.addBinding(zigbee.ONOFF_CLUSTER) + zigbee.addBinding(zigbee.LEVEL_CONTROL_CLUSTER) + zigbee.addBinding(0x0005)
}