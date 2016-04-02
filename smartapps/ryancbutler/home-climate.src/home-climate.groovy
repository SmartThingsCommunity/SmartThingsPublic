/**
 *  Home Climate
 *
 *  Copyright 2016 Ryan Butler
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
definition(
    name: "Home Climate",
    namespace: "ryancbutler",
    author: "Ryan Butler",
    description: "Enables you to pick an alternative temperature sensor in a separate space from the thermostat. Focuses on making you comfortable where you are spending your time rather than where the thermostat is located.",
    category: "Green Living",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


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
	section("Temperature sensor to use instead of the thermostat's... ") {
		input "sensor", "capability.temperatureMeasurement", title: "Temp Sensor"
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
	subscribe(sensor, "temperature", temperatureHandler)
	subscribe(thermostat, "temperature", temperatureHandler)
	subscribe(thermostat, "thermostatMode", temperatureHandler)
	
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
		def threshold = 2.0
		def tm = thermostat.currentThermostatMode
		def ct = thermostat.currentTemperature
		def sensortemp = sensor.currentTemperature
		log.trace("evaluate:, mode: $tm -- temp: $ct, CurrentSetheat: $thermostat.currentHeatingSetpoint, CurrentSetCool: $thermostat.currentCoolingSetpoint -- "  +
			"sensor: $sensortemp, Settoheat: $heatingSetpoint, Settocool: $coolingSetpoint")
		def whatsthediff = ct - sensortemp
        def absdiff = (whatsthediff).abs()
       	
        log.trace("We are off by $absdiff degrees")
        
        
        if (tm in ["cool","auto"]) {
			// air conditioner
			if (absdiff >= threshold) {
				def matchme = whatsthediff + coolingSetpoint
                log.trace("Setting to $matchme")
                thermostat.setCoolingSetpoint(matchme)			
                log.debug "thermostat.setCoolingSetpoint $matchme, ON"
			}
			else {
				thermostat.setCoolingSetpoint(coolingSetpoint)
				log.debug "thermostat.setCoolingSetpoint $heatingSetpoint, OFF"
			}
		}
		if (tm in ["heat","emergency heat","auto"]) {
			// heater
			if (absdiff >= threshold) {
            	def matchme = whatsthediff + heatingSetpoint
                log.trace("Setting to $matchme")
				thermostat.setHeatingSetpoint(matchme)
				log.debug "thermostat.setHeatingSetpoint $matchme, ON"
			}
			else{
				thermostat.setHeatingSetpoint(heatingSetpoint)
				log.debug "thermostat.setHeatingSetpoint $heatingSetpoint, OFF"
			}
		}

		thermostat.refresh()
	
}

// for backward compatibility with existing subscriptions
def coolingSetpointHandler(evt) {
	log.debug "coolingSetpointHandler()"
}
def heatingSetpointHandler (evt) {
	log.debug "heatingSetpointHandler ()"
}