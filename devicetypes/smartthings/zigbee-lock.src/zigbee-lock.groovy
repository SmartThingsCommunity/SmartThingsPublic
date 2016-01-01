/**
 *  ZigBee Lock
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
    definition (name: "ZigBee Lock", namespace: "smartthings", author: "SmartThings")
    {
        capability "Actuator"
        capability "Lock"
        capability "Refresh"
        capability "Sensor"
        capability "Battery"
        capability "Configuration"

        fingerprint profileId: "0104", inClusters: "0000,0001,0003,0004,0005,0009,0020,0101,0402,0B05,FDBD", outClusters: "000A,0019",
                        manufacturer: "Kwikset", model: "SMARTCODE_DEADBOLT_5", deviceJoinName: "Kwikset 5-Button Deadbolt"
        fingerprint profileId: "0104", inClusters: "0000,0001,0003,0004,0005,0009,0020,0101,0402,0B05,FDBD", outClusters: "000A,0019",
                        manufacturer: "Kwikset", model: "SMARTCODE_LEVER_5", deviceJoinName: "Kwikset 5-Button Lever"
        fingerprint profileId: "0104", inClusters: "0000,0001,0003,0004,0005,0009,0020,0101,0402,0B05,FDBD", outClusters: "000A,0019",
                        manufacturer: "Kwikset", model: "SMARTCODE_DEADBOLT_10", deviceJoinName: "Kwikset 10-Button Deadbolt"
        fingerprint profileId: "0104", inClusters: "0000,0001,0003,0004,0005,0009,0020,0101,0402,0B05,FDBD", outClusters: "000A,0019",
                        manufacturer: "Kwikset", model: "SMARTCODE_DEADBOLT_10T", deviceJoinName: "Kwikset 10-Button Touch Deadbolt"
        fingerprint profileId: "0104", inClusters: "0000,0001,0003,0009,000A,0101,0020", outClusters: "000A,0019",
                        manufacturer: "Yale", model: "YRL220 TS LL", deviceJoinName: "Yale Touch Screen Lever Lock"
        fingerprint profileId: "0104", inClusters: "0000,0001,0003,0009,000A,0101,0020", outClusters: "000A,0019",
                        manufacturer: "Yale", model: "YRD210 PB DB", deviceJoinName: "Yale Push Button Deadbolt Lock"
        fingerprint profileId: "0104", inClusters: "0000,0001,0003,0009,000A,0101,0020", outClusters: "000A,0019",
                        manufacturer: "Yale", model: "YRD220/240 TSDB", deviceJoinName: "Yale Touch Screen Deadbolt Lock"
        fingerprint profileId: "0104", inClusters: "0000,0001,0003,0009,000A,0101,0020", outClusters: "000A,0019",
                        manufacturer: "Yale", model: "YRL210 PB LL", deviceJoinName: "Yale Push Button Lever Lock"
    }

    tiles(scale: 2) {
		multiAttributeTile(name:"toggle", type:"generic", width:6, height:4){
			tileAttribute ("device.lock", key:"PRIMARY_CONTROL") {
				attributeState "locked", label:'locked', action:"lock.unlock", icon:"st.locks.lock.locked", backgroundColor:"#79b821", nextState:"unlocking"
				attributeState "unlocked", label:'unlocked', action:"lock.lock", icon:"st.locks.lock.unlocked", backgroundColor:"#ffffff", nextState:"locking"
				attributeState "unknown", label:"unknown", action:"lock.lock", icon:"st.locks.lock.unknown", backgroundColor:"#ffffff", nextState:"locking"
				attributeState "locking", label:'locking', icon:"st.locks.lock.locked", backgroundColor:"#79b821"
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
		standardTile("refresh", "device.lock", inactiveLabel:false, decoration:"flat", width:2, height:2) {
			state "default", label:'', action:"refresh.refresh", icon:"st.secondary.refresh"
		}

		main "toggle"
		details(["toggle", "lock", "unlock", "battery", "refresh"])
	}
}

// Globals
private getCLUSTER_POWER() { 0x0001 }
private getCLUSTER_DOORLOCK() { 0x0101 }

private getDOORLOCK_CMD_LOCK_DOOR() { 0x00 }
private getDOORLOCK_CMD_UNLOCK_DOOR() { 0x01 }
private getDOORLOCK_ATTR_LOCKSTATE() { 0x0000 }
private getPOWER_ATTR_BATTERY_PERCENTAGE_REMAINING() { 0x0021 }

private getTYPE_U8() { 0x20 }
private getTYPE_ENUM8() { 0x30 }

// Public methods
def installed() {
    log.trace "installed()"
}

def uninstalled() {
    log.trace "uninstalled()"
}

def configure() {
    /*
    def cmds =
        zigbee.configSetup("${CLUSTER_DOORLOCK}", "${DOORLOCK_ATTR_LOCKSTATE}",
                           "${TYPE_ENUM8}", 0, 3600, "{01}") +
        zigbee.configSetup("${CLUSTER_POWER}", "${POWER_ATTR_BATTERY_PERCENTAGE_REMAINING}",
                           "${TYPE_U8}", 600, 21600, "{01}")
    */
    def zigbeeId = device.zigbeeId
    def cmds =
        [
            "zdo bind 0x${device.deviceNetworkId} 0x${device.endpointId} 1 ${CLUSTER_DOORLOCK} {$zigbeeId} {}", "delay 200",
            "zcl global send-me-a-report ${CLUSTER_DOORLOCK} ${DOORLOCK_ATTR_LOCKSTATE} ${TYPE_ENUM8} 0 3600 {01}", "delay 200",
            "send 0x${device.deviceNetworkId} 1 0x${device.endpointId}", "delay 200",

            "zdo bind 0x${device.deviceNetworkId} 0x${device.endpointId} 1 ${CLUSTER_POWER} {$zigbeeId} {}", "delay 200",
            "zcl global send-me-a-report ${CLUSTER_POWER} ${POWER_ATTR_BATTERY_PERCENTAGE_REMAINING} ${TYPE_U8} 600 21600 {01}", "delay 200",
            "send 0x${device.deviceNetworkId} 1 0x${device.endpointId}", "delay 200",
        ]
    log.info "configure() --- cmds: $cmds"
    return cmds + refresh() // send refresh cmds as part of config
}

def refresh() {
    def cmds =
        zigbee.refreshData("${CLUSTER_DOORLOCK}", "${DOORLOCK_ATTR_LOCKSTATE}") +
        zigbee.refreshData("${CLUSTER_POWER}", "${POWER_ATTR_BATTERY_PERCENTAGE_REMAINING}")
    log.info "refresh() --- cmds: $cmds"
    return cmds
}

def parse(String description) {
    log.trace "parse() --- description: $description"

    Map map = [:]
    if (description?.startsWith('read attr -')) {
        map = parseReportAttributeMessage(description)
    }

    log.debug "parse() --- Parse returned $map"
    def result = map ? createEvent(map) : null
    return result
}

// Lock capability commands
def lock() {
    //def cmds = zigbee.zigbeeCommand("${CLUSTER_DOORLOCK}", "${DOORLOCK_CMD_LOCK_DOOR}", "{}")
    //log.info "lock() -- cmds: $cmds"
    //return cmds
    "st cmd 0x${device.deviceNetworkId} 0x${device.endpointId} ${CLUSTER_DOORLOCK} ${DOORLOCK_CMD_LOCK_DOOR} {}"
}

def unlock() {
    //def cmds = zigbee.zigbeeCommand("${CLUSTER_DOORLOCK}", "${DOORLOCK_CMD_UNLOCK_DOOR}", "{}")
    //log.info "unlock() -- cmds: $cmds"
    //return cmds
    "st cmd 0x${device.deviceNetworkId} 0x${device.endpointId} ${CLUSTER_DOORLOCK} ${DOORLOCK_CMD_UNLOCK_DOOR} {}"
}

// Private methods
private Map parseReportAttributeMessage(String description) {
    log.trace "parseReportAttributeMessage() --- description: $description"

    Map descMap = zigbee.parseDescriptionAsMap(description)

    log.debug "parseReportAttributeMessage() --- descMap: $descMap"

    Map resultMap = [:]
    if (descMap.clusterInt == CLUSTER_POWER && descMap.attrInt == POWER_ATTR_BATTERY_PERCENTAGE_REMAINING) {
        resultMap.name = "battery"
        resultMap.value = Math.round(Integer.parseInt(descMap.value, 16) / 2)
        if (device.getDataValue("manufacturer") == "Yale") {            //Handling issue with Yale locks incorrect battery reporting
            resultMap.value = Integer.parseInt(descMap.value, 16)
        }
        log.info "parseReportAttributeMessage() --- battery: ${resultMap.value}"
    }
    else if (descMap.clusterInt == CLUSTER_DOORLOCK && descMap.attrInt == DOORLOCK_ATTR_LOCKSTATE) {
        def value = Integer.parseInt(descMap.value, 16)
        resultMap.name = "lock"
        resultMap.putAll([0:["value":"unknown",
                             "descriptionText":"Not fully locked"],
                          1:["value":"locked"],
                          2:["value":"unlocked"]].get(value,
                                                      ["value":"unknown",
                                                       "descriptionText":"Unknown lock state"]))
        log.info "parseReportAttributeMessage() --- lock: ${resultMap.value}"
    }
    else {
        log.debug "parseReportAttributeMessage() --- ignoring attribute"
    }
    return resultMap
}
