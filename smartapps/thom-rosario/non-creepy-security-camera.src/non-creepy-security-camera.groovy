/**
 *  Non-Creepy Security Camera
 *  Version 1.0
 *  Copyright 2016 Thom Rosario
 *  Based on 
 *  "Foscam Mode Alarm"     Copyright 2014 skp19 and
 *  "Photo Burst When..."   Copyright 2013 SmartThings
 *
 *  I wanted a non-creepy security camera that would avert it's eyes while I was home.
 *  https://youtu.be/jHsbwY4EPyA?t=25
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
    name: "Non-Creepy Security Camera",
    namespace: "Thom Rosario",
    author: "Thom Rosario",
    category: "Safety & Security",
    description: "Using the Foscam Universal Device Handler created by skp19, this smart app moves your camera to a preset position based on SmartThings Routine activations",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Solution/camera.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Solution/camera@2x.png"
)

preferences {
	section("When the mode changes to...") {
		input ("alarmMode", "mode", multiple: true, title:"Which mode?")
	}
    section("Move this camera...") {
		input ("camera", "capability.imageCapture", multiple: true, title:"Which camera?")
	}
	section("To this preset...") {
		input ("newPosition", "text", title:"Which preset position?", required: true, defaultValue: "3")
	}
	section("... and return to this position...") {
		input ("origPosition", "text", title: "Which preset position?", required: false, defaultValue: "1")
	}
}

def installed() {
	log.debug "Installed with settings: ${settings}"
	subscribeToEvents()
}

def updated() {
	log.debug "Updated with settings: ${settings}"
	unsubscribe()
	subscribeToEvents()
}

def subscribeToEvents() {
    subscribe(location, "mode", modeAlarmHandler)
}

def modeAlarmHandler(evt) {
	log.debug "Mode changed to ${evt.value}."
    if (evt.value in alarmMode) {
        log.debug "Moving to position $newPosition"
		switch (newPosition) {
		    case "1":
		        camera?.preset1()
		        break
		    case "2":
		        camera?.preset2()
		        break
		    case "3":
		        camera?.preset3()
		        break
		    default:
		        camera?.preset3()
		}		
    }
    else {
        log.debug "Returning to position $origPosition"
		switch (origPosition) {
		    case "1":
		        camera?.preset1()
		        break
		    case "2":
		        camera?.preset2()
		        break
		    case "3":
		        camera?.preset3()
		        break
		    default:
		        camera?.preset1()
		}		
    }
}
