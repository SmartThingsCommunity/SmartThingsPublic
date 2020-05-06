/**
 *  aaron
 *
 *  Copyright 2019 richer liao
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
	definition (name: "aaron", namespace: "GLEDOPTO", author: "richer liao", cstHandler: true) {
		capability "Color Control"
		capability "Color"
		capability "Color Mode"

		fingerprint profileId: "C05E", deviceId: "0x0210", manufacturer: "GLEDOPTO", model: "GL-C-008", deviceJoinName: "GLEDOPTO RGBCCT Controller"
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
	// TODO: handle 'hue' attribute
	// TODO: handle 'saturation' attribute
	// TODO: handle 'color' attribute
	// TODO: handle 'colorValue' attribute
	// TODO: handle 'colorMode' attribute

}

// handle commands
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

def setColorValue() {
	log.debug "Executing 'setColorValue'"
	// TODO: handle 'setColorValue' command
}