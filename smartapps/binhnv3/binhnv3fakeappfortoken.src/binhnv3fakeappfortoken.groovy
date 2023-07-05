/**
 *  binhnv3FakeAppForToken
 *
 *  Copyright 2021 Binh Nguyen
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
    name: "binhnv3FakeAppForToken",
    namespace: "binhnv3",
    author: "Binh Nguyen",
    description: "Some trick",
    category: "Safety & Security",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
	section("Chose one") {
		input "button", "capability.button", title: "Button Pushed", required: false, multiple: true //tw
		input "motion", "capability.motionSensor", title: "Motion Here", required: false, multiple: true
		input "contact", "capability.contactSensor", title: "Contact Opens", required: false, multiple: true
		input "contactClosed", "capability.contactSensor", title: "Contact Closes", required: false, multiple: true
		input "acceleration", "capability.accelerationSensor", title: "Acceleration Detected", required: false, multiple: true
		input "mySwitch", "capability.switch", title: "Switch Turned On", required: false, multiple: true
		input "mySwitchOff", "capability.switch", title: "Switch Turned Off", required: false, multiple: true
		input "arrivalPresence", "capability.presenceSensor", title: "Arrival Of", required: false, multiple: true
		input "departurePresence", "capability.presenceSensor", title: "Departure Of", required: false, multiple: true
		input "smoke", "capability.smokeDetector", title: "Smoke Detected", required: false, multiple: true
		input "water", "capability.waterSensor", title: "Water Sensor Wet", required: false, multiple: true
	}
}

def installed() {
	log.debug "Installed with settings: ${settings}"

	initialize()
}

def updated() {
	log.debug "Updated with settings: ${settings}"

	unsubscribe()
	initialize()
}

def initialize() {
	// TODO: subscribe to attributes, devices, locations, etc.
}

// TODO: implement event handlers