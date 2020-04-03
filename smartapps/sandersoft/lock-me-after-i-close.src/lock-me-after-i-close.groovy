/**
 *  Lock Me After I Close
 *
 *  Copyright 2015 Kurt E. Sanders
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
 
import groovy.time.TimeCategory

definition(
    name: "Lock Me After I Close",
    namespace: "SanderSoft",
    author: "Kurt Sanders",
    description: "Verify status of a door lock and a door contact, and ensure that we are safe & secure",
    category: "Safety & Security",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")

preferences {
	section("When the door closes..."){
		input "contact", "capability.contactSensor", title: "Where?"
	}
	section("Lock which lock...") {
		input "lock","capability.lock", title: "Lock?"
	}
	section("Notify me...") {
	        input("recipients", "contact", title: "Send notifications to") {
            input "pushNotification", "bool", title: "Push notification", required: false, defaultValue: "true"
            input "phone", "phone", title: "Phone number", required: false
        }
	}
    section("Automatically lock the door after close...") {
    	input "secsToDelay", "number", title: "Delay (in seconds):", required: true
    }
    section("Unlock it if the lock is manually engaged while the door is open...") {
    	input "secsToDelay2", "number", title: "Delay (in seconds):", required: true
    }
}
def initialize() {
    log.debug "Auto Lock Door initialized."
	subscribe(contact, "contact", doorHandler, [filterEvents: false])
    subscribe(lock, "lock", doorHandler, [filterEvents: false])
    subscribe(lock, "unlock", doorHandler, [filterEvents: false])
    log.debug "Creating the routineDoorLockCheck schedule"
    runEvery30Minutes(routineDoorLockCheck)
}
def installed() {
    initialize()
}
def updated() {
    unsubscribe()
    unschedule()
    initialize()
}

def routineDoorLockCheck()
{
    def currentFormatTime = new Date().format("EEE, MMM d, hh:mm a", location.timeZone)
    log.debug "routineDoorLockCheck: Starting Routine Door/Lock Check at ${currentFormatTime}"
	def ls = lock.latestValue("lock")
    def cs = contact.latestValue("contact")    
    def isLockLocked =    (ls == "locked" || ls == "unknown")   
    def isContactClosed = (cs == "closed" || cs == "unknown")
    log.debug "routineDoorLockCheck: lock status = ${ls}, Door Status = ${cs}"
    
	if ((isContactClosed) && (!isLockLocked)) { // If the door is closed and unlocked then...
		def msg =  "Warning: Your Front door has been closed but NOT locked.  I'm going to try to lock the door!"
        pushNotificationHandler(msg)
		lockDoor()
        runIn(15,lockVerify, options)
    }
    log.debug "Re-Scheduling routineDoorLockCheck for 30 mins from ${currentFormatTime}"
}

def lockVerify()
{
    def currentFormatTime = new Date().format("EEE, MMM d, hh:mm a", location.timeZone)
    log.debug "lockVerify: Starting Routine Door/Lock Check at ${currentFormatTime}"
	def ls = lock.latestValue("lock")
    def cs = contact.latestValue("contact")    
    def isLockLocked =    (ls == "locked" || ls == "unknown")   
    def isContactClosed = (cs == "closed" || cs == "unknown")
    def msg = "lockVerify: lock status = ${ls}, Door Status = ${cs}"
    pushNotificationHandler(msg)
    
	if ((isContactClosed) && (!isLockLocked)) { // If the door is closed and unlocked then...
		msg =  "Warning: Your Front door has been closed but NOT locked.  I'm going to try to lock the door!"
        pushNotificationHandler(msg)
		lockDoor()
        runIn(15,LockVerify, options)
    }
}


def lockDoor()
{
    log.debug "Sending Lock CMD to the Door"
	lock.lock()
}
def unlockDoor()
{
    log.debug "Sending UnLock CMD to the Door"
    lock.unlock()
}
def doorHandler(evt)
{
	def ls = lock.latestValue("lock")
    def cs = contact.latestValue("contact")    
    def isLockLocked =    (ls == "locked" || ls == "unknown")   
    def isContactClosed = (cs == "closed" || cs == "unknown")
        
    def delayDate = new Date()
//    log.debug "delayDate = $delayDate"
    
    use( TimeCategory ) {
    delayDate = delayDate + secsToDelay.seconds
	}
//    log.debug "delayDate = $delayDate"

	def delayDate2 = new Date()
//    log.debug "delayDate2 = $delayDate2"
    
    use( TimeCategory ) {
    delayDate2 = delayDate2 + secsToDelay2.seconds
	}
//    log.debug "delayDate2 = $delayDate2"
    
    log.debug "The source of this event is: ${evt.source}"
    log.debug "Event: Name ${evt.name} is ${evt.value} value and created at: ${evt.date.format("EEE, MMM d, hh:mm a", location.timeZone)}"
    log.debug "ContactStatus: $cs LockStatus: $ls isLockedLocked: $isLockLocked isContactClosed: $isContactClosed"
// 	Handle the Various Events and Door/Lock Status
    
// 	Door has Been OPENED (Normal Door Opening Mode)
    if (evt.value == "open") { // If a person opens an unlocked door...
        log.debug "Cancel the current task. Door is unlocked and somebody just opened it!"
        contactOpenHandler()   // update the door open timestamp
        unschedule( lockDoor ) // ...we don't need to lock it later.
        log.debug "Lock status is $lock.currentLock"
        if (lock.currentLock == "locked") {
            if (secsToDelay2 == 0) {
	            unlockDoor()
            }
        else {
            runOnce(delayDate2,unlockDoor)
            }
        }
    }
// 	Door is currently OPEN, Lock was changed from UNLOCKED -> LOCKED (MISTAKE)
    else if ((!isContactClosed) && (evt.value == "locked")) {   
        log.debug "Door is in open status and somebody just locked the lock.  Mistake, unlocking the lock after !"
//        runIn( secsToDelay2, unlockDoor )   // ...schedule (in seconds) to unlock...  We don't want the door to be closed while the lock is engaged. 
        if (secsToDelay2 == 0) {
            unlockDoor()
            }
        else {
            runOnce(delayDate,unlockDoor)
            }
    }
    // 	Door is currently OPEN, Lock was changed from LOCKED -> UNLOCKED (MANUALLY UNLOCKED THE LOCK)
    else if ((!isContactClosed) && (evt.value == "unlocked")) { // If the door is open and a person unlocks it then...
        log.debug "Cancel the current task. Door is open and somebody just manually unlocked the lock!"
        unschedule( unlockDoor ) // ...we don't need to unlock it later.
    }
    
// 	Door is currently CLOSED, Lock was changed from UNLOCKED to LOCKED (NORMAL CLOSING & LOCKING MODE)
	else if ((isContactClosed) && (evt.value == "locked")) { // If the door is closed and a person manually locks it then...
        log.debug "Cancel the current task. Door is closed and somebody just locked it!"
        unschedule( lockDoor ) // ...we don't need to lock it later.
    }
// 	Door is currently CLOSED, Lock was changed from LOCKED to UNLOCKED
    else if ((isContactClosed) && (evt.value == "unlocked")) { // If the door is closed and a person unlocks it then...
        log.debug "Door is closed and somebody just unlocked it.  Locking the door!"
        log.debug "Re-arming lock in (${secsToDelay}s)."
//        runIn( secsToDelay, lockDoor ) // ...schedule (in seconds) to lock.
// Temporary Fix for problems with RunIn Routine
	if (secsToDelay == 0) {
    	lockDoor()
        }
    else {
	    runOnce(delayDate,lockDoor)
        }
    }
// 	Door has been CLOSED and Lock is UNLOCKED (Normal Close Door Mode)
	else if (!isLockLocked && (evt.value == "closed")) { // If a person closes an unlocked door...
        log.debug "Door is unlocked and somebody just closed it.  Locking the door!"
        contactCloseHandler()
        log.debug "Re-arming lock in (${secsToDelay}s)."
//        runIn( secsToDelay, lockDoor ) // ...schedule (in seconds) to lock.
// Temporary Fix for problems with RunIn Routine
		if (secsToDelay == 0) {
    		lockDoor()
        	}
    	else {
	    	runOnce(delayDate,lockDoor)
        	}
    	}
    else {
    // Unexpected Door and/or Lock state(s)
		def msg = "Ohh.. no!!.. Unknown Status Conflicts"
//        pushNotificationHandler(msg)
        msg = "Event Debug: Name ${evt.name} is ${evt.value} value"
//        pushNotificationHandler(msg)
        // Reset Any Orphaned Door Scheduled Events
		unschedule( lockDoor )
        unschedule( unlockDoor )
		updateLastRunDT()
    }
}

def contactCloseHandler() { //Specified contact has been event closed

    def currentFormatTime = new Date().format("EEE, MMM d, hh:mm a", location.timeZone)

    def today = new Date()
    def currentTime = now()
    def int DoorOpenTime = (currentTime - state.OpenStartDT)/1000
    def msg = "The Front Door has been closed and locked, it was opened at ${currentFormatTime} for ${timeConversion(DoorOpenTime)}"
	log.debug "${msg}"
	pushNotificationHandler(msg)
}

def contactOpenHandler() { //Specified contact has been event opened
    def currentFormatTime = new Date().format("EEE, MMM d, hh:mm a", location.timeZone)
    def today = new Date()
	updateLastRunDT()
	def msg = "Your front door was opened on ${currentFormatTime}"
    log.debug "${msg}"
	pushNotificationHandler(msg)
}

def updateLastRunDT() {
	log.debug "Updating OpenStartDT to ${now()}"
	state.OpenStartDT = now()
}

private static String timeConversion(int totalSeconds) {

    final int MINUTES_IN_AN_HOUR = 60;
    final int SECONDS_IN_A_MINUTE = 60;
	def HMS = "";

    int seconds = totalSeconds % SECONDS_IN_A_MINUTE;
    int totalMinutes = totalSeconds / SECONDS_IN_A_MINUTE;
    int minutes = totalMinutes % MINUTES_IN_AN_HOUR;
    int hours = totalMinutes / MINUTES_IN_AN_HOUR;
    if (hours > 0) {
	    return hours + " hrs " + minutes + " mins " + seconds + " secs";
    }
    else if (minutes>0) {
        return minutes + " mins " + seconds + " secs";
	}
    else {
	    return seconds + " secs";
    }
}

def pushNotificationHandler(msgString) {
    //Send out Notifications
	log.debug "$msgString"
    if (pushNotification) {
        sendPush("${msgString}")
    }
            if (phone != null && phone != "") {
        sendSms(phone,"${msgString}")
    }
}