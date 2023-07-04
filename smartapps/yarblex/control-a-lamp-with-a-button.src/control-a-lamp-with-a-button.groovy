/**
 *  Control a lamp with a button
 *
 *  Copyright 2020 Shane Spencer
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
    name: "Control a lamp with a button",
    namespace: "Yarblex",
    author: "Shane Spencer",
    description: "Use button to toggle lamp on to 100% or off, double press and button held dims light to user defined values",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
	section("Pick a Button") {
    	input "thebutton", "capability.button", required: true, title: "Which Button?"
        }
    section("Pick a Light") {
    	input "thelight", "capability.switch", required: true, title: "Which Light?"
		}
    section("Double click light level:") {
    	input(name: "ButtonDoubleClickedLevel", type: "number", title: "Light brightness when button is double clicked. ")
        }
    section("Button Held light level:") {
    	input(name: "ButtonHeldLightLevel", type: "number", title: "Light brigtness when button is held.")
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
	subscribe(thebutton, "Button.pushed", buttonPushedHandler)
    subscribe(thebutton, "Button.double", buttonDoubleHandler)
    subscribe(thebutton, "Button.held", buttonHeldHandler)
}
def buttonPushedHandler(evt) {
	log.debug "buttonPushedHandler called: $evt"
    
    def lightonoff = thelight.currentState("switch")
    
    if (lightonoff.value == "on"){
    	log.debug "If statment returned true"
        thelight.off()
      } else {
      	log.debug "If statment false"
        thelight.setLevel("100")
        thelight.on()
      }
}
def buttonDoubleHandler(evt) {
	log.debug "Double button handler was called"
	thelight.setLevel(ButtonDoubleClickedLevel)
	thelight.on()
    }
def buttonHeldHandler(evt) {
	log.debug "Button held event was called"
    thelight.setLevel(ButtonHeldLightLevel)
    thelight.on()
    }

