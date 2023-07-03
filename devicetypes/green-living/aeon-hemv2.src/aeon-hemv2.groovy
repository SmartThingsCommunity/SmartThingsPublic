/**
 *  Aeon HEMv2+
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
 *  Contributors: Brock Haymond: UI updates
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
 *	2014-09-07:	Bug fix & Cleanup
 *				- Fixed "Unexpected Error" on Refresh tile - (added Refresh Capability)
 *				- Cleaned up low values - reset to ridiculously high value instead of null
 *				- Added poll() command/capability (just does a refresh)
 * 	2014-09-19: GUI Tweaks, HEM v1 alterations (from Brock Haymond)
 *				- Reworked all tiles for look, color, text formatting, & readability
 *	2014-09-20: Added HEMv1 Battery reporting (from Brock Haymond)
 *	2014-11-06: Added alternate display of L2 and L2 values instead of Low/High, based on version by Jayant Jhamb
 *  2014-11-11: Massive overhaul completed (see GitHub comments for specifics)
 * 				- 
 */
metadata {
	// Automatically generated. Make future change here.
	definition (
		name: 		"Aeon HEMv2+", 
		namespace: 	"Green Living",
		category: 	"Green Living",
		author: 	"Barry A. Burke"
	) 
	{
    	capability "Energy Meter"
		capability "Power Meter"
		capability "Configuration"
		capability "Sensor"
        capability "Refresh"
        capability "Polling"
        capability "Battery"
        
        attribute "energy", "string"
        attribute "power", "string"
        attribute "volts", "string"
        attribute "voltage", "string"		// We'll deliver both, since the correct one is not defined anywhere
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
        command "refresh"
        command "poll"
        command "toggleDisplay"
        
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
		valueTile("powerDisp", "device.powerDisp") {
			state (
				"default", 
				label:'${currentValue} Watts', 
            	foregroundColors:[
            		[value: 1, color: "#000000"],
            		[value: 10000, color: "#ffffff"]
            	], 
            	foregroundColor: "#000000",
                backgroundColors:[
					[value: "0", 		color: "#153591"],
					[value: "3000", 	color: "#1e9cbb"],
					[value: "6000", 	color: "#90d2a7"],
					[value: "9000", 	color: "#44b621"],
					[value: "12000", 	color: "#f1d801"],
					[value: "15000", 	color: "#d04e00"], 
					[value: "18000", 	color: "#bc2323"]
					
				/* For low-wattage homes, use these values
					[value: "0", color: "#153591"],
					[value: "500", color: "#1e9cbb"],
					[value: "1000", color: "#90d2a7"],
					[value: "1500", color: "#44b621"],
					[value: "2000", color: "#f1d801"],
					[value: "2500", color: "#d04e00"],
					[value: "3000", color: "#bc2323"]
				*/
				]
			)
		}
        valueTile("powerOne", "device.powerOne") {
        	state(
        		"default", 
        		label:'${currentValue} Watts', 
            	foregroundColors:[
            		[value: 1, color: "#000000"],
            		[value: 10000, color: "#ffffff"]
            	], 
            	foregroundColor: "#000000",
                backgroundColors:[
					[value: "0", 		color: "#153591"],
					[value: "3000", 	color: "#1e9cbb"],
					[value: "6000", 	color: "#90d2a7"],
					[value: "9000", 	color: "#44b621"],
					[value: "12000", 	color: "#f1d801"],
					[value: "15000", 	color: "#d04e00"], 
					[value: "18000", 	color: "#bc2323"]
					
				/* For low-wattage homes, use these values
					[value: "0", color: "#153591"],
					[value: "500", color: "#1e9cbb"],
					[value: "1000", color: "#90d2a7"],
					[value: "1500", color: "#44b621"],
					[value: "2000", color: "#f1d801"],
					[value: "2500", color: "#d04e00"],
					[value: "3000", color: "#bc2323"]
				*/
				]
			)
        }
        valueTile("powerTwo", "device.powerTwo") {
        	state(
        		"default", 
        		label:'${currentValue} Watts', 
            	foregroundColors:[
            		[value: 1, color: "#000000"],
            		[value: 10000, color: "#ffffff"]
            	], 
            	foregroundColor: "#000000",
                backgroundColors:[
					[value: "0", 		color: "#153591"],
					[value: "3000", 	color: "#1e9cbb"],
					[value: "6000", 	color: "#90d2a7"],
					[value: "9000", 	color: "#44b621"],
					[value: "12000", 	color: "#f1d801"],
					[value: "15000", 	color: "#d04e00"], 
					[value: "18000", 	color: "#bc2323"]
					
				/* For low-wattage homes, use these values
					[value: "0", color: "#153591"],
					[value: "500", color: "#1e9cbb"],
					[value: "1000", color: "#90d2a7"],
					[value: "1500", color: "#44b621"],
					[value: "2000", color: "#f1d801"],
					[value: "2500", color: "#d04e00"],
					[value: "3000", color: "#bc2323"]
				*/
				]
			)
        }

	// Power row
		valueTile("energyDisp", "device.energyDisp") {
			state(
				"default", 
				label: '${currentValue}', 
				foregroundColor: "#000000", 
				backgroundColor: "#ffffff")
		}
        valueTile("energyOne", "device.energyOne") {
        	state(
        		"default", 
        		label: '${currentValue}', 
        		foregroundColor: "#000000", 
        		backgroundColor: "#ffffff")
        }        
        valueTile("energyTwo", "device.energyTwo") {
        	state(
        		"default", 
        		label: '${currentValue}', 
        		foregroundColor: "#000000", 
        		backgroundColor: "#ffffff")
        }
        
    // Volts row
        valueTile("voltsDisp", "device.voltsDisp") {
        	state(
        		"default", 
        		label: '${currentValue} Volts', 
        		backgroundColors:[
            		[value: "115.6", 	color: "#bc2323"],
                	[value: "117.8", 	color: "#D04E00"],
                	[value: "120.0", 	color: "#44B621"],
                	[value: "122.2", 	color: "#D04E00"],
                	[value: "124.4", 	color: "#bc2323"]
            	]
            )
        }
        valueTile("voltsOne", "device.voltsOne") {
        	state(
        		"default", 
        		label:'${currentValue} Volts',
       			backgroundColors:[
       				[value: "0", color: "#ffffff"],
            		[value: "115.6", 	color: "#bc2323"],
                	[value: "117.8", 	color: "#D04E00"],
                	[value: "120.0", 	color: "#44B621"],
                	[value: "122.2", 	color: "#D04E00"],
                	[value: "124.4", 	color: "#bc2323"]
            	]
            )
        }
        valueTile("voltsTwo", "device.voltsTwo") {
        	state(
        		"default", 
        		label:'${currentValue} Volts',
    			backgroundColors:[
    				[value: "0", color: "#ffffff"],
            		[value: "115.6", 	color: "#bc2323"],
                	[value: "117.8", 	color: "#D04E00"],
                	[value: "120.0", 	color: "#44B621"],
                	[value: "122.2", 	color: "#D04E00"],
                	[value: "124.4", 	color: "#bc2323"]
            	]
            )
        }
    
    // Amps row
        valueTile("ampsDisp", "device.ampsDisp") {
        	state (
        		"default", 
        		label: '${currentValue} Amps' , 
        		foregroundColor: "#000000", 
    			color: "#000000", 
    			backgroundColors:[
					[value: "0", 	color: "#153591"],
					[value: "25", 	color: "#1e9cbb"],
					[value: "50", 	color: "#90d2a7"],
					[value: "75", 	color: "#44b621"],
					[value: "100", color: "#f1d801"],
					[value: "125", color: "#d04e00"], 
					[value: "150", color: "#bc2323"]
				]
			)
        }
        valueTile("ampsOne", "device.ampsOne") {
        	state(
        		"default",
        		label:'${currentValue} Amps',
        		foregroundColor: "#000000", 
    			color: "#000000", 
    			backgroundColors:[
					[value: "0", 	color: "#153591"],
					[value: "25", 	color: "#1e9cbb"],
					[value: "50", 	color: "#90d2a7"],
					[value: "75", 	color: "#44b621"],
					[value: "100", color: "#f1d801"],
					[value: "125", color: "#d04e00"], 
					[value: "150", color: "#bc2323"]
				]
			)
        }
        valueTile("ampsTwo", "device.ampsTwo") {
        	state(
        		"default", 
        		label:'${currentValue} Amps',
        		foregroundColor: "#000000", 
    			color: "#000000", 
    			backgroundColors:[
					[value: "0", 	color: "#153591"],
					[value: "25", 	color: "#1e9cbb"],
					[value: "50", 	color: "#90d2a7"],
					[value: "75", 	color: "#44b621"],
					[value: "100", color: "#f1d801"],
					[value: "125", color: "#d04e00"], 
					[value: "150", color: "#bc2323"]
				]
			)        		
        }
    
    // Controls row
		standardTile("reset", "command.reset", inactiveLabel: false) {
			state "default", label:'reset', action:"reset", icon: "st.Health & Wellness.health7"
		}
		standardTile("refresh", "command.refresh", inactiveLabel: false) {
			state "default", label:'refresh', action:"refresh.refresh", icon:"st.secondary.refresh-icon"
		}
		standardTile("configure", "command.configure", inactiveLabel: false) {
			state "configure", label:'', action: "configure", icon:"st.secondary.configure"
		}
		standardTile("toggle", "command.toggleDisplay", inactiveLabel: false) {
			state "default", label: "toggle", action: "toggleDisplay", icon: "st.motion.motion.inactive"
		}
		/* HEMv1 has a battery; v2 is line-powered */
		 valueTile("battery", "device.battery", decoration: "flat") {
			state "battery", label:'${currentValue}% battery', unit:""
		}

// HEM Version Configuration only needs to be done here - comments to choose what gets displayed

		main (["energyDisp","energyTwo",
			"ampsDisp","voltsDisp",				// Comment out this one for HEMv1
			"powerDisp"
			])
		details([
			"energyOne","energyDisp","energyTwo",
			"powerOne","powerDisp","powerTwo",
			"ampsOne","ampsDisp","ampsTwo",			// Comment out these two lines for HEMv1
			"voltsOne","voltsDisp","voltsTwo",		// Comment out these two lines for HEMv1
			"reset","refresh","toggle",
		//	"battery",					// Include this for HEMv1	
			"configure"
		])
	}
    preferences {
    	input "kWhCost", "string", title: "\$/kWh (0.16)", description: "0.16", defaultValue: "0.16" as String
    	input "kWhDelay", "number", title: "kWh report seconds (60)", /* description: "120", */ defaultValue: 120
    	input "detailDelay", "number", title: "Detail report seconds (30)", /* description: "30", */ defaultValue: 30
    }
}

def installed() {
	state.display = 1
	reset()						// The order here is important
	configure()					// Since reports can start coming in even before we finish configure()
	refresh()
}

def updated() {
	configure()
	resetDisplay()
	refresh()
}

def parse(String description) {
//	log.debug "Parse received ${description}"
	def result = null
	def cmd = zwave.parse(description, [0x31: 1, 0x32: 1, 0x60: 3])
	if (cmd) {
		result = createEvent(zwaveEvent(cmd))
	}
	if (result) { 
		log.debug "Parse returned ${result?.descriptionText}"
		return result
	} else {
	}
}

def zwaveEvent(physicalgraph.zwave.commands.meterv1.MeterReport cmd) {
    def dispValue
    def newValue
    def formattedValue
    def MAX_AMPS = 220
    def MAX_WATTS = 24000
    
	def timeString = new Date().format("h:mm a", location.timeZone)
    
    if (cmd.meterType == 33) {
		if (cmd.scale == 0) {
        	newValue = Math.round(cmd.scaledMeterValue * 100) / 100
        	if (newValue != state.energyValue) {
        		formattedValue = String.format("%5.2f", newValue)
    			dispValue = "${formattedValue}\nkWh"
                sendEvent(name: "energyDisp", value: dispValue as String, unit: "", descriptionText: "Display Energy: ${newValue} kWh", displayed: false)
                state.energyValue = newValue
                BigDecimal costDecimal = newValue * ( kWhCost as BigDecimal )
                def costDisplay = String.format("%5.2f",costDecimal)
                state.costDisp = "Cost\n\$"+costDisplay
                if (state.display == 1) { sendEvent(name: "energyTwo", value: state.costDisp, unit: "", descriptionText: "Display Cost: \$${costDisplay}", displayed: false) }
                [name: "energy", value: newValue, unit: "kWh", descriptionText: "Total Energy: ${formattedValue} kWh"]
            }
		} 
		else if (cmd.scale == 1) {
            newValue = Math.round( cmd.scaledMeterValue * 100) / 100
            if (newValue != state.energyValue) {
            	formattedValue = String.format("%5.2f", newValue)
    			dispValue = "${formattedValue}\nkVAh"
                sendEvent(name: "energyDisp", value: dispValue as String, unit: "", descriptionText: "Display Energy: ${formattedValue} kVAh", displayed: false)
                state.energyValue = newValue
				[name: "energy", value: newValue, unit: "kVAh", descriptionText: "Total Energy: ${formattedValue} kVAh"]
            }
		}
		else if (cmd.scale==2) {				
        	newValue = Math.round(cmd.scaledMeterValue)		// really not worth the hassle to show decimals for Watts
            if (newValue > MAX_WATTS) { return }				// Ignore ridiculous values (a 200Amp supply @ 120volts is roughly 24000 watts)
        	if (newValue != state.powerValue) {
    			dispValue = newValue+"\nWatts"
                sendEvent(name: "powerDisp", value: dispValue as String, unit: "", descriptionText: "Display Power: ${newValue} Watts", displayed: false)
                
                if (newValue < state.powerLow) {
                	dispValue = newValue+"\n"+timeString
                	if (state.display == 1) { sendEvent(name: "powerOne", value: dispValue as String, unit: "", descriptionText: "Lowest Power: ${newValue} Watts")	}
                    state.powerLow = newValue
                    state.powerLowDisp = dispValue
                }
                if (newValue > state.powerHigh) {
                	dispValue = newValue+"\n"+timeString
                	if (state.display == 1) { sendEvent(name: "powerTwo", value: dispValue as String, unit: "", descriptionText: "Highest Power: ${newValue} Watts")	}
                    state.powerHigh = newValue
                    state.powerHighDisp = dispValue
                }
                state.powerValue = newValue
                [name: "power", value: newValue, unit: "W", descriptionText: "Total Power: ${newValue} Watts"]
            }
		}
 	}
    else if (cmd.meterType == 161) {
    	if (cmd.scale == 0) {
        	newValue = Math.round( cmd.scaledMeterValue * 100) / 100
        	if (newValue != state.voltsValue) {
        		formattedValue = String.format("%5.2f", newValue)
    			dispValue = "${formattedValue}\nVolts"
                sendEvent(name: "voltsDisp", value: dispValue as String, unit: "", descriptionText: "Display Voltage: ${formattedValue} Volts", displayed: false)

                if (newValue < state.voltsLow) {
                	dispValue = formattedValue+"\n"+timeString                	
                	if (state.display == 1) { sendEvent(name: "voltsOne", value: dispValue as String, unit: "", descriptionText: "Lowest Voltage: ${formattedValue} Volts")	}
                    state.voltsLow = newValue
                    state.voltsLowDisp = dispValue
                }
                if (newValue > state.voltsHigh) {
                    dispValue = formattedValue+"\n"+timeString
                	if (state.display == 1) { sendEvent(name: "voltsTwo", value: dispValue as String, unit: "", descriptionText: "Highest Voltage: ${formattedValue} Volts") }
                    state.voltsHigh = newValue
                    state.voltsHighDisp = dispValue
                }                
                state.voltsValue = newValue
                sendEvent( name: "voltage", value: newValue, unit: "V", descriptionText: "Total Voltage: ${formattedValue} Volts")
				[name: "volts", value: newValue, unit: "V", descriptionText: "Total Volts: ${formattedValue} Volts"]
            }
        }
        else if (cmd.scale==1) {
        	newValue = Math.round( cmd.scaledMeterValue * 100) / 100
            if ( newValue > MAX_AMPS) { return }								// Ignore silly values for 200Amp service
        	if (newValue != state.ampsValue) {
        		formattedValue = String.format("%5.2f", newValue)
    			dispValue = "${formattedValue}\nAmps"
                sendEvent(name: "ampsDisp", value: dispValue as String, unit: "", descriptionText: "Display Current: ${formattedValue} Amps", displayed: false)
                
                if (newValue < state.ampsLow) {
                	dispValue = formattedValue+"\n"+timeString
                	if (state.display == 1) { sendEvent(name: "ampsOne", value: dispValue as String, unit: "", descriptionText: "Lowest Current: ${formattedValue} Amps") }
                    state.ampsLow = newValue
                    state.ampsLowDisp = dispValue
                }
                if (newValue > state.ampsHigh) {
                	dispValue = formattedValue+"\n"+timeString
                	if (state.display == 1) { sendEvent(name: "ampsTwo", value: dispValue as String, unit: "", descriptionText: "Highest Current: ${formattedValue} Amps") }
                    state.ampsHigh = newValue
                    state.ampsHighDisp = dispValue
                }                
                state.ampsValue = newValue
				[name: "amps", value: newValue, unit: "A", descriptionText: "Total Current: ${formattedValue} Amps"]
            }
        }
    }           
}

def zwaveEvent(physicalgraph.zwave.commands.multichannelv3.MultiChannelCmdEncap cmd) {
	def dispValue
	def newValue
	def formattedValue
    def MAX_AMPS = 220
    def MAX_WATTS = 24000

   	if (cmd.commandClass == 50) {    
   		def encapsulatedCommand = cmd.encapsulatedCommand([0x30: 1, 0x31: 1]) // can specify command class versions here like in zwave.parse
		if (encapsulatedCommand) {
			if (cmd.sourceEndPoint == 1) {
				if (encapsulatedCommand.scale == 2 ) {
					newValue = Math.round(encapsulatedCommand.scaledMeterValue)
                    if (newValue > MAX_WATTS) { return }
					formattedValue = newValue as String
					dispValue = "${formattedValue}\nWatts"
					if (dispValue != state.powerL1Disp) {
						state.powerL1Disp = dispValue
						if (state.display == 2) {
							[name: "powerOne", value: dispValue, unit: "", descriptionText: "L1 Power: ${formattedValue} Watts"]
						}
						else {
						}
					}
				} 
				else if (encapsulatedCommand.scale == 0 ){
					newValue = Math.round(encapsulatedCommand.scaledMeterValue * 100) / 100
					formattedValue = String.format("%5.2f", newValue)
					dispValue = "${formattedValue}\nkWh"
					if (dispValue != state.energyL1Disp) {
						state.energyL1Disp = dispValue
						if (state.display == 2) {
							[name: "energyOne", value: dispValue, unit: "", descriptionText: "L1 Energy: ${formattedValue} kWh"]
						}
						else {
						}
					}
				}
				else if (encapsulatedCommand.scale == 1 ){
					newValue = Math.round(encapsulatedCommand.scaledMeterValue * 100) / 100
					formattedValue = String.format("%5.2f", newValue)
					dispValue = "${formattedValue}\nkVAh"
					if (dispValue != state.energyL1Disp) {
						state.energyL1Disp = dispValue
						if (state.display == 2) {
							[name: "energyOne", value: dispValue, unit: "", descriptionText: "L1 Energy: ${formattedValue} kVAh"]
						}
						else {
						}
					}
				}
				else if (encapsulatedCommand.scale == 5 ) {
					newValue = Math.round(encapsulatedCommand.scaledMeterValue * 100) / 100
                    if (newValue > MAX_AMPS) { return }
					formattedValue = String.format("%5.2f", newValue)
					dispValue = "${formattedValue}\nAmps"
					if (dispValue != state.ampsL1Disp) {
						state.ampsL1Disp = dispValue
						if (state.display == 2) {
							[name: "ampsOne", value: dispValue, unit: "", descriptionText: "L1 Current: ${formattedValue} Amps"]
						}
						else {
						}
					}
               	} 
/* Ignore voltage updates, because they always match the current Total Voltage               	
               	else if (encapsulatedCommand.scale == 4 ){
               		newValue = Math.round(encapsulatedCommand.scaledMeterValue * 100) / 100
					formattedValue = String.format("%5.2f", newValue)
					dispValue = "${formattedValue}\nVolts"
					if (dispValue != statevoltsL1Disp) {
						state.voltsL1Disp = dispValue
						if (state.display == 2) {
							[name: "voltsOne", value:dispValue, unit: "", descriptionText: "L1 Voltage: ${formattedValue} Volts"]
						}
						else {
						}
					}
               	}               
*/               	
			} 
			else if (cmd.sourceEndPoint == 2) {
				if (encapsulatedCommand.scale == 2 ){
					newValue = Math.round(encapsulatedCommand.scaledMeterValue)
                    if (newValue > MAX_WATTS ) { return }
					formattedValue = newValue as String
					dispValue = "${formattedValue}\nWatts"
					if (dispValue != state.powerL2Disp) {
						state.powerL2Disp = dispValue
						if (state.display == 2) {
							[name: "powerTwo", value: dispValue, unit: "", descriptionText: "L2 Power: ${formattedValue} Watts"]
						}
						else {
						}
					}
				} 
				else if (encapsulatedCommand.scale == 0 ){
					newValue = Math.round(encapsulatedCommand.scaledMeterValue * 100) / 100
					formattedValue = String.format("%5.2f", newValue)
					dispValue = "${formattedValue}\nkWh"
					if (dispValue != state.energyL2Disp) {
						state.energyL2Disp = dispValue
						if (state.display == 2) {
							[name: "energyTwo", value: dispValue, unit: "", descriptionText: "L2 Energy: ${formattedValue} kWh"]
						}
						else {
						}
					}
				} 
				else if (encapsulatedCommand.scale == 1 ){
					newValue = Math.round(encapsulatedCommand.scaledMeterValue * 100) / 100
					formattedValue = String.format("%5.2f", newValue)
					dispValue = "${formattedValue}\nkVAh"
					if (dispValue != state.energyL2Disp) {
						state.energyL2Disp = dispValue
						if (state.display == 2) {
							[name: "energyTwo", value: dispValue, unit: "", descriptionText: "L2 Energy: ${formattedValue} kVAh"]
						}
						else {
						}
					}
				}				
				else if (encapsulatedCommand.scale == 5 ){
               		newValue = Math.round(encapsulatedCommand.scaledMeterValue * 100) / 100
                    if (newValue > MAX_AMPS) { return } 
					formattedValue = String.format("%5.2f", newValue)
					dispValue = "${formattedValue}\nAmps"
					if (dispValue != state.ampsL2Disp) {
						state.ampsL2Disp = dispValue
						if (state.display == 2) {
							[name: "ampsTwo", value: dispValue, unit: "", descriptionText: "L2 Current: ${formattedValue} Amps"]
						}
						else {
						}
					}
				}
/* Ignore voltage updates, because they always match the current Total Voltage
	    		else if (encapsulatedCommand.scale == 4 ){
               		newValue = Math.round(encapsulatedCommand.scaledMeterValue * 100) / 100
					formattedValue = String.format("%5.2f", newValue)
					dispValue = "${formattedValue}\nVolts"
					if (dispValue != statevoltsL2Disp) {
						state.voltsL2Disp = dispValue
						if (state.display == 2) {
							[name: "voltsTwo", value:dispValue, unit: "", descriptionText: "L2 Voltage: ${formattedValue} Volts"]
						}
						else {
						}
					}
               	}               			
*/               	
			}
		}
	}
}

def zwaveEvent(physicalgraph.zwave.commands.batteryv1.BatteryReport cmd) {
	def map = [:]
	map.name = "battery"
	map.unit = "%"
	
	if (cmd.batteryLevel == 0xFF) {
		map.value = 1
		map.descriptionText = "${device.displayName} battery is low"
		map.isStateChange = true
	} 
	else {
		map.value = cmd.batteryLevel
	}
	log.debug map
	return map
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
	// Handles all Z-Wave commands we aren't interested in
    log.debug "Unhandled event ${cmd}"
	[:]
}

def refresh() {			// Request HEMv2 to send us the latest values for the 4 we are tracking
	log.debug "refresh()"
    
	delayBetween([
		zwave.meterV2.meterGet(scale: 0).format(),		// Change 0 to 1 if international version
		zwave.meterV2.meterGet(scale: 2).format(),
		zwave.meterV2.meterGet(scale: 4).format(),
		zwave.meterV2.meterGet(scale: 5).format()
	])
    resetDisplay()
}

def poll() {
	log.debug "poll()"
	refresh()
}

def toggleDisplay() {
	log.debug "toggleDisplay()"
    
	if (state.display == 1) { 
		state.display = 2 
	}
	else { 
		state.display = 1
	}
	resetDisplay()
}

def resetDisplay() {
	log.debug "resetDisplay() - energyL1Disp: ${state.energyL1Disp}"
	
	if ( state.display == 1 ) {
    	sendEvent(name: "voltsOne", value: state.voltsLowDisp, unit: "")
    	sendEvent(name: "ampsOne", value: state.ampsLowDisp, unit: "")    
		sendEvent(name: "powerOne", value: state.powerLowDisp, unit: "")     
    	sendEvent(name: "energyOne", value: state.lastResetTime, unit: "")
    	sendEvent(name: "voltsTwo", value: state.voltsHighDisp, unit: "")
    	sendEvent(name: "ampsTwo", value: state.ampsHighDisp, unit: "")
    	sendEvent(name: "powerTwo", value: state.powerHighDisp, unit: "")
    	sendEvent(name: "energyTwo", value: state.costDisp, unit: "")    	
	}
	else {
    	sendEvent(name: "voltsOne", value: "L1", unit: "")
    	sendEvent(name: "ampsOne", value: state.ampsL1Disp, unit: "")    
		sendEvent(name: "powerOne", value: state.powerL1Disp, unit: "")     
    	sendEvent(name: "energyOne", value: state.energyL1Disp, unit: "")	
		sendEvent(name: "voltsTwo", value: "L2", unit: "")
    	sendEvent(name: "ampsTwo", value: state.ampsL2Disp, unit: "")
    	sendEvent(name: "powerTwo", value: state.powerL2Disp, unit: "")
    	sendEvent(name: "energyTwo", value: state.energyL2Disp, unit: "")
	}
}

def reset() {
	log.debug "reset()"

	state.energyValue = -1
	state.powerValue = -1
	state.ampsValue = -1
	state.voltsValue = -1
	
    state.powerHigh = 0
    state.powerHighDisp = ""
    state.powerLow = 99999
    state.powerLowDisp = ""
    state.ampsHigh = 0
    state.ampsHighDisp = ""
    state.ampsLow = 999
    state.ampsLowDisp = ""
    state.voltsHigh = 0
    state.voltsHighDisp = ""
    state.voltsLow = 999
    state.voltsLowDisp = ""
    
    state.energyL1Disp = ""
    state.energyL2Disp = ""
    state.powerL1Disp = ""
    state.powerL2Disp = ""
    state.ampsL1Disp = ""
    state.ampsL2Disp = ""
    state.voltsL1Disp = ""
    state.voltsL2Disp = ""
    
    if (!state.display) { state.display = 1 }	// Sometimes it appears that installed() isn't called

    def dateString = new Date().format("M/d/YY", location.timeZone)
    def timeString = new Date().format("h:mm a", location.timeZone)    
	state.lastResetTime = "Since\n"+dateString+"\n"+timeString
	state.costDisp = "Cost\n--"
	
    resetDisplay()
    sendEvent(name: "energyDisp", value: "", unit: "")
    sendEvent(name: "powerDisp", value: "", unit: "")	
    sendEvent(name: "ampsDisp", value: "", unit: "")
    sendEvent(name: "voltsDisp", value: "", unit: "")

// No V1 available
	def cmd = delayBetween( [
		zwave.meterV2.meterReset().format(),			// Reset all values
		zwave.meterV2.meterGet(scale: 0).format(),		// Request the values we are interested in (0-->1 for kVAh)
		zwave.meterV2.meterGet(scale: 2).format(),
		zwave.meterV2.meterGet(scale: 4).format(),
		zwave.meterV2.meterGet(scale: 5).format()
	], 1000)
    cmd
    
    configure()
}

def configure() {
	log.debug "configure()"
    
	Long kDelay = settings.kWhDelay as Long
    Long dDelay = settings.detailDelay as Long
    
    if (kDelay == null) {		// Shouldn't have to do this, but there seem to be initialization errors
		kDelay = 15
	}

	if (dDelay == null) {
		dDelay = 15
	}
    
	def cmd = delayBetween([
		zwave.configurationV1.configurationSet(parameterNumber: 3, size: 1, scaledConfigurationValue: 0).format(),			// Disable (=0) selective reporting
//		zwave.configurationV1.configurationSet(parameterNumber: 4, size: 2, scaledConfigurationValue: 5).format(),			// Don't send whole HEM unless watts have changed by 30
//		zwave.configurationV1.configurationSet(parameterNumber: 5, size: 2, scaledConfigurationValue: 5).format(),			// Don't send L1 Data unless watts have changed by 15
//		zwave.configurationV1.configurationSet(parameterNumber: 6, size: 2, scaledConfigurationValue: 5).format(),			// Don't send L2 Data unless watts have changed by 15
//      zwave.configurationV1.configurationSet(parameterNumber: 8, size: 1, scaledConfigurationValue: 1).format(),			// Or by 5% (whole HEM)
//		zwave.configurationV1.configurationSet(parameterNumber: 9, size: 1, scaledConfigurationValue: 1).format(),			// Or by 5% (L1)
//        zwave.configurationV1.configurationSet(parameterNumber: 10, size: 1, scaledConfigurationValue: 1).format(),			// Or by 5% (L2)
//		zwave.configurationV1.configurationSet(parameterNumber: 101, size: 4, scaledConfigurationValue: 6145).format(),   	// Whole HEM and L1/L2 power in kWh
//		zwave.configurationV1.configurationSet(parameterNumber: 111, size: 4, scaledConfigurationValue: kDelay).format(), 	// Default every 120 Seconds
//		zwave.configurationV1.configurationSet(parameterNumber: 102, size: 4, scaledConfigurationValue: 1573646).format(),  // L1/L2 for Amps & Watts, Whole HEM for Amps, Watts, & Volts
//		zwave.configurationV1.configurationSet(parameterNumber: 112, size: 4, scaledConfigurationValue: dDelay).format(), 	// Defaul every 30 seconds

//		zwave.configurationV1.configurationSet(parameterNumber: 100, size: 1, scaledConfigurationValue: 0).format(),		// reset to defaults
		zwave.configurationV1.configurationSet(parameterNumber: 101, size: 4, scaledConfigurationValue: 6149).format(),   	// All L1/L2 kWh, total Volts & kWh
		zwave.configurationV1.configurationSet(parameterNumber: 111, size: 4, scaledConfigurationValue: 60).format(), 		// Every 60 seconds
		zwave.configurationV1.configurationSet(parameterNumber: 102, size: 4, scaledConfigurationValue: 1572872).format(),	// Amps L1, L2, Total
		zwave.configurationV1.configurationSet(parameterNumber: 112, size: 4, scaledConfigurationValue: 30).format(), 		// every 30 seconds
		zwave.configurationV1.configurationSet(parameterNumber: 103, size: 4, scaledConfigurationValue: 770).format(),		// Power (Watts) L1, L2, Total
		zwave.configurationV1.configurationSet(parameterNumber: 113, size: 4, scaledConfigurationValue: 6).format() 		// every 6 seconds
	], 2000)
	log.debug cmd

	cmd
}