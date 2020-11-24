/**
 *  Copyright 2018 SRPOL
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *		http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */
metadata {
	definition (name: "Z-Wave Multi Metering Switch", namespace: "smartthings", author: "SmartThings", mnmn: "SmartThings", vid: "generic-switch-power-energy", genericHandler: "Z-Wave") {
		capability "Zw Multichannel"
		capability "Switch"
		capability "Power Meter"
		capability "Energy Meter"
		capability "Refresh"
		capability "Configuration"
		capability "Actuator"
		capability "Sensor"
		capability "Health Check"

		command "reset"

		fingerprint mfr:"0086", prod:"0003", model:"0084", deviceJoinName: "Aeotec Switch 1" //Aeotec Nano Switch 1
		fingerprint mfr:"0086", prod:"0103", model:"0084", deviceJoinName: "Aeotec Switch 1" //Aeotec Nano Switch 1
		fingerprint mfr:"0086", prod:"0203", model:"0084", deviceJoinName: "Aeotec Switch 1" //AU //Aeotec Nano Switch 1
		fingerprint mfr: "0000", cc: "0x5E,0x25,0x27,0x32,0x81,0x71,0x60,0x8E,0x2C,0x2B,0x70,0x86,0x72,0x73,0x85,0x59,0x98,0x7A,0x5A", ccOut:"0x82", ui:"0x8700", deviceJoinName: "Aeotec Switch 1" //Aeotec Nano Switch 1
		fingerprint mfr: "027A", prod: "A000", model: "A004", deviceJoinName: "Zooz Switch" //Zooz ZEN Power Strip
		fingerprint mfr: "027A", prod: "A000", model: "A003", deviceJoinName: "Zooz Switch" //Zooz Double Plug
		fingerprint mfr: "015F", prod: "3102", model: "0201", deviceJoinName: "WYFY Switch 1" //WYFY Touch 1-button Switch
		fingerprint mfr: "015F", prod: "3102", model: "0202", deviceJoinName: "WYFY Switch 1" //WYFY Touch 2-button Switch
		fingerprint mfr: "015F", prod: "3102", model: "0204", deviceJoinName: "WYFY Switch 1" //WYFY Touch 4-button Switch
		fingerprint mfr: "015F", prod: "3111", model: "5102", deviceJoinName: "WYFY Switch 1" //WYFY Touch 1-button Switch
		fingerprint mfr: "015F", prod: "3121", model: "5102", deviceJoinName: "WYFY Switch 1" //WYFY Touch 2-button Switch
		fingerprint mfr: "015F", prod: "3141", model: "5102", deviceJoinName: "WYFY Switch 1" //WYFY Touch 4-button Switch
	}

	tiles(scale: 2){
		multiAttributeTile(name:"switch", type: "generic", width: 6, height: 4, canChangeIcon: true){
			tileAttribute("device.switch", key: "PRIMARY_CONTROL") {
				attributeState("on", label: '${name}', action: "switch.off", icon: "st.switches.switch.on", backgroundColor: "#00a0dc")
				attributeState("off", label: '${name}', action: "switch.on", icon: "st.switches.switch.off", backgroundColor: "#ffffff")
			}
		}
		valueTile("power", "device.power", decoration: "flat", width: 2, height: 2) {
			state "default", label:'${currentValue} W'
		}
		valueTile("energy", "device.energy", decoration: "flat", width: 2, height: 2) {
			state "default", label:'${currentValue} kWh'
		}
		standardTile("refresh", "device.power", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "default", label:'', action:"refresh.refresh", icon:"st.secondary.refresh"
		}
		standardTile("reset", "device.energy", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "default", label:'reset kWh', action:"reset"
		}

		main(["switch"])
		details(["switch","power","energy","refresh","reset"])
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

// Called when an instance of the app is installed. Typically subscribes to Events from the configured devices and creates any scheduled jobs.
def installed() {
	log.debug "Installed ${device.displayName} - ZwaveInfo: ${getZwaveInfo()}"
	sendEvent(name: "checkInterval", value: 2 * 15 * 60 + 2 * 60, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID, offlinePingable: "1"])
}

// Called when the preferences of an installed app are updated. Typically unsubscribes and re-subscribes to Events from the configured devices and unschedules/reschedules jobs.
def updated() {
	log.debug "${device.displayName} - Executing update()"
	sendHubCommand encap(zwave.multiChannelV3.multiChannelEndPointGet())
	runIn(3, "syncStart")
}

// Handles the configuration capability's configure command. This command will be called right after the device joins to set device-specific configuration commands.
def configure() {
	log.debug "${device.displayName} - Executing configure()"
	response([
			encap(zwave.multiChannelV3.multiChannelEndPointGet()),
			encap(zwave.manufacturerSpecificV2.manufacturerSpecificGet())
	])
}

// Called when an app is uninstalled. Does not need to be declared unless you have some external cleanup to do. subscriptions and scheduled jobs are automatically removed when an app is uninstalled, so you don’t need to do that here.
def uninstalled(){
	log.info "${device.displayName} - Executing uninstalled()"
}

// Main parser for Z-Wave Events
def parse(String description) {
	log.debug "${device.displayName} - Parsing: ${description}"
	def result = null
	if (description.startsWith("Err")) {
		result = createEvent(descriptionText:description, isStateChange:true)
	} else if (description != "updated") {
		def cmd = zwave.parse(description)
		if (cmd) {
			log.debug "${device.displayName} - Parsed: ${cmd}"
			result = zwaveEvent(cmd, null)
		}
	}
	log.debug "parsed '${description}' to ${result.inspect()}"
	result
}

// cmd.endPoints includes the USB ports but we don't want to expose them as child devices since they cannot be controlled so hardcode to just include the outlets
def zwaveEvent(physicalgraph.zwave.commands.multichannelv3.MultiChannelEndPointReport cmd, ep = null) {
	if(!childDevices) {
		if (isZoozZenStripV2()) {
			addChildSwitches(5)
		} else if (isZoozDoublePlug()) {
			addChildSwitches(2)
		} else {
			addChildSwitches(cmd.endPoints)
		}
	}
	response([
			resetAll(),
			refreshAll()
	])
}

def zwaveEvent(physicalgraph.zwave.commands.manufacturerspecificv2.ManufacturerSpecificReport cmd, ep = null) {
	def mfr = Integer.toHexString(cmd.manufacturerId)
	def model = Integer.toHexString(cmd.productId)
	updateDataValue("mfr", mfr)
	updateDataValue("model", model)
	lateConfigure()
}

private lateConfigure() {
	def cmds = []
	log.debug "Late configuration..."
	switch(getDeviceModel()) {
		case "Aeotec Nano Switch":
			cmds = [
					encap(zwave.configurationV1.configurationSet(parameterNumber: 255, size: 1, configurationValue: [0])),			// resets configuration
					encap(zwave.configurationV1.configurationSet(parameterNumber: 4, size: 1, configurationValue: [1])),			// enables overheat protection
					encap(zwave.configurationV1.configurationSet(parameterNumber: 80, size: 1, configurationValue: [2])),			// send BasicReport CC
					encap(zwave.configurationV1.configurationSet(parameterNumber: 101, size: 4, scaledConfigurationValue: 2048)),	// enabling kWh energy reports on ep 1
					encap(zwave.configurationV1.configurationSet(parameterNumber: 111, size: 4, scaledConfigurationValue: 600)),	// ... every 10 minutes
					encap(zwave.configurationV1.configurationSet(parameterNumber: 102, size: 4, scaledConfigurationValue: 4096)),	// enabling kWh energy reports on ep 2
					encap(zwave.configurationV1.configurationSet(parameterNumber: 112, size: 4, scaledConfigurationValue: 600)),	// ... every 10 minutes
					encap(zwave.configurationV1.configurationSet(parameterNumber: 90, size: 1, scaledConfigurationValue: 1) ),		// enables reporting based on wattage change
					encap(zwave.configurationV1.configurationSet(parameterNumber: 91, size: 2, scaledConfigurationValue: 20))		// report any 20W change
			]
			break
		case "Zooz Switch":
			cmds = [
					encap(zwave.configurationV1.configurationSet(parameterNumber: 2, size: 4, scaledConfigurationValue: 10)),	// makes device report every 5W change
					encap(zwave.configurationV1.configurationSet(parameterNumber: 3, size: 4, scaledConfigurationValue: 600)), // enabling power Wattage reports every 10 minutes
					encap(zwave.configurationV1.configurationSet(parameterNumber: 4, size: 4, scaledConfigurationValue: 600))	// enabling kWh energy reports every 10 minutes
			]
			break
		case "WYFY Touch":
			cmds = [ 
					encap(zwave.configurationV1.configurationSet(parameterNumber: 2, size: 1, scaledConfigurationValue: 1))	// Remebers state before power failure
			]
			break
		default:
			cmds = [encap(zwave.configurationV1.configurationSet(parameterNumber: 255, size: 1, scaledConfigurationValue: 0))]
			break
	}
	sendHubCommand cmds
}

def zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityMessageEncapsulation cmd, ep = null) {
	log.debug "Security Message Encap ${cmd}"
	def encapsulatedCommand = cmd.encapsulatedCommand()
	if (encapsulatedCommand) {
		zwaveEvent(encapsulatedCommand, null)
	} else {
		log.warn "Unable to extract encapsulated cmd from $cmd"
		createEvent(descriptionText: cmd.toString())
	}
}

def zwaveEvent(physicalgraph.zwave.commands.multichannelv3.MultiChannelCmdEncap cmd, ep = null) {
	log.debug "Multichannel command ${cmd}" + (ep ? " from endpoint $ep" : "")
	if (cmd.commandClass == 0x6C && cmd.parameter.size >= 4) { // Supervision encapsulated Message
		// Supervision header is 4 bytes long, two bytes dropped here are the latter two bytes of the supervision header
		cmd.parameter = cmd.parameter.drop(2)
		// Updated Command Class/Command now with the remaining bytes
		cmd.commandClass = cmd.parameter[0]
		cmd.command = cmd.parameter[1]
		cmd.parameter = cmd.parameter.drop(2)
	}
	def encapsulatedCommand = cmd.encapsulatedCommand()
	zwaveEvent(encapsulatedCommand, cmd.sourceEndPoint as Integer)
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd, ep = null) {
	log.debug "${device.displayName} - Basic ${cmd}" + (ep ? " from endpoint $ep" : "")
	handleSwitchReport(ep, cmd)
}

def zwaveEvent(physicalgraph.zwave.commands.switchbinaryv1.SwitchBinaryReport cmd, ep = null) {
	log.debug "Binary ${cmd}" + (ep ? " from endpoint $ep" : "")
	handleSwitchReport(ep, cmd)
}

private handleSwitchReport(endpoint, cmd) {
	def value = cmd.value ? "on" : "off"
	if (isZoozZenStripV2()) {
		// device also sends reports without any endpoint specified, therefore all endpoints must be queried
		// sometimes it also reports 0.0 Wattage only until it's queried for it, then it starts reporting real values
		endpoint ? [changeSwitch(endpoint, value), response(encap(zwave.meterV3.meterGet(scale: 0), endpoint))] : [response(refreshAll(false))]
	}  else {
		endpoint ? changeSwitch(endpoint, value) : []
	}
}

private changeSwitch(endpoint, value) {
	if (endpoint == 1) {
		createEvent(name: "switch", value: value, isStateChange: true, descriptionText: "Switch ${endpoint} is ${value}")
	} else {
		String childDni = "${device.deviceNetworkId}:${endpoint}"
		def child = childDevices.find { it.deviceNetworkId == childDni }
		child?.sendEvent(name: "switch", value: value, isStateChange: true, descriptionText: "Switch ${endpoint} is ${value}")
	}
}

def zwaveEvent(physicalgraph.zwave.commands.meterv3.MeterReport cmd, ep = null) {
	log.debug "Meter ${cmd}" + (ep ? " from endpoint $ep" : "")
	if (ep == 1) {
		createEvent(createMeterEventMap(cmd))
	} else if(ep) {
		String childDni = "${device.deviceNetworkId}:$ep"
		def child = childDevices.find { it.deviceNetworkId == childDni }
		child?.sendEvent(createMeterEventMap(cmd))
	} else {
		def event = createEvent([isStateChange:  false, descriptionText: "Wattage change has been detected. Refreshing each endpoint"])
		isAeotec() ? [event, response(refreshAll())] : event
	}
}

private createMeterEventMap(cmd) {
	def eventMap = [:]
	if (cmd.meterType == 1) {
		if (cmd.scale == 0) {
			eventMap.name = "energy"
			eventMap.value = cmd.scaledMeterValue
			eventMap.unit = "kWh"
		} else if (cmd.scale == 2) {
			eventMap.name = "power"
			eventMap.value = Math.round(cmd.scaledMeterValue)
			eventMap.unit = "W"
		}
	}
	eventMap
}

def zwaveEvent(physicalgraph.zwave.commands.configurationv1.ConfigurationReport cmd) {
	def paramKey = parameterMap().find( {it.num == cmd.parameterNumber } ).key
	log.debug "${device.displayName} - Parameter ${paramKey} value is ${cmd.scaledConfigurationValue} expected " + state."$paramKey".value
	state."$paramKey".state = (state."$paramKey".value == cmd.scaledConfigurationValue) ? "synced" : "incorrect"
	syncNext()
}

def syncStart() {
	log.debug "${device.displayName} - Executing syncStart()"
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
		log.debug "${device.displayName} - starting sync."
		multiStatusEvent("Sync in progress.", true, true)
		syncNext()
	}
}

private syncNext() {
	log.debug "${device.displayName} - Executing syncNext()"
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
	log.debug "${device.displayName} - Executing syncCheck()"
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
		log.debug "${device.displayName} - Sync failed! Check parameter: ${failed[0].num}"
		sendEvent(name: "syncStatus", value: "failed")
		multiStatusEvent("Sync failed! Check parameter: ${failed[0].num}", true, true)
	} else if (incorrect) {
		log.debug "${device.displayName} - Sync mismatch! Check parameter: ${incorrect[0].num}"
		sendEvent(name: "syncStatus", value: "incomplete")
		multiStatusEvent("Sync mismatch! Check parameter: ${incorrect[0].num}", true, true)
	} else if (notSynced) {
		log.debug "${device.displayName} - Sync incomplete!"
		sendEvent(name: "syncStatus", value: "incomplete")
		multiStatusEvent("Sync incomplete! Open settings and tap Done to try again.", true, true)
	} else {
		log.debug "${device.displayName} - Sync Complete"
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

// This method handles unexpected commands
def zwaveEvent(physicalgraph.zwave.Command cmd, ep) {
	// Handles all Z-Wave commands we aren't interested in
	log.warn "${device.displayName} - Unhandled ${cmd}" + (ep ? " from endpoint $ep" : "")
}

def on() {
	onOffCmd(0xFF)
}

def off() {
	onOffCmd(0x00)
}

// The Health Check capability uses the “checkInterval” attribute to determine the maximum number of seconds the device can go without generating new events.
// If the device hasn’t created any events within that amount of time, SmartThings executes the “ping()” command.
// If ping() does not generate any events, SmartThings marks the device as offline. 
def ping() {
	refreshAll()
}

def childOnOff(deviceNetworkId, value) {
	def switchId = getSwitchId(deviceNetworkId)
	if (switchId != null) sendHubCommand onOffCmd(value, switchId)
}

private onOffCmd(value, endpoint = 1) {
	if (isWYFYTouch()) {
		sendHubCommand(response(encap(zwave.basicV1.basicSet(value: value),endpoint)))
		sendHubCommand(response(encap(zwave.basicV1.basicGet(),endpoint)))
	} else {
		delayBetween([
			encap(zwave.basicV1.basicSet(value: value), endpoint),
			encap(zwave.basicV1.basicGet(), endpoint),
			"delay 3000",
			encap(zwave.meterV3.meterGet(scale: 0), endpoint),
			encap(zwave.meterV3.meterGet(scale: 2), endpoint)
		])
	}
}

private refreshAll(includeMeterGet = true) {
	if (isWYFYTouch()) includeMeterGet = false
	def endpoints = [1]
	childDevices.each {
		def switchId = getSwitchId(it.deviceNetworkId)
		if (switchId != null) {
			endpoints << switchId
		}
	}
	sendHubCommand refresh(endpoints,includeMeterGet)
}

def childRefresh(deviceNetworkId, includeMeterGet = true) {
	if (isWYFYTouch()) includeMeterGet = false
	def switchId = getSwitchId(deviceNetworkId)
	if (switchId != null) {
		sendHubCommand refresh([switchId],includeMeterGet)
	}
}

def refresh(endpoints = [1], includeMeterGet = true) {
	def cmds = []
	endpoints.each {
		cmds << [encap(zwave.basicV1.basicGet(), it)]
		if (includeMeterGet) {
			cmds << encap(zwave.meterV3.meterGet(scale: 0), it)
			cmds << encap(zwave.meterV3.meterGet(scale: 2), it)
		}
	}
	delayBetween(cmds, 200)
}

private resetAll() {
	childDevices.each { childReset(it.deviceNetworkId) }
	sendHubCommand reset()
}

def childReset(deviceNetworkId) {
	def switchId = getSwitchId(deviceNetworkId)
	if (switchId != null) {
		log.debug "Child reset switchId: ${switchId}"
		sendHubCommand reset(switchId)
	}
}

def reset(endpoint = 1) {
	log.debug "Resetting endpoint: ${endpoint}"
	delayBetween([
			encap(zwave.meterV3.meterReset(), endpoint),
			encap(zwave.meterV3.meterGet(scale: 0), endpoint),
			"delay 500"
	], 500)
}

def getSwitchId(deviceNetworkId) {
	def split = deviceNetworkId?.split(":")
	return (split.length > 1) ? split[1] as Integer : null
}

private encap(cmd, endpoint = null) {
	if (cmd) {
		if (endpoint) {
			cmd = zwave.multiChannelV3.multiChannelCmdEncap(destinationEndPoint: endpoint).encapsulate(cmd)
		}

		if (zwaveInfo.zw.contains("s")) {
			zwave.securityV1.securityMessageEncapsulation().encapsulate(cmd).format()
		} else {
			cmd.format()
		}
	}
}

private addChildSwitches(numberOfSwitches) {
	log.debug "${device.displayName} - Executing addChildSwitches()"
	for (def endpoint : 2..numberOfSwitches) {
		try {
			String childDni = "${device.deviceNetworkId}:$endpoint"
			def componentLabel = device.displayName[0..-2] + "${endpoint}"
			addChildDevice("Child Metering Switch", childDni, device.getHub().getId(), [
					completedSetup	: true,
					label			: componentLabel,
					isComponent		: false
			])			      

		} catch(Exception e) {
			log.debug "Exception: ${e}"
		}
	}
}

def isAeotec() {
	getDeviceModel() == "Aeotec Nano Switch"
}
def isZoozZenStripV2() {
	zwaveInfo.mfr.equals("027A") && zwaveInfo.model.equals("A004")
}
def isZoozDoublePlug() {
	zwaveInfo.mfr.equals("027A") && zwaveInfo.model.equals("A003")
}
def isWYFYTouch() {
	getDeviceModel() == "WYFY Touch"
}


private getDeviceModel() {
	if ((zwaveInfo.mfr?.contains("0086") && zwaveInfo.model?.contains("0084")) || (getDataValue("mfr") == "86") && (getDataValue("model") == "84")) {
		"Aeotec Nano Switch"
	} else if(zwaveInfo.mfr?.contains("027A")) {
		"Zooz Switch"
	} else if(zwaveInfo.mfr?.contains("015F")) {
		"WYFY Touch"
    } else {
		""
	}
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