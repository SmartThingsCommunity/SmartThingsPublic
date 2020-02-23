/**
 *  Copyright 2015 Nathan Jacobson <natecj@gmail.com>
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

definition(
  name: "SleepIQ Manager",
  namespace: "natecj",
  author: "Nathan Jacobson",
  description: "Manage sleepers across multiple beds through your SleepIQ account.",
  category: "Convenience",
  iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
  iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png"
)

preferences {
  page name: "rootPage"
  page name: "findDevicePage"
  page name: "selectDevicePage"
  page name: "createDevicePage"
}

def rootPage() {
  log.trace "rootPage()"
  
  def devices = getChildDevices()
  
  dynamicPage(name: "rootPage", install: true, uninstall: true) {
    section("Settings") {
      input("login", "text", title: "Username", description: "Your SleepIQ username")
      input("password", "password", title: "Password", description: "Your SleepIQ password")
      input("interval", "number", title: "Refresh Interval", description: "How many minutes between refresh", defaultValue: 15)
    }
    section("Devices") {
      if (devices.size() > 0) {
        devices.each { device ->
          paragraph title: device.label, "${device.currentBedId} / ${device.currentMode}"
        }
      }
      href "findDevicePage", title: "Create New Device", description: null
    }
    section {
      label title: "Assign a name", required: false
      mode title: "Set for specific mode(s)", required: false
    }
  }
}

def statusText(status) {
  "Current Status: " + (status ? "Present" : "Not Present")
}

def findDevicePage() {
  log.trace "findDevicePage()"

  def responseData = getBedData()
  log.debug "Response Data: $responseData"
  
  dynamicPage(name: "findDevicePage") {
    if (responseData.beds.size() > 0) {
      responseData.beds.each { bed ->
        section("Bed: ${bed.bedId}") {
          def leftStatus = bed.leftSide.isInBed
          def rightStatus = bed.rightSide.isInBed
          def bothStatus = leftStatus && rightStatus
          def eitherStatus = leftStatus || rightStatus
          href "selectDevicePage", title: "Both Sides", description: statusText(bothStatus), params: [bedId: bed.bedId, mode: "Both", status: bothStatus]
          href "selectDevicePage", title: "Either Side", description: statusText(eitherStatus), params: [bedId: bed.bedId, mode: "Either", status: eitherStatus]
          href "selectDevicePage", title: "Left Side", description: statusText(leftStatus), params: [bedId: bed.bedId, mode: "Left", status: leftStatus]
          href "selectDevicePage", title: "Right Side", description: statusText(rightStatus), params: [bedId: bed.bedId, mode: "Right", status: rightStatus]
        }
      }
    } else {
      section {
        paragraph "No Beds Found"
      }
    }
  }
}

def selectDevicePage(params) {
  log.trace "selectDevicePage()"
  
  settings.newDeviceName = null
  
  dynamicPage(name: "selectDevicePage") {
    section {
      paragraph "Bed ID: ${params.bedId}"
      paragraph "Mode: ${params.mode}"
      paragraph "Status: ${params.present ? 'Present' : 'Not Present'}"
      input "newDeviceName", "text", title: "Device Name", description: "What do you want to call this presence sensor?", defaultValue: ""
    }
    section {
      href "createDevicePage", title: "Create Device", description: null, params: [bedId: params.bedId, mode: params.mode, status: params.status]
    }
  }
}

def createDevicePage(params) {
  log.trace "createDevicePage()"

  def deviceId = "sleepiq.${params.bedId}.${params.mode}"
  def device = addChildDevice("natecj", "SleepIQ Presence Sensor", deviceId, null, [label: settings.newDeviceName])
  device.setStatus(params.status)
  device.setBedId(params.bedId)
  device.setMode(params.mode)
  settings.newDeviceName = null
  
  dynamicPage(name: "selectDevicePage") {
    section {
      paragraph "Name: ${device.name}"
      paragraph "Label: ${device.label}"
      paragraph "Bed ID: ${device.curentBedId}"
      paragraph "Mode: ${device.currentMode}"
      paragraph "Presence: ${device.currentPresnce}"
    }
    section {
      href "rootPage", title: "Back to Device List", description: null
    }
  }
}


def installed() {
  log.trace "installed()"
  initialize()
}

def updated() {
  log.trace "updated()"
  unsubscribe()
  unschedule()
  initialize()
}

def initialize() {
  log.trace "initialize()"
  refreshChildDevices()
  schedule("* /${settings.interval} * * * ?", "refreshChildDevices")
}

def refreshChildDevices() {
  log.trace "refreshChildDevices()"
  getBedData()
}

def getBedData() {
  log.trace "getBedData()"
    
  // Make request and wait for completion
  state.requestData = null
  doStatus()
  while(state.requestData == null) { sleep(1000) }
  def requestData = state.requestData
  state.requestData = null
  
  // Process data
  processBedData(requestData)
  
  // Return data
  requestData
}

def processBedData(responseData) {
  if (!responseData || responseData.size() == 0) {
    return
  }
  for(def device : getChildDevices()) {
    for(def bed : responseData.beds) {
      if (device.currentBedId == bed.bedId) {
        def statusMap = [:]
        statusMap["Both"] = bed.leftSide.isInBed && bed.rightSide.isInBed
        statusMap["Either"] = bed.leftSide.isInBed || bed.rightSide.isInBed
        statusMap["Left"] = bed.leftSide.isInBed
        statusMap["Right"] = bed.rightSide.isInBed
        if (statusMap.containsKey(device.currentMode)) {
          log.debug "Setting ${device.label} (${device.currentMode}) to ${statusMap[device.currentMode] ? "Present" : "Not Present"}"
          device.setStatus(statusMap[device.currentMode])
        }
        break
      }
    }
  }
}





private def ApiHost() { "api.sleepiq.sleepnumber.com" }

private def ApiUriBase() { "https://api.sleepiq.sleepnumber.com" }

private def ApiUserAgent() { "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/28.0.1500.95 Safari/537.36" }

private def doStatus(alreadyLoggedIn = false) {
  log.trace "doStatus()"

  // Login if there isnt an active session
  if (!state.session || !state.session?.key) {
    if (alreadyLoggedIn) {
      log.error "doStatus() Already attempted login, giving up for now"
    } else {
      doLogin()
    }
    return
  }

  // Make the request
  try {
    def statusParams = [
      uri: ApiUriBase() + '/rest/bed/familyStatus?_k=' + state.session?.key,
      headers: [
        'Content-Type': 'application/json;charset=UTF-8',
        'Host': ApiHost(),
        'User-Agent': ApiUserAgent(),
        'Cookie': state.session?.cookies,
        'DNT': '1',
      ],
    ]
    httpGet(statusParams) { response -> 
      if (response.status == 200) {
        log.trace "doStatus() Success -  Request was successful: ($response.status) $response.data"
        state.requestData = response.data
      } else {
        log.trace "doStatus() Failure - Request was unsuccessful: ($response.status) $response.data"
        state.session = null
        state.requestData = [:]
      }
    }
  } catch(Exception e) {
    if (alreadyLoggedIn) {
      log.error "doStatus() Error ($e)"
    } else {
      log.trace "doStatus() Error ($e)"
      doLogin()    
    }
  }
}

private def doLogin() {
  log.trace "doLogin()"
  state.session = null
  state.requestData = [:]
  try {
    def loginParams = [
      uri: ApiUriBase() + '/rest/login',
      headers: [
        'Content-Type': 'application/json;charset=UTF-8',
        'Host': ApiHost(),
        'User-Agent': ApiUserAgent(),
        'DNT': '1',
      ],
      body: '{"login":"' + settings.login + '","password":"' + settings.password + '"}='
    ]
    httpPut(loginParams) { response ->
      if (response.status == 200) {
        log.trace "doLogin() Success - Request was successful: ($response.status) $response.data"
        state.session = [:]
        state.session.key = response.data.key
        state.session.cookies = ''
        response.getHeaders('Set-Cookie').each {
          state.session.cookies = state.session.cookies + it.value.split(';')[0] + ';'
        }
        doStatus(true)
      } else {
        log.trace "doLogin() Failure - Request was unsuccessful: ($response.status) $response.data"
        state.session = null
        state.requestData = [:]
      }
    }
  } catch(Exception e) {
    log.error "doLogin() Error ($e)"
    state.session = null
    state.requestData = [:]
  }
}