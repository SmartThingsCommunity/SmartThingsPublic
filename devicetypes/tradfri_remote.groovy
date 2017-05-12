/**
 *  IKEA Tr&aring;dfri Dimmer
 *
 *  Copyright 2017 Jonas Laursen
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
	definition (name: "IKEA Trådfri Remote", namespace: "dk.decko", author: "Keith Spragg") {
		capability "Battery"
		capability "Switch"
		capability "Configuration"
		capability "Refresh"
        
        fingerprint profileId: "0104", inClusters: "0000,0001,0003,0009,0B05,1000", outClusters: "0003,0004,0005,0006,0008,0019,1000", manufacturer: "IKEA of Sweden", model: "TRADFRI remote control"
	}


	simulator {
		// TODO: define status and reply messages here
	}

	tiles {
		standardTile("switch", "device.switch") {
        	state "off", label: "Off", icon: "st.switches.switch.off"
            state "on", label: "On", icon: "st.switches.switch.on"
        }
        standardTile("configure", "device.configure", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "configure", label:'', action:"configuration.configure", icon:"st.secondary.configure"
		}
	}
    
    main("switch")
    details(["configure"])
}

// parse events into attributes
def parse(String description) {
	log.debug "Parsing '${description}'"
    /** def map = zigbee.parseDescriptionAsMap(description)
    if (map.command == "04") {
     	if (map.data[0] == "00") {
            sendEvent(name: "switch", value: "off")
        } else if (map.data[0] == "FF") {
            sendEvent(name: "switch", value: "on")
        }
    } */
}

def configure() {
	log.debug "Configure called"
	["zdo bind 0x${device.deviceNetworkId} 0x01 0x01 5 {${device.zigbeeId}} {}"]
}
