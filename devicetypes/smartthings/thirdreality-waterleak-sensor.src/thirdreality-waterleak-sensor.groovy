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

        fingerprint profileId: "0104", deviceId: "0402", inClusters: "0000,0001,0500", outClusters: "0006,0019", manufacturer:"Third Reality, Inc", model:"3RWS18BZ", deviceJoinName: "ThirdReality Water Leak Sensor"		//ThirdReality WaterLeak Sensor
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

def getBIND_CLUSTER() {0x8021}

// parse events into attributes
def parse(String description) {
    log.trace "[parse] Parsing '${description}'"
    def resMap = [:]

    if (description?.startsWith("zone status")) {
        resMap = createEvent(name: "water", value: zigbee.parseZoneStatus(description).isAlarm1Set() ? "wet" : "dry")
        if (getDataValue("buzzing_state") != "on") {
            sendEvent(name: "switch", value: zigbee.parseZoneStatus(description).isAlarm1Set() ? "on":"off")
        }
    } else if (description?.startsWith("on/off")) {
        resMap = zigbee.getEvent(description)
        updateDataValue("buzzing_state", resMap.value)
        sendEvent(resMap)
    } else if (description?.startsWith("read attr") || description?.startsWith("catchall")) {
        def descMap = zigbee.parseDescriptionAsMap(description)
        log.trace "[parse] descMap: ${descMap}"

        if (descMap?.clusterInt == zigbee.IAS_ZONE_CLUSTER && descMap?.attrInt == zigbee.ATTRIBUTE_IAS_ZONE_STATUS) {           //Water: Zone Status
            resMap = createEvent(name: "water", value: (descMap.value=="0000") ? "dry" : "wet")
        } else if (descMap?.clusterInt == zigbee.POWER_CONFIGURATION_CLUSTER ) {                                                //Battery: Power Config
            if (descMap?.attrInt == 0x0021) {
        	    resMap = createEvent(getBatteryPercentageResult(Integer.parseInt(descMap.value, 16)))
            } else if (descMap?.attrInt == 0x0020) {
                log.debug "[parse] Got Battery Voltage Value: 0x${descMap.value} * 100mV"
            }
        } else if (descMap?.clusterInt == zigbee.ONOFF_CLUSTER) {
            if (descMap?.attrInt == 0x0000) {                                                                                   //Switch: On/Off
                resMap = createEvent(name: "switch", value: (descMap.value=="00") ? "off" : "on")
            } else if (descMap?.commandInt == 0x0B) {
                log.trace "[parse] Cmd On/Off"
            }
        } else if (descMap?.clusterInt == BIND_CLUSTER) {                                                                             //Bind Rsp
            log.trace "[parse] got Bind Rsp"
        } else {
            log.warn "[WARN][parse] Unknown descMap: $descMap"
        }
    } else {
        log.warn "[WARN][parse] Unknown description: $description"
    }

    log.trace "[parse] return '${resMap}'"
    return resMap
}

// handle commands
def configure() {
    log.trace "[configure]"
    updateDataValue("buzzing_state", "off")
    def enrollCmds = zigbee.readAttribute(zigbee.POWER_CONFIGURATION_CLUSTER, 0x0021) + zigbee.readAttribute(zigbee.IAS_ZONE_CLUSTER,zigbee.ATTRIBUTE_IAS_ZONE_STATUS) + zigbee.readAttribute(0x0006, 0x0000)
    return zigbee.addBinding(zigbee.IAS_ZONE_CLUSTER) + enrollCmds
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
