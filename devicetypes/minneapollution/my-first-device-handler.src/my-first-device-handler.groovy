/**
 *  My First Device Handler
 *
 *  Copyright 2019 Chad Henderson
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
	definition (name: "My First Device Handler", namespace: "Minneapollution", author: "Chad Henderson", cstHandler: true) {
		capability "Button"
		capability "Color"
		capability "Color Control"
		capability "Color Mode"
		capability "Color Temperature"
		capability "Stateless Power Button"
		capability "Stateless Power Toggle Button"
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
	// TODO: handle 'button' attribute
	// TODO: handle 'numberOfButtons' attribute
	// TODO: handle 'supportedButtonValues' attribute
	// TODO: handle 'colorValue' attribute
	// TODO: handle 'hue' attribute
	// TODO: handle 'saturation' attribute
	// TODO: handle 'color' attribute
	// TODO: handle 'colorMode' attribute
	// TODO: handle 'colorTemperature' attribute
	// TODO: handle 'availablePowerButtons' attribute
	// TODO: handle 'availablePowerToggleButtons' attribute

}

// handle commands
def setColorValue() {
	log.debug "Executing 'setColorValue'"
	// TODO: handle 'setColorValue' command
}

def setHue() {
	log.debug "Executing 'setHue'"
	// TODO: handle 'setHue' command
}

def setSaturation() {
	log.debug "Executing 'setSaturation'"
	// TODO: handle 'setSaturation' command
}

def setColor() {
	log.debug "Executing 'setColor'"
	// TODO: handle 'setColor' command
}

def setColorTemperature() {
	log.debug "Executing 'setColorTemperature'"
	// TODO: handle 'setColorTemperature' command
}

def setButton() {
	log.debug "Executing 'setButton'"
	// TODO: handle 'setButton' command
}

def setButton() {
	log.debug "Executing 'setButton'"
	// TODO: handle 'setButton' command
}