/**
 *	Fibaro Wall Plug ZW5
 */
metadata {
	definition (name: "Fibaro Wall Plug US ZW5", namespace: "FibarGroup", author: "Fibar Group", ocfDeviceType: "oic.d.smartplug") {
		capability "Switch"
		capability "Energy Meter"
		capability "Power Meter"
		capability "Configuration"
		capability "Health Check"
		capability "Refresh"

		command "reset"

		fingerprint mfr: "010F", prod: "1401", model: "2000"
		fingerprint deviceId: "0x1001", inClusters: "0x5E, 0x55, 0x98, 0x9F, 0x22, 0x56, 0x7A, 0x6C", outClusters: "0x86, 0x25, 0x85, 0x8E, 0x59, 0x72, 0x5A, 0x73, 0x32, 0x70, 0x6C, 0x71, 0x75, 0x60"

	}

	tiles (scale: 2) {
		multiAttributeTile(name:"switch", type: "lighting", width: 3, height: 4, canChangeIcon: true){
			tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
				attributeState "off", label: 'Off', action: "switch.on", icon: "https://s3-eu-west-1.amazonaws.com/fibaro-smartthings/wallPlugUS/plug_us_off.png", backgroundColor: "#ffffff"
				attributeState "on", label: 'On', action: "switch.off", icon: "https://s3-eu-west-1.amazonaws.com/fibaro-smartthings/wallPlugUS/plug_us_blue.png", backgroundColor: "#00a0dc"
			}
			tileAttribute("device.multiStatus", key:"SECONDARY_CONTROL") {
				attributeState("multiStatus", label:'${currentValue}')
			}
		}
		valueTile("power", "device.power", decoration: "flat", width: 2, height: 2) {
			state "power", label:'${currentValue}\nW', action:"refresh"
		}
		valueTile("energy", "device.energy", decoration: "flat", width: 2, height: 2) {
			state "energy", label:'${currentValue}\nkWh', action:"refresh"
		}
		valueTile("reset", "device.energy", decoration: "flat", width: 2, height: 2) {
			state "reset", label:'reset\nkWh', action:"reset"
		}

		standardTile("refresh", "device.refresh", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
			state "default", label: "Refresh", action: "refresh", icon: "st.secondary.refresh"
		}

		main "switch"
		details(["switch", "power", "energy", "reset", "refresh"])
	}

	preferences {

		input (
				title: "Fibaro Wall Plug manual",
				description: "Tap to view the manual.",
				image: "https://s3-eu-west-1.amazonaws.com/fibaro-smartthings/wallPlugUS/plug_us_blue.png",
				//url: "http://manuals.fibaro.com/content/manuals/en/FGWPEF-102/FGWPEF-102-EN-A-v2.0.pdf",
				type: "href",
				element: "href"
		)

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
					description: it.defValDescr,
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

def test(options, defValue){
	def mapToString = "$options"
	def stringToList = mapToString.split(',').collect{it as String}
	def result = stringToList.get(0).substring(3)
	return result
}

//UI and tile functions
def on() {
	log.debug "on"
	def cmds = []
	cmds << [zwave.basicV1.basicSet(value: 0xFF),1]
	cmds << [zwave.switchBinaryV1.switchBinaryGet(),1]
	encapSequence(cmds,2000)
}

def off() {
	log.debug "off"
	def cmds = []
	cmds << [zwave.basicV1.basicSet(value: 0),1]
	cmds << [zwave.switchBinaryV1.switchBinaryGet(),1]
	encapSequence(cmds,2000)
}

def reset() {
	def cmds = []
	cmds << [zwave.meterV3.meterReset(), 1]
	cmds << [zwave.meterV3.meterGet(scale: 0), 1]
	cmds << [zwave.meterV3.meterGet(scale: 2), 1]
	encapSequence(cmds,1000)
}

def refresh() {
	def cmds = []
	cmds << [zwave.meterV3.meterGet(scale: 0), 1]
	cmds << [zwave.meterV3.meterGet(scale: 2), 1]
	cmds << [zwave.switchBinaryV1.switchBinaryGet(),1]
	encapSequence(cmds,1000)
}

def childReset(){
	def cmds = []
	cmds << response(encap(zwave.meterV3.meterReset(), 2))
	cmds << response(encap(zwave.meterV3.meterGet(scale: 0), 2))
	cmds << response(encap(zwave.meterV3.meterGet(scale: 2), 2))
	sendHubCommand(cmds,1000)
}

def childRefresh(){
	def cmds = []
	cmds << response(encap(zwave.meterV3.meterGet(scale: 0), 2))
	cmds << response(encap(zwave.meterV3.meterGet(scale: 2), 2))
	sendHubCommand(cmds,1000)
}


def installed(){
	log.debug "installed()..."
	sendEvent(name: "checkInterval", value: 1920, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID, offlinePingable: "1"])
	response(refresh())
}

//Configuration and synchronization
def updated() {
	if ( state.lastUpdated && (now() - state.lastUpdated) < 500 ) return

	def cmds = []
	log.warn "Executing updated"
	if (!childDevices) {
		createChildDevices()
	}
	state.lastUpdated = now()
	syncStart()
}

def configure() {
	def cmds = []
	cmds << zwave.multiChannelAssociationV2.multiChannelAssociationGet(groupingIdentifier:1)
	cmds << zwave.basicV1.basicSet(value: 0)
	encapSequence(cmds,1000)
}

def ping() {
	log.debug "ping..()"
	childRefresh()
	refresh()
	//response(refresh())
}

private syncStart() {
	boolean syncNeeded = false
	Integer settingValue = null
	parameterMap().each {
		if(settings."$it.key" != null) {
			settingValue = settings."$it.key" as Integer
			if (state."$it.key" == null) { state."$it.key" = [value: null, state: "synced"] }
			if (state."$it.key".value != settingValue || state."$it.key".state != "synced" ) {
				state."$it.key".value = settingValue
				state."$it.key".state = "notSynced"
				syncNeeded = true
			}
		}
	}
	if ( syncNeeded ) {
		logging("sync needed.", "info")
		syncNext()
	}
}

private syncNext() {
	logging("Executing syncNext()","info")
	def cmds = []
	for ( param in parameterMap() ) {
		if ( state."$param.key"?.value != null && state."$param.key"?.state in ["notSynced","inProgress"] ) {
			multiStatusEvent("Sync in progress. (param: ${param.num})", true)
			state."$param.key"?.state = "inProgress"

			cmds << response(encap(zwave.configurationV2.configurationSet(scaledConfigurationValue: state."$param.key".value, parameterNumber: param.num, size: param.size)))
			cmds << response(encap(zwave.configurationV2.configurationGet(parameterNumber: param.num)))
			break
		}
	}
	if (cmds) {
		runIn(10, "syncCheck")
		sendHubCommand(cmds,1000)
	} else {
		runIn(1, "syncCheck")
	}
}

def syncCheck() {
	logging("Executing syncCheck()","info")
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
		multiStatusEvent("Sync failed! Verify parameter: ${failed[0].num}", true, true)
	} else if (incorrect) {
		multiStatusEvent("Sync mismatch! Verify parameter: ${incorrect[0].num}", true, true)
	} else if (notSynced) {
		multiStatusEvent("Sync incomplete! Open settings and tap Done to try again.", true, true)
	} else {
		if (device.currentValue("multiStatus")?.contains("Sync")) { multiStatusEvent("Sync OK.", true, true) }
	}
}

private createChildDevices() {
	logging("${device.displayName} - executing createChildDevices()","info")
	addChildDevice(
			"Fibaro Wall Plug USB",
			"${device.deviceNetworkId}-2",
			null,
			[completedSetup: true, label: "${device.displayName} (CH2)", isComponent: false, componentName: "ch2", componentLabel: "Channel 2"]
	)
}

private getChild(Integer childNum) {
	return childDevices.find({ it.deviceNetworkId == "${device.deviceNetworkId}-${childNum}" })
}

private multiStatusEvent(String statusValue, boolean force = false, boolean display = false) {
	if (!device.currentValue("multiStatus")?.contains("Sync") || device.currentValue("multiStatus") == "Sync OK." || force) {
		sendEvent(name: "multiStatus", value: statusValue, descriptionText: statusValue, displayed: display)
	}
}

private ch2MultiStatusEvent(String statusValue, boolean force = false, boolean display = false) {
	getChild(2)?.sendEvent(name: "multiStatus", value: statusValue, descriptionText: statusValue, displayed: display)

}
//event handlers related to configuration and sync
def zwaveEvent(physicalgraph.zwave.commands.configurationv2.ConfigurationReport cmd) {
	def paramKey = parameterMap().find( {it.num == cmd.parameterNumber } ).key
	logging("Parameter ${paramKey} value is ${cmd.scaledConfigurationValue} expected " + state."$paramKey".value, "info")
	state."$paramKey".state = (state."$paramKey".value == cmd.scaledConfigurationValue) ? "synced" : "incorrect"
	syncNext()
}

def zwaveEvent(physicalgraph.zwave.commands.applicationstatusv1.ApplicationRejectedRequest cmd) {
	logging("rejected request!","warn")
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
def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd) {
	//ignore
}

def zwaveEvent(physicalgraph.zwave.commands.switchbinaryv1.SwitchBinaryReport cmd, ep=null) {
	log.warn "SwitchBinaryReport"
	logging("${device.displayName} - SwitchBinaryReport received, value: ${cmd.value}","info")
	sendEvent([name: "switch", value: (cmd.value == 0 ) ? "off": "on"])
}

def zwaveEvent(physicalgraph.zwave.commands.meterv3.MeterReport cmd, ep=null) {
	log.warn "${device.displayName} - MeterReport received, value: ${cmd.scaledMeterValue} scale: ${cmd.scale} ep: $ep"
	if (!ep || ep==1) {
		log.warn "chanell1"
		switch (cmd.scale) {
			case 0: sendEvent([name: "energy", value: cmd.scaledMeterValue, unit: "kWh"]);
					break;
			case 2: sendEvent([name: "power", value: cmd.scaledMeterValue, unit: "W"]);
					break;
		}
		if (device.currentValue("energy") != null) {
			multiStatusEvent("${device.currentValue("power")} W / ${device.currentValue("energy")} kWh")
		} else {
			multiStatusEvent("${device.currentValue("power")} W / 0.00 kWh")
		}
	}

	if (ep==2) {
		log.warn "chanell2"
		switch (cmd.scale) {
			case 0: getChild(2)?.sendEvent([name: "energy", value: cmd.scaledMeterValue, unit: "kWh"]);
					break;
			case 2: getChild(2)?.sendEvent([name: "power", value: cmd.scaledMeterValue, unit: "W"]);
					break;
		}
		if (device.currentValue("energy") != null) {
			ch2MultiStatusEvent("${getChild(2)?.currentValue("power")} W / ${getChild(2)?.currentValue("energy")} kWh")
		} else {
			ch2MultiStatusEvent("${getChild(2)?.currentValue("power")} W / 0.00 kWh")
		}
	}
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
		log.warn "Unable to extract secure cmd from $cmd"
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
		log.warn "Could not extract crc16 command from $cmd"
	}
}

def zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityCommandsSupportedReport cmd) {
	log.debug "SecurityCommandsSupportedReport"
}

def zwaveEvent(physicalgraph.zwave.commands.securityv1.NetworkKeyVerify cmd){
	log.debug "NetworkKeyVerify"
}

def zwaveEvent(physicalgraph.zwave.commands.securityv1.SecuritySchemeReport cmd){
	log.debug "SecuritySchemeReport"
}

def zwaveEvent(physicalgraph.zwave.commands.applicationstatusv1.ApplicationBusy cmd) {
	log.debug "ApplicationBusy"
}


def zwaveEvent(physicalgraph.zwave.commands.multichannelv3.MultiChannelCmdEncap cmd) {
	def encapsulatedCommand = cmd.encapsulatedCommand(cmdVersions())
	if (encapsulatedCommand) {
		logging("${device.displayName} - Parsed MultiChannelCmdEncap ${encapsulatedCommand}")
		zwaveEvent(encapsulatedCommand, cmd.sourceEndPoint as Integer)
	} else {
		log.warn "Unable to extract MultiChannel command from $cmd"
	}
}

private logging(text, type = "debug") {
//	if (settings.logging == "true") {
		log."$type" text
//	}
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
	} else if (zwaveInfo.cc.contains("56")){
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

/*
##########################
## Device Configuration ##
##########################
*/
private Map cmdVersions() {
	[0x5E: 2, 0x22 :1, 0x56: 1, 0x59: 2, 0x7A: 4 ,0x32: 3, 0x71: 8, 0x73: 1, 0x98: 1, 0x85: 2, 0x70: 2, 0x72: 2, 0x5A: 1, 0x8E: 2, 0x25: 1, 0x86: 2, 0x55: 2, 0x9F: 1, 0x75: 1, 0x60: 3, 0x6C: 1, 0x20: 1]
}

private parameterMap() {[

		[key: "restoreState", num: 2, size: 1, type: "enum", options: [0: "device remains switched off", 1: "device restores the state"], def: "0", title: "Restore state after power failure",
		 descr: "After the power supply is back on, the Wall Plug can be restored to previous state or remain switched off.", defValDescr: "Device remains switched off (Default)"],
		[key: "overloadSafety", num: 3, size: 2, type: "number", def: 0, min: 0, max: 18000 , title: "Overload safety switch",
		 descr: "Allows to turn off the controlled device in case of exceeding the defined power;\n0 - function inactive\n10-18000 (1.0-1800.0W, step 0.1W)\n To calculate the value of parameter, multiply the power in Watts by 10 for example: 50 W x 10 = 500 Where: 50 W – the power of device connected to Wall Plug; 500 - value of parameter."],
		[key: "standardPowerReports", num: 11, size: 1, type: "number", def: 15, min: 1, max: 100, title: "Standard power reports",
		 descr: "This parameter determines the minimum percentage change in active power that will result in sending a power report.\n1-99 - power change in percent\n100 - reports are disabled"],
		[key: "energyReportingThreshold", num: 12, size: 2, type: "number", def: 10, min: 0, max: 500, title: "Energy reporting threshold",
		 descr: "This parameter determines the minimum change in energy consumption (in relation to the previously reported) that will result in sending a new report.\n1-500 (0.01-5kWh) - threshold\n0 - reports are disabled"],
		[key: "periodicPowerReporting", num: 13, size: 2, type: "number", def: 3600, min: 0, max: 32400, title: " Periodic power reporting",
		 descr: "This parameter defines time period between independent reports sent when changes in power load have not been recorded or if changes are insignificant. By default reports are sent every hour.\n30-32400 - interval in seconds\n0 - periodic reports are disabled"],
		[key: "periodicReports", num: 14, size: 2, type: "number", def: 3600, min: 0, max: 32400, title: "Periodic energy reporting",
		 descr: "Time period between independent reports.\n0 - periodic reports inactive\n30-32400 (in seconds)"],
		[key: "ringColorOn", num: 41, size: 1, type: "enum", options: [
				0: "Off",
				1: "Load based - continuous",
				2: "Load based - steps",
				3: "White",
				4: "Red",
				5: "Green",
				6: "Blue",
				7: "Yellow",
				8: "Cyan",
				9: "Magenta"
		], def: "1", title: "Ring LED color when on", descr: "Ring LED colour when the device is ON.", defValDescr: "Load based - continuous (Default)"],
		[key: "ringColorOff", num: 42, size: 1, type: "enum", options: [
				0: "Off",
				1: "Last measured power",
				3: "White",
				4: "Red",
				5: "Green",
				6: "Blue",
				7: "Yellow",
				8: "Cyan",
				9: "Magenta"
		], def: "0", title: "Ring LED color when off", descr: "Ring LED colour when the device is OFF.", defValDescr: "Off (Default)"]
]}
