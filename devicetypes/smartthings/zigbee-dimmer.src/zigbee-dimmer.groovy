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
    definition (name: "ZigBee Dimmer", namespace: "smartthings", author: "SmartThings") {
        capability "Actuator"
        capability "Configuration"
        capability "Refresh"
        capability "Switch"
        capability "Switch Level"
        capability "Health Check"
        capability "Light"


        fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0008"
        fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0008, 0B04, FC0F", outClusters: "0019", manufacturer: "OSRAM", model: "LIGHTIFY A19 ON/OFF/DIM", deviceJoinName: "SYLVANIA Smart A19 Soft White"
        fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0008, FF00", outClusters: "0019", manufacturer: "MRVL", model: "MZ100", deviceJoinName: "Wemo Bulb"
        fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0008, 0B05", outClusters: "0019", manufacturer: "OSRAM SYLVANIA", model: "iQBR30", deviceJoinName: "Sylvania Ultra iQ"
        fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0008, FC0F", outClusters: "0019", manufacturer: "OSRAM", model: "LIGHTIFY PAR38 ON/OFF/DIM", deviceJoinName: "SYLVANIA Smart PAR38 Soft White"
        fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0008, 0B04, FC0F", outClusters: "0019", manufacturer: "OSRAM", model: "LIGHTIFY BR ON/OFF/DIM", deviceJoinName: "SYLVANIA Smart BR30 Soft White"
        fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0008, 0702, 0B05", outClusters: "0019", manufacturer: "sengled", model: "E11-G13", deviceJoinName: "Sengled Element Classic"
        fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0008", outClusters: "0003, 0006, 0008, 0019, 0406", manufacturer: "Leviton", model: "DL6HD", deviceJoinName: "Leviton Dimmer Switch"
        fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0008", outClusters: "0003, 0006, 0008, 0019, 0406", manufacturer: "Leviton", model: "DL3HL", deviceJoinName: "Leviton Lumina RF Plug-In Dimmer"
        fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0008", outClusters: "0003, 0006, 0008, 0019, 0406", manufacturer: "Leviton", model: "DL1KD", deviceJoinName: "Leviton Lumina RF Dimmer Switch"
    }

    tiles(scale: 2) {
        multiAttributeTile(name:"switch", type: "lighting", width: 6, height: 4, canChangeIcon: true){
            tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
                attributeState "on", label:'${name}', action:"switch.off", icon:"st.switches.light.on", backgroundColor:"#79b821", nextState:"turningOff"
                attributeState "off", label:'${name}', action:"switch.on", icon:"st.switches.light.off", backgroundColor:"#ffffff", nextState:"turningOn"
                attributeState "turningOn", label:'${name}', action:"switch.off", icon:"st.switches.light.on", backgroundColor:"#79b821", nextState:"turningOff"
                attributeState "turningOff", label:'${name}', action:"switch.on", icon:"st.switches.light.off", backgroundColor:"#ffffff", nextState:"turningOn"
            }
            tileAttribute ("device.level", key: "SLIDER_CONTROL") {
                attributeState "level", action:"switch level.setLevel"
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
        if (event.name=="level" && event.value==0) {}
        else {
            sendEvent(event)
        }
    }
    else {
        def cluster = zigbee.parse(description)

        if (cluster && cluster.clusterId == 0x0006 && cluster.command == 0x07) {
            if (cluster.data[0] == 0x00) {
                log.debug "ON/OFF REPORTING CONFIG RESPONSE: " + cluster
                sendEvent(name: "checkInterval", value: 60 * 12, displayed: false, data: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID])
            }
            else {
                log.warn "ON/OFF REPORTING CONFIG FAILED- error code:${cluster.data[0]}"
            }
        }
        else {
            log.warn "DID NOT PARSE MESSAGE for description : $description"
            log.debug "${cluster}"
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
    def additionalCmds = []
    if (device.getDataValue("model") == "iQBR30" && value.toInteger() > 0) { // Handle iQ bulb not following spec
        additionalCmds = zigbee.on()
    } else if (device.getDataValue("manufacturer") == "MRVL") { // Handle marvel stack not reporting
        additionalCmds = refresh()
    }
    zigbee.setLevel(value) + additionalCmds
}
/**
 * PING is used by Device-Watch in attempt to reach the Device
 * */
def ping() {
    return zigbee.onOffRefresh()
}

def refresh() {
    zigbee.onOffRefresh() + zigbee.levelRefresh()
}

def installed() {
    if ((device.getDataValue("manufacturer") == "MRVL") && (device.getDataValue("model") == "MZ100")) {
        sendEvent(name: "level", value: 100)
    }
}

def configure() {
    log.debug "Configuring Reporting and Bindings."
    // Device-Watch allows 2 check-in misses from device + ping (plus 1 min lag time)
    // enrolls with default periodic reporting until newer 5 min interval is confirmed
    sendEvent(name: "checkInterval", value: 2 * 10 * 60 + 1 * 60, displayed: false, data: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID])

    // OnOff minReportTime 0 seconds, maxReportTime 5 min. Reporting interval if no activity
    refresh() + zigbee.onOffConfig(0, 300) + zigbee.levelConfig()
}
