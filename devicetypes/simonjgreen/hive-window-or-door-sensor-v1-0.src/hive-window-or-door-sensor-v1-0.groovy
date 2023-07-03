/**
 *  Hive Window or Door Sensor V1.0
 *
 *  Copyright 2016 Simon Green
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
 *	VERSION HISTORY
 *    4th Sept. '16
 *    v1.0 Initial Release
*
 *    10th Sept. '16
 *    v1.1 Added support for temperature (the contact sensor has a temperature sensor in it) and battery level reading. Properly defined capabilities and attributes for use in other smartapps.
 */

metadata {
	definition (name: "Hive Window or Door Sensor V1.0", namespace: "simonjgreen", author: "Simon Green") {
		capability "Polling"
		capability "Refresh"
		capability "Contact Sensor" // "contact" string ("open" | "closed")
		capability "Temperature Measurement" // "temperature" number
		capability "Battery" // "battery" any

		attribute "contact", "string", ["open", "closed"]
		attribute "temperature", "number"
		attribute "battery", "string"
	}

	simulator {
		status "open": "open/closed: open"
		status "closed": "open/closed: closed"
	}

	tiles(scale: 2){
		standardTile("contact", "device.contact",  width: 6, height: 4, key:"PRIMARY_CONTROL") {
			state "open", label: "open", icon: "st.contact.contact.open", backgroundColor: "#FF0000"
			state "closed", label: "closed", icon: "st.contact.contact.closed", backgroundColor: "#00CC00"
		}
		standardTile("temperature", "device.temperature", inactiveLabel: true, decoration: "flat", width: 2, height: 2) {
			state("temperature", label: '${currentValue} °C', icon:"st.Weather.weather2")
		}
		standardTile("battery", "device.battery", inactiveLabel: true, decoration: "flat", width: 2, height: 2) {
			state("battery", label: '${currentValue}', icon:"st.Appliances.appliances17")
		}
		standardTile("refresh", "device.refresh", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state("default", label:'refresh', action:"polling.poll", icon:"st.secondary.refresh-icon")
		}
	}
}

// parse events into attributes
def parse(String description) {
	log.debug "Parsing '${description}'"
}

def installed() {
	log.debug "Executing 'installed'"
}

def poll() {
	log.debug "Executing 'poll'"
	def resp = parent.apiGET("/nodes/${device.deviceNetworkId}")
	if (resp.status != 200) {
		log.error("Unexpected result in poll(): [${resp.status}] ${resp.data}")
		return []
	}
	data.nodes = resp.data.nodes

  //Construct status message
  def statusMsg = "Currently"

  // determine contact sensor state
  def contact = data.nodes.attributes.state.reportedValue[0]
	def temperature = data.nodes.attributes.temperature.reportedValue[0]
	def battery = data.nodes.attributes.batteryState.reportedValue[0]

  log.debug "contact: $contact"
	log.debug "temperature: $temperature"
	log.debug "battery: $battery"

  if (contact == "OPEN") {
    statusMsg = statusMsg + " Open"
		contact = "open"
  }
  else if (contact == "CLOSED") {
  	statusMsg = statusMsg + " Closed"
		contact = "closed"
	}

  sendEvent(name: 'contact', value: contact)
	sendEvent(name: 'temperature', value: temperature)
	sendEvent(name: 'battery', value: battery)
}

def refresh() {
	log.debug "Executing 'refresh'"
	poll()
}