definition(
    name: "Indicator on Schedule",
    namespace: "com.obycode",
    author: "obycode",
    description: "Turn ooff switch indicators at a given time, and turn them back on later",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Meta/light_outlet.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Meta/light_outlet@2x.png"
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