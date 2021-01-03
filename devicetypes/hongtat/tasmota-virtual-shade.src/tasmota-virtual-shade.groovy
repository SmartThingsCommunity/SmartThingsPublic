/**
 *  Tasmota - Virtual Shade
 *
 *  Copyright 2020 AwfullySmart.com - HongTat Tan
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import groovy.json.JsonOutput
String driverVersion() { return "20201004" }
metadata {
    definition (name: "Tasmota Virtual Shade", namespace: "hongtat", author: "HongTat Tan", ocfDeviceType: "oic.d.blind", vid: "generic-shade") {
        capability "Actuator"
        capability "Window Shade"
        capability "Health Check"
        capability "Signal Strength"

        command "pause"

        attribute "lastSeen", "string"
    }

    preferences {
        section {
            input(title: "Device Settings",
                    description: "To view/update this settings, go to the Tasmota (Connect) SmartApp and select this device.",
                    displayDuringSetup: false,
                    type: "paragraph",
                    element: "paragraph")
            input(title: "", description: "Tasmota Virtual Shade v${driverVersion()}", displayDuringSetup: false, type: "paragraph", element: "paragraph")
        }
    }

    tiles(scale: 2) {
        multiAttributeTile(name:"windowShade", type: "generic", width: 6, height: 4) {
            tileAttribute("device.windowShade", key: "PRIMARY_CONTROL") {
                attributeState "open", label:'${name}', action:"close", icon:"st.shades.shade-open", backgroundColor:"#00A0DC", nextState:"closing"
                attributeState "closed", label:'${name}', action:"open", icon:"st.shades.shade-closed", backgroundColor:"#ffffff", nextState:"opening"
                attributeState "partially open", label:'Open', action:"close", icon:"st.shades.shade-open", backgroundColor:"#00A0DC", nextState:"closing"
                attributeState "opening", label:'${name}', action:"stop", icon:"st.shades.shade-opening", backgroundColor:"#00A0DC", nextState:"partially open"
                attributeState "closing", label:'${name}', action:"stop", icon:"st.shades.shade-closing", backgroundColor:"#ffffff", nextState:"partially open"
            }
        }
        standardTile("contPause", "device.switch", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
            state "pause", label:"", icon:'st.sonos.pause-btn', action:'pause', backgroundColor:"#cccccc"
        }
        standardTile("lqi", "device.lqi", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
            state "default", label: 'LQI: ${currentValue}'
        }
        standardTile("rssi", "device.rssi", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
            state "default", label: 'RSSI: ${currentValue}dBm'
        }
        main(["windowShade"])
        details(["windowShade", "contPause"])

    }
}

def parse(description) {
}

def parseEvents(status, json) {
    def events = []
    if (status as Integer == 200) {
        // rfData
        if (json?.rfData) {
            def data = parent.childSetting(device.id, ["payload_open", "payload_close", "payload_pause"])
            def found = data.find{ it?.value?.toUpperCase() == json?.rfData?.toUpperCase() }?.key
            if (found && found == "payload_open") {
                events << sendEvent(name: "windowShade", value: "open", isStateChange: true)
            } else if (found && found == "payload_close") {
                events << sendEvent(name: "windowShade", value: "closed", isStateChange: true)
            } else if (found && found == "payload_pause") {
                events << sendEvent(name: "windowShade", value: "partially open", isStateChange: true)
            }
        }
        // irData
        if (json?.irData) {
            def data = parent.childSetting(device.id, ["payload_open", "payload_close", "payload_pause"])
            def found = data.find{ it?.value?.toUpperCase() == json?.irData?.toUpperCase() }?.key
            if (found && found == "payload_open") {
                events << sendEvent(name: "windowShade", value: "open", isStateChange: true)
            } else if (found && found == "payload_close") {
                events << sendEvent(name: "windowShade", value: "closed", isStateChange: true)
            } else if (found && found == "payload_pause") {
                events << sendEvent(name: "windowShade", value: "partially open", isStateChange: true)
            }
        }
        // Bridge's Signal Strength
        if (json?.Wifi) {
            events << sendEvent(name: "lqi", value: json?.Wifi.RSSI, displayed: false)
            events << sendEvent(name: "rssi", value: json?.Wifi.Signal, displayed: false)
        }
        // Bridge's Last seen
        if (json?.lastSeen) {
            events << sendEvent(name: "lastSeen", value: json?.lastSeen, displayed: false)
        }
    }
    return events
}

def open() {
    def bridge = parent.childSetting(device.id, "bridge") ?: null
    def command = parent.childSetting(device.id, "command_open") ?: null
    if (bridge && command) {
        parent.callTasmota(bridge, command)
        sendEvent(name: "windowShade", value: "open", isStateChange: true)
    }
}

def close() {
    def bridge = parent.childSetting(device.id, "bridge") ?: null
    def command = parent.childSetting(device.id, "command_close") ?: null
    if (bridge && command) {
        parent.callTasmota(bridge, command)
        sendEvent(name: "windowShade", value: "closed", isStateChange: true)
    }
}

def pause() {
    def bridge = parent.childSetting(device.id, "bridge") ?: null
    def command = parent.childSetting(device.id, "command_pause") ?: null
    if (bridge && command) {
        parent.callTasmota(bridge, command)
        sendEvent(name: "windowShade", value: "partially open", isStateChange: true)
    }
}

def ping() {
    // Intentionally left blank as parent should handle this
}

def installed() {
    sendEvent(name: "checkInterval", value: 30 * 60 + 2 * 60, displayed: false, data: [protocol: "lan", hubHardwareId: device.hub.hardwareID])
    sendEvent(name: "supportedWindowShadeCommands", value: JsonOutput.toJson(["open", "close", "pause"]), displayed: false)
    sendEvent(name: "windowShade", value: "open", isStateChange: true)
}

def updated() {
    initialize()
}

def initialize() {
}