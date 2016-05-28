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
		name: "sliderDeviceTile",
		namespace: "smartthings/tile-ux",
		author: "SmartThings") {

		capability "Switch Level"
		command "setRangedLevel", ["number"]
	}

	tiles(scale: 2) {
		controlTile("tinySlider", "device.level", "slider", height: 2, width: 2, inactiveLabel: false) {
			state "level", action:"switch level.setLevel"
		}

		controlTile("mediumSlider", "device.level", "slider", height: 2, width: 4, inactiveLabel: false) {
			state "level", action:"switch level.setLevel"
		}

		controlTile("largeSlider", "device.level", "slider", decoration: "flat", height: 2, width: 6, inactiveLabel: false) {
			state "level", action:"switch level.setLevel"
		}

		controlTile("rangeSlider", "device.rangedLevel", "slider", height: 2, width: 4, range: "(20..80)") {
			state "level", action:"setRangedLevel"
		}

		valueTile("rangeValue", "device.rangedLevel", height: 2, width: 2) {
			state "range", label:'${currentValue}', defaultState: true
		}

		controlTile("rangeSliderConstrained", "device.rangedLevel", "slider", height: 2, width: 4, range: "(40..60)") {
			state "level", action:"setRangedLevel"
		}

		main("rangeValue")
		details([
			"tinySlider", "mediumSlider",
			"largeSlider",
			"rangeSlider", "rangeValue",
			"rangeSliderConstrained"
		])
	}
}

def installed() {
	sendEvent(name: "level", value: 63)
	sendEvent(name: "rangedLevel", value: 47)
}

def parse(String description) {
}

def setLevel(value) {
	log.debug "setting level to $value"
	sendEvent(name:"level", value:value)
}

def setRangedLevel(value) {
	log.debug "setting ranged level to $value"
	sendEvent(name:"rangedLevel", value:value)
}
