/**
 *	Copyright 2020 SmartThings
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
metadata {
	definition(name: "Qubino Temperature Sensor", namespace: "qubino", author: "SmartThings", mnmn: "SmartThings", vid: "SmartThings-smartthings-Qubino_Temperature_Sensor", ocfDeviceType: "oic.d.thermostat") {
		capability "Health Check"
		capability "Refresh"
		capability "Sensor"
		capability "Temperature Measurement"
	}

	tiles(scale: 2) {
		multiAttributeTile(name: "temperature", type: "generic", width: 6, height: 4) {
			tileAttribute("device.temperature", key: "PRIMARY_CONTROL") {
				attributeState("temperature", label:'${currentValue}°')
			}
		}

		standardTile("refresh", "device.refresh", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "default", label:'', action:"refresh.refresh", icon:"st.secondary.refresh"
		}
		main "temperature"
		details(["temperature", "refresh"])
	}
}

def installed() {
	log.debug "Child Temperature Sensor installed"
}

def updated() {
	log.debug "Child Temperature Sensor updated"
}

def ping() {
	refresh()
}

def refresh() {
	parent.refreshChild()
}