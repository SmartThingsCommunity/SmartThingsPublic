/**
 *  Copyright 2020 PlaidSystems
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
 Version v3.3
 * change to remotecontrol with components
 
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
def getVERSION() {'v3.3 12-2020'}
def getDEBUG() {true}
//zigbee cluster, attribute, identifiers
def getON_OFF_CLUSTER() {0x0006}
def getALARMS_CLUSTER() {0x0009}
def getBINARY_INPUT_CLUSTER() {0x000F}
def getON_TIME_ATTRIBUTE() {0x4001}
def getOFF_WAIT_TIME_ATTRIBUTE() {0x4002}
def getOUT_OF_SERVICE_IDENTIFIER() {0x0051}
def getPRESENT_VALUE_IDENTIFIER() {0x0055}

metadata {
	definition (name: "Spruce Controller", namespace: "plaidsystems", author: "Plaid Systems", mnmn: "SmartThingsCommunity",
    	ocfDeviceType: "x.com.st.d.remotecontroller", mcdSync: true, vid: "292b4067-e1d7-3460-a36d-2ddd8ab03079") {
		
        capability "Actuator"
		capability "Switch"
        capability "Valve"
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

		command "on"
		command "off"
		command "setStatus"
		command "setRainSensor"
		command "setControllerState"
        command "setValveDuration"

		//new release
		fingerprint manufacturer: "PLAID SYSTEMS", model: "PS-SPRZ16-01", deviceJoinName: "Spruce Irrigation Controller"
	}

	preferences {
		input title: "Version", description: VERSION, displayDuringSetup: true, type: "paragraph", element: "paragraph"

		input title: "Device settings", displayDuringSetup: true, type: "paragraph", element: "paragraph",
			description: "Settings for automatic operations and device touch buttons."

		input "touchButtonDuration", "integer", title: "Automatic turn off time when touch buttons are used on device? (minutes)", required: false, displayDuringSetup: true
		input "rainSensorEnable", "bool", title: "Rain Sensor Attached?", required: false, displayDuringSetup: true

		input name: "pumpMasterZone", type: "enum", title: "Pump or Master zone", description: "This zone will turn on and off anytime another zone is turned on or off", required: false,
			options: ["Zone 1", "Zone 2", "Zone 3", "Zone 4", "Zone 5", "Zone 6", "Zone 7", "Zone 8", "Zone 9", "Zone 10", "Zone 11", "Zone 12", "Zone 13", "Zone 14", "Zone 15", "Zone 16"]
		input "splitCycle", "bool", title: "Cycle scheduled watering time to reduce runoff?", required: false, displayDuringSetup: true
		input "valveDelay", "integer", title: "Delay between valves when a schedule runs? (seconds)", required: false, displayDuringSetup: true

		input title: "Zone devices", displayDuringSetup: true, type: "paragraph", element: "paragraph",
			description: "Set schedule minutes to water a zone anytime controller state is switched on."
		
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
	}
}

//----------------------zigbee parse-------------------------------//

// Parse incoming device messages to generate events
def parse(String description) {	
	def result = []
	def endpoint, value, command
	def map = zigbee.parseDescriptionAsMap(description)
	if (DEBUG && !map.raw) log.debug "map ${map}"

	if (description.contains("on/off")){
		command = 1
		value = description[-1]
	}
	else {
		endpoint = ( map.sourceEndpoint == null ? hextoint(map.endpoint) : hextoint(map.sourceEndpoint) )
		value = ( map.sourceEndpoint == null ? hextoint(map.value) : null )
		command = (value != null ? commandType(endpoint, map.clusterInt) : null)
	}

	if (DEBUG && command != null) log.debug "${command} >> endpoint ${endpoint} value ${value} cluster ${map.clusterInt}"
	switch(command) {
	  case "alarm":
		result.push(createEvent(name: "status", value: "alarm"))
		break
	  case "schedule":
		def scheduleValue = (value == 1 ? "on" : "off")
		if (scheduleValue == "off") result.push(createEvent(name: "status", value: "Schedule ${scheduleValue}"))
		result.push(createEvent(name: "switch", value: scheduleValue, displayed: false))
		result.push(createEvent(name: "controllerState", value: scheduleValue))
		break
	  case "zone":
	  	def onoff = (value == 1 ? "open" : "closed")
		def child = childDevices.find{it.deviceNetworkId == "${device.deviceNetworkId}:${endpoint}"}
		if (child) child.sendEvent(name: "valve", value: onoff)

		if (device.latestValue("controllerStatus") == "off") return setTouchButtonDuration()
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
    initialize()	//remove after test
    createChildDevices()
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
	for (i in 1..16){
		//endpoint is offset, zone number +1
		def endpoint = i + 1		
		
		def child = childDevices.find{it.deviceNetworkId == "${device.deviceNetworkId}:${endpoint}"}
		//create child
		if (!child){
			def childLabel = "Zone$i"
			child = addChildDevice("Spruce Valve", "${device.deviceNetworkId}:${endpoint}", device.hubId,
					[completedSetup: true, label: "${childLabel}", isComponent: true, componentName: "Zone$i", componentLabel: "Zone$i"])
			log.debug "${child}"
			child.sendEvent(name: "valve", value: "closed", displayed: false)
		}

	}

	state.oldLabel = device.label
}

def open() {
	log.debug "open"
    sendEvent(name: "valve", value: "open")
}

def closed() {
	log.debug "closed"
    sendEvent(name: "valve", value: "closed")
}

def setValveDuration(duration) {
	log.debug "valveDuration ${duration}"
	sendEvent(name: "valveDuration", value: duration, unit: "mins")
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
def setTouchButtonDuration(){
	def touchButtonDuration = (touchButtonDuration ? touchButtonDuration.toInteger() : 10)
	if (DEBUG) log.debug "touchButtonDuration ${touchButtonDuration} mins"

	def sendCmds = []
	sendCmds.push(zigbee.writeAttribute(ON_OFF_CLUSTER, OFF_WAIT_TIME_ATTRIBUTE, DataType.UINT16, touchButtonDuration, [destEndpoint: 1]))
	return sendCmds
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
	sendEvent(name: "controllerStatus", value: "pause", descriptionText: "pause on")
	scheduleOff()
}

def resume() {
	log.debug "resume"
	sendEvent(name: "switch", value: "on", displayed: false)
	sendEvent(name: "controllerStatus", value: "resume", descriptionText: "resume on")
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
	zigbee.command(ON_OFF_CLUSTER, 1, "", [destEndpoint: 1])
}
def scheduleOff() {
	zigbee.command(ON_OFF_CLUSTER, 0, "", [destEndpoint: 1])
}

// Commands to zones/valves
def valveOn(valueMap) {
	//get endpoint from deviceNetworkId
	def endpoint = valueMap.dni.replaceFirst("${device.deviceNetworkId}:","").toInteger()
	def duration = (device.latestValue("valveDuration").toInteger())

	sendEvent(name: "status", value: "${valueMap.label} on for ${duration}min(s)", descriptionText: "Zone ${valueMap.label} on for ${duration}min(s)")
	if (DEBUG) log.debug "state ${state.hasConfiguredHealthCheck} ${ON_OFF_CLUSTER}"
	zoneOn(endpoint, duration)
}

def valveOff(valueMap) {
	def endpoint = valueMap.dni.replaceFirst("${device.deviceNetworkId}:","").toInteger()

	sendEvent(name: "status", value: "${valueMap.label} turned off", descriptionText: "${valueMap.label} turned off")

	zoneOff(endpoint)
}

def zoneOn(endpoint, duration) {
	//send duration from slider
	return zoneDuration(duration) + zigbee.command(ON_OFF_CLUSTER, 1, "", [destEndpoint: endpoint])
}

def zoneOff(endpoint) {
	//reset touchButtonDuration to setting value
	return zigbee.command(ON_OFF_CLUSTER, 0, "", [destEndpoint: endpoint]) + setTouchButtonDuration()
}

def zoneDuration(int duration) {
	def sendCmds = []
	sendCmds.push(zigbee.writeAttribute(ON_OFF_CLUSTER, OFF_WAIT_TIME_ATTRIBUTE, DataType.UINT16, duration, [destEndpoint: 1]))
	return sendCmds
}

//------------------end commands----------------------------------//

//get times from settings and send to controller, then start schedule
def startSchedule() {
	def startRun = false
	def runTime, totalTime=0
	def scheduleTimes = []

	for (i in 1..16){
		def endpoint = i + 1        
		//if (settings."z${i}" && settings."z${i}Duration" != null){
        if (settings."z${i}Duration" != null){
			runTime = Integer.parseInt(settings."z${i}Duration")
			totalTime += runTime
			startRun = true

			scheduleTimes.push(zigbee.writeAttribute(ON_OFF_CLUSTER, OFF_WAIT_TIME_ATTRIBUTE, DataType.UINT16, runTime, [destEndpoint: endpoint]))
		}
		else {
			scheduleTimes.push(zigbee.writeAttribute(ON_OFF_CLUSTER, OFF_WAIT_TIME_ATTRIBUTE, DataType.UINT16, 0, [destEndpoint: endpoint]))
		}
	}
	if (!startRun || totalTime == 0) return noSchedule()

	//start after scheduleTimes are sent
	scheduleTimes.push(zigbee.command(ON_OFF_CLUSTER, 1, "", [destEndpoint: 1]))
	sendEvent(name: "status", value: "Scheduled for ${totalTime}min(s)", descriptionText: "Start schedule ending in ${totalTime} mins")
	return scheduleTimes
}

//write switch time settings map
def settingsMap(WriteTimes, attrType) {

	def runTime
	def sendCmds = []
	for (endpoint in 1..17) {
		if (WriteTimes."${endpoint}"){
			runTime = Integer.parseInt(WriteTimes."${endpoint}")

			if (attrType == ON_TIME_ATTRIBUTE) sendCmds.push(zigbee.writeAttribute(ON_OFF_CLUSTER, ON_TIME_ATTRIBUTE, DataType.UINT16, runTime, [destEndpoint: endpoint]))
			else sendCmds.push(zigbee.writeAttribute(ON_OFF_CLUSTER, OFF_WAIT_TIME_ATTRIBUTE, DataType.UINT16, runTime, [destEndpoint: endpoint]))
		}
	}
	return sendCmds
}

//send switch time
def writeType(endpoint, cycle) {
	zigbee.writeAttribute(ON_OFF_CLUSTER, ON_TIME_ATTRIBUTE, DataType.UINT16, cycle, [destEndpoint: endpoint])
}

//send switch off time
def writeTime(endpoint, runTime) {
	zigbee.writeAttribute(ON_OFF_CLUSTER, OFF_WAIT_TIME_ATTRIBUTE, DataType.UINT16, runTime, [destEndpoint: endpoint])
}

//set reporting and binding
def configure() {
	// Device-Watch allows 2 check-in misses from device, checks every 2 hours
	sendEvent(name: "DeviceWatch-DeviceStatus", value: "online")
	sendEvent(name: "healthStatus", value: "online")
	sendEvent(name: "DeviceWatch-Enroll", value: 2* 60 * 60, displayed: false, data: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID])

	configureHealthCheck()
	
	if (DEBUG) log.debug "Configuring Reporting and Bindings ${device.name} ${device.deviceNetworkId} ${device.hub.zigbeeId}"

	//setup binding for 18 endpoints
	def bindCmds = []
	bindCmds += zigbee.addBinding(ON_OFF_CLUSTER, [destEndpoint: 1])
	bindCmds += zigbee.addBinding(ALARMS_CLUSTER, [destEndpoint: 1])

	for (endpoint in 1..18){
		bindCmds += zigbee.addBinding(BINARY_INPUT_CLUSTER, [destEndpoint: endpoint])
	}

	//setup reporting for 18 endpoints
	def reportingCmds = []
	reportingCmds += zigbee.configureReporting(ON_OFF_CLUSTER, 0, DataType.BOOLEAN, 1, 0, 0x01, [destEndpoint: 1])
	reportingCmds += zigbee.configureReporting(ALARMS_CLUSTER, 0, DataType.UINT16, 1, 0, 0x00, [destEndpoint: 1])

	for (endpoint in 1..18){
		reportingCmds += zigbee.configureReporting(BINARY_INPUT_CLUSTER, PRESENT_VALUE_IDENTIFIER, DataType.BOOLEAN, 1, 0, 0x01, [destEndpoint: endpoint])
	}

	return bindCmds + reportingCmds + setRainSensor()
}

//PING is used by Device-Watch in attempt to reach the Device
def ping() {
	if (DEBUG) log.debug "device health ping"
	return zigbee.onOffRefresh()
}

def refresh() {
	if (DEBUG) log.debug "refresh"

	def refreshCmds = []
	for (endpoint in 1..17){
		refreshCmds += zigbee.readAttribute(BINARY_INPUT_CLUSTER, PRESENT_VALUE_IDENTIFIER, [destEndpoint: endpoint])
	}
	refreshCmds += zigbee.readAttribute(BINARY_INPUT_CLUSTER, OUT_OF_SERVICE_IDENTIFIER, [destEndpoint: 18])

	return refreshCmds
}

def healthPoll() {
	if (DEBUG) log.debug "healthPoll()"
	def cmds = refresh()
	cmds.each { sendHubCommand(new physicalgraph.device.HubAction(it)) }
}

def configureHealthCheck() {
	Integer hcIntervalMinutes = 12
	if (!state.hasConfiguredHealthCheck) {
		log.debug "Configuring Health Check, Reporting"
		unschedule("healthPoll")
		runEvery5Minutes("healthPoll")
		def healthEvent = [name: "checkInterval", value: hcIntervalMinutes * 60, displayed: false, data: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID]]
		// Device-Watch allows 2 check-in misses from device
		sendEvent(healthEvent)
		childDevices.each {
			it.sendEvent(healthEvent)
		}
		state.hasConfiguredHealthCheck = true
	}
}

//parse hex string and make integer
private hextoint(String hex) {
	Long.parseLong(hex, 16).toInteger()
}

/*
private hex(value) {
	new BigInteger(Math.round(value).toString()).toString(16)
}
//${zigbee.swapEndianHex(zigbee.convertToHexString(groupAddr, 4))}
private String swapEndianHex(String hex) {
	reverseArray(hex.decodeHex()).encodeHex()
}

private byte[] reverseArray(byte[] array) {
	int i = 0;
	int j = array.length - 1;
	byte tmp;
	while (j > i) {
		tmp = array[j];
		array[j] = array[i];
		array[i] = tmp;
		j--;
		i++;
	}
	return array
}
*/