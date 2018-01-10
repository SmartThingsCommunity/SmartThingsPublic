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
        capability "Actuator"
        capability "Sensor"
        capability "Health Check"
        capability "Power Source"
        capability "Voltage Measurement"
        
        attribute "swing", "String"
        attribute "temperatureUnit","String"
        attribute "productModel","String"
        attribute "firmwareVersion","String"
        
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
       
	}

	simulator {
		// TODO: define status and reply messages here
		
        // status messages
		status "on": "on/off: 1"
		status "off": "on/off: 0"
        status "temperature":"22"
        status "targetTemperature":"22"
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
            //tileAttribute("device.swing", key: "swing") {
    		//	attributeState("stopped", label:'${name}')
    		//	attributeState("fixedTop", label:'${name}')
            //    attributeState("fixedMiddleTop", label:'${name}')
            //    attributeState("fixedMiddle", label:'${name}')
            //    attributeState("fixedMiddleBottom", label:'${name}')
    		//	attributeState("fixedBottom", label:'${name}')
            //    attributeState("rangeTop", label:'${name}')
            //    attributeState("rangeMiddle", label:'${name}')
            //    attributeState("rangeBottom", label:'${name}')
    		//	attributeState("rangeFull", label:'${name}')
            //    attributeState("horizontal", label:'${name}')
            //    attributeState("both", label:'${name}')
  			//}
            
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
           
        valueTile("voltage", "device.voltage", width: 2, height: 2) {
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
        
        standardTile("mode", "device.thermostatMode",  width: 2, height: 2) {
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
        
        standardTile("refresh", "device.switch", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "default", label:"Refresh", action:"refresh.refresh", icon:"st.secondary.refresh"
		}
		       
		main (["switch"])
		details (["thermostatMulti","switch","fanLevel","mode","swing","voltage","refresh","powerSource","firmwareVersion","productModel"])    
	}
}

def temperatureDown(temp)
{
	def sunit = device.currentValue("temperatureUnit")
    def capabilities = parent.getCapabilities(device.deviceNetworkId, device.currentState("mode").value)
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
	def sunit = device.currentValue("temperatureUnit")
    def capabilities = parent.getCapabilities(device.deviceNetworkId, device.currentState("mode").value)
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
	def operMode = device.currentState("mode").value
    
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
	def operMode = device.currentState("mode").value
    
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
   	log.debug "Lower SetPoint"
    
    def Setpoint = device.currentValue("targetTemperature").toInteger()
   	def theTemp = device.currentValue("temperatureUnit")

    log.debug "Current target temperature = ${Setpoint}"
	
    Setpoint = temperatureDown(Setpoint)

    def result = parent.setACStates(this, device.deviceNetworkId , "on", device.currentState("mode").value, Setpoint, device.currentState("fanLevel").value, device.currentState("swing").value, device.currentState("temperatureUnit").value)

    if (result) {
        if (device.currentState("on").value == "off") { generateSwitchEvent("on") }
        
        sendEvent(name: 'coolingSetpoint', value: Setpoint,  displayed: false)
        sendEvent(name: 'thermostatSetpoint', value: Setpoint,  displayed: false)
       
        generateSetTempEvent(Setpoint)
        
    	log.debug "New target Temperature = ${Setpoint}"       
    }
	else {
    	log.debug "error"
       	generateErrorEvent()
    }
	generateStatusEvent()
    refresh()
}


void setThermostatMode(modes)
{ 
	log.debug "setThermostatMode"
  	//def currentMode = device.currentState("mode")?.value
  
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

void raiseCoolSetpoint() {
   	log.debug "Raise SetPoint"
    
    def Setpoint = device.currentValue("targetTemperature").toInteger()
    def theTemp = device.currentValue("temperatureUnit")
    
    log.debug "Current target temperature = ${Setpoint}"

	Setpoint = temperatureUp(Setpoint)

    def result = parent.setACStates(this, device.deviceNetworkId , "on", device.currentState("mode").value, Setpoint, device.currentState("fanLevel").value, device.currentState("swing").value, device.currentState("temperatureUnit").value)
    if (result) {
        if (device.currentState("on").value == "off") { generateSwitchEvent("on") }
        
        sendEvent(name: 'coolingSetpoint', value: Setpoint, displayed: false)
        sendEvent(name: 'thermostatSetpoint', value: Setpoint, displayed: false)
        
        generateSetTempEvent(Setpoint)
        
    	log.debug "New target Temperature = ${Setpoint}"       
    }
	else {
       	generateErrorEvent()
    }
	generateStatusEvent()
    refresh()
}

void raiseHeatSetpoint() {
   	log.debug "Raise SetPoint"
    
    def Setpoint = device.currentValue("targetTemperature").toInteger()
    def theTemp = device.currentValue("temperatureUnit")
    
    log.debug "Current target temperature = ${Setpoint}"

	Setpoint = temperatureUp(Setpoint)

    def result = parent.setACStates(this, device.deviceNetworkId , "on", device.currentState("mode").value, Setpoint, device.currentState("fanLevel").value, device.currentState("swing").value, device.currentState("temperatureUnit").value)
    if (result) {
        if (device.currentState("on").value == "off") { generateSwitchEvent("on") }
        
        sendEvent(name: 'heatingSetpoint', value: Setpoint, displayed: false)
        sendEvent(name: 'thermostatSetpoint', value: Setpoint, displayed: false)
        
        generateSetTempEvent(Setpoint)
        
    	log.debug "New target Temperature = ${Setpoint}"       
    }
	else {
       	generateErrorEvent()
    }
	generateStatusEvent()
    refresh()
}

void lowerHeatSetpoint() {
   	log.debug "Raise SetPoint"
    
    def Setpoint = device.currentValue("targetTemperature").toInteger()
    def theTemp = device.currentValue("temperatureUnit")
    
    log.debug "Current target temperature = ${Setpoint}"

	Setpoint = temperatureDown(Setpoint)

    def result = parent.setACStates(this, device.deviceNetworkId , "on", device.currentState("mode").value, Setpoint, device.currentState("fanLevel").value, device.currentState("swing").value, device.currentState("temperatureUnit").value)
    if (result) {
        if (device.currentState("on").value == "off") { generateSwitchEvent("on") }
        
        sendEvent(name: 'heatingSetpoint', value: Setpoint, displayed: false)    
        sendEvent(name: 'thermostatSetpoint', value: Setpoint, displayed: false)
        
        generateSetTempEvent(Setpoint)
        
    	log.debug "New target Temperature = ${Setpoint}"       
    }
	else {
       	generateErrorEvent()
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
def setFanSetpoint(temp) {
	temp = temp.toInteger()
	log.debug "setTemperature : " + temp   
    
    def result = parent.setACStates(this, device.deviceNetworkId , "on", "fan", temp, device.currentState("fanLevel").value, device.currentState("swing").value, device.currentState("temperatureUnit").value)
    
    if (result) {    
    	if (device.currentState("on").value == "off") { generateSwitchEvent("on") }
  		generateModeEvent("fan")
         
        sendEvent(name: 'thermostatSetpoint', value: temp, displayed: false)
    	generateSetTempEvent(temp)
    }
    else {
       	generateErrorEvent()
    }
    
    generateStatusEvent()
    refresh()
}

// Set Temperature
def setDrySetpoint(temp) {
	temp = temp.toInteger()
	log.debug "setTemperature : " + temp   
    
    def result = parent.setACStates(this, device.deviceNetworkId , "on", "dry", temp, device.currentState("fanLevel").value, device.currentState("swing").value, device.currentState("temperatureUnit").value)
    
    if (result) {    
    	if (device.currentState("on").value == "off") { generateSwitchEvent("on") }
  		generateModeEvent("dry")
         
        sendEvent(name: 'thermostatSetpoint', value: temp, displayed: false)
    	generateSetTempEvent(temp)
    }
    else {
       	generateErrorEvent()
    }
    
    generateStatusEvent()
    refresh()
}


// Set Temperature
def setCoolingSetpoint(temp) {
	temp = temp.toInteger()
	log.debug "setTemperature : " + temp   
    
    def result = parent.setACStates(this, device.deviceNetworkId , "on", "cool", temp, device.currentState("fanLevel").value, device.currentState("swing").value, device.currentState("temperatureUnit").value)
    
    if (result) {    
    	if (device.currentState("on").value == "off") { generateSwitchEvent("on") }
  		generateModeEvent("cool")
         
    	sendEvent(name: 'coolingSetpoint', value: temp, displayed: false)
        sendEvent(name: 'thermostatSetpoint', value: temp, displayed: false)
    	//sendEvent(name: 'heatingSetpoint', value: temp, displayed: false)
    	generateSetTempEvent(temp)
    }
    else {
       	generateErrorEvent()
    }
    
    generateStatusEvent()
    refresh()
}

def setHeatingSetpoint(temp) {
	temp = temp.toInteger()
	log.debug "setTemperature : " + temp
    
    def result = parent.setACStates(this, device.deviceNetworkId , "on", "heat", temp, device.currentState("fanLevel").value, device.currentState("swing").value, device.currentState("temperatureUnit").value)
    if (result) { 
        if (device.currentState("on").value == "off") { generateSwitchEvent("on") }
    	generateModeEvent("heat")
    	//sendEvent(name: 'coolingSetpoint', value: temp, displayed: false)
    	sendEvent(name: 'heatingSetpoint', value: temp, displayed: false)
        sendEvent(name: 'thermostatSetpoint', value: temp, displayed: false)
    	generateSetTempEvent(temp)
	}	
    else {
       	generateErrorEvent()
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
   
    log.debug "Temp Unit : " + device.currentState("temperatureUnit").value
    def result = parent.setACStates(this, device.deviceNetworkId, "on", device.currentState("mode").value, Setpoint, device.currentState("fanLevel").value, device.currentState("swing").value, device.currentState("temperatureUnit").value)
    if (result) {   
    	sendEvent(name: 'thermostatMode', value: device.currentState("mode").value, displayed: false,isStateChange: true)
        //sendEvent(name: 'thermostatOperatingState', value: "idle",isStateChange: true)
    	
        generateSwitchEvent("on")
    }
    else {
       	generateErrorEvent()
    }        
    generateStatusEvent() 
    refresh()
}

def off() {
	log.debug "off called"
    
    def Setpoint = device.currentValue("targetTemperature").toInteger()

    log.debug "Temp Unit : " + device.currentState("temperatureUnit").value
    def result = parent.setACStates(this, device.deviceNetworkId, "off", device.currentState("mode").value, Setpoint, device.currentState("fanLevel").value, device.currentState("swing").value, device.currentState("temperatureUnit").value)

    if (result) { 
    	sendEvent(name: 'thermostatMode', value: "off", displayed: false,isStateChange: true)
        //sendEvent(name: 'thermostatOperatingState', value: "idle",isStateChange: true)
    	
        generateSwitchEvent("off")
     }
    else {
       	generateErrorEvent()
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
        
    def capabilities = parent.getCapabilities(device.deviceNetworkId, device.currentState("mode").value)      
    def Level = LevelBefore
    if (capabilities.remoteCapabilities != null) {
    	def fanLevels = capabilities.remoteCapabilities.fanLevels
    	log.debug capabilities.remoteCapabilities.fanLevels
        
        Level = GetNextFanLevel("low",capabilities.remoteCapabilities.fanLevels)
        log.debug "Fan : " + Level
        
        def result = parent.setACStates(this, device.deviceNetworkId ,"on", device.currentState("mode").value, Setpoint, Level, device.currentState("swing").value, device.currentState("temperatureUnit").value)
        if (result) {
            if (device.currentState("on").value == "off") { generateSwitchEvent("on") }
            sendEvent(name: 'thermostatFanMode', value: "circulate", displayed: false)
            generatefanLevelEvent(Level)
        }
        else {
            generateErrorEvent()
        }       
        generateStatusEvent()
        refresh()
	}
    else {
    	//TODO when the mode do not exist
    }
}

def dfanMedium() {
	log.debug "fanMedium"
    
    def Setpoint = device.currentValue("targetTemperature").toInteger()
   
    def capabilities = parent.getCapabilities(device.deviceNetworkId, device.currentState("mode").value)      
    def Level = LevelBefore
    if (capabilities.remoteCapabilities != null) {
    	def fanLevels = capabilities.remoteCapabilities.fanLevels
    	log.debug capabilities.remoteCapabilities.fanLevels
        
        Level = GetNextFanLevel("medium",capabilities.remoteCapabilities.fanLevels)
        log.debug "Fan : " + Level
        
        def result = parent.setACStates(this, device.deviceNetworkId,"on", device.currentState("mode").value, Setpoint, Level, device.currentState("swing").value, device.currentState("temperatureUnit").value)
        if (result) {
            if (device.currentState("on").value == "off") { generateSwitchEvent("on") }
            sendEvent(name: 'thermostatFanMode', value: "on", displayed: false)
            generatefanLevelEvent(Level)
        }
        else {
            generateErrorEvent()
        }      
        generateStatusEvent()
        refresh()
	}
    else {
    	//TODO when the mode do not exist
    }    
}

def dfanHigh() {
	log.debug "fanHigh"
    
    def Setpoint = device.currentValue("targetTemperature").toInteger()
    
    def capabilities = parent.getCapabilities(device.deviceNetworkId, device.currentState("mode").value)      
    def Level = LevelBefore
    if (capabilities.remoteCapabilities != null) {
    	def fanLevels = capabilities.remoteCapabilities.fanLevels
    	log.debug capabilities.remoteCapabilities.fanLevels
        
        Level = GetNextFanLevel("high",capabilities.remoteCapabilities.fanLevels)
        log.debug "Fan : " + Level
        
        def result = parent.setACStates(this, device.deviceNetworkId,"on", device.currentState("mode").value, Setpoint, Level, device.currentState("swing").value, device.currentState("temperatureUnit").value)
        if (result) {
            if (device.currentState("on").value == "off") { generateSwitchEvent("on") }   
            sendEvent(name: 'thermostatFanMode', value: "on", displayed: false)
            generatefanLevelEvent(Level)
        }
        else {
            generateErrorEvent()
        }          
        generateStatusEvent()
        refresh()
	}
    else {
    	//TODO when the mode do not exist
    }        
}

def dfanAuto() {
	log.debug "fanAuto"
    
    def Setpoint = device.currentValue("targetTemperature").toInteger()

    def capabilities = parent.getCapabilities(device.deviceNetworkId, device.currentState("mode").value)      
    def Level = LevelBefore
    if (capabilities.remoteCapabilities != null) {
    	def fanLevels = capabilities.remoteCapabilities.fanLevels
    	log.debug capabilities.remoteCapabilities.fanLevels
        
        Level = GetNextFanLevel("auto",capabilities.remoteCapabilities.fanLevels)
        log.debug "Fan : " + Level       

        def result = parent.setACStates(this, device.deviceNetworkId, "on", device.currentState("mode").value, Setpoint, Level, device.currentState("swing").value, device.currentState("temperatureUnit").value)
        if (result) {
            if (device.currentState("on").value == "off") { generateSwitchEvent("on") }
            sendEvent(name: 'thermostatFanMode', value: "on", displayed: false)
            generatefanLevelEvent(Level)
        }
        else {
            generateErrorEvent()
        }        
        generateStatusEvent() 
        refresh()
	}
    else {
    	//TODO when the mode do not exist
    }         
}

def dfanQuiet() {
	log.debug "fanQuiet"
    
    def Setpoint = device.currentValue("targetTemperature").toInteger()

    def capabilities = parent.getCapabilities(device.deviceNetworkId, device.currentState("mode").value)      
    def Level = LevelBefore
    if (capabilities.remoteCapabilities != null) {
    	def fanLevels = capabilities.remoteCapabilities.fanLevels
    	log.debug capabilities.remoteCapabilities.fanLevels
        
        Level = GetNextFanLevel("quiet",capabilities.remoteCapabilities.fanLevels)
        log.debug "Fan : " + Level       

        def result = parent.setACStates(this, device.deviceNetworkId, "on", device.currentState("mode").value, Setpoint, Level, device.currentState("swing").value, device.currentState("temperatureUnit").value)
        if (result) {
            if (device.currentState("on").value == "off") { generateSwitchEvent("on") }
            sendEvent(name: 'thermostatFanMode', value: "on", displayed: false)
            generatefanLevelEvent(Level)
        }
        else {
            generateErrorEvent()
        }        
        generateStatusEvent() 
        refresh()
	}
    else {
    	//TODO when the mode do not exist
    }         
}

def dfanMediumHigh() {
	log.debug "fanMediumHigh"
    
    def Setpoint = device.currentValue("targetTemperature").toInteger()

    def capabilities = parent.getCapabilities(device.deviceNetworkId, device.currentState("mode").value)      
    def Level = LevelBefore
    if (capabilities.remoteCapabilities != null) {
    	def fanLevels = capabilities.remoteCapabilities.fanLevels
    	log.debug capabilities.remoteCapabilities.fanLevels
        
        Level = GetNextFanLevel("medium_high",capabilities.remoteCapabilities.fanLevels)
        log.debug "Fan : " + Level       

        def result = parent.setACStates(this, device.deviceNetworkId, "on", device.currentState("mode").value, Setpoint, Level, device.currentState("swing").value, device.currentState("temperatureUnit").value)
        if (result) {
            if (device.currentState("on").value == "off") { generateSwitchEvent("on") }
            sendEvent(name: 'thermostatFanMode', value: "on", displayed: false)
            generatefanLevelEvent(Level)
        }
        else {
            generateErrorEvent()
        }        
        generateStatusEvent() 
        refresh()
	}
    else {
    	//TODO when the mode do not exist
    }         
}

def dfanMediumLow() {
	log.debug "fanMediumLow"
    
    def Setpoint = device.currentValue("targetTemperature").toInteger()

    def capabilities = parent.getCapabilities(device.deviceNetworkId, device.currentState("mode").value)      
    def Level = LevelBefore
    if (capabilities.remoteCapabilities != null) {
    	def fanLevels = capabilities.remoteCapabilities.fanLevels
    	log.debug capabilities.remoteCapabilities.fanLevels
        
        Level = GetNextFanLevel("medium_low",capabilities.remoteCapabilities.fanLevels)
        log.debug "Fan : " + Level       

        def result = parent.setACStates(this, device.deviceNetworkId, "on", device.currentState("mode").value, Setpoint, Level, device.currentState("swing").value, device.currentState("temperatureUnit").value)
        if (result) {
            if (device.currentState("on").value == "off") { generateSwitchEvent("on") }
            sendEvent(name: 'thermostatFanMode', value: "on", displayed: false)
            generatefanLevelEvent(Level)
        }
        else {
            generateErrorEvent()
        }        
        generateStatusEvent() 
        refresh()
	}
    else {
    	//TODO when the mode do not exist
    }         
}

def dfanStrong() {
	log.debug "fanStrong"
    
    def Setpoint = device.currentValue("targetTemperature").toInteger()

    def capabilities = parent.getCapabilities(device.deviceNetworkId, device.currentState("mode").value)      
    def Level = LevelBefore
    if (capabilities.remoteCapabilities != null) {
    	def fanLevels = capabilities.remoteCapabilities.fanLevels
    	log.debug capabilities.remoteCapabilities.fanLevels
        
        Level = GetNextFanLevel("strong",capabilities.remoteCapabilities.fanLevels)
        log.debug "Fan : " + Level       

        def result = parent.setACStates(this, device.deviceNetworkId, "on", device.currentState("mode").value, Setpoint, Level, device.currentState("swing").value, device.currentState("temperatureUnit").value)
        if (result) {
            if (device.currentState("on").value == "off") { generateSwitchEvent("on") }
            sendEvent(name: 'thermostatFanMode', value: "on", displayed: false)
            generatefanLevelEvent(Level)
        }
        else {
            generateErrorEvent()
        }        
        generateStatusEvent() 
        refresh()
	}
    else {
    	//TODO when the mode do not exist
    }         
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
			returnCommand = dfanQuiet()
			break
        case "quiet":
			returnCommand = dfanMediumHigh()
			break
         case "medium_high":
			returnCommand = dfanMediumLow()
			break    
         case "medium_low":
			returnCommand = dfanStrong()
			break
          case "strong":
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
		case "heat":
			returnCommand = modeCool()
			break
		case "cool":
			returnCommand = modeFan()
			break		
		case "fan":
			returnCommand = modeDry()
			break
        case "dry":
            returnCommand = modeAuto()
			break
        case "auto":
			returnCommand = modeHeat()
			break
	}

	returnCommand
}

def modeHeat() {
	log.debug "modeHeat"
    
    def Setpoint = device.currentValue("targetTemperature").toInteger()
    
    def LevelBefore = device.currentState("fanLevel").value
    def capabilities = parent.getCapabilities(device.deviceNetworkId,"heat")
    def Level = LevelBefore
    if (capabilities.remoteCapabilities != null) {
    	def fanLevels = capabilities.remoteCapabilities.fanLevels
    	log.debug capabilities.remoteCapabilities.fanLevels
        
        Level = GetNextFanLevel(LevelBefore,capabilities.remoteCapabilities.fanLevels)
        log.debug "Fan : " + Level
   
        def result = parent.setACStates(this, device.deviceNetworkId, "on", "heat", Setpoint, Level, device.currentState("swing").value, device.currentState("temperatureUnit").value)
        if (result) {
        	if (LevelBefore != Level) {
                generatefanLevelEvent(Level)
            }
            sendEvent(name: 'thermostatMode', value: "heat", displayed: false,isStateChange: true)
            if (device.currentState("on").value == "off") { generateSwitchEvent("on") }

            generateModeEvent("heat")
        }
        else {
            generateErrorEvent()
        }      
        generateStatusEvent()
        refresh()
	}
    else {
       def themodes = parent.getCapabilities(device.deviceNetworkId,"modes")
       def sMode = GetNextMode("heat",themodes)

	   NextMode(sMode)
    }
}

def modeCool() {
	log.debug "modeCool"

    def Setpoint = device.currentValue("targetTemperature").toInteger()
    
    def LevelBefore = device.currentState("fanLevel").value   
    def capabilities = parent.getCapabilities(device.deviceNetworkId,"cool")
    def Level = LevelBefore
    if (capabilities.remoteCapabilities != null) {
    	def fanLevels = capabilities.remoteCapabilities.fanLevels
    	log.debug capabilities.remoteCapabilities.fanLevels
        
        Level = GetNextFanLevel(LevelBefore,capabilities.remoteCapabilities.fanLevels)
        log.debug "Fan : " + Level
        
        def result = parent.setACStates(this, device.deviceNetworkId, "on", "cool", Setpoint, Level, device.currentState("swing").value, device.currentState("temperatureUnit").value)
        if (result) {
        	if (LevelBefore != Level) {
                generatefanLevelEvent(Level)
            }
            sendEvent(name: 'thermostatMode', value: "cool", displayed: false,isStateChange: true)
            if (device.currentState("on").value == "off") { generateSwitchEvent("on") }

            generateModeEvent("cool")
        }
        else {
            generateErrorEvent()
        }    
        generateStatusEvent() 
        refresh()
	}
    else {
       def themodes = parent.getCapabilities(device.deviceNetworkId,"modes")
       def sMode = GetNextMode("cool",themodes)

	   NextMode(sMode)
    }
}

def modeFan() {
	log.debug "modeFan"

    def Setpoint = device.currentValue("targetTemperature").toInteger()
   
    def LevelBefore = device.currentState("fanLevel").value
    def capabilities = parent.getCapabilities(device.deviceNetworkId,"fan")
    def Level = LevelBefore
    if (capabilities.remoteCapabilities != null) {
    	def fanLevels = capabilities.remoteCapabilities.fanLevels
    	log.debug capabilities.remoteCapabilities.fanLevels
        
        Level = GetNextFanLevel(LevelBefore,capabilities.remoteCapabilities.fanLevels)
        log.debug "Fan : " + Level
        
        def result = parent.setACStates(this, device.deviceNetworkId, "on", "fan", Setpoint, Level, device.currentState("swing").value, device.currentState("temperatureUnit").value)
        if (result) {
			if (LevelBefore != Level) {
                generatefanLevelEvent(Level)
            }
            sendEvent(name: 'thermostatMode', value: "fan", displayed: false,isStateChange: true)

            if (device.currentState("on").value == "off") { generateSwitchEvent("on") }

            generateModeEvent("fan")
        }
        else {
            generateErrorEvent()
        }       
        generateStatusEvent()
        refresh()
	}
    else {
       def themodes = parent.getCapabilities(device.deviceNetworkId,"modes")
       def sMode = GetNextMode("fan",themodes)

	   NextMode(sMode)
    }
}

def modeDry() {
	log.debug "modeDry"

    def Setpoint = device.currentValue("targetTemperature").toInteger()
   
    def LevelBefore = device.currentState("fanLevel").value
    def capabilities = parent.getCapabilities(device.deviceNetworkId, "dry")
    def Level = LevelBefore
    if (capabilities.remoteCapabilities != null) {
    	def fanLevels = capabilities.remoteCapabilities.fanLevels
    	log.debug capabilities.remoteCapabilities.fanLevels  	
        
        Level = GetNextFanLevel(LevelBefore,capabilities.remoteCapabilities.fanLevels)
        log.debug "Fan : " + Level
        
        def result = parent.setACStates(this, device.deviceNetworkId, "on", "dry", Setpoint, Level, device.currentState("swing").value, device.currentState("temperatureUnit").value)
        if (result) {
            if (LevelBefore != Level) {
                generatefanLevelEvent(Level)
            }
            sendEvent(name: 'thermostatMode', value: "dry", displayed: false,isStateChange: true)

            if (device.currentState("on").value == "off") { generateSwitchEvent("on") }

            generateModeEvent("dry")
        }
        else {
            generateErrorEvent()
        }       
        generateStatusEvent()
        refresh()
    }
    else {
       def themodes = parent.getCapabilities(device.deviceNetworkId,"modes")
       def sMode = GetNextMode("dry",themodes)
	   log.debug "ici " + sMode
	   NextMode(sMode)
    }
}

def modeAuto() {
	log.debug "modeAuto"

    def Setpoint = device.currentValue("targetTemperature").toInteger()
    
	def LevelBefore = device.currentState("fanLevel").value
    def capabilities = parent.getCapabilities(device.deviceNetworkId, "auto")   
    def Level = LevelBefore
    if (capabilities.remoteCapabilities != null) {
    	def fanLevels = capabilities.remoteCapabilities.fanLevels
    	log.debug capabilities.remoteCapabilities.fanLevels
        
        Level = GetNextFanLevel(LevelBefore,capabilities.remoteCapabilities.fanLevels)
        log.debug "Fan : " + Level
        
        def result = parent.setACStates(this, device.deviceNetworkId, "on", "auto", Setpoint, Level, device.currentState("swing").value, device.currentState("temperatureUnit").value)
        if (result) {
            if (LevelBefore != Level) {
                    generatefanLevelEvent(Level)
            }
            sendEvent(name: 'thermostatMode', value: "auto", displayed: false,isStateChange: true)

            if (device.currentState("on").value == "off") { generateSwitchEvent("on") }

            generateModeEvent("auto")
        }
        else {
            generateErrorEvent()
        }       
        generateStatusEvent()
        refresh()
    }
    else {
       def themodes = parent.getCapabilities(device.deviceNetworkId,"modes")
       def sMode = GetNextMode("auto",themodes)

	   NextMode(sMode)
    }
}
def generateModeEvent(mode) {
   sendEvent(name: "mode", value: mode, descriptionText: "$device.displayName Thermostat mode is now ${mode}", displayed: true, isStateChange: true)   
}

def GetNextFanLevel(fanLevel, fanLevels)
{
	if (fanLevels == null) return "null"
	if (!fanLevels.contains(fanLevel)){
    	switch (fanLevel) {
        	case "high":
            	if (fanLevels.contains("auto")) return "auto"            	
                if (fanLevels.contains("quiet")) return "quiet"
                if (fanLevels.contains("medium_high")) return "medium_high"
                if (fanLevels.contains("medium_low")) return "medium_low"
                if (fanLevels.contains("strong")) return "strong"
                if (fanLevels.contains("low")) return "low"
                if (fanLevels.contains("medium")) return "medium"
                break
        	case "medium":
            	if (fanLevels.contains("high")) return "high"
                if (fanLevels.contains("auto")) return "auto"
                if (fanLevels.contains("quiet")) return "quiet"
                if (fanLevels.contains("medium_high")) return "medium_high"
                if (fanLevels.contains("medium_low")) return "medium_low"
                if (fanLevels.contains("strong")) return "strong"
            	if (fanLevels.contains("low")) return "low"                               
                break
        	case "low":
            	if (fanLevels.contains("medium")) return "medium"
                if (fanLevels.contains("high")) return "high"
            	if (fanLevels.contains("auto")) return "auto"
                if (fanLevels.contains("quiet")) return "quiet"
                if (fanLevels.contains("medium_high")) return "medium_high"
                if (fanLevels.contains("medium_low")) return "medium_low"
                if (fanLevels.contains("strong")) return "strong"
                break
        	case "auto":
                if (fanLevels.contains("quiet")) return "quiet"
                if (fanLevels.contains("medium_high")) return "medium_high"
                if (fanLevels.contains("medium_low")) return "medium_low"
                if (fanLevels.contains("strong")) return "strong"
            	if (fanLevels.contains("low")) return "low"
                if (fanLevels.contains("medium")) return "medium"
            	if (fanLevels.contains("high")) return "high"                
                break
            case "quiet":
            	if (fanLevels.contains("medium_high")) return "medium_high"
                if (fanLevels.contains("medium_low")) return "medium_low"
            	if (fanLevels.contains("strong")) return "strong"
                if (fanLevels.contains("low")) return "low"
                if (fanLevels.contains("medium")) return "medium"
                if (fanLevels.contains("high")) return "high"
                if (fanLevels.contains("auto")) return "auto"
                break
             case "medium_high":            	
                if (fanLevels.contains("medium_low")) return "medium_low"
            	if (fanLevels.contains("strong")) return "strong"
                if (fanLevels.contains("low")) return "low"
                if (fanLevels.contains("medium")) return "medium"
                if (fanLevels.contains("high")) return "high"
                if (fanLevels.contains("auto")) return "auto"
                if (fanLevels.contains("quiet")) return "quiet"
                break
             case "medium_low":            	
            	if (fanLevels.contains("strong")) return "strong"
                if (fanLevels.contains("low")) return "low"
                if (fanLevels.contains("medium")) return "medium"
                if (fanLevels.contains("high")) return "high"
                if (fanLevels.contains("auto")) return "auto"
                if (fanLevels.contains("quiet")) return "quiet"
                if (fanLevels.contains("medium_high")) return "medium_high"
                break
             case "strong":
                if (fanLevels.contains("low")) return "low"
                if (fanLevels.contains("medium")) return "medium"
                if (fanLevels.contains("high")) return "high"
                if (fanLevels.contains("auto")) return "auto"
                if (fanLevels.contains("quiet")) return "quiet"
                if (fanLevels.contains("medium_high")) return "medium_high"
            	if (fanLevels.contains("medium_low")) return "medium_low"
                break
            default:
                if (fanLevels.contains("auto")) return "auto"
                if (fanLevels.contains("quiet")) return "quiet"
                if (fanLevels.contains("medium_high")) return "medium_high"
                if (fanLevels.contains("medium_low")) return "medium_low"
                if (fanLevels.contains("strong")) return "strong"
                if (fanLevels.contains("low")) return "low"
                if (fanLevels.contains("medium")) return "medium"
                if (fanLevels.contains("high")) return "high"
                break
        }
    }    
    return fanLevel
}

def GetNextMode(mode, modes)
{
//log.debug "Modes " + modes.remoteCapabilities.containsKey("heat")
	//if (!modes.contains(mode)){
    if (!modes.remoteCapabilities.containsKey(mode)) {
    	switch (mode) {
        	case "heat":
            	if (modes.remoteCapabilities.containsKey("cool")) return "cool"
                if (modes.remoteCapabilities.containsKey("fan")) return "fan"
                if (modes.remoteCapabilities.containsKey("dry")) return "dry"
                if (modes.remoteCapabilities.containsKey("auto")) return "auto"
                break
        	case "cool":
            	if (modes.remoteCapabilities.containsKey("fan")) return "fan"
                if (modes.remoteCapabilities.containsKey("dry")) return "dry"
                if (modes.remoteCapabilities.containsKey("auto")) return "auto"
                if (modes.remoteCapabilities.containsKey("heat")) return "heat"                
                break
        	case "dry":
            	log.debug "ici"
                if (modes.remoteCapabilities.containsKey("auto")) return "auto"
            	if (modes.remoteCapabilities.containsKey("heat")) return "heat"
                if (modes.remoteCapabilities.containsKey("cool")) return "cool"
                if (modes.remoteCapabilities.containsKey("fan")) return "fan"
                log.debug "fuck"
                break
        	case "fan":
            	if (modes.remoteCapabilities.containsKey("dry")) return "dry"
                if (modes.remoteCapabilities.containsKey("auto")) return "auto"
                if (modes.remoteCapabilities.containsKey("heat")) return "heat"
                if (modes.remoteCapabilities.containsKey("cool")) return "cool"
                break
            case "auto":
            	if (modes.remoteCapabilities.containsKey("heat")) return "heat"
                if (modes.remoteCapabilities.containsKey("cool")) return "cool"
                if (modes.remoteCapabilities.containsKey("fan")) return "fan"
                if (modes.remoteCapabilities.containsKey("dry")) return "dry"
                break
        }
    }    
    return mode
}

def NextMode(sMode)
{
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

def GetNextSwingMode(swingMode, swingModes)
{
	if (swingModes == null) return ""
	if (!swingModes.contains(swingMode)){
    	switch (swingMode) {
        	case "stopped":
            	if (swingModes.contains("fixedTop")) return "fixedTop"
                if (swingModes.contains("fixedMiddleTop")) return "fixedMiddleTop"
                if (swingModes.contains("fixedMiddle")) return "fixedMiddle"
                if (swingModes.contains("fixedMiddleBottom")) return "fixedMiddleBottom"
                if (swingModes.contains("fixedBottom")) return "fixedBottom"
                if (swingModes.contains("rangeTop")) return "rangeTop"
                if (swingModes.contains("rangeMiddle")) return "rangeMiddle"
                if (swingModes.contains("rangeBottom")) return "rangeBottom"
                if (swingModes.contains("rangeFull")) return "rangeFull"
                if (swingModes.contains("horizontal")) return "horizontal"
                if (swingModes.contains("both")) return "both"
                 break
        	case "fixedTop":
            	if (swingModes.contains("fixedMiddleTop")) return "fixedMiddleTop"
                if (swingModes.contains("fixedMiddle")) return "fixedMiddle"
                if (swingModes.contains("fixedMiddleBottom")) return "fixedMiddleBottom"
                if (swingModes.contains("fixedBottom")) return "fixedBottom"
                if (swingModes.contains("rangeTop")) return "rangeTop"
                if (swingModes.contains("rangeMiddle")) return "rangeMiddle"
                if (swingModes.contains("rangeBottom")) return "rangeBottom"
                if (swingModes.contains("rangeFull")) return "rangeFull"
                if (swingModes.contains("horizontal")) return "horizontal"
                if (swingModes.contains("both")) return "both"
                if (swingModes.contains("stopped")) return "stopped"
                break
        	case "fixedMiddleTop":
				if (swingModes.contains("fixedMiddle")) return "fixedMiddle"
                if (swingModes.contains("fixedMiddleBottom")) return "fixedMiddleBottom"
                if (swingModes.contains("fixedBottom")) return "fixedBottom"
                if (swingModes.contains("rangeTop")) return "rangeTop"
                if (swingModes.contains("rangeMiddle")) return "rangeMiddle"
                if (swingModes.contains("rangeBottom")) return "rangeBottom"
                if (swingModes.contains("rangeFull")) return "rangeFull"
                if (swingModes.contains("stopped")) return "stopped"
                if (swingModes.contains("fixedTop")) return "fixedTop"
                break
        	case "fixedMiddle": 
            	if (swingModes.contains("fixedMiddleBottom")) return "fixedMiddleBottom"
                if (swingModes.contains("fixedBottom")) return "fixedBottom"
                if (swingModes.contains("rangeTop")) return "rangeTop"
                if (swingModes.contains("rangeMiddle")) return "rangeMiddle"
                if (swingModes.contains("rangeBottom")) return "rangeBottom"
                if (swingModes.contains("rangeFull")) return "rangeFull"
                if (swingModes.contains("horizontal")) return "horizontal"
                if (swingModes.contains("both")) return "both"
                if (swingModes.contains("stopped")) return "stopped"
                if (swingModes.contains("fixedTop")) return "fixedTop"
                if (swingModes.contains("fixedMiddleTop")) return "fixedMiddleTop"
                break
        	case "fixedMiddleBottom": 
            	if (swingModes.contains("fixedBottom")) return "fixedBottom"
                if (swingModes.contains("rangeTop")) return "rangeTop"
                if (swingModes.contains("rangeMiddle")) return "rangeMiddle"
                if (swingModes.contains("rangeBottom")) return "rangeBottom"
                if (swingModes.contains("rangeFull")) return "rangeFull"
                if (swingModes.contains("horizontal")) return "horizontal"
                if (swingModes.contains("both")) return "both"
                if (swingModes.contains("stopped")) return "stopped"
                if (swingModes.contains("fixedTop")) return "fixedTop"
                if (swingModes.contains("fixedMiddleTop")) return "fixedMiddleTop"
                if (swingModes.contains("fixedMiddle")) return "fixedMiddle"
                break
        	case "fixedBottom":
            	if (swingModes.contains("rangeTop")) return "rangeTop"
                if (swingModes.contains("rangeMiddle")) return "rangeMiddle"
                if (swingModes.contains("rangeBottom")) return "rangeBottom"
                if (swingModes.contains("rangeFull")) return "rangeFull"
                if (swingModes.contains("horizontal")) return "horizontal"
                if (swingModes.contains("both")) return "both"
                if (swingModes.contains("stopped")) return "stopped"
                if (swingModes.contains("fixedTop")) return "fixedTop"
                if (swingModes.contains("fixedMiddleTop")) return "fixedMiddleTop"
                if (swingModes.contains("fixedMiddle")) return "fixedMiddle"
                if (swingModes.contains("fixedMiddleBottom")) return "fixedMiddleBottom"
                break
        	case "rangeTop":           
                if (swingModes.contains("rangeMiddle")) return "rangeMiddle"
                if (swingModes.contains("rangeBottom")) return "rangeBottom"
                if (swingModes.contains("rangeFull")) return "rangeFull"
                if (swingModes.contains("horizontal")) return "horizontal"
                if (swingModes.contains("both")) return "both"
                if (swingModes.contains("stopped")) return "stopped"
                if (swingModes.contains("fixedTop")) return "fixedTop"
                if (swingModes.contains("fixedMiddleTop")) return "fixedMiddleTop"
                if (swingModes.contains("fixedMiddle")) return "fixedMiddle"
                if (swingModes.contains("fixedMiddleBottom")) return "fixedMiddleBottom"
                if (swingModes.contains("fixedBottom")) return "fixedBottom"
                break               
        	case "rangeMiddle":
            	if (swingModes.contains("rangeBottom")) return "rangeBottom"
                if (swingModes.contains("rangeFull")) return "rangeFull"
                if (swingModes.contains("horizontal")) return "horizontal"
                if (swingModes.contains("both")) return "both"
                if (swingModes.contains("stopped")) return "stopped"
                if (swingModes.contains("fixedTop")) return "fixedTop"
                if (swingModes.contains("fixedMiddleTop")) return "fixedMiddleTop"
                if (swingModes.contains("fixedMiddle")) return "fixedMiddle"
                if (swingModes.contains("fixedMiddleBottom")) return "fixedMiddleBottom"
                if (swingModes.contains("fixedBottom")) return "fixedBottom"
                if (swingModes.contains("rangeTop")) return "rangeTop"
                break
        	case "rangeBottom":  
            	if (swingModes.contains("rangeFull")) return "rangeFull"
                if (swingModes.contains("horizontal")) return "horizontal"
                if (swingModes.contains("both")) return "both"
                if (swingModes.contains("stopped")) return "stopped"
                if (swingModes.contains("fixedTop")) return "fixedTop"
                if (swingModes.contains("fixedMiddleTop")) return "fixedMiddleTop"
                if (swingModes.contains("fixedMiddle")) return "fixedMiddle"
                if (swingModes.contains("fixedMiddleBottom")) return "fixedMiddleBottom"
                if (swingModes.contains("fixedBottom")) return "fixedBottom"
                if (swingModes.contains("rangeTop")) return "rangeTop"
                if (swingModes.contains("rangeMiddle")) return "rangeMiddle"
                break
        	case "rangeFull":
                if (swingModes.contains("horizontal")) return "horizontal"
                if (swingModes.contains("both")) return "both"
            	if (swingModes.contains("stopped")) return "stopped"
                if (swingModes.contains("fixedTop")) return "fixedTop"
                if (swingModes.contains("fixedMiddleTop")) return "fixedMiddleTop"
                if (swingModes.contains("fixedMiddle")) return "fixedMiddle"
                if (swingModes.contains("fixedMiddleBottom")) return "fixedMiddleBottom"
                if (swingModes.contains("fixedBottom")) return "fixedBottom"
                if (swingModes.contains("rangeTop")) return "rangeTop"
                if (swingModes.contains("rangeMiddle")) return "rangeMiddle"
                if (swingModes.contains("rangeBottom")) return "rangeBottom"
                break
            case "horizontal":
                if (swingModes.contains("both")) return "both"
            	if (swingModes.contains("stopped")) return "stopped"
                if (swingModes.contains("fixedTop")) return "fixedTop"
                if (swingModes.contains("fixedMiddleTop")) return "fixedMiddleTop"
                if (swingModes.contains("fixedMiddle")) return "fixedMiddle"
                if (swingModes.contains("fixedMiddleBottom")) return "fixedMiddleBottom"
                if (swingModes.contains("fixedBottom")) return "fixedBottom"
                if (swingModes.contains("rangeTop")) return "rangeTop"
                if (swingModes.contains("rangeMiddle")) return "rangeMiddle"
                if (swingModes.contains("rangeBottom")) return "rangeBottom"
                if (swingModes.contains("rangeFull")) return "rangeFull"
                break
            case "both":                
            	if (swingModes.contains("stopped")) return "stopped"
                if (swingModes.contains("fixedTop")) return "fixedTop"
                if (swingModes.contains("fixedMiddleTop")) return "fixedMiddleTop"
                if (swingModes.contains("fixedMiddle")) return "fixedMiddle"
                if (swingModes.contains("fixedMiddleBottom")) return "fixedMiddleBottom"
                if (swingModes.contains("fixedBottom")) return "fixedBottom"
                if (swingModes.contains("rangeTop")) return "rangeTop"
                if (swingModes.contains("rangeMiddle")) return "rangeMiddle"
                if (swingModes.contains("rangeBottom")) return "rangeBottom"
                if (swingModes.contains("rangeFull")) return "rangeFull"
                if (swingModes.contains("horizontal")) return "horizontal"
                break               
        }
    }    
    return swingMode
}

def switchSwing() {
	log.debug "switchSwing"
	def currentMode = device.currentState("swing")?.value
	log.debug "switching Swing mode from current mode: $currentMode"
	def returnCommand
	switch (currentMode) {
		case "stopped":
			returnCommand = modeSwingTop()
			break
		case "fixedTop":
			returnCommand = modeSwingMiddleTop()
			break
        case "fixedMiddleTop":
			returnCommand = modeSwingMiddle()
			break
        case "fixedMiddle":
			returnCommand = modeSwingMiddleBottom()
			break
        case "fixedMiddleBottom":
			returnCommand = modeSwingBottom()
			break
		case "fixedBottom":
			returnCommand = modeSwingRangeTop()
			break
        case "rangeTop":
            returnCommand = modeSwingRangeMiddle()
			break        
        case "rangeMiddle":
			returnCommand = modeSwingRangeBottom()
			break
         case "rangeBottom":
			returnCommand = modeSwingRangeFull()
			break
        case "rangeFull":
			returnCommand = modeSwingHorizontal()
			break
        case "horizontal":
			returnCommand = modeSwingBoth()
			break
         case "both":
			returnCommand = modeSwingStopped()
			break
	}

	returnCommand
}

def modeSwingStopped() {
	log.debug "modeSwingStopped"
    
    def Setpoint = device.currentValue("targetTemperature").toInteger()
   
    def SwingBefore = device.currentState("swing").value
    def capabilities = parent.getCapabilities(device.deviceNetworkId, device.currentState("mode").value)
    def Swing = SwingBefore
    if (capabilities.remoteCapabilities != null) {
    	def fanLevels = capabilities.remoteCapabilities.swing
    	log.debug capabilities.remoteCapabilities.swing
        
        Swing = GetNextSwingMode("stopped",capabilities.remoteCapabilities.swing)
        log.debug "Swing : " + Swing
        
        def result = parent.setACStates(this, device.deviceNetworkId, "on", device.currentState("mode").value, Setpoint, device.currentState("fanLevel").value, Swing, device.currentState("temperatureUnit").value)
        if (result) {
            sendEvent(name: 'swing', value: Swing, displayed: false,isStateChange: true)
            if (device.currentState("on").value == "off") { generateSwitchEvent("on") }
			sendEvent(name: 'thermostatFanMode', value: "on", displayed: false)
            generateSwingModeEvent(Swing)
        }
        else {
            generateErrorEvent()
        }      
        generateStatusEvent()
        refresh()
	}
    else
    {
      //TODO
    }
}

def modeSwingTop() {
	log.debug "modeSwingFixedTop"
    
    def Setpoint = device.currentValue("targetTemperature").toInteger()
    
    def SwingBefore = device.currentState("swing").value
    def capabilities = parent.getCapabilities(device.deviceNetworkId, device.currentState("mode").value)
    def Swing = SwingBefore
    if (capabilities.remoteCapabilities != null) {
    	def fanLevels = capabilities.remoteCapabilities.swing
    	log.debug capabilities.remoteCapabilities.swing
        
        Swing = GetNextSwingMode("fixedTop",capabilities.remoteCapabilities.swing)
        log.debug "Swing : " + Swing
        
        def result = parent.setACStates(this, device.deviceNetworkId, "on", device.currentState("mode").value, Setpoint, device.currentState("fanLevel").value, Swing, device.currentState("temperatureUnit").value)
        if (result) {
            sendEvent(name: 'swing', value: Swing, displayed: false,isStateChange: true)
            if (device.currentState("on").value == "off") { generateSwitchEvent("on") }
			sendEvent(name: 'thermostatFanMode', value: "on", displayed: false)
            generateSwingModeEvent(Swing)
        }
        else {
            generateErrorEvent()
        }      
        generateStatusEvent()
        refresh()
	}
    else
    {
      //TODO
    }
}

def modeSwingMiddleTop() {
	log.debug "modeSwingFixedMiddleTop"
    
    def Setpoint = device.currentValue("targetTemperature").toInteger()
   	
    def SwingBefore = device.currentState("swing").value
    def capabilities = parent.getCapabilities(device.deviceNetworkId, device.currentState("mode").value)
    def Swing = SwingBefore
    if (capabilities.remoteCapabilities != null) {
    	def fanLevels = capabilities.remoteCapabilities.swing
    	log.debug capabilities.remoteCapabilities.swing
        
        Swing = GetNextSwingMode("fixedMiddleTop",capabilities.remoteCapabilities.swing)
        log.debug "Swing : " + Swing
        
        def result = parent.setACStates(this, device.deviceNetworkId, "on", device.currentState("mode").value, Setpoint, device.currentState("fanLevel").value, Swing, device.currentState("temperatureUnit").value)
        if (result) {
            sendEvent(name: 'swing', value: Swing, displayed: false,isStateChange: true)
            if (device.currentState("on").value == "off") { generateSwitchEvent("on") }
			sendEvent(name: 'thermostatFanMode', value: "on", displayed: false)
            generateSwingModeEvent(Swing)
        }
        else {
            generateErrorEvent()
        }      
        generateStatusEvent()
        refresh()
	}
    else
    {
      //TODO
    }
}

def modeSwingMiddle() {
	log.debug "modeSwingFixedMiddle"
    
    def Setpoint = device.currentValue("targetTemperature").toInteger()
   
    def SwingBefore = device.currentState("swing").value
    def capabilities = parent.getCapabilities(device.deviceNetworkId, device.currentState("mode").value)
    def Swing = SwingBefore
    if (capabilities.remoteCapabilities != null) {
    	def fanLevels = capabilities.remoteCapabilities.swing
    	log.debug capabilities.remoteCapabilities.swing
        
        Swing = GetNextSwingMode("fixedMiddle",capabilities.remoteCapabilities.swing)
        log.debug "Swing : " + Swing
        
        def result = parent.setACStates(this, device.deviceNetworkId, "on", device.currentState("mode").value, Setpoint, device.currentState("fanLevel").value, Swing, device.currentState("temperatureUnit").value)
        if (result) {
            sendEvent(name: 'swing', value: Swing, displayed: false,isStateChange: true)
            if (device.currentState("on").value == "off") { generateSwitchEvent("on") }
			sendEvent(name: 'thermostatFanMode', value: "on", displayed: false)
            generateSwingModeEvent(Swing)
        }
        else {
            generateErrorEvent()
        }      
        generateStatusEvent()
        refresh()
	}
    else
    {
      //TODO
    }
}

def modeSwingMiddleBottom() {
	log.debug "modeSwingFixedMiddleBottom"
    
    def Setpoint = device.currentValue("targetTemperature").toInteger()
   
    def SwingBefore = device.currentState("swing").value
    def capabilities = parent.getCapabilities(device.deviceNetworkId, device.currentState("mode").value)
    def Swing = SwingBefore
    if (capabilities.remoteCapabilities != null) {
    	def fanLevels = capabilities.remoteCapabilities.swing
    	log.debug capabilities.remoteCapabilities.swing
        
        Swing = GetNextSwingMode("fixedMiddleBottom",capabilities.remoteCapabilities.swing)
        log.debug "Swing : " + Swing
        
        def result = parent.setACStates(this, device.deviceNetworkId, "on", device.currentState("mode").value, Setpoint, device.currentState("fanLevel").value, Swing, device.currentState("temperatureUnit").value)
        if (result) {
            sendEvent(name: 'swing', value: Swing, displayed: false,isStateChange: true)
            if (device.currentState("on").value == "off") { generateSwitchEvent("on") }
			sendEvent(name: 'thermostatFanMode', value: "on", displayed: false)
            generateSwingModeEvent(Swing)
        }
        else {
            generateErrorEvent()
        }      
        generateStatusEvent()
        refresh()
	}
    else
    {
      //TODO
    }
}

def modeSwingBottom() {
	log.debug "modeSwingFixedBottom"
    
    def Setpoint = device.currentValue("targetTemperature").toInteger()
   
	def SwingBefore = device.currentState("swing").value
    def capabilities = parent.getCapabilities(device.deviceNetworkId, device.currentState("mode").value)
    def Swing = SwingBefore
    if (capabilities.remoteCapabilities != null) {
    	def fanLevels = capabilities.remoteCapabilities.swing
    	log.debug capabilities.remoteCapabilities.swing
        
        Swing = GetNextSwingMode("fixedBottom",capabilities.remoteCapabilities.swing)
        log.debug "Swing : " + Swing
        
        def result = parent.setACStates(this, device.deviceNetworkId, "on", device.currentState("mode").value, Setpoint, device.currentState("fanLevel").value, Swing, device.currentState("temperatureUnit").value)
        if (result) {
            sendEvent(name: 'swing', value: Swing, displayed: false,isStateChange: true)
            if (device.currentState("on").value == "off") { generateSwitchEvent("on") }
			sendEvent(name: 'thermostatFanMode', value: "on", displayed: false)
            generateSwingModeEvent(Swing)
        }
        else {
            generateErrorEvent()
        }      
        generateStatusEvent()
        refresh()
	}
    else
    {
      //TODO
    }
}

def modeSwingRangeTop() {
	log.debug "modeRangeTop"
    
    def Setpoint = device.currentValue("targetTemperature").toInteger()
   
	def SwingBefore = device.currentState("swing").value
    def capabilities = parent.getCapabilities(device.deviceNetworkId, device.currentState("mode").value)
    def Swing = SwingBefore
    if (capabilities.remoteCapabilities != null) {
    	def fanLevels = capabilities.remoteCapabilities.swing
    	log.debug capabilities.remoteCapabilities.swing
        
        Swing = GetNextSwingMode("rangeTop",capabilities.remoteCapabilities.swing)
        log.debug "Swing : " + Swing
        
        def result = parent.setACStates(this, device.deviceNetworkId, "on", device.currentState("mode").value, Setpoint, device.currentState("fanLevel").value, Swing, device.currentState("temperatureUnit").value)
        if (result) {
            sendEvent(name: 'swing', value: Swing, displayed: false,isStateChange: true)
            if (device.currentState("on").value == "off") { generateSwitchEvent("on") }
			sendEvent(name: 'thermostatFanMode', value: "on", displayed: false)
            generateSwingModeEvent(Swing)
        }
        else {
            generateErrorEvent()
        }      
        generateStatusEvent()
        refresh()
	}
    else
    {
      //TODO
    }
}

def modeSwingRangeMiddle() {
	log.debug "modeRangeMiddle"
    
    def Setpoint = device.currentValue("targetTemperature").toInteger()
   
	def SwingBefore = device.currentState("swing").value
    def capabilities = parent.getCapabilities(device.deviceNetworkId, device.currentState("mode").value)
    def Swing = SwingBefore
    if (capabilities.remoteCapabilities != null) {
    	def fanLevels = capabilities.remoteCapabilities.swing
    	log.debug capabilities.remoteCapabilities.swing
        
        Swing = GetNextSwingMode("rangeMiddle",capabilities.remoteCapabilities.swing)
        log.debug "Swing : " + Swing
        
        def result = parent.setACStates(this, device.deviceNetworkId, "on", device.currentState("mode").value, Setpoint, device.currentState("fanLevel").value, Swing, device.currentState("temperatureUnit").value)
        if (result) {
            sendEvent(name: 'swing', value: Swing, displayed: false,isStateChange: true)
            if (device.currentState("on").value == "off") { generateSwitchEvent("on") }
			sendEvent(name: 'thermostatFanMode', value: "on", displayed: false)
            generateSwingModeEvent(Swing)
        }
        else {
            generateErrorEvent()
        }      
        generateStatusEvent()
        refresh()
	}
    else
    {
      //TODO
    }
}

def modeSwingRangeBottom() {
	log.debug "modeRangeBottom"
    
    def Setpoint = device.currentValue("targetTemperature").toInteger()
   
    def SwingBefore = device.currentState("swing").value
    def capabilities = parent.getCapabilities(device.deviceNetworkId, device.currentState("mode").value)
    def Swing = SwingBefore
    if (capabilities.remoteCapabilities != null) {
    	def fanLevels = capabilities.remoteCapabilities.swing
    	log.debug capabilities.remoteCapabilities.swing
        
        Swing = GetNextSwingMode("rangeBottom",capabilities.remoteCapabilities.swing)
        log.debug "Swing : " + Swing
        
        def result = parent.setACStates(this, device.deviceNetworkId, "on", device.currentState("mode").value, Setpoint, device.currentState("fanLevel").value, Swing, device.currentState("temperatureUnit").value)
        if (result) {
            sendEvent(name: 'swing', value: Swing, displayed: false,isStateChange: true)
            if (device.currentState("on").value == "off") { generateSwitchEvent("on") }
			sendEvent(name: 'thermostatFanMode', value: "on", displayed: false)
            generateSwingModeEvent(Swing)
        }
        else {
            generateErrorEvent()
        }      
        generateStatusEvent()
        refresh()
	}
    else
    {
      //TODO
    }
}

def modeSwingRangeFull() {
	log.debug "modeRangeFull"
    
    def Setpoint = device.currentValue("targetTemperature").toInteger()
   
    def SwingBefore = device.currentState("swing").value
    def capabilities = parent.getCapabilities(device.deviceNetworkId, device.currentState("mode").value)
    def Swing = SwingBefore
    if (capabilities.remoteCapabilities != null) {
    	def fanLevels = capabilities.remoteCapabilities.swing
    	log.debug capabilities.remoteCapabilities.swing
        
        Swing = GetNextSwingMode("rangeFull",capabilities.remoteCapabilities.swing)
        log.debug "Swing : " + Swing
        
        def result = parent.setACStates(this, device.deviceNetworkId, "on", device.currentState("mode").value, Setpoint, device.currentState("fanLevel").value, Swing, device.currentState("temperatureUnit").value)
        if (result) {
            sendEvent(name: 'swing', value: Swing, displayed: false,isStateChange: true)
            if (device.currentState("on").value == "off") { generateSwitchEvent("on") }
			sendEvent(name: 'thermostatFanMode', value: "on", displayed: false)
            generateSwingModeEvent(Swing)
        }
        else {
            generateErrorEvent()
        }      
        generateStatusEvent()
        refresh()
	}
    else
    {
      //TODO
    }
}

def modeSwingHorizontal() {
	log.debug "modeHorizontal"
    
    def Setpoint = device.currentValue("targetTemperature").toInteger()
   
    def SwingBefore = device.currentState("swing").value
    def capabilities = parent.getCapabilities(device.deviceNetworkId, device.currentState("mode").value)
    def Swing = SwingBefore
    log.debug "Swing avant " + Swing
    if (capabilities.remoteCapabilities != null) {
    	def fanLevels = capabilities.remoteCapabilities.swing
    	log.debug capabilities.remoteCapabilities.swing
        
        Swing = GetNextSwingMode("horizontal",capabilities.remoteCapabilities.swing)
        log.debug "Swing : " + Swing
        
        def result = parent.setACStates(this, device.deviceNetworkId, "on", device.currentState("mode").value, Setpoint, device.currentState("fanLevel").value, Swing, device.currentState("temperatureUnit").value)
        if (result) {
            sendEvent(name: 'swing', value: Swing, displayed: false,isStateChange: true)
            if (device.currentState("on").value == "off") { generateSwitchEvent("on") }
			sendEvent(name: 'thermostatFanMode', value: "on", displayed: false)
            generateSwingModeEvent(Swing)
        }
        else {
            generateErrorEvent()
        }      
        generateStatusEvent()
        refresh()
	}
    else
    {
      //TODO
    }
}

def modeSwingBoth() {
	log.debug "modeBoth"
    
    def Setpoint = device.currentValue("targetTemperature").toInteger()
   
    def SwingBefore = device.currentState("swing").value
    def capabilities = parent.getCapabilities(device.deviceNetworkId, device.currentState("mode").value)
    def Swing = SwingBefore
    if (capabilities.remoteCapabilities != null) {
    	def fanLevels = capabilities.remoteCapabilities.swing
    	log.debug capabilities.remoteCapabilities.swing
        
        Swing = GetNextSwingMode("both",capabilities.remoteCapabilities.swing)
        log.debug "Swing : " + Swing
        
        def result = parent.setACStates(this, device.deviceNetworkId, "on", device.currentState("mode").value, Setpoint, device.currentState("fanLevel").value, Swing, device.currentState("temperatureUnit").value)
        if (result) {
            sendEvent(name: 'swing', value: Swing, displayed: false,isStateChange: true)
            if (device.currentState("on").value == "off") { generateSwitchEvent("on") }
			sendEvent(name: 'thermostatFanMode', value: "on", displayed: false)
            generateSwingModeEvent(Swing)
        }
        else {
            generateErrorEvent()
        }      
        generateStatusEvent()
        refresh()
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
            else if (name== "battery") {            	                
                isChange = true //isTemperatureStateChange(device, name, value.toString())
                isDisplayed = true
                 
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
                isDisplayed = true
                  
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
                
                //def mode = (value.toString() != "fan") ?: "auto"
                
				if (value=="cool") {
					sendEvent(name: 'thermostatOperatingState', value: "cooling", 
					isStateChange: isChange,
					displayed: isDisplayed)
				} else if (value=="heat") {
					sendEvent(name: 'thermostatOperatingState', value: "heating", 
					isStateChange: isChange,
					displayed: isDisplayed)
				} else if (value=="fan") {
					sendEvent(name: 'thermostatOperatingState', value: "fan only", 
					isStateChange: isChange,
					displayed: isDisplayed)
                } else if (value=="dry") {
					sendEvent(name: 'thermostatOperatingState', value: "Dry", 
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
                isDisplayed = true
                   
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
                isDisplayed = true
                   
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
					//unit: device.currentValue("temperatureUnit"),
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
    else if (name == "powerSource")
    {
        return "power source mode was ${value}"
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
        //value = state.swingMode
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
    def swing = device.currentValue("swing")
	def error = device.currentValue("Error")
                    
    def statusTextmsg = ""
    def sUnit = device.currentValue("temperatureUnit")

    statusTextmsg = "${humidity}%"
   
    sendEvent("name":"statusText", "value":statusTextmsg, "description":"", displayed: false, isStateChange: true)
}
