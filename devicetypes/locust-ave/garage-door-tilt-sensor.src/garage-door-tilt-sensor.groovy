/**
 *  Garage Door Tilt Sensor
 *
 *  Copyright 2015 Lucas Switzer
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
	definition (name: "Garage Door Tilt Sensor", namespace: "Locust Ave", author: "Lucas Switzer") {
		capability "Acceleration Sensor"
		capability "Battery"
		capability "Contact Sensor"
		capability "Motion Sensor"
		capability "Sensor"
		capability "Signal Strength"
		capability "Three Axis"
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
	// TODO: handle 'acceleration' attribute
	// TODO: handle 'battery' attribute
	// TODO: handle 'contact' attribute
	// TODO: handle 'motion' attribute
	// TODO: handle 'lqi' attribute
	// TODO: handle 'rssi' attribute
	// TODO: handle 'threeAxis' attribute

}

