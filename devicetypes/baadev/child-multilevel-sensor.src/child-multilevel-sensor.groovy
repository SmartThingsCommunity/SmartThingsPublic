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
            /*1*/ input "tempUnitConversion", "enum", title: "Temperature Unit Conversion - select F to C, C to F, or no conversion", description: "", defaultValue: "1", required: true, multiple: false, options:[["1":"none"], ["2":"Fahrenheit to Celsius"], ["3":"Celsius to Fahrenheit"]], displayDuringSetup: false
            /*8*/ input "atmosphericUnitConversion", "enum", title: "Atmospheric Pressure Unit Conversion - select kPa to ″Hg, ″Hg to kPa, or no conversion", description: "", defaultValue: "1", required: true, multiple: false, options:[["1":"none"], ["2":"kPa to ″Hg"], ["3":"″Hg to kPa"]], displayDuringSetup: false
            /*20*/ input "distanceUnitConversion", "enum", title: "Distance Unit Conversion", description: "", defaultValue: "1", required: true, multiple: false, options:[["1":"none"], ["2":"Centimeters to inches"], ["3":"Meters to inches"], ["4":"Feets to inches"]], displayDuringSetup: false
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

def installed() {
 	sendEvent(name: "sensType", value: "Waiting for device report")
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

def zwaveEvent(physicalgraph.zwave.commands.sensormultilevelv5.SensorMultilevelReport cmd) {
    def offsetValue = cmd.scaledSensorValue
    def scale = null
    def sensType = null
    switch (cmd.sensorType) {
    	case 1: // Air temperature
    		switch (cmd.scale) {
            	case 0: 
                	scale = "°C"
                    break
                case 1:
                	scale = "°F"
                    break
            }
            if (tempUnitConversion == "2") {
                double newValue = fahrenheitToCelsius(offsetValue)  //convert from Fahrenheit to Celsius
                offsetValue = newValue.round(2)
                scale = "°C"
            }
            if (tempUnitConversion == "3") {
                double newValue = celsiusToFahrenheit(offsetValue)  //convert from Celsius to Fahrenheit
                offsetValue = newValue.round(2)
                scale = "°F"
            }
            sensType = "Air temperature"
            break
            
        case 2: // General purpose
        	switch (cmd.scale) {
            	case 0: 
                	scale = "%"
                    break
                case 1:
                	scale = ""
                    break
            }
        	sensType = "General purpose"
        	break
            
        case 3: 
        	break
            
        case 4:
        	break
            
        case 5:
        	break
            
        case 6:
        	break
            
        case 7:
        	break
            
        case 8: // Atmospheric pressure
        	switch (cmd.scale) {
            	case 0: 
                	scale = "kPa"
                    break
                case 1:
                	scale = "″Hg"
                    break
            }
        	if (atmosphericUnitConversion == "2") {
            	double newValue = offsetValue * 0.29529980164712
                offsetValue = newValue.round(2)
                scale = "″Hg"
            }
        	if (atmosphericUnitConversion == "3") {
            	double newValue = offsetValue * 3.3864
                offsetValue = newValue.round(2)
                scale = "kPa"
            }
        	sensType = "Atmospheric pressure"
        	break
            
        case 9:
        	break
        case 10:
        	break
        case 11:
        	break
        case 20: // Distance
            switch (cmd.scale) {
                case 0: 
                scale = "m"
                break
                case 1:
                scale = "cm"
                break
                case 2:
                scale = "ft"
                break
            }
            if (distanceUnitConversion == "2") {
                double newValue = offsetValue / 2.54
                offsetValue = newValue.round(2)
                scale = "″"
            }
        	if (distanceUnitConversion == "3") {
                double newValue = (offsetValue * 100) / 2.54
                offsetValue = newValue.round(2)
                scale = "″"
            }
        	if (distanceUnitConversion == "4") {
                double newValue = offsetValue * 12
                offsetValue = newValue.round(2)
                scale = "″"   
            }
        	sensType = "Distance"

        	break
    }
    
    log.debug "cmd:${cmd}"
    sendEvent(name: "sensValue", value: "${offsetValue}${scale}")
    sendEvent(name: "sensType", value: sensType)
}













































