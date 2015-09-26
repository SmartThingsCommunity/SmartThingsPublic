/**
 *  Copyright 2015 SmartThings
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
 *  Better Double Tap
 *
 *  A couple important notes about this app:  
 *
 *      * First, this app expects that you are using a switch that will send on/off events even when the switch state
 *        is not changing.  For example, the first press turns the main lights on and sends an "on" event, but when 
 *        the second "Tap" is pressed, some switches will check the state of the switch, see that it is already on,
 *        and suppress the event.  This is no good.  In this case, you must customize your device type and look for 
 *        the variable "canBeCurrentState" (or something similar) and set it to true.  
 *
 *      * Second, due to cloud processing, the events are registered painfully slow and if you double tap too fast, 
 *        often times the second event will never even be sent.  This version of the app allows you to specify a 
 *        "window of opportunity" so-to-speak and defaults it to 10 seconds.  The original Double Tap app used 4 
 *        seconds and I just found that to be totally inadequate (unfortunately).  I would recommend setting it even
 *        higher, I personally use 30 seconds.  The presses cannot be done fast either, I would recommend pressing
 *        once, counting 2 Mississippi, and then doing the second press.  If timed correctly, it will properly turn
 *        on/off the linked devices.  This is just what we have to live with until an app like this can be processed
 *        locally rather than in the cloud.
 *
 *  Author: SmartThings
 *  Tweaked by: Pasquale Ranalli
 */
definition(
    name: "Better Double Tap",
    namespace: "pranalli",
    author: "Pasquale Ranalli",
    description: "Turn on or off any number of switches when an existing switch is tapped twice in a row.",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Meta/light_outlet.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Meta/light_outlet@2x.png"
)

preferences {
	section("When this switch is double-tapped...") {
		input "master", "capability.switch", title: "Where?"
	}
	section("Window of time for double-tap in seconds (default 10)") {
		input "timing", "10" 
	}
	section("Turn on or off all of these switches as well") {
		input "switches", "capability.switch", multiple: true, required: false
	}
	section("And turn off but not on all of these switches") {
		input "offSwitches", "capability.switch", multiple: true, required: false
	}
	section("And turn on but not off all of these switches") {
		input "onSwitches", "capability.switch", multiple: true, required: false
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
	log.info evt.value
    
    // Convert the user-provided timing to milliseconds
    def timing = settings.timing.toInteger() * 1000
    log.info timing

	// use Event rather than DeviceState because we may be changing DeviceState to only store changed values
	def recentStates = master.eventsSince(new Date(now() - timing), [all:true, max: 10]).findAll{it.name == "switch"}
	log.debug "${recentStates?.size()} STATES FOUND, LAST AT ${recentStates ? recentStates[0].dateCreated : ''}"

	if (evt.physical) {
		if (evt.value == "on" && lastTwoStatesWere("on", recentStates, evt)) {
			log.debug "detected two taps, turn on other light(s)"
			onSwitches()*.on()
		} else if (evt.value == "off" && lastTwoStatesWere("off", recentStates, evt)) {
			log.debug "detected two taps, turn off other light(s)"
			offSwitches()*.off()
		}
	}
	else {
		log.trace "Skipping digital on/off event"
	}
}

private onSwitches() {
	(switches + onSwitches).findAll{it}
}

private offSwitches() {
	(switches + offSwitches).findAll{it}
}

private lastTwoStatesWere(value, states, evt) {
	def result = false
	if (states) {

		log.trace "unfiltered: [${states.collect{it.dateCreated + ':' + it.value}.join(', ')}]"
		def onOff = states.findAll { it.physical || !it.type }
		log.trace "filtered:   [${onOff.collect{it.dateCreated + ':' + it.value}.join(', ')}]"

		// This test was needed before the change to use Event rather than DeviceState. It should never pass now.
		if (onOff[0].date.before(evt.date)) {
			log.warn "Last state does not reflect current event, evt.date: ${evt.dateCreated}, state.date: ${onOff[0].dateCreated}"
			result = evt.value == value && onOff[0].value == value
		}
		else {
			result = onOff.size() > 1 && onOff[0].value == value && onOff[1].value == value
		}
	}
	result
}
