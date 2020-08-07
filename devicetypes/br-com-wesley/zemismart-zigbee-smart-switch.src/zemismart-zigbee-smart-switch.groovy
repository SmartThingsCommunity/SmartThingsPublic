/**
 *  Zemismart ZigBee Smart Switch
 *
 *  Copyright 2019 w35l3y
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

import physicalgraph.zigbee.zcl.DataType

metadata {
    definition (name: "Zemismart ZigBee Smart Switch", namespace: "br.com.wesley", author: "w35l3y", ocfDeviceType: "oic.d.light", runLocally: false, executeCommandsLocally: false, mnmn:"SmartThings", vid: "generic-switch", genericHandler: "ZLL") {
        capability "Actuator"
        capability "Configuration"
        capability "Health Check"
        capability "Light"
        capability "Polling"
        capability "Refresh"
        capability "Switch"
        capability "Switch Level"

        //    Zemismart HGZB-41
//        fingerprint profileId: "C05E", deviceId: "0000", inClusters: "0000, 0003, 0004, 0005, 0006, 0008, 1000", outClusters: "0019", manufacturer: "FeiBit", model: "XFNB56-ZSW01LX2.0", deviceJoinName: "ZigBee Smart Switch"
//        fingerprint profileId: "C05E", deviceId: "0000", inClusters: "0000, 0003, 0004, 0005, 0006, 0008, 1000", outClusters: "0019", manufacturer: "3A Smart Home DE", model: "XLXN-1S27LX1.0", deviceJoinName: "ZigBee Smart Switch"

        //    Zemismart HGZB-42
//        fingerprint profileId: "C05E", deviceId: "0000", inClusters: "0000, 0003, 0004, 0005, 0006, 0008, 1000", outClusters: "0019", manufacturer: "FeiBit", model: "XFNB56-ZSW02LX2.0", deviceJoinName: "ZigBee Smart Switch"
//        fingerprint profileId: "C05E", deviceId: "0000", inClusters: "0000, 0003, 0004, 0005, 0006, 0008, 1000", outClusters: "0019", manufacturer: "3A Smart Home DE", model: "XLXN-2S27LX1.0", deviceJoinName: "ZigBee Smart Switch"

        //    Zemismart HGZB-43
//        fingerprint profileId: "C05E", deviceId: "0000", inClusters: "0000, 0003, 0004, 0005, 0006, 0008, 1000", outClusters: "0019", manufacturer: "FeiBit", model: "XFNB56-ZSW03LX2.0", deviceJoinName: "ZigBee Smart Switch"
//        fingerprint profileId: "C05E", deviceId: "0000", inClusters: "0000, 0003, 0004, 0005, 0006, 0008, 1000", outClusters: "0019", manufacturer: "3A Smart Home DE", model: "XLXN-3S27LX1.0", deviceJoinName: "ZigBee Smart Switch"
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
        standardTile("switch", "device.switch", width: 2, height: 2, canChangeIcon: true, decoration: "flat") {
            state ("off", label: '${name}', action: "on", icon: "st.switches.light.off", backgroundColor: "#ffffff", nextState: "turningOn")
            state ("on", label: '${name}', action: "off", icon: "st.switches.light.on", backgroundColor: "#00a0dc", nextState: "turningOff")
            state ("turningOff", label: '${name}', action: "on", icon: "st.switches.light.off", backgroundColor: "#ffffff", nextState: "turningOn")
            state ("turningOn", label: '${name}', action: "off", icon: "st.switches.light.on", backgroundColor: "#00a0dc", nextState: "turningOff")
        }
   
        controlTile("level", "device.level", "slider", range:"(1..9)", height: 2, width: 2, canChangeIcon: true, decoration: "flat") {
            state "level", action: "setLevel"
        }

        standardTile("refresh", "device.refresh", width: 2, height: 2, decoration: "flat", inactiveLabel: false) {
            state "default", label: "", action: "refresh.refresh", icon: "st.secondary.refresh"
        }

        main("switch")
        details(["switch", "level", "refresh"])
    }
}

def getChildCount() {
    def model = device.getDataValue("model")
    log.debug "Model found: " + model
    switch (model) {
    case ["FNB56-ZSW01LX2.0", "LXN-1S27LX1.0"]:
        return 1;break;
    case ["FNB56-ZSW02LX2.0", "LXN-2S27LX1.0"]:
        return 2;break;
    case ["FNB56-ZSW03LX2.0", "LXN-3S27LX1.0"]:
        return 3;break;
    default:
        log.debug "Model not found: " + model + "\nConsider adding new fingerprint for your device."
        return 3;break;
    }
}

def getInitialEndpoint () { Integer.parseInt(zigbee.endpointId, 10) }
def getHexEndpoint (index) { Integer.toHexString(getInitialEndpoint()+index).padLeft(2, "0").toUpperCase() }
def getEndpoint (child) { child.deviceNetworkId == device.deviceNetworkId?getInitialEndpoint():Integer.parseInt(child.deviceNetworkId.split(":")[-1], 16) }

def turn (Integer endpoint, value) {
    log.info "turn($endpoint, $value)"
    log.debug zigbee.command(zigbee.ONOFF_CLUSTER, value, "", [destEndpoint: endpoint])
    zigbee.command(zigbee.ONOFF_CLUSTER, value, "", [destEndpoint: endpoint])
}

def off (physicalgraph.device.cache.DeviceDTO child) { turn(getEndpoint(child), 0x00) }

def on (physicalgraph.device.cache.DeviceDTO child) { turn(getEndpoint(child), 0x01) }

def off() { off(device) }

def on() { on(device) }

def setOnOffTransitionTime (endpoint, seconds) {
zigbee.writeAttribute(zigbee.LEVEL_CONTROL_CLUSTER, 0x0010, DataType.UINT16, Math.round(Math.floor(seconds * 10)), [destEndpoint: endpoint])
}

def setOnLevel (endpoint, level) {
log.debug "setOnLevel($endpoint, $level)"
zigbee.writeAttribute(zigbee.LEVEL_CONTROL_CLUSTER, 0x0011, DataType.UINT8, level, [destEndpoint: endpoint])
}

def moveToLevel (endpoint, level, transitionTime) {
log.debug "moveToLevel($endpoint, $level, $transitionTime)"
    def p1 = DataType.pack(level, DataType.UINT8, false)
    def p2 = DataType.pack(transitionTime, DataType.UINT16, false)
    def dataString = [p1, p2].join(" ")
    //"st cmd 0x${device.deviceNetworkId} ${endpointId} 8 4 {${scaledLevel} ${transitionTime}}"
    zigbee.command(zigbee.LEVEL_CONTROL_CLUSTER, 0x0000, dataString, [destEndpoint: endpoint]) +
    zigbee.command(zigbee.LEVEL_CONTROL_CLUSTER, 0x0004, dataString, [destEndpoint: endpoint])
}

def move (endpoint, mode, rate = 0xFF) {
    def p1 = DataType.pack(mode, DataType.UINT8, false)
    def p2 = DataType.pack(rate, DataType.UINT8, false)
    def dataString = [p1, p2].join(" ")
    zigbee.command(zigbee.LEVEL_CONTROL_CLUSTER, 0x0001, dataString, [destEndpoint: endpoint])
}

def moveUp (endpoint, rate = 0xFF) {
move(endpoint, 0x00, rate)
}

def moveDown (endpoint, rate = 0xFF) {
move(endpoint, 0x01, rate)
}

def step (endpoint, mode, size, transitionTime) {
    def p1 = DataType.pack(mode, DataType.UINT8, false)
    def p2 = DataType.pack(size, DataType.UINT8, false)
    def p3 = DataType.pack(transitionTime, DataType.UINT16, false)
    def dataString = [p1, p2, p3].join(" ")
    zigbee.command(zigbee.LEVEL_CONTROL_CLUSTER, 0x0002, dataString, [destEndpoint: endpoint])
}

def stepUp (endpoint, size, transitionTime = 0xFFFF) {
step(endpoint, 0x00, size, transitionTime)
}

def stepDown (endpoint, size, transitionTime = 0xFFFF) {
step(endpoint, 0x01, size, transitionTime)
}

def stop (endpoint) {
    zigbee.command(zigbee.LEVEL_CONTROL_CLUSTER, 0x0003, "", [destEndpoint: endpoint]) +
    zigbee.command(zigbee.LEVEL_CONTROL_CLUSTER, 0x0007, "", [destEndpoint: endpoint])
}

def setLevel (endpoint, value, rate) {
log.debug "setLevel($endpoint, $value, $rate)"
    def level = Math.round(Math.floor(value * 2.54))
    setOnLevel(endpoint, level) + moveToLevel(endpoint, level, rate)
}

/*
https://people.ece.cornell.edu/land/courses/ece4760/FinalProjects/s2011/kjb79_ajm232/pmeter/ZigBee%20Cluster%20Library.pdf
3.10.2.3.5  'With On/Off' Commands
*/
def setLevel (physicalgraph.device.cache.DeviceDTO child, value, rate = 20) {
    def endpoint = getEndpoint(child)
    if (value > 0) {
        return on(child) + setLevel(endpoint, value, rate)
    }
    return setLevel(endpoint, value, rate) + off(child)
}

def setLevel (value, rate = 10) { setLevel(device, value, rate) }

def parse (description) {
    Map eventMap = zigbee.getEvent(description)
    Map eventDescMap = zigbee.parseDescriptionAsMap(description)

    log.debug "parse($description, $eventMap, $eventDescMap)"
    if (!eventMap || !eventMap.name) {
        def clusterId = eventDescMap?.clusterInt
        if (clusterId == zigbee.ONOFF_CLUSTER) {
            eventMap = [name: "switch", value: eventDescMap.value]
        //} else if (clusterId == zigbee.LEVEL_CONTROL_CLUSTER) {
        //    eventMap = [name: "level", value: device.currentValue("level")]
        }
    }

    /*if (!eventDescMap?.sourceEndpoint && !eventDescMap?.endpoint) {
    log.debug "Unknown device: $description, $eventMap, $eventDescMap"
    } else */if (eventMap) {
        def endpoint = getHexEndpoint(0)
        if (!eventDescMap || eventDescMap.sourceEndpoint == endpoint || eventDescMap.endpoint == endpoint) {
            log.debug "createEvent($eventMap)"
            return createEvent(eventMap)
        } else {
            def childDevice = childDevices.find {
                it.deviceNetworkId == "${device.deviceNetworkId}:${eventDescMap.sourceEndpoint}" || it.deviceNetworkId == "${device.deviceNetworkId}:${eventDescMap.endpoint}"
            }
            if (childDevice) {
                log.debug "child[ ${childDevice.deviceNetworkId.split(":")[-1]} ].createEvent($eventMap)"
                return childDevice.createEvent(childDevice.createAndSendEvent(eventMap))
            } else {
                log.debug "Child device: ${device.deviceNetworkId}:${eventDescMap.sourceEndpoint} was not found"
            }
        }
    }
}

def refresh (Integer endpoint) {
    zigbee.readAttribute(zigbee.LEVEL_CONTROL_CLUSTER, 0x0000, [destEndpoint: endpoint]) +
    zigbee.readAttribute(zigbee.ONOFF_CLUSTER, 0x0000, [destEndpoint: endpoint])
}

def refresh (physicalgraph.device.cache.DeviceDTO child) { refresh(getEndpoint(child)) }

def refresh (physicalgraph.app.ChildDeviceWrapper child) { refresh(getEndpoint(child)) }

def refresh() {
    log.debug "refresh()"
    ([refresh(device)] + childDevices.collect { refresh(it) }).flatten()
}

def poll (physicalgraph.device.cache.DeviceDTO child) {
    log.debug "poll($child)"
    refresh(child)
}

def poll () {
    log.debug "poll()"
    refresh()
}

def ping (physicalgraph.device.cache.DeviceDTO child) {
    log.debug "ping($child)"
    refresh(child)
}

def ping () {
    log.debug "ping()"
    refresh()
}

def createChildDevices () {
    if (!state.hasInstalledChildren) {
        (2..getChildCount()).each {
            addChildDevice("Zemismart ZigBee Smart Switch Child", "${device.deviceNetworkId}:${getHexEndpoint(it-1)}", device.hubId, [
                isComponent: false,
                componentName: "main",
                completedSetup: true,
                componentLabel: "#$it",
                label: "${device.displayName} $it",
            ])
        }
        state.hasInstalledChildren = true
    }
}

def healthPoll () {
    log.debug "healthPoll()"
    refresh().each { sendHubCommand(new physicalgraph.device.HubAction(it)) }
}

def configureHealthCheck () {
    if (!state.hasConfiguredHealthCheck) {
        log.debug "Configuring Health Check, Reporting"
        unschedule("healthPoll")
        runEvery5Minutes("healthPoll")
        // Device-Watch allows 2 check-in misses from device
        def healthEvent = [name: "checkInterval", value: 12 * 60, displayed: false, data: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID]]
        sendEvent(healthEvent)
        childDevices.each {
            it.sendEvent(healthEvent)
        }
        state.hasConfiguredHealthCheck = true
    }
}

def installed () {
    log.debug "installed()"

    createChildDevices()
}

def updated () {
    log.debug "updated()"
   
    createChildDevices()
    configureHealthCheck()
}

def configure () {
    log.debug "configure()"

    configureHealthCheck()

def initialEndpoint = getInitialEndpoint()
    (0..childDevices.size()).collect {
        zigbee.configureReporting(zigbee.LEVEL_CONTROL_CLUSTER, 0x0000, DataType.UINT8, 0, 120, 0x01, [destEndpoint: initialEndpoint + it]) +
        zigbee.configureReporting(zigbee.ONOFF_CLUSTER, 0x0000, DataType.BOOLEAN, 0, 120, null, [destEndpoint: initialEndpoint + it])
    } + refresh()
}