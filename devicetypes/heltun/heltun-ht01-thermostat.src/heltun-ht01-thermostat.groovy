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
	definition (name: "HELTUN HT01 Thermostat", namespace: "HELTUN", author: "Sarkis Kabrailian", cstHandler: true, ocfDeviceType: "oic.d.thermostat", mcdSync: true) {
		capability "Energy Meter"
		capability "Power Meter"
		capability "Relative Humidity Measurement"
		capability "Temperature Measurement"
		capability "Thermostat Heating Setpoint"
		capability "Thermostat Mode"
		capability "Thermostat Operating State"
		capability "Illuminance Measurement"
		capability "Voltage Measurement"
		capability "Configuration"
		capability "Health Check"
		capability "Refresh"

		fingerprint mfr: "0344", prod: "0004", model: "0001", deviceJoinName: "HELTUN Thermostat"
	}
	preferences {
		input (
			title: "HE-HT01 | HELTUN Heating Thermostat",
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

private channelNumber(String N) {
	N.split(":")[-1] as Integer
}

def installed() {
	state.oldLabel = device.label
	def childName = "${device.displayName} Floor Temperature"
	def existingChildren = getChildDevices()
	def floorTemperatureid = "${device.deviceNetworkId}:${1}"
	def childExists = (existingChildren.find {child -> child.getDeviceNetworkId() == floorTemperatureid} != NULL)
	if (!childExists) {
		addChildDevice("HE-TEMPERATURE", floorTemperatureid, device.hubId,[completedSetup: true, label: childName, isComponent: true, componentName: "FloorTemperature", componentLabel: "FloorTemperature"])
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
	}
	else {
		state."$parameter".state = "error"
	}
	configParam()
}

def zwaveEvent(physicalgraph.zwave.commands.thermostatmodev2.ThermostatModeReport cmd) {
	def locaScale = getTemperatureScale() //HubScale
	def deviceMode = numToModeMap[cmd.mode.toInteger()]
	sendEvent(name: "thermostatMode", data:[supportedThermostatModes: state.supportedModes], value: deviceMode)
	//if mode is off -> change stepoint value to 0
	if (cmd.mode == 0) {
		sendEvent(name: "heatingSetpoint", value: 0, unit: locaScale)
	}
	sendHubCommand(zwave.thermostatSetpointV2.thermostatSetpointGet(setpointType: cmd.mode.toInteger()).format()) //getSetpoint
}

def zwaveEvent(physicalgraph.zwave.commands.sensormultilevelv5.SensorMultilevelReport cmd) {
	def map = [:]
	def floorTemperature = 24
	def roomTemperature = 1
	def himidity = 5
	def illuminance = 3
	def locaScale = getTemperatureScale() //HubScale
	def deviceScale = (cmd.scale == 1) ? "F" : "C" //DeviceScale
	def child = childDevices?.find {channelNumber(it.deviceNetworkId) == 1 }
	if (roomTemperature == cmd.sensorType) {
		def deviceTemp = cmd.scaledSensorValue
		def scaledTemp = (deviceScale == locaScale) ? deviceTemp : (deviceScale == "F" ? roundC(fahrenheitToCelsius(deviceTemp)) : celsiusToFahrenheit(deviceTemp).toDouble().round(0).toInteger())
		map.name = "temperature"
		map.value = scaledTemp
		map.unit = locaScale
		sendEvent(map)
	} else if (floorTemperature == cmd.sensorType) {
		def deviceTemp = cmd.scaledSensorValue
		def scaledTemp = (deviceScale == locaScale) ? deviceTemp : (deviceScale == "F" ? roundC(fahrenheitToCelsius(deviceTemp)) : celsiusToFahrenheit(deviceTemp).toDouble().round(0).toInteger())
		map.name = "temperature"
		map.value = scaledTemp
		map.unit = locaScale
		child?.sendEvent(map)
	} else if (himidity == cmd.sensorType) {
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
		}else if (cmd.scale == 4) {
			map.name = "voltage"
			map.value = Math.round(cmd.scaledMeterValue)
			map.unit = "V"
		}
		sendEvent(map)
	}
}

def zwaveEvent(physicalgraph.zwave.commands.thermostatoperatingstatev2.ThermostatOperatingStateReport cmd) {
	def state = (cmd.operatingState == 1) ? "heating" : "idle"
	sendEvent(name: "thermostatOperatingState", value: state)
}

def zwaveEvent(physicalgraph.zwave.commands.thermostatsetpointv2.ThermostatSetpointReport cmd) {
	def locaScale = getTemperatureScale() //HubScale
	def deviceScale = (cmd.scale == 1) ? "F" : "C" //DeviceScale
	def deviceTemp = cmd.scaledValue
	def setPoint = (deviceScale == locaScale) ? deviceTemp : (deviceScale == "F" ? roundC(fahrenheitToCelsius(deviceTemp)) : celsiusToFahrenheit(deviceTemp).toDouble().round(0).toInteger())
	def mode = modeToNumMap[device.currentValue("thermostatMode")]
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
	def currentMode = modeToNumMap[mode]
	def temp = state.heatingSetpoint = tValue.toDouble() //temp got fromm the app
	def tempInC = (getTemperatureScale() == "F" ? roundC(fahrenheitToCelsius(temp)) : temp) //If not C, Convert to C
	cmds << zwave.thermostatSetpointV2.thermostatSetpointSet(setpointType: currentMode, scale: 0, precision: 1, scaledValue: tempInC).format()
	// Sync temp, opState, setPoint
	cmds << zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType:1).format()
	cmds << zwave.thermostatOperatingStateV2.thermostatOperatingStateGet().format()
	cmds << zwave.thermostatSetpointV2.thermostatSetpointGet(setpointType: currentMode).format()
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
		8 : "dryair",
		10 : "autochangeover",
		11 : "energysaveheat",
		13 : "away"
	]
}

def getModeToNumMap() {
	[
		"off": 0,
		"heat": 1,
		"dryair": 8,
		"autochangeover": 10,
		"energysaveheat": 11,
		"away": 13
	]
}

def roundC (tempInC) {
	return (Math.round(tempInC.toDouble() * 2))/2
}

def updated() {
	def childName = "${device.displayName} Floor Temperature"
	if (childDevices && device.label != state.oldLabel) {
		childDevices.each {it.setLabel(childName)}
		state.oldLabel = device.label
	}
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
	cmds << zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType:24).format() //floorTemperature
	cmds << zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType:3).format() //Humidity
	cmds << zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType:5).format() //Illuminance
	cmds << zwave.meterV3.meterGet(scale: 0).format() //get kWh
	cmds << zwave.meterV3.meterGet(scale: 2).format() //get Watts
	cmds << zwave.meterV3.meterGet(scale: 4).format() //get Voltage
	cmds << zwave.thermostatOperatingStateV2.thermostatOperatingStateGet().format() //get Thermostat Operating State
	cmds << zwave.clockV1.clockGet().format() //get Clock
	cmds << zwave.multiChannelAssociationV2.multiChannelAssociationGet(groupingIdentifier: 1).format() //get channel association
	cmds << zwave.thermostatModeV2.thermostatModeSupportedGet().format() //get supported modes
	sendHubCommand(cmds, 1200)
	runIn(15, "checkParam")
}

def zwaveEvent(physicalgraph.zwave.commands.multichannelassociationv2.MultiChannelAssociationReport cmd) {
	def cmds = []
	if (cmd.groupingIdentifier == 1) {
		if (cmd.nodeId != [1]) {
			cmds << zwave.multiChannelAssociationV2.multiChannelAssociationRemove(groupingIdentifier: 1).format()
			cmds << zwave.multiChannelAssociationV2.multiChannelAssociationSet(groupingIdentifier: 1, nodeId: 1).format()
		}
	}
	if (cmds) {
		sendHubCommand(cmds, 1200)
	}
}

def configure() {
	ping()
}

def zwaveEvent(physicalgraph.zwave.commands.clockv1.ClockReport cmd) {
	def currDate = Calendar.getInstance(location.timeZone)
	def time = [hour: currDate.get(Calendar.HOUR_OF_DAY), minute: currDate.get(Calendar.MINUTE), weekday: currDate.get(Calendar.DAY_OF_WEEK)]
	if ((time.hour != cmd.hour) || (time.minute != cmd.minute) || (time.weekday != cmd.weekday)){
		sendHubCommand(zwave.clockV1.clockSet(time).format())
	}
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

def emergencyHeat() {
	setThermostatMode("emergencyHeat")
}

private parameterMap() {[
[title: "Display Brightness Control", description: "The HE-HT01 can adjust its display brightness automatically depending on the illumination of the ambient environment and also allows to control it manually.",
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
    
[title: "Relay Output Mode", description: "This Parameter determines the type of load connected to the device relay output. The output type can be NO – normal open (no contact/voltage switch the load OFF) or NC - normal close (output is contacted / there is a voltage to switch the load OFF)",
 name: "Selected Mode", options: [
			0: "NO - Normal Open",
			1: "NC - Normal Close"
	], paramNum: 7, size: 1, default: "0", type: "enum"],

[title: "External Input Mode", description: "This parameter defines how the thermostat should react when pressing the button connected to the external input. The options are: Disabled, Toggle Switch: if the external input is shorted (with Sx or Line) the Thermostat switches to the operating mode selected in the External Input Action bellow and switches to OFF mode when the external input is open, Toggle Switch Reverse: Toggle Switch Reverse” mode: if the external input is shorted the Thermostat switches to OFF mode and switches to the operating mode selected in the External Input Action bellow when the input is open, Momentary Switch: each press of button (shorten of input) will consistently change the mode to the operating mode selected in External Input Action bellow",
 name: "Selected External Input Mode", options: [
			0: "Disabled",
			1: "Toggle Switch",
			2: "Toggle Switch Reverse",
			3: "Momentary Switch"
	], paramNum: 8, size: 1, default: "0", type: "enum"], 

[title: "External Input Action", description: "This parameter allows selection of which Operating Mode the HE-HT01 should revert to when the external input is shorted.",
 name: "Selected External Input Action", options: [
			1: "Heat",
			2: "Auto Cangeover",
			3: "Dry Air",
			4: "Energy Save Heat",
			5: "Away",
			6: "Off"
	], paramNum: 9, size: 1, default: "6", type: "enum"],
    
[title: "Source Sensor", description: "1) A – Air sensor: Regulation (heating control) is based on the SET POINT applied to the internal room air temperature sensor. 2) AF – Air sensor plus floor sensor: Regulation is based on SET POINT applied to the internal room temperature sensor but also controlled by the floor temperature sensor ensuring that the floor temperature remains within the floor temperature limits specified bellow. 3) F – Floor sensor: Regulation is based on the SET POINT applied to the external floor temperature sensor. 4) FA – Floor sensor plus air sensor: Regulation is based on SET POINT applied to the external floor sensor but is also controlled by the internal air temperature sensor ensuring that the air temperature remains within the air temperature limits specified bellow. 5) t – Time regulator: Regulation is based on the time settings for heating which will be ON during the (ON time) and OFF during the (OFF Time) specified in the configurations bellow. This cycle will be repeated constantly. 6) tA – Time regulator + Air sensor: Regulation is based on the ON & OFF times specified in the configurations bellow but also controlled by the internal air temperature sensor ensuring that the room temperature remains within the air temperature limits specified bellow. 7) tF – Time regulator + Floor sensor Parameters: Regulation is based on the ON & OFF times specified in the configurations bellow but also controlled by the floor temperature sensor ensuring that the floor temperature remains within the floor temperature limits specified bellow.",
 name: "Selected Source Sensor", options: [
			1: "Air Sensor",
			2: "Air + Floor Sensors",
			3: "Floor Sensor",
			4: "Floor + Air Sensors",
			5: "Time Regulator",
			6: "Time + Air Sensor",
			7: "Time + Floor Sensor"
	], paramNum: 11, size: 1, default: "3", type: "enum"],
    
[name: "Air Temperature Minimum in °Cx10", paramNum: 12, size: 2, default: 210, type: "number", min: 10, max: 360, unit: " °Cx10"],

[name: "Air Temperature Maximum in °Cx10", paramNum: 13, size: 2, default: 270, type: "number", min: 20, max: 370, unit: " °Cx10"],

[name: "Floor Temperature Minimum in °Cx10", paramNum: 14, size: 2, default: 180, type: "number", min: 10, max: 360, unit: " °Cx10"],

[name: "Floor Temperature Maximum in °Cx10", paramNum: 15, size: 2, default: 320, type: "number", min: 20, max: 370, unit: " °Cx10"],

[name: "Time Regulation ON Time in minutes", paramNum: 23, size: 2, default: 30, type: "number", min: 10, max: 240, unit: "min"],

[name: "Time Regulation OFF Time in minutes", paramNum: 24, size: 2, default: 30, type: "number", min: 20, max: 240, unit: "min"],

[title: "Floor Sensor Resistance", description: "If an external floor NTC temperature sensor is used it is necessary to select the correct resistance value in kiloOhms (kΩ) of the sensor",
 name: "Selected Floor Resistance in kΩ", paramNum: 10, size: 1, default: 10, type: "number", min: 1, max: 100, unit: "kΩ"],

[title: "Floor Temperature Calibration", description: "This Parameter defines the offset value for floor temperature. This value will be added or subtracted from the floor temperature sensor reading.Through the Z-Wave network the value of this Parameter should be x10, e.g. for 1.5°C set the value 15.",
 name: "Selected Temperature Offset in °Cx10", paramNum: 16, size: 1, default: 0, type: "number", min: -100, max: 100, unit: " °Cx10"],
 
[title: "Air Temperature Calibration", description: "This Parameter defines the offset value for room air temperature. This value will be added or subtracted from the air temperature sensor reading.Through the Z-Wave network the value of this Parameter should be x10, e.g. for 1.5°C set the value 15.",
 name: "Selected Temperature Offset in °Cx10", paramNum: 17, size: 1, default: 0, type: "number", min: -100, max: 100, unit: " °Cx10"],

[title: "Temperature Hysteresis", description: "This Parameter defines the hysteresis value for temperature control. The HE-HT01 will stabilize the temperature with selected hysteresis. For example, if the SET POINT is set for 25°C and HYSTERESIS is set for 0.5°C the HE-HT01 will change the state to IDLE when the temperature reaches 25.0°C, but it will change the state to HEATING if the temperature drops lower than 24.5°C.The value of this Parameter should be x10 e.g. for 0.5°C set the value 5.",
 name: "Selected Hysteresis in °Cx10", paramNum: 18, size: 1, default: 5, type: "number", min: 2, max: 100, unit: " °Cx10"],

[title: "Dry Time", description: "By choosing Dry Mode, the device will increase the temperature to the selected Set Point and keep it for the time specified in this parameter. A time range of 1 to 720 minutes (12 hours) can be set. As the Dry Time passes, the Thermostat will automatically change to the Mode set in the 'Mode to Switch After Dry Mode Operation Complete' configuration bellow.",
 name: "Selected Dry Time in minutes", paramNum: 25, size: 2, default: 30, type: "number", min: 5, max: 90, unit: "min"],

[title: "Mode to Switch After Dry Mode Operation Complete", description: "This Parameter indicates the mode that will be set after Dry Time.",
 name: "Selected Mode to Switch", options: [
			1: "Heat",
			2: "Auto Cangeover",
			4: "Energy Save Heat",
			5: "Away",
			6: "Off"
	], paramNum: 26, size: 1, default: "1", type: "enum"], 

[title: "Child Lock Restriction Level", description: "This parameter specifies the restriction level of Child Lock feature where it allows you to choose which touch buttons/features of HE-HT01 should be disabled temporarily while the device is locked. Choosing level 1 will lock all the buttons, choosing level 2 will let you change the setpoint and lock the remaining buttons, choosing level 3 will let you change the setpoint and the operating mode, and lock the remaining buttons. This parameter is available on firmware V2.4 or higher",
 name: "Selected Restriction Level", options: [
			1: "level 1 (Strictest)",
			2: "level 2",
			3: "level 3 (least strict)"
	], paramNum: 40, size: 1, default: "1", type: "enum"], 

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

[title: "Energy Consumption Meter Report", description: "This Parameter determines the change in the load power resulting in the consumption report being sent to the gateway. Use the value 0 if there is a need to stop sending the reports.",
 name: "Selected Change Percentage", paramNum: 142, size: 1, default: 25, type: "number", min: 0 , max: 50, unit: "%"],

[title: "Sensors Consecutive Report Interval", description: "When the device is connected to the gateway, it periodically sends to the gateway reports from its external NTC temperature sensor even if there are not changes in the values. This Parameter defines the interval between consecutive reports",
 name: "Selected Energy Report Interval in minutes", paramNum: 143, size: 1, default: 10, type: "number", min: 1 , max: 120, unit: "min"],

[title: "Air & Floor Temperature Sensors Report Threshold", description: "This Parameter determines the change in temperature level (in °C) resulting in temperature sensors report being sent to the gateway. The value of this Parameter should be x10 for °C, e.g. for 0.4°C use value 4. Use the value 0 if there is a need to stop sending the reports.",
 name: "Selected Temperature Threshold in °Cx10", paramNum: 144, size: 1, default: 2, type: "number", min: 0 , max: 100, unit: " °Cx10"],
 
[title: "Humidity Sensor Report Threshold", description: "This Parameter determines the change in humidity level in % resulting in humidity sensors report being sent to the gateway. Use the value 0 if there is a need to stop sending the reports.",
 name: "Selected Humidity Threshold in %", paramNum: 145, size: 1, default: 2, type: "number", min: 0 , max: 25, unit: "%"],

[title: "Light Sensor Report Threshold", description: "This Parameter determines the change in the ambient environment illuminance level resulting in a light sensors report being sent to the gateway. From 10% to 99% can be selected. Use the value 0 if there is a need to stop sending the reports.",
 name: "Selected Light Sensor Threshold in %", paramNum: 146, size: 1, default: 50, type: "number", min: 0 , max: 99, unit: "%"]
 
]}