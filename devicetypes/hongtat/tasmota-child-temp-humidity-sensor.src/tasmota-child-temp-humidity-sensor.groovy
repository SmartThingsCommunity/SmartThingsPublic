/**
 *  Tasmota - Child Temp/Humidity Sensor
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

String driverVersion() { return "20200913" }
metadata {
    definition(name: "Tasmota Child Temp/Humidity Sensor", namespace: "hongtat", author: "HongTat Tan", ocfDeviceType: "oic.d.thermostat", vid: "60f649d4-4d75-32f0-9002-6c50d1380815", mnmn: "SmartThingsCommunity") {
        capability "Configuration"
        capability "Refresh"
        capability "Temperature Measurement"
        capability "Relative Humidity Measurement"
        capability "Health Check"
        capability "Sensor"
    }

    preferences {
        input "tempOffset", "number", title: "Temperature Offset", description: "Adjust temperature by this many degrees", range: "*..*", displayDuringSetup: false
        input "humidityOffset", "number", title: "Humidity Offset", description: "Adjust humidity by this percentage", range: "*..*", displayDuringSetup: false
        input(title: "", description: "Tasmota Child Temp/Humidity Sensor v${driverVersion()}", displayDuringSetup: false, type: "paragraph", element: "paragraph")
    }

    tiles(scale: 2) {
        multiAttributeTile(name: "temperature", type: "generic", width: 6, height: 4, canChangeIcon: true) {
            tileAttribute("device.temperature", key: "PRIMARY_CONTROL") {
                attributeState "temperature", label: '${currentValue}°',
                    backgroundColors:[
                        // Celsius
                        [value: 0, color: "#153591"],
                        [value: 7, color: "#1e9cbb"],
                        [value: 15, color: "#90d2a7"],
                        [value: 23, color: "#44b621"],
                        [value: 28, color: "#f1d801"],
                        [value: 35, color: "#d04e00"],
                        [value: 37, color: "#bc2323"],
                        // Fahrenheit
                        [value: 40, color: "#153591"],
                        [value: 44, color: "#1e9cbb"],
                        [value: 59, color: "#90d2a7"],
                        [value: 74, color: "#44b621"],
                        [value: 84, color: "#f1d801"],
                        [value: 95, color: "#d04e00"],
                        [value: 96, color: "#bc2323"]
                    ]
            }
        }
        valueTile("humidity", "device.humidity", inactiveLabel: false, width: 2, height: 2) {
            state "humidity", label: '${currentValue}% humidity', unit: ""
        }
        standardTile("refresh", "device.refresh", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
            state "default", action: "refresh.refresh", icon: "st.secondary.refresh"
        }

        main "temperature", "humidity"
        details(["temperature", "humidity", "refresh"])
    }
}

def installed() {
    sendEvent(name: "checkInterval", value: 30 * 60 + 2 * 60, displayed: false, data: [protocol: "lan", hubHardwareId: device.hub.hardwareID])
    sendEvent(name: "temperature", value: 0, unit: "C", displayed: false)
    sendEvent(name: "humidity", value: 0, unit: "%", displayed: false)
}

def parse(String description) {
}

def parseEvents(status, json) {
    if (status as Integer == 200) {
        log.debug "Event: ${json}"
        if (json?.temperature != null) {
            def map = [:]
            def temp = convertTemperatureIfNeeded(json?.temperature?.toFloat(), ((json?.tempUnit == "C") ? "C" : "F"), 1).toFloat()
            map.name = "temperature"
            if (tempOffset) {
                map.value = Math.round((temp + (int) tempOffset) * 100) / 100
            } else {
                map.value = Math.round(temp * 100) / 100
            }
            map.unit = getTemperatureScale()
            sendEvent(map)
        }
        if (json?.humidity != null) {
            sendEvent(name: "humidity", value: ((humidityOffset) ? ((int) json?.humidity + (int) humidityOffset) : json?.humidity), unit: "%")
        }
    }
}

def ping() {
    // Intentionally left blank as parent should handle this
}

def refresh() {
    parent.refresh(device.deviceNetworkId)
}

def configure() {
}

def uninstalled() {
    parent.delete()
}