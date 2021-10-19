/**
 *  Copyright 2021 Sarkis Kabrailian
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
	definition (name: "HELTUN HLS01 Thermostat", namespace: "HELTUN", author: "Sarkis Kabrailian", cstHandler: true) {
		capability "Energy Meter"
		capability "Power Meter"
		capability "Temperature Measurement"
		capability "Thermostat Heating Setpoint"
		capability "Thermostat Mode"
		capability "Thermostat Operating State"
		capability "Voltage Measurement"    
		capability "Configuration"
		capability "Health Check" 
		capability "Refresh"

		fingerprint mfr: "0344", prod: "0004", model: "000A", inClusters: "0x5E,0x85,0x59,0x8E,0x55,0x86,0x72,0x5A,0x73,0x87,0x98,0x9F,0x6C,0x32,0x70,0x42,0x40,0x43,0x31,0x81,0x71,0x22,0x7A", deviceJoinName: "HELTUN HLS01 Thermostat"
	}
    preferences {
		section { 
			input (
				type: "paragraph",
				element: "paragraph",
				title: "Parameter Configuration:",
				description: "Set the Parameter ID and the corresponding Value to configure the parameters"
			)

			input (
				name: "ParamId",
				title: "Parameter ID:",
				type: "number",
				range: "0..65536",
				required: false
			)
            
			input (
				name: "ParamValue",
				title: "Parameter Value:",
				type: "number",
				range: "0..2147483647",
				required: false
			)
		}
	}
}

def updated() {
	state.confSet = [
		parameterNumber: null,
		configurationValue: null
	]    
	if ((settings.ParamId != null) & (settings.ParamValue != null) & (settings.ParamValue != state.confSet.configurationValue) ) {         
		state.confSet = [
			parameterNumber: settings.ParamId.toInteger(),
			configurationValue: settings.ParamValue
		]
	sendHubCommand(zwave.configurationV2.configurationGet(parameterNumber: state.confSet.parameterNumber))
	}   
}

def zwaveEvent(physicalgraph.zwave.commands.configurationv2.ConfigurationReport cmd) {
	state.confReport = [
		parameterNumber: cmd.parameterNumber,
		size: cmd.size,
		configurationValue: cmd.configurationValue
	]
	if (state.confSet.parameterNumber == state.confReport.parameterNumber && state.confSet.configurationValue != state.confReport.configurationValue[0]) {
	sendHubCommand(zwave.configurationV2.configurationSet(parameterNumber: state.confSet.parameterNumber, size: state.confReport.size, scaledConfigurationValue: state.confSet.configurationValue))
	}
}

def parse(String description) {
	def result = null
	def cmd = zwave.parse(description)
	if (cmd) {result = zwaveEvent(cmd)}
	return result

}

def zwaveEvent(physicalgraph.zwave.commands.thermostatmodev2.ThermostatModeReport cmd) {
	def locaScale = getTemperatureScale() //HubScale   
	def deviceMode = modeMap[cmd.mode.toInteger()]
	sendEvent(name: "thermostatMode", data:[supportedThermostatModes: state.supportedModes], value: deviceMode)      
	if (cmd.mode == 0) {sendEvent(name: "heatingSetpoint", value: 0, unit: locaScale)} 
	sendHubCommand(new physicalgraph.device.HubAction(zwave.thermostatSetpointV2.thermostatSetpointGet(setpointType: cmd.mode.toInteger()).format())) //getSetpoint       
}

def zwaveEvent(physicalgraph.zwave.commands.sensormultilevelv5.SensorMultilevelReport cmd) {
	def locaScale = getTemperatureScale() //HubScale
	def map = [:]
	if (1 == cmd.sensorType) {
		def deviceScale = (cmd.scale == 1) ? "F" : "C" //DeviceScale
		def deviceTemp = cmd.scaledSensorValue
		def scaledTemp = (deviceScale == locaScale) ? deviceTemp : (deviceScale == "F" ? roundC(fahrenheitToCelsius(deviceTemp)) : celsiusToFahrenheit(deviceTemp).toDouble().round(0).toInteger())
		map.name = "temperature"
		map.value = scaledTemp
		map.unit = locaScale
		sendEvent(map)
	}
}

def zwaveEvent(physicalgraph.zwave.commands.meterv3.MeterReport cmd) {
	def map = [:]
	if (cmd.meterType == 1) {
		if (cmd.scale == 0) {
			map.name = "energy"
			map.value = cmd.scaledMeterValue
			map.unit = "kWh"
			sendEvent(map)
		}else if (cmd.scale == 2) {
			map.name = "power"
			map.value = Math.round(cmd.scaledMeterValue)
			map.unit = "W"
			sendEvent(map)
		}else if (cmd.scale == 4) {
			map.name = "voltage"
			map.value = Math.round(cmd.scaledMeterValue)
			map.unit = "V"
			sendEvent(map)
		}        
	}
}

def zwaveEvent(physicalgraph.zwave.commands.thermostatoperatingstatev2.ThermostatOperatingStateReport cmd) {
	def state = (cmd.operatingState == 1) ? "heating" : "idle" //DeviceScale
	sendEvent(name: "thermostatOperatingState", value: state)
}

def zwaveEvent(physicalgraph.zwave.commands.thermostatsetpointv2.ThermostatSetpointReport cmd) {   
	def locaScale = getTemperatureScale() //HubScale
	def deviceScale = (cmd.scale == 1) ? "F" : "C" //DeviceScale
	def deviceTemp = cmd.scaledValue
	def setPoint = (deviceScale == locaScale) ? deviceTemp : (deviceScale == "F" ? roundC(fahrenheitToCelsius(deviceTemp)) : celsiusToFahrenheit(deviceTemp).toDouble().round(0).toInteger())
	def mode = modeMap[device.currentValue("thermostatMode")]
    if (mode == 0) {setPoint = 0}    
	sendEvent(name: "heatingSetpoint", value: setPoint, unit: locaScale)   
}

def zwaveEvent(physicalgraph.zwave.commands.thermostatmodev2.ThermostatModeSupportedReport cmd) {
	def tSupportedModes = []
	if(cmd.heat) { tSupportedModes << "heat" }
	if(cmd.autoChangeover) { tSupportedModes << "autochangeover" }
	if(cmd.dryAir) { tSupportedModes << "dryair" }
	if(cmd.energySaveHeat) { tSupportedModes << "energysaveheat" }
	if(cmd.away) { tSupportedModes << "away" }
	if(cmd.off) { tSupportedModes << "off" }    
	state.supportedModes = tSupportedModes
	sendEvent(name: "supportedThermostatModes", value: tSupportedModes, displayed: false)
}

def setHeatingSetpoint(tValue) {
	def cmds = []
	def mode = device.currentValue("thermostatMode")
	def currentMode = modeMap[mode]
	def temp = state.heatingSetpoint = tValue.toDouble() //temp got fromm the app
	def tempInC = (getTemperatureScale() == "F" ? roundC(fahrenheitToCelsius(temp)) : temp) //If not C, Convert to C     
	cmds << new physicalgraph.device.HubAction(zwave.thermostatSetpointV2.thermostatSetpointSet(setpointType: currentMode, scale: 0, precision: 1, scaledValue: tempInC).format())
	
    // Sync temp, opState, setPoint
	cmds << new physicalgraph.device.HubAction(zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType:1).format())
	cmds << new physicalgraph.device.HubAction(zwave.thermostatOperatingStateV2.thermostatOperatingStateGet().format())
	cmds << new physicalgraph.device.HubAction(zwave.thermostatSetpointV2.thermostatSetpointGet(setpointType: currentMode).format())
	sendHubCommand(cmds)
}

def setThermostatMode(String value) {
	def cmds = []
	cmds << new physicalgraph.device.HubAction(zwave.thermostatModeV2.thermostatModeSet(mode: modeMap[value]).format())
	cmds << new physicalgraph.device.HubAction(zwave.thermostatModeV2.thermostatModeGet().format())
	cmds << new physicalgraph.device.HubAction(zwave.thermostatSetpointV2.thermostatSetpointGet(setpointType: modeMap[value]).format())
	sendHubCommand(cmds)
}

def getModeMap() {
[
	"off": 0,
	"heat": 1,
    "autochangeover": 10,
    "away": 13,
    "energysaveheat": 11,
    "dryair": 8,
    
    0 : "off",
    1 : "heat",
    10 : "autochangeover",
    13 : "away",
    11 : "energysaveheat",
    8 : "dryair"
    
]}

def roundC (tempInC) {
	return (Math.round(tempInC.toDouble() * 2))/2
}

def refresh() {
	def cmds = []
	cmds << new physicalgraph.device.HubAction(zwave.thermostatModeV2.thermostatModeGet().format())						// get thermostatmode
	cmds << new physicalgraph.device.HubAction(zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType:1).format())		// Temperature
	cmds << new physicalgraph.device.HubAction(zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType:3).format())		// Humidity
	cmds << new physicalgraph.device.HubAction(zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType:5).format())		// Illuminance
	cmds << new physicalgraph.device.HubAction(zwave.meterV3.meterGet(scale: 0).format())								// get kWh
	cmds << new physicalgraph.device.HubAction(zwave.meterV3.meterGet(scale: 2).format())								// get Watts
	cmds << new physicalgraph.device.HubAction(zwave.meterV3.meterGet(scale: 4).format())								// get Voltage    
	cmds << new physicalgraph.device.HubAction(zwave.thermostatOperatingStateV2.thermostatOperatingStateGet().format())	// get Thermostat Operating State
	cmds << new physicalgraph.device.HubAction(zwave.clockV1.clockGet().format())										// get clock
	sendHubCommand(cmds, 1200)
}

def ping() {
	refresh()
}

def configure() {
	delayBetween([
		zwave.multiChannelAssociationV2.multiChannelAssociationRemove(groupingIdentifier: 1).format(),
		zwave.multiChannelAssociationV2.multiChannelAssociationSet(groupingIdentifier: 1, nodeId: 1).format(),
        zwave.thermostatModeV2.thermostatModeSupportedGet().format(),
		ping()
	], 3000)
}

def zwaveEvent(physicalgraph.zwave.commands.clockv1.ClockReport cmd) {
	def currDate = Calendar.getInstance(location.timeZone)
	def time = [hour: currDate.get(Calendar.HOUR_OF_DAY), minute: currDate.get(Calendar.MINUTE), weekday: currDate.get(Calendar.DAY_OF_WEEK)]
	if ((time.hour != cmd.hour) || (time.minute != cmd.minute) || (time.weekday != cmd.weekday)){
		sendHubCommand(new physicalgraph.device.HubAction(zwave.clockV1.clockSet(time).format()))
	}
}

def resetEnergyMeter() {
	sendHubCommand(new physicalgraph.device.HubAction(zwave.meterV3.meterReset().format()))
}

def off() {
	setThermostatMode("off")
}

def heat() {
	setThermostatMode("heat")
}

def emergencyHeat() {
	setThermostatMode("emergencyHeat")
}