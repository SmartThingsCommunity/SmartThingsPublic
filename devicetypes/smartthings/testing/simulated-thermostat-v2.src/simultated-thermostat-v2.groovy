/**
 *  Copyright 2019 SmartThings
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
 *  Example thermostat using new UI elements for controlling mode, fan mode, and setpoints. Device configuration page
 *  lets you simulate the dynamic customization of ranges, modes, and fan modes. Note that you have to exit and
 *  re-enter the device detail view for these changes to be picked up
 *
 */
metadata {
    definition (name: "Simulated Thermostat V2", namespace: "smartthings/testing", author: "Bob Florian") {
        capability "Thermostat"
        capability "Temperature Measurement"

        command "tempUp"
        command "tempDown"
    }

    // ST Classic UI definition
    tiles(scale: 2){
        // Main hero tile display the current temperature, operating state, and setpoint. It has not controls
        //
        multiAttributeTile(name:"thermostatMulti", type:"thermostat", width:6, height:4) {
            tileAttribute("device.temperature", key: "PRIMARY_CONTROL") {
                attributeState("temp", label:'${currentValue}°', unit:"°", defaultState: true)
            }
            tileAttribute("device.thermostatOperatingState", key: "OPERATING_STATE") {
                attributeState("idle", backgroundColor: "#FFFFFF")
                attributeState("heating", backgroundColor: "#E86D13")
                attributeState("cooling", backgroundColor: "#00A0DC")
            }
            tileAttribute("device.heatingSetpoint", key: "HEATING_SETPOINT") {
                attributeState("default", label: '${currentValue}', unit: "°F", defaultState: true)
            }
            tileAttribute("device.coolingSetpoint", key: "COOLING_SETPOINT") {
                attributeState("default", label: '${currentValue}', unit: "°F", defaultState: true)
            }
        }

        // Mode tile is now a dropdown. The supportedThermostatModes attribute can be used to remove modes from the list,
        // e.g. to not show auto and cool modes if the device is not connected to an air conditioner
        //
        controlTile("thermostatMode", "device.thermostatMode", "enum", width:2 , height: 1, supportedStates: "device.supportedThermostatModes") {
            state("off", action:"setThermostatMode", label: "Off", icon: "st.thermostat.heating-cooling-off")
            state("cool", action:"setThermostatMode", label: "Cool", icon: "st.thermostat.cool")
            state("heat", action:"setThermostatMode", label: "Heat", icon: "st.thermostat.heat")
            state("auto", action:"setThermostatMode", label:'Auto', icon: "st.tesla.tesla-hvac")
            state("emergency heat", action:"setHeatingSetpoint", label:'Emergency heat', icon: "st.thermostat.emergency-heat")
        }

        // Heating setpoint contol allows the min and max values to be set in the heatingSetpointRange attribute. The
        // debounce period prevents sending a lot of commands if the up and down arrows are used.
        controlTile("heatingSetpoint", "device.heatingSetpoint", "slider",
                sliderType: "HEATING",
                debouncePeriod: 1500,
                range: "device.heatingSetpointRange",
                width: 2, height: 2)
                {
                    state "default", action:"setHeatingSetpoint", label:'${currentValue}', backgroundColor: "#E86D13"
                }

        // Cooling setpoint contol allows the min and max values to be set in the coolingSetpointRange attribute. The
        // debounce period prevents sending a lot of commands if the up and down arrows are used.
        controlTile("coolingSetpoint", "device.coolingSetpoint", "slider",
                sliderType: "COOLING",
                debouncePeriod: 1500,
                range: "device.coolingSetpointRange",
                width: 2, height: 2)
                {
                    state "default", action:"setCoolingSetpoint", label:'${currentValue}', backgroundColor: "#00A0DC"
                }

        // Fan mode tile is now a dropdown. The supportedThermostatFanModes attribute can be used to remove modes from the list,
        // e.g. to not show circulate mode if the device doesn't support it
        //
        controlTile("thermostatFanMode", "device.thermostatFanMode", "enum", width:2 , height: 1, supportedStates: "device.supportedThermostatFanModes") {
            state "auto",      action: "setThermostatFanMode", label: "Auto", icon: "st.thermostat.fan-auto"
            state "on",        action: "setThermostatFanMode", label: "On", icon: "st.thermostat.fan-on"
            state "circulate", action: "setThermostatFanMode", label: "Circulate", icon: "st.thermostat.fan-circulate"
            state "off",      action: "setThermostatFanMode", label: "Off", icon: "st.thermostat.fan-off"
        }

        // These controls exist only to simulate ambient temperature changes
        standardTile("tempDown", "device.temperature", width: 3, height: 1, decoration: "flat") {
            state "default", label: "Decrease", action: "tempDown", icon: "st.thermostat.thermostat-down"
        }
        standardTile("tempUp", "device.temperature", width: 3, height: 1, decoration: "flat") {
            state "default", label: "Increase", action: "tempUp", icon: "st.thermostat.thermostat-up"
        }
    }

    preferences {
        // Customize the controls by changing these. Note that you have to refresh the device details page by exiting
        // from it an re-entering it, or do a pull-to-refresh
        section {
            input "minTemp", "number", title: "Minimum Temperature", defaultValue: "50"
            input "maxTemp", "number", title: "Maximum Temperature", defaultValue: "90"
        }
        section {
            input "modeOff", "boolean", title: "Has Off Mode", defaultValue: "true"
            input "modeAuto", "boolean", title: "Has Auto Mode", defaultValue: "true"
            input "modeHeat", "boolean", title: "Has Heat Mode", defaultValue: "true"
            input "modeCool", "boolean", title: "Has Cool Mode", defaultValue: "true"
            input "modeEmergency", "boolean", title: "Has Emergency Heat Mode", defaultValue: "false"
        }
        section {
            input "fanModeOff", "boolean", title: "Has Off Fan Mode", defaultValue: "false"
            input "fanModeAuto", "boolean", title: "Has Auto Fan Mode", defaultValue: "true"
            input "fanModeOn", "boolean", title: "Has On Fan Mode", defaultValue: "true"
            input "fanModeCirculate", "boolean", title: "Has Recirculate Fan Mode", defaultValue: "false"
        }
    }
}

void installed() {
    setThermostatMode("off")
    setHeatingSetpoint(68)
    setCoolingSetpoint(76)
    sendEvent(name: "temperature", value: "72")
    sendEvent(name: "thermostatOperatingState", value: "off")
    sendEvent(name: "heatingSetpointRange", value: [50, 90].encodeAsJSON())
    sendEvent(name: "coolingSetpointRange", value: [50, 90].encodeAsJSON())
    sendEvent(name: "supportedThermostatModes", value: ["off","heat","cool","auto"].encodeAsJSON())
    sendEvent(name: "supportedThermostatFanModes", value: ["auto","on"].encodeAsJSON())

}

void updated() {
    sendEvent(name: "heatingSetpointRange", value: [(minTemp as Integer) ?: 50, (maxTemp as Integer) ?: 90].encodeAsJSON())
    sendEvent(name: "coolingSetpointRange", value: [(minTemp as Integer) ?: 50, (maxTemp as Integer) ?: 90].encodeAsJSON())
    sendEvent(name: "supportedThermostatModes", value: enabledModes().encodeAsJSON())
    sendEvent(name: "supportedThermostatFanModes", value: enabledFanModes().encodeAsJSON())
}

void setThermostatMode(value) {
    sendEvent(name: "thermostatMode", value: value)
    updateOperatingState()
}

void setThermostatFanMode(value) {
    sendEvent(name: "thermostatFanMode", value: value)
}

void setHeatingSetpoint(value) {
    sendEvent(name: "heatingSetpoint", value: value)
    updateOperatingState()
}

void setCoolingSetpoint(value) {
    sendEvent(name: "coolingSetpoint", value: value)
    updateOperatingState()
}

void off() {
    setThermostatMode("off")
}

void heat() {
    setThermostatMode("heat")

}

void cool() {
    setThermostatMode("cool")
}

void auto() {
    setThermostatMode("auto")
}

void eco() {
    setThermostatMode("eco")
}

void emergencyHeat() {
    setThermostatMode("emergency heat")
}

void tempUp() {
    sendEvent(name: "temperature", value: device.currentValue("temperature") + 1)
    updateOperatingState()
}

void tempDown() {
    sendEvent(name: "temperature", value: device.currentValue("temperature") - 1)
    updateOperatingState()
}

private updateOperatingState() {
    def newOperatingState = device.currentValue("thermostatOperatingState")
    def temperature = device.currentValue("temperature")
    def operatingState = device.currentValue("thermostatOperatingState")
    def heatingSetpoint = device.currentValue("heatingSetpoint")
    def coolingSetpoint = device.currentValue("coolingSetpoint")
    def mode = device.currentValue("thermostatMode")

    if (mode == 'heat') {
        if (temperature < heatingSetpoint) {
            if (operatingState != 'heating') {
                newOperatingState = 'heating';
            }
        }
        else if (operatingState != 'idle') {
            newOperatingState = 'idle';
        }
    }
    else if (mode == 'cool') {
        if (temperature > coolingSetpoint) {
            if (operatingState != 'cooling') {
                newOperatingState = 'cooling';
            }
        }
        else if (operatingState != 'idle') {
            newOperatingState = 'idle';
        }
    }
    else if (mode == 'auto') {
        if (temperature < heatingSetpoint) {
            if (operatingState != 'heating') {
                newOperatingState = 'heating';
            }
        }
        else if (temperature > coolingSetpoint) {
            if (operatingState != 'cooling') {
                newOperatingState = 'cooling';
            }
        }
        else if (operatingState != 'idle') {
            newOperatingState = 'idle';
        }
    }
    else if (mode == 'off') {
        if (operatingState != 'idle') {
            newOperatingState = 'idle';
        }
    }
    log.debug "operatingState = ${newOperatingState}"
    sendEvent(name: "thermostatOperatingState", value: newOperatingState)
}

private enabledModes() {
    def result = []
    if (Boolean.valueOf(modeOff)) {
        result << "off"
    }
    if (Boolean.valueOf(modeAuto)) {
        result << "auto"
    }
    if (Boolean.valueOf(modeHeat)) {
        result << "heat"
    }
    if (Boolean.valueOf(modeCool)) {
        result << "cool"
    }
    if (Boolean.valueOf(modeEmergency)) {
        result << "emergency heat"
    }
    return result
}

private enabledFanModes() {
    def result = []
    if (Boolean.valueOf(fanModeOff)) {
        result << "off"
    }
    if (Boolean.valueOf(fanModeAuto)) {
        result << "auto"
    }
    if (Boolean.valueOf(fanModeOn)) {
        result << "on"
    }
    if (Boolean.valueOf(fanModeCirculate)) {
        result << "circulate"
    }
    return result
}
