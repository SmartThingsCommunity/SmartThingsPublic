/**
 *  Eurotronic Spirit TRV
 *
 *  Copyright 2018 Ed Cann
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 *  With Massive thanks to: 
 *      SmartThings Fibaro Dimmer 2 Handler
 *      Danfoss Living Connect Radiator Thermostat LC-13 by Tom Philip
 *      SmartThings Gneric Z-Wave Thermostat
 *  without which i would never have figured this out
 */
metadata {
	definition (name: "Eurotronic Spirit TRV", namespace: "dougalAgain", author: "Ed Cann", ocfDeviceType: "oic.d.thermostat", vid: "generic-thermostat-1") { //, vid:"SmartThings-smartthings-Z-Wave_Thermostat"
        capability "Thermostat Mode"
		capability "Refresh"
		capability "Battery"
		capability "Thermostat Heating Setpoint"
		capability "Health Check"
		capability "Thermostat"
		capability "Temperature Measurement"
		capability "Configuration"
        capability "Actuator"
        capability "Sensor"
        capability "Thermostat Cooling Setpoint"	//attribute- coolingSetpoint	command - setCoolingSetpoint - having both alows extra settings in routines
        capability "Polling"
        capability "Thermostat Operating State"
        capability "Switch"

        command "lock"
        command "unlock"
        command "boost"
        command "boostoff"
        command "ecoheat"
        command "ecooff"
		command "temperatureUp"
		command "temperatureDown"
        command "summer" //summer mode to lock out temp changes in summer time

		attribute "minHeatingSetpoint", "number" //google alex compatability // shuld be part of heating setpoint to test without//	
		attribute "maxHeatingSetpoint", "number" //google alex compatability // shuld be part of heating setpoint to test without//	
		attribute "summer", "String" //for feed
        attribute "thermostatTemperatureSetpoint", "String"						//need for google
    
	fingerprint inClusters: "0x55,0x98"
    fingerprint manufacturerId: "328"
    fingerprint type: "0806", mfr: "0148", prod: "0003", model: "0001", cc: "20,80,70,72,31,26,71,75,98,40,43,86,26"
   // fingerprint mfr: "010F", prod: "0203", model: "2000", deviceJoinName: "Fibaro Double Switch 2" 
 //	zw:Fs type:0806 mfr:0148 prod:0003 model:0001 ver:0.15 zwv:4.61 lib:03 cc:5E,55,98,9F sec:86,85,59,72,5A,73,75,31,26,40,43,80,70,71,6C,7A role:07 ff:9200 ui:9200
        // 0x80 = Battery v1
		// 0x70 = Configuration v1
		// 0x72 = Manufacturer Specific v1
		// 0x31 = Multilevel Sensor v5
        // 0x26 = MultiLevel Switch v1
        // 0x71 = Notification v8
		// 0x75 = Protection v2
        // 0x98 = Security v2
        // 0x40 = Thermostat Mode
		// 0x43 = Thermostat Setpoint v3
		// 0x86 = Version v1
	}

//	simulator {
//	}
	tiles(scale: 2) {

		multiAttributeTile(name:"temperature", type:"thermostat", width:6, height:4, canChangeIcon: true) {
			tileAttribute("device.temperature", key: "PRIMARY_CONTROL") {
				attributeState("temperature", label:'${currentValue}°',defaultState: true, backgroundColors:[
						// Celsius Color Range
						[value: 0, color: "#153591"],
						[value: 10, color: "#1e9cbb"],
						[value: 13, color: "#90d2a7"],
						[value: 17, color: "#44b621"],
						[value: 20, color: "#f1d801"],
						[value: 25, color: "#d04e00"],
						[value: 29, color: "#bc2323"],
						// Fahrenheit Color Range
						[value: 40, color: "#153591"],
						[value: 44, color: "#1e9cbb"],
						[value: 59, color: "#90d2a7"],
						[value: 74, color: "#44b621"],
						[value: 84, color: "#f1d801"],
						[value: 92, color: "#d04e00"],
						[value: 96, color: "#bc2323"]
					])
            }

			tileAttribute("device.nextHeatingSetpoint", key: "VALUE_CONTROL") {
				attributeState "VALUE_UP", action: "temperatureUp"
				attributeState "VALUE_DOWN", action: "temperatureDown"
			}

			tileAttribute("device.thermostatOperatingState", key: "OPERATING_STATE") {
				attributeState "pending heat", backgroundColor:"#44b621"
				attributeState "heating", backgroundColor:"#d04e00"
                attributeState "emergency heat", backgroundColor:"#bc2323"
				attributeState "idle",  backgroundColor:"#cccccc"
			}

			tileAttribute("device.thermostatMode", key: "THERMOSTAT_MODE") { //need
				attributeState "default", label:'misc mode'
                attributeState "emergency heat", label:'${currentValue}' //, icon: "st.thermostat.heat", backgroundColor:"#bc2323"
              	attributeState "eco", label:'${name}' //, icon: "st.nest.nest-leaf", backgroundColor:"#44b621"
                attributeState "cool", label:'${name}' //, icon: "st.nest.nest-leaf", backgroundColor:"#44b621"
                attributeState "off", label:'${name}' //, icon: "st.thermostat.heating-cooling-off", backgroundColor:"#1e9cbb"
                attributeState "heat", label:'${name}' //, icon: "st.thermostat.heat"
			}

			tileAttribute("device.thermostatSetpoint", key: "HEATING_SETPOINT") {
				attributeState("thermostatSetpoint", label:'${currentValue}',  defaultState: true, backgroundColors:[
					// Celsius setpoint temp colour range
				[value: 0, color: "#b8c2de"],
				[value: 10, color: "#bbe1ea"],
				[value: 13, color: "#ddf1e4"],
				[value: 17, color: "#c6e9bc"],
				[value: 20, color: "#faf3b2"],
				[value: 25, color: "#f0c9b2"],
				[value: 29, color: "#eabdbd"],
					// Fahrenheit setpoint temp colour range
				[value: 40, color: "#b8c2de"],
				[value: 44, color: "#bbe1ea"],
				[value: 59, color: "#ddf1e4"],
				[value: 74, color: "#c6e9bc"],
				[value: 84, color: "#faf3b2"],
				[value: 95, color: "#f0c9b2"],
				[value: 96, color: "#eabdbd"]
			])
			}
		}
		valueTile("battery", "device.battery", inactiveLabel: true, height: 2, width: 2, decoration: "flat") {
        	state ("battery", label:'${currentValue}%', icon:"st.samsung.da.RC_ic_charge", defaultState: true, backgroundColors:[
				[value: 100, color: "#44b621"],
				[value: 50, color: "#f1d801"],
				[value: 0, color: "#bc2323"],
			])
		}
        standardTile("operatingState", "device.thermostatOperatingState", width: 2, height: 2) { // duplication for feed (icons colours)
            state "pending heat", 	label:'${name}', backgroundColor:"#44b621", icon:"st.nest.nest-leaf"
			state "heating", 		label:'${name}', backgroundColor:"#e86d13",	icon:"st.thermostat.heat" //backgroundColor:"#d04e00"
            state "emergency heat", label:'${name}', backgroundColor:"#bc2323", icon:"st.thermostat.heat"
			state "idle", 			label:'${name}', backgroundColor:"#00a0dc", icon:"st.thermostat.heating-cooling-off"
		}
		standardTile("switcher", "device.switch", height: 2, width: 2, decoration: "flat") {
			state "off", action:"on", label: "off", icon: "st.thermostat.heating-cooling-off", backgroundColor:"#ffffff"
			state "on", action:"off", label: "on", icon: "st.thermostat.heat", backgroundColor:"#00a0dc"
		}
        
        valueTile("temp", "device.temperature", inactiveLabel: true, height: 2, width: 2, decoration: "flat") {
        	state ("temp", label:'${currentValue}°', defaultState: true, backgroundColors:[
			[value: 0, color: "#153591"],
			[value: 10, color: "#1e9cbb"],
			[value: 13, color: "#90d2a7"],
			[value: 17, color: "#44b621"],
			[value: 20, color: "#f1d801"],
			[value: 25, color: "#d04e00"],
			[value: 29, color: "#bc2323"],
            // Fahrenheit Color Range
						[value: 40, color: "#153591"],
						[value: 44, color: "#1e9cbb"],
						[value: 59, color: "#90d2a7"],
						[value: 74, color: "#44b621"],
						[value: 84, color: "#f1d801"],
						[value: 92, color: "#d04e00"],
						[value: 96, color: "#bc2323"]
            
		])
        }
        valueTile("heatingSetpoint", "device.heatingSetpoint", inactiveLabel: true, height: 2, width: 2, decoration: "flat") {
                state("heatingSetpoint", label:'${currentValue}', defaultState: true, backgroundColors:[
				[value: 0, color: "#b8c2de"], 
                    [value: 10, color: "#bbe1ea"],
                    [value: 13, color: "#ddf1e4"],
                    [value: 17, color: "#c6e9bc"], 
                    [value: 20, color: "#faf3b2"],
                    [value: 25, color: "#f0c9b2"],
                    [value: 29, color: "#eabdbd"],
                    // Fahrenheit setpoint temp colour range
					[value: 40, color: "#b8c2de"], 
                    [value: 44, color: "#bbe1ea"], 
                    [value: 59, color: "#ddf1e4"], 
                    [value: 74, color: "#c6e9bc"],
					[value: 84, color: "#faf3b2"], 
                    [value: 95, color: "#f0c9b2"], 
                    [value: 96, color: "#eabdbd"]
                    ])
			} 

        standardTile("refresh", "command.refresh", inactiveLabel: false, height: 2, width: 2,
                     decoration: "flat") {
                state "default", label:'refresh', action:"refresh.refresh",
                      icon:"st.secondary.refresh"
        }
        
		standardTile("boostMode", "device.thermostatMode", height: 2, width: 2, decoration: "flat") {
			state "default", label:'press for boost', action:"boost", icon: "st.thermostat.heat"
			state "emergency heat", action:"boostoff", icon: "st.thermostat.heat", backgroundColor:"#bc2323"
		}

		standardTile("ecoMode", "device.thermostatMode", height: 2, width: 2, decoration: "flat") {
        	state "default", action:"ecoheat", label: "Eco", icon: "st.nest.nest-leaf"
			//state "eco", action:"ecoheat", label: "Eco", icon: "st.nest.nest-leaf"
			state "cool", action:"ecooff", label: "Eco", icon: "st.nest.nest-leaf", backgroundColor:"#44b621"
		}

		standardTile("lockMode", "device.protectionState", height: 2, width: 2, decoration: "flat") {
			state "unlocked", label:'press to lock',action:"lock", icon: "st.tesla.tesla-locked"
			state "locked", label:'press to unlock',action:"unlock", icon: "st.tesla.tesla-locked", backgroundColor:"#1e9cbb"
		}

		standardTile("turnOff", "device.thermostatMode", height: 2, width: 2, decoration: "flat") {
			state "default", label:'press for off', action:"off", icon: "st.thermostat.heating-cooling-off"
            state "off", label:'press for heat', action:"heat", icon: "st.thermostat.heating-cooling-off", backgroundColor:"#1e9cbb"
			
		}
		
        standardTile("configureAfterSecure", "device.configure", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "configure", label:'config not req', action:"configure", icon:"st.secondary.configure"
            state "configdue", label: "press me", action:"configure", icon:"st.secondary.configure", backgroundColor:"#bc2323"
		}
		standardTile("summer", "device.summer", height: 2, width: 2, decoration: "flat") {
			state "off", 	label: "Press for \nSummer", 		action:"summer", 	icon: "st.thermostat.auto", backgroundColor:"#d3d3d3"
			state "on", 	label: "Press to turn\n off summer", action:"summer", 	icon: "st.custom.wuk.clear"
		}  
        
        valueTile("valve", "device.valve", inactiveLabel: true, height: 2, width: 2, decoration: "flat") {
        	state "valve", label:'${currentValue}%', icon:"st.Outdoor.outdoor16", defaultState: true
        }
        controlTile("heatSliderControl", "device.heatingSetpoint", "slider", height: 2, width: 2, inactiveLabel: false, range:"(4..28)") {
			state ("heatingSetpoint", action:"setHeatingSetpoint")
        }
		main "temperature"
		details(["temperature", "boostMode", "ecoMode", "lockMode", "turnOff", "refresh", "battery", "configureAfterSecure", "summer","valve", "heatSliderControl"])
	}
	def rates = [:]
		rates << ["1" : "Refresh every minutes (Not Recommended)"]
		rates << ["5" : "Refresh every 5 minutes"]
		rates << ["10" : "Refresh every 10 minutes"]
		rates << ["15" : "Refresh every 15 minutes"]
    
	preferences {
        input "LCDinvert", "enum", title: "Invert LCD", options: ["No", "Yes"], defaultValue: "No", required: false, displayDuringSetup: true
        input "LCDtimeout", "number", title: "LCD Timeout (in secs)", description: "LCD will switch off after this time (5 - 30secs)", range: "5..30", displayDuringSetup: true
        input "ecoTemp", "number", title: "Eco Heat Temperature", description: "Temperature to heat to in Eco Mode (8 - 28°C)", range: "8..28", displayDuringSetup: false
        input "backlight", "enum", title: "Enable backlight", options: ["No", "Yes"], defaultValue: "No", required: false, displayDuringSetup: true
        input "windowOpen", "enum", title: "Window Open Detection",description: "Sensitivity of Open Window Detection", options: ["Disabled", "Low", "Medium", "High" ], defaultValue: "Medium", required: false, displayDuringSetup: false
        input "tempOffset", "number", title: "Temperature Offset", description: "Adjust the measured temperature (-5 to +5°C)", range: "-5..5", displayDuringSetup: false
        input "tempMin", "number", title: "Min Temperature device Recognises", description: "default 4 (norm 4 to around 8°C)", range: "-5..10", displayDuringSetup: false
        input "tempMax", "number", title: "Max Temperature device Recognises", description: "default 28 (norm 28 to around 35°C)", range: "25..40", displayDuringSetup: false
		input name: "refreshRate", type: "enum", title: "Refresh Rate", options: rates, description: "Select Refresh Rate", required: false
    }   
}


def parse(String description) {
//log.debug "Parsing '${description}'"
	def result = []
    if (description.startsWith("Err 106")) {
        state.sec = 0
        result = createEvent(descriptionText: description, isStateChange: true)
    }
    else {
        def cmd = zwave.parse(description)
        if (cmd) {
            result += zwaveEvent(cmd)
            //log.debug "Parsed ${cmd} to ${result.inspect()}"
        } else {
            log.debug "Non-parsed event: ${description}"
        }
    }
    return result
}

def zwaveEvent(physicalgraph.zwave.commands.sensormultilevelv5.SensorMultilevelReport cmd) {
    def map = [ value: cmd.scaledSensorValue.toString(), displayed: true ]
    def value = cmd.scaledSensorValue.toString()
    switch (cmd.sensorType) {
    	case 1:
        	map.name = "temperature"
            map.unit = cmd.scale == 1 ? "F" : "C"
            state.temperature = cmd.scaledSensorValue //.toString()
            break;
		case 2:
        	map.name = "value"
            map.unit = cmd.scale == 1 ? "%" : ""
            break;
	}
    log.info "RepRecived $cmd"
    createEvent(map)
    //sendEvent(map)
}

def zwaveEvent(physicalgraph.zwave.commands.batteryv1.BatteryReport cmd) {
    def map = [ name: "battery", unit: "%" ]
    if (cmd.batteryLevel == 0xFF) {  // Special value for low battery alert
    	map.value = 1
        map.descriptionText = "${device.displayName} has a low battery"
        map.isStateChange = true
	} 
    else {
    	map.value = cmd.batteryLevel
    }
    state.lastBatteryReportReceivedAt = new Date().time // Store time of last battery update so we don't ask every wakeup, see WakeUpNotification handler
    log.info "RepRecived $cmd"
    createEvent(map)
	//sendEvent(map)
}

def zwaveEvent(physicalgraph.zwave.commands.associationv2.AssociationReport cmd) {
        def result = []
        if (cmd.nodeId.any { it == zwaveHubNodeId }) {
                result << sendEvent(descriptionText: "$device.displayName is associated in group ${cmd.groupingIdentifier}")
        } else if (cmd.groupingIdentifier == 1) {
                // We're not associated properly to group 1, set association
                result << sendEvent(descriptionText: "Associating $device.displayName in group ${cmd.groupingIdentifier}")
                result << response(zwave.associationV1.associationSet(groupingIdentifier:cmd.groupingIdentifier, nodeId:zwaveHubNodeId))
        }
        log.info "RepRecived $cmd"
        result
}

def zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityMessageEncapsulation cmd) { // Devices that support the Security command class can send messages in an encrypted form; they arrive wrapped in a SecurityMessageEncapsulation command and must be unencapsulated
	log.debug "raw secEncap $cmd"
    state.sec = 1
	def encapsulatedCommand = cmd.encapsulatedCommand ([0x20: 1, 0x80: 1, 0x70: 1, 0x72: 1, 0x31: 5, 0x26: 3, 0x75: 1, 0x40: 2, 0x43: 2, 0x86: 1, 0x71: 3, 0x98: 2, 0x7A: 1 ]) //0x98: 2, 0x98: 2, 

//to test    def encapsulatedCommand = cmd.encapsulatedCommand
//old def encapsulatedCommand = cmd.encapsulatedCommand ([0x20: 1, 0x80: 1, 0x70: 1, 0x72: 1, 0x31: 5, 0x26: 1, 0x71: 8,0x75: 1, 0x40: 2, 0x43: 2, 0x86: 1 ]) //0x98: 2, 0x98: 2, 
																					// 72 changed to v2													71 added 7A added			
    if (encapsulatedCommand) {
		return zwaveEvent(encapsulatedCommand)
	} else {
		log.warn "Unable to extract encapsulated cmd from $cmd"
		createEvent(descriptionText: cmd.toString())
	}
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
	def map = [ descriptionText: "${device.displayName}: ${cmd}" ]
	log.warn "mics zwave.Command - ${device.displayName} - $cmd"
    sendEvent(map)
}

def zwaveEvent(physicalgraph.zwave.commands.thermostatmodev2.ThermostatModeSupportedReport cmd) {
	    log.debug "$cmd"
    def supportedModes = []
		if(cmd.off) { supportedModes << "off" }
		if(cmd.heat) { supportedModes << "heat" }
		if(cmd.cool) { supportedModes << "cool" }
		if(cmd.auto) { supportedModes << "auto" }
		if(cmd.auxiliaryemergencyHeat) { supportedModes << "emergency heat" } //boost
    	if (cmd.energySaveHeat) { supportedModes << "eco"} //eco //removed 4/2/19 == true)
	state.supportedModes = supportedModes //.toString()
    log.info "modes are ${state.supportedModes}"
	
    //updateDataValue("availableThermostatModes", supportedModes.toString())
    sendEvent(name: "supportedThermostatModes", value: supportedModes, displayed: false)
    log.info "RepRec $cmd, modes are $supportedModes"
}

def zwaveEvent(physicalgraph.zwave.commands.manufacturerspecificv2.ManufacturerSpecificReport cmd) {
	if (cmd.manufacturerName) { updateDataValue("manufacturer", cmd.manufacturerName) }
	if (cmd.productTypeId) { updateDataValue("productTypeId", cmd.productTypeId.toString()) }
	if (cmd.productId) { updateDataValue("productId", cmd.productId.toString()) }
    if (cmd.manufacturerId){ updateDataValue("manufacturerId", cmd.manufacturerId.toString()) }
    log.info "RepRecived $cmd"
}

def zwaveEvent(physicalgraph.zwave.commands.configurationv2.ConfigurationReport cmd ) {
    log.info "RepRecived $cmd"
    //cmd
}

def zwaveEvent(physicalgraph.zwave.commands.switchmultilevelv3.SwitchMultilevelReport cmd){
	log.info "RepRecived - $cmd - - Valve open '${cmd.value}'%"
    createEvent(name: "valve", value: cmd.value, unit: "%", descriptionText: "Valve open '${cmd.value}'%")
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd){
	def event = [ ]
	if (cmd.value == 255) { //255 - 0xFF = normall mode
    	state.thermostatMode = "heat"
        state.thermostatOperatingState = "heating"
        state.switch = "on"
    }
    if (cmd.value == 240){ //240 - 0xF0 = boost
    	state.thermostatMode = "emergency heat"
        state.thermostatOperatingState = "emergency heat"
        state.switch = "on"
    }
    if (cmd.value == 0){ //0 - 0x00 = eco
    	state.thermostatMode = "cool"
        state.thermostatOperatingState = "pending heat"
        state.switch = "on"
    }
    if (cmd.value == 15){ //15 - 0x0F = off
    	state.thermostatMode = "off"
        state.thermostatOperatingState = "idle"
        state.switch = "off"
    }
    //254 - 0xFE = direct valve contol mode
    event << createEvent(name: "thermostatMode", value: state.thermostatMode, displayed: true)
    event << createEvent(name: "thermostatOperatingState", value: state.thermostatOperatingState, displayed: true)
    event << createEvent(name: "switch", value: state.switch, displayed: false)
    
	log.info "RepRecived ${cmd}, ${state.thermostatMode}, ${state.thermostatOperatingState}"
    return event
}

def zwaveEvent(physicalgraph.zwave.commands.thermostatmodev2.ThermostatModeReport cmd ) {
    def event = []
    if (cmd.mode == 1){ //1 normall heat 0x01
    	state.thermostatMode = "heat"
        state.thermostatOperatingState = "heating"
        state.switch = "on"
    }
    if (cmd.mode == 15){ //15 boost 0x0F
    	state.thermostatMode = "emergency heat"
        state.thermostatOperatingState = "emergency heat"
        state.switch = "on"
    }
    if (cmd.mode == 11){ //11 eco 11 0x0B
    	state.thermostatMode = "cool"
        state.thermostatOperatingState = "pending heat"
        state.switch = "on"
    }
    if (cmd.mode == 0){ // 0 off 0x00
    	state.thermostatMode = "off"
        state.thermostatOperatingState = "idle"
        state.switch = "off"
    }
    event << createEvent(name: "thermostatMode", value: state.thermostatMode, displayed: true)
    event << createEvent(name: "thermostatOperatingState", value: state.thermostatOperatingState, displayed: true)
    event << createEvent(name: "switch", value: state.switch, displayed: false)
    
	log.info "RepRecived ${cmd}, ${state.thermostatMode}, ${state.thermostatOperatingState}"
    return event
}

def zwaveEvent(physicalgraph.zwave.commands.thermostatsetpointv2.ThermostatSetpointReport cmd) { //	Parsed ThermostatSetpointReport(precision: 2, reserved01: 0, scale: 0, scaledValue: 21.00, setpointType: 1, size: 2, value: [8, 52])
	def event = []
	state.scale = cmd.scale	// So we can respond with same format later, see setHeatingSetpoint()
	state.precision = cmd.precision
    def radiatorSetPoint = cmd.scaledValue

    if (cmd.setpointType == 1 ) { //this is the standard heating setpoint //sendEvent
		event << createEvent(name: "nextHeatingSetpoint", value: radiatorSetPoint, unit: getTemperatureScale(), displayed: true)
       	event << createEvent(name: "heatingSetpoint", value: radiatorSetPoint.toString(), unit: getTemperatureScale(), displayed: true)
        event << createEvent(name: "coolingSetpoint", value: radiatorSetPoint.toString(), unit: getTemperatureScale(), displayed: false)
		event << createEvent(name: "thermostatSetpoint", value: radiatorSetPoint.toString(), unit: getTemperatureScale(), displayed: false)
        event << createEvent(name: "thermostatTemperatureSetpoint", value: radiatorSetPoint.toString(), unit: "C", displayed: false)
	}
    if (cmd.setpointType == 11 ) { // this is eco heat setting on this device
    	//event << createEvent(name: "coolingSetpoint", value: radiatorSetPoint.toString(), unit: getTemperatureScale(), displayed: false)
    }
    log.info "RepRec ${cmd}"
    return event //List
}

def setDeviceLimits() { // for google and amzon compatability
	sendEvent(name:"minHeatingSetpoint", value: settings.tempMin ?: 4, unit: "°C", displayed: false)
	sendEvent(name:"maxHeatingSetpoint", value: settings.tempMax ?: 28, unit: "°C", displayed: false)
    log.trace "setDeviceLimits - device max/min set"
}
def temperatureUp() {
    //log.debug(device.currentValue("nextHeatingSetpoint").toBigDecimal())
	def nextTemp = device.currentValue("nextHeatingSetpoint").toBigDecimal() + 0.5
							// TODO: deal with Farenheit?
	if(nextTemp > 28) {		// It can't handle above 28, so don't allow it go above
		nextTemp = 28
	}
	sendEvent(name:"nextHeatingSetpoint", value: nextTemp, unit: getTemperatureScale(), displayed: false)	
    runIn (5, "buffSetpoint",[data: [value: nextTemp], overwrite: true])
}
def temperatureDown() {
	def nextTemp = device.currentValue("nextHeatingSetpoint").toBigDecimal() - 0.5
	if(nextTemp < 8) {		// It can't go below 8, so don't allow it
		nextTemp = 8
	}
	sendEvent(name:"nextHeatingSetpoint", value: nextTemp, unit: getTemperatureScale(), displayed: false)	
   	runIn (5, "buffSetpoint",[data: [value: nextTemp], overwrite: true])
}
def buffSetpoint(data) {
	def key = "value"
	def nextTemp = data[key]
    //log.debug " buff nextTemp is $nextTemp"
	setHeatingSetpoint(nextTemp)
}

def setCoolingSetpoint(temp){
	log.trace "Set cooling setpoint temp of ${temp}, sending temp value to setHeatingSetpoint"
	setHeatingSetpoint(temp)
}
def setHeatingSetpoint(Double degrees) { //Double added
	def cmds = []
    def precision = state.precision ?: 2
    def deviceScale = state.scale ?: 0
    
    if (state.summer == "on" && degrees != 28){
    	degrees = 28.0
        log.warn "temp changed to ${degrees} as in summer mode"
    }
	
	sendEvent(name:"nextHeatingSetpoint", value: degrees, unit: getTemperatureScale(), descriptionText: "Next heating setpoint is ${degrees}", displayed: true, isStateChange:true)
	
	cmds << zwave.thermostatSetpointV2.thermostatSetpointSet(precision: precision, scale: deviceScale, scaledValue: degrees, setpointType: 1)
	cmds << zwave.thermostatSetpointV2.thermostatSetpointGet(setpointType: 1)
			
	log.trace "Setting Temp to ${degrees},  $cmds"
	secureSequence(cmds)
}

def lock() {
	def cmds = []
    sendEvent(name: "protectionState", value: "locked", displayed: false)
    cmds << zwave.protectionV1.protectionSet(protectionState: 1)
    cmds << zwave.protectionV1.protectionGet()
	log.trace "lock $cmds" 
    secureSequence(cmds)
}
def unlock() {
	def cmds = []
    sendEvent(name: "protectionState", value: "unlocked", displayed: false)
    cmds << zwave.protectionV1.protectionSet(protectionState: 0)
    cmds << zwave.protectionV1.protectionGet()
	log.trace "unlock $cmds" 
    secureSequence (cmds)
}

def emergencyHeat(){
	log.trace "emergencyHeat to boost"
    boost()
}
def boost() {
	def cmds = []
	sendEvent(name: "thermostatMode", value: "emergency heat", displayed: true)
    cmds << zwave.thermostatModeV2.thermostatModeSet(mode: 0x0F)
    cmds << zwave.thermostatModeV2.thermostatModeGet()
	log.trace "Boost On $cmds"
    secureSequence(cmds)
}
def boostoff() {
	log.trace "Boost Off"
    heat()
}

def cool(){
	log.trace "cool to eco"
	ecoheat()
}
def ecoheat() {
	def cmds = []
    sendEvent(name: "thermostatMode", value: "cool", displayed: true)
    cmds << zwave.thermostatModeV2.thermostatModeSet(mode: 11)
    cmds << zwave.thermostatModeV2.thermostatModeGet()
    log.trace "Eco/Cool Heat $cmds"
    secureSequence(cmds)
}
def ecooff() {
	log.trace "eco Off"
    heat()
}

def on() {
	log.trace "on to heat"
    heat()
}
def auto(){
	log.trace "auto to heat"
	heat()
}
def heat() {
    def cmds = []
    sendEvent(name: "thermostatMode", value: "heat", displayed: true)
	cmds << zwave.thermostatModeV2.thermostatModeSet(mode: 1)
    cmds << zwave.thermostatModeV2.thermostatModeGet()
	log.trace "heat $cmds" 
    secureSequence (cmds)
}

def off() {
	def cmds = []
    sendEvent(name: "thermostatMode", value: "off", displayed: true)
	cmds << zwave.thermostatModeV2.thermostatModeSet(mode: 0)
    cmds << zwave.thermostatModeV2.thermostatModeGet()
	log.trace "OFF $cmds" 
 	secureSequence(cmds)
}
                           
def setThermostatMode(mode){
	if (mode == "on" || mode == "heat" || mode == "auto" || mode == "1") { heat() }
    if (mode == "off" || mode == "0") { off()}
    if (mode == "cool" || mode == "eco") { ecoheat() }
    if (mode == "emergency heat") {boost()}
    //"rush hour" ??
	log.debug "set mode $mode" 
}

def refresh() {
	log.trace "refresh"
	poll()
}
//input "ecoTemp", "number", title: "Eco Heat Temperature", description: "Temperature to heat to in Eco Mode (8 - 28°C)", range: "8..28", displayDuringSetup: false
            
def updated() {
sendEvent(name: "checkInterval", value: 2 * 15 * 60 + 2 * 60, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])
    if (!state.updatedLastRanAt || new Date().time >= state.updatedLastRanAt + 2000) {
        state.updatedLastRanAt = new Date().time
        unschedule(refresh)
        unschedule(poll)
        log.trace "updated 1"
        runIn (05, configure)
        sendEvent(name: "configure", value: "configdue", displayed: false)
        switch(refreshRate) {
		case "1":
			runEvery1Minute(poll)
			log.info "Refresh Scheduled for every minute"
			break
		case "15":
			runEvery15Minutes(poll)
			log.info "Refresh Scheduled for every 15 minutes"
			break
		case "10":
			runEvery10Minutes(poll)
			log.info "Refresh Scheduled for every 10 minutes"
			break
		default:
        	runEvery5Minutes(poll)
			log.info "Refresh Scheduled for every 5 minutes"	}
        
	}
    else {
    	log.warn "update ran within the last 2 seconds"
    }
}

def poll() { // If you add the Polling capability to your device type, this command will be called approximately every 5 minutes to check the device's state
//log.debug "poll"
	def cmds = []

    if (!state.lastBatteryReportReceivedAt || (new Date().time) - state.lastBatteryReportReceivedAt > daysToTime(1)) {
		log.trace "POLL - Asking for battery report as over 1 days since"
       	cmds << zwave.batteryV1.batteryGet()
	}
	//once an hour ask for everything
    if (!state.extra || (new Date().time) - state.extra > (60*60000)) {			// mimutes * millseconds these settings shouldnt be needs as device should send response at time of update
    	//cmds <<	zwave.thermostatModeV2.thermostatModeGet()							// get mode
    	cmds <<	zwave.thermostatSetpointV1.thermostatSetpointGet(setpointType: 11) 	// get eco/cool setpoint
    	cmds <<	zwave.basicV1.basicGet()											// get mode (basic)	
    	cmds <<	zwave.thermostatSetpointV2.thermostatSetpointGet(setpointType: 1)	// get heating setpoint
        state.extra = new Date().time
    }
    cmds <<	zwave.sensorMultilevelV1.sensorMultilevelGet()						// get temp
	cmds << zwave.switchMultilevelV3.switchMultilevelGet()						// valve position
    cmds <<	zwave.thermostatModeV2.thermostatModeGet()							// get mode
    
    log.trace "POLL $cmds"
    secureSequence (cmds)
}

def daysToTime(days) {
	days*24*60*60*1000
}
// If you add the Configuration capability to your device type, this command will be called right after the device joins to set device-specific configuration commands.
def configure() {
	state.supportedModes = [off,heat] // basic modes prior to detailes from device
	setDeviceLimits()
	def cmds = []
	cmds << zwave.configurationV1.configurationSet(configurationValue:  LCDinvert == "Yes" ? [0x01] : [0x00], parameterNumber:1, size:1, scaledConfigurationValue:  LCDinvert == "Yes" ? 0x01 : 0x00)//,
	cmds << zwave.configurationV1.configurationSet(configurationValue: LCDtimeout == null ? [0] : [LCDtimeout], parameterNumber:2, size:1, scaledConfigurationValue: LCDtimeout == null ? 0 :  LCDtimeout)//,
	cmds << zwave.configurationV1.configurationSet(configurationValue:  backlight == "Yes" ? [0x01] : [0x00], parameterNumber:3, size:1, scaledConfigurationValue:  backlight == "Yes" ? 0x01 : 0x00)//,
	cmds << zwave.configurationV1.configurationSet(configurationValue:  windowOpen == "Low" ? [0x01] : windowOpen == "Medium" ? [0x02] : windowOpen == "High" ? [0x03] : [0x00], parameterNumber:7, size:1, scaledConfigurationValue:  windowOpen == "Low" ? 0x01 : windowOpen == "Disabled" ? 0x00 : windowOpen == "High" ? 0x03 : 0x02)//,
	cmds << zwave.configurationV1.configurationSet(configurationValue: tempOffset == null ? [0] : [tempOffset*10], parameterNumber:8, size:1, scaledConfigurationValue: tempOffset == null ? 0 : tempOffset*10)//,
	cmds << zwave.thermostatSetpointV1.thermostatSetpointSet(precision: 1, reserved01: 0, scale: 0, scaledValue: ecoTemp == null ? 8 : ecoTemp, setpointType: 11, size: 2, value: ecoTemp == null ? [0, 80] : [0, ecoTemp*10])//,
	cmds << zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType:1, scale:1)  // get temp
	cmds << zwave.thermostatModeV2.thermostatModeGet()
	cmds << zwave.thermostatSetpointV1.thermostatSetpointGet(setpointType: 0x01)
	cmds << zwave.thermostatSetpointV1.thermostatSetpointGet(setpointType: 0x0B)
	cmds << zwave.configurationV1.configurationGet(parameterNumber:1)
	cmds << zwave.configurationV1.configurationGet(parameterNumber:2)
	cmds << zwave.configurationV1.configurationGet(parameterNumber:3)
	cmds << zwave.configurationV1.configurationGet(parameterNumber:7)
	cmds << zwave.configurationV1.configurationGet(parameterNumber:8)
	cmds << zwave.batteryV1.batteryGet()
	cmds << zwave.thermostatModeV2.thermostatModeSupportedGet()
	cmds << zwave.manufacturerSpecificV1.manufacturerSpecificGet()

    sendEvent(name: "configure", value: "configure", displayed: false)   
	log.trace "config"
    secureSequence(cmds)
}

def secure(physicalgraph.zwave.Command cmd) {
	if (state.sec) {
		//log.debug "Seq secure - $cmd"
		zwave.securityV1.securityMessageEncapsulation().encapsulate(cmd).format()
	} 
    else {
    	//log.debug "Seq unsecure- $cmd"
		cmd.format()
	}
}

def secureSequence(commands, delay=1500) {
//log.debug "SeSeq $commands"
	//delayBetween(commands.collect{ secure(it) }, delay)
    sendHubCommand(commands.collect{ response(secure(it)) }, delay)
}

def zwaveEvent(physicalgraph.zwave.commands.crc16encapv1.Crc16Encap cmd) { //dont know if this is used
	log.debug "crc16encap- $cmd"
	def versions = [0x31: 5, 0x30: 1, 0x9C: 1, 0x70: 2, 0x85: 2]
	def version = versions[cmd.commandClass as Integer]
	def ccObj = version ? zwave.commandClass(cmd.commandClass, version) : zwave.commandClass(cmd.commandClass)
	def encapsulatedCommand = ccObj?.command(cmd.command)?.parse(cmd.data)
	if (encapsulatedCommand) {
		zwaveEvent(encapsulatedCommand)
	}
}

def summer() {
	def discText = " "
    def temp = 0
	def cmds = []
    if (state.summer == "on" || state.summer == null){
    	log.info "summer is on so turn off"
        temp = 20
    	discText = "summer mode off, temp change commands will processed as normall"
        state.summer = "off"
    }
    else if (state.summer == "off"){
		log.info "summer is off, turning on"
		temp = 28
    	discText = "summer mode activated, all temp change commands will be blocked and the trv will stay fully open. Mannual adjustment will turn this off"
    	state.summer = "on"
    }
    else {
    	log.warn "shouldnt go here"
        temp = 20
        state.summer = "off"
    	discText = "some thing went wroung in summer mode"
    }
    cmds << sendEvent(name: "summer", value: state.summer, descriptionText: discText)
	cmds <<	setHeatingSetpoint(temp)
    cmds
}  