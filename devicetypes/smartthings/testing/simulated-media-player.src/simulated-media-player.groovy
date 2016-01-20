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
 */

 metadata {
	definition (name: "Simulated Media Player", namespace: "smartthings/testing", author: "SmartThings") {
		capability "Actuator"
		capability "Switch"
		capability "Refresh"
		capability "Sensor"
		capability "Music Player"
		/*
		Music Player capability attributes:
			status: string,
			level: number,
			mute, enum("muted","unmuted")
			trackDescription: string
			trackData: json
		Music Player capability commands:
			play
			pause
			stop
			previousTrack
			nextTrack
			mute
			unmute
			playTrack: string
			setTrack: string
			resumeTrack: string
			restoreTrack: string
			setLevel: number
			playText: string
		*/
	}

	tiles(scale: 2) {
		multiAttributeTile(name: "mediaMulti", type:"mediaPlayer", width:6, height:4) {
            tileAttribute("device.status", key: "PRIMARY_CONTROL") {
				attributeState("paused", label:"Paused",)
				attributeState("playing", label:"Playing")
				attributeState("stopped", label:"Stopped")
			}
			tileAttribute("device.status", key: "MEDIA_STATUS") {
				attributeState("paused", label:"Paused", action:"music Player.play", nextState: "playing")
				attributeState("playing", label:"Playing", action:"music Player.pause", nextState: "paused")
				attributeState("stopped", label:"Stopped", action:"music Player.play", nextState: "playing")
			}
			tileAttribute("device.status", key: "PREVIOUS_TRACK") {
				attributeState("default", action:"music Player.previousTrack")
			}
			tileAttribute("device.status", key: "NEXT_TRACK") {
				attributeState("default", action:"music Player.nextTrack")
			}
			tileAttribute ("device.level", key: "SLIDER_CONTROL") {
				attributeState("level", action:"media Player.setLevel")
			}
			tileAttribute ("device.mute", key: "MEDIA_MUTED") {
				attributeState("unmuted", action:"media Player.mute", nextState: "muted")
				attributeState("muted", action:"media Player.unmute", nextState: "unmuted")
			}
			tileAttribute("device.trackDescription", key: "MARQUEE") {
				attributeState("default", label:"${currentValue}")
			}
		}

		// Row 1
		standardTile("nextTrack", "device.status", width: 2, height: 2, decoration: "flat") {
			state "next", label:'', action:"music Player.nextTrack", icon:"st.sonos.next-btn", backgroundColor:"#ffffff"
		}
		standardTile("play", "device.status", width: 2, height: 2, decoration: "flat") {
			state "default", label:'', action:"music Player.play", icon:"st.sonos.play-btn", nextState:"playing", backgroundColor:"#ffffff"
			//   state "grouped", label:'', action:"music Player.play", icon:"st.sonos.play-btn", backgroundColor:"#ffffff"
		}
		standardTile("previousTrack", "device.status", width: 2, height: 2, decoration: "flat") {
			state "previous", label:'', action:"music Player.previousTrack", icon:"st.sonos.previous-btn", backgroundColor:"#ffffff"
		}

		// Row 2
		standardTile("status", "device.status", width: 2, height: 2, decoration: "flat", canChangeIcon: true) {
			state "playing", label:'Playing', action:"music Player.pause", icon:"st.Electronics.electronics1", nextState:"paused", backgroundColor:"#ffffff"
			state "stopped", label:'Stopped', action:"music Player.play", icon:"st.Electronics.electronics1", nextState:"playing", backgroundColor:"#ffffff"
			state "paused", label:'Paused', action:"music Player.play", icon:"st.Electronics.electronics1", nextState:"playing", backgroundColor:"#ffffff"
			//   state "grouped", label:'Grouped', action:"", icon:"st.Electronics.electronics16", backgroundColor:"#ffffff"
		}
		standardTile("pause", "device.status", width: 2, height: 2, decoration: "flat") {
			state "default", label:'', action:"music Player.pause", icon:"st.sonos.pause-btn", nextState:"paused", backgroundColor:"#ffffff"
			//   state "grouped", label:'', action:"music Player.pause", icon:"st.sonos.pause-btn", backgroundColor:"#ffffff"
		}
		standardTile("mute", "device.mute", inactiveLabel: false, decoration: "flat") {
			state "unmuted", label:"", action:"music Player.mute", icon:"st.custom.sonos.unmuted", backgroundColor:"#ffffff", nextState:"muted"
			state "muted", label:"", action:"music Player.unmute", icon:"st.custom.sonos.muted", backgroundColor:"#ffffff", nextState:"unmuted"
		}

		// Row 3
		controlTile("levelSliderControl", "device.level", "slider", height: 2, width: 6, inactiveLabel: false, range:"0..30") {
			state "level", action:"tileSetLevel", backgroundColor:"#ffffff"
		}

		// Row 4
		valueTile("currentSong", "device.trackDescription", inactiveLabel: true, height:2, width:6, decoration: "flat") {
			state "default", label:'${currentValue}', backgroundColor:"#ffffff"
		}

		main "mediaMulti"

		details([])
	}
}

def installed() {
	state.tracks = [
		"Gangnam Style (강남스타일)\nPSY\nPsy 6 (Six Rules), Part 1",
		"Careless Whisper\nWham!\nMake It Big",
		"Never Gonna Give You Up\nRick Astley\nWhenever You Need Somebody",
		"Shake It Off\nTaylor Swift\n1989",
		"Ironic\nAlanis Morissette\nJagged Little Pill",
		"Hotline Bling\nDrake\nHotline Bling - Single"
	]
	state.currentTrack = 0

	sendEvent(name: "level", value: 72)
	sendEvent(name: "mute", value: "unmuted")
	sendEvent(name: "status", value: "stopped")
}

def parse(description) {
	// Nah, we're good. Thanks, though.
}

def play() {
	sendEvent(name: "status", value: "playing")
	sendEvent(name: "trackDescription", value: state.tracks[state.currentTrack])
}

def pause() {
	sendEvent(name: "status", value: "paused")
	sendEvent(name: "trackDescription", value: state.tracks[state.currentTrack])
}

def stop() {
	sendEvent(name: "status", value: "stopped")
}

def previousTrack() {
	state.currentTrack = state.currentTrack - 1
	if (state.currentTrack < 0)
		state.currentTrack = state.tracks.length-1

	sendEvent(name: "trackDescription", value: state.tracks[state.currentTrack])
}

def nextTrack() {
	state.currentTrack = state.currentTrack + 1
	if (state.currentTrack == state.tracks.length)
		state.currentTrack = 0

	sendEvent(name: "trackDescription", value: state.tracks[state.currentTrack])
}

def mute() {
	sendEvent(name: "mute", value: "muted")
}

def unmute() {
	sendEvent(name: "mute", value: "unmuted")
}

def setLevel(Double level) {
	sendEvent(name: "level", value: level)
}