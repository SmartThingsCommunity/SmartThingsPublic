/**
 *
 *	Copyright 2018 SmartThings
 *
 *	Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *	in compliance with the License. You may obtain a copy of the License at:
 *
 *		http://www.apache.org/licenses/LICENSE-2.0
 *
 *	Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *	on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *	for the specific language governing permissions and limitations under the License.
 *
 */
 
import physicalgraph.zigbee.zcl.DataType
import physicalgraph.zigbee.clusters.iaszone.ZoneStatus

metadata {
	definition (name:"ZigBee Lock Without Codes", namespace:"smartthings", author:"SmartThings", vid:"generic-lock-2", mnmn:"SmartThings", runLocally:true, minHubCoreVersion:'000.022.00013', executeCommandsLocally:true) {
		capability "Actuator"
		capability "Lock"
		capability "Refresh"
		capability "Sensor"
		capability "Battery"
		capability "Configuration"
		capability "Health Check"

		fingerprint profileId:"0104, 000A", inClusters:"0000, 0001, 0003, 0009, 0020,0101, 0B05", outclusters:"000A, 0019, 0B05", manufacturer:"Danalock", model:"V3-BTZB", deviceJoinName:"Danalock V3 Smart Lock"
		fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0500, 0101", outClusters: "0019", model: "E261-KR0B0Z0-HA", deviceJoinName: "C2O Lock", mnmn: "SmartThings", vid: "C2O-ZigBee-Lock"

	}

	tiles(scale:2) {
		multiAttributeTile(name:"toggle", type:"generic", width:6, height:4) {
			tileAttribute("device.lock", key:"PRIMARY_CONTROL"){
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

private getCLUSTER_POWER() { 0x0001 }
private getCLUSTER_DOORLOCK() { 0x0101 }
private getCLUSTER_IAS_ZONE() { 0x0500 }
private getDOORLOCK_CMD_LOCK_DOOR() { 0x00 }
private getDOORLOCK_CMD_UNLOCK_DOOR() { 0x01 }
private getDOORLOCK_RESPONSE_OPERATION_EVENT() { 0x20 }
private getDOORLOCK_RESPONSE_PROGRAMMING_EVENT() { 0x21 }
private getPOWER_ATTR_BATTERY_PERCENTAGE_REMAINING() { 0x0021 }
private getDOORLOCK_ATTR_LOCKSTATE() { 0x0000 }
private getIAS_ATTR_ZONE_STATUS() { 0x0002 }


def installed() {
	log.debug "Executing installed()"
	initialize()
}

def uninstalled() {
	log.debug "Executing uninstalled()"
	sendEvent(name:"lockRemoved", value:device.id, isStateChange:true, displayed:false)
}

def updated() {
	try {
		if (!state.init || !state.configured) {
			state.init = true
			def cmds = []
			if (!state.configured) {
				cmds << initialize()
			} else {
				cmds << refresh()
			}

			return response(cmds.flatten())
		}
	} catch (e) {
		log.warn "ZigBee DTH - updated() threw exception:- $e"
	}
	return null
}

def ping() {
	refresh()
}

def refresh() {

	def cmds = []
	cmds += zigbee.readAttribute(CLUSTER_DOORLOCK, DOORLOCK_ATTR_LOCKSTATE)

	if (isC2OLock()) {
		cmds += zigbee.readAttribute(CLUSTER_IAS_ZONE, IAS_ATTR_ZONE_STATUS)
	}

	return cmds
}

def configure() {
	def cmds = initialize()
	return cmds
}

def initialize() {
	log.debug "Executing initialize()"
	state.configured = true
	sendEvent(name:"checkInterval", value: 2 * 60 * 60 + 2 * 60, displayed:false, data: [protocol:"zigbee", hubHardwareId:device.hub.hardwareID, offlinePingable:"1"])

	def cmds = []
	if (isC2OLock()) {
		cmds += zigbee.readAttribute(CLUSTER_DOORLOCK, DOORLOCK_ATTR_LOCKSTATE)
		cmds += zigbee.readAttribute(CLUSTER_IAS_ZONE, IAS_ATTR_ZONE_STATUS)
		cmds += zigbee.enrollResponse()
		cmds += configureReporting(CLUSTER_IAS_ZONE, IAS_ATTR_ZONE_STATUS, DataType.BITMAP16, 30, 60*5, null)
	} else {
		cmds += zigbee.configureReporting(CLUSTER_DOORLOCK, DOORLOCK_ATTR_LOCKSTATE,DataType.ENUM8, 0, 3600, null) 
		cmds += zigbee.configureReporting(CLUSTER_POWER, POWER_ATTR_BATTERY_PERCENTAGE_REMAINING,DataType.UINT8, 600, 21600, 0x01)
		cmds += zigbee.readAttribute(CLUSTER_POWER, POWER_ATTR_BATTERY_PERCENTAGE_REMAINING)

		cmds += refresh()
	}

	return cmds
}

def lock() {
	def cmds = zigbee.command(CLUSTER_DOORLOCK, DOORLOCK_CMD_LOCK_DOOR) +
				zigbee.readAttribute(CLUSTER_POWER, POWER_ATTR_BATTERY_PERCENTAGE_REMAINING)

	return cmds
}

def unlock() {
	def cmds = zigbee.command(CLUSTER_DOORLOCK, DOORLOCK_CMD_UNLOCK_DOOR) +
				zigbee.readAttribute(CLUSTER_POWER, POWER_ATTR_BATTERY_PERCENTAGE_REMAINING)
	return cmds
}

def parse(String description) {
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

private def parseAttributeResponse(String description) {
	Map descMap = zigbee.parseDescriptionAsMap(description)
	log.debug "Executing parseAttributeResponse() with description map:- $descMap"
	def result = []
	Map responseMap = [:]
	def clusterInt = descMap.clusterInt
	def attrInt = descMap.attrInt
	def deviceName = device.displayName
	responseMap.data = deviceName

	if (clusterInt == CLUSTER_POWER && attrInt == POWER_ATTR_BATTERY_PERCENTAGE_REMAINING) {
		responseMap.name = "battery"

		if (Integer.parseInt(descMap.value, 16) != 255) {
			responseMap.value = Math.round(Integer.parseInt(descMap.value, 16) / 2)
			responseMap.descriptionText = "Battery is at ${responseMap.value}%"
		}
		
	} else if (clusterInt == CLUSTER_DOORLOCK && attrInt == DOORLOCK_ATTR_LOCKSTATE) {
		def value = Integer.parseInt(descMap.value, 16)
		responseMap.name = "lock"
		if (value == 0) {
			responseMap.value = "unknown"
			responseMap.descriptionText = "Unknown state"
		} else if (value == 1) {
			log.debug "locked"
			responseMap.value = "locked"
			responseMap.descriptionText = "Locked"
		} else if (value == 2) {
			log.debug "unlocked"
			responseMap.value = "unlocked"
			responseMap.descriptionText = "Unlocked"
		} else {
			responseMap.value = "unknown"
			responseMap.descriptionText = "Unknown state"
		}
	} else {
		return null
	}
	result << createEvent(responseMap)
	return result
}

private def parseCommandResponse(String description) {
	Map descMap = zigbee.parseDescriptionAsMap(description)
	log.debug "Executing parseCommandResponse() with description map:- $descMap"

	def deviceName = device.displayName
	def result = []
	Map responseMap = [:]
	def data = descMap.data
	def cmd = descMap.commandInt
	def clusterInt = descMap.clusterInt
	responseMap.data = deviceName

	if (clusterInt == CLUSTER_DOORLOCK && (cmd == DOORLOCK_CMD_LOCK_DOOR || cmd == DOORLOCK_CMD_UNLOCK_DOOR)) {
		def cmdList = []
		cmdList << "delay 4200"
		cmdList << zigbee.readAttribute(CLUSTER_DOORLOCK, DOORLOCK_ATTR_LOCKSTATE).first()
		result << response(cmdList)
	} else if (clusterInt == CLUSTER_DOORLOCK && cmd == DOORLOCK_RESPONSE_OPERATION_EVENT) {
		def eventSource = Integer.parseInt(data[0], 16)
		def eventCode = Integer.parseInt(data[1], 16)

		responseMap.name = "lock"
		responseMap.displayed = true
		responseMap.isStateChange = true

		if (eventSource == 1) {
			responseMap.data = [method: "command"]
		} else if (eventSource == 2) {
			def desc = "manually"
			responseMap.data = [method: "manual"]
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
			default:
				break
		}
	} else if (clusterInt == CLUSTER_IAS_ZONE && descMap.attrInt == IAS_ATTR_ZONE_STATUS && descMap.value && isC2OLock()) {
		def zs = new ZoneStatus(zigbee.convertToInt(descMap.value, 16))
		//isBatterySet() == false -> battery is ok -> send value 50
		//isBatterySet() == true -> battery is low -> send value 5
		//metadata can receive 2 values: 5 or 50 for C2O lock
		responseMap = [ name: "battery", value: zs.isBatterySet() ? 5 : 50]
	}

	result << createEvent(responseMap)
	return result
}

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

private boolean isC2OLock() {
	device.getDataValue("model") == "E261-KR0B0Z0-HA"
}