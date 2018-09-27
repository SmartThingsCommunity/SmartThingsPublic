/**
 *  Child Thermostat
 *
 *  Copyright 2018 Alexander Belov, Z-Wave.Me
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
    definition (name: "Child Thermostat", namespace: "z-wave-me", author: "Alexander Belov") {
        capability "Refresh"
        capability "Sensor"
        capability "Temperature Measurement"
        capability "Thermostat"
        capability "Thermostat Cooling Setpoint"
        capability "Thermostat Heating Setpoint"
        capability "Thermostat Mode"
        
        attribute "thermostatFanState", "string"
        attribute "lastUpdated", "String"

        command "switchMode"
        command "lowerHeatingSetpoint"
        command "raiseHeatingSetpoint"
        command "lowerCoolSetpoint"
        command "raiseCoolSetpoint"
        command "poll"
        command "pollDevice"
    }

    tiles (scale: 2) {
        standardTile("logo", "device.logo", inactiveLabel: true, decoration: "flat", width: 1, height: 1) {
            state "default", label:'', icon: "st.alarm.temperature.normal"
        }
        valueTile("lastUpdated", "device.lastUpdated", decoration: "flat", width: 5, height: 1) {
            state "default", label:'Last updated ${currentValue}'
        }
        multiAttributeTile(name:"temperature", type:"generic", width:6, height:4, canChangeIcon: true) {
            tileAttribute("device.temperature", key: "PRIMARY_CONTROL") {
                attributeState("temperature", label:'${currentValue}°', icon: "st.alarm.temperature.normal",
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
                )
            }
        }
        standardTile("lowerHeatingSetpoint", "device.heatingSetpoint", width:2, height:1, inactiveLabel: false, decoration: "flat") {
            state "heatingSetpoint", action:"lowerHeatingSetpoint", icon:"st.thermostat.thermostat-left"
        }
        valueTile("heatingSetpoint", "device.heatingSetpoint", width:2, height:1, inactiveLabel: false, decoration: "flat") {
            state "heatingSetpoint", label:'${currentValue}° heat', backgroundColor:"#ffffff"
        }
        standardTile("raiseHeatingSetpoint", "device.heatingSetpoint", width:2, height:1, inactiveLabel: false, decoration: "flat") {
            state "heatingSetpoint", action:"raiseHeatingSetpoint", icon:"st.thermostat.thermostat-right"
        }
        standardTile("lowerCoolSetpoint", "device.coolingSetpoint", width:2, height:1, inactiveLabel: false, decoration: "flat") {
            state "coolingSetpoint", action:"lowerCoolSetpoint", icon:"st.thermostat.thermostat-left"
        }
        valueTile("coolingSetpoint", "device.coolingSetpoint", width:2, height:1, inactiveLabel: false, decoration: "flat") {
            state "coolingSetpoint", label:'${currentValue}° cool', backgroundColor:"#ffffff"
        }
        standardTile("raiseCoolSetpoint", "device.heatingSetpoint", width:2, height:1, inactiveLabel: false, decoration: "flat") {
            state "heatingSetpoint", action:"raiseCoolSetpoint", icon:"st.thermostat.thermostat-right"
        }
        standardTile("mode", "device.thermostatMode", width:3, height:1, inactiveLabel: false, decoration: "flat") {
            state "off", action:"switchMode", nextState:"...", icon: "st.thermostat.heating-cooling-off"
            state "heat", action:"switchMode", nextState:"...", icon: "st.thermostat.heat"
            state "cool", action:"switchMode", nextState:"...", icon: "st.thermostat.cool"
            state "auto", action:"switchMode", nextState:"...", icon: "st.thermostat.auto"
            state "...", label: "Updating...",nextState:"...", backgroundColor:"#ffffff"
        }
        standardTile("refresh", "device.thermostatMode", width:3, height:1, inactiveLabel: false, decoration: "flat") {
            state "default", action:"refresh.refresh", icon:"st.secondary.refresh"
        }
    }
}

def installed() {
    runIn(3, "initialize", [overwrite: true])  // Allow configure command to be sent and acknowledged before proceeding
}

def updated() {
    // If not set update ManufacturerSpecific data
    if (!getDataValue("manufacturer")) {
        sendHubCommand(new physicalgraph.device.HubAction(zwave.manufacturerSpecificV2.manufacturerSpecificGet().format()))
        runIn(2, "initialize", [overwrite: true])  // Allow configure command to be sent and acknowledged before proceeding
    } else {
        initialize()
    }
}

def initialize() {
    unschedule()
    if (getDataValue("manufacturer") != "Honeywell") {
        runEvery5Minutes("poll")  // This is not necessary for Honeywell Z-wave, but could be for other Z-wave thermostats
    }
    pollDevice()
}

def parse(def description) {
    def cmd = zwave.parse(description)
    
    if (!description.startsWith("Err")) {
        if (description != "updated") zwaveEvent(cmd)
    } else {
        createEvent(descriptionText: description, isStateChange:true)
    }
    
    def nowDay = new Date().format("MMM dd", location.timeZone)
    def nowTime = new Date().format("h:mm a", location.timeZone)
    sendEvent(name: "lastUpdated", value: nowDay + " at " + nowTime, displayed: false)
}

// events
def zwaveEvent(physicalgraph.zwave.commands.thermostatsetpointv2.ThermostatSetpointReport cmd) {
    def cmdScale = cmd.scale == 1 ? "F" : "C"
    def setpoint = getTempInLocalScale(cmd.scaledValue, cmdScale)
    def unit = getTemperatureScale()
    
    switch (cmd.setpointType) {
        case 1:
            sendEvent(name: "heatingSetpoint", value: setpoint, unit: unit, displayed: false)
            updateThermostatSetpoint("heatingSetpoint", setpoint)
            break;
        case 2:
            sendEvent(name: "coolingSetpoint", value: setpoint, unit: unit, displayed: false)
            updateThermostatSetpoint("coolingSetpoint", setpoint)
            break;
        default:
            log.debug "unknown setpointType $cmd.setpointType"
            return
    }
    sendEvent(name: "temperature", value: cmd.scaledValue)
    // So we can respond with same format
    state.size = cmd.size
    state.scale = cmd.scale
    state.precision = cmd.precision
    // Make sure return value is not result from above expresion
    return 0
}

def zwaveEvent(physicalgraph.zwave.commands.thermostatmodev2.ThermostatModeReport cmd) {
    def map = [name: "thermostatMode", data:[supportedThermostatModes: state.supportedModes]]
    
    switch (cmd.mode) {
        case physicalgraph.zwave.commands.thermostatmodev2.ThermostatModeReport.MODE_OFF:
            map.value = "off"
            break
            
        case physicalgraph.zwave.commands.thermostatmodev2.ThermostatModeReport.MODE_HEAT:
            map.value = "heat"
            break
            
        case physicalgraph.zwave.commands.thermostatmodev2.ThermostatModeReport.MODE_COOL:
            map.value = "cool"
            break
            
        case physicalgraph.zwave.commands.thermostatmodev2.ThermostatModeReport.MODE_AUTO:
            map.value = "auto"
            break
    }
    sendEvent(map)
    updateThermostatSetpoint(null, null)
}

def zwaveEvent(physicalgraph.zwave.commands.thermostatmodev2.ThermostatModeSupportedReport cmd) {
    def supportedModes = []
    
    if(cmd.off) { supportedModes << "off" }
    if(cmd.heat) { supportedModes << "heat" }
    if(cmd.cool) { supportedModes << "cool" }
    if(cmd.auto) { supportedModes << "auto" }

    state.supportedModes = supportedModes
    sendEvent(name: "supportedThermostatModes", value: supportedModes, displayed: false)
}

def zwaveEvent(physicalgraph.zwave.commands.manufacturerspecificv2.ManufacturerSpecificReport cmd) {
    if (cmd.manufacturerName) {
        updateDataValue("manufacturer", cmd.manufacturerName)
    }
    if (cmd.productTypeId) {
        updateDataValue("productTypeId", cmd.productTypeId.toString())
    }
    if (cmd.productId) {
        updateDataValue("productId", cmd.productId.toString())
    }
}

// Command Inplementations
def poll() {
    // Call refresh which will cap the polling to once every 2 minutes
    refresh()
}

def refresh() {
    // Only allow refresh every 1 minutes to prevent flooding the Zwave network
    def timeNow = now()
    
    if (!state.refreshTriggeredAt || (1 * 60 * 1000 < (timeNow - state.refreshTriggeredAt))) {
        state.refreshTriggeredAt = timeNow
        // use runIn with overwrite to prevent multiple DTH instances run before state.refreshTriggeredAt has been saved
        runIn(2, "pollDevice", [overwrite: true])
    }
}

def pollDevice() {
    parent.parentCommand(parent.encap(zwaveHubNodeId, parent.extractEP(device.deviceNetworkId), zwave.thermostatModeV2.thermostatModeSupportedGet().format()))
    parent.parentCommand(parent.encap(zwaveHubNodeId, parent.extractEP(device.deviceNetworkId), zwave.thermostatModeV2.thermostatModeGet().format()))
    parent.parentCommand(parent.encap(zwaveHubNodeId, parent.extractEP(device.deviceNetworkId), zwave.thermostatSetpointV1.thermostatSetpointGet(setpointType: 1).format()))
    parent.parentCommand(parent.encap(zwaveHubNodeId, parent.extractEP(device.deviceNetworkId), zwave.thermostatSetpointV1.thermostatSetpointGet(setpointType: 2).format()))
    
}

def raiseHeatingSetpoint() {
    alterSetpoint(true, "heatingSetpoint")
}

def lowerHeatingSetpoint() {
    alterSetpoint(false, "heatingSetpoint")
}

def raiseCoolSetpoint() {
    alterSetpoint(true, "coolingSetpoint")
}

def lowerCoolSetpoint() {
    alterSetpoint(false, "coolingSetpoint")
}

def alterSetpoint(raise, setpoint) {
    def locationScale = getTemperatureScale()
    def deviceScale = (state.scale == 1) ? "F" : "C"
    def heatingSetpoint = getTempInLocalScale("heatingSetpoint")
    def coolingSetpoint = getTempInLocalScale("coolingSetpoint")
    def targetValue = (setpoint == "heatingSetpoint") ? heatingSetpoint : coolingSetpoint
    def delta = (locationScale == "F") ? 1 : 0.5
    
    targetValue += raise ? delta : - delta

    def data = enforceSetpointLimits(setpoint, [targetValue: targetValue, heatingSetpoint: heatingSetpoint, coolingSetpoint: coolingSetpoint])
    
    // update UI without waiting for the device to respond, this to give user a smoother UI experience
    // also, as runIn's have to overwrite and user can change heating/cooling setpoint separately separate runIn's have to be used
    if (data.targetHeatingSetpoint) {
        sendEvent("name": "heatingSetpoint", "value": getTempInLocalScale(data.targetHeatingSetpoint, deviceScale),
                unit: getTemperatureScale(), eventType: "ENTITY_UPDATE", displayed: false)
    }
    if (data.targetCoolingSetpoint) {
        sendEvent("name": "coolingSetpoint", "value": getTempInLocalScale(data.targetCoolingSetpoint, deviceScale),
                unit: getTemperatureScale(), eventType: "ENTITY_UPDATE", displayed: false)
    }
    if (data.targetHeatingSetpoint && data.targetCoolingSetpoint) {
        runIn(5, "updateHeatingSetpoint", [data: data, overwrite: true])
    } else if (setpoint == "heatingSetpoint" && data.targetHeatingSetpoint) {
        runIn(5, "updateHeatingSetpoint", [data: data, overwrite: true])
    } else if (setpoint == "coolingSetpoint" && data.targetCoolingSetpoint) {
        runIn(5, "updateCoolingSetpoint", [data: data, overwrite: true])
    }
}

def updateHeatingSetpoint(data) {
    updateSetpoints(data)
}

def updateCoolingSetpoint(data) {
    updateSetpoints(data)
}

def enforceSetpointLimits(setpoint, data) {
    def locationScale = getTemperatureScale() 
    def minSetpoint = (setpoint == "heatingSetpoint") ? getTempInDeviceScale(40, "F") : getTempInDeviceScale(50, "F")
    def maxSetpoint = (setpoint == "heatingSetpoint") ? getTempInDeviceScale(90, "F") : getTempInDeviceScale(99, "F")
    def deadband = (state.scale == 1) ? 3 : 2  // 3°F, 2°C
    def targetValue = getTempInDeviceScale(data.targetValue, locationScale)
    def heatingSetpoint = null
    def coolingSetpoint = null
    
    // Enforce min/mix for setpoints
    if (targetValue > maxSetpoint) {
        targetValue = maxSetpoint
    } else if (targetValue < minSetpoint) {
        targetValue = minSetpoint
    }
    // Enforce 3 degrees F deadband between setpoints
    if (setpoint == "heatingSetpoint") {
        heatingSetpoint = targetValue 
        coolingSetpoint = (heatingSetpoint + deadband > getTempInDeviceScale(data.coolingSetpoint, locationScale)) ? heatingSetpoint + deadband : null
    }
    if (setpoint == "coolingSetpoint") {
        coolingSetpoint = targetValue
        heatingSetpoint = (coolingSetpoint - deadband < getTempInDeviceScale(data.heatingSetpoint, locationScale)) ? coolingSetpoint - deadband : null
    }
    return [targetHeatingSetpoint: heatingSetpoint, targetCoolingSetpoint: coolingSetpoint]
}


def setHeatingSetpoint(degrees) {
    if (degrees) {
        state.heatingSetpoint = degrees.toDouble()
        runIn(2, "updateSetpoints", [overwrite: true])
    }
}

def setCoolingSetpoint(degrees) {
    if (degrees) {
        state.coolingSetpoint = degrees.toDouble()
        runIn(2, "updateSetpoints", [overwrite: true])
    }
}

def updateSetpoints() {
    def deviceScale = (state.scale == 1) ? "F" : "C"
    def data = [targetHeatingSetpoint: null, targetCoolingSetpoint: null]
    def heatingSetpoint = getTempInLocalScale("heatingSetpoint")
    def coolingSetpoint = getTempInLocalScale("coolingSetpoint")
    
    if (state.heatingSetpoint) {
        data = enforceSetpointLimits("heatingSetpoint", [targetValue: state.heatingSetpoint,
                heatingSetpoint: heatingSetpoint, coolingSetpoint: coolingSetpoint])
    }
    if (state.coolingSetpoint) {
        heatingSetpoint = data.targetHeatingSetpoint ? getTempInLocalScale(data.targetHeatingSetpoint, deviceScale) : heatingSetpoint
        coolingSetpoint = data.targetCoolingSetpoint ? getTempInLocalScale(data.targetCoolingSetpoint, deviceScale) : coolingSetpoint
        data = enforceSetpointLimits("coolingSetpoint", [targetValue: state.coolingSetpoint,
                heatingSetpoint: heatingSetpoint, coolingSetpoint: coolingSetpoint])
        data.targetHeatingSetpoint = data.targetHeatingSetpoint ?: heatingSetpoint
    }
    state.heatingSetpoint = null
    state.coolingSetpoint = null
    updateSetpoints(data)
}

def updateSetpoints(data) {
    if (data.targetHeatingSetpoint) {
        parent.parentCommand(parent.encap(zwaveHubNodeId, parent.extractEP(device.deviceNetworkId), zwave.thermostatSetpointV1.thermostatSetpointSet(setpointType: 1, scale: state.scale, precision: state.precision, scaledValue: data.targetHeatingSetpoint).format()))
    }
    if (data.targetCoolingSetpoint) {
        parent.parentCommand(parent.encap(zwaveHubNodeId, parent.extractEP(device.deviceNetworkId), zwave.thermostatSetpointV1.thermostatSetpointSet(setpointType: 2, scale: state.scale, precision: state.precision, scaledValue: data.targetCoolingSetpoint)))
    }
}

def updateThermostatSetpoint(setpoint, value) {
    def scale = getTemperatureScale()
    def heatingSetpoint = (setpoint == "heatingSetpoint") ? value : getTempInLocalScale("heatingSetpoint")
    def coolingSetpoint = (setpoint == "coolingSetpoint") ? value : getTempInLocalScale("coolingSetpoint")
    def mode = device.currentValue("thermostatMode")
    def thermostatSetpoint = heatingSetpoint    // corresponds to (mode == "heat" || mode == "emergency heat")
    
    if (mode == "cool") {
        thermostatSetpoint = coolingSetpoint
    } else if (mode == "auto" || mode == "off") {
        // Set thermostatSetpoint to the setpoint closest to the current temperature
        def currentTemperature = getTempInLocalScale("temperature")
        if (currentTemperature > (heatingSetpoint + coolingSetpoint)/2) {
            thermostatSetpoint = coolingSetpoint
        }
    }
    sendEvent(name: "thermostatSetpoint", value: thermostatSetpoint, unit: getTemperatureScale())
}

def switchMode() {
    def currentMode = device.currentValue("thermostatMode")
    def supportedModes = state.supportedModes
    
    // Old version of supportedModes was as string, make sure it gets updated
    if (supportedModes && supportedModes.size() && supportedModes[0].size() > 1) {
        def next = { supportedModes[supportedModes.indexOf(it) + 1] ?: supportedModes[0] }
        def nextMode = next(currentMode)
        runIn(2, "setGetThermostatMode", [data: [nextMode: nextMode], overwrite: true])
    } else {
        log.warn "supportedModes not defined"
        getSupportedModes()
    }
}

def switchToMode(nextMode) {
    def supportedModes = state.supportedModes
    
    // Old version of supportedModes was as string, make sure it gets updated
    if (supportedModes && supportedModes.size() && supportedModes[0].size() > 1) {
        if (supportedModes.contains(nextMode)) {
            runIn(2, "setGetThermostatMode", [data: [nextMode: nextMode], overwrite: true])
        } else {
            log.debug("ThermostatMode $nextMode is not supported by ${device.displayName}")
        }
    } else {
        log.warn "supportedModes not defined"
        getSupportedModes()
    }
}

def getSupportedModes() {
    parent.parentCommand(parent.encap(zwaveHubNodeId, parent.extractEP(device.deviceNetworkId), zwave.thermostatModeV2.thermostatModeSupportedGet()))
}

def getModeMap() { [
    "off": 0,
    "heat": 1,
    "cool": 2,
    "auto": 3
]}

def setThermostatMode(String value) {
    switchToMode(value)
}

def setGetThermostatMode(data) {
    parent.parentCommand(parent.encap(zwaveHubNodeId, parent.extractEP(device.deviceNetworkId), zwave.thermostatModeV2.thermostatModeSet(mode: modeMap[data.nextMode]).format()))
    parent.parentCommand(parent.encap(zwaveHubNodeId, parent.extractEP(device.deviceNetworkId), zwave.thermostatModeV2.thermostatModeGet().format()))
}

def off() {
    switchToMode("off")
}

def heat() {
    switchToMode("heat")
}

def cool() {
    switchToMode("cool")
}

def auto() {
    switchToMode("auto")
}

def getTempInLocalScale(state) {
    def temp = device.currentState(state)
    
    if (temp && temp.value && temp.unit) {
        return getTempInLocalScale(temp.value.toBigDecimal(), temp.unit)
    }
    return 0
}

// get/convert temperature to current local scale
def getTempInLocalScale(temp, scale) {
    if (temp && scale) {
        def scaledTemp = convertTemperatureIfNeeded(temp.toBigDecimal(), scale).toDouble()
        return (getTemperatureScale() == "F" ? scaledTemp.round(0).toInteger() : roundC(scaledTemp))
    }
    return 0
}

def getTempInDeviceScale(state) {
    def temp = device.currentState(state)
    
    if (temp && temp.value && temp.unit) {
        return getTempInDeviceScale(temp.value.toBigDecimal(), temp.unit)
    }
    return 0
}

def getTempInDeviceScale(temp, scale) {
    if (temp && scale) {
        def deviceScale = (state.scale == 1) ? "F" : "C"
        
        return (deviceScale == scale) ? temp :
                (deviceScale == "F" ? celsiusToFahrenheit(temp).toDouble().round(0).toInteger() : roundC(fahrenheitToCelsius(temp)))
    }
    return 0
}

def roundC (tempC) {
    return (Math.round(tempC.toDouble() * 2))/2
}
