/**
 *  Copyright 2015 Jody Albritton
 *  Based on the Notify me When SmartApp
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 *  Motion When Closed
 *
 *  Author: Jody Albritton
 *  Date: 2015-07-30
 *  Version: 1.0
 * Change Log:
 * Added Credit: Code base from the Notify me When SmartApp. 
 */
 
 
definition(
    name: "Motion in an Enclosed Space",
    namespace: "jodyalbritton",
    author: "Jody Albritton",
    description: "Get a push notification or text message when motion occurs in a closed off area.",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Meta/window_contact.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Meta/window_contact@2x.png"
)

preferences {
	section("When these doors are closed"){
		input "contacts", "capability.contactSensor", title: "Closed Doors", required: true, multiple: true
	}
    section("And this motion sensor is triggered"){
        input "motions", "capability.motionSensor", title: "Motion Here", required: true, multiple: true
    }
	section("Send this message (optional, sends standard status message if not specified)"){
		input "messageText", "text", title: "Message Text", required: false
	}
	section("Via a push notification and/or an SMS message"){
        input("recipients", "contact", title: "Send notifications to") {
            input "phone", "phone", title: "Phone Number (for SMS, optional)", required: false
            input "pushAndPhone", "enum", title: "Both Push and SMS?", required: false, options: ["Yes", "No"]
        }
	}
	section("Minimum time between messages (optional, defaults to every message)") {
		input "frequency", "decimal", title: "Minutes", required: false
	}
}

def installed() {
	log.debug "Installed with settings: ${settings}"
	subscribeToEvents()
}

def updated() {
	log.debug "Updated with settings: ${settings}"
	unsubscribe()
	subscribeToEvents()
}

def subscribeToEvents() {
	subscribe(motions, "motion.active", eventHandler)
}

def eventHandler(evt) {
	log.debug "Notify got evt ${evt}"
    def areClosed = doorsClosed()
    log.debug areClosed
    if (areClosed == true){
        if (frequency) {
            def lastTime = state[evt.deviceId]
            if (lastTime == null || now() - lastTime >= frequency * 60000) {
                sendMessage(evt)
            }
        }
        else {
            sendMessage(evt)
        }
    }
}



private sendMessage(evt) {
	def msg = messageText ?: defaultText(evt)
	log.debug "$evt.name:$evt.value, pushAndPhone:$pushAndPhone, '$msg'"

    if (location.contactBookEnabled) {
        sendNotificationToContacts(msg, recipients)
    }
    else {

        if (!phone || pushAndPhone != "No") {
            log.debug "sending push"
            sendPush(msg)
        }
        if (phone) {
            log.debug "sending SMS"
            sendSms(phone, msg)
        }
    }
	if (frequency) {
		state[evt.deviceId] = now()
	}
}

def doorsClosed(){
    def closed = false
		contacts.each { contact ->
        	def isClosed = contact.currentValue('contact')
        	if (isClosed == "closed" ) {
        		closed = true
             } else {
             	closed = false
             }
            }
    return closed
}

private defaultText(evt) {

		evt.descriptionText + "in a closed area"
}