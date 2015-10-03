/**
 *  Double Duty
 *
 *  Copyright 2015 Pasquale Ranalli
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */
definition(
    name: "Double Duty",
    namespace: "pranalli",
    author: "Pasquale Ranalli",
    description: "This app allows you to use redundant \"off\" and, optionally, \"on\" switch presses to control secondary lights.  You paid a lot for those switches, make them work double duty!",  
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Meta/light_outlet.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Meta/light_outlet@2x.png"
)

preferences {
	section("The switch whose off button will become a toggle") {
		input "master", "capability.switch", title: "Where?"
	}
    section("The switch(es) to be toggled") {
		input "switches", "capability.switch", multiple: true, required: false
	}
    section("Optional") {
		input "alsoUseOn", "bool", required: false, defaultValue: false, title: "Also use on button?"
	}
}

def installed()
{
	subscribe(master, "switch", switchHandler, [filterEvents: false])
}

def updated()
{
	unsubscribe()
	subscribe(master, "switch", switchHandler, [filterEvents: false])
}

def switchHandler(evt) {
	if (evt.physical) {
    
       	boolean isStateChange = evt.isStateChange()
       	log.debug "Master Switch Changed State: ${isStateChange}"

       	boolean isOff = master.latestState("switch").value == "off"
       	log.debug "Master Switch Currently Off: ${isOff}"
        
        log.debug "Use on switch selected: ${alsoUseOn}"

		// If the state did not change from the last press, we know this is a redundant event.
        // If the user selected to use the on button also, then we don't care about the current
        // state of the switch and can toggle.  Otherwise, we only toggle if the current switch 
        // state is "off"
       	if ((alsoUseOn || isOff) && !isStateChange) {
       		log.debug "Criteria met, let's toggle the switches"
      		toggleSwitches()
      	}
	}	
}

private toggleSwitches() {
	// If we encounter ANY slave switches that are currently on, then let's send an "off" command
    // so that we can start at a fresh baseline.  This prevents the situation where there is a mixed
    // state of slave switches toggling differently.  
    boolean turnOn = switches.every { it.latestState("switch").value == "off" }
    turnOn ? switches*.on() : switches*.off()
}