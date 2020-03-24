/**
 *  Turn Off With Motion
 *
 *  Copyright 2014 Kristopher Kubicki
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
    name: "Turn Off With Motion",
    namespace: "KristopherKubicki",
    author: "Kristopher Kubicki",
    description: "Turns off a device if there is motion",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")



preferences {
	section("Turn off when there's movement..."){
		input "motion1", "capability.motionSensor", title: "Where?", multiple: true
	}
	section("And on when there's been no movement for..."){
		input "minutes1", "number", title: "Minutes?"
	}
	section("Turn off/on light(s)..."){
		input "switches", "capability.switch", multiple: true
	}
}


def installed()
{
	subscribe(motion1, "motion", motionHandler)
	schedule("0 * * * * ?", "scheduleCheck")
}

def updated()
{
	unsubscribe()
	subscribe(motion1, "motion", motionHandler)
	unschedule()
	schedule("0 * * * * ?", "scheduleCheck")
}

def motionHandler(evt) {
	log.debug "$evt.name: $evt.value"

	if (evt.value == "active") {
		log.debug "turning on lights"
		switches.off()
		state.inactiveAt = null
	} else if (evt.value == "inactive") {
		if (!state.inactiveAt) {
			state.inactiveAt = now()
		}
	}
}

def scheduleCheck() {
	log.debug "schedule check, ts = ${state.inactiveAt}"
	if (state.inactiveAt) {
		def elapsed = now() - state.inactiveAt
		def threshold = 1000 * 60 * minutes1
		if (elapsed >= threshold) {
			log.debug "turning off lights"
			switches.on()
			state.inactiveAt = null
		}
		else {
			log.debug "${elapsed / 1000} sec since motion stopped"
		}
	}
}