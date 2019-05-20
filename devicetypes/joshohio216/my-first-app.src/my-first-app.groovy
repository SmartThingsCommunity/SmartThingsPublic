/**
 *  My First App
 *
 *  Copyright 2019 Joshua Calhoun
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
	definition (name: "My First App", namespace: "Joshohio216", author: "Joshua Calhoun", cstHandler: true) {
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
	// TODO: handle 'wifiNetworkName' attribute
	// TODO: handle 'wifiGuestNetworkName' attribute
	// TODO: handle 'connectedRouterCount' attribute
	// TODO: handle 'disconnectedRouterCount' attribute
	// TODO: handle 'connectedDeviceCount' attribute
	// TODO: handle 'wifiNetworkStatus' attribute
	// TODO: handle 'wifiGuestNetworkStatus' attribute

}

// handle commands
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