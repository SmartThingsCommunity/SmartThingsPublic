/**
 *  Copyright 2015 SmartThings
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
	definition (name: "Motion Detector", namespace: "smartthings", author: "SmartThings", mnmn: "SmartThings", vid: "generic-motion-9") {
		capability "Actuator"
		capability "Health Check"
		capability "Motion Sensor"
		capability "Sensor"

		fingerprint profileId: "0104", deviceId: "0402", inClusters: "0000,0001,0003,0009,0500", deviceJoinName: "Motion Sensor"
		fingerprint manufacturer: "Aurora", model: "MotionSensor51AU", deviceJoinName: "Aurora Motion Sensor" //raw description 22 0104 0107 00 03 0000 0003 0406 00 //Aurora Smart PIR Sensor
	}

	// simulator metadata
	simulator {
		status "active": "zone report :: type: 19 value: 0031"
		status "inactive": "zone report :: type: 19 value: 0030"
	}

	// UI tile definitions
	tiles(scale: 2) {
		multiAttributeTile(name:"motion", type: "generic", width: 6, height: 4){
			tileAttribute("device.motion", key: "PRIMARY_CONTROL") {
				attributeState("active", label:'motion', icon:"st.motion.motion.active", backgroundColor:"#00A0DC")
				attributeState("inactive", label:'no motion', icon:"st.motion.motion.inactive", backgroundColor:"#CCCCCC")
			}
		}
		main "motion"
		details "motion"
	}
}

def installed() {
	initialize()
	if(isAuroraMotionSensor51AU()) {
		// Aurora Smart PIR Sensor doesn't report when there is no motion during pairing process
		// reports are sent only if there is motion detected, so fake event is needed here
		sendEvent(name: "motion", value: "inactive", displayed: false)
	}
}

def updated() {
	initialize()
}

def initialize() {
	if (isTracked()) {
		// Device-Watch simply pings if no device events received for 12min(checkInterval)
		log.debug "device tracked"
		sendEvent(name: "checkInterval", value: 10 * 60 + 2 * 60, displayed: false, data: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID])
	} else {
		log.debug "device untracked"
		sendEvent(name: "DeviceWatch-Enroll", value: JsonOutput.toJson([protocol: "zigbee", scheme:"untracked"]), displayed: false)
	}
}

// Parse incoming device messages to generate events
def parse(String description) {
	log.debug "$description"
	def name = null
	def value = description
	def descriptionText = null
	if (zigbee.isZoneType19(description)) {
		name = "motion"
		def isActive = zigbee.translateStatusZoneType19(description)
		value = isActive ? "active" : "inactive"
		descriptionText = isActive ? "${device.displayName} detected motion" : "${device.displayName} motion has stopped"
	}

	def result = createEvent(
			name: name,
			value: value,
			descriptionText: descriptionText
	)

	log.debug "Parse returned ${result?.descriptionText}"
	return result
}

def isTracked() {
	return isAuroraMotionSensor51AU()
}

def isAuroraMotionSensor51AU() {
	return device.getDataValue("model") == "MotionSensor51AU"
}