/**
 * 	Z-Wave Lock
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
	definition (name: "Z-Wave Lock", namespace: "smartthings", author: "SmartThings") {
		capability "Actuator"
		capability "Lock"
		capability "Polling"
		capability "Refresh"
		capability "Sensor"
		capability "Lock Codes"
		capability "Battery"
		capability "Health Check"
		capability "Configuration"
		
		// Generic
		fingerprint deviceId: "0x4003", inClusters: "0x98"
		fingerprint deviceId: "0x4004", inClusters: "0x98"
		// KwikSet
		fingerprint mfr:"0090", prod:"0001", model:"0236", deviceJoinName: "KwikSet SmartCode 910 Deadbolt Door Lock"
		fingerprint mfr:"0090", prod:"0003", model:"0238", deviceJoinName: "KwikSet SmartCode 910 Deadbolt Door Lock"
		fingerprint mfr:"0090", prod:"0001", model:"0001", deviceJoinName: "KwikSet SmartCode 910 Contemporary Deadbolt Door Lock"
		fingerprint mfr:"0090", prod:"0003", model:"0339", deviceJoinName: "KwikSet SmartCode 912 Lever Door Lock"
		fingerprint mfr:"0090", prod:"0003", model:"4006", deviceJoinName: "KwikSet SmartCode 914 Deadbolt Door Lock" //backlit version
		fingerprint mfr:"0090", prod:"0003", model:"0440", deviceJoinName: "KwikSet SmartCode 914 Deadbolt Door Lock"
		fingerprint mfr:"0090", prod:"0001", model:"0642", deviceJoinName: "KwikSet SmartCode 916 Touchscreen Deadbolt Door Lock"
		fingerprint mfr:"0090", prod:"0003", model:"0642", deviceJoinName: "KwikSet SmartCode 916 Touchscreen Deadbolt Door Lock"
		// Schlage
		fingerprint mfr:"003B", prod:"6341", model:"0544", deviceJoinName: "Schlage Camelot Touchscreen Deadbolt Door Lock"
		fingerprint mfr:"003B", prod:"6341", model:"5044", deviceJoinName: "Schlage Century Touchscreen Deadbolt Door Lock"
		fingerprint mfr:"003B", prod:"634B", model:"504C", deviceJoinName: "Schlage Connected Keypad Lever Door Lock"
		// Yale
		fingerprint mfr:"0129", prod:"0002", model:"0800", deviceJoinName: "Yale Touchscreen Deadbolt Door Lock" // YRD120
		fingerprint mfr:"0129", prod:"0002", model:"0000", deviceJoinName: "Yale Touchscreen Deadbolt Door Lock" // YRD220, YRD240
		fingerprint mfr:"0129", prod:"0002", model:"FFFF", deviceJoinName: "Yale Touchscreen Lever Door Lock" // YRD220
		fingerprint mfr:"0129", prod:"0004", model:"0800", deviceJoinName: "Yale Push Button Deadbolt Door Lock" // YRD110
		fingerprint mfr:"0129", prod:"0004", model:"0000", deviceJoinName: "Yale Push Button Deadbolt Door Lock" // YRD210
		fingerprint mfr:"0129", prod:"0001", model:"0000", deviceJoinName: "Yale Push Button Lever Door Lock" // YRD210
		fingerprint mfr:"0129", prod:"8002", model:"0600", deviceJoinName: "Yale Assure Lock" //YRD416, YRD426, YRD446
		fingerprint mfr:"0129", prod:"0007", model:"0001", deviceJoinName: "Yale Keyless Connected Smart Door Lock"
		fingerprint mfr:"0129", prod:"8004", model:"0600", deviceJoinName: "Yale Assure Lock Push Button Deadbolt" //YRD216
		// Samsung
		fingerprint mfr:"022E", prod:"0001", model:"0001", deviceJoinName: "Samsung Digital Lock" // SHP-DS705, SHP-DHP728, SHP-DHP525
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
				attributeState "unlocked with timeout", label:'unlocked', action:"lock.lock", icon:"st.locks.lock.unlocked", backgroundColor:"#ffffff", nextState:"locking"
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

import java.text.SimpleDateFormat

import physicalgraph.zwave.commands.doorlockv1.*
import physicalgraph.zwave.commands.usercodev1.*

private def getSCHEDULE_TYPE_DAILY_REPEATING() { "dailyRepeating" }
private def getSCHEDULE_TYPE_WEEK_DAY() { "weekDay" }
private def getSCHEDULE_TYPE_YEAR_DAY() { "yearDay" }
private def getSCHEDULE_ENTRY_LOCK_CLASS_ID() { "4E" }
private def getUSER_TYPE_OWNER() { 1 }
private def getUSER_TYPE_WEEK_DAY() { 4 }
private def getUSER_TYPE_YEAR_DAY() { 3 }
private def getMAX_CODES_TO_SCAN() { 8 }

/**
 * Called on app installed
 */
def installed() {
	// Device-Watch pings if no device events received for 1 hour (checkInterval)
	sendEvent(name: "checkInterval", value: 1 * 60 * 60, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID, offlinePingable: "1"])
}

/**
 * Called on app uninstalled
 */
def uninstalled() {
	def deviceName = device.displayName
	log.trace "[DTH] Executing 'uninstalled()' for device $deviceName"
	sendEvent(name: "lockRemoved", value: device.id, isStateChange: true, displayed: false)
}

/**
 * Executed when the user taps on the 'Done' button on the device settings screen. Sends the values to lock.
 *
 * @return hubAction: The commands to be executed
 */
def updated() {
	// Device-Watch pings if no device events received for 1 hour (checkInterval)
	sendEvent(name: "checkInterval", value: 1 * 60 * 60, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID, offlinePingable: "1"])
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
	def cmds = []
	if (!state.configured) {
		// configureScheduling will be called in doConfigure so this will take care of scenario when lock is paired freshly
		cmds = doConfigure()
	} else {
		// In case a lock is already paired then it needs to be configured for scheduling
		cmds << setClock()
		cmds << configureScheduling()
		cmds = delayBetween(cmds, 200)
	}
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
	def cmds = []
	cmds << secure(zwave.doorLockV1.doorLockOperationGet())
	cmds << secure(zwave.batteryV1.batteryGet())
	if (isSchlageLock()) {
		cmds << secure(zwave.configurationV2.configurationGet(parameterNumber: getSchlageLockParam().codeLength.number))
	}
	cmds << setClock()
	cmds << configureScheduling()
	// delayBetween does the flattening
	cmds = delayBetween(cmds, 200)
	log.debug "Do configure returning with commands := $cmds"
	cmds
}

/**
 * Returns commands to be send to lock for scheduling related parameters
 * @return - Commands to be send to lock if lock supports scheduling, empty list otherwise
 */
def configureScheduling() {
	def cmds = []
	if(device.currentValue("numberOfSlotsDailyRepeating") == null ||
	device.currentValue("numberOfSlotsWeekDay") == null ||
	device.currentValue("numberOfSlotsYearDay") == null) {
		// Some schedule slots are never read before
		if (SCHEDULE_ENTRY_LOCK_CLASS_ID in zwaveInfo.sec) {
			log.debug "[DTH] supports ScheduleEntryLock command class"
			cmds << secure(zwave.scheduleEntryLockV3.scheduleEntryTypeSupportedGet())
		} else {
			log.debug "[DTH] doesn't support ScheduleEntryLock command class"
			sendEvent(name: "numberOfSlotsDailyRepeating", value: 0, displayed: false, descriptionText: "No. of daily repeating slots supported is 0")
			sendEvent(name: "numberOfSlotsWeekDay", value: 0, displayed: false, descriptionText: "No. of week day slots supported is 0")
			sendEvent(name: "numberOfSlotsYearDay", value: 0, displayed: false, descriptionText: "No. of year day slots supported is 0")
		}
	}
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
 * Responsible for parsing ConfigurationReport command
 *
 * @param cmd: The ConfigurationReport command to be parsed
 *
 * @return The event(s) to be sent out
 */
def zwaveEvent(physicalgraph.zwave.commands.configurationv2.ConfigurationReport cmd) {
	log.trace "[DTH] Executing 'zwaveEvent(physicalgraph.zwave.commands.configurationv2.ConfigurationReport cmd)' with cmd = $cmd"
	if (isSchlageLock() && cmd.parameterNumber == getSchlageLockParam().codeLength.number) {
		def result = []
		def length = cmd.scaledConfigurationValue
		def deviceName = device.displayName
		log.trace "[DTH] Executing 'ConfigurationReport' for device $deviceName with code length := $length"
		def codeLength = device.currentValue("codeLength")
		if (codeLength && codeLength != length) {
			log.trace "[DTH] Executing 'ConfigurationReport' for device $deviceName - all codes deleted"
			result = allCodesDeletedEvent()
			result << createEvent(name: "codeChanged", value: "all deleted", descriptionText: "Deleted all user codes",
			isStateChange: true, data: [lockName: deviceName, notify: true,
				notificationText: "Deleted all user codes in $deviceName at ${location.name}"])
			result << createEvent(name: "lockCodes", value: util.toJson([:]), displayed: false, descriptionText: "'lockCodes' attribute updated")
			result << createEvent(name: "lockCodesData", value: util.toJson([:]), displayed: false, descriptionText: "'lockCodesData' attribute updated")
		}
		result << createEvent(name:"codeLength", value: length, descriptionText: "Code length is $length", displayed: false)
		return result
	} else if (isKwiksetLock()) {
		def result = []
		def codeID = cmd.parameterNumber
		def scheduleType = getScheduleType(codeID)
		def lockCodes = loadLockCodes()
		def codeName = getCodeName(lockCodes, codeID)
		def userType = cmd.scaledConfigurationValue
		log.trace "[DTH] Executing 'ConfigurationReport()' with codeID := $codeID, scheduleType := $scheduleType, and userType := $userType"
		if (isScheduleScanningInProgress(codeID) || scheduleType == "always" || scheduleType == "unknown") {
			// We will land here when the schedules are being scanned
			if (userType == USER_TYPE_OWNER) {
				// no schedules exists for this code
				setUserInfo([codeID: "" + codeID, phoneNum: "", accessType: "always", lastUsage: new Date().getTime()])
				log.trace "[DTH] ConfigurationReport() -  Schedule scanning complete for codeID := $codeID"
				checkAndScanNextSchedule(codeID)
			} else if (userType == USER_TYPE_YEAR_DAY) {
				// year day schedule exists for this code
				log.trace "[DTH] ConfigurationReport() -  Requesting year day schedule for codeID := $codeID"
				result << response(requestYearDaySchedule(codeID))
			} else if (userType == USER_TYPE_WEEK_DAY) {
				// week day schedule exists for this code
				log.trace "[DTH] ConfigurationReport() -  Requesting week day schedule for codeID := $codeID"
				clearWeekdayScanningDataFromState(codeID)
				result << response(requestAllWeekDaySchedule(codeID))
			}
		} else {
			// We will land here when a schedule is being cleared by changing the user type
			if (userType == USER_TYPE_OWNER) {
				log.trace "[DTH] ConfigurationReport() -  $scheduleType deleted successfully for codeID := $codeID by changing the user type"
				result << scheduleDeleteEvent(codeID)
				def map = null
				def deviceName = device.displayName
				if (scheduleType == SCHEDULE_TYPE_YEAR_DAY) {
					map = [name: "scheduleChanged", value: "$codeID $SCHEDULE_TYPE_YEAR_DAY deleted", descriptionText: "Cleared limited schedule for \"$codeName\"", isStateChange: true, displayed: true]
					map.data = [lockName: deviceName, notify: true, notificationText: "Cleared limited schedule for \"$codeName\" in $deviceName at ${location.name}"]
					result << createEvent(map)
				} else if (scheduleType == SCHEDULE_TYPE_WEEK_DAY) {
					map = [name: "scheduleChanged", value: "$codeID $SCHEDULE_TYPE_WEEK_DAY deleted", descriptionText: "Cleared recurring schedule for \"$codeName\"", isStateChange: true, displayed: true]
					map.data = [lockName: deviceName, notify: true, notificationText: "Cleared recurring schedule for \"$codeName\" in $deviceName at ${location.name}"]
					result << createEvent(map)
				}
			} else {
				log.trace "[DTH] ConfigurationReport() -  $scheduleType deleted failed for codeID := $codeID by changing the user type"
				if (scheduleType == SCHEDULE_TYPE_YEAR_DAY) {
					result << createEvent(name: "scheduleChanged", value: "$codeID $SCHEDULE_TYPE_YEAR_DAY delete failed", descriptionText: "Limited schedule delete failed for \"$codeName\"", isStateChange: true, displayed: false)
				} else if (scheduleType == SCHEDULE_TYPE_WEEK_DAY) {
					result << createEvent(name: "scheduleChanged", value: "$codeID $SCHEDULE_TYPE_WEEK_DAY delete failed", descriptionText: "Recurring schedule delete failed for \"$codeName\"", isStateChange: true, displayed: false)
				}
			}
		}
		return result
	}
	return null
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
	
	// DoorLockOperationReport is called when trying to read the lock state or when the lock is locked/unlocked from the DTH or the smart app
	def map = [ name: "lock" ]
	map.data = [ lockName: device.displayName ]
	if (cmd.doorLockMode == 0xFF) {
		map.value = "locked"
		map.descriptionText = "Locked"
	} else if (cmd.doorLockMode >= 0x40) {
		map.value = "unknown"
		map.descriptionText = "Unknown state"
	} else if (cmd.doorLockMode == 0x01) {
		map.value = "unlocked with timeout"
		map.descriptionText = "Unlocked with timeout"
		map.data.notify = true
		map.data.notificationText = "${device.displayName} at ${location.name} was unlocked with timeout"
	}  else {
		map.value = "unlocked"
		map.descriptionText = "Unlocked"
		map.data.notify = true
		map.data.notificationText = "${device.displayName} at ${location.name} was unlocked"
		if (state.assoc != zwaveHubNodeId) {
			result << response(secure(zwave.associationV1.associationSet(groupingIdentifier:1, nodeId:zwaveHubNodeId)))
			result << response(zwave.associationV1.associationSet(groupingIdentifier:2, nodeId:zwaveHubNodeId))
			result << response(secure(zwave.associationV1.associationGet(groupingIdentifier:1)))
		}
	}
	if (generatesDoorLockOperationReportBeforeAlarmReport()) {
		// we're expecting lock events to come after notification events, but for specific yale locks they come out of order
		runIn(3, "delayLockEvent", [data: [map: map]])
        return [:]
	} else {
		return result ? [createEvent(map), *result] : createEvent(map)
	}
}

def delayLockEvent(data) {
	log.debug "Sending cached lock operation: $data.map"
	sendEvent(data.map)
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
			map.data = [ method: "manual", notify: true, notificationText: "$deviceName at ${location.name} was unlocked manually" ]
			break
		case 3: // Locked by command
			map.descriptionText = "Locked"
			map.data = [ method: "command" ]
			break
		case 4: // Unlocked by command
			map.descriptionText = "Unlocked"
			map.data = [ method: "command", notify: true, notificationText: "$deviceName at ${location.name} was unlocked" ]
			break
		case 5: // Locked with keypad
			if (cmd.eventParameter || cmd.alarmLevel) {
				codeID = readCodeSlotId(cmd)
				codeName = getCodeName(lockCodes, codeID)
				map.descriptionText = "Locked by \"$codeName\""
				map.data = [ usedCode: codeID, codeName: codeName, method: "keypad" ]
			} else {
				// locked by pressing the Schlage button
				map.descriptionText = "Locked manually"
			}
			break
		case 6: // Unlocked with keypad
			if (cmd.eventParameter || cmd.alarmLevel) {
				codeID = readCodeSlotId(cmd)
				codeName = getCodeName(lockCodes, codeID)
				map.descriptionText = "Unlocked by \"$codeName\""
				map.data = [ usedCode: codeID, codeName: codeName, method: "keypad", notify: true, notificationText: "$deviceName at ${location.name} was unlocked by \"$codeName\"" ]
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
			result << createEvent(name: "lockCodesData", value: util.toJson([:]), displayed: false, descriptionText: "'lockCodesData' attribute updated")
			break
		case 0xD: // User code deleted
			if (cmd.eventParameter || cmd.alarmLevel) {
				codeID = readCodeSlotId(cmd)
				if (lockCodes[codeID.toString()]) {
					codeName = getCodeName(lockCodes, codeID)
					map = [ name: "codeChanged", value: "$codeID deleted", isStateChange: true ]
					map.descriptionText = "Deleted \"$codeName\""
					map.data = [ codeName: codeName, notify: true, notificationText: "Deleted \"$codeName\" in $deviceName at ${location.name}" ]
					result << codeDeletedEvent(lockCodes, codeID)
					result << clearUserInfo(codeID)
				}
			}
			break
		case 0xE: // Master or user code changed/set
			if (cmd.eventParameter || cmd.alarmLevel) {
				codeID = readCodeSlotId(cmd)
				if(codeID == 0 && isKwiksetLock()) {
					//Ignoring this AlarmReport as Kwikset reports codeID 0 when all slots are full and user tries to set another lock code manually
					//Kwikset locks don't send AlarmReport when Master code is set
					log.trace "Ignoring this alarm report in case of Kwikset locks"
					break
				}
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
				codeID = readCodeSlotId(cmd)
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
				codeID = readCodeSlotId(cmd)
				codeName = getCodeName(lockCodes, codeID)
				map.descriptionText = "Unlocked by \"$codeName\""
				map.data = [ usedCode: codeID, codeName: codeName, method: "keypad", notify: true, notificationText: "$deviceName at ${location.name} was unlocked by \"$codeName\"" ]
			}
			break
		case 18: // Locked with keypad
			codeID = readCodeSlotId(cmd)
			map = [ name: "lock", value: "locked" ]
			// Kwikset lock reporting code id as 0 when locked using the lock keypad button
			if (isKwiksetLock() && codeID == 0) {
				map.descriptionText = "Locked manually"
				map.data = [ method: "manual" ]
			} else {
				codeName = getCodeName(lockCodes, codeID)
				map.descriptionText = "Locked by \"$codeName\""
				map.data = [ usedCode: codeID, codeName: codeName, method: "keypad" ]
			}
			break
		case 21: // Manually locked
			map = [ name: "lock", value: "locked", data: [ method: (cmd.alarmLevel == 2) ? "keypad" : "manual" ] ]
			map.descriptionText = "Locked manually"
			break
		case 22: // Manually unlocked
			map = [ name: "lock", value: "unlocked", data: [ method: "manual", notify: true, notificationText: "$deviceName at ${location.name} was unlocked manually" ] ]
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
			map = [ name: "lock", value: "unlocked", data: [ method: "command", notify: true, notificationText: "$deviceName at ${location.name} was unlocked" ] ]
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
			result << createEvent(name: "lockCodesData", value: util.toJson([:]), displayed: false, descriptionText: "'lockCodesData' attribute updated")
			break
		case 33: // User code deleted
			codeID = readCodeSlotId(cmd)
			if (lockCodes[codeID.toString()]) {
				codeName = getCodeName(lockCodes, codeID)
				map = [ name: "codeChanged", value: "$codeID deleted", isStateChange: true ]
				map.descriptionText = "Deleted \"$codeName\""
				map.data = [ codeName: codeName, notify: true, notificationText: "Deleted \"$codeName\" in $deviceName at ${location.name}" ]
				result << codeDeletedEvent(lockCodes, codeID)
				result << clearUserInfo(codeID)
			}
			break
		case 38: // Non Access
			map = [ descriptionText: "A Non Access Code was entered at the lock", isStateChange: true ]
			break
		case 13:
		case 112: // Master or user code changed/set
			codeID = readCodeSlotId(cmd)
			if(codeID == 0 && isKwiksetLock()) {
				//Ignoring this AlarmReport as Kwikset reports codeID 0 when all slots are full and user tries to set another lock code manually
				//Kwikset locks don't send AlarmReport when Master code is set
				log.trace "Ignoring this alarm report in case of Kwikset locks"
				break
			}
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
			codeID = readCodeSlotId(cmd)
			clearStateForSlot(codeID)
			map = [ name: "codeChanged", value: "$codeID failed", descriptionText: "User code is duplicate and not added",
				isStateChange: true, data: [isCodeDuplicate: true] ]
			break
		case 130:  // Batteries replaced
			map = [ descriptionText: "Batteries replaced", isStateChange: true ]
			result += setClock()
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
	// cmd.userIdentifier seems to be an int primitive type
	def codeID = cmd.userIdentifier.toString()
	def lockCodes = loadLockCodes()
	def map = [ name: "codeChanged", isStateChange: true ]
	def deviceName = device.displayName
	def userIdStatus = cmd.userIdStatus
	
	if (userIdStatus == UserCodeReport.USER_ID_STATUS_OCCUPIED ||
				(userIdStatus == UserCodeReport.USER_ID_STATUS_STATUS_NOT_AVAILABLE && cmd.user)) {
				
		def codeName
		
		// Schlage locks sends a blank/empty code during code creation/updation where as it sends "**********" during scanning
		// Some Schlage locks send "**********" during code creation also. The state check will work for them
		if ((!cmd.code || state["setname$codeID"]) && isSchlageLock()) {
			// this will be executed when the user tries to create/update a user code through the
			// smart app or manually on the lock. This is specific to Schlage locks.
			log.trace "[DTH] User code creation successful for Schlage lock"
			codeName = getCodeNameFromState(lockCodes, codeID)
			def changeType = getChangeType(lockCodes, codeID)

			map.value = "$codeID $changeType"
			map.isStateChange = true
			map.descriptionText = "${getStatusForDescription(changeType)} \"$codeName\""
			map.data = [ codeName: codeName, lockName: deviceName, notify: true, notificationText: "${getStatusForDescription(changeType)} \"$codeName\" in $deviceName at ${location.name}" ]
			if(!isMasterCode(codeID)) {
				result << codeSetEvent(lockCodes, codeID, codeName)
			} else {
				map.descriptionText = "${getStatusForDescription('set')} \"$codeName\""
				map.data.notificationText = "${getStatusForDescription('set')} \"$codeName\" in $deviceName at ${location.name}"
				map.data.lockName = deviceName
			}
		} else {
			// We'll land here during scanning of codes
			log.debug "Code scanning in progress for slot number := $codeID"
			codeName = getCodeName(lockCodes, codeID)
			def changeType = getChangeType(lockCodes, codeID)
			if (!lockCodes[codeID]) {
				result << codeSetEvent(lockCodes, codeID, codeName)
			} else {
				map.displayed = false
			}
			map.value = "$codeID $changeType"
			map.descriptionText = "${getStatusForDescription(changeType)} \"$codeName\""
			map.data = [ codeName: codeName, lockName: deviceName ]
			
			if (device.currentValue("numberOfSlotsDailyRepeating") || device.currentValue("numberOfSlotsWeekDay") || device.currentValue("numberOfSlotsYearDay")) {
				// setting user info with unknown schedule type
				def codesToScanSchedules = state.codesToScanSchedules
				if (!codesToScanSchedules || (codesToScanSchedules && codesToScanSchedules.size() > 0 && codeID.toInteger() in codesToScanSchedules)) {
					// set the access type to unknown only when we scan the schedule
					setUserInfo([codeID: "" + codeID, phoneNum: "", accessType: "unknown", lastUsage: new Date().getTime()])
				}
			} else {
				// setting user info with always schedule type
				setUserInfo([codeID: "" + codeID, phoneNum: "", accessType: "always", lastUsage: new Date().getTime()])
			}
		}
	} else if(userIdStatus == 254 && isSchlageLock()) {
		// This is code creation/updation error for Schlage locks.
		// It should be OK to mark this as duplicate pin code error since in case the batteries are down, or lock is not in range,
		// or wireless interference is there, the UserCodeReport will anyway not be received.
		map = [ name: "codeChanged", value: "$codeID failed", descriptionText: "User code is not added", isStateChange: true,
			data: [ lockName: deviceName, isCodeDuplicate: true] ]
	} else {
		// We are using userIdStatus here because codeID = 0 is reported when user tries to set programming code as the user code
		if (codeID == "0" && userIdStatus == UserCodeReport.USER_ID_STATUS_AVAILABLE_NOT_SET && isSchlageLock()) {
			// all codes deleted for Schlage locks
			log.trace "[DTH] All user codes deleted for Schlage lock"
			result << allCodesDeletedEvent()
			map = [ name: "codeChanged", value: "all deleted", descriptionText: "Deleted all user codes", isStateChange: true,
				data: [ lockName: deviceName, notify: true,
					notificationText: "Deleted all user codes in $deviceName at ${location.name}"] ]
			lockCodes = [:]
			result << lockCodesEvent(lockCodes)
			result << createEvent(name: "lockCodesData", value: util.toJson([:]), displayed: false, descriptionText: "'lockCodesData' attribute updated")
		} else {
			// code is not set
			if (lockCodes[codeID]) {
				def codeName = getCodeName(lockCodes, codeID)
				map.value = "$codeID deleted"
				map.descriptionText = "Deleted \"$codeName\""
				map.data = [ codeName: codeName, lockName: deviceName, notify: true, notificationText: "Deleted \"$codeName\" in $deviceName at ${location.name}" ]
				result << codeDeletedEvent(lockCodes, codeID)
				result << clearUserInfo(codeID)
			} else {
				map.value = "$codeID unset"
				map.displayed = false
				map.data = [ lockName: deviceName ]
			}
		}
	}
	
	clearStateForSlot(codeID)
	result << createEvent(map)
	
	if (codeID.toInteger() == state.checkCode) {  // reloadAllCodes() was called, keep requesting the codes in order
		if (state.checkCode + 1 > state.codes || state.checkCode >= MAX_CODES_TO_SCAN) {
			if (device.currentValue("numberOfSlotsDailyRepeating") || device.currentValue("numberOfSlotsWeekDay") || device.currentValue("numberOfSlotsYearDay")) {
				// lock code scanning complete, start schedule scanning
				// running after 2 seconds so that the 'lockCodes' map is populated before starting schedule scanning
				runIn(2, startScanningSchedule)
				//increasing it so that reloadAllCodes does not call startScanningSchedule before scanning of code 8
				state.checkCode = state.checkCode + 1
			} else {
				codeScanCompleteEvent()
			}
		} else {
			// scan next code
			state.checkCode = state.checkCode + 1
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
	if (state.checkCode && state.checkCode <= MAX_CODES_TO_SCAN) {
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
 * Responsible for parsing ScheduleEntryTypeSupportedReport command
 *
 * @param cmd: The ScheduleEntryTypeSupportedReport command to be parsed
 *
 * @return The event(s) to be sent out
 *
 */
def zwaveEvent(physicalgraph.zwave.commands.scheduleentrylockv3.ScheduleEntryTypeSupportedReport cmd) {
	log.trace "[DTH] Executing 'zwaveEvent(physicalgraph.zwave.commands.scheduleentrylockv3.ScheduleEntryTypeSupportedReport cmd)' with cmd = $cmd"
	def result = []
	def numberOfSlotsDailyRepeating = cmd.numberOfSlotsDailyRepeating
	def numberOfSlotsWeekDay = cmd.numberOfSlotsWeekDay
	def numberOfSlotsYearDay = cmd.numberOfSlotsYearDay
	if (!numberOfSlotsDailyRepeating) {
		numberOfSlotsDailyRepeating = 0
	}
	if (!numberOfSlotsWeekDay) {
		numberOfSlotsWeekDay = 0
	}
	if (!numberOfSlotsYearDay) {
		numberOfSlotsYearDay = 0
	}
	result << createEvent(name: "numberOfSlotsDailyRepeating", value: numberOfSlotsDailyRepeating, displayed: false, descriptionText: "No. of daily repeating slots supported is ${numberOfSlotsDailyRepeating}")
	result << createEvent(name: "numberOfSlotsWeekDay", value: numberOfSlotsWeekDay, displayed: false, descriptionText: "No. of week day slots supported is ${numberOfSlotsWeekDay}")
	result << createEvent(name: "numberOfSlotsYearDay", value: numberOfSlotsYearDay, displayed: false, descriptionText: "No. of year day slots supported is ${numberOfSlotsYearDay}")
	return result
}

/**
 * Responsible for parsing ScheduleEntryLockDailyRepeatingReport command
 *
 * @param cmd: The ScheduleEntryLockDailyRepeatingReport command to be parsed
 *
 * @return The event(s) to be sent out
 *
 */
def zwaveEvent(physicalgraph.zwave.commands.scheduleentrylockv3.ScheduleEntryLockDailyRepeatingReport cmd) {
	log.trace "[DTH] Executing 'zwaveEvent(physicalgraph.zwave.commands.scheduleentrylockv3.ScheduleEntryLockDailyRepeatingReport cmd)' with cmd = $cmd"
	if (device.currentValue("numberOfSlotsDailyRepeating")) {
		
		def result = []
		def codeID = cmd.userIdentifier
		def lockCodes = loadLockCodes()
		def codeName = getCodeName(lockCodes, codeID)

		if (cmd.durationHour == 255 && cmd.durationMinute == 255 && cmd.startHour == 255 && cmd.startMinute == 255 && cmd.weekDayBitmask == 255) {
			def scheduleType = getScheduleType(codeID)
			if (isScheduleScanningInProgress(codeID) || scheduleType == "always" || scheduleType == "unknown") {
				log.trace "[DTH] ScheduleEntryLockDailyRepeatingReport() -  Schedule found empty while scanning for codeID := $codeID"
				result << createEvent(name: "scheduleChanged", value: "$codeID $SCHEDULE_TYPE_DAILY_REPEATING unset", isStateChange: false, displayed: false)
				// For Kwikset lock, we know the type of schedule type through confugurationGet() and hence an empty schedule case will never be encountered
				// For other locks, the schedule type is not known and hence check for year day and week day schedule now
				if (device.currentValue("numberOfSlotsYearDay")) {
					log.trace "[DTH] ScheduleEntryLockDailyRepeatingReport() -  Requesting year day schedule for codeID := $codeID"
					result << response(requestYearDaySchedule(codeID))
				} else if (device.currentValue("numberOfSlotsWeekDay")) {
					log.trace "[DTH] ScheduleEntryLockDailyRepeatingReport() -  Requesting week day schedule for codeID := $codeID"
					clearWeekdayScanningDataFromState(codeID)
					result << response(requestAllWeekDaySchedule(codeID))
				} else {
					// schedule scanning for this codeID is complete
					setUserInfo([codeID: "" + codeID, phoneNum: "", accessType: "always", lastUsage: new Date().getTime()])
					log.trace "[DTH] ScheduleEntryLockDailyRepeatingReport() -  Schedule scanning complete for codeID := $codeID"
					checkAndScanNextSchedule(codeID)
				}
			} else {
				log.trace "[DTH] ScheduleEntryLockDailyRepeatingReport() -  Schedule deleted successfully for codeID := $codeID"
				result << scheduleDeleteEvent(codeID)
				def deviceName = device.displayName
				def map = [name: "scheduleChanged", value: "$codeID $SCHEDULE_TYPE_DAILY_REPEATING deleted", descriptionText: "Cleared recurring schedule for \"$codeName\"", isStateChange: true, displayed: true]
				map.data = [lockName: deviceName, notify: true, notificationText: "Cleared recurring schedule for \"$codeName\" in $deviceName at ${location.name}"]
				result << createEvent(map)
			}
		} else {
			def savedStartTime = state["setDailyRepeatingStartTime$codeID"]
			def savedEndTime = state["setDailyRepeatingEndTime$codeID"]
			def savedWeekDayBitmask = state["setDailyRepeatingBitMask$codeID"]
			
			def changeType = getScheduleChangeType(codeID, SCHEDULE_TYPE_DAILY_REPEATING)
			
			if (savedStartTime && savedEndTime && savedWeekDayBitmask) {
				def startTime = getWeekDayHoursAndMinutes(savedStartTime)
				def endTime = getWeekDayHoursAndMinutes(savedEndTime)
				
				def startHour = startTime.hour
				def startMin = startTime.minute
				def endHour = endTime.hour
				def endMin = endTime.minute
		
				def durMap = getDurationInHoursAndMinutes(startHour, startMin, endHour, endMin)
				def durMin = durMap.durMin
				def durHour = durMap.durHour
				
				if (cmd.durationHour == durHour && cmd.durationMinute == durMin && cmd.startHour == startHour && cmd.startMinute == startMin && cmd.weekDayBitmask == savedWeekDayBitmask) {
					log.trace "[DTH] ScheduleEntryLockDailyRepeatingReport() -  Schedule set successfully for codeID := $codeID"
					result << scheduleSetEvent(codeID, SCHEDULE_TYPE_DAILY_REPEATING)
					def deviceName = device.displayName
					def map = [name: "scheduleChanged", value: "$codeID $SCHEDULE_TYPE_DAILY_REPEATING $changeType", descriptionText: "${getStatusForDescription(changeType)} recurring schedule for \"$codeName\"", isStateChange: true, displayed: true]
					map.data = [lockName: deviceName, notify: true, notificationText: "${getStatusForDescription(changeType)} \"$codeName\" with recurring schedule in $deviceName at ${location.name}"]
					result << createEvent(map)
				} else {
					log.trace "[DTH] ScheduleEntryLockDailyRepeatingReport() -  Schedule set failed for codeID := $codeID"
					result << createEvent(name: "scheduleChanged", value: "$codeID $SCHEDULE_TYPE_DAILY_REPEATING failed", descriptionText: "Recurring schedule set failed for \"$codeName\"", isStateChange: true, displayed: false)
				}
			} else {
				log.trace "[DTH] ScheduleEntryLockDailyRepeatingReport() -  Schedule found for codeID := $codeID while scanning"
				def endHour = cmd.startHour + cmd.durationHour
				def endMin = cmd.startMinute + cmd.durationMinute
				if (endMin >= 60) {
					endMin = endMin % 60
					endHour = endHour + 1
				}
				
				state["setDailyRepeatingStartTime$codeID"] = sprintf("%02d:%02d", cmd.startHour, cmd.startMinute)
				state["setDailyRepeatingEndTime$codeID"] = sprintf("%02d:%02d", endHour, endMin)
				state["setDailyRepeatingBitMask$codeID"] = cmd.weekDayBitmask
				result << scheduleSetEvent(codeID, SCHEDULE_TYPE_DAILY_REPEATING)
				def map = [name: "scheduleChanged", value: "$codeID $SCHEDULE_TYPE_DAILY_REPEATING $changeType", descriptionText: "${getStatusForDescription(changeType)} recurring schedule for \"$codeName\"", isStateChange: true, displayed: true]
				if (changeType == "changed") {
					map.displayed = false
				}
				result << createEvent(map)
				
				// schedule scanning for this codeID is complete
				log.trace "[DTH] ScheduleEntryLockDailyRepeatingReport() -  Schedule scanning complete for codeID := $codeID"
				checkAndScanNextSchedule(codeID)
			}
			clearDailyRepeatingScheduleDataFromState(codeID)
		}

		if (result) {
			result = result.flatten()
			return result
		}
		return null
	}
	return null
}

/**
 * Responsible for parsing ScheduleEntryLockWeekDayReport command
 *
 * @param cmd: The ScheduleEntryLockWeekDayReport command to be parsed
 *
 * @return The event(s) to be sent out
 *
 */
def zwaveEvent(physicalgraph.zwave.commands.scheduleentrylockv3.ScheduleEntryLockWeekDayReport cmd) {
	log.trace "[DTH] Executing 'zwaveEvent(physicalgraph.zwave.commands.scheduleentrylockv3.ScheduleEntryLockWeekDayReport cmd)' with cmd = $cmd"
	if (device.currentValue("numberOfSlotsWeekDay")) {
		def result = []
		def codeID = cmd.userIdentifier
		def lockCodes = loadLockCodes()
		def codeName = getCodeName(lockCodes, codeID)
		
		def daysOfWeek = state["setWeekDayWeekDays$codeID"]
		if (daysOfWeek && daysOfWeek.size() > 0) {
			int dayOfWeek = cmd.dayOfWeek
			if (dayOfWeek in daysOfWeek) {
				def startTime = getWeekDayHoursAndMinutes(state["setWeekDayStartTime$codeID"])
				def startHour = startTime.hour
				def startMin = startTime.minute
				
				def endTime = getWeekDayHoursAndMinutes(state["setWeekDayEndTime$codeID"])
				def endHour = endTime.hour
				def endMin = endTime.minute
				
				if (cmd.startHour == startHour && cmd.startMinute == startMin && cmd.stopHour == endHour && cmd.stopMinute == endMin) {
					daysOfWeek.removeAll { it == dayOfWeek }
					state["setWeekDayWeekDays$codeID"] = daysOfWeek
					if (daysOfWeek.size() == 0) {
						log.trace "[DTH] ScheduleEntryLockWeekDayReport() -  Schedule set successfully for codeID := $codeID"
						def changeType = getScheduleChangeType(codeID, SCHEDULE_TYPE_WEEK_DAY)
						result << scheduleSetEvent(codeID, SCHEDULE_TYPE_WEEK_DAY)
						def deviceName = device.displayName
						def map = [name: "scheduleChanged", value: "$codeID $SCHEDULE_TYPE_WEEK_DAY set", descriptionText: "${getStatusForDescription(changeType)} recurring schedule for \"$codeName\"", isStateChange: true, displayed: true]
						map.data = [lockName: deviceName, notify: true, notificationText: "${getStatusForDescription(changeType)} \"$codeName\" with recurring schedule in $deviceName at ${location.name}"]
						result << createEvent(map)
						clearWeekdayScheduleDataFromState(codeID)
					}
				} else {
					log.trace "[DTH] ScheduleEntryLockWeekDayReport() -  Schedule set failed for codeID := $codeID"
					result << createEvent(name: "scheduleChanged", value: "$codeID $SCHEDULE_TYPE_WEEK_DAY failed", descriptionText: "Recurring schedule set failed for \"$codeName\"", isStateChange: true, displayed: false)
					clearWeekdayScheduleDataFromState(codeID)
				}
			}
		} else {
			def scheduleSlotId = cmd.scheduleSlotId
			scheduleSlotId = scheduleSlotId - 1
			def weekDayScanCount = state["weekDayScanCount$codeID"] ?: 0
			if (!isBitSet(weekDayScanCount, scheduleSlotId)) {
				// setting bit to on
				weekDayScanCount = weekDayScanCount | (1 << scheduleSlotId)
			}
			state["weekDayScanCount$codeID"] = weekDayScanCount
			def weekDayBitMask = state["setWeekDayScanBitMask$codeID"] ?: 0
			int dayOfWeek = cmd.dayOfWeek
			if (dayOfWeek != 255) {
				if (!isBitSet(weekDayBitMask, dayOfWeek)) {
					// setting bit to on
					weekDayBitMask = weekDayBitMask | (1 << dayOfWeek)
				}
				state["setWeekDayScanBitMask$codeID"] = weekDayBitMask
				state["setWeekDayStartTime$codeID"] = sprintf("%02d:%02d", cmd.startHour, cmd.startMinute)
				state["setWeekDayEndTime$codeID"] = sprintf("%02d:%02d", cmd.stopHour, cmd.stopMinute)
			}
			if (weekDayScanCount == 127 && weekDayBitMask != 0) {
				// schedule scanning for this codeID is complete
				log.trace "[DTH] ScheduleEntryLockWeekDayReport() -  Schedule found while scanning for codeID := $codeID"
				def changeType = getScheduleChangeType(codeID, SCHEDULE_TYPE_WEEK_DAY)
				clearWeekdayScanningDataFromState(codeID)
				state["setWeekDayBitMask$codeID"] = weekDayBitMask
				result << scheduleSetEvent(codeID, SCHEDULE_TYPE_WEEK_DAY)
				def map = [name: "scheduleChanged", value: "$codeID $SCHEDULE_TYPE_WEEK_DAY $changeType", descriptionText: "${getStatusForDescription(changeType)} recurring schedule for \"$codeName\"", isStateChange: true, displayed: true]
				if (changeType == "changed") {
					map.displayed = false
				}
				result << createEvent(map)
				clearWeekdayScheduleDataFromState(codeID)
				checkAndScanNextSchedule(codeID)
			} else if (weekDayScanCount == 127 && weekDayBitMask == 0) {
				def scheduleType = getScheduleType(codeID)
				if (isScheduleScanningInProgress(codeID) || scheduleType == "always" || scheduleType == "unknown") {
					log.trace "[DTH] ScheduleEntryLockWeekDayReport() -  Schedule found empty while scanning for codeID := $codeID"
					// schedule scanning for this codeID is complete
					setUserInfo([codeID: "" + codeID, phoneNum: "", accessType: "always", lastUsage: new Date().getTime()])
					log.trace "[DTH] ScheduleEntryLockWeekDayReport() -  Schedule scanning complete for codeID := $codeID"
					clearWeekdayScanningDataFromState(codeID)
					checkAndScanNextSchedule(codeID)
				} else {
					log.trace "[DTH] ScheduleEntryLockWeekDayReport() -  Schedule deleted successfully for codeID := $codeID"
					result << scheduleDeleteEvent(codeID)
					def deviceName = device.displayName
					def map = [name: "scheduleChanged", value: "$codeID $SCHEDULE_TYPE_WEEK_DAY deleted", descriptionText: "Cleared recurring schedule for \"$codeName\"", isStateChange: true, displayed: true]
					map.data = [lockName: deviceName, notify: true, notificationText: "Cleared recurring schedule for \"$codeName\" in $deviceName at ${location.name}"]
					result << createEvent(map)
				}
			}
		}
		if (result) {
			result = result.flatten()
			return result
		}
		return null
	}
	return null
}

/**
 * Responsible for parsing ScheduleEntryLockYearDayReport command
 *
 * @param cmd: The ScheduleEntryLockYearDayReport command to be parsed
 *
 * @return The event(s) to be sent out
 *
 */
def zwaveEvent(physicalgraph.zwave.commands.scheduleentrylockv3.ScheduleEntryLockYearDayReport cmd) {
	log.trace "[DTH] Executing 'zwaveEvent(physicalgraph.zwave.commands.scheduleentrylockv3.ScheduleEntryLockYearDayReport cmd)' with cmd = $cmd"
	if (device.currentValue("numberOfSlotsYearDay")) {
		def result = []
		def codeID = cmd.userIdentifier
		def lockCodes = loadLockCodes()
		def codeName = getCodeName(lockCodes, codeID)
		
		if (cmd.startYear == 255 && cmd.startMonth == 255 && cmd.startDay == 255 && cmd.startHour == 255 && cmd.startMinute == 255 &&
				cmd.stopYear == 255 && cmd.stopMonth == 255 && cmd.stopDay == 255 && cmd.stopHour == 255 && cmd.stopMinute == 255) {
			def scheduleType = getScheduleType(codeID)
			if (isScheduleScanningInProgress(codeID) || scheduleType == "always" || scheduleType == "unknown") {
				log.trace "[DTH] ScheduleEntryLockYearDayReport() -  Schedule found empty while scanning for codeID := $codeID"
				result << createEvent(name: "scheduleChanged", value: "$codeID $SCHEDULE_TYPE_YEAR_DAY unset", isStateChange: false, displayed: false)
				
				// For Kwikset lock, we know the type of schedule type through confugurationGet() and hence an empty schedule case will never be encountered
				// For other locks, the schedule type is not known and hence check for week day schedule now
				if (device.currentValue("numberOfSlotsWeekDay")) {
					log.trace "[DTH] ScheduleEntryLockYearDayReport() -  Requesting week day schedule for codeID := $codeID"
					clearWeekdayScanningDataFromState(codeID)
					result << response(requestAllWeekDaySchedule(codeID))
				} else {
					// schedule scanning for this codeID is complete
					setUserInfo([codeID: "" + codeID, phoneNum: "", accessType: "always", lastUsage: new Date().getTime()])
					log.trace "[DTH] ScheduleEntryLockYearDayReport() -  Schedule scanning complete for codeID := $codeID"
					checkAndScanNextSchedule(codeID)
				}
			} else {
				log.trace "[DTH] ScheduleEntryLockYearDayReport() -  Schedule deleted successfully for codeID := $codeID"
				result << scheduleDeleteEvent(codeID)
				def deviceName = device.displayName
				def map = [name: "scheduleChanged", value: "$codeID $SCHEDULE_TYPE_YEAR_DAY deleted", descriptionText: "Cleared limited schedule for \"$codeName\"", isStateChange: true, displayed: true]
				map.data = [lockName: deviceName, notify: true, notificationText: "Cleared limited schedule for \"$codeName\" in $deviceName at ${location.name}"]
				result << createEvent(map)
			}
		} else {
			def startDate = state["setYearDayStartDate$codeID"]
			def startTime = state["setYearDayStartTime$codeID"]
			def endDate = state["setYearDayStopDate$codeID"]
			def endTime = state["setYearDayStopTime$codeID"]
			
			def changeType = getScheduleChangeType(codeID, SCHEDULE_TYPE_YEAR_DAY)
			
			if (startDate && startTime && endDate && endTime) {
				def startDateTime = getYearDayTimeDetails(startDate, startTime)
				def startHour = startDateTime.hour
				def startMinute = startDateTime.minute
				def startDay = startDateTime.day
				def startMonth = startDateTime.month
				def startYear = startDateTime.year.toString()
				startYear = startYear.substring(2, startYear.length())
				startYear = Integer.parseInt(startYear, 10)
	
				def endDateTime = getYearDayTimeDetails(endDate, endTime)
				def stopHour = endDateTime.hour
				def stopMinute = endDateTime.minute
				def stopDay = endDateTime.day
				def stopMonth = endDateTime.month
				def stopYear = endDateTime.year.toString()
				stopYear = stopYear.substring(2, stopYear.length())
				stopYear = Integer.parseInt(stopYear, 10)
	
				if (cmd.startYear == startYear && cmd.startMonth == startMonth && cmd.startDay == startDay && cmd.startHour == startHour && cmd.startMinute == startMinute &&
						cmd.stopYear == stopYear && cmd.stopMonth == stopMonth && cmd.stopDay == stopDay && cmd.stopHour == stopHour && cmd.stopMinute == stopMinute) {
					log.trace "[DTH] ScheduleEntryLockYearDayReport() -  Schedule set successfully for codeID := $codeID"
					result << scheduleSetEvent(codeID, SCHEDULE_TYPE_YEAR_DAY)
					def deviceName = device.displayName
					def map = [name: "scheduleChanged", value: "$codeID $SCHEDULE_TYPE_YEAR_DAY $changeType", descriptionText: "${getStatusForDescription(changeType)} limited schedule for \"$codeName\"", isStateChange: true, displayed: true]
					map.data = [lockName: deviceName, notify: true, notificationText: "${getStatusForDescription(changeType)} \"$codeName\" with limited schedule in $deviceName at ${location.name}"]
					result << createEvent(map)
				} else {
					log.trace "[DTH] ScheduleEntryLockYearDayReport() -  Schedule set failed for codeID := $codeID"
					result << createEvent(name: "scheduleChanged", value: "$codeID $SCHEDULE_TYPE_YEAR_DAY failed", descriptionText: "Limited schedule set failed for \"$codeName\"", isStateChange: true, displayed: false)
				}
			} else {
				log.trace "[DTH] ScheduleEntryLockYearDayReport() -  Schedule found while scanning for codeID := $codeID"
				state["setYearDayStartDate$codeID"] = sprintf("20%02d-%02d-%02d", cmd.startYear, cmd.startMonth, cmd.startDay)
				state["setYearDayStartTime$codeID"] = sprintf("%02d:%02d", cmd.startHour, cmd.startMinute)
				state["setYearDayStopDate$codeID"] = sprintf("20%02d-%02d-%02d", cmd.stopYear, cmd.stopMonth, cmd.stopDay)
				state["setYearDayStopTime$codeID"] = sprintf("%02d:%02d", cmd.stopHour, cmd.stopMinute)
				result << scheduleSetEvent(codeID, SCHEDULE_TYPE_YEAR_DAY)
				def map = [name: "scheduleChanged", value: "$codeID $SCHEDULE_TYPE_YEAR_DAY $changeType", descriptionText: "${getStatusForDescription(changeType)} limited schedule for \"$codeName\"", isStateChange: true, displayed: true]
				if (changeType == "changed") {
					map.displayed = false
				}
				result << createEvent(map)
				
				// schedule scanning for this codeID is complete
				log.trace "[DTH] ScheduleEntryLockYearDayReport() -  Schedule scanning complete for codeID := $codeID"
				checkAndScanNextSchedule(codeID)
			}
			clearYeardayScheduleDataFromState(codeID)
		}

		if (result) {
			result = result.flatten()
			return result
		}
		return null
	}
	return null
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
	def cmds = []
	cmds << setClock()
	cmds << secure(zwave.doorLockV1.doorLockOperationGet())
	cmds = delayBetween(cmds, 200)
	cmds
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
	state.checkCode = state.checkCode ?: 1

	def cmds = []
	// Not calling validateAttributes() here because userNumberGet command will be added twice
	if(!device.currentValue("codeLength") && isSchlageLock()) {
		cmds << secure(zwave.configurationV2.configurationGet(parameterNumber: getSchlageLockParam().codeLength.number))
	}
	if (device.currentValue("numberOfSlotsDailyRepeating") == null && SCHEDULE_ENTRY_LOCK_CLASS_ID in zwaveInfo.sec) {
		cmds << secure(zwave.scheduleEntryLockV3.scheduleEntryTypeSupportedGet())
	}
	if (!state.codes) {
		// BUG: There might be a bug where Schlage does not return the below number of codes
		cmds << secure(zwave.userCodeV1.usersNumberGet())
	} else {
		sendEvent(name: "maxCodes", value: state.codes, displayed: false)
		if (state.checkCode + 1 > state.codes || state.checkCode > MAX_CODES_TO_SCAN) {
			// code scanning is complete but not schedule scanning
			startScanningSchedule()
		} else {
			// starting code scanning
			cmds << requestCode(state.checkCode)
		}
	}
	if(cmds.size() > 1) {
		cmds = delayBetween(cmds, 4200)
	}
	cmds
}

/**
 * API endpoint for setting the user code length on a lock. This is specific to Schlage locks.
 *
 * @param length: The user code length
 *
 * @returns The command fired for writing the code length attribute
 */
def setCodeLength(length) {
	if (isSchlageLock()) {
		length = length.toInteger()
		if (length >= 4 && length <= 8) {
			log.trace "[DTH] Executing 'setCodeLength()' by ${device.displayName}"
			def val = []
			val << length
			def param = getSchlageLockParam()
			return secure(zwave.configurationV2.configurationSet(parameterNumber: param.codeLength.number, size: param.codeLength.size, configurationValue: val))
		}
	}
	return null
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
	if(!device.currentValue("codeLength") && isSchlageLock()) {
		cmds << secure(zwave.configurationV2.configurationGet(parameterNumber: getSchlageLockParam().codeLength.number))
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
				set_cmds << secure(zwave.userCodeV1.userCodeSet(userIdentifier:n, userIdStatus:1, user:updated))
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
	def deviceName = device.displayName
	log.trace "[DTH] - Executing nameSlot() for device $deviceName"
	def lockCodes = loadLockCodes()
	def oldCodeName = getCodeName(lockCodes, codeSlot)
	def newCodeName = codeName ?: "Code $codeSlot"
	lockCodes[codeSlot] = newCodeName
	sendEvent(lockCodesEvent(lockCodes))
	sendEvent(name: "codeChanged", value: "$codeSlot renamed", data: [ lockName: deviceName, notify: false, notificationText: "Renamed \"$oldCodeName\" to \"$newCodeName\" in $deviceName at ${location.name}" ],
		descriptionText: "Renamed \"$oldCodeName\" to \"$newCodeName\"", displayed: true, isStateChange: true)
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
	// Calling user code get when deleting a code because some Kwikset locks do not generate
	// AlarmReport when a code is deleted manually on the lock
	secureSequence([
		zwave.userCodeV1.userCodeSet(userIdentifier:codeID, userIdStatus:0),
		zwave.userCodeV1.userCodeGet(userIdentifier:codeID)
	], 4200)
}

/**
 * API end-point for updating the 'lockCodesData' attribute with the user info
 *
 * @param userInfo: Map consisting values for code id, phone number, access type, and last usage
 */
def setUserInfo(userInfo) {
	log.trace "[DTH] Executing 'setUserInfo()' by ${device.displayName} with userInfo := ${userInfo}"
	def codeID = userInfo.codeID
	if (codeID) {
		codeID = codeID.toString()
		def lockCodesData = loadlockCodesData(codeID)
		lockCodesData[codeID].lastUsage = userInfo.lastUsage
		lockCodesData[codeID].userInfo.phoneNum = userInfo.phoneNum ?: ""
		def accessType = userInfo.accessType
		if (accessType) {
			if (accessType == "always") {
				lockCodesData[codeID].scheduleInfo = [:]
				lockCodesData[codeID].scheduleInfo.scheduleData = [:]
			}
			lockCodesData[codeID].scheduleInfo.scheduleType = accessType
		}
		sendEvent(lockCodesDataEvent(lockCodesData))
	}
}

/**
 * Starts the scanning of schedule for the first code present in the 'lockCodes' map
 */
def startScanningSchedule() {
	log.trace "[DTH] Executing 'startScanningSchedule()' by ${device.displayName}"
	def codesToScanSchedules = state.codesToScanSchedules
	def codeID = -1
	if (codesToScanSchedules && codesToScanSchedules.size() > 0) {
		// Schedule scanning did not complete the last time. Scan the remaining schedules.
		codeID = codesToScanSchedules[0]
	} else {
		def lockCodes = loadLockCodes()
		if (lockCodes && lockCodes.size() > 0) {
			def keySet = lockCodes.keySet()
			codesToScanSchedules = []
			// Saving the list of lock codes for which the schedule scanning should be executed
			// In case the user creates schedule while the scanning is in progress, those codes need not be scanned
			for (def i = 1; i <= MAX_CODES_TO_SCAN; i++) {
				if (i.toString() in keySet) {
					codesToScanSchedules << i
				}
			}
			state.codesToScanSchedules = codesToScanSchedules
			codeID = codesToScanSchedules[0]
		}
	}
	log.trace "[DTH] startScanningSchedule() -  Schedule to scan := ${state.codesToScanSchedules}"
	if (codeID != -1) {
		scanSchedule(codeID)
	} else {
		// no schedule to scan - scheduling scanning complete
		codeScanCompleteEvent()
	}
}

/**
 * Scans the schedule for the specified code slot number
 * 
 * @param codeID: The code slot number
 */
private def scanSchedule(codeID) {
	if (isKwiksetLock()) {
		// For Kwikset locks we can find out the user type and then issue the appropriate schedule get command
		log.trace "[DTH] scanSchedule() -  Calling configurationGet() for codeID := $codeID"
		sendHubCommand(new physicalgraph.device.HubAction(secure(zwave.configurationV2.configurationGet(parameterNumber: codeID))))
	} else {
		// only one of the supported schedule type should be requested from here
		// executing get schedule for daily repeating first as Yale locks only support daily repeating
		// executing get schedule for week day in the end as we have to execute 7 commands
		if (device.currentValue("numberOfSlotsDailyRepeating")) {
			log.trace "[DTH] scanSchedule() -  Requesting daily repeating schedule for codeID := $codeID"
			sendHubCommand(new physicalgraph.device.HubAction(requestDailyRepeatingSchedule(codeID)))
		} else if (device.currentValue("numberOfSlotsYearDay")) {
			log.trace "[DTH] scanSchedule() -  Requesting year day schedule for codeID := $codeID"
			sendHubCommand(new physicalgraph.device.HubAction(requestYearDaySchedule(codeID)))
		} else if (device.currentValue("numberOfSlotsWeekDay")) {
			log.trace "[DTH] scanSchedule() -  Requesting week day schedule for codeID := $codeID"
			sendHubCommand(new physicalgraph.device.HubAction(requestAllWeekDaySchedule(codeID)))
		} else {
			setUserInfo([codeID: "" + codeID, phoneNum: "", accessType: "always", lastUsage: new Date().getTime()])
		}
	}
}

/**
 * Initiates schedule scanning if there are pending user codes, else sends the scan complete event
 *
 * @param codeID: The code slot number
 */
private def checkAndScanNextSchedule(codeID) {
	removeScannedScheduleCode(codeID)
	if (hasSchedulesToScan()) {
		// more schedules to scan
		def codesToScanSchedules = state.codesToScanSchedules
		scanSchedule(codesToScanSchedules[0])
	} else {
		// scheduling scanning complete
		codeScanCompleteEvent()
	}
}

/**
 * Removes the scanned code from the state
 * 
 * @param codeID: The code slot number
 */
private def removeScannedScheduleCode(codeID) {
	def codesToScanSchedules = state.codesToScanSchedules
	codesToScanSchedules.removeAll { it == codeID }
	log.trace "[DTH] removeScannedScheduleCode() -  Remaining schedule to scan := $codesToScanSchedules"
	state.codesToScanSchedules = codesToScanSchedules
}

/**
 * Checks if there are more schedules to be scanned
 *
 * @returns true if more schedules are to be scanned, else false
 */
private def hasSchedulesToScan() {
	return state.codesToScanSchedules?.size() > 0
}

/**
 * API end-point for setting a schedule on a lock
 *
 * @param schSettings: The map with schedule info
 *
 * @returns cmds: The commands fired for setting a schedule on a lock
 */
def setSchedule(schSettings) {
	log.trace "[DTH] Executing 'setSchedule()' by ${device.displayName} with schSettings := $schSettings"
	def cmds = []
	cmds += setClock()
	
	def scheduleType = schSettings.scheduleType
	if (scheduleType == SCHEDULE_TYPE_DAILY_REPEATING) {
		cmds << getDailyRepeatingSetScheduleCmd(schSettings)
	} else if (scheduleType == SCHEDULE_TYPE_WEEK_DAY) {
		cmds << getWeekDaySetScheduleCmd(schSettings)
	} else if (scheduleType == SCHEDULE_TYPE_YEAR_DAY) {
		cmds << getYearDaySetScheduleCmd(schSettings)
	}
	cmds = cmds.flatten()
	cmds
}

/**
 * Clears the state and sends event for code scanning complete 
 */
def codeScanCompleteEvent() {
	state["checkCode"] = null
	state.remove("checkCode")
	sendEvent(name: "scanCodes", value: "Complete", descriptionText: "Code scan completed", displayed: false)
}

/**
 * API end-point for deleting a schedule on a lock
 *
 * @param schSettings: The map with schedule info
 *
 * @returns cmds: The commands fired for deleting a schedule on a lock
 */
def deleteSchedule(schSettings) {
	log.trace "[DTH] Executing 'deleteSchedule()' by ${device.displayName} with schSettings := $schSettings"
	def cmds = []
	
	def codeID = Integer.parseInt(schSettings.codeId, 10)
	if (schSettings.clearSchedule) {
		cmds << clearOldSchedule(codeID)
		cmds = cmds.flatten()
		return cmds
	}
	
	def scheduleType = schSettings.scheduleType
	if (isKwiksetLock()) {
		cmds << changeUserTypeToOwner(codeID)
	} else {
		if (scheduleType == SCHEDULE_TYPE_DAILY_REPEATING) {
			cmds << getDailyRepeatingDeleteScheduleCmd(codeID)
		} else if (scheduleType == SCHEDULE_TYPE_WEEK_DAY) {
			cmds << getWeekDayDeleteScheduleCmd(codeID)
		} else if (scheduleType == SCHEDULE_TYPE_YEAR_DAY) {
			cmds << getYearDayDeleteScheduleCmd(codeID)
		}
	}
	
	if (cmds) {
		cmds = cmds.flatten()
		return cmds
	}
	return null
}

/**
 * Clears a schedule on a lock
 *
 * @param codeID: The code slot number
 *
 * @returns cmds: The commands fired for clearing a schedule on a lock
 */
private def clearOldSchedule(codeID) {
	log.trace "[DTH] Executing 'clearOldSchedule()' by ${device.displayName} with codeID := $codeID"
	def cmds = []
	
	if (codeID instanceof String) {
		codeID = Integer.parseInt(codeID, 10)
	}
	
	if (isKwiksetLock()) {
		cmds << secure(zwave.configurationV2.configurationSet(parameterNumber: codeID, configurationValue: [1]))
	} else {
		if (device.currentValue("numberOfSlotsDailyRepeating")) {
			cmds << secure(zwave.scheduleEntryLockV3.scheduleEntryLockDailyRepeatingSet(scheduleSlotId: 1, setAction: 0, userIdentifier: codeID))
		}
		if (device.currentValue("numberOfSlotsWeekDay")) {
			for (def i = 0; i <= 6; i++) {
				cmds << secure(zwave.scheduleEntryLockV3.scheduleEntryLockWeekDaySet(setAction: 0, scheduleSlotId: (i + 1), userIdentifier: codeID))
			}
		}
		if (device.currentValue("numberOfSlotsYearDay")) {
			cmds << secure(zwave.scheduleEntryLockV3.scheduleEntryLockYearDaySet(scheduleSlotId: 1, setAction: 0, userIdentifier: codeID))
		}
	}
	
	if (cmds) {
		cmds = cmds.flatten()
		log.trace "[DTH] 'clearOldSchedule()' returning with cmds := $cmds"
		return cmds
	}
	return null
}

/**
 * API end-point for changing the user type of a code slot to type owner. This is applicable only for Kwikset locks.
 *
 * @param codeID: The code slot number
 *
 * @returns cmds: The commands fired for changing the user type
 */
def changeUserTypeToOwner(codeID) {
	if (isKwiksetLock()) {
		if (codeID != null) {
			def cmds = []
			cmds << secure(zwave.configurationV2.configurationSet(parameterNumber: codeID, configurationValue: [1]))
			cmds << secure(zwave.configurationV2.configurationGet(parameterNumber: codeID))
			cmds = delayBetween(cmds, 2000)
			return cmds
		}
		return null
	}
	return null
}

/**
 * Returns the command(s) for setting daily repeating schedule
 *
 * @return The command(s) for setting daily repeating schedule
 */
private def getDailyRepeatingSetScheduleCmd(schSettings) {
	if (device.currentValue("numberOfSlotsDailyRepeating")) {
		log.trace "[DTH] Executing 'getDailyRepeatingSetScheduleCmd()' by ${device.displayName} with schSettings := $schSettings"
		def cmds = []
		
		def codeID = Integer.parseInt(schSettings.codeId, 10)
		
		def startTime = getWeekDayHoursAndMinutes(schSettings.startTime)
		def startHour = startTime.hour
		def startMin = startTime.minute
		
		def endTime = getWeekDayHoursAndMinutes(schSettings.endTime)
		def endHour = endTime.hour
		def endMin = endTime.minute
		
		def durMap = getDurationInHoursAndMinutes(startHour, startMin, endHour, endMin)
		def durMin = durMap.durMin
		def durHour = durMap.durHour
		
		// For weekDayBitMask, the order of days is Sat, Fri, Thu, Wed, Tue, Mon, Sun with Sat as the most significant bit
		short weekDayBitMask = schSettings.weekDayBitMask
		
		state["setDailyRepeatingStartTime$codeID"] = schSettings.startTime
		state["setDailyRepeatingEndTime$codeID"] = schSettings.endTime
		state["setDailyRepeatingBitMask$codeID"] = weekDayBitMask
		
		cmds << secure(zwave.scheduleEntryLockV3.scheduleEntryLockDailyRepeatingSet(durationHour: durHour, durationMinute: durMin, scheduleSlotId: 1, setAction: 1,
			startHour: startHour, startMinute: startMin, userIdentifier: codeID, weekDayBitmask: weekDayBitMask))
		cmds << requestDailyRepeatingSchedule(codeID)
		cmds = delayBetween(cmds, 200)

		log.debug "[DTH] 'getDailyRepeatingSetScheduleCmd()' returning with commands := $cmds"
		return cmds
	}
	return null
}

/**
 * Returns the command(s) for setting week day schedule
 *
 * @return The command(s) for setting week day schedule
 */
private def getWeekDaySetScheduleCmd(schSettings) {
	if (device.currentValue("numberOfSlotsWeekDay")) {
		log.trace "[DTH] Executing 'getWeekDaySetScheduleCmd()' by ${device.displayName} with schSettings := $schSettings"
		def cmds = []
		
		def codeID = Integer.parseInt(schSettings.codeId, 10)
		
		def startTime = getWeekDayHoursAndMinutes(schSettings.startTime)
		def startHour = startTime.hour
		def startMin = startTime.minute
		
		def endTime = getWeekDayHoursAndMinutes(schSettings.endTime)
		def endHour = endTime.hour
		def endMin = endTime.minute
		
		// For weekDayBitMask, the order of days is Sat, Fri, Thu, Wed, Tue, Mon, Sun with Sat as the most significant bit
		short weekDayBitMask = schSettings.weekDayBitMask
		
		state["setWeekDayStartTime$codeID"] = schSettings.startTime
		state["setWeekDayEndTime$codeID"] = schSettings.endTime
		state["setWeekDayBitMask$codeID"] = weekDayBitMask
		
		def setCmds = []
		def getCmds = []
		def daysOfWeek = []
		// dayOfWeek - Sunday = 0, Monday = 1, Tuesday = 2, Wednesday = 3, Thursday = 4, Friday = 5, Saturday = 6
		for (def i = 0; i <= 6; i++) {
			if (isBitSet(weekDayBitMask, i)) {
				daysOfWeek << i
				setCmds << secure(zwave.scheduleEntryLockV3.scheduleEntryLockWeekDaySet(startHour: startHour, startMinute: startMin,
					setAction: 1, scheduleSlotId: (i + 1), stopHour: endHour, stopMinute: endMin, userIdentifier: codeID, dayOfWeek: i))
				getCmds << requestWeekDaySchedule((i + 1), codeID)
			}
		}
		if (setCmds && getCmds) {
			cmds = delayBetween(setCmds, 200) + ["delay 2000"] + delayBetween(getCmds, 200)
			state["setWeekDayWeekDays$codeID"] = daysOfWeek
		}
		
		log.debug "[DTH] 'getWeekDayScheduleSetCmd()' returning with commands := $cmds"
		return cmds
	}
	return null
}

/**
 * Returns the command(s) for setting year day schedule
 *
 * @return The command(s) for setting year day schedule
 */
private def getYearDaySetScheduleCmd(schSettings) {
	if (device.currentValue("numberOfSlotsYearDay")) {
		log.trace "[DTH] Executing 'getYearDaySetScheduleCmd()' by ${device.displayName} with schSettings := $schSettings"
		def cmds = []
		
		def codeID = Integer.parseInt(schSettings.codeId, 10)
		
		def startDate = schSettings.startDate
		def startTime = schSettings.startTime
		def endDate = schSettings.endDate
		def endTime = schSettings.endTime
		
		state["setYearDayStartDate$codeID"] = startDate
		state["setYearDayStartTime$codeID"] = startTime
		state["setYearDayStopDate$codeID"] = endDate
		state["setYearDayStopTime$codeID"] = endTime
		
		def startDateTime = getYearDayTimeDetails(startDate, startTime)
		def startHour = startDateTime.hour
		def startMinute = startDateTime.minute
		def startDay = startDateTime.day
		def startMonth = startDateTime.month
		def startYear = startDateTime.year.toString()
		startYear = startYear.substring(2, startYear.length())
		startYear = Integer.parseInt(startYear, 10)
		
		def endDateTime = getYearDayTimeDetails(endDate, endTime)
		def stopHour = endDateTime.hour
		def stopMinute = endDateTime.minute
		def stopDay = endDateTime.day
		def stopMonth = endDateTime.month
		def stopYear = endDateTime.year.toString()
		stopYear = stopYear.substring(2, stopYear.length())
		stopYear = Integer.parseInt(stopYear, 10)
		
		cmds << secure(zwave.scheduleEntryLockV3.scheduleEntryLockYearDaySet(scheduleSlotId: 1, setAction: 1, startDay: startDay, startHour: startHour, startMinute: startMinute,
				startMonth: startMonth, startYear: startYear, stopDay: stopDay, stopHour: stopHour, stopMinute: stopMinute, stopMonth: stopMonth,
				stopYear: stopYear, userIdentifier: codeID))
		cmds << requestYearDaySchedule(codeID)
		cmds = delayBetween(cmds, 200)
	   
	   log.debug "[DTH] getYearDayScheduleSetCmd() returning with commands := $cmds"
	   return cmds
	}
	return null
}

/**
 * Returns the command(s) for deleting week day schedule
 * 
 * @param codeID: The code slot number
 *
 * @return The command(s) for deleting week day schedule
 */
private def getDailyRepeatingDeleteScheduleCmd(codeID) {
	log.trace "[DTH] Executing 'getDailyRepeatingDeleteScheduleCmd()' by ${device.displayName} with codeID := $codeID"
	def cmds = []
	cmds << secure(zwave.scheduleEntryLockV3.scheduleEntryLockDailyRepeatingSet(scheduleSlotId: 1, setAction: 0, userIdentifier: codeID))
	cmds << requestDailyRepeatingSchedule(codeID)
	cmds = delayBetween(cmds, 200)

	log.debug "[DTH] 'getDailyRepeatingDeleteScheduleCmd()' returning with commands := $cmds"
	cmds
}

/**
 * Returns the command(s) for deleting week day schedule
 * 
 * @param codeID: The code slot number
 *
 * @return The command(s) for deleting week day schedule
 */
private def getWeekDayDeleteScheduleCmd(codeID) {
	def cmds = []
	def setCmds = []
	def getCmds = []
	for (def i = 0; i <= 6; i++) {
		setCmds << secure(zwave.scheduleEntryLockV3.scheduleEntryLockWeekDaySet(setAction: 0, scheduleSlotId: (i + 1), userIdentifier: codeID))
		getCmds << requestWeekDaySchedule((i + 1), codeID)
	}
	if (setCmds && getCmds) {
		cmds = delayBetween(setCmds, 200) + ["delay 2000"]+ delayBetween(getCmds, 200)
	}

	log.debug "[DTH] 'getWeekDayDeleteScheduleCmd()' returning with commands := $cmds"
	return cmds
}

/**
 * Returns the command(s) for deleting year day schedule
 * 
 * @param codeID: The code slot number
 *
 * @return The command(s) for deleting year day schedule
 */
private def getYearDayDeleteScheduleCmd(codeID) {
	log.trace "[DTH] Executing 'getYearDayDeleteScheduleCmd()' by ${device.displayName}"
	def cmds = []
	cmds << secure(zwave.scheduleEntryLockV3.scheduleEntryLockYearDaySet(scheduleSlotId: 1, setAction: 0, userIdentifier: codeID))
	cmds << requestYearDaySchedule(codeID)
	cmds = delayBetween(cmds, 200)

	log.debug "[DTH] 'getYearDayDeleteScheduleCmd()' returning with commands := $cmds"
	cmds
}

/**
 * Returns the command for daily repeating schedule get
 *
 * @param codeID: The code slot number
 *
 * @return The command for daily repeating schedule get
 */
private def requestDailyRepeatingSchedule(codeID) {
	log.debug "In requestDailyRepeatingSchedule() with codeID := $codeID"
	secure(zwave.scheduleEntryLockV3.scheduleEntryLockDailyRepeatingGet(scheduleSlotId: 1, userIdentifier: codeID))
}

/**
 * Returns the command for week day schedule get
 *
 * @param codeID: The code slot number
 *
 * @param slotID: The schedule slot number
 *
 * @return The command for week day schedule get
 */
private def requestWeekDaySchedule(slotID, codeID) {
	secure(zwave.scheduleEntryLockV3.scheduleEntryLockWeekDayGet(scheduleSlotId: slotID, userIdentifier: codeID))
}

/**
 * Returns the command for week day schedule get for all schedule slot ids
 *
 * @param codeID: The code slot number
 *
 * @return The command for week day schedule get for all schedule slot ids
 */
private def requestAllWeekDaySchedule(codeID) {
	def cmds = []
	for (def i = 0; i <= 6; i++) {
		cmds << requestWeekDaySchedule((i + 1), codeID)
	}
	cmds = delayBetween(cmds, 200)
	cmds
}

/**
 * Returns the command for year day schedule get
 *
 * @param codeID: The code slot number
 *
 * @return The command for year day schedule get
 */
private def requestYearDaySchedule(codeID) {
	secure(zwave.scheduleEntryLockV3.scheduleEntryLockYearDayGet(scheduleSlotId: 1, userIdentifier: codeID))
}

/**
 * Checks if a particular bit is set in a number
 *
 * @param weekDayBitmask: The week day bit mask
 *
 * @param bitNum: The bit number to check
 *
 * @return - true if the bit is set, false otherwise
 */
def isBitSet(weekDayBitmask, bitNum) {
	def mask = 1 << bitNum
	if ((weekDayBitmask & mask) != 0) {
		return true
	}
	return false
}

/**
 * Calculates the duration in hours and minutes
 *
 * @param startHour: The start hour
 *
 * @param startMin: The start minute
 *
 * @param endHour: The end hour
 *
 * @param endMin: The end minute
 *
 * @return The map with duration in hours and minutes
 */
private Map getDurationInHoursAndMinutes(startHour, startMin, endHour, endMin) {
	def startDate = new Date()
	startDate.set(hourOfDay: startHour, minute: startMin, second: 0, year: 2000, month: 1, date: 1)

	def endDate = new Date()
	endDate.set(hourOfDay: endHour, minute: endMin, second: 0, year: 2000, month: 1, date: 1)

	long difference = endDate.getTime() - startDate.getTime();
	difference = difference / 1000
	def durSeconds = difference % 60
	difference = (difference - durSeconds) / 60
	def durMin = difference % 60
	difference = (difference - durMin) / 60
	def durHour = difference % 24
	return [durHour: durHour, durMin: durMin]
}

/**
 * Removes the specified code if from lockCodesData and updates the attribute
 *
 * @param codeID: The code slot number
 */
def clearUserInfo(codeID) {
	log.trace "[DTH] Executing 'clearUserInfo()' by ${device.displayName} with codeID := $codeID"
	def lockCodesData = parseJson(device.currentValue("lockCodesData") ?: "{}") ?: [:]
	codeID = codeID.toString()
	lockCodesData.remove(codeID)
	lockCodesDataEvent(lockCodesData)
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
 * Reads the 'lockCodesData' attribute and parses the same
 *
 * @returns Map: The lockCodesData map
 */
private Map loadlockCodesData(codeID) {
	def lockCodesData = parseJson(device.currentValue("lockCodesData") ?: "{}") ?: [:]
	if (!lockCodesData[codeID]) {
		lockCodesData[codeID] = [:]
	}
	if (!lockCodesData[codeID].userInfo) {
		lockCodesData[codeID].userInfo = [:]
	}
	if (!lockCodesData[codeID].scheduleInfo) {
		lockCodesData[codeID].scheduleInfo = [:]
	}
	if (!lockCodesData[codeID].scheduleInfo.scheduleData) {
		lockCodesData[codeID].scheduleInfo.scheduleData = [:]
	}
	lockCodesData
}

/**
 * Populates the 'lockCodesData' attribute by calling create event
 *
 * @param lockCodesData The week day schedule info in a lock
 */
private Map lockCodesDataEvent(lockCodesData) {
	createEvent(name: "lockCodesData", value: util.toJson(lockCodesData), displayed: false, descriptionText: "'lockCodesData' attribute updated")
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
 * Creates the event map for user code creation
 *
 * @param codeID: The code slot number
 *
 * @param accessType: Recurring or limited
 *
 * @return The list of events to be sent out
 */
private def scheduleSetEvent(codeID, scheduleType) {
	// codeID seems to be an int primitive type
	codeID = codeID.toString()
	def lockCodesData = loadlockCodesData(codeID)
	lockCodesData[codeID].lastUsage = new Date().getTime()
	lockCodesData[codeID].scheduleInfo = [:]
	lockCodesData[codeID].scheduleInfo.scheduleData = [:]
	lockCodesData[codeID].scheduleInfo.scheduleType = scheduleType
	if (scheduleType == SCHEDULE_TYPE_DAILY_REPEATING) {
		lockCodesData[codeID].scheduleInfo.scheduleData = [startTime: state["setDailyRepeatingStartTime$codeID"],
			endTime: state["setDailyRepeatingEndTime$codeID"], weekDayBitMask: state["setDailyRepeatingBitMask$codeID"]]
	} else if (scheduleType == SCHEDULE_TYPE_WEEK_DAY) {
		lockCodesData[codeID].scheduleInfo.scheduleData = [startTime: state["setWeekDayStartTime$codeID"],
			endTime: state["setWeekDayEndTime$codeID"], weekDayBitMask: state["setWeekDayBitMask$codeID"]]
	} else if (scheduleType == SCHEDULE_TYPE_YEAR_DAY) {
		lockCodesData[codeID].scheduleInfo.scheduleData = [startDate: state["setYearDayStartDate$codeID"],
			startTime: state["setYearDayStartTime$codeID"], stopDate: state["setYearDayStopDate$codeID"], stopTime: state["setYearDayStopTime$codeID"]]
	}
	lockCodesDataEvent(lockCodesData)
}

/**
 * Creates the event map for user code creation
 *
 * @param codeID: The code slot number
 *
 * @return The list of events to be sent out
 */
private def scheduleDeleteEvent(codeID) {
	// codeID seems to be an int primitive type
	codeID = codeID.toString()
	def lockCodesData = loadlockCodesData(codeID)
	lockCodesData[codeID].lastUsage = new Date().getTime()
	lockCodesData[codeID].scheduleInfo = [:]
	lockCodesData[codeID].scheduleInfo.scheduleData = [:]
	lockCodesData[codeID].scheduleInfo.scheduleType = "always"
	lockCodesDataEvent(lockCodesData)
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
 * Checks if a schedule change type is set or update
 *
 * @param codeID The code slot number
 *
 * @param scheduleType The schedule type to check for
 *
 * @return "set" or "update" basis the presence of the code id in the lockCodesData map
 */
private def getScheduleChangeType(codeID, scheduleType) {
	def changeType = "set"
	def lockCodesData = device.currentValue("lockCodesData")
	if (lockCodesData) {
		lockCodesData = parseJson(lockCodesData)
		def data = lockCodesData[codeID.toString()]
		if (data) {
			def scheduleData = data.scheduleInfo?.scheduleData
			if (scheduleType == SCHEDULE_TYPE_DAILY_REPEATING || scheduleType == SCHEDULE_TYPE_WEEK_DAY) {
				if (scheduleData && scheduleData.startTime && scheduleData.endTime && scheduleData.weekDayBitMask) {
					changeType = "changed"
				}
			} else if (scheduleType == SCHEDULE_TYPE_YEAR_DAY) {
				if (scheduleData && scheduleData.startDate && scheduleData.startTime && scheduleData.stopDate && scheduleData.stopTime) {
					changeType = "changed"
				}
			}
		}
	}
	changeType
}

/**
 * Checks if schedule scanning is in progress
 *
 * @param codeID The code slot number
 *
 * @return true if scanning is in progress, else false
 */
private def isScheduleScanningInProgress(codeID) {
	def codesToScanSchedules = state.codesToScanSchedules
	if (codesToScanSchedules && codeID.toInteger() in codesToScanSchedules) {
		return true
	}
	return false
}

/**
 * Returns the schedule type basis the code slot number
 *
 * @param codeID The code slot number
 *
 * @return The schedule type
 */
private def getScheduleType(codeID) {
	def scheduleType = "always"
	def lockCodesData = device.currentValue("lockCodesData")
	if (lockCodesData) {
		lockCodesData = parseJson(lockCodesData)
		def data = lockCodesData[codeID.toString()]
		if (data && data.scheduleInfo && data.scheduleInfo.scheduleType) {
			scheduleType = data.scheduleInfo.scheduleType
		}
	}
	scheduleType
}

/**
 * Method to obtain status for description based on change type
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
 * Parse the time using SimpleDateFormat
 *
 * @param time: The time in HH:mm format
 *
 * @return The map with hour and minute
 */
private def getWeekDayHoursAndMinutes(time) {
	def sdf = new SimpleDateFormat("HH:mm")
	time = sdf.parse(time)
	Calendar calendar = GregorianCalendar.getInstance()
	calendar.setTime(time)
	[hour: calendar.get(Calendar.HOUR_OF_DAY), minute: calendar.get(Calendar.MINUTE)]
}

/**
 * Parse the date and time using SimpleDateFormat
 *
 * @param date: The date in yyyy-MM-dd format
 *
 * @param time: The time in HH:mm format
 *
 * @return The map with year, month, day, hour and minute
 */
private def getYearDayTimeDetails(date, time) {
	def sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm")
	def dateTime = sdf.parse(date + " " + time)
	Calendar calendar = GregorianCalendar.getInstance()
	calendar.setTime(dateTime)
	
	[year: calendar.get(Calendar.YEAR), month: calendar.get(Calendar.MONTH) + 1, day: calendar.get(Calendar.DAY_OF_MONTH),
		hour: calendar.get(Calendar.HOUR_OF_DAY), minute: calendar.get(Calendar.MINUTE)]
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

/**
 * Clears the daily repeating start time, end time and weekday bit mask from the state basis the code slot number
 *
 * @param codeID: The code slot number
 */
def clearDailyRepeatingScheduleDataFromState(codeID) {
	state["setDailyRepeatingStartTime$codeID"] = null
	state.remove("setDailyRepeatingStartTime$codeID")
	state["setDailyRepeatingEndTime$codeID"] = null
	state.remove("setDailyRepeatingEndTime$codeID")
	state["setDailyRepeatingBitMask$codeID"] = null
	state.remove("setDailyRepeatingBitMask$codeID")
}

/**
 * Clears the week day start time, end time and weekday bit mask from the state basis the code slot number
 *
 * @param codeID: The code slot number
 */
def clearWeekdayScheduleDataFromState(codeID) {
	state["setWeekDayStartTime$codeID"] = null
	state.remove("setWeekDayStartTime$codeID")
	state["setWeekDayEndTime$codeID"] = null
	state.remove("setWeekDayEndTime$codeID")
	state["setWeekDayBitMask$codeID"] = null
	state.remove("setWeekDayBitMask$codeID")
	state["setWeekDayWeekDays$codeID"] = null
	state.remove("setWeekDayWeekDays$codeID")
}

/**
 * Clears the week day scan count and weekday bit mask from the state basis the code slot number
 *
 * @param codeID: The code slot number
 */
def clearWeekdayScanningDataFromState(codeID) {
	state["weekDayScanCount$codeID"] = null
	state.remove("weekDayScanCount$codeID")
	state["setWeekDayScanBitMask$codeID"] = null
	state.remove("setWeekDayScanBitMask$codeID")
}

/**
 * Clears the code name, phone number, start time, end time and weekday bitmask from the state basis the code slot number
 *
 * @param codeID: The code slot number
 */
def clearYeardayScheduleDataFromState(codeID) {
	state["setYearDayStartDate$codeID"] = null
	state.remove("setYearDayStartDate$codeID")
	state["setYearDayStartTime$codeID"] = null
	state.remove("setYearDayStartTime$codeID")
	state["setYearDayStopDate$codeID"] = null
	state.remove("setYearDayStopDate$codeID")
	state["setYearDayStopTime$codeID"] = null
	state.remove("setYearDayStopTime$codeID")
}

/**
 * Constructs a map of the code length parameter in Schlage lock
 *
 * @return map: The map with key and values for parameter number, and size
 */
def getSchlageLockParam() {
	def map = [
		codeLength: [ number: 16, size: 1]
	]
	map
}

/**
 * Utility function to check if the lock manufacturer is Schlage
 *
 * @return true if the lock manufacturer is Schlage, else false
 */
def isSchlageLock() {
	if ("003B" == zwaveInfo.mfr) {
		if("Schlage" != getDataValue("manufacturer")) {
			updateDataValue("manufacturer", "Schlage")
		}
		return true
	}
	return false
}

/**
 * Utility function to check if the lock manufacturer is Kwikset
 *
 * @return true if the lock manufacturer is Kwikset, else false
 */
def isKwiksetLock() {
	if ("0090" == zwaveInfo.mfr) {
		if("Kwikset" != getDataValue("manufacturer")) {
			updateDataValue("manufacturer", "Kwikset")
		}
		return true
	}
	return false
}

/**
 * Utility function to check if the lock manufacturer is Yale
 *
 * @return true if the lock manufacturer is Yale, else false
 */
def isYaleLock() {
	if ("0129" == zwaveInfo.mfr) {
		if("Yale" != getDataValue("manufacturer")) {
			updateDataValue("manufacturer", "Yale")
		}
		return true
	}
	return false
}

/**
 * Returns true if this lock generates door lock operation report before alarm report, false otherwise
 * @return true if this lock generates door lock operation report before alarm report, false otherwise
 */
def generatesDoorLockOperationReportBeforeAlarmReport() {
	//Fix for ICP-2367, ICP-2366
	if(isYaleLock() && "0007" == zwaveInfo.prod && "0001" == zwaveInfo.model) {
		//Yale Keyless Connected Smart Door Lock
		return true
	}
	return false
}

 /** 
 * Generic function for reading code Slot ID from AlarmReport command
 * @param cmd: The AlarmReport command
 * @return user code slot id
 */
def readCodeSlotId(physicalgraph.zwave.commands.alarmv2.AlarmReport cmd) {
	if(cmd.numberOfEventParameters == 1) {
		return cmd.eventParameter[0]
	} else if(cmd.numberOfEventParameters >= 3) {
		return cmd.eventParameter[2]
	}
	return cmd.alarmLevel
}

/**
 * Function for setting locks time and offset
 */
private def setClock() {
	log.trace "In setClock"
	def cmds = []

	def commclasses = zwaveInfo.sec
	if(commclasses.contains("4E") && commclasses.contains("8B")) {
		def timeZoneFlag = 0
		def timeZoneOffset = location.timeZone.getRawOffset() / 1000

		if(timeZoneOffset < 0) {
			timeZoneFlag = 1
			timeZoneOffset = timeZoneOffset.abs()
		}

		def timeobt = timeZoneOffset.longValue()
		def minutes = (timeobt % 3600) / 60
		def hours = (timeobt - minutes * 60) / 3600

		if(!location.timeZone.useDaylightTime() || !location.timeZone.inDaylightTime(new Date())) {
			log.debug "Setting offset on lock hourTzo: ${hours} minuteOffsetDst: 0 minuteTzo: ${minutes} signOffsetDst: 0 signTzo: ${timeZoneFlag}"
			cmds << secure(zwave.scheduleEntryLockV3.scheduleEntryLockTimeOffsetSet(hourTzo:hours, minuteOffsetDst:0, minuteTzo:minutes, signOffsetDst:0, signTzo: timeZoneFlag))
		} else {
			def dstOffset = location.timeZone.dstSavings / 1000
			def dstFlag = 0
			if(dstOffset < 0) {
				dstFlag = 1
				dstOffset = dstOffset.abs()
			}
			def minutesDst = dstOffset  / 60
			log.debug "Setting offset on lock hourTzo: ${hours} minuteOffsetDst: ${minutesDst} minuteTzo: ${minutes} signOffsetDst: ${dstFlag} signTzo: ${timeZoneFlag}"
			cmds << secure(zwave.scheduleEntryLockV3.scheduleEntryLockTimeOffsetSet(hourTzo: hours, minuteOffsetDst: minutesDst, minuteTzo: minutes, signOffsetDst: dstFlag, signTzo: timeZoneFlag))
			}

		def now = new Date().toCalendar()
		short hour = now.get(Calendar.HOUR_OF_DAY)
		short min = now.get(Calendar.MINUTE)
		short sec = now.get(Calendar.SECOND)
		short day = now.get(Calendar.DAY_OF_MONTH)
		short month = now.get(Calendar.MONTH) + 1
		int year = now.get(Calendar.YEAR)

		log.debug "Setting time on lock - $year-$month-$day $hour:$min:$sec (YYYY-MM-DD hh:mm:ss)"
		cmds << secure(zwave.timeParametersV1.timeParametersSet(hourUtc: hour, minuteUtc: min, secondUtc: sec, day: day , month: month, year: year))
	}
	cmds
}
