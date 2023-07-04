/**
 *  SmartThings SmartApp: Blink Home Monitoring
 *
 *  Author: redloro@gmail.com
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
 *  https://github.com/MattTW/BlinkMonitorProtocol
 *  https://github.com/terafin/SmartThingsPublic/tree/master/smartapps/terafin/blink.src
 *  https://community.smartthings.com/t/blink-wifi-camera-motion-sensor-on-kickstarter/3594/243
 */
definition(
  name: "Blink Home Monitoring",
  namespace: "redloro-smartthings",
  author: "redloro@gmail.com",
  description: "Blink SmartApp",
  category: "My Apps",
  iconUrl: "https://raw.githubusercontent.com/redloro/smartthings/master/images/blink.png",
  iconX2Url: "https://raw.githubusercontent.com/redloro/smartthings/master/images/blink.png",
  iconX3Url: "https://raw.githubusercontent.com/redloro/smartthings/master/images/blink.png",
  singleInstance: true
)

preferences {
  section("SmartThings Hub") {
    input "hostHub", "hub", title: "Select Hub", multiple: false, required: true
  }
  section("Blink Credentials") {
    input name: "blinkUser", type: "text", title: "Email", required: true
    input name: "blinkPassword", type: "password", title: "Password", required: true
  }
  section("Smart Home Monitor") {
    input "enableSHM", "bool", title: "Integrate with Smart Home Monitor", required: true, defaultValue: true
  }
}

def installed() {
  subscribeToEvents()

  // add blink device
  def deviceId = 'blink|network'+getNetworks(getAuthToken())[0].id
  if (!getChildDevice(deviceId)) {
    addChildDevice("redloro-smartthings", "Blink Monitor", deviceId, hostHub.id, ["name": "Blink Monitor", label: "Blink Monitor", completedSetup: true])
  }
}

def subscribeToEvents() {
  subscribe(location, "alarmSystemStatus", alarmHandler)
}

def updated() {
  unsubscribe()
  subscribeToEvents()
}

def alarmHandler(evt) {
  if (!settings.enableSHM) {
    return
  }

  if (state.alarmSystemStatus == evt.value) {
    return
  }

  state.alarmSystemStatus = evt.value
  if (evt.value == "stay") {
    setStatus("on")
  }
  if (evt.value == "away") {
    setStatus("on")
  }
  if (evt.value == "off") {
    setStatus("off")
  }
}

def getStatus() {
  def status = (getNetworks(getAuthToken())[0].armed == true) ? "on" : "off"
  childDevices*.monitor(status)
  return status
}

def setStatus(status) {
  def authToken = getAuthToken()
  def networkId = getNetworks(authToken)[0].id

  try {
    httpPostJson(
      [
        uri:     "https://prod.immedia-semi.com",
        path:    (status == "on") ? "/network/${networkId}/arm" : "/network/${networkId}/disarm",
        headers: [
          TOKEN_AUTH: authToken
        ]
      ]
    ) { objResponse ->
      if (objResponse.data != null) {
          log.debug "Successfully set armed status: ${status}"
          childDevices*.monitor(status)
      } else {
          log.error "Failed to set status: ${objResponse.data}"
      }
    }
  } catch (objException) {
    log.error "Exception setting mode: ${objException}"
  }
}

private getNetworks(authToken) {
  def networks = [:]
  try {
    httpGet(
      [
        uri:     "https://prod.immedia-semi.com",
        path:    "/networks",
        headers: [
          TOKEN_AUTH: authToken
        ]
      ]
    ) { objResponse ->
      if (objResponse.data != nil) {
        networks = objResponse.data.networks
        //log.debug "Retrieved networks: ${networks}"
      } else {
        log.error "Failed to retrieve devices: ${objResponse.data}"
      }
    }
  } catch (objException) {
    log.error "Exception retrieving devices: ${objException}"
  }
  return networks
}

private getAuthToken() {
  def authToken = ''
  try {
    httpPostJson(
      [
        uri:  "https://prod.immedia-semi.com",
        path: "/login",
        body: [
          email:    settings.blinkUser,
          password: settings.blinkPassword,
          client_specifier: "iPhone 9.2 | 2.2 | 222"
        ]
      ]
    ) { objResponse ->
      if (objResponse.data != null) {
        authToken = objResponse.data.authtoken.authtoken
        //log.debug "Retrieved authentication token: ${authToken}"
      } else {
        log.error "Failed to login: ${objResponse.data}"
      }
    }
  } catch (objException) {
    log.error "Exception during login: ${objException}"
  }
  return authToken
}