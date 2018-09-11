/**
 *  Copyright 2015 SmartThings
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
	definition (name: "ZigBee Accessory Dimmer", namespace: "smartthings", author: "SmartThings", ocfDeviceType: "x.com.st.d.remotecontroller", runLocally: false, minHubCoreVersion: '000.019.00012', executeCommandsLocally: false, mnmn: "SmartThings", vid: "generic-dimmer") {
		capability "Actuator"
		capability "Configuration"
		capability "Switch"
		capability "Button"
		capability "Switch Level"
		capability "Health Check"

		fingerprint profileId: "0104", inClusters: "0000,0003", outClusters: "0000,0004,0003,0006,0008,0005", manufacturer: "Aurora", model: "Remote50AU", deviceJoinName: "Aurora Wireless Wall Remote"
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
		if (descMap && descMap.clusterInt == 0x0006) {
			if (descMap.commandInt == 0x01 || descMap.commandInt == 0x00) {
				if (device.currentValue("level") == 0) {
					sendEvent(name: "level", value: STEP)
				}
				sendEvent(name: "switch", value: device.currentValue("switch") == "on" ? "off" : "on")
			} else {
				log.warn "ON/OFF REPORTING CONFIG FAILED- error code:${cluster.data[0]}"
			}
		} else if (descMap && descMap.clusterInt == 0x0008) {
			def currentLevel = device.currentValue("level") as Integer
			if (descMap.commandInt == 0x02) {
				def value = Math.min(currentLevel + STEP, 100)
				log.debug "move to ${descMap.data}"
				if (descMap.data[0] == "00") {
					log.debug "move up"
                    sendEvent(name: "switch", value: "on")
					sendEvent(name: "level", value: value)
				} else if (descMap.data[0] == "01") {
					log.debug "move down"
					value = Math.max(currentLevel - STEP, 0)
					sendEvent(name: "level", value: value)
					if (value == 0) {
						sendEvent(name: "switch", value: "off")
					}
				}
			} else if (descMap.commandInt == 0x01) {
//				def step = descMap.data[0] == "00" ? 10 : -10
				sendEvent(name: "level", value: descMap.data[0] == "00" ? 100 : STEP)
                sendEvent(name: "switch", value: "on" )
//				def events = []
//                for (currentLevel..target).step(step).each{
//					events << createEvent(name: "level", value: currentLevel + it)
//				}
				log.debug "step to ${descMap.data}"
			} else if (descMap.commandInt == 0x03) {
				log.debug "stop move"
			}
		} else if (descMap && descMap.clusterInt == 0x0005) {
			if (descMap.commandInt == 0x05) {
                sendEvent(name: "button", value: "pressed", isStateChange: true)
			} else if (descMap.commandInt == 0x04) {
				sendEvent(name: "button", value: "held", isStateChange: true)
			}
		} else {
			log.warn "DID NOT PARSE MESSAGE for description : $description"
			log.debug "${descMap}"
		}
	}
}

def off() {
	sendEvent(name: "switch", value: "off")
}

def on() {
	if (device.currentValue("level") == 0) {
		sendEvent(name: "level", value: STEP)
	}
	sendEvent(name: "switch", value: "on")
}

def setLevel(value) {
	if (value != 0) sendEvent(name: "switch", value: "on")
	sendEvent(name: "level", value: value)
}
/**
 * PING is used by Device-Watch in attempt to reach the Device
 * */
def ping() {
	return refresh()
}

def refresh() {
	zigbee.readAttribute(0x0000, 0x0000)
}

def installed() {
	sendEvent(name: "switch", value: "off", isStateChange: false, displayed: false)
	sendEvent(name: "level", value: 0, isStateChange: false, displayed: false)
	sendEvent(name: "button", value: "pressed", isStateChange: false, displayed: false)
	sendEvent(name: "checkInterval", value: 24 * 60 * 60, displayed: false, data: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID])
}

def configure() {
	log.debug "Configuring Reporting, IAS CIE, and Bindings."
	def cmds = []
	return zigbee.onOffConfig() +
			zigbee.levelConfig() +
			//zigbee.configureReporting(zigbee.POWER_CONFIGURATION_CLUSTER, 0x20, DataType.UINT8, 30, 21600, 0x01) +
			//zigbee.enrollResponse() +
			//zigbee.readAttribute(zigbee.POWER_CONFIGURATION_CLUSTER, 0x20) +
			zigbee.configureReporting(0x0005, 0x0001, DataType.UINT8, 1, 3600, null)
	cmds
}