/**
 *  Dome Mouser v1.1
 *  (Model: DMMZ1)
 *
 *  Author: 
 *    Kevin LaFramboise (krlaframboise)
 *
 *  URL to documentation:  https://community.smartthings.com/t/release-dome-mouser-official/75732
 *    
 *
 *  Changelog:
 *
 *    1.1 (02/09/2017)
 *      - Cleaned code for publication.
 *
 *    1.0 (01/27/2017)
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
		name: "Dome Mouser", 
		namespace: "krlaframboise", 
		author: "Kevin LaFramboise"
	) {
		capability "Sensor"
		capability "Motion Sensor"
		capability "Contact Sensor"
		capability "Battery"
		capability "Configuration"
		capability "Refresh"
		capability "Polling"
		
		attribute "lastCheckin", "number"
		attribute "status", "enum", ["armed", "disarmed", "tripped"]
		
		fingerprint deviceId: "0x0701", inClusters: "0x30, 0x59, 0x5A, 0x5E, 0x70, 0x71, 0x72, 0x73, 0x80, 0x84, 0x85, 0x86"
		
		fingerprint mfr:"021F", prod:"0003", model:"0104"
	}
	
	simulator { }
	
	preferences {
		input "ledAlarm", "enum",
			title: "Tripped LED Alarm:",
			defaultValue: ledAlarmSetting,
			required: false,
			displayDuringSetup: true,
			options: ledAlarmOptions.collect { it.name }
		input "wakeUpInterval", "enum",
			title: "Wake Up Interval:",
			defaultValue: wakeUpIntervalSetting,
			required: false,
			displayDuringSetup: true,
			options: wakeUpIntervalOptions.collect { it.name }
		input "batteryReportingInterval", "enum",
			title: "Battery Reporting Interval:",
			defaultValue: wakeUpIntervalSetting,
			required: false,
			displayDuringSetup: true,
			options: wakeUpIntervalOptions.collect { it.name }
		input "debugOutput", "bool", 
			title: "Enable debug logging?", 
			defaultValue: true, 
			required: false
	}

	tiles(scale: 2) {
		multiAttributeTile(name:"status", type: "generic", width: 6, height: 4, canChangeIcon: false){
			tileAttribute ("device.status", key: "PRIMARY_CONTROL") {
				attributeState "disarmed", 
					label:'Disarmed',					
					icon:"https://raw.githubusercontent.com/krlaframboise/SmartThingsPublic/master/devicetypes/krlaframboise/dome-mouser.src/mouse.png",
					backgroundColor:"#ffffff"
				attributeState "armed", 
					label:'Armed', 
					icon:"https://raw.githubusercontent.com/krlaframboise/SmartThingsPublic/master/devicetypes/krlaframboise/dome-mouser.src/mouse.png", 
					backgroundColor:"#79b821"
				attributeState "tripped", 
					label:'Tripped', 
					icon:"https://raw.githubusercontent.com/krlaframboise/SmartThingsPublic/master/devicetypes/krlaframboise/dome-mouser.src/rip.png", 
					backgroundColor:"#bc2323"
			}
			tileAttribute ("device.status", key: "SECONDARY_CONTROL") {
				attributeState "disarmed", 
					label:'\"Contact Open\"', 
					backgroundColor:"#ffffff"
				attributeState "armed", 
					label:'\"Contact Closed\"', 
					backgroundColor:"#79b821"
				attributeState "tripped", 
					label:'\"Motion Active\"',
					backgroundColor:"#bc2323"
			}
		}	
		
		standardTile("refresh", "device.refresh", width: 2, height: 2) {
			state "refresh", label:'Refresh', action: "refresh", icon:"st.secondary.refresh-icon"
		}
		
		valueTile("battery", "device.battery", decoration: "flat", width: 2, height: 2){
			state "battery", label:'${currentValue}% battery', unit:""
		}
		
		main "status"
		details(["status", "refresh", "battery"])
	}
}

def updated() {	
	// This method always gets called twice when preferences are saved.
	if (!isDuplicateCommand(state.lastUpdated, 3000)) {		
		state.lastUpdated = new Date().time
		logTrace "updated()"

		logForceWakeupMessage "The configuration will be updated the next time the device wakes up."
		state.pendingChanges = true
	}		
}

// Sends the configuration settings to the device.
def configure() {
	logTrace "configure()"
	def cmds = []
	def refreshAll = (!state.isConfigured || state.pendingRefresh || !settings?.ledAlarm)
	
	if (!state.isConfigured) {
		logTrace "Waiting 1 second because this is the first time being configured"		
		sendEvent(getEventMap("status", "disarmed", false))
		sendEvent(getEventMap("motion", "inactive", false))
		sendEvent(getEventMap("contact", "open", false))
		cmds << "delay 1000"
	}
	
	configData.sort { it.paramNum }.each { 
		cmds += updateConfigVal(it.paramNum, it.value, refreshAll)	
	}
	
	if (refreshAll || canReportBattery()) {
		cmds << batteryGetCmd()
	}
	
	cmds << wakeUpIntervalSetCmd(convertOptionSettingToInt(wakeUpIntervalOptions, wakeUpIntervalSetting) * 60 * 60)
		
	if (cmds) {
		logDebug "Sending configuration to device."
		return delayBetween(cmds, 1000)
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


// Forces the configuration to be resent to the device the next time it wakes up.
def refresh() {	
	logForceWakeupMessage "The sensor data will be refreshed the next time the device wakes up."
	state.pendingRefresh = true
}

private logForceWakeupMessage(msg) {
	logDebug "${msg}  You can force the device to wake up immediately by pressing the connect button twice."
}

// Handles message from device.
def parse(String description) {
	def result = []

	def cmd = zwave.parse(description, commandClassVersions)
	if (cmd) {
		result += zwaveEvent(cmd)
	}
	else {
		logDebug "Unable to parse description: $description"
	}
	
	if (canCheckin()) {
		result << createEvent(name: "lastCheckin",value: new Date().time, isStateChange: true, displayed: false)
	}
	
	return result
}

private getCommandClassVersions() {
	[
		0x30: 2,	// Sensor Binary
		0x59: 1,  // AssociationGrpInfo
		0x5A: 1,  // DeviceResetLocally
		0x5E: 2,  // ZwaveplusInfo
		0x70: 1,  // Configuration
		0x71: 3,  // Alarm v1 or Notification v4
		0x72: 2,  // ManufacturerSpecific
		0x73: 1,  // Powerlevel
		0x80: 1,  // Battery
		0x84: 2,  // WakeUp
		0x85: 2,  // Association
		0x86: 1		// Version (2)
	]
}

// Send outstanding configuration changes to device and report battery level.
def zwaveEvent(physicalgraph.zwave.commands.wakeupv2.WakeUpNotification cmd)
{
	logTrace "WakeUpNotification: $cmd"
	def result = []
	
	if (state.pendingChanges != false) {
		result += configure()
	}
	else if (state.pendingRefresh || canReportBattery()) {
		result << batteryGetCmd()
	}
	else {
		logTrace "Skipping battery check because it was already checked within the last ${batteryReportingIntervalSetting}."
	}
	
	if (result) {
		result << "delay 2000"
	}
	result << wakeUpNoMoreInfoCmd()
	
	return response(result)
}

private canReportBattery() {
	def reportEveryMS = (convertOptionSettingToInt(wakeUpIntervalOptions, batteryReportingIntervalSetting) * 60 * 60 * 1000)
		
	return (!state.lastBatteryReport || ((new Date().time) - state.lastBatteryReport > reportEveryMS)) 
}

// Creates event for the battery level.
def zwaveEvent(physicalgraph.zwave.commands.batteryv1.BatteryReport cmd) {
	logTrace "BatteryReport: $cmd"
	def val = (cmd.batteryLevel == 0xFF ? 1 : cmd.batteryLevel)
	state.lastBatteryReport = new Date().time	
	logDebug "Battery ${val}%"
	[
		createEvent(getEventMap("battery", val, null, null, "%"))
	]
}	

// Displays configuration value in live logging.
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

def zwaveEvent(physicalgraph.zwave.commands.notificationv3.NotificationReport cmd) {
	def result = []	
	logTrace "NotificationReport: $cmd"
	if (cmd.notificationType == 0x13) {
		switch (cmd.event) {
			case 0:
				logDebug "Trap Reset"
				result << createEvent(getEventMap("status", "disarmed", null, "Trap Reset/Contact Open/Motion Inactive"))
				result << createEvent(getEventMap("motion", "inactive", false))
				result << createEvent(getEventMap("contact", "closed", false))
				break
			case 2:
				logDebug "Trap is armed"
				result << createEvent(getEventMap("status", "armed", null, "Armed/Contact Closed"))
				result << createEvent(getEventMap("contact", "closed", false))
				break
			case 4:
				logDebug "Trap is disarmed"
				result << createEvent(getEventMap("status", "disarmed", null, "Disarmed/Contact Open"))
				result << createEvent(getEventMap("contact", "open", false))
				break
			case 8:
				logDebug "Trap has been tripped"
				result << createEvent(getEventMap("status", "tripped", null, "Tripped/Motion Active"))
				result << createEvent(getEventMap("motion", "active", false))
				break
			default:
				logDebug "Unknown notification type: ${cmd}"
		}
	}
	return result
}

// Ignores sensor binary report since it uses the notification report to detect activity.
def zwaveEvent(physicalgraph.zwave.commands.sensorbinaryv2.SensorBinaryReport cmd) {
	logTrace "SensorBinaryReport: $cmd"
	return []
}

// Handles unexpected command.
def zwaveEvent(physicalgraph.zwave.Command cmd) {
	logDebug "Unhandled Command: $cmd"
	return []
}

private getEventMap(name, value, displayed=null, desc=null, unit=null) {	
	def isStateChange = (device.currentValue(name) != value)
	displayed = (displayed == null ? isStateChange : displayed)
	def eventMap = [
		name: name,
		value: value,
		displayed: displayed,
		isStateChange: isStateChange
	]
	if (desc) {
		eventMap.descriptionText = desc
	}
	if (unit) {
		eventMap.unit = unit
	}	
	logTrace "Creating Event: ${eventMap}"
	return eventMap
}

private wakeUpIntervalSetCmd(val) {
	logTrace "wakeUpIntervalSetCmd(${val})"
	return zwave.wakeUpV2.wakeUpIntervalSet(seconds:val, nodeid:zwaveHubNodeId).format()
}

private wakeUpNoMoreInfoCmd() {
	return zwave.wakeUpV2.wakeUpNoMoreInformation().format()
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

// Settings
private getLedAlarmSetting() {
	return settings?.ledAlarm ?: findDefaultOptionName(ledAlarmOptions)
}
private getWakeUpIntervalSetting() {
	return settings?.wakeUpInterval ?: findDefaultOptionName(wakeUpIntervalOptions)
}
private getBatteryReportingIntervalSetting() {
	return settings?.batteryReportingInterval ?: findDefaultOptionName(wakeUpIntervalOptions)
}

// Configuration Parameters
private getConfigData() {
	// [paramNum: 1, name: "Basic Set Association Group 2"],
	// [paramNum: 2, name: "Firing Mode"],
	// [paramNum: 3, name: "High Voltage Duration Time"],
	return [		
		[paramNum: 4, name: "LED Alarm Enabled", value: (ledAlarmSetting == "Disabled") ? 0 : 1],
		[paramNum: 5, name: "LED Alarm Length", value: convertOptionSettingToInt(ledAlarmOptions, ledAlarmSetting)]
	]	
}

private getLedAlarmOptions() {
	[
		[name: "Disabled", value: 0],
		[name: formatDefaultOptionName("Until Trap is Cleared"), value: 0],
		[name: "1 Hour", value: 1],
		[name: "2 Hours", value: 2],
		[name: "4 Hours", value: 4],
		[name: "8 Hours", value: 8],
		[name: "12 Hours", value: 12],
		[name: "1 Day", value: 24],
		[name: "2 Days", value: 48],
		[name: "3 Days", value: 72],
		[name: "4 Days", value: 96],
		[name: "5 Days", value: 120],
		[name: "6 Days", value: 144],
		[name: "1 Week", value: 168]
	]
}

private getWakeUpIntervalOptions() {
	[
		[name: "2 Hours", value: 2],
		[name: "4 Hours", value: 4],
		[name: "6 Hours", value: 6],
		[name: "8 Hours", value: 8],
		[name: formatDefaultOptionName("12 Hours"), value: 12],
		[name: "18 Hours", value: 18],
		[name: "24 Hours", value: 24]
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

private canCheckin() {
	// Only allow the event to be created once per minute.
	def lastCheckin = device.currentValue("lastCheckin")
	return (!lastCheckin || lastCheckin < (new Date().time - 60000))
}

private isDuplicateCommand(lastExecuted, allowedMil) {
	!lastExecuted ? false : (lastExecuted + allowedMil > new Date().time) 
}

private logDebug(msg) {
	if (settings?.debugOutput || settings?.debugOutput == null) {
		log.debug "$msg"
	}
}

private logTrace(msg) {
	// log.trace "$msg"
}