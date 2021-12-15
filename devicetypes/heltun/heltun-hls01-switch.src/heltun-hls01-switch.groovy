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
	definition (name: "HELTUN HLS01 Switch", namespace: "HELTUN", author: "Sarkis Kabrailian", cstHandler: true) {
		capability "Energy Meter"
		capability "Power Meter"
		capability "Switch"
		capability "Temperature Measurement"
		capability "Voltage Measurement"
		capability "Configuration"
		capability "Health Check"
		capability "Refresh"

		fingerprint mfr: "0344", prod: "0004", inClusters:"0x25", deviceJoinName: "HELTUN Switch" //model: "000A"
	}
	preferences {
		input (
			title: "HE-HLS01 | HELTUN High Load Switch",
			description: "The user manual document with all technical information is available in support.heltun.com page. In case of technical questions please contact HELTUN Support Team at support@heltun.com",
			type: "paragraph",
			element: "paragraph"
		)
		parameterMap().each {
			input (
				title: "${it.title}",
				description: it.description,
				type: "paragraph",
				element: "paragraph"
			)
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

def initialize() {
	runIn(3, "checkParam")
}

def parse(String description) {
	def result = null
	def cmd = zwave.parse(description)
	if (cmd) {result = zwaveEvent(cmd)}
	return result
}

def updated() {
	initialize()
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
	if (state."$parameter".value == cmd.scaledConfigurationValue) {
		state."$parameter".state = "configured"
	}
	else {
		state."$parameter".state = "error"
	}
	configParam()
}

def zwaveEvent(physicalgraph.zwave.commands.sensormultilevelv5.SensorMultilevelReport cmd) {
	def locaScale = getTemperatureScale() //HubScale
	def externalTemp = 1
	def map = [:]
	if (externalTemp == cmd.sensorType) {
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
		} else if (cmd.scale == 2) {
			map.name = "power"
			map.value = Math.round(cmd.scaledMeterValue)
			map.unit = "W"
			sendEvent(map)
		} else if (cmd.scale == 4) {
			map.name = "voltage"
			map.value = Math.round(cmd.scaledMeterValue)
			map.unit = "V"
			sendEvent(map)
		} else if (cmd.scale == 5) {
			map.name = "current"
			map.value = Math.round(cmd.scaledMeterValue)
			map.unit = "A"
		}
	}
}

def zwaveEvent(physicalgraph.zwave.commands.clockv1.ClockReport cmd) {
	def currDate = Calendar.getInstance(location.timeZone)
	def time = [hour: currDate.get(Calendar.HOUR_OF_DAY), minute: currDate.get(Calendar.MINUTE), weekday: currDate.get(Calendar.DAY_OF_WEEK)]
	if ((time.hour != cmd.hour) || (time.minute != cmd.minute) || (time.weekday != cmd.weekday)){
		sendHubCommand(zwave.clockV1.clockSet(time).format())
	}
}

def zwaveEvent(physicalgraph.zwave.commands.switchbinaryv1.SwitchBinaryReport cmd) {
	def state = cmd.value ? "on" : "off"
	sendEvent(name: "switch", value: state)
}

def zwaveEvent(physicalgraph.zwave.commands.multichannelassociationv2.MultiChannelAssociationReport cmd) {
	def cmds = []
	if (cmd.groupingIdentifier == 1) {
		if (cmd.nodeId != [zwaveHubNodeId]) {
			cmds << zwave.multiChannelAssociationV2.multiChannelAssociationRemove(groupingIdentifier: 1).format()
			cmds << zwave.multiChannelAssociationV2.multiChannelAssociationSet(groupingIdentifier: 1, nodeId: zwaveHubNodeId).format()
		}
	}
	if (cmds) {
		sendHubCommand(cmds, 1200)
	}
}

def roundC (tempInC) {
	return (Math.round(tempInC.toDouble() * 2))/2
}

def refresh() {
	def cmds = []
	cmds << zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType:1).format() //get Temperature
	cmds << zwave.meterV3.meterGet(scale: 0).format() //get kWh
	cmds << zwave.meterV3.meterGet(scale: 2).format() //get Watts
	cmds << zwave.meterV3.meterGet(scale: 4).format() //get Voltage
	cmds << zwave.multiChannelAssociationV2.multiChannelAssociationGet(groupingIdentifier: 1).format() //get channel association
	sendHubCommand(cmds, 1200)
	runIn(10, "checkParam")
}

def ping() {
	refresh()
}

def resetEnergyMeter() {
	sendHubCommand(zwave.meterV3.meterReset().format())
}

def on() {
	delayBetween([
		zwave.basicV1.basicSet(value: 0xFF).format(),
		zwave.switchBinaryV1.switchBinaryGet().format()
	])
}

def off() {
	delayBetween([
		zwave.basicV1.basicSet(value: 0x00).format(),
		zwave.switchBinaryV1.switchBinaryGet().format()
	])
}

def configure() {
	ping()
}

private parameterMap() {[
[title: "Relay Output Mode", description: "This Parameter determines the type of load connected to the device relay output. The output type can be NO – normal open (no contact/voltage switch the load OFF) or NC - normal close (output is contacted / there is a voltage to switch the load OFF)",
 name: "Selected Mode", options: [
			0: "NO - Normal Open",
			1: "NC - Normal Close"
	], paramNum: 7, size: 1, default: "0", type: "enum"],
 
[title: "Floor Sensor Resistance", description: "If an external floor NTC temperature sensor is used it is necessary to select the correct resistance value in kiloOhms (kΩ) of the sensor",
 name: "Selected Floor Resistance in kΩ", paramNum: 10, size: 1, default: 10, type: "number", min: 1, max: 100, unit: "kΩ"],

[title: "Temperature Sensor Calibration", description: "This Parameter defines the offset value for floor temperature. This value will be added or subtracted from the floor temperature sensor reading.Through the Z-Wave network the value of this Parameter should be x10, e.g. for 1.5°C set the value 15.",
 name: "Selected Temperature Offset in °Cx10", paramNum: 17, size: 1, default: 0, type: "number", min: -100, max: 100, unit: " °Cx10"],

[title: "Auto On/Off", description: "If this function is enabled the device will switch Off the relay output when there is no consumption and switch On the output again when the load is reconnected. It is possible to set a delay for Auto Off and Auto On functions in configurations (Auto Off Timeout) & (Auto On Reconnect Timeout) below",
 name: "Selected Mode", options: [
			0: "Auto On/Off Disabled",
			1: "Auto On/Off Enabled"
	], paramNum: 23, size: 1, default: "0", type: "enum"],

[title: "Auto Off Timeout", description: "If Auto On/Off is enabled, it is possible to delay the Auto Off function. The output will be switched Off when there is no consumption for the interval defined in minutes",
 name: "Seleced Auto Off Timeout in minutes", paramNum: 24, size: 1, default: 0, type: "number", min: 0, max: 120, unit: "min"],

[title: "Auto On Reconnect Timeout", description: "If Auto On/Off is enabled, it is possible to delay the Auto On function. When the load is reconnected the relay output will be switched On after the time defined in minutes",
 name: "Seleced Auto On Reconnect Timeout in minutes", paramNum: 25, size: 1, default: 5, type: "number", min: 0, max: 120, unit: "min"],
 
[title: "High Load Timeout Protection: Power Threshold", description: "If the HLS01 is used to control an electric socket, you can configure the device so that it automatically switch Off the socket if the potentially dangerous high load is connected longer than allowable time set below (High Load Timeout Protection: Time Threshold). Set the threshold value in watts, reaching which the connected load will be considered high The value of this parameter can be set from 100 to 3500 in watts. Use the value 0 if there is a need to disable this function.",
 name: "Selected Power Threshold in watts", paramNum: 26, size: 2, default: 0, type: "number", min: 0 , max: 3500, unit: "W"],

[title: "High Load Timeout Protection: Time Threshold", description: "If High Load Timeout Protection is activated: Power Threshold is enabled, use this parameter to set the threshold value in minutes. If the load is connected longer than this value, the device will automatically switch Off the socket. Use the value 0 if there is a need to disable this function.",
 name: "Selected Time Threshold in minutes", paramNum: 27, size: 2, default: 0, type: "number", min: 0 , max: 1440, unit: "min"],
 
[title: "External Input: Hold Control Mode", description: "This Parameter defines how the relay should react while holding the button connected to the external input. The options are: Hold is disabled, Operate like click, Momentary Switch: When the button is held, the relay output state is ON, as soon as the button is released the relay output state changes to OFF, Reversed Momentary: When the button is held, the relay output state is OFF, as soon as the button is released the relay output state changes to ON.",
 name: "Selected Hold Control Mode", options: [
			0: "Hold is disabled",
			1: "Operate like click",
			2: "Momentary Switch",
			3: "Reversed Momentary"
	], paramNum: 41, size: 1, default: "2", type: "enum"],

[title: "Hold Mode Duration for External Input S1", description: "This parameter specifies the time the device needs to recognize a hold mode when the button connected to an external input is held (key closed). This parameter is available on firmware V1.3 or higher",
 name: "Selected Duration in milliseconds", paramNum: 46, size: 2, default: 500, type: "number", min: 200 , max: 5000, unit: "ms"],

[title: "External Input: Click Control Mode", description: "This Parameter defines how the relay should react when clicking the button connected to the external input. The options are: Click is disabled, Toggle switch: relay inverts state (ON to OFF, OFF to ON), Only On: Relay switches to ON state only, Only Off: Relay switches to OFF state only, Timer: On > Off: Relay output switches to ON state (contacts are closed) then after a specified time switches back to OFF state (contacts are open). The time is specified in 'Relay Timer Mode Duration' below, Timer: Off > On: Relay output switches to OFF state (contacts are open) then after a specified time switches back to On state (contacts are closed). The time is specified in 'Relay Timer Mode Duration' below ",
 name: "Selected Click Control Mode", options: [
			0: "Click is disabled",
			1: "Toggle Switch",
			2: "Only On",
			3: "Only Off",
			4: "Timer: On > Off",
			5: "Timer: Off > On"
	], paramNum: 51, size: 1, default: "1", type: "enum"],

[title: "Relay Timer Mode Duration", description: "This parameters specify the duration in seconds for the Timer modes for Click Control Mode above. Press the button and the relay output goes to ON/OFF for the specified time then changes back to OFF/ON. If the value is set to “0” the relay output will operate as a short contact (duration is about 0.5 sec)",
 name: "Selected Timer Mode Duration in seconds", paramNum: 71, size: 2, default: 0, type: "number", min: 0 , max: 43200, unit: "s"],

[title: "Retore Relay State", description: "This parameter determines if the last relay state should be restored after power failure or not. This parameter is available on firmware V1.5 or higher",
 name: "Selected Mode", options: [
			0: "Relay Off After Power Failure",
			1: "Restore Last State"
	], paramNum: 66, size: 1, default: "0", type: "enum"],

[title: "Energy Consumption Meter Consecutive Report Interval", description: "When the device is connected to the gateway, it periodically sends reports from its energy consumption sensor even if there is no change in the value. This parameter defines the interval between consecutive reports of real time and cumulative energy consumption data to the gateway",
 name: "Selected Energy Report Interval in minutes", paramNum: 141, size: 1, default: 10, type: "number", min: 1 , max: 120, unit: "min"],

[title: "Energy Consumption Meter Report", description: "This Parameter determines the change in the load power resulting in the consumption report being sent to the gateway. Use the value 0 if there is a need to stop sending the reports.",
 name: "Selected Change Percentage", paramNum: 142, size: 1, default: 25, type: "number", min: 0 , max: 50, unit: "%"],

[title: "Sensors Consecutive Report Interval", description: "When the device is connected to the gateway, it periodically sends to the gateway reports from its external NTC temperature sensor even if there are not changes in the values. This Parameter defines the interval between consecutive reports",
 name: "Selected Energy Report Interval in minutes", paramNum: 143, size: 1, default: 10, type: "number", min: 1 , max: 120, unit: "min"],

[title: "External Temperature Sensor Report Threshold", description: "This Parameter determines the change in temperature level resulting in temperature sensors report being sent to the gateway. The value of this Parameter should be x10 for °C, e.g. for 0.4°C use value 4. Use the value 0 if there is a need to stop sending the reports.",
 name: "Selected Threshold in °Cx10", paramNum: 144, size: 1, default: 2, type: "number", min: 0 , max: 100, unit: " °Cx10"],

[title: "Overheat Protection", description: "You can define the maximum limit of temperature, reaching which the device will automatically switch Off the load. Use the value 0 if there is a need to disable this function",
 name: "Selected Limit in °C", paramNum: 153, size: 2, default: 60, type: "number", min: 0 , max: 120, unit: " °C"],

[title: "Over-Load Protection", description: "You can define the maximum power in Watt for connected load. The device will automatically switch off the output if the power consumed by the connected load exceeds this limit. Use the value 0 if there is a need to disable this function.",
 name: "Selected Limit in Watts", paramNum: 155, size: 2, default: 3500, type: "number", min: 0 , max: 4000, unit: "W"],

[title: "Over-Voltage Protection", description: "The device constantly monitors the voltage of your electricity network. You can define the maximum voltage of network exceeding which the device will automatically switch off the output. Use the value 0 if there is a need to disable this function.",
 name: "Selected Upper Limit in Volts", paramNum: 156, size: 2, default: 260, type: "number", min: 120 , max: 280, unit: "V"],

[title: "Voltage Drop Protection", description: "You can define the minimum voltage of your electricity network. If the voltage of the network drops bellow the determined level the device will automatically switch off the output. Use the value 0 if there is a need to disable this function.",
 name: "Selected Lower Limit in Volts", paramNum: 157, size: 2, default: 90, type: "number", min: 80 , max: 240, unit: "V"]

]}
