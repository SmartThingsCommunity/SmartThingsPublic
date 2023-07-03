/**
 *  Switch Sync
 *
 *  Copyright 2016 Kelly Kristek
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
    name: "Switch Sync",
    namespace: "Electricview",
    author: "Kelly Kristek",
    description: "This is an app that attempts to keep your virtual 3 way switches in sync regardless of where they are iniated from.\r\n",
    category: "Convenience",
    iconUrl: "http://i230.photobucket.com/albums/ee70/pickyassgamer/switchsyncsmall.png",
    iconX2Url: "http://i230.photobucket.com/albums/ee70/pickyassgamer/switchsyncmedium.png",
    iconX3Url: "http://i230.photobucket.com/albums/ee70/pickyassgamer/switchsync.png")


preferences {
	section("Select Mains Switch") {
		input "firstswitch", "capability.switch", required: true
	}
    section("Select AUX switch") {
    	input "secondswitch", "capability.switch", required: true
        }
}

def installed() {
	log.debug "Installed with settings: ${settings}"

	initialize()
}

def updated() {
	log.debug "Updated with settings: ${settings}"

	unsubscribe()
	initialize()
}

def initialize() {
	subscribe(firstswitch, "switch.on", firstswitchon)
    subscribe(firstswitch, "switch.off", firstswitchoff)
    subscribe(secondswitch,"switch.on", secondswitchon)
    subscribe(secondswitch,"switch.off", secondswitchoff)
}
def firstswitchon(evt){
	log.debug "FirstSwitchOn called: $evt"
    
    // Adding an if statement to verify the switch isn't already in the position we desire, to avoid endless loops
    if( "off" == secondswitch.currentSwitch) { 
        secondswitch.on()
    }
}

def firstswitchoff(evt){
	log.debug "FirstSwitchOff called: $evt"
    
    // Adding an if statement to verify the switch isn't already in the position we desire, to avoid endless loops
    if ( "on" == secondswitch.currentSwitch){
    secondswitch.off()
    }
}

def secondswitchon(evt){
	log.debug "SecondSwitchOn called: $evt"
    
    // Adding an if statement to verify the switch isn't already in the position we desire, to avoid endless loops
    if ( "off" == firstswitch.currentSwitch){
    firstswitch.on()
    }
}

def secondswitchoff(evt){
	log.debug "SecondSwitchOff called: $evt"
    
    // Adding an if statement to verify the switch isn't already in the position we desire, to avoid endless loops
    if ( "on" == firstswitch.currentSwitch){
    firstswitch.off()
    }
}

// TODO: implement event handlers