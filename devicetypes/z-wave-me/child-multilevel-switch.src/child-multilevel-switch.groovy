/**
 *  Child Multilevel Switch
 *
 *  Copyright 2018 Alexander Belov, Z-Wave.Me
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
metadata {
	definition (name: "Child Multilevel Switch", namespace: "z-wave-me", author: "Alexander Belov") {
		capability "Refresh"
		capability "Switch"
		capability "Switch Level"
		
		attribute "lastUpdated", "String"
	}

	tiles(scale: 2) {
		standardTile("switchLogo", "device.switchLogo", inactiveLabel: true, decoration: "flat", width: 1, height: 1) {
			state "default", label:'', icon: "http://cdn.device-icons.smartthings.com/Electronics/electronics14-icn@2x.png"
		}
		valueTile("lastUpdated", "device.lastUpdated", decoration: "flat", width: 5, height: 1) {
			state "default", label:'Last updated ${currentValue}'
		}
		multiAttributeTile(name: "switch", type: "generic", width: 6, height: 4, canChangeIcon: true) {
			tileAttribute("device.switch", key: "PRIMARY_CONTROL") {
				attributeState "on", label: '${name}', action: "switch.off", icon: "http://cdn.device-icons.smartthings.com/Electronics/electronics14-icn@2x.png", backgroundColor: "#00A0DC"
				attributeState "off", label: '${name}', action: "switch.on", icon: "http://cdn.device-icons.smartthings.com/Electronics/electronics14-icn@2x.png", backgroundColor: "#ffffff"
			} 
			tileAttribute("device.level", key: "SLIDER_CONTROL") {
				attributeState "level", action: "switch level.setLevel"
			}
		}
		standardTile("refresh", "device.switch", inactiveLabel: false, decoration: "flat", width: 1, height: 1) {
			state "refresh", label:'', action:"refresh.refresh", icon:"st.secondary.refresh"
		}
		valueTile("level", "device.level", inactiveLabel: false, decoration: "flat", width: 5, height: 1) {
			state "level", label: '${currentValue} %', unit: "%", backgroundColor: "#ffffff"
		}
	}
}

def parse(description) {
	def cmd = zwave.parse(description)
	log.debug "encap cmd: ${description}"
	
	if (description.startsWith("Err")) {
		createEvent(descriptionText: description, isStateChange:true)
	} else if (description != "updated") {
		zwaveEvent(cmd)
		
		def nowDay = new Date().format("MMM dd", location.timeZone)
		def nowTime = new Date().format("h:mm a", location.timeZone)
		sendEvent(name: "lastUpdated", value: nowDay + " at " + nowTime, displayed: false)
	}
}

def zwaveEvent(physicalgraph.zwave.commands.switchmultilevelv3.SwitchMultilevelReport cmd) {
	def switchState = cmd.value > 0 ? "on" : "off" 
	
	sendEvent(name: "level", value: "Last value: ${cmd.value}")
	sendEvent(name: "switch", value: switchState)
}

def refresh() {
	parent.parentCommand(parent.encap(zwaveHubNodeId, parent.extractEP(device.deviceNetworkId), zwave.switchMultilevelV3.switchMultilevelGet().format()))
}

def on() {
	parent.parentCommand(parent.encap(zwaveHubNodeId, parent.extractEP(device.deviceNetworkId), zwave.switchMultilevelV3.switchMultilevelSet(value: 0xFF).format()))
	refresh()
}

def off() {
	parent.parentCommand(parent.encap(zwaveHubNodeId, parent.extractEP(device.deviceNetworkId), zwave.switchMultilevelV3.switchMultilevelSet(value: 0x00).format()))
	refresh()
}

def setLevel(value) {
	parent.parentCommand(parent.encap(zwaveHubNodeId, parent.extractEP(device.deviceNetworkId), zwave.switchMultilevelV3.switchMultilevelSet(value: value).format()))
	refresh()
}