/**
 *  KeyWe Doorlock Handler
 *
 *  Copyright 2021 Smartlock KeyWe
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
	definition (name: "KeyWe Doorlock Handler", namespace: "Hardkeywe", author: "Smartlock KeyWe", cstHandler: true) {
		capability "Lock"
		capability "Door Control"
		capability "Security System"

		fingerprint mfr: "037B", prod: "0001", model: "0012", deviceJoinName: "KeyWe Smart Doorlock"
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
	// TODO: handle 'lock' attribute
	// TODO: handle 'door' attribute
	// TODO: handle 'securitySystemStatus' attribute
	// TODO: handle 'alarm' attribute

}

// handle commands
def lock() {
	log.debug "Executing 'lock'"
	// TODO: handle 'lock' command
}

def unlock() {
	log.debug "Executing 'unlock'"
	// TODO: handle 'unlock' command
}

def open() {
	log.debug "Executing 'open'"
	// TODO: handle 'open' command
}

def close() {
	log.debug "Executing 'close'"
	// TODO: handle 'close' command
}

def armStay() {
	log.debug "Executing 'armStay'"
	// TODO: handle 'armStay' command
}

def armAway() {
	log.debug "Executing 'armAway'"
	// TODO: handle 'armAway' command
}

def disarm() {
	log.debug "Executing 'disarm'"
	// TODO: handle 'disarm' command
}