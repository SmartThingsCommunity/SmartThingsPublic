/**
 *	Fibaro Double Switch 2
 */
metadata {
	definition (name: "Fibaro Double Switch 2 ZW5", namespace: "FibarGroup", author: "Fibar Group", mnmn: "SmartThings", vid:"generic-switch-power-energy") {
		capability "Switch"
		capability "Energy Meter"
		capability "Power Meter"
		capability "Button"
		capability "Configuration"
		capability "Health Check"
		capability "Refresh"

		command "reset"

		fingerprint mfr: "010F", prod: "0203", model: "2000", deviceJoinName: "Fibaro Switch"
		fingerprint mfr: "010F", prod: "0203", model: "1000", deviceJoinName: "Fibaro Switch"
		fingerprint mfr: "010F", prod: "0203", model: "3000", deviceJoinName: "Fibaro Switch"
	  }

	tiles (scale: 2) {
		multiAttributeTile(name:"switch", type: "lighting", width: 3, height: 4){
			tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
				attributeState "off", label: '${name}', action: "switch.on", icon: "https://s3-eu-west-1.amazonaws.com/fibaro-smartthings/switch/switch_2.png", backgroundColor: "#ffffff"
				attributeState "on", label: '${name}', action: "switch.off", icon: "https://s3-eu-west-1.amazonaws.com/fibaro-smartthings/switch/switch_1.png", backgroundColor: "#00a0dc"
			}
			tileAttribute("device.multiStatus", key:"SECONDARY_CONTROL") {
				attributeState("multiStatus", label:'${currentValue}')
			}
		}
		valueTile("power", "device.power", decoration: "flat", width: 2, height: 2) {
			state "power", label:'${currentValue}\n W', action:"refresh"
		}
		valueTile("energy", "device.energy", decoration: "flat", width: 2, height: 2) {
			state "energy", label:'${currentValue}\n kWh', action:"refresh"
		}
		valueTile("reset", "device.energy", decoration: "flat", width: 2, height: 2) {
			state "reset", label:'reset\n kWh', action:"reset"
		}

		main(["switch","power","energy"])
		details(["switch","power","energy","reset"])
	}

	preferences {
		parameterMap().each {
			input (
					title: "${it.num}. ${it.title}",
					description: it.descr,
					type: "paragraph",
					element: "paragraph"
			)

			input (
					name: it.key,
					title: null,
					description: "Default: $it.def" ,
					type: it.type,
					options: it.options,
					range: (it.min != null && it.max != null) ? "${it.min}..${it.max}" : null,
					defaultValue: it.def,
					required: false
			)
		}

		input ( name: "logging", title: "Logging", type: "boolean", required: false )
	}
}


def on(){
	def cmds = []
	cmds << [zwave.basicV1.basicSet(value: 255), 1]
	cmds << [zwave.switchBinaryV1.switchBinaryGet(),1]
	encapSequence(cmds, 5000)
}

def off(){
	def cmds = []
	cmds << [zwave.basicV1.basicSet(value: 0), 1]
	cmds << [zwave.switchBinaryV1.switchBinaryGet(),1]
	encapSequence(cmds, 5000)
}

def childOn() {
	sendHubCommand(response(encap(zwave.basicV1.basicSet(value: 255),2)))
	sendHubCommand(response(encap(zwave.switchBinaryV1.switchBinaryGet(),2)))
}

def childOff() {
	sendHubCommand(response(encap(zwave.basicV1.basicSet(value: 0),2)))
	sendHubCommand(response(encap(zwave.switchBinaryV1.switchBinaryGet(),2)))
}

def reset() {
	def cmds = []
	cmds << [zwave.meterV3.meterReset(), 1]
	cmds << [zwave.meterV3.meterGet(scale: 0), 1]
	cmds << [zwave.meterV3.meterGet(scale: 2), 1]
	encapSequence(cmds,1000)
}

def childReset() {
	def cmds = []
	cmds << response(encap(zwave.meterV3.meterReset(), 2))
	cmds << response(encap(zwave.meterV3.meterGet(scale: 0), 2))
	cmds << response(encap(zwave.meterV3.meterGet(scale: 2), 2))
	sendHubCommand(cmds,1000)
}

def refresh() {
	def cmds = []
	cmds << [zwave.meterV3.meterGet(scale: 0), 1]
	cmds << [zwave.meterV3.meterGet(scale: 2), 1]
	cmds << [zwave.switchBinaryV1.switchBinaryGet(),1]
	encapSequence(cmds,1000)
}

def childRefresh() {
	def cmds = []
	cmds << response(encap(zwave.meterV3.meterGet(scale: 0), 2))
	cmds << response(encap(zwave.meterV3.meterGet(scale: 2), 2))
	cmds << response(encap(zwave.switchBinaryV1.switchBinaryGet(), 2))
	sendHubCommand(cmds,1000)
}

def installed(){
	sendEvent(name: "checkInterval", value: 1920, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])
	initialize()
	response(refresh())
}

def ping() {
	refresh()
}

//Configuration and synchronization
def updated() {
	if ( state.lastUpdated && (now() - state.lastUpdated) < 500 ) return
	def cmds = initialize()
	if (device.label != state.oldLabel) {
		childDevices.each {
			def newLabel = "${device.displayName} - USB"
			it.setLabel(newLabel)
		}
		state.oldLabel = device.label
	}
	return cmds
}

def initialize() {
	def cmds = []
	logging("${device.displayName} - Executing initialize()","info")
	if (!childDevices) {
		createChildDevices()
	}
	if (device.currentValue("numberOfButtons") != 6) { sendEvent(name: "numberOfButtons", value: 6) }

	cmds << zwave.multiChannelAssociationV2.multiChannelAssociationGet(groupingIdentifier: 1) //verify if group 1 association is correct  
	runIn(3, "syncStart")
	state.lastUpdated = now()
	response(encapSequence(cmds,1000))
}

def syncStart() {
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
		logging("${device.displayName} - starting sync.", "info")
		multiStatusEvent("Sync in progress.", true, true)
		syncNext()
	}
}

private syncNext() {
	logging("${device.displayName} - Executing syncNext()","info")
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
		log.debug "cmds!"
		sendHubCommand(cmds,1000)
	} else {
		runIn(1, "syncCheck")
	}
}

def syncCheck() {
	logging("${device.displayName} - Executing syncCheck()","info")
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
		logging("${device.displayName} - Sync failed! Check parameter: ${failed[0].num}","info")
		sendEvent(name: "syncStatus", value: "failed")
		multiStatusEvent("Sync failed! Check parameter: ${failed[0].num}", true, true)
	} else if (incorrect) {
		logging("${device.displayName} - Sync mismatch! Check parameter: ${incorrect[0].num}","info")
		sendEvent(name: "syncStatus", value: "incomplete")
		multiStatusEvent("Sync mismatch! Check parameter: ${incorrect[0].num}", true, true)
	} else if (notSynced) {
		logging("${device.displayName} - Sync incomplete!","info")
		sendEvent(name: "syncStatus", value: "incomplete")
		multiStatusEvent("Sync incomplete! Open settings and tap Done to try again.", true, true)
	} else {
		logging("${device.displayName} - Sync Complete","info")
		sendEvent(name: "syncStatus", value: "synced")
		multiStatusEvent("Sync OK.", true, true)
	}
}

private multiStatusEvent(String statusValue, boolean force = false, boolean display = false) {
	if (!device.currentValue("multiStatus")?.contains("Sync") || device.currentValue("multiStatus") == "Sync OK." || force) {
		sendEvent(name: "multiStatus", value: statusValue, descriptionText: statusValue, displayed: display)
	}
}

def zwaveEvent(physicalgraph.zwave.commands.configurationv2.ConfigurationReport cmd) {
	def paramKey = parameterMap().find( {it.num == cmd.parameterNumber } ).key
	logging("${device.displayName} - Parameter ${paramKey} value is ${cmd.scaledConfigurationValue} expected " + state."$paramKey".value, "info")
	state."$paramKey".state = (state."$paramKey".value == cmd.scaledConfigurationValue) ? "synced" : "incorrect"
	syncNext()
}

private createChildDevices() {
	logging("${device.displayName} - executing createChildDevices()","info")
	state.oldLabel = device.label
	try {
	log.debug "adding child device ....."
		addChildDevice(
			"Fibaro Double Switch 2 - USB",
			"${device.deviceNetworkId}-2",
			device.hubId,
			[completedSetup: true,
			 label: "${device.displayName} (CH2)",
			 isComponent: false]
		)
	} catch (Exception e) {
		logging("${device.displayName} - error attempting to create child device: "+e, "debug")
	}
}

private getChild(Integer childNum) {
	return childDevices.find({ it.deviceNetworkId == "${device.deviceNetworkId}-${childNum}" })
}

def zwaveEvent(physicalgraph.zwave.commands.applicationstatusv1.ApplicationRejectedRequest cmd) {
	logging("${device.displayName} - rejected request!","warn")
	for ( param in parameterMap() ) {
		if ( state."$param.key"?.state == "inProgress" ) {
			state."$param.key"?.state = "failed"
			break
		}
	}
}

def zwaveEvent(physicalgraph.zwave.commands.multichannelassociationv2.MultiChannelAssociationReport cmd) {
	def cmds = []
	if (cmd.groupingIdentifier == 1) {
		if (cmd.nodeId != [0, zwaveHubNodeId, 1]) {
			log.debug "${device.displayName} - incorrect MultiChannel Association for Group 1! nodeId: ${cmd.nodeId} will be changed to [0, ${zwaveHubNodeId}, 1]"
			cmds << zwave.multiChannelAssociationV2.multiChannelAssociationRemove(groupingIdentifier: 1)
			cmds << zwave.multiChannelAssociationV2.multiChannelAssociationSet(groupingIdentifier: 1, nodeId: [0,zwaveHubNodeId,1])
		} else {
			logging("${device.displayName} - MultiChannel Association for Group 1 correct.","info")
		}
	}
	if (cmds) { [response(encapSequence(cmds, 1000))] }
}

//event handlers
def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd, ep=null) {
	log.debug "BasicReport - "+cmd
	//ignore
}

def zwaveEvent(physicalgraph.zwave.commands.switchbinaryv1.SwitchBinaryReport cmd, ep=null) {
	logging("${device.displayName} - SwitchBinaryReport received, value: ${cmd.value} ep: $ep","info")
	switch (ep) {
		case 1:
			sendEvent([name: "switch", value: (cmd.value == 0 ) ? "off": "on"])
			break
		case 2:
			getChild(2)?.sendEvent([name: "switch", value: (cmd.value == 0 ) ? "off": "on"])
			break
		default:
			def cmds = []
			log.debug "-------> Requesting switch report..."
			//cmds << response(encap(zwave.switchBinaryV1.switchBinaryGet(), 2))
			cmds << response(encap(zwave.switchBinaryV1.switchBinaryGet(), 1))
			cmds << response(encap(zwave.switchBinaryV1.switchBinaryGet(), 2))
			sendHubCommand(cmds,500)
	}
}

def zwaveEvent(physicalgraph.zwave.commands.meterv3.MeterReport cmd, ep=null) {
	logging("${device.displayName} - MeterReport received, value: ${cmd.scaledMeterValue} scale: ${cmd.scale} ep: $ep","info")
	log.debug "cmd: "+cmd
	if (ep==1) {
		switch (cmd.scale) {
			case 0:
				sendEvent([name: "energy", value: cmd.scaledMeterValue, unit: "kWh"])
				break
			case 2:
				sendEvent([name: "power", value: cmd.scaledMeterValue, unit: "W"])
				break
		}
		multiStatusEvent("${(device.currentValue("power") ?: "0.0")} W | ${(device.currentValue("energy") ?: "0.00")} kWh")

	} else if (ep==2) {
		switch (cmd.scale) {
			case 0:
				getChild(2)?.sendEvent([name: "energy", value: cmd.scaledMeterValue, unit: "kWh"])
				break
			case 2:
				getChild(2)?.sendEvent([name: "power", value: cmd.scaledMeterValue, unit: "W"])
				break
		}
		getChild(2)?.sendEvent([name: "combinedMeter", value: "${(getChild(2)?.currentValue("power") ?: "0.0")} W / ${(getChild(2)?.currentValue("energy") ?: "0.00")} kWh", displayed: false])
	} else if (!ep) {
		log.debug "-------> Requesting specific reports..."
		def cmds = []
		cmds << response(encap(zwave.meterV3.meterGet(scale: 0), 2))
		cmds << response(encap(zwave.meterV3.meterGet(scale: 2), 2))
		cmds << response(encap(zwave.meterV3.meterGet(scale: 0), 1))
		cmds << response(encap(zwave.meterV3.meterGet(scale: 2), 1))
		sendHubCommand(cmds,500)
	}
}

def zwaveEvent(physicalgraph.zwave.commands.centralscenev1.CentralSceneNotification cmd) {
	logging("${device.displayName} - CentralSceneNotification received, sceneNumber: ${cmd.sceneNumber} keyAttributes: ${cmd.keyAttributes}","info")
	log.info cmd
	def String action
	def Integer button
	switch (cmd.keyAttributes as Integer) {
		case 0: action = "pushed"; button = cmd.sceneNumber; break
		case 1: action = "released"; button = cmd.sceneNumber; break
		case 2: action = "held"; button = cmd.sceneNumber; break
		case 3: action = "pushed"; button = 2+(cmd.sceneNumber as Integer); break
		case 4: action = "pushed"; button = 4+(cmd.sceneNumber as Integer); break
	}
	log.info "button $button $action"
	sendEvent(name: "button", value: action, data: [buttonNumber: button], isStateChange: true)
}

/*
####################
## Z-Wave Toolkit ##
####################
*/
def parse(String description) {
	def result = []
	logging("${device.displayName} - Parsing: ${description}")
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
			logging("${device.displayName} - Parsed: ${cmd}")
			zwaveEvent(cmd)
		}
	}
}

def zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityMessageEncapsulation cmd) {
	def encapsulatedCommand = cmd.encapsulatedCommand(cmdVersions())
	if (encapsulatedCommand) {
		logging("${device.displayName} - Parsed SecurityMessageEncapsulation into: ${encapsulatedCommand}")
		zwaveEvent(encapsulatedCommand)
	} else {
		log.warn "Unable to extract Secure command from $cmd"
	}
}

def zwaveEvent(physicalgraph.zwave.commands.crc16encapv1.Crc16Encap cmd) {
	def version = cmdVersions()[cmd.commandClass as Integer]
	def ccObj = version ? zwave.commandClass(cmd.commandClass, version) : zwave.commandClass(cmd.commandClass)
	def encapsulatedCommand = ccObj?.command(cmd.command)?.parse(cmd.data)
	if (encapsulatedCommand) {
		logging("${device.displayName} - Parsed Crc16Encap into: ${encapsulatedCommand}")
		zwaveEvent(encapsulatedCommand)
	} else {
		log.warn "Unable to extract CRC16 command from $cmd"
	}
}

def zwaveEvent(physicalgraph.zwave.commands.multichannelv3.MultiChannelCmdEncap cmd) {
	if (cmd.commandClass == 0x6C && cmd.parameter.size >= 4) { // Supervision encapsulated Message
		// Supervision header is 4 bytes long, two bytes dropped here are the latter two bytes of the supervision header
		cmd.parameter = cmd.parameter.drop(2)
		// Updated Command Class/Command now with the remaining bytes
		cmd.commandClass = cmd.parameter[0]
		cmd.command = cmd.parameter[1]
		cmd.parameter = cmd.parameter.drop(2)
	}
	def encapsulatedCommand = cmd.encapsulatedCommand(cmdVersions())
	if (encapsulatedCommand) {
		logging("${device.displayName} - Parsed MultiChannelCmdEncap ${encapsulatedCommand}")
		zwaveEvent(encapsulatedCommand, cmd.sourceEndPoint as Integer)
	} else {
		log.warn "Unable to extract MultiChannel command from $cmd"
	}
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
	// Handles all Z-Wave commands we aren't interested in
	log.debug "Unhandled: ${cmd.toString()}"
	[:]
}

private logging(text, type = "debug") {
	if (settings.logging == "true") {
		log."$type" text
	}
}

private secEncap(physicalgraph.zwave.Command cmd) {
	logging("${device.displayName} - encapsulating command using Secure Encapsulation, command: $cmd","info")
	zwave.securityV1.securityMessageEncapsulation().encapsulate(cmd).format()
}

private crcEncap(physicalgraph.zwave.Command cmd) {
	logging("${device.displayName} - encapsulating command using CRC16 Encapsulation, command: $cmd","info")
	zwave.crc16EncapV1.crc16Encap().encapsulate(cmd).format()
}

private multiEncap(physicalgraph.zwave.Command cmd, Integer ep) {
	logging("${device.displayName} - encapsulating command using MultiChannel Encapsulation, ep: $ep command: $cmd","info")
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
		logging("${device.displayName} - no encapsulation supported for command: $cmd","info")
		cmd.format()
	}
}

private encapSequence(cmds, Integer delay=250) {
	delayBetween(cmds.collect{ encap(it) }, delay)
}

private encapSequence(cmds, Integer delay, Integer ep) {
	delayBetween(cmds.collect{ encap(it, ep) }, delay)
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
##########################
## Device Configuration ##
##########################
*/
private Map cmdVersions() {
	[0x5E: 1, 0x86: 1, 0x72: 1, 0x59: 1, 0x73: 1, 0x22: 1, 0x56: 1, 0x32: 3, 0x71: 1, 0x98: 1, 0x7A: 1, 0x25: 1, 0x5A: 1, 0x85: 2, 0x70: 2, 0x8E: 2, 0x60: 3, 0x75: 1, 0x5B: 1] //Fibaro Double Switch 2
}

private parameterMap() {[
		[key: "restoreState", num: 9, size: 1, type: "enum", options: [
				0: "power off after power failure",
				1: "restore state"
		], def: "1", title: "Restore state after power failure",
		 descr: "This parameter determines if the device will return to state prior to the power failure after power is restored"],
		[key: "ch1operatingMode", num: 10, size: 1, type: "enum", options: [
				0: "standard operation",
				1: "delay ON",
				2: "delay OFF",
				3: "auto ON",
				4: "auto OFF",
				5: "flashing mode"
		], def: "0", title: "First channel - Operating mode",
		 descr: "This parameter allows to choose operating for the 1st channel controlled by the S1 switch."],
		[key: "ch1reactionToSwitch", num: 11, size: 1, type: "enum", options: [
				0: "cancel and set target state",
				1: "no reaction",
				2: "reset timer"
		], def: "0", title: "Reaction to switch for delay/auto ON/OFF modes",
		 descr: "This parameter determines how the device in timed mode reacts to pushing the switch connected to the S1 terminal."],
		[key: "ch1timeParameter", num: 12, size: 2, type: "number", def: 50, min: 0, max: 32000, title: "First channel - Time parameter for delay/auto ON/OFF modes",
		 descr: "This parameter allows to set time parameter used in timed modes. (1-32000s)"],
		[key: "ch1pulseTime", num: 13, size: 2, type: "enum", options: [
				1: "0.1 s",
				5: "0.5 s",
				10: "1 s",
				20: "2 s",
				30: "3 s",
				40: "4 s",
				50: "5 s",
				60: "6 s",
				70: "7 s",
				80: "8 s",
				90: "9 s",
				100: "10 s",
				300: "30 s",
				600: "60 s",
				6000: "600 s"
		], def: 5, min: 1, max: 32000, title: "First channel - Pulse time for flashing mode",
		 descr: "This parameter allows to set time of switching to opposite state in flashing mode."],
		[key: "ch2operatingMode", num: 15, size: 1, type: "enum", options: [
				0: "standard operation",
				1: "delay ON",
				2: "delay OFF",
				3: "auto ON",
				4: "auto OFF",
				5: "flashing mode"
		], def: "0", title: "Second channel - Operating mode",
		 descr: "This parameter allows to choose operating for the 1st channel controlled by the S2 switch."],
		[key: "ch2reactionToSwitch", num: 16, size: 1, type: "enum", options: [
				0: "cancel and set target state",
				1: "no reaction",
				2: "reset timer"
		], def: "0", title: "Second channel - Restore state after power failure",
		 descr: "This parameter determines how the device in timed mode reacts to pushing the switch connected to the S2 terminal."],
		[key: "ch2timeParameter", num: 17, size: 2, type: "number", def: 50, min: 0, max: 32000, title: "Second channel - Time parameter for delay/auto ON/OFF modes",
		 descr: "This parameter allows to set time parameter used in timed modes."],
		[key: "ch2pulseTime", num: 18, size: 2, type: "enum", options: [
				1: "0.1 s",
				5: "0.5 s",
				10: "1 s",
				20: "2 s",
				30: "3 s",
				40: "4 s",
				50: "5 s",
				60: "6 s",
				70: "7 s",
				80: "8 s",
				90: "9 s",
				100: "10 s",
				300: "30 s",
				600: "60 s",
				6000: "600 s"
		], def: 5, min: 1, max: 32000, title: "Second channel - Pulse time for flashing mode",
		 descr: "This parameter allows to set time of switching to opposite state in flashing mode."],
		[key: "switchType", num: 20, size: 1, type: "enum", options: [
				0: "momentary switch",
				1: "toggle switch (contact closed - ON, contact opened - OFF)",
				2: "toggle switch (device changes status when switch changes status)"
		], def: "2", title: "Switch type",
		 descr: "Parameter defines as what type the device should treat the switch connected to the S1 and S2 terminals"],
		[key: "flashingReports", num: 21, size: 1, type: "enum", options: [
				0: "do not send reports",
				1: "sends reports"
		], def: "0", title: "Flashing mode - reports",
		 descr: "This parameter allows to define if the device sends reports during the flashing mode."],
		[key: "s1scenesSent", num: 28, size: 1, type: "enum", options: [
				0: "do not send scenes",
				1: "key pressed 1 time",
				2: "key pressed 2 times",
				3: "key pressed 1 & 2 times",
				4: "key pressed 3 times",
				5: "key pressed 1 & 3 times",
				6: "key pressed 2 & 3 times",
				7: "key pressed 1, 2 & 3 times",
				8: "key held & released",
				9: "key Pressed 1 time & held",
				10: "key pressed 2 times & held",
				11: "key pressed 1, 2 times & held",
				12: "key pressed 3 times & held",
				13: "key pressed 1, 3 times & held",
				14: "key pressed 2, 3 times & held",
				15: "key pressed 1, 2, 3 times & held"
		], def: "0", title: "Switch 1 - scenes sent",
		 descr: "This parameter determines which actions result in sending scene IDs assigned to them."],
		[key: "s2scenesSent", num: 29, size: 1, type: "enum", options: [
				0: "do not send scenes",
				1: "key pressed 1 time",
				2: "key pressed 2 times",
				3: "key pressed 1 & 2 times",
				4: "key pressed 3 times",
				5: "key pressed 1 & 3 times",
				6: "key pressed 2 & 3 times",
				7: "key pressed 1, 2 & 3 times",
				8: "key held & released",
				9: "key Pressed 1 time & held",
				10: "key pressed 2 times & held",
				11: "key pressed 1, 2 times & held",
				12: "key pressed 3 times & held",
				13: "key pressed 1, 3 times & held",
				14: "key pressed 2, 3 times & held",
				15: "key pressed 1, 2, 3 times & held"
		], def: "0", title: "Switch 2 - scenes sent",
		 descr: "This parameter determines which actions result in sending scene IDs assigned to them."],
		[key: "ch1energyReports", num: 53, size: 2, type: "enum", options: [
				1: "0.01 kWh",
				10: "0.1 kWh",
				50: "0.5 kWh",
				100: "1 kWh",
				500: "5 kWh",
				1000: "10 kWh"
		], def: 100, min: 0, max: 32000, title: "First channel - energy reports",
		 descr: "This parameter determines the min. change in consumed power that will result in sending power report"],
		[key: "ch2energyReports", num: 57, size: 2, type: "enum", options: [
				1: "0.01 kWh",
				10: "0.1 kWh",
				50: "0.5 kWh",
				100: "1 kWh",
				500: "5 kWh",
				1000: "10 kWh"
		], def: 100, min: 0, max: 32000, title: "Second channel - energy reports",
		 descr: "This parameter determines the min. change in consumed power that will result in sending power report"],
		[key: "periodicPowerReports", num: 58, size: 2, type: "enum", options: [
				1: "1 s",
				5: "5 s",
				10: "10 s",
				600: "600 s",
				3600: "3600 s",
				32000: "32000 s"
		], def: 3600, min: 0, max: 32000, title: "Periodic power reports",
		 descr: "This parameter defines in what time interval the periodic power reports are sent"],
		[key: "periodicEnergyReports", num: 59, size: 2, type: "enum", options: [
				1: "1 s",
				5: "5 s",
				10: "10 s",
				600: "600 s",
				3600: "3600 s",
				32000: "32000 s"
		], def: 3600, min: 0, max: 32000, title: "Periodic energy reports",
		 descr: "This parameter determines in what time interval the periodic Energy reports are sent"]
]}
