/**
 *  Copyright 2016 Kirk Brown
 * 	This code was started from the ECOBEE Thermostat device type template.
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
 *	Sensi Thermostat
 *
 *	Author: Kirk Brown
 *	Date: 2016-12-26
 *	Modified Heavily 2017-01-07 will NOT work with versions of SmartApp before 2017-01-07
 *		The device type now correctly parses partial update messages in addition to complete new subscription messages. This should be most noticeable
 *			for people with 1 sensi thermostat. 
 *
 * There are a large number of debug statements that will turn on if you uncomment the statement inside the TRACE function at the bottom of the code
 *
 *
 */
metadata {
	definition (name: "Sensi Thermostat", namespace: "kirkbrownOK/SensiThermostat", author: "Kirk Brown") {
		
		capability "Thermostat"
		capability "Temperature Measurement"
		capability "Sensor"
		capability "Refresh"
		capability "Relative Humidity Measurement"
		capability "Health Check"
        capability "Battery"

		command "generateEvent"
		command "raiseSetpoint"
		command "lowerSetpoint"
		command "resumeProgram"
		command "switchMode"
		command "switchFanMode"
        command "stopSchedule"
        command "heatUp"
        command "heatDown"
        command "coolUp"
        command "coolDown"
        command "refresh"
        command "poll"
        command "setKeypadLockoutOn"
        command "setKeypadLockoutOff"
        attribute "keypadLockout", "string" 
        
		attribute "thermostatSetpoint", "number"
		attribute "thermostatStatus", "string"
		attribute "maxHeatingSetpoint", "number"
		attribute "minHeatingSetpoint", "number"
		attribute "maxCoolingSetpoint", "number"
		attribute "minCoolingSetpoint", "number"
		attribute "deviceTemperatureUnit", "string"
		attribute "deviceAlive", "enum", ["true", "false"]
        attribute "operationalStatus", "string"
        attribute "environmentControls", "string"
        attribute "capabilities", "string"
        attribute "product", "string"
        attribute "settings", "string"

        
        
        attribute "sensiThermostatMode", "string"
        attribute "sensiBatteryVoltage", "number"
        attribute "sensiLowPower", "string" //New Attribute
        attribute "thermostatHoldMode", "string"
        attribute "thermostatOperatingMode", "string"
        attribute "thermostatFanState", "string"
	}

tiles(scale:2) {
		multiAttributeTile(name:"thermostatFull", type:"thermostat", width:6, height:4) {
			tileAttribute("device.temperature", key: "PRIMARY_CONTROL") {
                attributeState("default", label:'${currentValue}', unit:"dF", defaultState: true)
			}
			tileAttribute("device.temperature", key: "VALUE_CONTROL") {
				attributeState("VALUE_UP", action: "raiseSetpoint")
				attributeState("VALUE_DOWN", action: "lowerSetpoint")
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
                attributeState("aux", label: '${name}')
				attributeState("cool", label:'${name}')
				attributeState("auto", label:'${name}')
			}
			tileAttribute("device.heatingSetpoint", key: "HEATING_SETPOINT") {
                attributeState("default", label:'${currentValue}', unit:"dF")
			}
			tileAttribute("device.coolingSetpoint", key: "COOLING_SETPOINT") {
				attributeState("default", label:'${currentValue}', unit:"dF")
			}
		}
        valueTile("temperature", "device.temperature", decoration: "flat") {
            state "temperature", label:'${currentValue}°', unit:"F",
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
                [value: 63, color: "#90d2a7"],
                [value: 74, color: "#44b621"],
                [value: 80, color: "#f1d801"],
                [value: 95, color: "#d04e00"],
                [value: 96, color: "#bc2323"]
            ]
        }
        valueTile("temperatureIcon", "device.temperature", decoration: "flat") {
            state "temperature", label:'${currentValue}°', unit:"F",icon:"st.Home.home1",
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
                [value: 63, color: "#90d2a7"],
                [value: 74, color: "#44b621"],
                [value: 80, color: "#f1d801"],
                [value: 95, color: "#d04e00"],
                [value: 96, color: "#bc2323"]
            ]
        }

        valueTile("heatingSetpoint", "device.heatingSetpoint", inactiveLabel:false) {
            state "default", label:'${currentValue}°', unit:"F",
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

        valueTile("coolingSetpoint", "device.coolingSetpoint", inactiveLabel:false) {
            state "default", label:'${currentValue}°', unit:"F",
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
        
        
        standardTile("heatUp", "device.heatingSetpoint", inactiveLabel:false, decoration:"flat") {
            state "default", label:'Heating', icon:"st.custom.buttons.add-icon", action:"heatUp"
        }

        standardTile("heatDown", "device.heatingSetpoint", inactiveLabel:false, decoration:"flat") {
            state "default", label:'Heating', icon:"st.custom.buttons.subtract-icon", action:"heatDown"
        }

        standardTile("coolUp", "device.coolingSetpoint", inactiveLabel:false, decoration:"flat") {
            state "default", label:'Cooling', icon:"st.custom.buttons.add-icon", action:"coolUp"
        }

        standardTile("coolDown", "device.coolingSetpoint", inactiveLabel:false, decoration:"flat") {
            state "default", label:'Cooling', icon:"st.custom.buttons.subtract-icon", action:"coolDown"
        }

        standardTile("operatingState", "device.thermostatOperatingState", inactiveLabel:false, decoration:"flat") {
            state "default", label:'[State]'
            state "idle", label:'', icon:"st.thermostat.heating-cooling-off"
            state "heating", label:'', icon:"st.thermostat.heating"
            state "cooling", label:'', icon:"st.thermostat.cooling"
        }

        standardTile("fanState", "device.thermostatFanState", inactiveLabel:false, decoration:"flat") {
            state "default", label:'[Fan State]'
            state "on", label:'', icon:"st.thermostat.fan-on"
            state "off", label:'', icon:"st.thermostat.fan-off"
        }
        standardTile("keypadLockout", "device.keypadLockout", inactiveLabel:false, decoration:"flat", width: 2) {
            state "default", label:'Keypad Unknown', action: "setKeypadLockoutOn"
            state "on", label:'KEYPAD: LOCKED', action: "setKeypadLockoutOff", nextState: "off"
            state "off", label:'KEYPAD: UNLOCKED', action: "setKeypadLockoutOn", nextState: "on"
        }

        standardTile("mode", "device.thermostatMode", inactiveLabel:false) {
            state "default", label:'[Mode]'
            state "off", label:'', icon:"st.thermostat.heating-cooling-off", backgroundColor:"#FFFFFF", action:"thermostat.heat"
            state "heat", label:'', icon:"st.thermostat.heat", backgroundColor:"#FFCC99", action:"thermostat.cool"
            state "aux", label:'', icon:"st.thermostat.heat", backgroundColor:"#FFCC99", action:"thermostat.cool"
            state "cool", label:'', icon:"st.thermostat.cool", backgroundColor:"#99CCFF", action:"thermostat.auto"
            state "auto", label:'', icon:"st.thermostat.auto", backgroundColor:"#99FF99", action:"thermostat.off"
        }

        standardTile("fanMode", "device.thermostatFanMode", inactiveLabel:false) {
            state "default", label:'[Fan Mode]'
            state "auto", label:'', icon:"st.thermostat.fan-auto", backgroundColor:"#A4FCA6", action:"thermostat.fanOn"
            state "on", label:'', icon:"st.thermostat.fan-on", backgroundColor:"#FAFCA4", action:"thermostat.fanAuto"
        }

        standardTile("hold", "device.thermostatHoldMode",decoration:"flat") {
            state "default", label:'[Hold]'
            state "on", label:'Permanent Hold', backgroundColor:"#FFDB94", action:"resumeProgram"
            state "temporary", label: 'Temp Hold', backgroundColor:"#FFDB94", action:"resumeProgram"
            state "off", label:'Sensi Schedule', backgroundColor:"#FFFFFF", action:"stopSchedule"
        }

        standardTile("refresh", "device.refresh", decoration:"flat",width:1, height:1) {
            state "default", icon:"st.secondary.refresh", action:"refresh.refresh"
            state "error", icon:"st.secondary.refresh", action:"refresh.refresh"
        }
        //No clue what a Fully Charged battery is vs a Dead Battery. Guessing from 2 AA nominal 1.5V per unit and 3.0V is adequate
		valueTile("batteryDisplay", "device.sensiBatteryVoltage", inactiveLabel:false) {
            state "default", label:'${currentValue}', unit:"V",
                backgroundColors:[
                    [value: 2.9, color: "#ff3300"],
                    [value: 3.07, color: "#ffff00"],
                    [value: 3.1, color: "#33cc33"],
                    [value: 3.5, color: "#33cc33"],
                    [value: 2900, color: "#ff3300"],
                    [value: 3075, color: "#ffff00"],
                    [value: 3100, color: "#33cc33"],
                    [value: 3500, color: "#33cc33"]
                ]
        }
        valueTile("lowBattery", "device.sensiLowPower", inactiveLabel:false, width: 2) {
            state "default", label:'LowPower: ${currentValue}', unit:"V"
        }
        main(["temperatureIcon" ])

        details(["thermostatFull","temperature", "operatingState", "fanState",
        	"mode", "fanMode", "hold",
            "heatingSetpoint", "heatDown", "heatUp",
            "coolingSetpoint", "coolDown", "coolUp", 
            "batteryDisplay","lowBattery","keypadLockout",           
            "refresh"])
    }
	preferences {
		input "holdType", "enum", title: "Hold Type", description: "When changing temperature, use Temporary (Until next transition -> default) or Permanent hold-> TURNS OFF SENSI SCHEDULED CHANGES", required: false, options:["Temporary", "Permanent"]
	}
    

}

void installed() {
    // The device refreshes every 5 minutes by default so if we miss 2 refreshes we can consider it offline
    // Using 12 minutes because in testing, device health team found that there could be "jitter"
    sendEvent(name: "checkInterval", value: 60 * 12, data: [protocol: "cloud"], displayed: false)
    
    //send initial default events to populate the tiles.
    updated()
    
}

void updated() {
	TRACE("Updating with default values in ${location.temperatureScale}")
	sendEvent(name:"thermostatMode",value:"auto")
    sendEvent(name:"thermostatFanMode",value:"auto")
    sendEvent(name:"thermostatOperatingState",value:"idle")
    sendEvent(name:"checkInterval",value:"720")
    sendEvent(name:"thermostatHoldMode", value: "off")
    sendEvent(name: "thermostatFanState", value: "off")
    
    if(location.temperatureScale == "C") {
        sendEvent(name:"temperature",value:"20",unit:location.temperatureScale)
        sendEvent(name:"humidity",value:"24",unit:location.temperatureScale)
        sendEvent(name:"heatingSetpoint",value:"20",unit:location.temperatureScale)
        sendEvent(name:"coolingSetpoint",value:"22",unit:location.temperatureScale)
        sendEvent(name:"thermostatSetpoint",value:"20",unit:location.temperatureScale)

        sendEvent(name:"thermostatStatus",value:"?")
        sendEvent(name:"maxHeatingSetpoint",value:"37",unit:location.temperatureScale)
        sendEvent(name:"minHeatingSetpoint",value:"7",unit:location.temperatureScale)
        sendEvent(name:"maxCoolingSetpoint",value:"37",unit:location.temperatureScale)
        sendEvent(name:"minCoolingSetpoint",value:"7",unit:location.temperatureScale)	    
    
    } else if (location.temperatureScale == "F") {
    
        sendEvent(name:"temperature",value:"68",unit:location.temperatureScale)
        sendEvent(name:"humidity",value:"24")
        sendEvent(name:"heatingSetpoint",value:"68",unit:location.temperatureScale)
        sendEvent(name:"coolingSetpoint",value:"75",unit:location.temperatureScale)
        sendEvent(name:"thermostatSetpoint",value:"68",unit:location.temperatureScale)

        sendEvent(name:"thermostatStatus",value:"?")
        sendEvent(name:"maxHeatingSetpoint",value:"99",unit:location.temperatureScale)
        sendEvent(name:"minHeatingSetpoint",value:"45",unit:location.temperatureScale)
        sendEvent(name:"maxCoolingSetpoint",value:"99",unit:location.temperatureScale)
        sendEvent(name:"minCoolingSetpoint",value:"45",unit:location.temperatureScale)	
    }
}
//I don't have any idea if this  is set up right now not for Sensi
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

	poll()
	log.debug "refresh completed"
}

void poll() {	
	TRACE("Refresh $device.name")
	parent.pollChild(device.deviceNetworkId)
}

def generateEvent(results) {
	if(results) {
    	//This should break down into 1 or more of the following. ON first Poll after new subscription they will all be present. On existing subscription they will only be present if 
        // they contain Updated information
        //1 Capabilities
        //2 EnvironmentControls
        //3 OperationalStatus
        //4 Product
        //5 Schedule
        //6 Settings
        try{
            results.each { name, value ->
                //TRACE("name:${name} , value: ${value}")
                if(name == "Capabilities") {
                    parseCapabilities(value)
                } else if( name == "EnvironmentControls") {
                    parseEnvironmentControls(value)
                }  else if( name == "OperationalStatus") {
                    parseOperationalStatus(value)
                }  else if( name == "Product") {
                    parseProduct(value)
                }  else if( name == "Schedule") {
                    parseSchedule(value)
                }  else if( name == "Settings") {
                    parseSettings(value)
                    
                }
          } 
        }catch(e) {
          	log.info "No new data"
        }
    }
    generateSetpointEvent()
    generateStatusEvent()
    return null
}

def parseCapabilities(capabilities) {
	checkSendEvent("capabilities",capabilities,null,null,false)
    try{   	
    	capabilities.each { name, value ->
        	TRACE("Capabilities.name: $name, value: $value")
            if(name=="HeatLimits") {
                if( location.temperatureScale == "F") {
                    checkSendEvent("maxHeatingSetpoint", value?.Max?.F,null, location.temperatureScale)
                    checkSendEvent("minHeatingSetpoint", value?.Min?.F,null,location.temperatureScale)
                } else {
                    checkSendEvent("maxHeatingSetpoint", value?.Max?.C,null, location.temperatureScale)
                    checkSendEvent("minHeatingSetpoint", value?.Min?.C,null,location.temperatureScale)
                }
    		} else if(name=="CoolLimits") {
                if( location.temperatureScale == "F") {
                    checkSendEvent("maxCoolingSetpoint", value?.Max?.F, null, location.temperatureScale)
                    checkSendEvent("minCoolingSetpoint", value?.Min?.F, null, location.temperatureScale) 
                } else {
                    checkSendEvent("maxCoolingSetpoint", value?.Max?.C,null, location.temperatureScale)
                    checkSendEvent("minCoolingSetpoint", value?.Min?.C, null, location.temperatureScale)
                }

    		}        
    	}
    TRACE("Updated Capabilities")    
    } catch (e) {
    	checkSendEvent("refresh","error",null,null,false)
    	log.warn "Failed to update capabilities $e"
    }
}
def parseEnvironmentControls(controls) {
	checkSendEvent("environmentControls",controls,null,null,false)
    try{
    	controls.each{ name, value ->
        	TRACE("Control.name: $name, value: $value")
        	
        	//Check for CoolSetPoint
            if(name == "CoolSetpoint") { 
                def cMode = device.currentValue("thermostatMode") == null ? "off" : device.currentValue("thermostatMode")
                def sensiMode = device.currentValue("sensiThermostatMode") == null ? "off" : device.currentValue("sensiThermostatMode")
                
                def sendValue = 0
                sendValue = location.temperatureScale == "C"? value.C : value.F
                checkSendEvent("coolingSetpoint",sendValue, "${device.label} Cooling set to ${sendValue}",location.temperatureScale)
                
                if( (sensiMode == "autocool") || (cMode == "cool")) {
                    checkSendEvent("thermostatSetpoint", sendValue, "${device.label} Cooling set to ${sendValue}",location.temperatureScale)
                }

            }
            if(name =="HeatSetpoint") { 
                def cMode = device.currentValue("thermostatMode") == null ? "off" : device.currentValue("thermostatMode")
                def sensiMode = device.currentValue("sensiThermostatMode") == null ? "off" : device.currentValue("sensiThermostatMode")
                
                def sendValue = location.temperatureScale == "C"? value.C :value.F
                checkSendEvent("heatingSetpoint", sendValue,"${device.label} Heating set to ${sendValue}", location.temperatureScale)
                if( (sensiMode == "autoheat") || (cMode =="heat")) {
                    checkSendEvent("thermostatSetpoint",sendValue,"${device.label} Heating set to ${sendValue}",location.temperatureScale)
                }

            }
            if(name == "FanMode") { 
                def sendValue = value.toLowerCase()
                checkSendEvent("thermostatFanMode",sendValue, "${device.name} Fan Mode ${sendValue}")
            }
            if(name == "HoldMode") { 
                //off: means Schedule is Running, Temporary means off schedule until next state on: means Hold indifinitely
                def sendValue = value.toLowerCase()
                def scheduleMode = controls?.ScheduleMode?.toLowerCase() == null ? "on" : controls?.ScheduleMode?.toLowerCase()
                TRACE("HoldMode: $sendValue scheduleMode: $scheduleMode")
                if(scheduleMode == "off") { sendValue = "on" }
                checkSendEvent("thermostatHoldMode", sendValue, "${device.label} Hold Mode ${sendValue}")
            }
            if(name == "SystemMode") { 
                def currentMode = value.toLowerCase()
                checkSendEvent("thermostatMode", currentMode, "${device.label} mode set to ${currentMode}")
            }
                       
    	}
        TRACE("Completed Parsing EnvironmentControls")
    
    } catch (e) {
    	checkSendEvent("refresh","error",null,null,false)
    	log.warn "Error $e in parseEnvironmentControls()"
    }    
}
def parseOperationalStatus(status) {
	checkSendEvent("operationalStatus",status,null,null,false)
    def currentFanMode = device.currentValue("thermostatFanMode") == null? "auto" : device.currentValue("thermostatFanMode") 
    try{
    	status.each{ name, value ->
        	TRACE("Status: $name, value: $value")
            if(name=="OperatingMode") { 
                def currentMode = value.toLowerCase()
                checkSendEvent("sensiThermostatMode",currentMode,"${device.label} Sensi mode set to ${currentMode}")
            }
            if(name=="Running") { 
                def sendValue = value.Mode.toLowerCase()
                if ((sendValue == "off") || (sendValue == "null")) {
                    sendValue = "idle"
                    if(currentFanMode == "auto") {
                        checkSendEvent("thermostatFanState","off")
                    }
                } else if (sendValue == "heat") {       	
                    sendValue = "heating"
                    checkSendEvent("thermostatFanState","on")
                } else if (sendValue == "cool") {
                    sendValue = "cooling"
                    checkSendEvent("thermostatFanState", "on")
                }
                TRACE( "Running contains Operating State: ${sendValue} and fan is ${currentFanMode} ")
                checkSendEvent("thermostatOperatingState", sendValue, "${device.label} mode set to ${sendValue}")
            }
            if(name=="Temperature") { 
                def sendValue = location.temperatureScale == "C"? value.C.toInteger() : value.F.toInteger()
                checkSendEvent("temperature", sendValue, "${device.label} mode set to ${sendValue}",location.temperatureScale,true)
            }
            if(name=="Humidity") { 
                def sendValue = value
                checkSendEvent("humidity", sendValue, "${device.label} humidity ${sendValue}","%")
            } 
            if(name=="BatteryVoltage") { 
                //def sendValue = value //In milliVolts
                def sendValue = value/1000 // in Volts
                TRACE("BV is $value and sensiBV is ${sendValue.toFloat().round(1)}")
                checkSendEvent("sensiBatteryVoltage", sendValue.toFloat().round(1), "${device.label} BatteryVoltage ${sendValue}",null,false)
                
            }
            if(name == "LowPower") {
            	def sendValue = value
                checkSendEvent("sensiLowPower", sendValue, "${device.label} LowPower ${sendValue}",null,sendValue)
                if (sendValue == true) {
                	checkSendEvent("battery", 0, "${device.label} Battery is Low", null, true)
                } else {
                	checkSendEvent("battery", 100, "${device.label} Battery is Not Low", null, false)
                }
            }
    	}    
    } catch (e) {
    	checkSendEvent("refresh","error",null,null,false)
    	log.debug "Error $e in parseOperationalStatus()"
    }    
}
def parseProduct(product) {
	checkSendEvent("product", product,null,null,false)
    //Basically not doing anything with product information
    
}
def parseSchedule(schedule) {
	checkSendEvent("schedule", schedule,null,null,false)
    //Doing nothing with schedule information-> Can be viewed in the device under the schedule attribute
}
def parseSettings(settings) {
	checkSendEvent("settings",settings,null,null,false)
    
    //Added the ability to parse keypadLockout
    try{
    	
    	settings.each{ name, value ->
        	TRACE("settings.name: $name, value: $value")
            if(name=="KeypadLockout") { 
                def curKeypadLockout = value.toLowerCase()
                checkSendEvent("keypadLockout",curKeypadLockout,"${device.label} keypad lockout to ${curKeypadLockout}")
            }
    	}    
    } catch (e) {
    	log.debug "Error $e in parseStatus()"
    }
}
def parseMode(cMode) {
	//Sensi Thermostat returns Auto as AutoHeat or AutoCool? -> I did this in winter. Not sure how AutoCool looks.

	if(cMode == "autoheat") return "auto"
    if(cMode == "autocool") return "auto"
    return cMode

}
def checkSendEvent(evtName,evtValue,evtDescription=null,evtUnit=null,evtDisplayed=true) {
	//TRACE("Updating: name: ${evtName}, value: ${evtValue}, descriptionText: ${evtDescription}, unit: ${evtUnit}")
    try {
	def checkVal = device.currentValue(evtName) == null ? " " : device.currentValue(evtName)
    def myMap = []
    if (checkVal != evtValue) {
    	if(evtDisplayed == true) {
    		log.info "Updating: name: ${evtName}, value: ${evtValue}, descriptionText: ${evtDescription}, unit: ${evtUnit}"
        }
    	if((evtDescription == null) && (evtUnit == null)) {
        	myMap = [name: evtName, value: evtValue, displayed: evtDisplayed]
        } else if (evtUnit == null) {
        	myMap = [name: evtName, value: evtValue, descriptionText: evtDescription, displayed: evtDisplayed]
        } else if (evtDescription == null) {
        	myMap = [name: evtName, value: evtValue, unit: evtUnit, displayed: evtDisplayed]
        } else {
        	myMap = [name: evtName, value: evtValue, descriptionText: evtDescription, unit: evtUnit, displayed: evtDisplayed]
        }
        if(evtName != "refresh") { sendEvent(name:"refresh",value:"normal",displayed:false) }
        //log.debug "Sending Check Event: ${myMap}"
    	sendEvent(myMap)
    } else {
    	//log.debug "${evtName}:${evtValue} is the same"
    }
    } catch (e) {
    	log.debug "checkSendEvent $evtName $evtValue $e"
    }
}

void heatUp() {
	log.debug "Heat Up"
	def mode = device.currentValue("thermostatMode")
	if (mode == "off" ) {
		heat()
	}
	def heatingSetpoint = device.currentValue("heatingSetpoint")
	
    def targetvalue = heatingSetpoint + 1

    sendEvent(name:"heatingSetpoint", "value":targetvalue, "unit":location.temperatureScale, displayed: false)

    runIn(5, setDataHeatingSetpoint,[data: [value: targetvalue], overwrite: true]) //when user click button this runIn will be overwrite
}

void heatDown() {
	log.debug "Heat Down"
	def mode = device.currentValue("thermostatMode")
	if (mode == "off" ) {
		//heat()
	}
	def heatingSetpoint = device.currentValue("heatingSetpoint")
	
    def targetvalue = heatingSetpoint - 1

    sendEvent(name:"heatingSetpoint", "value":targetvalue, "unit":location.temperatureScale, displayed: false)

    runIn(5, setDataHeatingSetpoint,[data: [value: targetvalue], overwrite: true]) //when user click button this runIn will be overwrite

}

void coolUp() {
	log.debug "Cool Up"
	def mode = device.currentValue("thermostatMode")
	if (mode == "off" ) {
		//cool()
	}
	def coolingSetpoint = device.currentValue("coolingSetpoint")
	
    def targetvalue = coolingSetpoint + 1

    sendEvent(name:"coolingSetpoint", "value":targetvalue, "unit":location.temperatureScale, displayed: false)

    runIn(5, setDataCoolingSetpoint,[data: [value: targetvalue], overwrite: true]) //when user click button this runIn will be overwrite

}

void coolDown() {
	log.debug "Cool Down"
	def mode = device.currentValue("thermostatMode")
	if (mode == "off" ) {
		cool()
	}
	def coolingSetpoint = device.currentValue("coolingSetpoint")
	
    def targetvalue = coolingSetpoint - 1

    sendEvent(name:"coolingSetpoint", "value":targetvalue, "unit":location.temperatureScale, displayed: false)

    runIn(5, setDataCoolingSetpoint,[data: [value: targetvalue], overwrite: true]) //when user click button this runIn will be overwrite

}
void setDataHeatingSetpoint(setpoint) {
	log.debug "Set heating setpoint $setpoint.value"
	setHeatingSetpoint(setpoint.value)
    //runIn(5, "poll")
}
void setDataCoolingSetpoint(setpoint) {
	log.debug "Set Cooling setpoint $setpoint.value"
	setCoolingSetpoint(setpoint.value)
    //runIn(5, "poll")
}
void setHeatingSetpoint(setpoint) {
	TRACE( "***heating setpoint $setpoint")
    def cmdString = "set"

	def heatingSetpoint = setpoint.toInteger()
    
	def coolingSetpoint = device.currentValue("coolingSetpoint")
	def deviceId = device.deviceNetworkId
	def maxHeatingSetpoint = device.currentValue("maxHeatingSetpoint")
	def minHeatingSetpoint = device.currentValue("minHeatingSetpoint")
    def thermostatMode = device.currentValue("thermostatMode")

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

    
    
    if ( thermostatMode == "auto") {
    	cmdString = "SetAutoHeat" 
        //log.debug "Is AUTHO heat ${cmdString}"
    } else if( (thermostatMode == "heat") || (thermostatMode == "aux") ) { 
    	cmdString = "SetHeat" 
        //log.debug "Is Reg Heat ${cmdString}"
    }
     log.debug "Sending heatingSetpoint: ${heatingSetpoint} mode: ${thermostatMode} string: ${cmdString}"  
     sendEvent("name":"heatingSetpoint", "value":heatingSetpoint, "unit":location.temperatureScale)
    if (parent.setTempCmd(deviceId, cmdString, heatingSetpoint)) {       
        
        //"on" means the schedule will not run
        //"temporary" means do nothing special"
        //"off" means do nothing special
        def currentHoldMode = getDataByName("thermostatHoldMode")
        def desiredHoldType = holdType == null ? "temporary" : holdType
        //log.debug "holdType is: ${holdType} des Hold type is: ${desiredHoldType}"
        if( (desiredHoldType == "Permanent") && (currentHoldMode != "on")) {
            parent.setStringCmd(deviceId, "SetScheduleMode", "Off")
            sendEvent(name:"thermostatHoldMode", value: "on")
        } else {
            sendEvent(name:"thermostatHoldMode", value: "temporary")
        }
        //log.debug "Done setHeatingSetpoint: ${heatingSetpoint}"

	} else {
		log.error "Error setHeatingSetpoint(setpoint)"
	}
    
}

void setCoolingSetpoint(setpoint) {
	TRACE( "***cooling setpoint $setpoint")
    def cmdString = "set"
	def heatingSetpoint = device.currentValue("heatingSetpoint")

    def coolingSetpoint = setpoint.toInteger()

	def deviceId = device.deviceNetworkId
	def maxCoolingSetpoint = device.currentValue("maxCoolingSetpoint")
	def minCoolingSetpoint = device.currentValue("minCoolingSetpoint")
	def thermostatMode = device.currentValue("thermostatMode")
	if (coolingSetpoint > maxCoolingSetpoint) {
		coolingSetpoint = maxCoolingSetpoint
	} else if (coolingSetpoint < minCoolingSetpoint) {
		coolingSetpoint = minCoolingSetpoint
	}
	
	//enforce limits of heatingSetpoint vs coolingSetpoint
	if (heatingSetpoint >= coolingSetpoint) {
		heatingSetpoint = coolingSetpoint
	}

//	def coolingValue = location.temperatureScale == "C"? convertCtoF(coolingSetpoint) : coolingSetpoint
//	def heatingValue = location.temperatureScale == "C"? convertCtoF(heatingSetpoint) : heatingSetpoint
	if ( thermostatMode == "auto") {
    	cmdString = "SetAutoCool" 
        //log.debug "Set Auto Cool"
    } else if( thermostatMode == "cool" ) { 
    	cmdString = "SetCool" 
        //log.debug "set Cool"
    }
	def sendHoldType = getDataByName("thermostatHoldMode")
	log.debug "Sending CoolingSetpoint: ${coolingSetpoint} mode: ${thermostatMode} string: ${cmdString}"
    sendEvent("name":"coolingSetpoint", "value":coolingSetpoint, "unit":location.temperatureScale)
	if (parent.setTempCmd(deviceId, cmdString, coolingSetpoint)) {
        //"on" means the schedule will not run
        //"temporary" means do nothing special"
        //"off" means do nothing special
        def currentHoldMode = getDataByName("thermostatHoldMode")
        def desiredHoldType = holdType == null ? "temporary" : holdType
        //log.debug "holdType is: ${holdType} des Hold type is: ${desiredHoldType}"
        if( (desiredHoldType == "Permanent") && (currentHoldMode != "on")) {
            parent.setStringCmd(deviceId, "SetScheduleMode", "Off")
            sendEvent(name:"thermostatHoldMode", value: "on")
        } else {
            sendEvent(name:"thermostatHoldMode", value: "temporary")
        }		
	} else {
		log.error "Error setCoolingSetpoint(setpoint)"
	}
}

void resumeProgram() {
	log.debug "resumeProgram() is called"
	sendEvent("name":"thermostatStatus", "value":"resuming schedule", "description":statusText, displayed: false)
	def deviceId = device.deviceNetworkId
    def currentMode = getDataByName("thermostatHoldMode")
    if (currentMode == "temporary") {
    	parent.setStringCmd(deviceId, "SetHoldMode", "Off")       
    }
	if (parent.setStringCmd(deviceId,"SetScheduleMode","On")) {
		sendEvent("name":"thermostatStatus", "value":"setpoint is updating", "description":statusText, displayed: false)
		//runIn(5, "poll")
		//log.debug "resumeProgram() is done"
	} else {
		sendEvent("name":"thermostatStatus", "value":"failed resume click refresh", "description":statusText, displayed: false)
		log.error "Error resumeProgram() check parent.resumeProgram(deviceId)"
	}

}
void stopSchedule() {
	//log.debug "stopSchedule() is called"
	sendEvent("name":"thermostatStatus", "value":"stopping schedule", "description":statusText, displayed: false)
	def deviceId = device.deviceNetworkId
	if (parent.setStringCmd(deviceId,"SetScheduleMode","Off")) {
		sendEvent("name":"thermostatStatus", "value":"setpoint is updating", "description":statusText, displayed: false)
        sendEvent(name:"thermostatHoldMode", value: "on")
		//runIn(5, "poll")
	} else {
		sendEvent("name":"thermostatStatus", "value":"failed resume click refresh", "description":statusText, displayed: false)
		log.error "Error resumeProgram() check parent.resumeProgram(deviceId)"
	}

}
def modes() {
	
	if (state.modes) {
		//log.debug "Modes = ${state.modes}"
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
	//log.debug "in switchMode"
	def currentMode = device.currentState("thermostatMode")?.value
	def lastTriedMode = state.lastTriedMode ?: currentMode ?: "off"
	def modeOrder = modes()
	def next = { modeOrder[modeOrder.indexOf(it) + 1] ?: modeOrder[0] }
	def nextMode = next(lastTriedMode)
	switchToMode(nextMode)
}

def switchToMode(nextMode) {
	//log.debug "In switchToMode = ${nextMode}"
	if (nextMode in modes()) {
    	nextMode = nextMode.toLowerCase()
		state.lastTriedMode = nextMode
		"$nextMode"()
	} else {
		log.debug("no mode method '$nextMode'")
	}
}

def switchFanMode() {
	def currentFanMode = device.currentState("thermostatFanMode")?.value
	//log.debug "switching fan from current mode: $currentFanMode"
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
	//log.debug "switching to fan mode: $nextMode"
	def returnCommand

	if(nextMode == "auto") {
		returnCommand = fanAuto()

	} else if(nextMode == "on") {
		
		returnCommand = fanOn()
		
	}

	returnCommand
}

def getDataByName(String name) {
	state[name] ?: device.getDataValue(name)
}

def setThermostatMode(String mode) {
	//log.debug "setThermostatMode($mode)"
	mode = mode.toLowerCase()
	switchToMode(mode)
}

def setThermostatFanMode(String mode) {
	//log.debug "setThermostatFanMode($mode)"
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
	//log.debug "off"
	def deviceId = device.deviceNetworkId
	if (parent.setStringCmd (deviceId,"SetSystemMode","Off")) {
		generateModeEvent("off")
        //runIn(5, "poll")
    }
	else {
		log.debug "Error setting new mode."
		def currentMode = device.currentState("thermostatMode")?.value
		//generateModeEvent(currentMode) // reset the tile back
	}
	generateSetpointEvent()
	generateStatusEvent()
}

def heat() {
	//log.debug "heat"
	def deviceId = device.deviceNetworkId
	if (parent.setStringCmd (deviceId,"SetSystemMode","Heat")){
		generateModeEvent("heat")
        //runIn(5, "poll")
    }
	else {
		log.debug "Error setting new mode."
		def currentMode = device.currentState("thermostatMode")?.value
		//generateModeEvent(currentMode) // reset the tile back
	}
	generateSetpointEvent()
	generateStatusEvent()
}

def emergencyHeat() {
	auxHeatOnly()
}
def aux() {
	auxHeatOnly()
}
def auxHeatOnly() {
	//log.debug "auxHeatOnly"
	def deviceId = device.deviceNetworkId
	if (parent.setStringCmd (deviceId,"SetSystemMode","Aux")) {
		generateModeEvent("aux")
        //runIn(5, "poll")
    }
	else {
		log.debug "Error setting new mode."
		def currentMode = device.currentState("thermostatMode")?.value
		//generateModeEvent(currentMode) // reset the tile back
	}
	generateSetpointEvent()
	generateStatusEvent()
}

def cool() {
	//log.debug "cool"
	def deviceId = device.deviceNetworkId
	if (parent.setStringCmd (deviceId,"SetSystemMode","Cool")){
		generateModeEvent("cool")
        //runIn(5, "poll")
    }
	else {
		log.debug "Error setting new mode."
		def currentMode = device.currentState("thermostatMode")?.value
		//generateModeEvent(currentMode) // reset the tile back
	}
	generateSetpointEvent()
	generateStatusEvent()
}

def auto() {
	//log.debug "auto"
	def deviceId = device.deviceNetworkId
	if (parent.setStringCmd (deviceId,"SetSystemMode","Auto")) {
		generateModeEvent("auto")
        //runIn(5, "poll")
    }
	else {
		log.debug "Error setting new mode."
		def currentMode = device.currentState("thermostatMode")?.value
		//generateModeEvent(currentMode) // reset the tile back
	}
    
	generateSetpointEvent()
	generateStatusEvent()
}

def fanOn() {
	//log.debug "fanOn"
	def cmdVal = "On"
	def deviceId = device.deviceNetworkId
	def cmdString = "SetFanMode"
	if (parent.setStringCmd( deviceId,cmdString,cmdVal)) {
		sendEvent([name: "thermostatFanMode", value: "on", descriptionText: "${device.name} sent ${cmdString} ${cmdVal}"])
	} else {
		log.debug "Error setting new mode."
		def currentFanMode = device.currentState("thermostatFanMode")?.value
		//sendEvent([name: "thermostatFanMode", value: currentFanMode, descriptionText: "${device.name} sent ${cmdString} ${cmdVal}"])
	}

}
def setKeypadLockoutOn() {
	TRACE( "setKeypadLockoutOn()")
	def cmdVal = "On"
	def deviceId = device.deviceNetworkId
	def cmdSetting = "ChangeSetting"
    def cmdString = "KeypadLockout"
	if (parent.setSettingsStringCmd( deviceId,cmdSetting,cmdString,cmdVal)) {
		sendEvent([name: "keypadLockout", value: "On", descriptionText: "${device.name} sent ${cmdSetting} ${cmdString} ${cmdVal}"])
	} else {
		log.debug "Error setting keypad lockout."
		def currentKeypadLockout = device.currentState("keypadLockout")?.value
		//sendEvent([name: "thermostatFanMode", value: currentFanMode, descriptionText: "${device.name} sent ${cmdString} ${cmdVal}"])
	}
}

def setKeypadLockoutOff() {
	TRACE( "setKeypadLockoutOff()")
	def cmdVal = "Off"
	def deviceId = device.deviceNetworkId
	def cmdSetting = "ChangeSetting"
    def cmdString = "KeypadLockout"
	if (parent.setSettingsStringCmd( deviceId,cmdSetting,cmdString,cmdVal)) {
		sendEvent([name: "keypadLockout", value: "Off", descriptionText: "${device.name} sent ${cmdSetting} ${cmdString} ${cmdVal}"])
	} else {
		log.debug "Error setting keypad lockout."
		def currentKeypadLockout = device.currentState("keypadLockout")?.value
		//sendEvent([name: "thermostatFanMode", value: currentFanMode, descriptionText: "${device.name} sent ${cmdString} ${cmdVal}"])
	}
}

def fanAuto() {
	TRACE("fanAuto()")
	def cmdVal = "Auto"
	def deviceId = device.deviceNetworkId
	def cmdString = "SetFanMode"
	if (parent.setStringCmd(deviceId,cmdString,cmdVal)) {
		sendEvent([name: "thermostatFanMode", value: "auto", descriptionText: "${device.name} sent ${cmdString} ${cmdVal}"])

	} else {
		log.debug "Error setting new mode."
		def currentFanMode = device.currentState("thermostatFanMode")?.value
		//sendEvent([name: "thermostatFanMode", value: currentFanMode, descriptionText: "${device.name} sent ${cmdString} ${cmdVal}"])
	}

    
}

def generateSetpointEvent() {
	//log.debug "Generate SetPoint Event"

	def mode = device.currentValue("thermostatMode")
    def sensiMode = device.currentValue("sensiThermostatMode")
    def operatingState = device.currentValue("thermostatOperatingState")
    def heatingSetpoint = device.currentValue("heatingSetpoint")
    def coolingSetpoint = device.currentValue("coolingSetpoint")

	TRACE( "Current Mode = ${mode} sensiMode: ${sensiMode}")
	TRACE( "Heating Setpoint = ${heatingSetpoint}")
	TRACE( "Cooling Setpoint = ${coolingSetpoint}")



	if (mode == "heat") {
		sendEvent("name":"thermostatSetpoint", "value":heatingSetpoint, "unit":location.temperatureScale)
	}
	else if (mode == "cool") {
		sendEvent("name":"thermostatSetpoint", "value":coolingSetpoint, "unit":location.temperatureScale)
	} else if (mode == "auto") {
    	if(sensiMode =="AutoHeat") {
			sendEvent("name":"thermostatSetpoint", "value":heatingSetpoint, "unit":location.temperatureScale)
        } else if(sensiMode =="AutoCool") {
        	sendEvent("name":"thermostatSetpoint", "value":coolingSetpoint, "unit":location.temperatureScale)
        }
	} else if (mode == "off") {
		sendEvent("name":"thermostatSetpoint", "value":"Off")
	} else if (mode == "aux") {
		sendEvent("name":"thermostatSetpoint", "value":heatingSetpoint, "unit":location.temperatureScale)
	}
}

void raiseSetpoint() {
	def mode = device.currentValue("thermostatMode")
    
	def targetvalue
	def maxHeatingSetpoint = device.currentValue("maxHeatingSetpoint")
	def maxCoolingSetpoint = device.currentValue("maxCoolingSetpoint")
	def sensiMode = device.currentValue("sensiThermostatMode")
	if (mode == "off" ) {
		log.warn "this mode: $mode does not allow raiseSetpoint"
	} else {
    	if(mode == "auto") {
        	//The Sensi thermostat shares if it is in Auto Heating or Auto Cooling, so a raise should be able to go off the of operating mode ? 
        	if (sensiMode =="autoheat") { mode = "heat" }
            else if (sensiMode =="autocool") { mode = "cool" }
        }

		def heatingSetpoint = device.currentValue("heatingSetpoint")
		def coolingSetpoint = device.currentValue("coolingSetpoint")
		def thermostatSetpoint = device.currentValue("thermostatSetpoint")

		targetvalue = thermostatSetpoint ? thermostatSetpoint : 0
		targetvalue = targetvalue + 1

		if ((mode == "heat" || mode == "aux") && targetvalue > maxHeatingSetpoint) {
			targetvalue = maxHeatingSetpoint
		} else if (mode == "cool" && targetvalue > maxCoolingSetpoint) {
			targetvalue = maxCoolingSetpoint
		}

		sendEvent("name":"thermostatSetpoint", "value":targetvalue, "unit":location.temperatureScale, displayed: false)
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
    def sensiMode = device.currentValue("sensiThermostatMode")
	if (mode == "off" ) {
		log.warn "this mode: $mode does not allow lowerSetpoint()"
	} else {
    	if(mode == "auto") {
        	//The Sensi thermostat shares if it is in Auto Heating or Auto Cooling, so a raise should be able to go off the of operating mode ? 
        	if (sensiMode =="autoheat") { mode = "heat" }
            else if (sensiMode =="autocool") { mode = "cool" }
        }
		def heatingSetpoint = device.currentValue("heatingSetpoint")
		def coolingSetpoint = device.currentValue("coolingSetpoint")
		def thermostatSetpoint = device.currentValue("thermostatSetpoint")

		targetvalue = thermostatSetpoint ? thermostatSetpoint : 0
		targetvalue = targetvalue - 1

		if ((mode == "heat" || mode == "aux") && targetvalue < minHeatingSetpoint) {
			targetvalue = minHeatingSetpoint
		} else if (mode == "cool" && targetvalue < minCoolingSetpoint) {
			targetvalue = minCoolingSetpoint
		}

		sendEvent("name":"thermostatSetpoint", "value":targetvalue, "unit":location.temperatureScale, displayed: false)
		log.info "In mode $mode lowerSetpoint() to $targetvalue"

		runIn(3, "alterSetpoint", [data: [value:targetvalue], overwrite: true]) //when user click button this runIn will be overwrite
	}
}

//called by raiseSetpoint() and lowerSetpoint()
void alterSetpoint(temp) {
	def deviceId = device.deviceNetworkId
	def mode = device.currentValue("thermostatMode")
	def sensiMode = device.currentValue("sensiThermostatMode")
    //"on" means the schedule will not run
    //"temporary" means do nothing special"
    //"off" means do nothing special
	def currentHoldMode = getDataByName("thermostatHoldMode")
    def desiredHoldType = holdType == null ? "temporary" : holdType
   // log.debug "holdType is: ${holdType} des Hold type is: ${desiredHoldType}"
    if( (desiredHoldType == "Permanent") && (currentHoldMode != "on")) {
    	parent.setStringCmd(deviceId, "SetScheduleMode", "Off")
        sendEvent(name:"thermostatHoldMode", value: "on")
    } else {
    	sendEvent(name:"thermostatHoldMode", value: "temporary")
    }
	if (mode == "off" ) {
		log.warn "this mode: $mode does not allow alterSetpoint"
	} else {
		def heatingSetpoint = device.currentValue("heatingSetpoint")
		def coolingSetpoint = device.currentValue("coolingSetpoint")


		def targetHeatingSetpoint
		def targetCoolingSetpoint
		def thermostatSetpoint
		def modeNum = 0
		def temperatureScaleHasChanged = false

		if (location.temperatureScale == "C") {
			if ( heatingSetpoint > 40.0 || coolingSetpoint > 40.0 ) {
				temperatureScaleHasChanged = true
			}
		} else {
			if ( heatingSetpoint < 40.0 || coolingSetpoint < 40.0 ) {
				temperatureScaleHasChanged = true
			}
		}
		def cmdString 
		//step1: check thermostatMode, enforce limits before sending request to cloud
		if ((mode == "heat") || (mode == "aux") || (sensiMode == "autoheat")){
        	modeNum = 1
        	if((mode == "heat") || (mode == "aux")) { cmdString = "SetHeat"}
            else if(sensiMode == "autoheat") { cmdString = "SetAutoHeat" }
			if (temp.value > coolingSetpoint){
				targetHeatingSetpoint = temp.value
				//targetCoolingSetpoint = temp.value
			} else {
				targetHeatingSetpoint = temp.value
				//targetCoolingSetpoint = coolingSetpoint
			}
		} else if ((mode == "cool") || (sensiMode == "autocool") ) {
        	modeNum = 2
        	if(mode == "cool") { cmdString = "SetCool" }
            else if (sensiMode == "autocool") { cmdString = "SetAutoCool" }
			//enforce limits before sending request to cloud
			if (temp.value < heatingSetpoint){
				//targetHeatingSetpoint = temp.value
				targetCoolingSetpoint = temp.value
			} else {
				//targetHeatingSetpoint = heatingSetpoint
				targetCoolingSetpoint = temp.value
			}
		}

		TRACE( "alterSetpoint >> in mode ${mode} trying to change heatingSetpoint to $targetHeatingSetpoint " +
				"coolingSetpoint to $targetCoolingSetpoint with holdType : ${holdType}")

		def sendHoldType = getDataByName("thermostatHoldMode")
		if (parent.setTempCmd(deviceId,cmdString, temp.value)) {
			sendEvent("name": "thermostatSetpoint", "value": temp.value, displayed: false)
            if(modeNum == 1) { sendEvent("name": "heatingSetpoint", "value": targetHeatingSetpoint, "unit": location.temperatureScale) }
            else if (modeNum == 2) { sendEvent("name": "coolingSetpoint", "value": targetCoolingSetpoint, "unit": location.temperatureScale) }
			log.debug "alterSetpoint in mode $mode succeed change setpoint to= ${temp.value}"
		} else {
			log.error "Error alterSetpoint()"
			if (mode == "heat" || mode == "aux" || sensiMode == "autoheat"){
				//sendEvent("name": "thermostatSetpoint", "value": heatingSetpoint.toString(), displayed: false)
			} else if (mode == "cool" || sensiMode == "autocool") {
				//sendEvent("name": "thermostatSetpoint", "value": coolingSetpoint.toString(), displayed: false)
			}
		}
    //runIn(5, "poll")    
	
	if ( temperatureScaleHasChanged )
		generateSetpointEvent()
		generateStatusEvent()
	}
}

def generateStatusEvent() {
	def mode = device.currentValue("thermostatMode")
    def operatingMode = device.currentValue("thermostatOperatingState")
	def heatingSetpoint = device.currentValue("heatingSetpoint")
	def coolingSetpoint = device.currentValue("coolingSetpoint")
	def temperature = device.currentValue("temperature")
	def statusText

//	log.debug "Generate Status Event for Mode = ${mode} in state: ${operatingMode}"
//	log.debug "Temperature = ${temperature}"
//	log.debug "Heating set point = ${heatingSetpoint}"
//	log.debug "Cooling set point = ${coolingSetpoint}"
//	log.debug "HVAC Mode = ${mode}"
	
    if(operatingMode == "heat") {
    	statusText = "Heating to ${heatingSetpoint} ${location.temperatureScale}"
    }
    else if (operatingMode == "cool") {
    	statusText = "Cooling to ${coolingSetpoint} ${location.temperatureScale}"
    }
    else if (operatingMode == "aux") {
		statusText = "Emergency Heat"
	} 
    else if (operatingMode == "off") {
    	statusText = "Idle"
    }else {
		statusText = "?"
	}

//	log.debug "Generate Status Event = ${statusText}"
	sendEvent("name":"thermostatStatus", "value":statusText, "description":statusText, displayed: true)
}

def generateActivityFeedsEvent(notificationMessage) {
	sendEvent(name: "notificationMessage", value: "$device.displayName $notificationMessage", descriptionText: "$device.displayName $notificationMessage", displayed: true)
}

def roundC (tempC) {
	return (Math.round(tempC.toDouble() * 2))/2
}

def convertFtoC (tempF) {
	return ((Math.round(((tempF - 32)*(5/9)) * 2))/2).toDouble()
}

def convertCtoF (tempC) {
	return (Math.round(tempC * (9/5)) + 32).toInteger()
}


private def TRACE(message) {
    log.debug message
}