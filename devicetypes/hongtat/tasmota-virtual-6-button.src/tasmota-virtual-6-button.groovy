/**
 *  Tasmota - Virtual 6-button
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
private getButtonLabels() {[
    "button_1", "button_2", "button_3", "button_4", "button_5", "button_6"
]}

import groovy.json.JsonOutput
String driverVersion() { return "20200913" }
metadata {
    definition (name: "Tasmota Virtual 6 Button", namespace: "hongtat", author: "HongTat Tan", ocfDeviceType: "x.com.st.d.remotecontroller", mcdSync: true, vid: "generic-6-button") {
        capability "Button"
        capability "Sensor"
        capability "Health Check"
        capability "Configuration"

        attribute "lastSeen", "string"
    }

    preferences {
        section {
            input(title: "Device Settings",
                    description: "To view/update this settings, go to the Tasmota (Connect) SmartApp and select this device.",
                    displayDuringSetup: false,
                    type: "paragraph",
                    element: "paragraph")
            input(title: "", description: "Tasmota Virtual 6 Button v${driverVersion()}", displayDuringSetup: false, type: "paragraph", element: "paragraph")
        }
    }

    tiles(scale: 2) {
        multiAttributeTile(name: "button", type: "generic", width: 6, height: 4, canChangeIcon: true) {
            tileAttribute("device.button", key: "PRIMARY_CONTROL") {
                attributeState "default", label: ' ', icon: "st.unknown.zwave.remote-controller", backgroundColor: "#ffffff"
            }
        }

        main "button"
        details(["button"])
    }
}

def installed() {
    sendEvent(name: "checkInterval", value: 30 * 60 + 2 * 60, displayed: false, data: [protocol: "lan", hubHardwareId: device.hub.hardwareID])
    sendEvent(name: "button", value: "pushed", isStateChange: true, displayed: false)
    sendEvent(name: "supportedButtonValues", value: supportedButtonValues.encodeAsJSON(), displayed: false)
    log.debug "Installed"
    initialize()
}

def updated() {
    log.debug "updated()"
    if (childDevices && device.label != state.oldLabel) {
        childDevices.each {
            def newLabel = getButtonName(channelNumber(it.deviceNetworkId))
            it.setLabel(newLabel)
        }
        state.oldLabel = device.label
    }
}

def initialize() {
    def numberOfButtons = buttonLabels.size()
    log.debug "initialize(); numberOfButtons: ${numberOfButtons}"
    sendEvent(name: "numberOfButtons", value: numberOfButtons, displayed: false)
    if (!childDevices) {
        addChildButtons(numberOfButtons)
        if (childDevices) {
            for (def endpoint : 1..numberOfButtons) {
                String childDni = "${device.deviceNetworkId}:$endpoint"
                def child = childDevices.find { it.deviceNetworkId == childDni }
                child?.sendEvent(name: "button", value: "pushed", isStateChange: true)
            }
        }
    }
}

def configure() {
}

def parse(description) {
}

def parseEvents(status, json) {
    def events = []
    if (status as Integer == 200) {
        // rfData
        if (json?.rfData) {
            def numberOfButtons = buttonLabels.size()
            def data = parent.childSetting(device.id, buttonLabels)
            def found = data.find{ it?.value?.toUpperCase() == json?.rfData?.toUpperCase() }?.key
            for(def buttonNumber : 1..numberOfButtons) {
                if (found && found == "button_${buttonNumber}") {
                    def description = "Button ${buttonNumber} was pushed"
                    String childDni = "${device.deviceNetworkId}:$buttonNumber"
                    def child = childDevices.find { it.deviceNetworkId == childDni }
                    child?.sendEvent(name: "button", value: "pushed", descriptionText: description, data: [buttonNumber: buttonNumber], isStateChange: true)
                }
            }
        }
        // irData
        if (json?.irData) {
            def numberOfButtons = buttonLabels.size()
            def data = parent.childSetting(device.id, buttonLabels)
            def found = data.find{ it?.value?.toUpperCase() == json?.irData?.toUpperCase() }?.key
            for(def buttonNumber : 1..numberOfButtons) {
                if (found && found == "button_${buttonNumber}") {
                    def description = "Button ${buttonNumber} was pushed"
                    String childDni = "${device.deviceNetworkId}:$buttonNumber"
                    def child = childDevices.find { it.deviceNetworkId == childDni }
                    child?.sendEvent(name: "button", value: "pushed", descriptionText: description, data: [buttonNumber: buttonNumber], isStateChange: true)
                }
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

private addChildButtons(numberOfButtons) {
    state.oldLabel = device.label
    for(def endpoint : 1..numberOfButtons) {
        try {
            String childDni = "${device.deviceNetworkId}:${endpoint}"
            def child = addChildDevice("Tasmota Child Button Device", childDni, device.getHub().getId(), [
                    completedSetup: true,
                    label         : getButtonName(endpoint),
                    isComponent   : true,
                    componentName : "button$endpoint",
                    componentLabel: "Button $endpoint"
            ])
            child.sendEvent(name: "supportedButtonValues", value: supportedButtonValues.encodeAsJSON(), displayed: false)
        } catch(Exception e) {
            log.debug "Exception: ${e}"
        }
    }
}

private channelNumber(String dni) {
    dni.split(":")[-1] as Integer
}

private getButtonName(buttonNum) {
    return "${device.displayName} " + "Button ${buttonNum}"
}

private getSupportedButtonValues() {[
    "pushed"
]}