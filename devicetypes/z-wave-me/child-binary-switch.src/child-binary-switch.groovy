/**
 *  Child Binary Switch
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
	definition (name: "Child Binary Switch", namespace: "z-wave-me", author: "Alexander Belov") {
		capability "Switch"
		capability "Refresh"
				
		attribute "lastUpdated", "String"
	}
	preferences {
			input "switchRevert", "enum", title: "Revert switch state", description: "", defaultValue: "1", required: true, multiple: false, options:[["1":"Don't revert"], ["2":"Revert"]], displayDuringSetup: false
	}
	tiles(scale: 2) {
		standardTile("switchLogo", "device.switchLogo", inactiveLabel: true, decoration: "flat", width: 1, height: 1) {
			state "default", label:'', icon: "http://cdn.device-icons.smartthings.com/Home/home30-icn@2x.png"
		}
		valueTile("lastUpdated", "device.lastUpdated", decoration: "flat", width: 5, height: 1) {
			state "default", label:'Last updated ${currentValue}'
		}
		multiAttributeTile(name: "switch", type: "generic", width: 6, height: 4, canChangeIcon: true) {
			tileAttribute("device.switch", key: "PRIMARY_CONTROL") {
				attributeState "on", label: '${name}', action: "switch.off", icon: "http://cdn.device-icons.smartthings.com/Home/home30-icn@2x.png", backgroundColor: "#00A0DC"
				attributeState "off", label: '${name}', action: "switch.on", icon: "http://cdn.device-icons.smartthings.com/Home/home30-icn@2x.png", backgroundColor: "#ffffff"
			}

			tileAttribute("device.refresh", inactiveLabel: false, key: "SECONDARY_CONTROL") {
				attributeState "refresh", label: '', action:"refresh.refresh", icon:"st.secondary.refresh"
			}
		}
	}
}

def parse(def description) {
	def cmd = zwave.parse(description)
	
	if (description.startsWith("Err")) {
		createEvent(descriptionText: description, isStateChange:true)
	} else if (description != "updated") {
		zwaveEvent(cmd)
		
		def nowDay = new Date().format("MMM dd", location.timeZone)
		def nowTime = new Date().format("h:mm a", location.timeZone)
		sendEvent(name: "lastUpdated", value: nowDay + " at " + nowTime, displayed: false)
	}
}

def zwaveEvent(physicalgraph.zwave.commands.switchbinaryv1.SwitchBinaryReport cmd) {
	def switchState = ((cmd.value > 0) ^ (switchRevert == 2)) ? "on" : "off" 
		
	sendEvent(name: "switch", value: switchState)
}

def refresh() {
	parent.parentCommand(parent.encap(zwaveHubNodeId, parent.extractEP(device.deviceNetworkId), zwave.switchBinaryV1.switchBinaryGet().format()))
}

def on() {
	def value = switchRevert == 1 ? 0xFF : 0x00

	parent.parentCommand(parent.encap(zwaveHubNodeId, parent.extractEP(device.deviceNetworkId), zwave.switchBinaryV1.switchBinarySet(switchValue: value).format()))
	
	refresh()
}

def off() {
	def value = switchRevert == 1 ? 0x00 : 0xFF
	
	parent.parentCommand(parent.encap(zwaveHubNodeId, parent.extractEP(device.deviceNetworkId), zwave.switchBinaryV1.switchBinarySet(switchValue: value).format()))
	
	refresh()
}