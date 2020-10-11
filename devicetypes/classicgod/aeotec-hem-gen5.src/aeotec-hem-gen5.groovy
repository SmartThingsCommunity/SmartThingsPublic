/**
 *  Aeotec HEM Gen5
 *
 *  Copyright 2020 Jochen Kuhn
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
metadata {
	definition (name: "Aeotec HEM Gen5", namespace: "ClassicGOD", author: "Artur Draga") {
		capability "Energy Meter"
		capability "Power Meter"
		capability "Voltage Measurement"
		capability "Configuration"
		capability "Refresh"
		capability "Health Check"
        capability "Sensor"

		command "reset"
        command "resetMeter"

		attribute "current", "number"
		attribute "combinedMeter", "string"

		fingerprint mfr: "0086", model: "005F"
		fingerprint mfr: "0086", prod: "0002", model: "005F"
		fingerprint deviceId: "0X3101", inClusters:"0x5E,0x86,0x72,0x32,0x56,0x60,0x8E,0x70,0x59,0x85,0x7A,0x73,0x5A,0x98"
		fingerprint deviceId: "0X3101", inClusters:"0x5E,0x86,0x72,0x32,0x56,0x60,0x8E,0x70,0x59,0x85,0x7A,0x73,0x5A"
	}

	tiles (scale: 2) {
		multiAttributeTile(name:"multiTile", type: "generic", width: 3, height: 4){
			tileAttribute ("device.power", key: "PRIMARY_CONTROL") {
				attributeState "power", label: '${currentValue} W', icon: null, backgroundColors:[
					[value: 0, color: "#44b621"],
					[value: 2000, color: "#f1d801"],
					[value: 4000, color: "#d04e00"],
					[value: 6000, color: "#bc2323"]
				]
			}
			tileAttribute("device.combinedMeter", key:"SECONDARY_CONTROL") {
				attributeState("combinedMeter", label:'${currentValue}')
			} 
		}
		valueTile("power", "device.power", decoration: "flat", width: 2, height: 2, canChangeIcon: true) {
			state "power", label:'${currentValue} W', unit: "W", icon: "st.Home.home2", backgroundColors:[
				[value: 0, color: "#44b621"],
				[value: 2000, color: "#f1d801"],
				[value: 4000, color: "#d04e00"],
				[value: 6000, color: "#bc2323"]
			]
		}
		valueTile("energy", "device.energy", decoration: "flat", width: 2, height: 2) {
			state "energy", label:'${currentValue}\n kWh ', unit: "kWh"
		}
		valueTile("voltage", "device.voltage", decoration: "flat", width: 2, height: 2) {
			state "voltage", label:'${currentValue}\n V ', unit: "V"
		}
		valueTile("current", "device.current", decoration: "flat", width: 2, height: 2) {
			state "current", label:'${currentValue}\n A ', unit: "A"
		}
		standardTile("reset", "device.reset", decoration: "flat", width: 2, height: 2) {
			state "default", label:'Reset kWh', action:"reset", icon: "st.Kids.kids4"
		}
		standardTile("refresh", "device.refresh", decoration: "flat", width: 2, height: 2) {
			state "default", label:'', action:"refresh.refresh", icon:"st.secondary.refresh"
		}
		
		main "power"
		details("multiTile","energy","voltage","current","reset","refresh")
	}
		
	preferences {
		input (
			title: "Aeotec Home Energy Meter Gen5 manual",
			description: "Tap to view the manual.",
			image: "http://aeotec.com/images/products/220/z-wave-home-energy-measure@2x.jpg",
			url: "https://aeotec.freshdesk.com/helpdesk/attachments/6018901892",
			element: "href"
		)
		
		parameterMap().each { param ->
			if (param.num in (101..103)) {
				input (
					title: "${param.num}. Reports in group " + (param.num - 100),
					description: "Which reports need to send in Report group " + (param.num - 100),
					type: "paragraph",
					element: "paragraph"
				)
				optionMap().each { opt ->
					input ( name: "${param.key}${opt.key}", title: opt.name , type: "boolean", required: false, defaultValue: (opt.def && opt.def?.contains(param.num)) ? 1:0 )
				}
			} else {
				getPrefsFor(param)
			}
		}
		
		input ( name: "logging", title: "Logging", type: "boolean", required: false )
	}
}

def getPrefsFor(parameter) {
	input (
		title: "${parameter.num}. ${parameter.title}",
		description: parameter.descr,
		type: "paragraph",
		element: "paragraph"
	)
	input (
		name: parameter.key,
		title: null,
		//description: null,
		type: parameter.type,
		options: parameter.options,
		range: (parameter.min != null && parameter.max != null) ? "${parameter.min}..${parameter.max}" : null,
		defaultValue: parameter.def,
		required: false
	)
}

def refresh() {
	logging("${device.displayName} - executing refresh()","info")
	def cmds = []
	cmds << zwave.meterV3.meterGet(scale: 0)
	cmds << zwave.meterV3.meterGet(scale: 2)
	cmds << zwave.meterV3.meterGet(scale: 4)
	cmds << zwave.meterV3.meterGet(scale: 5)
	return encapSequence(cmds,1000)
}

def reset() {
	logging("${device.displayName} - executing reset()","info")
	if ( state.lastReset && (now() - state.lastReset) < 2000 ) {
		return resetMeter()
	} else {
		state.lastReset = now()
	}
}

def resetMeter() {
	logging("${device.displayName} - executing resetMeter()","info")
	def cmds = []
	sendEvent(name: "combinedMeter", value: "RESETTING KWH!", displayed: false)
	cmds << zwave.meterV3.meterReset()
	cmds << zwave.meterV3.meterGet(scale: 0)
	cmds << [zwave.meterV3.meterGet(scale: 0),1]
	cmds << [zwave.meterV3.meterGet(scale: 0),2]
	cmds << [zwave.meterV3.meterGet(scale: 0),3]
	if (cmds) { return encapSequence(cmds,1000) }
}

def childRefresh(dni) {
	return [:]
	logging("${device.displayName} - executing childRefresh() for $dni","info")
	def cmds = []
	Integer ep = (dni-"${device.deviceNetworkId}-c") as Integer
	cmds << response(encap(zwave.meterV3.meterGet(scale: 0), ep))
	cmds << response(encap(zwave.meterV3.meterGet(scale: 2), ep))
	cmds << response(encap(zwave.meterV3.meterGet(scale: 4), ep))
	cmds << response(encap(zwave.meterV3.meterGet(scale: 5), ep))
	sendHubCommand(cmds,1000)
}

def childReset(dni) {
	logging("${device.displayName} - executing childReset() for $dni","info")
	def cmds = []
	Integer ep = (dni-"${device.deviceNetworkId}-c") as Integer
	getChild(ep)?.sendEvent(name: "combinedMeter", value: "RESETTING KWH!", displayed: false)
	cmds << response(encap(zwave.meterV3.meterReset(), ep))
	cmds << response(encap(zwave.meterV3.meterGet(scale: 0), ep))
	cmds << response(encap(zwave.meterV3.meterGet(scale: 0)))
	sendHubCommand(cmds,1000)
}

private createChildDevices() {
	logging("${device.displayName} - executing createChildDevices()","info")
	if ((zwaveInfo.epc as Integer) > 1) {
		(zwaveInfo.epc as Integer)?.times { n ->
			addChildDevice(
				"Aeotec HEM Gen5 Child Device", 
				"${device.deviceNetworkId}-c${n+1}", 
				null,
				[completedSetup: true, label: "${device.displayName} (Clamp ${n+1})", isComponent: false, componentName: "clamp${n+1}", componentLabel: "Clamp ${n+1}"]
			)
		}
	}
}

private physicalgraph.app.ChildDeviceWrapper getChild(Integer childNum) {
	return childDevices.find({ it.deviceNetworkId == "${device.deviceNetworkId}-c${childNum}" })
}

// Parameter configuration, synchronization and verification
def updated() {
	if ( state.lastUpdated && (now() - state.lastUpdated) < 500 ) return
	logging("${device.displayName} - Executing updated()","info")
	Integer epc = (zwaveInfo.epc) ? (zwaveInfo.epc as Integer):0
	def clamps = []
	def cmds = []
	def Integer cmdCount = 0
	def value
	
	if (epc > 1 && !childDevices) {
		createChildDevices()
	}
	
	epc.times { n -> clamps << ("Clamp${n+1}" as String) }

	parameterMap().each {
		if(settings."$it.key" != null || it.num in (101..103) ) {
			if (state."$it.key" == null) { state."$it.key" = [value: null, state: "synced"] }
			
			if (it.num in (101..103)) { value = calcParamVal(it.key) }
			else if (it.key.contains("Clamp") && !(it.key[-6..-1] in clamps)) { value = null }
			else { value = settings."$it.key" as Integer } 
			
			if (state."$it.key".value != value || state."$it.key".state == "notSynced") {
				state."$it.key".value = value
				state."$it.key".state = "notSynced"
				cmds << zwave.configurationV2.configurationSet(configurationValue: intToParam(state."$it.key".value, it.size), parameterNumber: it.num, size: it.size)
				cmds << zwave.configurationV2.configurationGet(parameterNumber: it.num)
				cmdCount = cmdCount + 1
			}
		}
	}
	
	if ( cmdCount > 0 ) { 
		logging("${device.displayName} - sending config.", "info")
		sendEvent(name: "combinedMeter", value: "SYNC IN PROGRESS.", displayed: false)
		runIn((5+cmdCount*2), syncCheck)
	}
	
	state.lastUpdated = now()
	if (cmds) { response(encapSequence(cmds,1000)) }
}

def Integer calcParamVal(String paramKey) {
	def result = 0
	def clamps = []
	(zwaveInfo.epc as Integer)?.times { n -> clamps << ("clamp${n+1}" as String) }
	
	settings.findAll({it.key.take(6) == paramKey}).each { optSetting ->
		if ( optSetting.key.drop(6).take(3) == "hem" || optSetting.key.drop(6).take(6) in clamps ) {
			result += (optSetting.value == "true") ? optionMap().find( {it.key == (optSetting.key-paramKey)} ).value:0
		}
	}
	return result
}

def syncCheck() {
	logging("${device.displayName} - Executing syncCheck()","info")
	def Integer count = 0
	if (device.currentValue("combinedMeter")?.contains("SYNC") && device.currentValue("combinedMeter") != "SYNC OK.") {
		parameterMap().each {
			if (state."$it.key".state == "notSynced" ) {
				count = count + 1
			} 
		}
	}
	if (count == 0) {
		logging("${device.displayName} - Sync Complete","info")
		sendEvent(name: "combinedMeter", value: "SYNC OK.", displayed: false)
	} else {
		logging("${device.displayName} Sync Incomplete","info")
		if (device.currentValue("combinedMeter") != "SYNC FAILED!") {
			sendEvent(name: "combinedMeter", value: "SYNC INCOMPLETE.", displayed: false)
		}
	}
}

//config related event handlers
def zwaveEvent(physicalgraph.zwave.commands.configurationv2.ConfigurationReport cmd) {
	def paramKey
	paramKey = parameterMap().find( {it.num == cmd.parameterNumber as Integer} ).key 
	logging("${device.displayName} - Parameter ${paramKey} value is ${cmd.scaledConfigurationValue} expected " + state."$paramKey"?.value, "info")
	if (state."$paramKey".value == cmd.scaledConfigurationValue) {
		state."$paramKey".state = "synced"
	runIn(10, syncCheck)
	} 
}

def zwaveEvent(physicalgraph.zwave.commands.applicationstatusv1.ApplicationRejectedRequest cmd) {
	logging("${device.displayName} - rejected request!","warn")
	if (device.currentValue("combinedMeter") == "SYNC IN PROGRESS.") { 
		sendEvent(name: "combinedMeter", value: "SYNC FAILED!", displayed: false)
	}
}

//event handlers
def zwaveEvent(physicalgraph.zwave.commands.meterv3.MeterReport cmd, ep=null) {
	String unit
	String type
	switch (cmd.scale+((cmd.scale2) ? 1:0)) {
		case 0: type = "energy"; unit = "kWh"; break; 
		case 1: type = "totalEnergy"; unit = "kVAh"; break;
		case 2: type = "power"; unit = "W"; break;	
		case 4: type = "voltage"; unit = "V"; break;
		case 5: type = "current"; unit = "A"; break;
		case 7: type = "reactivePower"; unit = "kVar"; break;
		case 8: type = "reactiveEnergy"; unit = "kVarh"; break; 
	}
	logging("${device.displayName} - MeterReport received, ep: ${((ep) ? ep:0)} value: ${cmd.scaledMeterValue} ${unit}", "info")
	if (ep == null) {
		sendEvent([name: type, value: cmd.scaledMeterValue, unit: unit, displayed: false])
		if (!device.currentValue("combinedMeter")?.contains("SYNC") || device.currentValue("combinedMeter") == "SYNC OK." || device.currentValue("combinedMeter") == null ) {
			sendEvent([name: "combinedMeter", value: "${device.currentValue("voltage")} V | ${device.currentValue("current")} A | ${device.currentValue("energy")} kWh", displayed: false])
		}
	} else {
		getChild(ep)?.sendEvent([name: type, value: cmd.scaledMeterValue, unit: unit, displayed: false]) 
		getChild(ep)?.sendEvent([name: "combinedMeter", value: "${getChild(ep)?.currentValue("voltage")} V | ${getChild(ep)?.currentValue("current")} A | ${getChild(ep)?.currentValue("energy")} kWh", displayed: false])
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
		log.debug "Err 106"
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

def zwaveEvent(physicalgraph.zwave.commands.multichannelv3.MultiChannelCmdEncap cmd) {
	def encapsulatedCommand = cmd.encapsulatedCommand(cmdVersions())
	if (encapsulatedCommand) {
		logging("${device.displayName} - Parsed MultiChannelCmdEncap ${encapsulatedCommand}")
		zwaveEvent(encapsulatedCommand, cmd.sourceEndPoint as Integer)
	} else {
		log.warn "Could not extract MultiChannel command from $cmd"
	}
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
	logging("${device.displayName} - encapsulating command using Multi Channel Encapsulation, ep: $ep command: $cmd","info")
	zwave.multiChannelV3.multiChannelCmdEncap(destinationEndPoint:ep).encapsulate(cmd)
}

private encap(physicalgraph.zwave.Command cmd) {
	if (zwaveInfo.zw.contains("s") ) { 
		secEncap(cmd)
	} else if (zwaveInfo.cc.contains("56")){ 
		crcEncap(cmd)
	} else {
		logging("${device.displayName} - no encapsulation supported for command: $cmd","info")
		cmd.format()
	}
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
	  [0x5E: 2, 0x86: 1, 0x72: 1, 0x32: 3, 0x56: 1, 0x60: 3, 0x8E: 2, 0x70: 2, 0x59: 1, 0x85: 2, 0x7A: 2, 0x73: 1, 0x5A: 1, 0x98: 1] //Aeotec Home Energy Meter Gen 5
}

private parameterMap() {[
	//[key: "detectionMode", num: 2, size: 1, type: "enum", options: [
	//	0: "0 - power, energy absolute value", 
	//	1: "1 - positive/negative power, algebraic sum energy",
	//	2: "2 - positive/negative power, energy positive part",
	//	3: "3 - positive/negative power, energy negative part"],
	//	def: "0", title: "Power and Energy mode", 
	//	descr: "For parameters of 101 ~ 103, power, energy detection mode configuration"], //WTF aotec?
	[key: "reportingThreshold", num: 3, size: 1, type: "enum", options: [
		0: "0 - disable,", 
		1: "1 - enable"],
		def: "1", title: "Reporting Threshold", 
		descr: "Enable selective reporting only when power change reaches a certain threshold or percentage set in 4-11 below"],
	[key: "thresholdHEM", num: 4, size: 2, type: "number", def: 50, min: 0, max: 60000, title: "HEM threshold", 
		descr: "Threshold change in wattage to induce a automatic report (Whole HEM)\n0-60000 W"], 
	[key: "thresholdClamp1", num: 5, size: 2, type: "number", def: 50, min: 0, max: 60000, title: "Clamp 1 threshold", 
		descr: "Threshold change in wattage to induce a automatic report (Clamp 1)\n0-60000 W"], 
	[key: "thresholdClamp2", num: 6, size: 2, type: "number", def: 50, min: 0, max: 60000, title: "Clamp 2 threshold", 
		descr: "Threshold change in wattage to induce a automatic report (Clamp 2)\n0-60000 W"], 
	[key: "thresholdClamp3", num: 7, size: 2, type: "number", def: 50, min: 0, max: 60000, title: "Clamp 3 threshold", 
		descr: "Threshold change in wattage to induce a automatic report (Clamp 3)\n0-60000W"], 
	[key: "percentageHEM", num: 8, size: 1, type: "number", def: 10, min: 0, max: 100, title: "HEM percentage", 
		descr: "Percentage change in wattage to induce a automatic report (Whole HEM)\n0-100%"], 
	[key: "percentageClamp1", num: 9, size: 1, type: "number", def: 10, min: 0, max: 100, title: "Clamp 1 percentage", 
		descr: "Percentage change in wattage to induce a automatic report (Clamp 1)\n0-100%"], 
	[key: "percentageClamp2", num: 10, size: 1, type: "number", def: 10, min: 0, max: 100, title: "Clamp 2 percentage", 
		descr: "Percentage change in wattage to induce a automatic report (Clamp 2)\n0-100%"], 
	[key: "percentageClamp3", num: 11, size: 1, type: "number", def: 10, min: 0, max: 100, title: "Clamp 3 percentage", 
		descr: "Percentage change in wattage to induce a automatic report (Clamp 3)\n0-100%"],
	[key: "crcReporting", num: 13, size: 1, type: "enum", options: [
		0: "0 - disable,", 
		1: "1 - enable"],
		def: "0", title: "CRC-16 reporting", 
		descr: "Enable /disable reporting using CRC-16 Encapsulation Command"],
	[key: "group1", num: 101, size: 4, type: "number", def: 2, min: 0, max: 4210702, title: null, descr: null],
	[key: "group2", num: 102, size: 4, type: "number", def: 1, min: 0, max: 4210702, title: null, descr: null],
	[key: "group3", num: 103, size: 4, type: "number", def: 0, min: 0, max: 4210702, title: null, descr: null],
	[key: "timeGroup1", num: 111, size: 4, type: "number", def: 5, min: 0, max: 268435456, title: "Group 1 time interval", 
		descr: "The time interval for Report group 1\n0-268435456s"],
	[key: "timeGroup2", num: 112, size: 4, type: "number", def: 120, min: 0, max: 268435456, title: "Group 2 time interval", 
		descr: "The time interval for Report group 2\n0-268435456s"],
	[key: "timeGroup3", num: 113, size: 4, type: "number", def: 120, min: 0, max: 268435456, title: "Group 3 time interval", 
		descr: "The time interval for Report group 3\n0-268435456s"]
]}

private optionMap() {[
	[key: "hemkWh", name: "Report kWh of whole HEM", value: 1, def:[102]],
	[key: "hemW", name: "Report Watts of whole HEM.", value: 2, def:[101]],
	[key: "hemV", name: "Report Voltage of whole HEM.", value: 4, def:null],
	[key: "hemA", name: "Report Current (Amperes) of whole HEM.", value: 8, def:null],
	//[key: "hemKVarh", name: "Report KVarh of whole HEM", value: 16, def:null], //Doesn't work
	//[key: "hemkVar", name: "Report kVar of whole HEM", value: 32, def:null], //Doesn't work
	[key: "clamp1W", name: "Report Watts of Clamp 1.", value: 256, def:null],
	[key: "clamp2W", name: "Report Watts of Clamp 2.", value: 512, def:null],
	[key: "clamp3W", name: "Report Watts of Clamp 3.", value: 1024, def:null],
	[key: "clamp1kWh", name: "Report kWh of Clamp 1.", value: 2048, def:null],
	[key: "clamp2kWh", name: "Report kWh of Clamp 2.", value: 4096, def:null],
	[key: "clamp3kWh", name: "Report kWh of Clamp 3.", value: 8192, def:null],
	[key: "clamp1V", name: "Report Voltage of Clamp 1.", value: 65536, def:null],
	[key: "clamp2V", name: "Report Voltage of Clamp 2.", value: 131072, def:null],
	[key: "clamp3V", name: "Report Voltage of Clamp 3.", value: 262144, def:null],
	[key: "clamp1A", name: "Report Current (Amperes) of Clamp 1.", value: 524288, def:null],
	[key: "clamp2A", name: "Report Current (Amperes) of Clamp 2.", value: 1048576, def:null],
	[key: "clamp3A", name: "Report Current (Amperes) of Clamp 3.", value: 2097152, def:null],
	//[key: "clamp1KVarh", name: "Report KVarh of Clamp 1.", value: 16777216, def:null], //Doesn't work
	//[key: "clamp2KVarh", name: "Report KVarh of Clamp 2.", value: 33554432, def:null], //Doesn't work
	//[key: "clamp3KVarh", name: "Report KVarh of Clamp 3.", value: 67108864, def:null], //Doesn't work
	//[key: "clamp1KVar", name: "Report kVar of Clamp 1.", value: 134217728, def:null], //Doesn't work
	//[key: "clamp2KVar", name: "Report kVar of Clamp 2.", value: 268435456, def:null], //Doesn't work
	//[key: "clamp3KVar", name: "Report kVar of Clamp 3.", value: 536870912, def:null] //Doesn't work
]}