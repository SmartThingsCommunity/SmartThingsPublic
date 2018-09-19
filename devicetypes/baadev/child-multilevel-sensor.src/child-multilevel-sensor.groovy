/**
 *  altZunoHandler
 *
 *  Copyright 2018 Alexander Belov
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
metadata {
	definition (name: "Child Multilevel Sensor", namespace: "baadev", author: "Alexander Belov") {		
    
    	capability "Temperature Measurement"
    	capability "Sensor"
        capability "Refresh"

        attribute "lastUpdated", "String"
        attribute "sensValue", "String"
        attribute "sensType", "String"
	}
    preferences {
        section("Prefs") {
            /* 1 */ input "tempUnitConversion", "enum", title: "Temperature Unit Conversion - select F to C, C to F, or no conversion", description: "", defaultValue: "1", required: true, multiple: false, options:[["1":"none"], ["2":"Fahrenheit to Celsius"], ["3":"Celsius to Fahrenheit"]], displayDuringSetup: false
            /* 8 *//* 9 */ input "atmosphericUnitConversion", "enum", title: "Atmospheric Pressure Unit Conversion - select kPa to ″Hg, ″Hg to kPa, or no conversion", description: "", defaultValue: "1", required: true, multiple: false, options:[["1":"none"], ["2":"kPa to ″Hg"], ["3":"″Hg to kPa"]], displayDuringSetup: false
            /* 20 */ input "distanceUnitConversion", "enum", title: "Distance Unit Conversion", description: "", defaultValue: "1", required: true, multiple: false, options:[["1":"none"], ["2":"Centimeters to inches"], ["3":"Meters to inches"], ["4":"Feets to inches"]], displayDuringSetup: false
        }
    }
	tiles(scale: 2) {
        standardTile("logo", "device.logo", inactiveLabel: true, decoration: "flat", width: 1, height: 1) {
            state "default", label:'', icon: "http://cdn.device-icons.smartthings.com/Weather/weather2-icn@2x.png"
        }
        valueTile("lastUpdated", "device.lastUpdated", decoration: "flat", width: 5, height: 1) {
        	state "default", label:'Last updated ${currentValue}'
        }
		multiAttributeTile(name: "sensValue", type: "generic", width: 6, height: 4, canChangeIcon: true) {
			tileAttribute("device.sensValue", key: "PRIMARY_CONTROL") {
				attributeState("sensValue", label: '${currentValue}', defaultState: true, backgroundColor: "#44b621")
			}
            tileAttribute("device.sensType", key: "SECONDARY_CONTROL") {
            	attributeState("sensType", label: '${currentValue}')
            }
		}
    }
}

def parse(def description) {
    def cmd = zwave.parse(description)
    
	if (description.startsWith("Err")) {
		result = createEvent(descriptionText: description, isStateChange:true)
	} else if (description != "updated") {
        zwaveEvent(cmd)
        
        def nowDay = new Date().format("MMM dd", location.timeZone)
    	def nowTime = new Date().format("h:mm a", location.timeZone)
    	sendEvent(name: "lastUpdated", value: nowDay + " at " + nowTime, displayed: false)
    }
}

def installed() {
 	sendEvent(name: "sensType", value: "Waiting for device report")
}

def zwaveEvent(physicalgraph.zwave.commands.sensormultilevelv5.SensorMultilevelReport cmd) {
    double offsetValue = cmd.scaledSensorValue
    def scale = null
    def sensType = null
    switch (cmd.sensorType) {
    	case 1: 
        	sensType = "Air temperature"
            
        	if (cmd.scale == 0) scale = "°C"
            if (cmd.scale == 1) scale = "°F"
    		
            if (tempUnitConversion == "2") {
                offsetValue = fahrenheitToCelsius(offsetValue)
                scale = "°C"
            }
            if (tempUnitConversion == "3") {
                offsetValue = celsiusToFahrenheit(offsetValue)
                scale = "°F"
            }
            break
            
        case 2: 
        	sensType = "General purpose"
            
        	if (cmd.scale == 0) scale = "%"
            if (cmd.scale == 1) scale = ""
        	break
            
        case 3: 
        	sensType = "Luminance"
            
			if (cmd.scale == 0) scale = "%"
			if (cmd.scale == 1) scale = "lx"
        	break
            
        case 4:
        	sensType = "Power"
            
            if (cmd.scale == 0) scale = "W"
        	if (cmd.scale == 1) scale = "btu/h"
        	break
            
        case 5:
        	sensType = "Humidity"
            
            if (cmd.scale == 0) scale = "%"
       		if (cmd.scale == 1) scale = "g/m³"
        	break
            
        case 6:
        	sensType = "Velocity"
            
        	if (cmd.scale == 0) scale = "m/s"
       		if (cmd.scale == 1) scale = "mph"
        	break
            
        case 7:
        	sensType = "Direction"
            
            if (offsetValue == 0) scale = ""
        	if (offsetValue >= 45 && offsetValue < 135) scale = "E"
       		if (offsetValue >= 135 && offsetValue < 225) scale = "S"
        	if (offsetValue >= 225 && offsetValue < 315) scale = "w"
       		if (offsetValue >= 315 && offsetValue <= 360 || offsetValue >= 1 && offsetValue < 45) scale = "N"
        	break
            
        case 8: 
        	sensType = "Atmospheric pressure"
            
        	if (cmd.scale == 0) scale = "kPa"
            if (cmd.scale == 1) scale = "″Hg"

        	if (atmosphericUnitConversion == "2") {
                offsetValue = offsetValue * 0.29529980164712
                scale = "″Hg"
            }
        	if (atmosphericUnitConversion == "3") {
                offsetValue = offsetValue * 3.3864
                scale = "kPa"
            }
        	break
            
        case 9:
            sensType = "Barometric pressure"

        	if (cmd.scale == 0) scale = "kPa"
            if (cmd.scale == 1) scale = "″Hg"
        	
            if (atmosphericUnitConversion == "2") {
                offsetValue = offsetValue * 0.29529980164712
                scale = "″Hg"
            }
        	if (atmosphericUnitConversion == "3") {
                offsetValue = offsetValue * 3.3864
                scale = "kPa"
            }
        	break
            
        case 10:
        	sensType = "Solar radiation"

        	if (cmd.scale == 0) scale = "W/m²"
        	break
            
        case 11:
        	sensType = "Dew point"
            
        	if (cmd.scale == 0) scale = "C"
       		if (cmd.scale == 1) scale = "F"
        	break
            
        case 12:
        	sensType = "Rain rate"
            
        	if (cmd.scale == 0) scale = "mm/h"
       		if (cmd.scale == 1) scale = "in/h"
        	break
            
        case 13:
        	sensType = "Tide level"
            
        	if (cmd.scale == 0) scale = "m"
       		if (cmd.scale == 1) scale = "ft"
        	break
            
        case 14:
        	sensType = "Weight"
            
        	if (cmd.scale == 0) scale = "kg"
       		if (cmd.scale == 1) scale = "lb"
        	break
            
        case 15:
        	sensType = "Voltage"
            
        	if (cmd.scale == 0) scale = "V"
       		if (cmd.scale == 1) scale = "mV"
        	break
            
        case 16:
        	sensType = "Current"
            
        	if (cmd.scale == 0) scale = "A"
       		if (cmd.scale == 1) scale = "mA"
        	break
            
        case 17:
        	sensType = "CO2-level"
            
        	if (cmd.scale == 0) scale = "ppm"
        	break
            
        case 18:
        	sensType = "Air flow"
            
        	if (cmd.scale == 0) scale = "m³/h"
       		if (cmd.scale == 1) scale = "cfm"
        	break
            
        case 19:
        	sensType = "Tank capacity"
            
        	if (cmd.scale == 0) scale = "l"
       		if (cmd.scale == 1) scale = "m³"
            if (cmd.scale == 1) scale = "gal"
        	break
            
        case 20:
	        sensType = "Distance"
            
       		if (cmd.scale == 0) scale = "m"
	        if (cmd.scale == 1) scale = "cm"
            if (cmd.scale == 2) scale = "ft"

			if (distanceUnitConversion == "2") {
                offsetValue = offsetValue / 2.54
                scale = "″"
            }
        	if (distanceUnitConversion == "3") {
                offsetValue = (offsetValue * 100) / 2.54
                scale = "″"
            }
        	if (distanceUnitConversion == "4") {
                offsetValue = offsetValue * 12
                scale = "″"   
            }
        	break
    }
    
    log.debug "cmd:${cmd}"
    sendEvent(name: "sensValue", value: "${offsetValue.round(2)}${scale}")
    sendEvent(name: "sensType", value: sensType)
}













































