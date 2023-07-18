/**
 *  Copyright 2021 PlaidSystems
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

Version v3.8
 * remove zigbeeNodeType: "ROUTER" from fingerprint

Version v3.7
 * update add zoneOn, zoneOff commands for external integration
 * move zone status update to parse

 Version v3.6
 * update setTouchButtonDuration to only apply when controller is switched off
 * add external command settingsMap for use with user added Spruce Scheduler

 Version v3.5
 * update zigbee ONOFF cluster
 * update Health Check
 * remove binding since reporting handles this

 Version v3.4
 * update presentation with 'patch' to rename 'valve' to 'Zone x'
 * remove commands on, off
 * add command setValveDuration
 * update settings order and description
 * fix controllerStatus -> status

 Version v3.3
 * change to remotecontrol with components
 * health check -> ping

 Version v3.2
 * add zigbee constants
 * update to zigbee commands
 * tabs and trim whitespace

 Version v3.1
 * Change to work with standard ST automation options
 * use standard switch since custom attributes still don't work in automations
 * Add schedule minute times to settings
 * Add split cycle to settings
 * deprecate Spruce Scheduler compatibility

 Version v3.0
 * Update for new Samsung SmartThings app
 * update vid with status, message, rainsensor
 * maintain compatibility with Spruce Scheduler
 * Requires Spruce Valve as child device

 Version v2.7
 * added Rain Sensor = Water Sensor Capability
 * added Pump/Master
 * add "Dimmer" to Spruce zone child for manual duration

**/

import groovy.json.JsonOutput
import physicalgraph.zigbee.zcl.DataType

//dth version
def getVERSION() {'v3.8 8-2021'}
def getDEBUG() {false}
def getHC_INTERVAL_MINS() {60}
//zigbee cluster, attribute, identifiers
def getALARMS_CLUSTER() {0x0009}
def getBINARY_INPUT_CLUSTER() {0x000F}
def getON_TIME_ATTRIBUTE() {0x4001}
def getOFF_WAIT_TIME_ATTRIBUTE() {0x4002}
def getOUT_OF_SERVICE_IDENTIFIER() {0x0051}
def getPRESENT_VALUE_IDENTIFIER() {0x0055}

metadata {
	definition (name: "Spruce Controller", namespace: "plaidsystems", author: "Plaid Systems", mnmn: "SmartThingsCommunity",
		ocfDeviceType: "x.com.st.d.remotecontroller", mcdSync: true, vid: "2914a12b-504f-344f-b910-54008ba9408f") {

		capability "Actuator"
		capability "Switch"
		capability "Sensor"
		capability "Health Check"
		capability "heartreturn55003.status"
		capability "heartreturn55003.controllerState"
		capability "heartreturn55003.rainSensor"
		capability "heartreturn55003.valveDuration"

		capability "Configuration"
		capability "Refresh"

		attribute "status", "string"
		attribute "controllerState", "string"
		attribute "rainSensor", "string"
		attribute "valveDuration", "NUMBER"

		command "zoneOn"
		command "zoneOff"
		command "setStatus"
		command "setRainSensor"
		command "setControllerState"
		command "setValveDuration"
		command "settingsMap"

		//new release
		fingerprint manufacturer: "PLAID SYSTEMS", model: "PS-SPRZ16-01", deviceJoinName: "Spruce Irrigation Controller"
	}

	preferences {
		//general device settings
		input title: "Device settings", displayDuringSetup: true, type: "paragraph", element: "paragraph",
			description: "Settings for automatic operations and device touch buttons."
		input "rainSensorEnable", "bool", title: "Rain Sensor Attached?", required: false, displayDuringSetup: true
		input "touchButtonDuration", "integer", title: "Automatic turn off time when touch buttons are used on device? (minutes)", required: false, displayDuringSetup: true
		input name: "pumpMasterZone", type: "enum", title: "Pump or Master zone", description: "This zone will turn on and off anytime another zone is turned on or off", required: false,
			options: ["Zone 1", "Zone 2", "Zone 3", "Zone 4", "Zone 5", "Zone 6", "Zone 7", "Zone 8", "Zone 9", "Zone 10", "Zone 11", "Zone 12", "Zone 13", "Zone 14", "Zone 15", "Zone 16"]

		//break for ease of reading settings
		input title: "", description: "", displayDuringSetup: true, type: "paragraph", element: "paragraph"

		//schedule specific settings
		input title: "Schedule setup", displayDuringSetup: true, type: "paragraph", element: "paragraph",
			description: "These settings only effect when the controller is switched to the on state."
		input "splitCycle", "bool", title: "Cycle scheduled watering time to reduce runoff?", required: false, displayDuringSetup: true
		input "valveDelay", "integer", title: "Delay between valves when a schedule runs? (seconds)", required: false, displayDuringSetup: true

		input title: "Schedule times", displayDuringSetup: true, type: "paragraph", element: "paragraph",
			description: "Set the minutes for each zone to water anytime the controller is switched on."
		input name: "z1Duration", type: "integer", title: "Zone 1 schedule minutes"
		input name: "z2Duration", type: "integer", title: "Zone 2 schedule minutes"
		input name: "z3Duration", type: "integer", title: "Zone 3 schedule minutes"
		input name: "z4Duration", type: "integer", title: "Zone 4 schedule minutes"
		input name: "z5Duration", type: "integer", title: "Zone 5 schedule minutes"
		input name: "z6Duration", type: "integer", title: "Zone 6 schedule minutes"
		input name: "z7Duration", type: "integer", title: "Zone 7 schedule minutes"
		input name: "z8Duration", type: "integer", title: "Zone 8 schedule minutes"
		input name: "z9Duration", type: "integer", title: "Zone 9 schedule minutes"
		input name: "z10Duration", type: "integer", title: "Zone 10 schedule minutes"
		input name: "z11Duration", type: "integer", title: "Zone 11 schedule minutes"
		input name: "z12Duration", type: "integer", title: "Zone 12 schedule minutes"
		input name: "z13Duration", type: "integer", title: "Zone 13 schedule minutes"
		input name: "z14Duration", type: "integer", title: "Zone 14 schedule minutes"
		input name: "z15Duration", type: "integer", title: "Zone 15 schedule minutes"
		input name: "z16Duration", type: "integer", title: "Zone 16 schedule minutes"

		input title: "Version", description: VERSION, displayDuringSetup: true, type: "paragraph", element: "paragraph"
	}
}

//----------------------zigbee parse-------------------------------//

// Parse incoming device messages to generate events
def parse(description) {
	if (DEBUG) log.debug description
	def result = []
	def endpoint, value, command
	def map = zigbee.parseDescriptionAsMap(description)
	if (DEBUG && !map.raw) log.debug "map ${map}"

	if (description.contains("on/off")) {
		command = 1
		value = description[-1]
	}
	else {
		endpoint = ( map.sourceEndpoint == null ? zigbee.convertHexToInt(map.endpoint) : zigbee.convertHexToInt(map.sourceEndpoint) )
		value = ( map.sourceEndpoint == null ? zigbee.convertHexToInt(map.value) : null )
		command = (value != null ? commandType(endpoint, map.clusterInt) : null)
	}

	if (DEBUG && command != null) log.debug "${command} >> endpoint ${endpoint} value ${value} cluster ${map.clusterInt}"
	switch (command) {
	  case "alarm":
		result.push(createEvent(name: "status", value: "alarm"))
		break
	  case "schedule":
		def scheduleValue = (value == 1 ? "on" : "off")
		def scheduleState = device.latestValue("controllerState")
		def scheduleStatus = device.latestValue("status")

		if (scheduleState == "pause") log.debug "pausing schedule"
		else {
			if (scheduleStatus != "off" && scheduleValue == "off") result.push(createEvent(name: "status", value: "Schedule ${scheduleValue}"))
			result.push(createEvent(name: "controllerState", value: scheduleValue))
			result.push(createEvent(name: "switch", value: scheduleValue, displayed: false))
		}
		break
	  case "zone":
	  	def onoff = (value == 1 ? "open" : "closed")
		def child = childDevices.find{it.deviceNetworkId == "${device.deviceNetworkId}:${endpoint}"}
		if (child) child.sendEvent(name: "valve", value: onoff)

		sendEvent(name: "status", value: "Zone ${endpoint-1} ${onoff}", descriptionText: "Zone ${endpoint-1} ${onoff}", displayed:true)
		return setTouchButtonDuration()
		break
	  case "rainsensor":
		def rainSensor = (value == 1 ? "wet" : "dry")
		if (!rainSensorEnable) rainSensor = "disabled"
		result.push(createEvent(name: "rainSensor", value: rainSensor))
		break
	  case "refresh":
		//log.debug "refresh command not used"
		break
	  default:
	  	//log.debug "not used command"
		break
	}

	return result
}

def commandType(endpoint, cluster) {
	if (cluster == 9) return "alarm"
	else if (endpoint == 1) return "schedule"
	else if (endpoint in 2..17) return "zone"
	else if (endpoint == 18) return "rainsensor"
	else if (endpoint == 19) return "refresh"
}

//--------------------end zigbee parse-------------------------------//

def installed() {
	createChildDevices()
}

def uninstalled() {
	log.debug "uninstalled"
	removeChildDevices()
}

def updated() {
	log.debug "updated"
	initialize()
}

def initialize() {
	sendEvent(name: "switch", value: "off", displayed: false)
	sendEvent(name: "controllerState", value: "off", displayed: false)
	sendEvent(name: "status", value: "Initialize")
	if (device.latestValue("valveDuration") == null) sendEvent(name: "valveDuration", value: 10)

	//update zigbee device settings
	response(setDeviceSettings() + setTouchButtonDuration() + setRainSensor() + refresh())
}

def createChildDevices() {
	log.debug "create children"
	def pumpMasterZone = (pumpMasterZone ? pumpMasterZone.replaceFirst("Zone ","").toInteger() : null)

	//create, rename, or remove child
	for (i in 1..16) {
		//endpoint is offset, zone number +1
		def endpoint = i + 1

		def child = childDevices.find{it.deviceNetworkId == "${device.deviceNetworkId}:${endpoint}"}
		//create child
		if (!child) {
			def childLabel = "Zone$i"
			child = addChildDevice("Spruce Valve", "${device.deviceNetworkId}:${endpoint}", device.hubId,
					[completedSetup: true, label: "${childLabel}", isComponent: true, componentName: "Zone$i", componentLabel: "Zone$i"])
			log.debug "${child}"
			child.sendEvent(name: "valve", value: "closed", displayed: false)
		}

	}

	state.oldLabel = device.label
}

def removeChildDevices() {
	log.debug "remove all children"

	//get and delete children avoids duplicate children
	def children = getChildDevices()
	if (children != null) {
		children.each{
			deleteChildDevice(it.deviceNetworkId)
		}
	}
}


//----------------------------------commands--------------------------------------//

def setStatus(status) {
	if (DEBUG) log.debug "status ${status}"
	sendEvent(name: "status", value: status, descriptionText: "Initialized")
}

def setRainSensor() {
	if (DEBUG) log.debug "Rain sensor: ${rainSensorEnable}"

	if (rainSensorEnable) return zigbee.writeAttribute(BINARY_INPUT_CLUSTER, OUT_OF_SERVICE_IDENTIFIER, DataType.BOOLEAN, 1, [destEndpoint: 18])
	else return zigbee.writeAttribute(BINARY_INPUT_CLUSTER, OUT_OF_SERVICE_IDENTIFIER, DataType.BOOLEAN, 0, [destEndpoint: 18])
}

def setValveDuration(duration) {
	if (DEBUG) log.debug "Valve Duration set to: ${duration}"

	sendEvent(name: "valveDuration", value: duration, displayed: false)
}

//cahnge the device settings for automatically starting a pump or master zone, set the controller to split scheduled watering cycles, set a delay between scheduled valves
def setDeviceSettings() {
	def pumpMasterZone = (pumpMasterZone ? pumpMasterZone.replaceFirst("Zone ","").toInteger() : null)
	def splitCycle = (splitCycle == true ? 2 : 1)
	def valveDelay = (valveDelay ? valveDelay.toInteger() : 0)
	if (DEBUG) log.debug "Pump/Master: ${pumpMasterEndpoint} splitCycle: ${splitCycle} valveDelay: ${valveDelay}"

	def endpointMap = [:]
	for (zone in 0..17) {
		//setup zone, 1=single cycle, 2=split cycle, 4=pump/master
		def zoneSetup = splitCycle
		if (zone == pumpMasterZone) zoneSetup = 4
		else if (zone == 0) zoneSetup = valveDelay

		def endpoint = zone + 1
		endpointMap."${endpoint}" = "${zoneSetup}"
		zone++
	}

	return settingsMap(endpointMap, ON_TIME_ATTRIBUTE)
}

//change the default time a zone will turn on when the buttons on the face of the controller are used
def setTouchButtonDuration() {
	def touchButtonDuration = (touchButtonDuration ? touchButtonDuration.toInteger() : 10)
	if (DEBUG) log.debug "touchButtonDuration ${touchButtonDuration} mins"

	def sendCmds = []
	sendCmds.push(zigbee.writeAttribute(zigbee.ONOFF_CLUSTER, OFF_WAIT_TIME_ATTRIBUTE, DataType.UINT16, touchButtonDuration, [destEndpoint: 1]))
	if (device.latestValue("controllerState") == "off") return sendCmds
}

//controllerState
def setControllerState(state) {
	if (DEBUG) log.debug "state ${state}"
	sendEvent(name: "controllerState", value: state, descriptionText: "Initialized")

	switch(state) {
		case "on":
			if (!rainDelay()) {
				sendEvent(name: "switch", value: "on", displayed: false)
				sendEvent(name: "status", value: "initialize schedule", descriptionText: "initialize schedule")
				startSchedule()
			}
			break
		case "off":
			sendEvent(name: "switch", value: "off", displayed: false)
			scheduleOff()
			break
		case "pause":
			pause()
			break
		case "resume":
			resume()
			break
	}
}

//on & off from switch
def on() {
	log.debug "switch on"
	setControllerState("on")
}

def off() {
	log.debug "switch off"
	setControllerState("off")
}

def pause() {
	log.debug "pause"
	sendEvent(name: "switch", value: "off", displayed: false)
	sendEvent(name: "status", value: "paused schedule", descriptionText: "pause on")
	scheduleOff()
}

def resume() {
	log.debug "resume"
	sendEvent(name: "switch", value: "on", displayed: false)
	sendEvent(name: "status", value: "resumed schedule", descriptionText: "resume on")
	scheduleOn()
}

//set raindelay
def rainDelay() {
	if (rainSensorEnable && device.latestValue("rainSensor") == "wet") {
		sendEvent(name: "switch", value: "off", displayed: false)
		sendEvent(name: "controllerState", value: "off")
		sendEvent(name: "status", value: "rainy")
		return true
	}
	return false
}

//set schedule
def noSchedule() {
	sendEvent(name: "switch", value: "off", displayed: false)
	sendEvent(name: "controllerState", value: "off")
	sendEvent(name: "status", value: "Set schedule in settings")
}

//schedule on/off
def scheduleOn() {
	zigbee.command(zigbee.ONOFF_CLUSTER, 1, "", [destEndpoint: 1])
}
def scheduleOff() {
	zigbee.command(zigbee.ONOFF_CLUSTER, 0, "", [destEndpoint: 1])
}

// Commands to zones/valves
def valveOn(valueMap) {
	//get endpoint from deviceNetworkId
	def endpoint = valueMap.dni.replaceFirst("${device.deviceNetworkId}:","").toInteger()
	def duration = (device.latestValue("valveDuration").toInteger())

	if (DEBUG) log.debug "state ${state.hasConfiguredHealthCheck} ${zigbee.ONOFF_CLUSTER}"
	zoneOn(endpoint, duration)
}

def valveOff(valueMap) {
	def endpoint = valueMap.dni.replaceFirst("${device.deviceNetworkId}:","").toInteger()

	zoneOff(endpoint)
}

def zoneOn(endpoint, duration) {
	//send duration
	return zoneDuration(duration.toInteger()) + zigbee.command(zigbee.ONOFF_CLUSTER, 1, "", [destEndpoint: endpoint])
}

def zoneOff(endpoint) {
	//reset touchButtonDuration to setting value
	return zigbee.command(zigbee.ONOFF_CLUSTER, 0, "", [destEndpoint: endpoint]) + setTouchButtonDuration()
}

def zoneDuration(int duration) {
	def sendCmds = []
	sendCmds.push(zigbee.writeAttribute(zigbee.ONOFF_CLUSTER, OFF_WAIT_TIME_ATTRIBUTE, DataType.UINT16, duration, [destEndpoint: 1]))
	return sendCmds
}

//------------------end commands----------------------------------//

//get times from settings and send to controller, then start schedule
def startSchedule() {
	def startRun = false
	def runTime, totalTime=0
	def scheduleTimes = []

	for (i in 1..16) {
		def endpoint = i + 1
		//if (settings."z${i}" && settings."z${i}Duration" != null) {
		if (settings."z${i}Duration" != null) {
			runTime = Integer.parseInt(settings."z${i}Duration")
			totalTime += runTime
			startRun = true

			scheduleTimes.push(zigbee.writeAttribute(zigbee.ONOFF_CLUSTER, OFF_WAIT_TIME_ATTRIBUTE, DataType.UINT16, runTime, [destEndpoint: endpoint]))
		}
		else {
			scheduleTimes.push(zigbee.writeAttribute(zigbee.ONOFF_CLUSTER, OFF_WAIT_TIME_ATTRIBUTE, DataType.UINT16, 0, [destEndpoint: endpoint]))
		}
	}
	if (!startRun || totalTime == 0) return noSchedule()

	//start after scheduleTimes are sent
	scheduleTimes.push(zigbee.command(zigbee.ONOFF_CLUSTER, 1, "", [destEndpoint: 1]))
	sendEvent(name: "status", value: "Scheduled for ${totalTime}min(s)", descriptionText: "Start schedule ending in ${totalTime} mins")
	return scheduleTimes
}

//write switch time settings map
def settingsMap(WriteTimes, attrType) {
	if (DEBUG) log.debug "settingsMap ${WriteTimes}, ${attrType}"
	def runTime
	def sendCmds = []
	for (endpoint in 1..17) {
		if (WriteTimes."${endpoint}") {
			runTime = Integer.parseInt(WriteTimes."${endpoint}")

			if (attrType == ON_TIME_ATTRIBUTE) sendCmds.push(zigbee.writeAttribute(zigbee.ONOFF_CLUSTER, ON_TIME_ATTRIBUTE, DataType.UINT16, runTime, [destEndpoint: endpoint]))
			else sendCmds.push(zigbee.writeAttribute(zigbee.ONOFF_CLUSTER, OFF_WAIT_TIME_ATTRIBUTE, DataType.UINT16, runTime, [destEndpoint: endpoint]))
		}
	}
	return sendCmds
}

//send switch time
def writeType(endpoint, cycle) {
	zigbee.writeAttribute(zigbee.ONOFF_CLUSTER, ON_TIME_ATTRIBUTE, DataType.UINT16, cycle, [destEndpoint: endpoint])
}

//send switch off time
def writeTime(endpoint, runTime) {
	zigbee.writeAttribute(zigbee.ONOFF_CLUSTER, OFF_WAIT_TIME_ATTRIBUTE, DataType.UINT16, runTime, [destEndpoint: endpoint])
}

//set reporting and binding
def configure() {
	// Device-Watch checks every 1 hour
	sendEvent(name: "checkInterval", value: HC_INTERVAL_MINS * 60, displayed: false, data: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID])

	if (DEBUG) log.debug "Configuring Reporting ${device.name} ${device.deviceNetworkId} ${device.hub.zigbeeId}"

	//setup reporting for 18 endpoints
	def reportingCmds = []
	reportingCmds += zigbee.configureReporting(zigbee.ONOFF_CLUSTER, 0, DataType.BOOLEAN, 1, 0, 0x01, [destEndpoint: 1])
	reportingCmds += zigbee.configureReporting(ALARMS_CLUSTER, 0, DataType.UINT16, 1, 0, 0x00, [destEndpoint: 1])

	for (endpoint in 1..18) {
		reportingCmds += zigbee.configureReporting(BINARY_INPUT_CLUSTER, PRESENT_VALUE_IDENTIFIER, DataType.BOOLEAN, 1, 0, 0x01, [destEndpoint: endpoint])
	}

	return reportingCmds + setRainSensor()
}

//PING is used by Device-Watch in attempt to reach the Device
def ping() {
	if (DEBUG) log.debug "device health ping"
	return refresh()
}

def refresh() {
	if (DEBUG) log.debug "refresh"

	def refreshCmds = []
	for (endpoint in 1..17) {
		refreshCmds += zigbee.readAttribute(BINARY_INPUT_CLUSTER, PRESENT_VALUE_IDENTIFIER, [destEndpoint: endpoint])
	}
	refreshCmds += zigbee.readAttribute(BINARY_INPUT_CLUSTER, OUT_OF_SERVICE_IDENTIFIER, [destEndpoint: 18])

	return refreshCmds
}
