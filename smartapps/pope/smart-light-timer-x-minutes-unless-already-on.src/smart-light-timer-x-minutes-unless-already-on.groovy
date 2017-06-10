/**
 *  Smart Timer
 *  Loosely based on "Light Follows Me"
 *
 *  This prevent them from turning off when the timer expires, if they were already turned on
 *
 *  If the switch is already on, if won't be affected by the timer  (Must be turned of manually)
 *  If the switch is toggled while in timeout-mode, it will remain on and ignore the timer (Must be turned of manually)
 *
 *  The timeout perid begins when the contact is closed, or motion stops, so leaving a door open won't start the timer until it's closed.
 *
 *  Author: andersheie@gmail.com
 *  Date: 2015-10-30
 */

definition(
    name: "Smart Light Timer, X minutes unless already on",
    namespace: "Pope",
    author: "listpope@cox.net",
    description: "Turns on a switch for X minutes, then turns it off. Unless, the switch is already on, in which case it stays on. If the switch is toggled while the timer is running, the timer is canceled.",
    category: "Convenience",
    iconUrl: "http://upload.wikimedia.org/wikipedia/commons/6/6a/Light_bulb_icon_tips.svg",
    iconX2Url: "http://upload.wikimedia.org/wikipedia/commons/6/6a/Light_bulb_icon_tips.svg")

preferences {
	section("Turn on when there's movement..."){
		input "motions", "capability.motionSensor", multiple: true, title: "Select motion detectors", required: false
	}
	section("Or, turn on when one of these contacts opened"){
		input "contacts", "capability.contactSensor", multiple: true, title: "Select Contacts", required: false
	}
    section("Or, turn on when any of these people come home") {
    input "peoplearrive", "capability.presenceSensor", multiple: true, required: false
    }
    section("Or, turn on when any of these people leave") {
    input "peopleleave", "capability.presenceSensor", multiple: true, required: false
    }
	section("And off after no more triggers after..."){
		input "minutes1", "number", title: "Minutes?", defaultValue: "5"
	}
	section("Turn on/off light(s)..."){
		input "switches", "capability.switch", multiple: true, title: "Select Lights"
	}
}


def installed()
{
	log.debug "installed()"
    initialize()
}

def updated()
{
	log.debug "updated()"
	unsubscribe()
	initialize()
}

def initialize()
{
	log.debug "initialize()" 
	// Reset to new set of Switches, just in case.
    state.switches = [:]
    
    switches.each { 
		// log.debug "ID: $it.id" 
        // Set ready state for each switch
        state.switches["$it.id"] = "ready"
    }
    logStates()
	subscribe(motions, "motion", motionHandler)
    subscribe(switches, "switch", switchChange)
	subscribe(contacts, "contact", contactHandler)
  	subscribe(peoplearrive, "presence.present", presencePresentHandler)
    subscribe(peopleleave, "presence.not present", presenceNotPresentHandler)
    state.inactiveAt = null
   	schedule("0 * * * * ?", "scheduleCheck")

}

def logStates() {
	state.switches.each {
		log.debug "$it.key = $it.value" 
    }
}

def turnSwitchOn(id) {
	 for(S in switches) {
         if(S.id == id) {
             S.on()
         }
     }
}

def turnSwitchOff(id) {
	 for(S in switches) {
         if(S.id == id) {
             S.off()
         }
     }
}

def switchChange(evt) {
	def deviceID = evt.device.id
	log.debug "SwitchChange: $evt.name: $evt.value $evt.device.id current state = " + state.switches[deviceID]
    
	if(evt.value == "on") {
        if(state.switches[deviceID] == "activating") {
                // OK, probably an event from Activating something, and not the switch itself. Go to Active mode.
                log.debug "$deviceID = " + state.switches[deviceID] + " -> active"
                state.switches[deviceID] = "active"
            } else if(state.switches[deviceID] != "active") {
            	log.debug "$deviceID = " + state.switches[deviceID] + " -> already on"
                state.switches[deviceID] = "already on"
            }
    } else {
        // If active and switch is turned of manually, then stop the schedule and go to ready state
        if(state.switches[deviceID] == "active" || state.switches[deviceID] == "activating") {
            //unschedule()
        }
        state.switches[deviceID] = "ready"
    }
    logStates()
    
}

def contactHandler(evt) {
	log.debug "contactHandler: $evt.name: $evt.value"
    
	boolean resetInactive = false
    boolean setActive = false
    state.switches.each { thisswitch ->
         if (evt.value == "open") {
            if(state.switches[thisswitch.key] == "ready") {
                log.debug "Turning on lights by contact opening: $thisswitch"
                log.debug "$thisswitch.key = " + state.switches[thisswitch.key] + " -> activating"
                state.switches[thisswitch.key] = "activating"
                //log.debug "Looking for $thisswitch.key"
                
                turnSwitchOn(thisswitch.key)
                resetInactive = true
                //state.inactiveAt = null
            }
        } else if (evt.value == "closed") {
            if (!state.inactiveAt && (state.switches[thisswitch.key] == "active" 
            					   || state.switches[thisswitch.key] == "activating")) {
                // When contact closes, we reset the timer if not already set
                log.debug "$thisswitch.key = " + state.switches[thisswitch.key] + " -> active"
                state.switches[thisswitch.key] == "active"
                setActive = true
                //setActiveAndSchedule()
            }
        }
    }
    if(resetInactive) {
    	state.inactiveAt = null
    }
    if(setActive) {
    	setActiveAndSchedule()
    }
    log.debug "Final States Are"
    logStates()
}

def scheduleCheck() {
	log.debug "schedule check, ts = ${state.inactiveAt}"
	boolean resetInactive = false
	state.switches.each { thisswitch ->
         if(state.switches[thisswitch.key] != "already on") {
            if(state.inactiveAt != null) {
                def elapsed = now() - state.inactiveAt
                log.debug "${elapsed / 1000} sec since events stopped"
                def threshold = 1000 * 60 * minutes1
                if (elapsed >= threshold) {
                    if (state.switches[thisswitch.key] == "active" 
                    	|| state.switches[thisswitch.key] == "activating") {
	                    state.switches[thisswitch.key] = "ready"
                        log.debug "Turning off lights by contact closing: $thisswitch"
                        turnSwitchOff(thisswitch.key)
                    }
                    resetInactive = true
                    //state.inactiveAt = null
                }
            }
        }
        //log.debug "state: " + state.myState
    }
    if(resetInactive) {
    	state.inactiveAt = null
        unschedule()
    }
    logStates()
}


/**********************************************************************/

def motionHandler(evt) {
	log.debug "motionHandler: $evt.name: $evt.value (current state: " + state.myState + ")"


	state.switches.each { thisswitch ->
         if (evt.value == "active") {
            if(state.switches[thisswitch.key] == "ready" 
            	|| state.switches[thisswitch.key] == "active" 
                || state.switches[thisswitch.key] == "activating" ) {
                log.debug "$thisswitch.key = " + state.switches[thisswitch.key] + " -> activating"
                state.switches[thisswitch.key] = "activating"
                turnSwitchOn(thisswitch.key)
                state.inactiveAt = null
            }
        } else if (evt.value == "inactive") {
            if (!state.inactiveAt && (state.switches[thisswitch.key] == "active" || state.switches[thisswitch.key] == "activating")) {
                // When Motion ends, we reset the timer if not already set
               log.debug "$thisswitch.key = " + state.switches[thisswitch.key] + " -> active"
               state.switches[thisswitch.key] == "active"
               setActiveAndSchedule()
            }
        }
        //log.debug "state: " + state.switches["$it.key"]
    }
    logStates()
}

def presencePresentHandler(evt) {
  log.debug "presence: $evt.linkText has arrived home (current state: " + state.myState + ")"
  
  
    state.switches.each { thisswitch ->
        if (evt.value == "present") {
            if(state.switches[thisswitch.key] == "ready" 
            	|| state.switches[thisswitch.key] == "active" 
                || state.switches[thisswitch.key] == "activating" ) {
                log.debug "turning on switch $thisswitch"
                state.switches["hisswitch.key"] == "active"
                turnSwitchOn(thisswitch.key)
                // We don't wait until the person leave, but instead start timer immediately.
                setActiveAndSchedule()
                
            }
        } 
        //log.debug "state: " + state.switches["$it.key"]
    }
    logStates()
}

def presenceNotPresentHandler(evt) {
  log.debug "presence: $evt.linkText has left home (current state: " + state.myState + ")"

  

	state.switches.each { thisswitch ->
        if (evt.value == "not present") {
            if(state.switches[thisswitch.key] == "ready" 
            	|| state.switches[thisswitch.key] == "active" 
                || state.switches[thisswitch.key] == "activating" ) {
                log.debug "turning on lights $thisswitch"
                state.switches[thisswitch.key] == "active"
                turnSwitchOn(thisswitch.key)
                // We don't wait until the person arrive back, but instead start timer immediately.
                setActiveAndSchedule()
            }
        } 
        //log.debug "state: " + state.myState
    }
    logStates()
}

def setActiveAndSchedule() {
    unschedule()
/* 	state.myState = "active" */
    state.inactiveAt = now()
	schedule("0 * * * * ?", "scheduleCheck")
    log.debug "Scheduled new timer"
}

