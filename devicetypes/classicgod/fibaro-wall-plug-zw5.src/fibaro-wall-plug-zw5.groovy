/**
 *  Fibaro Wall Plug ZW5
 *  Requires: Fibaro Double Switch 2 Child Device
 *
 *  Copyright 2017 Artur Draga
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *	  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */
metadata {
	definition (name: "Fibaro Wall Plug ZW5", namespace: "ClassicGOD", author: "Artur Draga") {
		capability "Switch"
		capability "Energy Meter"
		capability "Power Meter"
		capability "Configuration"
		capability "Health Check"

		command "reset"
		command "refresh"

		fingerprint deviceId: "0x1001", inClusters:"0x5E,0x22,0x59,0x56,0x7A,0x32,0x71,0x73,0x98,0x31,0x85,0x70,0x72,0x5A,0x8E,0x25,0x86"
		fingerprint deviceId: "0x1001", inClusters:"0x5E,0x22,0x59,0x56,0x7A,0x32,0x71,0x73,0x31,0x85,0x70,0x72,0x5A,0x8E,0x25,0x86"
	}

	tiles (scale: 2) {
		multiAttributeTile(name:"switch", type: "lighting", width: 3, height: 4, canChangeIcon: true){
			tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
				attributeState "off", label: 'Off', action: "switch.on", icon: "st.switches.switch.off", backgroundColor: "#ffffff", nextState:"turningOn"
				attributeState "on", label: 'On', action: "switch.off", icon: "st.switches.switch.on", backgroundColor: "#00a0dc", nextState:"turningOff"
				attributeState "turningOn", label:'Turning On', action:"switch.off", icon:"st.switches.switch.on", backgroundColor:"#00a0dc", nextState:"turningOff"
				attributeState "turningOff", label:'Turning Off', action:"switch.on", icon:"st.switches.switch.off", backgroundColor:"#ffffff", nextState:"turningOn"
			}
			tileAttribute("device.combinedMeter", key:"SECONDARY_CONTROL") {
				attributeState("combinedMeter", label:'${currentValue}')
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
	}

	preferences {
		input ( name: "logging", title: "Logging", type: "boolean", required: false )
		parameterMap().each {
			input (
				name: it.key,
				title: "${it.num}. ${it.title}",
				description: it.descr,
				type: it.type,
				options: it.options,
				range: (it.min != null && it.max != null) ? "${it.min}..${it.max}" : null,
				defaultValue: it.def,
				required: false
			)
		}
	}
}

//UI and tile functions
def on() {
	encap(zwave.basicV1.basicSet(value: 255))
}

def off() {
	encap(zwave.basicV1.basicSet(value: 0))
}

def reset() {
	def cmds = []
	cmds << zwave.meterV3.meterReset()
	cmds << zwave.meterV3.meterGet(scale: 0)
	cmds << zwave.meterV3.meterGet(scale: 2)
	encapSequence(cmds,1000)
}

def refresh() {
	def cmds = []
	cmds << zwave.meterV3.meterGet(scale: 0)
	cmds << zwave.meterV3.meterGet(scale: 2)
	cmds << zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType: 4)
	encapSequence(cmds,1000)
}

//Configuration and synchronization
def updated() {
	if ( state.lastUpdated && (now() - state.lastUpdated) < 500 ) return
	def cmds = []
	logging("${device.displayName} - Executing updated()","info")
	
	def Integer cmdCount = 0
	parameterMap().each {
		if(settings."$it.key" != null) {
			if (state."$it.key" == null) { state."$it.key" = [value: null, state: "synced"] }
			if (state."$it.key".value != settings."$it.key" as Integer || state."$it.key".state == "notSynced") {
				state."$it.key".value = settings."$it.key" as Integer
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

//event handlers related to configuration and sync
def zwaveEvent(physicalgraph.zwave.commands.configurationv2.ConfigurationReport cmd) {
	def paramKey = parameterMap().find( {it.num == cmd.parameterNumber } ).key
	logging("${device.displayName} - Parameter ${paramKey} value is ${cmd.scaledConfigurationValue} expected " + state."$paramKey".value, "info")
	if (state."$paramKey".value == cmd.scaledConfigurationValue) {
		state."$paramKey".state = "synced"
	}
}

def zwaveEvent(physicalgraph.zwave.commands.applicationstatusv1.ApplicationRejectedRequest cmd) {
	logging("${device.displayName} - rejected request!","warn")
	if (device.currentValue("combinedMeter") == "SYNC IN PROGRESS.") { 
		sendEvent(name: "combinedMeter", value: "SYNC FAILED!", displayed: false)
	}
}

//event handlers
def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd) {
	//ignore
}

def zwaveEvent(physicalgraph.zwave.commands.switchbinaryv1.SwitchBinaryReport cmd) {
	logging("${device.displayName} - SwitchBinaryReport received, value: ${cmd.value}","info")
	sendEvent([name: "switch", value: (cmd.value == 0 ) ? "off": "on"])
}

def zwaveEvent(physicalgraph.zwave.commands.sensormultilevelv5.SensorMultilevelReport cmd) {
	logging("${device.displayName} - SensorMultilevelReport received, value: ${cmd.scaledSensorValue} scale: ${cmd.scale}","info")
	if (cmd.sensorType == 4) { 
		sendEvent([name: "power", value: cmd.scaledSensorValue, unit: "W"]) 
		updateCombinedMeter()
	}
}

def zwaveEvent(physicalgraph.zwave.commands.meterv3.MeterReport cmd) {
	logging("${device.displayName} - MeterReport received, value: ${cmd.scaledMeterValue} scale: ${cmd.scale}","info")
	switch (cmd.scale) {
		case 0:
			sendEvent([name: "energy", value: cmd.scaledMeterValue, unit: "kWh"])
			break
		case 2:
			sendEvent([name: "power", value: cmd.scaledMeterValue, unit: "W"])
			break
	}
	updateCombinedMeter()
}

//other
private updateCombinedMeter() {
	if (!device.currentValue("combinedMeter")?.contains("SYNC") || device.currentValue("combinedMeter") == "SYNC OK." || device.currentValue("combinedMeter") == null ) {
		sendEvent([name: "combinedMeter", value: "${device.currentValue("power")} W / ${device.currentValue("energy")} kWh", displayed: false])
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
		createEvent(descriptionText: cmd.toString())
	}
}

def zwaveEvent(physicalgraph.zwave.commands.crc16encapv1.Crc16Encap cmd) {
	def version = cmdVersions()[cmd.commandClass as Integer]
	def ccObj = version ? zwave.commandClass(cmd.commandClass, version) : zwave.commandClass(cmd.commandClass)
	def encapsulatedCommand = ccObj?.command(cmd.command)?.parse(cmd.data)
	if (!encapsulatedCommand) {
		log.warn "Could not extract crc16 command from $cmd"
	} else {
		logging("${device.displayName} - Parsed Crc16Encap into: ${encapsulatedCommand}")
		zwaveEvent(encapsulatedCommand)
	}
}

def zwaveEvent(physicalgraph.zwave.commands.multichannelv3.MultiChannelCmdEncap cmd) {
	def encapsulatedCommand = cmd.encapsulatedCommand(cmdVersions())
	if (encapsulatedCommand) {
		logging("${device.displayName} - Parsed MultiChannelCmdEncap ${encapsulatedCommand}")
		zwaveEvent(encapsulatedCommand, cmd.sourceEndPoint as Integer)
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
		zwave.crc16EncapV1.crc16Encap().encapsulate(cmd).format() // doesn't work righ now because SmartThings...
		//"5601${cmd.format()}0000"
}

private multiEncap(physicalgraph.zwave.Command cmd, Integer ep) {
	logging("${device.displayName} - encapsulating command using Multi Channel Encapsulation, ep: $ep command: $cmd","info")
	zwave.multiChannelV3.multiChannelCmdEncap(destinationEndPoint:ep).encapsulate(cmd)
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
	//[0x5E: 2, 0x59: 1, 0x80: 1, 0x56: 1, 0x7A: 3, 0x73: 1, 0x98: 1, 0x22: 1, 0x85: 2, 0x5B: 1, 0x70: 2, 0x8E: 2, 0x86: 2, 0x84: 2, 0x75: 2, 0x72: 2] //Fibaro KeyFob
	//[0x5E: 2, 0x86: 1, 0x72: 2, 0x59: 1, 0x80: 1, 0x73: 1, 0x56: 1, 0x22: 1, 0x31: 5, 0x98: 1, 0x7A: 3, 0x20: 1, 0x5A: 1, 0x85: 2, 0x84: 2, 0x71: 3, 0x8E: 2, 0x70: 2, 0x30: 1, 0x9C: 1] //Fibaro Motion Sensor ZW5
	//[0x5E: 2, 0x86: 1, 0x72: 1, 0x59: 1, 0x73: 1, 0x22: 1, 0x56: 1, 0x32: 3, 0x71: 1, 0x98: 1, 0x7A: 1, 0x25: 1, 0x5A: 1, 0x85: 2, 0x70: 2, 0x8E: 2, 0x60: 3, 0x75: 1, 0x5B: 1] //Fibaro Double Switch 2
	[0x5E: 2, 0x22: 1, 0x59: 1, 0x56: 1, 0x7A: 1, 0x32: 3, 0x71: 1, 0x73: 1, 0x98: 1, 0x31: 5, 0x85: 2, 0x70: 2, 0x72: 2, 0x5A: 1, 0x8E: 2, 0x25: 1, 0x86: 2] //Fibaro Wall Plug ZW5
}

private parameterMap() {[
	[key: "alwaysActive", num: 1, size: 1, type: "enum", options: [0: "0 - inactive", 1: "1 - activated"], def: "0", title: "Always on function", descr: null],
	[key: "restoreState", num: 2, size: 1, type: "enum", options: [0: "0 - power off after power failure", 1: "1 - restore state"], def: "1", title: "Restore state after power failure", descr: null],
	[key: "overloadSafety", num: 3, size: 2, type: "number", def: 0, min: 0, max: 30000 , title: "Overload safety switch", descr: null],
	[key: "immediatePowerReports", num: 10, size: 1, type: "number", def: 80, min: 1, max: 100, title: "Immediate power reports", descr: null],
	[key: "standardPowerReports", num: 11, size: 1, type: "number", def: 15, min: 1, max: 100, title: "Standard power reports", descr: null], 
	[key: "powerReportFrequency", num: 12, size: 2, type: "number", def: 30, min: 5, max: 600, title: "Power reporting interval", descr: null],
	[key: "energyReport", num: 13, size: 2, type: "number", def: 10, min: 0, max: 500, title: "Energy reports", descr: null], 
	[key: "periodicReports", num: 14, size: 2, type: "number", def: 3600, min: 0, max: 32400, title: "Periodic power and energy reports", descr: null], 
	[key: "deviceEnergyConsumed", num: 15, size: 1, type: "enum", options: [0: "0 - don't measure", 1: "1 - measure"], def: "0", title: "Energy consumed by the device itself", descr: null],
	[key: "powerLoad", num: 40, size: 2, type: "number", def: 25000, min: 1000, max: 30000, title: "Power load which makes the LED ring flash violet", descr: null],
	[key: "ringColorOn", num: 41, size: 1, type: "enum", options: [
		0: "0 - Off",
		1: "1 - Load based - continuous", 
		2: "2 - Load based - steps", 
		3: "3 - White", 
		4: "4 - Red", 
		5: "5 - Green", 
		6: "6 - Blue", 
		7: "7 - Yellow", 
		8: "8 - Cyan", 
		9: "9 - Magenta"
		], def: "1", title: "Ring LED color when on", descr: null],
	[key: "ringColorOff", num: 42, size: 1, type: "enum", options: [
		0: "0 - Off",
		1: "1 - Last measured power",  
		3: "3 - White", 
		4: "4 - Red", 
		5: "5 - Green", 
		6: "6 - Blue", 
		7: "7 - Yellow", 
		8: "8 - Cyan", 
		9: "9 - Magenta"
		], def: "0", title: "Ring LED color when off", descr: null]
]}