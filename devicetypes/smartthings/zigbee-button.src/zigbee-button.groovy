/**
 *  ZigBee Button
 *
 *  Copyright 2015 Mitch Pond
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

import groovy.json.JsonOutput
import physicalgraph.zigbee.zcl.DataType

metadata {
    definition (name: "ZigBee Button", namespace: "smartthings", author: "Mitch Pond", runLocally: true, minHubCoreVersion: "000.022.0002", executeCommandsLocally: false, ocfDeviceType: "x.com.st.d.remotecontroller") {
        capability "Actuator"
        capability "Battery"
        capability "Button"
        capability "Holdable Button"        
        capability "Configuration"
        capability "Refresh"
        capability "Sensor"
        capability "Health Check"

        command "enrollResponse"

        fingerprint inClusters: "0000, 0001, 0003, 0020, 0402, 0B05", outClusters: "0003, 0006, 0008, 0019", manufacturer: "OSRAM", model: "LIGHTIFY Dimming Switch", deviceJoinName: "OSRAM LIGHTIFY Dimming Switch"
        fingerprint inClusters: "0000, 0001, 0003, 0020, 0402, 0B05", outClusters: "0003, 0006, 0008, 0019", manufacturer: "CentraLite", model: "3130", deviceJoinName: "Centralite Zigbee Smart Switch"
        fingerprint inClusters: "0000, 0001, 0003, 0020, 0500", outClusters: "0003,0019", manufacturer: "CentraLite", model: "3455-L", deviceJoinName: "Iris Care Pendant"
        fingerprint inClusters: "0000, 0001, 0003, 0007, 0020, 0402, 0B05", outClusters: "0003, 0006, 0019", manufacturer: "CentraLite", model: "3460-L", deviceJoinName: "Iris Smart Button"
        fingerprint inClusters: "0000, 0001, 0003, 0007, 0020, 0B05", outClusters: "0003, 0006, 0019", manufacturer: "CentraLite", model:"3450-L", deviceJoinName: "Iris KeyFob"
    }

    simulator {}

    preferences {
        section {
            input ("holdTime", "number", title: "Minimum time in seconds for a press to count as \"held\"", defaultValue: 1, displayDuringSetup: false)
        }
    }

    tiles {
        standardTile("button", "device.button", width: 2, height: 2) {
            state "default", label: "", icon: "st.unknown.zwave.remote-controller", backgroundColor: "#ffffff"
            state "button 1 pushed", label: "pushed #1", icon: "st.unknown.zwave.remote-controller", backgroundColor: "#00A0DC"
        }

        valueTile("battery", "device.battery", decoration: "flat", inactiveLabel: false) {
            state "battery", label:'${currentValue}% battery', unit:""
        }

        standardTile("refresh", "device.refresh", inactiveLabel: false, decoration: "flat") {
            state "default", action:"refresh.refresh", icon:"st.secondary.refresh"
        }
        main (["button"])
        details(["button", "battery", "refresh"])
    }
}

def parse(String description) {
    log.debug "description is $description"
    def event = zigbee.getEvent(description)
    if (event) {
        sendEvent(event)
    }
    else {
        if ((description?.startsWith("catchall:")) || (description?.startsWith("read attr -"))) {
            def descMap = zigbee.parseDescriptionAsMap(description)
            if (descMap.clusterInt == 0x0001 && descMap.attrInt == 0x0020 && descMap.value != null) {
                event = getBatteryResult(zigbee.convertHexToInt(descMap.value))
            }
            else if (descMap.clusterInt == 0x0006 || descMap.clusterInt == 0x0008) {
                event = parseNonIasButtonMessage(descMap)
            }
        }
        else if (description?.startsWith('zone status')) {
            event = parseIasButtonMessage(description)
        }

        log.debug "Parse returned $event"
        def result = event ? createEvent(event) : []

        if (description?.startsWith('enroll request')) {
            List cmds = zigbee.enrollResponse()
            result = cmds?.collect { new physicalgraph.device.HubAction(it) }
        }
        return result
    }
}

private Map parseIasButtonMessage(String description) {
    def zs = zigbee.parseZoneStatus(description)
    return zs.isAlarm2Set() ? getButtonResult("press") : getButtonResult("release")
}

private Map getBatteryResult(rawValue) {
    log.debug 'Battery'
    def volts = rawValue / 10
    if (volts > 3.0 || volts == 0 || rawValue == 0xFF) {
        return [:]
    }
    else {
        def result = [
                name: 'battery'
        ]
        def minVolts = 2.1
        def maxVolts = 3.0
        def pct = (volts - minVolts) / (maxVolts - minVolts)
        result.value = Math.min(100, (int)(pct * 100))
        def linkText = getLinkText(device)
        result.descriptionText = "${linkText} battery was ${result.value}%"
        return result
    }
}

private Map parseNonIasButtonMessage(Map descMap){
    def buttonState = ""
    def buttonNumber = 0
    if ((device.getDataValue("model") == "3460-L") &&(descMap.clusterInt == 0x0006)) {
        if (descMap.commandInt == 1) {
            getButtonResult("press")
        }
        else if (descMap.commandInt == 0) {
            getButtonResult("release")
        }
    }
    else if ((device.getDataValue("model") == "3450-L") && (descMap.clusterInt == 0x0006)) {
        if (descMap.commandInt == 1) {
            getButtonResult("press")
        }
        else if (descMap.commandInt == 0) {
            def button = 1
            switch(descMap.sourceEndpoint) {
                case "01":
                    button = 4
                    break
                case "02":
                    button = 3
                    break
                case "03":
                    button = 1
                    break
                case "04":
                    button = 2
                    break
            }
        
            getButtonResult("release", button)
        }
    }
    else if (descMap.clusterInt == 0x0006) {
        buttonState = "pushed"
        if (descMap.command == "01") {
            buttonNumber = 1
        }
        else if (descMap.command == "00") {
            buttonNumber = 2
        }
        if (buttonNumber !=0) {
            def descriptionText = "$device.displayName button $buttonNumber was $buttonState"
            return createEvent(name: "button", value: buttonState, data: [buttonNumber: buttonNumber], descriptionText: descriptionText, isStateChange: true)
        }
        else {
            return [:]
        }
    }
    else if (descMap.clusterInt == 0x0008) {
        if (descMap.command == "05") {
            state.buttonNumber = 1
            getButtonResult("press", 1)
        }
        else if (descMap.command == "01") {
            state.buttonNumber = 2
            getButtonResult("press", 2)
        }
        else if (descMap.command == "03") {
            getButtonResult("release", state.buttonNumber)
        }
    }
}

def refresh() {
    log.debug "Refreshing Battery"

    return zigbee.readAttribute(zigbee.POWER_CONFIGURATION_CLUSTER, 0x20) +
            zigbee.enrollResponse()
}

def configure() {
    log.debug "Configuring Reporting, IAS CIE, and Bindings."
    def cmds = []
    if (device.getDataValue("model") == "3450-L") {
        cmds << [
                "zdo bind 0x${device.deviceNetworkId} 1 1 6 {${device.zigbeeId}} {}", "delay 300",
                "zdo bind 0x${device.deviceNetworkId} 2 1 6 {${device.zigbeeId}} {}", "delay 300",
                "zdo bind 0x${device.deviceNetworkId} 3 1 6 {${device.zigbeeId}} {}", "delay 300",
                "zdo bind 0x${device.deviceNetworkId} 4 1 6 {${device.zigbeeId}} {}", "delay 300"
        ]
    }
    return zigbee.onOffConfig() +
            zigbee.levelConfig() +
            zigbee.configureReporting(zigbee.POWER_CONFIGURATION_CLUSTER, 0x20, DataType.UINT8, 30, 21600, 0x01) +
            zigbee.enrollResponse() +
            zigbee.readAttribute(zigbee.POWER_CONFIGURATION_CLUSTER, 0x20) +
            cmds

}

private Map getButtonResult(buttonState, buttonNumber = 1) {
    if (buttonState == 'release') {
        log.debug "Button was value : $buttonState"
        if(state.pressTime == null) {
            return [:]
        }
        def timeDiff = now() - state.pressTime
        log.info "timeDiff: $timeDiff"
        def holdPreference = holdTime ?: 1
        log.info "holdp1 : $holdPreference"
        holdPreference = (holdPreference as int) * 1000
        log.info "holdp2 : $holdPreference"
        if (timeDiff > 10000) {         //timeDiff>10sec check for refresh sending release value causing actions to be executed
            return [:]
        }
        else {
            if (timeDiff < holdPreference) {
                buttonState = "pushed"
            }
            else {
                buttonState = "held"
            }
            def descriptionText = "$device.displayName button $buttonNumber was $buttonState"
            return createEvent(name: "button", value: buttonState, data: [buttonNumber: buttonNumber], descriptionText: descriptionText, isStateChange: true)
        }
    }
    else if (buttonState == 'press') {
        log.debug "Button was value : $buttonState"
        state.pressTime = now()
        log.info "presstime: ${state.pressTime}"
        return [:]
    }
}

def installed() {
    initialize()

    // Initialize default states
    device.currentValue("numberOfButtons")?.times {
        sendEvent(name: "button", value: "pushed", data: [buttonNumber: it+1], displayed: false)
    }
}

def updated() {
    initialize()
}

def initialize() {
    // Arrival sensors only goes OFFLINE when Hub is off
    sendEvent(name: "DeviceWatch-Enroll", value: JsonOutput.toJson([protocol: "zigbee", scheme:"untracked"]), displayed: false)
    if ((device.getDataValue("manufacturer") == "OSRAM") && (device.getDataValue("model") == "LIGHTIFY Dimming Switch")) {
        sendEvent(name: "numberOfButtons", value: 2, displayed: false)
    }
    else if (device.getDataValue("manufacturer") == "CentraLite") {
        if (device.getDataValue("model") == "3130") {
            sendEvent(name: "numberOfButtons", value: 2, displayed: false)
        }
        else if ((device.getDataValue("model") == "3455-L") || (device.getDataValue("model") == "3460-L")) {
            sendEvent(name: "numberOfButtons", value: 1, displayed: false)
        }
        else if (device.getDataValue("model") == "3450-L") {
            sendEvent(name: "numberOfButtons", value: 4, displayed: false)
        }
        else {
            sendEvent(name: "numberOfButtons", value: 4, displayed: false)    //default case. can be changed later.
        }
    }
    else {
        //default. can be changed
        sendEvent(name: "numberOfButtons", value: 4, displayed: false)
    }

}
