/*
 *  Copyright 2018 SmartThings
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
 *  Date : 2018-08-29
 */

metadata {
    definition (name: "ZigBee Multi Switch", namespace: "smartthings", author: "SmartThings", ocfDeviceType: "oic.d.switch", vid: "generic-switch" ) {
        capability "Actuator"
        capability "Configuration"
        capability "Refresh"
        capability "Health Check"
        capability "Switch"

        command "childOn", ["string"]
        command "childOff", ["string"]

        fingerprint  profileId: "0104", inClusters: "0000, 0005, 0004, 0006", manufacturer: "0000000", model: "cc891c853291418daa4e26cb315a3a0c", deviceJoinName: "Orvibo Smart Switch 1"
    }
    // simulator metadata
    simulator {
        // status messages
        status "on": "on/off: 1"
        status "off": "on/off: 0"

        // reply messages
        reply "zcl on-off on": "on/off: 1"
        reply "zcl on-off off": "on/off: 0"
    }

    tiles(scale: 2) {
        multiAttributeTile(name:"switch", type: "lighting", width: 6, height: 4, canChangeIcon: true){
            tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
                attributeState "on", label:'${name}', action:"switch.off", icon:"st.switches.light.on", backgroundColor:"#00A0DC", nextState:"turningOff"
                attributeState "off", label:'${name}', action:"switch.on", icon:"st.switches.light.off", backgroundColor:"#ffffff", nextState:"turningOn"
                attributeState "turningOn", label:'${name}', action:"switch.off", icon:"st.switches.light.on", backgroundColor:"#00A0DC", nextState:"turningOff"
                attributeState "turningOff", label:'${name}', action:"switch.on", icon:"st.switches.light.off", backgroundColor:"#ffffff", nextState:"turningOn"
            }
        }
        standardTile("refresh", "device.refresh", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
            state "default", label:"", action:"refresh.refresh", icon:"st.secondary.refresh"
        }
        main "switch"
        details(["switch", "refresh"])
    }
}

def installed() {
    createChildDevices()
}

// Parse incoming device messages to generate events
def parse(String description) {
    log.debug "description is $description"
    Map map = zigbee.getEvent(description)
    if (map) {
        if (description?.startsWith('on/off')) {
            log.debug "receive on/off message without endpoint id"
            sendHubCommand(refresh().collect { new physicalgraph.device.HubAction(it) }, 0)
        } else {
            Map descMap = zigbee.parseDescriptionAsMap(description)
            log.debug "$descMap"

            if (descMap?.clusterId == "0006" && descMap.sourceEndpoint == "01") {
                def result = map ? createEvent(map) : [:]
                sendEvent(result)
             } else if (descMap?.clusterId == "0006") {
                def result = map ? createEvent(map) : [:]
                def childDevice = childDevices.find {
                    it.deviceNetworkId == "$device.deviceNetworkId:${descMap.sourceEndpoint}"
                }
                if (childDevice && result) {
                    childDevice.sendEvent(result)
                }
            }
        }
    }
}

private void createChildDevices() {
    def i = 2
    addChildDevice("Child Switch", "${device.deviceNetworkId}:0${i}", null,
                     [completedSetup: true, label: "${device.displayName.split("1")[-1]} ${i}",
                     isComponent: false, componentName: "ch$i", componentLabel: "Channel $i"])

}

private getChildEndpoint(String dni) {
    dni.split(":")[-1] as Integer
}

def on() {
    log.debug("on")
    zigbee.on()
}

def off() {
    log.debug("off")
    zigbee.off()
}

def childOn(String dni) {
    log.debug(" child on ${dni}")
    zigbee.command(0x0006, 0x01, "", [destEndpoint: getChildEndpoint(dni)])
}

def childOff(String dni) {
    log.debug(" child off ${dni}")
    zigbee.command(0x0006, 0x00, "", [destEndpoint: getChildEndpoint(dni)])
}

/**
* PING is used by Device-Watch in attempt to reach the Device
* */
def ping() {
    return refresh()
}

def refresh()
{
    log.debug "refresh broadcast"
    return zigbee.readAttribute(0x0006, 0x0000, [destEndpoint: 0xFF])
}

def configure() {
    log.debug "config"
    // Device-Watch allows 2 check-in misses from device + ping (plus 2 min lag time)
    sendEvent(name: "checkInterval", value: 2 * 10 * 60 + 2 * 60, displayed: false, data: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID])

    return zigbee.configureReporting(0x0006, 0x0000, 0x10, 0, 600, null, [destEndpoint: 0x01]) +
                zigbee.configureReporting(0x0006, 0x0000, 0x10, 0, 600, null, [destEndpoint: 0x02]) +
                zigbee.readAttribute(0x0006, 0x0000,[destEndpoint: 0xFF])
}

