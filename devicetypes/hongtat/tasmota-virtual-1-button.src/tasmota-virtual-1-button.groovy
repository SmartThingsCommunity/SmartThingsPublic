/**
 *  Tasmota - Virtual 1-button
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
    "button_1",
]}

import groovy.json.JsonOutput
String driverVersion() { return "20201218" }
metadata {
    definition (name: "Tasmota Virtual 1 Button", namespace: "hongtat", author: "HongTat Tan", ocfDeviceType: "x.com.st.d.remotecontroller", vid: "b09a9fcf-f5ee-34f0-9a4d-4b7bf0f16b90", mnmn: "SmartThingsCommunity") {
        capability "Button"
        capability "Sensor"
        capability "Health Check"
        capability "Refresh"
        capability "Configuration"
        capability "Momentary"
        capability "Motion Sensor"

        attribute "lastSeen", "string"
    }

    preferences {
        section {
            input(title: "Device Settings",
                    description: "To view/update this settings, go to the Tasmota (Connect) SmartApp and select this device.",
                    displayDuringSetup: false,
                    type: "paragraph",
                    element: "paragraph")
            input(title: "", description: "Tasmota Virtual 1 Button v${driverVersion()}", displayDuringSetup: false, type: "paragraph", element: "paragraph")
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
    sendEvent(name: "motion", value: "inactive", displayed: false)
    sendEvent(name: "supportedButtonValues", value: supportedButtonValues.encodeAsJSON(), displayed: false)
    log.debug "Installed"
    initialize()
}

def updated() {
}

def initialize() {
    def numberOfButtons = buttonLabels.size()
    log.debug "initialize(); numberOfButtons: ${numberOfButtons}"
    sendEvent(name: "numberOfButtons", value: numberOfButtons, displayed: false)
    def syncFrequency = (parent.generalSetting("frequency") ?: 'Every 1 minute').replace('Every ', 'Every').replace(' minute', 'Minute').replace(' hour', 'Hour')
    try {
        "run$syncFrequency"(refresh)
    } catch (all) { }
    sendEvent(name: "checkInterval", value: parent.checkInterval(), displayed: false, data: [protocol: "lan", hubHardwareId: device.hub.hardwareID])
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
            Integer buttonNumber = 1
            if (found && found == "button_${buttonNumber}") {
                String description = "Button ${buttonNumber} was pushed"
                events << sendEvent(name: "button", value: "pushed", descriptionText: description, data: [buttonNumber: buttonNumber], isStateChange: true)
                events << sendEvent(name: "motion", value: "active", displayed: false)
                runIn(1, "clearMotionStatus", [overwrite: true])
            }
        }
        // irData
        if (json?.irData) {
            def numberOfButtons = buttonLabels.size()
            def data = parent.childSetting(device.id, buttonLabels)
            def found = data.find{ it?.value?.toUpperCase() == json?.irData?.toUpperCase() }?.key
            Integer buttonNumber = 1
            if (found && found == "button_${buttonNumber}") {
                String description = "Button ${buttonNumber} was pushed"
                events << sendEvent(name: "button", value: "pushed", descriptionText: description, data: [buttonNumber: buttonNumber], isStateChange: true)
                events << sendEvent(name: "motion", value: "active", displayed: false)
                runIn(1, "clearMotionStatus", [overwrite: true])
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

def push() {
    Integer buttonNumber = 1
    String description = "Button ${buttonNumber} was pushed"
    sendEvent(name: "button", value: "pushed", descriptionText: description, data: [buttonNumber: buttonNumber], isStateChange: true)
    sendEvent(name: "motion", value: "active", displayed: false)
    runIn(1, "clearMotionStatus", [overwrite: true])
}

def clearMotionStatus() {
    sendEvent(name: "motion", value: "inactive", displayed: false)
}

def refresh(dni=null) {
    def bridge = parent.childSetting(device.id, "bridge") ?: null
    if (bridge == null) {
        sendEvent(name: "button", value: device.currentValue("button"))
    }
}

def ping() {
    refresh()
}

private getSupportedButtonValues() {[
    "pushed"
]}