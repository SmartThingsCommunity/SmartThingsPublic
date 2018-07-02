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
	definition (name: "Orvibo Contact Sensor", namespace: "Orvibe", author: "biaoyi.deng@samsung.com",  vid:"generic-contact-3", ocfDeviceType: "x.com.st.d.sensor.contact") {
        capability "Contact Sensor"
        capability "Battery"
        
        fingerprint profileId: "0104",deviceId: "0402",inClusters: "0000,0003,0500,0001",outClusters: "", manufacturer: "ORVIBO", model: "e70f96b3773a4c9283c6862dbafb6a99"
    }
    
}

def parse(String description) {
	log.debug "parse description: $description"

	def resMap
    
    if (description.startsWith("zone")) {
    	resMap = createEvent(name: "contact", value: zigbee.parseZoneStatus(description).isAlarm1Set() ? "open" : "closed")
    } else if (description.startsWith("read")) {
    	Map descMap = (description - "read attr - ").split(",").inject([:]) {
			map, param -> def nameAndValue = param.split(":")
			map += [(nameAndValue[0].trim()):nameAndValue[1].trim()]
		}
        
        switch(descMap.cluster) {
		case "0001":
			log.debug "Battery status reported"

			if(descMap.attrId == "0021") {
            	resMap = createEvent(name: "battery", value: (convertHexToInt(descMap.value) / 2))
                log.debug "Battery Percentage convert to ${resMap.value}%"
			}
			break
		default:
			log.info descMap.cluster
			log.info "cluster1"
			break
		}
    }
    
    log.debug "Parse returned $resMap"
    return resMap
}

private Integer convertHexToInt(hex) {
	Integer.parseInt(hex,16)
}

