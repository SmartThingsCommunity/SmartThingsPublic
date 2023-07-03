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
 *  Keep Me Cozy II
 *
 *  Author: SmartThings
 */

definition(
    name: "Keep Me Cozy II",
    namespace: "smartthings",
    author: "SmartThings",
    description: "Control the temperature in a separate space from the thermostat using an alternative temperature sensor. Focuses on making you comfortable where you are spending your time rather than where the thermostat is located.",
    category: "Green Living",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Meta/temp_thermo.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Meta/temp_thermo@2x.png"
)

preferences() {
	section("Choose thermostat... ") {
		input "thermostat", "capability.thermostat"
	}
	section("Heat setting..." ) {
		input "heatingSetpoint", "decimal", title: "Degrees"
	}
	section("Air conditioning setting...") {
		input "coolingSetpoint", "decimal", title: "Degrees"
	}
	section("Thermostat setpoint range setting...") {
		input "temperatureMin", "decimal", title: "Min Degrees"		
		input "temperatureMax", "decimal", title: "Max Degrees"		
	}	
	section("Optionally choose a temperature sensor to use instead of the thermostat's... ") {
		input "sensor", "capability.temperatureMeasurement", title: "Temp Sensor", required: false
	}
}

def installed()
{
	log.debug "enter installed, state: $state"
	subscribeToEvents()
}

def updated()
{
	log.debug "enter updated, state: $state"
	unsubscribe()
	subscribeToEvents()
}

def subscribeToEvents()
{
	subscribe(location, changedLocationMode)
	if (sensor) {
		subscribe(sensor, "temperature", temperatureHandler)
		subscribe(thermostat, "temperature", temperatureHandler)
		subscribe(thermostat, "thermostatMode", temperatureHandler)
	}
	evaluate()
}

def changedLocationMode(evt)
{
	log.debug "changedLocationMode mode: $evt.value, heat: $heat, cool: $cool"
	evaluate()
}

def temperatureHandler(evt)
{
	evaluate()
}

private evaluate()
{
	if (sensor) {
		def THRESHOLD = 1.0
		def TEMP_CHANGE = 2;

		def sensorTemp = sensor.currentTemperature
		def thermoTemp = thermostat.currentTemperature
		def thermoMode = thermostat.currentThermostatMode
		def thermoCoolSetpoint = thermostat.currentCoolingSetpoint
		def thermoHeatSetpoint = thermostat.currentHeatingSetpoint
		def temperatureRaise = thermoTemp + TEMP_CHANGE
		def temperatureLower = thermoTemp - TEMP_CHANGE		
		log.trace("evaluate:, mode: $tm -- temp: $thermoTemp, heat: $thermoHeatSetpoint, cool: $thermoCoolSetpoint -- "  +
			"sensor: $sensorTemp, heat: $heatingSetpoint, cool: $coolingSetpoint, min: $temperatureMin, max: $temperatureMax")
		if (thermoMode in ["cool","auto"]) {
			// Air Conditioner
			if (sensorTemp - coolingSetpoint >= THRESHOLD) { 
				if(temperatureLower >= temperatureMin){
					thermostat.setCoolingSetpoint(temperatureLower)
					log.debug "thermostat.setCoolingSetpoint(${temperatureLower}), ON"
				}
			}
			else if (coolingSetpoint - sensorTemp >= THRESHOLD && thermoTemp - thermoCoolSetpoint >= THRESHOLD) {
				if(temperatureRaise <= temperatureMax){
					thermostat.setCoolingSetpoint(temperatureRaise)
					log.debug "thermostat.setCoolingSetpoint(${temperatureRaise}), OFF"
				}
			}
		}
		if (thermoMode in ["heat","emergency heat","auto"]) {
			// Heater
			if (heatingSetpoint - sensorTemp >= THRESHOLD) {
				if(temperatureRaise <= temperatureMax){
					thermostat.setHeatingSetpoint(temperatureRaise)
					log.debug "thermostat.setHeatingSetpoint(${temperatureRaise}), ON"
				}
			}
			else if (sensorTemp - heatingSetpoint >= THRESHOLD && thermoHeatSetpoint - thermoTemp >= THRESHOLD) {
				if(temperatureLower >= temperatureMin){
					thermostat.setHeatingSetpoint(temperatureLower)
					log.debug "thermostat.setHeatingSetpoint(${temperatureLower}), OFF"
				}
			}
		}
	}
	else {
		thermostat.setHeatingSetpoint(heatingSetpoint)
		thermostat.setCoolingSetpoint(coolingSetpoint)
		if (thermostat.hasCommand("poll")) {
			thermostat.poll()
		}
	}
}

// for backward compatibility with existing subscriptions
def coolingSetpointHandler(evt) {
	log.debug "coolingSetpointHandler()"
}
def heatingSetpointHandler (evt) {
	log.debug "heatingSetpointHandler ()"
}
