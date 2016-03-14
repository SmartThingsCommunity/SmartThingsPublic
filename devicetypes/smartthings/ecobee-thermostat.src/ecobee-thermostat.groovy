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

		command "generateEvent"
		command "raiseSetpoint"
		command "lowerSetpoint"
		command "resumeProgram"
		command "switchMode"
		command "switchFanMode"

		attribute "thermostatSetpoint","number"
		attribute "thermostatStatus","string"
		attribute "maxHeatingSetpoint", "number"
		attribute "minHeatingSetpoint", "number"
		attribute "maxCoolingSetpoint", "number"
		attribute "minCoolingSetpoint", "number"
		attribute "deviceTemperatureUnit", "number"
	}

	tiles {
		valueTile("temperature", "device.temperature", width: 2, height: 2) {
			state("temperature", label:'${currentValue}°', unit:"F",
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
		standardTile("mode", "device.thermostatMode", inactiveLabel: false, decoration: "flat") {
			state "off", action:"switchMode", nextState: "updating", icon: "st.thermostat.heating-cooling-off"
			state "heat", action:"switchMode",  nextState: "updating", icon: "st.thermostat.heat"
			state "cool", action:"switchMode",  nextState: "updating", icon: "st.thermostat.cool"
			state "auto", action:"switchMode",  nextState: "updating", icon: "st.thermostat.auto"
			state "auxHeatOnly", action:"switchMode", icon: "st.thermostat.emergency-heat"
			state "updating", label:"Working", icon: "st.secondary.secondary"
		}
		standardTile("fanMode", "device.thermostatFanMode", inactiveLabel: false, decoration: "flat") {
			state "auto", action:"switchFanMode", nextState: "updating", icon: "st.thermostat.fan-auto"
			state "on", action:"switchFanMode", nextState: "updating", icon: "st.thermostat.fan-on"
			state "updating", label:"Working", icon: "st.secondary.secondary"
		}
		standardTile("upButtonControl", "device.thermostatSetpoint", inactiveLabel: false, decoration: "flat") {
			state "setpoint", action:"raiseSetpoint", icon:"st.thermostat.thermostat-up"
		}
		valueTile("thermostatSetpoint", "device.thermostatSetpoint", width: 1, height: 1, decoration: "flat") {
			state "thermostatSetpoint", label:'${currentValue}'
		}
		valueTile("currentStatus", "device.thermostatStatus", height: 1, width: 2, decoration: "flat") {
			state "thermostatStatus", label:'${currentValue}', backgroundColor:"#ffffff"
		}
		standardTile("downButtonControl", "device.thermostatSetpoint", inactiveLabel: false, decoration: "flat") {
			state "setpoint", action:"lowerSetpoint", icon:"st.thermostat.thermostat-down"
		}
		controlTile("heatSliderControl", "device.heatingSetpoint", "slider", height: 1, width: 2, inactiveLabel: false) {
			state "setHeatingSetpoint", action:"thermostat.setHeatingSetpoint", backgroundColor:"#d04e00"
		}
		valueTile("heatingSetpoint", "device.heatingSetpoint", inactiveLabel: false, decoration: "flat") {
			state "heat", label:'${currentValue}° heat', unit:"F"
		}
		controlTile("coolSliderControl", "device.coolingSetpoint", "slider", height: 1, width: 2, inactiveLabel: false) {
			state "setCoolingSetpoint", action:"thermostat.setCoolingSetpoint", backgroundColor: "#1e9cbb"
		}
		valueTile("coolingSetpoint", "device.coolingSetpoint", inactiveLabel: false, decoration: "flat") {
			state "cool", label:'${currentValue}° cool', unit:"F", backgroundColor:"#ffffff"
		}
		standardTile("refresh", "device.thermostatMode", inactiveLabel: false, decoration: "flat") {
			state "default", action:"refresh.refresh", icon:"st.secondary.refresh"
		}
		standardTile("resumeProgram", "device.resumeProgram", inactiveLabel: false, decoration: "flat") {
			state "resume", action:"resumeProgram", nextState: "updating", label:'Resume', icon:"st.samsung.da.oven_ic_send"
			state "updating", label:"Working", icon: "st.secondary.secondary"
		}
		valueTile("humidity", "device.humidity", decoration: "flat") {
			state "humidity", label:'${currentValue}%'
		}
		main "temperature"
		details(["temperature", "upButtonControl", "thermostatSetpoint", "currentStatus", "downButtonControl", "mode", "fanMode","humidity", "resumeProgram", "refresh"])
	}

	preferences {
		input "holdType", "enum", title: "Hold Type", description: "When changing temperature, use Temporary (Until next transition) or Permanent hold (default)", required: false, options:["Temporary", "Permanent"]
	}

}

// parse events into attributes
def parse(String description) {
	log.debug "Parsing '${description}'"
}

def refresh() {
	log.debug "refresh called"
	poll()
	log.debug "refresh ended"
}

void poll() {
	log.debug "Executing 'poll' using parent SmartApp"
	parent.pollChild()
}

def generateEvent(Map results) {
	log.debug "parsing data $results"
	if(results) {
		results.each { name, value ->

			def linkText = getLinkText(device)
			def isChange = false
			def isDisplayed = true
			def event = [name: name, linkText: linkText, descriptionText: getThermostatDescriptionText(name, value, linkText),
						 handlerName: name]

			if (name=="temperature" || name=="heatingSetpoint" || name=="coolingSetpoint" ) {
				def sendValue = convertTemperatureIfNeeded(value.toDouble(), "F", 1) //API return temperature value in F
				sendValue =  location.temperatureScale == "C"? roundC(sendValue) : sendValue
				isChange = isTemperatureStateChange(device, name, value.toString())
				isDisplayed = isChange
				event << [value: sendValue, isStateChange: isChange, displayed: isDisplayed]
			}  else if (name=="maxCoolingSetpoint" || name=="minCoolingSetpoint" || name=="maxHeatingSetpoint" || name=="minHeatingSetpoint") {
				def sendValue = convertTemperatureIfNeeded(value.toDouble(), "F", 1) //API return temperature value in F
				sendValue =  location.temperatureScale == "C"? roundC(sendValue) : sendValue
				event << [value: sendValue, displayed: false]
			}  else if (name=="heatMode" || name=="coolMode" || name=="autoMode" || name=="auxHeatMode"){
				isChange = isStateChange(device, name, value.toString())
				event << [value: value.toString(), isStateChange: isChange, displayed: false]
			}  else if (name=="thermostatFanMode"){
				isChange = isStateChange(device, name, value.toString())
				event << [value: value.toString(), isStateChange: isChange, displayed: false]
			}  else if (name=="humidity") {
				isChange = isStateChange(device, name, value.toString())
				event << [value: value.toString(), isStateChange: isChange, displayed: false, unit: "%"]
			}  else {
				isChange = isStateChange(device, name, value.toString())
				isDisplayed = isChange
				event << [value: value.toString(), isStateChange: isChange, displayed: isDisplayed]
			}
			sendEvent(event)
		}
		generateSetpointEvent ()
		generateStatusEvent ()
	}
}

//return descriptionText to be shown on mobile activity feed
private getThermostatDescriptionText(name, value, linkText) {
	if(name == "temperature") {
		def sendValue = convertTemperatureIfNeeded(value.toDouble(), "F", 1) //API return temperature value in F
		sendValue =  location.temperatureScale == "C"? roundC(sendValue) : sendValue
		return "$linkText temperature is $sendValue ${location.temperatureScale}"

	} else if(name == "heatingSetpoint") {
		def sendValue = convertTemperatureIfNeeded(value.toDouble(), "F", 1) //API return temperature value in F
		sendValue =  location.temperatureScale == "C"? roundC(sendValue) : sendValue
		return "heating setpoint is $sendValue ${location.temperatureScale}"

	} else if(name == "coolingSetpoint"){
		def sendValue = convertTemperatureIfNeeded(value.toDouble(), "F", 1) //API return temperature value in F
		sendValue =  location.temperatureScale == "C"? roundC(sendValue) : sendValue
		return "cooling setpoint is $sendValue ${location.temperatureScale}"

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
	log.debug "***heating setpoint $setpoint"
	def heatingSetpoint = setpoint
	def coolingSetpoint = device.currentValue("coolingSetpoint")
	def deviceId = device.deviceNetworkId.split(/\./).last()
	def maxHeatingSetpoint = device.currentValue("maxHeatingSetpoint")
	def minHeatingSetpoint = device.currentValue("minHeatingSetpoint")

	//enforce limits of heatingSetpoint
	if (heatingSetpoint > maxHeatingSetpoint) {
		heatingSetpoint = maxHeatingSetpoint
	} else if (heatingSetpoint < minHeatingSetpoint) {
		heatingSetpoint = minHeatingSetpoint
	}

	//enforce limits of heatingSetpoint vs coolingSetpoint
	if (heatingSetpoint >= coolingSetpoint) {
		coolingSetpoint = heatingSetpoint
	}

	log.debug "Sending setHeatingSetpoint> coolingSetpoint: ${coolingSetpoint}, heatingSetpoint: ${heatingSetpoint}"

	def coolingValue = location.temperatureScale == "C"? convertCtoF(coolingSetpoint) : coolingSetpoint
	def heatingValue = location.temperatureScale == "C"? convertCtoF(heatingSetpoint) : heatingSetpoint

	def sendHoldType = holdType ? (holdType=="Temporary")? "nextTransition" : (holdType=="Permanent")? "indefinite" : "indefinite" : "indefinite"
	if (parent.setHold(this, heatingValue, coolingValue, deviceId, sendHoldType)) {
		sendEvent("name":"heatingSetpoint", "value":heatingSetpoint)
		sendEvent("name":"coolingSetpoint", "value":coolingSetpoint)
		log.debug "Done setHeatingSetpoint> coolingSetpoint: ${coolingSetpoint}, heatingSetpoint: ${heatingSetpoint}"
		generateSetpointEvent()
		generateStatusEvent()
	} else {
		log.error "Error setHeatingSetpoint(setpoint)"
	}
}

void setCoolingSetpoint(setpoint) {
	log.debug "***cooling setpoint $setpoint"
	def heatingSetpoint = device.currentValue("heatingSetpoint")
	def coolingSetpoint = setpoint
	def deviceId = device.deviceNetworkId.split(/\./).last()
	def maxCoolingSetpoint = device.currentValue("maxCoolingSetpoint")
	def minCoolingSetpoint = device.currentValue("minCoolingSetpoint")


	if (coolingSetpoint > maxCoolingSetpoint) {
		coolingSetpoint = maxCoolingSetpoint
	} else if (coolingSetpoint < minCoolingSetpoint) {
		coolingSetpoint = minCoolingSetpoint
	}

	//enforce limits of heatingSetpoint vs coolingSetpoint
	if (heatingSetpoint >= coolingSetpoint) {
		heatingSetpoint = coolingSetpoint
	}

	log.debug "Sending setCoolingSetpoint> coolingSetpoint: ${coolingSetpoint}, heatingSetpoint: ${heatingSetpoint}"

	def coolingValue = location.temperatureScale == "C"? convertCtoF(coolingSetpoint) : coolingSetpoint
	def heatingValue = location.temperatureScale == "C"? convertCtoF(heatingSetpoint) : heatingSetpoint

	def sendHoldType = holdType ? (holdType=="Temporary")? "nextTransition" : (holdType=="Permanent")? "indefinite" : "indefinite" : "indefinite"
	if (parent.setHold(this, heatingValue, coolingValue, deviceId, sendHoldType)) {
		sendEvent("name":"heatingSetpoint", "value":heatingSetpoint)
		sendEvent("name":"coolingSetpoint", "value":coolingSetpoint)
		log.debug "Done setCoolingSetpoint>> coolingSetpoint = ${coolingSetpoint}, heatingSetpoint = ${heatingSetpoint}"
		generateSetpointEvent()
		generateStatusEvent()
	} else {
		log.error "Error setCoolingSetpoint(setpoint)"
	}
}

void resumeProgram() {

	log.debug "resumeProgram() is called"
	sendEvent("name":"thermostatStatus", "value":"resuming schedule", "description":statusText, displayed: false)
	def deviceId = device.deviceNetworkId.split(/\./).last()
	if (parent.resumeProgram(this, deviceId)) {
		sendEvent("name":"thermostatStatus", "value":"setpoint is updating", "description":statusText, displayed: false)
		runIn(5, "poll")
		log.debug "resumeProgram() is done"
		sendEvent("name":"resumeProgram", "value":"resume", descriptionText: "resumeProgram is done", displayed: false, isStateChange: true)
	} else {
		sendEvent("name":"thermostatStatus", "value":"failed resume click refresh", "description":statusText, displayed: false)
		log.error "Error resumeProgram() check parent.resumeProgram(this, deviceId)"
	}

}

def modes() {
	if (state.modes) {
		log.debug "Modes = ${state.modes}"
		return state.modes
	}
	else {
		state.modes = parent.availableModes(this)
		log.debug "Modes = ${state.modes}"
		return state.modes
	}
}

def fanModes() {
	["on", "auto"]
}

def switchMode() {
	log.debug "in switchMode"
	def currentMode = device.currentState("thermostatMode")?.value
	def lastTriedMode = state.lastTriedMode ?: currentMode ?: "off"
	def modeOrder = modes()
	def next = { modeOrder[modeOrder.indexOf(it) + 1] ?: modeOrder[0] }
	def nextMode = next(lastTriedMode)
	switchToMode(nextMode)
}

def switchToMode(nextMode) {
	log.debug "In switchToMode = ${nextMode}"
	if (nextMode in modes()) {
		state.lastTriedMode = nextMode
		"$nextMode"()
	} else {
		log.debug("no mode method '$nextMode'")
	}
}

def switchFanMode() {
	def currentFanMode = device.currentState("thermostatFanMode")?.value
	log.debug "switching fan from current mode: $currentFanMode"
	def returnCommand

	switch (currentFanMode) {
		case "on":
			returnCommand = switchToFanMode("auto")
			break
		case "auto":
			returnCommand = switchToFanMode("on")
			break

	}
	if(!currentFanMode) { returnCommand = switchToFanMode("auto") }
	returnCommand
}

def switchToFanMode(nextMode) {

	log.debug "switching to fan mode: $nextMode"
	def returnCommand

	if(nextMode == "auto") {
		if(!fanModes.contains("auto")) {
			returnCommand = fanAuto()
		} else {
			returnCommand = switchToFanMode("on")
		}
	} else if(nextMode == "on") {
		if(!fanModes.contains("on")) {
			returnCommand = fanOn()
		} else {
			returnCommand = switchToFanMode("auto")
		}
	}

	returnCommand
}

def getDataByName(String name) {
	state[name] ?: device.getDataValue(name)
}

def setThermostatMode(String mode) {
	log.debug "setThermostatMode($mode)"
	mode = mode.toLowerCase()
	switchToMode(mode)
}

def setThermostatFanMode(String mode) {
	log.debug "setThermostatFanMode($mode)"
	mode = mode.toLowerCase()
	switchToFanMode(mode)
}

def generateModeEvent(mode) {
	sendEvent(name: "thermostatMode", value: mode, descriptionText: "$device.displayName is in ${mode} mode", displayed: true)
}

def generateFanModeEvent(fanMode) {
	sendEvent(name: "thermostatFanMode", value: fanMode, descriptionText: "$device.displayName fan is in ${fanMode} mode", displayed: true)
}

def generateOperatingStateEvent(operatingState) {
	sendEvent(name: "thermostatOperatingState", value: operatingState, descriptionText: "$device.displayName is ${operatingState}", displayed: true)
}

def off() {
	log.debug "off"
	def deviceId = device.deviceNetworkId.split(/\./).last()
	if (parent.setMode (this,"off", deviceId))
		generateModeEvent("off")
	else {
		log.debug "Error setting new mode."
		def currentMode = device.currentState("thermostatMode")?.value
		generateModeEvent(currentMode) // reset the tile back
	}
	generateSetpointEvent()
	generateStatusEvent()
}

def heat() {
	log.debug "heat"
	def deviceId = device.deviceNetworkId.split(/\./).last()
	if (parent.setMode (this,"heat", deviceId))
		generateModeEvent("heat")
	else {
		log.debug "Error setting new mode."
		def currentMode = device.currentState("thermostatMode")?.value
		generateModeEvent(currentMode) // reset the tile back
	}
	generateSetpointEvent()
	generateStatusEvent()
}

def emergencyHeat() {
	auxHeatOnly()
}

def auxHeatOnly() {
	log.debug "auxHeatOnly"
	def deviceId = device.deviceNetworkId.split(/\./).last()
	if (parent.setMode (this,"auxHeatOnly", deviceId))
		generateModeEvent("auxHeatOnly")
	else {
		log.debug "Error setting new mode."
		def currentMode = device.currentState("thermostatMode")?.value
		generateModeEvent(currentMode) // reset the tile back
	}
	generateSetpointEvent()
	generateStatusEvent()
}

def cool() {
	log.debug "cool"
	def deviceId = device.deviceNetworkId.split(/\./).last()
	if (parent.setMode (this,"cool", deviceId))
		generateModeEvent("cool")
	else {
		log.debug "Error setting new mode."
		def currentMode = device.currentState("thermostatMode")?.value
		generateModeEvent(currentMode) // reset the tile back
	}
	generateSetpointEvent()
	generateStatusEvent()
}

def auto() {
	log.debug "auto"
	def deviceId = device.deviceNetworkId.split(/\./).last()
	if (parent.setMode (this,"auto", deviceId))
		generateModeEvent("auto")
	else {
		log.debug "Error setting new mode."
		def currentMode = device.currentState("thermostatMode")?.value
		generateModeEvent(currentMode) // reset the tile back
	}
	generateSetpointEvent()
	generateStatusEvent()
}

def fanOn() {
	log.debug "fanOn"
	String fanMode = "on"
	def heatingSetpoint = device.currentValue("heatingSetpoint")
	def coolingSetpoint = device.currentValue("coolingSetpoint")
	def deviceId = device.deviceNetworkId.split(/\./).last()

	def sendHoldType = holdType ? (holdType=="Temporary")? "nextTransition" : (holdType=="Permanent")? "indefinite" : "indefinite" : "indefinite"

	def coolingValue = location.temperatureScale == "C"? convertCtoF(coolingSetpoint) : coolingSetpoint
	def heatingValue = location.temperatureScale == "C"? convertCtoF(heatingSetpoint) : heatingSetpoint

	if (parent.setFanMode(this, heatingValue, coolingValue, deviceId, sendHoldType, fanMode)) {
		generateFanModeEvent(fanMode)
	} else {
		log.debug "Error setting new mode."
		def currentFanMode = device.currentState("thermostatFanMode")?.value
		generateFanModeEvent(currentFanMode) // reset the tile back
	}
}

def fanAuto() {
	log.debug "fanAuto"
	String fanMode = "auto"
	def heatingSetpoint = device.currentValue("heatingSetpoint")
	def coolingSetpoint = device.currentValue("coolingSetpoint")
	def deviceId = device.deviceNetworkId.split(/\./).last()

	def sendHoldType = holdType ? (holdType=="Temporary")? "nextTransition" : (holdType=="Permanent")? "indefinite" : "indefinite" : "indefinite"

	def coolingValue = location.temperatureScale == "C"? convertCtoF(coolingSetpoint) : coolingSetpoint
	def heatingValue = location.temperatureScale == "C"? convertCtoF(heatingSetpoint) : heatingSetpoint

	if (parent.setFanMode(this, heatingValue, coolingValue, deviceId, sendHoldType, fanMode)) {
		generateFanModeEvent(fanMode)
	} else {
		log.debug "Error setting new mode."
		def currentFanMode = device.currentState("thermostatFanMode")?.value
		generateFanModeEvent(currentFanMode) // reset the tile back
	}
}

def generateSetpointEvent() {

	log.debug "Generate SetPoint Event"

	def mode = device.currentValue("thermostatMode")
	log.debug "Current Mode = ${mode}"

	def heatingSetpoint = device.currentValue("heatingSetpoint")
	log.debug "Heating Setpoint = ${heatingSetpoint}"

	def coolingSetpoint = device.currentValue("coolingSetpoint")
	log.debug "Cooling Setpoint = ${coolingSetpoint}"

	def maxHeatingSetpoint = device.currentValue("maxHeatingSetpoint")
	def maxCoolingSetpoint = device.currentValue("maxCoolingSetpoint")
	def minHeatingSetpoint = device.currentValue("minHeatingSetpoint")
	def minCoolingSetpoint = device.currentValue("minCoolingSetpoint")

	if(location.temperatureScale == "C")
	{
		maxHeatingSetpoint = roundC(maxHeatingSetpoint)
		maxCoolingSetpoint = roundC(maxCoolingSetpoint)
		minHeatingSetpoint = roundC(minHeatingSetpoint)
		minCoolingSetpoint = roundC(minCoolingSetpoint)
		heatingSetpoint = roundC(heatingSetpoint)
		coolingSetpoint = roundC(coolingSetpoint)
	}


	sendEvent("name":"maxHeatingSetpoint", "value":maxHeatingSetpoint, "unit":location.temperatureScale)
	sendEvent("name":"maxCoolingSetpoint", "value":maxCoolingSetpoint, "unit":location.temperatureScale)
	sendEvent("name":"minHeatingSetpoint", "value":minHeatingSetpoint, "unit":location.temperatureScale)
	sendEvent("name":"minCoolingSetpoint", "value":minCoolingSetpoint, "unit":location.temperatureScale)


	if (mode == "heat") {

		sendEvent("name":"thermostatSetpoint", "value":heatingSetpoint )

	}
	else if (mode == "cool") {

		sendEvent("name":"thermostatSetpoint", "value":coolingSetpoint)

	} else if (mode == "auto") {

		sendEvent("name":"thermostatSetpoint", "value":"Auto")

	} else if (mode == "off") {

		sendEvent("name":"thermostatSetpoint", "value":"Off")

	} else if (mode == "auxHeatOnly") {

		sendEvent("name":"thermostatSetpoint", "value":heatingSetpoint)

	}

}

void raiseSetpoint() {
	def mode = device.currentValue("thermostatMode")
	def targetvalue
	def maxHeatingSetpoint = device.currentValue("maxHeatingSetpoint")
	def maxCoolingSetpoint = device.currentValue("maxCoolingSetpoint")


	if (mode == "off" || mode == "auto") {
		log.warn "this mode: $mode does not allow raiseSetpoint"
	} else {
		def heatingSetpoint = device.currentValue("heatingSetpoint")
		def coolingSetpoint = device.currentValue("coolingSetpoint")
		def thermostatSetpoint = device.currentValue("thermostatSetpoint")
		log.debug "raiseSetpoint() mode = ${mode}, heatingSetpoint: ${heatingSetpoint}, coolingSetpoint:${coolingSetpoint}, thermostatSetpoint:${thermostatSetpoint}"

		if (device.latestState('thermostatSetpoint')) {
			targetvalue = device.latestState('thermostatSetpoint').value
			targetvalue = location.temperatureScale == "F"? targetvalue.toInteger() : targetvalue.toDouble()
		} else {
			targetvalue = 0
		}
		targetvalue = location.temperatureScale == "F"? targetvalue + 1 : targetvalue + 0.5

		if ((mode == "heat" || mode == "auxHeatOnly") && targetvalue > maxHeatingSetpoint) {
			targetvalue = maxHeatingSetpoint
		} else if (mode == "cool" && targetvalue > maxCoolingSetpoint) {
			targetvalue = maxCoolingSetpoint
		}

		sendEvent("name":"thermostatSetpoint", "value":targetvalue, displayed: false)
		log.info "In mode $mode raiseSetpoint() to $targetvalue"

		runIn(3, "alterSetpoint", [data: [value:targetvalue], overwrite: true]) //when user click button this runIn will be overwrite
	}
}

//called by tile when user hit raise temperature button on UI
void lowerSetpoint() {
	def mode = device.currentValue("thermostatMode")
	def targetvalue
	def minHeatingSetpoint = device.currentValue("minHeatingSetpoint")
	def minCoolingSetpoint = device.currentValue("minCoolingSetpoint")


	if (mode == "off" || mode == "auto") {
		log.warn "this mode: $mode does not allow lowerSetpoint"
	} else {
		def heatingSetpoint = device.currentValue("heatingSetpoint")
		def coolingSetpoint = device.currentValue("coolingSetpoint")
		def thermostatSetpoint = device.currentValue("thermostatSetpoint")
		log.debug "lowerSetpoint() mode = ${mode}, heatingSetpoint: ${heatingSetpoint}, coolingSetpoint:${coolingSetpoint}, thermostatSetpoint:${thermostatSetpoint}"
		if (device.latestState('thermostatSetpoint')) {
			targetvalue = device.latestState('thermostatSetpoint').value
			targetvalue = location.temperatureScale == "F"? targetvalue.toInteger() : targetvalue.toDouble()
		} else {
			targetvalue = 0
		}
		targetvalue = location.temperatureScale == "F"? targetvalue - 1 : targetvalue - 0.5

		if ((mode == "heat" || mode == "auxHeatOnly") && targetvalue < minHeatingSetpoint) {
			targetvalue = minHeatingSetpoint
		} else if (mode == "cool" && targetvalue < minCoolingSetpoint) {
			targetvalue = minCoolingSetpoint
		}

		sendEvent("name":"thermostatSetpoint", "value":targetvalue, displayed: false)
		log.info "In mode $mode lowerSetpoint() to $targetvalue"

		runIn(3, "alterSetpoint", [data: [value:targetvalue], overwrite: true]) //when user click button this runIn will be overwrite
	}
}

//called by raiseSetpoint() and lowerSetpoint()
void alterSetpoint(temp) {

	def mode = device.currentValue("thermostatMode")
	def heatingSetpoint = device.currentValue("heatingSetpoint")
	def coolingSetpoint = device.currentValue("coolingSetpoint")
	def deviceId = device.deviceNetworkId.split(/\./).last()

	def targetHeatingSetpoint
	def targetCoolingSetpoint

	//step1: check thermostatMode, enforce limits before sending request to cloud
	if (mode == "heat" || mode == "auxHeatOnly"){
		if (temp.value > coolingSetpoint){
			targetHeatingSetpoint = temp.value
			targetCoolingSetpoint = temp.value
		} else {
			targetHeatingSetpoint = temp.value
			targetCoolingSetpoint = coolingSetpoint
		}
	} else if (mode == "cool") {
		//enforce limits before sending request to cloud
		if (temp.value < heatingSetpoint){
			targetHeatingSetpoint = temp.value
			targetCoolingSetpoint = temp.value
		} else {
			targetHeatingSetpoint = heatingSetpoint
			targetCoolingSetpoint = temp.value
		}
	}

	log.debug "alterSetpoint >> in mode ${mode} trying to change heatingSetpoint to $targetHeatingSetpoint " +
			"coolingSetpoint to $targetCoolingSetpoint with holdType : ${holdType}"

	def sendHoldType = holdType ? (holdType=="Temporary")? "nextTransition" : (holdType=="Permanent")? "indefinite" : "indefinite" : "indefinite"

	def coolingValue = location.temperatureScale == "C"? convertCtoF(targetCoolingSetpoint) : targetCoolingSetpoint
	def heatingValue = location.temperatureScale == "C"? convertCtoF(targetHeatingSetpoint) : targetHeatingSetpoint

	if (parent.setHold(this, heatingValue, coolingValue, deviceId, sendHoldType)) {
		sendEvent("name": "thermostatSetpoint", "value": temp.value, displayed: false)
		sendEvent("name": "heatingSetpoint", "value": targetHeatingSetpoint)
		sendEvent("name": "coolingSetpoint", "value": targetCoolingSetpoint)
		log.debug "alterSetpoint in mode $mode succeed change setpoint to= ${temp.value}"
	} else {
		log.error "Error alterSetpoint()"
		if (mode == "heat" || mode == "auxHeatOnly"){
			sendEvent("name": "thermostatSetpoint", "value": heatingSetpoint.toString(), displayed: false)
		} else if (mode == "cool") {
			sendEvent("name": "thermostatSetpoint", "value": coolingSetpoint.toString(), displayed: false)
		}
	}
	generateStatusEvent()
}

def generateStatusEvent() {

	def mode = device.currentValue("thermostatMode")
	def heatingSetpoint = device.currentValue("heatingSetpoint")
	def coolingSetpoint = device.currentValue("coolingSetpoint")
	def temperature = device.currentValue("temperature")

	def statusText

	log.debug "Generate Status Event for Mode = ${mode}"
	log.debug "Temperature = ${temperature}"
	log.debug "Heating set point = ${heatingSetpoint}"
	log.debug "Cooling set point = ${coolingSetpoint}"
	log.debug "HVAC Mode = ${mode}"

	if (mode == "heat") {

		if (temperature >= heatingSetpoint)
			statusText = "Right Now: Idle"
		else
			statusText = "Heating to ${heatingSetpoint} ${location.temperatureScale}"

	} else if (mode == "cool") {

		if (temperature <= coolingSetpoint)
			statusText = "Right Now: Idle"
		else
			statusText = "Cooling to ${coolingSetpoint} ${location.temperatureScale}"

	} else if (mode == "auto") {

		statusText = "Right Now: Auto"

	} else if (mode == "off") {

		statusText = "Right Now: Off"

	} else if (mode == "auxHeatOnly") {

		statusText = "Emergency Heat"

	} else {

		statusText = "?"

	}
	log.debug "Generate Status Event = ${statusText}"
	sendEvent("name":"thermostatStatus", "value":statusText, "description":statusText, displayed: true)
}

def generateActivityFeedsEvent(notificationMessage) {
	sendEvent(name: "notificationMessage", value: "$device.displayName $notificationMessage", descriptionText: "$device.displayName $notificationMessage", displayed: true)
}

def roundC (tempC) {
	return (Math.round(tempC.toDouble() * 2))/2
}

def convertFtoC (tempF) {
	return String.format("%.1f", (Math.round(((tempF - 32)*(5/9)) * 2))/2)
}

def convertCtoF (tempC) {
	return (Math.round(tempC * (9/5)) + 32).toInteger()
}
