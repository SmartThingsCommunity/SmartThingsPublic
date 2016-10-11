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
 *	Author: SmartThings
 *	Date: 2013-12-02
 */
metadata {
	definition (name: "CentraLite ZigBee HA Thermostat", namespace: "smartthings", author: "SmartThings") {
		capability "Actuator"
		capability "Temperature Measurement"
		capability "Thermostat"
		capability "Configuration"
		capability "Refresh"
		capability "Sensor"
		capability "Polling"
		capability "Battery"

		// Custom commands 
        command "raiseHeatLevel"
		command "lowHeatLevel"
		command "raiseCoolLevel"
		command "lowerCoolLevel"
		command "setTemperature"
		command "setThermostatHoldMode"
		command "getPowerSource"

		attribute "temperatureScale", "string"
		attribute "thermostatHoldMode", "string"
		attribute "powerSource", "string"
		
		fingerprint profileId: "0104", inClusters: "0000,0001,0003,0020,0201,0202,0204,0B05", outClusters: "000A, 0019"
	}

	// simulator metadata
	simulator { }
    
	tiles(scale: 2) {
        multiAttributeTile(name:"temperature", type:"thermostat", width:6, height:4, canChangeIcon: false) {
            tileAttribute("device.temperature", key: "PRIMARY_CONTROL") {
                attributeState("default", label:'${currentValue}', unit:"dF",
                    backgroundColors:[
                        // Celsius Color Range
                        [value: 0, color: "#153591"],
                        [value: 7, color: "#1e9cbb"],
                        [value: 15, color: "#90d2a7"],
                        [value: 23, color: "#44b621"],
                        [value: 29, color: "#f1d801"],
                        [value: 33, color: "#d04e00"],
                        [value: 36, color: "#bc2323"],
                        // Fahrenheit Color Range
                        [value: 40, color: "#153591"],
                        [value: 44, color: "#1e9cbb"],
                        [value: 59, color: "#90d2a7"],
                        [value: 65, color: "#99ff33"],
                        [value: 74, color: "#44b621"],
                        [value: 84, color: "#f1d801"],
                        [value: 92, color: "#d04e00"],
                        [value: 96, color: "#bc2323"]
                    ])
            }
            tileAttribute("device.temperature", key: "VALUE_CONTROL") {
                attributeState("default", action: "setTemperature")
			}
            tileAttribute("device.thermostatMode", key: "THERMOSTAT_MODE") {
                attributeState("off", label:'${name}')
                attributeState("heat", label:'${name}')
                attributeState("cool", label:'${name}')
            }
            tileAttribute("device.heatingSetpoint", key: "HEATING_SETPOINT") {
                attributeState("default", label:'${currentValue}', unit:"dF")
            }
            tileAttribute("device.coolingSetpoint", key: "COOLING_SETPOINT") {
                attributeState("default", label:'${currentValue}', unit:"dF")
            }
        }
        
        //Row 1
        standardTile("raiseHeatLevel", "device.heatingSetpoint", height: 1, width: 1, inactiveLabel: false, decoration: "flat") {
            state "raiseHeatLevel", label: 'Heat', action:"raiseHeatLevel", icon:"st.thermostat.thermostat-up"
        }
        standardTile("lowHeatLevel", "device.heatingSetpoint", height: 1, width: 1, inactiveLabel: false, decoration: "flat") {
            state "lowHeatLevel", label: 'Heat', action:"lowHeatLevel", icon:"st.thermostat.thermostat-down"
        }
        valueTile("heatingSetpoint", "device.heatingSetpoint", height: 2, width: 2, inactiveLabel: false, decoration: "flat") {
			state "heat", label:'${currentValue}°', backgroundColor:"#fd631c"
		}
        standardTile("raiseCoolLevel", "device.coolingSetpoint", height: 1, width: 1, inactiveLabel: false, decoration: "flat") {
            state "raiseCoolLevel", label: 'Cool', action:"raiseCoolLevel", icon:"st.thermostat.thermostat-up"
        }
        standardTile("lowerCoolLevel", "device.coolingSetpoint", height: 1, width: 1, inactiveLabel: false, decoration: "flat") {
            state "lowerCoolLevel", label: 'Cool', action:"lowerCoolLevel", icon:"st.thermostat.thermostat-down"
        }
        valueTile("coolingSetpoint", "device.coolingSetpoint", height: 2, width: 2, inactiveLabel: false, decoration: "flat") {
			state "cool", label:'${currentValue}°', backgroundColor:"#66ccff"
		}
        
        //Row 2
        standardTile("fanMode", "device.thermostatFanMode", height: 2, width: 2, inactiveLabel: false, decoration: "flat") {
			state "fanAuto", action:"thermostat.setThermostatFanMode", icon: "st.thermostat.fan-auto", nextState: "fanOn"
			state "fanOn", action:"thermostat.setThermostatFanMode", icon: "st.thermostat.fan-on", nextState: "fanAuto"
		}
        standardTile("mode", "device.thermostatMode", height: 2, width: 2, inactiveLabel: false, decoration: "flat") {
			state "off", action:"thermostat.setThermostatMode", icon: "st.thermostat.heating-cooling-off", nextState: "cool"
			state "cool", action:"thermostat.setThermostatMode", icon: "st.thermostat.cool", nextState: "heat"
			state "heat", action:"thermostat.setThermostatMode", icon: "st.thermostat.heat", nextState: "off"
		}
		standardTile("holdMode", "device.thermostatHoldMode", height: 1, width: 1, inactiveLabel: false, decoration: "flat") {
			state "holdOff", label: 'Hold Off', action:"setThermostatHoldMode", icon: "st.Weather.weather2", nextState: "holdOff"
			state "holdOn", label: 'Hold On', action:"setThermostatHoldMode", icon: "st.Weather.weather2", nextState: "holdOn"
		}
        standardTile("battery", "device.battery", height: 1, width: 1, inactiveLabel: false, decoration: "flat") {
			state "default", label: '${currentValue}%', icon:"st.switches.switch.off"
		}
        standardTile("refresh", "device.refresh", height: 1, width: 1, inactiveLabel: false, decoration: "flat") {
			state "default", label: 'Refresh', action:"refresh.refresh", icon:"st.secondary.refresh-icon"
		}
        standardTile("configure", "device.configure", height: 1, width: 1, inactiveLabel: false, decoration: "flat") {
			state "configure", label: 'Config', action:"configuration.configure", icon:"st.secondary.tools"
		}
        
        main(["temperature"])
        details(["temperature", "raiseHeatLevel", "heatingSetpoint", "raiseCoolLevel", "coolingSetpoint", "lowHeatLevel", "lowerCoolLevel", "fanMode", "mode", "holdMode", "battery", "refresh", "configure"])
	}
}

def setTemperature(setpoint) {
	log.debug "setTemperature() called with setpoint ${setpoint}. "
 	log.debug "Current temperature: ${device.currentValue("temperature")}. Heat Setpoint: ${device.currentValue("heatingSetpoint")}. Cool Setpoint: ${device.currentValue("coolingSetpoint")}"
   
    def mode = device.currentValue("thermostatMode")
	if (mode == "off") {
		log.warn "setTemperature(): this mode: $mode does not allow raiseSetpoint"
        return
    }
    
    def midpoint
	def targetvalue
    def currentTemp = device.currentValue("temperature")
    def deltaTemp = setpoint - currentTemp
    
    log.debug "deltaTemp = ${deltaTemp}"
    
    if (mode == "heat") {
        log.debug "setTemperature(): change the heat temp"
		if (deltaTemp < 0) {
			lowHeatLevel()
        } else if (deltaTemp > 0) {
			raiseHeatLevel()
		}
    } else if (mode == "cool") {
        log.debug "setTemperature(): change the cool temp"
		if (deltaTemp < 0) {
			lowerCoolLevel()
        } else if (deltaTemp > 0) {
			raiseCoolLevel()
		}
    }
}

def raiseHeatLevel() {
    def mode = device.currentValue("thermostatMode")
 	if (mode == "off") {
		log.warn "raiseHeatLevel(): this mode: $mode does not allow raiseHeatLevel"
	} else {
		int nextLevel = device.currentValue("heatingSetpoint").toInteger() + 1
		if (nextLevel > getMaxTemp()) {
			nextLevel = getMaxTemp()
		}
		log.debug "Setting heat set point up to: ${nextLevel}"
		setHeatingSetpoint(nextLevel)
	}
}

def lowHeatLevel() {
	def mode = device.currentValue("thermostatMode")
 	if (mode == "off") {
		log.warn "lowHeatLevel(): this mode: $mode does not allow lowHeatLevel"
	} else {
		int nextLevel = device.currentValue("heatingSetpoint").toInteger() - 1
		if (nextLevel < getMinTemp()) {
			nextLevel = getMinTemp()
		}
		log.debug "Setting heat set point down to: ${nextLevel}"
		setHeatingSetpoint(nextLevel)
	}
}
        
def raiseCoolLevel() {
	def mode = device.currentValue("thermostatMode")
 	if (mode == "off") {
		log.warn "raiseCoolLevel(): this mode: $mode does not allow raiseCoolLevel"
	} else {
		int nextLevel = device.currentValue("coolingSetpoint").toInteger() + 1
		if (nextLevel > getMaxTemp()) {
			nextLevel = getMaxTemp()
		}
		log.debug "Setting cool set point up to: ${nextLevel}"
		setCoolingSetpoint(nextLevel)
	}
}

def lowerCoolLevel() {
	def mode = device.currentValue("thermostatMode")
 	if (mode == "off") {
		log.warn "lowerCoolLevel(): this mode: $mode does not allow lowerCoolLevel"
	} else {
		int nextLevel = device.currentValue("coolingSetpoint").toInteger() - 1
		if (nextLevel < getMinTemp()) {
			nextLevel = getMinTemp()
		}
		log.debug "Setting cool set point down to: ${nextLevel}"
		setCoolingSetpoint(nextLevel)
	}
}

def parse(String description) {
	log.debug "Parse description $description"
	def map = [:]
	if (description?.startsWith("read attr -")) {
		def descMap = parseDescriptionAsMap(description)
		log.debug "Desc Map: $descMap"
		if (descMap.cluster == "0201" && descMap.attrId == "0000") {
			log.debug "TEMP"
			map.name = "temperature"
			map.value = getTemperature(descMap.value)
		} else if (descMap.cluster == "0201" && descMap.attrId == "0011") {
			log.debug "COOLING SETPOINT"
			map.name = "coolingSetpoint"
			map.value = getTemperature(descMap.value)
		} else if (descMap.cluster == "0201" && descMap.attrId == "0012") {
			log.debug "HEATING SETPOINT"
			map.name = "heatingSetpoint"
			map.value = getTemperature(descMap.value)
		} else if (descMap.cluster == "0201" && descMap.attrId == "001c") {
			log.debug "MODE"
			map.name = "thermostatMode"
			map.value = getModeMap()[descMap.value]
		} else if (descMap.cluster == "0202" && descMap.attrId == "0000") {
			log.debug "FAN MODE"
			map.name = "thermostatFanMode"
			map.value = getFanModeMap()[descMap.value]
		} else if (descMap.cluster == "0001" && descMap.attrId == "0020") {
		    log.debug "BATTERY"
			map.name = "battery"
			map.value = getBatteryLevel(descMap.value)
		} else if (descMap.cluster == "0201" && descMap.attrId == "0023") {
		    log.debug "HOLD MODE"
			map.name = "thermostatHoldMode"
			map.value = getHoldModeMap()[descMap.value]			
		} else if (descMap.cluster == "0000" && descMap.attrId == "0007") {
		    log.debug "POWER SOURCE"
			map.name = "powerSource"
			map.value = getPowerSource()[descMap.value]			
		}
	}
	def result = null
	if (map) {
		result = createEvent(map)
	}
	log.debug "Parse returned $map"
	return result
}

def parseDescriptionAsMap(description) {
	(description - "read attr - ").split(",").inject([:]) { map, param ->
		def nameAndValue = param.split(":")
		map += [(nameAndValue[0].trim()):nameAndValue[1].trim()]
	}
}

def getModeMap() { [
	"00":"off",
    "01":"auto",
	"03":"cool",
	"04":"heat",
	"05":"emergencyHeat"
]}

def getHoldModeMap() { [
	"00":"holdOff",
	"01":"holdOn",
]}

def getPowerSource() { [
	"01":"24VAC",
	"03":"Battery",
]}

def getFanModeMap() { [
	"04":"fanOn",
	"05":"fanAuto"
]}

def getTemperature(value) {
	if (value != null) {
		def celsius = Integer.parseInt(value, 16) / 100
		if (getTemperatureScale() == "C") {
			return celsius
		} else {
			return Math.round(celsiusToFahrenheit(celsius))			
		}
	}
}

def setHeatingSetpoint(degrees) {
	if (degrees != null) {
		def temperatureScale = getTemperatureScale()
		def degreesInteger = Math.round(degrees)
		log.debug "setHeatingSetpoint({$degreesInteger} ${temperatureScale})"
		sendEvent("name": "heatingSetpoint", "value": degreesInteger)
		def celsius = (getTemperatureScale() == "C") ? degreesInteger : (fahrenheitToCelsius(degreesInteger) as Double).round(2)
		"st wattr 0x${device.deviceNetworkId} 1 0x201 0x12 0x29 {" + hex(celsius * 100) + "}"
	}
}

def setCoolingSetpoint(degrees) {
	if (degrees != null) {
		def degreesInteger = Math.round(degrees)
		log.debug "setCoolingSetpoint({$degreesInteger} ${temperatureScale})"
		sendEvent("name": "coolingSetpoint", "value": degreesInteger)
		def celsius = (getTemperatureScale() == "C") ? degreesInteger : (fahrenheitToCelsius(degreesInteger) as Double).round(2)
		"st wattr 0x${device.deviceNetworkId} 1 0x201 0x11 0x29 {" + hex(celsius * 100) + "}"
	}
}

def modes() {
    ["off", "cool", "heat"]
}

def setThermostatMode() {
	log.debug "switching thermostatMode"
	def currentMode = device.currentState("thermostatMode")?.value
	def modeOrder = modes()
    log.debug "modeOrder: ${modeOrder}"
	def index = modeOrder.indexOf(currentMode)
    log.debug "index: ${index}"
    def next = index >= 0 && index < modeOrder.size() - 1 ? modeOrder[index + 1] : modeOrder[0]
	log.debug "switching mode from $currentMode to $next"
	"$next"()
}

def setThermostatFanMode() {
	log.debug "Switching fan mode"
	def currentFanMode = device.currentState("thermostatFanMode")?.value
	log.debug "switching fan from current mode: $currentFanMode"
	def returnCommand
	switch (currentFanMode) {
		case "fanAuto":
			returnCommand = fanOn()
			break
		case "fanOn":
			returnCommand = fanAuto()
			break
	}
	if(!currentFanMode) {
    	returnCommand = fanAuto()
    }
	returnCommand
}

def setThermostatHoldMode() {
	log.debug "Switching Hold mode"
	def currentHoldMode = device.currentState("thermostatHoldMode")?.value
	log.debug "switching thermostat from current mode: $currentHoldMode"
	def returnCommand
	switch (currentHoldMode) {
		case "holdOff":
			returnCommand = holdOn()
			break
		case "holdOn":
			returnCommand = holdOff()
			break
	}
    if(!currentHoldMode) {
    	returnCommand = holdOff()
    }
	returnCommand
}

def setThermostatMode(String value) {
	log.debug "setThermostatMode({$value})"
	"$value"()
}

def setThermostatFanMode(String value) {
	log.debug "setThermostatFanMode({$value})"
	"$value"()
}

def setThermostatHoldMode(String value) {
	log.debug "setThermostatHoldMode({$value})"
	"$value"()
}

def off() {
	log.debug "off"
	sendEvent("name":"thermostatMode", "value":"off")
	"st wattr 0x${device.deviceNetworkId} 1 0x201 0x1C 0x30 {00}"
}

def cool() {
	log.debug "cool"
	sendEvent("name":"thermostatMode", "value":"cool")
	"st wattr 0x${device.deviceNetworkId} 1 0x201 0x1C 0x30 {03}"
}

def heat() {
	log.debug "heat"
	sendEvent("name":"thermostatMode", "value":"heat")
	"st wattr 0x${device.deviceNetworkId} 1 0x201 0x1C 0x30 {04}"
}

def on() {
	fanOn()
}

def fanOn() {
	log.debug "fanOn"
	sendEvent("name":"thermostatFanMode", "value":"fanOn")
	"st wattr 0x${device.deviceNetworkId} 1 0x202 0 0x30 {04}"
}

def auto() {
	fanAuto()
}

def fanAuto() {
	log.debug "fanAuto"
	sendEvent("name":"thermostatFanMode", "value":"fanAuto")
	"st wattr 0x${device.deviceNetworkId} 1 0x202 0 0x30 {05}"
}

def holdOn() {
	log.debug "Set Hold On for thermostat"
	sendEvent("name":"thermostatHoldMode", "value":"holdOn")
	"st wattr 0x${device.deviceNetworkId} 1 0x201 0x23 0x30 {01}"
}

def holdOff() {
	log.debug "Set Hold Off for thermostat"
	sendEvent("name":"thermostatHoldMode", "value":"holdOff")
	"st wattr 0x${device.deviceNetworkId} 1 0x201 0x23 0x30 {00}"
}

// Commment out below if no C-wire since it will kill the batteries
def poll() {
	refresh()
}

def refresh()
{
	log.debug "refresh called"
	[
		"st rattr 0x${device.deviceNetworkId} 1 0x000 0x07", "delay 200",
		"st rattr 0x${device.deviceNetworkId} 1 0x201 0", "delay 200",
		"st rattr 0x${device.deviceNetworkId} 1 0x201 0x11", "delay 200",
  		"st rattr 0x${device.deviceNetworkId} 1 0x201 0x12", "delay 200",
		"st rattr 0x${device.deviceNetworkId} 1 0x201 0x1C", "delay 200",
		"st rattr 0x${device.deviceNetworkId} 1 0x201 0x1E", "delay 200",
		"st rattr 0x${device.deviceNetworkId} 1 0x201 0x23", "delay 200",
		"st rattr 0x${device.deviceNetworkId} 1 0x001 0x20", "delay 200",
		"st rattr 0x${device.deviceNetworkId} 1 0x202 0"
	] + configure()
}

def configure() {
	log.debug "binding to Thermostat and Fan Control cluster"
	[
		"zdo bind 0x${device.deviceNetworkId} 1 1 0x000 {${device.zigbeeId}} {}", "delay 200",
		"zdo bind 0x${device.deviceNetworkId} 1 1 0x201 {${device.zigbeeId}} {}", "delay 200",
		"zdo bind 0x${device.deviceNetworkId} 1 1 0x202 {${device.zigbeeId}} {}", "delay 200",
		
		"zcl global send-me-a-report 1 0x20 0x20 3600 86400 {01}", "delay 100", //battery report request
		"send 0x${device.deviceNetworkId} 1 1", "delay 200"
	]
}

//Private methods

private getMaxTemp() {
	def maxTemp
    def locationScale = getTemperatureScale()
    if (locationScale == "C") {
        maxTemp = 44
    } else {
        maxTemp = 86
    }
    return maxTemp as int
}

private getMinTemp() {
	def minTemp
	def locationScale = getTemperatureScale()
    if (locationScale == "C") {
        minTemp = 7
    } else {
        minTemp = 30
    }
    return minTemp as int
}

private getBatteryLevel(rawValue) {
	def intValue = Integer.parseInt(rawValue,16)
	def min = 2.1
    def max = 3.0
    def vBatt = intValue / 10
    return ((vBatt - min) / (max - min) * 100) as int
}

private hex(value) {
	new BigInteger(Math.round(value).toString()).toString(16)
}