/**
 *  scorpionek85@hotmail.com Account
 *
 *  Copyright 2019 Andrzej Oziembłowski
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
	definition (name: "scorpionek85@hotmail.com Account", namespace: "scorpionek85", author: "Andrzej Oziembłowski", cstHandler: true) {
		capability "Samsung TV"
		capability "TV"
		capability "Wifi Mesh Router"
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
	// TODO: handle 'volume' attribute
	// TODO: handle 'mute' attribute
	// TODO: handle 'pictureMode' attribute
	// TODO: handle 'soundMode' attribute
	// TODO: handle 'switch' attribute
	// TODO: handle 'messageButton' attribute
	// TODO: handle 'volume' attribute
	// TODO: handle 'channel' attribute
	// TODO: handle 'power' attribute
	// TODO: handle 'picture' attribute
	// TODO: handle 'sound' attribute
	// TODO: handle 'movieMode' attribute
	// TODO: handle 'wifiNetworkName' attribute
	// TODO: handle 'wifiGuestNetworkName' attribute
	// TODO: handle 'connectedRouterCount' attribute
	// TODO: handle 'disconnectedRouterCount' attribute
	// TODO: handle 'connectedDeviceCount' attribute
	// TODO: handle 'wifiNetworkStatus' attribute
	// TODO: handle 'wifiGuestNetworkStatus' attribute

}

// handle commands
def volumeUp() {
	log.debug "Executing 'volumeUp'"
	// TODO: handle 'volumeUp' command
}

def volumeDown() {
	log.debug "Executing 'volumeDown'"
	// TODO: handle 'volumeDown' command
}

def setVolume() {
	log.debug "Executing 'setVolume'"
	// TODO: handle 'setVolume' command
}

def mute() {
	log.debug "Executing 'mute'"
	// TODO: handle 'mute' command
}

def unmute() {
	log.debug "Executing 'unmute'"
	// TODO: handle 'unmute' command
}

def setPictureMode() {
	log.debug "Executing 'setPictureMode'"
	// TODO: handle 'setPictureMode' command
}

def setSoundMode() {
	log.debug "Executing 'setSoundMode'"
	// TODO: handle 'setSoundMode' command
}

def on() {
	log.debug "Executing 'on'"
	// TODO: handle 'on' command
}

def off() {
	log.debug "Executing 'off'"
	// TODO: handle 'off' command
}

def showMessage() {
	log.debug "Executing 'showMessage'"
	// TODO: handle 'showMessage' command
}

def volumeUp() {
	log.debug "Executing 'volumeUp'"
	// TODO: handle 'volumeUp' command
}

def volumeDown() {
	log.debug "Executing 'volumeDown'"
	// TODO: handle 'volumeDown' command
}

def channelUp() {
	log.debug "Executing 'channelUp'"
	// TODO: handle 'channelUp' command
}

def channelDown() {
	log.debug "Executing 'channelDown'"
	// TODO: handle 'channelDown' command
}

def enableWifiNetwork() {
	log.debug "Executing 'enableWifiNetwork'"
	// TODO: handle 'enableWifiNetwork' command
}

def disableWifiNetwork() {
	log.debug "Executing 'disableWifiNetwork'"
	// TODO: handle 'disableWifiNetwork' command
}

def enableWifiGuestNetwork() {
	log.debug "Executing 'enableWifiGuestNetwork'"
	// TODO: handle 'enableWifiGuestNetwork' command
}

def disableWifiGuestNetwork() {
	log.debug "Executing 'disableWifiGuestNetwork'"
	// TODO: handle 'disableWifiGuestNetwork' command
}