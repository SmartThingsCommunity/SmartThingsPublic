/**
 *  Copyright 2015 Eurotronic
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
	definition (name: "Eurotronic Z-Wave Thermostatic Valve", namespace: "Eurotronic", author: "Eurotronic") {
		capability "Actuator"
		capability "Temperature Measurement"
		capability "Thermostat Heating Setpoint"
		capability "Thermostat Mode"
        capability "Thermostat Setpoint"
		capability "Sensor"
        capability "Switch Level"
        
		command "energySave"
        command "manual"
        command "setWakeUpInterval", ["number"]
        command "setEnergySavingSetpoint", ["number"]
        
        attribute "energySavingSetpoint", "number"

		fingerprint deviceId:"0x0806", inClusters:"0x43, 0x40, 0x31, 0x20, 0x26, 0x77, 0x80, 0x84, 0x72, 0x86"
    }
    
	tiles(scale: 1) {
    	
		valueTile("temperature", "device.temperature", width: 2, height: 2) {
			state("a", label:'${currentValue}°', icon: "st.Weather.weather2",
				backgroundColors:[
					[value: 0, color: "#153591"],
					[value: 7, color: "#1e9cbb"],
					[value: 15, color: "#90d2a7"],
					[value: 23, color: "#44b621"],
					[value: 29, color: "#f1d801"],
					[value: 35, color: "#d04e00"],
				]
			)
		}
		standardTile("mode", "device.thermostatMode") {
			state("off", label:'${name}', action:"heat", nextState:"heat", backgroundColor:"#808080", icon:"st.Seasonal Fall.seasonal-fall-008")
			state("heat", label:'${name}', action:"energySave", nextState:"energy save", backgroundColor:"#fdd470", icon:"st.Seasonal Winter.seasonal-winter-009")
			state("energy save", label:'save', action:"manual", nextState:"manual", backgroundColor:"#afd574", icon:"st.Transportation.transportation1")
            state("manual", label: '${name}', action:"off", nextState:"off", backgroundColor:"#8f00ff", icon:"st.Outdoor.outdoor18")
		}
        valueTile("battery", "device.battery",  width: 1, height: 1) {
        	state ("battery", label:'${currentValue}%', unit:"%", icon: "st.Health & Wellness.health9",
            	backgroundColors:[
					[value: 0, color: "#ce1010"],
					[value: 50, color: "#3535ba"],
					[value: 100, color: "#0d740d"]
				]
            )
    	}
		controlTile("heatSliderControl", "device.heatingSetpoint", "slider", height: 1, width: 2, range:"(13..50)") {
			state "a", action:"setHeatingSetpoint", backgroundColor:"#fdd470"
		}
		valueTile("heatingSetpoint", "device.heatingSetpoint") {
			state ("a", label:'${currentValue}°', backgroundColor:"#fdd470")
		}
        controlTile("energySavingControl", "device.energySavingSetpoint", "slider", height: 1, width: 2, range:"(13..50)") {
			state ("a", action:"setEnergySavingSetpoint", backgroundColor:"#afd574")
		}
		valueTile("energySavingSetpoint", "device.energySavingSetpoint" ) {
			state ("a", label:'${currentValue}°', backgroundColor:"#afd574")
		}
        controlTile("switchControlSlider", "device.switch", "slider", height: 1, width: 2) {
			state ("a", action:"setLevel", backgroundColor: "#8f00ff")
		}
		valueTile("switchValue", "device.switch") {
			state ("a", label:'${currentValue}%', backgroundColor:"#8f00ff")
		}
        
		main "temperature"
		details(["temperature", "mode", "battery", "heatSliderControl", "heatingSetpoint", "energySavingControl", "energySavingSetpoint", "switchControlSlider", "switchValue"])
	}
}

def parse(String description)
{
	log.debug "Parse description: $description"
    
    def result = []
    
    if (state.wakeUpInterval != 240 || state.nodeid != zwaveHubNodeId) {
    	result << response(zwave.wakeUpV2.wakeUpIntervalSet(nodeid: zwaveHubNodeId, seconds: 240))
        result << response(zwave.wakeUpV2.wakeUpIntervalGet().format())
    }
    
    def zwe = []
    
    if (description != "updated") {
    	zwe = zwaveEvent(zwave.parse(description, [0x20: 1, 0x26: 3, 0x31: 4, 0x40: 2, 0x43: 2, 0x77: 1, 0x80: 1, 0x84: 2, 0x72: 1, 0x86: 1]))    	
   	}
    
    return result + zwe
}

// Multilevel Switch
def zwaveEvent(physicalgraph.zwave.commands.switchmultilevelv3.SwitchMultilevelReport cmd){
	log.debug "Switch multilevel get $cmd"
    
    if (cmd.value == 255)
    	cmd.value = 100
    
    return createEvent(name: "switch", value: cmd.value)
}

def zwaveEvent(physicalgraph.zwave.commands.switchmultilevelv3.SwitchMultilevelStartLevelChange cmd){
	log.debug "SwitchMultilevelStartLevelChange notification $cmd"
}

def zwaveEvent(physicalgraph.zwave.commands.switchmultilevelv3.SwitchMultilevelStopLevelChange cmd){
	log.debug "SwitchMultilevelStopLevelChange notification $cmd"
}

//Multilevel Sensor
def zwaveEvent(physicalgraph.zwave.commands.sensormultilevelv4.SensorMultilevelReport cmd){
	log.debug "Sensor multilevel get $cmd"
    def map = [:]
	if (cmd.sensorType == 1) {
		map.value = cmd.scaledSensorValue
		map.unit = cmd.scale
		map.name = "temperature"
    }
    return createEvent(map)
}

//Thermostat
def zwaveEvent(physicalgraph.zwave.commands.thermostatmodev2.ThermostatModeReport cmd){
	log.debug "Thermostat mode notification $cmd"
    def map = [:]
	switch (cmd.mode) {
		case physicalgraph.zwave.commands.thermostatmodev2.ThermostatModeReport.MODE_OFF:
			map.value = "off"
			break
		case physicalgraph.zwave.commands.thermostatmodev2.ThermostatModeReport.MODE_HEAT:
			map.value = "heat"
			break
		case physicalgraph.zwave.commands.thermostatmodev2.ThermostatModeReport.MODE_ENERGY_SAVE_HEAT:
			map.value = "energy save"
			break
		case 0x1F:
			map.value = "manual"
			break
	}
	map.name = "thermostatMode"
	return createEvent(map)
}

def zwaveEvent(physicalgraph.zwave.commands.thermostatsetpointv2.ThermostatSetpointReport cmd){
	log.debug "ThermostatSetpointReport notification $cmd"
    
	def map = [:]
	map.value = cmd.scaledValue
	map.unit = cmd.scale
	switch (cmd.setpointType) {
		case 1:
			map.name = "heatingSetpoint"
			break;
		case 11:
			map.name = "energySavingSetpoint"
			break;
		default:
			return [:]
	}
	// So we can respond with same format
	state.size = cmd.size
	state.scale = cmd.scale
	state.precision = cmd.precision
	return createEvent(map)
}

//Battery
def zwaveEvent(physicalgraph.zwave.commands.batteryv1.BatteryReport cmd){
	log.debug "Battery notification $cmd"
    return createEvent(name: "battery", value: cmd.batteryLevel)
}

//Wake up
def zwaveEvent(physicalgraph.zwave.commands.wakeupv2.WakeUpIntervalReport cmd){
	log.debug "WakeUpInterval notification $cmd"
    state.nodeid = cmd.nodeid
    state.wakeUpInterval = cmd.seconds
    return createEvent(name: "wakeUpIntervalSeconds", value: cmd.seconds)
}

def zwaveEvent(physicalgraph.zwave.commands.wakeupv2.WakeUpNotification cmd){
	log.debug "Wake up notification $cmd $state"
    def result = []
    
    if (state.modeCommand) {
    	result << response(state.modeCommand)
        result << response(zwave.thermostatModeV2.thermostatModeGet().format())
        state.modeCommand = null
    }
    
    if (state.setLevelCommand) {
    	result << response(state.setLevelCommand)
    	result << response(zwave.switchMultilevelV3.switchMultilevelGet().format())
        state.setLevelCommand = null
    }
    
    if (state.setHeatingSetpointCommand) {
    	result << response(state.setHeatingSetpointCommand)
        result << response(zwave.thermostatSetpointV2.thermostatSetpointGet(setpointType: 1).format())
    	state.setHeatingSetpointCommand = null
    }
    
    if (state.setEnergySavingSetpointCommand) {
    	result << response(state.setEnergySavingSetpointCommand)
        result << response(zwave.thermostatSetpointV2.thermostatSetpointGet(setpointType: 11).format())
    	state.setEnergySavingSetpointCommand = null
    }
    
    //For some reason response("delay 1200") is not functional which is really bad because hub is 
    //not able to request all the information from device which it needs. All the commands are
    //sent at the same time. 
    result << response(zwave.sensorMultilevelV4.sensorMultilevelGet().format())
    result << response(zwave.batteryV1.batteryGet().format())
    result << response(zwave.switchMultilevelV3.switchMultilevelGet().format())
    //result << response(zwave.thermostatSetpointV2.thermostatSetpointGet(setpointType: 1).format())
    //result << response(zwave.thermostatSetpointV2.thermostatSetpointGet(setpointType: 11).format())
    //result << response(zwave.thermostatModeV2.thermostatModeGet().format())
    result << response(zwave.wakeUpV1.wakeUpNoMoreInformation().format())
    result
}

//Unexpected
def zwaveEvent(physicalgraph.zwave.Command cmd) {
	log.warn "Unexpected zwave command $cmd"
}

// Command Implementations
def off() {
	log.debug "Thermostat mode setting to off"
	state.modeCommand = zwave.thermostatModeV2.thermostatModeSet(mode: 0).format()
}

def heat() {
	log.debug "Thermostat mode setting to heat"
    state.modeCommand = zwave.thermostatModeV2.thermostatModeSet(mode: 1).format()
}

def energySave() {
	log.debug "Thermostat mode setting to energy save"
    state.modeCommand = zwave.thermostatModeV2.thermostatModeSet(mode: 11).format()
}

def manual() {
	log.debug "Thermostat mode setting to manual"
    state.modeCommand = zwave.thermostatModeV2.thermostatModeSet(mode: 31).format()
}

def setLevel(double lvl){    
    //by Eurotronic specification 100% is 0xFF and 99% is 0x63
    if (lvl == 100.0)
    	lvl = 255
    
    log.debug "Changing switch level to $lvl"

    state.setLevelCommand = zwave.switchMultilevelV3.switchMultilevelSet(value: 256).format()
}

def setHeatingSetpoint(double sp){
	log.debug "Setting setpoint to $sp"
    
	def map  = [ 
    	precision: 		1,
        scale: 			0,
        scaledValue:	sp,
        setpointType:	1,
        size:			2,
   	]
    	
    state.setHeatingSetpointCommand = zwave.thermostatSetpointV2.thermostatSetpointSet(map).format()
}

def setEnergySavingSetpoint(double sp){
	log.debug "Setting energy saving setpoint to $sp"
    
    def map  = [ 
    	precision: 		1,
        scale: 			0,
        scaledValue:	sp,
        setpointType:	11,
        size:			2,
   	]
    	
    state.setEnergySavingSetpointCommand = zwave.thermostatSetpointV2.thermostatSetpointSet(map).format()
}
