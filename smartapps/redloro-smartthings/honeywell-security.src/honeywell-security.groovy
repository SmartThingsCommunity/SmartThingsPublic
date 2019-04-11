/**
 *  SmartThings SmartApp: Honeywell Security
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
 */
import groovy.json.JsonSlurper

definition(
  name: "Honeywell Security",
  namespace: "redloro-smartthings",
  author: "redloro@gmail.com",
  description: "Honeywell Security SmartApp",
  category: "My Apps",
  iconUrl: "https://raw.githubusercontent.com/redloro/smartthings/master/images/honeywell-security.png",
  iconX2Url: "https://raw.githubusercontent.com/redloro/smartthings/master/images/honeywell-security.png",
  iconX3Url: "https://raw.githubusercontent.com/redloro/smartthings/master/images/honeywell-security.png",
  singleInstance: true
)

preferences {
	page(name: "page1")
}

def page1() {
  dynamicPage(name: "page1", install: true, uninstall: true) {
    section("SmartThings Hub") {
      input "hostHub", "hub", title: "Select Hub", multiple: false, required: true
    }
    section("SmartThings Node Proxy") {
      input "proxyAddress", "text", title: "Proxy Address", description: "(ie. 192.168.1.10)", required: true
      input "proxyPort", "text", title: "Proxy Port", description: "(ie. 8080)", required: true, defaultValue: "8080"
      input "authCode", "password", title: "Auth Code", description: "", required: true, defaultValue: "secret-key"
    }
    section("Honeywell Panel") {
      input name: "pluginType", type: "enum", title: "Plugin Type", required: true, submitOnChange: true, options: ["envisalink", "ad2usb"]
      input "securityCode", "password", title: "Security Code", description: "User code to arm/disarm the security panel", required: false
      input "enableDiscovery", "bool", title: "Discover Zones (WARNING: all existing zones will be removed)", required: false, defaultValue: false
    }

    if (pluginType == "envisalink") {
      section("Envisalink Vista TPI") {
        input "evlAddress", "text", title: "Host Address", description: "(ie. 192.168.1.11)", required: false
        input "evlPort", "text", title: "Host Port", description: "(ie. 4025)", required: false
        input "evlPassword", "password", title: "Password", description: "", required: false
      }
    }

    section("Smart Home Monitor") {
      input "enableSHM", "bool", title: "Integrate with Smart Home Monitor", required: true, defaultValue: true
    }
  }
}

def installed() {
  subscribeToEvents()
}

def subscribeToEvents() {
  subscribe(location, null, lanResponseHandler, [filterEvents:false])
  subscribe(location, "alarmSystemStatus", alarmHandler)
}

def uninstalled() {
  removeChildDevices()
}

def updated() {
  if (settings.enableDiscovery) {
    //remove child devices as we will reload
    removeChildDevices()
  }

  //subscribe to callback/notifications from STNP
  sendCommand('/subscribe/'+getNotifyAddress())

  //save envisalink settings to STNP config
  if (settings.pluginType == "envisalink" && settings.evlAddress && settings.evlPort && settings.evlPassword && settings.securityCode) {
    sendCommandPlugin('/config/'+settings.evlAddress+":"+settings.evlPort+":"+settings.evlPassword+":"+settings.securityCode)
  }

  //save ad2usb settings to STNP config
  if (settings.pluginType == "ad2usb" && settings.securityCode) {
    sendCommandPlugin('/config/'+settings.securityCode)
  }

  if (settings.enableDiscovery) {
    //delay discovery for 5 seconds
    runIn(5, discoverChildDevices)
    settings.enableDiscovery = false
  }
}

def lanResponseHandler(evt) {
  def map = stringToMap(evt.stringValue)

  //verify that this message is from STNP IP:Port
  //IP and Port are only set on HTTP GET response and we need the MAC
  if (map.ip == convertIPtoHex(settings.proxyAddress) &&
    map.port == convertPortToHex(settings.proxyPort)) {
      if (map.mac) {
        state.proxyMac = map.mac
      }
  }

  //verify that this message is from STNP MAC
  //MAC is set on both HTTP GET response and NOTIFY
  if (map.mac != state.proxyMac) {
    return
  }

  def headers = getHttpHeaders(map.headers);
  def body = getHttpBody(map.body);
  //log.trace "SmartThings Node Proxy: ${evt.stringValue}"
  //log.trace "Headers: ${headers}"
  //log.trace "Body: ${body}"

  //verify that this message is for this plugin
  if (headers.'stnp-plugin' != settings.pluginType) {
    return
  }

  //log.trace "Honeywell Security event: ${evt.stringValue}"
  processEvent(body)
}

private sendCommandPlugin(path) {
  sendCommand("/plugins/"+settings.pluginType+path)
}

private sendCommand(path) {
  //log.trace "Honeywell Security send command: ${path}"

  if (settings.proxyAddress.length() == 0 ||
    settings.proxyPort.length() == 0) {
    log.error "SmartThings Node Proxy configuration not set!"
    return
  }

  def host = getProxyAddress()
  def headers = [:]
  headers.put("HOST", host)
  headers.put("Content-Type", "application/json")
  headers.put("stnp-auth", settings.authCode)

  def hubAction = new physicalgraph.device.HubAction(
      method: "GET",
      path: path,
      headers: headers
  )
  sendHubCommand(hubAction)
}

private processEvent(evt) {
  if (evt.type == "discover") {
    addChildDevices(evt.partitions, evt.zones)
  }
  if (evt.type == "zone") {
    updateZoneDevices(evt.zone, evt.state)
  }
  if (evt.type == "partition") {
    updatePartitions(evt.partition, evt.state, evt.alpha)
    updateAlarmSystemStatus(evt.state)
  }
}

private addChildDevices(partitions, zones) {
  partitions.each {
    def deviceId = 'honeywell|partition'+it.partition
    if (!getChildDevice(deviceId)) {
      addChildDevice("redloro-smartthings", "Honeywell Partition", deviceId, hostHub.id, ["name": "Honeywell Security", label: "Honeywell Security", completedSetup: true])
      //log.debug "Added partition device: ${deviceId}"
    }
  }

  zones.each {
    def deviceId = 'honeywell|zone'+it.zone
    if (!getChildDevice(deviceId)) {
      it.type = it.type.capitalize()
      addChildDevice("redloro-smartthings", "Honeywell Zone "+it.type, deviceId, hostHub.id, ["name": it.name, label: it.name, completedSetup: true])
      //log.debug "Added zone device: ${deviceId}"
    }
  }
}

private removeChildDevices() {
  getAllChildDevices().each { deleteChildDevice(it.deviceNetworkId) }
}

def discoverChildDevices() {
  sendCommandPlugin('/discover')
}

private updateZoneDevices(zonenum,zonestatus) {
  //log.debug "updateZoneDevices: ${zonenum} is ${zonestatus}"
  def zonedevice = getChildDevice("honeywell|zone${zonenum}")
  if (zonedevice) {
    zonedevice.zone("${zonestatus}")
  }
}

private updatePartitions(partitionnum, partitionstatus, panelalpha) {
  //log.debug "updatePartitions: ${partitionnum} is ${partitionstatus}"
  def partitionDevice = getChildDevice("honeywell|partition${partitionnum}")
  if (partitionDevice) {
    partitionDevice.partition("${partitionstatus}", "${panelalpha}")
  }
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
    sendCommandPlugin('/armStay')
  }
  if (evt.value == "away") {
    sendCommandPlugin('/armAway')
  }
  if (evt.value == "off") {
    sendCommandPlugin('/disarm')
  }
}

private updateAlarmSystemStatus(partitionstatus) {
  if (!settings.enableSHM || partitionstatus == "arming") {
    return
  }

  def lastAlarmSystemStatus = state.alarmSystemStatus
  if (partitionstatus == "armedstay" || partitionstatus == "armedinstant") {
    state.alarmSystemStatus = "stay"
  }
  if (partitionstatus == "armedaway" || partitionstatus == "armedmax") {
    state.alarmSystemStatus = "away"
  }
  if (partitionstatus == "ready") {
    state.alarmSystemStatus = "off"
  }

  if (lastAlarmSystemStatus != state.alarmSystemStatus) {
    sendLocationEvent(name: "alarmSystemStatus", value: state.alarmSystemStatus)
  }
}

private getHttpHeaders(headers) {
  def obj = [:]
  new String(headers.decodeBase64()).split("\r\n").each {param ->
    def nameAndValue = param.split(":")
    obj[nameAndValue[0]] = (nameAndValue.length == 1) ? "" : nameAndValue[1].trim()
  }
  return obj
}

private getHttpBody(body) {
  def obj = null;
  if (body) {
    def slurper = new JsonSlurper()
    obj = slurper.parseText(new String(body.decodeBase64()))
  }
  return obj
}

private getProxyAddress() {
  return settings.proxyAddress + ":" + settings.proxyPort
}

private getNotifyAddress() {
  return settings.hostHub.localIP + ":" + settings.hostHub.localSrvPortTCP
}

private String convertIPtoHex(ipAddress) {
  return ipAddress.tokenize( '.' ).collect {  String.format( '%02x', it.toInteger() ) }.join().toUpperCase()
}

private String convertPortToHex(port) {
  return port.toString().format( '%04x', port.toInteger() ).toUpperCase()
}