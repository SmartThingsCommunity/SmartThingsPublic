/**
 *
 *  Lights On When Door Open After Sundown
 *
 *  Based on "Turn It On When It Opens" by SmartThings
 *
 *  Author: Aaron Crocco
 */
preferences {
	section("When the door opens..."){
		input "contact1", "capability.contactSensor", title: "Where?"
	}
	section("Turn on these lights..."){
		input "switches", "capability.switch", multiple: true
	}
	section("and change mode to...") {
		input "HomeAfterDarkMode", "mode", title: "Mode?"
	}
}


def installed()
{
	subscribe(contact1, "contact.open", contactOpenHandler)
}

def updated()
{
	unsubscribe()
	subscribe(contact1, "contact.open", contactOpenHandler)
}

def contactOpenHandler(evt) {
	log.debug "$evt.value: $evt, $settings"
	
	//Check current time to see if it's after sundown.
	def s = getSunriseAndSunset(zipCode: zipCode, sunriseOffset: sunriseOffset, sunsetOffset: sunsetOffset)
	def now = new Date()
	def setTime = s.sunset
	log.debug "Sunset is at $setTime. Current time is $now"
        
        
    if (setTime.before(now)) {	//Executes only if it's after sundown.
			
		log.trace "Turning on switches: $switches"
		switches.on()
		log.trace "Changing house mode to $HomeAfterDarkMode"
        setLocationMode(HomeAfterDarkMode)
        sendPush("Welcome home! Changing mode to $HomeAfterDarkMode.")

	}   
}

