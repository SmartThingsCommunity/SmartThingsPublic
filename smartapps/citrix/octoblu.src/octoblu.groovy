/**
The MIT License (MIT)

Copyright (c) 2015 Octoblu

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/

import org.apache.commons.codec.binary.Base64
import java.text.DecimalFormat
import groovy.transform.Field

@Field final USE_DEBUG = false
@Field final selectedCapabilities = [ "actuator", "sensor" ]

private getVendorName()       { "Octoblu" }
private getVendorIcon()       { "http://i.imgur.com/BjTfDYk.png" }
private apiUrl()              { appSettings.apiUrl ?: "https://meshblu.octoblu.com/" }
private getVendorAuthPath()   { appSettings.vendorAuthPath ?: "https://oauth.octoblu.com/authorize" }
private getVendorTokenPath()  { appSettings.vendorTokenPath ?: "https://oauth.octoblu.com/access_token" }

definition(
name: "Octoblu",
namespace: "citrix",
author: "Octoblu",
description: "Connect SmartThings devices to Octoblu",
category: "SmartThings Labs",
iconUrl: "http://i.imgur.com/BjTfDYk.png",
iconX2Url: "http://i.imgur.com/BjTfDYk.png"
) {
  appSetting "apiUrl"
  appSetting "vendorAuthPath"
  appSetting "vendorTokenPath"
}

preferences {
  page(name: "authPage")
  page(name: "subscribePage")
  page(name: "devicesPage")
}

mappings {
  path("/receiveCode") {
    action: [ GET: "receiveCode" ]
  }
  path("/message") {
    action: [ POST: "receiveMessage" ]
  }
}

// --------------------------------------

def authPage() {

  if (!state.accessToken) {
    createAccessToken()
  }

  debug "using app access token ${state.accessToken}"

  if (!state.vendorOAuthToken) {
    createOAuthDevice()
  }

  def oauthParams = [
  response_type: "code",
  client_id: state.vendorOAuthUuid,
  redirect_uri: "https://graph.api.smartthings.com/api/token/${state.accessToken}/smartapps/installations/${app.id}/receiveCode"
  ]
  def redirectUrl =  getVendorAuthPath() + '?' + toQueryString(oauthParams)
  debug "tokened redirect_uri = ${oauthParams.redirect_uri}"

  def isRequired = !state.vendorBearerToken
  return dynamicPage(name: "authPage", title: "Octoblu Authentication", nextPage:(isRequired ? null : "subscribePage"), install: isRequired, uninstall: showUninstall) {
    section {
      debug "url: ${redirectUrl}"
      if (isRequired) {
        paragraph title: "Token does not exist.", "Please login to Octoblu to complete setup."
      } else {
        paragraph title: "Token created.", "Login is not required."
      }
      href url:redirectUrl, style:"embedded", title: "Authorize", required: isRequired, description:"Click to fetch Octoblu Token."
    }
    section {
      input name: "showUninstall", type: "bool", title: "uninstall", description: "false", submitOnChange: true
      if (showUninstall) {
        paragraph title: "so long and thanks for all the fish", "sorry to see me leave ;_;"
        paragraph title: "i really promise to try harder next time", "please ignore the big red button"
      }
    }
  }
}

def createOAuthDevice() {
  def oAuthDevice = [
    "name": "SmartThings",
    "owner": "68c39f40-cc13-4560-a68c-e8acd021cff9",
    "type": "device:oauth",
    "online": true,
    "options": [
      "name": "SmartThings",
      "imageUrl": "https://i.imgur.com/TsXefbK.png",
      "callbackUrl": "https://graph.api.smartthings.com/api/"
    ],
    "configureWhitelist": [ "68c39f40-cc13-4560-a68c-e8acd021cff9" ],
    "discoverWhitelist": [ "*", "68c39f40-cc13-4560-a68c-e8acd021cff9" ],
    "receiveWhitelist": [ "*" ],
    "sendWhitelist": [ "*" ]
  ]

  def postParams = [ uri: apiUrl()+"devices",
  body: groovy.json.JsonOutput.toJson(oAuthDevice)]

  try {
    httpPostJson(postParams) { response ->
      debug "got new token for oAuth device ${response.data}"
      state.vendorOAuthUuid = response.data.uuid
      state.vendorOAuthToken = response.data.token
    }
  } catch (e) {
    log.error "unable to create oAuth device: ${e}"
  }

}

// --------------------------------------

def subscribePage() {
  return dynamicPage(name: "subscribePage", title: "Subscribe to Things", nextPage: "devicesPage") {
    section {
      // input name: "selectedCapabilities", type: "enum", title: "capability filter",
      // submitOnChange: true, multiple: true, required: false, options: [ "actuator", "sensor" ]
      for (capability in selectedCapabilities) {
         input name: "${capability}Capability".toString(), type: "capability.$capability", title: "$capability things", multiple: true, required: false
      }
    }
  }
}

// --------------------------------------

def devicesPage() {
  def postParams = [
  uri: apiUrl() + "devices?owner=${state.vendorUuid}&category=smart-things",
  headers: ["Authorization": "Bearer ${state.vendorBearerToken}"]]
  state.vendorDevices = [:]

  def hasDevice = [:]
  selectedCapabilities.each { capability ->
    def smartDevices = settings["${capability}Capability"]
    smartDevices.each { smartDevice ->
      hasDevice[smartDevice.id] = true
    }
  }

  debug "getting url ${postParams.uri}"
  try {
    httpGet(postParams) { response ->
      debug "devices json ${response.data.devices}"
      response.data.devices.each { device ->
        if (device.smartDeviceId && hasDevice[device.smartDeviceId]) {
          debug "found device ${device.uuid} with smartDeviceId ${device.smartDeviceId}"
          state.vendorDevices[device.smartDeviceId] = getVendorDeviceStateInfo(device)
        }
        debug "has device: ${device.uuid} ${device.name} ${device.type}"
      }
    }
  } catch (e) {
    log.error "devices error ${e}"
  }

  selectedCapabilities.each { capability ->
    debug "checking devices for capability ${capability}"
    createDevices(settings["${capability}Capability"])
  }

  def devInfo = state.vendorDevices.collect { k, v -> "${v.uuid} " }.sort().join(" \n")

  return dynamicPage(name: "devicesPage", title: "Octoblu Things", install: true) {
    section {
      paragraph title: "my uuid:", "${state.vendorUuid}"
      paragraph title: "my smart things (${state.vendorDevices.size()}):", "${devInfo}"
      paragraph title: "finish setup", "click 'Done' to subscribe to smart thing events"
    }
  }
}

def createDevices(smartDevices) {
  smartDevices.each { smartDevice ->

    def usesArguments = false
    def commandInfo = ""
    def commandArray = [ ".value", ".state", ".device", ".events"]

    smartDevice.supportedCommands.each { command ->
      commandInfo += "<b>${command.name}<b>( ${command.arguments.join(', ')} )<br/>"
      commandArray.push(command.name)
      usesArguments = usesArguments || command.arguments.size()>0
    }

    // def capabilitiesString = "<b>capabilities:<b><br/>" +
    // smartDevice.capabilities.each { capability
    //   capabilitiesString += "<b>${capability.name}</b><br/>"
    // }

    debug "creating device for ${smartDevice.id}"

    def messageSchema = [
      "type": "object",
      "title": "Command",
      "properties": [
        "smartDeviceId" : [
          "type": "string",
          "readOnly": true,
          "default": "${smartDevice.id}"
        ],
        "command": [
          "type": "string",
          "enum": commandArray,
          "default": ".value"
        ]
      ]
    ]

    // if (commandArray.size()>1) {
    //   messageSchema."properties"."delay" = [
    //     "type": "number",
    //     "title": "delay (ms)"
    //   ]
    // }

    if (usesArguments) {
      messageSchema."properties"."arguments" = [
        "type": "array",
        "description": commandInfo,
        "readOnly": !usesArguments,
        "items": [
          "type": "string",
          "title": "arg"
        ]
      ]
    }

    def deviceProperties = [
      "messageSchema": messageSchema,
      "needsSetup": false,
      "online": true,
      "name": "${smartDevice.displayName}",
      "smartDeviceId": "${smartDevice.id}",
      "logo": "https://i.imgur.com/TsXefbK.png",
      "owner": "${state.vendorUuid}",
      "configureWhitelist": [],
      "discoverWhitelist": ["${state.vendorUuid}"],
      "receiveWhitelist": ["*"],
      "sendWhitelist": ["*"],
      "type": "device:${smartDevice.name.replaceAll('\\s','-').toLowerCase()}",
      "category": "smart-things",
      "meshblu": [
        "messageHooks": [
          [
            "url": "https://graph.api.smartthings.com/api/token/${state.accessToken}/smartapps/installations/${app.id}/message",
            "method": "POST",
            "generateAndForwardMeshbluCredentials": false
          ]
        ]
      ]
    ]

    def params = [
    uri: apiUrl() + "devices",
    headers: ["Authorization": "Bearer ${state.vendorBearerToken}"],
    body: groovy.json.JsonOutput.toJson(deviceProperties) ]

    try {

      if (!state.vendorDevices[smartDevice.id]) {
        debug "creating new device for ${smartDevice.id}"
        httpPostJson(params) { response ->
          state.vendorDevices[smartDevice.id] = getVendorDeviceStateInfo(response.data)
        }
        return
      }

      params.uri = params.uri + "/${state.vendorDevices[smartDevice.id].uuid}"
      debug "the device ${smartDevice.id} has already been created, updating ${params.uri}"
      httpPutJson(params) { response ->
        resetVendorDeviceToken(smartDevice.id);
      }

    } catch (e) {
      log.error "unable to create new device ${e}"
    }
  }
}

def getVendorDeviceStateInfo(device) {
  return [ "uuid": device.uuid, "token": device.token ]
}

def resetVendorDeviceToken(smartDeviceId) {
  def deviceUUID = state.vendorDevices[smartDeviceId].uuid
  if (!deviceUUID) {
    debug "no device uuid in resetVendorDeviceToken?"
    return
  }
  debug "getting new token for ${smartDeviceId}/${deviceUUID}"
  def postParams = [
  uri: apiUrl() + "devices/${deviceUUID}/token",
  headers: ["Authorization": "Bearer ${state.vendorBearerToken}"]]
  try {
    httpPost(postParams) { response ->
      state.vendorDevices[smartDeviceId] = getVendorDeviceStateInfo(response.data)
      debug "got new token for ${smartDeviceId}/${deviceUUID}"
    }
  } catch (e) {
    log.error "unable to get new token ${e}"
  }
}

// --------------------------------------

def updated() {
  unsubscribe()
  debug "Updated with settings: ${settings}"
  def subscribed = [:]
  selectedCapabilities.each{ capability ->
    settings."${capability}Capability".each { thing ->
      if (subscribed[thing.id]) {
        return
      }
      subscribed[thing.id] = true
      thing.supportedAttributes.each { attribute ->
        debug "subscribe to attribute ${attribute.name}"
        subscribe thing, attribute.name, eventForward
      }
      thing.supportedCommands.each { command ->
        debug "subscribe to command ${command.name}"
        subscribeToCommand thing, command.name, eventForward
      }
      debug "subscribed to thing ${thing.id}"
    }
  }

  def params = [
  uri: apiUrl() + "devices/${state.vendorUuid}/tokens/${state.vendorToken}",
  headers: ["Authorization": "Bearer ${state.vendorBearerToken}"]]

  debug "deleting url ${params.uri}"
  try {
    httpDelete(params) { response ->
      debug "revoked token for ${state.vendorUuid}...?"
      state.vendorBearerToken = null
      state.vendorUuid = null
      state.vendorToken = null
    }
  } catch (e) {
    log.error "token delete error ${e}"
  }

  params.uri = apiUrl() + "devices/${state.vendorOAuthUuid}"
  params.headers = ["meshblu_auth_uuid": state.vendorOAuthUuid, "meshblu_auth_token": state.vendorOAuthToken]

  debug "deleting url ${params.uri}"
  try {
    httpDelete(params) { response ->
      debug "deleting oauth device for ${state.vendorOAuthUuid}...?"
      state.vendorOAuthUuid = null
      state.vendorOAuthToken = null
    }
  } catch (e) {
    log.error "oauth token delete error ${e}"
  }
}

// --------------------------------------

def receiveCode() {
  // revokeAccessToken()
  // state.accessToken = createAccessToken()
  debug "generated app access token ${state.accessToken}"

  def postParams = [
  uri: getVendorTokenPath(),
  body: [
  client_id: state.vendorOAuthUuid,
  client_secret: state.vendorOAuthToken,
  grant_type: "authorization_code",
  code: params.code ] ]

  def goodResponse = "<html><body><p>&nbsp;</p><h2>Received Octoblu Token!</h2><h3>Click 'Done' to finish setup.</h3></body></html>"
  def badResponse = "<html><body><p>&nbsp;</p><h2>Something went wrong...</h2><h3>PANIC!</h3></body></html>"
  debug "authorizeToken with postParams ${postParams}"

  try {
    httpPost(postParams) { response ->
      debug "response: ${response.data}"
      state.vendorBearerToken = response.data.access_token
      def bearer = new String((new Base64()).decode(state.vendorBearerToken)).split(":")
      state.vendorUuid = bearer[0]
      state.vendorToken = bearer[1]

      debug "have octoblu tokens ${state.vendorBearerToken}"
      render contentType: 'text/html', data: (state.vendorBearerToken ? goodResponse : badResponse)
    }
  } catch(e) {
    log.error "second leg oauth error ${e}"
    render contentType: 'text/html', data: badResponse
  }
}

def getEventData(evt) {
  return [
  "id" : evt.id,
  "name" : evt.name,
  "value" : evt.value,
  "deviceId" : evt.deviceId,
  "hubId" : evt.hubId,
  "locationId" : evt.locationId,
  "installedSmartAppId" : evt.installedSmartAppId,
  "date" : evt.date,
  "dateValue": evt.dateValue,
  "isoDate" : evt.isoDate,
  "isDigital" : evt.isDigital(),
  "isPhysical" : evt.isPhysical(),
  "isStateChange" : evt.isStateChange(),
  "linkText" : evt.linkText,
  "description" : evt.description,
  "descriptionText" : evt.descriptionText,
  "displayName" : evt.displayName,
  "source" : evt.source,
  "unit" : evt.unit,
  "category" : "event",
  "type" : "device:smart-thing"
  ]
}

def eventForward(evt) {
  def eventData = [ "devices" : "*", "payload" : getEventData(evt) ]

  debug "sending event: ${groovy.json.JsonOutput.toJson(eventData)}"

  def vendorDevice = state.vendorDevices[evt.deviceId]
  if (!vendorDevice) {
    log.error "aborting, vendor device for ${evt.deviceId} doesn't exist?"
    return
  }

  debug "using device ${vendorDevice}"

  def postParams = [
  uri: apiUrl() + "messages",
  headers: ["meshblu_auth_uuid": vendorDevice.uuid, "meshblu_auth_token": vendorDevice.token],
  body: groovy.json.JsonOutput.toJson(eventData) ]

  try {
    httpPostJson(postParams) { response ->
      debug "sent off device event"
    }
  } catch (e) {
    log.error "unable to send device event ${e}"
  }
}

// --------------------------------------

def receiveMessage() {
  debug("received data ${request.JSON}")
  def foundDevice = false
  selectedCapabilities.each{ capability ->
    settings."${capability}Capability".each { thing ->
      if (!foundDevice && thing.id == request.JSON.payload.smartDeviceId) {
        foundDevice = true
        if (!request.JSON.payload.command.startsWith(".")) {
          def args = (request.JSON.payload.arguments ?: [])
          thing."${request.JSON.payload.command}"(*args)
        } else {
          debug "calling internal command ${request.JSON.payload.command}"
          def commandData = [:]
          switch (request.JSON.payload.command) {
            case ".value":
              debug "got command .value"
              thing.supportedAttributes.each { attribute ->
                commandData[attribute.name] = thing.latestValue(attribute.name)
              }
              break
            case ".state":
              debug "got command .state"
              thing.supportedAttributes.each { attribute ->
                commandData[attribute.name] = thing.latestState(attribute.name)?.value
              }
              break
            case ".device":
              debug "got command .device"
              commandData = [
                "id" : thing.id,
                "displayName" : thing.displayName,
                "name" : thing.name,
                "label" : thing.label,
                "capabilities" : thing.capabilities.collect{ thingCapability -> return thingCapability.name },
                "supportedAttributes" : thing.supportedAttributes.collect{ attribute -> return attribute.name },
                "supportedCommands" : thing.supportedCommands.collect{ command -> return ["name" : command.name, "arguments" : command.arguments ] }
              ]
              break
            case ".events":
              debug "got command .events"
              commandData.events = []
              thing.events().each { event ->
                commandData.events.push(getEventData(event))
              }
              break
            default:
              commandData.error = "unknown command"
              debug "unknown command ${request.JSON.payload.command}"
          }
          commandData.command = request.JSON.payload.command

          debug "done switch!"

          def vendorDevice = state.vendorDevices[thing.id]
          debug "with vendorDevice ${vendorDevice} for ${groovy.json.JsonOutput.toJson(commandData)}"

          def postParams = [
            uri: apiUrl() + "messages",
            headers: ["meshblu_auth_uuid": vendorDevice.uuid, "meshblu_auth_token": vendorDevice.token],
            body: groovy.json.JsonOutput.toJson([ "devices" : "*", "payload" : commandData ]) ]

          debug "posting params ${postParams}"

          try {
            debug "calling httpPostJson!"
            httpPostJson(postParams) { response ->
              debug "sent off command result"
            }
          } catch (e) {
            log.error "unable to send command result ${e}"
          }

        }

        debug "done else"
      }
    }
  }
}

// --------------------------------------

private debug(logStr) {
  if (USE_DEBUG)
    log.debug logStr
}

String toQueryString(Map m) {
  return m.collect { k, v -> "${k}=${URLEncoder.encode(v.toString())}" }.sort().join("&")
}

def initialize()
{
  debug "Initialized with settings: ${settings}"
}

def uninstalled()
{
  debug "In uninstalled"
}

def installed() {
  debug "Installed with settings: ${settings}"
}

private Boolean canInstallLabs()
{
  return hasAllHubsOver("000.011.00603")
}

private Boolean hasAllHubsOver(String desiredFirmware)
{
  return realHubFirmwareVersions.every { fw -> fw >= desiredFirmware }
}

private List getRealHubFirmwareVersions()
{
  return location.hubs*.firmwareVersionString.findAll { it }
}