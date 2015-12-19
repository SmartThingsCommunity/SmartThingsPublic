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
		capability "Polling"
		capability "Sensor"
        capability "Refresh"

        command "generateEvent"
        command "raiseSetpoint"
        command "lowerSetpoint"
        command "resumeProgram"
        command "switchMode"

        attribute "thermostatSetpoint","number"
        attribute "thermostatStatus","string"
	}

	simulator { }

    	tiles {
		valueTile("temperature", "device.temperature", width: 2, height: 2) {
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
		standardTile("mode", "device.thermostatMode", inactiveLabel: false, decoration: "flat") {
			state "off", action:"switchMode", nextState: "updating", icon: "st.thermostat.heating-cooling-off"
			state "heat", action:"switchMode",  nextState: "updating", icon: "st.thermostat.heat"
			state "cool", action:"switchMode",  nextState: "updating", icon: "st.thermostat.cool"
			state "auto", action:"switchMode",  nextState: "updating", icon: "st.thermostat.auto"
			state "auxHeatOnly", action:"switchMode", icon: "st.thermostat.emergency-heat"
			state "updating", label:"Working", icon: "st.secondary.secondary"
		}
		standardTile("fanMode", "device.thermostatFanMode", inactiveLabel: false, decoration: "flat") {
			state "auto", label:'Fan: ${currentValue}', action:"switchFanMode", nextState: "on"
			state "on", label:'Fan: ${currentValue}', action:"switchFanMode", nextState: "off"
			state "off", label:'Fan: ${currentValue}', action:"switchFanMode", nextState: "circulate"
			state "circulate", label:'Fan: ${currentValue}', action:"switchFanMode", nextState: "auto"
		}
		standardTile("upButtonControl", "device.thermostatSetpoint", inactiveLabel: false, decoration: "flat") {
			state "setpoint", action:"raiseSetpoint", icon:"st.thermostat.thermostat-up"
		}
		valueTile("thermostatSetpoint", "device.thermostatSetpoint", width: 1, height: 1, decoration: "flat") {
			state "thermostatSetpoint", label:'${currentValue}°'
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
			state "resume", action:"resumeProgram", nextState: "updating", label:'Resume Schedule', icon:"st.samsung.da.oven_ic_send"
			state "updating", label:"Working", icon: "st.secondary.secondary"
		}
		main "temperature"
		details(["temperature", "upButtonControl", "thermostatSetpoint", "currentStatus", "downButtonControl", "mode", "resumeProgram", "refresh"])
	}

	preferences {
		input "holdType", "enum", title: "Hold Type", description: "When changing temperature, use Temporary (Until next transition) or Permanent hold (default)", required: false, options:["Temporary", "Permanent"]
	}

}

// parse events into attributes
def parse(String description) {
	log.debug "Parsing '${description}'"
	// TODO: handle '' attribute

}

def refresh() {
	log.debug "refresh called"
	poll()
	log.debug "refresh ended"
}

void poll() {
	log.debug "Executing 'poll' using parent SmartApp"

	def results = parent.pollChild(this)
	generateEvent(results) //parse received message from parent
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

			if (name=="temperature" || name=="heatingSetpoint" || name=="coolingSetpoint") {
				def sendValue = value? convertTemperatureIfNeeded(value.toDouble(), "F", 1): value //API return temperature value in F
				isChange = isTemperatureStateChange(device, name, value.toString())
				isDisplayed = isChange
				event << [value: sendValue, isStateChange: isChange, displayed: isDisplayed]
			} else if (name=="heatMode" || name=="coolMode" || name=="autoMode" || name=="auxHeatMode"){
				isChange = isStateChange(device, name, value.toString())
				event << [value: value.toString(), isStateChange: isChange, displayed: false]
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
		return "$linkText temperature is $value°F"

	} else if(name == "heatingSetpoint") {
		return "heating setpoint is $value°F"

	} else if(name == "coolingSetpoint"){
		return "cooling setpoint is $value°F"

	} else if (name == "thermostatMode") {
		return "thermostat mode is ${value}"

	} else if (name == "thermostatFanMode") {
		return "thermostat fan mode is ${value}"

	} else {
		return "${name} = ${value}"
	}
}

void setHeatingSetpoint(setpoint) {
	setHeatingSetpoint(setpoint.toDouble())
}

void setHeatingSetpoint(Double setpoint) {
//    def mode = device.currentValue("thermostatMode")
	def heatingSetpoint = setpoint
	def coolingSetpoint = device.currentValue("coolingSetpoint").toDouble()
	def deviceId = device.deviceNetworkId.split(/\./).last()

	//enforce limits of heatingSetpoint
	if (heatingSetpoint > 79) {
		heatingSetpoint = 79
	} else if (heatingSetpoint < 45) {
		heatingSetpoint = 45
	}

	//enforce limits of heatingSetpoint vs coolingSetpoint
	if (heatingSetpoint >= coolingSetpoint) {
		coolingSetpoint = heatingSetpoint
	}

	log.debug "Sending setHeatingSetpoint> coolingSetpoint: ${coolingSetpoint}, heatingSetpoint: ${heatingSetpoint}"

	def sendHoldType = holdType ? (holdType=="Temporary")? "nextTransition" : (holdType=="Permanent")? "indefinite" : "indefinite" : "indefinite"
	if (parent.setHold (this, heatingSetpoint,  coolingSetpoint, deviceId, sendHoldType)) {
		sendEvent("name":"heatingSetpoint", "value":heatingSetpoint)
		sendEvent("name":"coolingSetpoint", "value":coolingSetpoint)
		log.debug "Done setHeatingSetpoint> coolingSetpoint: ${coolingSetpoint}, heatingSetpoint: ${heatingSetpoint}"
		generateSetpointEvent()
		generateStatusEvent()
	} else {
		log.error "Error setHeatingSetpoint(setpoint)" //This error is handled by the connect app
	}
}

void setCoolingSetpoint(setpoint) {
	setCoolingSetpoint(setpoint.toDouble())
}

void setCoolingSetpoint(Double setpoint) {
//    def mode = device.currentValue("thermostatMode")
	def heatingSetpoint = device.currentValue("heatingSetpoint").toDouble()
	def coolingSetpoint = setpoint
	def deviceId = device.deviceNetworkId.split(/\./).last()

	if (coolingSetpoint > 92) {
		coolingSetpoint = 92
	} else if (coolingSetpoint < 65) {
		coolingSetpoint = 65
	}

	//enforce limits of heatingSetpoint vs coolingSetpoint
	if (heatingSetpoint >= coolingSetpoint) {
		heatingSetpoint = coolingSetpoint
	}

	log.debug "Sending setCoolingSetpoint> coolingSetpoint: ${coolingSetpoint}, heatingSetpoint: ${heatingSetpoint}"

	def sendHoldType = holdType ? (holdType=="Temporary")? "nextTransition" : (holdType=="Permanent")? "indefinite" : "indefinite" : "indefinite"
	if (parent.setHold (this, heatingSetpoint,  coolingSetpoint, deviceId, sendHoldType)) {
		sendEvent("name":"heatingSetpoint", "value":heatingSetpoint)
		sendEvent("name":"coolingSetpoint", "value":coolingSetpoint)
		log.debug "Done setCoolingSetpoint>> coolingSetpoint = ${coolingSetpoint}, heatingSetpoint = ${heatingSetpoint}"
		generateSetpointEvent()
		generateStatusEvent()
	} else {
		log.error "Error setCoolingSetpoint(setpoint)" //This error is handled by the connect app
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
	["off", "on", "auto", "circulate"]
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
		case "fanAuto":
			returnCommand = switchToFanMode("fanOn")
			break
		case "fanOn":
			returnCommand = switchToFanMode("fanCirculate")
			break
		case "fanCirculate":
			returnCommand = switchToFanMode("fanAuto")
			break
	}
	if(!currentFanMode) { returnCommand = switchToFanMode("fanOn") }
	returnCommand
}

def switchToFanMode(nextMode) {

	log.debug "switching to fan mode: $nextMode"
	def returnCommand

	if(nextMode == "fanAuto") {
		if(!fanModes.contains("fanAuto")) {
			returnCommand = fanAuto()
		} else {
			returnCommand = switchToFanMode("fanOn")
		}
	} else if(nextMode == "fanOn") {
		if(!fanModes.contains("fanOn")) {
			returnCommand = fanOn()
		} else {
			returnCommand = switchToFanMode("fanCirculate")
		}
	} else if(nextMode == "fanCirculate") {
		if(!fanModes.contains("fanCirculate")) {
			returnCommand = fanCirculate()
		} else {
			returnCommand = switchToFanMode("fanAuto")
		}
	}
	returnCommand
}

def getDataByName(String name) {
	state[name] ?: device.getDataValue(name)
}

def setThermostatMode(String value) {
	log.debug "setThermostatMode({$value})"

}

def setThermostatFanMode(String value) {

	log.debug "setThermostatFanMode({$value})"

}

def generateModeEvent(mode) {
	sendEvent(name: "thermostatMode", value: mode, descriptionText: "$device.displayName is in ${mode} mode", displayed: true)
}

def generateFanModeEvent(fanMode) {
	sendEvent(name: "thermostatFanMode", value: fanMode, descriptionText: "$device.displayName fan is in ${mode} mode", displayed: true)
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
//    parent.setFanMode (this,"on")

}

def fanAuto() {
	log.debug "fanAuto"
//    parent.setFanMode (this,"auto")

}

def fanCirculate() {
	log.debug "fanCirculate"
//    parent.setFanMode (this,"circulate")

}

def fanOff() {
	log.debug "fanOff"
//    parent.setFanMode (this,"off")

}

def generateSetpointEvent() {

	log.debug "Generate SetPoint Event"

	def mode = device.currentValue("thermostatMode")
	log.debug "Current Mode = ${mode}"

	def heatingSetpoint = device.currentValue("heatingSetpoint").toInteger()
	log.debug "Heating Setpoint = ${heatingSetpoint}"

	def coolingSetpoint = device.currentValue("coolingSetpoint").toInteger()
	log.debug "Cooling Setpoint = ${coolingSetpoint}"

	if (mode == "heat") {

		sendEvent("name":"thermostatSetpoint", "value":heatingSetpoint.toString())

	}
	else if (mode == "cool") {

		sendEvent("name":"thermostatSetpoint", "value":coolingSetpoint.toString())

	} else if (mode == "auto") {

		sendEvent("name":"thermostatSetpoint", "value":"Auto")

	} else if (mode == "off") {

		sendEvent("name":"thermostatSetpoint", "value":"Off")

	} else if (mode == "emergencyHeat") {

		sendEvent("name":"thermostatSetpoint", "value":heatingSetpoint.toString())

	}

}

void raiseSetpoint() {
	def mode = device.currentValue("thermostatMode")
	def targetvalue

	if (mode == "off" || mode == "auto") {
		log.warn "this mode: $mode does not allow raiseSetpoint"
	} else {
		def heatingSetpoint = device.currentValue("heatingSetpoint").toInteger()
		def coolingSetpoint = device.currentValue("coolingSetpoint").toInteger()
		def thermostatSetpoint = device.currentValue("thermostatSetpoint").toInteger()
		log.debug "raiseSetpoint() mode = ${mode}, heatingSetpoint: ${heatingSetpoint}, coolingSetpoint:${coolingSetpoint}, thermostatSetpoint:${thermostatSetpoint}"

		if (device.latestState('thermostatSetpoint')) {
			targetvalue = device.latestState('thermostatSetpoint').value as Integer
		} else {
			targetvalue = 0
		}
		targetvalue = targetvalue + 1

		if (mode == "heat" && targetvalue > 79) {
			targetvalue = 79
		} else if (mode == "cool" && targetvalue > 92) {
			targetvalue = 92
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

	if (mode == "off" || mode == "auto") {
		log.warn "this mode: $mode does not allow lowerSetpoint"
	} else {
		def heatingSetpoint = device.currentValue("heatingSetpoint").toInteger()
		def coolingSetpoint = device.currentValue("coolingSetpoint").toInteger()
		def thermostatSetpoint = device.currentValue("thermostatSetpoint").toInteger()
		log.debug "lowerSetpoint() mode = ${mode}, heatingSetpoint: ${heatingSetpoint}, coolingSetpoint:${coolingSetpoint}, thermostatSetpoint:${thermostatSetpoint}"
		if (device.latestState('thermostatSetpoint')) {
			targetvalue = device.latestState('thermostatSetpoint').value as Integer
		} else {
			targetvalue = 0
		}
		targetvalue = targetvalue - 1

		if (mode == "heat" && targetvalue.toInteger() < 45) {
			targetvalue = 45
		} else if (mode == "cool" && targetvalue.toInteger() < 65) {
			targetvalue = 65
		}

		sendEvent("name":"thermostatSetpoint", "value":targetvalue, displayed: false)
		log.info "In mode $mode lowerSetpoint() to $targetvalue"

		runIn(3, "alterSetpoint", [data: [value:targetvalue], overwrite: true]) //when user click button this runIn will be overwrite
	}
}

//called by raiseSetpoint() and lowerSetpoint()
void alterSetpoint(temp) {

	def mode = device.currentValue("thermostatMode")
	def heatingSetpoint = device.currentValue("heatingSetpoint").toInteger()
	def coolingSetpoint = device.currentValue("coolingSetpoint").toInteger()
	def deviceId = device.deviceNetworkId.split(/\./).last()

	def targetHeatingSetpoint
	def targetCoolingSetpoint

	//step1: check thermostatMode, enforce limits before sending request to cloud
	if (mode == "heat"){
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

	log.debug "alterSetpoint >> in mode ${mode} trying to change heatingSetpoint to ${targetHeatingSetpoint} " +
			"coolingSetpoint to ${targetCoolingSetpoint} with holdType : ${holdType}"

	def sendHoldType = holdType ? (holdType=="Temporary")? "nextTransition" : (holdType=="Permanent")? "indefinite" : "indefinite" : "indefinite"
	//step2: call parent.setHold to send http request to 3rd party cloud
	if (parent.setHold(this, targetHeatingSetpoint, targetCoolingSetpoint, deviceId, sendHoldType)) {
		sendEvent("name": "thermostatSetpoint", "value": temp.value.toString(), displayed: false)
		sendEvent("name": "heatingSetpoint", "value": targetHeatingSetpoint)
		sendEvent("name": "coolingSetpoint", "value": targetCoolingSetpoint)
		log.debug "alterSetpoint in mode $mode succeed change setpoint to= ${temp.value}"
	} else {
		log.error "Error alterSetpoint()"
		if (mode == "heat"){
			sendEvent("name": "thermostatSetpoint", "value": heatingSetpoint.toString(), displayed: false)
		} else if (mode == "cool") {
			sendEvent("name": "thermostatSetpoint", "value": coolingSetpoint.toString(), displayed: false)
		}
	}
	generateStatusEvent()
}

def generateStatusEvent() {

	def mode = device.currentValue("thermostatMode")
	def heatingSetpoint = device.currentValue("heatingSetpoint").toInteger()
	def coolingSetpoint = device.currentValue("coolingSetpoint").toInteger()
	def temperature = device.currentValue("temperature").toInteger()

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
			statusText = "Heating to ${heatingSetpoint}° F"

	} else if (mode == "cool") {

		if (temperature <= coolingSetpoint)
			statusText = "Right Now: Idle"
		else
			statusText = "Cooling to ${coolingSetpoint}° F"

	} else if (mode == "auto") {

		statusText = "Right Now: Auto"

	} else if (mode == "off") {

		statusText = "Right Now: Off"

	} else if (mode == "emergencyHeat") {

		statusText = "Emergency Heat"

	} else {

		statusText = "?"

	}
	log.debug "Generate Status Event = ${statusText}"
	sendEvent("name":"thermostatStatus", "value":statusText, "description":statusText, displayed: true)
}

//generate custom mobile activity feeds event
def generateActivityFeedsEvent(notificationMessage) {
	sendEvent(name: "notificationMessage", value: "$device.displayName $notificationMessage", descriptionText: "$device.displayName $notificationMessage", displayed: true)
}
