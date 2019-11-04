/**
 *  Virtual Device Creator
 *
 *  Copyright 2015 Bob Florian/SmartThings
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

definition(
        name: "Virtual Device Creator",
        namespace: "smartthings",
        author: "SmartThings",
        description: "Creates virtual devices",
        iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
        iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
        iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
        singleInstance: true,
        pausable: false
)


preferences {
    page name: "mainPage", title: "Click save to create a new virtual device.", install: true, uninstall: true
}

def mainPage() {
    dynamicPage(name: "mainPage") {
        section ("New Device") {
            input "virtualDeviceType", "enum", title: "Which type of virtual device do you want to create?", multiple: false, required: true, options: ["Virtual Switch", "Virtual Dimmer Switch"]
            input "theHub", "hub", title: "Select the hub (required for local execution) (Optional)", multiple: false, required: false
        }
        section("Device Name") {
            input "deviceName", title: "Enter device name", defaultValue: defaultLabel(), required: true
        }
        section("Devices Created") {
            paragraph "${getAllChildDevices().inject("") {result, i -> result + (i.label + "\n")} ?: ""}"
        }
        remove("Remove (Includes Devices)", "This will remove all virtual devices created through this app.")
    }
}

def defaultLabel() {
    "Virtual Device ${state.nextDni ?: 1}"
}

def installed() {
    log.debug "Installed with settings: ${settings}"
    state.nextDni = 1
}

def uninstalled() {
    getAllChildDevices().each {
        deleteChildDevice(it.deviceNetworkId, true)
    }
}

def updated() {
    log.debug "Updated with settings: ${settings}"
    initialize()
}

def initialize() {
    def latestDni = state.nextDni
    if (virtualDeviceType) {
        def d = addChildDevice("smartthings", virtualDeviceType, "virtual-$latestDni", theHub?.id, [completedSetup: true, label: deviceName])
        latestDni++
        state.nextDni = latestDni
    } else {
        log.error "Failed creating Virtual Device because the device type was missing"
    }
}