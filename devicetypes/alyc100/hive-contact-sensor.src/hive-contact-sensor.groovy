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
 */

metadata {
	definition (name: "Hive Window or Door Sensor V1.0", namespace: "simonjgreen", author: "Simon Green") {
		capability "Actuator"
		capability "Polling"
		capability "Refresh"
		capability "Contact Sensor"
	}

	simulator {
		status "open": "open/closed: open"
		status "closed": "open/closed: closed"
	}

	tiles(scale: 2){
		standardTile("state", "device.state",  width: 6, height: 4, key:"PRIMARY_CONTROL") {
			state "open", label: "open", icon: "st.contact.contact.open", backgroundColor: "#FF0000"
			state "closed", label: "closed", icon: "st.contact.contact.closed", backgroundColor: "#00CC00"
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
  def state = data.nodes.attributes.state.reportedValue[0]

  log.debug "state: $state"

  if (state == "OPEN") {
    statusMsg = statusMsg + " Open"
  }
  else if (state == "CLOSED") {
  	statusMsg = statusMsg + " Closed"
	}

  sendEvent(name: 'state', value: state)
}

def refresh() {
	log.debug "Executing 'refresh'"
	poll()
}