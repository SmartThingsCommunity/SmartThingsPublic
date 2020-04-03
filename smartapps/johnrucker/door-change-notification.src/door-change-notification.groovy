/**
 *  Door Status Notification
 *
 *  Author: SmartThings
 */
definition(
    name: "Door Change Notification",
    namespace: "JohnRucker",
    author: "John.Rucker@Solar-current.com",
    description: "Get a push notification when your CoopBoss detects any door change.",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")

preferences {
    section("When the door state changes") {
        paragraph "ALL door state changes will cause a notification to be sent to your phone."
		input "doorSensor", "capability.doorControl", title: "Select CoopBoss", required: true, multiple: false            
        input("recipients", "contact", title: "Recipients", description: "Send notifications to") {
        	input "phone", "phone", title: "Phone number?", required: true}
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
	subscribe(doorSensor, "doorState", coopDoorStateHandler)
}

def coopDoorStateHandler(evt) {
	def msg = "${doorSensor.displayName} door changed to ${evt.value}!"
	log.debug "${doorSensor.displayName} door changed to ${evt.value}, texting $phone"

    if (location.contactBookEnabled) {
        sendNotificationToContacts(msg, recipients)
    }
    else {
        sendPush(msg)
        if (phone) {
            sendSms(phone, msg)
        }   
	}
}