/**
 *  Verisure
 *
 *  Copyright 2017 Anders Sveen & Martin Carlsson
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
 *  CHANGE LOG
 *  - 0.1   - Initial release
 *  - 0.2   - Updated to handle values from new API
 *  - 0.2.1 - Fixed error with ARMED_AWAY state
 *
 * Version: 0.2
 *
 */
metadata {
    definition(
            name: "Verisure Alarm",
            author: "Anders Sveen & Martin Carlsson",
            namespace: "smartthings.f12.no") {
        capability "Sensor"
        attribute "status", "string"
        attribute "loggedBy", "string"
        attribute "loggedWhen", "string"
    }

    simulator {}

    tiles(scale: 2) {
        standardTile("alarmTile", "device.status", width: 6, height: 4, canChangeBackground: true, canChangeIcon: true) {
            state "DISARMED", label: 'Disarmed', backgroundColor: "#6cd18e", icon: "st.Home.home2"
            state "ARMED_AWAY", label: 'Armed', backgroundColor: "#c36cd1", icon: "st.Home.home3"
            state "ARMED_HOME", label: 'Armed Home', backgroundColor: "#c36cd1", icon: "st.Home.home2"
        }
        valueTile("nameTile", "device.loggedBy", decoration: "flat", height: 2, width: 6, inactiveLabel: false) {
            state "loggedBy", label: 'By: ${currentValue}'
        }
        valueTile("dateTile", "device.loggedWhen", decoration: "flat", height: 2, width: 6, inactiveLabel: false) {
            state "loggedWhen", label: 'Time: ${currentValue}'
        }
        main("alarmTile")
        details(["alarmTile", "nameTile", "dateTile"])
    }
}

def parse(String description) {
    log.debug("[alarm.status] " + device.status)
    log.debug("[alarm.loggedBy] " + device.loggedBy)
    log.debug("[alarm.loggedWhen] " + device.loggedWhen)

    def evnt01 = createEvent(name: "status", value: device.status)
    def evnt02 = createEvent(name: "loggedBy", value: device.loggedBy)
    def evnt03 = createEvent(name: "loggedWhen", value: device.loggedWhen)

    return [evnt01, evnt02, evnt03]
}