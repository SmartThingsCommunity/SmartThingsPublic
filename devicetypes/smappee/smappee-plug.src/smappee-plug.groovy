/**
 *  Smappee Plug
 *
 *  Copyright 2015 Chuck J
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
    definition (name: "Smappee Plug", namespace: "smappee", author: "Chuck J") {
        capability "Actuator"
        capability "Switch"
        capability "Sensor"
    }

    // simulator metadata
    simulator {
    }

    // UI tile definitions
    tiles(scale: 2) {
        multiAttributeTile(name: "button", type: "generic", width: 6, height: 4) {
            tileAttribute("device.switch", key: "PRIMARY_CONTROL") {
                attributeState "off", label: 'Off', action: "switch.on", icon: "st.switches.switch.off", backgroundColor: "#ffffff", nextState: "on"
                attributeState "on", label: 'On', action: "switch.off", icon: "st.switches.switch.on", backgroundColor: "#79b821", nextState: "off"
            }
        }
        main "button"
        details "button"
    }
}

def parse(String description) {
    log.debug "Smappee Plug parse: ${description}"
    log.debug device.deviceNetworkId
}

def on() {

    log.debug "$device.deviceNetworkId is turning on"
    parent.turnOn(device)
    sendEvent(name: "switch", value: "on")
    log.debug "$device.deviceNetworkId turned on"
}

def off() {

    log.debug "$device.deviceNetworkId is turning off"
    parent.turnOff(device)
    sendEvent(name: "switch", value: "off")
    log.debug "$device.deviceNetworkId turned off"
}
