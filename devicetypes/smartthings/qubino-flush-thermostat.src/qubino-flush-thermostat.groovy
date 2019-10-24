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
	definition (name: "Qubino Flush Thermostat", namespace: "smartthings", author: "SmartThings", ocfDeviceType: "oic.d.thermostat") {
		capability "Thermostat"
		capability "Thermostat Mode"
		capability "Thermostat Heating Setpoint"
		capability "Thermostat Cooling Setpoint"
		capability "Thermostat Operating State"
		capability "Temperature Measurement"
		capability "Power Meter"
		capability "Energy Meter"
		capability "Configuration"
		capability "Refresh"
		capability "Health Check"

		command "changeMode"

		fingerprint mfr: "0159", prod: "0005", model: "0054", deviceJoinName: "Qubino Flush On/Off Thermostat 2"
	}

	tiles(scale: 2) {
		multiAttributeTile(name:"thermostat", type:"general", width:6, height:4, canChangeIcon: false)  {
			tileAttribute("device.thermostatMode", key: "PRIMARY_CONTROL") {
				attributeState("off", icon: "st.thermostat.heating-cooling-off")
				attributeState("heat", icon: "st.thermostat.heat")
				attributeState("cool", icon: "st.thermostat.emergency-heat")
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
			state("cool", action: "setThermostatMode", label: 'Cool', icon: "st.thermostat.cool")
		}
		controlTile("heatingSetpoint", "device.heatingSetpoint", "slider",
				sliderType: "HEATING",
				debouncePeriod: 750,
				range: "device.setpointRange",
				width: 2, height: 2) {
			state "default", action:"setHeatingSetpoint", label:'${currentValue}', backgroundColor: "#E86D13"
		}
		controlTile("coolingSetpoint", "device.coolingSetpoint", "slider",
				sliderType: "COOLING",
				debouncePeriod: 750,
				range: "device.setpointRange",
				width: 2, height: 2) {
			state "default", action:"setCoolingSetpoint", label:'${currentValue}', backgroundColor: "#55D4ED"
		}
		standardTile("refresh", "command.refresh", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "refresh", label: 'refresh', action: "refresh.refresh", icon: "st.secondary.refresh-icon"
		}
		valueTile("power", "device.power", width: 2, height: 2) {
			state "default", label:'${currentValue} W'
		}
		valueTile("energy", "device.energy", width: 2, height: 2) {
			state "default", label:'${currentValue} kWh'
		}
		standardTile("changeMode", "device.changeMode", width: 2 , height: 2, inactiveLabel: false, decoration: "flat") {
			state("default", action: "changeMode", label: 'Push to switch mode', nextState: "unpair")
			state("unpair", label: 'Unpair and pair device again')
		}
		main "thermostat"
		details(["thermostat", "thermostatMode", "heatingSetpoint", "coolingSetpoint", "refresh", "power", "energy", "changeMode"])
	}

	preferences {
		input (
				title: "Thermostat Mode:",
				description: "This setting allows to change mode of the device. Remember to unpair to pair device again after change.",
				name: "paramMode",
				type: "enum",
				options: ["Heat", "Cool"]
		)
	}
}

def installed() {
	state.isThermostatModeSet = false
	sendEvent(name: "checkInterval", value: 2 * 60 * 60 + 12 * 60 , displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID, offlinePingable: "1"])
	sendEvent(name: "setpointRange", value: [minSetpointTemperature, maxSetpointTemperature], displayed: false)
	response(refresh())
}

def updated() {
	if (paramMode)
		!state.supportedModes.contains(paramMode) ? changeMode() : [:]
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

def zwaveEvent(physicalgraph.zwave.commands.thermostatmodev2.ThermostatModeReport cmd) {
	def map = [name: "thermostatMode", data:[supportedThermostatModes: state.supportedModes.encodeAsJson()]]
	switch (cmd.mode) {
		case physicalgraph.zwave.commands.thermostatmodev2.ThermostatModeReport.MODE_OFF:
			map.value = "off"
			break
		case physicalgraph.zwave.commands.thermostatmodev2.ThermostatModeReport.MODE_HEAT:
			map.value = "heat"
			break
		case physicalgraph.zwave.commands.thermostatmodev2.ThermostatModeReport.MODE_COOL:
			map.value = "cool"
			break
	}
	createEvent(map)
}

def zwaveEvent(physicalgraph.zwave.commands.thermostatsetpointv2.ThermostatSetpointReport cmd) {
	def map = [:]
	switch (cmd.setpointType) {
		case physicalgraph.zwave.commands.thermostatsetpointv2.ThermostatSetpointReport.SETPOINT_TYPE_HEATING_1:
			map.name = "heatingSetpoint"
			break
		case physicalgraph.zwave.commands.thermostatsetpointv2.ThermostatSetpointReport.SETPOINT_TYPE_COOLING_1:
			map.name = "coolingSetpoint"
			break
	}
	map.value = convertTemperatureIfNeeded(cmd.scaledValue, cmd.scale ? 'F' : 'C', cmd.precision)
	map.unit = temperatureScale
	createEvent(map)
}

def zwaveEvent(physicalgraph.zwave.commands.thermostatoperatingstatev2.ThermostatOperatingStateReport cmd) {
	def map = [name: "thermostatOperatingState"]
	switch (cmd.operatingState) {
		case physicalgraph.zwave.commands.thermostatoperatingstatev1.ThermostatOperatingStateReport.OPERATING_STATE_IDLE:
			map.value = "idle"
			break
		case physicalgraph.zwave.commands.thermostatoperatingstatev1.ThermostatOperatingStateReport.OPERATING_STATE_HEATING:
			map.value = "heating"
			break
		case physicalgraph.zwave.commands.thermostatoperatingstatev1.ThermostatOperatingStateReport.OPERATING_STATE_COOLING:
			map.value = "cooling"
			break
	}
	createEvent(map)
}

def zwaveEvent(physicalgraph.zwave.commands.meterv3.MeterReport cmd) {
	if (cmd.meterType == 1) {
		if (cmd.scale == 0) {
			createEvent(name: "energy", value: cmd.scaledMeterValue, unit: "kWh")
		} else if (cmd.scale == 2) {
			createEvent(name: "power", value: Math.round(cmd.scaledMeterValue), unit: "W")
		}
	}
}

def zwaveEvent(physicalgraph.zwave.commands.sensormultilevelv5.SensorMultilevelReport cmd) {
	def deviceTemperatureScale = cmd.scale ? 'F' : 'C'
	createEvent(name: "temperature", value: convertTemperatureIfNeeded(cmd.scaledSensorValue, deviceTemperatureScale, cmd.precision), unit: temperatureScale)
}

def zwaveEvent(physicalgraph.zwave.commands.configurationv2.ConfigurationReport cmd) {
	state.supportedModes = ["off"]
	//this device doesn't act like normal thermostat, it can support either 'cool' or 'heat' after configuration
	if (cmd.parameterNumber == 59 && !state.isThermostatModeSet) {
		state.supportedModes.add(cmd.scaledConfigurationValue ? "cool" : "heat")
		state.isThermostatModeSet = true
	}
	createEvent(name: "supportedThermostatModes", value: state.supportedModes.encodeAsJson(), displayed: false)
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
	log.warn "Unhandled command: ${cmd}"
	[:]
}

def setThermostatMode(String mode) {
	def modeValue = 0
	if (state.supportedModes.contains(mode)) {
		switch (mode) {
			case "off":
				modeValue = physicalgraph.zwave.commands.thermostatmodev2.ThermostatModeSet.MODE_OFF
				break
			case "heat":
				modeValue = physicalgraph.zwave.commands.thermostatmodev2.ThermostatModeSet.MODE_HEAT
				break
			case "cool":
				modeValue = physicalgraph.zwave.commands.thermostatmodev2.ThermostatModeSet.MODE_COOL
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

def setTemperatureSetpoint(temperatureSetpoint) {
	if (state.supportedModes.contains("heat")) {
		sendHubCommand(setHeatingSetpoint(temperatureSetpoint))
	} else {
		sendHubCommand(setCoolingSetpoint(temperatureSetpoint))
	}
}

def heat() {
	setThermostatMode("heat")
}

def cool() {
	setThermostatMode("cool")
}

def off() {
	setThermostatMode("off")
}

def setHeatingSetpoint(setpoint) {
	updateSetpoint(setpoint, physicalgraph.zwave.commands.thermostatsetpointv2.ThermostatSetpointSet.SETPOINT_TYPE_HEATING_1)
}

def setCoolingSetpoint(setpoint) {
	updateSetpoint(setpoint, physicalgraph.zwave.commands.thermostatsetpointv2.ThermostatSetpointSet.SETPOINT_TYPE_COOLING_1)
}

def updateSetpoint(setpoint, setpointType) {
	setpoint = temperatureScale == 'C' ? setpoint : fahrenheitToCelsius(setpoint)
	setpoint = Math.max(Math.min(setpoint, maxSetpointTemperature), minSetpointTemperature)
	[
			secure(zwave.thermostatSetpointV2.thermostatSetpointSet([precision: 1, scale: 0, scaledValue: setpoint, setpointType: setpointType, size: 2])),
			"delay 2000",
			secure(zwave.thermostatSetpointV2.thermostatSetpointGet(setpointType: setpointType))
	]
}

def configure() {
	secure(zwave.configurationV1.configurationGet(parameterNumber: 59))
}

def refresh() {
	def cmds = [
			secure(zwave.thermostatSetpointV2.thermostatSetpointGet(setpointType: 1)),
			secure(zwave.sensorMultilevelV5.sensorMultilevelGet()),
			secure(zwave.thermostatModeV2.thermostatModeGet()),
			secure(zwave.thermostatOperatingStateV2.thermostatOperatingStateGet()),
			secure(zwave.thermostatSetpointV2.thermostatSetpointGet(setpointType: currentSetpointType)),
			secure(zwave.meterV2.meterGet(scale: 0)),
			secure(zwave.meterV2.meterGet(scale: 2))
	]

	delayBetween(cmds, 2500)
}

def ping() {
	refresh()
}

def changeMode() {
	if (state.supportedModes.contains("heat")) {
		sendHubCommand(zwave.configurationV1.configurationSet(parameterNumber: 59, scaledConfigurationValue: 1))
	} else {
		sendHubCommand(zwave.configurationV1.configurationSet(parameterNumber: 59, scaledConfigurationValue: 0))
	}
}

private secure(cmd) {
	if (zwaveInfo.zw.contains("s")) {
		zwave.securityV1.securityMessageEncapsulation().encapsulate(cmd).format()
	} else {
		cmd.format()
	}
}

private getCurrentSetpointType() {
	state.supportedModes?.contains("heat") ?
			physicalgraph.zwave.commands.thermostatsetpointv2.ThermostatSetpointSet.SETPOINT_TYPE_HEATING_1 :
			physicalgraph.zwave.commands.thermostatsetpointv2.ThermostatSetpointSet.SETPOINT_TYPE_COOLING_1
}

private getMaxSetpointTemperature() {
		temperatureScale == 'C' ? 40 : 104
}

private getMinSetpointTemperature() {
		temperatureScale == 'C' ? -12 : 11
}