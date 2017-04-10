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
 *
 *  Speaker Custom Message
 *
 *  Author: SmartThings
 *  Date: 2014-1-29
 */
definition(
	name: "Speaker Notify with Sound",
	namespace: "smartthings",
	author: "SmartThings",
	description: "Play a sound or custom message through your Speaker when the mode changes or other events occur.",
	category: "SmartThings Labs",
	iconUrl: "http://cdn.device-icons.smartthings.com/Electronics/electronics16-icn.png",
	iconX2Url: "http://cdn.device-icons.smartthings.com/Electronics/electronics16-icn@2x.png"
)

preferences {
	page(name: "mainPage", title: "Play a message on your Speaker when something happens", install: true, uninstall: true)
	page(name: "changeVolume", title: "Enter volume level (%)")
	page(name: "timeIntervalInput", title: "Only during a certain time") {
		section {
			input "starting", "time", title: "Starting", required: false
			input "ending", "time", title: "Ending", required: false
		}
	}
}

def mainPage() {
	dynamicPage(name: "mainPage") {
		section {
			// Select speaker
			speakers()
		}
		// If speaker is selected...
		if (sonos) {
			def anythingSet = false
			// Display selected triggers
			section("Play when", hideable: true, hidden: true, hideWhenEmpty: true) {
				anythingSet = triggers(true)
			}
			// Display triggers that can be selected
			def hideable = anythingSet || app.installationState == "COMPLETE"
			def sectionTitle = anythingSet ? "Select additional triggers" : "Play message when..."
			section(sectionTitle, hideable: hideable, hidden: true, hideWhenEmpty: true) {
				triggers(false)
			}
			// If a trigger is set, display actions to take
			if(anythingSet) {
				section {
					action()
				}
			}
			// If a trigger and an actions is set display more options
			if (anythingSet && actionType) {
				section("More options", hideable: true, hidden: true) {
					input "resumePlaying", "bool", title: "Resume currently playing music after notification", required: false, defaultValue: true, submitOnChange: true
					// TODO: As speakers may have different ways/options to save/play songs, add manufacturer check when all speakers have added manufacturer and model
					def appManufacturer = sonos.currentState("manufacturer")
//					if (appManufacturer?.value?.contains("Bose") || appManufacturer?.value?.contains("Sonos")) {
					// For now, make sure this isn't displayed for Samsung speaker
					if (!appManufacturer?.value?.contains("Samsung")) {
						chooseTrack()
					}
					// Make sure volume input is in range
					if (volume && ((1 > volume) || (100 < volume))) {
						sendEvent(value:"Volume $volume, which is outside of allowed range (0-100)", eventType: "ALERT")
						app.updateSetting("volume", "")
						settings["volume"] = ""
					}
					// To be able to do range check of volume, need to display it on another page as 'submitOnChange: true'
					// is causing StaleObjectStateException for 'typing' input fields
					href "changeVolume", title: "Temporarily change volume", description: volume ?: "0-100%", state: volume ? "complete" : "incomplete"
					input "frequency", "decimal", title: "Minimum time between actions (defaults to every event)", description: "Minutes", required: false
					href "timeIntervalInput", title: "Only during a certain time", description: timeLabel ?: "Tap to set", state: timeLabel ? "complete" : "incomplete"
					input "days", "enum", title: "Only on certain days of the week", multiple: true, required: false,
						options: ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"], submitOnChange: true
					if (settings.modes) {
						input "modes", "mode", title: "Only when mode is", multiple: true, required: false, submitOnChange: true
					}
					input "oncePerDay", "bool", title: "Only once per day", required: false, defaultValue: false, submitOnChange: true
				}
				section([mobileOnly:true]) {
					label title: "Assign a name", required: false
					mode title: "Set for specific mode(s)", required: false, submitOnChange: true
				}
			}
		}
	}
}

def speakers() {
	input "sonos", "capability.musicPlayer", title: "On this Speaker player", required: true, submitOnChange: true

	if (!sonos || (sonos?.id != state.speakerId)) {
		// No or New speaker -> reset speaker data
		state.songOptions = null
		state.selectedSong = null
		state.speakerId = sonos?.id
		app.updateSetting("song", "")
		settings["song"] = ""
	}
	if (sonos) {
		// Update list of songs to play
		songOptions()
	}
}

def action() {
	if (sonos) {
		input "actionType", "enum", title: "Play", required: true, options: [
			"Custom Message",
			"Bell 1",
			"Bell 2",
			"Dogs Barking",
			"Fire Alarm",
			"The mail has arrived",
			"A door opened",
			"There is motion",
			"Smartthings detected a flood",
			"Smartthings detected smoke",
			"Someone is arriving",
			"Piano",
			"Lightsaber"], submitOnChange: true

		if (actionType == "Custom Message") {
			input "message","text",title:"Play this message", required:true, multiple: false
		}
	}
}

def chooseTrack() {
	input "song","enum",title:"Or play this music or radio station", required:false, description: song ? state.selectedSong?.station : "Tap to set", state: song ? "complete" : "incomplete", multiple: false, options: state.songOptions, submitOnChange: true

	if (song?.trim()) {
		saveSelectedSong()
	}

}

private songOptions() {
	def appManufacturer = sonos.currentState("manufacturer")
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
	if (appManufacturer?.value?.contains("Bose")) {
		// Bose speaker, get preset chanels (1-6)
		def preset
		def notSet
		for (int idx = 1; idx < 7; idx++) {
			preset = sonos.currentState("station${idx}")?.value
			notSet = "preset ${idx} not set"
			// Don't display presets not currently set
			if (preset && (preset.toLowerCase() != notSet)) {
				options << preset
			}
		}
		state.songOptions = options as List
	} else {
		// Until Sonos is updated with manufacturer data, assume Sonos speaker
		def states = sonos.statesSince("trackData", new Date(0), [max:8])
		def dataMaps = states.collect{it.jsonValue}
		options.addAll(dataMaps.collect{it.station})
		log.trace "${options.size()} songs in list"
		state.songOptions = options.take(20) as List
	}
}

private saveSelectedSong() {
	try {
		def thisSong = song
		log.info "Looking for $thisSong"
		def appManufacturer = sonos.currentState("manufacturer")
		if (appManufacturer?.value?.contains("Bose")) {
			// Bose speaker, get preset chanels (1-6)
			def preset
			for (int idx = 1; idx < 7; idx++) {
				preset = sonos.currentState("station${idx}")
				if (thisSong == preset?.value) {
					state.selectedSong = [name:"preset${idx}", station: thisSong]
					break
				}
			}
		} else {
			// Until Sonos is updated with manufacturer data, assume Sonos speaker
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
	}
	catch (Throwable t) {
		log.error t
	}
}

private changeVolume() {
	dynamicPage(name: "changeVolume") {
		section {
			input "volume", "number", description: volume ?: "0-100%", required: false
		}
	}
}

private displaySetting(boolean display, name) {
	return ((display && settings[name]) || (!display && !settings[name]))
}

private triggers(boolean displayConfigured) {
	boolean somethingSet = false
	def sensors = [[name:"motion", title:"Motion Here"],
					[name:"contact", title:"Contact Opens"],
					[name:"contactClosed", capability:"capability.contactSensor", title:"Contact Closes"],
					[name:"acceleration", title:"Acceleration Detected"],
					[name:"mySwitch", capability:"capability.switch", title:"Switch Turned On"],
					[name:"mySwitchOff", capability:"capability.switch", title: "Switch Turned Off"],
					[name:"arrivalPresence", capability:"capability.presenceSensor", title: "Arrival Of"],
					[name:"departurePresence", capability:"capability.presenceSensor", title: "Departure Of"],
					[name:"smoke", capability:"capability.smokeDetector", title: "Smoke Detected"],
					[name:"water", title: "Water Sensor Wet"],
					[name:"button1", capability:"capability.button", title: "Button Press"]]
	def events = [[name:"triggerModes", trigger:"mode", title: "System Changes Mode", multiple: true],
					[name:"timeOfDay", trigger:"time", title: "At a Scheduled Time", multiple: false]]

	for (item in sensors) {
		if (displaySetting(displayConfigured, item.name)) {
			somethingSet = true
			input item.name, (item.capability ? item.capability : "capability.${item.name}Sensor"), hideWhenEmpty: true, title: item.title, required: false, multiple: true, submitOnChange: true
		}
	}
	for (item in events) {
		if (displaySetting(displayConfigured, item.name)) {
			somethingSet = true
			input item.name, item.trigger, title: item.title, multiple: item.multiple, required: false, submitOnChange: true
		}
	}
	return somethingSet
}

def installed() {
	log.debug "Installed with settings: ${settings}"
	subscribeToEvents()
}

def updated() {
	log.debug "Updated with settings: ${settings}"
	unsubscribe()
	unschedule()
	subscribeToEvents()
}

def subscribeToEvents() {
	subscribe(app, appTouchHandler)
	subscribe(contact, "contact.open", eventHandler)
	subscribe(contactClosed, "contact.closed", eventHandler)
	subscribe(acceleration, "acceleration.active", eventHandler)
	subscribe(motion, "motion.active", eventHandler)
	subscribe(mySwitch, "switch.on", eventHandler)
	subscribe(mySwitchOff, "switch.off", eventHandler)
	subscribe(arrivalPresence, "presence.present", eventHandler)
	subscribe(departurePresence, "presence.not present", eventHandler)
	subscribe(smoke, "smoke.detected", eventHandler)
	subscribe(smoke, "smoke.tested", eventHandler)
	subscribe(smoke, "carbonMonoxide.detected", eventHandler)
	subscribe(water, "water.wet", eventHandler)
	subscribe(button1, "button.pushed", eventHandler)

	if (triggerModes) {
		subscribe(location, modeChangeHandler)
	}

	if (timeOfDay) {
		schedule(timeOfDay, scheduledTimeHandler)
	}

	loadText()
}

def eventHandler(evt) {
	log.trace "eventHandler($evt?.name: $evt?.value)"
	if (allOk) {
		log.trace "allOk"
		def lastTime = state[frequencyKey(evt)]
		if (oncePerDayOk(lastTime)) {
			if (frequency) {
				if (lastTime == null || now() - lastTime >= frequency * 60000) {
					takeAction(evt)
				}
				else {
					log.debug "Not taking action because $frequency minutes have not elapsed since last action"
				}
			}
			else {
				takeAction(evt)
			}
		}
		else {
			log.debug "Not taking action because it was already taken today"
		}
	}
}
def modeChangeHandler(evt) {
	log.trace "modeChangeHandler $evt.name: $evt.value ($triggerModes)"
	if (evt.value in triggerModes) {
		eventHandler(evt)
	}
}

def scheduledTimeHandler() {
	eventHandler(null)
}

def appTouchHandler(evt) {
	takeAction(evt)
}

private takeAction(evt) {

	log.trace "takeAction()"

	if (song?.trim()) {
		sonos.playSoundAndTrack(state.sound.uri, state.sound.duration, state.selectedSong, volume)
	}
	else if (resumePlaying){
		sonos.playTrackAndResume(state.sound.uri, state.sound.duration, volume)
	}
	else {
		sonos.playTrackAndRestore(state.sound.uri, state.sound.duration, volume)
	}

	if (frequency || oncePerDay) {
		state[frequencyKey(evt)] = now()
	}
	log.trace "Exiting takeAction()"
}

private frequencyKey(evt) {
	"lastActionTimeStamp"
}

private dayString(Date date) {
	def df = new java.text.SimpleDateFormat("yyyy-MM-dd")
	if (location.timeZone) {
		df.setTimeZone(location.timeZone)
	}
	else {
		df.setTimeZone(TimeZone.getTimeZone("America/New_York"))
	}
	df.format(date)
}

private oncePerDayOk(Long lastTime) {
	def result = true
	if (oncePerDay) {
		result = lastTime ? dayString(new Date()) != dayString(new Date(lastTime)) : true
		log.trace "oncePerDayOk = $result"
	}
	result
}

// TODO - centralize somehow
private getAllOk() {
	modeOk && daysOk && timeOk
}

private getModeOk() {
	def result = !modes || modes.contains(location.mode)
	log.trace "modeOk = $result"
	result
}

private getDaysOk() {
	def result = true
	if (days) {
		def df = new java.text.SimpleDateFormat("EEEE")
		if (location.timeZone) {
			df.setTimeZone(location.timeZone)
		}
		else {
			df.setTimeZone(TimeZone.getTimeZone("America/New_York"))
		}
		def day = df.format(new Date())
		result = days.contains(day)
	}
	log.trace "daysOk = $result"
	result
}

private getTimeOk() {
	def result = true
	if (starting && ending) {
		def currTime = now()
		def start = timeToday(starting, location?.timeZone).time
		def stop = timeToday(ending, location?.timeZone).time
		result = start < stop ? currTime >= start && currTime <= stop : currTime <= stop || currTime >= start
	}
	log.trace "timeOk = $result"
	result
}

private hhmm(time, fmt = "h:mm a")
{
	def t = timeToday(time, location.timeZone)
	def f = new java.text.SimpleDateFormat(fmt)
	f.setTimeZone(location.timeZone ?: timeZone(time))
	f.format(t)
}

private getTimeLabel()
{
	(starting && ending) ? hhmm(starting) + "-" + hhmm(ending, "h:mm a z") : ""
}
// TODO - End Centralize

private loadText() {
	switch ( actionType) {
		case "Bell 1":
			state.sound = [uri: "http://s3.amazonaws.com/smartapp-media/sonos/bell1.mp3", duration: "10"]
			break;
		case "Bell 2":
			state.sound = [uri: "http://s3.amazonaws.com/smartapp-media/sonos/bell2.mp3", duration: "10"]
			break;
		case "Dogs Barking":
			state.sound = [uri: "http://s3.amazonaws.com/smartapp-media/sonos/dogs.mp3", duration: "10"]
			break;
		case "Fire Alarm":
			state.sound = [uri: "http://s3.amazonaws.com/smartapp-media/sonos/alarm.mp3", duration: "17"]
			break;
		case "The mail has arrived":
			state.sound = [uri: "http://s3.amazonaws.com/smartapp-media/sonos/the+mail+has+arrived.mp3", duration: "1"]
			break;
		case "A door opened":
			state.sound = [uri: "http://s3.amazonaws.com/smartapp-media/sonos/a+door+opened.mp3", duration: "1"]
			break;
		case "There is motion":
			state.sound = [uri: "http://s3.amazonaws.com/smartapp-media/sonos/there+is+motion.mp3", duration: "1"]
			break;
		case "Smartthings detected a flood":
			state.sound = [uri: "http://s3.amazonaws.com/smartapp-media/sonos/smartthings+detected+a+flood.mp3", duration: "2"]
			break;
		case "Smartthings detected smoke":
			state.sound = [uri: "http://s3.amazonaws.com/smartapp-media/sonos/smartthings+detected+smoke.mp3", duration: "1"]
			break;
		case "Someone is arriving":
			state.sound = [uri: "http://s3.amazonaws.com/smartapp-media/sonos/someone+is+arriving.mp3", duration: "1"]
			break;
		case "Piano":
			state.sound = [uri: "http://s3.amazonaws.com/smartapp-media/sonos/piano2.mp3", duration: "10"]
			break;
		case "Lightsaber":
			state.sound = [uri: "http://s3.amazonaws.com/smartapp-media/sonos/lightsaber.mp3", duration: "10"]
			break;
		case "Custom Message":
			if (message) {
				state.sound = textToSpeech(message instanceof List ? message[0] : message) // not sure why this is (sometimes) needed)
			}
			else {
				state.sound = textToSpeech("You selected the custom message option but did not enter a message in the $app.label Smart App")
			}
			break;
		default:
			state.sound = [uri: "http://s3.amazonaws.com/smartapp-media/sonos/bell1.mp3", duration: "10"]
			break;
	}
}
