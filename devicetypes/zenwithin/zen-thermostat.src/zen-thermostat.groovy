/**
 *  Zen Thermostat
 *
 *  Author: Zen Within
 *  Date: 2015-02-21
 *  Updated by SmartThings
 *  Date: 2017-11-12
 */
metadata {
    definition (name: "Zen Thermostat", namespace: "zenwithin", author: "ZenWithin") {
        capability "Actuator"
        capability "Battery"
        capability "Health Check"
        capability "Refresh"
        capability "Sensor"
        capability "Temperature Measurement"
        capability "Thermostat"
        capability "Thermostat Heating Setpoint"
        capability "Thermostat Cooling Setpoint"
        capability "Thermostat Operating State"
        capability "Thermostat Mode"
        capability "Thermostat Fan Mode"

        command "setpointUp"
        command "setpointDown"
        command "switchMode"
        command "switchFanMode"
        // To please some of the thermostat SmartApps
        command "poll"

        fingerprint profileId: "0104", endpointId: "01", inClusters: "0000,0001,0003,0004,0005,0020,0201,0202,0204,0B05", outClusters: "000A, 0019", manufacturer: "Zen Within", model: "Zen-01", deviceJoinName: "Zen Thermostat"
    }

    tiles {
        multiAttributeTile(name:"temperature", type:"generic", width:3, height:2, canChangeIcon: true) {
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
            tileAttribute("device.batteryIcon", key: "SECONDARY_CONTROL") { // change to batteryIcon
                attributeState "ok_battery", label:'${currentValue}%', icon:"st.arlo.sensor_battery_4"
                attributeState "low_battery", label:'Low Battery', icon:"st.arlo.sensor_battery_0"
                attributeState "err_battery", label:'Battery Error', icon:"st.arlo.sensor_battery_0"
            }
            tileAttribute("device.thermostatSetpoint", key: "VALUE_CONTROL") {
                attributeState "VALUE_UP", action: "setpointUp"
                attributeState "VALUE_DOWN", action: "setpointDown"
            }
        }
        // mode changes "off" -> "auto" -> "heat" -> "emergency heat" -> "cool" -> "off"
        standardTile("mode", "device.thermostatMode", width:2, height:2, inactiveLabel: false, decoration: "flat") {
            state "off", action:"switchMode", nextState:"updating", icon: "st.thermostat.heating-cooling-off"
            state "auto", action:"switchMode", nextState:"updating", icon: "st.thermostat.auto"
            state "heat", action:"switchMode", nextState:"updating", icon: "st.thermostat.heat"
            state "emergency heat", action:"switchMode", nextState:"updating", icon: "st.thermostat.emergency-heat"
            state "cool", action:"switchMode", nextState:"updating", icon: "st.thermostat.cool"
            state "updating", label: "Updating...",nextState:"updating", backgroundColor:"#ffffff"
        }
        standardTile("fanMode", "device.thermostatFanMode", width:2, height:2, inactiveLabel: false, decoration: "flat") {
            state "speed", label:'${currentValue}', action:"switchFanMode", nextState:"updating", icon: "st.thermostat.fan-on"
            state "auto", action:"switchFanMode", nextState:"updating", icon: "st.thermostat.fan-auto"
            state "on", action:"switchFanMode", nextState:"updating", icon: "st.thermostat.fan-on"
            state "updating", label: "Updating...", nextState:"updating", backgroundColor:"#ffffff"
        }
        standardTile("thermostatOperatingState", "device.thermostatOperatingState", width: 2, height:2, decoration: "flat") {
            state "thermostatOperatingState", label:'${currentValue}', backgroundColor:"#ffffff"
        }
        standardTile("refresh", "device.thermostatMode", width:2, height:2, inactiveLabel: false, decoration: "flat") {
            state "default", action:"refresh.refresh", icon:"st.secondary.refresh"
        }
        main "temperature"
        details(["temperature", "mode", "fanMode", "thermostatOperatingState", "refresh"])
    }
    preferences {
        section {
            input("systemModes", "enum",
                title: "Thermostat configured modes\nSelect the modes the thermostat has been configured for, as displayed on the thermostat",
                description: "off, heat, cool", defaultValue: "3", required: true, multiple: false,
                options:["1":"off, heat",
                        "2":"off, cool",
                        "3":"off, heat, cool",
                        "4":"off, auto, heat, cool",
                        "5":"off, emergency heat, heat, cool"]
            )
        }
    }
}

// Globals
private getTHERMOSTAT_CLUSTER()                      { 0x0201 }
private getATTRIBUTE_LOCAL_TEMPERATURE()             { 0x0000 }
private getATTRIBUTE_OCCUPIED_COOLING_SETPOINT()     { 0x0011 }
private getATTRIBUTE_OCCUPIED_HEATING_SETPOINT()     { 0x0012 }
private getATTRIBUTE_MIN_HEAT_SETPOINT_LIMIT()       { 0x0015 }
private getATTRIBUTE_MAX_HEAT_SETPOINT_LIMIT()       { 0x0016 }
private getATTRIBUTE_MIN_COOL_SETPOINT_LIMIT()       { 0x0017 }
private getATTRIBUTE_MAX_COOL_SETPOINT_LIMIT()       { 0x0018 }
private getATTRIBUTE_MIN_SETPOINT_DEAD_BAND()        { 0x0019 }
private getATTRIBUTE_CONTROL_SEQUENCE_OF_OPERATION() { 0x001b }
private getATTRIBUTE_SYSTEM_MODE()                   { 0x001c }
private getATTRIBUTE_THERMOSTAT_RUNNING_MODE()       { 0x001e }
private getATTRIBUTE_THERMOSTAT_RUNNING_STATE()      { 0x0029 }

private getFAN_CONTROL_CLUSTER()                     { 0x0202 }
private getATTRIBUTE_FAN_MODE()                      { 0x0000 }
private getATTRIBUTE_FAN_MODE_SEQUENCE()             { 0x0001 }

private getATTRIBUTE_BATTERY_VOLTAGE()               { 0x0020 }

private getTypeINT16() { 0x29 }
private getTypeENUM8() { 0x30 }

def getSupportedModes() {
    return (settings.systemModes ? supportedModesMap[settings.systemModes] : ["off", "heat", "cool"])
}

def getSupportedModesMap() {
    [
        "1":["off", "heat"],
        "2":["off", "cool"],
        "3":["off", "heat", "cool"],
        "4":["off", "auto", "heat", "cool"],
        "5":["off", "emergency heat", "heat", "cool"]
    ]
}

def installed() {
    log.debug "installed"
    // set default supportedModes as device doesn't report according to its configuration
    sendEvent(name: "supportedThermostatModes", value: ["off", "heat", "cool"], eventType: "ENTITY_UPDATE", displayed: false)
    // Pairing can be 'silent' meaning the mobile app will not call updated() so initialize() needs also to be called by installed()
    // make sure configuration and initial poll is done by the DTH, but to try avoiding multiple config/poll be done us runIn ;o(
    runIn(3, "initialize", [overwrite: true])  // Allow configure command to be sent and acknowledged before proceeding
    initialize()
}

def updated() {
    log.debug "updated"
    // make sure supporedModes are in sync
    sendEvent(name: "supportedThermostatModes", value: supportedModes, eventType: "ENTITY_UPDATE", displayed: false)
    // Make sure we poll all attributes from the device
    state.pollAdditionalData = state.pollAdditionalData ? state.pollAdditionalData - (24 * 60 * 60 * 1000) : null
    // initialize() needs to be called after device details has been updated() but as installed() also calls this method and
    // that LiveLogging shows updated is being called more than one time, try avoiding multiple config/poll be done us runIn ;o(
    runIn(3, "initialize", [overwrite: true])
}

def initialize() {
    log.debug "initialize() - binding & attribute report"
    sendEvent(name: "checkInterval", value: 60 * 12, displayed: false, data: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID])
    // send configure commad to the thermostat
    def cmds = [
            //Set long poll interval to 2 qs
            "raw 0x0020 {11 00 02 02 00 00 00}", 
            "send 0x${device.deviceNetworkId} 1 1",
            //Thermostat - Cluster 201
            "zdo bind 0x${device.deviceNetworkId} 1 1 0x201 {${device.zigbeeId}} {}",
            "zcl global send-me-a-report 0x201 0 0x29 5 300 {3200}",      // report temperature changes over 0.5°C (0x3200 in little endian)
            "send 0x${device.deviceNetworkId} 1 1",
            "zcl global send-me-a-report 0x201 0x0011 0x29 5 300 {3200}", // report cooling setpoint delta: 0.5°C
            "send 0x${device.deviceNetworkId} 1 1",
            "zcl global send-me-a-report 0x201 0x0012 0x29 5 300 {3200}", // report heating setpoint delta: 0.5°C
            "send 0x${device.deviceNetworkId} 1 1",
            "zcl global send-me-a-report 0x201 0x001C 0x30 5 300 {}",     // report system mode
            "send 0x${device.deviceNetworkId} 1 1",
            "zcl global send-me-a-report 0x201 0x0029 0x19 5 300 {}",     // report running state
            "send 0x${device.deviceNetworkId} 1 1",
            //Fan Control - Cluster 202
            "zdo bind 0x${device.deviceNetworkId} 1 1 0x202 {${device.zigbeeId}} {}",
            "zcl global send-me-a-report 0x202 0 0x30 5 300 {}",          // report fan mode
            "send 0x${device.deviceNetworkId} 1 1",
        ]
    //Power Control - Cluster 0001 (report battery status)
    cmds += zigbee.batteryConfig()
    sendZigbeeCmds(cmds, 500)
    // Delay polling device attribute until the config is done
    runIn(15, "pollDevice", [overwrite: true])
}

// parse events into attributes
def parse(String description) {
    def map = [:]

    def descMap = zigbee.parseDescriptionAsMap(description)
    // Thermostat Cluster Attribute Read Response
    if (descMap.cluster == "0201") { // THERMOSTAT_CLUSTER
        def locationScale = getTemperatureScale()
        def mode = device.currentValue("thermostatMode")
        switch (descMap.attrId) {
            case "0000": // ATTRIBUTE_LOCAL_TEMPERATURE
                map.name = "temperature"
                map.unit = locationScale
                map.value = getTempInLocalScale(parseTemperature(descMap.value), "C") // Zibee always reports in °C
                break
            case "0011": // ATTRIBUTE_OCCUPIED_COOLING_SETPOINT
                state.deviceCoolingSetpoint = parseTemperature(descMap.value)
                map.name = "coolingSetpoint"
                map.unit = locationScale
                map.value = getTempInLocalScale(state.deviceCoolingSetpoint, "C") // Zibee always reports in °C
                runIn(5, "updateThermostatSetpoint", [overwrite: true])
                break
            case "0012": // ATTRIBUTE_OCCUPIED_HEATING_SETPOINT
                state.deviceHeatingSetpoint = parseTemperature(descMap.value)
                map.name = "heatingSetpoint"
                map.unit = locationScale
                map.value = getTempInLocalScale(state.deviceHeatingSetpoint, "C") // Zibee always reports in °C
                runIn(5, "updateThermostatSetpoint", [overwrite: true])
                break
                case "0015": // ATTRIBUTE_MIN_HEAT_SETPOINT_LIMIT
                updateMinSetpointLimit("minHeatSetpointCelsius", descMap.value)
                break
            case "0016": // ATTRIBUTE_MAX_HEAT_SETPOINT_LIMIT
                updateMaxSetpointLimit("maxHeatSetpointCelsius", descMap.value)
                break
            case "0017": // ATTRIBUTE_MIN_COOL_SETPOINT_LIMIT
                updateMinSetpointLimit("minCoolSetpointCelsius", descMap.value)
                break
            case "0018": // ATTRIBUTE_MAX_COOL_SETPOINT_LIMIT
                updateMaxSetpointLimit("maxCoolSetpointCelsius", descMap.value)
                break
/* Zen thermostat used when implenting this DTH always retured a value of 10, 1 degree deadband,
   however this is only valid at the end of the setpoints ranges, otherwise it has 4 degrees deadband
   between heating/cooling setpoint in auto mode. There also doen't seem to be a way for a user to change this
   Leaving this code in place for history/tracking purposes.
            case "0019": // ATTRIBUTE_MIN_SETPOINT_DEAD_BAND
                def deadBand = Integer.parseInt(descMap.value, 16)
                if (deadBand < 26 && deadBand > 9) {
                    device.updateDataValue("deadBandCelsius", "${deadBand / 10}")
                }
                break
*/
/* Zen thermostat used when implenting this DTH always retured a value of 0x04 "All modes are possible",
   regardless of configuration on the thermostat thus instead of polling this DTH will use user configurable settings instead.
   Leaving this code in place for history/tracking purposes.
                case "001b": // ATTRIBUTE_CONTROL_SEQUENCE_OF_OPERATION
                    map.name = "supportedThermostatModes"
                    map.displayed = false
                    map.value = controlSequenceOfOperationMap[descMap.value]
                    state.supportedModes = map.value
                    break
*/
            case "001c": // ATTRIBUTE_SYSTEM_MODE
                // Make sure operating state is in sync
                map.name = "thermostatMode"
                if (state.switchMode) {
                    // set isStateChange to true to force update if switchMode failed and old mode is returned
                    map.isStateChange = true
                    state.switchMode = false
                }
                map.data = [supportedThermostatModes: supportedModes]
                map.value = systemModeMap[descMap.value]
                // in case of refresh, allow heat/cool setpoints to be reported before updating setpoint
                runIn(10, "updateThermostatSetpoint", [overwrite: true])
                // Make sure operating state is in sync
                ping()
                break
            case "001e": // ATTRIBUTE_THERMOSTAT_RUNNING_MODE
                device.updateDataValue("thermostatRunningMode", systemModeMap[descMap.value])
                break
            case "0029": // ATTRIBUTE_THERMOSTAT_RUNNING_STATE
                map.name = "thermostatOperatingState"
                map.value = thermostatRunningStateMap[descMap.value]
                break
         } // switch (descMap.attrId)
    } // THERMOSTAT_CLUSTER
    // Fan Control Cluster Attribute Read Response
    else if (descMap.cluster == "0202") {
        switch (descMap.attrId) {
            case "0000": // ATTRIBUTE_FAN_MODE
                // Make sure operating state is in sync
                ping()
                map.name = "thermostatFanMode"
                if (state.switchFanMode) {
                    // set isStateChange to true to force update if switchMode failed and old mode is returned
                    map.isStateChange = true
                    state.switchFanMode = false
                }
                map.data = [supportedThermostatFanModes: state.supportedFanModes]
                map.value = fanModeMap[descMap.value]
                break
            case "0001": // ATTRIBUTE_FAN_MODE_SEQUENCE
                map.name = "supportedThermostatFanModes"
                map.value = fanModeSequenceMap[descMap.value]
                state.supportedFanModes = map.value
                break
        }
    } // Fan Control Cluster
    // Power Configuration Cluster
    else if (descMap.cluster == "0001") {
        if (descMap.attrId == "0020") {
            updateBatteryStatus(descMap.value)
        }
    }
    def result = null
    if (map) {
      result = createEvent(map)
    }
    return result
}

// =============== Help Functions - Don't use log.debug in all these functins ===============
/* Zen thermostat used when implenting this DTH always retured a value of 0x04 "All modes are possible"
   making this useless, leaving it for history/tracking purpose...
def getControlSequenceOfOperationMap() {
    [
        "00":["off", "cool"],
        "01":["off", "cool"],
        "02":["off", "heat"],
        "03":["off", "heat"],
        "04":["off", "auto", "heat", "cool", "emergency heat"],
        "05":["off", "auto", "heat", "cool"], // Zigbee says "All modes are possible" but for now disable "emergency heat"]
    ]
}
*/

def getSystemModeMap() {
    [
        "00":"off",
        "01":"auto",
        "03":"cool",
        "04":"heat",
        "05":"emergency heat",
        "06":"precooling",
        "07":"fan only",
        "08":"dry",
        "09":"sleep"
    ]
}

def getThermostatRunningStateMap() {
    /**  Bit Number
    //  0 Heat State
    //  1 Cool State
    //  2 Fan State
    //  3 Heat 2nd Stage State
    //  4 Cool 2nd Stage State
    //  5 Fan 2nd Stage State
    //  6 Fan 3rd Stage Stage
    **/
    [
        "0000":"idle",
        "0001":"heating",
        "0002":"cooling",
        "0004":"fan only",
        "0005":"heating",
        "0006":"cooling",
        "0008":"heating",
        "0009":"heating",
        "000A":"heating",
        "000D":"heating",
        "0010":"cooling",
        "0012":"cooling",
        "0014":"cooling",
        "0015":"cooling"
    ]
}

def getFanModeSequenceMap() {
    [
        "00":["low", "medium", "high"],
        "01":["low", "high"],
        "02":["low", "medium", "high", "auto"],
        "03":["low", "high", "auto"],
        "04":["on", "auto"],
    ]
}

def getFanModeMap() {
    [
        "00":"off",
        "01":"low",
        "02":"medium",
        "03":"high",
        "04":"on",
        "05":"auto",
        "00":"smart"
    ]
}

def updateMinSetpointLimit(setpoint, rawValue) {
    def min = parseTemperature(rawValue)
    if (min) {
        // Make sure min is an even number of step value (0.5°C/1°F) to nearest upper
        min = (((long)min - min) < -0.5) ? Math.ceil(min): Math.floor(min) + 0.5*(Math.ceil(min - (long)min))
        device.updateDataValue(setpoint, "${min}")
    } else {
        log.warn "received invalid min value for $setpoint ($rawValue)"
    }
}

def updateMaxSetpointLimit(setpoint, rawValue) {
    def max = parseTemperature(rawValue)
    if (max) {
        // Make sure max is an even number if step value (0.5°C/1°F) to nearest lower
        max = ((max - (long)max) < 0.5) ? Math.floor(max) : Math.floor(max) + 0.5
        device.updateDataValue(setpoint, "${max}")
    } else {
        log.warn "received invalid max value for $setpoint ($rawValue)"
    }
}

def updateBatteryStatus(rawValue) {
    if (rawValue && rawValue.matches("-?[0-9a-fA-F]+")) {
        def volts = zigbee.convertHexToInt(rawValue)
        // customAttribute in order to change UI icon/label
        def eventMap = [name: "batteryIcon", value: "err_battery", displayed: false]
        def linkText = getLinkText(device)
        if (volts != 255) {
            def minVolts = 34  // voltage when device UI starts to die, ie. when battery fails
            def maxVolts = 60  // 4 batteries at 1.5V (6.0V)
            def pct = (volts > minVolts) ? ((volts - minVolts) / (maxVolts - minVolts)) : 0
            eventMap.value = Math.min(100, (int)(pct * 100))
            // Update capability "Battery"
            sendEvent(name: "battery", value: eventMap.value, descriptionText: "${getLinkText(device)} battery was ${eventMap.value}%")
            eventMap.value = eventMap.value > 15 ? eventMap.value : "low_battery"
        }
        sendEvent(eventMap)
    } else {
        log.warn "received invalid battery value ($rawValue)"
    }
}

def updateThermostatSetpoint() {
    // Do calculation in device scale to avoid rounding errors when converting between °C->°F
    def scale = getTemperatureScale()
    def heatingSetpoint = state.deviceHeatingSetpoint ?:
            ((scale == "F") ? fahrenheitToCelsius(getTempInLocalScale("heatingSetpoint")) : getTempInLocalScale("heatingSetpoint"))
    def coolingSetpoint = state.deviceCoolingSetpoint ?:
             ((scale == "F") ? fahrenheitToCelsius(getTempInLocalScale("coolingSetpoint")) : getTempInLocalScale("coolingSetpoint"))
    def mode = device.currentValue("thermostatMode")
    state.deviceHeatingSetpoint = null
    state.deviceCoolingSetpoint = null
    def thermostatSetpoint = heatingSetpoint   // corresponds to (mode == "heat" || mode == "emergency heat")
    if (mode == "cool") {
        thermostatSetpoint = coolingSetpoint
    } else if (mode == "auto" || mode == "off") {
        // Set thermostatSetpoint to the average of both setpoints as that's what the device UI shows
        thermostatSetpoint = (heatingSetpoint + coolingSetpoint)/2
    }
    sendEvent(name: "thermostatSetpoint", value: getTempInLocalScale(thermostatSetpoint, "C"), unit: scale)
}

// parse zigbee temperaure value to °C
def parseTemperature(String value) {
    def temperature = null
    if (value && value.matches("-?[0-9a-fA-F]+") && value != "8000") {
        temperature = Integer.parseInt(value, 16)
        if (temperature > 32767) {
            temperature -= 65536
        }
        temperature = temperature / 100.0 as Double
    } else {
        log.warn "received no or invalid temperature"
    }
    return temperature
}

// Get stored temperature from currentState in current local scale
def getTempInLocalScale(state) {
    def temperature = device.currentState(state)
    if (temperature && temperature.value && temperature.unit) {
        return getTempInLocalScale(temperature.value.toBigDecimal(), temperature.unit)
    }
    return 0
}

// get/convert temperature to current local scale
def getTempInLocalScale(temperature, scale) {
    if (temperature && scale) {
        def scaledTemp = convertTemperatureIfNeeded(temperature.toBigDecimal(), scale).toDouble()
        return (getTemperatureScale() == "F" ? scaledTemp.round(0).toInteger() : roundC(scaledTemp))
    }
    return null
}

def getTempInDeviceScale(temp, scale) {
    if (temp && scale) {
        def deviceScale = (state.scale == 1) ? "F" : "C"
        return (deviceScale == scale) ? temp :
            (deviceScale == "F" ? celsiusToFahrenheit(temp).toDouble().round(0).toInteger() : roundC(fahrenheitToCelsius(temp)))
    }
    return 0
}

// Round to nearest X.0 or X.5
def roundC (tempC) {
    return (Math.round(tempC.toDouble() * 2))/2
}

// =============== Setpoints ===============
def setpointUp() {
    alterSetpoint(true)
}
def setpointDown() {
    alterSetpoint(false)
}

// Adjusts nextHeatingSetpoint either .5° C/1° F) if raise true/false
def alterSetpoint(raise, targetValue = null, setpoint = null) {
    def locationScale = getTemperatureScale()
    def deviceScale = "C"  // Zigbee is always reporting in °C
    def currentMode = device.currentValue("thermostatMode")
    def delta = (locationScale == "F") ? 1 : 0.5
    def heatingSetpoint = null
    def coolingSetpoint = null

    targetValue = targetValue ?: (getTempInLocalScale("thermostatSetpoint") + (raise ? delta : - delta))
    targetValue = getTempInDeviceScale(targetValue, locationScale)
    switch (currentMode) {
        case "auto":
            def minSetpoint = device.getDataValue("minHeatSetpointCelsius")
            def maxSetpoint = device.getDataValue("maxCoolSetpointCelsius")
            minSetpoint = minSetpoint ? Double.parseDouble(minSetpoint) : 4.0    // default 4.0
            maxSetpoint = maxSetpoint ? Double.parseDouble(maxSetpoint) : 37.5   // default 37.0
            // Set both heating and cooling setpoint, 4 degrees appart (possibly user configurable)
            // thermostatSetpoint is the average of heating/cooling
            targetValue = enforceSetpointLimit(targetValue, "minHeatSetpointCelsius", "maxCoolSetpointCelsius")
            heatingSetpoint = targetValue - 2
            coolingSetpoint = targetValue + 2
            if (heatingSetpoint < minSetpoint) {
                coolingSetpoint = coolingSetpoint - (minSetpoint - heatingSetpoint)
                heatingSetpoint = minSetpoint
                targetValue = (heatingSetpoint + coolingSetpoint) / 2
            }
            if (coolingSetpoint > maxSetpoint) {
                heatingSetpoint = (coolingSetpoint < maxSetpoint + 2) ? heatingSetpoint + (coolingSetpoint - maxSetpoint) : maxSetpoint - 0.5
                coolingSetpoint = maxSetpoint
                targetValue = (heatingSetpoint + coolingSetpoint) / 2
            }
            break
        case "heat":  // No Break
        case "emergency heat":
            heatingSetpoint = enforceSetpointLimit(targetValue, "minHeatSetpointCelsius", "maxHeatSetpointCelsius")
            break
        case "cool":
            // set coolingSetpoint to thermostatSetpoint
            coolingSetpoint = enforceSetpointLimit(targetValue, "minCoolSetpointCelsius", "maxCoolSetpointCelsius")
            break
         case "off":  // No Break
            // Do nothing, don't allow change of setpoints in off mode
        default:
            targetValue = null
            break
    }
    if (targetValue) {
        sendEvent(name: "thermostatSetpoint", value: getTempInLocalScale(targetValue, deviceScale),
                unit: locationScale, eventType: "ENTITY_UPDATE")//, displayed: false)
        def data = [targetHeatingSetpoint:heatingSetpoint, targetCoolingSetpoint:coolingSetpoint]
        // Use runIn to reduce chances UI is toggling the value
        runIn(3, "updateSetpoints", [data: data, overwrite: true])
    }
}

def enforceSetpointLimit(target, min, max) {
    def minSetpoint = device.getDataValue(min)
    def maxSetpoint = device.getDataValue(max)
    minSetpoint = minSetpoint ? Double.parseDouble(minSetpoint) : 4.0    // default 4.0
    maxSetpoint = maxSetpoint ? Double.parseDouble(maxSetpoint) : 37.5   // default 37.0
    // Enforce setpoint limits
    if (target < minSetpoint) {
        target = minSetpoint
    } else if (target > maxSetpoint) {
        target = maxSetpoint
    }
    return target
}

def setHeatingSetpoint(degrees) {
    def currentMode = device.currentValue("thermostatMode")
    if (degrees && (currentMode != "cool") && (currentMode != "off")) {
        state.heatingSetpoint = degrees.toDouble()
        // Use runIn to enable both setpoints to be changed if a routine/SA changes heating/cooling setpoint at the same time
        runIn(2, "updateSetpoints", [overwrite: true])
    }
}

def setCoolingSetpoint(degrees) {
    def currentMode = device.currentValue("thermostatMode")
    if (degrees && (currentMode == "cool" || currentMode == "auto")) {
        state.coolingSetpoint = degrees.toDouble()
        // Use runIn to enable both setpoints to be changed if a routine/SA changes heating/cooling setpoint at the same time
        runIn(2, "updateSetpoints", [overwrite: true])
    }
}

def updateSetpoints() {
    def deviceScale = "C"
    def data = [targetHeatingSetpoint: null, targetCoolingSetpoint: null]
    def targetValue = state.heatingSetpoint
    def setpoint = "heatingSetpoint"
    if (state.heatingSetpoint && state.coolingSetpoint) {
        setpoint = null
        targetValue = (state.heatingSetpoint + state.coolingSetpoint) / 2
    } else if (state.coolingSetpoint) {
        setpoint == "coolingSetpoint"
        targetValue = state.coolingSetpoint
    }
    state.heatingSetpoint = null
    state.coolingSetpoint = null
    alterSetpoint(null, targetValue, setpoint)
}

def updateSetpoints(data) {
    def cmds = []
    if (data.targetHeatingSetpoint) {
        sendEvent(name: "heatingSetpoint", value: getTempInLocalScale(data.targetHeatingSetpoint, "C"), unit: getTemperatureScale())
        cmds += zigbee.writeAttribute(THERMOSTAT_CLUSTER, ATTRIBUTE_OCCUPIED_HEATING_SETPOINT, typeINT16,
                hexString(Math.round(data.targetHeatingSetpoint*100.0), 4))
    }
    if (data.targetCoolingSetpoint) {
        sendEvent(name: "coolingSetpoint", value: getTempInLocalScale(data.targetCoolingSetpoint, "C"), unit: getTemperatureScale())
        cmds += zigbee.writeAttribute(THERMOSTAT_CLUSTER, ATTRIBUTE_OCCUPIED_COOLING_SETPOINT, typeINT16,
                hexString(Math.round(data.targetCoolingSetpoint*100.0), 4))
    }
    sendZigbeeCmds(cmds, 1000)
}

// =============== Thermostat Mode ===============
def switchMode() {
    def currentMode = device.currentValue("thermostatMode")
    def supportedModes = supportedModes
    if (supportedModes) {
        def next = { supportedModes[supportedModes.indexOf(it) + 1] ?: supportedModes[0] }
        switchToMode(next(currentMode))
    } else {
        log.err "supportedModes not defined"
    }
}

def switchToMode(nextMode) {
    def supportedModes = supportedModes
    if (supportedModes) {
        if (supportedModes.contains(nextMode)) {
            def cmds = []
            def setpoint = getTempInLocalScale("thermostatSetpoint")
            def heatingSetpoint = null
            def coolingSetpoint = null
            switch (nextMode) {
                case "heat": // No break
                case "emergency heat":
                    heatingSetpoint = setpoint
                    break
                case "cool":
                    coolingSetpoint = setpoint
                    break
                case "off":  // No break
                case "auto": // No break
                default:
                    def currentMode = device.currentValue("thermostatMode")
                    if (currentMode != "off" && currentMode != "auto") {
                        heatingSetpoint = setpoint - 2  // In auto/off keep heating/cooling setpoint 4° appart (customizable?)
                        coolingSetpoint = setpoint + 2
                    }
                    break
            }
            if (heatingSetpoint) {
                cmds += zigbee.writeAttribute(THERMOSTAT_CLUSTER, ATTRIBUTE_OCCUPIED_HEATING_SETPOINT, typeINT16,
                        hexString(Math.round(heatingSetpoint*100.0), 4))
            }
            if (coolingSetpoint) {
                cmds += zigbee.writeAttribute(THERMOSTAT_CLUSTER, ATTRIBUTE_OCCUPIED_COOLING_SETPOINT, typeINT16,
                        hexString(Math.round(coolingSetpoint*100.0), 4))
            }
            def mode = Integer.parseInt(systemModeMap.find { it.value == nextMode }?.key, 16)
            cmds += zigbee.writeAttribute(THERMOSTAT_CLUSTER, ATTRIBUTE_SYSTEM_MODE, typeENUM8, mode)
            sendZigbeeCmds(cmds)
            state.switchMode = true
        } else {
            log.debug("ThermostatMode $nextMode is not supported by ${device.displayName}")
        }
    } else {
        log.err "supportedModes not defined"
    }
}

def setThermostatMode(String value) {
    switchToMode(value?.toLowerCase())
}

def off() {
    switchToMode("off")
}

def cool() {
    switchToMode("cool")
}

def heat() {
    switchToMode("heat")
}

def auto() {
    switchToMode("auto")
}

// =============== Fan Mode ===============
def switchFanMode() {
    def currentMode = device.currentValue("thermostatFanMode")
    def supportedFanModes = state.supportedFanModes
    if (supportedFanModes) {
        def next = { supportedFanModes[supportedFanModes.indexOf(it) + 1] ?: supportedFanModes[0] }
        switchToFanMode(next(currentMode))
    } else {
        log.warn "supportedFanModes not defined"
        getSupportedFanModes()
    }
}

def switchToFanMode(nextMode) {
    def supportedFanModes = state.supportedFanModes
    if (supportedFanModes) {
        if (supportedFanModes.contains(nextMode)) {
            def mode = fanModeMap.find { it.value == nextMode }?.key
            def cmds = zigbee.writeAttribute(FAN_CONTROL_CLUSTER, ATTRIBUTE_FAN_MODE, typeENUM8, mode)
            sendZigbeeCmds(cmds)
            state.switchFanMode = true
        } else {
            log.debug("FanMode $nextMode is not supported by ${device.displayName}")
        }
    } else {
        log.warn "supportedFanModes not defined"
        getSupportedFanModes()
    }
}

def getSupportedFanModes() {
    def cmds = zigbee.readAttribute(FAN_CONTROL_CLUSTER, ATTRIBUTE_FAN_MODE_SEQUENCE)
    sendZigbeeCmds(cmds)
}

def setThermostatFanMode(String value) {
    switchToFanMode(value?.toLowerCase())
}

def fanOn() {
    switchToFanMode("on")
}

def fanAuto() {
    switchToFanMode("auto")
}

/**
 * PING is used by Device-Watch in attempt to reach the Device
 * */
def ping() {
    // No need to send a bunch of cmd, one is enough
    def cmds = zigbee.readAttribute(THERMOSTAT_CLUSTER, ATTRIBUTE_THERMOSTAT_RUNNING_STATE)
    sendZigbeeCmds(cmds)
}

def refresh() {
    // Only allow refresh every 2 minutes to prevent flooding the Zwave network
    def timeNow = now()
    if (!state.refreshTriggeredAt || (2 * 60 * 1000 < (timeNow - state.refreshTriggeredAt))) {
        state.refreshTriggeredAt = timeNow
        // use runIn with overwrite to prevent multiple DTH instances run before state.refreshTriggeredAt has been saved
        runIn(2, "pollDevice", [overwrite: true])
    }
}

def pollDevice() {
    log.debug "pollDevice() - update attributes"
    // First update supported modes, min/max setpoint ranges and deadband, this is normally only needed at install/updated
    def cmds = pollAdditionalData()
    // When supported modes are known we can update current modes
    cmds += zigbee.readAttribute(THERMOSTAT_CLUSTER, ATTRIBUTE_SYSTEM_MODE)  // Current operating mode
    cmds += zigbee.readAttribute(THERMOSTAT_CLUSTER, ATTRIBUTE_THERMOSTAT_RUNNING_MODE)  // The running thermostat mode
    // ATTRIBUTE_THERMOSTAT_RUNNING_STATE will be updated by response of ATTRIBUTE_SYSTEM_MODE and ATTRIBUTE_FAN_MODE
    // cmds += zigbee.readAttribute(THERMOSTAT_CLUSTER, ATTRIBUTE_THERMOSTAT_RUNNING_STATE) // The current relay state of the heat, cool, and fan relays
    cmds += zigbee.readAttribute(FAN_CONTROL_CLUSTER, ATTRIBUTE_FAN_MODE)    // The current fan mode
    // When system mode is known we can update current temperature and setpoints
    cmds += zigbee.readAttribute(THERMOSTAT_CLUSTER, ATTRIBUTE_LOCAL_TEMPERATURE)
    cmds += zigbee.readAttribute(THERMOSTAT_CLUSTER, ATTRIBUTE_OCCUPIED_HEATING_SETPOINT)
    cmds += zigbee.readAttribute(THERMOSTAT_CLUSTER, ATTRIBUTE_OCCUPIED_COOLING_SETPOINT)
    // Also update the current battery status
    cmds += zigbee.readAttribute(zigbee.POWER_CONFIGURATION_CLUSTER, ATTRIBUTE_BATTERY_VOLTAGE)
    sendZigbeeCmds(cmds)
}

def pollAdditionalData() {
    def cmds = []
    def timeNow = new Date().time
    if (!state.pollAdditionalData || (24 * 60 * 60 * 1000 < (timeNow - state.pollAdditionalData))) {
        state.pollAdditionalData = timeNow
        // Skip ATTRIBUTE_CONTROL_SEQUENCE_OF_OPERATION as it always reports the same regardless of thermostat configuration
        // cmds += zigbee.readAttribute(THERMOSTAT_CLUSTER, ATTRIBUTE_CONTROL_SEQUENCE_OF_OPERATION)
        cmds += zigbee.readAttribute(FAN_CONTROL_CLUSTER, ATTRIBUTE_FAN_MODE_SEQUENCE)
        cmds += zigbee.readAttribute(THERMOSTAT_CLUSTER, ATTRIBUTE_MIN_HEAT_SETPOINT_LIMIT)
        cmds += zigbee.readAttribute(THERMOSTAT_CLUSTER, ATTRIBUTE_MAX_HEAT_SETPOINT_LIMIT)
        cmds += zigbee.readAttribute(THERMOSTAT_CLUSTER, ATTRIBUTE_MIN_COOL_SETPOINT_LIMIT)
        cmds += zigbee.readAttribute(THERMOSTAT_CLUSTER, ATTRIBUTE_MAX_COOL_SETPOINT_LIMIT)
        // Skip ATTRIBUTE_MIN_SETPOINT_DEAD_BAND as it isn't really used in auto mode
        // cmds += zigbee.readAttribute(THERMOSTAT_CLUSTER, ATTRIBUTE_MIN_SETPOINT_DEAD_BAND)
    }

    return cmds
}

def sendZigbeeCmds(cmds, delay = 2000) {
    // remove zigbee library added "delay 2000" after each command
    // the new sendHubCommand won't honor these, instead it'll take the delay as argument
    cmds.removeAll { it.startsWith("delay") }
    // convert each command into a HubAction
    cmds = cmds.collect { new physicalgraph.device.HubAction(it) }
    sendHubCommand(cmds, delay)
}

def poll() {
    refresh()
}

