definition(
    name: "Auto Lock Door - No Contact",
    namespace: "techwithjake",
    author: "Tech With Jake",
    description: "Automatically locks a specific door after X minutes when unlocked.",
    category: "My Apps",
    iconUrl: "http://www.gharexpert.com/mid/4142010105208.jpg",
    iconX2Url: "http://www.gharexpert.com/mid/4142010105208.jpg"
)

preferences{
    section("Select the door lock:") {
        input "lock1", "capability.lock", required: true
    }
    section("Automatically lock the door when unlocked...") {
        input "minutesLater", "number", title: "Delay (in minutes):", required: true
    }
    section( "Notifications" ) {
        input("recipients", "contact", title: "Send notifications to", required: false) {
            input "phoneNumber", "phone", title: "Warn with text message (optional)", description: "Phone Number", required: false
        }
    }
}

def installed(){
    initialize()
}

def updated(){
    unsubscribe()
    unschedule()
    initialize()
}

def initialize(){
    log.debug "Settings: ${settings}"
    subscribe(lock1, "lock", doorHandler, [filterEvents: false])
}

def lockDoor(){
    log.debug "Locking the door."
    lock1.lock()
    if(location.contactBookEnabled) {
        if ( recipients ) {
            log.debug ( "Sending Push Notification..." )
            sendNotificationToContacts( "${lock1} locked after ${lock1} was unlocked for ${minutesLater} minutes!", recipients)
        }
    }
    if (phoneNumber) {
        log.debug("Sending text message...")
        sendSms( phoneNumber, "${lock1} locked after ${lock1} was unlocked for ${minutesLater} minutes!")
    }
}

def doorHandler(evt){
    if ((lock1.latestValue("lock") == "unlocked") && (evt.value == "unlocked")) { // If a person unlocks a locked door...
        //def delay = (minutesLater * 60) // runIn uses seconds
        runIn( (minutesLater * 60), lockDoor ) // ...schedule (in minutes) to lock.
    }
    else if ((lock1.latestValue("lock") == "locked") && (evt.value == "locked")) { // If a person manually locks it then...
        unschedule( lockDoor ) // ...we don't need to lock it later.
    }
}