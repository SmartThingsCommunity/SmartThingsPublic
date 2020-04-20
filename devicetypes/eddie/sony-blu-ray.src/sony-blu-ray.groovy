/**
 *  Sony Blu-ray
 *
 *  Copyright 2017 Eddie winter
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
	definition (name: "Sony Blu-ray", namespace: "Eddie", author: "Eddie winter") {
		capability "Acceleration Sensor"
		capability "Buffered Video Capture"
		capability "Location Mode"
		capability "Media Controller"
		capability "Refresh"
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
	// TODO: handle 'acceleration' attribute
	// TODO: handle 'clip' attribute
	// TODO: handle 'mode' attribute
	// TODO: handle 'activities' attribute
	// TODO: handle 'currentActivity' attribute
	// TODO: handle 'stream' attribute

}

// handle commands
def capture() {
	log.debug "Executing 'capture'"
	// TODO: handle 'capture' command
}

def setMode() {
	log.debug "Executing 'setMode'"
	// TODO: handle 'setMode' command
}

def startActivity() {
	log.debug "Executing 'startActivity'"
	// TODO: handle 'startActivity' command
}

def refresh() {
	log.debug "Executing 'refresh'"
	// TODO: handle 'refresh' command
}

def startStream() {
	log.debug "Executing 'startStream'"
	// TODO: handle 'startStream' command
}

def stopStream() {
	log.debug "Executing 'stopStream'"
	// TODO: handle 'stopStream' command
}