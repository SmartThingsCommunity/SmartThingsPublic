/**
 *  Zooz 4-in-1
 *
 *  Copyright 2017 Dan Welch
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
	definition (name: "Zooz 4-in-1", namespace: "Danlwelch", author: "Dan Welch") {
		capability "Illuminance Measurement"
		capability "Light"
		capability "Light"
		capability "Motion Sensor"
		capability "Temperature Measurement"

		attribute "Humidity", "string"
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
	// TODO: handle 'illuminance' attribute
	// TODO: handle 'switch' attribute
	// TODO: handle 'switch' attribute
	// TODO: handle 'motion' attribute
	// TODO: handle 'temperature' attribute
	// TODO: handle 'Humidity' attribute

}

// handle commands
def off() {
	log.debug "Executing 'off'"
	// TODO: handle 'off' command
}

def on() {
	log.debug "Executing 'on'"
	// TODO: handle 'on' command
}

def off() {
	log.debug "Executing 'off'"
	// TODO: handle 'off' command
}

def on() {
	log.debug "Executing 'on'"
	// TODO: handle 'on' command
}