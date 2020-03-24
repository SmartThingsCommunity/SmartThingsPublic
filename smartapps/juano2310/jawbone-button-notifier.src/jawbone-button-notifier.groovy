/**
 *  Jawbone Panic Button
 *
 *  Copyright 2014 Juan Risso
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

// Automatically generated. Make future change here.
definition(
    name: "Jawbone Button Notifier",
    namespace: "juano2310",
    author: "Juan Risso",
    category: "SmartThings Labs",
    description: "Send push notifications or text messages with your Jawbone Up when you hold the button.",
	iconUrl: "https://s3.amazonaws.com/smartapp-icons/Partner/jawbone-up.png",
	iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Partner/jawbone-up@2x.png",
)

preferences {
	section("Use this Jawbone as a notification button and...") {
		input "jawbone", "device.jawboneUser", multiple: true
	}
    section("Send a message when you press and hold the button...") {
    	input "warnMessage", "text", title: "Warning Message"
    }
    section("Or text message to these numbers (optional)") {
    	input ("phone1", "contact", required: false) {
        	input "phone1", "phone", required: false
        }
        input ("phone2", "contact", required: false) {
        	input "phone2", "phone", required: false
        }
        input ("phone3", "contact", required: false) {
        	input "phone3", "phone", required: false
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
	subscribe(jawbone, "sleeping", sendit)
}

def sendit(evt) {
	log.debug "$evt.value: $evt"
	sendMessage()
}

def sendMessage() {
	log.debug "Sending Message"
	def msg = warnMessage
    log.info msg
    if (phone1) {
        sendSms phone1, msg
    }
    if (phone2) {
        sendSms phone2, msg
    }
    if (phone3) {
        sendSms phone3, msg
    }
    if (!phone1 && !phone2 && !phone3) {
    	sendPush msg
    }
}
