/*
 *  Copyright 2017 - 2018 Stelpro
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
 *  Stelpro Ki Thermostat
 *
 *  Author: Stelpro
 *
 *  Date: 2018-04-24
 */
import physicalgraph.zwave.commands.*

metadata {
	definition (name: "Stelpro Ki Thermostat", namespace: "stelpro", author: "Stelpro", ocfDeviceType: "oic.d.thermostat") {
		capability "Actuator"
		capability "Temperature Measurement"
		capability "Temperature Alarm"
		capability "Thermostat"
		capability "Thermostat Mode"
		capability "Thermostat Operating State"
		capability "Thermostat Heating Setpoint"
		capability "Configuration"
		capability "Sensor"
		capability "Refresh"
		capability "Health Check"

		// Right now this can disrupt device health if the device is currently offline -- it would be erroneously marked online.
		//attribute "outsideTemp", "number"

		command "setOutdoorTemperature"
		command "quickSetOutTemp" // Maintain backward compatibility with self published versions of the "Stelpro Get Remote Temperature" SmartApp
		command "increaseHeatSetpoint"
		command "decreaseHeatSetpoint"
		command "eco" // Command does not exist in "Thermostat Mode"
		command "updateWeather"

		fingerprint deviceId: "0x0806", inClusters: "0x5E,0x86,0x72,0x40,0x43,0x31,0x85,0x59,0x5A,0x73,0x20,0x42", mfr: "0239", prod: "0001", model: "0001", deviceJoinName: "Stelpro Thermostat" //Stelpro Ki Thermostat
	}

	// simulator metadata
	simulator { }

	preferences {
		section {
			input("heatdetails", "enum", title: "Do you want a detailed operating state notification?", options: ["No", "Yes"], defaultValue: "No", required: true, displayDuringSetup: true)
		}
		section {
			input title: "Outdoor Temperature", description: "To get the current outdoor temperature to display on your thermostat enter your zip code or postal code below and make sure that your SmartThings location has a Geolocation configured (typically used for geofencing).", displayDuringSetup: false, type: "paragraph", element: "paragraph"
			input("zipcode", "text", title: "ZipCode (Outdoor Temperature)", description: "[Do not use space](Blank = No Forecast)")
		}
	}

	tiles(scale : 2) {
		multiAttributeTile(name:"thermostatMulti", type:"thermostat", width:6, height:4, canChangeIcon: true) {
			tileAttribute("device.temperature", key: "PRIMARY_CONTROL") {
				attributeState("temperature", label:'${currentValue}°', icon: "st.alarm.temperature.normal")
			}
			tileAttribute("device.heatingSetpoint", key: "VALUE_CONTROL") {
				attributeState("VALUE_UP", action: "increaseHeatSetpoint")
				attributeState("VALUE_DOWN", action: "decreaseHeatSetpoint")
			}
			tileAttribute("device.thermostatOperatingState", key: "OPERATING_STATE") {
				attributeState("idle", backgroundColor:"#44b621")
				attributeState("heating", backgroundColor:"#ffa81e")
			}
			tileAttribute("device.thermostatMode", key: "THERMOSTAT_MODE") {
				attributeState("heat", label:'${name}')
				attributeState("eco", label:'${name}')
			}
			tileAttribute("device.heatingSetpoint", key: "HEATING_SETPOINT") {
				attributeState("heatingSetpoint", label:'${currentValue}°')
			}
		}
		standardTile("mode", "device.thermostatMode", width: 2, height: 2) {
			state "heat", label:'${name}', action:"eco", nextState:"eco", icon:"st.Home.home29"
			state "eco", label:'${name}', action:"heat", nextState:"heat", icon:"st.Outdoor.outdoor3"
		}
		valueTile("heatingSetpoint", "device.heatingSetpoint", width: 2, height: 2) {
			state "heatingSetpoint", label:'Setpoint ${currentValue}°', backgroundColors:[
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
		}
		standardTile("temperatureAlarm", "device.temperatureAlarm", decoration: "flat", width: 2, height: 2) {
			state "default", label: 'No Alarm', icon: "st.alarm.temperature.normal", backgroundColor: "#ffffff"
			state "cleared", label: 'No Alarm', icon: "st.alarm.temperature.normal", backgroundColor: "#ffffff"
			state "freeze", label: 'Freeze', icon: "st.alarm.temperature.freeze", backgroundColor: "#bc2323"
			state "heat", label: 'Overheat', icon: "st.alarm.temperature.overheat", backgroundColor: "#bc2323"
		}
		standardTile("refresh", "device.refresh", decoration: "flat", width: 2, height: 2) {
			state "default", action:"refresh.refresh", icon:"st.secondary.refresh"
		}

		main ("thermostatMulti")
		details(["thermostatMulti", "mode", "heatingSetpoint", "temperatureAlarm", "refresh"])
	}
}

def getSupportedThermostatModes() {
	["heat", "eco"]
}

def getMinSetpointIndex() {
	0
}
def getMaxSetpointIndex() {
	1
}
def getThermostatSetpointRange() {
	(getTemperatureScale() == "C") ? [5, 30] : [41, 86]
}
def getHeatingSetpointRange() {
	thermostatSetpointRange
}

def getSetpointStep() {
	(getTemperatureScale() == "C") ? 0.5 : 1.0
}

def setupHealthCheck() {
	// Device-Watch simply pings if no device events received for 32min(checkInterval)
	sendEvent(name: "checkInterval", value: 2 * 15 * 60 + 2 * 60, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])
}

def configureSupportedRanges() {
	sendEvent(name: "supportedThermostatModes", value: supportedThermostatModes, displayed: false)
	// These are part of the deprecated Thermostat capability. Remove these when that capability is removed.
	sendEvent(name: "thermostatSetpointRange", value: thermostatSetpointRange, displayed: false)
	sendEvent(name: "heatingSetpointRange", value: heatingSetpointRange, displayed: false)
}

def installed() {
	sendEvent(name: "temperatureAlarm", value: "cleared", displayed: false)

	setupHealthCheck()

	configureSupportedRanges()
}

def updated() {
	setupHealthCheck()

	configureSupportedRanges()

	unschedule(scheduledUpdateWeather)
	if (settings.zipcode) {
		state.invalidZip = false // Reset and validate the zip-code later
		runEvery1Hour(scheduledUpdateWeather)
		scheduledUpdateWeather()
	}
}

def parse(String description) {
	// If the user installed with an old DTH version, update so that the new mobile client will work
	if (!device.currentValue("supportedThermostatModes")) {
		configureSupportedRanges()
	}
	// Existing installations need the temperatureAlarm state initialized
	if (device.currentValue("temperatureAlarm") == null) {
		sendEvent(name: "temperatureAlarm", value: "cleared", displayed: false)
	}

	if (description == "updated") {
		return null
	}

	// Class, version
	def map = createEvent(zwaveEvent(zwave.parse(description, [0x40:2, 0x43:2, 0x31:3, 0x42:1, 0x20:1, 0x85: 2])))
	if (!map) {
		return null
	}

	def result = [map]
	// This logic is to appease the (now deprecated but still sort-of used) consolidated
	// Thermostat capability gods.
	if (map.isStateChange && map.name == "heatingSetpoint") {
		result << createEvent([
				name: "thermostatSetpoint",
				value: map.value,
				unit: map.unit,
				data: [thermostatSetpointRange: thermostatSetpointRange]
			])
	}

	log.debug "Parse returned $result"
	result
}

def updateWeather() {
	log.debug "updating weather"
	def weather
	// If there is a zipcode defined, weather forecast will be sent. Otherwise, no weather forecast.
	if (settings.zipcode) {
		log.debug "ZipCode: ${settings.zipcode}"
		try {
			// If we do not have a zip-code setting we've determined as invalid, try to use the zip-code defined.
			if (!state.invalidZip) {
				weather = getTwcConditions(settings.zipcode)
			}
		} catch (e) {
			log.debug "getTwcConditions exception: $e"
			// There was a problem obtaining the weather with this zip-code, so fall back to the hub's location and note this for future runs.
			state.invalidZip = true
		}

		if (!weather) {
			try {
				// It is possible that a non-U.S. zip-code was used, so try with the location's lat/lon.
				if (location?.latitude && location?.longitude) {
					// Restrict to two decimal places for the API
					weather = getTwcConditions(sprintf("%.2f,%.2f", location.latitude, location.longitude))
				}
			} catch (e2) {
				log.debug "getTwcConditions exception: $e2"
				weather = null
			}
		}

		// Either the location lat,lon was invalid or one was not defined for the location, on top of an error with the given zip-code
		if (!weather) {
			log.debug("Something went wrong, no data found.")
		} else {
			def locationScale = getTemperatureScale()
			def tempToSend = weather.temperature
			log.debug("Outdoor Temperature: ${tempToSend} ${locationScale}")
			// Right now this can disrupt device health if the device is
			// currently offline -- it would be erroneously marked online.
			//sendEvent( name: 'outsideTemp', value: tempToSend )
			setOutdoorTemperature(tempToSend)
		}
	}
}

def scheduledUpdateWeather() {
	def actions = updateWeather()

	if (actions) {
		sendHubCommand(actions)
	}
}

// Command Implementations

/**
 * PING is used by Device-Watch in attempt to reach the Device
 **/
def ping() {
	log.debug "ping()"
	zwave.sensorMultilevelV3.sensorMultilevelGet().format()
}

def poll() {
	log.debug "poll()"
	delayBetween([
			updateWeather(),
			zwave.thermostatOperatingStateV1.thermostatOperatingStateGet().format(),
			zwave.thermostatModeV2.thermostatModeGet().format(),
			zwave.thermostatSetpointV2.thermostatSetpointGet(setpointType: 1).format(),
			zwave.sensorMultilevelV3.sensorMultilevelGet().format() // current temperature
		], 100)
}

// Event Generation
def zwaveEvent(thermostatsetpointv2.ThermostatSetpointReport cmd) {
	def cmdScale = cmd.scale == 1 ? "F" : "C"
	def temp;
	float tempfloat;
	def map = [:]

	if (cmd.scaledValue >= 327 ||
		cmd.setpointType != thermostatsetpointv2.ThermostatSetpointReport.SETPOINT_TYPE_HEATING_1) {
		return [:]
	}
	temp = convertTemperatureIfNeeded(cmd.scaledValue, cmdScale, cmd.precision)
	tempfloat = (Math.round(temp.toFloat() * 2)) / 2
	map.value = tempfloat

	map.unit = getTemperatureScale()
	map.displayed = false
	map.name = "heatingSetpoint"
	map.data = [heatingSetpointRange: heatingSetpointRange]

	// So we can respond with same format
	state.size = cmd.size
	state.scale = cmd.scale
	state.precision = cmd.precision

	map
}

def zwaveEvent(sensormultilevelv3.SensorMultilevelReport cmd) {
	def temp
	float tempfloat
	def format
	def map = [:]

	if (cmd.sensorType == sensormultilevelv3.SensorMultilevelReport.SENSOR_TYPE_TEMPERATURE_VERSION_1) {
		temp = convertTemperatureIfNeeded(cmd.scaledSensorValue, cmd.scale == 1 ? "F" : "C", cmd.precision)

		// The specific values checked below represent ambient temperature alarm indicators
		if (temp == 0x7ffd) { // Freeze Alarm
			map.name = "temperatureAlarm"
			map.value = "freeze"
		} else if (temp == 0x7fff) { // Overheat Alarm
			map.name = "temperatureAlarm"
			map.value = "heat"
		} else if (temp == 0x8000) { // Temperature Sensor Error
			map.descriptionText = "Received a temperature error"
		} else {
			map.name = "temperature"
			map.value = (Math.round(temp.toFloat() * 2)) / 2
			map.unit = getTemperatureScale()


			// Handle cases where we need to update the temperature alarm state given certain temperatures
			// Account for a f/w bug where the freeze alarm doesn't trigger at 0C
			if (map.value <= (map.unit == "C" ? 0 : 32)) {
				log.debug "EARLY FREEZE ALARM @ $map.value $map.unit (raw $intVal)"
				sendEvent(name: "temperatureAlarm", value: "freeze")
			}
			// Overheat alarm doesn't trigger until 80C, but we'll start sending at 50C to match thermostat display
			else if (map.value >= (map.unit == "C" ? 50 : 122)) {
				log.debug "EARLY HEAT ALARM @  $map.value $map.unit (raw $intVal)"
				sendEvent(name: "temperatureAlarm", value: "heat")
			} else if (device.currentValue("temperatureAlarm") != "cleared") {
				log.debug "CLEAR ALARM @ $map.value $map.unit (raw $intVal)"
				sendEvent(name: "temperatureAlarm", value: "cleared")
			}
		}
	} else if (cmd.sensorType == sensormultilevelv3.SensorMultilevelReport.SENSOR_TYPE_RELATIVE_HUMIDITY_VERSION_2) {
		map.value = cmd.scaledSensorValue
		map.unit = "%"
		map.name = "humidity"
	}

	map
}

def zwaveEvent(thermostatoperatingstatev1.ThermostatOperatingStateReport cmd) {
	def map = [:]
	def operatingState = zwaveOperatingStateToString(cmd.operatingState)

	if (operatingState) {
		map.name = "thermostatOperatingState"
		map.value = operatingState

		if (settings.heatdetails == "No") {
			map.displayed = false
		}
	} else {
		log.trace "${device.displayName} sent invalid operating state $value"
	}

	map
}

def zwaveEvent(thermostatmodev2.ThermostatModeReport cmd) {
	def map = [:]
	def mode = zwaveModeToString(cmd.mode)

	if (mode) {
		map.name = "thermostatMode"
		map.value = mode
		map.data = [supportedThermostatModes: supportedThermostatModes]
	} else {
		log.trace "${device.displayName} sent invalid mode $value"
	}

	map
}

def zwaveEvent(associationv2.AssociationReport cmd) {
	delayBetween([
		zwave.associationV1.associationRemove(groupingIdentifier:1, nodeId:0).format(),
		zwave.associationV1.associationSet(groupingIdentifier:1, nodeId:[zwaveHubNodeId]).format(),
		poll()
	], 2300)
}

def zwaveEvent(thermostatmodev2.ThermostatModeSupportedReport cmd) {
	log.debug "Zwave event received: $cmd"
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
	log.warn "Unexpected zwave command $cmd"
}

def refresh() {
	poll()
}

def configure() {
	unschedule(scheduledUpdateWeather)
	if (settings.zipcode) {
		state.invalidZip = false // Reset and validate the zip-code later
		runEvery1Hour(scheduledUpdateWeather)
	}
	poll()
}

def setHeatingSetpoint(preciseDegrees) {
	float minSetpoint = thermostatSetpointRange[minSetpointIndex]
	float maxSetpoint = thermostatSetpointRange[maxSetpointIndex]

	if (preciseDegrees >= minSetpoint && preciseDegrees <= maxSetpoint) {
		def degrees = new BigDecimal(preciseDegrees).setScale(1, BigDecimal.ROUND_HALF_UP)
		log.trace "setHeatingSetpoint($degrees)"
		def deviceScale = state.scale ?: 1
		def deviceScaleString = deviceScale == 2 ? "C" : "F"
		def locationScale = getTemperatureScale()
		def p = (state.precision == null) ? 1 : state.precision
		def setpointType = thermostatsetpointv2.ThermostatSetpointReport.SETPOINT_TYPE_HEATING_1

		def convertedDegrees = degrees
		if (locationScale == "C" && deviceScaleString == "F") {
			convertedDegrees = celsiusToFahrenheit(degrees)
		} else if (locationScale == "F" && deviceScaleString == "C") {
			convertedDegrees = fahrenheitToCelsius(degrees)
		}

		delayBetween([
			zwave.thermostatSetpointV2.thermostatSetpointSet(setpointType: setpointType, scale: deviceScale, precision: p, scaledValue: convertedDegrees).format(),
			zwave.thermostatSetpointV2.thermostatSetpointGet(setpointType: setpointType).format()
		], 1000)
	} else {
		log.debug "heatingSetpoint $preciseDegrees out of range! (supported: $minSetpoint - $maxSetpoint ${getTemperatureScale()})"
	}
}

// Maintain backward compatibility with self published versions of the "Stelpro Get Remote Temperature" SmartApp
def quickSetOutTemp(outsideTemp) {
	setOutdoorTemperature(outsideTemp)
}

def setOutdoorTemperature(outsideTemp) {
	def degrees = outsideTemp as Double
	def locationScale = getTemperatureScale()
	def p = (state.precision == null) ? 1 : state.precision
	def deviceScale = (locationScale == "C") ? 0 : 1
	def sensorType = sensormultilevelv3.SensorMultilevelReport.SENSOR_TYPE_TEMPERATURE_VERSION_1

	log.debug "setOutdoorTemperature: ${degrees}"
	zwave.sensorMultilevelV3.sensorMultilevelReport(sensorType: sensorType, scale: deviceScale, precision: p, scaledSensorValue: degrees).format()
}

def increaseHeatSetpoint() {
	float currentSetpoint = device.currentValue("heatingSetpoint")

	currentSetpoint = currentSetpoint + setpointStep
	setHeatingSetpoint(currentSetpoint)
}

def decreaseHeatSetpoint() {
	float currentSetpoint = device.currentValue("heatingSetpoint")

	currentSetpoint = currentSetpoint - setpointStep
	setHeatingSetpoint(currentSetpoint)
}

def getModeNumericMap() {[
		"heat": thermostatmodev2.ThermostatModeReport.MODE_HEAT,
		"eco": thermostatmodev2.ThermostatModeReport.MODE_ENERGY_SAVE_HEAT
]}
def zwaveModeToString(mode) {
	if (thermostatmodev2.ThermostatModeReport.MODE_HEAT == mode) {
		return "heat"
	} else if (thermostatmodev2.ThermostatModeReport.MODE_ENERGY_SAVE_HEAT == mode) {
		return "eco"
	}
	return null
}
def zwaveOperatingStateToString(state) {
	if (thermostatoperatingstatev1.ThermostatOperatingStateReport.OPERATING_STATE_IDLE == state) {
		return "idle"
	} else if (thermostatoperatingstatev1.ThermostatOperatingStateReport.OPERATING_STATE_HEATING == state) {
		return "heating"
	}
	return null
}

def setCoolingSetpoint(coolingSetpoint) {
	log.trace "${device.displayName} does not support cool setpoint"
}

def heat() {
	log.trace "heat mode applied"
	setThermostatMode("heat")
}

def eco() {
	log.trace "eco mode applied"
	setThermostatMode("eco")
}

def off() {
	log.trace "${device.displayName} does not support off mode"
}

def auto() {
	log.trace "${device.displayName} does not support auto mode"
}

def emergencyHeat() {
	log.trace "${device.displayName} does not support emergency heat mode"
}

def cool() {
	log.trace "${device.displayName} does not support cool mode"
}

def setThermostatMode(value) {
	if (supportedThermostatModes.contains(value)) {
		delayBetween([
			zwave.thermostatModeV2.thermostatModeSet(mode: modeNumericMap[value]).format(),
			zwave.thermostatModeV2.thermostatModeGet().format()
		], 1000)
	} else {
		log.trace "${device.displayName} does not support $value mode"
	}
}

def fanOn() {
	log.trace "${device.displayName} does not support fan on"
}

def fanAuto() {
	log.trace "${device.displayName} does not support fan auto"
}

def fanCirculate() {
	log.trace "${device.displayName} does not support fan circulate"
}

def setThermostatFanMode() {
	log.trace "${device.displayName} does not support fan mode"
}
