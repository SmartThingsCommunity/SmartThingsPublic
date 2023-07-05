/**
 *  Tasmota - Child Switch Device
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
    definition(name: "Tasmota Child Switch Device", namespace: "hongtat", author: "HongTat Tan", vid: "generic-switch") {
        capability "Switch"
        capability "Actuator"
        capability "Sensor"
        capability "Health Check"
        capability "Refresh"
    }

    tiles(scale: 2) {
        multiAttributeTile(name: "switch", width: 6, height: 4, canChangeIcon: true) {
            tileAttribute("device.switch", key: "PRIMARY_CONTROL") {
                attributeState "on", label: '${name}', action: "switch.off", icon: "st.switches.light.on", backgroundColor: "#00a0dc"
                attributeState "off", label: '${name}', action: "switch.on", icon: "st.switches.light.off", backgroundColor: "#ffffff"
            }
        }
        standardTile("refresh", "device.switch", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
            state "default", label:"", action:"refresh.refresh", icon:"st.secondary.refresh"
        }

        main "switch"
        details(["switch", "refresh"])
    }
}

def installed() {
    sendEvent(name: "checkInterval", value: 30 * 60 + 2 * 60, displayed: false, data: [protocol: "lan", hubHardwareId: device.hub.hardwareID])
    sendEvent(name: "switch", value: "off")
}

void on() {
    parent.childOn(device.deviceNetworkId)
}

void off() {
    parent.childOff(device.deviceNetworkId)
}

def ping() {
    // Intentionally left blank as parent should handle this
}

void refresh() {
    parent.refresh(device.deviceNetworkId)
}

def uninstalled() {
    parent.delete()
}