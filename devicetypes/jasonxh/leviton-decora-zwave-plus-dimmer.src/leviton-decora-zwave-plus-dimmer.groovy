/**
 *  Copyright 2017 Jason Xia
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
    definition (name: "Leviton Decora Z-Wave Plus Dimmer", namespace: "jasonxh", author: "Jason Xia", ocfDeviceType: "oic.d.light") {
        capability "Actuator"
        capability "Configuration"
        capability "Health Check"
        capability "Indicator"
        capability "Light"
        capability "Polling"
        capability "Refresh"
        capability "Sensor"
        capability "Switch"
        capability "Switch Level"

        attribute "loadType", "enum", ["incandescent", "led", "cfl"]
        attribute "presetLevel", "number"

        command "low"
        command "medium"
        command "high"

        fingerprint mfr:"001D", prod:"3201", model:"0001", deviceJoinName: "Leviton Decora Z-Wave Plus 600W Dimmer"
        fingerprint mfr:"001D", prod:"3301", model:"0001", deviceJoinName: "Leviton Decora Z-Wave Plus 1000W Dimmer"
    }

    simulator {
        // TODO - the simulator is not working yet
        status "on":  "command: 2003, payload: FF"
        status "off": "command: 2003, payload: 00"
        status "09%": "command: 2003, payload: 09"
        status "10%": "command: 2003, payload: 0A"
        status "33%": "command: 2003, payload: 21"
        status "66%": "command: 2003, payload: 42"
        status "99%": "command: 2003, payload: 63"

        reply "2001FF,delay 5000,2602": "command: 2603, payload: FF"
        reply "200100,delay 5000,2602": "command: 2603, payload: 00"
        reply "200119,delay 5000,2602": "command: 2603, payload: 19"
        reply "200132,delay 5000,2602": "command: 2603, payload: 32"
        reply "20014B,delay 5000,2602": "command: 2603, payload: 4B"
        reply "200163,delay 5000,2602": "command: 2603, payload: 63"
    }

    tiles(scale: 2) {
        multiAttributeTile(name: "switch", type: "lighting", width: 6, height: 4, canChangeIcon: true) {
            tileAttribute("device.switch", key: "PRIMARY_CONTROL") {
                attributeState "on", label: '${name}', action: "switch.off", icon: "st.switches.switch.on", backgroundColor: "#00a0dc", nextState: "turningOff"
                attributeState "off", label: '${name}', action: "switch.on", icon: "st.switches.switch.off", backgroundColor: "#ffffff", nextState: "turningOn"
                attributeState "turningOn", label: '${name}', action: "switch.off", icon: "st.switches.switch.on", backgroundColor: "#00a0dc", nextState: "turningOff"
                attributeState "turningOff", label: '${name}', action: "switch.on", icon: "st.switches.switch.off", backgroundColor: "#ffffff", nextState: "turningOn"
            }
            tileAttribute("device.level", key: "SLIDER_CONTROL") {
                attributeState "level", label: '${currentValue} %', action: "switch level.setLevel"
            }
        }

        standardTile("low", "device.level", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
            state "default", label: 'LOW', action: "low", icon: "st.Lighting.light14"
        }

        standardTile("medium", "device.level", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
            state "default", label: 'MED', action: "medium", icon: "st.Lighting.light13"
        }

        standardTile("high", "device.level", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
            state "default", label: 'HIGH', action: "high", icon: "st.Lighting.light11"
        }

        valueTile("level", "device.level", width: 4, height: 2, inactiveLabel: false, decoration: "flat") {
            state "level", label: 'Current level is ${currentValue}${unit}', unit: "%", backgroundColor: "#ffffff"
        }

        standardTile("refresh", "device.switch", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
            state "default", label: '', action: "refresh.refresh", icon: "st.secondary.refresh"
        }

        main("switch")
        details(["switch", "low", "medium", "high", "level", "refresh"])
    }

    preferences {
        input name: "loadTypePref", type: "enum", title: "Load Type",
                options: ["Incandescent (default)", "LED", "CFL"],
                displayDuringSetup: false, required: false
        input name: "indicatorStatusPref", type: "enum", title: "Indicator LED is lit",
                options: ["When switch is off (default)", "When switch is on", "Never"],
                displayDuringSetup: false, required: false
        input name: "presetLevelPref", type: "number", title: "Preset light level",
                description: "0 = last dim level (default). 1 to 100 = level", range: "0..100",
                displayDuringSetup: false, required: false
    }
}

def installed() {
    log.debug "installed..."
    initialize()
}

def updated() {
    log.debug "updated..."
    initialize()
    response(configure())
}

def configure() {
    def commands = []
    switch (loadTypePref) {
        case "Incandescent (default)":
            sendEvent(name: "loadType", value: "incandescent")
            commands.addAll(configurationCommand(8, 0))
            break
        case "LED":
            sendEvent(name: "loadType", value: "led")
            commands.addAll(configurationCommand(8, 1))
            break
        case "CFL":
            sendEvent(name: "loadType", value: "cfl")
            commands.addAll(configurationCommand(8, 2))
            break
    }
    switch (indicatorStatusPref) {
        case "When switch is off (default)": commands.addAll(indicatorWhenOff()); break
        case "When switch is on": commands.addAll(indicatorWhenOn()); break
        case "Never": commands.addAll(indicatorNever()); break
    }
    if (presetLevelPref != null) {
        commands.addAll(setPresetLevel(presetLevelPref as short))
    }
    log.debug "Configuring with commands $commands"
    commands
}

def parse(String description) {
    def result = null
    def cmd = zwave.parse(description, [0x20: 1, 0x25:1, 0x26: 1, 0x70: 1, 0x72: 2])
    if (cmd) {
        result = zwaveEvent(cmd)
        log.debug "Parsed $cmd to $result"
    } else {
        log.debug "Non-parsed event: $description"
    }
    result
}

def on() {
    delayBetween([
            zwave.switchMultilevelV1.switchMultilevelSet(value: 0xFF).format(),
            zwave.switchMultilevelV1.switchMultilevelGet().format()
    ], 1000)
}

def off() {
    delayBetween([
            zwave.switchMultilevelV1.switchMultilevelSet(value: 0x00).format(),
            zwave.switchMultilevelV1.switchMultilevelGet().format()
    ], 1000)
}

def setLevel(value, durationSeconds = null) {
    log.debug "setLevel >> value: $value, durationSeconds: $durationSeconds"
    def level = Math.max(Math.min(value as short, 99), 0)
    def dimmingDuration = 255
    def getStatusDelay = 1000
    if (durationSeconds != null) {
        durationSeconds = Math.max(Math.min(durationSeconds as int, 127 * 60), 0)
        dimmingDuration = durationSeconds < 128 ? durationSeconds : 127 + Math.round(durationSeconds / 60)
        getStatusDelay += 1000 * (durationSeconds < 128 ? durationSeconds : Math.round(durationSeconds / 60) * 60)
    }
    sendEvent(name: "level", value: level, unit: "%")
    sendEvent(name: "switch", value: level > 0 ? "on" : "off")
    delayBetween([
            zwave.switchMultilevelV2.switchMultilevelSet(value: level, dimmingDuration: dimmingDuration).format(),
            zwave.switchMultilevelV1.switchMultilevelGet().format()
    ], getStatusDelay)
}

def poll() {
    delayBetween(statusCommands, 100)
}

def ping() {
    poll()
}

def refresh() {
    def commands = statusCommands
    for (i in [5, 7, 8]) {
        commands << zwave.configurationV1.configurationGet(parameterNumber: i).format()
    }
    if (getDataValue("MSR") == null) {
        commands << zwave.manufacturerSpecificV1.manufacturerSpecificGet().format()
    }
    log.debug "Refreshing with commands $commands"
    delayBetween(commands, 100)
}

def indicatorNever() {
    sendEvent(name: "indicatorStatus", value: "never")
    configurationCommand(7, 0)
}

def indicatorWhenOff() {
    sendEvent(name: "indicatorStatus", value: "when off")
    configurationCommand(7, 255)
}

def indicatorWhenOn() {
    sendEvent(name: "indicatorStatus", value: "when on")
    configurationCommand(7, 254)
}

def low() {
    setLevel(10)
}

def medium() {
    setLevel(50)
}

def high() {
    setLevel(100)
}


private initialize() {
    // Device-Watch simply pings if no device events received for 32min(checkInterval)
    sendEvent(name: "checkInterval", value: 2 * 15 * 60 + 2 * 60, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])
}

private zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd) {
    dimmerEvent(cmd.value)
}

private zwaveEvent(physicalgraph.zwave.commands.switchmultilevelv1.SwitchMultilevelReport cmd) {
    dimmerEvent(cmd.value)
}

private zwaveEvent(physicalgraph.zwave.commands.switchmultilevelv1.SwitchMultilevelStopLevelChange cmd) {
    response(zwave.switchMultilevelV1.switchMultilevelGet().format())
}

private zwaveEvent(physicalgraph.zwave.commands.switchbinaryv1.SwitchBinaryReport cmd) {
    if (cmd.value == 0) {
        switchEvent(false)
    } else if (cmd.value == 255) {
        switchEvent(true)
    } else {
        log.debug "Bad switch value $cmd.value"
    }
}

private zwaveEvent(physicalgraph.zwave.commands.configurationv1.ConfigurationReport cmd) {
    def result = null
    switch (cmd.parameterNumber) {
        case 5:
            result = createEvent(name: "presetLevel", value: cmd.configurationValue[0])
            break
        case 7:
            def value = null
            switch (cmd.configurationValue[0]) {
                case 0: value = "never"; break
                case 254: value = "when on"; break
                case 255: value = "when off"; break
            }
            result = createEvent(name: "indicatorStatus", value: value)
            break
        case 8:
            def value = null
            switch (cmd.configurationValue[0]) {
                case 0: value = "incandescent"; break
                case 1: value = "led"; break
                case 2: value = "cfl"; break
            }
            result = createEvent(name: "loadType", value: value)
            break
    }
    result
}

private zwaveEvent(physicalgraph.zwave.commands.manufacturerspecificv2.ManufacturerSpecificReport cmd) {
    log.debug "manufacturerId:   $cmd.manufacturerId"
    log.debug "manufacturerName: $cmd.manufacturerName"
    log.debug "productId:        $cmd.productId"
    log.debug "productTypeId:    $cmd.productTypeId"
    def msr = String.format("%04X-%04X-%04X", cmd.manufacturerId, cmd.productTypeId, cmd.productId)
    updateDataValue("MSR", msr)
    updateDataValue("manufacturer", cmd.manufacturerName)
    createEvent([descriptionText: "$device.displayName MSR: $msr", isStateChange: false])
}

private zwaveEvent(physicalgraph.zwave.commands.hailv1.Hail cmd) {
    createEvent(name: "hail", value: "hail", descriptionText: "Switch button was pressed", displayed: false)
}

private zwaveEvent(physicalgraph.zwave.Command cmd) {
    log.warn "Unhandled zwave command $cmd"
}

private dimmerEvent(short level) {
    def result = null
    if (level == 0) {
        result = [createEvent(name: "level", value: 0, unit: "%"), switchEvent(false)]
    } else if (level >= 1 && level <= 100) {
        result = createEvent(name: "level", value: level, unit: "%")
        if (device.currentValue("switch") != "on") {
            // Don't blindly trust level. Explicitly request on/off status.
            result = [result, response(zwave.switchBinaryV1.switchBinaryGet().format())]
        }
    } else {
        log.debug "Bad dimming level $level"
    }
    result
}

private switchEvent(boolean on) {
    createEvent(name: "switch", value: on ? "on" : "off")
}

private getStatusCommands() {
    [
            // Even though SwitchBinary is not advertised by this device, it seems to be the only way to assess its true
            // on/off status.
            zwave.switchBinaryV1.switchBinaryGet().format(),
            zwave.switchMultilevelV1.switchMultilevelGet().format()
    ]
}

private configurationCommand(param, value) {
    param = param as short
    value = value as short
    delayBetween([
            zwave.configurationV1.configurationSet(parameterNumber: param, configurationValue: [value]).format(),
            zwave.configurationV1.configurationGet(parameterNumber: param).format()
    ], 1000)
}

private setPresetLevel(short level) {
    sendEvent(name: "presetLevel", value: level)
    configurationCommand(5, level)
}
