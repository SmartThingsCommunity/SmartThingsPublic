/**
 *  ThirdReality WaterLeak Sensor
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
    definition (name: "ThirdReality WaterLeak Sensor", namespace: "smartthings", author: "THIRDREALITY", cstHandler: true) {
        capability "Battery"
        capability "Switch"
        capability "Water Sensor"
        capability "Refresh"
        capability "Configuration"

        fingerprint profileId: "0104", deviceId: "0402", inClusters: "0000,0001,0500", outClusters: "0006,0019", manufacturer:"Third Reality, Inc", model:"3RWS18BZ", deviceJoinName: "Water Leak Sensor"		//ThirdReality WaterLeak Sensor
    }

    simulator {
        // When simulating, define status and reply messages here
    }

    tiles {		//Seems no use
        // define your main and details tiles here
        main("water")
        details(["water", "battery", "switch"])
    }
}

// parse events into attributes
def parse(String description) {
    log.trace "[parse] Parsing '${description}'"
    def resMap = [:]

    log.debug "[parse] descMap: $descMap"
    if (description?.startsWith("zone status")) {
        resMap = createEvent(name: "water", value: zigbee.parseZoneStatus(description).isAlarm1Set() ? "wet" : "dry")
        sendEvent(name: "switch", value: zigbee.parseZoneStatus(description).isAlarm1Set() ? "on" : "off")
    } else if (description?.startsWith("on/off")) {
        def event = zigbee.getEvent(description)
        sendEvent(event)
    } else if (description?.startsWith("read attr")) {
    	def descMap = zigbee.parseDescriptionAsMap(description)
        if (descMap?.cluster == "0001" && descMap?.attrId == "0021") {
            resMap = createEvent(getBatteryPercentageResult(Integer.parseInt(descMap.value, 16)))
        } 
        else {
            log.warn "[WARN][parse] Unknown cluster: $descMap.cluster or attrId: $descMap.attrId"
        }
    } else if (description?.startsWith("catchall")) {
    	def descMap = zigbee.parseDescriptionAsMap(description)
        if (descMap?.clusterId == "0500" && descMap?.attrId == "0002") {			//ZoneStatus
            resMap = createEvent(name: "water", value: descMap.value ? "wet" : "dry")
        }
        else if (descMap?.clusterId == "0006" && descMap?.attrId == "0000") {			//On/Off
            sendEvent(name: "switch", value: descMap.value ? "off" : "on")
        }
        else {
            log.warn "[WARN][parse] Unknown clusterId: $descMap.clusterId or attrId: $descMap.attrId"
        }
    } 
    else {
        log.warn "[WARN][parse] Unknown description: $description"
    }

    log.debug "[parse] return '${resMap}'"
    return resMap
}

// handle commands
def configure() {
    log.trace "[configure]"
    return zigbee.readAttribute(zigbee.POWER_CONFIGURATION_CLUSTER, 0x0021) + zigbee.readAttribute(zigbee.IAS_ZONE_CLUSTER,zigbee.ATTRIBUTE_IAS_ZONE_STATUS) + zigbee.readAttribute(0x0006, 0x0000)
}

def refresh() {
    log.trace "[refresh]"
    return zigbee.readAttribute(zigbee.POWER_CONFIGURATION_CLUSTER, 0x0021) + zigbee.readAttribute(zigbee.IAS_ZONE_CLUSTER,zigbee.ATTRIBUTE_IAS_ZONE_STATUS) + zigbee.readAttribute(0x0006, 0x0000)
}

def on() {
    log.trace "[on]"
    zigbee.on()
}

def off() {
    log.trace "[off]"
    zigbee.off()
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
