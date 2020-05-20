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
	definition(name: "Child Illuminance Sensor", namespace: "smartthings", author: "SmartThings", ocfDeviceType: "oic.d.sensor") {
		capability "Actuator"
		capability "Health Check"
		capability "Refresh"
		capability "Sensor"
		capability "Illuminance Measurement"
	}

	tiles(scale: 2) {
		multiAttributeTile(name:"illuminance", type: "generic", width: 6, height: 4){
			tileAttribute("device.illuminance", key: "PRIMARY_CONTROL") {
				attributeState("illuminance", label:'${currentValue}', unit:"lux",
						backgroundColors:[
								[value: 9, color: "#767676"],
								[value: 315, color: "#ffa81e"],
								[value: 1000, color: "#fbd41b"]
						]
				)
			}
		}
		main "illuminance"
		details(["illuminance", "refresh"])
	}
}

def installed() {
	log.debug "Child Illuminance Sensor installed"
}

def updated() {
	log.debug "Child Illuminance Sensor updated"
}

def ping() {
	refresh()
}

def refresh() {
	parent.refreshChild()
}