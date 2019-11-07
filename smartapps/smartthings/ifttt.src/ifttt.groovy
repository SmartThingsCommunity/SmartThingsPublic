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
 *  IFTTT API Access Application
 *
 *  Author: SmartThings
 *
 *  ---------------------+----------------+--------------------------+------------------------------------
 *  Device Type          | Attribute Name | Commands                 | Attribute Values
 *  ---------------------+----------------+--------------------------+------------------------------------
 *  switches             | switch         | on, off                  | on, off
 *  motionSensors        | motion         |                          | active, inactive
 *  contactSensors       | contact        |                          | open, closed
 *  presenceSensors      | presence       |                          | present, 'not present'
 *  temperatureSensors   | temperature    |                          | <numeric, F or C according to unit>
 *  accelerationSensors  | acceleration   |                          | active, inactive
 *  waterSensors         | water          |                          | wet, dry
 *  lightSensors         | illuminance    |                          | <numeric, lux>
 *  humiditySensors      | humidity       |                          | <numeric, percent>
 *  alarms               | alarm          | strobe, siren, both, off | strobe, siren, both, off
 *  locks                | lock           | lock, unlock             | locked, unlocked
 *  ---------------------+----------------+--------------------------+------------------------------------
 */

definition(
    name: "IFTTT",
    namespace: "smartthings",
    author: "SmartThings",
    description: "Put the internet to work for you.",
    category: "SmartThings Internal",
    iconUrl: "https://ifttt.com/images/channels/ifttt.png",
    iconX2Url: "https://ifttt.com/images/channels/ifttt_med.png",
    oauth: [displayName: "IFTTT", displayLink: "https://ifttt.com"],
    usesThirdPartyAuthentication: true,
    pausable: false
)

preferences {
	section("Allow IFTTT to control these things...") {
		input "switches", "capability.switch", title: "Which Switches?", multiple: true, required: false
		input "motionSensors", "capability.motionSensor", title: "Which Motion Sensors?", multiple: true, required: false
		input "contactSensors", "capability.contactSensor", title: "Which Contact Sensors?", multiple: true, required: false
		input "presenceSensors", "capability.presenceSensor", title: "Which Presence Sensors?", multiple: true, required: false
		input "temperatureSensors", "capability.temperatureMeasurement", title: "Which Temperature Sensors?", multiple: true, required: false
		input "accelerationSensors", "capability.accelerationSensor", title: "Which Vibration Sensors?", multiple: true, required: false
		input "waterSensors", "capability.waterSensor", title: "Which Water Sensors?", multiple: true, required: false
		input "lightSensors", "capability.illuminanceMeasurement", title: "Which Light Sensors?", multiple: true, required: false
		input "humiditySensors", "capability.relativeHumidityMeasurement", title: "Which Relative Humidity Sensors?", multiple: true, required: false
		input "alarms", "capability.alarm", title: "Which Sirens?", multiple: true, required: false
		input "locks", "capability.lock", title: "Which Locks?", multiple: true, required: false
	}
}

mappings {

	path("/:deviceType") {
		action: [
			GET: "list"
		]
	}
	path("/:deviceType/states") {
		action: [
			GET: "listStates"
		]
	}
	path("/:deviceType/subscription") {
		action: [
			POST: "addSubscription"
		]
	}
	path("/:deviceType/subscriptions/:id") {
		action: [
			DELETE: "removeSubscription"
		]
	}
	path("/:deviceType/:id") {
		action: [
			GET: "show",
			PUT: "update"
		]
	}
	path("/subscriptions") {
		action: [
			GET: "listSubscriptions"
		]
	}
}

def installed() {
	//log.debug settings
}

def updated() {
	def currentDeviceIds = settings.collect { k, devices -> devices }.flatten().collect { it.id }.unique()
	def subscriptionDevicesToRemove = app.subscriptions*.device.findAll { device ->
		!currentDeviceIds.contains(device.id)
	}
	subscriptionDevicesToRemove.each { device ->
		log.debug "Removing $device.displayName subscription"
		state.remove(device.id)
		unsubscribe(device)
	}
	//log.debug settings
}

def list() {
	//log.debug "[PROD] list, params: ${params}"
	def type = params.deviceType
	settings[type]?.collect{deviceItem(it)} ?: []
}

def listStates() {
	log.debug "[PROD] states, params: ${params}"
	def type = params.deviceType
	def attributeName = attributeFor(type)
	settings[type]?.collect{deviceState(it, it.currentState(attributeName))} ?: []
}

def listSubscriptions() {
	state
}

def update() {
	def type = params.deviceType
	def data = request.JSON
	def devices = settings[type]
	def device = settings[type]?.find { it.id == params.id }
	def command = data.command

	//log.debug "[PROD] update, params: ${params}, request: ${data}, devices: ${devices*.id}"
	
	if (!device) {
		httpError(404, "Device not found")
	} 
	
	if (validateCommand(device, type, command)) {
		device."$command"()
	} else {
		httpError(403, "Access denied. This command is not supported by current capability.")
	}
}

/**
 * Validating the command passed by the user based on capability.
 * @return boolean
 */
def validateCommand(device, deviceType, command) {
	def capabilityCommands = getDeviceCapabilityCommands(device.capabilities)
	def currentDeviceCapability = getCapabilityName(deviceType)
	if (capabilityCommands[currentDeviceCapability]) {
		return command in capabilityCommands[currentDeviceCapability] ? true : false
	} else {
		// Handling other device types here, which don't accept commands
		httpError(400, "Bad request.")
	}
}

/**
 * Need to get the attribute name to do the lookup. Only
 * doing it for the device types which accept commands
 * @return attribute name of the device type
 */
def getCapabilityName(type) {
    switch(type) {
		case "switches":
			return "Switch"
		case "alarms":
			return "Alarm"
		case "locks":
			return "Lock"
		default:
			return type
	}
}

/**
 * Constructing the map over here of
 * supported commands by device capability
 * @return a map of device capability -> supported commands
 */
def getDeviceCapabilityCommands(deviceCapabilities) {
	def map = [:]
	deviceCapabilities.collect {
		map[it.name] = it.commands.collect{ it.name.toString() }
	}
	return map
}


def show() {
	def type = params.deviceType
	def devices = settings[type]
	def device = devices.find { it.id == params.id }

	//log.debug "[PROD] show, params: ${params}, devices: ${devices*.id}"
	if (!device) {
		httpError(404, "Device not found")
	}
	else {
		def attributeName = attributeFor(type)
		def s = device.currentState(attributeName)
		deviceState(device, s)
	}
}

def addSubscription() {
	log.debug "[PROD] addSubscription1"
	def type = params.deviceType
	def data = request.JSON
	def attribute = attributeFor(type)
	def devices = settings[type]
	def deviceId = data.deviceId
	def callbackUrl = data.callbackUrl
	def device = devices.find { it.id == deviceId }

	//log.debug "[PROD] addSubscription, params: ${params}, request: ${data}, device: ${device}"
	if (device) {
		log.debug "Adding switch subscription " + callbackUrl
		state[deviceId] = [callbackUrl: callbackUrl]
		subscribe(device, attribute, deviceHandler)
	}
	//log.info state

}

def removeSubscription() {
	def type = params.deviceType
	def devices = settings[type]
	def deviceId = params.id
	def device = devices.find { it.id == deviceId }

	//log.debug "[PROD] removeSubscription, params: ${params}, request: ${data}, device: ${device}"
	if (device) {
		log.debug "Removing $device.displayName subscription"
		state.remove(device.id)
		unsubscribe(device)
	}
	//log.info state
}

def deviceHandler(evt) {
	def deviceInfo = state[evt.deviceId]
	if (deviceInfo) {
		try {
			httpPostJson(uri: deviceInfo.callbackUrl, path: '',  body: [evt: [deviceId: evt.deviceId, name: evt.name, value: evt.value]]) {
				log.debug "[PROD IFTTT] Event data successfully posted"
			}
		} catch (groovyx.net.http.ResponseParseException e) {
			log.error("Error parsing ifttt payload ${e}")
		}
	} else {
		log.debug "[PROD] No subscribed device found"
	}
}

private deviceItem(it) {
	it ? [id: it.id, label: it.displayName] : null
}

private deviceState(device, s) {
	device && s ? [id: device.id, label: device.displayName, name: s.name, value: s.value, unixTime: s.date.time] : null
}

private attributeFor(type) {
	switch (type) {
		case "switches":
			log.debug "[PROD] switch type"
			return "switch"
		case "locks":
			log.debug "[PROD] lock type"
			return "lock"
		case "alarms":
			log.debug "[PROD] alarm type"
			return "alarm"
		case "lightSensors":
			log.debug "[PROD] illuminance type"
			return "illuminance"
		default:
			log.debug "[PROD] other sensor type"
			return type - "Sensors"
	}
}
