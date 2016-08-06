/**
 *  Natural Breeze
 *
 *  Copyright 2016 Joe Helmrich
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
    name: "Natural Breeze",
    namespace: "josephhelmrich",
    author: "Joe Helmrich",
    description: "Cycles your ceiling fans on and off to achieve a natural breeze effect",
    category: "Convenience",
    iconUrl: "http://cdn.device-icons.smartthings.com/Lighting/light24-icn@2x.png",
    iconX2Url: "http://cdn.device-icons.smartthings.com/Lighting/light24-icn@2x.png",
    iconX3Url: "http://cdn.device-icons.smartthings.com/Lighting/light24-icn@2x.png")


preferences {
	section("Setup") {
    	input "fans", "capability.switchLevel", title: "Select Your Ceiling Fans", required: true, multiple: true
        input "offDelay", "number", title: "How often (seconds) do you want to cycle the fan?"
        input "onDelay", "number", title: "How long do you run the fan?"
        input "fanLevel", "number", title: "Enter a Fan Level"
        input "runMode", "mode", title: "Only run in these Modes", required: true, multiple: true
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
	subscribe(location, "mode", modeChangeHandler)
}

def modeChangeHandler(evt) {
    def rightMode = runMode.findAll { m ->
        m == location.mode ? true : false
    }    
    if (rightMode)
    {
    	log.info "Starting..."
    	timerHandler()
    }
    else
    {
    	log.info "Stopping..."
    }
}

def timerHandler() {
    def rightMode = runMode.findAll { m ->
        m == location.mode ? true : false
    }    

	if (rightMode)
    {
    	def currSwitches = fans.currentSwitch
		def onSwitches = currSwitches.findAll { switchVal ->
        	switchVal == "on" ? true : false
    	}    
    
    	if (onSwitches) {
        	fans.setLevel(0)
	    	runIn(offDelay, timerHandler) 
        }
        else {
        	log.info "Cycled Fans"
        	fans.setLevel(fanLevel)
	    	runIn(onDelay, timerHandler)
        }
    }
}