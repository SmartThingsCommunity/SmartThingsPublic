/**
 *  Lightify Smart Plug
 *
 *  Copyright 2016 osramtest
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
	definition (name: "OSRAM Lightify Smart Switch", namespace: "saurabh21289", author: "osram.test") {
		capability "Switch"
		capability "Configuration"

		//Raw description: 03 C05E 0010 02 08 1000 0000 0003 0004 0005 0006 0B04 FC0F 01 0019
		fingerprint profileId: "0104", inClusters: "0000 0003 0004 0005 0006 0B04 0B05 FC03", outClusters: "0019", manufacturer: "OSRAM Lightify",  model: "Plug 01", deviceJoinName: "OSRAM Lightify Smart Switch"
		
	}

	// simulator metadata
	simulator {
		
	}

	// UI tile definitions
	tiles(scale: 2) {
		multiAttributeTile(name:"switch", type: "lighting", width: 6, height: 4, canChangeIcon: true){
			tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
				attributeState "on", label: '${name}', action: "switch.off", icon: "st.switches.switch.on", backgroundColor: "#79b821", nextState: "turningOff"
				attributeState "off", label: '${name}', action: "switch.on", icon: "st.switches.switch.off", backgroundColor: "#ffffff", nextState: "turningOn"
                attributeState "toggle", label: '${name}', action: "switch.toggle", icon: "st.switches.switch.off", backgroundColor: "#ffffff", nextState: "turningOn"
				attributeState "turningOn", label: '${name}', action: "switch.off", icon: "st.switches.switch.on", backgroundColor: "#79b821", nextState: "turningOff"
				attributeState "turningOff", label: '${name}', action: "switch.on", icon: "st.switches.switch.off", backgroundColor: "#ffffff", nextState: "turningOn"
			}
			
		}    
        
		main "switch"
		details(["switch", "refresh"])
	}
}

// Parse incoming device messages to generate events
def parse(String description) {
	log.debug "Zigbee $description"
	def result = zigbee.getKnownDescription(description) //Parses messages which match the capabilities and stores it into "result" variable in JSON format
    log.info "result = " + result

	if (result) {
		log.info result.type + " " + result.value
		sendEvent(name: result.type, value: result.value)
	}
	else {
		log.warn "Couldn't parse message: $description" //Error logging
	}
}

def off() {
	zigbee.off()
}

def on() {
	zigbee.on()
}

def configure() {
	zigbee.onOffRefresh()
}



