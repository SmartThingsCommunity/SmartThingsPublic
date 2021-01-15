/**
 *  ThirdReality Motion Sensor
 *
 *  Copyright 2021 THIRDREALITY
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 */
metadata {
	definition (name: "ThirdReality Motion Sensor", namespace: "smartthings", author: "THIRDREALITY", cstHandler: true) {
		capability "Battery"
		capability "Motion Sensor"
        capability "Refresh"
        capability "Configuration"
        capability "Sensor"
        
        fingerprint profileId: "0104", deviceId: "0402", inClusters: "0000, 0001, 0500", outClusters: "0019", manufacturer: "Third Reality, Inc", model: "3RMS16BZ", deviceJoinName: "Motion Sensor" //ThirdReality Motion Sensor
	}


	simulator {
		// TODO: define status and reply messages here
	}

	tiles {
		// TODO: define your main and details tiles here
        standardTile(name: 'motion')
	}
}

// parse events into attributes
def parse(String description) {
	log.debug "Parsing '${description}'"
	def resMap

    if (description?.startsWith("zone status")) {
    	resMap = createEvent(name: "motion", value: zigbee.parseZoneStatus(description).isAlarm1Set() ? "active" : "inactive")
    } 
    else if (description?.startsWith("read attr")) {
        def descMap = zigbee.parseDescriptionAsMap(description)
        log.debug "[parse] descMap: $descMap"
        if (descMap?.cluster == "0001" && descMap?.attrId == "0021") {
        	log.debug "[parse] BatteryPercentageRemaining: 0x$descMap.value"
            resMap = createEvent(getBatteryPercentageResult(Integer.parseInt(descMap.value, 16)))
        } 
        else {
        	log.warn "[WARN][parse] Unknown cluster: $descMap.cluster or attrId: descMap.attrId"
        }
    } 
    else if (description?.startsWith("catchall")) {
    	//No need to do anything for the reading will come back again one by one.
    } 
    else {
    	log.warn "[WARN][parse] Unknown description: $description"
    }
    
	log.debug "[parse] returned $resMap"
	return resMap
}

def configure() {
	log.trace "[configure]"
    return zigbee.readAttribute(zigbee.POWER_CONFIGURATION_CLUSTER, 0x0021)
}

def refresh() {
	log.trace "[refresh]"
    return zigbee.readAttribute(zigbee.POWER_CONFIGURATION_CLUSTER, 0x0021) 
		 + zigbee.readAttribute(zigbee.IAS_ZONE_CLUSTER,zigbee.ATTRIBUTE_IAS_ZONE_STATUS)
}

def getBatteryPercentageResult(rawValue) {
	def result = [:]
	if (0 <= rawValue && rawValue <= 200) {
		result.name = 'battery'
		result.translatable = true
		result.value = Math.round(rawValue)
		result.descriptionText = "${device.displayName} battery was ${result.value}%"
	}
	return result
}