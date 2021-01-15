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
	definition (name: "ThirdReality Door Sensor", namespace: "smartthings", author: "THIRDREALITY", ocfDeviceType: "x.com.st.d.sensor.contact") {
		capability "Contact Sensor"
        capability "Battery"
        capability "Refresh"
        capability "Sensor"
        capability "Configuration"

		fingerprint profileId: "0104", deviceId: "0402", inClusters: "0000,0001,0500", outClusters: "0019", manufacturer:"Third Reality, Inc", model:"3RDS17BZ", deviceJoinName: "Door Sensor"		//'deviceJoinName' will be shown after found on App.
	}

	// simulator metadata
	simulator {
		// status messages
	}

	// UI tile definitions
	tiles {
    	standardTile(name: 'contact')
	}
}

// Parse incoming device messages to generate events
def parse(String description) {
	log.debug "[parse] description: $description"
	def resMap
    
    if (description?.startsWith("zone status")) {
    	resMap = createEvent(name: "contact", value: zigbee.parseZoneStatus(description).isAlarm1Set() ? "open" : "closed")
    } else if (description?.startsWith("read attr")) {
        def descMap = zigbee.parseDescriptionAsMap(description)
        log.debug "[parse] descMap: $descMap"
        if (descMap?.cluster == "0001" && descMap?.attrId == "0021") {
            resMap = createEvent(getBatteryPercentageResult(Integer.parseInt(descMap.value, 16)))
        } else {
        	log.warn "[WARN][parse] Unknown cluster: $descMap.cluster or attrId: descMap.attrId"
        }
    } else if (description?.startsWith("catchall")) {
    	//No need to do anything for the reading will come back again one by one.
    } else {
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
    return zigbee.readAttribute(zigbee.POWER_CONFIGURATION_CLUSTER, 0x0021) + zigbee.readAttribute(zigbee.IAS_ZONE_CLUSTER,zigbee.ATTRIBUTE_IAS_ZONE_STATUS)
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

