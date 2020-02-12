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
metadata {
	definition(name: "Child Dawon Z-wave Wall Smart Switch Health", namespace: "smartthings", author: "SmartThings", mnmn: "SmartThings", runLocally: true, minHubCoreVersion: '000.017.0012', executeCommandsLocally: false, vid: "generic-switch") {
		capability "Switch"
		capability "Refresh"
		capability "Actuator"
		capability "Sensor"
		capability "Health Check"
	}

	tiles(scale: 2) {
		multiAttributeTile(name: "switch", width: 6, height: 4, canChangeIcon: false) {
			tileAttribute("device.switch", key: "PRIMARY_CONTROL") {
				attributeState "on", label: '${name}', action: "switch.off", icon: "st.switches.light.on", backgroundColor: "#00a0dc"
				attributeState "off", label: '${name}', action: "switch.on", icon: "st.switches.light.off", backgroundColor: "#ffffff"
			}
		}
		standardTile("refresh", "device.switch", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "default", label:"", action:"refresh.refresh", icon:"st.secondary.refresh"
		}
		main "switch"
		details(["switch"])
	}
}

def installed() {
	// This is set to a default value, but it is the responsibility of the parent to set it to a more appropriate number
	sendEvent(name: "checkInterval", value: 30 * 60 + 2 * 60, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID, offlinePingable: "1"])
	refresh()
}

def on() {
	log.info "on"
	parent.childOnOff(device.deviceNetworkId, 0xFF)
}

def off() {
	log.info "off"
	parent.childOnOff(device.deviceNetworkId, 0x00)
}

def refresh() {
	log.info "refresh"
	parent.childRefresh(device.deviceNetworkId)
}

def ping() {
	refresh()
}
