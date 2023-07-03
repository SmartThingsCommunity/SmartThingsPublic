/**
 *  Light My Pressure Pad
 *  Turn your lights on when an open/close sensor opens and off when the sensor closes.
 *  Modified by Ken Washington to reverse the logic for a pressure pad which closes
 *  when someone steps on the pad, and that is when you want the light to come on
 *
 *  Author: SmartThings - edited by Ken Washington
 *  Date:   05/09/2015
 */
definition(
    name: "Light My Pressure Pad",
    namespace: "kewashi",
    author: "KenWashington",
    description: "Turn your lights on when a Pressure Pad is activated - with options for sunset and alternate light override.",
    category: "Convenience",
    iconUrl: "http://www.kenw.com/st/pressure_pad.png",
    iconX2Url: "http://www.kenw.com/st/pressure_pad_2x.png"
)

preferences {
	section("When the pad is stepped on/off...") {
		input "contact1", "capability.contactSensor", title: "Select which pressure pad?"
	}
	section("Turn on/off a light...") {
		input "switches", "capability.switch", multiple: true
	}
	section("Auto off options..."){
        input "nomotion", "boolean", title: "Turn off when pad opens"
		input "minutes1", "number", title: "Wait this many minutes?", required: false
	}
    section("More options...") {
        input "usesunset", "boolean", title: "Active only after sunset and before sunrise?"
		input "switches2", "capability.switch", title: "Ignore motion if these are on", multiple: true, required: false
    }
}

def installed() {
    state.isdark = true
    initialize()
}

def updated() {
	unsubscribe()
    initialize()
}

def initialize() {
	subscribe(contact1, "contact", contactHandler)

    state.isdark = true
    if (usesunset) {
        def srss = getSunriseAndSunset()
        if ( now() < srss.sunset.getTime() ) {
            state.isdark = false
        }
    	subscribe(location, "sunrise", sunriseHandler)
	    subscribe(location, "sunset", sunsetHandler)
        // log.debug "Sunset / Sunrise monitoring enabled. Currently isdark = $state.isdark"
    }
    
    // fix minutes1 if something crazy specified
    if (nomotion && minutes1 > 1440) {
    	minutes1 = 1440
    }

    log.debug "usesunset = $usesunset  minutes1 = $minutes1 nomotion = $nomotion"
}

// updated to handle sunset and override cases plus delay
def contactHandler(evt) {
	log.debug "Contact handler in pressure pad light app - value: $evt.value"


//	if (evt.value == "open") {
//		switch1.off()
//	} else if (evt.value == "closed") {
//		switch1.on()
//	}


    def ignoremotion = false

    // check if the override light is on - if so, log info and do nothing
    def overrideswitches = switches2.currentSwitch
    if (overrideswitches) {
        def onsw = overrideswitches.findAll {
            switchVal -> switchVal == "on" ? true : false
        }
        if (onsw.size() == 1) {
            log.debug "A switch is on that you specified to override so pressure pad ignored"
            ignoremotion = true
        }
        else if (onsw.size() > 0) {
            log.debug "${onsw.size()} switches are on that you specified to override so pressure pad ignored"
            ignoremotion = true
        }
    }

    // check if sunrise override
    if (usesunset==true) {
        if ( state.isdark==false) {
            ignoremotion = true
            log.debug "sunrise has occured and sunset has not happened yet so pressure pad ignored usesunset = $usesunset"
        }
    }
    
	// handle case when it is dark or the option isnt used
    if ( !ignoremotion ) {
    	if (evt.value == "closed") {
    		log.debug "turning on lights due to motion"
    		switches.on()
    	} else if (evt.value == "open") {

            // check settings
            if (nomotion && minutes1==0) {
	            log.debug "turning off lights due to no motion immediately"
            	switches.off()
            }
            else if (nomotion && minutes1) {
	            log.debug "turning off lights due to no motion in $minutes1 minutes"
        		runIn(minutes1 * 60, scheduleCheck, [overwrite: false])
            } else {
            	log.debug "motion stopped but lights not turned off because feature not activated"
            }
    	}
    }



}


def scheduleCheck() {
	def contactState = contact1.currentState("contact")

    // if the pressure pad is open, we are not stepping or sitting on it
    if (contactState.value == "open") {
        def elapsed = now() - contactState.rawDateCreated.time
    	def threshold = 1000 * 60 * minutes1 - 1000
    	if (elapsed >= threshold) {
            log.debug "Contact has stayed open long enough since last check ($elapsed ms):  turning lights off"
            switches.off()
    	} else {
        	log.debug "Contact has not stayed open long enough since last check ($elapsed ms):  doing nothing"
        }
    } else {
    	log.debug "Contact is closed, do nothing and wait for inactive"
    }
}


def sunriseHandler(evt) {
    state.isdark = false
	log.debug "Sunrise happened"
}

def sunsetHandler(evt) {
    state.isdark = true
	log.debug "Sunset happened"
}
