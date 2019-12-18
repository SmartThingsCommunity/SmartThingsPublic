/**
 *  IKEA Motion Sensor
 *
 *  Copyright 2019
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
    definition (name: "Ikea Motion Sensor", namespace: "smartthings", author: "SmartThings", ocfDeviceType: "x.com.st.d.sensor.motion", mnmn: "SmartThings", vid: "generic-motion-2") {
        capability "Battery"
        capability "Configuration"
        capability "Motion Sensor"
        capability "Sensor"
        capability "Health Check"
        capability "Refresh"

        fingerprint inClusters: "0000, 0001, 0003, 0009, 0B05, 1000", outClusters: "0003, 0004, 0006, 0019, 1000", manufacturer: "IKEA of Sweden",  model: "TRADFRI motion sensor", deviceJoinName: "IKEA TRÅDFRI Motion Sensor"
    }

    tiles(scale: 2) {
        multiAttributeTile(name: "motion", type: "generic", width: 6, height: 4) {
            tileAttribute("device.motion", key: "PRIMARY_CONTROL") {
                attributeState "active", label: 'motion', icon: "st.motion.motion.active", backgroundColor: "#00A0DC"
                attributeState "inactive", label: 'no motion', icon: "st.motion.motion.inactive", backgroundColor: "#cccccc"
            }
        }
        valueTile("battery", "device.battery", decoration: "flat", inactiveLabel: false, width: 2, height: 2) {
            state "battery", label: '${currentValue}% battery', unit: ""
        }
        standardTile("refresh", "device.refresh", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
            state "default", action: "refresh.refresh", icon: "st.secondary.refresh"
        }
        main(["motion"])
        details(["motion", "battery", "refresh"])
    }
}

private getCLUSTER_GROUPS() { 0x0004 }
private getON_WITH_TIMED_OFF_COMMAND() { 0x42 }
private getBATTERY_VOLTAGE_ATTR() { 0x0020 }

def installed() {
    sendEvent(name: "motion", value: "inactive", displayed: false,)
    sendEvent(name: "checkInterval", value: 12 * 60 * 60 + 12 * 60, displayed: false, data: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID])
}

def configure() {
    log.debug "Configuring device ${device.getDataValue("model")}"

    zigbee.readAttribute(zigbee.POWER_CONFIGURATION_CLUSTER, BATTERY_VOLTAGE_ATTR) + zigbee.batteryConfig() +
            readDeviceBindingTable()
}

def refresh() {
    zigbee.readAttribute(zigbee.POWER_CONFIGURATION_CLUSTER, BATTERY_VOLTAGE_ATTR)
}

def ping() {
    refresh()
}

def parse(String description) {
    log.debug "Parsing message from device: '$description'"
    def event = zigbee.getEvent(description)
    if (event) {
        log.debug "Creating event: ${event}"
        sendEvent(event)
    } else {
        if (isBindingTableMessage(description)) {
            parseBindingTableMessage(description)
        } else if (isAttrOrCmdMessage(description)) {
            parseAttrCmdMessage(description)
        } else {
            log.warn "Unhandled message came in"
        }
    }
}

private Map parseAttrCmdMessage(description) {
    def descMap = zigbee.parseDescriptionAsMap(description)
    log.debug "Message description map: ${descMap}"
    if (descMap.clusterInt == zigbee.POWER_CONFIGURATION_CLUSTER && descMap.attrInt == BATTERY_VOLTAGE_ATTR) {
        getBatteryEvent(zigbee.convertHexToInt(descMap.value))
    } else if (descMap.clusterInt == zigbee.ONOFF_CLUSTER && descMap.commandInt == ON_WITH_TIMED_OFF_COMMAND) {
        getMotionEvent(descMap)
    }
}

private Map getMotionEvent(descMap) {
    // User can manually adjust time (1 - 10 minutes) in which motion event will be cleared
    // Depending on that setting, device will send payload in range 600 - 6000
    def onTime = Integer.parseInt(descMap.data[2] + descMap.data[1], 16) / 10
    runIn(onTime, "clearMotionStatus", [overwrite: true])

    createEvent([
            name: "motion",
            value: "active",
            descriptionText: "${device.displayName} detected motion"
    ])
}

private def parseBindingTableMessage(description) {
    Integer groupAddr = getGroupAddrFromBindingTable(description)
    List cmds = []
    if (groupAddr) {
        cmds += addHubToGroup(groupAddr)
    } else {
        groupAddr = 0x0000
        cmds += addHubToGroup(groupAddr)
        cmds += zigbee.command(CLUSTER_GROUPS, 0x00, "${zigbee.swapEndianHex(zigbee.convertToHexString(groupAddr, 4))} 00")
    }
    cmds?.collect { new physicalgraph.device.HubAction(it) }
}

def clearMotionStatus() {
    sendEvent(name: "motion", value: "inactive", descriptionText: "${device.displayName} motion has stopped")
}

private Map getBatteryEvent(rawValue) {
    Map event = [:]
    def volts = rawValue / 10
    if (volts > 0 && rawValue != 0xFF) {
        event = [name: "battery"]
        def minVolts = 2.1
        def maxVolts = 3.0
        def pct = (volts - minVolts) / (maxVolts - minVolts)
        event.value = Math.min(100, (int) (pct * 100))
        def linkText = getLinkText(device)
        event.descriptionText = "${linkText} battery was ${event.value}%"
    }
    createEvent(event)
}

private Integer getGroupAddrFromBindingTable(description) {
    log.info "Parsing binding table - '$description'"
    def btr = zigbee.parseBindingTableResponse(description)
    def groupEntry = btr?.table_entries?.find { it.dstAddrMode == 1 }
    if (groupEntry != null) {
        log.info "Found group binding in the binding table: ${groupEntry}"
        Integer.parseInt(groupEntry.dstAddr, 16)
    } else {
        log.info "The binding table does not contain a group binding"
        null
    }
}

private List addHubToGroup(Integer groupAddr) {
    ["st cmd 0x0000 0x01 ${CLUSTER_GROUPS} 0x00 {${zigbee.swapEndianHex(zigbee.convertToHexString(groupAddr,4))} 00}",
     "delay 200"]
}

private List readDeviceBindingTable() {
    ["zdo mgmt-bind 0x${device.deviceNetworkId} 0",
     "delay 200"]
}

private boolean isAttrOrCmdMessage(description) {
    (description?.startsWith("catchall:")) || (description?.startsWith("read attr -"))
}
