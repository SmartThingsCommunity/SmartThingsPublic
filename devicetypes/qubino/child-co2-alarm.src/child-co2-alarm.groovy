/**
 *	Copyright 2015 SmartThings
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
 *
 *	Copyright 2014 SmartThings
 *
 */

/*
Qubino Flush Dimmer can only detect high or low state on the device input so the CO2 level is impossible to report.
 */
metadata {
	definition (name: "Child CO2 Alarm", namespace: "qubino", author: "SmartThings", runLocally: false, executeCommandsLocally: false) {
		capability "Carbon Dioxide Health Concern"
		capability "Sensor"
		capability "Health Check"
	}
}

def installed() {
	log.debug "Child CO2 Alarm installed"
}

def updated() {
	log.debug "Child CO2 Alarm updated"
}

def ping() {
	refresh()
}

def refresh() {
	parent.refreshChild()
}
