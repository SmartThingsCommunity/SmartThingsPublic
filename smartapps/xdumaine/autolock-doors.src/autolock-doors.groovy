definition(
    name: "Autolock Doors",
    namespace: "xdumaine",
    author: "Xander Dumaine",
    description: "Automatically locks doors a specific door after X minutes when unlocked). Optionally disable auto locking when in a specific mode.",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/xdumaine-random-junk/padlock.png",
    iconX2Url: "https://s3.amazonaws.com/xdumaine-random-junk/padlock.png"
)

preferences{
    section("Select the door locks:") {
        input "locks", "capability.lock", required: true, multiple: true
    }
    section("Automatically lock the door when unlocked...") {
        input "minutesLater", "number", title: "Delay (in minutes):", required: true
    }
    section("Disable auto lock when...") {
    	input "modes", "mode", title: "Select mode(s) (optional)", multiple: true
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
    locks.each {
    	subscribe(it, "lock", doorHandler, [filterEvents: false])
        it.lock()
    }
}

def lockDoor(){
    log.debug "Locking the doors."
    locks.each { it.lock() }
    if (location.contactBookEnabled) {
        if (recipients) {
            log.debug ("Sending Push Notification...")
            sendNotificationToContacts( "Selected doors were locked after one was unlocked for ${minutesLater} minutes!", recipients)
        }
    }
    if (phoneNumber) {
        log.debug("Sending text message...")
        sendSms( phoneNumber, "Selected doors were locked after one was unlocked for ${minutesLater} minutes!")
    }
}

def doorHandler(evt){
    log.debug("Handling event: " + evt.value)
    if (modes.contains(location.mode)) {
    	log.debug("Not running because location is in disabled mode: " + location.currentMode)
    } else {
    	log.debug("Current mode: " + location.currentMode + " Disabled mode: " + modes)
        def unlocked = locks.find {it.latestValue("lock") == "unlocked" }
        if (unlocked && evt.value == "unlocked") {
            log.debug("Scheduling lock because ${unlocked} was unlocked")
            runIn( (minutesLater * 60), lockDoor ) // ...schedule (in minutes) to lock.
        }
        else if (!unlocked && evt.value == "locked") { // If a person manually locks it then...
            unschedule( lockDoor ) // ...we don't need to lock it later.
        }
    }
}