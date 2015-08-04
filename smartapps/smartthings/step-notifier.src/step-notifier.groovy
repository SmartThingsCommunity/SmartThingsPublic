/**
 *  Step Notifier
 *
 *  Copyright 2014 Jeff's Account
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
    name: "Step Notifier",
    namespace: "smartthings",
    author: "SmartThings",
    description: "Use a step tracker device to track daily step goals and trigger various device actions when your goals are met!",
    category: "SmartThings Labs",
	iconUrl: "https://s3.amazonaws.com/smartapp-icons/Partner/jawbone-up.png",
	iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Partner/jawbone-up@2x.png"
)

preferences {
	page(name: "setupNotifications")
    page(name: "chooseTrack", title: "Select a song or station")
	page(name: "timeIntervalInput", title: "Only during a certain time") {
		section {
			input "starting", "time", title: "Starting", required: false
			input "ending", "time", title: "Ending", required: false
		}
	}
}

def setupNotifications() {
    
    dynamicPage(name: "setupNotifications", title: "Configure Your Goal Notifications.", install: true, uninstall: true) {	
    
		section("Select your Jawbone UP") {
			input "jawbone", "device.jawboneUser", title: "Jawbone UP", required: true, multiple: false
		}
           
     	section("Notify Me When"){
			input "thresholdType", "enum", title: "Select When to Notify", required: false, defaultValue: "Goal Reached", options: [["Goal":"Goal Reached"],["Threshold":"Specific Number of Steps"]], submitOnChange:true
            if (settings.thresholdType) {
                if (settings.thresholdType == "Threshold") {
                	input "threshold", "number", title: "Enter Step Threshold", description: "Number", required: true
                }
            }
		}
        
		section("Via a push notification and/or an SMS message"){
            input("recipients", "contact", title: "Send notifications to") {
                input "phone", "phone", title: "Phone Number (for SMS, optional)", required: false
                input "notificationType", "enum", title: "Select Notification", required: false, defaultValue: "None", options: ["None", "Push", "SMS", "Both"]
            }
		}
        
        section("Flash the Lights") {
        	input "lights", "capability.switch", title: "Which Lights?", required: false, multiple: true
        	input "flashCount", "number", title: "How Many Times?", defaultValue: 5, required: false           
        }
        
        section("Change the Color of the Lights") {
        	input "hues", "capability.colorControl", title: "Which Hue Bulbs?", required:false, multiple:true
            input "color", "enum", title: "Hue Color?", required: false, multiple:false, options: ["Red","Green","Blue","Yellow","Orange","Purple","Pink"]
			input "lightLevel", "enum", title: "Light Level?", required: false, options: [[10:"10%"],[20:"20%"],[30:"30%"],[40:"40%"],[50:"50%"],[60:"60%"],[70:"70%"],[80:"80%"],[90:"90%"],[100:"100%"]]
			input "duration", "number", title: "Duration in Seconds?", defaultValue: 30, required: false
        }
        
        section("Play a song on the Sonos") {
			input "sonos", "capability.musicPlayer", title: "On this Sonos player", required: false, submitOnChange:true
            if (settings.sonos) {
				input "song","enum",title:"Play this track or radio station", required:true, multiple: false, options: songOptions()  
				input "resumePlaying", "bool", title: "Resume currently playing music after notification", required: false, defaultValue: true                
                input "volume", "number", title: "Temporarily change volume", description: "0-100%", required: false
                input "songDuration", "number", title: "Play for this many seconds", defaultValue: 60, description: "0-100%", required: true
            }

		}
    }
}

def chooseTrack() {
	dynamicPage(name: "chooseTrack") {
		section{
			input "song","enum",title:"Play this track", required:true, multiple: false, options: songOptions()
		}
	}
}

private songOptions() {

	// Make sure current selection is in the set

	def options = new LinkedHashSet()
	if (state.selectedSong?.station) {
		options << state.selectedSong.station
	}
	else if (state.selectedSong?.description) {
		// TODO - Remove eventually? 'description' for backward compatibility
		options << state.selectedSong.description
	}

	// Query for recent tracks
	def states = sonos.statesSince("trackData", new Date(0), [max:30])
	def dataMaps = states.collect{it.jsonValue}
	options.addAll(dataMaps.collect{it.station})

	log.trace "${options.size()} songs in list"
	options.take(20) as List
}

private saveSelectedSong() {
	try {
		def thisSong = song
		log.info "Looking for $thisSong"
		def songs = sonos.statesSince("trackData", new Date(0), [max:30]).collect{it.jsonValue}
		log.info "Searching ${songs.size()} records"

		def data = songs.find {s -> s.station == thisSong}
		log.info "Found ${data?.station}"
		if (data) {
			state.selectedSong = data
			log.debug "Selected song = $state.selectedSong"
		}
		else if (song == state.selectedSong?.station) {
			log.debug "Selected existing entry '$song', which is no longer in the last 20 list"
		}
		else {
			log.warn "Selected song '$song' not found"
		}
	}
	catch (Throwable t) {
		log.error t
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

	log.trace "Entering initialize()"
    
	state.lastSteps = 0
    state.steps = jawbone.currentValue("steps").toInteger()
    state.goal = jawbone.currentValue("goal").toInteger()
    
	subscribe (jawbone,"goal",goalHandler)
    subscribe (jawbone,"steps",stepHandler)
    
    if (song) {
		saveSelectedSong()
	}
    
	log.trace "Exiting initialize()"    
}

def goalHandler(evt) {

	log.trace "Entering goalHandler()"

	def goal = evt.value.toInteger()
    
    state.goal = goal
    
    log.trace "Exiting goalHandler()"
}

def stepHandler(evt) {

	log.trace "Entering stepHandler()"
    
    log.debug "Event Value ${evt.value}"
    log.debug "state.steps = ${state.steps}"
    log.debug "state.goal = ${state.goal}"

	def steps = evt.value.toInteger()
    
    state.lastSteps = state.steps
    state.steps = steps
    
    def stepGoal
    if (settings.thresholdType == "Goal")
    	stepGoal = state.goal
    else
    	stepGoal = settings.threshold
    
    if ((state.lastSteps < stepGoal) && (state.steps >= stepGoal)) { // only trigger when crossing through the goal threshold
    
    // goal achieved for the day! Yay! Lets tell someone!
    
    	if (settings.notificationType != "None") { // Push or SMS Notification requested

            if (location.contactBookEnabled) {
                sendNotificationToContacts(stepMessage, recipients)
            }
            else {

                def options = [
                    method: settings.notificationType.toLowerCase(),
                    phone: settings.phone
                ]

                sendNotification(stepMessage, options)
            }
        }
        
        if (settings.sonos) { // play a song on the Sonos as requested
        
        	// runIn(1, sonosNotification, [overwrite: false])
            sonosNotification()
            
        }  
        
        if (settings.hues) { // change the color of hue bulbs ras equested
        
        	// runIn(1, hueNotification, [overwrite: false])
            hueNotification()
        
        }        
        
        if (settings.lights) { // flash the lights as requested
        
			// runIn(1, lightsNotification, [overwrite: false])
        	lightsNotification()
        
        }
    
    }
    
	log.trace "Exiting stepHandler()"    

}


def lightsNotification() {

	// save the current state of the lights 
    
    log.trace "Save current state of lights"
    
	state.previousLights = [:]

	lights.each {
		state.previousLights[it.id] = it.currentValue("switch")
	}
    
    // Flash the light on and off 5 times for now - this could be configurable 
            
    log.trace "Now flash the lights"
    
    for (i in 1..flashCount) {
           
    	lights.on()
        pause(500)
        lights.off()
               
    }
    
    // restore the original state
    
    log.trace "Now restore the original state of lights"    
    
 	lights.each {
		it."${state.previousLights[it.id]}"()
	}   


}

def hueNotification() {

	log.trace "Entering hueNotification()"

	def hueColor = 0
	if(color == "Blue")
		hueColor = 70//60
	else if(color == "Green")
		hueColor = 39//30
	else if(color == "Yellow")
		hueColor = 25//16
	else if(color == "Orange")
		hueColor = 10
	else if(color == "Purple")
		hueColor = 75
	else if(color == "Pink")
		hueColor = 83


	state.previousHue = [:]

	hues.each {
		state.previousHue[it.id] = [
			"switch": it.currentValue("switch"),
			"level" : it.currentValue("level"),
			"hue": it.currentValue("hue"),
			"saturation": it.currentValue("saturation")
		]
	}

	log.debug "current values = ${state.previousHue}"

	def newValue = [hue: hueColor, saturation: 100, level: (lightLevel as Integer) ?: 100]
	log.debug "new value = $newValue"

	hues*.setColor(newValue)
	setTimer()
    
	log.trace "Exiting hueNotification()"
    
}

def setTimer()
{
	log.debug "runIn ${duration}, resetHue"
	runIn(duration, resetHue, [overwrite: false])
}


def resetHue()
{
    log.trace "Entering resetHue()"
	settings.hues.each {
		it.setColor(state.previousHue[it.id])
	}
    log.trace "Exiting resetHue()"    
}

def sonosNotification() {

	log.trace "sonosNotification()"
    
    if (settings.song) {
    
   		if (settings.resumePlaying) {
     		if (settings.volume)
				sonos.playTrackAndResume(state.selectedSong, settings.songDuration, settings.volume)
            else
            	sonos.playTrackAndResume(state.selectedSong, settings.songDuration)
        } else {
        	if (settings.volume)
 				sonos.playTrackAtVolume(state.selectedSong, settings.volume)
            else
                sonos.playTrack(state.selectedSong)
        }
        
		sonos.on() // make sure it is playing
        
	}

	log.trace "Exiting sonosNotification()"
}
