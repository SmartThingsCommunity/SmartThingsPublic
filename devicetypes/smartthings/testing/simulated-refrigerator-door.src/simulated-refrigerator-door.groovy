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
metadata {
    definition (name: "Simulated Refrigerator Door", namespace: "smartthings/testing", author: "SmartThings") {
        capability "Contact Sensor"
        capability "Sensor"
        capability "Health Check"

        command "open"
        command "close"
    }

    tiles {
        standardTile("contact", "device.contact", width: 2, height: 2) {
            state("closed", label:'${name}', icon:"st.contact.contact.closed", backgroundColor:"#00A0DC", action: "open")
            state("open", label:'${name}', icon:"st.contact.contact.open", backgroundColor:"#e86d13", action: "close")
        }
        standardTile("freezerDoor", "device.contact", width: 2, height: 2, decoration: "flat") {
            state("closed", label:'Freezer', icon:"st.contact.contact.closed", backgroundColor:"#00A0DC")
            state("open", label:'Freezer', icon:"st.contact.contact.open", backgroundColor:"#e86d13")
        }
        standardTile("mainDoor", "device.contact", width: 2, height: 2, decoration: "flat") {
            state("closed", label:'Fridge', icon:"st.contact.contact.closed", backgroundColor:"#00A0DC")
            state("open", label:'Fridge', icon:"st.contact.contact.open", backgroundColor:"#e86d13")
        }
        standardTile("control", "device.contact", width: 1, height: 1, decoration: "flat") {
            state("closed", label:'${name}', icon:"st.contact.contact.closed", action: "open")
            state("open", label:'${name}', icon:"st.contact.contact.open", action: "close")
        }
        main "contact"
        details "contact"
    }
}

def installed() {
    initialize()
}

def updated() {
    initialize()
}

def initialize() {
    sendEvent(name: "contact", value: "closed")

    sendEvent(name: "DeviceWatch-DeviceStatus", value: "online")
    sendEvent(name: "healthStatus", value: "online")
    sendEvent(name: "DeviceWatch-Enroll", value: [protocol: "cloud", scheme:"untracked"].encodeAsJson(), displayed: false)
}


def open() {
    sendEvent(name: "contact", value: "open")
    parent.doorOpen(device.deviceNetworkId)
}

def close() {
    sendEvent(name: "contact", value: "closed")
    parent.doorClosed(device.deviceNetworkId)
}
