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
 *  Author: SmartThings
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
    //state.numberOfFlashes = 2
	subscribe(presenceSensors, "presence.present", presenceHandler)
}

def presenceHandler(evt) {
	log.trace "presence"
	flashLights()
}

private flashLights() {
	def doFlash = true
	def onFor = onFor ?: 3000
	def offFor = offFor ?: 3000
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
		def initialActionOn = switches.collect{it.currentSwitch != "on"}
        def initialLevel = switches.collect{it.currentLevel}
		def delay = 0L
		state.numberOfFlashes.times {
			log.trace "Switch on after  $delay msec"
			switches.eachWithIndex {s, i ->
				if (initialActionOn[i]) {
                	if(initialLevel[i]!=null) {
                    	s.setLevel(100, [delay: delay])
                        log.trace "    Set level to 100  $delay msec"
                    } else {
						s.on(delay: delay)
                    }
				}
				else {
					s.off(delay:delay)
				}
			}
			delay += onFor
			log.trace "Switch off after $delay msec"
			switches.eachWithIndex {s, i ->
				if (initialActionOn[i]) {
					s.off(delay: delay)
				}
				else {
                	if(initialLevel[i]!=null) {
                    	s.setLevel(100, [delay: delay])
                        log.trace "    Set level to 100  $delay msec"
                    } else {
						s.on(delay: delay)
                    }
				}
			}
			delay += offFor
		}
        
        //set dim level back if we leave the light on
        switches.eachWithIndex {s, i ->
            if (initialLevel[i]!=null && !initialActionOn[i]) {
                s.setLevel(initialLevel[i].toInteger(), [delay: delay])
                log.trace "    Set level to ${initialLevel[i]}  $delay msec"
            }
        }
        
	}
}