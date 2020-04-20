/**
 *  Copyright 2015 Brian Todoroff
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
 *  Party Lights
 *
 *  Author: Brian Todoroff
 *
 *  Date: 2016-12-06
 */
definition(
	name: "Party Lights",
	namespace: "btodoroff",
	author: "Brian Todoroff",
	description: "Flashes a set of lights",
	category: "Convenience",
	iconUrl: "https://s3.amazonaws.com/smartapp-icons/Meta/light_outlet.png",
	iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Meta/light_outlet@2x.png"
)

preferences {
	section("When this switch is turned on or off") {
		input "master", "capability.switch", title: "Where?"
	}
	section("At start, turn off all of these switches") {
		input "offSwitches", "capability.switch", multiple: true, required: false
	}
	section("And turn on all of these switches") {
		input "onSwitches", "capability.switch", multiple: true, required: false
	}  	
    section("Start randomly toggling these switches") {
		input "switches", "capability.switch", multiple: true, required: true
	}

}

def installed()
{   
	initialize()
}

def updated()
{
	unsubscribe()
	initialize()
}

def initialize()
{
	state.flashing = false;
    state.random = new Random()
    state.on = false;
    state.light = 0;
    subscribe(master, "switch.on", onHandler)
	subscribe(master, "switch.off", offHandler)
}

def logHandler(evt) {
	log.debug evt.value
}

def onHandler(evt) {
	log.debug evt.value
	log.debug onSwitches
	onSwitches?.on()
    offSwitches?.off()
    state.flashing = true
    runIn(1,flashHandler)
}

def offHandler(evt) {
	log.debug evt.value
	log.debug offSwitches
	state.flashing = false
}

def flashHandler()
{
 	def curLight = switches[new Random().nextInt(switches.size())]
    def curValue = curLight.currentValue("switch")
    log.debug curLight
    if(curValue == "on")
    {
    	curLight.off()
    }
    else
    {
    	curLight.on()
    }
	//switches[state.light].off()
	//state.light = state.light+1
    //if(state.light > switches.size()-1) state.light = 0
    //log.debug state.light
    //switches[state.light].on()
	if(state.flashing == true) runIn(1,flashHandler)
}

