/**
*  D-Link Camera Manager (Connect)
*
*  Copyright 2016 Ben Lebson
*  Parent/Child SmartApp based on Patrick Stuart's Generic Video Camera SmartApp
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
    name: "D-Link Camera Manager (Connect)",
    namespace: "blebson",
    author: "Ben Lebson",
    description: "This smartapp installs the D-Link Camera Manager (Connect) App so you can add multiple D-Link IP cameras",
    category: "Safety & Security",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Camera/dlink.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Camera/dlink@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Camera/dlink@3x.png",
    singleInstance: true)


preferences {
    page(name: "mainPage", title: "Existing Cameras", install: true, uninstall: true) {
        if(state?.installed) {
            section("Add a New Camera") {
                app "Dlink Video Camera Child", "blebson", "Dlink Video Camera Child", title: "New Fixed D-Link Camera", page: "mainPage", multiple: true, install: true
                app "Dlink PTZ Video Camera Child", "blebson", "Dlink PTZ Video Camera Child", title: "New PTZ D-Link Camera", page: "mainPage", multiple: true, install: true
            }
        } else {
            section("Initial Install") {
                paragraph "This smartapp installs the D-Link Camera Manager (Connect) App so you can add multiple D-Link IP cameras. Click 'Done' then go to smartapps in the flyout menu and add new cameras or edit existing cameras."
            }
        }
    }
}

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
    state.installed = true
}