/**
 *  Orvibo Contact Sensor
 * 
 *  Copyright Wayne Man 2016
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
 *  Orvibo Contact Sensor Device Type
 *  Battery levels updates periodically, for an instant update press physical button on sensor once
 *  30/04/2016 fixed fingerprint
 *  09/05/2016 added heartbeat to help track if sensor is alive (recommend using a devicecheck smartapp)
 */
metadata {
	definition (name: "Orvibo Contact Sensor", namespace: "a4refillpad", author: "Wayne Man") {
		capability "Contact Sensor"
		capability "Sensor"
        	capability "Battery"

		fingerprint inClusters: "0000,0001,0003,0500", manufacturer: "\u6B27\u745E", model: "75a4bfe8ef9c4350830a25d13e3ab068"

	}

	// simulator metadata
	simulator {
		// status messages
		status "open":   "zone report :: type: 19 value: 0031"
		status "closed": "zone report :: type: 19 value: 0030"
	}

    	tiles(scale: 2) {
		multiAttributeTile(name:"contact", type: "lighting", width: 6, height: 4) {
			tileAttribute ("device.contact", key: "PRIMARY_CONTROL") {
           		attributeState("open", label:'open', icon:"st.contact.contact.open", backgroundColor:"#ffa81e")
            	attributeState("closed", label:'closed', icon:"st.contact.contact.closed", backgroundColor:"#79b821")   
 			}
		}
        
        valueTile("battery", "device.battery", inactiveLabel: false, width: 2, height: 2, decoration: "flat") {
            state "battery", label:'${currentValue}% battery', unit:""
        }    
        
		main "contact"
		details(["contact", "battery"])
	}
 
}

// Parse incoming device messages to generate events
def parse(String description) {
	def name = null
	def value = description
   	def descriptionText = null
   	def now = new Date()
    log.debug "Parsing: ${description}"
    Map map = [:]

	List listMap = []
	List listResult = []
  
    
	if (zigbee.isZoneType19(description)) {
		name = "contact"
		value = zigbee.translateStatusZoneType19(description) ? "open" : "closed"
	} else if(description?.startsWith("read attr -")) {
    	map = parseReportAttributeMessage(description)
    }
	
	def result = createEvent(name: name, value: value)
	log.debug "Parse returned ${result?.descriptionText}"
//  send event for heartbeat    
    	sendEvent(name: "heartbeat", value: now)
	listResult << result
    
   	if (listMap) {
        for (msg in listMap) {
            listResult << createEvent(msg)
        }
    }
    else if (map) {
        listResult << createEvent(map)
    }

	log.debug "Parse returned ${result?.descriptionText}"
	return listResult
} 

private Map parseReportAttributeMessage(String description) {
	Map descMap = (description - "read attr - ").split(",").inject([:]) {
		map, param -> def nameAndValue = param.split(":")
		map += [(nameAndValue[0].trim()):nameAndValue[1].trim()]
	}
	Map resultMap = [:]

	log.info "IN parseReportAttributeMessage()"
	log.debug "descMap ${descMap}"

	switch(descMap.cluster) {
		case "0001":
			log.debug "Battery status reported"

			if(descMap.attrId == "0021") {
				resultMap.name = 'battery'
                resultMap.value = (convertHexToInt(descMap.value) / 2)
                log.debug "Battery Percentage convert to ${resultMap.value}%"
			}
			break
		default:
			log.info descMap.cluster
			log.info "cluster1"
			break
	}

	log.info "OUT parseReportAttributeMessage()"
	return resultMap
}

private Integer convertHexToInt(hex) {
	Integer.parseInt(hex,16)
}