/**
 *  Copyright Plaid Systems 2020
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
 *
3-2021
 * remove parse
 * cleanup space, comments
 * remove Health Check, this is handled by parent

11-2020
 * valveDuration slider capability added back to presentation
 * tabs and trim whitespace

**/

metadata {
	definition (name: "Spruce Valve", namespace: "plaidsystems", author: "Plaid Systems", mnmn: "SmartThingsCommunity") {
		capability "Actuator"
		capability "Valve"
		capability "Sensor"
	}
}

def installed() {
	initialize()
}

def updated() {
	initialize()
}

private initialize() {
	sendEvent(name: "valve", value: "closed")
}

def open() {
	parent.valveOn(dni: device.deviceNetworkId, value: 'open', label: device.label)
}

def close() {
	parent.valveOff(dni: device.deviceNetworkId, value: 'closed', label: device.label)
}
