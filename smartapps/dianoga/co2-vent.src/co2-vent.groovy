/**
 *  CO2 Vent
 *
 *  Copyright 2014 Brian Steere
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
        name: "CO2 Vent",
        namespace: "dianoga",
        author: "Brian Steere",
        description: "Turn on a switch when CO2 levels are too high",
        category: "Health & Wellness",
        iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
        iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
        pausable: true
)

preferences {
    section("CO2 Sensor") {
        input "sensor", "capability.carbonDioxideMeasurement", title: "Sensor", required: true
        input "level", "number", title: "CO2 Level", required: true
    }

    section("Ventilation Fan") {
        input "switches", "capability.switch", title: "Switches", required: true, multiple: true
    }
}

def installed() {
    log.debug "Installed with settings: ${settings}"

    initialize()
}

def updated() {
    log.debug "Updated with settings: ${settings}"

    unsubscribe()
    initialize()
}

def initialize() {
    state.active = false;
    subscribe(sensor, "carbonDioxide", 'handleLevel')
}

def handleLevel(evt) {
    def co2 = sensor.currentValue("carbonDioxide").toInteger()
    log.debug "CO2 Level: ${co2} / ${settings.level} Active: ${state.active}"

    if(co2 >= settings.level && !state.active) {
        log.debug "Turning on"
        switches.each { it.on(); }
        state.active = true;
    } else if(co2 < settings.level && state.active) {
        log.debug "Turning off"
        state.active = false;
        switches.each { it.off(); }
    }
}
