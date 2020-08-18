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
    definition(name: "Zigbee Non-Holdable Button", namespace: "smartthings", author: "SmartThings", runLocally: false, mnmn: "SmartThings", vid: "generic-button-2", ocfDeviceType: "x.com.st.d.remotecontroller") {
        capability "Configuration"
        capability "Battery"
        capability "Refresh"
        capability "Button"
        capability "Health Check"
        capability "Sensor"

        fingerprint profileId: "0104", inClusters: "0000, 0001, 0003, 0500", outClusters: "0019", manufacturer: "HEIMAN", model: "SOS-EM", deviceJoinName: "HEIMAN Button" //HEIMAN Emergency Button
    }

    tiles(scale: 2) {
        multiAttributeTile(name: "button", type: "generic", width: 6, height: 4) {
            tileAttribute("device.button", key: "PRIMARY_CONTROL") {
                attributeState "pushed", label: "Pressed", icon:"st.Weather.weather14", backgroundColor:"#53a7c0"
            }
        }
        valueTile("battery", "device.battery", decoration: "flat", inactiveLabel: false, width: 2, height: 2) {
            state "battery", label: '${currentValue}% battery', unit: ""
        }
        standardTile("refresh", "device.refresh", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
            state "default", action: "refresh.refresh", icon: "st.secondary.refresh"
        }

        main(["button"])
        details(["button", "battery", "refresh"])
    }
}

def installed() {
    sendEvent(name: "supportedButtonValues", value: ["pushed"].encodeAsJSON(), displayed: false)
    sendEvent(name: "numberOfButtons", value: 1, displayed: false)
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

    Map map = zigbee.getEvent(description)
    if (!map) {
        if (description?.startsWith('zone status')) {
            map = parseIasMessage(description)
        } else {
            Map descMap = zigbee.parseDescriptionAsMap(description)

            if (descMap?.clusterInt == zigbee.POWER_CONFIGURATION_CLUSTER && descMap.commandInt != 0x07 && descMap.value) {
                List<Map> descMaps = collectAttributes(descMap)
                def battMap = descMaps.find { it.attrInt == 0x0020 }

                if (battMap) {
                    map = getBatteryResult(Integer.parseInt(battMap.value, 16))
                }
                
            } else if (descMap?.clusterInt == 0x0500 && descMap.attrInt == 0x0002) {
                def zs = new ZoneStatus(zigbee.convertToInt(descMap.value, 16))
                map = translateZoneStatus(zs)
            } else if (descMap?.clusterInt == zigbee.IAS_ZONE_CLUSTER && descMap.attrInt == zigbee.ATTRIBUTE_IAS_ZONE_STATUS && descMap?.value) {
                map = translateZoneStatus(new ZoneStatus(zigbee.convertToInt(descMap?.value)))
            }
        }
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
	if (zs.isAlarm1Set()) {
        return getButtonResult('pushed')
    }
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

        def minVolts = 2.1
        def maxVolts = 3.0
        def pct = (volts - minVolts) / (maxVolts - minVolts)
        def roundedPct = Math.round(pct * 100)
        if (roundedPct <= 0)
            roundedPct = 1
        result.value = Math.min(100, roundedPct)
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

    refreshCmds += zigbee.readAttribute(zigbee.POWER_CONFIGURATION_CLUSTER, 0x0020) +
                   zigbee.readAttribute(zigbee.IAS_ZONE_CLUSTER, zigbee.ATTRIBUTE_IAS_ZONE_STATUS) +
                   zigbee.enrollResponse()
    return refreshCmds
}

def configure() {
    // Device-Watch allows 2 check-in misses from device + ping (plus 1 min lag time)
    // enrolls with default periodic reporting until newer 5 min interval is confirmed
    // Sets up low battery threshold reporting
    sendEvent(name: "DeviceWatch-Enroll", displayed: false, value: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID, scheme: "TRACKED", checkInterval: 6 * 60 * 60 + 1 * 60, offlinePingable: "1"].encodeAsJSON())

    return zigbee.readAttribute(zigbee.POWER_CONFIGURATION_CLUSTER, 0x0020) +
		   zigbee.enrollResponse() + 
		   zigbee.batteryConfig()
}
