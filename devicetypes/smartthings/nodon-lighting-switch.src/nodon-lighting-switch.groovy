/*
 *  Integrated ZigBee Switch
 * 
 *  Copyright 2020 SmartThings
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
public static String version() { return "v0.0.01.20220114" }

import java.lang.Math
import groovy.json.JsonOutput


private getMODEL_MAP() { 
    [

        "SIN-4-2-20" : 2
    ]
}

metadata {
    definition(name: "NodOn lighting Switch", namespace: "Smartthings", author: "NodOn", ocfDeviceType: "oic.d.switch", vid: "generic-switch") {
        capability "Actuator"
        capability "Configuration"
        capability "Refresh"
        capability "Health Check"
        capability "Switch"

        command "childOn", ["string"]
        command "childOff", ["string"]
        
		// NodOn
		fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0007, 0008, FC57", outClusters: "0021", manufacturer: "NodOn", model: "SIN-4-2-20", deviceJoinName: "NodOn Light1"
        
    }

    preferences {
        input name: "isAutoCreateChildDevice", type: "bool", title: "Auto detecting and create device", description: "default: true", defaultValue: true, required: true
        input name: "isCreateAllControllerSwitch", type: "bool", title: "Create All switch", description: "default: false", defaultValue: false, required: true
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
    def endpointCount = getEndpointCount()
    def model = device.getDataValue("model")
    createChildDevices()
    
    updateDataValue("onOff", "catchall")
    state.hasConfiguredHealthCheck = false
    refresh()
}

def updated() {
    log.debug "updated()"
    updateDataValue("onOff", "catchall")
    state.hasConfiguredHealthCheck = false
    refresh()
}

def parse(String description) {
    Map eventMap = zigbee.getEvent(description)
    Map eventDescMap = zigbee.parseDescriptionAsMap(description)

    if (!eventMap && eventDescMap) {
        eventMap = [:]
        if (eventDescMap?.clusterId == zigbee.ONOFF_CLUSTER) {
            eventMap[name] = "switch"
            eventMap[value] = eventDescMap?.value
        }
    }

    if (eventMap && eventDescMap) {
        if (eventDescMap?.attrId == "0000" || eventDescMap?.attId == null) {
            def endpointId = device.getDataValue("endpointId")
            log.debug "eventMap $eventMap | eventDescMap $eventDescMap"
            eventMap[displayed] = true
            if (eventDescMap?.sourceEndpoint == endpointId) {
                log.debug "parse - sendEvent parent $eventDescMap.sourceEndpoint"
                sendEvent(eventMap)
            } else {
                def childDevice = childDevices.find {
                    it.deviceNetworkId == "$device.deviceNetworkId:${eventDescMap.sourceEndpoint}"
                }
                if (childDevice) {
                    log.debug "parse - sendEvent child  $eventDescMap.sourceEndpoint"
                    childDevice.sendEvent(eventMap)
                } else if (isAutoCreateChildDevice != false || getEndpointCount() == 0){
                    def model = device.getDataValue("model")
                    log.debug "Child device: $device.deviceNetworkId:${eventDescMap?.sourceEndpoint} was not found"
                    def parentEndpointInt = zigbee.convertHexToInt(endpointId)
                    if (eventDescMap?.sourceEndpoint != null) {
                        def childEndpointInt = zigbee.convertHexToInt(eventDescMap?.sourceEndpoint)
                        def childEndpointHexString = zigbee.convertToHexString(childEndpointInt, 2).toUpperCase()
                        def deviceLabel = "${device.displayName[0..-2]}"
                        def deviceIndex = Math.abs(childEndpointInt - parentEndpointInt) + 1

                        def childByEndpointId = childDevices.find {
                            it.deviceNetworkId.endsWith(":${eventDescMap.sourceEndpoint}")
                        }

                        if (childByEndpointId) {
                            log.debug "FOUND CHILD!!!!! Change dni to $device.deviceNetworkId:$childEndpointHexString"
                            childByEndpointId.setDeviceNetworkId("$device.deviceNetworkId:$childEndpointHexString")               
                        } else {
                            log.debug "NOT FOUND CHILD!!!!! Create to $deviceLabel$deviceIndex"
                            createChildDevice("$deviceLabel$deviceIndex", childEndpointHexString)
                        }
                    }
                }

                if (isCreateAllControllerSwitch) {
                    def allControlChildDevice = childDevices.find {
                        it.deviceNetworkId == "$device.deviceNetworkId:ALL"
                    }
                    if (!allControlChildDevice) {
                        def deviceLabel = "${device.displayName[0..-2]}"
                        createChildDevice("${deviceLabel}ALL", "ALL")
                    }
                }
            }

            checkAllSwtichValue()
        }
    }
}

private checkAllSwtichValue() {
    def parentSwitchValue = device.currentState("switch").value
    log.debug("checkAllSwtichValue ${device.label} : ${parentSwitchValue}")

    def allChildDeviceValue = parentSwitchValue

    def allChildDevice = null
    childDevices?.each {
        if (it.deviceNetworkId == "$device.deviceNetworkId:ALL") {
            allChildDevice = it
        } else {
            if (it.currentState("switch")?.value == "on") {
                allChildDeviceValue = "on"
            }
        }
    }

    if (allChildDevice?.currentState("switch") != allChildDeviceValue) {
        allChildDevice?.sendEvent(name: "switch", value : allChildDeviceValue)
    }
}

private getEndpointCount() {
    def model = device.getDataValue("model")
    def count = MODEL_MAP[model] ?: 0

    log.debug("getEndpointCount[$model] : $count")
    return count
}

private void createChildDevices() {
    log.debug("=============createChildDevices of $device.deviceNetworkId")
    if (!state.isCreateChildDone || isAutoCreateChildDevice != false) {
        def endpointCount = getEndpointCount()
        def endpointId = device.getDataValue("endpointId")
        def endpointInt = zigbee.convertHexToInt(endpointId)
        def deviceLabel = "${device.displayName[0..-2]}"
        deviceLabel.minus("1")

        for (i in 1..endpointCount - 1) {
            def endpointHexString = zigbee.convertToHexString(endpointInt + i, 2).toUpperCase()
            createChildDevice("$deviceLabel${i + 1}", endpointHexString)
        }


        state.isCreateChildDone = true
    }
}

private void createChildDevice(String deviceLabel, String endpointHexString) {
    def childDevice = childDevices.find {
        it.deviceNetworkId == "$device.deviceNetworkId:$endpointHexString"
    }
    if (!childDevice) {
        log.debug("===========Need to createChildDevice: $device.deviceNetworkId:$endpointHexString")
        addChildDevice("smartthings", "Child Switch", "$device.deviceNetworkId:$endpointHexString", device.hubId,
                       [completedSetup: true, label: deviceLabel, isComponent: false])
    } else {
        log.debug("createChildDevice: SKIP - $device.deviceNetworkId:${endpointHexString}")
    }
}

private getChildEndpoint(String dni) {
    dni.split(":")[-1] as String
}

private allChildOn() {
    log.debug "Executing 'on all' for 0x${device.deviceNetworkId}"

    childDevices.each {
        if (it.deviceNetworkId == "$device.deviceNetworkId:ALL") {
            it.sendEvent(name: "switch", value: "on") 
        } else {
            if (it.currentState("switch").value != "on") {
                it.on()
            }
        }
    }
}

private allChildOff() {
    log.debug "Executing 'off all' for 0x${device.deviceNetworkId}"
    childDevices.each {
        if (it.deviceNetworkId == "$device.deviceNetworkId:ALL") {
            it.sendEvent(name: "switch", value: "off") 
        } else {
            if (it.currentState("switch").value != "off") {
                it.off()
            }
        }
    }
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
    if(childEndpoint == "ALL") {
        allChildOn()
        if (device.currentState("switch") != "on") {
            zigbee.on()
        }
    } else {
        def endpointInt = zigbee.convertHexToInt(childEndpoint)
        zigbee.command(zigbee.ONOFF_CLUSTER, 0x01, "", [destEndpoint: endpointInt])
    }
}

def childOff(String dni) {
    log.debug("child off ${dni}")
    def childEndpoint = getChildEndpoint(dni)
    if(childEndpoint == "ALL") {
        allChildOff()
        if (device.currentState("switch") != "off") {
            zigbee.off()
        }
    }else {
        def endpointInt = zigbee.convertHexToInt(childEndpoint)
        zigbee.command(zigbee.ONOFF_CLUSTER, 0x00, "", [destEndpoint: endpointInt])
    }
}

/**
 * PING is used by Device-Watch in attempt to reach the Device
 * */
def ping() {
    return refresh()
}

def refresh() {
    def cmds = zigbee.onOffRefresh()
    def endpointCount = getEndpointCount()

    if (endpointCount > 1) {
        childDevices.each {
            log.debug "$it"
            def childEndpoint = getChildEndpoint(it.deviceNetworkId)
            if (childEndpoint.isNumber()) {
                log.debug "refresh $childEndpoint"
                def endpointInt = zigbee.convertHexToInt(childEndpoint)
                cmds += zigbee.readAttribute(zigbee.ONOFF_CLUSTER, 0x0000, [destEndpoint: endpointInt])
            }
        }
    } else {
        cmds += zigbee.readAttribute(zigbee.ONOFF_CLUSTER, 0x0000, [destEndpoint: 0xFF])
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
            def childEndpoint = getChildEndpoint(it.deviceNetworkId)
            if (childEndpoint.isNumber()) {
                it.sendEvent(healthEvent)
            } else if (childEndpoint == "ALL") {
                it.sendEvent(name: "DeviceWatch-Enroll", value: JsonOutput.toJson([protocol: "zigbee", scheme:"untracked"]), displayed: false)
            }
        }
        state.hasConfiguredHealthCheck = true
    }
}

def configure() {
    log.debug "configure()"
    configureHealthCheck()

    //other devices supported by this DTH in the future
    def cmds = zigbee.onOffConfig(0, 120)
    def endpointCount = getEndpointCount()

    if (endpointCount > 1) {
        childDevices.each {
            def childEndpoint = getChildEndpoint(it.deviceNetworkId)
            if (childEndpoint.isNumber()) {
                log.debug "configure(): $childEndpoint"
                def endpointInt = zigbee.convertHexToInt(childEndpoint)
                cmds += zigbee.configureReporting(zigbee.ONOFF_CLUSTER, 0x0000, 0x10, 0, 120, null, [destEndpoint: endpointInt])
            }
        }
    } else {
        cmds += zigbee.configureReporting(zigbee.ONOFF_CLUSTER, 0x0000, 0x10, 0, 120, null, [destEndpoint: 0xFF])
    }
    cmds += refresh()
    return cmds
}