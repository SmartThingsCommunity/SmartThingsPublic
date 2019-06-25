/**
 *  vv_Z_WAVE_dd
 *
 *  Copyright 2019 VIDYA VASUDEVAN
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 */
metadata {
	definition (name: "vv_Z_WAVE_dd", namespace: "fdsvdv443", author: "VIDYA VASUDEVAN", cstHandler: true) {
		capability "Fan Speed"
		capability "Thermostat Cooling Setpoint"
		capability "Thermostat Fan Mode"
		capability "Audio Volume"

		fingerprint mfr: "5678", prod: "4567", model: "1234", deviceJoinName: "fvdfrfref43"
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
	// TODO: handle 'fanSpeed' attribute
	// TODO: handle 'coolingSetpoint' attribute
	// TODO: handle 'thermostatFanMode' attribute
	// TODO: handle 'supportedThermostatFanModes' attribute
	// TODO: handle 'volume' attribute

}

// handle commands
def setFanSpeed() {
	log.debug "Executing 'setFanSpeed'"
	// TODO: handle 'setFanSpeed' command
}

def setCoolingSetpoint() {
	log.debug "Executing 'setCoolingSetpoint'"
	// TODO: handle 'setCoolingSetpoint' command
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

def setVolume() {
	log.debug "Executing 'setVolume'"
	// TODO: handle 'setVolume' command
}

def volumeUp() {
	log.debug "Executing 'volumeUp'"
	// TODO: handle 'volumeUp' command
}

def volumeDown() {
	log.debug "Executing 'volumeDown'"
	// TODO: handle 'volumeDown' command
}