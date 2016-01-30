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
 * Roommate Warning
 *
 *  Author: erudition3000
 *
 *  blinks a light to warn a roommate when you are home
 *
 *  Date: 2013-02-21
 */

definition(
	name: "Roommate Warning",
	namespace: "smartthings",
	author: "SmartThings",
	description: "blinks a light to warn a roommate when you are home",
	category: "Convenience",
	iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/smartlights.png",
	iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/smartlights%402x.png"
)

preferences {
	section("Tell me when these people are home"){
		input "presenceSensors", "capability.presenceSensor", required: true, multiple: true
	}
	section("By flashing these lights") {
		input "switches", "capability.switch", required: true, multiple: true, title: "Which lights?"
        input "numTimes", "number", range: "2..10", required: false, defaultValue: 2, title: "this many times (2..10)"
        input "onFor", "number", range: "100..5000", required: false, defaultValue: 3000, title: "On time in msec (100..5000)"
        input "offFor", "number", range: "100..5000", required: false, defaultValue: 3000, title: "Off time in msec (100..5000)"
	}
}

def installed()
{
	log.trace "Installed with settings: ${settings}"
	initialize()
}

def updated()
{
	log.trace "Updated with settings: ${settings}"
	unsubscribe()
	initialize()
}

def initialize() {
	state.numberOfFlashes = (numTimes) ? numTimes.toInteger() : 2
    state.onFor = (onFor) ? onFor.toInteger() : 3000
    state.offFor = (offFor) ? offFor.toInteger() : 3000
    //state.numberOfFlashes = 2
	subscribe(presenceSensors, "presence.present", presenceHandler)
}

def presenceHandler(evt) {
	flashLights()
}

private flashLights() {
	def doFlash = true
	//def onFor = onFor ?: 3000
	//def offFor = offFor ?: 3000
	//def numFlashes = numFlashes ?: 3

	log.debug "LAST ACTIVATED IS: ${state.lastActivated}"
	if (state.lastActivated) {
		def elapsed = now() - state.lastActivated
		def sequenceTime = (state.numberOfFlashes + 1) * (onFor + offFor)
		doFlash = elapsed > sequenceTime
		log.debug "DO FLASH: $doFlash, ELAPSED: $elapsed, LAST ACTIVATED: ${state.lastActivated}"
	}

	if (doFlash) {
		log.debug "FLASHING ${state.numberOfFlashes} times"
		state.lastActivated = now()
		log.debug "LAST ACTIVATED SET TO: ${state.lastActivated}"
		def initialStateOn = switches.collect{it.currentSwitch == "on"}
        def initialLevel = switches.collect{it.currentLevel}
		
        def delay = 0L
        state.numberOfFlashes.times {
			log.trace "Flash ${it} -- at $delay msec"
			switches.eachWithIndex {s, i ->
				if (initialStateOn[i]) {
                	//first off, then on
                	s.off(delay:delay)
                    if(initialLevel[i]!=null) {
                    	s.setLevel(100, [delay: delay+state.offFor])
                    } else {
						s.on(delay: delay+state.offFor)
                    }
				}
				else {
                	//first on, then off
                	if(initialLevel[i]!=null) {
                    	s.setLevel(100, [delay: delay])
                    } else {
						s.on(delay: delay)
                    }
					s.off(delay:delay+state.onFor)
				}
			}
            delay += state.onFor + state.offFor
        }
        
        //set dim level back if we leave the light on
        switches.eachWithIndex {s, i ->
            if (initialLevel[i]!=null && initialStateOn[i]) {
                s.setLevel(initialLevel[i].toInteger(), [delay: delay])
                log.trace "    Set level to ${initialLevel[i]}  $delay msec"
            }
        }
        
	}
}