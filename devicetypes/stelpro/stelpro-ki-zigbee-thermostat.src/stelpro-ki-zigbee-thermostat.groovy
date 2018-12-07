/**
 *  Copyright 2017 - 2018 Stelpro
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *	  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 *  Stelpro Ki ZigBee Thermostat
 *
 *  Author: Stelpro
 *
 *  Date: 2018-04-04
 */

import physicalgraph.zigbee.zcl.DataType

metadata {
	definition (name: "Stelpro Ki ZigBee Thermostat", namespace: "stelpro", author: "Stelpro", ocfDeviceType: "oic.d.thermostat") {
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

		attribute "outsideTemp", "number"

		command "setOutdoorTemperature"
		command "quickSetOutTemp" // Maintain backward compatibility with self published versions of the "Stelpro Get Remote Temperature" SmartApp
		command "increaseHeatSetpoint"
		command "decreaseHeatSetpoint"
		command "parameterSetting"
		command "eco" // Command does not exist in "Thermostat Mode"
		command "updateWeather"

		fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0201, 0204", outClusters: "0402", manufacturer: "Stelpro", model: "STZB402+", deviceJoinName: "Stelpro Ki ZigBee Thermostat"
		fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0201, 0204", outClusters: "0402", manufacturer: "Stelpro", model: "ST218", deviceJoinName: "Stelpro Orleans Convector"
	}

	// simulator metadata
	simulator { }

	preferences {
		input("lock", "enum", title: "Do you want to lock your thermostat's physical keypad?", options: ["No", "Yes"], defaultValue: "No", required: false, displayDuringSetup: false)
		input("heatdetails", "enum", title: "Do you want a detailed operating state notification?", options: ["No", "Yes"], defaultValue: "No", required: false, displayDuringSetup: true)
		input("zipcode", "text", title: "ZipCode (Outdoor Temperature)", description: "[Do not use space](Blank = No Forecast)")
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
				attributeState("off", label:'${name}')
				attributeState("heat", label:'${name}')
				attributeState("eco", label:'${name}')
			}
			tileAttribute("device.heatingSetpoint", key: "HEATING_SETPOINT") {
				attributeState("heatingSetpoint", label:'${currentValue}°')
			}
		}
		standardTile("mode", "device.thermostatMode", width: 2, height: 2) {
			state "off", label:'${name}', action:"heat", nextState:"heat", icon:"st.Home.home29"
			state "heat", label:'${name}', action:"eco", nextState:"eco", icon:"st.Outdoor.outdoor3"
			state "eco", label:'${name}', action:"off", nextState:"off", icon:"st.Outdoor.outdoor3"
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
		standardTile("configure", "device.configure", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "configure", label:'', action:"configuration.configure", icon:"st.secondary.configure"
		}

		main ("thermostatMulti")
		details(["thermostatMulti", "mode", "heatingSetpoint", "temperatureAlarm", "refresh", "configure"])
	}
}

def getTHERMOSTAT_CLUSTER() { 0x0201 }
def getATTRIBUTE_LOCAL_TEMP() { 0x0000 }
def getATTRIBUTE_PI_HEATING_STATE() { 0x0008 }
def getATTRIBUTE_HEAT_SETPOINT() { 0x0012 }
def getATTRIBUTE_SYSTEM_MODE() { 0x001C }
def getATTRIBUTE_MFR_SPEC_SETPOINT_MODE() { 0x401C }
def getATTRIBUTE_MFR_SPEC_OUT_TEMP() { 0x4001 }

def getTHERMOSTAT_UI_CONFIG_CLUSTER() { 0x0204 }
def getATTRIBUTE_TEMP_DISP_MODE() { 0x0000 }
def getATTRIBUTE_KEYPAD_LOCKOUT() { 0x0001 }


def getSupportedThermostatModes() {
	["heat", "eco", "off"]
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

def getModeMap() {[
	"00":"off",
	"04":"heat",
	"05":"eco"
]}

def setupHealthCheck() {
	// Device-Watch simply pings if no device events received for 32min(checkInterval)
	sendEvent(name: "checkInterval", value: 2 * 15 * 60 + 2 * 60, displayed: false, data: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID])
}

def configureSupportedRanges() {
	sendEvent(name: "supportedThermostatModes", value: supportedThermostatModes, displayed: false)
	sendEvent(name: "thermostatSetpointRange", value: thermostatSetpointRange, displayed: false)
	sendEvent(name: "heatingSetpointRange", value: heatingSetpointRange, displayed: false)
}

def installed() {
	sendEvent(name: "temperatureAlarm", value: "cleared", displayed: false)

	setupHealthCheck()

	configureSupportedRanges()
}

def updated() {
	def requests = []
	setupHealthCheck()

	configureSupportedRanges()

	unschedule(scheduledUpdateWeather)
	if (settings.zipcode) {
		requests += updateWeather()
		runEvery1Hour(scheduledUpdateWeather)
	}

	requests += parameterSetting()
	response(requests)
}

def parameterSetting() {
	def lockmode = null
	def valid_lock = false

	log.debug "lock : $settings.lock"
	if (settings.lock == "Yes") {
		lockmode = 0x01
		valid_lock = true
	} else if (settings.lock == "No") {
		lockmode = 0x00
		valid_lock = true
	}

	if (valid_lock) {
		log.debug "lock valid"
		zigbee.writeAttribute(THERMOSTAT_UI_CONFIG_CLUSTER, ATTRIBUTE_KEYPAD_LOCKOUT, DataType.ENUM8, lockmode) +
			poll()
	} else {
		log.debug "nothing valid"
	}
}

def parse(String description) {
	log.debug "Parse description $description"
	def map = [:]

	// If the user installed with an old DTH version, update so that the new mobile client will work
	if (!device.currentValue("supportedThermostatModes")) {
		configureSupportedRanges()
	}
	// Existing installations need the temperatureAlarm state initialized
	if (device.currentValue("temperatureAlarm") == null) {
		sendEvent(name: "temperatureAlarm", value: "cleared", displayed: false)
	}

	if (description?.startsWith("read attr -")) {
		def descMap = zigbee.parseDescriptionAsMap(description)
		log.debug "Desc Map: $descMap"
		if (descMap.clusterInt == THERMOSTAT_CLUSTER) {
			if (descMap.attrInt == ATTRIBUTE_LOCAL_TEMP) {
				def intVal = Integer.parseInt(descMap.value, 16)
				map.name = "temperature"
				map.unit = getTemperatureScale()
				map.value = getTemperature(descMap.value)

				if (intVal == 0x7ffd) {		// 0x7FFD
					map.name = "temperatureAlarm"
					map.value = "freeze"
					map.unit = ""
				} else if (intVal == 0x7fff) {	// 0x7FFF
					map.name = "temperatureAlarm"
					map.value = "heat"
					map.unit = ""
				} else if (intVal == 0x8000) {	// 0x8000
					map.name = null
					map.value = null
					map.descriptionText = "Received a temperature error"
				} else if (intVal > 0x8000) {
					map.value = -(Math.round(2*(655.36 - map.value))/2)
				}

				if (device.currentValue("temperatureAlarm") != "cleared" && map.name == "temperature") {
					sendEvent(name: "temperatureAlarm", value: "cleared")
				}
			} else if (descMap.attrInt == ATTRIBUTE_HEAT_SETPOINT) {
				def intVal = Integer.parseInt(descMap.value, 16)
				if (intVal != 0x8000) {		// 0x8000
					log.debug "HEATING SETPOINT"
					map.name = "heatingSetpoint"
					map.value = getTemperature(descMap.value)
					map.data = [heatingSetpointRange: heatingSetpointRange]
				}
			} else if (descMap.attrInt == ATTRIBUTE_SYSTEM_MODE) {
				log.debug "MODE - ${descMap.value}"
				def value = modeMap[descMap.value]

				// If we receive an off here then we are off
				// Else we will determine the real mode in the mfg specific packet so store this
				if (value == "off") {
					map.name = "thermostatMode"
					map.value = value
					map.data = [supportedThermostatModes: supportedThermostatModes]
				} else {
					state.storedSystemMode = value
				}
				// Right now this doesn't seem to happen -- regardless of the size field the value seems to be two bytes
				/*if (descMap.size == "08") {
					log.debug "MODE"
					map.name = "thermostatMode"
					map.value = modeMap[descMap.value]
					map.data = [supportedThermostatModes: supportedThermostatModes]
				} else if (descMap.size == "0A") {
					log.debug "MODE & SETPOINT MODE"
					def twoModesAttributes = descMap.value[0..-9]
					map.name = "thermostatMode"
					map.value = modeMap[twoModesAttributes]
					map.data = [supportedThermostatModes: supportedThermostatModes]
				}*/
			} else if (descMap.attrInt == ATTRIBUTE_MFR_SPEC_SETPOINT_MODE) {
				log.debug "SETPOINT MODE - ${descMap.value}"
				// If the storedSystemMode is heat, then we set the real mode here
				// Otherwise, we just ignore this
				if (!state.storedSystemMode || state.storedSystemMode == "heat") {
					log.debug "USING SETPOINT MODE - ${descMap.value}"
					map.name = "thermostatMode"
					map.value = modeMap[descMap.value]
					map.data = [supportedThermostatModes: supportedThermostatModes]
				}
			} else if (descMap.attrInt == ATTRIBUTE_PI_HEATING_STATE) {
				def intVal = Integer.parseInt(descMap.value, 16)
				log.debug "HEAT DEMAND"
				map.name = "thermostatOperatingState"
				if (intVal < 10) {
					map.value = "idle"
				} else {
					map.value = "heating"
				}

				if (settings.heatdetails == "No") {
					map.displayed = false
				}
			}
		}
	}

	def result = null
	if (map) {
		result = createEvent(map)
	}
	log.debug "Parse returned $map"
	return result
}

def updateWeather() {
	log.debug "updating weather"
	def weather
	// If there is a zipcode defined, weather forecast will be sent. Otherwise, no weather forecast.
	if (settings.zipcode) {
		log.debug "ZipCode: ${settings.zipcode}"
		weather = getWeatherFeature("conditions", settings.zipcode)

		// Check if the variable is populated, otherwise return.
		if (!weather) {
			log.debug("Something went wrong, no data found.")
			return false
		}
		
		def locationScale = getTemperatureScale()
		def tempToSend = (locationScale == "C") ? weather.current_observation.temp_c : weather.current_observation.temp_f
		log.debug("Outdoor Temperature: ${tempToSend} ${locationScale}")
		// Right now this can disrupt device health if the device is
		// currently offline -- it would be erroneously marked online.
		//sendEvent( name: 'outsideTemp', value: tempToSend )
		setOutdoorTemperature(tempToSend)
	}
}

def scheduledUpdateWeather() {
	def actions = updateWeather()

	if (actions) {
		sendHubCommand(actions)
	}
}

/**
 * PING is used by Device-Watch in attempt to reach the Device
 **/
def ping() {
	log.debug "ping()"
	zigbee.readAttribute(THERMOSTAT_CLUSTER, ATTRIBUTE_LOCAL_TEMP)
}

def poll() {
	def requests = []
	log.debug "poll()"

	requests += updateWeather()
	requests += zigbee.readAttribute(THERMOSTAT_CLUSTER, ATTRIBUTE_LOCAL_TEMP)
	requests += zigbee.readAttribute(THERMOSTAT_CLUSTER, ATTRIBUTE_PI_HEATING_STATE)
	requests += zigbee.readAttribute(THERMOSTAT_CLUSTER, ATTRIBUTE_HEAT_SETPOINT)
	requests += zigbee.readAttribute(THERMOSTAT_CLUSTER, ATTRIBUTE_SYSTEM_MODE)
	requests += zigbee.readAttribute(THERMOSTAT_CLUSTER, ATTRIBUTE_MFR_SPEC_SETPOINT_MODE, ["mfgCode": "0x1185"])
	requests += zigbee.readAttribute(THERMOSTAT_UI_CONFIG_CLUSTER, ATTRIBUTE_TEMP_DISP_MODE)
	requests += zigbee.readAttribute(THERMOSTAT_UI_CONFIG_CLUSTER, ATTRIBUTE_KEYPAD_LOCKOUT)

	requests
}

def getTemperature(value) {
	if (value != null) {
		log.debug("value $value")
		def celsius = Integer.parseInt(value, 16) / 100
		if (getTemperatureScale() == "C") {
			return celsius
		} else {
			return Math.round(celsiusToFahrenheit(celsius))
		}
	}
}

def refresh() {
	poll()
}

def setHeatingSetpoint(preciseDegrees) {
	if (preciseDegrees != null) {
		def temperatureScale = getTemperatureScale()
		float minSetpoint = thermostatSetpointRange[minSetpointIndex]
		float maxSetpoint = thermostatSetpointRange[maxSetpointIndex]

		if (preciseDegrees >= minSetpoint && preciseDegrees <= maxSetpoint) {
			def degrees = new BigDecimal(preciseDegrees).setScale(1, BigDecimal.ROUND_HALF_UP)
			def celsius = (getTemperatureScale() == "C") ? degrees : (fahrenheitToCelsius(degrees) as Float).round(2)

			log.debug "setHeatingSetpoint({$degrees} ${temperatureScale})"

			zigbee.writeAttribute(THERMOSTAT_CLUSTER, ATTRIBUTE_HEAT_SETPOINT, DataType.INT16, zigbee.convertToHexString(celsius * 100, 4)) +
				zigbee.readAttribute(THERMOSTAT_CLUSTER, ATTRIBUTE_HEAT_SETPOINT) +
				zigbee.readAttribute(THERMOSTAT_CLUSTER, ATTRIBUTE_PI_HEATING_STATE)
		} else {
			log.debug "heatingSetpoint $preciseDegrees out of range! (supported: $minSetpoint - $maxSetpoint ${getTemperatureScale()})"
		}
	}
}

// Maintain backward compatibility with self published versions of the "Stelpro Get Remote Temperature" SmartApp
def quickSetOutTemp(outsideTemp) {
	setOutdoorTemperature(outsideTemp)
}

def setOutdoorTemperature(outsideTemp) {
	def degrees = outsideTemp as Double
	Integer tempToSend
	def celsius = (getTemperatureScale() == "C") ? degrees : (fahrenheitToCelsius(degrees) as Float).round(2)

	if (celsius < 0) {
		tempToSend = (celsius*100) + 65536
	} else {
		tempToSend = (celsius*100)
	}
	// The thermostat expects the byte order to be a little different than we send usually
	zigbee.writeAttribute(THERMOSTAT_CLUSTER, ATTRIBUTE_MFR_SPEC_OUT_TEMP, DataType.INT16, zigbee.swapEndianHex(zigbee.convertToHexString(tempToSend, 4)), ["mfgCode": "0x1185"])
}

def increaseHeatSetpoint() {
	def currentMode = device.currentState("thermostatMode")?.value
	if (currentMode != "off") {
		float currentSetpoint = device.currentValue("heatingSetpoint")

		currentSetpoint = currentSetpoint + setpointStep
		setHeatingSetpoint(currentSetpoint)
	}
}

def decreaseHeatSetpoint() {
	def currentMode = device.currentState("thermostatMode")?.value
	if (currentMode != "off") {
		float currentSetpoint = device.currentValue("heatingSetpoint")

		currentSetpoint = currentSetpoint - setpointStep
		setHeatingSetpoint(currentSetpoint)
	}
}

def setThermostatMode(value) {
	log.debug "setThermostatMode({$value})"
	if (supportedThermostatModes.contains(value)) {
		def currentMode = device.currentState("thermostatMode")?.value
		def modeNumber;
		Integer setpointModeNumber;
		if (value == "heat") {
			modeNumber = 04
			setpointModeNumber = 04
		} else if (value == "eco") {
			modeNumber = 04
			setpointModeNumber = 05
		} else {
			modeNumber = 00
			setpointModeNumber = 00
		}

		zigbee.writeAttribute(THERMOSTAT_CLUSTER, ATTRIBUTE_SYSTEM_MODE, DataType.ENUM8, modeNumber) +
			zigbee.writeAttribute(THERMOSTAT_CLUSTER, ATTRIBUTE_MFR_SPEC_SETPOINT_MODE, DataType.ENUM8, setpointModeNumber, ["mfgCode": "0x1185"]) +
			poll()
	} else {
		log.debug "Invalid thermostat mode $value"
	}
}

def off() {
	log.debug "off"
	setThermostatMode("off")
}

def heat() {
	log.debug "heat"
	setThermostatMode("heat")
}

def eco() {
	log.debug "eco"
	setThermostatMode("eco")
}

def configure() {
	def requests = []
	log.debug "binding to Thermostat cluster"

	unschedule(scheduledUpdateWeather)
	if (settings.zipcode) {
		requests += updateWeather()
		runEvery1Hour(scheduledUpdateWeather)
	}

	requests += zigbee.addBinding(THERMOSTAT_CLUSTER)
	// Configure Thermostat Cluster
	requests += zigbee.configureReporting(THERMOSTAT_CLUSTER, ATTRIBUTE_LOCAL_TEMP, DataType.INT16, 10, 60, 50)
	requests += zigbee.configureReporting(THERMOSTAT_CLUSTER, ATTRIBUTE_HEAT_SETPOINT, DataType.INT16, 1, 0, 50)
	requests += zigbee.configureReporting(THERMOSTAT_CLUSTER, ATTRIBUTE_SYSTEM_MODE, DataType.ENUM8, 1, 0, 1)
	requests += zigbee.configureReporting(THERMOSTAT_CLUSTER, ATTRIBUTE_MFR_SPEC_SETPOINT_MODE, DataType.ENUM8, 1, 0, 1)
	requests += zigbee.configureReporting(THERMOSTAT_CLUSTER, ATTRIBUTE_PI_HEATING_STATE, DataType.UINT8, 300, 900, 5)

	// Configure Thermostat Ui Conf Cluster
	requests += zigbee.configureReporting(THERMOSTAT_UI_CONFIG_CLUSTER, ATTRIBUTE_TEMP_DISP_MODE, DataType.ENUM8, 1, 0, 1)
	requests += zigbee.configureReporting(THERMOSTAT_UI_CONFIG_CLUSTER, ATTRIBUTE_KEYPAD_LOCKOUT, DataType.ENUM8, 1, 0, 1)

	// Read the configured variables
	requests += zigbee.readAttribute(THERMOSTAT_CLUSTER, ATTRIBUTE_LOCAL_TEMP)
	requests += zigbee.readAttribute(THERMOSTAT_CLUSTER, ATTRIBUTE_HEAT_SETPOINT)
	requests += zigbee.readAttribute(THERMOSTAT_CLUSTER, ATTRIBUTE_SYSTEM_MODE)
	requests += zigbee.readAttribute(THERMOSTAT_CLUSTER, ATTRIBUTE_MFR_SPEC_SETPOINT_MODE, ["mfgCode": "0x1185"])
	requests += zigbee.readAttribute(THERMOSTAT_CLUSTER, ATTRIBUTE_PI_HEATING_STATE)
	requests += zigbee.readAttribute(THERMOSTAT_UI_CONFIG_CLUSTER, ATTRIBUTE_TEMP_DISP_MODE)
	requests += zigbee.readAttribute(THERMOSTAT_UI_CONFIG_CLUSTER, ATTRIBUTE_KEYPAD_LOCKOUT)

	requests
}

// Unused Thermostat Capability commands
def emergencyHeat() {
	log.debug "${device.displayName} does not support emergency heat mode"
}

def cool() {
	log.debug "${device.displayName} does not support cool mode"
}

def setCoolingSetpoint(degrees) {
	log.debug "${device.displayName} does not support cool setpoint"
}

def on() {
	heat()
}

def setThermostatFanMode(value) {
	log.debug "${device.displayName} does not support $value"
}

def fanOn() {
	log.debug "${device.displayName} does not support fan on"
}

def auto() {
	fanAuto()
}

def fanAuto() {
	log.debug "${device.displayName} does not support fan auto"
}


