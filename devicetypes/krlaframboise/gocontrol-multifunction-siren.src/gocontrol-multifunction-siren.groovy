/**
 *  GoControl Multifunction Siren v 1.8.1
 *
 *  Devices:
 *    GoControl/Linear (Model#: WA105DBZ-1 / ZM1601US-3)
 *    Vision Home Security (Model#: ZM1601US-5)
 *  	LinearLinc Z-Wave Siren/Strobe (Model#: 1LIWA105DBZ-2)
 *
 *  Capabilities:
 *      Alarm, Tone, Audio Notification, Switch
 *      Battery
 *   
 *   ********************************************* 
 *   ** The Speech Synthesis and Music Player   **
 *   ** capabilities can ONLY be used to send   **
 *   ** specific commands to the device through **
 *   ** using any SmartApp.                     ** 
 *   *********************************************
 *
 *  Author: 
 *     Kevin LaFramboise (krlaframboise)
 *
 *  Url to Documentation:
 *      https://community.smartthings.com/t/release-gocontrol-linear-multifunction-siren/47024?u=krlaframboise
 *
 *  Changelog:
 *
 *    1.8.1 (07/22/2017)
 *    	- Fixed issue caused by the hub firmware update 000.018.00018
 *    	- If you're on hub v1 or you installed the device prior to May 2016, make sure you test the device after updating to this version.
 *
 *    1.7.4 (04/23/2017)
 *    	- SmartThings broke parse method response handling so switched to sendhubaction.
 *
 *    1.7.3 (04/09/2017)
 *    	- Bug fix for location timezone issue.
 *
 *    1.7.2 (03/21/2017)
 *    	- Fix for SmartThings TTS url changing.
 *
 *    1.7.1 (03/11/2017)
 *      - Don't display off status event when already off.
 *      - Removed polling capability.
 *
 *    1.7 (02/19/2017)
 *      - Added Health Check and self polling.
 *
 *    1.6.4 (02/07/2017)
 *      - Fixed all audio commands so that they don't use explit types due to SmartApps not following documented capabilities.
 *
 *    1.6.3 (02/06/2017)
 *      - Fixed audio commands for speaker notify w/ sound because it doesn't use the capabilities as documented.
 *
 *    1.6.2 (08/30/2016)
 *      - Added another model# to header.
 *
 *    1.6.1 (08/28/2016)
 *      - Added support for turning on the alarm while a
 *        different alarm type is already playing.
 *
 *    1.6 (08/06/2016)
 *      - Added support for Audio Notification capability
 *
 *    1.5.2 (07/20/2016)
 *      - Added support for Vision version of siren because
 *        it's the same device, but the configuration uses
 *        different parameter numbers.
 *
 *    1.4 (07/14/2016)
 *      - Added secure command support.
 *
 *    1.3 (06/17/2016)
 *      - Fixed delayed alarm with strobe bug.
 *      - Numbered settings to make it easier to explain things.
 *      - Added "Allow Siren Only Light" setting that allows you
 *        to display a solid red light while the siren is on.
 *      - Added parameters delaySeconds and useStrobe to the
 *        custom beep command that allow you to delay a beep
 *        and display the strobe during that delay.
 *
 *    1.2.1, 1.2.2, 1.2.3 (06/12/2016)
 *      - Improved beep performance. 
 *      - Fixed UI issue caused by latest android update.
 *        (unable to completely fix, but the buttons no longer
 *         completely disapear)
 *      - Fixed icons and colors in activity log.
 *      - Added always set alarm type setting.
 *
 *    1.2 (06/11/2016)
 *      - *** BREAKING CHANGES ***
 *            - Removed strobe instead of beep option from settings.
 *            - Removed always use both/on option from settings.
 *            - Removed beep(length) command, but it still works 
 *              in the speakText or playText fields.
 *      - Added settings for default alarm delay time and
 *        default strobe during delay.
 *      - Added commands customAlarm, customSiren, customBoth
 *      - Made it check the current alarmtype and only change it
 *        as needed which prevents it from always flashing before
 *        turning on.
 *      - Misc bug fixes.
 *
 *    1.1 (05/21/2016)
 *      - Improved polling functionality.
 *
 *    1.0.3 (05/04/2016 - 05/12/2016)
 *      - Enhanced reporting of status, alarm, and switch state.
 *      - Enhanced activity feed messages.
 *      - Enhanced debug logging.
 *      - Improved beep reliability a little bit.
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
	definition (name: "GoControl Multifunction Siren", namespace: "krlaframboise", author: "Kevin LaFramboise") {
		capability "Actuator"
		capability "Alarm"
		capability "Battery"
		capability "Music Player"
		capability "Audio Notification"
		capability "Speech Synthesis"
		capability "Health Check"
		capability "Switch"
		capability "Tone"
		
		attribute "lastCheckin", "string"

		command "customBeep" // beepLengthMS, delaySeconds, useStobe
		command "customBoth" // delaySeconds, autoOffSeconds, useStrobe
		command "customSiren" // delaySeconds, autoOffSeconds, useStrobe
		command "customStrobe" // delaySeconds, autoOffSeconds
		
		// Audio Notification commands that are in documentation,
		// but not in the capability code.
		command "playTrackAtVolume"
		command "playSoundAndTrack"

	
		fingerprint mfr: "0109", prod: "2005", model: "0508" //Vision
		
		fingerprint mfr: "014F", prod: "2005", model: "0503" //Linear/GoControl Battery Only
		
		fingerprint mfr: "014F", prod: "2009", model: "0903" //Linear/GoControl Powered (no battery reporting)
		
		fingerprint deviceId: "0x1000", inClusters: "0x25,0x70,0x72,0x86"
		fingerprint deviceId: "0x1005", inClusters: "0x25,0x5E,0x72,0x80,0x86"
	}

	simulator {
		reply "2001FF,2002": "command: 2003, payload: FF"
		reply "200100,2002": "command: 2003, payload: 00"
	}

	preferences {
		input "autoOffTime", "enum", 
			title: "1. Automatically turn off after:\n(You should disable this feature while testing)", 
			defaultValue: "30 Seconds",
			displayDuringSetup: true, 
			required: false,
			options: ["30 Seconds", "60 Seconds", "120 Seconds", "Disable Auto Off"]		
		input "bothAlarmTypeOverride", "enum",
			title: "2. What should the 'both' and 'on' commands turn on?\n(Some SmartApps like Smart Home Monitor use the both command so you can't use just the siren or the strobe.  This setting allows you to override the default action of those commands.)",
			defaultValue: "Siren and Strobe",
			displayDuringSetup: true,
			required: false,
			options: ["Siren and Strobe", "Siren Only", "Strobe Only"]
		input "alwaysSetAlarmType", "bool", 
			title: "3. Always Set Alarm Type?\n(Enabling this option will make the device turn on quicker, but the strobe will always flash once before turning on.)", 
			defaultValue: false, 
			displayDuringSetup: false, 
			required: false
		input "allowSirenOnlyLight", "bool",
			title: "4. Allow Siren Only Light?\n(When this option is enabled, the strobe light will stay on solid when using the siren command.)",
			defaultValue: false,
			displayDuringSetup: false,
			required: false
		input "alarmDelayStrobe", "bool", 
			title: "5. Default Use Strobe During Alarm Delay?", 
			defaultValue: false, 
			displayDuringSetup: true, 
			required: false
		input "alarmDelaySeconds", "number", 
			title: "6. Default Alarm Delay (seconds):", 
			defaultValue: 0, 
			displayDuringSetup: true, 
			required: false		
		input "beepLength", "number", 
			title: "7. Default Length of Beep (milliseconds):", 
			defaultValue: 0, 
			displayDuringSetup: true, 
			required: false
		input "checkinInterval", "enum",
			title: "Checkin Interval:",
			defaultValue: checkinIntervalSetting,
			required: false,
			displayDuringSetup: true,
			options: checkinIntervalOptions.collect { it.name }
		input "batteryReportingInterval", "enum",
			title: "Battery Reporting Interval:",
			defaultValue: batteryReportingIntervalSetting,
			required: false,
			displayDuringSetup: true,
			options: checkinIntervalOptions.collect { it.name }
		input "debugOutput", "bool", 
			title: "8. Enable debug logging?", 
			defaultValue: true, 
			displayDuringSetup: true, 
			required: false
	}

	tiles(scale: 2) {
		multiAttributeTile(name:"status", type: "generic", width: 6, height: 3, canChangeIcon: true){
			tileAttribute ("device.status", key: "PRIMARY_CONTROL") {
				attributeState "off", label:'Off', action: "off", icon:"st.alarm.alarm.alarm", backgroundColor:"#ffffff"
				attributeState "alarmPending", label:'Alarm Pending!', action: "off", icon:"st.alarm.alarm.alarm", backgroundColor:"#ff9999"
				attributeState "siren", label:'Siren On!', action: "off", icon:"st.alarm.alarm.alarm", backgroundColor:"#ff9999"
				attributeState "strobe", label:'Strobe On!', action: "off", icon:"st.alarm.alarm.alarm", backgroundColor:"#ff9999"
				attributeState "both", label:'Siren/Strobe On!', action: "off", icon:"st.alarm.alarm.alarm", backgroundColor:"#ff9999"
				attributeState "beep", label:'Beeping!', action: "off", icon:"st.Entertainment.entertainment2", backgroundColor:"#99ff99"							
			}
		}
		standardTile("turnOff", "device.alarm", width: 2, height: 2) {
			state "default", label:'Off', action: "off", backgroundColor: "#99c2ff", icon:"st.alarm.alarm.alarm", defaultState: true
			state "off", label:'Off', action: "off", backgroundColor: "#ffffff", icon:"st.alarm.alarm.alarm"
			state "alarmPending", label:'Cancel', action: "off", backgroundColor: "#99c2ff", icon:"st.alarm.alarm.alarm"
		}	
		standardTile("testBeep", "device.status", width: 2, height: 2) {
			state "default", label:' Beep ', action:"beep", icon:"st.Entertainment.entertainment2", backgroundColor: "#99ff99", defaultState: true
			state "beep", label:'Beeping', action: "off", icon:"st.Entertainment.entertainment2", backgroundColor: "#99c2ff"			
		}					
		valueTile("battery", "device.battery", decoration: "flat", width: 2, height: 2) {
			state "battery", label:'Battery ${currentValue}%', unit:"", defaultState: true
		}		
		standardTile("testSiren", "device.alarm", width: 2, height: 2) {
			state "default", label:'Siren ', action: "alarm.siren", backgroundColor: "#ff9999", icon:"st.alarm.alarm.alarm", defaultState: true			
			state "siren", label:' Siren', action: "alarm.off", icon:"st.alarm.alarm.alarm", backgroundColor: "#99c2ff"		
		}
		standardTile("testStrobe", "device.alarm", width: 2, height: 2) {
			state "default", label:'Strobe', action: "alarm.strobe", backgroundColor: "#ff9999", icon:"st.alarm.alarm.alarm", defaultState: true			
			state "strobe", label:'Strobe', action: "alarm.off", backgroundColor: "#99c2ff", icon:"st.alarm.alarm.alarm"
		}
		standardTile("testBoth", "device.alarm", width: 2, height: 2) {
			state "default", label:' Both ', action: "alarm.both", backgroundColor: "#ff9999", icon:"st.alarm.alarm.alarm", defaultState: true
			state "both", label:' Both ', action: "alarm.off", backgroundColor: "#99c2ff", icon:"st.alarm.alarm.alarm"
		}		
				
		main "status"
		details(["status", "testSiren", "testStrobe", "testBoth", "turnOff", "testBeep", "battery"])
	}
}

// Stores preferences and displays device settings.
def updated() {
	if (!isDuplicateCommand(state.lastUpdated, 5000)) {
		state.lastUpdated = new Date().time
		state.alarmDelaySeconds = validateRange(settings.alarmDelaySeconds, 0, 0, Integer.MAX_VALUE, "alarmDelaySeconds") 
		state.alarmDelayStrobe = validateBoolean(settings.alarmDelayStrobe, false)
		logDebug "Updating"

		initializeCheckin()
		
		def cmds = []		
		cmds += configure()	
		return sendResponse(delayBetween(cmds, 200))
	}
}

private initializeCheckin() {
	// Set the Health Check interval so that it can be skipped once plus 2 minutes.
	def checkInterval = ((checkinIntervalSettingMinutes * 2 * 60) + (2 * 60))
	
	sendEvent(name: "checkInterval", value: checkInterval, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])
	
	startHealthPollSchedule()
}

private startHealthPollSchedule() {
	unschedule(healthPoll)
	switch (checkinIntervalSettingMinutes) {
		case 5:
			runEvery5Minutes(healthPoll)
			break
		case 10:
			runEvery10Minutes(healthPoll)
			break
		case 15:
			runEvery15Minutes(healthPoll)
			break
		case 30:
			runEvery30Minutes(healthPoll)
			break
		case [60, 120]:
			runEvery1Hour(healthPoll)
			break
		default:
			runEvery3Hours(healthPoll)			
	}
}

// Executed by internal schedule and requests a report from the device to determine if it's still online.
def healthPoll() {
	logTrace "healthPoll()"
	def cmd = canReportBattery() ? batteryGetCmd() : versionGetCmd() // Not using batteryGet every time because powered GoControl device doesn't support batteryGet.
	sendHubCommand(new physicalgraph.device.HubAction(cmd))
}

private canReportBattery() {
	def reportEveryMS = (batteryReportingIntervalSettingMinutes * 60 * 1000)
		
	return (!state.lastBatteryReport || ((new Date().time) - state.lastBatteryReport > reportEveryMS)) 
}

// Executed by SmartThings if the specified checkInterval is exceeded.
def ping() {
	logTrace "ping()"
	if (!isDuplicateCommand(state.lastCheckinTime, 60000)) {
		logDebug "Attempting to ping device."
		// Restart the polling schedule in case that's the reason why it's gone too long without checking in.
		startHealthPollSchedule()
		
		return versionGetCmd() // Using versionGet because GoControlled powered device doesn't support batteryGet.
	}	
}

private configure() {
	def cmds = []
	cmds << autoOffSetCmd(getAutoOffTimeValue())
	cmds << batteryGetCmd()
	return cmds
}

private getAutoOffTimeValue() {
	def result	
	switch (settings.autoOffTime) {
		case "60 Seconds":
			state.autoOffSeconds = 60
			result = 1
			break
		case "120 Seconds":
			state.autoOffSeconds = 120
			result = 2
			break
		case "Disable Auto Off":
			state.autoOffSeconds = 0
			result = 3
			break
		default:
			state.autoOffSeconds = 30
			result = 0 // 30 Seconds
	}
	return result
}

private isDuplicateCommand(lastExecuted, allowedMil) {
	!lastExecuted ? false : (lastExecuted + allowedMil > new Date().time) 
}

// Turns on siren and strobe
def on() {
	both()	
}

def beep() {
	customBeep(settings.beepLength, 0, false)
}

// Turns on and then off after specified milliseconds.
def customBeep(beepLengthMS, delaySeconds=0, useStrobe=false) {	
	beepLengthMS = validateRange(beepLengthMS, 0, 0, Integer.MAX_VALUE, "Beep Length")
	delaySeconds = validateRange(delaySeconds, 0, 0, 3600, "Beep Delay")
	useStrobe = validateBoolean(useStrobe, false)
	
	state.activeAlarm = null
	sendEvent(createStatusEventMap("beep"))
	
	def result = []	
	def delayMsg = ""
	if (delaySeconds > 0) {
		delayMsg = " with ${delaySeconds} Second${useStrobe ? ' Strobe ' : ' '}Delay"
		
		if (useStrobe) {
			result << alarmTypeSetCmd(getStrobeOnlyAlarmType())
			result << switchOnSetCmd()
			result << "delay ${delaySeconds * 1000}"
			result << switchOffSetCmd()
			result << alarmTypeSetCmd(getSirenOnlyAlarmType())
		}
		else {
			result << "delay ${delaySeconds * 1000}"
		}
	}	
	
	result << alarmTypeSetCmd(getSirenOnlyAlarmType())
	result << switchOnSetCmd()
	
	if (beepLengthMS > 0) {
		result << "delay $beepLengthMS"
	}
	result += switchOffSetCmds()
	
	logDebug "Executing ${beepLengthMS} Millisecond Beep$delayMsg"
	
	return result	
}

// Turns on siren and strobe using default autooff
def both() {	
	customBoth(state.alarmDelaySeconds, state.autoOffSeconds, state.alarmDelayStrobe)
}

// Turns on siren and strobe using specified auto off, delay and whether or not it should strobe during delay.
def customBoth(delaySeconds, autoOffSeconds, useStrobe) {
	turnOn(getSirenAndStrobeAlarmType(), delaySeconds, autoOffSeconds, useStrobe)	
}

// Turns on strobe using default autooff
def strobe() {
	customStrobe(state.alarmDelaySeconds, state.autoOffSeconds)	
}

// Turn on strobe using specified autooff.
def customStrobe(delaySeconds, autoOffSeconds) {	
	turnOn(getStrobeOnlyAlarmType(), delaySeconds, autoOffSeconds, false)
}

// Turns on siren using default auto off
def siren() {
	customSiren(state.alarmDelaySeconds, state.autoOffSeconds, state.alarmDelayStrobe)
}

// Turns on siren using specified auto off, delay and whether or not it should strobe during delay.
def customSiren(delaySeconds, autoOffSeconds, useStrobe) {
	turnOn(getSirenOnlyAlarmType(), delaySeconds, autoOffSeconds, useStrobe)	
}

// Stores a map with the alarm settings and requests the
// current alarmType from the device.
def turnOn(alarmType, delaySeconds, autoOffSeconds, useStrobe) {
	delaySeconds = validateRange(delaySeconds, 0, 0, Integer.MAX_VALUE, "delaySeconds")
	autoOffSeconds = validateRange(autoOffSeconds, 0, 0, Integer.MAX_VALUE, "autoOffSeconds")
	useStrobe = validateBoolean(useStrobe, false)
	
	state.activeAlarm = [
		alarmType: alarmType,
		autoOffSeconds: autoOffSeconds,
		delaySeconds: delaySeconds,
		useStrobe: useStrobe
	]
	
	def result = []
	if (delaySeconds > 0 && !useStrobe) {
		logDebug "Alarm delayed by ${delaySeconds} seconds"
		result += startDelayedAlarm(null)
	}
	else if (validateBoolean(settings.alwaysSetAlarmType, false) || validateBoolean(settings.allowSirenOnlyLight, false)) {
		// Not checking current alarm type so turn on with null to make sure the alarm type gets set.
		result += turnOn(null)
	}
	else {
		// Check the device's current alarm type and when it responds, the handler will turn the device on.
		result << alarmTypeGetCmd()
	}
	return result
}

def turnOn(currentAlarmType) {	
	def activeAlarm = state.activeAlarm
	
	if (activeAlarm) {
		def result = []
		if (activeAlarm.delaySeconds > 2 && activeAlarm.useStrobe) {
			logDebug "Alarm delayed with strobe for ${activeAlarm.delaySeconds} seconds."
			result += startDelayedAlarm(currentAlarmType)
		}
		else {
			state.activeAlarm.alarmPending = false			
			if (activeAlarm.alarmType != currentAlarmType) {			
				if (device.currentValue("alarm") != "off") {
					logDebug "Turning off alarm because it's already on."
					result << switchOffSetCmd()
					result << "delay 200"
				}			
				result += alarmTypeSetCmds(activeAlarm.alarmType)
			}

			result << switchOnSetCmd()
			result << switchGetCmd()

			if (activeAlarm.autoOffSeconds) {
				logDebug "Turning on Alarm for ${activeAlarm.autoOffSeconds} seconds."
				result << "delay ${activeAlarm.autoOffSeconds * 1000}"
				result += switchOffSetCmds()			
			}	
			else {
				logDebug "Turning on Alarm"
			}
		}
		return delayBetween(result, 100)
	}
	else {
		return []
	}
}

private startDelayedAlarm(currentAlarmType) {
	def result = []	
	def useStrobe = state.activeAlarm?.useStrobe
	
	if (useStrobe) {
		if (getStrobeOnlyAlarmType() != currentAlarmType) {
			result << alarmTypeSetCmd(getStrobeOnlyAlarmType())
		}
		result << switchOnSetCmd()
	}
	result << "delay ${state.activeAlarm.delaySeconds * 1000}"
	
	result << switchOffSetCmd()
	
	if (useStrobe && state.activeAlarm.alarmType != getStrobeOnlyAlarmType()) {
		result << alarmTypeSetCmd(getSirenOnlyAlarmType())
	}
	
	result << alarmTypeGetCmd()
			
	sendEvent(createStatusEventMap("alarmPending"))
	
	state.activeAlarm.delaySeconds = null
	state.activeAlarm.useStrobe = null
	state.activeAlarm.alarmPending = true
	
	return result
}

// Turns off siren and strobe
def off() {
	logDebug "Executing off() command"
	turnOff()
}

private turnOff() {
	return switchOffSetCmds()
}

private getSirenAndStrobeAlarmType() {
	def overriding = true
	def result
	switch (settings.bothAlarmTypeOverride) {
		case "Siren Only":
			result = getSirenOnlyAlarmType()
			break
		case "Strobe Only":
			result = getStrobeOnlyAlarmType()
			break
		default:
			overriding = false
			result = 0
	}
	if (overriding) {
		logDebug "Overriding \"both\" command with \"${settings.bothAlarmTypeOverride}\""
	}	
	return result
}

private getSirenOnlyAlarmType() {
	return 1
}

private getStrobeOnlyAlarmType() {
	return 2
}

private alarmTypeSetCmds(alarmType) {
	def result = []
	
	result << alarmTypeSetCmd(alarmType)
	
	if (alarmType == 1  && state.activeAlarm && !validateBoolean(settings.allowSirenOnlyLight, false)) {
		// Prevents strobe light from staying on when setting to Siren Only.
		result << switchBinaryGetCmd()
	}	
	return result	
}

private alarmTypeSetCmd(alarmType) {
	alarmType = validateRange(alarmType, 0, 0, 2, "Alarm Type")
	configSetCmd(getAlarmTypeParamNumber(), 1, alarmType)	
}

private alarmTypeGetCmd() {
	configGetCmd(getAlarmTypeParamNumber())
}

private getAlarmTypeParamNumber() {
	return isVisionMfr ? 1 : 0
}

private autoOffSetCmd(autoOff) {
	configSetCmd(getAutoOffParamNumber(), 1, validateRange(autoOff, 0, 0, 3, "Auto Off"))
}

private getAutoOffParamNumber() {
	return isVisionMfr ? 2 : 1
}

private configSetCmd(paramNumber, paramSize, paramValue) {
	secureCmd(zwave.configurationV1.configurationSet(parameterNumber: paramNumber, size: paramSize, configurationValue: [paramValue]))
}

private configGetCmd(paramNumber) {
	secureCmd(zwave.configurationV1.configurationGet(parameterNumber: paramNumber))
}

private versionGetCmd() {
	secureCmd(zwave.versionV1.versionGet())
}

private switchOnSetCmd() {
	secureCmd(zwave.basicV1.basicSet(value: 0xFF))
}

private switchOffSetCmds() {
	return [
		switchOffSetCmd(),
		switchGetCmd()
	]
}

private switchOffSetCmd() {
	secureCmd(zwave.basicV1.basicSet(value: 0x00))
}

private switchGetCmd() {	
	secureCmd(zwave.basicV1.basicGet())
}

private switchBinaryGetCmd() {
	secureCmd(zwave.switchBinaryV1.switchBinaryGet())
}

private batteryGetCmd() {
	secureCmd(zwave.batteryV1.batteryGet())
}

private secureCmd(cmd) {
	if (zwaveInfo?.zw?.contains("s") || state.useSecureCommands) {
		return zwave.securityV1.securityMessageEncapsulation().encapsulate(cmd).format()
	}
	else {
		return cmd.format()
	}	
}

// Parses incoming message
def parse(String description) {	
	def result = []
	def cmd = zwave.parse(description, commandClassVersions)
	if (cmd) {
		result += zwaveEvent(cmd)
	}
	else {
		logDebug "Unable to parse: $description"
	}	
	if (!isDuplicateCommand(state.lastCheckinTime, 60000)) {
		result << createLastCheckinEvent()
	}		
	return result
}

private createLastCheckinEvent() {
	logDebug "Device Checked In"
	state.lastCheckinTime = new Date().time
	return createEvent(name: "lastCheckin", value: convertToLocalTimeString(new Date()), displayed: false)
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

def zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityMessageEncapsulation cmd) {
	def result = []
	
	def encapCmd = cmd.encapsulatedCommand(commandClassVersions)
	if (encapCmd) {
		state.useSecureCommands = true
		result += zwaveEvent(encapCmd)
	}
	else {
		log.warn "Unable to extract encapsulated cmd from $cmd"	
	}
	return result
}

private getCommandClassVersions() {
	[
		0x20: 1,	// Basic
		0x25: 1,	// Switch Binary
		0x59: 1,  // AssociationGrpInfo
		0x5A: 1,  // DeviceResetLocally
		0x5E: 2,  // ZwaveplusInfo
		0x70: 1,  // Configuration
		0x71: 3,  // Notification (4)
		0x72: 1,  // ManufacturerSpecific (1,2)
		0x73: 1,  // Powerlevel
		0x7A: 2,	// Firmware Update
		0x80: 1,  // Battery
		0x85: 2,  // Association
		0x86: 1,	// Version (2)
		0x98: 1		// Security
	]
}

// Requested by health poll to verify that it's still online.
def zwaveEvent(physicalgraph.zwave.commands.versionv1.VersionReport cmd) {
	logTrace "VersionReport: $cmd"	
	// Using this event for health monitoring to update lastCheckin
	return []
}

def zwaveEvent(physicalgraph.zwave.commands.switchbinaryv1.SwitchBinaryReport cmd) {
	logTrace "BinaryReport: $cmd"
}

def zwaveEvent(physicalgraph.zwave.commands.configurationv1.ConfigurationReport cmd) {
	if (cmd?.parameterNumber == getAlarmTypeParamNumber()) {		
		def val = cmd.configurationValue[0]
		logDebug "Current Alarm Type: ${val}"
		return sendResponse(turnOn(val))
	}	
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd) {
	def switchValue = (cmd.value == 0) ? "off" : "on"
	def alarmValue = null
	
	if (cmd.value == 0) {
		if (device.currentValue("alarm") != "off") {
			logDebug "Alarm is off"
		}
		state.activeAlarm = null
		alarmValue = "off"
	}
	else {		
		alarmValue = getLastAlarmStateValue()			
	}	
	
	return getCommandEvents(alarmValue, switchValue, alarmValue)
}

private getCommandEvents(alarmValue, switchValue, statusValue) {
	def result = []
	if (device.currentValue("status") != statusValue) {
		result << createEvent(createStatusEventMap(statusValue))
	}
	if (device.currentValue("alarm") != alarmValue) {
		result << createEvent(
			name:"alarm",
			description: "Alarm is $alarmValue",
			value: alarmValue, 
			isStateChange: true, 
			displayed: false
		)
	}
	if (device.currentValue("switch") != switchValue) {
		result << createEvent(
			name:"switch", 
			description: "Switch is $switchValue",
			value: switchValue, 
			isStateChange: true, 
			displayed: false
		)
	}
	return result
}

private createStatusEventMap(statusValue) {
	return [
		name: "status",
		description: "Status is $statusValue",
		value: statusValue, 
		isStateChange: true, 
		displayed: true
	]
}

private getLastAlarmStateValue() {
	def result
	switch (state.activeAlarm?.alarmType) {
		case 0:
			result = "both"
			break
		case 1:
			result = "siren"
			break
		case 2:
			result = "strobe"
			break			
		default:
			result = "off"
	}		
	return result
}

// Creates the event for the battery level.
def zwaveEvent(physicalgraph.zwave.commands.batteryv1.BatteryReport cmd) {
	logTrace "BatteryReport: $cmd"
	def val = (cmd.batteryLevel == 0xFF ? 1 : cmd.batteryLevel)
	if (val > 100) {
		val = 100
	}
	if (val < 1) {
		val = 1
	}
	state.lastBatteryReport = new Date().time	
	logDebug "Battery ${val}%"
	
	def isNew = (device.currentValue("battery") != val)
			
	def result = []
	result << createEvent(name: "battery", value: val, unit: "%", display: isNew, isStateChange: isNew)	
	return result
}

// Writes unexpected commands to debug log
def zwaveEvent(physicalgraph.zwave.Command cmd) {
	logDebug "Unknown Command: $cmd"	
}

def speak(text) {
	playText(text)
}

// Unsuported Music Player commands
def unmute() { handleUnsupportedCommand("unmute") }
def nextTrack() { handleUnsupportedCommand("nextTrack") }
def setLevel(number) { handleUnsupportedCommand("setLevel") }
def previousTrack() { handleUnsupportedCommand("previousTrack") }


private handleUnsupportedCommand(cmd) {
	log.info "Command $cmd is not supported"
}

// Turns siren off
def pause() { off() }
def stop() { off() }
def mute() { off() }

// Turns siren on
def play() { both() }

// Audio Notification commands
def playTrackAtVolume(URI, volume) {
	logTrace "playTrackAtVolume($URI, $volume)"
	playTrack(URI, volume)
}

def playSoundAndTrack(URI, duration=null, track, volume=null) {
	logTrace "playSoundAndTrack($URI, $duration, $track, $volume)"
	playTrack(URI, volume)
}

def playTrackAndResume(URI, volume=null, otherVolume=null) {
	if (otherVolume) {
		// Fix for Speaker Notify w/ Sound not using command as documented.
		volume = otherVolume
	}
	playTrack(URI, volume)
}	
def playTrackAndRestore(URI, volume=null, otherVolume=null) {
	if (otherVolume) {
		// Fix for Speaker Notify w/ Sound not using command as documented.
		volume = otherVolume
	}
	playTrack(URI, volume)
}	

def playTrack(URI, volume=null) {
	logTrace "playTrack($URI, $volume)"
	playText(getTextFromTTSUrl(URI), volume)
}

def getTextFromTTSUrl(ttsUrl) {
	if (ttsUrl?.toString()?.contains("/")) {
		def startIndex = ttsUrl.lastIndexOf("/") + 1
		ttsUrl = ttsUrl.substring(startIndex, ttsUrl.size())?.toLowerCase()?.replace(".mp3","")
	}
	return ttsUrl
}

def playTextAndResume(message, volume=null) {
	logTrace "playTextAndResume($message, $volume)"
	playText(message, volume) 
}

def playTextAndRestore(message, volume=null) {
	logTrace "playTextAndRestore($message, $volume)"
	playText(message, volume=null) 
}

def playText(message, volume=null) {
	logTrace "playText($message, $volume)"
	logDebug "Executing playText($message) Command"
	message = cleanMessage(message)
	def cmds
	switch (message) {
		case ["on", "play"]:
			cmds = both()
			break
		case ["stop", "off", "pause", "mute"]:
			cmds = off()
			break
		default:
			if (message) {
				cmds = parseComplexCommand(message)
			}
	}
	if (!cmds) {
		logDebug "'$message' is not a valid command."
	}
	else {
		return cmds
	}
}

def cleanMessage(message) {
	return message?.
		toLowerCase()?.
		replace(",", "_")?.
		replace(" ", "")?.
		replace("(", "")?.
		replace(")", "")
}

def parseComplexCommand(message) {	
	def cmds = []
	
	def args = getComplexCmdArgs(message)
	if (message.contains("beep")) {	
		if (!args || args?.size() == 0) {
			cmds += beep()
		}
		else if (args?.size() == 3) {
			cmds += customBeep(args[0], args[1], args[2])
		}
		else {			
			cmds += customBeep(args[0], 0, false)
		}		
	}	
	else if (message.contains("strobe")) {	
		cmds += (args?.size() == 2) ? customStrobe(args[0], args[1]) : strobe()
	}
	else if (message.contains("both")) {
		cmds += (args?.size() == 3) ? customBoth(args[0], args[1], args[2]) : both()
	}
	else if (message.contains("siren")) {
		cmds += (args?.size() == 3) ? customSiren(args[0], args[1], args[2]) : siren()
	}
	return cmds
}

private sendResponse(cmds) {
	def actions = []
	cmds?.each { cmd ->
		actions << new physicalgraph.device.HubAction(cmd)
	}	
	sendHubCommand(actions)
	return []
}

private getComplexCmdArgs(message) {
	def args = removeCmdPrefix(message).tokenize("_")
	if (args.every { node -> isNumeric(node) || node in ["true","false"]}) {
		return args
	}
	else {
		return null
	}	
}

private removeCmdPrefix(message) {
	for (prefix in ["custombeep","beep","customboth","both","customsiren","siren","customstrobe","strobe"]) {
		if (message.startsWith(prefix)) {
			return message.replace("${prefix}_", "").replace("$prefix", "")
		}		
	}
	return message
}

private getCheckinIntervalSettingMinutes() {
	return convertOptionSettingToInt(checkinIntervalOptions, checkinIntervalSetting) ?: 720
}
private getCheckinIntervalSetting() {
	return settings?.checkinInterval ?: findDefaultOptionName(checkinIntervalOptions)
}
private getBatteryReportingIntervalSettingMinutes() {
	return convertOptionSettingToInt(checkinIntervalOptions, batteryReportingIntervalSetting) ?: checkinIntervalSettingMinutes
}
private getBatteryReportingIntervalSetting() {
	return settings?.batteryReportingInterval ?: findDefaultOptionName(checkinIntervalOptions)
}

private getCheckinIntervalOptions() {
	[
		[name: "5 Minutes", value: 5],
		[name: "10 Minutes", value: 10],
		[name: "15 Minutes", value: 15],
		[name: "30 Minutes", value: 30],
		[name: "1 Hour", value: 60],
		[name: "2 Hours", value: 120],
		[name: "3 Hours", value: 180],
		[name: "6 Hours", value: 360],
		[name: "9 Hours", value: 540],
		[name: formatDefaultOptionName("12 Hours"), value: 720],
		[name: "18 Hours", value: 1080],
		[name: "24 Hours", value: 1440]
	]
}

private convertOptionSettingToInt(options, settingVal) {
	return safeToInt(options?.find { "${settingVal}" == it.name }?.value, 0)
}

private formatDefaultOptionName(val) {
	return "${val}${defaultOptionSuffix}"
}

private findDefaultOptionName(options) {
	def option = options?.find { it.name?.contains("${defaultOptionSuffix}") }
	return option?.name ?: ""
}

private getDefaultOptionSuffix() {
	return "   (Default)"
}

private getIsVisionMfr() {
	return zwaveInfo?.mfr == "0109"
}

private safeToInt(val, defaultVal=-1) {
	return "${val}"?.isInteger() ? "${val}".toInteger() : defaultVal
}

private isNumeric(val) {
	return val?.toString()?.isNumber()
}

private validateBoolean(val, defaulVal) {
	if (val == null) {
		defaultVal
	}
	else {
		(val == true || val == "true")
	}
}

private currentValue(attributeName) {
	def val = device.currentValue(attributeName)
	return val ? val : ""	
}

private int validateRange(val, defaultVal, minVal, maxVal, desc) {
	try {
		def result
		def errorType = null
		if (isNumeric(val)) {
			result = val.toInteger()
		}
		else {
			errorType = "invalid"
			result = defaultVal
		}
		
		if (result > maxVal) {
			errorType = "too high"
			result = maxVal
		} else if (result < minVal) {
			errorType = "too low"
			result = minVal
		} 

		if (errorType) {
			logDebug("$desc: $val is $errorType, using $result instead.")
		}
		return result
	}
	catch (e) {
		log.error "$desc: Using $defaultVal because $val validation generated error.  ($e)"
		return defaultVal
	}
}

private logDebug(msg) {
	if (settings?.debugOutput || settings?.debugOutput == null) {
		log.debug "$msg"
	}
}

private logTrace(msg) {
	// log.trace "$msg"
}