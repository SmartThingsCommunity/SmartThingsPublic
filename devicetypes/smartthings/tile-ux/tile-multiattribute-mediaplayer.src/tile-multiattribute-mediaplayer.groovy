/**
 *  Copyright 2016 SmartThings, Inc.
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
	definition (
		name: "mediaPlayerDeviceTile",
		namespace: "smartthings/tile-ux",
		author: "SmartThings") {

		capability "Actuator"
		capability "Switch"
		capability "Refresh"
		capability "Sensor"
		capability "Music Player"
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
				attributeState("status", action:"music Player.previousTrack", defaultState: true)
			}
			tileAttribute("device.status", key: "NEXT_TRACK") {
				attributeState("status", action:"music Player.nextTrack", defaultState: true)
			}
			tileAttribute ("device.level", key: "SLIDER_CONTROL") {
				attributeState("level", action:"music Player.setLevel")
			}
			tileAttribute ("device.mute", key: "MEDIA_MUTED") {
				attributeState("unmuted", action:"music Player.mute", nextState: "muted")
				attributeState("muted", action:"music Player.unmute", nextState: "unmuted")
			}
			tileAttribute("device.trackDescription", key: "MARQUEE") {
				attributeState("trackDescription", label:"${currentValue}", defaultState: true)
			}
		}

		main "mediaMulti"
		details(["mediaMulti"])
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
	// No parsing will happen with this simulated device.
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
		state.currentTrack = state.tracks.size()-1

	sendEvent(name: "trackDescription", value: state.tracks[state.currentTrack])
}

def nextTrack() {
	state.currentTrack = state.currentTrack + 1
	if (state.currentTrack == state.tracks.size())
		state.currentTrack = 0

	sendEvent(name: "trackDescription", value: state.tracks[state.currentTrack])
}

def mute() {
	sendEvent(name: "mute", value: "muted")
}

def unmute() {
	sendEvent(name: "mute", value: "unmuted")
}

def setLevel(level) {
	sendEvent(name: "level", value: level)
}
