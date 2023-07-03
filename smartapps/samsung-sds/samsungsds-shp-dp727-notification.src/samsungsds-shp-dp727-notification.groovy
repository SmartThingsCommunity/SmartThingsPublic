/**
 *  Z-Wave Lock
 *
 *  Copyright 2018 Kunwoo Kim
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
    name: "SamsungSDS SHP-DP727 notification",
    namespace: "Samsung SDS",
    author: "Kunwoo Kim",
    description: "Get a push notification",
    category: "My Apps",
	iconUrl: "https://s3.amazonaws.com/smartapp-icons/samsung_sds/app_icon.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/samsung_sds/app_icon@2x.png",    
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/samsung_sds/app_icon@2x.png")


preferences {
	section("Which door?") {
        input "door", "capability.Lock", required: true,
              title: "Which door?",multiple: true
    }

    section( "Notifications" ) {
        input "sendPushMessage", "enum", title: "Send a push notification?", metadata:[values:["Yes","No"]], required:false
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
    subscribe(door, "alarm", alarmHandler)
}

def doorOpenHandler(evt) {
	log.debug "[doorOpenHandler] evt.value = ${evt.value}"
    log.debug "[doorOpenHandler] evt.descriptionText = ${evt.descriptionText}"
}

def doorCloseHandler(evt) {
	log.debug "[doorCloseHandler] evt.value = ${evt.value}"
    log.debug "[doorCloseHandler] evt.descriptionText = ${evt.descriptionText}"
}

def alarmHandler(evt) {
	log.debug "[alarmHandler] evt.value = ${evt.value}"
    log.debug "[alarmHandler] evt.descriptionText = ${evt.descriptionText}"
    log.debug "[alarmHandler] evt.type = ${evt.type}"
    def value = evt.value
    def type = evt.type
    def description = evt.descriptionText
    
    log.debug "[alarmHandler] sendPushMessage = ${sendPushMessage}"
    if (sendPushMessage != "No") {
    	if(value == "siren"){
        	def str = "[${app.label}] ${evt.descriptionText}"
            
        	if(type == "tamper"){
				str = "Intrusion attempt is detected"
            }
            else if(type == "pinCode"){
            	str = "Fail to authenticate pinCode 5times. Start [Lock Mode] for 3 minutes"
            }
            else if(type == "fire"){
            	str = description
            }
            
        	sendPush(str)
        	reset()
        }
    }
}

private send(msg) {
    if ( sendPushMessage != "No" ) {
        log.debug( "sending push message = ${msg}" )
        sendPush( msg )
    }
    log.debug msg
}

def reset() {
	log.debug "reset alarm state1"
    sendEvent(isStateChange: true, name: "alarm", value: "off", descriptionText: "clear alarm mode to secure state")

}