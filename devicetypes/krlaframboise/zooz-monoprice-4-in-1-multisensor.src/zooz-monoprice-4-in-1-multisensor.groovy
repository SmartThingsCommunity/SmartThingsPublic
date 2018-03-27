/**
 *  Zooz/Monoprice 4-in-1 Multisensor 1.3.6
 *
 *  Zooz Z-Wave 4-in-1 Sensor (ZSE40)
 *
 *  Monoprice Z-Wave Plus 4-in-1 Motion Sensor with Temperature, Humidity, and Light Sensors (P/N 15902)
 *
 *  Author: 
 *    Kevin LaFramboise (krlaframboise)
 *
 *  URL to documentation:  https://community.smartthings.com/t/release-zooz-4-in-1-multisensor/82989?u=krlaframboise
 *    
 *
 *  Changelog:
 *
 *    1.3.6 (05/24/2017)
 *    	- Made illuminance events appear in recently tab because CoRE was ignoring the values.
 *    	- Added device name to description fields.
 *    	- Added a setting that causes the primary status to get rounded to the nearest whole number.
 *
 *    1.3.4 (04/29/2017)
 *    	- Made refresh command return null to fix possible issue it's causing with webCoRE.
 *
 *    1.3.3 (04/23/2017)
 *    	- SmartThings broke parse method response handling so switched to sendhubaction.
 *
 *    1.3.2 (04/20/2017)
 *      - Added workaround for ST Health Check bug.
 *
 *    1.3.1 (03/28/2017)
 *      - Added setting for reporting max lux illuminance.
 *
 *    1.3 (03/27/2017)
 *      - Added Light % to LUX Conversion
 *      - Added Lux Offset and main tile option.
 *      - Fixed syncing issue between main tile and actual values.
 *
 *    1.2.1 (03/12/2017)
 *      - Added Health Check capability.
 *      - Added offsets for Temp/Humidity/light
 *      - Made main and secondary tiles configurable.
 *
 *    1.1.3 (01/18/2017)
 *      - Removed changes made in the previous 2 versions because they aren't needed.
 *
 *    1.1 (01/15/2017)
 *      - Added firmware attribute
 *			- Added new fingerprint for latest model of the Zooz 4-in-1 sensor.
 *			- Added new LED Indicator Mode for updated Zooz model.
 *			- Added temperature scale setting
 *			- Replaced enum settings with numbers because defaultValue doesn't work with enums on the Android Mobile App.
 *
 *    1.0.2 (01/12/2017)
 *      - Fixed light tile text and changed minimum light threshold to 5 because that's the lowest value supported by the latest firmware version of the zooz 4-in-1 sensor.
 *
 *    1.0.1 (01/12/2017)
 *      - Changed illuminance from lux to %.
 *
 *    1.0 (01/08/2017)
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
		name: "Zooz/Monoprice 4-in-1 Multisensor", 
		namespace: "krlaframboise", 
		author: "Kevin LaFramboise"
	) {
		capability "Sensor"
		capability "Configuration"
		capability "Motion Sensor"
		capability "Illuminance Measurement"
		capability "Relative Humidity Measurement"
		capability "Temperature Measurement"		
		capability "Battery"
		capability "Tamper Alert"
		capability "Refresh"
		capability "Health Check"
		
		attribute "lastCheckin", "string"
		attribute "lastUpdate", "string"
		attribute "primaryStatus", "string"
		attribute "secondaryStatus", "string"
		attribute "pLight", "number"
		attribute "lxLight", "number"
		attribute "firmwareVersion", "string"
						
		fingerprint deviceId: "0x0701", inClusters: "0x5E, 0x98, 0x86, 0x72, 0x5A, 0x85, 0x59, 0x73, 0x80, 0x71, 0x31, 0x70, 0x84, 0x7A"
		
		fingerprint mfr:"027A", prod:"2021", model:"2101", deviceJoinName: "Zooz 4-in-1 Multisensor"
		
		fingerprint mfr:"0109", prod:"2021", model:"2101", deviceJoinName: "Zooz/Monoprice 4-in-1 Multisensor"
	}
	
	simulator { }
	
	preferences {
		input "primaryTileStatus", "enum",
			title: "Primary Status:",
			defaultValue: primaryTileStatusSetting,
			required: false,
			options: primaryStatusOptions
		input "roundPrimaryStatus", "bool", 
			title: "Round the Primary Status to a whole number?", 
			defaultValue: false, 
			displayDuringSetup: true, 
			required: false
		input "secondaryTileStatus", "enum",
			title: "Secondary Status:",
			defaultValue: secondaryTileStatusSetting,
			required: false,
			options: secondaryStatusOptions
		input "tempScale", "number",
			title: "Temperature Scale [0-1]${getNameValueSettingDesc(tempUnits)}",
			displayDuringSetup: true,
			required:false,
			defaultValue: tempScaleSetting,
			range: "0..1"
		input "tempTrigger", "number",
			title: "Temperature Change Trigger [1-50]\n(1 = 0.1°)\n(50 = 5.0°)",			
			displayDuringSetup: true,
			required:false,
			defaultValue: tempTriggerSetting,
			range: "1..50"
		input "tempOffset", "number",
			title: "Temperature Offset [-25 to 25]\n(0 = No Offset)\n(-1 = Subtract 1°)\n(1 = Add 1°)",			
			displayDuringSetup: true,
			required:false,
			defaultValue: tempOffsetSetting,
			range: "-25..25"
		input "humidityTrigger", "number",
			title: "Humidity Change Trigger [1-50]\n(1% - 50%)",
			displayDuringSetup: true,
			required:false,
			defaultValue: humidityTriggerSetting,
			range: "1..50"
		input "humidityOffset", "number",
			title: "Humidity % Offset [-25 to 25]\n(0 = No Offset)\n(-1 = Subtract 1%)\n(1 = Add 1%)",			
			displayDuringSetup: true,
			required:false,
			defaultValue: humidityOffsetSetting,
			range: "-25..25"
		input "lightTrigger", "number",
			title: "Light Change Trigger [5-50]\n(5% - 50%)",
			displayDuringSetup: true,
			required:false,
			defaultValue: lightTriggerSetting,
			range: "5..50"
		input "lightOffset", "number",
			title: "Light % Offset [-25 to 25]\n(0 = No Offset)\n(-1 = Subtract 1%)\n(1 = Add 1%)",
			displayDuringSetup: true,
			required:false,
			defaultValue: lightOffsetSetting,
			range: "-25..25"
		input "lxLightOffset", "number",
			title: "Light Lux Offset [-25 to 25]\n(0 = No Offset)\n(-1 = Subtract 1 lx)\n(1 = Add 1 lx)",
			displayDuringSetup: true,
			required:false,
			defaultValue: lxLightOffsetSetting,
			range: "-25..25"
		input "reportLx", "bool", 
			title: "Report Illuminance as Lux?\n(When enabled, a calculated lux level will be used for illuminance instead of the default %.)",
			defaultValue: reportLxSetting,
			displayDuringSetup: true, 
			required: false
		input "maxLx", "number",
			title: "Lux value to report when light level is at 100%:",
			defaultValue: maxLxSetting,
			displayDuringSetup: true,
			required: false
		input "motionTime", "number",
			title: "Motion Retrigger Time (Minutes)",
			displayDuringSetup: true,
			required:false,
			defaultValue: motionTimeSetting,
			range: "1..255"
		input "motionSensitivity", "number",
			title: "Motion Sensitivity [1-7]\n(1 = Most Sensitive)\n(7 = Least Sensitive)",
			displayDuringSetup: true,
			required:false,
			defaultValue: motionSensitivitySetting,
			range: "1..7"
		input "ledIndicatorMode", "number",
			title: "LED Indicator Mode [1-4]${getNameValueSettingDesc(ledIndicatorModes)}",
			displayDuringSetup: true,
			required: false,
			defaultValue: ledIndicatorModeSetting,
			range: "1..4"
		input "checkinInterval", "number",
			title: "Minimum Check-in Interval [0-167]\n(0 = 10 Minutes [FOR TESTING ONLY])\n(1 = 1 Hour)\n(167 = 7 Days)",
			defaultValue: checkinIntervalSetting,
			range: "0..167",
			displayDuringSetup: true, 
			required: false
		input "reportBatteryEvery", "number", 
			title: "Battery Reporting Interval [1-167]\n(1 = 1 Hour)\n(167 = 7 Days)", 
			description: "This setting can't be less than the Minimum Check-in Interval.",
			defaultValue: batteryReportingIntervalSetting,
			range: "1..167",
			displayDuringSetup: true, 
			required: false		
		input "autoClearTamper", "bool", 
			title: "Automatically Clear Tamper?\n(The tamper detected event is raised when the device is opened.  This setting allows you to decide whether or not to have the clear event automatically raised when the device closes.)",
			defaultValue: false,
			displayDuringSetup: true, 
			required: false		
		input "debugOutput", "bool", 
			title: "Enable debug logging?", 
			defaultValue: true, 
			displayDuringSetup: true, 
			required: false
	}

	tiles(scale: 2) {
		multiAttributeTile(name:"mainTile", type: "generic", width: 6, height: 4, canChangeIcon: true){
			tileAttribute ("device.primaryStatus", key: "PRIMARY_CONTROL") {
				attributeState "default",
					label:'${currentValue}',
					backgroundColor:"#53a7c0"
				attributeState "inactive", 
					label:'NO MOTION', 
					icon:"st.motion.motion.inactive", 
					backgroundColor:"#ffffff"
				attributeState "active", 
					label:'MOTION', 
					icon:"st.motion.motion.active", 
					backgroundColor:"#53a7c0"
			}
			tileAttribute ("device.secondaryStatus", key: "SECONDARY_CONTROL") {
				attributeState "default", label:'${currentValue}'
				attributeState "inactive", label:'NO MOTION'
				attributeState "active", label:'MOTION'
			}
		}
		
		valueTile("temperature", "device.temperature", width: 2, height: 2) {
			state("temperature", label:'${currentValue}°',
			backgroundColors:[
				[value: 31, color: "#153591"],
				[value: 44, color: "#1e9cbb"],
				[value: 59, color: "#90d2a7"],
				[value: 74, color: "#44b621"],
				[value: 84, color: "#f1d801"],
				[value: 95, color: "#d04e00"],
				[value: 96, color: "#bc2323"]
			])
		}
		
		valueTile("humidity", "device.humidity", decoration: "flat", width: 2, height: 2){
			state "humidity", label:'${currentValue}%\nHumidity', unit:""
		}
		
		valueTile("pLight", "device.pLight", decoration: "flat", width: 2, height: 2){
			state "pLight", label:'${currentValue}%\nLight', unit: ""
		}

		valueTile("lxLight", "device.lxLight", decoration: "flat", width: 2, height: 2){
			state "lxLight", label:'${currentValue} lx\nLight', unit: ""
		}
		
		valueTile("motion", "device.motion",  width: 2, height: 2){
			state "inactive", label:'No\nMotion', icon:"", backgroundColor:"#cccccc"
			state "active", label:'Motion', icon:"", backgroundColor:"#53a7c0"
		}
		
		standardTile("tampering", "device.tamper", width: 2, height: 2) {
			state "detected", label:"Tamper", backgroundColor: "#ff0000"
			state "clear", label:"No\nTamper", backgroundColor: "#cccccc"
		}
		
		valueTile("battery", "device.battery", decoration: "flat", width: 2, height: 2){
			state "battery", label:'${currentValue}% Battery', unit:""
		}		
		
		standardTile("refresh", "device.refresh", width: 2, height: 2) {
			state "default", label: "Refresh", action: "refresh", icon:"st.secondary.refresh-icon"
		}
		
		valueTile("lastUpdate", "device.lastUpdate", decoration: "flat", width: 2, height: 2){
			state "lastUpdate", label:'Settings\nUpdated\n\n${currentValue}', unit:""
		}
		
		main("mainTile")
		details(["mainTile", "humidity", "temperature", "lxLight", "battery", "pLight", "motion", "tampering", "refresh", "lastUpdate"])
	}
}

def updated() {	
	// This method always gets called twice when preferences are saved.
	if (!isDuplicateCommand(state.lastUpdated, 3000)) {
		state.lastUpdated = new Date().time
		logTrace "updated()"
		
		initializeOffsets()
						
		if (settings?.ledIndicatorModeSetting == 4) {
			logWarn "LED Indicator Mode #4 is only available in the Zooz device with firmware v16.9 and above."
		}
				
		if (!getAttrValue("tamper")) {
			sendEvent(createTamperEventMap("clear"))
		}

		logForceWakeupMessage("The configuration will be updated the next time the device wakes up.")
		state.pendingChanges = true
	}	
}

private initializeOffsets() {
	def eventMaps = []
	
	if (state.actualTemp != null) {
		eventMaps += createTempEventMaps(state.actualTemp, true)
	}
	
	if (state.actualHumidity != null) {
		eventMaps += createHumidityEventMaps(state.actualHumidity, true)
	}
	
	if (state.actualLight != null) {
		eventMaps += createLightEventMaps(state.actualLight, true)
	}
	
	eventMaps += createStatusEventMaps(eventMaps, true)
	
	eventMaps?.each { eventMap ->
		
		eventMap.descriptionText = getDisplayedDescriptionText(eventMap)
		
		sendEvent(eventMap)
	}
}

private getDisplayedDescriptionText(eventMap) {
	def deviceName = "${device.displayName}"
	if (eventMap?.displayed && eventMap?.descriptionText && !eventMap?.descriptionText?.contains(deviceName)) {
		return "${deviceName}: ${eventMap.descriptionText}"
	}
	else {
		return eventMap?.descriptionText
	}
}

def configure() {	
	logTrace "configure()"
	
	def cmds = []		
	if (!getAttrValue("firmwareVersion")) {
		// Give inclusion time to finish.
		logTrace "Waiting 1 second because this is the first time being configured"		
		cmds << "delay 500"		
		cmds << versionGetCmd()
	}
	
	if (state.pendingChanges) {
		
		sendEvent(name: "lastUpdate", value: convertToLocalTimeString(new Date()), displayed: false)
		
		initializeCheckin()
		
		cmds += delayBetween([
			wakeUpIntervalSetCmd(checkinIntervalSettingSeconds),
			configSetCmd(tempScaleParamNum, tempScaleSetting),
			configSetCmd(tempTriggerParamNum, tempTriggerSetting),
			configSetCmd(humidityTriggerParamNum, humidityTriggerSetting),
			configSetCmd(lightTriggerParamNum, lightTriggerSetting),
			configSetCmd(motionTimeParamNum, motionTimeSetting),
			configSetCmd(motionSensitivityParamNum, motionSensitivitySetting),
			configSetCmd(ledIndicatorModeParamNum, ledIndicatorModeSetting),
			configGetCmd(tempScaleParamNum),
			configGetCmd(tempTriggerParamNum),
			configGetCmd(humidityTriggerParamNum),
			configGetCmd(lightTriggerParamNum),
			configGetCmd(motionTimeParamNum),
			configGetCmd(motionSensitivityParamNum),
			configGetCmd(ledIndicatorModeParamNum)
		], 50)
	}
	else {
		cmds += refreshSensorData()
	}
	return sendResponse(cmds)
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
	// Set the Health Check interval so that it can be skipped once plus 2 minutes.
	def checkInterval = ((checkinIntervalSettingSeconds * 2) + (2 * 60))
	
	sendEvent(name: "checkInterval", value: checkInterval, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])
}

// Required for HealthCheck Capability, but doesn't actually do anything because this device sleeps.
def ping() {
	logDebug "ping()"	
}

private refreshSensorData() {
	logDebug "Refreshing Sensor Data"
	return delayBetween([
		batteryGetCmd(),
		basicGetCmd(),
		sensorMultilevelGetCmd(tempSensorType),
		sensorMultilevelGetCmd(humiditySensorType),
		sensorMultilevelGetCmd(lightSensorType)
	], 50)
}

// Settings
private getRoundPrimaryStatusSetting() {
	return settings?.roundPrimaryStatus ?: false
}
private getPrimaryTileStatusSetting() {
	return settings?.primaryTileStatus ?: "motion"
}
private getSecondaryTileStatusSetting() {	
	return settings?.secondaryTileStatus ?: "none"
}
private getTempScaleSetting() {
	return safeToInt(settings?.tempScale, (isNewZoozDevice() ? 1 : 0))
}
private getTempTriggerSetting() {
	return safeToInt(settings?.tempTrigger, 10)
}
private getTempOffsetSetting() {
	return safeToInt(settings?.tempOffset, 0)
}
private getHumidityTriggerSetting() {
	return safeToInt(settings?.humidityTrigger, 10)
}
private getHumidityOffsetSetting() {
	return safeToInt(settings?.humidityOffset, 0)
}
private getLightTriggerSetting() {
	return safeToInt(settings?.lightTrigger, 10)
}
private getLightOffsetSetting() {
	return safeToInt(settings?.lightOffset, 0)
}
private getLxLightOffsetSetting() {
	return safeToInt(settings?.lxLightOffset, 0)
}
private getReportLxSetting() {
	return (settings?.reportLx ?: false)
}
private getMaxLxSetting() {
	return safeToInt(settings?.maxLx, 50)
}
private getMotionTimeSetting() {
	return safeToInt(settings?.motionTime, 3)
}
private getMotionSensitivitySetting() {
	return safeToInt(settings?.motionSensitivity, 4)
}
private getLedIndicatorModeSetting() {
	return safeToInt(settings?.ledIndicatorMode, 3)
}
private getCheckinIntervalSetting() {
	return (safeToInt(settings?.checkinInterval, (isNewZoozDevice() ? 12 : 6)))
}
private getCheckinIntervalSettingSeconds() {
	if (checkinIntervalSetting == 0) {
		return (10 * 60)
	}
	else {
		return (checkinIntervalSetting * 60 * 60)
	}
}
private getBatteryReportingIntervalSetting() {
	return safeToInt(settings?.reportBatteryEvery, 12)
}
private getBatteryReportingIntervalSettingSeconds() {
	return (batteryReportingIntervalSetting * 60 * 60)
}
private getAutoClearTamperSetting() {
	return (settings?.autoClearTamper ?: false)
}
private getDebugOutputSetting() {
	return (settings?.debugOutput || settings?.debugOutput == null)
}

private getNameValueSettingDesc(nameValueMap) {
	def desc = ""
	nameValueMap?.sort { it.value }.each { 
		desc = "${desc}\n(${it.value} - ${it.name})"
	}
	return desc
}

private getTempUnits() {
	return [
		[name: "Celsius", unit: "C", value: 0],
		[name: "Fahrenheit", unit: "F", value: 1]
	]
}

private getLedIndicatorModes() {
	return [
		[name: "Temperature Off / Motion Off", value: 1],
		[name: "Temperature Pulse / Motion Flash", value: 2],
		[name: "Temperature Flash / Motion Flash", value: 3],
		[name: "Temperature Off / Motion Flash", value: 4]
	]
}

private getPrimaryStatusOptions() {
	return [
		["motion":"Motion"],
		["temperature":"Temperature"],
		["humidity": "Relative Humidity"],
		["pLight":"Light %"],
		["lxLight":"Light Lux"]
	]
}

private getSecondaryStatusOptions() {
	return [
		["none":"None"],
		["motion":"Motion"],
		["temperature":"Temperature"],
		["humidity": "Relative Humidity"],
		["pLight":"Light %"],
		["lxLight":"Light Lux"],
		["combined":"Combined Values"]
	]
}

private isNewZoozDevice() {
	return (settings?.firmwareVersion != 265)
}

// Sensor Types
private getTempSensorType() { return 1 }
private getHumiditySensorType() { return 5 }
private getLightSensorType() { return 3 }

// Configuration Parameters
private getTempScaleParamNum() { return 1 }
private getTempTriggerParamNum() { return 2 }
private getHumidityTriggerParamNum() { return 3 }
private getLightTriggerParamNum() { return 4 }
private getMotionTimeParamNum() { return 5 }
private getMotionSensitivityParamNum() { return 6 }
private getLedIndicatorModeParamNum() { return 7 }


def parse(String description) {
	def result = []
	
	sendEvent(name: "lastCheckin", value: convertToLocalTimeString(new Date()), displayed: false, isStateChange: true)
	
	if (description.startsWith("Err 106")) {
		state.useSecureCmds = false
		log.warn "Secure Inclusion Failed: ${description}"
		result << createEvent( name: "secureInclusion", value: "failed", eventType: "ALERT", descriptionText: "This sensor failed to complete the network security key exchange. If you are unable to control it via SmartThings, you must remove it from your network and add it again.")
	}
	else if (description.startsWith("Err")) {
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
	return result
}

def zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityMessageEncapsulation cmd) {
	def encapCmd = cmd.encapsulatedCommand(getCommandClassVersions())
		
	def result = []
	if (encapCmd) {
		state.useSecureCmds = true
		result += zwaveEvent(encapCmd)
	}
	else if (cmd.commandClassIdentifier == 0x5E) {
		logTrace "Unable to parse ZwaveplusInfo cmd"
	}
	else {
		log.warn "Unable to extract encapsulated cmd from $cmd"
		result << createEvent(descriptionText: "$cmd")
	}
	return result
}

private getCommandClassVersions() {
	[
		0x20: 1,  // Basic
		0x31: 5,	// Sensor Multilevel (v7)
		0x59: 1,  // AssociationGrpInfo
		0x5A: 1,  // DeviceResetLocally
		0x5E: 2,  // ZwaveplusInfo
		0x70: 1,  // Configuration
		0x71: 3,  // Alarm v1 or Notification v4
		0x72: 2,  // ManufacturerSpecific
		0x73: 1,  // Powerlevel
		0x7A: 2,  // FirmwareUpdateMd
		0x80: 1,  // Battery
		0x84: 2,  // WakeUp
		0x85: 2,  // Association
		0x86: 1,	// Version (2)
		0x98: 1		// Security
	]
}

def zwaveEvent(physicalgraph.zwave.commands.wakeupv2.WakeUpNotification cmd)
{
	logTrace "WakeUpNotification: $cmd"
	def result = []
	
	if (state.pendingChanges != false) {
		result += configure()
	}
	else if (state.pendingRefresh) {
		result += refreshSensorData()
	}
	else if (canReportBattery()) {
		result << batteryGetCmd()
	}
	else {
		logTrace "Skipping battery check because it was already checked within the last ${batteryReportingIntervalSetting} hours."
	}
	
	if (result) {
		result << "delay 2000"
	}
	result << wakeUpNoMoreInfoCmd()
	
	return sendResponse(result)
}

private canReportBattery() {
	def reportEveryMS = (batteryReportingIntervalSettingSeconds * 1000)
		
	return (!state.lastBatteryReport || ((new Date().time) - state.lastBatteryReport > reportEveryMS)) 
}

def zwaveEvent(physicalgraph.zwave.commands.batteryv1.BatteryReport cmd) {
	logTrace "BatteryReport: $cmd"
	def val = (cmd.batteryLevel == 0xFF ? 1 : cmd.batteryLevel)
	if (val > 100) {
		val = 100
	}
	else if (val < 1) {
		val = 1
	}
	state.lastBatteryReport = new Date().time	
	[
		createEvent(createEventMap("battery", val, "%"))
	]
}	

def zwaveEvent(physicalgraph.zwave.commands.manufacturerspecificv2.ManufacturerSpecificReport cmd) {
	logTrace "ManufacturerSpecificReport: ${cmd}"
	return []
}

def zwaveEvent(physicalgraph.zwave.commands.versionv1.VersionReport cmd) {
	logTrace "VersionCommandClassReport: ${cmd}"
	
	def version = "${cmd.applicationVersion}.${cmd.applicationSubVersion}"
	logDebug "Firmware Version: ${version}"
	
	def result = []
	if (getAttrValue("firmwareVersion") != "${version}") {
		result << createEvent(name: "firmwareVersion", value: "${version}", displayed: false)
	}
	return result 
}

def zwaveEvent(physicalgraph.zwave.commands.configurationv1.ConfigurationReport cmd) {
	logTrace "ConfigurationReport: $cmd"
	def configVal = cmd.scaledConfigurationValue
	def paramVal = configVal
	def paramName
	switch (cmd.parameterNumber) {
		case tempScaleParamNum:
			paramName = "Temperature Unit"
			paramVal = tempUnits.find { it.value == configVal }?.name
			break
		case tempTriggerParamNum:
			paramName = "Temperature Change Trigger"
			paramVal = "${configVal / 10}°"
			break
		case humidityTriggerParamNum:
			paramName = "Humidity Change Trigger"
			paramVal = "${configVal}%"
			break
		case lightTriggerParamNum:
			paramName = "Light Change Trigger"
			paramVal = "${configVal}%"
			break
		case motionTimeParamNum:
			paramName = "Motion Retrigger Time"
			paramVal = "${configVal} Minutes"
			break
		case motionSensitivityParamNum:
			paramName = "Motion Sensitivity"
			paramVal = configVal
			break
		case ledIndicatorModeParamNum:
			paramName = "LED Indicator Mode"
			paramVal = ledIndicatorModes.find { it.value == configVal }?.name
			break
		default:	
			paramName = "Parameter #${cmd.parameterNumber}"
	}		
	if (paramName) {
		if (paramVal == null) {
			paramVal = configVal
		}
		logDebug "${paramName}: ${paramVal}"
	} 
	state.pendingChanges = false
	return []
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd) {
	logTrace "BasicReport: $cmd"	
	return handleMotionEvent(cmd.value)
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicSet cmd) {
	logTrace "Basic Set: $cmd"	
	return handleMotionEvent(cmd.value)
}

private handleMotionEvent(val) {
	def motionVal = (val == 0xFF ? "active" : "inactive")
	
	def eventMaps = []
	eventMaps += createEventMaps("motion", motionVal, "", null, false)	
	eventMaps += createStatusEventMaps(eventMaps, false)
	
	def result = []
	eventMaps?.each {
		it.descriptionText = getDisplayedDescriptionText(it)
		result << createEvent(it)
	}
	return result
}


def zwaveEvent(physicalgraph.zwave.commands.notificationv3.NotificationReport cmd) {
	def result = []	
	logTrace "NotificationReport: $cmd"
	if (cmd.notificationType == 7) {
		if (cmd.eventParameter[0] == 3 || cmd.event == 3) {		
			result += handleTamperEvent(cmd.v1AlarmLevel)
		}
	}
	return result
}

private handleTamperEvent(val) {
	def result = []
	def tamperVal
	if (val == 0xFF) {
		tamperVal = "detected"
	}
	else if (val == 0) {
		if (autoClearTamperSetting) {
			tamperVal = "clear"
		}
		else {
			logDebug "Tamper is Clear"
		}
	}
	if (tamperVal) {
		result << createEvent(createTamperEventMap(tamperVal))
	}
	return result
}


def zwaveEvent(physicalgraph.zwave.commands.sensormultilevelv5.SensorMultilevelReport cmd) {
	state.lastRefreshed = new Date().time
	state.pendingRefresh = false	
	
	def eventMaps = []	
	switch (cmd.sensorType) {
		case tempSensorType:
			def unit = tempUnits.find { it.value == cmd.scale }?.unit
			def temp = convertTemperatureIfNeeded(cmd.scaledSensorValue, unit, cmd.precision)
			eventMaps += createTempEventMaps(temp, false)
			break		
		case humiditySensorType:
			eventMaps += createHumidityEventMaps(cmd.scaledSensorValue, false)
			break		
		case lightSensorType:
			eventMaps += createLightEventMaps(cmd.scaledSensorValue, false)
			break		
	}
	
	eventMaps += createStatusEventMaps(eventMaps, false)
	
	def result = []
	eventMaps?.each {
		logTrace "Creating Event: ${it}"
		it.descriptionText = getDisplayedDescriptionText(it)
		result << createEvent(it)
	}
	return result
}

private createTempEventMaps(val, onlyIfNew) {
	state.actualTemp = val
	def scale = getTemperatureScale()
	def offsetVal = applyOffset(val, tempOffsetSetting, "Temperature", "°${scale}")
	return createEventMaps("temperature", offsetVal, scale, null, onlyIfNew)	
}

private createHumidityEventMaps(val, onlyIfNew) {
	state.actualHumidity = val
	def offsetVal = applyOffset(val, humidityOffsetSetting, "Humidity", "%")
	return createEventMaps("humidity", offsetVal, "%", null, onlyIfNew)
}

private createLightEventMaps(val, onlyIfNew) {
	state.actualLight = val
	def pOffsetVal = applyOffset(val, lightOffsetSetting, "Light", "%")
	def lxOffsetVal = (val == 100) ? maxLxSetting : applyOffset(calculateLxVal(val), lxLightOffsetSetting, "Light", "lx")
	def lightOffsetVal = reportLxSetting ? lxOffsetVal : pOffsetVal
	def lightUnit = reportLxSetting ? "lx" : "%"
	
	def result = []
	result += createEventMaps("pLight", pOffsetVal, "%", false, onlyIfNew)
	result += createEventMaps("lxLight", lxOffsetVal, "lx", false, onlyIfNew)
	result += createEventMaps("illuminance", lightOffsetVal, lightUnit, true, onlyIfNew)
	return result
}

private calculateLxVal(pVal) {
	def multiplier = lxConversionData.find {
		pVal >= it.min && pVal <= it.max
	}?.multiplier ?: 0.5312
	def lxVal = pVal * multiplier
	return Math.round(safeToDec(lxVal) * 100) / 100
}

private getLxConversionData() {
	return [
		[min: 0, max: 9.99, multiplier: 0.4451],
		[min: 10, max: 19.99, multiplier: 0.563],
		[min: 20, max: 29.99, multiplier: 0.538],
		[min: 30, max: 39.99, multiplier: 0.536],
		[min: 40, max: 49.99, multiplier: 0.559],
		[min: 50, max: 59.99, multiplier: 0.6474],
		[min: 60, max: 69.99, multiplier: 0.5222],
		[min: 70, max: 79.99, multiplier: 0.5204],
		[min: 80, max: 89.99, multiplier: 0.4965],
		[min: 90, max: 100, multiplier: 0.4843]
	]
}

private applyOffset(val, offsetVal, name, unit) {
	if (offsetVal) {
		logDebug "Applying ${offsetVal}${unit} ${name} Offset to ${val}${unit}"
		val = (safeToDec(val, 0) + safeToDec(offsetVal, 0))
	}	
	return val
}

private createStatusEventMaps(eventMaps, onlyIfNew) {
	def result = []
	
	def primaryStatus = eventMaps?.find { it.name == primaryTileStatusSetting }?.descriptionText
	if (primaryStatus) {
		if (roundPrimaryStatusSetting) {
			primaryStatus = formatPrimaryStatusNumber(primaryStatus)
		}
		result += createEventMaps("primaryStatus", primaryStatus, "", false, onlyIfNew)
	}
	
	def secondaryStatus = getSecondaryStatus(eventMaps)
	if (secondaryStatus || secondaryTileStatusSetting == "none") {
		result += createEventMaps("secondaryStatus", secondaryStatus, "", false, onlyIfNew)
	}
	return result
}

private formatPrimaryStatusNumber(val) {
	def unit
	["% LIGHT", "% RH", " LUX", "°F", "°C"].each {
		if ("${val}".contains(it)) {
			unit = "${it}"
		}
	}
	
	if (unit) {
		def numericVal = safeToDec("${val}".replace("${unit}", ""))
		return "${Math.round(numericVal)}${unit}"
	}
	else {
		return val
	}
}

private getSecondaryStatus(eventMaps) {
	def status = ""
	if (secondaryTileStatusSetting == "combined"){
		def motionStatus = getAttrStatusText("motion", eventMaps)
		def lightStatus = getAttrStatusText("lxLight", eventMaps)
		def tempStatus = getAttrStatusText("temperature", eventMaps)
		def humidityStatus = getAttrStatusText("humidity", eventMaps)
		status = "${motionStatus} / ${tempStatus} / ${humidityStatus} / ${lightStatus}"
	}
	else if (status != "none") {
		status = getAttrStatusText(secondaryTileStatusSetting, eventMaps)
	}
	return status
}

private getAttrStatusText(attrName, eventMaps=null) {
	def status = (eventMaps?.find { it.name == attrName }?.descriptionText)
	if (status) {
		return status
	}
	else {
		return getDescriptionText(device.currentState(attrName))
	}	
}

private getDescriptionText(data) {
	switch (data?.name ?: "") {
		case "motion":
			return "${data.value}"
			break
		case "temperature":
			return "${data.value}°${data.unit}"					
			break
		case "humidity":
			return  "${data.value}% RH"
			break
		case "lxLight":
			return "${data.value} LUX"
		case "pLight":
			return "${data.value}% LIGHT"
			break
		default:
			return ""
	}	
}


def zwaveEvent(physicalgraph.zwave.Command cmd) {
	logDebug "Unhandled Command: $cmd"
	return []
}


// Resets the tamper attribute to clear and requests the device to be refreshed.
def refresh() {	
	if (getAttrValue("tamper") != "clear") {
		sendEvent(createTamperEventMap("clear"))		
	}
	else {
		logForceWakeupMessage("The sensor data will be refreshed the next time the device wakes up.")
		state.pendingRefresh = true
	}
	return null
}

private createTamperEventMap(val) {
	return createEventMap("tamper", val)
}

private logForceWakeupMessage(msg) {
	logDebug "${msg}  You can force the device to wake up immediately by using a paper clip to push the button on the bottom of the device."
}

private createEventMaps(eventName, newVal, unit, displayed, onlyIfNew) {
	def result = []
	if (!onlyIfNew || getAttrValue(eventName) != newVal) {
		def eventMap = createEventMap(eventName, newVal, unit, displayed)
		def desc = getDescriptionText(eventMap)
		if (desc) {
			eventMap.descriptionText = desc
		}
		result << eventMap
	}
	return result
}

private createEventMap(eventName, newVal, unit="", displayed=null) {
	def oldVal = getAttrValue(eventName)
	def isNew = "${oldVal}" != "${newVal}"
	def desc = "${eventName.capitalize()} is ${newVal}${unit}"
	
	if (displayed == null) {
		displayed = isNew
	}
	
	if (displayed) {
		logDebug "${desc}"
	}
	else {
		logTrace "${desc}"
	}
	
	return [
		name: eventName, 
		value: newVal, 
		displayed: displayed,
		isStateChange: true,
		unit: unit,
		descriptionText: "${device.displayName}: ${desc}"
	]
}

private getAttrValue(attrName) {
	try {
		return device?.currentValue("${attrName}")
	}
	catch (ex) {
		logTrace "$ex"
		return null
	}
}

private wakeUpIntervalSetCmd(val) {
	logTrace "wakeUpIntervalSetCmd(${val})"
	return secureCmd(zwave.wakeUpV2.wakeUpIntervalSet(seconds:val, nodeid:zwaveHubNodeId))
}

private wakeUpNoMoreInfoCmd() {
	return secureCmd(zwave.wakeUpV2.wakeUpNoMoreInformation())
}

private batteryGetCmd() {
	return secureCmd(zwave.batteryV1.batteryGet())
}

private manufacturerSpecificGetCmd() {
	return secureCmd(zwave.manufacturerSpecificV2.manufacturerSpecificGet())
}

private versionGetCmd() {
	return secureCmd(zwave.versionV1.versionGet())
}

private basicGetCmd() {
	return secureCmd(zwave.basicV1.basicGet())
}

private sensorMultilevelGetCmd(sensorType) {
	return secureCmd(zwave.sensorMultilevelV5.sensorMultilevelGet(scale: 2, sensorType: sensorType))
}

private configSetCmds(paramNumber, val) {	
	logTrace "Setting config param #${paramNumber} to ${val}"
	return [
		secureCmd(zwave.configurationV1.configurationSet(parameterNumber: paramNumber, size: 1, configurationValue: [val])),
		configGetCmd(paramNumber)
	]
}

private configSetCmd(paramNumber, val) {	
	logTrace "Setting config param #${paramNumber} to ${val}"
	return secureCmd(zwave.configurationV1.configurationSet(parameterNumber: paramNumber, size: 1, configurationValue: [val]))	
}

private configGetCmd(paramNumber) {
	return secureCmd(zwave.configurationV1.configurationGet(parameterNumber: paramNumber))
}

private secureCmd(cmd) {
	if (state.useSecureCmds == false) {
		return cmd.format()
	}
	else {
		return zwave.securityV1.securityMessageEncapsulation().encapsulate(cmd).format()
	}
}

private safeToInt(val, defaultVal=0) {
	return "${val}"?.isInteger() ? "${val}".toInteger() : defaultVal
}

private safeToDec(val, defaultVal=0) {
	return "${val}"?.isBigDecimal() ? "${val}".toBigDecimal() : defaultVal
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
	if (debugOutputSetting) {
		log.debug "$msg"
	}
}

private logTrace(msg) {
	// log.trace "$msg"
}