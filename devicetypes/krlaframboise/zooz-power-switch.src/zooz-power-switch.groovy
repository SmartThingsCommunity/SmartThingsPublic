/**
 *  Zooz Power Switch / Zooz Smart Plug v1.4
 *  (Models: ZEN15, ZEN06)
 *
 *  Author: 
 *    Kevin LaFramboise (krlaframboise)
 *
 *  URL to documentation: https://community.smartthings.com/t/release-zooz-power-switch-zooz-smart-plug/97220?u=krlaframboise
 *    
 *
 *  Changelog:
 *
 *    1.4 (12/17/2017)
 *      - Added Acceleration capability for detecting when power is on or off and setting for the active threshold.
 *      - Added overload protection override setting.
 *
 *    1.2 (09/09/2017)
 *      - The device seems to occasionallly report values that are way above a realitic value for some users so add a check that logs the value as a warning instead of creating events for it.
 *
 *    1.1 (09/06/2017)
 *      - Switched history to value tile to fix iOS bug.
 *
 *    1.0 (07/26/2017)
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
		name: "Zooz Power Switch", 
		namespace: "krlaframboise", 
		author: "Kevin LaFramboise"
	) {
		capability "Actuator"
		capability "Sensor"
		capability "Switch"		
		capability "Outlet"
		capability "Power Meter"
		capability "Energy Meter"
		capability "Voltage Measurement"
		capability "Configuration"
		capability "Refresh"
		capability "Health Check"
		capability "Acceleration Sensor"
		
		attribute "lastCheckin", "string"
		attribute "history", "string"
		attribute "current", "number"
		attribute "energyTime", "number"
		attribute "energyCost", "string"
		attribute "energyDuration", "string"
		
		["power", "voltage", "current"].each {
			attribute "${it}Low", "number"
			attribute "${it}High", "number"
		}
				
		command "reset"

		fingerprint mfr:"027A", prod:"0101", model:"000D", deviceJoinName: "Zooz Power Switch"
		
		fingerprint mfr:"027A", prod:"0101", model:"000A", deviceJoinName: "Zooz Smart Plug"
	}

	simulator { }
	
	preferences {
		configParams?.each {			
			getOptionsInput(it)
		}
		
		input "energyPrice", "decimal",
			title: "\$/kWh Cost:",
			defaultValue: energyPriceSetting,
			required: false,
			displayDuringSetup: true
			
		input "inactivePower", "number",
			title: "Report inactive when power is less than or equal to:",
			defaultValue: inactivePowerSetting,
			required: false,
			displayDuringSetup: true
			
		["Power", "Energy", "Voltage", "Current"].each {
			getBoolInput("display${it}", "Display ${it} Activity", true)
		}

		getBoolInput("debugOutput", "Enable Debug Logging", true)
	}

	tiles(scale: 2) {
		multiAttributeTile(name:"switch", type: "generic", width: 6, height: 4, canChangeIcon: true){
			tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
				attributeState "on", label: '${name}', action: "switch.off", icon: "st.switches.switch.on", backgroundColor: "#00a0dc"
				attributeState "off", label: '${name}', action: "switch.on", icon: "st.switches.switch.off", backgroundColor: "#ffffff"
			}
			tileAttribute ("device.acceleration", key: "SECONDARY_CONTROL") {
				attributeState "inactive", label:'INACTIVE'
				attributeState "active", label:'ACTIVE'
			}
		}
		standardTile("refresh", "device.refresh", width: 2, height: 2) {
			state "refresh", label:'Refresh', action: "refresh", icon:"st.secondary.refresh-icon"
		}
		standardTile("reset", "device.reset", width: 2, height: 2) {
			state "refresh", label:'Reset', action: "reset", icon:"st.secondary.refresh-icon"
		}
		valueTile("energy", "device.energy", width: 2, height: 2) {
			state "energy", label:'${currentValue} kWh', backgroundColor: "#cccccc"
		}
		valueTile("power", "device.power", width: 2, height: 2) {
			state "power", label:'${currentValue} W', backgroundColor: "#cccccc"
		}
		valueTile("voltage", "device.voltage", width: 2, height: 2) {
			state "voltage", label:'${currentValue} V', backgroundColor: "#cccccc"
		}
		valueTile("current", "device.current", width: 2, height: 2) {
			state "current", label:'${currentValue} A', backgroundColor: "#cccccc"
		}
		valueTile("history", "device.history", decoration:"flat",width: 6, height: 3) {
			state "history", label:'${currentValue}'
		}
		main "switch"
		details(["switch", "power", "energy", "refresh", "voltage", "current", "reset", "history"])
	}
}

private getOptionsInput(param) {
	if (param.prefName) {
		input "${param.prefName}", "enum",
			title: "${param.name}:",
			defaultValue: "${param.val}",
			required: false,
			displayDuringSetup: true,
			options: param.options?.collect { name, val -> name }
	}
}

private getBoolInput(name, title, defaultVal) {
	input "${name}", "bool", 
		title: "${title}?", 
		defaultValue: defaultVal, 
		required: false
}


// Meters
private getMeterEnergy() { 
	return getMeterMap("energy", 0, "kWh", null, settings?.displayEnergy != false) 
}

private getMeterPower() { 
	return getMeterMap("power", 2, "W", 2000, settings?.displayPower != false)
}

private getMeterVoltage() { 
	return getMeterMap("voltage", 4, "V", 150, settings?.displayVoltage != false) 
}

private getMeterCurrent() { 
	return getMeterMap("current", 5, "A", 18, settings?.displayCurrent != false)
}

private getMeterMap(name, scale, unit, limit, displayed) {
	return [name:name, scale:scale, unit:unit, limit: limit, displayed:displayed]
}


def updated() {	
	if (!isDuplicateCommand(state.lastUpdated, 3000)) {
		state.lastUpdated = new Date().time
		
		def cmds = configure()
		return cmds ? response(cmds) : []
	}
}

def configure() {
	def result = []
	
	updateHealthCheckInterval()
		
	def cmds = []	
	configParams.each { param ->	
		cmds += updateConfigVal(param)
	}
	result += delayBetweenCmds(cmds)
	
	if (!getAttrVal("energyTime")) {
		result += reset()
	}
	else {
		result += refresh()	
	}	
	return result
}

private updateConfigVal(param) {
	def cmds = []	
	if (hasPendingChange(param)) {
		def newVal = getParamIntVal(param)
		logTrace "${param.name}(#${param.num}): changing ${getParamStoredIntVal(param)} to ${newVal}"
		cmds << configSetCmd(param, newVal)
		cmds << configGetCmd(param)
	}	
	return cmds
}

private hasPendingChange(param) {
	return (getParamIntVal(param) != getParamStoredIntVal(param))
}

void updateHealthCheckInterval() {
	def minReportingInterval = minimumReportingInterval
	
	if (state.minReportingInterval != minReportingInterval) {
		state.minReportingInterval = minReportingInterval
			
		// Set the Health Check interval so that it can be skipped twice plus 5 minutes.
		def checkInterval = ((minReportingInterval * 2) + (5 * 60))
		
		def eventMap = createEventMap("checkInterval", checkInterval, false)
		eventMap.data = [protocol: "zwave", hubHardwareId: device.hub.hardwareID]
		
		sendEvent(eventMap)
	}
}

def ping() {
	logDebug "Pinging device because it has not checked in"
	return [switchBinaryGetCmd()]
}


def on() {
	logTrace "Turning On"
	return delayBetweenCmds([
		switchBinarySetCmd(0xFF),
		switchBinaryGetCmd()
	])
}

def off() {
	logTrace "Turning Off"
	return delayBetweenCmds([
		switchBinarySetCmd(0x00),
		switchBinaryGetCmd()
	])
}

def refresh() {
	logTrace "Refreshing"
	return delayBetweenCmds([
		switchBinaryGetCmd(),
		meterGetCmd(meterEnergy),
		meterGetCmd(meterPower),
		meterGetCmd(meterVoltage),
		meterGetCmd(meterCurrent)
	])
}

def reset() {
	logTrace "Resetting"
	["power", "voltage", "current"].each {
		sendEvent(createEventMap("${it}Low", getAttrVal(it), false))
		sendEvent(createEventMap("${it}High", getAttrVal(it), false))
	}
	sendEvent(createEventMap("energyTime", new Date().time, false))
	
	def result = []
	result << meterResetCmd()
	result += refresh()
	return result
}


private meterGetCmd(meter) {
	return secureCmd(zwave.meterV3.meterGet(scale: meter.scale))
}

private meterResetCmd() {
	return secureCmd(zwave.meterV3.meterReset())
}

private switchBinaryGetCmd() {
	return secureCmd(zwave.switchBinaryV1.switchBinaryGet())
}

private switchBinarySetCmd(val) {
	return secureCmd(zwave.switchBinaryV1.switchBinarySet(switchValue: val))
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

private delayBetweenCmds(cmds, delay=500) {
	return cmds ? delayBetween(cmds, delay) : []
}


def parse(String description) {	
	def result = []
	def cmd = zwave.parse(description, commandClassVersions)
	if (cmd) {
		result += zwaveEvent(cmd)		
	}
	else {
		log.warn "Unable to parse: $description"
	}
		
	if (!isDuplicateCommand(state.lastCheckinTime, 60000)) {
		state.lastCheckinTime = new Date().time
		result << createEvent(createEventMap("lastCheckin", convertToLocalTimeString(new Date()), false))
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

private getCommandClassVersions() {
	[
		0x20: 1,	// Basic
		0x25: 1,	// Switch Binary
		0x27: 1,	// All Switch
		0x2B: 1,	// Scene Activation
		0x2C: 1,	// Scene Actuator Configuration
		0x32: 3,	// Meter v4
		0x59: 1,	// AssociationGrpInfo
		0x5A: 1,	// DeviceResetLocally
		0x5E: 2,	// ZwaveplusInfo
		0x70: 2,	// Configuration
		0x72: 2,	// ManufacturerSpecific
		0x73: 1,	// Powerlevel
		0x7A: 2,	// Firmware Update Md (3)
		0x85: 2,	// Association
		0x86: 1,	// Version (2)
		0x98: 1		// Security
	]
}

def zwaveEvent(physicalgraph.zwave.commands.configurationv2.ConfigurationReport cmd) {	
	def val = cmd.scaledConfigurationValue
		
	def configParam = configParams.find { param ->
		param.num == cmd.parameterNumber
	}
	
	if (configParam) {
		def name = configParam.options?.find { it.value == val}?.key
		logDebug "${configParam.name}(#${configParam.num}) = ${name != null ? name : val} (${val})"
		state["configVal${cmd.parameterNumber}"] = val
		
		if (configParam.num == overloadProtectionParam.num) {
			refreshHistory()
		}
		
	}	
	else {
		logDebug "Parameter ${cmd.parameterNumber} = ${val}"
	}	
	return []
}

def zwaveEvent(physicalgraph.zwave.commands.switchbinaryv1.SwitchBinaryReport cmd) {
	logTrace "SwitchBinaryReport: ${cmd}"
	def result = []
	result << createSwitchEvent(cmd.value, "digital")
	return result
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd) {
	logTrace "BasicReport: ${cmd}"
	def result = []
	result << createSwitchEvent(cmd.value, "physical")
	return result
}

private createSwitchEvent(value, type) {
	def eventVal = (value == 0xFF) ? "on" : "off"
	def map = createEventMap("switch", eventVal, null, "Switch is ${eventVal}")
	map.type = type
	return createEvent(map)
}

def zwaveEvent(physicalgraph.zwave.commands.meterv3.MeterReport cmd) {
	logTrace "MeterReport: $cmd"
	def result = []	
	def val = roundTwoPlaces(cmd.scaledMeterValue)
		
	def meter 
	switch (cmd.scale) {
		case meterEnergy.scale:			
			meter = meterEnergy
			break
		case meterPower.scale:
			createAccelerationEvent(val)		
			meter = meterPower
			break
		case meterVoltage.scale:
			meter = meterVoltage
			break
		case meterCurrent.scale:
			meter = meterCurrent
			break
		default:
			logDebug "Unknown Meter Scale: $cmd"
	}

	if (meter?.limit && val > meter.limit) {
		log.warn "Ignored ${meter.name} value ${val}${meter.unit} because the highest possible value is ${meter.limit}${meter.unit}."
	}
	else if (meter?.name && getAttrVal("${meter.name}") != val) {
		result << createEvent(createEventMap(meter.name, val, meter.displayed, null, meter.unit))
		
		if (meter.name == meterEnergy.name) {
			result += createEnergyEvents(val)
		}
		else {
			result += createHighLowEvents(meter, val)
		}
		
		runIn(5, refreshHistory)
	}	
	return result
}

private createAccelerationEvent(val) {
	def deviceActive = (device.currentValue("acceleration") == "active")
	if (val > inactivePowerSetting &&  !deviceActive) {
		sendEvent(name:"acceleration", value:"active", displayed:false)
	}
	else if (val <= inactivePowerSetting && deviceActive){
		sendEvent(name:"acceleration", value:"inactive", displayed:false)
	}
}

private createHighLowEvents(meter, val) {
	def result = []
	def highLowNames = [] 
	def highName = "${meter.name}High"
	def lowName = "${meter.name}Low"
	if (!getAttrVal(highName) || val > getAttrVal(highName)) {
		highLowNames << highName
	}
	if (!getAttrVal(lowName) || meter.value < getAttrVal(lowName)) {
		highLowNames << lowName
	}
	
	highLowNames.each {
		result << createEvent(createEventMap("$it", val, false, null, meter.unit))
	}
	return result
}

private createEnergyEvents(val) {
	def result = []
	
	def cost = "\$${roundTwoPlaces(val * energyPriceSetting)}"
	if (getAttrVal("energyCost") != cost) {
		result << createEvent(createEventMap("energyCost", cost, false))
	}
	
	result << createEvent(createEventMap("energyDuration", calculateEnergyDuration(), false))	
	return result
}

private calculateEnergyDuration() {
	def energyTimeMS = getAttrVal("energyTime")
	if (!energyTimeMS) {
		return "Unknown"
	}
	else {
		def duration = roundTwoPlaces((new Date().time - energyTimeMS) / 60000)
		
		if (duration >= (24 * 60)) {
			return getFormattedDuration(duration, (24 * 60), "Day")
		}
		else if (duration >= 60) {
			return getFormattedDuration(duration, 60, "Hour")
		}
		else {
			return getFormattedDuration(duration, 0, "Minute")
		}
	}
}

private getFormattedDuration(duration, divisor, name) {
	if (divisor) {
		duration = roundTwoPlaces(duration / divisor)
	}	
	return "${duration} ${name}${duration == 1 ? '' : 's'}"
}

def refreshHistory() {
	def history = ""
	def items = [:]
			
	items["energyDuration"] = "Energy - Duration"
	items["energyCost"] = "Energy - Cost"
	["power", "voltage", "current"].each {
		items["${it}Low"] = "${it.capitalize()} - Low"
		items["${it}High"] = "${it.capitalize()} - High"
	}
	
	if (getParamStoredIntVal(overloadProtectionParam) == 0) {
		history += "*** Overload Protection Disabled ***\n"
	}
	
	items.each { attrName, caption ->
		def attr = device.currentState("${attrName}")
		def val = attr?.value ?: ""
		def unit = attr?.unit ?: ""
		history += "${caption}: ${val} ${unit}\n"
	}
	sendEvent(createEventMap("history", history, false))
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
	logDebug "Unhandled zwaveEvent: $cmd"
	return []
}


// Configuration Parameters
private getConfigParams() {
	return [
		overloadProtectionParam,
		powerFailureRecoveryParam,
		onOffNotificationsParam,
		ledIndicatorParam,
		powerValueChangeParam,
		powerPercentageChangeParam,
		powerReportIntervalParam,
		energyReportIntervalParam,
		voltageReportIntervalParam,
		electricityReportIntervalParam
	]
}

private getOverloadProtectionParam() {
	return createConfigParamMap(20, "Overload Protection", 1, ["Disabled (NOT RECOMMENDED)":0, "Enabled${defaultOptionSuffix}":1], "overloadProtection")
}

private getPowerFailureRecoveryParam() {
	return createConfigParamMap(21, "Power Failure Recovery", 1, ["Remember last status${defaultOptionSuffix}":0, "Turn On":1, "Turn Off":2], "powerFailureRecovery")
}

private getOnOffNotificationsParam() {
	return createConfigParamMap(24, "On/Off Notifications", 1, null, null, 2) // 2:Manual switch only
}

private getLedIndicatorParam() {
	return createConfigParamMap(27, "LED Power Consumption Indicator", 1, ["Always Show${defaultOptionSuffix}":0, "Show for 5 seconds when turned on or off":1], "ledIndicator")
}

private getPowerValueChangeParam() {
	return createConfigParamMap(151, "Power Report Value Change", 2, getPowerValueOptions(), "powerValueChange")
}

private getPowerPercentageChangeParam() {
	return createConfigParamMap(152, "Power Report Percentage Change", 1, getPercentageOptions(10, "No Reports"), "powerPercentageChange")
}

private getPowerReportIntervalParam() {
	return createConfigParamMap(171, "Power Reporting Interval", 4, getIntervalOptions(30, "No Reports"), "powerReportingInterval")
}

private getEnergyReportIntervalParam() {
	return createConfigParamMap(172, "Energy Reporting Interval", 4, getIntervalOptions(300, "No Reports"), "energyReportingInterval")	
}

private getVoltageReportIntervalParam() {
	return createConfigParamMap(173, "Voltage Reporting Interval", 4, getIntervalOptions(0, "No Reports"), "voltageReportingInterval")	
}

private getElectricityReportIntervalParam() {
	return createConfigParamMap(174, "Electrical Current Reporting Interval", 4, getIntervalOptions(0, "No Reports"), "electricityReportingInterval")	
}

private getParamStoredIntVal(param) {
	return state["configVal${param.num}"]	
}

private getParamIntVal(param) {
	return param.options ? convertOptionSettingToInt(param.options, param.val) : param.val
}

private createConfigParamMap(num, name, size, options, prefName, val=null) {
	if (val == null) {
		val = (settings?."${prefName}" ?: findDefaultOptionName(options))
	}
	return [
		num: num, 
		name: name, 
		size: size, 
		options: options, 
		prefName: prefName,
		val: val
	]
}

// Settings
private getEnergyPriceSetting() {
	return safeToDec(settings?.energyPrice, 0.12)
}

private getInactivePowerSetting() {
	return safeToInt(settings?.inactivePower, 0)
}

private getDebugOutputSetting() {
	return settings?.debugOutput != false
}

private getMinimumReportingInterval() {
	def minVal = (60 * 60 * 24 * 7)
	[powerReportIntervalParam, energyReportIntervalParam, voltageReportIntervalParam, electricityReportIntervalParam].each {
		def val = convertOptionSettingToInt(it.options, it.val)
		if (val && val < minVal) {
			minVal = val
		}		
	}
	return minVal
}

private getIntervalOptions(defaultVal=null, zeroName=null) {
	def options = [:]
	if (zeroName) {
		options["${zeroName}"] = 0
	}
	options << getIntervalOptionsRange("Second", 1, [5,10,15,30,45])
	options << getIntervalOptionsRange("Minute", 60, [1,2,3,4,5,10,15,30,45])
	options << getIntervalOptionsRange("Hour", (60 * 60), [1,2,3,6,9,12,18])
	options << getIntervalOptionsRange("Day", (60 * 60 * 24), [1,3,5])
	options << getIntervalOptionsRange("Week", (60 * 60 * 24 * 7), [1,2])
	return setDefaultOption(options, defaultVal)
}

private getIntervalOptionsRange(name, multiplier, range) {
	def options = [:]
	range?.each {
		options["${it} ${name}${it == 1 ? '' : 's'}"] = (it * multiplier)
	}
	return options
}

private getPowerValueOptions() {
	def options = [:]	
	[0,1,2,3,4,5,10,25,50,75,100,150,200,250,300,400,500,750,1000,1250,1500,1750,2000,2500,3000,3500,4000,4500,5000,6000,7000,8000,9000,10000,12500,15000].each {		
		if (it == 0) {
			options["No Reports"] = it
		}
		else {
			options["${it} Watts"] = it
		}
	}
	return setDefaultOption(options, 50)
}

private getPercentageOptions(defaultVal=null, zeroName=null) {
	def options = [:]
	if (zeroName) {
		options["${zeroName}"] = 0
	}	
	for (int i = 1; i <= 5; i += 1) {
		options["${i}%"] = i
	}		
	for (int i = 10; i <= 100; i += 5) {
		options["${i}%"] = i
	}	
	return setDefaultOption(options, defaultVal)
}

private convertOptionSettingToInt(options, settingVal) {
	return safeToInt(options?.find { name, val -> "${settingVal}" == name }?.value, 0)
}

private setDefaultOption(options, defaultVal) {
	def name = options.find { key, val -> val == defaultVal }?.key
	if (name != null) {
		return changeOptionName(options, defaultVal, "${name}${defaultOptionSuffix}")
	}
	else {
		return options
	}	
}

private changeOptionName(options, optionVal, newName) {
	def result = [:]	
	options?.each { name, val ->
		if (val == optionVal) {
			name = "${newName}"
		}
		result["${name}"] = val
	}
	return result
}

private findDefaultOptionName(options) {
	def option = options?.find { name, val ->
		name?.contains("${defaultOptionSuffix}") 
	}
	return option?.key ?: ""
}

private getDefaultOptionSuffix() {
	return "   (Default)"
}

private createEventMap(name, value, displayed=null, desc=null, unit=null) {	
	desc = desc ?: "${name} is ${value}"
	
	def eventMap = [
		name: name,
		value: value,
		displayed: (displayed == null ? ("${getAttrVal(name)}" != "${value}") : displayed),
		isStateChange: true
	]
	
	if (unit) {
		eventMap.unit = unit
		desc = "${desc} ${unit}"
	}
	
	if (desc && eventMap.displayed) {
		logDebug desc
		eventMap.descriptionText = "${device.displayName} - ${desc}"
	}
	else {
		logTrace "Creating Event: ${eventMap}"
	}
	return eventMap
}

private getAttrVal(attrName) {
	try {
		return device?.currentValue("${attrName}")
	}
	catch (ex) {
		logTrace "$ex"
		return null
	}
}

private safeToInt(val, defaultVal=0) {
	return "${val}"?.isInteger() ? "${val}".toInteger() : defaultVal
}

private safeToDec(val, defaultVal=0) {
	return "${val}"?.isBigDecimal() ? "${val}".toBigDecimal() : defaultVal
}

private roundTwoPlaces(val) {
	return Math.round(safeToDec(val) * 100) / 100
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