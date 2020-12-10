/**
 *	Copyright 2018 SmartThings
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
	definition (name: "Fibaro Heat Controller", namespace: "smartthings", author: "Samsung", ocfDeviceType: "oic.d.thermostat") {
		capability "Thermostat Mode"
		capability "Refresh"
		capability "Battery"
		capability "Thermostat Heating Setpoint"
		capability "Health Check"
		capability "Thermostat"
		capability "Thermostat Mode"
		capability "Temperature Measurement"

		command "setThermostatSetpointUp"
		command "setThermostatSetpointDown"
		command "switchMode"

		fingerprint mfr: "010F", prod: "1301", model: "1000", deviceJoinName: "Fibaro Thermostat" //Fibaro Heat Controller
	}

	tiles(scale: 2) {
		multiAttributeTile(name:"thermostat", type:"general", width:6, height:4, canChangeIcon: false)  {
			tileAttribute("device.heatingSetpoint", key: "VALUE_CONTROL") {
				attributeState("VALUE_UP", action: "setThermostatSetpointUp")
				attributeState("VALUE_DOWN", action: "setThermostatSetpointDown")
			}
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
		}

		valueTile("battery", "device.battery", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "battery", label: 'Battery:\n${currentValue}%', unit: "%"
		}
		standardTile("refresh", "command.refresh", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "refresh", label: 'refresh', action: "refresh.refresh", icon: "st.secondary.refresh-icon"
		}
		main "thermostat"
		details(["thermostat", "battery", "refresh"])
	}
}

def installed() {
	log.debug "installed()"
	state.supportedModes = ["off", "emergency heat", "heat"]

	sendEvent(name: "temperature", value: 0, unit: "C", displayed: false)
	sendEvent(name: "supportedThermostatModes", value: state.supportedModes, displayed: false)

	runIn(2, "updated", [overwrite: true])
}

def updated() {
	log.debug "updated()"

	sendEvent(name: "checkInterval", value: 2 * 60 * 60 + 2 * 60, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID, offlinePingable: "1"])

	runIn(5, "forcedRefresh", [overwrite: true])
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

def zwaveEvent(physicalgraph.zwave.commands.multichannelv3.MultiChannelCmdEncap cmd) {
	if (cmd.commandClass == 0x6C && cmd.parameter.size >= 4) { // Supervision encapsulated Message
		// Supervision header is 4 bytes long, two bytes dropped here are the latter two bytes of the supervision header
		cmd.parameter = cmd.parameter.drop(2)
		// Updated Command Class/Command now with the remaining bytes
		cmd.commandClass = cmd.parameter[0]
		cmd.command = cmd.parameter[1]
		cmd.parameter = cmd.parameter.drop(2)
	}
	def encapsulatedCommand = cmd.encapsulatedCommand()
	if (encapsulatedCommand) {
		log.debug "MultiChannel Encapsulation: ${encapsulatedCommand}"
		zwaveEvent(encapsulatedCommand, cmd.sourceEndPoint)
	} else {
		log.warn "unable to extract multi channel command from $cmd"
		createEvent(descriptionText: cmd.toString())
	}
}

def zwaveEvent(physicalgraph.zwave.commands.batteryv1.BatteryReport cmd, sourceEndPoint = null) {
	def value = cmd.batteryLevel == 255 ? 1 : cmd.batteryLevel
	def map = [name: "battery", value: value, unit: "%"]
	def result = [:]

	if (!sourceEndPoint || sourceEndPoint == 1) {
		result = createEvent(map)
	} else if (sourceEndPoint == 2) {
		if (childDevices) {
			sendEventToChild(map)
		}
	}

	result
}

def zwaveEvent(physicalgraph.zwave.commands.thermostatmodev2.ThermostatModeReport cmd, sourceEndPoint = null) {
	def mode
	switch (cmd.mode) {
		case 1:
			mode = "heat"
			break
		case 31:
			mode = "emergency heat"
			break
		case 0:
			mode = "off"
			break
	}

	createEvent(name: "thermostatMode", value: mode, data: [supportedThermostatModes: state.supportedModes])
}

def zwaveEvent(physicalgraph.zwave.commands.thermostatsetpointv2.ThermostatSetpointReport cmd, sourceEndPoint = null) {
	createEvent(name: "heatingSetpoint", value: convertTemperatureIfNeeded(cmd.scaledValue, 'C', cmd.precision), unit: temperatureScale)
}

def zwaveEvent(physicalgraph.zwave.commands.sensormultilevelv5.SensorMultilevelReport cmd, sourceEndPoint = null) {
	def map = [name: "temperature", value: convertTemperatureIfNeeded(cmd.scaledSensorValue, 'C', cmd.precision), unit: temperatureScale]
	if (map.value != "-100.0") {
		if (state.isTemperatureReportAbleToChangeStatus) {
			changeTemperatureSensorStatus("online")
			sendEventToChild(map)
		}
		createEvent(map)
	} else {
		changeTemperatureSensorStatus("offline")
		response(secureEncap(zwave.configurationV2.configurationGet(parameterNumber: 3)))
	}
}

def zwaveEvent(physicalgraph.zwave.commands.configurationv2.ConfigurationReport cmd) {
	if (cmd.parameterNumber == 3) {
		if (cmd.scaledConfigurationValue == 1) {
			if (!childDevices) {
				addChild()
			} else {
				refreshChild()
			}
			state.isTemperatureReportAbleToChangeStatus = true
			changeTemperatureSensorStatus("online")
		} else if (cmd.scaledConfigurationValue == 0 && childDevices) {
			state.isTemperatureReportAbleToChangeStatus = false
			changeTemperatureSensorStatus("offline")
		}
	}
}

def zwaveEvent(physicalgraph.zwave.commands.notificationv3.NotificationReport cmd, sourceEndPoint = null) {
	log.debug "Notification: ${cmd}"
}

def zwaveEvent(physicalgraph.zwave.commands.applicationstatusv1.ApplicationBusy cmd) {
	log.warn "Device is busy, delaying refresh"
	runIn(15, "forcedRefresh", [overwrite: true])
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
	log.warn "Unhandled command: ${cmd}"
	[:]
}

def setThermostatMode(String mode) {
	def modeValue = 0
	switch (mode) {
		case "heat":
			modeValue = 1
			break
		case "emergency heat":
			modeValue = 31
			break
		case "off":
			modeValue = 0
			break
	}

	[
			secureEncap(zwave.thermostatModeV2.thermostatModeSet(mode: modeValue)),
			"delay 2000",
			secureEncap(zwave.thermostatModeV2.thermostatModeGet())
	]
}

def heat() {
	setThermostatMode("heat")
}

def off() {
	setThermostatMode("off")
}

def emergencyHeat() {
	setThermostatMode("emergency heat")
}

def setHeatingSetpoint(setpoint) {
	setpoint = temperatureScale == 'C' ? setpoint : fahrenheitToCelsius(setpoint)
	[
			secureEncap(zwave.thermostatSetpointV2.thermostatSetpointSet([precision: 1, scale: 0, scaledValue: setpoint, setpointType: 1, size: 2])),
			"delay 2000",
			secureEncap(zwave.thermostatSetpointV2.thermostatSetpointGet(setpointType: 1))
	]
}

def setThermostatSetpointUp() {
	def setpoint = device.latestValue("heatingSetpoint")
	if (setpoint < maxHeatingSetpointTemperature) {
		setpoint = setpoint + (temperatureScale == 'C' ? 0.5 : 1)
	}
	setHeatingSetpoint(setpoint)
}

def setThermostatSetpointDown() {
	def setpoint = device.latestValue("heatingSetpoint")
	if (setpoint > minHeatingSetpointTemperature) {
		setpoint = setpoint - (temperatureScale == 'C' ? 0.5 : 1)
	}
	setHeatingSetpoint(setpoint)
}

def refresh() {
	def cmds = [
			secureEncap(zwave.configurationV2.configurationGet(parameterNumber: 3)),
			secureEncap(zwave.batteryV1.batteryGet(), 1),
			secureEncap(zwave.batteryV1.batteryGet(), 2),
			secureEncap(zwave.thermostatSetpointV2.thermostatSetpointGet(setpointType: 1)),
			secureEncap(zwave.thermostatModeV2.thermostatModeGet()),
			secureEncap(zwave.sensorMultilevelV5.sensorMultilevelGet()),
			secureEncap(zwave.sensorMultilevelV5.sensorMultilevelGet(), 2)
	]

	delayBetween(cmds, 2500)
}

def ping() {
	refresh()
}

private secureEncap(cmd, endpoint = null) {
	secure(encap(cmd, endpoint))
}

private secure(cmd) {
	if (zwaveInfo.zw.contains("s")) {
		zwave.securityV1.securityMessageEncapsulation().encapsulate(cmd).format()
	} else {
		cmd.format()
	}
}

private encap(cmd, endpoint = null) {
	if (endpoint) {
		zwave.multiChannelV3.multiChannelCmdEncap(destinationEndPoint:endpoint).encapsulate(cmd)
	} else {
		cmd
	}
}

def switchMode() {
	def currentMode = device.currentValue("thermostatMode")
	def supportedModes = state.supportedModes
	if (supportedModes && supportedModes.size()) {
		def next = { supportedModes[supportedModes.indexOf(it) + 1] ?: supportedModes[0] }
		def nextMode = next(currentMode)
		setThermostatMode(nextMode)
	} else {
		log.warn "supportedModes not defined"
	}
}

def sendEventToChild(event, forced = false) {
	String childDni = "${device.deviceNetworkId}:2"
	def child = childDevices.find { it.deviceNetworkId == childDni }
	if (state.isChildOnline || forced)
		child?.sendEvent(event)
}

def configureChild() {
	sendEventToChild(createEvent(name: "DeviceWatch-Enroll", value: [protocol: "zwave", scheme:"untracked"].encodeAsJson(), displayed: false), true)
}

private refreshChild() {
	def cmds = [
			secureEncap(zwave.batteryV1.batteryGet(), 2),
			secureEncap(zwave.sensorMultilevelV5.sensorMultilevelGet(), 2)
	]
	sendHubCommand(cmds, 2000)
}

private forcedRefresh() {
	sendHubCommand(refresh())
}

def addChild() {
	String childDni = "${device.deviceNetworkId}:2"
	String componentLabel =	 "Fibaro Temperature Sensor"

	addChildDevice("Child Temperature Sensor", childDni, device.hub.id,[completedSetup: true, label: componentLabel, isComponent: false])
}

private getMaxHeatingSetpointTemperature() {
	temperatureScale == 'C' ? 30 : 86
}

private getMinHeatingSetpointTemperature() {
	temperatureScale == 'C' ? 10 : 50
}

private changeTemperatureSensorStatus(status) {
	state.isChildOnline = (status == "online")
	def map = [name: "DeviceWatch-DeviceStatus", value: status]
	sendEventToChild(map, true)
}