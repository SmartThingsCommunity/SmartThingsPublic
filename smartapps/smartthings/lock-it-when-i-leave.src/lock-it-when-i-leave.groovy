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
 *  Lock It When I Leave
 *
 *  Author: SmartThings
 *  Date: 2013-02-11
 */

definition(
    name: "Lock It When I Leave",
    namespace: "smartthings",
    author: "SmartThings",
    description: "Locks a deadbolt or lever lock when a SmartSense Presence tag or smartphone leaves a location.",
    category: "Safety & Security",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience%402x.png",
    oauth: true
)

preferences {
	section("When I leave...") {
		input "presence1", "capability.presenceSensor", title: "Who?", multiple: true
	}
	section("Lock the lock...") {
		input "lock1","capability.lock", multiple: true
		input "unlock", "enum", title: "Unlock when presence is detected?", options: ["Yes","No"]
        input("recipients", "contact", title: "Send notifications to") {
            input "spam", "enum", title: "Send Me Notifications?", options: ["Yes", "No"]
        }
	}
}

def installed()
{
	subscribe(presence1, "presence", presence)
}

def updated()
{
	unsubscribe()
	subscribe(presence1, "presence", presence)
}

def presence(evt)
{
	if (evt.value == "present") {
		if (unlock == "Yes") {
			def anyLocked = lock1.count{it.currentLock == "unlocked"} != lock1.size()
			if (anyLocked) {
				sendMessage("Doors unlocked at arrival of $evt.linkText")
			}
			lock1.unlock()
		}
	}
	else {
		def nobodyHome = presence1.find{it.currentPresence == "present"} == null
		if (nobodyHome) {
			def anyUnlocked = lock1.count{it.currentLock == "locked"} != lock1.size()
			if (anyUnlocked) {
				sendMessage("Doors locked after everyone departed")
			}
			lock1.lock()
		}
	}
}

def sendMessage(msg) {

    if (location.contactBookEnabled) {
        sendNotificationToContacts(msg, recipients)
    }
    else {
        if (spam == "Yes") {
            sendPush msg
        }
    }
}
