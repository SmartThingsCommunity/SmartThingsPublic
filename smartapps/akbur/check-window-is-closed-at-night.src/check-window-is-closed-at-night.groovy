definition(
    name: "Check window is closed at night",
    namespace: "akbur",
    author: "Akbur Ghafoor",
    description: "Checks if your window is closed at night - if not, sends an SMS alert.",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Meta/window_contact.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Meta/window_contact@2x.png"
)

preferences {
	section("When the window is open at night...") {
		input "contact1", "capability.contactSensor", title: "Where?"
	}
	section("Text me at...") {
        input("recipients", "contact", title: "Send notifications to") {
            input "phone1", "phone", title: "Phone number? (in interntional format, starting with +<country code>, e.g. +447739123456)"
        }
	}
    
    section("Time to check at night") {
        input "time1", "time"
	}
}

def installed()
{
	subscribe(contact1, "contact", contactHandler)
    schedule(time1, checkWindowsHandler)
}

def updated()
{
	unsubscribe()
	subscribe(contact, "contact", contactHandler)
    schedule(time1, checkWindowsHandler)
}

def checkWindowsHandler()
{
	if (state.windowsopen)
    {
    	sendSms(phone1, "WARNING: Your ${contact1.label ?: contact1.name} is OPEN.")
    }
}

def contactHandler(evt) {
    
    if("open" == evt.value)
    {
    	// contact is open, log it for the night check
    	log.debug "Contact is in ${evt.value} state"
        state.windowsopen = true;
    }
  	if("closed" == evt.value)
    {
    	// contact was closed, log it for the night check
    	log.debug "Contact is in ${evt.value} state"
        state.windowsopen = false;
    }
}