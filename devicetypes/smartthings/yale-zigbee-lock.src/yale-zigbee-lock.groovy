/**
 *  Yale ZigBee Lock
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
import physicalgraph.zigbee.zcl.DataType

metadata {
	definition (name: "Yale ZigBee Lock", namespace: "smartthings", author: "SmartThings") {
		capability "Actuator"
		capability "Lock"
		capability "Polling"
		capability "Refresh"
		capability "Sensor"
		capability "Lock Codes"
		capability "Battery"
		capability "Configuration"
		capability "Health Check"

		input "audioLevel", "enum", title: "Volume Setting", options: ["Silent", "Low", "High"], defaultValue: getDefaultValueFor("audioLevel"), multiple: false, required: false
		input "autoLock", "enum", title: "Auto-Lock", options: ["On", "Off"], defaultValue: getDefaultValueFor("autoLock"), multiple: false, required: false
		input "relockTime", "number", title: "Re-Lock Time (10-180 seconds)", defaultValue: getDefaultValueFor("relockTime"), range: "10..180", displayDuringSetup: false, required: false
		input "wrongCodeEntryLimit", "number", title: "Wrong Code Entry Limit (3-7 Entries)", defaultValue: getDefaultValueFor("wrongCodeEntryLimit"), range: "3..7", displayDuringSetup: false, required: false
		input "wrongCodeTimeOut", "number", title: "Wrong Code Time Out (10-180 seconds)", defaultValue: getDefaultValueFor("wrongCodeTimeOut"), range: "10..180", displayDuringSetup: false, required: false
		input "language", "enum", title: "Language Setting", options: ["English", "Spanish", "French"], defaultValue: getDefaultValueFor("language"), multiple: false, required: false
		input "operatingMode", "enum", title: "Operating Mode", options: ["Normal", "Vacation", "Privacy"], defaultValue: getDefaultValueFor("operatingMode"), multiple: false, required: false
		input "oneTouchLocking", "enum", title: "One Touch Locking", options: ["On", "Off"], defaultValue: getDefaultValueFor("oneTouchLocking"), multiple: false, required: false
		input "insideStatusLed", "enum", title: "Enable Inside Status LED", options: ["On", "Off"], defaultValue: getDefaultValueFor("insideStatusLed"), multiple: false, required: false
		input "privacyButton", "enum", title: "Privacy Mode (Button)", options: ["On", "Off"], defaultValue: getDefaultValueFor("privacyButton"), multiple: false, required: false

		fingerprint profileId: "0104", inClusters: "0000,0001,0003,0009,000A,0101,0020", outClusters: "000A,0019", manufacturer: "Yale", model: "YRL220 TS LL", deviceJoinName: "Yale Touch Screen Lever Lock"
		fingerprint profileId: "0104", inClusters: "0000,0001,0003,0009,000A,0101,0020", outClusters: "000A,0019", manufacturer: "Yale", model: "YRD210 PB DB", deviceJoinName: "Yale Push Button Deadbolt Lock"
		fingerprint profileId: "0104", inClusters: "0000,0001,0003,0009,000A,0101,0020", outClusters: "000A,0019", manufacturer: "Yale", model: "YRD220/240 TSDB", deviceJoinName: "Yale Touch Screen Deadbolt Lock"
		fingerprint profileId: "0104", inClusters: "0000,0001,0003,0009,000A,0101,0020", outClusters: "000A,0019", manufacturer: "Yale", model: "YRL210 PB LL", deviceJoinName: "Yale Push Button Lever Lock"
		fingerprint profileId: "0104", inClusters: "0000,0001,0003,0009,000A,0101,0020", outClusters: "000A,0019", manufacturer: "Yale", model: "YRD226/246 TSDB", deviceJoinName: "Yale Touch Screen Deadbolt Lock"
	}

	tiles(scale: 2) {
		multiAttributeTile(name:"toggle", type:"generic", width:6, height:4) {
			tileAttribute ("device.lock", key:"PRIMARY_CONTROL") {
				attributeState "locked", label:'locked', action:"lock.unlock", icon:"st.locks.lock.locked", backgroundColor:"#00A0DC", nextState:"unlocking"
				attributeState "unlocked", label:'unlocked', action:"lock.lock", icon:"st.locks.lock.unlocked", backgroundColor:"#ffffff", nextState:"locking"
				attributeState "unknown", label:"unknown", action:"lock.lock", icon:"st.locks.lock.unknown", backgroundColor:"#ffffff", nextState:"locking"
				attributeState "locking", label:'locking', icon:"st.locks.lock.locked", backgroundColor:"#00A0DC"
				attributeState "unlocking", label:'unlocking', icon:"st.locks.lock.unlocked", backgroundColor:"#ffffff"
			}
		}
		standardTile("lock", "device.lock", inactiveLabel:false, decoration:"flat", width:2, height:2) {
			state "default", label:'lock', action:"lock.lock", icon:"st.locks.lock.locked", nextState:"locking"
		}
		standardTile("unlock", "device.lock", inactiveLabel:false, decoration:"flat", width:2, height:2) {
			state "default", label:'unlock', action:"lock.unlock", icon:"st.locks.lock.unlocked", nextState:"unlocking"
		}
		valueTile("battery", "device.battery", inactiveLabel:false, decoration:"flat", width:2, height:2) {
			state "battery", label:'${currentValue}% battery', unit:""
		}
		standardTile("refresh", "device.refresh", inactiveLabel:false, decoration:"flat", width:2, height:2) {
			state "default", label:'', action:"refresh.refresh", icon:"st.secondary.refresh"
		}

		main "toggle"
		details(["toggle", "lock", "unlock", "battery", "refresh"])
	}
}

// Globals - Cluster IDs
private getCLUSTER_POWER() { 0x0001 }
private getCLUSTER_DOORLOCK() { 0x0101 }
private getCLUSTER_ALARM() { 0x0009 }

// Globals - Command IDs
private getDOORLOCK_CMD_LOCK_DOOR() { 0x00 }
private getDOORLOCK_CMD_UNLOCK_DOOR() { 0x01 }
private getDOORLOCK_CMD_USER_CODE_SET() { 0x05 }
private getDOORLOCK_CMD_USER_CODE_GET() { 0x06 }
private getDOORLOCK_CMD_CLEAR_USER_CODE() { 0x07 }
private getDOORLOCK_RESPONSE_OPERATION_EVENT() { 0x20 }
private getDOORLOCK_RESPONSE_PROGRAMMING_EVENT() { 0x21 }
private getPOWER_ATTR_BATTERY_PERCENTAGE_REMAINING() { 0x0021 }
private getDOORLOCK_ATTR_LOCKSTATE() { 0x0000 }
private getDOORLOCK_ATTR_NUM_PIN_USERS() { 0x0012 }
private getDOORLOCK_ATTR_MAX_PIN_LENGTH() { 0x0017 }
private getDOORLOCK_ATTR_MIN_PIN_LENGTH() { 0x0018 }
private getDOORLOCK_ATTR_AUTO_RELOCK_TIME() { 0x0023 }
private getDOORLOCK_ATTR_SOUND_VOLUME() { 0x0024 }
private getDOORLOCK_ATTR_OPERATING_MODE() { 0x0025 }
private getDOORLOCK_ATTR_ONE_TOUCH_LOCK() { 0x0029 }
private getDOORLOCK_ATTR_LED_STATUS() { 0x002A }
private getDOORLOCK_ATTR_PRIVACY_BUTTON() { 0x002B }
private getDOORLOCK_ATTR_WRONG_CODE_ENTRY_LIMIT() { 0x0030 }
private getDOORLOCK_ATTR_USER_CODE_DISABLE_TIME() { 0x0031 }
private getDOORLOCK_ATTR_LANGUAGE() { 0x0021 }
private getDOORLOCK_ATTR_SEND_PIN_OTA() { 0x0032 }
private getALARM_ATTR_ALARM_COUNT() { 0x0000 }
private getALARM_CMD_ALARM() { 0x00 }

/**
 * Constructs a map of the configurable parameters in a lock. This is specific to Yale ZigBee locks.
 *
 * @return map: The map with key/values for attribute id, type, and allowed values for each settings
 */
def getLockParams() {
	def map = [
		audioLevel: 		[ attrId: DOORLOCK_ATTR_SOUND_VOLUME,           type: DataType.UINT8,        value: [ "silent": 0, "low": 1, "high": 2] ],
		relockTime: 		[ attrId: DOORLOCK_ATTR_AUTO_RELOCK_TIME,       type: DataType.UINT32,       value: settings.relockTime ],
		wrongCodeEntryLimit:[ attrId: DOORLOCK_ATTR_WRONG_CODE_ENTRY_LIMIT, type: DataType.UINT8,        value: settings.wrongCodeEntryLimit ],
		wrongCodeTimeOut:   [ attrId: DOORLOCK_ATTR_USER_CODE_DISABLE_TIME, type: DataType.UINT8,        value: settings.wrongCodeTimeOut ],
		language:           [ attrId: DOORLOCK_ATTR_LANGUAGE,               type: DataType.STRING_CHAR,  value: [ "english": "en", "spanish": "es", "french": "fr" ] ],
		operatingMode:      [ attrId: DOORLOCK_ATTR_OPERATING_MODE,         type: DataType.ENUM8,        value: [ "normal": 00, "vacation": 01, "privacy": 02 ] ],
		oneTouchLocking:    [ attrId: DOORLOCK_ATTR_ONE_TOUCH_LOCK,         type: DataType.BOOLEAN,      value: [ "on": 1, "off": 0 ] ],
		insideStatusLed:    [ attrId: DOORLOCK_ATTR_LED_STATUS,             type: DataType.BOOLEAN,      value: [ "on": 1, "off": 0 ] ],
		privacyButton:      [ attrId: DOORLOCK_ATTR_PRIVACY_BUTTON,         type: DataType.BOOLEAN,      value: [ "on": 1, "off": 0 ] ]
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
	if ("audioLevel".equals(settingName)) {
		return "High"
	}
	if ("autoLock".equals(settingName)) {
		return "Off"
	}
	if ("relockTime".equals(settingName)) {
		return 10
	}
	if ("wrongCodeEntryLimit".equals(settingName)) {
		return 5
	}
	if ("wrongCodeTimeOut".equals(settingName)) {
		return 60
	}
	if ("language".equals(settingName)) {
		return "English"
	}
	if ("operatingMode".equals(settingName)) {
		return "Normal"
	}
	if ("oneTouchLocking".equals(settingName)) {
		return "On"
	}
	if ("insideStatusLed".equals(settingName)) {
		return "Off"
	}
	if ("privacyButton".equals(settingName)) {
		return "Off"
	}
	return null
}

/**
 * Reads the values of the parameters defined in the DTH preferences
 *
 * @param settingName: The settings key
 *
 * @param param: The map with attribute id, type, and allowed values of the configurable parameters in a lock
 *
 * @return configVal: The user set value or the default value for a setting
 */
def getConfigValueFor(settingName, param) {
	def configVal
	//Audio Level
	if ("audioLevel".equals(settingName)) {
		if (settings.audioLevel == null) {
			log.warn "User set audioLevel to null. Using default value ${getDefaultValueFor('audioLevel')}."
			configVal = param.audioLevel.value[getDefaultValueFor("audioLevel").toLowerCase()]
		} else {
			configVal = param.audioLevel.value."${settings.audioLevel.toLowerCase()}"
		}
	}
	//Relock Time
	if ("relockTime".equals(settingName)) {
		if (settings.autoLock == null || "${settings.autoLock.toLowerCase()}" == "off") {
			configVal = 0
		} else {
			//autoLock is ON
			if (settings.relockTime == null) {
				log.warn "User set relockTime to null. Using default value ${getDefaultValueFor('relockTime')}."
				configVal = getDefaultValueFor("relockTime")
			} else {
				configVal = param.relockTime.value
			}
		}
	}
	//Wrong Code Entry Limit
	if ("wrongCodeEntryLimit".equals(settingName)) {
		if (settings.wrongCodeEntryLimit == null) {
			log.warn "User set wrongCodeEntryLimit to null. Using default value ${getDefaultValueFor('wrongCodeEntryLimit')}."
			configVal = getDefaultValueFor("wrongCodeEntryLimit")
		} else {
			configVal = param.wrongCodeEntryLimit.value
		}
	}
	//Wrong Code Time Out
	if ("wrongCodeTimeOut".equals(settingName)) {
		if (settings.wrongCodeTimeOut == null) {
			log.warn "User set wrongCodeTimeOut to null. Using default value ${getDefaultValueFor('wrongCodeTimeOut')}."
			configVal = getDefaultValueFor("wrongCodeTimeOut")
		} else {
			configVal = param.wrongCodeTimeOut.value
		}
	}
	//Language
	if ("language".equals(settingName)) {
		if (settings.language == null) {
			log.warn "User set language to null. Using default value ${getDefaultValueFor('language')}."
			configVal = param.language.value[getDefaultValueFor("language").toLowerCase()]
		} else {
			configVal = param.language.value."${settings.language.toLowerCase()}"
		}
	}
	//Operating Mode
	if ("operatingMode".equals(settingName)) {
		if (settings.operatingMode == null) {
			log.warn "User set operatingMode to null. Using default value ${getDefaultValueFor('operatingMode')}."
			configVal = param.operatingMode.value[getDefaultValueFor("operatingMode").toLowerCase()]
		} else {
			configVal = param.operatingMode.value."${settings.operatingMode.toLowerCase()}"
		}
	}
	//One Touch Locking
	if ("oneTouchLocking".equals(settingName)) {
		if (settings.oneTouchLocking == null) {
			log.warn "User set oneTouchLocking to null. Using default value ${getDefaultValueFor('oneTouchLocking')}."
			configVal = param.oneTouchLocking.value[getDefaultValueFor("oneTouchLocking").toLowerCase()]
		} else {
			configVal = param.oneTouchLocking.value."${settings.oneTouchLocking.toLowerCase()}"
		}
	}
	//Inside Status LED
	if ("insideStatusLed".equals(settingName)) {
		if (settings.insideStatusLed == null) {
			log.warn "User set insideStatusLed to null. Using default value ${getDefaultValueFor('insideStatusLed')}."
			configVal = param.insideStatusLed.value[getDefaultValueFor("insideStatusLed").toLowerCase()]
		} else {
			configVal = param.insideStatusLed.value."${settings.insideStatusLed.toLowerCase()}"
		}
	}
	//Privacy Button
	if ("privacyButton".equals(settingName)) {
		if (settings.privacyButton == null) {
			log.warn "User set privacyButton to null. Using default value ${getDefaultValueFor('privacyButton')}."
			configVal = param.privacyButton.value[getDefaultValueFor("privacyButton").toLowerCase()]
		} else {
			configVal = param.privacyButton.value."${settings.privacyButton.toLowerCase()}"
		}
	}
	configVal
}

/**
 * Called on app installed
 */
def installed() {
	log.trace "ZigBee DTH - Executing installed() for device ${device.displayName}"
}

/**
 * Called on app uninstalled
 */
def uninstalled() {
	log.trace "ZigBee DTH - Executing uninstalled() for device ${device.displayName}"
}

/**
 * Executed when the user taps on the 'Done' button on the device settings screen. Sends the values to lock.
 *
 * @return The list of commands to be executed
 */
def updated() {
	// Workaround for ST calling updated twice ---- start
	if ( state.updatedRan == 0 || state.updatedRan == null ) {
		state.updatedRan = 1
		log.trace "ZigBee DTH - Executing updated() for device ${device.displayName}"
	} else {
		state.updatedRan = 0
		return null
	}
	// Workaround for ST calling updated twice ---- end

	def cmds = []
	try {
		if (!state.init || !state.configured) {
			// Executed when the lock is being paired
			state.init = true
			log.trace "ZigBee DTH - Returning commands for lock operation get and battery get"
			if (!state.configured) {
				cmds << doConfigure()
			}
			cmds << zigbee.readAttribute(CLUSTER_DOORLOCK, DOORLOCK_ATTR_LOCKSTATE)
			cmds << zigbee.readAttribute(CLUSTER_POWER, POWER_ATTR_BATTERY_PERCENTAGE_REMAINING)
		} else {
			// Executed when the user taps on the 'Done' button on the device settings screen
			log.trace "ZigBee DTH - Writing user set preferences to the lock"
			def param = getLockParams()
			def doorLockClusterId = CLUSTER_DOORLOCK
			
			// audioLevel
			cmds << zigbee.writeAttribute(doorLockClusterId, param.audioLevel.attrId, param.audioLevel.type, getConfigValueFor("audioLevel", param))
			
			// autoLock
			// No need to write autoLock. relockTime = 0 indicates that autoLock is OFF.
			
			// relockTime
			cmds << zigbee.writeAttribute(doorLockClusterId, param.relockTime.attrId, param.relockTime.type, getConfigValueFor("relockTime", param))
			
			// wrongCodeEntryLimit
			cmds << zigbee.writeAttribute(doorLockClusterId, param.wrongCodeEntryLimit.attrId, param.wrongCodeEntryLimit.type, getConfigValueFor("wrongCodeEntryLimit", param))
			
			// wrongCodeTimeOut
			cmds << zigbee.writeAttribute(doorLockClusterId, param.wrongCodeTimeOut.attrId, param.wrongCodeTimeOut.type, getConfigValueFor("wrongCodeTimeOut", param))
			
			// language
			cmds << getZigbeeCommandForSettingLanguage(doorLockClusterId, param)
			
			// operatingMode
			cmds << zigbee.writeAttribute(doorLockClusterId, param.operatingMode.attrId, param.operatingMode.type, getConfigValueFor("operatingMode", param))
			
			// oneTouchLocking
			cmds << zigbee.writeAttribute(doorLockClusterId, param.oneTouchLocking.attrId, param.oneTouchLocking.type, getConfigValueFor("oneTouchLocking", param))
		
			// insideStatusLed
			cmds << zigbee.writeAttribute(doorLockClusterId, param.insideStatusLed.attrId, param.insideStatusLed.type, getConfigValueFor("insideStatusLed", param))
		
			// privacyButton
			cmds << zigbee.writeAttribute(doorLockClusterId, param.privacyButton.attrId, param.privacyButton.type, getConfigValueFor("privacyButton", param))
		}
	} catch (e) {
		log.warn "ZigBee DTH - updated() threw exception:- $e"
	}
	
	cmds = cmds.flatten()
	log.info "ZigBee DTH - updated() returning with cmds:- $cmds"
	response(cmds)
}

/**
 * Ping is used by Device-Watch in attempt to reach the device
 */
def ping() {
	log.trace "ZigBee DTH - Executing ping() for device ${device.displayName}"
	refresh()
}

/**
 * Called by the Smart Things platform in case Polling capability is added to the device type
 */
def poll() {
	log.trace "ZigBee DTH - Executing poll() for device ${device.displayName}"
	def cmds = []
	def latest = device.currentState("lock")?.date?.time
	if (!latest || !secondsPast(latest, 6 * 60) || secondsPast(state.lastPoll, 55 * 60)) {
		cmds << zigbee.readAttribute(CLUSTER_DOORLOCK, DOORLOCK_ATTR_LOCKSTATE)
		state.lastPoll = now()
	} else if (!state.lastbatt || now() - state.lastbatt > 53*60*60*1000) {
		cmds << zigbee.readAttribute(CLUSTER_POWER, POWER_ATTR_BATTERY_PERCENTAGE_REMAINING)
		state.lastbatt = now()
	}
	
	if (cmds) {
		log.info "ZigBee DTH - poll() returning with cmds:- $cmds"
		return cmds
	} else {
		// workaround to keep polling from stopping due to lack of activity
		sendEvent(descriptionText: "skipping poll", isStateChange: true, displayed: false)
		return null
	}
}

/**
 * Called when the user taps on the refresh button
 */
def refresh() {
	log.trace "ZigBee DTH - Executing refresh() for device ${device.displayName}"
	def cmds =
		zigbee.readAttribute(CLUSTER_DOORLOCK, DOORLOCK_ATTR_LOCKSTATE) +
		zigbee.readAttribute(CLUSTER_POWER, POWER_ATTR_BATTERY_PERCENTAGE_REMAINING)
	log.info "ZigBee DTH - refresh() returning with cmds:- $cmds"
	return cmds
}

/**
 * Configures the device to settings needed by SmarthThings at device discovery time
 *
 */
def configure() {
	log.trace "ZigBee DTH - Executing configure() for device ${device.displayName}"
	def cmds = doConfigure()
	log.info "ZigBee DTH - configure() returning with cmds:- $cmds"
	cmds
}

/**
 * Returns the list of commands to be executed when the device is being configured/paired
 *
 */
def doConfigure() {
	log.trace "ZigBee DTH - Executing doConfigure() for device ${device.displayName}"
	state.configured = true
	// Device-Watch allows 2 check-in misses from device + ping (plus 2 min lag time)
	sendEvent(name: "checkInterval", value: 2 * 60 * 60 + 2 * 60, displayed: false, data: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID])

	def cmds =
		zigbee.configureReporting(CLUSTER_DOORLOCK, DOORLOCK_ATTR_LOCKSTATE,
								  DataType.ENUM8, 0, 3600, null) +
		zigbee.configureReporting(CLUSTER_POWER, POWER_ATTR_BATTERY_PERCENTAGE_REMAINING,
								  DataType.UINT8, 600, 21600, 0x01) +
		zigbee.configureReporting(CLUSTER_ALARM, ALARM_ATTR_ALARM_COUNT,
								  DataType.UINT16, 0, 21600, null)
	
	def allCmds = refresh() + cmds + reloadAllCodes()
	log.info "ZigBee DTH - doConfigure() returning with cmds:- $allCmds"
	allCmds // send refresh and reloadAllCodes cmds as part of configureDevice
}

/**
 * Executes lock command on a Zigbee lock
 */
def lock() {
	log.trace "ZigBee DTH - Executing lock() for device ${device.displayName}"
	def cmds = zigbee.command(CLUSTER_DOORLOCK, DOORLOCK_CMD_LOCK_DOOR)
	log.info "ZigBee DTH - lock() returning with cmds:- $cmds"
	return cmds
}

/**
 * Executes unlock command on a Zigbee lock
 */
def unlock() {
	log.trace "ZigBee DTH - Executing unlock() for device ${device.displayName}"
	def cmds = zigbee.command(CLUSTER_DOORLOCK, DOORLOCK_CMD_UNLOCK_DOOR)
	log.info "ZigBee DTH - unlock() returning with cmds:- $cmds"
	return cmds
}

/**
 * API endpoint for server smart app to scan the lock and populate the attributes. Called only when the attributes are not populated.
 *
 * @return cmds: The command(s) fired for reading attributes
 */
def reloadAllCodes() {
	log.trace "ZigBee DTH - Executing reloadAllCodes() for device ${device.displayName}"
	sendEvent(name: "scanCodes", value: "Scanning", descriptionText: "Code scan in progress", displayed: false)
	def lockCodes = loadLockCodes()
	sendEvent(lockCodesEvent(lockCodes))
	def cmds = validateAttributes()
	state.checkCode = 1
	cmds += requestCode(1)
	
	log.info "ZigBee DTH - reloadAllCodes() returning with cmds:- $cmds"
	return cmds
}

/**
 * API endpoint for setting a user code on a Zigbee lock
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
		log.trace "ZigBee DTH - Executing nameSlot() for device ${device.displayName}"
		nameSlot(codeID, codeName)
		return
	}
	
	log.trace "ZigBee DTH - Executing setCode() for device ${device.displayName}"
	if (isValidCodeID(codeID) && isValidCode(code)) {
		log.debug "Zigbee DTH - setting code in slot number $codeID"
		def cmds = []
		def attrCmds = validateAttributes()
		// Executing both user code set and get commands as Yale lock do not generate programming event while creating
		// a user code from the SmartApp
		def setPayload = getPayloadToSetCode(codeID, code)
		cmds << zigbee.command(CLUSTER_DOORLOCK, DOORLOCK_CMD_USER_CODE_SET, setPayload).first()
		cmds << requestCode(codeID).first()
		def strname = (codeName ?: "Code $codeID")
		state["setname$codeID"] = strname
		state["setcode$codeID"] = encrypt(code.toString())
		cmds = delayBetween(cmds, 4200)
		if(attrCmds) {
			cmds = attrCmds + cmds
		}
		return cmds
	} else {
		log.warn "Zigbee DTH - Invalid input: Unable to set code in slot number $codeID"
		return null
	}
}

/**
 * Validates attributes and if attributes are not populated, adds the command maps to list of commands
 * @return List of command maps or empty list
 */
def validateAttributes() {
	def cmds = []
	if (state.attrSendPinOta == null || state.attrSendPinOta == 0) {
		cmds += zigbee.writeAttribute(CLUSTER_DOORLOCK, DOORLOCK_ATTR_SEND_PIN_OTA, DataType.BOOLEAN, 1)
	}
	if(!device.currentValue("maxCodes")) {
		cmds += zigbee.readAttribute(CLUSTER_DOORLOCK, DOORLOCK_ATTR_NUM_PIN_USERS)
	}
	if(!device.currentValue("minCodeLength")) {
		cmds += zigbee.readAttribute(CLUSTER_DOORLOCK, DOORLOCK_ATTR_MIN_PIN_LENGTH)
	}
	if(!device.currentValue("maxCodeLength")) {
		cmds += zigbee.readAttribute(CLUSTER_DOORLOCK, DOORLOCK_ATTR_MAX_PIN_LENGTH)
	}
	if (state.attrSendPinOta == null || state.attrSendPinOta == 0) {
		cmds += zigbee.readAttribute(CLUSTER_DOORLOCK, DOORLOCK_ATTR_SEND_PIN_OTA)
	}
	cmds = cmds.flatten()
	log.trace "validateAttributes returning commands list: " + cmds
	cmds
}

/**
 * API endpoint for deleting a user code on a Zigbee lock
 *
 * @param codeID: The code slot number
 *
 * @returns cmds: The command fired for deletion of a lock code
 */
def deleteCode(codeID) {
	log.trace "ZigBee DTH - Executing deleteCode() for device ${device.displayName}"
	def cmds = []
	if (isValidCodeID(codeID)) {
		log.debug "Zigbee DTH - deleting code slot number $codeID"
		cmds = zigbee.command(CLUSTER_DOORLOCK, DOORLOCK_CMD_CLEAR_USER_CODE, getLittleEndianHexString(codeID))
	} else {
		log.warn "Zigbee DTH - Invalid input: Unable to delete slot number $codeID"
	}
	log.info "ZigBee DTH - deleteCode() returning with cmds:- $cmds"
	return cmds
}

/**
 * API endpoint for setting/deleting multiple user codes on a lock
 *
 * @param codeSettings: The map with code slot numbers and code pins (in case of update)
 *
 * @returns The commands fired for creation and deletion of lock codes
 */
def updateCodes(codeSettings) {
	log.trace "ZigBee DTH - Executing updateCodes() for device ${device.displayName}"
	if(codeSettings instanceof String) codeSettings = util.parseJson(codeSettings)
	def set_cmds = []
	def get_cmds = []
	codeSettings.each { name, updated ->
		if (name.startsWith("code")) {
			def n = name[4..-1].toInteger()
			if (updated && updated.size() >= 4 && updated.size() <= 8) {
				log.debug "Setting code number $n"
				def cmds = setCode(n, updated)
				set_cmds << cmds.first()
				get_cmds << cmds.last()
			} else if (updated == null || updated == "" || updated == "0") {
				log.debug "Deleting code number $n"
				def cmds = deleteCode(n)
				set_cmds << cmds.first()
			}
		} else log.warn("unexpected entry $name: $updated")
	}
	
	if (set_cmds && get_cmds) {
		def allCmds = []
		allCmds = delayBetween(set_cmds, 2200) + ["delay 2200"] + delayBetween(get_cmds, 4200)
		return response(allCmds)
	} else if (set_cmds) {
		return response(delayBetween(set_cmds, 4200))
	}
	return null
}

/**
 * Renames an existing lock code slot
 *
 * @param codeSlot: The code slot number
 *
 * @param codeName The new name of the code
 */
def nameSlot(codeSlot, codeName) {
	def lockCodes = loadLockCodes()
	codeSlot = codeSlot.toString()
	if (lockCodes[codeSlot]) {
		log.trace "ZigBee DTH - Executing nameSlot() for device ${device.displayName}"
		lockCodes[codeSlot] = (codeName ?: "Code $codeSlot")
		sendEvent(lockCodesEvent(lockCodes))
	}
}

/**
 * Constructs the ZigBee command for user code get
 *
 * @param codeID: The code slot number
 *
 * @return The command for user code get
 */
def requestCode(codeID) {
	return zigbee.command(CLUSTER_DOORLOCK, DOORLOCK_CMD_USER_CODE_GET, getLittleEndianHexString(codeID))
}

/**
 * Responsible for parsing incoming device messages to generate events
 *
 * @param description The incoming description from the device
 *
 * @return result: The list of events to be sent out
 *
 */
def parse(String description) {
	log.trace "ZigBee DTH - Executing parse() for device ${device.displayName}"
	def result = null
	if (description) {
		if (description.startsWith('read attr -')) {
			result = parseAttributeResponse(description)
		} else {
			result = parseCommandResponse(description)
		}
	}
	return result
}

/**
 * Responsible for handling attribute responses
 *
 * @param description The description to be parsed
 *
 * @return result: The list of events to be sent out
 */
private def parseAttributeResponse(String description) {
	Map descMap = zigbee.parseDescriptionAsMap(description)
	log.trace "ZigBee DTH - Executing parseAttributeResponse() for device ${device.displayName} with description map:- $descMap"
	def result = []
	Map responseMap = [:]
	def clusterInt = descMap.clusterInt
	def attrInt = descMap.attrInt
	def deviceName = device.displayName
	if (clusterInt == CLUSTER_POWER && attrInt == POWER_ATTR_BATTERY_PERCENTAGE_REMAINING) {
		responseMap.name = "battery"
		responseMap.value = Math.round(Integer.parseInt(descMap.value, 16) / 2)
		// Handling Yale locks incorrect battery reporting issue
		if (device.getDataValue("manufacturer") == "Yale") {
			responseMap.value = Integer.parseInt(descMap.value, 16)
		}
		responseMap.descriptionText = "Battery is at ${responseMap.value}%"
	} else if (clusterInt == CLUSTER_DOORLOCK && attrInt == DOORLOCK_ATTR_LOCKSTATE) {
		def value = Integer.parseInt(descMap.value, 16)
		responseMap.name = "lock"
		if (value == 0) {
			responseMap.value = "unknown"
			responseMap.descriptionText = "Unknown state"
		} else if (value == 1) {
			responseMap.value = "locked"
			responseMap.descriptionText = "Locked"
		} else if (value == 2) {
			responseMap.value = "unlocked"
			responseMap.descriptionText = "Unlocked"
		} else {
			responseMap.value = "unknown"
			responseMap.descriptionText = "Unknown state"
		}
	} else if (clusterInt == CLUSTER_DOORLOCK && attrInt == DOORLOCK_ATTR_MIN_PIN_LENGTH && descMap.value) {
		def minCodeLength = Integer.parseInt(descMap.value, 16)
		responseMap = [name: "minCodeLength", value: minCodeLength, descriptionText: "Minimum PIN length is ${minCodeLength}", displayed: false]
	} else if (clusterInt == CLUSTER_DOORLOCK && attrInt == DOORLOCK_ATTR_MAX_PIN_LENGTH && descMap.value) {
		def maxCodeLength = Integer.parseInt(descMap.value, 16)
		responseMap = [name: "maxCodeLength", value: maxCodeLength, descriptionText: "Maximum PIN length is ${maxCodeLength}", displayed: false]
	} else if (clusterInt == CLUSTER_DOORLOCK && attrInt == DOORLOCK_ATTR_NUM_PIN_USERS && descMap.value) {
		def maxCodes = Integer.parseInt(descMap.value, 16)
		responseMap = [name: "maxCodes", value: maxCodes, descriptionText: "Maximum Number of user codes supported is ${maxCodes}", displayed: false]
	} else if (clusterInt == CLUSTER_DOORLOCK && attrInt == DOORLOCK_ATTR_SEND_PIN_OTA && descMap.value) {
		state.attrSendPinOta = Integer.parseInt(descMap.value, 16)
		return null
	} else {
		log.trace "ZigBee DTH - parseAttributeResponse() - ignoring attribute response"
		return null
	}
	
	if (responseMap.data) {
		responseMap.data.lockName = deviceName
	} else {
		responseMap.data = [ lockName: deviceName ]
	}
	result << createEvent(responseMap)
	log.info "ZigBee DTH - parseAttributeResponse() returning with result:- $result"
	return result
}

/**
 * Responsible for handling command responses
 *
 * @param description The description to be parsed
 *
 * @return result: The list of events to be sent out
 */
private def parseCommandResponse(String description) {
	Map descMap = zigbee.parseDescriptionAsMap(description)
	def deviceName = device.displayName
	log.trace "ZigBee DTH - Executing parseCommandResponse() for device ${deviceName}"
	
	def result = []
	Map responseMap = [:]
	def data = descMap.data
	def lockCodes = loadLockCodes()
	
	def cmd = descMap.commandInt
	def clusterInt = descMap.clusterInt
	
	if (clusterInt == CLUSTER_DOORLOCK && cmd == DOORLOCK_RESPONSE_OPERATION_EVENT) {
		log.trace "ZigBee DTH - Executing DOORLOCK_RESPONSE_OPERATION_EVENT for device ${deviceName} with description map:- $descMap"
		def eventSource = Integer.parseInt(data[0], 16)
		def eventCode = Integer.parseInt(data[1], 16)
		
		responseMap.name = "lock"
		responseMap.displayed = true
		responseMap.isStateChange = true
		
		def desc = ""
		def codeName = ""
		
		if (eventSource == 0) {
			def codeID = Integer.parseInt(data[3] + data[2], 16)
			codeName = getCodeName(lockCodes, codeID)
			responseMap.data = [ usedCode: codeID, codeName: codeName, method: "keypad" ]
		} else if (eventSource == 1) {
			responseMap.data = [ method: "command" ]
		} else if (eventSource == 2) {
			desc = "manually"
			responseMap.data = [ method: "manual" ]
		}
		
		switch (eventCode) {
			case 1:
				responseMap.value = "locked"
				if(codeName) {
					responseMap.descriptionText = "\"$codeName\" locked the lock"
				} else {
					responseMap.descriptionText = "Locked ${desc}"
				}
				break
			case 2:
				responseMap.value = "unlocked"
				if(codeName) {
					responseMap.descriptionText = "\"$codeName\" unlocked the lock"
				} else {
					responseMap.descriptionText = "Unlocked ${desc}"
				}
				break
			case 3: //Lock Failure Invalid Pin
				break
			case 4: //Lock Failure Invalid Schedule
				break
			case 5: //Unlock Invalid PIN
				break
			case 6: //Unlock Invalid Schedule
				break
			case 7: // locked by touching the keypad
			case 8: // locked using the key
			case 13: // locked using the Thumbturn
				responseMap.value = "locked"
				responseMap.descriptionText = "Locked ${desc}"
				break
			case 9: // unlocked using the key
			case 14: // unlocked using the Thumbturn
				responseMap.value = "unlocked"
				responseMap.descriptionText = "Unlocked ${desc}"
				break
			case 10: //Auto lock
				responseMap.value = "locked"
				responseMap.descriptionText = "Auto locked"
				responseMap.data = [ method: "auto" ]
				break
			default:
				break
		}
	} else if (clusterInt == CLUSTER_DOORLOCK && cmd == DOORLOCK_CMD_USER_CODE_SET) {
		log.trace "ZigBee DTH - Executing DOORLOCK_CMD_USER_CODE_SET for device ${deviceName} with description map:- $descMap"
		def status = Integer.parseInt(data[0], 16)
		switch (status) {
			case 0:
				log.debug "Lock code creation successful"
				// Lock code creation is successful but we do not have the codeID/code number here.
				// Hence, code creation success event will be sent from DOORLOCK_CMD_USER_CODE_GET response.
				break
			case 1:
				log.debug "Lock code creation failed - General failure"
				break
			case 2:
				log.debug "Lock code creation failed - Memory full"
				break
			case 3:
				log.debug "Lock code creation failed - Duplicate Code error"
				break
			default:
				break
		}
	} else if (clusterInt == CLUSTER_DOORLOCK && cmd == DOORLOCK_RESPONSE_PROGRAMMING_EVENT) {
		log.trace "ZigBee DTH - Executing DOORLOCK_RESPONSE_PROGRAMMING_EVENT for device ${deviceName} with description map:- $descMap"
		// Programming event is generated when the user creates/updates/deletes a code manually on the lock.
		// Ideally it should be generated even when the user tries to create/update a code through the
		// SmartApp as well, but that is not the case with Yale locks

		responseMap.name = "codeChanged"
		responseMap.isStateChange = true
		responseMap.displayed = true
		
		def codeID = Integer.parseInt(data[3] + data[2], 16)
		def codeName
		
		def eventCode = Integer.parseInt(data[1], 16)
		switch (eventCode) {
			case 1: // MasterCodeChanged
				codeName = "Master Code"
				responseMap.value = "0 set"
				responseMap.descriptionText = "${getStatusForDescription('set')} \"Master Code\""
				responseMap.data = [ codeName: codeName, notify: true, notificationText: "${getStatusForDescription('set')} \"$codeName\" in $deviceName at ${location.name}" ]
				break
			case 3: // PINCodeDeleted
				if (codeID == 255) {
					result = allCodesDeletedEvent()
					responseMap.value = "all deleted"
					responseMap.descriptionText = "Deleted all user codes"
					responseMap.data = [notify: true, notificationText: "Deleted all user codes in $deviceName at ${location.name}"]
					result << createEvent(name: "lockCodes", value: util.toJson([:]), displayed: false, descriptionText: "'lockCodes' attribute updated")
				} else {
					codeName = getCodeName(lockCodes, codeID)
					responseMap.value = "$codeID deleted"
					responseMap.descriptionText = "Deleted \"$codeName\""
					responseMap.data = [ codeName: codeName, notify: true, notificationText: "Deleted \"$codeName\" in $deviceName at ${location.name}" ]
					result << codeDeletedEvent(lockCodes, codeID)
				}
				break
			case 2: // PINCodeAdded
			case 4: // PINCodeChanged
				codeName = getCodeNameFromState(lockCodes, codeID)
				def changeType = getChangeType(lockCodes, codeID)
				responseMap.value = "$codeID $changeType"
				responseMap.descriptionText = "${getStatusForDescription(changeType)} \"$codeName\""
				responseMap.data = [ codeName: codeName, notify: true, notificationText: "${getStatusForDescription(changeType)} \"$codeName\" in $deviceName at ${location.name}" ]
				result << codeSetEvent(lockCodes, codeID, codeName)
				break
			default:
				break
		}
	} else if (clusterInt == CLUSTER_DOORLOCK && cmd == DOORLOCK_CMD_USER_CODE_GET) {
		log.trace "ZigBee DTH - Executing DOORLOCK_CMD_USER_CODE_GET for device ${deviceName}"
		// This is called only when the user creates/updates a code using the SmartApp or when the user tries
		// to scan the lock by calling reloadAllCodes()
		
		def userStatus = Integer.parseInt(data[2], 16)
		def codeID = Integer.parseInt(data[1] + data[0], 16)
		
		// PIN code saved in the state
		def localCode = decrypt(state["setcode$codeID"])
		
		def codeName = getCodeNameFromState(lockCodes, codeID)
		
		responseMap.name = "codeChanged"
		responseMap.isStateChange = true
		responseMap.displayed = true
		
		// userStatus = 1 indicates that the code slot is occupied
		if (userStatus == 1) {
			if (localCode) {
				// This will be applicable for both create and update through the SmartApp
				
				// PIN code fetched from the lock
				def serverCode = getCodeFromOctet(data)
				if (localCode == serverCode) {
					// Code set successfully
					log.debug "Code matches - lock code creation successful"
					def changeType = getChangeType(lockCodes, codeID)
					responseMap.value = "$codeID $changeType"
					responseMap.descriptionText = "${getStatusForDescription(changeType)} \"$codeName\""
					responseMap.data = [ codeName: codeName, notify: true, notificationText: "${getStatusForDescription(changeType)} \"$codeName\" in $deviceName at ${location.name}" ]
					result << codeSetEvent(lockCodes, codeID, codeName)
				} else {
					// Code update failed
					log.debug "Code update failed"
					responseMap.value = "$codeID failed"
					responseMap.descriptionText = "Failed to update code '$codeName'"
					//It should be OK to mark this as duplicate pin code error because in case lock batteries are down,
					// or lock is out of range, or there is wireless interference, the Lock will not be able to respond
					// back with user code get response.
					responseMap.data = [isCodeDuplicate: true]
				}
			} else {
				// This will be applicable when a slot is found occupied during scanning of lock
				// Populating the 'lockCodes' attribute after scanning a code slot
				log.debug "Scanning lock - code $codeID is occupied"
				responseMap.value = "$codeID set"
				responseMap.descriptionText = "${getStatusForDescription('set')} \"$codeName\""
				// We don't show notification for codes that are scanned
				responseMap.data = [ codeName: codeName ]
				result << codeSetEvent(lockCodes, codeID, codeName)
			}
		} else {
			// Code slot is empty - can happen when code creation fails or a slot is empty while scanning the lock
			if (localCode != null) {
				// Code slot found empty during creation of a user code
				log.debug "Code creation failed"
				responseMap.value = "$codeID failed"
				responseMap.descriptionText = "Failed to set code '$codeName'"
				//It should be OK to mark this as duplicate pin code error because in case lock batteries are down,
				// or lock is out of range, or there is wireless interference, the Lock will not be able to respond
				// back with user code get response.
				responseMap.data = [isCodeDuplicate: true]
				
				def codeReportMap = [ name: "codeReport", value: codeID, data: [ code: "" ], isStateChange: true, displayed: false ]
				codeReportMap.descriptionText = "Code $codeID is not set"
				result << createEvent(codeReportMap)
			} else {
				// Code slot found empty while scanning the lock
				responseMap.value = "$codeID unset"
				responseMap.descriptionText = "Code slot $codeID found empty during scanning"
				//This event will not be generated at all since isStateChange is false
				responseMap.isStateChange = false
				responseMap.displayed = false
			}
		}
		clearStateForSlot(codeID)
		
		if (codeID == state.checkCode) {
			log.debug "Code scanning in progress..."
			def maxCodes = (device.currentValue("maxCodes") == null) ? 8 : device.currentValue("maxCodes")
			maxCodes = 8
			if (state.checkCode >= maxCodes) {
				log.debug "Code scanning complete"
				state["checkCode"] = null
				sendEvent(name: "scanCodes", value: "Complete", descriptionText: "Code scan completed", displayed: false)
			} else {
				log.debug "More codes to scan..."
				state.checkCode = state.checkCode + 1
				result << response(requestCode(state.checkCode))
			}
		}
	} else if (clusterInt == CLUSTER_ALARM && cmd == ALARM_CMD_ALARM) {
		log.trace "ZigBee DTH - Executing ALARM_CMD_ALARM for device ${deviceName} with description map:- $descMap"
		def alarmCode = Integer.parseInt(data[0], 16)
		switch (alarmCode) {
			case 0: // Deadbolt Jammed
				responseMap = [ name: "lock", value: "unknown", descriptionText: "Was in unknown state" ]
				break
			case 1: // Lock Reset to Factory Defaults
				responseMap = [ name: "lock", value: "unknown", descriptionText: "Has been reset to factory defaults" ]
				break
			case 2: // Reserved
				break
			case 3: // RF Module Power Cycled
				responseMap = [ descriptionText: "Batteries replaced", isStateChange: true ]
				break
			case 4: // Tamper Alarm - wrong code entry limit
				responseMap = [ name: "tamper", value: "detected", descriptionText: "Keypad attempts exceed code entry limit", isStateChange: true ]
				break
			case 5: // Tamper Alarm - front escutcheon removed from main
				responseMap = [ name: "tamper", value: "detected", descriptionText: "Front escutcheon removed", isStateChange: true ]
				break
			case 6: // Forced Door Open under Door Locked Condition
				responseMap = [ name: "tamper", value: "detected", descriptionText: "Door forced open under door locked condition", isStateChange: true ]
				break
			case 16: // Battery too low to operate
				responseMap = [ name: "battery", value: device.currentValue("battery"), descriptionText: "Battery too low to operate lock", isStateChange: true ]
				break
			case 17: // Battery level critical
				responseMap = [ name: "battery", value: device.currentValue("battery"), descriptionText: "Battery level critical", isStateChange: true ]
				break
			case 18: // Battery very low
				responseMap = [ name: "battery", value: device.currentValue("battery"), descriptionText: "Battery very low", isStateChange: true ]
				break
			case 19: // Battery low
				responseMap = [ name: "battery", value: device.currentValue("battery"), descriptionText: "Battery low", isStateChange: true ]
				break
			default:
				break
		}
	} else {
		log.trace "ZigBee DTH - parseCommandResponse() - ignoring command response"
	}
	
	if(responseMap["value"]) {
		if (responseMap.data) {
			responseMap.data.lockName = deviceName
		} else {
			responseMap.data = [ lockName: deviceName ]
		}
		result << createEvent(responseMap)
	}
	if (result) {
		result = result.flatten()
	} else {
		result = null
	}
	log.debug "ZigBee DTH - parseCommandResponse() returning with result:- $result"
	return result
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
private def codeDeletedEvent(lockCodes, codeID) {
	lockCodes.remove("$codeID".toString())
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
		result << createEvent(name: "codeChanged", value: "$id deleted",
		data: [ codeName: codeName, lockName: deviceName,
			notify: true, notificationText: "Deleted \"$codeName\" in $deviceName at ${location.name}" ],
		descriptionText: "Deleted \"$codeName\"",
		displayed: true, isStateChange: true)
		clearStateForSlot(id)
	}
	result
}

/**
 * Populates the 'lockCodes' attribute by calling send event
 *
 * @param lockCodes The codes in a lock
 */
private Map lockCodesEvent(lockCodes) {
	createEvent(name: "lockCodes", value: util.toJson(lockCodes), displayed: false, descriptionText: "'lockCodes' attribute updated")
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
 * Converts the code octet to code PIN
 *
 * @param data The data map returned in case of user code get
 *
 * @return code: The code string
 */
private def getCodeFromOctet(data) {
	def code = ""
	def codeLength = Integer.parseInt(data[4], 16)
	if (codeLength >= device.currentValue("minCodeLength") && codeLength <= device.currentValue("maxCodeLength")) {
		for (def i = 5; i < (5 + codeLength); i++) {
			code += (char) (zigbee.convertHexToInt(data[i]))
		}
	}
	return code
}

/**
 * Checks if the slot number is within the allowed limits
 *
 * @param codeID The code slot number
 *
 * @return true if valid, false if not
 */
private boolean isValidCodeID(codeID) {
	def maxCodes = device.currentValue("maxCodes") ?: 250
	if (codeID.toInteger() >= 1 && codeID.toInteger() <= maxCodes) {
		return true
	}
	return false
}

/**
 * Checks if the code PIN is valid
 *
 * @param code The code PIN
 *
 * @return true if valid, false if not
 */
private boolean isValidCode(code) {
	def minCodeLength = device.currentValue("minCodeLength") ?: 4
	def maxCodeLength = device.currentValue("maxCodeLength") ?: 8
	if (code.toString().size() <= maxCodeLength && code.toString().size() >= minCodeLength && code.isNumber()) {
		return true
	}
	return false
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
 * Clears the code name and pin from the state basis the code slot number
 *
 * @param codeID: The code slot number
 */
def clearStateForSlot(codeID) {
	state.remove("setname$codeID")
	state["setname$codeID"] = null
	state.remove("setcode$codeID")
	state["setcode$codeID"] = null
}

/**
 * Forms the command for writing the 'language' attribute to the device
 *
 * @param doorLockClusterId: Door lock cluster id
 *
 * @param param: The map with attribute id, type, and allowed values of the configurable parameters in a lock
 *
 * @returns cmdList: ZigBee write attribute command for the 'language' attribute
 */
def getZigbeeCommandForSettingLanguage(doorLockClusterId, param) {
	//Dev-Conn not respecting byte order hence hex string needs to be changed to little endian format
	def cmdList = zigbee.writeAttribute(doorLockClusterId, param.language.attrId, param.language.type, getConfigValueFor("language", param))
	//Converting {026E65} to {6E6502}
	def startIndex = cmdList[0].indexOf("{") + 1
	def endIndex = cmdList[0].indexOf("}")
	def hexStrInBigEndian = cmdList[0].substring(startIndex, endIndex)
	def hexStrInLittleEndian = zigbee.swapEndianHex(hexStrInBigEndian)
	def cmdBuilder = new StringBuilder(cmdList[0])
	cmdBuilder.replace(startIndex, endIndex, hexStrInLittleEndian)
	cmdList[0] = cmdBuilder.toString()
	cmdList
}

/**
 * Constructs the payload for setting a code
 *
 * @param codeID: The code slot number
 *
 * @param code: The code PIN
 *
 * @returns payload: The payload for setting a code
 */
def getPayloadToSetCode(codeID, code) {
	def payload = "" + getLittleEndianHexString(codeID)
	payload += " " + getUserStatusForOccupied() + " " + getDefaultUserType()
	payload += " " + getOctetStringForCode(code)
	payload
}

/**
 * Returns the value 1 (Occupied/Enabled) for user status
 */
def getUserStatusForOccupied() {
	return "01"
}

/**
 * Returns the value 0 (Unrestricted User - default) for user type
 */
def getDefaultUserType() {
	return "00"
}

/**
 * Converts the code PIN to octet string
 *
 * @param code The code PIN
 *
 * @return octetCode: The code equivalent in octet string
 */
def getOctetStringForCode(code) {
	def octetStr = "" + zigbee.convertToHexString(code.length(), 2)
	for(int i = 0; i < code.length(); i++) {
		octetStr += " " +  zigbee.convertToHexString((int) code.charAt(i), 2)
	}
	octetStr
}

/**
 * Returns hex string in little endian format
 */
def getLittleEndianHexString(numStr) {
	return zigbee.swapEndianHex(zigbee.convertToHexString(numStr.toInteger(), 4))
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
