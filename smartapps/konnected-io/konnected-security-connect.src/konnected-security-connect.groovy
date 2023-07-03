/**
 *  Konnected Security (Connect)
 *
 *  Copyright 2017 konnected.io
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */
definition(
  name:        "Konnected Security (Connect)",
  namespace:   "konnected-io",
  author:      "konnected.io",
  description: "Convert your wired home alarm system into a SmartThings smart alarm",
  category:    "Safety & Security",
  iconUrl:     "https://raw.githubusercontent.com/konnected-io/docs/master/assets/images/KonnectedSecurity.png",
  iconX2Url:   "https://raw.githubusercontent.com/konnected-io/docs/master/assets/images/KonnectedSecurity@2x.png",
  iconX3Url:   "https://raw.githubusercontent.com/konnected-io/docs/master/assets/images/KonnectedSecurity@3x.png",
  singleInstance: true
)

mappings {
  path("/device/:mac/:id/:deviceState") { action: [ PUT: "childDeviceStateUpdate"] }
  path("/ping") { action: [ GET: "devicePing"] }
}

preferences {
  page(name: "pageWelcome",       install: false, uninstall: true, content: "pageWelcome",   nextPage: "pageDiscovery"     )
  page(name: "pageDiscovery",     install: false, uninstall: true, content: "pageDiscovery", nextPage: "pageConfiguration" )
  page(name: "pageConfiguration", install: true,  uninstall: true, content: "pageConfiguration")
}

def installed() {
  log.info "installed(): Installing Konnected Security SmartApp"
  initialize() 
  runEvery3Hours(discoverySearch)
}

def updated() {
  log.info "updated(): Updating Konnected Security SmartApp"
  initialize() 
}

def uninstalled() {
  log.info "uninstall(): Uninstalling Konnected Security SmartApp"
  revokeAccessToken()

  // Uninstall SmartApp, tell device that access is revoked and remove all the settings
  log.info "uninstall(): Removing Konnected Alarm device settings"
  def body = [
    token : "",
    apiUrl : "",
    sensors : [],
    actuators : []
  ]

  def selectedAlarmPanel = [] + getConfiguredDevices()
  selectedAlarmPanel.each { 
    sendHubCommand(new physicalgraph.device.HubAction([
      method: "PUT", 
      path: "/settings", 
      headers: [ HOST: getDeviceIpAndPort(it), "Content-Type": "application/json" ],
      body : groovy.json.JsonOutput.toJson(body)
    ], getDeviceIpAndPort(it) ))
  }
}

def initialize() {
  unsubscribe()
  unschedule()
  discoverySubscription(true)  
  childDeviceConfiguration()
  deviceUpdateSettings()
  state.pageConfigurationRefresh = 2
}

// Page : 1 : Welcome page - Manuals & links to devices
def pageWelcome() {
  dynamicPage(name: "pageWelcome", nextPage: "pageDiscovery") {
    def configuredAlarmPanels = [] + getConfiguredDevices()
    section("Tap Next to add or configure your Konnected device, or view the links below:") {
      href(
        name:        "pageWelcomeManual",
        title:       "Instructions & Documentation",
        description: "Tap to view the online documentation, or view on your computer at http://docs.konnected.io",
        required:    false,
        image:       "https://raw.githubusercontent.com/konnected-io/docs/master/assets/images/manual-icon.png",
        url:         "http://docs.konnected.io/security-alarm-system/"
      )
    }
    section("") {
      href(
        name:        "pageWelcomeDonate",
        title:       "Donate to Konnected!",
        description: "Konnected Alarm is an open source project. Your donations help fund future enhancements and products.",
        required:    false,
        image:       "https://raw.githubusercontent.com/konnected-io/docs/master/assets/images/donate-icon.png",
        url:         "http://docs.konnected.io/donate/"
      )
    }

    if (configuredAlarmPanels) {
      section("Device Status: You must be connected within your local area network to be able to view device status.") {
        configuredAlarmPanels.each {
          href(
            name:        "device_" + it.mac,
            title:       "konnected-security-" + it.mac[-6..-1],
            description: "Tap to view device status",
            required:    false,
            image:       "https://raw.githubusercontent.com/konnected-io/docs/master/assets/images/device-icon.png",
            url:         "http://" + getDeviceIpAndPort(it)
          )
        }
      }
    }
  }
}

//Page : 2 : Discovery page - search and select devices
def pageDiscovery() {
  //create accessToken
  if(!state.accessToken) { createAccessToken() }

  //This is a workaround to prevent page to refresh too fast.
  if(!state.pageConfigurationRefresh) { state.pageConfigurationRefresh = 2 }

  dynamicPage(name: "pageDiscovery", nextPage: "pageConfiguration", refreshInterval: state.pageConfigurationRefresh) {
    state.pageConfigurationRefresh =  state.pageConfigurationRefresh + 3
    discoverySubscription()
    discoverySearch()
    discoveryVerification()
    def alarmPanels = pageDiscoveryGetAlarmPanels()
    section("Please wait while we discover your device") {
      input(
        name: "selectedAlarmPanels",
        type: "enum",
        title: "Select devices (${alarmPanels.size() ?: 0} found)",
        required: true,
        multiple: true,
        options: alarmPanels,
        defaultValue: settings.selectedAlarmPanels,
        submitOnChange: true
      )
    }
  }
}

Map pageDiscoveryGetAlarmPanels() {
  def alarmPanels = [:]
  def verifiedAlarmPanels = getDevices().findAll{ it.value.verified == true }
  verifiedAlarmPanels.each { alarmPanels["${it.value.mac}"] = it.value.name ?: "konnected-security-${it.value.mac[-6..-1]}" }
  return alarmPanels
}

//Page : 3 : Configure sensors and alarms connected to the panel
def pageConfiguration() {
  // Get all selected devices
  def configuredAlarmPanels = [] + getConfiguredDevices()

  dynamicPage(name: "pageConfiguration") {
    configuredAlarmPanels.each { alarmPanel ->
      section(hideable: true, "konnected-security-${alarmPanel.mac[-6..-1]}") {
        for ( i in [1, 2, 5, 6, 7, 8, 9]) {
          def deviceTypeDefaultValue = (settings."deviceType_${alarmPanel.mac}_${i}") ? settings."deviceType_${alarmPanel.mac}_${i}" : ""
          def deviceLabelDefaultValue = (settings."deviceLabel_${alarmPanel.mac}_${i}") ? settings."deviceLabel_${alarmPanel.mac}_${i}" : ""

          input(
            name: "deviceType_${alarmPanel.mac}_${i}",
            type: "enum",
            title: "Pin ${pinLabel(i)}",
            required: false,
            multiple: false,
            options: pageConfigurationGetDeviceType(i),
            defaultValue: deviceTypeDefaultValue,
            submitOnChange: true
          )

          if (settings."deviceType_${alarmPanel.mac}_${i}") {
            input(
              name: "deviceLabel_${alarmPanel.mac}_${i}",
              type: "text",
              title: "Pin ${pinLabel(i)} Name",
              description: "Name the device connected to ${pinLabel(i)}",
              required: (settings."deviceType_${alarmPanel.mac}_${i}" != null),
              defaultValue: deviceLabelDefaultValue
            )
          }
        }
      }
    }
  }
}

private Map pageConfigurationGetDeviceType(Integer i) {
  def deviceTypes = [:]
  def sensorPins = [1,2,5,6,7,9]
  def actuatorPins = [1,2,5,6,7,8]

  if (sensorPins.contains(i)) {
    deviceTypes << [
      "Konnected Contact Sensor" : "Open/Close Sensor",
      "Konnected Motion Sensor"  : "Motion Sensor",
      "Konnected Smoke Sensor"   : "Smoke Detector",
      "Konnected Panic Button"   : "Panic Button"
    ]
  }

  if (actuatorPins.contains(i)) {
    deviceTypes << ["Konnected Siren/Strobe"   : "Siren/Strobe"]
  }

  return deviceTypes
}

// Retrieve selected device
def getConfiguredDevices() {
  getDevices().findAll {
    settings.selectedAlarmPanels?.contains(it.value.mac)
  }.collect {
    it.value
  }
}

// Retrieve devices saved in state
def getDevices() {
  if (!state.devices) { state.devices = [:] }
  return state.devices
}

def getDeviceIpAndPort(device) {
  "${convertHexToIP(device.networkAddress)}:${convertHexToInt(device.deviceAddress)}"
}

// Device Discovery : Device Type
def discoveryDeviceType() {
  return "urn:schemas-konnected-io:device:Security:1"
}

// Device Discovery : Send M-Search to multicast
def discoverySearch() {
  sendHubCommand(new physicalgraph.device.HubAction("lan discovery ${discoveryDeviceType()}", physicalgraph.device.Protocol.LAN))
}

// Device Discovery : Subscribe to SSDP events
def discoverySubscription(force=false) {
  if (force) {
    unsubscribe()
    state.subscribe = false
  }
  if(!state.subscribe) {
    subscribe(location, "ssdpTerm.${discoveryDeviceType()}", discoverySearchHandler, [filterEvents:false])
    state.subscribe = true
  }
}

// Device Discovery : Handle search response
def discoverySearchHandler(evt) {
  def event = parseLanMessage(evt.description)
  event << ["hub":evt?.hubId]
  String ssdpUSN = event.ssdpUSN.toString()
  def devices = getDevices()
  if (devices[ssdpUSN]) {
    def d = devices[ssdpUSN]
    d.networkAddress = event.networkAddress
    d.deviceAddress = event.deviceAddress
    log.debug "Refreshed attributes of device $d"
  } else {
    devices[ssdpUSN] = event
    log.debug "Discovered new device $event"
    discoveryVerify(event)
  }
}

//Device Discovery : Verify search response by retrieving XML
def discoveryVerification() {
  getDevices().findAll { it?.value?.verified != true }.each {
    discoveryVerify(it.value)
  }
}

// Device Discovery : Verify a Device
def discoveryVerify(Map device) {
  log.debug "Verifying communication with device $device"
  String host = getDeviceIpAndPort(device)
  sendHubCommand(
    new physicalgraph.device.HubAction(
      """GET ${device.ssdpPath} HTTP/1.1\r\nHOST: ${host}\r\n\r\n""",
      physicalgraph.device.Protocol.LAN,
      host,
      [callback: discoveryVerificationHandler]
    )
  )
}

//Device Discovery : Handle verification response
def discoveryVerificationHandler(physicalgraph.device.HubResponse hubResponse) {
  def body = hubResponse.xml
  def devices = getDevices()
  def device = devices.find { it?.key?.contains(body?.device?.UDN?.text()) }
  if (device) {
    log.debug "Verification Success: $body"
    device.value << [
      name: body?.device?.roomName?.text(),
      model:body?.device?.modelName?.text(),
      serialNumber:body?.device?.serialNum?.text(),
      verified: true
    ]
  }
}

//Child Devices : create/delete child devices from SmartThings app selection
def childDeviceConfiguration() {
  settings.each { name , value ->
    def nameValue = name.split("\\_")
    if (nameValue[0] == "deviceType") {
      def selectedAlarmPanel = getConfiguredDevices().find { it.mac == nameValue[1] }
      def deviceDNI = [ selectedAlarmPanel.mac, "${nameValue[2]}"].join('|')
      def deviceLabel = settings."deviceLabel_${nameValue[1]}_${nameValue[2]}"
      def deviceType = value
      def deviceChild = getChildDevice(deviceDNI)
      if (!deviceChild) {
        if (deviceType != "") {
          addChildDevice("konnected-io", deviceType, deviceDNI, selectedAlarmPanel.hub, [ "label": deviceLabel ? deviceLabel : deviceType , "completedSetup": true ])
        }
      } else {
        //Change name if it's set here
        if (deviceChild.label != deviceLabel)
          deviceChild.label = deviceLabel
        //Change Type, you will lose the history of events. delete and add back the child
        if (deviceChild.name != deviceType) {
          deleteChildDevice(deviceDNI)
          if (deviceType != "") {
            addChildDevice("konnected-io", deviceType, deviceDNI, selectedAlarmPanel.hub, [ "label": deviceLabel ? deviceLabel : deviceType , "completedSetup": true ])
          }
        }
      }
    }
  }
  def deleteChildDevices = getAllChildDevices().findAll { settings."deviceType_${it.deviceNetworkId.split("\\|")[0]}_${it.deviceNetworkId.split("\\|")[1]}" == "" }
  deleteChildDevices.each { deleteChildDevice(it.deviceNetworkId) }
}
//Child Devices : update state of child device sent from nodemcu
def childDeviceStateUpdate() {
  def device = getChildDevice(params.mac.toUpperCase() + "|" + params.id)
  if (device) device.setStatus(params.deviceState)
}

//Device: Ping from device
def devicePing() {
  return ""
}

//Device : update NodeMCU with token, url, sensors, actuators from SmartThings
def deviceUpdateSettings() {
  if(!state.accessToken) { createAccessToken() }
  def sensors = [:]
  def actuators = [:]
  def selectedAlarmPanel = [] + getConfiguredDevices()

  //initialize map for sensors/actuators
  selectedAlarmPanel.each {
    sensors[it.mac] = []
    actuators[it.mac] = []
  }
  //compile pins into respective sensors/actuators by mac
  getAllChildDevices().each {
    def mac = it.deviceNetworkId.split("\\|")[0]
    def pin = it.deviceNetworkId.split("\\|")[1]
    if (it.name != "Konnected Siren/Strobe") {
      sensors[mac] = sensors[mac] + [ pin : pin ]
    } else {
      actuators[mac] = actuators[mac] + [ pin : pin ]
    }
  }

  // send information to each devices
  selectedAlarmPanel.each {
    def body = [
      token : state.accessToken,
      apiUrl : apiServerUrl + "/api/smartapps/installations/" + app.id,
      sensors : sensors[it.mac],
      actuators : actuators[it.mac]
    ]

    log.debug "Updating settings on device $it.mac at " + getDeviceIpAndPort(it)
    sendHubCommand(new physicalgraph.device.HubAction([
      method: "PUT",
      path: "/settings",
      headers: [ HOST: getDeviceIpAndPort(it), "Content-Type": "application/json" ],
      body : groovy.json.JsonOutput.toJson(body)
    ], getDeviceIpAndPort(it) ))
  }
}

// Device: update NodeMCU with state of device changed from SmartThings
def deviceUpdateDeviceState(deviceDNI, deviceState) {
  def deviceId = deviceDNI.split("\\|")[1]
  def deviceMac = deviceDNI.split("\\|")[0]
  def body = [ pin : deviceId, state : deviceState ]
  def device = getConfiguredDevices().find { it.mac == deviceMac }

  if (device) {
    log.debug "Updating device $deviceMac pin $deviceId to $deviceState at " + getDeviceIpAndPort(device)
    sendHubCommand(new physicalgraph.device.HubAction([
      method: "PUT",
      path: "/device",
      headers: [ HOST: getDeviceIpAndPort(device), "Content-Type": "application/json" ],
      body : groovy.json.JsonOutput.toJson(body)
    ], getDeviceIpAndPort(device)))
  }
}

private String pinLabel(Integer i) {
  if (i == 9) {
    return "RX"
  } else {
    return "D$i"
  }
}

private Integer convertHexToInt(hex) { Integer.parseInt(hex,16) }
private String convertHexToIP(hex) { [convertHexToInt(hex[0..1]),convertHexToInt(hex[2..3]),convertHexToInt(hex[4..5]),convertHexToInt(hex[6..7])].join(".") }
