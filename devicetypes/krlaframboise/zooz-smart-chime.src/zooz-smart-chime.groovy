/**
 *  Zooz Smart Chime v1.2.1
 *  (Model: ZSE33)
 *
 *  Author: 
 *    Kevin LaFramboise (krlaframboise)
 *
 *  URL to documentation:  https://community.smartthings.com/t/release-zooz-smart-chime/77152?u=krlaframboise
 *    
 *
 *  Changelog:
 *
 *    1.2.1 (04/23/2017)
 *    	- SmartThings broke parse method response handling so switched to sendhubaction.
 *    	- Bug fix for location timezone issue.
 *
 *    1.2 (04/14/2017)
 *      - Added Switch Level capability
 *
 *    1.1 (02/19/2017)
 *      - Added Health Check and self polling.
 *
 *    1.0 (01/16/2017)
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
	definition (
		name: "Zooz Smart Chime", 
		namespace: "krlaframboise", 
		author: "Kevin LaFramboise"
	) {
		capability "Actuator"
		capability "Alarm"
		capability "Battery"
		capability "Configuration"
		capability "Refresh"
		capability "Switch"
		capability "Tone"
		capability "Health Check"
		capability "Polling"
		capability "Switch Level"
		
		attribute "lastCheckin", "string"
		
		attribute "status", "enum", ["alarm", "beep", "off", "on", "custom"]
		
		command "customChime"
		
		fingerprint deviceId: "0x1005", inClusters: "0x25, 0x59, 0x5A, 0x5E, 0x70, 0x71, 0x72, 0x73, 0x80, 0x85, 0x86, 0x87"
		
		fingerprint mfr:"027A", prod:"0003", model:"0088"
	}
	
	simulator { }
	
	preferences {
		input "sirenSound", "number",
			title: "Siren Sound [1-10]:",
			range: "1..10",
			displayDuringSetup: true,
			defaultValue: sirenSoundSetting
		input "sirenVolume", "number",
			title: "Siren Volume [1-3]:${getNameValueSettingDesc(volumeOptions)}",
			range: "1..3",
			required: false,
			defaultValue: sirenVolumeSetting,			
			displayDuringSetup: true
		input "sirenLength", "number",
			title: "Siren Length [0-4]:${getNameValueSettingDesc(sirenLengthOptions)}",
			range: "0..4",
			defaultValue: sirenLengthSetting,
			required: false,
			displayDuringSetup: true
		input "sirenLED", "number",
			title: "Siren LED [0-1]:${getNameValueSettingDesc(ledOptions)}",
			range: "0..1",
			defaultValue: sirenLEDSetting,
			required: false,
			displayDuringSetup: true		
		input "onChimeSound", "number",
			title: "Switch On Chime Sound [1-10]:",
			range: "1..10",
			required: false,
			displayDuringSetup: true,
			defaultValue: onChimeSoundSetting
		input "beepChimeSound", "number",
			title: "Beep Chime Sound [1-10]:",
			range: "1..10",
			required: false,
			displayDuringSetup: true,
			defaultValue: beepChimeSoundSetting
		input "chimeVolume", "number",
			title: "Chime Volume [1-3]:${getNameValueSettingDesc(volumeOptions)}",
			range: "1..3",
			required: false,
			defaultValue: chimeVolumeSetting,
			displayDuringSetup: true
		input "chimeRepeat", "number",
			title: "Chime Repeat [1-255]:\n(1-254 = # of Cycles)\n(255 = ${noLengthMsg})",
			range: "1..255",
			required: false,
			displayDuringSetup: true,
			defaultValue: chimeRepeatSetting
		input "chimeLED", "number",
			title: "Chime LED [0-1]:${getNameValueSettingDesc(ledOptions)}",
			range: "0..1",
			defaultValue: chimeLEDSetting,
			required: false,
			displayDuringSetup: true
		input "checkinInterval", "enum",
			title: "Checkin Interval:",
			defaultValue: checkinIntervalSetting,
			required: false,
			displayDuringSetup: true,
			options: checkinIntervalOptions.collect { it.name }
		input "debugOutput", "bool", 
			title: "Enable debug logging?", 
			defaultValue: true, 
			required: false
	}

	tiles(scale: 2) {
		multiAttributeTile(name:"status", type: "generic", width: 6, height: 4, canChangeIcon: true){
			tileAttribute ("device.status", key: "PRIMARY_CONTROL") {
				attributeState "off", 
					label:'Off', 
					action: "off", 
					icon: "st.Entertainment.entertainment2",
					backgroundColor:"#ffffff"
				attributeState "on", 
					label:'Chime (On)!', 
					action: "off", 
					icon:"st.Entertainment.entertainment2", 					
					backgroundColor: "#99c2ff"					
				attributeState "alarm", 
					label:'Siren!', 
					action: "off", 
					icon:"st.alarm.alarm.alarm", 
					backgroundColor:"#ff9999"
				attributeState "beep", 
					label:'Chime (Beep)!', 
					action: "off", 
					icon:"st.Entertainment.entertainment2", 
					backgroundColor:"#99ff99"
				attributeState "custom", 
					label:'Chime (Custom)!', 
					action: "off", 
					icon:"st.Entertainment.entertainment2", 
					backgroundColor:"#cc99cc"				
			}
		}
		
		standardTile("playAlarm", "device.alarm", width: 2, height: 2) {
			state "default", 
				label:'Alarm', 
				action:"alarm.both", 
				icon:"st.security.alarm.clear", 
				backgroundColor:"#ff9999"
			state "both",
				label:'Turn Off',
				action:"alarm.off",
				icon:"st.alarm.alarm.alarm", 
				background: "#ffffff"	
		}
				
		standardTile("playOn", "device.switch", width: 2, height: 2) {
			state "default", 
				label:'Turn On', 
				action:"switch.on", 
				icon:"st.Entertainment.entertainment2", 
				backgroundColor:"#99c2ff"
			state "on",
				label:'Turn Off',
				action:"switch.off",
				icon:"st.Entertainment.entertainment2", 
				background: "#ffffff"	
		}
		
		standardTile("playBeep", "device.status", width: 2, height: 2) {
			state "default", 
				label:'Beep', 
				action:"tone.beep", 
				icon:"st.Entertainment.entertainment2", 
				backgroundColor: "#99FF99"
			state "beep",
				label:'Stop',
				action:"off",
				icon:"st.Entertainment.entertainment2", 
				background: "#ffffff"	
		}
		
		standardTile("turnOff", "device.off", width: 2, height: 2) {
			state "default", 
				label:'Off', 
				action:"switch.off", 
				backgroundColor: "#ffffff"			
		}
		
		standardTile("refresh", "device.refresh", width: 2, height: 2) {
			state "refresh", label:'Refresh', action: "refresh", icon:"st.secondary.refresh-icon"
		}
		
		valueTile("battery", "device.battery", decoration: "flat", width: 2, height: 2){
			state "battery", label:'${currentValue}% battery', unit:""
		}		
				
		main "status"
		details(["status", "playOn", "playBeep", "playAlarm", "turnOff", "refresh", "battery"])
	}
}

def updated() {	
	// This method always gets called twice when preferences are saved.
	if (!isDuplicateCommand(state.lastUpdated, 3000)) {		
		state.lastUpdated = new Date().time
		state.activeEvents = []
		logTrace "updated()"
		
		initializeCheckin()
		
		if (state.firstUpdate == false) {
			def result = []
			result += configure()
			if (result) {
				return sendResponse(result)
			}
		}
		else {
			// Skip first time updating because it calls the configure method while it's already running.
			state.firstUpdate = false
		}
	}	
}

private sendResponse(cmds) {
	def actions = []
	cmds?.each { cmd ->
		actions << new physicalgraph.device.HubAction(cmd)
	}	
	sendHubCommand(actions)
	return []
}

private initializeCheckin() {
// Set the Health Check interval so that it pings the device if it's 1 minute past the scheduled checkin.
	def checkInterval = ((checkinIntervalSettingMinutes * 60) + 60)
	
	sendEvent(name: "checkInterval", value: checkInterval, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])
	
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

def healthPoll() {
	logTrace "healthPoll()"
	sendHubCommand([new physicalgraph.device.HubAction(batteryGetCmd())], 100)
}

def ping() {
	logTrace "ping()"
	if (canCheckin()) {
		logDebug "Attempting to ping device."
		// Restart the polling schedule in case that's the reason why it's gone too long without checking in.
		initializeCheckin()
		
		return poll()	
	}	
}

def poll() {
	if (canCheckin()) {
		logTrace "Polling Device"
		return batteryGetCmd()
	}
	else {
		logTrace "Skipped Poll"
	}
}

def configure() {
	logTrace "configure()"
	def cmds = []
	def refreshAll = (!state.isConfigured || state.pendingRefresh || !settings?.sirenSound)
	
	if (!state.isConfigured) {
		logTrace "Waiting 1 second because this is the first time being configured"		
		cmds << "delay 1000"			
	}
	
	configData.each { 
		cmds += updateConfigVal(it.paramNum, it.value, refreshAll)	
	}
	
	if (refreshAll) {
		cmds << switchBinaryGetCmd()
		cmds << batteryGetCmd()
	}
		
	if (cmds) {
		logDebug "Sending configuration to device."
		return delayBetween(cmds, 250)
	}
	else {
		return cmds
	}	
}

private updateConfigVal(paramNum, val, refreshAll) {
	def result = []
	def configVal = state["configVal${paramNum}"]
	
	if (refreshAll || (configVal != val)) {
		result << configSetCmd(paramNum, val)
		result << configGetCmd(paramNum)
	}
	
	return result
}

def off() {
	logDebug "Turning Off()"
	return sirenToggleCmds(0x00)	
}

def on() {	
	logDebug "Playing On Chime (#${onChimeSoundSetting})"	
	addPendingSound("switch", "on")
	return chimePlayCmds(onChimeSoundSetting)
}

def setLevel(level, rate=null) {
	logTrace "Executing setLevel($level)"
	if (!device.currentValue("level")) {
		sendEvent(name:"level", value:0, displayed:false)
	}
	return customChime(extractSoundFromLevel(level))	
}

private extractSoundFromLevel(level) {
	def sound = safeToInt(level, 1)
	if (sound <= 10) {
		return 1
	}
	else {
		if ((sound % 10) != 0) {
			sound = (sound - (sound % 10))
		}
		return (sound / 10)
	}
}

def beep() {
	logDebug "Playing Beep Chime (#${beepChimeSoundSetting})"	
	addPendingSound("status", "beep")
	return chimePlayCmds(beepChimeSoundSetting)
}

def customChime(sound) {	
	def val = validateSound(sound, beepChimeSoundSetting)
	if ("${sound}" != "${val}") {
		logDebug "Playing Custom Chime (#${val}) - (${sound} is not a valid sound number)"
	}
	else {
		logDebug "Playing Custom Chime (#${val})"	
	}	
	addPendingSound("status", "custom")	
	return chimePlayCmds(val)
}

def siren() { return both() }
def strobe() { return both() }
def both() {
	logDebug "Playing Siren (#${sirenSoundSetting})"
	addPendingSound("alarm", "both")

	if (sirenLengthSetting == 0) {
		// Siren Length is set to chime.
		return chimePlayCmds(sirenSoundSetting)
	}
	else {
		def result = []		
		result += sirenToggleCmds(0xFF)
		return result
	}
}

private addPendingSound(name, value) {
	state.pendingSound = [name: "$name", value: "$value", time: new Date().time]
}

def refresh() {	
	logTrace "refresh()"
	
	state.pendingRefresh = true
	return configure()
}

	
def parse(String description) {
	def result = []
	
	if (description.startsWith("Err")) {
		log.warn "Parse Error: $description"
		result << createEvent(descriptionText: "$device.displayName $description", isStateChange: true)
	}
	else {
		def cmd = zwave.parse(description, getCommandClassVersions())
		if (cmd) {
			result += zwaveEvent(cmd)
		}
		else {
			logDebug "Unable to parse description: $description"
		}
	}	
	if (canCheckin()) {
		result << createLastCheckinEvent()
	}	
	return result
}

private getCommandClassVersions() {
	[
		0x59: 1,  // AssociationGrpInfo
		0x5A: 1,  // DeviceResetLocally
		0x5E: 2,  // ZwaveplusInfo
		0x70: 1,  // Configuration
		0x71: 3,  // Notification v4
		0x72: 2,  // ManufacturerSpecific
		0x73: 1,  // Powerlevel
		0x80: 1,  // Battery
		0x85: 2,  // Association
		0x86: 1,  // Version (2)
		0x87: 1,  // Indicator
		0x25: 1,  // Switch Binary
	]
}

private canCheckin() {
	def minimumCheckinInterval = ((checkinIntervalSettingMinutes * 60 * 1000) - 5000)
	return (!state.lastCheckinTime || ((new Date().time - state.lastCheckinTime) >= minimumCheckinInterval))
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

def zwaveEvent(physicalgraph.zwave.commands.batteryv1.BatteryReport cmd) {
	logTrace "BatteryReport: $cmd"
	def isNew = (device.currentValue("battery") != cmd.batteryLevel)
	
	def val = (cmd.batteryLevel == 0xFF ? 1 : cmd.batteryLevel)
		
	def result = []
	result << createEvent(name: "battery", value: val, unit: "%", displayed: isNew, isStateChange: true)
	return result
}

def zwaveEvent(physicalgraph.zwave.commands.configurationv1.ConfigurationReport cmd) {	
	def name = configData.find { it.paramNum == cmd.parameterNumber }?.name
	if (name) {	
		def val = cmd.configurationValue[0]
	
		logDebug "${name} = ${val}"
	
		state."configVal${cmd.parameterNumber}" = val
	}
	else {
		logDebug "Parameter ${cmd.parameterNumber}: ${cmd.configurationValue}"
	}
	state.isConfigured = true
	state.pendingRefresh = false	
	return []
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd) {
	def result = []
	logTrace "BasicReport: ${cmd}"
	if (cmd.value == 0x00) {
		result += handleDeviceOff()
	}
	else {
		result += sendResponse(["delay 3000", basicGetCmd()])
	}	
	return result
}

def zwaveEvent(physicalgraph.zwave.commands.switchbinaryv1.SwitchBinaryReport cmd) {
	def result = []
	logTrace "SwitchBinaryReport: ${cmd}"	
	return result
}

def zwaveEvent(physicalgraph.zwave.commands.notificationv3.NotificationReport cmd) {
	def result = []	
	logTrace "NotificationReport: ${cmd}"
	
	if (cmd.notificationType == 14) {
		if (cmd.event == 0) {
			result += handleDeviceOff()
		}
		else if (cmd.event == 1) {
			result += handleDeviceOn(state.activeSound, state.pendingSound)
		}
	}
	return result
}

private handleDeviceOn(activeSound, pendingSound) {
	def result = []
	def activeSoundName = activeSound?.name
	
	state.activeSound = pendingSound
	
	if (pendingSound) {
		result << createEvent(getEventMap(pendingSound, true))
		
		def statusVal = ""
		if (pendingSound.name == "alarm") {
			statusVal = "alarm"
		}
		else if (pendingSound.name == "switch") {
			statusVal = "on"
		}
		if (statusVal) {
			result << createEvent(getStatusEventMap(statusVal))
		}
	}
	else {
		logTrace "Unable to create event on because the pending sound has not been set."
	}		
	return result
}

private handleDeviceOff() {
	def result = []
	["alarm", "switch", "status"].each { n ->			
		def displayed = false
		if ("${n}" == "${state.activeSound?.name}") {
			// Only the active event was initially displayed so it's the only off event that gets displayed.
			displayed = true
			state.activeSound = null
		}
		
		result << createEvent(getEventMap([name: "$n", value: "off"], displayed))
	}

	return result
}

private getStatusEventMap(val) {
	return getEventMap([name: "status", value: val], false)
}

private getEventMap(event, displayed=false) {	
	def isStateChange = (device.currentValue(event.name) != event.value)
	def eventMap = [
		name: event.name,
		value: event.value,
		displayed: displayed,
		isStateChange: true
	]
	logTrace "Creating Event: ${eventMap}"
	return eventMap
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
	logDebug "Unhandled Command: $cmd"
	return []
}

private chimePlayCmds(sound) {
	def cmds = []
	
	cmds << indicatorSetCmd(sound)
		
	if ((sound in [1, 9, 10]) && chimeRepeatSetting == 1) {
		// Fixes problem where these sounds stop playing before the start events are created causing the off events to never get created.
		cmds << basicGetCmd()
		cmds << "delay 3000"		
	}	
	return cmds	
}

private indicatorSetCmd(val) {
	return zwave.indicatorV1.indicatorSet(value: val).format()
}

private sirenToggleCmds(val) {
	return [
		switchBinarySetCmd(val),
		switchBinaryGetCmd()
	]
}
private switchBinaryGetCmd() {
	return zwave.switchBinaryV1.switchBinaryGet().format()
}
private switchBinarySetCmd(val) {
	return zwave.switchBinaryV1.switchBinarySet(switchValue: val).format()
}

private basicGetCmd() {
	return zwave.basicV1.basicGet().format()
}

private batteryGetCmd() {
	return zwave.batteryV1.batteryGet().format()
}

private configGetCmd(paramNum) {
	return zwave.configurationV1.configurationGet(parameterNumber: paramNum).format()
}

private configSetCmd(paramNum, val) {		
	return zwave.configurationV1.configurationSet(parameterNumber: paramNum, size: 1, scaledConfigurationValue: val).format()
}

// Configuration Parameters
private getConfigData() {
	// [paramNum: 6, name: "Chime Sound"]
	return [		
		[paramNum: 5, name: "Siren Sound", value: sirenSoundSetting],
		[paramNum: 1, name: "Siren Volume", value: sirenVolumeSetting],
		[paramNum: 2, name: "Siren Length", value: (sirenLengthSetting == 4 ? 255 : sirenLengthSetting)],
		[paramNum: 8, name: "Siren LED", value: sirenLEDSetting],
		[paramNum: 4, name: "Chime Volume", value: chimeVolumeSetting],
		[paramNum: 3, name: "Chime Repeat", value: chimeRepeatSetting],
		[paramNum: 9, name: "Chime LED", value: chimeLEDSetting],
		[paramNum: 7, name: "Chime Mode", value: chimeModeSetting]
	]	
}

// Settings
private getDebugOutputSetting() {
	return (settings?.debugOutput != false)
}
private getSirenSoundSetting() {
	return validateSound(settings?.sirenSound, 9)
}
private getSirenVolumeSetting() {
	return safeToInt(settings?.sirenVolume, 3)
}
private getSirenLengthSetting() {
	return safeToInt(settings?.sirenLength, 0)
}
private getSirenLEDSetting() {
	return safeToInt(settings?.sirenLED, 1)
}
private getOnChimeSoundSetting() {
	return validateSound(settings?.onChimeSound, 1)
}
private getBeepChimeSoundSetting() {
	 return validateSound(settings?.beepChimeSound, 3)
}
private getChimeVolumeSetting() {
	return safeToInt(settings?.chimeVolume, 3)
}
private getChimeRepeatSetting() {
	return safeToInt(settings?.chimeRepeat, 1)
}
private getChimeLEDSetting() {
	return safeToInt(settings?.chimeLED, 0)
}
private getChimeModeSetting() {
	return 1 // Chime Mode should always be disabled.
}

private getCheckinIntervalSettingMinutes() {
	return convertOptionSettingToInt(checkinIntervalOptions, checkinIntervalSetting)
}

private getCheckinIntervalSetting() {
	return settings?.checkinInterval ?: findDefaultOptionName(checkinIntervalOptions)
}

private validateSound(sound, defaultVal) {
	def val = safeToInt(sound, defaultVal)
	if (val > 10) {
		val = 10
	}
	else if (val < 1) {
		val = 1
	}
	return val
}

private getVolumeOptions() { 
	[
		[name: "Low", value: 1],
		[name: "Medium", value: 2],
		[name: "High", value: 3]
	]
}

private getLedOptions() { 
	[
		[name: "Off", value: 0],
		[name: "On", value: 1]
	]
}

private getSirenLengthOptions() {
	[
		[name: "Chime", value: 0],
		[name: "30 Seconds", value: 1],
		[name: "1 Minute", value: 2],
		[name: "5 Minutes", value: 3],
		[name: "${noLengthMsg}", value: 4] // config value is 255
	]
}

private getNoLengthMsg() { 
	return "Play until battery is depleted" 
}

private getNameValueSettingDesc(nameValueMap) {
	def desc = ""
	nameValueMap?.sort { it.value }.each { 
		desc = "${desc}\n(${it.value} - ${it.name})"
	}
	return desc
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

private safeToInt(val, defaultVal=-1) {
	return "${val}"?.isInteger() ? "${val}".toInteger() : defaultVal
}

private isDuplicateCommand(lastExecuted, allowedMil) {
	!lastExecuted ? false : (lastExecuted + allowedMil > new Date().time) 
}

private logDebug(msg) {
	if (debugOutputSetting) {
		log.debug "$msg"
	}
}

private logTrace(msg) {
	// log.trace "$msg"
}