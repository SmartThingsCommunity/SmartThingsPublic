/**
 *  JSON
 *
 *  Copyright 2015 Jesse Newland
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
    name: "JSON API",
    namespace: "jnewland",
    author: "Jesse Newland",
    description: "A JSON API for SmartThings",
    category: "SmartThings Labs",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    oauth: true)


def installed() {
    initialize()
}

def updated() {
    unsubscribe()
    initialize()
}

def initialize() {
    if (!state.accessToken) {
        createAccessToken()
    }
}

preferences {
    page(name: "copyConfig")
}

def copyConfig() {
    if (!state.accessToken) {
        createAccessToken()
    }
    dynamicPage(name: "copyConfig", title: "Config", install:true) {
        section("Select devices to include in the /devices API call") {
            input "switches", "capability.switch", title: "Switches", multiple: true, required: false
            input "hues", "capability.colorControl", title: "Hues", multiple: true, required: false
        }

        section() {
            paragraph "View this SmartApp's configuration to use it in other places."
            href url:"https://graph-eu01-euwest1.api.smartthings.com/api/smartapps/installations/${app.id}/config?access_token=${state.accessToken}", style:"embedded", required:false, title:"Config", description:"Tap, select, copy, then click \"Done\""
        }

        section() {
            href url:"https://graph-eu01-euwest1.api.smartthings.com/api/smartapps/installations/${app.id}/devices?access_token=${state.accessToken}", style:"embedded", required:false, title:"Debug", description:"View accessories JSON"
        }
    }
}

def renderConfig() {
    def configJson = new groovy.json.JsonOutput().toJson([
        description: "JSON API",
        platforms: [
            [
                platform: "SmartThings",
                name: "SmartThings",
                app_id:        app.id,
                access_token:  state.accessToken
            ]
        ],
    ])

    def configString = new groovy.json.JsonOutput().prettyPrint(configJson)
    render contentType: "text/plain", data: configString
}

def deviceCommandMap(device, type) {
  device.supportedCommands.collectEntries { command->
      def commandUrl = "https://graph-eu01-euwest1.api.smartthings.com/api/smartapps/installations/${app.id}/${type}/${device.id}/command/${command.name}?access_token=${state.accessToken}"
      [
        (command.name): commandUrl
      ]
  }
}

def authorizedDevices() {
    [
        switches: switches,
        hues: hues
    ]
}

def renderDevices() {
    def deviceData = authorizedDevices().collectEntries { devices->
        [
            (devices.key): devices.value.collect { device->
                [
                    name: device.displayName,
                    commands: deviceCommandMap(device, devices.key)
                ]
            }
        ]
    }
    def deviceJson    = new groovy.json.JsonOutput().toJson(deviceData)
    def deviceString  = new groovy.json.JsonOutput().prettyPrint(deviceJson)
    render contentType: "application/json", data: deviceString
}

def deviceCommand() {
  def device  = authorizedDevices()[params.type].find { it.id == params.id }
  def command = params.command
  if (!device) {
      httpError(404, "Device not found")
  } else {
      if (params.value) {
        device."$command"(params.value)
      } else {
        device."$command"()
      }
  }
}

mappings {
    if (!params.access_token || (params.access_token && params.access_token != state.accessToken)) {
        path("/devices")                      { action: [GET: "authError"] }
        path("/config")                       { action: [GET: "authError"] }
        path("/:type/:id/command/:command")   { action: [PUT: "authError"] }
    } else {
        path("/devices")                      { action: [GET: "renderDevices"]  }
        path("/config")                       { action: [GET: "renderConfig"]  }
        path("/:type/:id/command/:command")   { action: [PUT: "deviceCommand"] }
    }
}

def authError() {
    [error: "Permission denied"]
}