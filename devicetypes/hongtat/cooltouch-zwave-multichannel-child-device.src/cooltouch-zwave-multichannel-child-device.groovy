/**
 *  Z-Wave Light Switch Multichannel - Device Handler (child)
 *
 *  Copyright 2017 HongTat Tan
 *
 *
 *  Version history:
 *      1.0 (23/09/2017) - Initial Release
 *
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
    definition (name: "COOLTOUCH Z-Wave Multichannel Child Device", namespace: "hongtat", author: "HongTat Tan") {
        capability "Switch"
        capability "Actuator"
        capability "Sensor"
        capability "Refresh"
        capability "Health Check"
        capability "Configuration"

        attribute "lastCheckin", "String"
    }

    tiles(scale: 2) {
        multiAttributeTile(name: "switch", type: "lighting", width: 6, height: 4, canChangeIcon: true) {
            tileAttribute("device.switch", key: "PRIMARY_CONTROL") {
                attributeState "off", label: '${name}', action: "switch.on", icon: "st.switches.switch.off", backgroundColor: "#ffffff", nextState: "turningOn"
                attributeState "on", label: '${name}', action: "switch.off", icon: "st.switches.switch.on", backgroundColor: "#00a0dc", nextState: "turningOff"
                attributeState "turningOn", label: '${name}', action: "switch.off", icon: "st.switches.switch.on", backgroundColor: "#00a0dc", nextState: "turningOff"
                attributeState "turningOff", label: '${name}', action: "switch.on", icon: "st.switches.switch.off", backgroundColor: "#ffffff", nextState: "turningOn"
            }
            tileAttribute("device.lastCheckin", key: "SECONDARY_CONTROL") {
                attributeState("default", label:'${currentValue}')
            }
        }
        standardTile("refresh", "device.refresh", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
            state "default", label:"", action:"refresh.refresh", icon:"st.secondary.refresh"
        }
        standardTile("configure", "device.default", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
            state "default", label:"", action:"configure", icon:"st.secondary.configure"
        }
    }
}

void on() {
    parent.onOffCmd(device.deviceNetworkId, 0xFF)
}

void off() {
    parent.onOffCmd(device.deviceNetworkId, 0)
}

void refresh() {
    parent.refresh()
}

void configure() {
    parent.configure()
}