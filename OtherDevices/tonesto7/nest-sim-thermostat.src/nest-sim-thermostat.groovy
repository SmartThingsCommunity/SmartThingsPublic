/**
 *  Copyright 2015 SmartThings
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

def devVer() { return "1.0.2"}

metadata {
    definition (name: "Nest Sim Thermostat", namespace: "tonesto7", author: "Anthony S.") {
        capability "Thermostat"
        capability "Relative Humidity Measurement"

        command "tempUp"
        command "tempDown"
        command "heatUp"
        command "heatDown"
        command "coolUp"
        command "coolDown"
        command "humidityUp"
        command "humidityDown"
        command "setTemperature", ["number"]
        command "changePresence"
        command "safetyHumidityMaxUp"
        command "safetyHumidityMaxDown"
        command "comfortDewpointMaxUp"
        command "comfortDewpointMaxDown"
        command "changeOperState"
        command "setHeating"
        command "setCooling"
        command "setFanOnly"
        command "setIdle"

        command "safetyTempMinUp"
        command "safetyTempMinDown"
        command "safetyTempMaxUp"
        command "safetyTempMaxDown"
        command "lockedTempMinUp"
        command "lockedTempMinDown"
        command "lockedTempMaxUp"
        command "lockedTempMaxDown"
        command "changeTempLock"

        attribute "presence", "string"
        attribute "nestPresence", "string"
        attribute "safetyTempMin", "string"
        attribute "safetyTempMax", "string"
        attribute "safetyHumidityMax", "string"
        //attribute "safetyHumidityMin", "string"
        attribute "comfortHumidityMax", "string"

        attribute "tempLockOn", "string"
        attribute "lockedTempMin", "string"
        attribute "lockedTempMax", "string"
    }

    tiles(scale: 2) {
        multiAttributeTile(name:"thermostatMulti", type:"thermostat", width:6, height:4) {
            tileAttribute("device.temperature", key: "PRIMARY_CONTROL") {
                attributeState("default", label:'${currentValue}', unit:"dF")
            }
            tileAttribute("device.temperature", key: "VALUE_CONTROL") {
                attributeState("VALUE_UP", action: "tempUp")
                attributeState("VALUE_DOWN", action: "tempDown")
            }
            tileAttribute("device.humidity", key: "SECONDARY_CONTROL") {
                attributeState("default", label:'${currentValue}%', unit:"%")
            }
            tileAttribute("device.thermostatOperatingState", key: "OPERATING_STATE") {
                attributeState("idle", backgroundColor:"#44b621")
                attributeState("heating", backgroundColor:"#ea5462")
                attributeState("cooling", backgroundColor:"#269bd2")
            }
            tileAttribute("device.thermostatMode", key: "THERMOSTAT_MODE") {
                attributeState("off", label:'${name}')
                attributeState("heat", label:'${name}')
                attributeState("cool", label:'${name}')
                attributeState("auto", label:'${name}')
            }
            tileAttribute("device.heatingSetpoint", key: "HEATING_SETPOINT") {
                attributeState("default", label:'${currentValue}', unit:"dF")
            }
            tileAttribute("device.coolingSetpoint", key: "COOLING_SETPOINT") {
                attributeState("default", label:'${currentValue}', unit:"dF")
            }
        }

        valueTile("temperature", "device.temperature", width: 2, height: 2) {
            state("temperature", label:'Ambient:\n${currentValue}', unit:"dF",
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
        standardTile("tempDown", "device.temperature", width: 2, height: 2, decoration: "flat") {
            state "default", action:"tempDown", icon:"https://raw.githubusercontent.com/tonesto7/nest-manager/master/Images/Devices/cool_arrow_down.png"
        }
        standardTile("tempUp", "device.temperature", width: 2, height: 2, decoration: "flat") {
            state "default", action:"tempUp", icon:"https://raw.githubusercontent.com/tonesto7/nest-manager/master/Images/Devices/heat_arrow_up.png"
        }

        valueTile("heatingSetpoint", "device.heatingSetpoint", width: 2, height: 2, decoration: "flat") {
            state "heat", label:'${currentValue}\nHeat Setpoint', unit: "F", backgroundColor:"#ffffff"
        }
        standardTile("heatDown", "device.temperature", width: 2, height: 2, decoration: "flat") {
            state "default", action:"heatDown", icon:"https://raw.githubusercontent.com/tonesto7/nest-manager/master/Images/Devices/heat_arrow_down.png"
        }
        standardTile("heatUp", "device.temperature", width: 2, height: 2, decoration: "flat") {
            state "default", action:"heatUp", icon:"https://raw.githubusercontent.com/tonesto7/nest-manager/master/Images/Devices/heat_arrow_up.png"
        }

        valueTile("coolingSetpoint", "device.coolingSetpoint", width: 2, height: 2, decoration: "flat") {
            state "cool", label:'${currentValue}\nCool Setpoint', unit:"F", backgroundColor:"#ffffff"
        }
        standardTile("coolDown", "device.temperature", width: 2, height: 2, decoration: "flat") {
            state "default", action:"coolDown", icon:"https://raw.githubusercontent.com/tonesto7/nest-manager/master/Images/Devices/cool_arrow_down.png"
        }
        standardTile("coolUp", "device.temperature", width: 2, height: 2, decoration: "flat") {
            state "default", action:"coolUp", icon:"https://raw.githubusercontent.com/tonesto7/nest-manager/master/Images/Devices/cool_arrow_up.png"
        }

        valueTile("humidity", "device.humidity", width: 2, height: 2, decoration: "flat") {
            state "default", label:'Humidity\n${currentValue}', unit: "%", backgroundColor:"#ffffff"
        }
        standardTile("humidityDown", "device.humidity", width: 2, height: 2, decoration: "flat") {
            state "default", action:"humidityDown", icon:"https://raw.githubusercontent.com/tonesto7/nest-manager/master/Images/Devices/cool_arrow_down.png"
        }
        standardTile("humidityUp", "device.humidity", width: 2, height: 2, decoration: "flat") {
            state "default", action:"humidityUp", icon:"https://raw.githubusercontent.com/tonesto7/nest-manager/master/Images/Devices/cool_arrow_up.png"
        }

        standardTile("mode", "device.thermostatMode", width: 2, height: 2, decoration: "flat") {
            state "off", action:"thermostat.heat", icon: "https://raw.githubusercontent.com/tonesto7/nest-manager/master/Images/Devices/off_btn_icon.png"
            state "heat", action:"thermostat.cool", icon: "https://raw.githubusercontent.com/tonesto7/nest-manager/master/Images/Devices/heat_btn_icon.png"
            state "cool", action:"thermostat.auto", icon: "https://raw.githubusercontent.com/tonesto7/nest-manager/master/Images/Devices/cool_btn_icon.png"
            state "auto", action:"thermostat.off", icon: "https://raw.githubusercontent.com/tonesto7/nest-manager/master/Images/Devices/heat_cool_btn_icon.png"
        }

        standardTile("fanMode", "device.thermostatFanMode", width:2, height:2, decoration: "flat") {
            state "auto",	action:"fanOn", icon: "https://raw.githubusercontent.com/tonesto7/nest-manager/master/Images/Devices/fan_auto_icon.png"
            state "on",		action:"fanAuto", icon: "https://raw.githubusercontent.com/tonesto7/nest-manager/master/Images/Devices/fan_on_icon.png"
            state "circulate",	action:"fanAuto", icon: "https://raw.githubusercontent.com/tonesto7/nest-manager/master/Images/Devices/fan_on_icon.png"
            state "disabled", icon: "https://raw.githubusercontent.com/tonesto7/nest-manager/master/Images/Devices/fan_disabled_icon.png"
        }

        standardTile("operatingState", "device.thermostatOperatingState", width: 2, height: 2, decoration: "flat") {
            state "idle", label:'${name}', action: "setHeating", backgroundColor:"#ffffff"
            state "heating", label:'${name}', action: "setCooling", backgroundColor:"#ffa81e"
            state "cooling", label:'${name}', action: "setFanOnly", backgroundColor:"#269bd2"
            state "fan only", label:'${name}', action: "setIdle", backgroundColor:"#269bd2"
        }

        standardTile("nestPresence", "device.nestPresence", width:2, height:2, decoration: "flat") {
            state "home", 	    action: "changePresence",	icon: "https://raw.githubusercontent.com/tonesto7/nest-manager/master/Images/Devices/pres_home_icon.png"
            state "away", 		action: "changePresence", 	icon: "https://raw.githubusercontent.com/tonesto7/nest-manager/master/Images/Devices/pres_away_icon.png"
            state "auto-away", 	action: "changePresence", 	icon: "https://raw.githubusercontent.com/tonesto7/nest-manager/master/Images/Devices/pres_autoaway_icon.png"
            state "unknown",	action: "changePresence", 	icon: "st.unknown.unknown.unknown"
        }

        valueTile("safetyTempMin", "device.safetyTempMin", width: 2, height: 2, decoration: "flat") {
            state "default", label:'Safety Temp Min\n${currentValue}', unit: "F", backgroundColor:"#ffffff"
        }
        standardTile("safetyTempMinDown", "device.safetyTempMin", width: 2, height: 2, decoration: "flat") {
            state "default", action:"safetyTempMinDown", icon:"https://raw.githubusercontent.com/tonesto7/nest-manager/master/Images/Devices/cool_arrow_down.png"
        }
        standardTile("safetyTempMinUp", "device.safetyTempMin", width: 2, height: 2, decoration: "flat") {
            state "default", action:"safetyTempMinUp", icon:"https://raw.githubusercontent.com/tonesto7/nest-manager/master/Images/Devices/cool_arrow_up.png"
        }

        valueTile("safetyTempMax", "device.safetyTempMax", width: 2, height: 2, decoration: "flat") {
            state "default", label:'Safety Temp Max\n${currentValue}', unit: "F", backgroundColor:"#ffffff"
        }

        standardTile("safetyTempMaxDown", "device.safetyTempMax", width: 2, height: 2, decoration: "flat") {
            state "default", action:"safetyTempMaxDown", icon:"https://raw.githubusercontent.com/tonesto7/nest-manager/master/Images/Devices/heat_arrow_down.png"
        }
        standardTile("safetyTempMaxUp", "device.safetyTempMax", width: 2, height: 2, decoration: "flat") {
            state "default", action:"safetyTempMaxUp", icon:"https://raw.githubusercontent.com/tonesto7/nest-manager/master/Images/Devices/heat_arrow_up.png"
        }

        valueTile("safetyHumidityMax", "device.safetyHumidityMax", width: 2, height: 2, decoration: "flat") {
            state "default", label:'Hum Max\n${currentValue}', unit: "%", backgroundColor:"#ffffff"
        }
        standardTile("safetyHumidityMaxDown", "device.safetyHumidityMax", width: 2, height: 2, decoration: "flat") {
            state "default", action:"safetyTempMinDown", icon:"https://raw.githubusercontent.com/tonesto7/nest-manager/master/Images/Devices/cool_arrow_down.png"
        }
        standardTile("safetyHumidityMaxUp", "device.safetyHumidityMax", width: 2, height: 2, decoration: "flat") {
            state "default", action:"safetyHumidityMaxUp", icon:"https://raw.githubusercontent.com/tonesto7/nest-manager/master/Images/Devices/cool_arrow_up.png"
        }

        valueTile("comfortDewpointMax", "device.comfortDewpointMax", width: 2, height: 2, decoration: "flat") {
            state "default", label:'Dewpoint Max\n${currentValue}', unit: "%", backgroundColor:"#ffffff"
        }
        standardTile("comfortDewpointMaxDown", "device.safetyHumidityMax", width: 2, height: 2, decoration: "flat") {
            state "default", action:"comfortDewpointMaxDown", icon:"https://raw.githubusercontent.com/tonesto7/nest-manager/master/Images/Devices/cool_arrow_down.png"
        }
        standardTile("comfortDewpointMaxUp", "device.safetyHumidityMax", width: 2, height: 2, decoration: "flat") {
            state "default", action:"comfortDewpointMaxUp", icon:"https://raw.githubusercontent.com/tonesto7/nest-manager/master/Images/Devices/cool_arrow_up.png"
        }

        standardTile("tempLocked", "device.tempLockOn", width: 2, height: 2, decoration: "flat") {
            state "true", action:"changeTempLock", icon: "https://raw.githubusercontent.com/tonesto7/nest-manager/master/Images/App/lock_icon.png"
            state "false", action:"changeTempLock", icon: "https://raw.githubusercontent.com/tonesto7/nest-manager/master/Images/App/unlock_icon.png"
        }

        valueTile("lockedTempMin", "device.lockedTempMin", width: 2, height: 2, decoration: "flat") {
            state "default", label:'Temp Lock Min\n${currentValue}', unit: "F", backgroundColor:"#ffffff"
        }
        standardTile("lockedTempMinDown", "device.lockedTempMin", width: 2, height: 2, decoration: "flat") {
            state "default", action:"lockedTempMinDown", icon:"https://raw.githubusercontent.com/tonesto7/nest-manager/master/Images/Devices/cool_arrow_down.png"
        }
        standardTile("lockedTempMinUp", "device.lockedTempMin", width: 2, height: 2, decoration: "flat") {
            state "default", action:"lockedTempMinUp", icon:"https://raw.githubusercontent.com/tonesto7/nest-manager/master/Images/Devices/cool_arrow_up.png"
        }

        valueTile("lockedTempMax", "device.lockedTempMax", width: 2, height: 2, decoration: "flat") {
            state "default", label:'Temp Lock Max\n${currentValue}', unit: "F", backgroundColor:"#ffffff"
        }

        standardTile("lockedTempMaxDown", "device.lockedTempMax", width: 2, height: 2, decoration: "flat") {
            state "default", action:"lockedTempMaxDown", icon:"https://raw.githubusercontent.com/tonesto7/nest-manager/master/Images/Devices/heat_arrow_down.png"
        }
        standardTile("lockedTempMaxUp", "device.lockedTempMax", width: 2, height: 2, decoration: "flat") {
            state "default", action:"lockedTempMaxUp", icon:"https://raw.githubusercontent.com/tonesto7/nest-manager/master/Images/Devices/heat_arrow_up.png"
        }
        valueTile("filler", "device.filler", width: 2, height: 2, decoration: "flat") {
            state "default", label: ''
        }

        main("thermostatMulti")
        details([
            "thermostatMulti", "temperature","tempDown","tempUp",
            "mode", "fanMode", "operatingState",
            "heatingSetpoint", "heatDown", "heatUp",
            "coolingSetpoint", "coolDown", "coolUp",
            "humidity", "humidityDown", "humidityUp",
            "nestPresence", "filler", "filler",
            "safetyTempMin", "safetyTempMinDown", "safetyTempMinUp",
            "safetyTempMax", "safetyTempMaxDown", "safetyTempMaxUp",
            "safetyHumidityMax", "safetyHumidityMaxDown", "safetyHumidityMaxUp",
            "comfortDewpointMax", "comfortDewpointMaxDown", "comfortDewpointMaxUp",
            "tempLocked", "filler", "filler",
            "lockedTempMin", "lockedTempMinDown", "lockedTempMinUp",
            "lockedTempMax", "lockedTempMaxDown", "lockedTempMaxUp"

        ])
    }
}

def installed() {
    sendEvent(name: "temperature", value: 72, unit: getTemperatureScale(), descriptionText: "Temperature is: (72°${getTemperatureScale()})", displayed: false, isStateChange: true)
    sendEvent(name: "heatingSetpoint", value: 70, unit: getTemperatureScale(), descriptionText: "Heating Setpoint is: (70°${getTemperatureScale()})", displayed: false, isStateChange: true)
    sendEvent(name: "thermostatSetpoint", value: 70, unit: getTemperatureScale(), descriptionText: "Thermostat Setpoint is: (70°${getTemperatureScale()})", displayed: false, isStateChange: true)
    sendEvent(name: "coolingSetpoint", value: 76, unit: getTemperatureScale(), descriptionText: "Cooling Setpoint is: (76°${getTemperatureScale()})", displayed: false, isStateChange: true)
    sendEvent(name: "thermostatMode", value: "off", descriptionText: "Thermostat Mode is: (Off)", displayed: false, isStateChange: true)
    sendEvent(name: "thermostatFanMode", value: "auto", descriptionText: "Thermostat Fan Mode is: (Auto)", displayed: false, isStateChange: true)
    sendEvent(name: "thermostatOperatingState", value: "idle", descriptionText: "Thermostat Operating State is: (Idle)", displayed: false, isStateChange: true)
    sendEvent(name: "humidity", value: 40, unit: "%", descriptionText: "Relative Humidity is: (40%)", displayed: false, isStateChange: true)
    sendEvent(name: "presence", value: "present", descriptionText: "Presence is: (Present)", displayed: false, isStateChange: true)
    sendEvent(name: "nestPresence", value: "home", descriptionText: "Nest Presence is: (Home)", displayed: false, isStateChange: true)
    sendEvent(name: "safetyTempMin", value: 60, unit: getTemperatureScale(), descriptionText: "Safety Temp Min is: (60°${getTemperatureScale()})", displayed: false, isStateChange: true)
    sendEvent(name: "safetyTempMax", value: 85, unit: getTemperatureScale(), descriptionText: "Safety Temp Max is: (85°${getTemperatureScale()})", displayed: false, isStateChange: true)
    sendEvent(name: "safetyHumidityMax", value: 80, unit: "%", descriptionText: "Safety Humidity Max is: (80%)", displayed: false, isStateChange: true)
    //sendEvent(name: "safetyHumidityMin", value: 15, unit: "%", descriptionText: "Safety Humidity Min is: (15%)", displayed: false, isStateChange: true)
    sendEvent(name: "comfortDewpointMax", value: 65, unit: getTemperatureScale(), descriptionText: "Comfort Dew Point Max is: (65°${getTemperatureScale()})", displayed: false, isStateChange: true)
    sendEvent(name: "tempLockOn", value: false, descriptionText: "Nest Temp Lock is: (Off)", displayed: false, isStateChange: true)
    sendEvent(name: "lockedTempMin", value: 60, unit: getTemperatureScale(), descriptionText: "Locked Temp Min is: (60°${getTemperatureScale()})", displayed: false, isStateChange: true)
    sendEvent(name: "lockedTempMax", value: 80, unit: getTemperatureScale(), descriptionText: "Locked Temp Max is: (80°${getTemperatureScale()})", displayed: false, isStateChange: true)
}

def parse(String description) {
}

def evaluate(temp, heatingSetpoint, coolingSetpoint) {
    //log.debug "${device?.displayName} evaluate(Temp: $temp°${getTemperatureScale()}, HeatSetpoint: $heatingSetpoint°${getTemperatureScale()}, CoolSetpoint: $coolingSetpoint°${getTemperatureScale()})"
    def threshold = 1.0
    def current = device.currentValue("thermostatOperatingState")
    def mode = device.currentValue("thermostatMode")

    def heating = false
    def cooling = false
    def idle = false
    if (mode in ["heat","emergency heat","auto"]) {
        if (heatingSetpoint - temp >= threshold) {
            heating = true
            sendEvent(name: "thermostatOperatingState", value: "heating")
        }
        else if (temp - heatingSetpoint >= threshold) {
            idle = true
        }
        sendEvent(name: "thermostatSetpoint", value: heatingSetpoint)
    }
    if (mode in ["cool","auto"]) {
        if (temp - coolingSetpoint >= threshold) {
            cooling = true
            sendEvent(name: "thermostatOperatingState", value: "cooling")
        }
        else if (coolingSetpoint - temp >= threshold && !heating) {
            idle = true
        }
        sendEvent(name: "thermostatSetpoint", value: coolingSetpoint, unit: getTemperatureScale())
    }
    else {
        sendEvent(name: "thermostatSetpoint", value: heatingSetpoint, unit: getTemperatureScale())
    }
    if (idle && !heating && !cooling) {
        sendEvent(name: "thermostatOperatingState", value: "idle")
    }
}

def setHeatingSetpoint(Double degreesF) {
    log.debug "setHeatingSetpoint($degreesF)"
    sendEvent(name: "heatingSetpoint", value: degreesF, unit: getTemperatureScale(), descriptionText: "Heating Setpoint is: ($value°${getTemperatureScale()})", displayed: false, isStateChange: true)
    evaluate(device.currentValue("temperature"), degreesF, device.currentValue("coolingSetpoint"))
}

def setCoolingSetpoint(Double degreesF) {
    log.debug "setCoolingSetpoint($degreesF)"
    sendEvent(name: "coolingSetpoint", value: degreesF, unit: getTemperatureScale(), descriptionText: "Cooling Setpoint is: ($value°${getTemperatureScale()})", displayed: false, isStateChange: true)
    evaluate(device.currentValue("temperature"), device.currentValue("heatingSetpoint"), degreesF)
}

def setThermostatMode(String value) {
    sendEvent(name: "thermostatMode", value: value, descriptionText: "Thermostat Mode is: ($value)", displayed: false, isStateChange: true)
    evaluate(device.currentValue("temperature"), device.currentValue("heatingSetpoint"), device.currentValue("coolingSetpoint"))
}

def setThermostatFanMode(String value) {
    sendEvent(name: "thermostatFanMode", value: value, descriptionText: "Thermostat Fan is: ($value)", displayed: false, isStateChange: true)
}

def off() {
    sendEvent(name: "thermostatMode", value: "off", descriptionText: "Thermostat Mode is: (Off)", displayed: false, isStateChange: true)
    evaluate(device.currentValue("temperature"), device.currentValue("heatingSetpoint"), device.currentValue("coolingSetpoint"))
}

def heat() {
    sendEvent(name: "thermostatMode", value: "heat", descriptionText: "Thermostat Mode is: (Heat)", displayed: false, isStateChange: true)
    evaluate(device.currentValue("temperature"), device.currentValue("heatingSetpoint"), device.currentValue("coolingSetpoint"))
}

def auto() {
    sendEvent(name: "thermostatMode", value: "auto", descriptionText: "Thermostat Mode is: (Auto)", displayed: false, isStateChange: true)
    evaluate(device.currentValue("temperature"), device.currentValue("heatingSetpoint"), device.currentValue("coolingSetpoint"))
}

def emergencyHeat() {
    sendEvent(name: "thermostatMode", value: "emergency heat", descriptionText: "Thermostat Mode is: (Emergency Heat)", displayed: false, isStateChange: true)
    evaluate(device.currentValue("temperature"), device.currentValue("heatingSetpoint"), device.currentValue("coolingSetpoint"))
}

def cool() {
    sendEvent(name: "thermostatMode", value: "cool", descriptionText: "Thermostat Mode is: (Cool)", displayed: false, isStateChange: true)
    evaluate(device.currentValue("temperature"), device.currentValue("heatingSetpoint"), device.currentValue("coolingSetpoint"))
}

def fanOn() {
    sendEvent(name: "thermostatFanMode", value: "on", descriptionText: "Thermostat Fan is: (On)", displayed: false, isStateChange: true)
}

def fanAuto() {
    sendEvent(name: "thermostatFanMode", value: "auto", descriptionText: "Thermostat Fan is: (Auto)", displayed: false, isStateChange: true)
}

def fanCirculate() {
    sendEvent(name: "thermostatFanMode", value: "circulate", descriptionText: "Thermostat Fan is: (Circulate)", displayed: false, isStateChange: true)
}

def poll() {
    null
}

def tempUp() {
    def ts = device.currentState("temperature")
    def value = ts ? ts.integerValue + 1 : 72
    sendEvent(name:"temperature", value: value, unit: getTemperatureScale(), descriptionText: "Temperature is: (${value}°${getTemperatureScale()})", displayed: false, isStateChange: true)
    log.debug "Ambient Temperature is now: (${value}°${getTemperatureScale()})"
    evaluate(value, device.currentValue("heatingSetpoint"), device.currentValue("coolingSetpoint"))
}

def tempDown() {
    def ts = device.currentState("temperature")
    def value = ts ? ts.integerValue - 1 : 72
    sendEvent(name:"temperature", value: value, unit: getTemperatureScale(), descriptionText: "Temperature is: (${value}°${getTemperatureScale()})", displayed: false, isStateChange: true)
    log.debug "temperature is now: (${value}°${getTemperatureScale()})"
    evaluate(value, device.currentValue("heatingSetpoint"), device.currentValue("coolingSetpoint"))
}

def setTemperature(value) {
    def ts = device.currentState("temperature")
    sendEvent(name:"temperature", value: value, unit: getTemperatureScale(), descriptionText: "Temperature is: (${value}°${getTemperatureScale()})", displayed: false, isStateChange: true)
    log.debug "temperature is now: (${value}°${getTemperatureScale()})"
    evaluate(value, device.currentValue("heatingSetpoint"), device.currentValue("coolingSetpoint"))
}

def heatUp() {
    def ts = device.currentState("heatingSetpoint")
    def value = ts ? ts.integerValue + 1 : 68
    sendEvent(name:"heatingSetpoint", value: value, unit: getTemperatureScale(), descriptionText: "Heating Setpoint is: (${value}°${getTemperatureScale()})", displayed: false, isStateChange: true)
    log.debug "heatingSetpoint is now: (${value}°${getTemperatureScale()})"
    evaluate(device.currentValue("temperature"), value, device.currentValue("coolingSetpoint"))
}

def heatDown() {
    def ts = device.currentState("heatingSetpoint")
    def value = ts ? ts.integerValue - 1 : 68
    sendEvent(name:"heatingSetpoint", value: value, unit: getTemperatureScale(), descriptionText: "Heating Setpoint is: (${value}°${getTemperatureScale()})", displayed: false, isStateChange: true)
    log.debug "heatingSetpoint is now: (${value}°${getTemperatureScale()})"
    evaluate(device.currentValue("temperature"), value, device.currentValue("coolingSetpoint"))
}


def coolUp() {
    def ts = device.currentState("coolingSetpoint")
    def value = ts ? ts.integerValue + 1 : 76
    sendEvent(name:"coolingSetpoint", value: value, unit: getTemperatureScale(), descriptionText: "Cooling Setpoint is: (${value}°${getTemperatureScale()})", displayed: false, isStateChange: true)
    log.debug "Cool Setpoint is now: (${value}°${getTemperatureScale()})"
    evaluate(device.currentValue("temperature"), device.currentValue("heatingSetpoint"), value)
}

def coolDown() {
    def ts = device.currentState("coolingSetpoint")
    def value = ts ? ts.integerValue - 1 : 76
    sendEvent(name:"coolingSetpoint", value: value, descriptionText: "Cooling Setpoint is: (${value}°${getTemperatureScale()})", displayed: false, isStateChange: true)
    log.debug "Cool Setpoint is now: (${value}°${getTemperatureScale()})"
    evaluate(device.currentValue("temperature"), device.currentValue("heatingSetpoint"), value)
}

def humidityUp() {
    def ts = device.currentState("humidity")
    def value = ts ? ts.integerValue + 1 : 76
    sendEvent(name:"humidity", unit: "%", value: value, descriptionText: "Relative Humidity is: (${value}%)", displayed: false, isStateChange: true)
    log.debug "Relative Humidity is now: (${value}%)"
}

def humidityDown() {
    def ts = device.currentState("humidity")
    def value = ts ? ts.integerValue - 1 : 76
    sendEvent(name:"humidity", unit: "%", value: value, descriptionText: "Relative Humidity is: (${value}%)", displayed: false, isStateChange: true)
    log.debug "Relative Humidity is now: (${value}%)"
}

def changePresence() {
    def pres = device.currentState("presence")?.stringValue
    def nPres = device?.currentState("nestPresence")?.stringValue
    def newPres = (pres == "present") ? "not present" : "present"
    def newNestPres = (pres == "present") ? "away" : "home"
    log.debug "Nest Presence is now: (${newNestPres})"
    log.debug "Presence is now: (${newPres})"
    sendEvent(name: 'nestPresence', value: newNestPres, descriptionText: "Nest Presence is: ${newNestPres}", displayed: true, isStateChange: true )
    sendEvent(name: 'presence', value: newPres, descriptionText: "Device is: ${newPres}", displayed: false, isStateChange: true, state: newPres )
}

def changeTempLock() {
    def cur = device.currentState("tempLockOn")?.stringValue
    def newS = (cur.toString() == "true") ? false : true
    log.debug "Nest Temp Lock is now: (${newS == true ? "On" : "Off"})"
    sendEvent(name: 'tempLockOn', value: newS, descriptionText: "Nest Temp Lock is: ${newS == true ? "On" : "Off"}", displayed: false, isStateChange: true, state: newS )
}

def safetyTempMinUp() {
    def ts = device.currentState("safetyTempMin")
    def value = ts ? ts.integerValue + 1 : 72
    log.debug "Safety Temp Minimum is now: (${value}°${getTemperatureScale()})"
    sendEvent(name: 'safetyTempMin', value: value, unit: getTemperatureScale(), descriptionText: "Safety Temp Minimum is: (${value}°${getTemperatureScale()})", displayed: false, isStateChange: true)
}

def safetyTempMinDown() {
    def ts = device.currentState("safetyTempMin")
    def value = ts ? ts.integerValue - 1 : 72
    log.debug "Safety Temp Minimum is now: (${value}°${getTemperatureScale()})"
    sendEvent(name:'safetyTempMin', value: value, unit: getTemperatureScale(), descriptionText: "Safety Temp Minimum is: (${value}°${getTemperatureScale()})", displayed: false, isStateChange: true)
}

def safetyTempMaxUp() {
    def ts = device.currentState("safetyTempMax")
    def value = ts ? ts.integerValue + 1 : 72
    log.debug "Safety Temp Maximum is now: (${value}°${getTemperatureScale()})"
    sendEvent(name:'safetyTempMax', value: value, unit: getTemperatureScale(), descriptionText: "Safety Temp Maximum is: (${value}°${getTemperatureScale()})", displayed: false, isStateChange: true)
}

def safetyTempMaxDown() {
    def ts = device.currentState("safetyTempMin")
    def value = ts ? ts.integerValue - 1 : 72
    log.debug "Safety Temp Maximum is now: (${value}°${getTemperatureScale()})"
    sendEvent(name: 'safetyTempMax', value: value, unit: getTemperatureScale(), descriptionText: "Safety Temp Maximum is: (${value}°${getTemperatureScale()})", displayed: false, isStateChange: true)
}

def safetyHumidityMinUp() {
    def ts = device.currentState("safetyHumidityMin")
    def value = ts ? ts.integerValue + 1 : 15
    log.debug "Safety Humidity Min is now: (${value}%)"
    sendEvent(name: 'safetyHumidityMin', value: value, unit: "%", descriptionText: "Safety Humidity Min is: (${value}%)", displayed: false, isStateChange: true)
}

def safetyHumidityMinDown() {
    def ts = device.currentState("safetyHumidityMin")
    def value = ts ? ts.integerValue - 1 : 15
    log.debug "Safety Humidity Min is now: (${value}%)"
    sendEvent(name: 'safetyHumidityMin', value: value, unit: "%", descriptionText: "Safety Humidity Max is: (${value}%)", displayed: false, isStateChange: true)
}

def safetyHumidityMaxUp() {
    def ts = device.currentState("safetyHumidityMax")
    def value = ts ? ts.integerValue + 1 : 72
    log.debug "Safety Humidity Max is now: (${value}%)"
    sendEvent(name: 'safetyHumidityMax', value: value, unit: "%", descriptionText: "Safety Humidity Max is: (${value}%)", displayed: false, isStateChange: true)
}

def safetyHumidityMaxDown() {
    def ts = device.currentState("safetyHumidityMax")
    def value = ts ? ts.integerValue - 1 : 72
    log.debug "Safety Humidity Max is now: (${value}%)"
    sendEvent(name: 'safetyHumidityMax', value: value, unit: "%", descriptionText: "Safety Humidity Max is: (${value}%)", displayed: false, isStateChange: true)
}

def comfortDewpointMaxUp() {
    def ts = device.currentState("comfortDewpointMax")
    def value = ts ? ts.integerValue + 1 : 60
    log.debug "Comfort Dew Point Max is now: (${value}°${getTemperatureScale()})"
    sendEvent(name: 'comfortDewpointMax', value: value, unit: getTemperatureScale(), descriptionText: "Comfort Dew Point Max is: (${value}°${getTemperatureScale()})", displayed: false, isStateChange: true)
}

def comfortDewpointMaxDown() {
    def ts = device.currentState("comfortDewpointMax")
    def value = ts ? ts.integerValue - 1 : 60
    log.debug "Comfort Dew Point Max is now: (${value}°${getTemperatureScale()})"
    sendEvent(name: 'comfortDewpointMax', value: value, unit: getTemperatureScale(), descriptionText: "Comfort Dew Point Max is: (${value}°${getTemperatureScale()})", displayed: false, isStateChange: true)
}

def lockedTempMinUp() {
    def ts = device.currentState("lockedTempMin")
    def value = ts ? ts.integerValue + 1 : 72
    log.debug "Locked Temp Min is now: (${value}°${getTemperatureScale()})"
    sendEvent(name: 'lockedTempMin', value: value, unit: getTemperatureScale(), descriptionText: "Locked Temp Min is: (${value}°${getTemperatureScale()})", displayed: false, isStateChange: true)
}

def lockedTempMinDown() {
    def ts = device.currentState("lockedTempMin")
    def value = ts ? ts.integerValue - 1 : 72
    log.debug "Locked Temp Min is now: (${value}°${getTemperatureScale()})"
    sendEvent(name:'lockedTempMin', value: value, unit: getTemperatureScale(), descriptionText: "Locked Temp Min is: (${value}°${getTemperatureScale()})", displayed: false, isStateChange: true)
}

def lockedTempMaxUp() {
    def ts = device.currentState("lockedTempMax")
    def value = ts ? ts.integerValue + 1 : 72
    log.debug "Locked Temp Max is now: (${value}°${getTemperatureScale()})"
    sendEvent(name: 'lockedTempMax', value: value, unit: getTemperatureScale(), descriptionText: "Locked Temp Max is: (${value}°${getTemperatureScale()})", displayed: false, isStateChange: true)
}

def lockedTempMaxDown() {
    def ts = device.currentState("lockedTempMin")
    def value = ts ? ts.integerValue - 1 : 72
    log.debug "Locked Temp Max is now: (${value}°${getTemperatureScale()})"
    sendEvent(name: 'lockedTempMax', value: value, unit: getTemperatureScale(), descriptionText: "Locked Temp Max is: (${value}°${getTemperatureScale()})", displayed: false, isStateChange: true)
}

def changeOperState() {
    def ts = device.currentThermostatOperatingState.toString()
    switch(ts) {
        case "idle":
            setHeating()
            break
        case "heating":
            setCooling()
            break
        case "cooling":
            setFanOnly()
            break
        case "fan only":
            setIdle()
            break
    }
}

def setIdle() {
    def value = "idle"
    log.debug "thermostatOperatingState is now: (${value})"
    sendEvent(name: 'thermostatOperatingState', value: value, descriptionText: "OperatingState is now: (${value})", displayed: false, isStateChange: true)
}

def setHeating() {
    def value = "heating"
    log.debug "thermostatOperatingState is now: (${value})"
    sendEvent(name: 'thermostatOperatingState', value: value, descriptionText: "OperatingState is now: (${value})", displayed: false, isStateChange: true)
}

def setCooling() {
    def value = "cooling"
    log.debug "thermostatOperatingState is now: (${value})"
    sendEvent(name: 'thermostatOperatingState', value: value, descriptionText: "OperatingState is now: (${value})", displayed: false, isStateChange: true)
}

def setFanOnly() {
    def value = "fan only"
    log.debug "thermostatOperatingState is now: (${value})"
    sendEvent(name: 'thermostatOperatingState', value: value, descriptionText: "OperatingState is now: (${value})", displayed: false, isStateChange: true)
}
