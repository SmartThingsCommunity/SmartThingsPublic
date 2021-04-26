/**
 *  Widom Smart DRY contact
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
	definition (name: "WiDom Smart Dry Contact", namespace: "WiDomsrl", author: "WiDom srl", ocfDeviceType: "oic.d.switch", mnmn: "SmartThings", vid: "generic-switch") {
		capability "Actuator"
		capability "Switch"
		capability "Configuration"
		capability "Health Check"

		fingerprint mfr: "0149", prod: "1214", model: "0900", deviceJoinName: "WiDom Switch" // Raw Description zw:Ls2 type:1001 mfr:0149 prod:1214 model:0900 ver:1.00 zwv:6.04 lib:03 cc:5E,55,98,9F,6C sec:86,25,85,8E,59,72,5A,73,70,7A
	}
    
	preferences {
		input (
			title: "WiDom Smart Dry Contact manual",
			description: "Tap to view the manual.",
			image: "https://www.widom.it/wp-content/uploads/2019/03/widom-3d-smart-dry-contact.gif",
			url: "https://www.widom.it/wp-content/uploads/2020/04/Widom_Dry_Contact_IT_070420.pdf",
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
					//description: "Default: $it.def" ,
					type: it.type,
					options: it.options,
					//range: (it.min != null && it.max != null) ? "${it.min}..${it.max}" : null,
					defaultValue: it.def,
					required: false
			)
		}
		input ( name: "logging", title: "Logging", type: "boolean", required: false )
	}
}

def on() {
	encap(zwave.basicV1.basicSet(value: 255))
}

def off() {
	encap(zwave.basicV1.basicSet(value: 0))
}

//Configuration and synchronization
def updated() {
	if ( state.lastUpdated && (now() - state.lastUpdated) < 500 ) return
	def cmds = []
	logging("Executing updated()","info")
	state.lastUpdated = now()
	syncStart()
}

private syncStart() {
	boolean syncNeeded = false
	boolean syncNeededGroup = false
	Integer settingValue = null
	parameterMap().each {
		if (settings."$it.key" != null) {
			settingValue = settings."$it.key" as Integer
			if (state."$it.key" == null) { state."$it.key" = [value: null, state: "synced"] }
			if ( state."$it.key".value != settingValue || state."$it.key".state != "synced" ) {
				state."$it.key".value = settingValue
				state."$it.key".state = "notSynced"
				syncNeeded = true
			}
		}
	}
    
	if (syncNeeded) {
		logging("sync needed.", "info")
		syncNext()
	}
	if (syncNeededGroup) {
		logging("${device.displayName} - starting sync.", "info")
		multiStatusEvent("Sync in progress.", true, true)
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
			logging("Parameter number ${param.num}. Parameter Value: ${state."$param.key"?.value}","info")
			cmds << response(encap(zwave.configurationV1.configurationSet(configurationValue: intToParam(state."$param.key".value, param.size), parameterNumber: param.num, size: param.size)))
			cmds << response(encap(zwave.configurationV1.configurationGet(parameterNumber: param.num)))
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

private syncCheck() {
	logging("Executing syncCheck()","info")
	def failed = []
	def incorrect = []
	def notSynced = []
	parameterMap().each {
		if (state."$it.key"?.state == "incorrect") {
			incorrect << it
		} else if (state."$it.key"?.state == "failed") {
			failed << it
		} else if (state."$it.key"?.state in ["inProgress","notSynced"]) {
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

private multiStatusEvent(String statusValue, boolean force = false, boolean display = false) {
	if ( !device.currentValue("multiStatus")?.contains("Sync") || device.currentValue("multiStatus") == "Sync OK." || force ) {
		sendEvent(name: "multiStatus", value: statusValue, descriptionText: statusValue, displayed: display)
	}
}

private deviceIdEvent(String value, boolean force = false, boolean display = false) {
	sendEvent(name: "deviceID", value: value, descriptionText: value, displayed: display)
}

//event handlers related to configuration and sync
def zwaveEvent(physicalgraph.zwave.commands.configurationv1.ConfigurationReport cmd) {
	def paramKey = parameterMap().find( {it.num == cmd.parameterNumber } ).key
	logging("Parameter ${paramKey} value is ${cmd.scaledConfigurationValue} expected " + state."$paramKey".value, "info")
	state."$paramKey".state = (state."$paramKey".value == cmd.scaledConfigurationValue) ? "synced" : "incorrect"
	syncNext()
}

def zwaveEvent(physicalgraph.zwave.commands.applicationstatusv1.ApplicationRejectedRequest cmd) {
	logging("rejected request!","warn")
	for ( param in parameterMap() ) {
		if (state."$param.key"?.state == "inProgress") {
			state."$param.key"?.state = "failed"
			break
		}
	}
}
//event handlers
def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd) {
	//ignore
}
def zwaveEvent(physicalgraph.zwave.commands.switchbinaryv1.SwitchBinaryReport cmd) {
	logging("SwitchBinaryReport received, value: ${cmd.value} ","info")
	sendEvent([name: "switch", value: (cmd.value == 0 ) ? "off": "on"])
}

/*
####################
## Z-Wave Toolkit ##
####################
*/
def parse(String description) {
	def result = []
	def deviceId = [];
	logging("Parsing: ${description}")
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
		deviceId = description.split(", ")[0]
		deviceId = deviceId.split(":")[1]
		logging("deviceId- ${deviceId}")
		deviceIdEvent(deviceId, true, true)
		def cmd = zwave.parse(description, cmdVersions())
		if (cmd) {
			logging("Parsed: ${cmd}")
			zwaveEvent(cmd)
		}
	}
}

def zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityMessageEncapsulation cmd) {
	def encapsulatedCommand = cmd.encapsulatedCommand(cmdVersions())
	if (encapsulatedCommand) {
		logging("Parsed SecurityMessageEncapsulation into: ${encapsulatedCommand}")
		zwaveEvent(encapsulatedCommand)
	} else {
		logging("Unable to extract Secure command from $cmd","warn")
	}
}

def zwaveEvent(physicalgraph.zwave.commands.crc16encapv1.Crc16Encap cmd) {
	def version = cmdVersions()[cmd.commandClass as Integer]
	def ccObj = version ? zwave.commandClass(cmd.commandClass, version) : zwave.commandClass(cmd.commandClass)
	def encapsulatedCommand = ccObj?.command(cmd.command)?.parse(cmd.data)
	if (encapsulatedCommand) {
		logging("Parsed Crc16Encap into: ${encapsulatedCommand}")
		zwaveEvent(encapsulatedCommand)
	} else {
		logging("Unable to extract CRC16 command from $cmd","warn")
	}
}

def zwaveEvent(physicalgraph.zwave.commands.multichannelv3.MultiChannelCmdEncap cmd) {
	def encapsulatedCommand = cmd.encapsulatedCommand(cmdVersions())
	if (encapsulatedCommand) {
		logging("Parsed MultiChannelCmdEncap ${encapsulatedCommand}")
		zwaveEvent(encapsulatedCommand, cmd.sourceEndPoint as Integer)
	} else {
		logging("Unable to extract MultiChannel command from $cmd","warn")
	}
}

private logging(text, type = "debug") {
	if (settings.logging == "true" || type == "warn") {
		log."$type" "${device.displayName} - $text"
	}
}

private multiEncap(physicalgraph.zwave.Command cmd, Integer ep) {
	logging("encapsulating command using MultiChannel Encapsulation, ep: $ep command: $cmd","info")
	zwave.multiChannelV3.multiChannelCmdEncap(destinationEndPoint:ep).encapsulate(cmd)
}

private encap(physicalgraph.zwave.Command cmd, Integer ep) {
	encap(multiEncap(cmd, ep))
}

private encap(physicalgraph.zwave.Command cmd) {
	if (zwaveInfo.zw.contains("s")) {
		logging("encapsulating command using Secure Encapsulation, command: $cmd","info")
		zwave.securityV1.securityMessageEncapsulation().encapsulate(cmd).format()
	} else {
		logging("no encapsulation supported for command: $cmd","info")
		cmd.format()
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
##########################
## Device Configuration ##
##########################
*/
private Map cmdVersions() {
	[0x5E: 2, 0x86: 2, 0x72: 2, 0x59: 2, 0x98: 1, 0x25: 1, 0x5A: 1, 0x85: 2, 0x70: 1, 0x8E: 2, 0x6C: 1] 
}

private parameterMap() {[
		[key: "NumClicks", num: 1, size: 1, type: "number", min: 0, max: 7, def: 7, title: "Numbers of clicks to control the loads",
			descr: "Define which sequences of clicks control the load (see device manual)."],
		[key: "OffTimer", num: 10, size: 2, type: "number", def: 0, min: 0, max: 32000, title: " Timer to switch OFF the Relay",
			descr: "Defines the time after which the relay is switched OFF. Time unit is set by parameter 15(see device manual)"],
		[key: "OnTimer", num: 11, size: 2, type: "number", def: 0, min: 0, max: 32000, title: " Timer to switch ON the Relay",
			descr: "Defines the time after which the relay is switched ON. Time unit is set by parameter 15(see device manual)"],
		[key: "timerScale", num: 15, size: 1, type: "enum", options: [
			1: "Tenth of seconds",
			2: "Seconds",
		], def: "1", title: "Timer scale", descr: "Defines the time unit used for parameters No.10 and No.11"],
		[key: "oneClickScene", num: 20, size: 2, type: "number",min: 0, max: 255, def: 0, title: "One Click Scene ActivationSet",
			descr: "Defines the Scene Activation Set value sent to the Lifeline group with 1 Clickon the external switch"],
		[key: "twoClickScene", num: 21, size: 2, type: "number",min: 0, max: 255, def: 0, title: "Two Clicks Scene ActivationSet",
			descr: "Defines the Scene Activation Set value sent to the Lifeline group with 2 Clickson the external switch"],
		[key: "threeClickScene", num: 22, size: 2, type: "number",min: 0, max: 255, def: 0, title: "Three Clicks Scene ActivationSet",
			descr: "Defines the Scene Activation Set value sent to the Lifeline group with 1 Clicks on the external switch"],
		[key: "startUpStatus", num: 60, size: 1, type: "enum", options: [
			1: "ON",
			2: "OFF",
			3: "PREVIOUS STATUS"
		], def: "3", title: "Start-up status",
			descr: "Defines the status of the device following a restart"],
		[key: "externalSwitchType", num: 62, size: 1, type: "enum", options: [
			0: "IGNORE",
			1: "BUTTON",
			2: "SWITCH"
			], def: "1", title: " Type of external switches",
			descr: "Defines the type of external switch"],
]}
