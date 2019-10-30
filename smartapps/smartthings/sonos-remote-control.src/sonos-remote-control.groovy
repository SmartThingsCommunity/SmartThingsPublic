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
 *  Sonos Remote Control
 *
 *  Author: Matt Nohr
 *  Date: 2014-04-14
 */
 
/**
 * Buttons:
 * 1  2
 * 3  4
 *
 * Pushed:
 * 1: Play/Pause
 * 2: Volume Up
 * 3: Next Track
 * 4: Volume Down
 *
 * Held:
 * 1:
 * 2: Volume Up (2x)
 * 3: Previous Track
 * 4: Volume Down (2x)
 */

definition(
    name: "Sonos Remote Control",
    namespace: "smartthings",
    author: "SmartThings",
    description: "Control your Sonos system with an Aeon Minimote",
    category: "SmartThings Internal",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience%402x.png"
)

preferences {
	section("Select your devices") {
		input "buttonDevice", "capability.button", title: "Minimote", multiple: false, required: true
        input "sonos", "capability.musicPlayer", title: "Sonos", multiple: false, required: true
	}
    section("Options") {
    	input "volumeOffset", "number", title: "Adjust Volume by this amount", required: false, description: "optional - 5% default"
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
	subscribe(buttonDevice, "button", buttonEvent)
}

def buttonEvent(evt){
	def buttonNumber = evt.data
	def value = evt.value
    log.debug "buttonEvent: $evt.name = $evt.value ($evt.data)"
	log.debug "button: $buttonNumber, value: $value"
    
    def recentEvents = buttonDevice.eventsSince(new Date(now() - 2000)).findAll{it.value == evt.value && it.data == evt.data}
    log.debug "Found ${recentEvents.size()?:0} events in past 2 seconds"
    
    if(recentEvents.size <= 1){
        handleButton(extractButtonNumber(buttonNumber), value)
    } else {
    	log.debug "Found recent button press events for $buttonNumber with value $value"
    }
}

def extractButtonNumber(data) {
	def buttonNumber
    //TODO must be a better way to do this. Data is like {buttonNumber:1}
    switch(data) {
        case ~/.*1.*/:
            buttonNumber = 1
            break
        case ~/.*2.*/:
            buttonNumber = 2
            break
        case ~/.*3.*/:
            buttonNumber = 3
            break
        case ~/.*4.*/:
            buttonNumber = 4
            break
    }
    return buttonNumber
}

def handleButton(buttonNumber, value) {
	switch([number: buttonNumber, value: value]) {
        case{it.number == 1 && it.value == 'pushed'}:
            log.debug "Button 1 pushed - Play/Pause"
            togglePlayPause()
            break
        case{it.number == 2 && it.value == 'pushed'}:
            log.debug "Button 2 pushed - Volume Up"
            adjustVolume(true, false)
            break
        case{it.number == 3 && it.value == 'pushed'}:
            log.debug "Button 3 pushed - Next Track"
            sonos.nextTrack()
            break
        case{it.number == 4 && it.value == 'pushed'}:
            log.debug "Button 4 pushed - Volume Down"
			adjustVolume(false, false)
            break
        case{it.number == 2 && it.value == 'held'}:
            log.debug "Button 2 held - Volume Up 2x"
            adjustVolume(true, true)
            break
        case{it.number == 3 && it.value == 'held'}:
	        log.debug "Button 3 held - Previous Track"
            sonos.previousTrack()
        	break  
        case{it.number == 4 && it.value == 'held'}:
            log.debug "Button 4 held - Volume Down 2x"
            adjustVolume(false, true)
            break
        default:
            log.debug "Unhandled command: $buttonNumber $value"
           
    }
}

def togglePlayPause() {
	def currentStatus = sonos.currentValue("status")
    if (currentStatus == "playing") {
        options ? sonos.pause(options) : sonos.pause()
    }
    else {
        options ? sonos.play(options) : sonos.play()
    }
}

def adjustVolume(boolean up, boolean doubleAmount) {
	def changeAmount = (volumeOffset ?: 5) * (doubleAmount ? 2 : 1)
    def currentVolume = sonos.currentValue("level")
    
    if(up) {
    	sonos.setLevel(currentVolume + changeAmount)
    } else {
	    sonos.setLevel(currentVolume - changeAmount)
    }
}
