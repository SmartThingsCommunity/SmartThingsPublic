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
 *  SmartThings data is sent from this SmartApp to Initial State. This is event data only for
 *  devices for which the user has authorized. Likewise, Initial State's services call this
 *  SmartApp on the user's behalf to configure Initial State specific parameters. The ToS and
 *  Privacy Policy for Initial State can be found here: https://www.initialstate.com/terms
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
        input "accelerometers", "capability.accelerationSensor", title: "Accelerometers", multiple: true, required: false
        input "alarms", "capability.alarm", title: "Alarms", multiple: true, required: false
        input "batteries", "capability.battery", title: "Batteries", multiple: true, required: false
        input "beacons", "capability.beacon", title: "Beacons", multiple: true, required: false
        input "cos", "capability.carbonMonoxideDetector", title: "Carbon  Monoxide Detectors", multiple: true, required: false
        input "colors", "capability.colorControl", title: "Color Controllers", multiple: true, required: false
        input "contacts", "capability.contactSensor", title: "Contact Sensors", multiple: true, required: false
        input "doorsControllers", "capability.doorControl", title: "Door Controllers", multiple: true, required: false
        input "energyMeters", "capability.energyMeter", title: "Energy Meters", multiple: true, required: false
        input "illuminances", "capability.illuminanceMeasurement", title: "Illuminance Meters", multiple: true, required: false
        input "locks", "capability.lock", title: "Locks", multiple: true, required: false
        input "motions", "capability.motionSensor", title: "Motion Sensors", multiple: true, required: false
        input "musicPlayers", "capability.musicPlayer", title: "Music Players", multiple: true, required: false
        input "powerMeters", "capability.powerMeter", title: "Power Meters", multiple: true, required: false
        input "presences", "capability.presenceSensor", title: "Presence Sensors", multiple: true, required: false
        input "humidities", "capability.relativeHumidityMeasurement", title: "Humidity Meters", multiple: true, required: false
        input "relaySwitches", "capability.relaySwitch", title: "Relay Switches", multiple: true, required: false
        input "sleepSensors", "capability.sleepSensor", title: "Sleep Sensors", multiple: true, required: false
        input "smokeDetectors", "capability.smokeDetector", title: "Smoke Detectors", multiple: true, required: false
        input "peds", "capability.stepSensor", title: "Pedometers", multiple: true, required: false
        input "switches", "capability.switch", title: "Switches", multiple: true, required: false
        input "switchLevels", "capability.switchLevel", title: "Switch Levels", multiple: true, required: false
        input "temperatures", "capability.temperatureMeasurement", title: "Temperature Sensors", multiple: true, required: false
        input "thermostats", "capability.thermostat", title: "Thermostats", multiple: true, required: false
        input "valves", "capability.valve", title: "Valves", multiple: true, required: false
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
	if (accelerometers != null) {
		subscribe(accelerometers, "acceleration", genericHandler)
	}
	if (alarms != null) {
		subscribe(alarms, "alarm", genericHandler)
	}
	if (batteries != null) {
		subscribe(batteries, "battery", genericHandler)
	}
	if (beacons != null) {
		subscribe(beacons, "presence", genericHandler)
	}

	if (cos != null) {
		subscribe(cos, "carbonMonoxide", genericHandler)
	}
	if (colors != null) {
		subscribe(colors, "hue", genericHandler)
		subscribe(colors, "saturation", genericHandler)
		subscribe(colors, "color", genericHandler)
	}
	if (contacts != null) {
		subscribe(contacts, "contact", genericHandler)
	}
	if (energyMeters != null) {
		subscribe(energyMeters, "energy", genericHandler)
	}
	if (illuminances != null) {
		subscribe(illuminances, "illuminance", genericHandler)
	}
	if (locks != null) {
		subscribe(locks, "lock", genericHandler)
	}
	if (motions != null) {
		subscribe(motions, "motion", genericHandler)
	}
	if (musicPlayers != null) {
		subscribe(musicPlayers, "status", genericHandler)
		subscribe(musicPlayers, "level", genericHandler)
		subscribe(musicPlayers, "trackDescription", genericHandler)
		subscribe(musicPlayers, "trackData", genericHandler)
		subscribe(musicPlayers, "mute", genericHandler)
	}
	if (powerMeters != null) {
		subscribe(powerMeters, "power", genericHandler)
	}
	if (presences != null) {
		subscribe(presences, "presence", genericHandler)
	}
	if (humidities != null) {
		subscribe(humidities, "humidity", genericHandler)
	}
	if (relaySwitches != null) {
		subscribe(relaySwitches, "switch", genericHandler)
	}
	if (sleepSensors != null) {
		subscribe(sleepSensors, "sleeping", genericHandler)
	}
	if (smokeDetectors != null) {
		subscribe(smokeDetectors, "smoke", genericHandler)
	}
	if (peds != null) {
		subscribe(peds, "steps", genericHandler)
		subscribe(peds, "goal", genericHandler)
	}
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
	if (valves != null) {
		subscribe(valves, "contact", genericHandler)
	}
	if (waterSensors != null) {
		subscribe(waterSensors, "water", genericHandler)
	}
}

def getAccessKey() {
	log.trace "get access key"
	if (atomicState.accessKey == null) {
		httpError(404, "Access Key Not Found")
	} else {
		[
			accessKey: atomicState.accessKey
		]
	}
}

def getBucketKey() {
	log.trace "get bucket key"
	if (atomicState.bucketKey == null) {
		httpError(404, "Bucket key Not Found")
	} else {
		[
			bucketKey: atomicState.bucketKey,
			bucketName: atomicState.bucketName
		]
	}
}

def setBucketKey() {
	log.trace "set bucket key"
	def newBucketKey = request.JSON?.bucketKey
	def newBucketName = request.JSON?.bucketName

	log.debug "bucket name: $newBucketName"
	log.debug "bucket key: $newBucketKey"

	if (newBucketKey && (newBucketKey != atomicState.bucketKey || newBucketName != atomicState.bucketName)) {
		atomicState.bucketKey = "$newBucketKey"
		atomicState.bucketName = "$newBucketName"
		atomicState.isBucketCreated = false
	}

	tryCreateBucket()
}

def setAccessKey() {
	log.trace "set access key"
	def newAccessKey = request.JSON?.accessKey
	def newGrokerSubdomain = request.JSON?.grokerSubdomain

	if (newGrokerSubdomain && newGrokerSubdomain != "" && newGrokerSubdomain != atomicState.grokerSubdomain) {
		atomicState.grokerSubdomain = "$newGrokerSubdomain"
		atomicState.isBucketCreated = false
	}

	if (newAccessKey && newAccessKey != atomicState.accessKey) {
		atomicState.accessKey = "$newAccessKey"
		atomicState.isBucketCreated = false
	}
}

def installed() {
	atomicState.version = "1.0.18"
	subscribeToEvents()

	atomicState.isBucketCreated = false
	atomicState.grokerSubdomain = "groker"
	atomicState.eventBuffer = []

	runEvery15Minutes(flushBuffer)

	log.debug "installed (version $atomicState.version)"
}

def updated() {
	atomicState.version = "1.0.18"
	unsubscribe()

	if (atomicState.bucketKey != null && atomicState.accessKey != null) {
		atomicState.isBucketCreated = false
	}
	if (atomicState.eventBuffer == null) {
		atomicState.eventBuffer = []
	}
	if (atomicState.grokerSubdomain == null || atomicState.grokerSubdomain == "") {
		atomicState.grokerSubdomain = "groker"
	}

	subscribeToEvents()

	log.debug "updated (version $atomicState.version)"
}

def uninstalled() {
	log.debug "uninstalled (version $atomicState.version)"
}

def tryCreateBucket() {

	// can't ship events if there is no grokerSubdomain
	if (atomicState.grokerSubdomain == null || atomicState.grokerSubdomain == "") {
		log.error "streaming url is currently null"
		return
	}

	// if the bucket has already been created, no need to continue
	if (atomicState.isBucketCreated) {
		return
	}

	if (!atomicState.bucketName) {
    	atomicState.bucketName = atomicState.bucketKey
    }
    if (!atomicState.accessKey) {
    	return
    }
	def bucketName = "${atomicState.bucketName}"
	def bucketKey = "${atomicState.bucketKey}"
	def accessKey = "${atomicState.accessKey}"

	def bucketCreateBody = new JsonSlurper().parseText("{\"bucketKey\": \"$bucketKey\", \"bucketName\": \"$bucketName\"}")

	def bucketCreatePost = [
		uri: "https://${atomicState.grokerSubdomain}.initialstate.com/api/buckets",
		headers: [
			"Content-Type": "application/json",
			"X-IS-AccessKey": accessKey
		],
		body: bucketCreateBody
	]

	log.debug bucketCreatePost

	try {
		// Create a bucket on Initial State so the data has a logical grouping
		httpPostJson(bucketCreatePost) { resp ->
			log.debug "bucket posted"
			if (resp.status >= 400) {
				log.error "bucket not created successfully"
			} else {
				atomicState.isBucketCreated = true
			}
		}
	} catch (e) {
		log.error "bucket creation error: $e"
	}

}

def genericHandler(evt) {
	log.trace "$evt.displayName($evt.name:$evt.unit) $evt.value"

	def key = "$evt.displayName($evt.name)"
	if (evt.unit != null) {
		key = "$evt.displayName(${evt.name}_$evt.unit)"
	}
	def value = "$evt.value"

	tryCreateBucket()

	eventHandler(key, value)
}

// This is a handler function for flushing the event buffer
// after a specified amount of time to reduce the load on ST servers
def flushBuffer() {
	def eventBuffer = atomicState.eventBuffer
	log.trace "About to flush the buffer on schedule"
	if (eventBuffer != null && eventBuffer.size() > 0) {
		atomicState.eventBuffer = []
		tryShipEvents(eventBuffer)
	}
}

def eventHandler(name, value) {
	def epoch = now() / 1000
	def eventBuffer = atomicState.eventBuffer ?: []
	eventBuffer << [key: "$name", value: "$value", epoch: "$epoch"]

	if (eventBuffer.size() >= 10) {
		// Clear eventBuffer right away since we've already pulled it off of atomicState to reduce the risk of missing
		// events.  This assumes the grokerSubdomain, accessKey, and bucketKey are set correctly to avoid the eventBuffer
		// from growing unbounded.
		atomicState.eventBuffer = []
		tryShipEvents(eventBuffer)
	} else {
		// Make sure we persist the updated eventBuffer with the new event added back to atomicState
		atomicState.eventBuffer = eventBuffer
	}
	log.debug "Event added to buffer: " + eventBuffer
}

// a helper function for shipping the atomicState.eventBuffer to Initial State
def tryShipEvents(eventBuffer) {

	def grokerSubdomain = atomicState.grokerSubdomain
	// can't ship events if there is no grokerSubdomain
	if (grokerSubdomain == null || grokerSubdomain == "") {
		log.error "streaming url is currently null"
		return
	}
	def accessKey = atomicState.accessKey
	def bucketKey = atomicState.bucketKey
	// can't ship if access key and bucket key are null, so finish trying
	if (accessKey == null || bucketKey == null) {
		return
	}

	def eventPost = [
		uri: "https://${grokerSubdomain}.initialstate.com/api/events",
		headers: [
			"Content-Type": "application/json",
			"X-IS-BucketKey": "${bucketKey}",
			"X-IS-AccessKey": "${accessKey}",
			"Accept-Version": "0.0.2"
		],
		body: eventBuffer
	]

	try {
		// post the events to initial state
		httpPostJson(eventPost) { resp ->
			log.debug "shipped events and got ${resp.status}"
			if (resp.status >= 400) {
				log.error "shipping failed... ${resp.data}"
			}
		}
	} catch (e) {
		log.error "shipping events failed: $e"
	}

}