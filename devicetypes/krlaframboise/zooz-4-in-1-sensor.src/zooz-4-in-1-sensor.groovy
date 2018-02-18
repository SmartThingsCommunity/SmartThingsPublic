/**
 *  Zooz 4-in-1 Sensor v1.0
 *		(Model: ZSE40)
 *
 *  Author: 
 *    Kevin LaFramboise (krlaframboise)
 *
 *  URL to documentation:  https://community.smartthings.com/t/release-zooz-4-in-1-sensor/82989?u=krlaframboise
 *    
 *
 *  Changelog:
 *
 *    1.0 (12/16/2017)
 *    	- Initial Release
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
		name: "Zooz 4-in-1 Sensor", 
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
		
		attribute "pendingChanges", "number"
		
		attribute "primaryStatus", "string"
		attribute "secondaryStatus", "string"
		attribute "pLight", "number"
		attribute "lxLight", "number"
		attribute "firmwareVersion", "string"

		// Firmware 16.9 & 17.9
		fingerprint mfr:"027A", prod:"2021", model:"2101", deviceJoinName: "Zooz 4-in-1 Sensor"
		
		// Firmware 5.1
		fingerprint mfr:"0109", prod:"2021", model:"2101", deviceJoinName: "Zooz 4-in-1 Sensor"
	}
	
	simulator { }
	
	preferences {
		input "primaryTileStatus", "enum",
			title: "Primary Status:",
			defaultValue: primaryTileStatusSetting,
			required: false,
			options: primaryStatusOptions

		getBoolInput("roundPrimaryStatus", "Round the Primary Status to a whole number?", false)

		input "secondaryTileStatus", "enum",
			title: "Secondary Status:",
			defaultValue: secondaryTileStatusSetting,
			required: false,
			options: secondaryStatusOptions		

		getParamInput(tempScaleParam)
		getParamInput(tempTriggerParam)
		getNumberInput("tempOffset", "Temperature Offset [-25 to 25]\n(0 = No Offset)\n(-1 = Subtract 1°)\n(1 = Add 1°)", "-25..25", tempOffsetSetting)
		getParamInput(humidityTriggerParam)	
		getNumberInput("humidityOffset", "Humidity % Offset [-25 to 25]\n(0 = No Offset)\n(-1 = Subtract 1%)\n(1 = Add 1%)", "-25..25", humidityOffsetSetting)
		getParamInput(lightTriggerParam)
		getNumberInput("lightOffset", "Light % Offset [-25 to 25]\n(0 = No Offset)\n(-1 = Subtract 1%)\n(1 = Add 1%)", "-25..25", lightOffsetSetting)
		getNumberInput("lxLightOffset", "Light Lux Offset [-25 to 25]\n(0 = No Offset)\n(-1 = Subtract 1 lx)\n(1 = Add 1 lx)", "-25..25", lxLightOffsetSetting)
		getBoolInput("reportLx", "Report Illuminance as Lux?\n(When enabled, a calculated lux level will be used for illuminance instead of the default %.)", reportLxSetting)
		getNumberInput("maxLx", "Lux value to report when light level is at 100%:", "0..5000", maxLxSetting)
		getParamInput(motionTimeParam)
		getParamInput(motionSensitivityParam)
		getParamInput(ledIndicatorModeParam)
		getNumberInput("checkinInterval", "Minimum Check-in Interval [0-167]\n(0 = 10 Minutes [FOR TESTING ONLY])\n(1 = 1 Hour)\n(167 = 7 Days)", "0..167", checkinIntervalSetting)
		getNumberInput("reportBatteryEvery", "Battery Reporting Interval [1-167]\n(1 = 1 Hour)\n(167 = 7 Days)\nThis setting can't be less than the Minimum Check-in Interval.", "1..67", batteryReportingIntervalSetting)
		getBoolInput("autoClearTamper", "Automatically Clear Tamper?\n(The tamper detected event is raised when the device is opened.  This setting allows you to decide whether or not to have the clear event automatically raised when the device closes.)", false)
		getBoolInput("debugOutput", "Enable debug logging?", true)
	}

	tiles(scale: 2) {
		multiAttributeTile(name:"mainTile", type: "generic", width: 6, height: 4){
			tileAttribute ("device.primaryStatus", key: "PRIMARY_CONTROL") {
				attributeState "primaryStatus", 
					label:'${currentValue}', 
					icon:"st.motion.motion.inactive",
					backgroundColor:"#ffffff"			
				attributeState "inactive", 
					label:'NO MOTION', 
					icon:"st.motion.motion.inactive", 
					backgroundColor:"#ffffff"
				attributeState "active", 
					label:'MOTION', 
					icon:"st.motion.motion.active", 
					backgroundColor:"#00a0dc"
			}
			tileAttribute ("device.secondaryStatus", key: "SECONDARY_CONTROL") {
				attributeState "default", label:'${currentValue}'
				attributeState "inactive", label:'NO MOTION'
				attributeState "active", label:'MOTION'
			}
		}
		
		valueTile("temperature", "device.temperature", inactiveLabel: false, width: 2, height: 2) {
			state "temperature", label:'${currentValue}°',
			backgroundColors:[
				[value: 31, color: "#153591"],
				[value: 44, color: "#1e9cbb"],
				[value: 59, color: "#90d2a7"],
				[value: 74, color: "#44b621"],
				[value: 84, color: "#f1d801"],
				[value: 95, color: "#d04e00"],
				[value: 96, color: "#bc2323"]
			]
		}
		
		valueTile("humidity", "device.humidity", decoration: "flat", inactiveLabel: false, width: 2, height: 2){
			state "humidity", label:'${currentValue}% \nRH', unit:""
		}
		
		valueTile("pLight", "device.pLight", decoration: "flat", inactiveLabel: false, width: 2, height: 2){
			state "pLight", label:'${currentValue}% \nLight', unit: ""
		}

		valueTile("lxLight", "device.lxLight", decoration: "flat", width: 2, height: 2){
			state "lxLight", label:'${currentValue}lx \nLight', unit: ""
		}
		
		valueTile("motion", "device.motion", width: 2, height: 2){
			state "inactive", label:'No \nMotion', backgroundColor:"#ffffff"
			state "active", label:'Motion', backgroundColor:"#00a0dc"
		}
		
		valueTile("tampering", "device.tamper", width: 2, height: 2) {			
			state "clear", label:'Tamper \nClear', backgroundColor:"#ffffff"
			state "detected", label:'Tamper \nDetected', backgroundColor: "#e86d13"
		}
		
		valueTile("battery", "device.battery", decoration: "flat", inactiveLabel: false, width: 2, height: 2){
			state "default", label:'${currentValue}% \nBattery', unit: ""
		}
			
		
		valueTile("pending", "device.pendingChanges", decoration: "flat", width: 2, height: 2){
			state "pendingChanges", label:'${currentValue} Change(s) Pending'
			state "0", label: ''
			state "-1", label:'Updating Settings'
		}
		
		valueTile("lastUpdate", "device.lastUpdate", decoration: "flat", inactiveLabel:false, width: 2, height: 2){
			state "lastUpdate", label:'Settings\nUpdated\n\n${currentValue}'
		}
		
		valueTile("firmwareVersion", "device.firmwareVersion", decoration: "flat", inactiveLabel:false, width: 2, height: 2){
			state "firmwareVersion", label:'Firmware \n${currentValue}'
		}
		
		standardTile("refresh", "device.refresh", inactiveLabel: false, width: 2, height: 2) {
			state "default", label: "Refresh", action: "refresh", icon:"st.secondary.refresh-icon"
		}
		
		main("mainTile")
		details(["mainTile", "humidity", "temperature", "lxLight", "battery", "pLight", "motion", "tampering", "firmwareVersion", "lastUpdate", "refresh","pending"])
	}
}

private getNumberInput(name, title, range, defaultVal) {	
	input "${name}", "number", 
		title: "${title}", 
		range: "${range}",
		defaultValue: defaultVal, 
		required: false
}

private getBoolInput(name, title, defaultVal) {
	input "${name}", "bool", 
		title: "${title}", 
		defaultValue: defaultVal, 
		required: false
}

private getParamInput(param) {	
	input "${param.prefName}", "number",
		title: "${param.name}:",
		defaultValue: "${param.val}",
		required: false,
		displayDuringSetup: true,
		range: "${param.range}"
}

def updated() {	
	// This method always gets called twice when preferences are saved.
	if (!isDuplicateCommand(state.lastUpdated, 3000)) {
		state.lastUpdated = new Date().time
		logTrace "updated()"
	
		initializeOffsets()
					
		if (!getAttrValue("tamper")) {
			sendEvent(createTamperEventMap("clear"))
		}

		if (checkForPendingChanges()) {
			logForceWakeupMessage("The configuration will be updated the next time the device wakes up.")
		}		
	}	
}

private checkForPendingChanges() {
	def changes = 0
	configParams.each {
		if (hasPendingChange(it)) {		
			changes += 1
		}
	}
	if (checkinIntervalChanged) {
		changes += 1
	}
	if (changes != getAttrValue("pendingChanges")) {
		sendEvent(createEventMap("pendingChanges", changes, "", false))
	}
	return (changes != 0)
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
		sendEvent(name: "primaryStatus", value: "inactive", displayed: false)
		cmds << versionGetCmd()
	}
	
	if (state.pendingRefresh != false || !allAttributesHaveValues()) {
		state.pendingRefresh = false
		cmds += refreshSensorData()
	}
	else if (canReportBattery()) {
		cmds << batteryGetCmd()
	}
			
	configParams.each { param ->
		cmds += updateConfigVal(param)
	}	
	
	if (checkinIntervalChanged) {
		logTrace "Updating wakeup interval"
		cmds << wakeUpIntervalSetCmd(checkinIntervalSettingSeconds)
		cmds << wakeUpIntervalGetCmd()
	}
	
	return cmds ? delayBetween(cmds, 50) : []	
}

private allAttributesHaveValues() {
	return (getAttrValue("temperature") != null && getAttrValue("humidity") != null && getAttrValue("illuminance") != null && getAttrValue("battery") != null)
}

private updateConfigVal(param) {
	def result = []	
	if (hasPendingChange(param)) {	
		def newVal = param.val
		logDebug "${param.prefName}(#${param.num}): changing ${getParamStoredVal(param)} to ${newVal}"
		result << configSetCmd(param, newVal)
		result << configGetCmd(param)
	}		
	return result
}

private getCheckinIntervalChanged() {
	return (state.checkinInterval != checkinIntervalSettingSeconds)
}

private hasPendingChange(param) {
	
	if ((param.num != ledIndicatorModeParam.num || ledIndicatorModeMatchesFirmware(param.val)) && (param.num != motionTimeParam.num || motionTimeMatchesFirmware(param.val))) {
		return (param.val != getParamStoredVal(param) || state.refreshAll)
	}
	else {
		return false
	}
}

private ledIndicatorModeMatchesFirmware(val) {
	if (firmwareVersion >= firmwareV2 || val != 4) {
		return true
	}
	else {
		log.warn "LED Indicator Mode #4 is only available in firmware ${firmwareV2} and above."
		return false
	}
}

private motionTimeMatchesFirmware(val) {
	if (firmwareVersion < firmwareV3 || (val >= 15 && val <= 60)) {
		return true
	}
	else {
		log.warn "${val} Seconds is not a valid Motion Time for Firmware ${firmwareV3}."
		return false
	}
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
private getTempOffsetSetting() {
	return safeToInt(settings?.tempOffset, 0)
}
private getHumidityOffsetSetting() {
	return safeToInt(settings?.humidityOffset, 0)
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
private getCheckinIntervalSetting() {
	return (safeToInt(settings?.checkinInterval, (firmwareVersion >= firmwareV2 ? 12 : 6)))
}
private getCheckinIntervalSettingSeconds() {
	if (checkinIntervalSetting == 0) {
		return (10 * 60 * 60)
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
		desc = "${desc}\n(${it.value} = ${it.name})"
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
		[name: "Temperature Off / Motion Flash [ONLY FIRMWARE ${firmwareV2} AND ABOVE]", value: 4]
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

private getFirmwareVersion() {
	return safeToDec(getAttrValue("firmwareVersion"), 0.0)
}

private getFirmwareV1() { return 5.1 }
private getFirmwareV2() { return 16.9 }
private getFirmwareV3() { return 17.9 }


// Sensor Types
private getTempSensorType() { return 1 }
private getHumiditySensorType() { return 5 }
private getLightSensorType() { return 3 }

// Configuration Parameters
private getConfigParams() {
	return [		
		tempScaleParam,
		tempTriggerParam,
		humidityTriggerParam,
		lightTriggerParam,
		motionTimeParam,
		motionSensitivityParam,
		ledIndicatorModeParam
	]
}

private getTempScaleParam() {
	return createConfigParamMap(1, "Temperature Scale [0-1]${getNameValueSettingDesc(tempUnits)}", 1, "tempScale", "0..1", (firmwareVersion >= firmwareV2 ? 1 : 0))
}

private getTempTriggerParam() {
	return createConfigParamMap(2, "Temperature Change Trigger [1-50]\n(1 = 0.1°)\n(50 = 5.0°)", 1, "tempTrigger", "1..50", 10)
}

private getHumidityTriggerParam() {
	return createConfigParamMap(3, "Humidity Change Trigger [1-50]\n(1% - 50%)", 1, "humidityTrigger", "1..50", 10)
}

private getLightTriggerParam() {
	return createConfigParamMap(4, "Light Change Trigger [5-50]\n(5% - 50%)", 1, "lightTrigger", "5..50", 10)
}

private getMotionTimeParam() {	
	return createConfigParamMap(5, "Motion Retrigger Time [1-255 or 15-60]\n(1 Minute - 255 Minutes [FIRMWARE ${firmwareV1} & ${firmwareV2}])\n(15 Seconds - 60 Seconds [FIRMWARE ${firmwareV3}])", 1, "motionTime", "1..255", 15)
}

private getMotionSensitivityParam() {
	return createConfigParamMap(6, "Motion Sensitivity [1-7]\n(1 = Most Sensitive)\n(7 = Least Sensitive)", 1, "motionSensitivity", "1..7", 4)
}

private getLedIndicatorModeParam() {	
	return createConfigParamMap(7, "LED Indicator Mode [1-4]${getNameValueSettingDesc(ledIndicatorModes)}", 1, "ledIndicatorMode", "1..4", 3)
}


private getParamStoredVal(param) {
	return state["configVal${param.num}"]	
}

private createConfigParamMap(num, name, size, prefName, range, val) {
	if (settings?."${prefName}" != null) {
		val = settings?."${prefName}"
	}
	return [
		num: num, 
		name: name, 
		size: size, 
		prefName: prefName,
		range: range,
		val: val
	]
}

def parse(String description) {
	def result = []
	def cmd = zwave.parse(description, commandClassVersions)
	if (cmd) {
		result += zwaveEvent(cmd)
	}
	else {
		logDebug "Unable to parse description: $description"
	}
	return result
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

private getCommandClassVersions() {
	[
		0x20: 1,  // Basic
		0x31: 5,	// Sensor Multilevel (v7)
		0x59: 1,  // AssociationGrpInfo
		0x5A: 1,  // DeviceResetLocally
		0x5E: 2,  // ZwaveplusInfo
		0x70: 2,  // Configuration
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

def zwaveEvent(physicalgraph.zwave.commands.wakeupv2.WakeUpIntervalReport cmd) {
	state.checkinInterval = cmd.seconds
	
	sendUpdatingEvent()
	
	def msg = "Minimum Check-in Interval is ${cmd.seconds / 60} Minutes"
	if (cmd.seconds == 600) {
		log.warn "$msg"
	}
	else {
		logDebug "$msg"
	}
	
	// Set the Health Check interval so that it reports offline 5 minutes after it's missed 2 checkins.
	def val = ((cmd.seconds * 2) + (5 * 60))
	
	def eventMap = createEventMap("checkInterval", val, "", false)

	eventMap.data = [protocol: "zwave", hubHardwareId: device.hub.hardwareID]
	
	runIn(5, finalizeConfiguration)
	
	return [ createEvent(eventMap) ]
}

def zwaveEvent(physicalgraph.zwave.commands.wakeupv2.WakeUpNotification cmd) {
	logTrace "WakeUpNotification: $cmd"
	def cmds = []
	
	sendLastCheckinEvent()
	
	cmds += configure()
		
	if (cmds) {
		cmds << "delay 1200"
	}
	
	cmds << wakeUpNoMoreInfoCmd()
	return response(cmds)
}

private sendLastCheckinEvent() {
	if (!isDuplicateCommand(state.lastCheckinTime, 60000)) {
		state.lastCheckinTime = new Date().time			

		logDebug "Device Checked In"
		sendEvent(createEventMap("lastCheckin", convertToLocalTimeString(new Date()), "", false))
	}
}

private canReportBattery() {
	def reportEveryMS = (batteryReportingIntervalSettingSeconds * 1000)
		
	return (!state.lastBatteryReport || ((new Date().time) - state.lastBatteryReport > reportEveryMS)) 
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
	logTrace "VersionReport: ${cmd}"
	
	def version = "${cmd.applicationVersion}.${cmd.applicationSubVersion}"
	logDebug "Firmware Version: ${version}"
	
	def result = []
	if (getAttrValue("firmwareVersion") != "${version}") {
		result << createEvent(name: "firmwareVersion", value: "${version}", displayed: false)
	}
	return result 
}

def zwaveEvent(physicalgraph.zwave.commands.configurationv2.ConfigurationReport cmd) {	
	// logTrace "ConfigurationReport: ${cmd}"
	sendUpdatingEvent()
	
	def val = cmd.configurationValue[0]
		
	def configParam = configParams.find { param ->
		param.num == cmd.parameterNumber
	}
	
	if (configParam) {
		logDebug "${configParam.name}(#${configParam.num}) = ${val}"
		state["configVal${cmd.parameterNumber}"] = val
	}	
	else {
		logDebug "Parameter ${cmd.parameterNumber} = ${val}"
	}
	
	runIn(5, finalizeConfiguration)
	return []
}

private sendUpdatingEvent() {
	if (getAttrValue("pendingChanges") != -1) {
		sendEvent(createEventMap("pendingChanges", -1, "", false))
	}
}

def finalizeConfiguration() {
	logTrace "finalizeConfiguration()"
	state.refreshAll = false
	
	checkForPendingChanges()
	
	sendEvent(createEventMap("lastUpdate", convertToLocalTimeString(new Date()), "", false))
	return []
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd) {
	logTrace "BasicReport: $cmd"	
	return handleMotionEvent(cmd.value)
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicSet cmd) {
	// logTrace "Basic Set: $cmd"	
	return handleMotionEvent(cmd.value)
}

private handleMotionEvent(val) {
	def motionVal = (val == 0xFF ? "active" : "inactive")
	
	logTrace "Motion ${motionVal}"
	
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
	// logTrace "NotificationReport: $cmd"
	def result = []	
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
	// logTrace "SensorMultilevelReport: ${cmd}"
	
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


def refresh() {	
	if (device.currentValue("tamper") != "clear") {
		logDebug "Clearing Tamper"
		sendEvent(createTamperEventMap("clear"))
	}
	else if (state.pendingRefresh) {	
		sendEvent(createEventMap("pendingChanges", configParams.size(), "", false))			
		state.refreshAll = true		
		logForceWakeupMessage "All configuration settings will be sent to the device and its data will be refreshed the next time it wakes up."
	}
	else {
		state.pendingRefresh = true
		logForceWakeupMessage "The sensor data will be refreshed the next time the device wakes up."
	}
	return []
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

private wakeUpIntervalGetCmd() {
	return secureCmd(zwave.wakeUpV2.wakeUpIntervalGet())
}

private wakeUpIntervalSetCmd(val) {
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

private configSetCmd(param, val) {
	return secureCmd(zwave.configurationV2.configurationSet(parameterNumber: param.num, size: param.size, configurationValue: [val]))
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


private safeToInt(val, defaultVal=0) {
	return "${val}"?.isInteger() ? "${val}".toInteger() : defaultVal
}

private safeToDec(val, defaultVal=0) {
	return "${val}"?.isBigDecimal() ? "${val}".toBigDecimal() : defaultVal
}

private convertToLocalTimeString(dt) {
	def timeZoneId = location?.timeZone?.ID
	def localDt = "$dt"
	try {
		if (timeZoneId) {
			localDt = dt.format("MM/dd/yyyy hh:mm:ss a", TimeZone.getTimeZone(timeZoneId))
		}
	}
	catch (e) {
		// Hub TimeZone probably not set.
	}
	return localDt
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