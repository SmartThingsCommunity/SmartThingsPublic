definition(
    name: "Auto Lock Door Simple",
    namespace: "smartthings",
    author: "Santosh Nair (smartsanty@gmail.com)",
    description: "Automatically locks a door after X seconds when closed. Ensures the door lock is disengaged if the door is left open for Y seconds",
    category: "Safety & Security",
    iconUrl: "http://www.gharexpert.com/mid/4142010105208.jpg",
    iconX2Url: "http://www.gharexpert.com/mid/4142010105208.jpg",
    pausable: true
)

preferences{
    section("Select the door lock") {
            input "lock1", "capability.lock", required: true
        }
        section("Select the door contact sensor") {
            input "contact", "capability.contactSensor", required: true
        }
        section("Automatically lock the door when closed in") {
            input "lockSecondsLater", "number", title: "Delay in seconds (Minimum 5 sec):", required: true, range: "5..*"
        }
        section("Automatically disengage the lock if door is left open for") {
            input "unlockSecondsLater", "number", title: "Delay in seconds (Minimum 5 sec):", required: true, range: "5..*"
        }
}

def installed() {
    initialize()
}

def updated() {
    unsubscribe()
    unschedule()
    initialize()
}

def initialize() {
    log.debug "Settings: ${settings}"
    subscribe(lock1, "lock", doorHandler, [filterEvents: false])
    subscribe(lock1, "unlock", doorHandler, [filterEvents: false])  
    subscribe(contact, "contact.open", doorHandler)
    subscribe(contact, "contact.closed", doorHandler)
}

def lockDoor() {
    log.debug "Locking the door."
    lock1.lock()
}

def unlockDoor() {
    log.debug "Unlocking the door."
    lock1.unlock()
}

def doorHandler(evt) {
    if ((contact.latestValue("contact") == "open") && (evt.value == "locked")) { // If the door is open and a person locks the door then...  
        unschedule( lockDoor )					  // ... remove any previous lock door schedule
        runIn( unlockSecondsLater, unlockDoor )   // ...schedule (in seconds) to unlock...  We don't want the door to be closed while the lock is engaged. 
    }
    else if ((contact.latestValue("contact") == "open") && (evt.value == "unlocked")) { // If the door is open and a person unlocks it then...
        unschedule( unlockDoor ) // ...we don't need to unlock it later.
    }
    else if ((contact.latestValue("contact") == "closed") && (evt.value == "locked")) { // If the door is closed and a person manually locks it then...
        unschedule( lockDoor ) // ...we don't need to lock it later.
    }   
    else if ((contact.latestValue("contact") == "closed") && (evt.value == "unlocked")) { // If the door is closed and a person unlocks it then...
        unschedule( unlockDoor )			// ... remove any previous unlock door schedule
        runIn( lockSecondsLater, lockDoor ) // ...schedule (in seconds) to lock.
    }
    else if ((lock1.latestValue("lock") == "unlocked") && (evt.value == "open")) { // If a person opens an unlocked door...
        unschedule( lockDoor ) // ...we don't need to lock it later.
    }
    else if ((lock1.latestValue("lock") == "unlocked") && (evt.value == "closed")) { // If a person closes an unlocked door...
        unschedule( unlockDoor )			// ... remove any previous unlock door schedule
        runIn( lockSecondsLater, lockDoor ) // ...schedule (in seconds) to lock.
    }
    else { //Opening or Closing door when locked (in case you have a handle lock)
        unlockDoor()
    }
}