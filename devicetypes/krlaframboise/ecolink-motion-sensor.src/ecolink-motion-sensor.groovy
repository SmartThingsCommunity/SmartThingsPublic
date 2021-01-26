/**
 *  Ecolink Motion Sensor v1.0
 *    (Model: PIRZWAVE2.5-ECO)
 *
 *  Author: 
 *    Kevin LaFramboise (krlaframboise)
 *
 *  URL to documentation:
 *   
 *
 *  Changelog:
 *
 *    1.0.1 (12/25/2017)
 *      - Implemented ST new color scheme.
 *
 *    1.0 (06/10/2017)
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
	definition (name:"Ecolink Motion Sensor", namespace:"krlaframboise", author: "Kevin LaFramboise") {
		capability "Sensor"
		capability "Battery"
		capability "Motion Sensor"
		capability "Tamper Alert"
		capability "Refresh"
		capability "Configuration"
		capability "Health Check"

		attribute "lastCheckin", "string"
		
		fingerprint mfr:"014A", prod:"0004", model:"0001"
		fingerprint deviceId:"0x0701", inClusters:"0x20,0x30,0x59,0x5E,0x70,0x71,0x72,0x73,0x80,0x84,0x85,0x86"
	}

	preferences {
		input "checkinInterval", "enum",
			title: "Checkin Interval:",
			defaultValue: checkinIntervalSetting,
			required: false,
			displayDuringSetup: true,
			options: checkinIntervalOptions.collect { it.name }
		input "reportBatteryEvery", "enum",
			title: "Battery Reporting Interval:",
			defaultValue: batteryReportingIntervalSetting,
			required: false,
			displayDuringSetup: true,
			options: checkinIntervalOptions.collect { it.name }
		input "debugOutput", "bool", 
			title: "Enable debug logging?", 
			defaultValue: false, 
			displayDuringSetup: true, 
			required: false
	}

	tiles(scale: 2) {
		multiAttributeTile(name:"mainTile", type: "generic", width: 6, height: 4){
			tileAttribute ("device.motion", key: "PRIMARY_CONTROL") {
				attributeState("active", label:'motion', icon:"st.motion.motion.active", backgroundColor:"#00a0dc")
				attributeState("inactive", label:'no motion', icon:"st.motion.motion.inactive", backgroundColor:"#cccccc")
			}
		}
		valueTile("battery", "device.battery", decoration: "flat", width: 2, height: 2) {
			state "battery", label:'${currentValue}% battery', unit:"%"
		}
		valueTile("tampering", "device.tamper", width: 2, height: 2) {
			state "detected", label:"Tamper", backgroundColor: "#e86d13"
			state "clear", label:"", backgroundColor: "#ffffff"
		}
		standardTile("refresh", "command.refresh", width: 2, height: 2) {
			state "default", label:"Refresh", action: "refresh", icon:"st.secondary.refresh-icon"
		}
		main("mainTile")
		details(["mainTile", "tampering", "battery", "refresh"])
	}
}

def updated() {	
	if (!isDuplicateCommand(state.lastUpdated, 3000)) {
		state.lastUpdated = new Date().time
		logTrace "updated()"
		state.pendingConfig = true
	}
}

def configure() {	
	logTrace "configure()"
	def cmds = []
	
	if (!state.isConfigured) {
		logTrace "Waiting 1 second because this is the first time being configured"
		// Give inclusion time to finish.
		cmds << "delay 1000"			
	}
	
	if (state.refreshAll || state.checkinIntervalSeconds != checkinIntervalSettingSeconds) {
		cmds << wakeUpIntervalSetCmd(checkinIntervalSettingSeconds)
		cmds << wakeUpIntervalGetCmd()
	}
	
	// Disable SensorBinary Reports and BasicSets
	[1:0x00, 2:0xFF].each { 	
		if (state.refreshAll || state["configVal${it.key}"] != it.value) {
			cmds += [
				configSetCmd(it.key, it.value),
				configGetCmd(it.key)
			]
		}
	}
	
	if (state.refreshAll || canReportBattery()) {
		cmds << batteryGetCmd()
	}
	return delayBetween(cmds, 1000)
}

// Required for HealthCheck Capability, but doesn't actually do anything because this device sleeps.
def ping() {
	logDebug "ping()"	
}

def refresh() {	
	if (device.currentValue("tamper") != "clear") {
		logDebug "Resetting Tamper"
		sendEvent(createTamperEventMap("clear"))
	}
	else {
		logDebug "The wakeup interval will be sent to the device the next time it wakes up."
		state.refreshAll = true
	}
	return []
}

private configGetCmd(num) {
	return zwave.configurationV2.configurationGet(parameterNumber: num).format()
}

private configSetCmd(num, val) {
	return zwave.configurationV2.configurationSet(scaledConfigurationValue: val, parameterNumber: num, size: 1).format()	
}

private wakeUpIntervalSetCmd(seconds) {
	return zwave.wakeUpV2.wakeUpIntervalSet(seconds:seconds, nodeid:zwaveHubNodeId).format()
}

private wakeUpIntervalGetCmd() {
	return zwave.wakeUpV2.wakeUpIntervalGet().format()
}

private wakeUpNoMoreInfoCmd() {
	return zwave.wakeUpV2.wakeUpNoMoreInformation().format()
}

private batteryGetCmd() {
	return zwave.batteryV1.batteryGet().format()
}

private getCommandClassVersions() {
	[
		0x20: 1,  // Basic
		0x30: 2,	// Sensor Binary
		0x59: 1,  // AssociationGrpInfo
		0x5E: 2,  // ZwaveplusInfo
		0x70: 2,  // Configuration
		0x71: 3,  // Alarm v1 or Notification v4
		0x72: 2,  // ManufacturerSpecific
		0x73: 1,  // Powerlevel
		0x80: 1,  // Battery
		0x84: 2,  // WakeUp
		0x85: 2,  // Association
		0x86: 1,	// Version (2)
	]
}

def parse(String description) {	
	def result = []
	
	result += createLastCheckinEvent()
	
	def cmd = zwave.parse(description, commandClassVersions)
	if (cmd) {
		result += zwaveEvent(cmd)
	}
	else {
		logDebug "Unknown Description: $desc"
	}

	return result
}

private createLastCheckinEvent() {
	def result = []
	if (!isDuplicateCommand(state.lastCheckin, 60000)) {
		state.lastCheckin = new Date().time
		result << createEvent(name: "lastCheckin", value: convertToLocalTimeString(new Date()), displayed: false, isStateChange: true)
	}
	return result
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

def zwaveEvent(physicalgraph.zwave.commands.wakeupv2.WakeUpIntervalReport cmd) {
	logTrace "WakeUpIntervalReport: $cmd"
	def result = []
	
	state.checkinIntervalSeconds = cmd.seconds
	
	// Set the Health Check interval so that it can be skipped twice plus 5 minutes.
	def checkInterval = ((cmd.seconds * 3) + (5 * 60))
	
	result << createEvent(name: "checkInterval", value: checkInterval, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])
	
	return result
}

def zwaveEvent(physicalgraph.zwave.commands.wakeupv2.WakeUpNotification cmd)
{
	logTrace "WakeUpNotification"

	def cmds = []
	if (state.pendingConfig || state.refreshAll) {		
		cmds += configure()
	}
	else if (canReportBattery()) {
		cmds << batteryGetCmd()
	}
	if (cmds) {
		cmds << "delay 2000"
	}
	cmds << wakeUpNoMoreInfoCmd()
	return response(cmds)	
}

private canReportBattery() {
	def reportEveryMS = (batteryReportingIntervalSettingMinutes * 60 * 1000)
		
	return (!state.lastBatteryReport || ((new Date().time) - state.lastBatteryReport > reportEveryMS)) 
}

def zwaveEvent(physicalgraph.zwave.commands.configurationv2.ConfigurationReport cmd) {
	logTrace "ConfigurationReport: ${cmd}"
	state.isConfigured = true
	state.refreshAll = false
	state.pendingConfig = false
	state["configVal${cmd.parameterNumber}"] = cmd.scaledConfigurationValue		
	return []
}

def zwaveEvent(physicalgraph.zwave.commands.batteryv1.BatteryReport cmd) {
	def val = (cmd.batteryLevel == 0xFF ? 1 : cmd.batteryLevel)
	
	if (val > 100) {
		val = 100
	}
	
	def result = []
	result << createEvent(createBatteryEventMap(val))
	return result
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicSet cmd) {	
	logTrace "BasicSet: ${cmd}"
	return []
}

def zwaveEvent(physicalgraph.zwave.commands.sensorbinaryv2.SensorBinaryReport cmd) {
	logTrace "SensorBinaryReport: ${cmd}"	
	return []
}

def zwaveEvent(physicalgraph.zwave.commands.notificationv3.NotificationReport cmd) {
	logTrace "NotificationReport: $cmd"
	def map
	if (cmd.notificationType == 7) {
		switch (cmd.event) {
			case 3:
				map = createTamperEventMap("detected")
				break
			case 8:
				map = createMotionEventMap("active")				
				break
			case 0:
				map = createMotionEventMap("inactive")
				break
			default:
				logTrace "Unknown Notification Event: ${cmd.event}"
		}		
	}
	else if (cmd.notificationType == 8 && cmd.event == 0x0B) {
		map = createBatteryEventMap(0)
	}
	else {
		logTrace "Unknown Notification Type: ${cmd.notificationType}"
	}
	
	def result = []
	if (map) {
		result << createEvent(map)
	}
	return result
}

private createTamperEventMap(val) {
	logDebug "Tamper is ${val}"
	return [name:"tamper", value:val, displayed:(val == "detected")]
}

private createMotionEventMap(val) {
	logDebug "Motion is ${val}"
	return [name:"motion", value:val]
}

private createBatteryEventMap(val) {
	state.lastBatteryReport = new Date().time	
	logDebug "Battery is ${val}%"
	return [name:"battery", value:val, unit:"%"]
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
	logDebug "Unknown Command: $cmd"
	return []
}


// Settings
private getCheckinIntervalSettingSeconds() {
	return (convertOptionSettingToInt(checkinIntervalOptions, checkinIntervalSetting) ?: 360) * 60
}
private getCheckinIntervalSetting() {
	return settings?.checkinInterval ?: findDefaultOptionName(checkinIntervalOptions)
}
private getBatteryReportingIntervalSettingMinutes() {
	return convertOptionSettingToInt(checkinIntervalOptions, batteryReportingIntervalSetting) ?: 720
}
private getBatteryReportingIntervalSetting() {
	return settings?.reportBatteryEvery ?: findDefaultOptionName(checkinIntervalOptions)
}
private getDebugOutputSetting() {
	return (settings?.debugOutput	!= false)
}
 
private getCheckinIntervalOptions() {
	[
		[name: "10 Minutes", value: 10],
		[name: "15 Minutes", value: 15],
		[name: "30 Minutes", value: 30],
		[name: "1 Hour", value: 60],
		[name: "2 Hours", value: 120],
		[name: "3 Hours", value: 180],
		[name: formatDefaultOptionName("6 Hours"), value: 360],
		[name: "9 Hours", value: 540],
		[name: "12 Hours", value: 720],
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