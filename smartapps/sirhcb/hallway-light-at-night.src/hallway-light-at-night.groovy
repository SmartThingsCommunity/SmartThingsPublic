/**
 *  Hallway light at night.
 *
 *  Author: chrisb
 *  Borrowing pieces of code by: SmartThings & CoryS from "Lights Follow Me Dimmable"
 *
 *  Turn a dimmer light on at a low level if it isnt on already for a few minutes 
 *  when motion is detected during a given time period.
 *
 *  Example use would to be turn on a hall light at a low percentage at time when 
 *  someone gets up to use the bathroom at night.
 */


// Automatically generated. Make future change here.
definition(
    name: "Hallway light at night",
    namespace: "sirhcb",
    author: "seateabee@gmail.com",
    description: "Turn a dimmer light on at a low level if it isnt on already for a few minutes when motion is detected during a given time period. Example use would to be turn on a hall light at a low percentage at time when someone gets up to use the bathroom at night.",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience%402x.png",
    oauth: true
)

preferences {
	section("Turn on when there's movement..."){
		input "motion1", "capability.motionSensor", title: "Where?"
	}
	section("And off when there's been no movement for..."){
		input "minutes1", "number", title: "Minutes?"
	}
	section("Turn on this dimmer switch...") {
    	input "MultilevelSwitch", "capability.switchLevel", title: "Which?"
    }
    section("How Bright?"){
    	input "number", "number", title: "Percentage, 0-99"
    }    
	section("Only dim between these times:") {
		input "timeOfDay1", "time", title: "Start Time?"
		input "timeOfDay2", "time", title: "End Time?"
	}
}


def installed()
{
	subscribe(motion1, "motion", motionHandler)
	subscribe(MultilevelSwitch, "switch.on", switchOn)
    subscribe(MultilevelSwitch, "switch.off", switchOff)
}

def updated()
{
	unsubscribe()
	subscribe(motion1, "motion", motionHandler)
    subscribe(MultilevelSwitch, "switch.on", switchOn)
    subscribe(MultilevelSwitch, "switch.off", switchOff)
}

def motionHandler(evt) {
	log.debug "$evt.name: $evt.value"
    if (evt.value == "active") {  									// We're seeing motion, so go to next test...
    	MultilevelSwitch.refresh()
		log.debug "Seeing motion..."
        def startTime = timeToday(timeOfDay1)
    	def endTime = timeToday(timeOfDay2)
		if (now() < startTime.time && now() > endTime.time) { 			// Testing time of day.  If the statement is true, we're outside our
    		log.debug "Outside of time range, doing nothing."			// time frame, so we're doing nothing.
    		}
	    else {															// Passed time test, so go to next test...
        	log.trace "We're inside the time range."
	     	if (state.switch == 0) {										// Passed last test, light is off.  Let's go dimming!
				log.debug "Light is off, let's go dimming!"            
        		unschedule(reset)													//Turn off any scheduled resets.
                settings.MultilevelSwitch.setLevel(number)
				def timeDelay = minutes1 * 60										//runIn uses seconds. Multiply by 60 to convert to seconds.
            	runIn (timeDelay, reset)
			}																
            else {
            	log.debug "Light is on... I'm not touching it."
            }																// Completes the "if the light is on" test.    
        }  																// Completes the "if it's the right time" test.  
	}																// Completes the "there's motion" test.
}

def switchOn(evt) {
	log.debug "Switch on, state = 1"
    state.switch = 1				// When switch is on we're not going to run the dimmer function.  1 = on
}    

def switchOff(evt) {
	log.debug "Switch off, state = 0"
	state.switch = 0				// When switch is off, we'll run the dimmer function.  0 = off
}

def reset() {						// The dimming event will leave the light level very low, so reset to full level
	settings.MultilevelSwitch.setLevel(99)
    MultilevelSwitch.off()
    }