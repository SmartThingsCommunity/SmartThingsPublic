/**
 *  Sensibo
 *
 *  Copyright 2015 Eric Gosselin
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

import groovyx.net.http.HTTPBuilder
import groovyx.net.http.ContentType
import groovyx.net.http.Method
import groovyx.net.http.RESTClient


preferences {
}

metadata {
	definition (name: "SensiboPod", namespace: "EricG66", author: "Eric Gosselin", oauth: false) {
		capability "Relative Humidity Measurement"
		capability "Temperature Measurement"
		capability "Polling"
        capability "Refresh"
        capability "Switch"
        capability "Thermostat"
        capability "Battery"
        command "switchFanLevel"
        command "switchMode"
        command "raiseCoolSetpoint"
        command "lowerCoolSetpoint"
        command "raiseHeatSetpoint"
        command "lowerHeatSetpoint"
        command "heatingSetpointUp"
		command "heatingSetpointDown"
		command "coolingSetpointUp"
		command "coolingSetpointDown"
        command "battvoltage"
        command "raiseTemperature"
        command "lowerTemperature"
	}

	simulator {
		// TODO: define status and reply messages here
		
        // status messages
		status "on": "on/off: 1"
		status "off": "on/off: 0"
        status "temperature":"10"
        //status "humidity": "humidity"
        
        // reply messages
		reply "zcl on-off on": "on/off: 1"
		reply "zcl on-off off": "on/off: 0"
	}

	tiles(scale: 2) {
    	multiAttributeTile(name:"thermostatMulti", type:"thermostat",, width:6, height:4) {
   			tileAttribute("device.temperature", key: "PRIMARY_CONTROL") {
            	attributeState ("default", label:'${currentValue}°')//, 
                //backgroundColors:[
				//	[value: 15, color: "#153591"],
				//	[value: 18, color: "#1e9cbb"],
				//	[value: 21, color: "#90d2a7"],
				//	[value: 24, color: "#44b621"],
				//	[value: 27, color: "#f1d801"],
				//	[value: 30, color: "#d04e00"],
				//	[value: 33, color: "#bc2323"],
                //    [value: 59, color: "#153591"],
				//	[value: 64, color: "#1e9cbb"],
				//	[value: 70, color: "#90d2a7"],
				//	[value: 75, color: "#44b621"],
				//	[value: 81, color: "#f1d801"],
				//	[value: 86, color: "#d04e00"],
				//	[value: 91, color: "#bc2323"]
				//])
    		}
            tileAttribute("device.targetTemperature", key: "VALUE_CONTROL") {
    			//attributeState("default", action: "setTemperature")
                //attributeState("VALUE_UP", action: "raiseCoolSetpoint")
                //attributeState("VALUE_DOWN", action: "lowerCoolSetpoint")
                attributeState("VALUE_UP", action: "raiseTemperature")
                attributeState("VALUE_DOWN", action: "lowerTemperature")    			
  			}
            //tileAttribute ("statusText", key: "SECONDARY_CONTROL") {
			//	attributeState "statusText", label:'${currentValue}'
			//}
            tileAttribute("device.humidity", key: "SECONDARY_CONTROL") {
   				attributeState("default", label:'${currentValue}%', unit:"%")
  			}
            tileAttribute("device.thermostatOperatingState", key: "OPERATING_STATE") {
    			attributeState("idle", backgroundColor:"#a9a9a9")
    			attributeState("heating", backgroundColor:"#ffa81e")
    			attributeState("cooling", backgroundColor:"#269bd2")
                attributeState("fan only", backgroundColor:"#44b621")
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
        
		valueTile("temperature", "device.temperature", width: 2, height: 2) {
			state("temperature", label:'Temp: ${currentValue}',
				backgroundColors:[
					[value: 15, color: "#153591"],
					[value: 18, color: "#1e9cbb"],
					[value: 21, color: "#90d2a7"],
					[value: 24, color: "#44b621"],
					[value: 27, color: "#f1d801"],
					[value: 30, color: "#d04e00"],
					[value: 33, color: "#bc2323"],
                    [value: 59, color: "#153591"],
					[value: 64, color: "#1e9cbb"],
					[value: 70, color: "#90d2a7"],
					[value: 75, color: "#44b621"],
					[value: 81, color: "#f1d801"],
					[value: 86, color: "#d04e00"],
					[value: 91, color: "#bc2323"]
				]
			)
		}
        
		valueTile("humidity", "device.humidity", width: 2, height: 2) {
			state("humidity", label:'Humidity: ${currentValue} %',
				backgroundColors:[
					[value: 15, color: "#153591"],
					[value: 18, color: "#1e9cbb"],
					[value: 21, color: "#90d2a7"],
					[value: 24, color: "#44b621"],
					[value: 27, color: "#f1d801"],
					[value: 30, color: "#d04e00"],
					[value: 33, color: "#bc2323"],
                    [value: 59, color: "#153591"],
					[value: 64, color: "#1e9cbb"],
					[value: 70, color: "#90d2a7"],
					[value: 75, color: "#44b621"],
					[value: 81, color: "#f1d801"],
					[value: 86, color: "#d04e00"],
					[value: 91, color: "#bc2323"]
				]
			)
		}
           
        valueTile("voltage", "device.battvoltage", width: 2, height: 2) {
			state("battvoltage", label:'${currentValue}',
				backgroundColors:[
					[value: 2700, color: "#CC0000"],
					[value: 2800, color: "#FFFF00"],
					[value: 2900, color: "#00FF00"]
                    //[value: 20, color: "#CC0000"],
					//[value: 40, color: "#FFFF00"],
					//[value: 60, color: "#00FF00"]
                ]
           )
        }
            
        standardTile("switch", "device.switch", width: 2, height: 2, canChangeIcon: true) {
			state "on", label:'${name}', action:"switch.off", icon:"st.switches.switch.on", backgroundColor:"#79b821"
			state "off", label:'${name}', action:"switch.on", icon:"st.switches.switch.off", backgroundColor:"#ffffff"
		}
        
        standardTile("fanLevel", "device.fanLevel", width: 2, height: 2) {
            state "low", action:"switchFanLevel", backgroundColor:"#8C8C8D", icon:"http://i130.photobucket.com/albums/p242/brutalboy_photos/fan_low_2.png", nextState:"medium"
            state "medium", action:"switchFanLevel", backgroundColor:"#8C8C8D", icon:"http://i130.photobucket.com/albums/p242/brutalboy_photos/fan_medium_2.png", nextState:"high"
            state "high", action:"switchFanLevel", backgroundColor:"#8C8C8D", icon:"http://i130.photobucket.com/albums/p242/brutalboy_photos/fan_high_2.png", nextState:"auto"
            state "auto", action:"switchFanLevel", backgroundColor:"#8C8C8D", icon:"http://i130.photobucket.com/albums/p242/brutalboy_photos/fan_auto_2.png" , nextState:"low"
        }
        
        standardTile("mode", "device.thermostatMode",  width: 2, height: 2) {
            state "cool", action:"switchMode", backgroundColor:"#0099FF", icon:"st.thermostat.cool", nextState:"heat"
            state "heat", action:"switchMode", backgroundColor:"#FF3300", icon:"st.thermostat.heat", nextState:"auto"
            state "auto", action:"switchMode", backgroundColor:"#e8e3d8", icon:"st.thermostat.fan-on", nextState:"cool"         
        }
        
        standardTile("upCoolButtonControl", "device.targetTemperature", inactiveLabel: false, decoration: "flat", width: 1, height: 2) {
			state "setpoint", action:"raiseCoolSetpoint", icon:"st.thermostat.thermostat-up",label :"Up"
		}
        
        standardTile("downCoolButtonControl", "device.targetTemperature", inactiveLabel: false, decoration: "flat", width: 1, height: 2) {
			state "setpoint", action:"lowerCoolSetpoint", icon:"st.thermostat.thermostat-down", label :"Down"
		}
        
        standardTile("refresh", "device.switch", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "default", label:'', action:"refresh.refresh", icon:"st.secondary.refresh"
		}
		       
		main (["switch"])
		details (["thermostatMulti","switch","fanLevel","mode","voltage","refresh"])    
	}
}

def fanLevelModes() {
	["low", "medium","high", "auto"]
}

def Modes() {
   ["cool","heat","fan"]
}

def heat() {
    device.currentState("mode").value = "heat"
    modeHeat()
}

def emergencyHeat() {
	device.currentState("mode").value = "heat"
    modeHeat()
}

def cool() {
	device.currentState("mode").value = "cool"
    modeCool()
}

def auto() {
	device.currentState("mode").value = "fan"
    modeFan()
}

def fanOn(){
	device.currentState("fanLevel").value = "medium"
    fanMedium()
}

def fanAuto(){
	device.currentState("fanLevel").value = "auto"
    fanAuto()
}

def fanCirculate(){
	device.currentState("fanLevel").value = "low"
    fanLow()
}

void raiseTemperature() {
	def operMode = device.currentState("mode").value
    
    def Setpoint = device.currentValue("targetTemperature").toInteger()
    def theTemp = device.currentValue("temperatureUnit")
    //if (device.currentValue("temperatureUnit") == "F") {
    //	Setpoint = Math.round(fToC(Setpoint))
    //}
    
    log.debug "Current target temperature = ${Setpoint}"

    Setpoint++  
        
    if (Setpoint > 30)
    	Setpoint = 30
        
    switch (operMode) {
    	case "heat":
        	setHeatingSetpoint(Setpoint)
            break;
        case "cool":
        	setCoolingSetpoint(Setpoint)
            break;
        case "auto":
            setHeatingSetpoint(Setpoint)
        	setCoolingSetpoint(Setpoint)
        	break;
        default:
        	break;
    }
}

void lowerTemperature() {
	def operMode = device.currentState("mode").value
    
    def Setpoint = device.currentValue("targetTemperature").toInteger()
    def theTemp = device.currentValue("temperatureUnit")
    //if (device.currentValue("temperatureUnit") == "F") {
    //	Setpoint = Math.round(fToC(Setpoint))
    //}
    
    log.debug "Current target temperature = ${Setpoint}"

    Setpoint--  
        
    if (Setpoint < 16)
    	Setpoint = 16
        
    switch (operMode) {
    	case "heat":
        	setHeatingSetpoint(Setpoint)
            break;
        case "cool":
        	setCoolingSetpoint(Setpoint)
            break;
        case "auto":
            setHeatingSetpoint(Setpoint)
        	setCoolingSetpoint(Setpoint)
        	break;
        default:
        	break;
    }
}

void lowerCoolSetpoint() {
   	log.debug "Lower SetPoint"
    
    def Setpoint = device.currentValue("targetTemperature").toInteger()
   	def theTemp = device.currentValue("temperatureUnit")
    //if (device.currentValue("temperatureUnit") == "F") {
    //	Setpoint = Math.round(fToC(Setpoint))
    //}
    log.debug "Current target temperature = ${Setpoint}"

    Setpoint--  
    
    if (Setpoint < 16)
    	Setpoint = 16

    def result = parent.setACStates(this, device.deviceNetworkId , "on", device.currentState("mode").value, Setpoint, device.currentState("fanLevel").value)

    if (result) {
        if (device.currentState("on").value == "off") { generateSwitchEvent("on") }
        
    	sendEvent(name: 'coolingSetpoint', value: Setpoint, unit: theTemp, displayed: false)
    	//sendEvent(name: 'heatingSetpoint', value: Setpoint, unit: theTemp, displayed: false)
       
        //if (device.currentValue("temperatureUnit") == "F") {
    	//	Setpoint = Math.round(cToF(Setpoint))
    	//}
        generateSetTempEvent(Setpoint)
        
    	log.debug "New target Temperature = ${Setpoint}"       
    }
	else {
    	log.debug "error"
       	generateErrorEvent()
        //refresh()
    }
	generateStatusEvent()
    refresh()
}

void raiseCoolSetpoint() {
   	log.debug "Raise SetPoint"
    
    def Setpoint = device.currentValue("targetTemperature").toInteger()
    def theTemp = device.currentValue("temperatureUnit")
    //if (device.currentValue("temperatureUnit") == "F") {
    //	Setpoint = Math.round(fToC(Setpoint))
    //}
    
    log.debug "Current target temperature = ${Setpoint}"

    Setpoint++  
        
    if (Setpoint > 30)
    	Setpoint = 30

    def result = parent.setACStates(this, device.deviceNetworkId , "on", device.currentState("mode").value, Setpoint, device.currentState("fanLevel").value)
    if (result) {
        if (device.currentState("on").value == "off") { generateSwitchEvent("on") }
        
    	sendEvent(name: 'coolingSetpoint', value: Setpoint, unit: theTemp, displayed: false)
    	//sendEvent(name: 'heatingSetpoint', value: Setpoint, unit: theTemp, displayed: false)
    	
        //if (device.currentValue("temperatureUnit") == "F") {
    	//	Setpoint = Math.round(cToF(Setpoint))
    	//}
        generateSetTempEvent(Setpoint)
        
    	log.debug "New target Temperature = ${Setpoint}"       
    }
	else {
       	generateErrorEvent()
        //refresh()
    }
	generateStatusEvent()
    refresh()
}

def refresh()
{
  log.debug "refresh called"
  poll()
   
  log.debug "refresh ended"
}

// Set Temperature
def setCoolingSetpoint(temp) {
	log.debug "setTemperature"   
    
    def result = parent.setACStates(this, device.deviceNetworkId , "on", "cool", temp, device.currentState("fanLevel").value)
    
    if (result) {    
    	if (device.currentState("on").value == "off") { generateSwitchEvent("on") }
  		generateModeEvent("cool")
         
    	sendEvent(name: 'coolingSetpoint', value: temp, displayed: false)
    	//sendEvent(name: 'heatingSetpoint', value: temp, displayed: false)
    	generateSetTempEvent(temp)
    }
    else {
       	generateErrorEvent()
        //refresh()
    }
    
    generateStatusEvent()
    refresh()
}

def setHeatingSetpoint(temp) {
	log.debug "setTemperature"
    
    def result = parent.setACStates(this, device.deviceNetworkId , "on", "heat", temp, device.currentState("fanLevel").value)
    if (result) { 
        if (device.currentState("on").value == "off") { generateSwitchEvent("on") }
    	generateModeEvent("heat")
    	//sendEvent(name: 'coolingSetpoint', value: temp, displayed: false)
    	sendEvent(name: 'heatingSetpoint', value: temp, displayed: false)
    	generateSetTempEvent(temp)
	}	
    else {
       	generateErrorEvent()
        //refresh()
    }    
    generateStatusEvent()
    refresh()
}

def generateSetTempEvent(temp) {
   sendEvent(name: "targetTemperature", value: temp, descriptionText: "$device.displayName set temperature is now ${temp}", displayed: true, isStateChange: true)
}

// Turn off or Turn on the AC
def on() {
	log.debug "on called"
    
    def Setpoint = device.currentValue("targetTemperature").toInteger()
   
    //if (device.currentValue("temperatureUnit") == "F") {
    //	Setpoint = Math.round(fToC(Setpoint))
    //}
    
    def result = parent.setACStates(this, device.deviceNetworkId, "on", device.currentState("mode").value, Setpoint, device.currentState("fanLevel").value)
    if (result) {   
    	sendEvent(name: 'thermostatMode', value: device.currentState("mode").value, displayed: false,isStateChange: true)
        //sendEvent(name: 'thermostatOperatingState', value: "idle",isStateChange: true)
    	
        generateSwitchEvent("on")
    }
    else {
       	generateErrorEvent()
        //refresh()
    }        
    generateStatusEvent() 
    refresh()
}

def off() {
	log.debug "off called"
    
     def Setpoint = device.currentValue("targetTemperature").toInteger()
   
    //if (device.currentValue("temperatureUnit") == "F") {
    //	Setpoint = Math.round(fToC(Setpoint))
    //}
    
    def result = parent.setACStates(this, device.deviceNetworkId, "off", device.currentState("mode").value, Setpoint, device.currentState("fanLevel").value)

    if (result) { 
    	sendEvent(name: 'thermostatMode', value: "off", displayed: false,isStateChange: true)
        //sendEvent(name: 'thermostatOperatingState', value: "idle",isStateChange: true)
    	
        generateSwitchEvent("off")
     }
    else {
       	generateErrorEvent()
        //refresh()
    }         
    generateStatusEvent()
    refresh()
}

def generateSwitchEvent(mode) {
   sendEvent(name: "on", value: mode, descriptionText: "$device.displayName is now ${mode}", displayed: false, isStateChange: true)  
   sendEvent(name: "switch", value: mode, descriptionText: "$device.displayName is now ${mode}", displayed: true, isStateChange: true)   
   
   //refresh()
}

// For Fan Level
def dfanLow() {
	log.debug "fanLow"

    def Setpoint = device.currentValue("targetTemperature").toInteger()
   
    //if (device.currentValue("temperatureUnit") == "F") {
    //	Setpoint = Math.round(fToC(Setpoint))
    //}
    
    def result = parent.setACStates(this, device.deviceNetworkId ,"on", device.currentState("mode").value, Setpoint, "low")
    if (result) {
        if (device.currentState("on").value == "off") { generateSwitchEvent("on") }
    	sendEvent(name: 'thermostatFanMode', value: "circulate", displayed: false)
    	generatefanLevelEvent("low")
    }
    else {
       	generateErrorEvent()
        //refresh()
    }       
    generateStatusEvent()
    refresh()
}

def dfanMedium() {
	log.debug "fanMedium"
    
    def Setpoint = device.currentValue("targetTemperature").toInteger()
   
    //if (device.currentValue("temperatureUnit") == "F") {
    //	Setpoint = Math.round(fToC(Setpoint))
    //}
    def result = parent.setACStates(this, device.deviceNetworkId,"on", device.currentState("mode").value, Setpoint, "medium")
    if (result) {
   		if (device.currentState("on").value == "off") { generateSwitchEvent("on") }
    	sendEvent(name: 'thermostatFanMode', value: "on", displayed: false)
    	generatefanLevelEvent("medium")
    }
    else {
       	generateErrorEvent()
        //refresh()
    }      
    generateStatusEvent()
    refresh()
}

def dfanHigh() {
	log.debug "fanHigh"
    
    def Setpoint = device.currentValue("targetTemperature").toInteger()
   
    //if (device.currentValue("temperatureUnit") == "F") {
    //	Setpoint = Math.round(fToC(Setpoint))
    //}
    def result = parent.setACStates(this, device.deviceNetworkId,"on", device.currentState("mode").value, Setpoint, "high")
    if (result) {
     	if (device.currentState("on").value == "off") { generateSwitchEvent("on") }   
    	sendEvent(name: 'thermostatFanMode', value: "on", displayed: false)
    	generatefanLevelEvent("high")
    }
    else {
       	generateErrorEvent()
        //refresh()
    }          
    generateStatusEvent()
    refresh()
}

def dfanAuto() {
	log.debug "fanAuto"
    
    def Setpoint = device.currentValue("targetTemperature").toInteger()
   
    //if (device.currentValue("temperatureUnit") == "F") {
    //	Setpoint = Math.round(fToC(Setpoint))
    //}

	def fanLevel = "auto"
	// adapted to my AC model which no auto fanspeed in fan mode
    if (device.currentState("mode").value == "fan") {
    	fanLevel = "low"
    }

    def result = parent.setACStates(this, device.deviceNetworkId, "on", device.currentState("mode").value, Setpoint, fanLevel)
    if (result) {
        if (device.currentState("on").value == "off") { generateSwitchEvent("on") }
    	sendEvent(name: 'thermostatFanMode', value: "auto", displayed: false)
    	generatefanLevelEvent(fanLevel)
    }
    else {
       	generateErrorEvent()
        //refresh()
    }        
    generateStatusEvent() 
    refresh()
}

def generatefanLevelEvent(mode) {
   sendEvent(name: "fanLevel", value: mode, descriptionText: "$device.displayName fan level is now ${mode}", displayed: true, isStateChange: true)
}

def switchFanLevel() {
	log.debug "switchFanLevel"
	def currentFanMode = device.currentState("fanLevel")?.value
	log.debug "switching fan level from current mode: $currentFanMode"
	def returnCommand

	switch (currentFanMode) {
		case "low":
			returnCommand = dfanMedium()
			break
		case "medium":
			returnCommand = dfanHigh()
			break
		case "high":
			returnCommand = dfanAuto()
			break
        case "auto":
			returnCommand = dfanLow()
			break
	}

	returnCommand
}

// To change the AC mode

def switchMode() {
	log.debug "switchMode"
	def currentMode = device.currentState("mode")?.value
	log.debug "switching AC mode from current mode: $currentMode"
	def returnCommand

	switch (currentMode) {
		case "cool":
			returnCommand = modeHeat()
			break
		case "heat":
			returnCommand = modeFan()
			break
		case "fan":
			returnCommand = modeCool()
			break
	}

	returnCommand
}

def modeHeat() {
	log.debug "modeHeat"
    
    def Setpoint = device.currentValue("targetTemperature").toInteger()
   
    //if (device.currentValue("temperatureUnit") == "F") {
    //	Setpoint = Math.round(fToC(Setpoint))
    //}    
    def result = parent.setACStates(this, device.deviceNetworkId, "on", "heat", Setpoint, device.currentState("fanLevel").value)
    if (result) {
    	sendEvent(name: 'thermostatMode', value: "heat", displayed: false,isStateChange: true)
        if (device.currentState("on").value == "off") { generateSwitchEvent("on") }
    	
    	generateModeEvent("heat")
    }
    else {
       	generateErrorEvent()
        //refresh()
    }      
    generateStatusEvent()
    refresh()
}

def modeCool() {
	log.debug "modeCool"

    def Setpoint = device.currentValue("targetTemperature").toInteger()
   
    //if (device.currentValue("temperatureUnit") == "F") {
    //	Setpoint = Math.round(fToC(Setpoint))
    //}
    if (device.currentState("on").value == "off") { generateSwitchEvent("on") }
    def result = parent.setACStates(this, device.deviceNetworkId, "on", "cool", Setpoint, device.currentState("fanLevel").value)
    if (result) {
    	sendEvent(name: 'thermostatMode', value: "cool", displayed: false,isStateChange: true)
        if (device.currentState("on").value == "off") { generateSwitchEvent("on") }
    	
    	generateModeEvent("cool")
    }
    else {
       	generateErrorEvent()
        //refresh()
    }    
    generateStatusEvent() 
    refresh()
}

def modeFan() {
	log.debug "modeFan"

    def Setpoint = device.currentValue("targetTemperature").toInteger()
   
    //if (device.currentValue("temperatureUnit") == "F") {
    //	Setpoint = Math.round(fToC(Setpoint))
    //}
    // adapted to my AC model which no auto fanspeed in fan mode
    def LevelBefore = device.currentState("fanLevel").value.toString()
    def Level = (device.currentState("fanLevel").value == "auto") ? "medium" : device.currentState("fanLevel").value
    def result = parent.setACStates(this, device.deviceNetworkId, "on", "fan", Setpoint, Level)
    if (result) {
    	if (LevelBefore == "auto") {
     		generatefanLevelEvent("medium")
    	}
        sendEvent(name: 'thermostatMode', value: "auto", displayed: false,isStateChange: true)
        
        if (device.currentState("on").value == "off") { generateSwitchEvent("on") }
        
    	generateModeEvent("fan")
    }
    else {
       	generateErrorEvent()
        //refresh()
    }       
    generateStatusEvent()
    refresh()
}

def generateModeEvent(mode) {
   sendEvent(name: "mode", value: mode, descriptionText: "$device.displayName mode is now ${mode}", displayed: true, isStateChange: true)   
}

def generateErrorEvent() {
   log.debug "Event Error"
   sendEvent(name: "Error", value: "Error", descriptionText: "$device.displayName FAILED to set or get the AC State", displayed: true, isStateChange: true)  
}


void poll() {
	log.debug "Executing 'poll' using parent SmartApp"
	
	def results = parent.pollChild(this)
	
    def linkText = getLinkText(device)
                    
    parseTempUnitEventData(results)
    parseEventData(results)
	//generateStatusEvent()
}

def parseTempUnitEventData(Map results)
{
    log.debug "parsing data $results"
	if(results)
	{
		results.each { name, value ->
        	if (name=="temperatureUnit") { 
            	def linkText = getLinkText(device)
                def isChange = true
                def isDisplayed = false
                   
				sendEvent(
					name: name,
					value: value,
                    unit: value,
					linkText: linkText,
					descriptionText: "${name} = ${value}",
					handlerName: "temperatureUnit",
					isStateChange: isChange,
					displayed: isDisplayed)
            }
        }
 	}
}

def parseEventData(Map results)
{
	log.debug "parsing data $results"
	if(results)
	{
		results.each { name, value -> 
 
 			log.debug "name :" + name + " value :" + value
			def linkText = getLinkText(device)
            def isChange = false
            def isDisplayed = true
                             
            if (name=="battvoltage") {            	                
                 isChange = true //isTemperatureStateChange(device, name, value.toString())
                isDisplayed = true
                  
				sendEvent(
					name: name,
					value: value,
                    unit: "mA",
					linkText: linkText,
					descriptionText: getThermostatDescriptionText(name, value, linkText),
					handlerName: name,
					isStateChange: isChange,
					displayed: isDisplayed)
            	}
            //if (name== "battery") {            	                
            //    isChange = true //isTemperatureStateChange(device, name, value.toString())
            //    isDisplayed = false
                  
			//	sendEvent(
			//		name: name,
			//		value: value,
                    //unit: "V",
			//		linkText: linkText,
			//		descriptionText: getThermostatDescriptionText(name, value, linkText),
			//		handlerName: name,
			//		isStateChange: isChange,
			//		displayed: isDisplayed)
            //	}
            else if (name=="on") {            	
                isChange = true
                isDisplayed = false
                   
				sendEvent(
					name: name,
					value: value,
					linkText: linkText,
					descriptionText: getThermostatDescriptionText(name, value, linkText),
					handlerName: name,
					isStateChange: isChange,
					displayed: isDisplayed)
                    
                sendEvent(name: "switch", value: value)
            }
            else if (name=="thermostatMode") {
                isChange = true //isTemperatureStateChange(device, name, value.toString())
                isDisplayed = false
                
                def mode = (value.toString() != "fan") ?: "auto"
                
                if (value=="cool") {
					sendEvent(name: 'thermostatOperatingState', value: "cooling", 
					isStateChange: isChange,
					displayed: isDisplayed)
				} else if (value=="heat") {
					sendEvent(name: 'thermostatOperatingState', value: "heating", 
					isStateChange: isChange,
					displayed: isDisplayed)
				} else if (value=="auto") {
					sendEvent(name: 'thermostatOperatingState', value: "fan only", 
					isStateChange: isChange,
					displayed: isDisplayed)
				} else {
					sendEvent(name: 'thermostatOperatingState', value: "idle", 
					isStateChange: isChange,
					displayed: isDisplayed)
				}   
				sendEvent(
					name: name,
					value: value,
					linkText: linkText,
					descriptionText: getThermostatDescriptionText(name, value, linkText),
					handlerName: name,
					isStateChange: isChange,
					displayed: isDisplayed)
            	}
            else if (name=="coolingSetpoint" || name== "heatingSetpoint") {           	
                isChange = true //isTemperatureStateChange(device, name, value.toString())
                isDisplayed = false
                
				sendEvent(
					name: name,
					value: value,
                    unit : device.currentValue("temperatureUnit"),
					linkText: linkText,
					descriptionText: getThermostatDescriptionText(name, value, linkText),
					handlerName: name,
					isStateChange: isChange,
					displayed: isDisplayed)
            	}
            else if (name=="thermostatFanMode") {
            	def mode = (value.toString() == "high" || value.toString() == "medium") ? "on" : value.toString()
                mode = (mode == "low") ? "circulate" : mode
               	
                isChange = true //isTemperatureStateChange(device, name, value.toString())
                isDisplayed = false
                   
				sendEvent(
					name: name,
					value: value,
					linkText: linkText,
					descriptionText: getThermostatDescriptionText(name, value, linkText),
					handlerName: name,
					isStateChange: isChange,
					displayed: isDisplayed)
            	}
            else if (name=="temperature" || name== "lastTemperaturePush" || name== "lastHumidityPush") {
				isChange = true //isTemperatureStateChange(device, name, value.toString())
                isDisplayed = isChange
				
				sendEvent(
					name: name,
					value: value,
					unit: device.currentValue("temperatureUnit"),
					linkText: linkText,
					descriptionText: getThermostatDescriptionText(name, value, linkText),
					handlerName: name,
					isStateChange: isChange,
					displayed: isDisplayed)
                    
            }
            else if (name=="humidity") {
				isChange = true //isTemperatureStateChange(device, name, value.toString())
                isDisplayed = isChange
				
				sendEvent(
					name: name,
					value: value,
					linkText: linkText,
					descriptionText: getThermostatDescriptionText(name, value, linkText),
					handlerName: name,
					isStateChange: isChange,
					displayed: isDisplayed)                    
            }
            else {
            	isChange = true//isStateChange(device, name, value.toString())
                isDisplayed = false//isChange
                
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
        
        
		//generateSetpointEvent ()  
        generateStatusEvent ()
	}
}

private getThermostatDescriptionText(name, value, linkText)
{
	if(name == "temperature")
	{
		return "$name was $value " + device.currentState("temperatureUnit").value
	}
    else if(name == "humidity")
	{
		return "$name was $value %"
    }
    else if(name == "targetTemperature")
	{
		return "latest temperature setpoint was $value " + device.currentState("temperatureUnit").value
	}
	else if(name == "fanLevel")
	{
		return "latest fan level was $value"
	}
	else if(name == "on")
	{
		return "latest switch was $value"
	}
    else if (name == "mode")
    {
        return "thermostat mode was ${value}"
    }
    else if (name == "thermostatMode")
    {
        return "thermostat mode was ${value}"
    }
    else if (name == "Error")
    {
    	def str = (value == "Failed") ? "failed" : "success"
        return "Last setACState was ${str}"
    }
    else if (name == "battvoltage" || name== "battery")
    {
    	return "Battery voltage was ${value}"
    }
    else
    {
        return "${name} = ${value}"
    }
}

// parse events into attributes
def parse(String description) {
	log.debug "Parsing '${description}'"
    
    def name = null
	def value = null
    def statusTextmsg = ""   
    def msg = parseLanMessage(description)

    def headersAsString = msg.header // => headers as a string
    def headerMap = msg.headers      // => headers as a Map
    def body = msg.body              // => request body as a string
    def status = msg.status          // => http status code of the response
    def json = msg.json              // => any JSON included in response body, as a data structure of lists and maps
    def xml = msg.xml                // => any XML included in response body, as a document tree structure
    def data = msg.data              // => either JSON or XML in response body (whichever is specified by content-type header in response)

	if (description?.startsWith("on/off:")) {
		log.debug "Switch command"
		name = "switch"
		value = description?.endsWith(" 1") ? "on" : "off"
	}
   else if (description?.startsWith("temperature")) {
    	log.debug "Temperature"
        name = "temperature"
		value = device.currentValue("temperature")
    }
    else if (description?.startsWith("humidity")) {
    	log.debug "Humidity"
        name = "humidity"
		value = device.currentValue("humidity")
    }
    else if (description?.startsWith("targetTemperature")) {
    	log.debug "targetTemperature"
        name = "targetTemperature"
		value = device.currentValue("targetTemperature")
    }
    else if (description?.startsWith("fanLevel")) {
    	log.debug "fanLevel"
        name = "fanLevel"
		value = device.currentValue("fanLevel")
    }
    else if (description?.startsWith("mode")) {
    	log.debug "mode"
        name = "mode"
		value = device.currentValue("mode")
    }
    else if (description?.startsWith("on")) {
    	log.debug "on"
        name = "on"
        value = device.currentValue("on")
    }
    else if (description?.startsWith("switch")) {
    	log.debug "switch"
        name = "switch"
        value = device.currentValue("on")
    }
    else if (description?.startsWith("temperatureUnit")) {
    	log.debug "temperatureUnit"
        name = "temperatureUnit"
        value = device.currentValue("temperatureUnit")
    }
    else if (description?.startsWith("Error")) {
    	log.debug "Error"
        name = "Error"
        value = device.currentValue("Error")
    }
    else if (description?.startsWith("battvoltage")) {
    	log.debug "battvoltage"
        name = "battvoltage"
		value = device.currentValue("battvoltage")
    }
    else if (description?.startsWith("battery")) {
    	log.debug "battery"
        name = "battery"
		value = device.currentValue("battery")
    }
	
    def result = createEvent(name: name, value: value)
	log.debug "Parse returned ${result?.descriptionText}"
	return result
}

def generateStatusEvent() {
    //log.debug device
    def temperature = device.currentValue("temperature").toDouble()  
    def humidity = device.currentValue("humidity").toDouble() 
    def targetTemperature = device.currentValue("targetTemperature").split(' ')[0].toDouble()
    def fanLevel = device.currentValue("fanLevel")
    def mode = device.currentValue("mode")
    def on = device.currentValue("on")
	def error = device.currentValue("Error")
                    
    def statusTextmsg = ""
    def sUnit = device.currentValue("temperatureUnit")

    statusTextmsg = "${humidity}%"
   
    sendEvent("name":"statusText", "value":statusTextmsg, "description":"", displayed: false, isStateChange: true)
}

def cToF(temp) {
	return (temp * 1.8 + 32).toDouble()
}

def fToC(temp) {
	return ((temp - 32) / 1.8).toDouble()
}