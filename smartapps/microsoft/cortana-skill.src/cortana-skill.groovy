/**
 *  Cortana Skill
 *
 *  Copyright 2017 Microsoft Corporation
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
    name: "Cortana Skill",
    namespace: "microsoft",
    author: "Logan Stromberg",
    description: "Cortana skill integration",
    category: "Convenience",
    iconUrl: "https://botletstorage.blob.core.windows.net/static-template-images/Cortana-64.png",
    iconX2Url: "https://botletstorage.blob.core.windows.net/static-template-images/Cortana-128.png",
    iconX3Url: "https://botletstorage.blob.core.windows.net/static-template-images/Cortana-256.png")


preferences(oauthPage: "deviceAuthorization") {
	page(name: "deviceAuthorization", title: "", install: false, uninstall: true)
    {
        section("Switches")
        {
            input "switches", "capability.switch", multiple: true, required: false
        }
        section("Dimmers")
        {
            input "dimmers", "capability.switchLevel", multiple: true, required: false
        }
    }
}

def installed() {
	//log.debug "Installed with settings: ${settings}"
	initialize()
}

def updated() {
	//log.debug "Updated with settings: ${settings}"
	unsubscribe()
	initialize()
}

def initialize() {
	subscribe(app, appTouch)
}

mappings {
  path("/status") {
    action: [
      GET: "showStatus"
    ]
  }
  path("/switches/:id") {
    action: [
      PUT: "commandSwitch"
    ]
  }
  path("/dimmers/:id") {
    action: [
      PUT: "commandDimmer"
    ]
  }
}

def showStatus() {

    def returnVal = []
    switches.each { device ->
    	def s = device.currentState("switch")
        returnVal.add([type: "switch", id: device.id, label: device.displayName, name: device.displayName, state: s])
    }
    dimmers.each { device ->
    	def s = device.currentState("level")
        returnVal.add([type: "dimmer", id: device.id, label: device.displayName, name: device.displayName, state: s])
    }
    
    return returnVal;
}

void commandSwitch() {
    log.debug "commandSwitch, request: ${request.JSON}, params: ${params}"
    
    def command = request.JSON?.command
    
    if (command)
    {
        def mySwitch = switches.find { it.id == params.id }

		if (!mySwitch)
        {
            mySwitch = dimmers.find { it.id == params.id }
        }

        if (!mySwitch)
        {
            httpError(404, "Switch not found")
        }
        else
        {
            mySwitch."$command"()
        }
    }
}

void commandDimmer() {
    log.debug "commandDimmer, request: ${request.JSON}, params: ${params}"
    
    def value = request.JSON?.value
    
    if (value)
    {
        def mySwitch = dimmers.find { it.id == params.id }

        if (!mySwitch)
        {
            httpError(404, "Dimmer not found")
        }
        else
        {
            log.debug "setting value to ${value}"
            mySwitch.setLevel(value)
        }
    }
}