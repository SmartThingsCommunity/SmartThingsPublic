/*
 *  Copyright 2019 SmartThings
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
 *  Author : Fen Mei / f.mei@samsung.com
 *  Date : 2019-02-12
 */
metadata {
    definition(name: "Zigbee Plugin Motion Sensor", namespace: "smartthings", author: "SmartThings", runLocally: false, mnmn: "SmartThings", vid: "SmartThings-smartthings-ZigBee_Dimmer_with_Motion_Sensor") {
        capability "Motion Sensor"
        capability "Configuration"
        capability "Refresh"
        capability "Health Check"
        capability "Sensor"
       
        fingerprint profileId: "0104", deviceId: "0107", inClusters: "0000, 0003, 0004, 0406", outClusters:"0006, 0019", manufacturer:"", model:"E280-KR0A0Z0-HA", deviceJoinName: "Smart Occupancy Sensor (AC Type)"
    }
    tiles(scale: 2) {
        multiAttributeTile(name: "motion", type: "generic", width: 6, height: 4) {
            tileAttribute("device.motion", key: "PRIMARY_CONTROL") {
                attributeState "active", label: 'motion', icon: "st.motion.motion.active", backgroundColor: "#00A0DC"
                attributeState "inactive", label: 'no motion', icon: "st.motion.motion.inactive", backgroundColor: "#cccccc"
            }
        }

        standardTile("refresh", "device.refresh", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
            state "default", action: "refresh.refresh", icon: "st.secondary.refresh"
        }
        main(["motion"])
        details(["motion", "refresh"])
    }
}

def installed(){
    log.debug "installed"
    response(refresh())
}

def parse(String description) {
    log.debug "description(): $description"
    def map = zigbee.getEvent(description)
    if (!map) {
        def descMap = zigbee.parseDescriptionAsMap(description)
        if (descMap.clusterInt == 0x0406 && descMap.attrInt == 0x0000) { 
             map.name = "motion"
             map.value = descMap.value.endsWith("01") ? "active" : "inactive"
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

/**
 * PING is used by Device-Watch in attempt to reach the Device
 * */
def ping() {
    log.debug "ping "
    refresh()
}

def refresh() {
    log.debug "Refreshing Values"
    zigbee.readAttribute(0x0406, 0x0000)
}

def configure() {
    log.debug "configure"
    //this device will send occupancy status every 5 minutes
    sendEvent(name: "checkInterval", value: 10 * 60 + 2 * 60, displayed: false, data: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID, offlinePingable: "1"])
    return refresh()
}
