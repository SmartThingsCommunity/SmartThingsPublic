
definition(
    name: "Lock Manager",
    namespace: "airoscar",
    author: "Oscar Chen",
    description: "Manages door locks.",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/ModeMagic/bon-voyage.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/ModeMagic/bon-voyage%402x.png"
)

preferences {

	section("Monitor this door or window") {
		input "door", "capability.contactSensor"
        input "lock", "capability.lock"
	}
	section("And notify me if it's open for more than this many minutes (default 3)") {
		input "openThreshold", "number", description: "Number of minutes", required: false
	}
    section("Delay between notifications (default 10 minutes") {
        input "frequency", "number", title: "Number of minutes", description: "", required: false
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
	subscribe(door, "contact.open", doorOpen)
	subscribe(door, "contact.closed", doorClosed)
    subscribe(lock, "lock", lockHandler)
}

def doorOpen(evt)
{
	log.trace "doorOpen($evt.name: $evt.value)"
	def t0 = now()
	def delay = (openThreshold != null && openThreshold != "") ? openThreshold * 60 : 180
	runIn(delay, doorOpenTooLong, [overwrite: false])
	log.debug "scheduled doorOpenTooLong in ${now() - t0} msec"
}

def doorClosed(evt)
{
	log.trace "doorClosed($evt.name: $evt.value)"
}

def doorOpenTooLong() {
	def contactState = door.currentState("door")
    def freq = (frequency != null && frequency != "") ? frequency * 60 : 600

	if (contactState.value == "open") {
		def elapsed = now() - contactState.rawDateCreated.time
		def threshold = ((openThreshold != null && openThreshold != "") ? openThreshold * 60000 : 60000) - 1000
		if (elapsed >= threshold) {
			log.debug "Contact has stayed open long enough since last check ($elapsed ms):  calling sendMessage()"
			sendMessage()
            runIn(freq, doorOpenTooLong, [overwrite: false])
		} else {
			log.debug "Contact has not stayed open long enough since last check ($elapsed ms):  doing nothing"
		}
	} else {
		log.warn "doorOpenTooLong() called but contact is closed:  doing nothing"
	}
}

void sendMessage()
{
	def minutes = (openThreshold != null && openThreshold != "") ? openThreshold : 10
	def msg = "${contact.displayName} has been left open for ${minutes} minutes."
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