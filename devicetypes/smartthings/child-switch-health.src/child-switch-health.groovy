/**
 *  Copyright 2018 SmartThings
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */
metadata {
	definition(name: "Child Switch Health", namespace: "smartthings", author: "SmartThings", mnmn: "SmartThings", vid: "generic-switch") {
		capability "Switch"
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

		main "switch"
		details(["switch"])
	}
}

def installed() {
	// This is set to a default value, but it is the responsibility of the parent to set it to a more appropriate number
	sendEvent(name: "checkInterval", value: 30 * 60, displayed: false, data: [protocol: "zigbee"])
}

void on() {
	parent.childOn(device.deviceNetworkId)
}

void off() {
	parent.childOff(device.deviceNetworkId)
}

def ping() {
	// Intentionally left blank as parent should handle this
}

def uninstalled() {
	parent.delete()
}