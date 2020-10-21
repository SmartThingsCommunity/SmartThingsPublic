/**
 *	Child Thermostat Setpoints
 *
 *  Copyright 2020 SmartThings
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
	definition(name: "Child Thermostat Setpoints", namespace: "smartthings", author: "SmartThings", ocfDeviceType: "oic.d.thermostat") {
		capability "Actuator"
		capability "Health Check"
		capability "Refresh"
		capability "Thermostat Cooling Setpoint"
		capability "Thermostat Heating Setpoint"
	}
}

def setCoolingSetpoint(setpoint) {
	log.debug "setCoolingSetpoint: ${setpoint}"
	parent.setChildCoolingSetpoint(device.deviceNetworkId, setpoint)
}

def setHeatingSetpoint(setpoint) {
	log.debug "setHeatingSetpoint: ${setpoint}"
	parent.setChildHeatingSetpoint(device.deviceNetworkId, setpoint)
}

def ping() {
	refresh()
}

def refresh() {
	parent.refreshChild()
}