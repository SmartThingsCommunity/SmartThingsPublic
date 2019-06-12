/**
 *  Door Jammed Notification
 *
 *  Copyright 2015 John Rucker
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
    name: "Door Jammed Notification",
    namespace: "JohnRucker",
    author: "John.Rucker@Solar-current.com",
    description: "Sends a SmartThings notification and text messages when your CoopBoss detects a door jam.",
    category: "My Apps",
    iconUrl: "http://coopboss.com/images/SmartThingsIcons/coopbossLogo.png",
    iconX2Url: "http://coopboss.com/images/SmartThingsIcons/coopbossLogo2x.png",
    iconX3Url: "http://coopboss.com/images/SmartThingsIcons/coopbossLogo3x.png")

preferences {
    section("When the door state changes") {
        paragraph "Send a SmartThings notification when the coop's door jammed and did not close."
		input "doorSensor", "capability.doorControl", title: "Select CoopBoss", required: true, multiple: false            
        input("recipients", "contact", title: "Recipients", description: "Send notifications to") {
        	input "phone", "phone", title: "Phone number?", required: true}
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
	subscribe(doorSensor, "doorState", coopDoorStateHandler)
}

def coopDoorStateHandler(evt) {
	if (evt.value == "jammed"){
        def msg = "WARNING ${doorSensor.displayName} door is jammed and did not close!"
        log.debug "WARNING ${doorSensor.displayName} door is jammed and did not close, texting $phone"

        if (location.contactBookEnabled) {
            sendNotificationToContacts(msg, recipients)
        }
        else {
            sendPush(msg)
            if (phone) {
                sendSms(phone, msg)
            }
        }
	}        
}