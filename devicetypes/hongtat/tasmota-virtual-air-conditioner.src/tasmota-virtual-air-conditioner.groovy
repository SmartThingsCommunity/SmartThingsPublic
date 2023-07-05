/**
 *  Tasmota - Virtual Air Conditioner
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

import groovy.json.JsonOutput
String driverVersion() { return "20201204" }
metadata {
    definition (name: "Tasmota Virtual Air Conditioner", namespace: "hongtat", author: "HongTat Tan", ocfDeviceType: "oic.d.airconditioner", vid: "ff0a15cd-13dd-32c2-b51c-780bb3aad494", mnmn: "SmartThingsCommunity") {
        capability "Actuator"
        capability "Temperature Measurement"
        capability "Thermostat Cooling Setpoint"
        capability "Thermostat Mode"
        capability "voicehouse43588.airConditionerFanSpeed"
        capability "voicehouse43588.airConditionerVerticalSwing"
        capability "voicehouse43588.airConditionerHorizontalSwing"
        capability "Switch"

        capability "Health Check"
        capability "Configuration"
        capability "Refresh"
        capability "Sensor"

        attribute "lastSeen", "string"
    }

    preferences {
        section {
            input(title: "Device Settings",
                    description: "To view/update this settings, go to the Tasmota (Connect) SmartApp and select this device.",
                    displayDuringSetup: false,
                    type: "paragraph",
                    element: "paragraph")
            input(title: "", description: "Tasmota Virtual Air Conditioner v${driverVersion()}", displayDuringSetup: false, type: "paragraph", element: "paragraph")
        }
    }

    tiles(scale: 2) {
        multiAttributeTile(name:"thermostat", type:"general", width:6, height:4, canChangeIcon: false)  {
            tileAttribute("device.switch", key: "PRIMARY_CONTROL") {
                attributeState("off", label: "Off", action: "switch.on", icon: "st.thermostat.heating-cooling-off", backgroundColor: "#ffffff")
                attributeState("on", label: "On", action: "switch.off", backgroundColor: "#00a0dc")
            }
            tileAttribute("device.temperature", key: "SECONDARY_CONTROL") {
                attributeState("temperature", label:'${currentValue}°', icon: "st.alarm.temperature.normal")
            }
            tileAttribute("device.coolingSetpoint", key: "COOLING_SETPOINT") {
                attributeState("default", label: '${currentValue}', unit: "°", defaultState: true)
            }
        }
        controlTile("coolingSetpoint", "device.coolingSetpoint", "slider",
                sliderType: "COOLING",
                debouncePeriod: 750,
                range: "device.coolingSetpointRange",
                width: 2, height: 2) {
            state "default", action:"setCoolingSetpoint", label:'${currentValue}', backgroundColor: "#55D4ED"
        }
        controlTile("thermostatMode", "device.thermostatMode", "enum", width: 2 , height: 2, supportedStates: "device.supportedThermostatModes") {
            state("off", action: "setThermostatMode", label: 'Off', icon: "st.thermostat.heating-cooling-off")
            state("cool", action: "setThermostatMode", label: 'Cool', icon: "st.thermostat.cool")
            state("heat", action: "setThermostatMode", label: 'Heat', icon: "st.thermostat.heat")
            state("auto", action: "setThermostatMode", label: 'Auto', icon: "st.tesla.tesla-hvac")
        }
        controlTile("fanSpeed", "device.fanSpeed", "enum", width: 2 , height: 2, supportedStates: "device.supportedFanSpeed") {
            state("auto", action: "setFanSpeed", label: 'Auto', icon: "st.thermostat.fan-on")
            state("1", action: "setFanSpeed", label: 'Min', icon: "st.thermostat.fan-on")
            state("2", action: "setFanSpeed", label: 'Low', icon: "st.thermostat.fan-on")
            state("3", action: "setFanSpeed", label: 'Medium', icon: "st.thermostat.fan-on")
            state("4", action: "setFanSpeed", label: 'High', icon: "st.thermostat.fan-on")
            state("5", action: "setFanSpeed", label: 'Max', icon: "st.thermostat.fan-on")
        }
        controlTile("swingV", "device.swingV", "enum", width: 3 , height: 1, supportedStates: "device.supportedAirConditionerSwingV") {
            state("auto", action: "setSwingV", label: 'Auto')
            state("off", action: "setSwingV", label: 'Off')
            state("min", action: "setSwingV", label: 'Min')
            state("low", action: "setSwingV", label: 'Low')
            state("mid", action: "setSwingV", label: 'Mid')
            state("high", action: "setSwingV", label: 'High')
            state("max", action: "setSwingV", label: 'Max')
        }
        valueTile("spacerV", "spacer", decoration: "flat", inactiveLabel: false, width: 3, height: 1) {
            state "default", label:'Swing-V'
        }
        controlTile("swingH", "device.swingH", "enum", width: 3 , height: 1, supportedStates: "device.supportedAirConditionerSwingH") {
            state("auto", action: "setSwingH", label: 'Auto')
            state("off", action: "setSwingH", label: 'Off')
            state("left max", action: "setSwingH", label: 'Left Max')
            state("left", action: "setSwingH", label: 'Left')
            state("mid", action: "setSwingH", label: 'Mid')
            state("right", action: "setSwingH", label: 'Right')
            state("right max", action: "setSwingH", label: 'Right Max')
            state("wide", action: "setSwingH", label: 'Wide')
        }
        valueTile("spacerH", "spacer", decoration: "flat", inactiveLabel: false, width: 3, height: 1) {
            state "default", label: 'Swing-H'
        }
        main "thermostat"
        details(["thermostat", "thermostatMode", "fanSpeed", "coolingSetpoint", "swingV", "swingH", "spacerV", "spacerH"])
    }
}

def parse(description) {
}

def parseEvents(status, json) {
    def events = []
    if (status as Integer == 200) {
        // Contact sensor that senses if the AC is On or Off
        if (json?.contactSensor) {
            String cs = json?.contactSensor
            Map modeMap = [name: "thermostatMode", data:[supportedThermostatModes: supportedThermostatModes.encodeAsJson()]]
            Map switchMap = [name: "switch"]
            if (cs in ["open", "closed"]) {
                if (cs == "open") {
                    switchMap.value = "on"
                    if (state?.lastMode) {
                        modeMap.value = state.lastMode
                    } else {
                        modeMap.value = "cool"
                    }
                    modeMap.displayed = true
                } else {
                    switchMap.value = "off"
                    modeMap.value = "off"
                    modeMap.displayed = true
                }
                events << sendEvent(switchMap)
                events << sendEvent(modeMap)
            }
        }
        // Bridge's Last seen
        if (json?.lastSeen) {
            events << sendEvent(name: "switch", value: device.currentValue("switch"))
            events << sendEvent(name: "lastSeen", value: json?.lastSeen, displayed: false)
        }
    }
    return events
}

def ping() {
    // Intentionally left blank as parent should handle this
}

def installed() {
    state?.lastMode = "cool"
    sendEvent(name: "checkInterval", value: 30 * 60 + 2 * 60, displayed: false, data: [protocol: "lan", hubHardwareId: device.hub.hardwareID])
    sendEvent(name: "supportedThermostatModes", value: JsonOutput.toJson(supportedThermostatModes), displayed: false)
    sendEvent(name: "supportedFanSpeed", value: JsonOutput.toJson(supportedFanSpeed), displayed: false)
    sendEvent(name: "supportedAirConditionerSwingV", value: JsonOutput.toJson(supportedAirConditionerSwingV), displayed: false)
    sendEvent(name: "supportedAirConditionerSwingH", value: JsonOutput.toJson(supportedAirConditionerSwingH), displayed: false)
    sendEvent(name: "coolingSetpointRange", value: thermostatSetpointRange, displayed: false)
    sendEvent(name: "switch", value: "off", displayed: false)
    sendEvent(name: "fanSpeed", value: supportedFanSpeed[0], displayed: false)
    sendEvent(name: "coolingSetpoint", value: thermostatSetpointRange[0], unit: getTemperatureScale(), displayed: false)
    sendEvent(name: "temperature", value: thermostatSetpointRange[0], unit: getTemperatureScale(), displayed: false)
    sendEvent(name: "thermostatMode", value: "off", data:[supportedThermostatModes: JsonOutput.toJson(supportedThermostatModes)], displayed: false)
    sendEvent(name: "swingV", value: "auto", displayed: false)
    sendEvent(name: "swingH", value: "auto", displayed: false)
}

def updated() {
    initialize()
}

def initialize() {
}

def setFanSpeed(String speed) {
    def fanSpeed = [name: "fanSpeed"]
    if (supportedFanSpeed.contains(speed as String)) {
        fanSpeed.value = speed as String
        sendEvent(fanSpeed)
        done()
    } else {
        log.debug "Unsupported fan speed: '${speed}'"
    }
}

def on() {
    if (state?.lastMode) {
        setThermostatMode(state?.lastMode)
    } else {
        setThermostatMode("cool")
    }
}

def off() {
    setThermostatMode("off")
}

def cool() {
    setThermostatMode("cool")
}

def heat() {
    setThermostatMode("heat")
}

def auto() {
    setThermostatMode("auto")
}

def setThermostatMode(String mode) {
    def modeMap = [name: "thermostatMode", data:[supportedThermostatModes: supportedThermostatModes.encodeAsJson()]]
    def switchMap = [name: "switch"]

    if (supportedThermostatModes.contains(mode)) {
        switch (mode) {
            case "off":
                modeMap.value = "off"
                modeMap.displayed = true
                switchMap.value = "off"
                break
            default:
                modeMap.value = mode
                modeMap.displayed = true
                switchMap.value = "on"
                state?.lastMode = mode
                break
        }
        sendEvent(modeMap)
        sendEvent(switchMap)
        done()
    } else {
        log.debug "Unsupported AC mode: '${mode}'"
    }
}

def setCoolingSetpoint(setpoint) {
    if (setpoint < thermostatSetpointRange[0]) {
        setpoint = thermostatSetpointRange[0]
    } else if (setpoint > thermostatSetpointRange[1]) {
        setpoint = thermostatSetpointRange[1]
    }
    sendEvent(name: "coolingSetpoint", value: setpoint, unit: getTemperatureScale(), displayed: false)
    sendEvent(name: "temperature", value: setpoint, unit: getTemperatureScale())
    done()
}

def setSwingV(verticalSwing) {
    if (supportedAirConditionerSwingV.contains(verticalSwing)) {
        sendEvent(name: "swingV", value: verticalSwing)
        done()
    } else {
        log.debug "Unsupported vertical swing mode: '${verticalSwing}'"
    }
}

def setSwingH(horizontalSwing) {
    if (supportedAirConditionerSwingH.contains(horizontalSwing)) {
        sendEvent(name: "swingH", value: horizontalSwing)
        done()
    } else {
        log.debug "Unsupported horizontal swing mode: '${horizontalSwing}'"
    }
}

def done() {
    def bridge = parent.childSetting(device.id, "bridge") ?: null
    def vendor = parent.childSetting(device.id, "hvac") ?: null
    if (bridge && vendor) {
        def command = [:]
        def power = device.currentState("switch").value
        command.Vendor = vendor
        command.Power = power
        command.Mode = (power == "off") ? state.lastMode : device.currentState("thermostatMode").value
        command.FanSpeed = device.currentState("fanSpeed").value
        command.Temp = device.currentState("coolingSetpoint").value
        command.SwingV = device.currentState("swingV").value
        command.SwingH = device.currentState("swingH").value
        if (getTemperatureScale() == "F") {
            command.Celsius = 'Off'
        }
        if (power == "off") {
            log.debug "Command 1: " + JsonOutput.toJson(command)
            parent.callTasmota(bridge, 'IRhvac ' + JsonOutput.toJson(command))

            command.Mode = "off"
            log.debug "Command 2: " + JsonOutput.toJson(command)
            parent.callTasmota(bridge, 'IRhvac ' + JsonOutput.toJson(command))
        } else {
            log.debug "Command: " + JsonOutput.toJson(command)
            parent.callTasmota(bridge, 'IRhvac ' + JsonOutput.toJson(command))
        }
    } else {
        log.debug "Error: Please specify an IR bridge and air conditioner brand"
    }
}

def getThermostatSetpointRange() {
    (getTemperatureScale() == "C") ? [16, 31] : [60, 87]
}

def getSupportedThermostatModes() {
    ["off", "cool", "heat", "auto"]
}

def getSupportedFanSpeed() {
    ["auto", "1", "2", "3", "4", "5"]
}

def getSupportedAirConditionerSwingV() {
    ["auto", "off", "min", "low", "mid", "high", "max"]
}

def getSupportedAirConditionerSwingH() {
    ["auto", "off", "left max", "left", "mid", "right", "right max", "wide"]
}