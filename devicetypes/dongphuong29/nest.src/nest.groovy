/**
 *  Nest
 *
 *  Copyright 2017 Phuong Vo
 *
 */
metadata {
	definition (name: "Nest", namespace: "dongphuong29", author: "Phuong Vo") {
		capability "Polling"
		capability "Presence Sensor"
		capability "Relative Humidity Measurement"
		capability "Sensor"
		capability "Temperature Measurement"
		capability "Thermostat"

		attribute "temperatureUnit", "string"

		command "away"
		command "present"
		command "setPresence"
		command "heatingSetpointUp"
		command "heatingSetpointDown"
		command "coolingSetpointUp"
		command "coolingSetpointDown"
		command "setFahrenheit"
		command "setCelsius"
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
	// TODO: handle 'presence' attribute
	// TODO: handle 'humidity' attribute
	// TODO: handle 'temperature' attribute
	// TODO: handle 'temperature' attribute
	// TODO: handle 'heatingSetpoint' attribute
	// TODO: handle 'coolingSetpoint' attribute
	// TODO: handle 'thermostatSetpoint' attribute
	// TODO: handle 'thermostatMode' attribute
	// TODO: handle 'thermostatFanMode' attribute
	// TODO: handle 'thermostatOperatingState' attribute
	// TODO: handle 'schedule' attribute
	// TODO: handle 'coolingSetpointRange' attribute
	// TODO: handle 'heatingSetpointRange' attribute
	// TODO: handle 'supportedThermostatFanModes' attribute
	// TODO: handle 'supportedThermostatModes' attribute
	// TODO: handle 'thermostatSetpointRange' attribute
	// TODO: handle 'temperatureUnit' attribute

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

def heatingSetpointUp() {
	log.debug "Executing 'heatingSetpointUp'"
	// TODO: handle 'heatingSetpointUp' command
}

def heatingSetpointDown() {
	log.debug "Executing 'heatingSetpointDown'"
	// TODO: handle 'heatingSetpointDown' command
}

def coolingSetpointUp() {
	log.debug "Executing 'coolingSetpointUp'"
	// TODO: handle 'coolingSetpointUp' command
}

def coolingSetpointDown() {
	log.debug "Executing 'coolingSetpointDown'"
	// TODO: handle 'coolingSetpointDown' command
}

def setFahrenheit() {
	log.debug "Executing 'setFahrenheit'"
	// TODO: handle 'setFahrenheit' command
}

def setCelsius() {
	log.debug "Executing 'setCelsius'"
	// TODO: handle 'setCelsius' command
}