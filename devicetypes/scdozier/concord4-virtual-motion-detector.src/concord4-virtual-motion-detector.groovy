/**
 *  Virtual Motion Detector
 *
 *  Copyright 2017 scdozier
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
	definition (name: "Concord4 Virtual Motion Detector", namespace: "scdozier", author: "scdozier") {
    capability "Contact Sensor"
    capability "Motion Sensor"
    command "open"
    command "closed"    
    command "active"
    command "inactive"
	}

	simulator {
		// TODO: define status and reply messages here
	}

	tiles {
        standardTile("motion", "device.motion", inactiveLabel: false) {
			state "inactive", label: '${name}', icon: "st.motion.motion.inactive", backgroundColor: "#ffffff"
			state "active", label: '${name}', icon: "st.motion.motion.active", backgroundColor: "#00a0dc"
		}  
	}
    
    main "motion"
}


def open( String name ) {
	log.debug "Open"   
    sendEvent (name: "motion", value: "active" )
}

def closed( String name ) {
	log.debug "Close"
    sendEvent (name: "motion", value: "inactive" )
}
// parse events into attributes
def parse(String description) {
	return NULL

}