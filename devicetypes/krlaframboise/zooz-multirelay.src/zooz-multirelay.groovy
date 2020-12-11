/**
 *  Zooz MultiRelay v1.3.1
 *  (Models: ZEN16)
 *
 *  Author:
 *    Kevin LaFramboise (krlaframboise)
 *
 *	Documentation: https://community.smartthings.com/t/release-zooz-multirelay-zen16/181057
 *
 *  Changelog:
 *
 *    1.3.1 (09/16/2020)
 *      - Added option 2 for config params 12, 13, and 14. (FIRMWARE >= 1.02)
 *
 *    1.3 (09/02/2020)
 *      - Added support for firmware 1.03
 *
 *    1.2.1 (08/10/2020)
 *      - Added ST workaround for S2 Supervision bug with MultiChannel Devices.
 *
 *    1.2 (04/15/2020)
 *      - Added support for firmware 1.02
 *      - Fixed default parameters for new mobile app.
 *      - Changed new mobile app icon to switch
 *
 *    1.1.2 (04/10/2020)
 *      - Fixed time out issue in new mobile app, but to apply the fix you need to manually delete the child devices and then save the settings of the parent so that it re-creates them.
 *
 *    1.1.1 (03/13/2020)
 *      - Fixed bug with enum settings that was caused by a change ST made in the new mobile app.
 *
 *    1.1 (02/06/2020)
 *      - Added Auto On/Off Unit Setting for Relay (FIRMWARE >= 1.01)
 *      - Changed Auto On/off settings from enum to range because the unit is no longer fixed.
 *      - Create child devices with built-in Child Switch DTH only if the custom Child Switch DTH isn't installed.
 *      - Removed "Create Child Switch for ..." options so now a switch for each Relay will always be created.  Zooz wanted the child devices created by default, but the new mobile app has a bug with default values that would result in the child devices getting deleted every time the settings screen is opened.
 *
 *    1.0 (12/19/2019)
 *      - Initial Release
 *
 *
 *  Copyright 2020 Kevin LaFramboise (@krlaframboise)
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
import groovy.transform.Field

@Field static Map commandClassVersions = [
	0x20: 1,	// Basic
	0x25: 1,	// Switch Binary
	0x55: 1,	// Transport Service
	0x59: 1,	// AssociationGrpInfo
	0x5A: 1,	// DeviceResetLocally
	0x5E: 2,	// ZwaveplusInfo
	0x60: 3,	// Multi Channel
	0x6C: 1,	// Supervision
	0x70: 2,	// Configuration
	0x72: 2,	// ManufacturerSpecific
	0x73: 1,	// Powerlevel
	0x7A: 2,	// Firmware Update Md
	0x85: 2,	// Association
	0x86: 1,	// Version
	0x8E: 2,	// Multi Channel Association
	0x98: 1,	// Security 0
	0x9F: 1		// Security 2
]
 
metadata {
	definition (
		name: "Zooz MultiRelay",
		namespace: "krlaframboise",
		author: "Kevin LaFramboise",
		ocfDeviceType: "oic.d.switch",
		vid:"generic-switch"
	) {
		capability "Actuator"
		capability "Switch"
		capability "Outlet"
		capability "Light"
		capability "Configuration"
		capability "Refresh"
		capability "Health Check"

		attribute "firmwareVersion", "string"
		attribute "lastCheckIn", "string"

		(1..3).each {
			attribute "relay${it}Switch", "string"
			attribute "relay${it}Name", "string"

			command "relay${it}On"
			command "relay${it}Off"
		}

		fingerprint manufacturer: "027A", prod: "A000", model: "A00A", deviceJoinName: "Zooz MultiRelay"
	}

	simulator { }

	tiles(scale: 2) {
		multiAttributeTile(name:"switch", type: "generic", width: 6, height: 4){
			tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
				attributeState "on", label: '${name}', action: "switch.off", icon: "st.switches.switch.on", backgroundColor: "#00a0dc"
				attributeState "off", label: '${name}', action: "switch.on", icon: "st.switches.switch.off", backgroundColor: "#ffffff"
			}
		}

		standardTile("refresh", "device.refresh", width: 2, height: 2) {
			state "default", label:'Refresh', action: "refresh", icon:"st.secondary.refresh-icon"
		}
		standardTile("configure", "device.configure", width: 2, height: 2) {
			state "default", label:'Sync', action: "configure", icon:"st.secondary.tools"
		}
		valueTile("firmwareVersion", "device.firmwareVersion", decoration:"flat", width:3, height: 1) {
			state "firmwareVersion", label:'Firmware ${currentValue}'
		}
		valueTile("syncStatus", "device.syncStatus", decoration:"flat", width:2, height: 2) {
			state "syncStatus", label:'${currentValue}'
		}

		valueTile("relay1Name", "device.relay1Name", decoration:"flat", width:5, height: 1) {
			state "default", label:'${currentValue}'
		}
		standardTile("relay1Switch", "device.relay1Switch", width:1, height: 1) {
			state "on", label:'ON', action:"relay1Off", backgroundColor: "#00a0dc"
			state "off", label:'OFF', action:"relay1On"
		}

		valueTile("relay2Name", "device.relay2Name", decoration:"flat", width:5, height: 1) {
			state "default", label:'${currentValue}'
		}
		standardTile("relay2Switch", "device.relay2Switch", width:1, height: 1) {
			state "on", label:'ON', action:"relay2Off", backgroundColor: "#00a0dc"
			state "off", label:'OFF', action:"relay2On"
		}

		valueTile("relay3Name", "device.relay3Name", decoration:"flat", width:5, height: 1) {
			state "default", label:'${currentValue}'
		}
		standardTile("relay3Switch", "device.relay3Switch", width:1, height: 1) {
			state "on", label:'ON', action:"relay3Off", backgroundColor: "#00a0dc"
			state "off", label:'OFF', action:"relay3On"
		}


		main (["switch"])
		details(["switch", "refresh", "syncStatus", "configure", "relay1Name", "relay1Switch", "relay2Name", "relay2Switch", "relay3Name", "relay3Switch", "firmwareVersion"])
	}

	preferences {
		configParams.each {
			getOptionsInput(it)
		}
		
		input "debugLogging", "enum",
			title: "Logging:",
			required: false,
			defaultValue: "1",
			options: ["0":"Disabled", "1":"Enabled [DEFAULT]"]					
	}
}


private getOptionsInput(param) {
	if (param.options) {
		input "configParam${param.num}", "enum",
			title: "${param.name}:",
			required: false,
			defaultValue: param.value?.toString(),
			displayDuringSetup: true,
			options: param.options
	}
	else if (param.range) {
		input "configParam${param.num}", "number",
			title: "${param.name}:",
			required: false,
			defaultValue: param.value?.toString(),
			displayDuringSetup: true,
			range: param.range
	}
}


def installed () {
	initialize()
}

def updated() {
	if (!isDuplicateCommand(state.lastUpdated, 3000)) {
		state.lastUpdated = new Date().time

		initialize()

		refreshChildSwitches()

		executeConfigureCmds()
	}
}

private initialize() {
	if (!device.currentValue("switch")) {
		sendEvent(name: "switch", value: "off", displayed: false)
	}

	(1..3).each {
		if (!device.currentValue("relay${it}Switch")) {
			sendEvent(name: "relay${it}Switch", value: "off", displayed: false)
			sendEvent(name: "relay${it}Name", value: "Relay ${it}", displayed: false)
		}
	}

	if (!device.currentValue("checkInterval")) {
		def checkInterval = (6 * 60 * 60) + (5 * 60)
		sendEvent(name: "checkInterval", value: checkInterval, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])
	}

	unschedule()

	runEvery3Hours(ping)
}

private refreshChildSwitches() {
	(1..3).each {
		def childEnabled = true // settings ? settings["createRelay${it}"] : true
		def child = findChildByEndpoint(it)
		if (child && !childEnabled) {
			log.warn "Removing ${child.displayName}} "
			deleteChildDevice(child.deviceNetworkId)
			child = null
		}
		else if (!child && childEnabled) {
			child = addChildSwitch(it)
			child?.sendEvent(getEventMap("switch", device.currentValue("relay${it}Switch"), false))
		}

		def relayName = child ? child.displayName : "Relay ${it}"
		if (relayName != device.currentValue("relay${it}Name")) {
			sendEvent(getEventMap("relay${it}Name", relayName, false))
		}
	}
}

private addChildSwitch(endpoint) {
	def name = "Relay ${endpoint}"

	logDebug "Creating Child Switch for ${name}"
	
	return addChildDevice(
		"smartthings",
		"Child Switch",
		"${device.deviceNetworkId}:${endpoint}",
		null,
		[
			completedSetup: true,
			label: "${device.displayName}-${name}",
			isComponent: false,
			data: [endpoint: "${endpoint}"]
		]
	)
}


def configure() {
	logDebug "configure()..."

	if (state.resyncAll == null) {
		state.resyncAll = true

		runIn(4, refresh)
		runIn(8, executeConfigureCmds)
	}
	else {
		if (!pendingChanges) {
			state.resyncAll = true
		}
		executeConfigureCmds()
	}
}

def executeConfigureCmds() {	
	runIn(6, updateSyncStatus)
	
	def cmds = []

	if (state.resyncAll || !device.currentValue("firmwareVersion")) {
		cmds << versionGetCmd()
	}

	configParams.each {
		if (isParamSupported(it)) {
			def storedVal = getParamStoredValue(it.num)
			if (state.resyncAll || "${storedVal}" != "${it.value}") {
				if (state.configured) {
					logDebug "CHANGING ${it.name}(#${it.num}) from ${storedVal} to ${it.value}"
					cmds << configSetCmd(it)
				}
				cmds << configGetCmd(it)
			}
		}
	}

	if (cmds) {
		sendCommands(delayBetween(cmds, 250))
	}
}


def ping() {
	logDebug "ping()..."
	return sendCommands([ versionGetCmd() ])
}


def on() {
	logDebug "on()..."
	return [ switchBinarySetCmd(0xFF) ]
}


def off() {
	logDebug "off()..."
	return [ switchBinarySetCmd(0x00) ]
}


def relay1On() { relayOn(1) }
def relay2On() { relayOn(2) }
def relay3On() { relayOn(3) }

private relayOn(endpoint) {
	logDebug "relay${endpoint}On()..."
	executeChildOnOff(0xFF, endpoint)
}

def childOn(dni) {
	logDebug "childOn(${dni})..."
	executeChildOnOff(0xFF, getChildEndpoint(findChildByDNI(dni)))
}


def relay1Off() { relayOff(1) }
def relay2Off() { relayOff(2) }
def relay3Off() { relayOff(3) }

private relayOff(endpoint) {
	logDebug "relay${endpoint}Off()..."
	executeChildOnOff(0x00, endpoint)
}

def childOff(dni) {
	logDebug "childOff(${dni})..."
	executeChildOnOff(0x00, getChildEndpoint(findChildByDNI(dni)))
}

void executeChildOnOff(value, endpoint) {
	sendCommands([ switchBinarySetCmd(value, endpoint) ])
}


def refresh() {
	logDebug "refresh()..."

	refreshChildSwitches()

	def cmds = []
	(0..3).each {
		cmds << basicGetCmd(it)
	}
	sendCommands(delayBetween(cmds, 250))
	return []
}

private sendCommands(cmds) {
	def actions = []
	cmds?.each {
		actions << new physicalgraph.device.HubAction(it)
	}
	sendHubCommand(actions)
	return []
}


private versionGetCmd() {
	return secureCmd(zwave.versionV1.versionGet())
}

private basicGetCmd(endpoint=null) {
	return multiChannelCmdEncapCmd(zwave.basicV1.basicGet(), endpoint)
}

private switchBinaryGetCmd(endpoint=null) {
	return multiChannelCmdEncapCmd(zwave.switchBinaryV1.switchBinaryGet(), endpoint)
}

private switchBinarySetCmd(val, endpoint=null) {
	return multiChannelCmdEncapCmd(zwave.switchBinaryV1.switchBinarySet(switchValue: val), endpoint)
}

private configSetCmd(param) {
	return secureCmd(zwave.configurationV2.configurationSet(parameterNumber: param.num, size: param.size, scaledConfigurationValue: param.value))
}

private configGetCmd(param) {
	return secureCmd(zwave.configurationV2.configurationGet(parameterNumber: param.num))
}

private multiChannelCmdEncapCmd(cmd, endpoint) {
	if (endpoint) {
		return secureCmd(zwave.multiChannelV3.multiChannelCmdEncap(destinationEndPoint:safeToInt(endpoint)).encapsulate(cmd))
	}
	else {
		return secureCmd(cmd)
	}
}

private secureCmd(cmd) {
	if (zwaveInfo?.zw?.contains("s") || ("0x98" in device.rawDescription?.split(" "))) {
		return zwave.securityV1.securityMessageEncapsulation().encapsulate(cmd).format()
	}
	else {
		return cmd.format()
	}
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

		if (!isDuplicateCommand(state.lastCheckInTime, 60000)) {
			state.lastCheckInTime = new Date().time
			sendEvent(getEventMap("lastCheckIn", convertToLocalTimeString(new Date()), false))
		}
	}
	catch (e) {
		log.error "${e}"
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


def zwaveEvent(physicalgraph.zwave.commands.multichannelv3.MultiChannelCmdEncap cmd) {
	// Workaround that was added to all SmartThings Multichannel DTHs.
	if (cmd.commandClass == 0x6C && cmd.parameter.size >= 4) { // Supervision encapsulated Message
		// Supervision header is 4 bytes long, two bytes dropped here are the latter two bytes of the supervision header
		cmd.parameter = cmd.parameter.drop(2)
		// Updated Command Class/Command now with the remaining bytes
		cmd.commandClass = cmd.parameter[0]
		cmd.command = cmd.parameter[1]
		cmd.parameter = cmd.parameter.drop(2)
	}
	
	def encapsulatedCommand = cmd.encapsulatedCommand(commandClassVersions)

	if (encapsulatedCommand) {
		return zwaveEvent(encapsulatedCommand, cmd.sourceEndPoint)
	}
	else {
		logDebug "Unable to get encapsulated command: $cmd"
		return []
	}
}


def zwaveEvent(physicalgraph.zwave.commands.versionv1.VersionReport cmd) {
	logTrace "VersionReport: ${cmd}"

	def subVersion = String.format("%02d", cmd.applicationSubVersion)
	def fullVersion = "${cmd.applicationVersion}.${subVersion}"

	if (fullVersion != device.currentValue("firmwareVersion")) {
		sendEvent(getEventMap("firmwareVersion", fullVersion))
	}
	return []
}


def zwaveEvent(physicalgraph.zwave.commands.configurationv2.ConfigurationReport cmd) {
	state.configured = true

	updateSyncStatus("Syncing...")
	runIn(10, updateSyncStatus)

	def param = configParams.find { it.num == cmd.parameterNumber }
	if (param) {
		logDebug "${param.name}(#${param.num}) = ${cmd.scaledConfigurationValue}"
		setParamStoredValue(param.num, cmd.scaledConfigurationValue)
	}
	else {
		logDebug "Unknown Parameter #${cmd.parameterNumber} = ${cmd.scaledConfigurationValue}"
	}
	state.resyncAll = false
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
	if (device.currentValue("syncStatus") != status) {
		sendEvent(getEventMap("syncStatus", status, false))
	}
}

private getPendingChanges() {
	return (configParams.count { isConfigParamSynced(it) ? 0 : 1 })
}

private isConfigParamSynced(param) {
	return (!isParamSupported(param) || param.value == getParamStoredValue(param.num))
}

private isParamSupported(param) {
	return (!param.firmware || param.firmware <= firmwareVersion)
}

private getParamStoredValue(paramNum) {
	return safeToInt(state["configVal${paramNum}"], null)
}

private setParamStoredValue(paramNum, value) {
	state["configVal${paramNum}"] = value
}


def zwaveEvent(physicalgraph.zwave.commands.switchbinaryv1.SwitchBinaryReport cmd, endpoint=0) {
	logDebug "SwitchBinaryReport: ${cmd}" + (endpoint ? " (Endpoint ${endpoint})" : "")
	
	handleSwitchReport(cmd.value, endpoint)	
	return []
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd, endpoint=0) {
	logTrace "BasicReport: ${cmd}" + (endpoint ? " (Endpoint ${endpoint})" : "")
	
	handleSwitchReport(cmd.value, endpoint)	
	return []
}

private handleSwitchReport(rawValue, endpoint) {
	def value = (rawValue == 0xFF) ? "on" : "off"

	if (endpoint) {
		def child = findChildByEndpoint(endpoint)
		if (child) {
			def desc = "${child.displayName}: switch is ${value}"
			logDebug "${desc}"
			child.sendEvent(name: "switch", value: value, descriptionText: desc)
		}

		sendEvent(getEventMap("relay${endpoint}Switch", value, !child))
	}
	else {
		sendEvent(getEventMap("switch", value))
	}	
}


def zwaveEvent(physicalgraph.zwave.Command cmd, endpoint=null) {
	logDebug "Unhandled zwaveEvent: $cmd" + (endpoint ? " (Endpoint ${endpoint})" : "")
	return []
}


// Configuration Parameters
private getConfigParams() {
	return [
		powerFailureRecoveryParam,
		relay1TypeParam,
		relay2TypeParam,
		relay3TypeParam,
		ledIndicatorModeParam,
		relay1AutoOffParam,
		relay1AutoOffUnitParam,
		relay1AutoOnParam,
		relay1AutoOnUnitParam,
		relay2AutoOffParam,
		relay2AutoOffUnitParam,
		relay2AutoOnParam,
		relay2AutoOnUnitParam,
		relay3AutoOffParam,
		relay3AutoOffUnitParam,
		relay3AutoOnParam,
		relay3AutoOnUnitParam,
		relay1ManualControlParam,
		relay2ManualControlParam,
		relay3ManualControlParam,
		relay1BehaviorParam,
		relay2BehaviorParam,
		relay3BehaviorParam,
		dcMotorModeParam
	]
}

private getPowerFailureRecoveryParam() {
	def options = [
		0:"Turn All Relays Off",
		1:"Restore Relay States From Before Power Failure",
		2:"Turn All Relays On",
		3:"Restore Relay 1 and Relay 2 States and Turn Relay 3 Off",
		4:"Restore Relay 1 and Relay 2 States and Turn Relay 3 On"
	]
	return getParam(1, "On/Off Status Recovery After Power Failure", 1, 1, options)
}

private getRelay1TypeParam() {
	return getRelayTypeParam(2, 1)
}
private getRelay2TypeParam() {
	return getRelayTypeParam(3, 2)
}
private getRelay3TypeParam() {
	return getRelayTypeParam(4, 3)
}
private getRelayTypeParam(num, relay) {
	def options = [
		0:"Momentary Switch",
		1:"Toggle Switch",
		2:"Toggle Switch (any change)",
		3:"Garage Door (FIRMWARE >= 1.02)"
	]
	return getParam(num, "Switch Type for Relay ${relay}", 1, 2, options)
}

private getLedIndicatorModeParam() {
	def options = [
		0:"On when ALL Relays are Off", 
		1:"On when ANY Relay is On",
		2:"Always Off",
		3:"Always On"
	]
	return getParam(5, "LED Indicator Control", 1, 0, options)
}

private getRelay1AutoOffParam() {
	return getAutoOnOffParam(6, "Off", 1)
}
private getRelay1AutoOnParam() {
	return getAutoOnOffParam(7, "On", 1)
}
private getRelay2AutoOffParam() {
	return getAutoOnOffParam(8, "Off", 2)
}
private getRelay2AutoOnParam() {
	return getAutoOnOffParam(9, "On", 2)
}
private getRelay3AutoOffParam() {
	return getAutoOnOffParam(10, "Off", 3)
}
private getRelay3AutoOnParam() {
	return getAutoOnOffParam(11, "On", 3)
}
private getAutoOnOffParam(num, onOff, relay) {
	return getParam(num, "Auto Turn-${onOff} Timer for Relay ${relay} (0=Disabled, 1-65535)", 4, 0, null, "0..65535")
}

private getRelay1ManualControlParam() {
	return getRelayManualControlParam(12, 1)
}
private getRelay2ManualControlParam() {
	return getRelayManualControlParam(13, 2)
}
private getRelay3ManualControlParam() {
	return getRelayManualControlParam(14, 3)
}
private getRelayManualControlParam(num, relay) {
	def options = [
		0:"Disabled", 
		1:"Enabled", 
		2:"Disabled with On/Off Reporting (FIRMWARE >= 1.02)"
	]
	return getParam(num, "Manual Control for Relay ${relay}", 1, 1, options)
}


private getRelay1AutoOffUnitParam() {
	return getAutoOnOffUnitParam(15, "Off", 1)
}
private getRelay1AutoOnUnitParam() {
	return getAutoOnOffUnitParam(16, "On", 1)
}
private getRelay2AutoOffUnitParam() {
	return getAutoOnOffUnitParam(17, "Off", 2)
}
private getRelay2AutoOnUnitParam() {
	return getAutoOnOffUnitParam(18, "On", 2)
}
private getRelay3AutoOffUnitParam() {
	return getAutoOnOffUnitParam(19, "Off", 3)
}
private getRelay3AutoOnUnitParam() {
	return getAutoOnOffUnitParam(20, "On", 3)
}
private getAutoOnOffUnitParam(num, onOff, relay) {
	def options = [
		0:"Minutes",
		1:"Seconds (FIRMWARE >= 1.01)",
		2:"Hours (FIRMWARE >= 1.01)"
	]
	return getParam(num, "Auto Turn-${onOff} Timer Unit for Relay ${relay}", 1, 0, options, null, 1.01)
}


private getRelay1BehaviorParam() {
	return getRelayBehaviorParam(21, 1)
}
private getRelay2BehaviorParam() {
	return getRelayBehaviorParam(22, 2)
}
private getRelay3BehaviorParam() {
	return getRelayBehaviorParam(23, 3)
}
private getRelayBehaviorParam(num, relay) {
	def options = [
		0:"NO (reports on when switch on)",
		1:"NC (reports on when switch off)",
		2:"NC (reports on when switch on)"
	]
	return getParam(num, "Relay ${relay} Behavior (FIRMWARE >= 1.03)", 1, 0, options, null, 1.03)
}


private getDcMotorModeParam() {
	return getParam(24, "DC Motor Mode (FIRMWARE >= 1.03)", 1, 0, [0:"Disabled", 1:"Enabled"], null, 1.03)
}


private getParam(num, name, size, defaultVal, options=null, range=null, firmware=null) {
	def val = safeToInt((settings ? settings["configParam${num}"] : null), defaultVal)

	def map = [num: num, name: name, size: size, value: val]
	if (options) {
		map.options = setDefaultOption(options, defaultVal)
	}

	if (range) map.range = range

	if (firmware) map.firmware = firmware

	return map
}

private setDefaultOption(options, defaultVal) {	
	options?.each {
		if (it.key == defaultVal) {
			it.value = "${it.value} [DEFAULT]"
		}
	}	
	return options
}


private getEventMap(name, value, displayed=true) {
	def desc = "${device.displayName}: ${name} is ${value}"

	def eventMap = [
		name: name,
		value: value,
		displayed: displayed,
		descriptionText: "${desc}"
	]

	if (displayed) {
		logDebug "${desc}"
	}
	else {
		logTrace "${desc}"
	}
	return eventMap
}


private getFirmwareVersion() {
	return safeToDec(device.currentValue("firmwareVersion"))
}

private findChildByEndpoint(endpoint) {
	return childDevices?.find { getChildEndpoint(it) == endpoint }
}

private findChildByDNI(dni) {
	return childDevices?.find { it.deviceNetworkId == dni }
}

private getChildEndpoint(child) {
	return child ? safeToInt(child.getDataValue("endpoint")) : 0
}


private safeToInt(val, defaultVal=0) {
	if ("${val}"?.isInteger()) {
		return "${val}".toInteger()
	}
	else if ("${val}".isDouble()) {
		return "${val}".toDouble()?.round()
	}
	else {
		return  defaultVal
	}
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

void logDebug(msg) {
	if (safeToInt(settings?.debugLogging, 1)) {
		log.debug "$msg"
	}
}

private logTrace(msg) {
	// log.trace "$msg"
}