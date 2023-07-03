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
 */
definition(
    name: "Fibaro Dimmer Scenes",
    namespace: "My Apps",
    author: "Elnar Hajiyev",
    description: "Smart app that allows to control switches with changes of Fibaro Dimmer Scenes.",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Meta/light_outlet.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Meta/light_outlet@2x.png"
)

preferences {
    section("Listen to scenes from this Fibaro Dimmer devices...") {
    	input "fibaroDevicesSet", "capability.switch", title: "Fibaro Dimmers?", multiple: true, required: true
    }
    section("When any of these scenes are activated...") {
        input "scenesSet1", "enum", title: "Fibaro Dimmer scenes (set 1)?", multiple: true, required: false,
              options: ["10","11","12","13", "14", "15", "16", "17", "18", "20", "21", "22", "23", "24", "25", "26"]
    }
    section("Turn on all of these switches") {
		input "switchesSet1", "capability.switch", multiple: true, required: false
	}
    section("When any of these scenes are activated...") {
        input "scenesSet2", "enum", title: "Fibaro Dimmer scenes (set 2)?", multiple: true, required: false,
              options: ["10","11","12","13", "14", "15", "16", "17", "18", "20", "21", "22", "23", "24", "25", "26"]
	}
    section("Turn off all of these switches") {
		input "switchesSet2", "capability.switch", multiple: true, required: false
	}
    section("When any of these scenes are activated...") {
        input "scenesSet3", "enum", title: "Fibaro Dimmer scenes (set 3)?", multiple: true, required: false,
              options: ["10","11","12","13", "14", "15", "16", "17", "18", "20", "21", "22", "23", "24", "25", "26"]
	}
    section("Toggle all of these switches") {
		input "switchesSet3", "capability.switch", multiple: true, required: false
	}
}

def installed()
{
	subscribe(fibaroDevicesSet, "scene", sceneHandler, [filterEvents: false])
}

def updated()
{
	unsubscribe()
	subscribe(fibaroDevicesSet, "scene", sceneHandler, [filterEvents: false])
}

def sceneHandler(evt) {
	log.debug evt.value
    log.debug evt.data
    
    if(scenesSet1 && scenesSet1.contains(evt.value)) {
    	switchesSet1.on()
    }
    else if(scenesSet2 && scenesSet2.contains(evt.value)) {
    	switchesSet2.off()
    }
    else if(scenesSet3 && scenesSet3.contains(evt.value)) {
    	toggle(switchesSet3)
    }
}

def toggle(devices) {
    log.debug "toggle: $devices = ${devices*.currentValue('switch')}"
	
    if (devices*.currentValue('switch').contains('off')) {
        devices.on()
    }
    else if (devices*.currentValue('switch').contains('on')) {
        devices.off()
    }
    else {
        devices.on()
    }
}