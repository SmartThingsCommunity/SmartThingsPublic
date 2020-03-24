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
 *  Sonos Mood Music
 *
 *  Author: SmartThings
 *  Date: 2014-02-12
 */


private songOptions() {

	// Make sure current selection is in the set

	def options = new LinkedHashSet()
	options << "STOP PLAYING"
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

private saveSelectedSongs() {
	try {
		def songs = sonos.statesSince("trackData", new Date(0), [max:30]).collect{it.jsonValue}
		log.info "Searching ${songs.size()} records"

		if (!state.selectedSongs) {
			state.selectedSongs = [:]
		}

		settings.each {name, thisSong ->
			if (thisSong == "STOP PLAYING") {
				state.selectedSongs."$name" = "PAUSE"
			}
			if (name.startsWith("mode_")) {
				log.info "Looking for $thisSong"

				def data = songs.find {s -> s.station == thisSong}
				log.info "Found ${data?.station}"
				if (data) {
					state.selectedSongs."$name" = data
					log.debug "Selected song = $data.station"
				}
				else if (song == state.selectedSongs."$name"?.station) {
					log.debug "Selected existing entry '$thisSong', which is no longer in the last 20 list"
				}
				else {
					log.warn "Selected song '$thisSong' not found"
				}
			}
		}
	}
	catch (Throwable t) {
		log.error t
	}
}

definition(
    name: "Sonos Music Modes",
    namespace: "smartthings",
    author: "SmartThings",
    description: "Plays a different selected song or station for each mode.",
    category: "SmartThings Internal",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Partner/sonos.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Partner/sonos@2x.png"
)

preferences {
	page(name: "mainPage", title: "Play a message on your Sonos when something happens", nextPage: "chooseTrack", uninstall: true)
	page(name: "chooseTrack", title: "Select a song", install: true)
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
			input "sonos", "capability.musicPlayer", title: "Sonos player", required: true
		}
		section("More options", hideable: true, hidden: true) {
			input "volume", "number", title: "Set the volume", description: "0-100%", required: false
			input "frequency", "decimal", title: "Minimum time between actions (defaults to every event)", description: "Minutes", required: false
			href "timeIntervalInput", title: "Only during a certain time", description: timeLabel ?: "Tap to set", state: timeLabel ? "complete" : "incomplete"
			input "days", "enum", title: "Only on certain days of the week", multiple: true, required: false,
				options: ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"]
			if (settings.modes) {
            	input "modes", "mode", title: "Only when mode is", multiple: true, required: false
            }
			input "oncePerDay", "bool", title: "Only once per day", required: false, defaultValue: false
		}
	}
}

def chooseTrack() {
	dynamicPage(name: "chooseTrack") {
		section("Play a different song for each mode in which you want music") {
			def options = songOptions()
			location.modes.each {mode ->
				input "mode_$mode.name", "enum", title: mode.name, options: options, required: false
			}
		}
		section([mobileOnly:true]) {
			label title: "Assign a name", required: false
			mode title: "Set for specific mode(s)", required: false
		}
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
	log.trace "subscribeToEvents()"
	saveSelectedSongs()

	subscribe(location, modeChangeHandler)
}

def modeChangeHandler(evt) {
	log.trace "modeChangeHandler($evt.name: $evt.value)"
	if (allOk) {
		if (frequency) {
			def lastTime = state[frequencyKey(evt)]
			if (lastTime == null || now() - lastTime >= frequency * 60000) {
				takeAction(evt)
			}
		}
		else {
			takeAction(evt)
		}
	}
}

private takeAction(evt) {

	def name = "mode_$evt.value".toString()
	def selectedSong = state.selectedSongs."$name"

	if (selectedSong == "PAUSE") {
		sonos.stop()
	}
	else {
		log.info "Playing '$selectedSong"

		if (volume != null) {
			sonos.stop()
			pause(500)
			sonos.setLevel(volume)
			pause(500)
		}

		sonos.playTrack(selectedSong)
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
	def result = lastTime ? dayString(new Date()) != dayString(new Date(lastTime)) : true
	log.trace "oncePerDayOk = $result"
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

private timeIntervalLabel()
{
	(starting && ending) ? hhmm(starting) + "-" + hhmm(ending, "h:mm a z") : ""
}
// TODO - End Centralize

