/**
 *  Tasmota - Virtual Switch
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

String driverVersion() { return "20200913" }
metadata {
    definition (name: "Tasmota Virtual Switch", namespace: "hongtat", author: "HongTat Tan", ocfDeviceType: "oic.d.switch", vid: "generic-switch") {
        capability "Switch"
        capability "Actuator"
        capability "Configuration"
        capability "Sensor"
        capability "Health Check"
        capability "Signal Strength"

        attribute "lastSeen", "string"
    }

    preferences {
        section {
            input(title: "Device Settings",
                    description: "To view/update this settings, go to the Tasmota (Connect) SmartApp and select this device.",
                    displayDuringSetup: false,
                    type: "paragraph",
                    element: "paragraph")
            input(title: "", description: "Tasmota Virtual Switch v${driverVersion()}", displayDuringSetup: false, type: "paragraph", element: "paragraph")
        }
    }

    tiles(scale: 2) {
        multiAttributeTile(name: "switch", width: 6, height: 4, canChangeIcon: true) {
            tileAttribute("device.switch", key: "PRIMARY_CONTROL") {
                attributeState "on", label: '${name}', action: "switch.off", icon: "st.switches.light.on", backgroundColor: "#00a0dc"
                attributeState "off", label: '${name}', action: "switch.on", icon: "st.switches.light.off", backgroundColor: "#ffffff"
            }
        }

        standardTile("explicitOn", "device.switch", width: 2, height: 2, decoration: "flat") {
            state "default", label: "On", action: "switch.on", icon: "st.Home.home30", backgroundColor: "#ffffff"
        }
        standardTile("explicitOff", "device.switch", width: 2, height: 2, decoration: "flat") {
            state "default", label: "Off", action: "switch.off", icon: "st.Home.home30", backgroundColor: "#ffffff"
        }
        standardTile("lqi", "device.lqi", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
            state "default", label: 'LQI: ${currentValue}'
        }
        standardTile("rssi", "device.rssi", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
            state "default", label: 'RSSI: ${currentValue}dBm'
        }
        main(["switch"])
        details(["switch", "explicitOn", "explicitOff"])

    }
}

def parse(description) {
}

def parseEvents(status, json) {
    def events = []
    if (status as Integer == 200) {
        // rfData
        if (json?.rfData) {
            def data = parent.childSetting(device.id, ["payload_on", "payload_off"])
            def found = data.find{ it?.value?.toUpperCase() == json?.rfData?.toUpperCase() }?.key
            if (found && found == "payload_on") {
                events << sendEvent(name: "switch", value: "on", isStateChange: true)
            } else if (found && found == "payload_off") {
                events << sendEvent(name: "switch", value: "off", isStateChange: true)
            }
        }
        // irData
        if (json?.irData) {
            def data = parent.childSetting(device.id, ["payload_on", "payload_off"])
            def found = data.find{ it?.value?.toUpperCase() == json?.irData?.toUpperCase() }?.key
            if (found && found == "payload_on") {
                events << sendEvent(name: "switch", value: "on", isStateChange: true)
            } else if (found && found == "payload_off") {
                events << sendEvent(name: "switch", value: "off", isStateChange: true)
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

def on() {
    def bridge = parent.childSetting(device.id, "bridge") ?: null
    def command = parent.childSetting(device.id, "command_on") ?: null
    if (bridge && command) {
        parent.callTasmota(bridge, command)
        sendEvent(name: "switch", value: "on", isStateChange: true)
    }
}

def off() {
    def bridge = parent.childSetting(device.id, "bridge") ?: null
    def command = parent.childSetting(device.id, "command_off") ?: null
    if (bridge && command) {
        parent.callTasmota(bridge, command)
        sendEvent(name: "switch", value: "off", isStateChange: true)
    }
}

def ping() {
    // Intentionally left blank as parent should handle this
}

def installed() {
    sendEvent(name: "checkInterval", value: 30 * 60 + 2 * 60, displayed: false, data: [protocol: "lan", hubHardwareId: device.hub.hardwareID])
    sendEvent(name: "switch", value: "off")
}

def updated() {
    initialize()
}

def initialize() {
}