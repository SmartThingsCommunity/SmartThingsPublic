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
	definition (name: "Eurotronic Spirit TRV", namespace: "dougalAgain", author: "Ed Cann", ocfDeviceType: "oic.d.switch", mnmn: "SmartThings", vid: "generic-switch") {
        capability "Thermostat" // depeciated
        capability "Temperature Measurement"
        capability "Battery"
        capability "Actuator"
        capability "Refresh"
        capability "Sensor"
        capability "Configuration"
        capability "Polling"
        capability "Thermostat Cooling Setpoint"	//attribute- coolingSetpoint	command - setCoolingSetpoint - having both alows extra settings in routines
        capability "Thermostat Heating Setpoint" 
        capability "Switch"

        command "lock"
        command "unlock"
        command "boost"
        command "boostoff"
        command "ecoheat"
        command "ecooff"
        command "heat"
        command "off"
		command "temperatureUp"
		command "temperatureDown"


	  fingerprint type: "0806", mfr: "0148", prod: "0003", model: "0001", cc: "20,80,70,72,31,26,71,75,98,40,43,86,26"
     
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
				attributeState "Eco", label:'Eco', backgroundColor:"#44b621", icon:"st.nest.nest-leaf"
				attributeState "heating", backgroundColor:"#d04e00", icon:"st.thermostat.heat"
                attributeState "Boost", label:"Boost", backgroundColor:"#bc2323", icon:"st.thermostat.heat"
				attributeState "Off", label:"Off", backgroundColor:"#cccccc", icon:"st.thermostat.heating-cooling-off"
			}

			tileAttribute("device.thermostatMode", key: "THERMOSTAT_MODE") {
				attributeState "default", label:'${currentValue}'
              attributeState "heat", label:"heat"
				attributeState "off", label:"off"
			}

			tileAttribute("device.thermostatSetpoint", key: "HEATING_SETPOINT") {
				attributeState("thermostatSetpoint", label:'${currentValue}', unit:"°C", defaultState: true, backgroundColors:[
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

        standardTile("operatingState", "device.thermostatOperatingState", width: 2, height: 2) {
            state "esheating", label:'Energy Save Heating', backgroundColor:"#44b621", icon:"st.nest.nest-leaf"
			state "heating", label:"Heating", backgroundColor:"#d04e00", icon:"st.thermostat.heat"
            state "qheating", label:"Quick Heating", backgroundColor:"#bc2323", icon:"st.thermostat.heat"
			state "off", label:"Off", backgroundColor:"#cccccc", icon:"st.thermostat.heating-cooling-off"
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
                state "default", label:'', action:"refresh.refresh",
                      icon:"st.secondary.refresh"
        }

        valueTile("battery", "device.battery", inactiveLabel: true, height: 2, width: 2, decoration: "flat") {
        	state ("battery", label:'${currentValue}%', icon:"st.samsung.da.RC_ic_charge", defaultState: true, backgroundColors:[
				[value: 100, color: "#44b621"],
				[value: 50, color: "#f1d801"],
				[value: 0, color: "#bc2323"],
			])
		}
        
		standardTile("boostMode", "device.thermostatMode", height: 2, width: 2, decoration: "flat") {
			state "default", action:"boost", icon: "st.thermostat.heat"
			state "qheating", action:"boostoff", icon: "st.thermostat.heat", backgroundColor:"#bc2323"
		}

		standardTile("ecoMode", "device.thermostatMode", height: 2, width: 2, decoration: "flat") {
			state "default", action:"ecoheat", label: "Eco", icon: "st.nest.nest-leaf"
			state "esheating", action:"ecooff", label: "Eco", icon: "st.nest.nest-leaf", backgroundColor:"#44b621"
		}

		standardTile("lockMode", "device.protectionState", height: 2, width: 2, decoration: "flat") {
			state "unlocked", action:"lock", icon: "st.tesla.tesla-locked"
			state "locked", action:"unlock", icon: "st.tesla.tesla-locked", backgroundColor:"#1e9cbb"
		}

		standardTile("turnOff", "device.thermostatMode", height: 2, width: 2, decoration: "flat") {
			state "off", action:"heat", icon: "st.thermostat.heating-cooling-off", backgroundColor:"#1e9cbb"
			state "default", action:"off", icon: "st.thermostat.heating-cooling-off"
		}
		/*standardTile("configureAfterSecure", "command.configureAfterSecure", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "configure", label:'', action:"configureAfterSecure.configureAfterSecure", icon:"st.secondary.configure"
		}*/
        standardTile("configureAfterSecure", "device.configure", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "configure", label:'', action:"configure", icon:"st.secondary.configure"
            state "configdue", label: "press me", action:"configure", icon:"st.secondary.configure", backgroundColor:"#bc2323"
		}
		
		main "temperature"
		details(["temperature", "boostMode", "ecoMode", "lockMode", "turnOff", "refresh", "battery", "heatingSetpoint", "temp", "configureAfterSecure"])
	}
 
	preferences {
        input "LCDinvert", "enum", title: "Invert LCD", options: ["No", "Yes"], defaultValue: "No", required: false, displayDuringSetup: true
        input "LCDtimeout", "number", title: "LCD Timeout (in secs)", description: "LCD will switch off after this time (5 - 30secs)", range: "5..30", displayDuringSetup: true
        input "ecoTemp", "number", title: "Eco Heat Temperature", description: "Temperature to heat to in Eco Mode (8 - 28°C)", range: "8..28", displayDuringSetup: false
        input "backlight", "enum", title: "Enable backlight", options: ["No", "Yes"], defaultValue: "No", required: false, displayDuringSetup: true
        input "windowOpen", "enum", title: "Window Open Detection",description: "Sensitivity of Open Window Detection", options: ["Disabled", "Low", "Medium", "High" ], defaultValue: "Medium", required: false, displayDuringSetup: false
        input "tempOffset", "number", title: "Temperature Offset", description: "Adjust the measured temperature (-5 to +5°C)", range: "-5..5", displayDuringSetup: false
    }   
}


def parse(String description) {
//log.debug "Parsing '${description}'"
	def result = null
    def cmd = zwave.parse(description)
    if (cmd) {
       	result = zwaveEvent(cmd)
     	//log.debug "Parsed - ${result.inspect()}" //${cmd} to
    }
    else {
       	log.warn "Non-parsed event: ${description}"
    }
    result
}

def zwaveEvent(physicalgraph.zwave.commands.sensormultilevelv5.SensorMultilevelReport cmd) {
	def map = [ value: cmd.scaledSensorValue.toString(), displayed: true ]
    def value = cmd.scaledSensorValue.toString()
    switch (cmd.sensorType) {
    	case 1:
        	map.name = "temperature"
            map.unit = cmd.scale == 1 ? "F" : "C"
            break;
		case 2:
        	map.name = "value"
            map.unit = cmd.scale == 1 ? "%" : ""
            break;
	}
    log.info "Report recived $cmd"
    createEvent(map)
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
    log.info "Report recived $cmd"
    createEvent(map)
}

def zwaveEvent(physicalgraph.zwave.commands.associationv2.AssociationReport cmd) {
        def result = []
        if (cmd.nodeId.any { it == zwaveHubNodeId }) {
                result << createEvent(descriptionText: "$device.displayName is associated in group ${cmd.groupingIdentifier}")
        } else if (cmd.groupingIdentifier == 1) {
                // We're not associated properly to group 1, set association
                result << createEvent(descriptionText: "Associating $device.displayName in group ${cmd.groupingIdentifier}")
                result << response(zwave.associationV1.associationSet(groupingIdentifier:cmd.groupingIdentifier, nodeId:zwaveHubNodeId))
        }
        log.info "Report recived $cmd"
        result
}

def zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityMessageEncapsulation cmd) {
// Devices that support the Security command class can send messages in an encrypted form; they arrive wrapped in a SecurityMessageEncapsulation
// command and must be unencapsulated
        def encapsulatedCommand = cmd.encapsulatedCommand([0x98: 2, 0x20: 1, 0x80: 1, 0x70: 1, 0x72: 1, 0x31: 5, 0x26: 1, 0x71: 8,0x75: 1, 0x98: 2, 0x40: 2, 0x43: 2, 0x86: 1 ])
        // can specify command class versions here like in zwave.parse
        if (encapsulatedCommand) {
                return zwaveEvent(encapsulatedCommand)
        }
        log.info "Report recived $cmd"
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
log.warn "mics zwave.Command - $cmd "
//basic command not processed and sent hear
//255 = normall mode
        createEvent(descriptionText: "${device.displayName}: ${cmd}")
}

def zwaveEvent(physicalgraph.zwave.commands.configurationv1.ConfigurationReport cmd ) {
    log.info "Report recived $cmd"
    cmd
}

def zwaveEvent(physicalgraph.zwave.commands.switchmultilevelv1.SwitchMultilevelReport cmd){
	log.info "Report recived - $cmd - - Valve open '${cmd.value}'%"
    createEvent(name: "refresh", value: cmd.value, descriptionText: "Valve open '${cmd.value}'%")
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd){
	def event = [ ]
	if (cmd.value == 255) { //255 - 0xFF = normall mode
    	state.thermostatMode = "heat"
        state.thermostatOperatingState = "heating"
    	event << createEvent(name: "thermostatMode", value: "heat", displayed: true)
    	event << createEvent(name: "thermostatOperatingState", value: "heating", displayed: true)
    }
    if (cmd.value == 240){ //240 - 0xF0 = boost
    	state.thermostatMode = "qheating"
        state.thermostatOperatingState = "Boost"
    	event << createEvent(name: "thermostatMode", value: "qheating", displayed: true)
    	event << createEvent(name: "thermostatOperatingState", value: "Boost", displayed: true)
    }
    if (cmd.value == 0){ //0 - 0x00 = eco
    	state.thermostatMode = "Eco"
        state.thermostatOperatingState = "Eco"
    	event << createEvent(name: "thermostatMode", value: "Eco", displayed: true)
    	event << createEvent(name: "thermostatOperatingState", value: "Eco", displayed: true)
    }
    if (cmd.value == 15){ //15 - 0x0F = off
    	state.thermostatMode = "off"
        state.thermostatOperatingState = "off"
    	event << createEvent(name: "thermostatMode", value: "off", displayed: true)
    	event << createEvent(name: "thermostatOperatingState", value: "off", displayed: true)
    }
    //254 - 0xFE = direct valve contol mode
	log.info "Report recived basic mode is ${cmd.value}, ${state.thermostatMode}, ${state.thermostatOperatingState}"
    event
}

def zwaveEvent(physicalgraph.zwave.commands.thermostatmodev2.ThermostatModeReport cmd ) {
    def event = [ ]
    if (cmd.mode == 1){ //1 normall heat 0x01
    	state.thermostatMode = "heat"
        state.thermostatOperatingState = "heating"
    	event << createEvent(name: "thermostatMode", value: "heat", displayed: true)
    	event << createEvent(name: "thermostatOperatingState", value: "heating", displayed: true)
    }
    if (cmd.mode == 15){ //15 boost 0x0F
    	state.thermostatMode = "qheating"
        state.thermostatOperatingState = "Boost"
        event << createEvent(name: "thermostatMode", value: "qheating", displayed: true)
    	event << createEvent(name: "thermostatOperatingState", value: "Boost", displayed: true)
    }
    if (cmd.mode == 11){ //11 eco 11 0x0B
    	state.thermostatMode = "Eco"
        state.thermostatOperatingState = "Eco"
    	event << createEvent(name: "thermostatMode", value: "Eco", displayed: true)
    	event << createEvent(name: "thermostatOperatingState", value: "Eco", displayed: true)
    }
    if (cmd.mode == 0){ // 0 off 0x00
    	state.thermostatMode = "off"
        state.thermostatOperatingState = "off"
    	event << createEvent(name: "thermostatMode", value: "off", displayed: true)
    	event << createEvent(name: "thermostatOperatingState", value: "off", displayed: true)
    }
    log.info "Report recived mode is ${cmd.mode}, ${state.thermostatMode}, ${state.thermostatOperatingState}"
    event
}

def zwaveEvent(physicalgraph.zwave.commands.thermostatsetpointv2.ThermostatSetpointReport cmd) { //	Parsed ThermostatSetpointReport(precision: 2, reserved01: 0, scale: 0, scaledValue: 21.00, setpointType: 1, size: 2, value: [8, 52])
	def eventList = []
	state.scale = cmd.scale	// So we can respond with same format later, see setHeatingSetpoint()
	state.precision = cmd.precision
    def radiatorSetPoint = cmd.scaledValue
//todo manage trv vrs app
    if(cmd.setpointType == 1 && state.thermostatOperatingState != "Eco") { //this is the standard heating setpoint
		eventList << createEvent(name:"nextHeatingSetpoint", value: radiatorSetPoint, unit: getTemperatureScale(), displayed: true)
       	eventList << createEvent(name:"thermostatSetpoint", value: radiatorSetPoint.toString(), unit: getTemperatureScale(), displayed: false)
       	eventList << createEvent(name: "heatingSetpoint", value: radiatorSetPoint.toString(), unit: getTemperatureScale(), displayed: true)
    //to add -- descriptionText:discText
    log.info "Report recived $cmd NOT Eco"
    }
	
    if(cmd.setpointType == 11 && state.thermostatOperatingState == "Eco" ) {
    	eventList << createEvent(name: "heatingSetpoint", value: radiatorSetPoint.toString(), unit: getTemperatureScale(), displayed: true)
    log.info "Report recived $cmd Eco MODE"
    }
    log.debug "Report recived $cmd.setpointType"
    eventList
}

def temperatureUp() {
    //log.debug(device.currentValue("nextHeatingSetpoint").toBigDecimal())
	def nextTemp = device.currentValue("nextHeatingSetpoint").toBigDecimal() + 0.5
							// TODO: deal with Farenheit?
	if(nextTemp > 28) {		// It can't handle above 28, so don't allow it go above
		nextTemp = 28
	}
	sendEvent(name:"nextHeatingSetpoint", value: nextTemp, unit: getTemperatureScale(), displayed: true)	
    //runIn (5, "buffSetpoint",[data: [value: nextTemp]]) //, overwrite: true
    setHeatingSetpoint(nextTemp)
}
def temperatureDown() {
	def nextTemp = device.currentValue("nextHeatingSetpoint").toBigDecimal() - 0.5
	if(nextTemp < 8) {		// It can't go below 8, so don't allow it
		nextTemp = 8
	}
	sendEvent(name:"nextHeatingSetpoint", value: nextTemp, unit: getTemperatureScale(), displayed: true)	
   	//runIn (5, "buffSetpoint",[data: [value: nextTemp]]) //, overwrite: true
    setHeatingSetpoint(nextTemp)
}
def buffSetpoint(data) {
	log.debug "buff $data"
	def key = "value"
	def nextTemp = data[key]
    log.debug " buff nextTemp is $nextTemp"
	setHeatingSetpoint(nextTemp)
}

def setCoolingSetpoint(temp){
	log.trace "Set cooling setpoint temp of ${temp}, sending temp value to setHeatingSetpoint"
	setHeatingSetpoint(temp)
}

def setHeatingSetpoint(Double degrees) { //Double added
	def eventList = []
    def precision = state.precision ?: 2
    def deviceScale = state.scale ?: 0
	log.debug "set heatingpoint start $degrees"
	sendEvent(name:"nextHeatingSetpoint", value: degrees, unit: getTemperatureScale(), descriptionText: "Next heating setpoint is ${degrees}")
    def valueDegrees = degrees*10
//    eventList << zwave.thermostatSetpointV1.thermostatSetpointSet(precision: 1, scale: 0, scaledValue: degrees, setpointType: 1, size: 2, value: [0, valueDegrees.toInteger()])
  eventList << zwave.thermostatSetpointV2.thermostatSetpointSet(precision: precision, scale: deviceScale, scaledValue: degrees.toBigDecimal(), setpointType: 1)
    //eventList << zwave.thermostatSetpointV1.thermostatSetpointGet(setpointType: 0x01)
    eventList << zwave.thermostatSetpointV2.thermostatSetpointGet(setpointType: 1)
    log.trace "Setting Temp to ${degrees},  $eventList"
    secureSequence(eventList)
}

def lock() {
//log.trace("lock")    
    sendEvent(name: "protectionState", value: "locked", displayed: false)
    cmds << zwave.protectionV1.protectionSet(protectionState: 1)
    cmds << zwave.protectionV1.protectionGet()
	log.trace "lock $cmds" 
    secureSequence (cmds)
}

def unlock() {  
    sendEvent(name: "protectionState", value: "unlocked", displayed: false)
    cmds << zwave.protectionV1.protectionSet(protectionState: 0)
    cmds << zwave.protectionV1.protectionGet()
	log.trace "unlock $cmds" 
    secureSequence (cmds)
}

def boost() {
	sendEvent(name: "thermostatMode", value: "qheating", displayed: false)
	def eventList = []
    eventList << zwave.thermostatModeV2.thermostatModeSet(mode: 0x0F)
    eventList << zwave.thermostatModeV2.thermostatModeGet()
	log.trace "Boost On $eventList"
    secureSequence(eventList)
}
def boostoff() {
	log.trace "Boost Off"
    heat()
}

def ecoheat() {
    sendEvent(name: "thermostatMode", value: "esheating", displayed: false)
    //sendEvent(name: "thermostatOperatingState", value: "Eco", displayed: false)
    def eventList = []
    eventList << zwave.thermostatModeV2.thermostatModeSet(mode: 0x0B)
    eventList << zwave.thermostatModeV2.thermostatModeGet()
    log.trace "Eco Heat $eventList"
    secureSequence(eventList)
}
def ecooff() {
log.trace "eco Off"
    heat()
}

def on() {
    heat()
}

def heat() {
//log.trace "Heat"  
    sendEvent(name: "thermostatMode", value: "default", displayed: false)
        //sendEvent(name: "thermostatOperatingState", value: "Heating", displayed: false)
	cmds << zwave.thermostatModeV2.thermostatModeSet(mode: 0x01)
    cmds << zwave.thermostatModeV2.thermostatModeGet()
	log.trace "heat $cmds" 
    secureSequence (cmds)
}

def off() {
	def cmds = []
    sendEvent(name: "thermostatMode", value: "off", displayed: false)
        //sendEvent(name: "thermostatOperatingState", value: "Off", displayed: false)
	cmds << zwave.thermostatModeV2.thermostatModeSet(mode: 0x00)
    cmds << zwave.thermostatModeV2.thermostatModeGet()
	log.trace "OFF $cmds" 
    secureSequence (cmds)
}

def refresh() {
	log.trace "refresh"
	poll()
}
//input "ecoTemp", "number", title: "Eco Heat Temperature", description: "Temperature to heat to in Eco Mode (8 - 28°C)", range: "8..28", displayDuringSetup: false
            
def updated() {
    if (!state.updatedLastRanAt || new Date().time >= state.updatedLastRanAt + 2000) {
        state.updatedLastRanAt = new Date().time
        unschedule(refresh)
        unschedule(poll)
        log.trace "updated config state"
        sendEvent(name: "configure", value: "configdue", displayed: false)
     }
    else {
    	log.warn "update ran within the last 2 seconds"
    }
}

def poll() { // If you add the Polling capability to your device type, this command will be called approximately every 5 minutes to check the device's state
//log.debug "poll"
	def cmds = []
	
    if (!state.lastBatteryReportReceivedAt || (new Date().time) - state.lastBatteryReportReceivedAt > daysToTime(7)) {
		log.trace "POLL - Asking for battery report as over 7 days since"
       	cmds << zwave.batteryV1.batteryGet()     
	}
    
/*    if (!state.updatedLastRanAt || (new Date().time - state.updatedLastRanAt > daysToTime(7))){
    	log.trace " POLL - asking for config values since its been 7 days"
        state.updatedLastRanAt = new Date().time
       	cmds << zwave.configurationV1.configurationGet(parameterNumber:1)
    	cmds << zwave.configurationV1.configurationGet(parameterNumber:2)
    	cmds << zwave.configurationV1.configurationGet(parameterNumber:3)
    	cmds << zwave.configurationV1.configurationGet(parameterNumber:7)
    	cmds << zwave.configurationV1.configurationGet(parameterNumber:8)
	}
*/
	
    cmds <<	zwave.sensorMultilevelV1.sensorMultilevelGet()		// cmds <<	zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType:1)		// get temp , scale:1
    cmds <<	zwave.thermostatModeV2.thermostatModeGet()								// get mpde
    cmds <<	zwave.thermostatSetpointV2.thermostatSetpointGet(setpointType: 1)	// get setpoint 0x01
    cmds <<	zwave.thermostatSetpointV1.thermostatSetpointGet(setpointType: 11) //dont use the eco setpoint
    cmds << zwave.switchMultilevelV3.switchMultilevelGet()							// get valve position
    log.trace "POLL $cmds"
    secureSequence (cmds)
}

private daysToTime(days) {
	days*24*60*60*1000
}
// If you add the Configuration capability to your device type, this command will be called right after the device joins to set device-specific configuration commands.
def configure() {
log.trace "config"
def cmds = []
	   //secureSequence([
                cmds << zwave.configurationV1.configurationSet(configurationValue:  LCDinvert == "Yes" ? [0x01] : [0x00], parameterNumber:1, size:1, scaledConfigurationValue:  LCDinvert == "Yes" ? 0x01 : 0x00)//,
                cmds << zwave.configurationV1.configurationSet(configurationValue: LCDtimeout == null ? [0] : [LCDtimeout], parameterNumber:2, size:1, scaledConfigurationValue: LCDtimeout == null ? 0 :  LCDtimeout)//,
                cmds << zwave.configurationV1.configurationSet(configurationValue:  backlight == "Yes" ? [0x01] : [0x00], parameterNumber:3, size:1, scaledConfigurationValue:  backlight == "Yes" ? 0x01 : 0x00)//,
                cmds << zwave.configurationV1.configurationSet(configurationValue:  windowOpen == "Low" ? [0x01] : windowOpen == "Medium" ? [0x02] : windowOpen == "High" ? [0x03] : [0x00], parameterNumber:7, size:1, scaledConfigurationValue:  windowOpen == "Low" ? 0x01 : windowOpen == "Disabled" ? 0x00 : windowOpen == "High" ? 0x03 : 0x02)//,
                cmds << zwave.configurationV1.configurationSet(configurationValue: tempOffset == null ? [0] : [tempOffset*10], parameterNumber:8, size:1, scaledConfigurationValue: tempOffset == null ? 0 : tempOffset*10)//,
                cmds << zwave.thermostatSetpointV1.thermostatSetpointSet(precision: 1, reserved01: 0, scale: 0, scaledValue: ecoTemp == null ? 8 : ecoTemp, setpointType: 11, size: 2, value: ecoTemp == null ? [0, 80] : [0, ecoTemp*10])//,
                cmds << zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType:1, scale:1)//,  // get temp
                cmds << zwave.thermostatModeV2.thermostatModeGet()//,
                cmds << zwave.thermostatSetpointV1.thermostatSetpointGet(setpointType: 0x01)//,
                cmds << zwave.thermostatSetpointV1.thermostatSetpointGet(setpointType: 0x0B)//,
                cmds << zwave.configurationV1.configurationGet(parameterNumber:1)//,
                cmds << zwave.configurationV1.configurationGet(parameterNumber:2)//,
                cmds << zwave.configurationV1.configurationGet(parameterNumber:3)//,
                cmds << zwave.configurationV1.configurationGet(parameterNumber:7)//,
                cmds << zwave.configurationV1.configurationGet(parameterNumber:8)//,
                cmds << zwave.batteryV1.batteryGet()//,
        //])
    sendEvent(name: "configure", value: "configure", displayed: false)   
	log.trace "config"
    secureSequence(cmds)
}

private secure(physicalgraph.zwave.Command cmd) {
//log.debug "Seq - $cmd"
	zwave.securityV1.securityMessageEncapsulation().encapsulate(cmd).format()
}

private secureSequence(commands, delay=2000) {
//log.debug "SeSeq $commands"
	delayBetween(commands.collect{ secure(it) }, delay)
}
