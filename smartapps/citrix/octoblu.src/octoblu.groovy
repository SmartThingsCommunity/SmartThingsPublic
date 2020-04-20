/**
The MIT License (MIT)
Copyright (c) 2016 Octoblu
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

@Field final USE_DEBUG = true
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
  page(name: "welcomePage")
  page(name: "authPage")
  page(name: "subscribePage")
  page(name: "devicesPage")
}

mappings {
  path("/oauthCode") {
    action: [ GET: "getOauthCode" ]
  }
  path("/message") {
    action: [ POST: "postMessage" ]
  }
  path("/app") {
    action: [ POST: "postApp" ]
  }
}

// --------------------------------------

def getDevInfo() {
  return state.vendorDevices.collect { k, v -> "${v.uuid} " }.sort().join(" \n")
}

// --------------------------------------

def welcomePage() {
  cleanUpTokens()

  return dynamicPage(name: "welcomePage", nextPage: "authPage", uninstall: showUninstall) {
    section {
      paragraph title: "Welcome to the Octoblu SmartThings App!", "press 'Next' to continue"
    }
    if (state.vendorDevices && state.vendorDevices.size()>0) {
      section {
        paragraph title: "My SmartThings in Octobu (${state.vendorDevices.size()}):", getDevInfo()
      }
    }
    if (state.installed) {
      section {
        input name: "showUninstall", type: "bool", title: "Uninstall", submitOnChange: true
        if (showUninstall) {
          state.removeDevices = removeDevices
          input name: "removeDevices", type: "bool", title: "Remove Octoblu devices", submitOnChange: true
          paragraph title: "Sorry to see you go!", "please email <support@octoblu.com> with any feedback or issues"
        }
      }
    }
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
    redirect_uri: getApiServerUrl() + "/api/token/${state.accessToken}/smartapps/installations/${app.id}/oauthCode"
  ]

  def redirectUrl =  getVendorAuthPath() + '?' + toQueryString(oauthParams)
  debug "tokened redirect_uri = ${oauthParams.redirect_uri}"

  def isRequired = !state.vendorBearerToken
  return dynamicPage(name: "authPage", title: "Octoblu Authentication", nextPage:(isRequired ? null : "subscribePage"), install: isRequired) {
    section {
      debug "url: ${redirectUrl}"
      if (isRequired) {
        href url:redirectUrl, style:"embedded", title: "Authorize with Octoblu", required: isRequired, description:"please login with Octoblu to complete setup"
      } else {
        paragraph title: "Please press 'Next' to continue", "Octoblu token has been created"
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
      "callbackUrl": getApiServerUrl() + "/api"
    ],
    "configureWhitelist": [ "68c39f40-cc13-4560-a68c-e8acd021cff9" ],
    "discoverWhitelist": [ "*" ],
    "receiveWhitelist": [],
    "sendWhitelist": []
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
  return dynamicPage(name: "subscribePage", title: "Subscribe to SmartThing devices", nextPage: "devicesPage") {
    section {
      // input name: "selectedCapabilities", type: "enum", title: "capability filter",
      // submitOnChange: true, multiple: true, required: false, options: [ "actuator", "sensor" ]
      for (capability in selectedCapabilities) {
         input name: "${capability}Capability".toString(), type: "capability.$capability", title: "${capability.capitalize()} Things", multiple: true, required: false
      }
    }
    section(" ") {
      input name: "pleaseCreateAppDevice", type: "bool", title: "Create a SmartApp device", defaultValue: true
      paragraph "A SmartApp device allows access to location and hub information for this installation"
    }
    section(" ") {
      paragraph title: "", "Existing Octoblu devices may be modified!"
    }
  }
}

// --------------------------------------

def devicesPage() {
  def postParams = [
    uri: apiUrl() + "devices?owner=${state.vendorUuid}&category=smart-things",
    headers: ["Authorization": "Bearer ${state.vendorBearerToken}"]
  ]
  state.vendorDevices = [:]

  def hasDevice = [:]
  hasDevice[app.id] = true
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
          state.vendorDevices[device.smartDeviceId] = getDeviceInfo(device)
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
  if (pleaseCreateAppDevice)
    createAppDevice()

  return dynamicPage(name: "devicesPage", title: "Octoblu Things", install: true) {
    section {
      paragraph title: "Please press 'Done' to finish setup", "and subscribe to SmartThing events"
      paragraph title: "My Octoblu UUID:", "${state.vendorUuid}"
      paragraph title: "My SmartThings in Octobu (${state.vendorDevices.size()}):", getDevInfo()
    }
  }
}

def createDevices(smartDevices) {

  smartDevices.each { smartDevice ->
    def commands = [
      [ "name": "app-get-value" ],
      [ "name": "app-get-state" ],
      [ "name": "app-get-device" ],
      [ "name": "app-get-events" ]
    ]

    smartDevice.supportedCommands.each { command ->
      if (command.arguments.size()>0) {
        commands.push([ "name": command.name, "args": command.arguments ])
      } else {
        commands.push([ "name": command.name ])
      }
    }

    debug "creating device for ${smartDevice.id}"

    def schemas = [
      "version": "2.0.0",
      "message": [:]
    ]

    commands.each { command ->
      schemas."message"."$command.name" = [
        "type": "object",
        "properties": [
          "smartDeviceId": [
            "type": "string",
            "readOnly": true,
            "default": "$smartDevice.id",
            "x-schema-form": [
            	"condition": "false"
            ]
          ],
          "command": [
            "type": "string",
            "readOnly": true,
            "default": "$command.name",
            "enum": ["$command.name"],
            "x-schema-form": [
            	"condition": "false"
            ]
          ]
        ]
      ]

      if (command.args) {
        schemas."message"."$command.name"."properties"."args" = [
          "type": "object",
          "title": "Arguments",
          "properties": [:]
        ]

        command.args.each { arg ->
          def argLower = "$arg"
          argLower = argLower.toLowerCase()
          if (argLower == "color_map") {
            schemas."message"."$command.name"."properties"."args"."properties"."$argLower" = [
              "type": "object",
              "properties": [
                "hex": [
                  "type": "string"
                ],
                "level": [
                  "type": "number"
                ]
              ]
            ]
          } else {
            schemas."message"."$command.name"."properties"."args"."properties"."$argLower" = [
              "type": "$argLower"
            ]
          }
        }
      }
    }

    debug "UPDATED message schema: ${schemas}"

    def deviceProperties = [
      "schemas": schemas,
      "needsSetup": false,
      "online": true,
      "name": "${smartDevice.displayName}",
      "smartDeviceId": "${smartDevice.id}",
      "logo": "https://i.imgur.com/TsXefbK.png",
      "owner": "${state.vendorUuid}",
      "configureWhitelist": [],
      "discoverWhitelist": ["${state.vendorUuid}"],
      "receiveWhitelist": [],
      "sendWhitelist": [],
      "type": "device:${smartDevice.name.replaceAll('\\s','-').toLowerCase()}",
      "category": "smart-things",
      "meshblu": [
        "forwarders": [
          "received": [[
            "url": getApiServerUrl() + "/api/token/${state.accessToken}/smartapps/installations/${app.id}/message",
            "method": "POST",
            "type": "webhook"
          ]]
        ]
      ]
    ]

    updatePermissions(deviceProperties, smartDevice.id)
    def params = [
      uri: apiUrl() + "devices",
      headers: ["Authorization": "Bearer ${state.vendorBearerToken}"],
      body: groovy.json.JsonOutput.toJson(deviceProperties)
    ]

    try {

      if (!state.vendorDevices[smartDevice.id]) {
        debug "creating new device for ${smartDevice.id}"
        httpPostJson(params) { response ->
          state.vendorDevices[smartDevice.id] = getDeviceInfo(response.data)
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

def createAppDevice() {
  def commands = [
    [ "name": "app-get-location" ],
    [ "name": "app-get-devices" ],
    [ "name": "app-set-mode" ],
  ]

  def schemas = [
    "version": "2.0.0",
    "message": [:]
  ]

  commands.each { command ->
    schemas."message"."$command.name" = [
      "type": "object",
      "properties": [
        "command": [
          "type": "string",
          "readOnly": true,
          "default": "$command.name",
          "enum": ["$command.name"],
          "x-schema-form": [
            "condition": "false"
          ]
        ]
      ]
    ]
  }

  schemas."message"."app-set-mode"."properties"."args" = [
    "type": "object",
    "title": "Arguments",
    "properties": [
      "mode": [
        "type": "string"
      ]
    ]
  ]

  def deviceProperties = [
    "schemas": schemas,
    "needsSetup": false,
    "online": true,
    "name": "${location.name} SmartApp",
    "smartDeviceId": "${app.id}",
    "logo": "https://i.imgur.com/TsXefbK.png",
    "owner": "${state.vendorUuid}",
    "configureWhitelist": [],
    "discoverWhitelist": ["${state.vendorUuid}"],
    "receiveWhitelist": [],
    "sendWhitelist": [],
    "type": "device:smart-things-app",
    "category": "smart-things",
    "meshblu": [
      "forwarders": [
        "received": [[
          "url": getApiServerUrl() + "/api/token/${state.accessToken}/smartapps/installations/${app.id}/app",
          "method": "POST",
          "type": "webhook"
        ]]
      ]
    ]
  ]

  updatePermissions(deviceProperties, app.id)
  def params = [
    uri: apiUrl() + "devices",
    headers: ["Authorization": "Bearer ${state.vendorBearerToken}"],
    body: groovy.json.JsonOutput.toJson(deviceProperties)
  ]

  debug "creating app device!"
  debug params.body
  try {

    // debug params
    if (!state.vendorDevices[app.id]) {
      debug "creating new app device for ${app.id}"
      httpPostJson(params) { response ->
        state.vendorDevices[app.id] = getDeviceInfo(response.data)
      }
      return
    }

    params.uri = params.uri + "/${state.vendorDevices[app.id].uuid}"
    debug "the app device ${app.id} has already been created, updating ${params.uri}"
    httpPutJson(params) { response ->
      resetVendorDeviceToken(app.id);
    }

  } catch (e) {
    log.error "unable to create new device ${e}"
  }

}

def updatePermissions(newDevice, id) {
  def device = state.vendorDevices[id]
  if (!device) return
  newDevice.configureWhitelist = device.configureWhitelist
  newDevice.discoverWhitelist = device.discoverWhitelist
  newDevice.receiveWhitelist = device.receiveWhitelist
  newDevice.sendWhitelist = device.sendWhitelist
}

def getDeviceInfo(device) {
  return [
    "uuid": device.uuid,
    "token": device.token,
    "configureWhitelist": device.configureWhitelist,
    "discoverWhitelist": device.discoverWhitelist,
    "receiveWhitelist": device.receiveWhitelist,
    "sendWhitelist": device.sendWhitelist,
  ]
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
      state.vendorDevices[smartDeviceId] = getDeviceInfo(response.data)
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
  cleanUpTokens()
}

// --------------------------------------

def cleanUpTokens() {

  if (state.vendorToken) {
    def params = [
      uri: apiUrl() + "devices/${state.vendorUuid}/tokens/${state.vendorToken}",
      headers: ["Authorization": "Bearer ${state.vendorBearerToken}"]
    ]

    debug "deleting url ${params.uri}"
    try {
      httpDelete(params) { response ->
        debug "revoked token for ${state.vendorUuid}...?"
      }
    } catch (e) {
      log.error "token delete error ${e}"
    }
  }

  state.vendorBearerToken = null
  state.vendorUuid = null
  state.vendorToken = null

  if (state.vendorOAuthToken) {
    def params = [
      uri: apiUrl() + "devices/${state.vendorOAuthUuid}",
      headers: [
        "meshblu_auth_uuid": state.vendorOAuthUuid,
        "meshblu_auth_token": state.vendorOAuthToken
      ]
    ]

    debug "deleting url ${params.uri}"
    try {
      httpDelete(params) { response ->
        debug "deleted oauth device for ${state.vendorOAuthUuid}...?"
      }
    } catch (e) {
      log.error "oauth token delete error ${e}"
    }
  }

  state.vendorOAuthUuid = null
  state.vendorOAuthToken = null

}

// --------------------------------------

def getOauthCode() {
  // revokeAccessToken()
  // state.accessToken = createAccessToken()
  debug "generated app access token ${state.accessToken}"

  def postParams = [
    uri: getVendorTokenPath(),
    body: [
      client_id: state.vendorOAuthUuid,
      client_secret: state.vendorOAuthToken,
      grant_type: "authorization_code",
      code: params.code
    ]
  ]

  def style = "<style type='text/css'>body{font-size:2em;padding:1em}</style>"
  def startBody = "<html>${style}<body>"
  def endBody = "</body></html>"
  def goodResponse = "${startBody}<h1>Received Octoblu Token!</h1><h2>Press 'Done' to finish setup.</h2>${endBody}"
  def badResponse = "${startBody}<h1>Something went wrong...</h1><h2>PANIC!</h2>${endBody}"
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
    "date" : evt.date,
    "id" : evt.id,
    "data" : evt.data,
    "description" : evt.description,
    "descriptionText" : evt.descriptionText,
    "displayName" : evt.displayName,
    "deviceId" : evt.deviceId,
    "hubId" : evt.hubId,
    "installedSmartAppId" : evt.installedSmartAppId,
    "isoDate" : evt.isoDate,
    "isDigital" : evt.isDigital(),
    "isPhysical" : evt.isPhysical(),
    "isStateChange" : evt.isStateChange(),
    "locationId" : evt.locationId,
    "name" : evt.name,
    "source" : evt.source,
    "unit" : evt.unit,
    "value" : evt.value,
    "category" : "event",
    "type" : "device:smart-thing"
  ]
}

def eventForward(evt) {
  def eventData = [ "devices" : [ "*" ], "payload" : getEventData(evt) ]

  debug "sending event: ${groovy.json.JsonOutput.toJson(eventData)}"

  def vendorDevice = state.vendorDevices[evt.deviceId]
  if (!vendorDevice) {
    log.error "aborting, vendor device for ${evt.deviceId} doesn't exist?"
    return
  }

  debug "using device ${vendorDevice}"

  def postParams = [
    uri: apiUrl() + "messages",
    headers: [
      "meshblu_auth_uuid": vendorDevice.uuid,
      "meshblu_auth_token": vendorDevice.token
    ],
    body: groovy.json.JsonOutput.toJson(eventData)
  ]

  try {
    httpPostJson(postParams) { response ->
      debug "sent off device event"
    }
  } catch (e) {
    log.error "unable to send device event ${e}"
  }
}

// --------------------------------------

def postMessage() {
  debug("received message data ${request.JSON}")
  def foundDevice = false
  selectedCapabilities.each{ capability ->
    settings."${capability}Capability".each { thing ->
      if (!foundDevice && thing.id == request.JSON.smartDeviceId) {
        def vendorDevice = state.vendorDevices[thing.id]
        foundDevice = true
        if (vendorDevice.uuid == request.JSON.fromUuid) {
          log.error "aborting message from self"
          return
        }

        if (!request.JSON.command.startsWith("app-")) {
          def args = []
          if (request.JSON.args) {
            request.JSON.args.each { k, v ->
              args.push(v)
            }
          }

          debug "command being sent: ${request.JSON.command}\targs to be sent: ${args}"
          thing."${request.JSON.command}"(*args)
        } else {
          debug "calling internal command ${request.JSON.command}"
          def commandData = [:]
          switch (request.JSON.command) {
            case "app-get-value":
              debug "got command value"
              thing.supportedAttributes.each { attribute ->
                commandData[attribute.name] = thing.latestValue(attribute.name)
              }
              break
            case "app-get-state":
              debug "got command state"
              thing.supportedAttributes.each { attribute ->
                commandData[attribute.name] = thing.latestState(attribute.name)?.value
              }
              break
            case "app-get-device":
              debug "got command device"
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
            case "app-get-events":
              debug "got command events"
              commandData.events = []
              thing.events().each { event ->
                commandData.events.push(getEventData(event))
              }
              break
            default:
              commandData.error = "unknown command"
              debug "unknown command ${request.JSON.command}"
          }
          commandData.command = request.JSON.command
          debug "with vendorDevice ${vendorDevice} for ${groovy.json.JsonOutput.toJson(commandData)}"

          def postParams = [
            uri: apiUrl() + "messages",
            headers: ["meshblu_auth_uuid": vendorDevice.uuid, "meshblu_auth_token": vendorDevice.token],
            body: groovy.json.JsonOutput.toJson([ "devices" : [ "*" ], "payload" : commandData ])
          ]

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
      }
    }
  }
}

// --------------------------------------

def postApp() {
  debug("received app data ${request.JSON}")
  if (state.vendorDevices[app.id].uuid == request.JSON.fromUuid) {
    log.error "aborting message from self"
    return
  }
  def args = []
  if (request.JSON.args) {
    request.JSON.args.each { k, v ->
      args.push(v)
    }
  }

  def commandData = [:]

  switch (request.JSON.command) {
    case "app-get-location":
      debug "got command location"

      def modes = []
      location.modes.each { mode ->
        modes.push([
          "id" : mode.id,
          "name" : mode.name
        ])
      }

      def hubs = []
      location.hubs.each { hub ->
        debug "hub : ${hub}"
        hubs.push([
          "firmwareVersionString" : hub.firmwareVersionString,
          "id" : hub.id,
          "localIP" : hub.localIP,
          "localSrvPortTCP" : hub.localSrvPortTCP,
          "name" : hub.name,
          "type" : hub.type,
          "zigbeeEui" : hub.zigbeeEui,
          "zigbeeId" : hub.zigbeeId
        ])
      }

      commandData = [
        "contactBookEnabled" : location.contactBookEnabled,
        "id" : location.id,
        "latitude" : location.latitude,
        "longitude" : location.longitude,
        "temperatureScale" : location.temperatureScale,
        "timeZone" : location.timeZone.getID(),
        "zipCode" : location.zipCode,
        "mode" : location.mode,
        "modes" : modes,
        "hubs" : hubs
      ]

      debug "copied location!"
      debug commandData
      break

    case "app-get-devices":
      debug "got command devices"
      commandData.devices = state.vendorDevices.collect { k, v -> [ "smartDeviceId" : k, "uuid" : v.uuid ] }
      break

    case "app-set-mode":
      location.setMode(*args)
      commandData.mode = args[0]
      break

    default:
      commandData.error = "unknown command"
      debug "unknown command ${request.JSON.command}"
  }
  commandData.command = request.JSON.command
  debug "sending ${commandData}"

  def vendorDevice = state.vendorDevices[app.id]
  debug "with vendorDevice ${vendorDevice} for ${groovy.json.JsonOutput.toJson(commandData)}"

  def postParams = [
    uri: apiUrl() + "messages",
    headers: [ "meshblu_auth_uuid" : vendorDevice.uuid, "meshblu_auth_token" : vendorDevice.token ],
    body: groovy.json.JsonOutput.toJson([ "devices" : [ "*" ], "payload" : commandData ])
  ]

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
  debug "In uninstalled ${state.removeDevices}"
  if (state.removeDevices) {
    state.vendorDevices.each  { k, device ->
      def params = [
        uri: apiUrl() + "devices/${device.uuid}",
        headers: [ "meshblu_auth_uuid" : device.uuid, "meshblu_auth_token" : device.token ],
      ]

      debug "deleting url ${params.uri}"
      try {
        httpDelete(params) { response ->
          debug "delete device ${device.uuid}"
        }
      } catch (e) {
        log.error "token delete error ${e}"
      }
    }
  }
}

def installed() {
  debug "Installed with settings: ${settings}"
  state.installed = true
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