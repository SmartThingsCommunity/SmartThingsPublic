/**
 *  Virtual Contact
 *
 *  Copyright 2014 scdozier
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
	definition (name: "Concord4 Virtual Smoke", namespace: "scdozier", author: "scdozier") {
    capability "Contact Sensor"
    capability "Smoke Detector"
    command "open"
    command "closed"    
	}

	simulator {
		// TODO: define status and reply messages here
	}
   tiles (scale: 2){
      multiAttributeTile(name:"ContactTile", type:"generic", width:6, height:4) {
        tileAttribute("device.smokeDetector", key: "PRIMARY_CONTROL") {
            attributeState("clear", label: '${name}', icon: "st.alarm.smoke.clear", backgroundColor: "#ffffff")
 			attributeState("detected", label: '${name}', icon: "st.alarm.smoke.smoke", backgroundColor: "#e86d13")
        }
   			tileAttribute("device.armStatus", key: "SECONDARY_CONTROL") {
    			attributeState("default", label:'${currentValue}')
  			}
        }
       }

    
    main "ContactTile"
    details "ContactTile"
    
}


def open( String name ) {
	log.debug "clear"   
    sendEvent (name: "smoke", value: "clear" )
}

def closed( String name ) {
	log.debug "detected"
    sendEvent (name: "smoke", value: "detected" )
}
// parse events into attributes
def parse(String description) {
	return NULL

}