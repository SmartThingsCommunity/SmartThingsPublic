/**
 *  Notify When On For
 *
 *  Author: Nicholas Wilde
 *
 *  Based on Improved Power Allowance by chrisb
 *
 *  This program sends a notification instead of turning off a switch.
 */

// Automatically generated. Make future change here.
definition(
    name: "Notify When On For",
    namespace: "nicholaswilde/smartthings",
    author: "Nicholas Wilde",
    description: "Notify after switch has been on for X minutes.  If the switch/outlet is powered off prior to the 'time out,' then the scheduled time off is cancelled.",
    category: "Green Living",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Meta/window_contact.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Meta/window_contact@2x.png"
)

preferences {
	section("When a switch turns on...") {
		input "theSwitch", "capability.switch"
	}
	section("Notify how many minutes later? (optional, default 15 minutes)") {
		input "minutesLater", "number", title: "When?", required: false
	}
    section("Send this message (optional, sends standard status message if not specified)"){
		input "messageText", "text", title: "Message Text", required: false
	}
	section("Via a push notification and/or an SMS message"){
		input "phone", "phone", title: "Phone Number (for SMS, optional)", required: false
		input "pushAndPhone", "enum", title: "Both Push and SMS?", required: false, metadata: [values: ["Yes","No"]]
	}
}

def installed() {
	log.debug "Installed with settings: ${settings}"
	subscribe(theSwitch, "switch.on", switchOnHandler, [filterEvents: true])
}

def updated() {
	log.debug "Updated with settings: ${settings}"
	unsubscribe()
	subscribe(theSwitch, "switch.on", switchOnHandler, [filterEvents: true])
}

def switchOnHandler(evt) {
	log.debug "Switch ${theSwitch} turned: ${evt.value}"
    def minutesLater = minutesLater ? minutesLater: 15
	def delay = minutesLater * 60
	log.debug "Sending notification in ${minutesLater} minutes (${delay}s)"
	runIn(delay, sendMessage)
}

def sendMessage() {
	def msg = messageText ?: "${theSwitch} has been on for ${minutesLater} minutes."
    if (theSwitch.currentValue("switch") == "on"){
        if (!phone || pushAndPhone != "No") {
            log.debug "sending push"
            sendPush(msg)
        }
        if (phone) {
            log.debug "sending SMS"
            sendSms(phone, msg)
        }
    }
}