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
 *	Ecobee Thermostat
 *
 *	Author: SmartThings
 *	Date: 2013-06-13
 */
metadata {
	definition (name: "Ecobee Thermostat", namespace: "smartthings", author: "SmartThings") {
		capability "Actuator"
		capability "Thermostat"
		capability "Temperature Measurement"
		capability "Sensor"
		capability "Refresh"
		capability "Relative Humidity Measurement"
		capability "Health Check"

		command "generateEvent"
		command "resumeProgram"
		command "switchMode"
		command "switchFanMode"
		command "lowerHeatingSetpoint"
		command "raiseHeatingSetpoint"
		command "lowerCoolSetpoint"
		command "raiseCoolSetpoint"
		// To satisfy some SA/rules that incorrectly using poll instead of Refresh
		command "poll"

		attribute "thermostat", "string"
		attribute "maxHeatingSetpoint", "number"
		attribute "minHeatingSetpoint", "number"
		attribute "maxCoolingSetpoint", "number"
		attribute "minCoolingSetpoint", "number"
		attribute "deviceTemperatureUnit", "string"
		attribute "deviceAlive", "enum", ["true", "false"]
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
			tileAttribute("device.humidity", key: "SECONDARY_CONTROL") {
				attributeState "humidity", label:'${currentValue}%', icon:"st.Weather.weather12"
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
		standardTile("mode", "device.thermostatMode", width:2, height:2, inactiveLabel: false, decoration: "flat") {
			state "off", action:"switchMode", nextState: "updating", icon: "st.thermostat.heating-cooling-off"
			state "heat", action:"switchMode",  nextState: "updating", icon: "st.thermostat.heat"
			state "cool", action:"switchMode",  nextState: "updating", icon: "st.thermostat.cool"
			state "auto", action:"switchMode",  nextState: "updating", icon: "st.thermostat.auto"
			state "emergency heat", action:"switchMode", nextState: "updating", icon: "st.thermostat.emergency-heat"
			state "updating", label:"Updating...", icon: "st.secondary.secondary"
		}
		standardTile("fanMode", "device.thermostatFanMode", width:2, height:2, inactiveLabel: false, decoration: "flat") {
			state "auto", action:"switchFanMode", nextState: "updating", icon: "st.thermostat.fan-auto"
			state "on", action:"switchFanMode", nextState: "updating", icon: "st.thermostat.fan-on"
			state "updating", label:"Updating...", icon: "st.secondary.secondary"
		}
		valueTile("thermostat", "device.thermostat", width:2, height:1, decoration: "flat") {
			state "thermostat", label:'${currentValue}', backgroundColor:"#ffffff"
		}
		standardTile("refresh", "device.thermostatMode", width:2, height:1, inactiveLabel: false, decoration: "flat") {
			state "default", action:"refresh.refresh", icon:"st.secondary.refresh"
		}
		standardTile("resumeProgram", "device.resumeProgram", width:2, height:1, inactiveLabel: false, decoration: "flat") {
			state "resume", action:"resumeProgram", nextState: "updating", label:'Resume', icon:"st.samsung.da.oven_ic_send"
			state "updating", label:"Working", icon: "st.secondary.secondary"
		}
		main "temperature"
		details(["temperature",  "lowerHeatingSetpoint", "heatingSetpoint", "raiseHeatingSetpoint",
				"lowerCoolSetpoint", "coolingSetpoint", "raiseCoolSetpoint", "mode", "fanMode",
				"thermostat", "resumeProgram", "refresh"])
	}

	preferences {
		input "holdType", "enum", title: "Hold Type",
				description: "When changing temperature, use Temporary (Until next transition) or Permanent hold (default)",
				required: false, options:["Temporary", "Permanent"]
		input "deadbandSetting", "number", title: "Minimum temperature difference between the desired Heat and Cool " +
				"temperatures in Auto mode:\nNote! This must be the same as configured on the thermostat",
				description: "temperature difference °F", defaultValue: 5,
				required: false
	}

}

void installed() {
    // The device refreshes every 5 minutes by default so if we miss 2 refreshes we can consider it offline
    // Using 12 minutes because in testing, device health team found that there could be "jitter"
    sendEvent(name: "checkInterval", value: 60 * 12, data: [protocol: "cloud"], displayed: false)
}

// Device Watch will ping the device to proactively determine if the device has gone offline
// If the device was online the last time we refreshed, trigger another refresh as part of the ping.
def ping() {
    def isAlive = device.currentValue("deviceAlive") == "true" ? true : false
    if (isAlive) {
        refresh()
    }
}

// parse events into attributes
def parse(String description) {
	log.debug "Parsing '${description}'"
}

def refresh() {
	log.debug "refresh"
	poll()
}

void poll() {
	log.debug "Executing 'poll' using parent SmartApp"
	parent.pollChild()
}

def generateEvent(Map results) {
	if(results) {
		def linkText = getLinkText(device)
		def supportedThermostatModes = ["off"]
		def thermostatMode = null
		def locationScale = getTemperatureScale()

		results.each { name, value ->
			def event = [name: name, linkText: linkText, handlerName: name]
			def sendValue = value

			if (name=="temperature" || name=="heatingSetpoint" || name=="coolingSetpoint" ) {
				sendValue =  getTempInLocalScale(value, "F")  // API return temperature values in F
				event << [value: sendValue, unit: locationScale]
			} else if (name=="maxCoolingSetpoint" || name=="minCoolingSetpoint" || name=="maxHeatingSetpoint" || name=="minHeatingSetpoint") {
				// Old attributes, keeping for backward compatibility
				sendValue =  getTempInLocalScale(value, "F")  // API return temperature values in F
				event << [value: sendValue, unit: locationScale, displayed: false]
				// Store min/max setpoint in device unit to avoid conversion rounding error when updating setpoints 
				device.updateDataValue(name+"Fahrenheit", "${value}")
			} else if (name=="heatMode" || name=="coolMode" || name=="autoMode" || name=="auxHeatMode"){
				if (value == true) {
					supportedThermostatModes << ((name == "auxHeatMode") ? "emergency heat" : name - "Mode")
				}
				return // as we don't want to send this event here, proceed to next name/value pair
			} else if (name=="thermostatFanMode"){
				sendEvent(name: "supportedThermostatFanModes", value: fanModes(), displayed: false)
				event << [value: value, data:[supportedThermostatFanModes: fanModes()]]
			} else if (name=="humidity") {
				event << [value: value, displayed: false, unit: "%"]
			} else if (name == "deviceAlive") {
				event['displayed'] = false
			} else if (name == "thermostatMode") {
				thermostatMode = (value == "auxHeatOnly") ? "emergency heat" : value.toLowerCase()
				return // as we don't want to send this event here, proceed to next name/value pair
			} else {
				event << [value: value.toString()]
			}
			event << [descriptionText: getThermostatDescriptionText(name, sendValue, linkText)]
			sendEvent(event)
		}
		if (state.supportedThermostatModes != supportedThermostatModes) {
			state.supportedThermostatModes = supportedThermostatModes
			sendEvent(name: "supportedThermostatModes", value: supportedThermostatModes, displayed: false)
		}
		if (thermostatMode) {
			sendEvent(name: "thermostatMode", value: thermostatMode, data:[supportedThermostatModes:state.supportedThermostatModes], linkText: linkText,
					descriptionText: getThermostatDescriptionText("thermostatMode", thermostatMode, linkText), handlerName: "thermostatMode")
		}
		generateSetpointEvent ()
		generateStatusEvent ()
	}
}

//return descriptionText to be shown on mobile activity feed
private getThermostatDescriptionText(name, value, linkText) {
	if(name == "temperature") {
		return "temperature is ${value}°${location.temperatureScale}"

	} else if(name == "heatingSetpoint") {
		return "heating setpoint is ${value}°${location.temperatureScale}"

	} else if(name == "coolingSetpoint"){
		return "cooling setpoint is ${value}°${location.temperatureScale}"

	} else if (name == "thermostatMode") {
		return "thermostat mode is ${value}"

	} else if (name == "thermostatFanMode") {
		return "thermostat fan mode is ${value}"

	} else if (name == "humidity") {
		return "humidity is ${value} %"
	} else {
		return "${name} = ${value}"
	}
}

void setHeatingSetpoint(setpoint) {
log.debug "***setHeatingSetpoint($setpoint)"
	if (setpoint) {
		state.heatingSetpoint = setpoint.toDouble()
		runIn(2, "updateSetpoints", [overwrite: true])
	}
}

def setCoolingSetpoint(setpoint) {
log.debug "***setCoolingSetpoint($setpoint)"
	if (setpoint) {
		state.coolingSetpoint = setpoint.toDouble()
		runIn(2, "updateSetpoints", [overwrite: true])
	}
}

def updateSetpoints() {
	def deviceScale = "F" //API return/expects temperature values in F
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
	}
	state.heatingSetpoint = null
	state.coolingSetpoint = null
	updateSetpoint(data)
}

void resumeProgram() {
	log.debug "resumeProgram() is called"

	sendEvent("name":"thermostat", "value":"resuming schedule", "description":statusText, displayed: false)
	def deviceId = device.deviceNetworkId.split(/\./).last()
	if (parent.resumeProgram(deviceId)) {
		sendEvent("name":"thermostat", "value":"setpoint is updating", "description":statusText, displayed: false)
		sendEvent("name":"resumeProgram", "value":"resume", descriptionText: "resumeProgram is done", displayed: false, isStateChange: true)
	} else {
		sendEvent("name":"thermostat", "value":"failed resume click refresh", "description":statusText, displayed: false)
		log.error "Error resumeProgram() check parent.resumeProgram(deviceId)"
	}
	runIn(5, "refresh", [overwrite: true])
}

def modes() {
	return state.supportedThermostatModes
}

def fanModes() {
	// Ecobee does not report its supported fanModes; use hard coded values
	["on", "auto"]
}

def switchMode() {
	def currentMode = device.currentValue("thermostatMode")
	def modeOrder = modes()
	if (modeOrder) {
		def next = { modeOrder[modeOrder.indexOf(it) + 1] ?: modeOrder[0] }
		def nextMode = next(currentMode)
		switchToMode(nextMode)
	} else {
		log.warn "supportedThermostatModes not defined"
	}
}

def switchToMode(mode) {
	log.debug "switchToMode: ${mode}"
	def deviceId = device.deviceNetworkId.split(/\./).last()
	// Thermostat's mode for "emergency heat" is "auxHeatOnly"
	if (!(parent.setMode(((mode == "emergency heat") ? "auxHeatOnly" : mode), deviceId))) {
		log.warn "Error setting mode:$mode"
		// Ensure the DTH tile is reset
		generateModeEvent(device.currentValue("thermostatMode"))
	}
	runIn(5, "refresh", [overwrite: true])
}

def switchFanMode() {
	def currentFanMode = device.currentValue("thermostatFanMode")
	def fanModeOrder = fanModes()
	def next = { fanModeOrder[fanModeOrder.indexOf(it) + 1] ?: fanModeOrder[0] }
	switchToFanMode(next(currentFanMode))
}

def switchToFanMode(fanMode) {
	log.debug "switchToFanMode: $fanMode"
	def heatingSetpoint = getTempInDeviceScale("heatingSetpoint")
	def coolingSetpoint = getTempInDeviceScale("coolingSetpoint")
	def deviceId = device.deviceNetworkId.split(/\./).last()
	def sendHoldType = holdType ? ((holdType=="Temporary") ? "nextTransition" : "indefinite") : "indefinite"

	if (!(parent.setFanMode(heatingSetpoint, coolingSetpoint, deviceId, sendHoldType, fanMode))) {
		log.warn "Error setting fanMode:fanMode"
		// Ensure the DTH tile is reset
		generateFanModeEvent(device.currentValue("thermostatFanMode"))
	}
	runIn(5, "refresh", [overwrite: true])
}

def getDataByName(String name) {
	state[name] ?: device.getDataValue(name)
}

def setThermostatMode(String mode) {
	log.debug "setThermostatMode($mode)"
	def supportedModes = modes()
	if (supportedModes) {
		mode = mode.toLowerCase()
		def modeIdx = supportedModes.indexOf(mode)
		if (modeIdx < 0) {
			log.warn("Thermostat mode $mode not valid for this thermostat")
			return
		}
		mode = supportedModes[modeIdx]
		switchToMode(mode)
	} else {
		log.warn "supportedThermostatModes not defined"
	}
}

def setThermostatFanMode(String mode) {
	log.debug "setThermostatFanMode($mode)"
	mode = mode.toLowerCase()
	def supportedFanModes = fanModes()
	def modeIdx = supportedFanModes.indexOf(mode)
	if (modeIdx < 0) {
		log.warn("Thermostat fan mode $mode not valid for this thermostat")
		return
	}
	mode = supportedFanModes[modeIdx]
	switchToFanMode(mode)
}

def generateModeEvent(mode) {
	sendEvent(name: "thermostatMode", value: mode, data:[supportedThermostatModes: device.currentValue("supportedThermostatModes")],
			isStateChange: true, descriptionText: "$device.displayName is in ${mode} mode")
}

def generateFanModeEvent(fanMode) {
	sendEvent(name: "thermostatFanMode", value: fanMode, data:[supportedThermostatFanModes: device.currentValue("supportedThermostatFanModes")],
			isStateChange: true, descriptionText: "$device.displayName fan is in ${fanMode} mode")
}

def generateOperatingStateEvent(operatingState) {
	sendEvent(name: "thermostatOperatingState", value: operatingState, descriptionText: "$device.displayName is ${operatingState}", displayed: true)
}

def off() { setThermostatMode("off") }
def heat() { setThermostatMode("heat") }
def emergencyHeat() { setThermostatMode("emergency heat") }
def cool() { setThermostatMode("cool") }
def auto() { setThermostatMode("auto") }

def fanOn() { setThermostatFanMode("on") }
def fanAuto() { setThermostatFanMode("auto") }
def fanCirculate() { setThermostatFanMode("circulate") }

// =============== Setpoints ===============
def generateSetpointEvent() {
	def mode = device.currentValue("thermostatMode")
	def setpoint = getTempInLocalScale("heatingSetpoint")  // (mode == "heat") || (mode == "emergency heat")
	def coolingSetpoint = getTempInLocalScale("coolingSetpoint")

	if (mode == "cool") {
		setpoint = coolingSetpoint
	} else if ((mode == "auto") || (mode == "off")) {
		setpoint = roundC((setpoint + coolingSetpoint) / 2)
	} // else (mode == "heat") || (mode == "emergency heat")
	sendEvent("name":"thermostatSetpoint", "value":setpoint, "unit":location.temperatureScale)
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

// Adjusts nextHeatingSetpoint either .5° C/1° F) if raise true/false
def alterSetpoint(raise, setpoint) {
	// don't allow setpoint change if thermostat is off
	if (device.currentValue("thermostatMode") == "off") {
		return
	}
	def locationScale = getTemperatureScale()
	def deviceScale = "F" 
	def heatingSetpoint = getTempInLocalScale("heatingSetpoint")
	def coolingSetpoint = getTempInLocalScale("coolingSetpoint")
	def targetValue = (setpoint == "heatingSetpoint") ? heatingSetpoint : coolingSetpoint
	def delta = (locationScale == "F") ? 1 : 0.5
	targetValue += raise ? delta : - delta

	def data = enforceSetpointLimits(setpoint,
			[targetValue: targetValue, heatingSetpoint: heatingSetpoint, coolingSetpoint: coolingSetpoint], raise)
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
	runIn(5, "updateSetpoint", [data: data, overwrite: true])
}

def enforceSetpointLimits(setpoint, data, raise = null) {
	def locationScale = getTemperatureScale() 
	def minSetpoint = (setpoint == "heatingSetpoint") ? device.getDataValue("minHeatingSetpointFahrenheit") : device.getDataValue("minCoolingSetpointFahrenheit")
	def maxSetpoint = (setpoint == "heatingSetpoint") ? device.getDataValue("maxHeatingSetpointFahrenheit") : device.getDataValue("maxCoolingSetpointFahrenheit")
	minSetpoint = minSetpoint ? Double.parseDouble(minSetpoint) : ((setpoint == "heatingSetpoint") ? 45 : 65)  // default 45 heat, 65 cool
	maxSetpoint = maxSetpoint ? Double.parseDouble(maxSetpoint) : ((setpoint == "heatingSetpoint") ? 79 : 92)  // default 79 heat, 92 cool
	def deadband = deadbandSetting ? deadbandSetting : 5 // °F
	def delta = (locationScale == "F") ? 1 : 0.5
	def targetValue = getTempInDeviceScale(data.targetValue, locationScale)
	def heatingSetpoint = getTempInDeviceScale(data.heatingSetpoint, locationScale)
	def coolingSetpoint = getTempInDeviceScale(data.coolingSetpoint, locationScale)
	// Enforce min/mix for setpoints
	if (targetValue > maxSetpoint) {
		targetValue = maxSetpoint
	} else if (targetValue < minSetpoint) {
		targetValue = minSetpoint
	} else if ((raise != null) && ((setpoint == "heatingSetpoint" && targetValue == heatingSetpoint) ||
				(setpoint == "coolingSetpoint" && targetValue == coolingSetpoint))) {
		// Ensure targetValue differes from old. When location scale differs from device,
		// converting between C -> F -> C may otherwise result in no change.
		targetValue += raise ? delta : - delta
	}
	// Enforce deadband between setpoints
	if (setpoint == "heatingSetpoint") {
		heatingSetpoint = targetValue
		coolingSetpoint = (heatingSetpoint + deadband > coolingSetpoint) ? heatingSetpoint + deadband : coolingSetpoint
	}
	if (setpoint == "coolingSetpoint") {
		coolingSetpoint = targetValue
		heatingSetpoint = (coolingSetpoint - deadband < heatingSetpoint) ? coolingSetpoint - deadband : heatingSetpoint
	}
	return [targetHeatingSetpoint: heatingSetpoint, targetCoolingSetpoint: coolingSetpoint]
}

def updateSetpoint(data) {
	def deviceId = device.deviceNetworkId.split(/\./).last()
	def sendHoldType = holdType ? ((holdType=="Temporary") ? "nextTransition" : "indefinite") : "indefinite"

	if (parent.setHold(data.targetHeatingSetpoint, data.targetCoolingSetpoint, deviceId, sendHoldType)) {
		log.debug "alterSetpoint succeed to change setpoints:${data}"
	} else {
		log.error "Error alterSetpoint"
	}
	runIn(5, "refresh", [overwrite: true])
}

def generateStatusEvent() {
	def mode = device.currentValue("thermostatMode")
	def heatingSetpoint = device.currentValue("heatingSetpoint")
	def coolingSetpoint = device.currentValue("coolingSetpoint")
	def temperature = device.currentValue("temperature")
	def statusText = "Right Now: Idle"
	def operatingState = "idle"

	if (mode == "heat" || mode == "emergency heat") {
		if (temperature < heatingSetpoint) {
			statusText = "Heating to ${heatingSetpoint}°${location.temperatureScale}"
			operatingState = "heating"
		}
	} else if (mode == "cool") {
		if (temperature > coolingSetpoint) {
			statusText = "Cooling to ${coolingSetpoint}°${location.temperatureScale}"
			operatingState = "cooling"
		}
	} else if (mode == "auto") {
		if (temperature < heatingSetpoint) {
			statusText = "Heating to ${heatingSetpoint}°${location.temperatureScale}"
			operatingState = "heating"
		} else if (temperature > coolingSetpoint) {
			statusText = "Cooling to ${coolingSetpoint}°${location.temperatureScale}"
			operatingState = "cooling"
		}
	} else if (mode == "off") {
		statusText = "Right Now: Off"
	} else {
		statusText = "?"
	}

	sendEvent("name":"thermostat", "value":statusText, "description":statusText, displayed: true)
	sendEvent("name":"thermostatOperatingState", "value":operatingState, "description":operatingState, displayed: false)
}

def generateActivityFeedsEvent(notificationMessage) {
	sendEvent(name: "notificationMessage", value: "$device.displayName $notificationMessage", descriptionText: "$device.displayName $notificationMessage", displayed: true)
}

// Get stored temperature from currentState in current local scale
def getTempInLocalScale(state) {
	def temp = device.currentState(state)
	def scaledTemp = convertTemperatureIfNeeded(temp.value.toBigDecimal(), temp.unit).toDouble()
	return (getTemperatureScale() == "F" ? scaledTemp.round(0).toInteger() : roundC(scaledTemp))
}

// Get/Convert temperature to current local scale
def getTempInLocalScale(temp, scale) {
	def scaledTemp = convertTemperatureIfNeeded(temp.toBigDecimal(), scale).toDouble()
	return (getTemperatureScale() == "F" ? scaledTemp.round(0).toInteger() : roundC(scaledTemp))
}

// Get stored temperature from currentState in device scale
def getTempInDeviceScale(state) {
	def temp = device.currentState(state)
	if (temp && temp.value && temp.unit) {
		return getTempInDeviceScale(temp.value.toBigDecimal(), temp.unit)
	}
	return 0
}

def getTempInDeviceScale(temp, scale) {
	if (temp && scale) {
		//API return/expects temperature values in F
		return ("F" == scale) ? temp : celsiusToFahrenheit(temp).toDouble().round(0).toInteger()
	}
	return 0
}

def roundC (tempC) {
	return (Math.round(tempC.toDouble() * 2))/2
}
