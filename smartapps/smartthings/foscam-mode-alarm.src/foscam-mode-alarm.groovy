definition(
    name: "Foscam Mode Alarm",
    namespace: "smartthings",
    author: "skp19",
    description: "Enables Foscam alarm when the mode changes to the selected mode.",
    category: "Safety & Security",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
	section("When the mode changes to...") {
		input "alarmMode", "mode", multiple: true
	}
    section("Enable these Foscam alarms...") {
		input "cameras", "capability.imageCapture", multiple: true
        input "notify", "bool", title: "Notification?"
	}
    section("Only between these times...") {
    	input "startTime", "time", title: "Start Time", required: false
        input "endTime", "time", title: "End Time", required: false
    }
}

def installed() {
    subscribe(location, checkTime)
}

def updated() {
	unsubscribe()
    subscribe(location, checkTime)
}

def modeAlarm(evt) {
    if (evt.value in alarmMode) {
        log.trace "Mode changed to ${evt.value}. Enabling Foscam alarm."
        cameras?.alarmOn()
        sendMessage("Foscam alarm enabled")    	
    }
    else {
        log.trace "Mode changed to ${evt.value}. Disabling Foscam alarm."
        cameras?.alarmOff()
        sendMessage("Foscam alarm disabled")
    }
}

def checkTime(evt) {
    if(startTime && endTime) {
        def currentTime = new Date()
    	def startUTC = timeToday(startTime)
    	def endUTC = timeToday(endTime)
	    if((currentTime > startUTC && currentTime < endUTC && startUTC < endUTC) || (currentTime > startUTC && startUTC > endUTC) || (currentTime < endUTC && endUTC < startUTC)) {
    		modeAlarm(evt)
    	}
    }
    else {
    	modeAlarm(evt)
    }
}

def sendMessage(msg) {
	if (notify) {
		sendPush msg
	}
}