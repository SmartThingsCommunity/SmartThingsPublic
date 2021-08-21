definition(
  name: 'Lock Manager',
  namespace: 'ethayer',
  author: 'Erik Thayer',
  parent: parent ? "ethayer: Lock Manager" : null,
  description: 'Manage locks and users',
  category: 'Safety & Security',
  singleInstance: true,
  iconUrl: 'http://images.lockmanager.io/app/v1/images/lm.jpg',
  iconX2Url: 'http://images.lockmanager.io/app/v1/images/lm2x.jpg',
  iconX3Url: 'http://images.lockmanager.io/app/v1/images/lm3x.jpg'
)
import groovy.json.JsonSlurper
import groovy.json.JsonBuilder
import java.util.regex.*
include 'asynchttp_v1'

preferences {
  // Wizard
  page name: 'appPageWizard'

  // Manager ===
  page name: 'mainLandingPage'
  page name: 'mainSetupPage', title: 'Installed', install: true, uninstall: true, submitOnChange: true
  page name: 'mainPage', title: 'Lock Manager', install: true, uninstall: true, submitOnChange: true
  page name: 'infoRefreshPage'
  page name: 'notificationPage'
  page name: 'helloHomePage'
  page name: 'lockInfoPage'
  page name: 'keypadPage'
  page name: 'askAlexaPage'

  // Lock ====
  page name: 'lockLandingPage'
  page name: 'lockSetupPage'
  page name: 'lockMainPage'
  page name: 'lockErrorPage'
  page name: 'lockNotificationPage'
  page name: 'lockHelloHomePage'
  page name: 'lockInfoRefreshPage'
  page name: 'lockAskAlexaPage'

  // User ====
  page name: 'userLandingPage'
  page name: 'userSetupPage'
  page name: 'userMainPage'
  page name: 'userLockPage', title: 'Manage Lock', install: false, uninstall: false
  page name: 'schedulingPage', title: 'Schedule User', install: false, uninstall: false
  page name: 'calendarPage', title: 'Calendar', install: false, uninstall: false
  page name: 'userNotificationPage'
  page name: 'reEnableUserLockPage'
  page name: 'lockResetPage'
  page name: 'userKeypadPage'
  page name: 'userAskAlexaPage'

  // Keypad ====
  page name: 'keypadLandingPage'
  page name: 'keypadSetupPage'
  page name: 'keypadMainPage'
  page name: 'keypadErrorPage'

  // API ====
  page name: 'apiSetupPage'
}

def appPageWizard(params) {
  def appType = state.appType

  if (!appType) {
    if (params.type) {
      // inital set app type
      debugger("Param set is: ${params.type}")
      appType = params.type
    }
    if (parent) {
      appType = parent.theNewChild()
    }
    debugger("App Type: ${appType}")
    setAppType(appType)
  }

  // find the correct landing page
  switch (appType) {
    case 'lock':
      lockLandingPage()
      break
    case 'user':
      userLandingPage()
      break
    case 'keypad':
      keypadLandingPage()
      break
    case 'api':
      apiSetupPage()
      break
    default:
      mainLangingPage()
      break
  }
}

def installed() {
  // find the correct installer
  switch (state.appType) {
    case 'lock':
      lockInstalled()
      break
    case 'user':
      userInstalled()
      break
    case 'keypad':
      installedKeypad()
      break
    case 'api':
      installedApi()
      break
    default:
      debugger("Installed with settings: ${settings}")
      installedMain()
      break
  }
}

def updated() {
  // find the correct updater
  switch (state.appType) {
    case 'lock':
      lockUpdated()
      break
    case 'user':
      userUpdated()
      break
    case 'keypad':
      updatedKeypad()
      break
    case 'api':
      updatedApi()
      break
    default:
      debugger("Installed with settings: ${settings}")
      updatedMain()
      break
  }
}

def uninstalled() {
  switch (state.appType) {
    case 'lock':
      break
    case 'user':
      userUninstalled()
      break
    case 'keypad':
      break
    case 'api':
      break
    default:
      break
  }
}


def installedMain() {
  initializeMain()
}

def updatedMain() {
  log.debug "Main Updated with settings: ${settings}"
  unsubscribe()
  initializeMain()
}

def initializeMain() {
  def children = getLockApps()
  log.debug "there are ${children.size()} locks"

  state.initializeComplete = true
  state.appVersion = "2.1.3"

  subscribe(location, "mode", locationHandler)
}

def mainLangingPage() {
  if (state.initializeComplete) {
    mainPage()
  } else {
    mainSetupPage()
  }
}

def mainSetupPage() {
  dynamicPage(name: 'mainSetupPage', title: 'Lock Manager', install: true, uninstall: true, submitOnChange: true) {
    section('Initial Setup') {
      label(title: 'Label this SmartApp', required: false, defaultValue: 'Lock Manager')
      paragraph 'Lock Manager © 2021 v2.1.3'
    }
  }
}


def mainPage() {
  dynamicPage(name: 'mainPage', title: 'Lock Manager', install: true, uninstall: true, submitOnChange: true) {
    section('Create New Integration') {
      input name: "appType", type: "enum", title: "Choose Type", options: ['Lock', 'User', 'Keypad'], description: "Select the integration you need", submitOnChange: true
      if (settings.appType) {
        def appTypeString = settings.appType
        def miniTypeString = appTypeString.toLowerCase()
        debugger("New Param: ${miniTypeString}")
        app(name: 'newChild', params: [type: miniTypeString], appName: 'Lock Manager', namespace: 'ethayer', title: "Create New ${appTypeString}", multiple: true, image: "http://images.lockmanager.io/app/v1/images/new-${miniTypeString}.png")
      }
    }
    section('Locks') {
      def lockApps = getLockApps()
      lockApps = lockApps.sort{ it.lockSort() }
      if (lockApps) {
        def i = 0
        lockApps.each { lockApp ->
          i++
          href(name: "toLockInfoPage${i}", page: 'lockInfoPage', params: [id: lockApp.lockSort()], required: false, title: lockApp.label, image: 'http://images.lockmanager.io/app/v1/images/lock.png' )
        }
      }
    }
    section('Global Settings') {
      href(name: 'toNotificationPage', page: 'notificationPage', title: 'Notification Settings', description: notificationPageDescription(), state: notificationPageDescription() ? 'complete' : '', image: 'http://images.lockmanager.io/app/v1/images/bullhorn.png')

      def actions = location.helloHome?.getPhrases()*.label
      if (actions) {
        href(name: 'toHelloHomePage', page: 'helloHomePage', title: 'Hello Home Settings', image: 'http://images.lockmanager.io/app/v1/images/home.png')
      }

      def keypadApps = getKeypadApps()
      if (keypadApps) {
        href(name: 'toKeypadPage', page: 'keypadPage', title: 'Keypad Routines (optional)', image: 'http://images.lockmanager.io/app/v1/images/keypad.png')
      }
    }

    // section('API') {
    //   href(name: 'toApiPage', page: 'apiSetupPage', title: 'API Options', image: 'http://images.lockmanager.io/app/v1/images/keypad.png')
    // }

    section('Advanced', hideable: true, hidden: true) {
      input(name: 'overwriteMode', title: 'Overwrite?', type: 'bool', required: true, defaultValue: true, description: 'Overwrite mode automatically deletes codes not in the users list')
      input(name: 'enableDebug', title: 'Enable IDE debug messages?', type: 'bool', required: true, defaultValue: false, description: 'Show activity from Lock Manger in logs for debugging.')
      label(title: 'Label this SmartApp', required: false, defaultValue: 'Lock Manager')
      paragraph 'Lock Manager © 2021 v2.1.3'
    }
  }
}

def setAppType(appType) {
  if (!state.appType) {
    state.appType = appType
  }
}

def userPageOptions(count) {
  def options = []
  (1..count).each { page->
    options << ["${page}": "Page ${page}"]
  }
  return options
}

def determinePage(pageCount) {
  if (selectedUserPage) {
    if (pageCount < selectedUserPage.toInteger()) {
      return 0
    } else {
      return selectedUserPage.toInteger() - 1
    }
  } else {
    return 0
  }
}

def lockInfoPage(params) {
  dynamicPage(name:"lockInfoPage", title:"Lock Info") {
    def lockApp = getLockAppByIndex(params)
    if (lockApp) {
      section("${lockApp.label}") {
        def complete = lockApp.isCodeComplete()
        if (!complete) {
          def completeCount = lockApp.sweepProgress()
          def totalSlots = lockApp.lockCodeSlots()
          def percent = Math.round((completeCount/totalSlots) * 100)
          def estimatedMinutes = ((totalSlots - completeCount) * 6) / 60
          def p = ""
          p += "${percent}%\n"
          p += 'Sweep is in progress.\n'
          p += "Progress: ${completeCount}/${totalSlots}\n\n"

          p += "Estimated time left: ${estimatedMinutes} Minutes\n"
          p += "Lock will set codes after sweep is complete"
          paragraph p
        } else {
          def pageCount = lockApp.userPageCount()
          if (pageCount > 1) {
            input(name: 'selectedUserPage', title: 'Select the visible user page', type: 'enum', required: true, defaultValue: 1, description: 'Select Page',
            options: userPageOptions(pageCount), submitOnChange: true)
          }
          // def codeData = lockApp.codeData()
          def thePage = determinePage(pageCount)
          debugger("Page count: ${pageCount} Page: ${thePage}")

          def codeData = lockApp.codeDataPaginated(thePage)
          debugger(codeData)
          if (codeData) {
            def setCode = ''
            def usage
            def para
            def image
            codeData.each { data ->
              data = data.value
              if (data.codeState != 'unknown') {
                def userApp = lockApp.findSlotUserApp(data.slot)
                para = "Slot ${data.slot}"
                if (data.code) {
                  para = para + "\nCode: ${data.code}"
                }
                if (userApp) {
                  para = para + userApp.getLockUserInfo(lockApp.lock)
                  image = userApp.lockInfoPageImage(lockApp.lock)
                } else {
                  image = 'http://images.lockmanager.io/app/v1/images/times-circle-o.png'
                }
                if (data.codeState == 'refresh') {
                  para = para +'\nPending refresh...'
                }
                if (data.control) {
                  para = para +"\nControl: ${data.control}"
                }
                paragraph para, image: image
              }
            }
          }
        }
      }
      section('Lock Settings') {
        def pinLength = lockApp.pinLength()
        def lockCodeSlots = lockApp.lockCodeSlots()
        if (pinLength) {
          paragraph "Required Length: ${pinLength}"
        }
        paragraph "Slot Count: ${lockCodeSlots}"
      }
    } else {
      section() {
        paragraph 'Error: Can\'t find lock!'
      }
    }
  }
}

def notificationPage() {
  dynamicPage(name: 'notificationPage', title: 'Global Notification Settings') {
    section {
      paragraph 'These settings will apply to all users.  Settings on individual users will override these settings'

      input('recipients', 'contact', title: 'Send notifications to', submitOnChange: true, required: false, multiple: true, image: 'http://images.lockmanager.io/app/v1/images/book.png')
      href(name: 'toAskAlexaPage', title: 'Ask Alexa', page: 'askAlexaPage', image: 'http://images.lockmanager.io/app/v1/images/Alexa.png')
      if (!recipients) {
        input(name: 'phone', type: 'text', title: 'Text This Number', description: 'Phone number', required: false, submitOnChange: true)
        paragraph 'For multiple SMS recipients, separate phone numbers with a semicolon(;)'
        input(name: 'notification', type: 'bool', title: 'Send A Push Notification', description: 'Notification', required: false, submitOnChange: true)
      }

      if (phone != null || notification || recipients) {
        input(name: 'notifyAccess', title: 'on User Entry', type: 'bool', required: false, image: 'http://images.lockmanager.io/app/v1/images/unlock-alt.png')
        input(name: 'notifyLock', title: 'on Lock', type: 'bool', required: false, image: 'http://images.lockmanager.io/app/v1/images/lock.png')
        input(name: 'notifyAccessStart', title: 'when granting access', type: 'bool', required: false, image: 'http://images.lockmanager.io/app/v1/images/check-circle-o.png')
        input(name: 'notifyAccessEnd', title: 'when revoking access', type: 'bool', required: false, image: 'http://images.lockmanager.io/app/v1/images/times-circle-o.png')
      }
    }
    section('Only During These Times (optional)') {
      input(name: 'notificationStartTime', type: 'time', title: 'Notify Starting At This Time', description: null, required: false)
      input(name: 'notificationEndTime', type: 'time', title: 'Notify Ending At This Time', description: null, required: false)
    }
  }
}

def helloHomePage() {
  dynamicPage(name: 'helloHomePage', title: 'Global Hello Home Settings (optional)') {
    def actions = location.helloHome?.getPhrases()*.label
    actions?.sort()
    section('Hello Home Phrases') {
      input(name: 'manualUnlockRoutine', title: 'On Manual Unlock', type: 'enum', options: actions, required: false, multiple: true, image: 'http://images.lockmanager.io/app/v1/images/unlock-alt.png')
      input(name: 'manualLockRoutine', title: 'On Manual Lock', type: 'enum', options: actions, required: false, multiple: true, image: 'http://images.lockmanager.io/app/v1/images/lock.png')

      input(name: 'codeUnlockRoutine', title: 'On Code Unlock', type: 'enum', options: actions, required: false, multiple: true, image: 'http://images.lockmanager.io/app/v1/images/unlock-alt.png' )

      paragraph 'Supported on some locks:'
      input(name: 'codeLockRoutine', title: 'On Code Lock', type: 'enum', options: actions, required: false, multiple: true, image: 'http://images.lockmanager.io/app/v1/images/lock.png')

      paragraph 'These restrictions apply to all the above:'
      input "userNoRunPresence", "capability.presenceSensor", title: "DO NOT run Actions if any of these are present:", multiple: true, required: false
      input "userDoRunPresence", "capability.presenceSensor", title: "ONLY run Actions if any of these are present:", multiple: true, required: false
    }
  }
}

def askAlexaPage() {
  dynamicPage(name: 'askAlexaPage', title: 'Ask Alexa Message Settings') {
    section('Que Messages with the Ask Alexa app') {
      paragraph 'These settings apply to all users.  These settings are overridable on the user level'
      input(name: 'alexaAccess', title: 'on User Entry', type: 'bool', required: false, image: 'http://images.lockmanager.io/app/v1/images/unlock-alt.png')
      input(name: 'alexaLock', title: 'on Lock', type: 'bool', required: false, image: 'http://images.lockmanager.io/app/v1/images/lock.png')
      input(name: 'alexaAccessStart', title: 'when granting access', type: 'bool', required: false, image: 'http://images.lockmanager.io/app/v1/images/check-circle-o.png')
      input(name: 'alexaAccessEnd', title: 'when revoking access', type: 'bool', required: false, image: 'http://images.lockmanager.io/app/v1/images/times-circle-o.png')
    }
    section('Only During These Times (optional)') {
      input(name: 'alexaStartTime', type: 'time', title: 'Notify Starting At This Time', description: null, required: false)
      input(name: 'alexaEndTime', type: 'time', title: 'Notify Ending At This Time', description: null, required: false)
    }
  }
}

def keypadPage() {
  dynamicPage(name: 'keypadPage',title: 'Keypad Settings (optional)', install: true, uninstall: true) {
    def actions = location.helloHome?.getPhrases()*.label
    actions?.sort()
    section("Settings") {
      paragraph 'settings here are for all users. When any user enters their passcode, run these routines'
      input(name: 'armRoutine', title: 'Arm/Away routine', type: 'enum', options: actions, required: false, multiple: true)
      input(name: 'disarmRoutine', title: 'Disarm routine', type: 'enum', options: actions, required: false, multiple: true)
      input(name: 'stayRoutine', title: 'Arm/Stay routine', type: 'enum', options: actions, required: false, multiple: true)
      input(name: 'nightRoutine', title: 'Arm/Night routine', type: 'enum', options: actions, required: false, multiple: true)
    }
  }
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

def notificationPageDescription() {
  def parts = []
  def msg = ""
  if (settings.phone) {
    parts << "SMS to ${phone}"
  }
  if (settings.recipients) {
    parts << 'Sent to Address Book'
  }
  if (settings.notification) {
    parts << 'Push Notification'
  }
  msg += fancyString(parts)
  parts = []

  if (settings.notifyAccess) {
    parts << 'on entry'
  }
  if (settings.notifyLock) {
    parts << 'on lock'
  }
  if (settings.notifyAccessStart) {
    parts << 'when granting access'
  }
  if (settings.notifyAccessEnd) {
    parts << 'when revoking access'
  }
  if (settings.notificationStartTime) {
    parts << "starting at ${settings.notificationStartTime}"
  }
  if (settings.notificationEndTime) {
    parts << "ending at ${settings.notificationEndTime}"
  }
  if (parts.size()) {
    msg += ': '
    msg += fancyString(parts)
  }
  return msg
}

def getLockAppById(id) {
  def lockApp = false
  def lockApps = getLockApps()
  if (lockApps) {
    def i = 0
    lockApps.each { app ->
      if (app.lock.id == id) {
        lockApp = app
      }
    }
  }
  return lockApp
}

def getLockAppByIndex(params) {
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

  def lockApp = false
  def lockApps = getLockApps()
  if (lockApps) {
    def i = 0
    lockApps.each { app ->
      if (app.lock.id == state.lastLock) {
        lockApp = app
      }
    }
  }

  return lockApp
}

def availableSlots(selectedSlot) {
  def options = []
  def userApps = getUserApps()
  def lockApps = getLockApps()
  def slotCount = 30
  def usedSlots = []

  userApps.each { userApp ->
    def userSlot = userApp.userSlot.toInteger()
    // do not remove the currently selected slot
    if (selectedSlot?.toInteger() != userSlot) {
      usedSlots << userSlot
    }
  }

  // set slot count to the max available
  lockApps.each { lockApp ->
    def appSlotCount = lockApp.lockCodeSlots()
    // do not remove the currently selected slot
    if (appSlotCount > slotCount) {
      slotCount = appSlotCount
    }
  }

  (1..slotCount).each { slot->
    if (usedSlots.contains(slot)) {
      // do nothing
    } else {
      options << ["${slot}": "Slot ${slot}"]
    }
  }
  return options
}

def keypadMatchingUser(usedCode){
  def correctUser = false
  def userApps = getUserApps()
  userApps.each { userApp ->
    def code
    log.debug userApp.userCode
    if (userApp.isActiveKeypad()) {
      code = userApp.userCode.take(4)
      log.debug "code: ${code} used: ${usedCode}"
      if (code.toInteger() == usedCode.toInteger()) {
        correctUser = userApp
      }
    }
  }
  return correctUser
}

def findAssignedChildApp(lock, slot) {
  def childApp
  def userApps = getUserApps()
  userApps.each { child ->
    if (child.userSlot?.toInteger() == slot) {
      childApp = child
    }
  }
  return childApp
}

def getUserApps() {
  def childApps = []
  def children = getChildApps()
  children.each { child ->
    if (child.theAppType() == 'user') {
      childApps.push(child)
    }
  }
  return childApps
}

def getKeypadApps() {
  def childApps = []
  def children = getChildApps()
  children.each { child ->
    if (child.theAppType() == 'keypad') {
      childApps.push(child)
    }
  }
  return childApps
}

def getLockApps() {
  def childApps = []
  def children = getChildApps()
  children.each { child ->
    if (child.theAppType() == 'lock') {
      childApps.push(child)
    }
  }
  return childApps
}

def setAccess() {
  def lockApps = getLockApps()
  lockApps.each { lockApp ->
    lockApp.setCodes()
  }
}

def locationHandler(evt) {
  setAccess()
}

def theNewChild() {
  return appType.toLowerCase()
}

def anyoneHome(sensors) {
  def result = false
  if(sensors.findAll { it?.currentPresence == "present" }) {
    result = true
  }
  result
}

def apiApp() {
  def app = false
  def children = getChildApps()
  children.each { child ->
    if (child.enableAPI) {
      app = child
    }
  }
  return app
}

def executeHelloPresenceCheck(routines) {
  if (userNoRunPresence && userDoRunPresence == null) {
    if (!anyoneHome(userNoRunPresence)) {
      location.helloHome.execute(routines)
    }
  } else if (userDoRunPresence && userNoRunPresence == null) {
    if (anyoneHome(userDoRunPresence)) {
      location.helloHome.execute(routines)
    }
  } else if (userDoRunPresence && userNoRunPresence) {
    if (anyoneHome(userDoRunPresence) && !anyoneHome(userNoRunPresence)) {
      location.helloHome.execute(routines)
    }
  } else {
    location.helloHome.execute(routines)
  }
}

def debuggerOn() {
  // needed for child apps
  return enableDebug
}

def theAppType() {
  if (parent) {
    return state.appType
  } else {
    return 'main'
  }
}

def debugger(message) {
  return log.debug(message)
  // if (parent) {
  //   def doDebugger = parent.debuggerOn()
  //   if (doDebugger) {
  //     log.debug(message)
  //   }
  // } else {
  //   def doDebugger = debuggerOn()
  //   if (enableDebug) {
  //     return log.debug(message)
  //   }
  // }
}

def lockInstalled() {
  debugger("Lock Installed with settings: ${settings}")
  lockInitialize()
}

def lockUpdated() {
  debugger("Lock Updated with settings: ${settings}")
  lockInitialize()
}

def lockInitialize() {
  // reset listeners
  unsubscribe()
  unschedule()
  subscribe(lock, 'codeChanged', updateCode, [filterEvents:false])
  subscribe(lock, "lock", lockEvent)
  // Allow save and run setup in headless mode
  queSetupLockData()
}


def isUniqueLock() {
  def unique = true
  if (!state.installComplete) {
    // only look if we're not initialized yet.
    def lockApps = parent.getLockApps()
    lockApps.each { lockApp ->
      debugger(lockApp.lock.id)
      if (lockApp.lock.id == lock.id) {
        unique = false
      }
    }
  }
  return unique
}

def lockLandingPage() {
  if (lock) {
    def unique = isUniqueLock()
    if (unique){
      lockMainPage()
    } else {
      lockErrorPage()
    }
  } else {
    lockSetupPage()
  }
}

def lockSetupPage() {
  dynamicPage(name: "lockSetupPage", title: "Setup Lock", nextPage: "lockLandingPage", uninstall: true) {
    section('Lock App Label') {
      label(title: "Name for App", required: true, image: 'http://images.lockmanager.io/app/v1/images/lock.png')
    }
    section("Choose devices for this lock") {
      input(name: "lock", title: "Which Lock?", type: "capability.Lock", multiple: false, required: true)
      input(name: "contactSensor", title: "Which contact sensor?", type: "capability.contactSensor", multiple: false, required: false)
    }
  }
}

def lockMainPage() {
  dynamicPage(name: "lockMainPage", title: "Lock Settings", install: true, uninstall: true) {
    getLockMaxCodes()
    section("Settings") {
      if (state.installComplete) {
        if (state.sweepMode == 'Enabled') {
          def completeCount = sweepProgress()
          def totalSlots = lockCodeSlots()
          def percent = Math.round((completeCount/totalSlots) * 100)
          def estimatedMinutes = ((totalSlots - completeCount) * 6) / 60
          def p = ""
          p += "${percent}%\n"
          p += 'Sweep is in progress.\n'
          p += "Progress: ${completeCount}/${totalSlots}\n\n"

          p += "Estimated time left: ${estimatedMinutes} Minutes\n"
          p += "Lock will set codes after sweep is complete."
          paragraph p
        }
      } else {
        def totalSlots = lockCodeSlots()
        def estimatedMinutes = (totalSlots * 6) / 60
        def para = ""
        if (!skipSweep) {
          para += "This lock will take about \n${estimatedMinutes} Minutes to install.\n\n"
          para += "You may skip the sweep process and save this time."
        } else {
          para += "WARNING:\n"
          para += "You have choosen to skip the sweep process.\n\n"
          para += "This will save about ${estimatedMinutes} Minutes.\n\n"
          para += "Do this at your own risk.  The sweep process will prevent conflicts."
        }
        paragraph para
        input(name: 'skipSweep', title: 'Skip Sweep?', type: 'bool', required: true, description: 'Skip Process', defaultValue: false, submitOnChange: true)
      }

      def actions = location.helloHome?.getPhrases()*.label
      href(name: 'toNotificationPage', page: 'lockNotificationPage', title: 'Notification Settings', image: 'http://images.lockmanager.io/app/v1/images/bullhorn.png')
      if (actions) {
        href(name: 'toLockHelloHomePage', page: 'lockHelloHomePage', title: 'Hello Home Settings', image: 'http://images.lockmanager.io/app/v1/images/home.png')
      }
    }
    section('Setup', hideable: true, hidden: true) {
      label title: 'Label', defaultValue: "Lock: ${lock.label}", required: false, description: 'recommended to start with Lock:'
      input(name: 'lock', title: 'Which Lock?', type: 'capability.lock', multiple: false, required: true)
      input(name: 'contactSensor', title: 'Which contact sensor?', type: "capability.contactSensor", multiple: false, required: false)
      input(name: 'slotCount', title: 'How many slots?', type: 'number', multiple: false, required: false, description: 'Overwrite number of slots supported.')
    }
  }
}

def lockHelloHomePage() {
  dynamicPage(name: 'helloHomePage', title: 'Hello Home Settings (optional)') {
    def actions = location.helloHome?.getPhrases()*.label
    actions?.sort()
    section('Hello Home Phrases') {
      input(name: 'manualUnlockRoutine', title: 'On Manual Unlock', type: 'enum', options: actions, required: false, multiple: true, image: 'http://images.lockmanager.io/app/v1/images/unlock-alt.png')
      input(name: 'manualLockRoutine', title: 'On Manual Lock', type: 'enum', options: actions, required: false, multiple: true, image: 'http://images.lockmanager.io/app/v1/images/lock.png')

      input(name: 'codeUnlockRoutine', title: 'On Code Unlock', type: 'enum', options: actions, required: false, multiple: true, image: 'http://images.lockmanager.io/app/v1/images/unlock-alt.png' )

      paragraph 'Supported on some locks:'
      input(name: 'codeLockRoutine', title: 'On Code Lock', type: 'enum', options: actions, required: false, multiple: true, image: 'http://images.lockmanager.io/app/v1/images/lock.png')
      input(name: 'keypadLockRoutine', title: 'On Keypad Lock', type: 'enum', options: actions, required: false, multiple: true, image: 'http://images.lockmanager.io/app/v1/images/lock.png')

      paragraph 'These restrictions apply to all the above:'
      input "userNoRunPresence", "capability.presenceSensor", title: "DO NOT run Actions if any of these are present:", multiple: true, required: false
      input "userDoRunPresence", "capability.presenceSensor", title: "ONLY run Actions if any of these are present:", multiple: true, required: false
    }
  }
}

def getLockMaxCodes() {
  // Check to see if the Lock Handler knows how many slots there are
  if (lock?.hasAttribute('maxCodes')) {
    def slotCount = lock.latestValue('maxCodes')
    state.codeSlots = slotCount
  }
}

def isInit() {
  return (state.initializeComplete)
}

def lockErrorPage() {
  dynamicPage(name: 'lockErrorPage', title: 'Lock Duplicate', uninstall: true, nextPage: 'lockLandingPage') {
    section('Oops!') {
      paragraph 'The lock that you selected is already installed. Please choose a different Lock or choose Remove'
    }
    section('Choose devices for this lock') {
      input(name: 'lock', title: 'Which Lock?', type: 'capability.lock', multiple: false, required: true)
      input(name: 'contactSensor', title: 'Which contact sensor?', type: 'capability.contactSensor', multiple: false, required: false)
    }
    section('Lock App Label') {
      label(title: "Name for App", required: true, image: 'http://images.lockmanager.io/app/v1/images/lock.png')
    }
  }
}

def lockNotificationPage() {
  dynamicPage(name: 'lockNotificationPage', title: 'Notification Settings') {
    section {
      paragraph 'Some options only work on select locks'
      if (!state.supportsKeypadData) {
        paragraph 'This lock only reports manual messages.\n It does not support code on lock or lock on keypad.'
      }
      if (phone == null && !notification && !recipients) {
        input(name: 'muteLock', title: 'Mute this lock?', type: 'bool', required: false, submitOnChange: true, defaultValue: false, description: 'Mute notifications for this user if notifications are set globally', image: 'http://images.lockmanager.io/app/v1/images/bell-slash-o.png')
      }
      if (!muteLock) {
        input('recipients', 'contact', title: 'Send notifications to', submitOnChange: true, required: false, multiple: true, image: 'http://images.lockmanager.io/app/v1/images/book.png')
        href(name: 'toAskAlexaPage', title: 'Ask Alexa', page: 'lockAskAlexaPage', image: 'http://images.lockmanager.io/app/v1/images/Alexa.png')
        if (!recipients) {
          input(name: 'phone', type: 'text', title: 'Text This Number', description: 'Phone number', required: false, submitOnChange: true)
          paragraph 'For multiple SMS recipients, separate phone numbers with a semicolon(;)'
          input(name: 'notification', type: 'bool', title: 'Send A Push Notification', description: 'Notification', required: false, submitOnChange: true)
        }
        if (phone != null || notification || recipients) {
          input(name: 'notifyManualLock', title: 'On Manual Turn (Lock)', type: 'bool', required: false, image: 'http://images.lockmanager.io/app/v1/images/lock.png')
          input(name: 'notifyManualUnlock', title: 'On Manual Turn (Unlock)', type: 'bool', required: false, image: 'http://images.lockmanager.io/app/v1/images/unlock-alt.png')
          if (state.supportsKeypadData) {
            input(name: 'notifyKeypadLock', title: 'On Keypad Press to Lock', type: 'bool', required: false, image: 'http://images.lockmanager.io/app/v1/images/unlock-alt.png')
          }
        }
      }
    }
    if (!muteLock) {
      section('Only During These Times (optional)') {
        input(name: 'notificationStartTime', type: 'time', title: 'Notify Starting At This Time', description: null, required: false)
        input(name: 'notificationEndTime', type: 'time', title: 'Notify Ending At This Time', description: null, required: false)
      }
    }
  }
}


def queSetupLockData() {
  state.installComplete = true
  runIn(10, setupLockData)
}

def setupLockData() {
  debugger('run lock data setup')

  def lockUsers = parent.getUserApps()
  lockUsers.each { lockUser ->
    // initialize data attributes for this lock.
    lockUser.initializeLockData()
  }
  if (state.requestCount == null) {
    state.requestCount = 0
  }

  initSlots()
}

def initSlots() {
  def codeState = 'unknown'
  if (state.codes == null) {
    // new install!  Start learning!
    state.codes = [:]
    state.requestCount = 0
    // skipSweep may be null
    if (skipSweep != true) {
      state.sweepMode = 'Enabled'
      codeState = 'sweep'
    }
    state.refreshComplete = true
    state.supportsKeypadData = true
    state.pinLength = false
  }
  if (lock?.hasAttribute('pinLength')) {
    state.pinLength = lock.latestValue('pinLength')
  }

  // Check to see if the Lock Handler knows how many slots there are
  if (lock?.hasAttribute('maxCodes')) {
    def slotCount = lock.latestValue('maxCodes')
    state.codeSlots = slotCount
  }

  def userCodeSlots = getUserSlotList()
  def codeSlots = lockCodeSlots()

  (1..codeSlots).each { slot ->
    def control = 'available'
    if (state.codes["slot${slot}"] == null) {
      state.codes["slot${slot}"] = [:]
      state.codes["slot${slot}"].slot = slot
      state.codes["slot${slot}"].code = null
      // set attempts
      state.codes["slot${slot}"].attempts = 0
      state.codes["slot${slot}"].recoveryAttempts = 0
      state.codes["slot${slot}"].namedSlot = false
      state.codes["slot${slot}"].codeState = codeState
      state.codes["slot${slot}"].control = control
    }

    // manage controll type
    def currentControl = state.codes["slot${slot}"].control
    switch (currentControl) {
      case 'available':
      case 'controller':
        if (userCodeSlots.contains(slot.toInteger())) {
          control = 'controller'
        }
        break
      case 'api':
      default:
      // nothing to do
        break
    }
    state.codes["slot${slot}"].control = control
  }
  if (state.sweepMode == 'Enabled') {
    state.sweepProgress = 0
    sweepSequance()
  } else {
    setCodes()
  }
}

def sweepSequance() {
  def codeSlots = lockCodeSlots()
  def array = []
  def count = 0
  def completeCount = 0
  (1..codeSlots).each { slot ->
    // sweep in packages of 10
    if (count == 10) {
      // do nothing ~ We're going to stop adding codes for now.
    } else {
      def slotData = state.codes["slot${slot}"]
      if (slotData.codeState == 'sweep') {
        count++
        array << ["code${slotData.slot}", null]
      } else {
        // This code is already known/unset!
        completeCount++
        state.sweepProgress = completeCount
      }
    }
  }

  // allow 10 and 5 seconds per code delete
  def timeOut = 10 + (count * 6)

  def json = new groovy.json.JsonBuilder(array).toString()
  if (json != '[]') {
    debugger("Progress: ${completeCount}/${codeSlots} Data: ${json}")
    lock.updateCodes(json)
    runIn(timeOut, sweepSequance)
  } else {
    debugger('Sweep Completed!')
    state.sweepMode = 'Disabled'
    // Allow some cooldown time to prevent conflicts
    runIn(15, setCodes)
  }
}

def withinAllowed() {
  return (state.requestCount <= allowedAttempts())
}

def allowedAttempts() {
  return lockCodeSlots() * 2
}

def updateCode(event) {
  def data = new JsonSlurper().parseText(event.data)
  def name = event.name
  def description = event.descriptionText
  def activity = event.value =~ /(\d{1,3}).(\w*)/
  def slot = activity[0][1].toInteger()
  def activityType = activity[0][2]
  def previousCode = state.codes["slot${slot}"].code

  debugger("name: ${name} slot: ${slot} data: ${data} description: ${description} activity: ${activity[0]}")

  def code = null
  def userApp = findSlotUserApp(slot)
  if (userApp) {
    code = userApp.userCode
  }

  def codeState
  def previousCodeState = state.codes["slot${slot}"].codeState
  switch (slot) {
    case 251:
      // code is duplicate of master
      if (state.incorrectSlots.size() == 1) {
        // the only slot to set must be the incorrect one!
        def errorSlot = state.incorrectSlots[0]
        userApp = findSlotUserApp(errorSlot)
        // We can set this reason code immediatly
        userApp.disableAndSetReason(lock.id, 'Conflicts with Master Code')

        state.codes["slot${errorSlot}"].code = null
        state.codes["slot${errorSlot}"].codeState = 'known'
      }
      break
    default:
      switch (activityType) {
        case 'unset':
          debugger("Slot:${slot} is no longer set!")
          if (previousCodeState == 'unset' || state.sweepMode) {
             codeState = 'correct'
          } else {
            // We were not expecting an unset!
            codeState = 'unexpected'
          }
          state.codes["slot${slot}"].code = null
          state.codes["slot${slot}"].codeState = codeState
          break
        case 'changed':
        case 'set':
          switch(previousCodeState) {
            case 'set':
            case 'recovery':
              codeState = 'correct'
              debugger("Slot:${slot} is set!")
              break
            default:
              // We didnt expect a set, lets unset it and set the correct code
              debugger("Slot:${slot} unexpected set!")
              if (userApp) {
                // we have to delete it and set it again,
                // because if it's the same as user's PIN
                // it will error
                failRecovery(slot, previousCodeState, userApp)
              } else {
                // We can just delete the code because we don't want anything there
                code = 'invalid'
                codeState = 'unexpected'
              }

              break
          }
          state.codes["slot${slot}"].code = code
          state.codes["slot${slot}"].codeState = codeState
          break
        case 'failed':
          failRecovery(slot, previousCodeState, userApp);
          break
        default:
          // unknown action
          break
      }
  } //end switch(slot)

  switch (codeState) {
    case 'correct':
      if (previousCodeState == 'set') {
        codeInform(slot, 'access')
      } else if (previousCodeState == 'unset') {
        codeInform(slot, 'revoked')
      }

      break
    case 'unexpected':
      // run code logic, and reset code
      switch (activityType) {
        case 'set':
        case 'changed':
          codeInform(slot, 'unexpected-set')
          break
        case 'unset':
        default:
          codeInform(slot, 'unexpected-unset')
          break
      }
      debugger('Unexpected change!  Scheduling code logic.')
      runIn(25, setCodes)
      break
    case 'failed':
      if (previousCodeState == 'unset') {
        // I'm not sure if this would ever happen...
        codeInform(slot, 'failed-unset')
      } else if (previousCodeState == 'set') {
        codeInform(slot, 'failed-set')
      }
  }
}

def failRecovery(slot, previousCodeState, userApp) {
  def attempts = state.codes["slot${slot}"].recoveryAttempts
  if (attempts > 3) {
    if (userApp) {
      userApp.disableAndSetReason(lock.id, 'Code failed to set.  Possible duplicate or invalid PIN')
    }
    debugger("Slot:${slot} failed! Recovery failed.")
    state.codes["slot${slot}"].code = 'invalid'
    state.codes["slot${slot}"].codeState = 'failed'
  } else {
    debugger("Slot:${slot} failed, attempting recovery.")
    state.codes["slot${slot}"].recoveryAttempts = attempts + 1
    state.codes["slot${slot}"].code = 'invalid'
    state.codes["slot${slot}"].codeState = 'recovery'
  }
}

def lockEvent(evt) {
  def data = new JsonSlurper().parseText(evt.data)
  debugger("Lock event. ${data}")

  switch(data.method) {
    case 'keypad':
      keypadLockEvent(evt, data)
      break
    case 'manual':
      manualUnlock(evt)
      break
    case 'command':
      commandUnlock(evt)
      break
    case 'auto':
      autoLock(evt)
      break
  }
}

def keypadLockEvent(evt, data) {
  def message
  def userApp = findSlotUserApp(data.usedCode)
  if (evt.value == 'locked') {
    if (userApp) {
      userDidLock(userApp)
    } else {
      message = "${lock.label} was locked by keypad"
      debugger(message)
      if (keypadLockRoutine) {
        executeHelloPresenceCheck(keypadLockRoutine)
      }
      if (notifyKeypadLock) {
        sendLockMessage(message)
      }
      if (alexaKeypadLock) {
        askAlexaLock(message)
      }
    }
  } else if (evt.value == 'unlocked') {
    if (userApp) {
      userDidUnlock(userApp)
    } else {
      debugger('Lock was locked by unknown user!')
      // unlocked by unknown user?
    }
  }
}

def userDidLock(userApp) {
  def message = "${lock.label} was locked by ${userApp.userName}"
  debugger(message)
  // user specific
  if (userApp.userLockPhrase) {
    userApp.executeHelloPresenceCheck(userApp.userLockPhrase)
  }
  // lock specific
  if (codeLockRoutine) {
    userApp.executeHelloPresenceCheck(codeLockRoutine)
  }
  // global
  if (parent.codeLockRoutine) {
    parent.executeHelloPresenceCheck(parent.codeLockRoutine)
  }

  // messages
  if (userApp.notifyLock || parent.notifyLock) {
    userApp.sendUserMessage(message)
  }
  if (userApp.alexaLock || parent.alexaLock) {
    userApp.sendAskAlexaLock(message)
  }
}

def userDidUnlock(userApp) {
  def message
  message = "${lock.label} was unlocked by ${userApp.userName}"
  debugger(message)
  userApp.incrementLockUsage(lock.id)
  if (!userApp.isNotBurned()) {
    parent.setAccess()
    message += '.  Now burning code.'
  }
  // user specific
  if (userApp.userUnlockPhrase) {
    userApp.executeHelloPresenceCheck(userApp.userUnlockPhrase)
  }
  // lock specific
  if (codeUnlockRoutine) {
    userApp.executeHelloPresenceCheck(codeUnlockRoutine)
  }
  // global
  if (parent.codeUnlockRoutine) {
    parent.executeHelloPresenceCheck(parent.codeUnlockRoutine)
  }

  //Send Message
  if (userApp.notifyAccess || parent.notifyAccess) {
    userApp.sendUserMessage(message)
  }
  if (userApp.alexaAccess || parent.alexaAccess) {
    userApp.sendAskAlexaLock(message)
  }
}

def manualUnlock(evt) {
  def message
  if (evt.value == 'locked') {
    // locked manually
    message = "${lock.label} was locked manually"
    debugger(message)
    // lock specific
    if (manualLockRoutine) {
      executeHelloPresenceCheck(manualLockRoutine)
    }
    // global
    if (parent.manualLockRoutine) {
      parent.executeHelloPresenceCheck(parent.manualLockRoutine)
    }

    if (notifyManualLock) {
      sendLockMessage(message)
    }
    if (alexaManualLock) {
      askAlexaLock(message)
    }
  } else if (evt.value == 'unlocked') {
    message = "${lock.label} was unlocked manually"
    debugger(message)
    // lock specific
    if (manualUnlockRoutine) {
      executeHelloPresenceCheck(manualUnlockRoutine)
    }
    // global
    if (parent.manualUnlockRoutine) {
      parent.executeHelloPresenceCheck(parent.manualUnlockRoutine)
    }
  }
}

def commandUnlock(evt) {
  // no options for this scenario yet
}
def autoLock(evt) {
  // no options for this scenario yet
}

def setCodes() {
  def setValue
  def name
  // set what each slot should be in memory
  if (state.sweepMode == 'Enabled') {
    debugger('Not running code logic, Sweep mode is Enabled')
    return false
  }
  // set incorrect slot array to blank
  state.incorrectSlots = []

  debugger('run code logic')
  def codes = state.codes
  codes.each { data ->
    data = data.value
    name = false
    switch(data.control) {
      case 'controller':
        def lockUser = findSlotUserApp(data.slot)
        def codeState = state.codes["slot${data.slot}"].codeState
        if (lockUser?.isActive(lock.id) && codeState != 'recovery') {
          // is active, should be set
          setValue = lockUser.userCode.toString()
          state.codes["slot${data.slot}"].correctValue = setValue
          if (data.code.toString() != setValue) {
            state.codes["slot${data.slot}"].codeState = 'set'
          } else {
            // set name only if code is already set
            name = lockUser.userName
          }
        } else {
          // is inactive, should not be set
          setValue = null
          state.codes["slot${data.slot}"].correctValue = null
          if (data.code != setValue) {
            state.codes["slot${data.slot}"].codeState = 'unset'
          }
        }
        break
      case 'api':
        if (data.correctCode != null) {
          if (data.correctCode != data.code) {
            state.codes["slot${data.slot}"].codeState = 'unset'
          }
        } else if (data.correctCode.toString() != data.code.toString()) {
          state.codes["slot${data.slot}"].codeState = 'set'
        }

        // do nothing, correct code set by API service
        break
      default:
        // only overwrite if enabled
        if (parent.overwriteMode) {
          state.codes["slot${data.slot}"].correctValue = null
        }
        break
    }
    // ensure name is set correctly
    nameSlot(data.slot, name)
  }
  // After setting code data, send to the lock
  runIn(15, loadCodes)
}

def loadCodes() {
  // send codes to lock
  debugger('running load codes')
  def codesToSet
  def unsetCodes = collectCodesToUnset()
  // do this so we unset codes first
  if (unsetCodes.size > 0) {
    codesToSet = unsetCodes
  } else {
    codesToSet = collectCodesToSet()
  }

  def json = new groovy.json.JsonBuilder(codesToSet).toString()
  if (json != '[]') {
    debugger("update: ${json}")
    lock.updateCodes(json)
    // After sending codes, run memory logic again
    def timeOut = (codesToSet.size() * 6) + 10
    runIn(timeOut, setCodes)
  } else {
    // All done, codes should be correct
    debugger('No codes to set')
  }
}

def collectCodesToUnset() {
  def codes = state.codes

  def incorrectSlots = []
  def array = []
  def count = 0

  codes.each { data ->
    data = data.value

    if (count < 10) {
      def currentCode = data.code
      def correctCode = data.correctValue

      if (correctCode == null && currentCode != null) {
        array << ["code${data.slot}", code]
        incorrectSlots << data.slot
        count++
      }
    }
  }

  state.incorrectSlots = incorrectSlots
  return array
}

def collectCodesToSet() {
  def codes = state.codes

  def incorrectSlots = []
  def array = []
  def count = 0

  codes.each { data ->
    data = data.value

    if (count < 10) {
      def currentCode = data.code.toString()
      def correctCode = data.correctValue.toString()

      if (correctCode != currentCode && state.codes["slot${data.slot}"].attempts < 10) {
        array << ["code${data.slot}", correctCode]
        incorrectSlots << data.slot
        // increment attempt count
        state.codes["slot${data.slot}"].attempts = data.attempts + 1
        count++
      } else if (correctCode != currentCode && state.codes["slot${data.slot}"].attempts >= 10) {
        state.codes["slot${data.slot}"].attempts = 0
        // we've tried this slot 10 times, time to disable it
        def userApp = findSlotUserApp(data.slot)
        userApp?.disableLock(lock.id)
      } else {
        // code is correct
        state.codes["slot${data.slot}"].attempts = 0
      }
    }
  }

  state.incorrectSlots = incorrectSlots
  return array
}

def getUserSlotList() {
  def userSlots = []
  def lockUsers = parent.getUserApps()
  lockUsers.each { lockUser ->
    userSlots << lockUser.userSlot.toInteger()
  }
  return userSlots
}

def findSlotUserApp(slot) {
  if (slot) {
    def lockUsers = parent.getUserApps()
    return lockUsers.find { app -> app.userSlot.toInteger() == slot.toInteger() }
  } else {
    return false
  }
}

def codeInform(slot, action) {
  def userApp = findSlotUserApp(slot)
  if (userApp) {
    def message = ''
    def shouldSend = false
    switch(action) {
      case 'access':
        message = "${userApp.userName} now has access to ${lock.label}"
        // add name
        nameSlot(slot, userApp.userName)
        if (userApp.notifyAccessStart || parent.notifyAccessStart) {
          shouldSend = true
        }
        break
      case 'revoke':
        // remove name
        nameSlot(slot, false)
        message = "${userApp.userName} no longer has access to ${lock.label}"
        if (userApp.notifyAccessEnd || parent.notifyAccessEnd) {
          shouldSend = true
        }
        break
      case 'unexpected-set':
        message = "Unexpected code in Slot:${slot}. ${userApp.userName} may not have valid access to ${lock.label}. Checking for issues."
        shouldSend = true
        break
      case 'unexpected-unset':
        message = "Unexpected code delete Slot:${slot}. ${userApp.userName} may not have valid access to ${lock.label}. Checking for issues."
        shouldSend = true
        break
      case 'failed-set':
        def disabledReason = userApp.disabledReason()
        message = "Controller failed to set code for ${userApp.name}. ${disabledReason}"
        shouldSend = true
        break
    }

    if (shouldSend) {
      userApp.sendUserMessage(message)
    }
    debugger(message)
  } else {
    // remove set user name, no app
    nameSlot(slot, false)
  }
}

def isCodeComplete() {
  if (state.sweepMode == 'Enabled') {
    return false
  } else {
    return true
  }
}

def doorOpenCheck() {
  def currentState = contact.contactState
  if (currentState?.value == 'open') {
    def msg = "${contact.displayName} is open.  Scheduled lock failed."
    log.info msg
    if (sendPushMessage) {
      sendPush msg
    }
    if (phone) {
      sendSms phone, msg
    }
  } else {
    lockMessage()
    lock.lock()
  }
}

def lockMessage() {
  def msg = "Locking ${lock.displayName} due to scheduled lock."
  log.info msg
}


def sendLockMessage(message) {
  if (notificationStartTime != null && notificationEndTime != null) {
    def start = timeToday(notificationStartTime)
    def stop = timeToday(notificationEndTime)
    def now = rightNow()
    if (start.before(now) && stop.after(now)){
      sendMessage(message)
    }
  } else {
    sendMessage(message)
  }
}
def sendMessage(message) {
  if (!muteLock) {
    if (recipients) {
      sendNotificationToContacts(message, recipients)
    } else {
      if (notification) {
        sendPush(message)
      } else {
        sendNotificationEvent(message)
      }
      if (phone) {
        if ( phone.indexOf(';') > 1){
          def phones = phone.split(';')
          for ( def i = 0; i < phones.size(); i++) {
            sendSms(phones[i], message)
          }
        }
        else {
          sendSms(phone, message)
        }
      }
    }
  } else {
    sendNotificationEvent(message)
  }
}

def nameSlot(slot, name) {
  if (state.codes["slot${slot}"].namedSlot != name) {
    state.codes["slot${slot}"].namedSlot = name
    lock.nameSlot(slot, name)
  }
}

def askAlexaLock(message) {
  if (!muteLock) {
    if (alexaStartTime != null && alexaEndTime != null) {
      def start = timeToday(alexaStartTime)
      def stop = timeToday(alexaEndTime)
      def now = rightNow()
      if (start.before(now) && stop.after(now)){
        sendAskAlexa(message)
      }
    } else {
      sendAskAlexa(message)
    }
  }
}

def sendAskAlexaLock(message) {
  sendLocationEvent(name: 'AskAlexaMsgQueue',
                    value: 'LockManager/Lock',
                    isStateChange: true,
                    descriptionText: message,
                    unit: "Lock//${lock.label}")
}

def apiCodeUpdate(slot, code, control) {
  state.codes["slot${slot}"]['correctValue'] = code
  state.codes["slot${slot}"]['control'] = control
  setCodes()
}

def isRefreshComplete() {
  return state.refreshComplete
}

def totalUsage() {
  def usage = 0
  def userApps = parent.getUserApps()
  userApps.each { userApp ->
    def lockUsage = userApp.getLockUsage(lock.id)
    usage = usage + lockUsage
  }
  return usage
}

def lockCodeSlots() {
  // default to 30
  def codeSlots = 30
  if (slotCount) {
    // return the user defined value
    codeSlots = slotCount
  } else if (state?.codeSlots) {
    codeSlots = state.codeSlots.toInteger()
  }
  return codeSlots
}

def codeData() {
  return state.codes
}

def userPageCount() {
  def sortData = state.codes.sort{it.value.slot}
  def data = sortData.collect{ it }
  return data.collate(30).size()
}

def codeDataPaginated(page) {
  // collect a paginated list to prevent rate limit issues
  def sortData = state.codes.sort{it.value.slot}
  def data = sortData.collect{ it }
  return data.collate(30)[page]
}

def slotData(slot) {
  state.codes["slot${slot}"]
}

def lockState() {
  state.lockState
}

def lockSort() {
  if (lock) {
    return lock.id
  } else {
    return 1
  }
}

def sweepProgress() {
  state.sweepProgress
}

def enableUser(slot) {
  state.codes["slot${slot}"].attempts = 0
  runIn(10, setCodes)
}

def pinLength() {
  return state.pinLength
}

def userInstalled() {
  log.debug "Installed with settings: ${settings}"
  userInitialize()
}

def userUpdated() {
  log.debug "Updated with settings: ${settings}"
  userInitialize()
}

def userInitialize() {
  // reset listeners
  unsubscribe()
  unschedule()

  // setup data
  initializeLockData()
  initializeLocks()

  // set listeners
  subscribeToSchedule()
}

def userUninstalled() {
  unschedule()

  // prompt locks to delete this user
  initializeLocks()
}

def subscribeToSchedule() {
  if (startTime) {
    // sechedule time of start!
    log.debug 'scheduling time start'
    schedule(startTime, 'scheduledStartTime')
  }
  if (endTime) {
    // sechedule time of end!
    log.debug 'scheduling time end'
    schedule(endTime, 'scheduledEndTime')
  }
  if (startDateTime()) {
    // schedule calendar start!
    log.debug 'scheduling calendar start'
    runOnce(startDateTime().format(smartThingsDateFormat(), timeZone()), 'calendarStart')
  }
  if (endDateTime()) {
    // schedule calendar end!
    log.debug 'scheduling calendar end'
    runOnce(endDateTime().format(smartThingsDateFormat(), timeZone()), 'calendarEnd')
  }
}

def scheduledStartTime() {
  parent.setAccess()
}
def scheduledEndTime() {
  parent.setAccess()
}
def calendarStart() {
  parent.setAccess()
  if (calStartPhrase) {
    location.helloHome.execute(calStartPhrase)
  }
}
def calendarEnd() {
  parent.setAccess()
  if (calEndPhrase) {
    location.helloHome.execute(calEndPhrase)
  }
}

def initializeLockData() {
  debugger('Initialize lock data for user.')
  def lockApps = parent.getLockApps()
  lockApps.each { lockApp ->
    def lockId = lockApp.lock.id
    if (state."lock${lockId}" == null) {
      state."lock${lockId}" = [:]
      state."lock${lockId}".enabled = true
      state."lock${lockId}".usage = 0
    }
  }
}

def initializeLocks() {
  debugger('User asking for lock init')
  def lockApps = parent.getLockApps()
  lockApps.each { lockApp ->
    lockApp.queSetupLockData()
  }
}

def incrementLockUsage(lockId) {
  // this is called by a lock app when this user
  // used their code to lock the door
  state."lock${lockId}".usage = state."lock${lockId}".usage + 1
}

def lockReset(lockId) {
  state."lock${lockId}".enabled = true
  state."lock${lockId}".disabledReason = ''
  def lockApp = parent.getLockAppById(lockId)
  lockApp.enableUser(userSlot)
}

def userLandingPage() {
  if (userName) {
    userMainPage()
  } else {
    userSetupPage()
  }
}

def userSetupPage() {
  dynamicPage(name: 'userSetupPage', title: 'Setup Lock', nextPage: 'userMainPage', uninstall: true) {
    section('User App Label') {
      label(title: "Name for App", required: true, image: 'http://images.lockmanager.io/app/v1/images/user.png')
    }
    section('Choose details for this user') {
      input(name: 'userName', type: 'text', title: 'Name for User', required: true)
      input(name: 'userCode', type: 'text', title: userCodeInputTitle(), required: false, defaultValue: settings.'userCode', refreshAfterSelection: true)
      input(name: 'userSlot', type: 'enum', options: parent.availableSlots(settings.userSlot), title: 'Select slot', required: true, refreshAfterSelection: true )
    }
  }
}

def userMainPage() {
  //reset errors on each load
  dynamicPage(name: 'userMainPage', title: '', install: true, uninstall: true) {
    section('User Settings') {
      def usage = getAllLocksUsage()
      def text
      if (isActive()) {
        text = 'active'
      } else {
        text = 'inactive'
      }
      paragraph "${text}/${usage}"
      input(name: 'userCode', type: 'text', title: userCodeInputTitle(), required: false, defaultValue: settings.'userCode', refreshAfterSelection: true)
      input(name: 'userEnabled', type: 'bool', title: "User Enabled?", required: false, defaultValue: true, refreshAfterSelection: true)
    }
    section('Additional Settings') {
      def actions = location.helloHome?.getPhrases()*.label
      if (actions) {
        actions.sort()
        input name: 'userUnlockPhrase', type: 'enum', title: 'Hello Home Phrase on unlock', multiple: true, required: false, options: actions, refreshAfterSelection: true, image: 'http://images.lockmanager.io/app/v1/images/home.png'
        input name: 'userLockPhrase', type: 'enum', title: 'Hello Home Phrase on lock', description: 'Available on select locks only', multiple: true, required: false, options: actions, refreshAfterSelection: true, image: 'http://images.lockmanager.io/app/v1/images/home.png'

        input "userNoRunPresence", "capability.presenceSensor", title: "DO NOT run Actions if any of these are present:", multiple: true, required: false
        input "userDoRunPresence", "capability.presenceSensor", title: "ONLY run Actions if any of these are present:", multiple: true, required: false
      }
      input(name: 'burnAfterInt', title: 'How many uses before burn?', type: 'number', required: false, description: 'Blank or zero is infinite', image: 'http://images.lockmanager.io/app/v1/images/fire.png')
      href(name: 'toSchedulingPage', page: 'schedulingPage', title: 'Schedule (optional)', description: schedulingHrefDescription(), state: schedulingHrefDescription() ? 'complete' : '', image: 'http://images.lockmanager.io/app/v1/images/calendar.png')
      href(name: 'toNotificationPage', page: 'userNotificationPage', title: 'Notification Settings', description: userNotificationPageDescription(), state: userNotificationPageDescription() ? 'complete' : '', image: 'http://images.lockmanager.io/app/v1/images/bullhorn.png')
      href(name: 'toUserKeypadPage', page: 'userKeypadPage', title: 'Keypad Routines (optional)', image: 'http://images.lockmanager.io/app/v1/images/keypad.png')
    }
    section('Locks') {
      initializeLockData()
      def lockApps = parent.getLockApps()

      lockApps.each { app ->
        href(name: "toLockPage${app.lock.id}", page: 'userLockPage', params: [id: app.lock.id], description: lockPageDescription(app.lock.id), required: false, title: app.lock.label, image: lockPageImage(app.lock) )
      }
    }
    section('Setup', hideable: true, hidden: true) {
      label(title: "Name for App", defaultValue: 'User: ' + userName, required: true, image: 'http://images.lockmanager.io/app/v1/images/user.png')
      input name: 'userName', type: "text", title: "Name for user", required: true, image: 'http://images.lockmanager.io/app/v1/images/user.png'
      input(name: "userSlot", type: "enum", options: parent.availableSlots(settings.userSlot), title: "Select slot", required: true, refreshAfterSelection: true )
    }
  }
}

def userCodeInputTitle() {
  def title = 'Code 4-8 digits'
  def pinLength
  def lockApps = parent.getLockApps()
  lockApps.each { lockApp ->
    pinLength = lockApp.pinLength()
    if (pinLength) {
      title = "Code (Must be ${lockApp.lock.latestValue('pinLength')} digits)"
    }
  }
  return title
}

def lockPageImage(lock) {
  if (!state."lock${lock.id}".enabled || settings."lockDisabled${lock.id}") {
    return 'http://images.lockmanager.io/app/v1/images/ban.png'
  } else {
    return 'http://images.lockmanager.io/app/v1/images/lock.png'
  }
}

def lockInfoPageImage(lock) {
  if (!state."lock${lock.id}".enabled || settings."lockDisabled${lock.id}") {
    return 'http://images.lockmanager.io/app/v1/images/user-times.png'
  } else {
    return 'http://images.lockmanager.io/app/v1/images/user.png'
  }
}

def userLockPage(params) {
  dynamicPage(name:"userLockPage", title:"Lock Settings") {
    debugger('current params: ' + params)
    def lock = getLock(params)
    def lockApp = parent.getLockAppById(lock.id)
    def slotData = lockApp.slotData(userSlot)

    def usage = state."lock${lock.id}".usage

    debugger('found lock id?: ' + lock?.id)

    if (!state."lock${lock.id}".enabled) {
      section {
        paragraph "WARNING:\n\nThis user has been disabled.\n${state."lock${lock.id}".disabledReason}", image: 'http://images.lockmanager.io/app/v1/images/ban.png'
        href(name: 'toReEnableUserLockPage', page: 'reEnableUserLockPage', title: 'Reset User', description: 'Retry setting this user.',  params: [id: lock.id], image: 'http://images.lockmanager.io/app/v1/images/refresh.png' )
      }
    }
    section("${deviceLabel(lock)} settings for ${app.label}") {
      if (slotData.code) {
        paragraph "Lock is currently set to ${slotData.code}"
      }
      paragraph "User unlock count: ${usage}"
      if(slotData.attempts > 0) {
        paragraph "Lock set failed try ${slotData.attempts}/10"
      }
      input(name: "lockDisabled${lock.id}", type: 'bool', title: 'Disable lock for this user?', required: false, defaultValue: settings."lockDisabled${lock.id}", refreshAfterSelection: true, image: 'http://images.lockmanager.io/app/v1/images/ban.png' )
      href(name: 'toLockResetPage', page: 'lockResetPage', title: 'Reset Lock', description: 'Reset lock data for this user.',  params: [id: lock.id], image: 'http://images.lockmanager.io/app/v1/images/refresh.png' )
    }
  }
}

def userKeypadPage() {
  dynamicPage(name: 'userKeypadPage',title: 'Keypad Settings (optional)', install: true, uninstall: true) {
    def actions = location.helloHome?.getPhrases()*.label
    actions?.sort()
    section("Settings") {
      paragraph 'settings here are for this user only. When this user enters their passcode, run these routines'
      input(name: 'armRoutine', title: 'Arm/Away routine', type: 'enum', options: actions, required: false, multiple: true)
      input(name: 'disarmRoutine', title: 'Disarm routine', type: 'enum', options: actions, required: false, multiple: true)
      input(name: 'stayRoutine', title: 'Arm/Stay routine', type: 'enum', options: actions, required: false, multiple: true)
      input(name: 'nightRoutine', title: 'Arm/Night routine', type: 'enum', options: actions, required: false, multiple: true)
    }
  }
}

def lockPageDescription(lock_id) {
  def usage = state."lock${lock_id}".usage
  def description = "Entries: ${usage} "
  if (!state."lock${lock_id}".enabled) {
    description += '// ERROR//DISABLED'
  }
  if (settings."lockDisabled${lock_id}") {
    description += ' DISABLED'
  }
  description
}

def reEnableUserLockPage(params) {
  // do reset
  def lock = getLock(params)
  lockReset(lock.id)

  dynamicPage(name:'reEnableUserLockPage', title:'User re-enabled') {
    section {
      paragraph 'Lock has been reset.'
    }
    section {
      href(name: 'toMainPage', title: 'Back To Setup', page: 'userMainPage')
    }
  }
}

def lockResetPage(params) {
  // do reset
  def lock = getLock(params)

  state."lock${lock.id}".usage = 0
  lockReset(lock.id)

  dynamicPage(name:'lockResetPage', title:'Lock reset') {
    section {
      paragraph 'Lock has been reset.'
    }
    section {
      href(name: 'toMainPage', title: 'Back To Setup', page: 'userMainPage')
    }
  }
}

def schedulingPage() {
  dynamicPage(name: 'schedulingPage', title: 'Rules For Access Scheduling') {

    section {
      href(name: 'toCalendarPage', title: 'Calendar', page: 'calendarPage', description: calendarHrefDescription(), state: calendarHrefDescription() ? 'complete' : '')
    }

    section {
      input(name: 'days', type: 'enum', title: 'Allow User Access On These Days', description: 'Every day', required: false, multiple: true, options: ['Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday', 'Sunday'], submitOnChange: true)
    }
    section {
      input(name: 'activeModes', title: 'Allow Access only when in any of these modes', type: 'mode', required: false, multiple: true, submitOnChange: true)
    }
    section {
      input(name: 'startTime', type: 'time', title: 'Start Time', description: null, required: false)
      input(name: 'endTime', type: 'time', title: 'End Time', description: null, required: false)
    }
  }
}

def calendarPage() {
  dynamicPage(name: "calendarPage", title: "Calendar Access") {
    section() {
      paragraph "Enter each field carefully."
    }
    def actions = location.helloHome?.getPhrases()*.label
    section("Start Date") {
      input name: "startDay", type: "number", title: "Day", required: false
      input name: "startMonth", type: "number", title: "Month", required: false
      input name: "startYear", type: "number", description: "Format(yyyy)", title: "Year", required: false
      input name: "calStartTime", type: "time", title: "Start Time", description: null, required: false
      if (actions) {
        actions.sort()
        input name: "calStartPhrase", type: "enum", title: "Hello Home Phrase", multiple: true, required: false, options: actions, refreshAfterSelection: true
      }
    }
    section("End Date") {
      input name: "endDay", type: "number", title: "Day", required: false
      input name: "endMonth", type: "number", title: "Month", required: false
      input name: "endYear", type: "number", description: "Format(yyyy)", title: "Year", required: false
      input name: "calEndTime", type: "time", title: "End Time", description: null, required: false
      if (actions) {
        actions.sort()
        input name: "calEndPhrase", type: "enum", title: "Hello Home Phrase", multiple: true, required: false, options: actions, refreshAfterSelection: true
      }
    }
  }
}

def userNotificationPage() {
  dynamicPage(name: 'userNotificationPage', title: 'Notification Settings') {

    section {
      if (phone == null && !notification && !recipients) {
        input(name: 'muteUser', title: 'Mute this user?', type: 'bool', required: false, submitOnChange: true, defaultValue: false, description: 'Mute notifications for this user if notifications are set globally', image: 'http://images.lockmanager.io/app/v1/images/bell-slash-o.png')
      }
      if (!muteUser) {
        input('recipients', 'contact', title: 'Send notifications to', submitOnChange: true, required: false, multiple: true, image: 'http://images.lockmanager.io/app/v1/images/book.png')
        href(name: 'toAskAlexaPage', title: 'Ask Alexa', page: 'userAskAlexaPage', image: 'http://images.lockmanager.io/app/v1/images/Alexa.png')
        if (!recipients) {
          input(name: 'phone', type: 'text', title: 'Text This Number', description: 'Phone number', required: false, submitOnChange: true)
          paragraph 'For multiple SMS recipients, separate phone numbers with a semicolon(;)'
          input(name: 'notification', type: 'bool', title: 'Send A Push Notification', description: 'Notification', required: false, submitOnChange: true)
        }
        if (phone != null || notification || recipients) {
          input(name: 'notifyAccess', title: 'on User Entry', type: 'bool', required: false, image: 'http://images.lockmanager.io/app/v1/images/unlock-alt.png')
          input(name: 'notifyLock', title: 'on Lock', type: 'bool', required: false, image: 'http://images.lockmanager.io/app/v1/images/lock.png')
          input(name: 'notifyAccessStart', title: 'when granting access', type: 'bool', required: false, image: 'http://images.lockmanager.io/app/v1/images/check-circle-o.png')
          input(name: 'notifyAccessEnd', title: 'when revoking access', type: 'bool', required: false, image: 'http://images.lockmanager.io/app/v1/images/times-circle-o.png')
        }
      }
    }
    if (!muteUser) {
      section('Only During These Times (optional)') {
        input(name: 'notificationStartTime', type: 'time', title: 'Notify Starting At This Time', description: null, required: false)
        input(name: 'notificationEndTime', type: 'time', title: 'Notify Ending At This Time', description: null, required: false)
      }
    }
  }
}

def userAskAlexaPage() {
  dynamicPage(name: 'userAskAlexaPage', title: 'Ask Alexa Message Settings') {
    section('Que Messages with the Ask Alexa app') {
      input(name: 'alexaAccess', title: 'on User Entry', type: 'bool', required: false, image: 'http://images.lockmanager.io/app/v1/images/unlock-alt.png')
      input(name: 'alexaLock', title: 'on Lock', type: 'bool', required: false, image: 'http://images.lockmanager.io/app/v1/images/lock.png')
      input(name: 'alexaAccessStart', title: 'when granting access', type: 'bool', required: false, image: 'http://images.lockmanager.io/app/v1/images/check-circle-o.png')
      input(name: 'alexaAccessEnd', title: 'when revoking access', type: 'bool', required: false, image: 'http://images.lockmanager.io/app/v1/images/times-circle-o.png')
    }
    section('Only During These Times (optional)') {
      input(name: 'alexaStartTime', type: 'time', title: 'Notify Starting At This Time', description: null, required: false)
      input(name: 'alexaEndTime', type: 'time', title: 'Notify Ending At This Time', description: null, required: false)
    }
  }
}

def timeZone() {
  def zone
  if(location.timeZone) {
    zone = location.timeZone
  } else {
    zone = TimeZone.getDefault()
  }
  return zone
}

public smartThingsDateFormat() { "yyyy-MM-dd'T'HH:mm:ss.SSSZ" }

public humanReadableStartDate() {
  new Date().parse(smartThingsDateFormat(), startTime).format('h:mm a', timeZone(startTime))
}
public humanReadableEndDate() {
  new Date().parse(smartThingsDateFormat(), endTime).format('h:mm a', timeZone(endTime))
}

def readableDateTime(date) {
  new Date().parse(smartThingsDateFormat(), date.format(smartThingsDateFormat(), timeZone())).format("EEE, MMM d yyyy 'at' h:mma", timeZone())
}


def getLockUsage(lock_id) {
  return state."lock${lock_id}".usage
}

def getAllLocksUsage() {
  def usage = 0
  def lockApps = parent.getLockApps()
  lockApps.each { lockApp ->
    if (state."lock${lockApp.lock.id}"?.usage) {
      usage = usage + state."lock${lockApp.lock.id}".usage
    }
  }
  return usage
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

def userNotificationPageDescription() {
  def parts = []
  def msg = ''
  if (settings.phone) {
    parts << "SMS to ${phone}"
  }
  if (settings.notification) {
    parts << 'Push Notification'
  }
  if (settings.recipients) {
    parts << 'Sent to Address Book'
  }
  if (parts.size()) {
    msg += fancyString(parts)
  }
  parts = []

  if (settings.notifyAccess) {
    parts << 'on entry'
  }
  if (settings.notifyLock) {
    parts << 'on lock'
  }
  if (settings.notifyAccessStart) {
    parts << 'when granting access'
  }
  if (settings.notifyAccessEnd) {
    parts << 'when revoking access'
  }
  if (settings.notificationStartTime) {
    parts << "starting at ${settings.notificationStartTime}"
  }
  if (settings.notificationEndTime) {
    parts << "ending at ${settings.notificationEndTime}"
  }
  if (parts.size()) {
    msg += ': '
    msg += fancyString(parts)
  }
  if (muteUser) {
    msg = 'User Muted'
  }
  return msg
}

def deviceLabel(device) {
  return device.label ?: device.name
}

def schedulingHrefDescription() {
  def descriptionParts = []
  if (startDateTime() || endDateTime()) {
    descriptionParts << calendarHrefDescription()
  }
  if (days) {
    descriptionParts << "On ${fancyString(days)},"
  }
  if ((andOrTime != null) || (activeModes == null)) {
    if (startTime) {
      descriptionParts << "at ${humanReadableStartDate()}"
    }
    if (endTime) {
      descriptionParts << "until ${humanReadableEndDate()}"
    }
  }
  if (activeModes) {
    descriptionParts << "and when ${location.name} enters any of '${activeModes}' modes"
  }
  if (descriptionParts.size() <= 1) {
    // locks will be in the list no matter what. No rules are set if only locks are in the list
    return null
  }
  return descriptionParts.join(" ")
}

def isActive(lockId) {
  if (
      isUserEnabled() &&
      isValidCode() &&
      isNotBurned() &&
      isEnabled(lockId) &&
      userLockEnabled(lockId) &&
      isCorrectDay() &&
      isInCalendarRange() &&
      isCorrectMode() &&
      isInScheduledTime()
     ) {
    return true
  } else {
    return false
  }
}

def isActiveKeypad() {
  if (
      isUserEnabled() &&
      isValidCode() &&
      isNotBurned() &&
      isCorrectDay() &&
      isInCalendarRange() &&
      isCorrectMode() &&
      isInScheduledTime()
     ) {
    return true
  } else {
    return false
  }
}

def isUserEnabled() {
	if (userEnabled == null || userEnabled) {  //If true or unset, return true
		return true
	} else {
		return false
	}
}

def isValidCode() {
  if (userCode?.isNumber()) {
    return true
  } else {
    return false
  }
}

def isNotBurned() {
  if (burnAfterInt == null || burnAfterInt == 0) {
    return true // is not a burnable user
  } else {
    def totalUsage = getAllLocksUsage()
    if (totalUsage >= burnAfterInt) {
      // usage number is met!
      return false
    } else {
      // dont burn this user yet
      return true
    }
  }
}

def isEnabled(lockId) {
  if (state."lock${lockId}" == null) {
    return true
  } else if (state."lock${lockId}".enabled == null) {
    return true
  } else {
    return state."lock${lockId}".enabled
  }
}

def userLockEnabled(lockId) {
  def lockDisabled = settings."lockDisabled${lockId}"
  if (lockDisabled == null) {
    return true
  } else if (lockDisabled == true) {
    return false
  } else {
    return true
  }
}

def isCorrectDay() {
  def today = new Date().format("EEEE", timeZone())
  if (!days || days.contains(today)) {
    // if no days, assume every day
    return true
  }
  return false
}

def isInCalendarRange() {
  def dateStart = startDateTime()
  def dateEnd = endDateTime()
  def now = rightNow().getTime()
  if (dateStart && dateEnd) {
    // There's both an end time, and a start time.  Allow access between them.
    if (dateStart.getTime() < now && dateEnd.getTime() > now) {
      // It's in calendar times
      return true
    } else {
      // It's not in calendar times
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
  } else if (!dateEnd && dateStart) {
    // There's a start time, but no end time.  Allow access after start
    if (dateStart.getTime() < now) {
      // It's after the start time
      return true
    } else {
      // It's before the start time
      return false
    }
  } else {
    // there's no calendar
    return true
  }
}

def isCorrectMode() {
  if (activeModes) {
    // mode check is on
    if (activeModes.contains(location.mode)) {
      // we're in a right mode
      return true
    } else {
      // we're in the wrong mode
      return false
    }
  } else {
    // mode check is off
    return true
  }
}

def isInScheduledTime() {
  def now = rightNow()

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
    return true
  }
}

def startDateTime() {
  if (startDay && startMonth && startYear && calStartTime) {
    def time = new Date().parse(smartThingsDateFormat(), calStartTime).format("'T'HH:mm:ss.SSSZ", timeZone(calStartTime))
    return Date.parse("yyyy-MM-dd'T'HH:mm:ss.SSSZ", "${startYear}-${startMonth}-${startDay}${time}")
  } else {
    // Start Date Time not set
    return false
  }
}

def endDateTime() {
  if (endDay && endMonth && endYear && calEndTime) {
    def time = new Date().parse(smartThingsDateFormat(), calEndTime).format("'T'HH:mm:ss.SSSZ", timeZone(calEndTime))
    return Date.parse("yyyy-MM-dd'T'HH:mm:ss.SSSZ", "${endYear}-${endMonth}-${endDay}${time}")
  } else {
    // End Date Time not set
    return false
  }
}

def rightNow() {
  def now = new Date().format("yyyy-MM-dd'T'HH:mm:ss.SSSZ", timeZone())
  return Date.parse("yyyy-MM-dd'T'HH:mm:ss.SSSZ", now)
}

def getLockById(params) {
  return parent.locks.find{it.id == id}
}

def getLock(params) {
  def id = ''
  // Assign params to id.  Sometimes parameters are double nested.
  debugger('params: ' + params)
  debugger('last: ' + state.lastLock)
  if (params?.id) {
    id = params.id
  } else if (params?.params){
    id = params.params.id
  }
  def lockApp = parent.getLockAppById(id)
  if (!lockApp) {
    lockApp = parent.getLockAppById(state.lastLock)
  }

  if (lockApp) {
    state.lastLock = lockApp.lock.id
    return lockApp.lock
  } else {
    return false
  }
}

def userNotificationSettings() {
  def userSettings = false
  if (phone != null || notification || muteUser || recipients) {
    // user has it's own settings!
    userSettings = true
  }
  return userSettings
}

def sendUserMessage(msg) {
  if (userNotificationSettings()) {
    checkIfNotifyUser(msg)
  } else {
    checkIfNotifyGlobal(msg)
  }
}

def checkIfNotifyUser(msg) {
  if (notificationStartTime != null && notificationEndTime != null) {
    def start = timeToday(notificationStartTime)
    def stop = timeToday(notificationEndTime)
    def now = rightNow()
    if (start.before(now) && stop.after(now)){
      sendMessageViaUser(msg)
    }
  } else {
    sendMessageViaUser(msg)
  }
}

def checkIfNotifyGlobal(msg) {
  if (parent.notificationStartTime != null && parent.notificationEndTime != null) {
    def start = timeToday(parent.notificationStartTime)
    def stop = timeToday(parent.notificationEndTime)
    def now = rightNow()
    if (start.before(now) && stop.after(now)){
      sendMessageViaParent(msg)
    }
  } else {
    sendMessageViaParent(msg)
  }
}

def sendMessageViaParent(msg) {
  if (parent.recipients) {
    sendNotificationToContacts(msg, parent.recipients)
  } else {
    if (parent.notification) {
      sendPush(msg)
    } else {
      sendNotificationEvent(msg)
    }
    if (parent.phone) {
      if ( parent.phone.indexOf(";") > 1){
        def phones = parent.phone.split(";")
        for ( def i = 0; i < phones.size(); i++) {
          sendSms(phones[i], msg)
        }
      }
      else {
        sendSms(parent.phone, msg)
      }
    }
  }
}

def sendMessageViaUser(msg) {
  if (recipients) {
    sendNotificationToContacts(msg, recipients)
  } else {
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
}

def disableAndSetReason(lockID, reason) {
  state."lock${lockID}".enabled = false
  state."lock${lockID}".disabledReason = reason
}

def disableLock(lockID) {
  state."lock${lockID}".enabled = false
  state."lock${lockID}".disabledReason = 'Controller failed to set user code.'
}

def enableLock(lockID) {
  state."lock${lockID}".enabled = true
  state."lock${lockID}".disabledReason = null
}

def disabledReason() {
  state."lock${lockID}".disabledReason
}

def getLockUserInfo(lock) {
  def para = "\n${app.label}"
  if (settings."lockDisabled${lock.id}") {
    para += " DISABLED"
  }
  def usage = state."lock${lock.id}".usage
  para += " // Entries: ${usage}"
  if (!state."lock${lock.id}".enabled) {
    def reason = state."lock${lock.id}".disabledReason
    para += "\n ${reason}"
  }
  para
}

// User Ask Alexa

def userAlexaSettings() {
  if (alexaAccess || alexaLock || alexaAccessStart || alexaAccessEnd || alexaStartTime || alexaEndTime) {
    // user has it's own settings!
    return true
  }
  // user doesn't !
  return false
}

def askAlexaUser(msg) {
  if (userAlexaSettings()) {
    checkIfAlexaUser(msg)
  } else {
    checkIfAlexaGlobal(msg)
  }
}

def checkIfAlexaUser(message) {
  if (!muteUser) {
    if (alexaStartTime != null && alexaEndTime != null) {
      def start = timeToday(alexaStartTime)
      def stop = timeToday(alexaEndTime)
      def now = rightNow()
      if (start.before(now) && stop.after(now)){
        sendAskAlexaUser(message)
      }
    } else {
      sendAskAlexaUser(message)
    }
  }
}

def checkIfAlexaGlobal(message) {
  if (parent.alexaStartTime != null && parent.alexaEndTime != null) {
    def start = timeToday(parent.alexaStartTime)
    def stop = timeToday(parent.alexaEndTime)
    def now = rightNow()
    if (start.before(now) && stop.after(now)){
      sendAskAlexaUser(message)
    }
  } else {
    sendAskAlexaUser(message)
  }
}

def sendAskAlexaUser(message) {
  sendLocationEvent(name: 'AskAlexaMsgQueue',
                    value: 'LockManager/User',
                    isStateChange: true,
                    descriptionText: message,
                    unit: "User//${userName}")
}

def installedKeypad() {
  debugger("Keypad Installed with settings: ${settings}")
  initializeKeypad()
}

def updatedKeypad() {
  debugger("Keypad Updated with settings: ${settings}")
  initializeKeypad()
}

def initializeKeypad() {
  // reset listeners
  unsubscribe()
  atomicState.tries = 0
  atomicState.installComplete = true

  if (keypad) {
    subscribe(location, 'alarmSystemStatus', alarmStatusHandler)
    subscribe(keypad, 'codeEntered', codeEntryHandler)
  }
}

def isUniqueKeypad() {
  def unique = true
  if (!atomicState.installComplete) {
    // only look if we're not initialized yet.
    def keypadApps = parent.getKeypadApps()
    keypadApps.each { keypadApp ->
      if (keypadApp.keypad.id == keypad.id) {
        unique = false
      }
    }
  }
  return unique
}

def keypadLandingPage() {
  if (keypad) {
    def unique = isUniqueKeypad()
    if (unique){
      keypadMainPage()
    } else {
      keypadErrorPage()
    }
  } else {
    keypadSetupPage()
  }
}

def keypadSetupPage() {
  dynamicPage(name: 'keypadSetupPage', title: 'Setup Keypad', nextPage: 'keypadLandingPage', uninstall: true) {
    section('NOTE:') {
      def p =  'Locks with keypads ARE NOT KEYPADS in this context.\n\n'
          p += 'This integration works with stand-alone keypads only!'
      paragraph p
      paragraph 'For locks, use the Lock child-app.'
    }
    section('Keypad App Label') {
      label(title: "Name for App", required: true)
    }
    section('Choose keypad for this app') {
      input(name: 'keypad', title: 'Which keypad?', type: 'capability.lockCodes', multiple: false, required: true)
    }
  }
}

def keypadErrorPage() {
  dynamicPage(name: 'keypadErrorPage', title: 'Keypad Duplicate', uninstall: true, nextPage: 'keypadLandingPage') {
    section('Oops!') {
      paragraph 'The keypad that you selected is already installed. Please choose a different keypad or choose Remove'
    }
    section('Choose keypad for this app') {
      input(name: 'keypad', title: 'Which keypad?', type: 'capability.lockCodes', multiple: false, required: true)
    }
    section('Keypad App Label') {
      label(title: "Name for App", required: true)
    }
  }
}

def keypadMainPage() {
  dynamicPage(name: 'keypadMainPage',title: 'Keypad Settings (optional)', install: true, uninstall: true) {
    def actions = location.helloHome?.getPhrases()*.label
    actions?.sort()
    section('Routines') {
      paragraph 'settings here are for this keypad only. Global keypad settings, use parent app.'
      input(name: 'runDefaultAlarm', title: 'Act as SHM device?', type: 'bool', defaultValue: true, description: 'Toggle this off if actions should not effect SHM' )
      input(name: 'armRoutine', title: 'Arm/Away routine', type: 'enum', options: actions, required: false, multiple: true)
      input(name: 'disarmRoutine', title: 'Disarm routine', type: 'enum', options: actions, required: false, multiple: true)
      input(name: 'stayRoutine', title: 'Arm/Stay routine', type: 'enum', options: actions, required: false, multiple: true)
      input(name: 'nightRoutine', title: 'Arm/Night routine', type: 'enum', options: actions, required: false, multiple: true)
      input(name: 'armDelay', title: 'Arm Delay (in seconds)', type: 'number', required: false)
      input(name: 'notifyIncorrectPin', title: 'Notify you when incorrect code is used?', type: 'bool', required: false)
      input(name: 'attemptTollerance', title: 'How many times can incorrect code be used before notification?', type: 'number', defaultValue: 3, required: true)
    }
    section('Setup', hideable: true, hidden: true) {
      input(name: 'keypad', title: 'Keypad', type: 'capability.lockCodes', multiple: false, required: true)
      label title: 'Label', defaultValue: "Keypad: ${keypad.label}", required: false, description: 'recommended to start with Keypad:'
    }
  }
}

def alarmStatusHandler(event) {
  debugger("Keypad manager caught alarm status change: ${event.value}")
  if (runDefaultAlarm && event.value == 'off'){
    keypad?.setDisarmed()
  }
  else if (runDefaultAlarm && event.value == 'away'){
    keypad?.setArmedAway()
  }
  else if (runDefaultAlarm && event.value == 'stay') {
    keypad?.setArmedStay()
  }
}

def codeEntryHandler(evt) {
  //do stuff
  debugger("Caught code entry event! ${evt.value.value}")

  def codeEntered = evt.value as String
  def data = evt.data as Integer
  def currentarmMode = keypad.currentValue('armMode')
  def correctUser = parent.keypadMatchingUser(codeEntered)

  if (correctUser) {
    atomicState.tries = 0
    debugger('Correct PIN entered.')
    armCommand(data, correctUser, codeEntered)
  } else {
    debugger('Incorrect code!')
    atomicState.tries = atomicState.tries + 1
    if (atomicState.tries >= attemptTollerance) {
      keypad.sendInvalidKeycodeResponse()
      atomicState.tries = 0
    }
  }
}

def armCommand(value, correctUser, enteredCode) {
  def armMode
  def action
  keypad.acknowledgeArmRequest(value)
  switch (value) {
    case 0:
      armMode = 'off'
      action = 'disarmed'
      break
    case 1:
      armMode = 'stay'
      action = 'armed to \'Stay\''
      break
    case 2:
      armMode = 'night'
      action = 'armed to \'Night\''
      break
    case 3:
      armMode = 'away'
      action = 'armed to \'Away\''
      break
    default:
      log.error "${app.label}: Unexpected arm mode sent by keypad!"
      armMode = false
      break
  }

  // only delay on ARM actions
  def useDelay = 0
  if (armMode != 'off' && armMode != 'stay') {
    useDelay = armDelay
  }

  if (useDelay > 0) {
    keypad.setExitDelay(useDelay)
  }
  if (armMode) {
    // set values for delayed event
    atomicState.codeEntered = enteredCode
    atomicState.armMode = armMode

    runIn(useDelay, execRoutine)
  }

  def message = "${keypad.label} was ${action} by ${correctUser.label}"

  debugger(message)
  correctUser.sendUserMessage(message)
}

def execRoutine() {
  debugger('executing keypad actions')
  def armMode = atomicState.armMode
  def userApp = parent.keypadMatchingUser(atomicState.codeEntered)

  sendSHMEvent(armMode)

  // run hello home actions
  if (armMode == 'away') {
    if (armRoutine) {
      location.helloHome?.execute(armRoutine)
    }
    if (userApp.armRoutine) {
      location.helloHome?.execute(userApp.armRoutine)
    }
    if (parent.armRoutine) {
      location.helloHome?.execute(parent.armRoutine)
    }
  } else if (armMode == 'stay') {
    if (stayRoutine) {
      location.helloHome?.execute(stayRoutine)
    }
    if (userApp.stayRoutine) {
      location.helloHome?.execute(userApp.stayRoutine)
    }
    if (parent.stayRoutine) {
      location.helloHome?.execute(parent.stayRoutine)
    }
  } else if (armMode == 'off') {
    if (disarmRoutine) {
      location.helloHome?.execute(disarmRoutine)
    }
    if (userApp.disarmRoutine) {
      location.helloHome?.execute(userApp.disarmRoutine)
    }
    if (parent.disarmRoutine) {
      location.helloHome?.execute(parent.disarmRoutine)
    }
  } else if (armMode == 'night') {
    if (nightRoutine) {
      location.helloHome?.execute(nightRoutine)
    }
    if (userApp.nightRoutine) {
      location.helloHome?.execute(userApp.nightRoutine)
    }
    if (parent.nightRoutine) {
      location.helloHome?.execute(parent.nightRoutine)
    }
  }
}

def sendSHMEvent(armMode) {
  def event = [
        name:'alarmSystemStatus',
        value: armMode,
        displayed: true,
        description: "System Status is ${armMode}"
      ]
  debugger("Event: ${event}")
  if (runDefaultAlarm) {
    sendLocationEvent(event)
  }
}
