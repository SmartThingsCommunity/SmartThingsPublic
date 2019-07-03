/**
 *  Simulated Refrigerator
 *
 *  Example composite device handler that simulates a refrigerator with a freezer compartment and a main compartment.
 *  Each of these compartments has its own door, temperature, and temperature setpoint. Each compartment modeled
 *  as a child device of the main refrigerator device so that temperature-based SmartApps can be used with each
 *  compartment
 *
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
    definition (name: "Simulated Refrigerator", namespace: "smartthings/testing", author: "SmartThings") {
        capability "Contact Sensor"
        capability "Health Check"
    }

    tiles(scale: 2) {
        standardTile("contact", "device.contact", width: 4, height: 4) {
            state("closed", label:'${name}', icon:"st.fridge.fridge-closed", backgroundColor:"#00A0DC")
            state("open", label:'${name}', icon:"st.fridge.fridge-open", backgroundColor:"#e86d13")
        }
        childDeviceTile("freezerDoor", "freezerDoor", height: 2, width: 2, childTileName: "freezerDoor")
        childDeviceTile("mainDoor", "mainDoor", height: 2, width: 2, childTileName: "mainDoor")
        childDeviceTile("freezer", "freezer", height: 2, width: 2, childTileName: "freezer")
        childDeviceTile("refrigerator", "refrigerator", height: 2, width: 2, childTileName: "refrigerator")
        childDeviceTile("freezerSetpoint", "freezer", height: 1, width: 2, childTileName: "freezerSetpoint")
        childDeviceTile("refrigeratorSetpoint", "refrigerator", height: 1, width: 2, childTileName: "refrigeratorSetpoint")

        // for simulator
        childDeviceTile("freezerUp", "freezer", height: 1, width: 1, childTileName: "tempUp")
        childDeviceTile("freezerDown", "freezer", height: 1, width: 1, childTileName: "tempDown")
        childDeviceTile("refrigeratorUp", "refrigerator", height: 1, width: 1, childTileName: "tempUp")
        childDeviceTile("refrigeratorDown", "refrigerator", height: 1, width: 1, childTileName: "tempDown")
        childDeviceTile("freezerDoorControl", "freezerDoor", height: 1, width: 1, childTileName: "control")
        childDeviceTile("mainDoorControl", "mainDoor", height: 1, width: 1, childTileName: "control")
        childDeviceTile("freezerSetpointUp", "freezer", height: 1, width: 1, childTileName: "setpointUp")
        childDeviceTile("freezerSetpointDown", "freezer", height: 1, width: 1, childTileName: "setpointDown")
        childDeviceTile("refrigeratorSetpointUp", "refrigerator", height: 1, width: 1, childTileName: "setpointUp")
        childDeviceTile("refrigeratorSetpointDown", "refrigerator", height: 1, width: 1, childTileName: "setpointDown")
    }
}

def installed() {
    state.counter = state.counter ? state.counter + 1 : 1
    if (state.counter == 1) {
        addChildDevice(
                "Simulated Refrigerator Door",
                "${device.deviceNetworkId}.1",
                null,
                [completedSetup: true, label: "${device.label} (Freezer Door)", componentName: "freezerDoor", componentLabel: "Freezer Door"])

        addChildDevice(
                "Simulated Refrigerator Door",
                "${device.deviceNetworkId}.2",
                null,
                [completedSetup: true, label: "${device.label} (Main Door)", componentName: "mainDoor", componentLabel: "Main Door"])

        addChildDevice(
                "Simulated Refrigerator Temperature Control",
                "${device.deviceNetworkId}.3",
                null,
                [completedSetup: true, label: "${device.label} (Freezer)", componentName: "freezer", componentLabel: "Freezer"])

        addChildDevice(
                "Simulated Refrigerator Temperature Control",
                "${device.deviceNetworkId}.3",
                null,
                [completedSetup: true, label: "${device.label} (Fridge)", componentName: "refrigerator", componentLabel: "Fridge"])
    }
    initialize()
}

def updated() {
    initialize()
}

def initialize() {
    sendEvent(name: "DeviceWatch-DeviceStatus", value: "online")
    sendEvent(name: "healthStatus", value: "online")
    sendEvent(name: "DeviceWatch-Enroll", value: [protocol: "cloud", scheme:"untracked"].encodeAsJson(), displayed: false)
}

def doorOpen(dni) {
    // If any door opens, then the refrigerator is considered to be open
    sendEvent(name: "contact", value: "open")
}

def doorClosed(dni) {
    // Both doors must be closed for the refrigerator to be considered closed
    if (!childDevices.find{it.deviceNetworkId != dni && it.currentValue("contact") == "open"}) {
        sendEvent(name: "contact", value: "closed")
    }
}
