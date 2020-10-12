/*
 *  Copyright 2020 SmartThings
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not
 *  use this file except in compliance with the License. You may obtain a copy
 *  of the License at:
 *
 *		http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *  WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 *  License for the specific language governing permissions and limitations
 *  under the License.
 *
 *  author : fuls@naver.com
 */
public static String version() { return "v0.0.1.20200424" }
/*
 *   2020/04/24 >>> v0.0.1.20200424 - Initialize
 */
metadata {
    definition(name: "Zemi ZigBee Switch", namespace: "zemismart", author: "Onaldo", ocfDeviceType: "oic.d.switch", vid: "generic-switch") {
        capability "Actuator"
        capability "Configuration"
        capability "Refresh"
        capability "Health Check"
        capability "Switch"

        command "childOn", ["string"]
        command "childOff", ["string"]

        // Zemi ZigBee Multi Switch
        fingerprint endpointId: "0x10", profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006", manufacturer: "Feibit Inc co.", model: "FB56+ZSW1GKJ2.7", deviceJoinName: "Zemi Zigbee Switch"
        fingerprint endpointId: "0x0B", profileId: "C05E", inClusters: "0000, 0004, 0003, 0006, 0005, 1000, 0008", outClusters: "0019", manufacturer: "FeiBit", model: "FNB56-ZSW02LX2.0", deviceJoinName: "Zemi Zigbee Switch 1"
        fingerprint endpointId: "0x01", profileId: "C05E", inClusters: "0000, 0004, 0003, 0006, 0005, 1000, 0008", outClusters: "0019", manufacturer: "FeiBit", model: "FNB56-ZSW03LX2.0", deviceJoinName: "Zemi Zigbee Switch 1"
    }

    preferences {
        input type: "paragraph", element: "paragraph", title: "Version", description: version(), displayDuringSetup: false
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
        multiAttributeTile(name: "switch", type: "lighting", width: 6, height: 4, canChangeIcon: true) {
            tileAttribute("device.switch", key: "PRIMARY_CONTROL") {
                attributeState "on", label: '${name}', action: "switch.off", icon: "st.switches.light.on", backgroundColor: "#00A0DC", nextState: "turningOff"
                attributeState "off", label: '${name}', action: "switch.on", icon: "st.switches.light.off", backgroundColor: "#ffffff", nextState: "turningOn"
                attributeState "turningOn", label: '${name}', action: "switch.off", icon: "st.switches.light.on", backgroundColor: "#00A0DC", nextState: "turningOff"
                attributeState "turningOff", label: '${name}', action: "switch.on", icon: "st.switches.light.off", backgroundColor: "#ffffff", nextState: "turningOn"
            }
        }
        standardTile("refresh", "device.refresh", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
            state "default", label: "", action: "refresh.refresh", icon: "st.secondary.refresh"
        }
        main "switch"
        details(["switch", "refresh"])
    }
}

def installed() {
    log.debug "installed()"
    if (parent) {
        setDeviceType("Child Switch Health")
    } else {
        if (device.getDataValue("model") == "FB56+ZSW1GKJ2.7") {
            setDeviceType("ZigBee Switch")
        } else {
            createChildDevices()
        }

        updateDataValue("onOff", "catchall")
        refresh()
    }
}

def updated() {
    log.debug "updated()"
    updateDataValue("onOff", "catchall")
    refresh()
}

def parse(String description) {
    log.debug "description $description"
    Map eventMap = zigbee.getEvent(description)
    Map eventDescMap = zigbee.parseDescriptionAsMap(description)

    if (!eventMap && eventDescMap) {
        eventMap = [:]
        if (eventDescMap?.clusterId == zigbee.ONOFF_CLUSTER) {
            eventMap[name] = "switch"
            eventMap[value] = eventDescMap?.value
        }
    }

    if (eventMap) {
        log.debug "eventMap $eventMap"
        log.debug "eventDescMap $eventDescMap"
        if (eventDescMap?.sourceEndpoint == "0B" || eventDescMap?.sourceEndpoint == "01") {
            sendEvent(eventMap)
        } else {
            def childDevice = childDevices.find {
                it.deviceNetworkId == "$device.deviceNetworkId:${eventDescMap.sourceEndpoint}"
            }
            if (childDevice) {
                childDevice.sendEvent(eventMap)
            } else {
                log.debug "Child device: $device.deviceNetworkId:${eventDescMap.sourceEndpoint} was not found"
            }
        }
    }
}

private void createChildDevices() {
    log.debug("dni: $device.deviceNetworkId")
    def endPointMap = []
    def x = getChildCount()
    if (x == 2 && device.getDataValue("model") == "FNB56-ZSW02LX2.0") {
        endPointMap = ["C"]
    } else {
        endPointMap = 2..x
    }

    def switchNum = 1
    for (i in endPointMap) {
        switchNum += 1
        addChildDevice("Zemi ZigBee Switch", "$device.deviceNetworkId:0${i}", device.hubId,
                       [completedSetup: true, label: "Zemi ZigBee Switch ${switchNum}", isComponent: false])
    }
}

private getChildEndpoint(String dni) {
    dni.split(":")[-1] as String
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
    log.debug("child on ${dni}")
    def childEndpoint = getChildEndpoint(dni)
    zigbee.command(zigbee.ONOFF_CLUSTER, 0x01, "", [destEndpoint: childEndpoint])
}

def childOff(String dni) {
    log.debug("child off ${dni}")
    def childEndpoint = getChildEndpoint(dni)
    zigbee.command(zigbee.ONOFF_CLUSTER, 0x00, "", [destEndpoint: childEndpoint])
}

/**
 * PING is used by Device-Watch in attempt to reach the Device
 * */
def ping() {
    return refresh()
}

def refresh() {
    def cmds = zigbee.onOffRefresh()
    def x = getChildCount()

    def endPointMap = 2..x

    if (x == 2 && device.getDataValue("model") == "FNB56-ZSW02LX2.0") {
        endPointMap = [0x0C]
    }

    for (i in endPointMap) {
        cmds += zigbee.readAttribute(zigbee.ONOFF_CLUSTER, 0x0000, [destEndpoint: i])
    }

    return cmds
}

def poll() {
    refresh()
}

def healthPoll() {
    log.debug "healthPoll()"
    def cmds = refresh()
    cmds.each { sendHubCommand(new physicalgraph.device.HubAction(it)) }
}

def configureHealthCheck() {
    Integer hcIntervalMinutes = 12
    if (!state.hasConfiguredHealthCheck) {
        log.debug "Configuring Health Check, Reporting"
        unschedule("healthPoll")
        runEvery5Minutes("healthPoll")
        def healthEvent = [name: "checkInterval", value: hcIntervalMinutes * 60, displayed: false, data: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID]]
        // Device-Watch allows 2 check-in misses from device
        sendEvent(healthEvent)
        childDevices.each {
            it.sendEvent(healthEvent)
        }
        state.hasConfiguredHealthCheck = true
    }
}

def configure() {
    log.debug "configure()"
    configureHealthCheck()

    //other devices supported by this DTH in the future
    def cmds = zigbee.onOffConfig(0, 120)
    def x = getChildCount()

    def endPointMap = 2..x

    if (x == 2 && device.getDataValue("model") == "FNB56-ZSW02LX2.0") {
        endPointMap = [0x0C]
    }

    for (i in endPointMap) {
        cmds += zigbee.configureReporting(zigbee.ONOFF_CLUSTER, 0x0000, 0x10, 0, 120, null, [destEndpoint: i])
    }
    cmds += refresh()
    return cmds
}

private getChildCount() {
    if (device.getDataValue("model") == "FNB56-ZSW02LX2.0") {
        return 2
    } else if (device.getDataValue("model") == "FNB56-ZSW03LX2.0") {
        return 3
    } else {
        return 2    
    }
}