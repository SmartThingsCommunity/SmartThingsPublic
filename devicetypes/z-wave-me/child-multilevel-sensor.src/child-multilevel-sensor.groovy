/**
 *  Child Sensor Multilevel
 *
 *  Copyright 2018 Alexander Belov, Z-Wave.Me
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
	definition (name: "Child Multilevel Sensor", namespace: "z-wave-me", author: "Alexander Belov") {
	
		capability "Temperature Measurement"
		capability "Sensor"
		capability "Refresh"

		attribute "lastUpdated", "String"
		attribute "sensValue", "String"
		attribute "sensType", "String"
	}
	preferences {
		section("Prefs") {
			input "tempUnitConversion", "enum", title: "Temperature Unit", description: "", defaultValue: "1", required: true, multiple: false, options:[["1":"Device default"],["2":"Celsius"], ["3":"Fahrenheit"]], displayDuringSetup: false
			input "atmosphericUnitConversion", "enum", title: "Atmospheric Pressure Unit", description: "", defaultValue: "1", required: true, multiple: false, options:[["1":"Device default"],["2":"kPa"], ["3":"″Hg"]], displayDuringSetup: false
			input "distanceUnitConversion", "enum", title: "Distance Unit", description: "", defaultValue: "1", required: true, multiple: false, options:[["1":"Device default"],["2":"Meters"], ["3":"Centimeters"], ["4":"Inches"], ["5":"Feets"]], displayDuringSetup: false
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
		createEvent(descriptionText: description, isStateChange:true)
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
	double sensorValue = cmd.scaledSensorValue
	def scale = null
	def sensType = null
	
	switch (cmd.sensorType) {
		case 0x01:
		case 0x0B:
		case 0x17:
		case 0x18:
		case 0x22:
			if (cmd.sensorType == 0x01) sensType = "Air temperature"
			if (cmd.sensorType == 0x0B) sensType = "Dew point"
			if (cmd.sensorType == 0x17) sensType = "Water temperature"
			if (cmd.sensorType == 0x18) sensType = "Soil temperature"
			if (cmd.sensorType == 0x22) sensType = "Target temperature"

			if (cmd.scale == 0) scale = "°C"
			if (cmd.scale == 1) scale = "°F"
						
			if (tempUnitConversion == "2" && cmd.scale == 1) {
				sensorValue = fahrenheitToCelsius(sensorValue)
				scale = "°C"
			}
			if (tempUnitConversion == "3" && cmd.scale == 0) {
				sensorValue = celsiusToFahrenheit(sensorValue)
				scale = "°F"
			}
			break
			
		case 0x02: 
			sensType = "General purpose"
			
			if (cmd.scale == 0) scale = "%"
			if (cmd.scale == 1) scale = ""
			break
			
		case 0x03: 
			sensType = "Luminance"
			
			if (cmd.scale == 0) scale = "%"
			if (cmd.scale == 1) scale = "lx"
			break
			
		case 0x04:
			sensType = "Power"
			
			if (cmd.scale == 0) scale = "W"
			if (cmd.scale == 1) scale = "btu/h"
			break
			
		case 0x05:
			sensType = "Humidity"
			
			if (cmd.scale == 0) scale = "%"
	   		if (cmd.scale == 1) scale = "g/m³"
			break
			
		case 0x06:
			sensType = "Velocity"
			
			if (cmd.scale == 0) scale = "m/s"
	   		if (cmd.scale == 1) scale = "mph"
			break
			
		case 0x07:
			sensType = "Direction"
			
			if (sensorValue == 0) scale = ""
			if (sensorValue >= 45 && sensorValue < 135) scale = "E"
	   		if (sensorValue >= 135 && sensorValue < 225) scale = "S"
			if (sensorValue >= 225 && sensorValue < 315) scale = "w"
	   		if (sensorValue >= 315 && sensorValue <= 360 || sensorValue >= 1 && sensorValue < 45) scale = "N"
			break
			
		case 0x08:
		case 0x09:
			sensType = cmd.sensorType == 0x08 ? "Atmospheric pressure" : "Barometric pressure"
			
			if (cmd.scale == 0) scale = "kPa"
			if (cmd.scale == 1) scale = "″Hg"

			if (atmosphericUnitConversion == "3" && cmd.scale == 0) {
				sensorValue /= 3.3864
				scale = "″Hg"
			}
			if (atmosphericUnitConversion == "2" && cmd.scale == 1) {
				sensorValue *= 3.3864
				scale = "kPa"
			}
			break
			
		case 0x0A:
			sensType = "Solar radiation"

			if (cmd.scale == 0) scale = "W/m²"
			break

		case 0x0C:
		//TODO: convert
			sensType = "Rain rate"
			
			if (cmd.scale == 0) scale = "mm/h"
	   		if (cmd.scale == 1) scale = "in/h"
			break
			
		case 0x0D:
		//TODO: convert
			sensType = "Tide level"
			
			if (cmd.scale == 0) scale = "m"
	   		if (cmd.scale == 1) scale = "ft"
			break
			
		case 0x0E:
		//TODO: convert
			sensType = "Weight"
			
			if (cmd.scale == 0) scale = "kg"
	   		if (cmd.scale == 1) scale = "lb"
			break
			
		case 0x0F:
			sensType = "Voltage"
			
			if (cmd.scale == 0) scale = "V"
	   		if (cmd.scale == 1) scale = "mV"
			break
			
		case 0x10:
			sensType = "Current"
			
			if (cmd.scale == 0) scale = "A"
	   		if (cmd.scale == 1) scale = "mA"
			break
			
		case 0x11:
			sensType = "CO2-level"
			
			if (cmd.scale == 0) scale = "ppm"
			break
			
		case 0x12:
			sensType = "Air flow"
			
			if (cmd.scale == 0) scale = "m³/h"
	   		if (cmd.scale == 1) scale = "cfm"
			break
			
		case 0x13:
		//TODO: convert
			sensType = "Tank capacity"
			
			if (cmd.scale == 0) scale = "l"
	   		if (cmd.scale == 1) scale = "m³"
			if (cmd.scale == 2) scale = "gal"
			break
			
		case 0x14:
			sensType = "Distance"
			
	   		if (cmd.scale == 0) scale = "m"
			if (cmd.scale == 1) scale = "cm"
			if (cmd.scale == 2) scale = "ft"

			if (distanceUnitConversion == "2") {
				scale = "″"
				if (cmd.scale == 1) sensorValue /= 100
				if (cmd.scale == 2) sensorValue /= 0.3048
			}
			if (distanceUnitConversion == "3") {
				scale = "″"
				if (cmd.scale == 0) sensorValue *= 100
				if (cmd.scale == 2) sensorValue *= 30.48
			}
			if (distanceUnitConversion == "4") {
				scale = "″"
				if (cmd.scale == 0) sensorValue /= 254
				if (cmd.scale == 1) sensorValue /= 2.54
				if (cmd.scale == 2) sensorValue *= 12
			}
			if (distanceUnitConversion == "5") {
				scale = "″"
				if (cmd.scale == 0) sensorValue /= 0.3048
				if (cmd.scale == 1) sensorValue /= 30.48
			}
			
			break  
			
		case 0x15:
			sensType = "Angle position"
			
			if (cmd.scale == 0) scale = "%"
	   		if (cmd.scale == 1) scale = ""
			if (cmd.scale == 2) scale = ""
			break
			
		case 0x16:
			sensType = "Rotation"
			
			if (cmd.scale == 0) scale = "rpm"
	   		if (cmd.scale == 1) scale = "Hz"
			break
						
		case 0x19:
			sensType = "Seismic intensity"
			
			if (cmd.scale == 0) scale = "М"
	   		if (cmd.scale == 1) scale = "EMS"
			if (cmd.scale == 2) scale = "L"
	   		if (cmd.scale == 3) scale = "S"
			break   
			
		case 0x1A:
			sensType = "Seismic magnitude"
			
			if (cmd.scale == 0) scale = "Local"
	   		if (cmd.scale == 1) scale = "Moment"
			if (cmd.scale == 2) scale = "Surface wave "
			if (cmd.scale == 3) scale = "Body wave"
			break
			
		case 0x1B:
			sensType = "Ultraviolet"
			
			if (cmd.scale == 0) scale = "UV"
			break
			
		case 0x1C:
			sensType = "Electrical resistivity"
			
			if (cmd.scale == 0) scale = "Ω"
			break  
			
		case 0x1D:
			sensType = "Electrical conductivity"
			
			if (cmd.scale == 0) scale = "S/m"
			break 
			
		case 0x1E:
			sensType = "Loudness"
			
			if (cmd.scale == 0) scale = "dB"
	   		if (cmd.scale == 1) scale = "dBA"
			break
			
		case 0x1F:
			sensType = "Moisture"
			
			if (cmd.scale == 0) scale = "%"
	   		if (cmd.scale == 1) scale = "m³/m³"
			if (cmd.scale == 2) scale = "kΩ"
			if (cmd.scale == 3) scale = "aw"
			break
			
	////////////////////////////////////////////////////////////////
	/* Next cases don't supported by ST (needed CC V6 and higher) */
	////////////////////////////////////////////////////////////////
	
		case 0x20:
			sensType = "Frequency"
			
			if (cmd.scale == 0) scale = "Hz"
	   		if (cmd.scale == 1) scale = "kHz"
			break	  
			
		case 0x21:
			sensType = "Time"
			
			if (cmd.scale == 0) scale = "s"
			break			  
			
		 case 0x23:
			sensType = "Particulate Matter 2.5"
			
			if (cmd.scale == 0) scale = "mol/m³"
			if (cmd.scale == 1) scale = "µg/m³"
			break 
			
		case 0x24:
			sensType = "CH2O-level"
			
			if (cmd.scale == 0) scale = "mol/m³"
			break  
			
		 case 0x25:
			sensType = "Radon concentration"
			
			if (cmd.scale == 0) scale = "bq/m³"
			if (cmd.scale == 1) scale = "oCi/I"
			break  
			
		 case 0x26:
			sensType = "CH4 density"
			
			if (cmd.scale == 0) scale = "mol/m³"
			break  
			
		 case 0x27:
			sensType = "Volatile Organic Compound level"
			
			if (cmd.scale == 0) scale = "mol/m³"
			if (cmd.scale == 1) scale = "ppm"
			break  
			
		 case 0x28:
			sensType = "CO level"
			
			if (cmd.scale == 0) scale = "mol/m³"
			if (cmd.scale == 1) scale = "ppm"
			break  
			
		 case 0x29:
			sensType = "Soil humidity"
			
			if (cmd.scale == 0) scale = "%"
			break  
			
		 case 0x2A:
			sensType = "Soil reactivity"
			
			if (cmd.scale == 0) scale = "pH"
			break  
			
		 case 0x2B:
			sensType = "Soil salinity"
			
			if (cmd.scale == 0) scale = "mol/m³"
			break  
			
		 case 0x2C:
			sensType = "Heart rate"
			
			if (cmd.scale == 0) scale = "bpm"
			break  
			
		 case 0x2A:
			sensType = "Soil reactivity"
			
			if (cmd.scale == 0) scale = "pH"
			break
	}
	
	log.debug "cmd:${cmd}"
	sendEvent(name: "sensValue", value: "${sensorValue.round(2)}${scale}")
	sendEvent(name: "sensType", value: sensType)
}