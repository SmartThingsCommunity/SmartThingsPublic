/**
 *  NX-595e Alarm Module Zone
 *
 *  Copyright 2016 Trent Foley
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
	definition (name: "NX-595e Alarm Module Zone", namespace: "trentfoley", author: "Trent Foley") {
		capability "Contact Sensor"
		capability "Polling"
		capability "Refresh"
		capability "Sensor"

		attribute "zoneStatus", "string"

		command "bypass"
        command "setZoneStatus", ["string"]
	}

	simulator {
		// TODO: define status and reply messages here
	}

	tiles(scale: 2) {
		multiAttributeTile(name:"zone", type:"generic", width: 6, height: 4) {
        	tileAttribute("device.zoneStatus", key: "PRIMARY_CONTROL") {
            	attributeState "Ready", label: '${name}', icon: "st.contact.contact.closed", backgroundColor: "#79b821"
                attributeState "Not Ready", label: '${name}', icon: "st.contact.contact.open", backgroundColor: "#ffa81e"
                attributeState "Tamper", label: '${name}', icon: "st.contact.contact.open", backgroundColor: "#ffa81e"
                attributeState "Trouble", label: '${name}', icon: "st.contact.contact.open", backgroundColor: "#ffa81e"
                attributeState "Bypass", label: '${name}', icon: "st.contact.contact.open", backgroundColor: "#ffffff"
                attributeState "Inhibited", label: '${name}', icon: "st.contact.contact.open", backgroundColor: "#ffa81e"
                attributeState "Alarm", label: '${name}', icon: "st.alarm.alarm.alarm", backgroundColor: "#ffa81e"
                attributeState "Low Battery", label: '${name}', icon: "st.contact.contact.closed", backgroundColor: "#ffa81e"
                attributeState "Supervision Fault", label: '${name}', icon: "st.contact.contact.open", backgroundColor: "#ffa81e"
            }
		}
        main "zone"
		details(["zone"])
	}
}

def setZoneStatus(zoneStatus) {
	log.debug "setZoneStatus(${zoneStatus})"
	sendEvent(name: "zoneStatus", value: zoneStatus)
}

// parse events into attributes
def parse(String description) {
	log.debug "Parsing '${description}'"
	// TODO: handle 'contact' attribute
	// TODO: handle 'zoneStatus' attribute

}

// handle commands
def poll() {
	log.debug "Executing 'poll'"
	// TODO: handle 'poll' command
}

def refresh() {
	log.debug "Executing 'refresh'"
	// TODO: handle 'refresh' command
}

def bypass() {
	log.debug "Executing 'bypass'"
	// TODO: handle 'bypass' command
}