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

		fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0201, 0204", outClusters: "0402", manufacturer: "Stelpro", model: "STZB402+", deviceJoinName: "Stelpro Thermostat" //Stelpro Ki ZigBee Thermostat
		fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0201, 0204", outClusters: "0402", manufacturer: "Stelpro", model: "ST218", deviceJoinName: "Stelpro Thermostat" //Stelpro ORLÉANS Convector
	}

	// simulator metadata
	simulator { }

	preferences {
		section {
			input("lock", "enum", title: "Do you want to lock your thermostat's physical keypad?", options: ["No", "Yes"], defaultValue: "No", required: false, displayDuringSetup: false)
			input("heatdetails", "enum", title: "Do you want to see detailed operating state events in the activity history? There may be many.", options: ["No", "Yes"], defaultValue: "No", required: false, displayDuringSetup: true)
		}
		section {
			input(title: "Outdoor Temperature", description: "To get the current outdoor temperature to display on your thermostat enter your zip code or postal code below and make sure that your SmartThings location has a Geolocation configured (typically used for geofencing). Do not use space. If you don't want a forecast, leave it blank.",
					displayDuringSetup: false, type: "paragraph", element: "paragraph")
			input("zipcode", "text", title: "ZipCode (Outdoor Temperature)", description: "")
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
	def requests = []
	setupHealthCheck()

	configureSupportedRanges()

	unschedule(scheduledUpdateWeather)
	if (settings.zipcode) {
		state.invalidZip = false // Reset and validate the zip-code later
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
				map = handleTemperature(descMap)
			} else if (descMap.attrInt == ATTRIBUTE_HEAT_SETPOINT) {
				def intVal = Integer.parseInt(descMap.value, 16)
				// We receive 0x8000 when the thermostat is off
				if (intVal != 0x8000) {
					state.rawSetpoint = intVal
					log.debug "HEATING SETPOINT"
					map.name = "heatingSetpoint"
					map.value = getTemperature(descMap.value)
					map.unit = getTemperatureScale()
					map.data = [heatingSetpointRange: heatingSetpointRange]

					handleOperatingStateBugfix()
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
					// Sometimes we don't get the final decision, so ask for it just in case
					sendHubCommand(zigbee.readAttribute(THERMOSTAT_CLUSTER, ATTRIBUTE_MFR_SPEC_SETPOINT_MODE, ["mfgCode": "0x1185"]))
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

				// If the user does not want to see the Idle and Heating events in the event history,
				// don't show them. Otherwise, don't show them more frequently than 5 minutes.
				if (settings.heatdetails == "No" ||
						!secondsPast(device.currentState("thermostatOperatingState")?.getLastUpdated(), 60 * 5)) {
					map.displayed = false
				}
				map = validateOperatingStateBugfix(map)
				// Check to see if this was changed, if so make sure we have the correct heating setpoint
				if (map.data?.correctedValue) {
					sendHubCommand(zigbee.readAttribute(THERMOSTAT_CLUSTER, ATTRIBUTE_HEAT_SETPOINT))
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

def handleTemperature(descMap) {
	def map = [:]
	def intVal = Integer.parseInt(descMap.value, 16)

	// Handle special temperature flags where we need to change the event type
	if (intVal == 0x7ffd) { // Freeze Alarm
		map.name = "temperatureAlarm"
		map.value = "freeze"
	} else if (intVal == 0x7fff) { // Overheat Alarm
		map.name = "temperatureAlarm"
		map.value = "heat"
	} else if (intVal == 0x8000) { // Temperature Sensor Error
		map.descriptionText = "Received a temperature error"
	} else {
		if (intVal > 0x8000) { // Handle negative C (< 32F) readings
			intVal = -(Math.round(2 * (65536 - intVal)) / 2)
		}
		state.rawTemp = intVal
		map.name = "temperature"
		map.value = getTemperature(intVal)
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

		handleOperatingStateBugfix()
	}

	map
}

// Due to a bug in this model's firmware, sometimes we don't get
// an updated operating state; so we need some special logic to verify the accuracy.
// TODO: Add firmware version check when change versions are known
// The logic between these two functions works as follows:
//   In temperature and heatingSetpoint events check to see if we might need to request
//   the current operating state and request it with handleOperatingStateBugfix.
//
//   In operatingState events validate the data we received from the thermostat with
//   the current environment, adjust as needed. If we had to make an adjustment, then ask
//   for the setpoint again just to make sure we didn't miss data somewhere.
//
// There is a risk of false positives where we receive a new valid operating state before the
// new setpoint, so we basically toss it. When we come to receiving the setpoint or temperature
// (temperature roughly every minute) then we should catch the problem and request an update.
// I think this is a little easier than outright managing the operating state ourselves.
// All comparisons are made using the raw integer from the thermostat (unrounded Celsius decimal * 100)
// that is stored in temperature and setpoint events.

/**
 * Check if we should request the operating state, and request it if so
 */
def handleOperatingStateBugfix() {
	def currOpState = device.currentValue("thermostatOperatingState")

	if (state.rawSetpoint != null && state.rawTemp != null) {
		if (state.rawSetpoint <= state.rawTemp) {
			if (currOpState != "idle")
				sendHubCommand(zigbee.readAttribute(THERMOSTAT_CLUSTER, ATTRIBUTE_PI_HEATING_STATE))
		} else {
			if (currOpState != "heating")
				sendHubCommand(zigbee.readAttribute(THERMOSTAT_CLUSTER, ATTRIBUTE_PI_HEATING_STATE))
		}
	}
}
/**
 * Given an operating state event, check its validity against the current environment
 * @param map An operating state to validate
 * @return The passed map if valid, or a corrected map and a new param data.correctedValue if invalid
 */
def validateOperatingStateBugfix(map) {
	// If we don't have historical data, we will take the value we get,
	// otherwise validate if the difference is > 1
	if (state.rawSetpoint != null && state.rawTemp != null) {
		def oldVal = map.value

		if (state.rawSetpoint <= state.rawTemp || device.currentValue("thermostatMode") == "off") {
			map.value = "idle"
		} else {
			map.value = "heating"
		}

		// Indicate that we have made a change
		if (map.value != oldVal) {
			map.data = [correctedValue: true]
		}
	}

	map
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

/**
 * Given a raw temperature reading in Celsius return a converted temperature.
 *
 * @param value The temperature in Celsius, treated based on the following:
 *                 If value instanceof String, treat as a raw hex string and divide by 100
 *                 Otherwise treat value as a number and divide by 100
 *
 * @return A Celsius or Farenheit value
 */
def getTemperature(value) {
	if (value != null) {
		log.debug("value $value")
		def celsius = (value instanceof String ? Integer.parseInt(value, 16) : value) / 100
		if (getTemperatureScale() == "C") {
			return celsius
		} else {
			def rounded = new BigDecimal(celsiusToFahrenheit(celsius)).setScale(0, BigDecimal.ROUND_HALF_UP)
			return rounded
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
		state.invalidZip = false // Reset and validate the zip-code later
		requests += updateWeather()
		runEvery1Hour(scheduledUpdateWeather)
	}

	requests += zigbee.addBinding(THERMOSTAT_CLUSTER)
	// Configure Thermostat Cluster
	requests += zigbee.configureReporting(THERMOSTAT_CLUSTER, ATTRIBUTE_LOCAL_TEMP, DataType.INT16, 10, 60, 50)
	requests += zigbee.configureReporting(THERMOSTAT_CLUSTER, ATTRIBUTE_HEAT_SETPOINT, DataType.INT16, 1, 600, 50)
	requests += zigbee.configureReporting(THERMOSTAT_CLUSTER, ATTRIBUTE_SYSTEM_MODE, DataType.ENUM8, 1, 0, 1)
	requests += zigbee.configureReporting(THERMOSTAT_CLUSTER, ATTRIBUTE_MFR_SPEC_SETPOINT_MODE, DataType.ENUM8, 1, 0, 1)
	requests += zigbee.configureReporting(THERMOSTAT_CLUSTER, ATTRIBUTE_PI_HEATING_STATE, DataType.UINT8, 1, 600, 1)

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

/**
 * Checks if the time elapsed from the provided timestamp is greater than the number of senconds provided
 *
 * @param timestamp: The timestamp
 *
 * @param seconds: The number of seconds
 *
 * @returns true if elapsed time is greater than number of seconds provided, else false
 */
private Boolean secondsPast(timestamp, seconds) {
	if (!(timestamp instanceof Number)) {
		if (timestamp instanceof Date) {
			timestamp = timestamp.time
		} else if ((timestamp instanceof String) && timestamp.isNumber()) {
			timestamp = timestamp.toLong()
		} else {
			return true
		}
	}
	return (now() - timestamp) > (seconds * 1000)
}
