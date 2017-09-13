/**
 *  Simulated Refrigerator Temperature Control
 *
 *  Copyright 2017 SmartThings
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
	definition (name: "Simulated Refrigerator Temperature Control", namespace: "smartthings/testing", author: "SmartThings") {
		capability "Temperature Measurement"
		capability "Thermostat Cooling Setpoint"

		command "tempUp"
		command "tempDown"
		command "setpointUp"
		command "setpointDown"
	}

	tiles {
		valueTile("refrigerator", "device.temperature", width: 2, height: 2, canChangeBackground: true) {
			state("temperature", label:'${currentValue}°', unit:"F",
					backgroundColors:[
							[value: 0, color: "#153591"],
							[value: 40, color: "#1e9cbb"],
							[value: 45, color: "#f1d801"]
					]
			)
		}
		valueTile("freezer", "device.temperature", width: 2, height: 2, canChangeBackground: true) {
			state("temperature", label:'${currentValue}°', unit:"F",
					backgroundColors:[
							[value: 0, color: "#153591"],
							[value: 5, color: "#1e9cbb"],
							[value: 15, color: "#f1d801"]
					]
			)
		}
		valueTile("freezerSetpoint", "device.coolingSetpoint", inactiveLabel: false, decoration: "flat") {
			state "setpoint", label:'Freezer Set: ${currentValue}°', unit:"F"
		}
		valueTile("refrigeratorSetpoint", "device.coolingSetpoint", inactiveLabel: false, decoration: "flat") {
			state "heat", label:'Fridge Set: ${currentValue}°', unit:"F"
		}
		standardTile("tempUp", "device.temperature", inactiveLabel: false, decoration: "flat") {
			state "default", action:"tempUp", icon:"st.thermostat.thermostat-up"
		}
		standardTile("tempDown", "device.temperature", inactiveLabel: false, decoration: "flat") {
			state "default", action:"tempDown", icon:"st.thermostat.thermostat-down"
		}
		standardTile("setpointUp", "device.coolingSetpoint", inactiveLabel: false, decoration: "flat") {
			state "default", action:"setpointUp", icon:"st.thermostat.thermostat-up"
		}
		standardTile("setpointDown", "device.coolingSetpoint", inactiveLabel: false, decoration: "flat") {
			state "default", action:"setpointDown", icon:"st.thermostat.thermostat-down"
		}
	}
}

def installed() {
	sendEvent(name: "temperature", value: device.componentName == "freezer" ? 2 : 40)
	sendEvent(name: "coolingSetpoint", value: device.componentName == "freezer" ? 2 : 40)
}

def updated() {
	installed()
}

void tempUp() {
	def value = device.currentValue("temperature") as Integer
	sendEvent(name: "temperature", value: value + 1)
}

void tempDown() {
	def value = device.currentValue("temperature") as Integer
	sendEvent(name: "temperature", value: value - 1)
}

void setpointUp() {
	def value = device.currentValue("coolingSetpoint") as Integer
	sendEvent(name: "coolingSetpoint", value: value + 1)
}

void setpointDown() {
	def value = device.currentValue("coolingSetpoint") as Integer
	sendEvent(name: "coolingSetpoint", value: value - 1)
}
