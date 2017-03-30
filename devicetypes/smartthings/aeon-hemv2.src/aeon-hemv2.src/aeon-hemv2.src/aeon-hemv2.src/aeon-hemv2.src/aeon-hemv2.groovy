/**
 *  Aeon HEMv2
 *
 *  Copyright 2014 Barry A. Burke
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
 *  Aeon Home Energy Meter v2 (US)
 *
 *  Author: Barry A. Burke
 *
 *  Genesys: Based off of Aeon Smart Meter Code sample provided by SmartThings (2013-05-30). Built on US model
 *			 may also work on international versions (currently reports total values only)
 *
 *  History:
 * 		
 *	2014-06-13: Massive OverHaul
 *				- Fixed Configuration (original had byte order of bitstrings backwards
 *				- Increased reporting frequency to 10s - note that values won't report unless they change
 *				  (they will also report if they exceed limits defined in the settings - currently just using
 *				  the defaults).
 *				- Added support for Volts & Amps monitoring (was only Power and Energy)
 *				- Added flexible tile display. Currently only used to show High and Low values since last
 *				  reset (with time stamps). 
 *				- All tiles are attributes, so that their values are preserved when you're not 'watching' the
 *				  meter display
 *				- Values are formatted to Strings in zwaveEvent parser so that we don't lose decimal values 
 *				  in the tile label display conversion
 *				- Updated fingerprint to match Aeon Home Energy Monitor v2 deviceId & clusters
 *				- Added colors for Watts and Amps display
 * 				- Changed time format to 24 hour
 *	2014-06-17: Tile Tweaks
 *				- Reworked "decorations:" - current values are no longer "flat"
 *				- Added colors to current Watts (0-18000) & Amps (0-150)
 *				- Changed all colors to use same blue-green-orange-red as standard ST temperature guages
 *	2014-06-18: Cost calculations
 *				- Added $/kWh preference
 *				
 *
 */
metadata {
	// Automatically generated. Make future change here.
	definition (name: "Aeon HEMv2", namespace: "smartthings", author: "Barry A. Burke") {
    
		capability "Energy Meter"
		capability "Power Meter"
		capability "Configuration"
		capability "Sensor"
        
        attribute "energy", "string"
        attribute "power", "string"
        attribute "volts", "string"
        attribute "amps", "string"
        
        attribute "energyDisp", "string"
        attribute "energyOne", "string"
        attribute "energyTwo", "string"
        
        attribute "powerDisp", "string"
        attribute "powerOne", "string"
        attribute "powerTwo", "string"
        
        attribute "voltsDisp", "string"
        attribute "voltsOne", "string"
        attribute "voltsTwo", "string"
        
        attribute "ampsDisp", "string"
        attribute "ampsOne", "string"
        attribute "ampsTwo", "string"        
        
		command "reset"
        command "configure"
        
// v1		fingerprint deviceId: "0x2101", inClusters: " 0x70,0x31,0x72,0x86,0x32,0x80,0x85,0x60"

		fingerprint deviceId: "0x3101", inClusters: "0x70,0x32,0x60,0x85,0x56,0x72,0x86"
	}

	// simulator metadata
	simulator {
		for (int i = 0; i <= 10000; i += 1000) {
			status "power  ${i} W": new physicalgraph.zwave.Zwave().meterV1.meterReport(
				scaledMeterValue: i, precision: 3, meterType: 33, scale: 2, size: 4).incomingMessage()
		}
		for (int i = 0; i <= 100; i += 10) {
			status "energy  ${i} kWh": new physicalgraph.zwave.Zwave().meterV1.meterReport(
				scaledMeterValue: i, precision: 3, meterType: 33, scale: 0, size: 4).incomingMessage()
		}
        // TODO: Add data feeds for Volts and Amps
	}

	// tile definitions
	tiles {
    
    // Watts row

		valueTile("powerDisp", "device.powerDisp" /*, decoration: "flat" */) {
			state "default", label:'${currentValue}', 
            	foregroundColors:[[value: 1, color: "#000000"],[value: 10000, color: "#ffffff"]], foregroundColor: "#000000",
                backgroundColors:[
				[value: "0 Watts", 		color: "#153591"],
				[value: "3000 Watts", 	color: "#1e9cbb"],
				[value: "6000 Watts", 	color: "#90d2a7"],
				[value: "9000 Watts", 	color: "#44b621"],
				[value: "12000 Watts", 	color: "#f1d801"],
				[value: "15000 Watts", 	color: "#d04e00"], 
				[value: "18000 Watts", 	color: "#bc2323"]                
			]
		}
        valueTile("powerOne", "device.powerOne", decoration: "flat") {
        	state "default", label:'${currentValue}'
        }
        valueTile("powerTwo", "device.powerTwo", decoration: "flat") {
        	state "default", label:'${currentValue}'
        }

	// Power row
    
		valueTile("energyDisp", "device.energyDisp", decoration: "flat") {
			state "default", label: '${currentValue}'

		}
        valueTile("energyOne", "device.energyOne", decoration: "flat") {
        	state "default", label: '${currentValue}'
        }        
        valueTile("energyTwo", "device.energyTwo", decoration: "flat") {
        	state "default", label: '${currentValue}'
        }
        
    
    // Volts row
    
        valueTile("voltsDisp", "device.voltsDisp" /*, decoration: "flat"*/) {
        	state "default", label: '${currentValue}', backgroundColors:[
            	[value: "115.6 Volts", 	color: "#bc2323"],
                [value: "117.8 Volts", 	color: "#D04E00"],
                [value: "120.0 Volts", 	color: "#44B621"],
                [value: "122.2 Volts", 	color: "#D04E00"],
                [value: "124.4 Volts", 	color: "#bc2323"]
            ]
        }
        valueTile("voltsOne", "device.voltsOne", decoration: "flat") {
        	state "default", label:'${currentValue}'
        }
        valueTile("voltsTwo", "device.voltsTwo", decoration: "flat") {
        	state "default", label:'${currentValue}'
        }
    
    // Amps row
    
        valueTile("ampsDisp", "device.ampsDisp" /*, decoration: "flat"*/) {
        	state "default", label: '${currentValue}' , foregroundColor: "#000000", color: "#000000", backgroundColors:[
				[value: "0 Amps", 	color: "#153591"],
				[value: "25 Amps", 	color: "#1e9cbb"],
				[value: "50 Amps", 	color: "#90d2a7"],
				[value: "75 Amps", 	color: "#44b621"],
				[value: "100 Amps", color: "#f1d801"],
				[value: "125 Amps", color: "#d04e00"], 
				[value: "150 Amps", color: "#bc2323"]
			] 
        }
        valueTile("ampsOne", "device.ampsOne", decoration: "flat") {
        	state "default", label:'${currentValue}'
        }
        valueTile("ampsTwo", "device.ampsTwo", decoration: "flat") {
        	state "default", label:'${currentValue}'
        }
    
    // Controls row
    
		standardTile("reset", "device.energy", inactiveLabel: false, decoration: "flat") {
			state "default", label:'reset kWh', action:"reset"
		}
		standardTile("refresh", "device.power", inactiveLabel: false, decoration: "flat") {
			state "default", label:'', action:"refresh", icon:"st.secondary.refresh"
		}
		standardTile("configure", "device.power", inactiveLabel: false, decoration: "flat") {
			state "configure", label:'', action:"configuration.configure", icon:"st.secondary.configure"
		}

// TODO: Add configurable delay button - Cycle through 10s, 30s, 1m, 5m, 60m, off?

		main (["powerDisp","energyDisp","ampsDisp","voltsDisp"])
		details(["energyOne","energyDisp","energyTwo","powerOne","powerDisp","powerTwo","ampsOne","ampsDisp","ampsTwo","voltsOne","voltsDisp","voltsTwo","reset","refresh", "configure"])
	}
    preferences {
    	input "kWhCost", "string", title: "\$/kWh (0.16)", defaultValue: "0.16" as String
    }
}


def parse(String description) {
//	log.debug "Parse received ${description}"
	def result = null
	def cmd = zwave.parse(description, [0x31: 1, 0x32: 1, 0x60: 3])
	if (cmd) {
		result = createEvent(zwaveEvent(cmd))
	}
	if (result != null) log.debug "Parse returned ${result?.descriptionText}" 
	return result
}

def zwaveEvent(physicalgraph.zwave.commands.meterv1.MeterReport cmd) {
//	log.debug "zwaveEvent received ${cmd}"
    
    def dispValue
    def newValue
	def timeString = new Date().format("H:mm:ss", location.timeZone)
    
    if (cmd.meterType == 33) {
		if (cmd.scale == 0) {
        	newValue = cmd.scaledMeterValue
        	if (newValue != state.energyValue) {
    			dispValue = String.format("%5.2f",newValue)+"\nkWh"
                sendEvent(name: "energyDisp", value: dispValue as String, unit: "")
                state.energyValue = newValue
                BigDecimal costDecimal = newValue * ( kWhCost as BigDecimal)
                def costDisplay = String.format("%5.2f",costDecimal)
                sendEvent(name: "energyTwo", value: "Cost\n\$${costDisplay}", unit: "")
                [name: "energy", value: newValue, unit: "kWh"]
            }
		} else if (cmd.scale == 1) {
            newValue = cmd.scaledMeterValue
            if (newValue != state.energyValue) {
    			dispValue = String.format("%5.2f",newValue)+"\nkVAh"
                sendEvent(name: "energyDisp", value: dispValue as String, unit: "")
                state.energyValue = newValue
				[name: "energy", value: newValue, unit: "kVAh"]
            }
		}
		else if (cmd.scale==2) {				
        	newValue = Math.round( cmd.scaledMeterValue )		// really not worth the hassle to show decimals for Watts
        	if (newValue != state.powerValue) {
    			dispValue = newValue+"\nWatts"
                sendEvent(name: "powerDisp", value: dispValue as String, unit: "")
                
                if ((newValue < state.powerLow) || (state.powerLow == null)) {
                	dispValue = newValue+"\n"+timeString
                	sendEvent(name: "powerOne", value: dispValue as String, unit: "")
                    state.powerLow = newValue
                }
                if (newValue > state.powerHigh) {
                	dispValue = newValue+"\n"+timeString
					sendEvent(name: "powerTwo", value: dispValue as String, unit: "")
                    state.powerHigh = newValue
                }
                state.powerValue = newValue
                [name: "power", value: newValue, unit: "W"]
            }
		}
 	}
    else if (cmd.meterType == 161) {
    	if (cmd.scale == 0) {
        	newValue = cmd.scaledMeterValue
        	if (newValue != state.voltsValue) {
    			dispValue = String.format("%5.2f", newValue)+"\nVolts"
                sendEvent(name: "voltsDisp", value: dispValue as String, unit: "")

                if ((newValue < state.voltsLow) || (state.voltsLow == null)) {
                	dispValue = String.format("%5.2f", newValue)+"\n"+timeString
                	sendEvent(name: "voltsOne", value: dispValue as String, unit: "")                   
                    state.voltsLow = newValue
                }
                if (newValue > state.voltsHigh) {
                    dispValue = String.format("%5.2f", newValue)+"\n"+timeString
                	sendEvent(name: "voltsTwo", value: dispValue as String, unit: "")                    
                    state.voltsHigh = newValue
                }                
                state.voltsValue = newValue
				[name: "volts", value: newValue, unit: "V"]
            }
        }
        else if (cmd.scale==1) {
        	newValue = cmd.scaledMeterValue
        	if (newValue != state.ampsValue) {
    			dispValue = String.format("%5.2f", newValue)+"\nAmps"
                sendEvent(name: "ampsDisp", value: dispValue as String, unit: "")
                
                if ((newValue < state.ampsLow) || (state.ampsLow == null)) {
                	dispValue = String.format("%5.2f", newValue)+"\n"+timeString
                	sendEvent(name: "ampsOne", value: dispValue as String, unit: "")
                    state.ampsLow = newValue
                }
                if (newValue > state.ampsHigh) {
                	dispValue = String.format("%5.2f", newValue)+"\n"+timeString
                	sendEvent(name: "ampsTwo", value: dispValue as String, unit: "")
                    state.ampsHigh = newValue
                }                
                state.ampsValue = newValue
				[name: "amps", value: newValue, unit: "A"]
            }
        }
    }           
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
	// Handles all Z-Wave commands we aren't interested in
    log.debug "Unhandled event ${cmd}"
	[:]
}

def refresh() {
	delayBetween([
		zwave.meterV2.meterGet(scale: 0).format(),
		zwave.meterV2.meterGet(scale: 2).format()
	])
}
    
def reset() {
	log.debug "${device.name} reset"

    state.energyHigh = null
    state.energyLow = null
    state.powerHigh = 0
    state.powerLow = null
    state.ampsHigh = 0
    state.ampsLow = null
    state.voltsHigh = 0
    state.voltsLow = null
    
    def dateString = new Date().format("MM/dd/YY", location.timeZone)
    def timeString = new Date().format("H:mm:ss", location.timeZone)
    sendEvent(name: "energyOne", value: "Since\n"+dateString+"\n"+timeString, unit: "")
    sendEvent(name: "powerOne", value: "", unit: "")    
    sendEvent(name: "voltsOne", value: "", unit: "")
    sendEvent(name: "ampsOne", value: "", unit: "")
    sendEvent(name: "ampsDisp", value: "", unit: "")
    sendEvent(name: "voltsDisp", value: "", unit: "")
    sendEvent(name: "powerDisp", value: "", unit: "")    
	sendEvent(name: "energyDisp", value: "", unit: "")
    sendEvent(name: "energyTwo", value: "Cost\n--", unit: "")
    sendEvent(name: "powerTwo", value: "", unit: "")    
    sendEvent(name: "voltsTwo", value: "", unit: "")
    sendEvent(name: "ampsTwo", value: "", unit: "")
    
//    unschedule( "reset" )								// now scheduled by FixerUpper
//    schedule("0 1 0 * * ?", "reset" )					// Daily at 11:59pm
    
// No V1 available
	def cmd = delayBetween( [
		zwave.meterV2.meterReset().format(),
		zwave.meterV2.meterGet(scale: 0).format()
	])
    
    cmd
}

def configure() {
	// TODO: Turn on reporting for each leg of power - display as alternate view (Currently those values are
    //		 returned as zwaveEvents...they probably aren't implemented in the core Meter device yet.

	def cmd = delayBetween([
		zwave.configurationV1.configurationSet(parameterNumber: 3, size: 1, scaledConfigurationValue: 1).format(),		// Enable selective reporting
		zwave.configurationV1.configurationSet(parameterNumber: 4, size: 2, scaledConfigurationValue: 50).format(),		// Don't send unless watts have increased by 50
        zwave.configurationV1.configurationSet(parameterNumber: 8, size: 2, scaledConfigurationValue: 10).format(),		// Or by 10% (these 3 are the default values
		zwave.configurationV1.configurationSet(parameterNumber: 101, size: 4, scaledConfigurationValue: 10).format(),   // Average Watts & Amps
		zwave.configurationV1.configurationSet(parameterNumber: 111, size: 4, scaledConfigurationValue: 30).format(), 	// Every 30 Seconds
		zwave.configurationV1.configurationSet(parameterNumber: 102, size: 4, scaledConfigurationValue: 4).format(),   	// Average Voltage
		zwave.configurationV1.configurationSet(parameterNumber: 112, size: 4, scaledConfigurationValue: 150).format(), 	// every 2.5 minute
		zwave.configurationV1.configurationSet(parameterNumber: 103, size: 4, scaledConfigurationValue: 1).format(),	// Total kWh (cumulative)
		zwave.configurationV1.configurationSet(parameterNumber: 113, size: 4, scaledConfigurationValue: 300).format() 	// every 5 minutes
	])
	log.debug cmd

	cmd
}


