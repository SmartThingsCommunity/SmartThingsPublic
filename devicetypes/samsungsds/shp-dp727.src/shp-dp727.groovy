metadata {
  // Automatically generated. Make future change here.
  definition (name: "SHP-DP727", namespace: "SamsungSDS", author: "Kunwoo Kim") {
    capability "Actuator"
    capability "Lock"
    capability "Polling"
    capability "Refresh"
    capability "Sensor"
    capability "Lock Codes"
    capability "Battery"
    capability "Alarm"

	command "unlockwtimeout"
    command "reset"


    fingerprint deviceId: "0x4003", inClusters: "0x98"
    fingerprint deviceId: "0x4004", inClusters: "0x98"
    
  }
 
  simulator {
    status "locked": "command: 9881, payload: 00 62 03 FF 00 00 FE FE"
    status "unlocked": "command: 9881, payload: 00 62 03 00 00 00 FE FE"

    reply "9881006201FF,delay 4200,9881006202": "command: 9881, payload: 00 62 03 FF 00 00 FE FE"
    reply "988100620100,delay 4200,9881006202": "command: 9881, payload: 00 62 03 00 00 00 FE FE"
  }
  
  tiles(scale:2) {
	standardTile("main", "device.lock", width: 6, height: 3) {
      state "locked", label:'locked', icon:"st.samsung_sds.main_door_status_lock", backgroundColor:"#fffffe"
      state "unlocked", label:'unlocked', icon:"st.samsung_sds.main_door_status_unlock", backgroundColor:"#fffffe"
      state "unknown", label:"unknown", icon:"st.samsung_sds.main_door_status_unknown", backgroundColor:"#fffffe"
      state "unlockTimeout", label:'autolock error', icon:"st.samsung_sds.main_door_status_unlock", backgroundColor:"#fffffe"
    }
    standardTile("sub", "device.lock", width: 6, height: 3) {
      state "locked", label:'locked', icon:"st.samsung_sds.sub_door_status_lock", backgroundColor:"#fffffe"
      state "unlocked", label:'unlocked', icon:"st.samsung_sds.sub_door_status_unlock", backgroundColor:"#fffffe"
      state "unknown", label:"unknown", icon:"st.samsung_sds.sub_door_status_unknown", backgroundColor:"#fffffe"
      state "unlockTimeout", label:'autolock error', icon:"st.samsung_sds.sub_door_status_unlock", backgroundColor:"#fffffe"
    }
    
	standardTile("lock", "device.lock", type:"generic", width: 3, height: 2, inactiveLabel: false, decoration: "flat") {
      state "default", label:'lock', action:"lock.lock", icon:"st.samsung_sds.main_door_btn_lock", nextState:"locking"
       
    }
    standardTile("unlock", "device.lock", inactiveLabel: false, width: 3, height: 2, decoration: "flat") {
		state "default", label:'unlock', action:"lock.unlock", icon:"st.samsung_sds.main_door_btn_unlock"
    }
    standardTile("refresh", "device.lock", inactiveLabel: false, width: 3, height: 2, decoration: "flat") {
      state "default", label:'refresh', action:"refresh.refresh", icon:"st.samsung_sds.main_door_btn_refresh"
    }
	standardTile("battery", "device.battery", inactiveLabel: false, width: 3, height: 2, decoration: "flat") {
		state "battery", label:'${currentValue}% battery', unit:"", icon:"st.samsung_sds.main_door_btn_battery_nor"
        state "low_battery", label:'${currentValue}% battery', unit:"", icon:"st.samsung_sds.main_door_btn_battery_error"
	}
   
    standardTile("tamper", width: 3, height: 2,"device.alarm", decoration:"flat") {
      state("secure", label:'secure',    icon:"st.samsung_sds.sub_door_status_secu_idle",  backgroundColor:"#ffffff")
      state("siren", label:'tampered',  action:"reset", icon:"st.samsung_sds.sub_door_status_secu_tamper", backgroundColor:"#53a7c0")
    }

    main "sub"
	details(["main", "lock", "unlock", "refresh", "battery"])
  }
}

import physicalgraph.zwave.commands.doorlockv1.*
import physicalgraph.zwave.commands.usercodev1.*


def reset() {
	log.debug "reset alarm state"
	sendEvent(displayed: true,  isStateChange: true, name: "alarm", value: "secure", descriptionText: "$device.displayName clear alarm mode to secure state")
}

def parse(String description) {
	log.debug "parse : ${description}"
	def result = null
    if (description.startsWith("Err")) {
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
    } else {
        def cmd = zwave.parse(description, [ 0x98: 1, 0x72: 2, 0x85: 2, 0x86: 1 ])
        if (cmd) {
            log.debug "[parse] cmd : ${cmd}"
            result = zwaveEvent(cmd)
        }
    }
	log.debug "\"$description\" parsed to ${result.inspect()}"
	result
}

def zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityMessageEncapsulation cmd) {
  def encapsulatedCommand = cmd.encapsulatedCommand([0x62: 1, 0x71: 2, 0x80: 1, 0x85: 2, 0x63: 1, 0x98: 1, 0x86: 1])
  log.debug "encapsulated: $encapsulatedCommand"
  if (encapsulatedCommand) {
    zwaveEvent(encapsulatedCommand)
  }
}

def zwaveEvent(physicalgraph.zwave.commands.securityv1.NetworkKeyVerify cmd) {
  log.debug "[NetworkKeyVerify] Secure inclusion was successful"
  createEvent(name:"secureInclusion", value:"success", descriptionText:"Secure inclusion was successful")
}

def zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityCommandsSupportedReport cmd) {
  state.sec = cmd.commandClassSupport.collect { String.format("%02X ", it) }.join()
  if (cmd.commandClassControl) {
    state.secCon = cmd.commandClassControl.collect { String.format("%02X ", it) }.join()
  }
  log.debug "Security command classes: $state.sec  (Lock is securely included)"
  createEvent(name:"secureInclusion", value:"success", descriptionText:"Lock is securely included")
}

def zwaveEvent(DoorLockOperationReport cmd) {
  def result = []
  def map = [ name: "lock" ]
  log.debug "[DoorLockOperationReport] cmd.doorLockMode : ${cmd.doorLockMode}"
  log.debug "[DoorLockOperationReport] cmd.doorCondition : ${cmd.doorCondition}"
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

  log.debug "[AlarmReport] cmd.zwaveAlarmType : ${cmd.zwaveAlarmType}"
  log.debug "[AlarmReport] cmd.zwaveAlarmEvent : ${cmd.zwaveAlarmEvent}"
  log.debug "[AlarmReport] cmd.alarmType : ${cmd.alarmType}"

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
      case 3:	// locked by ST command
        map.descriptionText = "$device.displayName was locked by remote command"
        break
      case 4:	// unlocked by ST command
        map.descriptionText = "$device.displayName was unlocked by remote command"
        break
      case 5:
        if (cmd.eventParameter) {
          map.descriptionText = "$device.displayName was locked with code ${cmd.eventParameter[2]}"
          map.data = [ usedCode: cmd.eventParameter[2] ]
        }
        break
      case 6:
        if (cmd.eventParameter) {
          map.descriptionText = "$device.displayName was unlocked with code ${cmd.eventParameter[2]}"
          map.data = [ usedCode: cmd.eventParameter[2] ]
        }
        break
      case 9:
        map.descriptionText = "$device.displayName was autolocked"
        break
      case 7:
      case 8:
      case 0xA:
        map = [ name: "lock", value: "unknown", descriptionText: "$device.displayName was not locked fully" ]
        map.value = "unlockTimeout"
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
          map = [ name: "codeReport", value: cmd.eventParameter[2], data: [ code: "" ], isStateChange: true ]
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
          map.value = cmd.eventParameter[2]
          result << response(requestCode(cmd.eventParameter[2]))
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
    map = [ name: "alarm", value: "siren", displayed: true ]
    switch (cmd.zwaveAlarmEvent) {
      case 0:
        map.value = "clear"
        
        map.descriptionText = "tamper alert cleared"
        break
      case 1:
      case 2:
      	map.type = "tamper"
        map.descriptionText = "intrusion attempt detected"
        break
      case 3:
        map.descriptionText = "covering removed"
        break
      case 4:
      	map.type = "pinCode"
        map.descriptionText = "invalid code"
        break
      default:
        map.descriptionText = "tamper alarm $cmd.zwaveAlarmEvent"
        break
    }
  } else if (cmd.zwaveAlarmType == 10) {
  	map = [ name: "alarm", value: "siren", displayed: true ]
    switch (cmd.zwaveAlarmEvent) {
      case 1:
      	map.type = "emergency"
        map.descriptionText = "Urgent danger alarm occurred!"
        break
      case 2:
      	map.type = "fire"
        map.descriptionText = "The fire has been detected!"
        break
      default:
        map.descriptionText = "Emergency Alarm $cmd.zwaveAlarmEvent"
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
      log.debug "[AlarmReport] cmd.alarmLevel : ${cmd.alarmLevel}"
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
      map = [ descriptionText: "$device.displayName code $cmd.alarmLevel is duplicate", isStateChange: false ]
      break
    case 161:
      if (cmd.alarmLevel == 2) {
        map = [ descriptionText: "$device.displayName front escutcheon removed", isStateChange: true ]
      } else {
        map = [ descriptionText: "$device.displayName detected failed user code attempt", isStateChange: true ]
      }
      break
    case 167:
      if (!state.lastbatt || (new Date().time) - state.lastbatt > 12*60*60*1000) {
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
  

 	if(map.value == "siren"){
    	// 5초 후에 자동으로 알람 상태가 리셋되도록 함. 계속 알람 상태가 되어 있으면 이후 발생하는 이벤트에 대해 push 발생 안함
        def cmds = []
		cmds << "reset"
        cmds
        sendEvent(displayed: true,  isStateChange: true, name: "alarm", value: "secure", descriptionText: "$device.displayName clear alarm mode to secure state")
	  }

  result ? [createEvent(map), *result] : createEvent(map)
}

def zwaveEvent(UserCodeReport cmd) {
  def result = []
  def name = "code$cmd.userIdentifier"
  def code = cmd.code
  def map = [:]
  log.debug "[UserCodeReport] cmd.code : ${cmd.code}"
  log.debug "[UserCodeReport] cmd.userIdStatus : ${cmd.userIdStatus}"
  log.debug "[UserCodeReport] cmd.userIdentifier : ${cmd.userIdentifier}"
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
      map.isStateChange = (code != decrypt(state[name]))
    }
    result << createEvent(map)
  } else {
    map = [ name: "codeReport", value: cmd.userIdentifier, data: [ code: "" ] ]
    if (state.blankcodes && state["reset$name"]) {  // we deleted this code so we can tell that our new code gets set
      map.descriptionText = "$device.displayName code $cmd.userIdentifier was reset"
      map.displayed = map.isStateChange = false
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
      map.isStateChange = state[name] as Boolean
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
  log.debug "[UsersNumberReport] cmd.supportedUsers : ${cmd.supportedUsers}"
  if (state.requestCode && state.requestCode <= cmd.supportedUsers) {
    result << response(requestCode(state.requestCode))
  }
  result
}

def zwaveEvent(physicalgraph.zwave.commands.associationv2.AssociationReport cmd) {
  def result = []
  log.debug "[AssociationReport] cmd.nodeId : ${cmd.nodeId}"
  log.debug "[AssociationReport] cmd.groupingIdentifier : ${cmd.groupingIdentifier}"
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
  log.debug "[BasicSet] cmd.value : ${cmd.value}"
  result << response(zwave.associationV1.associationRemove(groupingIdentifier:1, nodeId:zwaveHubNodeId))
  if (state.assoc != zwaveHubNodeId) {
    result << response(zwave.associationV1.associationGet(groupingIdentifier:2))
  }
  result
}

def zwaveEvent(physicalgraph.zwave.commands.batteryv1.BatteryReport cmd) {
  def map = [ name: "battery", unit: "%" ]
  log.debug "[BatteryReport] cmd.batteryLevel : ${cmd.batteryLevel}"
  if (cmd.batteryLevel == 0xFF) {
    map.value = 1
    map.descriptionText = "$device.displayName has a low battery"
  } else {
    map.value = cmd.batteryLevel
  }
  state.lastbatt = new Date().time
  createEvent(map)
}

def zwaveEvent(physicalgraph.zwave.commands.manufacturerspecificv2.ManufacturerSpecificReport cmd) {
  def result = []

  log.debug "[ManufacturerSpecificReport] cmd.manufacturerId : ${cmd.manufacturerId}"
  log.debug "[ManufacturerSpecificReport] cmd.productTypeId : ${cmd.productTypeId}"
  log.debug "[ManufacturerSpecificReport] cmd.productId : ${cmd.productId}"
  def msr = String.format("%04X-%04X-%04X", cmd.manufacturerId, cmd.productTypeId, cmd.productId)
  log.debug "msr: $msr"
  updateDataValue("MSR", msr)

  result << createEvent(descriptionText: "$device.displayName MSR: $msr", isStateChange: false)
  result
}

def zwaveEvent(physicalgraph.zwave.commands.versionv1.VersionReport cmd) {
  def fw = "${cmd.applicationVersion}.${cmd.applicationSubVersion}"
  log.debug "[VersionReport] cmd.applicationSubVersion : ${cmd.applicationSubVersion}"
  updateDataValue("fw", fw)
  if (state.MSR == "003B-6341-5044") {
    updateDataValue("ver", "${cmd.applicationVersion >> 4}.${cmd.applicationVersion & 0xF}")
  }
  def text = "$device.displayName: firmware version: $fw, Z-Wave version: ${cmd.zWaveProtocolVersion}.${cmd.zWaveProtocolSubVersion}"
  createEvent(descriptionText: text, isStateChange: false)
}

def zwaveEvent(physicalgraph.zwave.commands.applicationstatusv1.ApplicationBusy cmd) {
  log.debug "[ApplicationBusy] cmd.status : ${cmd.status}"
  def msg = cmd.status == 0 ? "try again later" :
            cmd.status == 1 ? "try again in $cmd.waitTime seconds" :
            cmd.status == 2 ? "request queued" : "sorry"
  createEvent(displayed: true, descriptionText: "$device.displayName is busy, $msg")
}

def zwaveEvent(physicalgraph.zwave.commands.applicationstatusv1.ApplicationRejectedRequest cmd) {
  log.debug "[ApplicationRejectedRequest]"
  createEvent(displayed: true, descriptionText: "$device.displayName rejected the last request")
}

def zwaveEvent(physicalgraph.zwave.commands.configurationv1.ConfigurationReport cmd) {
  log.debug "[ConfigurationReport v1]"
  createEvent(displayed: true, descriptionText: "$device.displayName rejected the last request")
}

def zwaveEvent(physicalgraph.zwave.commands.configurationv2.ConfigurationReport cmd) {
  log.debug "[ConfigurationReport] cmd : ${cmd}"
  def paramNumber = cmd.parameterNumber
  def value = cmd.configurationValue
  
  log.debug "[ConfigurationReport] paramNumber : ${paramNumber},  value : ${value}"
  def map = null
  

  switch(paramNumber){
  	case 1:
    	map = [name: "notification", value: "notificationInfo", descriptionText : "SecurityMode Set : ${value}"]
        map.displayed = true
    	break
    case 2:
    	map = [name: "notification", value: "notificationAlert", descriptionText : "SafetyLock Set : ${value}"]
        map.displayed = true
    	break
  }
  createEvent(map)
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
  log.debug "[Command] cmd : ${cmd}"
  createEvent(displayed: false, descriptionText: "$device.displayName: $cmd")
}


def lockAndCheck(doorLockMode) {
  log.debug "[lockAndCheck]"
  secureSequence([
    zwave.doorLockV1.doorLockOperationSet(doorLockMode: doorLockMode),
    zwave.doorLockV1.doorLockOperationGet()
  ], 4200)
}

def lock() {
  log.debug "[lock]"
  lockAndCheck(DoorLockOperationSet.DOOR_LOCK_MODE_DOOR_SECURED)
}

def unlock() {
  log.debug "[unlock]"
  lockAndCheck(DoorLockOperationSet.DOOR_LOCK_MODE_DOOR_UNSECURED)
}

def unlockwtimeout() {
  log.debug "[unlockwtimeout]"
  lockAndCheck(DoorLockOperationSet.DOOR_LOCK_MODE_DOOR_UNSECURED_WITH_TIMEOUT)
}

def initialize() {
    log.trace "initialize()"
}
    
def installed() {
    log.trace "installed()"
    initialize()
	refresh()
}

def uninstalled() {
    log.trace "uninstall()"
}

def refresh() {
  log.debug "[refresh]"
  def cmds = [secure(zwave.doorLockV1.doorLockOperationGet())]
  cmds << secure(zwave.batteryV1.batteryGet())
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
  log.debug "refresh sending ${cmds.inspect()}"
  log.debug "refresh sending ${cmds}"
  cmds
}

def poll() {
  refresh()
  def cmds = []
  if (state.assoc != zwaveHubNodeId && secondsPast(state.associationQuery, 19 * 60)) {
    log.debug "setting association"
    cmds << zwave.associationV1.associationSet(groupingIdentifier:2, nodeId:zwaveHubNodeId).format()
    cmds << secure(zwave.associationV1.associationSet(groupingIdentifier:1, nodeId:zwaveHubNodeId))
    cmds << zwave.associationV1.associationGet(groupingIdentifier:2).format()
    cmds << "delay 6000"
    cmds << secure(zwave.associationV1.associationGet(groupingIdentifier:1))
    cmds << "delay 6000"
    state.associationQuery = new Date().time
  } else {
    // Only check lock state if it changed recently or we haven't had an update in an hour
    def latest = device.currentState("lock")?.date?.time
    if (!latest || !secondsPast(latest, 6 * 60) || secondsPast(state.lastPoll, 55 * 60)) {
      cmds << secure(zwave.doorLockV1.doorLockOperationGet())
      state.lastPoll = (new Date()).time
    } else if (!state.MSR) {
      cmds << zwave.manufacturerSpecificV1.manufacturerSpecificGet().format()
    } else if (!state.fw) {
      cmds << zwave.versionV1.versionGet().format()
    } else if (!state.codes) {
      state.pollCode = 1
      cmds << secure(zwave.userCodeV1.usersNumberGet())
    } else if (state.pollCode && state.pollCode <= state.codes) {
      cmds << requestCode(state.pollCode)
    } else if (!state.lastbatt || (new Date().time) - state.lastbatt > 53*60*60*1000) {
      cmds << secure(zwave.batteryV1.batteryGet())
    } else if (!state.enc) {
      encryptCodes()
      state.enc = 1
    }
  }
  log.debug "poll is sending ${cmds.inspect()}"
  // Send code data as event
  reportAllCodes(state)
  device.activity()
  cmds ?: null
}

private def encryptCodes() {
  def keys = new ArrayList(state.keySet().findAll { it.startsWith("code") })
  keys.each { key ->
    def match = (key =~ /^code(\d+)$/)
    if (match) try {
      def keynum = match[0][1].toInteger()
      if (keynum > 30 && !state[key]) {
        state.remove(key)
      } else if (state[key] && !state[key].startsWith("~")) {
        log.debug "encrypting $key: ${state[key].inspect()}"
        state[key] = encrypt(state[key])
      }
    } catch (java.lang.NumberFormatException e) { }
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
  log.error("[updateCodes]")
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
  return (new Date().time - timestamp) > (seconds * 1000)
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

def reportAllCodes(state) {
  def map = [ name: "reportAllCodes", data: [:], displayed: false, isStateChange: false, type: "physical" ]
  state.each { entry ->
    //iterate through all the state entries and add them to the event data to be handled by application event handlers
    if ( entry.key ==~ /^code\d{1,}/ && entry.value.startsWith("~") ) {
      map.data.put(entry.key, decrypt(entry.value))
    } else {
      map.data.put(entry.key, entry.value)
    }
  }
  sendEvent(map)
}