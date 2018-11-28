/**
 *  Samsung Smart Doorlock
 *
 *  Copyright 2018 Samsung SDS
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
	definition (name: "Samsung Smart Doorlock", namespace: "Samsung SDS", author: "kyun.park", mnmn: "SmartThings", vid: "generic-lock") {
		capability "Actuator"
		capability "Lock"
		capability "Battery"
		capability "Configuration"
		capability "Health Check"
		capability "Refresh"

		fingerprint profileId: "0104", inClusters: "0000, 0001, 0003, 0004, 0005, 0009, 0101", outClusters: "0019", manufacturer: "SAMSUNG SDS"
        
	}

	tiles(scale: 2) {
		multiAttributeTile(name:"toggle", type:"generic",  decoration:"flat", width:6, height:4) {
			tileAttribute ("device.lock", key:"PRIMARY_CONTROL") {
				attributeState "locked", label:'locked', action:"lock.unlock", icon:"st.locks.lock.locked", backgroundColor:"#00A0DC", nextState:"unlocking"
				attributeState "unlocked", label:'unlocked', icon:"st.locks.lock.unlocked", backgroundColor:"#ffffff"
				attributeState "unknown", label:"unknown", icon:"st.locks.lock.unknown", backgroundColor:"#ffffff"
				attributeState "unlocking", label:'unlocking', icon:"st.locks.lock.unlocked", backgroundColor:"#ffffff"
				attributeState "autolockError", label:'autolock error', icon:"st.locks.lock.unlocked", backgroundColor:"#ff0000"
			}
			tileAttribute("device.displayName", key: "SECONDARY_CONTROL") {
				attributeState "displayName", label: 'Model:  ${currentValue}'
			}
		}
		valueTile("battery", "device.battery", inactiveLabel:false, decoration:"flat", width:6, height:2) {
			state "battery", label:'${currentValue}% BATTERY', unit:""
		}
		main "toggle"
		details(["toggle", "battery"])
	}
}

// Globals - Cluster IDs
private getCLUSTER_POWER() { 0x0001 }
private getCLUSTER_DOORLOCK() { 0x0101 }
private getCLUSTER_ALARM() { 0x0009 }

// Globals - Command IDs
private getDOORLOCK_CMD_UNLOCK_DOOR() { 0x1F }
private getDOORLOCK_RESPONSE_OPERATION_EVENT() { 0x20 }
private getPOWER_ATTR_BATTERY_VOLTAGE() { 0x0020 }
private getDOORLOCK_ATTR_LOCKSTATE() { 0x0000 }
private getDOORLOCK_ATTR_DOORSTATE() { 0x0003 }
private getALARM_ATTR_ALARM_COUNT() { 0x0000 }
private getALARM_CMD_ALARM() { 0x00 }

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
	sendEvent(name: "lockRemoved", value: device.id, isStateChange: true, displayed: false)
}


/**
 * Ping is used by Device-Watch in attempt to reach the device
 */
def ping() {
	log.trace "ZigBee DTH - Executing ping() for device ${device.displayName}"
	refresh()
}

/**
 * Called when the user taps on the refresh button
 */
def refresh() {
	log.trace "ZigBee DTH - Executing refresh() for device ${device.displayName}"
	def cmds = zigbee.readAttribute(CLUSTER_DOORLOCK, DOORLOCK_ATTR_DOORSTATE)
	log.info "ZigBee DTH - refresh() returning with cmds:- $cmds"
	return cmds
}


/**
 * Configures the device to settings needed by SmarthThings at device discovery time
 *
 */
def configure() {
	log.trace "ZigBee DTH - Executing configure() for device ${device.displayName}"
	/**
	* Configure Reporting is not set for Samsung Smart Doorlock devices as the doorlocks are programmed to automatically report their status
	* Lock state is automatically reported every 30 minutes
	* Battery state is automatically reported every 12 hours
	* Battery state is also reported when the batteries are exchanged
	*/
	def cmds = zigbee.command(0x0000, 0x1E, "",[mfgCode: 0003]) // read modelName of the device as it is not being sent with zbjoin.
	sendEvent(name: "lock", value: "unlocked", isStateChange: true, displayed: false)
	sendEvent(name: "battery", value: "100", isStateChange: true, displayed: false)
	// Device-Watch allows 2 check-in misses from device + ping (plus 2 min lag time)
	sendEvent(name: "checkInterval", value: 2 * 60 * 60 + 2 * 60, displayed: false, data: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID, offlinePingable: "1"])

	log.info "ZigBee DTH - configure() returning with cmds:- $cmds"
	cmds
}


/**
 * Executes unlock command on a Zigbee lock
 */
def unlock() {
	log.trace "ZigBee DTH - Executing unlock() for device ${device.displayName}"
	def cmds = zigbee.command(CLUSTER_DOORLOCK, DOORLOCK_CMD_UNLOCK_DOOR, "100431323335", [mfgCode: 0003])
	log.info "ZigBee DTH - unlock() returning with cmds:- $cmds"
	return cmds
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
	if (clusterInt == CLUSTER_POWER && attrInt == POWER_ATTR_BATTERY_VOLTAGE) {
		responseMap.name = "battery"
		responseMap.value = getBatteryResult(Integer.parseInt(descMap.value, 10))
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
	} else {
		log.trace "ZigBee DTH - parseAttributeResponse() - ignoring attribute response"
		return null
	}

	responseMap.data = [ lockName: deviceName ]
	result << createEvent(responseMap)
	log.info "ZigBee DTH - parseAttributeResponse() returning with result:- $result"
	return result
}



/*

Change the voltage reading to the percentage format
as our current doorlock zigbee module does not support percentage reading

*/
private def getBatteryResult(rawValue) {
	def linkText = getLinkText(device)
	def result
	def volts = rawValue / 10
	def descriptionText
	if (volts > 6.5) {
		result = 200
		log.debug "Battery Reading Over the Limit"
	}
	else {
		def minVolts = 4.0
		def maxVolts = 6.0
		def pct = (volts - minVolts) / (maxVolts - minVolts)
		int p = pct * 100
		result = Math.min(100, p)
		log.debug "${linkText} battery is ${result.value}%"
	}
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

	def cmd = descMap.commandInt
	def clusterInt = descMap.clusterInt

	if (clusterInt == CLUSTER_DOORLOCK && (cmd == DOORLOCK_CMD_LOCK_DOOR || cmd == DOORLOCK_CMD_UNLOCK_DOOR)) {
		log.trace "ZigBee DTH - Executing DOOR LOCK/UNLOCK SUCCESS for device ${deviceName} with description map:- $descMap"
		//Read the unlock result here, and then confirms that door whether successfully opened or not.
		if (Integer.parseInt(data[0], 10) == 0) {
			responseMap.name = "lock"
			responseMap.displayed = true
			responseMap.isStateChange = true
			responseMap = [ name: "lock", value: "unlocked", descriptionText: "Successfully unlocked" ]
		} else if (Integer.parseInt(data[0], 10) == 1) {
			log.debug "failed to unlock doorlock"
		} else {
			log.debug "unexpected result from unlock command"
		}
        
	} else if (clusterInt == 0x0000){
		log.trace "ZigBee DTH - Reading Doorlock Model Name for display purpose ${data}"
		//Read the model code here, and then translate hex to alphabet. 
		int length = Integer.parseInt(data[0], 16)
		int i = 2;
		String model = "" + (char) Integer.parseInt(data[1], 16)
		char tempChar
		while ((Integer.parseInt(data[i], 16) != 32) && i < (length-1)){ 
			tempChar = (char) Integer.parseInt(data[i], 16)
			model = model + tempChar
			i++
		}
		responseMap = [ name: "displayName", value: model, displayed: false]
	} else if (clusterInt == CLUSTER_DOORLOCK && cmd == DOORLOCK_RESPONSE_OPERATION_EVENT) {
		log.trace "ZigBee DTH - Executing DOORLOCK_RESPONSE_OPERATION_EVENT for device ${deviceName} with description map:- $descMap"
		def eventSource = Integer.parseInt(data[0], 16)
		def eventCode = Integer.parseInt(data[1], 16)

		responseMap.name = "lock"
		responseMap.displayed = true
		responseMap.isStateChange = true

		def desc = ""
		def codeName = ""

		if (eventSource == 0) {
			desc = "using keypad"
			responseMap.data = [ method: "keypad" ]
		} else if (eventSource == 1) {
			responseMap.data = [ method: "command" ]
		} else if (eventSource == 2) {
			desc = "from inside"
			responseMap.data = [ method: "manual" ]
		} else if (eventSource == 3) {
			desc = "using keycard"
			responseMap.data = [ method: "rfid" ]
		} else if (eventSource == 4) {
			desc = "using fingerprint"
			responseMap.data = [ method: "fingerprint" ]
		} else if (eventSource == 5) {
			desc = "using Bluetooth"
			responseMap.data = [ method: "bluetooth" ]
		}
        

		switch (eventCode) {
			case 1:
				responseMap.value = "locked"
				responseMap.descriptionText = "Locked ${desc}"
				break
			case 2:
				responseMap.value = "unlocked"
				responseMap.descriptionText = "Unlocked ${desc}"
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
			case 2: // Low Voltage
				responseMap = [ name: "battery", value: device.currentValue("battery"), descriptionText: "Battery is low", isStateChange: true ]
				break
			case 4: // Tamper Alarm - wrong code entry limit 5 times
				responseMap = [ name: "tamper", value: "detected", descriptionText: "Keypad attempts exceed code entry limit", isStateChange: true ]
				break
			case 6: // Forced Door Open under Door Locked Condition
				responseMap = [ name: "tamper", value: "detected", descriptionText: "Door forced open under door locked condition", isStateChange: true ]
				break
			case 7: // Door opened for over 30 seconds
				responseMap = [ name: "lock", value: "autolockError", descriptionText: "Door remains opened for over 30 seconds" ]
				break
			case 8: // Door opened with threat (code + 112)
				responseMap = [ name: "tamper", value: "detected", descriptionText: "Opened door under threatening condition.", isStateChange: true ]
				break
			case 9: // Fire detection
				responseMap = [ name: "tamper", value: "detected", descriptionText: "Fire detected", isStateChange: true ]
				break
			case 10: // Stranger detected (IR detection over 1 min)
				responseMap = [ name: "tamper", value: "detected", descriptionText: "Stranger in front of door detected", isStateChange: true ]
				break
			default:
				break
		}
	} else {
		/********LATER TO ADD PROGRAMMING EVENT HERE, SUCH AS KEY CHANGE, UPDATE**********/
		log.trace "ZigBee DTH - parseCommandResponse() - ignoring command response"
	}

	if (responseMap["value"]) {
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
