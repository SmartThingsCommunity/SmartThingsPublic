/**
 *  Copyright 2015 SmartThings
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
	// Automatically generated. Make future change here.
	definition (name: "Simulated Thermostat", namespace: "smartthings/testing", author: "SmartThings") {
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
				attributeState("idle", backgroundColor:"#44b621")
				attributeState("heating", backgroundColor:"#ffa81e")
				attributeState("cooling", backgroundColor:"#269bd2")
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

		valueTile("temperature", "device.temperature", width: 2, height: 2) {
			state("temperature", label:'${currentValue}', unit:"dF",
				backgroundColors:[
					[value: 31, color: "#153591"],
					[value: 44, color: "#1e9cbb"],
					[value: 59, color: "#90d2a7"],
					[value: 74, color: "#44b621"],
					[value: 84, color: "#f1d801"],
					[value: 95, color: "#d04e00"],
					[value: 96, color: "#bc2323"]
				]
			)
		}
		standardTile("tempDown", "device.temperature", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
			state "default", label:'down', action:"tempDown"
		}
		standardTile("tempUp", "device.temperature", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
			state "default", label:'up', action:"tempUp"
		}

		valueTile("heatingSetpoint", "device.heatingSetpoint", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
			state "heat", label:'${currentValue} heat', unit: "F", backgroundColor:"#ffffff"
		}
		standardTile("heatDown", "device.temperature", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
			state "default", label:'down', action:"heatDown"
		}
		standardTile("heatUp", "device.temperature", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
			state "default", label:'up', action:"heatUp"
		}

		valueTile("coolingSetpoint", "device.coolingSetpoint", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
			state "cool", label:'${currentValue} cool', unit:"F", backgroundColor:"#ffffff"
		}
		standardTile("coolDown", "device.temperature", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
			state "default", label:'down', action:"coolDown"
		}
		standardTile("coolUp", "device.temperature", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
			state "default", label:'up', action:"coolUp"
		}

		standardTile("mode", "device.thermostatMode", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
			state "off", label:'${name}', action:"thermostat.heat", backgroundColor:"#ffffff"
			state "heat", label:'${name}', action:"thermostat.cool", backgroundColor:"#ffa81e"
			state "cool", label:'${name}', action:"thermostat.auto", backgroundColor:"#269bd2"
			state "auto", label:'${name}', action:"thermostat.off", backgroundColor:"#79b821"
		}
		standardTile("fanMode", "device.thermostatFanMode", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
			state "fanAuto", label:'${name}', action:"thermostat.fanOn", backgroundColor:"#ffffff"
			state "fanOn", label:'${name}', action:"thermostat.fanCirculate", backgroundColor:"#ffffff"
			state "fanCirculate", label:'${name}', action:"thermostat.fanAuto", backgroundColor:"#ffffff"
		}
		standardTile("operatingState", "device.thermostatOperatingState", width: 2, height: 2) {
			state "idle", label:'${name}', backgroundColor:"#ffffff"
			state "heating", label:'${name}', backgroundColor:"#ffa81e"
			state "cooling", label:'${name}', backgroundColor:"#269bd2"
		}

		main("thermostatMulti")
		details([
			"temperature","tempDown","tempUp",
			"mode", "fanMode", "operatingState",
			"heatingSetpoint", "heatDown", "heatUp",
			"coolingSetpoint", "coolDown", "coolUp",
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

def poll() {
	null
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
