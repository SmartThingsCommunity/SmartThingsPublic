/**
 *  Routine Activated Notifier
 *
 *  Copyright 2016 Richard Pope
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
    name: "Routine Activated Notifier",
    namespace: "VaticanUK",
    author: "Richard Pope",
    description: "Calls a URL when a routine is executed",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")

preferences {
    page(name: "selectActions")
}

def selectActions() {
    dynamicPage(name: "selectActions", title: "Select Hello Home Action to Monitor", install: true, uninstall: true) {

        // get the available actions
        def actions = location.helloHome?.getPhrases()*.label
        if (actions) {
            // sort them alphabetically
            actions.sort()
            section("Hello Home Actions") {
                log.trace actions
                // use the actions as the options for an enum input
                input "action", "enum", title: "Select an action to execute", options: actions, required: true
                input "url", "text", title: "Enter a URL to call", required: true
            }
            section ([mobileOnly:true]) {
                label title: "Assign a name", required: false
                mode title: "Set for specific mode(s)", required: false
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
	subscribe(location, "routineExecuted", routineChanged)
}

def routineChanged(evt) {
    log.debug "routineChanged: $evt"

    log.debug "evt name: ${evt.name}"
    log.debug "evt value: ${evt.value}"
    log.debug "evt displayName: ${evt.displayName}"

	if (evt.displayName == settings.action) {
    	def params = [ uri: settings.url ]
        
        try {
        	httpGet(params) { resp ->
            	resp.headers.each { 
                	log.debug "${it.name} : ${it.value}"
                }
                
                def theHeaders = resp.getHeaders("Content-Length")
        		log.debug "response contentType: ${resp.contentType}"
                log.debug "response status code: ${resp.status}"
                log.debug "response data: ${resp.data}"
            }
        } catch (e) {
        	log.error "something went wrong: $e"
        }
    }
}