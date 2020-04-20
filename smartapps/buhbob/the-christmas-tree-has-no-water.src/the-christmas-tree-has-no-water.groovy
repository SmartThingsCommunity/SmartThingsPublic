/**
* No Water
*
* Author: Bob
*/
definition(
    name: "The Christmas Tree Has no Water",
    namespace: "BuhBob",
    author: "Bob Solimine",
    description: "Sends an alert when there is no water and turns off a switch when no water is detected.",
    category: "Safety & Security",
    iconUrl: "http://cdn.device-icons.smartthings.com/Seasonal%20Winter/seasonal-winter-003-icn.png",
    iconX2Url: "http://cdn.device-icons.smartthings.com/Seasonal%20Winter/seasonal-winter-003-icn@2x.png",)

preferences {
section("When there's no water detected...") {
input "alarm", "capability.waterSensor", title: "Where?"
}
section( "Notifications" ) {
input "sendPushMessage", "enum", title: "Send a push notification?", metadata:[values:["Yes","No"]], required:false
input "phone", "phone", title: "Send a Text Message?", required: false
}
section("Turn off...") {
input "switches", "capability.switch", title: "Which?", required: false, multiple: true
}
}

def installed() {
subscribe(alarm, "water.dry", waterWetHandler)
subscribe(switches, "switch.on", waterWetHandler)
}

def updated() {
unsubscribe()
subscribe(alarm, "water.dry", waterWetHandler)
subscribe(switches, "switch.on", switchWaterCheck)
}

def waterWetHandler(evt) {
def deltaSeconds = 30

def timeAgo = new Date(now() - (1000 * deltaSeconds))
def recentEvents = alarm.eventsSince(timeAgo)
log.debug "Found ${recentEvents?.size() ?: 0} events in the last $deltaSeconds seconds"

switches.off()

def alreadySentSms = recentEvents.count { it.value && it.value == "wet" } > 1
if (alreadySentSms) {
	log.debug "SMS already sent to $phone within the last $deltaSeconds seconds"
} else {
	def msg = "${alarm.displayName} has no water!"
	log.debug "$alarm is dry, texting $phone"
	sendPush(msg)
	if (phone) {
		sendSms(phone, msg)
	}
}
}

def switchWaterCheck(evt) {
	def waterState = alarm.currentValue("water")
    log.debug "$waterState"
	if("$waterState" == "wet")
    {
    	log.debug "Water detected: $alarm. No action."
    }   
    else
    {
    	switches.off()
        log.debug "No water detected: $alarm. Turning off $switches."
    }
}