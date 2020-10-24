/**
 *  Ecolink Siren v1.0
 *  (Model: SC-ZWAVE5-ECO)
 *
 *  Author: 
 *    Kevin LaFramboise (krlaframboise)
 *
 *	The device is fully functional if selected as switch:
 *	- Switch On: Siren On
 *	- Switch Off: Turns Everything Off
 *	- Set Level 10%: Chime/Beep
 *	- Set Level 20%: Entry/Continuous Tone
 *	- Set Level 30%: Exit/Repeating Beep
 *    
 *
 *  Changelog:
 *
 *    1.0 (07/17/2018)
 *      - Initial Release
 *
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
 */

metadata {
	definition (name: "Ecolink Siren", namespace: "krlaframboise", author: "Kevin LaFramboise") {
		capability "Actuator"
		capability "Alarm"
		capability "Switch"
		capability "Tone"
		capability "Refresh"
		capability "Health Check"
		capability "Switch Level"
		
		attribute "lastCheckIn", "string"
		
		command "continuousTone"
		command "repeatingBeep"
		
		fingerprint mfr:"014A", prod:"0005", model:"000A", deviceJoinName:"Ecolink Siren"
	}

	simulator {	}
	
	tiles(scale:2) {
			multiAttributeTile(name:"switch", type: "generic", width: 6, height: 4, canChangeIcon: true){
			tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
				attributeState "on", label:'${name}', action:"switch.off", icon:"st.alarm.alarm.alarm", backgroundColor:"#e86d13"
				attributeState "off", label:'${name}', action:"switch.on", icon:"st.alarm.alarm.alarm", backgroundColor:"#ffffff"
			}
     }
		 
		standardTile("siren", "device.alarm", width: 2, height: 2) {
			state "default", 
				label:'Siren', 
				action:"alarm.both"
		}
				
		standardTile("off", "device.switch", width: 2, height: 2) {
			state "default", 
				label:'Off', 
				action:"alarm.off"
		}
		
		standardTile("refresh", "device.refresh", width: 2, height: 2) {
			state "default", 
				label:'Refresh', 
				action:"refresh.refresh"
		}
		
		standardTile("beep", "generic", width: 2, height: 2) {
			state "default", 
				label:'Beep', 
				action:"tone.beep"
		}
		
		standardTile("continuousTone", "generic", width: 2, height: 2) {
			state "default", 
				label:'Continuous Tone', 
				action:"continuousTone"
		}
		
		standardTile("repeatingBeep", "generic", width: 2, height: 2) {
			state "default", 
				label:'Repeating Beep', 
				action:"repeatingBeep"
		}
				
		main(["switch"])
		details(["switch", "off", "siren", "refresh", "beep", "continuousTone", "repeatingBeep"])
	}
	
	preferences {
		input "debugOutput", "bool", 
			title: "Enable Debug Logging?", 
			defaultValue: true, 
			required: false
	}
}


def updated() {	
	if (!isDuplicateCommand(state.lastUpdated, 2000)) {
		logTrace "updated()..."
		state.lastUpdated = new Date().time
	
		initializeCheckin()
	}
}

private initializeCheckin() {
	def checkInterval = (6 * 60 * 60) + (5 * 60)
	
	sendEvent(name: "checkInterval", value: checkInterval, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])
	
	startHealthPollSchedule()
}

private startHealthPollSchedule() {
	unschedule(healthPoll)
	runEvery3Hours(healthPoll)
}

def healthPoll() {
	logTrace "healthPoll()"	
	sendHubCommand([new physicalgraph.device.HubAction(versionGetCmd())])
}


def ping() {
	logTrace "ping()"
	// Don't allow it to ping the device more than once per minute.
	if (!isDuplicateCommand(state.lastCheckInTime, 60000)) {
		logDebug "Attempting to ping device."
		// Restart the polling schedule in case that's the reason why it's gone too long without checking in.
		startHealthPollSchedule()
		
		return [versionGetCmd()]
	}	
}


def siren() {
	logDebug "siren()..."
	return getOnCmds(sirenEP)
}

def strobe() {
	logDebug "strobe()..."
	return getOnCmds(sirenEP)
}

def both() {
	logDebug "both()..."
	return getOnCmds(sirenEP)
}

def setLevel(level, duration=null) {
	if (level == 20) {
		return continuousTone()
	}
	else if (level == 30) {
		return repeatingBeep()
	}
	else {
		return beep()
	}
}

def on() {
	logDebug "on()..."	
	return getOnCmds(sirenEP)
}

def off() {
	logDebug "off()..."
	return getOffCmds()
}


def beep() {
	logDebug "beep()..."
	return getOnCmds(beepEP)	
}

def continuousTone() {
	logDebug "continuousTone()..."
	return getOnCmds(continuousToneEP)
}

def repeatingBeep() {
	logDebug "repeatingBeep()..."
	return getOnCmds(repeatingBeepEP)
}


private getOnCmds(endPoint) {
	return delayBetween([
		basicSetCmd(0xFF, endPoint),
		basicGetCmd(endPoint)
	], 50)
}

private getOffCmds() {
	def cmds = []
	endPoints.each {
		cmds << basicSetCmd(0x00, it)
		cmds << basicGetCmd(it)
	}
	return delayBetween(cmds, 50)
}


def refresh() {
	logDebug "refresh()..."
	def cmds = []	
	endPoints.each {
		cmds << basicGetCmd(it)
	}	
	return delayBetween(cmds, 50)	
} 


private versionGetCmd() {
	return secureCmd(zwave.versionV1.versionGet())
}

private basicSetCmd(val, endPoint) {
	return multiChannelCmdEncapCmd(zwave.basicV1.basicSet(value: val), endPoint)
}

private basicGetCmd(endPoint) {
	return multiChannelCmdEncapCmd(zwave.basicV1.basicGet(), endPoint)
}

private multiChannelCmdEncapCmd(cmd, endPoint) {	
	return secureCmd(zwave.multiChannelV3.multiChannelCmdEncap(destinationEndPoint:endPoint).encapsulate(cmd))
}

private secureCmd(cmd) {
	if (zwaveInfo?.zw?.contains("s") || ("0x98" in device.rawDescription?.split(" "))) {
		return zwave.securityV1.securityMessageEncapsulation().encapsulate(cmd).format()
	}
	else {
		return cmd.format()
	}	
}

private getCommandClassVersions() {
	[
		0x20: 1,	// Basic
		0x59: 1,	// AssociationGrpInfo
		0x5A: 1,	// DeviceResetLocally
		0x5E: 2,	// ZwaveplusInfo
		0x60: 3,	// Multi Channel (4)
		0x72: 2,	// ManufacturerSpecific
		0x73: 1,	// Powerlevel
		0x7A: 2,	// Firmware Update Md
		0x85: 2,	// Association
		0x8E: 3,	// Multi Channel Association
		0x86: 1		// Version (2)
	]
}


def parse(description) {	
	def result = null
	
	if (description != "updated") {
		def cmd = zwave.parse(description, commandClassVersions)
		if (cmd) {
			result = zwaveEvent(cmd)			
		} 
		else {
			logDebug("Couldn't zwave.parse '$description'")
		}
	}
	
	if (!isDuplicateCommand(state.lastCheckInTime, 60000)) {
		sendLastCheckInEvent()
	}
	
	return result
}

def zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityMessageEncapsulation cmd) {
	def encapsulatedCmd = cmd.encapsulatedCommand(commandClassVersions)	
	
	def result = []
	if (encapsulatedCmd) {
		result += zwaveEvent(encapsulatedCmd)
	}
	else {
		log.warn "Unable to extract encapsulated cmd from $cmd"
	}
	return result
}

def zwaveEvent(physicalgraph.zwave.commands.multichannelv3.MultiChannelCmdEncap cmd) {
	def encapsulatedCommand = cmd.encapsulatedCommand([0x31: 3])
	
	if (encapsulatedCommand) {
		return zwaveEvent(encapsulatedCommand, cmd.sourceEndPoint)
	}
	else {
		logDebug "Unable to get encapsulated command: $cmd"
		return []
	}
}


def zwaveEvent(physicalgraph.zwave.commands.versionv1.VersionReport cmd) {
	logTrace "VersionReport: $cmd"	
	// Using this event for health monitoring to update lastCheckin
	return []
}


def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd, endPoint=null) {
	logTrace "BasicReport: $cmd"
	
	def switchVal = cmd.value ? "on" : "off"
	if (device.currentValue("switch") != switchVal) {
		sendEvent(name: "switch", value: switchVal)
	}
	
	if (endPoint == sirenEP) {
		def alarmVal = cmd.value ? "both" : "off"
		if (device.currentValue("alarm") != alarmVal) {
			sendEvent(name: "alarm", value: alarmVal)
		}
	}	
	
	if (cmd.value && endPoint == beepEP) {
		sendResponse(["delay 1000", basicGetCmd(beepEP)])
	}
	return []
}

private sendResponse(cmds) {
	def actions = []
	cmds?.each { cmd ->
		actions << new physicalgraph.device.HubAction(cmd)
	}	
	sendHubCommand(actions)
	return []
}


def zwaveEvent(physicalgraph.zwave.Command cmd) {
	logTrace "unhandled: $cmd"
	return []
}


private getEndPoints() { [sirenEP, beepEP, continuousToneEP, repeatingBeepEP] }
private getSirenEP() { return 1 }
private getBeepEP() { return 2 }
private getContinuousToneEP() { return 3 }
private getRepeatingBeepEP() { return 4 }


private sendLastCheckInEvent() {
	state.lastCheckInTime = new Date().time
	logDebug "Device Checked In"	
	sendEvent(name: "lastCheckIn", value: convertToLocalTimeString(new Date()), displayed: false)
}

private convertToLocalTimeString(dt) {
	def timeZoneId = location?.timeZone?.ID
	if (timeZoneId) {
		return dt.format("MM/dd/yyyy hh:mm:ss a", TimeZone.getTimeZone(timeZoneId))
	}
	else {
		return "$dt"
	}	
}

private isDuplicateCommand(lastExecuted, allowedMil) {
	!lastExecuted ? false : (lastExecuted + allowedMil > new Date().time) 
}

private logDebug(msg) {
	if (settings?.debugOutput != false) {
		log.debug "$msg"
	}
}

private logTrace(msg) {
	// log.trace "$msg"
}