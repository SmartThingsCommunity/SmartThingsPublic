/**
 *  Copyright 2015 SmartThings
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
	definition (name: "TKB Z-Wave Thermostat", namespace: "Mark-C-UK", author: "mark c") {
		capability "Actuator"
		capability "Temperature Measurement"
		capability "Thermostat"
        capability "Thermostat Cooling Setpoint"	//command - setCoolingSetpoint - having both alows extra settings in routines
        capability "Thermostat Heating Setpoint" 
        capability "Thermostat Mode"
        capability "Thermostat Operating State"
		capability "Refresh"
		capability "Sensor"
		capability "Health Check"
        capability "Switch"
		
        attribute "thermostatTemperatureSetpoint", "String"						//need for google
		attribute "nextHeatingSetpoint", "String"
        
		command "temperatureUp"
		command "temperatureDown"
		command "poll"

		fingerprint deviceId: "0x08"
		fingerprint inClusters: "0x43,0x40,0x44,0x31"
		fingerprint mfr:"0039", prod:"0011", model:"0001", deviceJoinName: "Honeywell Z-Wave Thermostat"
		fingerprint mfr:"008B", prod:"5452", model:"5439", deviceJoinName: "Trane Thermostat"
		fingerprint mfr:"008B", prod:"5452", model:"5442", deviceJoinName: "Trane Thermostat"
		fingerprint mfr:"008B", prod:"5452", model:"5443", deviceJoinName: "American Standard Thermostat"
	}

	tiles {
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
				attributeState "heating", backgroundColor:"#d04e00"
				attributeState "idle",  backgroundColor:"#cccccc"
                attributeState "pending heat",  backgroundColor:"#44b621"
			}

			tileAttribute("device.thermostatMode", key: "THERMOSTAT_MODE") { //need
				attributeState "default", label:'misc mode'
                attributeState "emergency heat", label:'${currentValue}' 
              	attributeState "eco", label:'${name}' 
                attributeState "cool", label:'${name}' 
                attributeState "off", label:'${name}'
                attributeState "heat", label:'${name}' 
			}

			tileAttribute("device.thermostatSetpoint", key: "HEATING_SETPOINT") { //unit:"C", 
				attributeState("thermostatSetpoint", label:'${currentValue}°', defaultState: true, backgroundColors:[
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
        standardTile("operatingState", "device.thermostatOperatingState", width: 2, height: 2) { // duplication for feed (icons colours)
			state "heating", 		label:'${name}', backgroundColor:"#e86d13",	icon:"st.thermostat.heat" //backgroundColor:"#d04e00"
			state "idle", 			label:'${name}', backgroundColor:"#00a0dc", icon:"st.thermostat.heating-cooling-off"
            state "pending heat", 	label:'idle', backgroundColor:"#44b621", icon:"st.nest.nest-leaf"
		}
		standardTile("mode", "device.thermostatMode", width:2, height:2, inactiveLabel: false, decoration: "flat") {
			state "off", action:"on", nextState:"...", icon: "st.thermostat.heating-cooling-off"
			state "heat", action:"off", nextState:"...", icon: "st.thermostat.heat"
			state "cool", action:"off", nextState:"...", icon: "st.thermostat.cool"
			state "auto", action:"off", nextState:"...", icon: "st.thermostat.auto"
			state "emergency heat", action:"off", nextState:"...", icon: "st.thermostat.emergency-heat"
			state "...", label: "Updating...",nextState:"...", backgroundColor:"#ffffff"
		}

		
		valueTile("heatingSetpoint", "device.heatingSetpoint", width:4, height:1, inactiveLabel: false, decoration: "flat") {
			state "heatingSetpoint", label:'Set Point ${currentValue}°', backgroundColor:"#ffffff"
		}
		controlTile("heatSliderControl", "device.heatingSetpoint", "slider", height: 2, width: 2, inactiveLabel: false, range:"(8..30)") {
			state ("heatingSetpoint", action:"setHeatingSetpoint") //need to send to a lag
		}
		
        valueTile("temp", "device.temperature", width: 2, height: 2, inactiveLabel: true) { // hear to enable it to show in activity feed
		state ("temp", label:'${currentValue}°',  defaultState: true, backgroundColors:[
			[value: 0, color: "#153591"],
			[value: 10, color: "#1e9cbb"],
			[value: 13, color: "#90d2a7"],
			[value: 17, color: "#44b621"],
			[value: 20, color: "#f1d801"],
			[value: 25, color: "#d04e00"],
			[value: 29, color: "#bc2323"],
		])
	}
		standardTile("refresh", "device.thermostatMode", width:2, height:1, inactiveLabel: false, decoration: "flat") {
			state "default", action:"refresh.refresh", icon:"st.secondary.refresh"
		}
		main "temperature"
		details(["temperature", "heatingSetpoint", "refresh", "operatingState", "mode", "heatSliderControl"])
	}
}

def installed() {
	// Configure device
	def cmds = [new physicalgraph.device.HubAction(zwave.associationV1.associationSet(groupingIdentifier:1, nodeId:[zwaveHubNodeId]).format()),
			new physicalgraph.device.HubAction(zwave.manufacturerSpecificV2.manufacturerSpecificGet().format())]
	sendHubCommand(cmds)
	runIn(3, "initialize", [overwrite: true])  // Allow configure command to be sent and acknowledged before proceeding
}

def updated() {
	// If not set update ManufacturerSpecific data
	if (!getDataValue("manufacturer")) {
		sendHubCommand(new physicalgraph.device.HubAction(zwave.manufacturerSpecificV2.manufacturerSpecificGet().format()))
		runIn(2, "initialize", [overwrite: true])  // Allow configure command to be sent and acknowledged before proceeding
	} else {
		initialize()
	}
    log.debug "updated"
}

def initialize() {
	// Device-Watch simply pings if no device events received for 32min(checkInterval)
	sendEvent(name: "checkInterval", value: 2 * 15 * 60 + 2 * 60, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])
    log.debug "initialize"
	pollDevice()
}

def parse(String description){
//0x70 = Configuration v1, 0x72 = Manufacturer Specific v1, 0x31 = Multilevel Sensor v5, 0x26 = MultiLevel Switch v1, 
// 0x71 = Notification v8, 0x75 = Protection v2, 0x98 = Security v2, 0x40 = Thermostat Mode, 0x43 = Thermostat Setpoint v3, 0x86 = Version v1

	def result = null
	if (description == "updated") { log.debug "updated $description" } 
    else {
		def zwcmd = zwave.parse(description)//, [0x42:1, 0x43:2, 0x31: 3]
       //log.debug "incoming- $zwcmd"
		if (zwcmd) {
			result = zwaveEvent(zwcmd)
		} else {
			log.debug "$device.displayName couldn't parse $description"
		}
	}
	if (!result) {
		return []
	}
	return [result]
}

// ---- Handle setpoints ----------------------------
def zwaveEvent(physicalgraph.zwave.commands.thermostatsetpointv2.ThermostatSetpointReport cmd) {
	//def event = []
    state.size = cmd.size			// So we can respond with same format
	state.scale = cmd.scale			// So we can respond with same format
	state.precision = cmd.precision	// So we can respond with same format
	def cmdScale = cmd.scale == 1 ? "F" : "C"
	def setpoint = getTempInLocalScale(cmd.scaledValue, cmdScale)
	def degunit = getTemperatureScale()
	if (cmd.setpointType == 1) {
    	sendEvent(name: "nextHeatingSetpoint", 				value: setpoint, unit: degunit, displayed: true)
		sendEvent(name: "heatingSetpoint", 					value: setpoint, unit: degunit, displayed: true)
    	sendEvent(name: "thermostatSetpoint", 				value: setpoint, unit: degunit, displayed: false)
    	sendEvent(name: "coolingSetpoint", 					value: setpoint, unit: degunit, displayed: false)
    	sendEvent(name: "thermostatTemperatureSetpoint", 	value: setpoint, unit: degunit, displayed: false)
	}
    else {
    	log.warn " unexpected setpint type ${cmd.setpointType} - $cmd"
    }
    log.info "setpoint Report - $setpoint, $degunit, " // ---- $eventList"
}
def temperatureUp() {
	def nextTemp = device.currentValue("nextHeatingSetpoint").toBigDecimal() + 1			
	sendEvent(name:"nextHeatingSetpoint", value: nextTemp, unit: getTemperatureScale(), displayed: false)	
    runIn (5, "buffSetpoint",[data: [value: nextTemp], overwrite: true])
}
def temperatureDown() {
	def nextTemp = device.currentValue("nextHeatingSetpoint").toBigDecimal() - 1
	sendEvent(name:"nextHeatingSetpoint", value: nextTemp, unit: getTemperatureScale(), displayed: false)	
   	runIn (5, "buffSetpoint",[data: [value: nextTemp], overwrite: true])
}
def buffSetpoint(data) {
	def key = "value"
	def nextTemp = data[key]
	setHeatingSetpoint(nextTemp)
}

def setCoolingSetpoint(temp){
	log.trace "Set cooling setpoint temp of ${temp}, sending temp value to setHeatingSetpoint"
	setHeatingSetpoint(temp)
}
def setHeatingSetpoint(degrees) {

	if(degrees < 8) 	{ degrees = 8	}
    if(degrees > 28) 	{ degrees = 28	}
	
    def cmds = []
    def precision = state.precision ?: 2
    def deviceScale = state.scale ?: 0
	sendEvent(name:"nextHeatingSetpoint", value: degrees, unit: getTemperatureScale(), descriptionText: "App/Auto heating setpoint is ${degrees}", displayed: true, isStateChange:true)
	cmds << zwave.thermostatSetpointV2.thermostatSetpointSet(precision: precision, scale: deviceScale, scaledValue: degrees, setpointType: 1)
	cmds << zwave.thermostatSetpointV2.thermostatSetpointGet(setpointType: 1)
	cmds << zwave.thermostatOperatingStateV1.thermostatOperatingStateGet().format()
	log.trace "Setting Temp to ${degrees},  $cmds"
	sendHubCommand(cmds, 1000)
}
// ----------- handle setpoints end -------
// ----------- handle temprature --------
def zwaveEvent(physicalgraph.zwave.commands.sensormultilevelv5.SensorMultilevelReport cmd) {
	def map = [:]
	if (cmd.sensorType == 1) { 
		map.name = "temperature"
		map.value = getTempInLocalScale(cmd.scaledSensorValue, cmd.scale == 1 ? "F" : "C")
		map.unit = getTemperatureScale()
	} 
    else if (cmd.sensorType == 5) {
    	map.name = "humidity"
		map.value = cmd.scaledSensorValue
		map.unit = "%"
	}
    log.info "sensor Report $map"
    createEvent(map)
}
// ---- handle operating state --------
def zwaveEvent(physicalgraph.zwave.commands.thermostatoperatingstatev2.ThermostatOperatingStateReport cmd) { //device.currentValue("thermostatMode")
	def map = [name: "thermostatOperatingState"]
	if ( cmd.operatingState == 0 ) { 
    	if (device.currentValue("thermostatMode") == "heat") { map.value = "pending heat" } //idle
    	else { map.value = "idle"}
    }
    else if ( cmd.operatingState == 1 ) { map.value = "heating" }
    else {map.value = "unknown"}
    log.info "thermostatoperatingstate Report - ${cmd.operatingState} - ${map.value} -- Heating or Idle --"
	sendHubCommand(new physicalgraph.device.HubAction(zwave.thermostatModeV2.thermostatModeGet().format())) // Makes sure we have the correct thermostat mode
	sendEvent(map)
}
/// ---------- Handle mode -----------
def zwaveEvent(physicalgraph.zwave.commands.thermostatmodev2.ThermostatModeReport cmd) {
	def map = [name: "thermostatMode"]
	if ( cmd.mode == 0 ) { map.value = "off"  }
    else if ( cmd.mode == 1 ) { map.value = "heat" }
    else if ( cmd.mode == 2 ) { map.value = "cool" }
    else if ( cmd.mode == 3 ) { map.value = "auto" }
    else if ( cmd.mode == 4 ) { map.value = "emergency heat" }
	else {map.value = "unknown"}
    log.info " ThermostatMode Report ${cmd.mode} ${map.value}, $map"
    if (map.value != "off") { sendEvent(name:"switch", value:"on")}
	sendEvent(map)
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd) {
	def Bmode = ""
    def Bswitch = ""
    if (cmd.value == 255) {
    	Bmode = "heat"
    	Bswitch = "on" 
    }
    else {
    	Bmode = "off"
    	Bswitch = "off"
    }
    log.info "Basic Report: $cmd - switch=$Bswitch - mode=$Bmode"
    sendEvent(name:"thermostatMode",value:Bmode)
    sendEvent(name:"switch",value:Bswitch)
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
	log.warn "Unexpected zwave command $cmd"
}



// Command Implementations
def poll() { runIn(2, "pollDevice", [overwrite: true]) }
def refresh() { runIn(2, "pollDevice", [overwrite: true]) }
def pollDevice() {
	def cmds = []
	cmds << new physicalgraph.device.HubAction(zwave.thermostatModeV2.thermostatModeGet().format())
	cmds << new physicalgraph.device.HubAction(zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType:1, scale:1).format()) // current temperature //2
    cmds << new physicalgraph.device.HubAction(zwave.thermostatOperatingStateV1.thermostatOperatingStateGet().format())
	cmds << new physicalgraph.device.HubAction(zwave.thermostatSetpointV1.thermostatSetpointGet(setpointType: 1).format())
    def time = getTimeAndDay()
	if (time) {
		cmds << new physicalgraph.device.HubAction(zwave.clockV1.clockSet(time).format())
	}
    //cmds << new physicalgraph.device.HubAction(zwave.clockV1.clockGet().format())
    log.trace "POLL Devices cmds are $cmds"
    sendHubCommand(cmds,2000) //500
}

def ping() { //PING is used by Device-Watch in attempt to reach the Device
	log.trace "ping() called" // Just get Operating State there's no need to flood more commands
	sendHubCommand(new physicalgraph.device.HubAction(zwave.thermostatOperatingStateV1.thermostatOperatingStateGet().format()))
}


def setThermostatMode(String value) {
	setGetThermostatMode(value)
}

def setGetThermostatMode(data) {
	def cmds = [new physicalgraph.device.HubAction(zwave.thermostatModeV2.thermostatModeSet(mode: data ).format()),
			new physicalgraph.device.HubAction(zwave.thermostatModeV2.thermostatModeGet().format())]
	sendHubCommand(cmds,1000)
}

def off() 			{ setGetThermostatMode(0) }
def on() 			{ setGetThermostatMode(1) }
def heat() 			{ setGetThermostatMode(1) }
def emergencyHeat() { setGetThermostatMode(0x0F) }
def cool() 			{ setGetThermostatMode(11) }
def auto() 			{ setGetThermostatMode(1) }

// Get stored temperature from currentState in current local scale
def getTempInLocalScale(state) {
	def temp = device.currentState(state)
	if (temp && temp.value && temp.unit) {
		return getTempInLocalScale(temp.value.toBigDecimal(), temp.unit)
	}
	return 0
}

// get/convert temperature to current local scale
def getTempInLocalScale(temp, scale) {
	if (temp && scale) {
		def scaledTemp = convertTemperatureIfNeeded(temp.toBigDecimal(), scale).toDouble()
		return (getTemperatureScale() == "F" ? scaledTemp.round(0).toInteger() : roundC(scaledTemp))
	}
	return 0
}

def getTempInDeviceScale(state) {
	def temp = device.currentState(state)
	if (temp && temp.value && temp.unit) {
		return getTempInDeviceScale(temp.value.toBigDecimal(), temp.unit)
	}
	return 0
}

def getTempInDeviceScale(temp, scale) {
	if (temp && scale) {
		def deviceScale = (state.scale == 1) ? "F" : "C"
		return (deviceScale == scale) ? temp :
				(deviceScale == "F" ? celsiusToFahrenheit(temp).toDouble().round(0).toInteger() : roundC(fahrenheitToCelsius(temp)))
	}
	return 0
}

def roundC (tempC) {
	return (Math.round(tempC.toDouble() * 2))/2
}
//setup
def zwaveEvent(physicalgraph.zwave.commands.manufacturerspecificv2.ManufacturerSpecificReport cmd) {
	if (cmd.manufacturerName) { updateDataValue("manufacturer", cmd.manufacturerName) }
	if (cmd.productTypeId) { updateDataValue("productTypeId", cmd.productTypeId.toString()) }
	if (cmd.productId) { updateDataValue("productId", cmd.productId.toString()) }
}

///// ----------------- MODES out and in ------------- //////////
def getSupportedModes() { //ask for modes
	def cmds = []
	cmds << new physicalgraph.device.HubAction(zwave.thermostatModeV2.thermostatModeSupportedGet().format())
	sendHubCommand(cmds)
}
def zwaveEvent(physicalgraph.zwave.commands.thermostatmodev2.ThermostatModeSupportedReport cmd) { // mode report
	def supportedModes = []
	if(cmd.off) { supportedModes << "off" }
	if(cmd.heat) { supportedModes << "heat" }
	if(cmd.cool) { supportedModes << "cool" }
	if(cmd.auto) { supportedModes << "auto" }
	if(cmd.auxiliaryemergencyHeat) { supportedModes << "emergency heat" }
	state.supportedModes = supportedModes
	sendEvent(name: "supportedThermostatModes", value: supportedModes, displayed: false)
}

private getTimeAndDay() {
	def timeNow = now()
	// Need to check that location have timeZone as SC may have created the location without setting it
	// Don't update clock more than once a day
	if (location.timeZone && (!state.timeClockSet || (24 * 60 * 60 * 1000 < (timeNow - state.timeClockSet)))) {
		def currentDate = Calendar.getInstance(location.timeZone)
		state.timeClockSet = timeNow
        log.debug "get time and day ${currentDate.get(Calendar.HOUR_OF_DAY)} : ${currentDate.get(Calendar.MINUTE)}"
		return [hour: currentDate.get(Calendar.HOUR_OF_DAY), minute: currentDate.get(Calendar.MINUTE), weekday: currentDate.get(Calendar.DAY_OF_WEEK)]
	}
}