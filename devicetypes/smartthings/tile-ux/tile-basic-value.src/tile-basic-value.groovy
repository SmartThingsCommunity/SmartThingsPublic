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
			state "default", label:'${currentValue}'
		}

		valueTile("longText", "device.longText", width: 2, height: 2) {
			state "default", label:'${currentValue}'
		}

		valueTile("integer", "device.integer", width: 2, height: 2) {
			state "default", label:'${currentValue}'
		}

		valueTile("integerFloat", "device.integerFloat", width: 2, height: 2) {
			state "default", label:'${currentValue}'
		}

		valueTile("pi", "device.pi", width: 2, height: 2) {
			state "default", label:'${currentValue}'
		}

		valueTile("floatAsText", "device.floatAsText", width: 2, height: 2) {
			state "default", label:'${currentValue}'
		}

		valueTile("bgColor", "device.integer", width: 2, height: 2) {
			state "default", label:'${currentValue}', backgroundColor: "#e86d13"
		}

		valueTile("bgColorRange", "device.integer", width: 2, height: 2) {
			state "default", label:'${currentValue}', backgroundColors: [
				[value: 10, color: "#ff0000"],
				[value: 90, color: "#0000ff"]
			]
		}

		valueTile("bgColorRangeSingleItem", "device.integer", width: 2, height: 2) {
			state "default", label:'${currentValue}', backgroundColors: [
				[value: 10, color: "#333333"]
			]
		}

		valueTile("bgColorRangeConflict", "device.integer", width: 2, height: 2) {
			state "default", label:'${currentValue}', backgroundColors: [
				[value: 10, color: "#990000"],
				[value: 10, color: "#000099"]
			]
		}

		valueTile("noValue", "device.nada", width: 2, height: 2) {
			state "default", label:'${currentValue}'
		}

		main("text")
		details([
			"text", "longText", "integer", 
            "integerFloat", "pi", "floatAsText",
            "bgColor", "bgColorRange", "bgColorRangeSingleItem",
            "bgColorRangeConflict", "noValue"
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
}

def parse(String description) {
}
