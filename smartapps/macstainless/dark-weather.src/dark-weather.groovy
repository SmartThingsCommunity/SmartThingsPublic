/**
 *  Dark Weather (Version 2)
 *
 *	This app monitors a switch state and will set the 
 *	house into a desired mode in order for the lights to work during the day.
 *	For best use, monitor a virtual switch and set IFTTT to turn it on when there's rain
 *	and off when the sky is clear.
 *	If it's after sundown, the app won't execute so you're not taken out of your evening / night mode.
 *
 *  Author: Aaron Crocco
 */
 
 
definition(
    name: "Dark Weather",
    namespace: "macstainless",
    author: "Aaron Crocco",
    description: "Use a virtual switch (best when combined with IFTTT weather monitoring) to trigger a mode change. Perfect for setting your house to a 'rain mode' so lights turn on during the day. V2.0",
    category: "Mode Magic",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
	section("App Setup (Version 2)") {}
	
    section("Choose a switch to trigger the change... "){
		input "triggerSwitch", "capability.switch"
	}
	section("When it rains change mode to...") {
		input "rainMode", "mode", title: "Mode?"
	}
    section("When it's all clear change mode to...") {
		input "clearMode", "mode", title: "Mode?"
	}
    section("and (optionally) turn off these lights...") {
    	input "switches", "capability.switch", multiple: true, required: false
    }
}

def installed() {
	log.debug "Installed with settings: ${settings}"
    subscribe(triggerSwitch, "switch", switchHandler)
	initialize()
}

def updated() {
	log.debug "Updated with settings: ${settings}"

	unsubscribe()
	initialize()
}

def initialize() {

        subscribe(triggerSwitch, "switch", switchHandler)
}

def switchHandler(evt) {

	//When the switch turned on or off, this code executes
    
    log.debug "Swich changed state!"

	if (evt.value == "on") {
        log.debug "switch turned on!"
        weatherModeChange("wet")
    }
    else if (evt.value == "off") {
        log.debug "switch turned off!"
        weatherModeChange("dry")
	}
}

def weatherModeChange(evt) {

	log.debug "Weather mode change has started. Current event value is $evt"
        
	if (evt =="wet") {
    	log.debug "Wet / rain!"     
        if (location.mode != rainMode) {
    		setLocationMode(rainMode)
            log.debug "Mode changed."
            sendPush("It's dark out! Changing mode to $rainMode.")
            log.debug "Rain message sent."
		}
	}
	else if (evt == "dry" ) {
    	log.debug "Dry!"  
        
        //Check current time to see if it's after sundown.
        def s = getSunriseAndSunset(zipCode: zipCode, sunriseOffset: sunriseOffset, sunsetOffset: sunsetOffset)
		def now = new Date()
		def setTime = s.sunset
		log.debug "Sunset is at $setTime. Current time is $now"
        
        
        if (setTime.after(now)) {	//Executes only if it's before sundown.
		
         	if (location.mode != clearMode) {
    			setLocationMode(clearMode)
                switches?.off()
        		sendPush("The sky is clear! Turning lights off and changing mode to $clearMode.")
				log.debug "Mode changed, dry message sent."
			}        
        }
	}
}