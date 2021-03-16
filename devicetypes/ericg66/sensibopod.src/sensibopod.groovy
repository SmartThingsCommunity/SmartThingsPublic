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
*
*
*
*/

import groovyx.net.http.HTTPBuilder
import groovyx.net.http.ContentType
import groovyx.net.http.Method
import groovyx.net.http.RESTClient
import groovy.json.*

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
        capability "Actuator"
        capability "Sensor"
        capability "Health Check"
        capability "Power Source"
        capability "Voltage Measurement"
        capability "Air Conditioner Mode"
        
        attribute "swing", "String"
        attribute "temperatureUnit","String"
        attribute "productModel","String"
        attribute "firmwareVersion","String"
        attribute "Climate","String"
        
        command "setAll"
        command "switchFanLevel"
        command "switchMode"
        command "raiseCoolSetpoint"
        command "lowerCoolSetpoint"
        command "raiseHeatSetpoint"
        command "lowerHeatSetpoint" 
        command "voltage"
        command "raiseTemperature"
        command "lowerTemperature"
        command "switchSwing"
        command "setThermostatMode"
        command "modeHeat"
        command "modeCool"
        command "modeDry"
        command "modeFan"
        command "modeAuto"
        command "lowfan"
        command "mediumfan"
        command "highfan"
        command "quietfan"
        command "strongfan"
        command "autofan"
        command "fullswing"
        command "setAirConditionerMode"
        command "toggleClimateReact"
        command "setClimateReact"
        command "configureClimateReact"
	}

	simulator {

	}

	tiles(scale: 2) {
    	multiAttributeTile(name:"thermostatMulti", type:"thermostat",, width:6, height:4,canChangeIcon: false) {
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
                attributeState("VALUE_UP", action: "raiseTemperature")
                attributeState("VALUE_DOWN", action: "lowerTemperature")    			
  			}
            tileAttribute("device.humidity", key: "SECONDARY_CONTROL") {
   				attributeState("default", label:'${currentValue}%', unit:"%")
  			}
            tileAttribute("device.thermostatOperatingState", key: "OPERATING_STATE") {
    			attributeState("idle", backgroundColor:"#a9a9a9")
    			attributeState("heating", backgroundColor:"#e86d13")
    			attributeState("cooling", backgroundColor:"#00a0dc")
                attributeState("fan only", backgroundColor:"#44b621")
                attributeState("Dry", backgroundColor:"#A1E5E5")
  			}
  			tileAttribute("device.thermostatMode", key: "THERMOSTAT_MODE") {
    			attributeState("off", label:'${name}')
                attributeState("heat", label:'${name}')
                attributeState("cool", label:'${name}')    			
                attributeState("fan", label:'${name}')
                attributeState("dry", label:'${name}')
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
			state("temperature", label:'Temp: ${currentValue}',backgroundColors:[])
		}
        
		valueTile("humidity", "device.humidity", width: 2, height: 2) {
			state("humidity", label:'Humidity: ${currentValue} %',
				backgroundColors:[
				]
			)
		}
           
        valueTile("voltage", "device.voltage", width: 1, height: 1) {
			state("voltage", label:'${currentValue}',
				backgroundColors:[
					[value: 2700, color: "#CC0000"],
					[value: 2800, color: "#FFFF00"],
					[value: 2900, color: "#00FF00"]
                ]
           )
        }
        
        valueTile("firmwareVersion", "device.firmwareVersion", width: 1, height: 1) {
			state("version", label:'Firmware: ${currentValue}',backgroundColors:[])
		}
        
        valueTile("productModel", "device.productModel", width: 1, height: 1) {
			state("mains", label:'Model: ${currentValue}',backgroundColors:[])
		}
        
        valueTile("powerSource", "device.powerSource", width: 1, height: 1) {
			state("mains", icon:"https://image.ibb.co/inKcN5/cable_power_cord_plug_circle_512.png", label:'Source: ${currentValue}',backgroundColors:[])
            state("battery", icon:"https://image.ibb.co/gFUNpk/battery.jpg", label:'Source: ${currentValue}',backgroundColors:[])
		}
        
        standardTile("switch", "device.switch", width: 2, height: 2, canChangeIcon: true) {
			state "on", label:'${name}', action:"switch.off", icon:"st.switches.switch.on", backgroundColor:"#00a0dc"
			state "off", label:'${name}', action:"switch.on", icon:"st.switches.switch.off", backgroundColor:"#ffffff"
		}
    
        standardTile("fanLevel", "device.fanLevel", width: 2, height: 2) {
            state "low", action:"switchFanLevel", backgroundColor:"#8C8C8D", icon:"https://image.ibb.co/gZxHpk/fan_low_2.png", nextState:"medium"
            state "medium", action:"switchFanLevel", backgroundColor:"#8C8C8D", icon:"https://image.ibb.co/cUa3Uk/fan_medium_2.png", nextState:"high"
            state "high", action:"switchFanLevel", backgroundColor:"#8C8C8D", icon:"https://image.ibb.co/fcfFaQ/fan_high_2.png", nextState:"auto"
            state "auto", action:"switchFanLevel", backgroundColor:"#8C8C8D", icon:"https://image.ibb.co/m8oq9k/fan_auto_2.png" , nextState:"quiet"
            state "quiet", action:"switchFanLevel", backgroundColor:"#8C8C8D", icon:"https://image.ibb.co/mC74wb/fan_quiet2.png" , nextState:"medium_high"
            state "medium_high", action:"switchFanLevel", backgroundColor:"#8C8C8D", icon:"https://image.ibb.co/j62DXR/fan_medium_3.png" , nextState:"medium_low"
            state "medium_low", action:"switchFanLevel", backgroundColor:"#8C8C8D", icon:"https://image.ibb.co/etX296/fan_low_3.png" , nextState:"strong"
            state "strong", action:"switchFanLevel", backgroundColor:"#8C8C8D", icon:"https://image.ibb.co/eaLeUw/fan_strong2.png" , nextState:"low"
        }
        
        standardTile("currentmode", "device.thermostatMode",  width: 2, height: 2) {
            state "heat", action:"switchMode", backgroundColor:"#e86d13", icon:"https://image.ibb.co/c7Grh5/sun.png", nextState:"cool"
            state "cool", action:"switchMode", backgroundColor:"#00a0dc", icon:"https://image.ibb.co/bZ56FQ/cold.png", nextState:"fan"
            state "fan", action:"switchMode", backgroundColor:"#e8e3d8", icon:"https://image.ibb.co/n1dhpk/status_message_fan.png", nextState:"dry"
            state "dry", action:"switchMode", backgroundColor:"#e8e3d8", icon:"https://image.ibb.co/k2ZNpk/dry_mode.png", nextState:"auto"
            state "auto", action:"switchMode", backgroundColor:"#e8e3d8", icon:"https://image.ibb.co/dwaRh5/auto_mode.png", nextState:"heat"               
        }
        
        standardTile("upCoolButtonControl", "device.targetTemperature", inactiveLabel: false, decoration: "flat", width: 1, height: 2) {
			state "setpoint", action:"raiseCoolSetpoint", icon:"st.thermostat.thermostat-up",label :"Up"
		}
        
        standardTile("downCoolButtonControl", "device.targetTemperature", inactiveLabel: false, decoration: "flat", width: 1, height: 2) {
			state "setpoint", action:"lowerCoolSetpoint", icon:"st.thermostat.thermostat-down", label :"Down"
		}
               
        standardTile("swing", "device.swing",  width: 2, height: 2) {
            state "stopped", action:"switchSwing", backgroundColor:"#8C8C8D", icon:"https://image.ibb.co/iWhvaQ/stopped.png", nextState:"fixedTop"
            state "fixedTop", action:"switchSwing", backgroundColor:"#8C8C8D", icon:"https://image.ibb.co/nbV3Uk/fixedTop.png", nextState:"fixedMiddleTop"
            state "fixedMiddleTop", action:"switchSwing", backgroundColor:"#8C8C8D", icon:"https://image.ibb.co/chbcpk/fixed_Middle_Top.png", nextState:"fixedMiddle"
            state "fixedMiddle", action:"switchSwing", backgroundColor:"#8C8C8D", icon:"https://image.ibb.co/dxDe25/fixed_Middle.png", nextState:"fixedMiddleBottom"
            state "fixedMiddleBottom", action:"switchSwing", backgroundColor:"#8C8C8D", icon:"https://image.ibb.co/ebZmh5/fixed_Middle_Bottom.png", nextState:"fixedBottom"
            state "fixedBottom", action:"switchSwing", backgroundColor:"#8C8C8D", icon:"https://image.ibb.co/n2tCN5/fixed_Bottom.png", nextState:"rangeTop"
            state "rangeTop", action:"switchSwing", backgroundColor:"#8C8C8D", icon:"https://image.ibb.co/gYQsN5/rangeTop.png", nextState:"rangeMiddle"
            state "rangeMiddle", action:"switchSwing", backgroundColor:"#8C8C8D", icon:"https://image.ibb.co/dC1XN5/range_Middle.png", nextState:"rangeBottom"
            state "rangeBottom", action:"switchSwing", backgroundColor:"#8C8C8D", icon:"https://image.ibb.co/csTOUk/range_Bottom.png", nextState:"rangeFull"
            state "rangeFull", action:"switchSwing", backgroundColor:"#8C8C8D", icon:"https://image.ibb.co/hHK8vQ/range_Full.png", nextState:"horizontal"
            state "horizontal", action:"switchSwing", backgroundColor:"#8C8C8D", icon:"https://image.ibb.co/cAGQ2G/range_Horizontal2.png", nextState:"both"
            state "both", action:"switchSwing", backgroundColor:"#8C8C8D", icon:"https://image.ibb.co/dLUOpw/range_Both2.png", nextState:"stopped"
        }
        
        standardTile("Climate", "device.Climate", width: 2, height: 2) {
			state "on", label:'${name}', action:"toggleClimateReact", icon:"https://i.ibb.co/Z8ZzcHR/auto-fix-2.png", backgroundColor:"#00a0dc"
			state "off", label:'${name}', action:"toggleClimateReact", icon:"https://i.ibb.co/Z8ZzcHR/auto-fix-2.png", backgroundColor:"#ffffff"
            state "notdefined", label:'N/A', action:"toggleClimateReact", icon:"https://i.ibb.co/Z8ZzcHR/auto-fix-2.png", backgroundColor:"#e86d13"
		}
        
        standardTile("refresh", "device.switch", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "default", label:"Refresh", action:"refresh.refresh", icon:"st.secondary.refresh"
		}
		
        standardTile("coolmode", "device.thermostatMode",  width: 1, height: 1) {
       		state "cool", action:"modeCool", backgroundColor:"#00a0dc", icon:"https://image.ibb.co/bZ56FQ/cold.png"
        }
        standardTile("heatmode", "device.thermostatMode",  width: 1, height: 1) {
       		state "heat", action:"modeHeat", backgroundColor:"#e86d13", icon:"https://image.ibb.co/c7Grh5/sun.png"
        }
        standardTile("drymode", "device.thermostatMode",  width: 1, height: 1) {
       		state "dry", action:"modeDry", backgroundColor:"#e8e3d8", icon:"https://image.ibb.co/k2ZNpk/dry_mode.png"
        }
        standardTile("fanmode", "device.thermostatMode",  width: 1, height: 1) {
       		state "fan", action:"modeFan", backgroundColor:"#e8e3d8", icon:"https://image.ibb.co/n1dhpk/status_message_fan.png"
        }        
        standardTile("highfan", "device.fanLevel",  width: 1, height: 1) {
       		state "high", action:"highfan", backgroundColor:"#8C8C8D", icon:"https://image.ibb.co/fcfFaQ/fan_high_2.png"
        }
        standardTile("autofan", "device.fanLevel",  width: 1, height: 1) {
       		state "auto", action:"autofan", backgroundColor:"#8C8C8D", icon:"https://image.ibb.co/m8oq9k/fan_auto_2.png"
        }
        standardTile("fullswing", "device.swing",  width: 1, height: 1) {
       		state "rangeFull", action:"fullswing", backgroundColor:"#8C8C8D", icon:"https://image.ibb.co/hHK8vQ/range_Full.png"
        }
        
		main (["switch"])
		details (["thermostatMulti","switch","fanLevel","currentmode","swing","Climate","refresh","coolmode","heatmode","fanmode","drymode","highfan","autofan","fullswing","firmwareVersion","productModel","powerSource","voltage"])    
	}
}

def setAll(newMode,temp,fan)
{
	log.trace "setAll() called with " + newMode + "," + temp + "," + fan
    
    def Setpoint = temp.toInteger()
    
    def LevelBefore = fan
    def capabilities = parent.getCapabilities(device.deviceNetworkId,newMode)
    def Level = LevelBefore
    if (capabilities.remoteCapabilities != null) {
    	def fanLevels = capabilities.remoteCapabilities.fanLevels
        
    	log.debug "Fan levels capabilities : " + capabilities.remoteCapabilities.fanLevels
        
        Level = GetNextFanLevel(LevelBefore,capabilities.remoteCapabilities.fanLevels)
        log.debug "Fan : " + Level
        
        def result = parent.setACStates(this, device.deviceNetworkId, "on", newMode, Setpoint, Level, device.currentState("swing").value, device.currentState("temperatureUnit").value)
       
        if (Level == null) {
        	Level = LevelBefore
		}
        
        if (result) {
        	if (LevelBefore != Level) {
                generatefanLevelEvent(Level)
            }
            sendEvent(name: 'thermostatMode', value: newMode, displayed: false,isStateChange: true)
            if (device.currentState("on").value == "off") { generateSwitchEvent("on") }

            generateModeEvent(newMode)
            
            generateStatusEvent()
            refresh()
        }
        else {
            generateErrorEvent()
            
            generateStatusEvent()
        }              
	}
    else {       
    }
}

def lowfan()
{
	log.trace "lowfan() called"
	dfanLevel("low")
}

def mediumfan()
{
	log.trace "mediumfan() called"
	dfanLevel("medium")
}

def highfan()
{
	log.trace "highfan() called"
	dfanLevel("high")
}

def quietfan()
{
	log.trace "quietfan() called"
	dfanLevel("quiet")
}

def strongfan()
{
	log.trace "strongfan() called"
	dfanLevel("strong")
}

def autofan()
{
	log.trace "autofan() called"
	dfanLevel("auto")
}

def fullswing()
{	
	log.trace "fullswing() called"
	modeSwing("rangeFull")
}

def temperatureDown(temp)
{
	log.trace "temperatureDown() called with "+ temp
    
	def sunit = device.currentValue("temperatureUnit")
    def capabilities = parent.getCapabilities(device.deviceNetworkId, device.currentState("currentmode").value)
    def values
       
    if (sunit == "F") {
    	if (capabilities.remoteCapabilities.temperatures.F == null) {
       		return -1
    	}
    	values = capabilities.remoteCapabilities.temperatures.F.values                
    }
    else {
    	if (capabilities.remoteCapabilities.temperatures.C == null) {
       		return -1
    	}
    	values = capabilities.remoteCapabilities.temperatures.C.values
    }
    
    def found = values.findAll{number -> number < temp}
       
	log.debug "Values retrieved : " + found
    
    if (found == null || found.empty) found = values.first()
    else found = found.last()
        
    log.debug "Temp before : " + temp               
    log.debug "Temp after : " + found
        
    temp = found
        
    return temp
}

def temperatureUp(temp)
{
	log.trace "temperatureUp() called with "+ temp
    
	def sunit = device.currentValue("temperatureUnit")
    def capabilities = parent.getCapabilities(device.deviceNetworkId, device.currentState("currentmode").value)
    def values
    
    if (sunit == "F") {
    	if (capabilities.remoteCapabilities.temperatures.F == null) {
       		return -1
    	}
    	values = capabilities.remoteCapabilities.temperatures.F.values                
    }
    else {
    	if (capabilities.remoteCapabilities.temperatures.C == null) {
       		return -1
    	}
    	values = capabilities.remoteCapabilities.temperatures.C.values
    }
    
    def found = values.findAll{number -> number > temp}

    log.debug "Values retrieved : " + found
    if (found == null || found.empty) found = values.last()
    else found = found.first()

    log.debug "Temp before : " + temp               
    log.debug "Temp after : " + found

    temp = found
        
    return temp
}

void raiseTemperature() {
	log.trace "raiseTemperature() called"	

	def operMode = device.currentState("currentmode").value
    
    def Setpoint = device.currentValue("targetTemperature").toInteger()
    def theTemp = device.currentValue("temperatureUnit")
    
    log.debug "Current target temperature = ${Setpoint}"

	Setpoint = temperatureUp(Setpoint)
    
    if (Setpoint == -1) { 
      return
    }
    
    switch (operMode) {
    	case "heat":
        	setHeatingSetpoint(Setpoint)
            break;
        case "cool":
        	setCoolingSetpoint(Setpoint)
            break;
        case "fan":
        	setFanSetpoint(Setpoint)
        	break;
         case "dry":
            setDrySetpoint(Setpoint)
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
	log.trace "lowerTemperature() called"
    
	def operMode = device.currentState("currentmode").value
    
    def Setpoint = device.currentValue("targetTemperature").toInteger()
    def theTemp = device.currentValue("temperatureUnit")
    
    log.debug "Current target temperature = ${Setpoint}"

	Setpoint = temperatureDown(Setpoint)
    
    if (Setpoint == -1) { 
      return
    }
    
    switch (operMode) {
    	case "heat":
        	setHeatingSetpoint(Setpoint)
            break;
        case "cool":
        	setCoolingSetpoint(Setpoint)
            break;
        case "fan":
            setFanSetpoint(Setpoint)
        	break;
         case "dry":
            setDrySetpoint(Setpoint)
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
   	log.trace "lowerCoolSetpoint() called"
    
    def Setpoint = device.currentValue("targetTemperature").toInteger()
   	def theTemp = device.currentValue("temperatureUnit")

    log.debug "Current target temperature = ${Setpoint}"
	
    Setpoint = temperatureDown(Setpoint)

    def result = parent.setACStates(this, device.deviceNetworkId , "on", device.currentState("currentmode").value, Setpoint, device.currentState("fanLevel").value, device.currentState("swing").value, device.currentState("temperatureUnit").value)

    if (result) {
    	log.info "Cooling temperature changed to " + Setpoint + " for " + device.deviceNetworkId
        
        if (device.currentState("on").value == "off") { generateSwitchEvent("on") }
        
        sendEvent(name: 'coolingSetpoint', value: Setpoint,  displayed: false)
        sendEvent(name: 'thermostatSetpoint', value: Setpoint,  displayed: false)
       
        generateSetTempEvent(Setpoint)
        
    	log.debug "New target Temperature = ${Setpoint}"
        
        generateStatusEvent()
    	refresh()
    }
	else {
    	log.debug "error"
       	generateErrorEvent()
        
        generateStatusEvent()
    }	
}


void setThermostatMode(modes)
{ 
	log.trace "setThermostatMode() called"
    
  	def currentMode = device.currentState("currentmode").value
  
  	log.debug "switching AC mode from current mode: $currentMode"

  	switch (modes) {
		case "cool":
			modeCool()
			break
		//case "fan":
		//	returnCommand = modeFan()
		//	break		
		//case "dry":
		//	returnCommand = modeDry()
		//	break
        case "auto":
	        modeAuto()
			break
        case "heat":
			modeHeat()
			break
        case "off":
            off()
            break
	}
}

void setAirConditionerMode(modes)
{ 
	log.trace "setAirConditionerMode() called"
    
  	def currentMode = device.currentState("currentmode").value
  
  	log.debug "switching AC mode from current mode: $currentMode"

  	switch (modes) {
		case "cool":
			modeCool()
			break
		case "fanOnly":
        case "fan":
			modeFan()
			break		
		case "dry":
			modeDry()
			break
        case "auto":
	        modeAuto()
			break
        case "heat":
			modeHeat()
			break
	}
}

void raiseCoolSetpoint() {
   	log.trace "raiseCoolSetpoint() called"
    
    def Setpoint = device.currentValue("targetTemperature").toInteger()
    def theTemp = device.currentValue("temperatureUnit")
    
    log.debug "Current target temperature = ${Setpoint}"

	Setpoint = temperatureUp(Setpoint)

    def result = parent.setACStates(this, device.deviceNetworkId , "on", device.currentState("currentmode").value, Setpoint, device.currentState("fanLevel").value, device.currentState("swing").value, device.currentState("temperatureUnit").value)
    if (result) {
    	log.info "Cooling temperature changed to " + Setpoint + " for " + device.deviceNetworkId
        
        if (device.currentState("on").value == "off") { generateSwitchEvent("on") }
        
        sendEvent(name: 'coolingSetpoint', value: Setpoint, displayed: false)
        sendEvent(name: 'thermostatSetpoint', value: Setpoint, displayed: false)
        
        generateSetTempEvent(Setpoint)
        
    	log.debug "New target Temperature = ${Setpoint}"
        
        generateStatusEvent()
    	refresh()
    }
	else {
       	generateErrorEvent()
        
        generateStatusEvent()
    }	
}

void raiseHeatSetpoint() {
	log.trace "raiseHeatSetpoint() called"
    
    def Setpoint = device.currentValue("targetTemperature").toInteger()
    def theTemp = device.currentValue("temperatureUnit")
    
    log.debug "Current target temperature = ${Setpoint}"

	Setpoint = temperatureUp(Setpoint)

    def result = parent.setACStates(this, device.deviceNetworkId , "on", device.currentState("currentmode").value, Setpoint, device.currentState("fanLevel").value, device.currentState("swing").value, device.currentState("temperatureUnit").value)
    if (result) {
    	log.info "Heating temperature changed to " + Setpoint + " for " + device.deviceNetworkId
        
        if (device.currentState("on").value == "off") { generateSwitchEvent("on") }
        
        sendEvent(name: 'heatingSetpoint', value: Setpoint, displayed: false)
        sendEvent(name: 'thermostatSetpoint', value: Setpoint, displayed: false)
        
        generateSetTempEvent(Setpoint)
        
    	log.debug "New target Temperature = ${Setpoint}"
        
        generateStatusEvent()
    	refresh()
    }
	else {
       	generateErrorEvent()
        
        generateStatusEvent()
    }
	
}

void lowerHeatSetpoint() {
	log.trace "lowerHeatSetpoint() called"
    
    def Setpoint = device.currentValue("targetTemperature").toInteger()
    def theTemp = device.currentValue("temperatureUnit")
    
    log.debug "Current target temperature = ${Setpoint}"

	Setpoint = temperatureDown(Setpoint)

    def result = parent.setACStates(this, device.deviceNetworkId , "on", device.currentState("currentmode").value, Setpoint, device.currentState("fanLevel").value, device.currentState("swing").value, device.currentState("temperatureUnit").value)
    if (result) {
    	log.info "Heating temperature changed to " + Setpoint + " for " + device.deviceNetworkId
        
        if (device.currentState("on").value == "off") { generateSwitchEvent("on") }
        
        sendEvent(name: 'heatingSetpoint', value: Setpoint, displayed: false)    
        sendEvent(name: 'thermostatSetpoint', value: Setpoint, displayed: false)
        
        generateSetTempEvent(Setpoint)
        
    	log.debug "New target Temperature = ${Setpoint}"
        
        generateStatusEvent()
    	refresh()
    }
	else {
       	generateErrorEvent()
        
        generateStatusEvent()
    }	
}

def refresh()
{
  log.trace "refresh() called"
  poll()
   
  log.trace "refresh() ended"
}

// Set Temperature
def setFanSetpoint(temp) {
	log.trace "setFanSetpoint() called"
    
	temp = temp.toInteger()
	log.debug "setTemperature : " + temp   
    
    def result = parent.setACStates(this, device.deviceNetworkId , "on", "fan", temp, device.currentState("fanLevel").value, device.currentState("swing").value, device.currentState("temperatureUnit").value)
    
    if (result) {
    	log.info "Fan temperature changed to " + temp + " for " + device.deviceNetworkId
        
    	if (device.currentState("on").value == "off") { generateSwitchEvent("on") }
  		generateModeEvent("fan")
         
        sendEvent(name: 'thermostatSetpoint', value: temp, displayed: false)
    	generateSetTempEvent(temp)
        
        generateStatusEvent()
    	refresh()
    }
    else {
       	generateErrorEvent()
        
        generateStatusEvent()
    }
}

// Set Temperature
def setDrySetpoint(temp) {
	log.trace "setDrySetpoint() called"
    
	temp = temp.toInteger()
	log.debug "setTemperature : " + temp   
    
    def result = parent.setACStates(this, device.deviceNetworkId , "on", "dry", temp, device.currentState("fanLevel").value, device.currentState("swing").value, device.currentState("temperatureUnit").value)
    
    if (result) {
    	log.info "Dry temperature changed to " + temp + " for " + device.deviceNetworkId
        
    	if (device.currentState("on").value == "off") { generateSwitchEvent("on") }
  		generateModeEvent("dry")
         
        sendEvent(name: 'thermostatSetpoint', value: temp, displayed: false)
    	generateSetTempEvent(temp)
        
        generateStatusEvent()
    	refresh()
    }
    else {
       	generateErrorEvent()
        
        generateStatusEvent()
    }   
}


// Set Temperature
def setCoolingSetpoint(temp) {
	log.trace "setCoolingSetpoint() called"

	temp = temp.toInteger()
	log.debug "setTemperature : " + temp   
    
    def result = parent.setACStates(this, device.deviceNetworkId , "on", "cool", temp, device.currentState("fanLevel").value, device.currentState("swing").value, device.currentState("temperatureUnit").value)
    
    if (result) {
    	log.info "Cooling temperature changed to " + temp + " for " + device.deviceNetworkId
        
    	if (device.currentState("on").value == "off") { generateSwitchEvent("on") }
  		generateModeEvent("cool")
         
    	sendEvent(name: 'coolingSetpoint', value: temp, displayed: false)
        sendEvent(name: 'thermostatSetpoint', value: temp, displayed: false)
    	//sendEvent(name: 'heatingSetpoint', value: temp, displayed: false)
    	generateSetTempEvent(temp)
        
        generateStatusEvent()
    	refresh()
    }
    else {
       	generateErrorEvent()
        
        generateStatusEvent()
    }
}

def setHeatingSetpoint(temp) {
	log.trace "setHeatingSetpoint() called"

	temp = temp.toInteger()
	log.debug "setTemperature : " + temp
    
    def result = parent.setACStates(this, device.deviceNetworkId , "on", "heat", temp, device.currentState("fanLevel").value, device.currentState("swing").value, device.currentState("temperatureUnit").value)
    if (result) {
    	log.info "Heating temperature changed to " + temp + " for " + device.deviceNetworkId
        
        if (device.currentState("on").value == "off") { generateSwitchEvent("on") }
    	generateModeEvent("heat")
    	//sendEvent(name: 'coolingSetpoint', value: temp, displayed: false)
    	sendEvent(name: 'heatingSetpoint', value: temp, displayed: false)
        sendEvent(name: 'thermostatSetpoint', value: temp, displayed: false)
    	generateSetTempEvent(temp)
        
        generateStatusEvent()
    	refresh()
	}	
    else {
       	generateErrorEvent()
        
        generateStatusEvent()
    }    
}

def generateSetTempEvent(temp) {
   sendEvent(name: "targetTemperature", value: temp, descriptionText: "$device.displayName set temperature is now ${temp}", displayed: true, isStateChange: true)
}

// Turn off or Turn on the AC
def on() {
	log.trace "on called"
    
    def Setpoint = device.currentValue("targetTemperature").toInteger()
   
    log.debug "Temp Unit : " + device.currentState("temperatureUnit").value
    def result = parent.setACStates(this, device.deviceNetworkId, "on", device.currentState("currentmode").value, Setpoint, device.currentState("fanLevel").value, device.currentState("swing").value, device.currentState("temperatureUnit").value)
    log.debug "Result : " + result
    if (result) {
    	log.info "AC turned ON for " + device.deviceNetworkId
    	sendEvent(name: 'thermostatMode', value: device.currentState("currentmode").value, displayed: false,isStateChange: true)
        //sendEvent(name: 'thermostatOperatingState', value: "idle",isStateChange: true)
    	
        generateSwitchEvent("on")
        
        generateStatusEvent() 
    	refresh()
    }
    else {
       	generateErrorEvent()
        
        generateStatusEvent() 
    }        
}

def off() {
	log.trace "off called"
    
    def Setpoint = device.currentValue("targetTemperature").toInteger()

    log.debug "Temp Unit : " + device.currentState("temperatureUnit").value
    def result = parent.setACStates(this, device.deviceNetworkId, "off", device.currentState("currentmode").value, Setpoint, device.currentState("fanLevel").value, device.currentState("swing").value, device.currentState("temperatureUnit").value)

    if (result) {
    	log.info "AC turned OFF for " + device.deviceNetworkId
        
    	sendEvent(name: 'thermostatMode', value: "off", displayed: false,isStateChange: true)
        //sendEvent(name: 'thermostatOperatingState', value: "idle",isStateChange: true)
    	
        generateSwitchEvent("off")
        
        generateStatusEvent()
    	refresh()
     }
    else {
       	generateErrorEvent()
        
        generateStatusEvent()
    }         
}

def generateSwitchEvent(mode) {
   sendEvent(name: "on", value: mode, descriptionText: "$device.displayName is now ${mode}", displayed: false, isStateChange: true)  
   sendEvent(name: "switch", value: mode, descriptionText: "$device.displayName is now ${mode}", displayed: true, isStateChange: true)   
   
   //refresh()
}

def dfanLevel(String newLevel){
	log.trace "dfanLevel called with fan = " + newLevel
    
    def Setpoint = device.currentValue("targetTemperature").toInteger()
    
    def capabilities = parent.getCapabilities(device.deviceNetworkId, device.currentState("currentmode").value)      
    def Level = LevelBefore
    if (capabilities.remoteCapabilities != null) {
    	def fanLevels = capabilities.remoteCapabilities.fanLevels
        
    	log.debug "Fan levels capabilities : " + capabilities.remoteCapabilities.fanLevels
        
        Level = GetNextFanLevel(newLevel,capabilities.remoteCapabilities.fanLevels)
        //log.debug "Fan : " + Level
        if (Level == null) {
          generateStatusEvent
          return
        }
        def result = parent.setACStates(this, device.deviceNetworkId,"on", device.currentState("currentmode").value, Setpoint, Level, device.currentState("swing").value, device.currentState("temperatureUnit").value)

        if (result) {
        	log.info "Fan level changed to " + Level + " for " + device.deviceNetworkId
            
            if (device.currentState("on").value == "off") { generateSwitchEvent("on") }
            if (Level == "low") {
            	sendEvent(name: 'thermostatFanMode', value: "circulate", displayed: false)
            }
            else {            	
                sendEvent(name: 'thermostatFanMode', value: "on", displayed: false)
            }
            generatefanLevelEvent(Level)
            
            generateStatusEvent()
        	refresh()
        }
        else {
            generateErrorEvent()
            
            generateStatusEvent()
        }             
	}
    else {
    	//TODO when the mode do not exist
    }    
}

def generatefanLevelEvent(mode) {
   sendEvent(name: "fanLevel", value: mode, descriptionText: "$device.displayName fan level is now ${mode}", displayed: true, isStateChange: true)
}

// toggle Climate React
def toggleClimateReact()
{
  	log.trace "toggleClimateReact() called"
    
	def currentClimateMode = device.currentState("Climate")?.value
    
    def returnCommand
    
    switch (currentClimateMode) {
    	case "off":
        	returnCommand = setClimateReact("on")
            break
        case "on":
        	returnCommand = setClimateReact("off")
            break            
    }
    
    if (!returnCommand) { returnCommand }
}

// Set Climate React
def setClimateReact(ClimateState) {

    ///////////////////////////////////////////////
    /// Parameter ClimateState : "on" or "off"
    ///////////////////////////////////////////////
    
	log.trace "setClimateReact() called"
    
	log.debug "Climate : " + ClimateState   
   
    def result = parent.setClimateReact(this, device.deviceNetworkId, ClimateState)
    
    if (result) {
    	log.info "Climate React changed to " + ClimateState + " for " + device.deviceNetworkId
              
        sendEvent(name: 'Climate', value: ClimateState, displayed: false)
    	//generateSetTempEvent(temp)
        
        generateStatusEvent()
    	refresh()
    }
    else {
       	generateErrorEvent()
        
        generateStatusEvent()
    }
}

def configureClimateReact(lowThres, highThres,stype,lowState,highState, on_off, ThresUnit)
{
    ///////////////////////////////////////////////
    // lowThres and highThres - Integer parameters
	// stype : possible values are "temperature", "humidity" or "feelsLike"
    // lowState and highState : 
    //    on, fanLevel, temperatureUnit, targetTemperature, mode      
    //
    //    like  "[true,'auto','C',21,'heat']"
    //    to turn off AC,first parameters = false : "[false,'auto','C',21,'heat']"
    // one_off : boolean value to enable/disable the Climate React
    // unit : Passing F for Farenheit or C for Celcius
    // 
    // Some examples: 
    //  
    // Range 19-24 Celcius, start to heat to 22 at auto fan if the temp is lower than 19 and stop the AC when higher than 24
    // configureClimateReact(19, 24, ‘temperature’, ‘[true, ‘auto’, ‘C’, 22, ‘heat’]’, ‘[false, ‘auto’, ‘C’, 22, ‘heat’]’, true, ‘C’);
    //
    // Range 67-68 Farenheit, start to heat to 68 at auto fan if the temp is lower than 67 and stop the AC when higher than 68
    // configureClimateReact(67, 68, ‘temperature’, ‘[true, ‘auto’, ‘F’, 68, ‘heat’]’, ‘[false, ‘auto’, ‘F’, 68, ‘heat’]’, true, ‘F’);
    //
    ///////////////////////////////////////////////
    
	log.trace "configureClimateReact() called"
    
    
    if (ThresUnit.toUpperCase() == "F")
    {
    	lowThres = fToc(lowThres).round(1)
    	highThres = fToc(highThres).round(1)
    }
    
    def json = new groovy.json.JsonBuilder()
    
    def lowStateMap = evaluate(lowState)
    def highStateMap = evaluate(highState)
        
    def lowStateJson
    def highStateJson
    
    if (lowStateMap) {
        lowStateJson = json {
            on lowStateMap[0]
            fanLevel lowStateMap[1]
            temperatureUnit lowStateMap[2]
            targetTemperature lowStateMap[3]
            mode lowStateMap[4]
        }
    }
    else { lowStateJson = null }
    
    if (highStateMap) {
        highStateJson = json {
            on highStateMap[0]
            fanLevel highStateMap[1]
            temperatureUnit highStateMap[2]
            targetTemperature highStateMap[3]
            mode highStateMap[4]
        }
    }
    else { highStateJson = null }
    
    def root = json {
    	deviceUid device.deviceNetworkId
        highTemperatureWebhook null
        highTemperatureThreshold highThres        
        lowTemperatureWebhook null
        type stype        
        lowTemperatureState lowStateJson
        enabled on_off
        highTemperatureState highStateJson
        lowTemperatureThreshold lowThres             
    }
    
    log.debug "CLIMATE REACT STRING : " + JsonOutput.prettyPrint(json.toString())
    def result = parent.configureClimateReact(this, device.deviceNetworkId, json.toString())
    
    if (result) {
    	log.info "Climate React settings changed for " + device.deviceNetworkId
              
        sendEvent(name: 'Climate', value: on_off, displayed: false)
        
        generateStatusEvent()
    	refresh()
    }
    else {
       	generateErrorEvent()
        
        generateStatusEvent()
    }
}

def switchFanLevel() {
	log.trace "switchFanLevel() called"
    
	def currentFanMode = device.currentState("fanLevel")?.value
	log.debug "switching fan level from current mode: $currentFanMode"
	def returnCommand

	switch (currentFanMode) {
		case "low":
			returnCommand = dfanLevel("medium")
			break
		case "medium":
			returnCommand = dfanLevel("high")
			break
		case "high":
			returnCommand = dfanLevel("auto")
			break
        case "auto":
			returnCommand = dfanLevel("quiet")
			break
        case "quiet":
			returnCommand = dfanLevel("medium_high")
			break
         case "medium_high":
			returnCommand = dfanLevel("medium_low")
			break    
         case "medium_low":
			returnCommand = dfanLevel("strong")
			break
          case "strong":
			returnCommand = dfanLevel("low")
			break
	}

	returnCommand
}

def modeHeat()
{
	log.trace "modeHeat() called"
	modeMode("heat")
}

def modeCool()
{
	log.trace "modeCool() called"
	modeMode("cool")
}

def modeDry()
{
	log.trace "modeDry() called"
	modeMode("dry")
}

def modeFan()
{
	log.trace "modeFan() called"
	modeMode("fan")
}

def modeAuto()
{
	log.trace "modeAuto() called"
	modeMode("auto")
}

// To change the AC mode

def switchMode() {
	log.trace "switchMode() called"
    
	def currentMode = device.currentState("currentmode")?.value
	log.debug "switching AC mode from current mode: $currentMode"
	def returnCommand

	switch (currentMode) {
		case "heat":
			returnCommand = modeMode("cool")
			break
		case "cool":
			returnCommand = modeMode("fan")
			break		
		case "fan":
			returnCommand = modeMode("dry")
			break
        case "dry":
            returnCommand = modeMode("auto")
			break
        case "auto":
			returnCommand = modeMode("heat")
			break
	}

	returnCommand
}

def modeMode(String newMode){
    log.trace "modeMode() called with " + newMode
    
    def Setpoint = device.currentValue("targetTemperature").toInteger()
    
    def LevelBefore = device.currentState("fanLevel").value
    def capabilities = parent.getCapabilities(device.deviceNetworkId,newMode)
    def Level = LevelBefore
    if (capabilities.remoteCapabilities != null) {
    	def fanLevels = capabilities.remoteCapabilities.fanLevels
    	
        log.debug "Fan levels capabilities : " + capabilities.remoteCapabilities.fanLevels
        
        Level = GetNextFanLevel(LevelBefore,capabilities.remoteCapabilities.fanLevels)
        
        log.debug "FanLevel : " + Level
        
        def result = parent.setACStates(this, device.deviceNetworkId, "on", newMode, Setpoint, Level, device.currentState("swing").value, device.currentState("temperatureUnit").value)
        
        if (Level == null) {
        	Level = LevelBefore
        }
        
        if (result) {
        	log.info "Mode changed to " + newMode + " for " + device.deviceNetworkId
            
        	if (LevelBefore != Level) {
                generatefanLevelEvent(Level)
            }
            sendEvent(name: 'thermostatMode', value: newMode, displayed: false,isStateChange: true)
            if (device.currentState("on").value == "off") { generateSwitchEvent("on") }

            generateModeEvent(newMode)
            
            generateStatusEvent()
        	refresh()
        }
        else {
            generateErrorEvent()
            
            generateStatusEvent()
        }             
	}
    else {
       def themodes = parent.getCapabilities(device.deviceNetworkId,"modes")
       def sMode = GetNextMode(newMode,themodes)

	   NextMode(sMode)
    }
}

def generateModeEvent(mode) {
   sendEvent(name: "thermostatMode", value: mode, descriptionText: "$device.displayName Thermostat mode is now ${mode}", displayed: true, isStateChange: true)   
}
   
def returnNext(liste1, liste2, val) throws Exception
{
    try {
    	def index = liste2.indexOf(val)
        
        if (index == -1) throw new Exception()
        else return liste2[liste2.indexOf(val)]
    }
    catch(Exception e) {
    	if (liste1.indexOf(val)+ 1 == liste1.size()) {
           val = liste1[0]
           }
         else {
           val = liste1[liste1.indexOf(val) + 1]
         }
         returnNext(liste1, liste2, val)
    }	
}

def GetNextFanLevel(fanLevel, fanLevels)
{
	log.trace "GetNextFanLevel called with " + fanLevel
    
    if (fanLevels == null || fanLevel == "null") {
      return null
    }
    
	def listFanLevel = ['low','medium','high','auto','quiet','medium_high','medium_low','strong']	
    def newFanLevel = returnNext(listFanLevel, fanLevels,fanLevel)
    
    log.debug "Next fanLevel = " + newFanLevel
	
    return newFanLevel
}

def GetNextMode(mode, modes)
{
	log.trace "GetNextMode called with " + mode
        
	def listMode = ['heat','cool','fan','dry','auto']	
    def newMode = returnNext(listMode, modes,mode)
    
    log.debug "Next Mode = " + newMode
    
	return newMode
}

def NextMode(sMode)
{
	log.trace "NextMode called()"
    
	if (sMode != null) {
    	switch (sMode)
        {
         	case "heat":
            	modeHeat()
            	break
            case "cool":
            	modeCool()
            	break
            case "fan":
            	modeFan()
            	break
            case "dry":
            	modeDry()
            	break
            case "auto":
            	modeAuto()
            	break                
        }
    }
    else 
    {
    	return null
    }
}

def GetNextSwingMode(swingMode, swingModes){
	log.trace "GetNextSwingMode() called with " + swingMode
	
    if (swingModes == null || swingMode == "null") {
    	return null
    }
    
	def listSwingMode = ['stopped','fixedTop','fixedMiddleTop','fixedMiddle','fixedMiddleBottom','fixedBottom','rangeTop','rangeMiddle','rangeBottom','rangeFull','horizontal','both']	
    def newSwingMode = returnNext(listSwingMode, swingModes,swingMode)
    
    log.debug "Next Swing Mode = " + newSwingMode
    
	return newSwingMode
}

def switchSwing() {
	log.trace "switchSwing() called"
    
	def currentMode = device.currentState("swing")?.value
	log.debug "switching Swing mode from current mode: $currentMode"
	def returnCommand
	switch (currentMode) {
		case "stopped":
			returnCommand = modeSwing("fixedTop")
			break
		case "fixedTop":
			returnCommand = modeSwing("fixedMiddleTop")
			break
        case "fixedMiddleTop":
			returnCommand = modeSwing("fixedMiddle")
			break
        case "fixedMiddle":
			returnCommand = modeSwing("fixedMiddleBottom")
			break
        case "fixedMiddleBottom":
			returnCommand = modeSwing("fixedBottom")
			break
		case "fixedBottom":
			returnCommand = modeSwing("rangeTop")
			break
        case "rangeTop":
            returnCommand = modeSwing("rangeMiddle")
			break        
        case "rangeMiddle":
			returnCommand = modeSwing("rangeBottom")
			break
         case "rangeBottom":
			returnCommand = modeSwing("rangeFull")
			break
        case "rangeFull":
			returnCommand = modeSwing("horizontal")
			break
        case "horizontal":
			returnCommand = modeSwing("both")
			break
         case "both":
			returnCommand = modeSwing("stopped")
			break
	}

	returnCommand
}
def modeSwing(String newSwing)
{
    log.trace "modeSwing() called with " + newSwing
    
    def Setpoint = device.currentValue("targetTemperature").toInteger()
   
    def SwingBefore = device.currentState("swing").value
    def capabilities = parent.getCapabilities(device.deviceNetworkId, device.currentState("currentmode").value)
    def Swing = SwingBefore
    if (capabilities.remoteCapabilities != null) {
    	def Swings = capabilities.remoteCapabilities.swing

        log.debug "Swing capabilities : " + capabilities.remoteCapabilities.swing

        Swing = GetNextSwingMode(newSwing,capabilities.remoteCapabilities.swing)
        //log.debug "Swing : " + Swing
        
        def result = parent.setACStates(this, device.deviceNetworkId, "on", device.currentState("currentmode").value, Setpoint, device.currentState("fanLevel").value, Swing, device.currentState("temperatureUnit").value)
        
        if (Swing == null) {
        	Swing = SwingBefore
        }
        
        if (result) {
        	log.info "Swing mode changed to " + Swing + " for " + device.deviceNetworkId
            
            sendEvent(name: 'swing', value: Swing, displayed: false,isStateChange: true)
            if (device.currentState("on").value == "off") { generateSwitchEvent("on") }
			sendEvent(name: 'thermostatFanMode', value: "on", displayed: false)
            generateSwingModeEvent(Swing)
            
            generateStatusEvent()
        	refresh()
        }
        else {
            generateErrorEvent()
            
            generateStatusEvent()
        }              
	}
    else
    {
      //TODO
    }
}

def generateSwingModeEvent(mode) {
   sendEvent(name: "swing", value: mode, descriptionText: "$device.displayName swing mode is now ${mode}", displayed: true, isStateChange: true)
}

def generateErrorEvent() {
   log.debug "$device.displayName FAILED to set the AC State"
   sendEvent(name: "Error", value: "Error", descriptionText: "$device.displayName FAILED to set or get the AC State", displayed: true, isStateChange: true)  
}


void poll() {
	log.trace "Executing 'poll' using parent SmartApp"
	
	def results = parent.pollChild(this)
	
    def linkText = getLinkText(device)
                    
    parseTempUnitEventData(results)
    parseEventData(results)
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
                    //unit: value,
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
	log.debug "parsing Event data $results"
    
	if(results)
	{
		results.each { name, value -> 
 
 			log.debug "name :" + name + " value :" + value
			def linkText = getLinkText(device)
            def isChange = false
            def isDisplayed = true
                             
            if (name=="voltage") {
                isChange = true //isTemperatureStateChange(device, name, value.toString())
                isDisplayed = false
                  
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
            else if (name== "battery") {            	                
                isChange = true //isTemperatureStateChange(device, name, value.toString())
                isDisplayed = false
                 
				sendEvent(
					name: name,
					value: value,
                    //unit: "V",
					linkText: linkText,
					descriptionText: getThermostatDescriptionText(name, value, linkText),
					handlerName: name,
					isStateChange: isChange,
					displayed: isDisplayed)
            	}
            else if (name== "powerSource") {            	                
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
            else if (name== "Climate") {            	                
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
               }
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
                 
				if (value=="cool") {
                	sendEvent(name: 'airConditionerMode', value: "cool", 
					isStateChange: isChange,
					displayed: isDisplayed)
					
                    sendEvent(name: 'thermostatOperatingState', value: "cooling", 
					isStateChange: isChange,
					displayed: isDisplayed)
				} else if (value=="heat") {
					sendEvent(name: 'airConditionerMode', value: "heat", 
					isStateChange: isChange,
					displayed: isDisplayed)
                    
                    sendEvent(name: 'thermostatOperatingState', value: "heating", 
					isStateChange: isChange,
					displayed: isDisplayed)
				} else if (value=="fan") {
             		sendEvent(name: 'airConditionerMode', value: "fanOnly", 
					isStateChange: isChange,
					displayed: isDisplayed)
                
					sendEvent(name: 'thermostatOperatingState', value: "fan only", 
					isStateChange: isChange,
					displayed: isDisplayed)
                } else if (value=="dry") {
                    sendEvent(name: 'airConditionerMode', value: "dry", 
					isStateChange: isChange,
					displayed: isDisplayed)
                    
					sendEvent(name: 'thermostatOperatingState', value: "dry", 
					isStateChange: isChange,
					displayed: isDisplayed)
                 } else if (value=="auto") {
                    sendEvent(name: 'airConditionerMode', value: "auto", 
					isStateChange: isChange,
					displayed: isDisplayed)
                    
					sendEvent(name: 'thermostatOperatingState', value: "auto", 
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
            else if (name=="coolingSetpoint" || name== "heatingSetpoint" || name == "thermostatSetpoint") {           	
                isChange = true //isTemperatureStateChange(device, name, value.toString())
                isDisplayed = false
                
				sendEvent(
					name: name,
					value: value,
                    //unit : device.currentValue("temperatureUnit"),
					linkText: linkText,
					descriptionText: getThermostatDescriptionText(name, value, linkText),
					handlerName: name,
					isStateChange: isChange,
					displayed: isDisplayed)
            	}
            else if (name=="temperatureUnit") { 
                isChange = true
                isDisplayed = false
                   
				sendEvent(
					name: name,
					value: value,
                    //unit: value,
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
 			else if (name=="swing") {
              	
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
                isDisplayed = false
				
				sendEvent(
					name: name,
					value: value,
					//unit: device.currentValue("temperatureUnit"),
					linkText: linkText,
					descriptionText: getThermostatDescriptionText(name, value, linkText),
					handlerName: name,
					isStateChange: isChange,
					displayed: isDisplayed)
                    
            }
            else if (name=="humidity") {
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
    else if (name == "currentmode")
    {
        return "thermostat mode was ${value}"
    }
    else if (name == "powerSource")
    {
        return "power source mode was ${value}"
    }
    else if (name == "Climate")
    {
        return "Climate React was ${value}"
    }
    else if (name == "thermostatMode")
    {
        return "thermostat mode was ${value}"
    }
    else if (name == "temperatureUnit")
    {
    	return "thermostat unit was ${value}"
    }
    else if (name == "voltage")
    {
    	return "Battery voltage was ${value}"
    }
    else if (name == "battery")
    {
    	return "Battery was ${value}"
    }
    
    else if (name == "voltage" || name== "battery")
    {
    	return "Battery voltage was ${value}"
    }
    else if (name == "swing")
    {
    	return "Swing mode was ${value}"
    }
    else if (name == "Error")
    {
    	def str = (value == "Failed") ? "failed" : "success"
        return "Last setACState was ${str}"
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
    else if (description?.startsWith("currentmode")) {
    	log.debug "mode"
        name = "currentmode"
		value = device.currentValue("currentmode")
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
    else if (description?.startsWith("voltage")) {
    	log.debug "voltage"
        name = "voltage"
		value = device.currentValue("voltage")
    }
    else if (description?.startsWith("battery")) {
    	log.debug "battery"
        name = "battery"
		value = device.currentValue("battery")
    }
    else if (description?.startsWith("swing")) {
    	log.debug "swing"
        name = "swing"
		value = device.currentValue("swing")
    }
	
    def result = createEvent(name: name, value: value)
	log.debug "Parse returned ${result?.descriptionText}"
	return result
}

def generateStatusEvent() {
    def temperature = device.currentValue("temperature").toDouble()  
    def humidity = device.currentValue("humidity").toDouble() 
    def targetTemperature = device.currentValue("targetTemperature").split(' ')[0].toDouble()
    def fanLevel = device.currentValue("fanLevel")
    def mode = device.currentValue("currentmode")
    def on = device.currentValue("on")
    def swing = device.currentValue("swing")
    def ClimateReact = device.currentValue("Climate")
    
	def error = device.currentValue("Error")
                    
    def statusTextmsg = ""
    def sUnit = device.currentValue("temperatureUnit")

    statusTextmsg = "${humidity}%"
   
    sendEvent("name":"statusText", "value":statusTextmsg, "description":"", displayed: false, isStateChange: true)
}

def fToc(temp) {
	return ((temp - 32) / 1.8).toDouble()
}
