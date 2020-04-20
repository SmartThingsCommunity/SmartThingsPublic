/**
 *  Turn Off Switch when Windows/Doors closed
 *
 *  Copyright 2017 Nick Mahon
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
    name: "Turn Off Switch",
    namespace: "Nick257b",
    author: "Nick Mahon",
    description: "Turn off a switch (e.g. a light), when all specified door/window contact sensors closed",
    category: "Safety & Security",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
    section("Select the switch to turn off") {
    	input "theSwitch", "capability.switch", title: "Which switch?", required: true, multiple: false
    }
	section("when these are ALL closed") {
		input "contactSensors", "capability.contactSensor", multiple: true
	}
    section("Send Push Notification?") {
		input "sendPush", "bool", required: false,
		title: "Send Push Notification when ALL closed?"
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
	subscribe(contactSensors, "contact.closed", contactClosedHandler)
}

def contactClosedHandler(evt)
{
	log.trace "evt.name: $evt.value, $evt.deviceId"
	if (evt.value == "closed") {
    	log.debug "isDoorsOpen()? ${isDoorsOpen()}"
        def device = contactSensors.find { it.id == evt.deviceId }
        
    	if (!isDoorsOpen()) {
        	def message = "${device.displayName} was closed, switching off light as all other selected contacts sensors also in a closed state."
            log.debug (message)
            theSwitch.off()
            
			if (sendPush) {
				sendPush(message)
			}
			
		}
	}
}


def isDoorsOpen() {
	log.debug "doorss: $contactSensors.currentContact"
	doors.findAll{it.currentContact == "open"}
}