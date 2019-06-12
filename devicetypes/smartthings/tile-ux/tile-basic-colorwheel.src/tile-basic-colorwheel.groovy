/**
 *  Copyright 2016 SmartThings, Inc.
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
	definition (
		name: "colorWheelDeviceTile",
		namespace: "smartthings/tile-ux",
		author: "SmartThings") {

		capability "Color Control"
	}

	tiles(scale: 2) {
		valueTile("currentColor", "device.color") {
			state "color", label: '${currentValue}', defaultState: true
		}

		controlTile("rgbSelector", "device.color", "color", height: 6, width: 6, inactiveLabel: false) {
			state "color", action: "color control.setColor"
		}

		main("currentColor")
		details([
			"rgbSelector"
		])
	}
}

// parse events into attributes
def parse(String description) {
	log.debug "Parsing '${description}'"
}

def setColor(value) {
	log.debug "setting color: $value"
    if (value.hex) { sendEvent(name: "color", value: value.hex) }
    if (value.hue) { sendEvent(name: "hue", value: value.hue) }
    if (value.saturation) { sendEvent(name: "saturation", value: value.saturation) }
}

def setSaturation(percent) {
	log.debug "Executing 'setSaturation'"
	sendEvent(name: "saturation", value: percent)
}

def setHue(percent) {
	log.debug "Executing 'setHue'"
	sendEvent(name: "hue", value: percent)
}
