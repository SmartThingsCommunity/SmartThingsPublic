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
 */
metadata {
	definition (name: "Telguard Garage Door Switch", namespace: "mjarends", author: "Mitch Arends") {
		capability "Actuator"
		capability "Switch"
		capability "Polling"
		capability "Refresh"
		capability "Sensor"
        capability "Contact Sensor"
        capability "Door Control"
		capability "Garage Door Control"

        attribute "contactState", "string"

		fingerprint deviceId: "0x1000", inClusters: "0x72,0x86,0x25", manufacturer: "Telguard", model: "GDC1"
	}

	// simulator metadata
	simulator {
		status "on":  "command: 2003, payload: FF"
		status "off": "command: 2003, payload: 00"

		// reply messages
		reply "2001FF,delay 100,2502": "command: 2503, payload: FF"
		reply "200100,delay 100,2502": "command: 2503, payload: 00"
	}

	// tile definitions
	tiles(scale: 2) {
		multiAttributeTile(name:"switch", type: "lighting", width: 6, height: 4, canChangeIcon: true){
			tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
                attributeState "off", label: 'Closed', action: "switch.on", icon: "st.doors.garage.garage-closed", backgroundColor: "#79b821", nextState:"openingdoor"
				attributeState "on", label: 'Open', action: "switch.off", icon: "st.doors.garage.garage-open", backgroundColor: "#ffa81e", nextState:"closingdoor"
                attributeState "closingdoor", label:'Closing', icon:"st.doors.garage.garage-closing", backgroundColor:"#ffd700"
                attributeState "openingdoor", label:'Opening', icon:"st.doors.garage.garage-opening", backgroundColor:"#ffd700"
			}
            tileAttribute ("device.statusText", key: "SECONDARY_CONTROL") {
           		attributeState "statusText", label:'${currentValue}'
            }
		}
   		standardTile("open", "device.door", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "default", label:'open', action:"door control.open", icon:"st.doors.garage.garage-opening"
		}
		standardTile("close", "device.door", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "default", label:'close', action:"door control.close", icon:"st.doors.garage.garage-closing"
		}
        standardTile("contact", "device.contact", width: 2, height: 2) {
			state "open", label: '${name}', icon: "st.contact.contact.open", backgroundColor: "#79b821"
			state "closed", label: '${name}', icon: "st.contact.contact.closed", backgroundColor: "#ffffff"
		}
		standardTile("refresh", "device.switch", width: 2, height: 2, decoration: "flat") {
			state "default", label:'', action:"refresh.refresh", icon:"st.secondary.refresh"
		}

		main "switch"
		details(["switch","open","close","contact","refresh"])
	}
}

def parse(String description) {
	def result = null
    log.debug "description: ${description}"
	def cmd = zwave.parse(description, [0x20: 1, 0x25: 1])
    // Process the parsed command
    log.debug "command: ${cmd}"
    if (cmd) {
		result = zwaveEvent(cmd)
        log.debug "parsed ${cmd} to ${result.inspect()}"
	}   
	log.debug "Parse returned ${result?.descriptionText}"
    
	return result
}

// Create events
def zwaveEvent(physicalgraph.zwave.commands.switchbinaryv1.SwitchBinaryReport cmd) {
    log.debug "switch binary report: ${cmd}"
	def result = []
    result << createEvent(name: "switch", value: cmd.value ? "on" : "off")
    result << createEvent(name: "contact", value: cmd.value ? "open" : "closed")
    result << createEvent(name: "door", value: cmd.value ? "open" : "closed")
    def state = cmd.value ? "OPEN" : "CLOSED"
    result << createEvent(name: "contactState", value: state)
    def timeString = new Date().format("h:mma MM-dd-yyyy", location.timeZone)
    def statusTextmsg = "Last refreshed: ${timeString}"
    result << createEvent("name":"statusText", "value":statusTextmsg, displayed: false)
    result
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
	// Handles all Z-Wave commands we aren't interested in
    log.debug "cmd: ${cmd}"
	[:]
}

// Implement commands
def on() {
	// If the device is off then turn on
	if (device.currentValue("switch") == "off") {
        log.debug "opening the door"
        delayBetween([
            zwave.basicV1.basicSet(value: 0xFF).format(),
            zwave.switchBinaryV1.switchBinaryGet().format()
        ])
   	} else {
    	log.debug "Not opening door since it is already open"
    }
}

def off() {
	// If the device is off then turn on
	if (device.currentValue("switch") == "on") {
        log.debug "closing the door"
        delayBetween([
            zwave.basicV1.basicSet(value: 0x00).format(),
            zwave.switchBinaryV1.switchBinaryGet().format()
        ])
   	} else {
    	log.debug "Not closing door since it is already closed"
    }
}

def open() {
	log.debug "attempting to open the door"
	// If the device is not in a open or opening state
	if (device.currentValue("contact") != "open") {
		log.debug "Door is currently " + device.currentValue("contact") + " - opening the door"
		on()
	} else {
		log.debug "Not opening door since it is already open"
	}
}

def close() {
	log.debug "attempting to close the door"
	if (device.currentValue("contact") != "closed") {
		log.debug "Door is currently " + device.currentValue("contact") + " - closing the door"
		off()
	} else {
		log.debug "Not closing door since it is already closed"
	}
}

def poll() {
	log.debug "polling device"
	delayBetween([
		zwave.switchBinaryV1.switchBinaryGet().format()
	])
}

def refresh() {
	log.debug "refreshing device"
	delayBetween([
		zwave.switchBinaryV1.switchBinaryGet().format()
	])
}
