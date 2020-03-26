/**
 *  Copyright 2015 SmartThings
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
 *  Hub IP Notifier
 *
 *  Author: luke
 *  Date: 2014-01-28
 */
definition(
    name: "Hub IP Notifier",
    namespace: "smartthings",
    author: "SmartThings",
    description: "Listen for local IP changes when your hub registers.",
    category: "SmartThings Internal",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/MyApps/Cat-MyApps.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/MyApps/Cat-MyApps@2x.png"
)

preferences {
	page(name: "pageWithIp", title: "Hub IP Notifier", install: true)

}

def pageWithIp() {
	def currentIp = state.localip ?: 'unknown'
	def registerDate = state.lastRegister ?: null
	dynamicPage(name: "pageWithIp", title: "Hub IP Notifier", install: true, uninstall: true) {
		section("When Hub Comes Online") {
			input "hub", "hub", title: "Select a hub"
		}
		section("Last Registration Details") {
			if(hub && registerDate) {
				   paragraph """Your hub last registered with IP:
$currentIp
on:
$registerDate"""
			} else if (hub && !registerDate) {
				paragraph "Your hub has not (re)registered since you installed this app"
			} else {
				paragraph "Check back here after installing to see the current IP of your hub"
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
	subscribe(hub, "hubInfo", registrationHandler, [filterEvents: false])
}

def registrationHandler(evt) {
	def hubInfo = evt.description.split(',').inject([:]) { map, token ->
		token.split(':').with { map[it[0].trim()] = it[1] }
		map
	}
	state.localip = hubInfo.localip
	state.lastRegister = new Date()
	sendNotificationEvent("${hub.name} registered in prod with IP: ${hubInfo.localip}")
}
