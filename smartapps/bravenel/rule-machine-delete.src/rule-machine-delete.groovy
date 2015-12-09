/**
 *  Rule Machine Delete
 *
 *  Copyright 2015 Bruce Ravenel
 *
 *  Version 1.2a   8 Dec 2015
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
    name: "Rule Machine",
    singleInstance: true,
    namespace: "bravenel",
    author: "Bruce Ravenel",
    description: "Rule Machine Delete",
    category: "My Apps",
  	iconUrl: "https://s3.amazonaws.com/smartapp-icons/ModeMagic/Cat-ModeMagic.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/ModeMagic/Cat-ModeMagic@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/ModeMagic/Cat-ModeMagic@3x.png"
)

preferences {
	page(name: "mainPage")
}

def mainPage() {
    dynamicPage(name: "mainPage", title: "Rules Machine Remove", install: true, uninstall: true, submitOnChange: true) {
        section {
            app(name: "childRules", appName: "Rule", namespace: "bravenel", title: "Remove Rule Machine...", multiple: true)
        }
    }
}

def installed() {
    initialize()   
}

def updated() {
    initialize()
}

def initialize() {
	    childApps.each {child ->
		if(child.name == "Rule") {
			log.info "Installed Rules and Triggers: ${child.label}"
		}
    }
}

def removeChild(appLabel) {
}
