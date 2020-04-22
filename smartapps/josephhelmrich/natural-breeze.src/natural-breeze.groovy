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
    iconX3Url: "http://cdn.device-icons.smartthings.com/Lighting/light24-icn@2x.png"
    )

preferences {
    section("Setup") {
        input "runMode", "mode", title: "Start Nature Breeze in these Modes", required: true, multiple: true
        input "fans", "capability.switchLevel", title: "Cycle These Ceiling Fans", required: true, multiple: true
        input "offDelay", "number", title: "How often (seconds) do you want to cycle the fan(s)?", required: true, defaultValue: 180
        input "onDelay", "number", title: "How long do you run the fan(s)?", required: true, defaultValue: 5
        input "fanLevel", "number", title: "What speed should the fans run?", required: false
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
    	log.info "Started"
    	timerHandler()
    }
    else
    {
    	if (fans != null) {
    		fans.off()
       	}
    	log.info "Stopped"
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
            if (null != fanLevel && 0 != fanLevel) {
                fans.setLevel(0)
            }
            else {
                fans.off()
            }
            runIn(offDelay, timerHandler) 
        }
        else {

            if (null != fanLevel && 0 != fanLevel) {
                log.info "Cycled Fans: ${fanLevel}%"
                fans.setLevel(fanLevel)
            }
            else {
                log.info "Cycled Fans"
                fans.on()
            }            
            runIn(onDelay, timerHandler)
        }
    }
}