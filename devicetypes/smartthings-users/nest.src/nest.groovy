/**
 *  Nest Direct
 *
 *  Author: dianoga7@3dgo.net
 *  Code: https://github.com/smartthings-users/device-type.nest
 *
 * INSTALLATION
 * =========================================
 * 1) Create a new device type (https://graph.api.smartthings.com/ide/devices)
 *     Name: Nest
 *     Author: dianoga7@3dgo.net
 *     Capabilities:
 *         Polling
 *         Relative Humidity Measurement
 *         Thermostat
 *         Temperature Measurement
 *         Presence Sensor
 *         Sensor
 *     Custom Attributes:
 *         temperatureUnit
 *     Custom Commands:
 *         away
 *         present
 *         setPresence
 *         heatingSetpointUp
 *         heatingSetpointDown
 *         coolingSetpointUp
 *         coolingSetpointDown
 *         setFahrenheit
 *         setCelsius
 *
 * 2) If you want to switch from slider controls to buttons, comment out the slider details line and uncomment the button details line.
 *
 * 3) Create a new device (https://graph.api.smartthings.com/device/list)
 *     Name: Your Choice
 *     Device Network Id: Your Choice
 *     Type: Nest (should be the last option)
 *     Location: Choose the correct location
 *     Hub/Group: Leave blank
 *
 * 4) Update device preferences
 *     Click on the new device to see the details.
 *     Click the edit button next to Preferences
 *     Fill in your information.
 *     To find your serial number, login to http://home.nest.com. Click on the thermostat
 *     you want to control. Under settings, go to Technical Info. Your serial number is
 *     the second item.
 *
 * 5) That's it, you're done.
 *
 * Copyright (C) 2013 Brian Steere <dianoga7@3dgo.net>
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this
 * software and associated documentation files (the "Software"), to deal in the Software
 * without restriction, including without limitation the rights to use, copy, modify,
 * merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following
 * conditions: The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE
 * OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

preferences {
	input("username", "text", title: "Username", description: "Your Nest username (usually an email address)")
	input("password", "password", title: "Password", description: "Your Nest password")
	input("serial", "text", title: "Serial #", description: "The serial number of your thermostat")
}

// for the UI
metadata {
	definition (name: "Nest", namespace: "smartthings-users", author: "dianoga7@3dgo.net") {
		capability "Polling"
		capability "Relative Humidity Measurement"
		capability "Thermostat"
		capability "Temperature Measurement"
		capability "Presence Sensor"
		capability "Sensor"

		command "away"
		command "present"
		command "setPresence"
		command "heatingSetpointUp"
		command "heatingSetpointDown"
		command "coolingSetpointUp"
		command "coolingSetpointDown"
		command "setFahrenheit"
		command "setCelsius"

		attribute "temperatureUnit", "string"
	}

	simulator {
		// TODO: define status and reply messages here
	}

	tiles {
		valueTile("temperature", "device.temperature", canChangeIcon: true, canChangeBackground:true) {
			state("temperature", label: '${currentValue}°', backgroundColors: [
				// Celsius Color Range
				[value: 0, color: "#153591"],
				[value: 7, color: "#1e9cbb"],
				[value: 15, color: "#90d2a7"],
				[value: 23, color: "#44b621"],
				[value: 29, color: "#f1d801"],
				[value: 33, color: "#d04e00"],
				[value: 36, color: "#bc2323"],
				// Fahrenheit Color Range
				[value: 40, color: "#153591"],
				[value: 44, color: "#1e9cbb"],
				[value: 59, color: "#90d2a7"],
				[value: 74, color: "#44b621"],
				[value: 84, color: "#f1d801"],
				[value: 92, color: "#d04e00"],
				[value: 96, color: "#bc2323"]
			]
			)
		}

		standardTile("thermostatMode", "device.thermostatMode", inactiveLabel: true, decoration: "flat") {
			state("auto", action:"thermostat.off", icon: "st.thermostat.auto")
			state("off", action:"thermostat.cool", icon: "st.thermostat.heating-cooling-off")
			state("cool", action:"thermostat.heat", icon: "st.thermostat.cool")
			state("heat", action:"thermostat.auto", icon: "st.thermostat.heat")
		}

		standardTile("thermostatFanMode", "device.thermostatFanMode", inactiveLabel: true, decoration: "flat") {
			state "auto", action:"thermostat.fanOn", icon: "st.thermostat.fan-auto"
			state "on", action:"thermostat.fanCirculate", icon: "st.thermostat.fan-on"
			state "circulate", action:"thermostat.fanAuto", icon: "st.thermostat.fan-circulate"
		}

		valueTile("heatingSetpoint", "device.heatingSetpoint") {
			state "default", label:'${currentValue}°', unit:"Heat", backgroundColor:"#bc2323"
		}

		valueTile("coolingSetpoint", "device.coolingSetpoint") {
			state "default", label:'${currentValue}°', unit:"Cool", backgroundColor:"#1e9cbb"
		}

		standardTile("thermostatOperatingState", "device.thermostatOperatingState", inactiveLabel: false, decoration: "flat") {
			state "idle", action:"polling.poll", label:'${name}', icon: "st.sonos.pause-icon"
			state "cooling", action:"polling.poll", label:'  ', icon: "st.thermostat.cooling", backgroundColor:"#1e9cbb"
			state "heating", action:"polling.poll", label:'  ', icon: "st.thermostat.heating", backgroundColor:"#bc2323"
			state "fan only", action:"polling.poll", label:'${name}', icon: "st.Appliances.appliances11"
		}

		valueTile("humidity", "device.humidity", inactiveLabel: false) {
			state "default", label:'${currentValue}%', unit:"Humidity"
		}

		standardTile("presence", "device.presence", inactiveLabel: false, decoration: "flat") {
			state "present", label:'${name}', action:"away", icon: "st.Home.home2"
			state "not present", label:'away', action:"present", icon: "st.Transportation.transportation5"
		}

		standardTile("refresh", "device.thermostatMode", inactiveLabel: false, decoration: "flat") {
			state "default", action:"polling.poll", icon:"st.secondary.refresh"
		}

		standardTile("temperatureUnit", "device.temperatureUnit", canChangeIcon: false) {
			state "fahrenheit",  label: "°F", icon: "st.alarm.temperature.normal", action:"setCelsius"
			state "celsius", label: "°C", icon: "st.alarm.temperature.normal", action:"setFahrenheit"
		}

		controlTile("heatSliderControl", "device.heatingSetpoint", "slider", height: 1, width: 2, inactiveLabel: false) {
			state "setHeatingSetpoint", label:'Set temperature to', action:"thermostat.setHeatingSetpoint"
		}
		controlTile("coolSliderControl", "device.coolingSetpoint", "slider", height: 1, width: 2, inactiveLabel: false) {
			state "setCoolingSetpoint", label:'Set temperature to', action:"thermostat.setCoolingSetpoint"
		}

		standardTile("heatingSetpointUp", "device.heatingSetpoint", canChangeIcon: false, inactiveLabel: false, decoration: "flat") {
			state "heatingSetpointUp", label:'  ', action:"heatingSetpointUp", icon:"st.thermostat.thermostat-up", backgroundColor:"#bc2323"
		}

		standardTile("heatingSetpointDown", "device.heatingSetpoint", canChangeIcon: false, inactiveLabel: false, decoration: "flat") {
			state "heatingSetpointDown", label:'  ', action:"heatingSetpointDown", icon:"st.thermostat.thermostat-down", backgroundColor:"#bc2323"
		}

		standardTile("coolingSetpointUp", "device.coolingSetpoint", canChangeIcon: false, inactiveLabel: false, decoration: "flat") {
			state "coolingSetpointUp", label:'  ', action:"coolingSetpointUp", icon:"st.thermostat.thermostat-up", backgroundColor:"#1e9cbb"
		}

		standardTile("coolingSetpointDown", "device.coolingSetpoint", canChangeIcon: false, inactiveLabel: false, decoration: "flat") {
			state "coolingSetpointDown", label:'  ', action:"coolingSetpointDown", icon:"st.thermostat.thermostat-down", backgroundColor:"#1e9cbb"
		}

		main(["temperature", "thermostatOperatingState", "humidity"])

		// ============================================================
		// Slider or Buttons...
		// To expose buttons, comment out the first detials line below and uncomment the second details line below.
		// To expose sliders, uncomment the first details line below and comment out the second details line below.

		details(["temperature", "thermostatOperatingState", "humidity", "thermostatMode", "thermostatFanMode", "presence", "heatingSetpoint", "heatSliderControl", "coolingSetpoint", "coolSliderControl", "temperatureUnit", "refresh"])
		// details(["temperature", "thermostatOperatingState", "humidity", "thermostatMode", "thermostatFanMode", "presence", "heatingSetpointDown", "heatingSetpoint", "heatingSetpointUp", "coolingSetpointDown", "coolingSetpoint", "coolingSetpointUp", "temperatureUnit", "refresh"])

		// ============================================================

	}

}

// parse events into attributes
def parse(String description) {

}

// handle commands
def setHeatingSetpoint(temp) {
	def latestThermostatMode = device.latestState('thermostatMode')
	def temperatureUnit = device.latestValue('temperatureUnit')

	switch (temperatureUnit) {
		case "celsius":
			if (temp) {
				if (temp < 9) {
					temp = 9
				}
				if (temp > 32) {
					temp = 32
				}
				if (latestThermostatMode.stringValue == 'auto') {
					api('temperature', ['target_change_pending': true, 'target_temperature_low': temp]) {
						sendEvent(name: 'heatingSetpoint', value: heatingSetpoint, unit: temperatureUnit, state: "heat")
					}
				} else if (latestThermostatMode.stringValue == 'heat') {
					api('temperature', ['target_change_pending': true, 'target_temperature': temp]) {
							sendEvent(name: 'heatingSetpoint', value: heatingSetpoint, unit: temperatureUnit, state: "heat")
					}
				}
			}
			break;
		default:
			if (temp) {
				if (temp < 51) {
					temp = 51
				}
				if (temp > 89) {
					temp = 89
				}
				if (latestThermostatMode.stringValue == 'auto') {
					api('temperature', ['target_change_pending': true, 'target_temperature_low': fToC(temp)]) {
						sendEvent(name: 'heatingSetpoint', value: heatingSetpoint, unit: temperatureUnit, state: "heat")
					}
				} else if (latestThermostatMode.stringValue == 'heat') {
					api('temperature', ['target_change_pending': true, 'target_temperature': fToC(temp)]) {
						sendEvent(name: 'heatingSetpoint', value: heatingSetpoint, unit: temperatureUnit, state: "heat")
					}
				}
			}
			break;
	}
	poll()
}

def coolingSetpointUp(){
	int newSetpoint = device.currentValue("coolingSetpoint") + 1
	log.debug "Setting cool set point up to: ${newSetpoint}"
	setCoolingSetpoint(newSetpoint)
}

def coolingSetpointDown(){
	int newSetpoint = device.currentValue("coolingSetpoint") - 1
	log.debug "Setting cool set point down to: ${newSetpoint}"
	setCoolingSetpoint(newSetpoint)
}

def setCoolingSetpoint(temp) {
	def latestThermostatMode = device.latestState('thermostatMode')
	def temperatureUnit = device.latestValue('temperatureUnit')

	switch (temperatureUnit) {
		case "celsius":
			if (temp) {
				if (temp < 9) {
					temp = 9
				}
				if (temp > 32) {
					temp = 32
				}
				if (latestThermostatMode.stringValue == 'auto') {
					api('temperature', ['target_change_pending': true, 'target_temperature_high': temp]) {
						sendEvent(name: 'coolingSetpoint', value: coolingSetpoint, unit: temperatureUnit, state: "cool")
					}
				} else if (latestThermostatMode.stringValue == 'cool') {
					api('temperature', ['target_change_pending': true, 'target_temperature': temp]) {
						sendEvent(name: 'coolingSetpoint', value: coolingSetpoint, unit: temperatureUnit, state: "cool")
					}
				}
			}
			break;
		default:
			if (temp) {
				if (temp < 51) {
					temp = 51
				}
				if (temp > 89) {
					temp = 89
				}
				if (latestThermostatMode.stringValue == 'auto') {
					api('temperature', ['target_change_pending': true, 'target_temperature_high': fToC(temp)]) {
						sendEvent(name: 'coolingSetpoint', value: coolingSetpoint, unit: temperatureUnit, state: "cool")
					}
				} else if (latestThermostatMode.stringValue == 'cool') {
					api('temperature', ['target_change_pending': true, 'target_temperature': fToC(temp)]) {
						sendEvent(name: 'coolingSetpoint', value: coolingSetpoint, unit: temperatureUnit, state: "cool")
					}
				}
			}
			break;
	}
	poll()
}

def heatingSetpointUp(){
	int newSetpoint = device.currentValue("heatingSetpoint") + 1
	log.debug "Setting heat set point up to: ${newSetpoint}"
	setHeatingSetpoint(newSetpoint)
}

def heatingSetpointDown(){
	int newSetpoint = device.currentValue("heatingSetpoint") - 1
	log.debug "Setting heat set point down to: ${newSetpoint}"
	setHeatingSetpoint(newSetpoint)
}

def setFahrenheit() {
	def temperatureUnit = "fahrenheit"
	log.debug "Setting temperatureUnit to: ${temperatureUnit}"
	sendEvent(name: "temperatureUnit",   value: temperatureUnit)
	poll()
}

def setCelsius() {
	def temperatureUnit = "celsius"
	log.debug "Setting temperatureUnit to: ${temperatureUnit}"
	sendEvent(name: "temperatureUnit",   value: temperatureUnit)
	poll()
}

def off() {
	setThermostatMode('off')
}

def heat() {
	setThermostatMode('heat')
}

def emergencyHeat() {
	setThermostatMode('heat')
}

def cool() {
	setThermostatMode('cool')
}

def auto() {
	setThermostatMode('range')
}

def setThermostatMode(mode) {
	mode = mode == 'emergency heat'? 'heat' : mode

	api('thermostat_mode', ['target_change_pending': true, 'target_temperature_type': mode]) {
		mode = mode == 'range' ? 'auto' : mode
		sendEvent(name: 'thermostatMode', value: mode)
		poll()
	}
}

def fanOn() {
	setThermostatFanMode('on')
}

def fanAuto() {
	setThermostatFanMode('auto')
}

def fanCirculate() {
	setThermostatFanMode('circulate')
}

def setThermostatFanMode(mode) {
	def modes = [
		on: ['fan_mode': 'on'],
		auto: ['fan_mode': 'auto'],
		circulate: ['fan_mode': 'duty-cycle', 'fan_duty_cycle': 900]
	]

	api('fan_mode', modes.getAt(mode)) {
		sendEvent(name: 'thermostatFanMode', value: mode)
		poll()
	}
}

def away() {
	setPresence('away')
	sendEvent(name: 'presence', value: 'not present')
}

def present() {
	setPresence('present')
	sendEvent(name: 'presence', value: 'present')
}

def setPresence(status) {
	log.debug "Status: $status"
	api('presence', ['away': status == 'away', 'away_timestamp': new Date().getTime(), 'away_setter': 0]) {
		poll()
	}
}

def poll() {
	log.debug "Executing 'poll'"
	api('status', []) {
		data.device = it.data.device.getAt(settings.serial)
		data.shared = it.data.shared.getAt(settings.serial)
		data.structureId = it.data.link.getAt(settings.serial).structure.tokenize('.')[1]
		data.structure = it.data.structure.getAt(data.structureId)

		data.device.fan_mode = data.device.fan_mode == 'duty-cycle'? 'circulate' : data.device.fan_mode
		data.structure.away = data.structure.away ? 'away' : 'present'

		log.debug(data.shared)

		def humidity = data.device.current_humidity
		def temperatureType = data.shared.target_temperature_type
		def fanMode = data.device.fan_mode
		def heatingSetpoint = '--'
		def coolingSetpoint = '--'

		temperatureType = temperatureType == 'range' ? 'auto' : temperatureType

		sendEvent(name: 'humidity', value: humidity)
		sendEvent(name: 'thermostatFanMode', value: fanMode)
		sendEvent(name: 'thermostatMode', value: temperatureType)

		def temperatureUnit = device.latestValue('temperatureUnit')

		switch (temperatureUnit) {
			case "celsius":
				def temperature = Math.round(data.shared.current_temperature)
				def targetTemperature = Math.round(data.shared.target_temperature)

				if (temperatureType == "cool") {
					coolingSetpoint = targetTemperature
				} else if (temperatureType == "heat") {
					heatingSetpoint = targetTemperature
				} else if (temperatureType == "auto") {
					coolingSetpoint = Math.round(data.shared.target_temperature_high)
					heatingSetpoint = Math.round(data.shared.target_temperature_low)
				}

				sendEvent(name: 'temperature', value: temperature, unit: temperatureUnit, state: temperatureType)
				sendEvent(name: 'coolingSetpoint', value: coolingSetpoint, unit: temperatureUnit, state: "cool")
				sendEvent(name: 'heatingSetpoint', value: heatingSetpoint, unit: temperatureUnit, state: "heat")
				break;
			default:
				def temperature = Math.round(cToF(data.shared.current_temperature))
				def targetTemperature = Math.round(cToF(data.shared.target_temperature))

				if (temperatureType == "cool") {
					coolingSetpoint = targetTemperature
				} else if (temperatureType == "heat") {
					heatingSetpoint = targetTemperature
				} else if (temperatureType == "auto") {
					coolingSetpoint = Math.round(cToF(data.shared.target_temperature_high))
					heatingSetpoint = Math.round(cToF(data.shared.target_temperature_low))
				}

				sendEvent(name: 'temperature', value: temperature, unit: temperatureUnit, state: temperatureType)
				sendEvent(name: 'coolingSetpoint', value: coolingSetpoint, unit: temperatureUnit, state: "cool")
				sendEvent(name: 'heatingSetpoint', value: heatingSetpoint, unit: temperatureUnit, state: "heat")
				break;
			}

		switch (device.latestValue('presence')) {
			case "present":
				if (data.structure.away == 'away') {
					sendEvent(name: 'presence', value: 'not present')
				}
				break;
			case "not present":
				if (data.structure.away == 'present') {
					sendEvent(name: 'presence', value: 'present')
				}
				break;
		}

		if (data.shared.hvac_ac_state) {
			sendEvent(name: 'thermostatOperatingState', value: "cooling")
		} else if (data.shared.hvac_heater_state) {
			sendEvent(name: 'thermostatOperatingState', value: "heating")
		} else if (data.shared.hvac_fan_state) {
			sendEvent(name: 'thermostatOperatingState', value: "fan only")
		} else {
			sendEvent(name: 'thermostatOperatingState', value: "idle")
		}
	}
}

def api(method, args = [], success = {}) {
	if(!isLoggedIn()) {
		log.debug "Need to login"
		login(method, args, success)
		return
	}

	def methods = [
		'status': [uri: "/v2/mobile/${data.auth.user}", type: 'get'],
		'fan_mode': [uri: "/v2/put/device.${settings.serial}", type: 'post'],
		'thermostat_mode': [uri: "/v2/put/shared.${settings.serial}", type: 'post'],
		'temperature': [uri: "/v2/put/shared.${settings.serial}", type: 'post'],
		'presence': [uri: "/v2/put/structure.${data.structureId}", type: 'post']
	]

	def request = methods.getAt(method)

	log.debug "Logged in"
	doRequest(request.uri, args, request.type, success)
}

// Need to be logged in before this is called. So don't call this. Call api.
def doRequest(uri, args, type, success) {
	log.debug "Calling $type : $uri : $args"

	if(uri.charAt(0) == '/') {
		uri = "${data.auth.urls.transport_url}${uri}"
	}

	def params = [
		uri: uri,
		headers: [
			'X-nl-protocol-version': 1,
			'X-nl-user-id': data.auth.userid,
			'Authorization': "Basic ${data.auth.access_token}"
		],
		body: args
	]

	def postRequest = { response ->
		if (response.getStatus() == 302) {
			def locations = response.getHeaders("Location")
			def location = locations[0].getValue()
			log.debug "redirecting to ${location}"
			doRequest(location, args, type, success)
		} else {
			success.call(response)
		}
	}

	try {
		if (type == 'post') {
			httpPostJson(params, postRequest)
		} else if (type == 'get') {
			httpGet(params, postRequest)
		}
	} catch (Throwable e) {
		login()
	}
}

def login(method = null, args = [], success = {}) {
	def params = [
		uri: 'https://home.nest.com/user/login',
		body: [username: settings.username, password: settings.password]
	]

	httpPost(params) {response ->
		data.auth = response.data
		data.auth.expires_in = Date.parse('EEE, dd-MMM-yyyy HH:mm:ss z', response.data.expires_in).getTime()
		log.debug data.auth

		api(method, args, success)
	}
}

def isLoggedIn() {
	if(!data.auth) {
		log.debug "No data.auth"
		return false
	}

	def now = new Date().getTime();
	return data.auth.expires_in > now
}

def cToF(temp) {
	return (temp * 1.8 + 32).toDouble()
}

def fToC(temp) {
	return ((temp - 32) / 1.8).toDouble()
}