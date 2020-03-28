/**
 *  Let There Be Dark!
 *  Turn your lights off when a Contact Sensor is opened and turn them back on when it is closed, ONLY if the Lights were previouly on.
 *
 *  Author: SmartThings modified by Douglas Rich
 */
definition(
    name: "Let There Be Dark!",
    namespace: "Dooglave",
    author: "Dooglave",
    description: "Turn your lights off when a Contact Sensor is opened and turn them back on when it is closed, ONLY if the Lights were previouly on",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Meta/light_contact-outlet.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Meta/light_contact-outlet@2x.png"
)

preferences {
	section("When the door opens") {
		input "contact1", "capability.contactSensor", title: "Where?"
	}
	section("Turn off a light") {
		input "switch1", "capability.switch"
	}
}

def installed() {
	subscribe(contact1, "contact", contactHandler)
}

def updated() {
	unsubscribe()
	subscribe(contact1, "contact", contactHandler)
}

def contactHandler(evt) {
	log.debug "$evt.value"
	if (evt.value == "open") {
        state.wasOn = switch1.currentValue("switch") == "on"
		switch1.off()
}	

if (evt.value == "closed") {
	if(state.wasOn)switch1.on()
}
}
