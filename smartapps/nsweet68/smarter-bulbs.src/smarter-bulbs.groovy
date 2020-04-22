/**
 *  Smarter Bulbs
 *
 *  Copyright 2016 Nick Sweet.
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

/************
 * Metadata *
 ************/
definition(
	name: "Smarter Bulbs",
	namespace: "nsweet68",
	author: "nick@sweet-stuff.cc",
	description: "Save the state of a bunch of bulbs and reset when 'Canary' bulb turns on",
	category: "My Apps",
	iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/App-LightUpMyWorld.png",
	iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/App-LightUpMyWorld@2x.png"
)
preferences {
    section("Canary Bulb") {
        input "canary", "capability.switch", title: "Who sings?"
    }
    section("Zigbee bulbs to monitor") {    
        input "slaves","capability.switch", multiple: true
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
    subscribe(slaves, "switch", saveStates)
    subscribe(canary,"switch.on", checkRestore)
    pollCanary()
    saveStates() 
	runEvery5Minutes(checkRestore)
    
}

def saveStates(evt) {
	log.debug "Checking States"
    if ("off" == canary.currentSwitch ) {
    	def lightsOff = [:]
    	slaves?.each {
			if (it.currentSwitch == "off"){
        	log.debug "${it.displayName} value ${it.currentSwitch}" 
        	lightsOff[it.id]="off"
        	}
		}
   	state.lOff = lightsOff
	}
}

def checkRestore(evt) {
    log.debug "Checking Restore"  
    pollCanary() 
    log.debug "Canary is ${canary.currentSwitch}"
    if ("on" == canary.currentSwitch) { 
    	log.debug "Turning stuff off"
        restoreState()
        canary.off()
        }
    pollSlaves()
    saveStates(evt)
}

private restoreState() {
  slaves?.each {
				if (state.lOff[it.id] == "off") {
                log.debug "turning $it.label off"
				it.off()
                }
			}
		}

private pollCanary() {
    def hasPoll = canary.hasCommand("poll")
    if (hasPoll) {
    	canary.poll()
        log.debug "Poll canary"
    }
    else
    {
    	canary.refresh()
        log.debug "Refresh canary"
    }
}

private pollSlaves() {

slaves.each {slave ->
def hasPoll = slave.hasCommand("poll")
    if (hasPoll) {
    	slave.poll()
        log.debug "Poll ${slave.displayName}"
    }
    else
    {
    	slave.refresh()
        log.debug "Refresh ${slave.displayName}"
    }
}
    
}
