/**
 *  Tasmota - Virtual Motion Sensor
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

String driverVersion() { return "20201204" }
metadata {
    definition (name: "Tasmota Virtual Motion Sensor", namespace: "hongtat", author: "HongTat Tan", ocfDeviceType: "x.com.st.d.sensor.motion", vid: "generic-motion-2") {
        capability "Configuration"
        capability "Motion Sensor"
        capability "Sensor"
        capability "Health Check"

        attribute "lastSeen", "string"
    }

    preferences {
        section {
            input(title: "Device Settings",
                    description: "To view/update this settings, go to the Tasmota (Connect) SmartApp and select this device.",
                    displayDuringSetup: false,
                    type: "paragraph",
                    element: "paragraph")
            input(title: "", description: "Tasmota Virtual Motion Sensor v${driverVersion()}", displayDuringSetup: false, type: "paragraph", element: "paragraph")
        }
    }
    tiles(scale: 2) {
        multiAttributeTile(name: "motion", type: "generic", width: 6, height: 4) {
            tileAttribute("device.motion", key: "PRIMARY_CONTROL") {
                attributeState "active", label: 'motion', icon: "st.motion.motion.active", backgroundColor: "#00A0DC"
                attributeState "inactive", label: 'no motion', icon: "st.motion.motion.inactive", backgroundColor: "#cccccc"
            }
        }

        main (["motion"])
        details(["motion"])
    }
}

def parse(description) {
}

def parseEvents(status, json) {
    def events = []
    if (status as Integer == 200) {
        // rfData
        if (json?.rfData) {
            def data = parent.childSetting(device.id, ["payload_active", "payload_inactive"])
            def found = data.find{ it?.value?.toUpperCase() == json?.rfData?.toUpperCase() }?.key
            if (found && found == "payload_active") {
                def offDelay = parent.childSetting(device.id, "off_delay") ?: 0
                if (offDelay > 0) {
                    runIn(offDelay, "clearMotionStatus", [overwrite: true])
                }
                events << sendEvent(name: "motion", value: "active", descriptionText: "${device.displayName} detected motion")
            } else if (found && found == "payload_inactive") {
                events << sendEvent(name: "motion", value: "inactive", descriptionText: "${device.displayName} motion has stopped")
            }
        }
        // Bridge's Last seen
        if (json?.lastSeen) {
            events << sendEvent(name: "motion", value: device.currentValue("motion"))
            events << sendEvent(name: "lastSeen", value: json?.lastSeen, displayed: false)
        }
    }
    return events
}

def clearMotionStatus() {
    log.debug "inactive()"
    sendEvent(name: "motion", value: "inactive", descriptionText: "${device.displayName} motion has stopped")
}

def ping() {
    // Intentionally left blank as parent should handle this
}

def installed() {
    sendEvent(name: "checkInterval", value: 30 * 60 + 2 * 60, displayed: false, data: [protocol: "lan", hubHardwareId: device.hub.hardwareID])
    sendEvent(name: "motion", value: "inactive", displayed: false)
}

def updated() {
    initialize()
}

def initialize() {
}