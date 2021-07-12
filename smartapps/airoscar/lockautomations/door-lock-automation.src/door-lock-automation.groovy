/**
 *  Door & Lock Manager
 * 
 *  Copyright 2016 Oscar Chen
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
    name: "Door Lock Automation",
    namespace: "airoscar/lockautomations",
    parent: "airoscar/doorlockmanagerparent:Door & Lock Manager",
    author: "Oscar Chen",
    description: "Manages door and lock behaviors. Send push notification or text message if the door is left open or if the lock is left unlocked for a preset amount of time; as well as automatically locking the lock with a preset delay after the door has been closed.",
    category: "Convenience",
    iconUrl: "http://cdn.device-icons.smartthings.com/Home/home3-icn.png",
    iconX2Url: "http://cdn.device-icons.smartthings.com/Home/home3-icn@2x.png",
    iconX3Url: "http://cdn.device-icons.smartthings.com/Home/home3-icn@2x.png"
)

preferences {

	section("Monitor this door") {
		input "contact", "capability.contactSensor", required: false
    }
    section ("Monitor this lock") {
        input "lock", "capability.lock", required: true
	}
	section("And notify me if it's open or unlocked for more than this many minutes") {
		input "openThreshold", "decimal", description: "Number of minutes", defaultValue: 3, required: false
	}
    section("Delay between notifications") {
        input "frequency", "number", title: "Number of minutes", description: "", defaultValue: 10, required: false
    }
    section("Automatically lock this door after the door has been closed for this many minutes") {
    	input "lockDelay", "decimal", title: "Number of minutes", defaultValue: 0.05, required: false
        input "doorCloseTrigger", "bool", title: "Enable?", defaultValue: true, required: true
    }
    section("Automatically lock this door after the lock has been unlocked for this many minutes") {
    	input "lockDelay2", "decimal", title: "Number of minutes", defaultValue: 0.5, required: false
        input "lockLockedTrigger", "bool", title: "Enable?", defaultValue: true, required: true
    }
	section("Via text message at this number (or via push notification if not specified") {
        input("recipients", "contact", title: "Send notifications to") {
            input "phone", "phone", title: "Phone number (optional)", required: false
        }
	}
    section ("Enable notification") {
    	input("enableNotification", "bool", title: "Turn on/off?", defaultValue: true, required: false)
    }
}

def installed() {
	state.lastMsgTime = now ()
	log.trace "installed()"
	subscribe()
}

def updated() {
	log.trace "updated()"
	unsubscribe()
	subscribe()
}

def subscribe() {
	if (doorCloseTrigger) {
		subscribe(contact, "contact.closed", doorHandler)
    }
    if (lockLockedTrigger) {
    	subscribe(lock, "lock.unlocked", lockHandler)
    }
}


def lockHandler(evt)
{
	log.trace "LockHandler($evt.name: $evt.value)"
    def t0 = now()
    def notidelay = (openThreshold != null && openThreshold != "") ? openThreshold * 60 : 180 //unlocked notification delay
    def lockdelay2 = (lockDelay2 != null && lockDelay2 != "") ? lockDelay2 * 60 : 30 // auto lock delay after the lock was unlocked
    runIn(lockdelay2, autoLockUnlocked, [overwrite: true])
    log.debug "lockHandler: scheduled autoLockUnlocked in ${lockdelay2} sec"
    runIn(notidelay, doorOpenTooLong, [overwrite: true])
    log.debug "lockHandler: scheduled doorOpenTooLong in ${notidelay} sec"
}

def doorHandler(evt)
{
	log.trace "doorHandler($evt.name: $evt.value)"
	def t0 = now()
	def notidelay = (openThreshold != null && openThreshold != "") ? openThreshold * 60 : 180 //door open notification delay
    def lockdelay1 = (lockDelay != null && lockDelay != "") ? lockDelay * 60 : 6 // auto lock delay after the door was closed
    runIn(lockdelay1, autoLockDoorClose, [overwrite: true])
    log.debug "doorHandler: scheduled autoLockDoorClose in ${lockdelay1} sec"
	runIn(notidelay, doorOpenTooLong, [overwrite: true])
	log.debug "doorHandler: scheduled doorOpenTooLong in ${notidelay} sec"
}

def autoLockDoorClose() {
	def contactState = contact.currentState("contact")
    def lockState = lock.currentState("lock")
    def elapsed = now() - contactState.rawDateCreated.time
	def autoLockDelay = ((lockDelay != null && lockDelay != "") ? lockDelay * 60000 : 5000) - 1000
	
    if (elapsed >= autoLockDelay) {
    	if (contactState.value == "closed") {
    		lock.lock()
            log.debug "Sent lock command triggered by contact close."
            runIn(1, redundantLock, [overwrite: false])
       	} else {
        	log.debug "autoLockDoorClose: No action, either door is open or lock is already locked."
            }
    } else {
    	runIn((autoLockDelay - elapsed)/1000 + 1, autoLockDoorClose, [overwrite: true])
        log.debug "autoLockDoorClose: scheduled to check again in ${(autoLockDelay - elapsed)/1000 + 1} seconds."
    }
}

def autoLockUnlocked() {
	def contactState = contact.currentState("contact")
    def lockState = lock.currentState("lock")
    def elapsed = now() - contactState.rawDateCreated.time
	def autoLockDelay = ((lockDelay2 != null && lockDelay2 != "") ? lockDelay2 * 60000 : 30000) - 1000
	
    if (elapsed >= autoLockDelay) {
    	if (contactState.value == "closed" && lockState.value == "unlocked") {
    		lock.lock()
            log.debug "Sent lock command triggered by lock unlocking."
            runIn(1, redundantLock, [overwrite: false])
       	} else {
        	log.debug "autoLockUnlocked: No action, either door is open or lock is already locked."
        }
    } else {
    	runIn((autoLockDelay - elapsed)/1000 + 1, autoLockUnlocked, [overwrite: true])
        log.debug "autoLockUnlocked: scheduled to check again in ${(autoLockDelay - elapsed)/1000 + 1} seconds."
    }
}

def redundantLock() {
	if (lock.currentState("lock").value == "unlocked") {
    	lock.lock()
    }
}

def doorOpenTooLong() {
	def contactState = contact.currentState("contact")
    def lockState = lock.currentState("lock")
    def freq = (frequency != null && frequency != "") ? frequency * 60 : 600 //notification frequency
    def elapsed
	def threshold = ((openThreshold != null && openThreshold != "") ? openThreshold * 60000 : 60000) - 1000
    def t0 = now()
	
	if (contactState.value == "open") {
    	elapsed = t0 - contactState.rawDateCreated.time
		if (elapsed >= threshold) {
			log.debug "contact has stayed open long enough since last check ($elapsed ms):  calling sendMessage()"
			sendMessage()
            runIn(freq, doorOpenTooLong, [overwrite: true])
		} else {
			log.debug "contact has not stayed open long enough since last check ($elapsed ms):  doing nothing"
		}
        
	} else if (lockState.value =="unlocked") {
        elapsed = t0 - lockState.rawDateCreated.time
        if (elapsed >= threshold) {
        	log.debug "lock has stayed unlocked long enough since last check ($elapsed ms): calling sendMessage()"
            sendMessage()
            runIn(freq, doorOpenTooLong, [overwrite: true])
        } else {
        	log.debug "lock has not stayed unlocked long enough since last check ($elapsed ms): doing nothing"
        }
        
    } else {
		log.debug "doorOpenTooLong: contact already closed. No action."
	}
}

void sendMessage()
{
	def lapsed = now() - state.lastMsgTime
    def threshold = ((openThreshold != null && openThreshold != "") ? openThreshold * 60000 : 60000) - 1000
	def minutes = (openThreshold != null && openThreshold != "") ? openThreshold : 3
	def msg = ""
    def enableMsg = (enableNotification != null && enableNotification != "") ? enableNotification: true

    if (contact.currentState("contact").value == "open") {
    	msg = "${contact.displayName} has been left open for ${minutes} minutes."
    } else if (lock.currentState("lock").value == "unlocked") {
    	msg = "${contact.displayName} is closed but ${lock.displayName} has been left unlocked for ${minutes} minutes."
    } else {
    	msg = "Please check ${contact.displayName} and ${lock.displayName}."
    }
	 
     //if notification is enabled
    if (enableMsg) {
    	//if enough time has lapsed since last notification
    	if (lapsed > threshold ) {
        	state.lastMsgTime = now()
            if (location.contactBookEnabled) {
                sendNotificationToContacts(msg, recipients)
            }
            else {
                if (phone) {
                    sendSms phone, msg
                } else {
                    sendPush msg
                    log.debug msg
                }
            }
         } else {
         	log.debug "Not enough time has lapsed since last notification"
         }
    }
 	
    
}