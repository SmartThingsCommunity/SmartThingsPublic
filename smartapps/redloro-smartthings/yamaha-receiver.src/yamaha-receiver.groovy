/**
 *  SmartThings SmartApp: Yamaha Network Receiver
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
 *  https://github.com/PSeitz/yamaha-nodejs
 *  http://<RECEIVER_IP_ADDRESS>/YamahaRemoteControl/desc.xml
 */
import groovy.util.XmlSlurper

definition(
  name: "Yamaha Receiver",
  namespace: "redloro-smartthings",
  author: "redloro@gmail.com",
  description: "Yamaha SmartApp",
  category: "My Apps",
  iconUrl: "https://raw.githubusercontent.com/redloro/smartthings/master/images/yamaha-receiver.png",
  iconX2Url: "https://raw.githubusercontent.com/redloro/smartthings/master/images/yamaha-receiver.png",
  iconX3Url: "https://raw.githubusercontent.com/redloro/smartthings/master/images/yamaha-receiver.png"
)

preferences {
  section("SmartThings Hub") {
    input "hostHub", "hub", title: "Select Hub", multiple: false, required: true
  }
  section("Yamaha Receiver") {
    input name: "receiverName", type: "text", title: "Name", required: true, defaultValue: "Yamaha"
    input name: "receiverIp", type: "text", title: "IP", required: true
    input name: "receiverZones", type: "enum", title: "Zones", required: true, multiple: true, options: ["Main_Zone","Zone_B","Zone_2","Zone_3","Zone_4"]
  }
}

def installed() {
  subscribeToEvents()
}

def subscribeToEvents() {
  subscribe(location, null, lanResponseHandler, [filterEvents:false])
}

def updated() {
  addChildDevices()
}

def uninstalled() {
  removeChildDevices()
}

def lanResponseHandler(evt) {
  def map = stringToMap(evt.stringValue)

  //verify that this message is from Yamaha Receiver IP
  if (!map.ip || map.ip != convertIPtoHex(settings.receiverIp)) {
    return
  }

  def headers = getHttpHeaders(map.headers);
  def body = getHttpBody(map.body);
  //log.trace "Headers: ${headers}"
  //log.trace "Body: ${body}"

  updateZoneDevices(body.children()[0])
}

private updateZoneDevices(evt) {
  //log.debug "updateZoneDevices: ${evt.toString()}"
  if (evt.name() == "System") {
    //log.debug "Update all zones"
    childDevices*.zone(evt)
    return
  }

  def zonedevice = getChildDevice(getDeviceId(evt.name()))
  if (zonedevice) {
    zonedevice.zone(evt)
  }

  //check for Zone_B
  zonedevice = getChildDevice(getDeviceId("Zone_B"))
  if (zonedevice && evt.name() == "Main_Zone") {
    zonedevice.zone(evt)
  }
}

private addChildDevices() {
  // add yamaha device
  settings.receiverZones.each {
    def deviceId = getDeviceId(it)
    if (!getChildDevice(deviceId)) {
      addChildDevice("redloro-smartthings", "Yamaha Zone", deviceId, hostHub.id, ["name": it, label: "${settings.receiverName}: ${it}", completedSetup: true])
      log.debug "Added Yamaha zone: ${deviceId}"
    }
  }

  childDevices*.refresh()
}

private removeChildDevices() {
  getAllChildDevices().each { deleteChildDevice(it.deviceNetworkId) }
}

private sendCommand(body) {
  //log.debug "Yamaha Network Receiver send command: ${body}"

  def hubAction = new physicalgraph.device.HubAction(
      headers: [HOST: getReceiverAddress()],
      method: "POST",
      path: "/YamahaRemoteControl/ctrl",
      body: body
  )
  sendHubCommand(hubAction)
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
    obj = new XmlSlurper().parseText(new String(body.decodeBase64()))
  }
  return obj
}

private getDeviceId(zone) {
  return "yamaha|${settings.receiverIp}|${zone}"
}

private getReceiverAddress() {
  return settings.receiverIp + ":80"
}

private String convertIPtoHex(ipAddress) {
  return ipAddress.tokenize( '.' ).collect {  String.format( '%02x', it.toInteger() ) }.join().toUpperCase()
}

private String convertPortToHex(port) {
  return port.toString().format( '%04x', port.toInteger() ).toUpperCase()
}