/**
 *	Nexia Thermostat
 *
 *	Author: Trent Foley
 *	Date: 2016-01-25
 */
metadata {
    definition (name: "Nexia Thermostat", namespace: "trentfoley", author: "Trent Foley") {
        capability "Actuator"
        capability "Temperature Measurement"
        capability "Thermostat"
        capability "Relative Humidity Measurement"
        capability "Polling"
        capability "Sensor"
        capability "Refresh"
        command "setTemperature"

        attribute "activeMode", "string"
        attribute "outdoorTemperature", "number"
    }

    simulator { }

    tiles(scale: 2) {
        valueTile("temperature", "device.temperature", canChangeIcon: true, icon: "st.Home.home1") {
            state("temperature", label:'${currentValue}°', unit:"F",
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
        multiAttributeTile(name: "thermostatMulti", type: "thermostat", width:6, height:4) {
            tileAttribute("device.temperature", key: "PRIMARY_CONTROL") {
                attributeState("default", label:'${currentValue}°', unit:"F",  defaultState: true)
            }
            tileAttribute("device.temperature", key: "VALUE_CONTROL") {
                attributeState("default", action: "setTemperature", label: '')
            }
            tileAttribute("device.humidity", key: "SECONDARY_CONTROL") {
                attributeState("default", label:'${currentValue}%', unit: "%", defaultState: true)
            }
            tileAttribute("device.thermostatOperatingState", key: "OPERATING_STATE") {
                attributeState("idle", backgroundColor: "#44b621")
                attributeState("fan only", backgroundColor: "#44b621")
                attributeState("heating", backgroundColor:"#ffa81e")
                attributeState("pending heat", backgroundColor:"#ffa81e")
                attributeState("cooling", backgroundColor:"#269bd2")
                attributeState("pending cool", backgroundColor:"#269bd2")
            }
            tileAttribute("device.thermostatMode", key: "THERMOSTAT_MODE") {
                attributeState("off", label:'${name}')
                attributeState("heat", label:'${name}')
                attributeState("cool", label:'${name}')
                attributeState("auto", label:'${name}')
            }
            tileAttribute("device.heatingSetpoint", key: "HEATING_SETPOINT") {
                attributeState("default", label:'${currentValue}', unit:"F", defaultState: true)
            }
            tileAttribute("device.coolingSetpoint", key: "COOLING_SETPOINT") {
                attributeState("default", label:'${currentValue}', unit:"F", defaultState: true)
            }
        }
        standardTile("setPointLabel", "device.thermostatSetpoint", height: 1, width: 1, decoration: "flat") {
            state "default", label: "Setpoint:"
        }
        valueTile("setPoint", "device.thermostatSetpoint", height: 1, width: 1, decoration: "flat") {
            state "default", label: '${currentValue}F'
        }
        standardTile("mode", "device.thermostatMode", height: 1, width: 1, decoration: "flat") {
            state "default", label: "Tstat Mode:"
        }
        standardTile("modeOff", "device.thermostatMode", height: 1, width: 1, decoration: "flat") {
            state "default", icon: "st.thermostat.heating-cooling-off", action: "off"
            state "off", icon: "st.thermostat.heating-cooling-off", backgroundColor: "#efefef"
        }
        standardTile("modeAuto", "device.thermostatMode", height: 1, width: 1, decoration: "flat") {
            state "default", icon: "st.thermostat.auto", action: "auto"
            state "auto", icon: "st.thermostat.auto", backgroundColor: "#44b621"
        }
        standardTile("modeCool", "device.thermostatMode", height: 1, width: 1, decoration: "flat") {
            state "default", icon: "st.thermostat.cool", action: "cool"
            state "cool", icon: "st.thermostat.cool", backgroundColor: "#269bd2"
        }
        standardTile("modeHeat", "device.thermostatMode", height: 1, width: 1, decoration: "flat") {
            state "default", icon: "st.thermostat.heat", action: "heat"
            state "heat", icon: "st.thermostat.heat", backgroundColor: "#ffa81e"
        }
        standardTile("modeEmergencyHeat", "device.thermostatMode", height: 1, width: 1, decoration: "flat") {
            state "default", icon: "st.thermostat.emergency-heat", action: "emergencyHeat"
            state "emergency heat", icon: "st.thermostat.emergency-heat", backgroundColor: "#ffa81e"
        }
        standardTile("fanMode", "device.thermostatFanMode", height: 1, width: 1, decoration: "flat") {
            state "default", label: "Fan Mode:"
        }
        standardTile("fanModeAuto", "device.thermostatFanMode", height: 1, width: 1, decoration: "flat") {
            state "default", icon: "st.thermostat.fan-auto", action: "fanAuto"
            state "auto", icon: "st.thermostat.fan-auto", backgroundColor: "#44b621"
        }
        standardTile("fanModeOn", "device.thermostatFanMode", height: 1, width: 1, decoration: "flat") {
            state "default", icon: "st.thermostat.fan-on", action: "fanOn"
            state "on", icon: "st.thermostat.fan-on", backgroundColor: "#44b621"
        }
        standardTile("fanModeCirculate", "device.thermostatFanMode", height: 1, width: 1, decoration: "flat") {
            state "default", icon: "st.thermostat.fan-circulate", action: "fanCirculate"
            state "circulate", icon: "st.thermostat.fan-circulate", backgroundColor: "#44b621"
        }
        valueTile("outdoorTemperature", "device.outdoorTemperature", height: 3, width: 3) {
            state("outdoorTemperature", label:'${currentValue}°', unit:"F", icon: "st.Outdoor.outdoor2",
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
        standardTile("refresh", "device.thermostatMode", height: 3, width: 3, decoration: "flat") {
            state "default", action:"refresh.refresh", icon:"st.secondary.refresh"
        }
        standardTile("blank", "device.thermostatMode", height: 1, width: 1, decoration: "flat")

        main "temperature"
        details(["thermostatMulti",  
                "mode", "modeOff", "modeAuto", "modeCool", "modeHeat", "modeEmergencyHeat", 
                "setPointLabel", "setPoint", "fanMode", "fanModeAuto", "fanModeOn", "fanModeCirculate",
                "outdoorTemperature", "refresh"])
    }
}

// parse events into attributes
def parse(String description) {
    log.debug "parse('${description}')"
}

// Implementation of capability.refresh
def refresh() {
    log.debug "refresh()"
    poll()
}

// Implementation of capability.polling
def poll() {
    log.debug "poll()"
    def data = parent.pollChild(this)

    if(data) {
        sendEvent(name: "temperature", value: data.temperature, unit: "F")
            sendEvent(name: "heatingSetpoint", value: data.heatingSetpoint, unit: "F")
            sendEvent(name: "coolingSetpoint", value: data.coolingSetpoint, unit: "F")
            sendEvent(name: "thermostatSetpoint", value: data.thermostatSetpoint, unit: "F")
            sendEvent(name: "thermostatMode", value: data.thermostatMode)
            sendEvent(name: "thermostatFanMode", value: data.thermostatFanMode)
            sendEvent(name: "thermostatOperatingState", value: data.thermostatOperatingState)
            sendEvent(name: "humidity", value: data.humidity, unit: "%")
            sendEvent(name: "activeMode", value: data.activeMode)
            sendEvent(name: "outdoorTemperature", value: data.outdoorTemperature, unit: "F")
    } else {
        log.error "ERROR: Device connection removed? No data found for ${device.deviceNetworkId} after polling"
    }
}

def setTemperature(degreesF) {
    log.debug "setTemperature(${degreesF})"
    def delta = degreesF - device.currentValue("temperature")
    log.debug "Determined delta to be ${delta}"

    if (device.currentValue("activeMode") == "cool") {
        setCoolingSetpoint(device.currentValue("coolingSetpoint") + delta)
    } else {
        setHeatingSetpoint(device.currentValue("heatingSetpoint") + delta)
    }
}

// Implementation of capability.thermostat
def setHeatingSetpoint(degreesF) {
    log.debug "setHeatingSetpoint(${degreesF})"
    sendEvent(name: "heatingSetpoint", value: degreesF, unit: "F")
    sendEvent(name: "thermostatSetpoint", value: degreesF, unit: "F")
    parent.setHeatingSetpoint(this, degreesF)
}

// Implementation of capability.thermostat
def setCoolingSetpoint(degreesF) {
    log.debug "setCoolingSetpoint(${degreesF})"
    sendEvent(name: "coolingSetpoint", value: degreesF, unit: "F")
    sendEvent(name: "thermostatSetpoint", value: degreesF, unit: "F")
    parent.setCoolingSetpoint(this, degreesF)
}

// Implementation of capability.thermostat
// Valid values are: "auto" "emergency heat" "heat" "off" "cool"
def setThermostatMode(String mode) {
    log.debug "setThermostatMode(${mode})"
    sendEvent(name: "thermostatMode", value: mode)
    parent.setThermostatMode(this, mode)
}

// Implementation of capability.thermostat
def off() { setThermostatMode("off") }

// Implementation of capability.thermostat
def heat() { setThermostatMode("heat") }

// Implementation of capability.thermostat
def emergencyHeat() { setThermostatMode("emergency heat") }

// Implementation of capability.thermostat
def cool() { setThermostatMode("cool") }

// Implementation of capability.thermostat
def auto() { setThermostatMode("auto") }

// Implementation of capability.thermostat
// Valid values are: "auto" "on" "circulate"
def setThermostatFanMode(String fanMode) {
    log.debug "setThermostatFanMode(${fanMode})"
    sendEvent(name: "thermostatFanMode", value: fanMode)
    parent.setThermostatFanMode(this, fanMode)
}

// Implementation of capability.thermostat
def fanOn() { setThermostatFanMode("on") }

// Implementation of capability.thermostat
def fanAuto() { setThermostatFanMode("auto") }

// Implementation of capability.thermostat
def fanCirculate() { setThermostatFanMode("circulate") }
