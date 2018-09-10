/**
 *  Copyright 2018 Stelpro
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
 *  Stelpro Maestro Thermostat
 *
 *  Author: Stelpro
 *
 *  Date: 2018-04-05
 */

metadata {
	definition (name: "Stelpro Maestro Thermostat", namespace: "Stelpro", author: "Stelpro") {
		capability "Actuator"
		capability "Temperature Measurement"
		capability "Temperature Alarm"
		capability "Relative Humidity Measurement"
		capability "Thermostat"
		capability "Thermostat Mode"
		capability "Thermostat Operating State"
		capability "Thermostat Heating Setpoint"
		capability "Configuration"
		capability "Polling"
		capability "Sensor"
		capability "Refresh"
		capability "Health Check"
		
		attribute "outsideTemp", "number"
		attribute "humidity", "number"
		//attribute "setpoint", "number"
		
		command "switchMode"
		command "quickSetHeat"
		command "quickSetOutTemp"
		command "increaseHeatSetpoint"
		command "decreaseHeatSetpoint"
		command "parameterSetting"
		command "setCustomThermostatMode"
		command "updateWeather"

		fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0201, 0204, 0405", outClusters: "0003, 000A, 0402", manufacturer: "Stelpro", model: "MaestroStat", deviceJoinName: "Stelpro Maestro Thermostat"
		fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0201, 0204, 0405", outClusters: "0003, 000A, 0402", manufacturer: "Stelpro", model: "SORB", deviceJoinName: "Stelpro Maestro Thermostat"
		fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0201, 0204, 0405", outClusters: "0003, 000A, 0402", manufacturer: "Stelpro", model: "SonomaStyle", deviceJoinName: "Stelpro Maestro Thermostat"
	}
	
	// simulator metadata
	simulator { }

	preferences {
		input("lock", "enum", title: "Do you want to lock your thermostat's physical keypad?", options: ["No", "Yes"], defaultValue: "No", required: false, displayDuringSetup: false)
		input("heatdetails", "enum", title: "Do you want a detailed operating state notification?", options: ["No", "Yes"], defaultValue: "No", required: false, displayDuringSetup: true)
		input("zipcode", "text", title: "ZipCode (Outdoor Temperature)", description: "[Do not use space](Blank = No Forecast)")
		/*
		input("away_setpoint", "enum", title: "Away setpoint", options: ["5", "5.5", "6", "6.5", "7", "7.5", "8", "8.5", "9", "9.5", "10", "10.5", "11", "11.5", "12", "12.5", "13", "13.5", "14", "14.5", "15", "5.5", "15.5", "16", "16.5", "17", "17.5", "18", "18.5", "19", "19.5", "20", "20.5", "21", "21.5", "22", "22.5", "23", "24", "24.5", "25", "25.5", "26", "26.5", "27", "27.5", "28", "28.5", "29", "29.5", "30"], defaultValue: "21", required: true)
		input("away_setpoint", "enum", title: "Away Setpoint", options: ["5", "5.5", "6", "6.5", "7", "7.5", "8", "8.5", "9", "9.5", "10", "10.5", "11", "11.5", "12", "12.5", "13", "13.5", "14", "14.5", "15", "5.5", "15.5", "16", "16.5", "17", "17.5", "18", "18.5", "19", "19.5", "20", "20.5", "21", "21.5", "22", "22.5", "23", "24", "24.5", "25", "25.5", "26", "26.5", "27", "27.5", "28", "28.5", "29", "29.5", "30"], defaultValue: "17", required: true)
		input("vacation_setpoint", "enum", title: "Vacation Setpoint", options: ["5", "5.5", "6", "6.5", "7", "7.5", "8", "8.5", "9", "9.5", "10", "10.5", "11", "11.5", "12", "12.5", "13", "13.5", "14", "14.5", "15", "5.5", "15.5", "16", "16.5", "17", "17.5", "18", "18.5", "19", "19.5", "20", "20.5", "21", "21.5", "22", "22.5", "23", "24", "24.5", "25", "25.5", "26", "26.5", "27", "27.5", "28", "28.5", "29", "29.5", "30"], defaultValue: "13", required: true)
		input("standby_setpoint", "enum", title: "Standby Setpoint", options: ["5", "5.5", "6", "6.5", "7", "7.5", "8", "8.5", "9", "9.5", "10", "10.5", "11", "11.5", "12", "12.5", "13", "13.5", "14", "14.5", "15", "5.5", "15.5", "16", "16.5", "17", "17.5", "18", "18.5", "19", "19.5", "20", "20.5", "21", "21.5", "22", "22.5", "23", "24", "24.5", "25", "25.5", "26", "26.5", "27", "27.5", "28", "28.5", "29", "29.5", "30"], defaultValue: "5", required: true)
		*/			
	}

	tiles(scale : 2) {
		multiAttributeTile(name:"thermostatMulti", type:"thermostat", width:6, height:4, canChangeIcon: true) {
			tileAttribute("device.temperature", key: "PRIMARY_CONTROL") {
				attributeState("temperature", label:'${currentValue}°')
			}
			tileAttribute("device.heatingSetpoint", key: "VALUE_CONTROL") {
				attributeState("VALUE_UP", action: "increaseHeatSetpoint")
				attributeState("VALUE_DOWN", action: "decreaseHeatSetpoint")
			}
			tileAttribute("device.thermostatOperatingState", key: "OPERATING_STATE") {
				attributeState("idle", backgroundColor:"#44b621")
				attributeState("heating", backgroundColor:"#ffa81e")
			}/*
			tileAttribute("device.thermostatMode", key: "THERMOSTAT_MODE") {
				attributeState("home", label:'${name}')
				attributeState("away", label:'${name}')
				attributeState("vacation", label:'${name}')
				attributeState("standby", label:'${name}')
			}*/
			tileAttribute("device.heatingSetpoint", key: "HEATING_SETPOINT") {
				attributeState("heatingSetpoint", label:'${currentValue}°', defaultState: true)
			}
			tileAttribute("device.humidity", key: "SECONDARY_CONTROL") {
				attributeState("humidity", label:'${currentValue}%', unit:"%", defaultState: true)
			}
		}
		
		/*
		standardTile("mode", "device.thermostatMode", width: 2, height: 2) {
			state "home", label:'${name}', action:"switchMode", nextState:"away", icon:"http://cdn.device-icons.smartthings.com/Home/home2-icn@2x.png"
			state "away", label:'${name}', action:"switchMode", nextState:"vacation", icon:"http://cdn.device-icons.smartthings.com/Home/home15-icn@2x.png"
			state "vacation", label:'${name}', action:"switchMode", nextState:"standby", icon:"http://cdn.device-icons.smartthings.com/Transportation/transportation2-icn@2x.png"
			state "standby", label:'${name}', action:"switchMode", nextState:"home"
		}*/
		
		valueTile("humidity", "device.humidity", width: 2, height: 2) {
			state "humidity", label:'Humidity ${currentValue}%', backgroundColor:"#4286f4", defaultState: true
		}
		
		valueTile("heatingSetpoint", "device.heatingSetpoint", width: 2, height: 2) {
			state "heatingSetpoint", label:'Setpoint ${currentValue}', unit:"F", backgroundColors:[
					[value: 67, color: "#45ea1c"],
					[value: 68, color: "#94fc1e"],
					[value: 69, color: "#cbed25"],
					[value: 70, color: "#edd044"],
					[value: 71, color: "#edaf44"],
					[value: 75, color: "#bc2323"]
			]
		}
		standardTile("refresh", "device.refresh", decoration: "flat", width: 2, height: 2) {
			state "default", action:"refresh.refresh", icon:"st.secondary.refresh"
		}
		standardTile("configure", "device.configure", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "configure", label:'', action:"configuration.configure", icon:"st.secondary.configure"
		}

		main ("thermostatMulti")
		details(["thermostatMulti", "humidity", "heatingSetpoint", "refresh", "configure"])
	}
}

def getSupportedThermostatModes() {
	modes()
}

def getThermostatSetpointRange() {
	if (getTemperatureScale() == "C") {
		[5, 30]
	}
	else {
		[41, 86]
	}
}

def getHeatingSetpointRange() {
	thermostatSetpointRange
}

def setupHealthCheck() {
	// Device-Watch simply pings if no device events received for 32min(checkInterval)
	sendEvent(name: "checkInterval", value: 2 * 15 * 60 + 2 * 60, displayed: false, data: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID])
}

def installed() {
	setupHealthCheck()

	sendEvent(name: "supportedThermostatModes", value: supportedThermostatModes, displayed: false)
	sendEvent(name: "thermostatSetpointRange", value: thermostatSetpointRange, displayed: false)
	sendEvent(name: "heatingSetpointRange", value: heatingSetpointRange, displayed: false)
}

def updated() {
	def requests = []

	setupHealthCheck()

	sendEvent(name: "supportedThermostatModes", value: supportedThermostatModes, displayed: false)
	sendEvent(name: "thermostatSetpointRange", value: thermostatSetpointRange, displayed: false)
	sendEvent(name: "heatingSetpointRange", value: heatingSetpointRange, displayed: false)

	if (settings.zipcode) {
		requests += scheduledUpdateWeather()
		unschedule(scheduledUpdateWeather)
		runEvery1Hour(scheduledUpdateWeather)
	} else {
		unschedule(scheduledUpdateWeather)
	}

	requests += parameterSetting()
	response(requests)
}

def parameterSetting() {
	def lockmode = null
	def valid_lock = 0

	log.info "lock : $settings.lock"
	if (settings.lock == "Yes") {
		lockmode = 0x01
		valid_lock = 1
	}
	else if (settings.lock == "No") {
		lockmode = 0x00
		valid_lock = 1
	}

	if (valid_lock == 1)
	{
		log.info "lock valid"
		delayBetween([
			zigbee.writeAttribute(0x204, 0x01, 0x30, lockmode),	//Write Lock Mode
			poll(),
		], 200)
	}
	else {
		log.info "nothing valid"
	}
}

def parse(String description) {
	log.debug "Parse description $description"
	def map = [:]
	if (description?.startsWith("read attr -")) {
		def descMap = zigbee.parseDescriptionAsMap(description)
		log.debug "Desc Map: $descMap"
		if (descMap.cluster == "0201" && descMap.attrId == "0000")
		{
			map.name = "temperature"
			map.value = getTemperature(descMap.value)
			if (descMap.value == "7ffd") {		//0x7FFD
				map.name = "temperatureAlarm"
				map.value = "freeze"
				map.unit = ""
			}
			else if (descMap.value == "7fff") {	//0x7FFF
				map.name = "temperatureAlarm"
				map.value = "heat"
				map.unit = ""
			}
			else if (descMap.value == "8000") {	//0x8000
				map.name = "temperatureAlarm"
				map.value = "cleared"
				map.unit = ""
			}
			
			else if (descMap.value > "8000") {
				map.value = -(Math.round(2*(655.36 - map.value))/2)
			}
		}
		else if (descMap.cluster == "0201" && descMap.attrId == "0012") {
			log.debug "HEATING SETPOINT"
			map.name = "heatingSetpoint"
			map.value = getTemperature(descMap.value)
			if (descMap.value == "8000") {		//0x8000
				map.name = "temperatureAlarm"
				map.value = "cleared"
				map.unit = ""
				map.data = []
			}
		}
	   /* else if (descMap.cluster == "0201" && descMap.attrId == "001c") {
			if (descMap.value.size() == 8) {
				log.debug "MODE"
				map.name = "thermostatMode"
				map.value = getModeMap()[descMap.value]
			}
			else if (descMap.value.size() == 10) {
				log.debug "MODE & SETPOINT MODE"
				def twoModesAttributes = descMap.value[0..-9]
				map.name = "thermostatMode"
				map.value = getModeMap()[twoModesAttributes]
			}
		}
		else if (descMap.cluster == "0201" && descMap.attrId == "401c") {
			log.debug "SETPOINT MODE"
			log.debug "descMap.value $descMap.value"
			map.name = "thermostatMode"
			map.value = getModeMap()[descMap.value]
		}*/
		else if (descMap.cluster == "0201" && descMap.attrId == "0008") {
			log.debug "HEAT DEMAND"
			map.name = "thermostatOperatingState"
			if (descMap.value < "10") {
				map.value = "idle"
			}
			else {
				map.value = "heating"
			}
			if (settings.heatdetails == "No") {
				map.displayed = false
			}
		}
	}
	else if(description?.startsWith("humidity")) {
		log.debug "DEVICE HUMIDITY"
		map.name = "humidity"
		map.value = (description - "humidity: " - "%").trim()
	}

	def result = null
	if (map) {
		result = createEvent(map)
	}
	log.debug "Parse returned $map"
	return result
}

def updateWeather() {
	log.info "updating weather"
	def weather
	// If there is a zipcode defined, weather forecast will be sent. Otherwise, no weather forecast.
	if (settings.zipcode) {
		log.debug "ZipCode: ${settings.zipcode}"
		weather = getWeatherFeature( "conditions", settings.zipcode )

		// Check if the variable is populated, otherwise return.
		if (!weather) {
			log.debug( "Something went wrong, no data found." )
			return false
		}
		
		def tempToSend
		
		if(getTemperatureScale() == "C" ) {
			tempToSend = weather.current_observation.temp_c
			log.debug( "Outdoor Temperature: ${tempToSend} C" )
		}
		else {
			tempToSend = weather.current_observation.temp_f
			log.debug( "Outdoor Temperature: ${tempToSend} F" )
		}	   
		sendEvent( name: 'outsideTemp', value: tempToSend )
		quickSetOutTemp(tempToSend)
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
	zigbee.readAttribute(0x201, 0x0000)
}

def poll() {
	sendEvent( name: 'change', value: 0 )
	delayBetween([
			updateWeather(),
			zigbee.readAttribute(0x201, 0x0000),	//Read Local Temperature
			zigbee.readAttribute(0x201, 0x0008),	//Read PI Heating State
			zigbee.readAttribute(0x201, 0x0012),	//Read Heat Setpoint
			zigbee.readAttribute(0x204, 0x0000),	//Read Temperature Display Mode
			zigbee.readAttribute(0x204, 0x0001),	//Read Keypad Lockout
			zigbee.readAttribute(0x405, 0x0000),	//Read Local Humidity
		], 200)
}



def getTemperature(value) {
	if (value != null) {
		log.debug("value $value")
		def celsius = Integer.parseInt(value, 16) / 100
		if (getTemperatureScale() == "C") {
			return celsius
		}
		else {
			return Math.round(celsiusToFahrenheit(celsius))
		}
	}
}

def refresh() {
	poll()
}

def quickSetHeat(degrees) {
	sendEvent( name: 'change', value: 1 )
	setHeatingSetpoint(degrees, 0)
}

def setHeatingSetpoint(preciseDegrees, delay = 0) {
	if (preciseDegrees != null) {
		def temperatureScale = getTemperatureScale()
		def degrees = new BigDecimal(preciseDegrees).setScale(1, BigDecimal.ROUND_HALF_UP)

		log.debug "setHeatingSetpoint({$degrees} ${temperatureScale})"
		
		sendEvent(name: "heatingSetpoint", value: degrees, unit: temperatureScale)
		sendEvent(name: "thermostatSetpoint", value: degrees, unit: temperatureScale)
		
		def celsius = (getTemperatureScale() == "C") ? degrees : (fahrenheitToCelsius(degrees) as Float).round(2)
		delayBetween([
			zigbee.writeAttribute(0x201, 0x12, 0x29, hex(celsius * 100)),
			zigbee.readAttribute(0x201, 0x12),	//Read Heat Setpoint
			zigbee.readAttribute(0x201, 0x08),	//Read PI Heat demand
		], 100)
	}
}

def setCoolingSetpoint(degrees) {
	if (degrees != null) {
		def degreesInteger = Math.round(degrees)
		log.debug "setCoolingSetpoint({$degreesInteger} ${temperatureScale})"
		sendEvent("name": "coolingSetpoint", "value": degreesInteger)
		def celsius = (getTemperatureScale() == "C") ? degreesInteger : (fahrenheitToCelsius(degreesInteger) as Double).round(2)
		"st wattr 0x${device.deviceNetworkId} 0x19 0x201 0x11 0x29 {" + hex(celsius * 100) + "}"
	}
}

def quickSetOutTemp(degrees) {
	setOutdoorTemperature(degrees, 0)
}

def setOutdoorTemperature(degrees, delay = 0) {
	setOutdoorTemperature(degrees.toDouble(), delay)
}

def setOutdoorTemperature(Double degrees, Integer delay = 0) {
	def p = (state.precision == null) ? 1 : state.precision
	Integer tempToSend
	def tempToSendInString
	
	def celsius = (getTemperatureScale() == "C") ? degrees : (fahrenheitToCelsius(degrees) as Float).round(2)

	if (celsius < 0) {
		tempToSend = (celsius*100) + 65536
	}
	else {
		tempToSend = (celsius*100)
	}
	tempToSendInString = zigbee.convertToHexString(tempToSend, 4)
	zigbee.writeAttribute(0x201, 0x4001, 0x29, tempToSendInString, ["mfgCode": "0x1185"])
}

def increaseHeatSetpoint() {
	float currentSetpoint = device.currentValue("heatingSetpoint")
	def locationScale = getTemperatureScale()
	float maxSetpoint
	float step

	if (locationScale == "C") {
		maxSetpoint = 30;
		step = 0.5
	}
	else {
		maxSetpoint = 86
		step = 1
	}

	if (currentSetpoint < maxSetpoint) {
		currentSetpoint = currentSetpoint + step
		quickSetHeat(currentSetpoint)
	}
}

def decreaseHeatSetpoint() {
	float currentSetpoint = device.currentValue("heatingSetpoint")
	def locationScale = getTemperatureScale()
	float minSetpoint
	float step

	if (locationScale == "C") {
		minSetpoint = 5;
		step = 0.5
	}
	else {
		minSetpoint = 41
		step = 1
	}

	if (currentSetpoint > minSetpoint) {
		currentSetpoint = currentSetpoint - step
		quickSetHeat(currentSetpoint)
	}
}

def modes() {
	["off", "heat"]
}

def setThermostatMode(value) {
	log.debug "setThermostatMode($value)"

	if (value == "heat") {
		on()
	}
	else if (value == "cool") {
		off()
	}
	else if (value == "on") {
		on()
	}
	else if (value == "off") {
		off()
	}
	else {
		log.debug "MODE $value NOT SUPPORTED"
	}
}

def on() {
	log.debug "on"
	sendEvent("name":"thermostatMode", "value":"heat")
}

def off() {
	log.debug "off"
	sendEvent("name":"thermostatMode", "value":"off")
	//quickSetHeat(5)
}

def cool() {
	log.debug "cool"
	sendEvent("name":"thermostatMode", "value":"off")
}

def heat() {
	log.debug "heat"
	sendEvent("name":"thermostatMode", "value":"heat")
}

def emergencyHeat() {
	log.debug "emergencyHeat"
}

def setCustomThermostatMode(mode) {
   setThermostatMode(mode)
}

/*
def fanOn() {
	log.debug "fanOn"
	sendEvent("name":"thermostatFanMode", "value":"fanOn")
	"st wattr 0x${device.deviceNetworkId} 0x19 0x202 0 0x30 {04}"
}

def auto() {
	fanAuto()
}

def fanAuto() {
	log.debug "fanAuto"
	sendEvent("name":"thermostatFanMode", "value":"fanAuto")
	//"st wattr 0x${device.deviceNetworkId} 1 0x202 0 0x30 {05}"
}
*/

def configure() {
	def requests = []
	log.debug "binding to Thermostat cluster"

	if (settings.zipcode) {
		requests += scheduledUpdateWeather()
		unschedule(scheduledUpdateWeather)
		runEvery1Hour(scheduledUpdateWeather)
	} else {
		unschedule(scheduledUpdateWeather)
	}

	requests += delayBetween([
		sendEvent("name":"thermostatMode", "value":"heat"),
		"zdo bind 0x${device.deviceNetworkId} 1 0x19 0x201 {${device.zigbeeId}} {}",
		//Cluster ID (0x0201 = Thermostat Cluster), Attribute ID, Data Type, Payload (Min report, Max report, On change trigger)
		zigbee.configureReporting(0x0201, 0x0000, 0x29, 10, 60, 50), 	//Attribute ID 0x0000 = local temperature, Data Type: S16BIT
		zigbee.configureReporting(0x0201, 0x0012, 0x29, 1, 0, 50),  	//Attribute ID 0x0012 = occupied heat setpoint, Data Type: S16BIT
		zigbee.configureReporting(0x0201, 0x0008, 0x20, 300, 900, 5),   //Attribute ID 0x0008 = pi heating demand, Data Type: U8BIT
		
		//Cluster ID (0x0204 = Thermostat Ui Conf Cluster), Attribute ID, Data Type, Payload (Min report, Max report, On change trigger)
		zigbee.configureReporting(0x0204, 0x0000, 0x30, 1, 0, 1),   //Attribute ID 0x0000 = temperature display mode, Data Type: 8 bits enum
		zigbee.configureReporting(0x0204, 0x0001, 0x30, 1, 0, 1),   //Attribute ID 0x0001 = keypad lockout, Data Type: 8 bits enum
		
		zigbee.configureReporting(0x0405, 0x0000, 0x21, 10, 300, 1), 		//Attribute ID 0x0000 = local humidity, Data Type: U16BIT
		
		//Read the configured variables
		zigbee.readAttribute(0x201, 0x0000),	//Read Local Temperature
		zigbee.readAttribute(0x201, 0x0012),	//Read Heat Setpoint
		zigbee.readAttribute(0x201, 0x0008),	//Read PI Heating State
		zigbee.readAttribute(0x204, 0x0000),	//Read Temperature Display Mode
		zigbee.readAttribute(0x204, 0x0001),	//Read Keypad Lockout
		zigbee.readAttribute(0x405, 0x0000),	//Read Local Humidity
	], 200)

	requests
}

private hex(value) {
	new BigInteger(Math.round(value).toString()).toString(16)
}
