/*
 *  Zooz ZEN26 S2 On/Off Switch VER. 3.0 
 *  	(Model: ZEN26 - MINIMUM FIRMWARE 2.03)
 *
 *  Changelog:
 *
 *    3.0 (09/16/2020)
 *      - Initial Release
 *
 *
 *  Copyright 2020 Zooz
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

import groovy.json.JsonOutput
import groovy.transform.Field

@Field static Map commandClassVersions = [
	0x20: 1,	// Basic
	0x25: 1,	// Switch Binary
	0x55: 1,	// Transport Service
	0x59: 1,	// AssociationGrpInfo
	0x5A: 1,	// DeviceResetLocally
	0x5B: 1,	// CentralScene (3)
	0x5E: 2,	// ZwaveplusInfo
	0x6C: 1,	// Supervision
	0x70: 1,	// Configuration
	0x7A: 2,	// FirmwareUpdateMd
	0x72: 2,	// ManufacturerSpecific
	0x73: 1,	// Powerlevel
	0x85: 2,	// Association
	0x86: 1,	// Version (2)
	0x8E: 2,	// Multi Channel Association
	0x98: 1,	// Security S0
	0x9F: 1		// Security S2
]

@Field static Map paddlePaddleOrientationOptions = [0:"Up for On, Down for Off [DEFAULT]", 1:"Up for Off, Down for On", 2:"Up or Down for On/Off"]

@Field static Map ledIndicatorOptions = [0:"LED On When Switch Off [DEFAULT]", 1:"LED On When Switch On", 2:"LED Always Off", 3:"LED Always On"]

@Field static Map disabledEnabledOptions = [0:"Disabled [DEFAULT]", 1:"Enabled"]

@Field static Map autoOnOffIntervalOptions = [0:"Disabled [DEFAULT]", 1:"1 Minute", 2:"2 Minutes", 3:"3 Minutes", 4:"4 Minutes", 5:"5 Minutes", 6:"6 Minutes", 7:"7 Minutes", 8:"8 Minutes", 9:"9 Minutes", 10:"10 Minutes", 15:"15 Minutes", 20:"20 Minutes", 25:"25 Minutes", 30:"30 Minutes", 45:"45 Minutes", 60:"1 Hour", 120:"2 Hours", 180:"3 Hours", 240:"4 Hours", 300:"5 Hours", 360:"6 Hours", 420:"7 Hours", 480:"8 Hours", 540:"9 Hours", 600:"10 Hours", 720:"12 Hours", 1080:"18 Hours", 1440:"1 Day", 2880:"2 Days", 4320:"3 Days", 5760:"4 Days", 7200:"5 Days", 8640:"6 Days", 10080:"1 Week", 20160:"2 Weeks", 30240:"3 Weeks", 40320:"4 Weeks", 50400:"5 Weeks", 60480:"6 Weeks"]

@Field static Map powerFailureRecoveryOptions = [0:"Forced to Off [DEFAULT]", 1:"Forced to On", 2:"Restores Last Status"]

@Field static Map relayControlOptions = [1:"Enable Paddle and Z-Wave [DEFAULT]", 0:"Disable Paddle", 2:"Disable Paddle and Z-Wave"]

@Field static Map relayBehaviorOptions = [0:"Reports Status & Changes LED [DEFAULT]", 1:"Doesn't Report Status or Change LED"]


metadata {
	definition (
		name: "Zooz ZEN26 S2 On/Off Switch VER. 3.0",
		namespace: "Zooz",
		author: "Kevin LaFramboise (@krlaframboise)",
		ocfDeviceType: "oic.d.switch"
	) {
		capability "Actuator"
		capability "Sensor"
		capability "Switch"
		capability "Light"
		capability "Configuration"
		capability "Refresh"
		capability "Health Check"
		capability "Button"

		attribute "firmwareVersion", "string"
		attribute "lastCheckIn", "string"
		attribute "syncStatus", "string"
		attribute "associatedDeviceNetworkIds", "string"

		fingerprint mfr:"027A", prod:"A000", model: "A001", deviceJoinName:"Zooz S2 On/Off Switch"
	}

	simulator { }

	tiles(scale: 2) {
		multiAttributeTile(name:"switch", type: "lighting", width: 6, height: 4, canChangeIcon: true){
			tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
				attributeState "on", label:'${name}', action:"switch.off", icon:"st.Lighting.light13", backgroundColor:"#00a0dc", nextState:"turningOff"
				attributeState "off", label:'${name}', action:"switch.on", icon:"st.Lighting.light13", backgroundColor:"#ffffff", nextState:"turningOn"
				attributeState "turningOn", label:'TURNING ON', action:"switch.off", icon:"st.lights.philips.hue-single", backgroundColor:"#00a0dc", nextState:"turningOff"
				attributeState "turningOff", label:'TURNING OFF', action:"switch.on", icon:"st.lights.philips.hue-single", backgroundColor:"#ffffff", nextState:"turningOn"
			}
		}
		standardTile("refresh", "device.refresh", width: 2, height: 2) {
			state "refresh", label:'Refresh', action: "refresh"
		}
		valueTile("syncStatus", "device.syncStatus", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "syncStatus", label:'${currentValue}'
		}
		standardTile("sync", "device.configure", width: 2, height: 2) {
			state "default", label: 'Sync', action: "configure"
		}
		valueTile("firmwareVersion", "device.firmwareVersion", decoration:"flat", width:3, height: 1) {
			state "firmwareVersion", label:'Firmware ${currentValue}'
		}

		standardTile("assocLabel", "device.associatedDeviceNetworkIds", decoration: "flat", width: 3, height: 1) {
			state "default", label:'Associated Device Network Ids:'
			state "none", label:""
		}

		standardTile("assocDNIs", "device.associatedDeviceNetworkIds", decoration: "flat", width: 3, height: 1) {
			state "default", label:'${currentValue}'
			state "none", label:""
		}

		main "switch"
		details(["switch", "refresh", "syncStatus", "sync", "firmwareVersion", "assocLabel", "assocDNIs"])
	}

	preferences {
		configParams.each { param ->
			if (!(param in [autoOffEnabledParam, autoOnEnabledParam])) {
				createEnumInput("configParam${param.num}", "${param.name}:", param.value, param.options)
			}
		}

		input "assocInstructions", "paragraph",
			title: "Device Associations",
			description: "Associations are an advance feature that allow you to establish direct communication between Z-Wave devices.  To make this motion sensor control another Z-Wave device, get that device's Device Network Id from the My Devices section of the IDE and enter the id below.  It supports up to 4 associations and you can use commas to separate the device network ids.",
			required: false

		input "assocDisclaimer", "paragraph",
			title: "WARNING",
			description: "If you add a device's Device Network ID to the list below and then remove that device from SmartThings, you MUST come back and remove it from the list below.  Failing to do this will substantially increase the number of z-wave messages being sent by this device and could affect the stability of your z-wave mesh.",
			required: false

		input "assocDNIs", "string",
			title: "Enter Device Network IDs for Association: (Enter 0 to clear field in new iOS mobile app)",
			required: false

		createEnumInput("debugOutput", "Enable Debug Logging?", 1, [0:"No", 1:"Yes [DEFAULT]"])
	}
}

void createEnumInput(String name, String title, Integer defaultVal, Map options) {
	input name, "enum",
		title: title,
		required: false,
		defaultValue: defaultVal.toString(),
		options: options
}

String getAssocDNIsSetting() {
	def val = settings?.assocDNIs
	return ((val && (val.trim() != "0")) ? val : "") // new iOS app has no way of clearing string input so workaround is to have users enter 0.
}


def installed() {
	logDebug "installed()..."

	if (state.debugLoggingEnabled == null) {
		state.debugLoggingEnabled = true
	}
	return []
}


def updated() {
	if (!isDuplicateCommand(state.lastUpdated, 5000)) {
		state.lastUpdated = new Date().time

		logDebug "updated()..."

		state.debugLoggingEnabled = (safeToInt(settings?.debugOutput, 1) != 0)

		initialize()

		runIn(5, executeConfigureCmds, [overwrite: true])
	}
	return []
}

void initialize() {
	def checkInterval = ((60 * 60 * 3) + (5 * 60))

	Map checkIntervalEvt = [name: "checkInterval", value: checkInterval, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID, offlinePingable: "1"]]

	if (!device.currentValue("checkInterval")) {
		sendEvent(checkIntervalEvt)
	}

	if (!device.currentValue("supportedButtonValues")) {
		sendEvent(name:"supportedButtonValues", value:JsonOutput.toJson(["down","down_hold","down_released","down_2x","down_4x","down_5x","up","up_hold","up_released","up_2x","up_4x","up_5x"]), displayed:false)
	}

	if (!device.currentValue("numberOfButtons")) {
		sendEvent(name:"numberOfButtons", value:1, displayed:false)
	}

	if (!device.currentValue("button")) {
		sendButtonEvent("pushed")
	}
}


def configure() {
	logDebug "configure()..."

	if (state.resyncAll == null) {
		state.resyncAll = true
		runIn(8, executeConfigureCmds, [overwrite: true])
	}
	else {
		if (!pendingChanges) {
			state.resyncAll = true
		}
		executeConfigureCmds()
	}
	return []
}

void executeConfigureCmds() {
	runIn(6, refreshSyncStatus)

	List<String> cmds = []

	if (!device.currentValue("switch")) {
		cmds << switchBinaryGetCmd()
	}

	if (state.resyncAll || !device.currentValue("firmwareVersion")) {
		cmds << versionGetCmd()
	}

	if ((state.resyncAll == true) || (state.group1Assoc != true)) {
		cmds << associationSetCmd(1, [zwaveHubNodeId])
		cmds << associationGetCmd(1)
	}

	cmds += getConfigureAssocsCmds()

	configParams.each { param ->
		Integer paramVal = getAdjustedParamValue(param)
		Integer storedVal = getParamStoredValue(param.num)

		if ((paramVal != null) && (state.resyncAll || (storedVal != paramVal))) {
			logDebug "Changing ${param.name}(#${param.num}) from ${storedVal} to ${paramVal}"
			cmds << configSetCmd(param, paramVal)
			cmds << configGetCmd(param)
		}
	}

	state.resyncAll = false
	if (cmds) {
		sendCommands(delayBetween(cmds, 500))
	}
}

private getAdjustedParamValue(Map param) {
	Integer paramVal
	switch(param.num) {
		case autoOffEnabledParam.num:
			paramVal = autoOffIntervalParam.value == 0 ? 0 : 1
			break
		case autoOffIntervalParam.num:
			paramVal = autoOffIntervalParam.value ?: null
			break
		case autoOnEnabledParam.num:
			paramVal = autoOnIntervalParam.value == 0 ? 0 : 1
			break
		case autoOnIntervalParam.num:
			paramVal = autoOnIntervalParam.value ?: null
			break
		default:
			paramVal = param.value
	}
	return paramVal
}


private getConfigureAssocsCmds() {
	def cmds = []

	if (!device.currentValue("associatedDeviceNetworkIds")) {
		sendEventIfNew("associatedDeviceNetworkIds", "none", false)
	}

	def settingNodeIds = assocDNIsSettingNodeIds

	def newNodeIds = settingNodeIds?.findAll { !(it in state.assocNodeIds) }
	if (newNodeIds) {
		cmds << associationSetCmd(2, newNodeIds)
	}

	def oldNodeIds = state.assocNodeIds?.findAll { !(it in settingNodeIds) }
	if (oldNodeIds) {
		cmds << associationRemoveCmd(2, oldNodeIds)
	}

	if (cmds || state.syncAll) {
		cmds << associationGetCmd(2)
	}

	if (!state.group1Assoc || state.syncAll) {
		if (state.group1Assoc == false) {
			logDebug "Adding missing lifeline association..."
			cmds << associationSetCmd(1, [zwaveHubNodeId])
		}
		cmds << associationGetCmd(1)
	}

	return cmds
}


private getAssocDNIsSettingNodeIds() {
	def nodeIds = convertHexListToIntList(assocDNIsSetting?.split(","))

	if (assocDNIsSetting && !nodeIds) {
		log.warn "'${assocDNIsSetting}' is not a valid value for the 'Device Network Ids for Association' setting.  All z-wave devices have a 2 character Device Network Id and if you're entering more than 1, use commas to separate them."
	}
	else if (nodeIds?.size() >  4) {
		log.warn "The 'Device Network Ids for Association' setting contains more than 4 Ids so only the first 4 will be associated."
	}

	return nodeIds
}


def ping() {
	logDebug "ping()..."
	return [ switchBinaryGetCmd() ]
}


def on() {
	logDebug "on()..."
	return delayBetween([
		switchBinarySetCmd(0xFF),
		switchBinaryGetCmd()
	], 250)
}


def off() {
	logDebug "off()..."
	
	return delayBetween([
		switchBinarySetCmd(0x00),
		switchBinaryGetCmd()
	], 250)
}


def refresh() {
	logDebug "refresh()..."

	refreshSyncStatus()

	sendCommands([switchBinaryGetCmd()])
	
	return []
}


void sendCommands(List<String> cmds) {
	if (cmds) {
		def actions = []
		cmds.each {
			actions << new physicalgraph.device.HubAction(it)
		}
		sendHubCommand(actions)
	}	
}


String associationSetCmd(Integer group, List<Integer> nodes) {
	return secureCmd(zwave.associationV2.associationSet(groupingIdentifier: group, nodeId: nodes))
}

String associationRemoveCmd(Integer group, List<Integer> nodes) {
	return secureCmd(zwave.associationV2.associationRemove(groupingIdentifier: group, nodeId: nodes))
}

String associationGetCmd(Integer group) {
	return secureCmd(zwave.associationV2.associationGet(groupingIdentifier: group))
}

String versionGetCmd() {
	return secureCmd(zwave.versionV1.versionGet())
}

String switchBinaryGetCmd() {
	return secureCmd(zwave.switchBinaryV1.switchBinaryGet())
}

String switchBinarySetCmd(val) {
	return secureCmd(zwave.switchBinaryV1.switchBinarySet(switchValue: val))
}

String configSetCmd(Map param, Integer value) {
	return secureCmd(zwave.configurationV1.configurationSet(parameterNumber: param.num, size: param.size, scaledConfigurationValue: value))
}

String configGetCmd(Map param) {
	return secureCmd(zwave.configurationV1.configurationGet(parameterNumber: param.num))
}

String secureCmd(cmd) {
	try {
		if (zwaveInfo?.zw?.contains("s") || ("0x98" in device?.rawDescription?.split(" "))) {
			return zwave.securityV1.securityMessageEncapsulation().encapsulate(cmd).format()
		}
		else {
			return cmd.format()
		}
	}
	catch (ex) {
		return cmd.format()
	}
}


def parse(String description) {
	def cmd = zwave.parse(description, commandClassVersions)
	if (cmd) {
		zwaveEvent(cmd)
	}
	else {
		log.warn "Unable to parse: $description"
	}

	updateLastCheckIn()
	return []
}

void updateLastCheckIn() {
	if (!isDuplicateCommand(state.lastCheckInTime, 60000)) {
		state.lastCheckInTime = new Date().time

		sendEvent(name: "lastCheckIn", value: convertToLocalTimeString(new Date()), displayed: false)
	}
}

String convertToLocalTimeString(dt) {
	try {
		def timeZoneId = location?.timeZone?.ID
		if (timeZoneId) {
			return dt.format("MM/dd/yyyy hh:mm:ss a", TimeZone.getTimeZone(timeZoneId))
		}
		else {
			return "$dt"
		}
	}
	catch (ex) {
		return "$dt"
	}
}


void zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityMessageEncapsulation cmd) {
	def encapsulatedCmd = cmd.encapsulatedCommand(commandClassVersions)
	if (encapsulatedCmd) {
		zwaveEvent(encapsulatedCmd)
	}
	else {
		log.warn "Unable to extract encapsulated cmd from $cmd"
	}
}


void zwaveEvent(physicalgraph.zwave.commands.configurationv1.ConfigurationReport cmd) {
	updateSyncingStatus()
	runIn(4, refreshSyncStatus)

	Map param = configParams.find { it.num == cmd.parameterNumber }
	if (param) {
		Integer val = cmd.scaledConfigurationValue
		logDebug "${param.name}(#${param.num}) = ${val}"
		setParamStoredValue(param.num, val)
	}
	else {
		logDebug "Parameter #${cmd.parameterNumber} = ${cmd.scaledConfigurationValue}"
	}
}


void zwaveEvent(physicalgraph.zwave.commands.associationv2.AssociationReport cmd) {
	logTrace "${cmd}"

	updateSyncingStatus()
	runIn(4, refreshSyncStatus)

	if (cmd.groupingIdentifier == 1) {
		logDebug "Lifeline Association: ${cmd.nodeId}"

		state.group1Assoc = (cmd.nodeId == [zwaveHubNodeId]) ? true : false
	}
	else if (cmd.groupingIdentifier == 2) {
		logDebug "Group 2 Association: ${cmd.nodeId}"

		state.assocNodeIds = cmd.nodeId

		def dnis = convertIntListToHexList(cmd.nodeId)?.join(", ") ?: "none"
		sendEventIfNew("associatedDeviceNetworkIds", dnis, false)
	}
}


void zwaveEvent(physicalgraph.zwave.commands.versionv1.VersionReport cmd) {
	String subVersion = String.format("%02d", cmd.applicationSubVersion)
	String fullVersion = "${cmd.applicationVersion}.${subVersion}"

	sendEventIfNew("firmwareVersion", fullVersion)
}


void zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd) {
	logTrace "${cmd}"
	sendSwitchEvent(cmd.value, "physical")
}

void zwaveEvent(physicalgraph.zwave.commands.switchbinaryv1.SwitchBinaryReport cmd) {
	logTrace "${cmd}"	
	sendSwitchEvent(cmd.value, "digital")
}

void sendSwitchEvent(rawVal, String type) {
	sendEventIfNew("switch", (rawVal ? "on" : "off"), true, type)
}


void zwaveEvent(physicalgraph.zwave.commands.centralscenev1.CentralSceneNotification cmd){
	if (state.lastSequenceNumber != cmd.sequenceNumber) {
		state.lastSequenceNumber = cmd.sequenceNumber

		String actionType
		String btnVal
		String displayName = ""

		switch (cmd.sceneNumber) {
			case 1:
				actionType = "down"				
				break
			case 2:
				actionType = "up"
				break
			default:
				logDebug "Unknown Scene: ${cmd}"
		}

		switch (cmd.keyAttributes){
			case 0:
				btnVal = actionType
				break
			case 1:
				btnVal = "${actionType}_released"
				break
			case 2:
				btnVal = "${actionType}_hold"
				break
			default:
				btnVal = "${actionType}_${cmd.keyAttributes - 1}x"
		}

		if (btnVal) {
			sendButtonEvent(btnVal)
		}
	}
}

void sendButtonEvent(String value) {
	logDebug "Button ${value}"
	sendEvent(name: "button", value: value, data:[buttonNumber: 1], isStateChange: true)
}


void zwaveEvent(physicalgraph.zwave.Command cmd) {
	logDebug "Unhandled zwaveEvent: $cmd"
}


void updateSyncingStatus() {
	sendEventIfNew("syncStatus", "Syncing...", false)
}

void refreshSyncStatus() {
	Integer changes = pendingChanges
	sendEventIfNew("syncStatus", (changes ?  "${changes} Pending Changes" : "Synced"), false)
}


Integer getPendingChanges() {
	Integer configChanges = configParams.count { param ->
		Integer paramVal = getAdjustedParamValue(param)
		((paramVal != null) && (paramVal != getParamStoredValue(param.num)))
	}
	Integer pendingAssocs = getConfigureAssocsCmds()?.size() ?: 0
	Integer group1Assoc = (state.group1Assoc != true) ? 1 : 0
	return (configChanges + pendingAssocs + group1Assoc)
}


Integer getParamStoredValue(Integer paramNum) {
	return safeToInt(state["configVal${paramNum}"] , null)
}

void setParamStoredValue(Integer paramNum, Integer value) {
	state["configVal${paramNum}"] = value
}


List<Map> getConfigParams() {
	return [
		paddlePaddleOrientationParam,
		ledIndicatorParam,
		autoOffEnabledParam,
		autoOffIntervalParam,
		autoOnEnabledParam,
		autoOnIntervalParam,
		// associationReportsParam,
		powerFailureRecoveryParam,
		sceneControlParam,
		relayControlParam,
		relayBehaviorParam
	]
}

Map getPaddlePaddleOrientationParam() {
	return getParam(1, "Paddle Orientation", 1, 0, paddlePaddleOrientationOptions)
}

Map getLedIndicatorParam() {
	return getParam(2, "LED Indicator", 1, 0, ledIndicatorOptions)
}

Map getAutoOffEnabledParam() {
	return getParam(3, "Auto Turn-Off Timer Enabled", 1, 0, disabledEnabledOptions)
}

Map getAutoOffIntervalParam() {
	return getParam(4, "Auto Turn-Off Timer", 4, 0, autoOnOffIntervalOptions)
}

Map getAutoOnEnabledParam() {
	return getParam(5, "Auto Turn-On Timer Enabled", 1, 0, disabledEnabledOptions)
}

Map getAutoOnIntervalParam() {
	return getParam(6, "Auto Turn-On Timer", 4, 0, autoOnOffIntervalOptions)
}

// Map getAssociationReportsParam() {
	// return getParam(7, "Association Settings", 1, 1, associationReportsOptions)
// }

Map getPowerFailureRecoveryParam() {
	return getParam(8, "Behavior After Power Outage", 1, 0, powerFailureRecoveryOptions)
}

Map getSceneControlParam() {
	return getParam(10, "Scene Control", 1, 0, disabledEnabledOptions)
}

Map getRelayControlParam() {
	return getParam(11, "Relay Control", 1, 1, relayControlOptions)
}

Map getRelayBehaviorParam() {
	return getParam(13, "Relay Behavior", 1, 0, relayBehaviorOptions)
}

Map getParam(Integer num, String name, Integer size, Integer defaultVal, Map options) {
	Integer val = safeToInt((settings ? settings["configParam${num}"] : null), defaultVal)

	return [num: num, name: name, size: size, value: val, options: options]
}

Map setDefaultOption(Map options, Integer defaultVal) {
	return options?.collectEntries { k, v ->
		if ("${k}" == "${defaultVal}") {
			v = "${v} [DEFAULT]"
		}
		["$k": "$v"]
	}
}


void sendEventIfNew(String name, value, boolean displayed=true, String type=null, String unit="") {
	String desc = "${name} is ${value}${unit}"
	if (device.currentValue(name) != value) {

		if (name != "syncStatus") {
			logDebug(desc)
		}

		Map evt = [name: name, value: value, descriptionText: "${device.displayName} ${desc}", displayed: displayed]

		if (type) {
			evt.type = type
		}
		if (unit) {
			evt.unit = unit
		}
		sendEvent(evt)
	}
	else {
		logTrace(desc)
	}
}


private convertIntListToHexList(intList) {
	def hexList = []
	intList?.each {
		hexList.add(Integer.toHexString(it).padLeft(2, "0").toUpperCase())
	}
	return hexList
}

private convertHexListToIntList(String[] hexList) {
	def intList = []

	hexList?.each {
		try {
			it = it.trim()
			intList.add(Integer.parseInt(it, 16))
		}
		catch (e) { }
	}
	return intList
}


Integer safeToInt(val, Integer defaultVal=0) {
	return "${val}"?.isInteger() ? "${val}".toInteger() : defaultVal
}


boolean isDuplicateCommand(lastExecuted, allowedMil) {
	!lastExecuted ? false : (lastExecuted + allowedMil > new Date().time)
}


void logDebug(String msg) {
	if (state.debugLoggingEnabled != false) {
		log.debug "$msg"
	}
}

void logTrace(String msg) {
	// log.trace "$msg"
}