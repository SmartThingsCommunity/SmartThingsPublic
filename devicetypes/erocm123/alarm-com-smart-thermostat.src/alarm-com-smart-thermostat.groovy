/**
 *  Copyright 2020 Eric Maycock
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
 *  Alarm.com Smart Thermostat ADC-T2000 / Building 36 Intelligent Thermostat B36-T10
 *
 *  Author: Eric Maycock (erocm123)
 *  Date: 2020-08-17
 *
 *  2020-08-17: Fixes to work with new app. 
 *
 *  2017-10-20: Removed parameter 26 "Power Source" as this seems to be read only. 
 *
 */
 
metadata {
	definition (name: "Alarm.com Smart Thermostat", namespace: "erocm123", author: "Eric Maycock", vid:"SmartThings-smartthings-Z-Wave_Thermostat")
    {
		capability "Refresh"
		capability "Actuator"
		capability "Temperature Measurement"
		capability "Relative Humidity Measurement"
		capability "Thermostat"
		capability "Configuration"
		capability "Sensor"
        capability "Battery"
        capability "Health Check"

		command "setTemperature"
        command "heatLevelUp"
		command "heatLevelDown"
		command "coolLevelUp"
		command "coolLevelDown"
        command "quickSetCool"
        command "quickSetHeat"
        command "modeoff"
        command "modeheat"
        command "modecool"
        command "modeauto"
        command "fanauto"
        command "fanon"
        command "fancir"
        
		attribute "thermostatFanState", "string"
        attribute "currentState", "string"
        attribute "currentMode", "string"
        attribute "currentfanMode", "string"
        attribute "needUpdate", "string"
        
        fingerprint mfr: "0190", prod: "0001", model: "0001"
	}

	tiles(scale: 2) {
         multiAttributeTile(name:"temperature", type:"thermostat", width:6, height:4) {
  			tileAttribute("device.temperature", key: "PRIMARY_CONTROL") {
    				attributeState("temperature", label:'${currentValue}°', icon: "st.alarm.temperature.normal", backgroundColors:[
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
					])
  			}
  			tileAttribute("device.temperature", key: "VALUE_CONTROL") {
    				attributeState("default", action: "setTemperature", label: "")
  			}
  			tileAttribute("device.humidity", key: "SECONDARY_CONTROL") {
    				attributeState("default", label:'${currentValue}%', unit:"%")
  			}
  			tileAttribute("device.thermostatOperatingState", key: "OPERATING_STATE") {
    				attributeState("idle", backgroundColor:"#44b621")
    				attributeState("heating", backgroundColor:"#ffa81e")
    				attributeState("cooling", backgroundColor:"#269bd2")
  			}
  			tileAttribute("device.thermostatMode", key: "THERMOSTAT_MODE") {
    				attributeState("off", label:'${name}')
    				attributeState("heat", label:'${name}')
    				attributeState("cool", label:'${name}')
    				attributeState("auto", label:'${name}')
  			}
  			tileAttribute("device.heatingSetpoint", key: "HEATING_SETPOINT") {
    				attributeState("default", label:'${currentValue}')
  			}
  			tileAttribute("device.coolingSetpoint", key: "COOLING_SETPOINT") {
    				attributeState("default", label:'${currentValue}')
  			}
            
		}
        standardTile("thermostatOperatingState", "device.currentState", canChangeIcon: false, inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
            state ("default", label:'${currentValue}', icon:"st.tesla.tesla-hvac")
        }
		standardTile("thermostatFanState", "device.thermostatFanState", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
            state "running", label:'Fan is On', icon:"st.Appliances.appliances11"
            state "idle", label:'Fan is Off', icon:"st.Appliances.appliances11"
        }        

        standardTile("modeoff", "device.thermostatMode", width: 3, height: 2, inactiveLabel: false, decoration: "flat") {
            state "off", label: '', action:"modeoff", icon:"st.thermostat.heating-cooling-off"
        }
        standardTile("modeheat", "device.thermostatMode", width: 3, height: 2, inactiveLabel: false, decoration: "flat") {
            state "heat", label:'', action:"modeheat", icon:"st.thermostat.heat"
        }
        standardTile("modecool", "device.thermostatMode", width: 3, height: 2, inactiveLabel: false, decoration: "flat") {
            state "cool", label:'', action:"modecool", icon:"st.thermostat.cool"
        }
        standardTile("modeauto", "device.thermostatMode", width: 3, height: 2, inactiveLabel: false, decoration: "flat") {
            state "cool", label:'', action:"modeauto", icon:"st.thermostat.auto"
        }

        standardTile("heatLevelUp", "device.heatingSetpoint", width: 1, height: 1, inactiveLabel: false, decoration: "flat") {
            state "heatLevelUp", label:'', action:"heatLevelUp", icon:"st.thermostat.thermostat-up", backgroundColor:"#d04e00"
        }
		standardTile("heatLevelDown", "device.heatingSetpoint", width: 1, height: 1, inactiveLabel: false, decoration: "flat") {
            state "heatLevelDown", label:'', action:"heatLevelDown", icon:"st.thermostat.thermostat-down", backgroundColor:"#d04e00"
        }
        valueTile("heatingSetpoint", "device.heatingSetpoint", width: 2, height: 2, inactiveLabel: false) {
			state "heat", label:'${currentValue}°', unit:"F",
            	backgroundColors:[
					[value: 40, color: "#f49b88"],
					[value: 50, color: "#f28770"],
					[value: 60, color: "#f07358"],
					[value: 70, color: "#ee5f40"],
					[value: 80, color: "#ec4b28"],
					[value: 90, color: "#ea3811"]
				]
		}
		controlTile("heatSliderControl", "device.heatingSetpoint", "slider", height: 2, width: 4, inactiveLabel: false, range:"(60..90)") {
			state "setHeatingSetpoint", action:"quickSetHeat", backgroundColor:"#d04e00"
		}

        standardTile("coolLevelUp", "device.coolingSetpoint", width: 1, height: 1, inactiveLabel: false, decoration: "flat") {
            state "coolLevelUp", label:'', action:"coolLevelUp", icon:"st.thermostat.thermostat-up", backgroundColor: "#1e9cbb"
        }
		standardTile("coolLevelDown", "device.coolingSetpoint", width: 1, height: 1, inactiveLabel: false, decoration: "flat") {
            state "coolLevelDown", label:'', action:"coolLevelDown", icon:"st.thermostat.thermostat-down", backgroundColor: "#1e9cbb"
        }
		valueTile("coolingSetpoint", "device.coolingSetpoint", width: 2, height: 2, inactiveLabel: false) {
			state "cool", label:'${currentValue}°', unit:"F",
            	backgroundColors:[
					[value: 40, color: "#88e1f4"],
					[value: 50, color: "#70dbf2"],
					[value: 60, color: "#58d5f0"],
					[value: 70, color: "#40cfee"],
					[value: 80, color: "#28c9ec"],
					[value: 90, color: "#11c3ea"]
				]
		}
		controlTile("coolSliderControl", "device.coolingSetpoint", "slider", height: 2, width: 4, inactiveLabel: false, range:"(60..90)") {
			state "setCoolingSetpoint", action:"quickSetCool", backgroundColor: "#1e9cbb"
		}
       
        standardTile("fanauto", "device.thermostatFanMode", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
            state "fanauto", label:'', action:"fanauto", icon:"st.thermostat.fan-auto"
        }
        standardTile("fanon", "device.thermostatFanMode", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
            state "fanon", label:'', action:"fanon", icon:"st.thermostat.fan-on"
        }
        standardTile("fancir", "device.thermostatFanMode", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
            state "fancir", label:'', action:"fancir", icon:"st.thermostat.fan-circulate"
        }

        standardTile("modefan", "device.currentfanMode", width: 2, height: 2, canChangeIcon: false, inactiveLabel: false, decoration: "flat") {
            state ("default", label:'${currentValue}', icon:"st.Appliances.appliances11")
        }
		standardTile("refresh", "device.thermostatMode", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
			state "default", action:"refresh.refresh", icon:"st.secondary.refresh"
		}
		standardTile("configure", "device.needUpdate", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
            state "NO" , label:'', action:"configuration.configure", icon:"st.secondary.configure"
            state "YES", label:'', action:"configuration.configure", icon:"https://github.com/erocm123/SmartThingsPublic/raw/master/devicetypes/erocm123/qubino-flush-1d-relay.src/configure@2x.png"
        }
        
        valueTile("battery", "device.battery", decoration: "flat", width: 2, height: 2) {
			state "battery", label:'${currentValue}% battery', unit:""
		}

        preferences {
            input description: "Once you change values on this page, the corner of the \"configuration\" icon will change orange until all configuration parameters are updated.", title: "Settings", displayDuringSetup: false, type: "paragraph", element: "paragraph"
		    generate_preferences(configuration_model())
        }

		main "temperature"
        details(["temperature", 
        "heatSliderControl", "heatingSetpoint", 
        "coolSliderControl", "coolingSetpoint", 
        "fanon", "fanauto", "fancir", 
        "modeoff", "modeheat", 
        "modecool", "modeauto",
        "battery", "refresh", "configure"])
	}
}

def parse(String description)
{   
	def map = createEvent(zwaveEvent(zwave.parse(description, [0x42:1, 0x43:2, 0x31: 3])))
	if (!map) {
		return null
	}

	if (map.name == "thermostatFanMode"){
		if (map.value == "fanAuto") {
        	sendEvent(name: "currentfanMode", value: "Auto Mode" as String)
	    }
	    if (map.value == "fanOn") {
	        	sendEvent(name: "currentfanMode", value: "On Mode" as String)
		}
	    if (map.value == "fanCirculate") {
	        	sendEvent(name: "currentfanMode", value: "Cycle Mode" as String)
 	   	}
	}

	def result = []
    result += map
	if (map.isStateChange && map.name in ["heatingSetpoint","coolingSetpoint","thermostatMode"]) {
		def map2 = [
			name: "thermostatSetpoint",
			unit: getTemperatureScale()
		]
		if (map.name == "thermostatMode") {
			state.lastTriedMode = map.value
			if (map.value == "cool") {
				map2.value = device.latestValue("coolingSetpoint")
				log.info "THERMOSTAT, latest cooling setpoint = ${map2.value}"
			}
			else {
				map2.value = device.latestValue("heatingSetpoint")
				log.info "THERMOSTAT, latest heating setpoint = ${map2.value}"
			}
		}
		else {
			def mode = device.latestValue("thermostatMode")
			log.info "THERMOSTAT, latest mode = ${mode}"
			if ((map.name == "heatingSetpoint" && mode == "heat") || (map.name == "coolingSetpoint" && mode == "cool")) {
				map2.value = map.value
				map2.unit = map.unit
			}
		}
		if (map2.value != null) {
			logging("THERMOSTAT, adding setpoint event: $map")
			result += createEvent(map2)
		}
	} else if (map.name == "thermostatFanMode" && map.isStateChange) {
		state.lastTriedFanMode = map.value
	}
    
    if (!state.lastBatteryReport || (now() - state.lastBatteryReport) / 60000 >= 60 * 24)
    {
        logging("Over 24hr since last battery report. Requesting report")
        result += response(commands(zwave.batteryV1.batteryGet()))
    }
   
	logging("Parse returned $result")
    return result
}

def zwaveEvent(physicalgraph.zwave.commands.thermostatsetpointv2.ThermostatSetpointReport cmd)
{
	def cmdScale = cmd.scale == 1 ? "F" : "C"
	def map = [:]
	map.value = convertTemperatureIfNeeded(cmd.scaledValue, cmdScale, cmd.precision)
	map.unit = getTemperatureScale()
	map.displayed = false
	switch (cmd.setpointType) {
		case 1:
			map.name = "heatingSetpoint"
			break;
		case 2:
			map.name = "coolingSetpoint"
			break;
		default:
			return [:]
	}
	// So we can respond with same format
	state.size = cmd.size
	state.scale = cmd.scale
	state.precision = cmd.precision
	map
}

def zwaveEvent(physicalgraph.zwave.commands.sensormultilevelv3.SensorMultilevelReport cmd)
{
	def map = [:]
	if (cmd.sensorType == 1) {
		map.value = convertTemperatureIfNeeded(cmd.scaledSensorValue, cmd.scale == 1 ? "F" : "C", cmd.precision)
		map.unit = getTemperatureScale()
		map.name = "temperature"
	} else if (cmd.sensorType == 5) {
		map.value = cmd.scaledSensorValue
		map.unit = "%"
		map.name = "humidity"
	}
	map
}

def zwaveEvent(physicalgraph.zwave.commands.batteryv1.BatteryReport cmd) {
    logging("Battery Report: $cmd")
	def map = [ name: "battery", unit: "%" ]
	if (cmd.batteryLevel == 0xFF) {
		map.value = 1
		map.descriptionText = "${device.displayName} battery is low"
		map.isStateChange = true
	} else {
		map.value = cmd.batteryLevel
	}
	state.lastBatteryReport = now()
	createEvent(map)
}

def zwaveEvent(physicalgraph.zwave.commands.thermostatoperatingstatev1.ThermostatOperatingStateReport cmd)
{
	def map = [:]
	switch (cmd.operatingState) {
		case physicalgraph.zwave.commands.thermostatoperatingstatev1.ThermostatOperatingStateReport.OPERATING_STATE_IDLE:
			map.value = "idle"
            sendEvent(name: "currentState", value: "Idle" as String)
			def mode = device.latestValue("thermostatMode")
            if (mode == "off") {
				sendEvent(name: "currentState", value: "Off" as String)
			}
            if (mode == "aux") {
				sendEvent(name: "currentState", value: "in AUX/EM Mode and is idle" as String)
			}
            if (mode == "heat") {
				sendEvent(name: "currentState", value: "in Heat Mode and is idle" as String)
			}
            if (mode == "cool") {
				sendEvent(name: "currentState", value: "in A/C Mode and is idle" as String)
			}
            if (mode == "auto") {
				sendEvent(name: "currentState", value: "in Auto Mode and is idle" as String)
			}
			break
		case physicalgraph.zwave.commands.thermostatoperatingstatev1.ThermostatOperatingStateReport.OPERATING_STATE_HEATING:
			map.value = "heating"
            sendEvent(name: "currentState", value: "Heating and is running" as String)
			break
		case physicalgraph.zwave.commands.thermostatoperatingstatev1.ThermostatOperatingStateReport.OPERATING_STATE_COOLING:
			map.value = "cooling"
            sendEvent(name: "currentState", value: "Cooling and is running" as String)
			break
		case physicalgraph.zwave.commands.thermostatoperatingstatev1.ThermostatOperatingStateReport.OPERATING_STATE_FAN_ONLY:
			map.value = "fan only"
            sendEvent(name: "currentState", value: "Fan Only Mode" as String)
			break
		case physicalgraph.zwave.commands.thermostatoperatingstatev1.ThermostatOperatingStateReport.OPERATING_STATE_PENDING_HEAT:
			map.value = "pending heat"
            sendEvent(name: "currentState", value: "Pending Heat Mode" as String)
			break
		case physicalgraph.zwave.commands.thermostatoperatingstatev1.ThermostatOperatingStateReport.OPERATING_STATE_PENDING_COOL:
			map.value = "pending cool"
            sendEvent(name: "currentState", value: "Pending A/C Mode" as String)
			break
		case physicalgraph.zwave.commands.thermostatoperatingstatev1.ThermostatOperatingStateReport.OPERATING_STATE_VENT_ECONOMIZER:
			map.value = "vent economizer"
            sendEvent(name: "currentState", value: "Vent Eco Mode" as String)
			break
	}
	map.name = "thermostatOperatingState"
	map
}

def zwaveEvent(physicalgraph.zwave.commands.thermostatfanstatev1.ThermostatFanStateReport cmd) {
	def map = [name: "thermostatFanState", unit: ""]
	switch (cmd.fanOperatingState) {
		case 0:
			map.value = "idle"
			break
		case 1:
			map.value = "running"
			break
		case 2:
			map.value = "running high"
			break
	}
	map
}

def zwaveEvent(physicalgraph.zwave.commands.thermostatmodev2.ThermostatModeReport cmd) {
	def map = [name: "thermostatMode", data:[supportedThermostatModes: state.supportedModes]]
	switch (cmd.mode) {
		case physicalgraph.zwave.commands.thermostatmodev2.ThermostatModeReport.MODE_OFF:
			map.value = "off"
            sendEvent(name: "currentMode", value: "off" as String)
			break
		case physicalgraph.zwave.commands.thermostatmodev2.ThermostatModeReport.MODE_HEAT:
			map.value = "heat"
            sendEvent(name: "currentMode", value: "heat" as String)
			break
		case physicalgraph.zwave.commands.thermostatmodev2.ThermostatModeReport.MODE_AUXILIARY_HEAT:
			map.value = "emergency heat"
            sendEvent(name: "currentMode", value: "aux" as String)
			break
		case physicalgraph.zwave.commands.thermostatmodev2.ThermostatModeReport.MODE_COOL:
			map.value = "cool"
            sendEvent(name: "currentMode", value: "cool" as String)
			break
		case physicalgraph.zwave.commands.thermostatmodev2.ThermostatModeReport.MODE_AUTO:
			map.value = "auto"
            sendEvent(name: "currentMode", value: "auto" as String)
			break
	}
	sendEvent(map)
}

def zwaveEvent(physicalgraph.zwave.commands.thermostatfanmodev3.ThermostatFanModeReport cmd) {
	def map = [name: "thermostatFanMode", data:[supportedThermostatFanModes: state.supportedFanModes]]
	switch (cmd.fanMode) {
		case physicalgraph.zwave.commands.thermostatfanmodev3.ThermostatFanModeReport.FAN_MODE_AUTO_LOW:
			map.value = "auto"
            sendEvent(name: "currentfanMode", value: "Auto Mode" as String)
			break
		case physicalgraph.zwave.commands.thermostatfanmodev3.ThermostatFanModeReport.FAN_MODE_LOW:
			map.value = "on"
            sendEvent(name: "currentfanMode", value: "On Mode" as String)
			break
		case physicalgraph.zwave.commands.thermostatfanmodev3.ThermostatFanModeReport.FAN_MODE_CIRCULATION:
			map.value = "circulate"
            sendEvent(name: "currentfanMode", value: "Cycle Mode" as String)
			break
	}
	sendEvent(map)
}

def zwaveEvent(physicalgraph.zwave.commands.thermostatmodev2.ThermostatModeSupportedReport cmd) {
	def supportedModes = []
	if(cmd.off) { supportedModes << "off" }
	if(cmd.heat) { supportedModes << "heat" }
	if(cmd.cool) { supportedModes << "cool" }
	if(cmd.auto) { supportedModes << "auto" }
	if(cmd.auxiliaryemergencyHeat) { supportedModes << "emergency heat" }

	state.supportedModes = supportedModes
	sendEvent(name: "supportedThermostatModes", value: supportedModes, displayed: false)
}

def zwaveEvent(physicalgraph.zwave.commands.thermostatfanmodev3.ThermostatFanModeSupportedReport cmd) {
	def supportedFanModes = []
	if(cmd.auto) { supportedFanModes << "auto" }
	if(cmd.circulation) { supportedFanModes << "circulate" }
	if(cmd.low) { supportedFanModes << "on" }

	state.supportedFanModes = supportedFanModes
	sendEvent(name: "supportedThermostatFanModes", value: supportedFanModes, displayed: false)
}

def updateState(String name, String value) {
	state[name] = value
	device.updateDataValue(name, value)
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd) {
	logging("Zwave event received: $cmd")
}

def zwaveEvent(physicalgraph.zwave.commands.configurationv2.ConfigurationReport cmd) {
    update_current_properties(cmd)
    logging("${device.displayName} parameter '${cmd.parameterNumber}' with a byte size of '${cmd.size}' is set to '${cmd2Integer(cmd.configurationValue)}'")
} 

def zwaveEvent(physicalgraph.zwave.Command cmd) {
	log.warn "Unhandled zwave command $cmd"
}

def integer2Cmd(value, size) {
	switch(size) {
	case 1:
		[value]
    break
	case 2:
    	def short value1   = value & 0xFF
        def short value2 = (value >> 8) & 0xFF
        [value2, value1]
    break
	case 4:
    	def short value1 = value & 0xFF
        def short value2 = (value >> 8) & 0xFF
        def short value3 = (value >> 16) & 0xFF
        def short value4 = (value >> 24) & 0xFF
		[value4, value3, value2, value1]
	break
	}
}

def cmd2Integer(array) { 

switch(array.size()) {
	case 1:
		array[0]
    break
	case 2:
    	((array[0] & 0xFF) << 8) | (array[1] & 0xFF)
    break
    case 3:
    	((array[0] & 0xFF) << 16) | ((array[1] & 0xFF) << 8) | (array[2] & 0xFF)
    break
	case 4:
    	((array[0] & 0xFF) << 24) | ((array[1] & 0xFF) << 16) | ((array[2] & 0xFF) << 8) | (array[3] & 0xFF)
	break
    }
}

def heatLevelUp(){
    int nextLevel = device.currentValue("heatingSetpoint") + 1
    
    if( nextLevel > 90){
    	nextLevel = 90
    }
    logging("Setting heat set point up to: ${nextLevel}")
    setHeatingSetpoint(nextLevel)
}

def heatLevelDown(){
    int nextLevel = device.currentValue("heatingSetpoint") - 1
    
    if( nextLevel < 40){
    	nextLevel = 40
    }
    logging("Setting heat set point down to: ${nextLevel}")
    setHeatingSetpoint(nextLevel)
}

def quickSetHeat(degrees) {
	setHeatingSetpoint(degrees, 2000)
}

def setHeatingSetpoint(degrees, delay = 2000) {
	setHeatingSetpoint(degrees.toDouble(), delay)
}

def setHeatingSetpoint(Double degrees, Integer delay = 2000) {
	log.trace "setHeatingSetpoint($degrees, $delay)"
	def deviceScale = state.scale ?: 1
	def deviceScaleString = deviceScale == 2 ? "C" : "F"
    def locationScale = getTemperatureScale()
	def p = (state.precision == null) ? 1 : state.precision
    def convertedDegrees
    if (locationScale == "C" && deviceScaleString == "F") {
    	convertedDegrees = celsiusToFahrenheit(degrees)
    } else if (locationScale == "F" && deviceScaleString == "C") {
    	convertedDegrees = fahrenheitToCelsius(degrees)
    } else {
    	convertedDegrees = degrees
    }
    state.heat = convertedDegrees
    sendEvent(name:"heatingSetpoint", value: convertedDegrees)
    if (device.currentValue("thermostatMode") == null || device.currentValue("thermostatMode") == "heat" || device.currentValue("thermostatMode") == "auto") { 
		commands([
			zwave.thermostatSetpointV1.thermostatSetpointSet(setpointType: 1, scale: deviceScale, precision: p, scaledValue: convertedDegrees),
			zwave.thermostatSetpointV1.thermostatSetpointGet(setpointType: 1)
		], delay)
    }
}

def coolLevelUp(){
    int nextLevel = device.currentValue("coolingSetpoint") + 1
    
    if( nextLevel > 99){
    	nextLevel = 99
    }
    logging("Setting cool set point up to: ${nextLevel}")
    setCoolingSetpoint(nextLevel)
}

def coolLevelDown(){
    int nextLevel = device.currentValue("coolingSetpoint") - 1
    
    if( nextLevel < 50){
    	nextLevel = 50
    }
    logging("Setting cool set point down to: ${nextLevel}")
    setCoolingSetpoint(nextLevel)
}

def quickSetCool(degrees) {
	setCoolingSetpoint(degrees, 2000)
}

def setCoolingSetpoint(degrees, delay = 2000) {
	setCoolingSetpoint(degrees.toDouble(), delay)
}

def setCoolingSetpoint(Double degrees, Integer delay = 2000) {
    log.trace "setCoolingSetpoint($degrees, $delay)"
	def deviceScale = state.scale ?: 1
	def deviceScaleString = deviceScale == 2 ? "C" : "F"
    def locationScale = getTemperatureScale()
	def p = (state.precision == null) ? 1 : state.precision
    def convertedDegrees
    if (locationScale == "C" && deviceScaleString == "F") {
    	convertedDegrees = celsiusToFahrenheit(degrees)
    } else if (locationScale == "F" && deviceScaleString == "C") {
    	convertedDegrees = fahrenheitToCelsius(degrees)
    } else {
    	convertedDegrees = degrees
    }
    state.cool = convertedDegrees
    sendEvent(name:"coolingSetpoint", value: convertedDegrees)
    if (device.currentValue("thermostatMode") == null || device.currentValue("thermostatMode") == "cool" || device.currentValue("thermostatMode") == "auto") { 
		commands([
			zwave.thermostatSetpointV1.thermostatSetpointSet(setpointType: 2, scale: deviceScale, precision: p,  scaledValue: convertedDegrees),
			zwave.thermostatSetpointV1.thermostatSetpointGet(setpointType: 2)
		], delay)
    }
}

def modeoff() {
	logging("Setting thermostat mode to OFF.")
	commands([
		zwave.thermostatModeV2.thermostatModeSet(mode: 0),
		zwave.thermostatModeV2.thermostatModeGet()
	])
}

def modeheat() {
	logging("Setting thermostat mode to HEAT.")
	commands([
		zwave.thermostatModeV2.thermostatModeSet(mode: 1),
        zwave.thermostatSetpointV1.thermostatSetpointSet(setpointType: 1, scale: 1, precision: 1,  scaledValue: state.heat?state.heat:65),
		zwave.thermostatSetpointV1.thermostatSetpointGet(setpointType: 1),
		zwave.thermostatModeV2.thermostatModeGet()
	])
}

def modecool() {
	logging("Setting thermostat mode to COOL.")
	commands([
		zwave.thermostatModeV2.thermostatModeSet(mode: 2),
        zwave.thermostatSetpointV1.thermostatSetpointSet(setpointType: 2, scale: 1, precision: 1,  scaledValue: state.cool?state.cool:80),
		zwave.thermostatSetpointV1.thermostatSetpointGet(setpointType: 2),
		zwave.thermostatModeV2.thermostatModeGet()
	])
}

def modeauto() {
	logging("Setting thermostat mode to AUTO.")
	commands([
		zwave.thermostatModeV2.thermostatModeSet(mode: 3),
		zwave.thermostatModeV2.thermostatModeGet()
	])
}

def modeemgcyheat() {
	commands([
		zwave.thermostatModeV2.thermostatModeSet(mode: 4),
		zwave.thermostatModeV2.thermostatModeGet()
	])
}
def fanon() {
	commands([
		zwave.thermostatFanModeV3.thermostatFanModeSet(fanMode: 1),
		zwave.thermostatFanModeV3.thermostatFanModeGet()
	])
}

def fanauto() {
	commands([
		zwave.thermostatFanModeV3.thermostatFanModeSet(fanMode: 0),
		zwave.thermostatFanModeV3.thermostatFanModeGet()
	])
}

def fancir() {
	commands([
		zwave.thermostatFanModeV3.thermostatFanModeSet(fanMode: 6),
		zwave.thermostatFanModeV3.thermostatFanModeGet()
	])
}

private def logging(message) {
    if (state.enableDebugging == null || state.enableDebugging == "true") log.debug "$message"
}

def updated()
{
	state.enableDebugging = settings.enableDebugging
    logging("updated() is being called")
    sendEvent(name: "checkInterval", value: 2 * 15 * 60 + 2 * 60, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])
    def cmds = update_needed_settings()
    cmds << zwave.thermostatModeV2.thermostatModeSupportedGet()
    cmds << zwave.thermostatFanModeV2.thermostatFanModeSupportedGet()

    sendEvent(name:"needUpdate", value: device.currentValue("needUpdate"), displayed:false, isStateChange: true)
    
    if (cmds != []) response(commands(cmds, 2000))
}

def setThermostatMode(mode) {
    switch (mode) {
        case "auto":
            modeauto()
        break;
        case "cool":
            modecool()
        break;
        case "emergency heat":
            modeemgcyheat()
        break;
        case "heat":
            modeheat()
        break;
        case "off":
            modeoff()
        break;
    }
}

def setThermostatFanMode(mode) {
    switch (mode) {
        case "auto":
            fanauto()
        break;
        case "on":
            fanon()
        break;
        case "circulate":
            fancir()
        break;
    }
}

def configure() {
	state.enableDebugging = settings.enableDebugging
    logging("Configuring Device For SmartThings Use")
    def cmds = []

    cmds = update_needed_settings()    
    
    cmds << zwave.thermostatModeV2.thermostatModeSupportedGet()
    cmds << zwave.thermostatFanModeV2.thermostatFanModeSupportedGet()
	cmds << zwave.associationV1.associationSet(groupingIdentifier:1, nodeId:[zwaveHubNodeId])

	if(!device.currentValue("temperature") || device.currentValue("humidity")) cmds << zwave.sensorMultilevelV3.sensorMultilevelGet() // current temperature
	if(!device.currentValue("coolingSetPoint")) cmds << zwave.thermostatSetpointV1.thermostatSetpointGet(setpointType: 1)
	if(!device.currentValue("heatingSetPoint")) cmds << zwave.thermostatSetpointV1.thermostatSetpointGet(setpointType: 2)
	if(!device.currentValue("thermostatMode")) cmds << zwave.thermostatModeV2.thermostatModeGet()
	if(!device.currentValue("thermostatFanState")) cmds << zwave.thermostatFanStateV1.thermostatFanStateGet()
	if(!device.currentValue("thermostatFanMode"))  cmds << zwave.thermostatFanModeV3.thermostatFanModeGet()
	if(!device.currentValue("thermostatOperatingState")) zwave.thermostatOperatingStateV1.thermostatOperatingStateGet()
    if(!device.currentValue("battery")) cmds << zwave.batteryV1.batteryGet()
    
    if (cmds != []) commands(cmds, 2000)
}

def refresh() {
    logging("refresh()")
	commands([
		zwave.sensorMultilevelV3.sensorMultilevelGet(), // current temperature
		zwave.thermostatSetpointV1.thermostatSetpointGet(setpointType: 1),
		zwave.thermostatSetpointV1.thermostatSetpointGet(setpointType: 2),
		zwave.thermostatModeV2.thermostatModeGet(),
		zwave.thermostatFanStateV1.thermostatFanStateGet(),
		zwave.thermostatFanModeV3.thermostatFanModeGet(),
		zwave.thermostatOperatingStateV1.thermostatOperatingStateGet(),
        zwave.batteryV1.batteryGet(),
	], 2000)
}

def ping() {
    logging("ping()")
	return commands(zwave.sensorMultilevelV3.sensorMultilevelGet())
}

private commands(commands, delay=2000) {
	delayBetween(commands.collect{ command(it) }, delay)
}

private command(physicalgraph.zwave.Command cmd) {
	if (getZwaveInfo()?.zw?.contains("s")) {
        zwave.securityV1.securityMessageEncapsulation().encapsulate(cmd).format()
    } else {
        cmd.format()
    }
}

private getStandardDelay() {
	2000
}

def tempUp() {
    log.debug "tempUp()"
	def operMode = device.currentValue("thermostatMode")
	def curTemp = device.currentValue("temperature").toInteger()
	switch (operMode) {
    	case "heat":
        	setHeatingSetpoint(getHeatTemp().toInteger() + 1)
            break;
        case "cool":
        	setCoolingSetpoint(getCoolTemp().toInteger() + 1)
            break;
        case "auto":
            if (settings.threshold == null || settings.threshold == "") settings.threshold = 70
            if (curTemp < settings.threshold) {
                setHeatingSetpoint(getHeatTemp().toInteger() + 1)
            } else {
                setCoolingSetpoint(getCoolTemp().toInteger() + 1)
            }
        	break;
        default:
        	break;
    }
}

def tempDown() {
    log.debug "tempDown"
	def operMode = device.currentValue("thermostatMode")
	def curTemp = device.currentValue("temperature").toInteger()
	switch (operMode) {
    	case "heat":
        	setHeatingSetpoint(getHeatTemp().toInteger() - 1)
            break;
        case "cool":
        	setCoolingSetpoint(getCoolTemp().toInteger() - 1)
            break;
        case "auto":
            if (settings.threshold == null || settings.threshold == "") settings.threshold = 70
            if (curTemp < settings.threshold) {
                setHeatingSetpoint(getHeatTemp().toInteger() - 1)
            } else {
                setCoolingSetpoint(getCoolTemp().toInteger() - 1)
            }
        	break;
        default:
        	break;
    }
}

def setTemperature(value) {
	def operMode = device.currentValue("thermostatMode")
	def curTemp = device.currentValue("temperature").toInteger()
	def newCTemp
	def newHTemp 
	switch (operMode) {
    	case "heat":
        	(value < curTemp) ? (newHTemp = getHeatTemp().toInteger() - 1) : (newHTemp = getHeatTemp().toInteger() + 1)
        	setHeatingSetpoint(newHTemp.toInteger())
            break;
        case "cool":
        	(value < curTemp) ? (newCTemp = getCoolTemp().toInteger() - 1) : (newCTemp = getCoolTemp().toInteger() + 1)
        	setCoolingSetpoint(newCTemp.toInteger())
            break;
        case "auto":
            if (settings.threshold == null || settings.threshold == "") settings.threshold = 70
            if (curTemp < settings.threshold) {
                (value < curTemp) ? (newHTemp = getHeatTemp().toInteger() - 1) : (newHTemp = getHeatTemp().toInteger() + 1)
                setHeatingSetpoint(newHTemp.toInteger())
            } else {
                (value < curTemp) ? (newCTemp = getCoolTemp().toInteger() - 1) : (newCTemp = getCoolTemp().toInteger() + 1)
                setCoolingSetpoint(newCTemp.toInteger())
            }
        	
            /*def cmds = []
            cmds << setHeatingSetpoint(newHTemp.toInteger())
            cmds << "delay 1000"
            cmds << setCoolingSetpoint(newCTemp.toInteger())
            return cmds*/
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

def generate_preferences(configuration_model)
{
    def configuration = parseXml(configuration_model)
   
    configuration.Value.each
    {
        if(it.@hidden != "true" && it.@disabled != "true"){
        switch(it.@type)
        {   
            case ["byte","short","four","number"]:
                input "${it.@index}", "number",
                    title:"${it.@label}\n" + "${it.Help}",
                    range: "${it.@min}..${it.@max}",
                    defaultValue: "${it.@value}",
                    displayDuringSetup: "${it.@displayDuringSetup}"
            break
            case "list":
                def items = []
                it.Item.each { items << ["${it.@value}":"${it.@label}"] }
                input "${it.@index}", "enum",
                    title:"${it.@label}\n" + "${it.Help}",
                    defaultValue: "${it.@value}",
                    displayDuringSetup: "${it.@displayDuringSetup}",
                    options: items
            break
            case "decimal":
               input "${it.@index}", "decimal",
                    title:"${it.@label}\n" + "${it.Help}",
                    range: "${it.@min}..${it.@max}",
                    defaultValue: "${it.@value}",
                    displayDuringSetup: "${it.@displayDuringSetup}"
            break
            case "boolean":
               input "${it.@index}", "boolean",
                    title: it.@label != "" ? "${it.@label}\n" + "${it.Help}" : "" + "${it.Help}",
                    defaultValue: "${it.@value}",
                    displayDuringSetup: "${it.@displayDuringSetup}"
            break
        }
        }
    }
}

def convertParam(number, value) {
	switch (number){
        case [5, 6, 7, 10, 15, 16, 17, 18, 31, 32, 33]:
        	(value * 256 + 704643072).toInteger()
        break
        default:
        	value.toInteger()
        break
    }
}

def update_current_properties(cmd)
{
    def currentProperties = state.currentProperties ?: [:]
    
    currentProperties."${cmd.parameterNumber}" = cmd.configurationValue

    if (settings."${cmd.parameterNumber}" != null)
    {
        if (convertParam(cmd.parameterNumber, settings."${cmd.parameterNumber}") == cmd2Integer(cmd.configurationValue))
        {
            sendEvent(name:"needUpdate", value:"NO", displayed:false, isStateChange: true)
        }
        else
        {
            sendEvent(name:"needUpdate", value:"YES", displayed:false, isStateChange: true)
        }
    }

    state.currentProperties = currentProperties
}

def update_needed_settings()
{
    def cmds = []
    def currentProperties = state.currentProperties ?: [:]
     
    def configuration = parseXml(configuration_model())
    def isUpdateNeeded = "NO"
    
    configuration.Value.each
    {     
        if ("${it.@setting_type}" == "zwave"){
            if (currentProperties."${it.@index}" == null)
            {
                if (device.currentValue("currentFirmware") == null || "${it.@fw}".indexOf(device.currentValue("currentFirmware")) >= 0){
                    isUpdateNeeded = "YES"
                    logging("Current value of parameter ${it.@index} is unknown")
                    cmds << zwave.configurationV1.configurationGet(parameterNumber: it.@index.toInteger())
                }
            }
            else if ((settings."${it.@index}" != null || "${it.@type}" == "hidden") && cmd2Integer(currentProperties."${it.@index}") != convertParam(it.@index.toInteger(), settings."${it.@index}"? settings."${it.@index}" : "${it.@value}"))
            { 
                isUpdateNeeded = "YES"
                logging("Parameter ${it.@index} will be updated to " + convertParam(it.@index.toInteger(), settings."${it.@index}"? settings."${it.@index}" : "${it.@value}"))
                def convertedConfigurationValue = convertParam(it.@index.toInteger(), settings."${it.@index}"? settings."${it.@index}" : "${it.@value}")
                cmds << zwave.configurationV1.configurationSet(configurationValue: integer2Cmd(convertedConfigurationValue, it.@byteSize.toInteger()), parameterNumber: it.@index.toInteger(), size: it.@byteSize.toInteger())
                cmds << zwave.configurationV1.configurationGet(parameterNumber: it.@index.toInteger())
            } 
        }
    }
    
    sendEvent(name:"needUpdate", value: isUpdateNeeded, displayed:false, isStateChange: true)
    return cmds
}

def configuration_model()
{
'''
<configuration>
<Value type="list" byteSize="1" index="1" label="HVAC System Type" min="0" max="1" value="0" setting_type="zwave" fw="">
 <Help>
Range: 0 to 1
Default: 0 (Normal)
</Help>
    <Item label="Normal" value="0" />
    <Item label="Heat Pump" value="1" />
</Value>
<Value type="byte" byteSize="1" index="2" label="Heat Stages" min="0" max="3" value="2" setting_type="zwave" fw="" disabled="true">
 <Help>
Number of Heat Stages
Range: 0 to 3
Default: 2
</Help>
</Value>
<Value type="byte" byteSize="1" index="3" label="Number of Cool Stages" min="0" max="2" value="2" setting_type="zwave" fw="" disabled="true">
 <Help>
Cool Stages
Range: 0 to 2
Default: 2
</Help>
</Value>
<Value type="list" byteSize="1" index="4" label="Heat Fuel Type" min="0" max="1" value="1" setting_type="zwave" fw="">
 <Help>
Range: 0-1
Default: 1 (Electric)
</Help>
    <Item label="Fossil Fuel" value="0" />
    <Item label="Electric" value="1" />
</Value>
<Value type="byte" byteSize="4" index="5" label="Calibration Temperature" min="-100" max="100" value="0" setting_type="zwave" fw="" disabled="false">
 <Help>
Calibration Temperature Range (in deg. F) Precision is tenths of a degree.
Range: -100 to 100
Default: 0
</Help>
</Value>
<Value type="byte" byteSize="4" index="6" label="Overshoot" min="0" max="30" value="5" setting_type="zwave" fw="">
 <Help>
Overshoot Range (in deg. F)  Precision is tenths of a degree.
Range: 0 to 30
Default: 5
</Help>
</Value>
<Value type="byte" byteSize="4" index="7" label="Swing" min="0" max="30" value="0" setting_type="zwave" fw="" disabled="true">
 <Help>
Swing Range (in deg. F) Precision is tenths of a degree.
Range: 0 to 30
Default: 0
</Help>
</Value>
<Value type="byte" byteSize="1" index="8" label="Heat Staging Delay" min="1" max="60" value="10" setting_type="zwave" fw="" disabled="true">
 <Help>
Heat Staging Delay (in min)
Range: 1 to 60
Default: 10
</Help>
</Value>
<Value type="byte" byteSize="1" index="9" label="Cool Staging Delay" min="1" max="60" value="10" setting_type="zwave" fw="" disabled="true">
 <Help>
Cool Staging Delay (in min)
Range: 1 to 60
Default: 10
</Help>
</Value>
<Value type="byte" byteSize="4" index="10" label="Balance Setpoint" min="0" max="950" value="350" setting_type="zwave" fw="" disabled="true">
 <Help>
Balance Setpont Range (in deg. F) Precision is tenths of a degree.
Range: 0 to 950
Default: 350
</Help>
</Value>
<Value type="list" byteSize="1" index="11" label="Recovery Settings" min="0" max="1" value="0" setting_type="zwave" fw="" disabled="true">
 <Help>
Range: 0 to 1
Default: 0 (Comfort)
</Help>
    <Item label="Comfort" value="0" />
    <Item label="Efficient" value="1" />
</Value>
<Value type="byte" byteSize="1" index="12" label="Fan Circulation Period" min="10" max="240" value="20" setting_type="zwave" fw="" disabled="true">
 <Help>
Fan Circulation Period (in min)
Range: 10 to 240
Default: 20
</Help>
</Value>
<Value type="byte" byteSize="1" index="13" label="Fan Circulation Duty Cycle" min="0" max="100" value="25" setting_type="zwave" fw="" disabled="true">
 <Help>
Duty Cycle (percentage)
Range: 0 to 100
Default: 25
</Help>
</Value>
<Value type="byte" byteSize="2" index="14" label="Fan Purge Time" min="1" max="3600" value="60" setting_type="zwave" fw="" disabled="true">
 <Help>
Purge Time (in s)
Range: 1 to 3600
Default: 60
</Help>
</Value>
<Value type="byte" byteSize="4" index="15" label="Maximum Heat Setpoint" min="350" max="950" value="950" setting_type="zwave" fw="">
 <Help>
Max Heat Setpoint Range (in deg. F) Precision is tenths of a degree.
Range: 350 to 950
Default: 950
</Help>
</Value>
<Value type="byte" byteSize="4" index="16" label="Minimum Heat Setpoint" min="350" max="950" value="350" setting_type="zwave" fw="">
 <Help>
Min Heat Setpoint Range (in deg. F) Precision is tenths of a degree.
Range: 350 to 950
Default: 350
</Help>
</Value>
<Value type="byte" byteSize="4" index="17" label="Maximum Cool Setpoint" min="500" max="950" value="950" setting_type="zwave" fw="">
 <Help>
Max Cool Setpoint Range(in deg. F) Precision is tenths of a degree.
Range: 500 to 950
Default: 950
</Help>
</Value>
<Value type="byte" byteSize="4" index="18" label="Minimum Cool Setpoint" min="500" max="950" value="500" setting_type="zwave" fw="">
 <Help>
Min Cool Setpoint (in deg. F) Precision is tenths of a degree.
Range: 500 to 950
Default: 500
</Help>
</Value>
<Value type="list" byteSize="1" index="19" label="Thermostat Lock" min="0" max="1" value="0" setting_type="zwave" fw="">
 <Help>
Range: 0 to 1
Default: 0 (Disabled)
</Help>
    <Item label="Disable" value="0" />
    <Item label="Enable" value="1" />
</Value>
<Value type="byte" byteSize="1" index="20" label="Compressor Delay" min="0" max="60" value="5" setting_type="zwave" fw="" disabled="true">
 <Help>
Compressor Delay (in min)
Range: 0 to 60
Default: 5
</Help>
</Value>
<Value type="byte" byteSize="1" index="21" label="Demand Response Period" min="10" max="240" value="10" setting_type="zwave" fw="" disabled="true">
 <Help>
Demand Response Period (in min)
Range: 10 to 240
Default: 10
</Help>
</Value>
<Value type="byte" byteSize="1" index="22" label="Demand Response Duty Cycle" min="0" max="100" value="25" setting_type="zwave" fw="" disabled="true">
 <Help>
Demand Response Duty Cycle (percentage)
Range: 0 to 100
Default: 25
</Help>
</Value>
<Value type="list" byteSize="1" index="23" label="Temperature Display Units" min="0" max="1" value="1" setting_type="zwave" fw="">
 <Help>
Range: 0 to 1
Default: 1 (Farenheit)
</Help>
    <Item label="Celsius" value="0" />
    <Item label="Farenheit" value="1" />
</Value>
<Value type="list" byteSize="1" index="24" label="HVAC Modes Enabled" min="3" max="31" value="15" setting_type="zwave" fw="">
 <Help>
Range: 3, 5, 7, 15, 31, 23, 19
Default: 15 (Off, Heat, Cool, Auto)
</Help>
    <Item label="Off, Heat" value="3" />
    <Item label="Off, Cool" value="5" />
    <Item label="Off, Heat, Cool" value="7" />
    <Item label="Off, Heat, Cool, Auto" value="15" />
    <Item label="Off, Heat, Cool, Auto, Emergency Heat" value="31" />
    <Item label="Off, Heat, Cool, Emergency Heat" value="23" />
    <Item label="Off, Heat, Emergency Heat" value="19" />
</Value>
<Value type="list" byteSize="1" index="25" label="Configurable Terminal Setting" min="0" max="3" value="0" setting_type="zwave" fw="" disabled="true">
 <Help>
Range: 0 to 3
Default: 0 (Disabled)
</Help>
    <Item label="Disable" value="0" />
    <Item label="W3" value="1" />
    <Item label="H" value="2" />
    <Item label="DH" value="3" />
</Value>
<Value type="list" byteSize="1" index="26" label="Power Source" min="0" max="1" value="0" setting_type="zwave" fw="" disabled="true">
 <Help>
Range: 0 to 1
Default: 0 (Battery)
</Help>
    <Item label="Battery" value="0" />
    <Item label="C-Wire" value="1" />
</Value>
<Value type="byte" byteSize="1" index="27" label="Battery Alert Threshold Low" min="0" max="100" value="30" setting_type="zwave" fw="">
 <Help>
Battery Alert Range (percentage)
Range: 0 to 100
Default: 30
</Help>
</Value>
<Value type="byte" byteSize="1" index="28" label="Battery Alert Threshold Very Low" min="0" max="100" value="15" setting_type="zwave" fw="">
 <Help>
Very Low Battery Alert Range (percentage)
Range: 0 to 100
Default: 15
</Help>
</Value>
<Value type="list" byteSize="1" index="30" label="Remote Temperature Enable" min="0" max="1" value="0" setting_type="zwave" fw="" disabled="true">
 <Help>
Range: 0 to 1
Default: 0 (Disabled)
</Help>
    <Item label="Disable" value="0" />
    <Item label="Enable" value="1" />
</Value>
<Value type="byte" byteSize="4" index="31" label="Heat Differential" min="10" max="100" value="30" setting_type="zwave" fw="">
 <Help>
Heat Differential (in deg. F)  Precision is tenths of a degree.
Range: 10 to 100
Default: 30
</Help>
</Value>
<Value type="byte" byteSize="4" index="32" label="Cool Differential" min="10" max="100" value="30" setting_type="zwave" fw="">
 <Help>
Cool Differential (in deg. F)  Precision is tenths of a degree.
Range: 10 to 100
Default: 30
</Help>
</Value>
<Value type="byte" byteSize="4" index="33" label="Temperature Reporting Threshold" min="5" max="20" value="10" setting_type="zwave" fw="">
 <Help>
Temperature Reporting Range (in deg. F)  Precision is tenths of a degree.
Range: 5 to 20
Default: 10
</Help>
</Value>
<Value type="list" byteSize="1" index="34" label="O/B Select" min="0" max="1" value="1" setting_type="zwave" fw="" disabled="true">
 <Help>
Range: 0 to 1
Default: 1 (O/B Terminal acts as O terminal, closed when heating)
</Help>
    <Item label="O/B Terminal acts as B terminal, closed when cooling" value="0" />
    <Item label="O/B Terminal acts as O terminal, closed when heating" value="1" />
</Value>
<Value type="list" byteSize="1" index="35" label="Z-Wave Echo Association Reports" min="0" max="1" value="0" setting_type="zwave" fw="" disabled="true">
 <Help>
Range: 0 to 1
Default: 0 (Disabled)
</Help>
    <Item label="Disable" value="0" />
    <Item label="Enable" value="1" />
</Value>
    <Value type="number" index="threshold" label="Auto Mode Threshold" min="0" max="100" value="70" setting_type="preference" fw="">
    <Help>
Temperature in which you do not want your thermostat to heat above or cool below. Used for auto mode adjustments
    </Help>
  </Value>
    <Value type="boolean" index="enableDebugging" label="Enable Debug Logging?" value="true" setting_type="preference" fw="1.04,1.05">
    <Help>
    </Help>
  </Value>
</configuration>
'''
}
