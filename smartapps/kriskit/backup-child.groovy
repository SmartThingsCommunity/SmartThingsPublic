/**
 *  Backup Child
 *
 *  Copyright 2016 Chris Kitch
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
    name: "Backup Child",
    namespace: "kriskit",
    author: "Chris Kitch",
    description: "A child app to be backed up and restored.",
    category: "",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    parent: "kriskit:Backup Test")

preferences {
	page(name: "configure")
}

def configure() {
	dynamicPage(name: "configure", uninstall: true, install: true) {
    	section {
        	label title: "Name", required: true
    		input "aValue", "text", title: "Some Value", required: true, description: "Whatever you want"
            input "switches", "capability.switch", title: "Switches", required: true, multiple: true
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
	subscribe(switches, "switch", onSwitchChange)
}

def onSwitchChange(evt) {
	log.debug "Switch state changed ${evt}"
}

public getDescriptor() {
	def result = [label: "${app.label} Backup", settings: [aValue: [type: "text", value: aValue], switches: [type: "capability.switch", value: switches?.collect { it.id }]]]
    log.debug result
    return result
}
