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

		fingerprint mfr: "0344", prod: "0004", model: "000A", inClusters:"0x5E,0x85,0x59,0x8E,0x55,0x86,0x72,0x5A,0x73,0x81,0x87,0x98,0x9F,0x6C,0x70,0x25,0x31,0x32,0x71,0x22,0x7A", deviceJoinName: "HELTUN HLS01 Switch"
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
		} else if (cmd.scale == 2) {
			map.name = "power"
			map.value = Math.round(cmd.scaledMeterValue)
			map.unit = "W"
			sendEvent(map)
		}else if (cmd.scale == 4) {
			map.name = "voltage"
			map.value = Math.round(cmd.scaledMeterValue)
			map.unit = "V"
			sendEvent(map)
		}else if (cmd.scale == 5) {
			map.name = "current"
			map.value = Math.round(cmd.scaledMeterValue)
			map.unit = "A"
		}      
	}
}

def roundC (tempInC) {
	return (Math.round(tempInC.toDouble() * 2))/2
}

def refresh() {
	def cmds = []
	cmds << new physicalgraph.device.HubAction(zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType:1).format())		// get Temperature
	cmds << new physicalgraph.device.HubAction(zwave.meterV3.meterGet(scale: 0).format())								// get kWh
	cmds << new physicalgraph.device.HubAction(zwave.meterV3.meterGet(scale: 2).format())								// get Watts
	cmds << new physicalgraph.device.HubAction(zwave.meterV3.meterGet(scale: 4).format())								// get Voltage    
	sendHubCommand(cmds, 1200)
}

def ping() {
	refresh()
}

def zwaveEvent(physicalgraph.zwave.commands.clockv1.ClockReport cmd) {
	def currDate = Calendar.getInstance(location.timeZone)
	def time = [hour: currDate.get(Calendar.HOUR_OF_DAY), minute: currDate.get(Calendar.MINUTE), weekday: currDate.get(Calendar.DAY_OF_WEEK)]
	if ((time.hour != cmd.hour) || (time.minute != cmd.minute) || (time.weekday != cmd.weekday)){
	sendHubCommand(new physicalgraph.device.HubAction(zwave.clockV1.clockSet(time).format()))
	}
}

def zwaveEvent(physicalgraph.zwave.commands.switchbinaryv1.SwitchBinaryReport cmd) {
	def state = cmd.value ? "on" : "off"
	sendEvent(name: "switch", value: state)
}

def resetEnergyMeter() {
	sendHubCommand(new physicalgraph.device.HubAction(zwave.meterV3.meterReset().format()))
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
	delayBetween([
		zwave.multiChannelAssociationV2.multiChannelAssociationRemove(groupingIdentifier: 1).format(),
		zwave.multiChannelAssociationV2.multiChannelAssociationSet(groupingIdentifier: 1, nodeId: 1).format(),
		ping()
	], 3000)
}