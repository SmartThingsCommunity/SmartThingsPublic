/**
 *  Xiaomi Aqara Smart Window Door Sensor
 *
 *  Copyright 2019 Carlos Garcia
 *https://graph-eu01-euwest1.api.smartthings.com/ide/device/editor/27801f06-b744-45e6-af58-699d9a639a99#
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
	definition (name: "Xiaomi Aqara Smart Window Door Sensor", namespace: "Door Sensor", author: "Carlos Garcia", cstHandler: true) {
		capability "Door Control"
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
	// TODO: handle 'door' attribute

}

// handle commands
def open() {
	log.debug "Executing 'open'"
	// TODO: handle 'open' command
}

def close() {
	log.debug "Executing 'close'"
	// TODO: handle 'close' command
}