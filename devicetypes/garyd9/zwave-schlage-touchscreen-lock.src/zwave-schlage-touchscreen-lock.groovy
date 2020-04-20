/**
 *
 *	INSTRUCTIONS:  If you scroll down a couple pages, you should find a line that looks like:
 *			main "toggle"
 *		and that followed by a line that STARTS with:
 *			details(["toggle",
 *		If you want to change the items that are available on the details page of the device (from 'things'),
 *		you should edit the "details" line to include whatever items you want to see (along with the order
 *		you want to see them in.)  There's a sample "details" line commented out (starts with //) below the
 *		first one.	That sample enables all (or mostly all) the possible items.
 *
 *		After the first time you have the device type installed (or after you've changed it), you might have
 *		to forcibly terminate the mobile app before the new/changed stuff will show up.	 (Most mobile apps
 *		don't actually terminate when you exit them.  How to terminate an app depends on your mobile OS.)
 *
 *		If a toggle is showing up as "loading..." on the UI (and you haven't recently changed it), tap the
 *		tile and it should reload the status within 10 seconds.
 *	2015-08-21 : refactor everything to bring in most of the updates from ST's base z-wave lock type.  Ensure
 *		its compatible with Erik Thayer's "lock code manager" smart app. (https://community.smartthings.com/t/lock-code-manager/12280)
 *	2015-03-07 : When the lock is locked/unlocked automatically, from the keypad, or manually, include that
 *		information in the map.data, usedCode.	(0 for keypad, "manual" for manually, and "auto" for automatic)
 *	2015-02-02 : changed state values to prevent UI confusion.	(Previously, when setting one item to 'unknown',
 *		the UI might show ALL the items as 'unknown'.)	Also added beeper toggle.
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
 *	Original Copyright information:
 *
 *	Copyright 2014 SmartThings
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

metadata 
{
	// Automatically generated. Make future change here.
	definition (name: "Z-Wave Schlage Touchscreen Lock", namespace: "garyd9", author: "Gary D") 
    {
		capability "Actuator"
		capability "Lock"
		capability "Polling"
		capability "Refresh"
		capability "Sensor"
		capability "Lock Codes"
		capability "Battery"

		attribute	"beeperMode", "string"
		attribute	"vacationMode", "string"	// "on", "off", "unknown"
		attribute	"lockLeave", "string"		// "on", "off", "unknown"
		attribute	"alarmMode", "string"		// "unknown", "Off", "Alert", "Tamper", "Kick"
		attribute	"alarmSensitivity", "number"	// 0 is unknown, otherwise 1-5 scaled to 1-99
		attribute	"localControl", "string"	// "on", "off", "unknown"
		attribute	"autoLock", "string"	// "on", "off", "unknown"
		attribute	"pinLength", "number"

		command "unlockwtimeout"

		command "setBeeperMode"
		command "setVacationMode"
		command "setLockLeave"
		command "setAlarmMode"
		command "setAlarmSensitivity"
		command "setLocalControl"
		command	"setAutoLock"
		command	"setPinLength"

		fingerprint deviceId: "0x4003", inClusters: "0x98"
		fingerprint deviceId: "0x4004", inClusters: "0x98"
	}

	simulator {
		status "locked": "command: 9881, payload: 00 62 03 FF 00 00 FE FE"
		status "unlocked": "command: 9881, payload: 00 62 03 00 00 00 FE FE"

		reply "9881006201FF,delay 4200,9881006202": "command: 9881, payload: 00 62 03 FF 00 00 FE FE"
		reply "988100620100,delay 4200,9881006202": "command: 9881, payload: 00 62 03 00 00 00 FE FE"
	}

	tiles {
		standardTile("toggle", "device.lock", width: 2, height: 2)
		{
			state "locked", label:'locked', action:"lock.unlock", icon:"st.locks.lock.locked", backgroundColor:"#79b821", nextState:"unlocking"
			state "unlocked", label:'unlocked', action:"lock.lock", icon:"st.locks.lock.unlocked", backgroundColor:"#ffffff", nextState:"locking"
			state "unknown", label:"unknown", action:"lock.lock", icon:"st.locks.lock.unknown", backgroundColor:"#ffffff", nextState:"locking"
			state "locking", label:'locking', icon:"st.locks.lock.locked", backgroundColor:"#79b821"
			state "unlocking", label:'unlocking', icon:"st.locks.lock.unlocked", backgroundColor:"#ffffff"
		}
		standardTile("lock", "device.lock", inactiveLabel: false, decoration: "flat")
		{
			state "default", label:'lock', action:"lock.lock", icon:"st.locks.lock.locked", nextState:"locking"
		}
		standardTile("unlock", "device.lock", inactiveLabel: false, decoration: "flat")
		{
			state "default", label:'unlock', action:"lock.unlock", icon:"st.locks.lock.unlocked", nextState:"unlocking"
		}
		valueTile("battery", "device.battery", inactiveLabel: false, decoration: "flat")
		{
			state "battery", label:'${currentValue}% battery', unit:""
		}
		standardTile("refresh", "device.lock", inactiveLabel: false, decoration: "flat")
		{
			state "default", label:'   ', action:"refresh.refresh", icon:"st.secondary.refresh"
		}
		standardTile("alarmMode", "device.alarmMode", inactiveLabel: true, canChangeIcon: false)
		{
			state "unknown_alarmMode", label: 'Alarm Mode\nLoading...', icon:"st.unknown.unknown.unknown", action:"setAlarmMode", nextState:"unknown_alarmMode"
			state "Off_alarmMode", label: 'Alarm: Off', icon:"st.alarm.beep.beep", action:"setAlarmMode", nextState:"unknown_alarmMode"
			state "Alert_alarmMode", label: 'Alert Alarm', icon:"st.alarm.beep.beep", action:"setAlarmMode", backgroundColor:"#79b821", nextState:"unknown_alarmMode"
			state "Tamper_alarmMode", label: 'Tamper Alarm', icon:"st.alarm.beep.beep", action:"setAlarmMode", backgroundColor:"#79b821", nextState:"unknown_alarmMode"
			state "Kick_alarmMode", label: 'Kick Alarm', icon:"st.alarm.beep.beep", action:"setAlarmMode", backgroundColor:"#79b821", nextState:"unknown_alarmMode"
		}
		controlTile("alarmSensitivity", "device.alarmSensitivity", "slider", height: 1, width: 2, inactiveLabel: false)
		{
			state "alarmSensitivity", label:'Sensitivity', action:"setAlarmSensitivity", backgroundColor:"#ff0000"
		}
		standardTile("autoLock", "device.autoLock", inactiveLabel: true, canChangeIcon: false)
		{
			state "unknown_autoLock", label: 'Auto Lock\nLoading...', icon:"st.unknown.unknown.unknown", action:"setAutoLock", nextState:"unknown_autoLock"
			state "off_autoLock", label: 'Auto Lock', icon:"st.presence.house.unlocked", action:"setAutoLock", nextState:"unknown_autoLock"
			state "on_autoLock", label: 'Auto Lock', icon:"st.presence.house.secured", action:"setAutoLock", backgroundColor:"#79b821", nextState:"unknown_autoLock"
		}

		// not included in details

		standardTile("vacationMode", "device.vacationMode", inactiveLabel: true, canChangeIcon: false)
		{
			state "unknown_vacationMode", label: 'Vacation\nLoading...', icon:"st.unknown.unknown.unknown", action:"setVacationMode", nextState:"unknown_vacationMode"
			state "off_vacationMode", label: 'Vacation', icon:"st.Health & Wellness.health2", action:"setVacationMode", nextState:"unknown_vacationMode"
			state "on_vacationMode", label: 'Vacation', icon:"st.Health & Wellness.health2", action:"setVacationMode", backgroundColor:"#79b821", nextState:"unknown_vacationMode"
		}

		// not included in details

		standardTile("lockLeave", "device.lockLeave", inactiveLabel: true, canChangeIcon: false)
		{
			state "unknown_lockLeave", label: 'Lock & Leave\nLoading...', icon:"st.unknown.unknown.unknown", action:"setLockLeave", nextState:"unknown_lockLeave"
			state "off_lockLeave", label: 'Lock & Leave', icon:"st.Health & Wellness.health12", action:"setLockLeave", nextState:"unknown_lockLeave"
			state "on_lockLeave", label: 'Lock & Leave', icon:"st.Health & Wellness.health12", action:"setLockLeave", backgroundColor:"#79b821", nextState:"unknown_lockLeave"
		}

		// not included in details

		standardTile("localControl", "device.localControl", inactiveLabel: true, canChangeIcon: false)
		{
			state "unknown_localControl", label: 'Local Ctrl\nLoading...', icon:"st.unknown.unknown.unknown", action:"setLocalControl", nextState:"unknown_localControl"
			state "off_localControl", label: 'Local Ctrl', icon:"st.Home.home3", action:"setLocalControl", nextState:"unknown_localControl"
			state "on_localControl", label: 'Local Ctrl', icon:"st.Home.home3", action:"setLocalControl", backgroundColor:"#79b821", nextState:"unknown_localControl"
		}


		// not included in details

		standardTile("beeperMode", "device.beeperMode", inactiveLabel: true, canChangeIcon: false)
		{
			state "unknown_beeperMode", label: 'Beeper\nLoading...', icon:"st.unknown.unknown.unknown", action:"setBeeperMode", nextState:"unknown_beeperMode"
			state "off_beeperMode", label: 'Beeper', icon:"st.unknown.unknown.unknown", action:"setBeeperMode", nextState:"unknown_beeperMode"
			state "on_beeperMode", label: 'Beeper', icon:"st.unknown.unknown.unknown", action:"setBeeperMode", backgroundColor:"#79b821", nextState:"unknown_beeperMode"
		}


		main "toggle"
		details(["toggle", "lock", "unlock", "alarmMode", "alarmSensitivity", "battery", "autoLock", "lockLeave", "refresh"])
//		details(["toggle", "lock", "unlock", "alarmMode", "alarmSensitivity", "battery", "autoLock", "lockLeave", "vacationMode", "beeperMode", "refresh"])
	}
}

import physicalgraph.zwave.commands.doorlockv1.*
import physicalgraph.zwave.commands.usercodev1.*

def parse(String description) 
{
	def result = null
	if (description.startsWith("Err")) 
    {
		if (state.sec) 
        {
			result = createEvent(descriptionText:description, displayed:false)
		}
        else 
        {
			result = createEvent(
				descriptionText: "This lock failed to complete the network security key exchange. If you are unable to control it via SmartThings, you must remove it from your network and add it again.",
				eventType: "ALERT",
				name: "secureInclusion",
				value: "failed",
				displayed: true,
			)
		}
	} 
    else 
    {
		def cmd = zwave.parse(description, [ 0x98: 1, 0x72: 2, 0x85: 2, 0x86: 1 ])
		if (cmd) 
        {
			result = zwaveEvent(cmd)
		}
	}
	log.debug "\"$description\" parsed to ${result.inspect()}"
	result
}

def zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityMessageEncapsulation cmd) 
{
	def encapsulatedCommand = cmd.encapsulatedCommand([0x62: 1, 0x71: 2, 0x80: 1, 0x85: 2, 0x63: 1, 0x98: 1, 0x86: 1])
	// log.debug "encapsulated: $encapsulatedCommand"
	if (encapsulatedCommand) 
    {
		zwaveEvent(encapsulatedCommand)
	}
}

def zwaveEvent(physicalgraph.zwave.commands.securityv1.NetworkKeyVerify cmd) {
	createEvent(name:"secureInclusion", value:"success", descriptionText:"Secure inclusion was successful")
}

def zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityCommandsSupportedReport cmd) 
{
	state.sec = cmd.commandClassSupport.collect { String.format("%02X ", it) }.join()
	if (cmd.commandClassControl) 
    {
		state.secCon = cmd.commandClassControl.collect { String.format("%02X ", it) }.join()
	}
	log.debug "Security command classes: $state.sec"
	createEvent(name:"secureInclusion", value:"success", descriptionText:"Lock is securely included")
}

def zwaveEvent(DoorLockOperationReport cmd) 
{
	def result = []
	def map = [ name: "lock" ]
	if (cmd.doorLockMode == 0xFF) 
    {
		map.value = "locked"
	}
    else if (cmd.doorLockMode >= 0x40) 
    {
		map.value = "unknown"
	}
    else if (cmd.doorLockMode & 1) 
    {
		map.value = "unlocked with timeout"
	}
    else 
    {
		map.value = "unlocked"
		if (state.assoc != zwaveHubNodeId) 
        {
			log.debug "setting association"
			result << response(secure(zwave.associationV1.associationSet(groupingIdentifier:1, nodeId:zwaveHubNodeId)))
			result << response(zwave.associationV1.associationSet(groupingIdentifier:2, nodeId:zwaveHubNodeId))
			result << response(secure(zwave.associationV1.associationGet(groupingIdentifier:1)))
		}
	}
	result ? [createEvent(map), *result] : createEvent(map)
}

def zwaveEvent(physicalgraph.zwave.commands.alarmv2.AlarmReport cmd) 
{
	def result = []
	def map = null
	if (cmd.zwaveAlarmType == 6) // ZWAVE_ALARM_TYPE_ACCESS_CONTROL
    {
		if (1 <= cmd.zwaveAlarmEvent && cmd.zwaveAlarmEvent < 10) 
        {
			map = [ name: "lock", value: (cmd.zwaveAlarmEvent & 1) ? "locked" : "unlocked" ]
		}
		switch(cmd.zwaveAlarmEvent) 
        {
			case 1:
				map.descriptionText = "$device.displayName was manually locked"
                map.data = [ usedCode: "manual" ]
				break
			case 2:
				map.descriptionText = "$device.displayName was manually unlocked"
                map.data = [ usedCode: "manual" ]
				break
			case 5:
				if (cmd.eventParameter) 
                {
					map.descriptionText = "$device.displayName was locked with code ${cmd.eventParameter.first()}"
					map.data = [ usedCode: cmd.eventParameter[0] ]
				}
				else
				{
					map.descriptionText = "$device.displayName was locked with keypad"
					map.data = [ usedCode: 0 ]
				}
				break
			case 6:
				if (cmd.eventParameter) 
                {
					map.descriptionText = "$device.displayName was unlocked with code ${cmd.eventParameter.first()}"
					map.data = [ usedCode: cmd.eventParameter[0] ]
				}
				else
				{
					map.descriptionText = "$device.displayName was unlocked with keypad"
					map.data = [ usedCode: 0 ]
				}
				break
			case 9:
				map.descriptionText = "$device.displayName was autolocked"
				map.data = [ usedCode: "auto" ]
				break
			case 7:
			case 8:
			case 0xA:
				map = [ name: "lock", value: "unknown", descriptionText: "$device.displayName was not locked fully" ]
				break
			case 0xB:
				map = [ name: "lock", value: "unknown", descriptionText: "$device.displayName is jammed", eventType: "ALERT", displayed: true ]
				break
			case 0xC:
				map = [ name: "codeChanged", value: "all", descriptionText: "$device.displayName: all user codes deleted", displayed: true, isStateChange: true ]
				allCodesDeleted()
				break
			case 0xD:
				if (cmd.eventParameter) 
                {
					map = [ name: "codeReport", value: cmd.eventParameter[0], data: [ code: "" ], isStateChange: true ]
					map.descriptionText = "$device.displayName code ${map.value} was deleted"
					map.isStateChange = (state["code$map.value"] != "")
					state["code$map.value"] = ""
				} 
                else 
                {
					map = [ name: "codeChanged", descriptionText: "$device.displayName: user code deleted", isStateChange: true ]
				}
				break
			case 0xE:
				map = [ name: "codeChanged", value: cmd.alarmLevel,  descriptionText: "$device.displayName: user code added", isStateChange: true ]
				if (cmd.eventParameter) 
                {
					map.value = cmd.eventParameter[0]
					result << response(requestCode(cmd.eventParameter[0]))
				}
				break
			case 0xF:
				map = [ name: "tamper", value: "detected", descriptionText: "$device.displayName: Too many user code failures.", eventType: "ALERT", displayed: true, isStateChange: true ]
				break
				// map = [ name: "codeChanged", descriptionText: "$device.displayName: user code not added, duplicate", isStateChange: true ]
				// break
			case 0x10:
				map = [ name: "tamper", value: "detected", descriptionText: "$device.displayName: keypad temporarily disabled", displayed: true ]
				break
			case 0x11:
				map = [ descriptionText: "$device.displayName: keypad is busy" ]
				break
			case 0x12:
				map = [ name: "codeChanged", descriptionText: "$device.displayName: program code changed", isStateChange: true ]
				break
			case 0x13:
				map = [ name: "tamper", value: "detected", descriptionText: "$device.displayName: code entry attempt limit exceeded", displayed: true ]
				break
			default:
				map = map ?: [ descriptionText: "$device.displayName: alarm event $cmd.zwaveAlarmEvent", displayed: false ]
				break
		}
	} 
    else if (cmd.zwaveAlarmType == 7) // ZWAVE_ALARM_TYPE_BURGLAR
    {
		map = [ name: "tamper", value: "detected", displayed: true, isStateChange: true ]
		switch (cmd.zwaveAlarmEvent) 
        {
			case 0:
				map.value = "clear"
				map.descriptionText = "$device.displayName: tamper alert cleared"
				break
			case 1:
			case 2:
				map.descriptionText = "$device.displayName: intrusion attempt detected"
				break
			case 3:
				map.descriptionText = "$device.displayName: covering removed"
				break
			case 4:
				map.descriptionText = "$device.displayName: invalid code"
				break
			default:
				map.descriptionText = "$device.displayName: tamper alarm $cmd.zwaveAlarmEvent"
				break
		}
	} 
    else switch(cmd.alarmType) 
    {
    // Schlage locks should be using the alarmv2 variables above or lock/unlock events
/*    
		case 21:  // Manually locked
		case 18:  // Locked with keypad
		case 24:  // Locked by command (Kwikset 914)
		case 27:  // Autolocked
			map = [ name: "lock", value: "locked" ]
			break
		case 16:  // Note: for levers this means it's unlocked, for non-motorized deadbolt, it's just unsecured and might not get unlocked
		case 19:
			map = [ name: "lock", value: "unlocked" ]
			if (cmd.alarmLevel) {
				map.descriptionText = "$device.displayName was unlocked with code $cmd.alarmLevel"
				map.data = [ usedCode: cmd.alarmLevel ]
			}
			break
		case 22:
		case 25:  // Kwikset 914 unlocked by command
			map = [ name: "lock", value: "unlocked" ]
			break
*/            
		case 9:
		case 17:
		case 23:
		case 26:
			map = [ name: "lock", value: "unknown", descriptionText: "$device.displayName bolt is jammed" ]
			break
		case 13:
			map = [ name: "codeChanged", value: cmd.alarmLevel, descriptionText: "$device.displayName code $cmd.alarmLevel was added", isStateChange: true ]
			result << response(requestCode(cmd.alarmLevel))
			break
		case 32:
			map = [ name: "codeChanged", value: "all", descriptionText: "$device.displayName: all user codes deleted", isStateChange: true ]
			allCodesDeleted()
		case 33:
			map = [ name: "codeReport", value: cmd.alarmLevel, data: [ code: "" ], isStateChange: true ]
			map.descriptionText = "$device.displayName code $cmd.alarmLevel was deleted"
			map.isStateChange = (state["code$cmd.alarmLevel"] != "")
			state["code$cmd.alarmLevel"] = ""
			break
		case 112:
			map = [ name: "codeChanged", value: cmd.alarmLevel, descriptionText: "$device.displayName code $cmd.alarmLevel changed", isStateChange: true ]
			result << response(requestCode(cmd.alarmLevel))
			break
		case 130:  // Yale YRD batteries replaced
			map = [ descriptionText: "$device.displayName batteries replaced", isStateChange: true ]
			break
		case 131:
			map = [ /*name: "codeChanged", value: cmd.alarmLevel,*/ descriptionText: "$device.displayName code $cmd.alarmLevel is duplicate", isStateChange: false ]
			break
		case 161:
			if (cmd.alarmLevel == 2) 
            {
				map = [ descriptionText: "$device.displayName front escutcheon removed", isStateChange: true ]
			}
            else 
            {
				map = [ descriptionText: "$device.displayName detected failed user code attempt", isStateChange: true ]
			}
			break
		case 167:
			if (!state.lastbatt || (new Date().time) - state.lastbatt > 12*60*60*1000) 
            {
				map = [ descriptionText: "$device.displayName: battery low", isStateChange: true ]
				result << response(secure(zwave.batteryV1.batteryGet()))
			}
            else 
            {
				map = [ name: "battery", value: device.currentValue("battery"), descriptionText: "$device.displayName: battery low", displayed: true ]
			}
			break
		case 168:
			map = [ name: "battery", value: 1, descriptionText: "$device.displayName: battery level critical", displayed: true ]
			break
		case 169:
			map = [ name: "battery", value: 0, descriptionText: "$device.displayName: battery too low to operate lock", isStateChange: true ]
			break
		default:
			map = [ displayed: false, descriptionText: "$device.displayName: alarm event $cmd.alarmType level $cmd.alarmLevel" ]
			break
	}
	result ? [createEvent(map), *result] : createEvent(map)
}

def zwaveEvent(UserCodeReport cmd) 
{
	def result = []
	def name = "code$cmd.userIdentifier"
	def code = cmd.code
	def map = [:]
	if (cmd.userIdStatus == UserCodeReport.USER_ID_STATUS_OCCUPIED ||
		(cmd.userIdStatus == UserCodeReport.USER_ID_STATUS_STATUS_NOT_AVAILABLE && cmd.user && code != "**********"))
	{
		if (code == "**********") 
        {  // Schlage locks send us this instead of the real code
			state.blankcodes = true
			code = state["set$name"] ?: decrypt(state[name]) ?: code
			state.remove("set$name".toString())
		}
		if (!code && cmd.userIdStatus == 1) 
        {  // Schlage touchscreen sends blank code to notify of a changed code
			map = [ name: "codeChanged", value: cmd.userIdentifier, displayed: true, isStateChange: true ]
			map.descriptionText = "$device.displayName code $cmd.userIdentifier " + (state[name] ? "changed" : "was added")
			code = state["set$name"] ?: decrypt(state[name]) ?: "****"
			state.remove("set$name".toString())
		} 
        else 
        {
			map = [ name: "codeReport", value: cmd.userIdentifier, data: [ code: code ] ]
			map.descriptionText = "$device.displayName code $cmd.userIdentifier is set"
			map.displayed = (cmd.userIdentifier != state.requestCode && cmd.userIdentifier != state.pollCode)
			map.isStateChange = (code != decrypt(state[name]))
		}
		result << createEvent(map)
	} 
    else 
    {
		map = [ name: "codeReport", value: cmd.userIdentifier, data: [ code: "" ] ]
		if (state.blankcodes && state["reset$name"]) 
        {  // we deleted this code so we can tell that our new code gets set
			map.descriptionText = "$device.displayName code $cmd.userIdentifier was reset"
			map.displayed = map.isStateChange = false
			result << createEvent(map)
			state["set$name"] = state["reset$name"]
			result << response(setCode(cmd.userIdentifier, state["reset$name"]))
			state.remove("reset$name".toString())
		}
        else 
        {
			if (state[name]) 
            {
				map.descriptionText = "$device.displayName code $cmd.userIdentifier was deleted"
			}
            else 
            {
				map.descriptionText = "$device.displayName code $cmd.userIdentifier is not set"
			}
			map.displayed = (cmd.userIdentifier != state.requestCode && cmd.userIdentifier != state.pollCode)
			map.isStateChange = state[name] as Boolean
			result << createEvent(map)
		}
		code = ""
	}
	state[name] = code ? encrypt(code) : code

	if (cmd.userIdentifier == state.requestCode)
    {  // reloadCodes() was called, keep requesting the codes in order
		if (state.requestCode + 1 > state.codes || state.requestCode >= 30) 
        {
			state.remove("requestCode")  // done
		}
        else 
        {
			state.requestCode = state.requestCode + 1  // get next
			result << response(requestCode(state.requestCode))
		}
	}
	if (cmd.userIdentifier == state.pollCode) 
    {
		if (state.pollCode + 1 > state.codes || state.pollCode >= 30) 
        {
			state.remove("pollCode")  // done
		}
        else 
        {
			state.pollCode = state.pollCode + 1
		}
	}
	log.debug "code report parsed to ${result.inspect()}"
	result
}

def zwaveEvent(UsersNumberReport cmd) 
{
	def result = []
	state.codes = cmd.supportedUsers
	if (state.requestCode && state.requestCode <= cmd.supportedUsers) 
    {
		result << response(requestCode(state.requestCode))
	}
	result
}

def zwaveEvent(physicalgraph.zwave.commands.associationv2.AssociationReport cmd) {
	def result = []
	if (cmd.nodeId.any { it == zwaveHubNodeId }) 
    {
		state.remove("associationQuery")
		log.debug "$device.displayName is associated to $zwaveHubNodeId"
		result << createEvent(descriptionText: "$device.displayName is associated")
		state.assoc = zwaveHubNodeId
		if (cmd.groupingIdentifier == 2) 
        {
			result << response(zwave.associationV1.associationRemove(groupingIdentifier:1, nodeId:zwaveHubNodeId))
		}
	} 
    else if (cmd.groupingIdentifier == 1) 
    {
		result << response(secure(zwave.associationV1.associationSet(groupingIdentifier:1, nodeId:zwaveHubNodeId)))
	}
    else if (cmd.groupingIdentifier == 2) 
    {
		result << response(zwave.associationV1.associationSet(groupingIdentifier:2, nodeId:zwaveHubNodeId))
	}
	result
}

def zwaveEvent(physicalgraph.zwave.commands.timev1.TimeGet cmd) 
{
	def result = []
	def now = new Date().toCalendar()
	if(location.timeZone) now.timeZone = location.timeZone
	result << createEvent(descriptionText: "$device.displayName requested time update", displayed: false)
	result << response(secure(zwave.timeV1.timeReport(
		hourLocalTime: now.get(Calendar.HOUR_OF_DAY),
		minuteLocalTime: now.get(Calendar.MINUTE),
		secondLocalTime: now.get(Calendar.SECOND)))
	)
	result
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicSet cmd) 
{
	// The old Schlage locks use group 1 for basic control - we don't want that, so unsubscribe from group 1
	def result = [ createEvent(name: "lock", value: cmd.value ? "unlocked" : "locked") ]
	result << response(zwave.associationV1.associationRemove(groupingIdentifier:1, nodeId:zwaveHubNodeId))
	if (state.assoc != zwaveHubNodeId) 
    {
		result << response(zwave.associationV1.associationGet(groupingIdentifier:2))
	}
	result
}

def zwaveEvent(physicalgraph.zwave.commands.batteryv1.BatteryReport cmd) 
{
	def map = [ name: "battery", unit: "%" ]
	if (cmd.batteryLevel == 0xFF) 
    {
		map.value = 1
		map.descriptionText = "$device.displayName has a low battery"
	}
    else 
    {
		map.value = cmd.batteryLevel
	}
	state.lastbatt = new Date().time
	createEvent(map)
}

def zwaveEvent(physicalgraph.zwave.commands.manufacturerspecificv2.ManufacturerSpecificReport cmd) 
{
	def result = []

	def msr = String.format("%04X-%04X-%04X", cmd.manufacturerId, cmd.productTypeId, cmd.productId)
	log.debug "msr: $msr"
	updateDataValue("MSR", msr)

	result << createEvent(descriptionText: "$device.displayName MSR: $msr", isStateChange: false)
	result
}

def zwaveEvent(physicalgraph.zwave.commands.versionv1.VersionReport cmd) 
{
	def fw = "${cmd.applicationVersion}.${cmd.applicationSubVersion}"
	updateDataValue("fw", fw)
	if (state.MSR == "003B-6341-5044") {
		updateDataValue("ver", "${cmd.applicationVersion >> 4}.${cmd.applicationVersion & 0xF}")
	}
	def text = "$device.displayName: firmware version: $fw, Z-Wave version: ${cmd.zWaveProtocolVersion}.${cmd.zWaveProtocolSubVersion}"
	createEvent(descriptionText: text, isStateChange: false)
}

def zwaveEvent(physicalgraph.zwave.commands.applicationstatusv1.ApplicationBusy cmd) 
{
	def msg = cmd.status == 0 ? "try again later" :
	          cmd.status == 1 ? "try again in $cmd.waitTime seconds" :
	          cmd.status == 2 ? "request queued" : "sorry"
	createEvent(displayed: true, descriptionText: "$device.displayName is busy, $msg")
}

def zwaveEvent(physicalgraph.zwave.commands.applicationstatusv1.ApplicationRejectedRequest cmd) 
{
	createEvent(displayed: true, descriptionText: "$device.displayName rejected the last request")
}

def zwaveEvent(physicalgraph.zwave.commands.configurationv2.ConfigurationReport cmd)
{
	def result = []
	def map = null		// use this for config reports that are handled

	// use desc/val for generic handling of config reports (it will just send a descriptionText for the acitivty stream)
	def desc = null
	def val = ""

	switch (cmd.parameterNumber)
	{
		case 0x3:
			map = parseBinaryConfigRpt('beeperMode', cmd.configurationValue[0], 'Beeper Mode')
			break

		// done:  vacation mode toggle
		case 0x4:
			map = parseBinaryConfigRpt('vacationMode', cmd.configurationValue[0], 'Vacation Mode')
			break

		// done: lock and leave mode
		case 0x5:
			map = parseBinaryConfigRpt('lockLeave', cmd.configurationValue[0], 'Lock & Leave')
			break

		// these don't seem to be useful.  It's just a bitmap of the code slots used.
		case 0x6:
			desc = "User Slot Bit Fields"
			val = "${cmd.configurationValue[3]} ${cmd.configurationValue[2]} ${cmd.configurationValue[1]} ${cmd.configurationValue[0]}"
			break

		// done:  the alarm mode of the lock.
		case 0x7:
			map = [ name:"alarmMode", displayed: true ]
			// when getting the alarm mode, also query the sensitivity for that current alarm mode
			switch (cmd.configurationValue[0])
			{
				case 0x00:
					map.value = "Off_alarmMode"
					break
				case 0x01:
					map.value = "Alert_alarmMode"
					result << response(secure(zwave.configurationV2.configurationGet(parameterNumber: 0x08)))
					break
				case 0x02:
					map.value = "Tamper_alarmMode"
					result << response(secure(zwave.configurationV2.configurationGet(parameterNumber: 0x09)))
					break
				case 0x03:
					map.value = "Kick_alarmMode"
					result << response(secure(zwave.configurationV2.configurationGet(parameterNumber: 0x0A)))
					break
				default:
					map.value = "unknown_alarmMode"
			}
			map.descriptionText = "$device.displayName Alarm Mode set to \"$map.value\""
			break

		// done: alarm sensitivities - one for each mode
		case 0x8:
		case 0x9:
		case 0xA:
			def whichMode = null
			switch (cmd.parameterNumber)
			{
				case 0x8:
					whichMode = "Alert"
					break;
				case 0x9:
					whichMode = "Tamper"
					break;
				case 0xA:
					whichMode = "Kick"
					break;
			}
			def curAlarmMode = device.currentValue("alarmMode")
			val = "${cmd.configurationValue[0]}"

			// the lock has sensitivity values between 1 and 5.	 ST sliders want a value between 0 and 99.	Use a formula
			// to make the internal attribute something visually appealing on the UI slider
			def modifiedValue = (cmd.configurationValue[0] * 24) - 23

			map = [ descriptionText: "$device.displayName Alarm $whichMode Sensitivity set to $val", displayed: true ]

			if (curAlarmMode == "${whichMode}_alarmMode")
			{
				map.name = "alarmSensitivity"
				map.value = modifiedValue
			}
			else
			{
				log.debug "got sensitivity for $whichMode while in $curAlarmMode"
				map.isStateChange = true
			}

			break

		case 0xB:
			map = parseBinaryConfigRpt('localControl', cmd.configurationValue[0], 'Local Alarm Control')
			break

		// how many times has the electric motor locked or unlock the device?
		case 0xC:
			desc = "Electronic Transition Count"
			def ttl = cmd.configurationValue[3] + (cmd.configurationValue[2] * 0x100) + (cmd.configurationValue[1] * 0x10000) + (cmd.configurationValue[0] * 0x1000000)
			val = "$ttl"
			break

		// how many times has the device been locked or unlocked manually?
		case 0xD:
			desc = "Mechanical Transition Count"
			def ttl = cmd.configurationValue[3] + (cmd.configurationValue[2] * 0x100) + (cmd.configurationValue[1] * 0x10000) + (cmd.configurationValue[0] * 0x1000000)
			val = "$ttl"
			break

		// how many times has there been a failure by the electric motor?  (due to jamming??)
		case 0xE:
			desc = "Electronic Failed Count"
			def ttl = cmd.configurationValue[3] + (cmd.configurationValue[2] * 0x100) + (cmd.configurationValue[1] * 0x10000) + (cmd.configurationValue[0] * 0x1000000)
			val = "$ttl"
			break

		// done: auto lock mode
		case 0xF:
			map = parseBinaryConfigRpt('autoLock', cmd.configurationValue[0], 'Auto Lock')
			break

		// this will be useful as an attribute/command usable by a smartapp
		case 0x10:
			map = [ name: 'pinLength', value: cmd.configurationValue[0], displayed: true, descriptionText: "$device.displayName PIN length configured to ${cmd.configurationValue[0]} digits"]
			break

		// not sure what this one stores
		case 0x11:
			desc = "Electronic High Preload Transition Count"
			def ttl = cmd.configurationValue[3] + (cmd.configurationValue[2] * 0x100) + (cmd.configurationValue[1] * 0x10000) + (cmd.configurationValue[0] * 0x1000000)
			val = "$ttl"
			break

		// ???
		case 0x12:
			desc = "Bootloader Version"
			val = "${cmd.configurationValue[0]}"
			break
		default:
			desc = "Unknown parameter ${cmd.parameterNumber}"
			val = "${cmd.configurationValue[0]}"
			break
	}
	if (map)
	{
		result << createEvent(map)
	}
	else if (desc != null)
	{
		// generic description text
		result << createEvent([ descriptionText: "$device.displayName reports \"$desc\" configured as \"$val\"", displayed: true, isStateChange: true ])
	}
	result
}

def parseBinaryConfigRpt(paramName, paramValue, paramDesc)
{
	def map = [ name: paramName, displayed: true ]

	def newVal = "on"
	if (paramValue == 0)
	{
		newVal = "off"
	}
	map.value = "${newVal}_${paramName}"
	map.descriptionText = "$device.displayName $paramDesc has been turned $newVal"
	return map
}



def zwaveEvent(physicalgraph.zwave.Command cmd) 
{
	createEvent(displayed: false, descriptionText: "$device.displayName: $cmd")
}

def lockAndCheck(doorLockMode) 
{
	secureSequence([
		zwave.doorLockV1.doorLockOperationSet(doorLockMode: doorLockMode),
		zwave.doorLockV1.doorLockOperationGet()
	], 4200)
}

def lock() 
{
	lockAndCheck(DoorLockOperationSet.DOOR_LOCK_MODE_DOOR_SECURED)
}

def unlock() 
{
	lockAndCheck(DoorLockOperationSet.DOOR_LOCK_MODE_DOOR_UNSECURED)
}

def unlockwtimeout() 
{
	lockAndCheck(DoorLockOperationSet.DOOR_LOCK_MODE_DOOR_UNSECURED_WITH_TIMEOUT)
}

def refresh() {
//	def cmds = [secure(zwave.doorLockV1.doorLockOperationGet())]
	def cmds = secureSequence([
			zwave.doorLockV1.doorLockOperationGet(),
//			  zwave.configurationV2.configurationBulkGet(numberOfParameters: 3, parameterOffset: 0x8),
//			  zwave.configurationV2.configurationBulkGet(numberOfParameters: 4, parameterOffset: 0x3),
//			  zwave.configurationV2.configurationGet(parameterNumber: 0x3),		// beeper (done)
//			  zwave.configurationV2.configurationGet(parameterNumber: 0x4),		// vacation mode (done)
//			  zwave.configurationV2.configurationGet(parameterNumber: 0x5),		// lock and leave (done)
//			  zwave.configurationV2.configurationGet(parameterNumber: 0x6),		// user slot bit field (not needed)
//			zwave.configurationV2.configurationGet(parameterNumber: 0x7),		// alarm mode (done)
//			  zwave.configurationV2.configurationGet(parameterNumber: 0x8),		// alert alarm sensitivity (done: retrieved after alarm mode)
//			  zwave.configurationV2.configurationGet(parameterNumber: 0x9),		// tamper alarm sensitivity (done: retrieved after alarm mode)
//			  zwave.configurationV2.configurationGet(parameterNumber: 0xA),		// kick alarm sensititivy (done: retrieved after alarm mode)
//			  zwave.configurationV2.configurationGet(parameterNumber: 0xB),		// local alarm control disable (done)
//			  zwave.configurationV2.configurationGet(parameterNumber: 0xC),		// electronic transition count
//			  zwave.configurationV2.configurationGet(parameterNumber: 0xD),		// mechanical transition count
//			  zwave.configurationV2.configurationGet(parameterNumber: 0xE),		// electronic failure count
//			  zwave.configurationV2.configurationGet(parameterNumber: 0xF),		// autolock (done)
//			  zwave.configurationV2.configurationGet(parameterNumber: 0x10),		// user code pin length (done)
//			  zwave.configurationV2.configurationGet(parameterNumber: 0x11,)	// electronic high preload transition count
//			  zwave.configurationV2.configurationGet(parameterNumber: 0x12,)	// bootloader version
			
		], 6000)

	// go ahead and fill in any missing values
	if (null == device.latestValue("pinLength"))
    {
    	log.debug "getting pin length"
    	cmds << "delay 6000"
        cmds << secure(zwave.configurationV2.configurationGet(parameterNumber: 0x10))
    }


	if (state.assoc == zwaveHubNodeId) {
		log.debug "$device.displayName is associated to ${state.assoc}"
	} else if (!state.associationQuery) {
		log.debug "checking association"
		cmds << "delay 4200"
		cmds << zwave.associationV1.associationGet(groupingIdentifier:2).format()  // old Schlage locks use group 2 and don't secure the Association CC
		cmds << secure(zwave.associationV1.associationGet(groupingIdentifier:1))
		state.associationQuery = new Date().time
	} else if (new Date().time - state.associationQuery.toLong() > 9000) {
		log.debug "setting association"
		cmds << "delay 6000"
		cmds << zwave.associationV1.associationSet(groupingIdentifier:2, nodeId:zwaveHubNodeId).format()
		cmds << secure(zwave.associationV1.associationSet(groupingIdentifier:1, nodeId:zwaveHubNodeId))
		cmds << zwave.associationV1.associationGet(groupingIdentifier:2).format()
		cmds << secure(zwave.associationV1.associationGet(groupingIdentifier:1))
		state.associationQuery = new Date().time
	}
	log.debug "refresh is sending ${cmds.inspect()}, state: ${state.inspect()}"
	cmds
}

def poll() {
	def cmds = []
    state.pinLength = null;
	if (state.assoc != zwaveHubNodeId && secondsPast(state.associationQuery, 19 * 60)) 
    {
		log.debug "setting association"
		cmds << zwave.associationV1.associationSet(groupingIdentifier:2, nodeId:zwaveHubNodeId).format()
		cmds << secure(zwave.associationV1.associationSet(groupingIdentifier:1, nodeId:zwaveHubNodeId))
		cmds << zwave.associationV1.associationGet(groupingIdentifier:2).format()
		cmds << "delay 6000"
		cmds << secure(zwave.associationV1.associationGet(groupingIdentifier:1))
		cmds << "delay 6000"
		state.associationQuery = new Date().time
	} 
    else 
    {
        // go ahead and fill in any missing values
        if (null == device.latestValue("pinLength"))
        {
            cmds << secure(zwave.configurationV2.configurationGet(parameterNumber: 0x10))
            cmds << "delay 6000"
        }
    
		// Only check lock state if it changed recently or we haven't had an update in an hour
		def latest = device.currentState("lock")?.date?.time
		if (!latest || !secondsPast(latest, 6 * 60) || secondsPast(state.lastPoll, 55 * 60)) 
        {
			cmds << secure(zwave.doorLockV1.doorLockOperationGet())
			state.lastPoll = (new Date()).time
		} 
        else if (!state.MSR) 
        {
			cmds << zwave.manufacturerSpecificV1.manufacturerSpecificGet().format()
		}
        else if (!state.fw) 
        {
			cmds << zwave.versionV1.versionGet().format()
		} 
        else if (!state.codes) 
        {
			state.pollCode = 1
			cmds << secure(zwave.userCodeV1.usersNumberGet())
		} 
        else if (state.pollCode && state.pollCode <= state.codes) 
        {
			cmds << requestCode(state.pollCode)
		} 
        else if (!state.lastbatt || (new Date().time) - state.lastbatt > 53*60*60*1000) 
        {
			cmds << secure(zwave.batteryV1.batteryGet())
		} 
        else if (!state.enc) 
        {
			encryptCodes()
			state.enc = 1
		}
	}
    reportAllCodes(state)
    
	log.debug "poll is sending ${cmds.inspect()}"
	device.activity()
	cmds ?: null
}

private def encryptCodes() 
{
	def keys = new ArrayList(state.keySet().findAll { it.startsWith("code") })
	keys.each { key ->
		def match = (key =~ /^code(\d+)$/)
		if (match) try 
        {
			def keynum = match[0][1].toInteger()
			if (keynum > 30 && !state[key]) 
            {
				state.remove(key)
			} 
            else if (state[key] && !state[key].startsWith("~")) 
			{
				log.debug "encrypting $key: ${state[key].inspect()}"
				state[key] = encrypt(state[key])
			}
		} 
        catch (java.lang.NumberFormatException e) { }
	}
}

def requestCode(codeNumber) 
{
	log.debug "getting user code $codeNumber"
	secure(zwave.userCodeV1.userCodeGet(userIdentifier: codeNumber))
}

def reloadAllCodes() 
{
	def cmds = []
	if (!state.codes) 
    {
		state.requestCode = 1
		cmds << secure(zwave.userCodeV1.usersNumberGet())
	}
    else 
    {
		if(!state.requestCode) state.requestCode = 1
		cmds << requestCode(codeNumber)
	}
	cmds
}

def setCode(codeNumber, code) 
{
	def strcode = code
	log.debug "setting code $codeNumber to $code"
	if (code instanceof String) 
    {
		code = code.toList().findResults { if(it > ' ' && it != ',' && it != '-') it.toCharacter() as Short }
	}
    else 
    {
		strcode = code.collect{ it as Character }.join()
	}
	if (state.blankcodes) 
    {
		// Can't just set, we won't be able to tell if it was successful
		if (state["code$codeNumber"] != "") 
        {
			if (state["setcode$codeNumber"] != strcode) 
            {
				state["resetcode$codeNumber"] = strcode
				return deleteCode(codeNumber)
			}
		} 
        else 
        {
			state["setcode$codeNumber"] = strcode
		}
	}
	secureSequence([
		zwave.userCodeV1.userCodeSet(userIdentifier:codeNumber, userIdStatus:1, user:code),
		zwave.userCodeV1.userCodeGet(userIdentifier:codeNumber)
	], 7000)
}

def deleteCode(codeNumber) 
{
	log.debug "deleting code $codeNumber"
	secureSequence([
		zwave.userCodeV1.userCodeSet(userIdentifier:codeNumber, userIdStatus:0),
		zwave.userCodeV1.userCodeGet(userIdentifier:codeNumber)
	], 7000)
}

def updateCodes(codeSettings) 
{
log.debug "updateCodes called with: ${codeSettings.inspect()}"
	if(codeSettings instanceof String) codeSettings = util.parseJson(codeSettings)
	def set_cmds = []
	def get_cmds = []
	codeSettings.each { name, updated ->
		def current = decrypt(state[name])
		if (name.startsWith("code")) 
        {
			def n = name[4..-1].toInteger()
			if (updated?.size() >= 4 && updated != current) 
            {
				log.debug "$name was $current, set to $updated"
				def cmds = setCode(n, updated)
				set_cmds << cmds.first()
				get_cmds << cmds.last()
			}
            else if ((current && updated == "") || updated == "0") 
            {
				log.debug "$name was $current, set to deleted"
				def cmds = deleteCode(n)
				set_cmds << cmds.first()
				get_cmds << cmds.last()
			}
            else if (updated && updated.size() < 4) 
            {
				log.debug "Attempt to set $name to a value that's too short"
				// Entered code was too short
				codeSettings["code$n"] = current
			}
            else
            {
            	log.debug "$name remains unchanged."
            }
		} 
        else 
        	log.warn("unexpected entry $name: $updated")
	}
	if (set_cmds) 
    {
		return response(delayBetween(set_cmds, 2200) + ["delay 2200"] + delayBetween(get_cmds, 4200))
	}
}

def getCode(codeNumber) 
{
	decrypt(state["code$codeNumber"])
}

def getAllCodes() 
{
	state.findAll { it.key.startsWith 'code' }.collectEntries  {
		[it.key, (it.value instanceof String && it.value.startsWith("~")) ? decrypt(it.value) : it.value]
	}
}

private secure(physicalgraph.zwave.Command cmd) 
{
	zwave.securityV1.securityMessageEncapsulation().encapsulate(cmd).format()
}

private secureSequence(commands, delay=4200) 
{
	delayBetween(commands.collect{ secure(it) }, delay)
}

private Boolean secondsPast(timestamp, seconds) 
{
	if (!(timestamp instanceof Number)) 
    {
		if (timestamp instanceof Date) 
        {
			timestamp = timestamp.time
		} 
        else if ((timestamp instanceof String) && timestamp.isNumber()) 
        {
			timestamp = timestamp.toLong()
		} 
        else 
        {
			return true
		}
	}
	return (new Date().time - timestamp) > (seconds * 1000)
}

private allCodesDeleted() {
	if (state.codes instanceof Integer) 
    {
		(1..state.codes).each { n ->
			if (state["code$n"]) 
            {
				result << createEvent(name: "codeReport", value: n, data: [ code: "" ], descriptionText: "code $n was deleted",
					displayed: false, isStateChange: true)
			}
			state["code$n"] = ""
		}
	}
}

// all the on/off parameters work the same way, so make a common method
// to deal with them
//
def setOnOffParameter(paramName, paramNumber)
{
	def cmds = null
	def cs = device.currentValue(paramName)

	// change parameter to the 'unknown' value - it will get refreshed after it is done changing
	sendEvent(name: paramName, value: "unknown_${paramName}", displayed: false )

	if (cs == "on_${paramName}")
	{
		// turn it off
		cmds = secureSequence([zwave.configurationV2.configurationSet(parameterNumber: paramNumber, size: 1, configurationValue: [0])],5000)
	}
	else if (cs == "off_${paramName}")
	{
		// turn it on
		cmds = secureSequence([zwave.configurationV2.configurationSet(parameterNumber: paramNumber, size: 1, configurationValue: [0xFF])],5000)
	}
	else
	{
		// it's in an unknown state, so just query it
		cmds = secureSequence([zwave.configurationV2.configurationGet(parameterNumber: paramNumber)], 5000)
	}

	log.debug "set $paramName sending ${cmds.inspect()}"

	cmds
}

def setBeeperMode()
{
	setOnOffParameter("beeperMode", 0x3)
}

def setVacationMode()
{
	setOnOffParameter("vacationMode", 0x4)
}

def setLockLeave()
{
	setOnOffParameter("lockLeave", 0x5)
}

def setLocalControl()
{
	setOnOffParameter("localControl", 0xB)
}

def setAutoLock()
{
	setOnOffParameter("autoLock", 0xF)
}

def setAlarmMode()
{

	def cs = device.currentValue("alarmMode")
	def newMode = 0x0

	def cmds = null

	switch (cs)
	{
		case "Off_alarmMode":
			newMode = 0x1
			break

		case "Alert_alarmMode":
			newMode = 0x2
			break

		case "Tamper_alarmMode":
			newMode = 0x3
			break;

		case "Kick_alarmMode":
			newMode = 0x0
			break;

		case "unknown_alarmMode":
		default:
			// don't send a mode - instead request the current state
			cmds = secureSequence([zwave.configurationV2.configurationGet(parameterNumber: 0x7)], 5000)

	}
	if (cmds == null)
	{
		// change the alarmSensitivity to the 'unknown' value - it will get refreshed after the alarm mode is done changing
		sendEvent(name: 'alarmSensitivity', value: 0, displayed: false )
		cmds = secureSequence([zwave.configurationV2.configurationSet(parameterNumber: 7, size: 1, configurationValue: [newMode])],5000)
	}

	log.debug "setAlarmMode sending ${cmds.inspect()}"
	cmds
}

def setPinLength(newValue)
{
	def cmds = null
	if ((newValue == null) || (newValue == 0))
	{
		// just send a request to refresh the value
		cmds = secureSequence([zwave.configurationV2.configurationGet(parameterNumber: 0x10)],5000)
	}
	else if (newValue <= 8)
	{
		sendEvent(descriptionText: "$device.displayName attempting to change PIN length to $newValue", displayed: true, isStateChange: true)
		cmds = secureSequence([zwave.configurationV2.configurationSet(parameterNumber: 10, size: 1, configurationValue: [newValue])],5000)
	}
	else
	{
		sendEvent(descriptionText: "$device.displayName UNABLE to set PIN length of $newValue", displayed: true, isStateChange: true)
	}
	log.debug "setPinLength sending ${cmds.inspect()}"
	cmds
}

def setAlarmSensitivity(newValue)
{
	def cmds = null
	if (newValue != null)
	{
		// newvalue will be between 0 and 99, but we need a value between 1 and 5 inclusive...
		newValue = (newValue / 20) + 1
		newValue = newValue.toInteger();

		// there are three possible values to set.	which one depends on the current alarmMode
		def cs = device.currentValue("alarmMode")

		def paramToSet = 0

		switch(cs)
		{
			case "Off":
				// do nothing.	the slider should be disabled anyway
				break
			case "Alert":
				// set param 8
				paramToSet = 0x8
				break;
			case "Tamper":
				paramToSet = 0x9
				break
			case "Kick":
				paramToSet = 0xA
				break
			default:
				sendEvent(descriptionText: "$device.displayName unable to set alarm sensitivity while alarm mode in unknown state", displayed: true, isStateChange: true)
				break
		}
		if (paramToSet != 0)
		{
			// first set the attribute to 0 for UI purposes
			sendEvent(name: 'alarmSensitivity', value: 0, displayed: false )
			// then add the actual attribute set call
			cmds = secureSequence([zwave.configurationV2.configurationSet(parameterNumber: paramToSet, size: 1, configurationValue: [newValue])],5000)
			log.debug "setAlarmSensitivity sending ${cmds.inspect()}"
		}
	}
	cmds
}

// provides compatibility with Erik Thayer's "Lock Code Manager"
def reportAllCodes(state) 
{
  def map = [ name: "reportAllCodes", data: [:], displayed: false, isStateChange: false, type: "physical" ]
  state.each { entry ->
    //iterate through all the state entries and add them to the event data to be handled by application event handlers
    if ( entry.key ==~ /^code\d{1,}/ && entry.value.startsWith("~") ) 
    {
		map.data.put(entry.key, decrypt(entry.value))
    }
    else 
    {
    	map.data.put(entry.key, entry.value)
    }
  }
  sendEvent(map)
}