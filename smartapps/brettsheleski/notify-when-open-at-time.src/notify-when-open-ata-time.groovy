
definition(
    name: "Notify Me When Open at Time",
    namespace: "sheleski",
    author: "Brett Sheleski",
    description: "Notifies if a contact has been left open or closed at a specified time.",
    category: "SmartThings Labs",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/App-NotifyWhenNotHere.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/App-NotifyWhenNotHere@2x.png"
)

preferences {

	section("Set time and contacts") {
        input "timeOfDay", "time", title: "At a Scheduled Time", required: true
		input "contactsOpen", "capability.contactSensor", title: "Contact Opened", required: false, multiple: true
		input "contactsClosed", "capability.contactSensor", title: "Contact Closed", required: false, multiple: true
	}

}

def installed() {
	log.debug "Installed with settings: ${settings}"
	subscribeToEvents()
}

def updated() {
	log.debug "Updated with settings: ${settings}"
	unsubscribe()
	unschedule()
	subscribeToEvents()
}

def subscribeToEvents() {	
	if (timeOfDay) {
		schedule(timeOfDay, scheduledTimeHandler)
	}
}

def scheduledTimeHandler() {
    contactsOpen.each {
        if (it.currentValue("contact") != "closed"){
            sendPush("${it.label ?: it.name} is open")
        }
    }

    contactsClosed.each {
        if (it.currentValue("contact") == "closed"){
            sendPush("${it.label ?:  it.name} is closed")
        }
    }
}
