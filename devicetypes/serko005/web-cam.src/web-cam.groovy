/**
 *  Web Cam
 *
 *  Copyright 2017 Serko
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
	definition (name: "Web Cam", namespace: "serko005", author: "Serko") {
		capability "Video Camera"
		capability "Video Capture"
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
	// TODO: handle 'camera' attribute
	// TODO: handle 'statusMessage' attribute
	// TODO: handle 'mute' attribute
	// TODO: handle 'settings' attribute
	// TODO: handle 'clip' attribute
	// TODO: handle 'stream' attribute
	// TODO: handle 'stream' attribute

}

// handle commands
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

def startStream() {
	log.debug "Executing 'startStream'"
	// TODO: handle 'startStream' command
}

def stopStream() {
	log.debug "Executing 'stopStream'"
	// TODO: handle 'stopStream' command
}