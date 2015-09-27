/**
 *  Off as Toggle 
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
 *  IMPORTANT: 
 *  	This Smart App requires that the Master Switch device is set up such that it will report events even if said events 
 *  	are not changing the state of the actual switch.  More specifically, we need to use a device that will report the
 *  	"off" event even when the switch is "off".  You may need to utilize a custom device type to enable this capability
 *  	as switches/dimmers will often suppress redundant events.  For example, in the Zwave Dimmer device type there is a
 *		variable called "canBeCurrentState" that needs to be set to true (default is false) in order for this to occur.
 * 
 *  ABOUT PERFORMANCE: 
 *  	The fact that SmartApps are forced to run through the cloud cause them to perform relatively poorly.  
 * 		Hence, apps like Double Tap are unbearable to use.  This app was created in an effort to provide an 
 * 		alternative to Double Tap (or my version, Better Double Tap) that is 100% reliable.  This app is 
 * 		100% reliable, but keep in mind that due to the need for the event to travel to the cloud first, there
 * 		may be a delay (can be upwards of 15 seconds) for the secondary switches to toggle.  Don't fret, I 
 *		promise they will toggle :)  
 */
definition(
    name: "Off as Toggle ",
    namespace: "pranalli",
    author: "Pasquale Ranalli",
    description: "This app allows you to re-purpose the \"off\" button of a switch or dimmer as a toggle for a secondary device or devices.",  
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Meta/light_outlet.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Meta/light_outlet@2x.png"
)

preferences {
	section("The switch whose off button will be re-purposed") {
		input "master", "capability.switch", title: "Where?"
	}
    section("The switch(es) to be toggled") {
		input "switches", "capability.switch", multiple: true, required: false
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

    boolean isStateChange = evt.isStateChange()
    log.debug "Master Switch Changed State: ${isStateChange}"
    
    boolean isOff = master.latestState("switch").value == "off"
    log.debug "Master Switch Currently Off: ${isOff}"
    
    // If the Master Switch is currently off and the given event did not result in a state change,
    // then we know that the off button was pressed while the switch was off.  Good.  Now, let's
    // toggle the slave switches!  
    if (isOff && !isStateChange) {
    	log.debug "Current and prior state were off, let's toggle the switches"
        toggleSwitches()
    }
}

private toggleSwitches() {
    switches.each {
        it.latestState("switch").value == "off" ? it.on() : it.off()
    }
}
