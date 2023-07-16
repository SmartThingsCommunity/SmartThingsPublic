/*
 *  Copyright 2016 SmartThings
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not
 *  use this file except in compliance with the License. You may obtain a copy
 *  of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *  WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 *  License for the specific language governing permissions and limitations
 *  under the License.
 */
import physicalgraph.zigbee.clusters.iaszone.ZoneStatus
import physicalgraph.zigbee.zcl.DataType

metadata {
    definition(name: "SmartSense Button", namespace: "smartthings", author: "SmartThings", runLocally: true, minHubCoreVersion: '000.022.0000', executeCommandsLocally: false, mnmn: "SmartThings", vid: "SmartThings-smartthings-SmartSense_Button", ocfDeviceType: "x.com.st.d.remotecontroller") {
        capability "Configuration"
        capability "Battery"
        capability "Refresh"
        capability "Temperature Measurement"
        capability "Button"
        capability "Holdable Button"
        capability "Health Check"
        capability "Sensor"

        fingerprint inClusters: "0000,0001,0003,0020,0402,0500", outClusters: "0019", manufacturer: "Samjin", model: "button", deviceJoinName: "Button"
    }

    simulator {
        status "button 1 pushed": "catchall: 0104 0500 01 01 0140 00 6C3F 00 00 0000 01 01 020000190100"
    }

    preferences {
        section {
            image(name: 'educationalcontent', multiple: true, images: [
                    "http://cdn.device-gse.smartthings.com/Moisture/Moisture1.png",
                    "http://cdn.device-gse.smartthings.com/Moisture/Moisture2.png",
                    "http://cdn.device-gse.smartthings.com/Moisture/Moisture3.png"
            ])
        }
        section {
            input "tempOffset", "number", title: "Temperature offset", description: "Select how many degrees to adjust the temperature.", range: "-100..100", displayDuringSetup: false
        }
    }

    tiles(scale: 2) {
        multiAttributeTile(name: "button", type: "generic", width: 6, height: 4) {
            tileAttribute("device.button", key: "PRIMARY_CONTROL") {
                attributeState "pushed", label: "Pressed", icon:"st.Weather.weather14", backgroundColor:"#53a7c0"
                attributeState "double", label: "Pressed Twice", icon:"st.Weather.weather11", backgroundColor:"#53a7c0"
                attributeState "held", label: "Held", icon:"st.Weather.weather13", backgroundColor:"#53a7c0"
            }
        }
        valueTile("temperature", "device.temperature", inactiveLabel: false, width: 2, height: 2) {
            state "temperature", label: '${currentValue}°',
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
        valueTile("battery", "device.battery", decoration: "flat", inactiveLabel: false, width: 2, height: 2) {
            state "battery", label: '${currentValue}% battery', unit: ""
        }
        standardTile("refresh", "device.refresh", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
            state "default", action: "refresh.refresh", icon: "st.secondary.refresh"
        }

        main(["button", "temperature"])
        details(["button", "temperature", "battery", "refresh"])
    }
}

def installed() {
    sendEvent(name: "supportedButtonValues", value: ["pushed","held","double"].encodeAsJSON(), displayed: false)
    sendEvent(name: "numberOfButtons", value: 1, displayed: false)
    sendEvent(name: "button", value: "pushed", data: [buttonNumber: 1], displayed: false)
}

private List<Map> collectAttributes(Map descMap) {
    List<Map> descMaps = new ArrayList<Map>()

    descMaps.add(descMap)

    if (descMap.additionalAttrs) {
        descMaps.addAll(descMap.additionalAttrs)
    }

    return  descMaps
}

def parse(String description) {
    log.debug "description: $description"

    // getEvent will handle temperature and humidity
    Map map = zigbee.getEvent(description)
    if (!map) {
        if (description?.startsWith('zone status')) {
            map = parseIasMessage(description)
        } else {
            Map descMap = zigbee.parseDescriptionAsMap(description)

            if (descMap?.clusterInt == zigbee.POWER_CONFIGURATION_CLUSTER && descMap.commandInt != 0x07 && descMap.value) {
                List<Map> descMaps = collectAttributes(descMap)

                if (device.getDataValue("manufacturer") == "Samjin") {
                    def battMap = descMaps.find { it.attrInt == 0x0021 }

                    if (battMap) {
                        map = getBatteryPercentageResult(Integer.parseInt(battMap.value, 16))
                    }
                } else {
                    def battMap = descMaps.find { it.attrInt == 0x0020 }

                    if (battMap) {
                        map = getBatteryResult(Integer.parseInt(battMap.value, 16))
                    }
                }
            } else if (descMap?.clusterInt == 0x0500 && descMap.attrInt == 0x0002) {
                def zs = new ZoneStatus(zigbee.convertToInt(descMap.value, 16))
                map = translateZoneStatus(zs)
            } else if (descMap?.clusterInt == zigbee.TEMPERATURE_MEASUREMENT_CLUSTER && descMap.commandInt == 0x07) {
                if (descMap.data[0] == "00") {
                    log.debug "TEMP REPORTING CONFIG RESPONSE: $descMap"
                    sendEvent(name: "checkInterval", value: 60 * 12, displayed: false, data: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID, offlinePingable: "1"])
                } else {
                    log.warn "TEMP REPORTING CONFIG FAILED- error code: ${descMap.data[0]}"
                }
            } else if (descMap?.clusterInt == zigbee.IAS_ZONE_CLUSTER && descMap.attrInt == zigbee.ATTRIBUTE_IAS_ZONE_STATUS && descMap?.value) {
                map = translateZoneStatus(new ZoneStatus(zigbee.convertToInt(descMap?.value)))
            } 
        }
    } else if (map.name == "temperature") {
        if (tempOffset) {
            map.value = new BigDecimal((map.value as float) + (tempOffset as float)).setScale(1, BigDecimal.ROUND_HALF_UP)
            map.unit = getTemperatureScale()
        }
        map.descriptionText = getTemperatureScale() == 'C' ? "${ device.displayName } was ${ map.value }°C" : "${ device.displayName } was ${ map.value }°F"
        map.translatable = true
    }

    log.debug "Parse returned $map"
    def result = map ? createEvent(map) : [:]

    if (description?.startsWith('enroll request')) {
        List cmds = zigbee.enrollResponse()
        log.debug "enroll response: ${cmds}"
        result = cmds?.collect { new physicalgraph.device.HubAction(it) }
    }
    return result
}

private Map parseIasMessage(String description) {
    ZoneStatus zs = zigbee.parseZoneStatus(description)

    translateZoneStatus(zs)
}

private Map translateZoneStatus(ZoneStatus zs) {
    if (zs.isAlarm1Set() && zs.isAlarm2Set()) {
        return getButtonResult('held')
    } else if (zs.isAlarm1Set()) {
        return getButtonResult('pushed')
    } else if (zs.isAlarm2Set()) {
        return getButtonResult('double')
    } else { }
}

private Map getBatteryResult(rawValue) {
    log.debug "Battery rawValue = ${rawValue}"
    def linkText = getLinkText(device)

    def result = [:]

    def volts = rawValue / 10

    if (!(rawValue == 0 || rawValue == 255)) {
        result.name = 'battery'
        result.translatable = true
        result.descriptionText = "${ device.displayName } battery was ${ value }%"
        if (device.getDataValue("manufacturer") == "SmartThings") {
            volts = rawValue // For the batteryMap to work the key needs to be an int
            def batteryMap = [28: 100, 27: 100, 26: 100, 25: 90, 24: 90, 23: 70,
                              22: 70, 21: 50, 20: 50, 19: 30, 18: 30, 17: 15, 16: 1, 15: 0]
            def minVolts = 15
            def maxVolts = 28

            if (volts < minVolts)
                volts = minVolts
            else if (volts > maxVolts)
                volts = maxVolts
            def pct = batteryMap[volts]
            result.value = pct
        } else {
            def minVolts = 2.1
            def maxVolts = 3.0
            def pct = (volts - minVolts) / (maxVolts - minVolts)
            def roundedPct = Math.round(pct * 100)
            if (roundedPct <= 0)
                roundedPct = 1
            result.value = Math.min(100, roundedPct)
        }

    }

    return result
}

private Map getBatteryPercentageResult(rawValue) {
    log.debug "Battery Percentage rawValue = ${rawValue} -> ${rawValue / 2}%"
    def result = [:]

    if (0 <= rawValue && rawValue <= 200) {
        result.name = 'battery'
        result.translatable = true
        result.descriptionText = "{{ device.displayName }} battery was {{ value }}%"
        result.value = Math.round(rawValue / 2)
    }

    return result
}

private Map getButtonResult(value) {
    def descriptionText
    if (value == "pushed")
        descriptionText = "${ device.displayName } was pushed"
    else if (value == "held")
        descriptionText = "${ device.displayName } was held"
    else
        descriptionText = "${ device.displayName } was pushed twice"
    return [
            name           : 'button',
            value          : value,
            descriptionText: descriptionText,
            translatable   : true,
            isStateChange  : true,
            data           : [buttonNumber: 1]
    ]
}

/**
 * PING is used by Device-Watch in attempt to reach the Device
 * */
def ping() {
    zigbee.readAttribute(zigbee.IAS_ZONE_CLUSTER, zigbee.ATTRIBUTE_IAS_ZONE_STATUS)
}

def refresh() {
    log.debug "Refreshing Values"
    def refreshCmds = []

    if (device.getDataValue("manufacturer") == "Samjin") {
        refreshCmds += zigbee.readAttribute(zigbee.POWER_CONFIGURATION_CLUSTER, 0x0021)
    } else {
        refreshCmds += zigbee.readAttribute(zigbee.POWER_CONFIGURATION_CLUSTER, 0x0020)
    }
    refreshCmds += zigbee.readAttribute(zigbee.TEMPERATURE_MEASUREMENT_CLUSTER, 0x0000) +
        zigbee.readAttribute(zigbee.IAS_ZONE_CLUSTER, zigbee.ATTRIBUTE_IAS_ZONE_STATUS) +
        zigbee.enrollResponse()

    return refreshCmds
}

def configure() {
    // Device-Watch allows 2 check-in misses from device + ping (plus 1 min lag time)
    // enrolls with default periodic reporting until newer 5 min interval is confirmed
    // Sets up low battery threshold reporting
    sendEvent(name: "DeviceWatch-Enroll", displayed: false, value: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID, scheme: "TRACKED", checkInterval: 2 * 60 * 60 + 1 * 60, lowBatteryThresholds: [15, 7, 3], offlinePingable: "1"].encodeAsJSON())

    log.debug "Configuring Reporting"
    def configCmds = []

    // temperature minReportTime 30 seconds, maxReportTime 5 min. Reporting interval if no activity
    // battery minReport 30 seconds, maxReportTime 6 hrs by default
    if (device.getDataValue("manufacturer") == "Samjin") {
        configCmds += zigbee.configureReporting(zigbee.POWER_CONFIGURATION_CLUSTER, 0x0021, DataType.UINT8, 30, 21600, 0x10)
    } else {
        configCmds += zigbee.batteryConfig()
    }
    configCmds += zigbee.temperatureConfig(30, 300)

    return refresh() + configCmds + refresh() // send refresh cmds as part of config
}
