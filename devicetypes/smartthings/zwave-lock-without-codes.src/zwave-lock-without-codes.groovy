/**
 * 	Z-Wave Lock
 *
 *  Copyright 2018 SmartThings
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
	definition(name: "Z-Wave Lock Without Codes", namespace: "smartthings", author: "SmartThings", runLocally: true, minHubCoreVersion: '000.017.0012', executeCommandsLocally: false, mnmn: "SmartThings", vid: "generic-lock-2") {
		capability "Actuator"
		capability "Lock"
		capability "Refresh"
		capability "Sensor"
		capability "Battery"
		capability "Health Check"
		capability "Configuration"

		fingerprint inClusters: "0x62"
		fingerprint mfr: "010E", prod: "0009", model: "0001", deviceJoinName: "Danalock V3 Smart Lock"
		fingerprint mfr: "0090", prod: "0003", model: "0446", deviceJoinName: "Kwikset Convert Deadbolt Door Lock" //99140
		fingerprint mfr: "033F", prod: "0001", model: "0001", deviceJoinName: "August Smart Lock Pro"
		fingerprint mfr: "021D", prod: "0003", model: "0001", deviceJoinName: "Alfred Smart Home Touchscreen Deadbolt" // DB2
	}

	simulator {
	}

	tiles(scale: 2) {
		multiAttributeTile(name: "toggle", type: "generic", width: 6, height: 4) {
			tileAttribute("device.lock", key: "PRIMARY_CONTROL") {
				attributeState "locked", label: 'locked', action: "lock.unlock", icon: "st.locks.lock.locked", backgroundColor: "#00A0DC", nextState: "unlocking"
				attributeState "unlocked", label: 'unlocked', action: "lock.lock", icon: "st.locks.lock.unlocked", backgroundColor: "#ffffff", nextState: "locking"
				attributeState "unlocked with timeout", label: 'unlocked', action: "lock.lock", icon: "st.locks.lock.unlocked", backgroundColor: "#ffffff", nextState: "locking"
				attributeState "unknown", label: "unknown", action: "lock.lock", icon: "st.locks.lock.unknown", backgroundColor: "#ffffff", nextState: "locking"
				attributeState "locking", label: 'locking', icon: "st.locks.lock.locked", backgroundColor: "#00A0DC"
				attributeState "unlocking", label: 'unlocking', icon: "st.locks.lock.unlocked", backgroundColor: "#ffffff"
			}
		}
		standardTile("lock", "device.lock", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "default", label: 'lock', action: "lock.lock", icon: "st.locks.lock.locked", nextState: "locking"
		}
		standardTile("unlock", "device.lock", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "default", label: 'unlock', action: "lock.unlock", icon: "st.locks.lock.unlocked", nextState: "unlocking"
		}
		valueTile("battery", "device.battery", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "battery", label: '${currentValue}% battery', unit: ""
		}
		standardTile("refresh", "device.lock", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "default", label: '', action: "refresh.refresh", icon: "st.secondary.refresh"
		}

		main "toggle"
		details(["toggle", "lock", "unlock", "battery", "refresh"])
	}
}

import physicalgraph.zwave.commands.doorlockv1.*

/**
 * Mapping of command classes and associated versions used for this DTH
 */
private getCommandClassVersions() {
	[
		0x62: 1,  // Door Lock
		0x63: 1,  // User Code
		0x71: 2,  // Alarm
		0x72: 2,  // Manufacturer Specific
		0x80: 1,  // Battery
		0x85: 2,  // Association
		0x86: 1,  // Version
		0x98: 1   // Security 0
	]
}

/**
 * Called on app installed
 */
def installed() {
	// Device-Watch pings if no device events received for 1 hour (checkInterval)
	sendEvent(name: "checkInterval", value: 1 * 60 * 60, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID, offlinePingable: "1"])
	scheduleInstalledCheck()
}

/**
 * Verify that we have actually received the lock's initial states.
 * If not, verify that we have at least requested them or request them,
 * and check again.
 */
def scheduleInstalledCheck() {
	runIn(120, installedCheck, [forceForLocallyExecuting: true])
}

def installedCheck() {
	if (device.currentState("lock") && device.currentState("battery")) {
		unschedule("installedCheck")
	} else {
		// We might have called updated() or configure() at some point but not have received a reply, so don't flood the network
		if (!state.lastLockDetailsQuery || secondsPast(state.lastLockDetailsQuery, 2 * 60)) {
			def actions = updated()

			if (actions) {
				sendHubCommand(actions.toHubAction())
			}
		}

		scheduleInstalledCheck()
	}
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
		if (!device.currentState("lock") || !state.configured) {
			log.debug "Returning commands for lock operation get and battery get"
			if (!state.configured) {
				cmds << doConfigure()
			}
			cmds << refresh()
			hubAction = response(delayBetween(cmds, 30 * 1000))
		}
	} catch (e) {
		log.warn "updated() threw $e"
	}
	hubAction
}

/**
 * Configures the device to settings needed by SmarthThings at device discovery time
 */
def configure() {
	log.trace "[DTH] Executing 'configure()' for device ${device.displayName}"
	def cmds = doConfigure()
	log.debug "Configure returning with commands := $cmds"
	cmds
}

/**
 * Returns the list of commands to be executed when the device is being configured/paired
 */
def doConfigure() {
	log.trace "[DTH] Executing 'doConfigure()' for device ${device.displayName}"
	state.configured = true
	def cmds = []
	cmds << secure(zwave.doorLockV1.doorLockOperationGet())
	if (zwaveInfo.mfr != "010E") {
		cmds << secure(zwave.batteryV1.batteryGet())
		cmds = delayBetween(cmds, 30 * 1000)
	}

	state.lastLockDetailsQuery = now()

	log.debug "Do configure returning with commands := $cmds"
	cmds
}

/**
 * Responsible for parsing incoming device messages to generate events
 *
 * @param description : The incoming description from the device
 *
 * @return result: The list of events to be sent out
 *
 */
def parse(String description) {
	log.trace "[DTH] Executing 'parse(String description)' for device ${device.displayName} with description = $description"

	def result = null
	if (description.startsWith("Err")) {
		if (state.sec) {
			result = createEvent(descriptionText: description, isStateChange: true, displayed: false)
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
		def cmd = zwave.parse(description, commandClassVersions)
		if (cmd) {
			result = zwaveEvent(cmd)
		}
	}
	log.debug "[DTH] parse() - returning result=$result"
	result
}

/**
 * Responsible for parsing SecurityMessageEncapsulation command
 *
 * @param cmd : The SecurityMessageEncapsulation command to be parsed
 *
 * @return The event(s) to be sent out
 *
 */
def zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityMessageEncapsulation cmd) {
	log.trace "[DTH] Executing 'zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityMessageEncapsulation)' with cmd = $cmd"
	def encapsulatedCommand = cmd.encapsulatedCommand(commandClassVersions)
	if (encapsulatedCommand) {
		zwaveEvent(encapsulatedCommand)
	}
}

/**
 * Responsible for parsing NetworkKeyVerify command
 *
 * @param cmd : The NetworkKeyVerify command to be parsed
 *
 * @return The event(s) to be sent out
 *
 */
def zwaveEvent(physicalgraph.zwave.commands.securityv1.NetworkKeyVerify cmd) {
	log.trace "[DTH] Executing 'zwaveEvent(physicalgraph.zwave.commands.securityv1.NetworkKeyVerify)' with cmd = $cmd"
	createEvent(name: "secureInclusion", value: "success", descriptionText: "Secure inclusion was successful", isStateChange: true)
}

/**
 * Responsible for parsing SecurityCommandsSupportedReport command
 *
 * @param cmd : The SecurityCommandsSupportedReport command to be parsed
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
	createEvent(name: "secureInclusion", value: "success", descriptionText: "Lock is securely included", isStateChange: true)
}

/**
 * Responsible for parsing DoorLockOperationReport command
 *
 * @param cmd : The DoorLockOperationReport command to be parsed
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
	def map = [name: "lock"]
	map.data = [lockName: device.displayName]
	if (cmd.doorLockMode == 0xFF) {
		map.value = "locked"
		map.descriptionText = "Locked"
	} else if (cmd.doorLockMode >= 0x40) {
		map.value = "unknown"
		map.descriptionText = "Unknown state"
	} else if (cmd.doorLockMode == 0x01) {
		map.value = "unlocked with timeout"
		map.descriptionText = "Unlocked with timeout"
	} else {
		map.value = "unlocked"
		map.descriptionText = "Unlocked"
	}
	return result ? [createEvent(map), *result] : createEvent(map)
}

/**
 * Responsible for parsing AlarmReport command
 *
 * @param cmd : The AlarmReport command to be parsed
 *
 * @return The event(s) to be sent out
 *
 */
def zwaveEvent(physicalgraph.zwave.commands.alarmv2.AlarmReport cmd) {
	log.trace "[DTH] Executing 'zwaveEvent(physicalgraph.zwave.commands.alarmv2.AlarmReport)' with cmd = $cmd"
	def result = []

	if (cmd.zwaveAlarmType == 6) {
		result = handleAccessAlarmReport(cmd)
	} else if (cmd.zwaveAlarmType == 8) {
		//I don't this is supported now, but better safe than sorry.
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
 * @param cmd : The AlarmReport command to be parsed
 *
 * @return The event(s) to be sent out
 *
 */
private def handleAccessAlarmReport(cmd) {
	log.trace "[DTH] Executing 'handleAccessAlarmReport' with cmd = $cmd"
	def result = []
	def map = null
	def codeID, changeType, codeName
	def deviceName = device.displayName
	if (1 <= cmd.zwaveAlarmEvent && cmd.zwaveAlarmEvent < 10) {
		map = [name: "lock", value: (cmd.zwaveAlarmEvent & 1) ? "locked" : "unlocked"]
	}
	switch (cmd.zwaveAlarmEvent) {
		case 1: // Manually locked
			map.descriptionText = "Locked manually"
			map.data = [method: (cmd.alarmLevel == 2) ? "keypad" : "manual"]
			break
		case 2: // Manually unlocked
			map.descriptionText = "Unlocked manually"
			map.data = [method: "manual"]
			break
		case 3: // Locked by command
			map.descriptionText = "Locked"
			map.data = [method: "command"]
			break
		case 4: // Unlocked by command
			map.descriptionText = "Unlocked"
			map.data = [method: "command"]
			break
		case 7:
			map = [name: "lock", value: "unknown", descriptionText: "Unknown state"]
			map.data = [method: "manual"]
			break
		case 8:
			map = [name: "lock", value: "unknown", descriptionText: "Unknown state"]
			map.data = [method: "command"]
			break
		case 9: // Auto locked
			map = [name: "lock", value: "locked", data: [method: "auto"]]
			map.descriptionText = "Auto locked"
			break
		case 0xA:
			map = [name: "lock", value: "unknown", descriptionText: "Unknown state"]
			map.data = [method: "auto"]
			break
		case 0xB:
			map = [name: "lock", value: "unknown", descriptionText: "Unknown state"]
			break
		case 0x13:
			map = [name: "tamper", value: "detected", descriptionText: "Keypad attempts exceed code entry limit", isStateChange: true, displayed: true]
			break
		default:
			map = [displayed: false, descriptionText: "Alarm event ${cmd.alarmType} level ${cmd.alarmLevel}"]
			break
	}

	if (map) {
		if (map.data) {
			map.data.lockName = deviceName
		} else {
			map.data = [lockName: deviceName]
		}
		result << createEvent(map)
	}
	result = result.flatten()
	result
}

/**
 * Responsible for handling Battery AlarmReport command
 *
 * @param cmd : The AlarmReport command to be parsed
 *
 * @return The event(s) to be sent out
 */
private def handleBatteryAlarmReport(cmd) {
	log.trace "[DTH] Executing 'handleBatteryAlarmReport' with cmd = $cmd"
	def result = []
	def deviceName = device.displayName
	def map = null
	switch (cmd.zwaveAlarmEvent) {
		case 0x0A:
			map = [name: "battery", value: 1, descriptionText: "Battery level critical", displayed: true, data: [lockName: deviceName]]
			break
		case 0x0B:
			map = [name: "battery", value: 0, descriptionText: "Battery too low to operate lock", isStateChange: true, displayed: true, data: [lockName: deviceName]]
			break
		default:
			map = [displayed: false, descriptionText: "Alarm event ${cmd.alarmType} level ${cmd.alarmLevel}"]
			break
	}
	result << createEvent(map)
	result
}

/**
 * Responsible for parsing BatteryReport command
 *
 * @param cmd : The BatteryReport command to be parsed
 *
 * @return The event(s) to be sent out
 *
 */
def zwaveEvent(physicalgraph.zwave.commands.batteryv1.BatteryReport cmd) {
	log.trace "[DTH] Executing 'zwaveEvent(physicalgraph.zwave.commands.batteryv1.BatteryReport)' with cmd = $cmd"
	def map = [name: "battery", unit: "%"]
	if (cmd.batteryLevel == 0xFF) {
		map.value = 1
		map.descriptionText = "${device.displayName} has a low battery"
		map.isStateChange = true
	} else {
		map.value = cmd.batteryLevel
	}
	state.lastbatt = now()
	if (cmd.batteryLevel == 0 && device.latestValue("battery") > 20) {
		// Danalock reports 00 when batteries are changed. We do not know what is the real level at this point.
		// We will ignore this level to mimic normal operation of the device (battery level is refreshed only when motor is operating)
		log.warn "Erroneous battery report dropped from ${device.latestValue("battery")} to $map.value. Not reporting"
	} else {
		createEvent(map)
	}

}


/**
 * Responsible for parsing zwave command
 *
 * @param cmd : The zwave command to be parsed
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
	def cmds = []
	cmds << zwave.doorLockV1.doorLockOperationSet(doorLockMode: doorLockMode)
	cmds << zwave.doorLockV1.doorLockOperationGet()
	if (zwaveInfo.mfr == "010E") {
		//Danalock checks battery only when motor is turned on.
		cmds << zwave.batteryV1.batteryGet()
	}
	secureSequence(cmds, 4200)
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
	if (zwaveInfo.mfr == "010E") {
		//Danalock V3 handles timeout as a parameter that causes all normal unlock() commands to have timeout
		log.trace "[DTH] Executing unlock() for device ${device.displayName}"
	} else {
		log.trace "[DTH] Executing unlockWithTimeout() for device ${device.displayName}"
		lockAndCheck(DoorLockOperationSet.DOOR_LOCK_MODE_DOOR_UNSECURED_WITH_TIMEOUT)
	}
}

/**
 * PING is used by Device-Watch in attempt to reach the Device
 */
def ping() {
	log.trace "[DTH] Executing ping() for device ${device.displayName}"
	runIn(30, followupStateCheck)
	if (zwaveInfo.mfr == "010E") {
		secure(zwave.doorLockV1.doorLockOperationGet())
	} else {
		secureSequence([zwave.doorLockV1.doorLockOperationGet(), zwave.batteryV1.batteryGet()])
	}
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
	if (zwaveInfo.mfr == "010E") {
		secure(zwave.doorLockV1.doorLockOperationGet())
	} else {
		secureSequence([zwave.doorLockV1.doorLockOperationGet(), zwave.batteryV1.batteryGet()])
	}
}

/**
 * Encapsulates a command
 *
 * @param cmd : The command to be encapsulated
 *
 * @returns ret: The encapsulated command
 */
private secure(physicalgraph.zwave.Command cmd) {
	zwave.securityV1.securityMessageEncapsulation().encapsulate(cmd).format()
}

/**
 * Encapsulates list of command and adds a delay
 *
 * @param commands : The list of command to be encapsulated
 * @param delay : The delay between commands
 *
 * @returns The encapsulated commands
 */
private secureSequence(commands, delay = 4200) {
	delayBetween(commands.collect { secure(it) }, delay)
}

/**
 * Checks if the time elapsed from the provided timestamp is greater than the number of senconds provided
 *
 * @param timestamp : The timestamp
 * @param seconds : The number of seconds
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

