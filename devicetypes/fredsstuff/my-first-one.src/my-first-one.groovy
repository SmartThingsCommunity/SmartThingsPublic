/**
 *  My first one
 *
 *  Copyright 2020 Fred Baker
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
	definition (name: "My first one", namespace: "Fredsstuff", author: "Fred Baker", cstHandler: true) {
		capability "Audio Capture"
		capability "Audio Mute"
		capability "Audio Stream"
		capability "Audio Volume"
		capability "Video Camera"
		capability "Video Capture"
		capability "Video Clips"
		capability "Video Stream"
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
	// TODO: handle 'clip' attribute
	// TODO: handle 'stream' attribute
	// TODO: handle 'mute' attribute
	// TODO: handle 'uri' attribute
	// TODO: handle 'volume' attribute
	// TODO: handle 'camera' attribute
	// TODO: handle 'statusMessage' attribute
	// TODO: handle 'mute' attribute
	// TODO: handle 'settings' attribute
	// TODO: handle 'clip' attribute
	// TODO: handle 'stream' attribute
	// TODO: handle 'videoClip' attribute
	// TODO: handle 'stream' attribute

}

// handle commands
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

def startAudio() {
	log.debug "Executing 'startAudio'"
	// TODO: handle 'startAudio' command
}

def stopAudio() {
	log.debug "Executing 'stopAudio'"
	// TODO: handle 'stopAudio' command
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

def on() {
	log.debug "Executing 'on'"
	// TODO: handle 'on' command
}

def off() {
	log.debug "Executing 'off'"
	// TODO: handle 'off' command
}

def mute() {
	log.debug "Executing 'mute'"
	// TODO: handle 'mute' command
}

def unmute() {
	log.debug "Executing 'unmute'"
	// TODO: handle 'unmute' command
}

def flip() {
	log.debug "Executing 'flip'"
	// TODO: handle 'flip' command
}

def capture() {
	log.debug "Executing 'capture'"
	// TODO: handle 'capture' command
}

def captureClip() {
	log.debug "Executing 'captureClip'"
	// TODO: handle 'captureClip' command
}

def startStream() {
	log.debug "Executing 'startStream'"
	// TODO: handle 'startStream' command
}

def stopStream() {
	log.debug "Executing 'stopStream'"
	// TODO: handle 'stopStream' command
}