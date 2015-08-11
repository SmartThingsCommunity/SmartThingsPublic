/**
 *  Initial State Event Streamer
 *
 *  Copyright 2015 David Sulpy
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
    name: "Initial State Event Streamer",
    namespace: "initialstate.events",
    author: "David Sulpy",
    description: "A SmartThings SmartApp to allow SmartThings events to be viewable inside an Initial State Event Bucket in your https://www.initialstate.com account.",
    category: "SmartThings Labs",
    iconUrl: "https://s3.amazonaws.com/initialstate-web-cdn/IS-wordmark-vertica_small.png",
    iconX2Url: "https://s3.amazonaws.com/initialstate-web-cdn/IS-wordmark-vertical.png",
    iconX3Url: "https://s3.amazonaws.com/initialstate-web-cdn/IS-wordmark-vertical.png",
    oauth: [displayName: "Initial State", displayLink: "https://www.initialstate.com"])

import groovy.json.JsonSlurper

preferences {
	section("Choose which devices to monitor...") {
        //input "accelerometers", "capability.accelerationSensor", title: "Accelerometers", multiple: true, required: false
        input "alarms", "capability.alarm", title: "Alarms", multiple: true, required: false
        //input "batteries", "capability.battery", title: "Batteries", multiple: true, required: false
        //input "beacons", "capability.beacon", title: "Beacons", multiple: true, required: false
        //input "buttons", "capability.button", title: "Buttons", multiple: true, required: false
        //input "cos", "capability.carbonMonoxideDetector", title: "Carbon  Monoxide Detectors", multiple: true, required: false
        //input "colors", "capability.colorControl", title: "Color Controllers", multiple: true, required: false
        input "contacts", "capability.contactSensor", title: "Contact Sensors", multiple: true, required: false
        //input "doorsControllers", "capability.doorControl", title: "Door Controllers", multiple: true, required: false
        //input "energyMeters", "capability.energyMeter", title: "Energy Meters", multiple: true, required: false
        //input "illuminances", "capability.illuminanceMeasurement", title: "Illuminance Meters", multiple: true, required: false
        input "locks", "capability.lock", title: "Locks", multiple: true, required: false
        input "motions", "capability.motionSensor", title: "Motion Sensors", multiple: true, required: false
        //input "musicPlayers", "capability.musicPlayer", title: "Music Players", multiple: true, required: false
        //input "powerMeters", "capability.powerMeter", title: "Power Meters", multiple: true, required: false
        input "presences", "capability.presenceSensor", title: "Presence Sensors", multiple: true, required: false
        input "humidities", "capability.relativeHumidityMeasurement", title: "Humidity Meters", multiple: true, required: false
        //input "relaySwitches", "capability.relaySwitch", title: "Relay Switches", multiple: true, required: false
        //input "sleepSensors", "capability.sleepSensor", title: "Sleep Sensors", multiple: true, required: false
        //input "smokeDetectors", "capability.smokeDetector", title: "Smoke Detectors", multiple: true, required: false
        //input "peds", "capability.stepSensor", title: "Pedometers", multiple: true, required: false
        input "switches", "capability.switch", title: "Switches", multiple: true, required: false
        input "switchLevels", "capability.switchLevel", title: "Switch Levels", multiple: true, required: false
        input "temperatures", "capability.temperatureMeasurement", title: "Temperature Sensors", multiple: true, required: false
        input "thermostats", "capability.thermostat", title: "Thermostats", multiple: true, required: false
        //input "valves", "capability.valve", title: "Valves", multiple: true, required: false
        input "waterSensors", "capability.waterSensor", title: "Water Sensors", multiple: true, required: false
    }
}

mappings {
	path("/access_key") {
		action: [
			GET: "getAccessKey",
			PUT: "setAccessKey"
		]
	}
	path("/bucket") {
		action: [
			GET: "getBucketKey",
			PUT: "setBucketKey"
		]
	}
}

def subscribeToEvents() {
	/*if (accelerometers != null) {
		subscribe(accelerometers, "acceleration", genericHandler)
	}*/
	if (alarms != null) {
		subscribe(alarms, "alarm", genericHandler)
	}
	/*if (batteries != null) {
		subscribe(batteries, "battery", genericHandler)
	}*/
	/*if (beacons != null) {
		subscribe(beacons, "presence", genericHandler)
	}*/
	/*
	if (buttons != null) {
		subscribe(buttons, "button", genericHandler)
	}*/
	/*if (cos != null) {
		subscribe(cos, "carbonMonoxide", genericHandler)
	}*/
	/*if (colors != null) {
		subscribe(colors, "hue", genericHandler)
		subscribe(colors, "saturation", genericHandler)
		subscribe(colors, "color", genericHandler)
	}*/
	if (contacts != null) {
		subscribe(contacts, "contact", genericHandler)
	}
	/*if (doorsControllers != null) {
		subscribe(doorsControllers, "door", genericHandler)
	}*/
	/*if (energyMeters != null) {
		subscribe(energyMeters, "energy", genericHandler)
	}*/
	/*if (illuminances != null) {
		subscribe(illuminances, "illuminance", genericHandler)
	}*/
	if (locks != null) {
		subscribe(locks, "lock", genericHandler)
	}
	if (motions != null) {
		subscribe(motions, "motion", genericHandler)
	}
	/*if (musicPlayers != null) {
		subscribe(musicPlayers, "status", genericHandler)
		subscribe(musicPlayers, "level", genericHandler)
		subscribe(musicPlayers, "trackDescription", genericHandler)
		subscribe(musicPlayers, "trackData", genericHandler)
		subscribe(musicPlayers, "mute", genericHandler)
	}*/
	/*if (powerMeters != null) {
		subscribe(powerMeters, "power", genericHandler)
	}*/
	if (presences != null) {
		subscribe(presences, "presence", genericHandler)
	}
	if (humidities != null) {
		subscribe(humidities, "humidity", genericHandler)
	}
	/*if (relaySwitches != null) {
		subscribe(relaySwitches, "switch", genericHandler)
	}*/
	/*if (sleepSensors != null) {
		subscribe(sleepSensors, "sleeping", genericHandler)
	}*/
	/*if (smokeDetectors != null) {
		subscribe(smokeDetectors, "smoke", genericHandler)
	}*/
	/*if (peds != null) {
		subscribe(peds, "steps", genericHandler)
		subscribe(peds, "goal", genericHandler)
	}*/
	if (switches != null) {
		subscribe(switches, "switch", genericHandler)
	}
	if (switchLevels != null) {
		subscribe(switchLevels, "level", genericHandler)
	}
	if (temperatures != null) {
		subscribe(temperatures, "temperature", genericHandler)
	}
	if (thermostats != null) {
		subscribe(thermostats, "temperature", genericHandler)
		subscribe(thermostats, "heatingSetpoint", genericHandler)
		subscribe(thermostats, "coolingSetpoint", genericHandler)
		subscribe(thermostats, "thermostatSetpoint", genericHandler)
		subscribe(thermostats, "thermostatMode", genericHandler)
		subscribe(thermostats, "thermostatFanMode", genericHandler)
		subscribe(thermostats, "thermostatOperatingState", genericHandler)
	}
	/*if (valves != null) {
		subscribe(valves, "contact", genericHandler)
	}*/
	if (waterSensors != null) {
		subscribe(waterSensors, "water", genericHandler)
	}
}

def getAccessKey() {
	log.trace "get access key"
	if (state.accessKey == null) {
		httpError(404, "Access Key Not Found")
	} else {
		[
			accessKey: state.accessKey
		]
	}
}

def getBucketKey() {
	log.trace "get bucket key"
	if (state.bucketKey == null) {
		httpError(404, "Bucket key Not Found")
	} else {
		[
			bucketKey: state.bucketKey,
			bucketName: state.bucketName
		]
	}
}

def setBucketKey() {
	log.trace "set bucket key"
	def newBucketKey = request.JSON?.bucketKey
	def newBucketName = request.JSON?.bucketName

	log.debug "bucket name: $newBucketName"
	log.debug "bucket key: $newBucketKey"

	if (newBucketKey && (newBucketKey != state.bucketKey || newBucketName != state.bucketName)) {
		state.bucketKey = "$newBucketKey"
		state.bucketName = "$newBucketName"
		state.isBucketCreated = false
	}
}

def setAccessKey() {
	log.trace "set access key"
	def newAccessKey = request.JSON?.accessKey

	if (newAccessKey && newAccessKey != state.accessKey) {
		state.accessKey = "$newAccessKey"
		state.isBucketCreated = false
	}
}

def installed() {

	subscribeToEvents()

	state.isBucketCreated = false
}

def updated() {
	unsubscribe()

	if (state.bucketKey != null && state.accessKey != null) {
		state.isBucketCreated = false
	}
	
	subscribeToEvents()
}

def createBucket() {

	if (!state.bucketName) {
    	state.bucketName = state.bucketKey
    }
	def bucketName = "${state.bucketName}"
	def bucketKey = "${state.bucketKey}"
	def accessKey = "${state.accessKey}"

	def bucketCreateBody = new JsonSlurper().parseText("{\"bucketKey\": \"$bucketKey\", \"bucketName\": \"$bucketName\"}")

	def bucketCreatePost = [
		uri: 'https://groker.initialstate.com/api/buckets',
		headers: [
			"Content-Type": "application/json",
			"X-IS-AccessKey": accessKey
		],
		body: bucketCreateBody
	]

	log.debug bucketCreatePost

	httpPostJson(bucketCreatePost) {
		log.debug "bucket posted"
		state.isBucketCreated = true
	}
}

def genericHandler(evt) {
	log.trace "$evt.displayName($evt.name:$evt.unit) $evt.value"

	def key = "$evt.displayName($evt.name)"
	if (evt.unit != null) {
		key = "$evt.displayName(${evt.name}_$evt.unit)"
	}
	def value = "$evt.value"

	eventHandler(key, value)
}

def eventHandler(name, value) {

	if (state.accessKey == null || state.bucketKey == null) {
		return
	}

	if (!state.isBucketCreated) {
		createBucket()
	}

	def eventBody = new JsonSlurper().parseText("[{\"key\": \"$name\", \"value\": \"$value\"}]")
	def eventPost = [
		uri: 'https://groker.initialstate.com/api/events',
		headers: [
			"Content-Type": "application/json",
			"X-IS-BucketKey": "${state.bucketKey}",
			"X-IS-AccessKey": "${state.accessKey}"
		],
		body: eventBody
	]

	log.debug eventPost

	httpPostJson(eventPost) {
		log.debug "event data posted"
	}
}