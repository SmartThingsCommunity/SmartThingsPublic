/**
 *  Almond Click Button
 *
 *  Copyright 2017 Gene Eilebrecht
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
	definition (name: "Almond Click", namespace: "geneiparq", author: "Gene Eilebrecht") {
        capability "Actuator"
		capability "Button"
		capability "Holdable Button"

		fingerprint endpointId: "01", profileId: "0104", inClusters: "0000,0003,0500", outClusters: "0003,0501", model:"ZB2-BU01", manufacturer: "Securifi Ltd. ZB2-BU01"
	}
    
    simulator {
	}

	tiles {
		standardTile("button", "device.button", width: 2, height: 2) {
			state "default", label: "", icon: "st.unknown.zwave.remote-controller", backgroundColor: "#ffffff"
		}
		main "button"
		details(["button"])
	}
}

def parse(String description) {
	log.debug "parse description: $description"
    def descMap = zigbee.parseDescriptionAsMap(description)
    
    def button = 1
    def results = null
    
    if (description?.startsWith('catchall: 0104 0501 01 FF 0140 00 8563 01 00 0000 00 00 0308')){
		results = createEvent([
    		name: "button",
        	value: "pushed", 
        	data:[buttonNumber: button], 
        	descriptionText: "${device.displayName} button ${button} was pushed",
        	isStateChange: true, 
        	displayed: true
    	])
    } else if (description?.startsWith('catchall: 0104 0501 01 FF 0140 00 8563 01 00 0000 00 00 0208')){
    	results = createEvent([
    		name: "button",
        	value: "held", 
        	data:[buttonNumber: button], 
        	descriptionText: "${device.displayName} button ${button} was held",
        	isStateChange: true, 
        	displayed: true
    	])
    } else if (description?.startsWith('catchall: 0104 0501 01 FF 0140 00 8563 01 00 0000 00 00 0008')){
    	results = createEvent([
    		name: "button",
        	value: "doublepushed", 
        	data:[buttonNumber: button], 
        	descriptionText: "${device.displayName} button ${button} was double pushed",
        	isStateChange: true, 
        	displayed: true
    	])
   	} else {
    	log.debug "unhandled response"
        results = null
    }
            
	return results;
}