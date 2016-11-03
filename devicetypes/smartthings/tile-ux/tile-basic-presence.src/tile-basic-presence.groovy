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
		name: "presenceDeviceTile",
		namespace: "smartthings/tile-ux",
		author: "SmartThings") {

		capability "Presence Sensor"

		command "arrived"
		command "departed"
	}

	tiles(scale: 2) {
		// You only get a presence tile view when the size is 3x3 otherwise it's a value tile
		standardTile("presence", "device.presence", width: 3, height: 3, canChangeBackground: true) {
			state("present", labelIcon:"st.presence.tile.mobile-present", backgroundColor:"#53a7c0")
			state("not present", labelIcon:"st.presence.tile.mobile-not-present", backgroundColor:"#ebeef2")
		}

		standardTile("notPresentBtn", "device.fake", width: 3, height: 3, decoration: "flat") {
			state("not present", label:'not present', backgroundColor:"#ffffff", action:"departed")
		}

		standardTile("presentBtn", "device.fake", width: 3, height: 3, decoration: "flat") {
			state("present", label:'present', backgroundColor:"#53a7c0", action:"arrived")
		}

		main("presence")
		details([
			"presence", "presenceControl", "notPresentBtn", "presentBtn"
		])
	}
}

def installed() {
	sendEvent(name: "presence", value: "present")
}

def parse(String description) {
}

def arrived() {
	log.trace "Executing 'arrived'"
	sendEvent(name: "presence", value: "present")
}

def departed() {
	log.trace "Executing 'arrived'"
	sendEvent(name: "presence", value: "not present")
}
