definition(
    name: "Indicator on Schedule",
    namespace: "com.obycode",
    author: "obycode",
    description: "Turn off switch indicators at a given time, and turn them back on later",
    category: "Convenience",
    iconUrl: "http://cdn.device-icons.smartthings.com/indicators/never-lit.png",
    iconX2Url: "http://cdn.device-icons.smartthings.com/indicators/never-lit@2x.png"
)

preferences {
	section("Select switches to control...") {
		input name: "switches", type: "capability.indicator", multiple: true
	}
	section("Turn all indicators off at...") {
		input name: "startTime", title: "Turn Off Time?", type: "time"
	}
	section("And set them back to default mode at...") {
		input name: "stopTime", title: "Turn On Time?", type: "time"
	}
    section("Default mode is...") {
        input name:"defaultMode", title: "Which mode?", type: "enum", metadata:[ values: ['Lit when on', 'Lit when off'] ]
    }
}

def installed() {
	log.debug "Installed with settings: ${settings}"
	schedule(startTime, "startTimerCallback")
	schedule(stopTime, "stopTimerCallback")

}

def updated(settings) {
	unschedule()
	schedule(startTime, "startTimerCallback")
	schedule(stopTime, "stopTimerCallback")
}

def startTimerCallback() {
	log.debug "Turning off indicator"
	switches.indicatorNever()

}

def stopTimerCallback() {
	log.debug "Setting default indicator mode"
    if (defaultMode == "Lit when on") {
		switches.indicatorWhenOn()
    }
    else {
    	switches.indicatorWhenOff()
    }
}