/**
 *  Light Switch Force Sync
 *
 *  Copyright 2015 Alex Lee Yuk Cheung
 *
 * 	Created because of Osram bulbs turning on randomly. If Osram bulbs are grouped and controlled via a master virtual switch,
 *	this smart app enables the individual bulbs to be checked and the bulb state (on/off) synchronised with the state of the master virtual switch.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 *	VERSION HISTORY
 *  20.01.2016
 *	v1.0 - Initial Release
 */
 
definition(
		name: "Light Switch Force Sync",
		namespace: "alyc100",
		author: "Alex Lee Yuk Cheung",
		description: "If you have a set of lights activated on a master switch. Poll to ensure states are updated.",
        category:    "My Apps",
                iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
                iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
                iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png"
) 

preferences {
	page(name: "configurePage")
  
}

def configurePage() {
	dynamicPage(name: "configurePage", title:"Setup", install: true, uninstall: true) {
    	section {
    		input("lights", "capability.switch", title: "For these lights",  multiple: true, required: true)
  		}
        
        section {
            input("masterSwitch", "capability.switch", title: "Synchronise to this master switch", multiple: false, required: true)
        }
    }
}

// App lifecycle hooks

def installed() {
	// Check for new devices and remove old ones every 3 hours
    // execute handlerMethod every 10 minutes.
    schedule("0 0/5 * * * ?", syncLightsToSwitch)
}

// called after settings are changed
def updated() {
	log.debug "Executing 'updated()'"
    unschedule(syncLightsToSwitch)
    schedule("0 0/5 * * * ?", syncLightsToSwitch)
}

def uninstalled() {
	log.info("Uninstalling, removing child devices...")
	unschedule(syncLightsToSwitch)
}

def syncLightsToSwitch() {
	log.debug "Executing 'syncLightsToSwitch()'"
    lights.refresh()
    runIn(10, updateLightState)
}

def updateLightState() {
	log.debug "masterSwitchState: $masterSwitch.currentSwitch"
    	for (light in lights) {
    	if (masterSwitch.currentSwitch == "on") {
        	if (light.currentSwitch == "off") {
    			light.on()
            }
    	} else {
        	if (light.currentSwitch == "on") {
    			light.off()
            }
    	}
    }
}