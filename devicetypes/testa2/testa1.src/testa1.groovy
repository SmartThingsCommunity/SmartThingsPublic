/**
 *  testa1
 *
 *  Copyright 2018 PunitGohel
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
	definition (name: "testa1", namespace: "testa2", author: "PunitGohel") {
		capability "Alarm System"
		capability "Audio Track Addressing"
		capability "Battery"
		capability "Bridge"

		attribute "testa3", "string"

		command "testa4"

		fingerprint endpointId: "testa5", profileId: "testa6", deviceId: "testa7", deviceVersion: "testa8", inClusters: "testa9", outClusters: "testa10", noneClusters: "testa11"
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
	// TODO: handle 'alarmSystemStatus' attribute
	// TODO: handle 'battery' attribute
	// TODO: handle 'testa3' attribute

}

// handle commands
def sendEvent(alarmSystemStatus,off)() {
	log.debug "Executing 'sendEvent(alarmSystemStatus,off)'"
	// TODO: handle 'sendEvent(alarmSystemStatus,off)' command
}

def sendEvent(alarmSystemStatus,stay)() {
	log.debug "Executing 'sendEvent(alarmSystemStatus,stay)'"
	// TODO: handle 'sendEvent(alarmSystemStatus,stay)' command
}

def sendEvent(alarmSystemStatus,away)() {
	log.debug "Executing 'sendEvent(alarmSystemStatus,away)'"
	// TODO: handle 'sendEvent(alarmSystemStatus,away)' command
}

def setAudioTrack() {
	log.debug "Executing 'setAudioTrack'"
	// TODO: handle 'setAudioTrack' command
}

def testa4() {
	log.debug "Executing 'testa4'"
	// TODO: handle 'testa4' command
}