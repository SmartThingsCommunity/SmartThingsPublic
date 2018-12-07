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
    definition (name: "ZigBee Dimmer Power", namespace: "smartthings", author: "SmartThings", ocfDeviceType: "oic.d.light", runLocally: true, minHubCoreVersion: '000.019.00012', executeCommandsLocally: true) {
        capability "Actuator"
        capability "Configuration"
        capability "Refresh"
        capability "Power Meter"
        capability "Sensor"
        capability "Switch"
        capability "Switch Level"
        capability "Health Check"
        capability "Light"

        fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0008, 0B04"
        fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0008, 0702"
        fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0008, 0702, 0B05", outClusters: "0019", manufacturer: "sengled", model: "Z01-CIA19NAE26", deviceJoinName: "Sengled Element touch"
        fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0008, 0702, 0B05", outClusters: "000A, 0019", manufacturer: "Jasco Products", model: "45852", deviceJoinName: "GE Zigbee Plug-In Dimmer"
        fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0008, 0702, 0B05", outClusters: "000A, 0019", manufacturer: "Jasco Products", model: "45857", deviceJoinName: "GE Zigbee In-Wall Dimmer"
    }

    tiles(scale: 2) {
        multiAttributeTile(name:"switch", type: "lighting", width: 6, height: 4, canChangeIcon: true){
            tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
                attributeState "on", label:'${name}', action:"switch.off", icon:"st.switches.switch.on", backgroundColor:"#00A0DC", nextState:"turningOff"
                attributeState "off", label:'${name}', action:"switch.on", icon:"st.switches.switch.off", backgroundColor:"#ffffff", nextState:"turningOn"
                attributeState "turningOn", label:'${name}', action:"switch.off", icon:"st.switches.switch.on", backgroundColor:"#00A0DC", nextState:"turningOff"
                attributeState "turningOff", label:'${name}', action:"switch.on", icon:"st.switches.switch.off", backgroundColor:"#ffffff", nextState:"turningOn"
            }
            tileAttribute ("device.level", key: "SLIDER_CONTROL") {
                attributeState "level", action:"switch level.setLevel"
            }
            tileAttribute ("power", key: "SECONDARY_CONTROL") {
                attributeState "power", label:'${currentValue} W'
            }
        }
        standardTile("refresh", "device.refresh", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
            state "default", label:"", action:"refresh.refresh", icon:"st.secondary.refresh"
        }
        main "switch"
        details(["switch", "refresh"])
    }
}

// Parse incoming device messages to generate events
def parse(String description) {
    log.debug "description is $description"

    def event = zigbee.getEvent(description)
    if (event) {
        log.info event
        if (event.name == "power") {
            if (device.getDataValue("manufacturer") != "OSRAM") {       //OSRAM devices do not reliably update power
                event.value = (event.value as Integer) / 10             //TODO: The divisor value needs to be set as part of configuration
                sendEvent(event)
            }
        }
        else {
            sendEvent(event)
        }
    } else {
        def descMap = zigbee.parseDescriptionAsMap(description)
        if (descMap && descMap.clusterInt == 0x0006 && descMap.commandInt == 0x07) {
            if (descMap.data[0] == "00") {
                log.debug "ON/OFF REPORTING CONFIG RESPONSE: " + cluster
                sendEvent(name: "checkInterval", value: 60 * 12, displayed: false, data: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID])
            } else {
                log.warn "ON/OFF REPORTING CONFIG FAILED- error code:${cluster.data[0]}"
            }
        } else if (device.getDataValue("manufacturer") == "sengled" && descMap && descMap.clusterInt == 0x0008 && descMap.attrInt == 0x0000) {
            // This is being done because the sengled element touch incorrectly uses the value 0xFF for the max level.
            // Per the ZCL spec for the UINT8 data type 0xFF is an invalid value, and 0xFE should be the max.  Here we
            // manually handle the invalid attribute value since it will be ignored by getEvent as an invalid value.
            // We also set the level of the bulb to 0xFE so future level reports will be 0xFE until it is changed by
            // something else.
            if (descMap.value.toUpperCase() == "FF") {
                descMap.value = "FE"
            }
            sendHubCommand(zigbee.command(zigbee.LEVEL_CONTROL_CLUSTER, 0x00, "FE0000").collect { new physicalgraph.device.HubAction(it) }, 0)
            sendEvent(zigbee.getEventFromAttrData(descMap.clusterInt, descMap.attrInt, descMap.encoding, descMap.value))
        } else {
            log.warn "DID NOT PARSE MESSAGE for description : $description"
            log.debug zigbee.parseDescriptionAsMap(description)
        }
    }
}

def off() {
    zigbee.off()
}

def on() {
    zigbee.on()
}

def setLevel(value) {
    zigbee.setLevel(value) + (value?.toInteger() > 0 ? zigbee.on() : [])
}

/**
 * PING is used by Device-Watch in attempt to reach the Device
 * */
def ping() {
    return zigbee.onOffRefresh()
}

def refresh() {
    def cmds = zigbee.onOffRefresh() + zigbee.levelRefresh() + zigbee.simpleMeteringPowerRefresh() + zigbee.electricMeasurementPowerRefresh()
    if (device.getDataValue("manufacturer") == "Jasco Products") {
        // Some versions of hub firmware will incorrectly remove this binding causing manual control of switch to stop working
        // These needs to be the first binding table entries because the device will automatically write these entries each time it restarts
        cmds += ["zdo bind 0x${device.deviceNetworkId} 2 1 0x0006 {${device.zigbeeId}} {${device.zigbeeId}}", "delay 2000",
                 "zdo bind 0x${device.deviceNetworkId} 2 1 0x0008 {${device.zigbeeId}} {${device.zigbeeId}}", "delay 2000"]
    }
    cmds + zigbee.onOffConfig(0, 300) + zigbee.levelConfig() + zigbee.simpleMeteringPowerConfig() + zigbee.electricMeasurementPowerConfig()
}

def configure() {
    log.debug "Configuring Reporting and Bindings."
    def cmds = []
    if (device.getDataValue("manufacturer") == "sengled") {
        def startLevel = 0xFE
        if ((device.currentState("level")?.value != null)) {
            startLevel = Math.round(Integer.parseInt(device.currentState("level").value) * 0xFE / 100)
        }
        // Level Control Cluster, command Move to Level, level start level, transition time 0
        cmds << zigbee.command(zigbee.LEVEL_CONTROL_CLUSTER, 0x00, sprintf("%02X0000", startLevel))
    }
    // Device-Watch allows 2 check-in misses from device + ping (plus 1 min lag time)
    // enrolls with default periodic reporting until newer 5 min interval is confirmed
    sendEvent(name: "checkInterval", value: 2 * 60 * 60 + 1 * 60, displayed: false, data: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID])
    cmds + refresh()
}
