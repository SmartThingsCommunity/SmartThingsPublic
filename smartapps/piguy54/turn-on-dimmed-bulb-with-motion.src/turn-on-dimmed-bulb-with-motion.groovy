/**
 *  Turn On Dimmed Bulb With Motion
 *
 *  Copyright 2015 Allen Brown
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
    name: "Turn On Dimmed Bulb With Motion",
    namespace: "piguy54",
    author: "Allen Brown",
    description: "Do you have bulbs that Smart Lighting can't dim (looking at you GE Link)? In that case this does it. Simple implementation of turn on, on motion, at a set dimmer level and off again after inactive time.",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
	section("When there's movement turn on...") {
		input "motion1", "capability.motionSensor", title: "Where?", multiple: true
	}
    section("At what dimmer level..."){
		input "dimmerLevel", "number", title: "Dim Level?"
	}
    section("And off when no motion for..."){
		input "minutes1", "number", title: "Minutes?"
	}
	section("Turn on/off lights..."){
		input "switch1", "capability.switch", multiple: true
	}
}

def installed() {
	subscribe(motion1, "motion", motionHandler)
}

def updated() {
	unsubscribe()
	subscribe(motion1, "motion", motionHandler)
}

def motionHandler(evt) {
	log.debug "$evt.name: $evt.value"
	if (evt.value == "active") {
		log.debug "turning on lights"
		switch1.on()
        switch1.setLevel(dimmerLevel)
	} else if (evt.value == "inactive") {
		runIn(60 * minutes1, scheduleCheck, [overwrite: false])
	}
}

def scheduleCheck() {
	log.debug "schedule check"
	def motionState = motion1.currentState("motion")
    log.debug "$motionState.value"
    if (!motionState.contains("active")) {
        def elapsed = now() - motionState.rawDateCreated.time
    	def threshold = 1000 * 60 * minutes1 - 1000
    	if (elapsed >= threshold) {
            log.debug "Motion has stayed inactive long enough since last check ($elapsed ms):  turning lights off"
            switch1.off()
    	} else {
        	log.debug "Motion has not stayed inactive long enough since last check ($elapsed ms):  doing nothing"
        }
    } else {
    	log.debug "Motion is active, do nothing and wait for inactive"
    }
}