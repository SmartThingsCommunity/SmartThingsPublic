/**
 *  User Lock Manager v4.1.5
 *
 *  Copyright 2015 Erik Thayer
 *  Keypad support added by BLebson
 *
 */
definition(
  name: "User Lock Manager",
  namespace: "ethayer",
  author: "Erik Thayer",
  description: "This app allows you to change, delete, and schedule user access.",
  category: "Safety & Security",
  iconUrl: "https://dl.dropboxusercontent.com/u/54190708/LockManager/lockmanager.png",
  iconX2Url: "https://dl.dropboxusercontent.com/u/54190708/LockManager/lockmanagerx2.png",
  iconX3Url: "https://dl.dropboxusercontent.com/u/54190708/LockManager/lockmanagerx3.png")

  import groovy.json.JsonSlurper
  import groovy.json.JsonBuilder

preferences {
  page(name: "rootPage")
  page(name: "setupPage")
  page(name: "userPage")
  page(name: "notificationPage")
  page(name: "onUnlockPage")
  page(name: "schedulingPage")
  page(name: "calendarPage")
  page(name: "resetAllCodeUsagePage")
  page(name: "resetCodeUsagePage")
  page(name: "reEnableUserPage")
  page(name: "infoPage")
  page(name: "keypadPage")
  page(name: "infoRefreshPage")
  page(name: "lockInfoPage")
}

def rootPage() {
  //reset errors on each load
  dynamicPage(name: "rootPage", title: "", install: true, uninstall: true) {

    section("Which Locks?") {
      input "theLocks","capability.lockCodes", title: "Select Locks", required: true, multiple: true, submitOnChange: true
    }

    if (theLocks) {
      initalizeLockData()

      section {
        input name: "maxUsers", title: "Number of users", type: "number", multiple: false, refreshAfterSelection: true, submitOnChange: true
        href(name: "toSetupPage", title: "User Settings", page: "setupPage", description: setupPageDescription(), state: setupPageDescription() ? "complete" : "")
        href(name: "toInfoPage", page: "infoPage", title: "Lock Info")
        href(name: "toKeypadPage", page: "keypadPage", title: "Keypad Info (optional)")
        href(name: "toNotificationPage", page: "notificationPage", title: "Notification Settings", description: notificationPageDescription(), state: notificationPageDescription() ? "complete" : "")
        href(name: "toSchedulingPage", page: "schedulingPage", title: "Schedule (optional)", description: schedulingHrefDescription(), state: schedulingHrefDescription() ? "complete" : "")
        href(name: "toOnUnlockPage", page: "onUnlockPage", title: "Global Hello Home")
      }
      section {
        label(title: "Label this SmartApp", required: false, defaultValue: "")
      }
    }
  }
}

def setupPage() {
  dynamicPage(name:"setupPage", title:"User Settings") {
    if (maxUsers > 0) {
      section('Users') {
        (1..maxUsers).each { user->
          if (!state."userState${user}") {
            //there's no values, so reset
            resetCodeUsage(user)
          }
          if (settings."userCode${user}" && settings."userSlot${user}") {
            getConflicts(settings."userSlot${user}")
          }
          href(name: "toUserPage${user}", page: "userPage", params: [number: user], required: false, description: userHrefDescription(user), title: userHrefTitle(user), state: userPageState(user) )
        }
      }
      section {
        href(name: "toResetAllCodeUsage", title: "Reset Code Usage", page: "resetAllCodeUsagePage", description: "Tap to reset")
      }
    } else {
      section("Users") {
        paragraph "Users are set to zero.  Please go back to the main page and change the number of users to at least 1."
      }
    }
  }
}

def userPage(params) {
  dynamicPage(name:"userPage", title:"User Settings") {
    def i = getUser(params);

    if (!state."userState${i}".enabled) {
      section {
        paragraph "WARNING:\n\nThis user has been disabled.\nReason: ${state."userState${i}".disabledReason}"
        href(name: "toreEnableUserPage", title: "Reset User", page: "reEnableUserPage", params: [number: i], description: "Tap to reset")
      }
    }
    if (settings."userCode${i}" && settings."userSlot${i}") {
      def conflict = getConflicts(settings."userSlot${i}")
      if (conflict.has_conflict) {
        section("Conflicts:") {
          theLocks.each { lock->
            if (conflict."lock${lock.id}" && conflict."lock${lock.id}".conflicts != []) {
              paragraph "${lock.displayName} slot ${fancyString(conflict."lock${lock.id}".conflicts)}"
            }
          }
        }
      }
    }
    section("Code #${i}") {
      input(name: "userName${i}", type: "text", title: "Name for User", defaultValue: settings."userName${i}")
      def title = "Code (4 to 8 digits)"
      theLocks.each { lock->
        if (lock.hasAttribute('pinLength')) {
          title = "Code (Must be ${lock.latestValue('pinLength')} digits)"
        }
      }
      input(name: "userCode${i}", type: "text", title: title, required: false, defaultValue: settings."userCode${i}", refreshAfterSelection: true)
      input(name: "userSlot${i}", type: "number", title: "Slot (1 through 30)", defaultValue: preSlectedCode(i))
    }
    section {
      input(name: "dontNotify${i}", title: "Mute entry notification?", type: "bool", required: false, defaultValue: settings."dontNotify${i}")
      input(name: "burnCode${i}", title: "Burn after use?", type: "bool", required: false, defaultValue: settings."burnCode${i}")
      input(name: "userEnabled${i}", title: "Enabled?", type: "bool", required: false, defaultValue: settings."userEnabled${i}")
      def hhPhrases = location.getHelloHome()?.getPhrases()*.label
      if (hhPhrases) {
        hhPhrases.sort()
        input name: "userHomePhrases${i}", type: "enum", title: "Hello Home Phrase", multiple: true,required: false, options: hhPhrases, defaultValue: settings."userHomePhrases${i}", refreshAfterSelection: true
        input "userNoRunPresence${i}", "capability.presenceSensor", title: "Don't run Actions if any of these are present:", multiple: true, required: false, defaultValue: settings."userNoRunPresence${i}" || false
        input "userDoRunPresence${i}", "capability.presenceSensor", title: "Run Actions only if any of these are present:", multiple: true, required: false, defaultValue: settings."userDoRunPresence${i}" || false
      }
    }
    section {
      href(name: "toSetupPage", title: "Back To Users", page: "setupPage")
      href(name: "toResetCodeUsagePage", title: "Reset Code Usage", page: "resetCodeUsagePage", params: [number: i], description: "Tap to reset")
    }
  }
}

def preSlectedCode(i) {
  if (settings."userSlot${i}" != null) {
    return settings."userSlot${i}"
  } else {
    return i
  }
}

def notificationPage() {
  dynamicPage(name: "notificationPage", title: "Notification Settings") {

    section {
      input(name: "phone", type: "text", title: "Text This Number", description: "Phone number", required: false, submitOnChange: true)
      paragraph "For multiple SMS recipients, separate phone numbers with a semicolon(;)"
      input(name: "notification", type: "bool", title: "Send A Push Notification", description: "Notification", required: false, submitOnChange: true)
      if (phone != null || notification || sendevent) {
        input(name: "notifyAccess", title: "on User Entry", type: "bool", required: false)
        input(name: "notifyLock", title: "on Lock", type: "bool", required: false)
        input(name: "notifyAccessStart", title: "when granting access", type: "bool", required: false)
        input(name: "notifyAccessEnd", title: "when revoking access", type: "bool", required: false)
      }
    }

    section("Only During These Times (optional)") {
      input(name: "notificationStartTime", type: "time", title: "Notify Starting At This Time", description: null, required: false)
      input(name: "notificationEndTime", type: "time", title: "Notify Ending At This Time", description: null, required: false)
    }
  }
}

def schedulingPage() {
  dynamicPage(name: "schedulingPage", title: "Rules For Access Scheduling") {
    if (!days) {
      section {
        href(name: "toCalendarPage", title: "Calendar", page: "calendarPage", description: calendarHrefDescription(), state: calendarHrefDescription() ? "complete" : "")
      }
    }
    if (!startDay && !startMonth && !startYear && !endDay && !endMonth && !endYear) {
      section {
        input(name: "days", type: "enum", title: "Allow User Access On These Days", description: "Every day", required: false, multiple: true, options: ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"], submitOnChange: true)
      }
      section {
        input(name: "modeStart", title: "Allow Access when in this mode", type: "mode", required: false, mutliple: false, submitOnChange: true)
      }
      section {
        if (modeStart) {
          input "andOrTime", "enum", title: "[And/Or] at a set time?", metadata:[values:["and", "or"]], required: false, submitOnChange: true
        }
        if ((modeStart == null) || andOrTime) {
          input(name: "startTime", type: "time", title: "Start Time", description: null, required: false)
          input(name: "endTime", type: "time", title: "End Time", description: null, required: false)
        }
      }
    }
  }
}

def calendarPage() {
  dynamicPage(name: "calendarPage", title: "Calendar Access") {
    section() {
      paragraph "This page is for advanced users only. You must enter each field carefully."
      paragraph "Calendar use does not support daily grant/deny OR Modes.  You cannot both have a date here, and allow access only on certain days/modes."
    }
    def hhPhrases = location.getHelloHome()?.getPhrases()*.label
    section("Start Date") {
      input name: "startDay", type: "number", title: "Day", required: false
      input name: "startMonth", type: "number", title: "Month", required: false
      input name: "startYear", type: "number", description: "Format(yyyy)", title: "Year", required: false
      input name: "startTime", type: "time", title: "Start Time", description: null, required: false
      if (hhPhrases) {
        hhPhrases.sort()
        input name: "calStartPhrase", type: "enum", title: "Hello Home Phrase", multiple: true,required: false, options: hhPhrases, refreshAfterSelection: true
      }
    }
    section("End Date") {
      input name: "endDay", type: "number", title: "Day", required: false
      input name: "endMonth", type: "number", title: "Month", required: false
      input name: "endYear", type: "number", description: "Format(yyyy)", title: "Year", required: false
      input name: "endTime", type: "time", title: "End Time", description: null, required: false
      if (hhPhrases) {
        hhPhrases.sort()
        input name: "calEndPhrase", type: "enum", title: "Hello Home Phrase", multiple: true,required: false, options: hhPhrases, refreshAfterSelection: true
      }
    }
  }
}

def onUnlockPage() {
  dynamicPage(name:"onUnlockPage", title:"Global Actions (Any Code)") {
    section("Actions") {
      def hhPhrases = location.getHelloHome()?.getPhrases()*.label
      if (hhPhrases) {
        hhPhrases.sort()
        input name: "homePhrases", type: "enum", title: "Home Mode Phrase", multiple: true, required: false, options: hhPhrases, refreshAfterSelection: true, submitOnChange: true
        if (homePhrases) {
          input "noRunPresence", "capability.presenceSensor", title: "Don't run Actions if any of these are present:", multiple: true, required: false
          input "doRunPresence", "capability.presenceSensor", title: "Run Actions only if any of these are present:", multiple: true, required: false
          input name: "manualUnlock", title: "Initiate phrase on manual unlock also?", type: "bool", defaultValue: false, refreshAfterSelection: true
          input(name: "modeIgnore", title: "Do not run Routine when in this mode", type: "mode", required: false, mutliple: false)
        }
      }
    }
  }
}

def resetCodeUsagePage(params) {
  def i = getUser(params)
  // do reset
  resetCodeUsage(i)

  dynamicPage(name:"resetCodeUsagePage", title:"User Usage Reset") {
    section {
      paragraph "User code usage has been reset."
    }
    section {
      href(name: "toSetupPage", title: "Back To Users", page: "setupPage")
    }
  }
}

def resetAllCodeUsagePage() {
  // do resetAll
  resetAllCodeUsage()
  dynamicPage(name:"resetAllCodeUsagePage", title:"User Settings") {
    section {
      paragraph "All user code usages have been reset."
    }
    section("Users") {
      href(name: "toSetupPage", title: "Back to Users", page: "setupPage")
      href(name: "toRootPage", title: "Main Page", page: "rootPage")
    }
  }
}

def reEnableUserPage(params) {
  // do reset
  def i = getUser(params)
  enableUser(i)
  lockErrorLoopReset()
  dynamicPage(name:"reEnableUserPage", title:"User re-enabled") {
    section {
      paragraph "User has been enabled."
    }
    section {
      href(name: "toSetupPage", title: "Back To Users", page: "setupPage")
    }
  }
}

def getUser(params) {
  def i = 1
  // Assign params to i.  Sometimes parameters are double nested.
  if (params.number) {
    i = params.number
  } else if (params.params){
    i = params.params.number
  } else if (state.lastUser) {
    i = state.lastUser
  }

  //Make sure i is a round number, not a float.
  if ( ! i.isNumber() ) {
    i = i.toInteger();
  } else if ( i.isNumber() ) {
    i = Math.round(i * 100) / 100
  }
  state.lastUser = i
  return i
}

def getLock(params) {
  def id = ''
  // Assign params to id.  Sometimes parameters are double nested.
  if (params.id) {
    id = params.id
  } else if (params.params){
    id = params.params.id
  } else if (state.lastLock) {
    id = state.lastLock
  }

  state.lastLock = id
  return theLocks.find{it.id == id}
}

def infoPage() {
  dynamicPage(name:"infoPage", title:"Lock Info") {
    section() {
      href(name: "toInfoRefreshPage", page: "infoRefreshPage", title: "Refresh Lock Data", description: 'Tap to refresh')
    }
    section("Locks") {
      if (theLocks) {
        def i = 0
        theLocks.each { lock->
          i++
          href(name: "toLockInfoPage${i}", page: "lockInfoPage", params: [id: lock.id], required: false, title: lock.displayName )
        }
      }
    }
  }
}

def infoRefreshPage() {
  dynamicPage(name:"infoRefreshPage", title:"Lock Info") {
    section() {
      manualPoll()
      paragraph "Lock info refreshing soon."
      href(name: "toInfoPage", page: "infoPage", title: "Back to Lock Info")
    }
  }
}

def lockInfoPage(params) {
  dynamicPage(name:"lockInfoPage", title:"Lock Info") {

    def lock = getLock(params)
    if (lock) {
      section("${lock.displayName}") {
        if (state."lock${lock.id}".codes != null) {
          def i = 0
          def pass = ''
          state."lock${lock.id}".codes.each { code->
            i++
            pass = state."lock${lock.id}".codes."slot${i}"
            paragraph "Slot ${i}\nCode: ${pass}"
          }
        } else {
          paragraph "No Lock data received yet.  Requires custom device driver.  Will be populated on next poll event."
        }
      }
    }
  }
}


def keypadPage() {
  dynamicPage(name: "keypadPage",title: "Keypad Settings (optional)") {
    section("Settings") {
      // TODO: put inputs here
      input(name: "keypad", title: "Keypad", type: "capability.lockCodes", multiple: true, required: false)
      input(name: "keypadstatus", title: "Send status to keypad?", type: "bool", multiple: false, required: true, defaultValue: true)
    }
    def hhPhrases = location.getHelloHome()?.getPhrases()*.label
    hhPhrases?.sort()
    section("Routines", hideable: true, hidden: true) {
      input(name: "armRoutine", title: "Arm/Away routine", type: "enum", options: hhPhrases, required: false)
      input(name: "disarmRoutine", title: "Disarm routine", type: "enum", options: hhPhrases, required: false)
      input(name: "stayRoutine", title: "Arm/Stay routine", type: "enum", options: hhPhrases, required: false)
      input(name: "nightRoutine", title: "Arm/Night routine", type: "enum", options: hhPhrases, required: false)
      input(name: "armDelay", title: "Arm Delay (in seconds)", type: "number", required: false)
      input(name: "notifyIncorrectPin", title: "Notify you when incorrect code is used?", type: "bool", required: false)
    }
  }
}

public smartThingsDateFormat() { "yyyy-MM-dd'T'HH:mm:ss.SSSZ" }

public humanReadableStartDate() {
  new Date().parse(smartThingsDateFormat(), startTime).format("h:mm a", timeZone(startTime))
}
public humanReadableEndDate() {
  new Date().parse(smartThingsDateFormat(), endTime).format("h:mm a", timeZone(endTime))
}

def manualPoll() {
  theLocks.poll()
}

def getConflicts(i) {
  def currentCode = settings."userCode${i}"
  def currentSlot = settings."userSlot${i}"
  def conflict = [:]
  conflict.has_conflict = false


  theLocks.each { lock->
    if (state."lock${lock.id}".codes) {
      conflict."lock${lock.id}" = [:]
      conflict."lock${lock.id}".conflicts = []
      def ind = 0
      state."lock${lock.id}".codes.each { code ->
        ind++
        if (currentSlot?.toInteger() != ind.toInteger() && !isUnique(currentCode, state."lock${lock.id}".codes."slot${ind}")) {
          conflict.has_conflict = true
          state."userState${i}".enabled = false
          state."userState${i}".disabledReason = "Code Conflict Detected"
          conflict."lock${lock.id}".conflicts << ind
        }
      }
    }
  }

  return conflict
}

def isUnique(newInt, oldInt) {

  if (newInt == null || oldInt == null) {
    // if either number is null, break here.
    return true
  }

  if (!newInt.isInteger() || !oldInt.isInteger()) {
    // number is not an integer, can't check.
    return true
  }

  def newArray = []
  def oldArray = []
  def result = true

  def i = 0
  // Get a normalized sequence, at the same length
  newInt.toString().toList().collect {
    i++
    if (i <= oldInt.length()) {
      newArray << normalizeNumber(it.toInteger())
    }
  }

  i = 0
  oldInt.toString().toList().collect {
    i++
    if (i <= oldInt.length()) {
      oldArray << normalizeNumber(it.toInteger())
    }
  }

  i = 0
  newArray.each { num->
    i++
    if (newArray.join() == oldArray.join()) {
      // The normalized numbers are the same!
      result = false
    }
  }
  return result
}

def normalizeNumber(number) {
  def result = null
  // RULE: Since some locks share buttons, make sure unique.
  // Even locks with 10-keys follow this rule! (annoyingly)
  switch (number) {
    case [1,2]:
      result = 1
      break
    case [3,4]:
      result = 2
      break
    case [5,6]:
      result = 3
      break
    case [7,8]:
      result = 4
      break
    case [9,0]:
      result = 5
      break
  }
  return result
}

def setupPageDescription(){
  def parts = []
  for (int i = 1; i <= settings.maxUsers; i++) {
    parts << settings."userName${i}"
  }
  return fancyString(parts)
}

def notificationPageDescription() {
  def parts = []
  def msg = ""
  if (settings.phone) {
    parts << "SMS to ${phone}"
  }
  if (settings.sendevent) {
    parts << "Event Notification"
  }
  if (settings.notification) {
    parts << "Push Notification"
  }
  msg += fancyString(parts)
  parts = []

  if (settings.notifyAccess) {
    parts << "on entry"
  }
  if (settings.notifyLock) {
    parts << "on lock"
  }
  if (settings.notifyAccessStart) {
    parts << "when granting access"
  }
  if (settings.notifyAccessEnd) {
    parts << "when revoking access"
  }
  if (settings.notificationStartTime) {
    parts << "starting at ${settings.notificationStartTime}"
  }
  if (settings.notificationEndTime) {
    parts << "ending at ${settings.notificationEndTime}"
  }
  if (parts.size()) {
    msg += ": "
    msg += fancyString(parts)
  }
  return msg
}

def calendarHrefDescription() {
  def dateStart = startDateTime()
  def dateEnd = endDateTime()
  if (dateEnd && dateStart) {
    def startReadableTime = readableDateTime(dateStart)
    def endReadableTime = readableDateTime(dateEnd)
    return "Accessible from ${startReadableTime} until ${endReadableTime}"
  } else if (!dateEnd && dateStart) {
    def startReadableTime = readableDateTime(dateStart)
    return "Accessible on ${startReadableTime}"
  } else if (dateEnd && !dateStart){
    def endReadableTime = readableDateTime(dateEnd)
    return "Accessible until ${endReadableTime}"
  }
}

def readableDateTime(date) {
  new Date().parse(smartThingsDateFormat(), date.format(smartThingsDateFormat(), location.timeZone)).format("EEE, MMM d yyyy 'at' h:mma", location.timeZone)
}

def userHrefTitle(i) {
  def title = "User ${i}"
  if (settings."userName${i}") {
    title = settings."userName${i}"
  }
  return title
}

def userHrefDescription(i) {
  def uc = settings."userCode${i}"
  def us = settings."userSlot${i}"
  def usage = state."userState${i}".usage
  def description = ""
  if (us != null) {
    description += "Slot: ${us}"
  }
  if (uc != null) {
    description += " / ${uc}"
    if(settings."burnCode${i}") {
      description += ' [Single Use]'
    }
  }
  if (usage != null) {
    description += " [Usage: ${usage}]"
  }
  return description
}

def userPageState(i) {
  if (settings."userCode${i}" && userIsEnabled(i)) {
    if (settings."burnCode${i}") {
      if (state."userState${i}".usage > 0) {
        return 'incomplete'
      } else {
        return 'complete'
      }
    } else {
      return 'complete'
    }

  } else if (settings."userCode${i}" && !settings."userEnabled${i}") {
    return 'incomplete'
  } else {
    return 'incomplete'
  }
}

def userIsEnabled(i) {
  if (settings."userEnabled${i}" && (settings."userCode${i}" != null) && (state."userState${i}".enabled != false)) {
    return true
  } else {
    return false
  }
}

def fancyDeviceString(devices = []) {
  fancyString(devices.collect { deviceLabel(it) })
}

def deviceLabel(device) {
  return device.label ?: device.name
}

def fancyString(listOfStrings) {
  listOfStrings.removeAll([null])
  def fancify = { list ->
    return list.collect {
      def label = it
      if (list.size() > 1 && it == list[-1]) {
        label = "and ${label}"
      }
      label
    }.join(", ")
  }

  return fancify(listOfStrings)
}

def schedulingHrefDescription() {
  if (startDateTime() || endDateTime()) {
    calendarHrefDescription()
  } else {
    def descriptionParts = []
    if (days) {
      descriptionParts << "On ${fancyString(days)},"
    }

    descriptionParts << "${fancyDeviceString(theLocks)} will be accessible"
    if ((andOrTime != null) || (modeStart == null)) {
      if (startTime) {
        descriptionParts << "at ${humanReadableStartDate()}"
      }
      if (endTime) {
        descriptionParts << "until ${humanReadableEndDate()}"
      }
    }

    if (modeStart) {
      if (startTime && andOrTime) {
        descriptionParts << andOrTime
      }
      descriptionParts << "when ${location.name} enters '${modeStart}' mode"
    }

    if (descriptionParts.size() <= 1) {
      // locks will be in the list no matter what. No rules are set if only locks are in the list
      return null
    }
    return descriptionParts.join(" ")
  }
}

def installed() {
  log.debug "Installing 'Locks' with settings: ${settings}"
  initialize()
}

def updated() {
  log.debug "Updating 'Locks' with settings: ${settings}"
  initialize()
}

private initialize() {
  unsubscribe()
  unschedule()
  if (startTime && !startDateTime()) {
    log.debug "scheduling access routine to run at ${startTime}"
    schedule(startTime, "reconcileCodesStart")
  } else if (startDateTime()) {
    // There's a start date, so let's run then
    log.debug "scheduling RUNONCE start"
    runOnce(startDateTime().format(smartThingsDateFormat(), location.timeZone), "reconcileCodesStart")
  }

  if (endTime && !endDateTime()) {
    log.debug "scheduling access denial routine to run at ${endTime}"
    schedule(endTime, "reconcileCodesEnd")
  } else if (endDateTime()) {
    // There's a end date, so let's run then
    log.debug "scheduling RUNONCE end"
    runOnce(endDateTime().format(smartThingsDateFormat(), location.timeZone), "reconcileCodesEnd")
  }

  subscribe(location, locationHandler)

  subscribe(theLocks, "codeReport", codereturn)
  subscribe(theLocks, "lock", codeUsed)
  subscribe(theLocks, "reportAllCodes", pollCodeReport, [filterEvents:false])
  if (keypad) {
    subscribe(location,"alarmSystemStatus",alarmStatusHandler)
    subscribe(keypad,"codeEntered",codeEntryHandler)
  }

  revokeDisabledUsers()
  reconcileCodes()
  lockErrorLoopReset()
  initalizeLockData()

  log.debug "state: ${state}"
}

def resetAllCodeUsage() {
  for (int i = 1; i <= settings.maxUsers; i++) {
    lockErrorLoopReset()
    resetCodeUsage(i)
  }
  log.debug "reseting all code usage"
}

def resetCodeUsage(i) {
  if(state."userState${i}" == null) {
    state."userState${i}" = [:]
    state."userState${i}".enabled = true
  }
  state."userState${i}".usage = 0
}

def enableUser(i) {
  state."userState${i}".enabled = true
}

def initalizeLockData() {
  theLocks.each { lock->
    if (state."lock${lock.id}" == null) {
      state."lock${lock.id}" = [:]
    }
  }
}

def lockErrorLoopReset() {
  state.error_loop_count = 0
  theLocks.each { lock->
    if (state."lock${lock.id}" == null) {
      state."lock${lock.id}" = [:]
    }
    state."lock${lock.id}".error_loop = false
  }
}


def locationHandler(evt) {
  log.debug "locationHandler evt: ${evt.value}"
  if (modeStart) {
    reconcileCodes()
  }
}

def reconcileCodes() {
  if (isAbleToStart()) {
    grantAccess()
  } else {
    revokeAccess()
  }
}

def reconcileCodesStart() {
  // schedule start of reconcileCodes
  reconcileCodes()
  if (calStartPhrase) {
    location.helloHome.execute(calStartPhrase)
  }
}

def reconcileCodesEnd() {
  // schedule end of reconcileCodes
  reconcileCodes()
  if (calEndPhrase) {
    location.helloHome.execute(calEndPhrase)
  }
}

def isAbleToStart() {
  def dateStart = startDateTime()
  def dateEnd = endDateTime()

  if (dateStart || dateEnd) {
    // calendar schedule above all
    return checkCalendarSchedule(dateStart, dateEnd)
  } else if (modeStart || startTime || endTime || days) {
    // No calendar set, check daily schedule
    if (isCorrectDay()) {
      // it's the right day
      checkDailySchedule()
    } else {
      // it's the wrong day
      return false
    }
  } else {
    // no schedule
    return true
  }
}

def checkDailySchedule() {
  if (andOrTime && modeStart && (isCorrectMode() || isInScheduledTime())) {
    // in correct mode or time with and/or switch
    if (andOrTime == 'and') {
      // must be both
      if (isCorrectMode() && isInScheduledTime()) {
        // is both
        return true
      } else {
        // is not both
        return false
      }
    } else {
      // could be either
      if (isCorrectMode() || isInScheduledTime()) {
        // it is either mode or time
        return true
      } else {
        // is not either mode or time
        return false
      }
    }
  } else {
    // Allow either mode or time, no andOrTime is set
    if (isCorrectMode() || isInScheduledTime()) {
      // it is either mode or time
      return true
    } else {
      // is not either mode or time
      return false
    }
  }
}

def checkCalendarSchedule(dateStart, dateEnd) {
  def now = rightNow().getTime()
  if (dateStart && !dateEnd) {
    // There's a start time, but no end time.  Allow access after start
    if (dateStart.getTime() > now) {
      // It's after the start time
      return true
    } else {
      // It's before the start time
      return false
    }

  } else if (dateEnd && !dateStart) {
    // There's a end time, but no start time.  Allow access until end
    if (dateStart.getTime() > now) {
      // It's after the start time
      return true
    } else {
      // It's before the start time
      return false
    }

  } else {
    // There's both an end time, and a start time.  Allow access between them.
    if (dateStart.getTime() < now && dateEnd.getTime() > now) {
      // It's in calendar times
      return true
    } else {
      // It's not in calendar times
      return false
    }
  }
}

def isCorrectMode() {
  if (modeStart) {
    // mode check is on
    if (location.mode == modeStart) {
      // we're in the right one mode
      return true
    } else {
      // we're in the wrong mode
      return false
    }
  } else {
    // mode check is off
    return false
  }
}

def isInScheduledTime() {
  def now = new Date()
  if (startTime && endTime) {
    def start = timeToday(startTime)
    def stop = timeToday(endTime)

    // there's both start time and end time
    if (start.before(now) && stop.after(now)){
      // It's between the times
      return true
    } else {
      // It's not between the times
      return false
    }
  } else if (startTime && !endTime){
    // there's a start time, but no end time
    def start = timeToday(startTime)
    if (start.before(now)) {
      // it's after start time
      return true
    } else {
      //it's before start time
      return false
    }
  } else if (!startTime && endTime) {
    // there's an end time but no start time
    def stop = timeToday(endTime)
    if (stop.after(now)) {
      // it's still before end time
      return true
    } else {
      // it's after end time
      return false
    }
  } else {
    // there are no times
    return false
  }
}

def startDateTime() {
  if (startDay && startMonth && startYear && startTime) {
    def time = new Date().parse(smartThingsDateFormat(), startTime).format("'T'HH:mm:ss.SSSZ", timeZone(startTime))
    return Date.parse("yyyy-MM-dd'T'HH:mm:ss.SSSZ", "${startYear}-${startMonth}-${startDay}${time}")
  } else {
    // Start Date Time not set
    return false
  }
}

def endDateTime() {
  if (endDay && endMonth && endYear && endTime) {
    def time = new Date().parse(smartThingsDateFormat(), endTime).format("'T'HH:mm:ss.SSSZ", timeZone(endTime))
    return Date.parse("yyyy-MM-dd'T'HH:mm:ss.SSSZ", "${endYear}-${endMonth}-${endDay}${time}")
  } else {
    // End Date Time not set
    return false
  }
}

def rightNow() {
  def now = new Date().format("yyyy-MM-dd'T'HH:mm:ss.SSSZ", location.timeZone)
  return Date.parse("yyyy-MM-dd'T'HH:mm:ss.SSSZ", now)
}

def isCorrectDay() {
  def today = new Date().format("EEEE", location.timeZone)
  log.debug "today: ${today}, days: ${days}"
  if (!days || days.contains(today)) {
    // if no days, assume every day
    return true
  }
  log.trace "should not allow access - Not correct Day"
  return false
}

def userSlotArray() {
  def array = []
  for (int i = 1; i <= settings.maxUsers; i++) {
    if (settings."userSlot${i}") {
      array << settings."userSlot${i}".toInteger()
    }
  }
  return array
}

def enabledUsersArray() {
  def array = []
  for (int i = 1; i <= settings.maxUsers; i++) {
    if (userIsEnabled(i)) {
      array << i
    }
  }
  return array
}
def enabledUsersSlotArray() {
  def array = []
  for (int i = 1; i <= settings.maxUsers; i++) {
    if (userIsEnabled(i)) {
      def userSlot = settings."userSlot${i}"
      array << userSlot.toInteger()
    }
  }
  return array
}

def disabledUsersSlotArray() {
  def array = []
  for (int i = 1; i <= settings.maxUsers; i++) {
    if (!userIsEnabled(i)) {
      if (settings."userSlot${i}") {
        array << settings."userSlot${i}".toInteger()
      }
    }
  }
  return array
}

def codereturn(evt) {
  def codeNumber = evt.data.replaceAll("\\D+","")
  def codeSlot = evt.value
  if (notifyAccessEnd || notifyAccessStart) {
    if (userSlotArray().contains(evt.integerValue.toInteger())) {
      def userName = settings."userName${usedUserIndex(evt.integerValue)}"
      if (codeNumber == "") {
        if (notifyAccessEnd) {
          def message = "${userName} no longer has access to ${evt.displayName}"
          if (codeNumber.isNumber()) {
            state."lock${evt.deviceId}".codes."slot${codeSlot}" = codeNumber
          }
          send(message)
        }
      } else {
        if (notifyAccessStart) {
          def message = "${userName} now has access to ${evt.displayName}"
          if (codeNumber.isNumber()) {
            state."lock${evt.deviceId}".codes."slot${codeSlot}" = codeNumber
          }
          send(message)
        }
      }
    }
  }
}

def usedUserIndex(usedSlot) {
  for (int i = 1; i <= settings.maxUsers; i++) {
    if (settings."userSlot${i}" && settings."userSlot${i}".toInteger() == usedSlot.toInteger()) {
      return i
    }
  }
  return false
}

def codeUsed(evt) {
  // check the status of the lock, helpful for some schlage locks.
  runIn(10, doPoll)
  log.debug("codeUsed evt.value: " + evt.value + ". evt.data: " + evt.data)
  def message = null

  if(evt.value == "unlocked" && evt.data) {
    def codeData = new JsonSlurper().parseText(evt.data)
    if(codeData.usedCode && codeData.usedCode.isNumber() && userSlotArray().contains(codeData.usedCode.toInteger())) {
      def usedIndex = usedUserIndex(codeData.usedCode).toInteger()
      def unlockUserName = settings."userName${usedIndex}"
      message = "${evt.displayName} was unlocked by ${unlockUserName}"
      // increment usage
      state."userState${usedIndex}".usage = state."userState${usedIndex}".usage + 1
      if(settings."userHomePhrases${usedIndex}") {
        // Specific User Hello Home
        if (settings."userNoRunPresence${usedIndex}" && settings."userDoRunPresence${usedIndex}" == null) {
          if (!anyoneHome(settings."userNoRunPresence${usedIndex}")) {
            location.helloHome.execute(settings."userHomePhrases${usedIndex}")
          }
        } else if (settings."userDoRunPresence${usedIndex}" && settings."userNoRunPresence${usedIndex}" == null) {
          if (anyoneHome(settings."userDoRunPresence${usedIndex}")) {
            location.helloHome.execute(settings."userHomePhrases${usedIndex}")
          }
        } else if (settings."userDoRunPresence${usedIndex}" && settings."userNoRunPresence${usedIndex}") {
          if (anyoneHome(settings."userDoRunPresence${usedIndex}") && !anyoneHome(settings."userNoRunPresence${usedIndex}")) {
            location.helloHome.execute(settings."userHomePhrases${usedIndex}")
          }
        } else {
          location.helloHome.execute(settings."userHomePhrases${usedIndex}")
        }
      }
      if(settings."burnCode${usedIndex}") {
        theLocks.deleteCode(codeData.usedCode)
        runIn(60*2, doPoll)
        message += ".  Now burning code."
      }
      //Don't send notification if muted
      if(settings."dontNotify${usedIndex}" == true) {
        message = null
      }
    }
  } else if(evt.value == "locked" && settings.notifyLock) {
    message = "${evt.displayName} has been locked"
  }

  if (message) {
    log.debug("Sending message: " + message)
    send(message)
  }

  if (homePhrases) {
    performActions(evt)
  }
}

def performActions(evt) {
  if(evt.value == "unlocked" && evt.data) {
    def codeData = new JsonSlurper().parseText(evt.data)
    if(enabledUsersArray().contains(codeData.usedCode) || isManualUnlock(codeData)) {
      // Global Hello Home
      if(location.currentMode != modeIgnore) {
        if (noRunPresence && doRunPresence == null) {
          if (!anyoneHome(noRunPresence)) {
            location.helloHome.execute(homePhrases)
          }
        } else if (doRunPresence && noRunPresence == null) {
          if (anyoneHome(doRunPresence)) {
            location.helloHome.execute(homePhrases)
          }
        } else if (doRunPresence && noRunPresence) {
          if (anyoneHome(doRunPresence) && !anyoneHome(noRunPresence)) {
            location.helloHome.execute(homePhrases)
          }
        } else {
         location.helloHome.execute(homePhrases)
        }
      } else {
        def routineMessage = "Already in ${modeIgnore} mode, skipping execution of ${homePhrases} routine."
        log.debug routineMessage
        send(routineMessage)
      }
    }
  }
}

def revokeDisabledUsers() {
  def array = []
  disabledUsersSlotArray().each { slot ->
    array << ["code${slot}", ""]
  }
  def json = new groovy.json.JsonBuilder(array).toString()
  if (json != '[]') {
    theLocks.updateCodes(json)
    runIn(60*2, doPoll)
  }
}

def doPoll() {
  // this gets codes if custom device is installed
  if (!allCodesDone()) {
    state.error_loop_count = state.error_loop_count + 1
  }
  theLocks.poll()
}

def grantAccess() {
  def array = []
  enabledUsersArray().each { user->
    def userSlot = settings."userSlot${user}"
    if (settings."userCode${user}" != null) {
      def newCode = settings."userCode${user}"
      array << ["code${userSlot}", "${newCode}"]
    } else {
      array << ["code${userSlot}", ""]
    }
  }
  def json = new groovy.json.JsonBuilder(array).toString()
  if (json != '[]') {
    theLocks.updateCodes(json)
    runIn(60*2, doPoll)
  }
}

def revokeAccess() {
  def array = []
  enabledUsersArray().each { user->
    def userSlot = settings."userSlot${user}"
    array << ["code${userSlot}", ""]
  }
  def json = new groovy.json.JsonBuilder(array).toString()
  if (json != '[]') {
    theLocks.updateCodes(json)
    runIn(60*2, doPoll)
  }
}

def isManualUnlock(codeData) {
  // check to see if the user wants this
  if (manualUnlock) {
    // garyd9's device type returns 'manual'
    if ((codeData.usedCode == "") || (codeData.usedCode == null) || (codeData.usedCode == 'manual')) {
      // no code used on unlock!
      return true
    } else {
      // probably a code we're not dealing with here
      return false
    }
  } else {
    return false
  }
}

def isActiveBurnCode(slot) {
  if (settings."burnCode${slot}" && state."userState${slot}".usage > 0) {
    return false
  } else {
    // not a burn code / not yet used
    return true
  }
}

def pollCodeReport(evt) {
  def active = isAbleToStart()
  def codeData = new JsonSlurper().parseText(evt.data)
  def numberOfCodes = codeData.codes
  def userSlots = userSlotArray()

  def array = []

  (1..maxUsers).each { user->
    def slot = settings."userSlot${user}"
    def code = codeData."code${slot}"
    def correctCode = settings."userCode${user}"
    if (active) {
      if (userIsEnabled(user) && isActiveBurnCode(user)) {
        if (code == settings."userCode${user}") {
          // Code is Active, We should be active. Nothing to do
        } else {
          // Code is incorrect, We should be active.
          array << ["code${slot}", settings."userCode${user}"]
        }
      } else {
        if (code != '') {
          // Code is set, user is disabled, We should be disabled.
          array << ["code${slot}", ""]
        } else {
          // Code is not set, user is disabled. Nothing to do
        }
      }
    } else {
      if (code != '') {
        // Code is set, We should be disabled.
        array << ["code${slot}", ""]
      } else {
        // Code is not active, We should be disabled. Nothing to do
      }
    }
  }

  def currentLock = theLocks.find{it.id == evt.deviceId}
  populateDiscovery(codeData, currentLock)

  def json = new groovy.json.JsonBuilder(array).toString()
  if (json != '[]') {
    runIn(60*2, doPoll)

    //Lock is in an error state
    state."lock${currentLock.id}".error_loop = true
    def error_number = state.error_loop_count + 1
    if (error_number <= 10) {
      log.debug "sendCodes fix is: ${json} Error: ${error_number}/10"
      currentLock.updateCodes(json)
    } else {
      log.debug "kill fix is: ${json}"
      currentLock.updateCodes(json)
      json = new JsonSlurper().parseText(json)
      def n = 0
      json.each { code ->
        n = code[0][4..-1].toInteger()
        def usedIndex = usedUserIndex(n)
        def name = settings."userName${usedIndex}"
        if (state."userState${usedIndex}".enabled) {
          state."userState${usedIndex}".enabled = false
          state."userState${usedIndex}".disabledReason = "Controller failed to set code"
          send("Controller failed to set code for ${name}")
        }
      }
    }
  } else {
    state."lock${currentLock.id}".error_loop = false
    if (allCodesDone) {
      lockErrorLoopReset()
    } else {
      runIn(60, doPoll)
    }
  }
}

def allCodesDone() {
  def i = 0
  def codeComplete = true
  theLocks.each { lock->
    i++
    if (state."lock${lock.id}".error_loop == true) {
      codeComplete = false
    }
  }
  return codeComplete
}

private anyoneHome(sensors) {
  def result = false
  if(sensors.findAll { it?.currentPresence == "present" }) {
    result = true
  }
  result
}

private send(msg) {
  if (notificationStartTime != null && notificationEndTime != null) {
    def start = timeToday(notificationStartTime)
    def stop = timeToday(notificationEndTime)
    def now = new Date()
    if (start.before(now) && stop.after(now)){
      sendMessage(msg)
    }
  } else {
    sendMessage(msg)
  }
}

private sendMessage(msg) {
  if (notification) {
    sendPush(msg)
  } else {
    sendNotificationEvent(msg)
  }
  if (phone) {
    if ( phone.indexOf(";") > 1){
      def phones = phone.split(";")
      for ( def i = 0; i < phones.size(); i++) {
        sendSms(phones[i], msg)
      }
    }
    else {
      sendSms(phone, msg)
    }
  }
}

def populateDiscovery(codeData, lock) {
  def codes = [:]
  def codeSlots = 30
  if (codeData.codes) {
    codeSlots = codeData.codes
  }
  (1..codeSlots).each { slot->
    codes."slot${slot}" = codeData."code${slot}"
  }
  atomicState."lock${lock.id}".codes = codes
}

private String getPIN() {
  return settings.pin.value.toString().padLeft(4,'0')
}

def alarmStatusHandler(event) {
  log.debug "Keypad manager caught alarm status change: "+event.value
  if(keypadstatus)
  {
  	if (event.value == "off"){
      keypad?.each() { it.setDisarmed() }
  	}
  	else if (event.value == "away"){
      keypad?.each() { it.setArmedAway() }
  	}
  	else if (event.value == "stay") {
      keypad?.each() { it.setArmedStay() }
  	}
  }
}

private sendSHMEvent(String shmState) {
  def event = [
        name:"alarmSystemStatus",
        value: shmState,
        displayed: true,
        description: "System Status is ${shmState}"
      ]
  log.debug "test ${event}"
  sendLocationEvent(event)
}

private execRoutine(armMode) {
  if (armMode == 'away') {
    location.helloHome?.execute(settings.armRoutine)
  } else if (armMode == 'stay') {
    location.helloHome?.execute(settings.stayRoutine)
  } else if (armMode == 'off') {
    location.helloHome?.execute(settings.disarmRoutine)
  }
}

def codeEntryHandler(evt) {
  //do stuff
  log.debug "Caught code entry event! ${evt.value.value}"

  def codeEntered = evt.value as String

  def data = evt.data as String
  def armMode = ''
  def currentarmMode = keypad.currentValue("armMode")
  def changedMode = 0

  if (data == '0') {
    armMode = 'off'
  }
  else if (data == '3') {
    armMode = 'away'
  }
  else if (data == '1') {
    armMode = 'stay'
  }
  else if (data == '2') {
    armMode = 'stay' //Currently no separate night mode for SHM, set to 'stay'
  } else {
    log.error "${app.label}: Unexpected arm mode sent by keypad!: "+data
    return []
  }

  def i = settings.maxUsers
  def message = " "
  while (i > 0) {
    log.debug "i =" + i
    def correctCode = settings."userCode${i}" as String

    if (codeEntered == correctCode) {

      log.debug "User Enabled: " + state."userState${i}".enabled

      if (state."userState${i}".enabled == true) {
        log.debug "Correct PIN entered. Change SHM state to ${armMode}"
        //log.debug "Delay: ${armDelay}"
        //log.debug "Data: ${data}"
        //log.debug "armMode: ${armMode}"

        def unlockUserName = settings."userName${i}"

        if (data == "0") {
          //log.debug "sendDisarmCommand"
          runIn(0, "sendDisarmCommand")
          message = "${evt.displayName} was disarmed by ${unlockUserName}"
        }
        else if (data == "1") {
          //log.debug "sendStayCommand"
          if(armDelay && keypadstatus) {
            keypad?.each() { it.setExitDelay(armDelay) }
          }
          runIn(armDelay, "sendStayCommand")
          message = "${evt.displayName} was armed to 'Stay' by ${unlockUserName}"
        }
        else if (data == "2") {
          //log.debug "sendNightCommand"
          if(armDelay && keypadstatus) {
            keypad?.each() { it.setExitDelay(armDelay) }
          }
          runIn(armDelay, "sendNightCommand")
          message = "${evt.displayName} was armed to 'Night' by ${unlockUserName}"
        }
        else if (data == "3") {
          //log.debug "sendArmCommand"
          if(armDelay && keypadstatus) {
            keypad?.each() { it.setExitDelay(armDelay) }
          }
          runIn(armDelay, "sendArmCommand")
          message = "${evt.displayName} was armed to 'Away' by ${unlockUserName}"
        }

        if(settings."burnCode${i}") {
          state."userState${i}".enabled = false
          message += ".  Now burning code."
        }

        log.debug "${message}"
        //log.debug "Initial Usage Count:" + state."userState${i}".usage
        state."userState${i}".usage = state."userState${i}".usage + 1
        //log.debug "Final Usage Count:" + state."userState${i}".usage
        send(message)
        i = 0
      } else if (state."userState${i}".enabled == false){
        log.debug "PIN Disabled"
        //Could also call acknowledgeArmRequest() with a parameter of 4 to report invalid code. Opportunity to simplify code?
        //keypad.sendInvalidKeycodeResponse()
      }
    }
    changedMode = 1
    i--
  }
  if (changedMode == 1 && i == 0) {
    def errorMsg = "Incorrect Code Entered: ${codeEntered}"
    if (notifyIncorrectPin) {
      log.debug "Incorrect PIN"
      send(errorMsg)
    }
    //Could also call acknowledgeArmRequest() with a parameter of 4 to report invalid code. Opportunity to simplify code?
    keypad.sendInvalidKeycodeResponse()
  }
}
def sendArmCommand() {
  log.debug "Sending Arm Command."
  if (keypadstatus) {
    keypad?.each() { it.acknowledgeArmRequest(3) }
  }
  sendSHMEvent("away")
  execRoutine("away")
}
def sendDisarmCommand() {
  log.debug "Sending Disarm Command."
  if (keypadstatus) {
    keypad?.each() { it.acknowledgeArmRequest(0) }
  }
  sendSHMEvent("off")
  execRoutine("off")
}
def sendStayCommand() {
  log.debug "Sending Stay Command."
  if (keypadstatus) {
    keypad?.each() { it.acknowledgeArmRequest(1) }
  }
  sendSHMEvent("stay")
  execRoutine("stay")
}
def sendNightCommand() {
  log.debug "Sending Night Command."
  if (keypadstatus) {
    keypad?.each() { it.acknowledgeArmRequest(2) }
  }
  sendSHMEvent("stay")
  execRoutine("stay")
}