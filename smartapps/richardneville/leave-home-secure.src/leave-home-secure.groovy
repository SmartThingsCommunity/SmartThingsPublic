definition (
        name: "Leave home secure",
        namespace: "richardneville",
        author: "Richard Neville",
        description: "Tells us if the doors and windows are open when we go out.",
        category: "My Apps",
        iconUrl: "http://animl.co.uk/img/SmartThingsLeaveHomeSecure.png",
        iconX2Url: "http://animl.co.uk/img/SmartThingsLeaveHomeSecure@2x.png",
        iconX3Url: "http://animl.co.uk/img/SmartThingsLeaveHomeSecure@2x.png"
        )

preferences {
    section("Things to report on") {
        input "contactSensorThings", "capability.contactSensor", required: true, multiple: true, 
            title: "Select which things to watch:"
        paragraph "This SmartApp reports if the doors and windows are left open when we go out. The report can be triggered in various ways. v0.1"
    }
    section("Things to trigger the report") {
        input "presenceThings", "capability.presenceSensor", required: false, multiple: true,
            title: "Notify when these things leave home:"
        input "contactSensorTrigger", "capability.contactSensor", required: false, multiple: true,
            title: "Notify when this thing closes:"
        input "motionSensorThing", "capability.motionSensor", required: false, multiple: true,
            title: "Notify when movement is detected near this thing:"
    }
    section("Settings") {
        input "fullReport", "bool", title: "Full report, even if things are closed?", required: false
        input "pushNotification", "bool", title: "Push the message out?", required: false
        input("recipients", "contact", title: "Send messages to:") {
            input "phone", "phone", title: "Send an SMS to:", description: "Phone Number", required: false
        }
        paragraph "(If no recipients are selected, app will just send a push notification instead.)"
    }
}

def installed() {
    initialize()
}

def updated() {
	unsubscribe()
    initialize()
}

def initialize() {
    subscribe(presenceThings, "presence", presenceHandler)
    subscribe(contactSensorTrigger, "contact", contactHandler)
    subscribe(motionSensorThing, "motion", motionHandler)
    
    /*
    subscribe(switchThing, "switch", switchHandler)
    */
}

def presenceHandler(evt) {
    if (evt.value == "not present") {
        if (allThingsLeftHome()) {
            checkIfHouseSecure()
	    }
    }
}

def contactHandler(evt) {
	if (evt.value == "closed") {
        checkIfHouseSecure()
    }
}

def motionHandler(evt) {
    if (evt.value == "active") {
    	// motion detected
        checkIfHouseSecure()
    } else if (evt.value == "inactive") {
        // motion stopped
    }
}

/*
def switchHandler(evt) {
	if (debugMode) {
        checkIfHouseSecure()
    }
}
*/

private checkIfHouseSecure() {
    for (contactSensorThing in contactSensorThings) {
        def sensorState = contactSensorThing.currentState("contact").value
        if (sensorState == "open" || fullReport) {
            sendMsg("${contactSensorThing.displayName} is ${sensorState}.")
        }
    }
}

private allThingsLeftHome() {
    def result = true
    for (presenceThing in presenceThings) {
        if (presenceThing.currentPresence == "present") {
            // someone is present, so set result to false and terminate the loop.
            result = false
            break
        }
    }
    return result
}

private sendMsg(msg) {
    if (pushNotification) {
        // check that contact book is enabled and recipients selected
        if (location.contactBookEnabled && recipients) {
            sendNotificationToContacts(msg, recipients)
        } else if (phone) { // check that the user did select a phone number
            sendNotificationEvent("SMS sent to ${phone}")
            sendSms(phone, msg)
            //sendNotification(msg, [method: "phone", phone: "1234567890"])
        } else {
            sendPush(msg)
        }
    } else {
        sendNotificationEvent(msg)
    }
}

/*

def switchHandler(evt) {
    if (evt.value == "on") {
        //sendPush("The ${switchThing.displayName} is on!")
    } else if (evt.value == "off") {
        //sendPush("The ${switchThing.displayName} is off!")
    }
}

def contactHandler(evt) {
    if (evt.value == "open") {
        // contactSensor open
    } else if (evt.value == "closed") {
        // contactSensor closed
    }
}

*/