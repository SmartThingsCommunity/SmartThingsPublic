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
 *	Ecobee Sensor
 *
 *	Author: SmartThings
 */
import groovy.json.JsonOutput
metadata {
	definition (name: "Ecobee Sensor", namespace: "smartthings", author: "SmartThings") {
		capability "Health Check"
		capability "Sensor"
		capability "Temperature Measurement"
		capability "Motion Sensor"
		capability "Refresh"
	}

	tiles(scale: 2) {
		multiAttributeTile(name: "temperature", type: "generic", width: 6, height: 4, canChangeIcon: true) {
      			tileAttribute ("device.temperature", key: "PRIMARY_CONTROL") {
			  	attributeState "temperature", label:'${currentValue}°', icon: "st.alarm.temperature.normal",
					backgroundColors:[
						// Celsius
						[value: 0, color: "#153591"],
						[value: 7, color: "#1e9cbb"],
						[value: 15, color: "#90d2a7"],
						[value: 23, color: "#44b621"],
						[value: 28, color: "#f1d801"],
						[value: 35, color: "#d04e00"],
						[value: 37, color: "#bc2323"],
						// Fahrenheit
						[value: 40, color: "#153591"],
						[value: 44, color: "#1e9cbb"],
						[value: 59, color: "#90d2a7"],
						[value: 74, color: "#44b621"],
						[value: 84, color: "#f1d801"],
						[value: 95, color: "#d04e00"],
						[value: 96, color: "#bc2323"]
					    ]
			}
		}

		standardTile("motion", "device.motion", inactiveLabel: false, width: 2, height: 2) {
			state "active", label:"Motion", icon:"st.motion.motion.active", backgroundColor:"#00A0DC"
			state "inactive", label:"No Motion", icon:"st.motion.motion.inactive", backgroundColor:"#cccccc"
		}

		standardTile("refresh", "device.refresh", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "default", action:"refresh.refresh", icon:"st.secondary.refresh"
		}

		main (["temperature","motion"])
		details(["temperature","motion","refresh"])
	}
}

def initialize() {
	sendEvent(name: "DeviceWatch-Enroll", value: JsonOutput.toJson([protocol: "cloud", scheme:"untracked"]), displayed: false)
	updateDataValue("EnrolledUTDH", "true")
}

void installed() {
	initialize()
}

def updated() {
	log.debug "updated()"
	parent.setSensorName(device.label, device.deviceNetworkId)
	initialize()
}

// Called when the DTH is uninstalled, is this true for cirrus/gadfly integrations?
// Informs parent to purge its associated data
def uninstalled() {
	log.debug "uninstalled() parent.purgeChildDevice($device.deviceNetworkId)"
	// purge DTH from parent
	parent?.purgeChildDevice(this)
}

def refresh() {
	log.debug "refresh, calling parent poll"
	parent.poll()
}
