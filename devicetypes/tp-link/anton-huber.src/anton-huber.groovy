/**
 *  Anton Huber
 *
 *  Copyright 2019 Anton Huber
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
	definition (name: "Anton Huber", namespace: "TP Link", author: "Anton Huber") {
		capability "Actuator"
		capability "Air Conditioner Fan Mode"
		capability "Astronomical Data"
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
	// TODO: handle 'fanMode' attribute
	// TODO: handle 'sunrise' attribute
	// TODO: handle 'sunset' attribute
	// TODO: handle 'sunriseTime' attribute
	// TODO: handle 'sunsetTime' attribute

}

// handle commands
def setFanMode() {
	log.debug "Executing 'setFanMode'"
	// TODO: handle 'setFanMode' command
}

def sendEvent(sunrise)() {
	log.debug "Executing 'sendEvent(sunrise)'"
	// TODO: handle 'sendEvent(sunrise)' command
}

def sendEvent(sunset)() {
	log.debug "Executing 'sendEvent(sunset)'"
	// TODO: handle 'sendEvent(sunset)' command
}

def sendEvent(sunriseTime)() {
	log.debug "Executing 'sendEvent(sunriseTime)'"
	// TODO: handle 'sendEvent(sunriseTime)' command
}

def sendEvent(sunsetTime)() {
	log.debug "Executing 'sendEvent(sunsetTime)'"
	// TODO: handle 'sendEvent(sunsetTime)' command
}