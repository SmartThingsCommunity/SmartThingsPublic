/**
 *  Door &amp; Lock Manager 
 *
 *  Copyright 2016 Oscar Chen
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
    name: "Door & Lock Manager",
    namespace: "airoscar/doorlockmanagerparent",
    author: "Oscar Chen",
    description: "Manages door and lock behaviors. Send push notification or text message if the door is left open or if the lock is left unlocked for a preset amount of time; as well as automatically locking the lock with a preset delay after the door has been closed.",
    category: "Convenience",
    singleInstance: true,
    iconUrl: "http://cdn.device-icons.smartthings.com/Home/home3-icn.png",
    iconX2Url: "http://cdn.device-icons.smartthings.com/Home/home3-icn@2x.png",
    iconX3Url: "http://cdn.device-icons.smartthings.com/Home/home3-icn@2x.png"
    
)


preferences {
	page(name: "mainPage", title: "Door Lock Automations", install: true, uninstall: true) {
        section {
            app(name: "Automations", appName: "Door Lock Automation", namespace: "airoscar/lockautomations", title: "New Door Lock Automation", multiple: true)
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
	// TODO: subscribe to attributes, devices, locations, etc.
}

// TODO: implement event handlers