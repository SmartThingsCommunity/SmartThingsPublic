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
 *  Notify Me When It Opens
 *
 *  Author: SmartThings
 */
definition(
    name: "Garage Door Watchdog",
    namespace: "com.inazaruk",
    author: "inazaruk",
    description: "Get a push message sent to your phone when an event happens with garage doors.",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Meta/window_contact.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Meta/window_contact@2x.png"
)

preferences {
	section("When the door opens or closes...") {
		input "theContact", "capability.contactSensor", title: "Which garage door?"
	}
}

def installed()
{
	log.debug("Application installed.")
    
	initialize()
}

def updated()
{
	log.debug("Application updated.")
    
    tearDown()
	initialize()
}

def tearDown() {
	log.debug("Tearing down...")
    
	unsubscribe()
    unschedule()
}

def initialize() {
	log.debug("Initializing...")
    
    subscribe(theContact, "contact", contactHandler)
    runEvery5Minutes(periodicCheck)
}

/**
 * Handles notifications about doors open/closed events.
 */
def contactHandler(evt) {
	log.debug("contactHandler(): name=$evt.value")
    
    def timeStampStr = notificationTimeStamp(evt.date)
	sendPush("Garage doors: $evt.value (${timeStampStr})")
}

/**
 * Periodically check garage doors status. In case we missed notification
 * or user ignored previous notification.
 */
def periodicCheck() {
	def currentState = theContact.currentState("contact")
    def timeDeltaMs = new Date().getTime() - currentState.date.getTime();
    def timeDeltaMin = (int) (timeDeltaMs / 1000 / 60);
    
    def timeStampStr = currentState.date.format("HH:mm", location.timeZone)
    def timeDeltaStr = "${timeDeltaMin} minutes"
    def notifTimeStampStr = notificationTimeStamp(new Date())
    
	if (currentState.value == "open") {
        def message = "Garage doors: STILL OPEN. For ${timeDeltaStr}, since ${timeStampStr}. (${notifTimeStampStr})"
        log.debug(message)
        if (timeDeltaMin > 1) {
      	  sendPush(message)
        }
    } else if (currentState.value == "closed") {
    	def message = "Garage doors: STILL CLOSED. For ${timeDeltaStr}, since ${timeStampStr}. (${notifTimeStampStr})"
        log.debug(message)
    }
}

def notificationTimeStamp(date) {
    return date.format("HH:mm:ss", location.timeZone)
}