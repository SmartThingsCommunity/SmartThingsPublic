/**
 *  My Device
 *
 *  Copyright 2019 Samuel Burns
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
metadata {
	definition (name: "My Device", namespace: "Google", author: "Samuel Burns", cstHandler: true) {
		capability "Alarm"
		capability "Audio Capture"
		capability "Audio Mute"
		capability "Audio Notification"
		capability "Audio Stream"
		capability "Audio Track Addressing"
		capability "Audio Track Data"
		capability "Audio Volume"
		capability "Speech Recognition"
		capability "Speech Synthesis"
	}


	simulator {
		// TODO: define status and reply messages here
	}

	tiles {
		// TODO: define your main and details tiles here
	}
}

// parse events into attributes
def parse(String description) {
	log.debug "Parsing '${description}'"
	// TODO: handle 'alarm' attribute
	// TODO: handle 'clip' attribute
	// TODO: handle 'stream' attribute
	// TODO: handle 'mute' attribute
	// TODO: handle 'uri' attribute
	// TODO: handle 'audioTrackData' attribute
	// TODO: handle 'totalTime' attribute
	// TODO: handle 'elapsedTime' attribute
	// TODO: handle 'volume' attribute
	// TODO: handle 'phraseSpoken' attribute

}

// handle commands
def off() {
	log.debug "Executing 'off'"
	// TODO: handle 'off' command
}

def strobe() {
	log.debug "Executing 'strobe'"
	// TODO: handle 'strobe' command
}

def siren() {
	log.debug "Executing 'siren'"
	// TODO: handle 'siren' command
}

def both() {
	log.debug "Executing 'both'"
	// TODO: handle 'both' command
}

def capture() {
	log.debug "Executing 'capture'"
	// TODO: handle 'capture' command
}

def setMute() {
	log.debug "Executing 'setMute'"
	// TODO: handle 'setMute' command
}

def mute() {
	log.debug "Executing 'mute'"
	// TODO: handle 'mute' command
}

def unmute() {
	log.debug "Executing 'unmute'"
	// TODO: handle 'unmute' command
}

def playTrack() {
	log.debug "Executing 'playTrack'"
	// TODO: handle 'playTrack' command
}

def playTrackAndResume() {
	log.debug "Executing 'playTrackAndResume'"
	// TODO: handle 'playTrackAndResume' command
}

def playTrackAndRestore() {
	log.debug "Executing 'playTrackAndRestore'"
	// TODO: handle 'playTrackAndRestore' command
}

def startAudio() {
	log.debug "Executing 'startAudio'"
	// TODO: handle 'startAudio' command
}

def stopAudio() {
	log.debug "Executing 'stopAudio'"
	// TODO: handle 'stopAudio' command
}

def setAudioTrack() {
	log.debug "Executing 'setAudioTrack'"
	// TODO: handle 'setAudioTrack' command
}

def setVolume() {
	log.debug "Executing 'setVolume'"
	// TODO: handle 'setVolume' command
}

def volumeUp() {
	log.debug "Executing 'volumeUp'"
	// TODO: handle 'volumeUp' command
}

def volumeDown() {
	log.debug "Executing 'volumeDown'"
	// TODO: handle 'volumeDown' command
}

def speak() {
	log.debug "Executing 'speak'"
	// TODO: handle 'speak' command
}