/*
 *  Enhanced Auto Lock Door
 *
 *  Author: Arnaud
 *  
 *  10/13/2015: Eric Maycock (erocm1231)
 *  - Created send method to consolidate code
 *  - Configured app to use new "contacts" functionality of SmartThings 2.0
 *  - Modified app so that you can disable the feature: "Automatically unlock the door when open...". This isn't necessary
 *    for all users.
 *	
 */


definition(
    name: "Enhanced Auto Lock Door",
    namespace: "Lock Auto Super Enhanced",
    author: "Arnaud",
    description: "Automatically locks a specific door after X minutes when closed  and unlocks it when open after X seconds.",
    category: "Safety & Security",
    iconUrl: "http://www.gharexpert.com/mid/4142010105208.jpg",
    iconX2Url: "http://www.gharexpert.com/mid/4142010105208.jpg"
)

preferences{
    page(name: "initialPage")
}

def initialPage() {
dynamicPage(name: "initialPage", title: "", install: true, uninstall: true) {
    section("Select the door lock:") {
        input "lock1", "capability.lock", required: true
    }
    section("Select the door contact sensor:") {
    	input "contact", "capability.contactSensor", required: true
    }   
    section("Automatically lock the door when closed...") {
        input "minutesLater", "number", title: "Delay (in minutes):", required: true
    }
	section("") {
        input "lever", "bool", title: "Is this a lever door?", submitOnChange: true
    }
	if (lever) {
            section("Automatically unlock the door when open...") {
                input "secondsLater", "number", title: "Delay (in seconds):", required: false
            }	
	}
    section("Send Notifications?") {
        input("recipients", "contact", title: "Send notifications to") {
            input "sendPushMessage", "enum", title: "Send a push notification?", metadata:[values:["Yes", "No"]], required: false, value:"No"
			input "phoneNumber", "phone", title: "Enter phone number to send text notification.", required: false
        }
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
    subscribe(lock1, "unlock", doorHandler, [filterEvents: false])  
    subscribe(contact, "contact.open", doorHandler)
	subscribe(contact, "contact.closed", doorHandler)
}

def lockDoor(){
    log.debug "Locking the door."
    lock1.lock()
    send("${lock1} locked after ${contact} was closed for ${minutesLater} minutes!")
}

def unlockDoor(){
    log.debug "Unlocking the door."
    lock1.unlock()
    send("${lock1} unlocked after ${contact} was opened for ${secondsLater} seconds!")
}

private send(message){
	log.debug("Send Notification Function")
	// check that contact book is enabled and recipients selected
	if (location.contactBookEnabled && recipients) {
    	log.debug ( "Sending notifications to selected contacts..." ) 
    	sendNotificationToContacts(message, recipients)
	} else if (sendPushMessage != "No") {
    	log.debug ( "Sending Push Notification..." ) 
    	sendPush( message )
	} else if (phoneNumber != "0") {
    	log.debug("Sending text message...")
		sendSms( phoneNumber, message )
	}
}

def doorHandler(evt){
    if ((contact.latestValue("contact") == "open") && (evt.value == "locked")) { // If the door is open and a person locks the door then...  
        def delay = (secondsLater) // runIn uses seconds
		if (delay != "" && lever )
			runIn( delay, unlockDoor )   // ...schedule (in minutes) to unlock...  We don't want the door to be closed while the lock is engaged. 
    }
    else if ((contact.latestValue("contact") == "open") && (evt.value == "unlocked")) { // If the door is open and a person unlocks it then...
        unschedule( unlockDoor ) // ...we don't need to unlock it later.
	}
    else if ((contact.latestValue("contact") == "closed") && (evt.value == "locked")) { // If the door is closed and a person manually locks it then...
        unschedule( lockDoor ) // ...we don't need to lock it later.
    }   
    else if ((contact.latestValue("contact") == "closed") && (evt.value == "unlocked")) { // If the door is closed and a person unlocks it then...
        def delay = (minutesLater * 60) // runIn uses seconds
        runIn( delay, lockDoor ) // ...schedule (in minutes) to lock.
    }
    else if ((lock1.latestValue("lock") == "unlocked") && (evt.value == "open")) { // If a person opens an unlocked door...
        unschedule( lockDoor ) // ...we don't need to lock it later.
    }
    else if ((lock1.latestValue("lock") == "unlocked") && (evt.value == "closed")) { // If a person closes an unlocked door...
        def delay = (minutesLater * 60) // runIn uses seconds
        runIn( delay, lockDoor ) // ...schedule (in minutes) to lock.
	}
    else { //Opening or Closing door when locked (in case you have a handle lock)
    	if (lever) {
            log.debug "Unlocking the door."
            lock1.unlock()
            send("${lock1} unlocked after ${contact} was opened or closed when ${lock1} was locked!")
		}
	}
}
