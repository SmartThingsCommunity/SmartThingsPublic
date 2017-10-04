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
		name: "valueDeviceTile",
		namespace: "smartthings/tile-ux",
		author: "SmartThings") {

		capability "Sensor"
	}

	tiles(scale: 2) {
		valueTile("text", "device.text", width: 2, height: 2) {
			state "val", label:'${currentValue}', defaultState: true
		}

		valueTile("longText", "device.longText", width: 2, height: 2) {
			state "val", label:'${currentValue}', defaultState: true
		}

		valueTile("integer", "device.integer", width: 2, height: 2) {
			state "val", label:'${currentValue}', defaultState: true
		}

		valueTile("integerFloat", "device.integerFloat", width: 2, height: 2) {
			state "val", label:'${currentValue}', defaultState: true
		}

		valueTile("pi", "device.pi", width: 2, height: 2) {
			state "val", label:'${currentValue}', defaultState: true
		}

		valueTile("floatAsText", "device.floatAsText", width: 2, height: 2) {
			state "val", label:'${currentValue}', defaultState: true
		}

		valueTile("bgColor", "device.integer", width: 2, height: 2) {
			state "val", label:'${currentValue}', backgroundColor: "#e86d13", defaultState: true
		}

		valueTile("bgColorRange", "device.integer", width: 2, height: 2) {
			state "val", label:'${currentValue}', defaultState: true, backgroundColors: [
				[value: 10, color: "#ff0000"],
				[value: 90, color: "#0000ff"]
			]
		}

		valueTile("bgColorRangeSingleItem", "device.integer", width: 2, height: 2) {
			state "val", label:'${currentValue}', defaultState: true, backgroundColors: [
				[value: 10, color: "#333333"]
			]
		}

		valueTile("bgColorRangeConflict", "device.integer", width: 2, height: 2) {
			state "valWithConflict", label:'${currentValue}', defaultState: true, backgroundColors: [
				[value: 10, color: "#990000"],
				[value: 10, color: "#000099"]
			]
		}

		valueTile("noValue", "device.nada", width: 4, height: 2) {
			state "noval", label:'${currentValue}', defaultState: true
		}

		valueTile("multiLine", "device.multiLine", width: 3, height: 2) {
			state "val", label: '${currentValue}', defaultState: true
		}

		valueTile("multiLineWithIcon", "device.multiLine", width: 3, height: 2) {
			state "val", label: '${currentValue}', icon: "st.switches.switch.off", defaultState: true
		}

		main("text")
		details([
			"text", "longText", "integer",
            "integerFloat", "pi", "floatAsText",
            "bgColor", "bgColorRange", "bgColorRangeSingleItem",
            "bgColorRangeConflict", "noValue",
            "multiLine", "multiLineWithIcon"
		])
	}
}

def installed() {
	sendEvent(name: "text", value: "Test")
	sendEvent(name: "longText", value: "The Longer The Text, The Better The Test")
	sendEvent(name: "integer", value: 47)
	sendEvent(name: "integerFloat", value: 47.0)
	sendEvent(name: "pi", value: 3.14159)
	sendEvent(name: "floatAsText", value: "3.14159")
	sendEvent(name: "multiLine", value: "Line 1\nLine 2\nLine 3")
}

def parse(String description) {
}
