definition(
    name: "Who let the dogs out (and left the lights on)?",
    namespace: "webxl",
    author: "Matt Motherway",
    description: "Turn something on when the dog goes out at night and off when he comes back in.",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Meta/light_contact-outlet.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Meta/light_contact-outlet@2x.png"
)

preferences {
    section("Options") {
    	input "contact1", "capability.contactSensor", title: "Which contact sensor?"
    	input "switches", "capability.switch", title: "Which lights?", multiple: true
		input "nightOnly", "enum", title: "Run only at night?", options: ["Yes", "No"], default: "Yes", required: true
		input "maxOnTime", "decimal", title: "Maximum on time in minutes (optional):", required: false
	}
}


def installed()
{
	state.skipSwitchOff = false
    subscribe(contact1, "contact.open", contactOpenHandler)
    subscribe(contact1, "contact.closed", contactCloseHandler)
    //subscribe(switches, "switch", switchHandler)
    
}

def updated()
{
	unsubscribe()
	subscribe(contact1, "contact.open", contactOpenHandler)
    subscribe(contact1, "contact.closed", contactCloseHandler)
    //shouldTurnOn()
}

def switchHandler(evt) {
	def ms = switches.get(0).currentValue("switch")
	log.debug "$evt.value: $evt, $ms"
}

def shouldTurnOn() {

	if (nightOnly == "No") {
    	return true
    }
    
	def now = new Date()
	def sunTime = getSunriseAndSunset()
    
    if (sunTime.sunset == null) {
    	log.debug "Couldn't determine sunrise/sunset for this location; using test zip code"
    	sunTime = getSunriseAndSunset(zipCode: "90210")
    }
    
    def nextRise = sunTime.sunrise + 1;
    
    log.debug "nowTime: $now"
	log.debug "riseTime: $nextRise"
	log.debug "setTime: $sunTime.sunset"
    
    return now > sunTime.sunset && now < nextRise
}


def switchesAreOn() {
	def switchesOn = switches.any { switchVal ->
        switchVal.currentValue("switch") == "on"
    }
    
    return switchesOn
}

def contactOpenHandler(evt) {

	log.debug "$evt.value: $evt, $settings, nightOnly: $nightOnly; $state.skipSwitchOff"
    
	if ( !switchesAreOn() && shouldTurnOn()) {
        switches.on()
        state.skipSwitchOff = true
        log.trace "Turning on switches: $switches"
        
        if (maxOnTime != null) {
        	def switchOffDelay = 60 * maxOnTime
			runIn(switchOffDelay, turnOffSwitch)
        }
    } else {
    	state.skipSwitchOff = false
    }
    
}

def contactCloseHandler(evt) {
	log.debug "$evt.value: $evt, $settings, $state.skipSwitchOff"
	if ( !state.skipSwitchOff && switchesAreOn() ) {	
		turnOffSwitch()
    }
}

def turnOffSwitch() {
	log.trace "Turning off switches: $switches"
    switches.off()
    state.skipSwitchOff = false
    unschedule("turnOffSwitch")
}

