/**
 *  Nest-ST
 *
 *  Copyright 2015 Matt Thomas
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
	definition (name: "Nest-ST", namespace: "travelbum", author: "Matt Thomas") {
		capability "Polling"
		capability "Relative Humidity Measurement"
		capability "Thermostat"

		attribute "presence", "string"

		command "away"
		command "present"
		command "setPresence"
	}

	simulator {
		// TODO: define status and reply messages here
	}

	tiles {
		// TODO: define your main and details tiles here
	}
}

// parse events into attributes
def parse(String description) {
	log.debug "Parsing '${description}'"
	// TODO: handle 'humidity' attribute
	// TODO: handle 'temperature' attribute
	// TODO: handle 'heatingSetpoint' attribute
	// TODO: handle 'coolingSetpoint' attribute
	// TODO: handle 'thermostatSetpoint' attribute
	// TODO: handle 'thermostatMode' attribute
	// TODO: handle 'thermostatFanMode' attribute
	// TODO: handle 'thermostatOperatingState' attribute
	// TODO: handle 'schedule' attribute
	// TODO: handle 'presence' attribute

}

// handle commands
def poll() {
	log.debug "Executing 'poll'"
	// TODO: handle 'poll' command
}

def setHeatingSetpoint() {
	log.debug "Executing 'setHeatingSetpoint'"
	// TODO: handle 'setHeatingSetpoint' command
}

def setCoolingSetpoint() {
	log.debug "Executing 'setCoolingSetpoint'"
	// TODO: handle 'setCoolingSetpoint' command
}

def off() {
	log.debug "Executing 'off'"
	// TODO: handle 'off' command
}

def heat() {
	log.debug "Executing 'heat'"
	// TODO: handle 'heat' command
}

def emergencyHeat() {
	log.debug "Executing 'emergencyHeat'"
	// TODO: handle 'emergencyHeat' command
}

def cool() {
	log.debug "Executing 'cool'"
	// TODO: handle 'cool' command
}

def setThermostatMode() {
	log.debug "Executing 'setThermostatMode'"
	// TODO: handle 'setThermostatMode' command
}

def fanOn() {
	log.debug "Executing 'fanOn'"
	// TODO: handle 'fanOn' command
}

def fanAuto() {
	log.debug "Executing 'fanAuto'"
	// TODO: handle 'fanAuto' command
}

def fanCirculate() {
	log.debug "Executing 'fanCirculate'"
	// TODO: handle 'fanCirculate' command
}

def setThermostatFanMode() {
	log.debug "Executing 'setThermostatFanMode'"
	// TODO: handle 'setThermostatFanMode' command
}

def auto() {
	log.debug "Executing 'auto'"
	// TODO: handle 'auto' command
}

def setSchedule() {
	log.debug "Executing 'setSchedule'"
	// TODO: handle 'setSchedule' command
}

def away() {
	log.debug "Executing 'away'"
	// TODO: handle 'away' command
}

def present() {
	log.debug "Executing 'present'"
	// TODO: handle 'present' command
}

def setPresence() {
	log.debug "Executing 'setPresence'"
	// TODO: handle 'setPresence' command
}


