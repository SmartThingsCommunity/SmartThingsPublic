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
 *  - 0.1.1 - Corrected capabilities to improve interoperability with other apps
 *
 * Version: 0.1.1
 *
 */
metadata {
    definition(
            name: "Verisure Sensor",
            author: "Martin Carlsson",
            namespace: "smartthings.f12.no") {
        capability "Temperature Measurement"
        capability "Relative Humidity Measurement"
        capability "Sensor"
        attribute "timestamp", "string"
        attribute "type", "string"
        attribute "humidity", "number"
        attribute "temperature", "number"
    }

    simulator {}

    tiles(scale: 2) {
        multiAttributeTile(name: "temperatureTile", type: "generic", width: 6, height: 4, canChangeIcon: false) {
            tileAttribute("device.temperature", key: "PRIMARY_CONTROL") {
                attributeState("temperature", label: '${currentValue}', unit: "dF", backgroundColors: [
                        [value: 31, color: "#153591"],
                        [value: 44, color: "#1e9cbb"],
                        [value: 59, color: "#90d2a7"],
                        [value: 74, color: "#44b621"],
                        [value: 84, color: "#f1d801"],
                        [value: 95, color: "#d04e00"],
                        [value: 96, color: "#bc2323"]
                ])
            }
            tileAttribute("device.humidity", key: "SECONDARY_CONTROL") {
                attributeState("humidity", label: '${currentValue}%', unit: "%")
            }
        }
        valueTile("timestampTile", "device.timestamp", decoration: "flat", height: 2, width: 6, inactiveLabel: false) {
            state "timestamp", label: 'Updated: ${currentValue}'
        }
        valueTile("typeTile", "device.type", decoration: "flat", height: 2, width: 6, inactiveLabel: false) {
            state "type", label: 'Type: ${currentValue}'
        }
        main("temperatureTile")
        details(["temperatureTile", "typeTile", "timestampTile"])
    }

}

def parse(String description) {
    log.debug("[deviceParse] " + device)
    log.debug('[device.currentValue("timestamp")] ' + device.currentValue("timestamp"))
    log.debug('[device.currentValue("humidity")] ' + device.currentValue("humidity"))
    log.debug('[device.currentValue("type")] ' + device.currentValue("type"))
    log.debug('[device.currentValue("temperature")] ' + device.currentValue("temperature"))

    def evnt01 = createEvent(name: "timestamp", value: device.currentValue("timestamp"))
    def evnt02 = createEvent(name: "humidity", value: device.currentValue("humidity"))
    def evnt03 = createEvent(name: "type", value: device.currentValue("type"))
    def evnt04 = createEvent(name: "temperature", value: device.currentValue("temperature"))

    return [evnt01, evnt02, evnt03, evnt04]
}