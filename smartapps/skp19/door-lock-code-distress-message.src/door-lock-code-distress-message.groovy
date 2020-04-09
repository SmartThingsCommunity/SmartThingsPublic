/**
 *  Door Lock Code Distress Message
 *
 *  Copyright 2014 skp19
 *
 */
definition(
    name: "Door Lock Code Distress Message",
    namespace: "skp19",
    author: "skp19",
    description: "Sends a text to someone when a specific code is entered",
    category: "Safety & Security",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")

import groovy.json.JsonSlurper

preferences {
	section("Choose Locks") {
		input "lock1", "capability.lock", multiple: true
	}
    section("Enter User Code Number (This is not the code used to unlock the door)") {
    	input "distressCode", "number", defaultValue: "0"
    }
    section("Distress Message Details") {
    	input "phone1", "phone", title: "Phone number to send message to"
    	input "distressMsg", "text", title: "Message to send"
    }
    section("User Code Discovery Mode (Enable and unlock the door using desired code. A message will be sent containing the user code used to unlock the door.)") {
    	input "discoveryMode", "bool", title: "Enable"
    }
}

def installed() {
    subscribe(lock1, "lock", checkCode)
}

def updated() {
	unsubscribe()
    subscribe(lock1, "lock", checkCode)
}

def checkCode(evt) {
    log.debug "$evt.value: $evt, $settings"

    if(evt.value == "unlocked" && evt.data) {
    	def lockData = new JsonSlurper().parseText(evt.data)
        
        if(discoveryMode) {
        	sendPush "Door unlocked with user code $lockData.usedCode"
        }
        
        if(lockData.usedCode == distressCode && discoveryMode == false) {
        	log.info "Distress Message Sent"
        	sendSms(phone1, distressMsg)
        }
    }
}
