/**
 *	Nexia Thermostat
 *
 *	Author: Trent Foley
 *	Date: 2016-01-19
 */
metadata {
	definition (name: "Nexia Thermostat", namespace: "trentfoley", author: "Trent Foley") {
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
		valueTile("temperature", "device.temperature", width: 2, height: 2, icon:"st.Home.home1", canChangeIcon: true) {
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
			state "off", action:"switchMode", nextState: "blank", icon: "st.thermostat.heating-cooling-off"
			state "heat", action:"switchMode",  nextState: "blank", icon: "st.thermostat.heat"
			state "cool", action:"switchMode",  nextState: "blank", icon: "st.thermostat.cool"
			state "auto", action:"switchMode",  nextState: "blank", icon: "st.thermostat.auto"
			state "auxHeatOnly", action:"switchMode", icon: "st.thermostat.emergency-heat"  
            state "blank", icon: "st.secondary.secondary"  
		}
		standardTile("fanMode", "device.thermostatFanMode", inactiveLabel: false, decoration: "flat") {
			state "auto", label:'Fan: ${currentValue}', action:"switchFanMode", nextState: "on"
			state "on", label:'Fan: ${currentValue}', action:"switchFanMode", nextState: "off"
			state "off", label:'Fan: ${currentValue}', action:"switchFanMode", nextState: "circulate"           
			state "circulate", label:'Fan: ${currentValue}', action:"switchFanMode", nextState: "auto"
		}
        standardTile("upButtonControl", "device.thermostatSetpoint", inactiveLabel: false, decoration: "flat") {
			state "setpoint", action:"raiseSetpoint", backgroundColor:"#d04e00", icon:"st.thermostat.thermostat-up"
		}
        valueTile("thermostatSetpoint", "device.thermostatSetpoint", width: 1, height: 1, decoration: "flat") {
			state "thermostatSetpoint", label:'${currentValue}'
		}
		valueTile("currentStatus", "device.thermostatStatus", height: 1, width: 2, decoration: "flat") {
			state "thermostatStatus", label:'${currentValue}', backgroundColor:"#ffffff"
		}
		standardTile("downButtonControl", "device.thermostatSetpoint", inactiveLabel: false, decoration: "flat") {
			state "setpoint", action:"lowerSetpoint", backgroundColor:"#d04e00", icon:"st.thermostat.thermostat-down"
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
			state "resume", label:'Resume Program', action:"device.resumeProgram", icon:"st.sonos.play-icon"
		}
		main "temperature"
        details(["temperature", "upButtonControl", "thermostatSetpoint", "currentStatus", "downButtonControl", "mode", "resumeProgram", "refresh"])
	}
}

/*
    
    preferences {
		input "highTemperature", "number", title: "Auto Mode High Temperature:", defaultValue: 80
		input "lowTemperature", "number", title: "Auto Mode Low Temperature:", defaultValue: 70
        input name: "holdType", type: "enum", title: "Hold Type", description: "When changing temperature, use Temporary or Permanent hold", required: true, options:["Temporary", "Permanent"]
  	}
    
*/


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
 
def go() {
    log.debug "before:go tile tapped"
    poll()
    log.debug "after"
}
 
def poll() {
	log.debug "Executing 'poll' using parent SmartApp"
	
	def results = parent.pollChild(this)
	parseEventData(results)
	generateStatusEvent()
    
	return null
}
 
def parseEventData(Map results)
{
	log.debug "parsing data $results"
	if(results) {
		results.each { name, value -> 
 
			def linkText = getLinkText(device)
            def isChange = false
            def isDisplayed = true
            
            if (name == "temperature" || name == "heatingSetpoint" || name == "coolingSetpoint") {
				isChange = isTemperatureStateChange(device, name, value.toString())
                isDisplayed = isChange
                   
				sendEvent(
					name: name,
					value: value,
					unit: "F",
					linkText: linkText,
					descriptionText: getThermostatDescriptionText(name, value, linkText),
					handlerName: name,
					isStateChange: isChange,
					displayed: isDisplayed)
                    
            }
            else {
            	isChange = isStateChange(device, name, value.toString())
                isDisplayed = isChange
                
                sendEvent(
					name: name,
					value: value.toString(),
					linkText: linkText,
					descriptionText: getThermostatDescriptionText(name, value, linkText),
					handlerName: name,
					isStateChange: isChange,
					displayed: isDisplayed)
                    
            }
		}

		generateSetpointEvent()  
        generateStatusEvent()
	}
}

def generateEvent(Map results) {
	parseEventData(results)
    return null
}
 
private getThermostatDescriptionText(name, value, linkText)
{
	if(name == "temperature") {
		return "$linkText was $value°F"
	}
	else if(name == "heatingSetpoint") {
		return "latest heating setpoint was $value°F"
	}
	else if(name == "coolingSetpoint") {
		return "latest cooling setpoint was $value°F"
	}
    else if (name == "thermostatMode") {
        return "thermostat mode is ${value}"
    }
    else {
        return "${name} = ${value}"
    }
}

def setHeatingSetpoint(degreesF) {
	setHeatingSetpoint(degreesF.toDouble())
}

def setHeatingSetpoint(Double degreesF) {
	log.debug "setHeatingSetpoint({$degreesF})"
	sendEvent("name":"heatingSetpoint", "value":degreesF)
	Double heatingSetpoint = device.currentValue("heatingSetpoint")
	log.debug "heatingSetpoint: $heatingSetpoint"
	parent.setHold(this, degreesF, heatingSetpoint)
}

def setCoolingSetpoint(degreesF) {
	setCoolingSetpoint(degreesF.toDouble())
}

def setCoolingSetpoint(Double degreesF) {
	log.debug "setCoolingSetpoint({$degreesF})"
	sendEvent("name":"coolingSetpoint", "value":degreesF)
	Double coolingSetpoint = device.currentValue("coolingSetpoint")
	parent.setHold(this, coolingSetpoint, degreesF)
	return null
}

def configure() {
	
}

def resumeProgram() {
	parent.resumeProgram(this)
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
   sendEvent(name: "thermostatMode", value: mode, descriptionText: "$device.displayName is in ${mode} mode", displayed: true, isStateChange: true)
}

def generateFanModeEvent(fanMode) {
   sendEvent(name: "thermostatFanMode", value: fanMode, descriptionText: "$device.displayName fan is in ${mode} mode", displayed: true, isStateChange: true)
}

def generateOperatingStateEvent(operatingState) {
   sendEvent(name: "thermostatOperatingState", value: operatingState, descriptionText: "$device.displayName is ${operatingState}", displayed: true, isStateChange: true)
}

def off() {
	log.debug "off"
	generateModeEvent("off")     
    if (parent.setMode (this,"off"))
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
	generateModeEvent("heat")    
    if (parent.setMode (this,"heat"))
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
    generateModeEvent("auxHeatOnly")    
    if (parent.setMode (this,"auxHeatOnly"))
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
    generateModeEvent("cool")
    if (parent.setMode (this,"cool"))
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
    generateModeEvent("auto")
    if (parent.setMode (this,"auto"))
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
    parent.setFanMode (this,"on")
}

def fanAuto() {
	log.debug "fanAuto"
   	parent.setFanMode (this,"auto")
}

def fanCirculate() {
	log.debug "fanCirculate"
    parent.setFanMode (this,"circulate")
}

def fanOff() {
	log.debug "fanOff"
    parent.setFanMode (this,"off")
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
        sendEvent("name":"thermostatSetpoint", "value": "${heatingSetpoint}°")
    }
    else if (mode == "cool") {
        sendEvent("name": "thermostatSetpoint", "value": "${coolingSetpoint}°")
    } else if (mode == "auto") {
        sendEvent("name": "thermostatSetpoint", "value": "${heatingSetpoint}° / ${coolingSetpoint}°")
    } else if (mode == "off") {
        sendEvent("name": "thermostatSetpoint", "value": "Off")
    } else if (mode == "emergencyHeat") {
        sendEvent("name": "thermostatSetpoint", "value": "${heatingSetpoint}°")
    }
}

def raiseSetpoint() {
   	log.debug "Raise SetPoint"
    
	def mode = device.currentValue("thermostatMode")
    def heatingSetpoint = device.currentValue("heatingSetpoint").toInteger()
    def coolingSetpoint = device.currentValue("coolingSetpoint").toInteger()
    
    log.debug "Current Mode = ${mode}"
    
    if (mode == "heat" || mode == "emergencyHeat") {
        heatingSetpoint++
        if (heatingSetpoint > 100) { heatingSetpoint = 100 }
        
        sendEvent("name": "thermostatSetpoint", "value": "${heatingSetpoint}°")        
        sendEvent("name": "heatingSetpoint", "value": heatingSetpoint)
        
        parent.setHold (this, heatingSetpoint, coolingSetpoint)
        
        log.debug "New Heating Setpoint = ${heatingSetpoint}"
    }
    else if (mode == "cool") {
        coolingSetpoint++
        if (coolingSetpoint > 100) { coolingSetpoint = 100 }
        
        sendEvent("name": "thermostatSetpoint", "value": "${coolingSetpoint}°")
        sendEvent("name": "coolingSetpoint", "value": coolingSetpoint)
        
        parent.setHold (this, heatingSetpoint, coolingSetpoint)
        
        log.debug "New Cooling Setpoint = ${coolingSetpoint}"
    }
    else if (mode == "auto") {
    	heatingSetpoint++
        if (heatingSetpoint > 100) { heatingSetpoint = 100 }
        
        coolingSetpoint++
        if (coolingSetpoint > 100) { coolingSetpoint = 100 }
        
        sendEvent("name": "heatingSetpoint", "value": heatingSetpoint)
        sendEvent("name": "coolingSetpoint", "value": coolingSetpoint)
        sendEvent("name": "thermostatSetpoint", "value": "${heatingSetpoint}° / ${coolingSetpoint}°")
        
        parent.setHold (this, heatingSetpoint, coolingSetpoint)
        log.debug "New Setpoint = ${heatingSetpoint}° / ${coolingSetpoint}°"
    }
    generateStatusEvent()
    return null()
}

def lowerSetpoint() {
   	log.debug "Lower SetPoint"
    
	def mode = device.currentValue("thermostatMode")
    def heatingSetpoint = device.currentValue("heatingSetpoint").toInteger()
    def coolingSetpoint = device.currentValue("coolingSetpoint").toInteger()
    
    log.debug "Current Mode = ${mode}, Current Heating Setpoint = ${heatingSetpoint}, Current Cooling Setpoint = ${coolingSetpoint}"
    
    if (mode == "heat" || mode == "emergencyHeat") {
        heatingSetpoint--
        if (heatingSetpoint < 45) { heatingSetpoint = 45 }

        sendEvent("name": "thermostatSetpoint", "value": "${heatingSetpoint}°")        
        sendEvent("name": "heatingSetpoint", "value": heatingSetpoint)
        
        parent.setHold (this, heatingSetpoint, coolingSetpoint)
        
        log.debug "New Heating Setpoint = ${heatingSetpoint}"
        
    }
    else if (mode == "cool") {
        coolingSetpoint--
        if (coolingSetpoint < 45) { coolingSetpoint = 45 }
        
        sendEvent("name":"thermostatSetpoint", "value": "${coolingSetpoint}°")
        sendEvent("name":"coolingSetpoint", "value": coolingSetpoint) 
        
        parent.setHold (this, heatingSetpoint, coolingSetpoint)
        
        log.debug "New Cooling Setpoint = ${coolingSetpoint}"
    }
    else if (mode == "auto") {
    	heatingSetpoint--
        if (heatingSetpoint < 45) { heatingSetpoint = 45 }
        
        coolingSetpoint--
        if (coolingSetpoint < 45) { coolingSetpoint = 45 }
        
        sendEvent("name": "heatingSetpoint", "value": heatingSetpoint)
        sendEvent("name": "coolingSetpoint", "value": coolingSetpoint)
        sendEvent("name": "thermostatSetpoint", "value": "${heatingSetpoint}° / ${coolingSetpoint}°")
        
        parent.setHold (this, heatingSetpoint, coolingSetpoint)
        log.debug "New Setpoint = ${heatingSetpoint}° / ${coolingSetpoint}°"
    }

    generateStatusEvent()
    return null()
}

def generateStatusEvent() {
    def statusText = device.currentValue("thermostatStatus")
    log.debug "Generate Status Event = ${statusText}"
    sendEvent("name": "thermostatStatus", "value": statusText, "description": statusText, displayed: true, isStateChange: true)
}
