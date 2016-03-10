/**
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

		command "unlockwtimeout"

		fingerprint deviceId: "0x4003", inClusters: "0x98"
		fingerprint deviceId: "0x4004", inClusters: "0x98"
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
				attributeState "locked", label:'locked', action:"lock.unlock", icon:"st.locks.lock.locked", backgroundColor:"#79b821", nextState:"unlocking"
				attributeState "unlocked", label:'unlocked', action:"lock.lock", icon:"st.locks.lock.unlocked", backgroundColor:"#ffffff", nextState:"locking"
				attributeState "unknown", label:"unknown", action:"lock.lock", icon:"st.locks.lock.unknown", backgroundColor:"#ffffff", nextState:"locking"
				attributeState "locking", label:'locking', icon:"st.locks.lock.locked", backgroundColor:"#79b821"
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

def updated() {
	try {
		if (!state.init) {
			state.init = true
			response(secureSequence([zwave.doorLockV1.doorLockOperationGet(), zwave.batteryV1.batteryGet()]))
		}
	} catch (e) {
		log.warn "updated() threw $e"
	}
}

def parse(String description) {
	def result = null
	if (description.startsWith("Err 106")) {
		if (state.sec) {
			result = createEvent(descriptionText:description, displayed:false)
		} else {
			result = createEvent(
				descriptionText: "This lock failed to complete the network security key exchange. If you are unable to control it via SmartThings, you must remove it from your network and add it again.",
				eventType: "ALERT",
				name: "secureInclusion",
				value: "failed",
				displayed: true,
			)
		}
	} else if (description == "updated") {
		return null
	} else {
		def cmd = zwave.parse(description, [ 0x98: 1, 0x72: 2, 0x85: 2, 0x86: 1 ])
		if (cmd) {
			result = zwaveEvent(cmd)
		}
	}
	log.debug "\"$description\" parsed to ${result.inspect()}"
	result
}

def zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityMessageEncapsulation cmd) {
	def encapsulatedCommand = cmd.encapsulatedCommand([0x62: 1, 0x71: 2, 0x80: 1, 0x85: 2, 0x63: 1, 0x98: 1, 0x86: 1])
	// log.debug "encapsulated: $encapsulatedCommand"
	if (encapsulatedCommand) {
		zwaveEvent(encapsulatedCommand)
	}
}

def zwaveEvent(physicalgraph.zwave.commands.securityv1.NetworkKeyVerify cmd) {
	createEvent(name:"secureInclusion", value:"success", descriptionText:"Secure inclusion was successful")
}

def zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityCommandsSupportedReport cmd) {
	state.sec = cmd.commandClassSupport.collect { String.format("%02X ", it) }.join()
	if (cmd.commandClassControl) {
		state.secCon = cmd.commandClassControl.collect { String.format("%02X ", it) }.join()
	}
	log.debug "Security command classes: $state.sec"
	createEvent(name:"secureInclusion", value:"success", descriptionText:"Lock is securely included")
}

def zwaveEvent(DoorLockOperationReport cmd) {
	def result = []
	def map = [ name: "lock" ]
	if (cmd.doorLockMode == 0xFF) {
		map.value = "locked"
	} else if (cmd.doorLockMode >= 0x40) {
		map.value = "unknown"
	} else if (cmd.doorLockMode & 1) {
		map.value = "unlocked with timeout"
	} else {
		map.value = "unlocked"
		if (state.assoc != zwaveHubNodeId) {
			log.debug "setting association"
			result << response(secure(zwave.associationV1.associationSet(groupingIdentifier:1, nodeId:zwaveHubNodeId)))
			result << response(zwave.associationV1.associationSet(groupingIdentifier:2, nodeId:zwaveHubNodeId))
			result << response(secure(zwave.associationV1.associationGet(groupingIdentifier:1)))
		}
	}
	result ? [createEvent(map), *result] : createEvent(map)
}

def zwaveEvent(physicalgraph.zwave.commands.alarmv2.AlarmReport cmd) {
	def result = []
	def map = null
	if (cmd.zwaveAlarmType == 6) {
		if (1 <= cmd.zwaveAlarmEvent && cmd.zwaveAlarmEvent < 10) {
			map = [ name: "lock", value: (cmd.zwaveAlarmEvent & 1) ? "locked" : "unlocked" ]
		}
		switch(cmd.zwaveAlarmEvent) {
			case 1:
				map.descriptionText = "$device.displayName was manually locked"
				break
			case 2:
				map.descriptionText = "$device.displayName was manually unlocked"
				break
			case 5:
				if (cmd.eventParameter) {
					map.descriptionText = "$device.displayName was locked with code ${cmd.eventParameter.first()}"
					map.data = [ usedCode: cmd.eventParameter[0] ]
				}
				break
			case 6:
				if (cmd.eventParameter) {
					map.descriptionText = "$device.displayName was unlocked with code ${cmd.eventParameter.first()}"
					map.data = [ usedCode: cmd.eventParameter[0] ]
				}
				break
			case 9:
				map.descriptionText = "$device.displayName was autolocked"
				break
			case 7:
			case 8:
			case 0xA:
				map = [ name: "lock", value: "unknown", descriptionText: "$device.displayName was not locked fully" ]
				break
			case 0xB:
				map = [ name: "lock", value: "unknown", descriptionText: "$device.displayName is jammed" ]
				break
			case 0xC:
				map = [ name: "codeChanged", value: "all", descriptionText: "$device.displayName: all user codes deleted", isStateChange: true ]
				allCodesDeleted()
				break
			case 0xD:
				if (cmd.eventParameter) {
					map = [ name: "codeReport", value: cmd.eventParameter[0], data: [ code: "" ], isStateChange: true ]
					map.descriptionText = "$device.displayName code ${map.value} was deleted"
					map.isStateChange = (state["code$map.value"] != "")
					state["code$map.value"] = ""
				} else {
					map = [ name: "codeChanged", descriptionText: "$device.displayName: user code deleted", isStateChange: true ]
				}
				break
			case 0xE:
				map = [ name: "codeChanged", value: cmd.alarmLevel,  descriptionText: "$device.displayName: user code added", isStateChange: true ]
				if (cmd.eventParameter) {
					map.value = cmd.eventParameter[0]
					result << response(requestCode(cmd.eventParameter[0]))
				}
				break
			case 0xF:
				map = [ name: "codeChanged", descriptionText: "$device.displayName: user code not added, duplicate", isStateChange: true ]
				break
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
	} else if (cmd.zwaveAlarmType == 7) {
		map = [ name: "tamper", value: "detected", displayed: true ]
		switch (cmd.zwaveAlarmEvent) {
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
	} else switch(cmd.alarmType) {
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
			break
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
			if (cmd.alarmLevel == 2) {
				map = [ descriptionText: "$device.displayName front escutcheon removed", isStateChange: true ]
			} else {
				map = [ descriptionText: "$device.displayName detected failed user code attempt", isStateChange: true ]
			}
			break
		case 167:
			if (!state.lastbatt || now() - state.lastbatt > 12*60*60*1000) {
				map = [ descriptionText: "$device.displayName: battery low", isStateChange: true ]
				result << response(secure(zwave.batteryV1.batteryGet()))
			} else {
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

def zwaveEvent(UserCodeReport cmd) {
	def result = []
	def name = "code$cmd.userIdentifier"
	def code = cmd.code
	def map = [:]
	if (cmd.userIdStatus == UserCodeReport.USER_ID_STATUS_OCCUPIED ||
		(cmd.userIdStatus == UserCodeReport.USER_ID_STATUS_STATUS_NOT_AVAILABLE && cmd.user && code != "**********"))
	{
		if (code == "**********") {  // Schlage locks send us this instead of the real code
			state.blankcodes = true
			code = state["set$name"] ?: decrypt(state[name]) ?: code
			state.remove("set$name".toString())
		}
		if (!code && cmd.userIdStatus == 1) {  // Schlage touchscreen sends blank code to notify of a changed code
			map = [ name: "codeChanged", value: cmd.userIdentifier, displayed: true, isStateChange: true ]
			map.descriptionText = "$device.displayName code $cmd.userIdentifier " + (state[name] ? "changed" : "was added")
			code = state["set$name"] ?: decrypt(state[name]) ?: "****"
			state.remove("set$name".toString())
		} else {
			map = [ name: "codeReport", value: cmd.userIdentifier, data: [ code: code ] ]
			map.descriptionText = "$device.displayName code $cmd.userIdentifier is set"
			map.displayed = (cmd.userIdentifier != state.requestCode && cmd.userIdentifier != state.pollCode)
			map.isStateChange = true
		}
		result << createEvent(map)
	} else {
		map = [ name: "codeReport", value: cmd.userIdentifier, data: [ code: "" ] ]
		if (state.blankcodes && state["reset$name"]) {  // we deleted this code so we can tell that our new code gets set
			map.descriptionText = "$device.displayName code $cmd.userIdentifier was reset"
			map.displayed = map.isStateChange = true
			result << createEvent(map)
			state["set$name"] = state["reset$name"]
			result << response(setCode(cmd.userIdentifier, state["reset$name"]))
			state.remove("reset$name".toString())
		} else {
			if (state[name]) {
				map.descriptionText = "$device.displayName code $cmd.userIdentifier was deleted"
			} else {
				map.descriptionText = "$device.displayName code $cmd.userIdentifier is not set"
			}
			map.displayed = (cmd.userIdentifier != state.requestCode && cmd.userIdentifier != state.pollCode)
			map.isStateChange = true
			result << createEvent(map)
		}
		code = ""
	}
	state[name] = code ? encrypt(code) : code

	if (cmd.userIdentifier == state.requestCode) {  // reloadCodes() was called, keep requesting the codes in order
		if (state.requestCode + 1 > state.codes || state.requestCode >= 30) {
			state.remove("requestCode")  // done
		} else {
			state.requestCode = state.requestCode + 1  // get next
			result << response(requestCode(state.requestCode))
		}
	}
	if (cmd.userIdentifier == state.pollCode) {
		if (state.pollCode + 1 > state.codes || state.pollCode >= 30) {
			state.remove("pollCode")  // done
		} else {
			state.pollCode = state.pollCode + 1
		}
	}
	log.debug "code report parsed to ${result.inspect()}"
	result
}

def zwaveEvent(UsersNumberReport cmd) {
	def result = []
	state.codes = cmd.supportedUsers
	if (state.requestCode && state.requestCode <= cmd.supportedUsers) {
		result << response(requestCode(state.requestCode))
	}
	result
}

def zwaveEvent(physicalgraph.zwave.commands.associationv2.AssociationReport cmd) {
	def result = []
	if (cmd.nodeId.any { it == zwaveHubNodeId }) {
		state.remove("associationQuery")
		log.debug "$device.displayName is associated to $zwaveHubNodeId"
		result << createEvent(descriptionText: "$device.displayName is associated")
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

def zwaveEvent(physicalgraph.zwave.commands.timev1.TimeGet cmd) {
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

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicSet cmd) {
	// The old Schlage locks use group 1 for basic control - we don't want that, so unsubscribe from group 1
	def result = [ createEvent(name: "lock", value: cmd.value ? "unlocked" : "locked") ]
	result << response(zwave.associationV1.associationRemove(groupingIdentifier:1, nodeId:zwaveHubNodeId))
	if (state.assoc != zwaveHubNodeId) {
		result << response(zwave.associationV1.associationGet(groupingIdentifier:2))
	}
	result
}

def zwaveEvent(physicalgraph.zwave.commands.batteryv1.BatteryReport cmd) {
	def map = [ name: "battery", unit: "%" ]
	if (cmd.batteryLevel == 0xFF) {
		map.value = 1
		map.descriptionText = "$device.displayName has a low battery"
	} else {
		map.value = cmd.batteryLevel
	}
	state.lastbatt = now()
	createEvent(map)
}

def zwaveEvent(physicalgraph.zwave.commands.manufacturerspecificv2.ManufacturerSpecificReport cmd) {
	def result = []

	def msr = String.format("%04X-%04X-%04X", cmd.manufacturerId, cmd.productTypeId, cmd.productId)
	log.debug "msr: $msr"
	updateDataValue("MSR", msr)

	result << createEvent(descriptionText: "$device.displayName MSR: $msr", isStateChange: false)
	result
}

def zwaveEvent(physicalgraph.zwave.commands.versionv1.VersionReport cmd) {
	def fw = "${cmd.applicationVersion}.${cmd.applicationSubVersion}"
	updateDataValue("fw", fw)
	if (state.MSR == "003B-6341-5044") {
		updateDataValue("ver", "${cmd.applicationVersion >> 4}.${cmd.applicationVersion & 0xF}")
	}
	def text = "$device.displayName: firmware version: $fw, Z-Wave version: ${cmd.zWaveProtocolVersion}.${cmd.zWaveProtocolSubVersion}"
	createEvent(descriptionText: text, isStateChange: false)
}

def zwaveEvent(physicalgraph.zwave.commands.applicationstatusv1.ApplicationBusy cmd) {
	def msg = cmd.status == 0 ? "try again later" :
	          cmd.status == 1 ? "try again in $cmd.waitTime seconds" :
	          cmd.status == 2 ? "request queued" : "sorry"
	createEvent(displayed: true, descriptionText: "$device.displayName is busy, $msg")
}

def zwaveEvent(physicalgraph.zwave.commands.applicationstatusv1.ApplicationRejectedRequest cmd) {
	createEvent(displayed: true, descriptionText: "$device.displayName rejected the last request")
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
	createEvent(displayed: false, descriptionText: "$device.displayName: $cmd")
}

def lockAndCheck(doorLockMode) {
	secureSequence([
		zwave.doorLockV1.doorLockOperationSet(doorLockMode: doorLockMode),
		zwave.doorLockV1.doorLockOperationGet()
	], 4200)
}

def lock() {
	lockAndCheck(DoorLockOperationSet.DOOR_LOCK_MODE_DOOR_SECURED)
}

def unlock() {
	lockAndCheck(DoorLockOperationSet.DOOR_LOCK_MODE_DOOR_UNSECURED)
}

def unlockwtimeout() {
	lockAndCheck(DoorLockOperationSet.DOOR_LOCK_MODE_DOOR_UNSECURED_WITH_TIMEOUT)
}

def refresh() {
	def cmds = [secure(zwave.doorLockV1.doorLockOperationGet())]
	if (state.assoc == zwaveHubNodeId) {
		log.debug "$device.displayName is associated to ${state.assoc}"
	} else if (!state.associationQuery) {
		log.debug "checking association"
		cmds << "delay 4200"
		cmds << zwave.associationV1.associationGet(groupingIdentifier:2).format()  // old Schlage locks use group 2 and don't secure the Association CC
		cmds << secure(zwave.associationV1.associationGet(groupingIdentifier:1))
		state.associationQuery = now()
	} else if (secondsPast(state.associationQuery, 9)) {
		cmds << "delay 6000"
		cmds << zwave.associationV1.associationSet(groupingIdentifier:2, nodeId:zwaveHubNodeId).format()
		cmds << secure(zwave.associationV1.associationSet(groupingIdentifier:1, nodeId:zwaveHubNodeId))
		cmds << zwave.associationV1.associationGet(groupingIdentifier:2).format()
		cmds << secure(zwave.associationV1.associationGet(groupingIdentifier:1))
		state.associationQuery = now()
	}
	log.debug "refresh sending ${cmds.inspect()}"
	cmds
}

def poll() {
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
	if (cmds) {
		log.debug "poll is sending ${cmds.inspect()}"
		cmds
	} else {
		// workaround to keep polling from stopping due to lack of activity
		sendEvent(descriptionText: "skipping poll", isStateChange: true, displayed: false)
		null
	}
}

def requestCode(codeNumber) {
	secure(zwave.userCodeV1.userCodeGet(userIdentifier: codeNumber))
}

def reloadAllCodes() {
	def cmds = []
	if (!state.codes) {
		state.requestCode = 1
		cmds << secure(zwave.userCodeV1.usersNumberGet())
	} else {
		if(!state.requestCode) state.requestCode = 1
		cmds << requestCode(codeNumber)
	}
	cmds
}

def setCode(codeNumber, code) {
	def strcode = code
	log.debug "setting code $codeNumber to $code"
	if (code instanceof String) {
		code = code.toList().findResults { if(it > ' ' && it != ',' && it != '-') it.toCharacter() as Short }
	} else {
		strcode = code.collect{ it as Character }.join()
	}
	if (state.blankcodes) {
		// Can't just set, we won't be able to tell if it was successful
		if (state["code$codeNumber"] != "") {
			if (state["setcode$codeNumber"] != strcode) {
				state["resetcode$codeNumber"] = strcode
				return deleteCode(codeNumber)
			}
		} else {
			state["setcode$codeNumber"] = strcode
		}
	}
	secureSequence([
		zwave.userCodeV1.userCodeSet(userIdentifier:codeNumber, userIdStatus:1, user:code),
		zwave.userCodeV1.userCodeGet(userIdentifier:codeNumber)
	], 7000)
}

def deleteCode(codeNumber) {
	log.debug "deleting code $codeNumber"
	secureSequence([
		zwave.userCodeV1.userCodeSet(userIdentifier:codeNumber, userIdStatus:0),
		zwave.userCodeV1.userCodeGet(userIdentifier:codeNumber)
	], 7000)
}

def updateCodes(codeSettings) {
	if(codeSettings instanceof String) codeSettings = util.parseJson(codeSettings)
	def set_cmds = []
	def get_cmds = []
	codeSettings.each { name, updated ->
		def current = decrypt(state[name])
		if (name.startsWith("code")) {
			def n = name[4..-1].toInteger()
			log.debug "$name was $current, set to $updated"
			if (updated?.size() >= 4 && updated != current) {
				def cmds = setCode(n, updated)
				set_cmds << cmds.first()
				get_cmds << cmds.last()
			} else if ((current && updated == "") || updated == "0") {
				def cmds = deleteCode(n)
				set_cmds << cmds.first()
				get_cmds << cmds.last()
			} else if (updated && updated.size() < 4) {
				// Entered code was too short
				codeSettings["code$n"] = current
			}
		} else log.warn("unexpected entry $name: $updated")
	}
	if (set_cmds) {
		return response(delayBetween(set_cmds, 2200) + ["delay 2200"] + delayBetween(get_cmds, 4200))
	}
}

def getCode(codeNumber) {
	decrypt(state["code$codeNumber"])
}

def getAllCodes() {
	state.findAll { it.key.startsWith 'code' }.collectEntries {
		[it.key, (it.value instanceof String && it.value.startsWith("~")) ? decrypt(it.value) : it.value]
	}
}

private secure(physicalgraph.zwave.Command cmd) {
	zwave.securityV1.securityMessageEncapsulation().encapsulate(cmd).format()
}

private secureSequence(commands, delay=4200) {
	delayBetween(commands.collect{ secure(it) }, delay)
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

private allCodesDeleted() {
	if (state.codes instanceof Integer) {
		(1..state.codes).each { n ->
			if (state["code$n"]) {
				result << createEvent(name: "codeReport", value: n, data: [ code: "" ], descriptionText: "code $n was deleted",
					displayed: false, isStateChange: true)
			}
			state["code$n"] = ""
		}
	}
}
