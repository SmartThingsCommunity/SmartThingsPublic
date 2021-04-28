/**
 *  Orvibo Motion Sensor
 *
 *  Copyright 2016 Chris Kitch
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
	definition (name: "Orvibo Motion Sensor", namespace: "kriskit.orvibo", author: "Chris Kitch") {
		capability "Motion Sensor"
		capability "Sensor"
		capability "Battery"

		fingerprint inClusters: "0000,0001,0003,0009,0500,0020", manufacturer: "\u6B27\u745E", model: "585fdfb8c2304119a2432e9845cf2623"
	}

	// simulator metadata
	simulator {
		status "active": "zone report :: type: 19 value: 0031"
		status "inactive": "zone report :: type: 19 value: 0030"
	}
    
	preferences {
		section {
			input title: "Motion Timeout", description: "These devices don't report when motion stops, so it's necessary to have a timer to report that motion has stopped. You can adjust how long this is below.", displayDuringSetup: false, type: "paragraph", element: "paragraph"
			input "motionStopTime", "number", title: "Seconds", range: "*..*", displayDuringSetup: false, defaultValue: 2
		}
	}

	// UI tile definitions
	tiles(scale: 2) {
		multiAttributeTile(name:"motion", type: "generic", width: 6, height: 4){
			tileAttribute ("device.motion", key: "PRIMARY_CONTROL") {
				attributeState "active", label:'motion', icon:"st.motion.motion.active", backgroundColor:"#53a7c0"
				attributeState "inactive", label:'no motion', icon:"st.motion.motion.inactive", backgroundColor:"#ffffff"
			}
		}
        
		valueTile("battery", "device.battery", inactiveLabel: false, decoration: "flat", width: 6, height: 2) {
			state "battery", label:'${currentValue}% battery', unit:""
		}

		main "motion"
		details(["motion", "battery"])
	}
}

// Parse incoming device messages to generate events
def parse(String description) {
    def name = null
	def value = description
	def descriptionText = null
    log.debug "Parsing: ${description}"
    
	Map map = [:]

	List listMap = []
	List listResult = []
    
	if (zigbee.isZoneType19(description)) {
		name = "motion"
		def isActive = zigbee.translateStatusZoneType19(description)
        
		value = isActive ? "active" : "inactive"
		descriptionText = getDescriptionText(isActive)
	} else if(description?.startsWith("read attr -")) 
    	map = parseReportAttributeMessage(description)

	if (value == "active") {
    	def timeout = 2
        
        if (motionStopTime)
        	timeout = motionStopTime
        
        log.debug "Stopping motion in ${timeout} seconds"
    	runIn(timeout, stopMotion)
	}

	def result = createEvent(
		name: name,
		value: value,
		descriptionText: descriptionText
	)
    
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

def stopMotion() {
	def description = getDescriptionText(false)
	log.debug description
    sendEvent(name:"motion", value: "inactive", descriptionText: description)
}

private getDescriptionText(isActive) {
	return isActive ? "${device.displayName} detected motion" : "${device.displayName} motion has stopped"
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
