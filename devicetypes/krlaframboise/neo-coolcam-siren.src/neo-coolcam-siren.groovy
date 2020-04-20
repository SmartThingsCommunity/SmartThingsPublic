/**
 *  Neo Coolcam Siren v1.0
 *  (Models: NAS-AB02ZU)
 *
 *  Author: 
 *    Kevin LaFramboise (krlaframboise)
 *
 *	Documentation:  https://community.smartthings.com/t/release-neo-coolcam-siren/156537?u=krlaframboise
 *
 *
 *  Changelog:
 *
 *    1.0 (03/04/2019)
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
	definition (
		name: "Neo Coolcam Siren", 
		namespace: "krlaframboise", 
		author: "Kevin LaFramboise",
		ocfDeviceType: "x.com.st.d.siren",
		vid:"generic-siren"
	) {
		capability "Actuator"
		capability "Sensor"
		capability "Switch Level"
		capability "Alarm"
		capability "Switch"		
		capability "Battery"
		capability "Configuration"
		capability "Refresh"
		capability "Health Check"
				
		attribute "primaryStatus", "string"
		attribute "firmwareVersion", "string"		
		attribute "lastCheckIn", "string"
				
		fingerprint mfr:"0258", prod:"0003", model:"0088", deviceJoinName: "Neo Coolcam Siren" // US
		fingerprint mfr:"0258", prod:"0003", model:"1088", deviceJoinName: "Neo Coolcam Siren" // EU
	}

	simulator { }
		
	tiles(scale: 2) {
		multiAttributeTile(name:"primaryStatus", type: "generic", width: 6, height: 4){
			tileAttribute ("device.primaryStatus", key: "PRIMARY_CONTROL") {
				attributeState "off", label: 'OFF', icon: "st.alarm.alarm.alarm", backgroundColor: "#ffffff"
				attributeState "alarm", label: 'ALARM', action: "alarm.off", icon: "st.alarm.alarm.alarm", backgroundColor: "#00a0dc"
				attributeState "play", label: 'PLAYING', action: "alarm.off", icon: "st.alarm.beep.beep", backgroundColor: "#00a0dc"
			}
		}
		
		standardTile("sliderText", "generic", width: 2, height: 2) {
			state "default", label:'Play Sound #'
		}
		
		controlTile("slider", "device.level", "slider",	height: 2, width: 4) {
			state "level", action:"setLevel"
		}
		
		standardTile("off", "device.alarm", width: 2, height: 2) {
			state "default", label:'Off', action: "alarm.off"
		}
		
		standardTile("on", "device.switch", width: 2, height: 2) {
			state "default", label:'On', action: "switch.on"
		}
		
		standardTile("alarm", "device.alarm", width: 2, height: 2) {
			state "default", label:'Alarm', action: "alarm.both"
		}
		
		standardTile("refresh", "device.refresh", width: 2, height: 2) {
			state "default", label:'Refresh', action: "refresh.refresh", icon:"st.secondary.refresh-icon"
		}
		
		standardTile("configure", "device.configure", width: 2, height: 2) {
			state "default", label:'Sync', action: "configuration.configure", icon:"st.secondary.tools"
		}
		
		valueTile("battery", "device.battery", width: 2, height: 2) {
			state "default", label:'${currentValue}% Battery'
		}
		
		valueTile("firmwareVersion", "device.firmwareVersion", decoration:"flat", width:3, height: 1) {
			state "firmwareVersion", label:'Firmware ${currentValue}'
		}
		
		valueTile("syncStatus", "device.syncStatus", decoration:"flat", width:3, height: 1) {
			state "syncStatus", label:'${currentValue}'
		}
		
		main "primaryStatus"
		details(["primaryStatus", "sliderText", "slider", "off", "alarm", "on", "refresh", "configure", "battery", "firmwareVersion", "syncStatus"])
	}
		
	preferences {
		configParams.each {
			if (it.pref) {
				input "${it.pref}", "enum",
					title: "${it.name}:",
					required: false,
					defaultValue: "${it.value}",
					displayDuringSetup: true,
					options: it.options
			}
		}
				
		input "switchOnAction", "enum",
			title: "Switch On Action",
			defaultValue: "0",
			required: false,
			options: setDefaultOption(switchOnActionOptions, "0")

		input "debugOutput", "bool", 
			title: "Enable Debug Logging?", 
			defaultValue: true, 
			required: false
	}
}


def installed() {
	logDebug "installed()..."
	initialize()
	return response(refresh())
}

def updated() {	
	if (!isDuplicateCommand(state.lastUpdated, 3000)) {
		state.lastUpdated = new Date().time
		logDebug "updated()..."

		initialize()
		
		def cmds = configureCmds()		
		return cmds ? response(cmds) : []
	}	
}

private initialize() {
	runIn(2, updateSyncStatus)
	
	unschedule()
	runEvery3Hours(scheduledCheckIn)
}


def scheduledCheckIn() {	
	sendResponse(batteryGetCmd())
}

def ping() {
	logDebug "ping()..."
	return sendResponse([basicGetCmd()])
}

private sendResponse(cmds) {
	def hubCmds = []
	cmds.each {
		hubCmds << new physicalgraph.device.HubAction(it)
	}
	sendHubCommand(hubCmds, 100)
	return []
}


def configure() {
	logDebug "configure()..."
	
	state.syncAll = true
	return configureCmds()	
}

private configureCmds() {	
	runIn(5, updateSyncStatus)
			
	def cmds = []
	
	if (!device.currentValue("switch")) {
		sendEvent(getEventMap("switch", "off"))
		sendEvent(getEventMap("level", 0))
		sendEvent(getEventMap("alarm", "off"))
		sendEvent(getEventMap("primaryStatus", "off"))
	}
	
	if (!device.currentValue("battery")) {
		cmds << batteryGetCmd()
	}
	
	if (!device.currentValue("firmwareVersion")) {
		cmds << versionGetCmd()
	}
	
	if (!device.currentValue("checkInterval")) {
		// Scheduled check in runs every 3 hours, but only creates an event every 12 hours so allow that check in to be 3 hours and 5 minutes late before marking the device offline. 
		def eventMap = getEventMap("checkInterval", ((60 * 60 * 15) + (5 * 60)))
		eventMap.data = [protocol: "zwave", hubHardwareId: device.hub.hardwareID]		
		sendEvent(eventMap)
	}
	else {
		// Configure and Updated are both called during inclusion so prevent duplicateion configuration commands.
		configParams.each { 
			if (state.syncAll || "${getParamStoredValue(it.num)}" != "${it.value}") {
				logDebug "CHANGING ${it.name}(#${it.num}) from ${getParamStoredValue(it.num)} to ${it.value}"
				cmds << configSetCmd(it, it.value)
				cmds << configGetCmd(it)
			}
		}
	}
	state.syncAll = false
	return cmds ? delayBetween(cmds, 1500) : []
}


def on() {
	logDebug "on()..."
	if (settings?.switchOnAction == "on") {
		return both()
	}
	else {
		def sound = safeToInt(settings?.switchOnAction, 0)
		if (sound) {
			return playSound(sound)
		}	
		else {
			log.warn "Ignoring 'on' command because the Switch On Action setting is set to 'Do Nothing'"
		}
	}
}


def setLevel(level, duration=null) {	
	if ("${level}" != "0") {
		logDebug "setLevel(${level})..."	
		playSound(extractSoundFromLevel(level))
	}
}

private extractSoundFromLevel(level) {
	def sound = safeToInt(level, 1)
	if (sound < 10) {
		return sound
	}
	else {
		if ((sound % 10) != 0) {
			sound = (sound - (sound % 10))
		}
		return (sound / 10)
	}
}

private playSound(sound) {
	logTrace "playSound(${sound})"
	
	if (sound == 9) {
		//Sound #9 doesn't work...
		sound = 10 
	}
	
	def cmds = []
	def val = safeToInt(sound, 0)
	if (val) {
		if (device.currentValue("alarm") == "off") {
			runIn(2, clearStatus)
			sendEvent(getEventMap("primaryStatus", "play"))
			
			logDebug "Playing sound #${val}"
			cmds << indicatorSetCmd(0) // Prevents duplicate chimes
			cmds << indicatorSetCmd(val)
		}
		else {
			log.warn "Can't play sound #${val} because alarm is on"
		}		
	}
	else {
		log.warn "${sound} is not a valid sound number"
	}	
	return cmds ? delayBetween(cmds, 2000) : []
}

def clearStatus() {
	if (device.currentValue("alarm") == "off" && device.currentValue("primaryStatus") == "play") {
		sendEvent(getEventMap("primaryStatus", "off"))
	}
}


def siren() {
	return both()
}

def strobe() { 
	return both() 
}

def both() { 
	logDebug "both()..."
	
	state.pendingAlarm = true
	
	return [ switchBinarySetCmd(0xFF) ]
}


def off() {
	logDebug "off()..."	
	
	runIn(5, fixAlarmOffState)
	
	return [ switchBinarySetCmd(0) ]
}

def fixAlarmOffState() {
	if (device.currentValue("alarm") != "off") {
		state.pendingOff = true
		sendResponse([ switchBinaryGetCmd() ])
	}
}


def refresh() {
	logDebug "refresh()..."	

	updateSyncStatus()
	
	state.pendingRefresh = true

	return delayBetween([
		switchBinaryGetCmd(), 
		batteryGetCmd()
	], 2000)
}


private versionGetCmd() {
	return secureCmd(zwave.versionV1.versionGet())
}

private basicGetCmd() {
	return secureCmd(zwave.basicV1.basicGet())
}

private indicatorSetCmd(val) {
	return secureCmd(zwave.indicatorV1.indicatorSet(value: val))
}

private batteryGetCmd() {
	return secureCmd(zwave.batteryV1.batteryGet())
}

private switchBinaryGetCmd() {
	return secureCmd(zwave.switchBinaryV1.switchBinaryGet())
}

private switchBinarySetCmd(val) {
	return secureCmd(zwave.switchBinaryV1.switchBinarySet(switchValue: val))
}

private configSetCmd(param, value) {
	return secureCmd(zwave.configurationV1.configurationSet(parameterNumber: param.num, size: param.size, scaledConfigurationValue: value))
}

private configGetCmd(param) {
	return secureCmd(zwave.configurationV2.configurationGet(parameterNumber: param.num))
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
		0x25: 1,	// Switch Binary
		0x59: 1,	// AssociationGrpInfo
		0x5A: 1,	// DeviceResetLocally
		0x5E: 2,	// ZwaveplusInfo
		0x70: 2,	// Configuration
		0x71: 3,	// Notification (v4)
		0x72: 2,	// ManufacturerSpecific
		0x73: 1,	// Powerlevel
		0x80: 1,	// Battery
		0x85: 2,	// Association
		0x86: 1,	// Version (3)
		0x87: 1		// Indicator
	]
}


def parse(String description) {	
	def result = []
	try {
		
		if (!"${description}".contains("command: 5E02")) {
			def cmd = zwave.parse(description, commandClassVersions)
			if (cmd) {
				result += zwaveEvent(cmd)		
			}
			else {
				log.warn "Unable to parse: $description"
			}
		}
			
		if (!isDuplicateCommand(state.lastCheckInTime, 59000)) {
			state.lastCheckInTime = new Date().time
			sendEvent(getEventMap("lastCheckIn", convertToLocalTimeString(new Date())))
		}
	}
	catch (e) {
		log.error "${e}"
	}
	return result
}


def zwaveEvent(physicalgraph.zwave.commands.versionv1.VersionReport cmd) {
	logTrace "VersionReport: ${cmd}"
	
	def version = "${cmd.applicationVersion}.${cmd.applicationSubVersion}"
	
	if (version != device.currentValue("firmwareVersion")) {
		logDebug "Firmware: ${version}"
		sendEvent(name: "firmwareVersion", value: version, displayed:false)
	}
	return []	
}


def zwaveEvent(physicalgraph.zwave.commands.batteryv1.BatteryReport cmd) {
	// logTrace "BatteryReport: $cmd"
	
	def val = (cmd.batteryLevel == 0xFF ? 1 : cmd.batteryLevel)
	if (val > 100) {
		val = 100
	}
	else if (val < 1) {
		val = 1
	}
	
	logDebug "Battery is ${val}%"
	sendEvent(getEventMap("battery", val, true, "%"))
	return []
}


def zwaveEvent(physicalgraph.zwave.commands.switchbinaryv1.SwitchBinaryReport cmd) {
	logTrace "SwitchBinaryReport: ${cmd}"

	if (state.pendingRefresh || state.pendingOff) {
		state.pendingRefresh = false
		state.pendingOff = false
		sendAlarmEvents(cmd.value)
	}
	
	return []
}

def zwaveEvent(physicalgraph.zwave.commands.notificationv3.NotificationReport cmd) {
	logTrace "NotificationReport: $cmd"

	if (cmd.notificationType == 14) {
		sendAlarmEvents(cmd.event)
	}	
	return []
}

private sendAlarmEvents(value) {
		if (!value || state.pendingAlarm) {
			state.pendingAlarm = false
			def primaryStatus = value ? "alarm" : "off"
			def alarmVal = value ? "both" : "off"
			def switchVal = value ? "on" : "off"
			
			if (device.currentValue("alarm") != alarmVal) {
				logDebug "Alarm is ${alarmVal}"
				sendEvent(getEventMap("alarm", alarmVal, true))
			}
			
			sendEvent(getEventMap("primaryStatus", primaryStatus))
			
			if (device.currentValue("switch") != switchVal) {
				sendEvent(getEventMap("switch", switchVal))
			}
		}	
}


def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd, endPoint=0) {
	logTrace "BasicReport: ${cmd}"
	
	return []
}	


def zwaveEvent(physicalgraph.zwave.commands.configurationv2.ConfigurationReport cmd) {	
	// logTrace "ConfigurationReport: ${cmd}"
	
	updateSyncStatus("Syncing...")
	runIn(5, updateSyncStatus)
	
	def param = configParams.find { it.num == cmd.parameterNumber }
	if (param) {	
		def val = cmd.size == 1 ? cmd.configurationValue[0] : cmd.scaledConfigurationValue
		
		logDebug "${param.name}(#${param.num}) = ${val}"
		setParamStoredValue(param.num, val)	
	}
	else {
		logDebug "Unknown Parameter #${cmd.parameterNumber} = ${val}"
	}		
	return []
}


def updateSyncStatus(status=null) {	
	if (status == null) {	
		def changes = getPendingChanges()
		if (changes > 0) {
			status = "${changes} Pending Change" + ((changes > 1) ? "s" : "")
		}
		else {
			status = "Synced"
		}
	}	
	if ("${syncStatus}" != "${status}") {
		sendEvent(getEventMap("syncStatus", status))
	}
}

private getSyncStatus() {
	return device.currentValue("syncStatus")
}

private getPendingChanges() {
	return (configParams.count { isConfigParamSynced(it) ? 0 : 1 })
}

private isConfigParamSynced(param) {
	return (param.value == getParamStoredValue(param.num))
}

private getParamStoredValue(paramNum) {
	return safeToInt(state["configVal${paramNum}"], null)
}

private setParamStoredValue(paramNum, value) {
	state["configVal${paramNum}"] = value
}


def zwaveEvent(physicalgraph.zwave.Command cmd) {
	logDebug "Unhandled zwaveEvent: $cmd"
	return []
}


private getConfigParams() {
	return [
		alarmVolumeParam,
		alarmDurationParam,		
		alarmSoundParam,
		alarmLEDParam,
		chimeVolumeParam,
		chimeRepeatParam,
		chimeLEDParam,		
		// chimeSoundParam,
		modeParam
	]
}

private getAlarmVolumeParam() {
	return getParam(1, "Alarm Volume", 1, 3, volumeOptions, "alarmVolume")
}

private getAlarmDurationParam() {
	return getParam(2, "Alarm Duration", 1, 2, alarmDurationOptions, "alarmDuration")
}

private getChimeRepeatParam() {
	return getParam(3, "Chime Repeat", 1, 1, chimeRepeatOptions, "chimeRepeat")
}

private getChimeVolumeParam() {
	return getParam(4, "Chime Volume", 1, 1, volumeOptions, "chimeVolume")	
}

private getAlarmSoundParam() {
	return getParam(5, "Alarm Sound", 1, 10, soundOptions, "alarmSound")
}

private getChimeSoundParam() {
	return getParam(6, "Chime Sound", 1, 9, soundOptions)
}

private getModeParam() {
	return getParam(7, "Alarm/Chime Mode", 1, 1)
}

private getAlarmLEDParam() {
	return getParam(8, "Alarm LED Enabled", 1, 1, enabledDisabledOptions, "alarmLED")
}

private getChimeLEDParam() {
	return getParam(9, "Chime LED Enabled", 1, 1, enabledDisabledOptions, "chimeLED")
}

private getParam(num, name, size, defaultVal, options=null, pref=null) {
	def val = safeToInt(((settings && pref != null) ? settings["${pref}"] : null), defaultVal) 
	
	def map = [num: num, name: name, size: size, value: val, pref: pref]
	if (options) {
		map.valueName = options?.find { k, v -> "${k}" == "${val}" }?.value
		map.options = setDefaultOption(options, defaultVal)
	}
	
	return map
}

private setDefaultOption(options, defaultVal) {
	return options?.collect { k, v ->
		if ("${k}" == "${defaultVal}") {
			v = "${v} [DEFAULT]"		
		}
		["$k": "$v"]
	}
}


private getVolumeOptions() {
	[
		1:"Low",
		2:"Medium",
		3:"High"
	]
}

private getAlarmDurationOptions() {
	[
		0:"Disabled",
		1:"30 Seconds",
		2:"1 Minute",
		3:"5 Minutes",
		255:"Unlimited"
	]
}

private getSoundOptions() {
	def options = [:]
	(1..8).each {
		options["${it}"] = "Sound #${it}"
	}
	options["10"] = "Sound #9"
	options["9"] = "Strobe"
	return options
}

private getChimeRepeatOptions() {
	def options = [:]
	(1..25).each {
		options["${it}"] = "${it}"
	}
	return options
}

private getEnabledDisabledOptions() {
	[
		0:"Disabled",
		1:"Enabled"
	]
}

private getSwitchOnActionOptions() {
	def options = [
		"0":"Do Nothing",
		"on": "Turn On Siren"	
	]
	
	(1..9).each {
		options["${it}"] = "Play Sound #${it}"
	}	
	return options
}


private getEventMap(name, value, displayed=false, unit=null) {	
	def eventMap = [
		name: name,
		value: value,
		displayed: displayed,
		// isStateChange: true,
		descriptionText: "${device.displayName} - ${name} is ${value}"
	]
	
	if (unit) {
		eventMap.unit = unit
		eventMap.descriptionText = "${eventMap.descriptionText} ${unit}"
	}	
	return eventMap
}


private safeToInt(val, defaultVal=0) {
	return "${val}"?.isInteger() ? "${val}".toInteger() : defaultVal
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