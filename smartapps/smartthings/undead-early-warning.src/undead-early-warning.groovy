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
 *  The simplest Undead Early Warning system that could possibly work. ;)
 *
 *  Author: SmartThings
 */
definition(
    name: "Undead Early Warning",
    namespace: "smartthings",
    author: "SmartThings",
    description: "Undead Early Warning",
    category: "Safety & Security",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/SafetyAndSecurity/App-UndeadEarlyWarning.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/SafetyAndSecurity/App-UndeadEarlyWarning@2x.png"
)

preferences {
	section("When the door opens...") {
		input "contacts", "capability.contactSensor", multiple: true, title: "Where could they come from?"
	}
	section("Turn on the lights!") {
		input "switches", "capability.switch", multiple: true
	}
}

def installed()
{
	subscribe(contacts, "contact.open", contactOpenHandler)
}

def updated()
{
	unsubscribe()
	subscribe(contacts, "contact.open", contactOpenHandler)
}

def contactOpenHandler(evt) {
	log.debug "$evt.value: $evt, $settings"
	log.trace "The Undead are coming! Turning on the lights: $switches"
	switches.on()
}
