/**
 *  Copyright 2014 SmartThings
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
    // Automatically generated. Make future change here.
    definition (name: "Simulated Temperature Sensor", namespace: "smartthings/testing", author: "SmartThings") {
        capability "Temperature Measurement"
        capability "Switch Level"
        capability "Sensor"
        capability "Health Check"

        command "up"
        command "down"
        command "setTemperature", ["number"]
    }

    // UI tile definitions
    tiles {
        valueTile("temperature", "device.temperature", width: 2, height: 2) {
            state("temperature", label:'${currentValue}', unit:"F",
                backgroundColors:[
                    [value: 31, color: "#153591"],
                    [value: 44, color: "#1e9cbb"],
                    [value: 59, color: "#90d2a7"],
                    [value: 74, color: "#44b621"],
                    [value: 84, color: "#f1d801"],
                    [value: 95, color: "#d04e00"],
                    [value: 96, color: "#bc2323"]
                ]
            )
        }
        standardTile("up", "device.temperature", inactiveLabel: false, decoration: "flat") {
            state "default", label:'up', action:"up"
        }
        standardTile("down", "device.temperature", inactiveLabel: false, decoration: "flat") {
            state "default", label:'down', action:"down"
        }
        main "temperature"
        details("temperature","up","down")
    }
}

// Parse incoming device messages to generate events
def parse(String description) {
    def pair = description.split(":")
    createEvent(name: pair[0].trim(), value: pair[1].trim(), unit:"F")
}

def installed() {
    initialize()
}

def updated() {
    initialize()
}

def initialize() {
    sendEvent(name: "DeviceWatch-DeviceStatus", value: "online")
    sendEvent(name: "healthStatus", value: "online")
    sendEvent(name: "DeviceWatch-Enroll", value: [protocol: "cloud", scheme:"untracked"].encodeAsJson(), displayed: false)
    if (!device.currentState("temperature")) {
        setTemperature(getTemperature())
    }
}

def setLevel(value, rate = null) {
    setTemperature(value)
}

def up() {
    setTemperature(getTemperature() + 1)
}

def down() {
    setTemperature(getTemperature() - 1)
}

def setTemperature(value) {
    sendEvent(name:"temperature", value: value)
}

private getTemperature() {
    def ts = device.currentState("temperature")
    Integer value = ts ? ts.integerValue : 72
    return value
}
