/**
 *  Copyright 2020 SRPOL
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
	definition(name: "Child Energy Meter", namespace: "smartthings", author: "SmartThings", mnmn: "SmartThings", vid: "generic-switch-power-energy") {
		capability "Power Meter"
		capability "Energy Meter"
		capability "Refresh"
		capability "Actuator"
		capability "Sensor"
		capability "Health Check"
	}

	tiles(scale: 2) {
		valueTile("power", "device.power", decoration: "flat", width: 2, height: 2) {
			state "default", label:'${currentValue} W'
		}
		valueTile("energy", "device.energy", decoration: "flat", width: 2, height: 2) {
			state "default", label:'${currentValue} kWh'
		}
		standardTile("refresh", "device.power", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "default", label:'', action:"refresh.refresh", icon:"st.secondary.refresh"
		}

		main(["switch"])
		details(["power","energy","refresh"])
	}
}

def refresh() {
	parent.childRefresh(device.deviceNetworkId)
}

def ping() {
	refresh()
}

def installed() {
	sendEvent(name: "checkInterval", value: 2 * 15 * 60 + 2 * 60, displayed: false, data: [hubHardwareId: device.hub.hardwareID])
}