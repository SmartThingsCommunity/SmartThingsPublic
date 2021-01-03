/**
 *  Tasmota - Virtual Contact Sensor
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
    definition (name: "Tasmota Virtual Contact Sensor", namespace: "hongtat", author: "HongTat Tan", vid: "generic-contact-3", ocfDeviceType: "x.com.st.d.sensor.contact") {
        capability "Configuration"
        capability "Contact Sensor"
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
            input(title: "", description: "Tasmota Virtual Contact Sensor v${driverVersion()}", displayDuringSetup: false, type: "paragraph", element: "paragraph")
        }
    }
    tiles(scale: 2) {
        multiAttributeTile(name: "contact", type: "generic", width: 6, height: 4){
            tileAttribute ("device.contact", key: "PRIMARY_CONTROL") {
                attributeState "open", label:'${name}', icon:"st.contact.contact.open", backgroundColor:"#e86d13"
                attributeState "closed", label:'${name}', icon:"st.contact.contact.closed", backgroundColor:"#00a0dc"
            }
        }

        main (["contact"])
        details(["contact"])
    }
}

def parse(description) {
}

def parseEvents(status, json) {
    def events = []
    if (status as Integer == 200) {
        // rfData
        if (json?.rfData) {
            def data = parent.childSetting(device.id, ["payload_open", "payload_close"])
            def found = data.find{ it?.value?.toUpperCase() == json?.rfData?.toUpperCase() }?.key
            if (found && found == "payload_open") {
                def offDelay = parent.childSetting(device.id, "off_delay") ?: 0
                if (offDelay > 0) {
                    runIn(offDelay, "clearOpenStatus", [overwrite: true])
                }
                events << sendEvent(name: "contact", value: "open", isStateChange: true)
            } else if (found && found == "payload_close") {
                events << sendEvent(name: "contact", value: "closed", isStateChange: true)
            }
        }
        // Bridge's Last seen
        if (json?.lastSeen) {
            events << sendEvent(name: "contact", value: device.currentValue("contact"))
            events << sendEvent(name: "lastSeen", value: json?.lastSeen, displayed: false)
        }
    }
    return events
}

def clearOpenStatus() {
    log.debug "closed()"
    sendEvent(name: "contact", value: "closed", isStateChange: true)
}

def ping() {
    // Intentionally left blank as parent should handle this
}

def installed() {
    sendEvent(name: "checkInterval", value: 30 * 60 + 2 * 60, displayed: false, data: [protocol: "lan", hubHardwareId: device.hub.hardwareID])
    sendEvent(name: "contact", value: "open", displayed: false)
}

def updated() {
    initialize()
}

def initialize() {
}