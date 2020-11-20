/*
 *  Sensative Strips Drip 1.0.1
 *  (Model: Strips-TpAiZw)
 *
 *  Author: 
 *    Kevin LaFramboise (krlaframboise)
 *
 *  URL to Documentation: https://community.smartthings.com/t/release-strips-drip-strips-comfort/135276?u=krlaframboise
 *
 *  Changelog:
 *
 *    1.0.1 (10/09/2020)
 *      - bug fixes / requested changes
 *
 *    1.0 (10/04/2020)
 *      - Initial Release
 *
 *
 *  Copyright 2020 Kevin LaFramboise
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
*/
metadata {
	definition (
		name: "Sensative Strips Drip", 
		namespace: "krlaframboise", 
		author: "Kevin LaFramboise",
		ocfDeviceType:"x.com.st.d.sensor.moisture",
		mnmn: "SmartThingsCommunity",
		vid: "8871b8d9-1851-390f-b791-741f5f34c993"
	) {
		capability "Sensor"
		capability "Configuration"
		capability "Illuminance Measurement"
		capability "Water Sensor"
		capability "Temperature Measurement"
		capability "Battery"
		capability "Refresh"
		capability "Health Check"

		attribute "lastCheckIn", "string"
		attribute "lastUpdate", "string"

		attribute "pendingChanges", "number"
		attribute "firmwareVersion", "string"

		fingerprint mfr:"019A", prod:"0003", model:"000A", deviceJoinName: "Strips Drip"
	}

	simulator { }

	tiles(scale: 2) {
		multiAttributeTile(name:"mainTile", type: "generic", width: 6, height: 4){
			tileAttribute ("device.water", key: "PRIMARY_CONTROL") {
				attributeState "Dry", 
					label:'DRY', 
					icon:"st.alarm.water.dry", 
					backgroundColor:"#ffffff"
				attributeState "Wet", 
					label:'WET', 
					icon:"st.alarm.water.wet", 
					backgroundColor:"#00a0dc"
			}
		}

		valueTile("temperature", "device.temperature", width: 2, height: 2) {
			state "temperature", label:'${currentValue}°'
		}

		valueTile("illuminance", "device.illuminance", width: 2, height: 2){
			state "default", label:'${currentValue}lx'
		}

		standardTile("water", "device.water", width: 2, height: 2){
			state "dry", label:'Dry', icon: "st.alarm.water.dry"
			state "wet", label:'Wet', icon: "st.alarm.water.wet"
		}

		valueTile("battery", "device.battery", width: 2, height: 2){
			state "default", label:'${currentValue}%'
			state "1", label:'${currentValue}%'
		}


		valueTile("pending", "device.pendingChanges", decoration: "flat", width: 2, height: 2){
			state "pendingChanges", label:'${currentValue} Change(s) Pending'
			state "0", label: 'No Pending Changes'
			state "-1", label:'Updating Settings'
			state "-2", label:'Refresh Pending'
			state "-3", label:'Refreshing'
		}

		valueTile("lastUpdate", "device.lastUpdate", decoration: "flat", width: 2, height: 2){
			state "lastUpdate", label:'Settings\nUpdated\n\n${currentValue}'
		}

		valueTile("lastActivity", "device.lastCheckIn", decoration: "flat", width: 2, height: 2){
			state "lastCheckIn", label:'Last\nActivity\n\n${currentValue}'
		}

		valueTile("firmwareVersion", "device.firmwareVersion", decoration: "flat", width: 2, height: 2){
			state "firmwareVersion", label:'Firmware \n${currentValue}'
		}

		standardTile("refresh", "device.refresh", width: 2, height: 2, decoration: "flat") {
			state "default", label: "Refresh", action: "refresh"
		}

		main("mainTile")
		details(["mainTile", "temperature", "illuminance", "battery", "water", "refresh","pending", "firmwareVersion", "lastActivity", "lastUpdate"])
	}

	preferences {
		[
			ledAlarmParam,
			leakageAlarmParam,
			leakageLevelParam,
			leakageAlarmIntervalParam,
			tempLightReportingIntervalParam,
			tempReportingParam,
			lightReportingParam,
			highAmbientLightParam,
			lowAmbientLightParam
		].each {
			getOptionsInput(it)
		}

		input "debugOutput", "bool", 
			title: "Enable debug logging?", 
			defaultValue: true, 
			required: false
	}
}

private getOptionsInput(param) {
	input "configParam${param.num}", "enum",
		title: "${param.name}:",
		required: false,
		defaultValue: param.value.toString(),
		options: param.options
}


def updated() {
	// This method always gets called twice when preferences are saved.
	if (!isDuplicateCommand(state.lastUpdated, 3000)) {
		state.lastUpdated = new Date().time
		logTrace "updated()"

		if (!device.currentValue("checkInterval")) {
			sendEvent(name: "checkInterval", value: ((defaultWakeUpIntervalSeconds * 2) + (5 * 60)), displayed: falsle, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])
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

	if (state.wakeUpInterval != defaultWakeUpIntervalSeconds) {
		changes += 1
	}

	if (changes != getAttrValue("pendingChanges")) {
		sendEvent(createEventMap("pendingChanges", changes, "", false))
	}
	return (changes != 0)
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
		cmds << versionGetCmd()
	}

	if (state.wakeUpInterval != defaultWakeUpIntervalSeconds) {
		cmds << wakeUpIntervalSetCmd(defaultWakeUpIntervalSeconds)
		cmds << wakeUpIntervalGetCmd()
	}

	if (!device.currentValue("water")) {
		sendEvent(name: "water", value: "dry")
	}

	if (state.pendingRefresh != false || state.refreshAll || !allAttributesHaveValues()) {
		runIn(5, finalizeConfiguration)
		sendEvent(createEventMap("pendingChanges", -3, "", false))

		cmds += [
			sensorMultilevelGetCmd(tempSensorType),
			sensorMultilevelGetCmd(lightSensorType)
		]
	}

	cmds << batteryGetCmd()

	if (state.configured != true) {
		configParams.each { param ->
			cmds << configGetCmd(param)
		}
	}
	else {
		configParams.each { param ->
			cmds += updateConfigVal(param)
		}
	}

	return cmds ? delayBetween(cmds, 500) : []
}

private allAttributesHaveValues() {
	return (getAttrValue("temperature") != null && 
		getAttrValue("water") != null && 
		getAttrValue("illuminance") != null && 
		getAttrValue("battery") != null)
}

private updateConfigVal(param) {
	def result = []
	if (hasPendingChange(param) || state.refreshAll) {
		logDebug "${param.name}(#${param.num}): changing ${getParamStoredVal(param)} to ${param.value}"
		result << configSetCmd(param, param.value)
		result << configGetCmd(param)
	}
	return result
}

private hasPendingChange(param) {
	return (param.value != getParamStoredVal(param))
}

private getParamStoredVal(param) {
	return state["configVal${param.num}"]
}

// Required for HealthCheck Capability, but doesn't actually do anything because this device sleeps.
def ping() {
	logDebug "ping()"
}


private getDebugOutputSetting() {
	return (settings?.debugOutput || settings?.debugOutput == null)
}


// Sensor Types
private getTempSensorType() { return 1 }
private getLightSensorType() { return 3 }
private getWaterSensorType() { return 31 }

// Configuration Parameters
private getConfigParams() {
	return [
		ledAlarmParam,
		tempLightReportingIntervalParam,
		tempReportingParam,
		lightReportingParam,
		highAmbientLightParam,
		lowAmbientLightParam,
		leakageAlarmParam,
		leakageLevelParam,
		leakageAlarmIntervalParam
	]
}


private getLedAlarmParam() {
	return getParam(2, "LED Alarm Event Reporting", 1, 1, enabledDisabledOptions)
}

private getTempLightReportingIntervalParam() {
	return getParam(3, "Temperature & Light Reporting Frequency", 1, 1, ["1":"Normal", "2":"Frequent"])
}

private getTempReportingParam() {
	return getParam(4, "Temperature Reporting", 1, 1, enabledDisabledOptions)
}

private getLightReportingParam() {
	return getParam(9, "Ambient Light Reporting", 1, 1, ["0":"Off", "1":"On", "2":"Report Only When Selected High/Low Ambient Light Levels are Passed"])
}

private getHighAmbientLightParam() {
	return getParam(10, "High Ambient Light Report Level", 4, 40000, getAmbientLightOptions(3, 64000))
}

private getLowAmbientLightParam() {
	return getParam(11, "Low Ambient Light Report Level", 4, 5000, getAmbientLightOptions(1, 42000))
}

private getLeakageAlarmParam() {
	return getParam(12, "Leakage Alarm", 1, 1, enabledDisabledOptions)
}

private getLeakageLevelParam() {
	return getParam(13, "Leakage Alarm Level", 1, 10, [10:"10%", 25:"25%", 50:"50%", 75:"75%", 100:"100%"])
}

private getLeakageAlarmIntervalParam() {
	return getParam(14, "Moisture Reporting Period", 1, 0, moistureIntervalOptions)
}


private getParam(num, name, size, defaultVal, options) {
	def val = safeToInt((settings ? settings["configParam${num}"] : null), defaultVal) 

	return [num: num, name: name, size: size, value: val, options: setDefaultOption(options, defaultVal)]
}

private setDefaultOption(options, defaultVal) {
	return options?.collectEntries { k, v ->
		if ("${k}" == "${defaultVal}") {
			v = "${v} [DEFAULT]"
		}
		["$k": "$v"]
	}
}


// Setting Options
private getEnabledDisabledOptions() {
	 return [
		"0":"Off", 
		"1":"On"
	]
}

private getMoistureIntervalOptions() {
	def options = ["0":"Off"]

	[1,2,3,4,5,10,12,18].each {
		options["${it}"] = "${it} Hour${it == 1 ? '' : 's'}"
	}

	(1..5).each {
		options["${it * 24}"] = "${it} Day${it == 1 ? '' : 's'}"
	}
	return options
}

private getAmbientLightOptions(minVal, maxVal) {
	def options = [:]

	[1,2,3,4,5,10,25,50,100,250,500,750,1000,1500,2000,3000,4000,5000].each {
		if ((it >= minVal) && (it <= maxVal)) {
			options[it.toString()] = String.format("%,d", it) + " lux"
		}
	}

	(3..32).each {
		int val = (it * 2000)
		if ((val >= minVal) && (val <= maxVal)) {
			options[val.toString()] = String.format("%,d", val) + " lux"
		}
	}

	return options
}


def parse(String description) {
	def result = []

	sendLastCheckInEvent()

	def cmd = zwave.parse(description, commandClassVersions)
	if (cmd) {
		result += zwaveEvent(cmd)
	}
	else {
		logDebug "Unable to parse description: $description"
	}
	return result
}

private sendLastCheckInEvent() {
	if (!isDuplicateCommand(state.lastCheckInTime, 60000)) {
		state.lastCheckInTime = new Date().time

		sendEvent(createEventMap("lastCheckIn", convertToLocalTimeString(new Date()), "", false))
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
	logDebug "$cmd"

	state.wakeUpInterval = cmd.seconds

	return []
}


def zwaveEvent(physicalgraph.zwave.commands.wakeupv2.WakeUpNotification cmd) {
	logTrace "WakeUpNotification: $cmd"
	def cmds = []

	logDebug "Device Woke Up"

	cmds += configure()

	if (cmds) {
		cmds << "delay 2000"
	}

	cmds << wakeUpNoMoreInfoCmd()
	return response(cmds)
}


def zwaveEvent(physicalgraph.zwave.commands.batteryv1.BatteryReport cmd) {
	def val = (cmd.batteryLevel == 0xFF ? 1 : cmd.batteryLevel)
	if (val > 100) {
		val = 100
	}
	else if (val < 1) {
		val = 1
	}

	sendEvent(createEventMap("battery", val, "%", true))
	return []
}


def zwaveEvent(physicalgraph.zwave.commands.manufacturerspecificv2.ManufacturerSpecificReport cmd) {
	logTrace "ManufacturerSpecificReport: ${cmd}"
	return []
}


def zwaveEvent(physicalgraph.zwave.commands.versionv1.VersionReport cmd) {
	String subVersion = String.format("%02d", cmd.applicationSubVersion)
	String fullVersion = "${cmd.applicationVersion}.${subVersion}"

	logDebug "Firmware Version: ${fullVersion}"

	sendEvent(name: "firmwareVersion", value: fullVersion, displayed: false)
	return []
}


def zwaveEvent(physicalgraph.zwave.commands.configurationv2.ConfigurationReport cmd) {
	logTrace "ConfigurationReport: ${cmd}"
	sendUpdatingEvent()

	def val = cmd.scaledConfigurationValue

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
	state.pendingRefresh = false
	state.configured = true

	checkForPendingChanges()

	sendEvent(createEventMap("lastUpdate", convertToLocalTimeString(new Date()), "", false))
	return []
}


def zwaveEvent(physicalgraph.zwave.commands.notificationv3.NotificationReport cmd) {
	logTrace "NotificationReport: $cmd"

	def cmds = []
	if (cmd.notificationType == 7) {
		if (cmd.event == 4 && cmd.notificationStatus == 255) {
			logDebug "Device Woke Up"
			cmds += configure()
			cmds << "delay 2000"
			cmds << wakeUpNoMoreInfoCmd()
		}
	}
	else if (cmd.notificationType == 5) {
		sendWaterEvent(cmd.event)
	}
	return cmds ? response(cmds) : []
}

private sendWaterEvent(rawVal) {
	def val = (rawVal ? "wet" : "dry")
	def desc = "${device.displayName} water is ${val}"

	logDebug "${desc}"

	sendEvent(name: "water", value: val, descriptionText: desc)
}


def zwaveEvent(physicalgraph.zwave.commands.sensormultilevelv5.SensorMultilevelReport cmd) {
	logTrace "SensorMultilevelReport: ${cmd}"

	def eventMaps = []
	switch (cmd.sensorType) {
		case tempSensorType:
			def unit = cmd.scale == 1 ? "F" : "C"
			def temp = convertTemperatureIfNeeded(cmd.scaledSensorValue, unit, cmd.precision)

			eventMaps += createEventMaps("temperature", temp, temperatureScale, true, onlyIfNew)
			break

		case lightSensorType:
			eventMaps += createEventMaps("illuminance", cmd.scaledSensorValue, "lux", true, onlyIfNew)
			break

		case waterSensorType:
			// sendWaterEvents(cmd.scaledSensorValue)
			break
	}


	def result = []
	eventMaps?.each {
		logTrace "Creating Event: ${it}"
		it.descriptionText = getDisplayedDescriptionText(it)
		result << createEvent(it)
	}
	return result
}


private getDescriptionText(data) {
	switch (data?.name ?: "") {
		case "water":
			return "${data.value}" == "wet" ? "Wet" : "Dry"
			break
		case "temperature":
			return "${data.value}°${data.unit}"
			break
		case "illuminance":
			return "${data.value} LUX"
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
	if (state.pendingRefresh) {
		state.refreshAll = true
		logForceWakeupMessage "All configuration settings and sensor data will be requested from the device the next time it wakes up."
	}
	else {
		state.pendingRefresh = true
		logForceWakeupMessage "The sensor data will be refreshed the next time the device wakes up."
		sendEvent(createEventMap("pendingChanges", -2, "", false))
	}
	return []
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

private wakeUpIntervalSetCmd(seconds) {
	return secureCmd(zwave.wakeUpV2.wakeUpIntervalSet(seconds:seconds, nodeid:zwaveHubNodeId))
}

private wakeUpNoMoreInfoCmd() {
	return secureCmd(zwave.wakeUpV2.wakeUpNoMoreInformation())
}

private batteryGetCmd() {
	return secureCmd(zwave.batteryV1.batteryGet())
}

private versionGetCmd() {
	return secureCmd(zwave.versionV1.versionGet())
}

private sensorMultilevelGetCmd(sensorType) {
	return secureCmd(zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType: sensorType))
}

private configSetCmd(param, val) {
	return secureCmd(zwave.configurationV2.configurationSet(parameterNumber: param.num, size: param.size, scaledConfigurationValue: val))
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

private getDefaultWakeUpIntervalSeconds() { return (24 * 60 * 60) }

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