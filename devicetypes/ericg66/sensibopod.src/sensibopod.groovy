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
	//input("username", "text", title: "Username", description: "Your Sensibo username (usually an email address)")
	//input("password", "password", title: "Password", description: "Your Sensibo password")
	//input("apikey", "text", title: "Serial #", description: "The APIkey number of your Sensibo account")
}

metadata {
	definition (name: "SensiboPod", namespace: "EricG66", author: "Eric Gosselin", oauth: false) {
		capability "Relative Humidity Measurement"
		capability "Temperature Measurement"
		capability "Polling"
        capability "Refresh"
        capability "Switch"
        capability "Thermostat"
        
		attribute "temperatureUnit", "string"
        
		command "getPodInfo"
		command "setFanLevel"
		command "setACMode"
		command "setPoint"
        command "getTemperature"
        command "getHumidity"
        command "generateEvent"
        command "levelUpDown"
        command "switchFanLevel"
        command "switchMode"

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
		// TODO: define your main and details tiles here
        
    	multiAttributeTile(name:"richcontact", type:"lighting", width:6, height:4) {
   			tileAttribute("device.targetTemperature", key: "PRIMARY_CONTROL") {
            	attributeState ("targetTemperature", label:'${currentValue} C', 
                backgroundColors:[
					[value: 15, color: "#153591"],
					[value: 18, color: "#1e9cbb"],
					[value: 21, color: "#90d2a7"],
					[value: 24, color: "#44b621"],
					[value: 27, color: "#f1d801"],
					[value: 30, color: "#d04e00"],
					[value: 33, color: "#bc2323"]
				])
       			//attributeState "on", label: '${temperature}',action:"switch.off", icon:"st.switches.switch.on", backgroundColor:"#79b821"
      			//attributeState "off", label:'${name}',action:"switch.on", icon:"st.switches.switch.off", backgroundColor:"#ffffff"
    		}
            tileAttribute ("statusText", key: "SECONDARY_CONTROL") {
				attributeState "statusText", label:'${currentValue}'
			}
            tileAttribute ("device.level", key: "VALUE_CONTROL") {
				attributeState "level", action: "levelUpDown"
	        }
  		}
        
        standardTile("fanLevel", "device.fanLevel", width: 2, height: 2) {
            state "low", action:"switchFanLevel", backgroundColor:"#8C8C8D", icon:"http://i130.photobucket.com/albums/p242/brutalboy_photos/fan_low_2.png", nextState:"medium"
            state "medium", action:"switchFanLevel", backgroundColor:"#8C8C8D", icon:"http://i130.photobucket.com/albums/p242/brutalboy_photos/fan_medium_2.png", nextState:"high"
            state "high", action:"switchFanLevel", backgroundColor:"#8C8C8D", icon:"http://i130.photobucket.com/albums/p242/brutalboy_photos/fan_high_2.png", nextState:"auto"
            state "auto", action:"switchFanLevel", backgroundColor:"#8C8C8D", icon:"http://i130.photobucket.com/albums/p242/brutalboy_photos/fan_auto_2.png" , nextState:"low"
        }
        
        standardTile("mode", "device.mode",  width: 2, height: 2) {
            state "cool", action:"switchMode", backgroundColor:"#0099FF", icon:"st.thermostat.cool", nextState:"heat"
            state "heat", action:"switchMode", backgroundColor:"#FF3300", icon:"st.thermostat.heat", nextState:"fan"
            state "fan", action:"switchMode", backgroundColor:"#e8e3d8", icon:"st.thermostat.fan-on", nextState:"cool"
            //e8e3d8
        }
        
        standardTile("refresh", "device.switch", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "default", label:'', action:"refresh.refresh", icon:"st.secondary.refresh"
		}

        valueTile("targetTemperature", "device.targetTemperature", inactiveLabel: false, width: 2, height: 2) {
			state "targetTemperature", label:'${currentValue} C', backgroundColor:"#ffffff"
        }
        
        valueTile("statusText", "statusText", inactiveLabel: false, width: 2, height: 2) {
			state "statusText", label:'${currentValue}', backgroundColor:"#ffffff"
        }
        
        valueTile("fanLevel2", "device.fanLevel", inactiveLabel: false, width: 2, height: 2) {
			state "fanLevel", label:'${currentValue}', backgroundColor:"#ffffff"
        }
        
        valueTile("mode2", "device.mode", inactiveLabel: false, width: 2, height: 2) {
			state "mode", label:'${currentValue}', backgroundColor:"#ffffff"
        }

		controlTile("coolSliderControl", "device.coolingSetpoint", "slider", height: 2, width: 2, inactiveLabel: false,range:"(15..30)") {
			state "setCoolingSetpoint", label:'Set temperature to', action:"thermostat.setCoolingSetpoint",
				backgroundColors:[
					[value: 31, color: "#153591"],
					[value: 44, color: "#1e9cbb"],
					[value: 59, color: "#90d2a7"],
					[value: 74, color: "#44b621"],
					[value: 84, color: "#f1d801"],
					[value: 95, color: "#d04e00"],
					[value: 96, color: "#bc2323"]
				]
		}
        valueTile("coolingSetpoint", "device.coolingSetpoint", inactiveLabel: false, decoration: "flat") {
			state "setCoolingSetpoint", label:'${currentValue}°', unit:"C"
		}
        valueTile("temperature", "device.temperature", width: 2, height: 2) {
			state("temperature", label:'Temp: ${currentValue} C', unit:"C",
				backgroundColors:[
					[value: 15, color: "#153591"],
					[value: 18, color: "#1e9cbb"],
					[value: 21, color: "#90d2a7"],
					[value: 24, color: "#44b621"],
					[value: 27, color: "#f1d801"],
					[value: 30, color: "#d04e00"],
					[value: 33, color: "#bc2323"]
				]
			)
		}
        
        valueTile("humidity", "device.humidity", width: 2, height: 2) {
			state("humidity", label:'Humidity: ${currentValue} %', unit:"C",
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
        
        standardTile("on", "device.on", width: 2, height: 2, canChangeIcon: true) {
			state "on", label:'${name}', action:"switch.off", icon:"st.switches.switch.on", backgroundColor:"#79b821"
			state "off", label:'${name}', action:"switch.on", icon:"st.switches.switch.off", backgroundColor:"#ffffff"
		}
		       
		main (["on"])
		//main "switch"
		details (["richcontact","on","temperature","humidity","switch", "fanLevel","mode","coolSliderControl","refresh"])    
        //details (["temperature","humidity","switch","statusText","targetTemperature","refresh", "fanLevel"])
	}
}

def fanLevelModes() {
	["low", "medium","high", "auto"]
}

def Modes() {
   ["cool","heat","fan"]
}

def levelUpDown(value) {
    //log.trace "levelUpDown called with value $value" // Values are 0 and 1
    //humidity = "10"
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
    //log.debug device.deviceNetworkId
    
    def result = parent.setACStates(this, device.deviceNetworkId , "on", device.currentState("mode").value, temp, device.currentState("fanLevel").value)
    log.debug result
    generateSetTempEvent(temp)
    
    generateStatusEvent() 
}

def generateSetTempEvent(temp) {
   sendEvent(name: "targetTemperature", value: temp, descriptionText: "$device.displayName set temperature is now ${temp}", displayed: true, isStateChange: true)
}

// Turn off or Turn on the AC
def on() {
	log.debug "on called"
    
    parent.setACStates(this, device.deviceNetworkId, "on", device.currentState("mode").value, device.currentState("targetTemperature").value, device.currentState("fanLevel").value)
    generateSwitchEvent("on")
    
    generateStatusEvent() 

}

def off() {
	log.debug "off called"
    parent.setACStates(this, device.deviceNetworkId, "off", device.currentState("mode").value, device.currentState("targetTemperature").value, device.currentState("fanLevel").value)
    generateSwitchEvent("off")
    
    generateStatusEvent() 
}

def generateSwitchEvent(mode) {
   sendEvent(name: "on", value: mode, descriptionText: "$device.displayName is now ${mode}", displayed: true, isStateChange: true)
}

// For Fan Level
def fanLow() {
	log.debug "fanLow"
    //log.debug device.deviceNetworkId
    
    def result = parent.setACStates(this, device.deviceNetworkId ,"on", device.currentState("mode").value, device.currentState("targetTemperature").value, "low")
    log.debug result
    generatefanLevelEvent("low")
    generateSwitchEvent("on")
    
    generateStatusEvent() 
}

def fanMedium() {
	log.debug "fanMedium"

    parent.setACStates(this, device.deviceNetworkId,"on", device.currentState("mode").value, device.currentState("targetTemperature").value, "medium")
    generatefanLevelEvent("medium")
    generateSwitchEvent("on")
    
    generateStatusEvent() 
}

def fanHigh() {
	log.debug "fanHigh"
    
    parent.setACStates(this, device.deviceNetworkId,"on", device.currentState("mode").value, device.currentState("targetTemperature").value, "high")
    generatefanLevelEvent("high")
    generateSwitchEvent("on")
    
    generateStatusEvent() 
}

def fanAuto() {
	log.debug "fanAuto"
    parent.setACStates(this, device.deviceNetworkId, "on", device.currentState("mode").value, device.currentState("targetTemperature").value, "auto")
    generatefanLevelEvent("auto")
    generateSwitchEvent("on")
    
    generateStatusEvent() 
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
			returnCommand = fanMedium()
			break
		case "medium":
			returnCommand = fanHigh()
			break
		case "high":
			returnCommand = fanAuto()
			break
        case "auto":
			returnCommand = fanLow()
			break
	}
	//if(!currentFanMode) { returnCommand = switchToFanMode("fanOn") }
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
	//if(!currentFanMode) { returnCommand = switchToFanMode("fanOn") }
	returnCommand
}

def modeHeat() {
	log.debug "modeHeat"
    //parent.setACStates(this, device.deviceNetworkId, device.currentState("on").value, "heat", device.currentState("targetTemperature").value, device.currentState("fanLevel").value)
    parent.setACStates(this, device.deviceNetworkId, "on", "heat", device.currentState("targetTemperature").value, device.currentState("fanLevel").value)
    generateModeEvent("heat")
    generateSwitchEvent("on")
    
    generateStatusEvent()
    
    //device.setIcon("on","on","st.thermostat.heat")
    //device.save()
}

def modeCool() {
	log.debug "modeCool"
    //parent.setACStates(this, device.deviceNetworkId, device.currentState("on").value, "cool", device.currentState("targetTemperature").value, device.currentState("fanLevel").value)
    parent.setACStates(this, device.deviceNetworkId, "on", "cool", device.currentState("targetTemperature").value, device.currentState("fanLevel").value)
    generateModeEvent("cool")
    generateSwitchEvent("on")

    generateStatusEvent() 
    
    //device.setIcon("on","on","st.thermostat.cool")
    //device.save()
}

def modeFan() {
	log.debug "modeFan"
    
    // adapted to my AC model
    def Level = (device.currentState("fanLevel").value == "auto") ? "high" : device.currentState("fanLevel").value
    
     if (Level != device.currentState("fanLevel").value) {
      generatefanLevelEvent("high")
    }
    //parent.setACStates(this, device.deviceNetworkId, device.currentState("on").value, "fan", device.currentState("targetTemperature").value, Level)
    parent.setACStates(this, device.deviceNetworkId, "on", "fan", device.currentState("targetTemperature").value, Level)
    generateModeEvent("fan")
   
    generateStatusEvent()
    
    //device.setIcon("on","on","st.switches.switch.on")
    //device.save()
}

def generateModeEvent(mode) {
   sendEvent(name: "mode", value: mode, descriptionText: "$device.displayName mode is now ${mode}", displayed: true, isStateChange: true)
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
            
            if (name=="temperature" || name=="targetTemperature" || name== "humidity") {
				isChange = true//isTemperatureStateChange(device, name, value.toString())
                isDisplayed = isChange
                   
				sendEvent(
					name: name,
					value: value,
					unit: "C",
					linkText: linkText,
					descriptionText: getThermostatDescriptionText(name, value, linkText),
					handlerName: name,
					isStateChange: isChange,
					displayed: isDisplayed)
                    
            }
            else {
            	isChange = true//isStateChange(device, name, value.toString())
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
		//generateSetpointEvent ()  
        generateStatusEvent ()
	}
}

private getThermostatDescriptionText(name, value, linkText)
{
	if(name == "temperature")
	{
		return "$name was $value C"
	}
    else if(name == "humidity")
	{
		return "$name was $value %"
    }
    else if(name == "targetTemperature")
	{
		return "latest temperature setpoint was $value C"
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
        return "thermostat mode is ${value}"
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
	// TODO: handle 'humidity' attribute
	// TODO: handle 'temperature' attribute
	// TODO: handle 'temperatureUnit' attribute
	
    def result = createEvent(name: name, value: value)
	log.debug "Parse returned ${result?.descriptionText}"
	return result
}

def setPoint() {
	log.debug "Executing 'setPoint'"
	// TODO: handle 'setPoint' command
}

def generateStatusEvent() {
    log.debug device
    def temperature = device.currentValue("temperature").toDouble()  
    def humidity = device.currentValue("humidity").toDouble() 
    def targetTemperature = device.currentValue("targetTemperature").toDouble()
    def fanLevel = device.currentValue("fanLevel")
    def mode = device.currentValue("mode")
    def on = device.currentValue("on")

    
    log.debug "Generate Status Event for Mode = ${mode}"
    log.debug "Temperature = ${temperature}"
    log.debug "Humidity = ${humidity}"
    log.debug "targetTemperature = ${targetTemperature}"
    log.debug "fanLevel = ${fanLevel}"
    log.debug "mode = ${mode}"
	log.debug "switch = ${on}"
            
    //} else {
    
    //    statusText = "?"
        
    //}
    
    def statusTextmsg = "Temp: ${temperature} C humidity ${humidity}%"
    //sendEvent("name":"statusText", "value":statusTextmsg)
    
    //log.debug "Generate Status Event = ${statusText}"
    sendEvent("name":"statusText", "value":statusTextmsg, "description":statusText, displayed: true, isStateChange: true)
}
