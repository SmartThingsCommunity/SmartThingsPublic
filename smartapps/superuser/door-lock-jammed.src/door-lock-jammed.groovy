preferences
{
    section("Check for jamms in lock...") {
        input "lock1", "capability.lock"
    }
    section("Via an SMS message"){
		input "phone", "phone", title: "Phone Number (for SMS, optional)", required: false
    }
}


def installed()
{
    initialize()
}


def updated()
{
    unsubscribe()
    initialize()
}


def initialize()
{
    log.debug "Settings: ${settings}"
    subscribe(lock1, "lock", doorHandler)
}

def doorHandler(evt)
{

    if (evt.value != "locked") {
    	if (evt.value != "unlocked") {
        	log.debug "Lock ${evt.displayName} is ${evt.value}."
            sendPush("Door lock is jammed, please check.")
        	if (phone) {
				sendSms(phone, textMessage ?: "Door lock is jammed, please check")
    		}
		}
	}
}