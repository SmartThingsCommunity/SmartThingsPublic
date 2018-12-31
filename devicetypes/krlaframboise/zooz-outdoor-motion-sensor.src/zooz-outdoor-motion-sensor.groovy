/**
 *  Zooz Outdoor Motion Sensor v1.0
 *    (Model: ZSE29)
 *
 *  Author: 
 *    Kevin LaFramboise (krlaframboise)
 *
 *  URL to documentation:
 *   
 *
 *  Changelog:
 *
 *    1.0 (10/16/2017)
 *      - Initial Release 
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
	definition (name:"Zooz Outdoor Motion Sensor", namespace:"krlaframboise", author: "Kevin LaFramboise", vid: "generic-motion-5") {
		capability "Sensor"
		capability "Battery"
		capability "Motion Sensor"
		capability "Tamper Alert"
		capability "Refresh"
		capability "Configuration"
		capability "Health Check"

		attribute "lastCheckIn", "string"
		
		fingerprint mfr:"027A", prod:"0001", model:"0005"
	}

	preferences {
		input "checkInInterval", "enum",
			title: "Check In Interval:",
			defaultValue: checkInIntervalSetting,
			required: false,
			displayDuringSetup: true,
			options: checkInIntervalOptions
		input "debugOutput", "bool", 
			title: "Enable debug logging?", 
			defaultValue: false, 
			displayDuringSetup: true, 
			required: false
	}

	tiles(scale: 2) {
		multiAttributeTile(name:"mainTile", type: "generic", width: 6, height: 4){
			tileAttribute ("device.motion", key: "PRIMARY_CONTROL") {
				attributeState("inactive", label:'no motion', icon:"st.motion.motion.inactive", backgroundColor:"#cccccc")
				attributeState("active", label:'motion', icon:"st.motion.motion.active", backgroundColor:"#00a0dc")
			}
			tileAttribute ("device.tamper", key: "SECONDARY_CONTROL") {
				attributeState("clear", label:'')
				attributeState("detected", label:'TAMPERING')
			}
		}
		valueTile("battery", "device.battery", decoration: "flat", width: 2, height: 2) {
			state "battery", label:'${currentValue}% Battery', unit:"%"
		}
		standardTile("refresh", "command.refresh", width: 2, height: 2) {
			state "default", label:"Refresh", action: "refresh", icon:"st.secondary.refresh-icon"
		}
		main("mainTile")
		details(["mainTile", "battery", "refresh"])
	}
}


def installed() {
	state.refreshAll = true
}


def updated() {	
	if (!isDuplicateCommand(state.lastUpdated, 3000)) {
		state.lastUpdated = new Date().time
		logTrace "updated()"
		
		if (state.checkInInterval != checkInIntervalSetting) {
			refresh()
		}		
	}
}


def configure() {	
	logTrace "configure()"
	def cmds = []
	
	if (!state.checkInInterval) {
		// First time configuring so give it time for inclusion to finish.
		cmds << "delay 2000"			
	}
	
	if (state.refreshAll || state.checkInInterval != checkInIntervalSetting) {
		cmds << wakeUpIntervalSetCmd(checkInIntervalSetting)
		cmds << wakeUpIntervalGetCmd()
	}
		
	if (canReportBattery()) {
		cmds << batteryGetCmd()
	}
	
	state.refreshAll = false
	return cmds ? delayBetween(cmds, 1000) : []
}


// Required for HealthCheck Capability, but doesn't actually do anything because this device sleeps.
def ping() {
	logDebug "ping()"	
}


def refresh() {	
	log.warn "The wakeup interval will be sent to the device the next time it wakes up.  You can force the device to wake up immediately by removing the battery for a few seconds and then putting it back in."
	state.refreshAll = true
	return []
}


private wakeUpIntervalSetCmd(seconds) {
	return secureCmd(zwave.wakeUpV2.wakeUpIntervalSet(seconds:seconds, nodeid:zwaveHubNodeId))
}

private wakeUpIntervalGetCmd() {
	return secureCmd(zwave.wakeUpV2.wakeUpIntervalGet())
}

private wakeUpNoMoreInfoCmd() {
	return secureCmd(zwave.wakeUpV2.wakeUpNoMoreInformation())
}

private batteryGetCmd() {
	return secureCmd(zwave.batteryV1.batteryGet())
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
		0x59: 1,	// AssociationGrpInfo
		0x55: 1,	// Transport Service
		0x5A: 1,	// DeviceResetLocally
		0x5E: 2,	// ZwaveplusInfo
		0x6C: 1,	// Supervision
		0x71: 3,	// Notification (4)
		0x72: 2,	// ManufacturerSpecific
		0x73: 1,	// Powerlevel
		0x7A: 2,	// Firmware Update Md (3)
		0x80: 1,	// Battery
		0x84: 2,  // WakeUp
		0x85: 2,	// Association
		0x86: 1,	// Version (2)
		0x98: 1,	// Security 0
		0x9F: 1		// Security 2
	]
}


def parse(String description) {	
	def result = []
	
	sendLastCheckInEvent()
	
	def cmd = zwave.parse(description, commandClassVersions)
	if (cmd) {
		result += zwaveEvent(cmd)
	}
	else {
		logDebug "Unknown Description: $desc"
	}

	return result
}

private sendLastCheckInEvent() {	
	if (!isDuplicateCommand(state.lastCheckIn, 60000)) {
		state.lastCheckIn = new Date().time		
		sendEvent(name: "lastCheckIn", value: convertToLocalTimeString(new Date()), displayed: false, isStateChange: true)
	}
}

private convertToLocalTimeString(dt) {
	try {
		def timeZoneId = location?.timeZone?.ID
		if (timeZoneId) {
			return dt.format("MM/dd/yyyy hh:mm:ss a", TimeZone.getTimeZone(timeZoneId))
		}
		else {
			return "$dt"
		}	
	}
	catch (e) {
		return "$dt"
	}
}


def zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityMessageEncapsulation cmd) {
	def encapCmd = cmd.encapsulatedCommand(getCommandClassVersions())
		
	def result = []
	if (encapCmd) {
		result += zwaveEvent(encapCmd)
	}
	else {
		log.warn "Unable to extract encapsulated cmd from $cmd"		
	}
	return result
}


def zwaveEvent(physicalgraph.zwave.commands.wakeupv2.WakeUpIntervalReport cmd) {
	logTrace "WakeUpIntervalReport: $cmd"
	
	state.checkInInterval = cmd.seconds
	
	// Set the Health Check interval so that it can be skipped twice plus 5 minutes.
	def checkInterval = ((cmd.seconds * 2) + (5 * 60))
	
	sendEvent(name: "checkInterval", value: checkInterval, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])
	
	return []
}


def zwaveEvent(physicalgraph.zwave.commands.wakeupv2.WakeUpNotification cmd)
{
	logDebug "Device Woke Up"
	
	def cmds = configure()		
	if (cmds) {
		cmds << "delay 1000"
	}
	cmds << wakeUpNoMoreInfoCmd()
	
	return response(cmds)	
}

private canReportBattery() {
	def reportEveryMS = (12 * 60 * 60 * 1000) // 12 Hours		
	return (state.refreshAll || !state.lastBatteryReport || ((new Date().time) - state.lastBatteryReport > reportEveryMS)) 
}


def zwaveEvent(physicalgraph.zwave.commands.batteryv1.BatteryReport cmd) {
	def val = (cmd.batteryLevel == 0xFF ? 1 : cmd.batteryLevel)
	
	if (val > 100) {
		val = 100
	}	
	
	state.lastBatteryReport = new Date().time	
	
	logDebug "Battery is ${val}%"
	sendEvent(name:"battery", value:val, unit:"%")
	return []
}


def zwaveEvent(physicalgraph.zwave.commands.notificationv3.NotificationReport cmd) {
	if (cmd.notificationType == 7) {
		switch (cmd.event) {
			case 3:
				sendTamperEventMap("detected")
				break
			case 8:
				sendMotionEventMap("active")				
				break
			case 0:
				if (cmd.eventParametersLength && cmd.eventParameter[0] == 8) { 
					sendMotionEventMap("inactive")
				}
				else {
					sendTamperEventMap("clear")
				}
				break
			default:
				logTrace "Unknown Notification Event: ${cmd.event}"
		}		
	}	
	else if (cmd.notificationType == 8 && cmd.event == 1) {
		logDebug "Device Powered On"
		def cmds = configure()
		return cmds ? response(cmds) : []
	}
	else {
		logTrace "Unknown Notification Type: ${cmd.notificationType}"
	}	
	return []
}

private sendTamperEventMap(val) {
	logDebug "Tamper is ${val}"
	sendEvent(name:"tamper", value:val, displayed:(val == "detected"), isStateChange: true)
}

private sendMotionEventMap(val) {
	logDebug "Motion is ${val}"
	sendEvent(name:"motion", value:val, isStateChange: true)
}


def zwaveEvent(physicalgraph.zwave.Command cmd) {
	logDebug "Unknown Command: $cmd"
	return []
}


// Settings
private getCheckInIntervalSetting() {
	return safeToInt(settings?.checkInInterval, 14400)
}

 
private getCheckInIntervalOptions() {
	[
		[600: "10 Minutes"],
		[1800: "30 Minutes"],
		[3600: "1 Hour"],
		[7200: "2 Hours"],
		[14400: "4 Hours (DEFAULT)"],
		[28800: "8 Hours"],
		[43200: "12 Hours"],
		[86400: "1 Day"]
	]
}

private safeToInt(val, defaultVal=0) {
	return "${val}"?.isInteger() ? "${val}".toInteger() : defaultVal
}


private isDuplicateCommand(lastExecuted, allowedMil) {
	!lastExecuted ? false : (lastExecuted + allowedMil > new Date().time) 
}

private logDebug(msg) {
	if (settings?.debugOutput	!= false) {
		log.debug "$msg"
	}
}

private logTrace(msg) {
	// log.trace "$msg"
}