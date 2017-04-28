/**
 *  Homebridge Routine Triggers
 *
 *  Copyright 2015 Jesse Newland
 *  Copyright 2016 Alexander White
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
    name: "Homebridge Routine Triggers",
    namespace: "ajvwhite",
    author: "Alexander White & Jesse Newland",
    description: "A SmartThings app designed to work with homebridge to provide Siri triggers for your SmartThings Routines.",
    category: "SmartThings Labs",
    iconUrl: "https://raw.githubusercontent.com/ajvwhite/homebridge-smartthings-routine-triggers/master/smartapp_icons/HomebridgeRoutineTrigger.png",
    iconX2Url: "https://raw.githubusercontent.com/ajvwhite/homebridge-smartthings-routine-triggers/master/smartapp_icons/HomebridgeRoutineTrigger@2x.png",
    iconX3Url: "https://raw.githubusercontent.com/ajvwhite/homebridge-smartthings-routine-triggers/master/smartapp_icons/HomebridgeRoutineTrigger@3x.png",
    oauth: true)


def installed() {
    log.debug "Installed with settings: ${settings}"
    initialize()
}

def updated() {
    log.debug "Updated with settings: ${settings}"
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
    dynamicPage(name: "copyConfig", title: "Config", install:true, uninstall:true) {
        section() {
            paragraph "Copy/Paste the below into your homebridge's config.json to create HomeKit accessories for your SmartThing Routines"
            href url:"https://graph-na02-useast1.api.smartthings.com:443/api/smartapps/installations/${app.id}/config?access_token=${state.accessToken}", style:"embedded", required:false, title:"Config", description:"Tap, select, copy, then click \"Done\""
        }
    }
}

def renderConfig() {
    def configJson = new groovy.json.JsonOutput().toJson(location?.helloHome?.getPhrases().collect({
        [
            accessory: "HomebridgeRoutineTrigger",
            name: it.label,
            smartAppId: app.id,
            accessToken: state.accessToken,
            appServerUri: getApiServerUrl()
        ]
    }))

    def configString = new groovy.json.JsonOutput().prettyPrint(configJson)
    render contentType: "text/plain", data: configString
}

def triggerRoutine() {
    def routineRequested = request.JSON?.routine
    
    if(routineRequested?.trim()) {
        location.helloHome?.execute(routineRequested)
    } else {
        httpError(400, "No Routine Requested")
    }
    
    render contentType: "text/plain", data: "OK", status: 200
}

mappings {
    if (!params.access_token || (params.access_token && params.access_token != state.accessToken)) {
        path("/config") { action: [GET: "authError"] }
        path("/trigger-routine") { action: [POST: "authError"] }
    } else {
        path("/config") { action: [GET: "renderConfig"] }
        path("/trigger-routine") { action: [POST: "triggerRoutine"] }
    }
}

def authError() {
    httpError(401, "Permission denied")
}