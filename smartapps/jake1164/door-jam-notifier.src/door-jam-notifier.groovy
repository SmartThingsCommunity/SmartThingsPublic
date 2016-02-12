/**
 *  Door Jam Notifier
 *
 *  Copyright 2016 Jason Jackson
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
    name: "Door Jam Notifier",
    namespace: "jake1164",
    author: "Jason Jackson",
    description: "Notify via sms when a Schlage lock is jammed and not locked.",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
	section("Check for door jam...") {
		input "lock", "capability.lock", title: "Which Lock?", multiple: false
        input "recheck_delay", "number", title: "How long to wait before rechecking if the door has been locked? (seconds)", default: 180
	}
    section("Notify"){
    	input("recipients", "contact", title: "Send sms notification to"){
        	input "phone1", "phone", title: "Phone Number", required: true
            input "phone2", "phone", title: "Phone Number (optional)", required: false
            input "phone3", "phone", title: "Phone Number (optional)", required: false
            input "phone4", "phone", title: "Phone Number (optional)", required: false
            input "phone5", "phone", title: "Phone Number (optional)", required: false
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
	subscribe(lock, "lock", lock_event, [filterEvents: false])
    subscribe(lock, "unlock", lock_event, [filterEvents: false])
}

def lock_event(evt){
	log.debug "Lock state has changed to ${evt.value}"
    state.lock_state = evt.value
	if(evt.value == "unknown"){
    	log.debug "Jammed door detected, wait $recheck_delay seconds and check if still jammed"
        state.retry = 0
		runIn(recheck_delay, check_lock)
	}
}

def check_lock(){
	log.debug "Testing to see if the lock has changed from unknown, last known state: ${state.lock_state}"
	if(state.lock_state == "unknown"){
    	if(state.retry == 0){
        	log.debug "Testing to see if we can just lock the door and move on with life"
            lock.lock()
            state.retry = 1
            runIn(10, check_lock)
        } else {
        	log.debug "Notifying users via sms"
            send_message(phone1)
            send_message(phone2)
            send_message(phone3)
            send_message(phone4)
            send_message(phone5)
        }    
    }
}

def send_message(phone){
	def message = "The front door is jammed and might not be locked!"
	if(phone){
    	log.debug "Sending sms message to $phone"
    	sendSms(phone, message)
    }
}