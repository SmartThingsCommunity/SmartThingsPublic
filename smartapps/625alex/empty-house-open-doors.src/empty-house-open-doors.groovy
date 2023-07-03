definition(
    name: "Empty house, open doors",
    namespace: "625alex",
    author: "Alex Malikov",
    description: "Notifies when last person leaves without closing doors or windows.",
    category: "Safety & Security",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png"
)

preferences 
{
	section("As the last of these persons leaves") {
		input "people", "capability.presenceSensor", multiple: true
	}
	section("Check that these are closed") {
		input "doors", "capability.contactSensor", multiple: true
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
            sendPush "${device.displayName} left ${location.name} without closing ${isDoorsOpen().join(", ")}"
		}
	}
}

def isSomeonePresent() {
	log.debug "presence: $people.currentPresence"
    people.findAll{it.currentPresence == "present"}
}

def isDoorsOpen() {
	doors.findAll{it.currentContact == "open"}
}
