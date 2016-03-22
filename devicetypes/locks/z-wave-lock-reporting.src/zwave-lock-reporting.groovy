metadata {
  // Automatically generated. Make future change here.
  definition (name: "Z-Wave Lock Reporting", namespace: "Locks", author: "SmartThings") {
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

  tiles {
    standardTile("toggle", "device.lock", width: 2, height: 2) {
      state "locked", label:'locked', action:"lock.unlock", icon:"st.locks.lock.locked", backgroundColor:"#79b821", nextState:"unlocking"
      state "unlocked", label:'unlocked', action:"lock.lock", icon:"st.locks.lock.unlocked", backgroundColor:"#ffffff", nextState:"locking"
      state "unknown", label:"unknown", action:"lock.lock", icon:"st.locks.lock.unknown", backgroundColor:"#ffffff", nextState:"locking"
      state "locking", label:'locking', icon:"st.locks.lock.locked", backgroundColor:"#79b821"
      state "unlocking", label:'unlocking', icon:"st.locks.lock.unlocked", backgroundColor:"#ffffff"
    }
    standardTile("lock", "device.lock", inactiveLabel: false, decoration: "flat") {
      state "default", label:'lock', action:"lock.lock", icon:"st.locks.lock.locked", nextState:"locking"
    }
    standardTile("unlock", "device.lock", inactiveLabel: false, decoration: "flat") {
      state "default", label:'unlock', action:"lock.unlock", icon:"st.locks.lock.unlocked", nextState:"unlocking"
    }
    valueTile("battery", "device.battery", inactiveLabel: false, decoration: "flat") {
      state "battery", label:'${currentValue}% battery', unit:""
    }
    standardTile("refresh", "device.lock", inactiveLabel: false, decoration: "flat") {
      state "default", label:'', action:"refresh.refresh", icon:"st.secondary.refresh"
    }

    main "toggle"
    details(["toggle", "lock", "unlock", "battery", "refresh"])
  }
}

import physicalgraph.zwave.commands.doorlockv1.*
import physicalgraph.zwave.commands.usercodev1.*

def parse(String description) {
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
    def cmd = zwave.parse(description, [ 0x98: 1, 0x72: 2, 0x85: 2 ])
    if (cmd) {
      result = zwaveEvent(cmd)
    }
  }
  log.debug "\"$description\" parsed to ${result.inspect()}"
  result
}

def zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityMessageEncapsulation cmd) {
  def encapsulatedCommand = cmd.encapsulatedCommand([0x62: 1, 0x71: 2, 0x80: 1, 0x85: 2, 0x63: 1, 0x98: 1])
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
        map = [ name: "codeChanged", value: "all", descriptionText: "$device.displayName: all user codes deleted", displayed: true ]
        allCodesDeleted()
        break
      case 0xD:
        if (cmd.eventParameter) {
          map = [ name: "codeReport", value: cmd.eventParameter[0], data: [ code: "" ], displayed: true ]
          map.descriptionText = "$device.displayName code ${map.value} was deleted"
          map.isStateChange = (state["code$map.value"] != "")
          state["code$map.value"] = ""
        } else {
          map = [ name: "codeChanged", descriptionText: "$device.displayName: user code deleted", displayed: true ]
        }
        break
      case 0xE:
        map = [ name: "codeChanged", value: cmd.alarmLevel,  descriptionText: "$device.displayName: user code added", displayed: true ]
        if (cmd.eventParameter) {
          map.value = cmd.eventParameter[0]
          result << response(requestCode(cmd.eventParameter[0]))
        }
        break
      default:
        map = map ?: [ descriptionText: "$device.displayName: alarm event $cmd.zwaveAlarmEvent", display: false ]
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
      map = [ name: "codeChanged", value: cmd.alarmLevel, descriptionText: "$device.displayName code $cmd.alarmLevel was added", displayed: true ]
      result << response(requestCode(cmd.alarmLevel))
      break
    case 32:
      map = [ name: "codeChanged", value: "all", descriptionText: "$device.displayName: all user codes deleted", displayed: true ]
      allCodesDeleted()
    case 33:
      map = [ name: "codeReport", value: cmd.alarmLevel, data: [ code: "" ], displayed: true ]
      map.descriptionText = "$device.displayName code $cmd.alarmLevel was deleted"
      map.isStateChange = (state["code$cmd.alarmLevel"] != "")
      state["code$cmd.alarmLevel"] = ""
      break
    case 112:
      map = [ name: "codeChanged", value: cmd.alarmLevel, descriptionText: "$device.displayName code $cmd.alarmLevel changed", displayed: true ]
      result << response(requestCode(cmd.alarmLevel))
      break
    case 130:  // Yale YRD batteries replaced
      map = [ descriptionText: "$device.displayName batteries replaced", displayed: true ]
      break
    case 131:
      map = [ /*name: "codeChanged", value: cmd.alarmLevel,*/ descriptionText: "$device.displayName code $cmd.alarmLevel is duplicate", displayed: false ]
    case 161:
      if (cmd.alarmLevel == 2) {
        map = [ descriptionText: "$device.displayName front escutcheon removed", isStateChange: true ]
      } else {
        map = [ descriptionText: "$device.displayName detected failed user code attempt", isStateChange: true ]
      }
      break
    case 167:
      if (!state.lastbatt || (new Date().time) - state.lastbatt > 12*60*60*1000) {
        map = [ descriptionText: "$device.displayName: battery low", displayed: true ]
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
      code = state["set$name"] ?: state[name] ?: code
      state.remove("set$name".toString())
    }
    if (!code && cmd.userIdStatus == 1) {  // Schlage touchscreen sends blank code to notify of a changed code
      map = [ name: "codeChanged", value: cmd.userIdentifier, displayed: true, isStateChange: true ]
      map.descriptionText = "$device.displayName code $cmd.userIdentifier " + (state[name] ? "changed" : "was added")
      code = state["set$name"] ?: state[name] ?: "****"
      state.remove("set$name".toString())
    } else {
      map = [ name: "codeReport", value: cmd.userIdentifier, data: [ code: code ] ]
      map.descriptionText = "$device.displayName code $cmd.userIdentifier is set"
      map.displayed = (cmd.userIdentifier != state.requestCode && cmd.userIdentifier != state.pollCode)
      map.isStateChange = (code != state[name])
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
  state[name] = code

  if (cmd.userIdentifier == state.requestCode) {  // reloadCodes() was called, keep requesting the codes in order
    if (state.requestCode + 1 > state.codes) {
      state.remove("requestCode")  // done
    } else {
      state.requestCode = state.requestCode + 1  // get next
      result << response(requestCode(state.requestCode))
    }
  }
  if (cmd.userIdentifier == state.pollCode) {
    if (state.pollCode + 1 > state.codes) {
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
  state.lastbatt = new Date().time
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
  def text = "$device.displayName: firmware version: $fw, Z-Wave version: ${cmd.zWaveProtocolVersion}.${cmd.zWaveProtocolSubVersion}"
  createEvent(descriptionText: text, isStateChange: false)
}

def zwaveEvent(physicalgraph.zwave.commands.applicationstatusv1.ApplicationBusy cmd) {
  def msg = cmd.status == 0 ? "try again later" :
            cmd.status == 1 ? "try again in $cmd.waitTime seconds" :
            cmd.status == 2 ? "request queued" : "sorry"
  createEvent(displayed: false, descriptionText: "$device.displayName is busy, $msg")
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
  cmds
}

def poll() {
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
    if (!latest || !secondsPast(latest, 6 * 60) || secondsPast(state.lastPoll, 67 * 60)) {
      cmds << secure(zwave.doorLockV1.doorLockOperationGet())
      state.lastPoll = (new Date()).time
    } else if (!state.codes) {
      state.pollCode = 1
      cmds << secure(zwave.userCodeV1.usersNumberGet())
    } else if (state.pollCode && state.pollCode <= state.codes) {
      cmds << requestCode(state.pollCode)
    } else if (!state.lastbatt || (new Date().time) - state.lastbatt > 53*60*60*1000) {
      cmds << secure(zwave.batteryV1.batteryGet())
    }
    if(cmds) cmds << "delay 6000"
  }
  log.debug "poll is sending ${cmds.inspect()}, state: ${state.inspect()}"
  reportAllCodes(state)
  device.activity()  // workaround to keep polling from being shut off
  cmds ?: null
}

def reportAllCodes(state) {
  def map = [ name: "reportAllCodes", data: state, displayed: false, isStateChange: false, type: "physical" ]
  sendEvent(map)
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
    if (state["code$codeNumber"] != "") {  // Can't just set, we won't be able to tell if it was successful
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
    def current = state[name]
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
  state["code$codeNumber"]
}

def getAllCodes() {
  state.findAll { it.key.startsWith 'code' }
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