/**
 *  Smart Bulb Controlled By Smart Switch 
 *
 *  Copyright 2016 Oscar Chen
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
    name: "Smart Bulb Controlled By Smart Switch",
    namespace: "airoscar",
    author: "Oscar Chen",
    description: "Allowing smart bulbs to be controlled by smart switch.",
    category: "Convenience",
    iconUrl: "http://cdn.device-icons.smartthings.com/Lighting/light11-icn.png",
    iconX2Url: "http://cdn.device-icons.smartthings.com/Lighting/light11-icn@2x.png",
    iconX3Url: "http://cdn.device-icons.smartthings.com/Lighting/light11-icn@2x.png")


preferences {
	section("Switch") {
		input "theSwitch", "capability.switch", required: true
	}
    
    section("Light Bulb") {
    	input "bulb", "capability.switch", required: true
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
	subscribe(theSwitch, "switch", myHandler)
}

def myHandler(evt) {
	log.debug "myHandler called: $evt"
	if (evt.value == "on") {
        bulb.on()
    } else if (evt.value == "off") {
    	bulb.off()
    }
    

}