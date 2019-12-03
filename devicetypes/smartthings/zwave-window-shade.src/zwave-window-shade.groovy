/**
 *  Copyright 2017 SmartThings
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


metadata {
    definition (name: "Z-Wave Window Shade", namespace: "smartthings", author: "SmartThings", ocfDeviceType: "oic.d.blind") {
        capability "Window Shade"
        capability "Window Shade Preset"
        capability "Battery"
        capability "Refresh"
        capability "Health Check"
        capability "Actuator"
        capability "Sensor"

        command "stop"

        capability "Switch Level"   // until we get a Window Shade Level capability

        // This device handler is specifically for non-SWF position-aware window coverings
        //
        fingerprint type: "0x1107", cc: "0x5E,0x26", deviceJoinName: "Window Shade"
        fingerprint type: "0x9A00", cc: "0x5E,0x26", deviceJoinName: "Window Shade"
//        fingerprint mfr:"026E", prod:"4353", model:"5A31", deviceJoinName: "Window Blinds"
//        fingerprint mfr:"026E", prod:"5253", model:"5A31", deviceJoinName: "Roller Shade"
    }

    simulator {
        status "open":  "command: 2603, payload: FF"
        status "closed": "command: 2603, payload: 00"
        status "10%": "command: 2603, payload: 0A"
        status "66%": "command: 2603, payload: 42"
        status "99%": "command: 2603, payload: 63"
        status "battery 100%": "command: 8003, payload: 64"
        status "battery low": "command: 8003, payload: FF"

        // reply messages
        reply "2001FF,delay 1000,2602": "command: 2603, payload: 10 FF FE"
        reply "200100,delay 1000,2602": "command: 2603, payload: 60 00 FE"
        reply "200142,delay 1000,2602": "command: 2603, payload: 10 42 FE"
        reply "200163,delay 1000,2602": "command: 2603, payload: 10 63 FE"
    }

    tiles(scale: 2) {
        multiAttributeTile(name:"windowShade", type: "lighting", width: 6, height: 4){
            tileAttribute ("device.windowShade", key: "PRIMARY_CONTROL") {
                attributeState "open", label:'${name}', action:"close", icon:"st.shades.shade-open", backgroundColor:"#79b821", nextState:"closing"
                attributeState "closed", label:'${name}', action:"open", icon:"st.shades.shade-closed", backgroundColor:"#ffffff", nextState:"opening"
                attributeState "partially open", label:'Open', action:"close", icon:"st.shades.shade-open", backgroundColor:"#79b821", nextState:"closing"
                attributeState "opening", label:'${name}', action:"stop", icon:"st.shades.shade-opening", backgroundColor:"#79b821", nextState:"partially open"
                attributeState "closing", label:'${name}', action:"stop", icon:"st.shades.shade-closing", backgroundColor:"#ffffff", nextState:"partially open"
            }
            tileAttribute ("device.level", key: "SLIDER_CONTROL") {
                attributeState "level", action:"setLevel"
            }
        }

        standardTile("home", "device.level", width: 2, height: 2, decoration: "flat") {
            state "default", label: "home", action:"presetPosition", icon:"st.Home.home2"
        }

        standardTile("refresh", "device.refresh", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
            state "default", label:'', action:"refresh.refresh", icon:"st.secondary.refresh", nextState: "disabled"
            state "disabled", label:'', action:"", icon:"st.secondary.refresh"
        }

        valueTile("battery", "device.battery", decoration: "flat", inactiveLabel: false, width: 2, height: 2) {
            state "battery", label:'${currentValue}% battery', unit:""
        }

        preferences {
            input "preset", "number", title: "Preset position", defaultValue: 50, range: "1..100", required: false, displayDuringSetup: false
        }

        main(["windowShade"])
        details(["windowShade", "home", "refresh", "battery"])

    }
}

def parse(String description) {
    def result = null
    //if (description =~ /command: 2603, payload: ([0-9A-Fa-f]{6})/)
    // TODO: Workaround manual parsing of v4 multilevel report
    def cmd = zwave.parse(description, [0x20: 1, 0x26: 3])  // TODO: switch to SwitchMultilevel v4 and use target value
    if (cmd) {
        result = zwaveEvent(cmd)
    }
    log.debug "Parsed '$description' to ${result.inspect()}"
    return result
}

def getCheckInterval() {
    // These are battery-powered devices, and it's not very critical
    // to know whether they're online or not – 12 hrs
    4 * 60 * 60
}

def installed() {
    sendEvent(name: "checkInterval", value: checkInterval, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID, offlinePingable: "1"])
    sendEvent(name: "supportedWindowShadeCommands", value: JsonOutput.toJson(["open", "close", "pause"]), displayed: false)
    response(refresh())
}

def updated() {
    if (device.latestValue("checkInterval") != checkInterval) {
        sendEvent(name: "checkInterval", value: checkInterval, displayed: false)
    }
    if (!device.latestState("battery")) {
        response(zwave.batteryV1.batteryGet())
    }
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd) {
    handleLevelReport(cmd)
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicSet cmd) {
    handleLevelReport(cmd)
}

def zwaveEvent(physicalgraph.zwave.commands.switchmultilevelv3.SwitchMultilevelReport cmd) {
    handleLevelReport(cmd)
}

private handleLevelReport(physicalgraph.zwave.Command cmd) {
    def descriptionText = null
    def shadeValue = null

    def level = cmd.value as Integer
    if (level >= 99) {
        level = 100
        shadeValue = "open"
    } else if (level <= 0) {
        level = 0  // unlike dimmer switches, the level isn't saved when closed
        shadeValue = "closed"
    } else {
        shadeValue = "partially open"
        descriptionText = "${device.displayName} shade is ${level}% open"
    }
    def levelEvent = createEvent(name: "level", value: level, unit: "%", displayed: false)
    def stateEvent = createEvent(name: "windowShade", value: shadeValue, descriptionText: descriptionText, isStateChange: levelEvent.isStateChange)

    def result = [stateEvent, levelEvent]
    if (!state.lastbatt || now() - state.lastbatt > 24 * 60 * 60 * 1000) {
        log.debug "requesting battery"
        state.lastbatt = (now() - 23 * 60 * 60 * 1000) // don't queue up multiple battery reqs in a row
        result << response(["delay 15000", zwave.batteryV1.batteryGet().format()])
    }
    result
}

def zwaveEvent(physicalgraph.zwave.commands.switchmultilevelv3.SwitchMultilevelStopLevelChange cmd) {
    [ createEvent(name: "windowShade", value: "partially open", displayed: false, descriptionText: "$device.displayName shade stopped"),
      response(zwave.switchMultilevelV1.switchMultilevelGet().format()) ]
}

def zwaveEvent(physicalgraph.zwave.commands.manufacturerspecificv2.ManufacturerSpecificReport cmd) {
    def msr = String.format("%04X-%04X-%04X", cmd.manufacturerId, cmd.productTypeId, cmd.productId)
    updateDataValue("MSR", msr)
    if (cmd.manufacturerName) {
        updateDataValue("manufacturer", cmd.manufacturerName)
    }
    createEvent([descriptionText: "$device.displayName MSR: $msr", isStateChange: false])
}

def zwaveEvent(physicalgraph.zwave.commands.batteryv1.BatteryReport cmd) {
    def map = [ name: "battery", unit: "%" ]
    if (cmd.batteryLevel == 0xFF) {
        map.value = 1
        map.descriptionText = "${device.displayName} has a low battery"
        map.isStateChange = true
    } else {
        map.value = cmd.batteryLevel
    }
    state.lastbatt = now()
    createEvent(map)
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
    log.debug "unhandled $cmd"
    return []
}

def open() {
    log.debug "open()"
    /*delayBetween([
            zwave.basicV1.basicSet(value: 0xFF).format(),
            zwave.switchMultilevelV1.switchMultilevelGet().format()
    ], 1000)*/
    zwave.basicV1.basicSet(value: 99).format()
}

def close() {
    log.debug "close()"
    /*delayBetween([
            zwave.basicV1.basicSet(value: 0x00).format(),
            zwave.switchMultilevelV1.switchMultilevelGet().format()
    ], 1000)*/
    zwave.basicV1.basicSet(value: 0).format()
}

def setLevel(value, duration = null) {
    log.debug "setLevel(${value.inspect()})"
    Integer level = value as Integer
    if (level < 0) level = 0
    if (level > 99) level = 99
    delayBetween([
            zwave.basicV1.basicSet(value: level).format(),
            zwave.switchMultilevelV1.switchMultilevelGet().format()
    ])
}

def presetPosition() {
    setLevel(preset ?: state.preset ?: 50)
}

def pause() {
    log.debug "pause()"
    stop()
}

def stop() {
    log.debug "stop()"
    zwave.switchMultilevelV3.switchMultilevelStopLevelChange().format()
}

def ping() {
    zwave.switchMultilevelV1.switchMultilevelGet().format()
}

def refresh() {
    log.debug "refresh()"
    delayBetween([
            zwave.switchMultilevelV1.switchMultilevelGet().format(),
            zwave.batteryV1.batteryGet().format()
    ], 1500)
}