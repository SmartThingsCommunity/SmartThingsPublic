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
 *  Ubi
 *
 *  Author: SmartThings
 */

definition(
    name: "Ubi",
    namespace: "smartthings",
    author: "SmartThings",
    description: "Add your Ubi device to your SmartThings Account",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Partner/ubi-app-icn.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Partner/ubi-app-icn@2x.png",
    oauth: [displayName: "Ubi", displayLink: ""]
)

preferences {
	section("Allow a web application to control these things...") {
		input name: "switches", type: "capability.switch", title: "Which Switches?", multiple: true, required: false
		input name: "motions", type: "capability.motionSensor", title: "Which Motion Sensors?", multiple: true, required: false
		input name: "locks", type: "capability.lock", title: "Which Locks?", multiple: true, required: false
		input name: "contactSensors", type: "capability.contactSensor", title: "Which Contact Sensors?", multiple: true, required: false
		input name: "presenceSensors", type: "capability.presenceSensor", title: "Which Presence Sensors?", multiple: true, required: false
	}
}

mappings {
	path("/list") {
		action: [
			GET: "listAll"
		]
	}

	path("/events/:id") {
		action: [
			GET: "showEvents"
		]
	}

	path("/switches") {
		action: [
			GET: "listSwitches",
			PUT: "updateSwitches",
			POST: "updateSwitches"
		]
	}
	path("/switches/:id") {
		action: [
			GET: "showSwitch",
			PUT: "updateSwitch",
			POST: "updateSwitch"
		]
	}
	path("/switches/subscriptions") {
		log.debug "switches added"
		action: [
			POST: "addSwitchSubscription"
		]
	}
	path("/switches/subscriptions/:id") {
		action: [
			DELETE: "removeSwitchSubscription",
			GET: "removeSwitchSubscription"
		]
	}

	path("/motionSensors") {
		action: [
			GET: "listMotions",
			PUT: "updateMotions",
			POST: "updateMotions"

		]
	}
	path("/motionSensors/:id") {
		action: [
			GET: "showMotion",
			PUT: "updateMotion",
			POST: "updateMotion"
		]
	}
	path("/motionSensors/subscriptions") {
		log.debug "motionSensors added"
		action: [
			POST: "addMotionSubscription"
		]
	}
	path("/motionSensors/subscriptions/:id") {
		log.debug "motionSensors Deleted"
		action: [
			DELETE: "removeMotionSubscription",
			GET: "removeMotionSubscription"
		]
	}

	path("/locks") {
		action: [
			GET: "listLocks",
			PUT: "updateLocks",
			POST: "updateLocks"
		]
	}
	path("/locks/:id") {
		action: [
			GET: "showLock",
			PUT: "updateLock",
			POST: "updateLock"
		]
	}
	path("/locks/subscriptions") {
		action: [
			POST: "addLockSubscription"
		]
	}
	path("/locks/subscriptions/:id") {
		action: [
			DELETE: "removeLockSubscription",
			GET: "removeLockSubscription"
		]
	}

	path("/contactSensors") {
		action: [
			GET: "listContactSensors",
			PUT: "updateContactSensor",
			POST: "updateContactSensor"
		]
	}
	path("/contactSensors/:id") {
		action: [
			GET: "showContactSensor",
			PUT: "updateContactSensor",
			POST: "updateContactSensor"
		]
	}
	path("/contactSensors/subscriptions") {
		log.debug "contactSensors/subscriptions"
		action: [
			POST: "addContactSubscription"
		]
	}
	path("/contactSensors/subscriptions/:id") {
		action: [
			DELETE: "removeContactSensorSubscription",
			GET: "removeContactSensorSubscription"
		]
	}

	path("/presenceSensors") {
		action: [
			GET: "listPresenceSensors",
			PUT: "updatePresenceSensor",
			POST: "updatePresenceSensor"
		]
	}
	path("/presenceSensors/:id") {
		action: [
			GET: "showPresenceSensor",
			PUT: "updatePresenceSensor",
			POST: "updatePresenceSensor"
		]
	}
	path("/presenceSensors/subscriptions") {
		log.debug "PresenceSensors/subscriptions"
		action: [
			POST: "addPresenceSubscription"
		]
	}
	path("/presenceSensors/subscriptions/:id") {
		action: [
			DELETE: "removePresenceSensorSubscription",
			GET: "removePresenceSensorSubscription"
		]
	}

	path("/state") {
		action: [
			GET: "currentState"
		]
	}

	path("/phrases") {
		action: [
			GET: "listPhrases"
		]
	}
	path("/phrases/:phraseName") {
		action: [
			GET: "executePhrase",
			POST: "executePhrase",
		]
	}

}

def installed() {
//	subscribe(motions, "motion.active", motionOpenHandler)

//	subscribe(contactSensors, "contact.open", contactOpenHandler)
//	log.trace "contactSensors Installed"

}

def updated() {

//	unsubscribe()
//    subscribe(motions, "motion.active", motionOpenHandler)
//	subscribe(contactSensors, "contact.open", contactOpenHandler)

//log.trace "contactSensors Updated"

}

def listAll() {
	listSwitches() + listMotions() + listLocks() + listContactSensors() + listPresenceSensors() + listPhrasesWithType()
}

def listContactSensors() {
	contactSensors.collect { device(it, "contactSensor") }
}


void updateContactSensors() {
	updateAll(contactSensors)
}

def showContactSensor() {
	show(contactSensors, "contact")
}

void updateContactSensor() {
	update(contactSensors)
}

def addContactSubscription() {
	log.debug "addContactSensorSubscription,  params: ${params}"
	addSubscription(contactSensors, "contact")
}

def removeContactSensorSubscription() {
	removeSubscription(contactSensors)
}


def listPresenceSensors() {
	presenceSensors.collect { device(it, "presenceSensor") }
}


void updatePresenceSensors() {
	updateAll(presenceSensors)
}

def showPresenceSensor() {
	show(presenceSensors, "presence")
}

void updatePresenceSensor() {
	update(presenceSensors)
}

def addPresenceSubscription() {
	log.debug "addPresenceSensorSubscription,  params: ${params}"
	addSubscription(presenceSensors, "presence")
}

def removePresenceSensorSubscription() {
	removeSubscription(presenceSensors)
}


def listSwitches() {
	switches.collect { device(it, "switch") }
}

void updateSwitches() {
	updateAll(switches)
}

def showSwitch() {
	show(switches, "switch")
}

void updateSwitch() {
	update(switches)
}

def addSwitchSubscription() {
	log.debug "addSwitchSubscription,  params: ${params}"
	addSubscription(switches, "switch")
}

def removeSwitchSubscription() {
	removeSubscription(switches)
}

def listMotions() {
	motions.collect { device(it, "motionSensor") }
}

void updateMotions() {
	updateAll(motions)
}

def showMotion() {
	show(motions, "motion")
}

void updateMotion() {
	update(motions)
}

def addMotionSubscription() {

	addSubscription(motions, "motion")
}

def removeMotionSubscription() {
	removeSubscription(motions)
}

def listLocks() {
	locks.collect { device(it, "lock") }
}

void updateLocks() {
	updateAll(locks)
}

def showLock() {
	show(locks, "lock")
}

void updateLock() {
	update(locks)
}

def addLockSubscription() {
	addSubscription(locks, "lock")
}

def removeLockSubscription() {
	removeSubscription(locks)
}

/*
def motionOpenHandler(evt) {
//log.trace "$evt.value: $evt, $settings"

	log.debug "$motions was active, sending push message to user"
	//sendPush("Your ${contact1.label ?: contact1.name} was opened")


	httpPostJson(uri: "http://automatesolutions.ca/test.php", path: '', body: [evt: [value: "motionSensor Active"]]) {
	log.debug "Event data successfully posted"
    }

}
def contactOpenHandler(evt) {
	//log.trace "$evt.value: $evt, $settings"

	log.debug "$contactSensors was opened, sending push message to user"
	//sendPush("Your ${contact1.label ?: contact1.name} was opened")


	httpPostJson(uri: "http://automatesolutions.ca/test.php", path: '', body: [evt: [value: "ContactSensor Opened"]]) {
	log.debug "Event data successfully posted"
    }


}
*/


def deviceHandler(evt) {
	log.debug "~~~~~TEST~~~~~~"
	def deviceInfo = state[evt.deviceId]
	if (deviceInfo)
	{
		httpPostJson(uri: deviceInfo.callbackUrl, path: '', body: [evt: [value: evt.value]]) {
			log.debug "Event data successfully posted"
		}
	}
	else
	{
		log.debug "No subscribed device found"
	}
}

def currentState() {
	state
}

def showStates() {
	def device = (switches + motions + locks).find { it.id == params.id }
	if (!device)
	{
		httpError(404, "Switch not found")
	}
	else
	{
		device.events(params)
	}
}

def listPhrasesWithType() {
	location.helloHome.getPhrases().collect {
		[
			"id"   : it.id,
			"label": it.label,
			"type" : "phrase"
		]
	}
}

def listPhrases() {
	location.helloHome.getPhrases().label
}

def executePhrase() {
	def phraseName = params.phraseName
	if (phraseName)
	{
		location.helloHome.execute(phraseName)
		log.debug "executed phrase: $phraseName"
	}
	else
	{
		httpError(404, "Phrase not found")
	}
}

private void updateAll(devices) {
	def type = params.param1
	def command = request.JSON?.command
	if (!devices) {
		httpError(404, "Devices not found")
	}
	if (command){
		devices.each { device ->
			executeCommand(device, type, command)
		}
	}
}

private void update(devices) {
	log.debug "update, request: ${request.JSON}, params: ${params}, devices: $devices.id"
	def type = params.param1
	def command = request.JSON?.command
	def device = devices?.find { it.id == params.id }

	if (!device) {
			httpError(404, "Device not found")
	}

	if (command) {
		executeCommand(device, type, command)
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

/**
 * Validates and executes the command
 * on the device or devices
 */
def executeCommand(device, type, command) {
	if (validateCommand(device, type, command)) {
		device."$command"()
	} else {
		httpError(403, "Access denied. This command is not supported by current capability.")
	}	
}

private show(devices, type) {
	def device = devices.find { it.id == params.id }
	if (!device)
	{
		httpError(404, "Device not found")
	}
	else
	{
		def attributeName = type

		def s = device.currentState(attributeName)
		[id: device.id, label: device.displayName, value: s?.value, unitTime: s?.date?.time, type: type]
	}
}

private addSubscription(devices, attribute) {
	//def deviceId = request.JSON?.deviceId
	//def callbackUrl = request.JSON?.callbackUrl

	log.debug "addSubscription,  params: ${params}"

	def deviceId = params.deviceId
	def callbackUrl = params.callbackUrl

	def myDevice = devices.find { it.id == deviceId }
	if (myDevice)
	{
		log.debug "Adding switch subscription" + callbackUrl
		state[deviceId] = [callbackUrl: callbackUrl]
		log.debug "Added state: $state"
		def subscription = subscribe(myDevice, attribute, deviceHandler)
		if (subscription && subscription.eventSubscription) {
			log.debug "Subscription is newly created"
		} else {
			log.debug "Subscription already exists, returning existing subscription"
			subscription = app.subscriptions?.find { it.deviceId == deviceId && it.data == attribute && it.handler == 'deviceHandler' }
		}
		[
			id: subscription.id,
			deviceId: subscription.deviceId,
			data: subscription.data,
			handler: subscription.handler,
			callbackUrl: callbackUrl
		]
	}
}

private removeSubscription(devices) {
	def deviceId = params.id
	def device = devices.find { it.id == deviceId }
	if (device)
	{
		log.debug "Removing $device.displayName subscription"
		state.remove(device.id)
		unsubscribe(device)
	}
}

private device(it, type) {
	it ? [id: it.id, label: it.displayName, type: type] : null
}
