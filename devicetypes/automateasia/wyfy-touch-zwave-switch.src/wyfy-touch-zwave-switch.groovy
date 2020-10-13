/**
 *  WYFY TOUCH ZWAVE SWITCH
 *
 *  Copyright 2020 Edwin Tan Poh Heng
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 */

/*
Description of State variables
endpointCount: The number of endpoints
endpointMap: A collection that maps childDeviceNetworkId to their endpoint 
zwaveParemeterKey: A collection of the ZWave parameter's value and state
*/

metadata {
	definition (name: "WYFY TOUCH ZWAVE SWITCH", namespace: "AutomateAsia", author: "Edwin Tan Poh Heng", cstHandler: true) {
		capability "Zw Multichannel"
		capability "Switch"
		capability "Refresh"
		capability "Configuration"
		capability "Health Check"
		
		fingerprint mfr: "015F", prod: "3141", model: "5102", deviceJoinName: "WYFY Touch S1"
	}
	
	preferences {
		parameterMap().each {
			input (
					title: "${it.title}",
					description: it.descr,
					type: "paragraph",
					element: "paragraph"
			)
			def defVal = it.def as Integer
			def descrDefVal = it.options ? it.options.get(defVal) : defVal
			input (
					name: it.key,
					title: null,
					description: "$descrDefVal",
					type: it.type,
					options: it.options,
					range: (it.min != null && it.max != null) ? "${it.min}..${it.max}" : null,
					defaultValue: it.def,
					required: false
			)
		}
	}
}

/*
######################
## Command handlers ##
######################
*/
// Handles the Switch capability's on command of the parent device
def on(){
	log.info("${device.displayName} - Executing parent switch capabilty command: on")
	sendHubCommand(response(encap(zwave.basicV1.basicSet(value: 255),1)))
	sendHubCommand(response(encap(zwave.basicV1.basicGet(),1)))
}

// Handles the Switch capability's off command of the parent device
def off(){
	log.info("${device.displayName} - Executing parent switch capability command: off")
	sendHubCommand(response(encap(zwave.basicV1.basicSet(value: 0),1)))
	sendHubCommand(response(encap(zwave.basicV1.basicGet(),1)))
}

// Handles the Switch capability's on command of a child device
def childOn(childDeviceNetworkId) {
	log.info("${device.displayName} - Executing child ${childDeviceNetworkId} switch capability command: on")
	def endpoint = state.endpointMap.get(childDeviceNetworkId)
	if (endpoint) {
		sendHubCommand(response(encap(zwave.basicV1.basicSet(value: 255),endpoint)))
		sendHubCommand(response(encap(zwave.basicV1.basicGet(),endpoint)))
	}
}

// Handles the Switch capability's off command of a child device
def childOff(childDeviceNetworkId) {
	log.info("${device.displayName} - Executing child ${childDeviceNetworkId} switch capability command: off")
	def endpoint = state.endpointMap.get(childDeviceNetworkId)
	if (endpoint) {
		sendHubCommand(response(encap(zwave.basicV1.basicSet(value: 0),endpoint)))
		sendHubCommand(response(encap(zwave.basicV1.basicGet(),endpoint)))
	}
}

// Handles the Refresh capability's refresh command of the parent device. Triggered by end user when pulls down the detailed parent device screen
def refresh() {
	log.info("${device.displayName} - Executing parent switch capabilty refresh")
	sendHubCommand(response(encap(zwave.basicV1.basicGet(),1)))
}

// Handles the Refresh capability's refresh command of the child device. Triggered by end user when pulls down the detailed child device screen
def childRefresh(childDeviceNetworkId) {
	log.info("${device.displayName} - Executing child ${childDeviceNetworkId} switch capability refresh")
	def endpoint = state.endpointMap.get(childDeviceNetworkId)
	if (endpoint) {
		sendHubCommand(response(encap(zwave.basicV1.basicGet(),endpoint)))
	}
}

// Handles the configuration capability's configure command. This command will be called right after the device joins to set device-specific configuration commands.
def configure() {
	log.info("${device.displayName} - Executing configure()")
	if ( state.lastUpdated && (now() - state.lastUpdated) < 500 ) return
	else initialize()
}

// The Health Check capability uses the “checkInterval” attribute to determine the maximum number of seconds the device can go without generating new events.
// If the device hasn’t created any events within that amount of time, SmartThings executes the “ping()” command.
// If ping() does not generate any events, SmartThings marks the device as offline. 
def ping() {
	log.info("${device.displayName} - Executing ping()")
	refresh()
}

/*
###############################
## Device Lifecycle handlers ##
###############################
*/
// Called when an instance of the app is installed. Typically subscribes to Events from the configured devices and creates any scheduled jobs.
def installed(){
	log.info("${device.displayName} - Executing installed()")
	log.debug("${device.displayName} - ZwaveInfo: ${getZwaveInfo()}")
	sendEvent(name: "checkInterval", value: 1800, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])
	// Initialise state data
	state.endpointMap = [:]
	state.endpointCount = getZwaveInfo()?.endpointCount
	initialize()	
	for (i in 1..state.endpointCount) {
		sendHubCommand(response(encap(zwave.basicV1.basicGet(),i)))
	}
}

// Called when the preferences of an installed app are updated. Typically unsubscribes and re-subscribes to Events from the configured devices and unschedules/reschedules jobs.
def updated() {
	log.info("${device.displayName} - Executing update()")
	if ( state.lastUpdated && (now() - state.lastUpdated) < 500 ) return
	else initialize()
}

// Standard helper method to handle common checks during installed() and updated() lifecycle events
def initialize() {
	log.info("${device.displayName} - Executing initialize()")
	if (!childDevices) {
		createChildDevices()
	}
	runIn(3, "syncStart")
	state.lastUpdated = now()
}

// Called when an app is uninstalled. Does not need to be declared unless you have some external cleanup to do. subscriptions and scheduled jobs are automatically removed when an app is uninstalled, so you don’t need to do that here.
def uninstalled(){
	log.info("${device.displayName} - Executing uninstalled()")
}

/*
############################
## Private Helper Methods ##
############################
*/

private createChildDevices() {
	log.info("${device.displayName} - Executing createChildDevices()")
	def endpointCount = state.endpointCount
    log.info("${device.displayName} - Number of endpoints: ${endpointCount}")
	//Creating child devices
	if (endpointCount > 1) {
		for (i in 2..endpointCount) {
			try {
				log.info("${device.displayName} - Adding child device ${i}")
				state.endpointMap.put("${device.deviceNetworkId}-${i}",i)
				addChildDevice("WYFY TOUCH ZWAVE SWITCH CHILD", "${device.deviceNetworkId}-${i}", device.hubId, [completedSetup: true, label: "WYFY Touch S${i}", isComponent: false])
			} catch (Exception e) {
				log.error("${device.displayName} - error attempting to create child device: ${i}. Caught exception", e)
			}
		}
	}
}

def syncStart() {
	log.info("${device.displayName} - Executing syncStart()")
	boolean syncNeeded = false
	parameterMap().each {
		if(settings."$it.key" != null) {
			if (state."$it.key" == null) { state."$it.key" = [value: null, state: "synced"] }
			if (state."$it.key".value != settings."$it.key" as Integer || state."$it.key".state in ["notSynced","inProgress"]) {
				state."$it.key".value = settings."$it.key" as Integer
				state."$it.key".state = "notSynced"
				syncNeeded = true
			}
		}
	}
	if ( syncNeeded ) {
		log.info("${device.displayName} - starting sync.")
		multiStatusEvent("Sync in progress.", true, true)
		syncNext()
	}
}

private syncNext() {
	log.info("${device.displayName} - Executing syncNext()")
	def cmds = []
	for ( param in parameterMap() ) {
		if ( state."$param.key"?.value != null && state."$param.key"?.state in ["notSynced","inProgress"] ) {
			multiStatusEvent("Sync in progress. (param: ${param.num})", true)
			state."$param.key"?.state = "inProgress"
			cmds << response(encap(zwave.configurationV2.configurationSet(configurationValue: intToParam(state."$param.key".value, param.size), parameterNumber: param.num, size: param.size)))
			cmds << response(encap(zwave.configurationV2.configurationGet(parameterNumber: param.num)))
			break
		}
	}
	if (cmds) {
		runIn(10, "syncCheck")
		log.debug("cmds!")
		sendHubCommand(cmds,1000)
	} else {
		runIn(1, "syncCheck")
	}
}

def syncCheck() {
	log.info("${device.displayName} - Executing syncCheck()")
	def failed = []
	def incorrect = []
	def notSynced = []
	parameterMap().each {
		if (state."$it.key"?.state == "incorrect" ) {
			incorrect << it
		} else if ( state."$it.key"?.state == "failed" ) {
			failed << it
		} else if ( state."$it.key"?.state in ["inProgress","notSynced"] ) {
			notSynced << it
		}
	}
	if (failed) {
		log.info("${device.displayName} - Sync failed! Check parameter: ${failed[0].num}")
		sendEvent(name: "syncStatus", value: "failed")
		multiStatusEvent("Sync failed! Check parameter: ${failed[0].num}", true, true)
	} else if (incorrect) {
		log.info("${device.displayName} - Sync mismatch! Check parameter: ${incorrect[0].num}")
		sendEvent(name: "syncStatus", value: "incomplete")
		multiStatusEvent("Sync mismatch! Check parameter: ${incorrect[0].num}", true, true)
	} else if (notSynced) {
		log.info("${device.displayName} - Sync incomplete!")
		sendEvent(name: "syncStatus", value: "incomplete")
		multiStatusEvent("Sync incomplete! Open settings and tap Done to try again.", true, true)
	} else {
		log.info("${device.displayName} - Sync Complete")
		sendEvent(name: "syncStatus", value: "synced")
		multiStatusEvent("Sync OK.", true, true)
	}
}

private multiStatusEvent(String statusValue, boolean force = false, boolean display = false) {
	if (!device.currentValue("multiStatus")?.contains("Sync") || device.currentValue("multiStatus") == "Sync OK." || force) {
		sendEvent(name: "multiStatus", value: statusValue, descriptionText: statusValue, displayed: display)
	}
}

private List intToParam(Long value, Integer size = 1) {
	def result = []
	size.times {
		result = result.plus(0, (value & 0xFF) as Short)
		value = (value >> 8)
	}
	return result
}
/*
####################
## Z-Wave Toolkit ##
####################
*/
// Main parser for Z-Wave Events
def parse(String description) {
	log.info("${device.displayName} - Parsing: ${description}")
	def result = []
	if (description.startsWith("Err 106")) {
		result = createEvent(
				descriptionText: "Failed to complete the network security key exchange. If you are unable to receive data from it, you must remove it from your network and add it again.",
				eventType: "ALERT",
				name: "secureInclusion",
				value: "failed",
				displayed: true,
		)
	} else if (description == "updated") {
		return null
	} else {
		def cmd = zwave.parse(description, cmdVersions())
		if (cmd) {
			log.info("${device.displayName} - Parsed: ${cmd}")
			result = zwaveEvent(cmd)
		}
	}
}

// This method receives the MultiChannelCmdEncap 
def zwaveEvent(physicalgraph.zwave.commands.multichannelv3.MultiChannelCmdEncap cmd) {
	def encapsulatedCommand = cmd.encapsulatedCommand(cmdVersions())
	if (encapsulatedCommand) {
		log.info("${device.displayName} - Parsed MultiChannelCmdEncap ${encapsulatedCommand}")
		zwaveEvent(encapsulatedCommand, cmd.sourceEndPoint as Integer)
	} else {
		log.warn("${device.displayName} - Unable to extract MultiChannel command from $cmd")
	}
}

// This method handles the Basic Report command received through the MultiChannelCmdEncap command and updates the child device
def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd, ep=null) {
	log.info("${device.displayName} - BasicReport received, value: ${cmd.value} ep: $ep")
	if (ep == 1) {
		sendEvent([name: "switch", value: (cmd.value == 0) ? "off":"on"])
	} else  {
		def childDevice = childDevices.find{ it.deviceNetworkId == "${device.deviceNetworkId}-${ep}" }
		if (childDevice) {
		    childDevice.sendEvent([name: "switch", value: (cmd.value == 0) ? "off":"on"])
		} else {
			log.error("${device.displayName} - Invalid ep: $ep")
		}
	}
}

// Not in use
def zwaveEvent(physicalgraph.zwave.commands.switchbinaryv1.SwitchBinaryReport cmd, ep=null) {
	log.info("${device.displayName} - SwitchBinaryReport received, value: ${cmd.value} ep: $ep")
}

def zwaveEvent(physicalgraph.zwave.commands.configurationv1.ConfigurationReport cmd) {
	def paramKey = parameterMap().find( {it.num == cmd.parameterNumber } ).key
	log.info("${device.displayName} - Parameter ${paramKey} value is ${cmd.scaledConfigurationValue} expected " + state."$paramKey".value)
	state."$paramKey".state = (state."$paramKey".value == cmd.scaledConfigurationValue) ? "synced" : "incorrect"
	syncNext()
}

// This method handles unexpected commands
def zwaveEvent(physicalgraph.zwave.Command cmd) {
	// Handles all Z-Wave commands we aren't interested in
	log.warn("${device.displayName} - Unhandled: ${cmd.toString()}")
	[:]
}

private secEncap(physicalgraph.zwave.Command cmd) {
	log.info("${device.displayName} - encapsulating command using Secure Encapsulation, command: $cmd")
	zwave.securityV1.securityMessageEncapsulation().encapsulate(cmd).format()
}

private crcEncap(physicalgraph.zwave.Command cmd) {
	log.info("${device.displayName} - encapsulating command using CRC16 Encapsulation, command: $cmd")
	zwave.crc16EncapV1.crc16Encap().encapsulate(cmd).format()
}

private multiEncap(physicalgraph.zwave.Command cmd, Integer ep) {
	log.info("${device.displayName} - encapsulating command using MultiChannel Encapsulation, ep: $ep, command: $cmd")
	zwave.multiChannelV3.multiChannelCmdEncap(destinationEndPoint:ep).encapsulate(cmd)
}

private encap(physicalgraph.zwave.Command cmd, Integer ep) {
	encap(multiEncap(cmd, ep))
}

private encap(List encapList) {
	encap(encapList[0], encapList[1])
}

private encap(Map encapMap) {
	encap(encapMap.cmd, encapMap.ep)
}

private encap(physicalgraph.zwave.Command cmd) {
	if (zwaveInfo.zw.contains("s")) {
		secEncap(cmd)
	} else if (zwaveInfo?.cc?.contains("56")){
		crcEncap(cmd)
	} else {
		log.info("${device.displayName} - no encapsulation supported for command: $cmd")
		cmd.format()
	}
}

private encapSequence(cmds, Integer delay=250) {
	delayBetween(cmds.collect{ encap(it) }, delay)
}

private encapSequence(cmds, Integer delay, Integer ep) {
	delayBetween(cmds.collect{ encap(it, ep) }, delay)
}

/*
##########################
## Device Configuration ##
##########################
*/
/*
List of command classes supported by WYFY Touch Z-Wave Switch
0x5E - ZWave Plus Info
0x85 - Association
0x59 - Association Group Info
0x8E - Multi Channel Association
0x60 - Multi Channel
0x55 - Transport Service
0x86 - Version
0x72 - Manufacturer Specific
0x5A - Device Reset Locally
0x73 - Powerlevel
0x25 - Binary Switch
0x27 - All Switch (Obsolete)
0x70 - Configuration
0x2C - Scene Actuator Configuration
0x2B - Scene Activation
0x5B - Central Scene
0x20 - Basic
0x7A - Firmware Update Meta Data
0x26 - Multilevel Switch
ccOut: 5B,20,26
*/
private Map cmdVersions() {
	[0x5E: 1, 0x85: 1,  0x59: 1, 0x8E: 2, 0x60: 3, 0x55: 1, 0x86: 1, 0x72: 1, 0x5A: 1, 0x73: 1, 0x25: 1, 0x27: 1, 0x70: 1, 0x2C: 1, 0x2B: 1, 0x5B: 1, 0x20: 1, 0x7A: 1] //WYFY TOUCH ZWAVE 4G
}


private parameterMap() {[
		[key: "State before power failure", num: 2, size: 1, type: "enum", options: [ 
				0: "Not saved. Switches will be off when powered is restored.",
				1: "Saved. Switches will return to last state when power is restored."
		], def: "1", title: "Parameter 2", descr: "This parameter determines if the switches will return to its state prior to power failure after power is restored" ],
		[key: "LED panel brightness level", num: 4, size: 1, type: "enum", options: [ 
				0: "LED disabled",
				1: "Level 1",
				2: "Level 2",
				3: "Level 3",
				4: "Level 4",
				5: "Level 5",
				6: "Level 6",
				7: "Level 7",
				8: "Level 8",
				9: "Level 9",
				10: "Level 10"				
		], def: "10", title: "Parameter 4", descr: "This parameter determines the brightness of the LED backlight" ],
		[key: "Pulse duration", num: 6, size: 2, type: "enum", options: [
				0: "Infinite",
				1: "1 s",
				5: "5 s",
				10: "10 s",
				20: "20 s",
				30: "30 s",
				40: "44 s",
				50: "50 s",
				60: "60 s",
				70: "70 s",
				80: "80 s",
				90: "90 s",
				100: "100 s",
				300: "300 s",
				600: "600 s",
				6000: "6000 s",
				60000: "60000 s"
		], def: 0, min: 1, max: 65535, title: "Parameter 6", descr: "This parameter defines the time period to automatically revert a switch that is configured in flashing mode."],
		[key: "Switch 1 Mode", num: 10, size: 1, type: "enum", options: [
				0: "Single click to switch on/off",
				1: "Turn off automatically after the time period defined in parameter 6.",
				2: "Turn on automatically after the time period defined in parameter 6.",
				3: "Hold >3s to turn on. Upon release, it will off.",
				4: "Single click to turn on/off. Hold >3s to turn on and upon release, it will off.",
				5: "Hold to turn on and release to off.",
				6: "Hold >3s to change state."
		], def: 0, title: "Parameter 10", descr: "This parameter defines the mode for switch 1."],
		[key: "Switch 2 Mode (if applicable)", num: 11, size: 1, type: "enum", options: [
				0: "Single click to switch on/off",
				1: "Turn off automatically after the time period defined in parameter 6.",
				2: "Turn on automatically after the time period defined in parameter 6.",
				3: "Hold >3s to turn on. Upon release, it will off.",
				4: "Single click to turn on/off. Hold >3s to turn on and upon release, it will off.",
				5: "Hold to turn on and release to off.",
				6: "Hold >3s to change state."
		], def: 0, title: "Parameter 11", descr: "This parameter defines the mode for switch 2 (if applicable)."],
		[key: "Switch 3 Mode (if applicable)", num: 12, size: 1, type: "enum", options: [
				0: "Single click to switch on/off",
				1: "Turn off automatically after the time period defined in parameter 6.",
				2: "Turn on automatically after the time period defined in parameter 6.",
				3: "Hold >3s to turn on. Upon release, it will off.",
				4: "Single click to turn on/off. Hold >3s to turn on and upon release, it will off. ",
				5: "Hold to turn on and release to off.",
				6: "Hold >3s to change state."
		], def: 0, title: "Parameter 12", descr: "This parameter defines the mode for switch 3 (if applicable)."],
		[key: "Switch 4 Mode (if applicable)", num: 13, size: 1, type: "enum", options: [
				0: "Single click to switch on/off",
				1: "Turn off automatically after the time period defined in parameter 6.",
				2: "Turn on automatically after the time period defined in parameter 6.",
				3: "Hold >3s to turn on. Upon release, it will off.",
				4: "Single click to turn on/off. Hold >3s to turn on and upon release, it will off. ",
				5: "Hold to turn on and release to off.",
				6: "Hold >3s to change state."
		], def: 0, title: "Parameter 13", descr: "This parameter defines the mode for switch 4 (if applicable)."],
		[key: "Physical control", num: 14, size: 1, type: "enum", options: [
				0: "Yes",
				15: "No"
		], def: 0, title: "Parameter 14", descr: "This parameter defines if the switches can be controlled physically."],
		[key: "Wireless control", num: 15, size: 1, type: "enum", options: [
				0: "Yes",
				15: "No"
		], def: 0, title: "Parameter 15", descr: "This parameter defines if the switches can be controlled wirelessly via Z-Wave."]
]}