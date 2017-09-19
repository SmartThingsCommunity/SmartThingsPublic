/**
 *  Yale Z-Wave Lock
 *
 *  Copyright 2015 SmartThings
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
	definition (name: "Yale Z-Wave Lock", namespace: "smartthings", author: "SmartThings") {
		capability "Actuator"
		capability "Lock"
		capability "Polling"
		capability "Refresh"
		capability "Sensor"
		capability "Lock Codes"
		capability "Battery"
		capability "Health Check"
		capability "Configuration"

		input "audioLevel", "enum", title: "Volume Setting", options: ["Silent", "Low", "High"], defaultValue: getDefaultValueFor("audioLevel"), multiple: false, required: false
		input "autoLock", "enum", title: "Auto-Lock", options: ["On", "Off"], defaultValue: getDefaultValueFor("autoLock"), multiple: false, required: false
		input "relockTime", "number", title: "Re-Lock Time (5-180 seconds)", defaultValue: getDefaultValueFor("relockTime"), range: "5..180", displayDuringSetup: false, required: false
		input "wrongCodeEntryLimit", "number", title: "Wrong Code Entry Limit (3-7 Entries)", defaultValue: getDefaultValueFor("wrongCodeEntryLimit"), range: "3..7", displayDuringSetup: false, required: false
		input "wrongCodeTimeOut", "number", title: "Wrong Code Time Out (10-180 seconds)", defaultValue: getDefaultValueFor("wrongCodeTimeOut"), range: "10..180", displayDuringSetup: false, required: false
		input "language", "enum", title: "Language Setting", options: ["English", "Spanish", "French"], defaultValue: getDefaultValueFor("language"), multiple: false, required: false
		input "operatingMode", "enum", title: "Operating Mode", options: ["Normal", "Vacation", "Privacy"], defaultValue: getDefaultValueFor("operatingMode"), multiple: false, required: false
		input "oneTouchLocking", "enum", title: "One Touch Locking", options: ["On", "Off"], defaultValue: getDefaultValueFor("oneTouchLocking"), multiple: false, required: false
		input "privacyButton", "enum", title: "Privacy Mode (Button)", options: ["On", "Off"], defaultValue: getDefaultValueFor("privacyButton"), multiple: false, required: false

		fingerprint mfr:"0129", prod:"0002", model:"0800", deviceJoinName: "Yale Touchscreen Deadbolt Door Lock" // YRD120
		fingerprint mfr:"0129", prod:"0002", model:"0000", deviceJoinName: "Yale Touchscreen Deadbolt Door Lock" // YRD220, YRD240
		fingerprint mfr:"0129", prod:"0002", model:"FFFF", deviceJoinName: "Yale Touchscreen Lever Door Lock" // YRD220
		fingerprint mfr:"0129", prod:"0004", model:"0800", deviceJoinName: "Yale Push Button Deadbolt Door Lock" // YRD110
		fingerprint mfr:"0129", prod:"0004", model:"0000", deviceJoinName: "Yale Push Button Deadbolt Door Lock" // YRD210
		fingerprint mfr:"0129", prod:"0001", model:"0000", deviceJoinName: "Yale Push Button Lever Door Lock" // YRD210
		fingerprint mfr:"0129", prod:"8002", model:"0600", deviceJoinName: "Yale Assure Lock with Bluetooth"
	}

	simulator {
		status "locked": "command: 9881, payload: 00 62 03 FF 00 00 FE FE"
		status "unlocked": "command: 9881, payload: 00 62 03 00 00 00 FE FE"

		reply "9881006201FF,delay 4200,9881006202": "command: 9881, payload: 00 62 03 FF 00 00 FE FE"
		reply "988100620100,delay 4200,9881006202": "command: 9881, payload: 00 62 03 00 00 00 FE FE"
	}

	tiles(scale: 2) {
		multiAttributeTile(name:"toggle", type: "generic", width: 6, height: 4){
			tileAttribute ("device.lock", key: "PRIMARY_CONTROL") {
				attributeState "locked", label:'locked', action:"lock.unlock", icon:"st.locks.lock.locked", backgroundColor:"#00A0DC", nextState:"unlocking"
				attributeState "unlocked", label:'unlocked', action:"lock.lock", icon:"st.locks.lock.unlocked", backgroundColor:"#ffffff", nextState:"locking"
				attributeState "unknown", label:"unknown", action:"lock.lock", icon:"st.locks.lock.unknown", backgroundColor:"#ffffff", nextState:"locking"
				attributeState "locking", label:'locking', icon:"st.locks.lock.locked", backgroundColor:"#00A0DC"
				attributeState "unlocking", label:'unlocking', icon:"st.locks.lock.unlocked", backgroundColor:"#ffffff"
			}
		}
		standardTile("lock", "device.lock", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "default", label:'lock', action:"lock.lock", icon:"st.locks.lock.locked", nextState:"locking"
		}
		standardTile("unlock", "device.lock", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "default", label:'unlock', action:"lock.unlock", icon:"st.locks.lock.unlocked", nextState:"unlocking"
		}
		valueTile("battery", "device.battery", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "battery", label:'${currentValue}% battery', unit:""
		}
		standardTile("refresh", "device.lock", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "default", label:'', action:"refresh.refresh", icon:"st.secondary.refresh"
		}

		main "toggle"
		details(["toggle", "lock", "unlock", "battery", "refresh"])
	}
}

import physicalgraph.zwave.commands.doorlockv1.*
import physicalgraph.zwave.commands.usercodev1.*

/**
 * Constructs a map of the configurable parameters in a lock
 *
 * @return map: The map with key and values for parameter number, size, and allowed values
 */
def getLockParams() {
	// The following is for the Yale ZWave Locks
	// map is as follows: parameter number, byte size, allowed value choices or number range
	def map = [
		audioLevel: 		[ number: 1, size: 1, value: [ "silent": 1, "low": 2, "high": 3 ]],
		autoLock: 			[ number: 2, size: 1, value: [ "off": 0x00, "on": 0xFF ]],
		relockTime: 		[ number: 3, size: 1, value: settings.relockTime ],
		wrongCodeEntryLimit:[ number: 4, size: 1, value: settings.wrongCodeEntryLimit ],
		language:           [ number: 5, size: 1, value: [ "english": 1, "spanish": 2, "french": 3 ]],
		wrongCodeTimeOut:   [ number: 7, size: 1, value: settings.wrongCodeTimeOut ],
		operatingMode:      [ number: 8, size: 1, value: [ "normal": 00, "vacation": 01, "privacy": 02 ]],
		oneTouchLocking:    [ number: 11, size: 1, value: [ "off": 0x00, "on": 0xFF ]],
		privacyButton:      [ number: 12, size: 1, value: [ "off": 0x00, "on": 0xFF ]],
	]

	return map
}

/**
 * Returns the default value for the configurable parameters in a lock
 *
 * @param settingName: The settings key
 *
 * @return The default value based on the settings key
 */
def getDefaultValueFor(settingName) {
	if("audioLevel".equals(settingName)) {
		return "High"
	}
	if("autoLock".equals(settingName)) {
		return "Off"
	}
	if("relockTime".equals(settingName)) {
		return 5
	}
	if("wrongCodeEntryLimit".equals(settingName)) {
		return 5
	}
	if("wrongCodeTimeOut".equals(settingName)) {
		return 60
	}
	if("language".equals(settingName)) {
		return "English"
	}
	if("operatingMode".equals(settingName)) {
		return "Normal"
	}
	if("oneTouchLocking".equals(settingName)) {
		return "On"
	}
	if("privacyButton".equals(settingName)) {
		return "Off"
	}
	return null
}

/**
 * Reads the values of the parameters defined in the DTH preferences
 *
 * @param settingName: The settings key
 *
 * @param param: The map with parameter number, size, and allowed values of the configurable parameters in a lock
 *
 * @return configVal: The list of values
 */
def getConfigValueFor(settingName, param) {
	def configVal = []
	//Audio Level
	if("audioLevel".equals(settingName)) {
		if(settings.audioLevel == null) {
			log.warn "User set audioLevel to null. Using default value ${getDefaultValueFor('audioLevel')}."
			configVal << param.audioLevel.value[getDefaultValueFor("audioLevel").toLowerCase()]
		} else {
			configVal << param.audioLevel.value."${settings.audioLevel.toLowerCase()}"
		}
	}
	//Auto Lock
	if("autoLock".equals(settingName)) {
		if(settings.autoLock == null) {
			log.warn "User set autoLock to null. Using default value ${getDefaultValueFor('autoLock')}."
			configVal << param.autoLock.value[getDefaultValueFor("autoLock").toLowerCase()]
		} else {
			configVal << param.autoLock.value."${settings.autoLock.toLowerCase()}"
		}
	}
	//Relock Time
	if("relockTime".equals(settingName)) {
		if(settings.relockTime == null) {
			log.warn "User set relockTime to null. Using default value ${getDefaultValueFor('relockTime')}."
			configVal << getDefaultValueFor("relockTime")
		} else {
			configVal << param.relockTime.value
		}
	}
	//Wrong Code Entry Limit
	if("wrongCodeEntryLimit".equals(settingName)) {
		if(settings.wrongCodeEntryLimit == null) {
			log.warn "User set wrongCodeEntryLimit to null. Using default value ${getDefaultValueFor('wrongCodeEntryLimit')}."
			configVal << getDefaultValueFor("wrongCodeEntryLimit")
		} else {
			configVal << param.wrongCodeEntryLimit.value
		}
	}
	//Wrong Code Time Out
	if("wrongCodeTimeOut".equals(settingName)) {
		if(settings.wrongCodeTimeOut == null) {
			log.warn "User set wrongCodeTimeOut to null. Using default value ${getDefaultValueFor('wrongCodeTimeOut')}."
			configVal << getDefaultValueFor("wrongCodeTimeOut")
		} else {
			configVal << param.wrongCodeTimeOut.value
		}
	}
	//Language
	if("language".equals(settingName)) {
		if(settings.language == null) {
			log.warn "User set language to null. Using default value ${getDefaultValueFor('language')}."
			configVal << param.language.value[getDefaultValueFor("language").toLowerCase()]
		} else {
			configVal << param.language.value."${settings.language.toLowerCase()}"
		}
	}
	//Operating Mode
	if("operatingMode".equals(settingName)) {
		if(settings.operatingMode == null) {
			log.warn "User set operatingMode to null. Using default value ${getDefaultValueFor('operatingMode')}."
			configVal << param.operatingMode.value[getDefaultValueFor("operatingMode").toLowerCase()]
		} else {
			configVal << param.operatingMode.value."${settings.operatingMode.toLowerCase()}"
		}
	}
	//One Touch Locking
	if("oneTouchLocking".equals(settingName)) {
		if(settings.oneTouchLocking == null) {
			log.warn "User set oneTouchLocking to null. Using default value ${getDefaultValueFor('oneTouchLocking')}."
			configVal << param.oneTouchLocking.value[getDefaultValueFor("oneTouchLocking").toLowerCase()]
		} else {
			configVal << param.oneTouchLocking.value."${settings.oneTouchLocking.toLowerCase()}"
		}
	}
	//Privacy Button
	if("privacyButton".equals(settingName)) {
		if(settings.privacyButton == null) {
			log.warn "User set privacyButton to null. Using default value ${getDefaultValueFor('privacyButton')}."
			configVal << param.privacyButton.value[getDefaultValueFor("privacyButton").toLowerCase()]
		} else {
			configVal << param.privacyButton.value."${settings.privacyButton.toLowerCase()}"
		}
	}
	configVal
}

/**
 * Called on app installed
 */
def installed() {
	// Device-Watch pings if no device events received for 1 hour (checkInterval)
	sendEvent(name: "checkInterval", value: 1 * 60 * 60, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])
}

/**
 * Executed when the user taps on the 'Done' button on the device settings screen. Sends the values to lock.
 *
 * @return hubAction: The commands to be executed
 */
def updated() {
	// Workaround for ST calling updated twice ---- start
	if ( state.updatedRan == 0 || state.updatedRan == null ) {
		state.updatedRan = 1
		log.trace "[DTH] Executing 'updated()' for device ${this.device.displayName}"
	} else {
		state.updatedRan = 0
		return null
	}
	// Workaround for ST calling updated twice ---- end
	
	// Device-Watch pings if no device events received for 1 hour (checkInterval)
	sendEvent(name: "checkInterval", value: 1 * 60 * 60, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])
	def hubAction = null
	try {
		def cmds = []
		if (!state.init || !state.configured) {
			state.init = true
			log.debug "Returning commands for lock operation get and battery get"
			if (!state.configured) {
				cmds << doConfigure()
			}
			cmds << refresh()
			cmds << reloadAllCodes()
			if (!state.MSR) {
				cmds << zwave.manufacturerSpecificV1.manufacturerSpecificGet().format()
			}
			if (!state.fw) {
				cmds << zwave.versionV1.versionGet().format()
			}
			hubAction = response(delayBetween(cmds, 4200))
		} else {
			def param = getLockParams()
			// audioLevel
			cmds << secure(zwave.configurationV2.configurationSet(parameterNumber: param.audioLevel.number, size: param.audioLevel.size, configurationValue: getConfigValueFor("audioLevel", param)))
			// autoLock
			cmds << secure(zwave.configurationV2.configurationSet(parameterNumber: param.autoLock.number, size: param.autoLock.size, configurationValue: getConfigValueFor("autoLock", param)))
			// relockTime
			cmds << secure(zwave.configurationV2.configurationSet(parameterNumber: param.relockTime.number, size: param.relockTime.size, configurationValue: getConfigValueFor("relockTime", param)))
			// wrongCodeEntryLimit
			cmds << secure(zwave.configurationV2.configurationSet(parameterNumber: param.wrongCodeEntryLimit.number, size: param.wrongCodeEntryLimit.size, configurationValue: getConfigValueFor("wrongCodeEntryLimit", param)))
			// wrongCodeTimeOut
			cmds << secure(zwave.configurationV2.configurationSet(parameterNumber: param.wrongCodeTimeOut.number, size: param.wrongCodeTimeOut.size, configurationValue: getConfigValueFor("wrongCodeTimeOut", param)))
			// language
			cmds << secure(zwave.configurationV2.configurationSet(parameterNumber: param.language.number, size: param.language.size, configurationValue: getConfigValueFor("language", param)))
			// operatingMode
			cmds << secure(zwave.configurationV2.configurationSet(parameterNumber: param.operatingMode.number, size: param.operatingMode.size, configurationValue: getConfigValueFor("operatingMode", param)))
			// oneTouchLocking
			cmds << secure(zwave.configurationV2.configurationSet(parameterNumber: param.oneTouchLocking.number, size: param.oneTouchLocking.size, configurationValue: getConfigValueFor("oneTouchLocking", param)))
			// privacyButton
			cmds << secure(zwave.configurationV2.configurationSet(parameterNumber: param.privacyButton.number, size: param.privacyButton.size, configurationValue: getConfigValueFor("privacyButton", param)))
			log.debug "cmds=$cmds"
			hubAction = response(delayBetween(cmds, 200))
		}
	} catch (e) {
		log.warn "updated() threw $e"
	}
	hubAction
}

/**
 * Configures the device to settings needed by SmarthThings at device discovery time
 *
 */
def configure() {
	log.trace "[DTH] Executing 'configure()' for device ${device.displayName}"
	def cmds = doConfigure()
	log.debug "Configure returning with commands := $cmds"
	cmds
}

/**
 * Returns the list of commands to be executed when the device is being configured/paired
 *
 */
def doConfigure() {
	log.trace "[DTH] Executing 'doConfigure()' for device ${device.displayName}"
	state.configured = true
	def cmds = secureSequence([zwave.doorLockV1.doorLockOperationGet(), zwave.batteryV1.batteryGet()])
	log.debug "Do configure returning with commands := $cmds"
	cmds
}

/**
 * Responsible for parsing incoming device messages to generate events
 *
 * @param description: The incoming description from the device
 *
 * @return result: The list of events to be sent out
 *
 */
def parse(String description) {
	log.trace "[DTH] Executing 'parse(String description)' for device ${device.displayName} with description = $description"

	def result = null
	if (description.startsWith("Err")) {
		if (state.sec) {
			result = createEvent(descriptionText:description, isStateChange:true, displayed:false)
		} else {
			result = createEvent(
					descriptionText: "This lock failed to complete the network security key exchange. If you are unable to control it via SmartThings, you must remove it from your network and add it again.",
					eventType: "ALERT",
					name: "secureInclusion",
					value: "failed",
					displayed: true,
					)
		}
	} else {
		def cmd = zwave.parse(description, [ 0x98: 1, 0x72: 2, 0x85: 2, 0x86: 1 ])
		if (cmd) {
			result = zwaveEvent(cmd)
		}
	}
	log.info "[DTH] parse() - returning result=$result"
	result
}

/**
 * Responsible for parsing SecurityMessageEncapsulation command
 *
 * @param cmd: The SecurityMessageEncapsulation command to be parsed
 *
 * @return The event(s) to be sent out
 *
 */
def zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityMessageEncapsulation cmd) {
	log.trace "[DTH] Executing 'zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityMessageEncapsulation)' with cmd = $cmd"
	def encapsulatedCommand = cmd.encapsulatedCommand([0x62: 1, 0x71: 2, 0x80: 1, 0x85: 2, 0x63: 1, 0x98: 1, 0x86: 1])
	if (encapsulatedCommand) {
		zwaveEvent(encapsulatedCommand)
	}
}

/**
 * Responsible for parsing NetworkKeyVerify command
 *
 * @param cmd: The NetworkKeyVerify command to be parsed
 *
 * @return The event(s) to be sent out
 *
 */
def zwaveEvent(physicalgraph.zwave.commands.securityv1.NetworkKeyVerify cmd) {
	log.trace "[DTH] Executing 'zwaveEvent(physicalgraph.zwave.commands.securityv1.NetworkKeyVerify)' with cmd = $cmd"
	createEvent(name:"secureInclusion", value:"success", descriptionText:"Secure inclusion was successful", isStateChange: true)
}

/**
 * Responsible for parsing SecurityCommandsSupportedReport command
 *
 * @param cmd: The SecurityCommandsSupportedReport command to be parsed
 *
 * @return The event(s) to be sent out
 *
 */
def zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityCommandsSupportedReport cmd) {
	log.trace "[DTH] Executing 'zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityCommandsSupportedReport)' with cmd = $cmd"
	state.sec = cmd.commandClassSupport.collect { String.format("%02X ", it) }.join()
	if (cmd.commandClassControl) {
		state.secCon = cmd.commandClassControl.collect { String.format("%02X ", it) }.join()
	}
	createEvent(name:"secureInclusion", value:"success", descriptionText:"Lock is securely included", isStateChange: true)
}

/**
 * Responsible for parsing DoorLockOperationReport command
 *
 * @param cmd: The DoorLockOperationReport command to be parsed
 *
 * @return The event(s) to be sent out
 *
 */
def zwaveEvent(DoorLockOperationReport cmd) {
	log.trace "[DTH] Executing 'zwaveEvent(DoorLockOperationReport)' with cmd = $cmd"
	def result = []
	
	unschedule("followupStateCheck")
	unschedule("stateCheck")
	
	def map = [ name: "lock" ]
	map.data = [ lockName: device.displayName ]
	if (cmd.doorLockMode == 0xFF) {
		map.value = "locked"
		map.descriptionText = "Locked"
	} else if (cmd.doorLockMode >= 0x40) {
		map.value = "unknown"
		map.descriptionText = "Unknown state"
	} else {
		map.value = "unlocked"
		map.descriptionText = "Unlocked"
		if (state.assoc != zwaveHubNodeId) {
			result << response(secure(zwave.associationV1.associationSet(groupingIdentifier:1, nodeId:zwaveHubNodeId)))
			result << response(zwave.associationV1.associationSet(groupingIdentifier:2, nodeId:zwaveHubNodeId))
			result << response(secure(zwave.associationV1.associationGet(groupingIdentifier:1)))
		}
	}
	result ? [createEvent(map), *result] : createEvent(map)
}

/**
 * Responsible for parsing AlarmReport command
 *
 * @param cmd: The AlarmReport command to be parsed
 *
 * @return The event(s) to be sent out
 *
 */
def zwaveEvent(physicalgraph.zwave.commands.alarmv2.AlarmReport cmd) {
	log.trace "[DTH] Executing 'zwaveEvent(physicalgraph.zwave.commands.alarmv2.AlarmReport)' with cmd = $cmd"
	def result = []
	
	if (cmd.zwaveAlarmType == 6) {
		result = handleAccessAlarmReport(cmd)
	} else if (cmd.zwaveAlarmType == 7) {
		result = handleBurglarAlarmReport(cmd)
	} else if(cmd.zwaveAlarmType == 8) {
		result = handleBatteryAlarmReport(cmd)
	} else {
		result = handleAlarmReportUsingAlarmType(cmd)
	}
		
	result = result ?: null
	log.debug "[DTH] zwaveEvent(physicalgraph.zwave.commands.alarmv2.AlarmReport) returning with result = $result"
	result
}

/**
 * Responsible for handling Access AlarmReport command
 *
 * @param cmd: The AlarmReport command to be parsed
 *
 * @return The event(s) to be sent out
 *
 */
private def handleAccessAlarmReport(cmd) {
	log.trace "[DTH] Executing 'handleAccessAlarmReport' with cmd = $cmd"
	def result = []
	def map = null
	def codeID, changeType, lockCodes, codeName
	def deviceName = device.displayName
	lockCodes = loadLockCodes()
	if (1 <= cmd.zwaveAlarmEvent && cmd.zwaveAlarmEvent < 10) {
		map = [ name: "lock", value: (cmd.zwaveAlarmEvent & 1) ? "locked" : "unlocked" ]
	}
	switch(cmd.zwaveAlarmEvent) {
		case 1: // Manually locked
			map.descriptionText = "Locked manually"
			map.data = [ method: (cmd.alarmLevel == 2) ? "keypad" : "manual" ]
			break
		case 2: // Manually unlocked
			map.descriptionText = "Unlocked manually"
			map.data = [ method: "manual" ]
			break
		case 3: // Locked by command
			map.descriptionText = "Locked"
			map.data = [ method: "command" ]
			break
		case 4: // Unlocked by command
			map.descriptionText = "Unlocked"
			map.data = [ method: "command" ]
			break
		case 5: // Locked with keypad
			if (cmd.eventParameter || cmd.alarmLevel) {
				codeID = cmd.eventParameter[2] ?: cmd.alarmLevel
				codeName = getCodeName(lockCodes, codeID)
				map.descriptionText = "\"$codeName\" locked the lock"
				map.data = [ usedCode: codeID, codeName: codeName, method: "keypad" ]
			}
			break
		case 6: // Unlocked with keypad
			if (cmd.eventParameter || cmd.alarmLevel) {
				codeID = cmd.eventParameter[2] ?: cmd.alarmLevel
				codeName = getCodeName(lockCodes, codeID)
				map.descriptionText = "\"$codeName\" unlocked the lock"
				map.data = [ usedCode: codeID, codeName: codeName, method: "keypad" ]
			}
			break
		case 7:
			map = [ name: "lock", value: "unknown", descriptionText: "Unknown state" ]
			map.data = [ method: "manual" ]
			break
		case 8:
			map = [ name: "lock", value: "unknown", descriptionText: "Unknown state" ]
			map.data = [ method: "command" ]
			break
		case 9: // Auto locked
			map = [ name: "lock", value: "locked", data: [ method: "auto" ] ]
			map.descriptionText = "Auto locked"
			break
		case 0xA:
			map = [ name: "lock", value: "unknown", descriptionText: "Unknown state" ]
			map.data = [ method: "auto" ]
			break
		case 0xB:
			map = [ name: "lock", value: "unknown", descriptionText: "Unknown state" ]
			break
		case 0xC: // All user codes deleted
			result = allCodesDeletedEvent()
			map = [ name: "codeChanged", value: "all deleted", descriptionText: "Deleted all user codes", isStateChange: true ]
			map.data = [notify: true, notificationText: "Deleted all user codes in $deviceName at ${location.name}"]
			result << createEvent(name: "lockCodes", value: util.toJson([:]), displayed: false, descriptionText: "'lockCodes' attribute updated")
			break
		case 0xD: // User code deleted
			if (cmd.eventParameter || cmd.alarmLevel) {
				codeID = cmd.eventParameter[2] ?: cmd.alarmLevel
				codeName = getCodeName(lockCodes, codeID)
				map = [ name: "codeChanged", value: "$codeID deleted", isStateChange: true ]
				map.descriptionText = "Deleted \"$codeName\""
				map.data = [ codeName: codeName, notify: true, notificationText: "Deleted \"$codeName\" in $deviceName at ${location.name}" ]
				result << codeDeletedEvent(lockCodes, codeID)
			}
			break
		case 0xE: // Master or user code changed/set
			if (cmd.eventParameter || cmd.alarmLevel) {
				codeID = cmd.eventParameter[2] ?: cmd.alarmLevel
				codeName = getCodeNameFromState(lockCodes, codeID)
				changeType = getChangeType(lockCodes, codeID)
				map = [ name: "codeChanged", value: "$codeID $changeType",  descriptionText: "${getStatusForDescription(changeType)} \"$codeName\"", isStateChange: true ]
				map.data = [ codeName: codeName, notify: true, notificationText: "${getStatusForDescription(changeType)} \"$codeName\" in $deviceName at ${location.name}" ]
				if(!isMasterCode(codeID)) {
					result << codeSetEvent(lockCodes, codeID, codeName)
				} else {
					map.descriptionText = "${getStatusForDescription('set')} \"$codeName\""
					map.data.notificationText = "${getStatusForDescription('set')} \"$codeName\" in $deviceName at ${location.name}"
				}
			}
			break
		case 0xF: // Duplicate Pin-code error
			if (cmd.eventParameter || cmd.alarmLevel) {
				codeID = cmd.eventParameter[2] ?: cmd.alarmLevel
				clearStateForSlot(codeID)
				map = [ name: "codeChanged", value: "$codeID failed", descriptionText: "User code is duplicate and not added",
					isStateChange: true, data: [isCodeDuplicate: true] ]
			}
			break
		case 0x10: // Tamper Alarm
		case 0x13:
			map = [ name: "tamper", value: "detected", descriptionText: "Keypad attempts exceed code entry limit", isStateChange: true, displayed: true ]
			break
		case 0x11: // Keypad busy
			map = [ descriptionText: "Keypad is busy" ]
			break
		case 0x12: // Master code changed
			codeName = getCodeNameFromState(lockCodes, 0)
			map = [ name: "codeChanged", value: "0 set", descriptionText: "${getStatusForDescription('set')} \"$codeName\"", isStateChange: true ]
			map.data = [ codeName: codeName, notify: true, notificationText: "${getStatusForDescription('set')} \"$codeName\" in $deviceName at ${location.name}" ]
			break
		case 0xFE:
			// delegating it to handleAlarmReportUsingAlarmType
			return handleAlarmReportUsingAlarmType(cmd)
		default:
			// delegating it to handleAlarmReportUsingAlarmType
			return handleAlarmReportUsingAlarmType(cmd)
	}
	
	if (map) {
		if (map.data) {
			map.data.lockName = deviceName
		} else {
			map.data = [ lockName: deviceName ]
		}
		result << createEvent(map)
	}
	result = result.flatten()
	result
}

/**
 * Responsible for handling Burglar AlarmReport command
 *
 * @param cmd: The AlarmReport command to be parsed
 *
 * @return The event(s) to be sent out
 *
 */
private def handleBurglarAlarmReport(cmd) {
	log.trace "[DTH] Executing 'handleBurglarAlarmReport' with cmd = $cmd"
	def result = []
	def deviceName = device.displayName
	
	def map = [ name: "tamper", value: "detected" ]
	map.data = [ lockName: deviceName ]
	switch (cmd.zwaveAlarmEvent) {
		case 0:
			map.value = "clear"
			map.descriptionText = "Tamper alert cleared"
			break
		case 1:
		case 2:
			map.descriptionText = "Intrusion attempt detected"
			break
		case 3:
			map.descriptionText = "Covering removed"
			break
		case 4:
			map.descriptionText = "Invalid code"
			break
		default:
			// delegating it to handleAlarmReportUsingAlarmType
			return handleAlarmReportUsingAlarmType(cmd)
	}
	
	result << createEvent(map)
	result
}

/**
 * Responsible for handling Battery AlarmReport command
 *
 * @param cmd: The AlarmReport command to be parsed
 *
 * @return The event(s) to be sent out
 */
private def handleBatteryAlarmReport(cmd) {
	log.trace "[DTH] Executing 'handleBatteryAlarmReport' with cmd = $cmd"
	def result = []
	def deviceName = device.displayName
	def map = null
	switch(cmd.zwaveAlarmEvent) {
		case 0x0A:
			map = [ name: "battery", value: 1, descriptionText: "Battery level critical", displayed: true, data: [ lockName: deviceName ] ]
			break
		case 0x0B:
			map = [ name: "battery", value: 0, descriptionText: "Battery too low to operate lock", isStateChange: true, displayed: true, data: [ lockName: deviceName ] ]
			break
		default:
			// delegating it to handleAlarmReportUsingAlarmType
			return handleAlarmReportUsingAlarmType(cmd)
	}
	result << createEvent(map)
	result
}

/**
 * Responsible for handling AlarmReport commands which are ignored by Access & Burglar handlers
 *
 * @param cmd: The AlarmReport command to be parsed
 *
 * @return The event(s) to be sent out
 *
 */
private def handleAlarmReportUsingAlarmType(cmd) {
	log.trace "[DTH] Executing 'handleAlarmReportUsingAlarmType' with cmd = $cmd"
	def result = []
	def map = null
	def codeID, lockCodes, codeName
	def deviceName = device.displayName
	lockCodes = loadLockCodes()
	switch(cmd.alarmType) {
		case 9:
		case 17:
			map = [ name: "lock", value: "unknown", descriptionText: "Unknown state" ]
			break
		case 16: // Note: for levers this means it's unlocked, for non-motorized deadbolt, it's just unsecured and might not get unlocked
		case 19: // Unlocked with keypad
			map = [ name: "lock", value: "unlocked" ]
			if (cmd.alarmLevel != null) {
				codeID = cmd.alarmLevel
				codeName = getCodeName(lockCodes, codeID)
				map.descriptionText = "\"$codeName\" unlocked the lock"
				map.data = [ usedCode: codeID, codeName: codeName, method: "keypad" ]
			}
			break
		case 18: // Locked with keypad
			codeID = cmd.alarmLevel
			codeName = getCodeName(lockCodes, codeID)
			map = [ name: "lock", value: "locked" ]
			map.descriptionText = "\"$codeName\" locked the lock"
			map.data = [ usedCode: codeID, codeName: codeName, method: "keypad" ]
			break
		case 21: // Manually locked
			map = [ name: "lock", value: "locked", data: [ method: (cmd.alarmLevel == 2) ? "keypad" : "manual" ] ]
			map.descriptionText = "Locked manually"
			break
		case 22: // Manually unlocked
			map = [ name: "lock", value: "unlocked", data: [ method: "manual" ] ]
			map.descriptionText = "Unlocked manually"
			break
		case 23:
			map = [ name: "lock", value: "unknown", descriptionText: "Unknown state" ]
			map.data = [ method: "command" ]
			break
		case 24: // Locked by command
			map = [ name: "lock", value: "locked", data: [ method: "command" ] ]
			map.descriptionText = "Locked"
			break
		case 25: // Unlocked by command
			map = [ name: "lock", value: "unlocked", data: [ method: "command" ] ]
			map.descriptionText = "Unlocked"
			break
		case 26:
			map = [ name: "lock", value: "unknown", descriptionText: "Unknown state" ]
			map.data = [ method: "auto" ]
			break
		case 27: // Auto locked
			map = [ name: "lock", value: "locked", data: [ method: "auto" ] ]
			map.descriptionText = "Auto locked"
			break
		case 32: // All user codes deleted
			result = allCodesDeletedEvent()
			map = [ name: "codeChanged", value: "all deleted", descriptionText: "Deleted all user codes", isStateChange: true ]
			map.data = [notify: true, notificationText: "Deleted all user codes in $deviceName at ${location.name}"]
			result << createEvent(name: "lockCodes", value: util.toJson([:]), displayed: false, descriptionText: "'lockCodes' attribute updated")
			break
		case 33: // User code deleted
			codeID = cmd.alarmLevel
			codeName = getCodeName(lockCodes, codeID)
			map = [ name: "codeChanged", value: "$codeID deleted", isStateChange: true ]
			map.descriptionText = "Deleted \"$codeName\""
			map.data = [ codeName: codeName, notify: true, notificationText: "Deleted \"$codeName\" in $deviceName at ${location.name}" ]
			result << codeDeletedEvent(lockCodes, codeID)
			break
		case 38: // Non Access
			map = [ descriptionText: "A Non Access Code was entered at the lock", isStateChange: true ]
			break
		case 13:
		case 112: // Master or user code changed/set
			codeID = cmd.alarmLevel
			codeName = getCodeNameFromState(lockCodes, codeID)
			def changeType = getChangeType(lockCodes, codeID)
			map = [ name: "codeChanged", value: "$codeID $changeType", descriptionText:
				"${getStatusForDescription(changeType)} \"$codeName\"", isStateChange: true ]
			map.data = [ codeName: codeName, notify: true, notificationText: "${getStatusForDescription(changeType)} \"$codeName\" in $deviceName at ${location.name}" ]
			if(!isMasterCode(codeID)) {
				result << codeSetEvent(lockCodes, codeID, codeName)
			} else {
				map.descriptionText = "${getStatusForDescription('set')} \"$codeName\""
				map.data.notificationText = "${getStatusForDescription('set')} \"$codeName\" in $deviceName at ${location.name}"
			}
			break
		case 34:
		case 113: // Duplicate Pin-code error
			codeID = cmd.alarmLevel
			clearStateForSlot(codeID)
			map = [ name: "codeChanged", value: "$codeID failed", descriptionText: "User code is duplicate and not added",
				isStateChange: true, data: [isCodeDuplicate: true] ]
			break
		case 130:  // Batteries replaced
			map = [ descriptionText: "Batteries replaced", isStateChange: true ]
			break
		case 131: // Disabled user entered at keypad
			map = [ descriptionText: "Code ${cmd.alarmLevel} is disabled", isStateChange: false ]
			break
		case 161: // Tamper Alarm
			if (cmd.alarmLevel == 2) {
				map = [ name: "tamper", value: "detected", descriptionText: "Front escutcheon removed", isStateChange: true ]
			} else {
				map = [ name: "tamper", value: "detected", descriptionText: "Keypad attempts exceed code entry limit", isStateChange: true, displayed: true ]
			}
			break
		case 167: // Low Battery Alarm
			if (!state.lastbatt || now() - state.lastbatt > 12*60*60*1000) {
				map = [ descriptionText: "Battery low", isStateChange: true ]
				result << response(secure(zwave.batteryV1.batteryGet()))
			} else {
				map = [ name: "battery", value: device.currentValue("battery"), descriptionText: "Battery low", isStateChange: true ]
			}
			break
		case 168: // Critical Battery Alarms
			map = [ name: "battery", value: 1, descriptionText: "Battery level critical", displayed: true ]
			break
		case 169: // Battery too low to operate
			map = [ name: "battery", value: 0, descriptionText: "Battery too low to operate lock", isStateChange: true, displayed: true ]
			break
		default:
			map = [ displayed: false, descriptionText: "Alarm event ${cmd.alarmType} level ${cmd.alarmLevel}" ]
			break
	}
	
	if (map) {
		if (map.data) {
			map.data.lockName = deviceName
		} else {
			map.data = [ lockName: deviceName ]
		}
		result << createEvent(map)
	}
	result = result.flatten()
	result
}

/**
 * Responsible for parsing UserCodeReport command
 *
 * @param cmd: The UserCodeReport command to be parsed
 *
 * @return The event(s) to be sent out
 *
 */
def zwaveEvent(UserCodeReport cmd) {
	log.trace "[DTH] Executing 'zwaveEvent(UserCodeReport)' with userIdentifier: ${cmd.userIdentifier} and status: ${cmd.userIdStatus}"
	def result = []
	def code = cmd.code
	// cmd.userIdentifier seems to be an int primitive type
	def codeID = cmd.userIdentifier.toString()
	def lockCodes = loadLockCodes()
	def map = [ name: "codeChanged", isStateChange: true ]
	def deviceName = device.displayName
	def userIdStatus = cmd.userIdStatus

	if (userIdStatus == UserCodeReport.USER_ID_STATUS_OCCUPIED ||
				(userIdStatus == UserCodeReport.USER_ID_STATUS_STATUS_NOT_AVAILABLE && cmd.user)) {
		// We'll land here during scanning of codes
		def codeName = getCodeName(lockCodes, codeID)
		if (!lockCodes[codeID]) {
			map.value = "$codeID set"
			result << codeSetEvent(lockCodes, codeID, codeName)
		} else {
			map.value = "$codeID changed"
			map.isStateChange = false
		}
		map.descriptionText = "${getStatusForDescription('set')} \"$codeName\""
		map.data = [ codeName: codeName, lockName: deviceName ]
	} else {
		// code is not set
		if (lockCodes[codeID]) {
			def codeName = getCodeName(lockCodes, codeID)
			map.value = "$codeID deleted"
			map.descriptionText = "Deleted \"$codeName\""
			map.data = [ codeName: codeName, lockName: deviceName, notify: true, notificationText: "Deleted \"$codeName\" in $deviceName at ${location.name}" ]
			result << codeDeletedEvent(lockCodes, codeID)
		} else {
			map.value = "$codeID unset"
			map.isStateChange = false
			map.data = [ lockName: deviceName ]
		}
	}
	
	clearStateForSlot(codeID)
	result << createEvent(map)
	
	if (codeID.toInteger() == state.checkCode) {  // reloadAllCodes() was called, keep requesting the codes in order
		if (state.checkCode + 1 > state.codes || state.checkCode >= 8) {
			state.remove("checkCode")  // done
			state["checkCode"] = null
			sendEvent(name: "scanCodes", value: "Complete", descriptionText: "Code scan completed", displayed: false)
		} else {
			state.checkCode = state.checkCode + 1  // get next
			result << response(requestCode(state.checkCode))
		}
	}
	if (codeID == state.pollCode) {
		if (state.pollCode + 1 > state.codes || state.pollCode >= 15) {
			state.remove("pollCode")  // done
			state["pollCode"] = null
		} else {
			state.pollCode = state.pollCode + 1
		}
	}

	result = result.flatten()
	result
}

/**
 * Responsible for parsing UsersNumberReport command
 *
 * @param cmd: The UsersNumberReport command to be parsed
 *
 * @return The event(s) to be sent out
 *
 */
def zwaveEvent(UsersNumberReport cmd) {
	log.trace "[DTH] Executing 'zwaveEvent(UsersNumberReport)' with cmd = $cmd"
	def result = [createEvent(name: "maxCodes", value: cmd.supportedUsers, displayed: false)]
	state.codes = cmd.supportedUsers
	if (state.checkCode) {
		if (state.checkCode <= cmd.supportedUsers) {
			result << response(requestCode(state.checkCode))
		} else {
			state.remove("checkCode")
			state["checkCode"] = null
		}
	}
	result
}

/**
 * Responsible for parsing AssociationReport command
 *
 * @param cmd: The AssociationReport command to be parsed
 *
 * @return The event(s) to be sent out
 *
 */
def zwaveEvent(physicalgraph.zwave.commands.associationv2.AssociationReport cmd) {
	log.trace "[DTH] Executing 'zwaveEvent(physicalgraph.zwave.commands.associationv2.AssociationReport)' with cmd = $cmd"
	def result = []
	if (cmd.nodeId.any { it == zwaveHubNodeId }) {
		state.remove("associationQuery")
		state["associationQuery"] = null
		result << createEvent(descriptionText: "Is associated")
		state.assoc = zwaveHubNodeId
		if (cmd.groupingIdentifier == 2) {
			result << response(zwave.associationV1.associationRemove(groupingIdentifier:1, nodeId:zwaveHubNodeId))
		}
	} else if (cmd.groupingIdentifier == 1) {
		result << response(secure(zwave.associationV1.associationSet(groupingIdentifier:1, nodeId:zwaveHubNodeId)))
	} else if (cmd.groupingIdentifier == 2) {
		result << response(zwave.associationV1.associationSet(groupingIdentifier:2, nodeId:zwaveHubNodeId))
	}
	result
}

/**
 * Responsible for parsing TimeGet command
 *
 * @param cmd: The TimeGet command to be parsed
 *
 * @return The event(s) to be sent out
 *
 */
def zwaveEvent(physicalgraph.zwave.commands.timev1.TimeGet cmd) {
	log.trace "[DTH] Executing 'zwaveEvent(physicalgraph.zwave.commands.timev1.TimeGet)' with cmd = $cmd"
	def result = []
	def now = new Date().toCalendar()
	if(location.timeZone) now.timeZone = location.timeZone
	result << createEvent(descriptionText: "Requested time update", displayed: false)
	result << response(secure(zwave.timeV1.timeReport(
		hourLocalTime: now.get(Calendar.HOUR_OF_DAY),
		minuteLocalTime: now.get(Calendar.MINUTE),
		secondLocalTime: now.get(Calendar.SECOND)))
	)
	result
}

/**
 * Responsible for parsing BasicSet command
 *
 * @param cmd: The BasicSet command to be parsed
 *
 * @return The event(s) to be sent out
 *
 */
def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicSet cmd) {
	log.trace "[DTH] Executing 'zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicSet)' with cmd = $cmd"
	// The old Schlage locks use group 1 for basic control - we don't want that, so unsubscribe from group 1
	def result = [ createEvent(name: "lock", value: cmd.value ? "unlocked" : "locked") ]
	def cmds = [
			zwave.associationV1.associationRemove(groupingIdentifier:1, nodeId:zwaveHubNodeId).format(),
			"delay 1200",
			zwave.associationV1.associationGet(groupingIdentifier:2).format()
	]
	[result, response(cmds)]
}

/**
 * Responsible for parsing BatteryReport command
 *
 * @param cmd: The BatteryReport command to be parsed
 *
 * @return The event(s) to be sent out
 *
 */
def zwaveEvent(physicalgraph.zwave.commands.batteryv1.BatteryReport cmd) {
	log.trace "[DTH] Executing 'zwaveEvent(physicalgraph.zwave.commands.batteryv1.BatteryReport)' with cmd = $cmd"
	def map = [ name: "battery", unit: "%" ]
	if (cmd.batteryLevel == 0xFF) {
		map.value = 1
		map.descriptionText = "Has a low battery"
	} else {
		map.value = cmd.batteryLevel
		map.descriptionText = "Battery is at ${cmd.batteryLevel}%"
	}
	state.lastbatt = now()
	createEvent(map)
}

/**
 * Responsible for parsing ManufacturerSpecificReport command
 *
 * @param cmd: The ManufacturerSpecificReport command to be parsed
 *
 * @return The event(s) to be sent out
 *
 */
def zwaveEvent(physicalgraph.zwave.commands.manufacturerspecificv2.ManufacturerSpecificReport cmd) {
	log.trace "[DTH] Executing 'zwaveEvent(physicalgraph.zwave.commands.manufacturerspecificv2.ManufacturerSpecificReport)' with cmd = $cmd"
	def result = []
	def msr = String.format("%04X-%04X-%04X", cmd.manufacturerId, cmd.productTypeId, cmd.productId)
	updateDataValue("MSR", msr)
	result << createEvent(descriptionText: "MSR: $msr", isStateChange: false)
	result
}

/**
 * Responsible for parsing VersionReport command
 *
 * @param cmd: The VersionReport command to be parsed
 *
 * @return The event(s) to be sent out
 *
 */
def zwaveEvent(physicalgraph.zwave.commands.versionv1.VersionReport cmd) {
	log.trace "[DTH] Executing 'zwaveEvent(physicalgraph.zwave.commands.versionv1.VersionReport)' with cmd = $cmd"
	def fw = "${cmd.applicationVersion}.${cmd.applicationSubVersion}"
	updateDataValue("fw", fw)
	if (getDataValue("MSR") == "003B-6341-5044") {
		updateDataValue("ver", "${cmd.applicationVersion >> 4}.${cmd.applicationVersion & 0xF}")
	}
	def text = "${device.displayName}: firmware version: $fw, Z-Wave version: ${cmd.zWaveProtocolVersion}.${cmd.zWaveProtocolSubVersion}"
	createEvent(descriptionText: text, isStateChange: false)
}

/**
 * Responsible for parsing ApplicationBusy command
 *
 * @param cmd: The ApplicationBusy command to be parsed
 *
 * @return The event(s) to be sent out
 *
 */
def zwaveEvent(physicalgraph.zwave.commands.applicationstatusv1.ApplicationBusy cmd) {
	log.trace "[DTH] Executing 'zwaveEvent(physicalgraph.zwave.commands.applicationstatusv1.ApplicationBusy)' with cmd = $cmd"
	def msg = cmd.status == 0 ? "try again later" :
			  cmd.status == 1 ? "try again in ${cmd.waitTime} seconds" :
			  cmd.status == 2 ? "request queued" : "sorry"
	createEvent(displayed: true, descriptionText: "Is busy, $msg")
}

/**
 * Responsible for parsing ApplicationRejectedRequest command
 *
 * @param cmd: The ApplicationRejectedRequest command to be parsed
 *
 * @return The event(s) to be sent out
 *
 */
def zwaveEvent(physicalgraph.zwave.commands.applicationstatusv1.ApplicationRejectedRequest cmd) {
	log.trace "[DTH] Executing 'zwaveEvent(physicalgraph.zwave.commands.applicationstatusv1.ApplicationRejectedRequest)' with cmd = $cmd"
	createEvent(displayed: true, descriptionText: "Rejected the last request")
}

/**
 * Responsible for parsing zwave command
 *
 * @param cmd: The zwave command to be parsed
 *
 * @return The event(s) to be sent out
 *
 */
def zwaveEvent(physicalgraph.zwave.Command cmd) {
	log.trace "[DTH] Executing 'zwaveEvent(physicalgraph.zwave.Command)' with cmd = $cmd"
	createEvent(displayed: false, descriptionText: "$cmd")
}

/**
 * Executes lock and then check command with a delay on a lock
 */
def lockAndCheck(doorLockMode) {
	secureSequence([
		zwave.doorLockV1.doorLockOperationSet(doorLockMode: doorLockMode),
		zwave.doorLockV1.doorLockOperationGet()
	], 4200)
}

/**
 * Executes lock command on a lock
 */
def lock() {
	log.trace "[DTH] Executing lock() for device ${device.displayName}"
	lockAndCheck(DoorLockOperationSet.DOOR_LOCK_MODE_DOOR_SECURED)
}

/**
 * Executes unlock command on a lock
 */
def unlock() {
	log.trace "[DTH] Executing unlock() for device ${device.displayName}"
	lockAndCheck(DoorLockOperationSet.DOOR_LOCK_MODE_DOOR_UNSECURED)
}

/**
 * Executes unlock with timeout command on a lock
 */
def unlockWithTimeout() {
	log.trace "[DTH] Executing unlockWithTimeout() for device ${device.displayName}"
	lockAndCheck(DoorLockOperationSet.DOOR_LOCK_MODE_DOOR_UNSECURED_WITH_TIMEOUT)
}

/**
 * PING is used by Device-Watch in attempt to reach the Device
 */
def ping() {
	log.trace "[DTH] Executing ping() for device ${device.displayName}"
	runIn(30, followupStateCheck)
	secure(zwave.doorLockV1.doorLockOperationGet())
}

/**
 * Checks the door lock state. Also, schedules checking of door lock state every one hour.
 */
def followupStateCheck() {
	runEvery1Hour(stateCheck)
	stateCheck()
}

/**
 * Checks the door lock state
 */
def stateCheck() {
	sendHubCommand(new physicalgraph.device.HubAction(secure(zwave.doorLockV1.doorLockOperationGet())))
}

/**
 * Called when the user taps on the refresh button
 */
def refresh() {
	log.trace "[DTH] Executing refresh() for device ${device.displayName}"

	def cmds = secureSequence([zwave.doorLockV1.doorLockOperationGet(), zwave.batteryV1.batteryGet()])
	if (!state.associationQuery) {
		cmds << "delay 4200"
		cmds << zwave.associationV1.associationGet(groupingIdentifier:2).format()  // old Schlage locks use group 2 and don't secure the Association CC
		cmds << secure(zwave.associationV1.associationGet(groupingIdentifier:1))
		state.associationQuery = now()
	} else if (now() - state.associationQuery.toLong() > 9000) {
		cmds << "delay 6000"
		cmds << zwave.associationV1.associationSet(groupingIdentifier:2, nodeId:zwaveHubNodeId).format()
		cmds << secure(zwave.associationV1.associationSet(groupingIdentifier:1, nodeId:zwaveHubNodeId))
		cmds << zwave.associationV1.associationGet(groupingIdentifier:2).format()
		cmds << secure(zwave.associationV1.associationGet(groupingIdentifier:1))
		state.associationQuery = now()
	}
	cmds
}

/**
 * Called by the Smart Things platform in case Polling capability is added to the device type
 */
def poll() {
	log.trace "[DTH] Executing poll() for device ${device.displayName}"
	def cmds = []
	// Only check lock state if it changed recently or we haven't had an update in an hour
	def latest = device.currentState("lock")?.date?.time
	if (!latest || !secondsPast(latest, 6 * 60) || secondsPast(state.lastPoll, 55 * 60)) {
		cmds << secure(zwave.doorLockV1.doorLockOperationGet())
		state.lastPoll = now()
	} else if (!state.lastbatt || now() - state.lastbatt > 53*60*60*1000) {
		cmds << secure(zwave.batteryV1.batteryGet())
		state.lastbatt = now()  //inside-214
	}
	if (state.assoc != zwaveHubNodeId && secondsPast(state.associationQuery, 19 * 60)) {
		cmds << zwave.associationV1.associationSet(groupingIdentifier:2, nodeId:zwaveHubNodeId).format()
		cmds << secure(zwave.associationV1.associationSet(groupingIdentifier:1, nodeId:zwaveHubNodeId))
		cmds << zwave.associationV1.associationGet(groupingIdentifier:2).format()
		cmds << "delay 6000"
		cmds << secure(zwave.associationV1.associationGet(groupingIdentifier:1))
		cmds << "delay 6000"
		state.associationQuery = now()
	} else {
		// Only check lock state once per hour
		if (secondsPast(state.lastPoll, 55 * 60)) {
			cmds << secure(zwave.doorLockV1.doorLockOperationGet())
			state.lastPoll = now()
		} else if (!state.MSR) {
			cmds << zwave.manufacturerSpecificV1.manufacturerSpecificGet().format()
		} else if (!state.fw) {
			cmds << zwave.versionV1.versionGet().format()
		} else if (!device.currentValue("maxCodes")) {
			state.pollCode = 1
			cmds << secure(zwave.userCodeV1.usersNumberGet())
		} else if (state.pollCode && state.pollCode <= state.codes) {
			cmds << requestCode(state.pollCode)
		} else if (!state.lastbatt || now() - state.lastbatt > 53*60*60*1000) {
			cmds << secure(zwave.batteryV1.batteryGet())
		}
	}

	if (cmds) {
		log.debug "poll is sending ${cmds.inspect()}"
		cmds
	} else {
		// workaround to keep polling from stopping due to lack of activity
		sendEvent(descriptionText: "skipping poll", isStateChange: true, displayed: false)
		null
	}
}

/**
 * Returns the command for user code get
 *
 * @param codeID: The code slot number
 *
 * @return The command for user code get
 */
def requestCode(codeID) {
	secure(zwave.userCodeV1.userCodeGet(userIdentifier: codeID))
}

/**
 * API endpoint for server smart app to populate the attributes. Called only when the attributes are not populated.
 *
 * @return The command(s) fired for reading attributes
 */
def reloadAllCodes() {
	log.trace "[DTH] Executing 'reloadAllCodes()' by ${device.displayName}"
	sendEvent(name: "scanCodes", value: "Scanning", descriptionText: "Code scan in progress", displayed: false)
	def lockCodes = loadLockCodes()
	sendEvent(lockCodesEvent(lockCodes))
	state.checkCode = 1

	if (!state.codes) {
		return secure(zwave.userCodeV1.usersNumberGet())
	} else {
		sendEvent(name: "maxCodes", value: state.codes, displayed: false)
		return requestCode(1)
	}
}

/**
 * API endpoint for setting a user code on a lock
 *
 * @param codeID: The code slot number
 *
 * @param code: The code PIN
 *
 * @param codeName: The name of the code
 *
 * @returns cmds: The commands fired for creation and checking of a lock code
 */
def setCode(codeID, code, codeName = null) {
	if (!code) {
		log.trace "[DTH] Executing 'nameSlot()' by ${this.device.displayName}"
		nameSlot(codeID, codeName)
		return
	}
	
	log.trace "[DTH] Executing 'setCode()' by ${this.device.displayName}"
	def strcode = code
	if (code instanceof String) {
		code = code.toList().findResults { if(it > ' ' && it != ',' && it != '-') it.toCharacter() as Short }
	} else {
		strcode = code.collect{ it as Character }.join()
	}

	def strname = (codeName ?: "Code $codeID")
	state["setname$codeID"] = strname
	def cmds = validateAttributes()
	cmds << secure(zwave.userCodeV1.userCodeSet(userIdentifier:codeID, userIdStatus:1, user:code))
	if(cmds.size() > 1) {
		cmds = delayBetween(cmds, 4200)
	}
	cmds
}

/**
 * Validates attributes and if attributes are not populated, adds the command maps to list of commands
 * @return List of commands or empty list
 */
def validateAttributes() {
	def cmds = []
	if(!device.currentValue("maxCodes")) {
		cmds << secure(zwave.userCodeV1.usersNumberGet())
	}
	log.trace "validateAttributes returning commands list: " + cmds
	cmds
}

/**
 * API endpoint for setting/deleting multiple user codes on a lock
 *
 * @param codeSettings: The map with code slot numbers and code pins (in case of update)
 *
 * @returns The commands fired for creation and deletion of lock codes
 */
def updateCodes(codeSettings) {
	log.trace "[DTH] Executing updateCodes() for device ${device.displayName}"
	if(codeSettings instanceof String) codeSettings = util.parseJson(codeSettings)
	def set_cmds = []
	codeSettings.each { name, updated ->
		if (name.startsWith("code")) {
			def n = name[4..-1].toInteger()
			if (updated && updated.size() >= 4 && updated.size() <= 8) {
				log.debug "Setting code number $n"
				def cmds = setCode(n, updated)
				set_cmds << cmds.first()
			} else if (updated == null || updated == "" || updated == "0") {
				log.debug "Deleting code number $n"
				set_cmds << deleteCode(n)
			}
		} else log.warn("unexpected entry $name: $updated")
	}
	if (set_cmds) {
		return response(delayBetween(set_cmds, 2200))
	}
	return null
}

/**
 * Renames an existing lock slot
 *
 * @param codeSlot: The code slot number
 *
 * @param codeName The new name of the code
 */
void nameSlot(codeSlot, codeName) {
	codeSlot = codeSlot.toString()
	if (!isCodeSet(codeSlot)) {
		return
	}
	def lockCodes = loadLockCodes()
	lockCodes[codeSlot] = (codeName ?: "Code $codeSlot")
	sendEvent(lockCodesEvent(lockCodes))
}

/**
 * API endpoint for deleting a user code on a lock
 *
 * @param codeID: The code slot number
 *
 * @returns cmds: The command fired for deletion of a lock code
 */
def deleteCode(codeID) {
	log.trace "[DTH] Executing 'deleteCode()' by ${this.device.displayName}"
	secure(zwave.userCodeV1.userCodeSet(userIdentifier:codeID, userIdStatus:0))
	/*secureSequence([
		zwave.userCodeV1.userCodeSet(userIdentifier:codeID, userIdStatus:0),
		zwave.userCodeV1.userCodeGet(userIdentifier:codeID)
	], 7000)*/
}

/**
 * Encapsulates a command
 *
 * @param cmd: The command to be encapsulated
 *
 * @returns ret: The encapsulated command
 */
private secure(physicalgraph.zwave.Command cmd) {
	zwave.securityV1.securityMessageEncapsulation().encapsulate(cmd).format()
}

/**
 * Encapsulates list of command and adds a delay
 *
 * @param commands: The list of command to be encapsulated
 *
 * @param delay: The delay between commands
 *
 * @returns The encapsulated commands
 */
private secureSequence(commands, delay=4200) {
	delayBetween(commands.collect{ secure(it) }, delay)
}

/**
 * Checks if the time elapsed from the provided timestamp is greater than the number of senconds provided
 *
 * @param timestamp: The timestamp
 *
 * @param seconds: The number of seconds
 *
 * @returns true if elapsed time is greater than number of seconds provided, else false
 */
private Boolean secondsPast(timestamp, seconds) {
	if (!(timestamp instanceof Number)) {
		if (timestamp instanceof Date) {
			timestamp = timestamp.time
		} else if ((timestamp instanceof String) && timestamp.isNumber()) {
			timestamp = timestamp.toLong()
		} else {
			return true
		}
	}
	return (now() - timestamp) > (seconds * 1000)
}

/**
 * Reads the code name from the 'lockCodes' map
 *
 * @param lockCodes: map with lock code names
 *
 * @param codeID: The code slot number
 *
 * @returns The code name
 */
private String getCodeName(lockCodes, codeID) {
	if (isMasterCode(codeID)) {
		return "Master Code"
	}
	lockCodes[codeID.toString()] ?: "Code $codeID"
}

/**
 * Reads the code name from the device state
 *
 * @param lockCodes: map with lock code names
 *
 * @param codeID: The code slot number
 *
 * @returns The code name
 */
private String getCodeNameFromState(lockCodes, codeID) {
	if (isMasterCode(codeID)) {
		return "Master Code"
	}
	def nameFromLockCodes = lockCodes[codeID.toString()]
	def nameFromState = state["setname$codeID"]
	if(nameFromLockCodes) {
		if(nameFromState) {
			//Updated from smart app
			return nameFromState
		} else {
			//Updated from lock
			return nameFromLockCodes
		}
	} else if(nameFromState) {
		//Set from smart app
		return nameFromState
	}
	//Set from lock
	return "Code $codeID"
}

/**
 * Check if a user code is present in the 'lockCodes' map
 *
 * @param codeID: The code slot number
 *
 * @returns true if code is present, else false
 */
private Boolean isCodeSet(codeID) {
	// BUG: Needed to add loadLockCodes to resolve null pointer when using schlage?
	def lockCodes = loadLockCodes()
	lockCodes[codeID.toString()] ? true : false
}

/**
 * Reads the 'lockCodes' attribute and parses the same
 *
 * @returns Map: The lockCodes map
 */
private Map loadLockCodes() {
	parseJson(device.currentValue("lockCodes") ?: "{}") ?: [:]
}

/**
 * Populates the 'lockCodes' attribute by calling create event
 *
 * @param lockCodes The user codes in a lock
 */
private Map lockCodesEvent(lockCodes) {
	createEvent(name: "lockCodes", value: util.toJson(lockCodes), displayed: false,
	descriptionText: "'lockCodes' attribute updated")
}

/**
 * Utility function to figure out if code id pertains to master code or not
 *
 * @param codeID - The slot number in which code is set
 * @return - true if slot is for master code, false otherwise
 */
private boolean isMasterCode(codeID) {
	if(codeID instanceof String) {
		codeID = codeID.toInteger()
	}
	(codeID == 0) ? true : false
}

/**
 * Creates the event map for user code creation
 *
 * @param lockCodes: The user codes in a lock
 *
 * @param codeID: The code slot number
 *
 * @param codeName: The name of the user code
 *
 * @return The list of events to be sent out
 */
private def codeSetEvent(lockCodes, codeID, codeName) {
	clearStateForSlot(codeID)
	// codeID seems to be an int primitive type
	lockCodes[codeID.toString()] = (codeName ?: "Code $codeID")
	def result = []
	result << lockCodesEvent(lockCodes)
	def codeReportMap = [ name: "codeReport", value: codeID, data: [ code: "" ], isStateChange: true, displayed: false ]
	codeReportMap.descriptionText = "${device.displayName} code $codeID is set"
	result << createEvent(codeReportMap)
	result
}

/**
 * Creates the event map for user code deletion
 *
 * @param lockCodes: The user codes in a lock
 *
 * @param codeID: The code slot number
 *
 * @return The list of events to be sent out
 */
private def codeDeletedEvent(lockCodes,codeID) {
	lockCodes.remove("$codeID".toString())
	// not sure if the trigger has done this or not
	clearStateForSlot(codeID)
	def result = []
	result << lockCodesEvent(lockCodes)
	def codeReportMap = [ name: "codeReport", value: codeID, data: [ code: "" ], isStateChange: true, displayed: false ]
	codeReportMap.descriptionText = "${device.displayName} code $codeID was deleted"
	result << createEvent(codeReportMap)
	result
}

/**
 * Creates the event map for all user code deletion
 *
 * @return The List of events to be sent out
 */
private def allCodesDeletedEvent() {
	def result = []
	def lockCodes = loadLockCodes()
	def deviceName = device.displayName
	lockCodes.each { id, code ->
		result << createEvent(name: "codeReport", value: id, data: [ code: "" ], descriptionText: "code $id was deleted",
					displayed: false, isStateChange: true)
		
		def codeName = code
		result << createEvent(name: "codeChanged", value: "$id deleted", data: [ codeName: codeName, lockName: deviceName,
			notify: true, notificationText: "Deleted \"$codeName\" in $deviceName at ${location.name}" ],
		descriptionText: "Deleted \"$codeName\"",
		displayed: true, isStateChange: true)
		clearStateForSlot(id)
	}
	result
}

/**
 * Checks if a change type is set or update
 *
 * @param lockCodes: The user codes in a lock
 *
 * @param codeID The code slot number
 *
 * @return "set" or "update" basis the presence of the code id in the lockCodes map
 */
private def getChangeType(lockCodes, codeID) {
	def changeType = "set"
	if (lockCodes[codeID.toString()]) {
		changeType = "changed"
	}
	changeType
}

/**
 * Method to obtain status for descriptuion based on change type
 * @param changeType: Either "set" or "changed"
 * @return "Added" for "set", "Updated" for "changed", "" otherwise
 */
private def getStatusForDescription(changeType) {
	if("set" == changeType) {
		return "Added"
	} else if("changed" == changeType) {
		return "Updated"
	}
	//Don't return null as it cause trouble
	return ""
}

/**
 * Clears the code name and pin from the state basis the code slot number
 *
 * @param codeID: The code slot number
 */
def clearStateForSlot(codeID) {
	state.remove("setname$codeID")
	state["setname$codeID"] = null
}
