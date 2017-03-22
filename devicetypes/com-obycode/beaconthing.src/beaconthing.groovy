/**
*  BeaconThing
*
*  Copyright 2015 obycode
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

import groovy.json.JsonSlurper

metadata {
	definition (name: "BeaconThing", namespace: "com.obycode", author: "obycode") {
		capability "Beacon"
		capability "Presence Sensor"
		capability "Sensor"

		attribute "inRange", "json_object"
		attribute "inRangeFriendly", "string"

		command "setPresence", ["string"]
		command "arrived", ["string"]
		command "left", ["string"]
	}

	simulator {
		status "present": "presence: 1"
		status "not present": "presence: 0"
	}

	tiles {
		standardTile("presence", "device.presence", width: 2, height: 2, canChangeBackground: true) {
			state("present", labelIcon:"st.presence.tile.present", backgroundColor:"#00A0DC")
			state("not present", labelIcon:"st.presence.tile.not-present", backgroundColor:"#ffffff")
		}
		valueTile("inRange", "device.inRangeFriendly", inactiveLabel: true, height:1, width:3, decoration: "flat") {
			state "default", label:'${currentValue}', backgroundColor:"#ffffff"
		}
		main "presence"
		details (["presence","inRange"])
	}
}

// parse events into attributes
def parse(String description) {
	log.debug "Parsing '${description}'"
}

def installed() {
	sendEvent(name: "presence", value: "not present")
	def emptyList = []
	def json = new groovy.json.JsonBuilder(emptyList)
	sendEvent(name:"inRange", value:json.toString())
}

def setPresence(status) {
	log.debug "Status is $status"
	sendEvent(name:"presence", value:status)
}

def arrived(id) {
	log.debug "$id has arrived"
	def theList = device.latestValue("inRange")
	def inRangeList = new JsonSlurper().parseText(theList)
	if (inRangeList.contains(id)) {
		return
	}
	inRangeList += id
	def json = new groovy.json.JsonBuilder(inRangeList)
	log.debug "Now in range: ${json.toString()}"
	sendEvent(name:"inRange", value:json.toString())

	// Generate human friendly string for tile
	def friendlyList = "Nearby: " + inRangeList.join(", ")
	sendEvent(name:"inRangeFriendly", value:friendlyList)

	if (inRangeList.size() == 1) {
		setPresence("present")
	}
}

def left(id) {
	log.debug "$id has left"
	def theList = device.latestValue("inRange")
	def inRangeList = new JsonSlurper().parseText(theList)
	inRangeList -= id
	def json = new groovy.json.JsonBuilder(inRangeList)
	log.debug "Now in range: ${json.toString()}"
	sendEvent(name:"inRange", value:json.toString())

	// Generate human friendly string for tile
	def friendlyList = "Nearby: " + inRangeList.join(", ")

	if (inRangeList.empty) {
		setPresence("not present")
		friendlyList = "No one is nearby"
	}

	sendEvent(name:"inRangeFriendly", value:friendlyList)
}
