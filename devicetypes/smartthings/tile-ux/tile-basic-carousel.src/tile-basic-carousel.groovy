/**
 *  Copyright 2016 SmartThings, Inc.
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
	definition (
		name: "carouselDeviceTile",
		namespace: "smartthings/tile-ux",
		author: "SmartThings") {

		capability "Thermostat"
		capability "Relative Humidity Measurement"

		command "tempUp"
		command "tempDown"
		command "heatUp"
		command "heatDown"
		command "coolUp"
		command "coolDown"
		command "setTemperature", ["number"]
	}

	tiles(scale: 2) {
		multiAttributeTile(name:"thermostatMulti", type:"thermostat", width:6, height:4) {
			tileAttribute("device.temperature", key: "PRIMARY_CONTROL") {
				attributeState("default", label:'${currentValue}', unit:"dF")
			}
			tileAttribute("device.temperature", key: "VALUE_CONTROL") {
				attributeState("default", action: "setTemperature")
			}
			tileAttribute("device.humidity", key: "SECONDARY_CONTROL") {
				attributeState("default", label:'${currentValue}%', unit:"%")
			}
			tileAttribute("device.thermostatOperatingState", key: "OPERATING_STATE") {
				attributeState("idle", backgroundColor:"#ffffff")
				attributeState("heating", backgroundColor:"#e86d13")
				attributeState("cooling", backgroundColor:"#00a0dc")
			}
			tileAttribute("device.thermostatMode", key: "THERMOSTAT_MODE") {
				attributeState("off", label:'${name}')
				attributeState("heat", label:'${name}')
				attributeState("cool", label:'${name}')
				attributeState("auto", label:'${name}')
			}
			tileAttribute("device.heatingSetpoint", key: "HEATING_SETPOINT") {
				attributeState("default", label:'${currentValue}', unit:"dF")
			}
			tileAttribute("device.coolingSetpoint", key: "COOLING_SETPOINT") {
				attributeState("default", label:'${currentValue}', unit:"dF")
			}
		}

		main("thermostatMulti")
		details([
			"thermostatMulti"
		])
	}
}

def installed() {
	sendEvent(name: "temperature", value: 72, unit: "F")
	sendEvent(name: "heatingSetpoint", value: 70, unit: "F")
	sendEvent(name: "thermostatSetpoint", value: 70, unit: "F")
	sendEvent(name: "coolingSetpoint", value: 76, unit: "F")
	sendEvent(name: "thermostatMode", value: "off")
	sendEvent(name: "thermostatFanMode", value: "fanAuto")
	sendEvent(name: "thermostatOperatingState", value: "idle")
	sendEvent(name: "humidity", value: 53, unit: "%")
}

def parse(String description) {
}

def evaluate(temp, heatingSetpoint, coolingSetpoint) {
	log.debug "evaluate($temp, $heatingSetpoint, $coolingSetpoint"
	def threshold = 1.0
	def current = device.currentValue("thermostatOperatingState")
	def mode = device.currentValue("thermostatMode")

	def heating = false
	def cooling = false
	def idle = false
	if (mode in ["heat","emergency heat","auto"]) {
		if (heatingSetpoint - temp >= threshold) {
			heating = true
			sendEvent(name: "thermostatOperatingState", value: "heating")
		}
		else if (temp - heatingSetpoint >= threshold) {
			idle = true
		}
		sendEvent(name: "thermostatSetpoint", value: heatingSetpoint)
	}
	if (mode in ["cool","auto"]) {
		if (temp - coolingSetpoint >= threshold) {
			cooling = true
			sendEvent(name: "thermostatOperatingState", value: "cooling")
		}
		else if (coolingSetpoint - temp >= threshold && !heating) {
			idle = true
		}
		sendEvent(name: "thermostatSetpoint", value: coolingSetpoint)
	}
	else {
		sendEvent(name: "thermostatSetpoint", value: heatingSetpoint)
	}
	if (idle && !heating && !cooling) {
		sendEvent(name: "thermostatOperatingState", value: "idle")
	}
}

def setHeatingSetpoint(Double degreesF) {
	log.debug "setHeatingSetpoint($degreesF)"
	sendEvent(name: "heatingSetpoint", value: degreesF)
	evaluate(device.currentValue("temperature"), degreesF, device.currentValue("coolingSetpoint"))
}

def setCoolingSetpoint(Double degreesF) {
	log.debug "setCoolingSetpoint($degreesF)"
	sendEvent(name: "coolingSetpoint", value: degreesF)
	evaluate(device.currentValue("temperature"), device.currentValue("heatingSetpoint"), degreesF)
}

def setThermostatMode(String value) {
	sendEvent(name: "thermostatMode", value: value)
	evaluate(device.currentValue("temperature"), device.currentValue("heatingSetpoint"), device.currentValue("coolingSetpoint"))
}

def setThermostatFanMode(String value) {
	sendEvent(name: "thermostatFanMode", value: value)
}

def off() {
	sendEvent(name: "thermostatMode", value: "off")
	evaluate(device.currentValue("temperature"), device.currentValue("heatingSetpoint"), device.currentValue("coolingSetpoint"))
}

def heat() {
	sendEvent(name: "thermostatMode", value: "heat")
	evaluate(device.currentValue("temperature"), device.currentValue("heatingSetpoint"), device.currentValue("coolingSetpoint"))
}

def auto() {
	sendEvent(name: "thermostatMode", value: "auto")
	evaluate(device.currentValue("temperature"), device.currentValue("heatingSetpoint"), device.currentValue("coolingSetpoint"))
}

def emergencyHeat() {
	sendEvent(name: "thermostatMode", value: "emergency heat")
	evaluate(device.currentValue("temperature"), device.currentValue("heatingSetpoint"), device.currentValue("coolingSetpoint"))
}

def cool() {
	sendEvent(name: "thermostatMode", value: "cool")
	evaluate(device.currentValue("temperature"), device.currentValue("heatingSetpoint"), device.currentValue("coolingSetpoint"))
}

def fanOn() {
	sendEvent(name: "thermostatFanMode", value: "fanOn")
}

def fanAuto() {
	sendEvent(name: "thermostatFanMode", value: "fanAuto")
}

def fanCirculate() {
	sendEvent(name: "thermostatFanMode", value: "fanCirculate")
}

def tempUp() {
	def ts = device.currentState("temperature")
	def value = ts ? ts.integerValue + 1 : 72
	sendEvent(name:"temperature", value: value)
	evaluate(value, device.currentValue("heatingSetpoint"), device.currentValue("coolingSetpoint"))
}

def tempDown() {
	def ts = device.currentState("temperature")
	def value = ts ? ts.integerValue - 1 : 72
	sendEvent(name:"temperature", value: value)
	evaluate(value, device.currentValue("heatingSetpoint"), device.currentValue("coolingSetpoint"))
}

def setTemperature(value) {
	def ts = device.currentState("temperature")
	sendEvent(name:"temperature", value: value)
	evaluate(value, device.currentValue("heatingSetpoint"), device.currentValue("coolingSetpoint"))
}

def heatUp() {
	def ts = device.currentState("heatingSetpoint")
	def value = ts ? ts.integerValue + 1 : 68
	sendEvent(name:"heatingSetpoint", value: value)
	evaluate(device.currentValue("temperature"), value, device.currentValue("coolingSetpoint"))
}

def heatDown() {
	def ts = device.currentState("heatingSetpoint")
	def value = ts ? ts.integerValue - 1 : 68
	sendEvent(name:"heatingSetpoint", value: value)
	evaluate(device.currentValue("temperature"), value, device.currentValue("coolingSetpoint"))
}


def coolUp() {
	def ts = device.currentState("coolingSetpoint")
	def value = ts ? ts.integerValue + 1 : 76
	sendEvent(name:"coolingSetpoint", value: value)
	evaluate(device.currentValue("temperature"), device.currentValue("heatingSetpoint"), value)
}

def coolDown() {
	def ts = device.currentState("coolingSetpoint")
	def value = ts ? ts.integerValue - 1 : 76
	sendEvent(name:"coolingSetpoint", value: value)
	evaluate(device.currentValue("temperature"), device.currentValue("heatingSetpoint"), value)
}
