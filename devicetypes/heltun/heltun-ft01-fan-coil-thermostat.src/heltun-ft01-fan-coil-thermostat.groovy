/**
 *  HELTUN FT01 Fan Coil Thermostat
 *
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
 */
 

metadata {
	definition (name: "HELTUN FT01 Fan Coil Thermostat", namespace: "HELTUN", author: "Sarkis Kabrailian", cstHandler: true, ocfDeviceType: "oic.d.thermostat") {
		capability "Energy Meter"
		capability "Fan Speed"
		capability "Power Meter"
		capability "Relative Humidity Measurement"
		capability "Temperature Measurement"
		capability "Thermostat Heating Setpoint"
		capability "Thermostat Mode"
		capability "Thermostat Operating State"
		capability "Illuminance Measurement"
		capability "Configuration"
		capability "Health Check"
		capability "Refresh"

		fingerprint mfr: "0344", prod: "0004", model: "0002", deviceJoinName: "HELTUN Thermostat" //Raw Description zw:L type:0806 mfr:0344 prod:0004 model:0002 ver:2.05 zwv:7.11 lib:03 cc:5E,85,59,8E,55,86,72,5A,73,98,9F,6C,81,31,32,70,42,40,43,44,45,87,22,7A
	}
	preferences {
		input (
			title: "HE-FT01 | HELTUN Fan Coil Thermostat",
			description: "The user manual document with all technical information is available in support.heltun.com page. In case of technical questions please contact HELTUN Support Team at support@heltun.com",
			type: "paragraph",
			element: "paragraph"
		)
		parameterMap().each {
			if (it.title != null) {
				input (
					title: "${it.title}",
					description: it.description,
					type: "paragraph",
					element: "paragraph"
				)
			}
			def unit = it.unit ? it.unit : ""
			def defV = it.default as Integer
			def defVDescr = it.options ? it.options.get(defV) : "${defV}${unit} - Default Value"
			input (
				name: it.name,
				title: null,
				description: "$defVDescr",
				type: it.type,
				options: it.options,
				range: (it.min != null && it.max != null) ? "${it.min}..${it.max}" : null,
				defaultValue: it.default,
				required: false
			)
		}
	}
}

def parse(String description) {
	def cmd = zwave.parse(description)
	if (cmd) {
		return zwaveEvent(cmd)
	}
}

def checkParam() {
	boolean needConfig = false
	parameterMap().each {
		if (state."$it.name" == null || state."$it.name".state == "defNotConfigured") {
			state."$it.name" = [value: it.default as Integer, state: "defNotConfigured"]
			needConfig = true
		}
		if (settings."$it.name" != null && (state."$it.name".value != settings."$it.name" as Integer || state."$it.name".state == "notConfigured")) {
			state."$it.name".value = settings."$it.name" as Integer
			state."$it.name".state = "notConfigured"
			needConfig = true
		}
	}
	if ( needConfig ) {
		configParam()
	}
}

private configParam() {
	def cmds = []
	for (parameter in parameterMap()) {
		if ( state."$parameter.name"?.value != null && state."$parameter.name"?.state in ["notConfigured", "defNotConfigured"] ) {
				cmds << zwave.configurationV2.configurationSet(scaledConfigurationValue: state."$parameter.name".value, parameterNumber: parameter.paramNum, size: parameter.size).format()
				cmds << zwave.configurationV2.configurationGet(parameterNumber: parameter.paramNum).format()
				break
		}
	}
	if (cmds) {
		runIn(5, "checkParam")
		sendHubCommand(cmds,500)
	}
}

def zwaveEvent(physicalgraph.zwave.commands.configurationv2.ConfigurationReport cmd) {
	def parameter = parameterMap().find( {it.paramNum == cmd.parameterNumber } ).name
	if (state."$parameter".value == cmd.scaledConfigurationValue){
		state."$parameter".state = "configured"
	} else {
		state."$parameter".state = "error"
	}
	configParam()
}

def zwaveEvent(physicalgraph.zwave.commands.thermostatmodev2.ThermostatModeReport cmd) {
	def localScale = getTemperatureScale() //HubScale
	def deviceMode = numToModeMap[cmd.mode.toInteger()]
	sendEvent(name: "thermostatMode", data:[supportedThermostatModes: state.supportedModes], value: deviceMode)
	if (cmd.mode == 0 || cmd.mode == 6) {
		sendEvent(name: "heatingSetpoint", value: 0, unit: localScale)
	}
}

def zwaveEvent(physicalgraph.zwave.commands.sensormultilevelv5.SensorMultilevelReport cmd) {
	def map = [:]
	def roomTemperature = 1
	def humidity = 5
	def illuminance = 3
	def localScale = getTemperatureScale() //HubScale
	def deviceScale = (cmd.scale == 1) ? "F" : "C" //DeviceScale
	if (roomTemperature == cmd.sensorType) {
		def deviceTemp = cmd.scaledSensorValue
		def scaledTemp = (deviceScale == localScale) ? deviceTemp : (deviceScale == "F" ? roundC(fahrenheitToCelsius(deviceTemp)) : celsiusToFahrenheit(deviceTemp).toDouble().round(0).toInteger())
		map.name = "temperature"
		map.value = scaledTemp
		map.unit = localScale
		sendEvent(map)
	} else if (humidity == cmd.sensorType) {
		map.name = "humidity"
		map.value = cmd.scaledSensorValue.toInteger()
		map.unit = "%"
		sendEvent(map)
	} else if (illuminance == cmd.sensorType) {
		map.name = "illuminance"
		map.value = cmd.scaledSensorValue
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
		} else if (cmd.scale == 2) {
			map.name = "power"
			map.value = Math.round(cmd.scaledMeterValue)
			map.unit = "W"
		}
		sendEvent(map)
	}
}

def zwaveEvent(physicalgraph.zwave.commands.thermostatoperatingstatev2.ThermostatOperatingStateReport cmd) {
	def state = cmd.operatingState.toInteger()
	def currentState = opStateMap[state]
	sendEvent(name: "thermostatOperatingState", value: currentState)
}

def zwaveEvent(physicalgraph.zwave.commands.thermostatfanstatev1.ThermostatFanStateReport cmd) {
	def state = cmd.fanOperatingState.toInteger()
	def currentState = fanStateMap[state]
	sendEvent(name: "thermostatFanState", value: currentState)
}

def zwaveEvent(physicalgraph.zwave.commands.thermostatfanmodev3.ThermostatFanModeReport cmd) {
	def speed = cmd.fanMode.toInteger()
	def fanSpeed = fanModeToSpeedMap[speed]
	if (cmd.off) {
		fanSpeed = 0
	}
	sendEvent(name: "fanSpeed", value: fanSpeed)
}

def zwaveEvent(physicalgraph.zwave.commands.thermostatsetpointv2.ThermostatSetpointReport cmd) {
	def localScale = getTemperatureScale() //HubScale
	def deviceScale = (cmd.scale == 1) ? "F" : "C" //DeviceScale
	def deviceTemp = cmd.scaledValue
	def setPoint = (deviceScale == localScale) ? deviceTemp : (deviceScale == "F" ? roundC(fahrenheitToCelsius(deviceTemp)) : celsiusToFahrenheit(deviceTemp).toDouble().round(0).toInteger())
	def mode = modeToNumMap[device.currentValue("thermostatMode")]
	if (mode == 0 || mode == 6) {
		setPoint = 0
	}
	sendEvent(name: "heatingSetpoint", value: setPoint, unit: localScale)
}

def zwaveEvent(physicalgraph.zwave.commands.thermostatmodev2.ThermostatModeSupportedReport cmd) {
	def tSupportedModes = []
	if(cmd.heat) { tSupportedModes << "heat" }
	if(cmd.cool) { tSupportedModes << "cool" }
	if(cmd.auto) { tSupportedModes << "auto" }
	if(cmd.fanOnly) { tSupportedModes << "fanonly" }
	if(cmd.autoChangeover) { tSupportedModes << "autochangeover" }
	if(cmd.energySaveHeat) { tSupportedModes << "energysaveheat" }
	if(cmd.energySaveCool) { tSupportedModes << "energysavecool" }
	if(cmd.off) { tSupportedModes << "off" }
	state.supportedModes = tSupportedModes
	sendEvent(name: "supportedThermostatModes", value: tSupportedModes, displayed: false)
}

def zwaveEvent(physicalgraph.zwave.commands.multichannelassociationv2.MultiChannelAssociationReport cmd) {
	def cmds = []
	if (cmd.groupingIdentifier == 1) {
		if (cmd.nodeId != [0, zwaveHubNodeId, 0]) {
			cmds << zwave.multiChannelAssociationV2.multiChannelAssociationRemove(groupingIdentifier: 1).format()
			cmds << zwave.multiChannelAssociationV2.multiChannelAssociationSet(groupingIdentifier: 1, nodeId: [0,zwaveHubNodeId,0]).format()
		}
	}
	if (cmds) {
		sendHubCommand(cmds, 1200)
	}
}

def zwaveEvent(physicalgraph.zwave.commands.clockv1.ClockReport cmd) {
	def currDate = new Date().toCalendar()
	def time = [hour: currDate.get(Calendar.HOUR_OF_DAY), minute: currDate.get(Calendar.MINUTE), weekday: currDate.get(Calendar.DAY_OF_WEEK)]
	if ((time.hour != cmd.hour) || (time.minute != cmd.minute) || (time.weekday != cmd.weekday)) {
		sendHubCommand(zwave.clockV1.clockSet(time).format())
	}
}

def setHeatingSetpoint(tValue) {
	def cmds = []
	def mode = device.currentValue("thermostatMode")
	def currentMode = modeToNumMap[mode]
	def temp = state.heatingSetpoint = tValue.toDouble() //temp got fromm the app
	def tempInC = (getTemperatureScale() == "F" ? roundC(fahrenheitToCelsius(temp)) : temp) //If not C, Convert to C
	cmds << zwave.thermostatSetpointV2.thermostatSetpointSet(setpointType: currentMode, scale: 0, precision: 1, scaledValue: tempInC).format()
	// Sync temp, opState, setPoint, fanSpeed
	cmds << zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType:1).format()
	cmds << zwave.thermostatOperatingStateV2.thermostatOperatingStateGet().format()
	cmds << zwave.thermostatFanModeV3.thermostatFanModeGet()
	cmds << zwave.thermostatSetpointV2.thermostatSetpointGet(setpointType: currentMode).format()
	sendHubCommand(cmds)
}

def setFanSpeed(speed) {
	def cmds = []
	boolean fanState = false
	if (speed == 0) {
		fanState = true
		cmds << zwave.thermostatFanModeV3.thermostatFanModeSet(off: fanState)
	} else {
		def fanSpeed = fanSpeedToModeMap[speed]
		cmds << zwave.thermostatFanModeV3.thermostatFanModeSet(fanMode: fanSpeed, off: fanState)
	}
	cmds << zwave.thermostatFanModeV3.thermostatFanModeGet()
	sendHubCommand(cmds)
}

def setThermostatMode(String value) {
	def cmds = []
	cmds << zwave.thermostatModeV2.thermostatModeSet(mode: modeToNumMap[value]).format()
	cmds << zwave.thermostatModeV2.thermostatModeGet().format()
	cmds << zwave.thermostatSetpointV2.thermostatSetpointGet(setpointType: modeToNumMap[value]).format()
	sendHubCommand(cmds)
}

def getNumToModeMap() {
	[
		0 : "off",
		1 : "heat",
		2: "cool",
		3 : "auto",
		6 : "fanonly",
		10 : "autochangeover",
		11 : "energysaveheat",
		12 : "energysavecool"
	]
}

def getModeToNumMap() {
	[
		"off": 0,
		"heat": 1,
		"cool": 2,
		"auto": 3,
		"fanonly": 6,
		"autochangeover": 10,
		"energysaveheat": 11,
		"energysavecool": 12
	]
}

def getOpStateMap() {
	[
		0 : "idle",
		1 : "heating",
		2 : "cooling",
		3 : "fan only"
	]
}

def getFanStateMap() {
	[
		0 : "idle",
		1 : "running",
		2 : "running high",
		3 : "running medium"
	]
}

def getFanModeToSpeedMap() {
	[
		0 : 1, //Fan Auto Low > Speed Low
		1 : 1, //Fan Low > Speed Low
		2 : 4, //Fan Auto High > Speed High
		3 : 3, //Fan High > Speed Max
		4 : 2, //Fan Auto mendium > Speed Medium
		5 : 2  //Fan medium > Speed Medium
	]
}

def getFanSpeedToModeMap() {
	[
		1 : 1, //Speed Low > Fan Low
		2 : 5, //Speed Medium > Fan Medium
		3 : 3, //Speed High > Fan Auto High
		4 : 2 //Speed Max > Fan High
	]
}

def roundC (tempInC) {
	return (Math.round(tempInC.toDouble() * 2))/2
}

def updated() {
	initialize()
}

def initialize() {
	runIn(3, "checkParam")
}

def ping() {
	refresh()
}

def refresh() {
	def cmds = []
	cmds << zwave.thermostatModeV2.thermostatModeGet().format() //get thermostatmode
	cmds << zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType:1).format() //roomTemperature
	cmds << zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType:3).format() //Humidity
	cmds << zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType:5).format() //Illuminance
	cmds << zwave.meterV3.meterGet(scale: 0).format() //get kWh
	cmds << zwave.meterV3.meterGet(scale: 2).format() //get Watts
	cmds << zwave.thermostatOperatingStateV2.thermostatOperatingStateGet().format() //get Thermostat Operating State
	cmds << zwave.clockV1.clockGet().format() //get Clock
	cmds << zwave.multiChannelAssociationV2.multiChannelAssociationGet(groupingIdentifier: 1).format() //get channel association
	cmds << zwave.thermostatModeV2.thermostatModeSupportedGet().format() //get supported modes
	cmds << zwave.thermostatFanModeV3.thermostatFanModeGet() //get fanMode
	sendHubCommand(cmds, 1200)
	runIn(15, "checkParam")
}

def configure() {
	ping()
}

def resetEnergyMeter() {
	sendHubCommand(zwave.meterV3.meterReset().format())
}

def off() {
	setThermostatMode("off")
}

def heat() {
	setThermostatMode("heat")
}

def cool() {
	setThermostatMode("cool")
}

def auto() {
	setThermostatMode("auto")
}

private parameterMap() {[
[title: "Display Brightness Control", description: "The HE-FT01 can adjust its display brightness automatically depending on the illumination of the ambient environment and also allows to control it manually.",
 name: "Selected Brightness Level", options: [
			0: "Auto",
			1: "Level 1 (Lowest)",
			2: "Level 2",
			3: "Level 3",
			4: "Level 4",
			5: "Level 5",
			6: "Level 6",
			7: "Level 7",
			8: "Level 8",
			9: "Level 9",
			10: "Level 10 (Highest)"
	], paramNum: 5, size: 1, default: "0", type: "enum"],
    
[title: "Touch Sensor Sensitivity Threshold", description: "This Parameter allows to adjust the Touch Buttons Sensitivity. Note: Setting the sensitivity too high can lead to false touch detection. We recommend not changing this Parameter unless there is a special need to do so.",
 name: "Selected Touch Sensitivity", options: [
			1: "Level 1 (Low sensitivity)",
			2: "Level 2",
			3: "Level 3",
			4: "Level 4",
			5: "Level 5",
			6: "Level 6",
			7: "Level 7",
			8: "Level 8",
			9: "Level 9",
			10: "Level 10 (High sensitivity)"
	], paramNum: 6, size: 1, default: "6", type: "enum"],
    
[title: "Fan Relay Output Mode", description: "This Parameter determines the type of load connected to the device fan relay relay outputs (OUT-1, OUT-2, OUT-3). The output type can be NO – normal open (no contact/voltage switch the load OFF) or NC - normal close (output is contacted / there is a voltage to switch the load OFF)",
 name: "Selected Mode", options: [
			0: "NO - Normal Open",
			1: "NC - Normal Close"
	], paramNum: 7, size: 1, default: "0", type: "enum"],

[title: "Heater Relay Output Mode", description: "This Parameter determines the type of load connected to the device heater relay output (OUT-4). The output type can be NO – normal open (no contact/voltage switch the load OFF) or NC - normal close (output is contacted / there is a voltage to switch the load OFF)",
 name: "Selected Mode", options: [
			0: "NO - Normal Open",
			1: "NC - Normal Close"
	], paramNum: 8, size: 1, default: "0", type: "enum"],

[title: "Cooler Relay Output Mode", description: "This Parameter determines the type of load connected to the device cooler relay output (OUT-5). The output type can be NO – normal open (no contact/voltage switch the load OFF) or NC - normal close (output is contacted / there is a voltage to switch the load OFF)",
 name: "Selected Mode", options: [
			0: "NO - Normal Open",
			1: "NC - Normal Close"
	], paramNum: 9, size: 1, default: "0", type: "enum"],
    
[title: "Heating State Fan Control", description: "This parameter determines if fan should be enabled or disabled in heating mode. If fan is enabled (normal operation), one of the outputs OUT-1, OUT-2, OUT-3 will be ON depending on the selected fan speed. If fan is disabled, in heating state, only OUT-4 will be ON and OUT-1, OUT-2, OUT-3 will always remain OFF",
 name: "Selected Mode", options: [
			0: "Fan Disabled",
			1: "Fan Enabled"
	], paramNum: 10, size: 1, default: "1", type: "enum"],
    
[title: "Cooling State Fan Control", description: "This parameter determines if fan should be enabled or disabled in cooling mode. If fan is enabled (normal operation), one of the outputs OUT-1, OUT-2, OUT-3 will be ON depending on the selected fan speed. If fan is disabled, in cooling state, only OUT-4 will be ON and OUT-1, OUT-2, OUT-3 will always remain OFF",
 name: "Selected Mode", options: [
			0: "Fan Disabled",
			1: "Fan Enabled"
	], paramNum: 11, size: 1, default: "1", type: "enum"],
    
[title: "Relays Load Power", description: "These parameters are used to specify the loads power that are connected to the device outputs (Relays). Using your connected device’s power consumption specification (see associated owner’s manual), set the load in Watts for the outputs bellow:",
 name: "Selected Fan Low Speed Load Power in Watts", paramNum: 12, size: 2, default: 0, type: "number", min: 0, max: 1100, unit: "W"],

[name: "Selected Fan Medium Speed Load Power in Watts", paramNum: 13, size: 2, default: 0, type: "number", min: 0, max: 1100, unit: "W"],

[name: "Selected Fan High Speed Load Power in Watts", paramNum: 14, size: 2, default: 0, type: "number", min: 0, max: 1100, unit: "W"],

[name: "Selected Heating Load Power in Watts", paramNum: 15, size: 2, default: 0, type: "number", min: 0, max: 1100, unit: "W"],

[name: "Selected Cooling Load Power in Watts", paramNum: 16, size: 2, default: 0, type: "number", min: 0, max: 1100, unit: "W"],

[title: "Air Temperature Calibration", description: "This Parameter defines the offset value for room air temperature. This value will be added or subtracted from the air temperature sensor reading.Through the Z-Wave network the value of this Parameter should be x10, e.g. for 1.5°C set the value 15.",
 name: "Selected Temperature Offset in °Cx10", paramNum: 17, size: 1, default: 0, type: "number", min: -100, max: 100, unit: " °Cx10"],

[title: "Temperature Hysteresis", description: "This Parameter defines the hysteresis value for temperature control. The HE-FT01 will stabilize the temperature with selected hysteresis. For example, if the SET POINT is set for 25°C and HYSTERESIS is set for 0.5°C the HE-FT01 will change the state to IDLE if the temperature reaches 25.0°C. It will change the state to HEATING if the temperature becomes lower than 24.5°C, and will change the state to COOLING if the temperature rises beyond 25.5°C.The value of this Parameter should be x10 e.g. for 0.5°C set the value 5.",
 name: "Selected Hysteresis in °Cx10", paramNum: 18, size: 1, default: 5, type: "number", min: 2, max: 100, unit: " °Cx10"],

[title: "TIME mode operation", description: "This Parameter determines the Climate Mode (Heating or Cooling) in which HE-FT01 will operates when the TIME mode is selected",
 name: "Selected Mode", options: [
			1: "Heating & Cooling",
			2: "Heating",
			3: "Cooling"
	], paramNum: 23, size: 1, default: "1", type: "enum"],
    
[title: "Schedule Time", description: "Use these Parameters to set the Morning, Day, Evening and Night start times manually for the Temperature Schedule. The value of these Parameters has format HHMM, e.g. for 08:00 use value 0800 (time without a colon). From 00:00 to 23:59 can be selected.",
 name: "Selected Morning Start Time", paramNum: 41, size: 2, default: 600, type: "number", min: 0, max: 2359, unit: " HHMM"],

[name: "Selected Day Start Time", paramNum: 42, size: 2, default: 900, type: "number", min: 0, max: 2359, unit: " HHMM"], 

[name: "Selected Evening Start Time", paramNum: 43, size: 2, default: 1800, type: "number", min: 0, max: 2359, unit: " HHMM"],

[name: "Selected Night Start Time", paramNum: 44, size: 2, default: 2300, type: "number", min: 0, max: 2359, unit: " HHMM"],

[title: "Schedule Temperature", description: "Use these Parameters to set the temperature for each day Schedule manually. The value of this Parameter should be x10, e.g., for 22.5°C set value 225. From 1°C (value 10) to 110°C (value 1100) can be selected.",
 name: "Monday Morning Temperature in °Cx10", paramNum: 45, size: 2, default: 240, type: "number", min: 10, max: 370, unit: " °Cx10"],
 
[name: "Monday Day Temperature in °Cx10", paramNum: 46, size: 2, default: 200, type: "number", min: 10, max: 370, unit: " °Cx10"],

[name: "Monday Evening Temperature in °Cx10", paramNum: 47, size: 2, default: 230, type: "number", min: 10, max: 370, unit: " °Cx10"],

[name: "Monday Night Temperature in °Cx10", paramNum: 48, size: 2, default: 180, type: "number", min: 10, max: 370, unit: " °Cx10"],

[name: "Tuesday Morning Temperature in °Cx10", paramNum: 49, size: 2, default: 240, type: "number", min: 10, max: 370, unit: " °Cx10"],
 
[name: "Tuesday Day Temperature in °Cx10", paramNum: 50, size: 2, default: 200, type: "number", min: 10, max: 370, unit: " °Cx10"],

[name: "Tuesday Evening Temperature in °Cx10", paramNum: 51, size: 2, default: 230, type: "number", min: 10, max: 370, unit: " °Cx10"],

[name: "Tuesday Night Temperature in °Cx10", paramNum: 52, size: 2, default: 180, type: "number", min: 10, max: 370, unit: " °Cx10"],

[name: "Wednesday Morning Temperature in °Cx10", paramNum: 53, size: 2, default: 240, type: "number", min: 10, max: 370, unit: " °Cx10"],
 
[name: "Wednesday Day Temperature in °Cx10", paramNum: 54, size: 2, default: 200, type: "number", min: 10, max: 370, unit: " °Cx10"],

[name: "Wednesday Evening Temperature in °Cx10", paramNum: 55, size: 2, default: 230, type: "number", min: 10, max: 370, unit: " °Cx10"],

[name: "Wednesday Night Temperature in °Cx10", paramNum: 56, size: 2, default: 180, type: "number", min: 10, max: 370, unit: " °Cx10"],

[name: "Thursday Morning Temperature in °Cx10", paramNum: 57, size: 2, default: 240, type: "number", min: 10, max: 370, unit: " °Cx10"],
 
[name: "Thursday Day Temperature in °Cx10", paramNum: 58, size: 2, default: 200, type: "number", min: 10, max: 370, unit: " °Cx10"],

[name: "Thursday Evening Temperature in °Cx10", paramNum: 59, size: 2, default: 230, type: "number", min: 10, max: 370, unit: " °Cx10"],

[name: "Thursday Night Temperature in °Cx10", paramNum: 60, size: 2, default: 180, type: "number", min: 10, max: 370, unit: " °Cx10"],

[name: "Friday Morning Temperature in °Cx10", paramNum: 61, size: 2, default: 240, type: "number", min: 10, max: 370, unit: " °Cx10"],
 
[name: "Friday Day Temperature in °Cx10", paramNum: 62, size: 2, default: 200, type: "number", min: 10, max: 370, unit: " °Cx10"],

[name: "Friday Evening Temperature in °Cx10", paramNum: 63, size: 2, default: 230, type: "number", min: 10, max: 370, unit: " °Cx10"],

[name: "Friday Night Temperature in °Cx10", paramNum: 64, size: 2, default: 180, type: "number", min: 10, max: 370, unit: " °Cx10"],

[name: "Saturday Morning Temperature in °Cx10", paramNum: 65, size: 2, default: 240, type: "number", min: 10, max: 370, unit: " °Cx10"],
 
[name: "Saturday Day Temperature in °Cx10", paramNum: 66, size: 2, default: 200, type: "number", min: 10, max: 370, unit: " °Cx10"],

[name: "Saturday Evening Temperature in °Cx10", paramNum: 67, size: 2, default: 230, type: "number", min: 10, max: 370, unit: " °Cx10"],

[name: "Saturday Night Temperature in °Cx10", paramNum: 68, size: 2, default: 180, type: "number", min: 10, max: 370, unit: " °Cx10"],

[name: "Sunday Morning Temperature in °Cx10", paramNum: 69, size: 2, default: 240, type: "number", min: 10, max: 370, unit: " °Cx10"],
 
[name: "Sunday Day Temperature in °Cx10", paramNum: 70, size: 2, default: 200, type: "number", min: 10, max: 370, unit: " °Cx10"],

[name: "Sunday Evening Temperature in °Cx10", paramNum: 71, size: 2, default: 230, type: "number", min: 10, max: 370, unit: " °Cx10"],

[name: "Sunday Night Temperature in °Cx10", paramNum: 72, size: 2, default: 180, type: "number", min: 10, max: 370, unit: " °Cx10"],

[title: "Energy Consumption Meter Consecutive Report Interval", description: "When the device is connected to the gateway, it periodically sends reports from its energy consumption sensor even if there is no change in the value. This parameter defines the interval between consecutive reports of real time and cumulative energy consumption data to the gateway",
 name: "Selected Energy Report Interval in minutes", paramNum: 141, size: 1, default: 10, type: "number", min: 1 , max: 120, unit: "min"],
 
[title: "Control Energy Meter Report", description: "This Parameter determines if the change in the energy meter will result in a report being sent to the gateway. Note: When the device is turning ON, the consumption data will be sent to the gateway once, even if the report is disabled.",
 name: "Sending Energy Meter Reports", options: [
 			0: "Disabled",
			1: "Enabled"
	], paramNum: 142, size: 1, default: "1", type: "enum"],

[title: "Sensors Consecutive Report Interval", description: "When the device is connected to the gateway, it periodically sends to the gateway reports from its external NTC temperature sensor even if there are not changes in the values. This Parameter defines the interval between consecutive reports",
 name: "Selected Energy Report Interval in minutes", paramNum: 143, size: 1, default: 10, type: "number", min: 1 , max: 120, unit: "min"],

[title: "Air & Floor Temperature Sensors Report Threshold", description: "This Parameter determines the change in temperature level (in °C) resulting in temperature sensors report being sent to the gateway. The value of this Parameter should be x10 for °C, e.g. for 0.4°C use value 4. Use the value 0 if there is a need to stop sending the reports.",
 name: "Selected Temperature Threshold in °Cx10", paramNum: 144, size: 1, default: 2, type: "number", min: 0 , max: 100, unit: " °Cx10"],
 
[title: "Humidity Sensor Report Threshold", description: "This Parameter determines the change in humidity level in % resulting in humidity sensors report being sent to the gateway. Use the value 0 if there is a need to stop sending the reports.",
 name: "Selected Humidity Threshold in %", paramNum: 145, size: 1, default: 2, type: "number", min: 0 , max: 25, unit: "%"],

[title: "Light Sensor Report Threshold", description: "This Parameter determines the change in the ambient environment illuminance level resulting in a light sensors report being sent to the gateway. From 10% to 99% can be selected. Use the value 0 if there is a need to stop sending the reports.",
 name: "Selected Light Sensor Threshold in %", paramNum: 146, size: 1, default: 50, type: "number", min: 0 , max: 99, unit: "%"]

]}