/**
 *  Good Night House
 *
 *  Copyright 2014 Joseph Charette
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
    name: "Good Night House",
    namespace: "charette.joseph@gmail.com",
    author: "Joseph Charette",
    description: "Some on, some off with delay for bedtime, Lock The Doors",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png"
/**
*  Borrowed code from
*  Walk Gentle Into That Good Night
*
*  Author: oneaccttorulethehouse@gmail.com
*  Date: 2014-02-01
 */
 )
preferences {
	section("When I touch the app turn these lights off…"){
		input "switchesoff", "capability.switch", multiple: true, required:true
	}
    section("When I touch the app turn these lights on…"){
		input "switcheson", "capability.switch", multiple: true, required:false
	}
    section("Lock theses locks...") {
		input "lock1","capability.lock", multiple: true
    }
	section("And change to this mode...") {
		input "newMode", "mode", title: "Mode?"
	}
   section("After so many seconds (optional)"){
		input "waitfor", "number", title: "Off after (default 120)", required: true
	}
}


def installed()
{
	log.debug "Installed with settings: ${settings}"
	log.debug "Current mode = ${location.mode}"
	subscribe(app, appTouch)
}


def updated()
{
	log.debug "Updated with settings: ${settings}"
	log.debug "Current mode = ${location.mode}"
	unsubscribe()
	subscribe(app, appTouch)
}

def appTouch(evt) {
	log.debug "changeMode, location.mode = $location.mode, newMode = $newMode, location.modes = $location.modes"
    if (location.mode != newMode) {
   			setLocationMode(newMode)
			log.debug "Changed the mode to '${newMode}'"
    }	else {
    	log.debug "New mode is the same as the old mode, leaving it be"
    	}
    log.debug "appTouch: $evt"
    lock1.lock()
    switcheson.on()
    def delay = (waitfor != null && waitfor != "") ? waitfor * 1000 : 120000
	switchesoff.off(delay: delay)
}
