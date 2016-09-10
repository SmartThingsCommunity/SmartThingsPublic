
definition(
    name: "Door & Lock Manager",
    namespace: "airoscar",
    author: "Oscar Chen",
    description: "Manages door and lock behaviors. Send push notification or text message if the door is left open or if the lock is left unlocked for a preset amount of time; as well as automatically locking the lock with a preset delay after the door has been closed.",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/ModeMagic/bon-voyage.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/ModeMagic/bon-voyage%402x.png"
)

preferences {

	section("Monitor this door") {
		input "contact", "capability.contactSensor"
    }
    section ("Monitor this lock") {
        input "lock", "capability.lock"
	}
	section("And notify me if it's open for more than this many minutes (default: 3)") {
		input "openThreshold", "number", description: "Number of minutes", required: false
	}
    section("Delay between notifications (default 10 minutes") {
        input "frequency", "number", title: "Number of minutes", description: "", required: false
    }
    section("Automatically lock this door after this many minutes (default: 0.5)") {
    	input "lockDelay", "number", title: "Number of minutes", required: false
    }
	section("Via text message at this number (or via push notification if not specified") {
        input("recipients", "contact", title: "Send notifications to") {
            input "phone", "phone", title: "Phone number (optional)", required: false
        }
	}
}

def installed() {
	log.trace "installed()"
	subscribe()
}

def updated() {
	log.trace "updated()"
	unsubscribe()
	subscribe()
}

def subscribe() {
	subscribe(contact, "contact", doorHandler)
    subscribe(lock, "lock", lockHandler)
}

def lockHandler(evt)
{
	log.trace "LockHandler($evt.name: $evt.value)"
    def t0 = now()
    def delay = (openThreshold != null && openThreshold != "") ? openThreshold * 60 : 180 //unlocked notification delay
    runIn(delay, doorOpenTooLong, [overwrite: false])
    log.debug "scheduled doorOpenTooLong in ${now() - t0} msec"
}

def doorHandler(evt)
{
	log.trace "doorHandler($evt.name: $evt.value)"
	def t0 = now()
	def delay = (openThreshold != null && openThreshold != "") ? openThreshold * 60 : 180 //door open notification delay
    def delay2 = (lockDelay != null && lockDelay != "") ? lockDelay * 60 : 30 // auto lock delay
    runIn(delay2, autoLock, [overwrite: false])
	runIn(delay, doorOpenTooLong, [overwrite: false])
	log.debug "scheduled doorOpenTooLong in ${now() - t0} msec"
}

def autoLock() {
	def contactState = contact.currentState("contact")
    def lockState = lock.currentState("lock")
    def elapsed = now() - contactState.rawDateCreated.time
	def autoLockDelay = ((delay2 != null && delay2 != "") ? delay2 * 60000 : 60000) - 1000
	
    if (elapsed >= threshold) {
    	if (contactState.value == "closed" && lockState.value == "unlocked") {
    		lock.lock()
       	}
    }
}


def doorOpenTooLong() {
	def contactState = contact.currentState("contact")
    def lockState = lock.currentState("lock")
    def freq = (frequency != null && frequency != "") ? frequency * 60 : 600 //notification frequency
	
	if (contactState.value == "open") {
    
		def elapsed = now() - contactState.rawDateCreated.time
		def threshold = ((openThreshold != null && openThreshold != "") ? openThreshold * 60000 : 60000) - 1000
        
		if (elapsed >= threshold) {
			log.debug "contact has stayed open long enough since last check ($elapsed ms):  calling sendMessage()"
			sendMessage()
            runIn(freq, doorOpenTooLong, [overwrite: false])
		} else {
			log.debug "contact has not stayed open long enough since last check ($elapsed ms):  doing nothing"
		}
        
	} else if (lockState.value =="unlocked") {
    
    	def elapsed = now() - contactState.rawDateCreated.time
        def threshold = ((openThreshold != null && openThreshold != "") ? openThreshold * 60000 : 60000) - 1000
        
        if (elapsed >= threshold) {
        	log.debug "lock has stayed unlocked long enough since last check ($elapsed ms): calling sendMessage()"
            sendMessage()
            runIn(freq, doorOpenTooLong, [overwrite: false])
        } else {
        	log.debug "lock has not stayed unlocked long enough since last check ($elapsed ms): doing nothing"
        }
        
    } else {
		log.warn "doing nothing"
	}
}

void sendMessage()
{
	def minutes = (openThreshold != null && openThreshold != "") ? openThreshold : 3
	def msg = ""

    if (contact.currentState("contact").value == "open") {
    	msg = "${contact.displayName} has been left open for ${minutes} minutes."
    } else if (lock.currentState("lock").value == "unlocked") {
    	msg = "${contact.displayName} is closed but ${lock.displayName} has been left unlocked for ${minutes} minutes."
    } else {
    	msg = "No message"
    }

	log.info msg
    if (location.contactBookEnabled) {
        sendNotificationToContacts(msg, recipients)
    }
    else {
        if (phone) {
            sendSms phone, msg
        } else {
            sendPush msg
        }
    }
}