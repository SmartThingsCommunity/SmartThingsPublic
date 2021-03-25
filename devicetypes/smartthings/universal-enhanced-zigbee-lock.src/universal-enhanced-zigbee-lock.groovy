/*
 *  Universal Enhanced ZigBee Lock
 *
 *  2017-01-21 : Modified ping() command to more align with other SmartThings ZigBee DTHs. Version 1.2a
 *  2017-01-20 : Added Health Check Capability and eventType: "ALERT" for tamper alerts. Version 1.2
 *  2017-01-08 : Add ZigBee DataType to align with SmartThings DTH changes. Version 1.1a
 *  2016-12-28 : Attribute Updates (true/false). Code Optimization.  Version 1.1
 *  2016-12-27 : Minor Changes.  Version 1.0 for submittal to SmartThings
 *
 *	This is a modification of work originally copyrighted by "SmartThings."	 All modifications to their work
 *	is released under the following terms:
 *
 *	The original licensing applies, with the following exceptions:
 *		1.	These modifications may NOT be used without freely distributing all these modifications freely
 *			and without limitation, in source form.	 The distribution may be met with a link to source code
 *			with these modifications.
 *		2.	These modifications may NOT be used, directly or indirectly, for the purpose of any type of
 *			monetary gain.	These modifications may not be used in a larger entity which is being sold,
 *			leased, or anything other than freely given.
 *		3.	To clarify 1 and 2 above, if you use these modifications, it must be a free project, and
 *			available to anyone with "no strings attached."	 (You may require a free registration on
 *			a free website or portal in order to distribute the modifications.)
 *		4.	The above listed exceptions to the original licensing do not apply to the holder of the
 *			copyright of the original work.	 The original copyright holder can use the modifications
 *			to hopefully improve their original work.  In that event, this author transfers all claim
 *			and ownership of the modifications to "SmartThings."
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
    definition (name: "Universal Enhanced ZigBee Lock", namespace: "smartthings", author: "jhamstead")
    {
        capability "Actuator"
        capability "Lock"
        capability "Refresh"
        capability "Sensor"
        capability "Battery"
        capability "Lock Codes"
        capability "Configuration"
        capability "Polling"
        capability "Tamper Alert"
        capability "Health Check"
        
        command "deleteAllCodes"
        command "enableAutolock"
        command "disableAutolock"
        command "enableOneTouch"
        command "disableOneTouch"
        command "enableKeypad"
        command "disableKeypad"
        command "enableAudio"
        command "enableAudioLow"
        command "enableAudioHigh"
        command "disableAudio"
        command "enablePrivacyButton"
        command "disablePrivacyButton"
        command "enableInternalLED"
        command "disableInternalLED"
        command "resetTamperAlert"
        
        attribute "wrongCodeEntryLimit", "number"
        attribute "userCodeDisableTime", "number"
        attribute "autoLockTime", "number"
        attribute "oneTouch", "enum", [true,false]
        attribute "keypad", "enum", [true,false]
        attribute "privacyButton", "enum", [true,false]
        attribute "LED", "enum", [true,false]
        attribute "volume", "enum", ["Silent","Low","High"]
        attribute "invalidCode", "enum", [true,false]
        attribute "numPINUsers", "number"
        //attribute "maxPINLength", "number"
        //attribute "minPINLength", "number"

        fingerprint profileId: "0104", inClusters: "0000,0001,0003,0004,0005,0009,0020,0101,0402,0B05,FDBD", outClusters: "000A,0019", manufacturer: "Kwikset", model: "SMARTCODE_DEADBOLT_5", deviceJoinName: "Kwikset 5-Button Deadbolt"
        fingerprint profileId: "0104", inClusters: "0000,0001,0003,0004,0005,0009,0020,0101,0402,0B05,FDBD", outClusters: "000A,0019", manufacturer: "Kwikset", model: "SMARTCODE_LEVER_5", deviceJoinName: "Kwikset 5-Button Lever"
        fingerprint profileId: "0104", inClusters: "0000,0001,0003,0004,0005,0009,0020,0101,0402,0B05,FDBD", outClusters: "000A,0019", manufacturer: "Kwikset", model: "SMARTCODE_DEADBOLT_10", deviceJoinName: "Kwikset 10-Button Deadbolt"
        fingerprint profileId: "0104", inClusters: "0000,0001,0003,0004,0005,0009,0020,0101,0402,0B05,FDBD", outClusters: "000A,0019", manufacturer: "Kwikset", model: "SMARTCODE_DEADBOLT_10T", deviceJoinName: "Kwikset 10-Button Touch Deadbolt"
        fingerprint profileId: "0104", inClusters: "0000,0001,0003,0009,000A,0101,0020", outClusters: "000A,0019", manufacturer: "Yale", model: "YRL220 TS LL", deviceJoinName: "Yale Touch Screen Lever Lock"
        fingerprint profileId: "0104", inClusters: "0000,0001,0003,0009,000A,0101,0020", outClusters: "000A,0019", manufacturer: "Yale", model: "YRD210 PB DB", deviceJoinName: "Yale Push Button Deadbolt Lock"
        fingerprint profileId: "0104", inClusters: "0000,0001,0003,0009,000A,0101,0020", outClusters: "000A,0019", manufacturer: "Yale", model: "YRD220/240 TSDB", deviceJoinName: "Yale Touch Screen Deadbolt Lock"
        fingerprint profileId: "0104", inClusters: "0000,0001,0003,0009,000A,0101,0020", outClusters: "000A,0019", manufacturer: "Yale", model: "YRL210 PB LL", deviceJoinName: "Yale Push Button Lever Lock"
    }

    tiles(scale: 2) {
		multiAttributeTile(name:"toggle", type:"generic", width:6, height:4){
			tileAttribute ("device.lock", key:"PRIMARY_CONTROL") {
				attributeState "locked", label:'locked', action:"lock.unlock", icon:"st.locks.lock.locked", backgroundColor:"#79b821", nextState:"unlocking"
				attributeState "unlocked", label:'unlocked', action:"lock.lock", icon:"st.locks.lock.unlocked", backgroundColor:"#ffffff", nextState:"locking"
                attributeState "unknown", label:"unknown", action:"lock.lock", icon:"st.locks.lock.unknown", backgroundColor:"#ffffff", nextState:"locking"
                attributeState "jammed", label:"jammed", action:"lock.unlock", icon:"st.locks.lock.unknown", backgroundColor:"#ee7070", nextState:"unlocking"
				attributeState "locking", label:'locking', icon:"st.locks.lock.locked", backgroundColor:"#79b821"
				attributeState "unlocking", label:'unlocking', icon:"st.locks.lock.unlocked", backgroundColor:"#ffffff"
			}
            tileAttribute ("device.tamper", key:"SECONDARY_CONTROL") {
                attributeState "clear", label:""
                attributeState "detected", label:"Alert!", icon:"st.alarm.alarm.alarm"         
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
        standardTile("volumeTile", "device.volumeTile", inactiveLabel:false, width:2, height:2) {
            state "unsupported", label:"Unsupported", icon:"st.custom.sonos.muted", backgroundColor:"#ffffff"
            state "Silent", label:"Silent", action:"enableAudioLow", icon:"st.custom.sonos.muted", nextState:"volumeSilentChanging", backgroundColor:"#ffffff"
            state "Low", label:"Low", action:"enableAudioHigh", icon:"st.custom.sonos.unmuted", nextState:"volumeLowChanging", backgroundColor:"#7070ee"
            state "High", label:"High", action:"disableAudio", icon:"st.custom.sonos.unmuted", nextState:"volumeHighChanging", backgroundColor:"#ee7070"
            state "volumeSilentChanging", label:'. . .', action:"refresh", icon:"st.custom.sonos.muted", backgroundColor:"#a0a0ff"
            state "volumeLowChanging", label:'. . .', action:"refresh", icon:"st.custom.sonos.unmuted", backgroundColor:"#a070a0"
            state "volumeHighChanging", label:'. . .', action:"refresh", icon:"st.custom.sonos.unmuted", backgroundColor:"#ffa0a0"
            state "unsupportedSilent", label:"Silent", icon:"st.custom.sonos.muted", backgroundColor:"#ffffff"
            state "unsupportedLow", label:"Low", icon:"st.custom.sonos.unmuted", backgroundColor:"#7070ee"
            state "unsupportedHigh", label:"High", icon:"st.custom.sonos.unmuted", backgroundColor:"#ee7070"
		}
        tileToggle("autoLockTime", "Timed Auto Lock Enabled", "Timed Auto Lock Disabled", "st.Health & Wellness.health7", "st.samsung.da.washer_ic_cancel", "enableAutolock", "disableAutolock")
        tileToggle("oneTouch", "One Touch Lock Enabled", "One Touch Lock Disabled", "st.security.alarm.off", "st.security.alarm.on", "enableOneTouch", "disableOneTouch")
        tileToggle("keypad", "Keypad Enabled", "Keypad Disabled", "st.unknown.zwave.remote-controller", "st.unknown.zwave.remote-controller", "enableKeypad", "disableKeypad", "`                    ")
        tileToggle("privacyButton", "Privacy Button Enabled", "Privacy Button Disabled", "st.custom.buttons.add-icon", "st.custom.buttons.subtract-icon", "enablePrivacyButton", "disablePrivacyButton")
        tileToggle("LED", "Internal LED Enabled", "Internal LED Disabled", "st.illuminance.illuminance.light", "st.illuminance.illuminance.dark", "enableInternalLED", "disableInternalLED")
		standardTile("tamper", "device.tamper", inactiveLabel:false, width:2, height:2) {
            state "clear", label:'No Alerts', icon:"st.nest.nest-leaf", backgroundColor:"#00dd00"
			state "detected", label:'Reset', action:"resetTamperAlert", icon:"st.alarm.alarm.alarm", backgroundColor:"#ff0000"
		}
		standardTile("reconfigure", "device.reconfigure", inactiveLabel:false, decoration:"flat", width:2, height:2) {
            state "reconfigure", label:'Force Reconfigure', action:"configure", icon:"st.secondary.tools"
		}
        valueTile("labelManual", "device.labelManual", inactiveLabel:true, decoration:"flat", width:6, height:2) {
            state "default", label: 'NOTE: If you change any of the options below manually outside of SmartThings, you must click refresh.  They will not update automatically.'
        }
        
		main "toggle"
		details(["toggle", "lock", "unlock", "battery", "tamper", "keypadTile", "volumeTile", "labelManual", "autoLockTimeTile", "oneTouchTile", "privacyButtonTile", "LEDTile", "refresh", "reconfigure"])
	}
    
	preferences {
        section ("Lock Properties"){
            input "autoLock", "number", title: "Auto Lock Timeout (5-180 Seconds)", description: true, defaultValue: 30, required: false, range: "5..180"
            input "wrongLimit", "number", title: "Wrong Code Entry Limit (1-10 Invalid Entries)", description: true, defaultValue: 5, required: false, range: "1..10"
            input "lockoutTime", "number", title: "Wrong Code Entry Lockout (5-180 Seconds)", description: true, defaultValue: 60, required: false, range: "5..180"
        }
	}
}
// Globals
private getCLUSTER_POWER() { 0x0001 }
private getCLUSTER_ALARM() { 0x0009 }
private getCLUSTER_DOORLOCK() { 0x0101 }

private getALARM_CMD_RESET_ALL() { 0x01 }
private getDOORLOCK_CMD_LOCK_DOOR() { 0x00 }
private getDOORLOCK_CMD_UNLOCK_DOOR() { 0x01 }
private getDOORLOCK_CMD_GET_LOG_RECORD() { 0x04 }
private getDOORLOCK_CMD_USER_CODE_SET() { 0x05 }
private getDOORLOCK_CMD_USER_CODE_GET() { 0x06 }
private getDOORLOCK_CMD_CLEAR_USER_CODE() { 0x07 }
private getDOORLOCK_CMD_CLEAR_ALL_USER_CODE() { 0x08 }
private getDOORLOCK_RESPONSE_OPERATION_EVENT() { 0x20 }
private getDOORLOCK_RESPONSE_PROGRAMMING_EVENT() { 0x21 }
private getPOWER_ATTR_BATTERY_PERCENTAGE_REMAINING() { 0x0021 }
private getALARM_COUNT() { 0x0000 }
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
private getDOORLOCK_ATTR_SEND_PIN_OTA() { 0x0032 }

// Public methods
def installed() {
    log.trace "installed()"
}

def uninstalled() {
    log.trace "uninstalled()"
}

def configure() {
    state.disableLocalPINStore = false
    state.updatedDate = Calendar.getInstance().getTimeInMillis()  //Workaround for repeated updated() calls
    def cmds =
        zigbee.configureReporting(CLUSTER_DOORLOCK, DOORLOCK_ATTR_LOCKSTATE,
                                  DataType.ENUM8, 0, 3600, null) +
        zigbee.configureReporting(CLUSTER_POWER, POWER_ATTR_BATTERY_PERCENTAGE_REMAINING,
                                  DataType.UINT8, 600, 21600, 0x01) +
        zigbee.configureReporting(CLUSTER_ALARM, ALARM_COUNT,
                                  DataType.UINT32, 0, 21600, null) +
        zigbee.configureReporting(CLUSTER_DOORLOCK, DOORLOCK_ATTR_OPERATING_MODE,
                                  DataType.ENUM8, 0, 21600, null) +
        zigbee.configureReporting(CLUSTER_DOORLOCK, DOORLOCK_ATTR_SOUND_VOLUME,
                                  DataType.UINT8, 0, 21600, null)

    // Device-Watch allows 2 check-in misses from device (lock state) + ping (plus 1 min lag time)
    sendEvent(name: "checkInterval", value: 2 * 60 * 60 + 1 * 60, displayed: false, data: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID])

    log.info "configure() --- cmds: $cmds"
    return refresh() + cmds // send refresh cmds as part of config
}

def refresh() {
    def cmds =
        zigbee.readAttribute(CLUSTER_DOORLOCK, DOORLOCK_ATTR_LOCKSTATE) +
        zigbee.readAttribute(CLUSTER_POWER, POWER_ATTR_BATTERY_PERCENTAGE_REMAINING) +
        zigbee.readAttribute(CLUSTER_DOORLOCK, DOORLOCK_ATTR_NUM_PIN_USERS) +
        zigbee.readAttribute(CLUSTER_DOORLOCK, DOORLOCK_ATTR_AUTO_RELOCK_TIME) +
        zigbee.readAttribute(CLUSTER_DOORLOCK, DOORLOCK_ATTR_ONE_TOUCH_LOCK) +
        zigbee.readAttribute(CLUSTER_DOORLOCK, DOORLOCK_ATTR_OPERATING_MODE) +
        zigbee.readAttribute(CLUSTER_DOORLOCK, DOORLOCK_ATTR_PRIVACY_BUTTON) +
        zigbee.readAttribute(CLUSTER_DOORLOCK, DOORLOCK_ATTR_LED_STATUS) +
        zigbee.readAttribute(CLUSTER_DOORLOCK, DOORLOCK_ATTR_SOUND_VOLUME) +
        zigbee.readAttribute(CLUSTER_DOORLOCK, DOORLOCK_ATTR_WRONG_CODE_ENTRY_LIMIT) +
        zigbee.readAttribute(CLUSTER_DOORLOCK, DOORLOCK_ATTR_USER_CODE_DISABLE_TIME)
        reportAllCodes()
    log.info "refresh() --- cmds: $cmds"
    return cmds
}

    /**
     * PING is used by Device-Watch in attempt to reach the Device
     * */
def ping() {
    log.debug "ping() called"
    return configure()
}

def updated() {
    def cmds = ""
    def cmd = ""
    def wrongEntryLimit = (settings.wrongLimit) ? settings.wrongLimit : 5
    def disableTime = (settings.lockoutTime) ? settings.lockoutTime : 60
    def myTime =( settings.autoLock && device.getDataValue("manufacturer") == "Kwikset" ) ? 30 : settings.autoLock
    if ( (Calendar.getInstance().getTimeInMillis() - state.updatedDate) < 7000 ) return // Needed because updated() is being called multiple times
    if ( device.currentValue("wrongCodeEntryLimit") != wrongEntryLimit && device.getDataValue("manufacturer") != "Kwikset" ) {
        cmd = zigbee.writeAttribute(CLUSTER_DOORLOCK, DOORLOCK_ATTR_WRONG_CODE_ENTRY_LIMIT, DataType.UINT8, zigbee.convertToHexString(wrongEntryLimit,2)) + 
              zigbee.readAttribute(CLUSTER_DOORLOCK, DOORLOCK_ATTR_WRONG_CODE_ENTRY_LIMIT) // Saves battery reporting change as can't be set manually
        cmds += cmd
        fireCommand(cmd)
    }
    if ( device.currentValue("userCodeDisableTime") != disableTime && device.getDataValue("manufacturer") != "Kwikset" ) {
        cmd = zigbee.writeAttribute(CLUSTER_DOORLOCK, DOORLOCK_ATTR_USER_CODE_DISABLE_TIME, DataType.UINT8, zigbee.convertToHexString(disableTime,2)) +
              zigbee.readAttribute(CLUSTER_DOORLOCK, DOORLOCK_ATTR_USER_CODE_DISABLE_TIME) // Saves battery reporting change as can't be set manually
        cmds += cmd
        fireCommand(cmd)
    }
    if ( device.currentValue("autoLockTime") != myTime && device.currentValue("autoLockTime") > 0 ) {
        cmd = zigbee.writeAttribute(CLUSTER_DOORLOCK, DOORLOCK_ATTR_AUTO_RELOCK_TIME, DataType.UINT32, zigbee.convertToHexString(myTime,8)) +
              zigbee.readAttribute(CLUSTER_DOORLOCK, DOORLOCK_ATTR_AUTO_RELOCK_TIME) // Saves battery reporting (Memory Restrictions)
        cmds += cmd
        fireCommand(cmd)
    }
    state.updatedDate = Calendar.getInstance().getTimeInMillis() //Part of workaround because updated() called multiple times
    log.info "updated() --- cmds: $cmds"
}

//Beginning of Custom Commands Section
def enableAutolock(myTime = settings.autoLock) {
    def cmds = ""
    if ( ! myTime ) myTime = 30
    if ( device.getDataValue("manufacturer") != "Kwikset" ) {
        cmds = zigbee.writeAttribute(CLUSTER_DOORLOCK, DOORLOCK_ATTR_AUTO_RELOCK_TIME, DataType.UINT32, zigbee.convertToHexString(myTime,8)) +
               zigbee.readAttribute(CLUSTER_DOORLOCK, DOORLOCK_ATTR_AUTO_RELOCK_TIME) //Needed because of configuration memory issues
    } else {
        log.warn "enableAutoLock() --- command not supported for this lock"
    }
    log.debug "enableAutoLock() --- cmds: $cmds"
    return cmds    
}

def disableAutolock() {
    def cmds = ""
    if ( device.getDataValue("manufacturer") != "Kwikset" ) {
        cmds = zigbee.writeAttribute(CLUSTER_DOORLOCK, DOORLOCK_ATTR_AUTO_RELOCK_TIME, DataType.UINT32, zigbee.convertToHexString(0,8)) +
               zigbee.readAttribute(CLUSTER_DOORLOCK, DOORLOCK_ATTR_AUTO_RELOCK_TIME) //Needed because of configuration memory issues
    } else {
        log.warn "disableAutoLock() --- command not supported for this lock"
    }
    log.debug "disableAutoLock() --- cmds: $cmds"
    return cmds    
}

def enableOneTouch() {
    def cmds = ""
    if ( device.getDataValue("manufacturer") != "Kwikset" ) {
        cmds = zigbee.writeAttribute(CLUSTER_DOORLOCK, DOORLOCK_ATTR_ONE_TOUCH_LOCK, DataType.BOOLEAN, 1) +
               zigbee.readAttribute(CLUSTER_DOORLOCK, DOORLOCK_ATTR_ONE_TOUCH_LOCK) //Needed because of configuration memory issues
    } else {
        log.warn "enableOneTouch() --- command not supported for this lock"
    }
    log.debug "enableOneTouch() --- cmds: $cmds"
    return cmds    
}

def disableOneTouch() {
    def cmds = ""
    if ( device.getDataValue("manufacturer") != "Kwikset" ) {
        cmds = zigbee.writeAttribute(CLUSTER_DOORLOCK, DOORLOCK_ATTR_ONE_TOUCH_LOCK, DataType.BOOLEAN, 0) +
               zigbee.readAttribute(CLUSTER_DOORLOCK, DOORLOCK_ATTR_ONE_TOUCH_LOCK) //Needed because of configuration memory issues
    } else {
        log.warn "disableOneTouch() --- command not supported for this lock"
    }
    log.debug "disableOneTouch() --- cmds: $cmds"
    return cmds    
}

def enablePrivacyButton() {
    def cmds = ""
    if ( device.getDataValue("manufacturer") != "Kwikset" ) {
        cmds = zigbee.writeAttribute(CLUSTER_DOORLOCK, DOORLOCK_ATTR_PRIVACY_BUTTON, DataType.BOOLEAN, 1) +
               zigbee.readAttribute(CLUSTER_DOORLOCK, DOORLOCK_ATTR_PRIVACY_BUTTON) //Needed because of configuration memory issues
    } else {
        log.warn "enablePrivacyButton() --- command not supported for this lock"
    }
    log.debug "enablePrivacyButton() --- cmds: $cmds"
    return cmds 
}

def disablePrivacyButton() {
    def cmds = ""
    if ( device.getDataValue("manufacturer") != "Kwikset" ) {
        cmds = zigbee.writeAttribute(CLUSTER_DOORLOCK, DOORLOCK_ATTR_PRIVACY_BUTTON, DataType.BOOLEAN, 0) +
               zigbee.readAttribute(CLUSTER_DOORLOCK, DOORLOCK_ATTR_PRIVACY_BUTTON) //Needed because of configuration memory issues
    } else {
        log.warn "disablePrivacyButton() --- command not supported for this lock"
    }
    log.debug "disablePrivacyButton() --- cmds: $cmds"
    return cmds  
}

def enableKeypad() {
    def cmds = ""
    if ( device.getDataValue("manufacturer") != "Kwikset" ) {
        cmds = zigbee.writeAttribute(CLUSTER_DOORLOCK, DOORLOCK_ATTR_OPERATING_MODE, DataType.ENUM8, zigbee.convertToHexString(0,2))
    } else {
        log.warn "enableKeypad() --- command not supported for this lock"
    }
    log.debug "enableKeypad() --- cmds: $cmds"
    return cmds 
}

def disableKeypad() {
    def cmds = ""
    if ( device.getDataValue("manufacturer") != "Kwikset" ) {
        cmds = zigbee.writeAttribute(CLUSTER_DOORLOCK, DOORLOCK_ATTR_OPERATING_MODE, DataType.ENUM8, zigbee.convertToHexString(2,2))
    } else {
        log.warn "disableKeypad() --- command not supported for this lock"
    }
    log.debug "disableKeypad() --- cmds: $cmds"
    return cmds 
}

def enableInternalLED() {
    def cmds = ""
    if ( device.getDataValue("manufacturer") != "Kwikset" ) {
        cmds = zigbee.writeAttribute(CLUSTER_DOORLOCK, DOORLOCK_ATTR_LED_STATUS, DataType.BOOLEAN, 1) +
               zigbee.readAttribute(CLUSTER_DOORLOCK, DOORLOCK_ATTR_LED_STATUS) // Needed because of configuration memory issues
    } else {
        log.warn "enableInternalLED() --- command not supported for this lock"
    }
    log.debug "enableInternalLED() --- cmds: $cmds"
    return cmds 
}

def disableInternalLED() {
    def cmds = ""
    if ( device.getDataValue("manufacturer") != "Kwikset" ) {
        cmds = zigbee.writeAttribute(CLUSTER_DOORLOCK, DOORLOCK_ATTR_LED_STATUS, DataType.BOOLEAN, 0) +
               zigbee.readAttribute(CLUSTER_DOORLOCK, DOORLOCK_ATTR_LED_STATUS) // Needed because of configuration memory issues
    } else {
        log.warn "disableInternalLED() --- command not supported for this lock"
    }
    log.debug "disableInternalLED() --- cmds: $cmds"
    return cmds 
}

def enableAudio(volume = "Low") {
    def cmds = ""
    def value = 1
    if ( volume.toString().equalsIgnoreCase( "High" ) ) value = 2
    if ( device.getDataValue("manufacturer") != "Kwikset" ) {
        cmds = zigbee.writeAttribute(CLUSTER_DOORLOCK, DOORLOCK_ATTR_SOUND_VOLUME, DataType.UINT8, value)
    } else {
        log.warn "enableAudio() --- command not supported for this lock"
    }
    log.debug "enableAudio() --- cmds: $cmds"
    return cmds    
}

def enableAudioLow() {
    return enableAudio()
}

def enableAudioHigh() {
    return enableAudio("High")
}

def disableAudio() {
    def cmds = ""
    if ( device.getDataValue("manufacturer") != "Kwikset" ) {
        cmds = zigbee.writeAttribute(CLUSTER_DOORLOCK, DOORLOCK_ATTR_SOUND_VOLUME, DataType.UINT8, 0)
    } else {
        log.warn "disableAudio() --- command not supported for this lock"
    }
    log.debug "enableAudio() --- cmds: $cmds"
    return cmds    
}

// Parse Zigbee Responses
def parse(String description) {

    log.trace "parse() --- description: $description"
    Map map = [:]
    if (description?.startsWith('read attr -')) {
        map = parseReportAttributeMessage(description)
    }else {
        map = parseResponseMessage(description)
    }
    def result = map ? createEvent(map) : null
    
    log.debug "parse() --- returned: $result"
    
    return result
}

// Polling capability commands
def poll() {
    reportAllCodes()
}

// Lock capability commands
def lock() {
    def cmds = zigbee.command(CLUSTER_DOORLOCK, DOORLOCK_CMD_LOCK_DOOR)
    log.info "lock() -- cmds: $cmds"
    return cmds
}

def unlock() {
    def cmds = zigbee.command(CLUSTER_DOORLOCK, DOORLOCK_CMD_UNLOCK_DOOR)
    log.info "unlock() -- cmds: $cmds"
    return cmds
}

// Additional Command For Tamper Alert
def resetTamperAlert() {
    def resultMap = [:]
    def cmds = ""
    if ( device.currentValue("tamper") == "detected" ) {
        resultMap = [ name: "tamper", descriptionText: "Tamper Alert Acknowledged", isStateChange: true, displayed: true, value: "cleared" ]
    }
    log.debug "resetTamperAlert() --- ${resultMap}"
    sendEvent(resultMap)
    if ( device.currentValue("invalidCode") ) sendEvent([ name: "invalidCode", isStateChange: true, displayed: false, value: false])
    cmds = zigbee.command( CLUSTER_ALARM, ALARM_CMD_RESET_ALL )
    log.info "resetTamperAlert() -- cmds: $cmds"
    return cmds
}

// Lock Code capability commands
def setCode(codeNumber, code) {
    def octetCode = ""
    def cmds = ""
    if (code.toString().size() <= getMaxPINLength() && code.toString().size() >= getMinPINLength() && code.isNumber() && codeNumber.toInteger() >= 1 && codeNumber.toInteger() <= getNumPINUsers() ){
         log.debug "Setting code $codeNumber to $code"
         code.toString().split('').each {
            if(it.trim()) octetCode += " 3${it}"
         }
        if ( device.getDataValue("manufacturer") == "Yale" ) {
           cmds = zigbee.command(CLUSTER_DOORLOCK, DOORLOCK_CMD_USER_CODE_SET, "${zigbee.convertToHexString(codeNumber.toInteger(),2)}00 01 00 0${code.toString().size()}${octetCode}") +
                  zigbee.command(CLUSTER_DOORLOCK, DOORLOCK_CMD_GET_LOG_RECORD, "0000") // Need to request response from Yale if code set as a programming event is not sent
        } else {
           cmds = zigbee.command(CLUSTER_DOORLOCK, DOORLOCK_CMD_USER_CODE_SET, "${zigbee.convertToHexString(codeNumber.toInteger(),2)}00 01 00 0${code.toString().size()}${octetCode}")
        }
        state["code${codeNumber}"] = encrypt(code)
    }else{
        log.debug "Invalid Input: Unable to set code $codeNumber to $code"
    }
    log.info "setCode() - ${cmds}"
    return cmds
}

def requestCode(codeNumber) {
    if (state["code${codeNumber}"]) {
        def resultMap = [ name: "codeReport", descriptionText: "Code recovered for user ${codeNumber}", isStateChange: true,
                          displayed: true, value: codeNumber, date: [ code: decrypt(state["code${codeNumber}"]) ] ]
        log.debug "requestCode: Code recovered for user $codeNumber: ${decrypt(state["code${codeNumber}"])}"
        sendEvent(resultMap)
    } else {
        log.debug "requestCode: Code not available user $codeNumber"
    }
}

def deleteCode(codeNumber) {
    def cmds = ""
    if (codeNumber.toInteger() >= 0 && codeNumber.toInteger() <= getNumPINUsers() ){
	    log.debug "Deleting code $codeNumber"
        cmds = zigbee.command(CLUSTER_DOORLOCK, DOORLOCK_CMD_CLEAR_USER_CODE, "${zigbee.convertToHexString(codeNumber.toInteger(),2)}00")
    }else{
        log.debug "Invalid Input: Unable to delete code for $codeNumber"
    }
    log.info "deleteCode() - ${cmds}"
    return cmds
}

def reloadAllCodes() {
    def cmds = ""
    def cmd = ""
    state.each { entry ->
        def codeNumber = entry.key.find(/\d+/)
        if ( entry.key ==~ /^code(\d+)$/ && entry.value ) {
            log.debug "Reloading Code for User ${codeNumber}"
            cmds = setCode(codeNumber, decrypt(entry.value))
            cmds += cmd
            fireCommand(cmd)
        }
    }
    log.info "reloadAllCodes() - ${cmds}"
}

def deleteAllCodes() {
    def cmds = ""
    cmds = zigbee.command(CLUSTER_DOORLOCK, DOORLOCK_CMD_CLEAR_ALL_USER_CODE)
    log.info "deleteAllCodes() - ${cmds}"
    return cmds
}

def updateCodes(codeSettings) {
    def cmds = ""
    def cmd = ""
	if(codeSettings instanceof String) codeSettings = util.parseJson(codeSettings)
	codeSettings.each { name, updated ->
		if (name ==~ /^code\d+$/) {
            def current = decrypt(state[name])
			def n = (name =~ /^code(\d+)$/)[0][1].toInteger()
            log.debug "updateCodes() - $name was $current, setting to $updated"
			if (updated.size() >= getMinPINLength() && updated.size() <= getMaxPINLength() && updated != current) {
                cmd = setCode(n, updated)
                cmds += cmd
                fireCommand(cmd)
			} else if ( (!updated || updated == "0") && current != "" ) {
				cmd = deleteCode(n)
                cmds += cmd
                fireCommand(cmd)
			} else if ( updated.size() < getMinPINLength() || updated.size() > getMaxPINLength() ) {
                log.warn("updateCodes() - Invalid PIN length $name: $updated") 
            } else if ( updated == current ) {
                log.debug("updateCodes() - PIN unchanged for $name: $updated") 
            } else log.warn("updateCodes() - unexpected PIN for $name: $updated") 
		} else log.warn("updateCodes() - unexpected entry code name: $name")
	}
    log.info "updateCodes() - ${cmds}"
}

// Private methods
private fireCommand(List commands) { //Function used from SmartThings Lightify Dimmer Switch support by Adam Outler
    if (commands != null && commands.size() > 0) {
        log.trace("Executing commands:" + commands)
        for (String value : commands){
            sendHubCommand([value].collect {new physicalgraph.device.HubAction(it)})
        }
    }
}

// provides compatibility with Erik Thayer's "Lock Code Manager"
private reportAllCodes() { //from garyd9's lock DTH
    def resultMap = [ name: "reportAllCodes", data: [:], displayed: false, isStateChange: false, type: "physical" ]
    state.each { entry ->
        //iterate through all the state entries and add them to the event data to be handled by application event handlers
        if ( entry.value && entry.key ==~ /^code\d+$/) {
		    resultMap.data.put(entry.key, decrypt(entry.value))
        } else if ( entry.key ==~ /^code\d+$/ ) {
            resultMap.data.put(entry.key, entry.value)
        }
    }
    sendEvent(resultMap)
}

private getMaxPINLength() {
    def max_length = 8
    if ( device.currentValue("maxPINLength") ) max_length = device.currentValue("maxPINLength")
    return max_length
}

private getMinPINLength() {
    def min_length = 4
    if ( device.currentValue("minPINLength") ) min_length = device.currentValue("minPINLength")
    return min_length
}

private getNumPINUsers() {
    def num_users = 30
    if ( device.currentValue("numPINUsers") ) num_users = device.currentValue("numPINUsers")
    return num_users
}

// This method creates a boiler plate standardTile configuration for On/Off Attributes
private tileToggle(varName, labelEnabled, labelDisabled, iconEnabled, iconDisabled, enableMethod, disableMethod, specialAlign="") {
        standardTile("${varName}Tile", "device.${varName}Tile", inactiveLabel:false, decoration:"flat", width:2, height:2) {
            state "unsupported", label:"${specialAlign}Unsupported", icon:"${iconDisabled}"
            state "${varName}Disabled", label:"${specialAlign}${labelDisabled}", action:"${enableMethod}", icon:"${iconDisabled}", nextState:"${varName}DChanging"
            state "${varName}Enabled", label:"${specialAlign}${labelEnabled}", action:"${disableMethod}", icon:"${iconEnabled}", nextState:"${varName}EChanging"
            state "${varName}DChanging", label:"${specialAlign}Updating . . .", icon:"${iconDisabled}"
            state "${varName}EChanging", label:"${specialAlign}Updating . . .", icon:"${iconEnabled}"
            state "unsupported${varName.capitalize()}Disabled", label:"${specialAlign}${labelDisabled}", icon:"${iconDisabled}"
            state "unsupported${varName.capitalize()}Enabled", label:"${specialAlign}${labelEnabled}", icon:"${iconEnabled}"
		}
}

// This method sets the varName attribute value and the correct state for toggled tiles
// isTrueFalse=true to set varName attribute values to true/false
// isReversed=true when value of 0 (zero) means Enabled
private Map parseTileToggle(varName, intValue, isTrueFalse=true, isReversed=false) {
    def value = intValue
    def lockCurrentValue = device.currentValue(varName)
    if (isTrueFalse) {
        if (isReversed) {
            value = (intValue == 0) ? true : false
        } else {
            value = (intValue == 0) ? false : true
        }
        lockCurrentValue = (lockCurrentValue == "true") ? true : false
    }
    def varMap = [name: varName, displayed: false, isStateChange: true, value: value ]
    def resultMap = [name: "${varName}Tile", isStateChange: true, displayed: true, descriptionText: "Current Value of ${varName}=${value}"  ]
    if ( lockCurrentValue == value ) {
        resultMap.displayed = false
        varMap.isStateChange = false
    }
    if ( (intValue > 0 && ! isReversed) || (isReversed && intValue == 0) ) {
        resultMap.value = (device.getDataValue("manufacturer") == "Kwikset") ? "unsupported" + varName.capitalize() + "Enabled" : "${varName}Enabled"
    } else {
        resultMap.value = (device.getDataValue("manufacturer") == "Kwikset") ? "unsupported" + varName.capitalize() + "Disabled" : "${varName}Disabled"
    }
    sendEvent(varMap)
    log.debug "parseToggle() - ${resultMap}"
    return resultMap
}

private Map parseReportAttributeMessage(String description) {
    Map descMap = zigbee.parseDescriptionAsMap(description)
    Map resultMap = [:]
    if (descMap.clusterInt == CLUSTER_POWER && descMap.attrInt == POWER_ATTR_BATTERY_PERCENTAGE_REMAINING) {
        resultMap.name = "battery"
        resultMap.value = Math.round(Integer.parseInt(descMap.value, 16) / 2)
        if (device.getDataValue("manufacturer") == "Yale") {            //Handling issue with Yale locks incorrect battery reporting
            resultMap.value = Integer.parseInt(descMap.value, 16)
        }
    } else if (descMap.clusterInt == CLUSTER_DOORLOCK && descMap.attrInt == DOORLOCK_ATTR_LOCKSTATE) {
        def value = Integer.parseInt(descMap.value, 16)
        def linkText = getLinkText(device)
        resultMap.name = "lock"
        if (value == 0) {
            resultMap.isStateChange = ( device.currentValue("lock") == "unknown" ) ? false : true
            resultMap += [ value: "unknown", descriptionText: "${linkText} is not fully locked", displayed: true ]
        } else if (value == 1) {
            resultMap.isStateChange = ( device.currentValue("lock") == "locked" ) ? false : true
            resultMap += [ value: "locked", descriptionText: "${linkText} is locked", displayed: false ]
        } else if (value == 2) {
            resultMap.isStateChange = ( device.currentValue("lock") == "unlocked" ) ? false : true
            resultMap += [ value: "unlocked", descriptionText: "${linkText} is unlocked", displayed: false ]
        } else {
            resultMap.isStateChange = ( device.currentValue("lock") == "unknown" ) ? false : true
            resultMap += [ value: "unknown", descriptionText: "${linkText} is in an unknown state", displayed: true ]
        }
    } else if (descMap.clusterInt == CLUSTER_DOORLOCK && descMap.attrInt == DOORLOCK_ATTR_MIN_PIN_LENGTH && descMap.value) {
        def value = Integer.parseInt(descMap.value, 16)
        resultMap = [ name: "minPINLength", descriptionText: "Minimum PIN length: ${value}", value: value ]
    } else if (descMap.clusterInt == CLUSTER_DOORLOCK && descMap.attrInt == DOORLOCK_ATTR_MAX_PIN_LENGTH && descMap.value) {
        def value = Integer.parseInt(descMap.value, 16)
        resultMap = [ name: "maxPINLength", descriptionText: "Maximum PIN length: ${value}", value: value ]
    } else if (descMap.clusterInt == CLUSTER_DOORLOCK && descMap.attrInt == DOORLOCK_ATTR_NUM_PIN_USERS && descMap.value) {
        def value = Integer.parseInt(descMap.value, 16)
        resultMap = [ name: "numPINUsers", descriptionText: "Maximum Number of PIN Users: ${value}", value: value ]
    } else if (descMap.clusterInt == CLUSTER_DOORLOCK && descMap.attrInt == DOORLOCK_ATTR_AUTO_RELOCK_TIME && descMap.value) {
        resultMap = parseTileToggle("autoLockTime", Integer.parseInt(descMap.value, 16), false) // is not True/False
    } else if (descMap.clusterInt == CLUSTER_DOORLOCK && descMap.attrInt == DOORLOCK_ATTR_ONE_TOUCH_LOCK && descMap.value) {
        resultMap = parseTileToggle("oneTouch", Integer.parseInt(descMap.value, 16))
    } else if (descMap.clusterInt == CLUSTER_DOORLOCK && descMap.attrInt == DOORLOCK_ATTR_OPERATING_MODE && descMap.value) {
        resultMap = parseTileToggle("keypad", Integer.parseInt(descMap.value, 16), true, true) // isReversed (0 = enabled)
    } else if (descMap.clusterInt == CLUSTER_DOORLOCK && descMap.attrInt == DOORLOCK_ATTR_PRIVACY_BUTTON && descMap.value) {
        resultMap = parseTileToggle("privacyButton", Integer.parseInt(descMap.value, 16))
    } else if (descMap.clusterInt == CLUSTER_DOORLOCK && descMap.attrInt == DOORLOCK_ATTR_LED_STATUS && descMap.value) {
        resultMap = parseTileToggle("LED", Integer.parseInt(descMap.value, 16))
    } else if (descMap.clusterInt == CLUSTER_DOORLOCK && descMap.attrInt == DOORLOCK_ATTR_WRONG_CODE_ENTRY_LIMIT && descMap.value) {
        def value = Integer.parseInt(descMap.value, 16)
        resultMap = [name: "wrongCodeEntryLimit", descriptionText: "Current Value of wrongCodeEntryLimit=${value}", isStateChange: true, value: value ]
        if ( device.currentValue("wrongCodeEntryLimit") == value ) resultMap.isStateChange = false
    } else if (descMap.clusterInt == CLUSTER_DOORLOCK && descMap.attrInt == DOORLOCK_ATTR_USER_CODE_DISABLE_TIME && descMap.value) {
        def value = Integer.parseInt(descMap.value, 16)
        resultMap = [name: "userCodeDisableTime", descriptionText: "Current Value of userCodeDisableTime=${value}", isStateChange: true, value: value ]
        if ( device.currentValue("userCodeDisableTime") == value ){ resultMap += [ isStateChange: false, displayed: false ] }
    } else if (descMap.clusterInt == CLUSTER_DOORLOCK && descMap.attrInt == DOORLOCK_ATTR_SOUND_VOLUME && descMap.value) {
        def intValue = Integer.parseInt(descMap.value, 16)
        def value
        if ( intValue == 1 ) {
            value = "Low"
        } else if ( intValue == 2 ){
            value = "High"
        } else {
            value = "Silent"
        }
        def varMap = [name: "volume", displayed: false, isStateChange: true, value: value ]
        resultMap = [name: "volumeTile", displayed: true, isStateChange: true, descriptionText: "Current Value of volume=${value}"]
        resultMap += (device.getDataValue("manufacturer") == "Kwikset") ? [ value: "unsuported${value}" ] : [ value: value ]
        if ( device.currentValue("volume") == value ) {
            resultMap.displayed = false
            varMap.isStateChange = false
        }
        sendEvent(varMap)
    } else {
        log.debug "parseReportAttributeMessage() --- ignoring attribute - ${description}"
    }
    return resultMap
}

private Map parseResponseMessage(String description) {
    Map descMap = zigbee.parseDescriptionAsMap(description)
    Map resultMap = [:]
    def linkText = getLinkText(device)
    def cmd = Integer.parseInt(descMap.command,16)
    if (descMap.clusterInt == CLUSTER_DOORLOCK && cmd == DOORLOCK_RESPONSE_OPERATION_EVENT) {
        def value = Integer.parseInt(descMap.data[0], 16)
        def user = Integer.parseInt(descMap.data[2], 16)
        def type
        resultMap.name = "lock"
        if (value == 0){
            type = ( user == 255 ) ? "locally" : "locally by user ${user}"
            resultMap.data = [ usedCode: user, type: "keypad" ]
        } else if (value == 1){
            type = ( user == 255 ) ? "remotely by SmartThings" : "remotely by user ${user}"
            resultMap.data = [ usedCode: user, type: "remotely" ]
        } else if (value == 2){
            type = ( user == 255 ) ? "manually" : "manually by user ${user}"
            resultMap.data =  [ usedCode: user, type: "manually" ]
        } else {
            log.info "Operation Event -- ignored"
            return
        }
        switch (Integer.parseInt(descMap.data[1], 16)) {
            case 1:
                resultMap += [ descriptionText: "${linkText} locked ${type}", value: "locked" ]
				break
            case 2:
                resultMap += [ descriptionText: "${linkText} unlocked ${type}", value: "unlocked" ]
				break
            case 3: //Lock Failure Invalid Pin
            case 4: //Lock Failure Invalid Schedule
                resultMap.name = "operationEvent"
                resultMap.descriptionText = "Invalid PIN entered ${type}"
                break
            case 5: //Unlock Invalid PIN
            case 6: //Unlock Invalid Schedule
                resultMap.name = "operationEvent"
                resultMap.descriptionText = "Invalid PIN entered ${type}"
				break
            case 7:
                resultMap += [ descriptionText: "${linkText} locked ${type} from keypad",  data: [ usedCode: user, type: "keypad" ], value: "locked" ]
				break
            case 8:
                resultMap += [ descriptionText: "${linkText} locked ${type} with key",  data: [ usedCode: user, type: "key" ], value: "locked" ]
				break
            case 9:
                resultMap += [ descriptionText: "${linkText} unlocked ${type} with key",  data: [ usedCode: user, type: "key" ], value: "unlocked" ]
				break
            case 10:
                resultMap += [ descriptionText: "${linkText} locked automatically", value: "locked" ]
				break
            case 13:
                resultMap += [ descriptionText: "${linkText} locked ${type}", value: "locked" ]
				break
            case 14:
                resultMap += [ descriptionText: "${linkText} unlocked ${type}", value: "unlocked" ]
				break
        }
        resultMap.displayed = true
        resultMap.isStateChange = true
    } else if (descMap.clusterInt == CLUSTER_DOORLOCK && cmd == DOORLOCK_RESPONSE_PROGRAMMING_EVENT) {
        def value = Integer.parseInt(descMap.data[0], 16)
        def type = ""
        if (value == 0){
            type = "locally"
        } else if (value == 1){
            type = "remotely"
        } else {
            log.info "Programming Event -- ignored"
            return
        }
        def codeNumber = Integer.parseInt(descMap.data[2], 16)
        resultMap.isStateChange = true
        resultMap.name="codeReport"
        resultMap.displayed = true
        switch (Integer.parseInt(descMap.data[1], 16)) {
            case 1: 
                resultMap.descriptionText = "Master code changed ${type}"
                resultMap.value = 0
				break
            case 2:
                resultMap.descriptionText = "User ${codeNumber}'s PIN code added ${type}"
                resultMap.value = codeNumber
                if ( type == "locally" ) { // Reports when done from keypad (cannot get code so setting to empty string)
                    resultMap.data = [ code: "" ]
                    state["code${codeNumber}"] = ""
                } else {
                    resultMap.data = [ code: decrypt(state["code${codeNumber}"]) ]
                }
				break
            case 3:
                if ( codeNumber == 255 ) {
                    resultMap.descriptionText = "All PIN Codes Deleted"
                    resultMap.value = ""
                    resultMap.data = [ code: "" ]
                    state.each { entry ->
                        //iterate through all the state entries to delete them
                        if ( entry.key ==~ /^code\d+$/ ) {
                            state[entry.key] = ""
                            sendEvent([ name: "codeReport", displayed: false, isStateChange: true, descriptionText: "User ${codeNumber}'s PIN code deleted ${type}",
                                        value: entry.key.substring(4), data: [ code: "" ] ])
                        }
                    }             
                } else {
                    resultMap.descriptionText = "User ${codeNumber}'s PIN code deleted ${type}"
                    if ( resultMap.value == codeNumber && resultMap.data["code"] == "" ){
                    resultMap.isStateChange = false
                    }
                    resultMap.value = codeNumber
                    resultMap.data = [ code: "" ]
                    state["code${codeNumber}"] = ""
                }
				break
            case 4:
                resultMap.descriptionText = "User ${codeNumber}'s PIN code changed ${type}"
                resultMap.value = codeNumber
                if ( type == "locally" ) { // Reports when done from keypad (cannot get code so setting to empty string)
                    resultMap.data = [ code: "" ]
                    state["code${codeNumber}"] = ""
                } else {
                    resultMap.data = [ code: decrypt(state["code${codeNumber}"]) ]
                }
				break
            default:
                return
				break
        }
    } else if (descMap.clusterInt == CLUSTER_DOORLOCK && cmd == DOORLOCK_CMD_GET_LOG_RECORD && descMap.data[6]) {
        if (Integer.parseInt(descMap.data[6], 16) == 1 && device.getDataValue("manufacturer") == "Yale") { //Needed because tested Yale lock does not send Programming Event
            def codeNumber = Integer.parseInt(descMap.data[9], 16)
            resultMap.name="codeReport"
            resultMap.descriptionText = "User ${codeNumber}'s PIN code added remotely"
            if ( resultMap.value == codeNumber && resultMap.data["code"] == decrypt(state["code${codeNumber}"]) ){
                resultMap.isStateChange = false
            } else {
                resultMap.isStateChange = true
            }
            resultMap.displayed = true
            resultMap.value = codeNumber
            resultMap.data = [ code: decrypt(state["code${codeNumber}"]) ]
        }
    } else if (descMap.clusterInt == CLUSTER_ALARM && cmd == ALARM_COUNT) {
        def value = Integer.parseInt(descMap.data[0], 16)
        def alarm_cluster = "${descMap.data[1]}${descMap.data[2]}"
        log.debug "Alarm Triggered By Cluster (${alarm_cluster}): ${value}"
        resultMap = [ name: "tamper", displayed: true, value: "detected", eventType: "ALERT" ]
        if (value == 0) {
           resultMap.descriptionText = "ALERT: ${linkText} deadbolt jammed"  
           sendEvent([ name: "lock", isStateChange: true, displayed: false, descriptionText: "${linkText} is jammed", value: "jammed" ])
        } else if (value == 1) {
            resultMap.descriptionText = "ALERT: ${linkText} reset to factory defaults"
        } else if (value == 4) {
            resultMap.descriptionText = "ALERT: ${linkText} wrong entry limit reached"
            sendEvent([ name: "invalidCode", isStateChange: true, displayed: false, value: true])
        } else if (value == 5) {
            resultMap.descriptionText = "ALERT: ${linkText} front panel removed"
        } else if (value == 6) {
            resultMap.descriptionText = "ALERT: ${linkText} forced open"
        }
    } else {
        log.debug "parseResponseMessage() --- ignoring response - ${description}"
    }
    return resultMap
}