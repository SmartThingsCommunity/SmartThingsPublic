definition(
    name: "Beep, When Closed",
    namespace: "smartthings",
    author: "SmartThings",
    description: "Beep presence sensor when open/close sensor closes.",
    category: "Convenience",
   iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience%402x.png",
)

preferences {
	section ("When closes...") {
		input "contact1", "capability.contactSensor", title: "Which open/contact sensor?"
	}
	section("Beep the presence sensor..") {
		input "presence", "capability.presenceSensor", title: "Which sensor beep?"
	}
}

def installed()
{
	subscribe(contact1, "contact.closed", contactClosedHandler)
}

def updated()
{
	unsubscribe()
	subscribe(contact1, "contact.closed", contactClosedHandler)
}

def contactClosedHandler(evt) {
	
        presence.beep()
}