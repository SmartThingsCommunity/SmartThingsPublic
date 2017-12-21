// Notes:
// - Auto mode not currently supported

/**
*  Nest FT Thermostat
*
*  CHANGELOG:
*  v1.0 - Original release
*
*  Original Authors: dianoga7@3dgo.net, desertblade@gmail.com, Anthony S. (tonesto7)
*  Code: https://github.com/smartthings-users/device-type.nest
*
* INSTALL:
* 1) Create a new device (https://graph.api.smartthings.com/device/list)
*     Name: Your Choice
*     Device Network Id: Your Choice
*     Type: Nest (should be the last option)
*     Location: Choose the correct location
*     Hub/Group: Leave blank
*
* 2) Update device preferences
*     Click on the new device to see the details.
*     Click the edit button next to Preferences
*     Fill in your information.
*     To find your serial number, login to http://home.nest.com. Click on the thermostat
*     you want to control. Under settings, go to Technical Info. Your serial number is
*     the second item.
*
* Copyright (C) 2013 Brian Steere <dianoga7@3dgo.net>
* Permission is hereby granted, free of charge, to any person obtaining a copy of this
* software and associated documentation files (the "Software"), to deal in the Software
* without restriction, including without limitation the rights to use, copy, modify,
* merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
* permit persons to whom the Software is furnished to do so, subject to the following
* conditions: The above copyright notice and this permission notice shall be included
* in all copies or substantial portions of the Software.
*
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
* INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
* PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
* HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
* CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE
* OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/

preferences {
    input("username", "text", title: "Username", description: "Your Nest username (usually an email address)")
    input("password", "password", title: "Password", description: "Your Nest password")
    input("serial", "text", title: "Serial #", description: "The serial number of your thermostat")
}

// for the UI
metadata {
    definition (name: "Nest Thermostat", namespace: "1A2B3C4Z", author: "Anthony Pluth") {
        capability "Polling"
        capability "Refresh"
        capability "Relative Humidity Measurement"
        capability "Thermostat"
        capability "Temperature Measurement"
        capability "Presence Sensor"
        capability "Sensor"
        capability "Actuator"
        capability "Signal Strength"

        command "away"
        command "present"
        command "setNestPresence"
        command "heatingSetpointUp"
        command "heatingSetpointDown"
        command "coolingSetpointUp"
        command "coolingSetpointDown"
        command "setFahrenheit"
        command "setCelsius"
        command "setTemperature"

        attribute "temperatureUnit", "string"
    }

    simulator {
        // TODO: define status and reply messages here
    }
    tiles(scale: 2) {
        multiAttributeTile(name:"temperature", type:"thermostat", width:6, height:4) {
            tileAttribute("device.temperature", key: "PRIMARY_CONTROL") {
                attributeState("default", label:'${currentValue}°')
            }
            //tileAttribute("device.temperature", key: "VALUE_CONTROL") {
            tileAttribute("device.targetTemperature", key: "VALUE_CONTROL") {
                attributeState("default", action: "setTemperature", unit:"°")
            }
            tileAttribute("device.humidity", key: "SECONDARY_CONTROL") {
                attributeState("default", label:'${currentValue}%', unit:"%")
            }
            tileAttribute("device.thermostatOperatingState", key: "OPERATING_STATE") {
                attributeState("idle", action:"polling.poll", backgroundColor: "#00A0DC")
                attributeState("heating", action:"polling.poll", backgroundColor: "#E86D13")
                attributeState("cooling", action:"polling.poll", backgroundColor: "#00A0DC")
            }

            tileAttribute("device.thermostatMode", key: "THERMOSTAT_MODE") {
                attributeState("off", action:"polling.poll", label:'${name}')
                attributeState("heat", action:"polling.poll", label:'${name}')
                attributeState("cool", action:"polling.poll", label:'${name}')
                attributeState("auto", action:"polling.poll", label:'${name}')
            }
            tileAttribute("device.heatingSetpoint", key: "HEATING_SETPOINT") {
                attributeState("default", label:'${currentValue}')
            }
            tileAttribute("device.coolingSetpoint", key: "COOLING_SETPOINT") {
                attributeState("default", label:'${currentValue}')
            }
        }
        standardTile("thermostatMode", "device.thermostatMode", inactiveLabel: true, width:2, height:2, decoration: "flat") {
//            state("auto", label: "auto", action:"thermostat.off", icon: "https://nest.com/support/images/misc-assets-icons/thermostat-icon-heat-cool.png")
            state("off", label: "off", action:"thermostat.cool", icon: "https://nest.com/support/images/misc-assets-icons/thermostat-icon.png")
            state("cool", label: "cool", action:"thermostat.heat", icon: "https://nest.com/support/images/misc-assets-icons/thermostat-icon-cool.png")
            state("heat", label: "heat", action:"thermostat.off", icon: "https://nest.com/support/images/misc-assets-icons/thermostat-icon-heat.png")
        }
        standardTile("thermostatFanMode", "device.thermostatFanMode", inactiveLabel: true, width:2, height:2, decoration: "flat") {
            state "auto", action:"thermostat.fanOn", icon: "st.thermostat.fan-auto"
            state "on", action:"thermostat.fanCirculate", icon: "st.thermostat.fan-on"
            state "circulate", action:"thermostat.fanAuto", icon: "st.thermostat.fan-circulate"
        }
        standardTile("nestPresence", "device.nestPresence", inactiveLabel: true, width:2, height:2, decoration: "flat") {
            state "present", label:'home', action:"away", icon: "st.Home.home2"
            state "not present", label:'away', action:"present", icon: "st.Transportation.transportation5"
        }
        standardTile("refresh", "device.thermostatMode", width:1, height:1, decoration: "flat") {
            state "default", action:"polling.poll", icon:"st.secondary.refresh"
        }
        valueTile("firmwareVer", "device.firmwareVer", width: 2, height: 1, decoration: "flat", wordWrap: true) {
            state "default", label: 'Firmware:\nv${currentValue}'
        }
        valueTile("autoAway", "device.autoAway", width: 2, height: 1, decoration: "flat", wordWrap: true) {
            state "default", label: '${currentValue}'
        }
        valueTile("airFilter", "device.airFilter", width: 2, height: 1, decoration: "flat", wordWrap: true) {
            state "default", label: '${currentValue}'
        }
        standardTile("leafValue", "device.leafValue", width: 1, height: 1) {
            state("on", icon: "https://nest.com/support/images/misc-assets-icons/nest-leaf-icon.png")
            state("off", icon: "https://dl.dropboxusercontent.com/s/r8od1fqp7sffrf0/nest_leaf_dark.png")
        }
        valueTile("heatingSetpoint", "device.heatingSetpoint", width: 1, height: 1, inactiveLabel: false) {
            state "heat", label: '${currentValue}° heat', unit: "F", backgroundColor: "#ffffff"
        }

        valueTile("coolingSetpoint", "device.coolingSetpoint", width: 1, height: 1, inactiveLabel: false) {
            state "cool", label: '${currentValue}° cool', unit: "F", backgroundColor: "#ffffff"
        }
        /*standardTile("temperatureUnit", "device.temperatureUnit", width:2, height:2, canChangeIcon: false, decoration: "flat") {
state "fahrenheit",  label: "°F", icon: "st.alarm.temperature.normal", action:"setCelsius"
state "celsius", label: "°C", icon: "st.alarm.temperature.normal", action:"setFahrenheit"
}*/

        standardTile("deviceIcon", "device.temperature", width:2, height:2) {
            state "default", label:'${currentValue}°', icon: "https://d2qxwxxauf5yxr.cloudfront.net/images/support/index/thermostat-1199122931.jpg"
        }

        main(["deviceIcon"])

        details(["temperature", "thermostatMode", "nestPresence", "thermostatFanMode", "autoAway", "airFilter", "leafValue", "firmwareVer", "refresh", "heatingSetpoint", "coolingSetpoint"])
    }
}

// update preferences
def updated() {
    log.debug "Updated"
    // reset the authentication
    data.auth = null
}

def installed() {
    sendEvent(name: "temperature", value: 72, unit: "F")
    sendEvent(name: "heatingSetpoint", value: 70, unit: "F")
    sendEvent(name: "coolingSetpoint", value: 76, unit: "F")
	sendEvent(name: "targetTemperature", value: 76, unit: "F")
    sendEvent(name: "thermostatMode", value: "off")
    sendEvent(name: "thermostatFanMode", value: "fanAuto")
    sendEvent(name: "thermostatOperatingState", value: "idle")
    sendEvent(name: "humidity", value: 53, unit: "%")
}

// parse events into attributes
def parse(String description) {

}

def setTemperature(value) {

log.debug("Set Temp: ${value}")
//sendEvent(name: 'targetTemperature', value: value.toInteger(), unit: temperatureUnit)

	def operMode = device.currentValue("thermostatMode")
    def curTemp = device.currentValue("temperature").toInteger()
    def newCTemp
    def newHTemp 
    switch (operMode) {
        case "heat":
        newHTemp = value
        setHeatingSetpoint(newHTemp.toInteger())
        break;
        case "cool":
        newCTemp = value
        setCoolingSetpoint(newCTemp.toInteger())
        break;
        case "auto":
        (value < curTemp) ? (newHTemp = getHeatTemp().toInteger() - 1) : (newHTemp = getHeatTemp().toInteger() + 1)
        (value < curTemp) ? (newCTemp = getCoolTemp().toInteger() + 1) : (newCTemp = getCoolTemp().toInteger() - 1)
        setHeatingSetpoint(newHTemp.toInteger())
        setCoolingSetpoint(newCTemp.toInteger())
        break;
        default:
            break;
    }
}

def getHeatTemp() { 
    try { return device.latestValue("heatingSetpoint") } 
    catch (e) { return 0 }
}

def getCoolTemp() { 
    try { return device.latestValue("coolingSetpoint") } 
    catch (e) { return 0 }
}

// handle commands
def setHeatingSetpoint(temp) {
    def latestThermostatMode = device.latestState('thermostatMode')
    def temperatureUnit = device.latestValue('temperatureUnit')
    
    log.debug "data.structure.away " + data.structure.away + " data.shared.can_heat " + data.shared.can_heat

    if ((data.structure.away == 'present') && data.shared.can_heat) {

        switch (temperatureUnit) {
            case "celsius":
            if (temp) {
                if (temp < 9) {
                    temp = 9
                }
                if (temp > 32) {
                    temp = 32
                }
                if (latestThermostatMode.stringValue == 'auto') {
                    api('temperature', ['target_change_pending': true, 'target_temperature_low': temp]) {
                        sendEvent(name: 'heatingSetpoint', value: temp, unit: temperatureUnit, state: "heat")
                        sendEvent(name: 'targetTemperature', value: temp, unit: temperatureUnit)
                    }
                } else if (latestThermostatMode.stringValue == 'heat') {
                    api('temperature', ['target_change_pending': true, 'target_temperature': temp]) {
                        sendEvent(name: 'heatingSetpoint', value: temp, unit: temperatureUnit, state: "heat")
                        sendEvent(name: 'targetTemperature', value: temp, unit: temperatureUnit)
                    }
                }
            }
            break;
            default:
                if (temp) {
                    if (temp < 51) {
                        temp = 51
                    }
                    if (temp > 89) {
                        temp = 89
                    }
                    log.debug "Heat Temp Received: ${temp}"
                    if (latestThermostatMode.stringValue == 'auto') {
                        api('temperature', ['target_change_pending': true, 'target_temperature_low': fToC(temp)]) {
                            sendEvent(name: 'heatingSetpoint', value: temp, unit: temperatureUnit, state: "heat")
                            sendEvent(name: 'targetTemperature', value: temp, unit: temperatureUnit)
                        }
                    } else if (latestThermostatMode.stringValue == 'heat') {
                        api('temperature', ['target_change_pending': true, 'target_temperature': fToC(temp)]) {
                            sendEvent(name: 'heatingSetpoint', value: temp, unit: temperatureUnit, state: "heat")
                            sendEvent(name: 'targetTemperature', value: temp, unit: temperatureUnit)
                        }
                    }
                }
            break;
        }
    } else {
        log.debug "Skipping heat change"
    }
    poll()
}

def setCoolingSetpoint(temp) {
    def latestThermostatMode = device.latestState('thermostatMode')
    def temperatureUnit = device.latestValue('temperatureUnit')

    log.debug "data.structure.away " + data.structure.away + " data.shared.can_cool " + data.shared.can_cool

    if ((data.structure.away == 'present') && data.shared.can_cool) {

        switch (temperatureUnit) {
            case "celsius":
            if (temp) {
                if (temp < 9) {
                    temp = 9
                }
                if (temp > 32) {
                    temp = 32
                }
                if (latestThermostatMode.stringValue == 'auto') {
                    api('temperature', ['target_change_pending': true, 'target_temperature_high': temp]) {
                        sendEvent(name: 'coolingSetpoint', value: temp, unit: temperatureUnit)
                        sendEvent(name: 'targetTemperature', value: temp, unit: temperatureUnit)
                    }
                } else if (latestThermostatMode.stringValue == 'cool') {
                    api('temperature', ['target_change_pending': true, 'target_temperature': temp]) {
                        sendEvent(name: 'coolingSetpoint', value: temp, unit: temperatureUnit)
                        sendEvent(name: 'targetTemperature', value: temp, unit: temperatureUnit)
                    }
                }
            }
            break;
            default:
                if (temp) {
                    if (temp < 51) {
                        temp = 51
                    }
                    if (temp > 89) {
                        temp = 89
                    }
                    if (latestThermostatMode.stringValue == 'auto') {
                        api('temperature', ['target_change_pending': true, 'target_temperature_high': fToC(temp)]) {
                            sendEvent(name: 'coolingSetpoint', value: temp, unit: temperatureUnit)
                            sendEvent(name: 'targetTemperature', value: temp, unit: temperatureUnit)
                        }
                    } else if (latestThermostatMode.stringValue == 'cool') {
                        api('temperature', ['target_change_pending': true, 'target_temperature': fToC(temp)]) {
                            sendEvent(name: 'coolingSetpoint', value: temp, unit: temperatureUnit)
                            sendEvent(name: 'targetTemperature', value: temp, unit: temperatureUnit)
                        }
                    }
                }
            break;
        }
    } else {
        log.debug "Skipping cool change"
    }
    poll()
}

def setFahrenheit() {
    def temperatureUnit = "fahrenheit"
    log.debug "Setting temperatureUnit to: ${temperatureUnit}"
    sendEvent(name: "temperatureUnit",   value: temperatureUnit)
    poll()
}

def setCelsius() {
    def temperatureUnit = "celsius"
    log.debug "Setting temperatureUnit to: ${temperatureUnit}"
    sendEvent(name: "temperatureUnit",   value: temperatureUnit)
    poll()
}

def off() {
    setThermostatMode('off')
}

def heat() {
    if (data.shared.can_heat) {
        setThermostatMode('heat')
    } else {
        log.debug "heating not supported"
        poll()
    }
}

def emergencyHeat() {
    if (data.shared.can_heat) {
        setThermostatMode('heat')
    } else {
        log.debug "heating not supported"
        poll()
    }
}

def cool() {
    if (data.shared.can_cool) {
        setThermostatMode('cool')
    } else {
        log.debug "cooling not supported"
        poll()
    }
}

def auto() {
    if ((data.shared.can_cool) && (data.shared.can_heat)) {
        setThermostatMode('range')
        
//        def heatingSetpoint = device.latestValue('heatingSetpoint')
//        def coolingSetpoint = device.latestValue('coolingSetpoint')
//        sendEvent(name: 'targetTemperature', value: heatingSetpoint, unit: temperatureUnit)        
//        sendEvent(name: 'targetTemperature', value: coolingSetpoint, unit: temperatureUnit)
    } else {
        log.debug "heating AND cooling not supported"
        poll()
    }
}

def setThermostatMode(mode) {
    mode = mode == 'emergency heat'? 'heat' : mode
    
    def heatingSetpoint = device.latestValue('heatingSetpoint')
    def coolingSetpoint = device.latestValue('coolingSetpoint')

    api('thermostat_mode', ['target_change_pending': true, 'target_temperature_type': mode]) {
        mode = mode == 'range' ? 'auto' : mode
        sendEvent(name: 'thermostatMode', value: mode)
    }

    if (mode == 'heat') {
        sendEvent(name: 'targetTemperature', value: heatingSetpoint, unit: temperatureUnit)        
        setTemperature(heatingSetpoint)
    } else
	if (mode == 'cool') {
        sendEvent(name: 'targetTemperature', value: coolingSetpoint, unit: temperatureUnit)        
        setTemperature(coolingSetpoint)
    }

    poll()

}

def fanOn() {
    if (data.device.has_fan) {
        setThermostatFanMode('on')
    } else {
        log.debug "no fan control"
        poll()
    }
}

def fanAuto() {
    setThermostatFanMode('auto')
}

def fanCirculate() {
    if (data.device.has_fan) {
        setThermostatFanMode('circulate')
    } else {
        log.debug "no fan control"
        poll()
    }
}

def setThermostatFanMode(mode) {
    def modes = [
        on: ['fan_mode': 'on'],
        auto: ['fan_mode': 'auto'],
        circulate: ['fan_mode': 'duty-cycle', 'fan_duty_cycle': 900]
    ]

    api('fan_mode', modes.getAt(mode)) {
        sendEvent(name: 'thermostatFanMode', value: mode)
        poll()
    }
}

def away() {
    setNestPresence('away')
    sendEvent(name: 'nestPresence', value: 'not present')
    sendEvent(name: 'presence', value: 'not present')
}

def present() {
    setNestPresence('present')
    sendEvent(name: 'nestPresence', value: 'present')
    sendEvent(name: 'presence', value: 'present')
}

def setNestPresence(status) {
    log.debug "Status: $status"
    api('nestPresence', ['away': status == 'away', 'away_timestamp': new Date().getTime(), 'away_setter': 0]) {
        poll()
    }
}

def setDeviceLabel(value) {

}

def setFirmwareVer(value) {
    def firmVer = data?.device?.current_version ?: "unknown"
    sendEvent(name: 'firmwareVer', value: firmVer)	
}

def setFilterStatus(hasFilter,filterReminder,filterStatus) {
    def value
    def filterVal = (filterStatus) ? "Ok" : "Replace"
    if (hasFilter) { value = "Installed (${filterVal})" }
    else { value = "Not Installed" }
    //log.info "Air Filter: ${value}"
    sendEvent(name: 'airFilter', value: "Filter:\n" + value)
}

def setAutoAwayStatus(enabled, learning, status) {
    def value 
    def aaActive = (status == 1) ? "On" : "Off"
    if(enabled) { value = "${aaActive} (${learning})" }
    else { value = "Disabled" }
    //log.info "Auto-Away: ${value}"
    sendEvent(name: 'autoAway', value: "Auto-Away:\n" + value)
}

def setLeafStatus(leaf) {
    def val = leaf.toBoolean() ? "on" : "off"
    //log.info "Nest Leaf: ${val}" 
    sendEvent(name: 'leafValue', value: '${val}' )
}

def setTempUnit(unit) {
    def cur = device.latestValue("temperatureUnit")
    def value = (unit = "F") ? "fahrenheit" : "celsius"	
    if (!cur || (value != cur)) {
        log.info "Temperature Unit: ${value}"
        sendEvent(name: "temperatureUnit", value: value)
    }
}

def setRssiLevel(value) {
    def tmp = value.toInteger()
    def val = getSigLevelVal(tmp)
    //log.info "RSSI Level: ${value}(${tmp}), ${val}"
    sendEvent(name: 'rssiTile', value: '${val}')
    sendEvent(name: 'rssi', value: value)
}

def getSigLevelVal(value) {
    def valA = ["one", "two", "three", "four", "five", "none"]
    if (value > 1 && value < 50) { return valA[0] }
    if (value >= 50 && value < 60) { return valA[1] }
    if (value >= 60 && value < 70) { return valA[2] }
    if (value >= 70 && value < 80) { return valA[3] }
    if (value >= 80) { return valA[4] }
    if ((value = 0) || (!value)) { return valA[5] }
}

def setDehumidifierStatus() {
}

def poll() {
    log.debug "Executing 'poll'"
    api('status', []) {
        //log.debug "performing actions after api status"
        data.device = it.data.device.getAt(settings.serial)
        data.shared = it.data.shared.getAt(settings.serial)
        data.structureId = it.data.link.getAt(settings.serial).structure.tokenize('.')[1]
        data.structure = it.data.structure.getAt(data.structureId)

        //log.debug "data.shared: " + data.shared
        def items = data.device
        for (item in items) {
            //log.debug "Device Data: ${item.key}: ${item.value}"
        }
        //log.debug "data.structure: " + data.structure

        data.device.fan_mode = data.device.fan_mode == 'duty-cycle'? 'circulate' : data.device.fan_mode
        data.structure.away = data.structure.away ? 'away' : 'present'

        setTempUnit(data?.temperature_scale?.toString())
        setFirmwareVer(data?.device?.current_version)
        setFilterStatus(data?.device?.has_air_filter.toBoolean(), data?.device?.filter_reminder_enabled, null)
        setLeafStatus(data?.device?.leaf)
        setRssiLevel(data?.device?.rssi)
        setAutoAwayStatus(data?.device?.auto_away_enable, data?.shared?.auto_away_learning, data?.shared?.auto_away)
        //setDehumidifierStatus(data?.device?.dehumidifier_type, data?.device?.dehumidifier_state
        def humidity = data?.device?.current_humidity
        def temperatureType = data?.shared?.target_temperature_type
        def fanMode = data?.device?.fan_mode

        def heatingSetpoint = device.latestValue('heatingSetpoint')
        def coolingSetpoint = device.latestValue('coolingSetpoint')

        temperatureType = temperatureType == 'range' ? 'auto' : temperatureType

        sendEvent(name: 'humidity', value: humidity)
        sendEvent(name: 'thermostatFanMode', value: fanMode)
        sendEvent(name: 'thermostatMode', value: temperatureType)

        def temperatureUnit = device.latestValue('temperatureUnit')
        switch (temperatureUnit) {
            case "celsius":
            def temperature = Math.round(data.shared.current_temperature)
            def targetTemperature = Math.round(data.shared.target_temperature)

            if (temperatureType == "cool") {
                coolingSetpoint = targetTemperature
                heatingSetpoint = device.latestValue('heatingSetpoint')
            } else if (temperatureType == "heat") {
                heatingSetpoint = targetTemperature
                coolingSetpoint = device.latestValue('coolingSetpoint')
            } else if (temperatureType == "auto") {
                coolingSetpoint = Math.round(data.shared.target_temperature_high)
                heatingSetpoint = Math.round(data.shared.target_temperature_low)
            }
            if (data.structure.away == 'away') {
                if (data.device.away_temperature_high_enabled) {
                    coolingSetpoint = Math.round(data.device.away_temperature_high)
                }
                if (data.device.away_temperature_low_enabled) {
                    heatingSetpoint = Math.round(data.device.away_temperature_low)
                }
            }
            sendEvent(name: 'temperature', value: temperature, unit: temperatureUnit, state: temperatureType)
            sendEvent(name: 'coolingSetpoint', value: coolingSetpoint, unit: temperatureUnit, state: "cool")
            sendEvent(name: 'heatingSetpoint', value: heatingSetpoint, unit: temperatureUnit, state: "heat")
            sendEvent(name: 'targetTemperature', value: targetTemperature, unit: temperatureUnit)
            
            break;
            default:
                def temperature = Math.round(cToF(data.shared.current_temperature))
                def targetTemperature = Math.round(cToF(data.shared.target_temperature))
                if (temperatureType == "cool") {
                    coolingSetpoint = targetTemperature
                    heatingSetpoint = device.latestValue('heatingSetpoint')
                } else if (temperatureType == "heat") {
                    heatingSetpoint = targetTemperature
                    coolingSetpoint = device.latestValue('coolingSetpoint')
                } else if (temperatureType == "auto") {
                    coolingSetpoint = Math.round(cToF(data.shared.target_temperature_high))
                    heatingSetpoint = Math.round(cToF(data.shared.target_temperature_low))
                }
            if (data.structure.away == 'away') {
                if (data.device.away_temperature_high_enabled) {
                    coolingSetpoint = Math.round(cToF(data.device.away_temperature_high))
                    //log.debug "we are away in poll coolingSetpoint " + coolingSetpoint
                }
                if (data.device.away_temperature_low_enabled) {
                    heatingSetpoint = Math.round(cToF(data.device.away_temperature_low))
                    //log.debug "we are away in poll heatingSetpoint " + heatingSetpoint
                }
                //log.debug "we are away in poll ending"
            }
            sendEvent(name: 'temperature', value: temperature, unit: temperatureUnit, state: temperatureType)
            sendEvent(name: 'coolingSetpoint', value: coolingSetpoint, unit: temperatureUnit, state: "cool")
            sendEvent(name: 'heatingSetpoint', value: heatingSetpoint, unit: temperatureUnit, state: "heat")
            sendEvent(name: 'targetTemperature', value: targetTemperature, unit: temperatureUnit)

            break;
        }

        if (data.structure.away == 'away') {
            sendEvent(name: 'nestPresence', value: 'not present')
            sendEvent(name: 'presence', value: 'not present')
        }
        if (data.structure.away == 'present') {
            sendEvent(name: 'nestPresence', value: 'present')
            sendEvent(name: 'presence', value: 'present')
        }

        if (data.shared.hvac_ac_state) {
            sendEvent(name: 'thermostatOperatingState', value: "cooling")
        } else if (data.shared.hvac_heater_state) {
            sendEvent(name: 'thermostatOperatingState', value: "heating")
        } else if (data.shared.hvac_fan_state) {
            sendEvent(name: 'thermostatOperatingState', value: "fan only")
        } else {
            sendEvent(name: 'thermostatOperatingState', value: "idle")
        }
    }
}

def api(method, args = [], success = {}) {
    if(!isLoggedIn()) {
        log.debug "Need to login"
        login(method, args, success)
        if(!isLoggedIn()) {
            log.debug "Still not logged in"
        }
        return
    }

    def methods = [
        'status': [uri: "/v2/mobile/${data.auth.user}", type: 'get'],
        'fan_mode': [uri: "/v2/put/device.${settings.serial}", type: 'post'],
        'thermostat_mode': [uri: "/v2/put/shared.${settings.serial}", type: 'post'],
        'temperature': [uri: "/v2/put/shared.${settings.serial}", type: 'post'],
        'nestPresence': [uri: "/v2/put/structure.${data.structureId}", type: 'post']
    ]

    def request = methods.getAt(method)

    log.debug "Logged in"
    doRequest(request.uri, args, request.type, success)
}

// Need to be logged in before this is called. So don't call this. Call api.
def doRequest(uri, args, type, success) {
    log.debug "Calling $type : $uri : $args"

    if(uri.charAt(0) == '/') {
        uri = "${data.auth.urls.transport_url}${uri}"
    }
    def params = [
        uri: uri,
        headers: [
            'X-nl-protocol-version': 1,
            'X-nl-user-id': data.auth.userid,
            'Authorization': "Basic ${data.auth.access_token}"
        ],
        body: args
    ]

    def postRequest = { response ->
        if (response.getStatus() == 302) {
            def locations = response.getHeaders("Location")
            def location = locations[0].getValue()
            log.debug "redirecting to ${location}"
            doRequest(location, args, type, success)
        } else {
            success.call(response)
        }
    }

    try {
        if (type == 'post') {
            httpPostJson(params, postRequest)
        } else if (type == 'get') {
            httpGet(params, postRequest)
        }
    } catch (Throwable e) {
        login()
    }
}

def login(method = null, args = [], success = {}) {
    def params = [
        uri: 'https://home.ft.nest.com/user/login',
        body: [username: settings.username, password: settings.password]
    ]

    httpPost(params) {response ->
        data.auth = response.data
        data.auth.expires_in = Date.parse('EEE, dd-MMM-yyyy HH:mm:ss z', response.data.expires_in).getTime()
        //log.debug data.auth
        def items = data.auth
        for (item in items) {
            //log.debug "Auth Data: ${item.key}: ${item.value}"
        }

        api(method, args, success)
    }
}

def isLoggedIn() {
    if(!data.auth) {
        log.debug "No data.auth"
        return false
    }

    def now = new Date().getTime();
    return data.auth.expires_in > now
}

def cToF(temp) {
    return (temp * 1.8 + 32).toDouble()
}

def fToC(temp) {
    return ((temp - 32) / 1.8).toDouble()
}
