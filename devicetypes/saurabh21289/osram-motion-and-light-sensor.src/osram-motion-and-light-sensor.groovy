/**
 *  Osram Motion and Light Sensor
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
	definition (name: "Osram Motion and Light Sensor", namespace: "saurabh21289", author: "osramtest") {
		
        capability "Configuration"
        capability "Illuminance Measurement"
		capability "Motion Sensor"
        capability "Presence Sensor"
        capability "Sensor"
    	capability "Switch"
    	capability "Switch Level"

		fingerprint endpointId: "01", profileId: "0104", deviceId: "F006", deviceVersion: "00 07", inClusters: "0000 0004 0009 000A 0015 0B05 FC04", outClusters: "0019 FC04"
	}

	simulator {
		// TODO: define status and reply messages here
	}

	tiles {
		// TODO: define your main and details tiles here
        multiAttributeTile(name:"motion", type: "generic", width: 6, height: 4){
			tileAttribute ("device.motion", key: "PRIMARY_CONTROL") {
				attributeState "active", label:'motion', icon:"st.motion.motion.active", backgroundColor:"#53a7c0"
				attributeState "inactive", label:'no motion', icon:"st.motion.motion.inactive", backgroundColor:"#ffffff"
			}
		}
        standardTile("presence", "device.presence", width: 2, height: 2, canChangeBackground: true) {
			state("present", labelIcon:"st.presence.tile.mobile-present", backgroundColor:"#53a7c0")
			state("not present", labelIcon:"st.presence.tile.mobile-not-present", backgroundColor:"#ebeef2")
		}
        valueTile("illuminance", "device.illuminance", width: 2, height: 2) {
			state("illuminance", label:'${currentValue}', unit:"lux",
				backgroundColors:[
					[value: 9, color: "#767676"],
					[value: 315, color: "#ffa81e"],
					[value: 1000, color: "#fbd41b"]
				]
			)
		}
   		main "motion"
        details(["motion", "illuminance", "presence"])

	}
	
}

def configure() {
  log.debug "Confuguring Reporting and Bindings."
  def configCmds = [
    // Bind the outgoing on/off cluster from remote to hub, so the hub receives messages when On/Off buttons pushed
    "zdo bind 0x${device.deviceNetworkId} 0x01 0x01 0x0021 {${device.zigbeeId}} {}",

    // Bind the outgoing level cluster from remote to hub, so the hub receives messages when Dim Up/Down buttons pushed
    "zdo bind 0x${device.deviceNetworkId} 0x01 0x01 0x0010 {${device.zigbeeId}} {}",
    
    // Bind the incoming battery info cluster from remote to hub, so the hub receives battery updates
    "zdo bind 0x${device.deviceNetworkId} 0x01 0x01 0x0001 {${device.zigbeeId}} {}",
    
    // Bind the incoming battery info cluster from remote to hub, so the hub receives battery updates
    "zdo bind 0x${device.deviceNetworkId} 0x01 0x01 0x000d {${device.zigbeeId}} {}",
  ]
  return configCmds 
}


// parse events into attributes
/*def parse(String description) {
log.info "parse returned:${description}"
    `if (description != "updated"){
    	log.info "parse returned:${description}"
		def pair = description.split(":")
		createEvent(name: pair[0].trim(), value: pair[1].trim())
      }
}*/

/*def parse(String description) {
	def result
	if (description?.startsWith("illuminance: ")) {
		def raw = description - "illuminance: "
		if (raw.isNumber()) {
			result = createEvent(
				name:  "illuminance",
				value: Math.round(zigbee.lux(raw as Integer)).toString(),
				unit:  "lux"
			)
		}
	}
	log.info "Parse returned ${result?.descriptionText}"
	return result
}


def active() {
	sendEvent(name: "motion", value: "active")
    log.info "active"
}

def inactive() {
    sendEvent(name: "motion", value: "inactive")
    log.info "inactive"
}*/

def parse(String description) {
	def name = parseName(description)
	def value = parseValue(description)
	def linkText = getLinkText(device)
	def descriptionText = parseDescriptionText(linkText, value, description)
	def handlerName = getState(value)
	def isStateChange = isStateChange(device, name, value)

	def results = [
    	translatable: true,
		name: name,
		value: value,
		unit: null,
		linkText: linkText,
		descriptionText: descriptionText,
		handlerName: handlerName,
		isStateChange: isStateChange,
		displayed: displayed(description, isStateChange)
	]
	log.debug "Parse returned $results.descriptionText"
	return results

}

private String parseName(String description) {
	if (description?.startsWith("presence: ")) {
		return "presence"
	}
	null
}

private String parseValue(String description) {
	switch(description) {
		case "presence: 1": return "present"
		case "presence: 0": return "not present"
		default: return description
	}
}

private parseDescriptionText(String linkText, String value, String description) {
	switch(value) {
		case "present": return "{{ linkText }} has arrived"
		case "not present": return "{{ linkText }} has left"
		default: return value
	}
}

private getState(String value) {
	switch(value) {
		case "present": return "arrived"
		case "not present": return "left"
		default: return value
	}
}