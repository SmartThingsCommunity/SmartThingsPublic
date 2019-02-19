/*
 *  eZEX Temp & Humidity Sensor (AC Type)
 *
 *  Copyright 2019 SmartThings
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
import physicalgraph.zigbee.zcl.DataType

metadata {
    definition(name: "eZEX Temp & Humidity Sensor", namespace: "smartthings", author: "SmartThings", mnmn:"SmartThings", vid:"generic-humidity") {
        capability "Configuration"
        capability "Temperature Measurement"
        capability "Relative Humidity Measurement"
        capability "Sensor"
        
        fingerprint profileId: "0104", inClusters: "0000,0003,0402,0405,0500", outClusters: "0019", model: "E282-KR0B0Z1-HA", deviceJoinName: "Smart Temperature/Humidity Sensor (AC Type)"
    }


    preferences {
        input title: "Temperature Offset", description: "This feature allows you to correct any temperature variations by selecting an offset. Ex: If your sensor consistently reports a temp that's 5 degrees too warm, you'd enter \"-5\". If 3 degrees too cold, enter \"+3\".", displayDuringSetup: false, type: "paragraph", element: "paragraph"
        input "tempOffset", "number", title: "Degrees", description: "Adjust temperature by this many degrees", range: "*..*", displayDuringSetup: false
        input title: "Humidity Offset", description: "This feature allows you to correct any humidity variations by selecting an offset. Ex: If your sensor consistently reports a humidity that's 6% higher then a similiar calibrated sensor, you'd enter \"-6\".", displayDuringSetup: false, type: "paragraph", element: "paragraph"
        input "humidityOffset", "number", title: "Humidity Offset in Percent", description: "Adjust humidity by this percentage", range: "*..*", displayDuringSetup: false
    }

    tiles(scale: 2) {
        multiAttributeTile(name: "temperature", type: "generic", width: 6, height: 4, canChangeIcon: true) {
            tileAttribute("device.temperature", key: "PRIMARY_CONTROL") {
                attributeState "temperature", label: '${currentValue}°',
                        backgroundColors: [
                                [value: 31, color: "#153591"],
                                [value: 44, color: "#1e9cbb"],
                                [value: 59, color: "#90d2a7"],
                                [value: 74, color: "#44b621"],
                                [value: 84, color: "#f1d801"],
                                [value: 95, color: "#d04e00"],
                                [value: 96, color: "#bc2323"]
                        ]
            }
        }
        valueTile("humidity", "device.humidity", inactiveLabel: false, width: 2, height: 2) {
            state "humidity", label: '${currentValue}% humidity', unit: ""
        }
        standardTile("refresh", "device.refresh", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
            state "default", action: "refresh.refresh", icon: "st.secondary.refresh"
        }

        main "temperature", "humidity"
        details(["temperature", "humidity", "refresh"])
    }
}

def parse(String description) {
    log.debug "description: $description"

    // getEvent will handle temperature and humidity
    Map map = zigbee.getEvent(description)
    if (!map) {
        Map descMap = zigbee.parseDescriptionAsMap(description)
        if (descMap?.clusterInt == zigbee.TEMPERATURE_MEASUREMENT_CLUSTER && descMap.commandInt == 0x07) {
            if (descMap.data[0] == "00") {
                log.debug "TEMP REPORTING CONFIG RESPONSE: $descMap"
                sendEvent(name: "checkInterval", value: 60 * 12, displayed: false, data: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID])
            } else {
                log.warn "TEMP REPORTING CONFIG FAILED- error code: ${descMap.data[0]}"
            }
        }
    } else if (map.name == "temperature") {
        if (tempOffset) {
            map.value = (int) map.value + (int) tempOffset
        }
        map.descriptionText = temperatureScale == 'C' ? '{{ device.displayName }} was {{ value }}°C' : '{{ device.displayName }} was {{ value }}°F'
        map.translatable = true
    } else if (map.name == "humidity") {
        if (humidityOffset) {
            map.value = (int) map.value + (int) humidityOffset
        }
    }

    log.debug "Parse returned $map"
    return map ? createEvent(map) : [:]
}

/**
 * PING is used by Device-Watch in attempt to reach the Device
 * */
def ping() {
    return refresh()
}

def refresh() {
    log.debug "refresh temperature, humidity"
    return zigbee.readAttribute(zigbee.RELATIVE_HUMIDITY_CLUSTER, 0x0000) +
           zigbee.readAttribute(zigbee.TEMPERATURE_MEASUREMENT_CLUSTER, 0x0000)
}

def configure() {
    // Device-Watch allows 2 check-in misses from device + ping (plus 1 min lag time)
    sendEvent(name: "checkInterval", value: 2 * 60 * 60 + 1 * 60, displayed: false, data: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID])

    log.debug "Configuring Reporting and Bindings."

    // temperature minReportTime 30 seconds, maxReportTime 1 hour. Reporting interval if no activity
    return refresh() +
           zigbee.configureReporting(zigbee.RELATIVE_HUMIDITY_CLUSTER, 0x0000, DataType.UINT16, 30, 3600, 100) +
           zigbee.configureReporting(zigbee.TEMPERATURE_MEASUREMENT_CLUSTER, 0x0000, DataType.UINT16, 30, 3600, 100)
}
