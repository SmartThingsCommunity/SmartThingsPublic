/**
 *  Accelerometer Light
 *
 *  Author: Chuck Norris, modified by Slonob
 *
 */
definition(
    name: "XLR8 Light Plus",
    namespace: "slonob",
    author: "The Norris, Jarrod Stenberg",
    description: "Turns on lights when a knock is detected. Can delay turning on multiple lights to simulate someone being home. Turns lights off when it becomes light or some time after motion ceases.",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Meta/light_motion-outlet-luminance.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Meta/light_motion-outlet-luminance@2x.png"
)

preferences {
    section("First light(s):"){
        input "lights1", "capability.switch", multiple: true
    }
    section("First light delay to simulate occupancy (recommend 5-10):"){
        input "delayStartSec1", "number", title: "Seconds:"
    }
    section("Blink first light when already on:"){
        input "lights1Blink", "bool", required: true
    }
    section("Second light(s):"){
        input "lights2", "capability.switch", multiple: true, required: false
    }
    section("Second light delay to simulate occupancy (recommend 8-15):"){
        input "delayStartSec2", "number", title: "Seconds:", required: false
    }
    section("Blink second light when already on:"){
        input "lights2Blink", "bool", required: false
    }
    section("Triggering sensor:"){
        input "accel", "capability.accelerationSensor", title: "Which acceleration sensor?", required: false
        input "motio", "capability.motionSensor", title: "Which motion sensor?", required: false
    }
    section("Turn lights off when there's been no movement after:"){
        input "delayStopMinutes", "number", title: "Minutes:"
    }
    
    
}

def installed() {
    initialize()
}

def updated() {
    unsubscribe()
    unschedule()
    initialize()
}

def initialize() {
	if (accel) {
		subscribe(accel, "acceleration", accelHandler)
    }
    if (motio) {
	    subscribe(motio, "motion", accelHandler)
	}
}

def accelHandler(evt) {
    log.debug "accelHandler event: $evt.name: $evt.value descriptionText:$evt.descriptionText"
    if (evt.value == "active") {
        log.debug "light1 original state is $lights1.currentSwitch"
		if (lights2) {
			log.debug "light2 original state is $lights2.currentSwitch"
        }
		log.debug "turning on lights due to motion with $delayStartSec1, $delayStartSec2 seconds delay"

		runIn(delayStartSec1, turnOnLights1, [overwrite: false])            
		if (lights2) {
	        if (delayStartSec2) {
            	runIn(delayStartSec2, turnOnLights2, [overwrite: false])            
			} else {
            	runIn(delayStartSec1 + 5, turnOnLights2, [overwrite: false])            
            }
		}
		state.motionStopTime = null
    }
    else {
        state.motionStopTime = now()
        if(delayStopMinutes) {
            runIn(delayStopMinutes*60, turnOffMotionAfterDelay, [overwrite: false])
        } else {
            turnOffMotionAfterDelay()
        }
    }
}

def turnOnLights1() {
    // either switch is "on" or "off"
    state.ogStatusLights1 = lights1.currentSwitch.contains("on");
    log.debug "lights1.currentSwitch = $lights1.currentSwitch -- state.ogStatusLights1 = $state.ogStatusLights1"
	if (lights1 && state.ogStatusLights1) { 
    	log.debug "lights1 were already on."
		if (lights1Blink) {
	    	log.debug "Blinking lights1."
		    lights1.off()
            lights1.on()
        }
	} else {
    	log.debug "lights1 were off. Turning on."
	    lights1.on()
	}
	state.lastStatus = "on"
}

def turnOnLights2() {
    state.ogStatusLights2 = lights2.currentSwitch.contains("on");
    log.debug "lights2.currentSwitch = $lights2.currentSwitch -- state.ogStatusLights2 = $state.ogStatusLights2"
	if (lights2 && state.ogStatusLights2) { 
    	log.debug "lights2 were already on."
		if (lights2Blink) {
	    	log.debug "Blinking lights2."
		    lights2.off()
	 	    lights2.on()
        }
	} else {
    	log.debug "lights2 were off. Turning on."
	    lights2.on()
	}
}

def turnOffMotionAfterDelay() {
    log.trace "In turnOffMotionAfterDelay, state.motionStopTime = $state.motionStopTime, state.lastStatus = $state.lastStatus"
    if (state.motionStopTime && state.lastStatus != "off") {
        def elapsed = now() - state.motionStopTime
        log.trace "elapsed = $elapsed"
        if (elapsed >= ((delayStopMinutes ?: 0) * 60000L) - 2000) {
        	log.debug "Turning off lights1? state.ogStatusLights1 = $state.ogStatusLights1"
			if (state.ogStatusLights1 == false) {
	            log.debug "Turning off lights1."
				lights1.off()
            }
			if (lights2) {
				log.debug "Turning off lights2? state.ogStatusLights2 = $state.ogStatusLights2"
				if (state.ogStatusLights2 == false) {
                	log.debug "Turning off lights2."
	            	lights2.off()
                }
			}
			state.lastStatus = "off"
        }
    }
}