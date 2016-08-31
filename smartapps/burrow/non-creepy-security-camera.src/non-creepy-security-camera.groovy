/**
 *  Non-Creepy Security Camera
 *  Version 1.0
 *  Copyright 2016 Thom Rosario
 *  Based on 
 *  "Foscam Mode Alarm" and "Foscam Presence Alarm"     Copyright 2014 skp19 and
 *  "Photo Burst When..."   							Copyright 2013 SmartThings
 *
 *  I wanted a non-creepy security camera that would avert it's eyes while I was home.  It also 
 *  enables/disables the camera motion detection, and will now go to user-specified presets when either
 *  motion is sensed, or a contact triggers either an open or close event.
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
 *
 *  TODO:  
 */

definition (
    name: "Non-Creepy Security Camera",
	parent: "burrow:Smart Burrow",
    namespace: "burrow",
    author: "Thom Rosario",
    category: "Safety & Security",
    description: "Using the Foscam Universal Device Handler created by skp19, this smart app moves your camera to a preset position based on different events.",
    iconUrl: "http://cdn.device-icons.smartthings.com/Entertainment/entertainment9-icn.png",
    iconX2Url: "http://cdn.device-icons.smartthings.com/Entertainment/entertainment9-icn@2x.png"
)

preferences {
	page (name: "privacyPage", title: "Privacy settings", install: false, uninstall: false, nextPage: "intrusionPage") {
	    section ("Which camera?") {
			paragraph "These settings apply to all of the other sections in this app.  When the camera's alert, the motion and sound detection are active, and the camera will return to where it belongs, based on both the active SmartThings mode and the presence sensors you've specified.  This app relies upon Foscam's built-in photo saving modes to get your images to you."
			input ("camera", "capability.imageCapture", multiple: false, title:"Choose a camera.")
			input ("alarmDuration", "number", title: "How many minutes should the camera remain alert?", required: true, defaultValue: "5")
		}
		section ("Non-creepy Mode Settings") {
			paragraph "This mode maintains your privacy by averting the camera's eye when you don't want it watching you.  If the conditions aren't met, it'll return to the position you've chosen."
			input ("nonCreepyModes", "mode", multiple: true, title:"When this mode activates...")
	        input ("presence", "capability.presenceSensor", title: "...and these people are present...", required: false, multiple: true)
			input ("nonCreepyPosition", "number", title:"... move the camera to this privacy preset.", required: true, defaultValue: "3")
		}
	}
	page (name: "intrusionPage", title: "Intrusion Settings", install: false, uninstall: false, nextPage: "appSettings") {
		section ("Motion Settings", hideable: true, hidden: false) {
			paragraph "This section allows you to watch for movement during specific SmartThings modes."
			input ("motion", "capability.motionSensor", multiple: true, title:"Which motion sensor?")
			input ("motionPreset", "number", title:"Which preset should I take photos of?", required: false, defaultValue: "1")
			input ("motionModes", "mode", multiple: true, title:"During which modes should I be on alert?")
		}
		section ("Contact Sensor Settings", hideable: true, hidden: false){
			paragraph "This section allows you to monitor contact sensors (both open and close) during specific SmartThings modes."
			input ("contact", "capability.contactSensor", multiple: true, title: "Which contact sensor?")
			input ("lock", "capability.lock", multiple: true, title: "Which door lock?")
			input ("contactPreset", "number", title: "Which preset should I take photos of?", required: false, defaultValue: "1")
			input ("contactModes", "mode", multiple: true, title: "During which modes should I be on alert?")
		}
	}
    page (name: "appSettings", title: "Application Settings", install: true, uninstall: true) {
	    section ("Notification Settings") {
			input ("recipients", "contact", title: "Who should I notify?", required: false) {
	            input ("phone", "phone", title: "Send with text message (optional)", description: "Phone Number", required: false)
			}
		}
        section ([mobileOnly:true]) {
            label title: "Assign a name", required: false
        }
    }
}

def installed () {
	log.debug "Installed with settings: ${settings}"
	state.position = 0
	state.wrongPosition = true
	state.origAlarmState = true
	init ()
}

def updated () {
	log.debug "Updated with settings: ${settings}"
	unsubscribe ()
	init ()
}

def init () {
    subscribe (location, "mode", nonCreepyHandler)
	subscribe (presence, "presence", nonCreepyHandler)
	subscribe (contact, "contact", contactHandler)
	subscribe (lock, "locked", contactHandler)
	subscribe (motion, "motion", motionHandler)
	nonCreepyHandler ()
	log.debug "init:  Current mode = ${location.mode}, people = ${presence.collect{it.label + ': ' + it.currentpresence}} & position = ${state.position}"
}

def notificationHandler (msg) {
	if (location.contactBookEnabled && recipients) {
	    sendNotificationToContacts(msg, recipients)
	} 
    else if (phone) { 
	    sendSms (phone, msg)
	}
}

def motionHandler (evt) {
    if ((evt.value == "active") && (location.mode in motionModes)) {
		state.wrongPosition = (state.position != motionPreset)
		def activeSensors = []
		motion.each {sensor ->
			if (sensor.currentMotion == "active") {
				activeSensors << sensor.label
			}
		}
		log.debug "motionHandler:  Active sensors:  ${activeSensors}. wrongPosition = ${state.wrongPosition}."
		notificationHandler ("Motion sensed on ${activeSensors}.  ${camera} is moving to preset ${state.position}")
		intruderHandler (motionPreset)
		camera?.take()
	}
}

def contactHandler (evt) {
	if (((evt.value == "open") || (evt.value == "unlocked")) && (location.mode in contactModes)) {
		state.wrongPosition = (state.position != contactPreset)
		def openSensors = []
		contact.each {sensor ->
			if (sensor.currentContact == "open") {
				openSensors << sensor.label
			}
		}
		log.debug "contactHandler:  Active sensors:  ${openSensors}. wrongPosition = ${state.wrongPosition}"
		notificationHandler ("Contact opened:  ${openSensors}.  ${camera} is moving to position ${state.position}")
		intruderHandler (contactPreset)
	}
}
	
def intruderHandler (preset) {
	log.debug "intruderHandler: requesting preset ${preset}"
	camera?.alarmOn ()
	camera?.ledOn ()
	if (state.wrongPosition) {
		state.position = preset
	}
	presetHandler ()
	runIn (alarmDuration*10, snapHandler)
	runIn (alarmDuration*60, nonCreepyHandler)
	log.debug "intruderHandler:  ${camera} armed and resetting in ${alarmDuration} minutes."
}

def nonCreepyHandler (evt) {
	state.nobodyHome = presence.find{it.currentPresence == "present"} == null
	state.activatePrivacy = ((location.mode in nonCreepyModes) && !state.nobodyHome)
    if (state.activatePrivacy) {
    	camera?.alarmOff ()
		camera?.ledAuto ()
		state.origAlarmState = false
		state.wrongPosition = (state.position != nonCreepyPosition)
		if (state.wrongPosition) {
			state.position = nonCreepyPosition
			log.debug "nonCreepyHandler:  ${camera} is moving to position ${state.position} & alarm is off."
			notificationHandler ("${camera} is moving to position ${state.position} & alarm is off.")
		} 
		runIn (alarmDuration*60, presetHandler)
    }
    else {	
    	camera?.alarmOn ()
		camera?.ledAuto ()
		state.origAlarmState = true
		state.wrongPosition = (state.position != returnPosition)
		if (state.wrongPosition) {
			state.position = returnPosition
			log.debug "nonCreepyHandler:  ${camera} is moving to position ${state.position} & alarm is on."
			notificationHandler("${camera} is moving to position ${state.position} & alarm is on.")
		}
		presetHandler ()
	}
}

def presetHandler () {
	camera?.ledAuto ()
	switch (state.position) {
	    case "1":
	        camera?.preset1 ()
	        break
	    case "2":
	        camera?.preset2 ()
	        break
	    case "3":
	        camera?.preset3 ()
	        break
	    case "4":
	        camera?.preset4 ()
	        break
	    case "5":
	        camera?.preset5 ()
	        break
	    case "6":
	        camera?.preset6 ()
	        break
	    default:
	        camera?.preset1 ()
	}
}

def snapHandler() {
	camera?.take()
}
