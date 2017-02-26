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

def refresh()
{
  log.debug "refresh called"
  poll()
  log.debug "refresh ended"
}
 
def go()
{
  log.debug "before:go tile tapped"
  poll()
  log.debug "after"
}
 
void poll() {
	log.debug "Executing 'poll' using parent SmartApp"
	
	def results = parent.pollChild(this)
	parseEventData(results)
	generateStatusEvent()
}
 
def parseEventData(Map results)
{
	log.debug "parsing data $results"
	if(results)
	{
		results.each { name, value -> 
 
			def linkText = getLinkText(device)
            def isChange = false
            def isDisplayed = true
            
            if (name=="temperature" || name=="heatingSetpoint" || name=="coolingSetpoint") {
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
		generateSetpointEvent ()  
        generateStatusEvent ()
	}
}

void generateEvent(Map results)
{
	log.debug "parsing data $results"
	if(results)
	{
		results.each { name, value -> 
        
 			def linkText = getLinkText(device)
            def isChange = false
            def isDisplayed = true
            
            if (name=="temperature" || name=="heatingSetpoint" || name=="coolingSetpoint") {
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
		generateSetpointEvent () 
        generateStatusEvent()
	}
}
 
private getThermostatDescriptionText(name, value, linkText)
{
	if(name == "temperature")
	{
		return "$linkText was $value°F"
	}
	else if(name == "heatingSetpoint")
	{
		return "latest heating setpoint was $value°F"
	}
	else if(name == "coolingSetpoint")
	{
		return "latest cooling setpoint was $value°F"
	}
    else if (name == "thermostatMode")
    {
        return "thermostat mode is ${value}"
    }
    else
    {
        return "${name} = ${value}"
    }
}


void setHeatingSetpoint(degreesF) {
	setHeatingSetpoint(degreesF.toDouble())
}

void setHeatingSetpoint(Double degreesF) {
	log.debug "setHeatingSetpoint({$degreesF})"
	sendEvent("name":"heatingSetpoint", "value":degreesF)
	Double coolingSetpoint = device.currentValue("coolingSetpoint")
	log.debug "coolingSetpoint: $coolingSetpoint"
	parent.setHold(this, degreesF, coolingSetpoint)
}

void setCoolingSetpoint(degreesF) {
	setCoolingSetpoint(degreesF.toDouble())
}

void setCoolingSetpoint(Double degreesF) {
	log.debug "setCoolingSetpoint({$degreesF})"
	sendEvent("name":"coolingSetpoint", "value":degreesF)
	Double heatingSetpoint = device.currentValue("heatingSetpoint")
	parent.setHold(this, heatingSetpoint, degreesF)
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
        
        sendEvent("name":"thermostatSetpoint", "value":heatingSetpoint.toString()+"°")                       
        
    }
    else if (mode == "cool") {
        
        sendEvent("name":"thermostatSetpoint", "value":coolingSetpoint.toString()+"°")     

    } else if (mode == "auto") {
        
        sendEvent("name":"thermostatSetpoint", "value":"Auto")

    } else if (mode == "off") {
            
        sendEvent("name":"thermostatSetpoint", "value":"Off")    

    } else if (mode == "emergencyHeat") {
        
        sendEvent("name":"thermostatSetpoint", "value":heatingSetpoint.toString()+"°")           

    }

}

void raiseSetpoint() {

   	log.debug "Raise SetPoint"
    
	def mode = device.currentValue("thermostatMode")
    def heatingSetpoint = device.currentValue("heatingSetpoint").toInteger()
    def coolingSetpoint = device.currentValue("coolingSetpoint").toInteger()
    
    log.debug "Current Mode = ${mode}"
    
    if (mode == "heat") {
        
        heatingSetpoint++
        
        if (heatingSetpoint > 99)
           heatingSetpoint = 99
        
        sendEvent("name":"thermostatSetpoint", "value":heatingSetpoint.toString()+"°")        
        sendEvent("name":"heatingSetpoint", "value":heatingSetpoint)               
        
        parent.setHold (this, heatingSetpoint, coolingSetpoint)
        
        log.debug "New Heating Setpoint = ${heatingSetpoint}"
        
    }
    else if (mode == "cool") {
                
        coolingSetpoint++
        
        if (coolingSetpoint > 99)
            coolingSetpoint = 99
        
        sendEvent("name":"thermostatSetpoint", "value":coolingSetpoint.toString()+"°")
        sendEvent("name":"coolingSetpoint", "value":coolingSetpoint)       
        
        parent.setHold (this, heatingSetpoint, coolingSetpoint)
        
        log.debug "New Cooling Setpoint = ${coolingSetpoint}"        
        
    }
    generateStatusEvent()

}


void lowerSetpoint() {
   	log.debug "Lower SetPoint"
    
	def mode = device.currentValue("thermostatMode")
    def heatingSetpoint = device.currentValue("heatingSetpoint").toInteger()
    def coolingSetpoint = device.currentValue("coolingSetpoint").toInteger()
    
    log.debug "Current Mode = ${mode}, Current Heating Setpoint = ${heatingSetpoint}, Current Cooling Setpoint = ${coolingSetpoint}"
    
    if (mode == "heat" || mode == "emergencyHeat") {
        
        heatingSetpoint--  
        
        if (heatingSetpoint < 32)
           heatingSetpoint = 32

        sendEvent("name":"thermostatSetpoint", "value":heatingSetpoint.toString()+"°")        
        sendEvent("name":"heatingSetpoint", "value":heatingSetpoint)
        
        parent.setHold (this, heatingSetpoint, coolingSetpoint)
        
        log.debug "New Heating Setpoint = ${heatingSetpoint}"
        
    }
    else if (mode == "cool") {
                
        coolingSetpoint--
        
        if (coolingSetpoint < 32)
           coolingSetpoint = 32
        
        sendEvent("name":"thermostatSetpoint", "value":coolingSetpoint.toString()+"°")
        sendEvent("name":"coolingSetpoint", "value":coolingSetpoint) 
        
        parent.setHold (this, heatingSetpoint, coolingSetpoint)
        
        log.debug "New Cooling Setpoint = ${coolingSetpoint}"
        
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
    sendEvent("name":"thermostatStatus", "value":statusText, "description":statusText, displayed: true, isStateChange: true)
}