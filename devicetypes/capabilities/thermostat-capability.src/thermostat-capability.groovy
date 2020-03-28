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
	definition (name: "Thermostat Capability", namespace: "capabilities", author: "SmartThings") {
		capability "Thermostat"
	}

	// simulator metadata
	simulator {
		["on","off","heat","cool","emergency heat"].each {
			status "$it": "thermostatMode:$it"
		}

		["on","auto","circulate"].each {
			status "fan $it": "thermostatFanMode:$it"
		}

		[60,68,72].each {
			status "heat $it": "heatingSetpoint:$it"
		}

		[72,76,80,85].each {
			status "cool $it": "coolingSetpoint:$it"
		}

		[40,58,62,70,74,78,82,86].each {
			status "temp $it": "temperature:$it"
		}

		// reply messages
		//reply "2502": "command: 2503, payload: FF"
		["on","off","heat","cool","emergency heat"].each {
			reply "thermostatMode:$it": "thermostatMode:$it"
		}
		["on","auto","circulate"].each {
			reply "thermostatFanMode:$it": "thermostatFanMode:$it"
		}
		for (n in 60..90) {
			reply "heatingSetpoint:${n}": "heatingSetpoint:$n"
			reply "heatingSetpoint:${n}.0": "heatingSetpoint:$n"
		}
		for (n in 60..90) {
			reply "coolingSetpoint:${n}": "coolingSetpoint:$n"
			reply "coolingSetpoint:${n}.0": "coolingSetpoint:$n"
		}
	}

	tiles {
		valueTile("temperature", "device.temperature", width: 2, height: 2) {
			state("temperature", label:'${currentValue}°', unit:"F",
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
		valueTile("heatingSetpoint", "device.heatingSetpoint", inactiveLabel: false, decoration: "flat") {
			state "heat", label:'${currentValue}° heat', unit: "F", backgroundColor:"#ffffff"
		}
		valueTile("coolingSetpoint", "device.coolingSetpoint", inactiveLabel: false, decoration: "flat") {
			state "cool", label:'${currentValue}° cool', unit:"F", backgroundColor:"#ffffff"
		}
		standardTile("mode", "device.thermostatMode", inactiveLabel: false, decoration: "flat") {
			state "off", label:'${name}', action:"thermostat.emergencyHeat", backgroundColor:"#ffffff"
			state "emergencyHeat", label:'${name}', action:"thermostat.heat", backgroundColor:"#e86d13"
			state "heat", label:'${name}', action:"thermostat.cool", backgroundColor:"#e86d13"
			state "cool", label:'${name}', action:"thermostat.off", backgroundColor:"#00A0DC"
		}
		standardTile("fanMode", "device.thermostatFanMode", inactiveLabel: false, decoration: "flat") {
			state "fanAuto", label:'${name}', action:"thermostat.fanOn", backgroundColor:"#ffffff"
			state "fanOn", label:'${name}', action:"thermostat.fanCirculate", backgroundColor:"#ffffff"
			state "fanCirculate", label:'${name}', action:"thermostat.fanAuto", backgroundColor:"#ffffff"
		}

		main "temperature"
		details(["temperature", "heatingSetpoint", "coolingSetpoint", "mode", "fanMode"])
	}
}

def parse(String description)
{
	def pair = description.split(":")
	def map = createEvent(name: pair[0].trim(), value: pair[1].trim())
	def result = [map]

	if (map.isStateChange && map.name in ["heatingSetpoint","coolingSetpoint","thermostatMode"]) {
		def map2 = [
			name: "thermostatSetpoint",
			unit: "F"
		]
		if (map.name == "thermostatMode") {
			if (map.value == "cool") {
				map2.value = device.latestValue("coolingSetpoint")
				log.info "THERMOSTAT, latest cooling setpoint = ${map2.value}"
			}
			else {
				map2.value = device.latestValue("heatingSetpoint")
				log.info "THERMOSTAT, latest heating setpoint = ${map2.value}"
			}
		}
		else {
			def mode = device.latestValue("thermostatMode")
			log.info "THERMOSTAT, latest mode = ${mode}"
			if ((map.name == "heatingSetpoint" && mode == "heat") || (map.name == "coolingSetpoint" && mode == "cool")) {
				map2.value = map.value
				map2.unit = map.unit
			}
		}
		if (map2.value != null) {
			log.debug "THERMOSTAT, adding setpoint event: $map"
			result << createEvent(map2)
		}
	}
	log.debug "Parse returned ${result?.descriptionText}"
	result
}

def setHeatingSetpoint(Double degreesF) {
	"heatingSetpoint:$degreesF"
}

def setCoolingSetpoint(Double degreesF) {
	"coolingSetpoint:$degreesF"
}

def setThermostatMode(String value) {
	"thermostatMode:$value"
}

def setThermostatFanMode(String value) {
	"thermostatFanMode:$value"
}

def off() {
	"thermostatMode:off"
}

def heat() {
	"thermostatMode:heat"
}

def emergencyHeat() {
	"thermostatMode:emergency heat"
}

def cool() {
	"thermostatMode:cool"
}

def fanOn() {
	"thermostatFanMode:on"
}

def fanAuto() {
	"thermostatMode:auto"
}

def fanCirculate() {
	"thermostatMode:circulate"
}

def poll() {
	null
}

