/**
 *  Tamper Warning
 *
 *  Copyright 2016 Jamie Furtner
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
    name: "Tamper Warning",
    namespace: "jfurtner",
    author: "Jamie Furtner",
    description: "Warn when device tampered with",
    //category: "Custom",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
	section("Select sensors to monitor") {
		input "sensors", "capability.tamperAlert", multiple:true
	}
    section('Send push notification') {
    	input 'sendPush', 'bool', required:false, title: 'Send push notification?'
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
    subscribe(sensors, 'tamper', sensorTamperedHandler)
}

def sensorTamperedHandler(evt) {
    if (sendPush) {
    	sendPush("Sensor ${evt.displayName} has been tampered with!")
    }
}