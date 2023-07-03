definition(
    name: "Shut the windows!",
    namespace: "nick257",
    author: "Nick Mahon",
    description: "Notifies when last person leaves without closing doors or windows.",
    category: "Safety & Security",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/ModeMagic/bon-voyage.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/ModeMagic/bon-voyage%402x.png"
)

preferences 
{
	section("As the last of these persons leaves") {
		input "people", "capability.presenceSensor", multiple: true
	}
	section("Check that these are closed") {
		input "doors", "capability.contactSensor", multiple: true
	}
    section("Send Push Notification?") {
		input "sendPush", "bool", required: false,
		title: "Send Push Notification when Opened?"
	}
	section("Send a text message to this number (optional)") {
	input "phone", "phone", required: false
	}
}


def installed() {
	log.debug "Installed with settings: ${settings}"
	initialize()
}

def updated() {
	log.debug "Updated with settings: ${settings}"
    unsubscribe()
    initialize()
}

def initialize() {
	subscribe(people, "presence", presence)
}

def presence(evt)
{
	log.debug "evt.name: $evt.value, $evt.deviceId"
	if (evt.value == "not present") {
    	log.debug "isSomeonePresent()? ${isSomeonePresent()}"
        log.debug "isDoorsOpen()? ${isDoorsOpen()}"
    	if (!isSomeonePresent() && isDoorsOpen()) {
        	def device = people.find { it.id == evt.deviceId }
            log.debug "${device.displayName} left ${location.name} without closing ${isDoorsOpen().join(", ")}"
            
            def message = "${device.displayName} left ${location.name} without closing ${isDoorsOpen().join(", ")}"
			if (sendPush) {
				sendPush(message)
			}
			if (phone) {
				sendSms(phone, message)
			}
		}
	}
}

def isSomeonePresent() {
	log.debug "presence: $people.currentPresence"
    people.findAll{it.currentPresence == "present"}
}

def isDoorsOpen() {
	log.debug "doorss: $doors.currentContact"
	doors.findAll{it.currentContact == "open"}
}
