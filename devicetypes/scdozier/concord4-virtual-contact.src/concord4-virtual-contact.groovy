/**
 *  Virtual Contact
 *
 *  Copyright 2014 CaesarsGhost
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
	definition (name: "Concord4 Virtual Contact", namespace: "scdozier", author: "CaesarsGhost") {
    capability "Contact Sensor"
    capability "Sensor"
    command "open"
    command "closed"    
	}

	simulator {
		// TODO: define status and reply messages here
	}

	tiles (scale: 2){
      multiAttributeTile(name:"ContactTile", type:"generic", width:6, height:4) {
        tileAttribute("device.contact", key: "PRIMARY_CONTROL") {
            attributeState("open", label: '${name}', icon: "st.contact.contact.open", backgroundColor: "#e86d13")
 			attributeState("closed", label: '${name}', icon: "st.contact.contact.closed", backgroundColor: "#00a0dc")
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
	log.debug("Setting device ${name} to open")
    sendEvent (name: "contact", value: "open" )
}

def closed( String name ) {
	log.debug("Setting device ${name} to closed")
    sendEvent (name: "contact", value: "closed" )
}
// parse events into attributes
def parse(String description) {
	return NULL

}