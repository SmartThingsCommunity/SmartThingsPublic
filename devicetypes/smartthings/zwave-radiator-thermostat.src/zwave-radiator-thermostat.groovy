/**
 *	Copyright 2019 SmartThings
 *
 *	Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *	in compliance with the License. You may obtain a copy of the License at:
 *
 *		http://www.apache.org/licenses/LICENSE-2.0
 *
 *	Unless required by applicable law or agreed to in writing, software distributed under the License is distributed

 *	on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *	for the specific language governing permissions and limitations under the License.
 *
 */
metadata {
	definition (name: "Z-Wave Radiator Thermostat", namespace: "smartthings", author: "SmartThings", ocfDeviceType: "oic.d.thermostat") {
		capability "Thermostat Mode"
		capability "Refresh"
		capability "Battery"
		capability "Thermostat Heating Setpoint"
		capability "Health Check"
		capability "Thermostat"
		capability "Temperature Measurement"

		fingerprint mfr: "0060", prod: "0015", model: "0001", deviceJoinName: "Everspring Thermostatic Radiator Valve", mnmn: "SmartThings", vid: "generic-radiator-thermostat"
		//this DTH is sending temperature setpoint commands using Celsius scale and assumes that they'll be handled correctly by device
		//if new device added to this DTH won't be able to do that, make sure to you'll handle conversion in a right way
	}

	tiles(scale: 2) {
		multiAttributeTile(name:"thermostat", type:"general", width:6, height:4, canChangeIcon: false)  {
			tileAttribute("device.thermostatMode", key: "PRIMARY_CONTROL") {
				attributeState("off", action:"switchMode", nextState:"...", icon: "st.thermostat.heating-cooling-off")
				attributeState("heat", action:"switchMode", nextState:"...", icon: "st.thermostat.heat")
				attributeState("emergency heat", action:"switchMode", nextState:"...", icon: "st.thermostat.emergency-heat")
			}
			tileAttribute("device.temperature", key: "SECONDARY_CONTROL") {
				attributeState("temperature", label:'${currentValue}°', icon: "st.alarm.temperature.normal",
						backgroundColors:[
								// Celsius
								[value: 0, color: "#153591"],
								[value: 7, color: "#1e9cbb"],
								[value: 15, color: "#90d2a7"],
								[value: 23, color: "#44b621"],
								[value: 28, color: "#f1d801"],
								[value: 35, color: "#d04e00"],
								[value: 37, color: "#bc2323"],
								// Fahrenheit
								[value: 40, color: "#153591"],
								[value: 44, color: "#1e9cbb"],
								[value: 59, color: "#90d2a7"],
								[value: 74, color: "#44b621"],
								[value: 84, color: "#f1d801"],
								[value: 95, color: "#d04e00"],
								[value: 96, color: "#bc2323"]
						]
				)
			}
			tileAttribute("device.heatingSetpoint", key: "HEATING_SETPOINT") {
				attributeState("default", label: '${currentValue}', unit: "°", defaultState: true)
			}
		}
		controlTile("thermostatMode", "device.thermostatMode", "enum", width: 2 , height: 2, supportedStates: "device.supportedThermostatModes") {
			state("off", action: "setThermostatMode", label: 'Off', icon: "st.thermostat.heating-cooling-off")
			state("heat", action: "setThermostatMode", label: 'Heat', icon: "st.thermostat.heat")
			state("emergency heat", action:"setThermostatMode", label: 'Emergency heat', icon: "st.thermostat.emergency-heat")
		}
		controlTile("heatingSetpoint", "device.heatingSetpoint", "slider",
				sliderType: "HEATING",
				debouncePeriod: 750,
				range: "device.heatingSetpointRange",
				width: 2, height: 2) {
			state "default", action:"setHeatingSetpoint", label:'${currentValue}', backgroundColor: "#E86D13"
		}
		valueTile("battery", "device.battery", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "battery", label: 'Battery:\n${currentValue}%', unit: "%"
		}
		standardTile("refresh", "command.refresh", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "refresh", label: 'refresh', action: "refresh.refresh", icon: "st.secondary.refresh-icon"
		}
		main "thermostat"
		details(["thermostat", "thermostatMode", "heatingSetpoint", "battery", "refresh"])
	}
}

def initialize() {
	sendEvent(name: "checkInterval", value: 4 * 60 * 60 + 24 * 60 , displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID, offlinePingable: "1"])
	sendEvent(name: "supportedThermostatModes", value: thermostatSupportedModes.encodeAsJson(), displayed: false)
	sendEvent(name: "heatingSetpointRange", value: [minHeatingSetpointTemperature, maxHeatingSetpointTemperature], displayed: false)
	response(refresh())
}

def installed() {
	initialize()
}

def updated() {
	initialize()
}

def parse(String description) {
	def result = null
	def cmd = zwave.parse(description)
	if (cmd) {
		result = zwaveEvent(cmd)
	} else {
		log.warn "${device.displayName} - no-parsed event: ${description}"
	}
	log.debug "Parse returned: ${result}"
	return result
}

def zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityMessageEncapsulation cmd) {
	def encapsulatedCommand = cmd.encapsulatedCommand()
	if (encapsulatedCommand) {
		log.debug "SecurityMessageEncapsulation into: ${encapsulatedCommand}"
		zwaveEvent(encapsulatedCommand)
	} else {
		log.warn "unable to extract secure command from $cmd"
		createEvent(descriptionText: cmd.toString())
	}
}



def zwaveEvent(physicalgraph.zwave.commands.batteryv1.BatteryReport cmd) {
	def value = cmd.batteryLevel == 255 ? 1 : cmd.batteryLevel
	def map = [name: "battery", value: value, unit: "%"]
	createEvent(map)
}

def zwaveEvent(physicalgraph.zwave.commands.thermostatmodev2.ThermostatModeReport cmd) {
	def map = [name: "thermostatMode", data:[supportedThermostatModes: thermostatSupportedModes.encodeAsJson()]]
	switch (cmd.mode) {
		case 1:
			map.value = "heat"
			break
		case 11:
			map.value = "emergency heat"
			break
		case 0:
			map.value = "off"
			break
	}
	createEvent(map)
}

def zwaveEvent(physicalgraph.zwave.commands.thermostatsetpointv2.ThermostatSetpointReport cmd) {
	def deviceTemperatureScale = cmd.scale ? 'F' : 'C'
	createEvent(name: "heatingSetpoint", value: convertTemperatureIfNeeded(cmd.scaledValue, deviceTemperatureScale, cmd.precision), unit: temperatureScale)
}

def zwaveEvent(physicalgraph.zwave.commands.sensormultilevelv5.SensorMultilevelReport cmd) {
	def deviceTemperatureScale = cmd.scale ? 'F' : 'C'
	createEvent(name: "temperature", value: convertTemperatureIfNeeded(cmd.scaledSensorValue, deviceTemperatureScale, cmd.precision), unit: temperatureScale)
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
	log.warn "Unhandled command: ${cmd}"
	[:]
}

def setThermostatMode(String mode) {
	def modeValue = 0
	if (thermostatSupportedModes.contains(mode)) {
		switch (mode) {
			case "heat":
				modeValue = 1
				break
			case "emergency heat":
				modeValue = 11
				break
			case "off":
				modeValue = 0
				break
		}
	} else {
		log.debug "Unsupported mode ${mode}"
	}

	[
			secure(zwave.thermostatModeV2.thermostatModeSet(mode: modeValue)),
			"delay 2000",
			secure(zwave.thermostatModeV2.thermostatModeGet())
	]
}

def heat() {
	setThermostatMode("heat")
}

def emergencyHeat() {
	setThermostatMode("emergency heat")
}

def off() {
	setThermostatMode("off")
}

def setHeatingSetpoint(setpoint) {
	setpoint = temperatureScale == 'C' ? setpoint : fahrenheitToCelsius(setpoint)
	setpoint = Math.max(Math.min(setpoint, maxHeatingSetpointTemperature), minHeatingSetpointTemperature)
	[
			secure(zwave.thermostatSetpointV2.thermostatSetpointSet([precision: 1, scale: 0, scaledValue: setpoint, setpointType: 1, size: 2])),
			"delay 2000",
			secure(zwave.thermostatSetpointV2.thermostatSetpointGet(setpointType: 1))
	]
}

def refresh() {
	def cmds = [
			secure(zwave.batteryV1.batteryGet()),
			secure(zwave.thermostatSetpointV2.thermostatSetpointGet(setpointType: 1)),
			secure(zwave.sensorMultilevelV5.sensorMultilevelGet()),
			secure(zwave.thermostatModeV2.thermostatModeGet())
	]

	delayBetween(cmds, 2500)
}

def ping() {
	refresh()
}

private secure(cmd) {
	if (zwaveInfo.zw.endsWith("s")) {
		zwave.securityV1.securityMessageEncapsulation().encapsulate(cmd).format()
	} else {
		cmd.format()
	}
}

private getMaxHeatingSetpointTemperature() {
	if (isEverspringRadiatorThermostat()) {
		temperatureScale == 'C' ? 35 : 95
	} else {
		temperatureScale == 'C' ? 30 : 86
	}
}

private getMinHeatingSetpointTemperature() {
	if (isEverspringRadiatorThermostat()) {
		temperatureScale == 'C' ? 15 : 59
	} else {
		temperatureScale == 'C' ? 10 : 50
	}
}

private getThermostatSupportedModes() {
	if (isEverspringRadiatorThermostat()) {
		["off", "heat", "emergency heat"]
	} else {
		["off","heat"]
	}
}

private isEverspringRadiatorThermostat() {
	zwaveInfo.mfr == "0060" && zwaveInfo.prod == "0015"
}
