definition(
    name: "Re-Auto Lock Door",
    namespace: "kdorff",
    author: "kdorff",
    description: "Automatically locks the specified door N after closing or unlocking",
    category: "Safety & Security",
    iconUrl: "http://chittagongit.com/images/door-lock-icon/door-lock-icon-5.jpg",
    iconX2Url: "http://chittagongit.com/images/door-lock-icon/door-lock-icon-5.jpg",
    pausable: true
)


/**
 * This code is based on "Enhanced Auto Lock Door" from Arnaud
 * https://github.com/SmartThingsCommunity/SmartThingsPublic/tree/master/smartapps/lock-auto-super-enhanced
 * It has been simplified to assume the Open/Closed sensor is located physically
 * on the same door as the Lock. Additionally, this has no need for "Unlock"
 * so those features have been completely removed.
 */

preferences {
    page name: "mainPage", install: true, uninstall: true
}

def mainPage() {
    dynamicPage(name: "mainPage") {
        section("Select the door lock:") {
            input "lock1", "capability.lock", required: true
        }
        section("Select the door contact sensor:") {
            input "contact", "capability.contactSensor", required: true
        }
        section("Automatically lock the door when closed...") {
            input "secondsLater", "number", title: "Delay (in seconds):", required: true
        }
        if (location.contactBookEnabled || phoneNumber) {
            section("Notifications") {
                input("recipients", "contact", title: "Send notifications to", required: false) {
                    input "phoneNumber", "phone", title: "Warn with text message (optional)", description: "Phone Number", required: false
                }
            }
        }
        section([mobileOnly:true]) {
            label title: "Assign a name", required: false
            mode title: "Set for specific mode(s)"
        }
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
    subscribe(contact, "contact.open", doorHandler)
    subscribe(contact, "contact.closed", doorHandler)
}

def lockDoor() {
    log.debug "Locking the door."
    lock1.lock()
    if(location.contactBookEnabled) {
        if (recipients) {
            log.debug ("Sending Push Notification...") 
            sendNotificationToContacts("${lock1} locked after ${contact} was closed for ${secondsLater} seconds!", recipients)
        }
    }
    if (phoneNumber) {
        log.debug("Sending text message...")
        sendSms(phoneNumber, "${lock1} locked after ${contact} was closed for ${secondsLater} seconds!")
    }
}

def doorHandler(evt) {
    if (evt.value == "open") {
        // Door was just opened. No need to lock it (until it closes).
        unschedule(lockDoor)
    }   
    else if ((evt.value == "locked") && (contact.latestValue("contact") == "closed")) {
        // Lock was just locked and door is closed. No need to lock it.
        unschedule(lockDoor)
    }   
    else if ((evt.value == "unlocked") && (contact.latestValue("contact") == "closed")) {
        // Lock was just unlocked and door is closed. Lock it in secondsLater seconds.
        runIn(secondsLater), lockDoor)
    }
    else if ((evt.value == "closed") && (lock1.latestValue("lock") == "unlocked")) { // If a person closes an unlocked door...
        // Door was just closed but lock is unlocked. Lock it in secondsLater seconds.
        runIn(secondsLater, lockDoor)
    }
}
